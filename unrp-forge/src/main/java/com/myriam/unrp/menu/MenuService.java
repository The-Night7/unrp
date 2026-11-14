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

/** Logique serveur du menu de candidature : construit ce que voit le joueur et valide ses demandes. */
public final class MenuService {
    private static final Random RNG = new Random();
    private static final Pattern OC_NAME = Pattern.compile("^[\\p{L}0-9 '\\-]{2,24}$");
    private static final Map<String, String> ROLE_FR = Map.of(
            "king", "Roi", "queen", "Reine", "jack", "Valet", "healer", "Soigneur",
            "alumni", "ancien", "hidden", "caché");
    private static final Map<String, String> TIER_FR = Map.of(
            "cripple", "Cripple", "low", "Low", "mid", "Mid", "elite", "Elite",
            "high", "High", "god", "God", "unknown", "Inconnu");

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

    /** Rafraîchit les menus ouverts des autres joueurs (un personnage vient d'être pris). */
    private static void refreshOthers(MinecraftServer server, ServerPlayer except) {
        ClaimData claims = ClaimData.get(server);
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other != except && claims.profileOf(other.getUUID()).isEmpty()) send(other, false, false, "");
        }
    }

    /** Groupes volontairement mis de côté (demande : on ignore les Vigilante). Pour les réactiver, vider la liste. */
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
            e.ability = ab != null ? ab.name : (c.abilityId == null ? "Aucune" : c.abilityId);
            e.abilityDesc = ab == null || ab.description == null ? "" : cut(ab.description, 390);
            e.schools = c.schools.stream().map(m -> {
                Models.School s = data.schools.get(m.school);
                String n = s != null ? s.name : m.school;
                String roles = m.roles.stream().map(r -> ROLE_FR.getOrDefault(r, r)).collect(Collectors.joining(", "));
                return roles.isEmpty() ? n : n + " (" + roles + ")";
            }).collect(Collectors.joining(", "));
            e.schools = cut(e.schools, 150);
            e.claimed = claims.isClaimed(c.id);
            p.entries.add(e);
        }
        for (Models.School s : data.schools.values()) {
            if (!"school".equals(s.kind)) continue; // les OC rejoignent des écoles, pas des groupes
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
            p.message = "Aucune donnée Unodex chargée : préviens un admin.";
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
            send(player, false, false, "Personnage inconnu.");
            return;
        }
        if (!claims.claimCharacter(characterId, player.getUUID())) {
            send(player, false, false, c.name + " vient d'être pris par quelqu'un d'autre.");
            return;
        }
        player.sendSystemMessage(Component.literal("Tu joues maintenant " + c.name + "."));
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
            send(player, false, false, "Nom invalide : 2 à 24 caractères (lettres, chiffres, espace, ' ou -).");
            return;
        }
        boolean nameTaken = claims.ocNameTaken(name)
                || data.characters.values().stream().anyMatch(c -> c.name.equalsIgnoreCase(name));
        if (nameTaken) {
            send(player, false, false, "Ce nom est déjà utilisé.");
            return;
        }
        Models.School school = data.schools.get(schoolId);
        if (school == null || !"school".equals(school.kind)) {
            send(player, false, false, "École invalide.");
            return;
        }
        RarityTable table = data.rarity;
        if (table == null) {
            send(player, false, false, "Table de rareté non chargée : préviens un admin.");
            return;
        }
        RarityTable.Roll roll = table.roll(RNG);
        if (!claims.createOc(player.getUUID(), name, school.id, roll.tier.id, roll.level, roll.tier.lifelong)) {
            send(player, false, false, "Impossible de créer cet OC.");
            return;
        }
        // tirage de capacité (pool éditable : ability_pool.json) ; pas pour un Cripple, à vie ou non
        String abilityId = null;
        if (!roll.tier.lifelong && !"cripple".equals(roll.tier.id)) abilityId = claims.rollFreeAbility(player.getUUID(), RNG);
        claims.setOcAbility(player.getUUID(), abilityId); // pose aussi les potentiels de départ des 5 stats
        Models.Ability drawn = abilityId == null ? null : data.abilities.get(abilityId);
        player.sendSystemMessage(Component.literal("OC créé : " + name + " (" + school.name + ") - rareté "
                + roll.tier.label + ", niveau " + String.format(Locale.ROOT, "%.1f", roll.level)
                + (roll.tier.lifelong ? " - ne pourra jamais avoir de capacité"
                : abilityId == null ? " - aucune capacité libre pour l'instant (elles sont toutes prises)"
                : " - capacité tirée : " + (drawn != null ? drawn.name : abilityId)) + "."));
        send(player, false, true, "");
        PlayerStateService.send(player);
    }

    /** Libellé français d'un tier de l'Unodex (le client reçoit l'id). */
    public static String tierLabel(String id) {
        return TIER_FR.getOrDefault(id, id);
    }
}
