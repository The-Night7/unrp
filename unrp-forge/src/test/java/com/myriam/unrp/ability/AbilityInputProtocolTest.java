package com.myriam.unrp.ability;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class AbilityInputProtocolTest {
    @Test void everyMoveHasDistinctPressAndReleaseIncludingGodStances() {
        var messages = new HashSet<Integer>();
        for (int i = 0; i < AbilityInputProtocol.CAPACITY; i++) {
            int press = AbilityInputProtocol.press(i), release = AbilityInputProtocol.release(i);
            assertTrue(messages.add(press)); assertTrue(messages.add(release));
            assertTrue(AbilityInputProtocol.isPress(press)); assertFalse(AbilityInputProtocol.isPress(release));
            assertEquals(i, AbilityInputProtocol.index(press)); assertEquals(i, AbilityInputProtocol.index(release));
            assertTrue(press < 1000 && release < 1000);
        }
        assertFalse(AbilityInputProtocol.isMove(0));
        assertFalse(AbilityInputProtocol.isMove(140));
    }
    @Test void invalidIndicesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> AbilityInputProtocol.press(32));
        assertThrows(IllegalArgumentException.class, () -> AbilityInputProtocol.release(-1));
        assertThrows(IllegalArgumentException.class, () -> AbilityInputProtocol.index(140));
    }
}
