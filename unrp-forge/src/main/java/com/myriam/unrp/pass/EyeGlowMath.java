package com.myriam.unrp.pass;

public final class EyeGlowMath {
   public static final int SHARP = 0;
   public static final int ROUND = 1;
   public static final int RING = 2;
   public static final double IN = 4.0D;
   public static final double BURST = 12.0D;
   public static final double BURST_PEAK = 3.0D;
   public static final double ACT = 10.0D;
   public static final double HELD = 2.0D;
   public static final double PULSE = 0.12D;
   public static final double PULSE_RATE = 0.14D;
   public static final double TILT = 0.18D;
   public static final double FRONT = -4.1D;
   public static final double GLOW = 0.8D;
   public static final double GLINT = 1.2D;
   public static final double CORE = 1.0D;
   public static final double HALO = 1.6D;
   public static final double STAR = 2.0D;
   public static final double GLOW_FLARE = 1.0D;
   public static final double GLINT_FLARE = 1.47D;
   public static final double CORE_FLARE = 0.8D;
   public static final double HALO_FLARE = 0.6D;
   public static final double STAR_FLARE = 3.0D;
   public static final double GLOW_A = 0.75D;
   public static final double GLINT_A = 0.95D;
   public static final double STAR_A = 0.8D;
   public static final double WHITER = 0.5D;
   public static final double HOT = 0.55D;
   public static final double TRAIL = 3.0D;
   public static final int MOST = 24;
   public static final double JUMP = 1.5D;
   public static final double TRAIL_W = 1.1D;
   public static final double TRAIL_A = 0.55D;
   public static final double TRAIL_FLARE = 0.5D;
   public static final double TRAIL_LEN = 20.0D;

   private EyeGlowMath() {
   }

   public static double clamp01(double x) {
      return x < 0.0D ? 0.0D : (x > 1.0D ? 1.0D : x);
   }

   public static double lit(double on) {
      double x = clamp01(on / 4.0D);
      return x * x * (3.0D - 2.0D * x);
   }

   public static double burst(double on) {
      if (on >= 0.0D && !(on >= 12.0D)) {
         if (on < 3.0D) {
            return on / 3.0D;
         } else {
            double x = 1.0D - (on - 3.0D) / 9.0D;
            return x * x;
         }
      } else {
         return 0.0D;
      }
   }

   public static double act(double since) {
      if (since >= 0.0D && !(since >= 10.0D)) {
         double x = 1.0D - since / 10.0D;
         return x * x;
      } else {
         return 0.0D;
      }
   }

   public static double flare(double on, double since, double pass, double passSince) {
      double f = Math.max(burst(on), act(since));
      if (passSince >= 0.0D && passSince <= 2.0D) {
         f = Math.max(f, clamp01(pass));
      }

      return f;
   }

   public static double pulse(double t, int seed) {
      return 0.94D + 0.06D * Math.sin(t * 0.14D + (double)seed * 1.7D);
   }

   public static double wide(int w) {
      return 1.0D + 0.25D * (double)(Math.max(1, Math.min(4, w)) - 2);
   }

   public static double tall(int h) {
      return 1.0D + 0.3D * (double)(Math.max(1, Math.min(3, h)) - 1);
   }

   public static double big(int w, int h) {
      return Math.max(wide(w), tall(h));
   }

   public static double glow(int w, int h, double flare) {
      return 0.8D * big(w, h) * (1.0D + 1.0D * flare);
   }

   public static double glint(int w, double flare) {
      return 1.2D * wide(w) * (1.0D + 1.47D * flare);
   }

   public static double core(int w, int h, double flare) {
      return 1.0D * big(w, h) * (1.0D + 0.8D * flare);
   }

   public static double halo(int w, int h, double flare) {
      return 1.6D * big(w, h) * (1.0D + 0.6D * flare);
   }

   public static double star(double burst) {
      return 2.0D + 3.0D * burst;
   }

   public static double spin(int style, boolean right) {
      if (style != 0) {
         return 0.0D;
      } else {
         return right ? 0.18D : -0.18D;
      }
   }

   public static double hot(double heat, double flare) {
      return Math.max(heat, 0.55D * flare);
   }

   public static double[] whiter(double[] c, double k) {
      return new double[]{c[0] + (1.0D - c[0]) * k, c[1] + (1.0D - c[1]) * k, c[2] + (1.0D - c[2]) * k};
   }

   public static double[] rgb(int rgb) {
      return new double[]{(double)(rgb >> 16 & 255) / 255.0D, (double)(rgb >> 8 & 255) / 255.0D, (double)(rgb & 255) / 255.0D};
   }

   public static double trailWidth(double age, int w, int h, double flare) {
      return age >= 0.0D && !(age >= 3.0D) ? 1.1D * big(w, h) * Math.pow(1.0D - age / 3.0D, 0.7D) * (1.0D + 0.5D * flare) : 0.0D;
   }

   public static double trailAlpha(double age) {
      return age >= 0.0D && !(age >= 3.0D) ? 0.55D * Math.pow(1.0D - age / 3.0D, 1.6D) : 0.0D;
   }

   public static boolean broken(double[] last, double[] next) {
      double dx = next[0] - last[0];
      double dy = next[1] - last[1];
      double dz = next[2] - last[2];
      return !(dx * dx + dy * dy + dz * dz <= 2.25D);
   }
}
