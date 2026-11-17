package com.myriam.unrp.style;

import com.myriam.unrp.data.StyleData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class StyleEvents {
    @SubscribeEvent
    public void onName(PlayerEvent.NameFormat e) {

        if (e.getEntity() instanceof ServerPlayer sp && sp.server == null) return;
        StyleData s;
        if (e.getEntity() instanceof ServerPlayer sp) s = StyleService.get(sp.server, sp.getUUID());
        else s = StyleService.CLIENT_CACHE.get(e.getEntity().getUUID());
        if (s != null && !s.alias.isEmpty()) {

            String moral = e.getEntity() instanceof ServerPlayer sp ? com.myriam.unrp.social.Reputation.titleOf(sp) : "";
            e.setDisplayname(Component.literal(moral + s.alias));
        }
    }
}
