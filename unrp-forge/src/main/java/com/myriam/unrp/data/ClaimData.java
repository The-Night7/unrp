package com.myriam.unrp.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Qui joue quoi. Un joueur a au plus UN profil : soit un personnage existant de l'Unodex, soit un OC.
 * Un personnage "pris" ne doit plus apparaître comme PNJ (phase 3). Sauvegardé dans le monde.
 */
public class ClaimData extends SavedData {
    private static final String NAME = "unrp_claims";

    /** Le personnage joué par un joueur. */
    public static final class Profile {
        public static final String CHARACTER = "character";
        public static final String OC = "oc";

        public String kind;
        /** kind == character : id du personnage Unodex. */
        public String characterId;
        /** kind == oc : nom, école, rareté et niveau tirés. */
        public String ocName;
        public String ocSchool;
        public String tier;
        public double level;
        public boolean lifelong;
        /** Aura actuelle ; -1 = pas encore initialisée (sera pleine). */
        public int aura = -1;
        /** OC : capacité tirée à la création (id de capacité Unodex) ; null = aucune */
        public String ocAbility;
        /** OC : progression vers le niveau suivant (aura dépensée), voir ability/Mastery */
        public int masteryXp;
        /** OC : potentiels de départ des 5 stats en dixièmes (Puissance, Défense, Récupération, Ruse, Vitesse) ; null = pas encore tirés */
        public int[] pot;

        /** Id de la capacité jouée : celle du personnage de l'Unodex, ou celle tirée pour l'OC. */
        public String abilityOf(Models.Character c) {
            return c != null ? c.abilityId : ocAbility;
        }

        static Profile character(String id) {
            Profile p = new Profile();
            p.kind = CHARACTER;
            p.characterId = id;
            return p;
        }

        CompoundTag toTag() {
            CompoundTag t = new CompoundTag();
            t.putString("kind", kind);
            if (characterId != null) t.putString("character", characterId);
            if (ocName != null) t.putString("oc_name", ocName);
            if (ocSchool != null) t.putString("oc_school", ocSchool);
            if (tier != null) t.putString("tier", tier);
            t.putDouble("level", level);
            t.putBoolean("lifelong", lifelong);
            t.putInt("aura", aura);
            if (ocAbility != null) t.putString("oc_ability", ocAbility);
            t.putInt("mastery_xp", masteryXp);
            if (pot != null) t.putIntArray("pot", pot);
            return t;
        }

        static Profile fromTag(CompoundTag t) {
            Profile p = new Profile();
            p.kind = t.getString("kind");
            p.characterId = t.contains("character") ? t.getString("character") : null;
            p.ocName = t.contains("oc_name") ? t.getString("oc_name") : null;
            p.ocSchool = t.contains("oc_school") ? t.getString("oc_school") : null;
            p.tier = t.contains("tier") ? t.getString("tier") : null;
            p.level = Math.min(10.0, t.getDouble("level")); // anciens OC Exo-god tirés au-dessus de 10
            p.lifelong = t.getBoolean("lifelong");
            p.aura = t.contains("aura") ? t.getInt("aura") : -1;
            p.ocAbility = t.contains("oc_ability") ? t.getString("oc_ability") : null;
            p.masteryXp = t.getInt("mastery_xp");
            p.pot = t.contains("pot") && t.getIntArray("pot").length == 5 ? t.getIntArray("pot") : null;
            return p;
        }
    }

    private final Map<String, UUID> claims = new HashMap<>();
    private final Map<UUID, Profile> profiles = new HashMap<>();

