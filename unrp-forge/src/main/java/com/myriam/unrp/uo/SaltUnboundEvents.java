package com.myriam.unrp.uo;

import com.myriam.unrp.anim.MoveAnim;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
public class SaltUnboundEvents {
   public static final String USED = "unrp_unbound_used";
   public static final String PHASE_KEY = "unrp_unbound_phase";
   public static final String LEFT_KEY = "unrp_unbound_left";
   public static final String HEALTH_KEY = "unrp_unbound_health";
   public static final String UNBOUND = "unrp.ss_unbound";
   public static final String PROWL = "unrp.ss_prowl";
   public static final String CLIMB = "unrp.ss_climb";
   public static final String SLOT4 = "unrp.unlock_s4";
   public static final UUID HEALTH_ID = UUID.fromString("5a17b0de-3d1e-4c7a-9e58-2f6b1c0d7a41");
   public static final String HEALTH_NAME = "unrp unbound";
   public static final int ERUPT = 1;
   public static final int BEAST = 2;
   static final String[] TAGS = new String[]{"unrp.ss_unbound", "unrp.ss_prowl", "unrp.ss_climb"};
   private static final Vec3 ZERO = Vec3.ZERO;
   static final Map<UUID, SaltUnboundEvents.Bound> BOUND = new HashMap();

   private SaltUnboundEvents() {
   }

   public static boolean bound(Entity e) {
      return e != null && BOUND.containsKey(e.getUUID());
   }

   public static int phase(Entity e) {
      SaltUnboundEvents.Bound b = e == null ? null : (SaltUnboundEvents.Bound)BOUND.get(e.getUUID());
      return b == null ? 0 : b.phase;
   }

   public static int left(Entity e) {
      SaltUnboundEvents.Bound b = e == null ? null : (SaltUnboundEvents.Bound)BOUND.get(e.getUUID());
      return b == null ? 0 : (b.phase == 2 ? b.left : 6000);
   }

   static double[] a(Vec3 v) {
      return new double[]{v.x, v.y, v.z};
   }

   static Vec3 v(double[] d) {
      return new Vec3(d[0], d[1], d[2]);
   }

   private static void sound(ServerLevel level, Vec3 at, String id, float volume, float pitch) {
      level.playSound((Player)null, at.x, at.y, at.z, CastFxEvents.sound(id), SoundSource.PLAYERS, volume, pitch);
   }

