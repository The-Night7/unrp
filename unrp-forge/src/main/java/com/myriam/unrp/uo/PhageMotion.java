package com.myriam.unrp.uo;



import net.minecraft.client.model.geom.ModelPart;

public final class PhageMotion {
   public static final int NONE = 0;
   public static final int DEVOUR = 1;
   public static final int BURROW = 2;
   public static final int ROAR = 3;
   public static final int LASH = 4;
   public static final int SLAM = 5;
   public static final int STOMP = 6;
   public static final double GROUND = 24.0D;
   public static final double SOLE = 2.0D;
   public static final double THIGH = (double)6.6F;
   public static final double SHIN = (double)5.9F;
   public static final double UPPER = (double)10.4F;
   public static final double FORE = (double)9.4F;
   public static final double STRIDE = 8.4D;
   public static final double DUTY = 0.62D;
   public static final double LIFT = 3.4D;
   public static final double FOOT_X = 11.2D;
   public static final double FOOT_Z = 1.4D;
   public static final double DEPTH = 42.0D;
   public static final double WORLD = 2.5D;
   public static final double CYCLE_DEVOUR = 4.2D;
   public static final double CYCLE_LASH = 3.4D;
   public static final double CYCLE_WALK = 1.7D;
   public static final double TURN = 7.033185307179586D;
   public static final double BITE_GRAB = 0.28D;
   public static final double BITE_LIFT = 0.42D;
   public static final double BITE_BITE = 0.485D;
   public static final double BITE_CHOMP = 0.505D;
   public static final double[] PRESENT = new double[]{-9.2D, 5.0D, -14.6D};
   public static final double[] DEVOUR_AT = new double[]{-25.0D, 24.0D, -18.0D};
   public static final double[] CROWN_LIFT = new double[]{0.55D, 0.42D, 0.3D, 0.42D, 0.55D};
   public static final double[] CROWN_CURL = new double[]{0.075D, 0.09D, 0.1D, 0.09D, 0.075D};
   public static final double[] CROWN_RAMP = new double[]{0.012D, 0.014D, 0.016D, 0.014D, 0.012D};
   public static final double[] CROWN_PLANE = new double[]{0.628D, 0.314D, 0.0D, -0.314D, -0.628D};
   public static final double[] CROWN_FLEX = new double[]{-0.06D, -0.045D, 0.0D, 0.045D, 0.06D};
   public static final double[] CROWN_FREQ = new double[]{1.0D, 1.13D, 0.94D, 1.07D, 0.97D};
   public static final double[] CROWN_PHASE = new double[]{0.0D, 1.9D, 3.7D, 5.3D, 0.9D};
   public static final double[][] FIN_AIM = new double[][]{{-0.55D, 0.45D, 0.7D}, {-0.35D, 0.05D, 0.94D}, {0.0D, 0.3D, 0.95D}, {0.35D, 0.05D, 0.94D}, {0.55D, 0.45D, 0.7D}};
   public static final double FIN_CURL = -0.05D;
   public static final double[] WHEEL_YAW = new double[]{-1.45D, -0.55D, 0.0D, 0.55D, 1.45D};
   public static final double[] WHEEL_DROOP = new double[]{0.15D, -0.09D, 0.12D, -0.09D, 0.15D};
   public static final double[] WHEEL_SAG = new double[]{0.004D, 0.024D, 0.016D, 0.024D, 0.004D};
   public static final double[] FLARE = new double[]{0.22D, 0.18D, 0.15D, 0.18D, 0.22D};
   private static final String[] LEGS = new String[]{"hleg_l", "hleg_r"};
   private static final String[] ARMS = new String[]{"arm_l", "arm_r"};
   private static final double[] SIDE = new double[]{1.0D, -1.0D};
   private static final double[] GAIT = new double[]{0.0D, 0.5D};
   private final PhageModel m;
   private final int[] legHip = new int[2];
   private final int[] legKnee = new int[2];
   private final int[] legPaw = new int[2];
   private final int[] armUp = new int[2];
   private final int[] armFore = new int[2];
   private final int[] armHand = new int[2];
   private final int[][] armClaw = new int[2][3];
   private final int[] armThumb = new int[2];
   private final int iPhage;
   private final int iPelvis;
   private final int iTorso;
   private final int iBelly;
   private final int iNeck;
   private final int iHead;
   private final int iJaw;
   private final int[] tailRoot = new int[5];
   private final int[] tailBase = new int[5];
   private final int[][] tailSeg = new int[5][9];
   private final int[] tailTip = new int[5];
   private final int[][] tailClaw = new int[5][3];
   private final int[][] tailClawTip = new int[5][3];
   private double[] feedAt;
   private boolean feeding;
   private double[] present = new double[]{0.0D, 0.0D, 0.0D};
   private double[] grip = new double[]{0.0D, 0.0D, 0.0D};
   private double[] mouth = new double[]{0.0D, 0.0D, 0.0D};
   private double speed = 1.0D;

   public PhageMotion(PhageModel model) {
      this.m = model;
      this.iPhage = model.id("phage");
      this.iPelvis = model.id("pelvis");
      this.iTorso = model.id("torso");
      this.iBelly = model.id("belly");
      this.iNeck = model.id("neck");
      this.iHead = model.id("head");
      this.iJaw = model.id("jaw");

      for(int n = 0; n < 2; ++n) {
         this.legHip[n] = model.id(LEGS[n]);
         this.legKnee[n] = model.id(LEGS[n] + "_knee");
         this.legPaw[n] = model.id(LEGS[n] + "_paw");
         this.armUp[n] = model.id(ARMS[n]);
         this.armFore[n] = model.id(ARMS[n] + "_fore");
         this.armHand[n] = model.id(ARMS[n] + "_hand");

         for(int i = 0; i < 3; ++i) {
            this.armClaw[n][i] = model.id(ARMS[n] + "_c" + i);
         }

         this.armThumb[n] = model.id(ARMS[n] + "_thumb");
      }

      for(int i = 0; i < 5; ++i) {
         this.tailRoot[i] = model.id("tailroot" + i);
         this.tailBase[i] = model.id("t" + i + "_base");

         for(int s = 0; s < 9; ++s) {
            this.tailSeg[i][s] = model.id("t" + i + "_seg" + s);
         }

         this.tailTip[i] = model.id("t" + i + "_tip");

         for(int c = 0; c < 3; ++c) {
            this.tailClaw[i][c] = model.id("t" + i + "_claw" + c);
            this.tailClawTip[i][c] = model.id("t" + i + "_clawt" + c);
         }
      }

   }

   public PhageModel model() {
      return this.m;
   }

   public double[] grip() {
      return (double[])this.grip.clone();
   }

   public double[] mouth() {
      return (double[])this.mouth.clone();
   }

   public double[] present() {
      return (double[])this.present.clone();
   }

   public double speed() {
      return this.speed;
   }

   private static double clamp(double v, double a, double b) {
      return SarcMath.clamp(v, a, b);
   }

   private static double env(double c, double a, double b) {
      return SarcMath.env(c, a, b);
   }

   private static double smooth(double x) {
      return SarcMath.smooth(x);
   }

   private static double smoother(double x) {
      return SarcMath.smoother(x);
   }

   private static double outCubic(double x) {
      return SarcMath.outCubic(x);
   }

   private static double inCubic(double x) {
      return SarcMath.inCubic(x);
   }

   private static double ring(double x, double f, double d) {
      return SarcMath.ring(x, f, d);
   }

   private static double bump(double c, double a, double b) {
      return SarcMath.bump(c, a, b);
   }

   private static double lerp(double a, double b, double k) {
      return SarcMath.lerp(a, b, k);
   }

   private static double frac(double x) {
      return SarcMath.frac(x);
   }

   private ModelPart p(int i) {
      return this.m.parts[i];
   }

