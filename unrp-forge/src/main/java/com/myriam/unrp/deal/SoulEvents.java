package com.myriam.unrp.deal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "unrp"
)
public class SoulEvents {
   private static final int SOULC = 5882600;
   private static final Map<UUID, long[]> eatenCopy = new HashMap();
   private static final Map<UUID, String> eatenPower = new HashMap();
   private static final Map<UUID, String> eatenAlias = new HashMap();
   private static final Map<UUID, Long> soulBurn = new HashMap();
   private static final Map<UUID, int[]> framedHold = new HashMap();
   private static final Map<UUID, Vec3> framedAnchor = new HashMap();
   private static final Map<Long, Object[]> furnaceBoost = new HashMap();

   private SoulEvents() {
   }

   private static ServerPlayer targetOf(ServerLevel level, ItemStack stack) {
      UUID id = SoulItem.targetOf(stack);
      if (id == null) {
         return null;
      } else {
         return SoulItem.serialOf(stack) != PactLedger.get(level.getServer()).serialOf(id) ? null : level.getServer().getPlayerList().getPlayer(id);
      }
   }

   public static boolean soulEntityTick(ServerLevel level, ItemEntity entity, ItemStack stack) {
      UUID id = SoulItem.targetOf(stack);
      if (id != null && SoulItem.serialOf(stack) == PactLedger.get(level.getServer()).serialOf(id)) {
         ServerPlayer target = level.getServer().getPlayerList().getPlayer(id);
         if (target == null) {
            return false;
         } else {
            if (entity.isUnderWater() && !target.hasEffect(MobEffects.WATER_BREATHING)) {
               int air = target.getAirSupply() - 5;
               if (air <= -40) {
                  target.setAirSupply(0);
                  TrueDamage.deal(level, (Entity)null, target, 2.0F);
               } else {
                  target.setAirSupply(air);
               }
            }

            if (entity.isOnFire() || entity.isInLava()) {
               target.setSecondsOnFire(2);
               soulBurn.put(id, level.getGameTime() + 25L);
               if (level.getGameTime() % 20L == 0L && !target.isInWater()) {
                  TrueDamage.deal(level, (Entity)null, target, 1.0F);
               }
            }

            Vec3 dv = entity.getDeltaMovement();
            boolean flung = entity.getPersistentData().getBoolean("dm_flung");
            if (!flung && dv.length() > 0.4D) {
               entity.getPersistentData().putBoolean("dm_flung", true);
               target.setDeltaMovement(dv.x, Math.max(dv.y, 0.1D), dv.z);
               target.hurtMarked = true;
               target.displayClientMessage(Component.literal("§3Your soul flies, and you go with it."), true);
            } else if (flung && dv.length() < 0.08D) {
               entity.getPersistentData().putBoolean("dm_flung", false);
            }

            BlockPos pos = entity.blockPosition();
            if (level.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
               String key = String.valueOf(level.dimension().location()) + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
               PactLedger.get(level.getServer()).linkPortal(key, id);
               entity.setDeltaMovement(0.0D, 0.4D, 2.0D * (double)(level.random.nextInt(2) == 0 ? 1 : -1) * 0.2D);
               entity.setPos(entity.getX(), entity.getY() + 0.5D, entity.getZ());
               entity.hurtMarked = true;
               Fx.ring(level, 5882600, Vec3.atCenterOf(pos), 1.4D, 12);
               level.playSound((Player)null, pos, DealBridge.sound("minecraft:block.portal.trigger"), SoundSource.PLAYERS, 1.2F, 0.5F);

               for(ServerPlayer near : level.players()) {
                  if (near.distanceToSqr(Vec3.atCenterOf(pos)) < 64.0D) {
                     near.displayClientMessage(Component.literal("§3This portal now opens into " + SoulItem.nameOf(stack) + "'s Soul Plane."), true);
                  }
               }
            }

            return false;
         }
      } else {
         entity.discard();
         return true;
      }
   }

