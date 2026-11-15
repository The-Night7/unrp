package com.myriam.unrp.look;

import com.google.gson.JsonParser;
import com.myriam.unrp.net.PlayerStatePacket;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class CovenantBarTest {
    @Test void everyStanceExposesAllFourActionsAndItsSwitchWithoutHidingThePowerToggle() throws Exception {
        var power = JsonParser.parseString(Files.readString(Path.of("src/main/resources/data/unrp/unodex/ability_powers.json")))
                .getAsJsonObject().getAsJsonObject("powers").getAsJsonObject("covenant");
        var moves = power.getAsJsonArray("moves");
        for (int stance = 0; stance < power.getAsJsonArray("stances").size(); stance++) {
            PlayerStatePacket state = new PlayerStatePacket();
            state.powName = new String[]{"covenant"};
            state.barPower = new int[moves.size()];
            state.barSlot = new int[moves.size()];
            state.barLocked = new boolean[moves.size()];
            Set<Integer> expected = new HashSet<>();
            for (int i = 0; i < moves.size(); i++) {
                var move = moves.get(i).getAsJsonObject();
                state.barSlot[i] = move.has("bar_slot") ? move.get("bar_slot").getAsInt() : i + 1;
                state.barLocked[i] = move.has("stance") && move.get("stance").getAsInt() != stance;
                if (!state.barLocked[i]) expected.add(i);
            }
            AbilityBar.rebuild(state);
            Set<Integer> exposed = new HashSet<>();
            boolean toggle = false;
            for (int[] page : AbilityBar.lists) for (int entry : page) {
                if (entry == -1) toggle = true;
                if (entry >= 0 && !state.barLocked[entry]) exposed.add(entry);
            }
            assertTrue(toggle, "Power toggle must remain reachable");
            assertEquals(expected, exposed, "Hidden action in stance " + stance);
        }
    }
}
