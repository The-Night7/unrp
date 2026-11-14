package com.myriam.unrp.uo;



public final class SaltMath {
   public static final int WEIGHT = 8;
   public static final int AURA_BASE = 150;
   public static final int REGEN = 11;
   public static final double SLOW_AT = 50.0D;
   public static final double PILLAR_AT = 100.0D;
   public static final double PILLAR_DAMAGE = 0.5D;
   public static final double UNDEAD = 2.0D;
   public static final int DECAY_AFTER = 60;
   public static final double DECAY = 5.0D;
   public static final double WATER = 20.0D;
   public static final double RAIN = 10.0D;
   public static final double STRUGGLE_FROM = 70.0D;
   public static final int PRESSES = 14;
   public static final double STRUGGLE_DECAY = 0.6D;
   public static final int PILLAR_TICKS = 160;
   public static final double RESET = 40.0D;
   public static final double STRUGGLE_DROP = 45.0D;
   public static final int LANCE_SLOT = 1;
   public static final int LANCE_COST = 10;
   public static final int LANCE_COOLDOWN = 40;
   public static final double LANCE_SALTING = 25.0D;
   public static final int LANCE_PIERCE = 2;
   public static final double LANCE_SPEED = 2.2D;
   public static final double LANCE_DAMAGE = 4.0D;
   public static final int LANCE_THROW = 8;
   public static final int LANCE_LIFE = 22;
   public static final int GREAT_COST = 35;
   public static final int GREAT_COOLDOWN = 600;
   public static final int GREAT_WINDUP = 24;
   public static final double GREAT_LENGTH = 6.0D;
   public static final double GREAT_SPEED = 2.4D;
   public static final double GREAT_DAMAGE = 18.0D;
   public static final double GREAT_SALTING = 60.0D;
   public static final double GREAT_BURST = 3.0D;
   public static final double GREAT_BURST_SALTING = 20.0D;
   public static final double GREAT_BURST_HIT = 3.0D;
   public static final int GREAT_THROW = 28;
   public static final int GREAT_LIFE = 40;
   public static final double EDGE_UNDEAD_BONUS = 0.2D;
   public static final double EDGE_SALTING = 8.0D;
   public static final int WALL_SLOT = 2;
   public static final int WALL_COST = 20;
   public static final int WALL_COOLDOWN = 300;
   public static final int WALL_LENGTH = 7;
   public static final int WALL_HEIGHT = 3;
   public static final int WALL_TICKS = 200;
   public static final int WALL_START = 5;
   public static final int WALL_WET = 14;
   public static final double WALL_AHEAD = 3.0D;
   public static final int SPIRES_SLOT = 3;
   public static final int SPIRES_COST = 30;
   public static final int SPIRES_COOLDOWN = 440;
   public static final double SPIRES_LENGTH = 18.0D;
   public static final double SPIRES_CONE = 50.0D;
   public static final double SPIRES_SPEED = 1.5D;
   public static final double SPIRES_DAMAGE = 8.0D;
   public static final double SPIRES_SALTING = 35.0D;
   public static final double SPIRES_LAUNCH = 0.8D;
   public static final int SPIRES_STAND = 60;
   public static final double SPIRES_SHATTER = 20.0D;
   public static final int SPIRES_AT = 5;
   public static final double SPIRES_BELOW = 4.5D;
   public static final double SPIRES_ABOVE = 3.5D;
   public static final int SEAL_COST = 22;
   public static final int SEAL_COOLDOWN = 320;
   public static final double SEAL_RANGE = 20.0D;
   public static final int SEAL_ROOT = 60;
   public static final double SEAL_SALTING = 45.0D;
   public static final double SEAL_SHATTER = 24.0D;
   public static final int SEAL_AT = 6;
   public static final int WAKE_SLOT = 4;
   public static final int WAKE_COST = 55;
   public static final int WAKE_COOLDOWN = 2400;
   public static final int WAKE_TICKS = 1200;
   public static final double WAKE_DEALT = 0.25D;
   public static final double WAKE_SALTING = 10.0D;
   public static final int WAKE_WEAK = 400;
   public static final double WAKE_ALLY = 0.5D;
   public static final double WAKE_HOVER = 3.0D;
   public static final double WAKE_LIFT = 0.16D;
   public static final double WINGS_SIZE = 1.5D;
   public static final int WINGS_FOLD = 24;
   public static final int GROUND_REACH = 24;
   public static final double LIFT_STEP = 0.05D;
   public static final int LIFT_MIN = -5;
   public static final int LIFT_MAX = 3;
   public static final double TRUMPET_RADIUS = 15.0D;
   public static final double TRUMPET_SALTING = 60.0D;
   public static final int TRUMPET_AT = 8;
   public static final double TRUMPET_SMITE = 8.0D;
   public static final double TRUMPET_ALLY_HIT = 4.0D;
   public static final int SHEARS_SLOW = 60;
   public static final int STRUGGLE_SLOW = 30;
   public static final int WET_NONE = 0;
   public static final int WET_WATER = 1;
   public static final int WET_RAIN = 2;
   public static final long UNBOUND_DAY_MS = 86400000L;
   public static final double UNBOUND_BLAST = 30.0D;
   public static final double UNBOUND_BLAST_RADIUS = 12.0D;
   public static final int UNBOUND_BLAST_AT = 8;
   public static final double UNBOUND_BLAST_DAMAGE = 4.0D;
   public static final double UNBOUND_BLAST_SHATTER = 6.0D;
   public static final double UNBOUND_BLAST_FLAT = 2.45D;
   public static final double UNBOUND_BLAST_UP = 1.0D;
   public static final int UNBOUND_CUTSCENE = 60;
   public static final int UNBOUND_TICKS = 6000;
   public static final double UNBOUND_HP_MULT = 2.0D;
   public static final double UNBOUND_CUT = 0.5D;
   public static final double UNBOUND_SPEED = 0.36D;
   public static final double UNBOUND_WAKE_HP = 0.3D;
   public static final double UNBOUND_SEEK = 32.0D;
   public static final double UNBOUND_SEEK_UP = 16.0D;
   public static final int REND_COOLDOWN = 240;
   public static final double REND_LENGTH = 60.0D;
   public static final double REND_HEIGHT = 50.0D;
   public static final double REND_DAMAGE = 20.0D;
   public static final double REND_SALTING = 50.0D;
   public static final double REND_SPEED = 2.0D;
   public static final int REND_AT = 8;
   public static final int REND_BUSY = 14;
   public static final double REND_SIDE = 1.8D;
   public static final double REND_SLACK = 0.6D;
   public static final double REND_AHEAD = 1.2D;
   public static final double REND_MIN = 9.0D;
   public static final double REND_KNOCK = 0.9D;
   public static final double REND_LIFT = 1.0D;
   public static final int REND_FADE = 50;
   public static final int HOWL_COOLDOWN = 300;
   public static final double HOWL_RADIUS = 10.0D;
   public static final int HOWL_ATTACKERS = 2;
   public static final int HOWL_WINDOW = 100;
   public static final int HOWL_AT = 4;
   public static final int HOWL_BUSY = 18;
   public static final double HOWL_FLAT = 1.5D;
   public static final double HOWL_UP = 0.75D;
   public static final double HOWL_DAMAGE = 2.0D;
   public static final int AMBUSH_COOLDOWN = 120;
   public static final double AMBUSH_DAMAGE = 12.0D;
   public static final double AMBUSH_MIN = 4.0D;
   public static final double AMBUSH_MAX = 14.0D;
   public static final int AMBUSH_BLINK = 4;
   public static final int AMBUSH_STRIKE = 9;
   public static final int AMBUSH_BUSY = 12;
   public static final double AMBUSH_BEHIND = 1.6D;
   public static final double AMBUSH_SALTING = 15.0D;
   public static final double AMBUSH_KNOCK = 0.8D;
   public static final double AMBUSH_LIFT = 0.4D;
   public static final int THORNS_COOLDOWN = 200;
   public static final double THORNS_RADIUS = 8.0D;
   public static final double THORNS_DAMAGE = 14.0D;
   public static final double THORNS_SALTING = 30.0D;
   public static final int THORNS_CROWD = 3;
   public static final double THORNS_NEAR = 0.7D;
   public static final int THORNS_AT = 5;
   public static final int THORNS_BUSY = 14;
   public static final double THORNS_KNOCK = 0.5D;
   public static final double THORNS_LIFT = 0.7D;
   public static final int SPEAR_EVERY = 20;
   public static final double SPEAR_DAMAGE = 10.0D;
   public static final double SPEAR_REACH = 2.4D;
   public static final double SPEAR_HIT_REACH = 3.2D;
   public static final int SPEAR_AT = 4;
   public static final double SPEAR_SALTING = 10.0D;
   public static final double SPEAR_KNOCK = 0.6D;
   public static final double SPEAR_LIFT = 0.35D;
   public static final int REND_FIRST = 40;
   public static final int HOWL_FIRST = 0;
   public static final int AMBUSH_FIRST = 30;
   public static final int THORNS_FIRST = 60;
   public static final int SPEAR_FIRST = 0;
   public static final double SCORE_HIT = 7.0D;
   public static final double SCORE_WEAK = 3.0D;
   public static final double SCORE_WEAK_HP = 8.0D;
   public static final double CLIMB_UP = 0.32D;
   public static final double CLIMB_FLAT = 0.06D;
   public static final double LEAP_UP = 0.55D;
   public static final double LEAP_BOOST = 1.4D;
   public static final double WALL_PEEK = 0.9D;
   public static final double WALL_STEP = 0.6D;
   public static final double LEAP_PEEK = 1.8D;
   public static final double BUSY_SLOW = 0.5D;
   public static final double STRIKE_SLOW = 0.3D;
   public static final int SMASH_EVERY = 6;
   public static final int SMASH_AT = 4;
   public static final double VEINS_RISE = 0.05D;
   public static final double VEINS_FALL = 0.04D;
   public static final double TREE_SIZE = 3.3D;
   public static final int TREE_SEED = 11;
   public static final double TREE_STAGGER = 1.4D;
   public static final double TREE_SPAN = 3.0D;
   public static final double TREE_GROW = 1.6D;
   public static final int TREE_BORN = 7;
   public static final double TREE_SHRINK = 24.0D;
   public static final int ACT_HOWL = 1;
   public static final int ACT_THORNS = 2;
   public static final int ACT_REND = 3;
   public static final int ACT_AMBUSH = 4;
   public static final int ACT_STRIKE = 5;
   public static final int ACT_CLOSE = 6;
   public static final int ACT_RUN = 7;
   public static final int WALL_NONE = 0;
   public static final int WALL_BUILT = 1;
   public static final int WALL_RAW = 2;

