package com.myriam.unrp.worldgen;

import java.util.List;
import static com.myriam.unrp.worldgen.CampusBuilder.Use.*;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

/** School/rooftop and class 2-B references, with a compressed brick teaching block and playable balconies. */
public final class NewBostinBlueprint {
    public static final int SIZE=97,HEIGHT=27;
    static final CampusBuilder.Building MAIN=new CampusBuilder.Building("New Bostin High School",10,18,86,68,3,true);
    static final List<CampusBuilder.Building> BUILDINGS=List.of(MAIN);
    private NewBostinBlueprint() {}
    public static SchoolBlueprint.Plan create() {
        var c=new CampusBuilder(SIZE,HEIGHT,"New Bostin");
        c.path(8,10,88,16);
        c.building(MAIN,(f,r) -> {
            if(f==1 && r==0) return new CampusBuilder.Room("Classroom 2-B",CLASSROOM);
            if(f==0) return switch(r) {
                case 0 -> new CampusBuilder.Room("Administration",OFFICE);
                case 1 -> new CampusBuilder.Room("Infirmary",INFIRMARY);
                case 2 -> new CampusBuilder.Room("Library",LIBRARY);
                default -> new CampusBuilder.Room("Cafeteria",CAFETERIA);
            };
            return new CampusBuilder.Room("Classroom "+(f+1)+"-"+(char)('A'+r+(f==1?1:0)),CLASSROOM);
        });
        // The flashback's visible exterior walkways are interpreted as linked rear balconies.
        for(int f=0;f<3;f++) {
            int y=f*6;
            c.box(FLOOR,10,y,69,86,y,72);c.box(FENCE,10,y+1,72,86,y+1,72);
            c.box(FENCE,10,y+1,69,10,y+1,71);c.box(FENCE,86,y+1,69,86,y+1,71);
            c.door(48,y+1,68);
            for(int x=16;x<82;x+=3) if(x<43 || x>53) c.at(LOCKER,x,y+1,42);
        }
        c.box(AIR,46,1,72,50,2,72);c.path(46,73,50,90);
        c.path(14,80,82,84);
        for(int x:new int[]{16,80}) {c.tree(x,10);c.tree(x,87);}
        c.bench(28,87);c.bench(64,87);
        for(int x:new int[]{7,89}) for(int z:new int[]{12,44,78,90}) c.lamp(x,z);
        return c.finish();
    }
}
