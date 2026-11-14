package com.myriam.unrp.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import com.myriam.unrp.uo.CounterweightEvents;

class CounterweightTest {
    private static boolean[] feats(int upTo) {
        boolean[] f = new boolean[7];
        for (int i = 2; i <= upTo; i++) f[i] = true;
        return f;
    }

    @Test
    void stageNeedsMomentumAndTrialInOrder() {
        assertEquals(1, CounterweightEvents.stageOf(5000, feats(1)));     // pas d'épreuve : Ballast
        assertEquals(1, CounterweightEvents.stageOf(149, feats(6)));      // pas assez d'élan
        assertEquals(2, CounterweightEvents.stageOf(150, feats(6)));
        assertEquals(4, CounterweightEvents.stageOf(1299, feats(6)));
        assertEquals(6, CounterweightEvents.stageOf(2000, feats(6)));
        boolean[] gap = feats(6);
        gap[3] = false;                                          // épreuve III manquante : bloqué en II
        assertEquals(2, CounterweightEvents.stageOf(2000, gap));
    }
}
