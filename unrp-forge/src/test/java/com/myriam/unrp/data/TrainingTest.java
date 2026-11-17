package com.myriam.unrp.data;

import com.myriam.unrp.player.Training;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingTest {
    @Test
    void costGrowsPerStep() {
        assertEquals(1, Training.cost(0));
        assertEquals(2, Training.cost(1));
        assertEquals(3, Training.cost(2));
        assertEquals(4, Training.cost(3));
        assertEquals(10, Training.spent(new int[] {4, 0, 0, 0, 0}));
        assertEquals(1 + 3 + 6, Training.spent(new int[] {1, 2, 3, 0, 0}));
    }

    @Test
    void ceilingShortensTheLastStep() {
        assertEquals(5, Training.gain(60));
        assertEquals(3, Training.gain(97));
        assertEquals(0, Training.gain(100));
        assertEquals(5, Training.refundTenths(0));
        assertEquals(3, Training.refundTenths(3));
        assertEquals(3, Training.refundPoints(3));
    }

    @Test
    void ocPotentialsStayInTheirRanges() {
        UnodexData d = new UnodexData(null, Map.of(), Map.of(), Map.of());
        d.ranges.put("strong_punch", new double[][] {{5.5, 6.5}, {3.25, 4.25}, {1.75, 2.75}, {1.5, 2.5}, {2.0, 3.0}});
        Random rng = new Random(7);
        long sum = 0;
        for (int n = 0; n < 20000; n++) {
            int[] p = d.rollPotentials("strong_punch", rng);
            assertTrue(p[0] >= 55 && p[0] <= 65 && p[1] >= 33 && p[1] <= 43 && p[4] >= 20 && p[4] <= 30);
            sum += p[0];
        }
        assertTrue(sum / 20000.0 < 58.0, "the roll must favour lower values (minimum of five samples)");
    }

    @Test
    void ocStatsFollowPotentialAndLevel() {
        int[] s = com.myriam.unrp.player.PlayerStateService.fromPotential(new int[] {60, 40, 30, 30, 20}, new int[] {10, 0, 0, 0, 0}, 10.0);
        assertEquals(7, s[0]);
        assertEquals(4, s[1]);
        assertEquals(1, com.myriam.unrp.player.PlayerStateService.fromPotential(new int[] {10, 10, 10, 10, 10}, new int[5], 0.0)[0]);
    }
}
