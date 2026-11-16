package com.myriam.unrp.worldgen;

import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

class WellstonBlueprintTest {
    @Test void canonLocationsReplaceTheGenericCampusAndRemainInsideTheFootprint() {
        var plan=SchoolBlueprint.forVersion(SchoolSites.school("wellston"),5);
        assertEquals(129,plan.size());
        Set<String> labels=plan.labels().stream().map(SchoolBlueprint.Label::text).collect(Collectors.toSet());
        assertTrue(labels.containsAll(Set.of("Wallik Hall","Whesley Hall","Manner Hall","Safe House - Room 224",
                "Wellston Weekly - Press Room","Library","Cafeteria","Infirmary","Headmaster's Office",
                "Boys' Dormitories","Girls' Dormitories","Rooftop","Wellston Private High School")));
        assertEquals(3,SchoolSites.school("wellston").floors());
        assertEquals(ROOF,plan.at(80,18,30));
        assertEquals(FENCE,plan.at(80,19,16));
        assertEquals(AIR,plan.at(80,25,30)); // No clock tower above the roof.
        assertEquals(SOFA,plan.at(42,7,35));
        assertEquals(COMPUTER,plan.at(79,8,35));
        assertEquals(BED_FOOT,plan.at(16,1,90));
        assertEquals(BED_HEAD,plan.at(16,1,91));
        assertEquals(BED_FOOT,plan.at(86,1,90));
    }
    @Test void allMainFloorsAndRooftopHaveAContinuousStairFlight() {
        var plan=WellstonBlueprint.create();
        for(int floor=0;floor<3;floor++) for(int step=0;step<6;step++) {
            int y=floor*6+1+step,z=19+step;
            assertEquals(STAIRS,plan.at(64,y,z));
            assertEquals(AIR,plan.at(64,y+1,z),"Blocked staircase floor "+floor+" step "+step);
            assertEquals(AIR,plan.at(64,y+2,z));
        }
        assertEquals(DOOR_LOWER,plan.at(64,19,26));
        assertEquals(DOOR_UPPER,plan.at(64,20,26));
    }
    @Test void keyLocationsAreReachableFromTheFrontGateWithTwoBlockHeadroom() {
        var plan=WellstonBlueprint.create();
        int size=plan.size(),layer=size*size;
        byte[] cells=new byte[layer*plan.height()];
        for(var box:plan.boxes()) for(int y=Math.max(0,box.y1());y<=box.y2();y++)
            for(int z=box.z1();z<=box.z2();z++) for(int x=box.x1();x<=box.x2();x++) cells[x+z*size+y*layer]=(byte)box.material().ordinal();
        boolean[] reached=new boolean[cells.length];
        ArrayDeque<Integer> queue=new ArrayDeque<>();
        int start=64+8*size+layer; reached[start]=true;queue.add(start);
        while(!queue.isEmpty()) {
            int at=queue.remove(),y=at/layer,z=at%layer/size,x=at%size;
            for(int[] direction:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) for(int dy=-1;dy<=1;dy++) {
                int nx=x+direction[0],nz=z+direction[1],ny=y+dy;
                if(nx<0 || nx>=size || nz<0 || nz>=size || ny<1 || ny+1>=plan.height()) continue;
                int next=nx+nz*size+ny*layer;
                if(!reached[next] && passable(cells[next]) && passable(cells[next+layer]) && !passable(cells[next-layer])) {
                    reached[next]=true;queue.add(next);
                }
            }
        }
        for(int[] point:new int[][]{{24,1,38},{48,7,35},{82,7,38},{40,1,64},{100,1,64},{29,1,100},{99,1,100},{29,7,100},{99,7,100},{74,19,30}})
            assertTrue(reached[point[0]+point[2]*size+point[1]*layer],"Unreachable campus location "+java.util.Arrays.toString(point));
    }
    @Test void previouslySavedWellstonPiecesRetainTheOriginalSmallDesign() {
        var plan=SchoolBlueprint.legacy(SchoolSites.school("wellston"));
        assertEquals(73,plan.size());
        assertEquals(BOOKS,plan.at(9,7,40));
        assertEquals(STAIRS,plan.at(36,6,20));
    }
    private static boolean passable(byte material) {
        var m=SchoolBlueprint.Material.values()[material];
        return m==AIR || m==SIGN || m==WALL_SIGN || m==DOOR_LOWER || m==DOOR_UPPER;
    }
}
