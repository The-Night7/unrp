package com.myriam.unrp.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import java.util.Optional;

/** The normal structure pipeline handles references, chunk clipping and persistence. */
public final class SchoolStructure extends Structure {
    public static final Codec<SchoolStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            settingsCodec(instance), Codec.STRING.fieldOf("school").forGetter(s -> s.schoolId)
    ).apply(instance, SchoolStructure::new));
    private final String schoolId;
    public SchoolStructure(StructureSettings settings, String id) {
        super(settings); SchoolSites.school(id); schoolId = id;
    }
    @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        var school = SchoolSites.school(schoolId);
        if (!SchoolSites.isSite(context.seed(), school, context.chunkPos().x, context.chunkPos().z)) return Optional.empty();
        int x=context.chunkPos().getMiddleBlockX(), z=context.chunkPos().getMiddleBlockZ();
        int floor=context.chunkGenerator().getSeaLevel()+1;
        // Fit a level campus above the local terrain, rather than silently skipping its only site.
        for(int dx:new int[]{-36,-18,0,18,36}) for(int dz:new int[]{-36,-18,0,18,36}) {
            floor=Math.max(floor,context.chunkGenerator().getBaseHeight(x+dx,z+dz, Heightmap.Types.WORLD_SURFACE_WG,
                    context.heightAccessor(),context.randomState()));
        }
        int min=context.heightAccessor().getMinBuildHeight();
        floor=Math.min(floor,context.heightAccessor().getMaxBuildHeight()-school.floors()*6-12);
        BlockPos origin=new BlockPos(x-36,floor,z-36);
        return Optional.of(new GenerationStub(new BlockPos(x,floor,z), builder -> builder.addPiece(new SchoolPiece(schoolId,origin,min))));
    }
    @Override public StructureType<?> type() { return SchoolWorldgen.SCHOOL.get(); }
}
