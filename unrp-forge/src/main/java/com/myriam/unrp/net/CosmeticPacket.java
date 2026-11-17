package com.myriam.unrp.net;

import com.myriam.unrp.item.Cosmetics;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class CosmeticPacket {
    public final UUID id;
    public final String[] pieces;

    public CosmeticPacket(UUID id, String[] pieces) {
        this.id = id;
        this.pieces = pieces;
    }

    public static void encode(CosmeticPacket p, FriendlyByteBuf b) {
        b.writeUUID(p.id);
        for (int i = 0; i < 4; i++) b.writeUtf(p.pieces[i], 64);
    }

    public static CosmeticPacket decode(FriendlyByteBuf b) {
        UUID id = b.readUUID();
        String[] v = new String[4];
        for (int i = 0; i < 4; i++) v[i] = b.readUtf(64);
        return new CosmeticPacket(id, v);
    }

    public static void handle(CosmeticPacket p, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> Cosmetics.CLIENT.put(p.id, p.pieces));
        ctx.get().setPacketHandled(true);
    }
}