   private static void bar(ServerPlayer p, String msg) {
      p.displayClientMessage(Component.literal(msg), true);
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

   private static void fx(ServerPlayer p, int kind, Vec3 x, Vec3 x2, int age, int value, int bits) {
      SaltFxPacket msg = new SaltFxPacket(p.getId(), kind, 0, 0, (float)x.x, (float)x.y, (float)x.z, (float)x2.x, (float)x2.y, (float)x2.z, age, value, bits);
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p), msg);
   }

   private static void steer(ServerPlayer p, int mode, LivingEntity t) {
      Vec3 g = t != null ? t.position() : p.position();
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new SaltSteerPacket(mode, t == null ? 0 : t.getId(), (float)g.x, (float)g.y, (float)g.z));
   }

   private static void later(SaltUnboundEvents.Bound b, long due, SaltUnboundEvents.Act act) {
      SaltUnboundEvents.Timed t = new SaltUnboundEvents.Timed();
      t.due = due;
      t.act = act;
      b.timed.add(t);
   }

   private static void runTimed(ServerPlayer p, ServerLevel level, SaltUnboundEvents.Bound b, long now) {
      if (!b.timed.isEmpty()) {
         List<SaltUnboundEvents.Timed> due = new ArrayList();
         Iterator<SaltUnboundEvents.Timed> it = b.timed.iterator();

         while(it.hasNext()) {
            SaltUnboundEvents.Timed t_0 = (SaltUnboundEvents.Timed)it.next();
            if (t_0.due <= now) {
               it.remove();
               due.add(t_0);
            }
         }

         for(SaltUnboundEvents.Timed t : due) {
            if (BOUND.get(p.getUUID()) == b) {
               t.act.run(p, level, b, now);
            }
         }

      }
   }

   static boolean can(ServerPlayer p) {
      Set<String> tags = p.getTags();
      return tags.contains("unrp.has.salt_seraph") && tags.contains("unrp.ss_on") && score(p, "unrp.unlock_s4") >= 1 && SaltSeraphEvents.noncanonOn(p) && !p.isSpectator() && SaltMath.unboundReady(System.currentTimeMillis(), p.getPersistentData().getLong("unrp_unbound_used"));
   }

   static boolean body(ServerPlayer p, LivingEntity e) {
      if (e != p && e.isAlive() && !e.isSpectator() && !(e instanceof ArmorStand) && !TimeWeft.everpresent(e)) {
         if (e instanceof Player) {
            Player pl = (Player)e;
            if (pl.isCreative()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static boolean prey(ServerPlayer p, LivingEntity e) {
      return body(p, e) && !SaltSeraphEvents.isStatue(e) && !e.isInvisible();
   }

   static void health(ServerPlayer p) {
      AttributeInstance inst = p.getAttribute(Attributes.MAX_HEALTH);
      if (inst != null && inst.getModifier(HEALTH_ID) == null) {
         inst.addTransientModifier(new AttributeModifier(HEALTH_ID, "unrp unbound", 1.0D, Operation.MULTIPLY_TOTAL));
      }
   }

   static void unhealth(ServerPlayer p) {
      AttributeInstance inst = p.getAttribute(Attributes.MAX_HEALTH);
      if (inst != null && inst.getModifier(HEALTH_ID) != null) {
         inst.removeModifier(HEALTH_ID);
      }

      if (p.getHealth() > p.getMaxHealth()) {
         p.setHealth(p.getMaxHealth());
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onLethal(LivingDeathEvent event) {
      LivingEntity bypass_0 = event.getEntity();
      if (bypass_0 instanceof ServerPlayer p) {
         if (!event.isCanceled()) {
            boolean bypass = event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY);
            SaltUnboundEvents.Bound b = (SaltUnboundEvents.Bound)BOUND.get(p.getUUID());
            if (b != null) {
               if (b.phase == 1 && !bypass) {
                  event.setCanceled(true);
                  p.setHealth(1.0F);
               }

               return;
            }

            if (!bypass && can(p)) {
               event.setCanceled(true);
               erupt(p);
               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onDied(LivingDeathEvent event) {
      LivingEntity b_0 = event.getEntity();
      if (b_0 instanceof ServerPlayer p) {
         if (!event.isCanceled()) {
            SaltUnboundEvents.Bound b = (SaltUnboundEvents.Bound)BOUND.remove(p.getUUID());
            if (b != null) {
               finish(p, b, false);
            }

            return;
         }
      }

   }

   static void erupt(ServerPlayer p) {
      ServerLevel level = p.serverLevel();
      long now = level.getGameTime();
      p.getPersistentData().putLong("unrp_unbound_used", System.currentTimeMillis());
      SaltSeraphEvents.unleash(p);
      if (p.isPassenger()) {
         p.stopRiding();
      }

      p.setHealth(1.0F);
      p.setDeltaMovement(0.0D, Math.min(0.0D, p.getDeltaMovement().y), 0.0D);
      p.hurtMarked = true;
      p.fallDistance = 0.0F;
      SaltUnboundEvents.Bound b = new SaltUnboundEvents.Bound();
      b.phase = 1;
      b.at = now;
      b.born = now;
      BOUND.put(p.getUUID(), b);
      p.addTag("unrp.ss_unbound");
      MoveAnim.play(p, "ss_erupt");
      fx(p, 25, ZERO, ZERO, 0, 6100, 0);
      fx(p, 26, p.position(), ZERO, 0, 60, 0);
      later(b, now + 8L, (pp, lv, bb, nn) -> blast(pp, lv));
      steer(p, 1, (LivingEntity)null);
      sound(level, p.position(), "minecraft:entity.warden.emerge", 1.0F, 0.8F);
      sound(level, p.position(), "minecraft:entity.warden.heartbeat", 1.0F, 0.6F);
   }

   static void blast(ServerPlayer p, ServerLevel level) {
      Vec3 c = p.position();
      fx(p, 27, c, ZERO, 0, 34, 0);
      sound(level, c, "minecraft:entity.generic.explode", 1.2F, 0.7F);
      sound(level, c, "minecraft:entity.warden.roar", 1.0F, 0.9F);
      double r = 14.0D;

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, (new AABB(c, c)).inflate(r, 6.0D, r))) {
         if (body(p, e) && SaltMath.blastReaches(a(c), a(e.position()))) {
            if (SaltSeraphEvents.set(e)) {
               SaltSeraphEvents.shatter(p, level, e, 6.0D);
            } else {
               SaltSeraphEvents.landed(p, level, e, 4.0D, (Vec3)null, 0.0D, 0.0D);
               if (e.isAlive()) {
                  e.setDeltaMovement(v(SaltMath.blastVel(a(c), a(e.position()))));
                  e.hurtMarked = true;
               }
            }
         }
      }

   }

   static void beast(ServerPlayer p, SaltUnboundEvents.Bound b, long now, int left) {
      b.phase = 2;
      b.at = now;
      b.left = left;
      health(p);
      p.addTag("unrp.ss_prowl");
      MoveAnim.hold(p, "ss_prowl", 3);
   }

   static void finish(ServerPlayer p, SaltUnboundEvents.Bound b, boolean woke) {
      b.timed.clear();
      b.rends.clear();
      unhealth(p);

      for(String t : TAGS) {
         p.removeTag(t);
      }

      steer(p, 0, (LivingEntity)null);
      fx(p, 24, ZERO, ZERO, 0, 25, woke ? 4 : 0);
   }

   static void wake(ServerPlayer p, ServerLevel level, SaltUnboundEvents.Bound b) {
      BOUND.remove(p.getUUID());
      finish(p, b, true);
      p.setHealth(Math.max(1.0F, (float)SaltMath.wakeHealth((double)p.getMaxHealth())));
      p.fallDistance = 0.0F;
      bar(p, "§7The instinct lets go: you wake at 30% health.");
      sound(level, p.position(), "minecraft:block.beacon.deactivate", 0.8F, 0.6F);
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player b_0 = event.player;
         if (b_0 instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer)b_0;
            SaltUnboundEvents.Bound b = (SaltUnboundEvents.Bound)BOUND.get(p.getUUID());
            if (b != null) {
               if (!p.isAlive()) {
                  return;
               }

               ServerLevel level = p.serverLevel();
               long now = level.getGameTime();
               if (!p.getTags().contains("unrp.has.salt_seraph")) {
                  BOUND.remove(p.getUUID());
                  finish(p, b, false);
                  return;
               }

               p.fallDistance = 0.0F;
               runTimed(p, level, b, now);
               if (BOUND.get(p.getUUID()) != b) {
                  return;
               }

               tickRends(p, level, b, now);
               if (b.phase == 1) {
                  if (now - b.at >= 60L) {
                     beast(p, b, now, 6000);
                     p.setHealth(p.getMaxHealth());
                     steer(p, 5, (LivingEntity)null);
                  } else {
                     steer(p, 1, (LivingEntity)null);
                  }

                  return;
               }

               health(p);
               --b.left;
               b.cdRend = Math.max(0, b.cdRend - 1);
               b.cdHowl = Math.max(0, b.cdHowl - 1);
               b.cdAmbush = Math.max(0, b.cdAmbush - 1);
               b.cdThorns = Math.max(0, b.cdThorns - 1);
               b.cdSpear = Math.max(0, b.cdSpear - 1);
               if (b.busy > 0) {
                  --b.busy;
               }

               if (b.left <= 0) {
                  wake(p, level, b);
                  return;
               }

               brain(p, level, b, now);
               if (p.tickCount % 10 == 0) {
                  fx(p, 25, ZERO, ZERO, (int)Math.min(32000L, now - b.born), 6100, 0);
               }

               return;
            }

            if (stray(p)) {
               for(String t : TAGS) {
                  p.removeTag(t);
               }

               unhealth(p);
            }

            return;
         }
      }

   }

   private static boolean stray(ServerPlayer p) {
      Set<String> tags = p.getTags();

      for(String t : TAGS) {
         if (tags.contains(t)) {
            return true;
         }
      }

      AttributeInstance inst = p.getAttribute(Attributes.MAX_HEALTH);
      return inst != null && inst.getModifier(HEALTH_ID) != null;
   }

   static boolean hitLately(SaltUnboundEvents.Bound b, LivingEntity e, long now) {
      Long at = (Long)b.hits.get(e.getUUID());
      return at != null && now - at < 100L;
   }

   static int recent(ServerLevel level, SaltUnboundEvents.Bound b, long now) {
      int n = 0;
      Iterator<Map.Entry<UUID, Long>> it = b.hits.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<UUID, Long> en = (Map.Entry)it.next();
         if (now - en.getValue() >= 100L) {
            it.remove();
         } else {
            Entity e = level.getEntity((UUID)en.getKey());
            if (e instanceof LivingEntity) {
               LivingEntity live = (LivingEntity)e;
               if (live.isAlive()) {
                  ++n;
               }
            }
         }
      }

      return n;
   }

   static List<LivingEntity> foes(ServerPlayer p, ServerLevel level) {
      Vec3 c = p.position();
      List<LivingEntity> out = new ArrayList();
      AABB box = (new AABB(c, c)).inflate(32.0D, 16.0D, 32.0D);

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
         if (prey(p, e)) {
            double dx = e.getX() - c.x;
            double dz = e.getZ() - c.z;
            if (dx * dx + dz * dz <= 1024.0D) {
               out.add(e);
            }
         }
      }

      return out;
   }

   static LivingEntity targetOf(ServerLevel level, SaltUnboundEvents.Bound b) {
      Entity e = b.target == null ? null : level.getEntity(b.target);
      if (e instanceof LivingEntity live) {
         if (live.isAlive()) {
            return live;
         }
      }

      return null;
   }

   static void brain(ServerPlayer p, ServerLevel level, SaltUnboundEvents.Bound b, long now) {
      if (b.busy > 0) {
         steer(p, 3, targetOf(level, b));
         climbing(p, b, false);
      } else {
         List<LivingEntity> foes = foes(p, level);
         LivingEntity t = null;
         double best = Double.MAX_VALUE;
         double[] me = a(p.position());

         for(LivingEntity e_0 : foes) {
            double sc = SaltMath.score(SaltMath.dist(a(e_0.position()), me), hitLately(b, e_0, now), (double)e_0.getHealth());
            if (sc < best) {
               best = sc;
               t = e_0;
            }
         }

         if (t == null) {
            b.target = null;
            steer(p, 5, (LivingEntity)null);
            climbing(p, b, false);
         } else {
            b.target = t.getUUID();
            double d = SaltMath.dist(a(t.position()), me);
            int crowd = 0;

            for(LivingEntity e : foes) {
               if (SaltMath.crowds(SaltMath.dist(a(e.position()), me))) {
                  ++crowd;
               }
            }

            int act = SaltMath.decide(recent(level, b, now), crowd, d, b.cdHowl, b.cdThorns, b.cdRend, b.cdAmbush, b.cdSpear);
            if (act != 7) {
               climbing(p, b, false);
            }

            if (act == 1) {
               howl(p, level, b, now);
            } else if (act == 2) {
               thorns(p, level, b, now);
            } else if (act == 3) {
               rend(p, b, t, now);
            } else if (act == 4) {
               ambush(p, b, t, now);
            } else if (act == 5) {
               steer(p, 4, t);
               strike(p, b, t, now);
            } else if (act == 6) {
               steer(p, 4, t);
            } else {
               run(p, level, b, t, now);
            }

         }
      }
   }

   static void climbing(ServerPlayer p, SaltUnboundEvents.Bound b, boolean on) {
      if (b.climbing != on) {
         b.climbing = on;
         if (on) {
            p.removeTag("unrp.ss_prowl");
            p.addTag("unrp.ss_climb");
            MoveAnim.hold(p, "ss_climb", 3);
         } else {
            p.removeTag("unrp.ss_climb");
            p.addTag("unrp.ss_prowl");
            MoveAnim.hold(p, "ss_prowl", 3);
         }

      }
   }

   static void run(ServerPlayer p, ServerLevel level, SaltUnboundEvents.Bound b, LivingEntity t, long now) {
      steer(p, 2, t);
      double[] dir = SaltMath.flat(SaltMath.sub(a(t.position()), a(p.position())));
      int wall = SaltTerrain.wallAhead(level, a(p.position()), dir);
      if (wall == 1) {
         climbing(p, b, false);
         smash(p, b, dir, now);
      } else {
         climbing(p, b, wall == 2);
      }
   }

   static void smash(ServerPlayer p, SaltUnboundEvents.Bound b, double[] dir, long now) {
      if (now - b.smashed >= 6L) {
         b.smashed = now;
         MoveAnim.play(p, "ss_claw");
         double[] ahead = SaltMath.add(a(p.position()), SaltMath.mul(dir, 0.9D));
         ServerLevel there = p.serverLevel();
         later(b, now + 4L, (pp, lv, bb, nn) -> {
            if (lv == there) {
               breakOut(pp, lv, ahead);
            }

         });
      }
   }

   static void breakOut(ServerPlayer p, ServerLevel level, double[] ahead) {
      if (level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && p.mayBuild()) {
         int cx = Mth.floor(ahead[0]);
         int cz = Mth.floor(ahead[2]);
         int fy = Mth.floor(p.getY() + 0.2D);
         boolean any = false;

         for(int dx = -1; dx <= 1; ++dx) {
            for(int dz = -1; dz <= 1; ++dz) {
               for(int dy = 0; dy <= 2; ++dy) {
                  BlockPos pos = new BlockPos(cx + dx, fy + dy, cz + dz);
                  BlockState st = level.getBlockState(pos);
                  if (SaltTerrain.built(st) && !st.hasBlockEntity() && !(st.getDestroySpeed(level, pos) < 0.0F) && level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos) && level.mayInteract(p, pos) && ForgeHooks.onBlockBreakEvent(level, p.gameMode.getGameModeForPlayer(), p, pos) >= 0) {
                     level.destroyBlock(pos, true, p);
                     any = true;
                  }
               }
            }
         }

         if (any) {
            sound(level, p.position(), "minecraft:entity.zombie.break_wooden_door", 1.0F, 0.8F);
         }

      }
   }

   static void strike(ServerPlayer p, SaltUnboundEvents.Bound b, LivingEntity t, long now) {
      b.cdSpear = 20;
      MoveAnim.play(p, "ss_claw");
      later(b, now + 4L, (pp, lv, bb, nn) -> {
         if (t.isAlive() && t.level() == lv && pp.isAlive() && !SaltSeraphEvents.isStatue(t) && SaltMath.spearLands(SaltMath.dist(a(t.position()), a(pp.position())))) {
            SaltSeraphEvents.landed(pp, lv, t, 10.0D, pp.position(), 0.6D, 0.35D);
            if (t.isAlive()) {
               SaltSeraphEvents.salt(pp, t, 10.0D);
            }

            fx(pp, 29, pp.position(), ZERO, 0, 8, 0);
            sound(lv, pp.position(), "minecraft:entity.player.attack.sweep", 1.0F, 0.7F);
         }
      });
   }

   static void ambush(ServerPlayer p, SaltUnboundEvents.Bound b, LivingEntity t, long now) {
      b.cdAmbush = 120;
      b.busy = 12;
      MoveAnim.play(p, "ss_lunge");
      steer(p, 3, t);
      boolean[] behind = new boolean[]{false};
      later(b, now + 4L, (pp, lv, bb, nn) -> {
         if (t.isAlive() && t.level() == lv) {
            double[] spot = SaltMath.ambushSpot(a(t.position()), (double)t.getYRot());
            double g = SaltTerrain.groundAt(lv, spot[0], spot[2], t.getY() + 2.0D, 8);
            Vec3 to = new Vec3(spot[0], g, spot[2]);
            if (lv.noCollision(pp, pp.getBoundingBox().move(to.subtract(pp.position())))) {
               fx(pp, 28, pp.position().add(0.0D, (double)pp.getBbHeight() * 0.5D, 0.0D), ZERO, 0, 16, 0);
               float yaw = (float)SaltMath.yawTo(a(to), a(t.position()));
               pp.teleportTo(lv, to.x, to.y, to.z, yaw, pp.getXRot());
               pp.setDeltaMovement(Vec3.ZERO);
               pp.hurtMarked = true;
               behind[0] = true;
               fx(pp, 28, pp.position().add(0.0D, (double)pp.getBbHeight() * 0.5D, 0.0D), ZERO, 0, 16, 0);
               sound(lv, pp.position(), "minecraft:entity.enderman.teleport", 0.9F, 0.7F);
            }
         }
      });
      later(b, now + 9L, (pp, lv, bb, nn) -> {
         if (behind[0] && t.isAlive() && t.level() == lv && pp.isAlive() && !SaltSeraphEvents.isStatue(t)) {
            SaltSeraphEvents.landed(pp, lv, t, 12.0D, pp.position(), 0.8D, 0.4D);
            if (t.isAlive()) {
               SaltSeraphEvents.salt(pp, t, 15.0D);
            }

            fx(pp, 29, pp.position(), ZERO, 0, 8, 0);
            sound(lv, pp.position(), "minecraft:entity.player.attack.strong", 1.0F, 0.6F);
         }
      });
   }

   static void howl(ServerPlayer p, ServerLevel level, SaltUnboundEvents.Bound b, long now) {
      b.cdHowl = 300;
      b.busy = 18;
      b.hits.clear();
      MoveAnim.play(p, "ss_howl");
      steer(p, 3, targetOf(level, b));
      later(b, now + 4L, (pp, lv, bb, nn) -> {
         Vec3 c = pp.position();
         fx(pp, 30, c, ZERO, 0, 26, 0);
         sound(lv, c, "minecraft:entity.ravager.roar", 1.2F, 0.8F);
         sound(lv, c, "minecraft:entity.warden.roar", 0.8F, 1.2F);
         double r = 11.0D;

         for(LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, (new AABB(c, c)).inflate(r))) {
            if (body(pp, e) && !SaltSeraphEvents.isStatue(e) && SaltMath.howlReaches(a(c), a(e.position()))) {
               SaltSeraphEvents.landed(pp, lv, e, 2.0D, (Vec3)null, 0.0D, 0.0D);
               if (e.isAlive() && !SaltSeraphEvents.set(e)) {
                  e.setDeltaMovement(v(SaltMath.howlVel(a(c), a(e.position()))));
                  e.hurtMarked = true;
               }
            }
         }

      });
   }

   static void thorns(ServerPlayer p, ServerLevel level, SaltUnboundEvents.Bound b, long now) {
      b.cdThorns = 200;
      b.busy = 14;
      MoveAnim.play(p, "ss_thorns");
      steer(p, 3, targetOf(level, b));
      later(b, now + 5L, (pp, lv, bb, nn) -> {
         Vec3 c = pp.position();
         fx(pp, 31, c, ZERO, 0, 50, 0);
         sound(lv, c, "minecraft:entity.evoker_fangs.attack", 1.2F, 0.7F);
         sound(lv, c, "minecraft:block.amethyst_cluster.break", 1.0F, 0.8F);
         double r = 9.0D;

         for(LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, (new AABB(c, c)).inflate(r))) {
            if (body(pp, e) && SaltMath.thornsReach(a(c), a(e.position()))) {
               if (SaltSeraphEvents.set(e)) {
                  SaltSeraphEvents.shatter(pp, lv, e, 14.0D);
               } else {
                  SaltSeraphEvents.landed(pp, lv, e, 14.0D, c, 0.5D, 0.7D);
                  if (e.isAlive()) {
                     SaltSeraphEvents.salt(pp, e, 30.0D);
                  }
               }
            }
         }

      });
   }

   static void rend(ServerPlayer p, SaltUnboundEvents.Bound b, LivingEntity t, long now) {
      b.cdRend = 240;
      b.busy = 14;
      MoveAnim.play(p, "ss_rend");
      steer(p, 3, t);
      double[] dir = SaltMath.flat(SaltMath.sub(a(t.position()), a(p.position())));
      later(b, now + 8L, (pp, lv, bb, nn) -> {
         SaltUnboundEvents.Rend r = new SaltUnboundEvents.Rend();
         r.level = lv;
         r.at = v(SaltMath.rendFrom(a(pp.position()), dir));
         r.dir = v(dir);
         r.side = new Vec3(-dir[2], 0.0D, dir[0]);
         r.born = nn;
         bb.rends.add(r);
         fx(pp, 32, r.at, r.dir, 0, SaltMath.rendLife(), 0);
         sound(lv, r.at, "minecraft:entity.warden.attack_impact", 1.4F, 0.6F);
         sound(lv, r.at, "minecraft:block.sand.fall", 1.4F, 0.6F);
      });
   }

   static void tickRends(ServerPlayer p, ServerLevel level, SaltUnboundEvents.Bound b, long now) {
      if (!b.rends.isEmpty()) {
         Iterator<SaltUnboundEvents.Rend> it = b.rends.iterator();

         while(it.hasNext()) {
            SaltUnboundEvents.Rend r = (SaltUnboundEvents.Rend)it.next();
            int age = (int)(now - r.born);
            if (r.level == level && SaltMath.rendRunning((double)age)) {
               double reach = SaltMath.rendReach((double)age);
               Vec3 front = r.at.add(r.dir.scale(reach));
               AABB box = (new AABB(r.at, front)).inflate(2.8D, 0.0D, 2.8D).expandTowards(0.0D, 50.0D, 0.0D).expandTowards(0.0D, -4.0D, 0.0D);

               for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
                  if (body(p, e) && !r.hits.contains(e.getUUID()) && SaltMath.rendHit(a(r.at), a(r.dir), a(r.side), age, a(e.position()))) {
                     r.hits.add(e.getUUID());
                     if (SaltSeraphEvents.set(e)) {
                        SaltSeraphEvents.shatter(p, level, e, 20.0D);
                     } else {
                        SaltSeraphEvents.landed(p, level, e, 20.0D, e.position().subtract(r.dir), 0.9D, 1.0D);
                        if (e.isAlive()) {
                           SaltSeraphEvents.salt(p, e, 50.0D);
                        }
                     }
                  }
               }
            } else {
               it.remove();
            }
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onAttacked(LivingAttackEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer p) {
         if (phase(p) == 1 && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void onHurt(LivingHurtEvent event) {
      LivingEntity b_0 = event.getEntity();
      if (b_0 instanceof ServerPlayer p) {
         SaltUnboundEvents.Bound b = (SaltUnboundEvents.Bound)BOUND.get(p.getUUID());
         if (b != null && b.phase == 2) {
            event.setAmount(event.getAmount() * 0.5F);
            Entity var4 = event.getSource().getEntity();
            if (var4 instanceof LivingEntity) {
               LivingEntity by = (LivingEntity)var4;
               if (by != p) {
                  b.hits.put(by.getUUID(), p.serverLevel().getGameTime());
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onFall(LivingFallEvent event) {
      if (bound(event.getEntity())) {
         event.setDistance(0.0F);
         event.setDamageMultiplier(0.0F);
      }

   }

   @SubscribeEvent
   public static void onKnock(LivingKnockBackEvent event) {
      if (phase(event.getEntity()) == 1) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onSwing(AttackEntityEvent event) {
      if (bound(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onUse(PlayerInteractEvent.RightClickItem event) {
      if (bound(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onBlock(PlayerInteractEvent.RightClickBlock event) {
      if (bound(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onBreak(PlayerInteractEvent.LeftClickBlock event) {
      if (bound(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onTouch(PlayerInteractEvent.EntityInteract event) {
      if (bound(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public static void onEat(LivingEntityUseItemEvent.Start event) {
      if (bound(event.getEntity())) {
         event.setCanceled(true);
      }

   }

   static void keep(ServerPlayer p, SaltUnboundEvents.Bound b) {
      CompoundTag d = p.getPersistentData();
      d.putInt("unrp_unbound_phase", 2);
      d.putInt("unrp_unbound_left", b.phase == 2 ? Math.max(1, b.left) : 6000);
      d.putFloat("unrp_unbound_health", b.phase == 2 ? p.getHealth() : -1.0F);
   }

   @SubscribeEvent
   public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
      Player d_0 = event.getEntity();
      if (d_0 instanceof ServerPlayer p) {
         CompoundTag d = p.getPersistentData();
         if (d.contains("unrp_unbound_phase")) {
            int left = Math.max(1, Math.min(6000, d.getInt("unrp_unbound_left")));
            float hp = d.getFloat("unrp_unbound_health");
            d.remove("unrp_unbound_phase");
            d.remove("unrp_unbound_left");
            d.remove("unrp_unbound_health");
            if (p.isAlive() && p.getTags().contains("unrp.has.salt_seraph") && !p.isSpectator()) {
               long now = p.serverLevel().getGameTime();
               SaltUnboundEvents.Bound b = new SaltUnboundEvents.Bound();
               b.born = now - 60L - (long)(6000 - left);
               BOUND.put(p.getUUID(), b);
               p.addTag("unrp.ss_unbound");
               beast(p, b, now, left);
               p.setHealth(hp > 0.0F ? Math.min(hp, p.getMaxHealth()) : p.getMaxHealth());
               fx(p, 25, ZERO, ZERO, (int)Math.min(32000L, now - b.born), 6100, 0);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player b_0 = event.getEntity();
      if (b_0 instanceof ServerPlayer p) {
         SaltUnboundEvents.Bound b = (SaltUnboundEvents.Bound)BOUND.remove(p.getUUID());
         if (b != null) {
            keep(p, b);
            unhealth(p);

            for(String t : TAGS) {
               p.removeTag(t);
            }

         }
      }
   }

   @SubscribeEvent
   public static void onClone(PlayerEvent.Clone event) {
      CompoundTag from = event.getOriginal().getPersistentData();
      CompoundTag to = event.getEntity().getPersistentData();
      if (from.contains("unrp_unbound_used")) {
         to.putLong("unrp_unbound_used", from.getLong("unrp_unbound_used"));
      }

   }

   @SubscribeEvent
   public static void onStopping(ServerStoppingEvent event) {
      for(Map.Entry<UUID, SaltUnboundEvents.Bound> en : BOUND.entrySet()) {
         ServerPlayer p = event.getServer().getPlayerList().getPlayer((UUID)en.getKey());
         if (p != null) {
            keep(p, (SaltUnboundEvents.Bound)en.getValue());
            unhealth(p);

            for(String t : TAGS) {
               p.removeTag(t);
            }
         }
      }

      BOUND.clear();
   }

   interface Act {
      void run(ServerPlayer var1, ServerLevel var2, SaltUnboundEvents.Bound var3, long var4);
   }

   static final class Bound {
      int phase;
      long at;
      long born;
      int left = 6000;
      int busy;
      int cdRend = 40;
      int cdHowl = 0;
      int cdAmbush = 30;
      int cdThorns = 60;
      int cdSpear = 0;
      final Map<UUID, Long> hits = new HashMap();
      UUID target;
      long smashed = -100L;
      boolean climbing;
      final List<SaltUnboundEvents.Timed> timed = new ArrayList();
      final List<SaltUnboundEvents.Rend> rends = new ArrayList();
   }

   static final class Rend {
      ServerLevel level;
      Vec3 at;
      Vec3 dir;
      Vec3 side;
      long born;
      final List<UUID> hits = new ArrayList();
   }

   static final class Timed {
      long due;
      SaltUnboundEvents.Act act;
   }
}
