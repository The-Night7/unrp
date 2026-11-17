package com.myriam.unrp.net;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** Server-owned signature state and private parchment delivery. */
public record CovenantPacket(String json) {
    public static void encode(CovenantPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.json, 262144);
    }
    public static CovenantPacket decode(FriendlyByteBuf buffer) {
        return new CovenantPacket(buffer.readUtf(262144));
    }
    public static void handle(CovenantPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(()
                                -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                                    () -> () -> com.myriam.unrp.client.CovenantClient.receive(packet.json)));
        context.setPacketHandled(true);
    }
}
