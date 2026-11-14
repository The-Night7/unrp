package com.myriam.unrp.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> serveur : le joueur a donné un coup de poing dans le vide (peut-être dans une barrière). */
public class PunchPacket {
    public static void encode(PunchPacket p, FriendlyByteBuf b) {}

    public static PunchPacket decode(FriendlyByteBuf b) {
        return new PunchPacket();
    }

    public static void handle(PunchPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp != null) com.myriam.unrp.pass.BarrierEvents.punchFromInside(sp); // BarrierPunchPacket du modèle
        });
        ctx.get().setPacketHandled(true);
    }
}
