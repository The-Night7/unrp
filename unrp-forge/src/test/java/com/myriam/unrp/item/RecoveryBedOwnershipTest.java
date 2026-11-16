package com.myriam.unrp.item;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecoveryBedOwnershipTest {
    private final UUID owner=UUID.randomUUID(),other=UUID.randomUUID();
    private final RecoveryBeds.Settings bed=new RecoveryBeds.Settings(owner,"covenant",.5);
    @Test void onlyOwnerMayChangeRate() {
        assertTrue(bed.mayEdit(owner));assertFalse(bed.mayEdit(other));
    }
    @Test void ownerCanOnlyRemoveAfterSourceStops() {
        assertFalse(bed.mayRemove(owner,true));assertTrue(bed.mayRemove(owner,false));
    }
    @Test void disablingSourceNeverLetsAnotherPlayerRemoveTheBed() {
        assertFalse(bed.mayRemove(other,true));assertFalse(bed.mayRemove(other,false));
    }
}
