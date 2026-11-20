package com.myriam.unrp.ability;

/** Names and horizontal boundaries of persistent Covenant claims. */
public final class CovenantTerritory {
    private CovenantTerritory() {}

    public static boolean validName(String name) {
        return name != null && name.matches("[A-Za-z0-9][A-Za-z0-9 _-]{0,47}");
    }

    public static boolean overlaps(double x, double z, double radius,
                                   double otherX, double otherZ, double otherRadius) {
        return Math.hypot(x - otherX, z - otherZ) < radius + otherRadius;
    }
}