   private double[] local(int i) {
      ModelPart q = this.p(i);
      return SarcMath.compose((double)q.x, (double)q.y, (double)q.z, (double)q.xRot, (double)q.yRot, (double)q.zRot, (double)q.xScale, (double)q.yScale, (double)q.zScale);
   }

   public double[] frame(int i) {
      double[] out = this.local(i);

      for(int k = this.m.parent(i); k >= 0; k = this.m.parent(k)) {
         out = SarcMath.mul(this.local(k), out);
      }

      return out;
   }

   private double[] quat(int i) {
      ModelPart q = this.p(i);
      return SarcMath.qEuler((double)q.xRot, (double)q.yRot, (double)q.zRot);
   }

   private void setQuat(int i, double[] q) {
      double[] e = SarcMath.eulerOf(SarcMath.qNorm(q));
      ModelPart pt = this.p(i);
      pt.xRot = (float)e[0];
      pt.yRot = (float)e[1];
      pt.zRot = (float)e[2];
   }

   private void setRot(int i, double x, double y, double z) {
      ModelPart pt = this.p(i);
      pt.xRot = (float)x;
      pt.yRot = (float)y;
      pt.zRot = (float)z;
   }

   private void defaults(PhageMotion.Vars v, double t, double still) {
      double breath = Math.sin(t * 1.2D);
      v.torsoS = 1.0D + breath * 0.012D * still;
      v.bellyY = breath * 0.3D * still;
      v.bellyS = 1.0D;
      v.neckX = breath * 0.02D * still;
      v.jaw = 0.07D + (1.0D - breath) * 0.02D;
      v.neckS = 1.0D;
      v.tlAmp = 1.0D;
      v.tlSpeed = 1.0D;

      for(int n = 0; n < 2; ++n) {
         v.foot[n][0] = SIDE[n] * 11.2D;
         v.foot[n][1] = 24.0D;
         v.foot[n][2] = 1.4D;
         v.arm[n][0] = 0.22D;
         v.arm[n][1] = 0.38D;
         v.arm[n][3] = 0.4D;
         v.arm[n][4] = 0.14D;
         v.arm[n][6] = 0.15D;
      }

   }

   private static double lookY(double t, double still) {
      double look = t * 0.29D;
      return (0.2D * Math.sin(look) + 0.06D * Math.sin(look * 2.7D + 1.0D)) * still;
   }

   private void idle(PhageMotion.Vars v, double t, double still) {
      double sway = Math.sin(t * 0.53D);
      double breath = Math.sin(t * 1.2D);
      v.rtX = 0.7D * sway * still;
      v.rtY = 0.3D * (1.0D - breath) * still;
      v.pvRoll = -0.025D * sway * still;
      v.torRoll = 0.035D * sway * still;
      v.torYaw = 0.04D * Math.sin(t * 0.37D + 1.2D) * still;
      double look = t * 0.29D;
      v.neckY = lookY(t, still);
      v.headY = 0.05D * Math.sin(look * 1.3D + 0.7D) * still;
      v.headZ = -0.03D * sway * still;

      for(int n = 0; n < 2; ++n) {
         double s = SIDE[n];
         double[] a = v.arm[n];
         double ph = t * 0.53D + (s > 0.0D ? 0.0D : 1.3D);
         a[0] += 0.05D * Math.sin(ph - 0.6D) * still;
         a[1] += 0.02D * s * sway * still;
         a[3] += 0.05D * Math.sin(ph + 0.4D) * still;
         a[6] = 0.12D + 0.3D * Math.max(0.0D, Math.sin(t * 0.9D + s * 1.7D)) * still;
      }

      double sn = frac(t / 5.6D);
      double snarl = smooth(env(sn, 0.6D, 0.65D)) * (1.0D - smooth(env(sn, 0.73D, 0.83D)));
      v.jaw += snarl * (0.42D + 0.03D * Math.sin(t * 31.0D));
      v.neckX -= snarl * 0.12D;
      v.headX -= snarl * 0.1D;
      v.neckY *= 1.0D - snarl * 0.6D;
      v.torPitch += snarl * 0.04D;

      for(int n = 0; n < 2; ++n) {
         double[] a = v.arm[n];
         a[6] += snarl * 0.55D;
         a[1] += snarl * 0.06D;
         a[3] += snarl * 0.15D;
      }

      v.tlLift += snarl * 0.08D;
      v.tlAmp += snarl * 0.6D;
      v.tlFlare += snarl * 0.18D;
   }

   public static double[] gaitFoot(double u) {
      if (u < 0.62D) {
         return new double[]{-4.2D + 8.4D * (u / 0.62D), 0.0D, 0.0D, 1.0D};
      } else {
         double w = (u - 0.62D) / 0.38D;
         double lift = 3.4D * Math.pow(Math.sin(Math.PI * w), 0.8D);
         double pitch = 0.5D * Math.sin(Math.PI * Math.min(1.0D, w * 1.35D)) + 0.32D * w * Math.sin((Math.PI * 2D) * w);
         return new double[]{4.2D - 8.4D * smoother(w), lift, pitch, 0.0D};
      }
   }

   private void walk(PhageMotion.Vars v, double t, double g, double still) {
      double ph = (Math.PI * 2D) * g;

      for(int n = 0; n < 2; ++n) {
         double[] f = gaitFoot(frac(g - GAIT[n]));
         v.foot[n][2] += f[0];
         v.foot[n][1] -= f[1];
         v.foot[n][0] += SIDE[n] * 1.8D * (f[1] / 3.4D);
         v.foot[n][3] = f[2];
      }

      double load = Math.cos(ph - 1.9477874452256718D);
      double dip = 0.5D + 0.5D * Math.cos(2.0D * ph - 1.0053096491487339D);
      v.rtY = 1.2D * dip;
      v.rtX = 1.4D * load;
      v.rtPitch = 0.012D * Math.cos(2.0D * ph - 0.9D);
      v.pvRoll = -0.04D * load;
      v.pvYaw = 0.07D * Math.cos(ph);
      v.torYaw = -0.15D * Math.cos(ph);
      v.torRoll = 0.075D * load;
      v.torPitch = 0.09D + 0.03D * dip;
      v.neckY = -0.6D * (v.pvYaw + v.torYaw) + lookY(t, still) * 0.3D;
      v.headZ = -0.6D * (v.pvRoll + v.torRoll);
      v.neckX += -0.05D * dip + 0.03D;
      v.headX += 0.03D * Math.sin(2.0D * ph - 1.4D);
      v.bellyY += 0.7D * Math.sin(2.0D * ph - 1.0053096491487339D - 1.2D);

      for(int n = 0; n < 2; ++n) {
         double s = SIDE[n];
         double[] a = v.arm[n];
         double sw = -s * Math.cos(ph);
         a[0] = 0.22D + 0.4D * sw;
         a[1] = 0.42D + 0.03D * sw;
         a[3] = 0.45D + 0.3D * Math.max(0.0D, sw) + 0.06D * Math.sin(2.0D * ph);
         a[4] = 0.12D - 0.22D * s * Math.sin(ph);
         a[6] = 0.2D;
      }

      v.tlAmp = 1.2D;
      v.tlLag = 0.06D * Math.sin(ph - 0.6D);
      v.tlSpeed = 1.2D;
   }

   private static double[] step(double c, double c0, double c1, double a, double b, double h) {
      double k = env(c, c0, c1);
      return new double[]{lerp(a, b, smoother(k)), h * Math.sin(Math.PI * k)};
   }

   private static PhageMotion.Reach reach(double k) {
      PhageMotion.Reach r = new PhageMotion.Reach();
      r.k = k;
      return r;
   }

