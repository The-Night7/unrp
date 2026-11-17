package com.myriam.unrp.uo;

public final class PuppetEvents {
    private PuppetEvents() {}

    public static boolean puppet(net.minecraft.world.entity.Entity e) {
        return false;
    }

    public static boolean priorNoAi(net.minecraft.world.entity.Mob mob) {
        return mob.getTags().contains("unrp.sg_puppet") ? mob.getTags().contains("unrp.sg_puppet_noai") : mob.isNoAi();
    }
}
