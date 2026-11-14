package com.myriam.unrp.social;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.data.Models;
import com.myriam.unrp.data.UnodexData;
import com.myriam.unrp.net.HubStatePacket;
import com.myriam.unrp.net.Net;
import com.myriam.unrp.net.PlayerStatePacket;
import com.myriam.unrp.player.PlayerStateService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tout le côté serveur du hub : entraînement, groupes, école et rangs, duels, territoires, notifications.
 * Le client envoie une action (HubActionPacket), le serveur vérifie tout puis renvoie l'état complet (JSON).
 */
public final class SocialService {
    public static final int DUEL_RANGE = 14, NEAR_RANGE = 64, XP_PER_POINT = 30;
    private static final long OFFER_TTL = 20L * 60;
    private static final String[] RANKS = {"king", "queen", "jack"};
    private static final String[] RANK_FR = {"Roi", "Reine", "Valet"};
    private static final AtomicInteger IDS = new AtomicInteger();

    record Offer(String id, String kind, UUID from, UUID to, String text, String stake, long expires) {}

    static final class Duel {
        UUID a, b;
        String kind, stake;
    }

    static final List<Offer> OFFERS = new ArrayList<>();
    static final List<Duel> DUELS = new ArrayList<>();

    private SocialService() {}

    // ------------------------------------------------------------------ outils
    static ClaimData.Profile profile(MinecraftServer s, UUID id) {
        return ClaimData.get(s).profileOf(id).orElse(null);
    }

    static Models.Character character(ClaimData.Profile pr) {
        return pr != null && ClaimData.Profile.CHARACTER.equals(pr.kind) ? UnodexData.get().characters.get(pr.characterId) : null;
    }

    static String name(MinecraftServer s, UUID id) {
        ClaimData.Profile pr = profile(s, id);
        Models.Character c = character(pr);
        if (c != null) return c.name;
        if (pr != null && pr.ocName != null) return pr.ocName;
        ServerPlayer sp = s.getPlayerList().getPlayer(id);
        return sp != null ? sp.getGameProfile().getName() : "?";
    }

    static String schoolOf(MinecraftServer s, UUID id) {
        ClaimData.Profile pr = profile(s, id);
        if (pr == null) return null;
        Models.Character c = character(pr);
        if (c == null) return pr.ocSchool;
        for (Models.Membership m : c.schools) {
            Models.School sc = UnodexData.get().schools.get(m.school);
            if (sc != null && "school".equals(sc.kind)) return sc.id;
        }
        return null;
    }

    /** Détenteur d'un rang : le joueur désigné, sinon le personnage de l'Unodex (et son joueur s'il est pris). */
    record Holder(UUID uuid, String name) {}

    static Holder holder(MinecraftServer s, String school, String rank) {
        Map<String, String> over = SocialData.get(s).store.ranks.get(school);
        if (over != null && over.containsKey(rank)) {
            UUID u = UUID.fromString(over.get(rank));
            return new Holder(u, name(s, u));
        }
        Models.School sc = UnodexData.get().schools.get(school);
        if (sc == null) return new Holder(null, null);
        for (Models.Leader l : sc.leaders) {
            if (rank.equals(l.role)) {
                Models.Character c = UnodexData.get().characters.get(l.characterId);
                UUID owner = ClaimData.get(s).ownerOf(l.characterId).orElse(null);
                return new Holder(owner, c != null ? c.name : l.characterId);
            }
        }
        return new Holder(null, null);
    }

    static String rankOf(MinecraftServer s, UUID id, String school) {
        if (school == null) return null;
        for (String r : RANKS) if (id.equals(holder(s, school, r).uuid)) return r;
        return null;
    }

    static String rankFr(String rank) {
        for (int i = 0; i < RANKS.length; i++) if (RANKS[i].equals(rank)) return RANK_FR[i];
        return rank;
    }

    /** Alliés : même groupe, ou même école. */
    public static boolean allies(MinecraftServer s, UUID a, UUID b) {
        SocialData d = SocialData.get(s);
        SocialData.Group ga = groupOf(d, a);
        if (ga != null && ga == groupOf(d, b)) return true;
        String sa = schoolOf(s, a);
        return sa != null && sa.equals(schoolOf(s, b));
    }

    /** GroupManager.sameGroup du modèle : même groupe du hub (ou la même personne). */
    public static boolean sameGroup(MinecraftServer s, UUID a, UUID b) {
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;
        SocialData.Group g = groupOf(SocialData.get(s), a);
        return g != null && g.members.contains(b.toString());
    }

