package com.myriam.unrp.uo;



public class SandHudState {
   private static volatile int reserve;
   private static volatile int cap;
   private static volatile int lifetime;
   private static volatile int tails;
   private static volatile boolean severed;
   private static volatile long lastUpdate;

   public static void set(int r, int c, int l, int t, boolean sev) {
      reserve = r;
      cap = c;
      lifetime = l;
      tails = t;
      severed = sev;
      lastUpdate = System.currentTimeMillis();
   }

   public static boolean fresh() {
      return System.currentTimeMillis() - lastUpdate < 3000L;
   }

   public static int reserve() {
      return reserve;
   }

   public static int cap() {
      return cap;
   }

   public static int lifetime() {
      return lifetime;
   }

   public static int tails() {
      return tails;
   }

   public static boolean severed() {
      return severed;
   }
}
