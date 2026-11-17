package com.myriam.unrp.pass;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PassStatePacket {
    public final UUID player;
    public final List<String> paths;
    public final List<List<String>> keys;
    public final List<String> common;
    public final boolean am;
    public final int locked;
    public final boolean transformed;

    public PassStatePacket(UUID player, List<String> paths, List<List<String>> keys, List<String> common, boolean am, int locked, boolean transformed) {
        this.player = player;
        this.paths = paths;
        this.keys = keys;
        this.common = common;
        this.am = am;
        this.locked = locked;
        this.transformed = transformed;
    }

    public static void encode(PassStatePacket p, FriendlyByteBuf buf) {
        buf.writeUUID(p.player);
        buf.writeVarInt(p.paths.size());
        for (int i = 0; i < p.paths.size(); i++) {
            buf.writeUtf(p.paths.get(i), 64);
            List<String> k = p.keys.get(i);
            buf.writeVarInt(k.size());
            for (String s : k) buf.writeUtf(s, 64);
        }
        buf.writeVarInt(p.common.size());
        for (String s : p.common) buf.writeUtf(s, 64);
        buf.writeBoolean(p.am);
        buf.writeVarInt(p.locked + 1);
        buf.writeBoolean(p.transformed);
    }

    public static PassStatePacket decode(FriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        int n = Math.min(32, buf.readVarInt());
        List<String> paths = new ArrayList<>(n);
        List<List<String>> keys = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            paths.add(buf.readUtf(64));
            int m = Math.min(64, buf.readVarInt());
            List<String> k = new ArrayList<>(m);
            for (int j = 0; j < m; j++) k.add(buf.readUtf(64));
            keys.add(k);
        }
        int c = Math.min(128, buf.readVarInt());
        List<String> common = new ArrayList<>(c);
        for (int j = 0; j < c; j++) common.add(buf.readUtf(64));
        return new PassStatePacket(id, paths, keys, common, buf.readBoolean(), buf.readVarInt() - 1, buf.readBoolean());
    }

    public static void handle(PassStatePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PassBridge.accept(p)));
        ctx.get().setPacketHandled(true);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PassStatePacket q && q.player.equals(player) && q.paths.equals(paths) && q.keys.equals(keys)
                && q.common.equals(common) && q.am == am && q.locked == locked && q.transformed == transformed;
    }

    @Override
    public int hashCode() {
        return player.hashCode() * 31 + paths.hashCode() * 7 + keys.hashCode() + common.hashCode() + locked;
    }
}
