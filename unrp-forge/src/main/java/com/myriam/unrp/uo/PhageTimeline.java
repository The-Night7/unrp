package com.myriam.unrp.uo;



public final class PhageTimeline {
   public static final double DEVOUR_RATE = 42.0D;
   public static final double DEVOUR_CHOMP = 21.0D;
   public static final double DEVOUR_SNATCH = 12.0D;
   public static final double CHEW_PERIOD = 14.0D;
   public static final double GULP = 14.0D;
   public static final double DIVE = 12.0D;
   public static final double ERUPT_RATE = 104.0D;
   public static final double RAMP_IN = 3.0D;
   public static final double RAMP_OUT = 4.0D;

   private PhageTimeline() {
   }

   private static double seg(double tau, double t0, double t1, double c0, double c1) {
      double k = SarcMath.clamp((tau - t0) / (t1 - t0), 0.0D, 1.0D);
      return c0 + (c1 - c0) * k;
   }

   public static double devourC(double tau, int hold) {
      if (hold > 0 && !(tau < 21.0D)) {
         double holdEnd = 12.0D + (double)hold;
         return tau < holdEnd ? 0.505D + 0.115D * SarcMath.frac((tau - 21.0D) / 14.0D) : seg(tau, holdEnd, holdEnd + 14.0D, 0.62D, 0.9D);
      } else {
         return Math.min(0.9D, tau / 42.0D);
      }
   }

   public static double spitC(double tau) {
      if (tau < 4.0D) {
         return seg(tau, 0.0D, 4.0D, 0.2D, 0.29D);
      } else {
         return tau < 14.0D ? seg(tau, 4.0D, 14.0D, 0.29D, 0.82D) : seg(tau, 14.0D, 22.0D, 0.82D, 1.0D);
      }
   }

   public static double roarC(double tau) {
      if (tau < 10.0D) {
         return seg(tau, 0.0D, 10.0D, 0.0D, 0.29D);
      } else {
         return tau < 40.0D ? seg(tau, 10.0D, 40.0D, 0.29D, 0.82D) : seg(tau, 40.0D, 50.0D, 0.82D, 1.0D);
      }
   }

   public static double lashC(double tau) {
      if (tau < 8.0D) {
         return seg(tau, 0.0D, 8.0D, 0.0D, 0.24D);
      } else {
         return tau < 28.0D ? seg(tau, 8.0D, 28.0D, 0.24D, 0.62D) : seg(tau, 28.0D, 42.0D, 0.62D, 1.0D);
      }
   }

   public static PhageTimeline.Pose at(int move, double tau, double life, int hold) {
      PhageTimeline.Pose p = new PhageTimeline.Pose();
      if (move > 0 && !(tau < 0.0D)) {
         switch (move) {
            case 1:
               p.move = 1;
               p.c = devourC(tau, hold);
               break;
            case 2:
               p.move = 2;
               if (tau < 12.0D) {
                  p.c = 0.27D * tau / 12.0D;
               } else {
                  p.c = 0.45D;
                  p.swim = (tau - 12.0D) / 20.0D;
               }

               p.planted = false;
               break;
            case 3:
            case 9:
               p.move = 2;
               p.c = Math.min(1.0D, 0.69D + tau / 104.0D);
               p.planted = false;
               break;
            case 4:
               p.move = 3;
               p.c = roarC(tau);
               break;
            case 5:
               p.move = 4;
               p.c = lashC(tau);
               break;
            case 6:
               p.move = 5;
               p.c = Math.min(1.0D, tau / 28.0D);
               break;
            case 7:
               p.move = 6;
               p.c = Math.min(1.0D, tau / 24.0D);
               break;
            case 8:
               p.move = 3;
               p.c = spitC(tau);
               break;
            default:
               return p;
         }

         double in = SarcMath.smooth(tau / 3.0D);
         double out = move == 2 ? 1.0D : SarcMath.smooth((life - tau) / 4.0D);
         p.weight = Math.min(in, out);
         return p;
      } else {
         return p;
      }
   }

   public static PhageTimeline.Pose at(int move, double tau, double life, int hold, boolean rampIn) {
      PhageTimeline.Pose p = at(move, tau, life, hold);
      if (!rampIn && p.move != 0) {
         p.weight = SarcMath.smooth((life - tau) / 4.0D);
      }

      return p;
   }

   public static final class Pose {
      public int move = 0;
      public double c;
      public double swim = -1.0D;
      public double weight;
      public boolean planted = true;
   }
}
