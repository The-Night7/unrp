package com.myriam.unrp.deal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "unrp"
)
public class SoulPlaneEvents {
   public static final ResourceKey<Level> PLANE = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("unrp", "soul_planes"));
   private static final String FLY_TAG = "unrp.dm_planefly";
   private static final int SOULC = 5882600;
   private static final UUID NO_PLOT = new UUID(0L, 0L);
   private static final Map<UUID, UUID> skySent = new HashMap();

   private SoulPlaneEvents() {
   }

   public static int plotIndexAt(double x) {
      return (int)Math.round((x - 8.0D) / 4096.0D);
   }

   private static String powerOf(ServerPlayer player) {
      return DealBridge.powerOf(player);
   }

   private static void syncSky(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      UUID owner = ledger.ownerOfPlot(plotIndexAt(player.getX()));
      UUID mark = owner == null ? NO_PLOT : owner;
      if (!mark.equals(skySent.get(player.getUUID()))) {
         skySent.put(player.getUUID(), mark);
         String power = "";
         if (owner != null) {
            ServerPlayer holder = level.getServer().getPlayerList().getPlayer(owner);
            if (holder != null) {
               power = powerOf(holder);
            }
         }

         com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PlaneSkyPacket(owner != null, owner, power));
      }
   }

   private static void clearSky(ServerPlayer player) {
      if (skySent.remove(player.getUUID()) != null) {
         com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PlaneSkyPacket(false, (UUID)null, ""));
      }

   }

   public static ServerLevel plane(MinecraftServer server) {
      return server.getLevel(PLANE);
   }

   public static BlockPos plotCenter(int index) {
      return new BlockPos(index * 4096 + 8, 96, 8);
   }

   public static void ensurePlatform(ServerLevel plane, BlockPos center) {
      BlockPos floor = center.below();
      if (plane.getBlockState(floor).isAir()) {
         for(int dx = -4; dx <= 4; ++dx) {
            for(int dz = -4; dz <= 4; ++dz) {
               plane.setBlock(floor.offset(dx, 0, dz), Blocks.SMOOTH_BASALT.defaultBlockState(), 3);
            }
         }

         plane.setBlock(floor, Blocks.GLOWSTONE.defaultBlockState(), 3);
         plane.setBlock(floor.offset(4, 1, 4), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
         plane.setBlock(floor.offset(-4, 1, 4), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
         plane.setBlock(floor.offset(4, 1, -4), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
         plane.setBlock(floor.offset(-4, 1, -4), Blocks.SOUL_LANTERN.defaultBlockState(), 3);
      }
   }

   public static void enter(ServerPlayer player, UUID plotOwner) {
      MinecraftServer server = player.getServer();
      if (server != null) {
         ServerLevel plane = plane(server);
         if (plane == null) {
            player.displayClientMessage(Component.literal("§7The Soul Planes aren't open in this world."), true);
         } else {
            PactLedger ledger = PactLedger.get(server);
            if (player.level().dimension() != PLANE) {
               ledger.setReturnPos(player.getUUID(), String.valueOf(player.level().dimension().location()) + "|" + player.getX() + "|" + player.getY() + "|" + player.getZ());
            }

            BlockPos center = plotCenter(ledger.plotOf(plotOwner));
            ensurePlatform(plane, center);
            Fx.burst((ServerLevel)player.level(), Fx.ember(5882600), player.position().add(0.0D, 1.0D, 0.0D), 24, 0.5D, 0.05D);
            PlaneGuest.invite(player, true);
            player.teleportTo(plane, (double)center.getX() + 0.5D, (double)center.getY() + 0.1D, (double)center.getZ() + 0.5D, player.getYRot(), player.getXRot());
            PlaneGuest.invite(player, false);
            plane.playSound((Player)null, center, DealBridge.sound("minecraft:block.portal.travel"), SoundSource.PLAYERS, 0.7F, 1.4F);
         }
      }
   }

   public static void sanctuary(ServerPlayer player, UUID master) {
      MinecraftServer server = player.getServer();
      if (server != null) {
         PactLedger ledger = PactLedger.get(server);
         if (player.level().dimension() != PLANE) {
            enter(player, master);
            player.displayClientMessage(Component.literal("§3Sanctuary. The Dealmaker's plane holds you."), true);
         } else {
            String back = ledger.takeReturnPos(player.getUUID());
            if (back == null) {
               player.displayClientMessage(Component.literal("§7No door back. Step off the edge of the world, or stay."), true);
            } else {
               String[] parts = back.split("\\|");
               ServerLevel dest = null;

               for(ServerLevel candidate : server.getAllLevels()) {
                  if (candidate.dimension().location().toString().equals(parts[0])) {
                     dest = candidate;
                     break;
                  }
               }

               if (dest != null) {
                  player.teleportTo(dest, Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), player.getYRot(), player.getXRot());
                  player.displayClientMessage(Component.literal("§3Back through the veil."), true);
               }
            }
         }
      }
   }

   public static void stashInPlane(MinecraftServer server, ItemEntity entity, UUID owner) {
      ServerLevel plane = plane(server);
      if (plane == null) {
         entity.setPos(entity.getX(), (double)(entity.level().getMinBuildHeight() + 4), entity.getZ());
         entity.setDeltaMovement(Vec3.ZERO);
      } else {
         PactLedger ledger = PactLedger.get(server);
         BlockPos center = plotCenter(ledger.plotOf(owner));
         ensurePlatform(plane, center);
         ItemStack stack = entity.getItem().copy();
         entity.discard();
         ItemEntity waiting = new ItemEntity(plane, (double)center.getX() + 0.5D, (double)center.getY() + 1.0D, (double)center.getZ() + 0.5D, stack);
         waiting.setDeltaMovement(Vec3.ZERO);
         plane.addFreshEntity(waiting);
      }
   }

   private static boolean inOwnPlot(ServerPlayer player, PactLedger ledger) {
      BlockPos center = plotCenter(ledger.plotOf(player.getUUID()));
      double dx = player.getX() - (double)center.getX();
      double dz = player.getZ() - (double)center.getZ();
      return dx * dx + dz * dz < 16384.0D;
   }

   @SubscribeEvent
   public static void onTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player ledger_0 = event.player;
         if (ledger_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)ledger_0;
            Level var6 = player.level();
            if (var6 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var6;
               if (level.dimension() != PLANE) {
                  if (player.getTags().contains("unrp.dm_planefly")) {
                     player.removeTag("unrp.dm_planefly");
                     if (!player.isCreative() && !player.isSpectator() && !player.getTags().contains("unrp.dm_wings")) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                     }
                  }

                  clearSky(player);
                  return;
               }

               PactLedger ledger = PactLedger.get(level.getServer());
               if (player.tickCount % 20 == 0) {
                  syncSky(player, level, ledger);
               }

               if (PlaneGuest.guest(player)) {
                  PlaneGuest.keep(player, level);
                  return;
               }

               boolean home = inOwnPlot(player, ledger);
               if (home && !player.getAbilities().mayfly) {
                  player.getAbilities().mayfly = true;
                  player.onUpdateAbilities();
                  player.addTag("unrp.dm_planefly");
               } else if (!home && player.getTags().contains("unrp.dm_planefly")) {
                  player.removeTag("unrp.dm_planefly");
                  if (!player.isCreative() && !player.isSpectator() && !player.getTags().contains("unrp.dm_wings")) {
                     player.getAbilities().mayfly = false;
                     player.getAbilities().flying = false;
                     player.onUpdateAbilities();
                  }
               }

               if (player.getY() < (double)(level.getMinBuildHeight() - 8)) {
                  BlockPos center = plotCenter(ledger.plotOf(player.getUUID()));
                  ensurePlatform(level, center);
                  player.teleportTo((double)center.getX() + 0.5D, (double)center.getY() + 0.1D, (double)center.getZ() + 0.5D);
                  player.setDeltaMovement(Vec3.ZERO);
                  player.fallDistance = 0.0F;
               }

               if (player.tickCount % 30 == 0 && home) {
                  Fx.circle(level, Fx.glint(5882600), player.position().add(0.0D, 0.2D, 0.0D), 1.6D, 6, 0.02D, 0.0D);
               }

               return;
            }
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onHurt(LivingHurtEvent event) {
      LivingEntity var3 = event.getEntity();
      if (var3 instanceof ServerPlayer player) {
         Level var4 = player.level();
         if (var4 instanceof ServerLevel level) {
            if (level.dimension() == PLANE) {
               if (event.getSource().is(TrueDamage.TYPE)) {
                  return;
               }

               if (inOwnPlot(player, PactLedger.get(level.getServer()))) {
                  event.setCanceled(true);
               }

               return;
            }
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onPortal(EntityTravelToDimensionEvent event) {
      Entity ledger_0 = event.getEntity();
      if (ledger_0 instanceof ServerPlayer player) {
         Level var11 = player.level();
         if (var11 instanceof ServerLevel level) {
            if (event.getDimension() != Level.NETHER && event.getDimension() != Level.OVERWORLD) {
               return;
            }

            PactLedger ledger = PactLedger.get(level.getServer());
            BlockPos base = player.blockPosition();

            for(int dx = -2; dx <= 2; ++dx) {
               for(int dy = -1; dy <= 3; ++dy) {
                  for(int dz = -2; dz <= 2; ++dz) {
                     BlockPos pos = base.offset(dx, dy, dz);
                     if (level.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
                        String key = String.valueOf(level.dimension().location()) + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
                        UUID target = ledger.portalTarget(key);
                        if (target != null) {
                           event.setCanceled(true);
                           enter(player, target);
                           player.displayClientMessage(Component.literal("§3The portal takes you into the Soul Planes."), true);
                           return;
                        }
                     }
                  }
               }
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      skySent.remove(event.getEntity().getUUID());
   }

   @SubscribeEvent
   public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
      Player ledger_0 = event.getEntity();
      if (ledger_0 instanceof ServerPlayer player) {
         if (player.getServer() != null) {
            PactLedger ledger = PactLedger.get(player.getServer());
            UUID master = ledger.dealmakerOf(player.getUUID());
            if (master != null && player.getRespawnPosition() == null) {
               enter(player, master);
               player.displayClientMessage(Component.literal("§3No bed claims you, so the pact does."), true);
               return;
            }

            return;
         }
      }

   }
}
