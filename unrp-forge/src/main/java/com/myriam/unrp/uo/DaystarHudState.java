package com.myriam.unrp.uo;



import net.minecraft.client.Minecraft;

public final class DaystarHudState {
   private static volatile int noonLeft;
   private static volatile int burnLeft;
   private static volatile boolean used;
   private static volatile long at;
   private static volatile long lastUpdate;

   private DaystarHudState() {
   }

   public static void set(int noon, int burn, boolean usedToday) {
      Minecraft mc = Minecraft.getInstance();
      noonLeft = noon;
      burnLeft = burn;
      used = usedToday;
      at = mc.level == null ? 0L : mc.level.getGameTime();
      lastUpdate = System.currentTimeMillis();
   }

   public static boolean fresh() {
      return System.currentTimeMillis() - lastUpdate < 3000L;
   }

   public static int noonLeft(long now) {
      return (int)Math.max(0L, (long)noonLeft - Math.max(0L, now - at));
   }

   public static int burnLeft(long now) {
      return (int)Math.max(0L, (long)burnLeft - Math.max(0L, now - at));
   }

   public static boolean used() {
      return used;
   }
}
