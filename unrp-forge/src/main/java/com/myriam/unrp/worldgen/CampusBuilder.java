package com.myriam.unrp.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

/** Shared construction primitives; the individual schools own their campus arrangement and room choices. */
final class CampusBuilder {
    enum Use { CLASSROOM, OFFICE, INFIRMARY, LIBRARY, CAFETERIA, LAB, STUDY, LECTURE, COMMONS, RESIDENCE }
    record Room(String name,Use use) {}
    record Building(String name,int x1,int z1,int x2,int z2,int floors,boolean roofAccess,SchoolBlueprint.Material facade) {
        Building(String name,int x1,int z1,int x2,int z2,int floors,boolean roofAccess) {this(name,x1,z1,x2,z2,floors,roofAccess,WALL);}
        int midX() { return (x1+x2)/2; }
        int midZ() { return (z1+z2)/2; }
    }
    private final int size,height;
    private final List<SchoolBlueprint.Box> boxes=new ArrayList<>();
    private final List<SchoolBlueprint.Label> labels=new ArrayList<>();
    CampusBuilder(int size,int height,String name) {
        this.size=size;this.height=height;
        box(AIR,0,1,0,size-1,height-1,size-1);box(FOUNDATION,0,-1,0,size-1,-1,size-1);
        box(GRASS,0,0,0,size-1,0,size-1);
        int end=size-4,mid=size/2;
        box(FENCE,3,1,4,end,1,4);box(FENCE,3,1,end,end,1,end);
        box(FENCE,3,1,5,3,1,end-1);box(FENCE,end,1,5,end,1,end-1);
        box(AIR,mid-4,1,4,mid+4,2,4);path(mid-4,0,mid+4,15);
        label(mid-8,1,8,name);
    }
    void box(SchoolBlueprint.Material material,int x1,int y1,int z1,int x2,int y2,int z2) { boxes.add(new SchoolBlueprint.Box(material,x1,y1,z1,x2,y2,z2)); }
    void at(SchoolBlueprint.Material material,int x,int y,int z) { box(material,x,y,z,x,y,z); }
    void path(int x1,int z1,int x2,int z2) {box(PAVING,x1,0,z1,x2,0,z2);}
    void label(int x,int y,int z,String text) {at(SIGN,x,y,z);labels.add(new SchoolBlueprint.Label(x,y,z,text));}
    void removeLabel(int x,int y,int z) {labels.removeIf(l -> l.x()==x && l.y()==y && l.z()==z);at(AIR,x,y,z);}
    void door(int x,int y,int z) {at(DOOR_LOWER,x,y,z);at(DOOR_UPPER,x,y+1,z);}
    void tree(int x,int z) {box(LOG,x,1,z,x,3,z);box(LEAVES,x-2,4,z-2,x+2,6,z+2);}
    void lamp(int x,int z) {box(ACCENT,x,1,z,x,2,z);at(LIGHT,x,3,z);}
    void bench(int x,int z) {box(CHAIR,x,1,z,x+3,1,z);}
    void building(Building b,BiFunction<Integer,Integer,Room> rooms) {
        int top=b.floors()*6,mx=b.midX(),mz=b.midZ();
        path(b.x1()-1,b.z1()-3,b.x2()+1,b.z2()+2);
        box(b.facade(),b.x1(),1,b.z1(),b.x2(),top-1,b.z2());
        box(AIR,b.x1()+1,1,b.z1()+1,b.x2()-1,top-1,b.z2()-1);
        for(int floor=0;floor<b.floors();floor++) {
            int base=floor*6,y=base+1;
            box(FLOOR,b.x1(),base,b.z1(),b.x2(),base,b.z2());
            for(int x=b.x1()+2;x<b.x2()-1;x+=4) {
                box(GLASS,x,base+2,b.z1(),x+1,base+4,b.z1());box(GLASS,x,base+2,b.z2(),x+1,base+4,b.z2());
            }
            for(int z=b.z1()+2;z<b.z2()-1;z+=4) {
                box(GLASS,b.x1(),base+2,z,b.x1(),base+4,z+1);box(GLASS,b.x2(),base+2,z,b.x2(),base+4,z+1);
            }
            box(TRIM,b.x1(),base+5,b.z1(),b.x2(),base+5,b.z1());box(TRIM,b.x1(),base+5,b.z2(),b.x2(),base+5,b.z2());
            box(TRIM,b.x1(),base+5,b.z1(),b.x1(),base+5,b.z2());box(TRIM,b.x2(),base+5,b.z1(),b.x2(),base+5,b.z2());
            room(b.x1()+2,b.z1()+2,mx-4,mz-3,y,rooms.apply(floor,0),true);
            room(mx+4,b.z1()+2,b.x2()-2,mz-3,y,rooms.apply(floor,1),true);
            room(b.x1()+2,mz+3,mx-4,b.z2()-2,y,rooms.apply(floor,2),false);
            room(mx+4,mz+3,b.x2()-2,b.z2()-2,y,rooms.apply(floor,3),false);
            for(int x:new int[]{b.x1()+4,mx,b.x2()-4}) at(LIGHT,x,base+5,mz);
        }
        box(ROOF,b.x1(),top,b.z1(),b.x2(),top,b.z2());
        box(FENCE,b.x1(),top+1,b.z1(),b.x2(),top+1,b.z1());box(FENCE,b.x1(),top+1,b.z2(),b.x2(),top+1,b.z2());
        box(FENCE,b.x1(),top+1,b.z1(),b.x1(),top+1,b.z2());box(FENCE,b.x2(),top+1,b.z1(),b.x2(),top+1,b.z2());
        for(int f=0;f<(b.roofAccess()?b.floors():b.floors()-1);f++) {
            int y=f*6+1,z=b.z1()+3;
            box(AIR,mx-1,y,z,mx+1,y+8,z+5);
            for(int step=0;step<6;step++) {
                box(FOUNDATION,mx-1,y+step-1,z+step,mx+1,y+step-1,z+step);
                box(STAIRS,mx-1,y+step,z+step,mx+1,y+step,z+step);
            }
        }
        if(b.roofAccess()) {
            int z=b.z1()+3;
            box(b.facade(),mx-2,top+1,z-1,mx+2,top+4,z+7);box(AIR,mx-1,top+1,z,mx+1,top+4,z+6);
            box(ROOF,mx-2,top+5,z-1,mx+2,top+5,z+7);
            box(AIR,mx-1,top,z,mx+1,top+2,z+4);box(STAIRS,mx-1,top,z+5,mx+1,top,z+5);
            door(mx,top+1,z+7);label(mx+3,top+1,z+9,"Rooftop");
        }
        door(mx,1,b.z1());door(mx,1,b.z2());label(mx-5,1,b.z1()-2,b.name());
    }
    private void room(int x1,int z1,int x2,int z2,int y,Room room,boolean north) {
        box(PARTITION,x1,y,z1,x2,y+4,z2);box(AIR,x1+1,y,z1+1,x2-1,y+4,z2-1);
        int edge=north?z2:z1,mid=(x1+x2)/2;
        door(mid,y,edge);label(mid-1,y,north?edge+1:edge-1,room.name());
        at(LIGHT,mid,y+4,(z1+z2)/2);
        switch(room.use()) {
            case CLASSROOM,LECTURE -> {
                box(BOARD,x1+2,y+1,z1+1,Math.min(x1+6,x2-1),y+2,z1+1);
                for(int x=x1+2;x<x2-1;x+=3) for(int z=z1+3;z<z2-1;z+=3) {at(DESK,x,y,z);at(CHAIR,x,y,z+1);}
                if(room.use()==Use.LECTURE) at(COMPUTER,x2-2,y+1,z1+2);
            }
            case LIBRARY -> {
                box(BOOKS,x1+1,y,z1+1,x1+1,y+2,z2-1);box(BOOKS,x2-1,y,z1+1,x2-1,y+2,z2-1);
                at(DESK,mid,y,z1+3);at(COMPUTER,mid,y+1,z1+3);at(CHAIR,mid,y,z1+4);
            }
            case INFIRMARY -> {
                for(int x=x1+2;x<x2-1;x+=4) {at(BED_FOOT,x,y,z1+2);at(BED_HEAD,x,y,z1+3);}
                at(DESK,x1+2,y,z2-2);
            }
            case LAB -> {
                box(COUNTER,x1+1,y,z1+1,x2-1,y,z1+1);at(CAULDRON,x1+2,y,z1+2);at(FURNACE,x2-2,y,z1+2);
                at(BREWING_STAND,x2-3,y+1,z1+1);box(BOARD,x2,y+1,z1+2,x2,y+2,Math.min(z1+5,z2-1));
                at(DESK,mid,y,z2-3);at(COMPUTER,mid,y+1,z2-3);at(CHAIR,mid,y,z2-2);
            }
            case CAFETERIA -> {
                box(COUNTER,x1+1,y,z1+1,x2-1,y,z1+1);at(FURNACE,x2-2,y,z1+2);
                for(int x=x1+2;x<x2-1;x+=4) for(int z=z1+3;z<z2-1;z+=3) {at(DESK,x,y,z);at(CHAIR,x,y,z+1);}
            }
            case OFFICE -> {at(DESK,mid,y,z1+2);at(COMPUTER,mid,y+1,z1+2);at(CHAIR,mid,y,z1+3);box(BOOKS,x1+1,y,z1+1,x1+1,y+2,z2-1);}
            case RESIDENCE -> {
                at(BED_FOOT,x1+2,y,z1+2);at(BED_HEAD,x1+2,y,z1+3);
                at(BED_FOOT,x2-2,y,z1+2);at(BED_HEAD,x2-2,y,z1+3);
                at(DESK,mid,y,z2-3);at(CHAIR,mid,y,z2-2);at(LOCKER,x2-2,y,z2-2);
            }
            case STUDY,COMMONS -> {
                box(SOFA,x1+2,y,z1+2,Math.min(x1+4,x2-1),y,z1+2);at(DESK,mid,y,z2-3);at(CHAIR,mid,y,z2-2);
                if(room.use()==Use.STUDY) box(BOOKS,x2-1,y,z1+1,x2-1,y+2,z2-1);
            }
        }
    }
    SchoolBlueprint.Plan finish() {return new SchoolBlueprint.Plan(List.copyOf(boxes),List.copyOf(labels),height,size);}
}
