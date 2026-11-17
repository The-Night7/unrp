package com.myriam.unrp.net;

import com.myriam.unrp.UnrpMod;
import com.myriam.unrp.data.PlayerData;
import com.myriam.unrp.power.AbilityUse;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class Net {
    public static final Identifier SYNC = UnrpMod.id("sync");
    public static final Identifier USE = UnrpMod.id("use");

    private Net() {}

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(USE, (server, player, handler, buf, responseSender) ->
                server.execute(() -> AbilityUse.use(player)));
    }

    public static void sync(ServerPlayerEntity player, PlayerData data) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(data.power);
        buf.writeInt(data.aura);
        buf.writeInt(data.maxAura());
        for (int stat : data.stats) buf.writeInt(stat);
        ServerPlayNetworking.send(player, SYNC, buf);
    }
}
