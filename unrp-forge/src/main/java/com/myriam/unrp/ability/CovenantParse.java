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

    public enum Kind { STAT, MOVE, BRANCH, SENSE, MEMORY, SOUL, LIFE, ABILITY, NONE }

    /** key : STAT = "0".."4" (Puissance, Défense, Récupération, Ruse, Vitesse) ; MOVE = sprint|jump|walk ; SENSE = sight|touch ; sinon "". */
    public record Pen(Kind kind, String key) {
        public String label() {
            return switch (kind) {
                case STAT -> "stat " + new String[] {"Power", "Defense", "Recovery", "Trick", "Speed"}[Integer.parseInt(key)];
                case MOVE -> "movement (" + (key.equals("sprint") ? "sprint" : key.equals("jump") ? "jump" : "walk") + ")";
                case BRANCH -> "ability branch";
                case SENSE -> "sense (" + (key.equals("sight") ? "sight" : key.equals("hearing") ? "hearing" : "touch") + ")";
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
        if (has(s, "sprint", "run", "courir", "course") || s.matches(".*\\bne\\s+court\\b.*")) return Viol.SPRINT;
        if (has(s, "jump", "saut", "bond")) return Viol.JUMP;
        if (has(s, "ability", "power", "pouvoir", "capacit", "magic", "magie", "cast")) return Viol.ABILITY;
        if (has(s, "attack", "fight", "hit", "strike", "violen", "combat", "frapp", "attaqu", "bagarr", "harm", "blesser")) return Viol.ATTACK;
        if (has(s, "speak", "talk", "lie", "lying", "parl", "ment", "bavard", "silence", "chat", "voice")) return Viol.SPEAK;
        if (has(s, "fly", "flight", "vol", "plan")) return Viol.FLY;
        if (has(s, "break", "mine", "dig", "casse", "miner", "creus", "destroy", "detruire")) return Viol.BREAK;
        if (has(s, "place", "build", "pose", "constru", "batir")) return Viol.PLACE;
        if (has(s, "sneak", "crouch", "accroup", "hide", "cacher")) return Viol.SNEAK;
        if (has(s, "leav", "exit", "flee", "fuir", "quitt", "sortir", "escape", "depart")) return Viol.LEAVE;
        return null;
    }

    public static Pen pen(String right) {
        String s = norm(right);
        if (s.isEmpty()) return null;
        if (has(s, "whole ability", "entire ability", "whole power", "entire power", "total forfeit", "capacite entiere", "pouvoir entier")) return new Pen(Kind.ABILITY, "");
        if (has(s, "nothing", "no consideration", "rien", "aucune contrepartie")) return new Pen(Kind.NONE, "");
        if (has(s, "branch", "branche")) return new Pen(Kind.BRANCH, branchKey(s));
        if (s.matches(".*\\b(soul|ame)\\b.*")) return new Pen(Kind.SOUL, "");
        if (has(s, "memor", "souvenir", "memoire", "recollection")) return new Pen(Kind.MEMORY, "");
        if (has(s, "capacit", "ability", "pouvoir")) return new Pen(Kind.BRANCH, "");
        if (has(s, "hearing", "ouie", "sound", "audition")) return new Pen(Kind.SENSE, "hearing");
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

    private static String branchKey(String text) {
        int at = text.indexOf("branch");
        String after = text.substring(at).replaceFirst("^branch(?:e)?", "").trim();
        String value = after.isBlank() ? text.substring(0, at) : after;
        return value.replaceAll("\\b(penalty|penalite|peine|punishment|their|your|his|her|the|a|an|ability|power|of|i|give|je|donne|ma|ta|sa|son|ses|leur|leurs|de|capacite)\\b", " ")
                .replaceAll("[^a-z0-9 _-]", " ").trim().replaceAll("\\s+", " ");
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
