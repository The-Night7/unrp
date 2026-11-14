package com.myriam.unrp.uo;



import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class SaltBlock extends Block {
   public static final BooleanProperty WATER = BooleanProperty.create("water");
   public static final int SAFETY = 60;

   public SaltBlock(BlockBehaviour.Properties p) {
      super(p);
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(WATER, false));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(new Property[]{WATER});
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (SaltSeraphEvents.owned(level, pos)) {
         level.scheduleTick(pos, this, 60);
      } else {
         level.setBlock(pos, state.getValue(WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
      }
   }
}
