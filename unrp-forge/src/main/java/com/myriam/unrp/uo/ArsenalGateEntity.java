package com.myriam.unrp.uo;



import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ArsenalGateEntity extends Entity {
   public static final int KIND_ARSENAL = 0;
   public static final int KIND_CLAIM = 1;
   public static final int OPEN = 5;
   public static final int CLOSE = 6;
   public static final int EMERGE = 6;
   public static final float EMERGE_AT = 1.5F;
   public static final int MOST = 200;
   public static final float BLADE = 1.15F;
   public static final double MOUTH = 0.44724074072778225D;
   private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ArsenalGateEntity.class, EntityDataSerializers.ITEM_STACK);
   private static final EntityDataAccessor<Integer> DATA_FIRE = SynchedEntityData.defineId(ArsenalGateEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_KIND = SynchedEntityData.defineId(ArsenalGateEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(ArsenalGateEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_YAW = SynchedEntityData.defineId(ArsenalGateEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_PITCH = SynchedEntityData.defineId(ArsenalGateEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_ROLL = SynchedEntityData.defineId(ArsenalGateEntity.class, EntityDataSerializers.FLOAT);
   public float aimYawO;
   public float aimPitchO;
   public float aimYaw;
   public float aimPitch;
   private boolean stale;

   public ArsenalGateEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void configure(ItemStack blade, int fireAt, int kind, float radius, float roll) {
      this.entityData.set(DATA_ITEM, blade.copy());
      this.entityData.set(DATA_FIRE, fireAt);
      this.entityData.set(DATA_KIND, kind);
      this.entityData.set(DATA_RADIUS, radius);
      this.entityData.set(DATA_ROLL, roll);
   }

   public void aim(Vec3 dir) {
      if (!(dir.lengthSqr() < 1.0E-8D)) {
         Vec3 n = dir.normalize();
         float yaw = (float)(Mth.atan2(n.x, n.z) * (180D / Math.PI));
         float pitch = (float)(Mth.atan2(n.y, Math.sqrt(n.x * n.x + n.z * n.z)) * (180D / Math.PI));
         this.entityData.set(DATA_YAW, yaw);
         this.entityData.set(DATA_PITCH, pitch);
         this.aimYaw = yaw;
         this.aimPitch = pitch;
      }
   }

   public static Vec3 direction(float yaw, float pitch) {
      double y = Math.toRadians((double)yaw);
      double p = Math.toRadians((double)pitch);
      double c = Math.cos(p);
      return new Vec3(Math.sin(y) * c, Math.sin(p), Math.cos(y) * c);
   }

   public Vec3 facing() {
      return direction(this.entityData.get(DATA_YAW), this.entityData.get(DATA_PITCH));
   }

   public float yaw(float partialTick) {
      return this.tickCount < 1 ? this.entityData.get(DATA_YAW) : Mth.rotLerp(partialTick, this.aimYawO, this.aimYaw);
   }

   public float pitch(float partialTick) {
      return this.tickCount < 1 ? this.entityData.get(DATA_PITCH) : Mth.lerp(partialTick, this.aimPitchO, this.aimPitch);
   }

   public ItemStack blade() {
      return (ItemStack)this.entityData.get(DATA_ITEM);
   }

   public int fireAt() {
      return this.entityData.get(DATA_FIRE);
   }

   public int kind() {
      return this.entityData.get(DATA_KIND);
   }

   public float radius() {
      return this.entityData.get(DATA_RADIUS);
   }

   public float roll() {
      return this.entityData.get(DATA_ROLL);
   }

   public boolean fired() {
      return this.tickCount >= this.fireAt();
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_ITEM, ItemStack.EMPTY);
      this.entityData.define(DATA_FIRE, 12);
      this.entityData.define(DATA_KIND, 0);
      this.entityData.define(DATA_RADIUS, 0.6F);
      this.entityData.define(DATA_YAW, 0.0F);
      this.entityData.define(DATA_PITCH, 0.0F);
      this.entityData.define(DATA_ROLL, 0.0F);
   }

   public void tick() {
      if (this.stale) {
         this.discard();
      } else {
         super.tick();
         this.aimYawO = this.aimYaw;
         this.aimPitchO = this.aimPitch;
         this.aimYaw = this.entityData.get(DATA_YAW);
         this.aimPitch = this.entityData.get(DATA_PITCH);
         if (this.tickCount == 1) {
            this.aimYawO = this.aimYaw;
            this.aimPitchO = this.aimPitch;
         }

         if (!this.level().isClientSide() && (this.tickCount > this.fireAt() + 6 + 2 || this.tickCount > 200)) {
            this.discard();
         }

      }
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 9216.0D;
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.stale = true;
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }
}
