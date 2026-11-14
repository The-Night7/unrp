package com.myriam.unrp.uo;

/** TimeWeft du modèle (Singularity), réduit à everpresent : l'entité hors du temps (tag sg_everpresent). */
public final class TimeWeft {
    private TimeWeft() {}

    public static final String EVERPRESENT_TAG = "unrp.sg_everpresent";

    public static boolean everpresent(net.minecraft.world.entity.Entity entity) {
        return entity.getTags().contains(EVERPRESENT_TAG);
    }

    /** Entité en stase (Singularity, pas portée). */
    public static boolean held(net.minecraft.world.entity.Entity entity) {
        return false;
    }

    /** Hors d'atteinte du temps (Time Manipulation, Hollow Mark ; Singularity stade 6 pas portée). */
    public static boolean timeImmune(net.minecraft.world.entity.LivingEntity target) {
        java.util.Set<String> tags = target.getTags();
        return tags.contains("unrp.time_immune") || tags.contains("unrp.has.time_manipulation") || tags.contains("unrp.active.time_manipulation") || tags.contains("unrp.has.hollow_mark");
    }
}
