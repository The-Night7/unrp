package com.myriam.unrp.data;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityPoolTest {
    private static UnodexData data(Map<String, Integer> pool) {
        UnodexData d = new UnodexData(null, Map.of(), Map.of(), Map.of());
        d.powers.put("a", new AbilityPower());
        d.powers.put("b", new AbilityPower());
        d.pool.putAll(pool);
        return d;
    }

    @Test
    void weightsAreRespected() {
        UnodexData d = data(Map.of("a", 75, "b", 25));
        Random rng = new Random(42);
        int a = 0, n = 200_000;
        for (int i = 0; i < n; i++) if ("a".equals(d.rollAbility(rng))) a++;
        assertTrue(Math.abs(a / (double) n - 0.75) < 0.01, "expected 75%, got " + a / (double) n);
    }

    @Test
    void zeroWeightAndUnplayableAreSkipped() {
        UnodexData d = data(Map.of("a", 0, "b", 10, "missing_power", 1000));
        Random rng = new Random(1);
        for (int i = 0; i < 1000; i++) assertEquals("b", d.rollAbility(rng));
    }

    @Test
    void emptyPoolGivesNothing() {
        assertNull(data(Map.of()).rollAbility(new Random()));
    }

    @Test
    void takenAbilitiesAreNeverDrawn() {
        UnodexData d = data(Map.of("a", 75, "b", 25));
        Random rng = new Random(7);
        for (int i = 0; i < 1000; i++) assertEquals("b", d.rollAbility(rng, java.util.Set.of("a")));
        assertNull(d.rollAbility(rng, java.util.Set.of("a", "b")));
    }

    @Test
    void originOnlyAbilitiesAreNeverRolled() {
        UnodexData d = data(Map.of("a", 1, "sarcophage", 1000, "chained_one", 1000));
        d.powers.put("sarcophage", new AbilityPower());
        d.powers.put("chained_one", new AbilityPower());
        Random rng = new Random(5);
        for (int i = 0; i < 500; i++) assertEquals("a", d.rollAbility(rng));
    }

    @Test
    void variantsCountAsTheSamePower() {
        UnodexData d = data(Map.of("a", 1, "a_variant", 1, "b", 1));
        d.aliases.put("a_variant", "a");
        d.powers.put("a_variant", d.powers.get("a"));
        Random rng = new Random(3);
        for (int i = 0; i < 500; i++) assertEquals("b", d.rollAbility(rng, java.util.Set.of("a")));
    }
}
