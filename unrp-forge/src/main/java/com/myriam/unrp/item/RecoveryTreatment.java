package com.myriam.unrp.item;

/** Medical-bed aura recovery per second, in addition to ordinary regeneration. */
public final class RecoveryTreatment {
    private RecoveryTreatment() {}
    public static int nextAura(int current,int maximum) {
        return nextAura(current,maximum,1);
    }
    public static int nextAura(int current,int maximum,double rate) {
        return (int)Math.min(maximum,(long)current+Math.max(1,(long)Math.floor(maximum*rate/50)));
    }
}
