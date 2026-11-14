package com.myriam.unrp.deal;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public final class PlaneGuest {
   public static final String HOLDER = "unrp.has.singularity";
   public static final String TAG = "unrp.sg_plane_guest";
   public static final String INVITED = "unrp_plane_invited";
   public static final String PLOT = "unrp_sg_guest_plot";
   public static final String BACK = "unrp_sg_guest_back";
   public static final String TOLD = "unrp_sg_guest_told";
   public static final int TELL_EVERY = 20;
   public static final String HANDS = "§8You're a guest here. §7Hands off until you cross out.";
   public static final String STILL = "§8Your power rests while you're a guest. §7Cross out to use it.";
   public static final String EMPTY = "§8Nobody's plane is open to visit yet.";
   public static final String SENT = "§5[Singularity] §7You can't cross out on your own anymore, so the plane sends you back.";
   public static final String UNTOUCHED = "Guests can't be touched here.";

   private PlaneGuest() {
   }

   public static boolean is(Level level) {
      return level != null && level.dimension() == SoulPlaneEvents.PLANE;
   }

   public static boolean guest(Entity e) {
      if (e instanceof ServerPlayer p) {
         if (p.getTags().contains("unrp.sg_plane_guest") && is(p.level())) {
            return true;
         }
      }

      return false;
   }

   public static boolean casts(int action) {
      return action >= 10 && action <= 12 || action >= 22 && action <= 27 || action == 29;
   }

   public static boolean canLeave(ServerPlayer player) {
      return player.getTags().contains("unrp.has.singularity") && DealBridge.sgStageOf(player) >= 2 && DealBridge.sgForgeHas(player, 28);
   }

   public static void invite(ServerPlayer player, boolean door) {
      if (door) {
         player.getPersistentData().putBoolean("unrp_plane_invited", true);
      } else {
         player.getPersistentData().remove("unrp_plane_invited");
      }

   }

   public static Vec3 landing(ServerPlayer player, ServerLevel plane, double fromX) {
      PactLedger ledger = PactLedger.get(plane.getServer());
      int want = SoulPlaneEvents.plotIndexAt(fromX);
      int best = -1;

      UUID owner;
      for(int i = 1; (owner = ledger.ownerOfPlot(i)) != null; ++i) {
         if (!owner.equals(player.getUUID()) && (best < 0 || Math.abs(i - want) < Math.abs(best - want))) {
            best = i;
         }
      }

      if (best < 0) {
         return null;
      } else {
         BlockPos center = SoulPlaneEvents.plotCenter(best);
         SoulPlaneEvents.ensurePlatform(plane, center);
         return new Vec3((double)center.getX() + 0.5D, (double)center.getY() + 0.1D, (double)center.getZ() + 0.5D);
      }
   }

   static void moved(ServerPlayer player, ServerLevel to, Vec3 land) {
      if (guest(player) && is(to)) {
         player.getPersistentData().putInt("unrp_sg_guest_plot", SoulPlaneEvents.plotIndexAt(land.x));
      }

   }

   static void become(ServerPlayer player, ServerLevel plane) {
      int plot = SoulPlaneEvents.plotIndexAt(player.getX());
      player.getPersistentData().putInt("unrp_sg_guest_plot", plot);
      player.addTag("unrp.sg_plane_guest");
      if (DealBridge.puppetPulling(player)) {
         DealBridge.puppetLetGo(player, "§7The strings don't follow you in.");
      }

      UUID owner = PactLedger.get(plane.getServer()).ownerOfPlot(plot);
      ServerPlayer host = owner != null && !owner.equals(player.getUUID()) ? plane.getServer().getPlayerList().getPlayer(owner) : null;
      player.sendSystemMessage(Component.literal("§5[Singularity] §7You're a guest " + (host == null ? "in the Soul Planes" : "on " + host.getName().getString() + "'s plane") + ". Look all you like. Nothing here can touch you and you can't touch it, and your power rests until you cross out."));
   }

   static void leave(ServerPlayer player) {
      player.removeTag("unrp.sg_plane_guest");
      player.getPersistentData().remove("unrp_sg_guest_plot");
      player.getPersistentData().remove("unrp_sg_guest_back");
      player.getPersistentData().remove("unrp_plane_invited");
      player.getPersistentData().remove("unrp_sg_guest_told");
   }

   static double[] backFor(ServerPlayer player, ServerLevel to) {
      CompoundTag back = player.getPersistentData().getCompound("unrp_sg_guest_back");
      if (!back.contains("d")) {
         return null;
      } else {
         ServerLevel was = null;

         for(ServerLevel l : to.getServer().getAllLevels()) {
            if (l.dimension().location().toString().equals(back.getString("d"))) {
               was = l;
            }
         }

         if (was == null) {
            return null;
         } else {
            double scale = (was.dimension() == Level.NETHER ? 8.0D : 1.0D) / (to.dimension() == Level.NETHER ? 8.0D : 1.0D);
            return new double[]{back.getDouble("x") * scale, back.getDouble("y"), back.getDouble("z") * scale, was == to ? 1.0D : 0.0D};
         }
      }
   }

   static void keep(ServerPlayer player, ServerLevel plane) {
      if (!canLeave(player)) {
         sendBack(player, plane);
      } else if (!(player.getY() >= (double)(plane.getMinBuildHeight() - 8))) {
         BlockPos center = SoulPlaneEvents.plotCenter(player.getPersistentData().getInt("unrp_sg_guest_plot"));
         SoulPlaneEvents.ensurePlatform(plane, center);
         player.teleportTo((double)center.getX() + 0.5D, (double)center.getY() + 0.1D, (double)center.getZ() + 0.5D);
         player.setDeltaMovement(Vec3.ZERO);
         player.fallDistance = 0.0F;
      }
   }

   static void sendBack(ServerPlayer player, ServerLevel plane) {
      CompoundTag back = player.getPersistentData().getCompound("unrp_sg_guest_back");
      ServerLevel to = null;

      for(ServerLevel l : plane.getServer().getAllLevels()) {
         if (!DealBridge.sgSealed(l) && l.dimension().location().toString().equals(back.getString("d"))) {
            to = l;
         }
      }

      double x = back.getDouble("x");
      double y = back.getDouble("y");
      double z = back.getDouble("z");
      if (to == null) {
         to = plane.getServer().overworld();
         BlockPos spawn = to.getSharedSpawnPos();
         to.getBlockState(spawn);
         x = (double)spawn.getX() + 0.5D;
         y = (double)to.getHeight(Types.MOTION_BLOCKING, spawn.getX(), spawn.getZ());
         z = (double)spawn.getZ() + 0.5D;
      }

      player.teleportTo(to, x, y, z, player.getYRot(), player.getXRot());
      if (!is(player.level())) {
         player.setDeltaMovement(Vec3.ZERO);
         player.fallDistance = 0.0F;
         player.sendSystemMessage(Component.literal("§5[Singularity] §7You can't cross out on your own anymore, so the plane sends you back."));
      }

   }

   static void tell(ServerPlayer player, String what) {
      long now = player.serverLevel().getGameTime();
      CompoundTag pd = player.getPersistentData();
      long last = pd.getLong("unrp_sg_guest_told");
      if (!pd.contains("unrp_sg_guest_told") || now < last || now - last >= 20L) {
         pd.putLong("unrp_sg_guest_told", now);
         player.displayClientMessage(Component.literal(what), true);
      }
   }

   static void departing(ServerPlayer player, ServerLevel to) {
      if (is(to) && !is(player.level()) && player.getTags().contains("unrp.has.singularity") && !player.getPersistentData().getBoolean("unrp_plane_invited")) {
         CompoundTag back = new CompoundTag();
         back.putString("d", player.level().dimension().location().toString());
         back.putDouble("x", player.getX());
         back.putDouble("y", player.getY());
         back.putDouble("z", player.getZ());
         player.getPersistentData().put("unrp_sg_guest_back", back);
      }
   }

   private static void settle(Entity e) {
      if (e instanceof ServerPlayer player) {
         if (player.getTags().contains("unrp.sg_plane_guest") && !is(player.level())) {
            leave(player);
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onTravel(EntityTravelToDimensionEvent event) {
      Entity to_0 = event.getEntity();
      if (to_0 instanceof ServerPlayer player) {
         if (event.getDimension() == SoulPlaneEvents.PLANE) {
            ServerLevel to = player.getServer() == null ? null : player.getServer().getLevel(SoulPlaneEvents.PLANE);
            if (to != null) {
               departing(player, to);
            }
         }
      }

   }

   @SubscribeEvent
   public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (event.getFrom() == SoulPlaneEvents.PLANE) {
            leave(player);
         }

         if (event.getTo() == SoulPlaneEvents.PLANE && is(player.level())) {
            if (player.getTags().contains("unrp.has.singularity") && !player.getPersistentData().getBoolean("unrp_plane_invited")) {
               become(player, player.serverLevel());
            } else {
               leave(player);
            }

         }
      }
   }

   @SubscribeEvent
   public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
      settle(event.getEntity());
   }

   @SubscribeEvent
   public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
      settle(event.getEntity());
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onBreak(BlockEvent.BreakEvent event) {
      if (guest(event.getPlayer())) {
         event.setCanceled(true);
         tell((ServerPlayer)event.getPlayer(), "§8You're a guest here. §7Hands off until you cross out.");
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onPlace(BlockEvent.EntityPlaceEvent event) {
      if (guest(event.getEntity())) {
         event.setCanceled(true);
         tell((ServerPlayer)event.getEntity(), "§8You're a guest here. §7Hands off until you cross out.");
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onTrample(BlockEvent.FarmlandTrampleEvent event) {
      if (guest(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
      if (guest(event.getEntity())) {
         event.setCanceled(true);
         tell((ServerPlayer)event.getEntity(), "§8You're a guest here. §7Hands off until you cross out.");
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onHitBlock(PlayerInteractEvent.LeftClickBlock event) {
      if (guest(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
      if (guest(event.getEntity())) {
         event.setCanceled(true);
         tell((ServerPlayer)event.getEntity(), "§8You're a guest here. §7Hands off until you cross out.");
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onUseEntity(PlayerInteractEvent.EntityInteract event) {
      if (guest(event.getEntity())) {
         event.setCanceled(true);
         tell((ServerPlayer)event.getEntity(), "§8You're a guest here. §7Hands off until you cross out.");
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onUseEntityAt(PlayerInteractEvent.EntityInteractSpecific event) {
      if (guest(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onSwing(AttackEntityEvent event) {
      if (guest(event.getEntity())) {
         event.setCanceled(true);
         tell((ServerPlayer)event.getEntity(), "§8You're a guest here. §7Hands off until you cross out.");
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onPickup(EntityItemPickupEvent event) {
      if (guest(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onToss(ItemTossEvent event) {
      if (guest(event.getPlayer()) && Tossed.byHand(event.getEntity())) {
         ItemStack back = event.getEntity().getItem();
         event.getPlayer().getInventory().add(back);
         if (back.isEmpty()) {
            event.setCanceled(true);
            tell((ServerPlayer)event.getPlayer(), "§8You're a guest here. §7Hands off until you cross out.");
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onAttack(LivingAttackEvent event) {
      if (guest(event.getEntity()) && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         event.setCanceled(true);
      } else {
         if (guest(event.getSource().getEntity())) {
            event.setCanceled(true);
         }

      }
   }
}
