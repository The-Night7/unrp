package com.myriam.unrp.uo;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** ModBlockEntities du modèle : la trame des blocs de Particles. */
public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "unrp");
    public static final RegistryObject<BlockEntityType<LatticeBlockEntity>> LATTICE = BLOCK_ENTITIES.register("lattice",
            () -> BlockEntityType.Builder.of(LatticeBlockEntity::new, com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get()).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
