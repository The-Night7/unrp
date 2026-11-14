package com.myriam.unrp.uo;



import net.minecraft.client.Minecraft;

public final class SaltHudState {
   private static volatile int[] left = new int[6];
   private static volatile int[] full = new int[6];
   private static volatile int wake;
   private static volatile int flags;
   private static volatile int[] judged = new int[0];
   private static volatile long at;
   private static volatile long lastUpdate;

   private SaltHudState() {
   }

   public static void set(int[] leftNow, int[] fullNow, int wakeNow, int flagsNow, int[] judgedNow) {
      Minecraft mc = Minecraft.getInstance();
      int[] l = new int[6];
      int[] f = new int[6];

      for(int i = 0; i < l.length; ++i) {
         l[i] = leftNow != null && i < leftNow.length ? leftNow[i] : 0;
         f[i] = fullNow != null && i < fullNow.length ? Math.max(1, fullNow[i]) : 1;
      }

      left = l;
      full = f;
      wake = wakeNow;
      flags = flagsNow;
      judged = judgedNow == null ? new int[0] : (int[])judgedNow.clone();
      at = mc.level == null ? 0L : mc.level.getGameTime();
      lastUpdate = System.currentTimeMillis();
   }

   public static boolean fresh() {
      return System.currentTimeMillis() - lastUpdate < 3000L;
   }

   public static double cooldown(int i, double now) {
      int[] l = left;
      int[] f = full;
      if (i >= 0 && i < l.length) {
         double rest = (double)l[i] - Math.max(0.0D, now - (double)at);
         return rest <= 0.0D ? 0.0D : Math.min(1.0D, rest / (double)f[i]);
      } else {
         return 0.0D;
      }
   }

   public static int wakeLeft(double now) {
      return (int)Math.max(0L, Math.round((double)wake - Math.max(0.0D, now - (double)at)));
   }

   public static boolean flag(int f) {
      return (flags & f) != 0;
   }

   public static boolean judged(int id) {
      if (!fresh()) {
         return false;
      } else {
         for(int j : judged) {
            if (j == id) {
               return true;
            }
         }

         return false;
      }
   }

   public static void clear() {
      left = new int[6];
      full = new int[6];
      wake = 0;
      flags = 0;
      judged = new int[0];
      lastUpdate = 0L;
   }
}
