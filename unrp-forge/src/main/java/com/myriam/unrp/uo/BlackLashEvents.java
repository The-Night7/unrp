package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;
import com.myriam.unrp.anim.MoveAnim;
import com.myriam.unrp.deal.TrueDamage;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraftforge.common.Tags.EntityTypes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class BlackLashEvents {
   public static final int TEAL = 5891826;
   public static final int SMOKE = 1840168;
   public static final int FRENZY_RED = 16731499;
   public static final int RAGE_PINK = 16747176;
   public static final double BIND_RANGE = 10.0D;
   public static final int SWING_BURN = 20;
   public static final int THROW_RANGE = 24;
   public static final int MISS_COOLDOWN = 6;
   public static final double SLING_SNAP = 12.0D;
   public static final int SLING_FLIGHT = 100;
   public static final double WARD_SLOW = -0.5D;
   public static final int HOVER = -1;
   public static final double FRENZY_SLIP = 6.0D;
   public static final int SWING_LATCH = 2;
   public static final int SWING_HINTS = 2;
   public static final String HAS = "unrp.has.black_lash";
   public static final String ON = "unrp.bl_on";
   public static final String OFF_TRIGGER = "unrp.bl_off_trigger";
   public static final String LASH_TRIGGER = "unrp.bl_lash_trigger";
   public static final String LASH_HELD = "unrp.bl_lash_held";
   public static final String LASH_RELEASE = "unrp.bl_lash_release";
   public static final String BIND_TRIGGER = "unrp.bl_bind_trigger";
   public static final String WHIP_TRIGGER = "unrp.bl_whip_trigger";
   public static final String STORM_TRIGGER = "unrp.bl_storm_trigger";
   public static final String LOCKED = "unrp.bl_locked";
   public static final String STORM_READY = "unrp.bl_storm_ready";
   public static final String SWINGING = "unrp.bl_swinging";
   public static final String HANGING = "unrp.bl_hanging";
   public static final String PAUSED = "unrp.bl_paused";
   public static final String POSE_ZIP = "unrp.bl_zip";
   public static final String POSE_SWING = "unrp.bl_swing";
   public static final String POSE_SLING = "unrp.bl_sling";
   public static final String POSE_LAUNCH = "unrp.bl_launch";
   public static final String POSE_HOLD = "unrp.bl_hold";
   public static final String POSE_WARD = "unrp.bl_ward";
   public static final String POSE_STORM = "unrp.bl_storm";
   public static final String POSE_FRENZY = "unrp.bl_frenzy";
   public static final String TWIN_SCORE = "unrp.unlock_s3";
   public static final String CATCH_SCORE = "unrp.unlock_s2";
   public static final String WIND_SCORE = "unrp.unlock_s4";
   public static final String DOWN = "unrp.bl_down";
   public static final String RAGING = "unrp.bl_rage";
   public static final String COPY = "unrp.bl_copy";
   public static final String WIND_USED = "unrp_bl_wind_used";
   public static final String WIND_PHASE = "unrp_bl_wind_phase";
   public static final String WIND_LEFT = "unrp_bl_wind_left";
   public static final int CATCH_SOFT = 20;
   public static final double CATCH_DEPTH = 80.0D;
   static final String[] TRANSIENT = new String[]{"unrp.bl_locked", "unrp.bl_storm_ready", "unrp.bl_swinging", "unrp.bl_hanging", "unrp.bl_paused", "unrp.bl_zip", "unrp.bl_swing", "unrp.bl_sling", "unrp.bl_launch", "unrp.bl_hold", "unrp.bl_ward", "unrp.bl_storm", "unrp.bl_frenzy", "unrp.bl_down", "unrp.bl_rage"};
   static final String[] TRIGGERS = new String[]{"unrp.bl_off_trigger", "unrp.bl_lash_trigger", "unrp.bl_lash_release", "unrp.bl_bind_trigger", "unrp.bl_whip_trigger", "unrp.bl_storm_trigger"};
   static final String[] REAL_ONLY = new String[]{"unrp.bl_whip_trigger", "unrp.bl_storm_trigger"};
   private static final UUID WARD_UUID = UUID.fromString("5c7b3f0e-9a4d-4f3e-8b21-6d2f0c9a7e14");
   private static final Vec3 ZERO = Vec3.ZERO;
   private static final ThreadLocal<Boolean> LASHING = ThreadLocal.withInitial(() -> Boolean.FALSE);
   private static final ThreadLocal<Boolean> PLAIN = ThreadLocal.withInitial(() -> Boolean.FALSE);
   private static final ThreadLocal<ServerPlayer> FED = new ThreadLocal();
   private static final Map<UUID, Long> CAUGHT = new HashMap();
   private static final Map<UUID, BlackLashEvents.Lash> LASHES = new HashMap();

   private BlackLashEvents() {
   }

   private static BlackLashEvents.Lash lash(ServerPlayer p) {
      BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.get(p.getUUID());
      if (s == null) {
         s = new BlackLashEvents.Lash();
         LASHES.put(p.getUUID(), s);
      }

      return s;
   }

   private static boolean noncanonOn(ServerPlayer player) {
      return ConfigBridgeEvents.flag(player.server, "noncanon", false) || OriginsConfig.isModDev(player.getUUID());
   }

   static double[] a(Vec3 v) {
      return new double[]{v.x, v.y, v.z};
   }

   static Vec3 v(double[] d) {
      return new Vec3(d[0], d[1], d[2]);
   }

   @SubscribeEvent
   public static void onPlayerTickStart(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.START) {
         Player s_0 = event.player;
         if (s_0 instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)s_0;
            BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.get(p.getUUID());
            if (s != null && s.swing != null) {
               s.swing.seen = p.position();
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player tags_0 = event.player;
         if (tags_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)tags_0;
            Set<String> tags = player.getTags();
            boolean real = tags.contains("unrp.has.black_lash");
            boolean copy = !real && tags.contains("unrp.bl_copy");
            if (!real && !copy) {
               if (LASHES.containsKey(player.getUUID()) || stray(player)) {
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
               BlackLashEvents.Lash s = lash(player);
               s.copy = copy;
               if (s.fresh) {
                  clearTransient(player);
                  s.fresh = false;
               }

               if (!s.windChecked) {
                  s.windChecked = true;
                  windResume(player, s, now);
               }

               boolean enabled = noncanonOn(player);
               if (tags.remove("unrp.bl_off_trigger") && !tags.contains("unrp.bl_on")) {
                  powerOff(player, s);
               }

               boolean on = tags.contains("unrp.bl_on") && enabled && player.isAlive() && !player.isSpectator() && !downed(s);
               if (!on && busy(s)) {
                  powerOff(player, s);
               }

               sample(player, s);
               runTimed(player, level, s, now);
               boolean press = tags.remove("unrp.bl_lash_trigger");
               boolean release = tags.remove("unrp.bl_lash_release");
               boolean bindKey = tags.remove("unrp.bl_bind_trigger");
               boolean whipKey = tags.remove("unrp.bl_whip_trigger");
               boolean stormKey = tags.remove("unrp.bl_storm_trigger");
               if (press || bindKey || whipKey || stormKey) {
                  if (!enabled) {
                     bar(player, "§cNon-canon powers are disabled on this server.");
                  } else if (on) {
                     if (press) {
                        lashPress(player, level, s, now);
                     }

                     if (bindKey) {
                        bindPress(player, level, s, now);
                     }

                     if (whipKey) {
                        whipPress(player, level, s, now);
                     }

                     if (stormKey) {
                        stormPress(player, level, s, now);
                     }
                  }
               }

               if (release) {
                  lashRelease(player, s, now);
               }

               tickStrain(player, level, s);
               tickGrab(player, level, s, now);
               tickReel(player, level, s, now);
               tickZip(player, level, s, now);
               tickSwing(player, level, s, now);
               tickSling(player, level, s, now);
               tickHeld(player, level, s, now);
               tickMoves(player, level, s, now);
               tickThrown(player, level, s, now);
               tickWard(player, level, s, now);
               tickStorm(player, level, s, now);
               tickFrenzy(player, level, s, now);
               tickSoft(player, s);
               tickFlip(player, s, now);
               tickWind(player, level, s, now);
               tickCatch(player, level, s, now);
               syncTags(player, s, now);
               stormCue(player, s, now);
               hud(player, s, now);
               if (player.tickCount % 10 == 0) {
                  refresh(player, s, now);
               }

               if (now % 200L == 0L) {
                  CAUGHT.values().removeIf((until) -> until < now - 200L);
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

      AttributeInstance speed = p.getAttribute(Attributes.MOVEMENT_SPEED);
      return speed != null && speed.getModifier(WARD_UUID) != null;
   }

   private static boolean busy(BlackLashEvents.Lash s) {
      return s.grab != null || s.reel != null || s.zip != null || s.swing != null || s.sling != null || !s.held.isEmpty() || !s.moves.isEmpty() || s.ward > 0 || s.storm > 0;
   }

   private static void clearTransient(ServerPlayer p) {
      Set<String> tags = p.getTags();

      for(String t : TRANSIENT) {
         tags.remove(t);
      }

      tags.remove("unrp.bl_lash_held");
      AttributeInstance speed = p.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null && speed.getModifier(WARD_UUID) != null) {
         speed.removeModifier(WARD_UUID);
      }

   }

   private static void sample(ServerPlayer p, BlackLashEvents.Lash s) {
      if (s.hasLast) {
         s.mx = p.getX() - s.lx;
         s.my = p.getY() - s.ly;
         s.mz = p.getZ() - s.lz;
         s.still = Math.hypot(s.mx, s.mz) < 0.01D ? s.still + 1 : 0;
      }

      s.lx = p.getX();
      s.ly = p.getY();
      s.lz = p.getZ();
      s.hasLast = true;
   }

   private static Vec3 measured(BlackLashEvents.Lash s) {
      return new Vec3(s.mx, s.my, s.mz);
   }

   private static void later(BlackLashEvents.Lash s, long due, BlackLashEvents.Act act) {
      BlackLashEvents.Timed t = new BlackLashEvents.Timed();
      t.due = due;
      t.epoch = s.epoch;
      t.act = act;
      s.timed.add(t);
   }

   private static void runTimed(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (!s.timed.isEmpty()) {
         List<BlackLashEvents.Timed> due = new ArrayList();
         Iterator<BlackLashEvents.Timed> it = s.timed.iterator();

         while(it.hasNext()) {
            BlackLashEvents.Timed t_0 = (BlackLashEvents.Timed)it.next();
            if (t_0.epoch != s.epoch) {
               it.remove();
            } else if (t_0.due <= now) {
               it.remove();
               due.add(t_0);
            }
         }

         for(BlackLashEvents.Timed t : due) {
            if (t.epoch == s.epoch) {
               t.act.run(p, level, s, now);
            }
         }

      }
   }

   private static int cooldown(ServerPlayer p, int base) {
      return Math.max(1, LashMath.cooldown(base, TrickCooldownEvents.reduction(p)));
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

   private static boolean locked(ServerPlayer p, BlackLashEvents.Lash s) {
      if (s.frenzy > 0) {
         bar(p, "§dThe tendrils won't answer.");
         return true;
      } else if (s.rest > 0) {
         bar(p, "§8Resting: the tendrils won't come.");
         return true;
      } else {
         return false;
      }
   }

   private static void strain(BlackLashEvents.Lash s, double v) {
      if (s.frenzy <= 0 && s.rest <= 0 && !raging(s) && !s.copy) {
         s.add += v;
      }

   }

   static boolean raging(BlackLashEvents.Lash s) {
      return s != null && s.wind == 3;
   }

   static boolean downed(BlackLashEvents.Lash s) {
      return s != null && (s.wind == 1 || s.wind == 2);
   }

   public static boolean isDowned(Entity e) {
      return e instanceof ServerPlayer && downed((BlackLashEvents.Lash)LASHES.get(e.getUUID()));
   }

   public static boolean isRaging(Entity e) {
      return e instanceof ServerPlayer && raging((BlackLashEvents.Lash)LASHES.get(e.getUUID()));
   }

   static boolean copyHolder(ServerPlayer p) {
      return p.getTags().contains("unrp.bl_copy");
   }

   private static int tint(ServerPlayer p) {
      return raging((BlackLashEvents.Lash)LASHES.get(p.getUUID())) ? 16731499 : 5891826;
   }

   private static boolean twin(ServerPlayer p) {
      return score(p, "unrp.unlock_s3") >= 1 && !copyHolder(p);
   }

   static boolean boss(LivingEntity e) {
      return e.getType().is(EntityTypes.BOSSES);
   }

   static boolean light(LivingEntity e) {
      return LashMath.light(e instanceof Player, boss(e), (double)e.getBbWidth(), (double)e.getBbHeight());
   }

   static boolean grabbable(ServerPlayer p, Entity e) {
      if (e instanceof LivingEntity live) {
         if (live != p && live.isAlive() && !live.isSpectator()) {
            if (!(live instanceof ArmorStand)) {
               if (live instanceof Player) {
                  Player pl = (Player)live;
                  if (pl.isCreative()) {
                     return false;
                  }
               }

               if (isDowned(live)) {
                  return false;
               }

               return !TimeWeft.everpresent(live);
            }

            return false;
         }
      }

      return false;
   }

   static boolean ally(ServerPlayer caster, LivingEntity live) {
      if (live == caster) {
         return false;
      } else {
         return com.myriam.unrp.social.SocialService.allied(live, caster) || live instanceof Player && com.myriam.unrp.social.SocialService.sameGroup(caster.server, caster.getUUID(), live.getUUID());
      }
   }

   static Vec3 aim(LivingEntity e) {
      return v(LashMath.aimPoint(a(e.position()), (double)e.getBbHeight()));
   }

   static Vec3 mid(LivingEntity e) {
      return v(LashMath.mid(a(e.position()), (double)e.getBbHeight()));
   }

   private static LivingEntity living(ServerLevel level, int id, UUID uuid) {
      Entity e = level.getEntity(id);
      if (e instanceof LivingEntity live) {
         if (!e.isRemoved() && uuid != null && uuid.equals(e.getUUID())) {
            return live;
         }
      }

      return null;
   }

   static boolean mine(BlackLashEvents.Lash s, Entity e) {
      if (s == null) {
         return false;
      } else {
         UUID id = e.getUUID();
         if (s.pending.containsKey(id)) {
            return true;
         } else {
            for(BlackLashEvents.Held h : s.held) {
               if (id.equals(h.uuid)) {
                  return true;
               }
            }

            for(BlackLashEvents.Move m : s.moves) {
               if (id.equals(m.held.uuid)) {
                  return true;
               }
            }

            for(BlackLashEvents.Thrown th : s.thrown) {
               if (id.equals(th.uuid)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   private static BlackLashEvents.Hit ray(ServerPlayer p, ServerLevel level, double range, boolean blocks, boolean bosses) {
      Vec3 eye = p.getEyePosition();
      Vec3 end = eye.add(p.getLookAngle().scale(range));
      BlockHitResult wall = level.clip(new ClipContext(eye, end, Block.COLLIDER, Fluid.NONE, p));
      Vec3 limit = wall.getType() == Type.MISS ? end : wall.getLocation();
      LivingEntity best = null;
      Vec3 bestAt = null;
      double bestD = Double.MAX_VALUE;
      AABB box = (new AABB(eye, limit)).inflate(1.0D);
      BlackLashEvents.Lash own = (BlackLashEvents.Lash)LASHES.get(p.getUUID());

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, (x) -> grabbable(p, x) && !mine(own, x))) {
         if (bosses || !boss(e)) {
            AABB bb = e.getBoundingBox().inflate(0.3D);
            Optional<Vec3> h = bb.clip(eye, limit);
            Vec3 at = (Vec3)h.orElse(bb.contains(eye) ? eye : null);
            if (at != null) {
               double d = eye.distanceToSqr(at);
               if (d < bestD) {
                  bestD = d;
                  best = e;
                  bestAt = at;
               }
            }
         }
      }

      if (best != null) {
         BlackLashEvents.Hit out_0 = new BlackLashEvents.Hit();
         out_0.entity = best;
         out_0.at = bestAt;
         return out_0;
      } else if (blocks && wall.getType() == Type.BLOCK) {
         BlackLashEvents.Hit out = new BlackLashEvents.Hit();
         out.block = wall;
         out.at = wall.getLocation();
         return out;
      } else {
         return null;
      }
   }

   private static Vec3 aimAt(ServerPlayer p, ServerLevel level) {
      BlackLashEvents.Hit h = ray(p, level, 24.0D, true, true);
      if (h != null && h.entity != null) {
         return aim(h.entity);
      } else {
         return h != null ? h.at : p.getEyePosition().add(p.getLookAngle().scale(16.0D));
      }
   }

   private static List<LivingEntity> foes(ServerPlayer p, ServerLevel level, double r) {
      List<LivingEntity> out = new ArrayList();
      Vec3 c = p.position();

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r + 2.0D))) {
         if (e != p && e.isAlive() && OriginStrike.foe(p, e) && e.position().distanceTo(c) <= r) {
            out.add(e);
         }
      }

      out.sort(OriginStrike.threatsFirst(p, (ex) -> ex.position().distanceToSqr(c)));
      return out;
   }

   private static List<LivingEntity> anyone(ServerPlayer p, ServerLevel level, double r) {
      List<LivingEntity> out = new ArrayList();
      Vec3 c = p.position();

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r + 2.0D))) {
         if (grabbable(p, e) && e.position().distanceTo(c) <= r) {
            out.add(e);
         }
      }

      out.sort(Comparator.comparingDouble((ex) -> ex.position().distanceToSqr(c)));
      return out;
   }

   static boolean bound(LivingEntity e) {
      for(BlackLashEvents.Lash s : LASHES.values()) {
         for(BlackLashEvents.Held h : s.held) {
            if (h.uuid != null && h.uuid.equals(e.getUUID())) {
               return true;
            }
         }

         for(BlackLashEvents.Move m : s.moves) {
            if (m.held.uuid != null && m.held.uuid.equals(e.getUUID())) {
               return true;
            }
         }

         for(BlackLashEvents.Thrown th : s.thrown) {
            if (th.uuid != null && th.uuid.equals(e.getUUID())) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean hurt(ServerPlayer p, ServerLevel level, LivingEntity e, double dmg, Vec3 from, double kb, double lift) {
      return !OriginStrike.fair(p, e) ? false : strike(p, level, e, dmg, from, kb, lift);
   }

   private static boolean hurtAnyone(ServerPlayer p, ServerLevel level, LivingEntity e, double dmg, Vec3 from, double kb, double lift) {
      return !grabbable(p, e) ? false : strike(p, level, e, dmg, from, kb, lift);
   }

   private static boolean plain(ServerPlayer p, ServerLevel level, LivingEntity e, double dmg, Vec3 from, double kb, double lift) {
      if (!grabbable(p, e)) {
         return false;
      } else {
         PLAIN.set(Boolean.TRUE);

         boolean var10;
         try {
            var10 = strike(p, level, e, dmg, from, kb, lift);
         } finally {
            PLAIN.set(Boolean.FALSE);
         }

         return var10;
      }
   }

   private static boolean strike(ServerPlayer p, ServerLevel level, LivingEntity e, double dmg, Vec3 from, double kb, double lift) {
      LASHING.set(Boolean.TRUE);

      boolean landed;
      try {
         landed = OriginStrike.hitFresh(e, level.damageSources().playerAttack(p), (float)dmg);
      } finally {
         LASHING.set(Boolean.FALSE);
      }

      if (landed && e.isAlive() && from != null && kb > 0.0D && !bound(e) && !TimeWeft.everpresent(e)) {
         double resist = e.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
         Vec3 nv = v(LashMath.knock(a(e.getDeltaMovement()), a(e.position()), a(from), kb, lift, resist));
         e.setDeltaMovement(nv);
         e.hurtMarked = true;
      }

      if (landed) {
         Vec3 m = mid(e);
         Fx.burst(level, Fx.glint(tint(p)), m, 6, 0.25D, 0.06D);
         level.sendParticles(ParticleTypes.CRIT, m.x, m.y, m.z, 5, 0.2D, 0.2D, 0.2D, 0.15D);
      }

      return landed;
   }

   private static void crack(ServerLevel level, Vec3 at, float volume) {
      crack(level, at, volume, 5891826);
   }

   private static void crack(ServerLevel level, Vec3 at, float volume, int rgb) {
      sound(level, at, "minecraft:entity.firework_rocket.blast", volume, 1.75F + level.random.nextFloat() * 0.2F);
      Fx.burst(level, Fx.glint(rgb), at, 5, 0.15D, 0.12D);
      level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 4, 0.1D, 0.1D, 0.1D, 0.25D);
   }

   private static void sound(ServerLevel level, Vec3 at, String id, float volume, float pitch) {
      level.playSound((Player)null, at.x, at.y, at.z, CastFxEvents.sound(id), SoundSource.PLAYERS, volume, pitch);
   }

   private static void steer(LivingEntity e, Vec3 want, double cap) {
      Vec3 d = want.subtract(e.position());
      if (d.length() > cap) {
         d = d.normalize().scale(cap);
      }

      e.setDeltaMovement(d);
      e.hurtMarked = true;
      e.fallDistance = 0.0F;
      if (e instanceof Mob m) {
         m.getNavigation().stop();
      }

   }

   private static void hover(LivingEntity e) {
      MobEffectInstance cur = e.getEffect(MobEffects.LEVITATION);
      if (cur == null || cur.getAmplifier() == -1 || cur.isAmbient()) {
         e.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 6, -1, false, false, false));
      }
   }

   private static void unhover(LivingEntity e) {
      MobEffectInstance cur = e.getEffect(MobEffects.LEVITATION);
      if (cur != null && cur.getAmplifier() == -1) {
         e.removeEffect(MobEffects.LEVITATION);
      }

   }

   private static void jumpLock(LivingEntity e) {
      if (e instanceof Player) {
         e.addEffect(new MobEffectInstance(MobEffects.JUMP, 8, 128, false, false));
      }

   }

   private static void fx(ServerPlayer p, int kind, int slot, int target, Vec3 x, Vec3 x2, int age, int value, int bits) {
      int flags = bits | (kind != 17 && raging((BlackLashEvents.Lash)LASHES.get(p.getUUID())) ? 16 : 0);
      LashFxPacket msg = new LashFxPacket(p.getId(), kind, slot, target, (float)x.x, (float)x.y, (float)x.z, (float)x2.x, (float)x2.y, (float)x2.z, age, value, flags);
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p), msg);
   }

   static int lineValue(int seed, int grip) {
      return seed + 8 * grip;
   }

   private static void end(ServerPlayer p, int kind, int slot) {
      fx(p, 17, slot, 0, ZERO, ZERO, 0, kind, 0);
   }

   private static void lashPress(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (!locked(p, s)) {
         if (p.isPassenger()) {
            bar(p, "§7Not while riding.");
         } else if (s.swing == null && (s.sling == null || s.sling.flying >= 0L)) {
            if (p.isShiftKeyDown()) {
               slingStart(p, level, s, now);
            } else {
               snatch(p, level, s, now);
            }

         }
      }
   }

   private static void lashRelease(ServerPlayer p, BlackLashEvents.Lash s, long now) {
      if (s.grab != null) {
         s.grab.released = true;
      }

      if (s.swing != null) {
         swingEnd(p, s, true);
      }

      if (s.sling != null && s.sling.flying < 0L) {
         slingRelease(p, s, now);
      }

   }

   private static void snatch(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (ready(p, s.readySnatch, now, "Snatch")) {
         BlackLashEvents.Hit hit = ray(p, level, 56.0D, true, true);
         if (hit == null) {
            Vec3 tip = p.getEyePosition().add(p.getLookAngle().scale(56.0D));
            int reach = LashMath.gripTicks(56.0D);
            fx(p, 0, 0, 0, tip, ZERO, 0, lineValue((int)(now % 7L), reach), 2);
            later(s, now + (long)reach + 2L, (pp, lv, ss, nn) -> end(pp, 0, 0));
            sound(level, p.position(), "minecraft:item.trident.throw", 0.5F, 1.6F);
            s.readySnatch = now + 6L;
            s.cdSnatch = 6;
         } else if (pay(p, 8)) {
            int cd = cooldown(p, 30);
            s.readySnatch = now + (long)cd;
            s.cdSnatch = cd;
            s.slingShown = false;
            strain(s, 8.0D);
            cancelPull(p, s);
            if (s.sling != null) {
               s.sling = null;
            }

            MoveAnim.play(p, "bl_snatch");
            BlackLashEvents.Grab g = new BlackLashEvents.Grab();
            g.at = now;
            g.seed = (int)(now % 7L);
            g.released = !p.getTags().contains("unrp.bl_lash_held");
            g.fromY = p.getY();
            if (hit.entity != null) {
               g.target = hit.entity.getId();
               g.uuid = hit.entity.getUUID();
               g.light = light(hit.entity);
               g.point = aim(hit.entity);
            } else {
               g.point = hit.at;
               g.block = hit.block.getBlockPos();
               g.face = hit.block.getDirection();
            }

            g.grip = LashMath.gripTicks(p.getEyePosition().distanceTo(g.point));
            s.grab = g;
            fx(p, 0, 0, g.target, g.point, ZERO, 0, lineValue(g.seed, g.grip), g.light ? 1 : 0);
            sound(level, p.position(), "minecraft:item.trident.throw", 0.7F, 1.4F);
         }
      }
   }

   private static void cancelPull(ServerPlayer p, BlackLashEvents.Lash s) {
      if (s.grab != null || s.reel != null || s.zip != null) {
         end(p, 0, 0);
      }

      s.grab = null;
      s.reel = null;
      s.zip = null;
   }

   private static void tickGrab(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      BlackLashEvents.Grab g = s.grab;
      if (g != null) {
         if (!p.getTags().contains("unrp.bl_lash_held")) {
            g.released = true;
         }

         if (now - g.at >= (long)g.grip) {
            s.grab = null;
            if (g.target != 0) {
               LivingEntity e = living(level, g.target, g.uuid);
               if (e != null && e.isAlive() && !((double)e.distanceTo(p) > 60.0D)) {
                  sound(level, e.position(), "minecraft:block.chain.hit", 0.8F, 1.2F);
                  if (g.light) {
                     BlackLashEvents.Reel r = new BlackLashEvents.Reel();
                     r.target = e.getId();
                     r.uuid = e.getUUID();
                     r.grip = now;
                     if (e.isPassenger()) {
                        e.stopRiding();
                     }

                     r.start = e.position();
                     r.ticks = LashMath.reelTicks(LashMath.dist(a(r.start), LashMath.reelStop(a(p.position()), a(r.start))));
                     s.reel = r;
                     if (e instanceof Mob) {
                        Mob m = (Mob)e;
                        m.getNavigation().stop();
                     }
                  } else {
                     zipStart(p, level, s, anchorOf(p, e), e, now);
                  }

               } else {
                  end(p, 0, 0);
               }
            } else {
               BlockState st = level.getBlockState(g.block);
               if (!st.isAir() && !st.getCollisionShape(level, g.block).isEmpty()) {
                  sound(level, g.point, "minecraft:block.chain.place", 0.8F, 1.3F);
                  if (poundable(p, level, g) && pay(p, 12)) {
                     poundStart(p, level, s, g, now);
                  } else {
                     if (!g.released && p.getTags().contains("unrp.bl_lash_held") && swingable(p, g)) {
                        end(p, 0, 0);
                        swingStart(p, s, g, now);
                     } else {
                        zipStart(p, level, s, g.point, (LivingEntity)null, now);
                     }

                  }
               } else {
                  end(p, 0, 0);
               }
            }
         }
      }
   }

   private static Vec3 anchorOf(ServerPlayer p, LivingEntity e) {
      double[] chest = LashMath.chest(a(e.position()), (double)e.getBbHeight());
      return v(LashMath.zipAnchor(chest, a(p.position()), a(e.position()), (double)e.getBbWidth()));
   }

   private static void tickReel(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      BlackLashEvents.Reel r = s.reel;
      if (r != null) {
         LivingEntity e = living(level, r.target, r.uuid);
         if (e != null && e.isAlive() && !TimeWeft.everpresent(e)) {
            double k = LashMath.reelK((int)(now - r.grip), r.ticks);
            double[] stop = LashMath.reelStop(a(p.position()), a(r.start));
            Vec3 want = v(LashMath.reelAt(a(r.start), stop, k));
            steer(e, want, 2.5D);
            jumpLock(e);
            if (k >= 1.0D) {
               s.reel = null;
               e.setDeltaMovement(0.0D, Math.min(0.0D, e.getDeltaMovement().y), 0.0D);
               e.hurtMarked = true;
               e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, true));
               hurt(p, level, e, LashMath.reelHit(raging(s)), (Vec3)null, 0.0D, 0.0D);
               end(p, 0, 0);
               sound(level, e.position(), "minecraft:entity.player.attack.knockback", 0.8F, 1.2F);
            }

         } else {
            s.reel = null;
            end(p, 0, 0);
         }
      }
   }

   private static void zipStart(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, Vec3 anchor, LivingEntity target, long now) {
      BlackLashEvents.Zip z = new BlackLashEvents.Zip();
      z.anchor = anchor;
      if (target != null) {
         z.target = target.getId();
         z.uuid = target.getUUID();
      }

      z.at = now;
      z.most = LashMath.zipMost(anchor.distanceTo(p.position().add(0.0D, 1.2D, 0.0D)));
      z.vel = v(LashMath.zipStart(a(anchor), a(p.position()))).scale(LashMath.rageSpeed(raging(s)));
      s.zip = z;
      p.setDeltaMovement(z.vel);
      p.hurtMarked = true;
      p.fallDistance = 0.0F;
      s.soft = true;
      sound(level, p.position(), "minecraft:item.trident.riptide_1", 0.7F, 1.4F);
   }

   private static boolean poundable(ServerPlayer p, ServerLevel level, BlackLashEvents.Grab g) {
      boolean air = !p.onGround() && !p.isPassenger() && !p.isInWater() && !p.isInLava() && !p.onClimbable();
      boolean dry = level.getFluidState(BlockPos.containing(g.point.x, g.point.y + 0.2D, g.point.z)).isEmpty();
      return LashMath.poundable(a(p.position()), a(g.point), air && dry, g.face == Direction.UP);
   }

   private static void poundStart(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, BlackLashEvents.Grab g, long now) {
      BlackLashEvents.Zip z = new BlackLashEvents.Zip();
      z.anchor = g.point;
      z.block = g.block;
      z.pound = true;
      z.fromY = Math.max(g.fromY, p.getY());
      z.lastY = p.getY();
      z.at = now;
      z.most = 40;
      z.vel = v(LashMath.poundVel(a(g.point.subtract(p.position())), 0)).scale(LashMath.rageSpeed(raging(s)));
      s.zip = z;
      int cd = cooldown(p, 80);
      s.readySnatch = now + (long)cd;
      s.cdSnatch = cd;
      strain(s, 12.0D);
      p.setDeltaMovement(z.vel);
      p.hurtMarked = true;
      p.fallDistance = 0.0F;
      s.soft = true;
      MoveAnim.play(p, "bl_slam");
      sound(level, p.position(), "minecraft:item.trident.riptide_1", 0.9F, 0.7F);
   }

   private static void tickPound(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, BlackLashEvents.Zip z, long now) {
      int age = (int)(now - z.at);
      double y = p.getY();
      boolean fell = z.lastY - y >= 0.05D;
      z.going = z.going || fell;
      z.stall = z.going && !fell ? z.stall + 1 : 0;
      z.lastY = y;
      p.fallDistance = 0.0F;
      double gap = y - ground(level, p.getX(), y + 0.5D, p.getZ());
      if (!p.isInWater() && !p.isInLava() && !p.onClimbable() && !p.isPassenger() && !level.getBlockState(z.block).getCollisionShape(level, z.block).isEmpty()) {
         if (LashMath.poundLanded(p.onGround(), y - z.anchor.y, z.stall, age, gap)) {
            s.zip = null;
            end(p, 0, 0);
            poundLand(p, level, s, z);
         } else if (LashMath.poundLost(age, gap)) {
            s.zip = null;
            end(p, 0, 0);
         } else {
            z.vel = v(LashMath.poundVel(a(z.anchor.subtract(p.position())), age)).scale(LashMath.rageSpeed(raging(s)));
            p.setDeltaMovement(z.vel);
            p.hurtMarked = true;
            Fx.burst(level, Fx.drop(1840168), p.position().add(0.0D, 1.0D, 0.0D), 3, 0.18D, 0.02D);
         }
      } else {
         s.zip = null;
         end(p, 0, 0);
      }
   }

   private static void poundLand(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, BlackLashEvents.Zip z) {
      double gy = ground(level, p.getX(), p.getY() + 0.5D, p.getZ());
      Vec3 at = new Vec3(p.getX(), gy, p.getZ());
      double drop = Math.max(0.0D, z.fromY - gy);
      double k = LashMath.poundK(drop);
      double reach = LashMath.poundReach(k);
      p.setDeltaMovement(0.0D, 0.1D, 0.0D);
      p.hurtMarked = true;
      p.fallDistance = 0.0F;
      s.soft = true;
      int epoch = s.epoch;

      for(LivingEntity e : foes(p, level, reach)) {
         hurt(p, level, e, LashMath.poundHit(k, e.position().distanceTo(at)), at, LashMath.poundKnock(k), LashMath.poundUp(k));
         if (s.epoch != epoch) {
            return;
         }
      }

      BlockState st = level.getBlockState(BlockPos.containing(at.x, at.y - 0.2D, at.z));
      double crater = LashMath.poundCrater(drop);
      if (crater > 0.0D) {
         NadirEvents.sunCrater(level, at, crater, p);
      }

      fx(p, 26, 0, 0, at, ZERO, 0, (int)Math.round(reach * 10.0D), crater > 0.0D ? 32 : 0);
      if (!st.isAir()) {
         level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, st), at.x, at.y + 0.1D, at.z, 20 + (int)Math.round(30.0D * k), reach * 0.3D, 0.1D, reach * 0.3D, 0.2D);
      }

      Fx.ring(level, tint(p), at.x, at.y + 0.1D, at.z, reach, 16);
      Fx.burst(level, Fx.drop(1840168), at.add(0.0D, 0.3D, 0.0D), 20, reach * 0.35D, 0.08D);
      sound(level, at, "minecraft:entity.warden.attack_impact", 1.0F, 0.75F);
      sound(level, at, "minecraft:entity.generic.explode", 0.5F + 0.4F * (float)k, 0.9F);
   }

   private static void tickZip(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      BlackLashEvents.Zip z = s.zip;
      if (z != null) {
         if (z.pound) {
            tickPound(p, level, s, z, now);
         } else {
            LivingEntity e = z.target != 0 ? living(level, z.target, z.uuid) : null;
            boolean lost = z.target != 0 && (e == null || !e.isAlive());
            if (e != null && e.isAlive()) {
               z.anchor = anchorOf(p, e);
            }

            Vec3 hand = p.position().add(0.0D, 1.2D, 0.0D);
            Vec3 to = z.anchor.subtract(hand);
            double dist = to.length();
            int age = (int)(now - z.at);
            z.stall = dist > z.lastDist - 0.05D ? z.stall + 1 : 0;
            z.lastDist = dist;
            p.fallDistance = 0.0F;
            if (!LashMath.zipDone(dist, age, z.most) && z.stall < 3 && !lost) {
               z.vel = v(LashMath.zipVel(a(z.vel), a(to), 1.6D));
               z.vel = z.vel.scale(LashMath.rageSpeed(raging(s)));
               p.setDeltaMovement(z.vel);
               p.hurtMarked = true;
            } else {
               s.zip = null;
               end(p, 0, 0);
               s.soft = true;
               if (e != null && e.isAlive()) {
                  hurt(p, level, e, 3.0D, p.position(), 0.1D, 0.3D);
                  p.setDeltaMovement(v(LashMath.zipBounce(a(to))));
                  p.hurtMarked = true;
                  sound(level, e.position(), "minecraft:entity.player.attack.strong", 0.9F, 1.1F);
               } else {
                  double[] flat = LashMath.vaultFlat(a(to));
                  p.setDeltaMovement(0.0D, 0.62D, 0.0D);
                  p.hurtMarked = true;
                  later(s, now + 5L, (pp, lv, ss, nn) -> {
                     Vec3 m = measured(ss);
                     pp.setDeltaMovement(m.x + flat[0] * 0.34D, m.y, m.z + flat[2] * 0.34D);
                     pp.hurtMarked = true;
                     pp.fallDistance = 0.0F;
                  });
                  fx(p, 1, 0, 0, z.anchor, ZERO, 0, 0, 0);
                  sound(level, z.anchor, "minecraft:entity.player.attack.knockback", 0.6F, 1.5F);
               }

            }
         }
      }
   }

   private static double handY(ServerPlayer p) {
      return 1.9D * (double)p.getBbHeight() / 1.8D;
   }

   private static boolean swingable(ServerPlayer p, BlackLashEvents.Grab g) {
      return !p.onGround() || LashMath.swingHigh(a(p.position().add(0.0D, handY(p), 0.0D)), a(g.point));
   }

   private static void swingStart(ServerPlayer p, BlackLashEvents.Lash s, BlackLashEvents.Grab g, long now) {
      Vec3 hand = p.position().add(0.0D, handY(p), 0.0D);
      BlackLashEvents.Swing sw = new BlackLashEvents.Swing();
      sw.anchor = g.point;
      sw.block = g.block;
      sw.face = g.face;
      sw.len = hand.distanceTo(g.point);
      boolean ground = p.onGround();
      sw.target = LashMath.swingTarget(sw.len, g.point.y - hand.y, ground);
      sw.vel = v(LashMath.swingEntry(a(measured(s)), a(hand), a(g.point)));
      if (ground) {
         sw.vel = v(LashMath.swingKick(a(p.getLookAngle()), a(hand), a(g.point)));
      }

      sw.since = now;
      sw.track = LashMath.swingTrack(a(hand));
      s.swing = sw;
      s.inAt = -1000L;
      fx(p, 2, 0, 0, sw.anchor, ZERO, 2, 3, 0);
      if (s.swingHints < 2) {
         ++s.swingHints;
         bar(p, "§3Swinging: forward and back pump, left and right steer, sneak reels in, jump lets out.");
      }

   }

   private static boolean burning(ServerLevel level, BlockPos block, Direction face) {
      BlockPos side = face == null ? block.above() : block.relative(face);
      BlockState st = level.getBlockState(side);
      return st.is(BlockTags.FIRE) || level.getFluidState(side).is(FluidTags.LAVA) || level.getBlockState(block).is(BlockTags.FIRE);
   }

   private static void tickSwing(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      BlackLashEvents.Swing sw = s.swing;
      if (sw != null) {
         if (!p.getTags().contains("unrp.bl_lash_held")) {
            swingEnd(p, s, true);
         } else {
            BlockState st = level.getBlockState(sw.block);
            if (!st.isAir() && !st.getCollisionShape(level, sw.block).isEmpty()) {
               if (burning(level, sw.block, sw.face)) {
                  ++sw.burn;
                  if (sw.burn >= 20) {
                     swingEnd(p, s, false);
                     bar(p, "§6Fire burns the tendril loose.");
                     sound(level, sw.anchor, "minecraft:block.fire.extinguish", 0.7F, 1.2F);
                     return;
                  }
               } else {
                  sw.burn = 0;
               }

               if (score(p, "unrp.aura") <= 0) {
                  swingEnd(p, s, true);
               } else {
                  strain(s, (sw.pose == 1 ? 0.5D : 2.0D) / 20.0D);
                  hover(p);
                  Vec3 seen = (sw.seen != null ? sw.seen : p.position()).add(0.0D, handY(p), 0.0D);
                  sw.seen = null;
                  double[] off = LashMath.swingSeen(sw.track, a(seen));
                  sw.track = LashMath.swingShift(sw.track, off);
                  Vec3 hand = v(sw.track[0]);
                  double dist = hand.distanceTo(sw.anchor);
                  if (LashMath.swingSnap(off, dist, sw.len)) {
                     swingEnd(p, s, false);
                     bar(p, "§7The tendril snaps loose.");
                  } else {
                     boolean keys = !p.onGround() && now - s.inAt <= 3L;
                     double forward = keys ? s.inForward : 0.0D;
                     double right = keys ? s.inRight : 0.0D;
                     if (keys) {
                        sw.target = LashMath.swingAim(sw.target, dist, s.inReel, s.inSlack);
                     }

                     double was = sw.len;
                     sw.len = LashMath.swingRope(sw.len, sw.target);
                     double give = Math.max(0.0D, sw.len - was);
                     boolean slack = dist < sw.len - 0.05D;
                     if (p.onGround() && slack) {
                        sw.vel = measured(s);
                        sw.track = LashMath.swingTrack(a(seen));
                        sw.pose = -1;
                        sw.still = 0;
                     } else {
                        boolean pushing = forward != 0.0D || right != 0.0D;
                        double[] push = LashMath.swingPush((double)p.getYRot(), forward, right);
                        double height = LashMath.swingHeight(a(hand), a(sw.vel), a(sw.anchor), sw.len);
                        double damp = LashMath.swingDamp(height, LashMath.dot(push, a(sw.vel)) > 0.0D, pushing, sw.carry);
                        double[][] step = LashMath.swingMove(a(hand), a(sw.vel), a(sw.anchor), sw.len, push, damp, give);
                        Vec3 nv = v(step[1]);
                        Vec3 move = v(step[0]).subtract(hand);
                        if (p.onGround() && nv.y < 0.0D) {
                           nv = new Vec3(nv.x, 0.0D, nv.z);
                           move = new Vec3(move.x, Math.max(0.0D, move.y), move.z);
                        }

                        if (p.horizontalCollision) {
                           nv = new Vec3(nv.x * 0.5D, nv.y, nv.z * 0.5D);
                           move = new Vec3(move.x * 0.5D, move.y, move.z * 0.5D);
                        }

                        sw.carry = LashMath.swingCarry(sw.carry, pushing, sw.vel.y, nv.y, (int)(now - sw.since));
                        sw.vel = nv;
                        sw.still = LashMath.swingStill(sw.still, height, pushing);
                        sw.pose = LashMath.swingPose(LashMath.swingHanging(sw.still), forward, right);
                        holdPose(p, sw.pose);
                        sw.track = LashMath.swingAdd(sw.track, a(hand.add(move)));
                        p.setDeltaMovement(move);
                        com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new LashMovePacket(move.x, move.y, move.z, sw.anchor.x, sw.anchor.y, sw.anchor.z, sw.len));
                        p.fallDistance = 0.0F;
                     }
                  }
               }
            } else {
               swingEnd(p, s, false);
               bar(p, "§7The anchor gives way.");
            }
         }
      }
   }

   private static void holdPose(ServerPlayer p, int pose) {
      switch (pose) {
         case 1:
            MoveAnim.hold(p, "bl_hang", 3);
            break;
         case 2:
            MoveAnim.hold(p, "bl_pump", 3);
            break;
         case 3:
            MoveAnim.hold(p, "bl_tuck", 3);
            break;
         case 4:
            MoveAnim.hold(p, "bl_veer", 3);
            break;
         case 5:
            MoveAnim.hold(p, "bl_veer_l", 3);
            break;
         default:
            MoveAnim.hold(p, "bl_swing", 3);
      }

   }

   public static void swingInput(ServerPlayer p, int forward, int right, int flags) {
      BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.get(p.getUUID());
      if (s != null && s.swing != null) {
         s.inForward = Math.max(-1.0D, Math.min(1.0D, (double)forward / 100.0D));
         s.inRight = Math.max(-1.0D, Math.min(1.0D, (double)right / 100.0D));
         s.inReel = (flags & 1) != 0;
         s.inSlack = (flags & 2) != 0;
         s.inAt = p.serverLevel().getGameTime();
      }
   }

   private static void tickFlip(ServerPlayer p, BlackLashEvents.Lash s, long now) {
      if (s.flipAt >= 0L) {
         long age = now - s.flipAt;
         if (age <= 18L && age >= 0L) {
            if (age >= 3L && (p.onGround() || p.isInWater() || p.isInLava() || p.onClimbable() || p.isPassenger() || s.swing != null || s.zip != null)) {
               MoveAnim.stop(p, "bl_flip");
               s.flipAt = -1L;
            }

         } else {
            s.flipAt = -1L;
         }
      }
   }

   private static void swingEnd(ServerPlayer p, BlackLashEvents.Lash s, boolean fling) {
      BlackLashEvents.Swing sw = s.swing;
      if (sw != null) {
         s.swing = null;
         unhover(p);
         if (fling) {
            boolean flip = LashMath.swingFlip(a(sw.vel));
            p.setDeltaMovement(v(LashMath.swingLetGo(a(sw.vel))));
            p.hurtMarked = true;
            if (LashMath.swingFling(a(sw.vel)) > 0.0D) {
               sound(p.serverLevel(), p.position(), "minecraft:item.trident.riptide_1", 0.6F, 1.6F);
            }

            if (flip) {
               MoveAnim.play(p, "bl_flip");
               s.flipAt = p.serverLevel().getGameTime();
               sound(p.serverLevel(), p.position(), "minecraft:entity.player.attack.sweep", 0.5F, 1.5F);
            }
         }

         s.soft = true;
         p.fallDistance = 0.0F;
         end(p, 2, 0);
      }
   }

   private static Vec3 anchorSide(ServerPlayer p, ServerLevel level, int side, BlockPos[] out) {
      Vec3 eye = p.getEyePosition();

      for(int k = 0; k < LashMath.slingTries(); ++k) {
         float pitch = LashMath.slingPitch(p.getXRot(), k);

         for(float ang : LashMath.SLING_YAWS) {
            Vec3 dir = Vec3.directionFromRotation(pitch, p.getYRot() + (float)side * ang);
            Vec3 end = eye.add(dir.scale(8.0D));
            BlockHitResult hit = level.clip(new ClipContext(eye, end, Block.COLLIDER, Fluid.NONE, p));
            if (hit.getType() == Type.BLOCK && hit.getLocation().distanceTo(eye) > 1.2D) {
               out[0] = hit.getBlockPos();
               return hit.getLocation();
            }
         }
      }

      return null;
   }

   private static void slingStart(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (ready(p, s.readySling, now, "Slingshot")) {
         BlockPos[] lb = new BlockPos[1];
         BlockPos[] rb = new BlockPos[1];
         Vec3 left = anchorSide(p, level, -1, lb);
         Vec3 right = anchorSide(p, level, 1, rb);
         if (left != null && right != null) {
            if (pay(p, 14)) {
               int cd = cooldown(p, 120);
               s.readySling = now + (long)cd;
               s.cdSling = cd;
               s.slingShown = true;
               strain(s, 15.0D);
               cancelPull(p, s);
               BlackLashEvents.Sling sl = new BlackLashEvents.Sling();
               sl.left = left;
               sl.right = right;
               sl.leftBlock = lb[0];
               sl.rightBlock = rb[0];
               sl.at = now;
               s.sling = sl;
               fx(p, 3, 0, 0, left, right, 0, 0, 0);
               sound(level, p.position(), "minecraft:entity.leash_knot.place", 0.9F, 0.8F);
            }
         } else {
            bar(p, "§7Nothing in reach on either side to anchor the tendrils to.");
         }
      }
   }

   private static boolean solid(ServerLevel level, BlockPos pos) {
      BlockState st = level.getBlockState(pos);
      return !st.isAir() && !st.getCollisionShape(level, pos).isEmpty();
   }

   private static void tickSling(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      BlackLashEvents.Sling sl = s.sling;
      if (sl != null) {
         if (sl.flying < 0L) {
            sl.charge = (int)LashMath.slingCharge((int)(now - sl.at));
            if (!p.getTags().contains("unrp.bl_lash_held")) {
               slingRelease(p, s, now);
            } else if (solid(level, sl.leftBlock) && solid(level, sl.rightBlock)) {
               Vec3 hand = p.position().add(0.0D, 1.2D, 0.0D);
               if (hand.distanceTo(sl.left) > 12.0D || hand.distanceTo(sl.right) > 12.0D) {
                  slingRelease(p, s, now);
               }

            } else {
               s.sling = null;
               end(p, 3, 0);
               bar(p, "§7The anchor gives way.");
            }
         } else {
            p.fallDistance = 0.0F;
            long since = now - sl.flying;
            if (since > 4L && p.onGround() || since > 100L || p.isInWater() || p.isPassenger()) {
               s.sling = null;
               s.soft = true;
               if (p.onGround()) {
                  fx(p, 5, 0, 0, p.position(), ZERO, 0, 0, 0);
                  sound(level, p.position(), "minecraft:entity.generic.big_fall", 0.8F, 1.1F);
                  if (raging(s)) {
                     for(LivingEntity e : foes(p, level, 4.0D)) {
                        hurt(p, level, e, 4.0D, p.position(), 0.7D, 0.4D);
                     }
                  }
               }
            }

         }
      }
   }

   private static void slingRelease(ServerPlayer p, BlackLashEvents.Lash s, long now) {
      BlackLashEvents.Sling sl = s.sling;
      if (sl != null && sl.flying < 0L) {
         Vec3 dir = v(LashMath.slingDir(a(sl.left), a(sl.right), a(p.position()), (double)p.getXRot()));
         double speed = LashMath.slingSpeed((double)sl.charge);
         speed *= LashMath.rageSpeed(raging(s));
         p.setDeltaMovement(dir.scale(speed));
         p.hurtMarked = true;
         p.fallDistance = 0.0F;
         s.soft = true;
         sl.flying = now;
         end(p, 3, 0);
         fx(p, 4, 0, 0, p.position().add(0.0D, 1.2D, 0.0D), ZERO, 0, 0, 0);
         MoveAnim.stop(p, "bl_sling");
         ServerLevel level = p.serverLevel();
         sound(level, p.position(), "minecraft:item.crossbow.shoot", 1.0F, 0.7F);
         sound(level, p.position(), "minecraft:item.trident.riptide_2", 0.6F, 1.3F);
      }
   }

   private static void bindPress(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (s.held.isEmpty() && s.pending.isEmpty()) {
         if (!locked(p, s) && ready(p, s.readyBind, now, "Bind")) {
            BlackLashEvents.Hit hit = ray(p, level, 10.0D, false, false);
            if (hit != null && hit.entity != null) {
               if (bound(hit.entity)) {
                  bar(p, "§7Other tendrils already hold it.");
               } else {
                  List<LivingEntity> targets = new ArrayList();
                  targets.add(hit.entity);
                  if (twin(p)) {
                     LivingEntity second = secondTarget(p, level, hit.entity);
                     if (second != null) {
                        targets.add(second);
                     }
                  }

                  if (pay(p, 20)) {
                     int cd = cooldown(p, 240);
                     s.readyBind = now + (long)cd;
                     s.cdBind = cd;
                     s.bindAt = now;
                     strain(s, 20.0D);
                     MoveAnim.play(p, "bl_bind");

                     for(int i = 0; i < targets.size(); ++i) {
                        seize(p, s, now, (LivingEntity)targets.get(i), i);
                     }

                     sound(level, p.position(), "minecraft:item.trident.throw", 0.7F, 1.2F);
                  }
               }
            } else {
               bar(p, "§7No one in reach to bind.");
            }
         }
      } else if (p.isShiftKeyDown()) {
         if (!s.held.isEmpty()) {
            slam(p, level, s, now);
         }

      } else {
         BlackLashEvents.Hit near = ray(p, level, 10.0D, false, false);
         if (near == null || near.entity == null || !take(p, level, s, now, near.entity)) {
            if (!s.held.isEmpty()) {
               throwHeld(p, level, s, now);
            }

         }
      }
   }

   private static boolean take(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now, LivingEntity t) {
      if (locked(p, s)) {
         return true;
      } else {
         int slot = LashMath.freeSlot(used(s), LashMath.bindMost(twin(p)));
         if (slot >= 0 && !bound(t)) {
            if (!pay(p, 10)) {
               return true;
            } else {
               strain(s, 10.0D);
               MoveAnim.play(p, "bl_bind");
               seize(p, s, now, t, slot);
               sound(level, p.position(), "minecraft:item.trident.throw", 0.7F, 1.35F);
               return true;
            }
         } else {
            return false;
         }
      }
   }

   private static boolean[] used(BlackLashEvents.Lash s) {
      boolean[] u = new boolean[6];

      for(BlackLashEvents.Held h : s.held) {
         if (h.slot >= 0 && h.slot < u.length) {
            u[h.slot] = true;
         }
      }

      for(BlackLashEvents.Move m : s.moves) {
         if (m.held.slot >= 0 && m.held.slot < u.length) {
            u[m.held.slot] = true;
         }
      }

      for(int taken : s.pending.values()) {
         if (taken >= 0 && taken < u.length) {
            u[taken] = true;
         }
      }

      return u;
   }

   private static void seize(ServerPlayer p, BlackLashEvents.Lash s, long now, LivingEntity t, int slot) {
      s.pending.put(t.getUUID(), slot);
      fx(p, 6, slot, t.getId(), ZERO, ZERO, 0, slot + 2, slot == 1 ? 4 : 0);
      later(s, now + 7L, (pp, lv, ss, nn) -> grabHold(pp, lv, ss, nn, t, slot));
   }

   private static LivingEntity secondTarget(ServerPlayer p, ServerLevel level, LivingEntity first) {
      LivingEntity best = null;
      double bd = Double.MAX_VALUE;
      int br = 2;

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(10.0D))) {
         if (e != first && grabbable(p, e) && !boss(e) && OriginStrike.foe(p, e) && !bound(e)) {
            double d = (double)e.distanceTo(p);
            if (!(d > 10.0D) && LashMath.whipFront(a(p.position()), a(e.position()), (double)p.getYRot())) {
               int rk = OriginStrike.rank(p, e);
               if (rk < br || rk == br && d < bd) {
                  br = rk;
                  bd = d;
                  best = e;
               }
            }
         }
      }

      return best;
   }

   private static void grabHold(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now, LivingEntity t, int slot) {
      s.pending.remove(t.getUUID());
      if (s.frenzy <= 0 && t.isAlive() && !t.isRemoved() && t.level() == level && !((double)t.distanceTo(p) > 14.0D) && !TimeWeft.everpresent(t) && !bound(t)) {
         if (t.isPassenger()) {
            t.stopRiding();
         }

         BlackLashEvents.Held h = new BlackLashEvents.Held();
         h.id = t.getId();
         h.uuid = t.getUUID();
         h.slot = slot;
         h.at = now;
         h.from = t.position();
         h.hp = t.getHealth();
         s.held.add(h);
         if (t instanceof ServerPlayer) {
            ServerPlayer vp = (ServerPlayer)t;
            StruggleEvents.begin(vp, 8, 5, 610, () -> h.broke = true);
         }

         if (t instanceof Mob) {
            Mob m = (Mob)t;
            m.getNavigation().stop();
         }

         sound(level, t.position(), "minecraft:block.chain.hit", 1.0F, 0.8F);
      } else {
         end(p, 6, slot);
      }
   }

   private static void letGo(ServerPlayer p, LivingEntity e, BlackLashEvents.Held h, boolean down) {
      end(p, 6, h.slot);
      if (e instanceof ServerPlayer vp) {
         StruggleEvents.end(vp, 5);
         unhover(vp);
      }

      if (down) {
         e.fallDistance = 0.0F;
         CAUGHT.put(e.getUUID(), e.level().getGameTime() + 100L);
      }

   }

   private static void tickHeld(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (!s.held.isEmpty()) {
         int count = s.held.size();
         Iterator<BlackLashEvents.Held> it = s.held.iterator();

         while(it.hasNext()) {
            BlackLashEvents.Held h = (BlackLashEvents.Held)it.next();
            LivingEntity e = living(level, h.id, h.uuid);
            if (e != null && e.isAlive()) {
               boolean broke = LashMath.bindBroken((double)h.hp, (double)e.getHealth(), h.broke ? 8 : 0);
               boolean spent = now - h.at > 600L;
               if (!broke && !spent && !TimeWeft.everpresent(e) && !((double)e.distanceTo(p) > 16.0D)) {
                  double[] spot = LashMath.holdSpot(a(p.position()), (double)p.getYRot(), h.slot, count);
                  Vec3 want = v(LashMath.holdAt(a(h.from), spot, (int)(now - h.at), (double)now, h.slot));
                  steer(e, want, 3.2D);
                  jumpLock(e);
                  if (e instanceof ServerPlayer) {
                     hover(e);
                  }

                  if (raging(s) && LashMath.rageHoldBeat((int)(now - h.at)) && OriginStrike.threat(p, e)) {
                     float before = e.getHealth();
                     int epoch = s.epoch;
                     plain(p, level, e, 2.0D, (Vec3)null, 0.0D, 0.0D);
                     if (s.epoch != epoch) {
                        return;
                     }

                     h.hp -= Math.max(0.0F, before - e.getHealth());
                  }
               } else {
                  letGo(p, e, h, true);
                  it.remove();
                  if (broke) {
                     bar(p, "§7It breaks free of the tendril.");
                  } else if (spent) {
                     bar(p, "§7The tendril sets it down.");
                  }
               }
            } else {
               end(p, 6, h.slot);
               it.remove();
            }
         }

      }
   }

   private static void throwHeld(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      Vec3 at = aimAt(p, level);
      MoveAnim.play(p, "bl_throw");

      for(BlackLashEvents.Held h : s.held) {
         LivingEntity e = living(level, h.id, h.uuid);
         if (e == null) {
            end(p, 6, h.slot);
         } else {
            BlackLashEvents.Move m = new BlackLashEvents.Move();
            m.held = h;
            m.at = now;
            m.from = e.position();
            m.back = v(LashMath.windupSpot(a(p.position()), (double)p.getYRot(), h.slot));
            m.aim = at;
            s.moves.add(m);
         }
      }

      s.held.clear();
      sound(level, p.position(), "minecraft:entity.player.attack.sweep", 1.0F, 0.7F);
   }

   private static double ground(ServerLevel level, double x, double y, double z) {
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      int top = (int)Math.floor(y);

      for(int dy = 0; dy < 10; ++dy) {
         pos.set((int)Math.floor(x), top - dy, (int)Math.floor(z));
         VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
         if (!shape.isEmpty()) {
            return (double)(top - dy) + shape.max(Axis.Y);
         }
      }

      return y - 2.0D;
   }

   private static void slam(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      MoveAnim.play(p, "bl_slam");

      for(BlackLashEvents.Held h : s.held) {
         LivingEntity e = living(level, h.id, h.uuid);
         if (e == null) {
            end(p, 6, h.slot);
         } else {
            double[] spot = LashMath.slamSpot(a(p.position()), (double)p.getYRot(), h.slot);
            BlackLashEvents.Move m = new BlackLashEvents.Move();
            m.held = h;
            m.at = now;
            m.slam = true;
            m.from = e.position();
            m.to = new Vec3(spot[0], ground(level, spot[0], p.getY() + 2.0D, spot[2]), spot[2]);
            s.moves.add(m);
            fx(p, 8, h.slot, e.getId(), ZERO, ZERO, 0, h.slot + 7, h.slot == 1 ? 4 : 0);
         }
      }

      s.held.clear();
      sound(level, p.position(), "minecraft:entity.player.attack.sweep", 1.0F, 0.5F);
   }

   private static void tickMoves(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (!s.moves.isEmpty()) {
         Iterator<BlackLashEvents.Move> it = s.moves.iterator();

         while(it.hasNext()) {
            BlackLashEvents.Move m = (BlackLashEvents.Move)it.next();
            LivingEntity e = living(level, m.held.id, m.held.uuid);
            if (e != null && e.isAlive()) {
               int age = (int)(now - m.at);
               if (!m.slam) {
                  if (age < 4) {
                     steer(e, v(LashMath.windupAt(a(m.from), a(m.back), age)), 2.0D);
                  } else {
                     it.remove();
                     letGo(p, e, m.held, false);
                     e.setDeltaMovement(v(LashMath.throwVel(a(m.aim), a(e.position()), (double)e.getBbHeight())));
                     e.hurtMarked = true;
                     BlackLashEvents.Thrown th = new BlackLashEvents.Thrown();
                     th.id = e.getId();
                     th.uuid = e.getUUID();
                     th.at = now;
                     s.thrown.add(th);
                     fx(p, 7, 0, e.getId(), ZERO, ZERO, 0, 0, 0);
                  }
               } else if (age < 4) {
                  steer(e, v(LashMath.slamAt(a(m.from), a(m.to), age)), 3.0D);
               } else {
                  it.remove();
                  letGo(p, e, m.held, false);
                  if (e instanceof ServerPlayer) {
                     steer(e, m.to, 3.0D);
                  } else {
                     e.teleportTo(m.to.x, m.to.y, m.to.z);
                  }

                  e.setDeltaMovement(0.0D, 0.12D, 0.0D);
                  e.hurtMarked = true;
                  int epoch = s.epoch;
                  hurt(p, level, e, 8.0D, (Vec3)null, 0.0D, 0.0D);
                  if (s.epoch != epoch) {
                     return;
                  }

                  e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, true));
                  fx(p, 5, 0, 0, m.to, ZERO, 0, 0, 32);
                  sound(level, m.to, "minecraft:entity.warden.attack_impact", 0.9F, 1.0F);
                  Fx.burst(level, Fx.drop(1840168), m.to.add(0.0D, 0.2D, 0.0D), 14, 0.6D, 0.08D);
               }
            } else {
               end(p, 6, m.held.slot);
               it.remove();
            }
         }

      }
   }

   private static void tickThrown(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (!s.thrown.isEmpty()) {
         Iterator<BlackLashEvents.Thrown> it = s.thrown.iterator();

         while(it.hasNext()) {
            BlackLashEvents.Thrown th = (BlackLashEvents.Thrown)it.next();
            LivingEntity e = living(level, th.id, th.uuid);
            if (e != null && e.isAlive()) {
               long age = now - th.at;
               if (!th.hitOther) {
                  Vec3 m = mid(e);

                  for(LivingEntity o : level.getEntitiesOfClass(LivingEntity.class, e.getBoundingBox().inflate(1.5D))) {
                     if (o != e && o != p && o.isAlive() && !o.isSpectator() && !bound(o) && mid(o).distanceTo(m) < 1.1D) {
                        th.hitOther = true;
                        Vec3 ev = e.getDeltaMovement();
                        int epoch_0 = s.epoch;
                        hurt(p, level, o, 6.0D, o.position().subtract(ev.x * 4.0D, 0.0D, ev.z * 4.0D), 0.8D, 0.35D);
                        hurt(p, level, e, 6.0D, o.position(), 0.2D, 0.3D);
                        if (s.epoch != epoch_0) {
                           return;
                        }

                        e.setDeltaMovement(e.getDeltaMovement().scale(0.2D));
                        e.hurtMarked = true;
                        fx(p, 5, 0, 0, o.position(), ZERO, 0, 0, 0);
                        sound(level, o.position(), "minecraft:entity.player.attack.strong", 1.0F, 0.8F);
                        break;
                     }
                  }
               }

               if (age > 30L || e.onGround() && age > 2L || age > 2L && e.horizontalCollision) {
                  if (!th.hitOther) {
                     int epoch = s.epoch;
                     hurt(p, level, e, 6.0D, (Vec3)null, 0.0D, 0.0D);
                     if (s.epoch != epoch) {
                        return;
                     }

                     fx(p, 5, 0, 0, e.position(), ZERO, 0, 0, 0);
                     sound(level, e.position(), "minecraft:entity.generic.big_fall", 0.8F, 0.9F);
                  }

                  it.remove();
               }
            } else {
               it.remove();
            }
         }

      }
   }

   private static void whipPress(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (!locked(p, s)) {
         if (p.isShiftKeyDown()) {
            ward(p, level, s, now);
         } else if (ready(p, s.readyWhip, now, "Whiplash") && pay(p, 16)) {
            int cd = cooldown(p, 100);
            s.readyWhip = now + (long)cd;
            s.cdWhip = cd;
            s.wardShown = false;
            strain(s, 12.0D);
            MoveAnim.play(p, "bl_whip");
            List<UUID> used = new ArrayList();

            for(int i = 0; i < 3; ++i) {
               int k = i;
               later(s, now + (long)LashMath.WHIP_AT[i], (pp, lv, ss, nn) -> stroke(pp, lv, ss, nn, k, used));
            }

         }
      }
   }

   private static void stroke(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now, int i, List<UUID> used) {
      double yaw = (double)p.getYRot();
      double[] feet = a(p.position());
      List<LivingEntity> fan = new ArrayList();

      for(LivingEntity e : foes(p, level, 8.0D)) {
         if (LashMath.whipFront(feet, a(e.position()), yaw)) {
            fan.add(e);
         }
      }

      fan.sort(OriginStrike.threatsFirst(p, (e) -> LashMath.whipRank(used.contains(e.getUUID()), e.position().distanceTo(p.position()))));
      LivingEntity t = fan.isEmpty() ? null : (LivingEntity)fan.get(0);
      if (t != null) {
         used.add(t.getUUID());
      }

      Vec3 point = t != null ? aim(t) : v(LashMath.whipMiss(feet, yaw));
      int flags = (i == 1 ? 4 : 0) | (i == 2 ? 8 : 0) | (raging(s) ? 32 : 0);
      fx(p, 9, i, t == null ? 0 : t.getId(), point, ZERO, 0, i, flags);
      sound(level, p.position(), "minecraft:item.trident.throw", 0.5F, 1.7F);
      later(s, now + 7L, (pp, lv, ss, nn) -> {
         boolean last = i == 2;
         if (t != null && t.isAlive() && t.level() == lv && (double)t.distanceTo(pp) <= 11.0D) {
            hurt(pp, lv, t, 4.0D, pp.position(), last ? 0.5D : 0.35D, last ? 0.7D : 0.2D);
            crack(lv, aim(t), 0.7F, tint(pp));
         } else {
            crack(lv, point, 0.5F, tint(pp));
         }

      });
   }

   private static void ward(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (ready(p, s.readyWard, now, "Lash Ward") && pay(p, 18)) {
         int cd = cooldown(p, 280);
         s.readyWard = now + (long)cd;
         s.cdWard = cd;
         s.wardShown = true;
         strain(s, 20.0D);
         s.ward = 80;
         AttributeInstance speed = p.getAttribute(Attributes.MOVEMENT_SPEED);
         if (speed != null && speed.getModifier(WARD_UUID) == null) {
            speed.addTransientModifier(new AttributeModifier(WARD_UUID, "unrp lash ward", -0.5D, Operation.MULTIPLY_TOTAL));
         }

         fx(p, 10, 0, 0, ZERO, ZERO, 0, 90, 0);
         sound(level, p.position(), "minecraft:entity.phantom.flap", 0.9F, 1.3F);
         sound(level, p.position(), "minecraft:block.chain.place", 0.7F, 0.7F);
      }
   }

   private static void wardEnd(ServerPlayer p, BlackLashEvents.Lash s) {
      s.ward = 0;
      AttributeInstance speed = p.getAttribute(Attributes.MOVEMENT_SPEED);
      if (speed != null && speed.getModifier(WARD_UUID) != null) {
         speed.removeModifier(WARD_UUID);
      }

      end(p, 10, 0);
   }

   private static void tickWard(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (s.ward > 0) {
         --s.ward;
         if (s.ward == 0) {
            wardEnd(p, s);
         } else {
            deflectNear(p, level);
            if (s.ward % 5 == 0) {
               sound(level, p.position(), "minecraft:entity.phantom.flap", 0.4F, 1.6F);
            }

            if (LashMath.wardLash(s.ward)) {
               List<LivingEntity> near = foes(p, level, 3.4D);

               for(int i = 0; i < near.size(); ++i) {
                  LivingEntity e = (LivingEntity)near.get(i);
                  fx(p, 11, i % 32, e.getId(), aim(e), ZERO, 0, i, 0);
                  later(s, now + 2L, (pp, lv, ss, nn) -> {
                     if (e.isAlive() && e.level() == lv) {
                        hurt(pp, lv, e, 3.0D, pp.position(), 0.8D, 0.3D);
                        crack(lv, aim(e), 0.5F, tint(pp));
                     }

                  });
               }

            }
         }
      }
   }

   private static void deflectNear(ServerPlayer p, ServerLevel level) {
      Vec3 c = p.position().add(0.0D, 1.0D, 0.0D);
      double[] feet = a(p.position());

      for(Projectile pr : level.getEntitiesOfClass(Projectile.class, (new AABB(c, c)).inflate(5.0D))) {
         if (!pr.getTags().contains("unrp_deflected")) {
            Entity owner = pr.getOwner();
            if (owner == null || !owner.getUUID().equals(p.getUUID())) {
               Vec3 vel = pr.getDeltaMovement();
               if (!(vel.lengthSqr() < 1.0E-4D) && !(vel.dot(c.subtract(pr.position())) <= 0.0D) && (LashMath.inWard(a(pr.position()), feet) || LashMath.inWard(a(pr.position().add(vel)), feet))) {
                  ProjectileImpactEvents.deflect(level, pr, p);
                  crack(level, pr.position(), 0.5F);
               }
            }
         }
      }

   }

   private static void stormPress(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (s.storm <= 0 && !locked(p, s) && ready(p, s.readyStorm, now, "Lash Storm")) {
         if (s.strain < 50.0D) {
            bar(p, "§dLash Storm needs half your Strain: push the tendrils harder first.");
         } else if (pay(p, 48)) {
            int cd = cooldown(p, 1800);
            s.readyStorm = now + (long)cd;
            s.cdStorm = cd;
            s.stormCued = true;
            s.storm = 60;
            fx(p, 12, 0, 0, ZERO, ZERO, 0, 84, 0);
            sound(level, p.position(), "minecraft:entity.warden.sonic_charge", 0.7F, 1.8F);
         }
      }
   }

   private static boolean stormOpen(ServerPlayer p) {
      return !copyHolder(p) && score(p, "unrp.unlock_s2") >= 1 && score(p, "unrp.unlock_s3") >= 1 && score(p, "unrp.unlock_s4") >= 1;
   }

   private static void stormCue(ServerPlayer p, BlackLashEvents.Lash s, long now) {
      boolean free = s.frenzy == 0 && s.rest == 0 && s.storm == 0 && !downed(s) && p.getTags().contains("unrp.bl_on");
      if (LashMath.stormReady(s.strain, stormOpen(p), free, now >= s.readyStorm)) {
         if (!s.stormCued) {
            s.stormCued = true;
            sound(p.serverLevel(), p.position(), "minecraft:block.respawn_anchor.charge", 0.6F, 1.5F);
            bar(p, "§bLash Storm is ready.");
         }
      } else if (s.storm > 0 || LashMath.stormRearm(s.strain)) {
         s.stormCued = false;
      }

   }

   private static void groundStrike(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now, int i, int age) {
      double[] g = LashMath.stormGround(a(p.position()), i, age);
      Vec3 at = new Vec3(g[0], ground(level, g[0], p.getY() + 1.5D, g[2]) + 0.05D, g[2]);
      fx(p, 13, i, 0, at, ZERO, 0, i, 0);
      later(s, now + 7L, (pp, lv, ss, nn) -> {
         crack(lv, at, 0.45F, tint(pp));
         BlockState st = lv.getBlockState(BlockPos.containing(at.x, at.y - 0.2D, at.z));
         if (!st.isAir()) {
            lv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, st), at.x, at.y + 0.1D, at.z, 10, 0.3D, 0.05D, 0.3D, 0.12D);
         }

      });
   }

   private static void tickStorm(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (s.storm > 0) {
         --s.storm;
         int age = 60 - s.storm;
         List<LivingEntity> near = null;

         for(int i = 0; i < 8; ++i) {
            if (LashMath.stormStrike(age, s.storm, i)) {
               if (near == null) {
                  near = foes(p, level, 10.0D);
               }

               int pick = LashMath.stormPick(i, age, near.size());
               if (pick < 0) {
                  groundStrike(p, level, s, now, i, age);
               } else {
                  LivingEntity t = (LivingEntity)near.get(pick);
                  fx(p, 13, i, t.getId(), aim(t), ZERO, 0, i, 0);
                  later(s, now + 7L, (pp, lv, ss, nn) -> {
                     if (t.isAlive() && t.level() == lv && (double)t.distanceTo(pp) <= 14.0D) {
                        hurt(pp, lv, t, 3.0D, pp.position(), 0.3D, 0.15D);
                        crack(lv, aim(t), 0.6F, tint(pp));
                     }

                  });
               }
            }
         }

         if (s.storm == 6) {
            fx(p, 14, 0, 0, p.position(), ZERO, 0, 0, 32);
            later(s, now + 7L, (pp, lv, ss, nn) -> {
               for(LivingEntity e : foes(pp, lv, 6.5D)) {
                  hurt(pp, lv, e, 6.0D, pp.position(), 0.9D, 0.5D);
               }

               sound(lv, pp.position(), "minecraft:entity.generic.explode", 0.9F, 1.4F);
               sound(lv, pp.position(), "minecraft:entity.warden.attack_impact", 1.0F, 0.8F);
               Fx.ring(lv, 5891826, pp.getX(), pp.getY() + 0.1D, pp.getZ(), 6.0D, 12);
               Fx.burst(lv, Fx.drop(1840168), pp.position().add(0.0D, 0.3D, 0.0D), 30, 3.0D, 0.1D);
            });
         }

         if (s.storm == 0) {
            s.strain = 0.0D;
         }

      }
   }

   private static void tickStrain(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s) {
      if (s.frenzy > 0) {
         --s.frenzy;
         if (s.frenzy == 0) {
            s.rest = 200;
            s.strain = 0.0D;
            frenzyStop(p, s);
            bar(p, "§8The Frenzy passes; the tendrils rest.");
            sound(level, p.position(), "minecraft:block.respawn_anchor.deplete", 0.6F, 0.7F);
         }
      } else if (s.rest > 0) {
         --s.rest;
      } else {
         boolean still = s.still > 2 && s.swing == null;
         s.strain = LashMath.strainStep(s.strain, s.add, still, p.isShiftKeyDown());
         if (LashMath.frenzyAt(s.strain)) {
            startFrenzy(p, level, s);
         }
      }

      s.add = 0.0D;
   }

   private static void startFrenzy(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s) {
      s.frenzy = 120;
      s.strain = 100.0D;
      swingEnd(p, s, false);
      if (s.sling != null && s.sling.flying < 0L) {
         s.sling = null;
         end(p, 3, 0);
      }

      cancelPull(p, s);
      dropAll(p, level, s);
      if (s.ward > 0) {
         wardEnd(p, s);
      }

      if (s.storm > 0) {
         s.storm = 0;
         end(p, 12, 0);
      }

      s.struck.clear();
      s.frenzyFrom = p.position();
      s.frenzyAt = !p.isPassenger() && !p.isInWater() && !p.isInLava() ? p.position().add(0.0D, liftRoom(p, level), 0.0D) : null;
      burst(p, level);
      frenzyFx(p, s, 0);
      bar(p, "§cFRENZY: the tendrils take over, and they feed on you.");
      sound(level, p.position(), "minecraft:entity.warden.roar", 0.7F, 1.4F);
   }

   private static void frenzyFx(ServerPlayer p, BlackLashEvents.Lash s, int age) {
      Vec3 from = s.frenzyFrom != null ? s.frenzyFrom : p.position();
      fx(p, 15, 0, 0, from, s.frenzyAt != null ? s.frenzyAt : from, age, 128, 16);
   }

   private static double liftRoom(ServerPlayer p, ServerLevel level) {
      for(int k = 0; k < 14; ++k) {
         double dy = 1.4D - (double)k * 0.1D;
         if (level.noCollision(p, p.getBoundingBox().move(0.0D, dy, 0.0D))) {
            return dy;
         }
      }

      return 0.0D;
   }

   private static void burst(ServerPlayer p, ServerLevel level) {
      for(LivingEntity e : anyone(p, level, 4.0D)) {
         if (!bound(e)) {
            double resist = e.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            e.setDeltaMovement(v(LashMath.knock(a(e.getDeltaMovement()), a(e.position()), a(p.position()), 1.0D, 0.5D, resist)));
            e.hurtMarked = true;
         }
      }

      sound(level, p.position(), "minecraft:entity.generic.explode", 0.7F, 1.5F);
      Fx.ring(level, 16731499, p.getX(), p.getY() + 0.1D, p.getZ(), 4.0D, 14);
      Fx.burst(level, Fx.drop(1840168), p.position().add(0.0D, 0.4D, 0.0D), 24, 1.6D, 0.1D);
   }

   private static void suspend(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s) {
      if (s.frenzyAt != null) {
         if (!p.isPassenger() && !p.isInWater() && !p.isInLava()) {
            if (p.position().distanceTo(s.frenzyAt) > 6.0D) {
               s.frenzyFrom = p.position();
               s.frenzyAt = p.position().add(0.0D, liftRoom(p, level), 0.0D);
               frenzyFx(p, s, 120 - s.frenzy);
            }

            steer(p, s.frenzyAt, 0.3D);
            hover(p);
            s.soft = true;
         } else {
            unhover(p);
            s.frenzyAt = null;
            frenzyFx(p, s, 120 - s.frenzy);
         }
      }
   }

   private static void feed(ServerPlayer p, ServerLevel level) {
      float amount = (float)LashMath.frenzyFeedAmount((double)p.getHealth(), (double)p.getAbsorptionAmount());
      if (amount > 0.0F) {
         int was = p.invulnerableTime;
         FED.set(p);

         try {
            OriginStrike.hitFresh(p, TrueDamage.source(level, (Entity)null), amount);
         } finally {
            FED.remove();
         }

         p.invulnerableTime = was;
      }

      sound(level, p.position(), "minecraft:entity.warden.heartbeat", 1.0F, 1.25F);
      Fx.burst(level, Fx.glint(16731499), mid(p), 10, 0.35D, 0.08D);
   }

   private static void frenzyCut(ServerPlayer p, BlackLashEvents.Lash s) {
      end(p, 15, 0);
      s.frenzy = 0;
      s.rest = 200;
      s.strain = 0.0D;
      frenzyStop(p, s);
   }

   private static void frenzyStop(ServerPlayer p, BlackLashEvents.Lash s) {
      if (s.frenzyAt != null) {
         unhover(p);
         s.soft = true;
      }

      s.frenzyAt = null;
      s.frenzyFrom = null;
      s.struck.clear();
   }

   private static void tickFrenzy(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (s.frenzy > 0) {
         if (p.isSpectator()) {
            frenzyCut(p, s);
         } else {
            int age = 120 - s.frenzy;
            suspend(p, level, s);
            if (LashMath.frenzyFeed(age)) {
               feed(p, level);
               if (s.frenzy <= 0) {
                  return;
               }
            }

            if (LashMath.frenzyBeat(age)) {
               List<LivingEntity> near = anyone(p, level, 9.0D);
               List<UUID> picked = new ArrayList();

               for(int j = 0; j < 2; ++j) {
                  int i = LashMath.frenzyTendril(age, j);
                  LivingEntity t = null;

                  for(LivingEntity e : near) {
                     Long last = (Long)s.struck.get(e.getUUID());
                     if (!picked.contains(e.getUUID()) && (last == null || LashMath.frenzyFree(last, now))) {
                        t = e;
                        break;
                     }
                  }

                  if (t == null) {
                     frenzySlam(p, level, s, now, i, age);
                  } else {
                     picked.add(t.getUUID());
                     s.struck.put(t.getUUID(), now);
                     LivingEntity hit = t;
                     fx(p, 25, i, t.getId(), aim(t), ZERO, 0, i, 32);
                     later(s, now + 7L, (pp, lv, ss, nn) -> {
                        if (hit.isAlive() && hit.level() == lv && (double)hit.distanceTo(pp) <= 13.0D) {
                           hurtAnyone(pp, lv, hit, LashMath.frenzyHit(ally(pp, hit)), pp.position(), 0.55D, 0.35D);
                           crack(lv, aim(hit), 0.75F, 16731499);
                        }

                     });
                  }
               }

               sound(level, p.position(), "minecraft:entity.phantom.flap", 0.6F, 0.6F);
            }
         }
      }
   }

   private static void frenzySlam(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now, int i, int age) {
      Vec3 c = s.frenzyAt != null && s.frenzyFrom != null ? s.frenzyFrom : p.position();
      double[] g = LashMath.frenzyGround(a(c), i, age);
      Vec3 at = new Vec3(g[0], ground(level, g[0], c.y + 1.5D, g[2]) + 0.05D, g[2]);
      fx(p, 25, i, 0, at, ZERO, 0, i, 0);
      later(s, now + 7L, (pp, lv, ss, nn) -> {
         crack(lv, at, 0.45F, 16731499);
         BlockState st = lv.getBlockState(BlockPos.containing(at.x, at.y - 0.2D, at.z));
         if (!st.isAir()) {
            lv.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, st), at.x, at.y + 0.1D, at.z, 12, 0.35D, 0.05D, 0.35D, 0.14D);
         }

      });
   }

   private static void dropAll(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s) {
      for(BlackLashEvents.Held h : s.held) {
         LivingEntity e = living(level, h.id, h.uuid);
         if (e != null) {
            letGo(p, e, h, true);
         } else {
            end(p, 6, h.slot);
         }
      }

      s.held.clear();

      for(int slot : s.pending.values()) {
         end(p, 6, slot);
      }

      s.pending.clear();
   }

   private static void tickSoft(ServerPlayer p, BlackLashEvents.Lash s) {
      if (s.soft) {
         if (s.zip == null && s.swing == null && (s.sling == null || s.sling.flying < 0L)) {
            if (p.onGround()) {
               ++s.softGround;
               if (s.softGround > 3) {
                  s.soft = false;
                  s.softGround = 0;
               }
            } else {
               s.softGround = 0;
            }

         } else {
            p.fallDistance = 0.0F;
            s.softGround = 0;
         }
      }
   }

   private static void set(ServerPlayer p, String tag, boolean want) {
      if (want) {
         p.addTag(tag);
      } else {
         p.removeTag(tag);
      }

   }

   private static void syncTags(ServerPlayer p, BlackLashEvents.Lash s, long now) {
      boolean lockedNow = s.frenzy > 0 || s.rest > 0;
      set(p, "unrp.bl_locked", lockedNow);
      set(p, "unrp.bl_storm_ready", !lockedNow && s.storm == 0 && s.strain >= 50.0D);
      boolean hanging = s.swing != null && s.swing.pose == 1;
      set(p, "unrp.bl_swinging", s.swing != null && !hanging);
      set(p, "unrp.bl_hanging", hanging);
      set(p, "unrp.bl_paused", s.sling != null || s.ward > 0);
      set(p, "unrp.bl_zip", s.zip != null);
      set(p, "unrp.bl_swing", s.swing != null && s.swing.pose == 0);
      set(p, "unrp.bl_sling", s.sling != null && s.sling.flying < 0L);
      set(p, "unrp.bl_launch", s.sling != null && s.sling.flying >= 0L);
      set(p, "unrp.bl_hold", !s.held.isEmpty() && now > s.bindAt + 7L);
      set(p, "unrp.bl_ward", s.ward > 0);
      set(p, "unrp.bl_storm", s.storm > 0);
      set(p, "unrp.bl_frenzy", s.frenzy > 0);
      set(p, "unrp.bl_down", s.wind == 1);
      set(p, "unrp.bl_rage", raging(s));
   }

   private static int frac(long ready, int len, long now) {
      return ready <= now ? 0 : (int)Math.min(255.0D, Math.ceil((double)(255L * (ready - now)) / (double)Math.max(1, len)));
   }

   private static void hud(ServerPlayer p, BlackLashEvents.Lash s, long now) {
      boolean used = windOwned(p) && !LashMath.windReady(System.currentTimeMillis(), windUsed(p));
      int[] cur = new int[]{(int)Math.round(s.strain), s.frenzy, s.rest, frac(s.readySnatch, s.cdSnatch, now), frac(s.readySling, s.cdSling, now), frac(s.readyBind, s.cdBind, now), frac(s.readyWhip, s.cdWhip, now), frac(s.readyWard, s.cdWard, now), 0, s.wind, windShown(s, now), frac(s.readyStorm, s.cdStorm, now), s.held.size(), holdShown(s, now)};
      int flags = (s.ward > 0 ? 1 : 0) | (s.storm > 0 ? 2 : 0) | (!s.held.isEmpty() ? 4 : 0) | (s.swing != null ? 8 : 0) | (s.sling != null && s.sling.flying < 0L ? 16 : 0) | (twin(p) ? 32 : 0) | (used ? 64 : 0) | (stormOpen(p) ? 128 : 0);
      cur[8] = flags;
      boolean same = now - s.sentAt < 20L;

      for(int i = 0; i < cur.length && same; ++i) {
         if (cur[i] != s.sent[i]) {
            same = false;
         }
      }

      if (!same) {
         System.arraycopy(cur, 0, s.sent, 0, cur.length);
         s.sentAt = now;
         int[] cds = new int[]{cur[3], cur[4], cur[5], cur[6], cur[7], cur[11]};
         com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new LashHudPacket(cur[0], cur[1], cur[2], cds, flags, cur[9], cur[10], cur[12], cur[13]));
      }
   }

   private static int holdShown(BlackLashEvents.Lash s, long now) {
      int left = -1;

      for(BlackLashEvents.Held h : s.held) {
         int l = LashMath.holdLeft(now - h.at);
         left = left < 0 ? l : Math.min(left, l);
      }

      return Math.max(0, left);
   }

   private static int windShown(BlackLashEvents.Lash s, long now) {
      if (s.wind != 3 && s.wind != 1) {
         return s.wind == 4 ? (int)Math.max(0L, 1200L - (now - s.windAt)) : 0;
      } else {
         return s.windLeft;
      }
   }

   private static void refresh(ServerPlayer p, BlackLashEvents.Lash s, long now) {
      if (s.wind == 1) {
         fx(p, 19, 0, 0, ZERO, ZERO, 200 - s.windLeft, 204, 0);
      }

      if (raging(s)) {
         fx(p, 22, 0, 0, ZERO, ZERO, 4800 - s.windLeft, 4810, 0);
      }

      if (s.caught != null && s.caught.done < 0L) {
         fx(p, 18, 0, s.caught.target, ZERO, ZERO, (int)(now - s.caught.at), 36, 0);
      }

      if (s.swing != null) {
         fx(p, 2, 0, 0, s.swing.anchor, ZERO, (int)(now - s.swing.since) + 2, 3, 0);
      }

      if (s.sling != null && s.sling.flying < 0L) {
         fx(p, 3, 0, 0, s.sling.left, s.sling.right, (int)(now - s.sling.at), 0, 0);
      }

      for(BlackLashEvents.Held h : s.held) {
         fx(p, 6, h.slot, h.id, ZERO, ZERO, (int)(now - h.at) + 7, h.slot + 2, h.slot == 1 ? 4 : 0);
      }

      if (s.ward > 0) {
         fx(p, 10, 0, 0, ZERO, ZERO, 80 - s.ward, 90, 0);
      }

      if (s.storm > 0) {
         fx(p, 12, 0, 0, ZERO, ZERO, 60 - s.storm, 84, 0);
      }

      if (s.frenzy > 0) {
         frenzyFx(p, s, 120 - s.frenzy);
      }

      boolean wisps = s.strain >= 50.0D && s.frenzy == 0 && s.rest == 0 && s.storm == 0 && p.getTags().contains("unrp.bl_on") && !raging(s) && !downed(s);
      int level = (int)Math.round(s.strain);
      if (!wisps || s.strainFx && Math.abs(level - s.strainSent) < 2 && p.tickCount % 40 != 0) {
         if (!wisps && s.strainFx) {
            end(p, 16, 0);
            s.strainFx = false;
            s.strainSent = -1;
         }
      } else {
         fx(p, 16, 0, 0, ZERO, ZERO, 0, level, 0);
         s.strainFx = true;
         s.strainSent = level;
      }

   }

   private static void powerOff(ServerPlayer p, BlackLashEvents.Lash s) {
      powerOff(p, s, p.serverLevel());
   }

   private static void powerOff(ServerPlayer p, BlackLashEvents.Lash s, ServerLevel level) {
      ++s.epoch;
      s.timed.clear();
      cancelPull(p, s);
      swingEnd(p, s, false);
      if (s.sling != null) {
         if (s.sling.flying < 0L) {
            end(p, 3, 0);
         }

         s.sling = null;
      }

      dropAll(p, level, s);

      for(BlackLashEvents.Move m : s.moves) {
         LivingEntity e = living(level, m.held.id, m.held.uuid);
         if (e != null) {
            letGo(p, e, m.held, true);
         } else {
            end(p, 6, m.held.slot);
         }
      }

      s.moves.clear();
      s.thrown.clear();
      if (s.ward > 0) {
         wardEnd(p, s);
      }

      if (s.storm > 0) {
         s.storm = 0;
         end(p, 12, 0);
      }

      if (s.strainFx) {
         end(p, 16, 0);
         s.strainFx = false;
      }

      MoveAnim.stop(p, "bl_sling");
      MoveAnim.stop(p, "bl_hold");
      MoveAnim.stop(p, "bl_swing");
      MoveAnim.stop(p, "bl_hang");
      MoveAnim.stop(p, "bl_pump");
      MoveAnim.stop(p, "bl_tuck");
      MoveAnim.stop(p, "bl_veer");
      MoveAnim.stop(p, "bl_veer_l");
      MoveAnim.stop(p, "bl_flip");
      s.flipAt = -1L;
   }

   private static void shutDown(ServerPlayer p) {
      BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.remove(p.getUUID());
      if (s != null) {
         powerOff(p, s);
         if (s.frenzy > 0) {
            end(p, 15, 0);
         }

         catchEnd(p, s);
         windClear(p, s);
      }

      clearTransient(p);
   }

   @SubscribeEvent
   public static void onFall(LivingFallEvent event) {
      if (!event.getEntity().level().isClientSide()) {
         Long until = (Long)CAUGHT.remove(event.getEntity().getUUID());
         if (until != null && event.getEntity().level().getGameTime() <= until) {
            event.setDamageMultiplier(0.0F);
            event.setDistance(0.0F);
         }

         LivingEntity s_0 = event.getEntity();
         if (s_0 instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)s_0;
            BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.get(p.getUUID());
            if (s != null && s.soft) {
               event.setDamageMultiplier(0.0F);
               event.setDistance(0.0F);
               if (s.zip == null && s.swing == null && (s.sling == null || s.sling.flying < 0L)) {
                  s.soft = false;
               }
            }

         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onFed(LivingDamageEvent event) {
      if (event.getEntity() == FED.get()) {
         event.setAmount(Math.min(event.getAmount(), Math.max(0.0F, event.getEntity().getHealth() - 1.0F)));
      }

   }

   @SubscribeEvent
   public static void onHurt(LivingHurtEvent event) {
      Entity s_0 = event.getSource().getEntity();
      if (s_0 instanceof ServerPlayer by) {
         if (by != event.getEntity() && !PLAIN.get() && raging((BlackLashEvents.Lash)LASHES.get(by.getUUID()))) {
            event.setAmount(event.getAmount() * (float)LashMath.dealt(true));
         }
      }

      LivingEntity var4 = event.getEntity();
      if (var4 instanceof ServerPlayer p) {
         BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.get(p.getUUID());
         if (s != null && (s.ward > 0 || raging(s))) {
            event.setAmount(event.getAmount() * (float)(1.0D - LashMath.takenCut(s.ward > 0, raging(s))));
         }

      }
   }

   @SubscribeEvent
   public static void onKnock(LivingKnockBackEvent event) {
      if (LASHING.get()) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void onDeath(LivingDeathEvent event) {
      LivingEntity s_0 = event.getEntity();
      if (s_0 instanceof ServerPlayer p) {
         if (!event.isCanceled()) {
            BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.get(p.getUUID());
            if (s == null) {
               return;
            }

            powerOff(p, s);
            if (s.frenzy > 0) {
               end(p, 15, 0);
            }

            s.strain = 0.0D;
            s.frenzy = 0;
            s.rest = 0;
            s.soft = false;
            s.frenzyAt = null;
            s.frenzyFrom = null;
            s.struck.clear();
            catchEnd(p, s);
            windClear(p, s);
            return;
         }
      }

   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player s_0 = event.getEntity();
      if (s_0 instanceof ServerPlayer p) {
         BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.remove(p.getUUID());
         if (s != null) {
            powerOff(p, s);
         }

         clearTransient(p);
         if (s != null) {
            catchEnd(p, s);
            saveWind(p, s);
         }

      }
   }

   @SubscribeEvent
   public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
      Player s_0 = event.getEntity();
      if (s_0 instanceof ServerPlayer p) {
         BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.get(p.getUUID());
         if (s != null) {
            ServerLevel from = p.server.getLevel(event.getFrom());
            powerOff(p, s, from != null ? from : p.serverLevel());
            s.hasLast = false;
            if (s.frenzy > 0) {
               frenzyCut(p, s);
            }
         }

      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
      Player s_0 = event.getEntity();
      if (s_0 instanceof ServerPlayer p) {
         BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.get(p.getUUID());
         if (s != null) {
            s.hasLast = false;
            s.fresh = true;
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onLethal(LivingDeathEvent event) {
      LivingEntity s_0 = event.getEntity();
      if (s_0 instanceof ServerPlayer p) {
         if (!event.isCanceled()) {
            BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.get(p.getUUID());
            if (s == null) {
               return;
            }

            DamageSource src = event.getSource();
            boolean bypass = src.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
            if (downed(s)) {
               if (!s.givingUp && !bypass) {
                  event.setCanceled(true);
                  p.setHealth(1.0F);
                  return;
               }

               return;
            }

            if (!bypass && windCan(p, s)) {
               event.setCanceled(true);
               windDown(p, s, src);
               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onAttacked(LivingAttackEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer p) {
         if (downed((BlackLashEvents.Lash)LASHES.get(p.getUUID())) && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
         }

      }
   }

   @SubscribeEvent
   public static void onSwingDown(AttackEntityEvent event) {
      if (isDowned(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onUseDown(PlayerInteractEvent.RightClickItem event) {
      if (isDowned(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onBlockDown(PlayerInteractEvent.RightClickBlock event) {
      if (isDowned(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onBreakDown(PlayerInteractEvent.LeftClickBlock event) {
      if (isDowned(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onTouchDown(PlayerInteractEvent.EntityInteract event) {
      if (isDowned(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onClone(PlayerEvent.Clone event) {
      CompoundTag from = event.getOriginal().getPersistentData();
      CompoundTag to = event.getEntity().getPersistentData();
      if (from.contains("unrp_bl_wind_used")) {
         to.putLong("unrp_bl_wind_used", from.getLong("unrp_bl_wind_used"));
      }

      if (!event.isWasDeath() && from.contains("unrp_bl_wind_phase")) {
         to.putInt("unrp_bl_wind_phase", from.getInt("unrp_bl_wind_phase"));
         to.putInt("unrp_bl_wind_left", from.getInt("unrp_bl_wind_left"));
      }

   }

   public static void giveUp(ServerPlayer p) {
      BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.get(p.getUUID());
      if (s != null && s.wind == 1 && p.isAlive()) {
         s.givingUp = true;
         DamageSource src = s.windSource != null ? s.windSource : p.damageSources().generic();
         p.setHealth(0.0F);
         p.die(src);
      }
   }

   static long windUsed(ServerPlayer p) {
      return p.getPersistentData().getLong("unrp_bl_wind_used");
   }

   static boolean windOwned(ServerPlayer p) {
      return p.getTags().contains("unrp.has.black_lash") && score(p, "unrp.unlock_s4") >= 1 && !copyHolder(p);
   }

   private static boolean windCan(ServerPlayer p, BlackLashEvents.Lash s) {
      return windOwned(p) && p.getTags().contains("unrp.bl_on") && noncanonOn(p) && !p.isSpectator() && (s.wind == 0 || s.wind == 4) && LashMath.windReady(System.currentTimeMillis(), windUsed(p));
   }

   private static void windScreen(ServerPlayer p, BlackLashEvents.Lash s) {
      int phase = s.wind;
      int left = phase == 1 ? s.windLeft : 0;
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new LashWindPacket(phase, left, 200, p.getScore()));
   }

   private static void windDown(ServerPlayer p, BlackLashEvents.Lash s, DamageSource src) {
      ServerLevel level = p.serverLevel();
      long now = level.getGameTime();
      p.getPersistentData().putLong("unrp_bl_wind_used", System.currentTimeMillis());
      powerOff(p, s);
      if (s.frenzy > 0) {
         end(p, 15, 0);
         s.frenzy = 0;
         s.strain = 0.0D;
         frenzyStop(p, s);
      }

      catchEnd(p, s);
      if (p.isPassenger()) {
         p.stopRiding();
      }

      p.setHealth(1.0F);
      p.setDeltaMovement(0.0D, Math.min(0.0D, p.getDeltaMovement().y), 0.0D);
      p.hurtMarked = true;
      p.fallDistance = 0.0F;
      s.wind = 1;
      s.windLeft = 200;
      s.windAt = now;
      s.downAt = p.position();
      s.windSource = src;
      s.givingUp = false;
      p.addTag("unrp.bl_down");
      MoveAnim.play(p, "bl_down");
      fx(p, 20, 0, 0, p.position(), ZERO, 0, 0, 0);
      fx(p, 19, 0, 0, ZERO, ZERO, 0, 204, 0);
      windScreen(p, s);
      sound(level, p.position(), "minecraft:entity.warden.heartbeat", 1.0F, 0.8F);
   }

   private static void tickWind(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (s.wind != 0) {
         if (s.wind == 1) {
            --s.windLeft;
            Vec3 mv = p.getDeltaMovement();
            if (Math.abs(mv.x) + Math.abs(mv.z) > 0.001D) {
               p.setDeltaMovement(0.0D, mv.y, 0.0D);
               p.hurtMarked = true;
            }

            if (s.downAt != null && Math.hypot(p.getX() - s.downAt.x, p.getZ() - s.downAt.z) > 0.6D) {
               p.teleportTo(s.downAt.x, p.getY(), s.downAt.z);
            }

            p.fallDistance = 0.0F;
            if (s.windLeft % 10 == 0) {
               windScreen(p, s);
            }

            if (s.windLeft % 20 == 0) {
               sound(level, p.position(), "minecraft:entity.warden.heartbeat", 0.8F, 0.8F);
            }

            if (s.windLeft <= 0) {
               windRise(p, level, s, now);
            }

         } else if (s.wind == 2) {
            long age = now - s.windAt;
            if (age == 5L) {
               windBurst(p, level);
            }

            if (age >= 12L) {
               windActive(p, s, now);
            }

         } else if (s.wind == 3) {
            --s.windLeft;
            if (LashMath.windBeat(now)) {
               windLash(p, level, s, now);
            }

            if (s.windLeft <= 0) {
               windSpent(p, level, s, now);
            }

         } else {
            if (s.wind == 4 && now - s.windAt >= 1200L) {
               s.wind = 0;
            }

         }
      }
   }

   private static void windRise(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      s.wind = 2;
      s.windAt = now;
      s.windSource = null;
      s.downAt = null;
      p.removeTag("unrp.bl_down");
      p.setHealth((float)Math.min((double)p.getMaxHealth(), 12.0D));
      p.fallDistance = 0.0F;
      end(p, 19, 0);
      MoveAnim.play(p, "bl_rise");
      fx(p, 21, 0, 0, p.position(), ZERO, 0, 0, 0);
      windScreen(p, s);
      sound(level, p.position(), "minecraft:entity.warden.roar", 0.6F, 1.5F);
   }

   private static void windBurst(ServerPlayer p, ServerLevel level) {
      for(LivingEntity e : anyone(p, level, 6.0D)) {
         hurtAnyone(p, level, e, LashMath.windHit(5.0D, ally(p, e)), p.position(), 1.1D, 0.5D);
      }

      sound(level, p.position(), "minecraft:entity.generic.explode", 0.8F, 1.3F);
      sound(level, p.position(), "minecraft:entity.warden.attack_impact", 1.0F, 0.7F);
      Fx.ring(level, 16731499, p.getX(), p.getY() + 0.1D, p.getZ(), 5.5D, 14);
      Fx.burst(level, Fx.drop(1840168), p.position().add(0.0D, 0.4D, 0.0D), 24, 2.0D, 0.1D);
   }

   private static void windActive(ServerPlayer p, BlackLashEvents.Lash s, long now) {
      s.wind = 3;
      s.windLeft = 4800;
      s.windAt = now;
      p.addTag("unrp.bl_rage");
      fx(p, 22, 0, 0, ZERO, ZERO, 0, 4810, 0);
   }

   private static void windLash(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      List<LivingEntity> near = anyone(p, level, 5.0D);
      if (!near.isEmpty()) {
         LivingEntity t = (LivingEntity)near.get(LashMath.windPick(now, near.size()));
         int slot = LashMath.windSlot(now);
         fx(p, 23, slot, t.getId(), aim(t), ZERO, 0, slot, 0);
         sound(level, p.position(), "minecraft:item.trident.throw", 0.4F, 1.8F);
         later(s, now + 7L, (pp, lv, ss, nn) -> {
            if (t.isAlive() && t.level() == lv && (double)t.distanceTo(pp) <= 8.0D) {
               plain(pp, lv, t, LashMath.windHit(3.0D, ally(pp, t)), pp.position(), 0.45D, 0.2D);
               crack(lv, aim(t), 0.6F, 16731499);
            }

         });
      }
   }

   private static void windSpent(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      s.wind = 4;
      s.windAt = now;
      s.windLeft = 0;
      p.removeTag("unrp.bl_rage");
      end(p, 22, 0);
      fx(p, 24, 0, 0, p.position(), ZERO, 0, 0, 0);
      p.setHealth((float)Math.min((double)p.getMaxHealth(), 2.0D));
      p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 1));
      p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 900, 1));
      p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0));
      bar(p, "§7It gives out: one heart, weak, slow and blind.");
      sound(level, p.position(), "minecraft:block.respawn_anchor.deplete", 0.8F, 0.6F);
   }

   private static void windClear(ServerPlayer p, BlackLashEvents.Lash s) {
      if (s.wind == 1) {
         end(p, 19, 0);
      }

      if (s.wind == 3) {
         end(p, 22, 0);
      }

      boolean screen = downed(s);
      s.wind = 0;
      s.windLeft = 0;
      s.windSource = null;
      s.givingUp = false;
      s.downAt = null;
      p.removeTag("unrp.bl_down");
      p.removeTag("unrp.bl_rage");
      if (screen) {
         windScreen(p, s);
      }

   }

   private static void saveWind(ServerPlayer p, BlackLashEvents.Lash s) {
      CompoundTag pd = p.getPersistentData();
      if (downed(s)) {
         pd.putInt("unrp_bl_wind_phase", 1);
         pd.putInt("unrp_bl_wind_left", s.wind == 1 ? Math.max(1, s.windLeft) : 1);
      } else if (raging(s)) {
         pd.putInt("unrp_bl_wind_phase", 3);
         pd.putInt("unrp_bl_wind_left", Math.max(1, s.windLeft));
      } else {
         pd.remove("unrp_bl_wind_phase");
         pd.remove("unrp_bl_wind_left");
      }

   }

   private static void windResume(ServerPlayer p, BlackLashEvents.Lash s, long now) {
      CompoundTag pd = p.getPersistentData();
      if (pd.contains("unrp_bl_wind_phase")) {
         int phase = pd.getInt("unrp_bl_wind_phase");
         int left = Math.max(1, pd.getInt("unrp_bl_wind_left"));
         pd.remove("unrp_bl_wind_phase");
         pd.remove("unrp_bl_wind_left");
         if (p.isAlive() && p.getTags().contains("unrp.has.black_lash")) {
            if (phase == 1) {
               s.wind = 1;
               s.windLeft = Math.min(left, 200);
               s.windAt = now;
               s.downAt = p.position();
               s.windSource = null;
               p.addTag("unrp.bl_down");
               MoveAnim.play(p, "bl_down");
               fx(p, 19, 0, 0, ZERO, ZERO, 200 - s.windLeft, 204, 0);
               windScreen(p, s);
            } else if (phase == 3) {
               s.wind = 3;
               s.windLeft = Math.min(left, 4800);
               s.windAt = now;
               p.addTag("unrp.bl_rage");
               fx(p, 22, 0, 0, ZERO, ZERO, 4800 - s.windLeft, 4810, 0);
            }

         }
      }
   }

   private static void tickCatch(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, long now) {
      if (s.caught != null) {
         tickCaught(p, level, s, s.caught, now);
      } else {
         boolean can = p.getTags().contains("unrp.bl_on") && noncanonOn(p) && score(p, "unrp.unlock_s2") >= 1 && !copyHolder(p) && s.frenzy <= 0 && s.rest <= 0 && !downed(s) && p.isAlive() && !p.isSpectator();
         if (!can) {
            s.fallY.clear();
         } else {
            Map<UUID, Double> seen = new HashMap();
            LivingEntity pick = null;

            for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(16.0D))) {
               if (e != p && e.isAlive() && !e.isSpectator() && ally(p, e)) {
                  Double last = (Double)s.fallY.get(e.getUUID());
                  seen.put(e.getUUID(), e.getY());
                  if (pick == null && now >= s.catchReady && last != null && LashMath.catchWants(e.getY() - last, (double)e.distanceTo(p), e.onGround()) && catchable(p, level, e)) {
                     pick = e;
                  }
               }
            }

            s.fallY.clear();
            s.fallY.putAll(seen);
            if (pick != null) {
               catchStart(p, level, s, pick, now);
            }

         }
      }
   }

   private static boolean catchable(ServerPlayer p, ServerLevel level, LivingEntity e) {
      if (!bound(e) && !isDowned(e) && !e.isPassenger() && !e.isInWater() && !e.isInLava() && !TimeWeft.everpresent(e)) {
         if (!e.hasEffect(MobEffects.LEVITATION) && !e.hasEffect(MobEffects.SLOW_FALLING)) {
            if (e instanceof Player) {
               Player pl = (Player)e;
               if (pl.isFallFlying() || pl.getAbilities().flying) {
                  return false;
               }
            }

            BlackLashEvents.Lash other = (BlackLashEvents.Lash)LASHES.get(e.getUUID());
            if (other == null || !other.soft && other.zip == null && other.swing == null && other.sling == null) {
               Long until = (Long)CAUGHT.get(e.getUUID());
               if (until != null && until >= level.getGameTime()) {
                  return false;
               } else {
                  return p.hasLineOfSight(e) && !Double.isNaN(groundBelow(level, e));
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static double groundBelow(ServerLevel level, LivingEntity e) {
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      int x = (int)Math.floor(e.getX());
      int z = (int)Math.floor(e.getZ());
      int top = (int)Math.floor(e.getY());
      int bottom = Math.max(level.getMinBuildHeight(), top - 80);

      for(int y = top; y >= bottom; --y) {
         pos.set(x, y, z);
         if (!level.getFluidState(pos).isEmpty()) {
            return (double)((float)y + level.getFluidState(pos).getHeight(level, pos));
         }

         VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
         if (!shape.isEmpty()) {
            double h = (double)y + shape.max(Axis.Y);
            if (h <= e.getY() + 0.01D) {
               return h;
            }
         }
      }

      return Double.NaN;
   }

   private static void catchStart(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, LivingEntity e, long now) {
      s.catchReady = now + 600L;
      BlackLashEvents.Catch c = new BlackLashEvents.Catch();
      c.target = e.getId();
      c.uuid = e.getUUID();
      c.at = now;
      s.caught = c;
      CAUGHT.put(e.getUUID(), now + 4L + 24L + 20L);
      MoveAnim.play(p, "bl_catch");
      fx(p, 18, 0, e.getId(), ZERO, ZERO, 0, 36, 0);
      sound(level, p.position(), "minecraft:item.trident.throw", 0.6F, 1.3F);
   }

   private static void tickCaught(ServerPlayer p, ServerLevel level, BlackLashEvents.Lash s, BlackLashEvents.Catch c, long now) {
      LivingEntity e = living(level, c.target, c.uuid);
      if (e != null && e.isAlive() && !((double)e.distanceTo(p) > 24.0D) && !downed(s) && p.isAlive()) {
         if (c.grip < 0L) {
            if (now - c.at < 4L) {
               return;
            }

            double g = groundBelow(level, e);
            if (Double.isNaN(g)) {
               catchEnd(p, s);
               return;
            }

            if (e.isPassenger()) {
               e.stopRiding();
            }

            c.grip = now;
            c.from = e.position();
            c.ground = g;
            sound(level, e.position(), "minecraft:block.chain.hit", 0.8F, 1.3F);
         }

         CAUGHT.put(e.getUUID(), now + 20L);
         if (c.done >= 0L) {
            if (now - c.done > 6L) {
               s.caught = null;
            }

         } else {
            double k = LashMath.catchK((int)(now - c.grip));
            steer(e, new Vec3(c.from.x, LashMath.catchY(c.from.y, c.ground, k), c.from.z), 1.6D);
            if (k >= 1.0D) {
               c.done = now;
               e.setDeltaMovement(0.0D, 0.0D, 0.0D);
               e.hurtMarked = true;
               e.fallDistance = 0.0F;
               end(p, 18, 0);
            }

         }
      } else {
         catchEnd(p, s);
      }
   }

   private static void catchEnd(ServerPlayer p, BlackLashEvents.Lash s) {
      if (s.caught != null && s.caught.done < 0L) {
         end(p, 18, 0);
      }

      s.caught = null;
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

   public static double strainOf(ServerPlayer p) {
      BlackLashEvents.Lash s = (BlackLashEvents.Lash)LASHES.get(p.getUUID());
      return s == null ? 0.0D : s.strain;
   }

   interface Act {
      void run(ServerPlayer var1, ServerLevel var2, BlackLashEvents.Lash var3, long var4);
   }

   static final class Catch {
      int target;
      UUID uuid;
      long at;
      long grip = -1L;
      Vec3 from;
      double ground;
      long done = -1L;
   }

   static final class Grab {
      long at;
      int target;
      UUID uuid;
      boolean light;
      Vec3 point;
      BlockPos block;
      Direction face;
      boolean released;
      int seed;
      int grip;
      double fromY;
   }

   static final class Held {
      int id;
      UUID uuid;
      int slot;
      long at;
      Vec3 from;
      float hp;
      boolean broke;
   }

   static final class Hit {
      LivingEntity entity;
      BlockHitResult block;
      Vec3 at;
   }

   static final class Lash {
      boolean fresh = true;
      int epoch;
      double strain;
      double add;
      int frenzy;
      int rest;
      long readySnatch;
      long readySling;
      long readyBind;
      long readyWhip;
      long readyWard;
      long readyStorm;
      int cdSnatch = 1;
      int cdSling = 1;
      int cdBind = 1;
      int cdWhip = 1;
      int cdWard = 1;
      int cdStorm = 1;
      boolean slingShown;
      boolean wardShown;
      boolean stormCued;
      BlackLashEvents.Grab grab;
      BlackLashEvents.Reel reel;
      BlackLashEvents.Zip zip;
      BlackLashEvents.Swing swing;
      BlackLashEvents.Sling sling;
      final List<BlackLashEvents.Held> held = new ArrayList();
      final List<BlackLashEvents.Move> moves = new ArrayList();
      final List<BlackLashEvents.Thrown> thrown = new ArrayList();
      final List<BlackLashEvents.Timed> timed = new ArrayList();
      long bindAt = -1000L;
      int ward;
      int storm;
      boolean soft;
      int softGround;
      boolean hasLast;
      double lx;
      double ly;
      double lz;
      double mx;
      double my;
      double mz;
      int still;
      final int[] sent = new int[14];
      long sentAt = -1000L;
      boolean strainFx;
      int strainSent = -1;
      boolean copy;
      int wind;
      int windLeft;
      long windAt;
      Vec3 downAt;
      DamageSource windSource;
      boolean givingUp;
      boolean windChecked;
      long catchReady;
      BlackLashEvents.Catch caught;
      final Map<UUID, Double> fallY = new HashMap();
      final Map<UUID, Integer> pending = new HashMap();
      final Map<UUID, Long> struck = new HashMap();
      Vec3 frenzyFrom;
      Vec3 frenzyAt;
      double inForward;
      double inRight;
      boolean inReel;
      boolean inSlack;
      long inAt = -1000L;
      long flipAt = -1L;
      int swingHints;
   }

   static final class Move {
      BlackLashEvents.Held held;
      long at;
      Vec3 from;
      Vec3 back;
      Vec3 to;
      Vec3 aim;
      boolean slam;
   }

   static final class Reel {
      int target;
      UUID uuid;
      long grip;
      Vec3 start;
      int ticks;
   }

   static final class Sling {
      Vec3 left;
      Vec3 right;
      BlockPos leftBlock;
      BlockPos rightBlock;
      long at;
      int charge;
      long flying = -1L;
   }

   static final class Swing {
      Vec3 anchor;
      BlockPos block;
      Direction face;
      double len;
      double target;
      Vec3 vel;
      long since;
      int burn;
      boolean carry = true;
      int still;
      int pose = -1;
      double[][] track;
      Vec3 seen;
   }

   static final class Thrown {
      int id;
      UUID uuid;
      long at;
      boolean hitOther;
   }

   static final class Timed {
      long due;
      int epoch;
      BlackLashEvents.Act act;
   }

   static final class Zip {
      Vec3 anchor;
      int target;
      UUID uuid;
      long at;
      Vec3 vel;
      double lastDist = Double.MAX_VALUE;
      int stall;
      int most;
      boolean pound;
      boolean going;
      double fromY;
      double lastY;
      BlockPos block;
   }
}
