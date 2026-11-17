package com.myriam.unrp.data;

import com.myriam.unrp.player.PlayerStateService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatRatioTest {
    private static int stat(double level) {
        ClaimData.Profile p = new ClaimData.Profile();
        p.kind = ClaimData.Profile.OC;
        p.level = level;
        p.pot = new int[] {100, 100, 100, 100, 100}; // Potential 10.0
        return PlayerStateService.stats(p, null)[0];
    }

    @Test
    void statsFollowLevel() {
        assertEquals(3, stat(1.0));
        assertEquals(5, stat(2.5));
        assertEquals(6, stat(4.0));
        assertEquals(8, stat(6.4));
        assertEquals(10, stat(10.0));
    }

    @Test
    void levelIsCappedAtTen() {
        assertEquals(10, stat(12.0));
    }

    @Test
    void neverBelowOne() {
        assertEquals(1, stat(0.0));
    }
}
