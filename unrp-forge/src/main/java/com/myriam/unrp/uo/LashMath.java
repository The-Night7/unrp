package com.myriam.unrp.uo;



public final class LashMath {
   public static final int WEIGHT = 13;
   public static final int AURA_BASE = 135;
   public static final int REGEN = 12;
   public static final double STRAIN_MAX = 100.0D;
   public static final double DRAIN_STILL = 10.0D;
   public static final double DRAIN_SNEAK = 20.0D;
   public static final int FRENZY_TICKS = 120;
   public static final int REST_TICKS = 200;
   public static final double FRENZY_RADIUS = 9.0D;
   public static final double FRENZY_ALLY = 0.5D;
   public static final double FRENZY_HIT = 4.0D;
   public static final int FRENZY_EVERY = 5;
   public static final int FRENZY_TENDRILS = 8;
   public static final int FRENZY_PER_BEAT = 2;
   public static final int FRENZY_GAP = 10;
   public static final double FRENZY_FEED = 2.0D;
   public static final int FRENZY_FEEDS = 3;
   public static final int FRENZY_FEED_EVERY = 40;
   public static final double FRENZY_LIFT = 1.4D;
   public static final double FRENZY_RISE = 0.3D;
   public static final double FRENZY_BURST = 4.0D;
   public static final double FRENZY_SHOVE = 1.0D;
   public static final double FRENZY_SHOVE_UP = 0.5D;
   public static final double FRENZY_KB = 0.55D;
   public static final double FRENZY_UP = 0.35D;
   public static final int FRENZY_LINGER = 8;
   public static final int FRENZY_RETRACT = 5;
   public static final double FRENZY_GROUND_NEAR = 2.5D;
   public static final double FRENZY_GROUND_FAR = 6.5D;
   public static final int WINDUP = 3;
   public static final int ROLL = 4;
   public static final int CRACK = 7;
   public static final int SNATCH_SLOT = 1;
   public static final int SNATCH_COST = 8;
   public static final int SNATCH_COOLDOWN = 30;
   public static final double SNATCH_RANGE = 56.0D;
   public static final double SNATCH_NEAR = 14.0D;
   public static final double GRIP_FAR = 21.0D;
   public static final double SNATCH_STRAIN = 8.0D;
   public static final double ZIP_SPEED = 1.6D;
   public static final int SWING_DRAIN = 8;
   public static final double SWING_STRAIN = 2.0D;
   public static final double PULL_HIT = 2.0D;
   public static final double ZIP_HIT = 3.0D;
   public static final int GRIP = 4;
   public static final int REEL_TICKS = 6;
   public static final double REEL_STOP = 1.6D;
   public static final double REEL_ARC = 0.9D;
   public static final double REEL_EASE = 2.2D;
   public static final int ZIP_MOST = 30;
   public static final double ZIP_ARRIVE = 1.4D;
   public static final double ZIP_HAND = 1.2D;
   public static final double VAULT_UP = 0.62D;
   public static final double VAULT_PUSH = 0.34D;
   public static final int VAULT_DELAY = 5;
   public static final double BOUNCE_BACK = 0.25D;
   public static final double BOUNCE_UP = 0.35D;
   public static final double POUND_LEAST = 4.0D;
   public static final double POUND_FULL = 24.0D;
   public static final double POUND_SLANT = 1.0D;
   public static final double POUND_HIT_LEAST = 6.0D;
   public static final double POUND_HIT_FULL = 16.0D;
   public static final double POUND_REACH_LEAST = 4.0D;
   public static final double POUND_REACH_FULL = 9.0D;
   public static final double POUND_EDGE = 0.6D;
   public static final double POUND_KB_LEAST = 0.6D;
   public static final double POUND_KB_FULL = 1.4D;
   public static final double POUND_UP_LEAST = 0.5D;
   public static final double POUND_UP_FULL = 0.9D;
   public static final double POUND_SPEED_START = 1.2D;
   public static final double POUND_SPEED = 3.2D;
   public static final double POUND_ACCEL = 0.5D;
   public static final double POUND_TOUCH = 0.5D;
   public static final double POUND_NEAR = 1.2D;
   public static final int POUND_SETTLE = 6;
   public static final int POUND_STALL = 3;
   public static final int POUND_MOST = 40;
   public static final int POUND_COST = 12;
   public static final int POUND_COOLDOWN = 80;
   public static final double POUND_STRAIN = 12.0D;
   public static final double POUND_CRATER_AT = 12.0D;
   public static final double POUND_CRATER_LEAST = 1.8D;
   public static final double POUND_CRATER_FULL = 3.0D;
   public static final double SWING_HAND = 1.9D;
   public static final double SWING_GRAVITY = 0.09D;
   public static final double SWING_DAMP = 0.999D;
   public static final double RELEASE_MULT = 1.2D;
   public static final double RELEASE_LIFT = 0.24D;
   public static final int SWING_STEPS = 8;
   public static final double SWING_PUSH = 0.02D;
   public static final double SWING_HELP = 0.998D;
   public static final double SWING_IDLE = 0.972D;
   public static final int SWING_CARRY = 60;
   public static final double SWING_SETTLE = 0.92D;
   public static final double SWING_SETTLE_H = 0.2D;
   public static final double SWING_FASTEST = 1.6D;
   public static final double SWING_TIGHT = 1.0D;
   public static final double SWING_IN = 0.3D;
   public static final double SWING_OUT = 0.3D;
   public static final double SWING_LONGEST = 56.0D;
   public static final double SWING_HANG_H = 0.06D;
   public static final double SWING_HANG_OUT = 0.25D;
   public static final int SWING_HANG_AFTER = 6;
   public static final double SWING_HANG_STRAIN = 0.5D;
   public static final int SWING_HANG_DRAIN = 16;
   public static final double FLIP_SPEED = 0.5D;
   public static final double FLIP_RISE = 0.1D;
   public static final double DROP_SPEED = 0.2D;
   public static final double FLIP_LIFT = 0.15D;
   public static final int FLIP_TICKS = 18;
   public static final int FLIP_LAND = 3;
   public static final int SWING_STALE = 3;
   public static final int SWING_TRACK = 16;
   public static final double SWING_SNAP = 6.0D;
   public static final double SWING_SLACK = 0.02D;
   public static final int SWING_FRESH = 3;
   public static final int POSE_SWINGING = 0;
   public static final int POSE_HANGING = 1;
   public static final int POSE_PUMPING = 2;
   public static final int POSE_TUCKING = 3;
   public static final int POSE_VEERING = 4;
   public static final int POSE_VEERING_LEFT = 5;
   public static final double LEAN_IN = 3.0D;
   public static final double LEAN_OUT = 4.0D;
   public static final double SWING_RISE = 3.0D;
   public static final double SWING_REEL = 0.8D;
   public static final double SWING_CLEAR = 1.0D;
   public static final double SWING_SHORTEST = 2.0D;
   public static final double SWING_REEL_RATE = 0.45D;
   public static final double SWING_KICK = 0.3D;
   public static final int SLING_SLOT = 1;
   public static final int SLING_COST = 14;
   public static final int SLING_COOLDOWN = 120;
   public static final double SLING_STRAIN = 15.0D;
   public static final double SLING_REACH = 8.0D;
   public static final int SLING_CHARGE = 40;
   public static final double SLING_SPEED_MAX = 3.0D;
   public static final double SLING_LIFT = 0.5D;
   public static final double SLING_LOW_LIFT = 0.7D;
   public static final double SLING_AIM_MOST = 60.0D;
   public static final float[] SLING_YAWS = new float[]{35.0F, 45.0F, 25.0F, 55.0F, 15.0F};
   public static final float[] SLING_LIFTS = new float[]{-10.0F, 0.0F, -22.0F};
   public static final float[] SLING_DOWN = new float[]{20.0F, 35.0F, 50.0F};
   public static final int BIND_SLOT = 2;
   public static final int BIND_COST = 20;
   public static final int BIND_COOLDOWN = 240;
   public static final int BIND_HOLD = 600;
   public static final int BIND_MOST = 3;
   public static final int BIND_MOST_TWIN = 6;
   public static final int BIND_TAKE_COST = 10;
   public static final double BIND_TAKE_STRAIN = 10.0D;
   public static final double HOLD_STEER = 3.2D;
   public static final int SET_DOWN = 100;
   public static final double[][] HOLD_AT = new double[][]{{0.4D, 2.3D, 2.3D}, {0.4D, -2.3D, 2.3D}, {-1.6D, 1.5D, 3.0D}, {-1.6D, -1.5D, 3.0D}};
   public static final double BIND_STRAIN = 20.0D;
   public static final int BIND_PRESSES = 8;
   public static final double BIND_BREAK = 12.0D;
   public static final double THROW_SPEED = 1.6D;
   public static final double THROW_HIT = 6.0D;
   public static final double SLAM = 8.0D;
   public static final int BIND_GRAB = 7;
   public static final int BIND_LIFT = 6;
   public static final int THROW_AT = 4;
   public static final int SLAM_AT = 4;
   public static final int THROWN_MOST = 30;
   public static final double THROWN_REACH = 1.1D;
   public static final int CATCH_COOLDOWN = 600;
   public static final double CATCH_RANGE = 16.0D;
   public static final int WHIP_SLOT = 3;
   public static final int WHIP_COST = 16;
   public static final int WHIP_COOLDOWN = 100;
   public static final double WHIP_STRAIN = 12.0D;
   public static final double WHIP_REACH = 7.0D;
   public static final int WHIP_CRACKS = 3;
   public static final double WHIP_DAMAGE = 4.0D;
   public static final int[] WHIP_AT = new int[]{1, 5, 9};
   public static final double WHIP_FAN = 0.3D;
   public static final int WARD_SLOT = 3;
   public static final int WARD_COST = 18;
   public static final int WARD_COOLDOWN = 280;
   public static final double WARD_STRAIN = 20.0D;
   public static final int WARD_TICKS = 80;
   public static final double WARD_RADIUS = 3.0D;
   public static final double WARD_LASH = 3.0D;
   public static final int WARD_EVERY = 10;
   public static final double WARD_CUT = 0.6D;
   public static final int WARD_LAND = 2;
   public static final int STORM_SLOT = 4;
   public static final int STORM_COST = 48;
   public static final int STORM_COOLDOWN = 1800;
   public static final int STORM_TICKS = 60;
   public static final double STORM_RADIUS = 10.0D;
   public static final int STORM_TENDRILS = 8;
   public static final double STORM_NEEDS = 50.0D;
   public static final double STORM_CRACK = 3.0D;
   public static final double STORM_FINALE = 6.0D;
   public static final int STORM_EVERY = 12;
   public static final int STORM_FINALE_AT = 6;
   public static final double STORM_GROUND_NEAR = 3.0D;
   public static final double STORM_GROUND_FAR = 7.5D;
   public static final double STORM_REARM = 5.0D;
   public static final int WIND_HOURS = 24;
   public static final int WIND_COUNTDOWN = 200;
   public static final int WIND_TICKS = 4800;
   public static final double WIND_RADIUS = 5.0D;
   public static final double WIND_LASH = 3.0D;
   public static final int WIND_EVERY = 15;
   public static final double WIND_DEALT = 0.3D;
   public static final double WIND_CUT = 0.3D;
   public static final double WIND_REVIVE = 12.0D;
   public static final double WIND_ALLY = 0.5D;
   public static final double WIND_END_HP = 2.0D;
   public static final int WIND_WEAKNESS = 1200;
   public static final int WIND_SLOWNESS = 900;
   public static final int WIND_BLINDNESS = 120;
   public static final double WIND_BURST = 5.0D;
   public static final double WIND_RAGE_HOLD = 2.0D;
   public static final double WIND_RAGE_LAND = 4.0D;
   public static final double CATCH_FALL = -0.55D;
   public static final int CATCH_GRIP = 4;
   public static final int CATCH_LOWER = 24;
   public static final int RISE_BURST_AT = 5;
   public static final int RISE_ACTIVE_AT = 12;
   public static final double RAGE_SPEED = 1.15D;
   public static final double RAGE_LAND_RADIUS = 4.0D;
   public static final int WIND_SLOTS = 5;
   public static final long WIND_MS = 86400000L;
   public static final int PHASE_NONE = 0;
   public static final int PHASE_DOWN = 1;
   public static final int PHASE_RISE = 2;
   public static final int PHASE_ACTIVE = 3;
   public static final int PHASE_SPENT = 4;