   private void devour(PhageMotion.Vars v, double c, double t) {
      double stalk = smooth(env(c, 0.0D, 0.15D)) * (1.0D - smooth(env(c, 0.16D, 0.22D)));
      double bow = smoother(env(c, 0.13D, 0.25D)) * (1.0D - smoother(env(c, 0.29D, 0.42D)));
      double rise = smoother(env(c, 0.29D, 0.42D)) * (1.0D - smoother(env(c, 0.6D, 0.76D)));
      double lunge = smooth(env(c, 0.45D, 0.5D)) * (1.0D - smooth(env(c, 0.515D, 0.6D)));
      double chompK = env(c, 0.493D, 0.505D);
      double chew = env(c, 0.505D, 0.64D);
      double gulp = bump(c, 0.66D, 0.8D);
      v.rtZ = 1.4D * stalk - 7.0D * bow - 3.0D * rise - 1.5D * lunge;
      v.rtY = 1.6D * stalk + 3.0D * bow + 0.5D * rise + 0.4D * gulp;
      v.pvPitch = 0.07D * bow;
      v.torPitch = 0.12D * stalk + 0.32D * bow - 0.06D * rise + 0.03D * lunge;
      v.torYaw = -0.08D * stalk - 0.16D * bow + 0.1D * rise - 0.14D * lunge;
      v.neckX += 0.15D * stalk - 0.18D * bow + 0.04D * rise - 0.04D * lunge + 0.12D * gulp;
      v.headX += 0.06D * stalk - 0.22D * bow + 0.1D * lunge;
      v.neckY = 0.32D * rise + 0.3D * bow + 0.42D * lunge;
      v.headZ = 0.14D * rise;
      double open = smooth(env(c, 0.04D, 0.22D)) * (1.0D - smooth(env(c, 0.24D, 0.32D)));
      double wide = smooth(env(c, 0.38D, 0.485D)) * (1.0D - inCubic(chompK));
      double chews = c > 0.505D && c < 0.64D ? Math.abs(Math.sin((c - 0.505D) / 0.135D * Math.PI * 3.0D)) * (1.0D - chew * 0.5D) : 0.0D;
      v.jaw = 0.07D + 0.32D * open + 0.98D * wide + 0.24D * chews + 0.05D * stalk * Math.sin(t * 28.0D) + ring(c - 0.505D, 44.0D, 16.0D) * 0.08D;
      if (c > 0.505D && c < 0.64D) {
         double th = Math.exp(-chew * 2.5D);
         v.neckY += 0.12D * Math.sin(chew * Math.PI * 2.0D * 2.5D) * th;
         v.headY = -0.07D * Math.sin(chew * Math.PI * 2.0D * 2.5D - 0.6D) * th;
      }

      v.neckS = 1.0D + 0.14D * bump(c, 0.66D, 0.74D);
      v.bellyS = 1.0D + 0.07D * bump(c, 0.7D, 0.86D);
      v.torsoS += 0.025D * bump(c, 0.7D, 0.86D);
      double[] fs = step(c, 0.14D, 0.24D, 0.0D, -3.0D, 3.0D);
      double[] fb = step(c, 0.62D, 0.74D, 0.0D, 3.0D, 2.4D);
      double spreadK = (fs[0] + fb[0]) / -3.0D;
      v.foot[1][2] += fs[0] + fb[0];
      v.foot[1][1] -= fs[1] + fb[1];
      v.foot[1][0] -= 4.5D * spreadK;
      v.foot[0][0] += 3.5D * smoother(env(c, 0.18D, 0.27D)) * (1.0D - smoother(env(c, 0.7D, 0.8D)));
      double[] ls = step(c, 0.19D, 0.28D, 0.0D, -2.0D, 1.6D);
      double[] lb = step(c, 0.7D, 0.8D, 0.0D, 2.0D, 1.4D);
      v.foot[0][2] += ls[0] + lb[0];
      v.foot[0][1] -= ls[1] + lb[1];
      double[] al = v.arm[0];
      al[0] = 0.22D - 0.3D * bow + 0.1D * rise;
      al[1] = 0.4D + 0.3D * bow + 0.1D * rise;
      al[3] = 0.4D + 0.25D * rise;
      al[6] = 0.15D + 0.5D * stalk + 0.4D * lunge;
      double[] ar = v.arm[1];
      ar[0] = 0.22D - 0.3D * stalk;
      ar[1] = 0.4D + 0.15D * stalk;
      ar[3] = 0.4D + 0.55D * stalk;
      ar[6] = 0.15D - 0.9D * stalk;
      ar[7] = stalk;
      double reachK = smoother(env(c, 0.13D, 0.24D)) * (1.0D - smoother(env(c, 0.52D, 0.64D)));
      if (reachK > 0.0D) {
         double liftK = smoother(env(c, 0.29D, 0.42D));
         PhageMotion.Reach r = reach(reachK);
         r.pModel = SarcMath.lerp3(new double[]{-1.0D, -0.1D, 0.4D}, new double[]{-1.0D, 0.3D, 0.3D}, liftK);
         r.handQ = SarcMath.qSlerp(SarcMath.qEuler(-0.35D, 0.3D, 0.0D), SarcMath.basisQ(new double[]{0.75D, 0.1D, -0.65D}, new double[]{0.0D, 1.0D, 0.0D}), liftK);
         r.tModel = new double[]{DEVOUR_AT[0], 16.8D, DEVOUR_AT[2]};
         r.feed = liftK;
         r.cycle = c;
         v.reach[1] = r;
         double closed = smooth(env(c, 0.26D, 0.28D));
         double opened = smooth(env(c, 0.505D, 0.535D));
         ar[6] = lerp(-0.9D, 1.0D, closed) * (1.0D - opened) + -0.5D * opened;
         ar[7] = 1.0D - closed + opened * 0.8D;
      }

      v.tlLift += 0.1D * stalk + 0.06D * bow;
      v.tlAmp = 1.0D + 0.8D * bow + 0.4D * stalk;
      v.tlSpeed = 1.0D + 0.6D * stalk;
      v.tlFlare = 0.3D * wide;
   }

