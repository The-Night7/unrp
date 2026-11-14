package com.myriam.unrp.uo;



import java.util.UUID;
import net.minecraft.core.Direction;
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

public class SunOrbEntity extends Entity implements IEntityAdditionalSpawnData {
   public static final int HELD = 0;
   public static final int WIND = 1;
   public static final int FLY = 2;
   public static final int BLAST = 3;
   public static final int BLAST_LIFE = 44;
   public static final int EMBER_BLAST_LIFE = 18;
   public static final int FLIGHT_MOST = 100;
   public static final int STRAY = 6;
   public static final double PROBE = 0.05D;
   public static final int MODE_SUN = 0;
   public static final int MODE_FLARE = 1;
   public static final int MODE_STAR = 2;
   public static final double STAR_LOOK = 96.0D;
   private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(SunOrbEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_CHARGE = SynchedEntityData.defineId(SunOrbEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> DATA_EMBER = SynchedEntityData.defineId(SunOrbEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(SunOrbEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_SINCE = SynchedEntityData.defineId(SunOrbEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_BLAST = SynchedEntityData.defineId(SunOrbEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_OWNER = SynchedEntityData.defineId(SunOrbEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_DROP = SynchedEntityData.defineId(SunOrbEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> DATA_DUG = SynchedEntityData.defineId(SunOrbEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_MODE = SynchedEntityData.defineId(SunOrbEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DATA_LOW = SynchedEntityData.defineId(SunOrbEntity.class, EntityDataSerializers.FLOAT);
   private UUID owner;
   private int followed;
   private int launchAt;
   private Vec3 aim = Vec3.ZERO;
   private Vec3 start = Vec3.ZERO;
   private Vec3 vel = Vec3.ZERO;
   private float damage;
   private float gauge = 1.0F;
   private int flown;
   private int skew;
   private boolean fresh;
   private boolean stale;

   public SunOrbEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void bind(ServerPlayer player, boolean ember, float size) {
      this.bind(player, ember, size, 0);
   }

   public void bind(ServerPlayer player, boolean ember, float size, int mode) {
      this.owner = player.getUUID();
      this.entityData.set(DATA_OWNER, player.getId());
      this.entityData.set(DATA_EMBER, ember);
      this.entityData.set(DATA_SIZE, size);
      this.entityData.set(DATA_MODE, mode);
      this.followed = this.tickCount;
   }

   public void grow(float size, float charge, Vec3 center) {
      this.entityData.set(DATA_SIZE, size);
      this.entityData.set(DATA_CHARGE, charge);
      this.setPos(center.x, center.y, center.z);
      this.followed = this.tickCount;
   }

   public void windUp(Vec3 aimAt, int delay, float dmg, float g) {
      this.aim = aimAt;
      this.launchAt = this.tickCount + Math.max(0, delay);
      this.damage = dmg;
      this.gauge = g;
      this.setState(1);
   }

   public void starDown(Vec3 aimAt, float low, float dmg, float g) {
      this.entityData.set(DATA_MODE, 2);
      this.entityData.set(DATA_LOW, low);
      this.windUp(aimAt, 3, dmg, g);
   }

   public float size() {
      return this.entityData.get(DATA_SIZE);
   }

   public float charge() {
      return this.entityData.get(DATA_CHARGE);
   }

   public boolean ember() {
      return this.entityData.get(DATA_EMBER);
   }

   public int state() {
      return this.entityData.get(DATA_STATE);
   }

   public int since() {
      return this.entityData.get(DATA_SINCE);
   }

   public double stateAge(double t) {
      return t - (double)this.skew - (double)this.since();
   }

   public float blast() {
      return this.entityData.get(DATA_BLAST);
   }

   public int ownerId() {
      return this.entityData.get(DATA_OWNER);
   }

   public float drop() {
      return this.entityData.get(DATA_DROP);
   }

   public boolean dug() {
      return this.entityData.get(DATA_DUG);
   }

   public UUID ownerUuid() {
      return this.owner;
   }

   public int mode() {
      return this.entityData.get(DATA_MODE);
   }

   public float low() {
      return this.entityData.get(DATA_LOW);
   }

   public double speed() {
      return this.mode() == 2 ? 1.6D : DaystarMath.flightSpeed((double)this.size(), this.ember());
   }

   public double gravity() {
      return this.mode() == 2 ? 0.08D : DaystarMath.gravity(this.ember());
   }

   public static Vec3 swoopAt(Vec3 hold, Vec3 feet, float low, double age) {
      double u = DaystarMath.swoop(age);
      return hold.add(feet.add(0.0D, (double)low, 0.0D).subtract(hold).scale(u));
   }

   private void setState(int s) {
      this.entityData.set(DATA_STATE, s);
      this.entityData.set(DATA_SINCE, this.tickCount);
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_SIZE, 1.2F);
      this.entityData.define(DATA_CHARGE, 0.0F);
      this.entityData.define(DATA_EMBER, false);
      this.entityData.define(DATA_STATE, 0);
      this.entityData.define(DATA_SINCE, 0);
      this.entityData.define(DATA_BLAST, 0.0F);
      this.entityData.define(DATA_OWNER, -1);
      this.entityData.define(DATA_DROP, -1.0F);
      this.entityData.define(DATA_DUG, false);
      this.entityData.define(DATA_MODE, 0);
      this.entityData.define(DATA_LOW, 0.0F);
   }

   public void tick() {
      if (this.stale) {
         this.discard();
      } else {
         super.tick();
         Level s = this.level();
         if (s instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)s;
            int var8 = this.state();
            ServerPlayer holder = this.owner == null ? null : sl.getServer().getPlayerList().getPlayer(this.owner);
            if (var8 != 0) {
               if (var8 == 1) {
                  if (holder != null && holder.level() == sl) {
                     int mode = this.mode();
                     Vec3 c = DaystarEvents.sunCenter(holder, (double)this.size(), mode);
                     if (mode == 2) {
                        c = swoopAt(c, holder.position(), this.low(), (double)(this.tickCount - this.since()));
                     }

                     this.setPos(c.x, c.y, c.z);
                     if (this.tickCount >= this.launchAt) {
                        if (mode == 1) {
                           this.flare(sl, holder, c);
                           return;
                        }

                        if (mode == 2 && this.slam(sl, holder, c)) {
                           return;
                        }

                        this.start = c;
                        Vec3 d = mode == 2 ? new Vec3(0.0D, -1.0D, 0.0D) : this.aim.subtract(c);
                        this.vel = d.lengthSqr() < 1.0E-6D ? holder.getLookAngle().scale(this.speed()) : d.normalize().scale(this.speed());
                        this.flown = 0;
                        this.setDeltaMovement(this.vel);
                        this.setState(2);
                     }

                  } else {
                     this.discard();
                  }
               } else if (var8 == 2) {
                  this.fly(sl, holder);
               } else {
                  int life = this.ember() ? 18 : 44;
                  if (this.tickCount - this.since() > life) {
                     this.discard();
                  }

               }
            } else {
               if (holder == null || this.tickCount - this.followed > 6) {
                  this.discard();
               }

            }
         } else {
            this.fresh = false;
            if (this.state() == 2) {
               Vec3 v = this.getDeltaMovement().add(0.0D, -this.gravity(), 0.0D);
               this.setDeltaMovement(v);
               this.setPos(this.getX() + v.x, this.getY() + v.y, this.getZ() + v.z);
            }

         }
      }
   }

   private void fly(ServerLevel sl, ServerPlayer holder) {
      double r = (double)this.size() * 0.5D;
      Vec3 from = this.position();
      this.vel = this.vel.add(0.0D, -this.gravity(), 0.0D);
      Vec3 to = from.add(this.vel);
      ++this.flown;
      boolean armed = this.mode() == 2 || to.distanceTo(this.start) > r + 1.5D;
      Vec3 drop = new Vec3(0.0D, -r * 0.6D, 0.0D);
      if (armed) {
         BlockHitResult hit = sl.clip(new ClipContext(from.add(drop), to.add(drop), Block.COLLIDER, Fluid.NONE, this));
         if (hit.getType() == Type.BLOCK) {
            Vec3 at_0 = hit.getLocation().subtract(drop);
            this.setPos(at_0.x, at_0.y, at_0.z);
            Direction face = hit.getDirection();
            double up = face == Direction.UP ? 0.05D : (face.getStepX() == 0 && face.getStepZ() == 0 ? -0.05D : 0.0D);
            this.impact(sl, holder, hit.getLocation(), hit.getLocation().add((double)face.getStepX() * 0.05D, up, (double)face.getStepZ() * 0.05D));
            return;
         }

         AABB box = (new AABB(to, to)).inflate(r + 1.5D);

         for(LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e != holder && e.isAlive() && !e.isSpectator()) {
               Vec3 mid = e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D);
               if (mid.distanceTo(to) < r + (double)e.getBbWidth() * 0.5D + 0.2D) {
                  this.setPos(to.x, to.y, to.z);
                  Vec3 at = to.subtract(0.0D, r * 0.5D, 0.0D);
                  this.impact(sl, holder, at, at);
                  return;
               }
            }
         }
      }

      this.setPos(to.x, to.y, to.z);
      this.setDeltaMovement(this.vel);
      if (this.flown > 100 || to.y < (double)(sl.getMinBuildHeight() - 8)) {
         this.impact(sl, holder, to, to);
      }

   }

   private boolean slam(ServerLevel sl, ServerPlayer holder, Vec3 c) {
      Vec3 feet = holder.position();
      double gy = DaystarEvents.groundUnder(sl, feet.add(0.0D, 0.05D, 0.0D), 96.0D);
      if (!Double.isNaN(gy) && !DaystarMath.starRoom((double)this.size(), gy - feet.y)) {
         Vec3 at = new Vec3(c.x, gy, c.z);
         this.impact(sl, holder, at, at.add(0.0D, 0.05D, 0.0D));
         return true;
      } else {
         return false;
      }
   }

   private void flare(ServerLevel sl, ServerPlayer holder, Vec3 at) {
      float reach = (float)DaystarMath.flareReach((double)this.size(), this.ember());
      this.entityData.set(DATA_BLAST, reach);
      this.setPos(at.x, at.y, at.z);
      this.setDeltaMovement(Vec3.ZERO);
      double gy = DaystarEvents.groundUnder(sl, holder.position().add(0.0D, 0.05D, 0.0D), 2.95D);
      this.entityData.set(DATA_DROP, Double.isNaN(gy) ? -1.0F : (float)Math.max(0.0D, at.y - gy));
      this.setState(3);
      DaystarEvents.flareBurst(sl, holder, this, at, Double.isNaN(gy) ? null : new Vec3(at.x, gy, at.z), this.size(), this.ember(), this.damage);
   }

   private void impact(ServerLevel sl, ServerPlayer holder, Vec3 at, Vec3 probe) {
      float radius = this.mode() == 2 ? (float)DaystarMath.starBlast((double)this.size(), this.ember()) : DaystarEvents.blastRadius(this.size(), this.ember());
      this.entityData.set(DATA_BLAST, radius);
      this.setPos(at.x, at.y, at.z);
      this.setDeltaMovement(Vec3.ZERO);
      double gy = DaystarEvents.groundUnder(sl, probe, (double)radius + 1.0D);
      boolean ground = !Double.isNaN(gy);
      this.entityData.set(DATA_DROP, ground ? (float)Math.max(0.0D, at.y - gy) : -1.0F);
      this.setState(3);
      boolean dug = DaystarEvents.sunImpact(sl, holder, this, at, ground ? new Vec3(probe.x, gy, probe.z) : null, this.size(), this.charge(), this.ember(), this.damage, this.gauge);
      this.entityData.set(DATA_DUG, dug);
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 65536.0D;
   }

   public boolean isPickable() {
      return false;
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
      super.onSyncedDataUpdated(key);
      if (DATA_SINCE.equals(key) && this.level().isClientSide && !this.fresh) {
         this.skew = this.tickCount - this.since();
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
