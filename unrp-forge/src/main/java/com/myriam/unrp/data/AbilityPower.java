package com.myriam.unrp.data;

import java.util.ArrayList;
import java.util.List;

/** Runtime model for ability_powers.json. Stat arrays use Power, Defense, Recovery, Trick, Speed. */
public class AbilityPower {
    /** Runtime-only view of a leased branch; no unrelated power passives. */
    public transient String branchSource;

    public double drain;

    public int drainEvery;

    public List<AbilityEffect.Eff> passive = new ArrayList<>();

    public List<Attr> attributes = new ArrayList<>();

    public double thorns;

    public String onActivate;

    public String activatePose;

    public String activateSound;

    public float activateVol = 1.5f, activatePitch = 1.0f;

    public String passOn;

    public String activeTag;

    public java.util.List<String> extraActiveTags = new java.util.ArrayList<>();
    public String offTag;

    public String icon;
    public String barColor;

    public double senseRadius;

    public int healEvery;
    public float healAmount;

    public boolean climb;

    public double fallMult;

    public boolean drainWhenHurt;

    public List<Mode> modes = new ArrayList<>();

    public List<String> stances = new ArrayList<>();
    public List<Move> moves = new ArrayList<>();

    public Look look;

    /** A leased move keeps its definition and visual identity, without unrelated base-power passives. */
    public AbilityPower branchView(String sourceId) {
        AbilityPower view = new AbilityPower();
        view.moves = moves; view.icon = icon; view.barColor = barColor;
        view.look = look; view.activatePose = activatePose; view.branchSource = sourceId;
        return view;
    }

    public static class Attr {
        /** ex. minecraft:generic.movement_speed, minecraft:generic.armor, minecraft:generic.attack_damage */
        public String attribute;
        public double amount;

        public int operation;
    }

    public static class Look {
        /** #RRGGBB */
        public String color;
        public String particle;

        public boolean jagged;

        public String style;

        public String sprite;

        public int rgb() {
            try {
                return Integer.parseInt(color.substring(1), 16);
            } catch (Exception e) {
                return -1;
            }
        }
    }

    public static class Mode {
        public String name;
        public double drain;
        public int drainEvery;
        public List<AbilityEffect.Eff> passive = new ArrayList<>();

        public int[] stats;
    }

    public static class Move {
        public String name;

        public String desc;

        public String input = "action";

        public int charge = 30;

        public int cost;

        public int minAura;

        public int cooldown;

        public int drainEvery;
        /** held/toggle : effets de potion et attributs tant qu'il est actif */
        public List<AbilityEffect.Eff> passive = new ArrayList<>();
        public List<Attr> attributes = new ArrayList<>();

        public double surge;

        public boolean cloak;

        public int healEvery;
        public float healAmount;

        public String group;

        public List<AbilityEffect.Eff> onHit = new ArrayList<>();

        public Double k;

        public int unlock;
        /** Minimum character level; independent of purchased ranks. */
        public double minLevel;

        public boolean mastery;

        public int requires = -1;

        public String pose;

        public String pass;

        public String triggerTag;

        public String holdTag;

        public String needTag, blockTag;

        public String showTag;

        public java.util.List<String> needAny;

        public String releaseTag;

        public int pressCost;

        public int barSlot = -1;

        public String icon;
        public String barColor;

        public String flashTag;
        public int flashTicks = 40;

        public String sound;

        public float soundVol = 1.5f;

        public String poseSneak;

        public int stance = -1;

        public int mode = -1;

        public int[] stats;

        public String special;

        public AbilityEffect fx;
    }
}
