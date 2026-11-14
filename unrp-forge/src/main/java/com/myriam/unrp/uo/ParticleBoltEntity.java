package com.myriam.unrp.uo;



import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class ParticleBoltEntity extends Entity {
   public static final int ARROW = 0;
   public static final int STAKE = 1;
   private static final EntityDataAccessor<Integer> DATA_STYLE = SynchedEntityData.defineId(ParticleBoltEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(ParticleBoltEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(ParticleBoltEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private float damage = 4.0F;
   private int life = 0;
   private int maxLife = 30;

   public ParticleBoltEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void configure(UUID owner, int style, float damage, float size, Vec3 velocity, int maxLife) {
      this.entityData.set(DATA_OWNER, Optional.ofNullable(owner));
      this.entityData.set(DATA_STYLE, style);
      this.entityData.set(DATA_SIZE, size);
      this.damage = damage;
      this.maxLife = maxLife;
      if (velocity != null) {
         this.setDeltaMovement(velocity);
      }

   }

   public UUID getOwnerId() {
      return (UUID)((Optional)this.entityData.get(DATA_OWNER)).orElse((Object)null);
   }

   public int getStyle() {
      return this.entityData.get(DATA_STYLE);
   }

   public float getBoltSize() {
      return this.entityData.get(DATA_SIZE);
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_STYLE, 0);
      this.entityData.define(DATA_SIZE, 1.0F);
      this.entityData.define(DATA_OWNER, Optional.empty());
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide) {
         Vec3 v = this.getDeltaMovement();
         this.setPos(this.getX() + v.x, this.getY() + v.y, this.getZ() + v.z);
      } else {
         ++this.life;
         if (this.life > this.maxLife) {
            this.fade(false);
         } else {
            Level vel_0 = this.level();
            if (vel_0 instanceof ServerLevel) {
               ServerLevel sl = (ServerLevel)vel_0;
               if (this.life % 2 == 0) {
                  sl.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), this.getX(), this.getY(), this.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.0D);
               }

               Vec3 vel = this.getDeltaMovement();
               Vec3 from = this.position();
               Vec3 to = from.add(vel);
               double sweep = 0.25D + 0.25D * (double)this.getBoltSize();
               UUID ownerId = this.getOwnerId();
               Iterator clipFrom_0 = this.level().getEntities(this, this.getBoundingBox().expandTowards(vel).inflate(sweep), (en) -> en.isAlive() && en instanceof LivingEntity && (ownerId == null || !en.getUUID().equals(ownerId))).iterator();
               if (clipFrom_0.hasNext()) {
                  Entity e = (Entity)clipFrom_0.next();
                  ParticlesEvents.boltImpact(this, (LivingEntity)e, this.damage);
                  this.fade(true);
               } else {
                  Vec3 clipFrom = from;
                  HitResult block = null;
                  int pass = 0;

                  while(pass < 3) {
                     HitResult h = this.level().clip(new ClipContext(clipFrom, to, Block.COLLIDER, Fluid.NONE, this));
                     if (h instanceof BlockHitResult) {
                        BlockHitResult bh = (BlockHitResult)h;
                        if (h.getType() != Type.MISS && this.level().getBlockState(bh.getBlockPos()).is((net.minecraft.world.level.block.Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get())) {
                           clipFrom = bh.getLocation().add(vel.scale(0.05D));
                           ++pass;
                           continue;
                        }
                     }

                     block = h;
                     break;
                  }

                  if (block != null && block.getType() != Type.MISS) {
                     this.setPos(block.getLocation().x, block.getLocation().y, block.getLocation().z);
                     this.fade(true);
                  } else {
                     this.setPos(to.x, to.y, to.z);
                  }
               }
            }
         }
      }
   }

   private void fade(boolean impact) {
      Level var3 = this.level();
      if (var3 instanceof ServerLevel sl) {
         sl.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), this.getX(), this.getY(), this.getZ(), impact ? 10 : 4, 0.3D, 0.3D, 0.3D, 0.05D);
         sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), impact ? 6 : 2, 0.25D, 0.25D, 0.25D, 0.1D);
         if (impact) {
            sl.playSound((Player)null, this.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.7F, this.getStyle() == 1 ? 0.7F : 1.3F);
         }
      }

      this.discard();
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      tag.putFloat("PbDamage", this.damage);
      tag.putInt("PbLife", this.life);
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.damage = tag.getFloat("PbDamage");
      this.life = tag.getInt("PbLife");
   }
}
