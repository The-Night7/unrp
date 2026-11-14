package com.myriam.unrp.uo;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class SunFissureEntity extends Entity implements IEntityAdditionalSpawnData {
   public static final int CLIMB = 2;
   public static final int DROP = 4;
   public static final int FIRST_DROP = 6;
   public static final double REACH_UP = 2.5D;
   private static final EntityDataAccessor<Float> DATA_DX = SynchedEntityData.defineId(SunFissureEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_DZ = SynchedEntityData.defineId(SunFissureEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_LEN = SynchedEntityData.defineId(SunFissureEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_GAUGE = SynchedEntityData.defineId(SunFissureEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_KIND = SynchedEntityData.defineId(SunFissureEntity.class, EntityDataSerializers.INT);
   private UUID owner;
   private float damage;
   private final Set<Integer> struck = new HashSet();
   private final Map<Long, int[]> cols = new HashMap();
   private double[][] path;
   private int[] known;
   private int[] reach;
   private int[] dug;
   private float ring = 3.5F;
   private boolean stale;

   public SunFissureEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void configure(ServerPlayer player, Vec3 dir, float length, float dmg, float gauge, int kind) {
      this.owner = player.getUUID();
      Vec3 flat = new Vec3(dir.x, 0.0D, dir.z);
      if (flat.lengthSqr() < 1.0E-6D) {
         flat = new Vec3(0.0D, 0.0D, 1.0D);
      }

      flat = flat.normalize();
      this.entityData.set(DATA_DX, (float)flat.x);
      this.entityData.set(DATA_DZ, (float)flat.z);
      this.entityData.set(DATA_LEN, length);
      this.entityData.set(DATA_GAUGE, gauge);
      this.entityData.set(DATA_KIND, kind);
      this.damage = dmg;
   }

   public Vec3 dir() {
      return new Vec3((double)((Float)this.entityData.get(DATA_DX)).floatValue(), 0.0D, (double)((Float)this.entityData.get(DATA_DZ)).floatValue());
   }

   public float length() {
      return this.entityData.get(DATA_LEN);
   }

   public float gauge() {
      return this.entityData.get(DATA_GAUGE);
   }

   public int kind() {
      return this.entityData.get(DATA_KIND);
   }

   public int rays() {
      return DaystarMath.rays(this.kind());
   }

   public double start() {
      return DaystarMath.rayStart(this.kind());
   }

   public boolean wide() {
      return this.kind() == 0;
   }

   public Vec3 ray(int i) {
      Vec3 d = this.dir();
      double a = Math.toRadians(DaystarMath.rayAngle(this.kind(), i));
      double c = Math.cos(a);
      double s = Math.sin(a);
      return new Vec3(d.x * c - d.z * s, 0.0D, d.x * s + d.z * c);
   }

   public float front(float age) {
      return (float)DaystarMath.front((double)age, (double)this.length());
   }

   public int runTicks() {
      return DaystarMath.runTicks((double)this.length());
   }

   public int life() {
      return DaystarMath.fissureLife((double)this.length());
   }

   public float ring() {
      return this.ring;
   }

   public void ring(float r) {
      this.ring = r;
   }

   public void strike(int id) {
      this.struck.add(id);
   }

   public static double walk(Level level, double x, double h, double z, int drop) {
      BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
      int bx = Mth.floor(x);
      int bz = Mth.floor(z);
      int base = Mth.floor(h + 0.01D);
      p.set(bx, base + 2, bz);
      if (!level.getBlockState(p).getCollisionShape(level, p).isEmpty()) {
         return Double.NaN;
      } else {
         for(int dy = 1; dy >= -drop; --dy) {
            p.set(bx, base + dy, bz);
            BlockState st = level.getBlockState(p);
            if (!st.getFluidState().isEmpty()) {
               return Double.NaN;
            }

            VoxelShape shape = st.getCollisionShape(level, p);
            if (!shape.isEmpty()) {
               return (double)(base + dy) + shape.max(Axis.Y);
            }
         }

         return Double.NaN;
      }
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_DX, 0.0F);
      this.entityData.define(DATA_DZ, 1.0F);
      this.entityData.define(DATA_LEN, 8.0F);
      this.entityData.define(DATA_GAUGE, 1.0F);
      this.entityData.define(DATA_KIND, 0);
   }

   private void lay(ServerLevel sl, int n, double front) {
      if (this.path == null) {
         int steps = (int)Math.ceil((double)this.length());
         this.path = new double[n][steps + 1];
         this.known = new int[n];
         this.reach = new int[n];
         this.dug = new int[n];
         Arrays.fill(this.known, -1);
         Arrays.fill(this.reach, steps);
         Arrays.fill(this.dug, -1);
      }

      Vec3 o = this.position();
      double s0 = this.start();

      for(int i = 0; i < n; ++i) {
         Vec3 d = this.ray(i);

         int k;
         for(int want = Math.min(this.path[i].length - 1, (int)Math.floor(front)); this.known[i] < want && this.known[i] < this.reach[i]; this.known[i] = k) {
            k = this.known[i] + 1;
            double h = walk(sl, o.x + d.x * (s0 + (double)k), k == 0 ? o.y : this.path[i][k - 1], o.z + d.z * (s0 + (double)k), k == 0 ? 6 : 4);
            if (Double.isNaN(h)) {
               this.reach[i] = k - 1;
               break;
            }

            this.path[i][k] = h;
         }
      }

   }

   private int far(int i) {
      return Math.min(this.known[i], this.reach[i]);
   }

   public void tick() {
      if (this.stale) {
         this.discard();
      } else {
         super.tick();
         Level age_0 = this.level();
         if (age_0 instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)age_0;
            int age = this.tickCount;
            if (age > this.life()) {
               this.discard();
            } else {
               ServerPlayer holder = this.owner == null ? null : sl.getServer().getPlayerList().getPlayer(this.owner);
               Vec3 o = this.position();
               double front = (double)this.front((float)age);
               boolean burning = front >= (double)this.length() && age < this.life() - 20;
               if (burning || !(front >= (double)this.length()) || age <= this.runTicks() + 1) {
                  int n = this.rays();
                  this.lay(sl, n, front);
                  double s0 = this.start();
                  double burnSide = this.wide() ? 1.6D : 1.0D;
                  Vec3[] dirs = new Vec3[n];
                  double x0 = o.x;
                  double x1 = o.x;
                  double y0 = o.y;
                  double y1 = o.y;
                  double z0 = o.z;
                  double z1 = o.z;

                  for(int i = 0; i < n; ++i) {
                     dirs[i] = this.ray(i);
                     int far_0 = this.far(i);

                     for(int k = 0; k <= far_0; ++k) {
                        double x = o.x + dirs[i].x * (s0 + (double)k);
                        double z = o.z + dirs[i].z * (s0 + (double)k);
                        x0 = Math.min(x0, x);
                        x1 = Math.max(x1, x);
                        z0 = Math.min(z0, z);
                        z1 = Math.max(z1, z);
                        y0 = Math.min(y0, this.path[i][k]);
                        y1 = Math.max(y1, this.path[i][k]);
                     }
                  }

                  double pad = 3.25D;
                  AABB box = (new AABB(x0, y0, z0, x1, y1, z1)).inflate(pad, 3.0D, pad);

                  for(LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, box)) {
                     if (e != holder && e.isAlive() && !e.isSpectator()) {
                        Vec3 rel = e.position().subtract(o);

                        for(int i = 0; i < n; ++i) {
                           int far = this.far(i);
                           if (far >= 0) {
                              Vec3 d = dirs[i];
                              double along = rel.x * d.x + rel.z * d.z - s0;
                              double side = Math.abs(rel.x * d.z - rel.z * d.x);
                              int k = (int)Math.round(Math.max(0.0D, Math.min((double)far, along)));
                              if (!(Math.abs(e.getY() - this.path[i][k]) > 2.5D)) {
                                 if (!this.struck.contains(e.getId()) && along >= -0.5D && along <= front + 0.6D && along <= (double)far + 0.6D && side <= 2.25D + (double)e.getBbWidth() * 0.5D) {
                                    this.struck.add(e.getId());
                                    DaystarEvents.sunderHit(sl, holder, this, e, this.damage, d);
                                    break;
                                 }

                                 if (burning && age % 20 == 0 && along >= 0.0D && along <= (double)far && side <= burnSide && e.onGround()) {
                                    DaystarEvents.crackBurn(sl, holder, this, e);
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (holder != null && this.wide() && (double)this.gauge() >= 0.5D && holder.level() == sl) {
                     Set<BlockPos> torn = new LinkedHashSet();

                     for(int i = 0; i < n; ++i) {
                        this.tear(sl, holder, i, dirs[i], s0, torn);
                     }

                     if (!torn.isEmpty()) {
                        this.dig(sl, holder, torn);
                     }
                  }

               }
            }
         }
      }
   }

   private void tear(ServerLevel sl, ServerPlayer holder, int i, Vec3 d, double s0, Set<BlockPos> torn) {
      int far = this.far(i);
      float g = this.gauge();
      Vec3 o = this.position();
      Vec3 side = new Vec3(-d.z, 0.0D, d.x);
      AABB keep = holder.getBoundingBox().inflate(0.6D, 0.0D, 0.6D);

      for(int j = this.dug[i] + 1; j <= 4 * far; ++j) {
         double at = (double)j * 0.25D;
         if (at > (double)this.length()) {
            break;
         }

         this.dug[i] = j;
         double h = this.path[i][j / 4];

         for(int off = -1; off <= 1; ++off) {
            int depth = DaystarMath.digDepth((double)g, off);
            if (depth > 0) {
               double x = o.x + d.x * (s0 + at) + side.x * (double)off;
               double z = o.z + d.z * (s0 + at) + side.z * (double)off;
               int bx = Mth.floor(x);
               int bz = Mth.floor(z);
               long key = (long)bx << 32 ^ (long)bz & 4294967295L;
               int[] had = (int[])this.cols.get(key);
               if ((had == null || had[1] < depth) && (!((double)bx + 1.0D > keep.minX) || !((double)bx < keep.maxX) || !((double)bz + 1.0D > keep.minZ) || !((double)bz < keep.maxZ))) {
                  int by;
                  if (had == null) {
                     double top = walk(sl, x, h, z, 4);
                     if (Double.isNaN(top)) {
                        continue;
                     }

                     by = Mth.ceil(top) - 1;
                  } else {
                     by = had[0];
                  }

                  for(int dd = had == null ? 0 : had[1]; dd < depth; ++dd) {
                     torn.add(new BlockPos(bx, by - dd, bz));
                  }

                  this.cols.put(key, new int[]{by, depth});
               }
            }
         }
      }

   }

   private void dig(ServerLevel sl, ServerPlayer holder, Set<BlockPos> torn) {
      List<BlockPos> spots = new ArrayList(torn);
      List<BlockState> was = new ArrayList(spots.size());

      for(BlockPos q_0 : spots) {
         was.add(sl.getBlockState(q_0));
      }

      if (NadirEvents.sunTear(sl, holder, spots) > 0) {
         for(int j = 0; j < spots.size(); ++j) {
            BlockPos q = (BlockPos)spots.get(j);
            BlockState st = (BlockState)was.get(j);
            if (!st.isAir() && sl.getBlockState(q).isAir()) {
               sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, st), (double)q.getX() + 0.5D, (double)q.getY() + 0.8D, (double)q.getZ() + 0.5D, 6, 0.3D, 0.25D, 0.3D, 0.15D);
               if (j % 3 == 0) {
                  sl.sendParticles(ParticleTypes.LAVA, (double)q.getX() + 0.5D, (double)q.getY() + 0.9D, (double)q.getZ() + 0.5D, 1, 0.2D, 0.1D, 0.2D, 0.0D);
               }
            }
         }

      }
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return distance < 25600.0D;
   }

   public boolean isPickable() {
      return false;
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public void writeSpawnData(FriendlyByteBuf buf) {
      buf.writeInt(this.tickCount);
      buf.writeFloat(this.ring);
   }

   public void readSpawnData(FriendlyByteBuf buf) {
      this.tickCount = buf.readInt();
      this.ring = buf.readFloat();
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
