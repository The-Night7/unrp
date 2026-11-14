package com.myriam.unrp.pass;

public final class PassCast {
   public static final int ED = 16726586;
   public static final int LT = 4568319;
   public static final int AM = 16734751;
   public static final int BR = 10409727;
   public static final int TM = 16740288;
   public static final int PD = 15242256;
   public static final double[] EMBER = PassFx.rgb(16738848);
   public static final double[] OBSIDIAN = PassFx.rgb(1709080);
   public static final double[] SMOKE_MAIN = PassFx.rgb(3353388);
   public static final double[] SMOKE_HOT = PassFx.rgb(5918800);
   public static final double[] SMOKE_DARK = PassFx.rgb(1051661);
   public static final double[][] EYE_AT = new double[][]{{-2.0D, -3.5D}, {2.0D, -3.5D}};
   public static final double[][] BOX_FROM = new double[][]{{-4.0D, -8.0D, -4.0D}, {-4.0D, 0.0D, -2.0D}, {-3.0D, -2.0D, -2.0D}, {-1.0D, -2.0D, -2.0D}, {-2.0D, 0.0D, -2.0D}, {-2.0D, 0.0D, -2.0D}};
   public static final double[][] BOX_SIZE = new double[][]{{8.0D, 8.0D, 8.0D}, {8.0D, 12.0D, 4.0D}, {4.0D, 12.0D, 4.0D}, {4.0D, 12.0D, 4.0D}, {4.0D, 12.0D, 4.0D}, {4.0D, 12.0D, 4.0D}};
   public static final int[][] FACES = new int[][]{{0, 2, 6, 4}, {1, 5, 7, 3}, {0, 4, 5, 1}, {2, 3, 7, 6}, {0, 1, 3, 2}, {4, 6, 7, 5}};

   private PassCast() {
   }

   public static PassCast.Pal pal(int rgb, boolean copy) {
      if (copy) {
         return new PassCast.Pal(SMOKE_MAIN, SMOKE_HOT, SMOKE_DARK, true);
      } else {
         double[] m = PassFx.rgb(rgb);
         return new PassCast.Pal(m, PassFx.whiter(m, 0.62D), PassFx.scale(m, 0.25D), false);
      }
   }

   public static void glints(PassQuads q, PassBody head, double[][] at, double[] c, double k, double flare) {
      double fl = flare;
      double[] hot = PassFx.whiter(c, 0.5D);

      for(double[] e : at) {
         double[] p = head.at(0, e[0], e[1], -4.6D);
         q.bill(1, p, 0.05D + 0.05D * fl, 0.0D, 24, c[0], c[1], c[2], 0.75D * k);
         q.bill(1, p, 0.075D + 0.11D * fl, 0.0D, 47, hot[0], hot[1], hot[2], 0.95D * k);
      }

   }

   public static void ghost(PassQuads q, PassBody b, double[] rgb, double a) {
      if (b != null && b.posed() && !(a <= 0.004D)) {
         for(int part = 0; part < 6; ++part) {
            double[] f = (double[])BOX_FROM[part].clone();
            double[] s = (double[])BOX_SIZE[part].clone();
            if (b.slim && (part == 2 || part == 3)) {
               s[0] = 3.0D;
               if (part == 2) {
                  f[0] = -2.0D;
               }
            }

            double[][] c = new double[8][];

            for(int i = 0; i < 8; ++i) {
               c[i] = b.at(part, f[0] + ((i & 1) != 0 ? s[0] : 0.0D), f[1] + ((i & 2) != 0 ? s[1] : 0.0D), f[2] + ((i & 4) != 0 ? s[2] : 0.0D));
            }

            double[] mid = b.at(part, f[0] + s[0] * 0.5D, f[1] + s[1] * 0.5D, f[2] + s[2] * 0.5D);

            for(int[] face : FACES) {
               double[] fc = PassMath.mul(PassMath.add(PassMath.add(c[face[0]], c[face[1]]), PassMath.add(c[face[2]], c[face[3]])), 0.25D);
               double[] out = PassMath.sub(fc, mid);
               if (!(PassMath.dot(out, PassMath.sub(q.eye, fc)) <= 0.0D)) {
                  q.quad(1, c[face[0]], c[face[1]], c[face[2]], c[face[3]], 40, rgb[0], rgb[1], rgb[2], a, (double[])null);
               }
            }
         }

      }
   }

   public static void eyesTrailer(PassQuads q, PassBody head, double[] c, double k, double flare) {
      glints(q, head, EYE_AT, c, k, flare);
      double[][] xs = new double[][]{{-3.0D, -1.0D}, {1.0D, 3.0D}};

      for(double[] x : xs) {
         double[] p1 = head.at(0, x[0], -3.0D, -4.75D);
         double[] p2 = head.at(0, x[1], -3.0D, -4.75D);
         double[] p3 = head.at(0, x[1], -4.0D, -4.75D);
         double[] p4 = head.at(0, x[0], -4.0D, -4.75D);
         q.quad(1, p1, p2, p3, p4, 40, 1.0D, 1.0D, 1.0D, 0.8D * k, (double[])null);
      }

   }

   public static final class Pal {
      public final double[] main;
      public final double[] hot;
      public final double[] dark;
      public final boolean copy;

      Pal(double[] main, double[] hot, double[] dark, boolean copy) {
         this.main = main;
         this.hot = hot;
         this.dark = dark;
         this.copy = copy;
      }

      public double[] darkOrNull() {
         return this.copy ? this.dark : null;
      }
   }
}
