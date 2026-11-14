package com.myriam.unrp.ability;

import com.myriam.unrp.data.AbilityPower;
import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.social.SocialData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Déblocage progressif des capacités, comme le modèle (PointsManager.slotCost/applyUnlock, scripts aura_system.js) :
 * - la capacité de rang 2, 3 ou 4 se débloque pour (rang - 1) points d'aptitude, pour tous tes pouvoirs à la fois ;
 * - tu gagnes max(1, niveau) points, dès le niveau 2 ou après 24000 ticks (20 min) de jeu ;
 * - une capacité exige aussi que sa capacité parente soit débloquée (champ "requires").
 */
public final class Unlocks {
    private Unlocks() {}

    public static final int MIN_RANK = 2, MAX_RANK = 4;
    private static final int GRACE_TICKS = 24000;

    public static int cost(int rank) {
        return rank - 1;
    }

    /** Points gagnés au total : un par niveau, au moins 1 une fois le délai passé. */
    public static int total(MinecraftServer server, ServerPlayer p) {
        ClaimData.Profile pr = ClaimData.get(server).profileOf(p.getUUID()).orElse(null);
        if (pr == null) return 0;
        int lvl = (int) Math.floor(pr.level);
        int played = SocialData.get(server).store.soc(p.getUUID()).playTicks;
        if (lvl < 2 && played < GRACE_TICKS) return 0;
        return Math.max(1, lvl);
    }

    /** Appelé toutes les 4 ticks : compte le temps de jeu des personnages de niveau < 2 (comme play_ticks du modèle). */
    public static void tickPlay(MinecraftServer server) {
        ClaimData claims = ClaimData.get(server);
        SocialData d = SocialData.get(server);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ClaimData.Profile pr = claims.profileOf(p.getUUID()).orElse(null);
            if (pr == null || pr.level >= 2) continue;
            SocialData.Soc soc = d.store.soc(p.getUUID());
            if (soc.playTicks >= GRACE_TICKS) continue;
            soc.playTicks += 4;
            if (soc.playTicks % 400 == 0 || soc.playTicks >= GRACE_TICKS) d.setDirty();
            if (soc.playTicks >= GRACE_TICKS) com.myriam.unrp.player.PlayerStateService.send(p);
        }
    }

    /** Maîtrise pleine : OC au niveau maximal, ou personnage de l'Unodex (niveau fixe). */
    public static boolean mastered(MinecraftServer server, ServerPlayer p) {
        return Mastery.atCap(ClaimData.get(server).profileOf(p.getUUID()).orElse(null));
    }

    public static int mask(MinecraftServer server, ServerPlayer p) {
        return SocialData.get(server).store.soc(p.getUUID()).unlocked;
    }

    public static boolean has(int mask, int rank) {
        return rank < MIN_RANK || (mask & (1 << rank)) != 0;
    }

    public static int spent(int mask) {
        int n = 0;
        for (int r = MIN_RANK; r <= MAX_RANK; r++) if (has(mask, r)) n += cost(r);
        return n;
    }

    public static int available(MinecraftServer server, ServerPlayer p) {
        return Math.max(0, total(server, p) - spent(mask(server, p)));
    }

    /** La capacité i du pouvoir est-elle verrouillée (rang non acheté, ou parente verrouillée) ? */
    public static boolean locked(int mask, AbilityPower power, int i, boolean mastered) {
        for (int guard = 0; i >= 0 && i < power.moves.size() && guard < 8; guard++) {
            AbilityPower.Move m = power.moves.get(i);
            if (!has(mask, m.unlock) || (m.mastery && !mastered)) return true;
            i = m.requires;
        }
        return false;
    }

    /** Achète un rang. Retourne un message pour le joueur. */
    public static String buy(ServerPlayer p, int rank) {
        if (rank < MIN_RANK || rank > MAX_RANK) return "Rang inconnu.";
        MinecraftServer s = p.server;
        SocialData d = SocialData.get(s);
        SocialData.Soc soc = d.store.soc(p.getUUID());
        if (has(soc.unlocked, rank)) return "Rang " + rank + " déjà débloqué.";
        if (available(s, p) < cost(rank)) return "Il te faut " + cost(rank) + " point(s) d'aptitude.";
        soc.unlocked |= 1 << rank;
        d.setDirty();
        return "Rang " + rank + " débloqué !";
    }

    /** Administration : tout débloquer ou tout reprendre. */
    public static void setAll(ServerPlayer p, boolean on) {
        SocialData d = SocialData.get(p.server);
        d.store.soc(p.getUUID()).unlocked = on ? (1 << 2 | 1 << 3 | 1 << 4) : 0;
        d.setDirty();
    }
}