   public static void eaten(ServerPlayer eater, ServerLevel level, ItemStack stack) {
      ServerPlayer target = targetOf(level, stack);
      UUID id = SoulItem.targetOf(stack);
      if (id != null) {
         if (id.equals(eater.getUUID())) {
            eater.displayClientMessage(Component.literal("§3You swallow your own soul. It tastes like your own name."), true);
         } else if (target == null) {
            eater.displayClientMessage(Component.literal("§7The soul is cold. Its owner isn't in the world."), true);
         } else {
            TrueDamage.deal(level, eater, target, target.getHealth() * 0.5F);
            target.sendSystemMessage(Component.literal("§3Someone took a bite of your soul."));
            String power = DealmakerEvents.normalPowerOf(target);
            int rank = DealmakerEvents.rank(eater);
            int dur = Math.min(12000, 20 * (30 + rank * 2));
            if (power == null) {
               eater.displayClientMessage(Component.literal("§7Their power refuses to be swallowed. The Ten cannot be eaten."), true);
            } else {
               String old = (String)eatenPower.remove(eater.getUUID());
               if (old != null) {
                  removeCopy(eater, old);
               }

               DealBridge.run(eater.server, "superpower add unrp:" + power + "_am " + eater.getName().getString());
               DealBridge.lend(eater, power);
               eatenCopy.put(eater.getUUID(), new long[]{(long)(LifeClock.now(eater) + dur)});
               eatenPower.put(eater.getUUID(), power);
               beginDisguise(eater, target.getName().getString());
               eater.displayClientMessage(Component.literal("§3You wear " + target.getName().getString() + "'s power for a time: §f" + power.replace('_', ' ')), true);
            }

            Fx.shell(level, Fx.ember(5882600), eater.position().add(0.0D, 1.0D, 0.0D), 1.2D, 40, 1.2D);
            level.playSound((Player)null, eater.blockPosition(), DealBridge.sound("minecraft:entity.warden.heartbeat"), SoundSource.PLAYERS, 1.6F, 0.6F);
         }
      }
   }

   private static void removeCopy(ServerPlayer eater, String power) {
      DealBridge.run(eater.server, "superpower remove unrp:" + power + "_am " + eater.getName().getString());
   }

   private static void beginDisguise(ServerPlayer eater, String targetName) {
      CompoundTag settings = DealBridge.settingsGet(eater);
      String prior = settings == null ? "" : settings.getString("Alias");
      eatenAlias.put(eater.getUUID(), prior == null ? "" : prior);
      eater.server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.left", new Object[]{eater.getName().getString()}).withStyle(ChatFormatting.YELLOW), false);
      PactEnforcement.applyAlias(eater, targetName);
      DealBridge.run(eater.server, "superpower add unrp:disguise " + eater.getName().getString());
      eater.displayClientMessage(Component.literal("§5You read as §f" + targetName + "§5 now. §7Only your face stays your own."), true);
   }

   public static void endDisguise(ServerPlayer eater, boolean silent) {
      String prior = (String)eatenAlias.remove(eater.getUUID());
      if (prior != null) {
         PactEnforcement.applyAlias(eater, prior);
      }

      DealBridge.run(eater.server, "superpower remove unrp:disguise " + eater.getName().getString());
      if (!silent && prior != null) {
         eater.server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.joined", new Object[]{eater.getName().getString()}).withStyle(ChatFormatting.YELLOW), false);
      }

   }