    public static ClaimData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ClaimData::load, ClaimData::new, NAME);
    }

    public Map<UUID, Profile> allProfiles() {
        return java.util.Collections.unmodifiableMap(profiles);
    }

    public Optional<UUID> ownerOf(String characterId) {
        return Optional.ofNullable(claims.get(characterId));
    }

    public Optional<Profile> profileOf(UUID player) {
        return Optional.ofNullable(profiles.get(player));
    }

    public boolean isClaimed(String characterId) {
        return claims.containsKey(characterId);
    }

    public boolean ocNameTaken(String name) {
        return profiles.values().stream().anyMatch(p -> Profile.OC.equals(p.kind) && p.ocName.equalsIgnoreCase(name));
    }

    /** Candidature d'un joueur sans profil. @return false s'il a déjà un profil ou si le personnage est pris. */
    public boolean claimCharacter(String characterId, UUID player) {
        if (profiles.containsKey(player) || claims.containsKey(characterId)) return false;
        claims.put(characterId, player);
        profiles.put(player, Profile.character(characterId));
        setDirty();
        return true;
    }

    /** Attribution par un admin : remplace l'éventuel profil du joueur. @return false si le personnage est pris par un autre. */
    public boolean forceClaim(String characterId, UUID player) {
        UUID owner = claims.get(characterId);
        if (owner != null && !owner.equals(player)) return false;
        reset(player);
        return claimCharacter(characterId, player);
    }

    /** @return false si le joueur a déjà un profil ou si le nom est pris. */
    public boolean createOc(UUID player, String name, String school, String tier, double level, boolean lifelong) {
        if (profiles.containsKey(player) || ocNameTaken(name)) return false;
        Profile p = new Profile();
        p.kind = Profile.OC;
        p.ocName = name;
        p.ocSchool = school;
        p.tier = tier;
        p.level = Math.min(10.0, level); // le niveau maximum est 10
        p.lifelong = lifelong;
        profiles.put(player, p);
        setDirty();
        return true;
    }

    /** Capacités (ids canoniques) détenues par les joueurs, sauf celle(s) du profil "except" : personnage de l'Unodex réclamé ou capacité d'OC. */
    public java.util.Set<String> heldAbilities(Profile except) {
        UnodexData d = UnodexData.get();
        java.util.Set<String> out = new java.util.HashSet<>();
        for (Profile pr : profiles.values()) {
            if (pr == except) continue;
            String id = pr.abilityOf(pr.characterId == null ? null : d.characters.get(pr.characterId));
            if (id != null) out.add(d.canon(id));
        }
        return out;
    }

    /** Tire une capacité libre pour le joueur (jamais une capacité déjà détenue par un autre joueur). */
    public String rollFreeAbility(UUID player, java.util.Random rng) {
        return UnodexData.get().rollAbility(rng, heldAbilities(profiles.get(player)));
    }

    /** Un OC sans capacité (créé avant le tirage) en reçoit une maintenant, sauf Cripple et Cripple à vie. */
    public void ensureOcAbility(Profile pr) {
        if (pr == null || !Profile.OC.equals(pr.kind)) return;
        if (!pr.lifelong && pr.ocAbility == null && !"cripple".equals(pr.tier)) {
            pr.ocAbility = UnodexData.get().rollAbility(new java.util.Random(), heldAbilities(pr));
            setDirty();
        }
        if (pr.pot == null) { // OC créé avant le tirage des potentiels : Cripple = 1,0 partout, sinon plages de sa capacité
            pr.pot = pr.ocAbility == null ? new int[] {10, 10, 10, 10, 10} : UnodexData.get().rollPotentials(pr.ocAbility, new java.util.Random());
            setDirty();
        }
    }

    /** Enregistre la capacité tirée pour un OC. */
    public void setOcAbility(UUID player, String abilityId) {
        Profile p = profiles.get(player);
        if (p == null) return;
        p.ocAbility = abilityId;
        p.pot = abilityId == null ? new int[] {10, 10, 10, 10, 10} : UnodexData.get().rollPotentials(abilityId, new java.util.Random());
        setDirty();
    }

    /** Efface le profil du joueur (libère son personnage). */
    public boolean reset(UUID player) {
        boolean had = profiles.remove(player) != null;
        had |= claims.values().removeIf(player::equals);
        if (had) setDirty();
        return had;
    }

    /** Libère un personnage et efface le profil de son joueur. */
    public boolean release(String characterId) {
        UUID owner = claims.remove(characterId);
        if (owner == null) return false;
        profiles.remove(owner);
        setDirty();
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag list = new CompoundTag();
        claims.forEach(list::putUUID);
        tag.put("claims", list);
        ListTag plist = new ListTag();
        profiles.forEach((uuid, p) -> {
            CompoundTag t = p.toTag();
            t.putUUID("player", uuid);
            plist.add(t);
        });
        tag.put("profiles", plist);
        return tag;
    }

    public static ClaimData load(CompoundTag tag) {
        ClaimData d = new ClaimData();
        CompoundTag list = tag.getCompound("claims");
        for (String key : list.getAllKeys()) {
            d.claims.put(key, list.getUUID(key));
        }
        ListTag plist = tag.getList("profiles", Tag.TAG_COMPOUND);
        for (int i = 0; i < plist.size(); i++) {
            CompoundTag t = plist.getCompound(i);
            d.profiles.put(t.getUUID("player"), Profile.fromTag(t));
        }
        // Données de la phase 1 : un personnage pris sans profil devient un profil.
        d.claims.forEach((id, uuid) -> d.profiles.putIfAbsent(uuid, Profile.character(id)));
        return d;
    }
}
