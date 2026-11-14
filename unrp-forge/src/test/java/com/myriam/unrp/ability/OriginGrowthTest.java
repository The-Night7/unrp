package com.myriam.unrp.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import com.myriam.unrp.uo.LimitBreakerForms;

class OriginGrowthTest {
    @Test
    void growthCurveMatchesModel() {
        assertEquals(1000, OriginGrowth.gainMilli(20));
        assertEquals(950, OriginGrowth.gainMilli(21));
        assertEquals(900, OriginGrowth.gainMilli(26));
        assertEquals(20000, OriginGrowth.growthMilli(20));
        assertEquals(46, OriginGrowth.xpToNext(1));
    }

    @Test
    void limitBreakerLadder() {
        assertEquals(1.0, LimitBreakerForms.powerOverEra(LimitBreakerForms.COLOSSUS), 1e-9);
        assertEquals(2.0 / 1.5, LimitBreakerForms.powerOverEra(6), 1e-9); // Radiance III sur l'ère de Radiance
        assertEquals(1, LimitBreakerForms.firstOfEra(1));
        assertEquals(12, LimitBreakerForms.firstOfEra(4));
    }
}
