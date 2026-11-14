package com.myriam.unrp.ability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Les « superpouvoirs » de Palladium tels que le modèle les manipule (commande superpower add/remove) :
 * pouvoirs accordés en plus du sien (pactes, âmes, soul_bound), pouvoir de base scellé (retiré par un pacte),
 * copies « _am » (réserve d'Aura Manipulation). Enregistré sur le joueur ; les tags « unrp.has.<pouvoir> »
 * du modèle en découlent.
 */
public final class Superpowers {
    private Superpowers() {}

    private static Set<String> read(ServerPlayer p, String key) {
        Set<String> out = new LinkedHashSet<>();
        CompoundTag d = com.myriam.unrp.item.UnItems.persisted(p);
        for (Tag t : d.getList(key, Tag.TAG_STRING)) out.add(t.getAsString());
        return out;
    }

    private static void write(ServerPlayer p, String key, Set<String> s) {
        ListTag l = new ListTag();
        for (String x : s) l.add(StringTag.valueOf(x));
        com.myriam.unrp.item.UnItems.persisted(p).put(key, l);
    }

    public static Set<String> granted(ServerPlayer p) {
        return read(p, "unrp_granted");
    }

    public static Set<String> revoked(ServerPlayer p) {
        return read(p, "unrp_revoked");
    }

    /** superpower add unrp:<id> */
    public static void add(ServerPlayer p, String id) {
        if (id.endsWith("_am")) {
            AbilityEngine.addCopy(p, id.substring(0, id.length() - 3));
            return;
        }
        Set<String> rev = revoked(p);
        if (rev.remove(id)) {
            write(p, "unrp_revoked", rev);
            return;
        }
        if (id.equals(AbilityEngine.basePowerId(p))) return;
        Set<String> g = granted(p);
        if (g.add(id)) write(p, "unrp_granted", g);
    }

    /** superpower remove unrp:<id> */
    public static void remove(ServerPlayer p, String id) {
        if (id.endsWith("_am")) {
            AbilityEngine.removeCopy(p, id.substring(0, id.length() - 3));
            return;
        }
        if (id.equals(AbilityEngine.basePowerId(p))) {
            Set<String> rev = revoked(p);
            if (rev.add(id)) write(p, "unrp_revoked", rev);
            AbilityEngine.removeCopy(p, id);
            AbilityEngine.forceOff(p);
            return;
        }
        Set<String> g = granted(p);
        if (g.remove(id)) write(p, "unrp_granted", g);
        AbilityEngine.removeCopy(p, id);
    }

    /** Pouvoirs que le joueur détient (tags « unrp.has.* » du modèle). */
    public static Set<String> held(ServerPlayer p) {
        Set<String> out = new LinkedHashSet<>();
        String base = AbilityEngine.basePowerId(p);
        if (base != null && !revoked(p).contains(base)) out.add(base);
        out.addAll(granted(p));
        out.addAll(AbilityEngine.copies(p));
        return out;
    }

    /** Pose / retire les tags « unrp.has.<pouvoir> » (appelé chaque tick par PassSync). */
    public static void syncTags(ServerPlayer p) {
        Set<String> held = held(p);
        for (String t : Set.copyOf(p.getTags()))
            if (t.startsWith("unrp.has.") && !held.contains(t.substring("unrp.has.".length()))) p.removeTag(t);
        for (String h : held) p.addTag("unrp.has." + h);
    }
}
