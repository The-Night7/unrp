package com.myriam.unrp.data;

import com.myriam.unrp.ability.Mastery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Évolution d'un OC : barre de maîtrise, plafond au niveau 10. */
class MasteryTest {
    private static ClaimData.Profile oc(double level, int xp) {
        ClaimData.Profile p = new ClaimData.Profile();
        p.kind = ClaimData.Profile.OC;
        p.level = level;
        p.masteryXp = xp;
        return p;
    }

    @Test
    void needGrowsWithLevel() {
        assertEquals(3000, Mastery.need(2.5));
        assertEquals(1200, Mastery.need(1.0));
        assertEquals(12000, Mastery.need(10.0));
    }

    @Test
    void percentFollowsXp() {
        assertEquals(0, Mastery.percent(oc(2.5, 0)));
        assertEquals(50, Mastery.percent(oc(2.5, 1500)));
        assertEquals(99, Mastery.percent(oc(2.5, 2999)));
    }

    @Test
    void capAtLevelTen() {
        assertTrue(Mastery.atCap(oc(10.0, 0)));
        assertFalse(Mastery.atCap(oc(9.9, 0)));
        assertEquals(100, Mastery.percent(oc(10.0, 0)));
    }

    @Test
    void loreCharactersAreAlwaysAtCap() {
        ClaimData.Profile c = new ClaimData.Profile();
        c.kind = ClaimData.Profile.CHARACTER;
        assertTrue(Mastery.atCap(c));
        assertEquals(-1, Mastery.percent(c));
    }
}
