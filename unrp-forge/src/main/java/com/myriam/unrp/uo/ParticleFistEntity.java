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

public class ParticleFistEntity extends Entity {
   private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(ParticleFistEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> DATA_SLAM = SynchedEntityData.defineId(ParticleFistEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(ParticleFistEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private float damage = 5.0F;
   private int life = 0;
   private int maxLife = 26;

   public ParticleFistEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void configure(UUID owner, float damage, float size, boolean slam, Vec3 velocity, int maxLife) {
      this.entityData.set(DATA_OWNER, Optional.ofNullable(owner));
      this.entityData.set(DATA_SIZE, size);
      this.entityData.set(DATA_SLAM, slam);
      this.damage = damage;
      this.maxLife = maxLife;
      if (velocity != null) {
         this.setDeltaMovement(velocity);
      }

   }

   public UUID getOwnerId() {
      return (UUID)((Optional)this.entityData.get(DATA_OWNER)).orElse((Object)null);
   }

   public float getFistSize() {
      return this.entityData.get(DATA_SIZE);
   }

   public boolean isSlam() {
      return this.entityData.get(DATA_SLAM);
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_SIZE, 1.0F);
      this.entityData.define(DATA_SLAM, false);
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
            this.burst(false);
         } else {
            Level vel_0 = this.level();
            if (vel_0 instanceof ServerLevel) {
               ServerLevel sl = (ServerLevel)vel_0;
               if (this.life % 2 == 0) {
                  sl.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), this.getX(), this.getY(), this.getZ(), 2, 0.15D, 0.15D, 0.15D, 0.01D);
                  sl.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), this.getX(), this.getY(), this.getZ(), 1, 0.08D, 0.08D, 0.08D, 0.0D);
               }

               Vec3 vel = this.getDeltaMovement();
               Vec3 from = this.position();
               Vec3 to = from.add(vel);
               double sweep = 0.35D + 0.35D * (double)this.getFistSize();
               UUID ownerId = this.getOwnerId();
               Iterator clipFrom_0 = this.level().getEntities(this, this.getBoundingBox().expandTowards(vel).inflate(sweep), (en) -> en.isAlive() && en instanceof LivingEntity && (ownerId == null || !en.getUUID().equals(ownerId))).iterator();
               if (clipFrom_0.hasNext()) {
                  Entity e = (Entity)clipFrom_0.next();
                  ParticlesEvents.fistImpact(this, (LivingEntity)e, this.damage);
                  this.burst(true);
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
                     this.burst(true);
                  } else {
                     this.setPos(to.x, to.y, to.z);
                  }
               }
            }
         }
      }
   }

   private void burst(boolean impact) {
      Level var3 = this.level();
      if (var3 instanceof ServerLevel sl) {
         sl.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), this.getX(), this.getY(), this.getZ(), impact ? 18 : 8, 0.4D, 0.4D, 0.4D, 0.06D);
         sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), impact ? 10 : 4, 0.35D, 0.35D, 0.35D, 0.12D);
         if (impact) {
            sl.playSound((Player)null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, this.isSlam() ? 0.5F : 0.7F);
         }
      }

      this.discard();
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      tag.putFloat("PfDamage", this.damage);
      tag.putInt("PfLife", this.life);
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.damage = tag.getFloat("PfDamage");
      this.life = tag.getInt("PfLife");
   }
}
