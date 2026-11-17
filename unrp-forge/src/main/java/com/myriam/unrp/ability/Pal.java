package com.myriam.unrp.ability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public final class Pal {
    private Pal() {}

    public static final ResourceLocation FLIGHT_SPEED = new ResourceLocation("palladium", "flight_speed");
    public static final ResourceLocation JUMP_POWER = new ResourceLocation("palladium", "jump_power");

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

    public static double flightSpeed(LivingEntity e) {
        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(FLIGHT_SPEED);
        AttributeInstance inst = attr == null ? null : e.getAttribute(attr);
        return inst == null ? 0 : inst.getValue();
    }

    public static boolean soaring(net.minecraft.world.entity.player.Player p) {
        return p instanceof net.threetag.palladium.entity.PalladiumPlayerExtension ext
                && ext.palladium$getFlightHandler().getFlightType().isNotNull();
    }

    public static boolean airborne(net.minecraft.world.entity.player.Player p) {
        return !p.onGround() && !p.isInWater() && !p.isInLava() && !p.isPassenger() && !p.isFallFlying() && !p.onClimbable() && !p.getAbilities().flying;
    }
}
