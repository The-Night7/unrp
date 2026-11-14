package com.myriam.unrp.data;

/** Une ligne de rarity.json. Classe simple (pas de dépendance Minecraft) pour pouvoir la tester seule. */
public class RarityTier {
    public String id;
    public String label;
    /** Pourcentage attendu (normalisé au chargement, le total peut ne pas faire 100). */
    public double weight;
    public double levelMin;
    public double levelMax;
    /** Ne pourra jamais avoir de capacité. */
    public boolean lifelong;
}
