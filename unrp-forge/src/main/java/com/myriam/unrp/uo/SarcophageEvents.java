package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;
import com.myriam.unrp.anim.MoveAnim;
import com.myriam.unrp.deal.Spectacle;


import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class SarcophageEvents {
   private static final int SAND = 14594656;
   private static final int RUST = 9848872;
   private static final int TAIL2_AT = 8;
   private static final int TAIL3_AT = 20;
   private static final int TAIL4_AT = 40;
   private static final int REACH_AT = 4;
   private static final int RESERVE_PER_SAND = 20;
   private static final int HARVEST_WINDOW_TICKS = 12000;
   private static final int HARVEST_WINDOW_MAX = 8;
   private static final String HAS_TAG = "unrp.has.sarcophage";
   private static final UUID TAIL_KB_ID = UUID.fromString("3f8a6d15-92c4-4e0b-b7a1-58cc21d4ef07");
   private static final UUID SURF_STEP_ID = UUID.fromString("6b2e91c8-40d7-45fa-8a63-9c05e7d21b53");
   public static final double GRAIN_RANGE = 24.0D;
   public static final int GRAIN_MAX_PER_TARGET = 5;
   public static final double TEMPEST_RADIUS = 8.0D;
   public static final int TEMPEST_STRIKES = 4;
   public static final int TEMPEST_PULSE_EVERY = 60;
   public static final double TEMPEST_SCALE = 1.5D;
   public static final double SHROUD_REACH = 2.5D;
   public static final int SURF_HIT_ICD = 12;
   public static final double SURF_REACH = 2.4D;
   public static final double CRASH_LENGTH = 7.0D;
   public static final double CRASH_WIDTH = 1.8D;
   public static final double COFFIN_CRUSH_RANGE = 32.0D;
   public static final int GRAB_HOLD = 60;
   public static final float HOLD_LEFT = 40.0F;
   public static final float HOLD_RIGHT = 75.0F;
   public static final float HOLD_UP = 45.0F;
   public static final float HOLD_UP_CROUCHED = 20.0F;
   public static final float HOLD_DOWN = 60.0F;
   public static final float CLAW_BONUS = 6.0F;
   public static final double GRAB_REACH = 2.8D;
   public static final double GRAB_PULL = 0.4D;
   public static final double GRAB_PULL_MAX = 1.1D;
   public static final double GRAB_LEASH = 11.0D;
   public static final double HURL_SPEED = 1.6D;
   private static final Map<UUID, ArrayDeque<Long>> harvests = new HashMap();
   private static final Map<UUID, SarcophageEvents.CoffinHold> coffins = new HashMap();
   private static final Map<UUID, Long> coffinedVictims = new HashMap();
   static final String COFFIN_DISABLED = "unrp.coffin_disabled";
   private static final Map<UUID, Integer> regrowNeed = new HashMap();
   public static final int REGROW_SAND = 2;
   private static final Map<UUID, Long> interposeCd = new HashMap();
   private static final Map<UUID, SarcophageEvents.GrabHold> grabHolds = new HashMap();
   private static final Map<UUID, Long> grabCd = new HashMap();
   private static final Map<UUID, Long> coffinCd = new HashMap();
   private static final Map<UUID, Long> sprayCd = new HashMap();
   private static final Set<UUID> shroudWas = new HashSet();
   private static final Map<UUID, Map<UUID, Long>> surfHits = new HashMap();

   public static float grabBossDamage(int might, double growth) {
      return 8.0F + 5.0F * (float)might + 0.25F * (float)growth;
   }

   public static float burstDamage(int might, double growth) {
      return 3.0F + 2.0F * (float)might + 0.1F * (float)growth;
   }

   public static double burstReach(int might) {
      return 2.0D + 0.25D * (double)might;
   }

   public static float grabOpen(int might) {
      return 3.0F + 1.5F * (float)might;
   }

   public static float holdBite(int might) {
      return 1.0F + 0.5F * (float)might;
   }

   public static float holdCrush(int might, double growth) {
      return 7.0F + 3.0F * (float)might + 0.15F * (float)growth;
   }

   public static float coffinGrind(int might) {
      return 1.0F + 0.5F * (float)might;
   }

   public static float coffinCrush(int might, double growth) {
      return 10.0F + 4.0F * (float)might + 0.2F * (float)growth;
   }

   public static double coffinBlast(int might) {
      return 3.0D + 0.5D * (double)might;
   }

   public static float waveDamage(int might, double growth) {
      return 2.0F + 1.5F * (float)might + 0.05F * (float)growth;
   }

   public static float crashDamage(int might, double growth) {
      return 6.0F + 3.0F * (float)might + 0.15F * (float)growth;
   }

   public static float grainDamage(int might, double growth) {
      return 4.0F + 1.5F * (float)might + 0.08F * (float)growth;
   }

   public static int grainStreams(int tails) {
      return 7 + tails;
   }

   public static float tempestStrike(double growth) {
      return 5.0F + 0.12F * (float)growth;
   }

   public static float tempestPulse(double growth) {
      return 8.0F + 0.2F * (float)growth;
   }

   public static float abrasion(int might, double growth) {
      return 1.5F + 0.75F * (float)might + 0.03F * (float)growth;
   }

   public static float tailBonus(int tails, double growth) {
      return 1.25F * (float)tails + 0.05F * (float)growth;
   }

   public static boolean isCoffined(UUID victimId) {
      return coffinedVictims.containsKey(victimId);
   }

   static int score(ServerPlayer player, String objective) {
      Scoreboard sb = player.getServer().getScoreboard();
      Objective obj = sb.getObjective(objective);
      return obj == null ? 0 : sb.getOrCreatePlayerScore(player.getScoreboardName(), obj).getScore();
   }

   static void setScore(ServerPlayer player, String objective, int value) {
      Scoreboard sb = player.getServer().getScoreboard();
      Objective obj = sb.getObjective(objective);
      if (obj != null) {
         sb.getOrCreatePlayerScore(player.getScoreboardName(), obj).setScore(value);
      }
   }

   static int tailsOf(ServerPlayer player) {
      return tailsFor(score(player, "unrp.vital_sand"), player.getTags().contains("unrp.sc_severed"));
   }

   public static int tailsFor(int lifetime, boolean severed) {
      int tails = 1;
      if (lifetime >= 8) {
         tails = 2;
      }

      if (lifetime >= 20) {
         tails = 3;
      }

      if (lifetime >= 40) {
         tails = 4;
      }

      if (severed && tails > 1) {
         --tails;
      }

      return tails;
   }

   public static int ladderTails(ServerPlayer player) {
      return tailsOf(player);
   }

   public static int ladderAt(int tails) {
      return tails >= 4 ? 40 : (tails == 3 ? 20 : (tails == 2 ? 8 : 0));
   }

   public static int ladderSand(ServerPlayer player) {
      return Math.max(0, score(player, "unrp.vital_sand"));
   }

   public static int ladderRegrow(ServerPlayer player) {
      if (!player.getTags().contains("unrp.sc_severed")) {
         return 0;
      } else {
         Integer need = (Integer)regrowNeed.get(player.getUUID());
         return need == null ? 2 : Math.max(1, need);
      }
   }

   private static int might(ServerPlayer player) {
      return player.getTags().contains("unrp.sc_phage") ? tailsOf(player) + 1 : tailsOf(player) + (player.getTags().contains("unrp.sc_tempest") ? 1 : 0);
   }

   static int mightOf(ServerPlayer player) {
      return might(player);
   }

   static double growthOf(ServerPlayer player) {
      return growth(player);
   }

   static int capOf(ServerPlayer player) {
      return reserveCap(player);
   }

   static double scaleOf(Entity e) {
      return Math.max(0.25D, (double)e.getBbWidth() / 0.6D);
   }

   private static double growth(ServerPlayer player) {
      return com.myriam.unrp.ability.OriginGrowth.points(player);
   }

   private static BlockParticleOption grains() {
      return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState());
   }

   private static void graveBurst(ServerPlayer player, ServerLevel level, LivingEntity center) {
      int might = might(player);
      Vec3 at = center.position();
      double reach = burstReach(might);
      float dmg = burstDamage(might, growth(player));

      for(LivingEntity live : OriginStrike.foesNear(level, player, at.add(0.0D, (double)center.getBbHeight() * 0.5D, 0.0D), reach)) {
         if (live != center) {
            OriginStrike.hitFresh(live, level.damageSources().playerAttack(player), dmg);
            Vec3 in = at.subtract(live.position());
            OriginStrike.shove(live, in.x * 0.15D, 0.25D, in.z * 0.15D);
            sandPuff(level, live.position().add(0.0D, (double)live.getBbHeight() * 0.5D, 0.0D), 8, 0.3D, 0.1D);
         }
      }

      Spectacle.shockwave(level, 14594656, at, reach);
      Spectacle.spikes(level, grains(), at, 10, reach, 1.6D);
      sandPuff(level, at.add(0.0D, 0.6D, 0.0D), 24, reach * 0.3D, 0.12D);
   }

   private static int reserveCap(ServerPlayer player) {
      return 40 + tailsOf(player) * 20;
   }

   static void bar(ServerPlayer player, String msg) {
      player.displayClientMessage(Component.literal(msg), true);
   }

   static void sandPuff(ServerLevel level, Vec3 at, int count, double spread, double speed) {
      level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()), at.x, at.y, at.z, count, spread, spread, spread, speed);
   }

   private static void sandLine(ServerLevel level, Vec3 from, Vec3 to, double step) {
      Vec3 d = to.subtract(from);
      double len = d.length();
      if (!(len < 0.01D)) {
         Vec3 unit = d.scale(1.0D / len);

         for(double s = 0.0D; s <= len; s += step) {
            Vec3 p = from.add(unit.scale(s));
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()), p.x, p.y, p.z, 2, 0.08D, 0.08D, 0.08D, 0.02D);
         }

      }
   }

   private static boolean onCd(ServerPlayer player, Map<UUID, Long> cds, long now) {
      Long at = (Long)cds.get(player.getUUID());
      return at != null && now < at;
   }

   private static boolean chargeAura(ServerPlayer player, int cost) {
      int aura = score(player, "unrp.aura");
      if (aura < cost) {
         bar(player, "§7Not enough aura.");
         return false;
      } else {
         setScore(player, "unrp.aura", aura - cost);
         return true;
      }
   }

   public static boolean consumeVitalSand(ServerPlayer player) {
      int lifetime = score(player, "unrp.vital_sand") + 1;
      setScore(player, "unrp.vital_sand", lifetime);
      int cap = reserveCap(player);
      int reserve = Math.min(cap, score(player, "unrp.sc_reserve") + 20);
      setScore(player, "unrp.sc_reserve", reserve);
      ServerLevel level = player.serverLevel();
      Vec3 at = player.position().add(0.0D, 1.0D, 0.0D);
      Fx.shell(level, Fx.drop(14594656), at, 0.9D, 16, -0.12D);
      sandPuff(level, at, 14, 0.5D, 0.06D);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.eat"), SoundSource.PLAYERS, 0.9F, 0.7F);
      Integer need = (Integer)regrowNeed.get(player.getUUID());
      if (need == null && player.getTags().contains("unrp.sc_severed")) {
         need = 2;
      }

      if (need != null) {
         if (need <= 1) {
            regrowNeed.remove(player.getUUID());
            player.removeTag("unrp.sc_severed");
            bar(player, "§6The severed tail regrows, grain by grain.");
            Fx.burst(level, Fx.drop(14594656), at, 20, 0.5D, 0.1D);
         } else {
            regrowNeed.put(player.getUUID(), need - 1);
            bar(player, "§7The stump drinks the sand. It needs more.");
         }
      }

      if (lifetime == 4) {
         player.addTag("unrp.sc_reach");
         bar(player, "§6Your tendrils lengthen.");
      }

      if (lifetime == 8 || lifetime == 20 || lifetime == 40) {
         tailCeremony(player, level, lifetime == 8 ? 2 : (lifetime == 20 ? 3 : 4));
      }

      syncTailTags(player);
      return true;
   }

   public static boolean regrowFromClub(ServerPlayer player) {
      if (!player.getTags().contains("unrp.sc_severed")) {
         return false;
      } else {
         regrowNeed.remove(player.getUUID());
         player.removeTag("unrp.sc_severed");
         syncTailTags(player);
         ServerLevel level = player.serverLevel();
         Vec3 at = player.position().add(0.0D, 1.2D, 0.0D);
         Fx.burst(level, Fx.drop(14594656), at, 24, 0.6D, 0.12D);
         sandPuff(level, at, 18, 0.6D, 0.08D);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.eat"), SoundSource.PLAYERS, 1.0F, 0.6F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.break"), SoundSource.PLAYERS, 1.0F, 0.5F);
         bar(player, "§6The club returns to the spine. The tail lives again.");
         return true;
      }
   }

   private static void tailCeremony(ServerPlayer player, ServerLevel level, int tails) {
      Vec3 base = player.position();

      for(int i = 0; i < 18; ++i) {
         Fx.burst(level, Fx.drop(14594656), base.add(0.0D, (double)i * 0.26D, 0.0D), 7, 0.4D, 0.06D);
      }

      sandPuff(level, base.add(0.0D, 1.0D, 0.0D), 40, 0.8D, 0.12D);
      Fx.ring(level, 14594656, base.x, base.y + 0.05D, base.z, 1.8D, 14);
      Fx.ring(level, 14594656, base.x, base.y + 1.2D, base.z, 1.2D, 10);
      Fx.burst(level, Fx.ember(9848872), base.add(0.0D, 1.4D, 0.0D), 12, 0.5D, 0.06D);
      Fx.shell(level, Fx.drop(14594656), base.add(0.0D, 2.2D, 0.0D), 0.9D, 20, 0.18D);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.break"), SoundSource.PLAYERS, 1.2F, 0.5F);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.sonic_boom"), SoundSource.PLAYERS, 0.5F, 0.4F);
      CastFxEvents.pose(player, "sc_sprout", 20);
      MinecraftServer server = player.getServer();
      if (server != null) {
         server.getPlayerList().broadcastSystemMessage(Component.literal("§6" + player.getName().getString() + "§e's Sarcophage has grown its " + (tails == 2 ? "second" : (tails == 3 ? "third" : "fourth")) + " tail."), false);
      }

   }

   private static boolean isActive(ServerPlayer player) {
      Set<String> tags = player.getTags();
      return tags.contains("unrp.has.sarcophage") && tags.contains("unrp.active.sarcophage") && !tags.contains("unrp.power_disabled");
   }

   private static void syncTailTags(ServerPlayer player) {
      int tails = tailsOf(player);
      boolean active = isActive(player);
      Set<String> tags = player.getTags();
      if (tails >= 2) {
         tags.add("unrp.sc_tails2");
      } else {
         tags.remove("unrp.sc_tails2");
      }

      if (tails >= 3) {
         tags.add("unrp.sc_tails3");
      } else {
         tags.remove("unrp.sc_tails3");
      }

      if (tails >= 4) {
         tags.add("unrp.sc_tails4");
      } else {
         tags.remove("unrp.sc_tails4");
      }

      AttributeInstance kb = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
      if (kb != null) {
         boolean want = active && tails >= 3;
         boolean has = kb.getModifier(TAIL_KB_ID) != null;
         if (want && !has) {
            kb.addTransientModifier(new AttributeModifier(TAIL_KB_ID, "unrp_tails", 0.4D, Operation.ADDITION));
         } else if (!want && has) {
            kb.removeModifier(TAIL_KB_ID);
         }
      }

      if (active && tails >= 2) {
         BlockState under = player.level().getBlockState(player.blockPosition().below());
         boolean onSand = under.is(Blocks.SAND) || under.is(Blocks.RED_SAND) || under.is(Blocks.SANDSTONE) || under.is(Blocks.RED_SANDSTONE) || under.is(Blocks.SMOOTH_SANDSTONE) || under.is(Blocks.CUT_SANDSTONE);
         player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, onSand ? 1 : 0, false, false));
      }

   }

   private static LivingEntity rayTarget(ServerPlayer player, ServerLevel level, double range) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();

      for(double d = 1.0D; d <= range; d += 0.5D) {
         Vec3 point = eye.add(look.scale(d));
         Iterator var9 = level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(point, 1.4D, 1.4D, 1.4D), (le) -> le != player && le.isAlive() && OriginStrike.fair(player, le)).iterator();
         if (var9.hasNext()) {
            return (LivingEntity)var9.next();
         }
      }

      return null;
   }

   private static boolean harvestAllowed(ServerPlayer player, long now) {
      ArrayDeque<Long> dq = (ArrayDeque)harvests.computeIfAbsent(player.getUUID(), (k) -> new ArrayDeque());

      while(!dq.isEmpty() && now - dq.peekFirst() > 12000L) {
         dq.pollFirst();
      }

      return dq.size() < 8;
   }

   private static void dropSand(ServerLevel level, Vec3 at, int count) {
      ItemEntity item = new ItemEntity(level, at.x, at.y + 0.4D, at.z, new ItemStack(com.myriam.unrp.item.ModItems.VITAL_SAND.get(), count));
      item.setDefaultPickUpDelay();
      level.addFreshEntity(item);
   }

   private static void doGrab(ServerPlayer player, ServerLevel level) {
      long now = level.getGameTime();
      SarcophageEvents.GrabHold holding = (SarcophageEvents.GrabHold)grabHolds.get(player.getUUID());
      if (holding != null) {
         hurl(player, level, holding);
      } else if (onCd(player, grabCd, now)) {
         bar(player, "§7The tendril is still coiling back.");
      } else {
         double range = 5.0D + (player.getTags().contains("unrp.sc_reach") ? 2.0D : 0.0D);
         LivingEntity target = rayTarget(player, level, range);
         if (target == null) {
            bar(player, "§7Nothing within the tendril's reach.");
         } else if (chargeAura(player, 6)) {
            grabCd.put(player.getUUID(), now + 80L);
            CastFxEvents.pose(player, "sc_grab", 12);
            CastFxEvents.swing(player, false);
            Vec3 from = player.position().add(0.0D, 1.3D, 0.0D);
            Vec3 to = target.position().add(0.0D, (double)target.getBbHeight() * 0.6D, 0.0D);
            Fx.line(level, Fx.drop(14594656), from, to, 0.35D, 0.1D, 0.03D);
            sandLine(level, from, to, 0.6D);
            Fx.line(level, Fx.ember(9848872), from.add(0.0D, 0.15D, 0.0D), to, 0.7D, 0.16D, 0.02D);
            sandPuff(level, to, 16, 0.4D, 0.1D);
            level.playSound((Player)null, target.blockPosition(), CastFxEvents.sound("minecraft:block.sand.break"), SoundSource.PLAYERS, 1.0F, 0.8F);
            level.playSound((Player)null, target.blockPosition(), CastFxEvents.sound("minecraft:entity.player.attack.sweep"), SoundSource.PLAYERS, 0.7F, 0.6F);
            if (target instanceof ServerPlayer) {
               ServerPlayer victim = (ServerPlayer)target;
               if (!victim.isCreative() && !victim.isSpectator()) {
                  OriginStrike.hitFresh(victim, level.damageSources().playerAttack(player), grabOpen(might(player)));
                  graveBurst(player, level, victim);
                  hold(player, level, victim, now, false);
                  UUID casterId = player.getUUID();
                  StruggleEvents.begin(victim, 6, 0, 60, () -> {
                     SarcophageEvents.GrabHold gh = (SarcophageEvents.GrabHold)grabHolds.get(casterId);
                     if (gh != null) {
                        gh.escaped = true;
                     }

                  });
                  bar(victim, "§6The sand closes around you. §eBreak free!");
               } else {
                  bar(player, "§7The grains can't find purchase.");
               }
            } else {
               boolean bossTier = target.getMaxHealth() >= 100.0F;
               if (bossTier) {
                  OriginStrike.hitFresh(target, level.damageSources().playerAttack(player), grabBossDamage(might(player), growth(player)));
                  graveBurst(player, level, target);
               } else {
                  graveBurst(player, level, target);
                  hold(player, level, target, now, true);
                  bar(player, "§6Held. §7Look where it should go; cast again to hurl it.");
               }
            }
         }
      }
   }

   private static void hold(ServerPlayer player, ServerLevel level, LivingEntity victim, long now, boolean mob) {
      SarcophageEvents.GrabHold hold = new SarcophageEvents.GrabHold();
      hold.victim = victim.getUUID();
      hold.dim = level.dimension();
      hold.until = now + 60L;
      hold.mob = mob;
      grabHolds.put(player.getUUID(), hold);
      player.addTag("unrp.sc_grabbing");
      OriginLooks.sarcHold(player, victim, 60);
   }

   static Vec3 holdDir(ServerPlayer player) {
      float turn = Mth.wrapDegrees(player.getYRot() - player.yBodyRot);
      float yaw = player.yBodyRot + Mth.clamp(turn, -40.0F, 75.0F);
      float up = player.isCrouching() ? 20.0F : 45.0F;
      float pitch = Mth.clamp(player.getXRot(), -up, 60.0F);
      return Vec3.directionFromRotation(pitch, yaw);
   }

   private static void hurl(ServerPlayer player, ServerLevel level, SarcophageEvents.GrabHold gh) {
      grabHolds.remove(player.getUUID());
      Entity e = level.dimension().equals(gh.dim) ? level.getEntity(gh.victim) : null;
      CastFxEvents.pose(player, "sc_grab", 12);
      CastFxEvents.swing(player, false);
      if (e instanceof LivingEntity victim) {
         if (victim.isAlive() && !gh.escaped && !TimeWeft.everpresent(victim)) {
            level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:entity.player.attack.sweep"), SoundSource.PLAYERS, 1.0F, 0.5F);
            finishHold(player, level, victim, gh, player.getLookAngle());
            return;
         }
      }

      player.removeTag("unrp.sc_grabbing");
      OriginLooks.sarcHold(player, (Entity)null, 0);
      if (e instanceof ServerPlayer vsp) {
         StruggleEvents.end(vsp);
      }

   }

   private static void finishHold(ServerPlayer caster, ServerLevel level, LivingEntity victim, SarcophageEvents.GrabHold gh, Vec3 hurl) {
      caster.removeTag("unrp.sc_grabbing");
      OriginLooks.sarcHold(caster, (Entity)null, 0);
      if (victim instanceof ServerPlayer vsp) {
         StruggleEvents.end(vsp);
      }

      Vec3 at = victim.position().add(0.0D, (double)victim.getBbHeight() * 0.5D, 0.0D);
      Spectacle.shockwave(level, 14594656, victim.position(), 2.2D);
      sandPuff(level, at, 30, 0.6D, 0.2D);
      Fx.shell(level, Fx.drop(9848872), at, 0.8D, 20, 0.18D);
      level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.attack_impact"), SoundSource.PLAYERS, 0.8F, 0.6F);
      if (gh.mob) {
         harvestKill(caster, level, victim, level.getGameTime());
      } else {
         OriginStrike.hitFresh(victim, level.damageSources().playerAttack(caster), holdCrush(might(caster), growth(caster)));
         boolean dampened = victim.getTags().contains("unrp.dampened");
         boolean low = victim.isAlive() && !SgRules.outside(victim) && victim.getHealth() < victim.getMaxHealth() * 0.25F;
         if (victim.isAlive() && dampened && low) {
            OriginStrike.hitFresh(victim, level.damageSources().playerAttack(caster), 1000.0F);
            Fx.shell(level, Fx.drop(14594656), at, 0.8D, 30, 0.25D);
            sandPuff(level, at, 40, 0.8D, 0.3D);
            dropSand(level, at, 3);
            level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.sonic_boom"), SoundSource.PLAYERS, 0.6F, 0.6F);
         }
      }

      if (hurl != null) {
         OriginStrike.shove(victim, hurl.x * 1.6D, hurl.y * 1.6D + 0.25D, hurl.z * 1.6D);
         Fx.burst(level, Fx.drop(14594656), victim.position().add(0.0D, (double)victim.getBbHeight() * 0.5D, 0.0D), 16, 0.4D, 0.2D);
      }

   }

   static void harvestKill(ServerPlayer player, ServerLevel level, LivingEntity target, long now) {
      OriginStrike.hitFresh(target, level.damageSources().playerAttack(player), 1000.0F);
      if (!target.isAlive()) {
         harvested(player, level, target, now);
      }

   }

   static void harvested(ServerPlayer player, ServerLevel level, LivingEntity target, long now) {
      Vec3 to = target.position().add(0.0D, (double)target.getBbHeight() * 0.6D, 0.0D);
      Fx.shell(level, Fx.drop(14594656), to, 0.6D, 20, 0.2D);
      sandPuff(level, to, 20, 0.5D, 0.15D);
      if (target instanceof Enemy && target.getMaxHealth() >= 8.0F && harvestAllowed(player, now)) {
         ((ArrayDeque)harvests.get(player.getUUID())).addLast(now);
         dropSand(level, to, target.getMaxHealth() >= 20.0F ? 2 : 1);
      } else if (!(target instanceof Enemy)) {
         bar(player, "§7Tame prey carries no vital grains.");
      } else {
         bar(player, "§7The sand is sated. It will hunger again soon.");
      }

   }

   static void prepareUnseal(ServerPlayer player, ServerLevel level) {
      player.removeTag("unrp.sc_tempest");
      if (player.getTags().contains("unrp.sc_surf")) {
         endSurf(player, level, false);
      }

      SarcophageEvents.GrabHold gh = (SarcophageEvents.GrabHold)grabHolds.remove(player.getUUID());
      if (gh != null) {
         player.removeTag("unrp.sc_grabbing");
         OriginLooks.sarcHold(player, (Entity)null, 0);
         if (player.getServer() != null) {
            ServerPlayer victim = player.getServer().getPlayerList().getPlayer(gh.victim);
            if (victim != null) {
               StruggleEvents.end(victim);
            }
         }
      }

   }

   private static void doCoffin(ServerPlayer player, ServerLevel level) {
      long now = level.getGameTime();
      SarcophageEvents.CoffinHold existing = (SarcophageEvents.CoffinHold)coffins.get(player.getUUID());
      if (existing != null) {
         crushCoffin(player, level, existing);
      } else if (onCd(player, coffinCd, now)) {
         bar(player, "§7The coffin sands are still settling.");
      } else {
         LivingEntity target = rayTarget(player, level, 8.0D);
         if (target == null) {
            bar(player, "§7No victim in sight to wrap.");
         } else {
            if (target instanceof ServerPlayer) {
               ServerPlayer sp = (ServerPlayer)target;
               if (sp.isCreative() || sp.isSpectator()) {
                  bar(player, "§7The grains can't find purchase.");
                  return;
               }
            }

            if (chargeAura(player, 15)) {
               coffinCd.put(player.getUUID(), now + 320L);
               CastFxEvents.pose(player, "sc_coffin", 12);
               int hold = 60 + tailsOf(player) * 15;
               SarcophageEvents.CoffinHold ch = new SarcophageEvents.CoffinHold();
               ch.victim = target.getUUID();
               ch.dim = level.dimension();
               ch.anchor = target.position();
               ch.until = now + (long)hold;
               ch.setDisabled = !target.getTags().contains("unrp.power_disabled");
               coffins.put(player.getUUID(), ch);
               coffinedVictims.put(target.getUUID(), ch.until);
               target.addTag("unrp.dampened");
               if (ch.setDisabled) {
                  target.addTag("unrp.power_disabled");
                  target.addTag("unrp.coffin_disabled");
               }

               if (target instanceof ServerPlayer) {
                  ServerPlayer victim = (ServerPlayer)target;
                  UUID casterId = player.getUUID();
                  StruggleEvents.begin(victim, 8 + tailsOf(player), 0, hold + 10, () -> releaseCoffin(ServerLifecycleHooks.getCurrentServer(), casterId));
               }

               BindVisualEntity cocoon = new BindVisualEntity(com.myriam.unrp.entity.ModEntities.BIND_VISUAL.get(), level);
               cocoon.bindTo(target, 2);
               level.addFreshEntity(cocoon);
               level.playSound((Player)null, target.blockPosition(), CastFxEvents.sound("minecraft:block.sand.place"), SoundSource.PLAYERS, 1.2F, 0.5F);
               level.playSound((Player)null, target.blockPosition(), CastFxEvents.sound("minecraft:block.respawn_anchor.deplete"), SoundSource.PLAYERS, 0.5F, 0.5F);
               Fx.ring(level, 14594656, target.getX(), target.getY() + 0.05D, target.getZ(), 1.4D, 12);
               Fx.ring(level, 9848872, target.getX(), target.getY() + (double)target.getBbHeight() * 0.6D, target.getZ(), 1.0D, 10);
               Fx.shell(level, Fx.drop(14594656), target.position().add(0.0D, (double)target.getBbHeight() * 0.5D, 0.0D), 0.8D, 22, -0.15D);
               sandPuff(level, target.position().add(0.0D, (double)target.getBbHeight() * 0.5D, 0.0D), 30, 0.7D, 0.1D);
            }
         }
      }
   }

   private static void crushCoffin(ServerPlayer player, ServerLevel level, SarcophageEvents.CoffinHold ch) {
      Entity e = level.dimension().equals(ch.dim) ? level.getEntity(ch.victim) : null;
      if (e instanceof LivingEntity target) {
         if (target.isAlive() && !((double)target.distanceTo(player) > 32.0D)) {
            int might = might(player);
            float crush = coffinCrush(might, growth(player));
            double blast = coffinBlast(might);
            Vec3 mid = target.position().add(0.0D, (double)target.getBbHeight() * 0.5D, 0.0D);
            OriginStrike.hitFresh(target, level.damageSources().playerAttack(player), crush);

            for(LivingEntity live : OriginStrike.foesNear(level, player, mid, blast)) {
               if (live != target) {
                  OriginStrike.hitFresh(live, level.damageSources().playerAttack(player), crush * 0.5F);
                  Vec3 out = new Vec3(live.getX() - target.getX(), 0.0D, live.getZ() - target.getZ());
                  Vec3 dir = out.lengthSqr() < 1.0E-4D ? Vec3.ZERO : out.normalize();
                  OriginStrike.shove(live, dir.x * 0.9D, 0.45D, dir.z * 0.9D);
               }
            }

            Fx.shell(level, Fx.drop(9848872), mid, 0.7D, 24, 0.15D);
            Fx.shell(level, Fx.drop(14594656), mid, blast * 0.6D, 40, 0.3D);
            sandPuff(level, mid, 50, 0.9D, 0.25D);
            Spectacle.shockwave(level, 14594656, target.position(), blast);
            Spectacle.pillar(level, grains(), target.position(), 4.0D);
            level.playSound((Player)null, target.blockPosition(), CastFxEvents.sound("minecraft:block.sand.break"), SoundSource.PLAYERS, 1.2F, 0.4F);
            level.playSound((Player)null, target.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.attack_impact"), SoundSource.PLAYERS, 0.7F, 0.7F);
            level.playSound((Player)null, target.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.8F, 0.7F);
            releaseCoffin(player.getServer(), player.getUUID());
            bar(player, "§6The coffin is crushed.");
            return;
         }
      }

      releaseCoffin(player.getServer(), player.getUUID());
      bar(player, "§7The coffin slips out of reach and crumbles.");
   }

   private static void releaseCoffin(MinecraftServer server, UUID casterId) {
      SarcophageEvents.CoffinHold ch = (SarcophageEvents.CoffinHold)coffins.remove(casterId);
      if (ch != null && server != null) {
         coffinedVictims.remove(ch.victim);
         ServerLevel level = server.getLevel(ch.dim);
         Entity e = level == null ? null : level.getEntity(ch.victim);
         LivingEntity var10000;
         if (e instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity)e;
            var10000 = victim;
         } else {
            var10000 = stray(server, ch.victim);
         }

         free(var10000);
      }
   }

   static LivingEntity stray(MinecraftServer server, UUID id) {
      if (server == null) {
         return null;
      } else {
         for(ServerLevel lv : server.getAllLevels()) {
            Entity e = lv.getEntity(id);
            if (e instanceof LivingEntity) {
               return (LivingEntity)e;
            }
         }

         return null;
      }
   }

   static void free(LivingEntity victim) {
      if (victim != null) {
         victim.removeTag("unrp.dampened");
         if (victim.getTags().contains("unrp.coffin_disabled")) {
            victim.removeTag("unrp.coffin_disabled");
            victim.removeTag("unrp.power_disabled");
         }

         if (victim instanceof ServerPlayer) {
            ServerPlayer vsp = (ServerPlayer)victim;
            StruggleEvents.end(vsp);
         }

      }
   }

   private static void toggleSurf(ServerPlayer player, ServerLevel level) {
      if (player.getTags().contains("unrp.sc_surf")) {
         endSurf(player, level, true);
      } else if (score(player, "unrp.sc_reserve") < 3) {
         bar(player, "§7Not enough sand in reserve to raise a wave.");
      } else {
         player.addTag("unrp.sc_surf");
         player.addTag("unrp.sc_softland");
         AttributeInstance step = player.getAttribute((Attribute)ForgeMod.STEP_HEIGHT_ADDITION.get());
         if (step != null && step.getModifier(SURF_STEP_ID) == null) {
            step.addTransientModifier(new AttributeModifier(SURF_STEP_ID, "unrp_surf_step", 1.1D, Operation.ADDITION));
         }

         CastFxEvents.pose(player, "sc_lunge", 12);
         Vec3 base = player.position();
         Fx.ring(level, 14594656, base.x, base.y + 0.05D, base.z, 1.4D, 10);
         sandPuff(level, base, 26, 0.7D, 0.15D);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.break"), SoundSource.PLAYERS, 1.1F, 0.5F);
         bar(player, "§6The dune rises beneath you. §7Cast again to dismount.");
      }
   }

   private static void endSurf(ServerPlayer player, ServerLevel level, boolean crash) {
      player.removeTag("unrp.sc_surf");
      MoveAnim.release(player, "sc_lunge");
      surfHits.remove(player.getUUID());
      AttributeInstance step = player.getAttribute((Attribute)ForgeMod.STEP_HEIGHT_ADDITION.get());
      if (step != null && step.getModifier(SURF_STEP_ID) != null) {
         step.removeModifier(SURF_STEP_ID);
      }

      Vec3 base = player.position();
      sandPuff(level, base, 20, 0.8D, 0.1D);
      Fx.burst(level, Fx.drop(14594656), base.add(0.0D, 0.3D, 0.0D), 12, 0.6D, 0.06D);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.fall"), SoundSource.PLAYERS, 0.9F, 0.8F);
      if (crash) {
         duneCrash(player, level);
      }

   }

   private static void duneCrash(ServerPlayer player, ServerLevel level) {
      int might = might(player);
      float dmg = crashDamage(might, growth(player));
      Vec3 look = player.getLookAngle();
      Vec3 flat = new Vec3(look.x, 0.0D, look.z);
      if (flat.lengthSqr() < 0.01D) {
         flat = new Vec3(0.0D, 0.0D, 1.0D);
      }

      flat = flat.normalize();
      Vec3 from = player.position().add(0.0D, 0.8D, 0.0D);
      Vec3 to = from.add(flat.scale(7.0D));

      for(LivingEntity live : OriginStrike.foesAlong(level, player, from, to, 1.8D)) {
         OriginStrike.hitFresh(live, level.damageSources().playerAttack(player), dmg);
         OriginStrike.shove(live, flat.x * 1.2D, 0.55D, flat.z * 1.2D);
      }

      Vec3 base = player.position();
      Vec3 dirF = flat;

      for(int i = 1; i <= 7; ++i) {
         Vec3 at = base.add(dirF.scale((double)i));
         double tall = 1.4D + 0.25D * (double)i;
         Spectacle.after(i, () -> {
            Spectacle.pillar(level, grains(), at, tall);
            sandPuff(level, at.add(0.0D, 0.4D, 0.0D), 10, 0.6D, 0.12D);
         });
      }

      Vec3 end = base.add(dirF.scale(7.0D));
      Spectacle.after(7, () -> Spectacle.shockwave(level, 14594656, end, 2.6D));
      CastFxEvents.pose(player, "sc_crash", 16);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.break"), SoundSource.PLAYERS, 1.3F, 0.4F);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.7F, 0.9F);
      bar(player, "§6The dune breaks forward.");
   }

   private static void tickSurf(ServerPlayer player, ServerLevel level) {
      if (!player.isInWater() && !player.isSpectator()) {
         if (player.tickCount % 20 == 0) {
            int reserve = score(player, "unrp.sc_reserve");
            if (reserve < 1) {
               bar(player, "§7The wave starves and falls.");
               endSurf(player, level, false);
               return;
            }

            setScore(player, "unrp.sc_reserve", reserve - 1);
         }

         Vec3 look = player.getLookAngle();
         Vec3 flat = new Vec3(look.x, 0.0D, look.z);
         if (flat.lengthSqr() < 0.01D) {
            flat = new Vec3(0.0D, 0.0D, 1.0D);
         }

         flat = flat.normalize();
         double vy = player.onGround() ? 0.16D : Math.max(-0.1D, player.getDeltaMovement().y * 0.85D);
         if (player.horizontalCollision) {
            vy = 0.48D;
         }

         player.setDeltaMovement(flat.x * 0.55D, vy, flat.z * 0.55D);
         player.hurtMarked = true;
         player.addTag("unrp.sc_softland");
         if (player.tickCount % 4 == 0) {
            trample(player, level, flat);
         }

         if (player.tickCount % 2 == 0) {
            Vec3 base = player.position();
            Vec3 side = new Vec3(-flat.z, 0.0D, flat.x);

            for(int j = -3; j <= 3; ++j) {
               Vec3 at_0 = base.add(flat.scale(0.9D + (double)Math.abs(j) * 0.12D)).add(side.scale((double)j * 0.34D)).add(0.0D, 0.1D + (double)(3 - Math.abs(j)) * 0.14D, 0.0D);
               level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()), at_0.x, at_0.y, at_0.z, 0, flat.x * 0.35D, 0.28D + (double)(3 - Math.abs(j)) * 0.06D, flat.z * 0.35D, 1.0D);
            }

            for(int k = 0; k < 4; ++k) {
               Vec3 at = base.add(flat.scale(-0.6D - (double)k * 0.35D)).add(side.scale((level.random.nextDouble() - 0.5D) * 1.3D));
               level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()), at.x, at.y + 0.15D, at.z, 0, -flat.x * 0.12D, 0.18D, -flat.z * 0.12D, 1.0D);
            }

            sandPuff(level, base.add(0.0D, 0.12D, 0.0D), 5, 0.5D, 0.1D);
            Fx.burst(level, Fx.drop(14594656), base.add(flat.scale(1.1D)).add(0.0D, 0.5D, 0.0D), 4, 0.35D, 0.08D);
         }

         if (player.tickCount % 8 == 0) {
            CastFxEvents.pose(player, "sc_lunge", 12);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.step"), SoundSource.PLAYERS, 1.2F, 0.7F);
         }

         if (player.tickCount % 24 == 0) {
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.break"), SoundSource.PLAYERS, 0.5F, 0.5F);
         }

      } else {
         endSurf(player, level, false);
      }
   }

   private static void trample(ServerPlayer player, ServerLevel level, Vec3 flat) {
      int might = might(player);
      float dmg = waveDamage(might, growth(player));
      long now = level.getGameTime();
      Map<UUID, Long> seen = (Map)surfHits.computeIfAbsent(player.getUUID(), (k) -> new HashMap());
      Vec3 front = player.position().add(flat.scale(1.0D)).add(0.0D, 0.8D, 0.0D);
      Vec3 side = new Vec3(-flat.z, 0.0D, flat.x);

      for(LivingEntity live : OriginStrike.foesNear(level, player, front, 2.4D)) {
         Long last = (Long)seen.get(live.getUUID());
         if (last == null || now - last >= 12L) {
            seen.put(live.getUUID(), now);
            OriginStrike.hitFresh(live, level.damageSources().playerAttack(player), dmg);
            double s = Math.signum(side.dot(live.position().subtract(player.position())));
            OriginStrike.shove(live, flat.x * 0.4D + side.x * s * 0.5D, 0.5D, flat.z * 0.4D + side.z * s * 0.5D);
            sandPuff(level, live.position().add(0.0D, 0.8D, 0.0D), 14, 0.4D, 0.12D);
         }
      }

      if (seen.size() > 64) {
         seen.clear();
      }

   }

   private static void doSpray(ServerPlayer player, ServerLevel level) {
      long now = level.getGameTime();
      if (onCd(player, sprayCd, now)) {
         bar(player, "§7The barrels are still refilling with grain.");
      } else if (chargeAura(player, 8)) {
         sprayCd.put(player.getUUID(), now + 100L);
         CastFxEvents.pose(player, "sc_spray", 10);
         CastFxEvents.swing(player, true);
         Vec3 eye = player.getEyePosition();
         Vec3 look = player.getLookAngle();
         Fx.burst(level, Fx.drop(14594656), eye.add(look.scale(0.9D)), 18, 0.35D, 0.15D);
         sandPuff(level, eye.add(look.scale(1.1D)), 30, 0.4D, 0.25D);
         Fx.cone(level, Fx.ember(9848872), eye.add(look.scale(0.8D)), look, 0.3D, 10, 0.5D);
         player.setDeltaMovement(player.getDeltaMovement().add(look.scale(-0.25D)));
         player.hurtMarked = true;
         int might = might(player);
         float per = grainDamage(might, growth(player));
         int streams = grainStreams(tailsOf(player));
         double spread = 0.26D + 0.04D * (double)might;
         Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
         right = right.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
         Vec3 up = right.cross(look).normalize();
         Map<UUID, Integer> struck = new HashMap();
         Fx.ring(level, 14594656, eye.x, eye.y - 0.2D, eye.z, 1.6D, 8);

         for(int i = 0; i < streams; ++i) {
            double yaw = (level.random.nextDouble() - 0.5D) * spread;
            double pitch = (level.random.nextDouble() - 0.5D) * 0.12D;
            Vec3 dir = look.add(right.scale(yaw)).add(up.scale(pitch)).normalize();
            Vec3 end = eye.add(dir.scale(24.0D));
            HitResult wall = level.clip(new ClipContext(eye, end, Block.COLLIDER, Fluid.NONE, player));
            double reach = wall.getType() == Type.MISS ? 24.0D : wall.getLocation().distanceTo(eye);
            end = eye.add(dir.scale(reach));
            LivingEntity hit = null;

            for(double d = 1.0D; d <= reach; d += 0.6D) {
               Vec3 point = eye.add(dir.scale(d));
               Iterator var28 = level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(point, 1.1D, 1.1D, 1.1D), (le) -> le != player && le.isAlive() && OriginStrike.fair(player, le)).iterator();
               if (var28.hasNext()) {
                  LivingEntity e = (LivingEntity)var28.next();
                  hit = e;
                  end = point;
               }

               if (hit != null) {
                  break;
               }
            }

            Fx.line(level, Fx.drop(14594656), eye.add(0.0D, -0.2D, 0.0D), end, 0.6D, 0.06D, 0.02D);
            sandLine(level, eye.add(dir.scale(1.5D)), end, 1.1D);
            Fx.line(level, Fx.glint(14594656), eye.add(0.0D, -0.1D, 0.0D), end, 1.4D, 0.03D, 0.01D);
            if (hit != null) {
               int k = struck.merge(hit.getUUID(), 1, Integer::sum);
               if (k <= 5) {
                  OriginStrike.hitFresh(hit, level.damageSources().playerAttack(player), k == 1 ? per : per * 0.5F);
               }

               hit.knockback(0.25D, player.getX() - hit.getX(), player.getZ() - hit.getZ());
               sandPuff(level, end, 12, 0.4D, 0.18D);
            } else {
               sandPuff(level, end, 8, 0.4D, 0.08D);
            }
         }

         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.break"), SoundSource.PLAYERS, 1.3F, 0.8F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.player.attack.crit"), SoundSource.PLAYERS, 0.9F, 0.7F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.firework_rocket.blast"), SoundSource.PLAYERS, 0.6F, 0.6F);
      }
   }

   private static void doSever(ServerPlayer player, ServerLevel level) {
      if (player.getTags().contains("unrp.sc_severed")) {
         bar(player, "§7One tail is already gone. Feed the stump first.");
      } else if (tailsOf(player) < 2) {
         bar(player, "§7You can't spare your only tail.");
      } else {
         player.addTag("unrp.sc_severed");
         regrowNeed.put(player.getUUID(), 2);
         CastFxEvents.pose(player, "sc_sever", 12);
         ItemStack club = new ItemStack(com.myriam.unrp.item.ModItems.OSSIFIED_TAIL.get());
         club.getOrCreateTag().putLong("unrp_tail_born", level.getGameTime());
         if (!player.getInventory().add(club)) {
            dropClub(level, player, club);
         }

         Vec3 at = player.position().add(0.0D, 1.2D, 0.0D);
         Fx.burst(level, Fx.drop(14594656), at, 24, 0.6D, 0.12D);
         sandPuff(level, at, 20, 0.6D, 0.1D);
         Fx.burst(level, Fx.ember(9848872), at, 8, 0.4D, 0.08D);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.skeleton.hurt"), SoundSource.PLAYERS, 1.0F, 0.6F);
         syncTailTags(player);
         bar(player, "§6A tail ossifies in your grip. §7Consume it to take it back.");
      }
   }

   private static void dropClub(ServerLevel level, ServerPlayer player, ItemStack club) {
      ItemEntity item = new ItemEntity(level, player.getX(), player.getY() + 0.8D, player.getZ(), club);
      item.setDefaultPickUpDelay();
      level.addFreshEntity(item);
   }

   private static void toggleTempest(ServerPlayer player, ServerLevel level) {
      if (player.getTags().contains("unrp.sc_tempest")) {
         endTempest(player, level);
      } else {
         CastFxEvents.pose(player, "sc_tempest", 22);
         if (tailsOf(player) < 4) {
            bar(player, "§7The dune answers only a four-tailed spine.");
         } else if (score(player, "unrp.sc_reserve") < 10) {
            bar(player, "§7The dune needs more sand to rise.");
         } else {
            player.addTag("unrp.sc_tempest");
            player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack().withSuppressedOutput(), "scale set pehkui:base 1.5 " + player.getName().getString());
            Vec3 base = player.position();

            for(int i = 0; i < 16; ++i) {
               sandPuff(level, base.add(0.0D, (double)i * 0.18D, 0.0D), 8, 0.6D, 0.1D);
            }

            Fx.ring(level, 14594656, base.x, base.y + 0.05D, base.z, 2.4D, 16);
            Fx.ring(level, 9848872, base.x, base.y + 1.2D, base.z, 1.6D, 12);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.place"), SoundSource.PLAYERS, 1.3F, 0.4F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.respawn_anchor.deplete"), SoundSource.PLAYERS, 0.7F, 0.4F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.heartbeat"), SoundSource.PLAYERS, 1.0F, 0.6F);
            bar(player, "§6The dune rises. §eFive tails crown the storm.");
         }
      }
   }

   private static void endTempest(ServerPlayer player, ServerLevel level) {
      player.removeTag("unrp.sc_tempest");
      player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack().withSuppressedOutput(), "scale set pehkui:base 1.0 " + player.getName().getString());
      Vec3 base = player.position();
      sandPuff(level, base.add(0.0D, 1.0D, 0.0D), 30, 1.0D, 0.12D);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.fall"), SoundSource.PLAYERS, 1.0F, 0.6F);
   }

   private static void tickTempest(ServerPlayer player, ServerLevel level) {
      if (player.tickCount % 20 == 0) {
         int reserve = score(player, "unrp.sc_reserve");
         if (reserve < 2) {
            bar(player, "§7The dune starves. The storm falls.");
            endTempest(player, level);
            return;
         }

         setScore(player, "unrp.sc_reserve", reserve - 2);
      }

      double growth = growth(player);
      Vec3 heart = player.position().add(0.0D, 1.0D, 0.0D);
      if (player.tickCount % 10 == 0) {
         List<LivingEntity> near = OriginStrike.foesNear(level, player, heart, 8.0D);
         near.sort(OriginStrike.threatsFirst(player, (ex) -> ex.distanceToSqr(player)));
         int struck = 0;

         for(LivingEntity e_0 : near) {
            if (struck >= 4) {
               break;
            }

            OriginStrike.hitFresh(e_0, level.damageSources().playerAttack(player), tempestStrike(growth));
            e_0.knockback(0.5D, player.getX() - e_0.getX(), player.getZ() - e_0.getZ());
            Vec3 to = e_0.position().add(0.0D, (double)e_0.getBbHeight() * 0.5D, 0.0D);
            Fx.line(level, Fx.drop(14594656), player.position().add(0.0D, 1.6D, 0.0D), to, 0.5D, 0.1D, 0.03D);
            sandLine(level, player.position().add(0.0D, 1.4D, 0.0D), to, 0.8D);
            sandPuff(level, to, 8, 0.3D, 0.1D);
            ++struck;
         }

         if (struck > 0) {
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.player.attack.sweep"), SoundSource.PLAYERS, 0.8F, 0.5F);
         }
      }

      if (player.tickCount % 60 == 0) {
         for(LivingEntity e : OriginStrike.foesNear(level, player, heart, 8.0D)) {
            OriginStrike.hitFresh(e, level.damageSources().playerAttack(player), tempestPulse(growth));
            Vec3 in = new Vec3(player.getX() - e.getX(), 0.0D, player.getZ() - e.getZ());
            Vec3 dir = in.lengthSqr() < 1.0E-4D ? Vec3.ZERO : in.normalize();
            OriginStrike.shove(e, dir.x * 0.6D, 0.2D, dir.z * 0.6D);
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, true));
         }

         Spectacle.shockwave(level, 14594656, player.position(), 8.0D);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.break"), SoundSource.PLAYERS, 1.4F, 0.4F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.6F, 0.5F);
      }

      if (player.tickCount % 4 == 0) {
         double spin = (double)player.tickCount * 0.15D;

         for(int i = 0; i < 20; ++i) {
            double a = (Math.PI * 2D) * (double)i / 20.0D + spin;
            double x = player.getX() + Math.cos(a) * 8.0D;
            double z = player.getZ() + Math.sin(a) * 8.0D;
            double y = player.getY() + level.random.nextDouble() * 3.0D;
            level.sendParticles(grains(), x, y, z, 0, -Math.sin(a) * 0.6D, 0.05D, Math.cos(a) * 0.6D, 1.0D);
         }
      }

      if (player.tickCount % 20 == 0) {
         Vec3 base = player.position();
         sandPuff(level, base.add(0.0D, 0.2D, 0.0D), 14, 1.6D, 0.06D);
         Fx.ring(level, 14594656, base.x, base.y + 0.08D, base.z, 2.2D, 10);
      }

      if (player.tickCount % 40 == 0) {
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.heartbeat"), SoundSource.PLAYERS, 0.8F, 0.5F);
      }

   }

   private static void abrade(ServerPlayer player, ServerLevel level) {
      float dmg = abrasion(might(player), growth(player));
      Vec3 heart = player.position().add(0.0D, 1.0D, 0.0D);
      boolean any = false;

      for(LivingEntity live : OriginStrike.foesNear(level, player, heart, 2.5D)) {
         OriginStrike.hitFresh(live, level.damageSources().playerAttack(player), dmg);
         sandPuff(level, live.position().add(0.0D, (double)live.getBbHeight() * 0.5D, 0.0D), 8, 0.3D, 0.08D);
         any = true;
      }

      Fx.ring(level, 14594656, player.getX(), player.getY() + 0.1D, player.getZ(), 2.5D, 12);
      if (any) {
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.hit"), SoundSource.PLAYERS, 0.9F, 0.6F);
      }

   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player level = event.player;
         if (level instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)level;
            ServerLevel var11 = player.serverLevel();
            Set<String> tags = player.getTags();
            boolean has = tags.contains("unrp.has.sarcophage");
            if (player.tickCount % 20 == 0 && has) {
               com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SandHudPacket(score(player, "unrp.sc_reserve"), reserveCap(player), score(player, "unrp.vital_sand"), tailsOf(player), tags.contains("unrp.sc_severed")));
            }

            if (player.tickCount % 40 == 0) {
               if (!has) {
                  if (tags.contains("unrp.sc_tails2") || tags.contains("unrp.sc_shroud") || tags.contains("unrp.sc_tempest") || tags.contains("unrp.sc_severed") || tags.contains("unrp.sc_surf") || tags.contains("unrp.sc_phage")) {
                     tags.remove("unrp.sc_tails2");
                     tags.remove("unrp.sc_tails3");
                     tags.remove("unrp.sc_tails4");
                     tags.remove("unrp.sc_shroud");
                     tags.remove("unrp.sc_surf");
                     tags.remove("unrp.sc_severed");
                     tags.remove("unrp.sc_reach");
                     if (tags.contains("unrp.sc_tempest")) {
                        endTempest(player, var11);
                     }

                     if (tags.contains("unrp.sc_phage")) {
                        PhageEvents.reseal(player, var11, false, (String)null);
                     }

                     AttributeInstance kb = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
                     if (kb != null && kb.getModifier(TAIL_KB_ID) != null) {
                        kb.removeModifier(TAIL_KB_ID);
                     }
                  }
               } else {
                  syncTailTags(player);
                  if (score(player, "unrp.vital_sand") >= 4) {
                     tags.add("unrp.sc_reach");
                  }

                  BlockState under = var11.getBlockState(player.blockPosition().below());
                  if (under.is(Blocks.SAND) || under.is(Blocks.RED_SAND)) {
                     int cap = reserveCap(player);
                     int reserve_0 = score(player, "unrp.sc_reserve");
                     if (reserve_0 < cap) {
                        setScore(player, "unrp.sc_reserve", Math.min(cap, reserve_0 + 2));
                     }
                  }
               }
            }

            if (has) {
               boolean activeNow = isActive(player);
               if (!activeNow) {
                  tags.remove("unrp.sc_grab_trigger");
                  tags.remove("unrp.sc_lunge_trigger");
                  tags.remove("unrp.sc_spray_trigger");
                  if (tags.contains("unrp.sc_phage")) {
                     PhageEvents.reseal(player, var11, false, (String)null);
                  }

                  if (tags.contains("unrp.sc_tempest")) {
                     endTempest(player, var11);
                  }

                  if (tags.contains("unrp.sc_surf")) {
                     endSurf(player, var11, false);
                  }

               } else {
                  boolean phage = tags.contains("unrp.sc_phage");
                  boolean sneak = player.isShiftKeyDown();
                  if (tags.remove("unrp.sc_grab_trigger")) {
                     if (phage) {
                        PhageEvents.grabKey(player, var11, sneak);
                     } else if (sneak && !grabHolds.containsKey(player.getUUID())) {
                        doCoffin(player, var11);
                     } else {
                        doGrab(player, var11);
                     }
                  }

                  if (tags.remove("unrp.sc_lunge_trigger")) {
                     if (phage) {
                        PhageEvents.lungeKey(player, var11, sneak);
                     } else if (sneak) {
                        doSever(player, var11);
                     } else {
                        toggleSurf(player, var11);
                     }
                  }

                  if (tags.remove("unrp.sc_spray_trigger")) {
                     if (phage) {
                        PhageEvents.sprayKey(player, var11, sneak);
                     } else if (sneak) {
                        toggleTempest(player, var11);
                     } else {
                        doSpray(player, var11);
                     }
                  }

                  if (tags.contains("unrp.sc_surf")) {
                     tickSurf(player, var11);
                  }

                  boolean shroudOn = tags.contains("unrp.sc_shroud");
                  boolean shroudEdge = shroudOn != shroudWas.contains(player.getUUID());
                  if (shroudOn) {
                     shroudWas.add(player.getUUID());
                  } else {
                     shroudWas.remove(player.getUUID());
                  }

                  if (shroudEdge && phage) {
                     PhageEvents.shroudKey(player, var11, sneak);
                     phage = tags.contains("unrp.sc_phage");
                  } else if (shroudEdge && sneak && tags.contains("unrp.sc_tempest") && PhageEvents.tryUnseal(player, var11)) {
                     phage = tags.contains("unrp.sc_phage");
                  } else if (shroudEdge && shroudOn) {
                     CastFxEvents.pose(player, "sc_shroud", 16);
                  }

                  if (phage) {
                     PhageEvents.tick(player, var11);
                  }

                  if (shroudOn && !phage && player.tickCount % 20 == 0) {
                     int reserve = score(player, "unrp.sc_reserve");
                     if (reserve > 0) {
                        setScore(player, "unrp.sc_reserve", reserve - 1);
                        Fx.shell(var11, Fx.drop(14594656), player.position().add(0.0D, 1.0D, 0.0D), 0.7D, 4, 0.02D);
                        sandPuff(var11, player.position().add(0.0D, 1.0D, 0.0D), 3, 0.4D, 0.02D);
                        abrade(player, var11);
                     } else {
                        bar(player, "§7The shroud hangs empty. Feed the sand.");
                     }
                  }

                  if (tags.contains("unrp.sc_tempest")) {
                     tickTempest(player, var11);
                  }

               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
         if (server != null) {
            long now = server.overworld().getGameTime();
            if (!grabHolds.isEmpty()) {
               Iterator<Map.Entry<UUID, SarcophageEvents.GrabHold>> git = grabHolds.entrySet().iterator();

               while(git.hasNext()) {
                  Map.Entry<UUID, SarcophageEvents.GrabHold> entry_0 = (Map.Entry)git.next();
                  SarcophageEvents.GrabHold gh = (SarcophageEvents.GrabHold)entry_0.getValue();
                  ServerLevel level_0 = server.getLevel(gh.dim);
                  ServerPlayer caster_0 = server.getPlayerList().getPlayer((UUID)entry_0.getKey());
                  Entity e_0 = level_0 == null ? null : level_0.getEntity(gh.victim);
                  if (level_0 != null && caster_0 != null && caster_0.isAlive() && e_0 instanceof LivingEntity) {
                     LivingEntity victim_0 = (LivingEntity)e_0;
                     if (victim_0.isAlive() && !gh.escaped && !TimeWeft.everpresent(victim_0) && !((double)victim_0.distanceTo(caster_0) > 11.0D * scaleOf(caster_0))) {
                        if (now >= gh.until) {
                           git.remove();
                           finishHold(caster_0, level_0, victim_0, gh, (Vec3)null);
                        } else {
                           Vec3 holdPoint = caster_0.getEyePosition().add(holdDir(caster_0).scale(2.8D * scaleOf(caster_0)));
                           Vec3 center = victim_0.position().add(0.0D, (double)victim_0.getBbHeight() * 0.5D, 0.0D);
                           Vec3 pull = holdPoint.subtract(center).scale(0.4D);
                           if (pull.length() > 1.1D) {
                              pull = pull.normalize().scale(1.1D);
                           }

                           victim_0.setDeltaMovement(pull);
                           victim_0.hurtMarked = true;
                           victim_0.fallDistance = 0.0F;
                           victim_0.addEffect(new MobEffectInstance(MobEffects.JUMP, 8, 128, false, false));
                           if (now % 4L == 0L) {
                              sandPuff(level_0, center, 3, 0.3D, 0.02D);
                              Fx.burst(level_0, Fx.drop(14594656), center, 2, 0.3D, 0.03D);
                           }

                           if (now % 10L == 0L) {
                              OriginStrike.hitFresh(victim_0, level_0.damageSources().playerAttack(caster_0), holdBite(might(caster_0)));
                              sandPuff(level_0, center, 6, 0.35D, 0.08D);
                              if (gh.mob && !victim_0.isAlive()) {
                                 git.remove();
                                 caster_0.removeTag("unrp.sc_grabbing");
                                 OriginLooks.sarcHold(caster_0, (Entity)null, 0);
                                 harvested(caster_0, level_0, victim_0, now);
                                 continue;
                              }
                           }

                           if (now % 20L == 0L) {
                              OriginLooks.sarcHold(caster_0, victim_0, (int)(gh.until - now));
                           }
                        }
                        continue;
                     }
                  }

                  git.remove();
                  if (caster_0 != null) {
                     caster_0.removeTag("unrp.sc_grabbing");
                     OriginLooks.sarcHold(caster_0, (Entity)null, 0);
                  }

                  if (e_0 instanceof ServerPlayer) {
                     ServerPlayer vsp = (ServerPlayer)e_0;
                     StruggleEvents.end(vsp);
                  }
               }
            }

            if (!coffins.isEmpty()) {
               Iterator<Map.Entry<UUID, SarcophageEvents.CoffinHold>> it = coffins.entrySet().iterator();

               while(it.hasNext()) {
                  Map.Entry<UUID, SarcophageEvents.CoffinHold> entry = (Map.Entry)it.next();
                  SarcophageEvents.CoffinHold ch = (SarcophageEvents.CoffinHold)entry.getValue();
                  ServerLevel level = server.getLevel(ch.dim);
                  if (level == null) {
                     it.remove();
                     coffinedVictims.remove(ch.victim);
                     free(stray(server, ch.victim));
                  } else {
                     Entity e = level.getEntity(ch.victim);
                     if (e instanceof LivingEntity) {
                        LivingEntity victim = (LivingEntity)e;
                        if (victim.isAlive() && now < ch.until) {
                           if (!TimeWeft.everpresent(victim)) {
                              victim.teleportTo(ch.anchor.x, ch.anchor.y, ch.anchor.z);
                           }

                           victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 4, false, false));
                           victim.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 8, 2, false, false));
                           victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 8, 1, false, false));
                           if (now % 8L == 0L) {
                              Vec3 at = victim.position().add(0.0D, (double)victim.getBbHeight() * 0.55D, 0.0D);
                              sandPuff(level, at, 8, 0.5D, 0.05D);
                           }

                           if (now % 20L == 0L) {
                              ServerPlayer caster = server.getPlayerList().getPlayer((UUID)entry.getKey());
                              if (caster != null) {
                                 OriginStrike.hitFresh(victim, level.damageSources().playerAttack(caster), coffinGrind(might(caster)));
                              }

                              Fx.ring(level, 14594656, victim.getX(), victim.getY() + 0.1D, victim.getZ(), 1.2D, 8);
                              level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.sand.step"), SoundSource.PLAYERS, 0.7F, 0.5F);
                           }
                           continue;
                        }
                     }

                     it.remove();
                     coffinedVictims.remove(ch.victim);
                     LivingEntity var10000;
                     if (e instanceof LivingEntity) {
                        LivingEntity victim2 = (LivingEntity)e;
                        var10000 = victim2;
                     } else {
                        var10000 = stray(server, ch.victim);
                     }

                     free(var10000);
                  }
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void onHurt(LivingHurtEvent event) {
      Entity src = event.getSource().getDirectEntity();
      if (src instanceof ServerPlayer attacker) {
         if (attacker.getTags().contains("unrp.has.sarcophage") && event.getEntity() != attacker && isActive(attacker) && !OriginStrike.active()) {
            int tails = tailsOf(attacker);
            float bonus = tailBonus(tails, growth(attacker));
            if (attacker.getTags().contains("unrp.sc_tempest")) {
               bonus += 6.0F;
            }

            if (attacker.getTags().contains("unrp.sc_phage") && OriginStrike.fair(attacker, event.getEntity())) {
               float full = (float)Math.max(1.0D, attacker.getAttributeValue(Attributes.ATTACK_DAMAGE));
               float swing = Mth.clamp(event.getAmount() / full, 0.0F, 1.5F);
               event.setAmount((PhageEvents.clawDamage(might(attacker)) + 6.0F) * swing);
            }

            if (bonus > 0.0F) {
               Level var13 = attacker.level();
               if (var13 instanceof ServerLevel) {
                  ServerLevel lvl = (ServerLevel)var13;
                  event.setAmount(event.getAmount() + bonus);
                  Vec3 at = event.getEntity().position().add(0.0D, (double)event.getEntity().getBbHeight() * 0.6D, 0.0D);
                  sandPuff(lvl, at, 4, 0.25D, 0.06D);
               }
            }
         }
      }

      LivingEntity var8 = event.getEntity();
      if (var8 instanceof ServerPlayer player) {
         Set<String> tags = player.getTags();
         if (tags.contains("unrp.has.sarcophage")) {
            float amount = event.getAmount();
            amount *= 1.0F - 0.05F * (float)tailsOf(player);
            if (tags.contains("unrp.sc_shroud") && !tags.contains("unrp.sc_phage") && score(player, "unrp.sc_reserve") > 0) {
               amount *= 0.6F;
            }

            if (tags.contains("unrp.sc_tempest") && score(player, "unrp.sc_reserve") > 0) {
               amount *= 0.65F;
            }

            if (tags.contains("unrp.sc_phage")) {
               amount = PhageEvents.absorb(player, amount);
            }

            event.setAmount(amount);
         }
      }
   }

   @SubscribeEvent
   public static void onFall(LivingFallEvent event) {
      LivingEntity level_0 = event.getEntity();
      if (level_0 instanceof ServerPlayer player) {
         if (!player.getTags().contains("unrp.sc_surf") && !player.getTags().contains("unrp.sc_phage")) {
            if (player.getTags().remove("unrp.sc_softland")) {
               event.setCanceled(true);
               Level var3 = player.level();
               if (var3 instanceof ServerLevel) {
                  ServerLevel level = (ServerLevel)var3;
                  Fx.burst(level, Fx.drop(14594656), player.position(), 10, 0.5D, 0.05D);
                  sandPuff(level, player.position(), 8, 0.5D, 0.05D);
               }
            }

         } else {
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public static void onProjectileImpact(ProjectileImpactEvent event) {
      HitResult player_0 = event.getRayTraceResult();
      if (player_0 instanceof EntityHitResult hit) {
         Entity tags_0 = hit.getEntity();
         if (tags_0 instanceof ServerPlayer player) {
            Set<String> tags = player.getTags();
            if (tags.contains("unrp.has.sarcophage") && tags.contains("unrp.sc_tails2")) {
               Level now = player.level();
               if (now instanceof ServerLevel) {
                  ServerLevel level = (ServerLevel)now;
                  long var11 = level.getGameTime();
                  Long cd = (Long)interposeCd.get(player.getUUID());
                  if (cd == null || var11 - cd >= 40L) {
                     if (!(level.random.nextFloat() >= 0.35F)) {
                        interposeCd.put(player.getUUID(), var11);
                        event.setCanceled(true);
                        Vec3 at = player.position().add(0.0D, (double)player.getBbHeight() * 0.7D, 0.0D);
                        Fx.burst(level, Fx.drop(14594656), at, 14, 0.5D, 0.12D);
                        sandPuff(level, at, 10, 0.4D, 0.1D);
                        level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sand.hit"), SoundSource.PLAYERS, 1.0F, 1.2F);
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player step_0 = event.getEntity();
      if (step_0 instanceof ServerPlayer player) {
         releaseCoffin(player.getServer(), player.getUUID());
         player.removeTag("unrp.sc_grabbing");
         AttributeInstance step = player.getAttribute((Attribute)ForgeMod.STEP_HEIGHT_ADDITION.get());
         if (step != null && step.getModifier(SURF_STEP_ID) != null) {
            step.removeModifier(SURF_STEP_ID);
         }

         SarcophageEvents.GrabHold gh = (SarcophageEvents.GrabHold)grabHolds.remove(player.getUUID());
         if (gh != null) {
            OriginLooks.sarcHold(player, (Entity)null, 0);
         }

         if (gh != null && player.getServer() != null) {
            ServerPlayer victim = player.getServer().getPlayerList().getPlayer(gh.victim);
            if (victim != null) {
               StruggleEvents.end(victim);
            }
         }

         if (player.getTags().contains("unrp.sc_tempest")) {
            Level var5 = player.level();
            if (var5 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var5;
               endTempest(player, level);
            }
         }

         harvests.remove(player.getUUID());
         shroudWas.remove(player.getUUID());
         surfHits.remove(player.getUUID());
         interposeCd.remove(player.getUUID());
         grabCd.remove(player.getUUID());
         coffinCd.remove(player.getUUID());
         sprayCd.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onJoinLevel(EntityJoinLevelEvent event) {
      Entity e = event.getEntity();
      if (!event.getLevel().isClientSide() && e instanceof LivingEntity victim) {
         if (!coffinedVictims.containsKey(e.getUUID()) && victim.getTags().contains("unrp.dampened")) {
            free(victim);
            return;
         }
      }

   }

   private static class CoffinHold {
      UUID victim;
      ResourceKey<Level> dim;
      Vec3 anchor;
      long until;
      boolean setDisabled;
   }

   private static class GrabHold {
      UUID victim;
      ResourceKey<Level> dim;
      long until;
      boolean escaped;
      boolean mob;
   }
}
