package com.myriam.unrp.ability;

import java.util.regex.Pattern;

public final class CovenantSpecificRules {
    public enum Kind { SIGNED_CONTRACTS, HOME_REPORT }

    public record Rule(Kind kind, String target) {
        public boolean matches(String name) {
            return target.equals("*") || target.equalsIgnoreCase(name);
        }

        public String text() {
            return switch (kind) {
                case SIGNED_CONTRACTS -> "Transfer signed contracts to the Sovereign.";
                case HOME_REPORT ->
                    "When " + target + " enters the Court, record their home in a book for the Sovereign.";
            };
        }
    }

    private static final Pattern TARGET = Pattern.compile("[A-Za-z0-9_]{1,16}|\\*");
    private static final Pattern HOME = Pattern.compile(
        "When ([A-Za-z0-9_]{1,16}|\\*) enters the Court, record their home in a book for the Sovereign\\.",
        Pattern.CASE_INSENSITIVE);

    private CovenantSpecificRules() {}

    public static boolean validTarget(String name) {
        return TARGET.matcher(name).matches();
    }

    public static Rule parse(String text) {
        String input = text.trim();
        if (input.equalsIgnoreCase("Transfer signed contracts to the Sovereign.")) {
            return new Rule(Kind.SIGNED_CONTRACTS, "*");
        }

        var match = HOME.matcher(input);
        return match.matches() ? new Rule(Kind.HOME_REPORT, match.group(1)) : null;
    }
}
