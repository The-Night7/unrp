package com.myriam.unrp.uo;



public final class DaystarMath {
   public static final double ROOF = 0.6D;
   public static final double RAIN = 0.75D;
   public static final double THUNDER = 0.55D;
   public static final double NETHER = 0.5D;
   public static final double END = 0.1D;
   public static final double LOW = 0.55D;
   public static final double HEIGHT = 0.12D;
   public static final double NOON_HEIGHT = 0.45D;
   public static final double WIDTH = 0.08D;
   public static final double NOON_BULK = 1.45D;
   public static final double HEAT_FROM = 0.5D;
   public static final double HEAT_HOLD = 0.15D;
   public static final double NIGHT = 0.25D;
   public static final double SUN_MIN = 1.2D;
   public static final double SUN_MAX = 10.0D;
   public static final double EMBER = 0.7D;
   public static final double SUN_DMG_MIN = 6.0D;
   public static final double SUN_DMG_MAX = 24.0D;
   public static final double EMBER_DMG = 3.0D;
   public static final double CRATER_FROM = 6.0D;
   public static final double SUNDER_MIN = 14.0D;
   public static final double SUNDER_MAX = 32.0D;
   public static final int NOON_FROM = 5000;
   public static final int NOON_TO = 7000;
   public static final double FLIGHT_EMBER = 1.1D;
   public static final double FLIGHT_BASE = 0.95D;
   public static final double FLIGHT_SHRINK = 0.05D;
   public static final double FLIGHT_LEAST = 0.42D;
   public static final double GRAVITY_EMBER = 0.02D;
   public static final double GRAVITY_SUN = 0.008D;
   public static final double EMBER_BLAST = 1.0D;
   public static final double FALL_LEAST = 0.4D;
   public static final double FALL_SPAN = 0.6D;
   public static final double SUNDER_SPEED = 1.6D;
   public static final int SUNDER_BURN = 100;
   public static final int DIM_SKY = 0;
   public static final int DIM_NETHER = 1;
   public static final int DIM_END = 2;
   public static final double FINGER_UP = 2.04D;
   public static final double FINGER_SIDE = 0.41D;
   public static final double FINGER_BACK = 0.1D;
   public static final double FINGER_GAP = 0.1D;
   public static final double FLARE_UP = 0.95D;
   public static final double FLARE_REACH = 2.6D;
   public static final double FLARE_GROW = 0.45D;
   public static final double FLARE_DMG = 0.85D;
   public static final double FLARE_EMBER_DMG = 1.5D;
   public static final double FLARE_KNOCK = 1.4D;
   public static final double FLARE_LIFT = 1.2D;
   public static final int FLARE_WIND = 4;
   public static final double STAR_PITCH = 50.0D;
   public static final double STAR_SPEED = 1.6D;
   public static final double STAR_GRAVITY = 0.08D;
   public static final double STAR_BLAST = 1.35D;
   public static final double STAR_DMG = 1.25D;
   public static final double STAR_CRATER_FROM = 3.0D;
   public static final double STAR_CRATER = 0.6D;
   public static final double STAR_CRATER_LEAST = 2.0D;
   public static final double STAR_ROOM = 0.4D;
   public static final double STAR_SINK = 0.6D;
   public static final double STAR_HOP = 0.32D;
   public static final int STAR_WIND = 3;
   public static final int KIND_LINE = 0;
   public static final int KIND_ARC = 1;
   public static final int KIND_FALL = 2;
   public static final int KIND_SKY = 3;
   public static final double DIG_FROM = 0.5D;
   public static final double DIG_WIDE = 0.8D;
   public static final int ARC_RAYS = 5;
   public static final double ARC_SPREAD = 80.0D;
   public static final double ARC_LONG = 0.55D;
   public static final int FALL_RAYS = 8;
   public static final double FALL_LONG = 0.45D;
   public static final double FALL_START = 1.0D;
   public static final double FALL_PITCH = 50.0D;
   public static final double SKY_PITCH = -35.0D;
   public static final double PLUNGE_SPEED = 1.8D;
   public static final int PLUNGE_MOST = 240;
   public static final double FALL_REACH = 3.5D;
   public static final double FALL_REACH_GROW = 1.5D;
   public static final double FALL_HIT = 0.8D;
   public static final double FALL_BOOST = 0.6D;
   public static final double DROP_LEAST = 4.0D;
   public static final double DROP_FULL = 24.0D;
   public static final double CRESCENT_SPEED = 1.5D;
   public static final int CRESCENT_LIFE = 20;
   public static final double CRESCENT_SPAN = 1.8D;
   public static final double CRESCENT_GROW = 0.6D;
   public static final double CRESCENT_HIT = 0.9D;
   public static final double HURL_SPEED = 1.7D;
   public static final int HURL_OUT = 12;
   public static final double HURL_BACK = 0.9D;
   public static final double HURL_BACK_GAIN = 0.15D;
   public static final double HURL_BACK_MOST = 2.4D;
   public static final double HURL_HIT = 0.8D;
   public static final double HURL_CATCH = 1.4D;
   public static final int HURL_MOST = 200;
   public static final int HURL_COOLDOWN = 40;
   public static final int HURL_COST = 6;
   public static final int HURL_STRIKE = 4;
   public static final int HURL_LOST = 20;
   public static final double HURL_FROM_SIDE = -0.09D;
   public static final double HURL_FROM_UP = 1.83D;
   public static final double HURL_FROM_FWD = 1.0D;

