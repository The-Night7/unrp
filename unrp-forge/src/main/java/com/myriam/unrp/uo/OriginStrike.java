package com.myriam.unrp.uo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public final class OriginStrike {
   public static final double FLOOR = 0.25D;
   private static final ThreadLocal<Boolean> FLOORING = ThreadLocal.withInitial(() -> Boolean.FALSE);

   private OriginStrike() {
   }

   public static boolean fair(LivingEntity caster, Entity e) {
      if (e instanceof LivingEntity live) {
         if (live != caster && live.isAlive() && !live.isSpectator()) {
            if (live instanceof ArmorStand) {
               return false;
            }

            if (keeper(live)) {
               return false;
            }

            if (caster == null || live != caster.getVehicle() && live.getVehicle() != caster) {
               if (caster != null && live instanceof AbstractHorse) {
                  AbstractHorse h = (AbstractHorse)live;
                  if (caster.getUUID().equals(h.getOwnerUUID())) {
                     return false;
                  }
               }

               if (caster != null && HollowDeepFavors.servesOwner(live, caster)) {
                  return false;
               }

               if (caster != null && com.myriam.unrp.social.SocialService.allied(live, caster)) {
                  return false;
               }

               if (caster instanceof ServerPlayer) {
                  ServerPlayer sp = (ServerPlayer)caster;
                  if (live instanceof Player && com.myriam.unrp.social.SocialService.sameGroup(sp.server, sp.getUUID(), live.getUUID())) {
                     return false;
                  }
               }

               if (live instanceof Player) {
                  Player p = (Player)live;
                  if (p.isCreative()) {
                     return false;
                  }
               }

               return true;
            }

            return false;
         }
      }

      return false;
   }

   public static boolean keeper(LivingEntity live) {
      return false;
   }

   public static boolean foe(LivingEntity caster, Entity e) {
      return fair(caster, e);
   }

   public static boolean threat(LivingEntity caster, Entity e) {
      if (!fair(caster, e)) {
         return false;
      } else if (!(e instanceof Player) && !(e instanceof Enemy)) {
         if (caster != null && e instanceof Mob) {
            Mob m = (Mob)e;
            if (m.getTarget() == caster || m.getLastHurtByMob() == caster) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public static int rank(LivingEntity caster, Entity e) {
      return threat(caster, e) ? 0 : 1;
   }

   public static Comparator<LivingEntity> threatsFirst(LivingEntity caster, ToDoubleFunction<LivingEntity> then) {
      return Comparator.<LivingEntity>comparingInt((e) -> rank(caster, e)).thenComparingDouble(then);
   }

   public static List<LivingEntity> foesNear(ServerLevel level, LivingEntity caster, Vec3 c, double r) {
      List<LivingEntity> out = new ArrayList();

      for(LivingEntity live : level.getEntitiesOfClass(LivingEntity.class, (new AABB(c, c)).inflate(r + 2.0D))) {
         if (foe(caster, live)) {
            Vec3 mid = live.position().add(0.0D, (double)live.getBbHeight() * 0.5D, 0.0D);
            if (mid.distanceTo(c) <= r + (double)live.getBbWidth() * 0.5D) {
               out.add(live);
            }
         }
      }

      return out;
   }

   public static double toSegment(Vec3 p, Vec3 a, Vec3 b) {
      Vec3 ab = b.subtract(a);
      double len = ab.lengthSqr();
      double t = len < 1.0E-6D ? 0.0D : Math.max(0.0D, Math.min(1.0D, p.subtract(a).dot(ab) / len));
      return p.distanceTo(a.add(ab.scale(t)));
   }

   public static List<LivingEntity> foesAlong(ServerLevel level, LivingEntity caster, Vec3 a, Vec3 b, double r) {
      List<LivingEntity> out = new ArrayList();

      for(LivingEntity live : level.getEntitiesOfClass(LivingEntity.class, (new AABB(a, b)).inflate(r + 2.0D))) {
         if (foe(caster, live)) {
            Vec3 mid = live.position().add(0.0D, (double)live.getBbHeight() * 0.5D, 0.0D);
            if (toSegment(mid, a, b) <= r + (double)live.getBbWidth() * 0.5D) {
               out.add(live);
            }
         }
      }

      return out;
   }

   public static boolean active() {
      return FLOORING.get();
   }

   public static void shove(LivingEntity live, double x, double y, double z) {
      if (true) {
         live.setDeltaMovement(live.getDeltaMovement().add(x, y, z));
         live.hurtMarked = true;
      }
   }

   public static void pin(LivingEntity live) {
      if (true) {
         Vec3 v = live.getDeltaMovement();
         live.setDeltaMovement(v.x * 0.1D, Math.min(v.y, 0.0D), v.z * 0.1D);
         live.hurtMarked = true;
      }
   }

   public static boolean hit(LivingEntity victim, DamageSource source, float amount) {
      if (!(amount <= 0.0F) && victim.isAlive()) {
         boolean was = FLOORING.get();
         FLOORING.set(Boolean.TRUE);

         boolean var4;
         try {
            var4 = victim.hurt(source, amount);
         } finally {
            FLOORING.set(was);
         }

         return var4;
      } else {
         return false;
      }
   }

   public static boolean hitFresh(LivingEntity victim, DamageSource source, float amount) {
      victim.invulnerableTime = 0;
      return hit(victim, source, amount);
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onFloor(LivingDamageEvent event) {
      if (FLOORING.get()) {
         LivingEntity head_0 = event.getEntity();
         if (head_0 instanceof Player) {
            Player victim = (Player)head_0;
            float head = victim.getHealth() - victim.getMaxHealth() * 0.25F;
            if (head <= 0.0F) {
               return;
            }

            float cap = Math.max(0.0F, victim.getHealth() - 1.0F);
            if (event.getAmount() > cap) {
               event.setAmount(cap);
            }

            return;
         }
      }

   }
}
