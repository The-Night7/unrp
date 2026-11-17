package com.myriam.unrp.player;

import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.data.Models;
import com.myriam.unrp.data.UnodexData;
import com.myriam.unrp.net.Net;
import com.myriam.unrp.net.PlayerStatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Calculates effective stats and aura, then synchronizes the resulting player state. */
public final class PlayerStateService {
    private static final Map<String, String> ROLE_LABELS = Map.of(
            "king", "King", "queen", "Queen", "jack", "Jack", "healer", "Healer", "alumni", "Alumni", "hidden", "hidden");

    private PlayerStateService() {}

    public static double effectiveLevel(ServerPlayer player) {
        ClaimData.Profile profile = ClaimData.get(player.server).profileOf(player.getUUID()).orElse(null);
        if (profile == null) return 1;
        Models.Character character = ClaimData.Profile.CHARACTER.equals(profile.kind)
                ? UnodexData.get().characters.get(profile.characterId) : null;
        double original = character != null && character.level != null ? character.level : profile.level;
        return com.myriam.unrp.item.RecoveryTreatment.effectiveLevel(original,
                com.myriam.unrp.ability.AbilityEngine.restoredPercent(player));
    }

    public static int maxAura(int[] stats) {
        return 100 + 10 * (stats[3] + stats[2]);
    }

    public static int regen(int[] stats) {
        return 1 + stats[2] / 2;
    }

    public static double levelRatio(double level) {
        return Math.sqrt(Math.min(10.0, Math.max(0, level)) / 10.0);
    }

    static int stat(Double v) {
        return v == null ? 1 : Math.max(1, (int) Math.round(v));
    }

    public static int[] potentialBase(ClaimData.Profile pr, Models.Character c) {
        if (c != null && c.stats != null) {
            return new int[] {tenths(c.stats.power), tenths(c.stats.defense), tenths(c.stats.recovery), tenths(c.stats.trick), tenths(c.stats.speed)};
        }
        int[] p = pr.pot;
        return p != null && p.length == 5 ? p.clone() : new int[] {10, 10, 10, 10, 10};
    }

    private static int tenths(Double v) {
        return v == null ? 10 : (int) Math.round(v * 10);
    }

    public static int[] stats(ClaimData.Profile pr, Models.Character c) {
        if (c != null && c.stats != null) {
            return new int[]{stat(c.stats.power), stat(c.stats.defense), stat(c.stats.recovery),
                    stat(c.stats.trick), stat(c.stats.speed)};
        }
        return fromPotential(potentialBase(pr, c), new int[5], pr.level);
    }

    public static int[] fromPotential(int[] potTenths, int[] bonusTenths, double level) {
        int[] out = new int[5];
        for (int i = 0; i < 5; i++) {
            out[i] = Math.max(1, (int) Math.round((potTenths[i] + bonusTenths[i]) / 10.0 * levelRatio(level)));
        }
        return out;
    }

    public static int[] stats(MinecraftServer server, java.util.UUID id, ClaimData.Profile pr, Models.Character c) {
        int[] bonus = com.myriam.unrp.social.SocialData.get(server).store.soc(id).bonus;
        int[] mode = com.myriam.unrp.ability.AbilityEngine.statDelta(id);
        int[] base;
        if (c != null && c.stats != null) {
            base = stats(pr, c);
            for (int i = 0; i < 5; i++) base[i] = Math.max(1, base[i] + (int) Math.round(bonus[i] / 10.0));
        } else {
            base = fromPotential(potentialBase(pr, c), bonus, pr.level);
        }
        for (int i = 0; i < 5; i++) base[i] = Math.max(0, base[i] + mode[i]);
        var player=server.getPlayerList().getPlayer(id);
        if(player!=null) {
            double restored=com.myriam.unrp.ability.AbilityEngine.restoredPercent(player);
            for(int i=0;i<base.length;i++)base[i]=com.myriam.unrp.item.RecoveryTreatment.effectiveStat(base[i],restored);
        }
        return base;
    }

