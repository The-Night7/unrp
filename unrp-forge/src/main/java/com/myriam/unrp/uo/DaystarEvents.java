package com.myriam.unrp.uo;

import com.myriam.unrp.pass.AmRoster;
import com.myriam.unrp.deal.Fx;
import com.myriam.unrp.anim.MoveAnim;
import com.myriam.unrp.anim.MoveClipIds;
import com.myriam.unrp.deal.Spectacle;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class DaystarEvents {
   public static final int GOLD = 16757311;
   public static final int WHITE_GOLD = 16771496;
   public static final double HEAT_RADIUS = 3.0D;
   public static final float HEAT_ALLY = 0.25F;
   public static final double NIGHT_SLOW = 0.4D;
   public static final double LOW_PROFILE = 0.1D;
   public static final int QUIET = 60;
   public static final int SUN_CHARGE = 80;
   public static final double RADIATE_FROM = 0.6D;
   public static final float RADIATE = 1.5F;
   public static final float SUNDER_BASE = 10.0F;
   public static final double SUNDER_WIDTH = 4.5D;
   public static final double SUNDER_LAUNCH = 0.9D;
   public static final double SUNDER_SPEED = 1.6D;
   public static final int SUNDER_BURN = 100;
   public static final int SUNDER_STRIKE = 7;
   public static final int SKY_STRIKE = 6;
   public static final int LAND_STRIKE = 2;
   public static final int SOFT_LANDING = 10;
   public static final int BRIEF_AXE = 22;
   public static final int NOON_COST = 55;
   public static final int NOON_TICKS = 1200;
   public static final float NOON_DEALT = 0.4F;
   public static final float NOON_TAKEN = 0.95F;
   public static final int BURNOUT_TICKS = 6000;
   public static final double BURNOUT_HEALTH = -8.0D;
   public static final float AXE_SWEEP = 0.5F;
   public static final double AXE_SWEEP_REACH = 2.0D;
   public static final String HAS = "unrp.has.daystar";
   public static final String COPY = "unrp.ds_copy";
   public static final String ON = "unrp.ds_on";
   public static final String HEAT = "unrp.ds_heat";
   public static final String NOON = "unrp.ds_noon";
   public static final String BURNOUT = "unrp.ds_burnout";
   public static final String GROWN = "unrp.ds_grown";
   public static final String AXE_HELD = "unrp.ds_axe_held";
   public static final String FEED = "unrp.ds_sun_feed";
   public static final String READY = "unrp.ds_ready";
   public static final String CHARGE_POSE = "unrp.ds_sun_charge";
   public static final String EMBER_POSE = "unrp.ds_sun_held";
   public static final String FLARE_POSE = "unrp.ds_sun_flare";
   public static final String PLUNGE_POSE = "unrp.ds_plunge";
   public static final String NOON_END = "unrp_ds_noon_end";
   public static final String NOON_DAY = "unrp_ds_noon_day";
   public static final String BURN_END = "unrp_ds_burnout_end";
   private static final UUID SLOW_UUID = UUID.fromString("880f9d1d-381d-4640-8aff-4693d735dce1");
   private static final UUID PROFILE_UUID = UUID.fromString("8abdbeda-7c2a-4d8d-8104-3782e37812f9");
   private static final UUID BURN_UUID = UUID.fromString("9c8b34ad-52fe-407d-a7ec-cc24dcdca474");
   private static final Map<UUID, DaystarEvents.Day> DAYS = new HashMap();
   private static final String[] REAL_ONLY = new String[]{"unrp.ds_axe_trigger", "unrp.ds_sun_charging", "unrp.ds_sun_release", "unrp.ds_sunder_trigger", "unrp.ds_noon_trigger", "unrp.ds_noon"};
   private static boolean sweeping;

   private static boolean noncanonOn(ServerPlayer player) {
      return ConfigBridgeEvents.flag(player.server, "noncanon", false) || OriginsConfig.isModDev(player.getUUID());
   }

   private static DaystarEvents.Day day(ServerPlayer player) {
      DaystarEvents.Day d = (DaystarEvents.Day)DAYS.get(player.getUUID());
      if (d == null) {
         d = new DaystarEvents.Day();
         DAYS.put(player.getUUID(), d);
      }

      return d;
   }

   public static int dimOf(Level level) {
      if (level.dimension() == Level.NETHER) {
         return 1;
      } else if (level.dimension() == Level.END) {
         return 2;
      } else if (!level.dimensionType().hasSkyLight()) {
         return level.dimensionType().ultraWarm() ? 1 : 2;
      } else {
         return 0;
      }
   }

   public static double gauge(ServerPlayer player) {
      ServerLevel level = player.serverLevel();
      BlockPos head = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
      boolean sky = level.canSeeSky(head);
      boolean wet = sky && level.isRainingAt(head);
      return DaystarMath.gauge(dimOf(level), level.getDayTime(), sky, wet, level.isThundering(), player.isShiftKeyDown());
   }

   public static double sunScale(ServerPlayer player, String stat) {
      Set<String> tags = player.getTags();
      boolean real = tags.contains("unrp.has.daystar");
      if (tags.contains("unrp.ds_on") && (real || tags.contains("unrp.ds_copy")) && noncanonOn(player)) {
         DaystarEvents.Day d = (DaystarEvents.Day)DAYS.get(player.getUUID());
         double pf = DaystarMath.powerFactor(d == null ? gauge(player) : d.gauge);
         return real ? pf : copyScale(player, stat, pf);
      } else {
         return 1.0D;
      }
   }

   private static double copyScale(ServerPlayer player, String stat, double pf) {
      int own = peek(player, "unrp.cp_daystar_" + stat);
      int other = peek(player, "unrp.ambase_" + stat);

      for(int s = 1; s <= 54; ++s) {
         String sn = AmRoster.SLOT_SHORTS[s];
         if (sn != null && !"daystar".equals(sn)) {
            other = Math.max(other, peek(player, "unrp.cp_" + sn + "_" + stat));
         }
      }

      return DaystarMath.copyScale((double)own, (double)other, pf);
   }

   public static double gaugeOpen(ServerPlayer player) {
      ServerLevel level = player.serverLevel();
      BlockPos head = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
      boolean sky = level.canSeeSky(head);
      boolean wet = sky && level.isRainingAt(head);
      return DaystarMath.open(dimOf(level), level.getDayTime(), sky, wet, level.isThundering());
   }

   private static double sunGauge(DaystarEvents.Day d) {
      return d.mode == 1 ? d.open : d.gauge;
   }

   public static double standScale(Player p) {
      Pose pose = p.getPose();
      double stand = pose == Pose.CROUCHING ? 1.5D : (pose != Pose.SWIMMING && !p.isFallFlying() && !p.isAutoSpinAttack() ? (pose == Pose.SLEEPING ? 0.2D : 1.8D) : 0.6D);
      return (double)p.getBbHeight() / stand;
   }

   public static Vec3 heldAt(Vec3 feet, float bodyYaw, double scale, double size, int mode) {
      if (mode == 1) {
         return feet.add(0.0D, DaystarMath.flareUp(scale), 0.0D);
      } else {
         double yaw = Math.toRadians((double)bodyYaw);
         double side = 0.41D * scale;
         double back = 0.1D * scale;
         return feet.add(Math.cos(yaw) * side + Math.sin(yaw) * back, DaystarMath.heldUp(scale, size), Math.sin(yaw) * side - Math.cos(yaw) * back);
      }
   }

   public static Vec3 sunCenter(ServerPlayer player, double size) {
      return sunCenter(player, size, 0);
   }

   public static Vec3 sunCenter(ServerPlayer player, double size, int mode) {
      return heldAt(player.position(), player.yBodyRot, standScale(player), size, mode);
   }

   public static float blastRadius(float size, boolean ember) {
      return (float)DaystarMath.blast((double)size, ember);
   }

   private static boolean ally(ServerPlayer caster, LivingEntity live) {
      if (live == caster) {
         return false;
      } else {
         return com.myriam.unrp.social.SocialService.allied(live, caster) || live instanceof Player && com.myriam.unrp.social.SocialService.sameGroup(caster.server, caster.getUUID(), live.getUUID());
      }
   }

   private static boolean mount(ServerPlayer rider, Entity e) {
      return rider != null && e == rider.getVehicle();
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player tags_0 = event.player;
         if (tags_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)tags_0;
            Set<String> tags = player.getTags();
            boolean real = tags.contains("unrp.has.daystar");
            boolean copy = !real && tags.contains("unrp.ds_copy");
            if (!real && !copy) {
               if (DAYS.containsKey(player.getUUID()) || tags.contains("unrp.ds_grown") || tags.contains("unrp.ds_noon") || tags.contains("unrp.ds_burnout") || tags.contains("unrp.ds_heat") || tags.contains("unrp.ds_axe_held")) {
                  shutDown(player, true);
               }

               tags.remove("unrp.ds_off_trigger");
            } else {
               if (copy) {
                  for(String t : REAL_ONLY) {
                     tags.remove(t);
                  }
               }

               ServerLevel level = player.serverLevel();
               long now = level.getGameTime();
               DaystarEvents.Day d = day(player);
               boolean enabled = noncanonOn(player);
               if (tags.remove("unrp.ds_off_trigger") && !tags.contains("unrp.ds_on")) {
                  powerOff(player, level, d);
               }

               boolean on = tags.contains("unrp.ds_on") && enabled;
               if (d.fresh && !tags.contains("unrp.ds_sun_charging")) {
                  player.removeTag("unrp.ds_sun_feed");
                  player.removeTag("unrp.ds_sun_charge");
                  player.removeTag("unrp.ds_sun_held");
                  player.removeTag("unrp.ds_sun_flare");
               }

               if (d.fresh && d.plunge < 0L) {
                  player.removeTag("unrp.ds_plunge");
               }

               if (d.fresh || player.tickCount % 5 == 0) {
                  d.gauge = gauge(player);
                  d.open = gaugeOpen(player);
               }

               tickNoon(player, level, now);
               if (player.tickCount % 20 == 0) {
                  tickBurnout(player, now);
               }

               if (real && player.tickCount % 10 == 0) {
                  hud(player, level, now);
               }

               if (tags.remove("unrp.ds_axe_trigger")) {
                  if (!enabled) {
                     bar(player, "§cNon-canon powers are disabled on this server.");
                  } else if (hurling(d)) {
                     recall(player, d);
                  } else if (!keepBrief(player)) {
                     if (player.isShiftKeyDown() && SunsteelAxeItem.isAxe(player.getMainHandItem())) {
                        if (d.plunge < 0L) {
                           hurl(player, level, d, now);
                        }
                     } else if (findAxe(player) >= 0) {
                        dissolveAxes(player, true);
                     } else {
                        drawAxe(player, level, false);
                     }
                  }
               }

               if (d.hurled != null || d.hurlAt >= 0L) {
                  tickHurl(player, level, d, now);
               }

               if (tags.contains("unrp.ds_sun_charging")) {
                  if (enabled && tags.contains("unrp.ds_on")) {
                     chargeSun(player, level, d);
                  } else {
                     player.removeTag("unrp.ds_sun_charging");
                     if (!enabled) {
                        setScore(player, "unrp.aura", score(player, "unrp.aura") + 15);
                        bar(player, "§cNon-canon powers are disabled on this server.");
                     }

                     dropSun(player, level, d);
                  }
               }

               if (tags.remove("unrp.ds_sun_release")) {
                  releaseSun(player, level, d);
               }

               if (tags.remove("unrp.ds_sunder_trigger")) {
                  if (!enabled) {
                     setScore(player, "unrp.aura", score(player, "unrp.aura") + 30);
                     bar(player, "§cNon-canon powers are disabled on this server.");
                  } else {
                     sunder(player, level, d);
                  }
               }

               if (tags.remove("unrp.ds_noon_trigger")) {
                  if (!enabled) {
                     bar(player, "§cNon-canon powers are disabled on this server.");
                  } else {
                     highNoon(player, level, d);
                  }
               }

               if (d.plunge >= 0L) {
                  tickPlunge(player, level, d, now);
               }

               if (player.tickCount % 5 == 0 || d.fresh) {
                  tickAxe(player, d, on && real, now);
               }

               if (on) {
                  if (player.tickCount % 10 == 0) {
                     heat(player, level, d);
                     night(player, d, now, true);
                  }

                  if (player.tickCount % 20 == 0 || d.fresh) {
                     grow(player, d, d.fresh);
                  }
               } else if (player.tickCount % 10 == 0) {
                  player.removeTag("unrp.ds_heat");
                  night(player, d, now, false);
                  shrink(player, d);
               }

               d.fresh = false;
            }
         }
      }
   }

   private static void powerOff(ServerPlayer player, ServerLevel level, DaystarEvents.Day d) {
      loseHurl(player, d);
      dissolveAxes(player, false);
      dropSun(player, level, d);
      player.removeTag("unrp.ds_heat");
      player.removeTag("unrp.ds_sun_charging");
      night(player, d, level.getGameTime(), false);
      shrink(player, d);
      MoveAnim.stop(player, "ds_ready");
      MoveAnim.stop(player, "ds_charge");
      MoveAnim.stop(player, "ds_lift");
      MoveAnim.stop(player, "ds_flare");
      if (d.plunge >= 0L) {
         endPlunge(player, d);
         MoveAnim.stop(player, "ds_plunge");
      }

   }

   private static void shutDown(ServerPlayer player, boolean quiet) {
      ServerLevel level = player.serverLevel();
      DaystarEvents.Day d = (DaystarEvents.Day)DAYS.remove(player.getUUID());
      if (d == null) {
         d = new DaystarEvents.Day();
      }

      loseHurl(player, d);
      dissolveAxes(player, !quiet);
      dropSun(player, level, d);

      for(String t : new String[]{"unrp.ds_heat", "unrp.ds_noon", "unrp.ds_axe_held", "unrp.ds_sun_feed", "unrp.ds_ready", "unrp.ds_sun_charge", "unrp.ds_sun_held", "unrp.ds_sun_flare", "unrp.ds_plunge", "unrp.ds_sun_charging"}) {
         player.removeTag(t);
      }

      night(player, d, level.getGameTime(), false);
      if (player.getTags().contains("unrp.ds_grown")) {
         scale(player, "pehkui:base", 1.0D);
         scale(player, "pehkui:model_width", 1.0D);
         player.removeTag("unrp.ds_grown");
      }

      endBurnout(player);
      CompoundTag pd = player.getPersistentData();
      pd.remove("unrp_ds_noon_end");
   }

   private static void tickNoon(ServerPlayer player, ServerLevel level, long now) {
      if (player.getTags().contains("unrp.ds_noon")) {
         long end = player.getPersistentData().getLong("unrp_ds_noon_end");
         if (end > 0L && now < end) {
            if ((end - now) % 20L == 0L) {
               long secs = (end - now) / 20L;
               if (secs <= 5L || secs % 15L == 0L) {
                  bar(player, "§6HIGH NOON §f" + secs + "s");
               }
            }
         } else {
            endNoon(player, true);
         }

      }
   }

   private static void hud(ServerPlayer player, ServerLevel level, long now) {
      CompoundTag pd = player.getPersistentData();
      long noonEnd = pd.getLong("unrp_ds_noon_end");
      long burnEnd = pd.getLong("unrp_ds_burnout_end");
      int noonLeft = player.getTags().contains("unrp.ds_noon") && noonEnd > now ? (int)Math.min(1200L, noonEnd - now) : 0;
      int burnLeft = player.getTags().contains("unrp.ds_burnout") && burnEnd > now ? (int)Math.min(6000L, burnEnd - now) : 0;
      boolean used = pd.getLong("unrp_ds_noon_day") == level.getDayTime() / 24000L + 1L;
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DaystarHudPacket(noonLeft, burnLeft, used));
   }

   private static void endNoon(ServerPlayer player, boolean burn) {
      player.removeTag("unrp.ds_noon");
      player.getPersistentData().remove("unrp_ds_noon_end");
      DaystarEvents.Day d = (DaystarEvents.Day)DAYS.get(player.getUUID());
      if (d != null) {
         grow(player, d, true);
      }

      if (burn) {
         burnout(player);
      }

   }

   private static void burnout(ServerPlayer player) {
      long now = player.serverLevel().getGameTime();
      player.getPersistentData().putLong("unrp_ds_burnout_end", now + 6000L);
      player.addTag("unrp.ds_burnout");
      AttributeInstance hp = player.getAttribute(Attributes.MAX_HEALTH);
      if (hp != null && hp.getModifier(BURN_UUID) == null) {
         hp.addPermanentModifier(new AttributeModifier(BURN_UUID, "Daystar burnout", -8.0D, Operation.ADDITION));
      }

      if (player.getHealth() > player.getMaxHealth()) {
         player.setHealth(player.getMaxHealth());
      }

      bar(player, "§6Burnout. §7You lose four hearts for five minutes.");
      player.serverLevel().playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.fire.extinguish"), SoundSource.PLAYERS, 0.8F, 0.6F);
   }

   private static void tickBurnout(ServerPlayer player, long now) {
      boolean tagged = player.getTags().contains("unrp.ds_burnout");
      AttributeInstance hp = player.getAttribute(Attributes.MAX_HEALTH);
      boolean modded = hp != null && hp.getModifier(BURN_UUID) != null;
      if (tagged || modded) {
         long end = player.getPersistentData().getLong("unrp_ds_burnout_end");
         if (tagged && end > 0L && now < end) {
            if (!modded && hp != null) {
               hp.addPermanentModifier(new AttributeModifier(BURN_UUID, "Daystar burnout", -8.0D, Operation.ADDITION));
               if (player.getHealth() > player.getMaxHealth()) {
                  player.setHealth(player.getMaxHealth());
               }
            }
         } else {
            endBurnout(player);
         }

      }
   }

   private static void endBurnout(ServerPlayer player) {
      AttributeInstance hp = player.getAttribute(Attributes.MAX_HEALTH);
      if (hp != null && hp.getModifier(BURN_UUID) != null) {
         hp.removeModifier(BURN_UUID);
      }

      boolean was = player.getTags().contains("unrp.ds_burnout");
      player.removeTag("unrp.ds_burnout");
      player.getPersistentData().remove("unrp_ds_burnout_end");
      if (was) {
         bar(player, "§eThe burnout passes.");
      }

   }

   private static void highNoon(ServerPlayer player, ServerLevel level, DaystarEvents.Day d) {
      if (!player.getTags().contains("unrp.ds_noon")) {
         long dayIndex = level.getDayTime() / 24000L + 1L;
         BlockPos head = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
         boolean sky = level.dimensionType().hasSkyLight() && level.dimension() != Level.END && level.canSeeSky(head);
         boolean used = player.getPersistentData().getLong("unrp_ds_noon_day") == dayIndex;
         if (!DaystarMath.noonOpen(level.getDayTime(), sky, used)) {
            bar(player, used ? "§6High Noon comes once a game day." : "§6High Noon needs the sun overhead: 11:00 to 13:00, under open sky.");
         } else if (spendAura(player, 55)) {
            CompoundTag pd = player.getPersistentData();
            pd.putLong("unrp_ds_noon_day", dayIndex);
            pd.putLong("unrp_ds_noon_end", level.getGameTime() + 1200L);
            player.addTag("unrp.ds_noon");
            List<MobEffect> bad = new ArrayList();

            for(MobEffectInstance inst : player.getActiveEffects()) {
               if (inst.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                  bad.add(inst.getEffect());
               }
            }

            for(MobEffect e : bad) {
               player.removeEffect(e);
            }

            grow(player, d, true);
            MoveAnim.play(player, "ds_noon");
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.activate"), SoundSource.PLAYERS, 1.6F, 0.6F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.bell.resonate"), SoundSource.PLAYERS, 1.0F, 1.2F);
            Vec3 at = player.position();
            Fx.ring(level, 16757311, at.add(0.0D, 0.1D, 0.0D), 4.0D, 24);
            Fx.burst(level, Fx.flare(16771496), at.add(0.0D, (double)player.getBbHeight() * 0.6D, 0.0D), 30, 0.8D, 0.12D);
            bar(player, "§6HIGH NOON §f60 seconds");
         }
      }
   }

   private static int findAxe(ServerPlayer player) {
      Inventory inv = player.getInventory();

      for(int i = 0; i < inv.getContainerSize(); ++i) {
         if (SunsteelAxeItem.isAxe(inv.getItem(i))) {
            return i;
         }
      }

      return -1;
   }

   private static boolean keepBrief(ServerPlayer player) {
      int slot = findAxe(player);
      if (slot < 0) {
         return false;
      } else {
         CompoundTag tag = player.getInventory().getItem(slot).getTag();
         if (tag != null && tag.getBoolean("unrp_ds_brief")) {
            tag.remove("unrp_ds_brief");
            return true;
         } else {
            return false;
         }
      }
   }

   private static boolean drawAxe(ServerPlayer player, ServerLevel level, boolean brief) {
      if (findAxe(player) >= 0) {
         return true;
      } else {
         Inventory inv = player.getInventory();
         ItemStack held = inv.getSelected();
         if (!held.isEmpty()) {
            int free = inv.getFreeSlot();
            if (free < 0) {
               bar(player, "§7Your hands are full: free a slot to draw the axe.");
               return false;
            }

            inv.setItem(free, held.copy());
         }

         ItemStack axe = new ItemStack(com.myriam.unrp.item.ModItems.SUNSTEEL_AXE.get());
         CompoundTag tag = axe.getOrCreateTag();
         tag.putBoolean("unrp_ds_axe", true);
         tag.putUUID("unrp_ds_owner", player.getUUID());
         tag.putLong("unrp_ds_born", level.getGameTime());
         if (brief) {
            tag.putBoolean("unrp_ds_brief", true);
         }

         tag.putBoolean("Unbreakable", true);
         tag.putInt("HideFlags", 4);
         inv.setItem(inv.selected, axe);
         MoveAnim.play(player, "ds_draw");
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:item.trident.return"), SoundSource.PLAYERS, 0.9F, 0.7F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.fire.ambient"), SoundSource.PLAYERS, 1.0F, 1.4F);
         Fx.burst(level, Fx.flare(16771496), player.position().add(0.0D, (double)player.getBbHeight() + 0.3D, 0.0D), 12, 0.3D, 0.06D);
         return true;
      }
   }

   private static boolean dissolveAxes(ServerPlayer player, boolean message) {
      boolean any = false;
      Inventory inv = player.getInventory();

      for(int i = 0; i < inv.getContainerSize(); ++i) {
         if (SunsteelAxeItem.isAxe(inv.getItem(i))) {
            inv.setItem(i, ItemStack.EMPTY);
            any = true;
         }
      }

      player.removeTag("unrp.ds_axe_held");
      player.removeTag("unrp.ds_ready");
      if (any && message) {
         bar(player, "§7The axe dissolves back into the light.");
         Fx.burst(player.serverLevel(), Fx.flare(16757311), player.position().add(0.0D, (double)player.getBbHeight() * 0.6D, 0.0D), 8, 0.3D, 0.04D);
      }

      return any;
   }

   private static void tickAxe(ServerPlayer player, DaystarEvents.Day d, boolean on, long now) {
      boolean dry = score(player, "unrp.aura") <= 0 || player.getTags().contains("unrp.aura_depleted");
      if ((!on || dry) && loseHurl(player, d)) {
         bar(player, "§7The axe dissolves back into the light.");
      }

      int slot = findAxe(player);
      if (slot < 0) {
         player.removeTag("unrp.ds_ready");
         if (d.hurled != null) {
            player.addTag("unrp.ds_axe_held");
         } else {
            player.removeTag("unrp.ds_axe_held");
         }

      } else {
         ItemStack axe = player.getInventory().getItem(slot);
         CompoundTag tag = axe.getTag();
         boolean brief = tag != null && tag.getBoolean("unrp_ds_brief");
         long born = Math.max(tag == null ? 0L : tag.getLong("unrp_ds_born"), d.landed);
         boolean late = brief && d.plunge < 0L && now - born > 22L;
         if (on && !late && !dry) {
            if (brief) {
               player.removeTag("unrp.ds_axe_held");
            } else {
               player.addTag("unrp.ds_axe_held");
            }

            boolean inHand = SunsteelAxeItem.isAxe(player.getMainHandItem());
            if (inHand && !player.getTags().contains("unrp.ds_sun_charging")) {
               player.addTag("unrp.ds_ready");
            } else {
               player.removeTag("unrp.ds_ready");
            }

         } else {
            dissolveAxes(player, !late);
         }
      }
   }

   private static boolean hurling(DaystarEvents.Day d) {
      return d.hurled != null || d.hurlAt >= 0L;
   }

   private static void hurl(ServerPlayer player, ServerLevel level, DaystarEvents.Day d, long now) {
      if (now < d.hurlReady) {
         bar(player, "§7The axe just came back. Give it a moment before you throw it again.");
      } else if (score(player, "unrp.aura") <= 6) {
         bar(player, "§cNot enough aura (7 needed to throw the axe and keep it).");
      } else if (spendAura(player, 6)) {
         d.hurlAt = now;
         UUID id = player.getUUID();
         MoveAnim.play(player, "ds_hurl");
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.blaze.shoot"), SoundSource.PLAYERS, 0.5F, 1.6F);
         Spectacle.after(4, () -> {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(id);
            DaystarEvents.Day dd = p == null ? null : (DaystarEvents.Day)DAYS.get(id);
            if (dd != null && dd.hurlAt == now) {
               dd.hurlAt = -1L;
               if (!p.isRemoved() && p.isAlive() && p.level() == level) {
                  letGo(p, level, dd, level.getGameTime());
               }
            }
         });
      }
   }

   private static void letGo(ServerPlayer player, ServerLevel level, DaystarEvents.Day d, long now) {
      Inventory inv = player.getInventory();
      int slot = inv.selected;
      ItemStack axe = inv.getItem(slot);
      if (SunsteelAxeItem.isAxe(axe) && player.getTags().contains("unrp.ds_on")) {
         SunAxeEntity e = (SunAxeEntity)(com.myriam.unrp.entity.ModEntities.SUN_AXE.get()).create(level);
         if (e != null) {
            Vec3 look = player.getLookAngle();
            double sc = standScale(player);
            double yaw = Math.toRadians((double)player.yBodyRot);
            Vec3 fwd = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
            Vec3 right = new Vec3(-Math.cos(yaw), 0.0D, -Math.sin(yaw));
            Vec3 eye = player.getEyePosition();
            Vec3 at = player.position().add(right.scale(-0.09D * sc)).add(0.0D, 1.83D * sc, 0.0D).add(fwd.scale(1.0D * sc));
            BlockHitResult wall = level.clip(new ClipContext(eye, at, Block.COLLIDER, Fluid.NONE, player));
            if (wall.getType() == Type.BLOCK) {
               at = wall.getLocation().subtract(at.subtract(eye).normalize().scale(0.15D));
            }

            Vec3 far = eye.add(look.scale(20.4D));
            BlockHitResult sight = level.clip(new ClipContext(eye, far, Block.COLLIDER, Fluid.NONE, player));
            Vec3 dir = (sight.getType() == Type.BLOCK ? sight.getLocation() : far).subtract(at);
            if (dir.lengthSqr() < 1.0D) {
               dir = look;
            }

            inv.setItem(slot, ItemStack.EMPTY);
            e.setPos(at.x, at.y, at.z);
            e.launch(player, axe, slot, dir, (float)DaystarMath.hurlHit(11.0D, d.open), (float)d.open);
            level.addFreshEntity(e);
            d.hurled = e.getUUID();
            d.hurlSeen = now;
            d.hurlFrom = now;
            player.removeTag("unrp.ds_ready");
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:item.trident.throw"), SoundSource.PLAYERS, 1.0F, 0.7F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.fire.ambient"), SoundSource.PLAYERS, 0.8F, 1.6F);
            Fx.burst(level, Fx.flare(16757311), at, 10, 0.25D, 0.05D);
         }
      } else {
         setScore(player, "unrp.aura", score(player, "unrp.aura") + 6);
      }
   }

   private static void recall(ServerPlayer player, DaystarEvents.Day d) {
      if (d.hurled != null) {
         Entity var3 = player.serverLevel().getEntity(d.hurled);
         if (var3 instanceof SunAxeEntity) {
            SunAxeEntity axe = (SunAxeEntity)var3;
            axe.turn();
         }

      }
   }

   private static void tickHurl(ServerPlayer player, ServerLevel level, DaystarEvents.Day d, long now) {
      if (d.hurled == null) {
         if (now - d.hurlAt > 24L) {
            catchNow(player, level, d);
         }

      } else {
         Entity e = level.getEntity(d.hurled);
         if (e instanceof SunAxeEntity && e.isAlive()) {
            if (now - d.hurlFrom > 220L) {
               catchNow(player, level, d);
            } else {
               d.hurlSeen = now;
            }
         } else {
            if (now - d.hurlSeen > 20L) {
               d.hurled = null;
               d.hurlReady = now + 40L;
            }

         }
      }
   }

   private static boolean loseHurl(ServerPlayer player, DaystarEvents.Day d) {
      d.hurlAt = -1L;
      if (d.hurled == null) {
         return false;
      } else {
         boolean found = false;

         for(ServerLevel l : player.server.getAllLevels()) {
            Entity e = l.getEntity(d.hurled);
            if (e instanceof SunAxeEntity) {
               e.discard();
               found = true;
            }
         }

         d.hurled = null;
         return found;
      }
   }

   private static void catchNow(ServerPlayer player, ServerLevel level, DaystarEvents.Day d) {
      if (d.hurled == null) {
         if (d.hurlAt >= 0L) {
            setScore(player, "unrp.aura", score(player, "unrp.aura") + 6);
         }

         d.hurlAt = -1L;
      } else {
         d.hurlAt = -1L;

         for(ServerLevel l : player.server.getAllLevels()) {
            Entity var6 = l.getEntity(d.hurled);
            if (var6 instanceof SunAxeEntity) {
               SunAxeEntity axe = (SunAxeEntity)var6;
               if (putBack(player, axe.stack(), axe.slot())) {
                  level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:item.trident.return"), SoundSource.PLAYERS, 1.0F, 1.2F);
               }

               axe.discard();
            }
         }

         d.hurled = null;
         d.hurlReady = level.getGameTime() + 40L;
      }
   }

   private static boolean putBack(ServerPlayer player, ItemStack axe, int slot) {
      if (!axe.isEmpty() && SunsteelAxeItem.isAxe(axe) && findAxe(player) < 0) {
         Inventory inv = player.getInventory();
         int to;
         if (slot >= 0 && slot < inv.items.size() && ((ItemStack)inv.items.get(slot)).isEmpty()) {
            to = slot;
         } else if (inv.getSelected().isEmpty()) {
            to = inv.selected;
         } else {
            to = inv.getFreeSlot();
         }

         if (to < 0) {
            bar(player, "§7Your hands are full: the axe dissolves back into the light.");
            return false;
         } else {
            inv.setItem(to, axe);
            return true;
         }
      } else {
         return false;
      }
   }

   public static void hurlLost(ServerPlayer holder, SunAxeEntity axe) {
      if (holder != null) {
         DaystarEvents.Day d = (DaystarEvents.Day)DAYS.get(holder.getUUID());
         if (d != null && axe.getUUID().equals(d.hurled)) {
            d.hurled = null;
            d.hurlReady = holder.serverLevel().getGameTime() + 40L;
            bar(holder, "§7The axe dissolves back into the light.");
         }

      }
   }

   public static void hurlCaught(ServerPlayer holder, SunAxeEntity axe) {
      DaystarEvents.Day d = (DaystarEvents.Day)DAYS.get(holder.getUUID());
      if (d != null && axe.getUUID().equals(d.hurled)) {
         ServerLevel level = holder.serverLevel();
         d.hurled = null;
         d.hurlReady = level.getGameTime() + 40L;
         if (holder.getTags().contains("unrp.ds_on") && putBack(holder, axe.stack(), axe.slot())) {
            level.playSound((Player)null, holder.blockPosition(), CastFxEvents.sound("minecraft:item.trident.return"), SoundSource.PLAYERS, 1.0F, 1.0F);
            Fx.burst(level, Fx.flare(16771496), holder.position().add(0.0D, (double)holder.getBbHeight() * 0.6D, 0.0D), 10, 0.3D, 0.05D);
         }
      }
   }

   public static void hurlStruck(ServerLevel level, Vec3 at) {
      level.playSound((Player)null, BlockPos.containing(at), CastFxEvents.sound("minecraft:item.trident.hit_ground"), SoundSource.PLAYERS, 1.0F, 0.8F);
      level.sendParticles(ParticleTypes.LAVA, at.x, at.y, at.z, 4, 0.2D, 0.2D, 0.2D, 0.0D);
      level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 10, 0.25D, 0.25D, 0.25D, 0.04D);
   }

   public static void hurlHit(ServerLevel level, ServerPlayer holder, SunAxeEntity axe, LivingEntity e, float dmg, Vec3 dir) {
      if (!mount(holder, e) && !ally(holder, e) && OriginStrike.fair(holder, e)) {
         OriginStrike.hitFresh(e, level.damageSources().trident(axe, holder), dmg);
         if (!e.fireImmune()) {
            e.setSecondsOnFire(5);
         }

         OriginStrike.shove(e, dir.x * 0.4D, 0.15D, dir.z * 0.4D);
         level.playSound((Player)null, e.blockPosition(), CastFxEvents.sound("minecraft:item.trident.hit"), SoundSource.PLAYERS, 1.0F, 0.9F);
         level.sendParticles(ParticleTypes.FLAME, e.getX(), e.getY() + (double)e.getBbHeight() * 0.5D, e.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
      }
   }

   private static void heat(ServerPlayer player, ServerLevel level, DaystarEvents.Day d) {
      boolean noon = player.getTags().contains("unrp.ds_noon");
      boolean hot = DaystarMath.heatOn(d.gauge) && !player.isShiftKeyDown() && !player.isInWater();
      if (!hot) {
         player.removeTag("unrp.ds_heat");
      } else {
         player.addTag("unrp.ds_heat");
         double r = 3.0D * (noon ? 2.0D : 1.0D);

         for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(r))) {
            if (e != player && !mount(player, e) && e.isAlive() && !e.fireImmune() && !e.isInWater() && !((double)e.distanceTo(player) > r)) {
               if (ally(player, e)) {
                  if (player.tickCount % 40 == 0) {
                     OriginStrike.hit(e, level.damageSources().onFire(), 1.0F);
                  }
               } else if (OriginStrike.fair(player, e)) {
                  e.setRemainingFireTicks(Math.max(e.getRemainingFireTicks(), 40));
               }
            }
         }

         if (level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            melt(level, player.blockPosition(), r);
         }

      }
   }

   private static void melt(ServerLevel level, BlockPos c, double r) {
      int rr = (int)Math.ceil(r);
      boolean hot = level.dimensionType().ultraWarm();

      for(int dx = -rr; dx <= rr; ++dx) {
         for(int dz = -rr; dz <= rr; ++dz) {
            if (!((double)(dx * dx + dz * dz) > r * r)) {
               for(int dy = -1; dy <= 1; ++dy) {
                  BlockPos p = c.offset(dx, dy, dz);
                  BlockState st = level.getBlockState(p);
                  if (!st.is(Blocks.SNOW) && !st.is(Blocks.SNOW_BLOCK) && !st.is(Blocks.POWDER_SNOW)) {
                     if (!st.is(Blocks.ICE) && !st.is(Blocks.FROSTED_ICE)) {
                        continue;
                     }

                     level.setBlockAndUpdate(p, hot ? Blocks.AIR.defaultBlockState() : Blocks.WATER.defaultBlockState());
                  } else {
                     level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                  }

                  level.sendParticles(ParticleTypes.CLOUD, (double)p.getX() + 0.5D, (double)p.getY() + 0.9D, (double)p.getZ() + 0.5D, 2, 0.2D, 0.1D, 0.2D, 0.01D);
               }
            }
         }
      }

   }

   private static void night(ServerPlayer player, DaystarEvents.Day d, long now, boolean on) {
      boolean dark = on && DaystarMath.night(d.gauge);
      boolean axe = SunsteelAxeItem.isAxe(player.getMainHandItem());
      modifier(player, Attributes.ATTACK_SPEED, SLOW_UUID, "Daystar night", -0.4D, Operation.MULTIPLY_TOTAL, dark && axe);
      modifier(player, Attributes.MOVEMENT_SPEED, PROFILE_UUID, "Low Profile", 0.1D, Operation.MULTIPLY_BASE, dark && now - d.swung > 60L);
   }

   private static void modifier(ServerPlayer player, Attribute attr, UUID id, String name, double amount, AttributeModifier.Operation op, boolean want) {
      AttributeInstance inst = player.getAttribute(attr);
      if (inst != null) {
         boolean has = inst.getModifier(id) != null;
         if (want && !has) {
            inst.addTransientModifier(new AttributeModifier(id, name, amount, op));
         } else if (!want && has) {
            inst.removeModifier(id);
         }

      }
   }

   private static boolean pehkui() {
      return ModList.get().isLoaded("pehkui");
   }

   private static void scale(ServerPlayer player, String type, double value) {
      MinecraftServer server = player.getServer();
      if (server != null && pehkui()) {
         server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "scale set " + type + " " + String.format(Locale.ROOT, "%.3f", value) + " " + player.getName().getString());
      }
   }

   private static void grow(ServerPlayer player, DaystarEvents.Day d, boolean force) {
      if (pehkui()) {
         boolean noon = player.getTags().contains("unrp.ds_noon");
         double h = DaystarMath.heightFor(d.gauge, noon);
         double w = DaystarMath.widthFor(d.gauge, noon);
         if (force || !(Math.abs(h - d.height) < 0.01D) || !(Math.abs(w - d.width) < 0.01D)) {
            d.height = h;
            d.width = w;
            scale(player, "pehkui:base", h);
            scale(player, "pehkui:model_width", w);
            player.addTag("unrp.ds_grown");
         }
      }
   }

   private static void shrink(ServerPlayer player, DaystarEvents.Day d) {
      if (player.getTags().contains("unrp.ds_grown")) {
         scale(player, "pehkui:base", 1.0D);
         scale(player, "pehkui:model_width", 1.0D);
         player.removeTag("unrp.ds_grown");
         d.height = 1.0D;
         d.width = 1.0D;
      }
   }

   private static SunOrbEntity orb(ServerLevel level, DaystarEvents.Day d) {
      if (d.sun < 0) {
         return null;
      } else {
         Entity e = level.getEntity(d.sun);
         if (e instanceof SunOrbEntity) {
            SunOrbEntity s = (SunOrbEntity)e;
            if (s.isAlive()) {
               return s;
            }
         }

         return null;
      }
   }

   private static void dropSun(ServerPlayer player, ServerLevel level, DaystarEvents.Day d) {
      SunOrbEntity s = orb(level, d);
      if (s != null && s.state() == 0) {
         Fx.burst(level, Fx.flare(16757311), s.position(), 10, (double)s.size() * 0.4D, 0.05D);
         s.discard();
      }

      d.sun = -1;
      d.charge = 0;
      player.removeTag("unrp.ds_sun_feed");
      player.removeTag("unrp.ds_sun_charge");
      player.removeTag("unrp.ds_sun_held");
      player.removeTag("unrp.ds_sun_flare");
   }

   private static void chargeSun(ServerPlayer player, ServerLevel level, DaystarEvents.Day d) {
      SunOrbEntity s = orb(level, d);
      if (s == null) {
         d.mode = player.isShiftKeyDown() ? 1 : 0;
         d.ember = DaystarMath.night(d.mode == 1 ? d.open : d.gauge);
         d.charge = 0;
         d.capped = false;
         s = (SunOrbEntity)(com.myriam.unrp.entity.ModEntities.SUN_ORB.get()).create(level);
         if (s == null) {
            return;
         }

         double size0 = d.ember ? 0.7D : 1.2D;
         Vec3 c = sunCenter(player, size0, d.mode);
         s.setPos(c.x, c.y, c.z);
         s.bind(player, d.ember, (float)size0, d.mode);
         level.addFreshEntity(s);
         d.sun = s.getId();
         player.addTag(d.mode == 1 ? "unrp.ds_sun_flare" : (d.ember ? "unrp.ds_sun_held" : "unrp.ds_sun_charge"));
         player.removeTag("unrp.ds_ready");
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.ambient"), SoundSource.PLAYERS, 1.2F, d.ember ? 1.6F : 0.8F);
      }

      if (!d.ember && !d.capped && d.charge < 80) {
         if (score(player, "unrp.aura") <= 0) {
            d.capped = true;
            player.removeTag("unrp.ds_sun_feed");
            bar(player, "§6No aura left to feed it.");
         } else {
            ++d.charge;
            player.addTag("unrp.ds_sun_feed");
         }
      } else {
         player.removeTag("unrp.ds_sun_feed");
      }

      double k = d.ember ? 0.0D : (double)d.charge / 80.0D;
      double size = d.ember ? 0.7D : DaystarMath.sunSize(Math.max(sunGauge(d), 0.25D), k);
      s.grow((float)size, (float)k, sunCenter(player, size, d.mode));
      if (d.charge % 20 == 10 && !d.ember) {
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.fire.ambient"), SoundSource.PLAYERS, 0.6F + (float)k, 0.6F + 0.4F * (float)k);
      }

      if (!d.ember && k >= 0.6D) {
         radiate(player, level, d, s, k, size);
      }

   }

   private static void radiate(ServerPlayer player, ServerLevel level, DaystarEvents.Day d, SunOrbEntity s, double k, double size) {
      double kk = (k - 0.6D) / 0.4D;
      if (player.tickCount % 10 == 0) {
         double reach = size * 0.5D + 2.5D;
         Vec3 c = s.position();
         AABB box = (new AABB(c, c)).inflate(reach + 2.0D, reach + 4.0D, reach + 2.0D);

         for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e != player && !mount(player, e) && e.isAlive()) {
               Vec3 mid = e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D);
               double flat = Math.hypot(e.getX() - player.getX(), e.getZ() - player.getZ());
               if (!(mid.distanceTo(c) > reach + (double)e.getBbHeight() * 0.5D) || !(flat > size * 0.5D + 1.5D)) {
                  boolean friend = ally(player, e);
                  if (friend || OriginStrike.fair(player, e)) {
                     float dmg = (float)(1.5D * DaystarMath.powerFactor(sunGauge(d)) * (0.5D + kk) * (double)(friend ? 0.25F : 1.0F));
                     OriginStrike.hit(e, level.damageSources().indirectMagic(s, player), dmg);
                     if (!e.fireImmune()) {
                        e.setSecondsOnFire(3);
                     }
                  }
               }
            }
         }
      }

      if (k > 0.8D && player.tickCount % 4 == 0 && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
         double rs = (size * 0.5D + 1.5D) * DaystarMath.smooth01(kk) + 1.0D;
         evaporate(level, player.position().add(0.0D, -1.0D, 0.0D), rs, 64);
      }

   }

   public static int evaporate(ServerLevel level, Vec3 c, double r, int most) {
      int n = 0;
      int rr = (int)Math.ceil(r);
      BlockPos o = BlockPos.containing(c);

      for(int dy = 3; dy >= -4; --dy) {
         for(int dx = -rr; dx <= rr; ++dx) {
            for(int dz = -rr; dz <= rr; ++dz) {
               if (!((double)(dx * dx + dz * dz) > r * r)) {
                  BlockPos p = o.offset(dx, dy, dz);
                  BlockState st = level.getBlockState(p);
                  if (st.is(Blocks.WATER) && level.getFluidState(p).is(FluidTags.WATER)) {
                     level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                     if (n % 2 == 0) {
                        level.sendParticles(ParticleTypes.CLOUD, (double)p.getX() + 0.5D, (double)p.getY() + 0.6D, (double)p.getZ() + 0.5D, 2, 0.25D, 0.2D, 0.25D, 0.02D);
                     }

                     ++n;
                     if (n >= most) {
                        return n;
                     }
                  }
               }
            }
         }
      }

      return n;
   }

   private static Vec3 aimPoint(ServerPlayer player, double size, boolean ember) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      Vec3 far = eye.add(look.scale(48.0D));
      BlockHitResult hit = player.serverLevel().clip(new ClipContext(eye, far, Block.COLLIDER, Fluid.NONE, player));
      Vec3 at = hit.getType() == Type.BLOCK ? hit.getLocation() : eye.add(look.scale(24.0D));
      return ember ? at : at.add(0.0D, size * 0.3D, 0.0D);
   }

   private static void releaseSun(ServerPlayer player, ServerLevel level, DaystarEvents.Day d) {
      player.removeTag("unrp.ds_sun_feed");
      player.removeTag("unrp.ds_sun_charge");
      player.removeTag("unrp.ds_sun_held");
      player.removeTag("unrp.ds_sun_flare");
      SunOrbEntity s = orb(level, d);
      d.sun = -1;
      if (s != null && s.state() == 0) {
         boolean ember = s.ember();
         double k = ember ? 0.0D : (double)d.charge / 80.0D;
         if (s.mode() == 1) {
            float fd = (float)DaystarMath.flareDamage(Math.max(d.open, 0.25D), k, ember);
            MoveAnim.play(player, "ds_flare_burst");
            s.windUp(s.position(), 4, fd, (float)d.open);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.activate"), SoundSource.PLAYERS, 1.0F, ember ? 1.6F : 1.1F);
            d.charge = 0;
         } else {
            float dmg = (float)(ember ? 3.0D * DaystarMath.powerFactor(d.gauge) : DaystarMath.sunDamage(Math.max(d.gauge, 0.25D), k));
            boolean airborne = !player.onGround() && !player.isInWater() && !player.isPassenger() && !player.onClimbable() && !player.isFallFlying();
            if (DaystarMath.starDrop(airborne, (double)player.getXRot())) {
               fallingStar(player, level, s, (float)((double)dmg * 1.25D), (float)d.gauge);
               d.charge = 0;
            } else {
               MoveAnim.play(player, ember ? "ds_throw" : "ds_heave");
               s.windUp(aimPoint(player, (double)s.size(), ember), ember ? 4 : 6, dmg, (float)d.gauge);
               level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.blaze.shoot"), SoundSource.PLAYERS, 1.0F, ember ? 1.2F : 0.6F);
               d.charge = 0;
            }
         }
      }
   }

   private static void fallingStar(ServerPlayer player, ServerLevel level, SunOrbEntity s, float dmg, float g) {
      Vec3 feet = player.position();
      BlockHitResult hit = level.clip(new ClipContext(feet.add(0.0D, 0.1D, 0.0D), feet.add(0.0D, -96.0D, 0.0D), Block.COLLIDER, Fluid.NONE, player));
      double below = hit.getType() == Type.BLOCK ? hit.getLocation().y - feet.y : -96.0D;
      MoveAnim.play(player, "ds_slam");
      s.starDown(feet.add(0.0D, below, 0.0D), (float)DaystarMath.starLow((double)s.size(), below), dmg, g);
      Vec3 v = player.getDeltaMovement();
      player.setDeltaMovement(v.x * 0.6D, Math.max(v.y, 0.32D), v.z * 0.6D);
      player.hurtMarked = true;
      player.fallDistance = 0.0F;
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.blaze.shoot"), SoundSource.PLAYERS, 1.2F, 0.5F);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.activate"), SoundSource.PLAYERS, 0.9F, 1.8F);
   }

   public static void flareBurst(ServerLevel level, ServerPlayer holder, SunOrbEntity orb, Vec3 at, Vec3 ground, float size, boolean ember, float damage) {
      double reach = DaystarMath.flareReach((double)size, ember);
      AABB box = (new AABB(at, at)).inflate(reach + 1.5D);

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
         if (e != holder && !mount(holder, e) && e.isAlive() && !e.isSpectator()) {
            Vec3 mid = e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D);
            double dist = mid.distanceTo(at);
            if (!(dist > reach + 0.5D + (double)e.getBbWidth() * 0.5D)) {
               boolean friend = holder != null && ally(holder, e);
               if (holder == null || friend || OriginStrike.fair(holder, e)) {
                  double fall = DaystarMath.fall(dist, reach);
                  float dmg = (float)((double)damage * fall * (double)(friend ? 0.25F : 1.0F));
                  DamageSource src = holder != null ? level.damageSources().explosion(orb, holder) : level.damageSources().explosion(orb, (Entity)null);
                  OriginStrike.hitFresh(e, src, dmg);
                  if (!e.fireImmune()) {
                     e.setSecondsOnFire(ember ? 3 : 6);
                  }

                  Vec3 away = new Vec3(mid.x - at.x, 0.0D, mid.z - at.z);
                  away = away.lengthSqr() < 1.0E-4D ? Vec3.ZERO : away.normalize();
                  double push = DaystarMath.flareKnock((double)size, ember) * fall;
                  double lift = DaystarMath.flareLift((double)size, ember) * fall;
                  OriginStrike.shove(e, away.x * push, lift, away.z * push);
               }
            }
         }
      }

      float vol = ember ? 0.9F : Math.min(4.0F, 1.4F + size * 0.3F);
      level.playSound((Player)null, BlockPos.containing(at), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, vol, ember ? 1.5F : 0.9F);
      level.playSound((Player)null, BlockPos.containing(at), CastFxEvents.sound("minecraft:item.firecharge.use"), SoundSource.PLAYERS, vol, 0.7F);
      level.playSound((Player)null, BlockPos.containing(at), CastFxEvents.sound("minecraft:entity.blaze.shoot"), SoundSource.PLAYERS, vol * 0.8F, 0.6F);
      Vec3 ring = ground != null ? ground : at;
      Fx.ring(level, 16757311, ring.add(0.0D, 0.15D, 0.0D), reach, 24 + (int)(reach * 4.0D));
      level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 20 + (int)(reach * 8.0D), reach * 0.45D, 0.4D, reach * 0.45D, 0.08D);
      if (!ember && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
         evaporate(level, ring, reach, 400);
      }

   }

   public static double groundUnder(Level level, Vec3 p, double reach) {
      BlockPos.MutableBlockPos q = new BlockPos.MutableBlockPos();
      int bx = (int)Math.floor(p.x);
      int bz = (int)Math.floor(p.z);
      int top = (int)Math.floor(p.y);
      int rows = (int)Math.ceil(reach) + 1;

      for(int dy = 0; dy <= rows; ++dy) {
         q.set(bx, top - dy, bz);
         VoxelShape shape = level.getBlockState(q).getCollisionShape(level, q);
         if (!shape.isEmpty()) {
            double y = (double)(top - dy) + shape.max(Axis.Y);
            if (!(y > p.y + 0.001D)) {
               return p.y - y <= reach ? y : Double.NaN;
            }
         }
      }

      return Double.NaN;
   }

   public static boolean sunImpact(ServerLevel level, ServerPlayer holder, SunOrbEntity orb, Vec3 at, Vec3 ground, float size, float charge, boolean ember, float damage, float gauge) {
      double blast = orb != null && orb.mode() == 2 ? DaystarMath.starBlast((double)size, ember) : DaystarMath.blast((double)size, ember);
      AABB box = (new AABB(at, at)).inflate(blast + 1.5D);

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
         if (holder != null && e != holder && !mount(holder, e) && e.isAlive() && !e.isSpectator()) {
            Vec3 mid = e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D);
            double dist = mid.distanceTo(at);
            if (!(dist > blast + 0.5D + (double)e.getBbWidth() * 0.5D)) {
               boolean friend = holder != null && ally(holder, e);
               if (holder == null || friend || OriginStrike.fair(holder, e)) {
                  double fall = DaystarMath.fall(dist, blast);
                  float dmg = (float)((double)damage * fall * (double)(friend ? 0.25F : 1.0F));
                  DamageSource src = holder != null ? level.damageSources().explosion(orb, holder) : level.damageSources().explosion(orb, (Entity)null);
                  OriginStrike.hitFresh(e, src, dmg);
                  if (!e.fireImmune()) {
                     e.setSecondsOnFire(ember ? 3 : 8);
                  }

                  Vec3 away = new Vec3(mid.x - at.x, 0.0D, mid.z - at.z);
                  away = away.lengthSqr() < 1.0E-4D ? Vec3.ZERO : away.normalize();
                  double push = DaystarMath.knock((double)size, ember) * fall;
                  double lift = DaystarMath.lift((double)size, ember) * fall;
                  OriginStrike.shove(e, away.x * push, lift, away.z * push);
               }
            }
         }
      }

      boolean star = orb != null && orb.mode() == 2;
      float vol = ember ? 0.8F : Math.min(4.0F, (star ? 1.8F : 1.2F) + size * 0.3F);
      level.playSound((Player)null, BlockPos.containing(at), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, vol, ember ? 1.4F : (star ? 0.55F : 0.7F));
      level.playSound((Player)null, BlockPos.containing(at), CastFxEvents.sound("minecraft:item.firecharge.use"), SoundSource.PLAYERS, vol, 0.6F);
      level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.3D, at.z, ember ? 8 : 30 + (int)(size * 6.0F), blast * 0.4D, 0.3D, blast * 0.4D, 0.06D);
      if (ember) {
         return false;
      } else {
         level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5D, at.z, 2 + (int)((double)size * 0.5D), blast * 0.3D, 0.3D, blast * 0.3D, 0.0D);
         if (level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            int boiled = evaporate(level, at, blast + 1.0D, 900);
            if (boiled > 0 && holder != null) {
               bar(holder, "§fThe water boils away.");
            }
         }

         if (star) {
            return ground != null && DaystarMath.starCraters((double)size) && NadirEvents.sunCrater(level, ground, DaystarMath.starCrater((double)size), holder) > 0;
         } else {
            return ground != null && DaystarMath.craters((double)size) && NadirEvents.sunCrater(level, ground, (double)size * 0.45D, holder) > 0;
         }
      }
   }

   private static void sunder(ServerPlayer player, ServerLevel level, DaystarEvents.Day d) {
      if (d.plunge < 0L) {
         if (hurling(d)) {
            catchNow(player, level, d);
         }

         if (findAxe(player) < 0) {
            drawAxe(player, level, true);
         }

         boolean airborne = !player.onGround() && !player.isInWater() && !player.isInLava() && !player.isPassenger() && !player.onClimbable() && !player.isFallFlying();
         boolean sneak = player.isShiftKeyDown();
         int kind = DaystarMath.sunderKind(airborne, (double)player.getXRot(), sneak);
         double g = sneak ? d.open : d.gauge;
         float dmg = (float)(10.0D * DaystarMath.powerFactor(g));
         if (kind == 2) {
            plunge(player, level, d, g);
         } else if (kind == 3) {
            skyward(player, level, g, dmg);
         } else {
            MoveAnim.play(player, "ds_sunder");
            float len = (float)DaystarMath.rayLength(kind, g);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.ravager.roar"), SoundSource.PLAYERS, 0.6F, 1.4F);
            UUID id = player.getUUID();
            Spectacle.after(7, () -> {
               ServerPlayer p = level.getServer().getPlayerList().getPlayer(id);
               if (p != null && !p.isRemoved() && p.level() == level) {
                  Vec3 look = p.getLookAngle();
                  Vec3 flat = new Vec3(look.x, 0.0D, look.z);
                  flat = flat.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
                  Vec3 start = p.position().add(flat.scale(1.6D * (double)p.getBbHeight() / 1.8D));
                  SunFissureEntity f = (SunFissureEntity)(com.myriam.unrp.entity.ModEntities.SUN_FISSURE.get()).create(level);
                  if (f != null) {
                     f.setPos(start.x, p.getY(), start.z);
                     f.configure(p, flat, len, dmg, (float)g, kind);
                     level.addFreshEntity(f);
                     level.playSound((Player)null, BlockPos.containing(start), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, kind == 1 ? 1.4F : 1.2F, 0.5F);
                     level.playSound((Player)null, BlockPos.containing(start), CastFxEvents.sound("minecraft:block.fire.ambient"), SoundSource.PLAYERS, 1.6F, 0.8F);
                     level.playSound((Player)null, BlockPos.containing(start), CastFxEvents.sound("minecraft:entity.blaze.shoot"), SoundSource.PLAYERS, 0.9F, 0.6F);
                     Fx.burst(level, Fx.flare(16757311), start.add(0.0D, 0.2D, 0.0D), 24, 0.8D, 0.12D);
                     Spectacle.debris(level, start, 1.6D, 18);
                  }
               }
            });
         }
      }
   }

   private static void plunge(ServerPlayer player, ServerLevel level, DaystarEvents.Day d, double g) {
      long now = level.getGameTime();
      d.plunge = now;
      d.plungeFrom = player.getY();
      d.plungeLast = player.getY();
      d.plungeStall = 0;
      d.plungeG = g;
      d.soft = now + 10L;
      player.addTag("unrp.ds_plunge");
      MoveAnim.play(player, "ds_plunge");
      Vec3 v = player.getDeltaMovement();
      player.setDeltaMovement(v.x * 0.5D, -1.8D, v.z * 0.5D);
      player.hurtMarked = true;
      player.fallDistance = 0.0F;
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.ravager.roar"), SoundSource.PLAYERS, 0.6F, 1.4F);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:item.trident.riptide_1"), SoundSource.PLAYERS, 1.0F, 0.7F);
      Fx.burst(level, Fx.flare(16757311), player.position().add(0.0D, 1.2D, 0.0D), 16, 0.5D, 0.1D);
   }

   private static void endPlunge(ServerPlayer player, DaystarEvents.Day d) {
      d.plunge = -1L;
      player.removeTag("unrp.ds_plunge");
   }

   private static void tickPlunge(ServerPlayer player, ServerLevel level, DaystarEvents.Day d, long now) {
      int age = (int)(now - d.plunge);
      player.fallDistance = 0.0F;
      d.soft = now + 10L;
      double y = player.getY();
      d.plungeStall = d.plungeLast - y >= 0.05D ? 0 : d.plungeStall + 1;
      d.plungeLast = y;
      if (player.isAlive() && !player.isPassenger() && !player.onClimbable() && !player.isFallFlying() && !player.isInLava()) {
         double gy = groundUnder(level, player.position().add(0.0D, 0.2D, 0.0D), 2.0D);
         double gap = Double.isNaN(gy) ? 9.0D : y - gy;
         if (!player.isInWater() && !player.onGround() && (age < 2 || d.plungeStall < 2 || !(gap < 1.2D))) {
            if (age <= 240 && !(y < (double)(level.getMinBuildHeight() - 16))) {
               Vec3 v = player.getDeltaMovement();
               player.setDeltaMovement(v.x * 0.5D, -1.8D, v.z * 0.5D);
               player.hurtMarked = true;
               level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.6D, player.getZ(), 6, 0.3D, 0.6D, 0.3D, 0.02D);
               Fx.burst(level, Fx.flare(16757311), player.position().add(0.0D, 2.3D, 0.0D), 3, 0.25D, 0.02D);
            } else {
               endPlunge(player, d);
            }
         } else {
            land(player, level, d, now);
         }
      } else {
         endPlunge(player, d);
      }
   }

   private static void land(ServerPlayer player, ServerLevel level, DaystarEvents.Day d, long now) {
      double drop = Math.max(0.0D, d.plungeFrom - player.getY());
      double g = d.plungeG;
      endPlunge(player, d);
      d.landed = now;
      d.soft = now + 10L;
      player.fallDistance = 0.0F;
      player.setDeltaMovement(Vec3.ZERO);
      player.hurtMarked = true;
      MoveAnim.play(player, "ds_land");
      UUID id = player.getUUID();
      boolean wet = player.isInWater();
      Spectacle.after(2, () -> {
         ServerPlayer p = level.getServer().getPlayerList().getPlayer(id);
         if (p != null && !p.isRemoved() && p.level() == level && p.isAlive()) {
            sunfall(p, level, g, drop, wet || p.isInWater());
         }
      });
   }

   private static void sunfall(ServerPlayer p, ServerLevel level, double g, double drop, boolean wet) {
      double gy = groundUnder(level, p.position().add(0.0D, 0.3D, 0.0D), 3.0D);
      Vec3 at = new Vec3(p.getX(), Double.isNaN(gy) ? p.getY() : gy, p.getZ());
      Vec3 look = p.getLookAngle();
      Vec3 flat = new Vec3(look.x, 0.0D, look.z);
      flat = flat.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
      float dmg = (float)(10.0D * DaystarMath.powerFactor(g));
      SunFissureEntity f = (SunFissureEntity)(com.myriam.unrp.entity.ModEntities.SUN_FISSURE.get()).create(level);
      if (f != null) {
         f.setPos(at.x, at.y, at.z);
         f.configure(p, flat, (float)DaystarMath.rayLength(2, g), dmg, (float)g, 2);
         double reach = DaystarMath.fallReach(drop);
         f.ring((float)reach);
         level.addFreshEntity(f);
         sunfallSlam(level, p, f, at, reach, (float)((double)dmg * DaystarMath.fallHit(drop)), wet);
         BlockPos bp = BlockPos.containing(at);
         level.playSound((Player)null, bp, CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 1.6F, 0.5F);
         level.playSound((Player)null, bp, CastFxEvents.sound("minecraft:entity.warden.attack_impact"), SoundSource.PLAYERS, 1.4F, 0.7F);
         if (wet) {
            level.playSound((Player)null, bp, CastFxEvents.sound("minecraft:block.fire.extinguish"), SoundSource.PLAYERS, 1.4F, 0.6F);
            level.sendParticles(ParticleTypes.CLOUD, at.x, at.y + 0.6D, at.z, 40, reach * 0.4D, 0.4D, reach * 0.4D, 0.06D);
         } else {
            level.playSound((Player)null, bp, CastFxEvents.sound("minecraft:block.fire.ambient"), SoundSource.PLAYERS, 1.6F, 0.7F);
            Spectacle.shockwave(level, 16757311, at, reach);
            level.sendParticles(ParticleTypes.FLAME, at.x, at.y + 0.3D, at.z, 40, reach * 0.4D, 0.3D, reach * 0.4D, 0.08D);
            level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y + 0.5D, at.z, 3, reach * 0.25D, 0.2D, reach * 0.25D, 0.0D);
         }
      }
   }

   public static void sunfallSlam(ServerLevel level, ServerPlayer holder, SunFissureEntity f, Vec3 at, double reach, float dmg, boolean wet) {
      AABB box = (new AABB(at, at)).inflate(reach + 1.0D, 2.5D, reach + 1.0D);

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
         if (e != holder && e.isAlive() && !e.isSpectator()) {
            double dx = e.getX() - at.x;
            double dz = e.getZ() - at.z;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (!(dist > reach + (double)e.getBbWidth() * 0.5D) && !(Math.abs(e.getY() - at.y) > 2.5D)) {
               boolean friend = holder != null && ally(holder, e);
               if (holder == null || friend || OriginStrike.fair(holder, e)) {
                  f.strike(e.getId());
                  double edge = 1.0D - 0.4D * DaystarMath.clamp01(dist / reach);
                  DamageSource src = holder != null ? level.damageSources().indirectMagic(f, holder) : level.damageSources().inFire();
                  OriginStrike.hitFresh(e, src, (float)((double)(friend ? dmg * 0.25F : dmg) * edge));
                  if (!wet && !e.fireImmune()) {
                     e.setSecondsOnFire(6);
                  }

                  double nx = dist < 1.0E-4D ? 0.0D : dx / dist;
                  double nz = dist < 1.0E-4D ? 0.0D : dz / dist;
                  double vy = e.getDeltaMovement().y;
                  OriginStrike.shove(e, nx * 0.9D * edge, Math.max(0.0D, 0.7D * edge - vy), nz * 0.9D * edge);
                  level.sendParticles(ParticleTypes.FLAME, e.getX(), e.getY() + 0.4D, e.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
               }
            }
         }
      }

   }

   private static void skyward(ServerPlayer player, ServerLevel level, double g, float dmg) {
      MoveAnim.play(player, "ds_skyward");
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.ravager.roar"), SoundSource.PLAYERS, 0.5F, 1.6F);
      UUID id = player.getUUID();
      Spectacle.after(6, () -> {
         ServerPlayer p = level.getServer().getPlayerList().getPlayer(id);
         if (p != null && !p.isRemoved() && p.level() == level && p.isAlive()) {
            Vec3 look = p.getLookAngle();
            Vec3 at = p.getEyePosition().add(look.scale(1.2D));
            SunCrescentEntity c = (SunCrescentEntity)(com.myriam.unrp.entity.ModEntities.SUN_CRESCENT.get()).create(level);
            if (c != null) {
               c.setPos(at.x, at.y, at.z);
               c.launch(p, look, (float)DaystarMath.crescentSpan(g), (float)((double)dmg * 0.9D), (float)g);
               level.addFreshEntity(c);
               BlockPos bp = BlockPos.containing(at);
               level.playSound((Player)null, bp, CastFxEvents.sound("minecraft:entity.player.attack.sweep"), SoundSource.PLAYERS, 1.4F, 0.6F);
               level.playSound((Player)null, bp, CastFxEvents.sound("minecraft:entity.blaze.shoot"), SoundSource.PLAYERS, 1.0F, 0.8F);
               level.playSound((Player)null, bp, CastFxEvents.sound("minecraft:item.firecharge.use"), SoundSource.PLAYERS, 0.8F, 0.7F);
               Fx.burst(level, Fx.flare(16757311), at, 14, 0.4D, 0.08D);
            }
         }
      });
   }

   public static void crescentHit(ServerLevel level, ServerPlayer holder, SunCrescentEntity c, LivingEntity e, float dmg, Vec3 dir) {
      if (holder != null) {
         boolean friend = ally(holder, e);
         if (friend || OriginStrike.fair(holder, e)) {
            DamageSource src = level.damageSources().indirectMagic(c, holder);
            OriginStrike.hitFresh(e, src, friend ? dmg * 0.25F : dmg);
            if (!e.fireImmune()) {
               e.setSecondsOnFire(6);
            }

            OriginStrike.shove(e, dir.x * 0.6D, Math.max(0.15D, dir.y * 0.6D), dir.z * 0.6D);
            level.sendParticles(ParticleTypes.FLAME, e.getX(), e.getY() + (double)e.getBbHeight() * 0.5D, e.getZ(), 12, 0.3D, 0.3D, 0.3D, 0.06D);
         }
      }
   }

   public static void crescentEnd(ServerLevel level, Vec3 at, double span, boolean wall) {
      level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, wall ? 30 : 12, span * 0.3D, span * 0.3D, span * 0.3D, 0.05D);
      if (wall) {
         level.playSound((Player)null, BlockPos.containing(at), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.7F, 1.3F);
         Spectacle.debris(level, at.add(0.0D, 0.2D, 0.0D), 1.2D, 12);
      }
   }

   public static void sunderHit(ServerLevel level, ServerPlayer holder, SunFissureEntity f, LivingEntity e, float dmg, Vec3 dir) {
      if (holder != null) {
         boolean friend = ally(holder, e);
         if (friend || OriginStrike.fair(holder, e)) {
            DamageSource src = level.damageSources().indirectMagic(f, holder);
            OriginStrike.hitFresh(e, src, friend ? dmg * 0.25F : dmg);
            if (!e.fireImmune()) {
               e.setSecondsOnFire(6);
            }

            double vy = e.getDeltaMovement().y;
            OriginStrike.shove(e, dir.x * 0.15D, Math.max(0.0D, 0.9D - vy), dir.z * 0.15D);
            level.sendParticles(ParticleTypes.FLAME, e.getX(), e.getY() + 0.4D, e.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.05D);
         }
      }
   }

   public static void crackBurn(ServerLevel level, ServerPlayer holder, SunFissureEntity f, LivingEntity e) {
      if (holder != null) {
         boolean friend = ally(holder, e);
         if (friend || OriginStrike.fair(holder, e)) {
            OriginStrike.hit(e, level.damageSources().inFire(), friend ? 0.25F : 1.0F);
            if (!e.fireImmune()) {
               e.setSecondsOnFire(3);
            }

         }
      }
   }

   @SubscribeEvent
   public static void onAttack(AttackEntityEvent event) {
      Player d_0 = event.getEntity();
      if (d_0 instanceof ServerPlayer player) {
         DaystarEvents.Day d = (DaystarEvents.Day)DAYS.get(player.getUUID());
         if (d != null) {
            d.swung = player.serverLevel().getGameTime();
         }

         axeSwing(player);
      }
   }

   public static void axeSwing(ServerPlayer player) {
      if (player.isAlive() && !player.isSpectator() && !player.isSleeping() && SunsteelAxeItem.isAxe(player.getMainHandItem()) && !SaltSeraphEvents.isPillar(player) && !SaltUnboundEvents.bound(player)) {
         int base = MoveAnim.holding(player);
         int over = MoveAnim.playing(player);
         if (base >= 0 && base != MoveClipIds.id("ds_ready")) {
            if ((base == MoveClipIds.id("ds_lift") || base == MoveClipIds.id("ds_charge")) && (over < 0 || over == MoveClipIds.id("ds_hew"))) {
               MoveAnim.cue(player, "ds_hew", 0, 0);
            }
         } else if (over < 0 || over == MoveClipIds.id("ds_chop")) {
            MoveAnim.cue(player, "ds_chop", 0, 0);
         }

      }
   }

   @SubscribeEvent
   public static void onHurt(LivingHurtEvent event) {
      Entity tags_0 = event.getSource().getEntity();
      if (tags_0 instanceof ServerPlayer player) {
         if (event.getEntity() != player) {
            Set<String> tags = player.getTags();
            if ((tags.contains("unrp.has.daystar") || tags.contains("unrp.ds_copy")) && tags.contains("unrp.ds_on") && noncanonOn(player)) {
               DaystarEvents.Day d = day(player);
               d.swung = player.serverLevel().getGameTime();
               float amount = event.getAmount();
               if (tags.contains("unrp.ds_noon")) {
                  amount *= 1.4F;
               }

               boolean melee = !sweeping && event.getSource().getDirectEntity() == player && SunsteelAxeItem.isAxe(player.getMainHandItem());
               if (melee) {
                  amount *= (float)DaystarMath.powerFactor(d.gauge);
                  LivingEntity target = event.getEntity();
                  if (!target.fireImmune()) {
                     target.setSecondsOnFire(5);
                  }

                  float sweep = (float)(11.0D * DaystarMath.powerFactor(d.gauge) * 0.5D);
                  sweepAround(player, target, sweep);
               }

               event.setAmount(amount);
               return;
            }

            return;
         }
      }

   }

   private static void sweepAround(ServerPlayer player, LivingEntity target, float dmg) {
      ServerLevel level = player.serverLevel();
      AABB box = target.getBoundingBox().inflate(2.0D, 0.5D, 2.0D);
      List<LivingEntity> near = level.getEntitiesOfClass(LivingEntity.class, box);
      boolean was = sweeping;
      sweeping = true;

      try {
         for(LivingEntity e : near) {
            if (e != target && e != player && OriginStrike.fair(player, e) && !((double)e.distanceTo(target) > 2.0D)) {
               OriginStrike.hitFresh(e, level.damageSources().playerAttack(player), dmg);
               if (!e.fireImmune()) {
                  e.setSecondsOnFire(3);
               }
            }
         }
      } finally {
         sweeping = was;
      }

      level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + (double)target.getBbHeight() * 0.5D, target.getZ(), 8, 0.4D, 0.3D, 0.4D, 0.04D);
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onNoonCut(LivingDamageEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (player.getTags().contains("unrp.ds_noon")) {
            event.setAmount(event.getAmount() * 0.050000012F);
            return;
         }
      }

   }

   @SubscribeEvent
   public static void onKnock(LivingKnockBackEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (player.getTags().contains("unrp.ds_noon")) {
            event.setCanceled(true);
         }
      }

   }

   @SubscribeEvent
   public static void onEffect(MobEffectEvent.Applicable event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (player.getTags().contains("unrp.ds_noon")) {
            if (event.getEffectInstance().getEffect().getCategory() == MobEffectCategory.HARMFUL) {
               event.setResult(Result.DENY);
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      LivingEntity d_0 = event.getEntity();
      if (d_0 instanceof ServerPlayer player) {
         if (player.getTags().contains("unrp.has.daystar")) {
            DaystarEvents.Day d = (DaystarEvents.Day)DAYS.get(player.getUUID());
            if (d != null) {
               dropSun(player, player.serverLevel(), d);
               endPlunge(player, d);
               loseHurl(player, d);
            }

            if (player.getTags().contains("unrp.ds_noon")) {
               endNoon(player, false);
            }

            dissolveAxes(player, false);
            return;
         }
      }

   }

   @SubscribeEvent
   public static void onFall(LivingFallEvent event) {
      LivingEntity d_0 = event.getEntity();
      if (d_0 instanceof ServerPlayer player) {
         DaystarEvents.Day d = (DaystarEvents.Day)DAYS.get(player.getUUID());
         if (d != null && (d.plunge >= 0L || player.serverLevel().getGameTime() <= d.soft)) {
            event.setDistance(0.0F);
            event.setDamageMultiplier(0.0F);
         }

      }
   }

   @SubscribeEvent
   public static void onClone(PlayerEvent.Clone event) {
      Player from = event.getEntity();
      if (from instanceof ServerPlayer player) {
         CompoundTag var9 = event.getOriginal().getPersistentData();
         CompoundTag to = player.getPersistentData();
         if (var9.contains("unrp_ds_noon_day")) {
            to.putLong("unrp_ds_noon_day", var9.getLong("unrp_ds_noon_day"));
         }

         DaystarEvents.Day d = (DaystarEvents.Day)DAYS.get(player.getUUID());
         if (d != null) {
            d.fresh = true;
            d.sun = -1;
            d.plunge = -1L;
         }

         if (!event.isWasDeath()) {
            for(String key : new String[]{"unrp_ds_noon_end", "unrp_ds_burnout_end"}) {
               if (var9.contains(key)) {
                  to.putLong(key, var9.getLong(key));
               }
            }

         } else {
            player.removeTag("unrp.ds_noon");
            player.removeTag("unrp.ds_burnout");
            player.removeTag("unrp.ds_heat");
            player.removeTag("unrp.ds_axe_held");
            player.removeTag("unrp.ds_sun_feed");
            player.removeTag("unrp.ds_ready");
            player.removeTag("unrp.ds_sun_charge");
            player.removeTag("unrp.ds_sun_held");
            player.removeTag("unrp.ds_sun_flare");
            player.removeTag("unrp.ds_plunge");
            to.remove("unrp_ds_noon_end");
            to.remove("unrp_ds_burnout_end");
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         if (event.getServer().getTickCount() % 20 == 0) {
            for(ServerPlayer online : event.getServer().getPlayerList().getPlayers()) {
               boolean keeps = online.getTags().contains("unrp.ds_on") && online.getTags().contains("unrp.has.daystar") && noncanonOn(online);
               Inventory inv = online.getInventory();

               for(int i = 0; i < inv.getContainerSize(); ++i) {
                  ItemStack stack = inv.getItem(i);
                  if (SunsteelAxeItem.isAxe(stack)) {
                     CompoundTag tag = stack.getTag();
                     boolean owned = tag.hasUUID("unrp_ds_owner") && tag.getUUID("unrp_ds_owner").equals(online.getUUID());
                     if (!keeps || !owned) {
                        inv.setItem(i, ItemStack.EMPTY);
                        bar(online, owned ? "§7The axe dissolves: Daystar is off." : "§7An axe of light that was never yours dissolves.");
                     }
                  }
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onToss(ItemTossEvent event) {
      if (SunsteelAxeItem.isAxe(event.getEntity().getItem())) {
         event.getEntity().discard();
         Player var2 = event.getPlayer();
         if (var2 instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)var2;
            bar(sp, "§7The axe dissolves the moment it leaves your grip.");
            sp.removeTag("unrp.ds_axe_held");
            sp.removeTag("unrp.ds_ready");
         }
      }

   }

   @SubscribeEvent
   public static void onContainerClose(PlayerContainerEvent.Close event) {
      Player menu = event.getEntity();
      if (menu instanceof ServerPlayer player) {
         AbstractContainerMenu var6 = event.getContainer();
         boolean any = false;

         for(Slot slot : var6.slots) {
            if (slot.container != player.getInventory() && SunsteelAxeItem.isAxe(slot.getItem())) {
               slot.set(ItemStack.EMPTY);
               any = true;
            }
         }

         if (any) {
            var6.broadcastChanges();
            bar(player, "§7The axe dissolves the moment it leaves your grip.");
         }

      }
   }

   @SubscribeEvent
   public static void onItemJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide()) {
         Entity var2 = event.getEntity();
         if (var2 instanceof ItemEntity) {
            ItemEntity item = (ItemEntity)var2;
            if (SunsteelAxeItem.isAxe(item.getItem())) {
               event.setCanceled(true);
            }
         }

      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player d_0 = event.getEntity();
      if (d_0 instanceof ServerPlayer player) {
         DaystarEvents.Day d = (DaystarEvents.Day)DAYS.remove(player.getUUID());
         if (d != null) {
            dropSun(player, player.serverLevel(), d);
            loseHurl(player, d);
         }

         dissolveAxes(player, false);
         player.removeTag("unrp.ds_heat");
         player.removeTag("unrp.ds_sun_charging");
         player.removeTag("unrp.ds_plunge");
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

   private static int peek(ServerPlayer p, String obj) {
      try {
         ServerScoreboard sb = p.server.getScoreboard();
         Objective o = sb.getObjective(obj);
         return o != null && sb.hasPlayerScore(p.getScoreboardName(), o) ? sb.getOrCreatePlayerScore(p.getScoreboardName(), o).getScore() : 0;
      } catch (Exception var4) {
         return 0;
      }
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

   static final class Day {
      double gauge = 1.0D;
      double open = 1.0D;
      int mode;
      double height = 1.0D;
      double width = 1.0D;
      boolean fresh = true;
      int charge;
      boolean capped;
      boolean ember;
      int sun = -1;
      long swung = -1000L;
      long plunge = -1L;
      double plungeFrom;
      double plungeLast;
      int plungeStall;
      double plungeG;
      long soft = -1000L;
      long hurlAt = -1L;
      UUID hurled;
      long hurlSeen;
      long hurlReady = -1000L;
      long hurlFrom = -1000L;
      long landed = -1000L;
   }
}
