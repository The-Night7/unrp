package com.myriam.unrp.worldgen;

import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

class RemainingCampusesTest {
    private static final List<String> IDS=List.of("new_bostin","rowden","westview","wellston_university");
    @Test void everySchoolNowHasItsOwnLayoutInsteadOfTheLegacyTemplate() {
        Set<String> layouts=new java.util.HashSet<>();
        for(var school:SchoolSites.SCHOOLS) {
            var plan=SchoolBlueprint.forVersion(school,5);
            assertNotEquals(SchoolBlueprint.legacy(school),plan,school.id());
            assertTrue(layouts.add(plan.boxes().toString()),"Duplicate campus geometry: "+school.id());
        }
        assertEquals(6,layouts.size());
    }
    @Test void newBostinHasClass2BLockersLinkedBalconiesAndAnAccessibleRoof() {
        var plan=NewBostinBlueprint.create();
        assertTrue(names(plan).contains("Classroom 2-B"));
        assertEquals(1,plan.labels().stream().filter(l -> l.text().equals("Classroom 2-B")).count());
        assertEquals(LOCKER,plan.at(16,1,42));
        for(int y:new int[]{1,7,13}) assertEquals(DOOR_LOWER,plan.at(48,y,68));
        assertEquals(FENCE,plan.at(30,7,72));assertEquals(FLOOR,plan.at(48,6,71));
        assertTrue(names(plan).contains("Rooftop"));
    }
    @Test void universityHasATeachingMedicalDepartmentAndMixedHeightCampus() {
        var plan=UniversityBlueprint.create();
        assertTrue(names(plan).containsAll(Set.of("Medical Department","Medical Teaching Laboratory","Student Residences","Academic Tower")));
        assertTrue(names(plan).stream().anyMatch(s -> s.startsWith("University Library")));
        assertTrue(plan.boxes().stream().anyMatch(b -> b.material()==BREWING_STAND));
        assertTrue(plan.boxes().stream().anyMatch(b -> b.material()==BED_HEAD));
        assertTrue(plan.boxes().stream().anyMatch(b -> b.material()==TOWER_GLASS));
        assertEquals(161,plan.size());assertEquals(8,SchoolSites.school("wellston_university").floors());
        assertFalse(names(plan).stream().anyMatch(s -> s.contains("Safe House") || s.contains("Recovery Machine") || s.contains("King")));
    }
    @Test void westviewBridgeConnectsBothFloorLevelsAndRowdenHasSeparatePavilions() {
        var west=SchoolBlueprint.forVersion(SchoolSites.school("westview"),4);
        for(int y:new int[]{1,7}) {assertEquals(AIR,west.at(34,y,48));assertEquals(AIR,west.at(62,y,48));assertEquals(FLOOR,west.at(48,y-1,48));}
        assertEquals(2,WestviewV4Blueprint.BUILDINGS.size());assertEquals(3,RowdenBlueprint.BUILDINGS.size());
        assertFalse(names(RowdenBlueprint.create()).stream().anyMatch(s -> s.contains("Resort") || s.contains("Obelisk")));
    }
    @Test void stairHeadroomRemainsOpenInEveryNewBuilding() {
        for(String id:IDS) {
            var plan=SchoolBlueprint.forVersion(SchoolSites.school(id),5);
            for(var b:buildings(id)) for(int f=0;f<(b.roofAccess()?b.floors():b.floors()-1);f++) for(int step=0;step<6;step++) {
                int y=f*6+1+step,z=b.z1()+3+step;
                assertEquals(STAIRS,plan.at(b.midX(),y,z),id+" "+b.name());
                assertEquals(AIR,plan.at(b.midX(),y+1,z),id+" "+b.name()+" blocked headroom");
                assertEquals(AIR,plan.at(b.midX(),y+2,z));
            }
        }
    }
    @Test void allRoomSignsAndEveryBuildingFloorAreReachableFromTheFrontGate() {
        for(String id:IDS) {
            var plan=SchoolBlueprint.forVersion(SchoolSites.school(id),5);
            boolean[] reached=reachable(plan);int size=plan.size(),layer=size*size;
            for(var label:plan.labels()) assertTrue(reached[label.x()+label.z()*size+label.y()*layer],id+" inaccessible: "+label.text());
            for(var b:buildings(id)) for(int f=0;f<b.floors();f++)
                assertTrue(reached[b.midX()+b.midZ()*size+(f*6+1)*layer],id+" inaccessible floor "+f+" "+b.name());
        }
    }
    @Test void oldVersionsAndExistingDedicatedCampusesRemainUnchanged() {
        for(String id:IDS) {
            var school=SchoolSites.school(id);
            for(int version=1;version<=3;version++) {
                assertEquals(SchoolBlueprint.legacy(school),SchoolBlueprint.forVersion(school,version));
                assertEquals(73,SchoolBlueprint.forVersion(school,version).size());
                assertEquals(id.equals("wellston_university")?3:2,SchoolBlueprint.schoolForVersion(school,version).floors());
            }
        }
        for(String id:List.of("wellston","agwin")) assertEquals(SchoolBlueprint.forVersion(SchoolSites.school(id),3),SchoolBlueprint.forVersion(SchoolSites.school(id),5));
    }
    private static Set<String> names(SchoolBlueprint.Plan plan) {return plan.labels().stream().map(SchoolBlueprint.Label::text).collect(Collectors.toSet());}
    private static List<CampusBuilder.Building> buildings(String id) {
        return switch(id) {case "new_bostin" -> NewBostinBlueprint.BUILDINGS;case "rowden" -> RowdenBlueprint.BUILDINGS;case "westview" -> WestviewBlueprint.BUILDINGS;default -> UniversityBlueprint.BUILDINGS;};
    }
    static boolean[] reachable(SchoolBlueprint.Plan plan) {
        int size=plan.size(),layer=size*size;byte[] cells=new byte[layer*plan.height()];
        for(var b:plan.boxes()) for(int y=Math.max(0,b.y1());y<=b.y2();y++) for(int z=b.z1();z<=b.z2();z++) for(int x=b.x1();x<=b.x2();x++) cells[x+z*size+y*layer]=(byte)b.material().ordinal();
        boolean[] pass=new boolean[SchoolBlueprint.Material.values().length];
        for(var m:List.of(AIR,SIGN,WALL_SIGN,DOOR_LOWER,DOOR_UPPER,SIDE_DOOR_LOWER,SIDE_DOOR_UPPER)) pass[m.ordinal()]=true;
        boolean[] seen=new boolean[cells.length];ArrayDeque<Integer> queue=new ArrayDeque<>();
        int start=size/2+10*size+layer;seen[start]=true;queue.add(start);
        int[][] directions={{1,0},{-1,0},{0,1},{0,-1}};
        while(!queue.isEmpty()) {
            int at=queue.remove(),x=at%size,z=at%layer/size,y=at/layer;
            for(int[] d:directions) for(int dy=-1;dy<=1;dy++) {
                int nx=x+d[0],nz=z+d[1],ny=y+dy;
                if(nx<0 || nx>=size || nz<0 || nz>=size || ny<1 || ny+1>=plan.height()) continue;
                int next=nx+nz*size+ny*layer;
                if(!seen[next] && pass[cells[next]] && pass[cells[next+layer]] && !pass[cells[next-layer]]) {seen[next]=true;queue.add(next);}
            }
        }
        return seen;
    }
}