    /** GroupManager.allied du modèle : même entité ou alliées au sens de Minecraft (équipes). */
    public static boolean allied(net.minecraft.world.entity.Entity a, net.minecraft.world.entity.Entity b) {
        return a != null && b != null && (a == b || a.isAlliedTo(b));
    }

    static SocialData.Group groupOf(SocialData d, UUID id) {
        for (SocialData.Group g : d.store.groups.values()) if (g.members.contains(id.toString())) return g;
        return null;
    }

    static Duel duelOf(UUID id) {
        for (Duel d : DUELS) if (d.a.equals(id) || d.b.equals(id)) return d;
        return null;
    }

    static void pushOffer(Offer o) {
        OFFERS.removeIf(x -> x.from.equals(o.from) && x.to.equals(o.to) && x.kind.equals(o.kind));
        OFFERS.add(o);
    }

    public static void note(MinecraftServer s, UUID to, String text) {
        SocialData d = SocialData.get(s);
        SocialData.Note n = new SocialData.Note();
        n.id = "n" + IDS.incrementAndGet() + "_" + System.nanoTime();
        n.text = text;
        n.time = System.currentTimeMillis();
        List<SocialData.Note> feed = d.store.soc(to).feed;
        feed.add(0, n);
        while (feed.size() > 40) feed.remove(feed.size() - 1);
        d.setDirty();
        ServerPlayer sp = s.getPlayerList().getPlayer(to);
        if (sp != null) {
            sp.displayClientMessage(Component.literal(text), true);
            sendState(sp);
        }
    }

    // ------------------------------------------------------------------ entraînement
    /** Plus de points d'entraînement par le combat : comme le modèle, les points de stats viennent des niveaux (voir Training). */
    public static void addXp(ServerPlayer p, int amount) {
        // volontairement vide
    }

    /** Points de stats disponibles : 1 par niveau (comme les points d'aptitude), moins le coût des paliers déjà pris. */
    public static int statPoints(ServerPlayer p) {
        SocialData.Soc soc = SocialData.get(p.server).store.soc(p.getUUID());
        return Math.max(0, com.myriam.unrp.ability.Unlocks.total(p.server, p) - com.myriam.unrp.player.Training.spent(soc.spent));
    }

    private static void note(ServerPlayer p, String s) {
        p.displayClientMessage(Component.literal(s), true);
    }

    /** Un palier d'entraînement (delta > 0) ou son retrait (delta < 0), règles exactes du modèle. */
    private static void train(ServerPlayer p, String idx, int delta) {
        int i = parse(idx, -1);
        if (i < 0 || i > 4) return;
        SocialData d = SocialData.get(p.server);
        SocialData.Soc soc = d.store.soc(p.getUUID());
        ClaimData.Profile pr = profile(p.server, p.getUUID());
        com.myriam.unrp.data.Models.Character ch = character(pr);
        int pot = PlayerStateService.potentialBase(pr, ch)[i] + soc.bonus[i];
        if (delta > 0) {
            if (soc.spent[i] >= com.myriam.unrp.player.Training.MAX_STEPS) {
                note(p, "§cEntraînement maximal (+2,0).");
                return;
            }
            int gain = com.myriam.unrp.player.Training.gain(pot);
            if (gain <= 0) {
                note(p, "§cCette stat est au plafond de 10,0.");
                return;
            }
            int cost = com.myriam.unrp.player.Training.cost(soc.spent[i]);
            if (statPoints(p) < cost) {
                note(p, "§cIl te faut " + cost + " point(s) de stats.");
                return;
            }
            soc.bonus[i] += gain;
            soc.partial[i] = gain < com.myriam.unrp.player.Training.STEP ? gain : 0;
            soc.spent[i]++;
        } else {
            if (soc.spent[i] <= 0) {
                note(p, "§cRien à rendre.");
                return;
            }
            soc.bonus[i] -= com.myriam.unrp.player.Training.refundTenths(soc.partial[i]);
            soc.partial[i] = 0;
            soc.spent[i]--;
        }
        d.setDirty();
        PlayerStateService.send(p);
    }

