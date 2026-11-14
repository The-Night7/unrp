package com.myriam.unrp.uo;



import net.minecraft.client.model.geom.ModelPart;

public final class SarcophageTailPose {
   public static final float[] SLOT_X = new float[]{0.0F, -5.2F, 5.2F, -3.29F, 3.29F};
   public static final float[] SLOT_Y = new float[]{7.98F, 3.27F, 3.27F, 12.16F, 12.16F};
   public static final float[] SLOT_Z = new float[]{3.3F, 3.3F, 3.3F, 3.3F, 3.3F};
   public static final float[] SLOT_LIFT = new float[]{0.023F, 0.12F, 0.12F, -0.4F, -0.4F};
   public static final float[] SLOT_YAW = new float[]{0.0F, -0.275F, 0.275F, -0.25F, 0.25F};
   public static final float[] SLOT_ROLL = new float[]{0.0F, -0.639F, 0.639F, -1.017F, 1.017F};
   public static final float[] SLOT_CURL = new float[]{0.213F, 0.266F, 0.266F, 0.296F, 0.296F};
   public static final float[] SLOT_RAMP = new float[]{0.012F, 0.016F, 0.016F, 0.01F, 0.01F};
   public static final float[] SLOT_AMP = new float[]{1.0F, 1.0F, 1.0F, 0.9F, 0.9F};
   public static final float[] SLOT_PHASE = new float[]{0.0F, 2.1F, 2.1F, 4.2F, 4.2F};
   public static final float[] SLOT_MIRROR = new float[]{1.0F, -1.0F, 1.0F, -1.0F, 1.0F};
   public static final float[] WHIP_LIFT = new float[]{0.1F, 0.18F, 0.18F, 0.12F, 0.12F};
   public static final float[] WHIP_CURL = new float[]{0.05F, 0.065F, 0.065F, 0.05F, 0.05F};
   public static final float[] WHIP_YAW = new float[]{0.0F, -0.14F, 0.14F, -0.32F, 0.32F};
   public static final float[] LEG_LIFT = new float[]{0.0F, 0.0F, 0.0F, 0.796F, 0.796F};
   public static final float[] LEG_ROLL = new float[]{0.0F, 0.0F, 0.0F, 0.089F, 0.089F};
   public static final float[] ARM_LIFT = new float[]{0.0F, 0.452F, 0.452F, 0.0F, 0.0F};
   public static final float[] ARM_ROLL = new float[]{0.0F, -0.584F, -0.584F, 0.0F, 0.0F};
   public static final float[] LOOK_LIFT = new float[]{-0.031F, -0.259F, -0.259F, 0.0F, 0.0F};
   public static final float[] LOOK_ROLL = new float[]{0.0F, 0.173F, 0.173F, 0.0F, 0.0F};
   public static final float[] TENSE_LIFT = new float[]{0.07F, -0.051F, -0.051F, 0.133F, 0.133F};
   public static final float[] ROOT_X = new float[]{0.0F, -3.1F, 3.1F, -2.95F, 2.95F};
   public static final float[] ROOT_Y = new float[]{8.0F, 3.9F, 3.9F, 11.1F, 11.1F};
   public static final float ROOT_SKIN = 2.0F;
   public static final float ROOT_SINK = 2.0F;
   public static final float ROOT_INTO = 0.6F;
   public static final double WHIP_TICKS = 20.0D;
   public static final double STAGGER = 0.016D;

   private SarcophageTailPose() {
   }

   public static double[] rootPose(int slot) {
      double[] tail = new double[]{(double)SLOT_X[slot], (double)SLOT_Y[slot], (double)SLOT_Z[slot]};
      double[] skin = new double[]{(double)ROOT_X[slot], (double)ROOT_Y[slot], 2.0D};
      double[] u = SarcMath.norm(SarcMath.sub(tail, skin));
      double[] seat = SarcMath.mul(SarcMath.compose(0.0D, 0.0D, 0.0D, (double)SLOT_LIFT[slot], (double)SLOT_YAW[slot], (double)SLOT_ROLL[slot], 1.0D, 1.0D, 1.0D), SarcMath.compose(0.0D, 0.0D, 0.0D, (double)SLOT_CURL[slot], 0.0D, 0.0D, 1.0D, 1.0D, 1.0D));
      double[] x0 = SarcMath.applyDir(seat, new double[]{1.0D, 0.0D, 0.0D});
      double[] x = SarcMath.norm(SarcMath.sub(x0, SarcMath.scale(u, SarcMath.dot(x0, u))));
      double[] y = SarcMath.cross(u, x);
      double[] start = SarcMath.sub(skin, SarcMath.scale(u, 2.0D));
      double length = SarcMath.dist(tail, skin) + 2.0D + (double)0.6F;
      return new double[]{start[0], start[1], start[2], Math.atan2(y[2], u[2]), Math.asin(SarcMath.clamp(-x[2], -1.0D, 1.0D)), Math.atan2(x[1], x[0]), length};
   }

