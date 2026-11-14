package com.myriam.unrp.net;

import com.myriam.unrp.ability.AbilityEngine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> serveur : une entrée de pouvoir.
 * 0 = activer/désactiver ; 10+i = appui sur le mouvement i ; 20+i = relâchement.
 */
public class UseAbilityPacket {
    public final int action;

    public UseAbilityPacket(int action) {
        this.action = action;
    }

    public static void encode(UseAbilityPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.action);
    }

    public static UseAbilityPacket decode(FriendlyByteBuf buf) {
        return new UseAbilityPacket(buf.readVarInt());
    }

    public static void handle(UseAbilityPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender != null) AbilityEngine.input(sender, p.action);
        });
        ctx.get().setPacketHandled(true);
    }
}
