package com.myriam.unrp.data;

import com.myriam.unrp.UnrpMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UnrpState extends PersistentState {
    private final Map<UUID, PlayerData> players = new HashMap<>();

    public static UnrpState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(UnrpState::fromNbt, UnrpState::new, UnrpMod.MOD_ID);
    }

    public PlayerData get(UUID id) {
        return players.computeIfAbsent(id, k -> new PlayerData());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound list = new NbtCompound();
        players.forEach((id, data) -> list.put(id.toString(), data.toNbt()));
        nbt.put("players", list);
        return nbt;
    }

    public static UnrpState fromNbt(NbtCompound nbt) {
        UnrpState state = new UnrpState();
        NbtCompound list = nbt.getCompound("players");
        for (String key : list.getKeys()) {
            state.players.put(UUID.fromString(key), PlayerData.fromNbt(list.getCompound(key)));
        }
        return state;
    }
}
