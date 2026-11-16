package com.myriam.unrp.worldgen;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

class SchoolUpgradeTest {
    @Test void compiledFinalGridMatchesOrderedPaintingIncludingEmptyAndNegativeCells() {
        var plan=SchoolBlueprint.forVersion(SchoolSites.school("new_bostin"),5);
        var grid=new SchoolPlanGrid(plan);
        for(int x=0;x<plan.size();x+=3)for(int z=0;z<plan.size();z+=3)for(int y=-1;y<plan.height();y+=2)
            assertEquals(plan.at(x,y,z),grid.at(x,y,z));
        assertEquals(AIR,grid.at(-1,1,0));assertEquals(AIR,grid.at(0,plan.height(),0));
    }
    @Test void gradeDoesNotLiftTheWholeCampusOntoAnIsolatedPeak() {
        assertEquals(71,SchoolTerrain.floor(63,new int[]{69,70,70,71,71,72,120},-62,250));
        assertEquals(64,SchoolTerrain.floor(63,new int[]{40,42,45,46,48},-62,250));
        assertEquals(100,SchoolTerrain.floor(63,new int[]{150,160,170},-62,100));
    }
    @Test void apronMeetsTheOuterTerrainAndInnerCampusExactlyForCutAndFill() {
        for(int natural:new int[]{30,64,100,170}) {
            assertEquals(natural,SchoolTerrain.apronHeight(natural,70,0,8));
            assertEquals(70,SchoolTerrain.apronHeight(natural,70,8,8));
            int middle=SchoolTerrain.apronHeight(natural,70,4,8);
            assertTrue(middle>=Math.min(natural,70)&&middle<=Math.max(natural,70));
        }
    }
    @Test void newCampusesKeepEverySignAndRoomAccessibleAndHaveRealExteriorDepth() {
        for(var school:SchoolSites.SCHOOLS) {
            var old=SchoolBlueprint.forVersion(school,5);var plan=SchoolBlueprint.create(school);
            assertEquals(old.size()+16,plan.size());assertEquals(old.height(),plan.height());
            assertEquals(old.labels().size(),plan.labels().size());
            var grid=new SchoolPlanGrid(plan);
            boolean[] seen=RemainingCampusesTest.reachable(plan);int size=plan.size(),layer=size*size;
            for(var label:plan.labels()) {
                boolean reachable=seen[label.x()+label.z()*size+label.y()*layer];
                if(grid.at(label.x(),label.y(),label.z())==WALL_SIGN) {
                    for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)for(int dy=-5;dy<=0;dy++) {
                        int x=label.x()+dx,z=label.z()+dz,y=label.y()+dy;
                        if(x>=0&&z>=0&&x<size&&z<size&&y>0)reachable|=seen[x+z*size+y*layer];
                    }
                }
                assertTrue(reachable,school.id()+" inaccessible "+label.text());
                assertTrue(grid.at(label.x(),label.y(),label.z())==SIGN||grid.at(label.x(),label.y(),label.z())==WALL_SIGN,school.id()+" overwritten sign");
            }
            assertTrue(plan.boxes().stream().anyMatch(b->b.material()==CORNICE),school.id());
            assertTrue(plan.boxes().stream().anyMatch(b->b.material()==PATH_EDGE),school.id());
            for(var label:old.labels())assertEquals(label.text(),plan.labels().get(old.labels().indexOf(label)).text());
        }
    }
}
