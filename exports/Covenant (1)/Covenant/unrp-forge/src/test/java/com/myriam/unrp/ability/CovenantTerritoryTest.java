package com.myriam.unrp.ability;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CovenantTerritoryTest {
    @Test void namesCanBeUsedInCommandsWithoutRuleSyntax() {
        assertTrue(CovenantTerritory.validName("Myriam Court"));
        assertTrue(CovenantTerritory.validName("School-2_West"));
        for (String name : new String[]{"", " ", "x | Penalty: Soul", "x\nclaim", "a".repeat(49)})
            assertFalse(CovenantTerritory.validName(name));
    }

    @Test void claimsCannotOverlapEvenWhenNeitherCenterIsInsideTheOther() {
        assertTrue(CovenantTerritory.overlaps(0, 0, 150, 250, 0, 150));
        assertTrue(CovenantTerritory.overlaps(0, 0, 500, 600, 0, 150));
        assertFalse(CovenantTerritory.overlaps(0, 0, 150, 300, 0, 150));
        assertFalse(CovenantTerritory.overlaps(0, 0, 150, 250, 250, 150));
    }
}
