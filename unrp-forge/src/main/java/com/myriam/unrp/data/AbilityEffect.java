package com.myriam.unrp.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Ce que fait une capacité quand on appuie sur la touche (une entrée de ability_effects.json).
 * Tous les champs sont optionnels : voir docs/ABILITIES.md pour le rôle de chacun selon "kind".
 */
public class AbilityEffect {
    /** strike | beam | blast | buff | dash | teleport | heal | debuff | pulse | scan | dispel | wall | bolt | shell | grow | ram | share */
    public String kind = "buff";
    /** code d'effet du moteur du modèle (PassFxPacket) envoyé par cet effet, -1 = aucun ; ex. 3 = LT_TOUCH */
    public int passOp = -1;
    public int cost = 20;
    /** recharge, en ticks (20 = 1 seconde) */
    public int cooldown = 60;
    public double range, radius, dmg, kb, up, power, amount, explosion, lifesteal, glow;
    public int hits = 1, fire;
    /** debuff : immobilise la cible pendant ce nombre de secondes */
    public double root;
    public boolean cleanse, report;
    /** beam : fait tomber un éclair (visuel, sans dégâts de foudre) au point touché */
    public boolean bolt;
    /** wall : nom du bloc temporaire (ex. light_blue_stained_glass) */
    public String block;
    /** bolt : vitesse en blocs par tick ; life : durée de vie en ticks */
    public double speed;
    public int life;
    /** shell : nombre maximal de barrières en même temps ; dégâts subis par le lanceur quand une barrière se brise */
    public int max;
    public double recoil;
    /** strike : ne touche que la cible la plus proche devant soi */
    public boolean single;
    public String particle = "crit";
    public List<Eff> selfEffects = new ArrayList<>();
    public List<Eff> effects = new ArrayList<>();

    public static class Eff {
        /** nom d'effet de potion Minecraft : strength, speed, slowness, nausea... */
        public String id;
        /** 0 = niveau I */
        public int amp;
        public double secs = 10;
    }
}
