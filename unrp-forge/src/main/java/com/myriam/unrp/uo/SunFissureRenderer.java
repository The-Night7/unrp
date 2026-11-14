package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SunFissureRenderer extends EntityRenderer<SunFissureEntity> {
   public static final double CORE_WIDE = 1.3D;
   public static final double CORE = 0.85D;
   public static final double JET_GAP_WIDE = 1.0D;
   public static final double JET_GAP = 1.25D;
   public static final double LIP_WIDE = 2.2D;
   public static final double LIP = 1.3D;
   public static final double SUNK_MOST = 2.5D;
   public static final double SCORCH_SIZE = 1.15D;
   public static final double SCORCH_LIP = 0.9D;
   public static final double NEAR = 1.5D;
   public static final double NEAR_FADE = 3.0D;

   public SunFissureRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(SunFissureEntity entity) {
      return DaystarDraw.FX;
   }

   public boolean shouldRender(SunFissureEntity entity, Frustum frustum, double x, double y, double z) {
      return entity.shouldRender(x, y, z);
   }

   static int trace(Level level, Vec3 from, Vec3 d, double s0, double front, Vec3[] pts) {
      int want = pts.length - 1;
      double h = from.y;

      for(int k = 0; k <= want; ++k) {
         double at = s0 + Math.min(front, (double)k);
         double x = from.x + d.x * at;
         double z = from.z + d.z * at;
         double y = SunFissureEntity.walk(level, x, h, z, k == 0 ? 6 : 4);
         if (Double.isNaN(y)) {
            return k - 1;
         }

         pts[k] = new Vec3(x - from.x, y - from.y, z - from.z);
         h = y;
      }

      return want;
   }

   static Vec3 settle(Level level, Vec3 from, Vec3 p) {
      double y = SunFissureEntity.walk(level, from.x + p.x, from.y + p.y, from.z + p.z, 4);
      return Double.isNaN(y) ? null : new Vec3(p.x, y - from.y, p.z);
   }

   static double sunkAt(Level level, Vec3 from, Vec3 p, Vec3 side, double off) {
      double low = Double.NaN;

      for(int sign = -1; sign <= 1; sign += 2) {
         double y = SunFissureEntity.walk(level, from.x + p.x + side.x * off * (double)sign, from.y + p.y, from.z + p.z + side.z * off * (double)sign, 4);
         if (!Double.isNaN(y) && (Double.isNaN(low) || y < low)) {
            low = y;
         }
      }

      return Double.isNaN(low) ? 0.0D : Math.max(0.0D, Math.min(2.5D, low - from.y - p.y));
   }

   static double clampAlong(int last, double s, double front) {
      return Math.max(0.0D, Math.min(Math.min(front, (double)last), s));
   }

   static Vec3 along(Vec3[] pts, int last, double s, double front) {
      if (last <= 0) {
         return pts[0];
      } else {
         double c = clampAlong(last, s, front);
         int k = Math.min(last - 1, (int)Math.floor(c));
         double span = Math.min(front, (double)(k + 1)) - (double)k;
         double f = span > 1.0E-6D ? (c - (double)k) / span : 0.0D;
         return pts[k].add(pts[k + 1].subtract(pts[k]).scale(Math.min(1.0D, f)));
      }
   }

   static double along(double[] v, int last, double s, double front) {
      if (last <= 0) {
         return v[0];
      } else {
         double c = clampAlong(last, s, front);
         int k = Math.min(last - 1, (int)Math.floor(c));
         double span = Math.min(front, (double)(k + 1)) - (double)k;
         double f = span > 1.0E-6D ? (c - (double)k) / span : 0.0D;
         return v[k] + (v[k + 1] - v[k]) * Math.min(1.0D, f);
      }
   }

   static float clear(Vec3 eyeRel, Vec3 p) {
      double dx = p.x - eyeRel.x;
      double dz = p.z - eyeRel.z;
      return DaystarDraw.clamp01((Math.sqrt(dx * dx + dz * dz) - 1.5D) / 3.0D);
   }

   private static double crackSide(int i, int s, int sign) {
      return (double)sign * (0.95D + 0.4D * DaystarDraw.hash(i * 31 + s, sign + 2, 32));
   }

   public void render(SunFissureEntity e, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
      super.render(e, yaw, pt, pose, buffers, light);
      Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
      Vector3f l = camera.getLeftVector();
      Vector3f u = camera.getUpVector();
      Vec3 camRight = new Vec3((double)(-l.x()), (double)(-l.y()), (double)(-l.z()));
      Vec3 camUp = new Vec3((double)u.x(), (double)u.y(), (double)u.z());
      Vec3 eye = camera.getPosition();
      DaystarDraw.normals(pose.last().normal());
      Matrix4f m = pose.last().pose();
      Level level = e.level();
      Vec3 from = e.getPosition(pt);
      double age = (double)((float)e.tickCount + pt);
      double t = age;
      float len = e.length();
      float g = e.gauge();
      double front = (double)e.front((float)age);
      int life = e.life();
      double burnEnd = (double)(life - 20);
      float fade = age > burnEnd ? DaystarDraw.clamp01(1.0D - (age - burnEnd) / 20.0D) : 1.0F;
      float heat = front >= (double)len ? DaystarDraw.clamp01(1.0D - (age - (double)e.runTicks()) / 100.0D) : 1.0F;
      boolean wide = e.wide();
      boolean fall = e.kind() == 2;
      int n = e.rays();
      double s0 = e.start();
      int want = (int)Math.ceil(front);
      Vec3[][] pts = new Vec3[n][want + 1];
      double[][] sunk = new double[n][want + 1];
      int[] last = new int[n];
      double[] reach = new double[n];
      Vec3[] sides = new Vec3[n];
      double lip = wide ? 2.2D : 1.3D;

      for(int i = 0; i < n; ++i) {
         Vec3 d = e.ray(i);
         sides[i] = new Vec3(-d.z, 0.0D, d.x);
         last[i] = trace(level, from, d, s0, front, pts[i]);
         reach[i] = last[i] < want ? (double)Math.max(0, last[i]) : front;

         for(int k = 0; k <= last[i]; ++k) {
            sunk[i][k] = sunkAt(level, from, pts[i][k], sides[i], lip);
         }
      }

      Vec3 hub = null;
      if (fall) {
         double gy = SunFissureEntity.walk(level, from.x, from.y, from.z, 6);
         hub = Double.isNaN(gy) ? null : new Vec3(0.0D, gy - from.y, 0.0D);
      } else if (last[0] >= 0) {
         hub = pts[0][0];
      }

      Set<Long> seen = new HashSet();
      double band = wide ? 2.0D : 1.0D;

      for(int i = 0; i < n; ++i) {
         if (last[i] >= 0) {
            for(double sj = 0.0D; sj <= reach[i]; sj += 0.5D) {
               Vec3 c_0 = along(pts[i], last[i], sj, front);
               boolean trench = along(sunk[i], last[i], sj, front) > 0.5D;
               float ramp = DaystarDraw.clamp01((age - sj / 1.6D) / 3.0D) * fade;

               for(double off = -band; off <= band + 1.0E-6D; off += 0.5D) {
                  Vec3 q_0 = c_0.add(sides[i].scale(off));
                  int bx = Mth.floor(from.x + q_0.x);
                  int bz = Mth.floor(from.z + q_0.z);
                  if (seen.add((long)bx << 32 ^ (long)bz & 4294967295L)) {
                     Vec3 top = settle(level, from, new Vec3((double)bx + 0.5D - from.x, q_0.y, (double)bz + 0.5D - from.z));
                     if (top != null) {
                        double hh = DaystarDraw.hash(bx, bz, 23);
                        boolean lipCol = trench && top.y > c_0.y + 0.5D;
                        float a_0 = ramp * (0.3F + 0.22F * (float)hh) * (Math.abs(off) > band - 0.6D ? 0.7F : 1.0F);
                        Vec3 at = lipCol ? top.add(sides[i].scale(Math.signum(off) * 0.4D)) : top;
                        DaystarDraw.decal(buffers, m, at.add(0.0D, 0.02D + 0.002D * (double)(bx + bz & 3), 0.0D), lipCol ? 0.9D : 1.15D, hh * 6.283D, 0, a_0, light);
                     }
                  }
               }
            }
         }
      }

      if (hub != null) {
         double grow = (double)DaystarDraw.outCubic(age / 3.0D);
         DaystarDraw.decal(buffers, m, hub.add(0.0D, 0.035D, 0.0D), (fall ? 2.6D : (wide ? 1.6D : 1.3D)) * grow, 0.7D, 1, 0.95F * fade, light);
      }

      Vec3 eyeRel = eye.subtract(from);
      Vec3[][][] cracks = new Vec3[n][][];

      for(int i = 0; wide && i < n; ++i) {
         cracks[i] = new Vec3[Math.max(0, last[i] + 1)][2];

         for(int s = 1; s <= last[i]; ++s) {
            for(int h = 0; h < 2; ++h) {
               cracks[i][s][h] = settle(level, from, pts[i][s].add(sides[i].scale(crackSide(i, s, h * 2 - 1))));
            }
         }
      }

      VertexConsumer soft = buffers.getBuffer(DaystarDraw.soft());
      double core = wide ? 1.3D : 0.85D;

      for(int i = 0; i < n; ++i) {
         Vec3 w = sides[i].scale(core);

         for(int s = 0; s < last[i]; ++s) {
            Vec3 a_1 = pts[i][s].add(0.0D, 0.03D, 0.0D);
            Vec3 b_0 = pts[i][s + 1].add(0.0D, 0.03D, 0.0D);
            DaystarDraw.quad(soft, m, a_1.subtract(w), b_0.subtract(w), b_0.add(w), a_1.add(w), 8, 0.16F, 0.1F, 0.08F, 0.9F * fade, false);
         }

         if (wide) {
            Vec3 w2 = sides[i].scale(0.42D);

            for(int s = 1; s < last[i]; ++s) {
               for(int h = 0; h < 2; ++h) {
                  Vec3 a_2 = cracks[i][s][h];
                  Vec3 b_1 = cracks[i][s + 1][h];
                  if (!(DaystarDraw.hash(i, s, 31) < 0.2D) && a_2 != null && b_1 != null && !(Math.abs(a_2.y - b_1.y) > 0.6D)) {
                     a_2 = a_2.add(0.0D, 0.025D, 0.0D);
                     b_1 = b_1.add(0.0D, 0.025D, 0.0D);
                     DaystarDraw.quad(soft, m, a_2.subtract(w2), b_1.subtract(w2), b_1.add(w2), a_2.add(w2), 8, 0.2F, 0.12F, 0.09F, 0.8F * fade, false);
                  }
               }
            }
         }
      }

      List<double[]> jets = new ArrayList();
      int rows = wide ? 2 : 1;
      double jetGap = wide ? 1.0D : 1.25D;

      for(int i = 0; i < n; ++i) {
         if (last[i] >= 0) {
            int j_0 = 0;

            for(int r = 0; r < rows; ++r) {
               for(double sj = 0.4D + (double)r * jetGap * 0.5D; sj <= reach[i]; ++j_0) {
                  double since_0 = age - sj / 1.6D;
                  double h3 = DaystarDraw.hash(j_0, r * 7 + i, 3);
                  double off = rows == 2 ? (r == 0 ? -0.6D : 0.6D) + (DaystarDraw.hash(j_0, r * 7 + i, 5) - 0.5D) * 0.35D : (DaystarDraw.hash(j_0, i, 5) - 0.5D) * 0.6D;
                  Vec3 on = along(pts[i], last[i], sj, front);
                  Vec3 base_0 = settle(level, from, on.add(sides[i].scale(off)));
                  if (base_0 != null) {
                     double sink_0 = Math.max(0.0D, Math.min(2.5D, on.y + along(sunk[i], last[i], sj, front) - base_0.y));
                     double jet = since_0 < 12.0D ? Math.sin(Math.PI * (double)DaystarDraw.clamp01(since_0 / 12.0D)) : 0.0D;
                     double flick = 0.7D + 0.3D * Math.sin(t * 0.9D + (double)j_0 * 1.7D + (double)r * 2.1D + (double)i);
                     double linger = (wide ? 2.0D + 0.9D * h3 : 1.2D + 0.5D * h3) * (double)heat * flick * (double)DaystarDraw.clamp01((since_0 - 6.0D) / 6.0D);
                     double hgt_0 = (wide ? 3.8D + 2.4D * h3 : 2.7D + 1.5D * h3) * (0.75D + 0.25D * (double)g) * jet + linger;
                     hgt_0 = hgt_0 > 0.05D ? hgt_0 + sink_0 : 0.0D;
                     float seen0_0 = clear(eyeRel, base_0);
                     jets.add(new double[]{base_0.x, base_0.y, base_0.z, hgt_0, since_0, (double)j_0, sink_0, (double)seen0_0});
                     if (hgt_0 > 0.05D) {
                        DaystarDraw.upright(soft, m, base_0, camRight, Math.max(wide ? 1.6D + 0.8D * jet : 1.1D + 0.5D * jet, hgt_0 * 0.5D), hgt_0, h3 < 0.25D ? 2 : 9, 1.0F, 0.58F, 0.18F, 0.92F * fade * seen0_0, false);
                     }

                     if (since_0 >= 0.0D && since_0 < 18.0D) {
                        int shards = wide ? 4 : 3;

                        for(int c_0 = 0; c_0 < shards; ++c_0) {
                           double ang = (DaystarDraw.hash(j_0, c_0 + r * 5, 7 + i) - 0.5D) * 2.6D;
                           double out = wide ? 0.16D : 0.12D;
                           double up = (wide ? 0.34D : 0.28D) + 0.1D * DaystarDraw.hash(j_0, c_0, 8 + i);
                           Vec3 q = base_0.add(Math.cos(ang) * out * since_0, sink_0 + up * since_0 - 0.02D * since_0 * since_0, Math.sin(ang) * out * since_0);
                           if (q.y > base_0.y) {
                              DaystarDraw.bill(soft, m, q, camRight, camUp, wide ? 0.2D : 0.14D, since_0 * 0.4D + (double)c_0, 11, 0.32F, 0.24F, 0.2F, DaystarDraw.clamp01(1.0D - since_0 / 18.0D) * clear(eyeRel, q), false);
                           }
                        }
                     }

                     if (since_0 >= 8.0D && (wide || j_0 % 2 == 0)) {
                        double ph_0 = ((since_0 - 8.0D) / 36.0D + DaystarDraw.hash(j_0, i, 11)) % 1.0D;
                        Vec3 sp = base_0.add(sides[i].scale((DaystarDraw.hash(j_0, i, 12) - 0.5D) * 1.4D * ph_0)).add(0.0D, sink_0 + 1.2D + ph_0 * 4.5D, 0.0D);
                        DaystarDraw.bill(soft, m, sp, camRight, camUp, 0.55D + 1.3D * ph_0, since_0 * 0.02D + (double)j_0, 10, 0.13F, 0.11F, 0.1F, 0.3F * (float)Math.sin(Math.PI * ph_0) * (0.35F + 0.65F * heat) * fade, false);
                     }
                  }

                  sj += jetGap;
               }
            }

            if (last[i] == want && front < (double)len && front > 0.0D) {
               Vec3 tip_0 = pts[i][last[i]];
               double lick = 0.8D + 0.2D * Math.sin(t * 1.3D + (double)i);
               DaystarDraw.upright(soft, m, tip_0, camRight, wide ? 1.8D : 1.2D, (wide ? 2.6D : 1.8D) * lick + sunk[i][last[i]], 9, 1.0F, 0.66F, 0.24F, 0.9F, false);
            }
         }
      }

      VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
      double glowW = wide ? 1.25D : 0.8D;

      for(int i = 0; i < n; ++i) {
         for(int s = 0; s < last[i]; ++s) {
            Vec3 a_3 = pts[i][s].add(0.0D, 0.06D, 0.0D);
            Vec3 b_2 = pts[i][s + 1].add(0.0D, 0.06D, 0.0D);
            DaystarDraw.ribbon(add, m, a_3, b_2, eyeRel, glowW, 8, 1.0F, 0.55F + 0.2F * heat, 0.2F, 0.75F * heat * fade, true);
            DaystarDraw.ribbon(add, m, a_3.add(0.0D, 0.01D, 0.0D), b_2.add(0.0D, 0.01D, 0.0D), eyeRel, glowW * 0.4D, 8, 1.0F, 0.92F, 0.62F, 0.85F * heat * fade, true);
         }

         if (wide) {
            for(int s = 1; s < last[i]; ++s) {
               for(int h = 0; h < 2; ++h) {
                  Vec3 a_4 = cracks[i][s][h];
                  Vec3 b_3 = cracks[i][s + 1][h];
                  if (!(DaystarDraw.hash(i, s, 31) < 0.2D) && a_4 != null && b_3 != null && !(Math.abs(a_4.y - b_3.y) > 0.6D)) {
                     DaystarDraw.ribbon(add, m, a_4.add(0.0D, 0.055D, 0.0D), b_3.add(0.0D, 0.055D, 0.0D), eyeRel, 0.42D, 8, 1.0F, 0.5F + 0.2F * heat, 0.16F, 0.6F * heat * fade, true);
                  }
               }
            }

            for(double sj = 1.0D; sj <= reach[i]; sj += 2.2D) {
               Vec3 c = along(pts[i], last[i], sj, front).add(0.0D, 1.0D + along(sunk[i], last[i], sj, front), 0.0D);
               float shim = 0.8F + 0.2F * (float)Math.sin(t * 0.37D + sj);
               DaystarDraw.bill(add, m, c, camRight, camUp, 1.9D, 0.0D, 5, 1.0F, 0.45F, 0.12F, 0.2F * heat * fade * shim, true);
            }
         }
      }

      for(double[] jt : jets) {
         Vec3 base = new Vec3(jt[0], jt[1], jt[2]);
         double hgt = jt[3];
         double since = jt[4];
         int j = (int)jt[5];
         double sink = jt[6];
         float seen0 = (float)jt[7];
         if (hgt > 0.05D) {
            DaystarDraw.upright(add, m, base, camRight, Math.max(wide ? 0.8D : 0.6D, hgt * 0.22D), hgt * 0.8D, 2, 1.0F, 0.78F, 0.34F, 0.6F * fade * seen0, true);
         }

         if (since >= 0.0D && since < 8.0D) {
            double k_0 = 1.0D - since / 8.0D;
            DaystarDraw.bill(add, m, base.add(0.0D, 0.6D + sink, 0.0D), camRight, camUp, (wide ? 2.1D : 1.3D) * k_0, 0.0D, 5, 1.0F, 0.7F, 0.3F, 0.8F * (float)k_0 * seen0, true);
         }

         if (heat > 0.0F && j % 2 == 0) {
            double ph = (t * 0.03D + (double)j * 0.13D + jt[0] * 0.05D) % 1.0D;
            DaystarDraw.bill(add, m, base.add(0.0D, sink + 0.2D + ph * (wide ? 3.0D : 2.2D), 0.0D), camRight, camUp, wide ? 0.07D : 0.05D, 0.0D, 7, 1.0F, 0.7F, 0.3F, (float)(1.0D - ph) * heat * fade, true);
         }
      }

      for(int i = 0; i < n; ++i) {
         if (last[i] == want && !(front >= (double)len) && !(front <= 0.0D)) {
            Vec3 tip = pts[i][last[i]];
            double rise = sunk[i][last[i]];
            DaystarDraw.bill(add, m, tip.add(0.0D, 0.8D + rise, 0.0D), camRight, camUp, wide ? 2.6D : 1.8D, t * 0.05D, 1, 1.0F, 0.75F, 0.35F, 0.8F, true);
            DaystarDraw.bill(add, m, tip.add(0.0D, 0.6D + rise, 0.0D), camRight, camUp, wide ? 3.4D : 2.2D, 0.0D, 5, 1.0F, 0.6F, 0.22F, 0.55F, true);
            if (wide) {
               DaystarDraw.ring(add, m, tip.add(0.0D, 0.07D, 0.0D), 0.9D + 0.3D * Math.sin(t * 0.8D), 0.25D, 24, 1.0F, 0.7F, 0.3F, 0.6F);
            }
         }
      }

      if (hub != null) {
         Vec3 h = hub.add(0.0D, 0.06D, 0.0D);
         float see = fall && Minecraft.getInstance().options.getCameraType().isFirstPerson() ? clear(eyeRel, h) : 1.0F;
         if (fall) {
            double k = (double)DaystarDraw.outCubic(age / 7.0D);
            float a = DaystarDraw.clamp01(1.0D - age / 10.0D);
            DaystarDraw.ring(add, m, h, (double)e.ring() * k, 0.5D, 48, 1.0F, 0.72F, 0.3F, 0.9F * a);
            DaystarDraw.ring(add, m, h.add(0.0D, 0.01D, 0.0D), (double)e.ring() * 0.6D * k, 0.3D, 40, 1.0F, 0.86F, 0.5F, 0.7F * a);
            if (age < 9.0D && see > 0.0F) {
               float b_4 = DaystarDraw.clamp01(1.0D - age / 9.0D);
               DaystarDraw.upright(add, m, h, camRight, 2.2D, 9.0D * (0.6D + 0.4D * (double)b_4), 12, 1.0F, 0.8F, 0.4F, 0.8F * b_4 * see, true);
            }

            DaystarDraw.bill(add, m, h.add(0.0D, 0.5D, 0.0D), camRight, camUp, 2.2D, 0.0D, 5, 1.0F, 0.5F, 0.15F, 0.35F * heat * fade * see, true);
         }

         double flash = fall ? 6.0D : 5.0D;
         if (age < flash) {
            float b = DaystarDraw.clamp01(1.0D - age / flash);
            DaystarDraw.bill(add, m, h.add(0.0D, 0.7D, 0.0D), camRight, camUp, (fall ? 6.0D : (wide ? 3.2D : 2.4D)) * (double)b, 0.0D, 5, 1.0F, 0.8F, 0.45F, 0.9F * b * (0.33F + 0.67F * see), true);
         }
      }

   }
}
