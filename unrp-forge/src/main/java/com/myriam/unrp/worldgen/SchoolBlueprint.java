package com.myriam.unrp.worldgen;

import java.util.ArrayList;
import java.util.List;

/** Procedural campus geometry. Ordered boxes are painted from first to last; no Minecraft state. */
public final class SchoolBlueprint {
    public static final int SIZE = 73, CURRENT_LAYOUT_VERSION = 3;
    public enum Material { AIR, FOUNDATION, PAVING, GRASS, WALL, FLOOR, ROOF, ACCENT, GLASS,
        FENCE, LEAVES, LOG, LIGHT, WATER, WHITE, COURT, BOOKS, DESK, CHAIR, STAIRS,
        DOOR_LOWER, DOOR_UPPER, BED_HEAD, BED_FOOT, COUNTER, FURNACE, CAULDRON, BOARD, SIGN,
        TRIM, PARTITION, LOCKER, COMPUTER, SOFA, WALL_SIGN,
        CAFETERIA_TABLE, CAFETERIA_CHAIR, CAFETERIA_COUNTER, CAFETERIA_WALL,
        AGWIN_YELLOW, AGWIN_PINK, CENTRE_PANEL, WINDOW_FRAME, WINDOW_MULLION, HALL_TRIM,
        SIDE_DOOR_LOWER, SIDE_DOOR_UPPER, STAIRS_NORTH }
    public record Box(Material material, int x1, int y1, int z1, int x2, int y2, int z2) {
        public Box {
            if (x1 > x2 || y1 > y2 || z1 > z2) throw new IllegalArgumentException("Inverted box");
        }
        public boolean contains(int x, int y, int z) {
            return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
        }
    }
    public record Label(int x, int y, int z, String text) {}
    public record Plan(List<Box> boxes, List<Label> labels, int height, int size) {
        public Plan(List<Box> boxes, List<Label> labels, int height) { this(boxes,labels,height,SIZE); }
        public Material at(int x, int y, int z) {
            Material result = Material.AIR;
            for (Box b : boxes) if (b.contains(x, y, z)) result = b.material();
            return result;
        }
    }
    private final List<Box> boxes = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private final SchoolSites.School school;
    private SchoolBlueprint(SchoolSites.School school) { this.school = school; }
    public static Plan create(SchoolSites.School school) { return forVersion(school,CURRENT_LAYOUT_VERSION); }
    public static Plan forVersion(SchoolSites.School school,int version) {
        if(school.id().equals("wellston") && version>=2) return WellstonBlueprint.create();
        if(school.id().equals("agwin") && version>=3) return AgwinBlueprint.create();
        return legacy(school);
    }
    public static SchoolSites.School schoolForVersion(SchoolSites.School school,int version) {
        if(school.id().equals("wellston") && version<2) return new SchoolSites.School(school.id(),school.name(),school.regionX(),school.regionZ(),2,"gold");
        if(school.id().equals("agwin") && version<3) return new SchoolSites.School(school.id(),school.name(),school.regionX(),school.regionZ(),2,"blue");
        return school;
    }
    /** Existing serialized campus pieces retain the layout and palette with which they started. */
    public static Plan legacy(SchoolSites.School school) {
        return new SchoolBlueprint(schoolForVersion(school,1)).build();
    }
    private void box(Material m, int x1, int y1, int z1, int x2, int y2, int z2) { boxes.add(new Box(m,x1,y1,z1,x2,y2,z2)); }
    private void block(Material m, int x, int y, int z) { box(m,x,y,z,x,y,z); }
    private void door(int x, int y, int z) { block(Material.DOOR_LOWER,x,y,z); block(Material.DOOR_UPPER,x,y+1,z); }
    private void label(int x, int y, int z, String text) { block(Material.SIGN,x,y,z); labels.add(new Label(x,y,z,text)); }
    private void shell(int x1, int z1, int x2, int z2) {
        int roof = school.floors() * 6;
        box(Material.WALL,x1,1,z1,x2,roof-1,z2);
        box(Material.AIR,x1+1,1,z1+1,x2-1,roof-1,z2-1);
        for (int f=0;f<school.floors();f++) {
            int y=f*6;
            box(Material.FLOOR,x1,y,z1,x2,y,z2);
            for (int x=x1+3;x<x2-1;x+=4) {
                box(Material.GLASS,x,y+2,z1,x+1,y+3,z1);
                box(Material.GLASS,x,y+2,z2,x+1,y+3,z2);
            }
            for (int z=z1+3;z<z2-1;z+=4) {
                box(Material.GLASS,x1,y+2,z,x1,y+3,z+1);
                box(Material.GLASS,x2,y+2,z,x2,y+3,z+1);
            }
            box(Material.ACCENT,x1,y+5,z1,x2,y+5,z1);
            box(Material.ACCENT,x1,y+5,z2,x2,y+5,z2);
        }
        box(Material.ROOF,x1-1,roof,z1-1,x2+1,roof,z2+1);
        box(Material.ACCENT,x1,roof+1,z1,x2,roof+1,z1);
        box(Material.ACCENT,x1,roof+1,z2,x2,roof+1,z2);
    }
    private void room(int x1, int z1, int x2, int z2, int y, String name, boolean classroom) {
        int wallZ=z1==13?z2:z1;
        box(Material.WALL,x1,y,wallZ,x2,y+4,wallZ);
        int dx=(x1+x2)/2;
        door(dx,y,wallZ);
        label(dx-2,y,wallZ==z1?wallZ-1:wallZ+1,name);
        block(Material.LIGHT,dx,y+4,(z1+z2)/2);
        if (classroom) {
            box(Material.BOARD,x1+2,y+1,z1+1,x1+6,y+2,z1+1);
            for(int x=x1+2;x<x2-1;x+=4) for(int z=z1+3;z<z2-1;z+=3) {
                block(Material.DESK,x,y,z); block(Material.CHAIR,x,y,z+1);
            }
        }
    }
    private Plan build() {
        int top=school.floors()*6;
        box(Material.AIR,0,1,0,72,top+9,72);
        box(Material.FOUNDATION,0,-1,0,72,-1,72);
        box(Material.GRASS,0,0,0,72,0,72);
        box(Material.PAVING,5,0,8,67,0,66);
        box(Material.FENCE,2,1,2,70,1,2); box(Material.FENCE,2,1,70,70,1,70);
        box(Material.FENCE,2,1,3,2,1,69); box(Material.FENCE,70,1,3,70,1,69);
        box(Material.AIR,33,1,2,39,1,2);
        box(Material.PAVING,33,0,0,39,0,12);
        shell(8,12,64,31); shell(8,32,24,55); shell(48,32,64,55);
        // Join the three wings with accessible corridors on every floor.
        for(int f=0;f<school.floors();f++) {
            int y=f*6+1;
            box(Material.AIR,15,y,31,20,y+3,32); box(Material.AIR,52,y,31,57,y+3,32);
            box(Material.WALL,31,y,13,31,y+4,30);
            box(Material.WALL,41,y,13,41,y+4,30);
            box(Material.AIR,9,y,21,63,y+3,24);
            for(int x:new int[]{8,41}) {
                int x1=x+1,x2=x+22;
                room(x1,13,x2,20,y,f==0?(x==8?"Classroom A":"Classroom B"):"Classroom "+(f*2+(x==8?1:2)),true);
                room(x1,25,x2,30,y,f==0?(x==8?"Infirmary":"Staff Office"):(x==8?"Science Lab":"Study Room"),f>0);
            }
            // Lamps in the central and cross corridors.
            for(int z:new int[]{14,23,29}) block(Material.LIGHT,36,y+4,z);
            // Stair flights connect all floors, with headroom through the upper slab.
            if(f<school.floors()-1) {
                box(Material.AIR,34,y,15,38,y+8,20);
                for(int step=0;step<6;step++) {
                    box(Material.FOUNDATION,34,y-1+step,15+step,38,y-1+step,15+step);
                    box(Material.STAIRS,34,y+step,15+step,38,y+step,15+step);
                }
            }
        }
        // Main entrance and courtyard exits are two blocks tall.
        for(int x=35;x<=37;x++) door(x,1,12);
        box(Material.AIR,34,1,31,38,3,31);
        box(Material.AIR,24,1,35,24,3,37); box(Material.AIR,48,1,35,48,3,37);
        label(31,1,7,school.name());
        for(int x:new int[]{28,44}) { box(Material.LOG,x,1,6,x,3,6); block(Material.LIGHT,x,4,6); }
        // Cafeteria with a kitchen and dining tables.
        for(int x:new int[]{12,18}) for(int z:new int[]{36,41}) {
            box(Material.DESK,x,1,z,x+2,1,z); block(Material.CHAIR,x,1,z+1); block(Material.CHAIR,x+2,1,z+1);
        }
        box(Material.COUNTER,10,1,47,22,1,47); block(Material.FURNACE,11,1,52); block(Material.CAULDRON,14,1,52);
        label(21,1,34,"Cafeteria");
        // The upstairs left wing is a furnished library.
        box(Material.BOOKS,9,7,34,9,9,53); box(Material.BOOKS,23,7,34,23,9,53);
        for(int z:new int[]{37,44,51}) { box(Material.BOOKS,11,7,z,15,8,z); block(Material.DESK,20,7,z); block(Material.CHAIR,20,7,z+1); }
        block(Material.LIGHT,16,11,43); label(21,7,34,"Library");
        // Gymnasium and separate outdoor sports court.
        box(Material.COURT,49,0,34,63,0,53); box(Material.WHITE,49,0,43,63,0,43);
        for(int z:new int[]{35,52}) { box(Material.WHITE,54,3,z,58,4,z); block(Material.ACCENT,56,3,z+ (z==35?1:-1)); }
        block(Material.LIGHT,56,5,43); label(61,1,34,"Gymnasium");
        box(Material.COURT,27,0,43,45,0,63);
        box(Material.WHITE,27,0,43,45,0,43); box(Material.WHITE,27,0,63,45,0,63);
        box(Material.WHITE,27,0,43,27,0,63); box(Material.WHITE,45,0,43,45,0,63);
        box(Material.WHITE,27,0,53,45,0,53);
        for(int z:new int[]{45,61}) { box(Material.FENCE,36,1,z,36,3,z); box(Material.WHITE,34,4,z,38,5,z); block(Material.ACCENT,36,3,z+(z==45?1:-1)); }
        // Infirmary beds and staff furniture.
        for(int x:new int[]{11,15,19}) { block(Material.BED_FOOT,x,1,27); block(Material.BED_HEAD,x,1,28); }
        for(int x:new int[]{44,50,56,60}) { block(Material.DESK,x,1,27); block(Material.CHAIR,x,1,28); }
        // Courtyard fountain, benches, landscaping and perimeter lighting.
        box(Material.ACCENT,33,0,35,39,0,39); box(Material.WATER,34,0,36,38,0,38); block(Material.ACCENT,36,1,37);
        for(int x:new int[]{27,44}) box(Material.CHAIR,x,1,34,x,1,38);
        for(int x:new int[]{5,67}) for(int z:new int[]{8,28,48,66}) {
            box(Material.LOG,x,1,z,x,3,z); box(Material.LEAVES,x-1,4,z-1,x+1,5,z+1);
        }
        for(int x:new int[]{10,22,50,62}) for(int z:new int[]{9,58}) block(Material.LIGHT,x,1,z);
        // A clock tower gives Wellston and its university their own skyline.
        if(school.palette().equals("gold") || school.floors()==3) {
            box(Material.WALL,32,top+1,25,40,top+7,30);
            box(Material.ACCENT,31,top+8,24,41,top+8,31);
            box(Material.WHITE,34,top+3,25,38,top+6,25);
            box(Material.ACCENT,36,top+4,24,36,top+6,24); block(Material.ACCENT,37,top+4,24);
        }
        // Re-establish unobstructed landings after overlapping upper-floor room work.
        for(int f=1;f<school.floors();f++) box(Material.AIR,34,f*6+1,21,38,f*6+3,23);
        return new Plan(List.copyOf(boxes),List.copyOf(labels),top+10);
    }
}
