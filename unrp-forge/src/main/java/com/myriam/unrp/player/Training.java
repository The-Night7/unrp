package com.myriam.unrp.player;

public final class Training {
    private Training() {}

    public static final int MAX_STEPS = 4;

    public static final int STEP = 5;

    public static final int CEILING = 100;

    public static int cost(int steps) {
        return steps + 1;
    }

    public static int gain(int potTenths) {
        return Math.max(0, Math.min(STEP, CEILING - potTenths));
    }

    public static int spent(int[] steps) {
        int s = 0;
        for (int n : steps) s += n * (n + 1) / 2;
        return s;
    }

    public static int refundTenths(int partial) {
        return partial > 0 ? partial : STEP;
    }

    public static int refundPoints(int steps) {
        return steps;
    }
}
