package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class WeaveDraw {
   public static final ResourceLocation TEX = new ResourceLocation("unrp", "textures/entity/aura_thread.png");
   public static final int STRAND = 0;
   public static final int SOFT = 1;
   public static final int MOTE = 2;
   public static final int STAR = 3;
   public static final int KNOT = 4;
   public static final int CORE = 5;
   public static final int THIN = 6;
   public static final int SPARK = 7;
   public static final int TEAL = 5692644;
   public static final int GROW = 4;
   public static final int BESTOW_ARRIVE = 9;
   public static final double TETHER_NEAR = 6.0D;
   public static final double TETHER_FAR = 16.0D;
   public static final int INTRO = 16;
   private static final Vec3 X = new Vec3(1.0D, 0.0D, 0.0D);
   private static final Vec3 Z = new Vec3(0.0D, 0.0D, 1.0D);

   private WeaveDraw() {
   }

   public static RenderType add() {
      return RenderType.eyes(TEX);
   }

   private static float r8(int c) {
      return (float)(c >> 16 & 255) / 255.0F;
   }

   private static float g8(int c) {
      return (float)(c >> 8 & 255) / 255.0F;
   }

   private static float b8(int c) {
      return (float)(c & 255) / 255.0F;
   }

   private static double clamp01(double x) {
      return x < 0.0D ? 0.0D : (x > 1.0D ? 1.0D : x);
   }

   public static float[] mix(int c, float k) {
      return new float[]{r8(c) + (1.0F - r8(c)) * k, g8(c) + (1.0F - g8(c)) * k, b8(c) + (1.0F - b8(c)) * k};
   }

   private static Vec3 axis(Vec3 a, Vec3 b, double t, double sag) {
      return a.add(b.subtract(a).scale(t)).subtract(0.0D, sag * 4.0D * t * (1.0D - t), 0.0D);
   }

   public static Vec3 point(Vec3 a, Vec3 b, double t, int k, double phase, double r, double turns, double sag, Vec3 e1, Vec3 e2) {
      double phi = (Math.PI * 2D) * turns * t - phase + (double)k * Math.PI;
      double rr = r * Math.sin(Math.PI * t);
      return axis(a, b, t, sag).add(e1.scale(rr * Math.cos(phi))).add(e2.scale(rr * Math.sin(phi)));
   }

   public static void thread(VertexConsumer add, Matrix4f m, Vec3 a, Vec3 b, Vec3 eye, double phase, double from, double to, int rgb, float alpha, float w, double r, int strands, float glow) {
      Vec3 d = b.subtract(a);
      double l = d.length();
      if (!(l < 0.05D) && !(to <= from) && !(alpha <= 0.003F)) {
         Vec3[] bs = ArsenalDraw.basis(d.scale(1.0D / l));
         int n = Math.max(8, Math.min(40, (int)Math.ceil(l * 5.0D)));
         double turns = Math.max(1.0D, l * 1.2D);
         double sag = Math.min(0.6D, 0.06D * l);
         float[][] cols = new float[][]{mix(5692644, 0.15F), mix(rgb, 0.1F)};

         for(int k = 0; k < strands; ++k) {
            for(int i = 0; i < n; ++i) {
               double t0 = Math.max(from, (double)i / (double)n);
               double t1 = Math.min(to, (double)(i + 1) / (double)n);
               if (!(t1 <= t0)) {
                  double tm = (t0 + t1) * 0.5D;
                  double fade = Math.min(1.0D, Math.min((tm - from) / 0.08D, (to - tm) / 0.08D));
                  ArsenalDraw.ribbon(add, m, point(a, b, t0, k, phase, r, turns, sag, bs[0], bs[1]), point(a, b, t1, k, phase, r, turns, sag, bs[0], bs[1]), eye, w, 0, cols[k][0], cols[k][1], cols[k][2], (float)((double)alpha * fade), true);
               }
            }
         }

         if (glow > 0.0F) {
            int g = Math.max(4, Math.min(16, (int)Math.ceil(l * 2.0D)));

            for(int j = 0; j < g; ++j) {
               double s0 = Math.max(from, (double)j / (double)g);
               double s1 = Math.min(to, (double)(j + 1) / (double)g);
               if (!(s1 <= s0)) {
                  ArsenalDraw.ribbon(add, m, axis(a, b, s0, sag), axis(a, b, s1, sag), eye, w * 3.2F, 1, r8(rgb), g8(rgb), b8(rgb), glow * alpha, true);
               }
            }
         }

      }
   }

   public static void bead(VertexConsumer add, Matrix4f m, Vec3 at, Vec3 camRight, Vec3 camUp, int rgb, float size, float age, float alpha) {
      float[] w = mix(rgb, 0.55F);
      ArsenalDraw.plane(add, m, at, camRight, camUp, size, 0.0F, 2, r8(rgb), g8(rgb), b8(rgb), alpha, true);
      ArsenalDraw.plane(add, m, at, camRight, camUp, size * 1.4F, age * 0.2F, 3, w[0], w[1], w[2], 0.8F * alpha, true);
   }

   private static void arrive(VertexConsumer add, Matrix4f m, Vec3 at, Vec3 camRight, Vec3 camUp, int rgb, float f) {
      float[] w = mix(rgb, 0.4F);
      ArsenalDraw.plane(add, m, at, camRight, camUp, 0.3F * (1.0F - f) + 0.05F, 0.0F, 5, r8(rgb), g8(rgb), b8(rgb), 1.0F - f, true);
      ArsenalDraw.plane(add, m, at, camRight, camUp, 0.2F + 0.5F * f, 0.0F, 6, w[0], w[1], w[2], 1.0F - f, true);
   }

   private static void knot(VertexConsumer add, Matrix4f m, Vec3 c, int rgb, float size, float spin, float alpha) {
      float[] w = mix(rgb, 0.3F);
      ArsenalDraw.plane(add, m, c, X, Z, size, spin, 4, w[0], w[1], w[2], alpha, true);
   }

   public static void siphon(VertexConsumer add, Matrix4f m, Vec3 src, Vec3 dst, Vec3 eye, float age, int rgb, Vec3 camRight, Vec3 camUp) {
      if (!(age < 0.0F) && !(age > 16.0F)) {
         int arrive = 11;
         double to = (double)ArsenalDraw.out(age / 4.0F);
         double from = age > (float)arrive ? clamp01((double)(age - (float)arrive) / (double)(16 - arrive)) : 0.0D;
         thread(add, m, src, dst, eye, (double)age * 0.35D, from, to, rgb, 0.95F, 0.05F, 0.11D, 2, 0.28F);
         if (age >= 2.0F && age < (float)arrive) {
            float t = (float)clamp01((double)((age - 2.0F) / ((float)arrive - 2.0F)));
            bead(add, m, src.add(dst.subtract(src).scale((double)ArsenalDraw.out(t))), camRight, camUp, rgb, 0.18F, age, 1.0F);
         }

         if (age >= (float)arrive) {
            arrive(add, m, dst, camRight, camUp, rgb, (float)clamp01((double)(age - (float)arrive) / (double)(16 - arrive)));
         }

      }
   }

   public static void bestow(VertexConsumer add, Matrix4f m, Vec3 src, Vec3 dst, Vec3 eye, float age, int rgb, Vec3 camRight, Vec3 camUp) {
      if (!(age < 0.0F) && !(age > 14.0F)) {
         double to = (double)ArsenalDraw.out(age / 4.0F);
         double from = age > 9.0F ? clamp01((double)(age - 9.0F) / 5.0D) : 0.0D;
         thread(add, m, src, dst, eye, (double)age * 0.35D, from, to, rgb, 0.95F, 0.05F, 0.11D, 2, 0.28F);
         if (age >= 1.0F && age < 9.0F) {
            float t = (float)clamp01((double)((age - 1.0F) / 8.0F));
            bead(add, m, src.add(dst.subtract(src).scale((double)ArsenalDraw.out(t))), camRight, camUp, rgb, 0.17F, age, 1.0F);
         }

         if (age >= 9.0F) {
            float f = (float)clamp01((double)(age - 9.0F) / 5.0D);
            knot(add, m, dst, rgb, 0.55F + 0.25F * f, age * 0.1F, 0.9F * (1.0F - f));
            float[] w = mix(rgb, 0.4F);
            ArsenalDraw.plane(add, m, dst, X, Z, 0.5F + 0.8F * f, 0.0F, 6, w[0], w[1], w[2], 1.0F - f, true);
         }

      }
   }

   public static void tether(VertexConsumer add, Matrix4f m, Vec3 src, Vec3 dst, Vec3 eye, float age, int rgb, Vec3 camRight, Vec3 camUp, float phase, boolean link) {
      float fade = (float)clamp01((double)(age - 9.0F) / 4.0D);
      if (!(fade <= 0.0F)) {
         for(int j = 0; j < 3; ++j) {
            double ang = (double)age * 0.07D + (double)j * 2.0943951023931953D + (double)phase;
            double bob = 0.12D * Math.sin(ang * 2.0D + (double)j) + 0.15D * Math.sin(ang);
            ArsenalDraw.plane(add, m, dst.add(0.55D * Math.cos(ang), bob, 0.55D * Math.sin(ang)), camRight, camUp, 0.09F, 0.0F, 2, r8(rgb), g8(rgb), b8(rgb), 0.9F * fade, true);
            double ang2 = ang - 0.25D;
            double bob2 = 0.12D * Math.sin(ang2 * 2.0D + (double)j) + 0.15D * Math.sin(ang2);
            ArsenalDraw.plane(add, m, dst.add(0.55D * Math.cos(ang2), bob2, 0.55D * Math.sin(ang2)), camRight, camUp, 0.055F, 0.0F, 2, r8(rgb), g8(rgb), b8(rgb), 0.5F * fade, true);
         }

         if (link) {
            double d = dst.subtract(src).length();
            float a = (float)(0.45D * (double)fade * clamp01((16.0D - d) / 10.0D));
            if (a > 0.003F) {
               thread(add, m, src, dst, eye, (double)age * 0.12D + (double)phase, 0.0D, 1.0D, rgb, a, 0.03F, 0.04D, 1, 0.15F);
            }
         }

      }
   }

   public static void unravel(VertexConsumer add, Matrix4f m, Vec3 c, float age, int rgb, Vec3 camRight, Vec3 camUp) {
      if (!(age < 0.0F) && !(age > 12.0F)) {
         float f = (float)clamp01((double)age / 12.0D);
         knot(add, m, c, rgb, 0.6F + 0.8F * f, age * 0.15F, 1.0F - f);

         for(int j = 0; j < 6; ++j) {
            double ang = (double)j * (Math.PI / 3D) + 0.3D;
            Vec3 p = c.add(Math.cos(ang) * (0.3D + 1.2D * (double)f), 0.25D * (double)f, Math.sin(ang) * (0.3D + 1.2D * (double)f));
            ArsenalDraw.plane(add, m, p, camRight, camUp, 0.08F, 0.0F, 7, r8(rgb), g8(rgb), b8(rgb), 1.0F - f, true);
         }

      }
   }

   public static void blade(VertexConsumer add, Matrix4f m, Vec3 eye, Vec3 camRight, Vec3 camUp, float age, int rgb, float intro) {
      Vec3 a = new Vec3(0.22D, 0.22D, 0.5D);
      Vec3 b = new Vec3(0.94D, 0.94D, 0.5D);
      double e = clamp01((double)intro / 16.0D);
      double r = 0.075D + 0.4D * (1.0D - (double)ArsenalDraw.out((float)e));
      float alpha = Math.min(1.0F, intro / 4.0F + 0.001F);
      Vec3 e1 = new Vec3(-0.70710678D, 0.70710678D, 0.0D);
      Vec3 e2 = Z;
      int n = 20;
      double turns = 3.0D;
      double phase = (double)age * 0.25D;
      float[][] cols = new float[][]{mix(5692644, 0.15F), mix(rgb, 0.1F)};

      for(int k = 0; k < 2; ++k) {
         for(int i = 0; i < n; ++i) {
            ArsenalDraw.ribbon(add, m, point(a, b, (double)i / (double)n, k, phase, r, turns, 0.0D, e1, e2), point(a, b, (double)(i + 1) / (double)n, k, phase, r, turns, 0.0D, e1, e2), eye, 0.045F, 0, cols[k][0], cols[k][1], cols[k][2], 0.85F * alpha, true);
         }
      }

      for(int j = 0; j < 4; ++j) {
         ArsenalDraw.ribbon(add, m, axis(a, b, (double)j / 4.0D, 0.0D), axis(a, b, (double)(j + 1) / 4.0D, 0.0D), eye, 0.14F, 1, r8(rgb), g8(rgb), b8(rgb), 0.25F * alpha, true);
      }

      for(int k = 0; k < 2; ++k) {
         double tm = ((double)age * 0.03D + (double)k * 0.5D) % 1.0D;
         ArsenalDraw.plane(add, m, point(a, b, tm, k, phase, r, turns, 0.0D, e1, e2), camRight, camUp, 0.05F, 0.0F, 2, r8(rgb), g8(rgb), b8(rgb), 0.9F * alpha, true);
      }

      if (intro < 6.0F) {
         float f = (float)clamp01((double)intro / 6.0D);
         float[] w = mix(rgb, 0.4F);
         ArsenalDraw.plane(add, m, a, camRight, camUp, 0.15F + 0.4F * f, 0.0F, 6, w[0], w[1], w[2], 1.0F - f, true);
      }

   }
}
