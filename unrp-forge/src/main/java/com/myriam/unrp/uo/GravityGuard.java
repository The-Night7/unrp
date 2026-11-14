package com.myriam.unrp.uo;



import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public final class GravityGuard {
   private static final Logger LOG = LogUtils.getLogger();
   private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();
   private static final Set<Integer> STOOD_DOWN = ConcurrentHashMap.newKeySet();

   private GravityGuard() {
   }

   public static int direction(Entity entity) {
      if (STOOD_DOWN.contains(entity.getId())) {
         return 0;
      } else {
         try {
            return PlayerGravity.of(entity);
         } catch (Throwable var2) {
            fail(entity, "reading gravity", var2);
            return 0;
         }
      }
   }

   public static void fail(Entity entity, String where, Throwable t) {
      STOOD_DOWN.add(entity.getId());
      if (REPORTED.add(where)) {
         LOG.error("[unrp] directional gravity failed while {}; that player is back on ordinary gravity. Please send this stack trace.", where, t);
      }

   }

   public static void forget(int id) {
      STOOD_DOWN.remove(id);
   }

   public static boolean stoodDown(int id) {
      return STOOD_DOWN.contains(id);
   }

   public static void clear() {
      STOOD_DOWN.clear();
   }
}
