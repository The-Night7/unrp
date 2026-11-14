package com.myriam.unrp.client;

/** Nom et couleur d'affichage d'un tier. */
public final class TierStyle {
    private TierStyle() {}

    public static String label(String tier) {
        return switch (tier) {
            case "cripple", "cripple_life" -> "Cripple";
            case "low" -> "Low";
            case "mid" -> "Mid";
            case "elite" -> "Elite";
            case "high" -> "High";
            case "god" -> "God";
            case "exo_god" -> "Exo-god";
            default -> "Inconnu";
        };
    }

    public static int color(String tier) {
        return switch (tier) {
            case "cripple", "cripple_life" -> 0x888888;
            case "low" -> 0xC8C8C8;
            case "mid" -> 0x55FF55;
            case "elite" -> 0x55FFFF;
            case "high" -> 0xFFAA00;
            case "god" -> 0xFF55FF;
            case "exo_god" -> 0xFF5555;
            default -> 0xAAAAAA;
        };
    }
}
