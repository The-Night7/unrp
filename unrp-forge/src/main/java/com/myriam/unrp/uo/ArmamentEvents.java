package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;
import com.myriam.unrp.anim.MoveAnim;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraftforge.common.Tags.EntityTypes;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.threetag.palladium.power.PowerManager;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class ArmamentEvents {
   public static final int VAULT_SIZE = 12;
   public static final int GOLD = 15254106;
   public static final String NBT_MARK = "unrp_ad";
   public static final String NBT_WORTH = "unrp_ad_worth";
   public static final String NBT_TYPE = "unrp_ad_type";
   public static final String NBT_SLOT = "unrp_ad_slot";
   public static final String NBT_OWNER = "unrp_ad_owner";
   public static final String NBT_BORN = "unrp_ad_born";
   public static final int PROC_NONE = 0;
   public static final int PROC_BURST = 1;
   public static final int PROC_SEAR = 2;
   public static final int PROC_VENOM = 3;
   public static final int PROC_WARP = 4;
   public static final int PROC_WITHER = 5;
   private static final String[] PROC_NAMES = new String[]{"", "Bursting", "Searing", "Venomous", "Warping", "Sapping"};
   private static final String[] PROC_DESC = new String[]{"", "detonates on impact", "sets targets ablaze", "injects venom", "warps targets away", "saps the target's strength"};
   private static final UUID DMG_UUID = UUID.fromString("8c2e5ab1-6d34-4e5f-9b12-4aa0c2e6da01");
   private static final UUID SPD_UUID = UUID.fromString("8c2e5ab1-6d34-4e5f-9b12-4aa0c2e6da02");
   public static final int ANTICIPATION = 8;
   public static final int STAGGER = 2;
   public static final double AIM_REACH = 48.0D;
   public static final int VOLLEY_SPREAD = 14;
   public static final int FAN_SPLIT = 6;
   public static final double FAN_INNER = 2.15D;
   public static final double FAN_OUTER = 3.15D;
   public static final double FAN_BACK_INNER = 1.05D;
   public static final double FAN_BACK_OUTER = 1.6D;
   public static final double FAN_SPAN = 100.0D;
   public static final double FAN_SPAN_OUTER = 108.0D;
   public static final double FAN_HEIGHT = 1.9D;
   public static final double FAN_SQUASH = 0.85D;
   public static final double GATE_ROOT = 1.2D;
   public static final double GATE_CLEARANCE = 0.45D;
   public static final float SUBDUE = 0.5F;
   private static final Map<UUID, Integer> SHOT_CHARGE = new HashMap();
   private static final Map<UUID, Boolean> BANG_PENDING = new HashMap();
   private static final Map<UUID, ArmamentEvents.Volley> VOLLEYS = new HashMap();
   private static final Map<UUID, Map<UUID, Integer>> VOLLEY_HITS = new HashMap();
   private static final Map<UUID, Long> PROC_CD = new HashMap();

   private static boolean noncanonOn(ServerPlayer player) {
      return ConfigBridgeEvents.flag(player.server, "noncanon", false) || OriginsConfig.isModDev(player.getUUID());
   }

   private static boolean active(ServerPlayer player) {
      return player.getTags().contains("unrp.ad_on");
   }

   public static int appraiseItem(ItemStack stack) {
      String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
      int worth;
      if (!id.contains("netherite") && !id.equals("nether_star") && !id.equals("beacon")) {
         if (!id.equals("elytra") && !id.equals("totem_of_undying") && !id.contains("dragon")) {
            if (!id.contains("diamond") && !id.contains("emerald")) {
               if (id.contains("gold")) {
                  worth = 7;
               } else if (!id.contains("amethyst") && !id.contains("iron")) {
                  if (!id.contains("copper") && !id.contains("lapis")) {
                     if (!id.contains("stone") && !id.contains("cobble") && !id.contains("brick")) {
                        if (!id.contains("wood") && !id.contains("stick") && !id.contains("plank") && !id.contains("log")) {
                           Rarity r = stack.getRarity();
                           worth = r == Rarity.EPIC ? 9 : (r == Rarity.RARE ? 7 : (r == Rarity.UNCOMMON ? 5 : 3));
                        } else {
                           worth = 2;
                        }
                     } else {
                        worth = 3;
                     }
                  } else {
                     worth = 4;
                  }
               } else {
                  worth = 6;
               }
            } else {
               worth = 8;
            }
         } else {
            worth = 9;
         }
      } else {
         worth = 10;
      }

      int ench = stack.getEnchantmentTags().size();
      if (ench >= 3) {
         worth += 2;
      } else if (ench >= 1) {
         ++worth;
      }

      if (stack.getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE)) {
         ++worth;
      }

      return Math.max(1, Math.min(10, worth));
   }

   private static int procOf(LivingEntity mob) {
      byte var10000;
      switch (BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath()) {
         case "creeper":
            var10000 = 1;
            break;
         case "blaze":
         case "magma_cube":
            var10000 = 2;
            break;
         case "spider":
         case "cave_spider":
         case "bee":
            var10000 = 3;
            break;
         case "enderman":
         case "endermite":
         case "shulker":
            var10000 = 4;
            break;
         case "zombie":
         case "husk":
         case "drowned":
         case "zombie_villager":
            var10000 = 5;
            break;
         default:
            var10000 = 0;
      }

      return var10000;
   }

   private static int appraiseEntity(ServerPlayer player, LivingEntity mob) {
      int worth = 2 + Math.round(mob.getMaxHealth() / 8.0F);

      for(String tag : mob.getTags()) { // pouvoirs Palladium : nos tags « unrp.has.* » / « unrp.power.* »
         if (tag.startsWith("unrp.has.") || tag.startsWith("unrp.power.")) {
            worth += 2;
            break;
         }
      }

      return Math.max(2, Math.min(10, worth));
   }

   private static String tierName(int worth) {
      if (worth >= 10) {
         return "Netherite Blade";
      } else if (worth >= 8) {
         return "Diamond Blade";
      } else if (worth >= 7) {
         return "Golden Blade";
      } else if (worth >= 5) {
         return "Iron Blade";
      } else {
         return worth >= 4 ? "Stone Blade" : "Wooden Blade";
      }
   }

   private static String armamentName(int worth, int type) {
      String var10000;
      switch (type) {
         case 1:
            var10000 = "Creeper Blade";
            break;
         case 2:
            var10000 = "Blaze Blade";
            break;
         case 3:
            var10000 = "Fang Blade";
            break;
         case 4:
            var10000 = "Warp Blade";
            break;
         case 5:
            var10000 = "Rotting Blade";
            break;
         default:
            var10000 = tierName(worth);
      }

      return var10000;
   }

   public static ItemStack display(int worth, int type) {
      return ArmamentItem.display(new ItemStack(com.myriam.unrp.item.ModItems.ARMAMENT.get()), worth, type);
   }

   private static ItemStack buildArmament(ServerPlayer owner, int worth, int type, int slot) {
      ItemStack stack = display(worth, type);
      CompoundTag tag = stack.getOrCreateTag();
      tag.putBoolean("unrp_ad", true);
      tag.putInt("unrp_ad_worth", worth);
      tag.putInt("unrp_ad_type", type);
      tag.putInt("unrp_ad_slot", slot);
      tag.putUUID("unrp_ad_owner", owner.getUUID());
      tag.putLong("unrp_ad_born", owner.serverLevel().getGameTime());
      tag.putBoolean("Unbreakable", true);
      tag.putInt("HideFlags", 7);
      CompoundTag display = new CompoundTag();
      display.putString("Name", "{\"text\":\"Armament: " + armamentName(worth, type) + "\",\"color\":\"gold\",\"italic\":false}");
      ListTag lore = new ListTag();
      lore.add(StringTag.valueOf("{\"text\":\"Worth " + worth + "\",\"color\":\"yellow\",\"italic\":false}"));
      if (type != 0) {
         lore.add(StringTag.valueOf("{\"text\":\"" + PROC_NAMES[type] + ": " + PROC_DESC[type] + "\",\"color\":\"gray\",\"italic\":false}"));
      }

      lore.add(StringTag.valueOf("{\"text\":\"Dissolves when the dominion ends\",\"color\":\"dark_gray\",\"italic\":true}"));
      display.put("Lore", lore);
      tag.put("display", display);
      stack.addAttributeModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(DMG_UUID, "unrp_ad_dmg", 2.0D + (double)worth, Operation.ADDITION), EquipmentSlot.MAINHAND);
      stack.addAttributeModifier(Attributes.ATTACK_SPEED, new AttributeModifier(SPD_UUID, "unrp_ad_spd", -2.4D, Operation.ADDITION), EquipmentSlot.MAINHAND);
      return stack;
   }

   private static boolean isArmament(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      return tag != null && tag.getBoolean("unrp_ad");
   }

   private static int vaultGet(ServerPlayer p, int slot) {
      return score(p, "unrp.ad_slot_" + slot);
   }

   private static void vaultSet(ServerPlayer p, int slot, int value) {
      setScore(p, "unrp.ad_slot_" + slot, value);
   }

   private static int vaultCount(ServerPlayer p) {
      int n = 0;

      for(int i = 1; i <= 12; ++i) {
         if (vaultGet(p, i) > 0) {
            ++n;
         }
      }

      return n;
   }

   private static int vaultAdd(ServerPlayer p, int worth, int type) {
      for(int i = 1; i <= 12; ++i) {
         if (vaultGet(p, i) <= 0) {
            vaultSet(p, i, worth * 100 + type);
            return i;
         }
      }

      return -1;
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player tags = event.player;
         if (tags instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)tags;
            Set var7 = player.getTags();
            ServerLevel level = player.serverLevel();
            if (var7.remove("unrp.ad_off_trigger") && !active(player)) {
               dissolveManifested(player, true);
               SHOT_CHARGE.remove(player.getUUID());
               closeVolley(player, level);
               MoveAnim.stop(player, "ad_aim");
            }

            boolean enabled = noncanonOn(player);
            if (var7.remove("unrp.ad_forge_trigger")) {
               if (!enabled) {
                  bar(player, "§cNon-canon powers are disabled on this server.");
               } else if (player.isShiftKeyDown()) {
                  sneakForge(player, level);
               } else {
                  forge(player, level);
               }
            }

            if (var7.remove("unrp.ad_recall_trigger")) {
               if (!enabled) {
                  bar(player, "§cNon-canon powers are disabled on this server.");
               } else if (player.isShiftKeyDown()) {
                  if (dissolveManifested(player, false)) {
                     bar(player, "§7The armament sinks back into the vault.");
                  } else {
                     bar(player, "§7You have no armament out.");
                  }
               } else {
                  recall(player, level);
               }
            }

            if (var7.contains("unrp.ad_shot_charging")) {
               if (!enabled) {
                  player.removeTag("unrp.ad_shot_charging");
               } else {
                  int ct_0 = SHOT_CHARGE.merge(player.getUUID(), 1, Integer::sum);
                  MoveAnim.hold(player, "ad_aim", 3);
                  if (ct_0 == 1) {
                     level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:item.crossbow.loading_start"), SoundSource.PLAYERS, 0.8F, 1.5F);
                  }

                  if (ct_0 % 8 == 0) {
                     Vec3 tip = fingertip(player);
                     Fx.burst(level, Fx.flare(15254106), tip, 2, 0.08D, 0.01D);
                  }

                  if (Boolean.TRUE.equals(BANG_PENDING.remove(player.getUUID()))) {
                     player.removeTag("unrp.ad_shot_charging");
                     SHOT_CHARGE.remove(player.getUUID());
                     fireShot(player, level, ct_0);
                  }
               }
            }

            if (var7.remove("unrp.ad_shot_release")) {
               Integer ct = (Integer)SHOT_CHARGE.remove(player.getUUID());
               BANG_PENDING.remove(player.getUUID());
               if (ct != null && enabled) {
                  fireShot(player, level, ct);
               }
            }

            if (var7.remove("unrp.ad_full_trigger")) {
               if (!enabled) {
                  bar(player, "§cNon-canon powers are disabled on this server.");
               } else if (vaultCount(player) == 0) {
                  setScore(player, "unrp.aura", score(player, "unrp.aura") + 40);
                  bar(player, "§7Your vault is empty. Forge armaments first.");
               } else {
                  beginFullArsenal(player, level);
               }
            }

            tickVolley(player, level);
         }
      }
   }

   private static Vec3 fingertip(ServerPlayer player) {
      Vec3 look = player.getLookAngle();
      Vec3 up0 = Math.abs(look.y) > 0.97D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
      Vec3 right = look.cross(up0).normalize();
      return player.getEyePosition().add(right.scale(0.31D)).add(0.0D, -0.27D, 0.0D).add(look.scale(0.94D));
   }

   private static void sneakForge(ServerPlayer player, ServerLevel level) {
      ItemStack held = player.getMainHandItem();
      if (isArmament(held)) {
         CompoundTag tag = held.getTag();
         int slot = tag.getInt("unrp_ad_slot");
         vaultSet(player, slot, 0);
         player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.glass.break"), SoundSource.PLAYERS, 0.7F, 1.3F);
         Fx.burst(level, Fx.glint(15254106), player.position().add(0.0D, 1.1D, 0.0D), 12, 0.4D, 0.06D);
         MoveAnim.play(player, "ad_shatter");
         bar(player, "§6Shattered from the vault. §7(" + vaultCount(player) + "/12)");
      } else {
         LivingEntity mob = lookMob(player);
         if (mob != null) {
            int worth_0 = appraiseEntity(player, mob);
            int proc = procOf(mob);
            String hold = resists(mob);
            bar(player, "§6Worth " + worth_0 + "§7: would forge §f" + armamentName(worth_0, proc) + (proc != 0 ? " §7(" + PROC_DESC[proc] + ")" : "") + (hold != null ? "§c, but " + hold : ""));
         } else if (!held.isEmpty()) {
            int worth = appraiseItem(held);
            bar(player, "§6Worth " + worth + "§7: would forge §f" + tierName(worth) + " §7(" + (3 + worth) + " damage)");
         } else {
            listVault(player);
         }
      }
   }

   private static void listVault(ServerPlayer player) {
      List<String> names = new ArrayList();

      for(int i = 1; i <= 12; ++i) {
         int enc = vaultGet(player, i);
         if (enc > 0) {
            names.add("§f" + armamentName(enc / 100, enc % 100) + " §7(" + enc / 100 + ")");
         }
      }

      if (names.isEmpty()) {
         bar(player, "§7The vault is empty. Hold an item or look at a creature to appraise its Worth.");
      } else {
         player.sendSystemMessage(Component.literal("§6Arsenal " + names.size() + "/12: " + String.join("§8, ", names)));
      }
   }

   private static String resists(LivingEntity mob) {
      String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
      if (!mobId.startsWith("unrp:") && !mobId.equals("minecraft:wither") && !mobId.equals("minecraft:ender_dragon") && !mobId.equals("minecraft:warden") && !mobId.equals("minecraft:elder_guardian") && !mob.getType().is(EntityTypes.BOSSES)) {
         if (mob instanceof TamableAnimal) {
            TamableAnimal tame = (TamableAnimal)mob;
            if (tame.getOwnerUUID() != null) {
               return "it's bound to another soul";
            }
         }

         return mob.getMaxHealth() > 20.0F && mob.getHealth() > mob.getMaxHealth() * 0.5F ? "it still resists. Weaken it below half (" + Math.round(100.0F * mob.getHealth() / mob.getMaxHealth()) + "%)" : null;
      } else {
         return "its aura resists your dominion";
      }
   }

   private static void forge(ServerPlayer player, ServerLevel level) {
      ItemStack held = player.getMainHandItem();
      if (isArmament(held)) {
         bar(player, "§7Already an armament. Recall cycles the vault; SNEAK-Forge shatters it.");
      } else if (!held.isEmpty()) {
         int worth_0 = appraiseItem(held);
         int cost_0 = 4 + worth_0;
         if (spendAura(player, cost_0)) {
            if (vaultCount(player) >= 12) {
               setScore(player, "unrp.aura", score(player, "unrp.aura") + cost_0);
               bar(player, "§cThe vault is full (12). Shatter an armament first.");
            } else {
               held.shrink(1);
               int slot_0 = vaultAdd(player, worth_0, 0);
               forgeFx(player, level, player.position().add(0.0D, 1.0D, 0.0D));
               if (player.getMainHandItem().isEmpty()) {
                  player.setItemInHand(InteractionHand.MAIN_HAND, buildArmament(player, worth_0, 0, slot_0));
               }

               bar(player, "§6Forged: §f" + tierName(worth_0) + " §7(Worth " + worth_0 + ", " + vaultCount(player) + "/12)");
            }
         }
      } else {
         LivingEntity mob = lookMob(player);
         if (mob == null) {
            bar(player, "§7Hold an item to forge it, or look at a creature with an empty hand.");
         } else {
            String hold = resists(mob);
            if (hold != null) {
               bar(player, "§c" + Character.toUpperCase(hold.charAt(0)) + hold.substring(1) + ".");
            } else {
               int worth = appraiseEntity(player, mob);
               int proc = procOf(mob);
               int cost = 6 + worth;
               if (spendAura(player, cost)) {
                  if (vaultCount(player) >= 12) {
                     setScore(player, "unrp.aura", score(player, "unrp.aura") + cost);
                     bar(player, "§cThe vault is full (12). Shatter an armament first.");
                  } else {
                     Vec3 at = mob.position().add(0.0D, (double)mob.getBbHeight() * 0.6D, 0.0D);
                     claimGate(player, level, mob, at);
                     Fx.line(level, Fx.flare(15254106), at, player.position().add(0.0D, 1.1D, 0.0D), 0.5D, 0.08D, 0.02D);
                     Fx.shell(level, Fx.glint(15254106), at, 0.5D, 14, 0.2D);
                     mob.discard();
                     int slot = vaultAdd(player, worth, proc);
                     forgeFx(player, level, at);
                     if (player.getMainHandItem().isEmpty()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, buildArmament(player, worth, proc, slot));
                     }

                     bar(player, "§6Forged: §f" + armamentName(worth, proc) + " §7(Worth " + worth + ", " + vaultCount(player) + "/12)");
                  }
               }
            }
         }
      }
   }

   private static void claimGate(ServerPlayer player, ServerLevel level, LivingEntity mob, Vec3 at) {
      ArsenalGateEntity gate = new ArsenalGateEntity(com.myriam.unrp.entity.ModEntities.ARSENAL_GATE.get(), level);
      gate.setPos(at.x, at.y, at.z);
      float r = (float)Math.max(0.45D, Math.min(1.4D, (double)mob.getBbHeight() * 0.45D + (double)mob.getBbWidth() * 0.2D));
      gate.configure(ItemStack.EMPTY, 7, 1, r, 0.0F);
      gate.aim(player.getEyePosition().subtract(at));
      level.addFreshEntity(gate);
      level.playSound((Player)null, mob.blockPosition(), CastFxEvents.sound("minecraft:block.respawn_anchor.deplete"), SoundSource.PLAYERS, 0.7F, 1.4F);
   }

   private static void forgeFx(ServerPlayer player, ServerLevel level, Vec3 at) {
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.anvil.use"), SoundSource.PLAYERS, 0.5F, 1.5F);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.chime"), SoundSource.PLAYERS, 0.9F, 0.8F);
      Fx.burst(level, Fx.flare(15254106), at, 10, 0.4D, 0.04D);
      MoveAnim.play(player, "ad_forge");
   }

   private static void recall(ServerPlayer player, ServerLevel level) {
      ItemStack held = player.getMainHandItem();
      if (!held.isEmpty() && !isArmament(held)) {
         bar(player, "§cYour hand is full. Recall needs an empty hand.");
      } else {
         List<Integer> slots = new ArrayList();

         for(int i = 1; i <= 12; ++i) {
            if (vaultGet(player, i) > 0) {
               slots.add(i);
            }
         }

         if (slots.isEmpty()) {
            bar(player, "§7The vault is empty. Forge armaments first.");
         } else {
            int current = isArmament(held) ? held.getTag().getInt("unrp_ad_slot") : -1;
            int next = slots.get(0);

            for(int i = 0; i < slots.size(); ++i) {
               if (slots.get(i) == current) {
                  next = slots.get((i + 1) % slots.size());
                  break;
               }
            }

            if (next == current && slots.size() == 1) {
               bar(player, "§7This is the only armament in your vault.");
            } else {
               int enc = vaultGet(player, next);
               player.setItemInHand(InteractionHand.MAIN_HAND, buildArmament(player, enc / 100, enc % 100, next));
               level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.enchantment_table.use"), SoundSource.PLAYERS, 0.8F, 1.5F);
               Fx.burst(level, Fx.glint(15254106), player.position().add(0.0D, 1.1D, 0.0D), 8, 0.3D, 0.04D);
               MoveAnim.play(player, "ad_recall");
               bar(player, "§6Recalled: §f" + armamentName(enc / 100, enc % 100) + " §7(" + (slots.indexOf(next) + 1) + "/" + slots.size() + ")");
            }
         }
      }
   }

   private static boolean dissolveManifested(ServerPlayer player, boolean message) {
      boolean any = false;
      Inventory inv = player.getInventory();

      for(int i = 0; i < inv.getContainerSize(); ++i) {
         if (isArmament(inv.getItem(i))) {
            inv.setItem(i, ItemStack.EMPTY);
            any = true;
         }
      }

      if (any && message) {
         bar(player, "§7Your armaments dissolve into golden motes.");
      }

      return any;
   }

   private static void fireShot(ServerPlayer player, ServerLevel level, int ct) {
      int tier = ct >= 40 ? 3 : (ct >= 16 ? 2 : 1);
      int extra = tier == 3 ? 8 : (tier == 2 ? 4 : 0);
      if (extra > 0 && score(player, "unrp.aura") < extra) {
         tier = 1;
         extra = 0;
      }

      if (extra > 0) {
         setScore(player, "unrp.aura", score(player, "unrp.aura") - extra);
      }

      float damage = tier == 3 ? 12.0F : (tier == 2 ? 8.0F : 5.0F);
      double speed = tier == 3 ? 3.0D : (tier == 2 ? 2.6D : 2.2D);
      Vec3 look = player.getLookAngle();
      Vec3 tip = fingertip(player);
      ArsenalBoltEntity bolt = new ArsenalBoltEntity(com.myriam.unrp.entity.ModEntities.ARSENAL_BOLT.get(), player, level);
      bolt.setItem(new ItemStack(Items.NETHER_STAR));
      bolt.configure(1, tier * 3, 0, damage, 3);
      bolt.tier(tier);
      bolt.setPos(tip.x, tip.y, tip.z);
      bolt.shoot(look.x, look.y, look.z, (float)speed, 0.0F);
      bolt.face(look, 0.0F);
      level.addFreshEntity(bolt);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.5F + (float)tier * 0.15F, 1.6F - (float)tier * 0.2F);
      Fx.burst(level, Fx.flare(15254106), tip, 4 + tier * 3, 0.15D, 0.03D);
      Fx.ring(level, 15254106, tip, 0.4D + (double)tier * 0.2D, 6);
      MoveAnim.release(player, "ad_aim");
      MoveAnim.play(player, "ad_bang");
      Component bang = Component.literal(player.getName().getString() + ": \"Bang.\"").withStyle(ChatFormatting.GOLD);

      for(ServerPlayer nearby : level.players()) {
         if (nearby.distanceToSqr(player) < 576.0D) {
            nearby.sendSystemMessage(bang);
         }
      }

   }

   public static Vec3 aimPoint(ServerPlayer player, ServerLevel level) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      Vec3 far = eye.add(look.scale(48.0D));
      HitResult block = level.clip(new ClipContext(eye, far, Block.COLLIDER, Fluid.NONE, player));
      Vec3 end = block.getType() == Type.MISS ? far : block.getLocation();
      EntityHitResult hit = ProjectileUtil.getEntityHitResult(level, player, eye, end, (new AABB(eye, end)).inflate(1.5D), (e) -> e instanceof LivingEntity && OriginStrike.fair(player, e));
      if (hit != null) {
         return hit.getEntity().getBoundingBox().getCenter();
      } else {
         return block.getType() == Type.MISS ? eye.add(look.scale(28.0D)) : end;
      }
   }

   public static double[][] fanSlots(int n) {
      double[][] out = new double[n][];
      if (n <= 6) {
         fanRow(out, 0, n, 2.65D, 1.3250000000000002D, 100.0D, 0);
      } else {
         int inner = n / 2;
         int at = fanRow(out, 0, inner, 2.15D, 1.05D, 100.0D * (double)(inner - 1) / (double)Math.max(1, inner), 1);
         fanRow(out, at, n - inner, 3.15D, 1.6D, 108.0D, 2);
      }

      Arrays.sort(out, Comparator.<double[]>comparingLong((s) -> -Math.round(Math.abs(s[0]) * 1000.0D)).thenComparingDouble((s) -> -s[3]).thenComparingDouble((s) -> s[0]));
      return out;
   }

   private static int fanRow(double[][] out, int at, int count, double radius, double back, double span, int ring) {
      for(int i = 0; i < count; ++i) {
         double a = count == 1 ? 0.0D : -span + 2.0D * span * (double)i / (double)(count - 1);
         out[at + i] = new double[]{a, radius, back, (double)ring};
      }

      return at + count;
   }

   public static Vec3 fanPoint(Vec3 origin, Vec3 flat, double[] slot) {
      Vec3 right = flat.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
      double a = Math.toRadians(slot[0]);
      return origin.add(right.scale(slot[1] * Math.sin(a))).add(0.0D, 1.9D + 0.85D * slot[1] * Math.cos(a), 0.0D).subtract(flat.scale(slot[2]));
   }

   public static float gateRadius(int worth) {
      return 0.36F + 0.024F * (float)worth;
   }

   public static long gateOpenDelay(int k, int n) {
      return Math.round((double)(k * 3) / (double)Math.max(1, n));
   }

   public static long volleyDelay(int k, int n) {
      return 8L + Math.round((double)k * Math.min(2.0D, 14.0D / (double)Math.max(1, n - 1)));
   }

   private static void beginFullArsenal(ServerPlayer player, ServerLevel level) {
      closeVolley(player, level);
      VOLLEY_HITS.put(player.getUUID(), new HashMap());
      List<int[]> blades = new ArrayList();

      for(int i = 1; i <= 12; ++i) {
         int enc = vaultGet(player, i);
         if (enc > 0) {
            blades.add(new int[]{enc / 100, enc % 100, i});
         }
      }

      blades.sort(Comparator.<int[]>comparingInt((b) -> b[0]).thenComparingInt((b) -> b[2]));
      long now = level.getGameTime();
      Vec3 look = player.getLookAngle();
      Vec3 flat = new Vec3(look.x, 0.0D, look.z);
      ArmamentEvents.Volley v = new ArmamentEvents.Volley();
      v.origin = player.position();
      v.flat = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
      double[][] slots = fanSlots(blades.size());

      for(int k = 0; k < blades.size(); ++k) {
         ArmamentEvents.Shot s = new ArmamentEvents.Shot();
         s.worth = ((int[])blades.get(k))[0];
         s.type = ((int[])blades.get(k))[1];
         s.slot = slots[k];
         s.openAt = now + gateOpenDelay(k, blades.size());
         s.fireAt = now + volleyDelay(k, blades.size());
         s.roll = (k % 2 == 0 ? 1.0F : -1.0F) * (70.0F + (float)(k * 37 % 40));
         v.shots.add(s);
      }

      VOLLEYS.put(player.getUUID(), v);
      MoveAnim.hold(player, "ad_command", 3);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.ender_chest.open"), SoundSource.PLAYERS, 1.0F, 0.7F);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.power_select"), SoundSource.PLAYERS, 1.0F, 0.6F);
      bar(player, "§6Full Arsenal: §f" + blades.size() + " armaments answer the call.");
   }

   private static void tickVolley(ServerPlayer player, ServerLevel level) {
      ArmamentEvents.Volley v = (ArmamentEvents.Volley)VOLLEYS.get(player.getUUID());
      if (v != null) {
         if (active(player) && player.isAlive()) {
            long now = level.getGameTime();
            MoveAnim.hold(player, "ad_command", 3);
            Vec3 aim = aimPoint(player, level);
            boolean left = false;

            for(int k = 0; k < v.shots.size(); ++k) {
               ArmamentEvents.Shot s = (ArmamentEvents.Shot)v.shots.get(k);
               if (!s.done) {
                  left = true;
                  if (s.gate < 0 && now >= s.openAt) {
                     openGate(level, v, s, k, aim, now);
                  }

                  Entity e = s.gate >= 0 ? level.getEntity(s.gate) : null;
                  if (e instanceof ArsenalGateEntity) {
                     ArsenalGateEntity gate = (ArsenalGateEntity)e;
                     Vec3 dir = aim.subtract(gate.position());
                     gate.aim(dir);
                     if (now >= s.fireAt) {
                        launch(player, level, gate, s, dir);
                        s.done = true;
                     }
                  } else if (s.gate >= 0) {
                     s.done = true;
                  }
               }
            }

            if (!left) {
               VOLLEYS.remove(player.getUUID());
               MoveAnim.release(player, "ad_command");
            }

         } else {
            closeVolley(player, level);
         }
      }
   }

   private static void openGate(ServerLevel level, ArmamentEvents.Volley v, ArmamentEvents.Shot s, int k, Vec3 aim, long now) {
      Vec3 at = openAir(level, v.origin.add(0.0D, 1.2D, 0.0D), fanPoint(v.origin, v.flat, s.slot));
      ArsenalGateEntity gate = new ArsenalGateEntity(com.myriam.unrp.entity.ModEntities.ARSENAL_GATE.get(), level);
      gate.setPos(at.x, at.y, at.z);
      gate.configure(display(s.worth, s.type), (int)Math.max(1L, s.fireAt - now), 0, gateRadius(s.worth), s.roll);
      gate.aim(aim.subtract(at));
      level.addFreshEntity(gate);
      s.gate = gate.getId();
      level.playSound((Player)null, BlockPos.containing(at), CastFxEvents.sound("minecraft:block.amethyst_block.chime"), SoundSource.PLAYERS, 0.8F, 0.7F + 0.06F * (float)k);
   }

   public static Vec3 openAir(ServerLevel level, Vec3 from, Vec3 at) {
      HitResult hit = level.clip(new ClipContext(from, at, Block.COLLIDER, Fluid.NONE, (Entity)null));
      if (hit.getType() == Type.MISS) {
         return at;
      } else {
         Vec3 d = at.subtract(from);
         double len = d.length();
         double keep = Math.max(0.0D, hit.getLocation().distanceTo(from) - 0.45D);
         return len < 1.0E-6D ? from : from.add(d.scale(keep / len));
      }
   }

   private static void launch(ServerPlayer player, ServerLevel level, ArsenalGateEntity gate, ArmamentEvents.Shot s, Vec3 dir) {
      if (!(dir.lengthSqr() < 1.0E-6D)) {
         Vec3 n = dir.normalize();
         Vec3 from = gate.position().add(n.scale(0.44724074072778225D));
         ArsenalBoltEntity bolt = new ArsenalBoltEntity(com.myriam.unrp.entity.ModEntities.ARSENAL_BOLT.get(), player, level);
         bolt.setItem(display(s.worth, s.type));
         bolt.configure(0, s.worth, s.type, 3.0F + (float)s.worth * 0.1F, 0);
         bolt.setPos(from.x, from.y, from.z);
         bolt.shoot(n.x, n.y, n.z, 2.3F, 0.25F);
         bolt.face(bolt.getDeltaMovement(), s.roll);
         level.addFreshEntity(bolt);
         level.playSound((Player)null, BlockPos.containing(from), CastFxEvents.sound("minecraft:item.trident.throw"), SoundSource.PLAYERS, 0.8F, 0.9F + level.random.nextFloat() * 0.4F);
      }
   }

   private static void closeVolley(ServerPlayer player, ServerLevel level) {
      ArmamentEvents.Volley v = (ArmamentEvents.Volley)VOLLEYS.remove(player.getUUID());
      if (v != null) {
         for(ArmamentEvents.Shot s : v.shots) {
            Entity e = s.gate >= 0 ? level.getEntity(s.gate) : null;
            if (e instanceof ArsenalGateEntity && !s.done) {
               e.discard();
            }
         }

         MoveAnim.stop(player, "ad_command");
      }
   }

   public static boolean volleyHitAllowed(ServerPlayer shooter, LivingEntity victim) {
      Map<UUID, Integer> hits = (Map)VOLLEY_HITS.get(shooter.getUUID());
      if (hits == null) {
         return true;
      } else {
         int n = hits.getOrDefault(victim.getUUID(), 0);
         if (n >= 3) {
            return false;
         } else {
            hits.put(victim.getUUID(), n + 1);
            return true;
         }
      }
   }

   public static void applyProc(ServerLevel level, LivingEntity victim, LivingEntity attacker, int type) {
      long now = level.getGameTime();
      if (type == 1 || type == 4) {
         Long cd = (Long)PROC_CD.get(victim.getUUID());
         if (cd != null && now - cd < 40L) {
            return;
         }

         PROC_CD.put(victim.getUUID(), now);
      }

      switch (type) {
         case 1:
            level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.6F, 1.2F);
            Fx.shell(level, Fx.wave(16757594), victim.position().add(0.0D, 0.9D, 0.0D), 0.5D, 10, 0.4D);

            for(LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(2.0D))) {
               if (near != attacker && (attacker == null || OriginStrike.fair(attacker, near))) {
                  if (near != victim) {
                     OriginStrike.hit(near, level.damageSources().indirectMagic(attacker != null ? attacker : victim, attacker != null ? attacker : victim), 2.5F);
                  }

                  near.knockback(0.6D, victim.getX() - near.getX(), victim.getZ() - near.getZ());
               }
            }
            break;
         case 2:
            victim.setSecondsOnFire(4);
            break;
         case 3:
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0, true, true));
            break;
         case 4:
            double nx = victim.getX() + (level.random.nextDouble() - 0.5D) * 10.0D;
            double nz = victim.getZ() + (level.random.nextDouble() - 0.5D) * 10.0D;
            if (victim.randomTeleport(nx, victim.getY(), nz, true)) {
               level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:entity.enderman.teleport"), SoundSource.PLAYERS, 0.8F, 1.0F);
               Fx.burst(level, Fx.ember(10177256), victim.position().add(0.0D, 1.0D, 0.0D), 10, 0.4D, 0.05D);
            }
            break;
         case 5:
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, true, true));
            victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 1, true, true));
      }

   }

   private static LivingEntity lookMob(ServerPlayer player) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      LivingEntity best = null;
      double bestDot = 0.95D;

      for(LivingEntity e : player.serverLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6.5D))) {
         if (e != player && !(e instanceof Player) && !e.isDeadOrDying()) {
            Vec3 to = e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D).subtract(eye);
            if (!(to.length() > 6.5D)) {
               double dot = to.normalize().dot(look);
               if (dot > bestDot && player.hasLineOfSight(e)) {
                  bestDot = dot;
                  best = e;
               }
            }
         }
      }

      return best;
   }

   @SubscribeEvent
   public static void onChat(ServerChatEvent event) {
      ServerPlayer player = event.getPlayer();
      String msg = event.getMessage().getString().trim();
      if (msg.equalsIgnoreCase("bang")) {
         if (SHOT_CHARGE.containsKey(player.getUUID())) {
            BANG_PENDING.put(player.getUUID(), true);
            event.setCanceled(true);
         }

      }
   }

   @SubscribeEvent
   public static void onHurt(LivingHurtEvent event) {
      Entity level_0 = event.getSource().getEntity();
      if (level_0 instanceof LivingEntity attacker) {
         if (event.getSource().getDirectEntity() == attacker) {
            Level held_0 = attacker.level();
            if (held_0 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)held_0;
               ItemStack held = attacker.getMainHandItem();
               if (isArmament(held)) {
                  CompoundTag tag = held.getTag();
                  int type = tag.getInt("unrp_ad_type");
                  if (type != 0) {
                     applyProc(level, event.getEntity(), attacker, type);
                  }

                  Fx.burst(level, Fx.glint(15254106), event.getEntity().position().add(0.0D, (double)event.getEntity().getBbHeight() * 0.6D, 0.0D), 4, 0.25D, 0.05D);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         if (event.getServer().getTickCount() % 20 == 0) {
            long now = event.getServer().overworld().getGameTime();
            PROC_CD.values().removeIf((t) -> now - t > 200L);

            for(ServerPlayer online : event.getServer().getPlayerList().getPlayers()) {
               boolean holderActive = active(online) && noncanonOn(online);
               Inventory inv = online.getInventory();

               for(int i = 0; i < inv.getContainerSize(); ++i) {
                  ItemStack stack = inv.getItem(i);
                  if (isArmament(stack)) {
                     CompoundTag tag = stack.getTag();
                     if (!tag.contains("unrp_ad_born") || now - tag.getLong("unrp_ad_born") >= 60L) {
                        boolean owned = tag.hasUUID("unrp_ad_owner") && tag.getUUID("unrp_ad_owner").equals(online.getUUID());
                        int slot = tag.getInt("unrp_ad_slot");
                        int enc = slot >= 1 && slot <= 12 ? vaultGet(online, slot) : 0;
                        boolean backed = enc == tag.getInt("unrp_ad_worth") * 100 + tag.getInt("unrp_ad_type");
                        if (!holderActive || !owned || !backed) {
                           inv.setItem(i, ItemStack.EMPTY);
                           bar(online, !owned ? "§7An armament that was never yours dissolves." : (!holderActive ? "§7Your armament dissolves. The dominion isn't active." : "§7An armament with no vault behind it dissolves."));
                        }
                     }
                  }
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onToss(ItemTossEvent event) {
      if (isArmament(event.getEntity().getItem())) {
         event.getEntity().discard();
         Player var2 = event.getPlayer();
         if (var2 instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)var2;
            bar(sp, "§7The armament dissolves the moment it leaves your grip.");
         }
      }

   }

   @SubscribeEvent
   public static void onItemJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide()) {
         Entity var2 = event.getEntity();
         if (var2 instanceof ItemEntity) {
            ItemEntity item = (ItemEntity)var2;
            if (isArmament(item.getItem())) {
               event.setCanceled(true);
            }
         }

      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         dissolveManifested(player, false);
         SHOT_CHARGE.remove(player.getUUID());
         BANG_PENDING.remove(player.getUUID());
         closeVolley(player, player.serverLevel());
         VOLLEY_HITS.remove(player.getUUID());
      }
   }

   private static boolean spendAura(ServerPlayer player, int cost) {
      int cur = score(player, "unrp.aura");
      if (cur < cost) {
         bar(player, "§cNot enough aura (" + cost + " needed).");
         return false;
      } else {
         setScore(player, "unrp.aura", cur - cost);
         return true;
      }
   }

   private static void bar(ServerPlayer player, String msg) {
      player.displayClientMessage(Component.literal(msg), true);
   }

   private static int score(ServerPlayer p, String obj) {
      try {
         ServerScoreboard sb = p.server.getScoreboard();
         Objective o = sb.getObjective(obj);
         return o == null ? 0 : sb.getOrCreatePlayerScore(p.getScoreboardName(), o).getScore();
      } catch (Exception var4) {
         return 0;
      }
   }

   private static void setScore(ServerPlayer p, String obj, int val) {
      try {
         ServerScoreboard sb = p.server.getScoreboard();
         Objective o = sb.getObjective(obj);
         if (o == null) {
            o = sb.addObjective(obj, ObjectiveCriteria.DUMMY, Component.literal(obj), RenderType.INTEGER);
         }

         sb.getOrCreatePlayerScore(p.getScoreboardName(), o).setScore(val);
      } catch (Exception var5) {
      }

   }

   static final class Shot {
      int worth;
      int type;
      double[] slot;
      long openAt;
      long fireAt;
      float roll;
      int gate = -1;
      boolean done;
   }

   static final class Volley {
      final List<ArmamentEvents.Shot> shots = new ArrayList();
      Vec3 origin;
      Vec3 flat;
   }
}
