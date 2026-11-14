package com.myriam.unrp.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Un pouvoir jouable : une entrée "powers.<id>" de data/unrp/unodex/ability_powers.json.
 * Même structure que les pouvoirs du mod modèle : un interrupteur (touche R) + jusqu'à 5 capacités
 * sur la barre (touches X C V B N). Référence complète de chaque champ : docs/POUVOIRS.md.
 * Les noms de champs JSON sont en snake_case (drainEvery ici = "drain_every" dans le JSON).
 */
public class AbilityPower {
    /** ANCIEN format : aura perdue par seconde tant que le pouvoir est actif. Préférer drainEvery. */
    public double drain;
    /** -1 aura toutes les N ticks tant que le pouvoir est actif (0 = gratuit). 20 ticks = 1 seconde. */
    public int drainEvery;
    /** effets de potion maintenus tant que le pouvoir est actif */
    public List<AbilityEffect.Eff> passive = new ArrayList<>();
    /** attributs modifiés tant que le pouvoir est actif (vitesse, armure...) */
    public List<Attr> attributes = new ArrayList<>();
    /** dégâts renvoyés à qui te frappe au corps à corps tant que le pouvoir est actif (0 = aucun) */
    public double thorns;
    /** effet à l'activation : "sky_strike" = la foudre tombe sur toi si tu vois le ciel (Lightning) */
    public String onActivate;
    /** pose jouée à l'activation */
    public String activatePose;
    /** son du modèle (unrp:<id>) joué quand on allume le pouvoir, ex. "tm_on" */
    public String activateSound;
    /** Volume et hauteur du son d'activation (1,5 / 1 par défaut). */
    public float activateVol = 1.5f, activatePitch = 1.0f;
    /** clé de capacité du modèle allumée tant que le pouvoir est actif (moteur d'effets : « lightning_activation »...) */
    public String passOn;
    /** tag posé tant que le pouvoir est actif (« unrp.active.barrier »), comme la capacité « active_tag » du modèle */
    public String activeTag;
    /** autres tags posés tant que le pouvoir est actif, et tag posé à l'extinction (« last_tick_commands » de l'interrupteur du modèle) */
    public java.util.List<String> extraActiveTags = new java.util.ArrayList<>();
    public String offTag;
    /** icône et couleur de cadre de la première case (l'interrupteur), comme la fiche Palladium du modèle */
    public String icon;
    public String barColor;
    /** tant que le pouvoir est actif : les créatures dans ce rayon brillent (sens électrique de Lightning) ; 0 = non */
    public double senseRadius;
    /** soin passif tant que le pouvoir est actif : "heal_amount" PV toutes les "heal_every" ticks */
    public int healEvery;
    public float healAmount;
    /** tant que le pouvoir est actif : grimper aux murs comme une araignée (accroupi : rester accroché) */
    public boolean climb;
    /** tant que le pouvoir est actif : dégâts de chute multipliés par ce nombre (0 = normal) */
    public double fallMult;
    /** la perte d'aura du pouvoir ne s'applique que si le joueur est blessé (Regeneration) */
    public boolean drainWhenHurt;
    /** pouvoirs à plusieurs formes (Modes, Phase Shift) : la forme courante ajoute son passif et son coût */
    public List<Mode> modes = new ArrayList<>();
    /** postures (noms) : la capacité spéciale "stance_next" passe à la suivante et change les capacités de la barre */
    public List<String> stances = new ArrayList<>();
    public List<Move> moves = new ArrayList<>();
    /** apparence (couleur, particule) d'après le webtoon */
    public Look look;

    /** Modificateur d'attribut Minecraft, comme "palladium:attribute_modifier" dans le modèle. */
    public static class Attr {
        /** ex. minecraft:generic.movement_speed, minecraft:generic.armor, minecraft:generic.attack_damage */
        public String attribute;
        public double amount;
        /** 0 = ajoute amount ; 1 = +amount x valeur de base ; 2 = x (1 + amount) sur le total */
        public int operation;
    }

    public static class Look {
        /** #RRGGBB */
        public String color;
        public String particle;
        /** trait en zigzag (foudre) */
        public boolean jagged;
        /** style visuel : lightning, fire, ice, water, wind, shadow, light, blood, nature, mind, gravity, barrier, cloak,
         *  speed, strength, heal, poison, earth, metal, sound, space, energy (voir ability/Styles.java) */
        public String style;
        /** particule principale, si on veut autre chose que celle du style (voir ModParticles.NAMES) */
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
        /** bonus/malus de stats dans ce mode : [Puissance, Défense, Récupération, Ruse, Vitesse] */
        public int[] stats;
    }

