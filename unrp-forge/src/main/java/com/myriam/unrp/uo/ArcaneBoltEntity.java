package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;


import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.Vector3f;

public class ArcaneBoltEntity extends Entity {
   private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(ArcaneBoltEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(ArcaneBoltEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Integer> DATA_TINT = SynchedEntityData.defineId(ArcaneBoltEntity.class, EntityDataSerializers.INT);
   private float damage = 5.0F;
   private float burstRadius = 1.2F;
   private int life = 0;
   private boolean ruled = false;
   private boolean stale;
   private Consumer<LivingEntity> onStrike = null;

   public ArcaneBoltEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void configure(UUID owner, float damage, float size, float burstRadius, Vec3 velocity) {
      this.entityData.set(DATA_OWNER, Optional.ofNullable(owner));
      this.entityData.set(DATA_SIZE, size);
      this.damage = damage;
      this.burstRadius = burstRadius;
      if (velocity != null) {
         this.setDeltaMovement(velocity);
      }

   }

   public void tint(int rgb) {
      this.entityData.set(DATA_TINT, rgb);
   }

   public void rule(Consumer<LivingEntity> onStrike) {
      this.ruled = true;
      this.onStrike = onStrike;
   }

   public boolean ruled() {
      return this.ruled;
   }

   private LivingEntity caster() {
      UUID own = this.getOwnerId();
      if (own != null) {
         Level var3 = this.level();
         if (var3 instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)var3;
            return sl.getPlayerByUUID(own);
         }
      }

      return null;
   }

   public int getTint() {
      return this.entityData.get(DATA_TINT);
   }

   private Vector3f tintVec() {
      int c = this.getTint();
      return new Vector3f((float)(c >> 16 & 255) / 255.0F, (float)(c >> 8 & 255) / 255.0F, (float)(c & 255) / 255.0F);
   }

   public float getBoltSize() {
      return this.entityData.get(DATA_SIZE);
   }

   public UUID getOwnerId() {
      return (UUID)((Optional)this.entityData.get(DATA_OWNER)).orElse((Object)null);
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_SIZE, 0.6F);
      this.entityData.define(DATA_OWNER, Optional.empty());
      this.entityData.define(DATA_TINT, 0);
   }