   private LashMath() {
   }

   public static double clamp(double x, double a, double b) {
      return x < a ? a : (x > b ? b : x);
   }

   public static double clamp01(double x) {
      return x < 0.0D ? 0.0D : (x > 1.0D ? 1.0D : x);
   }

   public static double smooth01(double x) {
      double c = clamp(x, 0.0D, 1.0D);
      return c * c * (3.0D - 2.0D * c);
   }

   public static double smooth(double e0, double e1, double x) {
      double t = clamp01((x - e0) / (e1 - e0));
      return t * t * (3.0D - 2.0D * t);
   }

   public static double outCubic(double x) {
      double k = 1.0D - clamp01(x);
      return 1.0D - k * k * k;
   }

   public static double[] add(double[] a, double[] b) {
      return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
   }

   public static double[] sub(double[] a, double[] b) {
      return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
   }

   public static double[] mul(double[] a, double k) {
      return new double[]{a[0] * k, a[1] * k, a[2] * k};
   }

   public static double dot(double[] a, double[] b) {
      return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
   }

   public static double[] cross(double[] a, double[] b) {
      return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
   }

   public static double len(double[] a) {
      return Math.sqrt(dot(a, a));
   }

   public static double[] norm(double[] a) {
      double l = len(a);
      return l < 1.0E-9D ? new double[]{0.0D, 0.0D, 0.0D} : new double[]{a[0] / l, a[1] / l, a[2] / l};
   }