    public static void send(ServerPlayer player) {
        PlayerStatePacket pkt = build(player);
        if (pkt.hasProfile) BodyStats.apply(player, pkt.stats);
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), pkt);
    }

    public static PlayerStatePacket build(ServerPlayer player) {
        ClaimData claims = ClaimData.get(player.server);
        UnodexData data = UnodexData.get();
        PlayerStatePacket s = new PlayerStatePacket();
        ClaimData.Profile pr = claims.profileOf(player.getUUID()).orElse(null);
        if (pr == null) return s;

        s.hasProfile = true;
        s.kind = pr.kind;
        Models.Character c = ClaimData.Profile.CHARACTER.equals(pr.kind) ? data.characters.get(pr.characterId) : null;
        s.name = c != null ? c.name : (pr.ocName != null ? pr.ocName : String.valueOf(pr.characterId));
        s.tier = c != null && c.tier != null ? c.tier : (pr.tier == null ? "unknown" : pr.tier);
        Double lvl = c != null ? c.level : Double.valueOf(pr.level);
        double restored=com.myriam.unrp.ability.AbilityEngine.restoredPercent(player);
        if(lvl!=null&&restored<100) {
            lvl=effectiveLevel(player);
            s.tier=lvl>=7.5?"exo_god":lvl>=6?"god":lvl>=5?"high":lvl>=3.5?"elite":lvl>=2?"mid":"low";
        }
        s.level = lvl == null ? "?" : String.format(Locale.ROOT, "%.1f", lvl);

        claims.ensureOcAbility(pr);
        String aid = pr.abilityOf(c);
        Models.Ability ab = aid != null ? data.abilities.get(aid) : null;
        s.hasAbility = ab != null && !pr.lifelong;
        if (ab != null) {
            s.abilityName = ab.name;
            s.abilityType = ab.type == null ? "" : ab.type;
            s.abilityNature = ab.nature == null ? "" : ab.nature;
            s.abilityDesc = ab.description == null ? "" : ab.description;
        }

        s.stats = stats(player.server, player.getUUID(), pr, c);
        s.mastery = com.myriam.unrp.ability.Mastery.percent(pr);
        s.maxAura = maxAura(s.stats);
        s.regen = regen(s.stats);
        com.myriam.unrp.ability.AbilityEngine.describe(player, s, aid);
        if (pr.aura < 0) {
            pr.aura = s.maxAura;
            claims.setDirty();
        }
        s.aura = Math.min(pr.aura, s.maxAura);

        Models.School first = null;
        if (c != null) {
            s.schools = c.schools.stream().map(m -> {
                Models.School sc = data.schools.get(m.school);
                String n = sc != null ? sc.name : m.school;
                String roles = m.roles.stream().map(r -> ROLE_LABELS.getOrDefault(r, r)).collect(Collectors.joining(", "));
                return roles.isEmpty() ? n : n + " (" + roles + ")";
            }).collect(Collectors.joining(", "));
            if (!c.schools.isEmpty()) first = data.schools.get(c.schools.get(0).school);
        } else if (pr.ocSchool != null) {
            first = data.schools.get(pr.ocSchool);
            s.schools = (first != null ? first.name : pr.ocSchool) + " (student)";
        }
        if (first != null) {
            s.members = first.memberCount;
            s.leaders = first.leaders.stream().map(l -> {
                Models.Character lc = data.characters.get(l.characterId);
                return (lc != null ? lc.name : l.characterId) + " (" + ROLE_LABELS.getOrDefault(l.role, l.role) + ")";
            }).collect(Collectors.joining(", "));
        }
        return s;
    }

    public static void tick(MinecraftServer server) {
        ClaimData claims = ClaimData.get(server);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ClaimData.Profile pr = claims.profileOf(p.getUUID()).orElse(null);
            if (pr == null) {
                BodyStats.apply(p, null); // No profile: use ordinary player attributes.
                continue;
            }
            Models.Character c = ClaimData.Profile.CHARACTER.equals(pr.kind) ? UnodexData.get().characters.get(pr.characterId) : null;
            int[] st = stats(server, p.getUUID(), pr, c);
            BodyStats.apply(p, st);
            long t = server.overworld().getGameTime();
            if (p.getHealth() < p.getMaxHealth() && p.isAlive() && t % BodyStats.regenEvery(st[2]) < 20) p.heal(0.5f);
            if (pr.lifelong || pr.abilityOf(c) == null) continue;
            int max = maxAura(st);
            if (pr.aura < 0) pr.aura = max;
            if (pr.aura < max && !com.myriam.unrp.ability.AbilityEngine.blocksRegen(p.getUUID())) {
                pr.aura = Math.min(max, pr.aura + regen(st));
                claims.setDirty();
                send(p);
            }
        }
    }
}
