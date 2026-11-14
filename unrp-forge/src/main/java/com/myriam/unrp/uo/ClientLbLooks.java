package com.myriam.unrp.uo;



import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class ClientLbLooks {
   public static final double SEEN = 96.0D;
   public static final double COOL_NEAR = 24.0D;
   public static final int STILL_HOLD = 18;
   public static final double STILL_RING = 7.0D;
   public static final float COOL_MOST = 0.24F;
   public static final float COPY_ALPHA = 0.55F;
   public static final int MOST = 48;
   public static final int MOST_COPIES = 6;
   public static final int MOST_NOTCHES = 60;
   public static final int DOME_SEGS = 24;
   public static final int DOME_RINGS = 8;
   public static final double NOVA_FADE = 3.0D;
   private static final ResourceLocation WHITE = new ResourceLocation("unrp", "textures/misc/white.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final float[] PURE = new float[]{1.0F, 1.0F, 1.0F};
   private static final float[] SILVER = new float[]{0.88F, 0.92F, 0.97F};
   private static final float[] DUST = new float[]{0.52F, 0.47F, 0.41F};
   private static final float[] EMBER = new float[]{1.0F, 0.55F, 0.22F};
   private static final float[] CHAR = new float[]{0.13F, 0.09F, 0.07F};
   private static final Map<Integer, long[]> ASCENDS = new HashMap();
   private static final Map<Integer, Long> COUNTERS = new HashMap();
   private static final Map<Integer, List<double[]>> COPIES = new HashMap();
   private static final List<ClientLbLooks.Look> LOOKS = new ArrayList();
   private static boolean inWorld;

   private ClientLbLooks() {
   }

   static void accept(OriginLookPacket msg, long now) {
      switch (msg.kind) {
         case 8:
            ASCENDS.put(msg.entity, new long[]{now + (long)msg.life, (long)msg.get(0)});
            break;
         case 9:
            List<double[]> list = (List)COPIES.computeIfAbsent(msg.entity, (k) -> new ArrayList());
            if (list.size() >= 6) {
               list.remove(0);
            }

            list.add(new double[]{msg.get(0), msg.get(1), msg.get(2), msg.get(3), msg.get(4), (double)now, (double)msg.life});
            break;
         case 10:
            if (msg.life <= 0) {
               COUNTERS.remove(msg.entity);
            } else {
               COUNTERS.put(msg.entity, now + (long)msg.life);
            }
            break;
         case 11:
            ClientSgHold.still(msg.get(0), msg.get(1) + 1.1D, msg.get(2), 18);
            add(msg, now);
            break;
         case 12:
            add(msg, now);
      }

   }

   private static void add(OriginLookPacket msg, long now) {
      if (LOOKS.size() >= 48) {
         LOOKS.remove(0);
      }

      LOOKS.add(new ClientLbLooks.Look(msg, now));
   }

   public static boolean inWorld() {
      return inWorld;
   }

   public static int ascension(int entity) {
      long[] a = (long[])ASCENDS.get(entity);
      return a == null ? 0 : (int)a[1];
   }

   public static boolean busy() {
      return !ASCENDS.isEmpty() || !COUNTERS.isEmpty() || !COPIES.isEmpty() || !LOOKS.isEmpty();
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level == null) {
            if (busy()) {
               clearAll();
            }

         } else {
            long now = mc.level.getGameTime();
            ASCENDS.values().removeIf((a) -> now > a[0]);
            COUNTERS.values().removeIf((c) -> now > c);
            COPIES.values().removeIf((list) -> {
               list.removeIf((c) -> now > (long)c[5] + (long)c[6] + 1L || now < (long)c[5] - 200L);
               return list.isEmpty();
            });
            LOOKS.removeIf((l) -> now > l.born + (long)l.msg.life + 2L || now < l.born - 200L);
         }
      }
   }

   public static float cool(double now) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && !LOOKS.isEmpty()) {
         float best = 0.0F;

         for(ClientLbLooks.Look l : LOOKS) {
            if (l.msg.kind == 11) {
               double age = now - (double)l.born;
               if (!(age < 0.0D) && !(age > (double)l.msg.life)) {
                  double dx = mc.player.getX() - l.msg.get(0);
                  double dy = mc.player.getY() - l.msg.get(1);
                  double dz = mc.player.getZ() - l.msg.get(2);
                  if (!(dx * dx + dy * dy + dz * dz > 576.0D)) {
                     float k = (float)(smooth(age / 3.0D) * (1.0D - smooth((age - 18.0D) / 10.0D)));
                     best = Math.max(best, k);
                  }
               }
            }
         }

         return best * 0.24F;
      } else {
         return 0.0F;
      }
   }

   @SubscribeEvent
   public static void onStage(RenderLevelStageEvent event) {
      RenderLevelStageEvent.Stage stage = event.getStage();
      if (stage == Stage.AFTER_SKY) {
         inWorld = true;
      } else if (stage != Stage.AFTER_ENTITIES && stage != Stage.AFTER_LEVEL) {
         if (stage == Stage.AFTER_TRANSLUCENT_BLOCKS) {
            if (!LOOKS.isEmpty() || !COUNTERS.isEmpty()) {
               Minecraft mc = Minecraft.getInstance();
               ClientLevel level = mc.level;
               if (level != null) {
                  Vec3 cam = event.getCamera().getPosition();
                  float pt = event.getPartialTick();
                  double now = (double)level.getGameTime() + (double)pt;
                  MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
                  RenderType type = RenderType.entityTranslucentEmissive(WHITE);
                  Matrix4f m = event.getPoseStack().last().pose();
                  float fogStart = RenderSystem.getShaderFogStart();
                  RenderSystem.setShaderFogStart(1000000.0F);

                  try {
                     VertexConsumer vc = buffers.getBuffer(type);
                     boolean first = mc.options.getCameraType() == CameraType.FIRST_PERSON;

                     for(ClientLbLooks.Look l : LOOKS) {
                        double age = now - (double)l.born;
                        if (!(age < 0.0D) && !(age > (double)l.msg.life)) {
                           Entity caster = level.getEntity(l.msg.entity);
                           if (caster == null || !caster.isInvisible()) {
                              if (l.msg.kind != 12) {
                                 if (l.msg.kind == 11) {
                                    stillRing(vc, m, cam, l.msg, age);
                                 }
                              } else {
                                 nova(vc, m, cam, l.msg, age, !first || caster == null || caster != mc.player);
                              }
                           }
                        }
                     }

                     for(Map.Entry<Integer, Long> en : COUNTERS.entrySet()) {
                        Entity e = level.getEntity(en.getKey());
                        if (e != null && !e.isInvisible() && (!first || e != mc.player)) {
                           counterLight(vc, m, cam, e, now, pt);
                        }
                     }

                     buffers.endBatch(type);
                  } catch (Throwable var22) {
                     LOOKS.clear();
                     COUNTERS.clear();
                  } finally {
                     RenderSystem.setShaderFogStart(fogStart);
                  }

               }
            }
         }
      } else {
         inWorld = false;
      }
   }

   @SubscribeEvent
   public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
      if (inWorld && !COPIES.isEmpty()) {
         LivingEntity e = event.getEntity();
         List<double[]> list = (List)COPIES.get(e.getId());
         if (list != null && !list.isEmpty() && !e.isInvisible() && GravityFrame.plain(GravityGuard.direction(e))) {
            try {
               float pt = event.getPartialTick();
               double now = (double)e.level().getGameTime() + (double)pt;
               double ex = Mth.lerp((double)pt, e.xo, e.getX());
               double ey = Mth.lerp((double)pt, e.yo, e.getY());
               double ez = Mth.lerp((double)pt, e.zo, e.getZ());
               double pk = Math.max(0.05D, (double)e.getBbWidth() / 0.6D);
               EntityModel<?> model = event.getRenderer().getModel();
               PoseStack ps = event.getPoseStack();
               MultiBufferSource buffers = event.getMultiBufferSource();

               for(double[] c : list) {
                  double age = now - c[5];
                  if (!(age < 0.0D) && !(age > c[6])) {
                     float a = (float)((double)0.55F * (1.0D - age / Math.max(1.0D, c[6])));
                     if (!(a < 0.02F)) {
                        float[] col = rgb((int)c[4]);
                        float grow = (float)(0.9375D * (1.0D + 0.05D * age / Math.max(1.0D, c[6])));
                        ps.pushPose();

                        try {
                           ps.translate((c[0] - ex) / pk, (c[1] - ey) / pk, (c[2] - ez) / pk);
                           ps.mulPose(Axis.YP.rotationDegrees(180.0F - (float)c[3]));
                           ps.scale(-grow, -grow, grow);
                           ps.translate(0.0D, -1.501D, 0.0D);
                           model.renderToBuffer(ps, buffers.getBuffer(RenderType.entityTranslucentEmissive(WHITE, false)), 15728880, OverlayTexture.NO_OVERLAY, col[0], col[1], col[2], a);
                        } finally {
                           ps.popPose();
                        }
                     }
                  }
               }
            } catch (Throwable var28) {
               COPIES.clear();
            }

         }
      }
   }

   private static void nova(VertexConsumer vc, Matrix4f m, Vec3 cam, OriginLookPacket msg, double age, boolean core) {
      Vec3 base = new Vec3(msg.get(0), msg.get(1), msg.get(2));
      if (!(base.distanceToSqr(cam) > 9216.0D)) {
         Vec3 palms = new Vec3(msg.get(3), msg.get(4), msg.get(5));
         float[] col = rgb((int)msg.get(6));
         double reach = Mth.clamp(msg.get(7), 1.0D, 8.0D);
         if (core && age < 7.0D && palms.distanceTo(cam) > 0.6D) {
            double grow = Math.sin(Math.min(1.0D, age / 4.0D) * Math.PI * 0.5D);
            double r_0 = 0.12D + 0.38D * grow;
            float a_0 = (float)(age < 4.0D ? 1.0D : 1.0D - (age - 4.0D) / 3.0D);
            Vec3[] f = ClientSgLooks.facing(cam, palms);
            ClientSgLooks.disc(vc, m, cam, palms, f[0], f[1], 0.0D, r_0 * 1.8D, col, 0.45F * a_0, 0.0F, 24);
            ClientSgLooks.disc(vc, m, cam, palms, f[0], f[1], 0.0D, r_0, PURE, 0.95F * a_0, 0.5F * a_0, 20);
         }

         double gy = msg.get(8);
         float floor = Double.isNaN(gy) ? 0.0F : (float)Mth.clamp(1.0D - (base.y - gy) / 3.0D, 0.0D, 1.0D);
         Vec3 ground = new Vec3(base.x, Double.isNaN(gy) ? base.y : gy, base.z);
         double ds = (age - 1.0D) / 11.0D;
         if (ds >= 0.0D && ds <= 1.0D) {
            double r = reach * (1.0D - (1.0D - ds) * (1.0D - ds));
            float a = (float)(0.7D * (1.0D - ds * ds));
            dome(vc, m, cam, base, r, col, a);
            if (floor > 0.0F) {
               ClientVertigo.flatRing(vc, m, cam, ground.x, ground.y + 0.05D, ground.z, Math.max(0.0D, r - 0.22D), r, 0.0D, PURE, 0.0F, 0.8F * a * floor, 48);
            }
         }

         if (!(floor <= 0.0F)) {
            double ps = Mth.clamp((age - 1.0D) / 11.0D, 0.0D, 1.0D);
            double outer = Math.min(reach * 0.82D, 0.6D + (reach - 0.6D) * (1.0D - (1.0D - ps) * (1.0D - ps)));
            float pa = (float)(0.4D * (1.0D - smooth((age - 14.0D) / 46.0D))) * floor;
            if (pa > 0.02F && outer > 0.7D) {
               ClientVertigo.flatRing(vc, m, cam, ground.x, ground.y + 0.03D, ground.z, 0.45D, outer, 0.0D, DUST, pa * 0.6F, pa, 48);

               for(int i = 0; i < 28; ++i) {
                  double th = (Math.PI * 2D) * (double)i / 28.0D + ClientVertigo.hash(i * 17 + 5) * 0.2D;
                  double r0 = 0.6D + ClientVertigo.hash(i * 31 + 3) * 0.4D;
                  double r1 = Math.min(outer, r0 + 0.8D + ClientVertigo.hash(i * 7 + 11) * 1.6D);
                  if (!(r1 <= r0 + 0.05D)) {
                     ClientVertigo.flatLine(vc, m, cam, ground.x + Math.cos(th) * r0, ground.y + 0.035D, ground.z + Math.sin(th) * r0, ground.x + Math.cos(th) * r1, ground.y + 0.035D, ground.z + Math.sin(th) * r1, 0.05D, DUST, pa * 0.9F);
                  }
               }
            }

            if (age >= 9.0D) {
               double cooled = Mth.clamp((age - 9.0D) / 60.0D, 0.0D, 1.0D);
               float[] c = mixRgb(EMBER, CHAR, (float)cooled);
               float sa = (float)(0.8D * smooth((age - 9.0D) / 3.0D) * (1.0D - smooth((age - 80.0D) / 30.0D))) * floor;
               if (sa > 0.02F) {
                  ClientVertigo.flatRing(vc, m, cam, ground.x, ground.y + 0.04D, ground.z, reach * 0.84D, reach * 0.95D, 0.0D, c, sa * 0.7F, sa, 64);
                  ClientVertigo.flatRing(vc, m, cam, ground.x, ground.y + 0.04D, ground.z, reach * 0.95D, reach * 1.02D, 0.0D, c, sa, 0.0F, 64);
               }
            }

         }
      }
   }

   private static void dome(VertexConsumer vc, Matrix4f m, Vec3 cam, Vec3 c, double r, float[] col, float a) {
      if (!(r < 0.1D) && !(a < 0.02F)) {
         for(int j = 0; j < 8; ++j) {
            double l0 = (Math.PI / 2D) * (double)j / 8.0D;
            double l1 = (Math.PI / 2D) * (double)(j + 1) / 8.0D;

            for(int i = 0; i < 24; ++i) {
               double t0 = (Math.PI * 2D) * (double)i / 24.0D;
               double t1 = (Math.PI * 2D) * (double)(i + 1) / 24.0D;
               Vec3 p0 = sphere(c, r, l0, t0);
               Vec3 p1 = sphere(c, r, l0, t1);
               Vec3 p2 = sphere(c, r, l1, t1);
               Vec3 p3 = sphere(c, r, l1, t0);
               ClientVertigo.quad(vc, m, col, p0.x - cam.x, p0.y - cam.y, p0.z - cam.z, p1.x - cam.x, p1.y - cam.y, p1.z - cam.z, p2.x - cam.x, p2.y - cam.y, p2.z - cam.z, p3.x - cam.x, p3.y - cam.y, p3.z - cam.z, rim(p0, c, cam, a), rim(p1, c, cam, a), rim(p2, c, cam, a), rim(p3, c, cam, a));
            }
         }

      }
   }

   private static Vec3 sphere(Vec3 c, double r, double lat, double lon) {
      double h = Math.cos(lat) * r;
      return new Vec3(c.x + Math.cos(lon) * h, c.y + Math.sin(lat) * r, c.z + Math.sin(lon) * h);
   }

   private static float rim(Vec3 p, Vec3 c, Vec3 cam, float a) {
      Vec3 n = p.subtract(c);
      Vec3 v = cam.subtract(p);
      double nl = n.length();
      double vl = v.length();
      if (!(nl < 1.0E-6D) && !(vl < 1.0E-6D)) {
         double d = Math.abs(n.dot(v) / (nl * vl));
         double f = 1.0D - d;
         return (float)((double)a * (0.12D + 0.88D * f * f));
      } else {
         return a;
      }
   }

   private static void stillRing(VertexConsumer vc, Matrix4f m, Vec3 cam, OriginLookPacket msg, double age) {
      Vec3 base = new Vec3(msg.get(0), msg.get(1), msg.get(2));
      if (!(base.distanceToSqr(cam) > 9216.0D)) {
         double s = Mth.clamp((age - 2.0D) / 16.0D, 0.0D, 1.0D);
         double r = 0.5D + 6.5D * (1.0D - (1.0D - s) * (1.0D - s));
         float a = (float)(0.85D * smooth(age / 2.0D) * (1.0D - smooth((age - 18.0D) / 10.0D)));
         if (!(a < 0.02F)) {
            ClientVertigo.flatRing(vc, m, cam, base.x, base.y + 0.06D, base.z, Math.max(0.0D, r - 0.3D), r, 0.0D, SILVER, 0.0F, 0.55F * a, 64);
            ClientVertigo.flatRing(vc, m, cam, base.x, base.y + 0.065D, base.z, r, r + 0.06D, 0.0D, PURE, 0.9F * a, 0.0F, 64);
         }
      }
   }

   private static void counterLight(VertexConsumer vc, Matrix4f m, Vec3 cam, Entity e, double now, float pt) {
      double h = (double)e.getBbHeight();
      Vec3 c = new Vec3(Mth.lerp((double)pt, e.xo, e.getX()), Mth.lerp((double)pt, e.yo, e.getY()) + h * 0.62D, Mth.lerp((double)pt, e.zo, e.getZ()));
      if (!(c.distanceToSqr(cam) > 9216.0D)) {
         double r = Math.max(0.55D, (double)e.getBbWidth() * 1.25D);

         for(int k = 0; k < 4; ++k) {
            double ang = now * 0.2D - (double)k * 0.16D + (double)e.getId() * 0.7D;
            Vec3 p = c.add(Math.cos(ang) * r, Math.sin(now * 0.11D - (double)k * 0.1D) * 0.12D * h / 1.8D, Math.sin(ang) * r);
            if (!(p.distanceTo(cam) < 0.4D)) {
               Vec3[] f = ClientSgLooks.facing(cam, p);
               float a = 1.0F - (float)k * 0.24F;
               double size = 0.075D * (1.0D - (double)k * 0.18D);
               if (k == 0) {
                  ClientSgLooks.disc(vc, m, cam, p, f[0], f[1], 0.0D, size * 2.6D, SILVER, 0.35F, 0.0F, 14);
               }

               ClientSgLooks.disc(vc, m, cam, p, f[0], f[1], 0.0D, size, PURE, 0.95F * a, 0.6F * a, 12);
            }
         }

      }
   }

   static float[] rgb(int c) {
      return new float[]{(float)(c >> 16 & 255) / 255.0F, (float)(c >> 8 & 255) / 255.0F, (float)(c & 255) / 255.0F};
   }

   private static float[] mixRgb(float[] a, float[] b, float t) {
      return new float[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
   }

   static double smooth(double t) {
      double k = Mth.clamp(t, 0.0D, 1.0D);
      return k * k * (3.0D - 2.0D * k);
   }

   public static void clearAll() {
      ASCENDS.clear();
      COUNTERS.clear();
      COPIES.clear();
      LOOKS.clear();
   }

   private static final class Look {
      final OriginLookPacket msg;
      final long born;

      Look(OriginLookPacket msg, long born) {
         this.msg = msg;
         this.born = born;
      }
   }
}
