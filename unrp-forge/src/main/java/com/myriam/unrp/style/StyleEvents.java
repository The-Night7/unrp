package com.myriam.unrp.style;

import com.myriam.unrp.data.StyleData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** L'alias choisi dans l'onglet Style remplace le nom au-dessus de la tête et dans le chat (client et serveur). */
public class StyleEvents {
    @SubscribeEvent
    public void onName(PlayerEvent.NameFormat e) {
        // Palladium rafraîchit le nom depuis le constructeur de Player,
        // avant que ServerPlayer ait initialisé son champ server.
        if (e.getEntity() instanceof ServerPlayer sp && sp.server == null) return;
        StyleData s;
        if (e.getEntity() instanceof ServerPlayer sp) s = StyleService.get(sp.server, sp.getUUID());
        else s = StyleService.CLIENT_CACHE.get(e.getEntity().getUUID());
        if (s != null && !s.alias.isEmpty()) {
            // AliasNameEvents du modèle : le titre de réputation ([Gardien] / [Tyran]) précède l'alias.
            String moral = e.getEntity() instanceof ServerPlayer sp ? com.myriam.unrp.social.Reputation.titleOf(sp) : "";
            e.setDisplayname(Component.literal(moral + s.alias));
        }
    }
}
