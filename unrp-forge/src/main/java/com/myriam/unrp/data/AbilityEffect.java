package com.myriam.unrp.data;

import java.util.ArrayList;
import java.util.List;

/** Generic effect fields loaded from snake_case JSON; durations are ticks unless named secs. */
public class AbilityEffect {
    /** strike | beam | blast | buff | dash | teleport | heal | debuff | pulse | scan | dispel | wall | bolt | shell | grow | ram | share */
    public String kind = "buff";

    public int passOp = -1;
    public int cost = 20;

    public int cooldown = 60;
    public double range, radius, dmg, kb, up, power, amount, explosion, lifesteal, glow;
    public int hits = 1, fire;

    public double root;
    public boolean cleanse, report;

    public boolean bolt;

    public String block;

    public double speed;
    public int life;

    public int max;
    public double recoil;

    public boolean single;
    public String particle = "crit";
    public List<Eff> selfEffects = new ArrayList<>();
    public List<Eff> effects = new ArrayList<>();

    public static class Eff {
        /** nom d'effet de potion Minecraft : strength, speed, slowness, nausea... */
        public String id;

        public int amp;
        public double secs = 10;
    }
}
