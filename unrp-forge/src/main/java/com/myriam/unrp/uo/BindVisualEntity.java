package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;


import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BindVisualEntity extends Entity {
   public static final int MODE_BUBBLE = 0;
   public static final int MODE_ICE = 1;
   public static final int MODE_SAND = 2;
   private static final EntityDataAccessor<Byte> DATA_MODE = SynchedEntityData.defineId(BindVisualEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Integer> DATA_VICTIM = SynchedEntityData.defineId(BindVisualEntity.class, EntityDataSerializers.INT);
   private UUID victimUuid;
   private int life = 0;

   public BindVisualEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
   }

   public void bindTo(LivingEntity victim, int mode) {
      this.victimUuid = victim.getUUID();
      this.entityData.set(DATA_MODE, (byte)mode);
      this.entityData.set(DATA_VICTIM, victim.getId());
      this.moveToVictim(victim);
   }

   public int getMode() {
      return this.entityData.get(DATA_MODE);
   }

   public int getVictimId() {
      return this.entityData.get(DATA_VICTIM);
   }

   private void moveToVictim(LivingEntity victim) {
      if (this.getMode() == 0) {
         this.setPos(victim.getX(), victim.getY() + (double)victim.getBbHeight() * 0.75D, victim.getZ());
      } else {
         this.setPos(victim.getX(), victim.getY(), victim.getZ());
      }

   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_MODE, (byte)0);
      this.entityData.define(DATA_VICTIM, -1);
   }

   public void tick() {
      super.tick();
      Level e_0 = this.level();
      if (e_0 instanceof ServerLevel sl) {
         ++this.life;
         if (this.life <= 600 && this.victimUuid != null) {
            Entity e = sl.getEntity(this.victimUuid);
            if (e instanceof LivingEntity) {
               LivingEntity victim = (LivingEntity)e;
               if (victim.isAlive()) {
                  boolean claimed = this.getMode() == 2 && SarcophageEvents.isCoffined(this.victimUuid); // modes 0/1 : Hydrofreeze du modèle, qui chez nous ne pose pas cette entité
                  if (!claimed) {
                     this.endFx(sl, victim);
                     this.discard();
                     return;
                  }

                  if (this.entityData.get(DATA_VICTIM) != victim.getId()) {
                     this.entityData.set(DATA_VICTIM, victim.getId());
                  }

                  this.moveToVictim(victim);
                  return;
               }
            }

            if (e instanceof LivingEntity) {
               LivingEntity le = (LivingEntity)e;
               this.endFx(sl, le);
            }

            this.discard();
         } else {
            this.discard();
         }
      }
   }

   private void endFx(ServerLevel sl, LivingEntity victim) {
      Vec3 at = victim.position().add(0.0D, (double)victim.getBbHeight() * 0.6D, 0.0D);
      if (this.getMode() == 0) {
         sl.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:entity.fishing_bobber.splash"), SoundSource.PLAYERS, 0.8F, 1.3F);
         Fx.shell(sl, Fx.drop(6272767), at, 0.4D, 14, 0.2D);
      } else if (this.getMode() == 2) {
         sl.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.sand.break"), SoundSource.PLAYERS, 1.0F, 0.5F);
         Fx.shell(sl, Fx.drop(14594656), at, 0.6D, 24, 0.22D);
      } else {
         sl.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.glass.break"), SoundSource.PLAYERS, 0.8F, 0.9F);
         Fx.shell(sl, Fx.shard(14087423), at, 0.5D, 20, 0.25D);
      }

   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      if (tag.hasUUID("unrp_victim")) {
         this.victimUuid = tag.getUUID("unrp_victim");
      }

      this.entityData.set(DATA_MODE, tag.getByte("unrp_mode"));
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      if (this.victimUuid != null) {
         tag.putUUID("unrp_victim", this.victimUuid);
      }

      tag.putByte("unrp_mode", (byte)this.getMode());
   }

   public boolean isPickable() {
      return false;
   }

   public boolean isPushable() {
      return false;
   }
}
