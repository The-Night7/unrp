package com.myriam.unrp.net;

import com.myriam.unrp.menu.MenuService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> serveur : "je veux jouer ce personnage". */
public class ClaimRequestPacket {
    public final String characterId;

    public ClaimRequestPacket(String characterId) {
        this.characterId = characterId;
    }

    public static void encode(ClaimRequestPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.characterId, 64);
    }

    public static ClaimRequestPacket decode(FriendlyByteBuf buf) {
        return new ClaimRequestPacket(buf.readUtf(64));
    }

    public static void handle(ClaimRequestPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender != null) MenuService.handleClaim(sender, p.characterId);
        });
        ctx.get().setPacketHandled(true);
    }
}
