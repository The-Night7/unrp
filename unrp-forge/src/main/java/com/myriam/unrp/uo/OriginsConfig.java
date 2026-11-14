package com.myriam.unrp.uo;

/** OriginsConfig du modèle, réduit à isModDev (liste distante des développeurs du modèle : personne chez nous). */
public final class OriginsConfig {
    private OriginsConfig() {}

    public static boolean isModDev(java.util.UUID id) {
        return false;
    }

    public static final java.util.Set<String> ORIGIN_POWERS = java.util.Set.of("sarcophage", "edict", "wyrm_engine", "singularity", "limit_breaker", "dealmaker", "counterweight", "crimson_mark", "hollow_mark", "nadir", "chained_one");

    public static boolean isOriginPower(String shortName) {
        return ORIGIN_POWERS.contains(shortName);
    }
}
