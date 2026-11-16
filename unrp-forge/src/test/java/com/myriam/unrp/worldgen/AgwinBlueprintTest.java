package com.myriam.unrp.worldgen;

import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

class AgwinBlueprintTest {
    @Test void depictedFacadeAndClassroomReplaceTheGenericBlueCampus() {
        var plan=SchoolBlueprint.forVersion(SchoolSites.school("agwin"),5);
        assertEquals(97,plan.size());assertEquals(5,SchoolSites.school("agwin").floors());
        assertEquals("agwin",SchoolSites.school("agwin").palette());
        assertEquals(AGWIN_YELLOW,plan.at(11,10,22));assertEquals(AGWIN_PINK,plan.at(65,10,22));
        assertEquals(CENTRE_PANEL,plan.at(35,10,20));
        assertEquals(ACCENT,plan.at(40,7,16)); // Entrance canopy.
        assertEquals(WINDOW_FRAME,plan.at(12,28,22));assertEquals(GLASS,plan.at(13,28,22));
        Set<String> names=plan.labels().stream().map(SchoolBlueprint.Label::text).collect(Collectors.toSet());
        assertTrue(names.containsAll(Set.of("Agwin","Agwin Private High School","Classroom 2-3")));
        assertFalse(names.stream().anyMatch(s -> s.contains("Safe House") || s.contains("Wellston") || s.contains("Dormitor")));
        assertEquals(BOARD,plan.at(20,10,45));assertEquals(DESK,plan.at(20,9,47));assertEquals(CHAIR,plan.at(20,9,48));
    }
    @Test void fiveContinuousFlightsHaveHeadroomAndReachTheRoof() {
        var plan=AgwinBlueprint.create();
        for(int floor=0;floor<5;floor++) for(int step=0;step<6;step++) {
            int y=3+floor*6+step,z=25+step;
            assertEquals(STAIRS,plan.at(48,y,z));
            assertEquals(AIR,plan.at(48,y+1,z));assertEquals(AIR,plan.at(48,y+2,z));
        }
        assertEquals(DOOR_LOWER,plan.at(48,33,32));assertEquals(DOOR_UPPER,plan.at(48,34,32));
    }
    @Test void roomsAllStoreysAndRearGardenAreReachableThroughTheRaisedEntrance() {
        var plan=AgwinBlueprint.create();int size=plan.size(),layer=size*size;
        byte[] cells=new byte[layer*plan.height()];
        for(var box:plan.boxes()) for(int y=Math.max(0,box.y1());y<=box.y2();y++)
            for(int z=box.z1();z<=box.z2();z++) for(int x=box.x1();x<=box.x2();x++) cells[x+z*size+y*layer]=(byte)box.material().ordinal();
        boolean[] seen=new boolean[cells.length];ArrayDeque<Integer> queue=new ArrayDeque<>();
        int start=48+10*size+layer;seen[start]=true;queue.add(start);
        while(!queue.isEmpty()) {
            int at=queue.remove(),x=at%size,z=at%layer/size,y=at/layer;
            for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) for(int dy=-1;dy<=1;dy++) {
                int nx=x+d[0],nz=z+d[1],ny=y+dy;
                if(nx<0 || nx>=size || nz<0 || nz>=size || ny<1 || ny+1>=plan.height()) continue;
                int next=nx+nz*size+ny*layer;
                if(!seen[next] && passable(cells[next]) && passable(cells[next+layer]) && !passable(cells[next-layer])) {seen[next]=true;queue.add(next);}
            }
        }
        for(int[] p:new int[][]{{25,3,32},{25,3,52},{66,3,50},{25,9,48},{48,3,40},{48,9,40},{48,15,40},{48,21,40},{48,27,40},{64,33,50},{48,1,80}})
            assertTrue(seen[p[0]+p[2]*size+p[1]*layer],"Unreachable Agwin point "+java.util.Arrays.toString(p));
    }
    @Test void oldAgwinAndWellstonVersionsKeepTheirOriginalPlansAndPalettes() {
        var agwin=SchoolSites.school("agwin");
        for(int version:new int[]{1,2}) {
            var old=SchoolBlueprint.forVersion(agwin,version);
            assertEquals(73,old.size());assertEquals(BOOKS,old.at(9,7,40));
            assertEquals("blue",SchoolBlueprint.schoolForVersion(agwin,version).palette());
            assertEquals(2,SchoolBlueprint.schoolForVersion(agwin,version).floors());
        }
        var wellston=SchoolSites.school("wellston");
        assertEquals(73,SchoolBlueprint.forVersion(wellston,1).size());
        assertEquals(SchoolBlueprint.forVersion(wellston,2),SchoolBlueprint.forVersion(wellston,3));
        assertEquals("wellston",SchoolBlueprint.schoolForVersion(wellston,2).palette());
    }
    private static boolean passable(byte value) {
        var m=SchoolBlueprint.Material.values()[value];
        return m==AIR || m==SIGN || m==WALL_SIGN || m==DOOR_LOWER || m==DOOR_UPPER || m==SIDE_DOOR_LOWER || m==SIDE_DOOR_UPPER;
    }
}
