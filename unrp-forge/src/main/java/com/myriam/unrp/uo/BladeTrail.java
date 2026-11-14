package com.myriam.unrp.uo;

import com.myriam.unrp.anim.MoveClips;

import com.myriam.unrp.anim.MoveRig;


public final class BladeTrail {
   public static final double LAG = 1.2D;
   public static final double LINGER = 7.0D;
   public static final int STEPS = 24;
   public static final double RAMP = 0.65D;
   public static final int SPARKS = 20;
   public static final double[] BODY = new double[]{0.7D, 0.5D, 0.6D, 0.5D};
   public static final double[] FLAMES = new double[]{0.6D, 0.4D, 0.3D, 0.6D};
   public static final double[] HOT = new double[]{0.35D, 0.35D, 0.12D, 0.4D};
   public static final double CORE = 0.35D;
   public static final double SPRITE = 32.0D;
   public static final double EDGE_U = 11.0D;
   public static final double EDGE_V = 5.5D;
   public static final double BACK_U = 16.6D;
   public static final double BACK_V = 15.4D;
   public static final double TURN_X = 0.0D;
   public static final double TURN_Y = -90.0D;
   public static final double TURN_Z = 55.0D;
   public static final double MOVE_X = 0.0D;
   public static final double MOVE_Y = 10.96D;
   public static final double MOVE_Z = 0.46D;
   public static final double SCALE = 1.6D;
   public static final double HAND_X = 1.0D;
   public static final double HAND_Y = 2.0D;
   public static final double HAND_Z = -10.0D;
   public static final double MODEL = 0.9375D;
   public static final double PIVOT = 1.501D;
   public static final String[] NAMES = new String[]{"ds_chop", "ds_sunder", "ds_land", "ds_skyward", "ds_hew"};
   public static final double[] FROM = new double[]{5.0D, 6.0D, 0.5D, 5.0D, 4.45D};
   public static final double[] HIT = new double[]{6.5D, 7.5D, 2.0D, 6.25D, 5.5D};
   public static final boolean[] FLASH = new boolean[]{true, false, false, false, false};
   public static final int KEEP = 4;
   public static final double SAME = 0.001D;
   private static final double D2R = (Math.PI / 180D);
   private static final double[][] TURN = mul(rx(0.0D), mul(ry((-Math.PI / 2D)), rz(0.9599310885968813D)));
   private static final double[][] GRIP = mul(rx((-Math.PI / 2D)), ry(Math.PI));

   private BladeTrail() {
   }

   static double[][] rx(double a) {
      double c = Math.cos(a);
      double s = Math.sin(a);
      return new double[][]{{1.0D, 0.0D, 0.0D}, {0.0D, c, -s}, {0.0D, s, c}};
   }

   static double[][] ry(double a) {
      double c = Math.cos(a);
      double s = Math.sin(a);
      return new double[][]{{c, 0.0D, s}, {0.0D, 1.0D, 0.0D}, {-s, 0.0D, c}};
   }

   static double[][] rz(double a) {
      double c = Math.cos(a);
      double s = Math.sin(a);
      return new double[][]{{c, -s, 0.0D}, {s, c, 0.0D}, {0.0D, 0.0D, 1.0D}};
   }

   static double[][] mul(double[][] a, double[][] b) {
      double[][] o = new double[3][3];

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 3; ++j) {
            o[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
         }
      }

      return o;
   }

   public static boolean shown(int which, double ct) {
      return ct >= FROM[which] && ct <= HIT[which] + 7.0D;
   }

   public static float fade(int which, double ct) {
      return ct <= HIT[which] ? 1.0F : (float)Math.max(0.0D, 1.0D - (ct - HIT[which]) / 7.0D);
   }

   public static double cut(int which, double ct, double last) {
      return ct + Math.max(0.0D, HIT[which] - last);
   }

   public static float fade(int which, double ct, double last) {
      return fade(which, cut(which, ct, last));
   }

   public static float ramp(double s) {
      double x = Math.min(1.0D, Math.max(0.0D, s / 0.65D));
      return (float)Math.pow(x, 1.2D);
   }

   public static double[] along(double[] len) {
      int n = len.length - 1;
      double[] s = new double[n + 1];
      double tot = len[n];

      for(int i = 0; i <= n; ++i) {
         s[i] = tot > 1.0E-6D ? len[i] / tot : (double)i / (double)n;
      }

      return s;
   }

   public static int seg(double[] s, double x) {
      int n = s.length - 1;

      for(int i = 0; i < n; ++i) {
         if (s[i + 1] >= x) {
            return i;
         }
      }

      return n - 1;
   }

   public static double[] hand(MoveClips.Clip clip, double t, int flags) {
      double[] ch = new double[56];
      MoveRig.sample(clip, t, false, false, ch);
      float[] out = new float[36];
      MoveRig.solve(clip, ch, new float[36], flags, MoveRig.clipTime(clip, t, false), false, out);
      double[][] m = mul(mul(rz((double)out[17]), mul(ry((double)out[16]), rx((double)out[15]))), GRIP);
      return new double[]{(double)out[12], (double)out[13], (double)out[14], m[0][0], m[0][1], m[0][2], m[1][0], m[1][1], m[1][2], m[2][0], m[2][1], m[2][2]};
   }

   public static double[] texel(double[] h, double u, double v) {
      double x = 1.6D * (u / 32.0D * 16.0D - 8.0D);
      double y = 1.6D * ((1.0D - v / 32.0D) * 16.0D - 8.0D);
      double lx = 1.0D + TURN[0][0] * x + TURN[0][1] * y;
      double ly = 12.96D + TURN[1][0] * x + TURN[1][1] * y;
      double lz = -9.54D + TURN[2][0] * x + TURN[2][1] * y;
      double px = h[0] + h[3] * lx + h[4] * ly + h[5] * lz;
      double py = h[1] + h[6] * lx + h[7] * ly + h[8] * lz;
      double pz = h[2] + h[9] * lx + h[10] * ly + h[11] * lz;
      double k = 0.05859375D;
      return new double[]{-px * k, 1.4071874999999998D - py * k, -pz * k};
   }

   public static final class Strokes {
      private final int[] kind = new int[4];
      private final double[] start = new double[4];
      private final double[] last = new double[4];
      private int n;

      public void see(int which, double at, double ct) {
         if (BladeTrail.shown(which, ct)) {
            for(int i = 0; i < this.n; ++i) {
               if (this.kind[i] == which && Math.abs(this.start[i] - at) < 0.001D) {
                  this.last[i] = Math.max(this.last[i], ct);
                  return;
               }
            }

            if (this.n == 4) {
               this.drop(0);
            }

            this.kind[this.n] = which;
            this.start[this.n] = at;
            this.last[this.n] = ct;
            ++this.n;
         }
      }

      public int live(double t) {
         for(int i = this.n - 1; i >= 0; --i) {
            double ct = t - this.start[i];
            if (ct < this.last[i] - 0.001D || !BladeTrail.shown(this.kind[i], BladeTrail.cut(this.kind[i], ct, this.last[i]))) {
               this.drop(i);
            }
         }

         return this.n;
      }

      public int kind(int i) {
         return this.kind[i];
      }

      public double start(int i) {
         return this.start[i];
      }

      public double last(int i) {
         return this.last[i];
      }

      private void drop(int i) {
         for(int j = i; j + 1 < this.n; ++j) {
            this.kind[j] = this.kind[j + 1];
            this.start[j] = this.start[j + 1];
            this.last[j] = this.last[j + 1];
         }

         --this.n;
      }
   }
}