   private void burrow(PhageMotion.Vars v, double cb, double t, double swim) {
      double rise = bump(cb, 0.0D, 0.16D);
      double dive = smooth(env(cb, 0.11D, 0.27D));
      v.tlFin = smooth(env(cb, 0.12D, 0.26D)) * (1.0D - smooth(env(cb, 0.68D, 0.72D)));
      if (cb < 0.27D) {
         double lean = smoother(env(cb, 0.11D, 0.25D));
         v.rtY = -2.0D * rise + 42.0D * inCubic(dive);
         v.rtPitch = 1.45D * lean;
         v.rtZ = -14.0D * dive;
         v.torPitch = -0.18D * rise;
         v.neckX += -0.3D * rise + 0.25D * lean;
         v.jaw = 0.07D + 0.4D * rise + 0.5D * dive;
         double up = smooth(env(cb, 0.02D, 0.12D));

         for(int n = 0; n < 2; ++n) {
            double s_0 = SIDE[n];
            PhageMotion.Reach r_0 = reach(up);
            r_0.torsoK = 1.0D;
            r_0.tTorso = new double[]{s_0 * 12.0D, -40.0D, -6.0D};
            r_0.pTorso = new double[]{s_0 * 0.3D, 0.0D, 1.0D};
            r_0.wrist = -0.2D;
            v.reach[n] = r_0;
            v.arm[n][6] = lerp(0.15D, -0.3D, up);
            v.arm[n][7] = up * 0.6D;
         }

         v.tlFlare = 0.4D * rise * (1.0D - v.tlFin);
         v.air = smooth(env(cb, 0.15D, 0.24D));
         v.trail = true;
      } else if (cb < 0.69D) {
         if (swim >= 0.0D) {
            v.rtX = 0.0D;
            v.rtZ = -14.0D * (1.0D - smooth(Math.min(1.0D, swim / 0.5D)));
            v.rtYaw = 0.0D;
            v.rtRoll = 0.12D * Math.sin(swim * Math.PI * 1.6D);
         } else {
            double u = env(cb, 0.27D, 0.69D);
            double ax = 10.0D;
            double az = 36.0D;
            double px = Math.sin(u * Math.PI * 2.0D) * ax;
            double pz = -14.0D + 14.0D * u - Math.sin(u * Math.PI) * az;
            double dxu = (Math.PI * 2D) * ax * Math.cos(u * Math.PI * 2.0D);
            double dzu = 14.0D - Math.PI * az * Math.cos(u * Math.PI);
            v.rtX = px;
            v.rtZ = pz;
            v.rtYaw = Math.atan2(-dxu, -dzu);
            v.rtRoll = 0.12D * Math.sin(u * Math.PI * 4.0D);
         }

         v.rtY = 42.0D;
         v.rtPitch = 1.45D;

         for(int n = 0; n < 2; ++n) {
            double s = SIDE[n];
            double pad = Math.sin(t * 6.0D + (s > 0.0D ? 0.0D : Math.PI));
            PhageMotion.Reach r = reach(1.0D);
            r.torsoK = 1.0D;
            r.tTorso = new double[]{s * (13.0D + 3.0D * pad), -39.0D, -6.0D + 4.0D * pad};
            r.pTorso = new double[]{s * 0.3D, 0.0D, 1.0D};
            r.wrist = -0.2D;
            v.reach[n] = r;
            v.arm[n][6] = -0.3D;
            v.arm[n][7] = 0.6D;
         }

         v.air = 1.0D;
         v.trail = true;
      } else if (cb < 0.85D) {
         double rs = env(cb, 0.69D, 0.765D);
         double fl = env(cb, 0.765D, 0.85D);
         double r1 = outCubic(rs);
         v.rtY = cb < 0.765D ? lerp(42.0D, -16.0D, r1) : lerp(-16.0D, 0.0D, inCubic(fl));
         v.rtPitch = cb < 0.765D ? lerp(1.45D, -0.32D, r1) : lerp(-0.32D, 0.1D, fl);
         v.torPitch = -0.15D * (1.0D - fl);
         v.neckX += cb < 0.765D ? -0.35D * r1 : -0.35D * (1.0D - fl);
         v.jaw = 0.07D + 1.0D * Math.sin(Math.PI * env(cb, 0.69D, 0.85D));
         double spreadK = smooth(env(cb, 0.7D, 0.76D));

         for(int n = 0; n < 2; ++n) {
            double s = SIDE[n];
            double[] wideAt = new double[]{s * 40.0D, -24.0D + 10.0D * fl, -4.0D - 6.0D * fl};
            PhageMotion.Reach r = reach(1.0D);
            r.torsoK = 1.0D;
            r.tTorso = SarcMath.lerp3(new double[]{s * 12.0D, -40.0D, -6.0D}, wideAt, spreadK);
            r.pTorso = SarcMath.lerp3(new double[]{s * 0.3D, 0.0D, 1.0D}, new double[]{0.0D, 0.4D, 1.0D}, spreadK);
            r.wrist = -0.3D;
            v.reach[n] = r;
            v.arm[n][6] = -0.5D;
            v.arm[n][7] = 1.0D;
         }

         v.tlFlare = spreadK * (1.0D - 0.6D * fl);
         v.tlAmp = 2.0D;
         v.air = 1.0D;
      } else {
         double land = env(cb, 0.85D, 1.0D);
         double squash = Math.exp(-land * 6.0D) * Math.cos(land * 11.0D);
         v.rtY = 2.4D * Math.max(0.0D, squash) + 0.5D * (1.0D - smooth(land));
         v.rtPitch = 0.08D * (1.0D - smooth(env(cb, 0.85D, 0.95D)));
         v.torPitch = 0.18D * (1.0D - smooth(env(cb, 0.86D, 0.97D)));

         for(int n = 0; n < 2; ++n) {
            v.foot[n][0] += SIDE[n] * 3.0D * (1.0D - smooth(env(cb, 0.9D, 1.0D)));
         }

         v.neckX += 0.22D * Math.exp(-land * 6.0D) * Math.cos(land * 12.0D);
         v.bellyY += 2.2D * ring(cb - 0.85D, 26.0D, 8.0D);
         double slam = 1.0D - smooth(env(cb, 0.88D, 0.98D));

         for(int n = 0; n < 2; ++n) {
            double s = SIDE[n];
            PhageMotion.Reach r = reach(slam);
            r.tModel = new double[]{s * 24.0D, 22.6D, -14.0D};
            r.pModel = new double[]{s * 0.9D, -0.4D, 0.3D};
            r.handQ = SarcMath.qEuler(-0.15D, 0.0D, s * 0.25D);
            v.reach[n] = r;
            v.arm[n][6] = 0.3D + 0.4D * (1.0D - slam);
            v.arm[n][7] = slam * 0.6D;
         }

         v.tlAmp = 1.4D;
         v.tlFlare = 0.3D * Math.exp(-land * 5.0D);
      }

   }

   private void roar(PhageMotion.Vars v, double cr, double t) {
      double inh = smooth(env(cr, 0.0D, 0.28D)) * (1.0D - smooth(env(cr, 0.28D, 0.33D)));
      double blast = outCubic(env(cr, 0.29D, 0.35D)) * (1.0D - smooth(env(cr, 0.82D, 0.95D)));
      v.torsoS += inh * 0.045D;
      v.bellyS = 1.0D + inh * 0.04D;
      v.rtY = -1.8D * inh + 1.4D * blast;
      v.rtZ = 1.0D * inh - 3.0D * blast + ring(cr - 0.3D, 20.0D, 7.0D) * 1.4D;
      v.pvPitch = -0.06D * inh + 0.05D * blast;
      v.torPitch = -0.3D * inh + 0.3D * blast + 0.04D * ring(cr - 0.3D, 18.0D, 6.0D);
      v.neckX += -0.4D * inh + 0.08D * blast;
      v.headX += -0.18D * inh + 0.06D * blast;
      v.jaw = 0.07D + 0.16D * inh + blast * (0.92D + 0.04D * Math.sin(t * 41.0D));
      v.neckY = Math.sin((cr - 0.3D) * 6.0D) * 0.14D * blast;
      v.headY = -v.neckY * 0.3D;
      double armK = smooth(env(cr, 0.0D, 0.14D)) * (1.0D - smooth(env(cr, 0.84D, 0.98D)));
      double throwK = outCubic(env(cr, 0.29D, 0.36D));

      for(int n = 0; n < 2; ++n) {
         double s = SIDE[n];
         double quake = 0.6D * Math.sin(t * 37.0D + s) * blast;
         PhageMotion.Reach r = reach(armK);
         r.torsoK = 1.0D;
         r.tTorso = SarcMath.lerp3(new double[]{s * 34.0D, -32.0D, 3.0D}, new double[]{s * 33.0D, -12.0D + quake, -19.0D}, throwK);
         r.pTorso = SarcMath.lerp3(new double[]{0.0D, 0.3D, 1.0D}, new double[]{s * 0.2D, 1.0D, 0.5D}, throwK);
         r.wrist = -0.25D * blast;
         v.reach[n] = r;
         v.arm[n][6] = 0.15D + 0.65D * inh - 0.95D * blast;
         v.arm[n][7] = blast;
      }

      double[] brace = step(cr, 0.29D, 0.36D, 0.0D, 2.2D, 1.4D);
      v.foot[0][2] += brace[0] * 0.3D;
      v.foot[1][2] += brace[0];
      v.foot[1][1] -= brace[1];
      v.tlLift += 0.1D * inh;
      v.tlCurl += 0.03D * inh;
      v.tlFlare = smoother(env(cr, 0.29D, 0.4D)) * (1.0D - smooth(env(cr, 0.8D, 0.97D)));
      v.tlTremor = blast;
      v.tlAmp = 1.0D + 0.5D * inh;
   }

