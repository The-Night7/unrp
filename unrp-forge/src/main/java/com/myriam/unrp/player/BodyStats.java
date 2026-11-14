package com.myriam.unrp.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Les stats du personnage agissent VRAIMENT sur le corps du joueur (toujours, pouvoir actif ou non).
 * Les stats utilisées sont les stats effectives : fiche + entraînement + bonus du mode / de la forme en cours.
 *
 *  Puissance     : dégâts au corps à corps +0,4 par point ; recul infligé +0,05 par point
 *  Défense       : armure +1 par point ; robustesse +0,4 par point ; résistance au recul +4 % par point (max 60 %)
 *  Récupération  : PV max +1 (un demi-cœur) par point ; récupération naturelle (voir regenEvery)
 *  Vitesse       : vitesse de déplacement +2 % par point ; vitesse d'attaque +0,1 par point
 *  Ruse          : pas d'effet sur le corps ; elle allonge la durée des effets des capacités et l'aura max
 *
 * Toutes les valeurs sont ici : modifie-les pour rééquilibrer.
 */
public final class BodyStats {
    private BodyStats() {}

    public static final double DAMAGE_PER_POWER = 0.4, KNOCKBACK_PER_POWER = 0.05;
    public static final double ARMOR_PER_DEFENSE = 1.0, TOUGHNESS_PER_DEFENSE = 0.4, KB_RES_PER_DEFENSE = 0.04;
    public static final double HEALTH_PER_RECOVERY = 1.0;
    public static final double SPEED_PER_SPEED = 0.02, ATTACK_SPEED_PER_SPEED = 0.1;

    /** Récupération naturelle : 0,5 PV toutes les N ticks si blessé (200 - 15 x Récupération, minimum 40). */
    public static int regenEvery(int recovery) {
        return Math.max(40, 200 - 15 * recovery);
    }

    /** Applique (ou retire si st == null) les modificateurs. Appelé chaque seconde. */
    public static void apply(ServerPlayer p, int[] st) {
        set(p, Attributes.ATTACK_DAMAGE, "power_dmg", st == null ? 0 : DAMAGE_PER_POWER * st[0], AttributeModifier.Operation.ADDITION);
        set(p, Attributes.ATTACK_KNOCKBACK, "power_kb", st == null ? 0 : KNOCKBACK_PER_POWER * st[0], AttributeModifier.Operation.ADDITION);
        set(p, Attributes.ARMOR, "def_armor", st == null ? 0 : ARMOR_PER_DEFENSE * st[1], AttributeModifier.Operation.ADDITION);
        set(p, Attributes.ARMOR_TOUGHNESS, "def_tough", st == null ? 0 : TOUGHNESS_PER_DEFENSE * st[1], AttributeModifier.Operation.ADDITION);
        set(p, Attributes.KNOCKBACK_RESISTANCE, "def_kbres", st == null ? 0 : Math.min(0.6, KB_RES_PER_DEFENSE * st[1]), AttributeModifier.Operation.ADDITION);
        set(p, Attributes.MAX_HEALTH, "rec_hp", st == null ? 0 : HEALTH_PER_RECOVERY * st[2], AttributeModifier.Operation.ADDITION);
        set(p, Attributes.MOVEMENT_SPEED, "spd_move", st == null ? 0 : SPEED_PER_SPEED * st[4], AttributeModifier.Operation.MULTIPLY_BASE);
        set(p, Attributes.ATTACK_SPEED, "spd_atk", st == null ? 0 : ATTACK_SPEED_PER_SPEED * st[4], AttributeModifier.Operation.ADDITION);
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }

    private static void set(ServerPlayer p, Attribute attr, String key, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        UUID id = UUID.nameUUIDFromBytes(("unrp:body:" + key).getBytes(StandardCharsets.UTF_8));
        AttributeModifier cur = inst.getModifier(id);
        if (cur != null && Math.abs(cur.getAmount() - amount) < 1e-6) return;
        if (cur != null) inst.removeModifier(id);
        if (amount != 0) inst.addTransientModifier(new AttributeModifier(id, "unrp " + key, amount, op));
    }
}
