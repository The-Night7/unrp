package com.myriam.unrp.uo;



import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class ParticleConstructEntity extends Entity {
   public static final int PYLON = 0;
   public static final int WALL = 1;
   public static final int TOWER = 2;
   public static final int HAND = 3;
   private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(ParticleConstructEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Float> DATA_HP = SynchedEntityData.defineId(ParticleConstructEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_MAX_HP = SynchedEntityData.defineId(ParticleConstructEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> DATA_AWAKE = SynchedEntityData.defineId(ParticleConstructEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(ParticleConstructEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Integer> DATA_BORN = SynchedEntityData.defineId(ParticleConstructEntity.class, EntityDataSerializers.INT);

   public ParticleConstructEntity(EntityType<?> type, Level level) {
      super(type, level);
   }

   public void configure(UUID owner, int variant, float maxHp) {
      this.entityData.set(DATA_OWNER, Optional.ofNullable(owner));
      this.entityData.set(DATA_VARIANT, (byte)variant);
      this.entityData.set(DATA_MAX_HP, maxHp);
      this.entityData.set(DATA_HP, maxHp);
      this.entityData.set(DATA_BORN, (int)this.level().getGameTime());
      this.refreshDimensions();
   }

   public EntityDimensions getDimensions(Pose pose) {
      int variant = this.getVariant();
      if (variant == 1) {
         return EntityDimensions.scalable(4.6F, 2.8F);
      } else if (variant == 2) {
         return EntityDimensions.scalable(1.2F, 4.2F);
      } else {
         return variant == 3 ? EntityDimensions.scalable(2.4F, 3.2F) : EntityDimensions.scalable(1.1F, 3.6F);
      }
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_VARIANT, (byte)0);
      this.entityData.define(DATA_HP, 40.0F);
      this.entityData.define(DATA_MAX_HP, 40.0F);
      this.entityData.define(DATA_AWAKE, false);
      this.entityData.define(DATA_OWNER, Optional.empty());
      this.entityData.define(DATA_BORN, 0);
   }

   public int getVariant() {
      return this.entityData.get(DATA_VARIANT);
   }

   public UUID getOwnerId() {
      return (UUID)((Optional)this.entityData.get(DATA_OWNER)).orElse((Object)null);
   }

   public int getBorn() {
      return this.entityData.get(DATA_BORN);
   }

   public float getHp() {
      return this.entityData.get(DATA_HP);
   }

   public float getMaxHp() {
      return this.entityData.get(DATA_MAX_HP);
   }

   public float hpFraction() {
      float max = this.getMaxHp();
      return max <= 0.0F ? 0.0F : this.getHp() / max;
   }

   public boolean isAwake() {
      return this.entityData.get(DATA_AWAKE);
   }

   public void mend(float amount) {
      float max = this.getMaxHp();
      float hp = this.getHp() + amount;
      this.entityData.set(DATA_HP, hp > max ? max : hp);
   }

   public void setAwake(boolean awake) {
      this.entityData.set(DATA_AWAKE, awake);
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public boolean isPickable() {
      return true;
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.level().isClientSide()) {
         return true;
      } else {
         Entity direct = source.getEntity();
         UUID owner = this.getOwnerId();
         if (owner != null && direct != null && owner.equals(direct.getUUID())) {
            ParticlesEvents.onConstructReclaimed(this);
            this.dissolve(false);
            return true;
         } else {
            float mul = this.getVariant() == 1 && this.isAwake() ? 0.5F : 1.0F;
            float hp = this.getHp() - amount * mul;
            this.entityData.set(DATA_HP, hp);
            if (this.getVariant() == 1 && this.isAwake() && direct instanceof LivingEntity) {
               LivingEntity le = (LivingEntity)direct;
               if (source.getDirectEntity() == direct) {
                  le.hurt(this.level().damageSources().thorns(this), 2.0F);
                  Level var9 = this.level();
                  if (var9 instanceof ServerLevel) {
                     ServerLevel tl = (ServerLevel)var9;
                     tl.sendParticles(ParticleTypes.CRIT, le.getX(), le.getY() + (double)le.getBbHeight() * 0.6D, le.getZ(), 8, 0.25D, 0.3D, 0.25D, 0.1D);
                  }
               }
            }

            Level var11 = this.level();
            if (var11 instanceof ServerLevel) {
               ServerLevel sl = (ServerLevel)var11;
               sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0D, this.getZ(), 4, 0.3D, 0.4D, 0.3D, 0.05D);
            }

            if (hp <= 0.0F) {
               ParticlesEvents.onConstructDestroyed(this, source);
               this.dissolve(true);
            }

            return true;
         }
      }
   }

   public void dissolve(boolean violent) {
      Level var3 = this.level();
      if (var3 instanceof ServerLevel sl) {
         sl.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), this.getX(), this.getY() + 1.0D, this.getZ(), violent ? 24 : 12, 0.4D, 0.8D, 0.4D, 0.05D);
         sl.playSound((Player)null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.8F, violent ? 0.6F : 1.4F);
      }

      this.discard();
   }

   public void onClientRemoval() {
      super.onClientRemoval();
      DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ShardGhosts.shatter(this));
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide()) {
         if (this.tickCount % 20 == 0) {
            UUID owner = this.getOwnerId();
            if (owner == null) {
               this.dissolve(false);
            } else {
               ServerPlayer p = this.getServer() == null ? null : this.getServer().getPlayerList().getPlayer(owner);
               if (p != null && p.getTags().contains("unrp.active.particles") && p.serverLevel() == this.level() && !(p.distanceToSqr(this) > 2304.0D)) {
                  ParticlesEvents.tickConstruct(this, p);
               } else {
                  this.dissolve(false);
               }
            }
         }
      }
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      tag.putByte("PcVariant", (byte)this.getVariant());
      tag.putFloat("PcHp", this.getHp());
      tag.putFloat("PcMaxHp", this.getMaxHp());
      UUID owner = this.getOwnerId();
      if (owner != null) {
         tag.putUUID("PcOwner", owner);
      }

   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.entityData.set(DATA_VARIANT, tag.getByte("PcVariant"));
      this.refreshDimensions();
      this.entityData.set(DATA_MAX_HP, tag.getFloat("PcMaxHp"));
      this.entityData.set(DATA_HP, tag.getFloat("PcHp"));
      if (tag.hasUUID("PcOwner")) {
         this.entityData.set(DATA_OWNER, Optional.of(tag.getUUID("PcOwner")));
      }

   }
}