   public static double lashYaw(double cl) {
      double wind = smooth(env(cl, 0.0D, 0.24D));
      double spin = smoother(env(cl, 0.24D, 0.62D));
      double settle = smooth(env(cl, 0.62D, 0.86D));
      double y = -0.75D * wind * (1.0D - spin) + spin * 7.033185307179586D;
      if (cl >= 0.62D) {
         y = 7.033185307179586D - 0.75D * settle - (Math.PI * 2D);
      }

      return y + ring(cl - 0.62D, 15.0D, 6.0D) * 0.07D;
   }

   public static double lashSpeed(double cl) {
      return (lashYaw(cl + 0.004D) - lashYaw(cl - 0.004D)) / 0.008D;
   }

   private void lash(PhageMotion.Vars v, double cl) {
      double wind = smooth(env(cl, 0.0D, 0.24D));
      double spin = smoother(env(cl, 0.24D, 0.62D));
      v.rtYaw = lashYaw(cl);
      double crouch = smooth(env(cl, 0.02D, 0.22D)) * (1.0D - smooth(env(cl, 0.62D, 0.86D)));
      v.rtY = 0.9D * crouch + 0.3D * bump(cl, 0.24D, 0.62D);
      v.torPitch = 0.14D * crouch;
      v.torYaw = -0.28D * wind * (1.0D - spin) + 0.22D * bump(cl, 0.24D, 0.5D) - 0.12D * ring(cl - 0.62D, 12.0D, 5.0D);
      v.rtRoll = -0.05D * Math.sin(Math.PI * spin);
      double flat = smooth(env(cl, 0.15D, 0.3D)) * (1.0D - smooth(env(cl, 0.66D, 0.92D)));
      v.tlWheel = flat;
      double spd = lashSpeed(cl) / 30.0D;
      v.tlWhip = clamp(-0.05D * spd, -0.06D, 0.06D) + 0.035D * wind * (1.0D - spin) + 0.05D * ring(cl - 0.6D, 11.0D, 4.5D);
      v.tlAmp = 1.0D + 0.6D * wind;
      v.jaw = 0.07D + 0.45D * wind * (1.0D - spin) + 0.25D * bump(cl, 0.26D, 0.62D);
      v.neckY = 0.25D * wind * (1.0D - spin) - 0.15D * bump(cl, 0.24D, 0.62D);
      double balance = smooth(env(cl, 0.08D, 0.26D)) * (1.0D - smooth(env(cl, 0.62D, 0.86D)));
      boolean spinning = cl > 0.26D && cl < 0.6D;

      for(int n = 0; n < 2; ++n) {
         double s = SIDE[n];
         double lead = s * wind * (1.0D - spin);
         PhageMotion.Reach r = reach(balance);
         r.torsoK = 1.0D;
         r.tTorso = new double[]{s * 30.0D, -8.0D + 3.0D * bump(cl, 0.3D, 0.6D), -18.0D + 6.0D * lead};
         r.pTorso = new double[]{s * 0.3D, 0.8D, 0.6D};
         r.wrist = 0.1D;
         v.reach[n] = r;
         v.arm[n][6] = 0.15D + 0.6D * balance;
      }

      double qs = 0.95D;

      for(int n = 0; n < 2; ++n) {
         double phs = (double)n * 0.5D;
         double y0 = v.rtYaw / qs + phs;
         double kk = clamp((frac(y0) - 0.42D) / 0.58D, 0.0D, 1.0D);
         double stepped = (Math.floor(y0) + smooth(kk) - phs) * qs;
         double lift = Math.sin(Math.PI * kk) * 1.5D * (spinning ? 1.0D : 0.6D);
         double x0 = SIDE[n] * (11.2D + 3.0D * crouch);
         double z0 = 1.4D;
         double cs = Math.cos(stepped);
         double ss = Math.sin(stepped);
         v.foot[n][0] = v.rtX + x0 * cs + z0 * ss;
         v.foot[n][2] = v.rtZ - x0 * ss + z0 * cs;
         v.foot[n][1] = 24.0D - lift;
         v.foot[n][4] = stepped;
         v.world[n] = true;
      }

   }

   private void slam(PhageMotion.Vars v, double c, double t) {
      double raise = smooth(env(c, 0.0D, 0.35D)) * (1.0D - smooth(env(c, 0.36D, 0.46D)));
      double down = smooth(env(c, 0.36D, 0.46D)) * (1.0D - smooth(env(c, 0.72D, 1.0D)));
      double hit = ring(c - 0.46D, 26.0D, 9.0D);
      v.rtY = -1.6D * raise + 2.2D * down + 1.2D * hit;
      v.rtZ = 1.0D * raise - 2.0D * down;
      v.torPitch = -0.26D * raise + 0.34D * down;
      v.neckX += -0.3D * raise + 0.12D * down;
      v.jaw = 0.07D + 0.5D * raise + 0.65D * down * (1.0D - env(c, 0.6D, 1.0D));
      double reachK = Math.max(raise, down);

      for(int n = 0; n < 2; ++n) {
         double s = SIDE[n];
         PhageMotion.Reach r = reach(reachK);
         r.torsoK = raise + down > 1.0E-6D ? raise / (raise + down) : 1.0D;
         r.tTorso = new double[]{s * 13.0D, -40.0D, -5.0D};
         r.pTorso = new double[]{s * 0.3D, 0.0D, 1.0D};
         r.tModel = new double[]{s * 24.0D, 22.6D, -14.0D};
         r.pModel = new double[]{s * 0.9D, -0.4D, 0.3D};
         r.wrist = -0.25D;
         v.reach[n] = r;
         v.arm[n][6] = 0.4D * down - 0.3D * raise;
         v.arm[n][7] = 0.6D * raise;
         v.foot[n][0] += SIDE[n] * 2.4D * down;
      }

      v.tlFlare = 0.7D * down + 0.2D * raise;
      v.tlAmp = 1.0D + raise;
      v.tlTremor = 0.6D * down * (1.0D - env(c, 0.5D, 0.8D));
   }

   private void stomp(PhageMotion.Vars v, double c) {
      double up = Math.sin(Math.PI * env(c, 0.05D, 0.4D));
      double hit = c > 0.4D ? ring(c - 0.4D, 30.0D, 8.0D) : 0.0D;
      double after = bump(c, 0.38D, 0.95D);
      v.foot[1][1] -= 4.2D * up;
      v.foot[1][2] -= 1.5D * up;
      v.foot[1][0] -= 1.5D * after;
      v.rtY = -0.8D * up + 1.8D * Math.max(0.0D, hit) + 0.6D * after;
      v.rtX = 1.2D * up;
      v.pvRoll = 0.06D * up;
      v.torPitch = 0.08D * up + 0.16D * after;
      v.neckX += 0.1D * after;
      v.jaw = 0.07D + 0.45D * after;

      for(int n = 0; n < 2; ++n) {
         double[] a = v.arm[n];
         a[0] = 0.22D - 0.2D * after;
         a[1] = 0.38D + 0.45D * after + 0.1D * up;
         a[3] = 0.4D - 0.15D * after;
         a[6] = 0.15D - 0.6D * after;
         a[7] = after;
      }

      v.tlFlare = 0.45D * after;
      v.tlTremor = 0.4D * Math.max(0.0D, hit);
   }

