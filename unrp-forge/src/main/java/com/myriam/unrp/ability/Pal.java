package com.myriam.unrp.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/**
 * Attributs de Palladium, comme les capacités « palladium:attribute_modifier » du modèle. Le vol passe par
 * palladium:flight_speed : dès que l'attribut dépasse 0, c'est le FlightHandler de Palladium qui fait voler le joueur
 * (double saut pour décoller, ses animations, son son). Les UUID et les valeurs sont ceux des fiches du modèle.
 */
public final class Pal {
    private Pal() {}

    public static final ResourceLocation FLIGHT_SPEED = new ResourceLocation("palladium", "flight_speed");
    public static final ResourceLocation JUMP_POWER = new ResourceLocation("palladium", "jump_power");

    /** Pose (on) ou retire (off) un modificateur additif sur un attribut Palladium. */
    public static void mod(LivingEntity e, ResourceLocation attribute, UUID id, String name, double amount, boolean on) {
        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(attribute);
        AttributeInstance inst = attr == null ? null : e.getAttribute(attr);
        if (inst == null) return;
        AttributeModifier cur = inst.getModifier(id);
        if (on) {
            if (cur != null && cur.getAmount() == amount) return;
            if (cur != null) inst.removeModifier(id);
            inst.addTransientModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.ADDITION));
        } else if (cur != null) {
            inst.removeModifier(id);
        }
    }

    public static void flight(LivingEntity e, UUID id, String name, double amount, boolean on) {
        mod(e, FLIGHT_SPEED, id, name, amount, on);
    }

    /** Vitesse de vol Palladium actuelle (0 = ne vole pas). */
    public static double flightSpeed(LivingEntity e) {
        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(FLIGHT_SPEED);
        AttributeInstance inst = attr == null ? null : e.getAttribute(attr);
        return inst == null ? 0 : inst.getValue();
    }

    /** Vole avec Palladium (TempestClawsEvents.soaring du modèle : type de vol du FlightHandler non nul). */
    public static boolean soaring(net.minecraft.world.entity.player.Player p) {
        return p instanceof net.threetag.palladium.entity.PalladiumPlayerExtension ext
                && ext.palladium$getFlightHandler().getFlightType().isNotNull();
    }

    /** En l'air au sens du modèle (LimitBreakerEvents.airborne). */
    public static boolean airborne(net.minecraft.world.entity.player.Player p) {
        return !p.onGround() && !p.isInWater() && !p.isInLava() && !p.isPassenger() && !p.isFallFlying() && !p.onClimbable() && !p.getAbilities().flying;
    }
}
