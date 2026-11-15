package com.myriam.unrp.worldgen;

import java.util.ArrayList;
import java.util.List;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

/** Episode 374's visible Agwin exterior and classroom language, with adapted playable interiors. */
public final class AgwinBlueprint {
    public static final int SIZE=97, HEIGHT=39, FLOORS=5, PLINTH=2;
    private final List<SchoolBlueprint.Box> boxes=new ArrayList<>();
    private final List<SchoolBlueprint.Label> labels=new ArrayList<>();
    private AgwinBlueprint() {}
    public static SchoolBlueprint.Plan create() { return new AgwinBlueprint().build(); }
    private void box(SchoolBlueprint.Material m,int x1,int y1,int z1,int x2,int y2,int z2) {
        boxes.add(new SchoolBlueprint.Box(m,x1,y1,z1,x2,y2,z2));
    }
    private void at(SchoolBlueprint.Material m,int x,int y,int z) { box(m,x,y,z,x,y,z); }
    private void door(int x,int y,int z) { at(DOOR_LOWER,x,y,z);at(DOOR_UPPER,x,y+1,z); }
    private void sideDoor(int x,int y,int z) { at(SIDE_DOOR_LOWER,x,y,z);at(SIDE_DOOR_UPPER,x,y+1,z); }
    private void label(int x,int y,int z,String text) { at(SIGN,x,y,z);labels.add(new SchoolBlueprint.Label(x,y,z,text)); }
    private void wallLabel(int x,int y,int z,String text) { at(WALL_SIGN,x,y,z);labels.add(new SchoolBlueprint.Label(x,y,z,text)); }
    private void window(int x,int y,int z) {
        box(WINDOW_FRAME,x,y+2,z,x+4,y+4,z);
        box(GLASS,x+1,y+2,z,x+3,y+4,z);
        box(WINDOW_MULLION,x+2,y+2,z,x+2,y+4,z);
    }
    private void facade() {
        box(WALL,10,1,22,86,31,70);box(AIR,11,3,23,85,31,69);
        box(FOUNDATION,10,0,22,86,1,70);
        for(int floor=0;floor<FLOORS;floor++) {
            int y=PLINTH+floor*6;
            box(FLOOR,10,y,22,86,y,70);
            // The yellow and pink panels are visible on two middle storeys, not a blue school-wide palette.
            if(floor==1 || floor==2) {
                box(AGWIN_YELLOW,10,y+1,22,33,y+4,22);
                box(AGWIN_PINK,63,y+1,22,86,y+4,22);
            }
            for(int x:new int[]{12,19,26,66,73,80}) window(x,y,22);
            for(int x=12;x<83;x+=7) window(x,y,70);
            for(int z=25;z<=65;z+=7) for(int x:new int[]{10,86}) {
                box(WINDOW_FRAME,x,y+2,z,x,y+4,z+4);box(GLASS,x,y+2,z+1,x,y+4,z+3);
            }
            box(TRIM,10,y+5,22,86,y+5,22);box(TRIM,10,y+5,70,86,y+5,70);
            box(TRIM,10,y+5,22,10,y+5,70);box(TRIM,86,y+5,22,86,y+5,70);
            // Shallow projecting, tiled centre with four groups of framed windows.
            box(CENTRE_PANEL,34,y,20,62,y+5,20);
            box(FLOOR,34,y,20,62,y,22);
            box(AIR,35,y+1,21,61,y+4,22);
            for(int x:new int[]{36,43,50,57}) window(x,y,20);
        }
        box(ROOF,10,32,22,86,32,70);box(ROOF,34,32,20,62,32,21);
        box(ACCENT,10,33,22,86,33,22);box(ACCENT,10,33,70,86,33,70);
        box(ACCENT,10,33,22,10,33,70);box(ACCENT,86,33,22,86,33,70);
        // Low stepped pediment approximates the shallow triangular centre shown in the establishing panel.
        box(CENTRE_PANEL,35,33,20,61,33,20);box(CENTRE_PANEL,39,34,20,57,34,20);box(CENTRE_PANEL,43,35,20,53,35,20);
        // Broad entrance canopy, narrow supports and an accessible stepped entrance.
        box(ACCENT,32,7,15,64,7,21);
        for(int x:new int[]{34,62}) box(CENTRE_PANEL,x,1,16,x,6,16);
        box(FLOOR,34,2,18,62,2,21);
        box(STAIRS,44,1,16,52,1,16);box(STAIRS,44,2,17,52,2,17);
        box(AIR,46,3,20,50,6,22);
        for(int x=47;x<=49;x++) door(x,3,20);
        wallLabel(48,7,14,"Agwin Private High School");
        label(30,1,12,"Agwin");
    }
    private void classroom(int x1,int z1,int x2,int z2,int floor,int number,boolean left) {
        int y=PLINTH+floor*6+1;
        box(PARTITION,x1,y,z1,x2,y+4,z2);box(AIR,x1+1,y,z1+1,x2-1,y+4,z2-1);
        int edge=z1==24?z2:z1;
        door((x1+x2)/2,y,edge);
        int side=left?x1:x2,mid=(z1+z2)/2;
        sideDoor(side,y,mid);
        // Glazed partitions next to the doors match the windowed corridor/classroom separation.
        for(int z:new int[]{z1+2,z2-2}) box(GLASS,side,y+1,z,side,y+3,z);
        box(HALL_TRIM,side,y,z1,side,y,z2);
        sideDoor(side,y,mid);
        label(left?side-1:side+1,y,mid,"Classroom "+(floor+1)+"-"+number);
        box(BOARD,x1+3,y+1,z1+1,x1+11,y+2,z1+1);
        for(int x=x1+3;x<x2-1;x+=4) for(int z=z1+3;z<z2-1;z+=3) {at(DESK,x,y,z);at(CHAIR,x,y,z+1);}
        at(DESK,x2-4,y,z1+2);
        for(int z:new int[]{z1+3,z2-3}) at(LIGHT,(x1+x2)/2,y+4,z);
    }
    private void rename(int x,int y,int z,String name) {
        labels.removeIf(l -> l.x()==x && l.y()==y && l.z()==z);label(x,y,z,name);
    }
    private void interiors() {
        for(int floor=0;floor<FLOORS;floor++) {
            int y=PLINTH+floor*6+1;
            classroom(17,24,42,36,floor,1,true);classroom(54,24,79,36,floor,2,false);
            classroom(17,44,42,56,floor,3,true);classroom(54,44,79,56,floor,4,false);
            classroom(17,58,42,68,floor,5,true);classroom(54,58,79,68,floor,6,false);
            for(int z=25;z<69;z+=6) for(int x:new int[]{13,48,83}) at(LIGHT,x,y+4,z);
            for(int x=18;x<80;x+=8) at(LIGHT,x,y+4,40);
            // Successive thin stair flights preserve headroom below and reach all five levels and the roof.
            box(AIR,47,y,25,49,y+8,30);
            for(int step=0;step<6;step++) {
                box(FOUNDATION,47,y+step-1,25+step,49,y+step-1,25+step);
                box(STAIRS,47,y+step,25+step,49,y+step,25+step);
            }
        }
        // Support rooms are playable layout choices; no canonical Agwin room assignments are claimed.
        box(AIR,18,3,25,41,6,35);
        for(int x:new int[]{21,26,31,36}) {at(BED_FOOT,x,3,28);at(BED_HEAD,x,3,29);}
        box(COUNTER,19,3,33,21,3,34);at(DESK,39,3,27);at(CHAIR,39,3,28);
        rename(16,3,30,"Infirmary");
        box(AIR,55,3,25,78,6,35);at(DESK,65,3,28);at(CHAIR,65,3,29);box(BOOKS,76,3,26,78,5,26);
        rename(80,3,30,"Administration");
        box(AIR,18,3,45,41,6,55);
        box(BOOKS,18,3,45,40,5,45);box(BOOKS,18,3,55,40,5,55);
        for(int x:new int[]{22,28,34}) {at(DESK,x,3,49);at(CHAIR,x,3,50);}
        rename(16,3,50,"Library");
        box(AIR,55,3,45,78,6,55);
        for(int x:new int[]{58,64,70,75}) for(int z:new int[]{47,51}) {box(DESK,x,3,z,x+1,3,z);at(CHAIR,x,3,z+1);}
        box(COUNTER,56,3,54,77,3,54);at(FURNACE,76,3,55);at(CAULDRON,74,3,55);
        rename(80,3,50,"Cafeteria");
        rename(16,3,63,"Staff Room");rename(80,3,63,"Study Room");
        // The rear exit descends from the same raised floor as the front entrance.
        door(48,3,70);box(FLOOR,44,2,71,52,2,71);
        box(STAIRS_NORTH,44,2,72,52,2,72);box(STAIRS_NORTH,44,1,73,52,1,73);
        // A small service penthouse sits behind the pediment; roof access is a gameplay adaptation.
        box(CENTRE_PANEL,46,33,24,50,36,32);box(AIR,47,33,25,49,36,31);
        box(ROOF,46,37,24,50,37,32);
        box(AIR,47,32,25,49,34,29);box(STAIRS,47,32,30,49,32,30);
        door(48,33,32);label(52,33,34,"Roof Access");
    }
    private SchoolBlueprint.Plan build() {
        box(AIR,0,1,0,96,HEIGHT-1,96);box(FOUNDATION,0,-1,0,96,-1,96);box(GRASS,0,0,0,96,0,96);
        box(PAVING,7,0,8,89,0,77);
        // Metal boundary and grille gate; the central pedestrian opening remains usable.
        box(FENCE,4,1,5,92,2,5);box(FENCE,4,1,93,92,2,93);
        box(FENCE,4,1,6,4,2,92);box(FENCE,92,1,6,92,2,92);
        box(AIR,45,1,5,51,3,5);box(PAVING,43,0,0,53,0,18);
        for(int x:new int[]{35,60}) box(ACCENT,x,1,5,x+1,3,5);
        facade();interiors();
        for(int x:new int[]{21,75}) {box(LOG,x,1,12,x,4,12);box(LEAVES,x-2,5,10,x+2,7,14);}
        // Unseen rear grounds are a modest garden rather than an invented Turf Wars stadium or beacon tower.
        box(PAVING,44,0,74,52,0,89);box(PAVING,14,0,80,82,0,84);
        for(int x:new int[]{18,78}) {box(LOG,x,1,86,x,3,86);box(LEAVES,x-2,4,84,x+2,6,88);}
        for(int x:new int[]{30,64}) box(CHAIR,x,1,86,x+2,1,86);
        for(int x:new int[]{8,88}) for(int z:new int[]{10,42,76,90}) {box(ACCENT,x,1,z,x,2,z);at(LIGHT,x,3,z);}
        return new SchoolBlueprint.Plan(List.copyOf(boxes),List.copyOf(labels),HEIGHT,SIZE);
    }
}
