package com.myriam.unrp.uo;

/** TrickCooldownEvents du modèle, réduit à reduction : la Ruse (stat actuelle, plafonnée à 10) réduit les recharges jusqu'à 40 %. */
public final class TrickCooldownEvents {
    private TrickCooldownEvents() {}

    public static double reduction(net.minecraft.server.level.ServerPlayer player) {
        int[] st = com.myriam.unrp.ability.AbilityEngine.currentStats(player);
        double effective = st == null ? 0.0D : st[3];
        if (effective > 10.0D) effective = 10.0D;
        if (effective < 0.0D) effective = 0.0D;
        return 0.4D * (effective / 10.0D);
    }
}
