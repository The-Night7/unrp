package com.myriam.unrp.pass;

public final class PassAm {
   public static final double[] EMBER = PassCast.EMBER;
   public static final double[] OBS = PassCast.OBSIDIAN;
   public static final double[] EXPEL_SMOKE = new double[]{0.08D, 0.06D, 0.07D};
   public static final double[] SHELL_SMOKE = new double[]{0.09D, 0.08D, 0.08D};
   public static final int SHELL_SMOKE_LIFE = 33;
   public static final double SHELL_SMOKE_UP = 1.0D;
   public static final double REACH = 24.0D;
   public static final int IGNITE_LIFE = 40;
   public static final int SENSE_LIFE = 30;
   public static final int SENSE_RUN = 24;
   public static final int SENSE_FLARE = 12;
   public static final int LAND_AT = 12;
   public static final int LANDED_LIFE = 30;
   public static final int EXPEL_AT = 5;
   public static final int EXPEL_LIFE = 60;
   public static final int EYES_IN = 8;
   public static final int EYES_OUT = 4;
   public static final int FLARE_FOR = 24;
   public static final double FLARE_LOW = 0.2D;
   public static final double FP_REF = 4.0D;
   public static final double FP_LEAST = 0.2D;

   private PassAm() {
   }

   public static double[] col(int rgb) {
      return rgb == 0 ? EMBER : PassFx.rgb(rgb);
   }

   public static double fp(boolean fp, double[] eye, double[] p) {
      return fp ? PassMath.clamp(PassMath.dist(eye, p) / 4.0D, 0.2D, 1.0D) : 1.0D;
   }

   public static void ignite(PassQuads q, double[] g, double age) {
      double k = PassMath.clamp01(1.0D - age / 20.0D);
      double r = 0.5D + 3.0D * PassMath.outCubic(age / 14.0D);
      q.ring(0, PassMath.v(g[0], g[1] + 0.05D, g[2]), r, 0.7D, 32, OBS, 0.7D * k, 40, (double[])null, 0.0D);
      q.ring(1, PassMath.v(g[0], g[1] + 0.06D, g[2]), r + 0.3D, 0.1D, 32, EMBER, 0.55D * k, 40, (double[])null, 0.0D);
   }

   public static void sense(PassQuads q, double[] g, double age) {
      double r = 0.5D + 24.0D * PassMath.outCubic(PassMath.clamp01(age / 24.0D));
      double k = PassMath.clamp01(1.0D - age / 28.0D);
      q.ring(1, PassMath.v(g[0], g[1] + 0.05D, g[2]), r, 0.5D, 32, EMBER, 0.7D * k, 96, (double[])null, 0.0D);
      if (r > 2.0D) {
         q.ring(1, PassMath.v(g[0], g[1] + 0.045D, g[2]), r - 1.2D, 2.0D, 32, EMBER, 0.14D * k, 96, (double[])null, 0.0D);
      }

      q.ring(0, PassMath.v(g[0], g[1] + 0.04D, g[2]), r - 0.2D, 0.8D, 32, OBS, 0.3D * k, 96, (double[])null, 0.0D);
   }

   public static double reached(double d, double age) {
      if (!(d > 24.0D) && !(d < 0.5D)) {
         double x = 1.0D - Math.cbrt(1.0D - (d - 0.5D) / 24.0D);
         return age - 24.0D * x;
      } else {
         return -1.0D;
      }
   }

   public static boolean flares(double since) {
      return since >= 0.0D && since < 12.0D;
   }

   public static void flare(PassQuads q, double[] feet, double[] chest, double since, double[] col) {
      if (flares(since)) {
         double u = since / 12.0D;
         q.ring(1, PassMath.v(feet[0], feet[1] + 0.06D, feet[2]), 0.5D + 0.6D * PassMath.outCubic(u), 0.2D, 32, col, 0.9D * (1.0D - u), 32, (double[])null, 0.0D);
         PassFx.glow(q, chest, 0.7D * (1.0D - u) + 0.2D, col, 0.55D * (1.0D - u));
      }
   }

   public static void landed(PassQuads q, double[] hand, double age, double[] col, boolean fp) {
      double[] hot = PassFx.whiter(col, 0.6D);
      double k = PassMath.clamp01(1.0D - age / 14.0D);
      double s = fp(fp, q.eye, hand);
      PassFx.star(q, hand, 1.2D * k * s, age * 0.2D, hot, k, true);
      PassFx.glow(q, hand, (0.8D * k + 0.1D) * s, col, 0.7D * k);
      PassFx.sparks(q, hand, 421.0D, age, 10, 0.16D, hot, k, (new PassFx.O()).life(10.0D).g(0.02D).w(s));
   }

   public static void expel(PassQuads q, double[] light, double[] g, double age) {
      double k = PassMath.clamp01(1.0D - age / 18.0D);
      q.ring(0, PassMath.v(g[0], g[1] + 0.05D, g[2]), 0.5D + 5.0D * PassMath.outCubic(age / 14.0D), 1.0D, 32, OBS, 0.8D * k, 48, (double[])null, 0.0D);
      q.ring(1, PassMath.v(g[0], g[1] + 0.06D, g[2]), 0.8D + 5.0D * PassMath.outCubic(age / 14.0D), 0.1D, 32, EMBER, 0.45D * k, 48, (double[])null, 0.0D);
      PassFx.smoke(q, light, PassMath.v(g[0], g[1] + 0.4D, g[2]), 432.0D, age, 8, EXPEL_SMOKE, 0.5D, (new PassFx.O()).life(30.0D).rise(0.04D).spread(0.08D).size(0.5D).stagger(1.0D));
   }

   public static double[] shellSmokeAt(double[] c) {
      return PassMath.v(c[0], c[1] + 1.0D, c[2]);
   }

   public static void shellSmoke(PassQuads q, double[] light, double[] c, double age) {
      PassFx.smoke(q, light, shellSmokeAt(c), 460.0D, age, 12, SHELL_SMOKE, 0.6D, (new PassFx.O()).life(26.0D).rise(0.05D).spread(0.12D).size(0.6D).stagger(0.5D));
   }

   public static double eyesIn(double on) {
      return PassMath.clamp01(on / 8.0D);
   }

   public static double flareAge(double t, double takeAt, boolean seen) {
      return seen && !Double.isNaN(takeAt) ? t - takeAt - 12.0D : 99.0D;
   }

   public static boolean flaring(double fa) {
      return fa >= 0.0D && fa < 24.0D;
   }

   public static double[] eyeColor(double fa, double[] col) {
      return flaring(fa) ? PassFx.mix(col, EMBER, PassMath.clamp01(fa / 24.0D)) : EMBER;
   }

   public static double eyeFlare(double fa) {
      return flaring(fa) ? 1.0D - fa / 24.0D : 0.2D;
   }

   public static double eyesOut(double off, double from) {
      return PassMath.clamp01(from * (1.0D - off / 4.0D));
   }
}
