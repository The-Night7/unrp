package com.myriam.unrp.uo;



import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class AuraThreadEntity extends Entity {
   public static final int KIND_SIPHON = 0;
   public static final int KIND_BESTOW = 1;
   public static final int KIND_TETHER = 2;
   public static final int KIND_UNRAVEL = 3;
   public static final int SIPHON_LIFE = 16;
   public static final int BESTOW_LIFE = 14;
   public static final int UNRAVEL_LIFE = 12;
   public static final int TETHER_GRACE = 40;
   public static final int ARRIVE = 11;
   private static final EntityDataAccessor<Integer> DATA_SOURCE = SynchedEntityData.defineId(AuraThreadEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_SINK = SynchedEntityData.defineId(AuraThreadEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_KIND = SynchedEntityData.defineId(AuraThreadEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(AuraThreadEntity.class, EntityDataSerializers.INT);
   private int keepUntil = 40;
   private boolean stale;

   public AuraThreadEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void configure(int source, int sink, int kind, int color) {
      this.entityData.set(DATA_SOURCE, source);
      this.entityData.set(DATA_SINK, sink);
      this.entityData.set(DATA_KIND, kind);
      this.entityData.set(DATA_COLOR, color);
   }

   public void keep() {
      this.keepUntil = this.tickCount + 40;
   }

   public int source() {
      return this.entityData.get(DATA_SOURCE);
   }

   public int sink() {
      return this.entityData.get(DATA_SINK);
   }

   public int kind() {
      return this.entityData.get(DATA_KIND);
   }

   public int color() {
      return this.entityData.get(DATA_COLOR);
   }

   public static int life(int kind) {
      return kind == 0 ? 16 : (kind == 1 ? 14 : (kind == 3 ? 12 : Integer.MAX_VALUE));
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_SOURCE, -1);
      this.entityData.define(DATA_SINK, -1);
      this.entityData.define(DATA_KIND, 0);
      this.entityData.define(DATA_COLOR, 5692644);
   }

   public void tick() {
      if (this.stale) {
         this.discard();
      } else {
         super.tick();
         Entity anchor = this.level().getEntity(this.sink());
         if (anchor != null) {
            this.setPos(anchor.getX(), anchor.getY() + (double)anchor.getBbHeight() * 0.5D, anchor.getZ());
         }

         if (!this.level().isClientSide()) {
            int kind = this.kind();
            if (kind == 2) {
               if (anchor == null || !anchor.isAlive() || this.tickCount > this.keepUntil) {
                  this.discard();
               }
            } else if (this.tickCount > life(kind) + 2) {
               this.discard();
            }

         }
      }
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 4096.0D;
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.stale = true;
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }
}