   public static double strikeCurve(double c) {
      if (c < 0.22D) {
         return -SarcMath.smooth(c / 0.22D);
      } else if (c < 0.34D) {
         return -1.0D + 2.0D * SarcMath.outCubic((c - 0.22D) / 0.12D);
      } else {
         double k = c - 0.34D;
         return Math.exp(-k * 6.5D) * Math.cos(k * 16.0D);
      }
   }

   public static double whipCycle(double ticks) {
      if (ticks < 0.0D) {
         return -1.0D;
      } else if (ticks <= 4.0D) {
         return 0.1D + 0.06D * ticks;
      } else {
         return ticks <= 20.0D ? 0.34D + (ticks - 4.0D) / 16.0D * 0.66D : -1.0D;
      }
   }

   private static double strikeAt(SarcophageTailPose.Motion m, int s) {
      return m.strike < 0.0D ? 0.0D : strikeCurve(SarcMath.frac(m.strike - (double)s * 0.016D)) * m.strikeK;
   }

   public static void pose(SegmentedTailModel tail, int slot, double t, SarcophageTailPose.Motion m) {
      tail.reset();
      double amp = 0.075D * (double)SLOT_AMP[slot] * m.amp;
      double st0 = strikeAt(m, 0);
      ModelPart base = tail.base;
      base.setPos(SLOT_X[slot], SLOT_Y[slot], SLOT_Z[slot]);
      double tense = m.tense;
      double tenseYaw = slot == 1 ? tense * 0.05D : -tense * 0.05D;
      double leg = Math.max(0.0D, slot == 3 ? m.legR : (slot == 4 ? m.legL : 0.0D));
      double arm = Math.max(0.0D, slot == 1 ? m.armR : (slot == 2 ? m.armL : 0.0D));
      double up = Math.max(0.0D, -m.headPitch);
      double out = (double)Math.signum(SLOT_ROLL[slot]);
      double lift = (double)LEG_LIFT[slot] * leg + (double)ARM_LIFT[slot] * arm + (double)LOOK_LIFT[slot] * up;
      double roll = out * ((double)LEG_ROLL[slot] * leg + (double)ARM_ROLL[slot] * arm + (double)LOOK_ROLL[slot] * up);
      base.setRotation((float)((double)SLOT_LIFT[slot] + m.lift + st0 * (double)WHIP_LIFT[slot] + tense * (double)TENSE_LIFT[slot] + lift), (float)((double)SLOT_YAW[slot] + Math.abs(st0) * (double)WHIP_YAW[slot] * 0.5D + tenseYaw), (float)((double)SLOT_ROLL[slot] + roll));

      for(int s = 0; s < 8; ++s) {
         double a = (double)s / 7.0D;
         double ph = t * m.speed - (double)s * 0.55D + (double)SLOT_PHASE[slot];
         double wave = Math.sin(ph) * amp * (0.25D + 0.75D * a);
         double rise = Math.cos(ph * 0.73D + 0.6D) * amp * 0.38D * a;
         double gait = Math.sin(m.limb * 0.6662D + (double)SLOT_PHASE[slot] * 0.9D - (double)s * 0.42D) * 0.045D * m.walk * (0.35D + 0.65D * a);
         double st = strikeAt(m, s + 1);
         tail.seg[s].setRotation((float)((double)(SLOT_CURL[slot] + SLOT_RAMP[slot] * (float)s) + rise + st * (double)WHIP_CURL[slot] + m.curl), (float)((wave + gait) * (double)SLOT_MIRROR[slot] + Math.abs(st) * (double)WHIP_YAW[slot] * 0.25D * a), 0.0F);
      }

      tail.pinch((float)(0.12D * Math.sin(t * 0.21D + (double)SLOT_PHASE[slot]) - 0.05D - m.flare), 0.0F);
   }

   public static SarcophageTailPose.Motion motion(boolean tempest, double walk, double limb, double whipTicks, double still) {
      SarcophageTailPose.Motion m = new SarcophageTailPose.Motion();
      m.amp = (tempest ? 1.5D : 1.0D) * still + walk * 0.35D;
      m.speed = tempest ? 0.15D : 0.085D;
      m.walk = walk;
      m.limb = limb;
      m.lift = tempest ? -0.06D : 0.0D;
      double c = whipCycle(whipTicks);
      if (c >= 0.0D) {
         m.strike = c;
         m.strikeK = SarcMath.smooth(whipTicks / 1.5D);
         double st = strikeCurve(c) * m.strikeK;
         m.flare = Math.max(0.0D, st) * 0.9D;
      }

      return m;
   }

   public static final class Motion {
      public double amp = 1.0D;
      public double speed = 0.085D;
      public double walk;
      public double limb;
      public double lift;
      public double curl;
      public double strike = -1.0D;
      public double strikeK = 1.0D;
      public double flare;
      public double tense;
      public double legR;
      public double legL;
      public double armR;
      public double armL;
      public double headPitch;
   }
}
