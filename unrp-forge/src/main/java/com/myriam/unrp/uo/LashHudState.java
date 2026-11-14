package com.myriam.unrp.uo;



import net.minecraft.client.Minecraft;

public final class LashHudState {
   private static volatile int strain;
   private static volatile int frenzy;
   private static volatile int rest;
   private static volatile int flags;
   private static volatile int wind;
   private static volatile int windLeft;
   private static volatile int held;
   private static volatile int holdLeft;
   private static volatile int[] cds = new int[6];
   private static volatile long at;
   private static volatile long lastUpdate;

   private LashHudState() {
   }

   public static void set(int strainNow, int frenzyLeft, int restLeft, int[] cooldowns, int flagsNow, int windNow, int windLeftNow, int heldNow, int holdLeftNow) {
      Minecraft mc = Minecraft.getInstance();
      strain = strainNow;
      frenzy = frenzyLeft;
      rest = restLeft;
      int[] copy = new int[6];

      for(int i = 0; i < copy.length && cooldowns != null && i < cooldowns.length; ++i) {
         copy[i] = cooldowns[i];
      }

      cds = copy;
      flags = flagsNow;
      wind = windNow;
      windLeft = windLeftNow;
      held = heldNow;
      holdLeft = holdLeftNow;
      at = mc.level == null ? 0L : mc.level.getGameTime();
      lastUpdate = System.currentTimeMillis();
   }

   public static boolean fresh() {
      return System.currentTimeMillis() - lastUpdate < 3000L;
   }

   public static int strain() {
      return strain;
   }

   public static int frenzyLeft(long now) {
      return (int)Math.max(0L, (long)frenzy - Math.max(0L, now - at));
   }

   public static int restLeft(long now) {
      return (int)Math.max(0L, (long)rest - Math.max(0L, now - at));
   }

   public static double cooldown(int i) {
      int[] c = cds;
      return i >= 0 && i < c.length ? (double)c[i] / 255.0D : 0.0D;
   }

   public static boolean flag(int f) {
      return (flags & f) != 0;
   }

   public static int wind() {
      return wind;
   }

   public static int windLeft(long now) {
      return (int)Math.max(0L, (long)windLeft - Math.max(0L, now - at));
   }

   public static int held() {
      return held;
   }

   public static int holdLeft(long now) {
      return (int)Math.max(0L, (long)holdLeft - Math.max(0L, now - at));
   }
}
