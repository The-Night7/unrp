package com.myriam.unrp.uo;



import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class WaveOrbEntity extends Entity {
   private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(WaveOrbEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(WaveOrbEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_OWNER = SynchedEntityData.defineId(WaveOrbEntity.class, EntityDataSerializers.INT);
   private int lease;
   private boolean stale;

   public WaveOrbEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void setRadius(float r) {
      this.entityData.set(DATA_RADIUS, r);
   }

   public float getRadius() {
      return this.entityData.get(DATA_RADIUS);
   }

   public void setColor(int rgb) {
      this.entityData.set(DATA_COLOR, rgb);
   }

   public int getColor() {
      return this.entityData.get(DATA_COLOR);
   }

   public void lease() {
      this.lease = 0;
   }

   public void setOwner(int id) {
      this.entityData.set(DATA_OWNER, id);
   }

   public int getOwner() {
      return this.entityData.get(DATA_OWNER);
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_RADIUS, 0.3F);
      this.entityData.define(DATA_COLOR, 16730680);
      this.entityData.define(DATA_OWNER, -1);
   }

   public void tick() {
      if (this.stale) {
         this.discard();
      } else {
         super.tick();
         if (!this.level().isClientSide) {
            ++this.lease;
            if (this.lease > 40) {
               this.discard();
            }

         }
      }
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.stale = true;
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }
}
