package com.myriam.unrp.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SchoolPiece extends StructurePiece {
    private static final Map<String, SchoolBlueprint.Plan> PLANS = new ConcurrentHashMap<>();
    private final String schoolId;
    private final BlockPos origin;
    SchoolPiece(String id, BlockPos origin, int minY) {
        super(SchoolWorldgen.CAMPUS.get(),0,new BoundingBox(origin.getX(),minY,origin.getZ(),
                origin.getX()+72,origin.getY()+SchoolSites.school(id).floors()*6+10,origin.getZ()+72));
        schoolId=id; this.origin=origin.immutable();
    }
    SchoolPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(SchoolWorldgen.CAMPUS.get(),tag);
        schoolId=tag.getString("School"); SchoolSites.school(schoolId);
        origin=BlockPos.of(tag.getLong("Origin"));
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("School",schoolId); tag.putLong("Origin",origin.asLong());
    }
    @Override public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
                                     RandomSource random, BoundingBox clip, ChunkPos chunk, BlockPos pivot) {
        if (!level.getLevel().dimension().equals(Level.OVERWORLD)) return;
        var school=SchoolSites.school(schoolId);
        var plan=PLANS.computeIfAbsent(schoolId,id -> SchoolBlueprint.create(SchoolSites.school(id)));
        int minX=Math.max(origin.getX(),clip.minX()), maxX=Math.min(origin.getX()+72,clip.maxX());
        int minZ=Math.max(origin.getZ(),clip.minZ()), maxZ=Math.min(origin.getZ()+72,clip.maxZ());
        BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
        // Solid foundations extend through air/water to the terrain. Never write into a neighboring chunk.
        for(int x=minX;x<=maxX;x++) for(int z=minZ;z<=maxZ;z++) {
            for(int y=origin.getY()-2;y>level.getMinBuildHeight();y--) {
                pos.set(x,y,z);
                BlockState old=level.getBlockState(pos);
                if(!old.isAir() && old.getFluidState().isEmpty() && !old.is(BlockTagsAdapter.LEAVES)) break;
                level.setBlock(pos,Blocks.STONE_BRICKS.defaultBlockState(),2);
            }
        }
        var palette=new java.util.EnumMap<SchoolBlueprint.Material,BlockState>(SchoolBlueprint.Material.class);
        for(var material:SchoolBlueprint.Material.values()) palette.put(material,state(material,school));
        for(var box:plan.boxes()) {
            int x1=Math.max(origin.getX()+box.x1(),minX), x2=Math.min(origin.getX()+box.x2(),maxX);
            int z1=Math.max(origin.getZ()+box.z1(),minZ), z2=Math.min(origin.getZ()+box.z2(),maxZ);
            int y1=Math.max(origin.getY()+box.y1(),Math.max(clip.minY(),level.getMinBuildHeight()));
            int y2=Math.min(origin.getY()+box.y2(),Math.min(clip.maxY(),level.getMaxBuildHeight()-1));
            BlockState state=palette.get(box.material());
            for(int x=x1;x<=x2;x++) for(int z=z1;z<=z2;z++) for(int y=y1;y<=y2;y++) {
                pos.set(x,y,z); level.setBlock(pos,state,2);
            }
        }
        for(var label:plan.labels()) {
            BlockPos at=origin.offset(label.x(),label.y(),label.z());
            if(!clip.isInside(at)) continue;
            if(level.getBlockEntity(at) instanceof SignBlockEntity sign) {
                var text=sign.getFrontText();
                String[] words=label.text().split(" ");
                StringBuilder line=new StringBuilder(); int row=0;
                for(String word:words) {
                    if(line.length()+word.length()+1>16 && row<3) { text=text.setMessage(row++,Component.literal(line.toString())); line.setLength(0); }
                    if(!line.isEmpty()) line.append(' '); line.append(word);
                }
                text=text.setMessage(row,Component.literal(line.toString()));
                sign.setText(text,true); sign.setText(text,false); sign.setWaxed(true); sign.setChanged();
            }
        }
    }
    private static BlockState state(SchoolBlueprint.Material material, SchoolSites.School school) {
        Block accent=switch(school.palette()) {
            case "blue" -> Blocks.BLUE_TERRACOTTA; case "green" -> Blocks.GREEN_TERRACOTTA;
            case "red" -> Blocks.RED_TERRACOTTA; case "cyan" -> Blocks.CYAN_TERRACOTTA;
            case "purple" -> Blocks.PURPLE_TERRACOTTA; default -> Blocks.YELLOW_TERRACOTTA;
        };
        return switch(material) {
            case AIR -> Blocks.AIR.defaultBlockState();
            case FOUNDATION -> Blocks.STONE_BRICKS.defaultBlockState();
            case PAVING -> Blocks.SMOOTH_STONE.defaultBlockState();
            case GRASS -> Blocks.GRASS_BLOCK.defaultBlockState();
            case WALL -> (school.palette().equals("red")?Blocks.STONE_BRICKS:school.palette().equals("green")?Blocks.BRICKS:Blocks.SMOOTH_QUARTZ).defaultBlockState();
            case FLOOR -> Blocks.BIRCH_PLANKS.defaultBlockState();
            case ROOF -> (school.palette().equals("green")?Blocks.DARK_OAK_PLANKS:Blocks.DEEPSLATE_TILES).defaultBlockState();
            case ACCENT -> accent.defaultBlockState();
            case GLASS -> Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
            case FENCE -> Blocks.IRON_BARS.defaultBlockState();
            case LEAVES -> Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
            case LOG -> Blocks.OAK_LOG.defaultBlockState();
            case LIGHT -> Blocks.SEA_LANTERN.defaultBlockState();
            case WATER -> Blocks.WATER.defaultBlockState();
            case WHITE -> Blocks.WHITE_CONCRETE.defaultBlockState();
            case COURT -> Blocks.ORANGE_TERRACOTTA.defaultBlockState();
            case BOOKS -> Blocks.BOOKSHELF.defaultBlockState();
            case DESK -> Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.TOP);
            case CHAIR -> Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);
            case STAIRS -> Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH);
            case DOOR_LOWER -> Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER);
            case DOOR_UPPER -> Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER);
            case BED_HEAD -> Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.PART,BedPart.HEAD).setValue(BedBlock.FACING,Direction.SOUTH);
            case BED_FOOT -> Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.PART,BedPart.FOOT).setValue(BedBlock.FACING,Direction.SOUTH);
            case COUNTER -> Blocks.SMOOTH_STONE.defaultBlockState();
            case FURNACE -> Blocks.SMOKER.defaultBlockState();
            case CAULDRON -> Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,3);
            case BOARD -> Blocks.BLACK_CONCRETE.defaultBlockState();
            case SIGN -> Blocks.OAK_SIGN.defaultBlockState();
        };
    }
    private static final class BlockTagsAdapter {
        static final net.minecraft.tags.TagKey<Block> LEAVES=net.minecraft.tags.BlockTags.LEAVES;
    }
}
