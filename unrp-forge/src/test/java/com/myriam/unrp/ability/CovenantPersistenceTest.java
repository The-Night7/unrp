package com.myriam.unrp.ability;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CovenantPersistenceTest {
    private static final String OWNER = "00000000-0000-0000-0000-000000000001";
    private static final String VICTIM = "00000000-0000-0000-0000-000000000002";
    private static final String COLLATERAL = "00000000-0000-0000-0000-000000000003";
    private static final String COURT = "00000000-0000-0000-0000-000000000004";
    @AfterEach
    void clear() {
        Specials17.restoreJson("");
    }

    private String fixture(String kind, String key, boolean permanent) {
        String collateral = """
            {"id":"%s","court":"%s","tier":4,"pen":{"kind":"%s","key":"%s"},"amt":2,
             "from":"%s","fromName":"Victim","by":"%s","permanent":%s,"until":9223372036854775807}
            """.formatted(COLLATERAL, COURT, kind, key, VICTIM, OWNER, permanent);
        return """
            {"sovereigns":{"%s":{"stock":[%s],"signatures":{"%s":12096000},
              "constitution":{"id":"%s","owner":"%s","dim":"minecraft:overworld",
              "center":{"x":12.0,"y":64.0,"z":20.0},"tier":4,"constitutional":true,"follow":false,
              "laws":[{"id":1,"text":"No running. Penalty: Speed","law":{"viol":"SPRINT","pen":{"kind":"STAT","key":"4"}}}]}}},
              "applied":[%s],"leases":[],"vault":[],"offers":{},"arbitration":{},"immunity":{}}
            """.formatted(OWNER, collateral, VICTIM, COURT, OWNER, collateral);
    }

    @Test
    void claimedTerritoryRetainsBoundariesLawsAndSelectionAfterReload() {
        String json = """
            {"sovereigns":{"%s":{"selectedCourt":0,"jurisdictions":[{
              "owner":"%s","name":"Myriam Court","claimed":true,"district":true,"follow":false,
              "dim":"minecraft:overworld","center":{"x":120,"y":64,"z":-30},"radius":150,
              "laws":[{"id":7,"text":"No running. Penalty: Speed",
                "law":{"viol":"SPRINT","pen":{"kind":"STAT","key":"4"}}}]
            }]}}}
            """.formatted(OWNER, OWNER);
        Specials17.restoreJson(json);
        Specials17.restoreJson(Specials17.snapshotJson());
        var owner = JsonParser.parseString(Specials17.snapshotJson()).getAsJsonObject()
                .getAsJsonObject("sovereigns").getAsJsonObject(OWNER);
        var claim = owner.getAsJsonArray("jurisdictions").get(0).getAsJsonObject();
        assertTrue(claim.get("claimed").getAsBoolean());
        assertFalse(claim.get("follow").getAsBoolean());
        assertEquals("Myriam Court", claim.get("name").getAsString());
        assertEquals(150, claim.get("radius").getAsDouble());
        assertEquals(-30, claim.getAsJsonObject("center").get("z").getAsDouble());
        assertEquals(Long.MAX_VALUE, claim.get("expires").getAsLong());
        assertEquals(1, claim.getAsJsonArray("laws").size());
        assertEquals(0, owner.get("selectedCourt").getAsInt());
    }

    @Test
    void oldJurisdictionsAreNotConvertedIntoPermanentClaims() {
        Specials17.restoreJson("""
            {"sovereigns":{"%s":{"jurisdictions":[{"owner":"%s","expires":1728000}]}}}
            """.formatted(OWNER, OWNER));
        var court = JsonParser.parseString(Specials17.snapshotJson()).getAsJsonObject()
                .getAsJsonObject("sovereigns").getAsJsonObject(OWNER)
                .getAsJsonArray("jurisdictions").get(0).getAsJsonObject();
        assertFalse(court.get("claimed").getAsBoolean());
        assertEquals(1728000, court.get("expires").getAsLong());
    }

    @Test
    void permanentSoulsAndConstitutionsSurviveSnapshotRoundTrip() {
        Specials17.restoreJson(fixture("SOUL", "", true));
        String saved = Specials17.snapshotJson();
        Specials17.restoreJson("");
        Specials17.restoreJson(saved);
        var restored = JsonParser.parseString(Specials17.snapshotJson()).getAsJsonObject();
        var soul = restored.getAsJsonArray("applied").get(0).getAsJsonObject();
        assertTrue(soul.get("permanent").getAsBoolean());
        assertEquals(Long.MAX_VALUE, soul.get("until").getAsLong());
        var owner = restored.getAsJsonObject("sovereigns").getAsJsonObject(OWNER);
        assertEquals(12096000, owner.getAsJsonObject("signatures").get(VICTIM).getAsLong());
        assertEquals("minecraft:overworld", owner.getAsJsonObject("constitution").get("dim").getAsString());
        assertEquals(1, owner.getAsJsonObject("constitution").getAsJsonArray("laws").size());
    }

    @Test
    void statCollateralIsRestoredAsBothPenaltyAndBenefit() {
        Specials17.restoreJson(fixture("STAT", "4", false));
        assertEquals(-2, Specials17.statAdj(UUID.fromString(VICTIM))[4]);
        assertEquals(2, Specials17.statAdj(UUID.fromString(OWNER))[4]);
        String saved = Specials17.snapshotJson();
        Specials17.restoreJson(saved);
        assertEquals(-2, Specials17.statAdj(UUID.fromString(VICTIM))[4]);
        assertEquals(2, Specials17.statAdj(UUID.fromString(OWNER))[4]);
    }

    @Test
    void changingWorldDoesNotLeakCollateralIntoTheNextWorld() {
        Specials17.restoreJson(fixture("STAT", "4", false));
        Specials17.restoreJson("");
        assertArrayEquals(new int[5], Specials17.statAdj(UUID.fromString(VICTIM)));
        assertTrue(JsonParser.parseString(Specials17.snapshotJson())
                .getAsJsonObject()
                .getAsJsonArray("applied")
                .isEmpty());
    }
    @Test
    void reachingGodTierRemovesEarlierCollateralReturnTimers() {
        String old = fixture("BRANCH", "", false)
                         .replace("\"until\":9223372036854775807", "\"until\":12000")
                         .replace("\"tier\":4", "\"tier\":2");
        Specials17.restoreJson(old);
        Specials17.upgradeCollateral(UUID.fromString(OWNER), 6.0);
        var collateral = JsonParser.parseString(Specials17.snapshotJson())
                             .getAsJsonObject()
                             .getAsJsonArray("applied")
                             .get(0)
                             .getAsJsonObject();
        assertEquals(Long.MAX_VALUE, collateral.get("until").getAsLong());
        assertEquals(4, collateral.get("tier").getAsInt());
    }
    @Test
    void specificRulesAndUndeliveredHomeReportsSurviveReload() {
        String json = """
            {"sovereigns":{"%s":{"court":{"owner":"%s","laws":[
                {"id":8,"text":"Home report","specific":{"kind":"HOME_REPORT","target":"Myriam"},
                 "reports":{"%s":{"player":"Myriam","dimension":"minecraft:overworld","x":123.5,"y":70,"z":-99,"hasHome":true}}}
            ]}}}}
            """.formatted(OWNER, OWNER, VICTIM);
        Specials17.restoreJson(json);
        String saved = Specials17.snapshotJson();
        Specials17.restoreJson(saved);
        var root = JsonParser.parseString(Specials17.snapshotJson()).getAsJsonObject();
        var law = root.getAsJsonObject("sovereigns")
                      .getAsJsonObject(OWNER)
                      .getAsJsonObject("court")
                      .getAsJsonArray("laws")
                      .get(0)
                      .getAsJsonObject();
        assertEquals("HOME_REPORT", law.getAsJsonObject("specific").get("kind").getAsString());
        assertEquals(123.5, law.getAsJsonObject("reports").getAsJsonObject(VICTIM).get("x").getAsDouble());
        assertEquals(9, root.getAsJsonObject("sovereigns").getAsJsonObject(OWNER).get("nextLaw").getAsInt());
    }
}
