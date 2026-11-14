package com.myriam.unrp.uo;



public class CwHudState {
   private static volatile int charge;
   private static volatile int cap;
   private static volatile int momentum;
   private static volatile int stage;
   private static volatile int frame;
   private static volatile int links;
   private static volatile int flags;
   private static volatile long lastUpdate;
   private static volatile int lastCharge;
   private static volatile long flashUntil;

   public static void set(int c, int cp, int m, int st, int f, int l, int fl) {
      if (c > lastCharge) {
         flashUntil = System.currentTimeMillis() + 220L;
      }

      lastCharge = c;
      charge = c;
      cap = cp;
      momentum = m;
      stage = st;
      frame = f;
      links = l;
      flags = fl;
      lastUpdate = System.currentTimeMillis();
   }

   public static void clearAll() {
      charge = 0;
      cap = 0;
      momentum = 0;
      stage = 0;
      frame = 0;
      links = 0;
      flags = 0;
      lastCharge = 0;
      flashUntil = 0L;
      lastUpdate = 0L;
   }

   public static boolean fresh() {
      return System.currentTimeMillis() - lastUpdate < 3000L;
   }

   public static boolean flashing() {
      return System.currentTimeMillis() < flashUntil;
   }

   public static int charge() {
      return charge;
   }

   public static int cap() {
      return cap;
   }

   public static int momentum() {
      return momentum;
   }

   public static int stage() {
      return stage;
   }

   public static int frame() {
      return frame;
   }

   public static int links() {
      return links;
   }

   public static boolean held() {
      return (flags & 1) != 0;
   }

   public static boolean chained() {
      return (flags & 2) != 0;
   }
}