    // ------------------------------------------------------------------ actions
    public static void action(ServerPlayer p, String act, String a, String b) {
        MinecraftServer s = p.server;
        ClaimData.Profile pr = profile(s, p.getUUID());
        if (pr == null) return;
        SocialData d = SocialData.get(s);
        Set<UUID> touched = new HashSet<>();
        touched.add(p.getUUID());
        OFFERS.removeIf(o -> s.overworld().getGameTime() > o.expires);
        switch (act) {
            case "open" -> {}
            case "unlock" -> {
                p.displayClientMessage(Component.literal("§b" + com.myriam.unrp.ability.Unlocks.buy(p, parse(a, 0))), true);
                PlayerStateService.send(p);
            }
            case "train" -> train(p, a, 1);
            case "untrain" -> train(p, a, -1);
            case "read_all" -> {
                d.store.soc(p.getUUID()).feed.forEach(n -> n.read = true);
                d.setDirty();
            }
            case "group_create" -> groupCreate(p, a, d);
            case "group_invite" -> invite(p, a, "group", "t'invite dans son groupe", null, touched);
            case "group_leave" -> groupLeave(p, d, touched);
            case "group_disband" -> groupDisband(p, d, touched);
            case "group_kick" -> groupKick(p, a, d, touched);
            case "duel_friendly" -> invite(p, a, "friendly", "te propose un duel amical", null, touched);
            case "duel_formal" -> formalChallenge(p, touched);
            case "duel_yield" -> {
                Duel du = duelOf(p.getUUID());
                if (du != null) end(s, du, du.a.equals(p.getUUID()) ? du.b : du.a, "a abandonné");
            }
            case "offer_accept" -> respond(p, a, true, touched);
            case "offer_decline" -> respond(p, a, false, touched);
            case "school_promote" -> promote(p, a, b, touched);
            case "school_revoke" -> revoke(p, a, touched);
            case "turf_claim" -> turfClaim(p, a, d);
            case "turf_war" -> turfWar(p, a, touched);
            default -> {}
        }
        for (UUID u : touched) {
            ServerPlayer sp = s.getPlayerList().getPlayer(u);
            if (sp != null) sendState(sp);
        }
    }

    private static void msg(ServerPlayer p, String text) {
        p.displayClientMessage(Component.literal(text), true);
    }