   private static PhageMotion.Vars blend(PhageMotion.Vars a, PhageMotion.Vars b, double k) {
      if (k <= 0.0D) {
         return a;
      } else if (k >= 1.0D) {
         return b;
      } else {
         PhageMotion.Vars o = new PhageMotion.Vars();
         o.rtX = lerp(a.rtX, b.rtX, k);
         o.rtY = lerp(a.rtY, b.rtY, k);
         o.rtZ = lerp(a.rtZ, b.rtZ, k);
         o.rtPitch = lerp(a.rtPitch, b.rtPitch, k);
         o.rtYaw = lerp(a.rtYaw, b.rtYaw, k);
         o.rtRoll = lerp(a.rtRoll, b.rtRoll, k);
         o.pvPitch = lerp(a.pvPitch, b.pvPitch, k);
         o.pvYaw = lerp(a.pvYaw, b.pvYaw, k);
         o.pvRoll = lerp(a.pvRoll, b.pvRoll, k);
         o.torPitch = lerp(a.torPitch, b.torPitch, k);
         o.torYaw = lerp(a.torYaw, b.torYaw, k);
         o.torRoll = lerp(a.torRoll, b.torRoll, k);
         o.torsoS = lerp(a.torsoS, b.torsoS, k);
         o.bellyY = lerp(a.bellyY, b.bellyY, k);
         o.bellyS = lerp(a.bellyS, b.bellyS, k);
         o.neckX = lerp(a.neckX, b.neckX, k);
         o.neckY = lerp(a.neckY, b.neckY, k);
         o.headX = lerp(a.headX, b.headX, k);
         o.headY = lerp(a.headY, b.headY, k);
         o.headZ = lerp(a.headZ, b.headZ, k);
         o.jaw = lerp(a.jaw, b.jaw, k);
         o.neckS = lerp(a.neckS, b.neckS, k);
         o.tlLift = lerp(a.tlLift, b.tlLift, k);
         o.tlCurl = lerp(a.tlCurl, b.tlCurl, k);
         o.tlAmp = lerp(a.tlAmp, b.tlAmp, k);
         o.tlSpeed = lerp(a.tlSpeed, b.tlSpeed, k);
         o.tlLag = lerp(a.tlLag, b.tlLag, k);
         o.tlTremor = lerp(a.tlTremor, b.tlTremor, k);
         o.tlFlare = lerp(a.tlFlare, b.tlFlare, k);
         o.tlFin = lerp(a.tlFin, b.tlFin, k);
         o.tlWheel = lerp(a.tlWheel, b.tlWheel, k);
         o.tlWhip = lerp(a.tlWhip, b.tlWhip, k);

         for(int n = 0; n < 2; ++n) {
            double ya = a.world[n] ? a.foot[n][4] : a.foot[n][4] + a.rtYaw + a.pvYaw * 0.4D;
            double yb = b.world[n] ? b.foot[n][4] : b.foot[n][4] + b.rtYaw + b.pvYaw * 0.4D;

            for(int i = 0; i < 6; ++i) {
               o.foot[n][i] = lerp(a.foot[n][i], b.foot[n][i], k);
            }

            o.foot[n][4] = lerp(ya, yb, k);
            o.world[n] = true;

            for(int i = 0; i < 8; ++i) {
               o.arm[n][i] = lerp(a.arm[n][i], b.arm[n][i], k);
            }

            PhageMotion.Reach ra = a.reach[n];
            PhageMotion.Reach rb = b.reach[n];
            if (ra != null || rb != null) {
               PhageMotion.Reach src = rb != null ? rb : ra;
               PhageMotion.Reach r = new PhageMotion.Reach();
               r.k = lerp(ra == null ? 0.0D : ra.k, rb == null ? 0.0D : rb.k, k);
               r.torsoK = src.torsoK;
               r.tTorso = src.tTorso;
               r.tModel = src.tModel;
               r.pTorso = src.pTorso;
               r.pModel = src.pModel;
               r.handQ = src.handQ;
               r.wrist = src.wrist;
               r.feed = src.feed;
               r.cycle = src.cycle;
               o.reach[n] = r;
            }
         }

         o.air = lerp(a.air, b.air, k);
         o.trail = k < 0.5D ? a.trail : b.trail;
         return o;
      }
   }

   private void legIK(int n, double[] foot) {
      int hip = this.legHip[n];
      int knee = this.legKnee[n];
      int paw = this.legPaw[n];
      double[] inv = SarcMath.invert(this.frame(this.iPelvis));
      double[] vv = SarcMath.apply(inv, foot[0], foot[1] - 2.0D, foot[2]);
      ModelPart hp = this.p(hip);
      double dx = vv[0] - (double)hp.x;
      double dy = vv[1] - (double)hp.y;
      double dz = vv[2] - (double)hp.z;
      double roll = -Math.atan2(dx, Math.max(1.0D, dy));
      double dyz = Math.sqrt(dx * dx + dy * dy);
      double d = clamp(Math.sqrt(dyz * dyz + dz * dz), Math.abs((double)0.6999998F) + 0.05D, 12.475D);
      double dir = Math.atan2(dz, dyz);
      double a = Math.acos(clamp((43.55999874114991D + d * d - 34.8100011253357D) / ((double)13.2F * d), -1.0D, 1.0D));
      double th = dir - a;
      double ky = Math.cos(th) * (double)6.6F;
      double kz = Math.sin(th) * (double)6.6F;
      double fy = Math.cos(dir) * d;
      double fz = Math.sin(dir) * d;
      double sh = Math.atan2(fz - kz, fy - ky);
      this.setRot(hip, th, 0.0D, roll);
      this.setRot(knee, sh - th, 0.0D, 0.0D);
      double[] q = SarcMath.rotOf(this.frame(knee));
      double[] q2 = SarcMath.qEuler(foot[3], foot[4], foot[5]);
      this.setQuat(paw, SarcMath.qMul(SarcMath.qInv(q), q2));
   }

   private void gripPose(int n, double k, double spread) {
      double s = SIDE[n];

      for(int i = 0; i < 3; ++i) {
         this.setRot(this.armClaw[n][i], 0.5D + 1.05D * k - 0.35D * spread, 0.0D, s * (double)(i - 1) * 0.12D * spread);
      }

      this.setRot(this.armThumb[n], 0.3D + 0.8D * k, 0.0D, -s * (0.5D - 0.75D * k + 0.25D * spread));
   }

   private void armFK(int n, double[] a) {
      double s = SIDE[n];
      this.setRot(this.armUp[n], -a[0], s * a[2], -s * a[1]);
      this.setRot(this.armFore[n], -a[3], 0.0D, 0.0D);
      this.setRot(this.armHand[n], -a[4], 0.0D, -s * a[5]);
      this.gripPose(n, a[6], a[7]);
   }

   private void solveArm(int n, double[] w, double[] pole) {
      int arm = this.armUp[n];
      int fore = this.armFore[n];
      this.setRot(arm, 0.0D, 0.0D, 0.0D);
      double[] mt = this.frame(this.iTorso);
      ModelPart ap = this.p(arm);
      double[] s = SarcMath.apply(mt, (double)ap.x, (double)ap.y, (double)ap.z);
      double[] dv = SarcMath.sub(w, s);
      double d = SarcMath.len(dv);
      double dd = clamp(d, Math.abs(1.0D) + 0.2D, 19.720799240112303D);
      double[] nrm = SarcMath.scale(dv, 1.0D / Math.max(d, 1.0E-6D));
      double[] b = SarcMath.norm(SarcMath.sub(pole, SarcMath.scale(nrm, SarcMath.dot(pole, nrm))));
      double al = Math.acos(clamp((108.15999206542983D + dd * dd - 88.35999282836929D) / ((double)20.8F * dd), -1.0D, 1.0D));
      double[] u = SarcMath.add(SarcMath.scale(nrm, Math.cos(al)), SarcMath.scale(b, Math.sin(al)));
      double[] e = SarcMath.add(s, SarcMath.scale(u, (double)10.4F));
      double[] f = SarcMath.norm(SarcMath.sub(SarcMath.add(s, SarcMath.scale(nrm, dd)), e));
      double[] z = SarcMath.norm(SarcMath.sub(b, SarcMath.scale(u, SarcMath.dot(b, u))));
      double[] x = SarcMath.cross(u, z);
      double[] q2 = SarcMath.qBasis(x, u, z);
      double[] q = SarcMath.rotOf(this.frame(arm));
      this.setQuat(arm, SarcMath.qMul(SarcMath.qInv(q), q2));
      this.setRot(fore, -Math.acos(clamp(SarcMath.dot(u, f), -1.0D, 1.0D)), 0.0D, 0.0D);
   }

