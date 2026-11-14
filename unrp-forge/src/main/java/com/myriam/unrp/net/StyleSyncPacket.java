package com.myriam.unrp.net;

import com.myriam.unrp.client.ClientHooks;
import com.myriam.unrp.data.StyleData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Serveur -> clients : l'apparence d'un joueur (pour dessiner ses yeux et son alias). */
public class StyleSyncPacket {
    public final UUID id;
    public final StyleData style;

    public StyleSyncPacket(UUID id, StyleData style) {
        this.id = id;
        this.style = style;
    }

    public static void encode(StyleSyncPacket p, FriendlyByteBuf b) {
        b.writeUUID(p.id);
        p.style.write(b);
    }

    public static StyleSyncPacket decode(FriendlyByteBuf b) {
        return new StyleSyncPacket(b.readUUID(), StyleData.read(b));
    }

    public static void handle(StyleSyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.onStyleSync(p.id, p.style)));
        ctx.get().setPacketHandled(true);
    }
}
