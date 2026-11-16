package com.myriam.unrp.item;

/** Medical-bed aura recovery per second, in addition to ordinary regeneration. */
public final class RecoveryTreatment {
    private RecoveryTreatment() {}
    public static int nextAura(int current,int maximum) {
        return (int)Math.min(maximum,(long)current+Math.max(1,maximum/50));
    }
}
