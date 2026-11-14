package com.myriam.unrp.deal;

import java.util.ArrayList;
import java.util.List;

public class PactHudState {
   private static volatile String[] names = new String[]{"", "", ""};
   private static volatile int[] hp = new int[3];
   private static volatile int[] flags = new int[3];
   private static volatile List<String> info = new ArrayList();
   private static volatile long lastUpdate;

   public static void set(String[] n, int[] h, int[] f, List<String> lines) {
      names = n;
      hp = h;
      flags = f;
      info = lines;
      lastUpdate = System.currentTimeMillis();
   }

   public static boolean fresh() {
      return System.currentTimeMillis() - lastUpdate < 3000L;
   }

   public static void clearAll() {
      names = new String[]{"", "", ""};
      hp = new int[3];
      flags = new int[3];
      info = new ArrayList();
      lastUpdate = 0L;
   }

   public static String name(int i) {
      return names[i];
   }

   public static int hp(int i) {
      return hp[i];
   }

   public static boolean online(int i) {
      return (flags[i] & 1) != 0;
   }

   public static boolean gone(int i) {
      return (flags[i] & 2) != 0;
   }

   public static boolean warded(int i) {
      return (flags[i] & 4) != 0;
   }

   public static boolean spent(int i) {
      return (flags[i] & 8) != 0;
   }

   public static List<String> info() {
      return info;
   }
}
