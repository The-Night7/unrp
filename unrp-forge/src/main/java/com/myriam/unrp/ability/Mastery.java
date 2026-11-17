package com.myriam.unrp.ability;

import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.player.PlayerStateService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class Mastery {
    private Mastery() {}

    public static final double MAX_LEVEL = 10.0;

    public static final double RATE = 1.0;

    public static boolean isOc(ClaimData.Profile pr) {
        return pr != null && ClaimData.Profile.OC.equals(pr.kind);
    }

    public static int need(double level) {
        return Math.max(1, (int) Math.round(level * 10) * 120);
    }

    public static boolean atCap(ClaimData.Profile pr) {
        return !isOc(pr) || pr.level >= MAX_LEVEL - 1e-9;
    }

    public static int percent(ClaimData.Profile pr) {
        if (!isOc(pr)) return -1;
        if (atCap(pr)) return 100;
        return Math.min(99, (int) Math.floor(pr.masteryXp * 100.0 / need(pr.level)));
    }

    public static void add(ServerPlayer p, ClaimData claims, ClaimData.Profile pr, int amount) {
        if (amount <= 0 || !isOc(pr) || atCap(pr)) return;
        pr.masteryXp += Math.max(1, (int) Math.round(amount * RATE));
        int need = need(pr.level);
        if (pr.masteryXp >= need) {
            pr.masteryXp = 0;
            pr.level = Math.min(MAX_LEVEL, Math.round((pr.level + 0.1) * 10) / 10.0);
            p.displayClientMessage(Component.literal(String.format(java.util.Locale.ROOT, "§6Mastery increased! Level %.1f", pr.level)), true);
            claims.setDirty();
            PlayerStateService.send(p);
        } else {
            claims.setDirty();
        }
    }
}
