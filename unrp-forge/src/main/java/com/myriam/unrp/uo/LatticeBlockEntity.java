package com.myriam.unrp.uo;



import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class LatticeBlockEntity extends BlockEntity {
   public static final int END_GENTLE = 0;
   public static final int END_HOME = 1;
   public static final int END_BURST = 2;
   private UUID owner;
   private long born;
   private long fade;
   private int end;
   private boolean unloading;
   public long seenAt = Long.MIN_VALUE;
   public long firstAt = Long.MIN_VALUE;
   public long rimAt = Long.MIN_VALUE;
   public int rimMask;
   public float[] specks;

   public LatticeBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.LATTICE.get(), pos, state);
   }

   public UUID getOwner() {
      return this.owner;
   }

   public long getBorn() {
      return this.born;
   }

   public long getFade() {
      return this.fade;
   }

   public int getEnd() {
      return this.end;
   }

   public void setOwner(UUID id) {
      this.owner = id;
      this.sync();
   }

   public void weave(UUID id, long when) {
      this.owner = id;
      this.born = when;
      this.fade = 0L;
      this.end = 0;
      this.sync();
   }

   public void setFade(long when) {
      if (this.fade != when) {
         this.fade = when;
         this.sync();
      }
   }

   public void setEnd(int kind) {
      if (this.end != kind) {
         this.end = kind;
         this.sync();
      }
   }

   private void sync() {
      this.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }

   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
      this.born = tag.getLong("Born");
      this.fade = tag.getLong("Fade");
      this.end = tag.getInt("End");
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      if (this.owner != null) {
         tag.putUUID("Owner", this.owner);
      }

      tag.putLong("Born", this.born);
      tag.putLong("Fade", this.fade);
      tag.putInt("End", this.end);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void onChunkUnloaded() {
      this.unloading = true;
      super.onChunkUnloaded();
   }

   public void setRemoved() {
      super.setRemoved();
      if (!this.unloading && this.level != null && this.level.isClientSide) {
         int cx = SectionPos.blockToSectionCoord(this.worldPosition.getX());
         int cz = SectionPos.blockToSectionCoord(this.worldPosition.getZ());
         if (this.level.hasChunk(cx, cz)) {
            BlockPos at = this.worldPosition;
            BlockState st = this.getBlockState();
            UUID who = this.owner;
            int kind = this.end;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LatticeFx.dissolve(at, st, who, kind));
         }
      }
   }
}
