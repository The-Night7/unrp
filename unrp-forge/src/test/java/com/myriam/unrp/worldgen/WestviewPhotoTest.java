package com.myriam.unrp.worldgen;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

class WestviewPhotoTest {
    @Test void photosProduceALargeGlazedCampusWithLowPavilionsAndLeafMural() {
        var plan=WestviewBlueprint.create();
        assertEquals(177,plan.size());assertEquals(8,WestviewBlueprint.BUILDINGS.size());
        assertEquals(2,WestviewBlueprint.BUILDINGS.stream().filter(b -> b.floors()==1).count());
        assertEquals(TOWER_GLASS,plan.at(12,2,20));
        assertEquals(WALL,plan.at(10,2,22)); // Plain concrete end wall, away from its narrow stairwell slit.
        assertEquals(ROOF,plan.at(20,26,25)); // Low-pitched blue-grey cap on the four-storey west block.
        assertTrue(plan.boxes().stream().anyMatch(b -> b.material()==MURAL_GREEN));
        assertTrue(plan.boxes().stream().anyMatch(b -> b.material()==MURAL_LIGHT));
        assertEquals(ROAD_DARK,plan.at(168,0,100));assertEquals(WHITE,plan.at(122,0,10));
        assertTrue(plan.labels().stream().anyMatch(l -> l.text().equals("Reception and Administration")));
    }
    @Test void gatewayHasTallConcretePiersLintelAndAnOpenPedestrianPassage() {
        var plan=WestviewBlueprint.create();
        assertEquals(ACCENT,plan.at(74,13,8));assertEquals(ACCENT,plan.at(102,13,8));
        assertEquals(ACCENT,plan.at(88,10,8));
        assertEquals(GATE_EW,plan.at(81,2,8));assertEquals(GATE_NS,plan.at(83,2,11));
        assertEquals(PLAZA_PAVERS,plan.at(88,0,8));
        for(int z=0;z<=21;z++) for(int y:new int[]{1,2}) assertEquals(AIR,plan.at(88,y,z),"Blocked approach at "+z);
        assertEquals(DOOR_LOWER,plan.at(88,1,22));
    }
    @Test void glazedFrontLinksWorkOnAllThreeLevelsAndTheCoveredSpineStaysOpen() {
        var plan=WestviewBlueprint.create();
        for(int floor=0;floor<3;floor++) {
            int y=floor*6;
            assertEquals(FLOOR,plan.at(68,y,33));
            for(int x:new int[]{64,72,104,112}) {assertEquals(AIR,plan.at(x,y+1,33));assertEquals(AIR,plan.at(x,y+2,33));}
        }
        assertEquals(ROOF,plan.at(88,5,100));
        assertEquals(AIR,plan.at(88,1,100));assertEquals(AIR,plan.at(88,2,100));
        boolean[] seen=RemainingCampusesTest.reachable(plan);int size=plan.size(),layer=size*size;
        for(int[] p:new int[][]{{68,7,33},{108,13,33},{88,1,100},{33,1,135},{115,1,120}})
            assertTrue(seen[p[0]+p[2]*size+p[1]*layer],"Unreachable reference feature "+java.util.Arrays.toString(p));
    }
    @Test void savedHCampusAndAllOtherSchoolsArePreserved() {
        var westview=SchoolSites.school("westview");
        assertEquals(WestviewV4Blueprint.create(),SchoolBlueprint.forVersion(westview,4));
        assertEquals(97,SchoolBlueprint.forVersion(westview,4).size());
        assertEquals("westview",SchoolBlueprint.schoolForVersion(westview,4).palette());
        assertEquals(2,SchoolBlueprint.schoolForVersion(westview,4).floors());
        assertEquals(73,SchoolBlueprint.forVersion(westview,3).size());
        assertEquals("cyan",SchoolBlueprint.schoolForVersion(westview,3).palette());
        for(String id:List.of("wellston","agwin","new_bostin","rowden","wellston_university")) {
            var school=SchoolSites.school(id);
            assertEquals(SchoolBlueprint.forVersion(school,4),SchoolBlueprint.forVersion(school,5));
            assertEquals(SchoolBlueprint.schoolForVersion(school,4),SchoolBlueprint.schoolForVersion(school,5));
        }
    }
}
