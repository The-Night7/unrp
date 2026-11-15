package com.myriam.unrp.worldgen;

import java.util.List;
import static com.myriam.unrp.worldgen.CampusBuilder.Use.*;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

/** Episode 369's landscaped, mixed-height university and teaching labs, with a compressed campus arrangement. */
public final class UniversityBlueprint {
    public static final int SIZE=161,HEIGHT=55;
    static final List<CampusBuilder.Building> BUILDINGS=List.of(
            new CampusBuilder.Building("Medical Department",12,18,74,50,3,false),
            new CampusBuilder.Building("Lecture Hall",90,18,148,50,3,false),
            new CampusBuilder.Building("Student Residences",12,68,58,132,8,false,TOWER_WALL),
            new CampusBuilder.Building("Academic Tower",102,68,148,132,7,false,TOWER_WALL));
    private UniversityBlueprint() {}
    public static SchoolBlueprint.Plan create() {
        var c=new CampusBuilder(SIZE,HEIGHT,"Wellston University");c.path(9,10,151,16);
        c.building(BUILDINGS.get(0),(f,r) -> new CampusBuilder.Room(f==0 && r==0?"Medical Teaching Laboratory":"Medical Lab "+(f+1)+"-"+(r+1),LAB));
        c.building(BUILDINGS.get(1),(f,r) -> new CampusBuilder.Room(f==0 && r==0?"Admissions":"Lecture "+(f+1)+"-"+(r+1),f==0 && r==0?OFFICE:LECTURE));
        c.building(BUILDINGS.get(2),(f,r) -> new CampusBuilder.Room(f==0?"Residence Commons "+(r+1):"Student Room "+(f+1)+"-"+(r+1),f==0?COMMONS:RESIDENCE));
        c.building(BUILDINGS.get(3),(f,r) -> new CampusBuilder.Room(f==0?"University Library "+(r+1):"Seminar "+(f+1)+"-"+(r+1),f==0?LIBRARY:(r%2==0?LECTURE:STUDY)));
        // White/grey towers with continuous window bands reproduce the visible campus language.
        for(var b:BUILDINGS.subList(2,4)) for(int f=0;f<b.floors();f++) {
            int y=f*6;
            c.box(TOWER_GLASS,b.x1()+1,y+2,b.z1(),b.x2()-1,y+4,b.z1());
            c.box(TOWER_GLASS,b.x1()+1,y+2,b.z2(),b.x2()-1,y+4,b.z2());
            for(int x=b.x1();x<=b.x2();x+=8) {
                c.box(TOWER_WALL,x,y+1,b.z1(),x,y+5,b.z1());c.box(TOWER_WALL,x,y+1,b.z2(),x,y+5,b.z2());
            }
            if(f==0) {c.door(b.midX(),1,b.z1());c.door(b.midX(),1,b.z2());}
        }
        // Low pitched brick-hall roofs contrast with the flat modern towers, as in the establishing view.
        for(var b:BUILDINGS.subList(0,2)) for(int rise=1;rise<=3;rise++)
            c.box(ROOF,b.x1()+rise,18+rise,b.z1()+rise*2,b.x2()-rise,18+rise,b.z2()-rise*2);
        c.path(76,17,84,151);c.path(9,56,151,62);c.path(9,140,151,145);
        for(int x:new int[]{66,94}) for(int z:new int[]{78,103,128}) c.tree(x,z);
        for(int x:new int[]{6,154}) for(int z:new int[]{14,58,98,142}) c.lamp(x,z);
        c.bench(67,89);c.bench(90,116);
        return c.finish();
    }
}
