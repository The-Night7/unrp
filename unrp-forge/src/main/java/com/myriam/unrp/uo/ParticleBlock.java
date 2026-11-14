package com.myriam.unrp.uo;



import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ParticleBlock extends Block implements EntityBlock {
   public static final EnumProperty<ParticleBlock.Orientation> ORIENTATION = EnumProperty.create("orientation", ParticleBlock.Orientation.class);
   public static final IntegerProperty LAYER = IntegerProperty.create("layer", 0, 3);
   public static final BooleanProperty GLOW = BooleanProperty.create("glow");
   private static final VoxelShape FILM = Block.box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape[] FILMS = new VoxelShape[]{Block.box(0.0D, 1.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.box(0.0D, 5.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.box(0.0D, 9.0D, 0.0D, 16.0D, 12.0D, 16.0D), FILM};
   private static final VoxelShape PANE_NS = Block.box(0.0D, 0.0D, 6.5D, 16.0D, 16.0D, 9.5D);
   private static final VoxelShape PANE_EW = Block.box(6.5D, 0.0D, 0.0D, 9.5D, 16.0D, 16.0D);

   public ParticleBlock(BlockBehaviour.Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(GLOW, Boolean.FALSE)).setValue(ORIENTATION, ParticleBlock.Orientation.FLOOR)).setValue(LAYER, 3));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ORIENTATION, LAYER});
      builder.add(new Property[]{GLOW});
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new LatticeBlockEntity(pos, state);
   }

   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return !level.isClientSide && type == ModBlockEntities.LATTICE.get() ? (lvl, pos, st, be) -> {
         if ((lvl.getGameTime() + (pos.asLong() & 31L)) % 40L == 0L) {
            if (!ParticlesEvents.woven(lvl, pos)) {
               lvl.removeBlock(pos, false);
            }

         }
      } : null;
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.INVISIBLE;
   }

   private static VoxelShape shapeFor(BlockState state) {
      ParticleBlock.Orientation o = (ParticleBlock.Orientation)state.getValue(ORIENTATION);
      if (o == ParticleBlock.Orientation.WALL_NS) {
         return PANE_NS;
      } else {
         return o == ParticleBlock.Orientation.WALL_EW ? PANE_EW : FILMS[state.getValue(LAYER)];
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
      return shapeFor(state);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
      if (context instanceof EntityCollisionContext ecc) {
         Entity e = ecc.getEntity();
         if (e != null && e.getTags().contains("unrp.pmarked")) {
            return Shapes.empty();
         }
      }

      return shapeFor(state);
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      ParticleBlock.Orientation o = (ParticleBlock.Orientation)state.getValue(ORIENTATION);

      for(int i = 0; i < 2; ++i) {
         double x;
         double y;
         double z;
         if (o == ParticleBlock.Orientation.WALL_NS) {
            x = (double)pos.getX() + random.nextDouble();
            y = (double)pos.getY() + random.nextDouble();
            z = (double)pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D;
         } else if (o == ParticleBlock.Orientation.WALL_EW) {
            x = (double)pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D;
            y = (double)pos.getY() + random.nextDouble();
            z = (double)pos.getZ() + random.nextDouble();
         } else {
            double top = (double)(state.getValue(LAYER) + 1) * 0.25D;
            x = (double)pos.getX() + random.nextDouble();
            y = (double)pos.getY() + top - 0.12D + random.nextDouble() * 0.24D;
            z = (double)pos.getZ() + random.nextDouble();
         }

         level.addParticle(com.myriam.unrp.deal.PowerParticles.MOTE.get(), x, y, z, (random.nextDouble() - 0.5D) * 0.01D, 0.0D, (random.nextDouble() - 0.5D) * 0.01D);
      }

   }

   public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
      super.stepOn(level, pos, state, entity);
      if (level instanceof ServerLevel sl) {
         if (entity.tickCount % 5 == 0) {
            sl.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), entity.getX(), (double)pos.getY() + 1.02D, entity.getZ(), 3, 0.18D, 0.02D, 0.18D, 0.005D);
         }
      }

   }

   public static enum Orientation implements StringRepresentable {
      FLOOR("floor"),
      WALL_NS("wall_ns"),
      WALL_EW("wall_ew");

      private final String key;

      private Orientation(String key) {
         this.key = key;
      }

      public String getSerializedName() {
         return this.key;
      }

      // $FF: synthetic method
      private static ParticleBlock.Orientation[] $values() {
         return new ParticleBlock.Orientation[]{FLOOR, WALL_NS, WALL_EW};
      }
   }
}
