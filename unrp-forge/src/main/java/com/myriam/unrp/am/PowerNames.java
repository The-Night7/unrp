package com.myriam.unrp.am;

import java.util.Map;

public final class PowerNames {
    private PowerNames() {}

    private static final Map<String, String> NAMES = Map.ofEntries(Map.entry("aura_transference", "Aura Transference"),
            Map.entry("boon_projection", "Boon of Projection"), Map.entry("boon_travel", "Boon of Travel"), Map.entry("conjure_disks", "Conjure: Disks"),
            Map.entry("conjure_vines", "Conjure: Vines"), Map.entry("dealmaker", "The Dealmaker"), Map.entry("disguise", "Stolen Shape"),
            Map.entry("explosive_missile", "Explosive Missiles"), Map.entry("homing_explosive_missile", "Homing Explosive Missiles"),
            Map.entry("chained_one", "The Chained One"), Map.entry("nadir", "Vertigo"), Map.entry("vine_barrier", "Briarhold"), Map.entry("wyrm_engine", "Dragon Engine"));

    public static String display(String id) {
        if (id == null) return "";
        String s = id;
        int colon = id.indexOf(':');
        if (colon >= 0) s = id.substring(colon + 1);
        String prefix = "";
        if (s.endsWith("_am")) s = s.substring(0, s.length() - 3);
        else if (s.endsWith("_leader")) s = s.substring(0, s.length() - 7);
        else if (s.endsWith("_enh")) { s = s.substring(0, s.length() - 4); prefix = "Enhanced "; }
        else if (s.endsWith("_syn")) { s = s.substring(0, s.length() - 4); prefix = "Synthetic "; }
        String named = NAMES.get(s);
        return prefix + (named != null ? named : pretty(s));
    }

    public static String pretty(String s) {
        StringBuilder out = new StringBuilder();
        for (String word : s.split("_")) {
            if (word.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }
}
