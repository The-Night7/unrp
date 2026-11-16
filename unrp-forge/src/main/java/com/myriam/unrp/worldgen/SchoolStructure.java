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
        var plan=SchoolBlueprint.create(school);
        int half=plan.size()/2;
        // A robust grade avoids lifting the entire campus onto the highest hill sample.
        int[] samples=new int[81];int sample=0;
        for(int ix=0;ix<9;ix++)for(int iz=0;iz<9;iz++) {
            int sx=x-half+ix*(plan.size()-1)/8,sz=z-half+iz*(plan.size()-1)/8;
            samples[sample++]=context.chunkGenerator().getBaseHeight(sx,sz,Heightmap.Types.WORLD_SURFACE_WG,context.heightAccessor(),context.randomState());
        }
        int min=context.heightAccessor().getMinBuildHeight();
        int floor=SchoolTerrain.floor(context.chunkGenerator().getSeaLevel(),samples,min+2,context.heightAccessor().getMaxBuildHeight()-plan.height()-2);
        BlockPos origin=new BlockPos(x-half,floor,z-half);
        return Optional.of(new GenerationStub(new BlockPos(x,floor,z), builder -> builder.addPiece(new SchoolPiece(schoolId,origin,min))));
    }
    @Override public StructureType<?> type() { return SchoolWorldgen.SCHOOL.get(); }
}
