package com.myriam.unrp.net;

import com.myriam.unrp.menu.MenuService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CreateOcPacket {
    public final String name;
    public final String schoolId;

    public CreateOcPacket(String name, String schoolId) {
        this.name = name;
        this.schoolId = schoolId;
    }

    public static void encode(CreateOcPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.name, 64);
        buf.writeUtf(p.schoolId, 64);
    }

    public static CreateOcPacket decode(FriendlyByteBuf buf) {
        return new CreateOcPacket(buf.readUtf(64), buf.readUtf(64));
    }

    public static void handle(CreateOcPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            if (sender != null) MenuService.handleCreateOc(sender, p.name, p.schoolId);
        });
        ctx.get().setPacketHandled(true);
    }
}