   public void tick() {
      if (this.stale) {
         this.discard();
      } else {
         super.tick();
         if (this.level().isClientSide) {
            Vec3 v = this.getDeltaMovement();
            this.setPos(this.getX() + v.x, this.getY() + v.y, this.getZ() + v.z);
         } else {
            ++this.life;
            if (this.life > 400) {
               this.discard();
            } else {
               Vec3 vel = this.getDeltaMovement();
               if (vel.lengthSqr() < 0.01D) {
                  this.discard();
               } else {
                  Vec3 from = this.position();
                  Vec3 to = from.add(vel);
                  HitResult block = this.level().clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, this));
                  LivingEntity caster = this.ruled ? this.caster() : null;
                  Iterator sl_0 = this.level().getEntities(this, this.getBoundingBox().expandTowards(vel).inflate(0.9D), (en) -> en.isAlive() && en instanceof LivingEntity && !en.getUUID().equals(this.getOwnerId()) && (!this.ruled || OriginStrike.fair(caster, en))).iterator();
                  if (sl_0.hasNext()) {
                     Entity e_0 = (Entity)sl_0.next();
                     Vec3 hitAt = e_0.position().add(0.0D, (double)e_0.getBbHeight() * 0.5D, 0.0D);
                     this.setPos(hitAt.x, hitAt.y, hitAt.z);
                     if (e_0 instanceof LivingEntity) {
                        LivingEntity le_0 = (LivingEntity)e_0;
                        le_0.invulnerableTime = 0;
                     }

                     if (this.ruled && e_0 instanceof LivingEntity) {
                        LivingEntity le = (LivingEntity)e_0;
                        this.ruledBurst(le);
                     } else {
                        this.burst();
                     }

                  } else if (block.getType() != Type.MISS) {
                     Vec3 end = block.getLocation();
                     this.setPos(end.x, end.y, end.z);
                     if (this.ruled) {
                        this.ruledBurst((LivingEntity)null);
                     } else {
                        this.burst();
                     }

                  } else {
                     this.setPos(to.x, to.y, to.z);
                     Level e = this.level();
                     if (e instanceof ServerLevel) {
                        ServerLevel sl = (ServerLevel)e;
                        sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 1, 0.03D, 0.03D, 0.03D, 0.0D);
                        if (this.getTint() != 0) {
                           sl.sendParticles(new DustParticleOptions(this.tintVec(), 1.6F), this.getX(), this.getY(), this.getZ(), 4, 0.16D, 0.16D, 0.16D, 0.01D);
                        }

                        if (this.life % 3 == 0) {
                           sl.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.02D);
                        }
                     }

                  }
               }
            }
         }
      }
   }

   private void burst() {
      this.burst(true);
   }

   private void burst(boolean harm) {
      Level own_0 = this.level();
      if (own_0 instanceof ServerLevel sl) {
         UUID own = this.getOwnerId();
         Player owner = own == null ? null : sl.getPlayerByUUID(own);
         DamageSource var10000;
         if (owner instanceof ServerPlayer sp) {
            var10000 = sl.damageSources().playerAttack(sp);
         } else {
            var10000 = sl.damageSources().magic();
         }

         DamageSource source = var10000;

         for(LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(Math.max((double)this.burstRadius, 1.2D)), (en) -> harm && en.isAlive() && !en.getUUID().equals(this.getOwnerId()))) {
            e.hurt(source, this.damage);
            Vec3 push = e.position().subtract(this.position());
            Vec3 dir = (new Vec3(push.x, 0.0D, push.z)).normalize();
            e.setDeltaMovement(e.getDeltaMovement().add(dir.x * 0.35D, 0.15D, dir.z * 0.35D));
            e.hurtMarked = true;
         }

         sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), (int)(10.0F + 14.0F * this.burstRadius), 0.25D * (double)this.burstRadius, 0.25D * (double)this.burstRadius, 0.25D * (double)this.burstRadius, 0.1D);
         if (this.getTint() != 0) {
            sl.sendParticles(new DustParticleOptions(this.tintVec(), 1.6F), this.getX(), this.getY(), this.getZ(), (int)(14.0F + 12.0F * this.burstRadius), 0.35D * (double)this.burstRadius, 0.35D * (double)this.burstRadius, 0.35D * (double)this.burstRadius, 0.05D);
         }

         sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY(), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
         sl.playSound((Player)null, this.blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 0.8F, 1.4F);
         sl.playSound((Player)null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5F, 1.7F);
      }

      this.discard();
   }

   private void ruledBurst(LivingEntity struck) {
      Level caster_0 = this.level();
      if (caster_0 instanceof ServerLevel sl) {
         LivingEntity caster = this.caster();
         if (caster instanceof ServerPlayer sp) {
            DamageSource source = sl.damageSources().playerAttack(sp);
            List<LivingEntity> caught = OriginStrike.foesNear(sl, caster, this.position(), (double)this.burstRadius);
            if (struck != null && struck.isAlive() && !caught.contains(struck)) {
               caught.add(0, struck);
            }

            for(LivingEntity e : caught) {
               OriginStrike.hitFresh(e, source, this.damage);
               Vec3 push = e.position().subtract(this.position());
               Vec3 dir = (new Vec3(push.x, 0.0D, push.z)).normalize();
               OriginStrike.shove(e, dir.x * 0.45D, 0.2D, dir.z * 0.45D);
               if (this.onStrike != null) {
                  this.onStrike.accept(e);
               }
            }
         }

         if (this.getTint() != 0) {
            Fx.ring(sl, this.getTint(), this.getX(), this.getY(), this.getZ(), (double)this.burstRadius, 12);
            Fx.shell(sl, Fx.ember(this.getTint()), this.position(), (double)this.burstRadius * 0.5D, 18, 0.18D);
         }
      }

      this.burst(false);
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.entityData.set(DATA_SIZE, tag.getFloat("BSize"));
      this.damage = tag.getFloat("BDmg");
      if (tag.getBoolean("BRuled")) {
         this.stale = true;
      }

   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      tag.putFloat("BSize", this.getBoltSize());
      tag.putFloat("BDmg", this.damage);
      tag.putBoolean("BRuled", this.ruled);
   }
}
