package com.myriam.unrp.data;

import com.myriam.unrp.data.Models.Ability;
import com.myriam.unrp.data.Models.Character;
import com.myriam.unrp.data.Models.School;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Contenu chargé des JSON (data/unrp/unodex/). Remplacé en bloc à chaque /reload. */
public class UnodexData {
    public static final UnodexData EMPTY = new UnodexData(null, Map.of(), Map.of(), Map.of());

    private static volatile UnodexData current = EMPTY;

    public final RarityTable rarity;
    public final Map<String, School> schools;
    public final Map<String, Character> characters;
    public final Map<String, Ability> abilities;
    /** Pouvoirs jouables (ability_powers.json), par id de pouvoir. */
    public final Map<String, AbilityPower> powers = new LinkedHashMap<>();
    /** id de capacité Unodex -> id de pouvoir (ex. modes_atk -> modes) */
    public final Map<String, String> aliases = new LinkedHashMap<>();
    /** Pool de tirage de capacité des OC (ability_pool.json) : id de capacité -> poids. */
    public final Map<String, Integer> pool = new LinkedHashMap<>();
    /** Plages de potentiel de départ d'un OC (stat_ranges.json) : id de pouvoir -> 5 x {min, max}, ordre Puissance, Défense, Récupération, Ruse, Vitesse. */
    public final Map<String, double[][]> ranges = new LinkedHashMap<>();

    /** Le plus petit de n tirages uniformes entre min et max (weightedLow du modèle : biais vers le bas). */
    static double weightedLow(java.util.Random rng, double min, double max, int n) {
        double r = rng.nextDouble();
        for (int i = 1; i < n; i++) r = Math.min(r, rng.nextDouble());
        return min + (max - min) * r;
    }

    /** Potentiels de départ d'un OC (dixièmes, 5 stats) selon sa capacité. Sans plage connue : 1,0 à 2,0. */
    public int[] rollPotentials(String abilityId, java.util.Random rng) {
        String key = abilityId == null ? null : aliases.getOrDefault(abilityId, abilityId);
        double[][] r = key == null ? null : ranges.get(key);
        int[] out = new int[5];
        for (int i = 0; i < 5; i++) {
            double lo = r == null ? 1.0 : r[i][0], hi = r == null ? 2.0 : r[i][1];
            double v = lo == hi ? lo : weightedLow(rng, lo, hi, 5);
            out[i] = (int) Math.round(v * 10);
        }
        return out;
    }

    /** Id de pouvoir d'une capacité (les variantes d'un même pouvoir comptent pour la même : modes_atk et modes_def). */
    public String canon(String abilityId) {
        return abilityId == null ? null : aliases.getOrDefault(abilityId, abilityId);
    }

    /** Capacités du modèle (Unrp) au poids 0 : jamais tirées, ni par le pool ni par /unrp rollability, même si on les y écrit. */
    public static final java.util.Set<String> NOT_ROLLABLE = java.util.Set.of("sarcophage", "edict", "wyrm_engine", "singularity", "limit_breaker",
            "dealmaker", "counterweight", "crimson_mark", "hollow_mark", "nadir", "chained_one", "covenant");

    /** Tire une capacité pour un OC selon les poids du pool ; null si le pool est vide. */
    public String rollAbility(java.util.Random rng) {
        return rollAbility(rng, java.util.Set.of());
    }

    /** Comme rollAbility, mais sans les capacités déjà détenues par quelqu'un (taken = ids canoniques) : une capacité est unique. null si plus aucune n'est libre. */
    public String rollAbility(java.util.Random rng, java.util.Set<String> taken) {
        int total = 0;
        for (Map.Entry<String, Integer> e : pool.entrySet()) if (e.getValue() > 0 && !NOT_ROLLABLE.contains(canon(e.getKey())) && powerFor(e.getKey()) != null && !taken.contains(canon(e.getKey()))) total += e.getValue();
        if (total <= 0) return null;
        int r = rng.nextInt(total);
        for (Map.Entry<String, Integer> e : pool.entrySet()) {
            if (e.getValue() <= 0 || NOT_ROLLABLE.contains(canon(e.getKey())) || powerFor(e.getKey()) == null || taken.contains(canon(e.getKey()))) continue;
            r -= e.getValue();
            if (r < 0) return e.getKey();
        }
        return null;
    }

    public AbilityPower powerFor(String abilityId) {
        if (abilityId == null) return null;
        return powers.get(aliases.getOrDefault(abilityId, abilityId));
    }

    public UnodexData(RarityTable rarity, Map<String, School> schools,
                      Map<String, Character> characters, Map<String, Ability> abilities) {
        this.rarity = rarity;
        this.schools = Collections.unmodifiableMap(new LinkedHashMap<>(schools));
        this.characters = Collections.unmodifiableMap(new LinkedHashMap<>(characters));
        this.abilities = Collections.unmodifiableMap(new LinkedHashMap<>(abilities));
    }

    public static UnodexData get() {
        return current;
    }

    static void set(UnodexData data) {
        current = data;
    }
}
