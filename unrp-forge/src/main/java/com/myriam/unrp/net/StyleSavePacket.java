package com.myriam.unrp.net;

import com.myriam.unrp.data.StyleData;
import com.myriam.unrp.style.StyleService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client -> serveur : "voici mon apparence" (bouton Appliquer). Le serveur borne toutes les valeurs. */
public class StyleSavePacket {
    public final StyleData style;

    public StyleSavePacket(StyleData style) {
        this.style = style;
    }

    public static void encode(StyleSavePacket p, FriendlyByteBuf b) {
        p.style.write(b);
    }

    public static StyleSavePacket decode(FriendlyByteBuf b) {
        return new StyleSavePacket(StyleData.read(b));
    }

    public static void handle(StyleSavePacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender != null) StyleService.save(sender, p.style);
        });
        ctx.get().setPacketHandled(true);
    }
}
