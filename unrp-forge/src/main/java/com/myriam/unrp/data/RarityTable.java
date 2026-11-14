package com.myriam.unrp.data;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Table de tirage de potentiel (rarity.json) et logique de tirage. Aucune dépendance Minecraft. */
public class RarityTable {
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    /** Résultat d'un tirage. */
    public static final class Roll {
        public final RarityTier tier;
        /** Niveau de potentiel, 1 décimale. */
        public final double level;

        Roll(RarityTier tier, double level) {
            this.tier = tier;
            this.level = level;
        }
    }

    private final List<RarityTier> tiers;
    private final double totalWeight;

    public RarityTable(List<RarityTier> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("rarity.json : aucune rareté définie");
        }
        double sum = 0;
        for (RarityTier t : tiers) {
            if (t.weight < 0 || t.levelMax < t.levelMin) {
                throw new IllegalArgumentException("rarity.json : valeurs invalides pour '" + t.id + "'");
            }
            sum += t.weight;
        }
        if (sum <= 0) {
            throw new IllegalArgumentException("rarity.json : la somme des poids doit être > 0");
        }
        this.tiers = List.copyOf(tiers);
        this.totalWeight = sum;
    }

    public static RarityTable fromJson(Reader reader) {
        JsonObject root = GSON.fromJson(reader, JsonObject.class);
        List<RarityTier> list = new ArrayList<>();
        root.getAsJsonArray("tiers").forEach(e -> list.add(GSON.fromJson(e, RarityTier.class)));
        return new RarityTable(list);
    }

    public List<RarityTier> tiers() {
        return tiers;
    }

    public double totalWeight() {
        return totalWeight;
    }

    /** Probabilité (0..1) d'une rareté. */
    public double probability(RarityTier t) {
        return t.weight / totalWeight;
    }

    public Roll roll(Random rng) {
        double r = rng.nextDouble() * totalWeight;
        RarityTier chosen = tiers.get(tiers.size() - 1);
        double acc = 0;
        for (RarityTier t : tiers) {
            acc += t.weight;
            if (r < acc) {
                chosen = t;
                break;
            }
        }
        double raw = chosen.levelMin + rng.nextDouble() * (chosen.levelMax - chosen.levelMin);
        double level = Math.min(chosen.levelMax, Math.round(raw * 10.0) / 10.0);
        return new Roll(chosen, Math.max(chosen.levelMin, level));
    }
}
