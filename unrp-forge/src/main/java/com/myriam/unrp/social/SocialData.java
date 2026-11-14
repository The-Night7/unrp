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

/** État social persistant : points d'entraînement, notifications, groupes, rangs, territoires. */
public class SocialData extends SavedData {
    private static final String NAME = "unrp_social";
    private static final Gson GSON = new Gson();

    public static class Note {
        public String id, text;
        public boolean read;
        public long time;
    }

    public static class Soc {
        /** points de stats disponibles, et barre de progression vers le prochain */
        public int pool, xp;
        /** points dépensés : power, defense, recovery, trick, speed */
        public int[] spent = new int[5];
        /** dixièmes de potentiel ajoutés par l'entraînement, et gain raccourci du dernier palier (0 = complet), par stat */
        public int[] bonus;
        public int[] partial;
        /** rangs de capacités débloqués (bits 2, 3, 4) ; voir ability/Unlocks */
        public int unlocked;
        /** ticks de jeu depuis la création du personnage, comptés tant que le niveau est < 2 (premier point d'aptitude) */
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

    /** Ce qui est réellement sauvegardé (sérialisé en JSON dans une seule chaîne). */
    public static class Store {
        public Map<String, Soc> players = new HashMap<>();
        public Map<String, Group> groups = new LinkedHashMap<>();
        /** école -> rang (king|queen|jack) -> uuid du joueur qui le détient (écrase l'Unodex) */
        public Map<String, Map<String, String>> ranks = new HashMap<>();
        public List<Turf> turfs = new ArrayList<>();

        public Soc soc(UUID id) {
            Soc s = players.computeIfAbsent(id.toString(), k -> new Soc());
            if (s.bonus == null) { // ancienne sauvegarde (points +1 par stat) : l'entraînement repart à zéro, comme le modèle
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