   private DaystarMath() {
   }

   public static double clamp01(double x) {
      return x < 0.0D ? 0.0D : (x > 1.0D ? 1.0D : x);
   }

   public static double smooth01(double x) {
      double c = clamp01(x);
      return c * c * (3.0D - 2.0D * c);
   }

   public static long dayClock(long dayTime) {
      return (dayTime % 24000L + 24000L) % 24000L;
   }

   public static double sunHeight(long dayTime) {
      return Math.sin((Math.PI * 2D) * (double)dayClock(dayTime) / 24000.0D);
   }

   public static double clockGauge(long dayTime) {
      return smooth01((1.0D + sunHeight(dayTime)) / 2.0D);
   }

   public static double place(double clock, boolean sky, boolean rain, boolean thunder) {
      double g = clock;
      if (!sky) {
         g = clock * 0.6D;
      } else if (thunder) {
         g = clock * 0.55D;
      } else if (rain) {
         g = clock * 0.75D;
      }

      return clamp01(g);
   }

   public static double open(int dim, long dayTime, boolean sky, boolean wet, boolean thundering) {
      if (dim == 1) {
         return 0.5D;
      } else {
         return dim == 2 ? 0.1D : place(clockGauge(dayTime), sky, wet && !thundering, wet && thundering);
      }
   }

   public static double gauge(int dim, long dayTime, boolean sky, boolean wet, boolean thundering, boolean sneaking) {
      return held(open(dim, dayTime, sky, wet, thundering), sneaking);
   }

   public static double held(double g, boolean sneaking) {
      return sneaking ? Math.max(0.0D, g - 0.15D) : clamp01(g);
   }

   public static double powerFactor(double g) {
      return 0.55D + 0.44999999999999996D * clamp01(g);
   }

   public static double copyScale(double own, double other, double pf) {
      return !(own <= 0.0D) && !(own <= other) ? Math.max(other, own * clamp01(pf)) / own : 1.0D;
   }

   public static double heightFor(double g, boolean noon) {
      return noon ? 1.45D : 1.0D + 0.12D * clamp01(g);
   }

   public static double bulkFor(double g, boolean noon) {
      return noon ? 1.45D : smooth01(g);
   }

   public static double widthFor(double g, boolean noon) {
      return 1.0D + 0.08D * bulkFor(g, noon);
   }

   public static boolean heatOn(double g) {
      return g >= 0.5D;
   }

   public static boolean night(double g) {
      return g < 0.25D;
   }

   public static double sunMax(double g) {
      return night(g) ? 0.7D : 1.2D + 8.8D * clamp01(g);
   }

   public static double sunSize(double g, double charge) {
      return night(g) ? 0.7D : 1.2D + (sunMax(g) - 1.2D) * clamp01(charge);
   }

   public static double sunDamage(double g, double charge) {
      return night(g) ? 3.0D * powerFactor(g) : (6.0D + 18.0D * clamp01(charge)) * powerFactor(g);
   }

   public static double blast(double size, boolean ember) {
      return ember ? 1.0D : size / 2.0D + 1.5D;
   }

   public static double flightSpeed(double size, boolean ember) {
      return ember ? 1.1D : Math.max(0.42D, 0.95D - 0.05D * size);
   }

   public static double gravity(boolean ember) {
      return ember ? 0.02D : 0.008D;
   }

   public static double fall(double dist, double blast) {
      return clamp01(1.0D - dist / (blast + 1.0D)) * 0.6D + 0.4D;
   }

   public static double knock(double size, boolean ember) {
      return ember ? 0.2D : 0.4D + size * 0.05D;
   }

   public static double lift(double size, boolean ember) {
      return ember ? 0.2D : 0.35D + size * 0.025D;
   }

