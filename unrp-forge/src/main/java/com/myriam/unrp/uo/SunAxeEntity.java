package com.myriam.unrp.uo;



import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class SunAxeEntity extends Entity {
   public static final double REACH = 1.2D;
   public static final int HIST = 8;
   private static final EntityDataAccessor<Float> DATA_GAUGE = SynchedEntityData.defineId(SunAxeEntity.class, EntityDataSerializers.FLOAT);
   private UUID owner;
   private ItemStack stack = ItemStack.EMPTY;
   private int slot = -1;
   private float damage;
   private Vec3 vel = Vec3.ZERO;
   private int back = -1;
   private final Set<Integer> struckOut = new HashSet();
   private final Set<Integer> struckBack = new HashSet();
   private final double[] hist = new double[32];
   private int histN;
   private boolean stale;

   public SunAxeEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void launch(ServerPlayer player, ItemStack axe, int from, Vec3 dir, float dmg, float gauge) {
      this.owner = player.getUUID();
      this.stack = axe;
      this.slot = from;
      this.damage = dmg;
      Vec3 d = dir.lengthSqr() < 1.0E-6D ? player.getLookAngle() : dir.normalize();
      this.vel = d.scale(1.7D);
      this.setDeltaMovement(this.vel);
      this.face(this.vel);
      this.entityData.set(DATA_GAUGE, gauge);
   }

   public ItemStack stack() {
      return this.stack;
   }

   public int slot() {
      return this.slot;
   }

   public float gauge() {
      return this.entityData.get(DATA_GAUGE);
   }

   public UUID owner() {
      return this.owner;
   }

   private void face(Vec3 v) {
      if (v.horizontalDistanceSqr() > 1.0E-6D) {
         this.setYRot((float)(Math.atan2(-v.x, v.z) * 180.0D / Math.PI));
      }

      this.setXRot((float)(-Math.atan2(v.y, Math.sqrt(v.horizontalDistanceSqr())) * 180.0D / Math.PI));
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_GAUGE, 1.0F);
   }

   public void tick() {
      if (this.stale) {
         this.discard();
      } else {
         super.tick();
         Level holder_0 = this.level();
         if (holder_0 instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)holder_0;
            ServerPlayer holder = this.owner == null ? null : sl.getServer().getPlayerList().getPlayer(this.owner);
            if (holder != null && holder.isAlive() && holder.level() == sl && !this.stack.isEmpty()) {
               Vec3 from = this.position();
               if (this.back < 0) {
                  Vec3 to_0 = from.add(this.vel);
                  BlockHitResult hit = sl.clip(new ClipContext(from, to_0, Block.COLLIDER, Fluid.NONE, this));
                  boolean blocked = hit.getType() == Type.BLOCK;
                  Vec3 end_0 = blocked ? hit.getLocation().subtract(this.vel.normalize().scale(0.2D)) : to_0;
                  this.sweep(sl, holder, from, end_0, this.struckOut);
                  this.setPos(end_0.x, end_0.y, end_0.z);
                  if (blocked) {
                     DaystarEvents.hurlStruck(sl, end_0);
                  }

                  if (!blocked && this.tickCount < 12) {
                     this.setDeltaMovement(this.vel);
                  } else {
                     this.turn();
                  }

               } else {
                  Vec3 target = holder.getEyePosition().subtract(0.0D, 0.4D, 0.0D);
                  Vec3 to = target.subtract(from);
                  double dist = to.length();
                  double speed = DaystarMath.hurlBack(this.back++);
                  boolean near = dist <= Math.max(1.4D, speed);
                  if (!near && this.tickCount <= 200) {
                     this.vel = to.scale(speed / dist);
                     Vec3 end = from.add(this.vel);
                     this.sweep(sl, holder, from, end, this.struckBack);
                     this.setPos(end.x, end.y, end.z);
                     this.setDeltaMovement(this.vel);
                     this.face(this.vel.scale(-1.0D));
                     if (this.tickCount % 2 == 0) {
                        sl.sendParticles(ParticleTypes.FLAME, end.x, end.y, end.z, 2, 0.2D, 0.2D, 0.2D, 0.01D);
                     }

                  } else {
                     if (near) {
                        this.sweep(sl, holder, from, target, this.struckBack);
                     }

                     DaystarEvents.hurlCaught(holder, this);
                     this.discard();
                  }
               }
            } else {
               DaystarEvents.hurlLost(holder, this);
               this.discard();
            }
         } else {
            Vec3 v = this.getDeltaMovement();
            this.setPos(this.getX() + v.x, this.getY() + v.y, this.getZ() + v.z);
            this.remember();
         }
      }
   }

   private void remember() {
      System.arraycopy(this.hist, 0, this.hist, 4, 28);
      this.hist[0] = this.getX();
      this.hist[1] = this.getY();
      this.hist[2] = this.getZ();
      this.hist[3] = (double)this.getYRot();
      this.histN = Math.min(8, this.histN + 1);
   }

   public double pastLimit() {
      return (double)Math.max(0, this.histN - 1);
   }

   public Vec3 past(double back) {
      if (this.histN == 0) {
         return this.position();
      } else {
         double b = Math.max(0.0D, Math.min(this.pastLimit(), back));
         int i = (int)Math.floor(b);
         int j = Math.min(this.histN - 1, i + 1);
         double f = b - (double)i;
         return new Vec3(Mth.lerp(f, this.hist[i * 4], this.hist[j * 4]), Mth.lerp(f, this.hist[i * 4 + 1], this.hist[j * 4 + 1]), Mth.lerp(f, this.hist[i * 4 + 2], this.hist[j * 4 + 2]));
      }
   }

   public float pastYaw(double back) {
      if (this.histN == 0) {
         return this.getYRot();
      } else {
         double b = Math.max(0.0D, Math.min(this.pastLimit(), back));
         int i = (int)Math.floor(b);
         int j = Math.min(this.histN - 1, i + 1);
         return Mth.rotLerp((float)(b - (double)i), (float)this.hist[i * 4 + 3], (float)this.hist[j * 4 + 3]);
      }
   }

   public void turn() {
      if (this.back < 0) {
         this.back = 0;
         this.vel = Vec3.ZERO;
         this.setDeltaMovement(Vec3.ZERO);
      }

   }

   private void sweep(ServerLevel sl, ServerPlayer holder, Vec3 from, Vec3 end, Set<Integer> struck) {
      AABB box = (new AABB(from, end)).inflate(2.2D);

      for(LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, box)) {
         if (e != holder && e.isAlive() && !e.isSpectator() && !struck.contains(e.getId())) {
            Vec3 mid = e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D);
            double reach = 1.2D + (double)Math.max(e.getBbWidth(), e.getBbHeight()) * 0.5D;
            Vec3 path = end.subtract(from);
            double run = path.lengthSqr();
            double t = run < 1.0E-9D ? 0.0D : Math.max(0.0D, Math.min(1.0D, mid.subtract(from).dot(path) / run));
            if (mid.distanceTo(from.add(path.scale(t))) <= reach) {
               struck.add(e.getId());
               DaystarEvents.hurlHit(sl, holder, this, e, this.damage, run < 1.0E-9D ? Vec3.ZERO : path.normalize());
            }
         }
      }

   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 16384.0D;
   }

   public boolean isPickable() {
      return false;
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.stale = true;
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }
}
