package com.myriam.unrp.ability;

import com.myriam.unrp.data.AbilityPower;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Liens d'aura (lot 10) : Energy Conduit, repris de EnergyConduitEvents. Fiche : docs/fidelite/energy_conduit.md.
 */
public final class Specials10 {
    private Specials10() {}

    private static final Set<String> ALL = Set.of("ec_lasso", "ec_transfer", "ec_restore", "ec_control");
    private static final int MAX_LINKS = 2;
    private static final double LINK_RANGE = 38.0;

    public static boolean handles(String s) {
        return s != null && ALL.contains(s);
    }

    private static final Map<UUID, List<UUID>> LINKS = new HashMap<>();   // lanceur -> cibles liées (la plus récente en dernier)
    private static final Map<UUID, Integer> RESTRAINT = new HashMap<>();
    private static final Set<UUID> CHANNEL = new HashSet<>();

    public static void onLogout(UUID id) {
        LINKS.remove(id);
        RESTRAINT.remove(id);
        CHANNEL.remove(id);
    }

    private static void msg(ServerPlayer p, String s) {
        p.displayClientMessage(Component.literal(s), true);
    }

    private static LivingEntity resolve(ServerLevel lv, UUID id) {
        return lv.getEntity(id) instanceof LivingEntity le && le.isAlive() ? le : null;
    }

    private static List<LivingEntity> links(ServerPlayer p) {
        List<LivingEntity> out = new ArrayList<>();
        List<UUID> ids = LINKS.get(p.getUUID());
        if (ids == null) return out;
        ids.removeIf(i -> {
            LivingEntity t = resolve(p.serverLevel(), i);
            if (t == null || t.distanceTo(p) > LINK_RANGE) {
                if (t != null) t.removeTag("unrp.aura_linked");
                return true;
            }
            out.add(t);
            return false;
        });
        return out;
    }

