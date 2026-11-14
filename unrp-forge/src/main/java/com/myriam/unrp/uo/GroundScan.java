package com.myriam.unrp.uo;



import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class GroundScan {
   private GroundScan() {
   }

   public static double below(Level level, double x, double y, double z, int rows) {
      BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
      int top = (int)Math.floor(y + 1.0D);
      int bx = (int)Math.floor(x);
      int bz = (int)Math.floor(z);

      for(int dy = 0; dy < rows; ++dy) {
         p.set(bx, top - dy, bz);
         BlockState st = level.getBlockState(p);
         VoxelShape shape = st.getCollisionShape(level, p);
         if (!shape.isEmpty()) {
            return (double)(top - dy) + shape.max(Axis.Y);
         }
      }

      return Double.NaN;
   }
}
