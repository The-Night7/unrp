package com.myriam.unrp.ability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.Set;

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
        if (id.equals(AbilityEngine.basePowerId(p)) && Specials17.branchEscrowed(p)) return;
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

    public static Set<String> held(ServerPlayer p) {
        if (Specials17.powerBlocked(p)) return Set.of();
        Set<String> out = new LinkedHashSet<>();
        String base = AbilityEngine.basePowerId(p);
        if (base != null && !revoked(p).contains(base)) out.add(base);
        out.addAll(granted(p));
        out.addAll(AbilityEngine.copies(p));
        return out;
    }

    public static void syncTags(ServerPlayer p) {
        Set<String> held = held(p);
        for (String t : Set.copyOf(p.getTags()))
            if (t.startsWith("unrp.has.") && !held.contains(t.substring("unrp.has.".length()))) p.removeTag(t);
        for (String h : held) p.addTag("unrp.has." + h);
    }
}
