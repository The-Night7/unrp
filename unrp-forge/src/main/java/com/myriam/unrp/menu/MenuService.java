package com.myriam.unrp.menu;

import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.data.Models;
import com.myriam.unrp.data.RarityTable;
import com.myriam.unrp.data.RarityTier;
import com.myriam.unrp.data.UnodexData;
import com.myriam.unrp.net.MenuDataPacket;
import com.myriam.unrp.net.Net;
import com.myriam.unrp.player.PlayerStateService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MenuService {
    private static final Random RNG = new Random();
    private static final Pattern OC_NAME = Pattern.compile("^[\\p{L}0-9 '\\-]{2,24}$");
    private static final Map<String, String> ROLE_LABELS = Map.of(
            "king", "King", "queen", "Queen", "jack", "Jack", "healer", "Healer",
            "alumni", "Alumni", "hidden", "hidden");
    private static final Map<String, String> TIER_FR = Map.of(
            "cripple", "Cripple", "low", "Low", "mid", "Mid", "elite", "Elite",
            "high", "High", "god", "God", "unknown", "Unknown");

    private MenuService() {}

    // ------------------------------------------------------------ envoi
    public static void onLogin(ServerPlayer player) {
        if (ClaimData.get(player.server).profileOf(player.getUUID()).isEmpty()) {
            send(player, true, false, "");
        }
    }

    public static void send(ServerPlayer player, boolean open, boolean close, String message) {
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), build(player.server, player, open, close, message));
    }

    private static void refreshOthers(MinecraftServer server, ServerPlayer except) {
        ClaimData claims = ClaimData.get(server);
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other != except && claims.profileOf(other.getUUID()).isEmpty()) send(other, false, false, "");
        }
    }

    static final java.util.Set<String> IGNORED_GROUPS = java.util.Set.of("vigilante");

    static boolean isIgnored(Models.Character c) {
        return !c.schools.isEmpty() && c.schools.stream().allMatch(m -> IGNORED_GROUPS.contains(m.school));
    }

    static MenuDataPacket build(MinecraftServer server, ServerPlayer player, boolean open, boolean close, String message) {
        UnodexData data = UnodexData.get();
        ClaimData claims = ClaimData.get(server);
        MenuDataPacket p = new MenuDataPacket();
        p.open = open;
        p.close = close;
        p.hasProfile = claims.profileOf(player.getUUID()).isPresent();
        p.message = message;

        List<Models.Character> chars = data.characters.values().stream()
                .filter(c -> !isIgnored(c))
                .sorted(Comparator.comparing((Models.Character c) -> c.name.toLowerCase(Locale.ROOT))).toList();
        for (Models.Character c : chars) {
            MenuDataPacket.Entry e = new MenuDataPacket.Entry();
            e.id = c.id;
            e.name = c.name;
            e.category = c.category;
            e.tier = c.tier == null ? "unknown" : c.tier;
            e.level = c.level == null ? "?" : String.format(Locale.ROOT, "%.1f", c.level);
            Models.Ability ab = c.abilityId == null ? null : data.abilities.get(c.abilityId);
            e.ability = ab != null ? ab.name : (c.abilityId == null ? "None" : c.abilityId);
            e.abilityDesc = ab == null || ab.description == null ? "" : cut(ab.description, 390);
            e.schools = c.schools.stream().map(m -> {
                Models.School s = data.schools.get(m.school);
                String n = s != null ? s.name : m.school;
                String roles = m.roles.stream().map(r -> ROLE_LABELS.getOrDefault(r, r)).collect(Collectors.joining(", "));
                return roles.isEmpty() ? n : n + " (" + roles + ")";
            }).collect(Collectors.joining(", "));
            e.schools = cut(e.schools, 150);
            e.claimed = claims.isClaimed(c.id);
            p.entries.add(e);
        }
        for (Models.School s : data.schools.values()) {
            if (!"school".equals(s.kind)) continue;
            MenuDataPacket.SchoolInfo si = new MenuDataPacket.SchoolInfo();
            si.id = s.id;
            si.name = s.name;
            p.schools.add(si);
        }
        if (data.rarity != null) {
            for (RarityTier t : data.rarity.tiers()) {
                MenuDataPacket.RarityInfo r = new MenuDataPacket.RarityInfo();
                r.label = t.label;
                r.percent = 100.0 * data.rarity.probability(t);
                p.rarities.add(r);
            }
        }
        if (data.characters.isEmpty() && message.isEmpty()) {
            p.message = "No Unodex data loaded. Contact an administrator.";
        }
        return p;
    }

    private static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    // ------------------------------------------------------- demandes
    public static void handleClaim(ServerPlayer player, String characterId) {
        ClaimData claims = ClaimData.get(player.server);
        if (claims.profileOf(player.getUUID()).isPresent()) {
            send(player, false, true, "");
            return;
        }
        Models.Character c = UnodexData.get().characters.get(characterId);
        if (c == null || isIgnored(c)) {
            send(player, false, false, "Unknown character.");
            return;
        }
        if (!claims.claimCharacter(characterId, player.getUUID())) {
            send(player, false, false, c.name + " was just claimed by another player.");
            return;
        }
        player.sendSystemMessage(Component.literal("You now play " + c.name + "."));
        send(player, false, true, "");
        PlayerStateService.send(player);
        refreshOthers(player.server, player);
    }

    public static void handleCreateOc(ServerPlayer player, String rawName, String schoolId) {
        ClaimData claims = ClaimData.get(player.server);
        UnodexData data = UnodexData.get();
        if (claims.profileOf(player.getUUID()).isPresent()) {
            send(player, false, true, "");
            return;
        }
        String name = rawName == null ? "" : rawName.trim().replaceAll("\\s+", " ");
        if (!OC_NAME.matcher(name).matches()) {
            send(player, false, false, "Invalid name: use 2–24 letters, numbers, spaces, apostrophes or hyphens.");
            return;
        }
        boolean nameTaken = claims.ocNameTaken(name)
                || data.characters.values().stream().anyMatch(c -> c.name.equalsIgnoreCase(name));
        if (nameTaken) {
            send(player, false, false, "That name is already in use.");
            return;
        }
        Models.School school = data.schools.get(schoolId);
        if (school == null || !"school".equals(school.kind)) {
            send(player, false, false, "Invalid school.");
            return;
        }
        RarityTable table = data.rarity;
        if (table == null) {
            send(player, false, false, "Rarity table not loaded. Contact an administrator.");
            return;
        }
        RarityTable.Roll roll = table.roll(RNG);
        if (!claims.createOc(player.getUUID(), name, school.id, roll.tier.id, roll.level, roll.tier.lifelong)) {
            send(player, false, false, "Could not create this OC.");
            return;
        }

        String abilityId = null;
        if (!roll.tier.lifelong && !"cripple".equals(roll.tier.id)) abilityId = claims.rollFreeAbility(player.getUUID(), RNG);
        claims.setOcAbility(player.getUUID(), abilityId);
        Models.Ability drawn = abilityId == null ? null : data.abilities.get(abilityId);
        player.sendSystemMessage(Component.literal("OC created: " + name + " (" + school.name + ") - rarity "
                + roll.tier.label + ", level " + String.format(Locale.ROOT, "%.1f", roll.level)
                + (roll.tier.lifelong ? " - permanently without an ability"
                : abilityId == null ? " - no free ability available yet (all are taken)"
                : " - ability rolled: " + (drawn != null ? drawn.name : abilityId)) + "."));
        send(player, false, true, "");
        PlayerStateService.send(player);
    }

    public static String tierLabel(String id) {
        return TIER_FR.getOrDefault(id, id);
    }
}
