package com.myriam.unrp.net;

import com.myriam.unrp.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Serveur -> clients : le joueur "entity" joue la pose "clip" (vide = arrêter). hold > 0 : tenue tant de ticks. */
public class PosePacket {
    public final int entity;
    public final String clip;
    public final int hold;

    public PosePacket(int entity, String clip, int hold) {
        this.entity = entity;
        this.clip = clip;
        this.hold = hold;
    }

    public static void encode(PosePacket p, FriendlyByteBuf b) {
        b.writeVarInt(p.entity);
        b.writeUtf(p.clip, 64);
        b.writeVarInt(p.hold);
    }

    public static PosePacket decode(FriendlyByteBuf b) {
        return new PosePacket(b.readVarInt(), b.readUtf(64), b.readVarInt());
    }

    public static void handle(PosePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.onPose(p)));
        ctx.get().setPacketHandled(true);
    }
}
