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

public class ClaimData extends SavedData {
    private static final String NAME = "unrp_claims";

    public static final class Profile {
        public static final String CHARACTER = "character";
        public static final String OC = "oc";

        public String kind;

        public String characterId;

        public String ocName;
        public String ocSchool;
        public String tier;
        public double level;
        public boolean lifelong;

        public int aura = -1;

        public String ocAbility;

        public int masteryXp;

        public int[] pot;

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
            p.level = Math.min(10.0, t.getDouble("level"));
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

    public boolean claimCharacter(String characterId, UUID player) {
        if (profiles.containsKey(player) || claims.containsKey(characterId)) return false;
        claims.put(characterId, player);
        profiles.put(player, Profile.character(characterId));
        setDirty();
        return true;
    }

    public boolean forceClaim(String characterId, UUID player) {
        UUID owner = claims.get(characterId);
        if (owner != null && !owner.equals(player)) return false;
        reset(player);
        return claimCharacter(characterId, player);
    }

    public boolean createOc(UUID player, String name, String school, String tier, double level, boolean lifelong) {
        if (profiles.containsKey(player) || ocNameTaken(name)) return false;
        Profile p = new Profile();
        p.kind = Profile.OC;
        p.ocName = name;
        p.ocSchool = school;
        p.tier = tier;
        p.level = Math.min(10.0, level);
        p.lifelong = lifelong;
        profiles.put(player, p);
        setDirty();
        return true;
    }

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

    public String rollFreeAbility(UUID player, java.util.Random rng) {
        return UnodexData.get().rollAbility(rng, heldAbilities(profiles.get(player)));
    }

    public void ensureOcAbility(Profile pr) {
        if (pr == null || !Profile.OC.equals(pr.kind)) return;
        if (!pr.lifelong && pr.ocAbility == null && !"cripple".equals(pr.tier)) {
            pr.ocAbility = UnodexData.get().rollAbility(new java.util.Random(), heldAbilities(pr));
            setDirty();
        }
        if (pr.pot == null) {
            pr.pot = pr.ocAbility == null ? new int[] {10, 10, 10, 10, 10} : UnodexData.get().rollPotentials(pr.ocAbility, new java.util.Random());
            setDirty();
        }
    }

    public void setOcAbility(UUID player, String abilityId) {
        Profile p = profiles.get(player);
        if (p == null) return;
        p.ocAbility = abilityId;
        p.pot = abilityId == null ? new int[] {10, 10, 10, 10, 10} : UnodexData.get().rollPotentials(abilityId, new java.util.Random());
        setDirty();
    }

    public boolean reset(UUID player) {
        boolean had = profiles.remove(player) != null;
        had |= claims.values().removeIf(player::equals);
        if (had) setDirty();
        return had;
    }

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

        d.claims.forEach((id, uuid) -> d.profiles.putIfAbsent(uuid, Profile.character(id)));
        return d;
    }
}
