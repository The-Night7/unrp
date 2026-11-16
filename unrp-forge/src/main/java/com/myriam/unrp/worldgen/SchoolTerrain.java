package com.myriam.unrp.worldgen;

import java.util.Arrays;

/** Terrain fitting has no random state and is independent of chunk placement order. */
public final class SchoolTerrain {
    private SchoolTerrain() {}
    public static int floor(int seaLevel,int[] samples,int min,int max) {
        if(samples.length==0)throw new IllegalArgumentException("Missing terrain samples");
        int[] sorted=samples.clone();Arrays.sort(sorted);
        return Math.max(min,Math.min(max,Math.max(seaLevel+1,sorted[(sorted.length-1)*3/5])));
    }
    public static int apronHeight(int natural,int platform,int edgeDistance,int width) {
        double t=Math.max(0,Math.min(1,(double)edgeDistance/width));
        return (int)Math.round(natural+(platform-natural)*t);
    }
}
