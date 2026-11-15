package com.myriam.unrp.worldgen;

import java.util.List;
import static com.myriam.unrp.worldgen.CampusBuilder.Use.*;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

/** Frozen version-4 H-shaped campus, retained for saved pieces after the photo-based redesign. */
final class WestviewV4Blueprint {
    public static final int SIZE=97,HEIGHT=21;
    static final List<CampusBuilder.Building> BUILDINGS=List.of(
            new CampusBuilder.Building("West Academic Wing",12,22,34,74,2,false),
            new CampusBuilder.Building("East Academic Wing",62,22,84,74,2,false));
    private WestviewV4Blueprint() {}
    public static SchoolBlueprint.Plan create() {
        var c=new CampusBuilder(SIZE,HEIGHT,"Westview");c.path(9,16,87,20);
        c.building(BUILDINGS.get(0),(f,r) -> {
            if(f==0 && r==0) return new CampusBuilder.Room("Library",LIBRARY);
            if(f==0 && r==1) return new CampusBuilder.Room("Administration",OFFICE);
            if(f==0 && r==2) return new CampusBuilder.Room("Infirmary",INFIRMARY);
            return new CampusBuilder.Room("West Classroom "+(f+1)+"-"+(r+1),CLASSROOM);
        });
        c.building(BUILDINGS.get(1),(f,r) -> new CampusBuilder.Room(
                f==0?(r<2?"Cafeteria ":"Design Studio ")+(r+1):"Science Lab "+(r+1),
                f==0?(r<2?CAFETERIA:STUDY):LAB));
        // Two enclosed bridge levels link the wings directly into their cross corridors.
        for(int floor=0;floor<2;floor++) {
            int y=floor*6;
            c.box(FLOOR,35,y,43,61,y,53);
            c.box(WALL,35,y+1,43,61,y+5,43);c.box(WALL,35,y+1,53,61,y+5,53);
            for(int x=37;x<60;x+=4) {c.box(GLASS,x,y+2,43,x+1,y+4,43);c.box(GLASS,x,y+2,53,x+1,y+4,53);}
            c.box(AIR,34,y+1,47,34,y+3,49);c.box(AIR,62,y+1,47,62,y+3,49);
            c.at(LIGHT,48,y+5,48);
        }
        c.box(ROOF,35,12,43,61,12,53);c.door(48,1,43);c.door(48,1,53);
        c.label(44,1,41,"Student Hub");c.path(44,12,52,42);c.path(44,54,52,88);
        for(int x:new int[]{39,57}) for(int z:new int[]{30,66,83}) c.tree(x,z);
        c.bench(37,74);c.bench(55,74);
        for(int x:new int[]{8,88}) for(int z:new int[]{12,48,86}) c.lamp(x,z);
        return c.finish();
    }
}
