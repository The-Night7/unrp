package com.myriam.unrp.item;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Collision geometry generated from the production machine model cuboids. */
public final class MachineShapes {
    private MachineShapes() {}
    private static final java.util.Map<String,VoxelShape> CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    public static VoxelShape of(String id) { return CACHE.computeIfAbsent(id,MachineShapes::build); }
    private static VoxelShape build(String id) {
        return switch(id) {
            case "conversion_machine" -> Shapes.or(Block.box(1,0,1,15,2,15),Block.box(2,2,4,12,13,14),Block.box(3,5,2,11,12,4),Block.box(3,12,3,11,14,6),Block.box(12,3,5,15,11,13),Block.box(3,3,1,11,5,4));
            case "recovery_machine" -> Shapes.or(Block.box(1,1,1,12,3,15),Block.box(2,3,2,11,6,14),Block.box(2,6,11,11,10,14),Block.box(3,6,10,10,7,12),Block.box(1,0,2,3,2,4),Block.box(10,0,12,12,2,14),Block.box(13,0,9,15,11,11),Block.box(12,10,7,16,15,10));
            case "suppression_field" -> Shapes.or(Block.box(1,0,1,15,3,15),Block.box(5,3,5,11,11,11),Block.box(3,11,3,13,13,13),Block.box(5,13,5,11,15,11),Block.box(3,3,3,5,10,5),Block.box(11,3,3,13,10,5),Block.box(3,3,11,5,10,13),Block.box(11,3,11,13,10,13));
            case "phylactery" -> Shapes.or(Block.box(3,0,3,13,2,13),Block.box(4,2,4,12,12,12),Block.box(3,12,3,13,14,13),Block.box(6,14,6,10,16,10),Block.box(3,2,3,4,12,4),Block.box(12,2,3,13,12,4),Block.box(3,2,12,4,12,13),Block.box(12,2,12,13,12,13));
            default -> Shapes.block();
        };
    }
    public static VoxelShape rotate(VoxelShape input, Direction facing) {
        int turns = switch(facing) {case EAST -> 1; case SOUTH -> 2; case WEST -> 3; default -> 0;};
        VoxelShape result = input;
        for (int i=0;i<turns;i++) {
            VoxelShape[] rotated={Shapes.empty()};
            result.forAllBoxes((x1,y1,z1,x2,y2,z2)->rotated[0]=Shapes.or(rotated[0],Shapes.box(1-z2,y1,x1,1-z1,y2,x2)));
            result=rotated[0];
        }
        return result;
    }
}