   public static double[] lerp(double[] a, double[] b, double t) {
      return new double[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
   }

   public static double dist(double[] a, double[] b) {
      return len(sub(a, b));
   }

   public static double[] fwd(double yawDeg) {
      double y = yawDeg * Math.PI / 180.0D;
      return new double[]{-Math.sin(y), 0.0D, Math.cos(y)};
   }

   public static double[] right(double yawDeg) {
      double y = yawDeg * Math.PI / 180.0D;
      return new double[]{-Math.cos(y), 0.0D, -Math.sin(y)};
   }

   public static double strainStep(double s, double add, boolean still, boolean sneaking) {
      double v = s + add;
      if (add <= 0.0D) {
         if (sneaking) {
            --v;
         } else if (still) {
            v -= 0.5D;
         }
      }

      return clamp(v, 0.0D, 100.0D);
   }

   public static boolean frenzyAt(double strain) {
      return strain >= 100.0D;
   }

   public static double slingSpeed(double ticks) {
      return 3.0D * smooth01(ticks / 40.0D) * 0.85D + 0.44999999999999996D;
   }

   public static int cooldown(int base, double reduction) {
      int c = (int)Math.floor((double)base * (1.0D - reduction));
      return c < 0 ? 0 : c;
   }

   public static boolean light(boolean player, boolean boss, double width, double height) {
      if (player) {
         return true;
      } else {
         return !boss && width <= 0.75D && height <= 2.05D;
      }
   }

   public static double[] aimPoint(double[] feet, double height) {
      return new double[]{feet[0], feet[1] + height * 0.62D, feet[2]};
   }

   public static double[] chest(double[] feet, double height) {
      return new double[]{feet[0], feet[1] + height * 0.72D, feet[2]};
   }

   public static double[] mid(double[] feet, double height) {
      return new double[]{feet[0], feet[1] + height * 0.5D, feet[2]};
   }

   public static double[] knock(double[] vel, double[] victimFeet, double[] from, double kb, double lift, double resist) {
      double[] v = (double[])vel.clone();
      if (from != null && !(kb <= 0.0D)) {
         double keep = 1.0D - clamp01(resist);
         double[] d = sub(victimFeet, from);
         d[1] = 0.0D;
         double l = len(d);
         if (l == 0.0D) {
            l = 1.0D;
         }

         v[0] += d[0] / l * kb * keep;
         v[2] += d[2] / l * kb * keep;
         v[1] = Math.max(v[1], lift * keep);
         return v;
      } else {
         return v;
      }
   }

   public static double[] zipAnchor(double[] chest, double[] casterFeet, double[] targetFeet, double targetWidth) {
      return add(chest, mul(norm(sub(casterFeet, targetFeet)), 1.0D + targetWidth * 0.5D));
   }

   public static double[] zipStart(double[] anchor, double[] feet) {
      return mul(norm(sub(anchor, add(feet, new double[]{0.0D, 1.2D, 0.0D}))), 1.6D);
   }

   public static double[] zipVel(double[] vel, double[] to, double speed) {
      return mul(norm(add(mul(norm(vel), 0.4D), mul(norm(to), 0.6D))), speed);
   }

   public static boolean zipDone(double dist, int age) {
      return dist < 1.4D || age > 30;
   }

   public static boolean zipDone(double dist, int age, int most) {
      return dist < 1.4D || age > most;
   }

   public static int zipMost(double dist) {
      return Math.max(30, (int)Math.ceil(dist / 1.6D) + 8);
   }

   public static int gripTicks(double dist) {
      return 4 + (int)Math.round(Math.max(0.0D, dist - 14.0D) / 21.0D);
   }

   public static int reelTicks(double path) {
      return Math.max(6, (int)Math.ceil(6.0D * path / 12.4D));
   }

   public static double[] zipBounce(double[] to) {
      return add(mul(norm(new double[]{to[0], 0.0D, to[2]}), -0.25D), new double[]{0.0D, 0.35D, 0.0D});
   }

   public static boolean poundable(double[] feet, double[] point, boolean airborne, boolean top) {
      double drop = feet[1] - point[1];
      double run = len(new double[]{point[0] - feet[0], 0.0D, point[2] - feet[2]});
      return airborne && top && drop >= 4.0D && run <= drop * 1.0D;
   }

   public static double poundK(double drop) {
      return clamp01((drop - 4.0D) / 20.0D);
   }

   public static double poundReach(double k) {
      return 4.0D + 5.0D * clamp01(k);
   }

   public static double poundHit(double k, double dist) {
      double full = 6.0D + 10.0D * clamp01(k);
      return full * (1.0D - 0.4D * clamp01(dist / poundReach(k)));
   }

   public static double poundKnock(double k) {
      return 0.6D + 0.7999999999999999D * clamp01(k);
   }

   public static double poundUp(double k) {
      return 0.5D + 0.4D * clamp01(k);
   }

   public static double[] poundVel(double[] to, int age) {
      return mul(norm(to), Math.min(3.2D, 1.2D + 0.5D * (double)Math.max(0, age)));
   }

   public static boolean poundLanded(boolean ground, double aboveAnchor, int stall, int age, double gap) {
      return ground || (aboveAnchor < 0.5D || age >= 6 && stall >= 3 || age > 40) && gap < 1.2D;
   }

   public static boolean poundLost(int age, double gap) {
      return age > 40 && gap >= 1.2D;
   }

   public static double poundCrater(double drop) {
      return drop < 12.0D ? 0.0D : 1.8D + 1.2D * clamp01((drop - 12.0D) / 12.0D);
   }

   public static double[] vaultFlat(double[] to) {
      double x = to[0] == 0.0D ? 1.0E-4D : to[0];
      return norm(new double[]{x, 0.0D, to[2]});
   }

   public static double[] reelStop(double[] casterFeet, double[] reelStart) {
      return add(casterFeet, mul(norm(sub(reelStart, casterFeet)), 1.6D));
   }

   public static double reelK(int sinceGrip, int ticks) {
      return clamp01((double)(sinceGrip - 1) / (double)Math.max(1, ticks));
   }

   public static double reelK(int sinceGrip) {
      return clamp01((double)(sinceGrip - 1) / 6.0D);
   }

   public static double[] reelAt(double[] start, double[] stop, double k) {
      double e = 1.0D - Math.pow(1.0D - k, 2.2D);
      double[] p = lerp(start, stop, e);
      p[1] = start[1] + Math.sin(Math.PI * k) * 0.9D;
      return p;
   }

   public static double[] swingEntry(double[] vel, double[] hand, double[] anchor) {
      double l = dist(hand, anchor);
      double[] v = (double[])vel.clone();
      double sp = len(v);
      double[] rope = mul(sub(anchor, hand), 1.0D / (l == 0.0D ? 1.0D : l));
      double[] tan = sub(v, mul(rope, dot(v, rope)));
      if (len(tan) > 1.0E-4D) {
         v = mul(norm(tan), sp);
      }

      return v;
   }

   public static double[] swingPush(double yawDeg, double forward, double right) {
      double y = yawDeg * Math.PI / 180.0D;
      double x = -Math.sin(y) * forward - Math.cos(y) * right;
      double z = Math.cos(y) * forward - Math.sin(y) * right;
      double l = Math.sqrt(x * x + z * z);
      if (l > 1.0D) {
         x /= l;
         z /= l;
      }

      return new double[]{x * 0.02D, 0.0D, z * 0.02D};
   }

   public static double swingHeight(double[] hand, double[] vel, double[] anchor, double rope) {
      return Math.max(0.0D, hand[1] - (anchor[1] - rope)) + 0.5D * dot(vel, vel) / 0.09D;
   }

   public static double swingDamp(double height, boolean help, boolean pushing, boolean carry) {
      if (carry) {
         return 0.999D;
      } else if (help) {
         return 0.998D;
      } else {
         return !pushing && height < 0.2D ? 0.92D : 0.972D;
      }
   }

   public static double[][] swingMove(double[] hand, double[] vel, double[] anchor, double rope, double[] push, double damp, double give) {
      int n = 8;
      double k = Math.pow(damp, 1.0D / (double)n);
      double[] h = (double[])hand.clone();
      double[] v = (double[])vel.clone();

      for(int i = 0; i < n; ++i) {
         v = new double[]{(v[0] + push[0] / (double)n) * k, (v[1] - 0.09D / (double)n + push[1] / (double)n) * k, (v[2] + push[2] / (double)n) * k};
         double[] nh = new double[]{h[0] + v[0] / (double)n, h[1] + v[1] / (double)n, h[2] + v[2] / (double)n};
         double[] d = sub(nh, anchor);
         double dl = len(d);
         if (dl > rope) {
            double[] r = mul(d, 1.0D / dl);
            nh = add(anchor, mul(r, rope));
            double out = dot(v, r);
            if (out > give) {
               v = sub(v, mul(r, out - give));
            }
         }

         h = nh;
      }

      double sp = len(v);
      if (sp > 1.6D) {
         v = mul(v, 1.6D / sp);
      }

      return new double[][]{h, v};
   }

   public static double swingRope(double rope, double target) {
      return rope > target ? Math.max(target, rope - 0.45D) : Math.min(target, rope + 0.3D);
   }

   public static double swingAim(double target, double dist, boolean reelIn, boolean letOut) {
      if (reelIn && !letOut) {
         return Math.max(1.0D, Math.min(target, dist) - 0.3D);
      } else {
         return letOut && !reelIn ? Math.min(56.0D, Math.max(1.0D, Math.min(target, dist)) + 0.3D) : target;
      }
   }

   public static boolean swingCarry(boolean carry, boolean pushing, double vyBefore, double vyAfter, int age) {
      return carry && !pushing && (!(vyBefore > 0.0D) || !(vyAfter <= 0.0D)) && age < 60;
   }

   public static int swingStill(int still, double height, boolean pushing) {
      if (!pushing && !(height > 0.25D)) {
         return height < 0.06D ? still + 1 : still;
      } else {
         return 0;
      }
   }

   public static boolean swingHanging(int still) {
      return still >= 6;
   }

   public static int swingPose(boolean hanging, double forward, double right) {
      if (forward == 0.0D && right == 0.0D) {
         return hanging ? 1 : 0;
      } else if (Math.abs(forward) >= Math.abs(right)) {
         return forward > 0.0D ? 2 : 3;
      } else {
         return right > 0.0D ? 4 : 5;
      }
   }

   public static boolean swingFlip(double[] vel) {
      return len(vel) >= 0.5D && vel[1] >= 0.1D;
   }

   public static double swingFling(double[] vel) {
      return smooth(0.2D, 0.5D, len(vel));
   }

   public static double[] swingLetGo(double[] vel) {
      double k = swingFling(vel);
      double[] v = mul(vel, 1.0D + 0.19999999999999996D * k);
      v[1] += 0.24D * k + (swingFlip(vel) ? 0.15D : 0.0D);
      return v;
   }

   public static double[][] swingTrack(double[] hand) {
      double[][] t = new double[16][];

      for(int i = 0; i < 16; ++i) {
         t[i] = (double[])hand.clone();
      }

      return t;
   }

   public static double[] swingSeen(double[][] track, double[] seen) {
      int best = 0;
      double near = Double.MAX_VALUE;

      for(int i = 0; i < track.length; ++i) {
         double d = len(sub(seen, track[i]));
         if (d < near) {
            near = d;
            best = i;
         }
      }

      return sub(seen, track[best]);
   }

   public static double[][] swingShift(double[][] track, double[] by) {
      double[][] t = new double[track.length][];

      for(int i = 0; i < track.length; ++i) {
         t[i] = add(track[i], by);
      }

      return t;
   }

   public static double[][] swingAdd(double[][] track, double[] next) {
      double[][] t = new double[track.length][];
      t[0] = (double[])next.clone();

      for(int i = 1; i < track.length; ++i) {
         t[i] = (double[])track[i - 1].clone();
      }

      return t;
   }

   public static boolean swingSnap(double[] off, double dist, double rope) {
      return len(off) > 6.0D && dist > rope + 6.0D;
   }

   public static double[] swingHold(double[] hand, double[] move, double[] anchor, double rope) {
      double[] d = sub(add(hand, move), anchor);
      double l = len(d);
      double most = rope + 0.02D;
      return l <= most ? (double[])move.clone() : sub(add(anchor, mul(d, most / l)), hand);
   }

   public static boolean swingHigh(double[] hand, double[] anchor) {
      return anchor[1] - hand[1] >= 3.0D;
   }

   public static double swingTarget(double len, double rise, boolean ground) {
      return !ground ? len : Math.min(len, Math.max(2.0D, Math.min(len * 0.8D, rise - 1.0D)));
   }

   public static double[] swingKick(double[] look, double[] hand, double[] anchor) {
      double[] flat = norm(new double[]{look[0], 0.0D, look[2]});
      return swingEntry(mul(flat, 0.3D), hand, anchor);
   }

   public static double[] swingLean(double[] rope, double yawDeg) {
      double[] f = fwd(yawDeg);
      double[] r = right(yawDeg);
      return new double[]{Math.asin(clamp(dot(rope, f), -1.0D, 1.0D)) * 0.9D, -Math.asin(clamp(dot(rope, r), -1.0D, 1.0D)) * 0.9D};
   }

   public static double leanStep(double w, boolean on) {
      return on ? Math.min(1.0D, w + 0.3333333333333333D) : Math.max(0.0D, w - 0.25D);
   }

   public static double[] leanQuat(double[] lean) {
      double hx = -lean[0] * 0.5D;
      double hz = lean[1] * 0.5D;
      double cx = Math.cos(hx);
      double sx = Math.sin(hx);
      double cz = Math.cos(hz);
      double sz = Math.sin(hz);
      return new double[]{sx * cz, -sx * sz, cx * sz, cx * cz};
   }

   public static double[] qrot(double[] q, double[] v) {
      double[] u = new double[]{q[0], q[1], q[2]};
      double[] t = mul(cross(u, v), 2.0D);
      return add(add(v, mul(t, q[3])), cross(u, t));
   }

   public static double[] leanTop(double[] lean, double yawDeg) {
      double[] t = qrot(leanQuat(lean), new double[]{0.0D, 1.0D, 0.0D});
      double a = (180.0D - yawDeg) * Math.PI / 180.0D;
      return new double[]{t[0] * Math.cos(a) + t[2] * Math.sin(a), t[1], -t[0] * Math.sin(a) + t[2] * Math.cos(a)};
   }

   public static double aimLift(double pitchDeg) {
      return Math.tan(Math.toRadians(clamp(-pitchDeg, 0.0D, 60.0D)));
   }

   public static double[] slingDir(double[] left, double[] right, double[] feet, double pitchDeg) {
      double[] mid = mul(add(left, right), 0.5D);
      double[] dir = sub(mid, add(feet, new double[]{0.0D, 1.2D, 0.0D}));
      double lift = Math.max(dir[1] < 0.0D ? 0.7D : 0.5D, aimLift(pitchDeg));
      dir[1] = Math.max(dir[1], len(new double[]{dir[0], 0.0D, dir[2]}) * lift);
      return norm(dir);
   }

   public static int slingTries() {
      return SLING_LIFTS.length + SLING_DOWN.length;
   }

   public static float slingPitch(float look, int k) {
      float p = k < SLING_LIFTS.length ? look + SLING_LIFTS[k] : SLING_DOWN[Math.min(SLING_DOWN.length - 1, k - SLING_LIFTS.length)];
      return Math.max(-89.0F, Math.min(89.0F, p));
   }

   public static double slingCharge(int held) {
      return (double)Math.min(40, Math.max(0, held));
   }

   public static double[] holdSpot(double[] feet, double yawDeg, int slot, int count) {
      double[] f = fwd(yawDeg);
      double[] r = right(yawDeg);
      if (slot >= 2) {
         double[] h = HOLD_AT[Math.min(HOLD_AT.length - 1, slot - 2)];
         return add(feet, add(mul(f, h[0]), add(mul(r, h[1]), new double[]{0.0D, h[2], 0.0D})));
      } else {
         double side = slot == 0 ? (count > 1 ? 0.9D : 0.2D) : -0.9D;
         return add(feet, add(mul(f, 1.9D), add(mul(r, side), new double[]{0.0D, 1.7D, 0.0D})));
      }
   }

   public static int bindMost(boolean twin) {
      return twin ? 6 : 3;
   }

   public static int freeSlot(boolean[] used, int most) {
      for(int i = 0; i < most && i < used.length; ++i) {
         if (!used[i]) {
            return i;
         }
      }

      return -1;
   }

   public static int holdLeft(long since) {
      return (int)Math.max(0L, 600L - since);
   }

   public static double[] holdAt(double[] from, double[] spot, int age, double t, int slot) {
      double k = clamp01((double)age / 6.0D);
      double[] p = lerp(from, spot, outCubic(k));
      p[1] += Math.sin(t * 0.3D + (double)slot) * 0.05D;
      return p;
   }

   public static boolean bindBroken(double hpAtBind, double hpNow, int presses) {
      return hpAtBind - hpNow >= 12.0D || presses >= 8;
   }

   public static double[] windupSpot(double[] feet, double yawDeg, int slot) {
      double[] f = fwd(yawDeg);
      double[] r = right(yawDeg);
      if (slot < 2) {
         return add(feet, add(mul(f, -0.5D), add(mul(r, 0.7D - (double)slot * 1.4D), new double[]{0.0D, 2.7D, 0.0D})));
      } else {
         int row = slot / 2;
         double side = (slot % 2 == 0 ? 1.0D : -1.0D) * (0.7D + (double)row * 1.1D);
         return add(feet, add(mul(f, -0.5D - (double)row * 0.4D), add(mul(r, side), new double[]{0.0D, 2.7D + (double)row * 0.7D, 0.0D})));
      }
   }

   public static double[] windupAt(double[] from, double[] back, int age) {
      return lerp(from, back, smooth(0.0D, 1.0D, (double)(age + 1) / 4.0D));
   }

   public static double[] throwVel(double[] at, double[] from, double height) {
      double[] dir = sub(at, from);
      dir[1] = 0.0D;
      double d = len(dir);
      dir = norm(dir);
      double reach = 1.0D - d * 0.09D / 1.6D;
      double n = reach > 0.05D ? clamp(Math.log(reach) / Math.log(0.91D), 3.0D, 20.0D) : 20.0D;
      double vy = clamp((at[1] + height * 0.5D - from[1] + 0.04D * n * n) / n, -0.2D, 0.6D);
      return add(mul(dir, 1.6D), new double[]{0.0D, vy, 0.0D});
   }

   public static double[] slamSpot(double[] feet, double yawDeg, int slot) {
      return add(feet, mul(fwd(yawDeg), 1.8D + (double)slot * 0.6D));
   }

   public static double[] slamAt(double[] from, double[] to, int age) {
      double[] top = add(from, new double[]{0.0D, 1.1D, 0.0D});
      return age < 2 ? lerp(from, top, outCubic((double)(age + 1) / 2.0D)) : lerp(top, to, Math.pow((double)(age - 1) / 3.0D, 2.0D));
   }

   public static boolean whipFront(double[] casterFeet, double[] foeFeet, double yawDeg) {
      return dot(norm(sub(foeFeet, casterFeet)), fwd(yawDeg)) > 0.3D;
   }

   public static double whipRank(boolean used, double dist) {
      return (used ? 10.0D : 0.0D) + dist;
   }

   public static double[] whipMiss(double[] feet, double yawDeg) {
      return add(feet, add(mul(fwd(yawDeg), 5.6000000000000005D), new double[]{0.0D, 1.0D, 0.0D}));
   }

   public static boolean wardLash(int left) {
      return left > 0 && left % 10 == 0;
   }

   public static boolean inWard(double[] p, double[] feet) {
      return dist(p, add(feet, new double[]{0.0D, 1.0D, 0.0D})) < 2.4D;
   }

   public static boolean stormStrike(int age, int left, int i) {
      return age >= 4 && left > 8 && (age - 4 - i * 2) % 12 == 0;
   }

   public static int stormPick(int i, int age, int count) {
      return count <= 0 ? -1 : (i + age / 12) % count;
   }

   public static double[] stormGround(double[] feet, int i, int age) {
      int beat = Math.max(0, age) / 12;
      double a = (double)i * (Math.PI / 4D) + (double)beat * 0.9D + (double)(i % 2) * 0.35D;
      double r = 3.0D + 4.5D * ((double)((i * 5 + beat * 3) % 7) / 6.0D);
      return new double[]{feet[0] + Math.cos(a) * r, feet[1], feet[2] + Math.sin(a) * r};
   }

   public static boolean stormReady(double strain, boolean open, boolean free, boolean cooled) {
      return open && free && cooled && strain >= 50.0D;
   }

   public static boolean stormRearm(double strain) {
      return strain < 45.0D;
   }

   public static boolean frenzyBeat(int age) {
      return age >= 0 && age < 120 && age % 5 == 0;
   }

   public static int frenzyTendril(int age, int j) {
      return (Math.max(0, age) / 5 * 2 + j) % 8;
   }

   public static boolean frenzyFeed(int age) {
      return age >= 0 && age % 40 == 0 && age / 40 < 3;
   }

   public static double frenzyFeedAmount(double health, double absorb) {
      return clamp(health + absorb - 1.0D, 0.0D, 2.0D);
   }

   public static boolean frenzyFree(long lastStruck, long now) {
      return now - lastStruck >= 10L;
   }

   public static int frenzyStroke() {
      return 20;
   }

   public static double[] frenzyGround(double[] feet, int i, int age) {
      int beat = Math.max(0, age) / 5;
      double a = (double)i * (Math.PI / 4D) + (double)beat * 1.3D + (double)(i % 2) * 0.4D;
      double r = 2.5D + 4.0D * ((double)((i * 3 + beat * 5) % 9) / 8.0D);
      return new double[]{feet[0] + Math.cos(a) * r, feet[1], feet[2] + Math.sin(a) * r};
   }

   public static double frenzyHit(boolean ally) {
      return 4.0D * (ally ? 0.5D : 1.0D);
   }

   public static boolean catchWants(double vy, double dist, boolean onGround) {
      return vy < -0.55D && dist < 16.0D && !onGround;
   }

   public static double catchK(int sinceGrip) {
      return clamp01((double)sinceGrip / 24.0D);
   }

   public static double catchY(double from, double ground, double k) {
      double e = 1.0D - Math.pow(1.0D - k, 2.0D);
      return from + (ground - from) * e;
   }

   public static boolean catchFlail(double k) {
      return k < 0.4D;
   }

   public static boolean windReady(long nowMs, long usedMs) {
      return usedMs <= 0L || nowMs - usedMs >= 86400000L || nowMs < usedMs;
   }

   public static long windLeftMs(long nowMs, long usedMs) {
      return windReady(nowMs, usedMs) ? 0L : 86400000L - (nowMs - usedMs);
   }

   public static boolean windBeat(long tick) {
      return tick % 15L == 0L;
   }

   public static int windPick(long tick, int count) {
      return (int)(tick / 15L % (long)Math.max(1, count));
   }

   public static int windSlot(long tick) {
      return (int)(tick / 15L % 5L);
   }

   public static double windHit(double base, boolean ally) {
      return base * (ally ? 0.5D : 1.0D);
   }

   public static double dealt(boolean raging) {
      return raging ? 1.3D : 1.0D;
   }

   public static double takenCut(boolean warding, boolean raging) {
      double cut = raging ? 0.3D : 0.0D;
      if (warding) {
         cut = Math.max(cut, 0.6D);
      }

      return cut;
   }

   public static double reelHit(boolean raging) {
      return raging ? 4.0D : 2.0D;
   }

   public static boolean rageHoldBeat(int sinceHeld) {
      return sinceHeld % 20 == 19;
   }

   public static double rageSpeed(boolean raging) {
      return raging ? 1.15D : 1.0D;
   }

   public static double veinsStep(int phase, double v) {
      if (phase == 2) {
         return Math.min(1.0D, v + 0.06D);
      } else if (phase == 3) {
         return Math.min(1.0D, v + 0.05D);
      } else {
         return phase == 4 ? Math.max(0.0D, v - 0.04D) : v;
      }
   }

   public static double downedK(int left) {
      return 1.0D - (double)left / 200.0D;
   }

   public static int downedSecs(int left) {
      return Math.max(0, (int)Math.ceil((double)left / 20.0D));
   }

   public static int holdTogetherCount(int left) {
      double prog = 1.0D - (double)left / 200.0D;
      return 3 + (int)Math.floor(prog * 7.0D);
   }

   public static String mmss(int ticks) {
      int s = Math.max(0, (int)Math.ceil((double)ticks / 20.0D));
      int r = s % 60;
      return s / 60 + ":" + (r < 10 ? "0" : "") + r;
   }
}
