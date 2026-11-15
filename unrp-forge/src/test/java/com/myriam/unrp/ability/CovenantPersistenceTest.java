package com.myriam.unrp.ability;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CovenantPersistenceTest {
    private static final String OWNER = "00000000-0000-0000-0000-000000000001";
    private static final String VICTIM = "00000000-0000-0000-0000-000000000002";
    private static final String COLLATERAL = "00000000-0000-0000-0000-000000000003";
    private static final String COURT = "00000000-0000-0000-0000-000000000004";
    @AfterEach void clear() { Specials17.restoreJson(""); }

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

    @Test void permanentSoulsAndConstitutionsSurviveSnapshotRoundTrip() {
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

    @Test void statCollateralIsRestoredAsBothPenaltyAndBenefit() {
        Specials17.restoreJson(fixture("STAT", "4", false));
        assertEquals(-2, Specials17.statAdj(UUID.fromString(VICTIM))[4]);
        assertEquals(2, Specials17.statAdj(UUID.fromString(OWNER))[4]);
        String saved = Specials17.snapshotJson();
        Specials17.restoreJson(saved);
        assertEquals(-2, Specials17.statAdj(UUID.fromString(VICTIM))[4]);
        assertEquals(2, Specials17.statAdj(UUID.fromString(OWNER))[4]);
    }

    @Test void changingWorldDoesNotLeakCollateralIntoTheNextWorld() {
        Specials17.restoreJson(fixture("STAT", "4", false));
        Specials17.restoreJson("");
        assertArrayEquals(new int[5], Specials17.statAdj(UUID.fromString(VICTIM)));
        assertTrue(JsonParser.parseString(Specials17.snapshotJson()).getAsJsonObject().getAsJsonArray("applied").isEmpty());
    }
    @Test void reachingGodTierRemovesEarlierCollateralReturnTimers() {
        String old = fixture("BRANCH", "", false).replace("\"until\":9223372036854775807", "\"until\":12000").replace("\"tier\":4", "\"tier\":2");
        Specials17.restoreJson(old);
        Specials17.upgradeCollateral(UUID.fromString(OWNER), 6.0);
        var collateral = JsonParser.parseString(Specials17.snapshotJson()).getAsJsonObject().getAsJsonArray("applied").get(0).getAsJsonObject();
        assertEquals(Long.MAX_VALUE, collateral.get("until").getAsLong());
        assertEquals(4, collateral.get("tier").getAsInt());
    }
}
