package com.myriam.unrp.worldgen;

import java.util.List;
import static com.myriam.unrp.worldgen.CampusBuilder.Use.*;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

/** Author-supplied photographic references: glazed slab blocks, low pavilions and a monumental entrance. */
public final class WestviewBlueprint {
    public static final int SIZE=177,HEIGHT=33;
    static final List<CampusBuilder.Building> BUILDINGS=List.of(
            new CampusBuilder.Building("West Teaching Block",10,20,64,46,4,false),
            new CampusBuilder.Building("East Teaching Block",112,20,166,46,4,false),
            new CampusBuilder.Building("Reception and Administration",72,22,104,48,3,false),
            new CampusBuilder.Building("Science Block",16,66,76,92,3,false),
            new CampusBuilder.Building("Arts and Humanities",100,66,160,92,3,false),
            new CampusBuilder.Building("North Academic Block",66,124,164,150,4,false),
            new CampusBuilder.Building("Library Pavilion",12,104,54,128,1,false),
            new CampusBuilder.Building("Dining Pavilion",12,140,54,164,1,false));
    private WestviewBlueprint() {}
    public static SchoolBlueprint.Plan create() {
        var c=new CampusBuilder(SIZE,HEIGHT,"Westview");
        c.removeLabel(80,1,8); // This old sign position is now part of the entrance gateway.
        for(int i=0;i<BUILDINGS.size();i++) {
            int index=i;var b=BUILDINGS.get(i);
            c.building(b,(f,r) -> room(index,f,r));
            curtainWalls(c,b,index==2);metalRoof(c,b);
        }
        frontBridges(c);
        // Roofed pedestrian routes link every block while leaving the large lawns open.
        covered(c,85,12,91,21);covered(c,85,49,91,123);
        covered(c,35,47,39,57);covered(c,37,55,46,59);covered(c,44,57,48,65);
        covered(c,137,47,141,57);covered(c,130,55,139,59);covered(c,128,57,132,65);
        covered(c,44,93,48,99);covered(c,33,97,46,101);covered(c,31,99,35,103);
        covered(c,128,93,132,115);covered(c,115,113,130,117);covered(c,113,115,117,123);
        covered(c,31,129,35,139);covered(c,55,114,88,118);
        // A side entrance opens the low library pavilion onto the covered route to the central spine.
        c.box(AIR,54,1,115,54,3,117);
        c.path(31,165,35,171);
        // Service loop and a marked forecourt parking area, without spawning vehicles.
        c.box(ROAD_DARK,6,0,16,9,0,171);c.box(ROAD_DARK,167,0,16,171,0,171);
        c.box(ROAD_DARK,6,0,167,171,0,171);c.box(ROAD_DARK,116,0,8,164,0,15);
        for(int x=116;x<=164;x+=6) c.box(WHITE,x,0,9,x,0,14);
        c.box(PLAZA_PAVERS,67,0,0,109,0,21);
        gateway(c);mural(c);flags(c);
        for(int x:new int[]{24,62,114,156}) for(int z:new int[]{55,103}) c.tree(x,z);
        for(int x:new int[]{80,96}) c.tree(x,143);
        c.bench(54,58);c.bench(114,58);c.bench(80,104);c.bench(94,104);
        for(int x:new int[]{7,169}) for(int z:new int[]{18,60,100,158}) c.lamp(x,z);
        c.label(64,1,14,"Westview");
        return c.finish();
    }
    private static CampusBuilder.Room room(int building,int floor,int room) {
        return switch(building) {
            case 2 -> new CampusBuilder.Room(floor==0?switch(room) {case 0 -> "Admissions";case 1 -> "Administration";case 2 -> "Infirmary";default -> "Reception Lounge";}:"Student Hub "+(floor+1)+"-"+(room+1),
                    floor==0?(room==2?INFIRMARY:room==3?COMMONS:OFFICE):STUDY);
            case 3 -> new CampusBuilder.Room("Science Lab "+(floor+1)+"-"+(room+1),LAB);
            case 4 -> new CampusBuilder.Room("Arts Studio "+(floor+1)+"-"+(room+1),floor==0?STUDY:CLASSROOM);
            case 6 -> new CampusBuilder.Room("Library "+(room+1),LIBRARY);
            case 7 -> new CampusBuilder.Room(room<3?"Dining Room "+(room+1):"Student Commons",room<3?CAFETERIA:COMMONS);
            default -> new CampusBuilder.Room((building==0?"West":building==1?"East":"North")+" Classroom "+(floor+1)+"-"+(room+1),CLASSROOM);
        };
    }
    private static void curtainWalls(CampusBuilder c,CampusBuilder.Building b,boolean atrium) {
        int top=b.floors()*6;
        // Plain concrete end walls and a single vertical stairwell slit contrast with the long glazed faces.
        for(int x:new int[]{b.x1(),b.x2()}) {
            c.box(WALL,x,1,b.z1(),x,top-1,b.z2());
            c.box(TOWER_GLASS,x,2,b.midZ()-1,x,top-2,b.midZ()+1);
            for(int f=1;f<b.floors();f++) c.box(TRIM,x,f*6,b.midZ()-1,x,f*6,b.midZ()+1);
        }
        for(int f=0;f<b.floors();f++) {
            int y=f*6;
            for(int z:new int[]{b.z1(),b.z2()}) {
                c.box(ACCENT,b.x1()+1,y+1,z,b.x2()-1,y+1,z);
                c.box(TOWER_GLASS,b.x1()+1,y+(atrium?1:2),z,b.x2()-1,y+4,z);
                for(int x=b.x1()+1;x<b.x2();x+=4) c.box(TRIM,x,y+1,z,x,y+4,z);
                c.box(TRIM,b.x1(),y+5,z,b.x2(),y+5,z);
            }
        }
        c.door(b.midX(),1,b.z1());c.door(b.midX(),1,b.z2());
    }
    private static void metalRoof(CampusBuilder c,CampusBuilder.Building b) {
        int y=b.floors()*6;
        c.box(AIR,b.x1(),y+1,b.z1(),b.x2(),y+2,b.z2());
        c.box(ROOF,b.x1(),y+1,b.z1()+2,b.x2(),y+1,b.z2()-2);
        c.box(ROOF,b.x1()+1,y+2,b.z1()+4,b.x2()-1,y+2,b.z2()-4);
    }
    private static void frontBridges(CampusBuilder c) {
        for(int floor=0;floor<3;floor++) {
            int y=floor*6;
            for(int x1:new int[]{65,105}) {
                int x2=x1+6;
                c.box(FLOOR,x1,y,31,x2,y,35);
                c.box(TOWER_GLASS,x1,y+1,31,x2,y+4,31);c.box(TOWER_GLASS,x1,y+1,35,x2,y+4,35);
                c.box(TRIM,x1,y+5,31,x2,y+5,35);
                c.box(AIR,x1-1,y+1,32,x1-1,y+3,34);c.box(AIR,x2+1,y+1,32,x2+1,y+3,34);
            }
        }
        c.box(ROOF,65,18,31,71,18,35);c.box(ROOF,105,18,31,111,18,35);
    }
    private static void covered(CampusBuilder c,int x1,int z1,int x2,int z2) {
        c.path(x1,z1,x2,z2);c.box(ROOF,x1,5,z1,x2,5,z2);
        if(x2-x1<z2-z1) for(int z=z1;z<=z2;z+=8) {c.box(TRIM,x1,1,z,x1,4,z);c.box(TRIM,x2,1,z,x2,4,z);}
        else for(int x=x1;x<=x2;x+=8) {c.box(TRIM,x,1,z1,x,4,z1);c.box(TRIM,x,1,z2,x,4,z2);}
    }
    private static void gateway(CampusBuilder c) {
        c.box(GATE_EW,3,1,6,71,3,6);c.box(GATE_EW,105,1,6,173,3,6);
        for(int x:new int[]{72,100}) c.box(ACCENT,x,1,5,x+4,13,11);
        c.box(ACCENT,77,10,7,99,10,9);
        c.box(GATE_EW,77,1,8,83,3,8);c.box(GATE_EW,93,1,8,99,3,8);
        // The inward gate leaves flank a five-block pedestrian passage instead of blocking the player.
        c.box(GATE_NS,83,1,9,83,3,12);c.box(GATE_NS,93,1,9,93,3,12);
    }
    private static void flags(CampusBuilder c) {
        for(int i=0;i<3;i++) {
            int x=63+i*5;
            c.box(GATE_NS,x,1,16,x,10,16);
            c.box(i==0?FLAG_CYAN:i==1?WHITE:MURAL_GREEN,x+1,8,16,x+3,10,16);
        }
    }
    private static void mural(CampusBuilder c) {
        // Block-built leaf motif inspired by the supplied entrance image, not an imported photograph texture.
        c.box(WALL,72,1,21,82,17,21);
        for(int y=2;y<=15;y++) {
            int x=74+(y-2)/4;c.at(MURAL_GREEN,x,y,20);
            if(y%3==0) {leaf(c,x-1,y,2,1);leaf(c,x+2,y+1,2,2);}
        }
    }
    private static void leaf(CampusBuilder c,int cx,int cy,int rx,int ry) {
        for(int x=cx-rx;x<=cx+rx;x++) for(int y=cy-ry;y<=cy+ry;y++) {
            if(x<73 || x>82 || y<2 || y>16) continue;
            double d=(x-cx)*(x-cx)/(double)(rx*rx)+(y-cy)*(y-cy)/(double)(ry*ry);
            if(d<=1) c.at((x+y)%3==0?MURAL_LIGHT:MURAL_GREEN,x,y,20);
        }
    }
}