   private void armIK(int n, double[] target, double[] pole, double[] handQ, double wrist) {
      double[] aim = (double[])target.clone();
      double[] wristAt = (double[])aim.clone();
      int hand = this.armHand[n];
      int fore = this.armFore[n];

      for(int pass = 0; pass < 3; ++pass) {
         this.solveArm(n, wristAt, pole);
         if (handQ != null) {
            double[] q = SarcMath.rotOf(this.frame(fore));
            this.setQuat(hand, SarcMath.qMul(SarcMath.qInv(q), handQ));
         } else {
            this.setRot(hand, -wrist, 0.0D, 0.0D);
         }

         double[] g = SarcMath.apply(this.frame(hand), (double)PhageModel.GRIP[0], (double)PhageModel.GRIP[1], (double)PhageModel.GRIP[2]);
         wristAt = SarcMath.add(wristAt, SarcMath.sub(aim, g));
      }

   }

   private void pinch(int i, double k) {
      double close = Math.max(0.0D, k);
      double open = Math.max(0.0D, -k);

      for(int q = 0; q < 3; ++q) {
         int c = this.tailClaw[i][q];
         float[] r0 = this.m.restOf(c);
         double x = (double)r0[3] + (q == 0 ? open * 0.75D - close * 0.62D : -open * 0.7D + close * 0.5D);
         ModelPart cp = this.p(c);
         cp.setPos(r0[0], r0[1], r0[2]);
         this.setRot(c, x, (double)r0[4], 0.0D);
         int ct = this.tailClawTip[i][q];
         float[] t0 = this.m.restOf(ct);
         this.p(ct).xRot = (float)((double)t0[3] * (1.0D + close * 0.9D - open * 0.45D));
      }

   }

   private PhageMotion.Vars moveVars(PhageMotion.Drive d, int move, double c) {
      PhageMotion.Vars v = new PhageMotion.Vars();
      this.defaults(v, d.t, d.still);
      if (move == 1) {
         this.devour(v, c, d.t);
      } else if (move == 2) {
         this.burrow(v, c, d.t, d.swim);
      } else if (move == 3) {
         this.roar(v, c, d.t);
      } else if (move == 4) {
         this.lash(v, c);
      } else if (move == 5) {
         this.slam(v, c, d.t);
      } else if (move == 6) {
         this.stomp(v, c);
      }

      return v;
   }

   public void pose(PhageMotion.Drive d) {
      if (d.move == 1 && this.feedAt == null && !this.feeding) {
         this.feeding = true;
         PhageMotion.Drive at = new PhageMotion.Drive();
         at.t = 1.8900000000000001D;
         at.wave = -1.0D;
         at.move = 1;
         at.c = 0.45D;
         at.weight = 1.0D;
         this.pose(at);
         this.feedAt = (double[])this.present.clone();
         this.feeding = false;
      }

      this.m.rest();
      double t = d.t;
      PhageMotion.Vars base = new PhageMotion.Vars();
      this.defaults(base, t, d.still);
      this.idle(base, t, d.still);
      if (d.walk > 0.0D) {
         PhageMotion.Vars w = new PhageMotion.Vars();
         this.defaults(w, t, d.still);
         this.walk(w, t, d.gait, d.still);
         base = blend(base, w, clamp(d.walk, 0.0D, 1.0D));
      }

      PhageMotion.Vars v = base;
      if (d.move != 0 && d.weight > 0.0D) {
         v = blend(base, this.moveVars(d, d.move, d.c), clamp(d.weight, 0.0D, 1.0D));
      }

      double look = 1.0D - 0.8D * clamp(d.weight, 0.0D, 1.0D);
      double ly = clamp(d.lookYaw, -0.9D, 0.9D) * look;
      double lp = clamp(d.lookPitch, -0.8D, 0.8D) * look;
      v.neckY += ly * 0.55D;
      v.headY += ly * 0.3D;
      v.neckX += lp * 0.25D;
      v.headX += lp * 0.3D;
      this.apply(v, d);
   }

