package com.myriam.unrp.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import com.myriam.unrp.uo.HollowMarkEvents;

class HollowMarkTest {
    @Test
    void codexPricesMatchModel() {
        assertEquals(1, HollowMarkEvents.costOf(0, 1));   // Blink rang 2 : 0 + 1
        assertEquals(2, HollowMarkEvents.costOf(1, 1));   // Void Gaze rang 2 : 1 + 1
        assertEquals(20, HollowMarkEvents.costOf(0, 10)); // rang 11 : (0 + 10) x 2
        assertEquals(8, HollowMarkEvents.costOf(12, 1));  // Bend Time : 4 + 1, +3 dès le rang 2
    }

    @Test
    void stagesByRunesSpent() {
        assertEquals(1, HollowMarkEvents.stageFor(7));
        assertEquals(2, HollowMarkEvents.stageFor(8));
        assertEquals(3, HollowMarkEvents.stageFor(20));
        assertEquals(4, HollowMarkEvents.stageFor(40));
    }
}
