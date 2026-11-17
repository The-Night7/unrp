package com.myriam.unrp.ability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent Covenant ledger, independent of whether its Sovereigns are online. */
public final class CovenantStateData extends SavedData {
    private String json = "";
    public static CovenantStateData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
            CovenantStateData::load, CovenantStateData::new, "unrp_covenant");
    }
    private static CovenantStateData load(CompoundTag tag) {
        CovenantStateData data = new CovenantStateData();
        data.json = tag.getString("ledger");
        return data;
    }
    public String read() {
        return json;
    }
    public void write(String value) {
        json = value;
        setDirty();
    }
    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("ledger", json);
        return tag;
    }
}
