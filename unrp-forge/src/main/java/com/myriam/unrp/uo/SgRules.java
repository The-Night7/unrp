package com.myriam.unrp.uo;

/** SgRules du modèle, réduit à outside : hors d'atteinte (Singularity palier 3 + MassForge rang 17, pas de Singularity chez nous). */
public final class SgRules {
    private SgRules() {}

    public static boolean outside(net.minecraft.world.entity.Entity entity) {
        return false;
    }
}
