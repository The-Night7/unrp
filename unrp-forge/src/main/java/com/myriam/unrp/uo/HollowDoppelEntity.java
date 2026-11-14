package com.myriam.unrp.uo;



import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.level.Level;

public class HollowDoppelEntity extends PathfinderMob {
   private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(HollowDoppelEntity.class, EntityDataSerializers.OPTIONAL_UUID);

   public HollowDoppelEntity(EntityType<? extends PathfinderMob> type, Level level) {
      super(type, level);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.3D).add(Attributes.FOLLOW_RANGE, 16.0D);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_OWNER, Optional.empty());
   }

   public UUID getOwnerId() {
      return (UUID)((Optional)this.entityData.get(DATA_OWNER)).orElse((Object)null);
   }

   public void setOwnerId(UUID owner) {
      this.entityData.set(DATA_OWNER, Optional.ofNullable(owner));
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide() && !HollowDeepFavors.doppelKnown(this)) {
         this.discard();
      }

   }
}
