package com.myriam.unrp.data;

import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RarityTableTest {
    private static RarityTable load() throws Exception {
        try (Reader r = new InputStreamReader(
                RarityTableTest.class.getResourceAsStream("/data/unrp/unodex/rarity.json"), StandardCharsets.UTF_8)) {
            return RarityTable.fromJson(r);
        }
    }

    @Test
    void expectedProbabilitiesSumToOne() throws Exception {
        RarityTable t = load();
        double sum = t.tiers().stream().mapToDouble(t::probability).sum();
        assertEquals(1.0, sum, 1e-9);
        assertEquals(100.0, t.totalWeight(), 1e-9, "rarity.json weights must total 100");
    }

    @Test
    void rolledLevelsStayInTierRange() throws Exception {
        RarityTable t = load();
        Random rng = new Random(42);
        for (int i = 0; i < 100_000; i++) {
            RarityTable.Roll r = t.roll(rng);
            assertTrue(r.level >= r.tier.levelMin && r.level <= r.tier.levelMax,
                    r.tier.id + " level " + r.level);
        }
    }

    @Test
    void distributionMatchesExpected() throws Exception {
        RarityTable t = load();
        Random rng = new Random(1234);
        int n = 2_000_000;
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < n; i++) counts.merge(t.roll(rng).tier.id, 1, Integer::sum);
        for (RarityTier tier : t.tiers()) {
            double observed = counts.getOrDefault(tier.id, 0) / (double) n;
            double expected = t.probability(tier);
            double tolerance = Math.max(0.0003, 5 * Math.sqrt(expected * (1 - expected) / n));
            assertEquals(expected, observed, tolerance, tier.id);
        }
    }

    @Test
    void lifelongTiersAreCripples() throws Exception {
        RarityTable t = load();
        assertTrue(t.tiers().stream().filter(x -> x.lifelong).allMatch(x -> x.id.startsWith("cripple")));
    }
}
