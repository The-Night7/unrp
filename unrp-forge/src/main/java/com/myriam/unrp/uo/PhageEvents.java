package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;
import com.myriam.unrp.deal.Spectacle;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public final class PhageEvents {
   public static final String TAG = "unrp.sc_phage";
   public static final String BURROW_TAG = "unrp.sc_burrow";
   public static final double SCALE = 2.5D;
   public static final int UNSEAL_TAILS = 4;
   public static final int UNSEAL_RESERVE = 60;
   public static final int UPKEEP = 3;
   public static final int SEAL_COOLDOWN = 3600;
   public static final int FED = 10;
   public static final int MOVE_DEVOUR = 1;
   public static final int MOVE_BURROW = 2;
   public static final int MOVE_ERUPT = 3;
   public static final int MOVE_ROAR = 4;
   public static final int MOVE_LASH = 5;
   public static final int MOVE_OBELISK = 6;
   public static final int MOVE_SINK = 7;
   public static final int MOVE_SPIT = 8;
   public static final int MOVE_RISE = 9;
   public static final int MOVE_HOLD = 10;
   public static final double MOTION = 0.28D;
   public static final double DEVOUR_RANGE = 6.0D;
   public static final double DEVOUR_CONE = 0.64D;
   public static final int DEVOUR_CD = 80;
   public static final int DEVOUR_SNATCH = 12;
   public static final int DEVOUR_LIFT = 20;
   public static final int DEVOUR_CHOMP = 21;
   public static final int DEVOUR_TICKS = 38;
   public static final int DEVOUR_HOLD = 60;
   public static final int DEVOUR_GRIND = 10;
   public static final int DEVOUR_GULP = 14;
   public static final double[] TAKE = new double[]{-24.9D, 16.7D, -18.1D};
   public static final double[] FEED = new double[]{-16.0D, -10.4D, -16.1D};
   public static final double[] MAW = new double[]{-1.8D, -10.7D, -14.6D};
   public static final double DISGORGE_RANGE = 10.0D;
   public static final double DISGORGE_CONE = 0.82D;
   public static final int DISGORGE_CD = 160;
   public static final int DISGORGE_AT = 4;
   public static final int DISGORGE_TICKS = 22;
   public static final int BURROW_DIVE = 12;
   public static final int BURROW_SWIM = 80;
   public static final int BURROW_CD = 200;
   public static final double ERUPT_REACH = 4.0D;
   public static final int ERUPT_AT = 4;
   public static final int ERUPT_TICKS = 32;
   public static final double SINK_RADIUS = 3.5D;
   public static final int SINK_AT = 10;
   public static final int SINK_TICKS = 120;
   public static final int SINK_ANIM = 24;
   public static final int SINK_CD = 240;
   public static final double ROAR_RANGE = 16.0D;
   public static final double ROAR_CONE = 0.87D;
   public static final int ROAR_FROM = 10;
   public static final int ROAR_UNTIL = 40;
   public static final int ROAR_EVERY = 5;
   public static final int ROAR_TICKS = 50;
   public static final int ROAR_CD = 180;
   public static final double OBELISK_RANGE = 24.0D;
   public static final double OBELISK_REACH = 1.8D;
   public static final double OBELISK_TALL = 5.0D;
   public static final int OBELISK_AT = 13;
   public static final int OBELISK_TICKS = 28;
   public static final int OBELISK_CD = 200;
   public static final double LASH_REACH = 4.48D;
   public static final int[] LASH_HITS = new int[]{10, 14, 18, 22, 26};
   public static final int LASH_TICKS = 42;
   public static final int LASH_CD = 120;
   public static final int RISE_TICKS = 32;
   private static final int SAND = 14594656;
   private static final int RUST = 9848872;
   private static final Map<UUID, PhageEvents.Beast> beasts = new HashMap();
   private static final Map<UUID, Long> sealCd = new HashMap();
   private static final Map<UUID, Map<Integer, Long>> cds = new HashMap();

   public static float devourBossDamage(int might, double growth) {
      return 1.5F * SarcophageEvents.grabBossDamage(might, growth);
   }

   public static float disgorgeDamage(int might, double growth) {
      return 4.0F + 2.5F * (float)might + 0.1F * (float)growth;
   }

   public static float eruptDamage(int might, double growth) {
      return 1.2F * SarcophageEvents.crashDamage(might, growth);
   }

   public static float roarDamage(int might, double growth) {
      return 0.5F * SarcophageEvents.grainDamage(might, growth);
   }

   public static float obeliskDamage(int might, double growth) {
      return SarcophageEvents.crashDamage(might, growth);
   }

   public static float lashDamage(double growth) {
      return SarcophageEvents.tempestStrike(growth);
   }

   public static float sinkGrind(int might, double growth) {
      return SarcophageEvents.abrasion(might, growth);
   }

   public static float clawDamage(int might) {
      return SarcophageEvents.grabOpen(might);
   }

   public static float maulDamage(int might) {
      return SarcophageEvents.holdBite(might);
   }

   private PhageEvents() {
   }

   public static boolean unsealed(Entity e) {
      return e != null && e.getTags().contains("unrp.sc_phage");
   }

   private static double scaleOf(Entity e) {
      return Math.max(0.25D, (double)e.getBbWidth() / 0.6D);
   }

   static Vec3 modelPoint(ServerPlayer p, double[] px) {
      double k = scaleOf(p) * 0.9375D / 16.0D;
      double lx = -px[0] * k;
      double ly = (24.016D - px[1]) * k;
      double lz = px[2] * k;
      double th = Math.toRadians(180.0D - (double)p.yBodyRot);
      double wx = lx * Math.cos(th) + lz * Math.sin(th);
      double wz = -lx * Math.sin(th) + lz * Math.cos(th);
      return p.position().add(wx, ly, wz);
   }

   private static void scale(ServerPlayer player, double s, double motion) {
      if (player.getServer() != null) {
         player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack().withSuppressedOutput(), "scale set pehkui:base " + s + " " + player.getName().getString());
         player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack().withSuppressedOutput(), "scale set pehkui:motion " + motion + " " + player.getName().getString());
      }
   }

   private static BlockParticleOption grains() {
      return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState());
   }

   private static BlockParticleOption stone() {
      return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SANDSTONE.defaultBlockState());
   }

   private static void sound(ServerLevel level, Entity at, String id, float vol, float pitch) {
      level.playSound((Player)null, at.blockPosition(), CastFxEvents.sound(id), SoundSource.PLAYERS, vol, pitch);
   }

   private static boolean onCd(ServerPlayer player, int move, long now) {
      Map<Integer, Long> m = (Map)cds.get(player.getUUID());
      Long at = m == null ? null : (Long)m.get(move);
      return at != null && now < at;
   }

   private static long cdLeft(ServerPlayer player, int move, long now) {
      Map<Integer, Long> m = (Map)cds.get(player.getUUID());
      Long at = m == null ? null : (Long)m.get(move);
      return at == null ? 0L : Math.max(0L, at - now);
   }

   private static void setCd(ServerPlayer player, int move, long until) {
      ((Map)cds.computeIfAbsent(player.getUUID(), (k) -> new HashMap())).put(move, until);
   }

   private static boolean busy(ServerPlayer player, PhageEvents.Beast b, long now) {
      if (b.move != 0) {
         SarcophageEvents.bar(player, "§7The beast is still moving.");
         return true;
      } else {
         return false;
      }
   }

   private static boolean ready(ServerPlayer player, int move, long now, String what) {
      if (onCd(player, move, now)) {
         long left = (cdLeft(player, move, now) + 19L) / 20L;
         SarcophageEvents.bar(player, "§7" + what + " again in " + left + "s.");
         return false;
      } else {
         return true;
      }
   }

   private static void start(ServerPlayer player, PhageEvents.Beast b, int move, int length, long now, double extra) {
      b.move = move;
      b.start = now;
      b.length = length;
      OriginLooks.phage(player, move, length, extra);
   }

   public static boolean tryUnseal(ServerPlayer player, ServerLevel level) {
      if (unsealed(player)) {
         return false;
      } else {
         long now = level.getGameTime();
         Long cd = (Long)sealCd.get(player.getUUID());
         if (cd != null && now < cd) {
            SarcophageEvents.bar(player, "§7The seal still holds. It breaks again in " + (cd - now + 19L) / 20L + "s.");
            return true;
         } else if (SarcophageEvents.tailsOf(player) < 4) {
            SarcophageEvents.bar(player, "§7Only a four-tailed spine can break the seal.");
            return true;
         } else {
            int reserve = SarcophageEvents.score(player, "unrp.sc_reserve");
            if (reserve < 60) {
               SarcophageEvents.bar(player, "§7The seal needs 60 Sand Reserve to break.");
               return true;
            } else {
               SarcophageEvents.prepareUnseal(player, level);
               player.addTag("unrp.sc_phage");
               scale(player, 2.5D, 0.28D);
               PhageEvents.Beast b = new PhageEvents.Beast();
               beasts.put(player.getUUID(), b);
               start(player, b, 9, 32, now, 0.0D);
               Vec3 base = player.position();

               for(int i = 0; i < 20; ++i) {
                  SarcophageEvents.sandPuff(level, base.add(0.0D, (double)i * 0.4D, 0.0D), 10, 1.2D, 0.18D);
               }

               Spectacle.shockwave(level, 14594656, base, 6.0D);
               Spectacle.pillar(level, grains(), base, 9.0D);
               Fx.ring(level, 14594656, base.x, base.y + 0.1D, base.z, 4.0D, 18);
               Fx.ring(level, 9848872, base.x, base.y + 2.0D, base.z, 3.0D, 14);
               sound(level, player, "minecraft:entity.warden.emerge", 1.4F, 0.6F);
               sound(level, player, "minecraft:block.sand.break", 1.5F, 0.4F);
               sound(level, player, "minecraft:entity.warden.roar", 1.0F, 0.5F);
               SarcophageEvents.bar(player, "§6The seal breaks. §eThe Phage rises.");
               return true;
            }
         }
      }
   }

   public static void reseal(ServerPlayer player, ServerLevel level, boolean keepHalf, String why) {
      if (player.getTags().remove("unrp.sc_phage")) {
         player.removeTag("unrp.sc_burrow");
         PhageEvents.Beast b = (PhageEvents.Beast)beasts.remove(player.getUUID());
         if (b != null) {
            letGo(player, level, b, false);
         }

         if (keepHalf) {
            int reserve = SarcophageEvents.score(player, "unrp.sc_reserve");
            SarcophageEvents.setScore(player, "unrp.sc_reserve", reserve / 2);
         }

         scale(player, 1.0D, 1.0D);
         sealCd.put(player.getUUID(), level.getGameTime() + 3600L);
         OriginLooks.phage(player, 0, 0, 0.0D);
         Vec3 base = player.position();
         SarcophageEvents.sandPuff(level, base.add(0.0D, 1.5D, 0.0D), 60, 1.6D, 0.14D);
         Fx.burst(level, Fx.drop(14594656), base.add(0.0D, 1.0D, 0.0D), 30, 1.2D, 0.1D);
         sound(level, player, "minecraft:block.sand.fall", 1.4F, 0.5F);
         sound(level, player, "minecraft:block.sand.place", 1.0F, 0.6F);
         if (why != null) {
            SarcophageEvents.bar(player, why);
         }

      }
   }

   public static void grabKey(ServerPlayer player, ServerLevel level, boolean sneak) {
      PhageEvents.Beast b = (PhageEvents.Beast)beasts.computeIfAbsent(player.getUUID(), (k) -> new PhageEvents.Beast());
      long now = level.getGameTime();
      if (sneak) {
         spit(player, level, b, now);
      } else {
         devour(player, level, b, now);
      }

   }

   public static void lungeKey(ServerPlayer player, ServerLevel level, boolean sneak) {
      PhageEvents.Beast b = (PhageEvents.Beast)beasts.computeIfAbsent(player.getUUID(), (k) -> new PhageEvents.Beast());
      long now = level.getGameTime();
      if (b.move == 2) {
         erupt(player, level, b, now);
      } else {
         if (sneak) {
            sink(player, level, b, now);
         } else {
            burrow(player, level, b, now);
         }

      }
   }

   public static void sprayKey(ServerPlayer player, ServerLevel level, boolean sneak) {
      PhageEvents.Beast b = (PhageEvents.Beast)beasts.computeIfAbsent(player.getUUID(), (k) -> new PhageEvents.Beast());
      long now = level.getGameTime();
      if (sneak) {
         obelisk(player, level, b, now);
      } else {
         roar(player, level, b, now);
      }

   }

   public static void shroudKey(ServerPlayer player, ServerLevel level, boolean sneak) {
      PhageEvents.Beast b = (PhageEvents.Beast)beasts.computeIfAbsent(player.getUUID(), (k) -> new PhageEvents.Beast());
      long now = level.getGameTime();
      if (sneak) {
         reseal(player, level, true, "§6The sand settles back into the seal. §7You keep half the reserve.");
      } else {
         lash(player, level, b, now);
      }

   }

   private static LivingEntity prey(ServerPlayer player, ServerLevel level, double range) {
      Vec3 look = player.getLookAngle();
      Vec3 flat = new Vec3(look.x, 0.0D, look.z);
      if (flat.lengthSqr() < 1.0E-4D) {
         flat = Vec3.directionFromRotation(0.0F, player.getYRot());
      }

      flat = flat.normalize();
      Vec3 from = player.position();
      double reach = range + (double)player.getBbWidth() * 0.5D;
      LivingEntity best = null;
      double bestScore = Double.MAX_VALUE;
      AABB box = player.getBoundingBox().inflate(reach + 1.0D, 2.0D, reach + 1.0D);

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, (le) -> le != player && le.isAlive() && OriginStrike.fair(player, le))) {
         Vec3 to = e.position().subtract(from);
         Vec3 tf = new Vec3(to.x, 0.0D, to.z);
         double d = tf.length();
         if (!(d > reach + (double)e.getBbWidth() * 0.5D) && !(d < 0.001D)) {
            double cos = tf.scale(1.0D / d).dot(flat);
            if (!(cos < 0.64D) || !(d > (double)player.getBbWidth())) {
               double off = d * Math.sqrt(Math.max(0.0D, 1.0D - cos * cos));
               double score = d + off * 1.5D;
               if (score < bestScore) {
                  bestScore = score;
                  best = e;
               }
            }
         }
      }

      return best;
   }

   private static LivingEntity preyOf(ServerLevel level, PhageEvents.Beast b) {
      if (b.prey == null) {
         return null;
      } else {
         Entity e = level.getEntity(b.prey);
         LivingEntity var10000;
         if (e instanceof LivingEntity) {
            LivingEntity live = (LivingEntity)e;
            var10000 = live;
         } else {
            var10000 = null;
         }

         return var10000;
      }
   }

   private static void devour(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, long now) {
      if (!busy(player, b, now) && ready(player, 1, now, "The maw opens")) {
         LivingEntity target = prey(player, level, 6.0D);
         setCd(player, 1, now + (long)(target == null ? 40 : 80));
         b.prey = target == null ? null : target.getUUID();
         b.preyPlayer = target instanceof ServerPlayer;
         b.preyBoss = target != null && !b.preyPlayer && target.getMaxHealth() >= 100.0F;
         b.held = false;
         b.escaped = false;
         start(player, b, 1, 38, now, 0.0D);
         double push;
         Vec3 dir;
         if (target != null) {
            Vec3 to = target.position().subtract(player.position());
            dir = new Vec3(to.x, 0.0D, to.z);
            double d = dir.length();
            dir = d < 0.001D ? Vec3.ZERO : dir.scale(1.0D / d);
            push = Math.max(0.4D, Math.min(1.3D, d * 0.18D));
         } else {
            Vec3 look = player.getLookAngle();
            dir = new Vec3(look.x, 0.0D, look.z);
            dir = dir.lengthSqr() < 1.0E-4D ? Vec3.ZERO : dir.normalize();
            push = 0.9D;
         }

         player.setDeltaMovement(player.getDeltaMovement().add(dir.x * push, 0.12D, dir.z * push));
         player.hurtMarked = true;
         Vec3 base = player.position();
         SarcophageEvents.sandPuff(level, base.add(0.0D, 0.3D, 0.0D), 30, 1.0D, 0.15D);
         Fx.ring(level, 14594656, base.x, base.y + 0.1D, base.z, 2.2D, 10);
         sound(level, player, "minecraft:entity.ravager.attack", 1.2F, 0.6F);
         sound(level, player, "minecraft:block.sand.break", 1.0F, 0.6F);
      }
   }

   private static void letGo(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, boolean spit) {
      LivingEntity prey = preyOf(level, b);
      boolean wasHeld = b.held;
      b.held = false;
      b.prey = null;
      if (prey != null && wasHeld) {
         if (prey instanceof ServerPlayer) {
            ServerPlayer vp = (ServerPlayer)prey;
            StruggleEvents.end(vp, 0);
         }

         if (spit && prey.isAlive()) {
            Vec3 look = player.getLookAngle();
            OriginStrike.shove(prey, look.x * 1.2D, 0.45D + Math.max(0.0D, look.y), look.z * 1.2D);
            SarcophageEvents.sandPuff(level, prey.position().add(0.0D, (double)prey.getBbHeight() * 0.5D, 0.0D), 24, 0.6D, 0.2D);
         }

      }
   }

   private static void feed(ServerPlayer player, ServerLevel level) {
      int cap = SarcophageEvents.capOf(player);
      int reserve = SarcophageEvents.score(player, "unrp.sc_reserve");
      SarcophageEvents.setScore(player, "unrp.sc_reserve", Math.min(cap, reserve + 10));
      sound(level, player, "minecraft:entity.generic.eat", 1.2F, 0.5F);
      sound(level, player, "minecraft:entity.player.burp", 0.8F, 0.5F);
   }

   private static void devourTick(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, int t, long now) {
      label92: {
         LivingEntity prey = preyOf(level, b);
         if (t == 12) {
            double reach = 9.0D;
            if (prey == null || !prey.isAlive() || !OriginStrike.fair(player, prey) || (double)prey.distanceTo(player) > reach || TimeWeft.everpresent(prey)) {
               break label92;
            }

            if (prey instanceof ServerPlayer) {
               ServerPlayer vp_0 = (ServerPlayer)prey;
               if (vp_0.isCreative() || vp_0.isSpectator()) {
                  break label92;
               }
            }

            b.held = true;
            Vec3 at_0 = prey.position().add(0.0D, (double)prey.getBbHeight() * 0.5D, 0.0D);
            Fx.shell(level, Fx.drop(14594656), at_0, 0.7D, 18, -0.12D);
            SarcophageEvents.sandPuff(level, at_0, 24, 0.5D, 0.14D);
            sound(level, prey, "minecraft:block.sand.break", 1.2F, 0.7F);
            if (b.preyPlayer && prey instanceof ServerPlayer) {
               ServerPlayer vp = (ServerPlayer)prey;
               b.holdUntil = now + 60L;
               b.length = 86;
               UUID caster = player.getUUID();
               StruggleEvents.begin(vp, 8, 0, 60, () -> {
                  PhageEvents.Beast bb = (PhageEvents.Beast)beasts.get(caster);
                  if (bb != null) {
                     bb.escaped = true;
                  }

               });
               SarcophageEvents.bar(vp, "§6The maw closes on you. §eBreak free!");
               OriginLooks.phage(player, 10, 60, (double)b.length);
            }
         }

         if (!b.held) {
            return;
         }

         if (prey != null && prey.isAlive() && !b.escaped && !TimeWeft.everpresent(prey) && !((double)prey.distanceTo(player) > 6.0D * scaleOf(player))) {
            Vec3 point;
            if (t < 20) {
               double k = (double)(t - 12) / 8.0D;
               Vec3 a = modelPoint(player, TAKE);
               point = a.add(modelPoint(player, FEED).subtract(a).scale(Math.max(0.0D, Math.min(1.0D, k))));
            } else {
               point = modelPoint(player, FEED);
            }

            Vec3 center = prey.position().add(0.0D, (double)prey.getBbHeight() * 0.5D, 0.0D);
            Vec3 pull = point.subtract(center).scale(0.5D);
            if (pull.length() > 1.4D) {
               pull = pull.normalize().scale(1.4D);
            }

            prey.setDeltaMovement(pull);
            prey.hurtMarked = true;
            prey.fallDistance = 0.0F;
            prey.addEffect(new MobEffectInstance(MobEffects.JUMP, 8, 128, false, false));
            int might = SarcophageEvents.mightOf(player);
            double growth = SarcophageEvents.growthOf(player);
            if (t == 21) {
               Vec3 maw = modelPoint(player, MAW);
               Fx.burst(level, Fx.drop(9848872), maw, 14, 0.5D, 0.1D);
               SarcophageEvents.sandPuff(level, maw, 20, 0.6D, 0.16D);
               sound(level, player, "minecraft:entity.warden.attack_impact", 1.0F, 0.6F);
               if (!b.preyPlayer && !b.preyBoss) {
                  SarcophageEvents.harvestKill(player, level, prey, now);
                  feed(player, level);
                  b.held = false;
                  b.prey = null;
                  return;
               }

               if (b.preyBoss) {
                  OriginStrike.hitFresh(prey, level.damageSources().playerAttack(player), devourBossDamage(might, growth));
                  feed(player, level);
                  letGo(player, level, b, true);
                  return;
               }

               OriginStrike.hitFresh(prey, level.damageSources().playerAttack(player), maulDamage(might));
               feed(player, level);
            }

            if (b.preyPlayer && t > 21 && (t - 21) % 10 == 0) {
               OriginStrike.hitFresh(prey, level.damageSources().playerAttack(player), maulDamage(might));
               SarcophageEvents.sandPuff(level, center, 10, 0.4D, 0.1D);
               sound(level, prey, "minecraft:block.sand.hit", 0.9F, 0.5F);
            }

            if (b.preyPlayer && now >= b.holdUntil) {
               letGo(player, level, b, true);
            }

            return;
         }

         letGo(player, level, b, false);
         cutDevour(player, b, t, 14);
         return;
      }

      b.prey = null;
      Vec3 at = modelPoint(player, TAKE);
      SarcophageEvents.sandPuff(level, at, 16, 0.6D, 0.12D);
   }

   private static void cutDevour(ServerPlayer player, PhageEvents.Beast b, int t, int rest) {
      if (b.move == 1 && b.length > t + rest) {
         b.length = t + rest;
         OriginLooks.phage(player, 10, Math.max(1, t - 12), (double)b.length);
      }
   }

   private static void spit(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, long now) {
      if (b.held) {
         letGo(player, level, b, true);
         cutDevour(player, b, (int)(now - b.start), 4);
         b.move = 0;
      }

      if (!busy(player, b, now) && ready(player, 8, now, "The sand wells up")) {
         setCd(player, 8, now + 160L);
         start(player, b, 8, 22, now, 0.0D);
      }
   }

   private static void disgorge(ServerPlayer player, ServerLevel level) {
      Vec3 maw = modelPoint(player, MAW);
      Vec3 dir = player.getLookAngle();
      int might = SarcophageEvents.mightOf(player);
      float dmg = disgorgeDamage(might, SarcophageEvents.growthOf(player));

      for(LivingEntity e : OriginStrike.foesNear(level, player, maw, 10.0D)) {
         Vec3 to = e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D).subtract(maw);
         double len = to.length();
         if (!(len < 0.001D) && !(to.scale(1.0D / len).dot(dir) < 0.82D)) {
            OriginStrike.hitFresh(e, level.damageSources().playerAttack(player), dmg);
            OriginStrike.shove(e, dir.x * 1.2D, 0.35D + Math.max(0.0D, dir.y) * 0.6D, dir.z * 1.2D);
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, true));
         }
      }

      Fx.cone(level, Fx.drop(14594656), maw, dir, 0.6D, 70, 0.9D);
      Fx.cone(level, Fx.ember(9848872), maw, dir, 0.5D, 20, 0.7D);

      for(int i = 1; i <= 10; ++i) {
         Vec3 p = maw.add(dir.scale((double)i));
         level.sendParticles(grains(), p.x, p.y, p.z, 6, 0.25D * (double)i * 0.15D + 0.2D, 0.2D, 0.25D * (double)i * 0.15D + 0.2D, 0.1D);
      }

      sound(level, player, "minecraft:entity.ravager.roar", 1.0F, 0.8F);
      sound(level, player, "minecraft:block.sand.break", 1.4F, 0.5F);
   }

   private static void burrow(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, long now) {
      if (!busy(player, b, now) && ready(player, 2, now, "It can dive")) {
         if (!player.onGround()) {
            SarcophageEvents.bar(player, "§7The beast must stand on the ground to dive.");
         } else {
            player.addTag("unrp.sc_burrow");
            start(player, b, 2, 92, now, 0.0D);
            Vec3 base = player.position();
            Spectacle.pillar(level, grains(), base, 5.0D);
            SarcophageEvents.sandPuff(level, base.add(0.0D, 0.5D, 0.0D), 50, 1.4D, 0.2D);
            sound(level, player, "minecraft:entity.warden.dig", 1.4F, 0.6F);
            sound(level, player, "minecraft:block.sand.break", 1.4F, 0.5F);
         }
      }
   }

   private static void burrowTick(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, int t, long now) {
      if (t >= 12) {
         player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 2, false, false));
         if (t % 2 == 0) {
            Vec3 base = player.position();
            level.sendParticles(grains(), base.x, base.y + 0.15D, base.z, 10, 1.2D, 0.1D, 1.2D, 0.12D);
            Vec3 look = player.getLookAngle();
            Vec3 side = new Vec3(-look.z, 0.0D, look.x);

            for(int k = -2; k <= 2; ++k) {
               Vec3 at = base.add(side.scale((double)k * 1.3D)).add(look.x * -1.5D, 0.3D, look.z * -1.5D);
               level.sendParticles(grains(), at.x, at.y, at.z, 0, -look.x * 0.2D, 0.35D, -look.z * 0.2D, 1.0D);
            }
         }

         if (t % 12 == 0) {
            sound(level, player, "minecraft:block.sand.step", 1.4F, 0.4F);
         }

         if (t >= 91) {
            erupt(player, level, b, now);
         }

      }
   }

   private static void erupt(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, long now) {
      player.removeTag("unrp.sc_burrow");
      setCd(player, 2, now + 200L);
      start(player, b, 3, 32, now, 0.0D);
      player.setDeltaMovement(player.getDeltaMovement().x, 0.55D, player.getDeltaMovement().z);
      player.hurtMarked = true;
   }

   private static void eruptHit(ServerPlayer player, ServerLevel level) {
      Vec3 base = player.position();
      Vec3 heart = base.add(0.0D, 1.0D, 0.0D);
      int might = SarcophageEvents.mightOf(player);
      float dmg = eruptDamage(might, SarcophageEvents.growthOf(player));
      double reach = 4.0D + (double)player.getBbWidth() * 0.5D;

      for(LivingEntity e : OriginStrike.foesNear(level, player, heart, reach)) {
         OriginStrike.hitFresh(e, level.damageSources().playerAttack(player), dmg);
         Vec3 out = new Vec3(e.getX() - base.x, 0.0D, e.getZ() - base.z);
         Vec3 d = out.lengthSqr() < 1.0E-4D ? Vec3.ZERO : out.normalize();
         OriginStrike.shove(e, d.x * 0.6D, 1.0D, d.z * 0.6D);
      }

      Spectacle.shockwave(level, 14594656, base, reach);
      Spectacle.pillar(level, grains(), base, 7.0D);
      Spectacle.spikes(level, grains(), base, 14, reach, 2.4D);
      SarcophageEvents.sandPuff(level, base.add(0.0D, 1.0D, 0.0D), 70, 2.0D, 0.3D);
      sound(level, player, "minecraft:entity.generic.explode", 1.2F, 0.6F);
      sound(level, player, "minecraft:entity.warden.emerge", 1.2F, 0.8F);
   }

   private static void sink(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, long now) {
      if (!busy(player, b, now) && ready(player, 7, now, "The quicksand stirs")) {
         setCd(player, 7, now + 240L);
         start(player, b, 7, 24, now, 0.0D);
         b.sinkAt = player.position();
         b.sinkDim = level.dimension();
         b.sinkFrom = now + 10L;
         b.sinkUntil = now + 10L + 120L;
      }
   }

   private static void sinkTick(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, long now) {
      if (b.sinkAt != null && now >= b.sinkFrom) {
         if (now < b.sinkUntil && level.dimension().equals(b.sinkDim)) {
            Vec3 c = b.sinkAt;
            if (now == b.sinkFrom) {
               Spectacle.shockwave(level, 14594656, c, 3.5D);
               sound(level, player, "minecraft:block.sand.break", 1.4F, 0.4F);
               sound(level, player, "minecraft:entity.ravager.step", 1.2F, 0.5F);
            }

            int might = SarcophageEvents.mightOf(player);
            float grind = sinkGrind(might, SarcophageEvents.growthOf(player));

            for(LivingEntity e : OriginStrike.foesNear(level, player, c.add(0.0D, 0.5D, 0.0D), 5.0D)) {
               double dx = e.getX() - c.x;
               double dz = e.getZ() - c.z;
               if (!(dx * dx + dz * dz > 12.25D) && !(Math.abs(e.getY() - c.y) > 2.0D)) {
                  e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 3, false, false));
                  e.addEffect(new MobEffectInstance(MobEffects.JUMP, 10, 128, false, false));
                  if (!TimeWeft.everpresent(e)) {
                     Vec3 v = e.getDeltaMovement();
                     e.setDeltaMovement(v.x * 0.5D, Math.min(v.y, -0.05D), v.z * 0.5D);
                     e.hurtMarked = true;
                  }

                  if ((now - b.sinkFrom) % 20L == 0L) {
                     OriginStrike.hitFresh(e, level.damageSources().playerAttack(player), grind);
                  }
               }
            }

            if (now % 2L == 0L) {
               for(int i = 0; i < 14; ++i) {
                  double a = level.random.nextDouble() * Math.PI * 2.0D;
                  double r = Math.sqrt(level.random.nextDouble()) * 3.5D;
                  level.sendParticles(grains(), c.x + Math.cos(a) * r, c.y + 0.1D, c.z + Math.sin(a) * r, 1, 0.05D, 0.02D, 0.05D, 0.02D);
               }
            }

            if (now % 10L == 0L) {
               Fx.ring(level, 14594656, c.x, c.y + 0.05D, c.z, 3.5D, 10);
            }

         } else {
            b.sinkAt = null;
         }
      }
   }

   private static void roar(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, long now) {
      if (!busy(player, b, now) && ready(player, 4, now, "The breath returns")) {
         setCd(player, 4, now + 180L);
         start(player, b, 4, 50, now, 0.0D);
         sound(level, player, "minecraft:entity.warden.sonic_charge", 1.0F, 0.6F);
      }
   }

   private static void breath(ServerPlayer player, ServerLevel level, boolean first) {
      Vec3 maw = modelPoint(player, MAW);
      Vec3 dir = player.getLookAngle();
      int might = SarcophageEvents.mightOf(player);
      float dmg = roarDamage(might, SarcophageEvents.growthOf(player));

      for(LivingEntity e : OriginStrike.foesNear(level, player, maw, 16.0D)) {
         Vec3 to = e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D).subtract(maw);
         double len = to.length();
         if (!(len < 0.001D) && !(to.scale(1.0D / len).dot(dir) < 0.87D)) {
            if (e instanceof Player) {
               Player p_0 = (Player)e;
               if (p_0.isBlocking()) {
                  p_0.disableShield(true);
               }
            }

            OriginStrike.hitFresh(e, level.damageSources().playerAttack(player), dmg);
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, true));
            OriginStrike.shove(e, dir.x * 0.25D, 0.05D, dir.z * 0.25D);
         }
      }

      Fx.cone(level, Fx.drop(14594656), maw, dir, 0.5D, 40, 1.3D);

      for(int i = 2; i <= 16; i += 2) {
         Vec3 p = maw.add(dir.scale((double)i));
         double w = 0.3D + (double)i * 0.12D;
         level.sendParticles(grains(), p.x, p.y, p.z, 5, w, w * 0.6D, w, 0.15D);
      }

      if (first) {
         sound(level, player, "minecraft:entity.ravager.roar", 1.6F, 0.5F);
         sound(level, player, "minecraft:entity.warden.roar", 1.2F, 0.7F);
      }

      sound(level, player, "minecraft:block.sand.break", 0.9F, 0.6F);
   }

   private static void obelisk(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, long now) {
      if (!busy(player, b, now) && ready(player, 6, now, "The obelisks rise")) {
         Vec3 eye = player.getEyePosition();
         Vec3 look = player.getLookAngle();
         HitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(24.0D)), Block.COLLIDER, Fluid.NONE, player));
         Vec3 spot = hit.getType() == Type.MISS ? eye.add(look.scale(24.0D)) : hit.getLocation();
         BlockPos pos = BlockPos.containing(spot);

         for(int i = 0; i < 24 && pos.getY() > level.getMinBuildHeight() && level.getBlockState(pos.below()).isAir(); ++i) {
            pos = pos.below();
         }

         b.obeliskAt = new Vec3(spot.x, (double)pos.getY(), spot.z);
         setCd(player, 6, now + 200L);
         start(player, b, 6, 28, now, 0.0D);
         Spectacle.warn(level, 14594656, b.obeliskAt, 3.2D);
      }
   }

   private static void obeliskHit(ServerPlayer player, ServerLevel level, PhageEvents.Beast b) {
      if (b.obeliskAt != null) {
         Vec3 c = b.obeliskAt;
         double yaw = Math.toRadians((double)player.getYRot());
         double[][] offs = new double[][]{{0.0D, 0.0D}, {-2.2D, 1.6D}, {2.2D, 1.6D}};
         int might = SarcophageEvents.mightOf(player);
         float dmg = obeliskDamage(might, SarcophageEvents.growthOf(player));

         for(double[] o : offs) {
            double ox = o[0] * Math.cos(yaw) - o[1] * Math.sin(yaw);
            double oz = o[0] * Math.sin(yaw) + o[1] * Math.cos(yaw);
            Vec3 at = c.add(ox, 0.0D, oz);
            Spectacle.pillar(level, stone(), at, 5.0D);
            Spectacle.pillar(level, grains(), at, 3.5D);
            Spectacle.shockwave(level, 14594656, at, 2.2D);
            Vec3 a = at.add(0.0D, 0.2D, 0.0D);
            Vec3 top = at.add(0.0D, 5.0D, 0.0D);

            for(LivingEntity e : OriginStrike.foesAlong(level, player, a, top, 1.8D)) {
               OriginStrike.hitFresh(e, level.damageSources().playerAttack(player), dmg);
               Vec3 out = new Vec3(e.getX() - at.x, 0.0D, e.getZ() - at.z);
               Vec3 d = out.lengthSqr() < 1.0E-4D ? Vec3.ZERO : out.normalize();
               OriginStrike.shove(e, d.x * 0.4D, 1.1D, d.z * 0.4D);
            }
         }

         level.playSound((Player)null, BlockPos.containing(c), CastFxEvents.sound("minecraft:block.stone.break"), SoundSource.PLAYERS, 1.6F, 0.5F);
         level.playSound((Player)null, BlockPos.containing(c), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 1.0F, 0.7F);
         b.obeliskAt = null;
      }
   }

   private static void lash(ServerPlayer player, ServerLevel level, PhageEvents.Beast b, long now) {
      if (!busy(player, b, now) && ready(player, 5, now, "The tails coil")) {
         setCd(player, 5, now + 120L);
         start(player, b, 5, 42, now, 0.0D);
         player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 42, 3, false, false));
      }
   }

   private static void lashHit(ServerPlayer player, ServerLevel level, boolean last) {
      double reach = 4.48D * scaleOf(player);
      Vec3 base = player.position();
      Vec3 heart = base.add(0.0D, 1.0D, 0.0D);
      float dmg = lashDamage(SarcophageEvents.growthOf(player));
      boolean any = false;

      for(LivingEntity e : OriginStrike.foesNear(level, player, heart, reach)) {
         OriginStrike.hitFresh(e, level.damageSources().playerAttack(player), dmg);
         Vec3 out = new Vec3(e.getX() - base.x, 0.0D, e.getZ() - base.z);
         Vec3 d = out.lengthSqr() < 1.0E-4D ? Vec3.ZERO : out.normalize();
         if (last) {
            OriginStrike.shove(e, d.x * 1.4D, 0.5D, d.z * 1.4D);
         } else {
            OriginStrike.shove(e, d.x * 0.15D, 0.05D, d.z * 0.15D);
         }

         SarcophageEvents.sandPuff(level, e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D), 8, 0.3D, 0.12D);
         any = true;
      }

      Fx.ring(level, 14594656, base.x, base.y + 1.2D, base.z, reach, 12);
      level.sendParticles(grains(), base.x, base.y + 1.0D, base.z, 30, reach * 0.5D, 0.6D, reach * 0.5D, 0.1D);
      sound(level, player, "minecraft:entity.player.attack.sweep", 1.4F, any ? 0.5F : 0.7F);
      if (last) {
         sound(level, player, "minecraft:entity.warden.attack_impact", 1.0F, 0.6F);
      }

   }

   public static void tick(ServerPlayer player, ServerLevel level) {
      PhageEvents.Beast b = (PhageEvents.Beast)beasts.computeIfAbsent(player.getUUID(), (k) -> new PhageEvents.Beast());
      long now = level.getGameTime();
      if (player.tickCount % 20 == 0) {
         int reserve = SarcophageEvents.score(player, "unrp.sc_reserve");
         if (reserve < 3) {
            reseal(player, level, false, "§7The sand is spent. The seal closes.");
            return;
         }

         SarcophageEvents.setScore(player, "unrp.sc_reserve", reserve - 3);
      }

      if (player.onGround() && b.move != 2) {
         Vec3 v = player.getDeltaMovement();
         b.stride += Math.sqrt(v.x * v.x + v.z * v.z);
         if (b.stride > 0.9D * scaleOf(player)) {
            b.stride = 0.0D;
            Vec3 at = player.position();
            level.sendParticles(grains(), at.x, at.y + 0.1D, at.z, 18, 0.9D, 0.05D, 0.9D, 0.12D);
            sound(level, player, "minecraft:entity.ravager.step", 1.0F, 0.5F);
         }
      }

      if (b.move != 0) {
         int t = (int)(now - b.start);
         switch (b.move) {
            case 1:
               devourTick(player, level, b, t, now);
               break;
            case 2:
               burrowTick(player, level, b, t, now);
               break;
            case 3:
               if (t == 4) {
                  eruptHit(player, level);
               }
               break;
            case 4:
               if (t >= 10 && t <= 40 && (t - 10) % 5 == 0) {
                  breath(player, level, t == 10);
               }
               break;
            case 5:
               for(int i = 0; i < LASH_HITS.length; ++i) {
                  if (t == LASH_HITS[i]) {
                     lashHit(player, level, i == LASH_HITS.length - 1);
                  }
               }
               break;
            case 6:
               if (t == 13) {
                  obeliskHit(player, level, b);
               }
            case 7:
            default:
               break;
            case 8:
               if (t == 4) {
                  disgorge(player, level);
               }
               break;
            case 9:
               if (t == 6) {
                  Vec3 base = player.position();
                  Spectacle.shockwave(level, 14594656, base, 5.0D);
                  sound(level, player, "minecraft:entity.generic.explode", 0.8F, 0.6F);
               }
         }

         if (b.move != 0 && now - b.start >= (long)b.length) {
            if (b.held) {
               letGo(player, level, b, true);
            }

            b.move = 0;
         }
      }

      sinkTick(player, level, b, now);
   }

   public static float absorb(ServerPlayer player, float amount) {
      int reserve = SarcophageEvents.score(player, "unrp.sc_reserve");
      if (reserve > 0 && !(amount <= 0.0F)) {
         float half = amount * 0.5F;
         int cost = (int)Math.ceil((double)half);
         if (cost > reserve) {
            cost = reserve;
            half = (float)reserve;
         }

         SarcophageEvents.setScore(player, "unrp.sc_reserve", reserve - cost);
         Level var6 = player.level();
         if (var6 instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)var6;
            SarcophageEvents.sandPuff(level, player.position().add(0.0D, (double)player.getBbHeight() * 0.5D, 0.0D), 6, 0.6D, 0.08D);
         }

         return amount - half;
      } else {
         return amount;
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onAttacked(LivingAttackEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (player.getTags().contains("unrp.sc_burrow")) {
            if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
               return;
            }

            event.setCanceled(true);
            return;
         }
      }

   }

   @SubscribeEvent
   public static void onKnockback(LivingKnockBackEvent event) {
      if (unsealed(event.getEntity())) {
         event.setStrength(event.getStrength() * 0.5F);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
      if (unsealed(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onUseOnBlock(PlayerInteractEvent.RightClickBlock event) {
      if (unsealed(event.getEntity())) {
         event.setUseItem(Result.DENY);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onUseStart(LivingEntityUseItemEvent.Start event) {
      if (unsealed(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      LivingEntity var3 = event.getEntity();
      if (var3 instanceof ServerPlayer player) {
         if (unsealed(player)) {
            Level var4 = player.level();
            if (var4 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var4;
               reseal(player, level, false, (String)null);
            }
         }
      }

   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player level_0 = event.getEntity();
      if (level_0 instanceof ServerPlayer player) {
         if (unsealed(player)) {
            Level var3 = player.level();
            if (var3 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var3;
               reseal(player, level, false, (String)null);
            }
         }

         player.removeTag("unrp.sc_burrow");
         beasts.remove(player.getUUID());
         cds.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      beasts.clear();
      cds.clear();
      sealCd.clear();
   }

   private static final class Beast {
      int move;
      long start;
      int length;
      UUID prey;
      boolean preyPlayer;
      boolean preyBoss;
      boolean held;
      boolean escaped;
      long holdUntil;
      Vec3 sinkAt;
      ResourceKey<Level> sinkDim;
      long sinkFrom;
      long sinkUntil;
      Vec3 obeliskAt;
      double stride;
   }
}