   private void apply(PhageMotion.Vars v, PhageMotion.Drive d) {
      double t = d.t;
      ModelPart root = this.p(this.iPhage);
      root.setPos((float)v.rtX, (float)v.rtY, (float)v.rtZ);
      this.setRot(this.iPhage, v.rtPitch, v.rtYaw, v.rtRoll);
      this.setRot(this.iPelvis, v.pvPitch, v.pvYaw, v.pvRoll);
      float[] tr0 = this.m.restOf(this.iTorso);
      this.setRot(this.iTorso, (double)tr0[3] + v.torPitch, (double)tr0[4] + v.torYaw, (double)tr0[5] + v.torRoll);
      ModelPart torso = this.p(this.iTorso);
      torso.xScale = (float)v.torsoS;
      torso.yScale = (float)v.torsoS;
      torso.zScale = (float)v.torsoS;
      ModelPart belly = this.p(this.iBelly);
      belly.y += (float)v.bellyY;
      belly.xScale = (float)(v.bellyS * (1.0D + Math.max(0.0D, v.bellyY) * 0.015D));
      belly.yScale = (float)(1.0D - v.bellyY * 0.01D);
      belly.zScale = (float)v.bellyS;
      ModelPart neck = this.p(this.iNeck);
      neck.xRot += (float)v.neckX;
      neck.yRot += (float)v.neckY;
      neck.xScale = (float)v.neckS;
      neck.yScale = (float)v.neckS;
      ModelPart head = this.p(this.iHead);
      head.xRot += (float)v.headX;
      head.yRot += (float)v.headY;
      head.zRot += (float)v.headZ;
      this.p(this.iJaw).xRot = (float)((double)this.m.restOf(this.iJaw)[3] + v.jaw);

      for(int n = 0; n < 2; ++n) {
         double[] f = (double[])v.foot[n].clone();
         if (!v.world[n]) {
            f[4] += v.rtYaw + v.pvYaw * 0.4D;
         }

         this.legIK(n, f);
      }

      if (v.air > 0.0D) {
         for(int n = 0; n < 2; ++n) {
            ModelPart hip = this.p(this.legHip[n]);
            ModelPart knee = this.p(this.legKnee[n]);
            ModelPart paw = this.p(this.legPaw[n]);
            double kick = v.trail ? 0.22D * Math.sin(t * 7.0D + (SIDE[n] > 0.0D ? 0.0D : Math.PI)) : 0.0D;
            double hx = v.trail ? 0.15D + kick : -0.95D;
            double kx = v.trail ? 0.25D - kick * 0.5D : 1.25D;
            double px2 = v.trail ? 0.9D : -0.25D;
            this.setRot(this.legHip[n], lerp((double)hip.xRot, hx, v.air), 0.0D, lerp((double)hip.zRot, -SIDE[n] * 0.12D, v.air));
            this.setRot(this.legKnee[n], lerp((double)knee.xRot, kx, v.air), 0.0D, 0.0D);
            this.setRot(this.legPaw[n], lerp((double)paw.xRot, px2, v.air), 0.0D, 0.0D);
         }
      }

      for(int n = 0; n < 2; ++n) {
         this.armFK(n, v.arm[n]);
         PhageMotion.Reach r = v.reach[n];
         if (r != null && r.k > 0.0D) {
            double[] fkArm = this.quat(this.armUp[n]);
            double[] fkFore = this.quat(this.armFore[n]);
            double[] fkHand = this.quat(this.armHand[n]);
            double[] tg = r.tModel;
            double[] pl = r.pModel;
            if (r.torsoK > 0.0D) {
               double[] mt = this.frame(this.iTorso);
               double[] tq = SarcMath.rotOf(mt);
               double[] tt = SarcMath.apply(mt, r.tTorso);
               double[] pt = SarcMath.qRotate(tq, r.pTorso);
               tg = SarcMath.lerp3(r.tModel, tt, r.torsoK);
               pl = SarcMath.lerp3(r.pModel, pt, r.torsoK);
            }

            if (!(r.feed >= 0.0D)) {
               this.armIK(n, tg, pl, r.handQ, r.wrist);
            } else {
               double[] mh = this.frame(this.iHead);
               this.present = SarcMath.apply(mh, PRESENT);
               double[] up = r.cycle >= 0.45D && this.feedAt != null ? this.feedAt : this.present;
               tg = SarcMath.lerp3(r.tModel, up, r.feed);
               this.armIK(n, tg, pl, r.handQ, 0.0D);
            }

            if (r.k < 1.0D) {
               this.setQuat(this.armUp[n], SarcMath.qSlerp(fkArm, this.quat(this.armUp[n]), r.k));
               this.setQuat(this.armFore[n], SarcMath.qSlerp(fkFore, this.quat(this.armFore[n]), r.k));
               this.setQuat(this.armHand[n], SarcMath.qSlerp(fkHand, this.quat(this.armHand[n]), r.k));
            }
         }
      }

      double still = d.still;

      for(int i = 0; i < 5; ++i) {
         double ang = (double)this.m.restOf(this.tailRoot[i])[5];
         double lift = CROWN_LIFT[i] + v.tlLift;
         double curl0 = CROWN_CURL[i] + v.tlCurl;
         double ramp = CROWN_RAMP[i];
         double plane = CROWN_PLANE[i];
         if (v.tlFlare > 0.0D) {
            double fk = clamp(v.tlFlare, 0.0D, 1.0D);
            lift = lerp(lift, CROWN_LIFT[i] + FLARE[i], fk);
            curl0 = lerp(curl0, 0.02D, fk);
            ramp = lerp(ramp, 0.004D, fk);
            plane = lerp(plane, CROWN_PLANE[i] * 0.5D, fk);
         }

         double[] q = SarcMath.qEuler(lift, 0.0D, ang + plane);
         if (v.tlFin > 0.0D) {
            double[] q2 = SarcMath.aimQ(FIN_AIM[i], new double[]{1.0D, 0.0D, 0.0D});
            q = SarcMath.qSlerp(q, q2, v.tlFin);
            curl0 = lerp(curl0, -0.05D, v.tlFin);
            ramp = lerp(ramp, 0.0D, v.tlFin);
         }

         if (v.tlWheel > 0.0D) {
            double wy_0 = WHEEL_YAW[i];
            double wd = WHEEL_DROOP[i];
            double[] q2 = SarcMath.aimQ(new double[]{Math.sin(wy_0) * Math.cos(wd), Math.sin(wd), Math.cos(wy_0) * Math.cos(wd)}, new double[]{0.0D, 1.0D, 0.0D});
            q = SarcMath.qSlerp(q, q2, v.tlWheel);
            curl0 = lerp(curl0, v.tlWhip, v.tlWheel);
            ramp = lerp(ramp, 0.0D, v.tlWheel);
         }

         this.setQuat(this.tailRoot[i], q);
         ModelPart b = this.p(this.tailBase[i]);
         b.setPos(0.0F, 0.0F, 0.0F);
         this.setRot(this.tailBase[i], 0.0D, 0.0D, 0.0D);
         double w = (d.wave < 0.0D ? t * v.tlSpeed : d.wave) * 0.9D * CROWN_FREQ[i];
         int ns = 9;

         for(int s = 0; s < ns; ++s) {
            double a = (double)s / (double)(ns - 1);
            double ph2 = w - (double)s * 0.5D + CROWN_PHASE[i];
            double wx = Math.sin(ph2) * 0.04D * v.tlAmp * (0.35D + 0.65D * a) * still;
            double wy = Math.cos(ph2 * 0.8D + 0.5D) * 0.022D * v.tlAmp * a * still;
            double tr = v.tlTremor != 0.0D ? Math.sin(t * 47.0D + (double)s * 1.3D + (double)i) * 0.03D * v.tlTremor * a : 0.0D;
            double whip = v.tlWheel > 0.0D ? v.tlWhip * v.tlWheel * (0.4D + 1.2D * a) : 0.0D;
            double flex = CROWN_FLEX[i] * Math.pow(a, 1.5D) * (1.0D - v.tlWheel) * (1.0D - v.tlFin) * (1.0D - 0.7D * clamp(v.tlFlare, 0.0D, 1.0D)) + WHEEL_SAG[i] * v.tlWheel;
            this.setRot(this.tailSeg[i][s], curl0 + ramp * (double)s + wx + tr + whip * 0.5D, flex + wy + tr * 0.7D + v.tlLag * (0.1D + 0.2D * a) * (1.0D - v.tlWheel), 0.0D);
         }

         this.pinch(i, 0.15D * Math.sin(t * 1.6D + (double)i * 1.7D) - 0.1D - v.tlTremor * 0.4D - v.tlFlare * 0.5D);
      }

      this.speed = v.tlSpeed;
      this.grip = SarcMath.apply(this.frame(this.armHand[1]), (double)PhageModel.GRIP[0], (double)PhageModel.GRIP[1], (double)PhageModel.GRIP[2]);
      this.mouth = SarcMath.apply(this.frame(this.iHead), (double)PhageModel.MOUTH[0], (double)PhageModel.MOUTH[1], (double)PhageModel.MOUTH[2]);
   }

   public double[] partPoint(String name, double x, double y, double z) {
      return SarcMath.apply(this.frame(this.m.id(name)), x, y, z);
   }

   public int tailSeg(int tail, int seg) {
      return this.tailSeg[tail][seg];
   }

   public int tailTip(int tail) {
      return this.tailTip[tail];
   }

   public static final class Drive {
      public double t;
      public double wave;
      public double still = 1.0D;
      public double walk;
      public double gait;
      public double lookYaw;
      public double lookPitch;
      public int move = 0;
      public double c;
      public double weight;
      public double swim = -1.0D;
   }

   static final class Reach {
      double k;
      double torsoK;
      double[] tTorso = new double[]{0.0D, 0.0D, 0.0D};
      double[] tModel = new double[]{0.0D, 0.0D, 0.0D};
      double[] pTorso = new double[]{0.0D, 0.0D, 1.0D};
      double[] pModel = new double[]{0.0D, 0.0D, 1.0D};
      double[] handQ;
      double wrist;
      double feed = -1.0D;
      double cycle = -1.0D;
   }

   static final class Vars {
      double rtX;
      double rtY;
      double rtZ;
      double rtPitch;
      double rtYaw;
      double rtRoll;
      double pvPitch;
      double pvYaw;
      double pvRoll;
      double torPitch;
      double torYaw;
      double torRoll;
      double torsoS;
      double bellyY;
      double bellyS;
      double neckX;
      double neckY;
      double headX;
      double headY;
      double headZ;
      double jaw;
      double neckS;
      double tlLift;
      double tlCurl;
      double tlAmp;
      double tlSpeed;
      double tlLag;
      double tlTremor;
      double tlFlare;
      double tlFin;
      double tlWheel;
      double tlWhip;
      final double[][] foot = new double[2][6];
      final boolean[] world = new boolean[2];
      final double[][] arm = new double[2][8];
      final PhageMotion.Reach[] reach = new PhageMotion.Reach[2];
      double air;
      boolean trail;
   }
}
