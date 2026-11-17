package com.myriam.unrp.item;

/** Progress towards restoring a disabled ability. Rates are percentage points per treatment second. */
public final class RecoveryTreatment {
    private RecoveryTreatment() {}
    public static double advance(double progress,double rate) {
        return advance(progress,rate,100);
    }
    public static double advance(double progress,double rate,double ceiling) {
        if(!Double.isFinite(ceiling)||ceiling<1||ceiling>100)throw new IllegalArgumentException("Invalid recovery ceiling");
        if(!Double.isFinite(progress)||!Double.isFinite(rate)||rate<=0)throw new IllegalArgumentException("Invalid recovery progress or rate");
        return Math.max(progress,Math.min(ceiling,Math.max(0,progress)+rate));
    }
    public static double effectiveLevel(double originalLevel, double percent) {
        return originalLevel * Math.max(0, Math.min(100, percent)) / 100;
    }
    public static int effectiveStat(int stat,double percent) {
        return stat<=1?stat:1+(int)Math.floor((stat-1)*Math.max(0,Math.min(100,percent))/100);
    }
}
