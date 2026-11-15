package com.myriam.unrp.worldgen;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

class SchoolBlueprintTest {
    @Test void geometryStaysInsideItsChunkClippedStructureBounds() {
        for(var school:SchoolSites.SCHOOLS) {
            var plan=SchoolBlueprint.create(school);
            for(var box:plan.boxes()) {
                assertTrue(box.x1()>=0 && box.z1()>=0);
                assertTrue(box.x2()<SchoolBlueprint.SIZE && box.z2()<SchoolBlueprint.SIZE);
                assertTrue(box.y1()>=-1 && box.y2()<plan.height());
            }
            assertTrue(plan.labels().stream().anyMatch(label -> label.text().equals(school.name())));
            for(var label:plan.labels()) assertEquals(SIGN,plan.at(label.x(),label.y(),label.z()),"Label overwritten: "+label.text());
        }
    }
    @Test void entranceCorridorsAndStairHeadroomStayOpenOnEveryFloor() {
        for(var school:SchoolSites.SCHOOLS) {
            var plan=SchoolBlueprint.create(school);
            assertEquals(DOOR_LOWER,plan.at(36,1,12));
            assertEquals(DOOR_UPPER,plan.at(36,2,12));
            assertEquals(AIR,plan.at(36,1,31));
            for(int f=0;f<school.floors();f++) {
                assertEquals(AIR,plan.at(36,f*6+1,23));
                assertEquals(AIR,plan.at(17,f*6+1,31));
                assertEquals(AIR,plan.at(54,f*6+1,32));
                if(f<school.floors()-1) for(int step=0;step<6;step++) {
                    int y=f*6+1+step,z=15+step;
                    assertEquals(STAIRS,plan.at(36,y,z));
                    assertEquals(AIR,plan.at(36,y+1,z),"Stair headroom at "+school.id()+" floor "+f);
                    assertEquals(AIR,plan.at(36,y+2,z));
                }
            }
        }
    }
    @Test void essentialRoomsAreFurnishedAndBedsHaveBothHalves() {
        for(var school:SchoolSites.SCHOOLS) {
            var plan=SchoolBlueprint.create(school);
            assertEquals(BOOKS,plan.at(9,7,40));
            assertEquals(DESK,plan.at(12,1,36));
            assertEquals(FURNACE,plan.at(11,1,52));
            assertEquals(BED_FOOT,plan.at(11,1,27));
            assertEquals(BED_HEAD,plan.at(11,1,28));
            assertEquals(COURT,plan.at(30,0,50));
        }
    }
}
