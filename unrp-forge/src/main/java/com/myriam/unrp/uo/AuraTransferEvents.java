package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;
import com.myriam.unrp.anim.MoveAnim;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.ClipContext;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.threetag.palladium.power.PowerManager;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class AuraTransferEvents {
   public static final int ITEM = 1;
   public static final int PERSON = 2;
   public static final String COPIED_PREFIX = "unrp.at.";
   public static final String IMBUED_PREFIX = "unrp.ati.";
   public static final String NBT_TRAIT = "unrp_at_trait";
   public static final String NBT_ID = "unrp_at_id";
   public static final String NBT_BORN = "unrp_at_born";
   public static final double WEAVE_REACH = 8.5D;
   public static final int TEAL = 5692644;
   public static final int IMBUE_COST = 10;
   public static final Map<String, Integer> TRAIT_KIND = new LinkedHashMap();
   public static final Map<String, String> TRAIT_LABEL = new HashMap();
   public static final Map<String, Integer> TRAIT_COLOR = new HashMap();
   private static final UUID MOD_ARMOR = UUID.fromString("7c1f4ab2-9d43-4f5e-8b21-3aa0c2e6d901");
   private static final UUID MOD_KB = UUID.fromString("7c1f4ab2-9d43-4f5e-8b21-3aa0c2e6d902");
   private static final UUID MOD_ATKSPD = UUID.fromString("7c1f4ab2-9d43-4f5e-8b21-3aa0c2e6d903");
   private static final UUID MOD_SHRED = UUID.fromString("7c1f4ab2-9d43-4f5e-8b21-3aa0c2e6d904");
   private static final UUID MOD_TOUGH = UUID.fromString("7c1f4ab2-9d43-4f5e-8b21-3aa0c2e6d905");
   private static final Map<UUID, List<AuraTransferEvents.Imbue>> IMBUES;
   private static final Map<UUID, Long> SHRED_UNTIL;
   private static boolean reentrant;

   private static void trait(String short_, String label, int kind, int color) {
      TRAIT_KIND.put(short_, kind);
      TRAIT_LABEL.put(short_, label);
      TRAIT_COLOR.put(short_, color);
   }

   public static int traitColor(String short_) {
      return TRAIT_COLOR.getOrDefault(short_, 5692644);
   }

   private static boolean noncanonOn(ServerPlayer player) {
      return ConfigBridgeEvents.flag(player.server, "noncanon", false) || OriginsConfig.isModDev(player.getUUID());
   }

   private static boolean hasPower(ServerPlayer player) {
      return player.getTags().contains("unrp.has.aura_transference");
   }

   private static boolean active(ServerPlayer player) {
      return player.getTags().contains("unrp.at_on");
   }

   public static int slotsFor(ServerPlayer player) {
      int lvl = score(player, "power_level");
      if (lvl >= 80) {
         return 5;
      } else if (lvl >= 50) {
         return 4;
      } else {
         return lvl >= 30 ? 3 : 2;
      }
   }

   private static String resolveTargetPower(LivingEntity target) {
      if (target instanceof Player p) {
         for(String tag_0 : p.getTags()) {
            if (tag_0.startsWith("unrp.has.")) {
               return CastFxEvents.baseOf(tag_0.substring("unrp.has.".length()));
            }
         }

         return null;
      } else {
         for(String tag : target.getTags()) {
            if (tag.startsWith("unrp.power.")) {
               return CastFxEvents.baseOf(tag.substring("unrp.power.".length()));
            }
         }

         for(String tag : target.getTags()) { // pouvoirs Palladium du joueur : nos tags « unrp.has.* »
            if (tag.startsWith("unrp.has.")) {
               return CastFxEvents.baseOf(tag.substring("unrp.has.".length()));
            }
         }

         return null;
      }
   }

   @SubscribeEvent
   public static void onAttack(AttackEntityEvent event) {
      Player target_0 = event.getEntity();
      if (target_0 instanceof ServerPlayer player) {
         Entity short__0 = event.getTarget();
         if (short__0 instanceof LivingEntity target) {
            if (active(player)) {
               if (noncanonOn(player)) {
                  String short_ = resolveTargetPower(target);
                  if (short_ != null) {
                     if (SgRules.outside(target)) {
                        bar(player, "§8There's nothing of theirs to take.");
                     } else if (!player.getMainHandItem().isEmpty()) {
                        bar(player, "§7Strike with an §fEMPTY HAND§7 to siphon a trait.");
                     } else if (short_.equals("aura_transference")) {
                        bar(player, "§7You can't copy your own power.");
                     } else if (!short_.equals("aura_manipulation") && !short_.equals("time_manipulation")) {
                        if (OriginsConfig.isOriginPower(short_)) {
                           bar(player, "§4The trait slips through your fingers like sand. That aura is off the scale.");
                        } else if (!TRAIT_KIND.containsKey(short_)) {
                           bar(player, "§7You feel no stable trait in that aura.");
                        } else {
                           String tag = "unrp.at." + short_;
                           if (player.getTags().contains(tag)) {
                              bar(player, "§7Trait already held: §b" + (String)TRAIT_LABEL.get(short_));
                           } else {
                              player.addTag(tag);
                              ServerLevel level = player.serverLevel();
                              thread(level, target, player, 0, traitColor(short_));
                              Fx.burst(level, Fx.flare(traitColor(short_)), target.position().add(0.0D, (double)target.getBbHeight() * 0.6D, 0.0D), 6, 0.3D, 0.03D);
                              level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.chime"), SoundSource.PLAYERS, 0.9F, 1.3F);
                              level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.allay.item_taken"), SoundSource.PLAYERS, 0.6F, 0.8F);
                              MoveAnim.play(player, "at_siphon");
                              player.displayClientMessage(Component.literal("§bTrait copied: ").append(trait(short_)).append(Component.literal(" §7[" + kindLabel(short_) + "] (" + countCopied(player) + " held)")), true);
                           }
                        }
                     } else {
                        bar(player, "§cThat aura is too vast to copy.");
                     }
                  }
               }
            }
         }
      }
   }

   private static String kindLabel(String short_) {
      int k = TRAIT_KIND.getOrDefault(short_, 0);
      if (k == 3) {
         return "Weapon+Person";
      } else {
         return k == 1 ? "Weapon" : "Person";
      }
   }

   private static int countCopied(ServerPlayer player) {
      int n = 0;

      for(String tag : player.getTags()) {
         if (tag.startsWith("unrp.at.")) {
            ++n;
         }
      }

      return n;
   }

   private static List<String> copiedList(ServerPlayer player) {
      List<String> out = new ArrayList();

      for(String key : TRAIT_KIND.keySet()) {
         if (player.getTags().contains("unrp.at." + key)) {
            out.add(key);
         }
      }

      return out;
   }

   private static String selected(ServerPlayer player) {
      List<String> owned = copiedList(player);
      if (owned.isEmpty()) {
         return null;
      } else {
         int sel = score(player, "unrp.at_sel");
         return (String)owned.get(Math.floorMod(sel, owned.size()));
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player tags_0 = event.player;
         if (tags_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)tags_0;
            Set<String> tags = player.getTags();
            if (tags.remove("unrp.at_off_trigger") && !active(player)) {
               clearAll(player, true);
            }

            boolean enabled = noncanonOn(player);
            if (tags.remove("unrp.at_attune_trigger")) {
               if (!enabled) {
                  bar(player, "§cNon-canon powers are disabled on this server.");
               } else if (player.isShiftKeyDown()) {
                  removeMostRecent(player);
               } else {
                  cycleSelection(player);
               }
            }

            if (tags.remove("unrp.at_imbue_item_trigger")) {
               if (!enabled) {
                  bar(player, "§cNon-canon powers are disabled on this server.");
               } else {
                  imbueItem(player);
               }
            }

            if (tags.remove("unrp.at_imbue_player_trigger")) {
               if (!enabled) {
                  bar(player, "§cNon-canon powers are disabled on this server.");
               } else {
                  imbuePlayer(player);
               }
            }

            if (player.tickCount % 20 == 0) {
               if (!enabled && hasPower(player)) {
                  if (!((List)IMBUES.getOrDefault(player.getUUID(), List.of())).isEmpty()) {
                     clearAll(player, false);
                  }

                  if (player.tickCount % 200 == 0 && active(player)) {
                     bar(player, "§cNon-canon powers are disabled on this server.");
                  }

               } else {
                  List<AuraTransferEvents.Imbue> list = (List)IMBUES.get(player.getUUID());
                  if (list != null && !list.isEmpty()) {
                     if (!active(player)) {
                        clearAll(player, true);
                     } else {
                        Iterator<AuraTransferEvents.Imbue> it = list.iterator();

                        while(it.hasNext()) {
                           AuraTransferEvents.Imbue im = (AuraTransferEvents.Imbue)it.next();
                           if (!im.onItem) {
                              ServerPlayer target = player.server.getPlayerList().getPlayer(im.holder);
                              if (target == null) {
                                 it.remove();
                                 bar(player, "§7A woven trait faded with its bearer.");
                              } else {
                                 applyPersonTrait(target, im.trait);
                                 keepTether(player, target, im);
                              }
                           }
                        }

                        syncDrainTags(player, list.size());
                        if (list.isEmpty()) {
                           IMBUES.remove(player.getUUID());
                        }

                     }
                  } else {
                     syncDrainTags(player, 0);
                  }
               }
            }
         }
      }
   }

   private static void syncDrainTags(ServerPlayer player, int count) {
      for(int k = 1; k <= 5; ++k) {
         String tag = "unrp.at_drain_" + k;
         if (k <= count) {
            player.addTag(tag);
         } else {
            player.removeTag(tag);
         }
      }

   }

   private static void cycleSelection(ServerPlayer player) {
      List<String> owned = copiedList(player);
      if (owned.isEmpty()) {
         bar(player, "§7No traits copied yet. Strike a powered being with an empty hand.");
      } else {
         int sel = Math.floorMod(score(player, "unrp.at_sel") + 1, owned.size());
         setScore(player, "unrp.at_sel", sel);
         String short_ = (String)owned.get(sel);
         player.serverLevel().playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:ui.button.click"), SoundSource.PLAYERS, 0.4F, 1.6F);
         player.displayClientMessage(Component.literal("§bAttuned: ").append(trait(short_)).append(Component.literal(" §7[" + kindLabel(short_) + "] (" + (sel + 1) + "/" + owned.size() + ")")), true);
      }
   }

   private static void removeMostRecent(ServerPlayer player) {
      List<AuraTransferEvents.Imbue> list = (List)IMBUES.get(player.getUUID());
      if (list != null && !list.isEmpty()) {
         AuraTransferEvents.Imbue im = (AuraTransferEvents.Imbue)list.remove(list.size() - 1);
         dissolve(player, im);
         MoveAnim.play(player, "at_unravel");
         syncDrainTags(player, list.size());
         if (list.isEmpty()) {
            IMBUES.remove(player.getUUID());
         }

         bar(player, "§dDissolved: §f" + (String)TRAIT_LABEL.get(im.trait) + " §7(" + list.size() + "/" + slotsFor(player) + " woven)");
      } else {
         bar(player, "§7No woven traits to dissolve.");
      }
   }

   private static void dissolve(ServerPlayer caster, AuraTransferEvents.Imbue im) {
      if (im.onItem) {
         stripItemEverywhere(caster, im.holder);
      } else {
         ServerPlayer target = caster.server.getPlayerList().getPlayer(im.holder);
         if (target != null) {
            target.removeTag("unrp.ati." + im.trait);
            removePersonTrait(target, im.trait);
            Entity old = im.tether >= 0 ? target.level().getEntity(im.tether) : null;
            if (old instanceof AuraThreadEntity) {
               old.discard();
            }

            thread(target.serverLevel(), target, target, 3, traitColor(im.trait));
         }

         im.tether = -1;
      }

      caster.serverLevel().playSound((Player)null, caster.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.deactivate"), SoundSource.PLAYERS, 0.5F, 1.6F);
   }

   private static void clearAll(ServerPlayer player, boolean message) {
      List<AuraTransferEvents.Imbue> list = (List)IMBUES.remove(player.getUUID());
      if (list != null) {
         for(AuraTransferEvents.Imbue im : list) {
            dissolve(player, im);
         }
      }

      syncDrainTags(player, 0);
      if (message && list != null && !list.isEmpty()) {
         bar(player, "§dEvery woven trait dissolves.");
      }

   }

   private static void imbueItem(ServerPlayer player) {
      String short_ = selected(player);
      if (short_ == null) {
         bar(player, "§7No traits copied yet. Strike a powered being with an empty hand.");
      } else if ((TRAIT_KIND.get(short_) & 1) == 0) {
         bar(player, "§c" + (String)TRAIT_LABEL.get(short_) + " can't bind to a weapon. Attune another trait.");
      } else {
         ItemStack held = player.getMainHandItem();
         boolean ranged = short_.equals("energy_discharge") || short_.equals("arcane_shot");
         if (held.isEmpty() || ranged && !(held.getItem() instanceof ProjectileWeaponItem) || !ranged && !held.getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE)) {
            bar(player, ranged ? "§cArcane traits need a bow or crossbow in hand." : "§cHold a melee weapon to imbue it.");
         } else {
            CompoundTag nbt = held.getTag();
            if (nbt != null && nbt.contains("unrp_at_trait")) {
               bar(player, "§cThis weapon already carries a trait. SNEAK-press Attune to dissolve your last imbuement.");
            } else {
               List<AuraTransferEvents.Imbue> list = (List)IMBUES.computeIfAbsent(player.getUUID(), (k) -> new ArrayList());
               int max = slotsFor(player);
               if (list.size() >= max) {
                  bar(player, "§cAll " + max + " trait slots are woven. Level up, or dissolve one first.");
               } else if (score(player, "unrp.aura") < 10) {
                  bar(player, "§cNot enough aura.");
               } else {
                  setScore(player, "unrp.aura", score(player, "unrp.aura") - 10);
                  UUID id = UUID.randomUUID();
                  CompoundTag tag = held.getOrCreateTag();
                  tag.putString("unrp_at_trait", short_);
                  tag.putUUID("unrp_at_id", id);
                  tag.putLong("unrp_at_born", player.level().getGameTime());
                  list.add(new AuraTransferEvents.Imbue(short_, true, id));
                  syncDrainTags(player, list.size());
                  imbueFx(player, player.position().add(0.0D, 1.0D, 0.0D), short_);
                  MoveAnim.play(player, "at_weave");
                  bar(player, "§bWoven into weapon: §f" + (String)TRAIT_LABEL.get(short_) + " §7(" + list.size() + "/" + max + ")");
               }
            }
         }
      }
   }

   private static void imbuePlayer(ServerPlayer player) {
      String short_ = selected(player);
      if (short_ == null) {
         bar(player, "§7No traits copied yet. Strike a powered being with an empty hand.");
      } else if ((TRAIT_KIND.get(short_) & 2) == 0) {
         bar(player, "§c" + (String)TRAIT_LABEL.get(short_) + " can't bind to a person. Attune another trait.");
      } else {
         ServerPlayer target = player.isShiftKeyDown() ? player : lookTarget(player);
         if (target == null) {
            bar(player, "§cLook at someone within 8 blocks, or SNEAK-press to imbue yourself.");
         } else if (target != player && SgRules.outside(target)) {
            bar(player, "§8The trait can't take hold in them.");
         } else if (target.getTags().contains("unrp.ati." + short_)) {
            bar(player, "§cThey already bear " + (String)TRAIT_LABEL.get(short_) + ".");
         } else {
            List<AuraTransferEvents.Imbue> list = (List)IMBUES.computeIfAbsent(player.getUUID(), (k) -> new ArrayList());
            int max = slotsFor(player);
            if (list.size() >= max) {
               bar(player, "§cAll " + max + " trait slots are woven. Level up, or dissolve one first.");
            } else if (score(player, "unrp.aura") < 10) {
               bar(player, "§cNot enough aura.");
            } else {
               setScore(player, "unrp.aura", score(player, "unrp.aura") - 10);
               target.addTag("unrp.ati." + short_);
               AuraTransferEvents.Imbue woven = new AuraTransferEvents.Imbue(short_, false, target.getUUID());
               list.add(woven);
               syncDrainTags(player, list.size());
               applyPersonTrait(target, short_);
               ServerLevel level = player.serverLevel();
               thread(level, player, target, 1, traitColor(short_));
               keepTether(player, target, woven);
               if (target != player) {
                  target.displayClientMessage(Component.literal("§b" + player.getName().getString() + " weaves a trait into you: ").append(trait(short_)), true);
               }

               imbueFx(player, target.position().add(0.0D, 1.0D, 0.0D), short_);
               MoveAnim.play(player, target == player ? "at_self" : "at_bestow");
               bar(player, "§bWoven into " + (target == player ? "yourself" : target.getName().getString()) + ": §f" + (String)TRAIT_LABEL.get(short_) + " §7(" + list.size() + "/" + max + ")");
            }
         }
      }
   }

   private static void imbueFx(ServerPlayer player, Vec3 at, String short_) {
      ServerLevel level = player.serverLevel();
      Fx.burst(level, Fx.flare(traitColor(short_)), at, 8, 0.45D, 0.03D);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.enchantment_table.use"), SoundSource.PLAYERS, 0.7F, 1.4F);
   }

   private static Component trait(String short_) {
      return Component.literal((String)TRAIT_LABEL.getOrDefault(short_, short_)).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(traitColor(short_))));
   }

   private static void thread(ServerLevel level, Entity source, Entity sink, int kind, int color) {
      AuraThreadEntity t = new AuraThreadEntity(com.myriam.unrp.entity.ModEntities.AURA_THREAD.get(), level);
      t.setPos(sink.getX(), sink.getY() + (double)sink.getBbHeight() * 0.5D, sink.getZ());
      t.configure(source.getId(), sink.getId(), kind, color);
      level.addFreshEntity(t);
   }

   private static void keepTether(ServerPlayer caster, ServerPlayer target, AuraTransferEvents.Imbue im) {
      Entity e = im.tether >= 0 ? target.level().getEntity(im.tether) : null;
      if (e instanceof AuraThreadEntity t_0) {
         if (t_0.isAlive() && t_0.sink() == target.getId() && t_0.source() == caster.getId()) {
            t_0.keep();
            return;
         }
      }

      if (e != null) {
         e.discard();
      }

      ServerLevel level = target.serverLevel();
      AuraThreadEntity t = new AuraThreadEntity(com.myriam.unrp.entity.ModEntities.AURA_THREAD.get(), level);
      t.setPos(target.getX(), target.getY() + (double)target.getBbHeight() * 0.5D, target.getZ());
      t.configure(caster.getId(), target.getId(), 2, traitColor(im.trait));
      t.keep();
      level.addFreshEntity(t);
      im.tether = t.getId();
   }

   private static ServerPlayer lookTarget(ServerPlayer player) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      Vec3 far = eye.add(look.scale(8.5D));
      HitResult block = player.level().clip(new ClipContext(eye, far, Block.COLLIDER, Fluid.NONE, player));
      Vec3 end = block.getType() == Type.MISS ? far : block.getLocation();
      EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, eye, end, (new AABB(eye, end)).inflate(1.0D), (e) -> {
         if (e instanceof ServerPlayer sp_0) {
            if (sp_0 != player && !sp_0.isSpectator()) {
               return true;
            }
         }

         return false;
      });
      if (hit != null) {
         Entity bestDot_0 = hit.getEntity();
         if (bestDot_0 instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)bestDot_0;
            return sp;
         }
      }

      ServerPlayer best = null;
      double bestDot = 0.96D;

      for(ServerPlayer other : player.serverLevel().players()) {
         if (other != player && !other.isSpectator()) {
            Vec3 to = other.position().add(0.0D, (double)other.getBbHeight() * 0.5D, 0.0D).subtract(eye);
            double dist = to.length();
            if (!(dist > 8.5D)) {
               double dot = to.normalize().dot(look);
               if (dot > bestDot && player.hasLineOfSight(other)) {
                  bestDot = dot;
                  best = other;
               }
            }
         }
      }

      return best;
   }

   private static void applyPersonTrait(ServerPlayer target, String short_) {
      switch (short_) {
         case "speed":
            effect(target, MobEffects.MOVEMENT_SPEED, 0);
            break;
         case "regeneration":
            effect(target, MobEffects.REGENERATION, 0);
            break;
         case "healing":
            if (!target.hasEffect(MobEffects.ABSORPTION)) {
               target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1, true, false));
            }
            break;
         case "illumination":
            MobEffectInstance nv = target.getEffect(MobEffects.NIGHT_VISION);
            if (nv == null || nv.getDuration() < 210) {
               target.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 410, 0, true, false));
            }
            break;
         case "whirlwind":
            effect(target, MobEffects.SLOW_FALLING, 0);
            break;
         case "heat_palm":
            effect(target, MobEffects.FIRE_RESISTANCE, 0);
            break;
         case "invisibility":
            if (target.isShiftKeyDown()) {
               effect(target, MobEffects.INVISIBILITY, 0);
            } else {
               dropOwn(target, MobEffects.INVISIBILITY);
            }
            break;
         case "strong_kick":
            effect(target, MobEffects.JUMP, 1);
            break;
         case "azure_storm":
            effect(target, MobEffects.DOLPHINS_GRACE, 0);
            break;
         case "phase_shift":
            target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            break;
         case "rock_arm":
            modifier(target, Attributes.ARMOR_TOUGHNESS, MOD_TOUGH, "unrp_at_tough", 2.0D, Operation.ADDITION);
            break;
         case "stone_skin":
            modifier(target, Attributes.KNOCKBACK_RESISTANCE, MOD_KB, "unrp_at_kb", 0.6D, Operation.ADDITION);
            break;
         case "armor_suit":
            modifier(target, Attributes.ARMOR, MOD_ARMOR, "unrp_at_armor", 4.0D, Operation.ADDITION);
            break;
         case "quick_strike":
            modifier(target, Attributes.ATTACK_SPEED, MOD_ATKSPD, "unrp_at_atkspd", 0.3D, Operation.MULTIPLY_TOTAL);
      }

   }

   private static void removePersonTrait(ServerPlayer target, String short_) {
      switch (short_) {
         case "speed":
            dropOwn(target, MobEffects.MOVEMENT_SPEED);
            break;
         case "regeneration":
            dropOwn(target, MobEffects.REGENERATION);
            break;
         case "healing":
            dropOwn(target, MobEffects.ABSORPTION);
            break;
         case "illumination":
            dropOwn(target, MobEffects.NIGHT_VISION);
            break;
         case "whirlwind":
            dropOwn(target, MobEffects.SLOW_FALLING);
            break;
         case "heat_palm":
            dropOwn(target, MobEffects.FIRE_RESISTANCE);
            break;
         case "invisibility":
            dropOwn(target, MobEffects.INVISIBILITY);
            break;
         case "strong_kick":
            dropOwn(target, MobEffects.JUMP);
            break;
         case "azure_storm":
            dropOwn(target, MobEffects.DOLPHINS_GRACE);
            break;
         case "rock_arm":
            removeModifier(target, Attributes.ARMOR_TOUGHNESS, MOD_TOUGH);
            break;
         case "stone_skin":
            removeModifier(target, Attributes.KNOCKBACK_RESISTANCE, MOD_KB);
            break;
         case "armor_suit":
            removeModifier(target, Attributes.ARMOR, MOD_ARMOR);
            break;
         case "quick_strike":
            removeModifier(target, Attributes.ATTACK_SPEED, MOD_ATKSPD);
      }

   }

   public static boolean own(MobEffectInstance cur) {
      return cur != null && cur.isAmbient() && !cur.isVisible() && cur.getDuration() <= 410;
   }

   private static void dropOwn(ServerPlayer target, MobEffect mob) {
      if (own(target.getEffect(mob))) {
         target.removeEffect(mob);
      }

   }

   private static void effect(ServerPlayer target, MobEffect mob, int amp) {
      MobEffectInstance cur = target.getEffect(mob);
      if (cur == null || cur.getDuration() < 30) {
         target.addEffect(new MobEffectInstance(mob, 60, amp, true, false));
      }

   }

   private static void modifier(ServerPlayer target, Attribute attr, UUID id, String name, double amount, AttributeModifier.Operation op) {
      AttributeInstance inst = target.getAttribute(attr);
      if (inst != null && inst.getModifier(id) == null) {
         inst.addTransientModifier(new AttributeModifier(id, name, amount, op));
      }

   }

   private static void removeModifier(ServerPlayer target, Attribute attr, UUID id) {
      AttributeInstance inst = target.getAttribute(attr);
      if (inst != null && inst.getModifier(id) != null) {
         inst.removeModifier(id);
      }

   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         if (event.getServer().getTickCount() % 20 == 0) {
            long now = event.getServer().overworld().getGameTime();

            for(ServerPlayer online : event.getServer().getPlayerList().getPlayers()) {
               for(String tag : new ArrayList<>(online.getTags())) {
                  if (tag.startsWith("unrp.ati.")) {
                     String short_ = tag.substring("unrp.ati.".length());
                     if (!backed(online.getUUID(), short_)) {
                        online.removeTag(tag);
                        removePersonTrait(online, short_);
                     }
                  }
               }

               sweepInventory(online);
               Long shredEnd = (Long)SHRED_UNTIL.get(online.getUUID());
               if (shredEnd != null && shredEnd <= now) {
                  SHRED_UNTIL.remove(online.getUUID());
                  removeModifier(online, Attributes.ARMOR, MOD_SHRED);
               }
            }

         }
      }
   }

   private static boolean backed(UUID holder, String trait) {
      for(List<AuraTransferEvents.Imbue> list : IMBUES.values()) {
         for(AuraTransferEvents.Imbue im : list) {
            if (!im.onItem && im.holder.equals(holder) && im.trait.equals(trait)) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean backedItem(UUID id) {
      for(List<AuraTransferEvents.Imbue> list : IMBUES.values()) {
         for(AuraTransferEvents.Imbue im : list) {
            if (im.onItem && im.holder.equals(id)) {
               return true;
            }
         }
      }

      return false;
   }

   private static void sweepInventory(ServerPlayer player) {
      Inventory inv = player.getInventory();

      for(int i = 0; i < inv.getContainerSize(); ++i) {
         stripIfOrphan(inv.getItem(i));
      }

   }

   private static void stripIfOrphan(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      if (tag != null && tag.contains("unrp_at_trait")) {
         if (!tag.hasUUID("unrp_at_id") || !backedItem(tag.getUUID("unrp_at_id"))) {
            tag.remove("unrp_at_trait");
            tag.remove("unrp_at_id");
            tag.remove("unrp_at_born");
         }
      }
   }

   private static void stripItemEverywhere(ServerPlayer caster, UUID id) {
      for(ServerPlayer online : caster.server.getPlayerList().getPlayers()) {
         Inventory inv = online.getInventory();

         for(int i = 0; i < inv.getContainerSize(); ++i) {
            CompoundTag tag = inv.getItem(i).getTag();
            if (tag != null && tag.hasUUID("unrp_at_id") && tag.getUUID("unrp_at_id").equals(id)) {
               tag.remove("unrp_at_trait");
               tag.remove("unrp_at_id");
               tag.remove("unrp_at_born");
               return;
            }
         }
      }

   }

   private static String itemTrait(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      if (tag != null && tag.contains("unrp_at_trait") && tag.hasUUID("unrp_at_id")) {
         return !backedItem(tag.getUUID("unrp_at_id")) ? null : tag.getString("unrp_at_trait");
      } else {
         return null;
      }
   }

   @SubscribeEvent
   public static void onHurt(LivingHurtEvent event) {
      if (!reentrant) {
         LivingEntity victim = event.getEntity();
         Level attacker_0 = victim.level();
         if (attacker_0 instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)attacker_0;
            if (victim instanceof ServerPlayer) {
               ServerPlayer vp = (ServerPlayer)victim;
               Set<String> vtags = vp.getTags();
               Entity var7 = event.getSource().getEntity();
               LivingEntity var10000;
               if (var7 instanceof LivingEntity) {
                  LivingEntity le_0 = (LivingEntity)var7;
                  var10000 = le_0;
               } else {
                  var10000 = null;
               }

               LivingEntity attackerLiving = var10000;
               if (vtags.contains("unrp.ati.conjure_disks") && attackerLiving != null && event.getSource().getDirectEntity() == attackerLiving && level.random.nextFloat() < 0.1F) {
                  event.setCanceled(true);
                  Fx.burst(level, Fx.glint(10217727), vp.position().add(0.0D, 1.2D, 0.0D), 8, 0.4D, 0.05D);
                  level.playSound((Player)null, vp.blockPosition(), CastFxEvents.sound("minecraft:item.shield.block"), SoundSource.PLAYERS, 0.7F, 1.3F);
                  vp.displayClientMessage(Component.literal("§bOrbit Guard!"), true);
                  return;
               }

               if (vtags.contains("unrp.ati.umbral_shift") && attackerLiving != null && level.random.nextFloat() < 0.12F) {
                  event.setCanceled(true);
                  Fx.burst(level, Fx.ember(2761784), vp.position().add(0.0D, 1.0D, 0.0D), 12, 0.5D, 0.04D);
                  level.playSound((Player)null, vp.blockPosition(), CastFxEvents.sound("minecraft:entity.enderman.teleport"), SoundSource.PLAYERS, 0.5F, 1.5F);
                  vp.displayClientMessage(Component.literal("§5Shadowstep!"), true);
                  return;
               }

               if (vtags.contains("unrp.ati.barrier")) {
                  event.setAmount(event.getAmount() * 0.85F);
               }

               if (vtags.contains("unrp.ati.telekinesis") && event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
                  event.setAmount(event.getAmount() * 0.7F);
               }

               if (vtags.contains("unrp.ati.tremor") && event.getSource().is(DamageTypeTags.IS_FALL)) {
                  event.setAmount(event.getAmount() * 0.5F);
               }

               if (vtags.contains("unrp.ati.particles") && level.random.nextFloat() < 0.2F && !vp.hasEffect(MobEffects.ABSORPTION)) {
                  vp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0, true, false));
               }

               if (attackerLiving != null && event.getSource().getDirectEntity() == attackerLiving) {
                  if (vtags.contains("unrp.ati.phantom_push")) {
                     attackerLiving.knockback(0.6D, vp.getX() - attackerLiving.getX(), vp.getZ() - attackerLiving.getZ());
                  }

                  if (vtags.contains("unrp.ati.hypnosis") && attackerLiving instanceof Mob) {
                     Mob mob = (Mob)attackerLiving;
                     if (level.random.nextFloat() < 0.15F) {
                        mob.setTarget((LivingEntity)null);
                        mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, true, true));
                     }
                  }

                  if (vtags.contains("unrp.ati.lightning") && OriginStrike.fair(vp, attackerLiving)) {
                     reentrant = true;

                     try {
                        OriginStrike.hit(attackerLiving, level.damageSources().indirectMagic(vp, vp), 2.0F);
                     } finally {
                        reentrant = false;
                     }

                     Fx.burst(level, Fx.arc(9427199), attackerLiving.position().add(0.0D, 1.0D, 0.0D), 5, 0.3D, 0.05D);
                  }

                  if (vtags.contains("unrp.ati.nightmare")) {
                     attackerLiving.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, true, false));
                  }
               }
            }

            Entity var29 = event.getSource().getEntity();
            LivingEntity var38;
            if (var29 instanceof LivingEntity) {
               LivingEntity le = (LivingEntity)var29;
               var38 = le;
            } else {
               var38 = null;
            }

            LivingEntity attacker = var38;
            if (attacker != null) {
               boolean melee = event.getSource().getDirectEntity() == attacker;
               boolean projectile = event.getSource().getDirectEntity() instanceof Projectile;
               if (melee || projectile) {
                  String trait = null;
                  if (melee) {
                     trait = itemTrait(attacker.getMainHandItem());
                  } else {
                     trait = itemTrait(attacker.getMainHandItem());
                     if (trait == null) {
                        trait = itemTrait(attacker.getOffhandItem());
                     }
                  }

                  if (trait != null) {
                     Fx.burst(level, Fx.flare(traitColor(trait)), victim.position().add(0.0D, (double)victim.getBbHeight() * 0.6D, 0.0D), 3, 0.25D, 0.02D);
                     if (projectile) {
                        if (trait.equals("energy_discharge")) {
                           event.setAmount(event.getAmount() * 1.25F);
                           Fx.burst(level, Fx.flare(16738906), victim.position().add(0.0D, 1.0D, 0.0D), 4, 0.3D, 0.03D);
                        } else if (trait.equals("missiles")) {
                           victim.knockback(0.8D, attacker.getX() - victim.getX(), attacker.getZ() - victim.getZ());
                        }

                     } else {
                        switch (trait) {
                           case "flame_claw":
                              victim.setSecondsOnFire(4);
                              Fx.burst(level, Fx.ember(16747578), victim.position().add(0.0D, 1.0D, 0.0D), 6, 0.3D, 0.04D);
                              break;
                           case "hydrofreeze":
                              victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, true, true));
                              Fx.burst(level, Fx.shard(14087423), victim.position().add(0.0D, 1.0D, 0.0D), 8, 0.4D, 0.04D);
                              break;
                           case "blademaster":
                              event.setAmount(event.getAmount() * 1.25F);
                              break;
                           case "dizzy_punch":
                              victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, true, true));
                              victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, true, true));
                              break;
                           case "strong_punch":
                              victim.knockback(1.0D, (double)Mth.sin(attacker.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(attacker.getYRot() * ((float)Math.PI / 180F))));
                              break;
                           case "arachnid":
                              victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0, true, true));
                              break;
                           case "demon_claw":
                              attacker.heal(event.getAmount() * 0.15F);
                              break;
                           case "hand_blade":
                              if (!SgRules.outside(victim) && victim.getArmorValue() >= 8) {
                                 event.setAmount(event.getAmount() + 2.0F);
                              }
                              break;
                           case "contact_shred":
                              if (victim instanceof ServerPlayer) {
                                 ServerPlayer sv = (ServerPlayer)victim;
                                 if (!SgRules.outside(sv)) {
                                    modifier(sv, Attributes.ARMOR, MOD_SHRED, "unrp_at_shred", -2.0D, Operation.ADDITION);
                                    SHRED_UNTIL.put(sv.getUUID(), level.getGameTime() + 160L);
                                    Fx.burst(level, Fx.glint(12109008), sv.position().add(0.0D, 1.0D, 0.0D), 6, 0.3D, 0.05D);
                                 }
                              }
                              break;
                           case "needles":
                              if (!SgRules.outside(victim) && victim.getHealth() >= victim.getMaxHealth() - 0.01F) {
                                 event.setAmount(event.getAmount() + 1.5F);
                              }
                              break;
                           case "ability_gauging":
                              victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false));
                              break;
                           case "spikes":
                              event.setAmount(event.getAmount() + 1.0F);
                              victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, true, true));
                              break;
                           case "conjure_vines":
                              victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 3, true, true));
                              break;
                           case "phantom_fist":
                              event.setAmount(event.getAmount() + 1.0F);
                              victim.knockback(0.5D, attacker.getX() - victim.getX(), attacker.getZ() - victim.getZ());
                              break;
                           case "spectral_claw":
                              event.setAmount(event.getAmount() + 1.5F);
                              Fx.burst(level, Fx.ember(9418968), victim.position().add(0.0D, 1.0D, 0.0D), 5, 0.3D, 0.04D);
                              break;
                           case "explosion":
                              if (level.random.nextFloat() < 0.15F) {
                                 reentrant = true;

                                 try {
                                    for(LivingEntity near_0 : level.getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(1.5D))) {
                                       if (near_0 != attacker && near_0 != victim && OriginStrike.fair(attacker, near_0)) {
                                          OriginStrike.hit(near_0, level.damageSources().indirectMagic(attacker, attacker), 2.0F);
                                       }
                                    }
                                 } finally {
                                    reentrant = false;
                                 }

                                 Fx.shell(level, Fx.wave(16757594), victim.position().add(0.0D, 0.9D, 0.0D), 0.4D, 8, 0.35D);
                                 level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.45F, 1.4F);
                              }
                              break;
                           case "shadow_flame":
                              event.setAmount(event.getAmount() + 1.0F);
                              victim.setSecondsOnFire(2);
                              Fx.burst(level, Fx.ember(5909416), victim.position().add(0.0D, 1.0D, 0.0D), 5, 0.3D, 0.04D);
                              break;
                           case "predator":
                              if (!SgRules.outside(victim) && victim.getHealth() < victim.getMaxHealth() * 0.4F) {
                                 event.setAmount(event.getAmount() * 1.3F);
                              }
                              break;
                           case "catalyst_ray":
                              if (level.random.nextFloat() < 0.1F) {
                                 MobEffect[] pool = new MobEffect[]{MobEffects.CONFUSION, MobEffects.GLOWING, MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.POISON};
                                 victim.addEffect(new MobEffectInstance(pool[level.random.nextInt(pool.length)], 100, 0, true, true));
                                 Fx.burst(level, Fx.flare(11557608), victim.position().add(0.0D, 1.2D, 0.0D), 8, 0.4D, 0.05D);
                                 level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.resonate"), SoundSource.PLAYERS, 0.8F, 0.8F);
                              }
                              break;
                           case "crescent_slash":
                              reentrant = true;

                              try {
                                 for(LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(2.5D))) {
                                    if (near != victim && near != attacker && OriginStrike.fair(attacker, near)) {
                                       OriginStrike.hit(near, level.damageSources().indirectMagic(attacker, attacker), 1.5F);
                                    }
                                 }
                              } finally {
                                 reentrant = false;
                              }

                              Fx.circle(level, Fx.wave(9427176), victim.position().add(0.0D, 1.0D, 0.0D), 1.8D, 10, 0.05D, 0.15D);
                        }

                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onProjectileJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide()) {
         Entity shooter_0 = event.getEntity();
         if (shooter_0 instanceof Projectile) {
            Projectile proj = (Projectile)shooter_0;
            Entity trait_0 = proj.getOwner();
            if (trait_0 instanceof ServerPlayer) {
               ServerPlayer shooter = (ServerPlayer)trait_0;
               String trait = itemTrait(shooter.getMainHandItem());
               if (trait == null) {
                  trait = itemTrait(shooter.getOffhandItem());
               }

               if ("arcane_shot".equals(trait)) {
                  proj.setDeltaMovement(proj.getDeltaMovement().scale(1.4D));
                  proj.hurtMarked = true;
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         clearAll(player, false);
         SHRED_UNTIL.remove(player.getUUID());
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

   static {
      trait("flame_claw", "Searing Edge", 1, 16747578);
      trait("hydrofreeze", "Frostbite", 1, 10217727);
      trait("blademaster", "Honed Edge", 1, 14212840);
      trait("energy_discharge", "Discharge Rounds", 1, 16738906);
      trait("arcane_shot", "Arcane Loft", 1, 11898111);
      trait("catalyst_ray", "Mutagen Taint", 1, 11557608);
      trait("dizzy_punch", "Dazing Blow", 1, 15914076);
      trait("strong_punch", "Hammer Blow", 1, 15245408);
      trait("crescent_slash", "Sweeping Arc", 1, 9427176);
      trait("arachnid", "Venom Fang", 1, 9163338);
      trait("demon_claw", "Ravenous Edge", 1, 14694474);
      trait("contact_shred", "Shredding Touch", 1, 12107980);
      trait("hand_blade", "Keen Pierce", 1, 8380671);
      trait("barrier", "Warding Aura", 2, 7321343);
      trait("speed", "Fleetfoot", 2, 15921274);
      trait("regeneration", "Mending Aura", 2, 16744360);
      trait("healing", "Vital Reserve", 2, 8188570);
      trait("stone_skin", "Ironhide", 2, 11576467);
      trait("armor_suit", "Plated Aura", 2, 13160664);
      trait("quick_strike", "Battle Tempo", 2, 16765274);
      trait("umbral_shift", "Shadowstep", 2, 10121944);
      trait("invisibility", "Ghostveil", 2, 14216447);
      trait("lightning", "Static Ward", 2, 9427199);
      trait("illumination", "Bright Eyes", 2, 16773800);
      trait("whirlwind", "Windborne", 2, 12447718);
      trait("tremor", "Groundsense", 2, 13146714);
      trait("telekinesis", "Deflection Field", 2, 12614911);
      trait("heat_palm", "Emberskin", 2, 16738874);
      trait("nightmare", "Dread Aura", 2, 10509000);
      trait("strong_kick", "Springheel", 2, 15249504);
      trait("phase_shift", "Fluid Form", 2, 7332048);
      trait("rock_arm", "Bulwark Arm", 2, 11573888);
      trait("energy_conduit", "Efficient Flow", 2, 5957832);
      trait("phantom_push", "Phantom Recoil", 2, 11061503);
      trait("azure_storm", "Stormrider", 2, 3844351);
      trait("particles", "Latticework", 2, 15263999);
      trait("conjure_disks", "Orbit Guard", 2, 10217727);
      trait("hypnosis", "Lulling Aura", 2, 14712048);
      trait("needles", "Needlepoint", 1, 13162720);
      trait("ability_gauging", "Appraising Touch", 1, 15786106);
      trait("spikes", "Barbed Edge", 1, 11055288);
      trait("missiles", "Concussive Rounds", 1, 16751178);
      trait("conjure_vines", "Binding Lash", 1, 5949530);
      trait("phantom_fist", "Spectral Shove", 1, 9419007);
      trait("spectral_claw", "Phase Rend", 1, 9418968);
      trait("explosion", "Volatile Edge", 1, 16757594);
      trait("shadow_flame", "Cinderbrand", 1, 9067240);
      trait("predator", "Predatory Edge", 1, 14703172);
      IMBUES = new HashMap();
      SHRED_UNTIL = new HashMap();
      reentrant = false;
   }

   private static final class Imbue {
      final String trait;
      final boolean onItem;
      final UUID holder;
      int tether = -1;

      Imbue(String trait, boolean onItem, UUID holder) {
         this.trait = trait;
         this.onItem = onItem;
         this.holder = holder;
      }
   }
}
