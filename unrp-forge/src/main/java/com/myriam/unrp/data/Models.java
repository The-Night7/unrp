package com.myriam.unrp.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Modèles des fichiers JSON générés. Les noms de champs correspondent aux clés JSON en snake_case
 * (ex. ability_id -> abilityId). Les listes/objets absents du JSON restent à leur valeur par défaut.
 */
public final class Models {
    private Models() {}

    public static class Stats {
        public Double power, defense, recovery, trick, speed;
    }

    public static class Membership {
        public String school;
        public List<String> roles = new ArrayList<>();
        public String region;
    }

    public static class Snapshot {
        public Double level;
        public String tier;
        public String ability;
        public String abilityId;
        public String type;
        public String nature;
        public Stats stats;
        public Double rate;
        public Stats bonuses;
        public String description;
        public String source;
    }

    public static class Character {
        public String id;
        public String name;
        /** lore | rp */
        public String category;
        public List<String> sources = new ArrayList<>();
        public List<Membership> schools = new ArrayList<>();
        public String tier;
        public Double level;
        public String abilityId;
        public Stats stats;
        public String sex;
        public Integer heightCm;
        public String color;
        /** missing | deceased | null */
        public String status;
        public List<String> charges = new ArrayList<>();
        public String reference;
        public String image;
        public List<Snapshot> snapshots = new ArrayList<>();
    }

    /** Personnage à rang dans une école (roi, reine, valet, soigneur). */
    public static class Leader {
        public String characterId;
        /** king | queen | jack | healer */
        public String role;
    }

    public static class Campus {
        public String variant;
        public String palette;
        public String notes;
    }

    public static class School {
        public String id;
        public String name;
        /** school | organization */
        public String kind;
        public boolean kindConfirmed;
        public List<String> regions = new ArrayList<>();
        public List<Leader> leaders = new ArrayList<>();
        public Campus campus = new Campus();
        public int memberCount;
        public List<String> members = new ArrayList<>();
    }

    public static class Impl {
        /** todo | wip | done */
        public String status = "todo";
        public String palladiumPower;
        public String notes;
    }

    public static class Ability {
        public String id;
        public String name;
        public List<String> aliases = new ArrayList<>();
        public String type;
        public String nature;
        public String description;
        public List<String> users = new ArrayList<>();
        public Double levelMin, levelMax;
        public int wave;
        public Impl impl = new Impl();
    }
}
