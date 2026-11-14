package com.myriam.unrp.uo;



import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class PlayerGravity {
   public static final String KEY = "unrp_gravity";
   public static final String[] TAGS = new String[]{"unrp.gravity_down", "unrp.gravity_up", "unrp.gravity_north", "unrp.gravity_south", "unrp.gravity_west", "unrp.gravity_east"};
   private static PlayerGravity.Lookup clientLookup = (id) -> 0;
   private static PlayerGravity.JumpProbe jumpProbe = (entity) -> false;

   private PlayerGravity() {
   }

   public static void bindClient(PlayerGravity.Lookup lookup) {
      clientLookup = lookup;
   }

   public static void bindClientJump(PlayerGravity.JumpProbe probe) {
      jumpProbe = probe;
   }

   public static boolean jumping(Entity entity) {
      return entity.level() != null && entity.level().isClientSide ? jumpProbe.jumping(entity) : false;
   }

   public static int of(Entity entity) {
      if (!(entity instanceof Player)) {
         return 0;
      } else if (entity.level() == null) {
         return 0;
      } else {
         return entity.level().isClientSide ? clamp(clientLookup.get(entity.getUUID())) : clamp(entity.getPersistentData().getInt("unrp_gravity"));
      }
   }

   public static boolean rotated(Entity entity) {
      return !GravityFrame.plain(of(entity));
   }

   public static int fromTags(Player player) {
      for(int i = 1; i < TAGS.length; ++i) {
         if (player.getTags().contains(TAGS[i])) {
            return i;
         }
      }

      return 0;
   }

   public static boolean set(ServerPlayer player, int dir) {
      int want = clamp(dir);
      if (player.getPersistentData().getInt("unrp_gravity") == want) {
         return false;
      } else {
         player.getPersistentData().putInt("unrp_gravity", want);
         return true;
      }
   }

   public static int ofDirection(Direction direction) {
      return direction == null ? 0 : clamp(direction.ordinal());
   }

   public static Direction direction(int dir) {
      return Direction.values()[clamp(dir)];
   }

   private static int clamp(int dir) {
      return dir >= 0 && dir < 6 ? dir : 0;
   }

   public interface JumpProbe {
      boolean jumping(Entity var1);
   }

   public interface Lookup {
      int get(UUID var1);
   }
}
