package com.myriam.unrp.client;

import com.myriam.unrp.net.MenuDataPacket;
import com.myriam.unrp.net.PlayerStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Point d'entrée côté client des paquets (séparé pour ne jamais être chargé sur un serveur dédié). */
public final class ClientHooks {
    private ClientHooks() {}

    public static void onMenuData(MenuDataPacket p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CandidatureScreen screen) {
            if (p.close) {
                mc.setScreen(null);
            } else {
                screen.update(p);
            }
        } else if (p.open && !p.close) {
            mc.setScreen(new CandidatureScreen(p));
        }
    }

    public static void onPlayerState(PlayerStatePacket p) {
        ClientState.set(p);
        com.myriam.unrp.look.StatClientCache.update(p.aura, p.maxAura);
    }

    public static void onHubState(String json) {
        ClientHub.set(json);
    }

    public static void onStyleSync(java.util.UUID id, com.myriam.unrp.data.StyleData style) {
        com.myriam.unrp.style.StyleService.CLIENT_CACHE.put(id, style);
    }

    public static void onFx(com.myriam.unrp.net.FxPacket p) {
        FxRenderer.receive(p);
    }

    public static void onPose(com.myriam.unrp.net.PosePacket p) {
        PoseClient.receive(p);
    }
}
