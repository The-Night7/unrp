package com.myriam.unrp.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
    private final int layoutVersion;
    SchoolPiece(String id, BlockPos origin, int minY) {
        this(id,origin,minY,SchoolBlueprint.create(SchoolSites.school(id)));
    }
    private SchoolPiece(String id, BlockPos origin, int minY, SchoolBlueprint.Plan plan) {
        super(SchoolWorldgen.CAMPUS.get(),0,new BoundingBox(origin.getX(),minY,origin.getZ(),
                origin.getX()+plan.size()-1,origin.getY()+plan.height(),origin.getZ()+plan.size()-1));
        schoolId=id; this.origin=origin.immutable(); layoutVersion=SchoolBlueprint.CURRENT_LAYOUT_VERSION;
    }
    SchoolPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(SchoolWorldgen.CAMPUS.get(),tag);
        schoolId=tag.getString("School"); SchoolSites.school(schoolId);
        origin=BlockPos.of(tag.getLong("Origin"));
        layoutVersion=tag.contains("LayoutVersion")?tag.getInt("LayoutVersion"):1;
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("School",schoolId); tag.putLong("Origin",origin.asLong()); tag.putInt("LayoutVersion",layoutVersion);
    }
    @Override public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
                                     RandomSource random, BoundingBox clip, ChunkPos chunk, BlockPos pivot) {
        if (!level.getLevel().dimension().equals(Level.OVERWORLD)) return;
        var school=SchoolBlueprint.schoolForVersion(SchoolSites.school(schoolId),layoutVersion);
        var plan=PLANS.computeIfAbsent(schoolId+":"+layoutVersion,id -> SchoolBlueprint.forVersion(school,layoutVersion));
        int minX=Math.max(boundingBox.minX(),clip.minX()), maxX=Math.min(boundingBox.maxX(),clip.maxX());
        int minZ=Math.max(boundingBox.minZ(),clip.minZ()), maxZ=Math.min(boundingBox.maxZ(),clip.maxZ());
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
                SchoolSignText.initialize(sign,label.text());
            }
        }
    }
    private static BlockState state(SchoolBlueprint.Material material, SchoolSites.School school) {
        Block accent=switch(school.palette()) {
            case "new_bostin" -> Blocks.POLISHED_ANDESITE; case "rowden" -> Blocks.RED_TERRACOTTA;
            case "westview_modern" -> Blocks.LIGHT_GRAY_CONCRETE; case "westview" -> Blocks.CYAN_TERRACOTTA; case "university" -> Blocks.SMOOTH_QUARTZ;
            case "agwin" -> Blocks.LIGHT_GRAY_CONCRETE; case "wellston" -> Blocks.SMOOTH_SANDSTONE; case "blue" -> Blocks.BLUE_TERRACOTTA; case "green" -> Blocks.GREEN_TERRACOTTA;
            case "red" -> Blocks.RED_TERRACOTTA; case "cyan" -> Blocks.CYAN_TERRACOTTA;
            case "purple" -> Blocks.PURPLE_TERRACOTTA; default -> Blocks.YELLOW_TERRACOTTA;
        };
        return switch(material) {
            case AIR -> Blocks.AIR.defaultBlockState();
            case FOUNDATION -> Blocks.STONE_BRICKS.defaultBlockState();
            case PAVING -> Blocks.SMOOTH_STONE.defaultBlockState();
            case GRASS -> Blocks.GRASS_BLOCK.defaultBlockState();
            case WALL -> (school.palette().equals("westview_modern")?Blocks.WHITE_CONCRETE:school.palette().equals("new_bostin") || school.palette().equals("university")?Blocks.BRICKS:school.palette().equals("rowden")?Blocks.STONE_BRICKS:school.id().equals("wellston") && school.palette().equals("wellston")?Blocks.TERRACOTTA:school.palette().equals("red")?Blocks.STONE_BRICKS:school.palette().equals("green")?Blocks.BRICKS:Blocks.SMOOTH_QUARTZ).defaultBlockState();
            case FLOOR -> (java.util.Set.of("agwin","new_bostin","university","westview_modern").contains(school.palette())?Blocks.SMOOTH_STONE:Blocks.BIRCH_PLANKS).defaultBlockState();
            case ROOF -> (school.palette().equals("westview_modern")?Blocks.LIGHT_BLUE_TERRACOTTA:school.palette().equals("westview")?Blocks.WAXED_OXIDIZED_COPPER:school.palette().equals("new_bostin")?Blocks.LIGHT_GRAY_CONCRETE:school.palette().equals("university")?Blocks.GRAY_CONCRETE:school.palette().equals("agwin")?Blocks.GRAY_CONCRETE:school.palette().equals("wellston")?Blocks.LIGHT_GRAY_CONCRETE:school.palette().equals("green")?Blocks.DARK_OAK_PLANKS:Blocks.DEEPSLATE_TILES).defaultBlockState();
            case ACCENT -> accent.defaultBlockState();
            case GLASS -> (java.util.Set.of("agwin","new_bostin","university","westview_modern").contains(school.palette())?Blocks.LIGHT_GRAY_STAINED_GLASS:Blocks.LIGHT_BLUE_STAINED_GLASS).defaultBlockState();
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
            case DOOR_LOWER -> (school.palette().equals("agwin")?Blocks.BIRCH_DOOR:Blocks.OAK_DOOR).defaultBlockState().setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER);
            case DOOR_UPPER -> (school.palette().equals("agwin")?Blocks.BIRCH_DOOR:Blocks.OAK_DOOR).defaultBlockState().setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER);
            case BED_HEAD -> Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.PART,BedPart.HEAD).setValue(BedBlock.FACING,Direction.SOUTH);
            case BED_FOOT -> Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.PART,BedPart.FOOT).setValue(BedBlock.FACING,Direction.SOUTH);
            case COUNTER -> Blocks.SMOOTH_STONE.defaultBlockState();
            case FURNACE -> Blocks.SMOKER.defaultBlockState();
            case CAULDRON -> Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,3);
            case BOARD -> (school.palette().equals("university")?Blocks.WHITE_CONCRETE:school.palette().equals("new_bostin")?Blocks.GREEN_CONCRETE:school.palette().equals("agwin")?Blocks.GREEN_CONCRETE:Blocks.BLACK_CONCRETE).defaultBlockState();
            case SIGN -> Blocks.OAK_SIGN.defaultBlockState();
            case WALL_SIGN -> Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING,Direction.NORTH);
            case TRIM -> (java.util.Set.of("new_bostin","university","rowden","westview_modern").contains(school.palette())?Blocks.POLISHED_ANDESITE:school.palette().equals("agwin")?Blocks.LIGHT_GRAY_CONCRETE:Blocks.SMOOTH_SANDSTONE).defaultBlockState();
            case PARTITION -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case LOCKER -> Blocks.IRON_BLOCK.defaultBlockState();
            case COMPUTER -> Blocks.BLACK_CONCRETE.defaultBlockState();
            case SOFA -> Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
            case CAFETERIA_TABLE -> Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.TOP);
            case CAFETERIA_CHAIR -> Blocks.PURPUR_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);
            case CAFETERIA_COUNTER -> Blocks.PRISMARINE_BRICKS.defaultBlockState();
            case CAFETERIA_WALL -> Blocks.YELLOW_CONCRETE.defaultBlockState();
            case AGWIN_YELLOW -> Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            case AGWIN_PINK -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
            case CENTRE_PANEL -> Blocks.QUARTZ_BRICKS.defaultBlockState();
            case WINDOW_FRAME -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            case WINDOW_MULLION -> Blocks.BLACK_STAINED_GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.EAST,true).setValue(IronBarsBlock.WEST,true);
            case HALL_TRIM -> Blocks.PRISMARINE_BRICKS.defaultBlockState();
            case SIDE_DOOR_LOWER -> Blocks.BIRCH_DOOR.defaultBlockState().setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER).setValue(DoorBlock.FACING,Direction.EAST);
            case SIDE_DOOR_UPPER -> Blocks.BIRCH_DOOR.defaultBlockState().setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER).setValue(DoorBlock.FACING,Direction.EAST);
            case STAIRS_NORTH -> Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH);
            case TOWER_WALL -> Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
            case TOWER_GLASS -> Blocks.GRAY_STAINED_GLASS.defaultBlockState();
            case BREWING_STAND -> Blocks.BREWING_STAND.defaultBlockState();
            case ROAD_DARK -> Blocks.GRAY_CONCRETE.defaultBlockState();
            case PLAZA_PAVERS -> Blocks.POLISHED_ANDESITE.defaultBlockState();
            case GATE_EW -> Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST,true).setValue(IronBarsBlock.WEST,true);
            case GATE_NS -> Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH,true).setValue(IronBarsBlock.SOUTH,true);
            case MURAL_GREEN -> Blocks.GREEN_CONCRETE.defaultBlockState();
            case MURAL_LIGHT -> Blocks.LIME_TERRACOTTA.defaultBlockState();
            case FLAG_CYAN -> Blocks.CYAN_CONCRETE.defaultBlockState();
        };
    }
    private static final class BlockTagsAdapter {
        static final net.minecraft.tags.TagKey<Block> LEAVES=net.minecraft.tags.BlockTags.LEAVES;
    }
}
