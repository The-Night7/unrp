package com.myriam.unrp.worldgen;

import com.google.gson.JsonParser;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class SchoolSitesTest {
    private static SchoolSites.Site vanillaCandidate(long seed, int regionX, int regionZ) {
        var random=new WorldgenRandom(new LegacyRandomSource(0));
        random.setLargeFeatureWithSalt(seed,regionX,regionZ,SchoolSites.SALT);
        int range=SchoolSites.SPACING-SchoolSites.SEPARATION;
        return new SchoolSites.Site(regionX*SchoolSites.SPACING+random.nextInt(range),regionZ*SchoolSites.SPACING+random.nextInt(range));
    }
    @Test void sitesMatchMinecraftPlacementIncludingNegativeRegions() {
        for(long seed:new long[]{0,1,-1,123456789,Long.MIN_VALUE,Long.MAX_VALUE}) {
            var unique=new HashSet<SchoolSites.Site>();
            for(var school:SchoolSites.SCHOOLS) {
                var site=SchoolSites.site(seed,school);
                var actual=vanillaCandidate(seed,school.regionX(),school.regionZ());
                assertEquals(actual.chunkX(),site.chunkX()); assertEquals(actual.chunkZ(),site.chunkZ());
                assertTrue(unique.add(site));
                assertTrue(SchoolSites.isSite(seed,school,site.chunkX(),site.chunkZ()));
                for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) if(dx!=0 || dz!=0)
                    assertFalse(SchoolSites.isSite(seed,school,site.chunkX()+dx,site.chunkZ()+dz));
            }
            assertEquals(6,unique.size());
        }
    }
    @Test void eachSchoolHasOnlyOneEligiblePlacementEvenInOtherRegions() {
        for(var school:SchoolSites.SCHOOLS) {
            int accepted=0;
            for(int rx=-4;rx<=4;rx++) for(int rz=-4;rz<=4;rz++) {
                var candidate=vanillaCandidate(987654321,rx,rz);
                if(SchoolSites.isSite(987654321,school,candidate.chunkX(),candidate.chunkZ())) accepted++;
            }
            assertEquals(1,accepted,school.id());
        }
    }
    @Test void worldgenFilesAgreeWithThePlannerAndExcludeOrganizations() throws Exception {
        var data=Path.of("src/main/resources/data/unrp");
        var schools=JsonParser.parseString(Files.readString(data.resolve("unodex/schools.json"))).getAsJsonObject().getAsJsonArray("schools");
        var ids=new HashSet<String>();
        for(var value:schools) { var school=value.getAsJsonObject(); if(school.get("kind").getAsString().equals("school")) ids.add(school.get("id").getAsString()); }
        assertEquals(ids,SchoolSites.SCHOOLS.stream().map(SchoolSites.School::id).collect(java.util.stream.Collectors.toSet()));
        for(var school:SchoolSites.SCHOOLS) {
            var structure=JsonParser.parseString(Files.readString(data.resolve("worldgen/structure/"+school.id()+".json"))).getAsJsonObject();
            assertEquals("unrp:school",structure.get("type").getAsString());
            assertEquals(school.id(),structure.get("school").getAsString());
            var set=JsonParser.parseString(Files.readString(data.resolve("worldgen/structure_set/"+school.id()+".json"))).getAsJsonObject();
            var placement=set.getAsJsonObject("placement");
            assertEquals(SchoolSites.SPACING,placement.get("spacing").getAsInt());
            assertEquals(SchoolSites.SEPARATION,placement.get("separation").getAsInt());
            assertEquals(SchoolSites.SALT,placement.get("salt").getAsInt());
            assertEquals("unrp:"+school.id(),set.getAsJsonArray("structures").get(0).getAsJsonObject().get("structure").getAsString());
        }
    }
}
