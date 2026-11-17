package com.myriam.unrp.power;

import com.myriam.unrp.data.PlayerData;
import com.myriam.unrp.data.UnrpState;
import com.myriam.unrp.net.Net;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class AbilityUse {
    private AbilityUse() {}

    public static void use(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        UnrpState state = UnrpState.get(server);
        PlayerData data = state.get(player.getUuid());
        Power power = Powers.get(data.power);

        if (power == null) {
            player.sendMessage(Text.translatable("unrp.msg.no_power"), true);
            return;
        }

        long now = player.getServerWorld().getTime();
        if (now < data.cooldownUntil) {
            long seconds = (data.cooldownUntil - now + 19) / 20;
            player.sendMessage(Text.translatable("unrp.msg.cooldown", seconds), true);
            return;
        }
        if (data.aura < power.cost) {
            player.sendMessage(Text.translatable("unrp.msg.no_aura", power.cost), true);
            return;
        }

        data.aura -= power.cost;
        data.cooldownUntil = now + power.cooldown;
        power.ability.run(player, data);

        Net.sync(player, data);
        state.markDirty();
    }
}
