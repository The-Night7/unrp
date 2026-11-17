package com.myriam.unrp.net;

import com.myriam.unrp.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FxPacket {
    public static final int LINE = 0, SHELL = 1, SHELL_GONE = 2, SHIELD = 3, WAVE = 4, GLOW = 5;

    public int kind, id, color, life, extra;
    public double x1, y1, z1, x2, y2, z2;
    public float a;

    public static void encode(FxPacket p, FriendlyByteBuf b) {
        b.writeByte(p.kind);
        b.writeVarInt(p.id);
        b.writeInt(p.color);
        b.writeVarInt(p.life);
        b.writeVarInt(p.extra);
        b.writeDouble(p.x1);
        b.writeDouble(p.y1);
        b.writeDouble(p.z1);
        b.writeDouble(p.x2);
        b.writeDouble(p.y2);
        b.writeDouble(p.z2);
        b.writeFloat(p.a);
    }

    public static FxPacket decode(FriendlyByteBuf b) {
        FxPacket p = new FxPacket();
        p.kind = b.readByte();
        p.id = b.readVarInt();
        p.color = b.readInt();
        p.life = b.readVarInt();
        p.extra = b.readVarInt();
        p.x1 = b.readDouble();
        p.y1 = b.readDouble();
        p.z1 = b.readDouble();
        p.x2 = b.readDouble();
        p.y2 = b.readDouble();
        p.z2 = b.readDouble();
        p.a = b.readFloat();
        return p;
    }

    public static void handle(FxPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.onFx(p)));
        ctx.get().setPacketHandled(true);
    }
}
