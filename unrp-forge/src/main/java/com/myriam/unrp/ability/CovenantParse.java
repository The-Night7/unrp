package com.myriam.unrp.ability;

import java.text.Normalizer;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Lecture des lois de Covenant, écrites en français ou en anglais dans le chat. Aucune dépendance au jeu : testable seul.
 * Une loi : « Personne ne court. Pénalité : sa Vitesse ». Le texte avant la pénalité dit l'infraction, le texte après dit la peine.
 */
public final class CovenantParse {
    private CovenantParse() {}

    public enum Viol { SPRINT, JUMP, ATTACK, ABILITY, SPEAK, FLY, BREAK, PLACE, SNEAK, LEAVE }

    public enum Kind { STAT, MOVE, BRANCH, SENSE, MEMORY, SOUL, LIFE }

    /** key : STAT = "0".."4" (Puissance, Défense, Récupération, Ruse, Vitesse) ; MOVE = sprint|jump|walk ; SENSE = sight|touch ; sinon "". */
    public record Pen(Kind kind, String key) {
        public String label() {
            return switch (kind) {
                case STAT -> "stat " + new String[] {"Puissance", "Défense", "Récupération", "Ruse", "Vitesse"}[Integer.parseInt(key)];
                case MOVE -> "mouvement (" + (key.equals("sprint") ? "course" : key.equals("jump") ? "saut" : "marche") + ")";
                case BRANCH -> "branche de capacité";
                case SENSE -> "sens (" + (key.equals("sight") ? "vue" : "toucher") + ")";
                case MEMORY -> "mémoire";
                case SOUL -> "âme";
                case LIFE -> "vie";
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
        for (String k : keys) if (s.contains(k)) return true;
        return false;
    }

    /** Coupe « infraction | peine » au premier marqueur de pénalité ; right vaut "" s'il n'y en a pas. */
    public static String[] split(String text) {
        String n = norm(text);
        int cut = -1;
        for (String m : new String[] {"penalty", "penalite", "peine", "punishment", "|", "->", "=>"}) {
            int i = n.indexOf(m);
            if (i >= 0 && (cut < 0 || i < cut)) cut = i;
        }
        if (cut < 0) return new String[] {n, ""};
        return new String[] {n.substring(0, cut), n.substring(cut)};
    }

    public static Viol viol(String left) {
        String s = norm(left);
        if (has(s, "sprint", "run", "cour", "course")) return Viol.SPRINT;
        if (has(s, "jump", "saut", "bond")) return Viol.JUMP;
        if (has(s, "ability", "power", "pouvoir", "capacit", "magic", "magie", "cast")) return Viol.ABILITY;
        if (has(s, "attack", "fight", "hit", "strike", "violen", "combat", "frapp", "attaqu", "bagarr", "harm", "blesser")) return Viol.ATTACK;
        if (has(s, "speak", "talk", "lie", "lying", "parl", "ment", "bavard", "silence", "chat", "voice")) return Viol.SPEAK;
        if (has(s, "fly", "flight", "vol", "plan")) return Viol.FLY;
        if (has(s, "break", "mine", "dig", "casse", "miner", "creus", "destroy", "detruire")) return Viol.BREAK;
        if (has(s, "place", "build", "pose", "constru", "batir")) return Viol.PLACE;
        if (has(s, "sneak", "crouch", "accroup", "hide", "cacher")) return Viol.SNEAK;
        if (has(s, "leave", "exit", "flee", "fuir", "quitt", "sortir", "escape", "depart")) return Viol.LEAVE;
        return null;
    }

    public static Pen pen(String right) {
        String s = norm(right);
        if (s.isEmpty()) return null;
        if (has(s, "soul", "ame ", "ame.", "ame,", " ame", "ame\n") || s.endsWith("ame")) return new Pen(Kind.SOUL, "");
        if (has(s, "memor", "souvenir", "memoire", "recollection")) return new Pen(Kind.MEMORY, "");
        if (has(s, "branch", "capacit", "ability", "pouvoir", "power branch")) return new Pen(Kind.BRANCH, "");
        if (has(s, "sight", "vision", "vue", "eyes", "yeux", "sens")) return new Pen(Kind.SENSE, "sight");
        if (has(s, "touch", "toucher", "feeling")) return new Pen(Kind.SENSE, "touch");
        if (has(s, "jump", "saut")) return new Pen(Kind.MOVE, "jump");
        if (has(s, "sprint", "running", "course", "courir")) return new Pen(Kind.MOVE, "sprint");
        if (has(s, "movement", "mouvement", "walk", "marche", "legs", "jambes")) return new Pen(Kind.MOVE, "walk");
        if (has(s, "recover", "recup", "regen")) return new Pen(Kind.STAT, "2");
        if (has(s, "defen", "defense")) return new Pen(Kind.STAT, "1");
        if (has(s, "trick", "ruse", "cunning")) return new Pen(Kind.STAT, "3");
        if (has(s, "speed", "vitesse", "swift")) return new Pen(Kind.STAT, "4");
        if (has(s, "power", "puissance", "strength", "force")) return new Pen(Kind.STAT, "0");
        if (has(s, "life", "vie")) return new Pen(Kind.LIFE, "");
        return null;
    }

    /** Une loi complète, ou null si l'infraction ou la peine n'est pas reconnue. */
    public static Law law(String text) {
        String[] lr = split(text);
        Viol v = viol(lr[0]);
        Pen p = pen(lr[1]);
        return v == null || p == null ? null : new Law(v, p);
    }

    public static Set<Term> terms(String text) {
        String s = norm(text);
        Set<Term> out = EnumSet.noneOf(Term.class);
        if (has(s, "everything", "all item", "tout", "inventaire", "inventory")) out.add(Term.ALL);
        if (has(s, "weapon", "arme", "sword", "epee")) out.add(Term.WEAPONS);
        if (has(s, "armor", "armour", "armure")) out.add(Term.ARMOR);
        if (has(s, "elytra", "elytre", "wings", "ailes")) out.add(Term.ELYTRA);
        if (has(s, "pearl", "perle")) out.add(Term.PEARLS);
        return out;
    }

    /** « je donne X contre Y » / « X for Y » : [X, Y] en peines, null si illisible. */
    public static Pen[] pact(String text) {
        String s = norm(text);
        int i = -1, len = 0;
        for (String sep : new String[] {" contre ", " for ", " against ", " en echange de ", " pour "}) {
            int k = s.indexOf(sep);
            if (k >= 0) { i = k; len = sep.length(); break; }
        }
        if (i < 0) return null;
        Pen a = pen(s.substring(0, i)), b = pen(s.substring(i + len));
        return a == null || b == null ? null : new Pen[] {a, b};
    }
}
