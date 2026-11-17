package com.myriam.unrp.uo;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class WeaveSyncPacket {
    final int motes, motesMax, flags;

    public WeaveSyncPacket(int motes, int motesMax, int flags) {
        this.motes = motes;
        this.motesMax = motesMax;
        this.flags = flags;
    }

    public static void encode(WeaveSyncPacket m, FriendlyByteBuf b) {
        b.writeVarInt(m.motes);
        b.writeVarInt(m.motesMax);
        b.writeVarInt(m.flags);
    }

    public static WeaveSyncPacket decode(FriendlyByteBuf b) {
        return new WeaveSyncPacket(b.readVarInt(), b.readVarInt(), b.readVarInt());
    }

    public static void handle(WeaveSyncPacket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> com.myriam.unrp.look.StatClientCache.setWeave(m.motes, m.motesMax, m.flags));
        ctx.get().setPacketHandled(true);
    }
}
