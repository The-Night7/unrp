package com.myriam.unrp.ability;

import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.player.PlayerStateService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Évolution d'un OC (la « maîtrise » du mod modèle, kubejs/aura_system.js) : l'aura dépensée remplit une barre ;
 * quand elle est pleine, le niveau monte de 0,1 et la barre repart à zéro. Plafond : niveau 10.
 * Il faut (niveau x 10) x 120 aura pour passer au niveau suivant (ex. niveau 2,5 : 3000). Les personnages de l'Unodex
 * ont un niveau fixe : ils sont considérés au plafond (maîtrise pleine).
 */
public final class Mastery {
    private Mastery() {}

    /** Niveau maximal d'un OC. */
    public static final double MAX_LEVEL = 10.0;
    /** Multiplicateur d'aura dépensée -> maîtrise (exp_rate du modèle, 1 par défaut). */
    public static final double RATE = 1.0;

    public static boolean isOc(ClaimData.Profile pr) {
        return pr != null && ClaimData.Profile.OC.equals(pr.kind);
    }

    /** Aura nécessaire pour passer au niveau suivant. */
    public static int need(double level) {
        return Math.max(1, (int) Math.round(level * 10) * 120);
    }

    public static boolean atCap(ClaimData.Profile pr) {
        return !isOc(pr) || pr.level >= MAX_LEVEL - 1e-9;
    }

    /** Barre de progression en % (100 au plafond, -1 pour un personnage de l'Unodex). */
    public static int percent(ClaimData.Profile pr) {
        if (!isOc(pr)) return -1;
        if (atCap(pr)) return 100;
        return Math.min(99, (int) Math.floor(pr.masteryXp * 100.0 / need(pr.level)));
    }

    /** Appelé chaque fois que de l'aura est dépensée. */
    public static void add(ServerPlayer p, ClaimData claims, ClaimData.Profile pr, int amount) {
        if (amount <= 0 || !isOc(pr) || atCap(pr)) return;
        pr.masteryXp += Math.max(1, (int) Math.round(amount * RATE));
        int need = need(pr.level);
        if (pr.masteryXp >= need) {
            pr.masteryXp = 0;
            pr.level = Math.min(MAX_LEVEL, Math.round((pr.level + 0.1) * 10) / 10.0);
            p.displayClientMessage(Component.literal(String.format(java.util.Locale.ROOT, "§6Maîtrise accrue ! Niveau %.1f", pr.level)), true);
            claims.setDirty();
            PlayerStateService.send(p);
        } else {
            claims.setDirty();
        }
    }
}
