package com.myriam.unrp.item;

/** Progress towards restoring a disabled ability. Rates are percentage points per treatment second. */
public final class RecoveryTreatment {
    private RecoveryTreatment() {}
    public static double advance(double progress,double rate) {
        if(!Double.isFinite(progress)||!Double.isFinite(rate)||rate<=0)throw new IllegalArgumentException("Invalid recovery progress or rate");
        return Math.min(100,Math.max(0,progress)+rate);
    }
}
