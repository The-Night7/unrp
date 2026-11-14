package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;


import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ArsenalBoltEntity extends ThrowableItemProjectile {
   public static final int MODE_BLADE = 0;
   public static final int MODE_SHOT = 1;
   public static final int EMBED = 16;
   public static final int LIFE = 50;
   private static final EntityDataAccessor<Integer> DATA_MODE = SynchedEntityData.defineId(ArsenalBoltEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(ArsenalBoltEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_STUCK = SynchedEntityData.defineId(ArsenalBoltEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_YAW = SynchedEntityData.defineId(ArsenalBoltEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_PITCH = SynchedEntityData.defineId(ArsenalBoltEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_ROLL = SynchedEntityData.defineId(ArsenalBoltEntity.class, EntityDataSerializers.FLOAT);
   private int worth = 4;
   private int procType = 0;
   private float damage = 4.0F;
   private int pierceLeft = 0;
   private int life = 0;
   private int stuckHere = 0;
   private final Set<Integer> struck = new HashSet();

   public ArsenalBoltEntity(EntityType<? extends ArsenalBoltEntity> type, Level level) {
      super(type, level);
   }

   public ArsenalBoltEntity(EntityType<? extends ArsenalBoltEntity> type, LivingEntity shooter, Level level) {
      super(type, shooter, level);
   }

   public void configure(int mode, int worth, int procType, float damage, int pierce) {
      this.entityData.set(DATA_MODE, mode);
      this.worth = worth;
      this.procType = procType;
      this.damage = damage;
      this.pierceLeft = pierce;
   }

   public void tier(int tier) {
      this.entityData.set(DATA_TIER, tier);
   }

   public void face(Vec3 dir, float roll) {
      if (!(dir.lengthSqr() < 1.0E-8D)) {
         Vec3 n = dir.normalize();
         this.entityData.set(DATA_YAW, (float)(Mth.atan2(n.x, n.z) * (180D / Math.PI)));
         this.entityData.set(DATA_PITCH, (float)(Mth.atan2(n.y, Math.sqrt(n.x * n.x + n.z * n.z)) * (180D / Math.PI)));
         this.entityData.set(DATA_ROLL, roll);
      }
   }

   public int mode() {
      return this.entityData.get(DATA_MODE);
   }

   public int tier() {
      return this.entityData.get(DATA_TIER);
   }

   public int stuckAt() {
      int synced = this.entityData.get(DATA_STUCK);
      return synced > 0 ? synced : this.stuckHere;
   }

   public float faceYaw() {
      return this.entityData.get(DATA_YAW);
   }

   public float facePitch() {
      return this.entityData.get(DATA_PITCH);
   }

   public float faceRoll() {
      return this.entityData.get(DATA_ROLL);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_MODE, 0);
      this.entityData.define(DATA_TIER, 1);
      this.entityData.define(DATA_STUCK, 0);
      this.entityData.define(DATA_YAW, 0.0F);
      this.entityData.define(DATA_PITCH, 0.0F);
      this.entityData.define(DATA_ROLL, 0.0F);
   }

   protected Item getDefaultItem() {
      return (Item)com.myriam.unrp.item.ModItems.ARMAMENT.get();
   }

   protected float getGravity() {
      return 0.0F;
   }

   public void tick() {
      int stuck = this.stuckAt();
      if (stuck > 0) {
         Level var5 = this.level();
         if (var5 instanceof ServerLevel) {
            ServerLevel sl_0 = (ServerLevel)var5;
            if (this.tickCount - stuck >= 16) {
               this.fade(sl_0);
            }
         }

      } else {
         super.tick();
         Level var3 = this.level();
         if (var3 instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)var3;
            if (!this.isRemoved()) {
               ++this.life;
               if (this.life > 50) {
                  this.fade(sl);
                  return;
               }

               if (this.mode() == 1) {
                  sl.sendParticles(Fx.flare(15254106), this.getX(), this.getY(), this.getZ(), 1, 0.03D, 0.03D, 0.03D, 0.0D);
               } else if (this.life % 3 == 0) {
                  sl.sendParticles(Fx.glint(15254106), this.getX(), this.getY(), this.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.0D);
               }
            }
         }

      }
   }

   protected boolean canHitEntity(Entity target) {
      if (super.canHitEntity(target) && !this.struck.contains(target.getId())) {
         if (this.level().isClientSide()) {
            return true;
         } else {
            Entity var3 = this.getOwner();
            if (var3 instanceof LivingEntity) {
               LivingEntity owner = (LivingEntity)var3;
               if (!OriginStrike.fair(owner, target)) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   protected void onHitEntity(EntityHitResult result) {
      Level victim_0 = this.level();
      if (victim_0 instanceof ServerLevel sl) {
         Entity attacker_0 = result.getEntity();
         if (attacker_0 instanceof LivingEntity victim) {
            this.struck.add(victim.getId());
            if (this.mode() == 0) {
               Entity le_0 = this.getOwner();
               if (le_0 instanceof ServerPlayer) {
                  ServerPlayer shooter = (ServerPlayer)le_0;
                  if (!ArmamentEvents.volleyHitAllowed(shooter, victim)) {
                     return;
                  }
               }
            }

            OriginStrike.hitFresh(victim, sl.damageSources().thrown(this, this.getOwner()), this.damage);
            Entity var6 = this.getOwner();
            LivingEntity var10000;
            if (var6 instanceof LivingEntity le) {
               var10000 = le;
            } else {
               var10000 = null;
            }

            LivingEntity attacker = var10000;
            if (this.procType > 0) {
               ArmamentEvents.applyProc(sl, victim, attacker, this.procType);
            }

            Fx.burst(sl, Fx.flare(15254106), victim.position().add(0.0D, (double)victim.getBbHeight() * 0.6D, 0.0D), this.mode() == 1 ? 8 : 5, 0.3D, 0.05D);
            if (this.mode() == 1 && this.pierceLeft > 0) {
               --this.pierceLeft;
            } else {
               this.fade(sl);
            }
         }
      }
   }

   protected void onHitBlock(BlockHitResult result) {
      Vec3 v = this.getDeltaMovement();
      if (this.mode() != 1 && !(v.lengthSqr() < 1.0E-6D)) {
         Vec3 at = result.getLocation().subtract(v.normalize().scale(0.3D));
         this.setPos(at.x, at.y, at.z);
         this.setDeltaMovement(Vec3.ZERO);
         this.stuckHere = Math.max(1, this.tickCount);
         Level var5 = this.level();
         if (var5 instanceof ServerLevel) {
            ServerLevel sl_0 = (ServerLevel)var5;
            this.entityData.set(DATA_STUCK, this.stuckHere);
            sl_0.playSound((Player)null, this.getX(), this.getY(), this.getZ(), CastFxEvents.sound("minecraft:item.trident.hit_ground"), SoundSource.PLAYERS, 0.55F, 1.3F + sl_0.random.nextFloat() * 0.3F);
            Fx.burst(sl_0, Fx.glint(15254106), at, 4, 0.15D, 0.04D);
         }

      } else {
         Level sl_1 = this.level();
         if (sl_1 instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)sl_1;
            this.fade(sl);
         }

      }
   }

   private void fade(ServerLevel sl) {
      Fx.burst(sl, Fx.glint(15254106), this.position().add(0.0D, 0.1D, 0.0D), 6, 0.2D, 0.05D);
      this.discard();
   }
}
