package com.myriam.unrp.uo;

import com.myriam.unrp.pass.UnrpPowerLayers;
import com.myriam.unrp.pass.ViewUp;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class ClientVertigo {
   public static final double SEEN = 160.0D;
   public static final double HALO_SEEN = 48.0D;
   public static final float STONE = 0.32F;
   public static final int MOST_CRATERS = 48;
   public static final double NEAR_CAM = 1.6D;
   private static final ResourceLocation WHITE = new ResourceLocation("unrp", "textures/misc/white.png");
   private static final float[] MOON = new float[]{0.86F, 0.91F, 0.95F};
   private static final float[] SLATE = new float[]{0.37F, 0.43F, 0.52F};
   private static final float[] PALE = new float[]{0.66F, 0.72F, 0.8F};
   private static final float[] SOOT = new float[]{0.06F, 0.05F, 0.05F};
   private static final float[] INDIGO = new float[]{0.29F, 0.27F, 0.63F};
   private static final float[] GRASS = new float[]{0.4F, 0.58F, 0.24F};
   private static final Map<UUID, ClientVertigo.Ball> BALLS = new ConcurrentHashMap();
   private static final List<ClientVertigo.Crater> CRATERS = new ArrayList();
   private static final Map<String, ClientVertigo.ZoneFx> ZONES = new ConcurrentHashMap();
   private static final Map<Integer, ClientVertigo.Orbit> ORBITS = new ConcurrentHashMap();
   private static final List<Integer> HALOS = new ArrayList();
   static final float[] UP = ViewUp.UP;

   private ClientVertigo() {
   }

   private static BlockState state(String name) {
      try {
         Block b = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
         return b != null && b != Blocks.AIR ? b.defaultBlockState() : Blocks.DIRT.defaultBlockState();
      } catch (RuntimeException var2) {
         return Blocks.DIRT.defaultBlockState();
      }
   }

   public static void acceptBall(LandfallPacket msg) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         if (!msg.end && !msg.spots.isEmpty()) {
            BALLS.put(msg.owner, new ClientVertigo.Ball(mc.level, msg));
         } else {
            BALLS.remove(msg.owner);
         }
      }
   }

   public static void acceptCrater(CraterPacket msg) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         if (CRATERS.size() >= 48) {
            CRATERS.remove(0);
         }

         CRATERS.add(new ClientVertigo.Crater(mc.level, msg, mc.level.getGameTime()));
      }
   }

   public static void acceptZone(ZonePacket msg) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         String key = String.valueOf(msg.owner) + (msg.heavy ? ":mill" : ":shear");
         if (msg.end) {
            ZONES.remove(key);
         } else {
            ZONES.put(key, new ClientVertigo.ZoneFx(mc.level, msg));
         }
      }
   }

   public static void acceptStones(StonesPacket msg) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         if (msg.until <= 0L) {
            ORBITS.remove(msg.entity);
         } else {
            ORBITS.put(msg.entity, new ClientVertigo.Orbit(mc.level, state(msg.block), msg.until));
         }
      }
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level == null) {
            if (!BALLS.isEmpty() || !CRATERS.isEmpty() || !ZONES.isEmpty() || !ORBITS.isEmpty() || !HALOS.isEmpty()) {
               clearAll();
            }

         } else {
            long now = mc.level.getGameTime();
            HALOS.clear();
            if (mc.player != null) {
               for(AbstractClientPlayer p : mc.level.players()) {
                  if (p.position().distanceToSqr(mc.player.position()) <= 2304.0D && UnrpPowerLayers.powerVariant(p, "nadir") != 0) {
                     HALOS.add(p.getId());
                  }
               }
            }

            long flight = 120L;
            BALLS.values().removeIf((b) -> b.level != mc.level || now - b.began > flight);
            CRATERS.removeIf((c) -> c.level != mc.level || now - c.at > (long)c.keep);
            ZONES.values().removeIf((z) -> z.level != mc.level || now > z.until + 10L);
            ORBITS.values().removeIf((o) -> o.level != mc.level || now > o.until);
         }
      }
   }

   static void look(PoseStack ps) {
      Vector3f n = ps.last().normal().transform(new Vector3f(0.0F, 1.0F, 0.0F));
      UP[0] = n.x();
      UP[1] = n.y();
      UP[2] = n.z();
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_SKY) {
         look(event.getPoseStack());
      } else if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         look(event.getPoseStack());
         Minecraft mc = Minecraft.getInstance();
         ClientLevel level = mc.level;
         if (level != null) {
            Vec3 cam = event.getCamera().getPosition();
            float pt = event.getPartialTick();
            double now = (double)level.getGameTime() + (double)pt;
            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            PoseStack ps = event.getPoseStack();
            List<AbstractClientPlayer> halos = new ArrayList();

            for(Integer hid : HALOS) {
               Entity o_0 = level.getEntity(hid);
               if (o_0 instanceof AbstractClientPlayer) {
                  AbstractClientPlayer p_0 = (AbstractClientPlayer)o_0;
                  if (haloFor(mc, p_0, cam)) {
                     halos.add(p_0);
                  }
               }
            }

            if (!halos.isEmpty() || !CRATERS.isEmpty() || !ZONES.isEmpty() || !BALLS.isEmpty()) {
               RenderType type = RenderType.entityTranslucentEmissive(WHITE);
               Matrix4f m = ps.last().pose();
               float fogStart = RenderSystem.getShaderFogStart();
               RenderSystem.setShaderFogStart(1000000.0F);

               try {
                  VertexConsumer vc = buffers.getBuffer(type);

                  for(ClientVertigo.Crater c : CRATERS) {
                     if (c.level == level) {
                        crater(vc, m, c, cam, now);
                     }
                  }

                  for(ClientVertigo.ZoneFx z : ZONES.values()) {
                     if (z.level == level) {
                        if (z.heavy) {
                           mill(vc, m, level, z, cam, now, pt);
                        } else if (follow(mc, level, z, pt)) {
                           shear(vc, m, z, cam, now);
                        }
                     }
                  }

                  for(ClientVertigo.Ball b_0 : BALLS.values()) {
                     if (b_0.level == level) {
                        telegraph(vc, m, b_0, cam, now);
                     }
                  }

                  for(AbstractClientPlayer p : halos) {
                     halo(vc, m, p, cam, now, pt);
                  }

                  buffers.endBatch(type);
               } catch (Throwable var21) {
                  CRATERS.clear();
                  ZONES.clear();
               } finally {
                  RenderSystem.setShaderFogStart(fogStart);
               }
            }

            if (!BALLS.isEmpty() || !ORBITS.isEmpty()) {
               try {
                  BlockRenderDispatcher br = mc.getBlockRenderer();

                  for(ClientVertigo.Ball b : BALLS.values()) {
                     if (b.level == level) {
                        ball(ps, buffers, br, level, b, cam, now);
                     }
                  }

                  for(Map.Entry<Integer, ClientVertigo.Orbit> en : ORBITS.entrySet()) {
                     ClientVertigo.Orbit o = (ClientVertigo.Orbit)en.getValue();
                     Entity e = level.getEntity(en.getKey());
                     if (o.level == level && e instanceof LivingEntity) {
                        LivingEntity live = (LivingEntity)e;
                        if (live.isAlive()) {
                           stones(ps, buffers, br, level, live, o, cam, now, pt);
                        }
                     }
                  }

                  buffers.endBatch();
               } catch (Throwable var20) {
                  BALLS.clear();
                  ORBITS.clear();
               }

            }
         }
      }
   }

   private static boolean haloFor(Minecraft mc, AbstractClientPlayer p, Vec3 cam) {
      if (!p.isInvisible() && !p.isSpectator() && !(p.position().distanceToSqr(cam) > 2304.0D)) {
         return p != mc.player || mc.options.getCameraType() != CameraType.FIRST_PERSON;
      } else {
         return false;
      }
   }

   private static void ball(PoseStack ps, MultiBufferSource.BufferSource buffers, BlockRenderDispatcher br, ClientLevel level, ClientVertigo.Ball b, Vec3 cam, double now) {
      double age = now - (double)b.began;
      double lift = NadirEvents.landfallLift(age);
      double cy = b.y + lift;
      if (!(Math.abs(b.x - cam.x) > 160.0D) && !(Math.abs(b.z - cam.z) > 160.0D)) {
         float spin = (float)(age * 2.2D);
         double shake = age < 12.0D ? (12.0D - age) * 0.015D * Math.sin(age * 3.1D) : 0.0D;
         int light = LevelRenderer.getLightColor(level, BlockPos.containing(b.x, cy + 3.0D, b.z));
         ps.pushPose();

         try {
            ps.translate(b.x - cam.x + shake, cy - cam.y, b.z - cam.z - shake);
            ps.mulPose(Axis.YP.rotationDegrees(spin));
            ps.mulPose(Axis.XP.rotationDegrees(spin * 0.35F));

            for(int[] s : b.spots) {
               ps.pushPose();

               try {
                  ps.translate((double)s[0] - 0.5D, (double)s[1] - 0.5D, (double)s[2] - 0.5D);
                  br.renderSingleBlock(b.states[s[3]], ps, buffers, light, OverlayTexture.NO_OVERLAY);
               } finally {
                  ps.popPose();
               }
            }
         } finally {
            ps.popPose();
         }

      }
   }

   private static void stones(PoseStack ps, MultiBufferSource.BufferSource buffers, BlockRenderDispatcher br, ClientLevel level, LivingEntity e, ClientVertigo.Orbit o, Vec3 cam, double now, float pt) {
      double ex = Mth.lerp((double)pt, e.xo, e.getX());
      double ey = Mth.lerp((double)pt, e.yo, e.getY()) + (double)e.getBbHeight() * 0.5D;
      double ez = Mth.lerp((double)pt, e.zo, e.getZ());
      double r = Math.max(1.0D, (double)e.getBbWidth() * 1.6D);
      int light = LevelRenderer.getLightColor(level, BlockPos.containing(ex, ey, ez));

      for(int i = 0; i < 6; ++i) {
         double a = now * 0.25D + (double)i * Math.PI / 3.0D;
         double sx = ex + Math.cos(a) * r;
         double sy = ey + Math.sin(a * 1.7D) * 0.25D;
         double sz = ez + Math.sin(a) * r;
         float size = 0.32F * (i % 2 == 0 ? 1.0F : 0.75F);
         ps.pushPose();

         try {
            ps.translate(sx - cam.x, sy - cam.y, sz - cam.z);
            ps.mulPose(Axis.YP.rotationDegrees((float)(now * 7.0D + (double)i * 40.0D)));
            ps.mulPose(Axis.XP.rotationDegrees((float)(now * 5.0D + (double)i * 25.0D)));
            ps.scale(size, size, size);
            ps.translate(-0.5D, -0.5D, -0.5D);
            br.renderSingleBlock(o.state, ps, buffers, light, OverlayTexture.NO_OVERLAY);
         } finally {
            ps.popPose();
         }
      }

   }

   private static void telegraph(VertexConsumer vc, Matrix4f m, ClientVertigo.Ball b, Vec3 cam, double now) {
      double age = now - (double)b.began;
      if (!(age < 20.0D)) {
         double gy = b.y + 1.5D + 0.06D;
         double pulse = 0.55D + 0.35D * Math.sin(now * 0.6D);
         float a = (float)(Mth.clamp((age - 20.0D) / 10.0D, 0.0D, 1.0D) * pulse);
         double reach = 9.0D;
         flatRing(vc, m, cam, b.x, gy, b.z, reach - 0.18D, reach, 0.0D, MOON, 0.7F * a, 0.7F * a, 48);
         flatRing(vc, m, cam, b.x, gy, b.z, reach * 0.33D, reach * 0.33D + 0.14D, now * 0.02D, INDIGO, 0.8F * a, 0.8F * a, 24);

         for(int i = 0; i < 4; ++i) {
            double t = now * 0.01D + (double)i * Math.PI * 0.5D;
            double x0 = b.x + Math.cos(t) * reach * 0.45D;
            double z0 = b.z + Math.sin(t) * reach * 0.45D;
            double x1 = b.x + Math.cos(t) * reach * 0.85D;
            double z1 = b.z + Math.sin(t) * reach * 0.85D;
            flatLine(vc, m, cam, x0, gy, z0, x1, gy, z1, 0.08D, MOON, 0.6F * a);
         }

         double lift = NadirEvents.landfallLift(age);
         double bottom = b.y + lift - 2.5D;
         if (bottom > gy + 0.5D) {
            line(vc, m, cam, new Vec3(b.x, bottom, b.z), new Vec3(b.x, gy, b.z), 0.05D, MOON, 0.5F * a);
            cube(vc, m, cam, b.x, gy + 0.12D, b.z, 0.12D, MOON, 0.8F * a);
         }

      }
   }

   private static void crater(VertexConsumer vc, Matrix4f m, ClientVertigo.Crater c, Vec3 cam, double now) {
      if (!(Math.abs(c.x - cam.x) > 160.0D) && !(Math.abs(c.z - cam.z) > 160.0D)) {
         double life = now - (double)c.at;
         float a = (float)(Mth.clamp(life / 3.0D, 0.0D, 1.0D) * Mth.clamp(((double)c.keep - life) / 80.0D, 0.0D, 1.0D));
         if (!(a <= 0.01F)) {
            double y = c.y + 0.03D;
            double r = (double)c.radius;
            if (!c.real) {
               flatRing(vc, m, cam, c.x, y, c.z, 0.0D, r * 0.75D, 0.0D, SOOT, 0.62F * a, 0.5F * a, 32);
               flatRing(vc, m, cam, c.x, y, c.z, r * 0.75D, r * 1.15D, 0.0D, SOOT, 0.5F * a, 0.0F, 32);
            }

            if (!c.real) {
               flatRing(vc, m, cam, c.x, y + 0.005D, c.z, r * 0.98D, r * 1.1D, 0.0D, SLATE, 0.3F * a, 0.12F * a, 32);
            }

            double start = c.real ? 1.05D : 0.55D;
            long h = c.seed;
            int cracks = 7 + (int)Math.floorMod(h, 5L);

            for(int i = 0; i < cracks; ++i) {
               h = h * 6364136223846793005L + 1442695040888963407L;
               double ang = (Math.PI * 2D) * (double)i / (double)cracks + (double)((h >>> 33) % 1000L) / 1000.0D * 0.5D;
               double px = c.x + Math.cos(ang) * r * start;
               double pz = c.z + Math.sin(ang) * r * start;
               int segs = 4;

               for(int k = 0; k < segs; ++k) {
                  h = h * 6364136223846793005L + 1442695040888963407L;
                  double turn = ((double)((h >>> 33) % 1000L) / 1000.0D - 0.5D) * 0.9D;
                  double len = r * (0.22D + (double)((h >>> 20) % 100L) / 100.0D * 0.18D);
                  double nx = px + Math.cos(ang + turn) * len;
                  double nz = pz + Math.sin(ang + turn) * len;
                  float fade = 1.0F - (float)k / (float)segs;
                  flatLine(vc, m, cam, px, y + 0.01D, pz, nx, y + 0.01D, nz, 0.09D * (double)fade + 0.03D, SOOT, 0.6F * a * fade);
                  px = nx;
                  pz = nz;
               }
            }

         }
      }
   }

   private static boolean follow(Minecraft mc, ClientLevel level, ClientVertigo.ZoneFx z, float pt) {
      Player owner = level.getPlayerByUUID(z.owner);
      if (owner == null) {
         return true;
      } else if (owner.isInvisible() && owner != mc.player) {
         return false;
      } else {
         z.x = Mth.lerp((double)pt, owner.xo, owner.getX());
         z.y = Mth.lerp((double)pt, owner.yo, owner.getY());
         z.z = Mth.lerp((double)pt, owner.zo, owner.getZ());
         return true;
      }
   }

   private static boolean nearCam(Vec3 cam, double x, double y, double z) {
      double dx = x - cam.x;
      double dy = y - cam.y;
      double dz = z - cam.z;
      return dx * dx + dy * dy + dz * dz < 2.5600000000000005D;
   }

   private static void shear(VertexConsumer vc, Matrix4f m, ClientVertigo.ZoneFx z, Vec3 cam, double now) {
      if (!(Math.abs(z.x - cam.x) > 160.0D) && !(Math.abs(z.z - cam.z) > 160.0D)) {
         float fade = (float)(Mth.clamp((now - (double)z.began) / 6.0D, 0.0D, 1.0D) * Mth.clamp(((double)z.until - now) / 10.0D, 0.0D, 1.0D));
         if (!(fade <= 0.01F)) {
            double[] pull = GravityFrame.pull(z.dir);
            double px = pull[0];
            double pz = pull[2];
            double qx = -pz;
            double qz = px;
            double r = z.radius;
            flatRing(vc, m, cam, z.x, z.y + 0.06D, z.z, r - 0.14D, r, 0.0D, MOON, 0.4F * fade, 0.4F * fade, 48);

            for(int i = 0; i < 20; ++i) {
               double ang = (Math.PI * 2D) * (double)i / 20.0D + now * 0.004D;
               double ax = z.x + Math.cos(ang) * r;
               double az = z.z + Math.sin(ang) * r;
               double ay = z.y + 2.4D + 0.15D * Math.sin(now * 0.1D + (double)i);
               double sway = 0.12D * Math.sin(now * 0.15D + (double)i * 1.3D);
               double ex = ax + (px + qx * sway) * 1.5D;
               double ez = az + (pz + qz * sway) * 1.5D;
               double ey = ay - 0.1D;
               line(vc, m, cam, new Vec3(ax, ay, az), new Vec3(ex, ey, ez), 0.035D, MOON, 0.75F * fade);
               cube(vc, m, cam, ax, ay, az, 0.045D, SLATE, 0.8F * fade);
               cube(vc, m, cam, ex, ey, ez, 0.09D, MOON, 0.9F * fade);
            }

            for(int i = 0; i < 70; ++i) {
               double h1_0 = hash(i * 3 + 1);
               double h2_0 = hash(i * 3 + 2);
               double h3_0 = hash(i * 3 + 3);
               double lateral_0 = (h1_0 * 2.0D - 1.0D) * r;
               double travel_0 = (now * 0.9D + h3_0 * 2.0D * r) % (2.0D * r) - r;
               if (!(lateral_0 * lateral_0 + travel_0 * travel_0 > r * r)) {
                  double sx_0 = z.x + qx * lateral_0 + px * travel_0;
                  double sz_0 = z.z + qz * lateral_0 + pz * travel_0;
                  double sy_0 = z.y + 0.2D + h2_0 * 3.6D;
                  if (!nearCam(cam, sx_0, sy_0, sz_0)) {
                     line(vc, m, cam, new Vec3(sx_0 - px * 0.9D, sy_0, sz_0 - pz * 0.9D), new Vec3(sx_0, sy_0, sz_0), 0.025D, PALE, 0.45F * fade);
                  }
               }
            }

            for(int i = 0; i < 26; ++i) {
               double h1 = hash(i * 5 + 101);
               double h2 = hash(i * 5 + 102);
               double h3 = hash(i * 5 + 103);
               double lateral = (h1 * 2.0D - 1.0D) * r;
               double travel = (now * 0.6D + h3 * 2.0D * r) % (2.0D * r) - r;
               if (!(lateral * lateral + travel * travel > r * r)) {
                  double sx = z.x + qx * lateral + px * travel;
                  double sz = z.z + qz * lateral + pz * travel;
                  double sy = z.y + 0.15D + h2 * 0.9D + 0.2D * Math.sin(now * 0.3D + (double)i);
                  if (!nearCam(cam, sx, sy, sz)) {
                     double t = now * 0.4D + (double)i;
                     double dx = Math.cos(t) * 0.09D;
                     double dy = Math.sin(t * 1.3D) * 0.09D;
                     line(vc, m, cam, new Vec3(sx - dx, sy - dy, sz + dx), new Vec3(sx + dx, sy + dy, sz - dx), 0.05D, GRASS, 0.85F * fade);
                  }
               }
            }

         }
      }
   }

   private static void mill(VertexConsumer vc, Matrix4f m, ClientLevel level, ClientVertigo.ZoneFx z, Vec3 cam, double now, float pt) {
      if (!(Math.abs(z.x - cam.x) > 160.0D) && !(Math.abs(z.z - cam.z) > 160.0D)) {
         float fade = (float)(Mth.clamp((now - (double)z.began) / 6.0D, 0.0D, 1.0D) * Mth.clamp(((double)z.until - now) / 10.0D, 0.0D, 1.0D));
         if (!(fade <= 0.01F)) {
            double r = z.radius;

            for(int k = 0; k < 4; ++k) {
               double t = (now / 24.0D + (double)k / 4.0D) % 1.0D;
               double yy = z.y + 0.08D + 6.0D * Math.pow(1.0D - t, 1.5D);
               float a = (float)(0.12D + 0.5D * t) * fade;
               flatRing(vc, m, cam, z.x, yy, z.z, r * 0.96D, r, 0.0D, MOON, a, a, 48);
            }

            for(int i = 0; i < 18; ++i) {
               double ang_0 = (Math.PI * 2D) * (double)i / 18.0D;
               double ax = z.x + Math.cos(ang_0) * r;
               double az = z.z + Math.sin(ang_0) * r;

               for(int d = 0; d < 5; ++d) {
                  double top = z.y + 6.0D - (now * 0.25D + (double)d * 1.2D + (double)i * 0.3D) % 6.0D;
                  double bot = Math.max(z.y + 0.05D, top - 0.6D);
                  if (top > z.y + 0.1D) {
                     line(vc, m, cam, new Vec3(ax, top, az), new Vec3(ax, bot, az), 0.03D, SLATE, 0.55F * fade);
                  }
               }
            }

            long h = z.owner.getLeastSignificantBits() ^ (long)(z.x * 13.0D) ^ (long)(z.z * 7.0D);
            double gy = z.y + 0.04D;

            for(int i = 0; i < 10; ++i) {
               double ang = (Math.PI * 2D) * (double)i / 10.0D + 0.2D;
               double px = z.x;
               double pz = z.z;

               for(int k = 0; k < 5; ++k) {
                  h = h * 6364136223846793005L + 1442695040888963407L;
                  double turn = ((double)((h >>> 33) % 1000L) / 1000.0D - 0.5D) * 0.7D;
                  double len = r * 0.19D;
                  double nx = px + Math.cos(ang + turn) * len;
                  double nz = pz + Math.sin(ang + turn) * len;
                  flatLine(vc, m, cam, px, gy, pz, nx, gy, nz, 0.1D - (double)k * 0.012D, SOOT, 0.55F * fade);
                  px = nx;
                  pz = nz;
               }
            }

            for(int ringI = 0; ringI < 2; ++ringI) {
               double rr = r * (ringI == 0 ? 0.4D : 0.72D);

               for(int k = 0; k < 12; ++k) {
                  h = h * 6364136223846793005L + 1442695040888963407L;
                  if ((h >>> 40 & 3L) != 0L) {
                     double a0 = (Math.PI * 2D) * (double)k / 12.0D;
                     double a1 = a0 + 0.41887902047863906D;
                     flatLine(vc, m, cam, z.x + Math.cos(a0) * rr, gy, z.z + Math.sin(a0) * rr, z.x + Math.cos(a1) * rr, gy, z.z + Math.sin(a1) * rr, 0.07D, SOOT, 0.45F * fade);
                  }
               }
            }

            flatRing(vc, m, cam, z.x, z.y + 0.1D, z.z, r * 0.82D, r * 0.94D, 0.0D, PALE, 0.0F, 0.28F * fade, 48);
            flatRing(vc, m, cam, z.x, z.y + 0.1D, z.z, r * 0.94D, r * 1.08D, 0.0D, PALE, 0.28F * fade, 0.0F, 48);
            AABB box = new AABB(z.x - r, z.y - 2.0D, z.z - r, z.x + r, z.y + 6.0D, z.z + r);

            for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, (exx) -> {
               if (exx.isAlive() && !exx.getUUID().equals(z.owner) && !exx.isSpectator()) {
                  if (!(exx instanceof Player)) {
                     return true;
                  }

                  Player pl = (Player)exx;
                  if (!pl.isCreative()) {
                     return true;
                  }
               }

               return false;
            })) {
               double ex = Mth.lerp((double)pt, e.xo, e.getX());
               double ey = Mth.lerp((double)pt, e.yo, e.getY()) + (double)e.getBbHeight();
               double ez = Mth.lerp((double)pt, e.zo, e.getZ());
               if (!((ex - z.x) * (ex - z.x) + (ez - z.z) * (ez - z.z) > r * r)) {
                  double bob = 0.05D * Math.sin(now * 0.5D + (double)e.getId());

                  for(int s = -1; s <= 1; s += 2) {
                     double off = (double)s * 0.09D;
                     line(vc, m, cam, new Vec3(ex + off, ey + 1.5D, ez), new Vec3(ex + off, ey + 0.35D + bob, ez), 0.05D, MOON, 0.8F * fade);
                  }

                  cube(vc, m, cam, ex, ey + 0.3D + bob, ez, 0.16D, SLATE, 0.9F * fade);
               }
            }

         }
      }
   }

   private static void halo(VertexConsumer vc, Matrix4f m, AbstractClientPlayer p, Vec3 cam, double now, float pt) {
      int dir = GravityGuard.direction(p);
      double x = Mth.lerp((double)pt, p.xo, p.getX());
      double y = Mth.lerp((double)pt, p.yo, p.getY());
      double z = Mth.lerp((double)pt, p.zo, p.getZ());
      double[] up = GravityFrame.up(dir);
      double[] e1 = GravityFrame.toWorld(dir, 1.0D, 0.0D, 0.0D);
      double[] e2 = GravityFrame.toWorld(dir, 0.0D, 0.0D, 1.0D);
      double wx = x + up[0] * 0.95D;
      double wy = y + up[1] * 0.95D;
      double wz = z + up[2] * 0.95D;
      double radius = 0.62D;
      int n = 32;

      for(int i = 0; i < n; ++i) {
         double a0 = (Math.PI * 2D) * (double)i / (double)n;
         double a1 = (Math.PI * 2D) * (double)(i + 1) / (double)n;
         Vec3 p0 = new Vec3(wx + (e1[0] * Math.cos(a0) + e2[0] * Math.sin(a0)) * radius, wy + (e1[1] * Math.cos(a0) + e2[1] * Math.sin(a0)) * radius, wz + (e1[2] * Math.cos(a0) + e2[2] * Math.sin(a0)) * radius);
         Vec3 p1 = new Vec3(wx + (e1[0] * Math.cos(a1) + e2[0] * Math.sin(a1)) * radius, wy + (e1[1] * Math.cos(a1) + e2[1] * Math.sin(a1)) * radius, wz + (e1[2] * Math.cos(a1) + e2[2] * Math.sin(a1)) * radius);
         line(vc, m, cam, p0, p1, 0.012D, MOON, 0.2F);
      }

      for(int k = 0; k < 6; ++k) {
         double a = now * 0.05D + (double)k * Math.PI / 3.0D;
         double cx = wx + (e1[0] * Math.cos(a) + e2[0] * Math.sin(a)) * radius;
         double cy = wy + (e1[1] * Math.cos(a) + e2[1] * Math.sin(a)) * radius;
         double cz = wz + (e1[2] * Math.cos(a) + e2[2] * Math.sin(a)) * radius;
         boolean lit = k == dir;
         cube(vc, m, cam, cx, cy, cz, lit ? 0.085D : 0.055D, lit ? MOON : SLATE, lit ? 0.95F : 0.6F);
      }

      if (!GravityFrame.plain(dir)) {
         double[] pull = GravityFrame.pull(dir);
         double yaw = Math.toRadians((double)Mth.lerp(pt, p.yBodyRotO, p.yBodyRot));
         double[] fwd = GravityFrame.toWorld(dir, -Math.sin(yaw), 0.0D, Math.cos(yaw));
         Vec3 start = new Vec3(x + up[0] * 1.35D + fwd[0] * 0.32D, y + up[1] * 1.35D + fwd[1] * 0.32D, z + up[2] * 1.35D + fwd[2] * 0.32D);
         Vec3 end = start.add(pull[0] * 1.4D, pull[1] * 1.4D, pull[2] * 1.4D);
         line(vc, m, cam, start, end, 0.03D, MOON, 0.7F);
         cube(vc, m, cam, end.x, end.y, end.z, 0.07D, MOON, 0.9F);
      }

   }

   static double hash(int i) {
      long h = (long)i * -7046029254386353131L;
      h ^= h >>> 31;
      h *= -4658895280553007687L;
      h ^= h >>> 29;
      return (double)(h >>> 11) * (double)1.110223E-16F;
   }

   static void flatRing(VertexConsumer vc, Matrix4f m, Vec3 cam, double cx, double cy, double cz, double inner, double outer, double turn, float[] rgb, float ai, float ao, int n) {
      float y = (float)(cy - cam.y);

      for(int i = 0; i < n; ++i) {
         double a0 = turn + (Math.PI * 2D) * (double)i / (double)n;
         double a1 = turn + (Math.PI * 2D) * (double)(i + 1) / (double)n;
         quad(vc, m, rgb, cx + Math.cos(a0) * inner - cam.x, (double)y, cz + Math.sin(a0) * inner - cam.z, cx + Math.cos(a1) * inner - cam.x, (double)y, cz + Math.sin(a1) * inner - cam.z, cx + Math.cos(a1) * outer - cam.x, (double)y, cz + Math.sin(a1) * outer - cam.z, cx + Math.cos(a0) * outer - cam.x, (double)y, cz + Math.sin(a0) * outer - cam.z, ai, ai, ao, ao);
      }

   }

   static void flatLine(VertexConsumer vc, Matrix4f m, Vec3 cam, double x0, double y0, double z0, double x1, double y1, double z1, double width, float[] rgb, float a) {
      double dx = x1 - x0;
      double dz = z1 - z0;
      double len = Math.sqrt(dx * dx + dz * dz);
      if (!(len < 1.0E-4D)) {
         double sx = -dz / len * width * 0.5D;
         double sz = dx / len * width * 0.5D;
         quad(vc, m, rgb, x0 + sx - cam.x, y0 - cam.y, z0 + sz - cam.z, x1 + sx - cam.x, y1 - cam.y, z1 + sz - cam.z, x1 - sx - cam.x, y1 - cam.y, z1 - sz - cam.z, x0 - sx - cam.x, y0 - cam.y, z0 - sz - cam.z, a, a, a, a);
      }
   }

   static void line(VertexConsumer vc, Matrix4f m, Vec3 cam, Vec3 a, Vec3 b, double width, float[] rgb, float alpha) {
      Vec3 d = b.subtract(a);
      Vec3 mid = a.add(b).scale(0.5D);
      Vec3 side = d.cross(cam.subtract(mid));
      double len = side.length();
      if (!(len < 1.0E-6D)) {
         side = side.scale(width * 0.5D / len);
         quad(vc, m, rgb, a.x + side.x - cam.x, a.y + side.y - cam.y, a.z + side.z - cam.z, b.x + side.x - cam.x, b.y + side.y - cam.y, b.z + side.z - cam.z, b.x - side.x - cam.x, b.y - side.y - cam.y, b.z - side.z - cam.z, a.x - side.x - cam.x, a.y - side.y - cam.y, a.z - side.z - cam.z, alpha, alpha, alpha, alpha);
      }
   }

   static void cube(VertexConsumer vc, Matrix4f m, Vec3 cam, double cx, double cy, double cz, double h, float[] rgb, float a) {
      double x0 = cx - h - cam.x;
      double x1 = cx + h - cam.x;
      double y0 = cy - h - cam.y;
      double y1 = cy + h - cam.y;
      double z0 = cz - h - cam.z;
      double z1 = cz + h - cam.z;
      quad(vc, m, rgb, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, a, a, a, a);
      quad(vc, m, rgb, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, a, a, a, a);
      quad(vc, m, rgb, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, a, a, a, a);
      quad(vc, m, rgb, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, a, a, a, a);
      quad(vc, m, rgb, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, a, a, a, a);
      quad(vc, m, rgb, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, a, a, a, a);
   }

   static void quad(VertexConsumer vc, Matrix4f m, float[] rgb, double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, float a0, float a1, float a2, float a3) {
      vtx(vc, m, rgb, x0, y0, z0, 0.0F, 1.0F, a0);
      vtx(vc, m, rgb, x1, y1, z1, 1.0F, 1.0F, a1);
      vtx(vc, m, rgb, x2, y2, z2, 1.0F, 0.0F, a2);
      vtx(vc, m, rgb, x3, y3, z3, 0.0F, 0.0F, a3);
   }

   static void vtx(VertexConsumer vc, Matrix4f m, float[] rgb, double x, double y, double z, float u, float v, float a) {
      vc.vertex(m, (float)x, (float)y, (float)z).color(rgb[0], rgb[1], rgb[2], a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(UP[0], UP[1], UP[2]).endVertex();
   }

   public static void clearAll() {
      BALLS.clear();
      CRATERS.clear();
      ZONES.clear();
      ORBITS.clear();
      HALOS.clear();
   }

   private static final class Ball {
      final ClientLevel level;
      final double x;
      final double y;
      final double z;
      final long began;
      final BlockState[] states;
      final int[][] spots;

      Ball(ClientLevel level, LandfallPacket msg) {
         this.level = level;
         this.x = msg.x;
         this.y = msg.y;
         this.z = msg.z;
         this.began = msg.began;
         this.states = new BlockState[Math.max(1, msg.names.size())];

         for(int i = 0; i < this.states.length; ++i) {
            this.states[i] = i < msg.names.size() ? ClientVertigo.state((String)msg.names.get(i)) : Blocks.DIRT.defaultBlockState();
         }

         this.spots = new int[msg.spots.size()][];

         for(int i = 0; i < this.spots.length; ++i) {
            int[] s = (int[])msg.spots.get(i);
            this.spots[i] = new int[]{s[0], s[1], s[2], Mth.clamp(s[3], 0, this.states.length - 1)};
         }

      }
   }

   private static final class Crater {
      final ClientLevel level;
      final double x;
      final double y;
      final double z;
      final float radius;
      final long at;
      final int keep;
      final boolean real;
      final long seed;

      Crater(ClientLevel level, CraterPacket msg, long at) {
         this.level = level;
         this.x = msg.x;
         this.y = msg.y;
         this.z = msg.z;
         this.radius = msg.radius;
         this.at = at;
         this.keep = msg.keep;
         this.real = msg.real;
         this.seed = Double.doubleToLongBits(msg.x * 31.0D + msg.z * 17.0D + msg.y);
      }
   }

   private static final class Orbit {
      final ClientLevel level;
      final BlockState state;
      final long until;

      Orbit(ClientLevel level, BlockState state, long until) {
         this.level = level;
         this.state = state;
         this.until = until;
      }
   }

   private static final class ZoneFx {
      final ClientLevel level;
      final UUID owner;
      final boolean heavy;
      double x;
      double y;
      double z;
      final double radius;
      final int dir;
      final long began;
      final long until;

      ZoneFx(ClientLevel level, ZonePacket msg) {
         this.level = level;
         this.owner = msg.owner;
         this.heavy = msg.heavy;
         this.x = msg.x;
         this.y = msg.y;
         this.z = msg.z;
         this.radius = (double)msg.radius;
         this.dir = msg.dir;
         this.began = msg.began;
         this.until = msg.until;
      }
   }
}
