package com.myriam.unrp.deal;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(
   modid = "unrp"
)
public class PactEnforcement {
   private static final UUID HEART_UUID = UUID.fromString("7374c6fc-0002-4a9d-ad61-000000000001");
   private static final UUID ATK_UUID = UUID.fromString("7374c6fc-0002-4a9d-ad61-000000000002");
   private static final UUID SPD_UUID = UUID.fromString("7374c6fc-0002-4a9d-ad61-000000000003");
   private static final UUID ARMOR_UUID = UUID.fromString("7374c6fc-0002-4a9d-ad61-000000000004");
   private static final UUID TOUGH_UUID = UUID.fromString("7374c6fc-0002-4a9d-ad61-000000000005");
   private static final UUID MOVE_UUID = UUID.fromString("7374c6fc-0002-4a9d-ad61-000000000006");
   private static final UUID KBRES_UUID = UUID.fromString("7374c6fc-0002-4a9d-ad61-000000000007");
   private static final UUID EREACH_UUID = UUID.fromString("7374c6fc-0002-4a9d-ad61-000000000008");
   private static final UUID BREACH_UUID = UUID.fromString("7374c6fc-0002-4a9d-ad61-000000000009");
   private static final Map<UUID, PactEnforcement.Rules> cache = new HashMap();
   private static final Map<UUID, List<ItemStack>> keepStash = new HashMap();
   private static final Set<UUID> betraying = new HashSet();
   private static final UUID WOUNDS_UUID = UUID.fromString("7374c6fc-0002-4a9d-ad61-00000000000a");
   private static MinecraftServer deathMsgFlipped;

   private PactEnforcement() {
   }

   public static PactEnforcement.Rules rulesOf(MinecraftServer server, UUID player) {
      PactEnforcement.Rules rules = (PactEnforcement.Rules)cache.get(player);
      return rules != null ? rules : rebuild(server, player);
   }

   public static void refresh(MinecraftServer server, UUID player) {
      if (player != null) {
         rebuild(server, player);
         ServerPlayer online = server.getPlayerList().getPlayer(player);
         if (online != null) {
            applyAttributes(online, (PactEnforcement.Rules)cache.get(player));
            syncGrants(online, (PactEnforcement.Rules)cache.get(player));
         }

      }
   }

   private static void syncGrants(ServerPlayer player, PactEnforcement.Rules r) {
      Set<String> gaveNow = new HashSet();
      Set<String> tookNow = new HashSet();

      for(String tag : new ArrayList<String>(player.getTags())) {
         if (tag.startsWith("unrp.pact_gave.")) {
            gaveNow.add(tag.substring("unrp.pact_gave.".length()));
         } else if (tag.startsWith("unrp.pact_took.")) {
            tookNow.add(tag.substring("unrp.pact_took.".length()));
         }
      }

      Set<String> wantGain = new HashSet(r.powersGained);
      if (r.boons.contains("projection")) {
         wantGain.add("boon_projection");
      }

      if (r.boons.contains("travel")) {
         wantGain.add("boon_travel");
      }

      for(String p_0 : wantGain) {
         if (!gaveNow.contains(p_0) && !DealBridge.titanHolds(player) && !p_0.equals("legacy") && !player.getTags().contains("unrp.has." + p_0)) {
            superpower(player, "add", p_0);
            player.addTag("unrp.pact_gave." + p_0);
         }
      }

      for(String p : gaveNow) {
         if (!wantGain.contains(p)) {
            superpower(player, "remove", p);
            player.removeTag("unrp.pact_gave." + p);
            player.removeTag("unrp.has." + p);
         }
      }

      for(String p : r.powersLost) {
         if (!tookNow.contains(p) && !p.equals("legacy") && player.getTags().contains("unrp.has." + p) && !DealBridge.isOriginPower(p)) {
            superpower(player, "remove", p);
            player.addTag("unrp.pact_took." + p);
         }
      }

      for(String p : tookNow) {
         if (!r.powersLost.contains(p) && !DealBridge.titanHolds(player)) {
            superpower(player, "add", p);
            player.removeTag("unrp.pact_took." + p);
         }
      }

      if (r.boons.contains("allied")) {
         player.addTag("unrp.pact_allied");
      } else {
         player.removeTag("unrp.pact_allied");
      }

   }

   private static void superpower(ServerPlayer player, String op, String power) {
      DealBridge.run(player.server, "superpower " + op + " unrp:" + power + " " + player.getName().getString());
   }

   private static PactEnforcement.Rules rebuild(MinecraftServer server, UUID player) {
      PactEnforcement.Rules rules = new PactEnforcement.Rules();
      PactLedger ledger = PactLedger.get(server);

      for(PactLedger.Pact pact : ledger.signedPactsTouching(player)) {
         if (PactFate.conditionsActive(server, pact)) {
            for(PactLedger.Term t : pact.terms) {
               UUID beneficiary = pact.partyOf(t.side);
               UUID other = pact.partyOf(1 - t.side);
               if (beneficiary != null) {
                  boolean mine = beneficiary.equals(player);
                  if (mine) {
                     fold(rules, t, other, server);
                  } else if (other != null && other.equals(player)) {
                     foldPayer(rules, t, beneficiary);
                  }

                  if (t.cat.equals("curse") && t.key.equals("vanishing") && player.equals(pact.signer)) {
                     rules.vanishTo = pact.owner;
                  }
               }
            }
         }
      }

      cache.put(player, rules);
      return rules;
   }

   public static int forcedMult(ServerPlayer player) {
      PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
      if (r.curses.contains("dampen")) {
         return 50;
      } else {
         return r.boons.contains("amplify") ? 150 : 0;
      }
   }

