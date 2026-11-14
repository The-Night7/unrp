package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class LatticeFx {
   public static final int GHOST = 16;
   private static final int MOST_FRAME = 4096;
   private static final int MOST_GHOSTS = 768;
   private static final double SEEN = 64.0D;
   private static final int[][] PLUS = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
   private static final int[][] STEPS = new int[][]{{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
   private static final List<LatticeBlockEntity> FRAME = new ArrayList();
   private static final ArrayDeque<LatticeFx.Ghost> GHOSTS = new ArrayDeque();
   private static final Map<Integer, LatticeFx.Cast> CASTS = new HashMap();
   private static final WeaveQuads Q = new WeaveQuads();
   private static ClientLevel where;

   private LatticeFx() {
   }

   static void seen(LatticeBlockEntity be) {
      if (FRAME.size() < 4096) {
         FRAME.add(be);
      }

   }

   private static void rebase(Minecraft mc) {
      if (mc.level != where) {
         where = mc.level;
         FRAME.clear();
         GHOSTS.clear();
         CASTS.clear();
      }

   }

   public static void dissolve(BlockPos pos, BlockState st, UUID owner, int kind) {
      Minecraft mc = Minecraft.getInstance();
      rebase(mc);
      if (mc.level != null && mc.player != null && st.is((Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get())) {
         if (!(pos.distToCenterSqr(mc.player.getX(), mc.player.getY(), mc.player.getZ()) > 4096.0D)) {
            int color = owner == null ? AuraColorResolver.forViewer(15160448) : AuraColorResolver.forOwner(owner, 15160448);
            LatticeFx.Ghost gh = new LatticeFx.Ghost();
            gh.x = pos.getX();
            gh.y = pos.getY();
            gh.z = pos.getZ();
            gh.o = LatticeRenderer.orientIndex((ParticleBlock.Orientation)st.getValue(ParticleBlock.ORIENTATION));
            gh.layer = st.getValue(ParticleBlock.LAYER);
            gh.kind = kind;
            gh.r = (float)(color >> 16 & 255) / 255.0F;
            gh.g = (float)(color >> 8 & 255) / 255.0F;
            gh.b = (float)(color & 255) / 255.0F;
            gh.start = mc.level.getGameTime();
            gh.owner = owner;
            gh.specks = LatticeRenderer.table(pos);
            GHOSTS.add(gh);

            while(GHOSTS.size() > 768) {
               GHOSTS.poll();
            }

         }
      }
   }

   public static void accept(WeaveFxPacket msg) {
      Minecraft mc = Minecraft.getInstance();
      rebase(mc);
      if (mc.level != null) {
         if (msg.flags == 0) {
            CASTS.remove(msg.entity);
         } else {
            LatticeFx.Cast c = (LatticeFx.Cast)CASTS.computeIfAbsent(msg.entity, (k) -> new LatticeFx.Cast());
            c.flags = msg.flags;
            c.reach = msg.reach;
            c.orient = msg.orient;
            c.plane = msg.plane;
            c.swarm = msg.swarm;
            c.seen = mc.level.getGameTime();
         }
      }
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         rebase(mc);
         if (mc.level != null) {
            long now = mc.level.getGameTime();

            while(!GHOSTS.isEmpty() && (now - ((LatticeFx.Ghost)GHOSTS.peek()).start > 17L || now < ((LatticeFx.Ghost)GHOSTS.peek()).start - 200L)) {
               GHOSTS.poll();
            }

            CASTS.values().removeIf((c) -> now - c.seen > 60L || now < c.seen - 200L);
            if (FRAME.size() >= 4096) {
               FRAME.clear();
            }

         }
      }
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         Minecraft mc = Minecraft.getInstance();
         ClientLevel level = mc.level;
         if (level != null && level == where) {
            boolean localWeave = WeaveInputHandler.weaveHeld();
            if (!FRAME.isEmpty() || !GHOSTS.isEmpty() || !CASTS.isEmpty() || localWeave) {
               Camera camera = event.getCamera();
               Vec3 cam = camera.getPosition();
               Vector3f left = camera.getLeftVector();
               Vector3f up = camera.getUpVector();
               float pt = event.getPartialTick();
               long now = level.getGameTime();
               MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
               Matrix4f m = event.getPoseStack().last().pose();

               try {
                  for(int pass = 1; pass <= 2; ++pass) {
                     RenderType type = pass == 1 ? LatticeRenderer.soft() : LatticeRenderer.bloom();
                     VertexConsumer vc = buffers.getBuffer(type);
                     Q.begin(vc, m, pass, cam.x, cam.y, cam.z, -left.x(), -left.y(), -left.z(), up.x(), up.y(), up.z());

                     for(LatticeBlockEntity be : FRAME) {
                        if (!be.isRemoved()) {
                           LatticeRenderer.drawPane(Q, be, now, pt);
                        }
                     }

                     for(LatticeFx.Ghost gh : GHOSTS) {
                        drawGhost(Q, gh, level, now, pt);
                     }

                     drawCasts(Q, level, mc, camera, now, pt, localWeave);
                     buffers.endBatch(type);
                  }
               } catch (Throwable var21) {
                  GHOSTS.clear();
                  CASTS.clear();
               } finally {
                  FRAME.clear();
               }

            }
         } else {
            FRAME.clear();
         }
      }
   }

   private static void drawGhost(WeaveQuads q, LatticeFx.Ghost gh, ClientLevel level, long now, float pt) {
      float age = (float)(now - gh.start) + pt;
      if (!(age < 0.0F) && !(age > 16.0F)) {
         float g = age / 16.0F;
         float top = (float)(gh.layer + 1) * 0.25F;
         int o = gh.o;
         float nx = o == 2 ? 1.0F : 0.0F;
         float ny = o == 0 ? 1.0F : 0.0F;
         float nz = o == 1 ? 1.0F : 0.0F;
         double[] A = new double[3];
         double[] B = new double[3];
         double cx = (double)gh.x + 0.5D;
         double cy = (double)((float)gh.y + (o == 0 ? top - 0.05F : 0.5F));
         double cz = (double)gh.z + 0.5D;
         Vec3 home = null;
         if (gh.kind == 1 && gh.owner != null) {
            Player owner = level.getPlayerByUUID(gh.owner);
            if (owner != null) {
               home = owner.getPosition(pt).add(0.0D, (double)owner.getBbHeight() * 0.6D, 0.0D);
            }
         }

         float shrink = 1.0F - LatticeRenderer.smooth(g / 0.4F);
         if (shrink > 0.0F) {
            float[][] mesh = new float[][]{{0.5F, 0.0F, 0.0F, 0.5F}, {1.0F, 0.0F, 0.0F, 1.0F}, {1.0F, 0.5F, 0.5F, 1.0F}, {0.0F, 0.5F, 0.5F, 1.0F}, {0.0F, 0.0F, 1.0F, 1.0F}, {0.5F, 0.0F, 1.0F, 0.5F}};

            for(float[] s : mesh) {
               float mu = (s[0] + s[2]) * 0.5F;
               float mv = (s[1] + s[3]) * 0.5F;
               float hu = (s[2] - s[0]) * 0.5F * shrink;
               float hv = (s[3] - s[1]) * 0.5F * shrink;
               rest(A, o, gh.x, gh.y, gh.z, top, mu - hu, mv - hv, 0.01F);
               rest(B, o, gh.x, gh.y, gh.z, top, mu + hu, mv + hv, 0.01F);
               q.flat(A[0], A[1], A[2], B[0], B[1], B[2], nx, ny, nz, 0.034F, gh.r, gh.g, gh.b, 0.25F * shrink, 0.25F * shrink, 4);
            }
         }

         if (gh.kind == 2 && age < 5.0F) {
            float[] hot = LatticeRenderer.toward(gh.r, gh.g, gh.b, 0.6F);
            q.bill(cx, cy, cz, 0.5F + age * 0.12F, hot[0], hot[1], hot[2], 0.6F * (1.0F - age / 5.0F), 3, 2);
         }

         float[] h = gh.specks;
         float[] gl = LatticeRenderer.toward(gh.r, gh.g, gh.b, 0.7F);

         for(int i = 0; i < 26; ++i) {
            int at = i * 10;
            float lw = o == 0 ? -0.09F + h[at + 2] * 0.16F : (h[at + 2] - 0.5F) * 0.16F;
            rest(A, o, gh.x, gh.y, gh.z, top, h[at], h[at + 1], lw);
            float size = 0.05F + h[at + 6] * 0.05F;
            double pz;
            float a;
            double px;
            double py;
            if (home != null) {
               float k = LatticeRenderer.clamp01((g - h[at + 7] * 0.3F) / 0.7F);
               float ek = k * k;
               double mx = (A[0] + home.x) * 0.5D;
               double my = (A[1] + home.y) * 0.5D + 0.6D + (double)h[at + 9] * 0.5D;
               double mz = (A[2] + home.z) * 0.5D;
               double ax = A[0] + (mx - A[0]) * (double)ek;
               double ay = A[1] + (my - A[1]) * (double)ek;
               double az = A[2] + (mz - A[2]) * (double)ek;
               double bx = mx + (home.x - mx) * (double)ek;
               double by = my + (home.y - my) * (double)ek;
               double bz = mz + (home.z - mz) * (double)ek;
               px = ax + (bx - ax) * (double)ek;
               py = ay + (by - ay) * (double)ek;
               pz = az + (bz - az) * (double)ek;
               a = k < 0.85F ? 1.0F : 1.0F - (k - 0.85F) / 0.15F;
               size *= 1.0F - 0.4F * k;
            } else if (gh.kind == 2) {
               double dx = A[0] - cx + (double)(nx * (h[at + 2] - 0.5F));
               double dy = A[1] - cy + 0.15D + (double)(ny * (h[at + 2] - 0.5F));
               double dz = A[2] - cz + (double)(nz * (h[at + 2] - 0.5F));
               double dl = Math.sqrt(dx * dx + dy * dy + dz * dz);
               if (dl < 1.0E-6D) {
                  dl = 1.0D;
               }

               double travel = (0.1D + 0.14D * (double)h[at + 8]) * (double)age * (1.0D - 0.45D * (double)g) / dl;
               px = A[0] + dx * travel;
               py = A[1] + dy * travel;
               pz = A[2] + dz * travel;
               float j = 1.0F - g;
               a = j * j;
               size *= 1.0F + 0.3F * j;
            } else {
               float side = h[at + 8] < 0.5F ? -1.0F : 1.0F;
               px = A[0] + (double)((nx * side * 0.014F + (h[at + 4] - 0.5F) * 0.02F) * age);
               py = A[1] + (double)((0.008F + ny * side * 0.01F) * age);
               pz = A[2] + (double)((nz * side * 0.014F + (h[at + 5] - 0.5F) * 0.02F) * age);
               px += (double)(0.05F * Mth.sin(age * 0.4F + h[at + 3] * 6.28F) * g);
               pz += (double)(0.05F * Mth.cos(age * 0.4F + h[at + 3] * 6.28F) * g);
               a = (float)Math.pow((double)(1.0F - g), 1.3D);
               size *= 1.0F - 0.5F * g;
            }

            if (i % 3 == 0) {
               q.bill(px, py, pz, size, gl[0], gl[1], gl[2], a, 1, 1);
            } else {
               q.bill(px, py, pz, size, gh.r, gh.g, gh.b, a, 0, 1);
            }
         }

      }
   }

   private static void rest(double[] out, int o, int bx, int by, int bz, float top, float u, float v, float w) {
      if (o == 1) {
         out[0] = (double)((float)bx + u);
         out[1] = (double)((float)by + v);
         out[2] = (double)((float)bz + 0.5F + w);
      } else if (o == 2) {
         out[0] = (double)((float)bx + 0.5F + w);
         out[1] = (double)((float)by + v);
         out[2] = (double)((float)bz + u);
      } else {
         out[0] = (double)((float)bx + u);
         out[1] = (double)((float)by + top - 0.05F + w);
         out[2] = (double)((float)bz + v);
      }

   }

   private static Vec3 brushOf(ClientLevel level, AbstractClientPlayer p, int reach, float pt) {
      Vec3 eye = p.getEyePosition(pt);
      Vec3 look = p.getViewVector(pt);
      Vec3 end = eye.add(look.scale((double)reach));
      BlockHitResult ray = level.clip(new ClipContext(eye, end, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, p));
      if (ray.getType() == Type.BLOCK) {
         return level.getBlockState(ray.getBlockPos()).is((Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get()) ? ray.getLocation().add(look.scale(0.02D)) : ray.getLocation().add(look.scale(-0.25D));
      } else {
         return end;
      }
   }

   private static void drawCasts(WeaveQuads q, ClientLevel level, Minecraft mc, Camera camera, long now, float pt, boolean localWeave) {
      AbstractClientPlayer self = mc.player;
      float t = (float)(now % 24000L) + pt;
      boolean selfDrawn = false;

      for(Map.Entry<Integer, LatticeFx.Cast> en : CASTS.entrySet()) {
         Entity e = level.getEntity(en.getKey());
         if (e instanceof AbstractClientPlayer p) {
            if (p.isAlive() && !(p.distanceToSqr(q.cx, q.cy, q.cz) > 4096.0D)) {
               boolean me = p == self;
               if (me) {
                  selfDrawn = true;
               }

               drawCaster(q, level, mc, camera, p, (LatticeFx.Cast)en.getValue(), me, me && localWeave, t, pt);
            }
         }
      }

      if (!selfDrawn && localWeave && self != null) {
         drawCaster(q, level, mc, camera, self, (LatticeFx.Cast)null, true, true, t, pt);
      }

   }

   private static void drawCaster(WeaveQuads q, ClientLevel level, Minecraft mc, Camera camera, AbstractClientPlayer p, LatticeFx.Cast c, boolean me, boolean localWeave, float t, float pt) {
      int color = AuraColorResolver.forOwner(p.getUUID(), 15160448);
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      boolean firstPerson = me && mc.options.getCameraType().isFirstPerson();
      boolean hidden = p.isInvisible() && !me;
      if (c != null && (c.flags & 1) != 0 && !firstPerson && !hidden) {
         orbit(q, p, Math.max(1, c.swarm), t, pt, r, g, b);
      }

      boolean weaving = me ? localWeave : c != null && (c.flags & 6) != 0;
      if (weaving && !hidden) {
         boolean spent = c != null && (c.flags & 4) != 0;
         int reach = me ? WeaveInputHandler.clientReach() : (c == null ? 9 : c.reach);
         Vec3 brush = brushOf(level, p, reach, pt);
         Vec3 look = p.getViewVector(pt);
         boolean known = c != null && (c.flags & 2) != 0 && c.orient >= 0;
         int o = known ? c.orient : WeaveBrush.rawOrient(p.getXRot(), look.x, look.z, p.isShiftKeyDown());
         int plane;
         if (known) {
            plane = c.plane;
         } else if (o == 0) {
            plane = WeaveBrush.heightOf(brush.y);
         } else {
            plane = Mth.floor(o == 1 ? brush.z : brush.x);
         }

         reticle(q, brush, o, plane, t, r, g, b, spent);
         Vec3 hand;
         if (firstPerson) {
            Vector3f fwd = camera.getLookVector();
            Vector3f left = camera.getLeftVector();
            Vector3f up = camera.getUpVector();
            hand = new Vec3(q.cx + (double)fwd.x() * 0.6D - (double)left.x() * 0.32D - (double)up.x() * 0.26D, q.cy + (double)fwd.y() * 0.6D - (double)left.y() * 0.32D - (double)up.y() * 0.26D, q.cz + (double)fwd.z() * 0.6D - (double)left.z() * 0.32D - (double)up.z() * 0.26D);
         } else {
            float yaw = Mth.rotLerp(pt, p.yBodyRotO, p.yBodyRot) * ((float)Math.PI / 180F);
            Vec3 base = p.getPosition(pt);
            double fx = (double)(-Mth.sin(yaw));
            double fz = (double)Mth.cos(yaw);
            double rx = (double)(-Mth.cos(yaw));
            double rz = (double)(-Mth.sin(yaw));
            double lift = (double)p.getBbHeight() * 0.69D;
            hand = new Vec3(base.x + rx * 0.36D + fx * 0.45D, base.y + lift, base.z + rz * 0.36D + fz * 0.45D);
         }

         stream(q, hand, brush, t, r, g, b, spent, (float)(p.getId() % 7) * 0.13F);
      }
   }

   private static void orbit(WeaveQuads q, AbstractClientPlayer p, int count, float t, float pt, float r, float g, float b) {
      Vec3 base = p.getPosition(pt);
      double cx = base.x;
      double cy = base.y + (double)p.getBbHeight() * 0.58D;
      double cz = base.z;
      float[] hot = LatticeRenderer.toward(r, g, b, 0.6F);
      long seed = p.getUUID().getLeastSignificantBits();

      for(int i = 0; i < count; ++i) {
         float h1 = LatticeRenderer.hash(seed + (long)i * 131L, 1);
         float h2 = LatticeRenderer.hash(seed + (long)i * 131L, 2);
         float h3 = LatticeRenderer.hash(seed + (long)i * 131L, 3);
         float rad = 0.9F + 0.3F * h1;
         float tilt = (h2 - 0.5F) * 1.2F;
         float node = (float)i * ((float)Math.PI * 2F) / (float)count + t * 0.004F;
         float w = 0.06F + 0.035F * h3;
         float ax = Mth.cos(node);
         float az = Mth.sin(node);
         float bx = -Mth.sin(node) * Mth.cos(tilt);
         float by = Mth.sin(tilt);
         float bz = Mth.cos(node) * Mth.cos(tilt);

         for(int k = 0; k < 4; ++k) {
            float th = t * w + h1 * 6.28F - (float)k * 0.16F;
            float c = Mth.cos(th);
            float s = Mth.sin(th);
            double px = cx + (double)(rad * (ax * c + bx * s));
            double py = cy + (double)(rad * by * s);
            double pz = cz + (double)(rad * (az * c + bz * s));
            if (k == 0) {
               if (i % 2 == 0) {
                  q.bill(px, py, pz, 0.07F, hot[0], hot[1], hot[2], 0.95F, 1, 1);
               } else {
                  q.bill(px, py, pz, 0.07F, r, g, b, 0.95F, 0, 1);
               }

               q.bill(px, py, pz, 0.15F, r, g, b, 0.22F, 3, 2);
            } else {
               q.bill(px, py, pz, 0.05F * (1.0F - (float)k * 0.18F), r, g, b, 0.5F * (1.0F - (float)k / 4.0F), 0, 1);
            }
         }
      }

   }

   private static void reticle(WeaveQuads q, Vec3 at, int o, int plane, float t, float r, float g, float b, boolean spent) {
      float ux;
      float uy;
      float uz;
      float vx;
      float vy;
      float vz;
      if (o == 1) {
         ux = 1.0F;
         uy = 0.0F;
         uz = 0.0F;
         vx = 0.0F;
         vy = 1.0F;
         vz = 0.0F;
      } else if (o == 2) {
         ux = 0.0F;
         uy = 0.0F;
         uz = 1.0F;
         vx = 0.0F;
         vy = 1.0F;
         vz = 0.0F;
      } else {
         ux = 1.0F;
         uy = 0.0F;
         uz = 0.0F;
         vx = 0.0F;
         vy = 0.0F;
         vz = 1.0F;
      }

      float[] pale = LatticeRenderer.toward(r, g, b, 0.55F);
      float rr = spent ? 0.3F + 0.05F * Mth.sin(t * 2.3F) : 0.46F + 0.03F * Mth.sin(t * 0.3F);

      for(int i = 0; i < 8; ++i) {
         float ang_0 = t * 0.15F + (float)i * ((float)Math.PI * 2F) / 8.0F;
         float c_0 = Mth.cos(ang_0) * rr;
         float s_0 = Mth.sin(ang_0) * rr;
         float a = spent ? 0.35F * (0.5F + 0.5F * Mth.sin(t * 1.7F + (float)i * 2.1F)) : 0.9F;
         double px = at.x + (double)(ux * c_0) + (double)(vx * s_0);
         double py = at.y + (double)(uy * c_0) + (double)(vy * s_0);
         double pz = at.z + (double)(uz * c_0) + (double)(vz * s_0);
         if (i % 2 == 0) {
            q.bill(px, py, pz, 0.085F, r, g, b, a, 0, 1);
         } else {
            q.bill(px, py, pz, 0.085F, pale[0], pale[1], pale[2], a, 0, 1);
         }

         if (!spent) {
            float ang2 = ang_0 - 0.22F;
            float c2 = Mth.cos(ang2) * rr;
            float s2 = Mth.sin(ang2) * rr;
            q.bill(at.x + (double)(ux * c2) + (double)(vx * s2), at.y + (double)(uy * c2) + (double)(vy * s2), at.z + (double)(uz * c2) + (double)(vz * s2), 0.045F, r, g, b, 0.35F, 0, 1);
         }
      }

      if (!spent) {
         float[] hot = LatticeRenderer.toward(r, g, b, 0.8F);

         for(int j = 0; j < 4; ++j) {
            float ang = -t * 0.22F + (float)j * ((float)Math.PI / 2F);
            float c = Mth.cos(ang) * 0.2F;
            float s = Mth.sin(ang) * 0.2F;
            q.bill(at.x + (double)(ux * c) + (double)(vx * s), at.y + (double)(uy * c) + (double)(vy * s), at.z + (double)(uz * c) + (double)(vz * s), 0.05F, hot[0], hot[1], hot[2], 0.8F, 1, 1);
         }

         q.bill(at.x, at.y, at.z, 0.36F + 0.05F * Mth.sin(t * 0.5F), r, g, b, 0.55F, 3, 2);
         q.bill(at.x, at.y, at.z, 0.07F, hot[0], hot[1], hot[2], 0.95F, 6, 1);
         float glow = 0.16F + 0.08F * Mth.sin(t * 0.35F);
         int ca;
         int cb;
         double lift;
         if (o == 0) {
            ca = Mth.floor(at.x);
            cb = Mth.floor(at.z);
            lift = (double)plane * 0.25D + 0.02D;
         } else if (o == 1) {
            ca = Mth.floor(at.x);
            cb = Mth.floor(at.y);
            lift = (double)plane + 0.5D;
         } else {
            ca = Mth.floor(at.z);
            cb = Mth.floor(at.y);
            lift = (double)plane + 0.5D;
         }

         float nx = o == 2 ? 1.0F : 0.0F;
         float ny = o == 0 ? 1.0F : 0.0F;
         float nz = o == 1 ? 1.0F : 0.0F;
         double[] A = new double[3];
         double[] B = new double[3];

         for(int[] cell : PLUS) {
            for(int e = 0; e < 4; ++e) {
               int na = cell[0] + STEPS[e][0];
               int nb = cell[1] + STEPS[e][1];
               if (Math.abs(na) + Math.abs(nb) > 1) {
                  float a0;
                  float b0;
                  float a1;
                  float b1;
                  if (e == 0) {
                     a0 = 0.0F;
                     b0 = 0.0F;
                     a1 = 1.0F;
                     b1 = 0.0F;
                  } else if (e == 1) {
                     a0 = 0.0F;
                     b0 = 1.0F;
                     a1 = 1.0F;
                     b1 = 1.0F;
                  } else if (e == 2) {
                     a0 = 0.0F;
                     b0 = 0.0F;
                     a1 = 0.0F;
                     b1 = 1.0F;
                  } else {
                     a0 = 1.0F;
                     b0 = 0.0F;
                     a1 = 1.0F;
                     b1 = 1.0F;
                  }

                  stampPoint(A, o, (double)((float)(ca + cell[0]) + a0), (double)((float)(cb + cell[1]) + b0), lift);
                  stampPoint(B, o, (double)((float)(ca + cell[0]) + a1), (double)((float)(cb + cell[1]) + b1), lift);
                  q.flat(A[0], A[1], A[2], B[0], B[1], B[2], nx, ny, nz, 0.03F, r, g, b, glow, glow, 4);
               }
            }
         }

      }
   }

   private static void stampPoint(double[] out, int o, double a, double b, double lift) {
      if (o == 0) {
         out[0] = a;
         out[1] = lift;
         out[2] = b;
      } else if (o == 1) {
         out[0] = a;
         out[1] = b;
         out[2] = lift;
      } else {
         out[0] = lift;
         out[1] = b;
         out[2] = a;
      }

   }

   private static void stream(WeaveQuads q, Vec3 from, Vec3 to, float t, float r, float g, float b, boolean spent, float seed) {
      double dx = to.x - from.x;
      double dy = to.y - from.y;
      double dz = to.z - from.z;
      double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (!(len < 0.3D)) {
         double hl = Math.sqrt(dx * dx + dz * dz);
         double sx = hl < 1.0E-6D ? 1.0D : dz / hl;
         double sz = hl < 1.0E-6D ? 0.0D : -dx / hl;
         double kx = from.x + dx * 0.5D;
         double ky = from.y + dy * 0.5D + 0.22D + 0.05D * len;
         double kz = from.z + dz * 0.5D;
         double px = from.x;
         double py = from.y;
         double pz = from.z;

         for(int s = 1; s <= 14; ++s) {
            float k = (float)s / 14.0F;
            float u_0 = 1.0F - k;
            double nx = (double)(u_0 * u_0) * from.x + (double)(2.0F * u_0 * k) * kx + (double)(k * k) * to.x;
            double ny = (double)(u_0 * u_0) * from.y + (double)(2.0F * u_0 * k) * ky + (double)(k * k) * to.y;
            double nz = (double)(u_0 * u_0) * from.z + (double)(2.0F * u_0 * k) * kz + (double)(k * k) * to.z;
            float aa = spent ? 0.05F : 0.16F * LatticeRenderer.smooth((float)(s - 1) / 3.0F);
            q.ribbon(px, py, pz, nx, ny, nz, 0.03F, r, g, b, aa, aa, 4);
            px = nx;
            py = ny;
            pz = nz;
         }

         int n = 12 + Math.min(12, (int)Math.round(len * 1.4D));
         float[] hot = LatticeRenderer.toward(r, g, b, 0.55F);

         for(int i = 0; i < n; ++i) {
            if (!spent || (i & 1) != 1) {
               float ph = t * 0.055F + (float)i / (float)n + seed;
               ph -= (float)Mth.floor(ph);
               float u = 1.0F - ph;
               double bx = (double)(u * u) * from.x + (double)(2.0F * u * ph) * kx + (double)(ph * ph) * to.x;
               double by = (double)(u * u) * from.y + (double)(2.0F * u * ph) * ky + (double)(ph * ph) * to.y;
               double bz = (double)(u * u) * from.z + (double)(2.0F * u * ph) * kz + (double)(ph * ph) * to.z;
               float env = Mth.sin((float)Math.PI * ph);
               float strand = (i & 1) == 0 ? 0.0F : (float)Math.PI;
               float helix = ph * ((float)Math.PI * 2F) * 2.0F + t * 0.2F + strand;
               float wob = 0.07F * env;
               bx += sx * (double)Mth.cos(helix) * (double)wob;
               by += (double)(Mth.sin(helix) * wob);
               bz += sz * (double)Mth.cos(helix) * (double)wob;
               float a = LatticeRenderer.smooth(ph / 0.12F);
               if (spent) {
                  a *= 0.35F + 0.35F * Mth.sin(t * 2.1F + (float)i);
               }

               float size = 0.055F + 0.035F * env;
               if (i % 4 == 0) {
                  q.bill(bx, by, bz, size, hot[0], hot[1], hot[2], a, 6, 1);
               } else {
                  q.bill(bx, by, bz, size, r, g, b, a, 6, 1);
               }

               if (!spent) {
                  q.bill(bx, by, bz, 0.16F, r, g, b, 0.28F * a, 3, 2);
               }
            }
         }

      }
   }

   static final class Cast {
      int flags;
      int reach;
      int orient;
      int plane;
      int swarm;
      long seen;
   }

   static final class Ghost {
      int x;
      int y;
      int z;
      int o;
      int layer;
      int kind;
      float r;
      float g;
      float b;
      long start;
      UUID owner;
      float[] specks;
   }
}
