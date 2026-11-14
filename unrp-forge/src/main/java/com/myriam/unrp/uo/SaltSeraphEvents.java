package com.myriam.unrp.uo;

import com.myriam.unrp.anim.MoveAnim;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags.EntityTypes;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class SaltSeraphEvents {
   public static final double AIM_RANGE = 64.0D;
   public static final int LIFT_TICKS = 6;
   public static final int STUCK_JUMP = -5;
   public static final int JUDGED_EVERY = 10;
   public static final int ROOT_SLOW = 6;
   public static final int SLOW_REFRESH = 10;
   public static final int SYNC_EVERY = 20;
   public static final String HAS = "unrp.has.salt_seraph";
   public static final String ON = "unrp.ss_on";
   public static final String OFF_TRIGGER = "unrp.ss_off_trigger";
   public static final String LANCE_TRIGGER = "unrp.ss_lance_trigger";
   public static final String WALL_TRIGGER = "unrp.ss_wall_trigger";
   public static final String SPIRES_TRIGGER = "unrp.ss_spires_trigger";
   public static final String WAKE_TRIGGER = "unrp.ss_wake_trigger";
   public static final String COPY = "unrp.ss_copy";
   public static final String POSE_WAKE = "unrp.ss_wake";
   public static final String PAUSED = "unrp.ss_paused";
   public static final String STATUE_KEY = "unrp_salt_statue";
   public static final String NOAI_KEY = "unrp_salt_noai";
   public static final String SALT_KEY = "unrp_salt_left";
   public static final String PILLAR_KEY = "unrp_salt_pillar";
   static final String[] TRANSIENT = new String[]{"unrp.ss_wake", "unrp.ss_paused"};
   static final String[] TRIGGERS = new String[]{"unrp.ss_off_trigger", "unrp.ss_lance_trigger", "unrp.ss_wall_trigger", "unrp.ss_spires_trigger", "unrp.ss_wake_trigger"};
   static final String[] REAL_ONLY = new String[]{"unrp.ss_wall_trigger", "unrp.ss_spires_trigger", "unrp.ss_wake_trigger"};
   private static final Vec3 ZERO = Vec3.ZERO;
   private static final ThreadLocal<Boolean> SALTING = ThreadLocal.withInitial(() -> Boolean.FALSE);
   static final Map<UUID, SaltSeraphEvents.Salter> SALTERS = new HashMap();
   static final Map<UUID, SaltSeraphEvents.Salted> SALTED = new HashMap();
   static final List<SaltSeraphEvents.Wall> WALLS = new ArrayList();
   static final Map<ResourceKey<Level>, Set<BlockPos>> OWNED = new HashMap();

   private SaltSeraphEvents() {
   }

   static SaltSeraphEvents.Salter salter(ServerPlayer p) {
      SaltSeraphEvents.Salter s = (SaltSeraphEvents.Salter)SALTERS.get(p.getUUID());
      if (s == null) {
         s = new SaltSeraphEvents.Salter();
         SALTERS.put(p.getUUID(), s);
      }

      return s;
   }

   static boolean noncanonOn(ServerPlayer player) {
      return ConfigBridgeEvents.flag(player.server, "noncanon", false) || OriginsConfig.isModDev(player.getUUID());
   }

   static double[] a(Vec3 v) {
      return new double[]{v.x, v.y, v.z};
   }

   static Vec3 v(double[] d) {
      return new Vec3(d[0], d[1], d[2]);
   }

   static Vec3 mid(LivingEntity e) {
      return e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D);
   }

   static double scaleOf(LivingEntity e) {
      return (double)e.getBbHeight() / 1.8D;
   }

   static boolean undead(LivingEntity e) {
      return e.getMobType() == MobType.UNDEAD;
   }

   static boolean boss(LivingEntity e) {
      return e.getType().is(EntityTypes.BOSSES);
   }

   static boolean ally(ServerPlayer caster, LivingEntity live) {
      if (live == caster) {
         return false;
      } else {
         return com.myriam.unrp.social.SocialService.allied(live, caster) || live instanceof Player && com.myriam.unrp.social.SocialService.sameGroup(caster.server, caster.getUUID(), live.getUUID());
      }
   }

   static boolean body(ServerPlayer p, Entity e) {
      if (e instanceof LivingEntity live) {
         if (live != p && live.isAlive() && !live.isSpectator()) {
            if (!(live instanceof ArmorStand)) {
               if (live instanceof Player) {
                  Player pl = (Player)live;
                  if (pl.isCreative()) {
                     return false;
                  }
               }

               return !TimeWeft.everpresent(live);
            }

            return false;
         }
      }

      return false;
   }

   static int who(ServerPlayer p, LivingEntity e, boolean waking) {
      if (!body(p, e)) {
         return 0;
      } else if (ally(p, e)) {
         return SaltMath.spares(true, waking) ? 0 : 2;
      } else {
         return OriginStrike.fair(p, e) ? 1 : 0;
      }
   }

   public static boolean isStatue(Entity e) {
      SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.get(e.getUUID());
      return st != null && st.statue;
   }

   public static boolean isPillar(Entity e) {
      SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.get(e.getUUID());
      return st != null && st.pillar > 0;
   }

   static boolean set(Entity e) {
      SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.get(e.getUUID());
      return st != null && (st.statue || st.pillar > 0);
   }

   static boolean rooted(Entity e) {
      SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.get(e.getUUID());
      return st != null && st.root > 0;
   }

   public static double saltOf(Entity e) {
      SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.get(e.getUUID());
      return st == null ? 0.0D : st.salt;
   }

   static boolean waking(SaltSeraphEvents.Salter s) {
      return s != null && s.wake > 0;
   }

   private static void sound(ServerLevel level, Vec3 at, String id, float volume, float pitch) {
      level.playSound((Player)null, at.x, at.y, at.z, CastFxEvents.sound(id), SoundSource.PLAYERS, volume, pitch);
   }

   private static void bar(ServerPlayer player, String msg) {
      player.displayClientMessage(Component.literal(msg), true);
   }

   private static int score(ServerPlayer p, String obj) {
      try {
         ServerScoreboard sb = p.server.getScoreboard();
         Objective o = sb.getObjective(obj);
         return o != null && sb.hasPlayerScore(p.getScoreboardName(), o) ? sb.getOrCreatePlayerScore(p.getScoreboardName(), o).getScore() : 0;
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

   private static boolean pay(ServerPlayer p, int cost) {
      int cur = score(p, "unrp.aura");
      if (cur < cost) {
         bar(p, "§cNot enough aura (" + cost + " needed).");
         return false;
      } else {
         setScore(p, "unrp.aura", cur - cost);
         return true;
      }
   }

   private static int cooldown(ServerPlayer p, int base) {
      return Math.max(1, SaltMath.cooldown(base, TrickCooldownEvents.reduction(p)));
   }

   private static String secs(long ticks) {
      return (double)Math.round((double)ticks / 2.0D) / 10.0D + "s";
   }

   private static boolean ready(ServerPlayer p, long readyAt, long now, String name) {
      if (now < readyAt) {
         bar(p, "§7" + name + ": §f" + secs(readyAt - now));
         return false;
      } else {
         return true;
      }
   }

   private static void later(SaltSeraphEvents.Salter s, long due, SaltSeraphEvents.Act act) {
      SaltSeraphEvents.Timed t = new SaltSeraphEvents.Timed();
      t.due = due;
      t.epoch = s.epoch;
      t.act = act;
      s.timed.add(t);
   }

   private static void runTimed(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (!s.timed.isEmpty()) {
         List<SaltSeraphEvents.Timed> due = new ArrayList();
         Iterator<SaltSeraphEvents.Timed> it = s.timed.iterator();

         while(it.hasNext()) {
            SaltSeraphEvents.Timed t_0 = (SaltSeraphEvents.Timed)it.next();
            if (t_0.epoch != s.epoch) {
               it.remove();
            } else if (t_0.due <= now) {
               it.remove();
               due.add(t_0);
            }
         }

         for(SaltSeraphEvents.Timed t : due) {
            if (t.epoch == s.epoch) {
               t.act.run(p, level, s, now);
            }
         }

      }
   }

   private static void fx(ServerPlayer p, int kind, int slot, int target, Vec3 x, Vec3 x2, int age, int value, int bits) {
      SaltFxPacket msg = new SaltFxPacket(p.getId(), kind, slot, target, (float)x.x, (float)x.y, (float)x.z, (float)x2.x, (float)x2.y, (float)x2.z, age, value, bits);
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p), msg);
   }

   private static void fxAt(LivingEntity around, int kind, int target, Vec3 x, Vec3 x2, int value) {
      SaltFxPacket msg = new SaltFxPacket(around.getId(), kind, 0, target, (float)x.x, (float)x.y, (float)x.z, (float)x2.x, (float)x2.y, (float)x2.z, 0, value, 0);
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> around), msg);
   }

   private static boolean ours(MobEffectInstance cur) {
      return !cur.isVisible() && !cur.showIcon() && !cur.isAmbient();
   }

   private static void lift(ServerPlayer p, double vy) {
      MobEffectInstance cur = p.getEffect(MobEffects.LEVITATION);
      if (cur == null || ours(cur)) {
         int amp = SaltMath.liftAmp(vy);
         if (cur == null || cur.getAmplifier() != amp || cur.getDuration() <= 2) {
            if (cur != null) {
               p.removeEffect(MobEffects.LEVITATION);
            }

            p.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 6, amp, false, false, false));
         }
      }
   }

   private static void unlift(ServerPlayer p) {
      MobEffectInstance cur = p.getEffect(MobEffects.LEVITATION);
      if (cur != null && ours(cur)) {
         p.removeEffect(MobEffects.LEVITATION);
      }

   }

   static void salt(ServerPlayer caster, LivingEntity e, double amount) {
      if (e != null && e != caster && e.isAlive() && !(amount <= 0.0D) && !TimeWeft.everpresent(e)) {
         if (e instanceof Player) {
            Player pl = (Player)e;
            if (pl.isCreative() || pl.isSpectator()) {
               return;
            }
         }

         SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.get(e.getUUID());
         if (st == null || !st.statue && st.pillar <= 0) {
            if (st == null) {
               st = new SaltSeraphEvents.Salted(e);
               st.lastY = e.getY();
               SALTED.put(e.getUUID(), st);
            }

            st.salt = SaltMath.saltAdd(st.salt, amount, undead(e), boss(e));
            st.since = 0;
            if (e instanceof ServerPlayer) {
               ServerPlayer vp = (ServerPlayer)e;
               if (SaltMath.struggles(st.salt) && !st.struggling) {
                  struggle(vp, st);
               }

               if (st.salt >= 100.0D) {
                  pillar(vp, st);
               }
            } else if (st.salt >= 100.0D) {
               statue(e, st);
            }

            sync(st, true);
         }
      }
   }

   private static void struggle(ServerPlayer vp, SaltSeraphEvents.Salted st) {
      st.struggling = true;
      StruggleEvents.begin(vp, 14, 6, 72000, SaltMath.struggleDecayPresses(), 1, () -> free(vp, st, false));
   }

   private static void pillar(ServerPlayer vp, SaltSeraphEvents.Salted st) {
      st.salt = 100.0D;
      st.pillar = 160;
      st.pillarAt = vp.position();
      vp.setDeltaMovement(0.0D, Math.min(0.0D, vp.getDeltaMovement().y), 0.0D);
      vp.hurtMarked = true;
      if (vp.isPassenger()) {
         vp.stopRiding();
      }

      ServerLevel level = vp.serverLevel();
      fxAt(vp, 5, vp.getId(), vp.position(), ZERO, (int)Math.round((double)vp.getBbHeight() * 100.0D));
      sound(level, vp.position(), "minecraft:block.bone_block.place", 1.0F, 0.7F);
      sound(level, vp.position(), "minecraft:block.amethyst_block.resonate", 0.8F, 1.4F);
      vp.displayClientMessage(Component.literal("§fA Salt Pillar: §7jump to break out."), true);
   }

   static boolean nativeNoAi(Mob mob) {
      return mob.isNoAi() && !HollowDeepFavors.stone(mob) && !HollowDeepFavors.timeHeld(mob);
   }

   private static void statue(LivingEntity e, SaltSeraphEvents.Salted st) {
      st.salt = 100.0D;
      st.statue = true;
      if (e instanceof Mob mob) {
         mob.getPersistentData().putBoolean("unrp_salt_noai", nativeNoAi(mob));
         mob.setTarget((LivingEntity)null);
         mob.getNavigation().stop();
         mob.setNoAi(true);
         mob.setPersistenceRequired();
         if (mob instanceof Creeper creeper) {
            if (!creeper.isIgnited()) {
               creeper.setSwellDir(-1);
            }
         }
      }

      e.getPersistentData().putBoolean("unrp_salt_statue", true);
      e.setDeltaMovement(0.0D, Math.min(0.0D, e.getDeltaMovement().y), 0.0D);
      e.hurtMarked = true;
      e.clearFire();
      if (e.isPassenger()) {
         e.stopRiding();
      }

      ServerLevel level = (ServerLevel)e.level();
      fxAt(e, 5, e.getId(), e.position(), ZERO, (int)Math.round((double)e.getBbHeight() * 100.0D));
      sound(level, e.position(), "minecraft:block.bone_block.place", 1.0F, 0.6F);
      sound(level, e.position(), "minecraft:block.amethyst_block.resonate", 0.8F, 1.2F);
   }

   static void free(LivingEntity e, SaltSeraphEvents.Salted st, boolean shears) {
      boolean was = st.statue || st.pillar > 0 || st.struggling;
      st.pillar = 0;
      st.pillarAt = null;
      st.root = 0;
      if (st.statue) {
         st.statue = false;
         e.getPersistentData().remove("unrp_salt_statue");
         if (e instanceof Mob) {
            Mob mob = (Mob)e;
            boolean prior = mob.getPersistentData().getBoolean("unrp_salt_noai");
            mob.getPersistentData().remove("unrp_salt_noai");
            mob.setNoAi(prior || HollowDeepFavors.stone(mob) || HollowDeepFavors.timeHeld(mob));
         }
      }

      st.salt = shears ? 0.0D : 40.0D;
      st.since = 0;
      if (st.struggling) {
         st.struggling = false;
         if (e instanceof ServerPlayer) {
            ServerPlayer vp = (ServerPlayer)e;
            StruggleEvents.end(vp, 6);
         }
      }

      if (was) {
         int slow = shears ? 60 : 30;
         e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slow, 1, false, true, true));
         ServerLevel level = (ServerLevel)e.level();
         fxAt(e, 6, e.getId(), e.position(), ZERO, (int)Math.round((double)e.getBbHeight() * 100.0D));
         sound(level, e.position(), "minecraft:block.sand.break", 1.0F, 1.2F);
         sound(level, e.position(), "minecraft:block.glass.break", 0.5F, 1.6F);
      }

      sync(st, true);
   }

   private static int flagsOf(SaltSeraphEvents.Salted st) {
      return (st.statue ? 1 : 0) | (st.pillar > 0 ? 2 : 0) | (st.root > 0 ? 4 : 0);
   }

   private static void sync(SaltSeraphEvents.Salted st, boolean force) {
      int v = (int)Math.ceil(st.salt);
      int flags = flagsOf(st);
      long now = st.e.level().getGameTime();
      if (force || v != st.sentSalt || flags != st.sentFlags || now - st.sentAt >= 20L) {
         st.sentSalt = v;
         st.sentFlags = flags;
         st.sentAt = now;
         com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> st.e), new SaltMarkPacket(st.e.getId(), v, flags));
      }
   }

   private static int wetOf(SaltSeraphEvents.Salted st, long now) {
      LivingEntity e = st.e;
      if (!e.isInWater() && now - st.splashed >= 2L) {
         return !e.level().isRainingAt(e.blockPosition()) && !e.level().isRainingAt(BlockPos.containing(e.getX(), e.getY() + 1.0D, e.getZ())) ? 0 : 2;
      } else {
         return 1;
      }
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         if (!WALLS.isEmpty()) {
            tickWalls();
         }

         if (!SALTED.isEmpty()) {
            List<UUID> gone = new ArrayList();

            for(Map.Entry<UUID, SaltSeraphEvents.Salted> en : SALTED.entrySet()) {
               SaltSeraphEvents.Salted st_0 = (SaltSeraphEvents.Salted)en.getValue();
               LivingEntity e = st_0.e;
               if (!e.isRemoved() && e.isAlive()) {
                  long now = e.level().getGameTime();
                  if (st_0.statue) {
                     tickStatue(e, st_0);
                     sync(st_0, false);
                  } else if (st_0.pillar > 0) {
                     tickPillar(e, st_0);
                     sync(st_0, false);
                  } else {
                     ++st_0.since;
                     if (st_0.salt > 0.0D) {
                        st_0.salt = SaltMath.saltStep(st_0.salt, st_0.since, wetOf(st_0, now));
                     }

                     if (SaltMath.slowed(st_0.salt) && now % 10L == 0L) {
                        MobEffectInstance cur = e.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
                        if (cur == null || cur.getAmplifier() <= 1) {
                           e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 1, true, false, true));
                        }
                     }

                     if (st_0.root > 0) {
                        --st_0.root;
                        Vec3 mv = e.getDeltaMovement();
                        e.setDeltaMovement(0.0D, Math.min(0.0D, mv.y), 0.0D);
                        e.hurtMarked = true;
                     }

                     if (st_0.struggling && SaltMath.struggleDropped(false, st_0.salt)) {
                        st_0.struggling = false;
                        if (e instanceof ServerPlayer) {
                           ServerPlayer vp_0 = (ServerPlayer)e;
                           StruggleEvents.end(vp_0, 6);
                        }
                     }

                     sync(st_0, false);
                     if (st_0.salt <= 0.0D && st_0.root <= 0 && !st_0.struggling) {
                        gone.add((UUID)en.getKey());
                     }
                  }
               } else {
                  gone.add((UUID)en.getKey());
                  if (e instanceof ServerPlayer) {
                     ServerPlayer vp = (ServerPlayer)e;
                     if (st_0.struggling) {
                        StruggleEvents.end(vp, 6);
                     }
                  }
               }
            }

            for(UUID id : gone) {
               SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.remove(id);
               if (st != null) {
                  SaltMarkPacket clear = new SaltMarkPacket(st.e.getId(), 0, 0);
                  if (st.e.isAlive() && !st.e.isRemoved()) {
                     com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> st.e), clear);
                  } else {
                     com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.ALL.noArg(), clear);
                  }
               }
            }

         }
      }
   }

   private static void tickStatue(LivingEntity e, SaltSeraphEvents.Salted st) {
      if (e instanceof Mob mob) {
         if (!mob.isNoAi()) {
            mob.setNoAi(true);
         }
      }

      if (e.isOnFire()) {
         e.clearFire();
      }

      if (e.getAirSupply() < e.getMaxAirSupply()) {
         e.setAirSupply(e.getMaxAirSupply());
      }

      if (!e.onGround() && Math.abs(e.getY() - st.lastY) < 1.0E-4D) {
         st.fall = Math.max(-3.0D, st.fall - 0.08D) * 0.98D;
         e.move(MoverType.SELF, new Vec3(0.0D, st.fall, 0.0D));
      } else if (e.onGround()) {
         st.fall = 0.0D;
      }

      st.lastY = e.getY();
   }

   private static void tickPillar(LivingEntity e, SaltSeraphEvents.Salted st) {
      --st.pillar;
      Vec3 mv = e.getDeltaMovement();
      if (Math.abs(mv.x) + Math.abs(mv.z) > 0.001D) {
         e.setDeltaMovement(0.0D, Math.min(0.0D, mv.y), 0.0D);
         e.hurtMarked = true;
      }

      if (st.pillarAt != null && Math.hypot(e.getX() - st.pillarAt.x, e.getZ() - st.pillarAt.z) > 0.3D) {
         e.teleportTo(st.pillarAt.x, e.getY(), st.pillarAt.z);
      }

      if (e instanceof Player) {
         e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 4, 6, false, false, false));
         e.addEffect(new MobEffectInstance(MobEffects.JUMP, 4, -5, false, false, false));
      }

      if (st.pillar <= 0) {
         free(e, st, false);
         if (e instanceof ServerPlayer) {
            ServerPlayer vp = (ServerPlayer)e;
            vp.displayClientMessage(Component.literal("§7The salt gives way on its own."), true);
         }
      }

   }

   static void root(LivingEntity e, int ticks) {
      SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.get(e.getUUID());
      if (st == null) {
         st = new SaltSeraphEvents.Salted(e);
         st.lastY = e.getY();
         SALTED.put(e.getUUID(), st);
      }

      st.root = Math.max(st.root, ticks);
      e.setDeltaMovement(0.0D, Math.min(0.0D, e.getDeltaMovement().y), 0.0D);
      e.hurtMarked = true;
      e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 6, false, false, true));
      sync(st, true);
   }

   static boolean landed(ServerPlayer p, ServerLevel level, LivingEntity e, double dmg, Vec3 from, double kb, double lift) {
      SALTING.set(Boolean.TRUE);

      boolean landed;
      try {
         landed = OriginStrike.hitFresh(e, level.damageSources().playerAttack(p), (float)dmg);
      } finally {
         SALTING.set(Boolean.FALSE);
      }

      if (landed && e.isAlive() && from != null && kb > 0.0D && !set(e) && !rooted(e) && !TimeWeft.everpresent(e)) {
         double resist = e.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
         e.setDeltaMovement(v(SaltMath.knock(a(e.getDeltaMovement()), a(e.position()), a(from), kb, lift, resist)));
         e.hurtMarked = true;
      }

      return landed;
   }

   static void shatter(ServerPlayer p, ServerLevel level, LivingEntity e, double dmg) {
      Vec3 m = mid(e);
      fxAt(e, 7, e.getId(), m, ZERO, 0);
      sound(level, m, "minecraft:block.glass.break", 1.0F, 0.8F);
      sound(level, m, "minecraft:block.amethyst_cluster.break", 0.8F, 1.0F);
      landed(p, level, e, dmg, (Vec3)null, 0.0D, 0.0D);
   }

   private static void saltHit(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, LivingEntity e, int kind, double dmg, double salting, Vec3 from, double kb, double lift) {
      if (!set(e)) {
         boolean w = waking(s);
         double hit = dmg * SaltMath.wakeBonus(w) * SaltMath.allyCut(kind == 2);
         landed(p, level, e, hit, from, kb, lift);
         if (e.isAlive()) {
            salt(p, e, salting + SaltMath.wakeSalt(w));
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onHurt(LivingHurtEvent event) {
      LivingEntity e = event.getEntity();
      if (set(e)) {
         event.setAmount(event.getAmount() * (float)SaltMath.taken(true));
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onEdge(LivingHurtEvent event) {
      if (!SALTING.get()) {
         Entity e_0 = event.getSource().getEntity();
         if (e_0 instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)e_0;
            if (event.getSource().getDirectEntity() == p && event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
               if (!edgeOn(p)) {
                  return;
               }

               LivingEntity e = event.getEntity();
               SaltSeraphEvents.Salter s = (SaltSeraphEvents.Salter)SALTERS.get(p.getUUID());
               boolean w = waking(s);
               int kind = who(p, e, w);
               if (kind == 0) {
                  return;
               }

               event.setAmount((float)(SaltMath.edgeHit((double)event.getAmount(), undead(e)) * SaltMath.wakeBonus(w) * SaltMath.allyCut(kind == 2)));
               ServerLevel level = p.serverLevel();
               if (set(e)) {
                  fxAt(e, 7, e.getId(), mid(e), ZERO, 0);
                  sound(level, mid(e), "minecraft:block.glass.break", 0.6F, 1.2F);
                  return;
               }

               salt(p, e, 8.0D + SaltMath.wakeSalt(w));
               fxAt(e, 3, e.getId(), e.position().add(0.0D, (double)e.getBbHeight() * 0.72D, 0.0D), ZERO, 10);
               sound(level, e.position(), "minecraft:block.amethyst_block.hit", 0.6F, 1.6F);
               return;
            }

            return;
         }
      }

   }

   static boolean edgeOn(ServerPlayer p) {
      Set<String> tags = p.getTags();
      return (tags.contains("unrp.has.salt_seraph") || tags.contains("unrp.ss_copy")) && tags.contains("unrp.ss_on") && noncanonOn(p) && !set(p) && !SaltUnboundEvents.bound(p);
   }

   @SubscribeEvent
   public static void onKnock(LivingKnockBackEvent event) {
      if (set(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      LivingEntity e = event.getEntity();
      if (!e.level().isClientSide() && !event.isCanceled()) {
         SaltSeraphEvents.Salted st;
         boolean var10000;
         label65: {
            st = (SaltSeraphEvents.Salted)SALTED.get(e.getUUID());
            if (!SALTING.get()) {
               label64: {
                  Entity s_0 = event.getSource().getEntity();
                  if (s_0 instanceof ServerPlayer) {
                     ServerPlayer by = (ServerPlayer)s_0;
                     if (edgeOn(by) && event.getSource().getDirectEntity() == by && event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
                        break label64;
                     }
                  }

                  var10000 = false;
                  break label65;
               }
            }

            var10000 = true;
         }

         boolean bySalt = var10000;
         if (st != null && (st.statue || st.pillar > 0) || bySalt) {
            fxAt(e, 8, e.getId(), e.position(), ZERO, (int)Math.round((double)e.getBbHeight() * 100.0D));
            sound((ServerLevel)e.level(), e.position(), "minecraft:block.sand.break", 1.0F, 0.8F);
            sound((ServerLevel)e.level(), e.position(), "minecraft:block.sand.fall", 0.8F, 0.9F);
         }

         if (st != null && st.struggling && e instanceof ServerPlayer) {
            ServerPlayer vp = (ServerPlayer)e;
            StruggleEvents.end(vp, 6);
         }

         if (st != null) {
            st.struggling = false;
            st.pillar = 0;
            e.getPersistentData().remove("unrp_salt_statue");
         }

         if (e instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)e;
            SaltSeraphEvents.Salter s = (SaltSeraphEvents.Salter)SALTERS.get(p.getUUID());
            if (s != null) {
               powerOff(p, s);
               drop(p, s);
            }
         }

      }
   }

   @SubscribeEvent
   public static void onShear(PlayerInteractEvent.EntityInteract event) {
      if (!event.getLevel().isClientSide()) {
         Entity st_0 = event.getTarget();
         if (st_0 instanceof LivingEntity) {
            LivingEntity e = (LivingEntity)st_0;
            SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.get(e.getUUID());
            if (st != null && st.statue) {
               event.setCanceled(true);
               ItemStack held = event.getItemStack();
               if (!(held.getItem() instanceof ShearsItem)) {
                  event.setCancellationResult(InteractionResult.PASS);
                  return;
               }

               event.setCancellationResult(InteractionResult.SUCCESS);
               Player player = event.getEntity();
               player.swing(event.getHand(), true);
               if (!player.getAbilities().instabuild) {
                  held.hurtAndBreak(1, player, (pl) -> pl.broadcastBreakEvent(event.getHand()));
               }

               sound((ServerLevel)e.level(), e.position(), "minecraft:block.bone_block.break", 1.0F, 1.2F);
               free(e, st, true);
               player.displayClientMessage(Component.literal("§fSheared free: §7Slowness for 3 s."), true);
               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide()) {
         Entity old_0 = event.getEntity();
         if (old_0 instanceof LivingEntity) {
            LivingEntity e = (LivingEntity)old_0;
            if (!e.getPersistentData().getBoolean("unrp_salt_statue")) {
               return;
            }

            SaltSeraphEvents.Salted old = (SaltSeraphEvents.Salted)SALTED.get(e.getUUID());
            if (old == null || old.e != e && old.e.isRemoved()) {
               SaltSeraphEvents.Salted st = new SaltSeraphEvents.Salted(e);
               st.salt = 100.0D;
               st.statue = true;
               st.lastY = e.getY();
               SALTED.put(e.getUUID(), st);
               if (e instanceof Mob) {
                  Mob mob = (Mob)e;
                  mob.setNoAi(true);
               }

               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onTrack(PlayerEvent.StartTracking event) {
      Player st_0 = event.getEntity();
      if (st_0 instanceof ServerPlayer viewer) {
         SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.get(event.getTarget().getUUID());
         if (st != null) {
            com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), new SaltMarkPacket(st.e.getId(), (int)Math.ceil(st.salt), flagsOf(st)));
         }
      }
   }

   @SubscribeEvent
   public static void onSplash(ProjectileImpactEvent event) {
      Projectile now_0 = event.getProjectile();
      if (now_0 instanceof ThrownPotion potion) {
         if (!potion.level().isClientSide()) {
            if (PotionUtils.getPotion(potion.getItem()) == Potions.WATER && PotionUtils.getMobEffects(potion.getItem()).isEmpty()) {
               long now = potion.level().getGameTime();

               for(LivingEntity e : potion.level().getEntitiesOfClass(LivingEntity.class, potion.getBoundingBox().inflate(4.0D, 2.0D, 4.0D))) {
                  SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.get(e.getUUID());
                  if (st != null && potion.distanceToSqr(e) < 16.0D) {
                     st.splashed = now;
                  }
               }

               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onSwingStuck(AttackEntityEvent event) {
      if (isPillar(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onUseStuck(PlayerInteractEvent.RightClickItem event) {
      if (isPillar(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onBlockStuck(PlayerInteractEvent.RightClickBlock event) {
      if (isPillar(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onBreakStuck(PlayerInteractEvent.LeftClickBlock event) {
      if (isPillar(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onFall(LivingFallEvent event) {
      LivingEntity s_0 = event.getEntity();
      if (s_0 instanceof ServerPlayer p) {
         SaltSeraphEvents.Salter s = (SaltSeraphEvents.Salter)SALTERS.get(p.getUUID());
         if (s != null && s.soft) {
            event.setDamageMultiplier(0.0F);
            event.setDistance(0.0F);
            s.soft = false;
         }

      }
   }

   private static SaltSeraphEvents.Hit pick(ServerPlayer p, ServerLevel level, double range, Predicate<LivingEntity> ok) {
      Vec3 eye = p.getEyePosition();
      Vec3 end = eye.add(p.getLookAngle().scale(range));
      BlockHitResult wall = level.clip(new ClipContext(eye, end, Block.COLLIDER, Fluid.NONE, p));
      Vec3 limit = wall.getType() == Type.MISS ? end : wall.getLocation();
      SaltSeraphEvents.Hit out = new SaltSeraphEvents.Hit();
      out.at = limit;
      double bestD = Double.MAX_VALUE;
      AABB box = (new AABB(eye, limit)).inflate(1.0D);

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, (x) -> body(p, x) && ok.test(x))) {
         AABB bb = e.getBoundingBox().inflate(0.3D);
         Vec3 at = (Vec3)bb.clip(eye, limit).orElse(bb.contains(eye) ? eye : null);
         if (at != null) {
            double d = eye.distanceToSqr(at);
            if (d < bestD) {
               bestD = d;
               out.entity = e;
               out.at = mid(e);
            }
         }
      }

      return out;
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player tags_0 = event.player;
         if (tags_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)tags_0;
            Set<String> tags = player.getTags();
            boolean real = tags.contains("unrp.has.salt_seraph");
            boolean copy = !real && tags.contains("unrp.ss_copy");
            if (!real && !copy) {
               if (SALTERS.containsKey(player.getUUID()) || stray(player)) {
                  shutDown(player);
               }

               for(String t_0 : TRIGGERS) {
                  tags.remove(t_0);
               }

            } else {
               if (copy) {
                  for(String t : REAL_ONLY) {
                     tags.remove(t);
                  }
               }

               ServerLevel level = player.serverLevel();
               long now = level.getGameTime();
               SaltSeraphEvents.Salter s = salter(player);
               if (s.fresh) {
                  clearTransient(player);
                  s.fresh = false;
               }

               boolean enabled = noncanonOn(player);
               if (tags.remove("unrp.ss_off_trigger") && !tags.contains("unrp.ss_on")) {
                  powerOff(player, s);
               }

               boolean on = tags.contains("unrp.ss_on") && enabled && player.isAlive() && !player.isSpectator() && !set(player) && !SaltUnboundEvents.bound(player);
               if (!on && busy(s)) {
                  powerOff(player, s);
               }

               runTimed(player, level, s, now);
               boolean lanceKey = tags.remove("unrp.ss_lance_trigger");
               boolean wallKey = tags.remove("unrp.ss_wall_trigger");
               boolean spiresKey = tags.remove("unrp.ss_spires_trigger");
               boolean wakeKey = tags.remove("unrp.ss_wake_trigger");
               if (lanceKey || wallKey || spiresKey || wakeKey) {
                  if (!enabled) {
                     bar(player, "§cNon-canon powers are disabled on this server.");
                  } else if (on) {
                     boolean sneak = player.isShiftKeyDown();
                     if (lanceKey) {
                        if (sneak && real) {
                           greatPress(player, level, s, now);
                        } else {
                           lancePress(player, level, s, now);
                        }
                     }

                     if (wallKey && real) {
                        wallPress(player, level, s, now);
                     }

                     if (spiresKey && real) {
                        if (sneak) {
                           sealPress(player, level, s, now);
                        } else {
                           spiresPress(player, level, s, now);
                        }
                     }

                     if (wakeKey && real) {
                        wakePress(player, level, s, now);
                     }
                  }
               }

               tickFlights(player, level, s, now);
               tickRuns(player, level, s, now);
               tickWake(player, level, s, now);
               syncTags(player, s);
               hud(player, level, s, now);
               if (player.tickCount % 10 == 0) {
                  refresh(player, s, now);
               }

            }
         }
      }
   }

   private static boolean stray(ServerPlayer p) {
      Set<String> tags = p.getTags();

      for(String t : TRANSIENT) {
         if (tags.contains(t)) {
            return true;
         }
      }

      return false;
   }

   private static boolean busy(SaltSeraphEvents.Salter s) {
      return s.wake > 0 || !s.timed.isEmpty() || !s.flights.isEmpty() || !s.runs.isEmpty();
   }

   private static void clearTransient(ServerPlayer p) {
      Set<String> tags = p.getTags();

      for(String t : TRANSIENT) {
         tags.remove(t);
      }

   }

   private static void lancePress(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (ready(p, s.readyLance, now, "Salt Lance") && pay(p, 10)) {
         s.cdLance = cooldown(p, 40);
         s.readyLance = now + (long)s.cdLance;
         MoveAnim.play(p, "ss_lance");
         fx(p, 0, 0, 0, ZERO, ZERO, 0, 8, 0);
         sound(level, p.position(), "minecraft:block.amethyst_block.chime", 0.8F, 1.5F);
         later(s, now + 8L, (pp, lv, ss, nn) -> launch(pp, lv, ss, nn, false));
      }
   }

   private static void greatPress(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (ready(p, s.readyGreat, now, "Great Lance") && pay(p, 35)) {
         s.cdGreat = cooldown(p, 600);
         s.readyGreat = now + (long)s.cdGreat;
         MoveAnim.play(p, "ss_great");
         fx(p, 9, 0, 0, aim(p, level), ZERO, 0, 30, 0);
         sound(level, p.position(), "minecraft:block.amethyst_block.resonate", 1.0F, 0.8F);
         sound(level, p.position(), "minecraft:block.beacon.power_select", 0.6F, 1.6F);
         later(s, now + 28L, (pp, lv, ss, nn) -> launch(pp, lv, ss, nn, true));
      }
   }

   private static void launch(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now, boolean great) {
      double yaw = (double)p.getYRot();
      double sc = scaleOf(p);
      Vec3 from = v(great ? SaltMath.greatFrom(a(p.position()), yaw, sc) : SaltMath.lanceFrom(a(p.position()), yaw, sc));
      Vec3 dir = aim(p, level).subtract(from);
      dir = dir.lengthSqr() < 1.0E-6D ? p.getLookAngle() : dir.normalize();
      SaltSeraphEvents.Flight f = new SaltSeraphEvents.Flight();
      f.great = great;
      f.id = s.flightIds++ & 31;
      f.from = from;
      f.dir = dir;
      f.last = from;
      s.flights.add(f);
      fx(p, great ? 10 : 1, f.id, 0, from, dir, 0, 0, 0);
      sound(level, from, "minecraft:item.trident.throw", great ? 1.0F : 0.8F, great ? 0.6F : 1.2F);
      if (great) {
         sound(level, from, "minecraft:entity.warden.attack_impact", 0.5F, 1.6F);
      }

   }

   private static Vec3 aim(ServerPlayer p, ServerLevel level) {
      return pick(p, level, 64.0D, (x) -> true).at;
   }

   private static void tickFlights(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (!s.flights.isEmpty()) {
         for(SaltSeraphEvents.Flight f : new ArrayList<>(s.flights)) {
            tickFlight(p, level, s, f);
            ++f.age;
         }

      }
   }

   private static void tickFlight(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, SaltSeraphEvents.Flight f) {
      double speed = f.great ? 2.4D : 2.2D;
      Vec3 tip = f.from.add(f.dir.scale(speed * (double)f.age));
      BlockHitResult wall = level.clip(new ClipContext(f.last, tip, Block.COLLIDER, Fluid.NONE, p));
      boolean stuck = wall.getType() == Type.BLOCK;
      Vec3 stop = stuck ? wall.getLocation() : tip;
      double reach = stop.subtract(f.from).length() / speed;
      boolean w = waking(s);
      AABB box = (new AABB(f.from, stop)).inflate(2.5D);
      List<LivingEntity> near = level.getEntitiesOfClass(LivingEntity.class, box, (x) -> who(p, x, w) != 0);
      near.sort(Comparator.comparingDouble((x) -> x.position().distanceToSqr(f.from)));

      for(LivingEntity e : near) {
         if (!f.hits.contains(e.getUUID())) {
            int kind = who(p, e, w);
            double[] c = f.great ? SaltMath.greatHit(a(f.from), a(f.dir), reach, a(e.position()), (double)e.getBbHeight(), (double)e.getBbWidth()) : SaltMath.lanceHit(a(f.from), a(f.dir), reach, a(mid(e)), (double)e.getBbWidth());
            if (c != null) {
               f.hits.add(e.getUUID());
               Vec3 at = v(c);
               fx(p, 3, 0, 0, at, ZERO, 0, f.great ? 16 : 10, 0);
               if (set(e)) {
                  shatter(p, level, e, f.great ? 18.0D : 8.0D);
               } else if (f.great) {
                  saltHit(p, level, s, e, kind, 18.0D, 60.0D, f.from, 0.6D, 0.3D);
               } else {
                  saltHit(p, level, s, e, kind, 4.0D, 25.0D, f.from, 0.15D, 0.3D);
               }

               sound(level, at, "minecraft:block.amethyst_block.hit", 1.0F, f.great ? 0.7F : 1.3F);
               if (!f.great && f.hits.size() >= 2) {
                  flightDone(p, s, f, at, false);
                  return;
               }
            }
         }
      }

      if (stuck) {
         if (f.great) {
            greatBurst(p, level, s, f, stop);
         } else {
            flightDone(p, s, f, stop, false);
            sound(level, stop, "minecraft:block.glass.break", 0.7F, 1.4F);
         }

      } else {
         f.last = tip;
         if (f.age > (f.great ? 40 : 22)) {
            flightDone(p, s, f, tip, false);
         }

      }
   }

   private static void flightDone(ServerPlayer p, SaltSeraphEvents.Salter s, SaltSeraphEvents.Flight f, Vec3 at, boolean stuck) {
      s.flights.remove(f);
      fx(p, f.great ? 11 : 2, f.id, 0, at, f.dir, 0, 0, stuck ? 1 : 0);
   }

   private static void greatBurst(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, SaltSeraphEvents.Flight f, Vec3 tip) {
      flightDone(p, s, f, tip, true);
      fx(p, 12, 0, 0, tip, f.dir, 0, 0, 0);
      sound(level, tip, "minecraft:block.glass.break", 1.0F, 0.6F);
      sound(level, tip, "minecraft:entity.generic.explode", 0.5F, 1.5F);
      boolean w = waking(s);

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, (new AABB(tip, tip)).inflate(5.0D))) {
         int kind = who(p, e, w);
         if (kind != 0 && !isStatue(e) && SaltMath.greatBurstReaches(a(tip), a(mid(e)))) {
            landed(p, level, e, 3.0D * SaltMath.allyCut(kind == 2), tip, 0.5D, 0.3D);
            if (e.isAlive()) {
               salt(p, e, 20.0D);
            }
         }
      }

   }

   public static boolean owned(ServerLevel level, BlockPos pos) {
      Set<BlockPos> mine = (Set)OWNED.get(level.dimension());
      return mine != null && mine.contains(pos);
   }

   private static void own(ServerLevel level, BlockPos pos, boolean on) {
      Set<BlockPos> mine = (Set)OWNED.get(level.dimension());
      if (on) {
         if (mine == null) {
            mine = new HashSet();
            OWNED.put(level.dimension(), mine);
         }

         mine.add(pos);
      } else if (mine != null) {
         mine.remove(pos);
         if (mine.isEmpty()) {
            OWNED.remove(level.dimension());
         }
      }

   }

   private static void fxNear(ServerLevel level, int kind, Vec3 at, int value) {
      SaltFxPacket msg = new SaltFxPacket(0, kind, 0, 0, (float)at.x, (float)at.y, (float)at.z, 0.0F, 0.0F, 0.0F, 0, value, 0);
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(at.x, at.y, at.z, 64.0D, level.dimension())), msg);
   }

   static boolean takes(ServerLevel level, BlockPos pos, BlockState state) {
      if (state.isAir()) {
         return true;
      } else {
         net.minecraft.world.level.block.Block b = state.getBlock();
         if (!(b instanceof SaltBlock) && !(b instanceof DoublePlantBlock) && !state.hasBlockEntity()) {
            FluidState fluid = state.getFluidState();
            if (!fluid.isEmpty() && !fluid.is(FluidTags.WATER)) {
               return false;
            } else {
               return state.canBeReplaced() && state.getCollisionShape(level, pos).isEmpty();
            }
         } else {
            return false;
         }
      }
   }

   static boolean placeable(ServerPlayer p, ServerLevel level, BlockPos pos) {
      BlockEvent.EntityPlaceEvent ev = new BlockEvent.EntityPlaceEvent(BlockSnapshot.create(level.dimension(), level, pos), level.getBlockState(pos.below()), p);
      return !MinecraftForge.EVENT_BUS.post(ev);
   }

   private static boolean crowded(ServerLevel level, BlockPos pos) {
      return !level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), (x) -> x.isAlive() && !x.isSpectator()).isEmpty();
   }

   private static void wallPress(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (ready(p, s.readyWall, now, "Salt Wall") && pay(p, 20)) {
         s.cdWall = cooldown(p, 300);
         s.readyWall = now + (long)s.cdWall;
         MoveAnim.play(p, "ss_wall");
         SaltSeraphEvents.Wall w = new SaltSeraphEvents.Wall();
         w.level = level;
         w.wet = p.isInWater();
         w.crumbleAt = now + (long)SaltMath.wallCrumbleAt(w.wet);
         int[][] cols = SaltMath.wallCols(a(p.position()), (double)p.getYRot());
         boolean builds = p.mayBuild();

         for(int i = 0; i < cols.length; ++i) {
            for(int h = 0; h < 3; ++h) {
               BlockPos pos = new BlockPos(cols[i][0], cols[i][1] + h, cols[i][2]);
               if (builds && level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos) && level.mayInteract(p, pos) && placeable(p, level, pos)) {
                  SaltSeraphEvents.Placed c = new SaltSeraphEvents.Placed();
                  c.pos = pos;
                  c.due = now + (long)SaltMath.wallRiseAt(i, h);
                  w.rising.add(c);
               }
            }
         }

         if (!w.rising.isEmpty()) {
            WALLS.add(w);
         }

         sound(level, p.position(), "minecraft:block.sand.place", 1.0F, 0.8F);
         sound(level, p.position(), "minecraft:block.amethyst_block.chime", 0.7F, 0.9F);
      }
   }

   private static void tickWalls() {
      Iterator<SaltSeraphEvents.Wall> it = WALLS.iterator();

      while(it.hasNext()) {
         SaltSeraphEvents.Wall w = (SaltSeraphEvents.Wall)it.next();
         long now = w.level.getGameTime();
         Iterator<SaltSeraphEvents.Placed> ri = w.rising.iterator();

         while(ri.hasNext()) {
            SaltSeraphEvents.Placed c_0 = (SaltSeraphEvents.Placed)ri.next();
            if (c_0.due <= now) {
               ri.remove();
               if (!w.crumbling) {
                  rise(w, c_0, now);
               }
            }
         }

         if (!w.crumbling && now >= w.crumbleAt) {
            w.crumbling = true;
            w.standing.sort(Comparator.comparingInt((cx) -> -cx.pos.getY()));

            for(int i = 0; i < w.standing.size(); ++i) {
               ((SaltSeraphEvents.Placed)w.standing.get(i)).due = now + (long)SaltMath.crumbleDelay(i);
            }
         }

         if (w.crumbling) {
            Iterator<SaltSeraphEvents.Placed> si = w.standing.iterator();

            while(si.hasNext()) {
               SaltSeraphEvents.Placed c = (SaltSeraphEvents.Placed)si.next();
               if (c.due <= now) {
                  si.remove();
                  crumble(w, c);
               }
            }

            if (w.standing.isEmpty() && w.rising.isEmpty()) {
               it.remove();
            }
         }
      }

   }

   private static void rise(SaltSeraphEvents.Wall w, SaltSeraphEvents.Placed c, long now) {
      ServerLevel level = w.level;
      if (level.isLoaded(c.pos)) {
         BlockState was = level.getBlockState(c.pos);
         if (takes(level, c.pos, was) && !crowded(level, c.pos)) {
            c.wet = was.getFluidState().is(FluidTags.WATER);
            c.was = was.getBlock() instanceof BaseFireBlock ? Blocks.AIR.defaultBlockState() : was;
            net.minecraft.world.level.block.Block salt = (net.minecraft.world.level.block.Block)com.myriam.unrp.item.ModBlocks.SALT_BLOCK.get();
            level.setBlock(c.pos, (BlockState)salt.defaultBlockState().setValue(SaltBlock.WATER, c.wet), 3);
            own(level, c.pos, true);
            level.scheduleTick(c.pos, salt, (int)Math.max(1L, w.crumbleAt - now) + 30 + 60);
            w.standing.add(c);
            fxNear(level, 13, new Vec3((double)c.pos.getX() + 0.5D, (double)c.pos.getY() + 0.1D, (double)c.pos.getZ() + 0.5D), 0);
            if (w.standing.size() % 3 == 1) {
               sound(level, Vec3.atCenterOf(c.pos), "minecraft:block.bone_block.place", 0.6F, 1.2F);
            }

         }
      }
   }

   private static void crumble(SaltSeraphEvents.Wall w, SaltSeraphEvents.Placed c) {
      ServerLevel level = w.level;
      own(level, c.pos, false);
      if (level.isLoaded(c.pos)) {
         if (level.getBlockState(c.pos).getBlock() instanceof SaltBlock) {
            BlockState back = c.was == null ? Blocks.AIR.defaultBlockState() : c.was;
            if (!back.isAir() && !back.canSurvive(level, c.pos)) {
               back = c.wet ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
            }

            level.setBlock(c.pos, back, 3);
            Vec3 at = Vec3.atCenterOf(c.pos);
            fxNear(level, w.wet ? 15 : 14, at, 0);
            if (c.due != w.soundAt) {
               w.soundAt = c.due;
               sound(level, at, w.wet ? "minecraft:block.fire.extinguish" : "minecraft:block.sand.break", 0.8F, w.wet ? 1.4F : 0.9F);
            }

         }
      }
   }

   private static void spiresPress(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (ready(p, s.readySpires, now, "Spire Surge") && pay(p, 30)) {
         s.cdSpires = cooldown(p, 440);
         s.readySpires = now + (long)s.cdSpires;
         MoveAnim.play(p, "ss_spires");
         sound(level, p.position(), "minecraft:block.sand.step", 1.0F, 0.7F);
         later(s, now + 5L, (pp, lv, ss, nn) -> surge(pp, lv, ss));
      }
   }

   private static void surge(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s) {
      double yaw = (double)p.getYRot();
      SaltSeraphEvents.Run r = new SaltSeraphEvents.Run();
      r.at = p.position();
      r.dir = v(SaltMath.fwd(yaw));
      r.side = v(SaltMath.right(yaw));
      s.runs.add(r);
      fx(p, 16, 0, 0, r.at, r.dir, 0, SaltMath.spiresLife(), 0);
      sound(level, r.at, "minecraft:entity.evoker_fangs.attack", 1.0F, 0.8F);
      sound(level, r.at, "minecraft:block.sand.break", 1.0F, 0.7F);
   }

   private static boolean seen(ServerLevel level, Vec3 from, LivingEntity e, Entity by) {
      Vec3 head = e.position().add(0.0D, (double)e.getBbHeight() * 0.9D, 0.0D);

      for(Vec3 to : new Vec3[]{mid(e), head}) {
         if (level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, by)).getType() == Type.MISS) {
            return true;
         }
      }

      return false;
   }

   private static void tickRuns(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (!s.runs.isEmpty()) {
         boolean w = waking(s);
         Iterator<SaltSeraphEvents.Run> it = s.runs.iterator();

         while(it.hasNext()) {
            SaltSeraphEvents.Run r = (SaltSeraphEvents.Run)it.next();
            if (!SaltMath.spiresRunning(r.age)) {
               it.remove();
            } else {
               double front = SaltMath.spiresFront(r.age);
               AABB box = (new AABB(r.at, r.at)).inflate(front + 2.0D, 5.0D, front + 2.0D);
               Vec3 eye = r.at.add(0.0D, 1.5D, 0.0D);

               for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, (x) -> who(p, x, w) != 0)) {
                  if (!r.hits.contains(e.getUUID()) && SaltMath.spiresHit(a(r.at), a(r.dir), a(r.side), r.age, a(e.position()))) {
                     double dy = e.getY() - r.at.y;
                     if (!(dy < -4.5D) && !(dy > 3.5D) && seen(level, eye, e, p)) {
                        r.hits.add(e.getUUID());
                        if (set(e)) {
                           shatter(p, level, e, 20.0D);
                        } else {
                           saltHit(p, level, s, e, who(p, e, w), 8.0D, 35.0D, r.at, 0.25D, 0.8D);
                           sound(level, e.position(), "minecraft:block.amethyst_block.hit", 0.8F, 1.1F);
                        }
                     }
                  }
               }

               ++r.age;
            }
         }

      }
   }

   private static void sealPress(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (ready(p, s.readySeal, now, "Salt Seal")) {
         boolean w = waking(s);
         SaltSeraphEvents.Hit h = pick(p, level, 20.0D, (x) -> who(p, x, w) == 1);
         if (h.entity == null) {
            bar(p, "§7Salt Seal: no target in reach.");
         } else if (pay(p, 22)) {
            s.cdSeal = cooldown(p, 320);
            s.readySeal = now + (long)s.cdSeal;
            LivingEntity t = h.entity;
            MoveAnim.play(p, "ss_seal");
            fx(p, 17, 0, t.getId(), ZERO, ZERO, 0, 8, 0);
            sound(level, p.position(), "minecraft:block.sand.hit", 1.0F, 1.2F);
            later(s, now + 6L, (pp, lv, ss, nn) -> seal(pp, lv, ss, t));
         }
      }
   }

   private static void seal(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, LivingEntity t) {
      if (t.isAlive() && !t.isRemoved() && t.level() == level && who(p, t, waking(s)) == 1) {
         fxAt(t, 18, t.getId(), t.position(), ZERO, 84);
         sound(level, t.position(), "minecraft:entity.evoker_fangs.attack", 0.9F, 1.2F);
         sound(level, t.position(), "minecraft:block.bone_block.place", 0.8F, 0.8F);
         if (set(t)) {
            shatter(p, level, t, 24.0D);
         } else {
            salt(p, t, 45.0D + SaltMath.wakeSalt(waking(s)));
            if (t.isAlive() && !set(t)) {
               root(t, 60);
            }

         }
      }
   }

   private static double groundBelow(ServerLevel level, LivingEntity e) {
      BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
      int x = Mth.floor(e.getX());
      int z = Mth.floor(e.getZ());
      int top = Mth.floor(e.getY() + 0.5D);
      double limit = e.getY() + 0.6D;

      for(int y = top; y > top - 24 && y >= level.getMinBuildHeight(); --y) {
         m.set(x, y, z);
         BlockState st = level.getBlockState(m);
         FluidState fl = st.getFluidState();
         double h = Double.NaN;
         if (!fl.isEmpty()) {
            h = (double)((float)y + fl.getHeight(level, m));
         }

         VoxelShape shape = st.getCollisionShape(level, m);
         if (!shape.isEmpty()) {
            double sh = (double)y + shape.max(Axis.Y);
            h = Double.isNaN(h) ? sh : Math.max(h, sh);
         }

         if (!Double.isNaN(h) && h <= limit) {
            return h;
         }
      }

      return e.getY() - 24.0D;
   }

   private static void wakePress(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (s.wake > 0) {
         trumpet(p, level, s, now);
      } else if (ready(p, s.readyWake, now, "Seraph Wake") && pay(p, 55)) {
         s.cdWake = cooldown(p, 2400);
         s.readyWake = now + (long)s.cdWake;
         s.wake = 1200;
         s.wakeAt = now;
         s.trumpeted = false;
         s.soft = false;
         Vec3 mv = p.getDeltaMovement();
         p.setDeltaMovement(mv.x, Math.max(mv.y, 0.16D), mv.z);
         p.hurtMarked = true;
         p.fallDistance = 0.0F;
         lift(p, 0.16D);
         tag(p, "unrp.ss_wake", true);
         tag(p, "unrp.ss_paused", true);
         fx(p, 20, 0, 0, ZERO, ZERO, 0, 1224, 0);
         fx(p, 19, 0, 0, p.position(), ZERO, 0, 20, 0);
         sound(level, p.position(), "minecraft:block.beacon.activate", 1.0F, 0.7F);
         sound(level, p.position(), "minecraft:entity.phantom.flap", 1.0F, 0.6F);
         sound(level, p.position(), "minecraft:block.respawn_anchor.charge", 0.7F, 0.6F);
         bar(p, "§fSeraph Wake: §7press again for the Last Trumpet.");
      }
   }

   private static void tickWake(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (s.wake <= 0) {
         tickSoft(p, s);
      } else {
         --s.wake;
         lift(p, SaltMath.hoverVel(SaltMath.hoverTarget(groundBelow(level, p), now), p.getY()));
         p.fallDistance = 0.0F;
         if (s.wake == 0) {
            endWake(p, level, s);
         }

      }
   }

   private static void tickSoft(ServerPlayer p, SaltSeraphEvents.Salter s) {
      if (s.soft) {
         if (!p.onGround() && !p.isInWater()) {
            s.softGround = 0;
         } else {
            ++s.softGround;
            if (s.softGround > 3) {
               s.soft = false;
               s.softGround = 0;
            }
         }

      }
   }

   private static void endWake(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s) {
      s.wake = 0;
      s.soft = true;
      s.softGround = 0;
      unlift(p);
      p.fallDistance = 0.0F;
      tag(p, "unrp.ss_wake", false);
      tag(p, "unrp.ss_paused", false);
      p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0, false, true, true));
      fx(p, 20, 0, 0, ZERO, ZERO, 1200, 1224, 0);
      sound(level, p.position(), "minecraft:block.beacon.deactivate", 0.8F, 0.8F);
      bar(p, "§7The Wake fades: Weakness for 20 s.");
   }

   private static void trumpet(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (!s.trumpeted) {
         s.trumpeted = true;
         MoveAnim.play(p, "ss_trumpet");
         fx(p, 21, 0, 0, ZERO, ZERO, 0, 30, 0);
         sound(level, p.position(), "minecraft:block.bell.use", 1.0F, 0.5F);
         sound(level, p.position(), "minecraft:block.beacon.power_select", 0.8F, 1.2F);
         later(s, now + 8L, (pp, lv, ss, nn) -> blow(pp, lv, ss));
      }
   }

   private static void blow(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s) {
      Vec3 c = p.position();
      fx(p, 22, 0, 0, new Vec3(c.x, groundBelow(level, p), c.z), ZERO, 0, 40, 0);
      sound(level, c, "minecraft:block.bell.resonate", 1.5F, 0.6F);
      sound(level, c, "minecraft:entity.lightning_bolt.thunder", 0.6F, 1.6F);
      sound(level, c, "minecraft:block.beacon.activate", 1.0F, 1.4F);
      AABB box = (new AABB(c, c)).inflate(17.0D);

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, (x) -> who(p, x, true) != 0)) {
         if (SaltMath.trumpetReaches(a(c), a(mid(e)), (double)e.getBbWidth())) {
            if (who(p, e, true) == 2) {
               landed(p, level, e, 2.0D, c, 0.2D, 0.3D);
               if (e.isAlive()) {
                  salt(p, e, 30.0D);
               }
            } else {
               if (undead(e) && !isStatue(e)) {
                  landed(p, level, e, 8.0D, c, 0.3D, 0.3D);
                  fxAt(e, 23, e.getId(), mid(e), ZERO, 14);
                  sound(level, e.position(), "minecraft:entity.lightning_bolt.impact", 0.6F, 1.4F);
               }

               if (e.isAlive()) {
                  salt(p, e, 60.0D);
               }
            }
         }
      }

   }

   private static void tag(ServerPlayer p, String name, boolean want) {
      if (want) {
         p.addTag(name);
      } else {
         p.removeTag(name);
      }

   }

   private static void syncTags(ServerPlayer p, SaltSeraphEvents.Salter s) {
      tag(p, "unrp.ss_wake", s.wake > 0);
      tag(p, "unrp.ss_paused", s.wake > 0);
   }

   private static int[] judged(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s) {
      if (s.wake <= 0) {
         return new int[0];
      } else {
         Vec3 c = p.position();
         List<LivingEntity> near = level.getEntitiesOfClass(LivingEntity.class, (new AABB(c, c)).inflate(15.0D), (x) -> who(p, x, true) == 2 && x.position().distanceTo(c) <= 15.0D);
         near.sort(Comparator.comparingDouble((x) -> x.distanceToSqr(p)));
         int n = Math.min(16, near.size());
         int[] out = new int[n];

         for(int i = 0; i < n; ++i) {
            out[i] = ((LivingEntity)near.get(i)).getId();
         }

         return out;
      }
   }

   private static void hud(ServerPlayer p, ServerLevel level, SaltSeraphEvents.Salter s, long now) {
      if (p.tickCount % 10 == 0 || s.wake <= 0 && s.judged.length > 0) {
         s.judged = judged(p, level, s);
      }

      long[] ends = new long[]{s.readyLance, s.readyGreat, s.readyWall, s.readySpires, s.readySeal, s.readyWake};
      int[] lens = new int[]{s.cdLance, s.cdGreat, s.cdWall, s.cdSpires, s.cdSeal, s.cdWake};
      long[] cur = new long[16];

      for(int i = 0; i < 6; ++i) {
         cur[i] = Math.max(now, ends[i]);
         cur[i + 6] = (long)lens[i];
      }

      int ub = SaltUnboundEvents.phase(p);
      int ubLeft = SaltUnboundEvents.left(p);
      cur[12] = ub == 1 ? -1L : (ub == 2 ? now + (long)ubLeft : (s.wake > 0 ? s.wakeAt + 1200L : 0L));
      cur[13] = ub == 1 ? 4L : (ub == 2 ? 8L : (long)((s.wake > 0 ? 1 : 0) | (s.trumpeted ? 2 : 0)));
      cur[14] = (long)Arrays.hashCode(s.judged);
      boolean same = now - s.sentAt < 20L;

      for(int i = 0; i < cur.length && same; ++i) {
         if (i < 6) {
            if (cur[i] > now == s.sent[i] > now && (cur[i] <= now || cur[i] == s.sent[i])) {
               continue;
            }
         } else if (cur[i] == s.sent[i]) {
            continue;
         }

         same = false;
      }

      if (!same) {
         System.arraycopy(cur, 0, s.sent, 0, cur.length);
         s.sentAt = now;
         int[] left = new int[6];

         for(int i = 0; i < left.length; ++i) {
            left[i] = (int)Math.max(0L, ends[i] - now);
         }

         com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new SaltHudPacket(left, lens, ub != 0 ? ubLeft : s.wake, (int)cur[13], s.judged));
      }
   }

   private static void refresh(ServerPlayer p, SaltSeraphEvents.Salter s, long now) {
      if (s.wake > 0) {
         fx(p, 20, 0, 0, ZERO, ZERO, 1200 - s.wake, 1224, 0);
      }

   }

   static void unleash(ServerPlayer p) {
      SaltSeraphEvents.Salter s = (SaltSeraphEvents.Salter)SALTERS.get(p.getUUID());
      if (s != null) {
         ++s.epoch;
         s.timed.clear();
         if (s.wake > 0) {
            s.wake = 0;
            s.trumpeted = false;
            s.soft = false;
            unlift(p);
            tag(p, "unrp.ss_wake", false);
            tag(p, "unrp.ss_paused", false);
            fx(p, 20, 0, 0, ZERO, ZERO, 1200, 1224, 0);
         }

         s.judged = new int[0];
      }
   }

   private static void powerOff(ServerPlayer p, SaltSeraphEvents.Salter s) {
      ++s.epoch;
      s.timed.clear();
      if (s.wake > 0) {
         endWake(p, p.serverLevel(), s);
      }

      tag(p, "unrp.ss_wake", false);
      tag(p, "unrp.ss_paused", false);
   }

   private static void drop(ServerPlayer p, SaltSeraphEvents.Salter s) {
      for(SaltSeraphEvents.Flight f : s.flights) {
         fx(p, f.great ? 11 : 2, f.id, 0, f.last, f.dir, 0, 0, 0);
      }

      s.flights.clear();
      s.runs.clear();
   }

   private static void shutDown(ServerPlayer p) {
      SaltSeraphEvents.Salter s = (SaltSeraphEvents.Salter)SALTERS.remove(p.getUUID());
      if (s != null) {
         powerOff(p, s);
         drop(p, s);
      }

      clearTransient(p);
      unlift(p);
   }

   private static void keep(ServerPlayer p, SaltSeraphEvents.Salted st) {
      CompoundTag d = p.getPersistentData();
      if (st.e == p && st.salt > 0.0D && p.isAlive()) {
         d.putDouble("unrp_salt_left", st.salt);
         d.putInt("unrp_salt_pillar", st.pillar);
      } else {
         d.remove("unrp_salt_left");
         d.remove("unrp_salt_pillar");
      }

   }

   @SubscribeEvent
   public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
      Player d_0 = event.getEntity();
      if (d_0 instanceof ServerPlayer p) {
         CompoundTag d = p.getPersistentData();
         if (d.contains("unrp_salt_left")) {
            double salt = Math.min(100.0D, d.getDouble("unrp_salt_left"));
            int left = Math.min(160, d.getInt("unrp_salt_pillar"));
            d.remove("unrp_salt_left");
            d.remove("unrp_salt_pillar");
            if (!(salt <= 0.0D) && p.isAlive() && !p.isCreative() && !p.isSpectator() && !TimeWeft.everpresent(p)) {
               SaltSeraphEvents.Salted st = new SaltSeraphEvents.Salted(p);
               st.salt = salt;
               st.since = 0;
               st.lastY = p.getY();
               SALTED.put(p.getUUID(), st);
               if (SaltMath.struggles(st.salt)) {
                  struggle(p, st);
               }

               if (left > 0) {
                  pillar(p, st);
                  st.pillar = left;
               }

               sync(st, true);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player st_0 = event.getEntity();
      if (st_0 instanceof ServerPlayer p) {
         SaltSeraphEvents.Salted st = (SaltSeraphEvents.Salted)SALTED.remove(p.getUUID());
         if (st != null) {
            keep(p, st);
         }

         SaltSeraphEvents.Salter s = (SaltSeraphEvents.Salter)SALTERS.get(p.getUUID());
         if (s != null) {
            powerOff(p, s);
            drop(p, s);
            s.soft = false;
            s.fresh = true;
            s.judged = new int[0];
            s.sentAt = -1000L;
         }

         clearTransient(p);
      }
   }

   @SubscribeEvent
   public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
      Player s_0 = event.getEntity();
      if (s_0 instanceof ServerPlayer p) {
         SaltSeraphEvents.Salter s = (SaltSeraphEvents.Salter)SALTERS.get(p.getUUID());
         if (s != null) {
            powerOff(p, s);
            drop(p, s);
         }

      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
      Player s_0 = event.getEntity();
      if (s_0 instanceof ServerPlayer p) {
         SaltSeraphEvents.Salter s = (SaltSeraphEvents.Salter)SALTERS.get(p.getUUID());
         if (s != null) {
            s.fresh = true;
            s.soft = false;
            s.sentAt = -1000L;
         }

      }
   }

   @SubscribeEvent
   public static void onStopping(ServerStoppingEvent event) {
      for(SaltSeraphEvents.Salted st : SALTED.values()) {
         LivingEntity c_0 = st.e;
         if (c_0 instanceof ServerPlayer vp) {
            keep(vp, st);
         }
      }

      for(SaltSeraphEvents.Wall w : WALLS) {
         for(SaltSeraphEvents.Placed c : w.standing) {
            crumble(w, c);
         }
      }

      WALLS.clear();
      OWNED.clear();
      SALTERS.clear();
      SALTED.clear();
   }

   interface Act {
      void run(ServerPlayer var1, ServerLevel var2, SaltSeraphEvents.Salter var3, long var4);
   }

   static final class Flight {
      boolean great;
      int id;
      Vec3 from;
      Vec3 dir;
      Vec3 last;
      int age;
      final List<UUID> hits = new ArrayList();
   }

   static final class Hit {
      LivingEntity entity;
      Vec3 at;
   }

   static final class Placed {
      BlockPos pos;
      BlockState was;
      boolean wet;
      long due;
   }

   static final class Run {
      Vec3 at;
      Vec3 dir;
      Vec3 side;
      int age;
      final List<UUID> hits = new ArrayList();
   }

   static final class Salted {
      final LivingEntity e;
      double salt;
      int since = 999;
      int pillar;
      boolean statue;
      int root;
      long splashed = -100L;
      boolean struggling;
      Vec3 pillarAt;
      double fall;
      double lastY;
      int sentSalt = -1;
      int sentFlags = -1;
      long sentAt;

      Salted(LivingEntity e) {
         this.e = e;
      }
   }

   static final class Salter {
      long readyLance;
      long readyGreat;
      long readyWall;
      long readySpires;
      long readySeal;
      long readyWake;
      int cdLance = 1;
      int cdGreat = 1;
      int cdWall = 1;
      int cdSpires = 1;
      int cdSeal = 1;
      int cdWake = 1;
      int wake;
      long wakeAt;
      boolean trumpeted;
      boolean soft;
      int softGround;
      int flightIds;
      int epoch;
      boolean fresh = true;
      int[] judged = new int[0];
      final List<SaltSeraphEvents.Flight> flights = new ArrayList();
      final List<SaltSeraphEvents.Run> runs = new ArrayList();
      final List<SaltSeraphEvents.Timed> timed = new ArrayList();
      final long[] sent = new long[16];
      long sentAt = -1000L;
   }

   static final class Timed {
      long due;
      int epoch;
      SaltSeraphEvents.Act act;
   }

   static final class Wall {
      ServerLevel level;
      boolean wet;
      long crumbleAt;
      boolean crumbling;
      long soundAt = -1L;
      final List<SaltSeraphEvents.Placed> rising = new ArrayList();
      final List<SaltSeraphEvents.Placed> standing = new ArrayList();
   }
}