    private static void cutAll(ServerPlayer p) {
        List<UUID> ids = LINKS.remove(p.getUUID());
        if (ids == null) return;
        for (UUID i : ids) {
            LivingEntity t = resolve(p.serverLevel(), i);
            if (t != null) t.removeTag("unrp.aura_linked");
        }
    }

    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m) {
        ServerLevel lv = p.serverLevel();
        UUID id = p.getUUID();
        switch (m.special) {
            case "ec_lasso" -> {
                List<LivingEntity> ls = links(p);
                if (p.isShiftKeyDown()) { // couper le dernier lien
                    if (ls.isEmpty()) {
                        msg(p, "§bAucun lien d'aura à couper.");
                        return false;
                    }
                    LivingEntity last = ls.get(ls.size() - 1);
                    LINKS.get(id).remove(last.getUUID());
                    last.removeTag("unrp.aura_linked");
                    msg(p, "§bLe lien d'aura le plus récent est coupé.");
                    return false; // pas de coût ni de recharge
                }
                if (ls.size() >= MAX_LINKS) {
                    msg(p, "§c✖ Tous les liens d'aura sont pris §7(accroupi + appui pour en couper un).");
                    return false;
                }
                Vec3 look = p.getViewVector(1f);
                Projectiles.P lasso = new Projectiles.P();
                lasso.kind = Projectiles.Kind.NEEDLE;
                lasso.owner = id;
                lasso.pos = p.getEyePosition().add(look.scale(0.7)).add(0, -0.15, 0);
                lasso.vel = look.scale(1.4);
                lasso.maxLife = 30;
                lasso.damage = 0f;
                lasso.size = 0.6f;
                lasso.rgb = 0x5CC8FF;
                lasso.lineTrail = true;
                lasso.onHit = (pr, e) -> {
                    List<UUID> l = LINKS.computeIfAbsent(id, k -> new ArrayList<>());
                    if (!l.contains(e.getUUID()) && l.size() < MAX_LINKS) {
                        l.add(e.getUUID());
                        e.addTag("unrp.aura_linked");
                        msg(p, "§bLien d'aura noué avec " + e.getName().getString());
                    }
                };
                Projectiles.spawn(lasso);
            }
            case "ec_transfer", "ec_restore", "ec_control" -> {
                CHANNEL.add(id);
                channel(p, lv, m.special);
            }
            default -> { }
        }
        return true;
    }

    public static void whileOn(ServerPlayer p, AbilityPower.Move m, long now) { }

    public static void stop(ServerPlayer p, String special) {
        if (special.startsWith("ec_") && !special.equals("ec_lasso")) {
            CHANNEL.remove(p.getUUID());
            RESTRAINT.remove(p.getUUID());
        }
    }

    /** Canalisation maintenue (toutes les 4 ticks) : donner, soigner ou attirer ceux qui sont liés. */
    private static void channel(ServerPlayer p, ServerLevel lv, String kind) {
        UUID id = p.getUUID();
        long tick = p.tickCount;
        boolean sneak = p.isShiftKeyDown();
        List<LivingEntity> ls = links(p);
        if (ls.isEmpty()) {
            if (tick % 40 < 4) msg(p, "§bAucun lien d'aura pour canaliser.");
            return;
        }
        boolean pulse = tick % 10 < 4;
        switch (kind) {
            case "ec_transfer" -> {
                for (LivingEntity t : ls) {
                    if (!sneak) { // donner de l'aura aux alliés liés
                        if (pulse && t instanceof ServerPlayer ap && AbilityEngine.aura(p) > 15) {
                            AbilityEngine.spendAura(p, 2);
                            AbilityEngine.refundAura(ap, 2);
                        }
                    } else if (pulse) { // puiser l'aura d'un lié
                        if (t instanceof ServerPlayer enemy) {
                            int their = AbilityEngine.aura(enemy);
                            if (their > 20) {
                                int take = Math.min(2, their - 20);
                                AbilityEngine.spendAura(enemy, take);
                                AbilityEngine.refundAura(p, take);
                            } else if (tick % 40 < 4) msg(p, "§bSon aura est trop mince pour y puiser.");
                        } else {
                            AbilityEngine.refundAura(p, 1);
                        }
                    }
                    Fx.line(lv, p.getBoundingBox().getCenter(), t.getBoundingBox().getCenter(), 0x5CC8FF, 0, 0.1f, 5);
                }
            }
            case "ec_restore" -> {
                for (LivingEntity t : ls) {
                    if (!sneak) {
                        if (pulse && AbilityEngine.aura(p) > 4 && t.getHealth() < t.getMaxHealth()) {
                            AbilityEngine.spendAura(p, 4);
                            t.heal(2f);
                        }
                    } else if (pulse) {
                        if (p.getHealth() >= p.getMaxHealth()) {
                            if (tick % 40 < 4) msg(p, "§bTu es déjà en pleine forme.");
                        } else if (t instanceof ServerPlayer enemy) {
                            if (AbilityEngine.aura(enemy) > 20) {
                                AbilityEngine.spendAura(enemy, 3);
                                p.heal(2f);
                            } else if (tick % 40 < 4) msg(p, "§bSon aura est trop mince pour y puiser.");
                        } else {
                            t.hurt(lv.damageSources().magic(), 1.5f);
                            p.heal(1.5f);
                        }
                    }
                    Fx.line(lv, p.getBoundingBox().getCenter(), t.getBoundingBox().getCenter(), 0x7CFF9A, 0, 0.1f, 5);
                }
            }
            case "ec_control" -> {
                if (!sneak) { // attirer ceux qui sont liés
                    RESTRAINT.put(id, 0);
                    for (LivingEntity t : ls) {
                        Vec3 pull = p.position().add(0, 0.5, 0).subtract(t.position().add(0, t.getBbHeight() * 0.5, 0)).scale(0.12);
                        if (pull.length() > 0.6) pull = pull.normalize().scale(0.6);
                        t.setDeltaMovement(t.getDeltaMovement().scale(0.4).add(pull));
                        t.hurtMarked = true;
                        t.fallDistance = 0;
                        Fx.line(lv, p.getBoundingBox().getCenter(), t.getBoundingBox().getCenter(), 0xFFB347, 0, 0.1f, 5);
                    }
                } else { // entraver : plus on tient, plus c'est serré
                    int rt = RESTRAINT.getOrDefault(id, 0) + 4;
                    RESTRAINT.put(id, rt);
                    int amp = Math.min(5, 1 + rt / 40);
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 2, false, false));
                    for (LivingEntity t : ls) {
                        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 12, amp, false, false));
                        if (rt > 40) t.addEffect(new MobEffectInstance(MobEffects.JUMP, 12, 128, false, false));
                        if (tick % 8 < 4) AbilityEngine.spendAura(p, 1);
                        Fx.line(lv, p.getBoundingBox().getCenter(), t.getBoundingBox().getCenter(), 0xFFB347, 0, 0.14f, 5);
                    }
                }
            }
            default -> { }
        }
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            UUID id = p.getUUID();
            if (LINKS.containsKey(id) && !AbilityEngine.isOn(p, "energy_conduit")) cutAll(p); // éteindre coupe tous les liens
        }
    }
}
