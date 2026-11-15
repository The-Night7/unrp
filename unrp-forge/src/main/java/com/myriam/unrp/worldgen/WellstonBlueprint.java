package com.myriam.unrp.worldgen;

import java.util.ArrayList;
import java.util.List;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

/** Canon-based Wellston locations in a compressed, playable campus; room adjacencies are adaptations. */
public final class WellstonBlueprint {
    public static final int SIZE=129, HEIGHT=27;
    private final List<SchoolBlueprint.Box> boxes=new ArrayList<>();
    private final List<SchoolBlueprint.Label> labels=new ArrayList<>();
    private WellstonBlueprint() {}
    public static SchoolBlueprint.Plan create() { return new WellstonBlueprint().build(); }
    private void box(SchoolBlueprint.Material m,int x1,int y1,int z1,int x2,int y2,int z2) {
        boxes.add(new SchoolBlueprint.Box(m,x1,y1,z1,x2,y2,z2));
    }
    private void at(SchoolBlueprint.Material m,int x,int y,int z) { box(m,x,y,z,x,y,z); }
    private void door(int x,int y,int z) { at(DOOR_LOWER,x,y,z);at(DOOR_UPPER,x,y+1,z); }
    private void label(int x,int y,int z,String text) { at(SIGN,x,y,z);labels.add(new SchoolBlueprint.Label(x,y,z,text)); }
    private void wallLabel(int x,int y,int z,String text) { at(WALL_SIGN,x,y,z);labels.add(new SchoolBlueprint.Label(x,y,z,text)); }
    private void shell(int x1,int z1,int x2,int z2,int floors) {
        int roof=floors*6;
        box(WALL,x1,1,z1,x2,roof-1,z2);
        box(AIR,x1+1,1,z1+1,x2-1,roof-1,z2-1);
        for(int f=0;f<floors;f++) {
            int y=f*6;
            box(FLOOR,x1,y,z1,x2,y,z2);
            box(TRIM,x1,y+5,z1,x2,y+5,z1);box(TRIM,x1,y+5,z2,x2,y+5,z2);
            box(TRIM,x1,y+5,z1,x1,y+5,z2);box(TRIM,x2,y+5,z1,x2,y+5,z2);
            for(int x=x1+2;x<=x2-2;x+=4) {
                box(GLASS,x,y+2,z1,x+1,y+4,z1);box(GLASS,x,y+2,z2,x+1,y+4,z2);
            }
            for(int z=z1+3;z<=z2-2;z+=4) {
                box(GLASS,x1,y+2,z,x1,y+4,z+1);box(GLASS,x2,y+2,z,x2,y+4,z+1);
            }
        }
        box(ROOF,x1,roof,z1,x2,roof,z2);
        box(FENCE,x1,roof+1,z1,x2,roof+2,z1);box(FENCE,x1,roof+1,z2,x2,roof+2,z2);
        box(FENCE,x1,roof+1,z1,x1,roof+2,z2);box(FENCE,x2,roof+1,z1,x2,roof+2,z2);
    }
    private void stairs(int x,int z,int floors,boolean roofAccess) {
        for(int f=0;f<(roofAccess?floors:floors-1);f++) {
            int y=f*6+1;
            box(AIR,x,y,z,x+2,y+8,z+5);
            for(int step=0;step<6;step++) {
                box(FOUNDATION,x,y+step-1,z+step,x+2,y+step-1,z+step);
                box(STAIRS,x,y+step,z+step,x+2,y+step,z+step);
            }
        }
    }
    private void classroom(int x1,int z1,int x2,int z2,int floor,String name,boolean north) {
        int y=floor*6+1,edge=north?z2:z1,dx=(x1+x2)/2;
        box(PARTITION,x1,y,edge,x2,y+4,edge);door(dx,y,edge);
        label(dx-2,y,north?edge+1:edge-1,name);
        box(BOARD,x1+2,y+1,z1+1,x1+6,y+2,z1+1);
        for(int x=x1+3;x<x2-1;x+=4) for(int z=z1+3;z<z2-1;z+=3) {
            at(DESK,x,y,z);at(CHAIR,x,y,z+1);
        }
        at(LIGHT,dx,y+4,(z1+z2)/2);
    }
    private void computer(int x,int y,int z) { at(DESK,x,y,z);at(COMPUTER,x,y+1,z);at(CHAIR,x,y,z+1); }
    private void bed(int x,int y,int z) { at(BED_FOOT,x,y,z);at(BED_HEAD,x,y,z+1); }
    private void mainHall() {
        shell(12,16,116,42,3);
        for(int f=0;f<3;f++) {
            int y=f*6+1;
            for(int x:new int[]{36,59,69,91}) {
                box(PARTITION,x,y,17,x,y+4,27);box(PARTITION,x,y,32,x,y+4,41);
            }
            for(int i=0;i<4;i++) {
                int x1=new int[]{14,38,71,93}[i],x2=new int[]{34,57,89,114}[i];
                classroom(x1,18,x2,27,f,"Room "+((f+1)*100+10+i),true);
                classroom(x1,32,x2,40,f,"Room "+((f+1)*100+20+i),false);
            }
            // Metal lockers along the corridor, leaving central circulation clear.
            for(int x=15;x<=113;x+=3) if(x<59 || x>69) at(LOCKER,x,y,29);
            for(int x:new int[]{22,46,64,82,106}) at(LIGHT,x,y+4,30);
        }
        // The central facade is slightly projected and pale-trimmed, without an invented clock tower.
        box(WALL,59,1,14,69,17,15);
        box(TRIM,59,1,14,60,17,14);box(TRIM,68,1,14,69,17,14);
        for(int f=0;f<3;f++) {
            int y=f*6;
            box(TRIM,59,y+5,14,69,y+5,14);
            if(f>0) for(int x:new int[]{62,65}) box(GLASS,x,y+2,14,x+1,y+4,14);
        }
        box(AIR,62,1,14,66,3,16);
        for(int x=63;x<=65;x++) door(x,1,14);
        wallLabel(64,5,13,"Wellston Private High School");
        label(54,1,10,"Wellston");label(72,1,12,"Wallik Hall");
        // Roof access is the main staircase's final flight, inside a plain service hut.
        stairs(63,19,3,true);
        box(WALL,62,19,18,66,23,26);box(AIR,63,19,19,65,22,25);
        box(ROOF,62,24,18,66,24,26);
        box(AIR,63,19,26,65,21,26);door(64,19,26);
        // Restore the opening and landing after constructing the hut.
        box(AIR,63,18,19,65,20,23);
        box(STAIRS,63,18,24,65,18,24);
        label(68,19,28,"Rooftop");
        // Infirmary: replace the generic south-west classroom furniture, keeping its door and corridor.
        box(AIR,15,1,33,33,4,39);
        for(int x:new int[]{17,21,25,29}) bed(x,1,35);
        computer(16,1,38);box(COUNTER,31,1,34,33,1,38);
        relabel(22,1,31,"Infirmary");
        // Second floor: the Safe House, explicitly Room 224, and the newspaper's workroom.
        box(AIR,39,7,33,56,10,39);
        box(SOFA,40,7,35,45,7,35);box(SOFA,40,7,38,45,7,38);
        box(DESK,41,7,36,44,7,36);box(BOOKS,54,7,34,56,9,34);
        box(DESK,49,7,37,52,7,37);at(CHAIR,49,7,38);at(CHAIR,52,7,38);
        relabel(45,7,31,"Safe House - Room 224");label(55,7,38,"No fighting. Everyone welcome.");
        box(AIR,72,7,33,88,10,39);
        for(int x:new int[]{74,79,84}) computer(x,7,35);
        box(BOOKS,86,7,38,88,8,38);relabel(78,7,31,"Wellston Weekly - Press Room");
        box(AIR,94,1,33,113,4,39);computer(105,1,35);box(BOOKS,94,1,34,94,3,39);
        relabel(101,1,31,"Headmaster's Office");
        door(64,1,42);
    }
    private void relabel(int x,int y,int z,String text) {
        // Replace existing labels at exactly this location rather than leaving a misleading room sign.
        labels.removeIf(l -> l.x()==x && l.y()==y && l.z()==z);label(x,y,z,text);
    }
    private void annex(int x1,int x2,String name,boolean library) {
        shell(x1,50,x2,76,3);
        label(x1+2,1,47,name);
        for(int f=0;f<3;f++) {
            int y=f*6+1;
            for(int x:new int[]{x1+6,x1+18,x1+30}) at(LIGHT,x,y+4,64);
            if(f>0) {
                classroom(x1+2,52,x2-2,62,f,library?"Science Classroom":"Classroom",true);
                classroom(x1+2,67,x2-2,74,f,"Study Room",false);
            }
            box(AIR,x1+16,y,50,x1+20,y+3,50);
        }
        int cx=(x1+x2)/2;
        stairs(cx-1,53,3,false);
        // A covered, ground-level walkway joins the annex to the main teaching block.
        box(PAVING,cx-2,0,43,cx+2,0,49);box(ROOF,cx-3,5,43,cx+3,5,49);
        box(AIR,cx-2,1,42,cx+2,3,42);box(AIR,cx-2,1,50,cx+2,3,50);
        if(library) {
            box(BOOKS,x1+1,1,52,x1+1,3,73);box(BOOKS,x2-1,1,52,x2-1,3,73);
            for(int z:new int[]{55,61,69}) { box(BOOKS,x1+4,1,z,x1+10,2,z);computer(x2-5,1,z); }
            label(x1+4,1,64,"Library");
        } else {
            for(int x:new int[]{x1+5,x1+11,x2-10,x2-4}) for(int z:new int[]{54,59,67}) {
                box(CAFETERIA_TABLE,x,1,z,x+2,1,z);at(CAFETERIA_TABLE,x+1,1,z-1);at(CAFETERIA_TABLE,x+1,1,z+1);
                at(CAFETERIA_CHAIR,x,1,z+1);at(CAFETERIA_CHAIR,x+2,1,z+1);
            }
            box(CAFETERIA_COUNTER,x1+3,1,72,x2-3,1,72);
            box(CAFETERIA_WALL,x1+1,1,75,x2-1,4,75);
            box(GLASS,x1+3,2,72,x2-3,2,72);
            at(FURNACE,x1+3,1,74);at(CAULDRON,x1+6,1,74);
            label(x1+3,1,64,"Cafeteria");label(x2-11,1,70,"Triple Chocolate Cake - Monthly Special");
        }
    }
    private void dorm(int x1,String name) {
        int x2=x1+34,cx=x1+17;
        shell(x1,86,x2,116,2);
        for(int f=0;f<2;f++) {
            int y=f*6+1;
            box(PARTITION,x1+14,y,87,x1+14,y+4,115);box(PARTITION,x1+20,y,87,x1+20,y+4,115);
            box(AIR,x1+1,y,99,x2-1,y+3,102);
            for(int side:new int[]{0,1}) {
                int left=side==0?x1+2:x1+22,right=side==0?x1+12:x2-2;
                for(boolean front:new boolean[]{true,false}) {
                    int wall=front?98:103;
                    box(PARTITION,left,y,wall,right,y+4,wall);door((left+right)/2,y,wall);
                    int z=front?90:106;
                    bed(left+2,y,z);bed(right-2,y,z);at(DESK,left+2,y,z+4);at(CHAIR,left+2,y,z+5);
                    at(LOCKER,right-2,y,z+4);at(LIGHT,(left+right)/2,y+4,z+2);
                }
            }
            at(LIGHT,cx,y+4,100);
        }
        stairs(cx-1,89,2,false);door(cx,1,86);label(cx-5,1,83,name);
    }
    private SchoolBlueprint.Plan build() {
        box(AIR,0,1,0,128,HEIGHT-1,128);box(FOUNDATION,0,-1,0,128,-1,128);box(GRASS,0,0,0,128,0,128);
        box(PAVING,7,0,8,121,0,80);
        box(FENCE,3,1,4,125,2,4);box(FENCE,3,1,124,125,2,124);
        box(FENCE,3,1,5,3,2,123);box(FENCE,125,1,5,125,2,123);
        box(AIR,57,1,4,71,3,4);box(PAVING,57,0,0,71,0,16);
        for(int x:new int[]{55,73}) {box(WALL,x,1,3,x+1,4,5);box(TRIM,x,5,3,x+1,5,5);}
        mainHall();annex(12,48,"Whesley Hall",true);annex(80,116,"Manner Hall",false);
        // A landscaped quad replaces the prototype's invented central basketball arena/fountain.
        box(GRASS,51,0,49,77,0,75);box(PAVING,62,0,43,66,0,123);box(PAVING,48,0,63,80,0,67);
        for(int x:new int[]{54,74}) for(int z:new int[]{53,72}) {
            box(LOG,x,1,z,x,4,z);box(LEAVES,x-2,5,z-2,x+2,6,z+2);
        }
        for(int x:new int[]{53,75}) box(CHAIR,x,1,58,x,1,61);
        dorm(12,"Boys' Dormitories");dorm(82,"Girls' Dormitories");
        box(PAVING,26,0,80,102,0,84);box(PAVING,27,0,80,31,0,86);box(PAVING,97,0,80,101,0,86);
        for(int x:new int[]{8,120}) for(int z:new int[]{12,46,80,120}) {
            box(LOG,x,1,z,x,3,z);box(LEAVES,x-2,4,z-2,x+2,5,z+2);
        }
        for(int x:new int[]{10,50,78,118}) for(int z:new int[]{9,46,80,120}) {
            box(FOUNDATION,x,1,z,x,2,z);at(LIGHT,x,3,z);
        }
        return new SchoolBlueprint.Plan(List.copyOf(boxes),List.copyOf(labels),HEIGHT,SIZE);
    }
}
