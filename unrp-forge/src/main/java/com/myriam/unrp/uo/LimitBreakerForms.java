package com.myriam.unrp.uo;



public final class LimitBreakerForms {
   public static final int NONE = 0;
   public static final int COLOSSUS = 1;
   public static final int SURGE = 2;
   public static final int FALSE_DAWN = 3;
   public static final int RADIANCE = 4;
   public static final int RADIANCE_II = 5;
   public static final int RADIANCE_III = 6;
   public static final int GILDED_COLOSSUS = 7;
   public static final int PRIMAL = 8;
   public static final int ZENITH = 9;
   public static final int AZURE = 10;
   public static final int AZURE_EVOLUTION = 11;
   public static final int INSTINCT = 12;
   public static final int INSTINCT_MASTERED = 13;
   public static final int FIFTH_DAWN = 14;
   public static final int COUNT = 15;
   public static final String[] NAME = new String[]{"", "Colossus", "Surge", "False Dawn", "Radiance", "Radiance II", "Radiance III", "Gilded Colossus", "Primal", "Zenith", "Azure", "Azure Evolution", "Instinct", "Instinct Mastered", "The Fifth Dawn"};
   public static final String[] ROSE_NAME = new String[]{"", "Colossus", "Surge", "False Dawn", "Radiance", "Radiance II", "Radiance III", "Gilded Colossus", "Primal", "Zenith", "Rose", "Rose Evolution", "Instinct", "Instinct Mastered", "The Fifth Dawn"};
   public static final int[] ERA = new int[]{0, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4};
   public static final int[] RGB = new int[]{16777215, 10119740, 16730684, 16771488, 16765519, 16768358, 16771202, 16761134, 12595264, 16735878, 5873919, 2381544, 13228266, 15923199, 16773108};
   public static final int[] ROSE_RGB = new int[]{16777215, 10119740, 16730684, 16771488, 16765519, 16768358, 16771202, 16761134, 12595264, 16735878, 16727710, 13115002, 13228266, 15923199, 16773108};
   public static final double[] POWER = new double[]{1.0D, 1.05D, 1.15D, 1.3D, 1.5D, 1.75D, 2.0D, 2.1D, 2.35D, 2.7D, 3.0D, 3.4D, 3.9D, 4.6D, 6.0D};
   public static final double[] UPKEEP_MULT = new double[]{0.0D, 1.0D, 2.5D, 1.4D, 1.0D, 1.4D, 1.9D, 2.2D, 1.0D, 1.3D, 1.6D, 2.0D, 1.0D, 1.5D, 2.2D};
   public static final float[] SCALE = new float[]{1.0F, 3.2F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 3.8F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F};
   public static final int[] LEVEL_REQ = new int[]{0, 4, 4, 30, 10, 70, 110, 150, 25, 190, 220, 250, 45, 300, 350};
   public static final int[] HAIR = new int[]{0, 0, 0, 2, 3, 4, 5, 0, 2, 0, 3, 4, 0, 0, 6};

   private LimitBreakerForms() {
   }

   public static boolean valid(int form) {
      return form >= 1 && form < 15;
   }

   public static int era(int form) {
      return valid(form) ? ERA[form] : 0;
   }

   public static int firstOfEra(int era) {
      for(int f = 1; f < 15; ++f) {
         if (ERA[f] == era) {
            return f;
         }
      }

      return 0;
   }

   public static double powerOverEra(int form) {
      return !valid(form) ? 1.0D : POWER[form] / POWER[firstOfEra(ERA[form])];
   }

   public static boolean roseSwapped(int form, boolean rose) {
      return rose && (form == 10 || form == 11);
   }

   public static String displayName(int form, boolean rose) {
      if (!valid(form)) {
         return "";
      } else {
         return rose ? ROSE_NAME[form] : NAME[form];
      }
   }

   public static int color(int form, boolean rose) {
      if (!valid(form)) {
         return 16777215;
      } else {
         return rose ? ROSE_RGB[form] : RGB[form];
      }
   }

   public static boolean apeForm(int form) {
      return form == 1 || form == 7;
   }

   public static int next(int form, boolean[] unlockedByForm) {
      for(int f = Math.max(1, form + 1); f < 15; ++f) {
         if (unlockedByForm[f]) {
            return f;
         }
      }

      return 0;
   }
}
