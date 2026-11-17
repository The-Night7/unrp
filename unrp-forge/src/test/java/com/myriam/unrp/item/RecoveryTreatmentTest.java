package com.myriam.unrp.item;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecoveryTreatmentTest {
    @Test void recoveryFractionDeterminesEffectiveLevel() {
        assertEquals(3, RecoveryTreatment.effectiveLevel(10, 30), 1e-9);
        assertEquals(2.4, RecoveryTreatment.effectiveLevel(8, 30), 1e-9);
        assertEquals(8, RecoveryTreatment.effectiveLevel(8, 100), 1e-9);
    }
    @Test void defaultRateRequiresOneHundredTreatmentSeconds() {
        double progress=0;
        for(int seconds=0;seconds<99;seconds++)progress=RecoveryTreatment.advance(progress,1);
        assertEquals(99,progress);
        assertEquals(100,RecoveryTreatment.advance(progress,1));
    }
    @Test void ownersRateControlsTimeToFullAbilityRecovery() {
        double slow=0,fast=0;
        for(int seconds=0;seconds<20;seconds++) {
            slow=RecoveryTreatment.advance(slow,.5);
            fast=RecoveryTreatment.advance(fast,5);
        }
        assertEquals(10,slow);assertEquals(100,fast);
    }
    @Test void fractionalProgressSurvivesChangingRateAndDoesNotOverflow() {
        assertEquals(35.5,RecoveryTreatment.advance(35,.5));
        assertEquals(40.5,RecoveryTreatment.advance(35.5,5));
        assertEquals(100,RecoveryTreatment.advance(99.5,5));
        assertEquals(100,RecoveryTreatment.advance(100,1));
    }
    @Test void invalidRatesCannotInstantlyRestoreAnAbility() {
        for(double rate:new double[]{0,-1,Double.NaN,Double.POSITIVE_INFINITY})
            assertThrows(IllegalArgumentException.class,()->RecoveryTreatment.advance(0,rate));
    }
    @Test void ownerCeilingStopsTheCourseBeforeFullRestoration() {
        double progress=0;
        for(int seconds=0;seconds<1000;seconds++)progress=RecoveryTreatment.advance(progress,3,50);
        assertEquals(50,progress);
        assertEquals(30,RecoveryTreatment.advance(29,5,30));
    }
    @Test void raisingCeilingAllowsFurtherTreatmentButLoweringDoesNotStealRecoveredStrength() {
        assertEquals(51,RecoveryTreatment.advance(50,1,75));
        assertEquals(50,RecoveryTreatment.advance(50,1,25));
    }
    @Test void partialRestorationChangesActualStatsAndFullRecoveryPreservesThem() {
        assertEquals(5,RecoveryTreatment.effectiveStat(9,50));
        assertEquals(9,RecoveryTreatment.effectiveStat(9,100));
        assertEquals(1,RecoveryTreatment.effectiveStat(1,50));
        assertEquals(0,RecoveryTreatment.effectiveStat(0,50));
    }

}
