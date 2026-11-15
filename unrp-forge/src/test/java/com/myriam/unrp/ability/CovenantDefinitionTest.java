package com.myriam.unrp.ability;

import com.google.gson.JsonParser;
import com.myriam.unrp.data.UnodexData;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class CovenantDefinitionTest {
    @Test void allGodActionsAreReachableWithoutOldRankPurchases() throws Exception {
        var root = JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/unrp/unodex/ability_powers.json"))).getAsJsonObject();
        var power = root.getAsJsonObject("powers").getAsJsonObject("covenant");
        var moves = power.getAsJsonArray("moves");
        assertTrue(moves.size() <= AbilityInputProtocol.CAPACITY);
        Set<String> special = new HashSet<>();
        int[] counts = new int[power.getAsJsonArray("stances").size()];
        for (var value : moves) {
            var move = value.getAsJsonObject();
            String id = move.get("special").getAsString(); special.add(id);
            assertFalse(move.has("unlock"), "Tier gating must not retain old rank-point limits: " + id);
            assertEquals(CovenantRules.minimum(id), move.get("min_level").getAsDouble());
            if (move.has("stance")) counts[move.get("stance").getAsInt()]++;
            String icon = move.get("icon").getAsString();
            assertTrue(Files.exists(Path.of("src/main/resources/assets/unrp").resolve(icon.substring("unrp:".length()))));
        }
        assertTrue(special.containsAll(Set.of("cv_constitution", "cv_retroactive", "cv_forfeit", "cv_class_action", "cv_sovereign_pact", "cv_due", "cv_jurisdiction", "cv_message")));
        for (int count : counts) assertTrue(count + 1 <= 5, "Stance switch plus moves must fit five keys");
        assertTrue(UnodexData.NOT_ROLLABLE.contains("covenant"));
    }
}