   private static void fold(PactEnforcement.Rules r, PactLedger.Term t, UUID other, MinecraftServer server) {
      switch (t.cat) {
         case "hearts":
            r.heartsAdd += (t.key.equals("lose") ? -t.v : t.v) * 2.0D;
            break;
         case "attr":
            switch (t.key) {
               case "atk":
                  r.atkMul = t.v;
                  return;
               case "atkspd":
                  r.atkSpdMul = t.v;
                  return;
               case "armor":
                  r.armorAdd += t.v;
                  return;
               case "armorlose":
                  r.armorAdd -= t.v;
                  return;
               case "tough":
                  r.toughAdd += t.v;
                  return;
               case "toughlose":
                  r.toughAdd -= t.v;
                  return;
               case "speed":
                  r.moveMul = t.v;
                  return;
               case "kb":
                  r.kbMul = t.v;
                  return;
               case "kbres":
                  r.kbRes = t.v;
                  return;
               case "reach":
                  r.reach = t.v;
                  return;
               case "mine":
                  r.mineMul = t.v;
                  return;
               case "jump":
                  r.jumpMul = t.v;
                  return;
               case "grav":
                  r.gravMul = t.v;
                  return;
               case "size":
                  r.sizeMul = t.v;
                  return;
               case "dmgred":
                  r.dmgRed = t.v;
                  return;
               default:
                  return;
            }
         case "effect":
            r.permEffects.add(new String[]{t.a, String.valueOf((int)t.v)});
            break;
         case "immune":
            r.immuneEffects.add(t.a);
            break;
         case "rule":
            switch (t.key) {
               case "keepinv":
                  r.keepInv = t.v >= 1.0D;
                  return;
               case "regen":
                  r.regenOff = t.v < 1.0D;
                  return;
               case "hunger":
                  r.hungerLock = t.v < 1.0D;
                  return;
               case "durability":
                  r.durabilityOff = t.v < 1.0D;
                  return;
               case "insomnia":
                  r.insomniaOff = t.v < 1.0D;
                  return;
               case "sleep":
                  r.sleepOff = t.v < 1.0D;
                  return;
               case "deathmsg":
                  r.deathMsgOff = t.v < 1.0D;
                  return;
               default:
                  return;
            }
         case "boon":
            r.boons.add(t.key);
            if (t.key.equals("lucky")) {
               r.permEffects.add(new String[]{"minecraft:luck", "3"});
            }
            break;
         case "curse":
            r.curses.add(t.key);
            if (t.key.equals("binding")) {
               r.permEffects.add(new String[]{"minecraft:slowness", "1"});
            }
            break;
         case "ability":
            switch (t.key) {
               case "gain":
                  r.powersGained.add(t.a);
                  return;
               case "lose":
                  r.powersLost.add(t.a);
                  return;
               case "move_off":
                  r.sealedMoves.add(t.a);
                  return;
               default:
                  return;
            }
         case "protect_player":
            r.immunePlayers.add(t.a.toLowerCase());
            break;
         case "protect_type":
            r.immuneTypes.add(t.a);
            break;
         case "prohibit_player":
            r.prohibitPlayers.add(t.a.toLowerCase());
            break;
         case "prohibit_mob":
            r.prohibitMobs.add(t.a);
            break;
         case "prohibit_item":
            r.prohibitItems.add(t.a);
            break;
         case "prohibit_dim":
            r.prohibitDims.add(t.a);
            break;
         case "prohibit_area":
            r.prohibitAreas.add(new int[]{t.x1, t.z1, t.x2, t.z2});
            r.areaDims.add(t.a);
            break;
         case "reveal_loc":
            if (other != null) {
               r.reveals.add(new Object[]{other, "loc"});
            }
            break;
         case "reveal_health":
            if (other != null) {
               r.reveals.add(new Object[]{other, "hp"});
            }
      }

   }

   private static void foldPayer(PactEnforcement.Rules r, PactLedger.Term t, UUID beneficiary) {
      switch (t.cat) {
         case "xp_share":
            r.xpOut.add(new Object[]{beneficiary, t.v});
            break;
         case "drop_claim":
            r.dropClaims.put(t.a, beneficiary);
      }

   }

   private static void applyAttributes(ServerPlayer player, PactEnforcement.Rules r) {
      double hearts = Math.max(-19.0D, r.heartsAdd);
      set(player.getAttribute(Attributes.MAX_HEALTH), HEART_UUID, "Pact hearts", hearts, Operation.ADDITION, hearts != 0.0D);
      if (player.getHealth() > player.getMaxHealth()) {
         player.setHealth(player.getMaxHealth());
      }

      set(player.getAttribute(Attributes.ATTACK_DAMAGE), ATK_UUID, "Pact damage", r.atkMul == null ? 0.0D : r.atkMul - 1.0D, Operation.MULTIPLY_TOTAL, r.atkMul != null);
      set(player.getAttribute(Attributes.ATTACK_SPEED), SPD_UUID, "Pact tempo", r.atkSpdMul == null ? 0.0D : r.atkSpdMul - 1.0D, Operation.MULTIPLY_TOTAL, r.atkSpdMul != null);
      set(player.getAttribute(Attributes.ARMOR), ARMOR_UUID, "Pact plate", Math.max(-30.0D, r.armorAdd), Operation.ADDITION, r.armorAdd != 0.0D);
      set(player.getAttribute(Attributes.ARMOR_TOUGHNESS), TOUGH_UUID, "Pact temper", Math.max(-20.0D, r.toughAdd), Operation.ADDITION, r.toughAdd != 0.0D);
      set(player.getAttribute(Attributes.MOVEMENT_SPEED), MOVE_UUID, "Pact stride", r.moveMul == null ? 0.0D : r.moveMul - 1.0D, Operation.MULTIPLY_TOTAL, r.moveMul != null);
      set(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KBRES_UUID, "Pact footing", r.kbRes == null ? 0.0D : r.kbRes, Operation.ADDITION, r.kbRes != null);
      set(player.getAttribute((Attribute)ForgeMod.ENTITY_REACH.get()), EREACH_UUID, "Pact reach", r.reach == null ? 0.0D : r.reach - 3.0D, Operation.ADDITION, r.reach != null);
      set(player.getAttribute((Attribute)ForgeMod.BLOCK_REACH.get()), BREACH_UUID, "Pact grasp", r.reach == null ? 0.0D : r.reach - 4.5D, Operation.ADDITION, r.reach != null);
      if (!DealmakerEvents.imbued(player.getUUID())) {
         double scale = r.sizeMul == null ? 1.0D : r.sizeMul;
         DealBridge.run(player.server, "scale set pehkui:base " + scale + " " + player.getName().getString());
      }

   }

   public static double sizeOf(MinecraftServer server, UUID player) {
      PactEnforcement.Rules r = rulesOf(server, player);
      return r.sizeMul == null ? 1.0D : r.sizeMul;
   }

   private static void set(AttributeInstance attr, UUID id, String name, double value, AttributeModifier.Operation op, boolean active) {
      if (attr != null) {
         AttributeModifier old = attr.getModifier(id);
         if (old != null) {
            if (active && old.getAmount() == value) {
               return;
            }

            attr.removeModifier(id);
         }

         if (active) {
            attr.addPermanentModifier(new AttributeModifier(id, name, value, op));
         }

      }
   }

