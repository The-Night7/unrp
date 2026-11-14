package com.myriam.unrp.look;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
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
public final class PowerEyes {
   public static final int RIGHT = 0;
   public static final int LEFT = 1;
   private static final int LIGHT = 15728880;
   private static final Map<Integer, PowerEyes.State> STATES = new HashMap();
   private static ClientLevel where;

   private PowerEyes() {
   }

   static String key(AbstractClientPlayer p) {
      if (CustomEyesLayer.colorOf(p) < 0) {
         return "";
      } else {
         String power = ClientEyeCache.get(p.getUUID());
         return power.isEmpty() ? "lit" : power;
      }
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         ClientLevel level = Minecraft.getInstance().level;
         if (level == null) {
            STATES.clear();
            where = null;
         } else {
            if (level != where) {
               STATES.clear();
               where = level;
            }

            double now = (double)level.getGameTime();
            Set<Integer> seen = new HashSet();

            for(AbstractClientPlayer p : level.players()) {
               seen.add(p.getId());
               String k = key(p);
               PowerEyes.State s = (PowerEyes.State)STATES.get(p.getId());
               if (s == null) {
                  s = new PowerEyes.State();
                  s.key = k;
                  STATES.put(p.getId(), s);
               } else if (!k.equals(s.key)) {
                  if (!k.isEmpty()) {
                     s.onAt = now;
                  }

                  s.key = k;
               }

               acted(s, com.myriam.unrp.anim.MoveAnimClient.played(p.getId()));
            }

            STATES.keySet().retainAll(seen);
         }
      }
   }

   static void acted(PowerEyes.State s, double at) {
      if (at > s.actAt) {
         s.actAt = at;
      }

   }

   public static void passFlare(int entity, double flare, double t) {
      PowerEyes.State s = (PowerEyes.State)STATES.get(entity);
      if (s != null) {
         s.pass = flare;
         s.passAt = t;
      }

   }

   static double[] look(AbstractClientPlayer p, double t) {
      PowerEyes.State s = (PowerEyes.State)STATES.get(p.getId());
      double pulse = com.myriam.unrp.pass.EyeGlowMath.pulse(t, p.getId());
      if (s == null) {
         return new double[]{1.0D, 0.0D, pulse, 0.0D};
      } else {
         double on = t - s.onAt;
         return new double[]{com.myriam.unrp.pass.EyeGlowMath.lit(on), com.myriam.unrp.pass.EyeGlowMath.flare(on, t - s.actAt, s.pass, t - s.passAt), pulse, com.myriam.unrp.pass.EyeGlowMath.burst(on)};
      }
   }

   static void draw(VertexConsumer vc, Matrix4f m, AbstractClientPlayer p, double[][] eyes, int style, int w, int h, boolean trail, int rgb, double[] lk, double t, float pt) {
      Vector3f unit = m.transformDirection(new Vector3f(0.0625F, 0.0F, 0.0F));
      double px = (double)unit.length();
      if (px > 1.0E-6D && Double.isFinite(px)) {
         float front = -0.25625F;
         double[] c = com.myriam.unrp.pass.EyeGlowMath.rgb(rgb);
         double[] hot = com.myriam.unrp.pass.EyeGlowMath.whiter(c, 0.5D);
         PowerEyes.State s = (PowerEyes.State)STATES.get(p.getId());
         boolean world = trail && com.myriam.unrp.pass.PassBodies.inWorld && s != null;
         double[] frame = world ? com.myriam.unrp.pass.PassBodies.layer(m) : null;
         Vec3 cam = world ? Minecraft.getInstance().gameRenderer.getMainCamera().getPosition() : null;

         for(int side = 0; side < 2; ++side) {
            double[] e = eyes[side];
            if (e == null) {
               if (s != null) {
                  s.trails[side].n = 0;
               }
            } else {
               float ex = (float)(e[0] / 16.0D);
               float ey = (float)(e[1] / 16.0D);
               glint(vc, m, ex, ey, front, style, w, h, ex > 0.0F, c, hot, lk, t);
               if (frame != null) {
                  double[] wp = new double[]{frame[0] * (double)ex + frame[1] * (double)ey + frame[2] * (double)front + frame[3], frame[4] * (double)ex + frame[5] * (double)ey + frame[6] * (double)front + frame[7], frame[8] * (double)ex + frame[9] * (double)ey + frame[10] * (double)front + frame[11]};
                  if (near(p, wp, pt)) {
                     PowerEyes.Trail tr = s.trails[side];
                     tr.add(wp, t);
                     trail(vc, tr, px, w, h, c, lk, t, cam);
                  }
               }
            }
         }

      }
   }

   static boolean near(AbstractClientPlayer p, double[] w, float pt) {
      for(double v : w) {
         if (!Double.isFinite(v)) {
            return false;
         }
      }

      Vec3 eye = p.getEyePosition(pt);
      double dx = w[0] - eye.x;
      double dy = w[1] - eye.y;
      double dz = w[2] - eye.z;
      double r = 1.0D + (double)p.getBbHeight();
      return dx * dx + dy * dy + dz * dz <= r * r;
   }

   static void glint(VertexConsumer vc, Matrix4f m, float ex, float ey, float front, int style, int w, int h, boolean right, double[] c, double[] hot, double[] lk, double t) {
      double lit = lk[0];
      double fl = lk[1];
      double burst = lk[3];
      double g = com.myriam.unrp.pass.EyeGlowMath.glow(w, h, fl) / 16.0D;
      decal(vc, 24, m, ex, ey, front, g, g, 0.0D, c, 0.75D * lit * lk[2]);
      if (style == 2) {
         double r = com.myriam.unrp.pass.EyeGlowMath.halo(w, h, fl) / 16.0D;
         decal(vc, 28, m, ex, ey, front, r, r, 0.0D, c, 0.95D * lit);
         double k = com.myriam.unrp.pass.EyeGlowMath.core(w, h, fl) / 16.0D * 0.6D;
         decal(vc, 25, m, ex, ey, front, k, k, 0.0D, hot, 0.95D * lit);
      } else if (style == 1) {
         double k = com.myriam.unrp.pass.EyeGlowMath.core(w, h, fl) / 16.0D;
         decal(vc, 25, m, ex, ey, front, k, k, 0.0D, hot, 0.95D * lit);
         double e = com.myriam.unrp.pass.EyeGlowMath.glint(w, fl) / 16.0D * 0.6D;
         decal(vc, 47, m, ex, ey, front, e, e * com.myriam.unrp.pass.EyeGlowMath.tall(h), 0.0D, hot, 0.57D * lit);
      } else {
         double e = com.myriam.unrp.pass.EyeGlowMath.glint(w, fl) / 16.0D;
         decal(vc, 47, m, ex, ey, front, e, e * com.myriam.unrp.pass.EyeGlowMath.tall(h), com.myriam.unrp.pass.EyeGlowMath.spin(style, right), hot, 0.95D * lit);
      }

      if (burst > 0.004D) {
         double r = com.myriam.unrp.pass.EyeGlowMath.star(burst) / 16.0D;
         decal(vc, 26, m, ex, ey, front, r, r, t * 0.05D, hot, 0.8D * burst * lit);
      }

   }

   static void trail(VertexConsumer vc, PowerEyes.Trail tr, double px, int w, int h, double[] c, double[] lk, double t, Vec3 cam) {
      if (tr.n >= 2) {
         float[] uv = com.myriam.unrp.pass.PassDraw.uv(1, 31, 0.0D, 0.0D, 1.0D, 1.0D);
         Vector3f prev = null;
         double pw = 0.0D;
         double pa = 0.0D;
         double left = 20.0D * px;

         for(int i = 0; i < tr.n; ++i) {
            double age = t - tr.t[i];
            double wd = com.myriam.unrp.pass.EyeGlowMath.trailWidth(age, w, h, lk[1]) * px;
            double al = com.myriam.unrp.pass.EyeGlowMath.trailAlpha(age) * lk[0];
            Vector3f v = com.myriam.unrp.pass.PassBodies.toView((float)(tr.p[i][0] - cam.x), (float)(tr.p[i][1] - cam.y), (float)(tr.p[i][2] - cam.z));
            if (prev != null) {
               double dx = (double)(v.x() - prev.x());
               double dy = (double)(v.y() - prev.y());
               double dz = (double)(v.z() - prev.z());
               double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
               if (len >= left) {
                  double k = len > 1.0E-9D ? left / len : 0.0D;
                  Vector3f end = new Vector3f((float)((double)prev.x() + dx * k), (float)((double)prev.y() + dy * k), (float)((double)prev.z() + dz * k));
                  ribbon(vc, uv, prev, end, pw, pw + (wd - pw) * k, pa, 0.0D, c);
                  return;
               }

               left -= len;
               ribbon(vc, uv, prev, v, pw, wd, pa, al, c);
            }

            if (age >= 3.0D) {
               return;
            }

            prev = v;
            pw = wd;
            pa = al;
         }

      }
   }

   static void ribbon(VertexConsumer vc, float[] uv, Vector3f a, Vector3f b, double wa, double wb, double aa, double ab, double[] c) {
      if (!(aa <= 0.003D) || !(ab <= 0.003D)) {
         double dx = (double)(b.x() - a.x());
         double dy = (double)(b.y() - a.y());
         double dz = (double)(b.z() - a.z());
         if (!(dx * dx + dy * dy + dz * dz < 1.0E-10D)) {
            double mx = (double)(-(a.x() + b.x())) * 0.5D;
            double my = (double)(-(a.y() + b.y())) * 0.5D;
            double mz = (double)(-(a.z() + b.z())) * 0.5D;
            double sx = dy * mz - dz * my;
            double sy = dz * mx - dx * mz;
            double sz = dx * my - dy * mx;
            double l = Math.sqrt(sx * sx + sy * sy + sz * sz);
            if (!(l < 1.0E-9D)) {
               sx /= l;
               sy /= l;
               sz /= l;
               float ra = (float)(c[0] * aa);
               float ga = (float)(c[1] * aa);
               float ba = (float)(c[2] * aa);
               float rb = (float)(c[0] * ab);
               float gb = (float)(c[1] * ab);
               float bb = (float)(c[2] * ab);
               float[][] q = new float[][]{{(float)((double)a.x() - sx * wa), (float)((double)a.y() - sy * wa), (float)((double)a.z() - sz * wa), uv[0], uv[1], ra, ga, ba}, {(float)((double)b.x() - sx * wb), (float)((double)b.y() - sy * wb), (float)((double)b.z() - sz * wb), uv[2], uv[1], rb, gb, bb}, {(float)((double)b.x() + sx * wb), (float)((double)b.y() + sy * wb), (float)((double)b.z() + sz * wb), uv[2], uv[3], rb, gb, bb}, {(float)((double)a.x() + sx * wa), (float)((double)a.y() + sy * wa), (float)((double)a.z() + sz * wa), uv[0], uv[3], ra, ga, ba}};

               for(int i = 0; i < 4; ++i) {
                  vert(vc, q[i]);
               }

               for(int i = 3; i >= 0; --i) {
                  vert(vc, q[i]);
               }

            }
         }
      }
   }

   static void decal(VertexConsumer vc, int cell, Matrix4f m, float ex, float ey, float front, double sx, double sy, double spin, double[] rgb, double a) {
      if (a > 0.003D) {
         float[] uv = com.myriam.unrp.pass.PassDraw.uv(1, cell, 0.0D, 0.0D, 1.0D, 1.0D);
         double cs = Math.cos(spin);
         double sn = Math.sin(spin);
         float ax = (float)(cs * sx);
         float ay = (float)(-sn * sx);
         float bx = (float)(-sn * sy);
         float by = (float)(-cs * sy);
         float r = (float)(rgb[0] * a);
         float g = (float)(rgb[1] * a);
         float b = (float)(rgb[2] * a);
         float[][] at = new float[][]{{ex - ax - bx, ey - ay - by, uv[0], uv[1]}, {ex + ax - bx, ey + ay - by, uv[2], uv[1]}, {ex + ax + bx, ey + ay + by, uv[2], uv[3]}, {ex - ax + bx, ey - ay + by, uv[0], uv[3]}};
         float[][] q = new float[4][];

         for(int i = 0; i < 4; ++i) {
            Vector3f v = m.transformPosition(new Vector3f(at[i][0], at[i][1], front));
            q[i] = new float[]{v.x(), v.y(), v.z(), at[i][2], at[i][3], r, g, b};
         }

         for(int i = 0; i < 4; ++i) {
            vert(vc, q[i]);
         }

         for(int i = 3; i >= 0; --i) {
            vert(vc, q[i]);
         }

      }
   }

   static void vert(VertexConsumer vc, float[] v) {
      vc.vertex(v[0], v[1], v[2], c01(v[5]), c01(v[6]), c01(v[7]), 1.0F, v[3], v[4], OverlayTexture.NO_OVERLAY, 15728880, 0.0F, 1.0F, 0.0F);
   }

   static float c01(float x) {
      return x < 0.0F ? 0.0F : (x > 1.0F ? 1.0F : x);
   }

   static final class State {
      String key = "";
      double onAt = -1.0E9D;
      double actAt = -1.0E9D;
      double pass;
      double passAt = -1.0E9D;
      final PowerEyes.Trail[] trails = new PowerEyes.Trail[]{new PowerEyes.Trail(), new PowerEyes.Trail()};
   }

   static final class Trail {
      final double[][] p = new double[24][];
      final double[] t = new double[24];
      int n;

      void add(double[] w, double now) {
         if (this.n > 0 && (com.myriam.unrp.pass.EyeGlowMath.broken(this.p[0], w) || now < this.t[0] || now - this.t[0] >= 3.0D)) {
            this.n = 0;
         }

         if (this.n > 0 && this.t[0] == now) {
            this.p[0] = w;
         } else {
            for(int i = Math.min(this.n, 23); i > 0; --i) {
               this.p[i] = this.p[i - 1];
               this.t[i] = this.t[i - 1];
            }

            this.p[0] = w;
            this.t[0] = now;

            for(this.n = Math.min(this.n + 1, 24); this.n > 2 && now - this.t[this.n - 2] >= 3.0D; --this.n) {
            }

         }
      }
   }
}
