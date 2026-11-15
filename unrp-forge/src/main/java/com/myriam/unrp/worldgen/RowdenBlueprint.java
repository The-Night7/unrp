package com.myriam.unrp.worldgen;

import java.util.List;
import static com.myriam.unrp.worldgen.CampusBuilder.Use.*;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

/** Original architecture for a canon school whose campus plan is not established by the references. */
public final class RowdenBlueprint {
    public static final int SIZE=97,HEIGHT=25;
    static final List<CampusBuilder.Building> BUILDINGS=List.of(
            new CampusBuilder.Building("Rowden High School",12,18,84,42,3,false),
            new CampusBuilder.Building("Library and Arts",12,52,40,84,2,false),
            new CampusBuilder.Building("Student Commons",56,52,84,84,2,false));
    private RowdenBlueprint() {}
    public static SchoolBlueprint.Plan create() {
        var c=new CampusBuilder(SIZE,HEIGHT,"Rowden");c.path(9,10,87,16);
        c.building(BUILDINGS.get(0),(f,r) -> f==0?switch(r) {
            case 0 -> new CampusBuilder.Room("Administration",OFFICE);case 1 -> new CampusBuilder.Room("Infirmary",INFIRMARY);
            default -> new CampusBuilder.Room("Classroom 1-"+(r+1),CLASSROOM);
        }:new CampusBuilder.Room("Classroom "+(f+1)+"-"+(r+1),CLASSROOM));
        c.building(BUILDINGS.get(1),(f,r) -> new CampusBuilder.Room(f==0?"Library "+(r+1):"Arts Studio "+(r+1),f==0?LIBRARY:STUDY));
        c.building(BUILDINGS.get(2),(f,r) -> new CampusBuilder.Room(f==0?"Cafeteria "+(r+1):"Club Room "+(r+1),f==0?CAFETERIA:COMMONS));
        c.path(44,43,52,91);c.path(9,46,87,50);
        // Open landscaped quad; no resort, theme-park ride or royal battle arena is treated as a school building.
        for(int x:new int[]{44,52}) for(int z:new int[]{57,78}) c.tree(x,z);
        c.bench(44,66);c.bench(50,71);
        for(int x:new int[]{8,88}) for(int z:new int[]{12,46,88}) c.lamp(x,z);
        return c.finish();
    }
}
