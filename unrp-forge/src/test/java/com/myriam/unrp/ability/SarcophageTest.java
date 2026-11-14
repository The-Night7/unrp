package com.myriam.unrp.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import com.myriam.unrp.uo.SarcophageEvents;

class SarcophageTest {
    @Test
    void tailsGrowWithVitalSand() {
        assertEquals(1, SarcophageEvents.tailsFor(7, false));
        assertEquals(2, SarcophageEvents.tailsFor(8, false));
        assertEquals(3, SarcophageEvents.tailsFor(20, false));
        assertEquals(4, SarcophageEvents.tailsFor(40, false));
        assertEquals(3, SarcophageEvents.tailsFor(40, true)); // une queue tranchée
        assertEquals(1, SarcophageEvents.tailsFor(0, true));  // la seule queue ne se tranche pas
    }

    @Test
    void formulas() {
        assertEquals(10f, SarcophageEvents.coffinCrush(0, 0), 1e-6);
        assertEquals(12f, SarcophageEvents.crashDamage(2, 0), 1e-6);
        assertEquals(11, SarcophageEvents.grainStreams(4));
    }
}
