package com.myriam.unrp.pass;

import com.myriam.unrp.style.StyleService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** AuraColor du modèle, côté serveur : couleur des effets d'un lanceur. */
public final class PassServer {
    private PassServer() {}

    /** Valeur par défaut de l'onglet Style : « pas choisie », le pouvoir garde alors sa couleur. */
    public static final int UNSET = 0x8A4DFF;

    /** AuraColor.projectileColor : sombre pour Aura Manipulation, blanc (= couleur du pouvoir) sans choix, sinon la couleur choisie. */
    public static int color(Entity user) {
        if (!(user instanceof ServerPlayer sp)) return 0xFFFFFF;
        if (sp.getTags().contains("unrp.has.aura_manipulation")) return 0x26262C;
        int c = StyleService.get(sp.server, sp.getUUID()).auraColor & 0xFFFFFF;
        return c == UNSET ? 0xFFFFFF : c;
    }
}