    public static class Move {
        public String name;
        /** texte affiché dans le hub (onglet Capacités) */
        public String desc;
        /**
         * action = un effet par pression (recharge) ;
         * charge = maintenir puis relâcher (puissance selon le temps de charge) ;
         * held   = actif tant que la touche est maintenue ;
         * toggle = une pression allume, une autre éteint (reste allumé).
         */
        public String input = "action";
        /** charge : ticks de maintien pour la puissance maximale */
        public int charge = 30;
        /** aura retirée à l'utilisation (action/charge), ou par pulsation pour un held sans drainEvery */
        public int cost;
        /** aura minimale pour pouvoir lancer (0 = égale au coût) */
        public int minAura;
        /** recharge de CE mouvement, en ticks (jamais sur l'activation du pouvoir) */
        public int cooldown;
        /** held/toggle : -1 aura toutes les N ticks tant qu'il est actif ; shell : tant qu'une barrière existe */
        public int drainEvery;
        /** held/toggle : effets de potion et attributs tant qu'il est actif */
        public List<AbilityEffect.Eff> passive = new ArrayList<>();
        public List<Attr> attributes = new ArrayList<>();
        /** held : course forcée vers l'avant à cette vitesse (blocs/tick) tant que c'est maintenu (Surge Run) */
        public double surge;
        /** toggle : cape d'invisibilité (chute amortie, les monstres te repèrent mal, attaquer te trahit 1 s) */
        public boolean cloak;
        /** held/toggle : soin "heal_amount" PV toutes les "heal_every" ticks tant qu'actif */
        public int healEvery;
        public float healAmount;
        /** toggle : un seul interrupteur allumé par groupe (formes de Phase Shift) */
        public String group;
        /** toggle : effets donnés à la cible de chaque coup au corps à corps (Ice Blades : lenteur) */
        public List<AbilityEffect.Eff> onHit = new ArrayList<>();
        /** held : intensité de chaque pulsation (défaut 0,25) */
        public Double k;
        /** rang de déblocage (2, 3 ou 4) : coûte rang-1 points d'aptitude, valable pour tous tes pouvoirs ; 0 = disponible d'emblée */
        public int unlock;
        /** exige la maîtrise pleine (OC au niveau 10 ; personnage de l'Unodex : toujours) : Domaine des mille lames */
        public boolean mastery;
        /** index (dans "moves") de la capacité à débloquer d'abord ; -1 = aucune */
        public int requires = -1;
        /** pose du corps jouée à l'utilisation (assets/unrp/poses.json) ; absente = pose par défaut selon fx.kind */
        public String pose;
        /** clé de capacité du modèle allumée tant que cette capacité est tenue ou allumée (« surge_run », « beam »...) */
        public String pass;
        /** tag posé à l'utilisation (« unrp.barrier_deploy_trigger »), comme les commandes Palladium du modèle : son code Java le lit */
        public String triggerTag;
        /** tag présent tant que la capacité est tenue ou allumée (« unrp.heat_palm_smelt_drain »...) : le code et les poses du modèle le lisent */
        public String holdTag;
        /** Conditions Palladium « has_tag » / « not has_tag » de la capacité du modèle. */
        public String needTag, blockTag;
        /** Case visible seulement avec ce tag (« !tag » : sans lui) — deux capacités du modèle sur la même touche selon la posture. */
        public String showTag;
        /** Condition Palladium « or » : au moins une (tag, ou « score:objectif » >= 1). */
        public java.util.List<String> needAny;
        /** Tag posé au relâchement d'une capacité maintenue (« last_tick_commands » du modèle). */
        public String releaseTag;
        /** Aura retirée à l'appui d'une capacité maintenue (« scoreboard players remove » du modèle). */
        public int pressCost;
        /** case de la barre de Palladium (list_index du modèle : 1 à 4 = première liste, 5 à 9 = deuxième...) ; -1 = rang + 1 */
        public int barSlot = -1;
        /** icône (texture) et couleur de cadre de la case */
        public String icon;
        public String barColor;
        /** tag posé « flash_ticks » ticks à l'utilisation (commande Palladium à durée du modèle) */
        public String flashTag;
        public int flashTicks = 40;
        /** son du modèle (assets/unrp/sounds.json, ex. "lt_bolt") joué à l'utilisation, à la place de la signature de la pose */
        public String sound;
        /** volume de ce son (celui du modèle : 1,5 par défaut ; au-dessus de 1, il porte plus loin) */
        public float soundVol = 1.5f;
        /** pose si le joueur est accroupi (variante de la capacité), sinon "pose" */
        public String poseSneak;
        /** posture où la capacité apparaît (0, 1...) ; -1 = toujours */
        public int stance = -1;
        /** mode où la capacité apparaît (index dans "modes") ; -1 = tous les modes */
        public int mode = -1;
        /** bonus/malus de stats tant que la capacité est allumée ou tenue : [Puissance, Défense, Récupération, Ruse, Vitesse] */
        public int[] stats;
        /** mouvement spécial : mode_next, stance_next, ou une capacité propre au pouvoir (ability/Specials.java) */
        public String special;
        /** l'effet déclenché (voir AbilityEffect / docs/POUVOIRS.md) ; peut être absent pour un toggle purement passif */
        public AbilityEffect fx;
    }
}
