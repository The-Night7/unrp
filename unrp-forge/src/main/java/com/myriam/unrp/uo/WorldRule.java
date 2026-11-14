package com.myriam.unrp.uo;



public final class WorldRule {
   public static final long MARGIN = 24000L;
   public static final int SAME = 0;
   public static final int MOVED = 1;
   public static final int ASKED = 2;
   public static final String REALMS = "unrp:";
   public static final String GONE = "unrp:old_world";

   private WorldRule() {
   }

   public static boolean old(String dim) {
      return dim == null || !dim.startsWith("unrp:");
   }

   public static String dimOf(String where) {
      if (where == null) {
         return "";
      } else {
         int bar = where.indexOf(124);
         return bar < 0 ? where : where.substring(0, bar);
      }
   }

   public static boolean moved(boolean known, long markSeed, long markClock, long seed, long now) {
      return known && markSeed != seed && ahead(markClock, now);
   }

   public static int changed(boolean asked, boolean auto, boolean moved) {
      return asked ? 2 : (auto && moved ? 1 : 0);
   }

   public static long seen(boolean known, long markClock, long evidence) {
      return known ? Math.max(markClock, evidence) : evidence;
   }

   public static boolean doubtful(boolean known, long markSeed, long markClock, long seed, long now, long evidence, long settled) {
      if (known && markSeed != seed) {
         return true;
      } else {
         long seen = seen(known, markClock, evidence);
         return ahead(seen, now) && seen > settled;
      }
   }

   static boolean ahead(long stamp, long now) {
      if (stamp <= now) {
         return false;
      } else {
         long by = stamp - now;
         return by > 24000L || by < 0L;
      }
   }

   public static long clock(long now, boolean known, long markClock, long evidence) {
      long best = Math.max(now, evidence);
      return known ? Math.max(best, markClock) : best;
   }

   public static String why(int why) {
      return why == 1 ? "the seed changed and the clock started over" : (why == 2 ? "an op asked" : "nothing changed");
   }
}