   public static double front(double age, double len) {
      return Math.min(len, age * 1.6D);
   }

   public static int runTicks(double len) {
      return (int)Math.ceil(len / 1.6D);
   }

   public static int fissureLife(double len) {
      return runTicks(len) + 100 + 20;
   }

   public static boolean craters(double size) {
      return size >= 6.0D;
   }

   public static double sunderLength(double g) {
      return 14.0D + 18.0D * clamp01(g);
   }

   public static double heldUp(double scale, double size) {
      return 2.04D * scale + 0.1D + size * 0.5D;
   }

   public static double flareUp(double scale) {
      return 0.95D * scale;
   }

   public static double flareReach(double size, boolean ember) {
      return ember ? 2.6D : 2.6D + size * 0.45D;
   }

   public static double flareDamage(double g, double charge, boolean ember) {
      return ember ? 3.0D * powerFactor(g) * 1.5D : sunDamage(g, charge) * 0.85D;
   }

   public static double flareKnock(double size, boolean ember) {
      return knock(size, ember) * 1.4D;
   }

   public static double flareLift(double size, boolean ember) {
      return lift(size, ember) * 1.2D;
   }

   public static boolean starDrop(boolean airborne, double pitch) {
      return airborne && pitch >= 50.0D;
   }

   public static double starBlast(double size, boolean ember) {
      return blast(size, ember) * 1.35D;
   }

   public static boolean starCraters(double size) {
      return size >= 3.0D;
   }

   public static double starCrater(double size) {
      return Math.max(2.0D, size * 0.6D);
   }

   public static boolean starRoom(double size, double groundBelow) {
      return groundBelow <= -(size + 0.4D + 1.0D);
   }

   public static double starLow(double size, double groundBelow) {
      double r = size * 0.5D;
      return starRoom(size, groundBelow) ? -(r + 0.4D) : Math.max(-(r + 0.4D), groundBelow + r * 0.6D);
   }

   public static double swoop(double age) {
      return smooth01(age / 3.0D);
   }

   public static int sunderKind(boolean airborne, double pitch, boolean sneaking) {
      if (airborne && pitch >= 50.0D) {
         return 2;
      } else if (sneaking) {
         return 1;
      } else {
         return pitch <= -35.0D ? 3 : 0;
      }
   }

   public static int rays(int kind) {
      return kind == 1 ? 5 : (kind == 2 ? 8 : 1);
   }

   public static double rayAngle(int kind, int i) {
      if (kind == 1) {
         return 80.0D * ((double)i / 4.0D - 0.5D);
      } else {
         return kind == 2 ? 360.0D * (double)i / 8.0D : 0.0D;
      }
   }

   public static double rayLength(int kind, double g) {
      double len = sunderLength(g);
      return kind == 1 ? len * 0.55D : (kind == 2 ? len * 0.45D : len);
   }

   public static double rayStart(int kind) {
      return kind == 2 ? 1.0D : 0.0D;
   }

   public static int digDepth(double g, int off) {
      if (!(g < 0.5D) && off >= -1 && off <= 1) {
         if (g < 0.8D) {
            return off == 0 ? 1 : 0;
         } else {
            return off == 0 ? 2 : 1;
         }
      } else {
         return 0;
      }
   }

   public static double dropK(double drop) {
      return clamp01((drop - 4.0D) / 20.0D);
   }

   public static double fallHit(double drop) {
      return 0.8D + 0.6D * dropK(drop);
   }

   public static double fallReach(double drop) {
      return 3.5D + 1.5D * dropK(drop);
   }

   public static double hurlBack(int ticksBack) {
      return Math.min(2.4D, 0.9D + 0.15D * (double)Math.max(0, ticksBack));
   }

   public static double hurlHit(double axe, double g) {
      return axe * powerFactor(g) * 0.8D;
   }

   public static double crescentSpan(double g) {
      return 1.8D + 0.6D * clamp01(g);
   }

   public static boolean noonOpen(long dayTime, boolean sky, boolean used) {
      long t = dayClock(dayTime);
      return sky && !used && t >= 5000L && t <= 7000L;
   }

   public static String clockText(long dayTime) {
      long mins = (dayClock(dayTime) * 60L / 1000L + 360L) % 1440L;
      long h = mins / 60L;
      long m = mins % 60L;
      return (h < 10L ? "0" : "") + h + ":" + (m < 10L ? "0" : "") + m;
   }

   public static String mmss(long ticks) {
      long s = Math.max(0L, (ticks + 19L) / 20L);
      long sec = s % 60L;
      return s / 60L + ":" + (sec < 10L ? "0" : "") + sec;
   }
}
