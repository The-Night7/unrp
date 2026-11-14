package com.myriam.unrp.uo;



public class StruggleClientState {
   private static volatile boolean active;
   private static volatile int kind;
   private static volatile int progress;
   private static volatile long shownAt;

   public static void set(boolean a, int k, int p) {
      active = a;
      kind = k;
      progress = p;
      if (a) {
         shownAt = System.currentTimeMillis();
      }

   }

   public static boolean active() {
      return active;
   }

   public static int kind() {
      return kind;
   }

   public static int progress() {
      return progress;
   }

   public static long shownAt() {
      return shownAt;
   }

   public static void clear() {
      active = false;
      progress = 0;
   }
}
