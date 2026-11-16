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
    private record Compiled(SchoolBlueprint.Plan plan, SchoolPlanGrid grid) {}
    private static final Map<String, Compiled> PLANS = new ConcurrentHashMap<>();
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
        var compiled=PLANS.computeIfAbsent(schoolId+":"+layoutVersion,id -> {
            var p=SchoolBlueprint.forVersion(school,layoutVersion);return new Compiled(p,new SchoolPlanGrid(p));
        });
        var plan=compiled.plan();var grid=compiled.grid();
        int minX=Math.max(boundingBox.minX(),clip.minX()), maxX=Math.min(boundingBox.maxX(),clip.maxX());
        int minZ=Math.max(boundingBox.minZ(),clip.minZ()), maxZ=Math.min(boundingBox.maxZ(),clip.maxZ());
        BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
        var palette=new java.util.EnumMap<SchoolBlueprint.Material,BlockState>(SchoolBlueprint.Material.class);
        for(var material:SchoolBlueprint.Material.values()) palette.put(material,state(material,school));
        // Clear each column once. Do not leave slices of a cut hillside suspended above roofs.
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++) {
            int lx=x-origin.getX(),lz=z-origin.getZ();
            int natural=level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,x,z)-1;
            int edge=Math.min(Math.min(lx,lz),Math.min(plan.size()-1-lx,plan.size()-1-lz));
            boolean apron=layoutVersion>=6&&edge<SchoolPolish.APRON;
            int grade=apron?SchoolTerrain.apronHeight(natural,origin.getY(),edge,SchoolPolish.APRON):origin.getY();
            int top=Math.min(level.getMaxBuildHeight()-1,Math.max(natural,origin.getY()+plan.height()-1));
            for(int y=grade+1;y<=top;y++) {pos.set(x,y,z);level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);}
            for(int y=grade-1;y>level.getMinBuildHeight();y--) {
                pos.set(x,y,z);BlockState old=level.getBlockState(pos);
                if(y<grade-4&&!old.isAir()&&old.getFluidState().isEmpty()&&!old.is(BlockTagsAdapter.LEAVES))break;
                level.setBlock(pos,y>=grade-4?Blocks.DIRT.defaultBlockState():Blocks.STONE.defaultBlockState(),2);
            }
            if(apron) {
                pos.set(x,grade,z);
                boolean entrance=lx>=plan.size()/2-4&&lx<=plan.size()/2+4&&lz<SchoolPolish.APRON;
                level.setBlock(pos,palette.get(entrance?SchoolBlueprint.Material.PATH_TILE:SchoolBlueprint.Material.GRASS),2);
            }
        }
        // Final solid cells first, attached/two-part blocks second. No transient furniture is placed and erased.
        for(int phase=0;phase<2;phase++) for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++) {
            int lx=x-origin.getX(),lz=z-origin.getZ();
            int edge=Math.min(Math.min(lx,lz),Math.min(plan.size()-1-lx,plan.size()-1-lz));
            if(layoutVersion>=6&&edge<SchoolPolish.APRON)continue;
            for(int y=-1;y<plan.height();y++) {
                var material=grid.at(lx,y,lz);
                if(material==SchoolBlueprint.Material.AIR||deferred(material)!=(phase==1))continue;
                int wy=origin.getY()+y;
                if(wy<clip.minY()||wy>clip.maxY()||wy<level.getMinBuildHeight()||wy>=level.getMaxBuildHeight())continue;
                pos.set(x,wy,z);BlockState block=palette.get(material);
                if(block.getBlock() instanceof IronBarsBlock) {
                    for(Direction direction:Direction.Plane.HORIZONTAL) {
                        var neighbor=palette.get(grid.at(lx+direction.getStepX(),y,lz+direction.getStepZ()));
                        boolean attach=neighbor.getBlock() instanceof IronBarsBlock||neighbor.canOcclude()||neighbor.getBlock() instanceof AbstractGlassBlock;
                        var property=switch(direction) {case NORTH -> IronBarsBlock.NORTH;case SOUTH -> IronBarsBlock.SOUTH;case WEST -> IronBarsBlock.WEST;default -> IronBarsBlock.EAST;};
                        block=block.setValue(property,attach);
                    }
                }
                level.setBlock(pos,block,2);
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
    private static boolean deferred(SchoolBlueprint.Material material) {
        return switch(material) {
            case SIGN,WALL_SIGN,DOOR_LOWER,DOOR_UPPER,SIDE_DOOR_LOWER,SIDE_DOOR_UPPER,BED_HEAD,BED_FOOT,FENCE,GATE_EW,GATE_NS,WINDOW_MULLION,BREWING_STAND,FLOWER,WATER,PENDANT,STREET_LANTERN -> true;
            default -> false;
        };
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
            case WINDOW_SILL -> Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.TOP);
            case CORNICE -> Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.BOTTOM);
            case PATH_EDGE -> Blocks.STONE_BRICKS.defaultBlockState();
            case PATH_TILE -> Blocks.SMOOTH_STONE.defaultBlockState();
            case PLANTER_SOIL -> Blocks.COARSE_DIRT.defaultBlockState();
            case SHRUB -> Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
            case FLOWER -> Blocks.DANDELION.defaultBlockState();
            case FACADE_BASE -> (school.id().equals("wellston")?Blocks.SMOOTH_SANDSTONE:Blocks.POLISHED_ANDESITE).defaultBlockState();
            case FACADE_PIER -> (school.id().equals("wellston")?Blocks.SMOOTH_SANDSTONE:school.id().equals("westview")?Blocks.WHITE_CONCRETE:Blocks.SMOOTH_QUARTZ).defaultBlockState();
            case FLOOR_INLAY -> (java.util.Set.of("wellston","rowden").contains(school.id())?Blocks.DARK_OAK_PLANKS:Blocks.POLISHED_ANDESITE).defaultBlockState();
            case PENDANT -> Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);
            case STREET_POST -> Blocks.DARK_OAK_FENCE.defaultBlockState();
            case STREET_LANTERN -> Blocks.LANTERN.defaultBlockState();
            case ROOF_VENT -> Blocks.STONE_BRICK_SLAB.defaultBlockState();
        };
    }
    private static final class BlockTagsAdapter {
        static final net.minecraft.tags.TagKey<Block> LEAVES=net.minecraft.tags.BlockTags.LEAVES;
    }
}
