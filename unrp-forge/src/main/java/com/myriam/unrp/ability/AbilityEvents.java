package com.myriam.unrp.ability;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Branche les événements de combat sur le moteur de pouvoirs (barrières, riposte "thorns"). */
public class AbilityEvents {
    @SubscribeEvent
    public void onAttack(LivingAttackEvent e) {
        if (e.isCanceled()) return;
        AbilityEngine.onAttack(e);
    }

    /** Explosion : le recul est annulé pour son porteur. */
    @SubscribeEvent
    public void onKnockback(net.minecraftforge.event.entity.living.LivingKnockBackEvent e) {
        if (Specials7.cancelKnockback(e.getEntity())) e.setCanceled(true);
    }

    /** Particles : les marqués guérissent moins et volent plus loin face à l'Avant-garde. */
    @SubscribeEvent
    public void onHeal(net.minecraftforge.event.entity.living.LivingHealEvent e) {
    }

    @SubscribeEvent
    public void onKnockbackMarked(net.minecraftforge.event.entity.living.LivingKnockBackEvent e) {
    }

    /** Aura Transference : un coup à mains nues copie un trait. */
    @SubscribeEvent
    public void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent e) {
    }

    /** Covenant : sauter, casser, poser et parler peuvent être des manquements ; le chat porte aussi les lois et les pactes. */
    @SubscribeEvent
    public void onJump(net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent e) {
        if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p) {
            Specials17.onJump(p);
        }
    }

    @SubscribeEvent
    public void onBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent e) {
        com.myriam.unrp.item.Phylactery.onBreak(e);
        if (e.getPlayer() instanceof net.minecraft.server.level.ServerPlayer p) Specials17.onBreak(p);
    }

    @SubscribeEvent
    public void onPlace(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent e) {
        if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p) Specials17.onPlace(p);
    }


    /** Hollow Mark : runes et charmes lâchés par les monstres forts. */
    @SubscribeEvent
    public void onDrops(net.minecraftforge.event.entity.living.LivingDropsEvent e) {
    }

    /** Hollow Mark : la Sympathie partage les dégâts finaux ; le double encaisse. */
    @SubscribeEvent
    public void onDamage(net.minecraftforge.event.entity.living.LivingDamageEvent e) {
    }

    /** Hollow Mark : serviteurs, figés et voilés changent les cibles des monstres. */
    @SubscribeEvent
    public void onTarget(net.minecraftforge.event.entity.living.LivingChangeTargetEvent e) {
    }

    @SubscribeEvent
    public void onChat(net.minecraftforge.event.ServerChatEvent e) {
        Specials17.onChat(e);
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent e) {
        AbilityEngine.onHurt(e);
    }

    /** Cape : les chutes font moitié moins mal. */
    @SubscribeEvent
    public void onFall(net.minecraftforge.event.entity.living.LivingFallEvent e) {
        if (Specials3.noFall(e.getEntity()) || Specials6.noFall(e.getEntity())) {
            e.setCanceled(true);
            return;
        }
        if (AbilityEngine.cloaked(e.getEntity())) e.setDamageMultiplier(e.getDamageMultiplier() * 0.5f);
        e.setDamageMultiplier(e.getDamageMultiplier() * AbilityEngine.fallMult(e.getEntity()));
    }

    /** Cape : les monstres ne te repèrent presque plus (visibilité x 0,1). */
    @SubscribeEvent
    public void onVisibility(net.minecraftforge.event.entity.living.LivingEvent.LivingVisibilityEvent e) {
        if (AbilityEngine.cloaked(e.getEntity())) e.modifyVisibility(0.1);
    }

    @SubscribeEvent
    public void onDeath(net.minecraftforge.event.entity.living.LivingDeathEvent e) {
        if (com.myriam.unrp.item.Phylactery.onDeath(e)) return; // un phylactère retient
        Alignment.onDeath(e);
        if (Specials2.onDeath(e)) return;
        if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p) AbilityEngine.onDeath(p);
    }
}
