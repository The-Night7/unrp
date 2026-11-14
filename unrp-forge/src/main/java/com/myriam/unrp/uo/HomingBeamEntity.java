package com.myriam.unrp.uo;



import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.Vector3f;

public class HomingBeamEntity extends Entity {
   private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(HomingBeamEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_WAVE = SynchedEntityData.defineId(HomingBeamEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Float> DATA_WIDTH = SynchedEntityData.defineId(HomingBeamEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> DATA_SPENT = SynchedEntityData.defineId(HomingBeamEntity.class, EntityDataSerializers.BOOLEAN);
   public static final float RETRACT = 6.0F;
   public static final int RETRACT_STEP = 3;
   public final ArrayDeque<Vec3> clientTrail = new ArrayDeque();
   private UUID owner;
   private int targetId = -1;
   private double speed = 1.5D;
   private double turnRate = 0.1D;
   private float damage = 8.0F;
   private int life = 120;
   private int pierceBlocks = 0;
   private double leadFactor = 1.0D;
   private float turnPenalty = 0.0F;
   private boolean waveMode = false;
   private int carveBudget = 0;
   private int spentTicks = -1;
   private int spentSeen = -1;
   private final HashSet<UUID> waveStruck = new HashSet();

   public void waveMode() {
      this.waveMode = true;
      this.entityData.set(DATA_WAVE, true);
   }

   public void carve(int budget) {
      this.carveBudget = budget;
   }

   public HomingBeamEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void configure(ServerPlayer owner, int targetId, Vec3 dir, double speed, double turnRate, float damage, int life, float width, int pierceBlocks, double leadFactor, int color) {
      this.owner = owner.getUUID();
      this.targetId = targetId;
      this.speed = speed;
      this.turnRate = turnRate;
      this.damage = damage;
      this.life = life;
      this.pierceBlocks = pierceBlocks;
      this.leadFactor = leadFactor;
      this.entityData.set(DATA_COLOR, color);
      this.entityData.set(DATA_WIDTH, width);
      this.setDeltaMovement(dir.normalize().scale(speed));
   }

   public int getColor() {
      return this.entityData.get(DATA_COLOR);
   }

   public boolean isWaveVisual() {
      return this.entityData.get(DATA_WAVE);
   }

   public float getBeamWidth() {
      return this.entityData.get(DATA_WIDTH);
   }

   public boolean isSpent() {
      return this.entityData.get(DATA_SPENT);
   }

   public float spentFor(float partialTick) {
      return this.spentSeen < 0 ? 0.0F : (float)(this.tickCount - this.spentSeen) + partialTick;
   }

   private void spend(Vec3 at) {
      if (this.spentTicks < 0) {
         this.spentTicks = 0;
         if (at != null) {
            this.setPos(at.x, at.y, at.z);
         }

         this.setDeltaMovement(Vec3.ZERO);
         this.entityData.set(DATA_SPENT, true);
      }
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_COLOR, 3806292);
      this.entityData.define(DATA_WIDTH, 0.2F);
      this.entityData.define(DATA_WAVE, false);
      this.entityData.define(DATA_SPENT, false);
   }

   public void tick() {
      super.tick();
      if (this.level().isClientSide()) {
         if (this.isSpent()) {
            if (this.spentSeen < 0) {
               this.spentSeen = this.tickCount;
            }

            for(int k = 0; k < 3 && this.clientTrail.size() > 1; ++k) {
               this.clientTrail.removeLast();
            }

         } else {
            this.clientTrail.addFirst(this.position());

            while(this.clientTrail.size() > 16) {
               this.clientTrail.removeLast();
            }

            Vec3 v = this.getDeltaMovement();
            this.setPos(this.getX() + v.x, this.getY() + v.y, this.getZ() + v.z);
         }
      } else if (this.spentTicks >= 0) {
         if ((float)(++this.spentTicks) > 6.0F) {
            this.discard();
         }

      } else if (--this.life <= 0) {
         this.spend((Vec3)null);
      } else {
         Vec3 dir = this.getDeltaMovement().normalize();
         Entity t = this.targetId >= 0 ? this.level().getEntity(this.targetId) : null;
         if (t instanceof LivingEntity) {
            LivingEntity le_0 = (LivingEntity)t;
            if (le_0.isAlive()) {
               Vec3 tp = le_0.getBoundingBox().getCenter();
               double dist = tp.distanceTo(this.position());
               Vec3 lead = le_0.getDeltaMovement().scale(Math.min(20.0D, dist / this.speed) * this.leadFactor);
               Vec3 desired = tp.add(lead.x, Math.max(-0.5D, Math.min(0.5D, lead.y)), lead.z).subtract(this.position()).normalize();
               double angle = Math.acos(Math.max(-1.0D, Math.min(1.0D, dir.dot(desired))));
               if (angle > 1.0E-4D) {
                  double frac = Math.min(1.0D, this.turnRate / angle);
                  dir = dir.scale(1.0D - frac).add(desired.scale(frac)).normalize();
                  if (angle > 0.06D) {
                     this.turnPenalty += (float)(angle - 0.06D) * 2.0F;
                     Level var14 = this.level();
                     if (var14 instanceof ServerLevel) {
                        ServerLevel sl_0 = (ServerLevel)var14;
                        sl_0.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 2, 0.05D, 0.05D, 0.05D, 0.01D);
                     }
                  }
               }
            }
         }

         this.setDeltaMovement(dir.scale(this.speed));
         Vec3 next = this.position().add(this.getDeltaMovement());
         BlockHitResult blockHit = this.level().clip(new ClipContext(this.position(), next, Block.COLLIDER, Fluid.NONE, this));
         if (blockHit.getType() != Type.MISS) {
            if (this.waveMode && this.carveBudget > 0 && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.carveThrough(blockHit.getLocation())) {
               next = blockHit.getLocation().add(this.getDeltaMovement().normalize().scale(0.9D));
            } else {
               if (this.pierceBlocks <= 0) {
                  this.impact(blockHit.getLocation());
                  return;
               }

               --this.pierceBlocks;
               next = blockHit.getLocation().add(this.getDeltaMovement().normalize().scale(1.1D));
            }
         }

         AABB path = (new AABB(this.position(), next)).inflate(0.35D);
         LivingEntity hit = null;
         double best = Double.MAX_VALUE;

         for(LivingEntity le : this.level().getEntitiesOfClass(LivingEntity.class, path, (e) -> e.isAlive() && !e.getUUID().equals(this.owner))) {
            double d = le.getBoundingBox().getCenter().distanceToSqr(this.position());
            if (d < best) {
               best = d;
               hit = le;
            }
         }

         if (hit != null && !this.waveMode) {
            float dealt = Math.max(this.damage * 0.45F, this.damage - this.turnPenalty);
            ServerPlayer op = this.owner == null ? null : (this.level().getServer() == null ? null : this.level().getServer().getPlayerList().getPlayer(this.owner));
            hit.hurt(op != null ? this.level().damageSources().indirectMagic(this, op) : this.level().damageSources().magic(), dealt);
            hit.setDeltaMovement(hit.getDeltaMovement().add(this.getDeltaMovement().normalize().scale(0.25D)));
            hit.hurtMarked = true;
            this.impact(hit.getBoundingBox().getCenter());
         } else {
            if (this.waveMode) {
               AABB sweep = (new AABB(this.position(), next)).inflate((double)this.getBeamWidth() * 0.9D);

               for(LivingEntity le : this.level().getEntitiesOfClass(LivingEntity.class, sweep, (e) -> e.isAlive() && !e.getUUID().equals(this.owner) && !this.waveStruck.contains(e.getUUID()))) {
                  this.waveStruck.add(le.getUUID());
                  float want = this.damage;
                  if (le instanceof Player) {
                     float head = le.getHealth() - le.getMaxHealth() * 0.25F;
                     if (head > 0.0F) {
                        want = Math.min(want, Math.max(0.0F, le.getHealth() - 1.0F));
                     }
                  }

                  OriginMasteryEvents.hurtBy(this.owner, le, this.level().damageSources().magic(), want);
                  Vec3 shove = this.getDeltaMovement().normalize();
                  le.setDeltaMovement(le.getDeltaMovement().add(shove.x * 1.4D, 0.32D, shove.z * 1.4D));
                  le.hurtMarked = true;
               }

               Level var29 = this.level();
               if (var29 instanceof ServerLevel) {
                  ServerLevel sl = (ServerLevel)var29;
                  int c = this.getColor();
                  Vector3f tint = new Vector3f((float)(c >> 16 & 255) / 255.0F, (float)(c >> 8 & 255) / 255.0F, (float)(c & 255) / 255.0F);
                  sl.sendParticles(new DustParticleOptions(tint, 2.2F), this.getX(), this.getY(), this.getZ(), 10, (double)this.getBeamWidth() * 0.5D, (double)this.getBeamWidth() * 0.5D, (double)this.getBeamWidth() * 0.5D, 0.02D);
                  sl.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.04D);
               }
            }

            this.setPos(next.x, next.y, next.z);
         }
      }
   }

