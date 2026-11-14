package com.myriam.unrp.item;

import com.myriam.unrp.UnrpMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Blocs du mod : sel, barrières d'aura, treillis de particules, machines, champ de suppression, phylactère. */
public final class ModBlocks {
    private ModBlocks() {}

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, UnrpMod.MOD_ID);

    private static BlockBehaviour.Properties base(MapColor c, float hardness, SoundType s) {
        return BlockBehaviour.Properties.of().mapColor(c).strength(hardness, hardness * 3f).sound(s);
    }

    public static final RegistryObject<Block> SALT_BLOCK = BLOCKS.register("salt_block", () -> new com.myriam.unrp.uo.SaltBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SNOW)
            .strength(-1.0f, 6.0f).noLootTable().sound(SoundType.CALCITE).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)
            .isValidSpawn((st, l, ps, e) -> false))); // SaltBlock du modèle, mêmes propriétés
    public static final RegistryObject<Block> BARRIER_BLOCK = BLOCKS.register("barrier_block", () -> new Block(base(MapColor.COLOR_LIGHT_BLUE, 2f, SoundType.GLASS).noOcclusion().lightLevel(s -> 4)));
    public static final RegistryObject<Block> BARRIER_BLOCK_AM = BLOCKS.register("barrier_block_am", () -> new Block(base(MapColor.COLOR_ORANGE, 2f, SoundType.GLASS).noOcclusion().lightLevel(s -> 4)));
    public static final RegistryObject<Block> PARTICLE_BLOCK = BLOCKS.register("particle_block", () -> new com.myriam.unrp.uo.ParticleBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_MAGENTA).strength(-1.0f, 3600000.0f).noLootTable().sound(SoundType.AMETHYST).noOcclusion()
            .lightLevel(s -> s.getValue(com.myriam.unrp.uo.ParticleBlock.GLOW) ? 7 : 0).isValidSpawn((s, l, p, e) -> false)
            .isRedstoneConductor((s, l, p) -> false).isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false))); // ParticleBlock du modèle
    public static final RegistryObject<Block> CONVERSION_MACHINE = BLOCKS.register("conversion_machine", () -> new Facing(base(MapColor.METAL, 3.5f, SoundType.METAL)));
    public static final RegistryObject<Block> RECOVERY_MACHINE = BLOCKS.register("recovery_machine", () -> new Facing(base(MapColor.METAL, 3.5f, SoundType.METAL)));
    public static final RegistryObject<Block> SUPPRESSION_FIELD = BLOCKS.register("suppression_field", () -> new Lit(base(MapColor.COLOR_BLACK, 3.5f, SoundType.METAL)));
    public static final RegistryObject<Block> PHYLACTERY = BLOCKS.register("phylactery", () -> new Phylactery.PhylBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(5f, 3600000f).sound(SoundType.AMETHYST).lightLevel(s -> 5).noLootTable()));

    /** Orientée vers le joueur à la pose (ConversionMachineBlock / RecoveryMachineBlock du modèle : FACING). */
    public static class Facing extends net.minecraft.world.level.block.HorizontalDirectionalBlock {
        public Facing(BlockBehaviour.Properties p) {
            super(p);
            registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
        }

        @Override
        protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, net.minecraft.world.level.block.state.BlockState> b) {
            b.add(FACING);
        }

        @Override
        public net.minecraft.world.level.block.state.BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext ctx) {
            return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
        }
    }

    /** SuppressionFieldBlock du modèle : LIT quand le champ est actif. */
    public static class Lit extends Block {
        public static final net.minecraft.world.level.block.state.properties.BooleanProperty LIT =
                net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;

        public Lit(BlockBehaviour.Properties p) {
            super(p);
            registerDefaultState(stateDefinition.any().setValue(LIT, false));
        }

        @Override
        protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, net.minecraft.world.level.block.state.BlockState> b) {
            b.add(LIT);
        }
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
