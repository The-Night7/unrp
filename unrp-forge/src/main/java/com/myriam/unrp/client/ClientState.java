package com.myriam.unrp.client;

import com.myriam.unrp.net.PlayerStatePacket;

public final class ClientState {
    private static PlayerStatePacket current = new PlayerStatePacket();
    private static long receivedAt;

    private ClientState() {}

    public static PlayerStatePacket get() {
        return current;
    }

    static void set(PlayerStatePacket p) {
        current = p;
        net.minecraft.client.multiplayer.ClientLevel l = net.minecraft.client.Minecraft.getInstance().level;
        receivedAt = l == null ? 0 : l.getGameTime();
    }

    public static float cooldownLeft(int i, float partial) {
        PlayerStatePacket s = current;
        if (i < 0 || i >= s.moveLeft.length) return 0;
        net.minecraft.client.multiplayer.ClientLevel l = net.minecraft.client.Minecraft.getInstance().level;
        if (l == null) return 0;
        return Math.max(0f, s.moveLeft[i] - (l.getGameTime() - receivedAt + partial));
    }
}
