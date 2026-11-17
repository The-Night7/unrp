package com.myriam.unrp.social;

import com.google.gson.Gson;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SocialData extends SavedData {
    private static final String NAME = "unrp_social";
    private static final Gson GSON = new Gson();

    public static class Note {
        public String id, text;
        public boolean read;
        public long time;
    }

    public static class Soc {

        public int pool, xp;

        public int[] spent = new int[5];

        public int[] bonus;
        public int[] partial;

        public int unlocked;

        public int playTicks;
        public List<Note> feed = new ArrayList<>();
        public com.myriam.unrp.data.StyleData style;
    }

    public static class Group {
        public String name, owner;
        public List<String> members = new ArrayList<>();
    }

    public static class Turf {
        public String id, name, school, dim;
        public double x, z, r = 24;
    }

    public static class Store {
        public Map<String, Soc> players = new HashMap<>();
        public Map<String, Group> groups = new LinkedHashMap<>();

        public Map<String, Map<String, String>> ranks = new HashMap<>();
        public List<Turf> turfs = new ArrayList<>();

        public Soc soc(UUID id) {
            Soc s = players.computeIfAbsent(id.toString(), k -> new Soc());
            if (s.bonus == null) {
                s.bonus = new int[5];
                s.partial = new int[5];
                s.spent = new int[5];
            }
            return s;
        }
    }

    public Store store = new Store();

    public static SocialData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(SocialData::load, SocialData::new, NAME);
    }

    public static SocialData load(CompoundTag tag) {
        SocialData d = new SocialData();
        Store s = GSON.fromJson(tag.getString("json"), Store.class);
        if (s != null) d.store = s;
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("json", GSON.toJson(store));
        return tag;
    }
}
