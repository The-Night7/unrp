package com.myriam.unrp.style;

import com.myriam.unrp.data.StyleData;
import com.myriam.unrp.net.Net;
import com.myriam.unrp.net.StyleSyncPacket;
import com.myriam.unrp.social.SocialData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

/** Sauvegarde et diffusion de l'apparence des joueurs. */
public final class StyleService {
    /** Apparence côté client, par joueur (rempli par StyleSyncPacket). Lue aussi pour l'alias du chat côté serveur. */
    public static final java.util.Map<UUID, StyleData> CLIENT_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private StyleService() {}

    public static StyleData get(MinecraftServer server, UUID id) {
        StyleData s = SocialData.get(server).store.soc(id).style;
        return s == null ? new StyleData() : s;
    }

    public static void save(ServerPlayer p, StyleData incoming) {
        SocialData d = SocialData.get(p.server);
        d.store.soc(p.getUUID()).style = incoming.copy().clamp();
        d.setDirty();
        Net.CHANNEL.send(PacketDistributor.ALL.noArg(), new StyleSyncPacket(p.getUUID(), get(p.server, p.getUUID())));
        p.refreshDisplayName();
        p.displayClientMessage(Component.literal("Style enregistré."), true);
    }

    /** À la connexion : le nouveau reçoit le style de tous, tout le monde reçoit le sien. */
    public static void onLogin(ServerPlayer joiner) {
        MinecraftServer s = joiner.server;
        for (ServerPlayer other : s.getPlayerList().getPlayers()) {
            Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> joiner), new StyleSyncPacket(other.getUUID(), get(s, other.getUUID())));
        }
        Net.CHANNEL.send(PacketDistributor.ALL.noArg(), new StyleSyncPacket(joiner.getUUID(), get(s, joiner.getUUID())));
    }
}
