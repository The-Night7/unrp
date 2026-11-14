package com.myriam.unrp.net;

import com.myriam.unrp.social.SocialService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> serveur : une action du hub (entraîner, inviter, défier...). Le serveur vérifie tout. */
public class HubActionPacket {
    public final String action, a, b;

    public HubActionPacket(String action, String a, String b) {
        this.action = action;
        this.a = a;
        this.b = b;
    }

    public static void encode(HubActionPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.action, 32);
        buf.writeUtf(p.a, 64);
        buf.writeUtf(p.b, 64);
    }

    public static HubActionPacket decode(FriendlyByteBuf buf) {
        return new HubActionPacket(buf.readUtf(32), buf.readUtf(64), buf.readUtf(64));
    }

    public static void handle(HubActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender != null) SocialService.action(sender, p.action, p.a, p.b);
        });
        ctx.get().setPacketHandled(true);
    }
}
