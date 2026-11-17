package com.myriam.unrp.uo;

public final class PowerScalingEvents {
    private PowerScalingEvents() {}

    private static final java.util.List<String> ORDER = java.util.List.of("power", "defense", "recovery", "trick", "speed");

    public static double effectiveStat(net.minecraft.server.level.ServerPlayer player, String potObjective, String multObjective) {
        int[] st = com.myriam.unrp.ability.AbilityEngine.currentStats(player);
        int i = ORDER.indexOf(potObjective.substring(potObjective.lastIndexOf('_') + 1));
        double e = st == null || i < 0 ? 0.0D : st[i];
        return e > 10.0D ? 10.0D : (e < 0.0D ? 0.0D : e);
    }
}
