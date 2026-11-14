package com.myriam.unrp.data;

import net.minecraft.nbt.NbtCompound;

/** Tout ce que le mod retient d'un joueur. */
public class PlayerData {
    public static final int POWER = 0, SPEED = 1, TRICK = 2, RECOVERY = 3, DEFENSE = 4;
    public static final String[] STAT_NAMES = {"power", "speed", "trick", "recovery", "defense"};
    public static final int STAT_MIN = 1, STAT_MAX = 10;

    /** Identifiant du pouvoir ("" = aucun). */
    public String power = "";
    public int aura = 100;
    public final int[] stats = {1, 1, 1, 1, 1};

    /** Temps de jeu (ticks) avant lequel le pouvoir est en recharge. Non sauvegardé. */
    public long cooldownUntil = 0;

    // --- Formules de jeu : tout l'équilibrage est ici ---
    public int maxAura() {
        return 100 + 10 * (stats[TRICK] + stats[RECOVERY]);
    }

    /** Aura régénérée par seconde. */
    public int regen() {
        return 1 + stats[RECOVERY] / 2;
    }

    public static int statIndex(String name) {
        for (int i = 0; i < STAT_NAMES.length; i++) {
            if (STAT_NAMES[i].equals(name)) return i;
        }
        return -1;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("power", power);
        nbt.putInt("aura", aura);
        nbt.putIntArray("stats", stats);
        return nbt;
    }

    public static PlayerData fromNbt(NbtCompound nbt) {
        PlayerData data = new PlayerData();
        data.power = nbt.getString("power");
        data.aura = nbt.getInt("aura");
        int[] saved = nbt.getIntArray("stats");
        for (int i = 0; i < data.stats.length && i < saved.length; i++) {
            data.stats[i] = Math.max(STAT_MIN, Math.min(STAT_MAX, saved[i]));
        }
        return data;
    }
}
