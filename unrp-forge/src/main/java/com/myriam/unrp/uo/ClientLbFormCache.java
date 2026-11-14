package com.myriam.unrp.uo;



import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.Util;

public final class ClientLbFormCache {
   private static final Map<UUID, Integer> FORMS = new ConcurrentHashMap();
   private static final Map<UUID, Long> LIT = new ConcurrentHashMap();
   private static final Map<UUID, long[]> BECOMING = new ConcurrentHashMap();
   public static final int FLARE_TICKS = 16;
   public static final float HAIR_AT = 0.1F;
   public static final float FUR_AT = 0.25F;
   public static final float APE_AT = 0.35F;
   public static final float TINT_AT = 0.45F;
   public static final float LINGER = 1.0F;
   private static int preview;

   private ClientLbFormCache() {
   }

   public static void beginPreview() {
      ++preview;
   }

   public static void endPreview() {
      preview = Math.max(0, preview - 1);
   }

   public static boolean previewing() {
      return preview > 0;
   }

   public static int stateKey(int entityId) {
      return preview > 0 ? entityId ^ 1073741824 : entityId;
   }

   public static void put(UUID id, int form) {
      Integer old = (Integer)FORMS.get(id);
      int was = old == null ? 0 : old & 63;
      if (form <= 0) {
         FORMS.remove(id);
      } else {
         FORMS.put(id, form);
      }

      int now = form <= 0 ? 0 : form & 63;
      if (preview == 0 && now > 0 && now != was) {
         LIT.put(id, Util.getMillis());
      }

      long[] b = (long[])BECOMING.get(id);
      if (b != null && preview == 0 && (b[0] & 63L) == (long)now) {
         BECOMING.remove(id);
      }

   }

   public static void becoming(UUID id, int form, int ticks) {
      if (form > 0 && ticks > 0) {
         BECOMING.put(id, new long[]{(long)form, Util.getMillis(), (long)ticks});
      } else {
         BECOMING.remove(id);
      }
   }

   public static float progress(UUID id) {
      long[] b = (long[])BECOMING.get(id);
      if (b != null && preview <= 0) {
         float u = (float)(Util.getMillis() - b[1]) / (50.0F * (float)b[2]);
         if (!(u < 0.0F) && !(u >= 2.0F)) {
            return Math.min(1.0F, u);
         } else {
            BECOMING.remove(id);
            return -1.0F;
         }
      } else {
         return -1.0F;
      }
   }

   public static int target(UUID id) {
      long[] b = (long[])BECOMING.get(id);
      return b != null && preview <= 0 ? (int)(b[0] & 63L) : 0;
   }

   public static boolean targetRose(UUID id) {
      long[] b = (long[])BECOMING.get(id);
      return b != null && (b[0] & 64L) != 0L;
   }

   public static float flare(UUID id) {
      Long lit = (Long)LIT.get(id);
      if (lit == null) {
         return 0.0F;
      } else {
         float age = (float)(Util.getMillis() - lit) / 800.0F;
         if (!(age >= 1.0F) && !(age < 0.0F)) {
            return (1.0F - age) * (1.0F - age);
         } else {
            LIT.remove(id);
            return 0.0F;
         }
      }
   }

   public static long litAt(UUID id) {
      Long lit = (Long)LIT.get(id);
      return lit == null ? 0L : lit;
   }

   public static int form(UUID id) {
      Integer f = (Integer)FORMS.get(id);
      return f == null ? 0 : f & 63;
   }

   public static boolean auraOff(UUID id) {
      Integer f = (Integer)FORMS.get(id);
      return f != null && (f & 128) != 0;
   }

   public static boolean rose(UUID id) {
      Integer f = (Integer)FORMS.get(id);
      return f != null && (f & 64) != 0;
   }

   public static Integer raw(UUID id) {
      return (Integer)FORMS.get(id);
   }

   public static void restore(UUID id, Integer raw) {
      if (raw == null) {
         FORMS.remove(id);
      } else {
         FORMS.put(id, raw);
      }

   }

   public static void clearAll() {
      FORMS.clear();
      LIT.clear();
      BECOMING.clear();
   }
}
