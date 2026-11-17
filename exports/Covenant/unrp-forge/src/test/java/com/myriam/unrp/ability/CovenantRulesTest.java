package com.myriam.unrp.ability;

import static com.myriam.unrp.ability.CovenantParse.Kind.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CovenantRulesTest {
    @Test
    void tierBoundariesMatchNormalRarityLevels() {
        assertEquals(10, CovenantRules.radius(1.9));
        assertEquals(30, CovenantRules.radius(2.0));
        assertEquals(75, CovenantRules.radius(3.5));
        assertEquals(150, CovenantRules.radius(5.0));
        assertEquals(500, CovenantRules.radius(6.0));
        assertEquals(500, CovenantRules.radius(10.0));
    }
    @Test
    void lowerTierRestrictionsAreLiftedAtGodTier() {
        assertEquals(2, CovenantRules.lawCap(1.9));
        assertEquals(5, CovenantRules.lawCap(2));
        assertEquals(Integer.MAX_VALUE, CovenantRules.lawCap(3.5));
        assertEquals(5, CovenantRules.branchCap(5.9));
        assertEquals(Integer.MAX_VALUE, CovenantRules.branchCap(6));
        assertEquals(10, CovenantRules.wieldCap(6));
        assertFalse(CovenantRules.allowed(3.4, BRANCH));
        assertTrue(CovenantRules.allowed(3.5, BRANCH));
        assertFalse(CovenantRules.allowed(4.9, SOUL));
        assertTrue(CovenantRules.allowed(5, SOUL));
        assertFalse(CovenantRules.allowed(5.9, ABILITY));
        assertTrue(CovenantRules.allowed(6, ABILITY));
        assertTrue(CovenantRules.allowed(6, NONE));
    }
    @Test
    void indemnityBecomesPermanentAtFiveRatherThanAtGodTier() {
        assertEquals(1120, CovenantRules.indemnityExpiry(4.9, 1000));
        assertEquals(Long.MAX_VALUE, CovenantRules.indemnityExpiry(5, 1000));
        assertEquals(Long.MAX_VALUE, CovenantRules.indemnityExpiry(6, 1000));
    }
    @Test
    void GodTierRemovesSoulConsequences() {
        assertFalse(CovenantRules.soulBurden(5, 10));
        assertTrue(CovenantRules.soulBurden(5, 11));
        assertFalse(CovenantRules.soulBurden(6, 11));
        assertFalse(CovenantRules.soulBurden(10, 10000));
    }
    @Test
    void signaturesAndJurisdictionUseTheSpecifiedDurations() {
        assertEquals(20L * 60 * 60, CovenantRules.HOUR);
        assertEquals(24 * CovenantRules.HOUR, CovenantRules.DAY);
        assertEquals(7 * CovenantRules.DAY, CovenantRules.WEEK);
        assertEquals(6, CovenantRules.minimum("cv_constitution"));
        assertEquals(6, CovenantRules.minimum("cv_retroactive"));
        assertEquals(2, CovenantRules.minimum("cv_pact"));
        assertEquals(3.5, CovenantRules.minimum("cv_proxy"));
    }
    @Test
    void retroactiveLawsNeverReachOutsideTheCourtOrPastOneHour() {
        long now = 100000;
        assertTrue(CovenantRules.retroactiveEligible(now, now - CovenantRules.HOUR, true));
        assertFalse(CovenantRules.retroactiveEligible(now, now - CovenantRules.HOUR - 1, true));
        assertFalse(CovenantRules.retroactiveEligible(now, now - 1, false));
        assertFalse(CovenantRules.retroactiveEligible(now, now + 1, true));
    }
}
