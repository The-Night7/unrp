package com.myriam.unrp.worldgen;

import java.util.Arrays;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

/** Final material cells, compiled once instead of repeatedly constructing and deleting blocks. */
public final class SchoolPlanGrid {
    private static final SchoolBlueprint.Material[] MATERIALS=SchoolBlueprint.Material.values();
    private final int size,height,layer;
    private final byte[] cells;
    public SchoolPlanGrid(SchoolBlueprint.Plan plan) {
        size=plan.size();height=plan.height();layer=size*size;cells=new byte[layer*(height+1)];
        for(var b:plan.boxes()) for(int y=Math.max(-1,b.y1());y<=Math.min(height-1,b.y2());y++)
            for(int z=Math.max(0,b.z1());z<=Math.min(size-1,b.z2());z++)
                Arrays.fill(cells,(y+1)*layer+z*size+Math.max(0,b.x1()),(y+1)*layer+z*size+Math.min(size-1,b.x2())+1,(byte)b.material().ordinal());
    }
    public SchoolBlueprint.Material at(int x,int y,int z) {
        if(x<0||z<0||x>=size||z>=size||y< -1||y>=height)return AIR;
        return MATERIALS[Byte.toUnsignedInt(cells[x+z*size+(y+1)*layer])];
    }
    public boolean clear(int x,int y,int z) {return at(x,y,z)==AIR;}
}
