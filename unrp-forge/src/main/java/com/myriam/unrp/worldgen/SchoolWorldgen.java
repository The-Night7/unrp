package com.myriam.unrp.worldgen;

import com.myriam.unrp.UnrpMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class SchoolWorldgen {
    private static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, UnrpMod.MOD_ID);
    private static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, UnrpMod.MOD_ID);
    public static final RegistryObject<StructureType<SchoolStructure>> SCHOOL = TYPES.register("school", () -> () -> SchoolStructure.CODEC);
    public static final RegistryObject<StructurePieceType> CAMPUS = PIECES.register("school_campus", () -> SchoolPiece::new);
    private SchoolWorldgen() {}
    public static void register(IEventBus bus) { TYPES.register(bus); PIECES.register(bus); }
}
