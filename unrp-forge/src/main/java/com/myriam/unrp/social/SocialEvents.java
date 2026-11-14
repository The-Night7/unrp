package com.myriam.unrp.social;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

/** Règles de combat liées au hub : fin des duels à 2 cœurs, points d'entraînement gagnés au combat. */
public class SocialEvents {
    @SubscribeEvent
    public void onHurt(LivingHurtEvent e) {
        Entity src = e.getSource().getEntity();
        if (e.getEntity() instanceof ServerPlayer victim) {
            SocialService.Duel du = SocialService.duelOf(victim.getUUID());
            if (du != null && src instanceof ServerPlayer atk) {
                UUID other = du.a.equals(victim.getUUID()) ? du.b : du.a;
                if (atk.getUUID().equals(other) && victim.getHealth() - e.getAmount() <= 4.0F) {
                    e.setCanceled(true);
                    SocialService.end(victim.server, du, atk.getUUID(), "2 cœurs");
                    return;
                }
            }
            if (src instanceof ServerPlayer atk && atk != victim) {
                SocialService.Duel ad = SocialService.duelOf(atk.getUUID());
                if (ad == null || !"friendly".equals(ad.kind)) SocialService.addXp(atk, 1);
            }
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        Entity src = e.getSource().getEntity();
        LivingEntity dead = e.getEntity();
        if (dead instanceof ServerPlayer victim) {
            SocialService.Duel du = SocialService.duelOf(victim.getUUID());
            if (du != null) {
                UUID other = du.a.equals(victim.getUUID()) ? du.b : du.a;
                SocialService.end(victim.server, du, other, "mort");
            } else if (src instanceof ServerPlayer atk && atk != victim) {
                SocialService.addXp(atk, 10);
            }
        } else if (src instanceof ServerPlayer atk && dead.getMaxHealth() >= 10.0F) {
            SocialService.addXp(atk, 3);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        com.myriam.unrp.ability.AbilityEngine.onLogout(p.getUUID());
        SocialService.OFFERS.removeIf(o -> o.from().equals(p.getUUID()) || o.to().equals(p.getUUID()));
        SocialService.Duel du = SocialService.duelOf(p.getUUID());
        if (du != null) {
            UUID other = du.a.equals(p.getUUID()) ? du.b : du.a;
            SocialService.end(p.server, du, other, "déconnexion");
        }
    }
}