   private SaltMath() {
   }

   public static double clamp(double x, double a, double b) {
      return x < a ? a : (x > b ? b : x);
   }

   public static double clamp01(double x) {
      return clamp(x, 0.0D, 1.0D);
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

   public static double[] right(double yaw) {
      double y = Math.toRadians(yaw);
      return new double[]{-Math.cos(y), 0.0D, -Math.sin(y)};
   }

   public static double[] fwd(double yaw) {
      double y = Math.toRadians(yaw);
      return new double[]{-Math.sin(y), 0.0D, Math.cos(y)};
   }

   public static double[] flat(double[] v) {
      return norm(new double[]{v[0], 0.0D, v[2]});
   }

   public static double saltAdd(double cur, double amount, boolean undead, boolean boss) {
      double a = amount * (undead ? 2.0D : 1.0D);
      double v = cur + a;
      if (boss) {
         v = Math.min(v, 99.0D);
      }

      return clamp(v, 0.0D, 100.0D);
   }

   public static double saltStep(double cur, int sinceHit, int wet) {
      double per = 0.0D;
      if (wet == 1) {
         per = 20.0D;
      } else if (wet == 2) {
         per = 10.0D;
      } else if (sinceHit > 60) {
         per = 5.0D;
      }

      return clamp(cur - per / 20.0D, 0.0D, 100.0D);
   }

   public static int saltStage(double v) {
      return v >= 100.0D ? 2 : (v >= 50.0D ? 1 : 0);
   }

   public static boolean slowed(double v) {
      return v >= 50.0D;
   }

   public static boolean struggles(double v) {
      return v >= 70.0D;
   }

   public static double struggleDecayPresses() {
      return 0.084D;
   }

   public static boolean struggleDropped(boolean pillar, double salt) {
      return !pillar && salt < 45.0D;
   }

   public static double taken(boolean pillar) {
      return pillar ? 1.5D : 1.0D;
   }

   public static double edgeHit(double base, boolean undead) {
      return base * (undead ? 1.2D : 1.0D);
   }

   public static double[] lanceFrom(double[] feet, double yaw, double scale) {
      return add(feet, add(new double[]{0.0D, 1.55D * scale, 0.0D}, mul(right(yaw), 0.3D)));
   }

   public static double[] lanceAt(double[] from, double[] dir, double age) {
      return add(from, mul(dir, 2.2D * age));
   }

   public static double[] lanceHit(double[] from, double[] dir, double age, double[] mid, double width) {
      double[] rel = sub(mid, from);
      double along = dot(rel, dir);
      if (!(along < 0.0D) && !(along > 2.2D * age + 0.3D)) {
         double[] closest = add(from, mul(dir, along));
         return dist(closest, mid) < 0.45D + width * 0.5D ? closest : null;
      } else {
         return null;
      }
   }

   public static double[] greatFrom(double[] feet, double yaw, double scale) {
      return add(feet, add(new double[]{0.0D, 2.9D * scale, 0.0D}, add(mul(right(yaw), 0.35D), mul(fwd(yaw), 0.8D))));
   }

   public static double[] greatTip(double[] from, double[] dir, double age) {
      return add(from, mul(dir, 2.4D * age));
   }

   public static double[] greatHit(double[] from, double[] dir, double age, double[] feet, double height, double width) {
      double best = 1.0E9D;
      double[] closest = null;
      double[] ks = new double[]{0.3D, 0.55D, 0.9D};

      for(double k : ks) {
         double[] m = new double[]{feet[0], feet[1] + height * k, feet[2]};
         double along = dot(sub(m, from), dir);
         if (!(along < 0.0D) && !(along > 2.4D * age + 0.4D)) {
            double[] c = add(from, mul(dir, along));
            double dd = dist(c, m);
            if (dd < best) {
               best = dd;
               closest = c;
            }
         }
      }

      return closest != null && !(best > 0.9D + width * 0.5D) ? closest : null;
   }

   public static boolean greatBurstReaches(double[] tip, double[] mid) {
      return dist(mid, tip) <= 3.5D;
   }

   public static int[][] wallCols(double[] feet, double yaw) {
      double[] f = fwd(yaw);
      double[] r = right(yaw);
      double[] c = add(feet, mul(f, 3.0D));
      int[][] cols = new int[7][];

      for(int i = 0; i < 7; ++i) {
         double off = (double)i - 3.0D;
         double[] p = add(c, mul(r, off));
         cols[i] = new int[]{(int)Math.floor(p[0]), (int)Math.floor(feet[1]), (int)Math.floor(p[2])};
      }

      return cols;
   }

   public static int wallRiseAt(int col, int h) {
      return 5 + Math.abs(col - 3) + h;
   }

   public static int wallCrumbleAt(boolean inWater) {
      return inWater ? 14 : 205;
   }

   public static int crumbleDelay(int order) {
      return order / 7 * 3;
   }

   public static double spiresFront(int age) {
      return 1.5D * (double)(age + 1);
   }

   public static boolean spiresRunning(int age) {
      return spiresFront(age) <= 18.1D;
   }

   public static double spiresHalf() {
      return 0.4363323129985824D;
   }

   public static double spiresWidth(double front) {
      return 2.0D * front * Math.tan(spiresHalf()) + 1.0D;
   }

   public static int spiresCount(double front) {
      return Math.max(1, (int)Math.round(spiresWidth(front) / 1.15D));
   }

   public static boolean spiresHit(double[] origin, double[] dir, double[] side, int age, double[] feet) {
      double front = spiresFront(age);
      double[] rel = sub(feet, origin);
      double along = dot(rel, dir);
      double lat = Math.abs(dot(rel, side));
      if (!(along < 0.3D) && !(along > front + 0.6D) && !(along < front - 1.5D - 0.9D)) {
         return lat <= along * Math.tan(spiresHalf()) + 0.9D;
      } else {
         return false;
      }
   }

   public static int spiresLife() {
      return 88;
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

   public static double[] mid(double[] feet, double height) {
      return new double[]{feet[0], feet[1] + height * 0.5D, feet[2]};
   }

   public static double[] chest(double[] feet, double height) {
      return new double[]{feet[0], feet[1] + height * 0.72D, feet[2]};
   }

   public static int cooldown(int base, double reduction) {
      int c = (int)Math.floor((double)base * (1.0D - reduction));
      return c < 0 ? 0 : c;
   }

   public static double hoverTarget(double groundY, long tick) {
      return groundY + 3.0D - 0.4D + 0.25D * Math.sin((double)tick * 0.09D);
   }

   public static double hoverVel(double target, double y) {
      return clamp((target - y) * 0.12D, -0.2D, 0.2D);
   }

   public static int liftAmp(double vy) {
      long a = Math.round(vy / 0.05D) - 1L;
      return (int)Math.max(-5L, Math.min(3L, a));
   }

   public static double liftStep(double vy, int amp) {
      return vy + (0.05D * (double)(amp + 1) - vy) * 0.2D;
   }

   public static double wakeBonus(boolean waking) {
      return waking ? 1.25D : 1.0D;
   }

   public static double wakeSalt(boolean waking) {
      return waking ? 10.0D : 0.0D;
   }

   public static boolean spares(boolean ally, boolean waking) {
      return ally && !waking;
   }

   public static double allyCut(boolean ally) {
      return ally ? 0.5D : 1.0D;
   }

   public static boolean trumpetReaches(double[] at, double[] mid, double width) {
      return dist(mid, at) <= 15.0D + width * 0.5D;
   }

   public static boolean unboundReady(long nowMs, long usedMs) {
      return usedMs <= 0L || nowMs - usedMs >= 86400000L || nowMs < usedMs;
   }

   public static boolean blastReaches(double[] caster, double[] feet) {
      double[] d = sub(feet, caster);
      d[1] = 0.0D;
      return len(d) <= 12.0D;
   }

   public static double[] blastVel(double[] caster, double[] feet) {
      double[] d = sub(feet, caster);
      d[1] = 0.0D;
      double l = len(d);
      d = l < 0.01D ? new double[]{1.0D, 0.0D, 0.0D} : mul(d, 1.0D / l);
      return new double[]{d[0] * 2.45D, 1.0D, d[2] * 2.45D};
   }

   public static double score(double dist, boolean hitLately, double health) {
      double sc = dist;
      if (hitLately) {
         sc = dist - 7.0D;
      }

      if (health < 8.0D) {
         sc -= 3.0D;
      }

      return sc;
   }

   public static boolean crowds(double dist) {
      return dist < 5.6D;
   }

   public static int decide(int recent, int crowd, double d, int cdHowl, int cdThorns, int cdRend, int cdAmbush, int cdSpear) {
      if (recent >= 2 && cdHowl <= 0) {
         return 1;
      } else if (crowd >= 3 && cdThorns <= 0) {
         return 2;
      } else if (cdRend <= 0 && d > 9.0D && d < 60.0D) {
         return 3;
      } else if (cdAmbush <= 0 && d > 4.0D && d < 14.0D) {
         return 4;
      } else if (d < 2.4D) {
         return cdSpear <= 0 ? 5 : 6;
      } else {
         return 7;
      }
   }

   public static double[] runVel(double[] dir, double[] vel, boolean onGround, int wall, boolean water) {
      double[] v = (double[])vel.clone();
      if (wall == 1) {
         v[0] = 0.0D;
         v[2] = 0.0D;
         return v;
      } else if (wall == 2) {
         v[0] = dir[0] * 0.06D;
         v[2] = dir[2] * 0.06D;
         v[1] = 0.32D;
         return v;
      } else {
         double sp = 0.36D;
         if (water && onGround) {
            v[1] = 0.55D;
            sp = 0.504D;
         }

         if (onGround || !water) {
            v[0] = dir[0] * sp;
            v[2] = dir[2] * sp;
         }

         return v;
      }
   }

   public static double[] slowVel(double[] vel, double k) {
      return new double[]{vel[0] * k, vel[1], vel[2] * k};
   }

   public static double yawTo(double[] from, double[] to) {
      double[] d = sub(to, from);
      return Math.toDegrees(Math.atan2(-d[0], d[2]));
   }

   public static double rendReach(double age) {
      return 2.0D * age;
   }

   public static boolean rendRunning(double age) {
      return rendReach(age) <= 60.1D;
   }

   public static int rendLife() {
      return (int)Math.ceil(30.0D) + 50;
   }

   public static boolean rendHit(double[] at, double[] dir, double[] side, int age, double[] feet) {
      double reach = rendReach((double)age);
      if (reach > 60.1D) {
         return false;
      } else {
         double prev = Math.max(0.0D, reach - 2.0D);
         double[] rel = sub(feet, at);
         double along = dot(rel, dir);
         double lat = Math.abs(dot(rel, side));
         return along >= prev - 0.6D && along <= reach + 0.6D && lat <= 1.8D;
      }
   }

   public static double[] rendFrom(double[] feet, double[] dir) {
      return add(feet, mul(dir, 1.2D));
   }

   public static boolean howlReaches(double[] at, double[] feet) {
      return dist(feet, at) <= 10.0D;
   }

   public static double[] howlVel(double[] at, double[] feet) {
      double[] d = flat(sub(feet, at));
      return new double[]{d[0] * 1.5D, 0.75D, d[2] * 1.5D};
   }

   public static boolean thornsReach(double[] at, double[] feet) {
      return dist(feet, at) <= 8.0D;
   }

   public static double[] ambushSpot(double[] targetFeet, double targetYaw) {
      return add(targetFeet, mul(flat(fwd(targetYaw)), -1.6D));
   }

   public static boolean spearLands(double dist) {
      return dist <= 3.2D;
   }

   public static double veins(int beastTicks) {
      return Math.min(1.0D, 0.05D * (double)Math.max(0, beastTicks));
   }

   public static double veinsAfter(double at, int wokeTicks) {
      return Math.max(0.0D, at - 0.04D * (double)Math.max(0, wokeTicks));
   }

   public static double treeShrink(double sinceWoke) {
      return clamp01(sinceWoke / 24.0D);
   }

   public static double treeGrow(double sinceBorn, double shrink) {
      return sinceBorn * 1.6D * (1.0D - shrink);
   }

   public static String mmss(int ticks) {
      int s = Math.max(0, (int)Math.ceil((double)ticks / 20.0D));
      int r = s % 60;
      return s / 60 + ":" + (r < 10 ? "0" : "") + r;
   }

   public static double wakeHealth(double baseMax) {
      return baseMax * 0.3D;
   }
}