   @SubscribeEvent
   public static void onTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player r_0 = event.player;
         if (r_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)r_0;
            Level var13 = player.level();
            if (var13 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var13;
               PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
               if (r.gravMul != null && !player.onGround() && !player.getAbilities().flying && !player.isFallFlying() && !player.isInWater()) {
                  Vec3 dv = player.getDeltaMovement();
                  double adjust = 0.08D * (1.0D - r.gravMul) * 0.98D;
                  double ny = Math.max(-3.5D, Math.min(3.5D, dv.y + adjust));
                  player.setDeltaMovement(dv.x, ny, dv.z);
                  player.hurtMarked = true;
                  if (r.gravMul < 0.0D) {
                     player.fallDistance = 0.0F;
                  }
               }

               if (r.curses.contains("gills")) {
                  if (player.isEyeInFluid(FluidTags.WATER)) {
                     player.setAirSupply(player.getMaxAirSupply());
                  } else {
                     int air = player.getAirSupply() - 8;
                     if (air <= 0) {
                        air = 0;
                        if (player.tickCount % 20 == 0) {
                           player.hurt(level.damageSources().drown(), 2.0F);
                        }
                     }

                     player.setAirSupply(air);
                  }
               }

               if (r.curses.contains("uninvited") && player.tickCount % 5 == 0) {
                  BlockState feet = level.getBlockState(player.blockPosition());
                  BlockState head = level.getBlockState(player.blockPosition().above());
                  boolean inDoor = feet.getBlock() instanceof DoorBlock || head.getBlock() instanceof DoorBlock || feet.getBlock() instanceof FenceGateBlock;
                  if (inDoor) {
                     Vec3 center = Vec3.atCenterOf(player.blockPosition());
                     Vec3 push = player.position().subtract(center);
                     Vec3 flat = new Vec3(push.x, 0.0D, push.z);
                     Vec3 dir = flat.lengthSqr() < 1.0E-4D ? Vec3.directionFromRotation(0.0F, player.getYRot()).scale(-1.0D) : flat.normalize();
                     player.setDeltaMovement(dir.x * 0.6D, 0.1D, dir.z * 0.6D);
                     player.hurtMarked = true;
                     player.displayClientMessage(Component.literal("§5You are not invited through."), true);
                  }
               }

               if (player.tickCount % 20 != 0) {
                  return;
               }

               if (r.curses.contains("greed")) {
                  int filled = 0;

                  for(int i = 0; i < 36; ++i) {
                     if (!player.getInventory().getItem(i).isEmpty()) {
                        ++filled;
                     }
                  }

                  if (filled >= 9) {
                     int amp_0 = filled >= 27 ? 2 : (filled >= 18 ? 1 : 0);
                     player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 260, amp_0, true, false));
                  }
               }

               if (r.boons.contains("allied")) {
                  player.addTag("unrp.pact_allied");
               } else if (player.getTags().contains("unrp.pact_allied")) {
                  player.removeTag("unrp.pact_allied");
               }

               for(String[] eff : r.permEffects) {
                  MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(eff[0]));
                  if (effect != null) {
                     int amp = Math.max(0, Integer.parseInt(eff[1]) - 1);
                     player.addEffect(new MobEffectInstance(effect, 260, amp, true, false));
                  }
               }

               if (r.hungerLock) {
                  player.getFoodData().setFoodLevel(20);
                  player.getFoodData().setSaturation(10.0F);
               }

               if (r.durabilityOff) {
                  for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                     ItemStack stack_0 = player.getInventory().getItem(i);
                     if (stack_0.isDamaged()) {
                        stack_0.setDamageValue(0);
                     }
                  }
               }

               if (r.insomniaOff) {
                  player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
               }

               for(String itemId : r.prohibitItems) {
                  Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                  if (item != null) {
                     for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (stack.getItem() == item) {
                           ItemStack out = stack.copy();
                           stack.shrink(stack.getCount());
                           player.drop(out, false);
                           player.displayClientMessage(Component.literal("§7The contract won't let you hold that."), true);
                        }
                     }
                  }
               }

               String dimId = level.dimension().location().toString();

               for(int i = 0; i < r.prohibitAreas.size(); ++i) {
                  if (((String)r.areaDims.get(i)).equals(dimId)) {
                     int[] a = (int[])r.prohibitAreas.get(i);
                     int minX = Math.min(a[0], a[2]);
                     int maxX = Math.max(a[0], a[2]);
                     int minZ = Math.min(a[1], a[3]);
                     int maxZ = Math.max(a[1], a[3]);
                     if (player.getX() >= (double)minX && player.getX() <= (double)maxX && player.getZ() >= (double)minZ && player.getZ() <= (double)maxZ) {
                        double pushX = Math.abs(player.getX() - (double)minX) < Math.abs((double)maxX - player.getX()) ? (double)minX - 2.0D : (double)maxX + 2.0D;
                        player.teleportTo(pushX, player.getY(), player.getZ());
                        player.displayClientMessage(Component.literal("§7The contract bars you from this ground."), true);
                     }
                  }
               }

               return;
            }
         }
      }

   }

   @SubscribeEvent
   public static void onHeal(LivingHealEvent event) {
      LivingEntity r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         if (player.getTags().contains("unrp.dm_ritual")) {
            if (PactLedger.get(player.server).ritualRunning(player.getUUID())) {
               event.setCanceled(true);
               return;
            }

            player.removeTag("unrp.dm_ritual");
         }

         PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
         if (r.regenOff) {
            event.setCanceled(true);
         } else if (r.curses.contains("decay") && !player.hasEffect(MobEffects.REGENERATION)) {
            event.setCanceled(true);
         } else {
            if (r.boons.contains("overflow")) {
               float room = player.getMaxHealth() - player.getHealth();
               float over = event.getAmount() - room;
               if (over > 0.0F) {
                  float cap = player.getMaxHealth();
                  float abs = Math.min(cap, player.getAbsorptionAmount() + over);
                  player.setAbsorptionAmount(abs);
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onEffect(MobEffectEvent.Applicable event) {
      LivingEntity r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
         if (!r.immuneEffects.isEmpty()) {
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(event.getEffectInstance().getEffect());
            if (id != null && r.immuneEffects.contains(id.toString())) {
               event.setResult(Result.DENY);
            }

         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onHurt(LivingHurtEvent event) {
      boolean truth = event.getSource().is(TrueDamage.TYPE);
      LivingEntity r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer victim_0) {
         if (!truth) {
            PactEnforcement.Rules r = rulesOf(victim_0.server, victim_0.getUUID());
            Entity var5 = event.getSource().getEntity();
            if (var5 instanceof ServerPlayer) {
               ServerPlayer attacker_0 = (ServerPlayer)var5;
               if (r.immunePlayers.contains(attacker_0.getName().getString().toLowerCase())) {
                  event.setCanceled(true);
                  return;
               }
            }

            if (!r.immuneTypes.isEmpty() && matchesType(event, r.immuneTypes)) {
               event.setCanceled(true);
               return;
            }
         }
      }

      var rr1 = event.getEntity();
      if (rr1 instanceof ServerPlayer victim) {
         if (!truth) {
            PactEnforcement.Rules r = rulesOf(victim.server, victim.getUUID());
            if (r.boons.contains("lucky") && victim.getRandom().nextFloat() < 0.15F) {
               event.setCanceled(true);
               victim.displayClientMessage(Component.literal("§6Luck turns the blade."), true);
               return;
            }

            if (r.dmgRed != null) {
               event.setAmount((float)((double)event.getAmount() * Math.max(0.0D, 1.0D - r.dmgRed / 100.0D)));
               if (event.getAmount() <= 0.0F) {
                  event.setCanceled(true);
                  return;
               }
            }
         }
      }

      Entity var11 = event.getSource().getEntity();
      if (var11 instanceof ServerPlayer attacker) {
         PactEnforcement.Rules r = rulesOf(attacker.server, attacker.getUUID());
         LivingEntity var15 = event.getEntity();
         if (var15 instanceof ServerPlayer victim) {
            if (r.prohibitPlayers.contains(victim.getName().getString().toLowerCase())) {
               event.setCanceled(true);
               attacker.displayClientMessage(Component.literal("§7The contract stays your hand."), true);
               return;
            }
         }

         ResourceLocation type = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
         if (type != null && r.prohibitMobs.contains(type.toString())) {
            event.setCanceled(true);
            attacker.displayClientMessage(Component.literal("§7The contract stays your hand."), true);
            return;
         }

         if (r.curses.contains("betrayal") && !betraying.contains(attacker.getUUID()) && (event.getEntity() instanceof ServerPlayer || event.getEntity().hasCustomName()) && attacker.getRandom().nextFloat() < 0.25F) {
            betraying.add(attacker.getUUID());
            attacker.hurt(attacker.damageSources().magic(), event.getAmount());
            betraying.remove(attacker.getUUID());
            attacker.displayClientMessage(Component.literal("§5The curse returns your blow to you."), true);
         }
      }

   }

   @SubscribeEvent
   public static void onFinalDamage(LivingDamageEvent event) {
      LivingEntity r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         if (!(event.getAmount() <= 0.0F)) {
            PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
            if (r.curses.contains("wounds") && !event.getSource().is(TrueDamage.TYPE)) {
               int debt = score(player, "unrp.dm_wounds") + Math.max(1, Math.round(event.getAmount() * 0.25F));
               setScore(player, "unrp.dm_wounds", debt);
               applyWounds(player, debt);
               return;
            }

            return;
         }
      }

   }

   public static void applyWounds(ServerPlayer player, int debt) {
      AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
      if (attr != null) {
         AttributeModifier old = attr.getModifier(WOUNDS_UUID);
         double base = attr.getValue() - (old == null ? 0.0D : old.getAmount());
         double applied = -Math.min((double)debt, Math.max(0.0D, base - 1.0D));
         if (old != null) {
            attr.removeModifier(WOUNDS_UUID);
         }

         if (applied < 0.0D) {
            attr.addPermanentModifier(new AttributeModifier(WOUNDS_UUID, "Open wounds", applied, Operation.ADDITION));
         }

         if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
         }

      }
   }

   @SubscribeEvent
   public static void onWake(PlayerWakeUpEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (score(player, "unrp.dm_wounds") > 0) {
            setScore(player, "unrp.dm_wounds", 0);
            applyWounds(player, 0);
            player.displayClientMessage(Component.literal("§6Sleep closes the old wounds."), true);
         }

      }
   }

   @SubscribeEvent
   public static void onToss(ItemTossEvent event) {
      Player r_0 = event.getPlayer();
      if (r_0 instanceof ServerPlayer player) {
         PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
         if (r.curses.contains("binding") && Tossed.byHand(event.getEntity())) {
            ItemStack back = event.getEntity().getItem();
            player.getInventory().add(back);
            if (!back.isEmpty()) {
               player.displayClientMessage(Component.literal("§5Your pockets are full: what the curse cannot hold falls."), true);
            } else {
               event.setCanceled(true);
               player.displayClientMessage(Component.literal("§5The curse binds your possessions to you."), true);
            }
         }
      }
   }

   private static int score(ServerPlayer p, String obj) {
      ServerScoreboard sb = p.server.getScoreboard();
      Objective o = sb.getObjective(obj);
      return o == null ? 0 : sb.getOrCreatePlayerScore(p.getScoreboardName(), o).getScore();
   }

   private static void setScore(ServerPlayer p, String obj, int val) {
      ServerScoreboard sb = p.server.getScoreboard();
      Objective o = sb.getObjective(obj);
      if (o != null) {
         sb.getOrCreatePlayerScore(p.getScoreboardName(), o).setScore(val);
      }

   }

   private static boolean matchesType(LivingHurtEvent event, Set<String> types) {
      for(String t : types) {
         boolean var10000;
         switch (t) {
            case "fire":
               var10000 = event.getSource().is(DamageTypeTags.IS_FIRE);
               break;
            case "fall":
               var10000 = event.getSource().is(DamageTypeTags.IS_FALL);
               break;
            case "explosion":
               var10000 = event.getSource().is(DamageTypeTags.IS_EXPLOSION);
               break;
            case "projectile":
               var10000 = event.getSource().is(DamageTypeTags.IS_PROJECTILE);
               break;
            case "drown":
               var10000 = event.getSource().is(DamageTypeTags.IS_DROWNING);
               break;
            case "freeze":
               var10000 = event.getSource().is(DamageTypeTags.IS_FREEZING);
               break;
            case "lightning":
               var10000 = event.getSource().is(DamageTypeTags.IS_LIGHTNING);
               break;
            case "magic":
               var10000 = event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO);
               break;
            case "physical":
               var10000 = !event.getSource().is(DamageTypeTags.IS_FIRE) && !event.getSource().is(DamageTypeTags.IS_EXPLOSION) && !event.getSource().is(DamageTypeTags.IS_PROJECTILE) && !event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO) && event.getSource().getDirectEntity() != null;
               break;
            default:
               var10000 = false;
         }

         boolean hit = var10000;
         if (hit) {
            return true;
         }
      }

      return false;
   }

   @SubscribeEvent
   public static void onJump(LivingEvent.LivingJumpEvent event) {
      LivingEntity r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
         if (r.jumpMul != null) {
            Vec3 dv = player.getDeltaMovement();
            player.setDeltaMovement(dv.x, dv.y * r.jumpMul, dv.z);
            player.hurtMarked = true;
         }
      }
   }

   @SubscribeEvent
   public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
      Player r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
         if (r.mineMul != null) {
            event.setNewSpeed((float)((double)event.getNewSpeed() * r.mineMul));
         }

      }
   }

   @SubscribeEvent
   public static void onXpChange(PlayerXpEvent.XpChange event) {
      Player r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         if (event.getAmount() > 0) {
            PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
            if (r.xpOut.isEmpty()) {
               return;
            }

            int amount = event.getAmount();
            int taken = 0;

            for(Object[] share : r.xpOut) {
               UUID to = (UUID)share[0];
               double pct = (Double)share[1];
               int cut = (int)Math.floor((double)amount * pct / 100.0D);
               if (cut > 0) {
                  ServerPlayer receiver = player.server.getPlayerList().getPlayer(to);
                  if (receiver != null) {
                     receiver.giveExperiencePoints(cut);
                     taken += cut;
                  }
               }
            }

            if (taken > 0) {
               event.setAmount(Math.max(0, amount - taken));
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onPickup(EntityItemPickupEvent event) {
      Player r = event.getEntity();
      if (r instanceof ServerPlayer player) {
         PactEnforcement.Rules var8 = rulesOf(player.server, player.getUUID());
         ItemEntity itemEntity = event.getItem();
         ResourceLocation id = ForgeRegistries.ITEMS.getKey(itemEntity.getItem().getItem());
         if (id != null) {
            if (var8.prohibitItems.contains(id.toString())) {
               event.setCanceled(true);
            } else {
               UUID claimant = (UUID)var8.dropClaims.get(id.toString());
               if (claimant != null && !claimant.equals(player.getUUID())) {
                  ServerPlayer receiver = player.server.getPlayerList().getPlayer(claimant);
                  if (receiver != null) {
                     ItemStack claimed = itemEntity.getItem().copy();
                     itemEntity.discard();
                     event.setCanceled(true);
                     if (!receiver.getInventory().add(claimed)) {
                        receiver.drop(claimed, false);
                     }

                     player.displayClientMessage(Component.literal("§7The contract sends that to the other party."), true);
                  }
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
      Player r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
         if (!r.prohibitItems.isEmpty()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
            if (id != null && r.prohibitItems.contains(id.toString())) {
               event.setCanceled(true);
            }

         }
      }
   }

   @SubscribeEvent
   public static void onTravel(EntityTravelToDimensionEvent event) {
      Entity r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         if (!DealBridge.myWorldReturning(player.getUUID())) {
            PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
            if (r.prohibitDims.contains(event.getDimension().location().toString())) {
               event.setCanceled(true);
               player.displayClientMessage(Component.literal("§7That world is closed to you by contract."), true);
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onSleep(PlayerSleepInBedEvent event) {
      Player r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
         if (r.sleepOff) {
            event.setResult(BedSleepingProblem.NOT_POSSIBLE_NOW);
            player.displayClientMessage(Component.literal("§7Your terms don't allow sleep."), true);
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onVanishing(LivingDropsEvent event) {
      LivingEntity r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
         if (r.curses.contains("vanishing") && r.vanishTo != null) {
            List<ItemStack> taken = new ArrayList();
            event.getDrops().forEach((drop) -> taken.add(drop.getItem().copy()));
            event.getDrops().clear();
            event.setCanceled(true);
            if (!taken.isEmpty()) {
               ServerPlayer owner = player.server.getPlayerList().getPlayer(r.vanishTo);
               if (owner != null) {
                  for(ItemStack stack_0 : taken) {
                     if (!owner.getInventory().add(stack_0)) {
                        owner.drop(stack_0, false);
                     }
                  }

                  owner.displayClientMessage(Component.literal("§6The curse delivers " + player.getName().getString() + "'s possessions."), true);
               } else {
                  ListTag list = new ListTag();

                  for(ItemStack stack : taken) {
                     list.add(stack.save(new CompoundTag()));
                  }

                  PactLedger.get(player.server).setPendingItems(r.vanishTo, list);
               }

               player.sendSystemMessage(Component.literal("§5The Curse of Vanishing carries everything you held to The Dealmaker."));
            }
         }
      }
   }

   @SubscribeEvent
   public static void onDeathMessage(LivingDeathEvent event) {
      LivingEntity r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
         if (r.deathMsgOff) {
            GameRules rules = player.server.getGameRules();
            if (rules.getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)) {
               ((GameRules.BooleanValue)rules.getRule(GameRules.RULE_SHOWDEATHMESSAGES)).set(false, player.server);
               deathMsgFlipped = player.server;
            }

         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END && deathMsgFlipped != null) {
         MinecraftServer server = deathMsgFlipped;
         deathMsgFlipped = null;
         ((GameRules.BooleanValue)server.getGameRules().getRule(GameRules.RULE_SHOWDEATHMESSAGES)).set(true, server);
      }
   }

   @SubscribeEvent
   public static void onDrops(LivingDropsEvent event) {
      LivingEntity r_0 = event.getEntity();
      if (r_0 instanceof ServerPlayer player) {
         PactEnforcement.Rules r = rulesOf(player.server, player.getUUID());
         if (r.keepInv) {
            List<ItemStack> stash = new ArrayList();
            event.getDrops().forEach((drop) -> stash.add(drop.getItem().copy()));
            event.getDrops().clear();
            event.setCanceled(true);
            keepStash.put(player.getUUID(), stash);
         }
      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
      Player stash_0 = event.getEntity();
      if (stash_0 instanceof ServerPlayer player) {
         List<ItemStack> stash = (List)keepStash.remove(player.getUUID());
         if (stash != null) {
            for(ItemStack stack : stash) {
               if (!player.getInventory().add(stack)) {
                  player.drop(stack, false);
               }
            }

            player.displayClientMessage(Component.literal("§6Your goods return, as agreed."), true);
         }

         int debt = score(player, "unrp.dm_wounds");
         if (debt > 0) {
            applyWounds(player, debt);
         }

         refresh(player.server, player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onKnockback(LivingKnockBackEvent event) {
      LivingEntity attacker = event.getEntity().getLastHurtByMob();
      if (attacker instanceof ServerPlayer sp) {
         PactEnforcement.Rules r = rulesOf(sp.server, sp.getUUID());
         if (r.kbMul != null) {
            event.setStrength((float)((double)event.getStrength() * r.kbMul));
         }
      }

   }

   public static void onSigned(ServerLevel level, PactLedger ledger, PactLedger.Pact pact, PactLedger.Term t, ServerPlayer owner, ServerPlayer signer) {
      ServerPlayer beneficiary = t.side == 1 ? owner : signer;
      switch (t.cat) {
         case "item":
            if (t.side == 0 && signer != null) {
               give(signer, t.a, (int)t.v);
            } else if (t.side == 1 && owner != null) {
               give(owner, t.a, (int)t.v);
            }
            break;
         case "soul_own":
            if (t.side == 1 && pact.signer != null) {
               ledger.setSoulOwner(pact.signer, pact.owner);
               if (signer != null) {
                  signer.sendSystemMessage(Component.literal("§3Your soul now sits in the Dealmaker's ledger."));
               }
            } else if (t.side == 0 && pact.signer != null) {
               releaseSoul(level, ledger, pact.signer);
            }
            break;
         case "soul_release":
            UUID released = byName(level, t.a);
            if (released != null) {
               releaseSoul(level, ledger, released);
            }
            break;
         case "alias":
            if (pact.signer != null) {
               ServerPlayer target_0 = level.getServer().getPlayerList().getPlayer(pact.signer);
               if (target_0 != null) {
                  applyAlias(target_0, t.a);
               } else {
                  ledger.setPendingAlias(pact.signer, t.a);
               }
            }
            break;
         case "title":
            if (pact.signer != null) {
               ledger.setTitle(pact.signer, t.a);
            }
            break;
         case "disguise":
            if (pact.signer != null) {
               ServerPlayer target = level.getServer().getPlayerList().getPlayer(pact.signer);
               if (target != null) {
                  applyAlias(target, t.a);
                  target.sendSystemMessage(Component.literal("§5The contract pulls " + t.a + "'s name over you like a hood."));
               } else {
                  ledger.setPendingAlias(pact.signer, t.a);
               }

               if (level.getServer().getPlayerList().getPlayerByName(t.a) == null) {
                  level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.joined", new Object[]{t.a}).withStyle(ChatFormatting.YELLOW), false);
               }
            }
      }

      if (beneficiary != null && !t.cat.equals("note")) {
         beneficiary.displayClientMessage(Component.literal("§6The pact grants: §7" + describe(t)), true);
      }

   }

   public static void onDestroyed(ServerLevel level, PactLedger ledger, PactLedger.Pact pact, PactLedger.Term t) {
      switch (t.cat) {
         case "disguise":
            if (pact.signer != null) {
               ServerPlayer target_0 = level.getServer().getPlayerList().getPlayer(pact.signer);
               if (target_0 != null) {
                  applyAlias(target_0, "");
                  target_0.sendSystemMessage(Component.literal("§7The hood lifts. Your own name settles back."));
               } else {
                  ledger.setPendingAlias(pact.signer, "");
               }
            }
            break;
         case "alias":
            if (pact.signer != null) {
               ServerPlayer target = level.getServer().getPlayerList().getPlayer(pact.signer);
               if (target != null) {
                  applyAlias(target, "");
               } else {
                  ledger.setPendingAlias(pact.signer, "");
               }
            }
            break;
         case "title":
            if (pact.signer != null) {
               ledger.setTitle(pact.signer, "");
            }
      }

   }

   public static void releaseSoul(ServerLevel level, PactLedger ledger, UUID target) {
      UUID keeper = ledger.soulOwnerOf(target);
      ledger.setSoulOwner(target, (UUID)null);
      ledger.bumpSerial(target);

      for(PactLedger.Pact p : ledger.allSigned()) {
         if (p.soul && target.equals(p.signer)) {
            DealmakerEvents.destroyPact(level, ledger, p, (ServerPlayer)null);
         }
      }

      if (keeper != null) {
         int seat = ledger.rankOf(keeper, target);
         if (seat >= 0) {
            ledger.setBind(keeper, seat, (UUID)null, "");
            ServerPlayer freedAlly = level.getServer().getPlayerList().getPlayer(target);
            if (freedAlly != null) {
               DealmakerEvents.stripBindPackage(freedAlly);
               freedAlly.removeTag("unrp.has.soul_bound");
               DealBridge.run(level.getServer(), "superpower remove unrp:soul_bound " + freedAlly.getName().getString());
            }
         }
      }

      ServerPlayer freed = level.getServer().getPlayerList().getPlayer(target);
      if (freed != null) {
         freed.sendSystemMessage(Component.literal("§bYour soul is your own again."));
      }

   }

   public static void applyAlias(ServerPlayer player, String alias) {
      CompoundTag current = DealBridge.settingsGet(player);
      CompoundTag updated = current == null ? new CompoundTag() : current.copy();
      updated.putString("Alias", alias == null ? "" : alias);
      DealBridge.settingsApply(player, updated);
   }

   private static void give(ServerPlayer player, String itemId, int count) {
      Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
      if (item != null) {
         int put;
         for(int left = Math.max(1, count); left > 0; left -= put) {
            put = Math.min(left, item.getMaxStackSize(new ItemStack(item)));
            ItemStack stack = new ItemStack(item, put);
            if (!player.getInventory().add(stack)) {
               player.drop(stack, false);
            }
         }

      }
   }

   private static UUID byName(ServerLevel level, String name) {
      ServerPlayer online = level.getServer().getPlayerList().getPlayerByName(name);
      if (online != null) {
         return online.getUUID();
      } else {
         Optional<GameProfile> cached = level.getServer().getProfileCache() == null ? Optional.empty() : level.getServer().getProfileCache().get(name);
         return cached.map(GameProfile::getId).orElse(null);
      }
   }

   public static List<String> infoLines(MinecraftServer server, ServerPlayer player) {
      PactEnforcement.Rules r = rulesOf(server, player.getUUID());
      List<String> out = new ArrayList();

      for(Object[] reveal : r.reveals) {
         UUID target = (UUID)reveal[0];
         ServerPlayer online = server.getPlayerList().getPlayer(target);
         String name = online != null ? online.getName().getString() : "offline";
         if (online == null) {
            out.add("§8" + name);
         } else if (reveal[1].equals("loc")) {
            BlockPos pos = online.blockPosition();
            out.add("§f" + name + " §7@ §f" + pos.getX() + " " + pos.getY() + " " + pos.getZ());
         } else {
            out.add("§f" + name + " §7HP §c" + String.format("%.0f", online.getHealth()) + "§7/§c" + String.format("%.0f", online.getMaxHealth()));
         }
      }

      return out;
   }

   public static boolean validateTerm(ServerPlayer player, PactLedger.Term t) {
      double up = 1.0D;
      switch (t.cat) {
         case "hearts":
            double cap = DealmakerEvents.capFor(player, 3.0D, 50.0D);
            return t.v >= 1.0D && t.v <= cap && (t.key.equals("gain") || t.key.equals("lose"));
         case "attr":
            switch (t.key) {
               case "atk":
               case "atkspd":
                  up = DealmakerEvents.capFor(player, 1.5D, 10.0D);
                  break;
               case "armor":
               case "armorlose":
                  return t.v >= 0.0D && t.v <= DealmakerEvents.capFor(player, 6.0D, 30.0D);
               case "tough":
               case "toughlose":
                  return t.v >= 0.0D && t.v <= DealmakerEvents.capFor(player, 4.0D, 20.0D);
               case "speed":
                  up = DealmakerEvents.capFor(player, 1.4D, 5.0D);
                  break;
               case "kb":
                  up = DealmakerEvents.capFor(player, 1.4D, 5.0D);
                  break;
               case "kbres":
                  return t.v >= 0.0D && t.v <= DealmakerEvents.capFor(player, 0.3D, 1.0D);
               case "reach":
                  return t.v >= 0.0D && t.v <= DealmakerEvents.capFor(player, 5.5D, 15.0D);
               case "mine":
                  up = DealmakerEvents.capFor(player, 2.0D, 30.0D);
                  break;
               case "jump":
                  up = DealmakerEvents.capFor(player, 1.6D, 10.0D);
                  break;
               case "grav":
                  double dev_0 = DealmakerEvents.capFor(player, 0.6D, 6.0D);
                  return t.v >= Math.max(-5.0D, 1.0D - dev_0) && t.v <= Math.min(5.0D, 1.0D + dev_0);
               case "size":
                  double dev = DealmakerEvents.capFor(player, 0.5D, 4.0D);
                  return t.v >= Math.max(0.1D, 1.0D - dev) && t.v <= Math.min(5.0D, 1.0D + dev);
               case "dmgred":
                  return t.v >= 0.0D && t.v <= DealmakerEvents.capFor(player, 20.0D, 100.0D);
               default:
                  return false;
            }

            return t.v >= 0.0D && t.v <= up;
         case "effect":
            return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(t.a)) != null && t.v >= 1.0D && t.v <= Math.max(1.0D, (double)Math.round(DealmakerEvents.capFor(player, 1.0D, 5.0D)));
         case "immune":
            return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(t.a)) != null;
         case "item":
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation(t.a)) != null && t.v >= 1.0D && t.v <= DealmakerEvents.capFor(player, 16.0D, 256.0D);
         case "xp_share":
            return t.v >= 1.0D && t.v <= DealmakerEvents.capFor(player, 15.0D, 100.0D);
         case "drop_claim":
         case "prohibit_item":
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation(t.a)) != null;
         case "prohibit_mob":
            return ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(t.a)) != null;
         case "prohibit_dim":
            return t.a.contains(":");
         case "prohibit_area":
            long dx = Math.abs((long)t.x2 - (long)t.x1);
            long dz = Math.abs((long)t.z2 - (long)t.z1);
            long capSide = Math.round(DealmakerEvents.capFor(player, 128.0D, 4096.0D));
            return t.a.contains(":") && dx <= capSide && dz <= capSide;
         case "alias":
         case "title":
            return t.a.length() <= 32;
         case "disguise":
            return t.side == 0 && !t.a.isEmpty() && t.a.length() <= 16;
         case "note":
            return t.text.length() <= 200;
         case "soul_own":
         case "soul_release":
         case "protect_player":
         case "protect_type":
         case "prohibit_player":
         case "reveal_loc":
         case "reveal_health":
         case "rule":
            return true;
         case "boon":
            return t.side == 0 && Set.of("immortal", "projection", "echo", "adapt", "travel", "amplify", "overflow", "lucky", "allied").contains(t.key);
         case "curse":
            return t.side == 0 && Set.of("gills", "uninvited", "greed", "betrayal", "decay", "repetition", "witnessing", "knowledge", "wounds", "dampen", "vanishing", "binding").contains(t.key);
         case "ability":
            switch (t.key) {
               case "gain":
               case "lose":
                  return DealBridge.grantablePowers(player.server).contains(t.a);
               case "move_off":
                  int colon = t.a.indexOf(58);
                  if (colon <= 0) {
                     return false;
                  } else {
                     String power = t.a.substring(0, colon);
                     if (DealBridge.isOriginPower(power)) {
                        return false;
                     } else {
                        List<DealBridge.MoveInfo> moves = (List)DealBridge.table(player.server).get(power);
                        if (moves == null) {
                           return false;
                        } else {
                           try {
                              int slot = Integer.parseInt(t.a.substring(colon + 1));

                              for(DealBridge.MoveInfo m : moves) {
                                 if (m.slot == slot) {
                                    return true;
                                 }
                              }

                              return false;
                           } catch (NumberFormatException var14) {
                              return false;
                           }
                        }
                     }
                  }
               default:
                  return false;
            }
         case "cond":
            boolean var28;
            switch (t.key) {
               case "time":
                  var28 = t.a.equals("day") || t.a.equals("night");
                  break;
               case "health":
                  var28 = t.v >= 0.0D && t.v <= 100.0D;
                  break;
               case "prox":
                  var28 = !t.a.isEmpty();
                  break;
               case "dim":
                  var28 = t.a.contains(":");
                  break;
               default:
                  var28 = false;
            }

            return var28;
         case "bind":
            boolean var10000;
            switch (t.key) {
               case "timer":
                  var10000 = t.v >= 1.0D;
                  break;
               case "death":
               case "violence":
                  var10000 = true;
                  break;
               case "attack":
                  var10000 = !t.a.isEmpty();
                  break;
               case "dim":
                  var10000 = !t.a.isEmpty();
                  break;
               default:
                  var10000 = false;
            }

            return var10000;
         case "punish":
            return t.side == 0 && Set.of("soul", "death", "possessions", "contract", "xp", "levels", "seal").contains(t.key);
         default:
            return false;
      }
   }

   public static String describe(PactLedger.Term t) {
      String who = t.side == 1 ? "The Owner" : "The Signer";
      String var10000;
      switch (t.cat) {
         case "hearts":
            var10000 = who + (t.key.equals("lose") ? " loses " : " gains ") + (int)t.v + " hearts";
            break;
         case "attr":
            String var25;
            switch (t.key) {
               case "atk":
                  var25 = "deals " + t.v + "x damage";
                  break;
               case "atkspd":
                  var25 = "swings at " + t.v + "x speed";
                  break;
               case "armor":
                  var25 = "gains " + (int)t.v + " armor";
                  break;
               case "armorlose":
                  var25 = "loses " + (int)t.v + " armor";
                  break;
               case "tough":
                  var25 = "gains " + (int)t.v + " toughness";
                  break;
               case "toughlose":
                  var25 = "loses " + (int)t.v + " toughness";
                  break;
               case "speed":
                  var25 = "moves at " + t.v + "x speed";
                  break;
               case "kb":
                  var25 = "knocks back at " + t.v + "x";
                  break;
               case "kbres":
                  var25 = "stands at " + Math.round(t.v * 100.0D) + "% knockback resistance";
                  break;
               case "reach":
                  var25 = "reaches " + t.v + " blocks";
                  break;
               case "mine":
                  var25 = "mines at " + t.v + "x speed";
                  break;
               case "jump":
                  var25 = "jumps at " + t.v + "x height";
                  break;
               case "grav":
                  var25 = "falls at " + t.v + "x gravity";
                  break;
               case "size":
                  var25 = "stands at " + t.v + "x size";
                  break;
               case "dmgred":
                  var25 = "shrugs off " + (int)t.v + "% of all damage, before all else";
                  break;
               default:
                  var25 = "?";
            }

            var10000 = who + " " + var25;
            break;
         case "effect":
            var10000 = who + " permanently holds " + prettyId(t.a) + " " + (int)t.v;
            break;
         case "immune":
            var10000 = who + " is immune to " + prettyId(t.a);
            break;
         case "item":
            var10000 = who + " receives " + (int)t.v + "x " + prettyId(t.a);
            break;
         case "soul_own":
            var10000 = t.side == 1 ? "The Owner receives the Signer's SOUL" : "The Signer's soul is returned, and all soul-pacts on it are void";
            break;
         case "soul_release":
            var10000 = "You release " + t.a + "'s soul";
            break;
         case "rule":
            String var10001;
            switch (t.key) {
               case "keepinv":
                  var10001 = t.v >= 1.0D ? "keeps inventory on death" : "drops inventory";
                  break;
               case "regen":
                  var10001 = t.v >= 1.0D ? "regenerates naturally" : "no natural regeneration";
                  break;
               case "hunger":
                  var10001 = t.v >= 1.0D ? "hungers normally" : "never hungers";
                  break;
               case "durability":
                  var10001 = t.v >= 1.0D ? "wears tools normally" : "tools never wear";
                  break;
               case "insomnia":
                  var10001 = t.v >= 1.0D ? "phantoms may come" : "phantoms never come";
                  break;
               case "sleep":
                  var10001 = t.v >= 1.0D ? "can sleep" : "can't sleep";
                  break;
               case "deathmsg":
                  var10001 = t.v >= 1.0D ? "dies aloud" : "dies unannounced";
                  break;
               default:
                  var10001 = "?";
            }

            var10000 = who + ": " + var10001;
            break;
         case "ability":
            switch (t.key) {
               case "gain":
                  return who + " gains the ability " + prettyId(t.a);
               case "lose":
                  return who + " loses the ability " + prettyId(t.a);
               case "move_off":
                  return who + "'s move " + prettyId(t.a.replace(':', ' ').replace(" ", " slot ")) + " is sealed";
               default:
                  return "?";
            }
         case "boon":
            switch (t.key) {
               case "immortal":
                  var10000 = "the Signer's first death each rest is refused";
                  break;
               case "projection":
                  var10000 = "the Signer can walk as a ghost (new bar)";
                  break;
               case "echo":
                  var10000 = "the Signer's abilities echo a second time";
                  break;
               case "adapt":
                  var10000 = "the Signer adapts: four survived hits of one move grant immunity";
                  break;
               case "travel":
                  var10000 = "the Signer can pair two doors into a passage (new bar)";
                  break;
               case "amplify":
                  var10000 = "the Signer is permanently Amplified";
                  break;
               case "overflow":
                  var10000 = "healing past full becomes absorption, up to double";
                  break;
               case "lucky":
                  var10000 = "the Signer holds Luck III and sometimes dodges outright";
                  break;
               case "allied":
                  var10000 = "Ember and Spectre agents leave the Signer be";
                  break;
               default:
                  var10000 = "?";
            }

            var10000 = "BOON: " + var10000;
            break;
         case "curse":
            switch (t.key) {
               case "gills":
                  var10000 = "the Signer can only breathe water";
                  break;
               case "uninvited":
                  var10000 = "no door or gate admits the Signer";
                  break;
               case "greed":
                  var10000 = "the heavier the Signer's pockets, the slower their steps";
                  break;
               case "betrayal":
                  var10000 = "the Signer's blows sometimes land on themselves";
                  break;
               case "decay":
                  var10000 = "only regeneration can heal the Signer";
                  break;
               case "repetition":
                  var10000 = "the Signer can't use the same move twice in a row";
                  break;
               case "witnessing":
                  var10000 = "unseen for five seconds, the Signer's power stops working";
                  break;
               case "knowledge":
                  var10000 = "anyone looking at the Signer sees their cooldowns";
                  break;
               case "wounds":
                  var10000 = "a quarter of all damage cuts the Signer's max health until they sleep";
                  break;
               case "dampen":
                  var10000 = "the Signer is permanently Dampened";
                  break;
               case "vanishing":
                  var10000 = "on death, everything the Signer holds goes to the Owner";
                  break;
               case "binding":
                  var10000 = "the Signer is slowed and can't let go of anything";
                  break;
               default:
                  var10000 = "?";
            }

            var10000 = "CURSE: " + var10000;
            break;
         case "cond":
            switch (t.key) {
               case "time":
                  var10000 = "the sun " + (t.a.equals("day") ? "is up" : "is down");
                  break;
               case "health":
                  var10000 = "the Signer is under " + (int)t.v + "% health";
                  break;
               case "prox":
                  var10000 = "the Signer is " + (t.v >= 1.0D ? "near " : "far from ") + t.a;
                  break;
               case "dim":
                  var10000 = (t.v >= 1.0D ? "inside " : "outside ") + t.a;
                  break;
               default:
                  var10000 = "?";
            }

            var10000 = "WHEN: " + var10000;
            break;
         case "bind":
            switch (t.key) {
               case "timer":
                  var10000 = (int)t.v + " minutes have passed";
                  break;
               case "death":
                  var10000 = "the Signer dies";
                  break;
               case "attack":
                  var10000 = "the Signer attacks " + t.a;
                  break;
               case "dim":
                  var10000 = "the Signer enters " + t.a;
                  break;
               case "violence":
                  var10000 = "the Signer harms anything";
                  break;
               default:
                  var10000 = "?";
            }

            var10000 = "UNTIL: " + var10000;
            break;
         case "punish":
            switch (t.key) {
               case "soul":
                  var10000 = "the Owner takes the Signer's soul";
                  break;
               case "death":
                  var10000 = "the Signer dies on the spot";
                  break;
               case "possessions":
                  var10000 = "everything the Signer carries goes to the Owner";
                  break;
               case "contract":
                  var10000 = "the Signer pre-signs a fresh blank contract for the Owner";
                  break;
               case "xp":
                  var10000 = "all the Signer's experience flows to the Owner";
                  break;
               case "levels":
                  var10000 = "half the Signer's levels flow to the Owner";
                  break;
               case "seal":
                  var10000 = "the Signer's ability is sealed and passes to the Owner";
                  break;
               default:
                  var10000 = "?";
            }

            var10000 = "ON BREACH: " + var10000;
            break;
         case "protect_player":
            var10000 = who + " is immune to " + t.a + "'s damage";
            break;
         case "protect_type":
            var10000 = who + " is immune to " + t.a + " damage";
            break;
         case "xp_share":
            var10000 = who + " receives " + (int)t.v + "% of the other's XP";
            break;
         case "drop_claim":
            var10000 = who + " claims all " + prettyId(t.a) + " the other acquires";
            break;
         case "prohibit_player":
            var10000 = who + " can't harm " + t.a;
            break;
         case "prohibit_mob":
            var10000 = who + " can't harm " + prettyId(t.a);
            break;
         case "prohibit_area":
            var10000 = who + " is barred from (" + t.x1 + "," + t.z1 + ")-(" + t.x2 + "," + t.z2 + ") in " + t.a;
            break;
         case "prohibit_dim":
            var10000 = who + " is barred from " + t.a;
            break;
         case "prohibit_item":
            var10000 = who + " can't touch " + prettyId(t.a);
            break;
         case "reveal_loc":
            var10000 = who + " always sees the other's location";
            break;
         case "reveal_health":
            var10000 = who + " always sees the other's health";
            break;
         case "alias":
            var10000 = "The Signer's alias becomes \"" + t.a + "\"";
            break;
         case "title":
            var10000 = "The Signer bears the title [" + t.a + "]";
            break;
         case "disguise":
            var10000 = "The Signer is disguised as " + t.a + " until destruction";
            break;
         case "note":
            var10000 = t.text;
            break;
         default:
            var10000 = t.cat;
      }

      return var10000;
   }

   private static String prettyId(String id) {
      int colon = id.indexOf(58);
      String path = colon >= 0 ? id.substring(colon + 1) : id;
      return path.replace('_', ' ');
   }

   public static void dropCache(UUID player) {
      cache.remove(player);
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      UUID id = event.getEntity().getUUID();
      cache.remove(id);
      List<ItemStack> stash = (List)keepStash.remove(id);
      if (stash != null && !stash.isEmpty()) {
         Player list_0 = event.getEntity();
         if (list_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)list_0;
            if (player.getServer() != null) {
               ListTag list = new ListTag();

               for(ItemStack stack : stash) {
                  list.add(stack.save(new CompoundTag()));
               }

               PactLedger.get(player.getServer()).setPendingItems(id, list);
            }
         }
      }

   }

   @SubscribeEvent
   public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
      Player ledger_0 = event.getEntity();
      if (ledger_0 instanceof ServerPlayer player) {
         if (player.getServer() != null) {
            PactLedger ledger = PactLedger.get(player.getServer());
            int levels = ledger.takePendingLevels(player.getUUID());
            if (levels > 0) {
               player.giveExperienceLevels(levels);
               player.sendSystemMessage(Component.literal("§6The pact pays out §f" + levels + "§6 levels owed to you."));
            }

            String powers = ledger.takePendingPowers(player.getUUID());
            if (powers != null) {
               for(String p : powers.split(",")) {
                  if (!p.isEmpty()) {
                     superpower(player, "add", p);
                  }
               }

               player.sendSystemMessage(Component.literal("§6A sealed ability, claimed in your absence, settles into you."));
            }

            ListTag list = ledger.takePendingItems(player.getUUID());
            if (list == null) {
               return;
            }

            for(int i = 0; i < list.size(); ++i) {
               ItemStack stack = ItemStack.of(list.getCompound(i));
               if (!stack.isEmpty() && !player.getInventory().add(stack)) {
                  player.drop(stack, false);
               }
            }

            player.displayClientMessage(Component.literal("§6Your goods return, as agreed."), true);
            return;
         }
      }

   }

   public static class Rules {
      public double heartsAdd;
      public double armorAdd;
      public double toughAdd;
      public Double atkMul;
      public Double atkSpdMul;
      public Double moveMul;
      public Double kbMul;
      public Double kbRes;
      public Double reach;
      public Double mineMul;
      public Double jumpMul;
      public Double gravMul;
      public Double sizeMul;
      public final List<String[]> permEffects = new ArrayList();
      public final Set<String> immuneEffects = new HashSet();
      public boolean keepInv;
      public boolean regenOff;
      public boolean hungerLock;
      public boolean durabilityOff;
      public boolean insomniaOff;
      public boolean sleepOff;
      public final Set<String> immunePlayers = new HashSet();
      public final Set<String> immuneTypes = new HashSet();
      public final Set<String> prohibitPlayers = new HashSet();
      public final Set<String> prohibitMobs = new HashSet();
      public final Set<String> prohibitItems = new HashSet();
      public final Set<String> prohibitDims = new HashSet();
      public final List<int[]> prohibitAreas = new ArrayList();
      public final List<String> areaDims = new ArrayList();
      public final List<Object[]> xpOut = new ArrayList();
      public final Map<String, UUID> dropClaims = new HashMap();
      public final List<Object[]> reveals = new ArrayList();
      public Double dmgRed;
      public boolean deathMsgOff;
      public final Set<String> boons = new HashSet();
      public final Set<String> curses = new HashSet();
      public final Set<String> sealedMoves = new HashSet();
      public final Set<String> powersGained = new HashSet();
      public final Set<String> powersLost = new HashSet();
      public UUID vanishTo;
   }
}
