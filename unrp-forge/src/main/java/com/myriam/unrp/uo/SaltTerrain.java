package com.myriam.unrp.uo;



import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class SaltTerrain {
   private SaltTerrain() {
   }

   public static boolean built(BlockState s) {
      return s.is(BlockTags.PLANKS) || s.is(BlockTags.LOGS) || s.is(BlockTags.WOODEN_STAIRS) || s.is(BlockTags.WOODEN_SLABS) || s.is(BlockTags.WOODEN_FENCES) || s.is(BlockTags.FENCE_GATES) || s.is(BlockTags.WOODEN_DOORS) || s.is(BlockTags.WOODEN_TRAPDOORS) || s.is(Blocks.COBBLESTONE) || s.is(Blocks.MOSSY_COBBLESTONE) || s.is(Blocks.COBBLESTONE_STAIRS) || s.is(Blocks.COBBLESTONE_SLAB) || s.is(Blocks.COBBLESTONE_WALL) || s.is(Blocks.BRICKS) || s.is(Blocks.BRICK_STAIRS) || s.is(Blocks.BRICK_SLAB) || s.is(Blocks.BRICK_WALL) || s.is(BlockTags.STONE_BRICKS) || s.is(net.minecraftforge.common.Tags.Blocks.GLASS) || s.is(net.minecraftforge.common.Tags.Blocks.GLASS_PANES);
   }

   static boolean solid(BlockGetter level, BlockPos pos, BlockState s) {
      return !s.isAir() && !s.getCollisionShape(level, pos).isEmpty();
   }

   public static int wallAhead(BlockGetter level, double[] feet, double[] dir) {
      double[] ahead = SaltMath.add(feet, SaltMath.mul(dir, 0.9D));
      int bx = Mth.floor(ahead[0]);
      int bz = Mth.floor(ahead[2]);
      int fy = Mth.floor(feet[1] + 0.2D);

      for(int dy = 0; dy <= 1; ++dy) {
         BlockPos pos = new BlockPos(bx, fy + dy, bz);
         BlockState s = level.getBlockState(pos);
         if (solid(level, pos, s) && (dy != 0 || !((double)fy + s.getCollisionShape(level, pos).max(Axis.Y) <= feet[1] + 0.6D))) {
            return built(s) ? 1 : 2;
         }
      }

      return 0;
   }

   static boolean wet(BlockGetter level, double x, double y, double z) {
      FluidState f = level.getFluidState(BlockPos.containing(x, y, z));
      return !f.isEmpty() && (f.is(FluidTags.WATER) || f.is(FluidTags.LAVA));
   }

   public static double groundAt(BlockGetter level, double x, double z, double fromY, int reach) {
      BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
      int bx = Mth.floor(x);
      int bz = Mth.floor(z);
      int top = Mth.floor(fromY);

      for(int by = top; by > top - reach; --by) {
         m.set(bx, by, bz);
         BlockState s = level.getBlockState(m);
         if (solid(level, m, s)) {
            double h = (double)by + s.getCollisionShape(level, m).max(Axis.Y);
            if (h <= fromY + 0.001D) {
               return h;
            }
         }

         if (!s.getFluidState().isEmpty()) {
            return (double)((float)by + s.getFluidState().getHeight(level, m));
         }
      }

      return fromY - (double)reach;
   }

   public static boolean leapAhead(BlockGetter level, double[] feet, double[] dir) {
      double[] far = SaltMath.add(feet, SaltMath.mul(dir, 1.8D));
      double g = groundAt(level, far[0], far[2], feet[1] + 0.5D, 8);
      return wet(level, far[0], g - 0.5D, far[2]) || wet(level, far[0], feet[1] - 0.5D, far[2]);
   }
}
