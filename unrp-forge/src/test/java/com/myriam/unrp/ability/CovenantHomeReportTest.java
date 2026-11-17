package com.myriam.unrp.ability;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CovenantHomeReportTest {
    @Test
    void reportIncludesVisitorDimensionAndCoordinates() {
        var report =
            new CovenantSpecificEffects.HomeReport("Visitor", "minecraft:overworld", 12.25, 70, -9.5, true);
        String page = report.pageText();
        assertTrue(page.contains("Player: Visitor"));
        assertTrue(page.contains("Dimension: minecraft:overworld"));
        assertTrue(page.contains("X: 12.25\nY: 70.00\nZ: -9.50"));
    }

    @Test
    void missingHomeDoesNotPretendThePlayerLivesAtZero() {
        var report = new CovenantSpecificEffects.HomeReport("Visitor", "", 0, 0, 0, false);
        String page = report.pageText();
        assertTrue(page.contains("No /sethome has been recorded."));
        assertFalse(page.contains("X:"));
    }
}
