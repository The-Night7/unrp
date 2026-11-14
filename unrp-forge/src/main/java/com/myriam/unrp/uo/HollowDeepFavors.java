package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;
import com.myriam.unrp.deal.Spectacle;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public final class HollowDeepFavors {
   public static final int THRALL = 7;
   public static final int SYMPATHY = 8;
   public static final int SHADE = 9;
   public static final int CALCIFY = 10;
   public static final int DOPPEL = 11;
   public static final int BEND = 12;
   public static final int GIFT = 13;
   public static final String T_THRALL = "unrp.hm_thrall_trigger";
   public static final String T_SYMPATHY = "unrp.hm_sympathy_trigger";
   public static final String T_SHADE = "unrp.hm_shade_trigger";
   public static final String T_CALCIFY = "unrp.hm_calcify_trigger";
   public static final String T_BEND = "unrp.hm_bend_trigger";
   public static final String SHADE_ON = "unrp.hm_shade";
   public static final String STONE_TAG = "unrp.hm_stone";
   public static final String STONE_NOAI_TAG = "unrp.hm_stone_noai";
   public static final String OWNS = "unrp.hm_owns.";
   public static final String HELD_SHOT = "unrp.hm_held_shot";
   public static final String HELD_SHOT_NG = "unrp.hm_held_shot_ng";
   private static final int ASH = 10133670;
   private static final int BONE = 15262938;
   private static final int GLYPH = 8381416;
   private static final int PALE = 6253168;
   private static final int STONE = 9211794;
   public static final int THRALL_COST = 24;
   public static final int EYES_COST = 6;
   public static final int SYMPATHY_COST = 16;
   public static final int CALCIFY_COST = 26;
   public static final double THRALL_LEASH = 48.0D;
   public static final double THRALL_FOLLOW = 5.0D;
   public static final double THRALL_CATCH_UP = 28.0D;
   public static final double THRALL_HUNT = 24.0D;
   public static final int THRALL_MEMORY = 160;
   public static final double SHADE_STILL = 0.09D;
   public static final double SHADE_SEEN_WITHIN = 2.5D;
   public static final double SHADE_LOSE = 16.0D;
   public static final double SYMPATHY_SPAN = 64.0D;
   public static final double SYMPATHY_LEASH = 96.0D;
   public static final double STONE_SPREAD = 2.5D;
   public static final double SHADE_SOFT = 0.13D;
   public static final int DOPPEL_COST = 22;
   public static final int BEND_COST = 40;
   public static final int GIFT_COST = 30;
   public static final double DOPPEL_RUN = 10.0D;
   public static final double DOPPEL_BURST = 3.0D;
   public static final double GIFT_NEAR = 6.0D;
   public static final double GIFT_SPREAD = 4.0D;
   public static final float MARROW_GUARD = 0.8F;
   public static final float MARROW_MEND = 0.65F;
   public static final int WHISPER = 200;
   private static final UUID GRAVE_HEALTH = UUID.fromString("5f0c8f1e-61a2-4c7e-9a8e-3b2d6c1a9e47");
   private static final UUID VIGOR_HEALTH = UUID.fromString("9b3e2d71-4c58-4f0a-b6e1-2a7d5c8e0f13");
   private static final UUID SWIFT_SPEED = UUID.fromString("3d7a1c94-0b6e-4f52-8c1d-7e9a2b5f4c60");
   public static final double SWIFT = 0.2D;
   public static final int HELD_GRACE = 120;
   public static final String[] HUMANOIDS = new String[]{"minecraft:zombie", "minecraft:husk", "minecraft:drowned", "minecraft:zombie_villager", "minecraft:zombified_piglin", "minecraft:skeleton", "minecraft:stray", "minecraft:wither_skeleton", "minecraft:piglin", "minecraft:piglin_brute", "minecraft:villager", "minecraft:wandering_trader", "minecraft:vindicator", "minecraft:pillager"};
   private static final Map<UUID, HollowDeepFavors.Doppel> doppels = new LinkedHashMap();
   private static final Map<UUID, HollowDeepFavors.Bubble> bubbles = new HashMap();
   private static final Map<UUID, UUID> heldIn = new HashMap();
   private static final Map<UUID, Long> bendReady = new HashMap();
   private static final Map<UUID, Integer> giftTurn = new HashMap();
   private static final Map<UUID, Long> heldGrace = new HashMap();
   private static final Map<UUID, HollowDeepFavors.Thrall> thralls = new LinkedHashMap();
   private static final Map<UUID, long[]> eyes = new HashMap();
   private static final Map<UUID, Object[]> lastStruck = new HashMap();
   private static final Map<UUID, Object[]> lastStriker = new HashMap();
   private static final Map<UUID, LinkedHashMap<UUID, Long>> links = new HashMap();
   private static final Set<UUID> echoing = new HashSet();
   private static final Map<UUID, HollowDeepFavors.Shade> shades = new HashMap();
   private static final Map<UUID, HollowDeepFavors.Stone> stones = new HashMap();

   public static double thrallReach(int r) {
      return Math.min(32.0D, 14.0D + (double)r);
   }

   public static int thrallTicks(int r) {
      return Math.min(6000, 600 + 200 * r);
   }

   public static float thrallBite(int r) {
      return 3.0F + 0.9F * (float)r;
   }

   public static int thrallGap(int r) {
      return Math.max(10, 24 - r);
   }

   public static int thrallCap(int r) {
      int[] mile = HollowMarkEvents.MILE_AT[7];
      return 1 + (r >= mile[1] ? 1 : 0) + (r >= mile[2] ? 1 : 0);
   }

   public static int eyeTicks(int r) {
      return Math.min(160, 60 + 10 * r);
   }

   public static double sympathyReach(int r) {
      return Math.min(40.0D, 20.0D + (double)r);
   }

   public static int sympathyTicks(int r) {
      return Math.min(1800, 400 + 60 * r);
   }

   public static float sympathyShare(int r) {
      return Math.min(0.75F, 0.3F + 0.03F * (float)r);
   }

   public static int sympathyCap(int r) {
      return Math.min(6, 3 + (r >= HollowMarkEvents.MILE_AT[8][0] ? 1 : 0) + r / 6);
   }

   public static float echoCap(int r) {
      return 6.0F + 1.5F * (float)r;
   }

   public static float knell(int r) {
      return 4.0F + 0.8F * (float)r;
   }

   public static int shadeSettle(int r) {
      return Math.max(4, 20 - 2 * r);
   }

   public static int shadeGrace(int r) {
      return 10 + 4 * r;
   }

   public static int shadeDrain(int r) {
      return Math.max(1, 3 - r / 3);
   }

   public static float firstCut(int r) {
      return Math.min(0.75F, 0.3F + 0.03F * (float)Math.max(0, r - 5));
   }

   public static double calcifyReach(int r) {
      return Math.min(28.0D, 12.0D + 0.5D * (double)r);
   }

   public static int stoneTicks(int r) {
      return Math.min(240, 40 + 10 * r);
   }

   public static float shatter(int r) {
      return Math.min(2.0F, 1.4F + 0.03F * (float)Math.max(0, r - 5));
   }

   public static int doppelTicks(int r) {
      return Math.min(1200, 200 + 40 * r);
   }

   public static float doppelHealth(int r) {
      return Math.min(200.0F, 10.0F + 4.0F * (float)r);
   }

   public static double doppelLure(int r) {
      return Math.min(32.0D, 12.0D + (double)r);
   }

   public static int doppelCap(int r) {
      return r >= HollowMarkEvents.MILE_AT[11][1] ? 2 : 1;
   }

   public static int doppelBlinkGap(int r) {
      return Math.max(30, 60 - 2 * r);
   }

   public static float doppelBurst(int r) {
      return 4.0F + 0.8F * (float)r;
   }

   public static double bendRadius(int r) {
      return Math.min(12.0D, 6.0D + 0.25D * (double)r);
   }

   public static int bendTicks(int r) {
      int[] mile = HollowMarkEvents.MILE_AT[12];
      return r < mile[0] ? 60 : (r < mile[1] ? 120 : Math.min(320, 200 + 10 * (r - mile[1])));
   }

   public static int bendRecharge(int r) {
      return Math.max(400, 900 - 20 * r);
   }

   public static float bendRelease(int r) {
      return 3.0F + 0.6F * (float)r;
   }

   public static double giftReach(int r) {
      return Math.min(24.0D, 10.0D + (double)r);
   }

   public static int giftTicks(int r) {
      return Math.min(2400, 600 + 120 * r);
   }

   private HollowDeepFavors() {
   }

   private static int rank(ServerPlayer player, int row) {
      return HollowMarkEvents.rank(player, row);
   }

   private static void tell(ServerPlayer player, String msg) {
      HollowMarkEvents.say(player, msg);
   }

   private static String secs(int ticks) {
      int tenths = Math.round((float)ticks / 2.0F);
      return tenths % 10 == 0 ? String.valueOf(tenths / 10) : tenths / 10 + "." + tenths % 10;
   }

   private static String nameOf(Entity e) {
      return e.getName().getString();
   }

   private static String typeOf(Entity e) {
      ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
      return key == null ? "" : key.toString();
   }

   private static Vec3 mid(Entity e) {
      return e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D);
   }

   private static <T extends LivingEntity> T aimed(ServerPlayer player, ServerLevel level, Class<T> cls, double reach, Predicate<T> keep) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      T best = null;
      double bestDot = 0.96D;

      for(T live : level.getEntitiesOfClass(cls, player.getBoundingBox().inflate(reach), (e) -> e != player && e.isAlive())) {
         Vec3 to = mid(live).subtract(eye);
         double len = to.length();
         if (!(len > reach) && !(len < 0.001D) && keep.test(live)) {
            double dot = to.scale(1.0D / len).dot(look);
            if (dot > bestDot && player.hasLineOfSight(live)) {
               bestDot = dot;
               best = live;
            }
         }
      }

      return best;
   }

   public static boolean servesOwner(LivingEntity live, LivingEntity caster) {
      HollowDeepFavors.Thrall t = (HollowDeepFavors.Thrall)thralls.get(live.getUUID());
      HollowDeepFavors.Doppel copy = (HollowDeepFavors.Doppel)doppels.get(live.getUUID());
      UUID master = t != null ? t.owner : (copy != null ? copy.owner : null);
      return master != null && master.equals(caster.getUUID());
   }

   public static boolean doppelKnown(Entity e) {
      return doppels.containsKey(e.getUUID());
   }

   public static boolean bendHeld(Entity e) {
      return heldIn.containsKey(e.getUUID());
   }

   private static boolean worn(ServerPlayer player, String kind) {
      return player.getTags().contains("unrp.hm_charm." + kind) && HollowMarkEvents.holdsMark(player);
   }

   public static double blinkBonus(ServerPlayer player) {
      return (worn(player, "reach") ? 3.0D : 0.0D) + (worn(player, "grave_stride") ? 6.0D : 0.0D);
   }

   public static double gazeBoost(ServerPlayer player) {
      return worn(player, "hollow_sight") ? 1.5D : 1.0D;
   }

   public static int greed(ServerPlayer killer, int runes, ServerLevel level) {
      if (!worn(killer, "bone_greed")) {
         return runes;
      } else if (runes > 0) {
         return runes + (level.random.nextFloat() < 0.5F ? 1 : 0);
      } else {
         return level.random.nextFloat() < 0.08F ? 1 : 0;
      }
   }

   public static boolean shrouded(ServerPlayer player) {
      HollowDeepFavors.Shade s = (HollowDeepFavors.Shade)shades.get(player.getUUID());
      return s != null && s.shrouded;
   }

   public static boolean stone(Entity e) {
      return stones.containsKey(e.getUUID());
   }

   public static boolean priorNoAi(Mob mob) {
      return mob.getTags().contains("unrp.hm_stone") ? mob.getTags().contains("unrp.hm_stone_noai") : PuppetEvents.priorNoAi(mob);
   }

   private static boolean frozenByTime(Entity e) {
      return e.getTags().contains("unrp.time_frozen") || TimeWeft.held(e);
   }

   public static boolean timeHeld(Entity e) {
      return frozenByTime(e) || bendHeld(e);
   }

   private static boolean humanoid(Entity e) {
      String id = typeOf(e);

      for(String h : HUMANOIDS) {
         if (h.equals(id)) {
            return true;
         }
      }

      return false;
   }

   private static boolean casting(ServerPlayer player, Set<String> tags) {
      return tags.contains("unrp.hm_blink_trigger") || tags.contains("unrp.hm_gaze_trigger") || tags.contains("unrp.hm_slam_trigger") || tags.contains("unrp.hm_thrall_trigger") || tags.contains("unrp.hm_sympathy_trigger") && !player.isShiftKeyDown() || tags.contains("unrp.hm_calcify_trigger") || tags.contains("unrp.hm_bend_trigger") || tags.contains("unrp.hm_darts_trigger") && !player.isShiftKeyDown();
   }

   public static void syncOwned(ServerPlayer player) {
      for(int row = 7; row <= 13; ++row) {
         String tag = "unrp.hm_owns." + HollowMarkEvents.ABILITY[row];
         if (rank(player, row) >= 1) {
            if (!player.getTags().contains(tag)) {
               player.addTag(tag);
            }
         } else if (player.getTags().contains(tag)) {
            player.removeTag(tag);
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player tags = event.player;
         if (tags instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)tags;
            Level var10 = player.level();
            if (var10 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var10;
               Set var11 = player.getTags();
               boolean armed = HollowMarkEvents.armedFor(player);
               HollowDeepFavors.Shade cloak = (HollowDeepFavors.Shade)shades.get(player.getUUID());
               if (cloak != null && cloak.shrouded && casting(player, var11)) {
                  unveil(player, level, cloak, "§8The cast tears you out of the grey.");
                  cloak.lockUntil = level.getGameTime() + 30L;
               }

               if (var11.contains("unrp.hm_thrall_trigger")) {
                  player.removeTag("unrp.hm_thrall_trigger");
                  if (armed) {
                     if (player.isShiftKeyDown()) {
                        borrowEyes(player, level);
                     } else {
                        doThrall(player, level);
                     }
                  }
               }

               if (var11.contains("unrp.hm_sympathy_trigger")) {
                  player.removeTag("unrp.hm_sympathy_trigger");
                  if (armed) {
                     if (player.isShiftKeyDown()) {
                        doGift(player, level);
                     } else {
                        doSympathy(player, level);
                     }
                  }
               }

               if (var11.contains("unrp.hm_shade_trigger")) {
                  player.removeTag("unrp.hm_shade_trigger");
                  if (armed) {
                     if (player.isShiftKeyDown()) {
                        doDoppel(player, level);
                     } else {
                        toggleShade(player, level);
                     }
                  }
               }

               if (var11.contains("unrp.hm_calcify_trigger")) {
                  player.removeTag("unrp.hm_calcify_trigger");
                  if (armed) {
                     doCalcify(player, level);
                  }
               }

               if (var11.contains("unrp.hm_bend_trigger")) {
                  player.removeTag("unrp.hm_bend_trigger");
                  if (armed) {
                     doBend(player, level);
                  }
               }

               UUID id = player.getUUID();
               long now = level.getGameTime();
               if (HollowMarkEvents.holdsMark(player)) {
                  tickShade(player, level, now, armed);
                  long[] seeing = (long[])eyes.get(id);
                  if (seeing != null && now >= seeing[0]) {
                     eyes.remove(id);
                  }

                  if (player.tickCount % 20 == 0) {
                     syncOwned(player);
                     corrupt(player, now);
                  }

                  return;
               }

               charmHealth(player, false);
               if (var11.contains("unrp.hm_shade") || shades.containsKey(id)) {
                  player.removeTag("unrp.hm_shade");
                  HollowDeepFavors.Shade s = (HollowDeepFavors.Shade)shades.remove(id);
                  if (s != null && s.shrouded) {
                     unveil(player, level, s, (String)null);
                  }
               }

               return;
            }
         }
      }

   }

   private static void healthMod(ServerPlayer player, UUID id, String name, double amount, boolean on) {
      AttributeInstance hp = player.getAttribute(Attributes.MAX_HEALTH);
      if (hp != null) {
         AttributeModifier had = hp.getModifier(id);
         if (on && had == null) {
            hp.addPermanentModifier(new AttributeModifier(id, name, amount, Operation.ADDITION));
         } else {
            if (on || had == null) {
               return;
            }

            hp.removeModifier(id);
         }

         if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
         }

      }
   }

   private static void charmHealth(ServerPlayer player, boolean holder) {
      healthMod(player, GRAVE_HEALTH, "unrp_grave_stride", -4.0D, holder && worn(player, "grave_stride"));
      healthMod(player, VIGOR_HEALTH, "unrp_charm_vigor", 4.0D, holder && worn(player, "vigor"));
      charmSpeed(player, holder && worn(player, "swift"));
   }

   private static void charmSpeed(ServerPlayer player, boolean on) {
      AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null) {
         AttributeModifier had = speed.getModifier(SWIFT_SPEED);
         if (on && had == null) {
            speed.addPermanentModifier(new AttributeModifier(SWIFT_SPEED, "unrp_charm_swift", 0.2D, Operation.MULTIPLY_TOTAL));
         } else if (!on && had != null) {
            speed.removeModifier(SWIFT_SPEED);
         }

      }
   }

   private static void corrupt(ServerPlayer player, long now) {
      charmHealth(player, true);
      if (worn(player, "hollow_sight") && now % 600L < 20L) {
         player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false, true));
      }

      if (worn(player, "bone_greed")) {
         player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 0, false, false, true));
      }

      boolean any = false;

      for(int i = 5; i < BoneCharmItem.KINDS.length; ++i) {
         any |= worn(player, BoneCharmItem.KINDS[i]);
      }

      if (any && now % 200L < 20L) {
         player.playNotifySound(CastFxEvents.sound("minecraft:ambient.cave"), SoundSource.PLAYERS, 0.3F, 1.7F);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void onMarrowGuard(LivingHurtEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (worn(player, "marrow_ward") && HollowMarkEvents.armedFor(player)) {
            event.setAmount(event.getAmount() * 0.8F);
         }
      }

   }

   @SubscribeEvent
   public static void onMarrowMend(LivingHealEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (worn(player, "marrow_ward")) {
            event.setAmount(event.getAmount() * 0.65F);
         }
      }

   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         MinecraftServer server = event.getServer();
         tickThralls(server);
         tickLinks(server);
         tickStones();
         tickDoppels(server);
         tickBubbles(server);
      }
   }

   private static List<UUID> doppelsOf(UUID owner) {
      List<UUID> out = new ArrayList();

      for(Map.Entry<UUID, HollowDeepFavors.Doppel> e : doppels.entrySet()) {
         if (((HollowDeepFavors.Doppel)e.getValue()).owner.equals(owner)) {
            out.add((UUID)e.getKey());
         }
      }

      return out;
   }

   private static void doDoppel(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 11);
      if (r < 1) {
         tell(player, "§8Doppel isn't yours yet.");
      } else if (HollowMarkEvents.payFor(player, 22)) {
         int cap = doppelCap(r);
         HollowDoppelEntity copy = (HollowDoppelEntity)(com.myriam.unrp.entity.ModEntities.HOLLOW_DOPPEL.get()).create(level);
         if (copy == null) {
            HollowMarkEvents.refundFor(player, 22);
         } else {
            copy.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
            copy.setOwnerId(player.getUUID());
            copy.setCustomName(player.getName());
            copy.setCustomNameVisible(true);
            float health = doppelHealth(r);
            AttributeInstance hp = copy.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) {
               hp.setBaseValue((double)health);
            }

            copy.setHealth(health);
            long now = level.getGameTime();
            doppels.put(copy.getUUID(), new HollowDeepFavors.Doppel(player.getUUID(), level, r, now + (long)doppelTicks(r), now + (long)doppelBlinkGap(r)));
            if (!level.addFreshEntity(copy)) {
               doppels.remove(copy.getUUID());
               HollowMarkEvents.refundFor(player, 22);
               tell(player, "§8The grey won't hold a double here.");
            } else {
               List<UUID> mine = doppelsOf(player.getUUID());

               for(int i = 0; i < mine.size() - cap; ++i) {
                  HollowDeepFavors.Doppel old = (HollowDeepFavors.Doppel)doppels.remove(mine.get(i));
                  if (old != null) {
                     Entity ahead_0 = old.level.getEntity((UUID)mine.get(i));
                     if (ahead_0 instanceof HollowDoppelEntity) {
                        HollowDoppelEntity gone = (HollowDoppelEntity)ahead_0;
                        fade(old.level, gone);
                        gone.discard();
                     }
                  }
               }

               Vec3 look = player.getLookAngle();
               Vec3 flat = new Vec3(look.x, 0.0D, look.z);
               Vec3 dir = flat.lengthSqr() < 1.0E-4D ? Vec3.ZERO : flat.normalize();
               Vec3 ahead = player.position().add(dir.scale(10.0D));
               copy.getNavigation().moveTo(ahead.x, ahead.y, ahead.z, 1.1D);
               lure(level, player, copy, r);
               if (!player.isInvisible()) {
                  Fx.burst(level, Fx.ember(10133670), player.position().add(0.0D, 1.0D, 0.0D), 18, 0.35D, 0.03D);
                  Fx.ring(level, 8381416, player.position().add(0.0D, 0.1D, 0.0D), 0.9D, 12);
               }

               level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.illusioner.cast_spell"), SoundSource.PLAYERS, 0.6F, 0.6F);
               tell(player, "§bDOPPEL§8: your double steps out for " + secs(doppelTicks(r)) + "s §8(" + doppelsOf(player.getUUID()).size() + "/" + cap + ")");
            }
         }
      }
   }

   private static void lure(ServerLevel level, ServerPlayer owner, HollowDoppelEntity copy, int r) {
      for(Mob m : level.getEntitiesOfClass(Mob.class, copy.getBoundingBox().inflate(doppelLure(r)), (mx) -> mx.isAlive() && mx.getTarget() == owner)) {
         m.setTarget(copy);
      }

   }

   private static void fade(ServerLevel level, LivingEntity copy) {
      if (!copy.isInvisible()) {
         Fx.burst(level, Fx.ember(10133670), mid(copy), 20, 0.35D, 0.03D);
         Fx.burst(level, Fx.glint(8381416), mid(copy), 6, 0.2D, 0.03D);
      }

      level.playSound((Player)null, copy.blockPosition(), CastFxEvents.sound("minecraft:block.sculk_shrieker.hit"), SoundSource.PLAYERS, 0.5F, 1.6F);
   }

   private static void burst(ServerLevel level, ServerPlayer owner, LivingEntity copy, HollowDeepFavors.Doppel d) {
      if (!d.burst && owner != null && d.rank >= HollowMarkEvents.MILE_AT[11][2]) {
         d.burst = true;
         Vec3 at = mid(copy);

         for(LivingEntity live : level.getEntitiesOfClass(LivingEntity.class, (new AABB(at, at)).inflate(5.0D), (e) -> {
            if (e != copy) {
               if (OriginStrike.threat(owner, e)) {
                  return true;
               }

               if (e instanceof Mob) {
                  Mob m = (Mob)e;
                  if (m.getTarget() == copy && OriginStrike.fair(owner, e)) {
                     return true;
                  }
               }
            }

            return false;
         })) {
            if (!(mid(live).distanceTo(at) > 3.0D + (double)live.getBbWidth() * 0.5D)) {
               OriginStrike.hitFresh(live, HollowMarkEvents.greyFor(owner, level), doppelBurst(d.rank));
            }
         }

         if (!copy.isInvisible()) {
            Spectacle.shockwave(level, 8381416, copy.position(), 3.0D);
            Fx.shell(level, Fx.ember(6253168), mid(copy), 2.0999999999999996D, 26, 0.3D);
         }

         level.playSound((Player)null, copy.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.sonic_boom"), SoundSource.PLAYERS, 0.4F, 1.8F);
      }
   }

   private static void doppelBlink(ServerLevel level, HollowDoppelEntity copy) {
      for(int i = 0; i < 6; ++i) {
         double a = level.random.nextDouble() * Math.PI * 2.0D;
         double reach = 3.0D + level.random.nextDouble() * 2.0D;
         Vec3 to = copy.position().add(Math.cos(a) * reach, 0.0D, Math.sin(a) * reach);
         BlockPos feet = BlockPos.containing(to);
         if (level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir() && !level.getBlockState(feet.below()).isAir()) {
            fade(level, copy);
            copy.teleportTo(to.x, (double)feet.getY(), to.z);
            copy.getNavigation().stop();
            return;
         }
      }

   }

   private static void tickDoppels(MinecraftServer server) {
      if (!doppels.isEmpty()) {
         for(Map.Entry<UUID, HollowDeepFavors.Doppel> e : new ArrayList<>(doppels.entrySet())) {
            HollowDeepFavors.Doppel d = (HollowDeepFavors.Doppel)e.getValue();
            if (doppels.get(e.getKey()) == d) {
               Entity raw = d.level.getEntity((UUID)e.getKey());
               if (raw instanceof HollowDoppelEntity) {
                  HollowDoppelEntity copy = (HollowDoppelEntity)raw;
                  if (copy.isAlive()) {
                     ServerPlayer owner = server.getPlayerList().getPlayer(d.owner);
                     long now = d.level.getGameTime();
                     if (owner != null && HollowMarkEvents.holdsMark(owner) && owner.level() == d.level && now < d.until) {
                        if (now % 20L == 0L) {
                           lure(d.level, owner, copy, d.rank);
                        }

                        if (d.rank >= HollowMarkEvents.MILE_AT[11][0] && now >= d.nextBlink) {
                           d.nextBlink = now + (long)doppelBlinkGap(d.rank);
                           doppelBlink(d.level, copy);
                        }
                     } else {
                        burst(d.level, owner, copy, d);
                        if (!copy.isRemoved()) {
                           fade(d.level, copy);
                           copy.discard();
                        }

                        doppels.remove(e.getKey());
                     }
                     continue;
                  }
               }

               doppels.remove(e.getKey());
            }
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onDoppelBlow(LivingDamageEvent event) {
      LivingEntity live = event.getEntity();
      HollowDeepFavors.Doppel d = (HollowDeepFavors.Doppel)doppels.get(live.getUUID());
      if (d != null && !(event.getAmount() < live.getHealth())) {
         Level owner_0 = live.level();
         if (owner_0 instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)owner_0;
            event.setCanceled(true);
            if (!live.isAlive()) {
               return;
            }

            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(d.owner);
            burst(level, owner, live, d);
            if (live.isRemoved()) {
               return;
            }

            fade(level, live);
            live.discard();
            if (owner != null) {
               tell(owner, "§7Your double comes apart.");
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onDoppelFalls(LivingDeathEvent event) {
      HollowDeepFavors.Doppel d = (HollowDeepFavors.Doppel)doppels.get(event.getEntity().getUUID());
      if (d != null) {
         Level owner = event.getEntity().level();
         if (owner instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)owner;
            ServerPlayer var4 = level.getServer().getPlayerList().getPlayer(d.owner);
            burst(level, var4, event.getEntity(), d);
            fade(level, event.getEntity());
            if (var4 != null) {
               tell(var4, "§7Your double comes apart.");
            }

            return;
         }
      }

   }

   private static void doBend(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 12);
      if (r < 1) {
         tell(player, "§8Bend Time isn't yours yet.");
      } else {
         UUID id = player.getUUID();
         long now = level.getGameTime();
         Long ready = (Long)bendReady.get(id);
         if (bubbles.containsKey(id)) {
            tell(player, "§8The hour is already bent.");
         } else if (ready != null && now < ready) {
            tell(player, "§8The hour won't bend again for " + secs((int)(ready - now)) + "s.");
         } else if (HollowMarkEvents.payFor(player, 40)) {
            boolean stop = r >= HollowMarkEvents.MILE_AT[12][0];
            int ticks = bendTicks(r);
            double radius = bendRadius(r);
            bubbles.put(id, new HollowDeepFavors.Bubble(id, level, player.position(), radius, r, stop, now + (long)ticks));
            bendReady.put(id, now + (long)ticks + (long)bendRecharge(r));
            CastFxEvents.pose(player, "hm_brand", 20);
            if (!player.isInvisible()) {
               Spectacle.shockwave(level, 6253168, player.position(), radius);
               Fx.shell(level, Fx.ember(8381416), player.position().add(0.0D, 1.0D, 0.0D), radius, 60, 0.0D);
            }

            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.deactivate"), SoundSource.PLAYERS, 1.0F, 0.5F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.respawn_anchor.deplete"), SoundSource.PLAYERS, 0.8F, 0.6F);
            tell(player, "§bBEND TIME§8: the hour " + (stop ? "stops" : "slows") + " for " + secs(ticks) + "s");
         }
      }
   }

   private static boolean bends(ServerPlayer caster, LivingEntity e) {
      return e != caster && e.isAlive() && OriginStrike.foe(caster, e) && !TimeWeft.timeImmune(e) && !TimeWeft.everpresent(e) && !stone(e) && !vast(e);
   }

   private static void tickBubbles(MinecraftServer server) {
      if (!bubbles.isEmpty()) {
         for(Map.Entry<UUID, HollowDeepFavors.Bubble> en : new ArrayList<>(bubbles.entrySet())) {
            HollowDeepFavors.Bubble b = (HollowDeepFavors.Bubble)en.getValue();
            if (bubbles.get(en.getKey()) == b) {
               ServerPlayer caster = server.getPlayerList().getPlayer(b.caster);
               ServerLevel level = b.level;
               long now = level.getGameTime();
               boolean lost = caster == null || caster.level() != level || !HollowMarkEvents.holdsMark(caster);
               if (!lost && now < b.until) {
                  if (now % 10L == 0L) {
                     Fx.circle(level, Fx.ember(10133670), b.center.add(0.0D, 0.15D, 0.0D), b.radius, 40, 0.0D, 0.0D);
                     Fx.shell(level, Fx.ember(b.stop ? 8381416 : 6253168), b.center.add(0.0D, 1.0D, 0.0D), b.radius, 24, 0.0D);
                  }

                  AABB box = (new AABB(b.center, b.center)).inflate(b.radius);

                  for(LivingEntity e_0 : level.getEntitiesOfClass(LivingEntity.class, box, (live) -> bends(caster, live))) {
                     if (!(e_0.position().distanceTo(b.center) > b.radius)) {
                        if (!b.stop) {
                           e_0.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 3, false, false, true));
                           e_0.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 8, 2, false, false, true));
                           e_0.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 8, 0, false, false, true));
                        } else {
                           UUID eid = e_0.getUUID();
                           if (!b.held.containsKey(eid) && !heldIn.containsKey(eid) && !b.freed.contains(eid) && heldGrace.getOrDefault(eid, 0L) <= now) {
                              long left = b.until - now;
                              long until_0 = e_0 instanceof Player ? now + Math.min(120L, left / 2L) : (boss(e_0) ? now + Math.max(20L, left / 3L) : b.until);
                              b.held.put(eid, e_0.position());
                              b.heldUntil.put(eid, until_0);
                              heldIn.put(eid, b.caster);
                              OriginLooks.held(level, e_0, (int)(until_0 - now));
                           }
                        }
                     }
                  }

                  List<LivingEntity> released = new ArrayList();
                  Iterator<Map.Entry<UUID, Vec3>> hit = b.held.entrySet().iterator();

                  while(hit.hasNext()) {
                     Map.Entry<UUID, Vec3> h = (Map.Entry)hit.next();
                     Entity raw = level.getEntity((UUID)h.getKey());
                     Long until = (Long)b.heldUntil.get(h.getKey());
                     if (raw instanceof LivingEntity) {
                        LivingEntity e = (LivingEntity)raw;
                        if (e.isAlive() && until != null && now < until && !TimeWeft.everpresent(e) && !TimeWeft.timeImmune(e)) {
                           if (e instanceof Player && e.isPassenger()) {
                              e.stopRiding();
                           }

                           Vec3 at = (Vec3)h.getValue();
                           e.teleportTo(at.x, at.y, at.z);
                           e.setDeltaMovement(Vec3.ZERO);
                           e.hurtMarked = true;
                           e.fallDistance = 0.0F;
                           if (now % 20L == 0L) {
                              OriginLooks.held(level, e, (int)(until - now));
                           }

                           if (e instanceof Mob) {
                              Mob mob = (Mob)e;
                              mob.setTarget((LivingEntity)null);
                              mob.getNavigation().stop();
                              if (mob instanceof Creeper) {
                                 Creeper creeper = (Creeper)mob;
                                 creeper.setSwellDir(-1);
                              }
                           }
                           continue;
                        }
                     }

                     hit.remove();
                     b.freed.add((UUID)h.getKey());
                     b.heldUntil.remove(h.getKey());
                     heldIn.remove(h.getKey());
                     if (raw != null) {
                        OriginLooks.held(level, raw, 0);
                     }

                     if (raw instanceof LivingEntity) {
                        LivingEntity done_0 = (LivingEntity)raw;
                        if (done_0.isAlive() && until != null && now >= until) {
                           if (done_0 instanceof Player) {
                              heldGrace.put((UUID)h.getKey(), now + 120L);
                           }

                           released.add(done_0);
                        }
                     }
                  }

                  for(Projectile shot : level.getEntitiesOfClass(Projectile.class, box, (p) -> {
                     if (p.isAlive() && p.getOwner() != caster) {
                        Entity patt38347$temp = p.getOwner();
                        if (!(patt38347$temp instanceof LivingEntity)) {
                           return true;
                        }

                        LivingEntity shooter = (LivingEntity)patt38347$temp;
                        if (OriginStrike.foe(caster, shooter) && !TimeWeft.timeImmune(shooter) && !TimeWeft.everpresent(shooter)) {
                           return true;
                        }
                     }

                     return false;
                  })) {
                     if (!(shot.position().distanceTo(b.center) > b.radius)) {
                        if (b.stop) {
                           UUID sid = shot.getUUID();
                           if (!b.shots.containsKey(sid)) {
                              if (shot.getTags().contains("unrp.hm_held_shot") || shot.getTags().contains("unrp.hm_held_shot_ng") || shot.isNoGravity() && shot.getDeltaMovement().lengthSqr() < 1.0E-6D) {
                                 continue;
                              }

                              b.shots.put(sid, new Object[]{shot.getDeltaMovement(), shot.isNoGravity()});
                              shot.addTag(shot.isNoGravity() ? "unrp.hm_held_shot_ng" : "unrp.hm_held_shot");
                           }

                           shot.setDeltaMovement(Vec3.ZERO);
                           shot.setNoGravity(true);
                           shot.hurtMarked = true;
                        } else if (shot.getDeltaMovement().lengthSqr() > 0.0025D) {
                           shot.setDeltaMovement(shot.getDeltaMovement().scale(0.8D));
                           shot.hurtMarked = true;
                        }
                     }
                  }

                  for(LivingEntity done : released) {
                     releaseBlow(b, caster, level, done);
                  }
               } else {
                  bubbles.remove(en.getKey());
                  unbend(b, lost ? null : caster);
               }
            }
         }

      }
   }

   private static void releaseBlow(HollowDeepFavors.Bubble b, ServerPlayer caster, ServerLevel level, LivingEntity live) {
      if (b.rank >= HollowMarkEvents.MILE_AT[12][2] && caster != null && live.isAlive() && OriginStrike.fair(caster, live)) {
         OriginStrike.hitFresh(live, HollowMarkEvents.greyFor(caster, level), bendRelease(b.rank));
         if (!live.isInvisible()) {
            Fx.burst(level, Fx.glint(8381416), mid(live), 10, 0.3D, 0.06D);
         }

      }
   }

   private static void unbend(HollowDeepFavors.Bubble b, ServerPlayer caster) {
      ServerLevel level = b.level;
      List<LivingEntity> released = new ArrayList();

      for(UUID eid : new ArrayList<>(b.held.keySet())) {
         heldIn.remove(eid);
         Entity raw = level.getEntity(eid);
         if (raw != null) {
            OriginLooks.held(level, raw, 0);
            if (raw instanceof Player) {
               heldGrace.put(eid, level.getGameTime() + 120L);
            }

            if (raw instanceof LivingEntity) {
               LivingEntity live_0 = (LivingEntity)raw;
               released.add(live_0);
            }
         }
      }

      b.held.clear();
      b.heldUntil.clear();

      for(Map.Entry<UUID, Object[]> s : b.shots.entrySet()) {
         Entity var13 = level.getEntity((UUID)s.getKey());
         if (var13 instanceof Projectile shot) {
            shot.setDeltaMovement((Vec3)(s.getValue())[0]);
            shot.setNoGravity((Boolean)(s.getValue())[1]);
            shot.removeTag("unrp.hm_held_shot");
            shot.removeTag("unrp.hm_held_shot_ng");
            shot.hurtMarked = true;
         }
      }

      b.shots.clear();
      b.freed.clear();

      for(LivingEntity live : released) {
         releaseBlow(b, caster, level, live);
      }

      Spectacle.shockwave(level, 8381416, b.center, b.radius * 0.6D);
      level.playSound((Player)null, BlockPos.containing(b.center), CastFxEvents.sound("minecraft:block.bell.resonate"), SoundSource.PLAYERS, 0.9F, 0.5F);
      if (caster != null) {
         tell(caster, "§7The hour runs on.");
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onHeldBlow(LivingAttackEvent event) {
      Entity src = event.getSource().getEntity();
      if (src != null && heldIn.containsKey(src.getUUID())) {
         event.setCanceled(true);
      }

   }

   private static boolean ally(ServerPlayer caster, ServerPlayer p) {
      return p != caster && p.isAlive() && !p.isSpectator() && (com.myriam.unrp.social.SocialService.allied(p, caster) || com.myriam.unrp.social.SocialService.sameGroup(caster.server, caster.getUUID(), p.getUUID()));
   }

   private static List<String> charmsWorn(ServerPlayer player) {
      List<String> out = new ArrayList();

      for(String kind : BoneCharmItem.KINDS) {
         if (player.getTags().contains("unrp.hm_charm." + kind)) {
            out.add(kind);
         }
      }

      return out;
   }

   private static MobEffect lentEffect(String kind) {
      MobEffect var10000;
      switch (kind) {
         case "swift":
         case "grave_stride":
            var10000 = MobEffects.MOVEMENT_SPEED;
            break;
         case "vigor":
            var10000 = MobEffects.HEALTH_BOOST;
            break;
         case "ward":
         case "marrow_ward":
            var10000 = MobEffects.DAMAGE_RESISTANCE;
            break;
         case "reach":
            var10000 = MobEffects.JUMP;
            break;
         case "bone_greed":
            var10000 = MobEffects.ABSORPTION;
            break;
         default:
            var10000 = MobEffects.NIGHT_VISION;
      }

      return var10000;
   }

   public static boolean lendable(ServerPlayer p, String kind) {
      MobEffect effect = lentEffect(kind);
      return effect != MobEffects.HEALTH_BOOST && effect != MobEffects.ABSORPTION || !p.hasEffect(effect);
   }

   private static boolean lend(ServerPlayer p, String kind, int ticks) {
      if (!lendable(p, kind)) {
         return false;
      } else {
         p.addEffect(new MobEffectInstance(lentEffect(kind), ticks, kind.equals("grave_stride") ? 1 : 0, false, false, true));
         return true;
      }
   }

   private static boolean sameEffect(List<String> given, String kind) {
      for(String g : given) {
         if (lentEffect(g) == lentEffect(kind)) {
            return true;
         }
      }

      return false;
   }

   private static String charmName(String kind) {
      return BoneCharmItem.KIND_NAME[BoneCharmItem.indexOf(kind)];
   }

   private static void doGift(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 13);
      if (r < 1) {
         tell(player, "§8Void Gift isn't yours yet.");
      } else {
         List<String> charms = charmsWorn(player);
         if (charms.isEmpty()) {
            tell(player, "§8You wear no charm to give.");
         } else {
            ServerPlayer ally = aimed(player, level, ServerPlayer.class, giftReach(r), (p) -> ally(player, p));
            if (ally == null) {
               double best = 6.0D;

               for(ServerPlayer p_0 : level.players()) {
                  double dist = (double)p_0.distanceTo(player);
                  if (dist <= best && ally(player, p_0)) {
                     best = dist;
                     ally = p_0;
                  }
               }
            }

            if (ally == null) {
               tell(player, "§8No ally near enough to take it.");
            } else {
               int ticks = giftTicks(r);
               int count = r >= HollowMarkEvents.MILE_AT[13][0] ? 2 : 1;
               List<ServerPlayer> takers = new ArrayList();
               takers.add(ally);
               if (r >= HollowMarkEvents.MILE_AT[13][1]) {
                  for(ServerPlayer p : level.players()) {
                     if (p != ally && (double)p.distanceTo(ally) <= 4.0D && ally(player, p)) {
                        takers.add(p);
                     }
                  }
               }

               int turn = giftTurn.getOrDefault(player.getUUID(), 0);
               List<String> given = new ArrayList();
               int steps = 0;

               while(steps < charms.size() && given.size() < count) {
                  String kind_0 = (String)charms.get((turn + steps) % charms.size());
                  ++steps;
                  if (!sameEffect(given, kind_0)) {
                     for(ServerPlayer taker_0 : takers) {
                        if (lendable(taker_0, kind_0)) {
                           given.add(kind_0);
                           break;
                        }
                     }
                  }
               }

               if (given.isEmpty()) {
                  tell(player, "§8" + (takers.size() > 1 ? "Everyone there" : ally.getName().getString()) + " already has what you'd give. No aura spent.");
               } else if (HollowMarkEvents.payFor(player, 30)) {
                  giftTurn.put(player.getUUID(), turn + steps);
                  List<String> names = new ArrayList();

                  for(String kind_1 : given) {
                     names.add(charmName(kind_1));
                  }

                  String what = String.join(" and ", names);
                  int reached = 0;
                  ServerPlayer first = null;

                  for(ServerPlayer taker : takers) {
                     List<String> got = new ArrayList();

                     for(String kind : given) {
                        if (lend(taker, kind, ticks)) {
                           got.add(charmName(kind));
                        }
                     }

                     if (!got.isEmpty()) {
                        ++reached;
                        if (first == null) {
                           first = taker;
                        }

                        if (!taker.isInvisible()) {
                           Fx.ring(level, 8381416, taker.position().add(0.0D, 0.1D, 0.0D), 0.9D, 14);
                           Fx.burst(level, Fx.glint(15262938), mid(taker), 10, 0.3D, 0.03D);
                        }

                        if (!taker.isInvisible() && !player.isInvisible()) {
                           Fx.line(level, Fx.glint(8381416), mid(player), mid(taker), 0.5D, 0.02D, 0.0D);
                        }

                        tell(taker, "§b" + player.getName().getString() + " §8lends you §7" + String.join(" and ", got) + " §8for " + secs(ticks) + "s");
                     }
                  }

                  level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.chime"), SoundSource.PLAYERS, 0.9F, 0.8F);
                  tell(player, "§bVOID GIFT§8: " + what + " lent to " + (reached <= 1 && first != null ? first.getName().getString() : reached + " allies") + " for " + secs(ticks) + "s");
               }
            }
         }
      }
   }

   private static void doThrall(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 7);
      if (r < 1) {
         tell(player, "§8Thrall isn't yours yet.");
      } else {
         Mob target = aimed(player, level, Mob.class, thrallReach(r), (e) -> !doppelKnown(e));
         if (target == null) {
            tell(player, "§8Nothing there to bend.");
         } else {
            HollowDeepFavors.Thrall had = (HollowDeepFavors.Thrall)thralls.get(target.getUUID());
            String why = null;
            if (had != null && !had.owner.equals(player.getUUID())) {
               why = "It already kneels to another.";
            } else if (TimeWeft.everpresent(target)) {
               why = "It stands outside the grey.";
            } else if (typeOf(target).startsWith("unrp:")) {
               why = "That will isn't the grey's to take.";
            } else if (boss(target)) {
               why = "Too vast to bend.";
            } else if (!(target instanceof Animal) && (r < HollowMarkEvents.MILE_AT[7][0] || !humanoid(target))) {
               why = r < HollowMarkEvents.MILE_AT[7][0] ? "Only beasts kneel to you yet." : "Only beasts, men and the walking dead kneel.";
            } else if (had == null && !OriginStrike.fair(player, target)) {
               why = "That one already walks with you.";
            }

            if (why != null) {
               tell(player, "§8" + why);
            } else if (HollowMarkEvents.payFor(player, 24)) {
               long now = level.getGameTime();
               if (had == null) {
                  List<UUID> mine = thrallsOf(player.getUUID());
                  int cap = thrallCap(r);

                  for(int i = 0; i <= mine.size() - cap; ++i) {
                     HollowDeepFavors.Thrall old = (HollowDeepFavors.Thrall)thralls.remove(mine.get(i));
                     if (old != null) {
                        Entity var13 = old.level.getEntity((UUID)mine.get(i));
                        if (var13 instanceof Mob) {
                           Mob gone = (Mob)var13;
                           loosen(player, gone, old.level, "§7An older thrall's eyes clear.");
                        }
                     }
                  }

                  thralls.put(target.getUUID(), new HollowDeepFavors.Thrall(player.getUUID(), level, r, now, now + (long)thrallTicks(r)));
               } else {
                  had.until = now + (long)thrallTicks(r);
                  had.rank = r;
               }

               target.setTarget((LivingEntity)null);
               target.getNavigation().stop();
               CastFxEvents.pose(player, "hm_grasp", 20);
               Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.6D)).add(0.0D, -0.3D, 0.0D);
               Vec3 neck = target.position().add(0.0D, (double)target.getBbHeight() * 0.8D, 0.0D);
               if (!target.isInvisible()) {
                  Fx.line(level, Fx.ember(6253168), hand, neck, 0.3D, 0.04D, 0.0D);
                  Fx.line(level, Fx.glint(8381416), hand, neck, 0.6D, 0.02D, 0.0D);
                  Fx.ring(level, 8381416, neck, 0.35D + (double)target.getBbWidth() * 0.5D, 14);
                  Fx.burst(level, Fx.ember(10133670), mid(target), 22, 0.4D, 0.04D);
                  Fx.burst(level, Fx.glint(8381416), neck, 8, 0.2D, 0.03D);
                  Spectacle.shockwave(level, 6253168, target.position(), 1.6D + (double)target.getBbWidth());
               }

               level.playSound((Player)null, target.blockPosition(), CastFxEvents.sound("minecraft:entity.evoker.prepare_wololo"), SoundSource.PLAYERS, 0.7F, 0.6F);
               level.playSound((Player)null, target.blockPosition(), CastFxEvents.sound("minecraft:block.chain.place"), SoundSource.PLAYERS, 0.8F, 0.7F);
               tell(player, "§bTHRALL: §8" + nameOf(target) + " kneels for " + secs(thrallTicks(r)) + "s §8(" + thrallsOf(player.getUUID()).size() + "/" + thrallCap(r) + ")");
            }
         }
      }
   }

   private static boolean boss(Entity e) {
      String id = typeOf(e);
      return id.equals("minecraft:ender_dragon") || id.equals("minecraft:wither") || id.equals("minecraft:warden") || id.equals("minecraft:elder_guardian");
   }

   private static boolean vast(Entity e) {
      String id = typeOf(e);
      return id.equals("minecraft:ender_dragon") || id.equals("minecraft:wither");
   }

   private static List<UUID> thrallsOf(UUID owner) {
      List<UUID> out = new ArrayList();

      for(Map.Entry<UUID, HollowDeepFavors.Thrall> e : thralls.entrySet()) {
         if (((HollowDeepFavors.Thrall)e.getValue()).owner.equals(owner)) {
            out.add((UUID)e.getKey());
         }
      }

      return out;
   }

   private static void loosen(ServerPlayer owner, Mob mob, ServerLevel level, String msg) {
      mob.setTarget((LivingEntity)null);
      mob.getNavigation().stop();
      if (!mob.isInvisible()) {
         Fx.burst(level, Fx.ember(10133670), mid(mob), 14, 0.35D, 0.03D);
         Fx.ring(level, 6253168, mob.getX(), mob.getY() + 0.1D, mob.getZ(), 0.6D + (double)mob.getBbWidth(), 10);
      }

      level.playSound((Player)null, mob.blockPosition(), CastFxEvents.sound("minecraft:block.chain.break"), SoundSource.PLAYERS, 0.6F, 0.8F);
      if (owner != null && msg != null) {
         tell(owner, msg);
      }

      if (owner != null) {
         long[] seeing = (long[])eyes.get(owner.getUUID());
         if (seeing != null && seeing[1] == (long)mob.getId()) {
            eyes.remove(owner.getUUID());
            OriginLooks.eyes(owner, -1, 0);
         }
      }

   }

   private static void borrowEyes(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 7);
      if (r < 1) {
         tell(player, "§8Thrall isn't yours yet.");
      } else {
         Mob best = null;
         double bestD = Double.MAX_VALUE;

         for(UUID id : thrallsOf(player.getUUID())) {
            HollowDeepFavors.Thrall t = (HollowDeepFavors.Thrall)thralls.get(id);
            if (t.level == level) {
               Entity d_0 = level.getEntity(id);
               if (d_0 instanceof Mob) {
                  Mob mob = (Mob)d_0;
                  if (mob.isAlive()) {
                     double d = (double)mob.distanceTo(player);
                     if (d < bestD) {
                        bestD = d;
                        best = mob;
                     }
                  }
               }
            }
         }

         if (best == null) {
            tell(player, "§8No thrall to see through.");
         } else if (!player.onGround()) {
            tell(player, "§8Stand on solid ground to look away from yourself.");
         } else if (HollowMarkEvents.payFor(player, 6)) {
            int ticks = eyeTicks(r);
            eyes.put(player.getUUID(), new long[]{level.getGameTime() + (long)ticks, (long)best.getId()});
            OriginLooks.eyes(player, best.getId(), ticks);
            if (!best.isInvisible()) {
               Fx.burst(level, Fx.glint(8381416), best.getEyePosition(), 10, 0.15D, 0.02D);
            }

            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.illusioner.cast_spell"), SoundSource.PLAYERS, 0.6F, 1.4F);
            tell(player, "§bBORROWED EYES§8: through the " + nameOf(best) + ", " + secs(ticks) + "s");
         }
      }
   }

   private static void tickThralls(MinecraftServer server) {
      if (!thralls.isEmpty()) {
         Iterator<Map.Entry<UUID, HollowDeepFavors.Thrall>> it = thralls.entrySet().iterator();

         while(it.hasNext()) {
            Map.Entry<UUID, HollowDeepFavors.Thrall> e = (Map.Entry)it.next();
            HollowDeepFavors.Thrall t = (HollowDeepFavors.Thrall)e.getValue();
            ServerPlayer owner = server.getPlayerList().getPlayer(t.owner);
            Entity raw = t.level.getEntity((UUID)e.getKey());
            if (raw instanceof Mob) {
               Mob mob = (Mob)raw;
               if (mob.isAlive()) {
                  long now = t.level.getGameTime();
                  if (owner != null && HollowMarkEvents.holdsMark(owner) && owner.level() == t.level && !owner.isDeadOrDying() && now < t.until && !((double)mob.distanceTo(owner) > 48.0D) && !TimeWeft.everpresent(mob)) {
                     drive(owner, mob, t, t.level, now);
                  } else {
                     it.remove();
                     loosen(owner, mob, t.level, owner == null ? null : "§7Your thrall's eyes clear.");
                  }
                  continue;
               }
            }

            it.remove();
         }

      }
   }

   private static void drive(ServerPlayer owner, Mob mob, HollowDeepFavors.Thrall t, ServerLevel level, long now) {
      if (!stone(mob) && !bendHeld(mob)) {
         if ((now - t.bound) % 10L == 0L) {
            LivingEntity v_0 = pickVictim(owner, mob, level, now);
            t.victim = v_0 == null ? null : v_0.getUUID();
            if (v_0 != null) {
               mob.setTarget(v_0);
               mob.getNavigation().moveTo(v_0, 1.25D);
            } else {
               mob.setTarget((LivingEntity)null);
               double d = (double)mob.distanceTo(owner);
               if (d > 28.0D) {
                  mob.teleportTo(owner.getX(), owner.getY(), owner.getZ());
                  mob.getNavigation().stop();
               } else if (d > 5.0D) {
                  mob.getNavigation().moveTo(owner, 1.1D);
               } else {
                  mob.getNavigation().stop();
               }
            }

            if (!mob.isInvisible() && !shrouded(owner)) {
               Fx.ring(level, 8381416, mob.getX(), mob.getY() + 0.06D, mob.getZ(), 0.45D + (double)mob.getBbWidth() * 0.6D, 12);
            }
         }

         if ((now - t.bound) % 40L == 0L) {
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false, false));
         }

         Entity rawV = t.victim == null ? null : level.getEntity(t.victim);
         if (rawV instanceof LivingEntity) {
            LivingEntity v = (LivingEntity)rawV;
            if (v.isAlive() && now >= t.nextBite) {
               double reach = (double)mob.getBbWidth() * 0.5D + (double)v.getBbWidth() * 0.5D + 1.4D;
               if (!((double)mob.distanceTo(v) > reach) && mob.hasLineOfSight(v)) {
                  t.nextBite = now + (long)thrallGap(t.rank);
                  mob.swing(InteractionHand.MAIN_HAND);
                  OriginStrike.hitFresh(v, level.damageSources().indirectMagic(mob, owner), thrallBite(t.rank));
                  bitten(level, mob, v);
                  return;
               }

               return;
            }
         }

      }
   }

   private static void bitten(ServerLevel level, Entity mob, LivingEntity v) {
      if (!v.isInvisible()) {
         Vec3 at = mid(v).add(0.0D, (double)v.getBbHeight() * 0.2D, 0.0D);
         Spectacle.slash(level, Fx.ember(6253168), at, 0.6D, (double)mob.getYRot());
         Fx.burst(level, Fx.ember(15262938), at, 8, 0.25D, 0.05D);
         Fx.burst(level, Fx.glint(8381416), at, 3, 0.15D, 0.03D);
         level.playSound((Player)null, v.blockPosition(), CastFxEvents.sound("minecraft:entity.player.attack.crit"), SoundSource.PLAYERS, 0.5F, 0.7F);
      }
   }

   private static LivingEntity recent(Object[] mark, ServerLevel level, long now) {
      if (mark != null && now - ((Number)mark[1]).longValue() <= 160L) {
         Entity var5 = level.getEntity((UUID)mark[0]);
         LivingEntity var10000;
         if (var5 instanceof LivingEntity) {
            LivingEntity live = (LivingEntity)var5;
            var10000 = live;
         } else {
            var10000 = null;
         }

         return var10000;
      } else {
         return null;
      }
   }

   private static boolean prey(ServerPlayer owner, Mob mob, LivingEntity v) {
      if (v != null && v != mob && v != owner && v.isAlive() && v.level() == mob.level()) {
         HollowDeepFavors.Thrall other = (HollowDeepFavors.Thrall)thralls.get(v.getUUID());
         if (other != null && other.owner.equals(owner.getUUID())) {
            return false;
         } else {
            return OriginStrike.fair(owner, v) && (double)v.distanceTo(mob) <= 24.0D;
         }
      } else {
         return false;
      }
   }

   private static LivingEntity pickVictim(ServerPlayer owner, Mob mob, ServerLevel level, long now) {
      LivingEntity v = recent(lastStruck.get(owner.getUUID()), level, now);
      if (prey(owner, mob, v)) {
         return v;
      } else {
         v = recent(lastStriker.get(owner.getUUID()), level, now);
         if (prey(owner, mob, v)) {
            return v;
         } else {
            LivingEntity best = null;
            double bestD = 12.0D;

            for(Mob m : level.getEntitiesOfClass(Mob.class, owner.getBoundingBox().inflate(12.0D), (mx) -> mx.isAlive() && mx.getTarget() == owner)) {
               double d = (double)m.distanceTo(owner);
               if (d < bestD && prey(owner, mob, m)) {
                  bestD = d;
                  best = m;
               }
            }

            return best;
         }
      }
   }

   @SubscribeEvent
   public static void onRemember(LivingHurtEvent event) {
      if (!thralls.isEmpty()) {
         Level hurt = event.getEntity().level();
         if (hurt instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)hurt;
            LivingEntity var8 = event.getEntity();
            Entity src = event.getSource().getEntity();
            long now = level.getGameTime();
            if (src instanceof ServerPlayer) {
               ServerPlayer owner_0 = (ServerPlayer)src;
               if (owner_0 != var8 && !thralls.containsKey(var8.getUUID())) {
                  lastStruck.put(owner_0.getUUID(), new Object[]{var8.getUUID(), now});
               }
            }

            if (var8 instanceof ServerPlayer) {
               ServerPlayer owner = (ServerPlayer)var8;
               if (src instanceof LivingEntity) {
                  LivingEntity attacker = (LivingEntity)src;
                  if (attacker != owner && !servesOwner(attacker, owner)) {
                     lastStriker.put(owner.getUUID(), new Object[]{attacker.getUUID(), now});
                  }
               }
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onEyesBroken(LivingHurtEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (eyes.containsKey(player.getUUID())) {
            eyes.remove(player.getUUID());
            OriginLooks.eyes(player, -1, 0);
            tell(player, "§8The pain drags you back into your own eyes.");
            return;
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onThrallBlow(LivingAttackEvent event) {
      Entity src = event.getSource().getEntity();
      HollowDeepFavors.Thrall t = src == null ? null : (HollowDeepFavors.Thrall)thralls.get(src.getUUID());
      if (t != null) {
         event.setCanceled(true);
         LivingEntity victim = event.getEntity();
         Level owner_0 = victim.level();
         if (owner_0 instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)owner_0;
            if (t.victim != null && t.victim.equals(victim.getUUID())) {
               if (!bendHeld(src) && !stone(src)) {
                  ServerPlayer owner = level.getServer().getPlayerList().getPlayer(t.owner);
                  long now = level.getGameTime();
                  if (owner != null && OriginStrike.fair(owner, victim) && now >= t.nextBite) {
                     t.nextBite = now + (long)thrallGap(t.rank);
                     Entity direct = event.getSource().getDirectEntity();
                     OriginStrike.hitFresh(victim, level.damageSources().indirectMagic(direct == null ? src : direct, owner), Math.max(event.getAmount(), thrallBite(t.rank)));
                     bitten(level, src, victim);
                     return;
                  }

                  return;
               }

               return;
            }
         }

      }
   }

   @SubscribeEvent
   public static void onTargetChange(LivingChangeTargetEvent event) {
      LivingEntity who = event.getEntity();
      LivingEntity next = event.getNewTarget();
      if (next != null) {
         if (!stones.containsKey(who.getUUID()) && !heldIn.containsKey(who.getUUID())) {
            HollowDeepFavors.Thrall t = (HollowDeepFavors.Thrall)thralls.get(who.getUUID());
            if (t == null || t.victim != null && t.victim.equals(next.getUUID())) {
               if (next instanceof ServerPlayer) {
                  ServerPlayer sp = (ServerPlayer)next;
                  if (shrouded(sp) && (double)who.distanceTo(sp) > 2.5D) {
                     event.setCanceled(true);
                  }
               }

            } else {
               event.setCanceled(true);
            }
         } else {
            event.setCanceled(true);
         }
      }
   }

   private static void doSympathy(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 8);
      if (r < 1) {
         tell(player, "§8Sympathy isn't yours yet.");
      } else {
         LivingEntity target = aimed(player, level, LivingEntity.class, sympathyReach(r), (e) -> OriginStrike.foe(player, e));
         if (target == null) {
            tell(player, "§8No foe there to thread.");
         } else if (TimeWeft.everpresent(target)) {
            tell(player, "§8It stands outside the grey.");
         } else if (HollowMarkEvents.payFor(player, 16)) {
            long now = level.getGameTime();
            LinkedHashMap<UUID, Long> mine = (LinkedHashMap)links.computeIfAbsent(player.getUUID(), (k) -> new LinkedHashMap());
            mine.remove(target.getUUID());
            int cap = sympathyCap(r);

            while(mine.size() >= cap) {
               UUID oldest = (UUID)mine.keySet().iterator().next();
               mine.remove(oldest);
            }

            mine.put(target.getUUID(), now + (long)sympathyTicks(r));
            CastFxEvents.pose(player, "hm_gaze", 12);
            Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.6D)).add(0.0D, -0.3D, 0.0D);
            if (!target.isInvisible()) {
               Fx.line(level, Fx.glint(8381416), hand, mid(target), 0.45D, 0.02D, 0.0D);
               Fx.line(level, Fx.ember(15262938), hand, mid(target), 0.7D, 0.05D, 0.0D);
               Fx.ring(level, 8381416, target.getX(), target.getY() + (double)target.getBbHeight() + 0.25D, target.getZ(), 0.4D, 12);
               Fx.burst(level, Fx.ember(8381416), mid(target), 10, 0.3D, 0.03D);
            }

            threads(level, mine, now);
            level.playSound((Player)null, target.blockPosition(), CastFxEvents.sound("minecraft:block.chain.hit"), SoundSource.PLAYERS, 0.8F, 0.6F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.chime"), SoundSource.PLAYERS, 0.7F, 0.5F);
            tell(player, "§bSYMPATHY: §8" + mine.size() + "/" + cap + " threaded, " + Math.round(sympathyShare(r) * 100.0F) + "% of every wound shared");
         }
      }
   }

   private static void threads(ServerLevel level, LinkedHashMap<UUID, Long> mine, long now) {
      Vec3 prev = null;
      Vec3 first = null;

      for(Map.Entry<UUID, Long> e : mine.entrySet()) {
         if (e.getValue() >= now) {
            Entity at_0 = level.getEntity((UUID)e.getKey());
            if (at_0 instanceof LivingEntity) {
               LivingEntity live = (LivingEntity)at_0;
               if (live.isAlive() && !live.isInvisible()) {
                  Vec3 at = live.position().add(0.0D, (double)live.getBbHeight() + 0.25D, 0.0D);
                  Fx.ring(level, 8381416, at, 0.3D, 8);
                  if (prev != null) {
                     Fx.line(level, Fx.glint(8381416), prev, at, 0.8D, 0.02D, 0.0D);
                  }

                  if (first == null) {
                     first = at;
                  }

                  prev = at;
               }
            }
         }
      }

      if (first != null && prev != null && first != prev && mine.size() > 2) {
         Fx.line(level, Fx.ember(6253168), prev, first, 1.2D, 0.03D, 0.0D);
      }

   }

   private static void tickLinks(MinecraftServer server) {
      if (!links.isEmpty()) {
         Iterator<Map.Entry<UUID, LinkedHashMap<UUID, Long>>> it = links.entrySet().iterator();

         while(it.hasNext()) {
            Map.Entry<UUID, LinkedHashMap<UUID, Long>> e = (Map.Entry)it.next();
            ServerPlayer owner = server.getPlayerList().getPlayer((UUID)e.getKey());
            if (owner != null && HollowMarkEvents.holdsMark(owner)) {
               ServerLevel level = owner.serverLevel();
               long now = level.getGameTime();
               LinkedHashMap<UUID, Long> mine = (LinkedHashMap)e.getValue();
               mine.entrySet().removeIf((l) -> {
                  if (l.getValue() >= now) {
                     Entity patt66988$temp = level.getEntity((UUID)l.getKey());
                     if (patt66988$temp instanceof LivingEntity) {
                        LivingEntity live = (LivingEntity)patt66988$temp;
                        if (live.isAlive() && !((double)live.distanceTo(owner) > 96.0D)) {
                           return false;
                        }
                     }
                  }

                  return true;
               });
               if (mine.isEmpty()) {
                  it.remove();
               } else if (now % 10L == 0L) {
                  threads(level, mine, now);
               }
            } else {
               it.remove();
            }
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void onSympathy(LivingDamageEvent event) {
      if (!links.isEmpty()) {
         Level hurt = event.getEntity().level();
         if (hurt instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)hurt;
            LivingEntity var23 = event.getEntity();
            float amount = event.getAmount();
            if (amount < 0.25F) {
               return;
            }

            UUID hid = var23.getUUID();
            long now = level.getGameTime();

            for(Map.Entry<UUID, LinkedHashMap<UUID, Long>> e : new ArrayList<>(links.entrySet())) {
               UUID ownerId = (UUID)e.getKey();
               LinkedHashMap<UUID, Long> mine = (LinkedHashMap)e.getValue();
               Long until = (Long)mine.get(hid);
               if (until != null && until >= now && !echoing.contains(ownerId)) {
                  ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
                  if (owner != null && HollowMarkEvents.holdsMark(owner) && owner.level() == level) {
                     int r = rank(owner, 8);
                     float echo = Math.min(echoCap(r), amount * sympathyShare(r));
                     if (!(echo < 0.25F)) {
                        echoing.add(ownerId);

                        try {
                           boolean any = false;

                           for(Map.Entry<UUID, Long> l : new ArrayList<>(mine.entrySet())) {
                              if (!((UUID)l.getKey()).equals(hid) && l.getValue() >= now) {
                                 Entity var19 = level.getEntity((UUID)l.getKey());
                                 if (var19 instanceof LivingEntity) {
                                    LivingEntity live = (LivingEntity)var19;
                                    if (live.isAlive() && !((double)live.distanceTo(var23) > 64.0D) && OriginStrike.fair(owner, live)) {
                                       OriginStrike.hitFresh(live, HollowMarkEvents.greyFor(owner, level), echo);
                                       if (r >= HollowMarkEvents.MILE_AT[8][1] && !TimeWeft.everpresent(live)) {
                                          live.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, true, true));
                                       }

                                       if (!var23.isInvisible() && !live.isInvisible()) {
                                          Fx.line(level, Fx.ember(8381416), mid(var23), mid(live), 0.5D, 0.03D, 0.0D);
                                       }

                                       if (!live.isInvisible()) {
                                          Fx.burst(level, Fx.ember(15262938), mid(live), 6, 0.2D, 0.04D);
                                       }

                                       any = true;
                                    }
                                 }
                              }
                           }

                           if (any) {
                              level.playSound((Player)null, var23.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.resonate"), SoundSource.PLAYERS, 0.5F, 0.6F);
                           }
                        } finally {
                           echoing.remove(ownerId);
                        }
                     }
                  }
               }
            }

            return;
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onKnell(LivingDeathEvent event) {
      LivingEntity dead = event.getEntity();
      Level did = dead.level();
      if (did instanceof ServerLevel level) {
         UUID var20 = dead.getUUID();
         HollowDeepFavors.Thrall fallen = (HollowDeepFavors.Thrall)thralls.get(var20);
         if (fallen != null) {
            ServerPlayer owner_0 = level.getServer().getPlayerList().getPlayer(fallen.owner);
            if (owner_0 != null) {
               tell(owner_0, "§7Your thrall falls.");
            }
         }

         if (!links.isEmpty()) {
            long now = level.getGameTime();

            for(Map.Entry<UUID, LinkedHashMap<UUID, Long>> e : new ArrayList<>(links.entrySet())) {
               LinkedHashMap<UUID, Long> mine = (LinkedHashMap)e.getValue();
               Long until = (Long)mine.remove(var20);
               if (until != null && until >= now && !echoing.contains(e.getKey())) {
                  ServerPlayer owner = level.getServer().getPlayerList().getPlayer((UUID)e.getKey());
                  if (owner != null && owner.level() == level && HollowMarkEvents.holdsMark(owner)) {
                     int r = rank(owner, 8);
                     if (r >= HollowMarkEvents.MILE_AT[8][2]) {
                        echoing.add((UUID)e.getKey());

                        try {
                           for(Map.Entry<UUID, Long> l : new ArrayList<>(mine.entrySet())) {
                              if (l.getValue() >= now) {
                                 Entity var16 = level.getEntity((UUID)l.getKey());
                                 if (var16 instanceof LivingEntity) {
                                    LivingEntity live = (LivingEntity)var16;
                                    if (live.isAlive() && !((double)live.distanceTo(dead) > 64.0D) && OriginStrike.fair(owner, live)) {
                                       OriginStrike.hitFresh(live, HollowMarkEvents.greyFor(owner, level), knell(r));
                                       if (!live.isInvisible()) {
                                          Fx.shell(level, Fx.ember(8381416), mid(live), 0.9D, 18, 0.15D);
                                          Spectacle.shockwave(level, 6253168, live.position(), 1.4D);
                                       }
                                    }
                                 }
                              }
                           }
                        } finally {
                           echoing.remove(e.getKey());
                        }

                        if (!dead.isInvisible()) {
                           Fx.shell(level, Fx.ember(15262938), mid(dead), 1.2D, 24, 0.25D);
                        }

                        level.playSound((Player)null, dead.blockPosition(), CastFxEvents.sound("minecraft:block.bell.resonate"), SoundSource.PLAYERS, 0.8F, 0.6F);
                        tell(owner, "§bThe thread tolls for the fallen.");
                     }
                  }
               }
            }

         }
      }
   }

   private static void toggleShade(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 9);
      if (r < 1) {
         tell(player, "§8Shade Step isn't yours yet.");
      } else if (player.getTags().contains("unrp.hm_shade")) {
         player.removeTag("unrp.hm_shade");
         HollowDeepFavors.Shade s = (HollowDeepFavors.Shade)shades.remove(player.getUUID());
         if (s != null && s.shrouded) {
            unveil(player, level, s, (String)null);
         }

         tell(player, "§7SHADE STEP§8: the grey lets you go.");
      } else {
         player.addTag("unrp.hm_shade");
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sculk_sensor.clicking"), SoundSource.PLAYERS, 0.6F, 0.5F);
         tell(player, "§bSHADE STEP§8: stand still or walk softly and the grey hides you.");
      }
   }

   private static boolean graceHolds(ServerPlayer player, int r, int moving) {
      int[] mile = HollowMarkEvents.MILE_AT[9];
      if (r < mile[0]) {
         return false;
      } else if (player.isSprinting()) {
         return r >= mile[2] && moving <= shadeGrace(r);
      } else {
         return r >= mile[2] || moving <= shadeGrace(r);
      }
   }

   private static void tickShade(ServerPlayer player, ServerLevel level, long now, boolean armed) {
      UUID id = player.getUUID();
      if (!player.getTags().contains("unrp.hm_shade")) {
         HollowDeepFavors.Shade gone_0 = (HollowDeepFavors.Shade)shades.remove(id);
         if (gone_0 != null && gone_0.shrouded) {
            unveil(player, level, gone_0, (String)null);
         }

      } else {
         int r = rank(player, 9);
         if (r >= 1 && armed && !player.isDeadOrDying()) {
            HollowDeepFavors.Shade s = (HollowDeepFavors.Shade)shades.computeIfAbsent(id, (k) -> new HollowDeepFavors.Shade());
            double dx = player.getX() - s.x;
            double dz = player.getZ() - s.z;
            double speed = s.placed ? Math.sqrt(dx * dx + dz * dz) : 0.0D;
            s.x = player.getX();
            s.z = player.getZ();
            s.placed = true;
            if (now < s.lockUntil) {
               s.still = 0;
            } else {
               boolean slow = !player.isFallFlying() && speed <= (player.isShiftKeyDown() ? 0.13D : 0.09D);
               if (slow) {
                  s.moving = 0;
                  ++s.still;
                  if (!s.shrouded && s.still >= shadeSettle(r)) {
                     vanish(player, level, s);
                  }
               } else {
                  s.still = 0;
                  if (s.shrouded) {
                     ++s.moving;
                     if (!graceHolds(player, r, s.moving)) {
                        unveil(player, level, s, "§8The grey loses you.");
                     }
                  }
               }

               if (s.shrouded) {
                  MobEffectInstance worn = player.getEffect(MobEffects.INVISIBILITY);
                  if (worn == null || worn.getDuration() < 10) {
                     player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, false, false, false));
                  }

                  if (now % 20L == 0L && !HollowMarkEvents.payFor(player, shadeDrain(r))) {
                     unveil(player, level, s, "§8The shade starves without aura.");
                     s.lockUntil = now + 40L;
                  } else {
                     if (now % 10L == 0L) {
                        for(Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(16.0D), (m) -> m.getTarget() == player)) {
                           if ((double)mob.distanceTo(player) > 2.5D) {
                              mob.setTarget((LivingEntity)null);
                           }
                        }
                     }

                  }
               }
            }
         } else {
            player.removeTag("unrp.hm_shade");
            HollowDeepFavors.Shade gone = (HollowDeepFavors.Shade)shades.remove(id);
            if (gone != null && gone.shrouded) {
               unveil(player, level, gone, (String)null);
            }

         }
      }
   }

   private static void vanish(ServerPlayer player, ServerLevel level, HollowDeepFavors.Shade s) {
      s.shrouded = true;
      s.moving = 0;
      player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, false, false, false));
      tell(player, "§bSHROUDED");
   }

   private static void unveil(ServerPlayer player, ServerLevel level, HollowDeepFavors.Shade s, String msg) {
      s.shrouded = false;
      s.moving = 0;
      s.still = 0;
      MobEffectInstance inv = player.getEffect(MobEffects.INVISIBILITY);
      if (inv != null && inv.getDuration() <= 40) {
         player.removeEffect(MobEffects.INVISIBILITY);
      }

      Fx.burst(level, Fx.ember(10133670), player.position().add(0.0D, 1.0D, 0.0D), 8, 0.3D, 0.02D);
      if (msg != null) {
         tell(player, msg);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onShadeStrike(LivingHurtEvent event) {
      if (!shades.isEmpty()) {
         Entity s_0 = event.getSource().getEntity();
         if (s_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)s_0;
            if (player != event.getEntity()) {
               Level var7 = player.level();
               if (var7 instanceof ServerLevel) {
                  ServerLevel level = (ServerLevel)var7;
                  HollowDeepFavors.Shade s = (HollowDeepFavors.Shade)shades.get(player.getUUID());
                  if (s != null && s.shrouded && !OriginStrike.active()) {
                     int r = rank(player, 9);
                     if (r >= HollowMarkEvents.MILE_AT[9][1] && event.getSource().getDirectEntity() == player) {
                        event.setAmount(event.getAmount() * (1.0F + firstCut(r)));
                        LivingEntity victim = event.getEntity();
                        Vec3 at = victim.position().add(0.0D, (double)victim.getBbHeight() * 0.7D, 0.0D);
                        Spectacle.slash(level, Fx.ember(8381416), at, 0.8D, (double)player.getYRot());
                        Fx.burst(level, Fx.ember(10133670), at, 12, 0.3D, 0.05D);
                        level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.sculk_shrieker.hit"), SoundSource.PLAYERS, 0.7F, 1.6F);
                     }

                     unveil(player, level, s, "§8You step out of the grey.");
                     s.lockUntil = level.getGameTime() + 30L;
                     return;
                  }

                  return;
               }
            }
         }
      }

   }

   private static void doCalcify(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 10);
      if (r < 1) {
         tell(player, "§8Calcify isn't yours yet.");
      } else {
         LivingEntity target = aimed(player, level, LivingEntity.class, calcifyReach(r), (e) -> OriginStrike.foe(player, e));
         if (target == null) {
            tell(player, "§8Nothing there to still.");
         } else {
            String why = refuseStone(target, r);
            if (why != null) {
               tell(player, "§8" + why);
            } else if (HollowMarkEvents.payFor(player, 26)) {
               CastFxEvents.pose(player, "hm_slam", 14);
               int ticks = stoneTicks(r);
               int held = stoneFor(player, level, target, ticks, r);
               int spread = 0;
               if (r >= HollowMarkEvents.MILE_AT[10][2]) {
                  for(LivingEntity near : OriginStrike.foesNear(level, player, mid(target), 2.5D)) {
                     if (near != target && refuseStone(near, r) == null) {
                        stoneFor(player, level, near, ticks / 2, r);
                        ++spread;
                     }
                  }
               }

               Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.6D)).add(0.0D, -0.3D, 0.0D);
               if (!target.isInvisible()) {
                  Fx.line(level, Fx.ember(9211794), hand, mid(target), 0.25D, 0.05D, 0.0D);
                  Fx.line(level, Fx.glint(8381416), hand, mid(target), 0.7D, 0.02D, 0.0D);
               }

               level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sculk_catalyst.bloom"), SoundSource.PLAYERS, 0.6F, 0.5F);
               tell(player, "§bCALCIFY: §8" + nameOf(target) + " stilled for " + secs(held) + "s" + (spread > 0 ? ", and " + spread + " beside it" : ""));
            }
         }
      }
   }

   private static String refuseStone(LivingEntity e, int r) {
      if (TimeWeft.everpresent(e)) {
         return "It stands outside the stone.";
      } else if (timeHeld(e)) {
         return "Time already holds it.";
      } else if (PuppetEvents.puppet(e)) {
         return "Another hand holds its strings.";
      } else {
         String id = typeOf(e);
         if (!id.equals("minecraft:ender_dragon") && !id.equals("minecraft:wither")) {
            return e instanceof Player && r < HollowMarkEvents.MILE_AT[10][0] ? "The stone can't take a player yet." : null;
         } else {
            return "Too vast to still.";
         }
      }
   }

   private static int stoneFor(ServerPlayer caster, ServerLevel level, LivingEntity e, int ticks, int r) {
      String id = typeOf(e);
      if (id.equals("minecraft:warden") || id.equals("minecraft:elder_guardian")) {
         ticks = Math.max(20, ticks / 3);
      }

      long now = level.getGameTime();
      HollowDeepFavors.Stone had = (HollowDeepFavors.Stone)stones.get(e.getUUID());
      if (e instanceof Player p) {
         ticks = Math.max(20, ticks / 2);
         p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 3, false, false, true));
         p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, ticks, 1, false, false, true));
         p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 0, false, false, true));
      } else if (e instanceof Mob mob) {
         if (had == null) {
            if (mob.isNoAi()) {
               mob.addTag("unrp.hm_stone_noai");
            }

            mob.addTag("unrp.hm_stone");
            mob.setTarget((LivingEntity)null);
            mob.getNavigation().stop();
            mob.setNoAi(true);
         }
      }

      HollowDeepFavors.Stone s = had != null ? had : new HollowDeepFavors.Stone(level, e instanceof Mob);
      s.until = Math.max(s.until, now + (long)ticks);
      s.caster = caster.getUUID();
      s.rank = r;
      s.whole = true;
      stones.put(e.getUUID(), s);
      OriginLooks.stone(level, e, (int)(s.until - now));
      if (!e.isInvisible()) {
         level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), e.getX(), e.getY() + (double)e.getBbHeight() * 0.5D, e.getZ(), 24, (double)e.getBbWidth() * 0.4D, (double)e.getBbHeight() * 0.4D, (double)e.getBbWidth() * 0.4D, 0.05D);
         Fx.ring(level, 8381416, e.getX(), e.getY() + 0.08D, e.getZ(), 0.5D + (double)e.getBbWidth() * 0.7D, 14);
      }

      level.playSound((Player)null, e.blockPosition(), CastFxEvents.sound("minecraft:block.stone.place"), SoundSource.PLAYERS, 1.0F, 0.6F);
      level.playSound((Player)null, e.blockPosition(), CastFxEvents.sound("minecraft:block.deepslate.place"), SoundSource.PLAYERS, 0.8F, 0.5F);
      return (int)(s.until - now);
   }

   private static void unstone(Mob mob) {
      boolean wasNoAi = mob.getTags().contains("unrp.hm_stone_noai");
      mob.removeTag("unrp.hm_stone");
      mob.removeTag("unrp.hm_stone_noai");
      mob.setNoAi(wasNoAi || frozenByTime(mob));
   }

   private static void crack(ServerLevel level, LivingEntity live, boolean shattered) {
      if (live.isInvisible()) {
         OriginLooks.stone(level, live, 0);
      } else {
         level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), live.getX(), live.getY() + (double)live.getBbHeight() * 0.5D, live.getZ(), shattered ? 60 : 20, (double)live.getBbWidth() * 0.5D, (double)live.getBbHeight() * 0.45D, (double)live.getBbWidth() * 0.5D, shattered ? 0.25D : 0.08D);
         if (shattered) {
            Fx.burst(level, Fx.glint(8381416), mid(live), 12, 0.4D, 0.08D);
            Spectacle.shockwave(level, 6253168, live.position(), 1.6D + (double)live.getBbWidth());
         }

         level.playSound((Player)null, live.blockPosition(), CastFxEvents.sound("minecraft:block.stone.break"), SoundSource.PLAYERS, shattered ? 1.2F : 0.8F, shattered ? 0.6F : 0.9F);
         OriginLooks.stone(level, live, 0);
      }
   }

   private static void tickStones() {
      if (!stones.isEmpty()) {
         Iterator<Map.Entry<UUID, HollowDeepFavors.Stone>> it = stones.entrySet().iterator();

         while(it.hasNext()) {
            Map.Entry<UUID, HollowDeepFavors.Stone> en = (Map.Entry)it.next();
            HollowDeepFavors.Stone s = (HollowDeepFavors.Stone)en.getValue();
            Entity raw = s.level.getEntity((UUID)en.getKey());
            if (raw instanceof LivingEntity) {
               LivingEntity live = (LivingEntity)raw;
               if (live.isAlive()) {
                  long now = s.level.getGameTime();
                  if (now >= s.until) {
                     if (s.whole) {
                        crack(s.level, live, false);
                     }

                     if (live instanceof Mob) {
                        Mob mob_0 = (Mob)live;
                        unstone(mob_0);
                     }

                     it.remove();
                  } else {
                     if (live instanceof Mob) {
                        Mob mob_1 = (Mob)live;
                        Vec3 v = mob_1.getDeltaMovement();
                        mob_1.setDeltaMovement(0.0D, Math.min(v.y, 0.0D), 0.0D);
                        mob_1.hurtMarked = true;
                        if (mob_1 instanceof Creeper) {
                           Creeper creeper = (Creeper)mob_1;
                           creeper.setSwellDir(-1);
                        }
                     }

                     if (now % 20L == 0L && !live.isInvisible()) {
                        s.level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), live.getX(), live.getY() + 0.1D, live.getZ(), 3, (double)live.getBbWidth() * 0.3D, 0.05D, (double)live.getBbWidth() * 0.3D, 0.0D);
                     }
                  }
                  continue;
               }
            }

            if (raw instanceof Mob) {
               Mob mob = (Mob)raw;
               unstone(mob);
            }

            it.remove();
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onShatter(LivingHurtEvent event) {
      if (!stones.isEmpty()) {
         LivingEntity live = event.getEntity();
         HollowDeepFavors.Stone s = (HollowDeepFavors.Stone)stones.get(live.getUUID());
         if (s != null && s.whole && s.mob && s.rank >= HollowMarkEvents.MILE_AT[10][1] && !(event.getAmount() < 2.0F) && !OriginStrike.active()) {
            Entity src = event.getSource().getEntity();
            if (src != null && src.getUUID().equals(s.caster)) {
               s.whole = false;
               event.setAmount(event.getAmount() * shatter(s.rank));
               s.until = s.level.getGameTime();
               crack(s.level, live, true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide()) {
         Entity kept_0 = event.getEntity();
         if (kept_0 instanceof Mob) {
            Mob mob = (Mob)kept_0;
            if (mob.getTags().contains("unrp.hm_stone")) {
               HollowDeepFavors.Stone s = (HollowDeepFavors.Stone)stones.get(mob.getUUID());
               if (s == null || s.level != event.getLevel()) {
                  stones.remove(mob.getUUID());
                  unstone(mob);
               }
            }
         }

         kept_0 = event.getEntity();
         if (kept_0 instanceof Projectile) {
            Projectile shot = (Projectile)kept_0;
            if (shot.getTags().contains("unrp.hm_held_shot") || shot.getTags().contains("unrp.hm_held_shot_ng")) {
               boolean kept = false;

               for(HollowDeepFavors.Bubble b : bubbles.values()) {
                  kept |= b.level == event.getLevel() && b.shots.containsKey(shot.getUUID());
               }

               if (!kept) {
                  shot.setNoGravity(shot.getTags().contains("unrp.hm_held_shot_ng"));
                  shot.removeTag("unrp.hm_held_shot");
                  shot.removeTag("unrp.hm_held_shot_ng");
               }
            }
         }

      }
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent event) {
      for(Map.Entry<UUID, HollowDeepFavors.Stone> en_0 : stones.entrySet()) {
         Entity var4 = ((HollowDeepFavors.Stone)en_0.getValue()).level.getEntity((UUID)en_0.getKey());
         if (var4 instanceof Mob mob_0) {
            unstone(mob_0);
         }
      }

      for(Map.Entry<UUID, HollowDeepFavors.Thrall> en_1 : thralls.entrySet()) {
         Entity var13 = ((HollowDeepFavors.Thrall)en_1.getValue()).level.getEntity((UUID)en_1.getKey());
         if (var13 instanceof Mob mob) {
            mob.setTarget((LivingEntity)null);
         }
      }

      for(HollowDeepFavors.Bubble b : new ArrayList<>(bubbles.values())) {
         unbend(b, (ServerPlayer)null);
      }

      for(Map.Entry<UUID, HollowDeepFavors.Doppel> en : doppels.entrySet()) {
         Entity var14 = ((HollowDeepFavors.Doppel)en.getValue()).level.getEntity((UUID)en.getKey());
         if (var14 instanceof HollowDoppelEntity copy) {
            copy.discard();
         }
      }

      clearAll();
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer gone) {
         forget(gone);
      }

   }

   public static void forget(ServerPlayer player) {
      UUID id = player.getUUID();

      for(UUID copyId : doppelsOf(id)) {
         HollowDeepFavors.Doppel copy = (HollowDeepFavors.Doppel)doppels.remove(copyId);
         if (copy != null) {
            Entity t_0 = copy.level.getEntity(copyId);
            if (t_0 instanceof HollowDoppelEntity) {
               HollowDoppelEntity gone = (HollowDoppelEntity)t_0;
               gone.discard();
            }
         }
      }

      HollowDeepFavors.Bubble bent = (HollowDeepFavors.Bubble)bubbles.remove(id);
      if (bent != null) {
         unbend(bent, (ServerPlayer)null);
      }

      Long ready = (Long)bendReady.get(id);
      if (ready != null && ready <= player.level().getGameTime()) {
         bendReady.remove(id);
      }

      giftTurn.remove(id);
      heldGrace.remove(id);

      for(UUID mobId : thrallsOf(id)) {
         HollowDeepFavors.Thrall t = (HollowDeepFavors.Thrall)thralls.remove(mobId);
         if (t != null) {
            Entity var8 = t.level.getEntity(mobId);
            if (var8 instanceof Mob) {
               Mob mob = (Mob)var8;
               loosen((ServerPlayer)null, mob, t.level, (String)null);
            }
         }
      }

      links.remove(id);
      eyes.remove(id);
      shades.remove(id);
      lastStruck.remove(id);
      lastStriker.remove(id);
      player.removeTag("unrp.hm_shade");
   }

   public static void clearAll() {
      thralls.clear();
      eyes.clear();
      lastStruck.clear();
      lastStriker.clear();
      links.clear();
      echoing.clear();
      shades.clear();
      stones.clear();
      doppels.clear();
      bubbles.clear();
      heldIn.clear();
      bendReady.clear();
      giftTurn.clear();
      heldGrace.clear();
   }

   private static final class Bubble {
      final UUID caster;
      final ServerLevel level;
      final Vec3 center;
      final double radius;
      final int rank;
      final boolean stop;
      final long until;
      final Map<UUID, Vec3> held = new HashMap();
      final Map<UUID, Long> heldUntil = new HashMap();
      final Map<UUID, Object[]> shots = new HashMap();
      final Set<UUID> freed = new HashSet();

      Bubble(UUID caster, ServerLevel level, Vec3 center, double radius, int rank, boolean stop, long until) {
         this.caster = caster;
         this.level = level;
         this.center = center;
         this.radius = radius;
         this.rank = rank;
         this.stop = stop;
         this.until = until;
      }
   }

   private static final class Doppel {
      final UUID owner;
      final ServerLevel level;
      final int rank;
      final long until;
      long nextBlink;
      boolean burst;

      Doppel(UUID owner, ServerLevel level, int rank, long until, long nextBlink) {
         this.owner = owner;
         this.level = level;
         this.rank = rank;
         this.until = until;
         this.nextBlink = nextBlink;
      }
   }

   private static final class Shade {
      double x;
      double z;
      boolean placed;
      int still;
      int moving;
      boolean shrouded;
      long lockUntil;
   }

   private static final class Stone {
      final ServerLevel level;
      final boolean mob;
      long until;
      UUID caster;
      int rank;
      boolean whole = true;

      Stone(ServerLevel level, boolean mob) {
         this.level = level;
         this.mob = mob;
      }
   }

   private static final class Thrall {
      final UUID owner;
      final ServerLevel level;
      int rank;
      final long bound;
      long until;
      long nextBite;
      UUID victim;

      Thrall(UUID owner, ServerLevel level, int rank, long bound, long until) {
         this.owner = owner;
         this.level = level;
         this.rank = rank;
         this.bound = bound;
         this.until = until;
      }
   }
}