    private static int parse(String v, int def) {
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return def;
        }
    }

    // ------------------------------------------------------------------ groupes
    private static void groupCreate(ServerPlayer p, String raw, SocialData d) {
        String n = raw == null ? "" : raw.trim();
        if (!n.matches("^[\\p{L}0-9 '\\-]{2,24}$")) {
            msg(p, "Nom invalide (2 à 24 lettres/chiffres).");
            return;
        }
        if (groupOf(d, p.getUUID()) != null) {
            msg(p, "Tu as déjà un groupe.");
            return;
        }
        if (d.store.groups.containsKey(n.toLowerCase(Locale.ROOT))) {
            msg(p, "Un groupe porte déjà ce nom.");
            return;
        }
        SocialData.Group g = new SocialData.Group();
        g.name = n;
        g.owner = p.getUUID().toString();
        g.members.add(g.owner);
        d.store.groups.put(n.toLowerCase(Locale.ROOT), g);
        d.setDirty();
    }

    private static void groupLeave(ServerPlayer p, SocialData d, Set<UUID> touched) {
        SocialData.Group g = groupOf(d, p.getUUID());
        if (g == null) return;
        if (g.owner.equals(p.getUUID().toString())) {
            groupDisband(p, d, touched);
            return;
        }
        g.members.remove(p.getUUID().toString());
        g.members.forEach(m -> touched.add(UUID.fromString(m)));
        d.setDirty();
    }

    private static void groupDisband(ServerPlayer p, SocialData d, Set<UUID> touched) {
        SocialData.Group g = groupOf(d, p.getUUID());
        if (g == null || !g.owner.equals(p.getUUID().toString())) return;
        g.members.forEach(m -> touched.add(UUID.fromString(m)));
        d.store.groups.remove(g.name.toLowerCase(Locale.ROOT));
        d.setDirty();
    }

    private static void groupKick(ServerPlayer p, String target, SocialData d, Set<UUID> touched) {
        SocialData.Group g = groupOf(d, p.getUUID());
        if (g == null || !g.owner.equals(p.getUUID().toString()) || target == null || target.equals(g.owner)) return;
        if (g.members.remove(target)) {
            touched.add(UUID.fromString(target));
            d.setDirty();
        }
    }

    // ------------------------------------------------------------------ offres
    private static ServerPlayer nearby(ServerPlayer p, String id, int range) {
        try {
            ServerPlayer t = p.server.getPlayerList().getPlayer(UUID.fromString(id));
            if (t == null || t == p) return null;
            if (t.level() != p.level() || t.distanceTo(p) > range) return null;
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    private static void invite(ServerPlayer p, String target, String kind, String verb, String stake, Set<UUID> touched) {
        MinecraftServer s = p.server;
        SocialData d = SocialData.get(s);
        ServerPlayer t = nearby(p, target, "group".equals(kind) ? NEAR_RANGE : DUEL_RANGE);
        if (t == null) {
            msg(p, "Trop loin, ou hors ligne.");
            return;
        }
        if (profile(s, t.getUUID()) == null) {
            msg(p, "Cette personne n'a pas encore de personnage.");
            return;
        }
        if ("group".equals(kind)) {
            SocialData.Group g = groupOf(d, p.getUUID());
            if (g == null || !g.owner.equals(p.getUUID().toString())) {
                msg(p, "Seul le propriétaire du groupe peut inviter.");
                return;
            }
            if (groupOf(d, t.getUUID()) != null) {
                msg(p, "Cette personne a déjà un groupe.");
                return;
            }
        } else {
            if (duelOf(p.getUUID()) != null || duelOf(t.getUUID()) != null) {
                msg(p, "Un duel est déjà en cours.");
                return;
            }
        }
        String text = name(s, p.getUUID()) + " " + verb + ".";
        pushOffer(new Offer("o" + IDS.incrementAndGet(), kind, p.getUUID(), t.getUUID(), text, stake,
                s.overworld().getGameTime() + OFFER_TTL));
        touched.add(t.getUUID());
        note(s, t.getUUID(), text + " (hub > Groupe/Duels)");
        msg(p, "Offre envoyée à " + name(s, t.getUUID()) + ".");
    }

    private static void respond(ServerPlayer p, String id, boolean accept, Set<UUID> touched) {
        MinecraftServer s = p.server;
        Offer o = OFFERS.stream().filter(x -> x.id.equals(id) && x.to.equals(p.getUUID())).findFirst().orElse(null);
        if (o == null) return;
        OFFERS.remove(o);
        touched.add(o.from);
        ServerPlayer from = s.getPlayerList().getPlayer(o.from);
        if (!accept) {
            note(s, o.from, name(s, p.getUUID()) + " a refusé.");
            return;
        }
        if (from == null) {
            msg(p, "Cette personne n'est plus là.");
            return;
        }
        SocialData d = SocialData.get(s);
        switch (o.kind) {
            case "group" -> {
                SocialData.Group g = groupOf(d, o.from);
                if (g == null || groupOf(d, p.getUUID()) != null) {
                    msg(p, "Plus possible de rejoindre ce groupe.");
                    return;
                }
                g.members.add(p.getUUID().toString());
                d.setDirty();
                g.members.forEach(m -> touched.add(UUID.fromString(m)));
                note(s, o.from, name(s, p.getUUID()) + " a rejoint " + g.name + ".");
            }
            default -> {
                if (duelOf(p.getUUID()) != null || duelOf(o.from) != null) {
                    msg(p, "Un duel est déjà en cours.");
                    return;
                }
                if (from.level() != p.level() || from.distanceTo(p) > DUEL_RANGE) {
                    msg(p, "Trop loin pour commencer.");
                    return;
                }
                Duel du = new Duel();
                du.a = o.from;
                du.b = p.getUUID();
                du.kind = o.kind;
                du.stake = o.stake;
                DUELS.add(du);
                String intro = switch (o.kind) {
                    case "friendly" -> "Duel amical : il s'arrête à 2 cœurs, rien n'est en jeu.";
                    case "formal" -> "Combat pour la couronne : il s'arrête à 2 cœurs.";
                    default -> "Guerre de territoire : il s'arrête à 2 cœurs.";
                };
                for (UUID u : List.of(du.a, du.b)) note(s, u, intro);
                touched.add(du.a);
                touched.add(du.b);
            }
        }
    }

    // ------------------------------------------------------------------ duels
    /** Fin de duel : le perdant reste à 2 cœurs minimum, le vainqueur gagne l'enjeu éventuel. */
    static void end(MinecraftServer s, Duel du, UUID winner, String why) {
        DUELS.remove(du);
        UUID loser = du.a.equals(winner) ? du.b : du.a;
        String w = name(s, winner), l = name(s, loser);
        note(s, winner, "Victoire contre " + l + " (" + why + ").");
        note(s, loser, "Défaite contre " + w + " (" + why + ").");
        if ("formal".equals(du.kind) && du.stake != null) {
            // du.stake = id de l'école dont la couronne est en jeu ; le challenger est du.a
            if (winner.equals(du.a)) crown(s, du.stake, winner);
            else note(s, winner, "Tu gardes la couronne.");
        } else if ("turf".equals(du.kind) && du.stake != null) {
            SocialData d = SocialData.get(s);
            for (SocialData.Turf t : d.store.turfs) {
                if (t.id.equals(du.stake)) {
                    String ws = schoolOf(s, winner);
                    if (winner.equals(du.a) && ws != null) {
                        t.school = ws;
                        d.setDirty();
                        note(s, winner, "Le territoire « " + t.name + " » est à toi.");
                        note(s, loser, "Le territoire « " + t.name + " » est perdu.");
                    }
                }
            }
        }
        for (UUID u : List.of(winner, loser)) {
            ServerPlayer sp = s.getPlayerList().getPlayer(u);
            if (sp != null) sendState(sp);
        }
    }

    /** Le challenger prend le rang de Roi ; l'ancien Roi devient Reine (si c'est un joueur). */
    private static void crown(MinecraftServer s, String school, UUID challenger) {
        SocialData d = SocialData.get(s);
        Holder oldKing = holder(s, school, "king");
        Map<String, String> over = d.store.ranks.computeIfAbsent(school, k -> new java.util.HashMap<>());
        over.values().removeIf(v -> v.equals(challenger.toString()));
        over.put("king", challenger.toString());
        if (oldKing.uuid != null && !oldKing.uuid.equals(challenger)) over.put("queen", oldKing.uuid.toString());
        d.setDirty();
        Models.School sc = UnodexData.get().schools.get(school);
        note(s, challenger, "Tu es le nouveau Roi de " + (sc != null ? sc.name : school) + ".");
        if (oldKing.uuid != null) note(s, oldKing.uuid, "Tu as perdu la couronne.");
    }

    private static void formalChallenge(ServerPlayer p, Set<UUID> touched) {
        MinecraftServer s = p.server;
        String school = schoolOf(s, p.getUUID());
        if (school == null) {
            msg(p, "Il faut être inscrit dans une école.");
            return;
        }
        Holder king = holder(s, school, "king");
        if (king.uuid == null) {
            msg(p, "Cette école n'a pas de Roi qui puisse répondre.");
            return;
        }
        if (king.uuid.equals(p.getUUID())) {
            msg(p, "Tu es déjà le Roi.");
            return;
        }
        invite(p, king.uuid.toString(), "formal", "te défie pour la couronne", school, touched);
    }

    // ------------------------------------------------------------------ école et rangs
    private static void promote(ServerPlayer p, String target, String rank, Set<UUID> touched) {
        MinecraftServer s = p.server;
        String school = schoolOf(s, p.getUUID());
        if (school == null || !"king".equals(rankOf(s, p.getUUID(), school)) || target == null) return;
        if (!"queen".equals(rank) && !"jack".equals(rank)) return;
        UUID t;
        try {
            t = UUID.fromString(target);
        } catch (Exception e) {
            return;
        }
        if (!school.equals(schoolOf(s, t)) || "king".equals(rankOf(s, t, school))) {
            msg(p, "Cette personne doit être élève de ton école.");
            return;
        }
        SocialData d = SocialData.get(s);
        Map<String, String> over = d.store.ranks.computeIfAbsent(school, k -> new java.util.HashMap<>());
        over.values().removeIf(v -> v.equals(t.toString()));
        over.put(rank, t.toString());
        d.setDirty();
        touched.add(t);
        note(s, t, "Tu deviens " + rankFr(rank) + ".");
    }

    private static void revoke(ServerPlayer p, String rank, Set<UUID> touched) {
        MinecraftServer s = p.server;
        String school = schoolOf(s, p.getUUID());
        if (school == null || !"king".equals(rankOf(s, p.getUUID(), school))) return;
        if (!"queen".equals(rank) && !"jack".equals(rank)) return;
        SocialData d = SocialData.get(s);
        Map<String, String> over = d.store.ranks.get(school);
        if (over == null || over.get(rank) == null) {
            msg(p, "Ce siège n'a pas été attribué par un Roi.");
            return;
        }
        UUID old = UUID.fromString(over.remove(rank));
        d.setDirty();
        touched.add(old);
        note(s, old, "Ton siège de " + rankFr(rank) + " t'a été retiré.");
    }

    // ------------------------------------------------------------------ territoires
    private static void turfClaim(ServerPlayer p, String raw, SocialData d) {
        MinecraftServer s = p.server;
        String school = schoolOf(s, p.getUUID());
        if (school == null || !"king".equals(rankOf(s, p.getUUID(), school))) {
            msg(p, "Seul un Roi peut revendiquer un territoire.");
            return;
        }
        String n = raw == null ? "" : raw.trim();
        if (!n.matches("^[\\p{L}0-9 '\\-]{2,24}$")) {
            msg(p, "Nom de territoire invalide.");
            return;
        }
        for (SocialData.Turf t : d.store.turfs) {
            if (t.name.equalsIgnoreCase(n)) {
                msg(p, "Ce nom de territoire est pris.");
                return;
            }
            if (t.dim.equals(dimOf(p)) && Math.hypot(t.x - p.getX(), t.z - p.getZ()) < t.r * 2) {
                msg(p, "Trop près d'un autre territoire.");
                return;
            }
        }
        SocialData.Turf t = new SocialData.Turf();
        t.id = "t" + IDS.incrementAndGet() + "_" + System.nanoTime();
        t.name = n;
        t.school = school;
        t.dim = dimOf(p);
        t.x = p.getX();
        t.z = p.getZ();
        d.store.turfs.add(t);
        d.setDirty();
    }

    private static String dimOf(ServerPlayer p) {
        return p.level().dimension().location().toString();
    }

    static SocialData.Turf standingOn(ServerPlayer p, SocialData d) {
        for (SocialData.Turf t : d.store.turfs) {
            if (t.dim.equals(dimOf(p)) && Math.hypot(t.x - p.getX(), t.z - p.getZ()) <= t.r) return t;
        }
        return null;
    }

    private static void turfWar(ServerPlayer p, String turfId, Set<UUID> touched) {
        MinecraftServer s = p.server;
        SocialData d = SocialData.get(s);
        String school = schoolOf(s, p.getUUID());
        if (school == null || !"king".equals(rankOf(s, p.getUUID(), school))) {
            msg(p, "Seul un Roi peut déclarer une guerre.");
            return;
        }
        SocialData.Turf t = standingOn(p, d);
        if (t == null || !t.id.equals(turfId)) {
            msg(p, "Tiens-toi sur le territoire que tu veux disputer.");
            return;
        }
        if (school.equals(t.school)) {
            msg(p, "Ton école le tient déjà.");
            return;
        }
        for (Duel du : DUELS) if ("turf".equals(du.kind) && t.id.equals(du.stake)) {
            msg(p, "Une guerre est déjà en cours ici.");
            return;
        }
        Holder defender = holder(s, t.school, "king");
        if (defender.uuid == null) {
            msg(p, "Cette école n'a pas de Roi qui puisse répondre.");
            return;
        }
        invite(p, defender.uuid.toString(), "turf", "déclare la guerre pour « " + t.name + " »", t.id, touched);
    }

    // ------------------------------------------------------------------ état envoyé au client
    public static void sendState(ServerPlayer p) {
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new HubStatePacket(build(p).toString()));
    }

    static JsonObject build(ServerPlayer p) {
        MinecraftServer s = p.server;
        UnodexData data = UnodexData.get();
        ClaimData.Profile pr = profile(s, p.getUUID());
        Models.Character ch = character(pr);
        SocialData d = SocialData.get(s);
        SocialData.Soc soc = d.store.soc(p.getUUID());
        JsonObject o = new JsonObject();
        if (pr == null) {
            o.addProperty("profile", false);
            return o;
        }
        o.addProperty("profile", true);
        PlayerStatePacket ps = PlayerStateService.build(p);
        String school = schoolOf(s, p.getUUID());
        String myRank = rankOf(s, p.getUUID(), school);

        JsonObject me = new JsonObject();
        me.addProperty("name", ps.name);
        me.addProperty("tier", ps.tier);
        me.addProperty("level", ps.level);
        me.addProperty("school", school);
        Models.School msc = school == null ? null : data.schools.get(school);
        me.addProperty("school_name", msc != null ? msc.name : "");
        me.addProperty("rank", myRank);
        o.add("me", me);

        JsonObject power = new JsonObject();
        power.addProperty("has_ability", ps.hasAbility);
        power.addProperty("ability", ps.abilityName);
        power.addProperty("type", ps.abilityType);
        int rep = Reputation.rep(p);
        power.addProperty("rep", rep);
        power.addProperty("standing", Reputation.standingOf(rep));
        power.addProperty("nature", ps.abilityNature);
        power.addProperty("desc", ps.abilityDesc);
        power.addProperty("aura", ps.aura);
        power.addProperty("max_aura", ps.maxAura);
        power.addProperty("regen", ps.regen);
        power.addProperty("mastery", ps.mastery);
        JsonArray stats = new JsonArray();
        for (int v : ps.stats) stats.add(v);
        power.add("stats", stats);
        // The tree uses the actual move prerequisites, including moves outside the current stance.
        com.myriam.unrp.data.AbilityPower definition = data.powerFor(pr.abilityOf(ch));
        JsonArray tree = new JsonArray();
        if (definition != null && ps.hasAbility) {
            power.addProperty("icon", definition.icon);
            boolean mastered = com.myriam.unrp.ability.Unlocks.mastered(s, p);
            for (int i = 0; i < definition.moves.size(); i++) {
                com.myriam.unrp.data.AbilityPower.Move move = definition.moves.get(i);
                JsonObject node = new JsonObject();
                node.addProperty("name", move.name);
                node.addProperty("desc", move.desc);
                node.addProperty("icon", move.icon);
                node.addProperty("requires", move.requires);
                node.addProperty("rank", move.unlock);
                node.addProperty("mastery", move.mastery);
                node.addProperty("mastered", mastered);
                node.addProperty("locked", com.myriam.unrp.ability.Unlocks.locked(ps.unlockMask, definition, i, mastered));
                node.addProperty("cost", move.cost);
                node.addProperty("cooldown", move.cooldown);
                node.addProperty("input", move.input);
                node.addProperty("charge", move.charge);
                node.addProperty("slot", move.barSlot >= 0 ? move.barSlot : i + 1);
                tree.add(node);
            }
        }
        power.add("tree", tree);
        o.add("power", power);

        JsonObject tr = new JsonObject();
        tr.addProperty("points", statPoints(p));
        tr.addProperty("total", com.myriam.unrp.ability.Unlocks.total(p.server, p));
        tr.addProperty("max_steps", com.myriam.unrp.player.Training.MAX_STEPS);
        int[] potBase = PlayerStateService.potentialBase(pr, character(pr));
        JsonArray steps = new JsonArray(), pots = new JsonArray(), costs = new JsonArray();
        for (int i = 0; i < 5; i++) {
            steps.add(soc.spent[i]);
            int pot = potBase[i] + soc.bonus[i];
            pots.add(pot);
            boolean can = soc.spent[i] < com.myriam.unrp.player.Training.MAX_STEPS && com.myriam.unrp.player.Training.gain(pot) > 0;
            costs.add(can ? com.myriam.unrp.player.Training.cost(soc.spent[i]) : -1);
        }
        tr.add("steps", steps);
        tr.add("pot", pots);
        tr.add("cost", costs);
        o.add("train", tr);

        SocialData.Group g = groupOf(d, p.getUUID());
        if (g != null) {
            JsonObject go = new JsonObject();
            go.addProperty("name", g.name);
            go.addProperty("owner", g.owner.equals(p.getUUID().toString()));
            JsonArray ms = new JsonArray();
            for (String m : g.members) {
                UUID u = UUID.fromString(m);
                JsonObject mo = new JsonObject();
                mo.addProperty("id", m);
                mo.addProperty("name", name(s, u));
                mo.addProperty("online", s.getPlayerList().getPlayer(u) != null);
                mo.addProperty("leader", m.equals(g.owner));
                ms.add(mo);
            }
            go.add("members", ms);
            o.add("group", go);
        }

        JsonArray near = new JsonArray();
        List<ServerPlayer> others = new ArrayList<>(s.getPlayerList().getPlayers());
        others.removeIf(x -> x == p || x.level() != p.level() || x.distanceTo(p) > NEAR_RANGE || profile(s, x.getUUID()) == null);
        others.sort(Comparator.comparingDouble(x -> x.distanceTo(p)));
        for (ServerPlayer x : others.stream().limit(12).toList()) {
            JsonObject no = new JsonObject();
            no.addProperty("id", x.getUUID().toString());
            no.addProperty("name", name(s, x.getUUID()));
            no.addProperty("dist", Math.round(x.distanceTo(p)));
            no.addProperty("has_group", groupOf(d, x.getUUID()) != null);
            near.add(no);
        }
        o.add("near", near);

        // école : rangs, élèves, classement
        if (msc != null) {
            JsonObject so = new JsonObject();
            so.addProperty("id", msc.id);
            so.addProperty("name", msc.name);
            JsonArray rk = new JsonArray();
            for (int i = 0; i < RANKS.length; i++) {
                Holder h = holder(s, msc.id, RANKS[i]);
                JsonObject ro = new JsonObject();
                ro.addProperty("rank", RANKS[i]);
                ro.addProperty("label", RANK_FR[i]);
                ro.addProperty("holder", h.name == null ? "Trône vide" : h.name);
                ro.addProperty("id", h.uuid == null ? "" : h.uuid.toString());
                ro.addProperty("online", h.uuid != null && s.getPlayerList().getPlayer(h.uuid) != null);
                Map<String, String> over = d.store.ranks.get(msc.id);
                ro.addProperty("granted", over != null && over.containsKey(RANKS[i]));
                rk.add(ro);
            }
            so.add("ranks", rk);
            JsonArray st = new JsonArray();
            for (Map.Entry<UUID, ClaimData.Profile> e : ClaimData.get(s).allProfiles().entrySet()) {
                if (!msc.id.equals(schoolOf(s, e.getKey()))) continue;
                JsonObject sto = new JsonObject();
                sto.addProperty("id", e.getKey().toString());
                sto.addProperty("name", name(s, e.getKey()));
                sto.addProperty("online", s.getPlayerList().getPlayer(e.getKey()) != null);
                String r = rankOf(s, e.getKey(), msc.id);
                sto.addProperty("rank", r == null ? "" : rankFr(r));
                st.add(sto);
            }
            so.add("students", st);
            o.add("school", so);
        }
        JsonArray all = new JsonArray();
        for (Models.School sc : data.schools.values()) {
            if (!"school".equals(sc.kind)) continue;
            JsonObject ao = new JsonObject();
            ao.addProperty("id", sc.id);
            ao.addProperty("name", sc.name);
            for (String r : RANKS) {
                Holder h = holder(s, sc.id, r);
                ao.addProperty(r, h.name == null ? "-" : h.name);
            }
            ao.addProperty("members", sc.memberCount);
            all.add(ao);
        }
        o.add("schools", all);

        JsonArray offers = new JsonArray();
        for (Offer of : OFFERS) {
            if (!of.to.equals(p.getUUID())) continue;
            JsonObject oo = new JsonObject();
            oo.addProperty("id", of.id);
            oo.addProperty("kind", of.kind);
            oo.addProperty("text", of.text);
            offers.add(oo);
        }
        o.add("offers", offers);

        Duel du = duelOf(p.getUUID());
        if (du != null) {
            JsonObject duo = new JsonObject();
            duo.addProperty("opp", name(s, du.a.equals(p.getUUID()) ? du.b : du.a));
            duo.addProperty("kind", du.kind);
            o.add("duel", duo);
        }

        JsonArray feed = new JsonArray();
        int unread = 0;
        for (SocialData.Note n : soc.feed) {
            JsonObject no = new JsonObject();
            no.addProperty("text", n.text);
            no.addProperty("read", n.read);
            if (!n.read) unread++;
            feed.add(no);
        }
        o.add("feed", feed);
        o.addProperty("unread", unread);

        JsonArray turfs = new JsonArray();
        for (SocialData.Turf t : d.store.turfs) {
            JsonObject to = new JsonObject();
            to.addProperty("id", t.id);
            to.addProperty("name", t.name);
            Models.School ts = data.schools.get(t.school);
            to.addProperty("school", ts != null ? ts.name : t.school);
            to.addProperty("mine", t.school.equals(school));
            to.addProperty("x", Math.round(t.x));
            to.addProperty("z", Math.round(t.z));
            turfs.add(to);
        }
        o.add("turfs", turfs);
        SocialData.Turf here = standingOn(p, d);
        if (here != null) {
            JsonObject ho = new JsonObject();
            ho.addProperty("id", here.id);
            ho.addProperty("name", here.name);
            Models.School ts = data.schools.get(here.school);
            ho.addProperty("school", ts != null ? ts.name : here.school);
            ho.addProperty("mine", here.school.equals(school));
            o.add("here", ho);
        }
        return o;
    }
}
