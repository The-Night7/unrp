package com.myriam.unrp.uo;



public class GritHudState {
   private static volatile int grit;
   private static volatile int cap;
   private static volatile int level;
   private static volatile int xp;
   private static volatile int xpNext;
   private static volatile int edge;
   private static volatile int form;
   private static volatile int tier;
   private static volatile int ki;
   private static volatile int kiCap;
   private static volatile long lastUpdate;

   public static void set(int g, int c, int lv, int x, int xn, int e, int f, int t, int k, int kc) {
      grit = g;
      cap = c;
      level = lv;
      xp = x;
      xpNext = xn;
      edge = e;
      form = f;
      tier = t;
      ki = k;
      kiCap = kc;
      lastUpdate = System.currentTimeMillis();
   }

   public static boolean fresh() {
      return System.currentTimeMillis() - lastUpdate < 3000L;
   }

   public static int grit() {
      return grit;
   }

   public static int cap() {
      return cap;
   }

   public static int level() {
      return level;
   }

   public static int xp() {
      return xp;
   }

   public static int xpNext() {
      return xpNext;
   }

   public static int edge() {
      return edge;
   }

   public static int form() {
      return form;
   }

   public static int tier() {
      return tier;
   }

   public static int ki() {
      return ki;
   }

   public static int kiCap() {
      return kiCap;
   }
}
