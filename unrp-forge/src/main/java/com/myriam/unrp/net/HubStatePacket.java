package com.myriam.unrp.net;

import com.myriam.unrp.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HubStatePacket {
    public final String json;

    public HubStatePacket(String json) {
        this.json = json;
    }

    public static void encode(HubStatePacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.json, 262144);
    }

    public static HubStatePacket decode(FriendlyByteBuf buf) {
        return new HubStatePacket(buf.readUtf(262144));
    }

    public static void handle(HubStatePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.onHubState(p.json)));
        ctx.get().setPacketHandled(true);
    }
}
