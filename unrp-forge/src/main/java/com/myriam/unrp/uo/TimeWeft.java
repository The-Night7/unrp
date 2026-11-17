package com.myriam.unrp.uo;

public final class TimeWeft {
    private TimeWeft() {}

    public static final String EVERPRESENT_TAG = "unrp.sg_everpresent";

    public static boolean everpresent(net.minecraft.world.entity.Entity entity) {
        return entity.getTags().contains(EVERPRESENT_TAG);
    }

    public static boolean held(net.minecraft.world.entity.Entity entity) {
        return false;
    }

    public static boolean timeImmune(net.minecraft.world.entity.LivingEntity target) {
        java.util.Set<String> tags = target.getTags();
        return tags.contains("unrp.time_immune") || tags.contains("unrp.has.time_manipulation") || tags.contains("unrp.active.time_manipulation") || tags.contains("unrp.has.hollow_mark");
    }
}
