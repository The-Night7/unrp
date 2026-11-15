package com.myriam.unrp.ability;

/** Covenant's tier rules. All durations use server ticks (20 ticks per second). */
public final class CovenantRules {
    public static final long HOUR = 72_000, DAY = 24 * HOUR, WEEK = 7 * DAY;
    public static final double MID = 2.0, ELITE = 3.5, HIGH = 5.0, GOD = 6.0;
    private CovenantRules() {}
    public static int tier(double level) { return level < MID ? 0 : level < ELITE ? 1 : level < HIGH ? 2 : level < GOD ? 3 : 4; }
    public static int radius(double level) { return new int[] {10, 30, 75, 150, 500}[tier(level)]; }
    public static int lawCap(double level) { return tier(level) == 0 ? 2 : tier(level) == 1 ? 5 : Integer.MAX_VALUE; }
    public static int branchCap(double level) { return level >= GOD ? Integer.MAX_VALUE : 5; }
    public static int wieldCap(double level) { return level >= GOD ? 10 : 5; }
    public static boolean soulBurden(double level, long souls) { return level >= HIGH && level < GOD && souls > 10; }
    public static long indemnityExpiry(double level, long now) { return level >= HIGH ? Long.MAX_VALUE : now + 120; }
    public static boolean allowed(double level, CovenantParse.Kind kind) {
        return switch (kind) {
            case STAT, MOVE -> true;
            case BRANCH, SENSE -> level >= ELITE;
            case MEMORY, SOUL -> level >= HIGH;
            case ABILITY -> level >= GOD;
            case NONE -> level >= GOD;
            case LIFE -> false;
        };
    }
    public static boolean branchAllowed(boolean previouslyOwned, boolean entireAbility, java.util.Set<Integer> leasedMoves, int requestedMove) {
        return previouslyOwned || entireAbility || leasedMoves.contains(requestedMove);
    }
    public static boolean retroactiveEligible(long now, long actionAt, boolean physicallyInsideAtTheTime) {
        return physicallyInsideAtTheTime && actionAt <= now && actionAt >= now - HOUR;
    }
    public static double minimum(String special) {
        if (special == null) return 0;
        return switch (special) {
            case "cv_fine", "cv_pact", "cv_endow" -> MID;
            case "cv_foreclose", "cv_exile", "cv_proxy", "cv_appropriate" -> ELITE;
            case "cv_constitution", "cv_retroactive", "cv_forfeit", "cv_class_action", "cv_sovereign_pact", "cv_due", "cv_jurisdiction", "cv_message" -> GOD;
            case "cv_summon" -> HIGH;
            default -> 0;
        };
    }
}
