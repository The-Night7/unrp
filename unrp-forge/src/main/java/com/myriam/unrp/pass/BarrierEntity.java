package com.myriam.unrp.pass;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BarrierEntity extends Entity {
   private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_HP = SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_MAX_HP = SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> DATA_SELF = SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Integer> DATA_BORN = SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_STAGE = SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_RAM = SynchedEntityData.defineId(BarrierEntity.class, EntityDataSerializers.BOOLEAN);
   private UUID ownerId = null;
   private long lastHurtTime = 0L;
   private int slot = -1;
   private boolean broken;

   public BarrierEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void configure(UUID owner, float maxHp, boolean self, float size) {
      this.ownerId = owner;
      this.entityData.set(DATA_OWNER, Optional.ofNullable(owner));
      this.entityData.set(DATA_MAX_HP, maxHp);
      this.entityData.set(DATA_HP, maxHp);
      this.entityData.set(DATA_SELF, self);
      this.entityData.set(DATA_SIZE, size);
      this.entityData.set(DATA_BORN, (int)this.level().getGameTime());
      this.refreshDimensions();
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_SIZE, 1.75F);
      this.entityData.define(DATA_HP, 30.0F);
      this.entityData.define(DATA_MAX_HP, 30.0F);
      this.entityData.define(DATA_SELF, false);
      this.entityData.define(DATA_OWNER, Optional.empty());
      this.entityData.define(DATA_BORN, 0);
      this.entityData.define(DATA_STAGE, 0);
      this.entityData.define(DATA_RAM, false);
   }

   public int getBorn() {
      return this.entityData.get(DATA_BORN);
   }

   public int getStage() {
      return this.entityData.get(DATA_STAGE);
   }

   private void setStage(int stage) {
      this.entityData.set(DATA_STAGE, stage);
   }

   public boolean isRam() {
      return this.entityData.get(DATA_RAM);
   }

   public void markRam() {
      this.entityData.set(DATA_RAM, true);
   }

   public Vec3 center() {
      return new Vec3(this.getX(), this.getY() + (double)(this.getSize() / 2.0F), this.getZ());
   }

   public float getSize() {
      return this.entityData.get(DATA_SIZE);
   }

   public void setSize(float size) {
      this.entityData.set(DATA_SIZE, Math.max(2.5F, Math.min(7.0F, size)));
      this.refreshDimensions();
      this.setPos(this.getX(), this.getY(), this.getZ());
   }

   public float getHp() {
      return this.entityData.get(DATA_HP);
   }

   public float getMaxHp() {
      return this.entityData.get(DATA_MAX_HP);
   }

   public boolean isSelfBarrier() {
      return this.entityData.get(DATA_SELF);
   }

   public int getSlot() {
      return this.slot;
   }

   public void setSlot(int slot) {
      this.slot = slot;
   }

   public UUID getOwnerId() {
      return (UUID)((Optional)this.entityData.get(DATA_OWNER)).orElse(this.ownerId);
   }

   public boolean isInside(Entity e) {
      float r = this.getSize() / 2.0F;
      Vec3 center = new Vec3(this.getX(), this.getY() + (double)r, this.getZ());
      Vec3 ec = new Vec3(e.getX(), e.getY() + (double)e.getBbHeight() / 2.0D, e.getZ());
      return ec.distanceTo(center) < (double)r;
   }

   public boolean clearlyOutside(Entity e) {
      float r = this.getSize() / 2.0F;
      Vec3 center = new Vec3(this.getX(), this.getY() + (double)r, this.getZ());
      Vec3 ec = new Vec3(e.getX(), e.getY() + (double)e.getBbHeight() / 2.0D, e.getZ());
      return ec.distanceTo(center) > (double)r + 0.75D;
   }

   public float hpFraction() {
      float m = this.getMaxHp();
      return m <= 0.0F ? 0.0F : this.getHp() / m;
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (DATA_SIZE.equals(key)) {
         this.refreshDimensions();
      }

   }

   public EntityDimensions getDimensions(Pose pose) {
      float s = this.entityData == null ? 1.75F : this.getSize();
      return EntityDimensions.scalable(s, s);
   }

   public boolean isPickable() {
      return false;
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean hurt(DamageSource source, float amount) {
      if (!this.level().isClientSide && !(amount <= 0.0F)) {
         if (this.getTags().contains("unrp.sanctuary")) {
         }

         if (source.getEntity() != null && source.getEntity().getUUID().equals(this.ownerId) && source.getDirectEntity() == source.getEntity()) {
            amount = Math.max(amount, 4.0F);
         }

         this.lastHurtTime = this.level().getGameTime();
         float hp = this.getHp() - amount;
         this.entityData.set(DATA_HP, Math.max(0.0F, hp));
         this.checkMilestones();
         if (!this.isRemoved()) {
            BarrierPass.hit(this, source, amount);
         }

         if (hp <= 0.0F) {
            this.shatter(true);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean struck(DamageSource source, float amount) {
      float stood = this.getHp();
      boolean took = this.hurt(source, amount);
      if (took) {
         BarrierReflect.turn(this, source, amount, stood);
      }

      return took;
   }

   private void checkMilestones() {
      float f = this.hpFraction();
      int stage = f <= 0.0F ? 4 : (f <= 0.25F ? 3 : (f <= 0.5F ? 2 : (f <= 0.75F ? 1 : 0)));

      while(this.getStage() < stage) {
         this.setStage(this.getStage() + 1);
         this.recoilOwner(1.0F + (float)this.getStage());
      }

   }

   private void recoilOwner(float amount) {
      Level lv_lv0 = this.level();
      if (lv_lv0 instanceof ServerLevel sl) {
         if (this.ownerId != null) {
            ServerPlayer owner = sl.getServer().getPlayerList().getPlayer(this.ownerId);
            if (owner != null && owner.isAlive()) {
               owner.hurt(sl.damageSources().magic(), amount);
               owner.displayClientMessage(Component.literal("§b❈ Ta barrière cède !"), true);
               return;
            }

            return;
         }
      }

   }

   public void mend(float amount) {
      if (!(amount <= 0.0F)) {
         this.entityData.set(DATA_HP, Math.min(this.getMaxHp(), this.getHp() + amount));
         float f = this.hpFraction();
         int stage = f <= 0.25F ? 3 : (f <= 0.5F ? 2 : (f <= 0.75F ? 1 : 0));
         if (stage < this.getStage()) {
            this.setStage(stage);
         }

      }
   }

   public void shatter(boolean recoil) {
      Level lv_lv0 = this.level();
      if (lv_lv0 instanceof ServerLevel sl) {
         if (!this.broken) {
            this.broken = true;
            BarrierPass.shatter(this);
         }

         if (this.slot >= 0 && this.ownerId != null) {
            ServerPlayer owner = sl.getServer().getPlayerList().getPlayer(this.ownerId);
            if (owner != null) {
               long until = sl.getGameTime() + 600L;
               String key = this.slot == 0 ? "unrp_barrier_self_lock" : (this.slot == 1 ? "unrp_barrier_ext_lock_0" : "unrp_barrier_ext_lock_1");
               owner.getPersistentData().putLong(key, until);
               owner.displayClientMessage(Component.literal(this.slot == 0 ? "§c❈ Ta barrière a volé en éclats. Elle se régénère dans 30 s." : "§c❈ Une barrière externe a volé en éclats. Cet emplacement se régénère dans 30 s."), true);
            }
         }
      }

      this.discard();
   }

   public void dissipate() {
      if (this.level() instanceof ServerLevel && !this.isRemoved() && !this.broken) {
         BarrierPass.gone(this);
      }

      this.discard();
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         long now = this.level().getGameTime();
         if (now % 60L == 0L && this.getHp() < this.getMaxHp() && now - this.lastHurtTime >= 100L) {
            this.entityData.set(DATA_HP, Math.min(this.getMaxHp(), this.getHp() + 1.0F));
            float f = this.hpFraction();
            int stage = f <= 0.25F ? 3 : (f <= 0.5F ? 2 : (f <= 0.75F ? 1 : 0));
            if (stage < this.getStage()) {
               this.setStage(stage);
            }
         }

         UUID own = this.getOwnerId();
         if (now % 20L == 0L && own != null) {
            Player owner = this.level().getPlayerByUUID(own);
            if (owner == null || !owner.isAlive() || owner.distanceTo(this) > 50.0F) {
               if (owner != null) {
                  owner.displayClientMessage(Component.literal("§7Ta barrière s'efface : tu es trop loin."), true);
               }

               this.dissipate();
               return;
            }
         }

         if (now % 20L == 0L) {
            for(Mob mob : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(1.6D), (m) -> m.isAlive() && m.getTarget() != null && this.clearlyOutside(m))) {
               if (this.isInside(mob.getTarget())) {
                  mob.swing(InteractionHand.MAIN_HAND);
                  this.struck(this.level().damageSources().mobAttack(mob), 2.5F);
                  break;
               }
            }
         }

         float r = this.getSize() / 2.0F;
         Vec3 center = new Vec3(this.getX(), this.getY() + (double)r, this.getZ());

         for(LivingEntity le : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.0D), (lex) -> lex.isAlive() && !lex.getUUID().equals(this.ownerId))) {
            if (!false) {
               Vec3 lc = new Vec3(le.getX(), le.getY() + (double)le.getBbHeight() / 2.0D, le.getZ());
               Vec3 toC = lc.subtract(center);
               double dist = toC.length();
               if (!(dist < 0.05D)) {
                  boolean inside = dist < (double)r;
                  double shellGap = Math.abs(dist - (double)r);
                  if (inside && dist > (double)r - 0.9D) {
                     Vec3 dir = toC.normalize();
                     double overshoot = dist - ((double)r - 0.9D);
                     Vec3 correct = dir.scale(-(0.35D + overshoot));
                     le.setDeltaMovement(le.getDeltaMovement().x * 0.3D + correct.x, le.getDeltaMovement().y, le.getDeltaMovement().z * 0.3D + correct.z);
                     Vec3 clamped = center.add(dir.scale(Math.min(dist, (double)r - 0.6D)));
                     le.setPos(clamped.x, le.getY(), clamped.z);
                     if (le instanceof ServerPlayer) {
                        ServerPlayer sp = (ServerPlayer)le;
                        sp.connection.send(new ClientboundSetEntityMotionPacket(le));
                        sp.connection.send(new ClientboundTeleportEntityPacket(le));
                     }
                  } else if (!inside && shellGap < 0.6D) {
                     Vec3 dir = toC.normalize();
                     le.setDeltaMovement(le.getDeltaMovement().add(dir.scale(0.18D)));
                     if (le instanceof ServerPlayer) {
                        ServerPlayer sp = (ServerPlayer)le;
                        sp.connection.send(new ClientboundSetEntityMotionPacket(le));
                     }
                  }

                  if (inside && this.slot >= 0 && this.getSize() < 5.0F && now % 10L == 0L) {
                     float crush = 2.0F + (5.0F - this.getSize()) * 1.2F;
                     le.hurt(this.level().damageSources().cramming(), crush);
                     le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, true));
                     if (!this.isRemoved()) {
                        BarrierPass.crush(this, le);
                     }
                  }
               }
            }
         }

      }
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      if (tag.hasUUID("Owner")) {
         this.ownerId = tag.getUUID("Owner");
         this.entityData.set(DATA_OWNER, Optional.of(this.ownerId));
      }

      this.entityData.set(DATA_SIZE, tag.getFloat("Size"));
      this.entityData.set(DATA_HP, tag.getFloat("Hp"));
      this.entityData.set(DATA_MAX_HP, tag.getFloat("MaxHp"));
      this.entityData.set(DATA_SELF, tag.getBoolean("Self"));
      this.setStage(tag.getInt("Stage"));
      this.entityData.set(DATA_RAM, tag.getBoolean("Ram"));
      this.slot = tag.contains("Slot") ? tag.getInt("Slot") : -1;
      this.refreshDimensions();
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      if (this.ownerId != null) {
         tag.putUUID("Owner", this.ownerId);
      }

      tag.putFloat("Size", this.getSize());
      tag.putFloat("Hp", this.getHp());
      tag.putFloat("MaxHp", this.getMaxHp());
      tag.putBoolean("Self", this.isSelfBarrier());
      tag.putInt("Stage", this.getStage());
      tag.putBoolean("Ram", this.isRam());
      tag.putInt("Slot", this.slot);
   }
}
