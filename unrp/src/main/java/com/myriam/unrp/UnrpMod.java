package com.myriam.unrp;

import com.myriam.unrp.command.UnrpCommands;
import com.myriam.unrp.data.PlayerData;
import com.myriam.unrp.data.UnrpState;
import com.myriam.unrp.net.Net;
import com.myriam.unrp.power.Powers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class UnrpMod implements ModInitializer {
    public static final String MOD_ID = "unrp";

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        Powers.init();
        Net.registerServer();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                UnrpCommands.register(dispatcher));

        // À la connexion, on envoie l'état au client pour que le HUD soit à jour.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerData data = UnrpState.get(server).get(handler.player.getUuid());
            Net.sync(handler.player, data);
        });

        ServerTickEvents.END_SERVER_TICK.register(UnrpMod::tick);
    }

    /** Une fois par seconde : régénération de l'aura. */
    private static void tick(MinecraftServer server) {
        if (server.getTicks() % 20 != 0) return;
        UnrpState state = UnrpState.get(server);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerData data = state.get(player.getUuid());
            if (data.power.isEmpty()) continue;
            int max = data.maxAura();
            if (data.aura < max) {
                data.aura = Math.min(max, data.aura + data.regen());
                Net.sync(player, data);
            }
        }
        state.markDirty();
    }
}
