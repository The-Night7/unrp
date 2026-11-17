package com.myriam.unrp.ability;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AbilityEvents {
    @SubscribeEvent
    public void onServerStart(net.minecraftforge.event.server.ServerStartedEvent event) { Specials17.start(event.getServer()); }

    @SubscribeEvent
    public void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) { Specials17.shutdown(event.getServer()); }

    @SubscribeEvent
    public void onAttack(LivingAttackEvent e) {
        if (e.isCanceled()) return;
        AbilityEngine.onAttack(e);
    }

    @SubscribeEvent
    public void onKnockback(net.minecraftforge.event.entity.living.LivingKnockBackEvent e) {
        if (Specials7.cancelKnockback(e.getEntity())) e.setCanceled(true);
    }

    @SubscribeEvent
    public void onHeal(net.minecraftforge.event.entity.living.LivingHealEvent e) {
    }

    @SubscribeEvent
    public void onKnockbackMarked(net.minecraftforge.event.entity.living.LivingKnockBackEvent e) {
    }

    @SubscribeEvent
    public void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent e) {
        if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) Specials17.onAttackAttempt(player);
    }

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

    @SubscribeEvent
    public void onDrops(net.minecraftforge.event.entity.living.LivingDropsEvent e) {
    }

    @SubscribeEvent
    public void onDamage(net.minecraftforge.event.entity.living.LivingDamageEvent e) {
    }

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

    @SubscribeEvent
    public void onFall(net.minecraftforge.event.entity.living.LivingFallEvent e) {
        if (Specials3.noFall(e.getEntity()) || Specials6.noFall(e.getEntity())) {
            e.setCanceled(true);
            return;
        }
        if (AbilityEngine.cloaked(e.getEntity())) e.setDamageMultiplier(e.getDamageMultiplier() * 0.5f);
        e.setDamageMultiplier(e.getDamageMultiplier() * AbilityEngine.fallMult(e.getEntity()));
    }

    @SubscribeEvent
    public void onVisibility(net.minecraftforge.event.entity.living.LivingEvent.LivingVisibilityEvent e) {
        if (AbilityEngine.cloaked(e.getEntity())) e.modifyVisibility(0.1);
    }

    @SubscribeEvent
    public void onDeath(net.minecraftforge.event.entity.living.LivingDeathEvent e) {
        if (com.myriam.unrp.item.Phylactery.onDeath(e)) return;
        Alignment.onDeath(e);
        if (Specials2.onDeath(e)) return;
        if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p) AbilityEngine.onDeath(p);
    }
}