   private boolean carveThrough(Vec3 at) {
      Level r = this.level();
      if (!(r instanceof ServerLevel sl)) {
         return false;
      } else {
         double var12 = Math.max(1.2D, (double)this.getBeamWidth() * 0.95D);
         BlockPos center = BlockPos.containing(at);
         int span = (int)Math.ceil(var12);
         int broke = 0;
         boolean blockedCore = false;

         for(BlockPos p : BlockPos.betweenClosed(center.offset(-span, -span, -span), center.offset(span, span, span))) {
            if (broke >= 40 || this.carveBudget - broke <= 0) {
               break;
            }

            if (p.closerToCenterThan(at, var12)) {
               BlockState state = sl.getBlockState(p);
               if (!state.isAir()) {
                  if (!(state.getBlock().getExplosionResistance() > 9.0F) && !(state.getDestroySpeed(sl, p) < 0.0F)) {
                     sl.destroyBlock(p, false);
                     ++broke;
                  } else if (p.equals(center)) {
                     blockedCore = true;
                  }
               }
            }
         }

         if (broke > 0) {
            this.carveBudget -= broke;
            sl.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 2, var12 * 0.4D, var12 * 0.4D, var12 * 0.4D, 0.0D);
            sl.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 10, var12 * 0.6D, var12 * 0.6D, var12 * 0.6D, 0.15D);
            sl.playSound((Player)null, center, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.8F, 1.5F);
         }

         return broke > 0 && !blockedCore;
      }
   }

   private void impact(Vec3 at) {
      Level var3 = this.level();
      if (var3 instanceof ServerLevel sl) {
         sl.sendParticles(ParticleTypes.FIREWORK, at.x, at.y, at.z, 12, 0.2D, 0.2D, 0.2D, 0.08D);
      }

      this.spend(at);
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.life = Math.min(this.life, 20);
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 9216.0D;
   }
}