   @SubscribeEvent
   public static void onTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player copy_0 = event.player;
         if (copy_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)copy_0;
            Level var11 = player.level();
            if (var11 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var11;
               if (player.getTags().remove("unrp.dg_end_trigger")) {
                  long[] active = (long[])eatenCopy.remove(player.getUUID());
                  String power_0 = (String)eatenPower.remove(player.getUUID());
                  if (power_0 != null) {
                     removeCopy(player, power_0);
                  }

                  if (active != null || eatenAlias.containsKey(player.getUUID())) {
                     endDisguise(player, false);
                     player.displayClientMessage(Component.literal("§7You shed the stolen shape by choice."), true);
                  }
               }

               long[] copy = (long[])eatenCopy.get(player.getUUID());
               if (copy != null && (long)LifeClock.now(player) > copy[0]) {
                  eatenCopy.remove(player.getUUID());
                  String power = (String)eatenPower.remove(player.getUUID());
                  if (power != null) {
                     removeCopy(player, power);
                  }

                  endDisguise(player, false);
                  player.displayClientMessage(Component.literal("§7The stolen shape slips away."), true);
               }

               int[] hold = (int[])framedHold.get(player.getUUID());
               if (hold != null) {
                  boolean var10000;
                  label88: {
                     Entity frame = level.getEntity(hold[0]);
                     if (frame instanceof ItemFrame) {
                        ItemFrame f = (ItemFrame)frame;
                        if (f.getItem().getItem() == com.myriam.unrp.item.ModItems.SOUL.get() && player.getUUID().equals(SoulItem.targetOf(f.getItem()))) {
                           var10000 = true;
                           break label88;
                        }
                     }

                     var10000 = false;
                  }

                  boolean valid = var10000;
                  if (!valid) {
                     framedHold.remove(player.getUUID());
                     framedAnchor.remove(player.getUUID());
                     player.displayClientMessage(Component.literal("§7The frame releases you."), true);
                  } else {
                     Vec3 anchor = (Vec3)framedAnchor.get(player.getUUID());
                     if (anchor != null) {
                        player.teleportTo(anchor.x, anchor.y, anchor.z);
                        player.setDeltaMovement(Vec3.ZERO);
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 6, false, false));
                     }
                  }
               }

               if (player.tickCount % 40 == 0) {
                  trackCompasses(player, level);
               }

               if (player.tickCount % 20 == 0) {
                  PactLedger ledger = PactLedger.get(level.getServer());

                  for(UUID lit : ledger.lanternLit()) {
                     if (lit.equals(player.getUUID())) {
                        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, true, false));
                     }
                  }

                  for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                     ItemStack s = player.getInventory().getItem(i);
                     if (s.hasTag() && s.getTag().contains("dm_soulbound_target")) {
                        UUID bt;
                        try {
                           bt = UUID.fromString(s.getTag().getString("dm_soulbound_target"));
                        } catch (IllegalArgumentException var10) {
                           continue;
                        }

                        if (s.getTag().getInt("dm_soulbound_serial") != ledger.serialOf(bt)) {
                           s.shrink(s.getCount());
                           player.displayClientMessage(Component.literal("§7The bound thing frays into soul-smoke. It was summoned again somewhere else."), true);
                        }
                     }
                  }
               }

               return;
            }
         }
      }

   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END && !furnaceBoost.isEmpty()) {
         Iterator<Map.Entry<Long, Object[]>> it = furnaceBoost.entrySet().iterator();

         while(it.hasNext()) {
            Map.Entry<Long, Object[]> e = (Map.Entry)it.next();
            Object[] data = e.getValue();
            ServerLevel level = (ServerLevel)data[0];
            BlockPos pos = BlockPos.of(e.getKey());
            long until = (Long)data[1];
            UUID targetId = (UUID)data[2];
            BlockEntity be = level.getBlockEntity(pos);
            if (level.getGameTime() <= until && be instanceof AbstractFurnaceBlockEntity) {
               AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity)be;

               for(int target = 0; target < 7; ++target) {
                  AbstractFurnaceBlockEntity.serverTick(level, pos, level.getBlockState(pos), furnace);
               }

               if (level.getGameTime() % 20L == 0L) {
                  ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
                  if (target != null) {
                     target.setSecondsOnFire(2);
                     soulBurn.put(targetId, level.getGameTime() + 25L);
                     if (!target.isInWater()) {
                        TrueDamage.deal(level, (Entity)null, target, 1.0F);
                     }
                  }

                  Fx.burst(level, Fx.flare(15235136), Vec3.atCenterOf(pos).add(0.0D, 0.6D, 0.0D), 6, 0.3D, 0.03D);
               }
            } else {
               it.remove();
            }
         }

      }
   }

   private static void trackCompasses(ServerPlayer player, ServerLevel level) {
      for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
         ItemStack stack = player.getInventory().getItem(i);
         if (stack.getItem() == Items.COMPASS && stack.hasTag() && stack.getTag().contains("dm_track")) {
            UUID id;
            try {
               id = UUID.fromString(stack.getTag().getString("dm_track"));
            } catch (IllegalArgumentException var8) {
               continue;
            }

            ServerPlayer target = level.getServer().getPlayerList().getPlayer(id);
            if (target != null) {
               CompoundTag tag = stack.getTag();
               CompoundTag posTag = NbtUtils.writeBlockPos(target.blockPosition());
               tag.put("LodestonePos", posTag);
               tag.putString("LodestoneDimension", target.level().dimension().location().toString());
               tag.putBoolean("LodestoneTracked", false);
            }
         }
      }

   }

   @SubscribeEvent
   public static void onSoulLeftClick(PlayerInteractEvent.LeftClickBlock event) {
      Player main = event.getEntity();
      if (main instanceof ServerPlayer player) {
         Level var10 = player.level();
         if (var10 instanceof ServerLevel level) {
            ItemStack var11 = player.getMainHandItem();
            BlockPos pos = event.getPos();
            BlockState state = level.getBlockState(pos);
            if (var11.getItem() == Items.COMPASS && player.getOffhandItem().getItem() == com.myriam.unrp.item.ModItems.SOUL.get()) {
               ServerPlayer target_0 = targetOf(level, player.getOffhandItem());
               UUID id_0 = SoulItem.targetOf(player.getOffhandItem());
               String name_0 = SoulItem.nameOf(player.getOffhandItem());
               if (id_0 != null) {
                  var11.getOrCreateTag().putString("dm_track", id_0.toString());
                  var11.setHoverName(Component.literal(name_0));
                  player.getOffhandItem().shrink(1);
                  event.setCanceled(true);
                  player.displayClientMessage(Component.literal("§3The compass now hungers for " + name_0 + "."), true);
               }

               return;
            }

            if (var11.getItem() != com.myriam.unrp.item.ModItems.SOUL.get()) {
               return;
            }

            ServerPlayer target = targetOf(level, var11);
            UUID id = SoulItem.targetOf(var11);
            String name = SoulItem.nameOf(var11);
            if (id == null) {
               return;
            }

            if (!state.is(Blocks.CHEST) && !state.is(Blocks.TRAPPED_CHEST) && !state.is(Blocks.BARREL)) {
               if (!state.is(Blocks.FURNACE) && !state.is(Blocks.BLAST_FURNACE) && !state.is(Blocks.SMOKER)) {
                  if (state.is(Blocks.BELL)) {
                     event.setCanceled(true);
                     ServerPlayer far = level.getServer().getPlayerList().getPlayer(id);
                     if (far != null) {
                        far.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.RECORDS, 2.0F, 1.0F);
                        far.displayClientMessage(Component.literal("§3A bell tolls for you, from somewhere."), true);
                     }

                     level.playSound((Player)null, pos, SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
                     return;
                  }

                  if (state.is(Blocks.SOUL_LANTERN)) {
                     event.setCanceled(true);
                     String key = String.valueOf(level.dimension().location()) + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
                     PactLedger.get(level.getServer()).setLantern(key, id);
                     var11.shrink(1);
                     Fx.shell(level, Fx.glint(5882600), Vec3.atCenterOf(pos), 0.7D, 20, 0.4D);
                     level.playSound((Player)null, pos, DealBridge.sound("minecraft:block.amethyst_block.chime"), SoundSource.PLAYERS, 1.4F, 0.7F);
                     player.displayClientMessage(Component.literal("§3" + name + " is held in the lantern's light. They glow until it breaks."), true);
                  }

                  return;
               }

               event.setCanceled(true);
               furnaceBoost.put(pos.asLong(), new Object[]{level, level.getGameTime() + 900L, id});
               Fx.ring(level, 15235136, Vec3.atCenterOf(pos), 1.0D, 10);
               level.playSound((Player)null, pos, DealBridge.sound("minecraft:item.firecharge.use"), SoundSource.PLAYERS, 1.2F, 0.6F);
               player.displayClientMessage(Component.literal("§6Soul Furnace.§7 It burns " + name + " instead of coal."), true);
               return;
            }

            event.setCanceled(true);
            if (target == null) {
               player.displayClientMessage(Component.literal("§7Their soul is here, but they're not in the world."), true);
               return;
            }

            openInventoryMirror(player, target);
            return;
         }
      }

   }

   private static void openInventoryMirror(ServerPlayer viewer, final ServerPlayer target) {
      Container mirror = new Container() {
         public int getContainerSize() {
            return 45;
         }

         public boolean isEmpty() {
            return false;
         }

         public ItemStack getItem(int slot) {
            if (slot < 36) {
               return target.getInventory().getItem(slot);
            } else if (slot < 40) {
               return (ItemStack)target.getInventory().armor.get(slot - 36);
            } else {
               return slot == 40 ? (ItemStack)target.getInventory().offhand.get(0) : ItemStack.EMPTY;
            }
         }

         public ItemStack removeItem(int slot, int amount) {
            ItemStack stack_0 = this.getItem(slot);
            if (stack_0.isEmpty()) {
               return ItemStack.EMPTY;
            } else {
               ItemStack out = stack_0.split(amount);
               this.setChanged();
               return out;
            }
         }

         public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = this.getItem(slot);
            this.setItem(slot, ItemStack.EMPTY);
            return stack;
         }

         public void setItem(int slot, ItemStack stack) {
            if (slot < 36) {
               target.getInventory().setItem(slot, stack);
            } else if (slot < 40) {
               target.getInventory().armor.set(slot - 36, stack);
            } else if (slot == 40) {
               target.getInventory().offhand.set(0, stack);
            }

         }

         public void setChanged() {
            target.getInventory().setChanged();
         }

         public void clearContent() {
            for(int slot = 0; slot <= 40; ++slot) {
               this.setItem(slot, ItemStack.EMPTY);
            }

            this.setChanged();
         }

         public boolean stillValid(Player p) {
            return !target.hasDisconnected();
         }
      };
      viewer.openMenu(new SimpleMenuProvider((id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x5, id, inv, mirror, 5), Component.literal(target.getName().getString() + "'s soul, opened")));
      target.sendSystemMessage(Component.literal("§3Cold fingers leaf through what's yours."));
   }

   @SubscribeEvent
   public static void onSoulStrike(AttackEntityEvent event) {
      Player main_0 = event.getEntity();
      if (main_0 instanceof ServerPlayer player) {
         Level var12 = player.level();
         if (var12 instanceof ServerLevel level) {
            ItemStack main = player.getMainHandItem();
            if (main.getItem() != com.myriam.unrp.item.ModItems.SOUL.get()) {
               return;
            }

            Entity target_0 = event.getTarget();
            if (target_0 instanceof ArmorStand stand) {
               event.setCanceled(true);
               ServerPlayer target = targetOf(level, main);
               if (target == null) {
                  player.displayClientMessage(Component.literal("§7They're not in the world, so you can't swap their gear."), true);
                  return;
               }

               for(EquipmentSlot slot : EquipmentSlot.values()) {
                  if (slot != EquipmentSlot.OFFHAND) {
                     ItemStack fromStand = stand.getItemBySlot(slot).copy();
                     ItemStack fromPlayer = target.getItemBySlot(slot).copy();
                     stand.setItemSlot(slot, fromPlayer);
                     target.setItemSlot(slot, fromStand);
                  }
               }

               Fx.burst(level, Fx.glint(5882600), stand.position().add(0.0D, 1.0D, 0.0D), 14, 0.4D, 0.04D);
               level.playSound((Player)null, stand.blockPosition(), DealBridge.sound("minecraft:entity.armor_stand.place"), SoundSource.PLAYERS, 1.2F, 0.8F);
               target.displayClientMessage(Component.literal("§3Your garb isn't your own to choose today."), true);
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onFramed(PlayerInteractEvent.EntityInteractSpecific event) {
      Player frame_0 = event.getEntity();
      if (frame_0 instanceof ServerPlayer player) {
         Level var7 = player.level();
         if (var7 instanceof ServerLevel level) {
            Entity held_0 = event.getTarget();
            if (held_0 instanceof ItemFrame frame) {
               ItemStack held = player.getItemInHand(event.getHand());
               if (held.getItem() != com.myriam.unrp.item.ModItems.SOUL.get()) {
                  return;
               }

               UUID id = SoulItem.targetOf(held);
               if (id == null) {
                  return;
               }

               ServerPlayer target = level.getServer().getPlayerList().getPlayer(id);
               if (target != null) {
                  framedHold.put(id, new int[]{frame.getId()});
                  framedAnchor.put(id, target.position());
                  target.displayClientMessage(Component.literal("§3You're held. Somewhere, your soul hangs in a frame."), true);
               }

               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onPotionHit(ProjectileImpactEvent event) {
      Projectile at = event.getProjectile();
      if (at instanceof ThrownPotion potion) {
         Level var10 = potion.level();
         if (var10 instanceof ServerLevel level) {
            Vec3 var11 = event.getRayTraceResult().getLocation();

            for(ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, (new AABB(var11, var11)).inflate(2.5D))) {
               if (item.getItem().getItem() == com.myriam.unrp.item.ModItems.SOUL.get()) {
                  ServerPlayer target = targetOf(level, item.getItem());
                  if (target != null) {
                     for(MobEffectInstance effect : PotionUtils.getMobEffects(potion.getItem())) {
                        if (effect.getEffect() == MobEffects.HARM) {
                           Entity thrower = potion.getOwner();
                           TrueDamage.deal(level, thrower, target, (float)(6 << effect.getAmplifier()));
                        } else {
                           target.addEffect(new MobEffectInstance(effect));
                        }
                     }

                     target.displayClientMessage(Component.literal("§3Something poured over your soul finds your veins."), true);
                     Fx.burst(level, Fx.ember(5882600), item.position().add(0.0D, 0.3D, 0.0D), 12, 0.3D, 0.04D);
                  }
               }
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onAnvil(AnvilRepairEvent event) {
      Player out_0 = event.getEntity();
      if (out_0 instanceof ServerPlayer player) {
         Level var7 = player.level();
         if (var7 instanceof ServerLevel level) {
            ItemStack out = event.getOutput();
            if (out.getItem() == com.myriam.unrp.item.ModItems.SOUL.get() && out.hasCustomHoverName()) {
               UUID id = SoulItem.targetOf(out);
               if (id == null) {
                  return;
               }

               String alias = out.getHoverName().getString();
               ServerPlayer target = level.getServer().getPlayerList().getPlayer(id);
               if (target != null) {
                  PactEnforcement.applyAlias(target, alias);
                  target.sendSystemMessage(Component.literal("§3Your name has been rewritten. You're \"" + alias + "\" now."));
               } else {
                  PactLedger.get(level.getServer()).setPendingAlias(id, alias);
               }

               player.displayClientMessage(Component.literal("§3The anvil rings. The name holds."), true);
               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      UUID id = event.getEntity().getUUID();
      framedHold.remove(id);
      framedAnchor.remove(id);
      soulBurn.remove(id);
      eatenCopy.remove(id);
      String power = (String)eatenPower.remove(id);
      if (power != null) {
         Player var4 = event.getEntity();
         if (var4 instanceof ServerPlayer) {
            ServerPlayer player_0 = (ServerPlayer)var4;
            removeCopy(player_0, power);
         }
      }

      if (eatenAlias.containsKey(id)) {
         Player var6 = event.getEntity();
         if (var6 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var6;
            endDisguise(player, true);
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onSoulFireHurt(LivingHurtEvent event) {
      LivingEntity until_0 = event.getEntity();
      if (until_0 instanceof ServerPlayer player) {
         Level var4 = player.level();
         if (var4 instanceof ServerLevel level) {
            Long until = (Long)soulBurn.get(player.getUUID());
            if (until == null) {
               return;
            }

            if (level.getGameTime() > until) {
               soulBurn.remove(player.getUUID());
               return;
            }

            if (event.getSource().is(DamageTypes.ON_FIRE)) {
               event.setCanceled(true);
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onSoulBindCraft(PlayerEvent.ItemCraftedEvent event) {
      Player out_0 = event.getEntity();
      if (out_0 instanceof ServerPlayer player) {
         Level var10 = player.level();
         if (var10 instanceof ServerLevel level) {
            ItemStack out = event.getCrafting();
            if (out.hasTag() && out.getTag().contains("dm_soulbound_target")) {
               UUID target;
               try {
                  target = UUID.fromString(out.getTag().getString("dm_soulbound_target"));
               } catch (IllegalArgumentException var9) {
                  return;
               }

               PactLedger ledger = PactLedger.get(level.getServer());
               int serial = ledger.bumpSerial(target);
               out.getTag().putInt("dm_soulbound_serial", serial);
               ItemStack stored = out.copy();
               stored.getTag().remove("dm_soulbound_serial");
               ledger.setBoundItem(target, stored.save(new CompoundTag()));
               DealBridge.invalidateGear(target);
               player.displayClientMessage(Component.literal("§3The soul settles into the " + out.getHoverName().getString() + ".§7 Summons now call the bound thing itself."), true);
               ServerPlayer victim = level.getServer().getPlayerList().getPlayer(target);
               if (victim != null) {
                  victim.sendSystemMessage(Component.literal("§3Your soul has been worked into an object on a crafting table."));
               }

               level.playSound((Player)null, player.blockPosition(), DealBridge.sound("minecraft:block.smithing_table.use"), SoundSource.PLAYERS, 1.2F, 0.6F);
               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onBoundItemJoin(EntityJoinLevelEvent event) {
      Entity var2 = event.getEntity();
      if (var2 instanceof ItemEntity item) {
         if (item.getItem().hasTag() && item.getItem().getTag().contains("dm_soulbound_target")) {
            item.setUnlimitedLifetime();
            item.setInvulnerable(true);
         }
      }

   }

   @SubscribeEvent
   public static void onBlockBreak(BlockEvent.BreakEvent event) {
      LevelAccessor pos = event.getLevel();
      if (pos instanceof ServerLevel level) {
         BlockPos var7 = event.getPos();
         String key = String.valueOf(level.dimension().location()) + "|" + var7.getX() + "|" + var7.getY() + "|" + var7.getZ();
         PactLedger ledger = PactLedger.get(level.getServer());
         UUID lit = ledger.lanternTarget(key);
         if (lit != null) {
            ledger.setLantern(key, (UUID)null);
            ServerPlayer target = level.getServer().getPlayerList().getPlayer(lit);
            if (target != null) {
               target.displayClientMessage(Component.literal("§7The lantern breaks. The light lets you go."), true);
            }
         }

         furnaceBoost.remove(var7.asLong());
      }
   }
}
