package com.myriam.unrp.look;

public final class StatClientCache {
    private StatClientCache() {}

    public static int hudMode = 0;
    public static int aura, auraMax, momentum;

    public static void update(int a, int max) {
        aura = a;
        auraMax = max;
    }

    public static int motes = 0;
    public static int motesMax = 0;
    public static int weaveFlags = 0;

    public static void setWeave(int motesValue, int motesMaxValue, int flags) {
        motes = motesValue;
        motesMax = motesMaxValue;
        weaveFlags = flags;
    }
}
