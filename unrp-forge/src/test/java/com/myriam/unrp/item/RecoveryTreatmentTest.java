package com.myriam.unrp.item;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecoveryTreatmentTest {
    @Test void exhaustedPatientNeedsManySecondsOfTreatment() {
        int aura=0;
        for(int seconds=0;seconds<10;seconds++)aura=RecoveryTreatment.nextAura(aura,300);
        assertEquals(60,aura);
        assertTrue(aura<300);
        for(int seconds=10;seconds<50;seconds++)aura=RecoveryTreatment.nextAura(aura,300);
        assertEquals(300,aura);
    }
    @Test void recoveryCapsAtCapacityWithoutOverflow() {
        assertEquals(300,RecoveryTreatment.nextAura(299,300));
        assertEquals(300,RecoveryTreatment.nextAura(300,300));
        assertEquals(Integer.MAX_VALUE,RecoveryTreatment.nextAura(Integer.MAX_VALUE-1,Integer.MAX_VALUE));
    }
    @Test void ownersRateControlsRecoveryWithoutExceedingCapacity() {
        assertEquals(3,RecoveryTreatment.nextAura(0,300,.5));
        assertEquals(12,RecoveryTreatment.nextAura(0,300,2));
        assertEquals(300,RecoveryTreatment.nextAura(299,300,20));
    }
    @Test void smallAuraPoolsStillRecover() {
        assertEquals(1,RecoveryTreatment.nextAura(0,20));
        assertEquals(20,RecoveryTreatment.nextAura(19,20));
    }
}
