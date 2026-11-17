package com.myriam.unrp.ability;

import java.text.Normalizer;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Parses supported English Covenant laws without any Minecraft dependency.
 * Example: "No running. Penalty: Speed". The two clauses describe the trigger and penalty.
 */
public final class CovenantParse {
    private CovenantParse() {}

    public enum Viol { SPRINT, JUMP, ATTACK, ABILITY, SPEAK, FLY, BREAK, PLACE, SNEAK, LEAVE }

    public enum Kind { STAT, MOVE, BRANCH, SENSE, MEMORY, SOUL, LIFE, ABILITY, NONE }

    /**
     * key: STAT = "0".."4" (Power, Defense, Recovery, Trick, Speed); MOVE = sprint|jump|walk; SENSE =
     * sight|hearing|touch; otherwise empty.
     */
    public record Pen(Kind kind, String key) {
        public String label() {
            return switch (kind) {
                case STAT ->
                    "stat "
                        + new String[] {
                            "Power", "Defense", "Recovery", "Trick", "Speed"}[Integer.parseInt(key)];
                case MOVE ->
                    "movement ("
                        + (key.equals("sprint")      ? "sprint"
                                : key.equals("jump") ? "jump"
                                                     : "walk")
                        + ")";
                case BRANCH -> "ability branch";
                case SENSE ->
                    "sense ("
                        + (key.equals("sight")          ? "sight"
                                : key.equals("hearing") ? "hearing"
                                                        : "touch")
                        + ")";
                case MEMORY -> "memory";
                case SOUL -> "soul";
                case LIFE -> "life";
                case ABILITY -> "whole ability";
                case NONE -> "no consideration";
            };
        }
    }

    public record Law(Viol viol, Pen pen) {}

    public enum Term { WEAPONS, ARMOR, ELYTRA, PEARLS, ALL }

    public static String norm(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.toLowerCase(Locale.ROOT).replace('’', '\'');
    }

    private static boolean has(String s, String... keys) {
        for (String k : keys)
            if (s.contains(k))
                return true;
        return false;
    }

    /** Split the trigger and penalty at the first marker; the right clause is empty when absent. */
    public static String[] split(String text) {
        String n = norm(text);
        int cut = -1;
        for (String m : new String[] {"penalty", "punishment", "|", "->", "=>"}) {
            int i = n.indexOf(m);
            if (i >= 0 && (cut < 0 || i < cut))
                cut = i;
        }
        if (cut < 0)
            return new String[] {n, ""};
        return new String[] {n.substring(0, cut), n.substring(cut)};
    }

    public static Viol viol(String left) {
        String s = norm(left);
        if (has(s, "sprint", "run"))
            return Viol.SPRINT;
        if (has(s, "jump"))
            return Viol.JUMP;
        if (has(s, "ability", "power", "magic", "cast"))
            return Viol.ABILITY;
        if (has(s, "attack", "fight", "hit", "strike", "violen", "combat", "harm"))
            return Viol.ATTACK;
        if (has(s, "speak", "talk", "silence", "chat", "voice") || s.matches(".*\\b(lie|lies|lying)\\b.*"))
            return Viol.SPEAK;
        if (has(s, "fly", "flight"))
            return Viol.FLY;
        if (has(s, "break", "mine", "dig", "destroy"))
            return Viol.BREAK;
        if (has(s, "place", "build"))
            return Viol.PLACE;
        if (has(s, "sneak", "crouch", "hide"))
            return Viol.SNEAK;
        if (has(s, "leav", "exit", "flee", "escape", "depart"))
            return Viol.LEAVE;
        return null;
    }

    public static Pen pen(String right) {
        String s = norm(right);
        if (s.isEmpty())
            return null;
        if (has(s, "whole ability", "entire ability", "whole power", "entire power", "total forfeit"))
            return new Pen(Kind.ABILITY, "");
        if (has(s, "nothing", "no consideration"))
            return new Pen(Kind.NONE, "");
        if (has(s, "branch"))
            return new Pen(Kind.BRANCH, branchKey(s));
        if (s.matches(".*\\b(soul)\\b.*"))
            return new Pen(Kind.SOUL, "");
        if (has(s, "memor", "recollection"))
            return new Pen(Kind.MEMORY, "");
        if (has(s, "ability"))
            return new Pen(Kind.BRANCH, "");
        if (has(s, "hearing", "sound", "audition"))
            return new Pen(Kind.SENSE, "hearing");
        if (has(s, "sight", "vision", "eyes"))
            return new Pen(Kind.SENSE, "sight");
        if (has(s, "touch", "feeling"))
            return new Pen(Kind.SENSE, "touch");
        if (has(s, "jump"))
            return new Pen(Kind.MOVE, "jump");
        if (has(s, "sprint", "running"))
            return new Pen(Kind.MOVE, "sprint");
        if (has(s, "movement", "walk", "legs"))
            return new Pen(Kind.MOVE, "walk");
        if (has(s, "recover", "regen"))
            return new Pen(Kind.STAT, "2");
        if (has(s, "defen", "defense"))
            return new Pen(Kind.STAT, "1");
        if (has(s, "trick", "cunning"))
            return new Pen(Kind.STAT, "3");
        if (has(s, "speed", "swift"))
            return new Pen(Kind.STAT, "4");
        if (has(s, "power", "strength", "force"))
            return new Pen(Kind.STAT, "0");
        if (has(s, "life"))
            return new Pen(Kind.LIFE, "");
        return null;
    }

    private static String branchKey(String text) {
        int at = text.indexOf("branch");
        String after = text.substring(at).replaceFirst("^branch", "").trim();
        String value = after.isBlank() ? text.substring(0, at) : after;
        return value
            .replaceAll("\\b(penalty|punishment|their|your|his|her|the|a|an|ability|power|of|i|give)\\b", " ")
            .replaceAll("[^a-z0-9 _-]", " ")
            .trim()
            .replaceAll("\\s+", " ");
    }

    /** Return a complete law, or null if its trigger or penalty is unsupported. */
    public static Law law(String text) {
        String[] lr = split(text);
        Viol v = viol(lr[0]);
        Pen p = pen(lr[1]);
        return v == null || p == null ? null : new Law(v, p);
    }

    public static Set<Term> terms(String text) {
        String s = norm(text);
        Set<Term> out = EnumSet.noneOf(Term.class);
        if (has(s, "everything", "all item", "inventory"))
            out.add(Term.ALL);
        if (has(s, "weapon", "sword"))
            out.add(Term.WEAPONS);
        if (has(s, "armor", "armour"))
            out.add(Term.ARMOR);
        if (has(s, "elytra", "wings"))
            out.add(Term.ELYTRA);
        if (has(s, "pearl"))
            out.add(Term.PEARLS);
        return out;
    }

    /** Parse "X for Y" into two penalties, or return null when unsupported. */
    public static Pen[] pact(String text) {
        String s = norm(text);
        int i = -1, len = 0;
        for (String sep : new String[] {" for ", " against "}) {
            int k = s.indexOf(sep);
            if (k >= 0) {
                i = k;
                len = sep.length();
                break;
            }
        }
        if (i < 0)
            return null;
        Pen a = pen(s.substring(0, i)), b = pen(s.substring(i + len));
        return a == null || b == null ? null : new Pen[] {a, b};
    }
}
