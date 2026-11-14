package com.myriam.unrp.uo;



import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class SunCrescentEntity extends Entity implements IEntityAdditionalSpawnData {
   public static final int FADE = 8;
   public static final double THICK = 1.0D;
   public static final double ARC = 1.35D;
   public static final double RISE = 0.45D;
   public static final double BOW = 0.35D;
   private static final EntityDataAccessor<Float> DATA_SPAN = SynchedEntityData.defineId(SunCrescentEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_GAUGE = SynchedEntityData.defineId(SunCrescentEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_STOP = SynchedEntityData.defineId(SunCrescentEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_WALL = SynchedEntityData.defineId(SunCrescentEntity.class, EntityDataSerializers.BOOLEAN);
   private UUID owner;
   private float damage;
   private Vec3 vel = Vec3.ZERO;
   private final Set<Integer> struck = new HashSet();
   private int skew;
   private boolean fresh;
   private boolean stale;

   public SunCrescentEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void launch(ServerPlayer player, Vec3 dir, float span, float dmg, float gauge) {
      this.owner = player.getUUID();
      this.damage = dmg;
      Vec3 d = dir.lengthSqr() < 1.0E-6D ? player.getLookAngle() : dir.normalize();
      this.vel = d.scale(1.5D);
      this.setDeltaMovement(this.vel);
      this.setYRot(player.getYRot());
      this.setXRot(player.getXRot());
      this.entityData.set(DATA_SPAN, span);
      this.entityData.set(DATA_GAUGE, gauge);
   }

   public float span() {
      return this.entityData.get(DATA_SPAN);
   }

   public float gauge() {
      return this.entityData.get(DATA_GAUGE);
   }

   public int stop() {
      return this.entityData.get(DATA_STOP);
   }

   public double stopAge(double t) {
      return t - (double)this.skew - (double)this.stop();
   }

   public boolean wall() {
      return this.entityData.get(DATA_WALL);
   }

   public Vec3 heading() {
      return this.getViewVector(1.0F);
   }

   public Vec3 right() {
      double yaw = Math.toRadians((double)this.getYRot());
      return new Vec3(-Math.cos(yaw), 0.0D, -Math.sin(yaw));
   }

   public Vec3 up() {
      Vec3 u = this.right().cross(this.heading());
      return u.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 1.0D, 0.0D) : u.normalize();
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_SPAN, 1.8F);
      this.entityData.define(DATA_GAUGE, 1.0F);
      this.entityData.define(DATA_STOP, -1);
      this.entityData.define(DATA_WALL, false);
   }

   public void tick() {
      if (this.stale) {
         this.discard();
      } else {
         super.tick();
         int stop = this.stop();
         Level holder_0 = this.level();
         if (holder_0 instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)holder_0;
            if (stop >= 0) {
               if (this.tickCount - stop > 8) {
                  this.discard();
               }

            } else {
               ServerPlayer holder = this.owner == null ? null : sl.getServer().getPlayerList().getPlayer(this.owner);
               if (this.tickCount <= 20 && !(this.vel.lengthSqr() < 1.0E-6D)) {
                  Vec3 from = this.position();
                  Vec3 to = from.add(this.vel);
                  BlockHitResult hit = sl.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, this));
                  boolean blocked = hit.getType() == Type.BLOCK;
                  Vec3 end = blocked ? hit.getLocation() : to;
                  this.sweep(sl, holder, from, end);
                  this.setPos(end.x, end.y, end.z);
                  if (blocked) {
                     this.halt(sl, end, true);
                  } else {
                     this.setDeltaMovement(this.vel);
                     if (this.tickCount % 2 == 0) {
                        sl.sendParticles(ParticleTypes.FLAME, end.x, end.y, end.z, 3, 0.4D, 0.4D, 0.4D, 0.01D);
                     }

                  }
               } else {
                  this.halt(sl, this.position(), false);
               }
            }
         } else {
            this.fresh = false;
            if (stop < 0) {
               Vec3 v = this.getDeltaMovement();
               this.setPos(this.getX() + v.x, this.getY() + v.y, this.getZ() + v.z);
            }

         }
      }
   }

   private void sweep(ServerLevel sl, ServerPlayer holder, Vec3 from, Vec3 end) {
      Vec3 path = end.subtract(from);
      double run = path.length();
      Vec3 d = run < 1.0E-6D ? this.heading() : path.scale(1.0D / run);
      Vec3 r = this.right();
      Vec3 u = this.up();
      double span = (double)this.span();
      AABB box = (new AABB(from, end)).inflate(span + 1.5D);

      for(LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, box)) {
         if (e != holder && e.isAlive() && !e.isSpectator() && !this.struck.contains(e.getId())) {
            Vec3 mid = e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D);
            double along = Math.max(0.0D, Math.min(run, mid.subtract(from).dot(d)));
            Vec3 rel = mid.subtract(from.add(d.scale(along)));
            double half = (double)e.getBbWidth() * 0.5D;
            double tall = (double)e.getBbHeight() * 0.5D;
            double up = rel.dot(u);
            double fw = rel.dot(d);
            if (Math.abs(rel.dot(r)) <= span + half && up >= -1.0D - tall && up <= span * 0.45D + 1.0D + tall && fw <= 1.0D + half && fw >= -(1.0D + half + span * 0.35D)) {
               this.struck.add(e.getId());
               DaystarEvents.crescentHit(sl, holder, this, e, this.damage, d);
            }
         }
      }

   }

   private void halt(ServerLevel sl, Vec3 at, boolean wall) {
      this.vel = Vec3.ZERO;
      this.setDeltaMovement(Vec3.ZERO);
      this.entityData.set(DATA_WALL, wall);
      this.entityData.set(DATA_STOP, this.tickCount);
      DaystarEvents.crescentEnd(sl, at, (double)this.span(), wall);
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 16384.0D;
   }

   public boolean isPickable() {
      return false;
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (DATA_STOP.equals(key) && this.level().isClientSide && !this.fresh) {
         this.skew = this.tickCount - this.stop();
      }

   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public void writeSpawnData(FriendlyByteBuf buf) {
      buf.writeInt(this.tickCount);
   }

   public void readSpawnData(FriendlyByteBuf buf) {
      this.tickCount = buf.readInt();
      this.fresh = true;
      this.xOld = this.getX();
      this.yOld = this.getY();
      this.zOld = this.getZ();
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.stale = true;
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }
}
