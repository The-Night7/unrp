package com.myriam.unrp.item;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecoveryTreatmentTest {
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
}
