package com.myriam.unrp.ability;

import com.myriam.unrp.data.AbilityPower;
import com.myriam.unrp.social.SocialService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Capacités propres à un pouvoir, reprises du comportement du mod modèle (champ "special" d'une capacité).
 * Chaque fonction dit en commentaire ce qu'elle reproduit. Fiches de comparaison : docs/fidelite/.
 *
 * Statistiques du personnage (st) : 0 Puissance, 1 Défense, 2 Récupération, 3 Ruse, 4 Vitesse.
 */
public final class Specials {
    private Specials() {}

    private static final Set<String> ALL = Set.of(
            "tk_pressure", "tk_force", "tk_orbit", "tk_dominion",
            "heal_self", "heal_mark", "heal_external", "heal_triage",
            "regen_adrenaline", "martyr",
            "hydro_envelop", "hydro_freeze", "water_form",
            "nm_field", "nm_hallucinate", "nm_panic",
            "tm_flow", "tm_rewind", "tm_shift", "tm_freeze", "tm_strike", "tm_delayed");

    public static boolean handles(String special) {
        return ALL.contains(special) || Specials2.handles(special) || Specials3.handles(special) || Specials4.handles(special) || Specials5.handles(special) || Specials6.handles(special) || Specials7.handles(special) || Specials8.handles(special) || Specials9.handles(special) || Specials10.handles(special) || Specials17.handles(special);
    }

    // ------------------------------------------------------------------ outils
    private static double dmg(int[] st, double base) {
        return base * (0.7 + 0.15 * st[0]);
    }

    private static double secs(int[] st, double base) {
        return base * (0.8 + 0.05 * st[3]);
    }

    private static void msg(ServerPlayer p, String s) {
        p.displayClientMessage(Component.literal(s), true);
    }

    private static int col(AbilityPower power) {
        return power.look != null && power.look.rgb() >= 0 ? power.look.rgb() : 0xFFFFFF;
    }

    /** Créature visée (ligne de visée, "range" blocs). */
    static LivingEntity aimed(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f);
        BlockHitResult bh = p.serverLevel().clip(new ClipContext(eye, eye.add(look.scale(range)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        double max = bh.getType() == HitResult.Type.MISS ? range : eye.distanceTo(bh.getLocation());
        LivingEntity best = null;
        double bt = max;
        for (LivingEntity e : p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(range),
                e -> e != p && e.isAlive() && !e.isSpectator())) {
            var hit = e.getBoundingBox().inflate(0.4).clip(eye, eye.add(look.scale(max)));
            if (hit.isPresent() && eye.distanceTo(hit.get()) < bt) {
                bt = eye.distanceTo(hit.get());
                best = e;
            }
        }
        return best;
    }

    private static void push(Entity e, Vec3 v) {
        e.setDeltaMovement(v);
        e.hurtMarked = true;
    }

    private static boolean ally(ServerPlayer p, Entity e) {
        return e instanceof ServerPlayer o && SocialService.allies(p.server, p.getUUID(), o.getUUID());
    }

    // ------------------------------------------------------------------ états suivis chaque tick
    /** Telekinesis : créature ou bloc tenu en l'air devant soi. */
    private static final class Lift {
        Entity target;
        long until;
    }

    private static final class Orbit {
        long until;
        final Set<UUID> hit = new java.util.HashSet<>();
    }

    private static final class Frozen {
        LivingEntity e;
        Vec3 at;
        long until;
        boolean ice; // prison de glace (Hydrofreeze) ou temps figé (Time Manipulation)
    }

    private record Delayed(UUID caster, LivingEntity target, long at, double dmg) {}

    private static final Map<UUID, Lift> LIFTS = new HashMap<>();
    private static final Map<UUID, Orbit> ORBITS = new HashMap<>();
    private static final Map<UUID, UUID> HEAL_MARK = new HashMap<>();
    private static final Map<UUID, UUID> ENVELOP = new HashMap<>();
    private static final Map<UUID, Long> ENVELOP_START = new HashMap<>();
    private static final List<Frozen> FROZEN = new ArrayList<>();
    private static final List<Delayed> DELAYED = new ArrayList<>();
    private static final Map<UUID, ArrayDeque<Snap>> HISTORY = new HashMap<>();
    private static final Map<UUID, Vec3> TM_MARK = new HashMap<>();
    private static final Map<UUID, Long> MARTYR = new HashMap<>();
    private static final Map<UUID, Long> WATER_FORM = new HashMap<>();

    private record Snap(Vec3 pos, float health) {}

    private static final UUID ORBIT_KB = UUID.nameUUIDFromBytes("unrp:tk_orbit_kb".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    /** Orbite télékinétique : +0,3 de résistance au recul tant qu'elle dure (le modèle, "tk_orbit_kb"). */
    private static void orbitResist(ServerPlayer p, boolean on) {
        var inst = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);
        if (inst == null) return;
        boolean has = inst.getModifier(ORBIT_KB) != null;
        if (on && !has) inst.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(ORBIT_KB, "unrp tk_orbit_kb", 0.3,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        else if (!on && has) inst.removeModifier(ORBIT_KB);
    }

    public static void onLogout(UUID id) {
        Specials2.onLogout(id);
        Specials3.onLogout(id);
        Specials4.onLogout(id);
        Specials5.onLogout(id);
        Specials6.onLogout(id);
        Specials7.onLogout(id);
        Specials8.onLogout(id);
        Specials9.onLogout(id);
        Specials10.onLogout(id);
        Specials17.onLogout(id);
        com.myriam.unrp.item.Phylactery.onLogout(id);
        LIFTS.remove(id);
        ORBITS.remove(id);
        HEAL_MARK.remove(id);
        ENVELOP.remove(id);
        HISTORY.remove(id);
        TM_MARK.remove(id);
        MARTYR.remove(id);
        WATER_FORM.remove(id);
    }

    /** Fin d'une capacité maintenue ou d'un interrupteur. */
    public static void stop(ServerPlayer p, String special) {
        if (special == null) return;
        if ("tm_flow".equals(special)) { // fin du flux : TM_FLOW coupé, clés éteintes
            com.myriam.unrp.pass.PassSync.key(p, "anim_clock", false);
            com.myriam.unrp.pass.PassSync.key(p, "anim_counter", false);
            com.myriam.unrp.pass.TimePass.flow(p, p.serverLevel(), false, 0.0, false);
        }
        Specials2.stop(p, special);
        Specials3.stop(p, special);
        Specials4.stop(p, special);
        Specials5.stop(p, special);
        Specials6.stop(p, special);
        Specials7.stop(p, special);
        Specials8.stop(p, special);
        Specials9.stop(p, special);
        Specials10.stop(p, special);
        Specials17.stop(p, special);
        switch (special) {
            case "hydro_envelop" -> {
                ENVELOP.remove(p.getUUID());
                ENVELOP_START.remove(p.getUUID());
            }
            case "martyr" -> MARTYR.remove(p.getUUID());
            case "water_form" -> WATER_FORM.remove(p.getUUID());
            default -> { }
        }
    }

    // ------------------------------------------------------------------ déclenchement
    /** Une utilisation (action), ou une pulsation (held, toutes les 4 ticks). */
    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m, int[] st, int stance) {
        ServerLevel lv = p.serverLevel();
        long now = lv.getGameTime();
        int c = col(power);
        boolean sneak = p.isShiftKeyDown();
        if (Specials2.handles(m.special)) return Specials2.run(p, power, m, st);
        if (Specials3.handles(m.special)) return Specials3.run(p, power, m);
        if (Specials4.handles(m.special)) return Specials4.run(p, power, m);
        if (Specials5.handles(m.special)) return Specials5.run(p, power, m);
        if (Specials6.handles(m.special)) return Specials6.run(p, power, m);
        if (Specials7.handles(m.special)) return Specials7.run(p, power, m);
        if (Specials8.handles(m.special)) return Specials8.run(p, power, m);
        if (Specials9.handles(m.special)) return Specials9.run(p, power, m);
        if (Specials10.handles(m.special)) return Specials10.run(p, power, m);
        if (Specials17.handles(m.special)) return Specials17.run(p, power, m);
        switch (m.special) {
            // ---------------- Telekinesis
            case "tk_pressure" -> { // Pressure Control (maintenir) : écrase la cible visée ; accroupi : champ d'écrasement
                if (sneak) {
                    Vec3 eye = p.getEyePosition();
                    BlockHitResult bh = lv.clip(new ClipContext(eye, eye.add(p.getViewVector(1f).scale(14)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
                    Vec3 at = bh.getLocation();
                    for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(4), e -> e != p && e.isAlive())) {
                        Vec3 to = at.subtract(e.position()).scale(0.08);
                        push(e, new Vec3(to.x, -0.4, to.z));
                        e.hurt(lv.damageSources().indirectMagic(p, p), (float) dmg(st, 1.0));
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 2));
                    }
                    Fx.wave(lv, at, 4, c, 6);
                    msg(p, "Champ d'écrasement");
                    return true;
                }
                LivingEntity t = aimed(p, 14);
                if (t == null) {
                    msg(p, "Aucune cible à portée.");
                    return false;
                }
                push(t, new Vec3(0, -0.5, 0));
                t.hurt(lv.damageSources().indirectMagic(p, p), (float) dmg(st, 1.5));
                t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 3));
                Fx.line(lv, p.getEyePosition().add(0, -0.3, 0), t.getBoundingBox().getCenter(), c, Styles.LINE_HELIX, 0.08f, 5);
                Fx.sprite(lv, "ring", c, 2f, t.getBoundingBox().getCenter(), 1, 0, 0);
                return true;
            }
            case "tk_force" -> { // Force Manipulation : soulever une créature ou un bloc ; de nouveau : la lancer ; accroupi : la reposer
                Lift l = LIFTS.get(p.getUUID());
                if (l != null && l.target.isAlive() && now < l.until) {
                    LIFTS.remove(p.getUUID());
                    l.target.setNoGravity(false);
                    if (sneak) {
                        push(l.target, Vec3.ZERO);
                        msg(p, l.target instanceof FallingBlockEntity ? "Bloc reposé" : "Relâché sur place");
                    } else {
                        Vec3 v = p.getViewVector(1f).scale(2.2).add(0, 0.18, 0);
                        push(l.target, v);
                        if (l.target instanceof LivingEntity le) le.hurt(lv.damageSources().indirectMagic(p, p), (float) dmg(st, 3));
                        Fx.line(lv, p.getEyePosition(), l.target.position(), c, Styles.LINE_HELIX, 0.1f, 6);
                        msg(p, l.target instanceof FallingBlockEntity ? "Bloc lancé" : "Projeté au loin");
                    }
                    return true;
                }
                LivingEntity t = aimed(p, 14);
                if (t != null) {
                    l = new Lift();
                    l.target = t;
                    l.until = now + 120;
                    LIFTS.put(p.getUUID(), l);
                    t.setNoGravity(true);
                    msg(p, "Cible en lévitation");
                    return true;
                }
                Vec3 eye = p.getEyePosition();
                BlockHitResult bh = lv.clip(new ClipContext(eye, eye.add(p.getViewVector(1f).scale(12)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, p));
                if (bh.getType() == HitResult.Type.BLOCK) {
                    BlockPos bp = bh.getBlockPos();
                    BlockState bs = lv.getBlockState(bp);
                    if (bs.isAir() || bs.getDestroySpeed(lv, bp) < 0 || bs.getDestroySpeed(lv, bp) > 50 || lv.getBlockEntity(bp) != null) {
                        msg(p, "Ce bloc ne peut pas être soulevé.");
                        return false;
                    }
                    FallingBlockEntity fb = FallingBlockEntity.fall(lv, bp, bs);
                    fb.setNoGravity(true);
                    l = new Lift();
                    l.target = fb;
                    l.until = now + 400;
                    LIFTS.put(p.getUUID(), l);
                    msg(p, "Bloc en lévitation");
                    return true;
                }
                msg(p, "Aucune cible à portée.");
                return false;
            }
            case "tk_orbit" -> { // Telekinetic Orbit : des débris tournent autour de toi et repoussent ce qui les touche
                Orbit o = new Orbit();
                o.until = now + (long) (secs(st, 10) * 20);
                ORBITS.put(p.getUUID(), o);
                msg(p, "Orbite télékinétique");
                return true;
            }
            case "tk_dominion" -> { // Absolute Dominion : tout est projeté vers le ciel ; accroupi : tout est écrasé au sol
                double r = 12;
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r), e -> e != p && e.isAlive())) {
                    if (sneak) {
                        push(e, new Vec3(0, -2.8, 0));
                        e.hurt(lv.damageSources().indirectMagic(p, p), (float) dmg(st, 10));
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                    } else {
                        Vec3 away = e.position().subtract(p.position()).multiply(1, 0, 1);
                        away = away.lengthSqr() < 1e-4 ? Vec3.ZERO : away.normalize().scale(0.6);
                        push(e, new Vec3(away.x, 2.4, away.z));
                        e.hurt(lv.damageSources().indirectMagic(p, p), (float) dmg(st, 4));
                    }
                }
                Fx.wave(lv, p.position(), r, c, 14);
                msg(p, sneak ? "Écrasement cataclysmique" : "Expulsion violente");
                return true;
            }
            // ---------------- Healing / Regeneration
            case "heal_mark" -> { // Mark Target : marque une créature (elle brille) pour le soin à distance
                LivingEntity t = aimed(p, 16);
                if (t == null) {
                    msg(p, "Impossible de marquer ça.");
                    return false;
                }
                HEAL_MARK.put(p.getUUID(), t.getUUID());
                t.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 60, 0, false, false));
                msg(p, "Cible marquée : " + t.getName().getString());
                return true;
            }
            case "heal_external" -> { // External Heal (maintenir) : régénération II pour la cible marquée à 10 blocs
                UUID id = HEAL_MARK.get(p.getUUID());
                Entity e = id == null ? null : lv.getEntity(id);
                if (!(e instanceof LivingEntity le) || !le.isAlive() || le.distanceTo(p) > 10) {
                    msg(p, "Pas de cible marquée à 10 blocs.");
                    return false;
                }
                le.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, false, true));
                Fx.line(lv, p.getEyePosition().add(0, -0.3, 0), le.getBoundingBox().getCenter(), c, Styles.LINE_WAVY, 0.1f, 5);
                return true;
            }
            case "heal_triage" -> { // Triage Surge : soigne d'un coup tous les alliés à 8 blocs ; tu t'affaiblis 4 s
                int n = 0;
                for (Player o : lv.getEntitiesOfClass(Player.class, p.getBoundingBox().inflate(8), o -> o.isAlive())) {
                    if (o != p && !ally(p, o)) continue;
                    o.heal((float) (8 * (0.7 + 0.15 * st[2])));
                    Styles.impact(lv, power.look, c, o.getBoundingBox().getCenter(), 6);
                    n++;
                }
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                Fx.wave(lv, p.position(), 8, c, 14);
                msg(p, "Soin d'urgence (" + n + ")");
                return true;
            }
            case "regen_adrenaline" -> { // Adrenaline : +4 PV tout de suite et Vitesse 5 s
                p.heal(4);
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
                Styles.impact(lv, power.look, c, p.getBoundingBox().getCenter(), 8);
                return true;
            }
            // ---------------- Hydrofreeze
            case "hydro_envelop" -> { // Water Envelopment (maintenir) : la cible (8 blocs) est enfermée dans l'eau
                UUID cur = ENVELOP.get(p.getUUID());
                Entity e = cur == null ? null : lv.getEntity(cur);
                if (!(e instanceof LivingEntity le) || !le.isAlive()) {
                    LivingEntity t = aimed(p, 8);
                    if (t == null) {
                        msg(p, "Aucune cible");
                        return false;
                    }
                    ENVELOP.put(p.getUUID(), t.getUUID());
                    ENVELOP_START.put(p.getUUID(), now);
                    lv.playSound(null, t.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.8f, 1.1f);
                    return true;
                }
                if (le.distanceTo(p) > 10 || now - ENVELOP_START.getOrDefault(p.getUUID(), now) > 260) {
                    ENVELOP.remove(p.getUUID());
                    msg(p, "La cible s'est échappée");
                    return false;
                }
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 3));
                le.setAirSupply(Math.max(-10, le.getAirSupply() - 20));
                if (le.getAirSupply() <= 0) le.hurt(lv.damageSources().drown(), (float) dmg(st, 1.0));
                push(le, le.getDeltaMovement().multiply(0.3, 0, 0.3).add(0, -0.04, 0));
                Fx.sprite(lv, "drop", c, 1.2f, le.getBoundingBox().getCenter(), 6, 0.4, 0.02);
                Fx.sprite(lv, "ring", c, 3f, le.getBoundingBox().getCenter(), 1, 0, 0);
                return true;
            }
            case "hydro_freeze" -> { // Freeze Bind : la cible visée (18 blocs) est prise dans la glace 10 s
                LivingEntity t = aimed(p, 18);
                if (t == null) {
                    msg(p, "Aucune cible en vue");
                    return false;
                }
                freeze(t, now + 200, true);
                lv.playSound(null, t.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.9f, 0.7f);
                Fx.sprite(lv, "shard", c, 1.4f, t.getBoundingBox().getCenter(), 24, 0.5, 0.05);
                msg(p, "Cible gelée !");
                return true;
            }
            // ---------------- Nightmare
            case "nm_hallucinate" -> { // Hallucination : la cible visée (18 blocs) est prise d'une terreur forte
                LivingEntity t = aimed(p, 18);
                if (t == null) {
                    msg(p, "[Nightmare] Aucune cible en vue.");
                    return false;
                }
                fear(p, t, 3, st);
                Fx.line(lv, p.getEyePosition(), t.getEyePosition(), c, Styles.LINE_WAVY | Styles.LINE_NOCORE, 0.12f, 10);
                Fx.sprite(lv, "wisp", c, 1.5f, t.getEyePosition(), 14, 0.5, 0.03);
                return true;
            }
            case "nm_panic" -> { // Panic Spike : la cible visée est agrippée puis projetée, terrifiée
                LivingEntity t = aimed(p, 14);
                if (t == null) {
                    msg(p, "[Nightmare] Aucune cible en vue.");
                    return false;
                }
                Vec3 away = t.position().subtract(p.position()).multiply(1, 0, 1);
                away = away.lengthSqr() < 1e-4 ? p.getViewVector(1f) : away.normalize();
                double force = 4.5 + p.getRandom().nextDouble();
                push(t, new Vec3(away.x * force * 0.35, 0.6 + p.getRandom().nextDouble() * 0.3, away.z * force * 0.35));
                fear(p, t, 2, st);
                Fx.sprite(lv, "smoke", c, 1.6f, t.getBoundingBox().getCenter(), 20, 0.6, 0.06);
                return true;
            }
            // ---------------- Time Manipulation
            case "tm_rewind" -> { // Temporal Rewind : toi (ou, accroupi, l'allié visé à 12 blocs) revient 3 s en arrière
                ServerPlayer target = p;
                if (sneak) {
                    LivingEntity t = aimed(p, 12);
                    if (!(t instanceof ServerPlayer sp) || !ally(p, sp)) {
                        msg(p, "Aucun allié en vue.");
                        return false;
                    }
                    target = sp;
                }
                ArrayDeque<Snap> h = HISTORY.get(target.getUUID());
                if (h == null || h.isEmpty()) return false;
                Snap s = h.peekFirst(); // le plus ancien : 3 s
                com.myriam.unrp.pass.TimePass.rewind(p, target, lv, 60); // TM_REWIND : le moteur rejoue le chemin à l'envers
                target.teleportTo(s.pos.x, s.pos.y, s.pos.z);
                if (s.health > target.getHealth()) target.setHealth(s.health);
                for (var eff : List.of(MobEffects.POISON, MobEffects.WITHER, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.WEAKNESS,
                        MobEffects.BLINDNESS, MobEffects.CONFUSION, MobEffects.DIG_SLOWDOWN)) {
                    target.removeEffect(eff);
                }
                push(target, Vec3.ZERO);
                msg(p, "Retour en arrière");
                return true;
            }
            case "tm_shift" -> { // Temporal Shift : saut de 8 blocs en avant (le point de départ est retenu) ; accroupi : retour à ce point
                if (sneak) {
                    Vec3 mk = TM_MARK.remove(p.getUUID());
                    if (mk == null) {
                        msg(p, "Aucun point retenu.");
                        return false;
                    }
                    com.myriam.unrp.pass.TimePass.recall(p, lv, lv, p.position(), mk); // TM_RECALL
                    p.teleportTo(mk.x, mk.y, mk.z);
                    push(p, Vec3.ZERO);
                    return true;
                }
                Vec3 prevMark = TM_MARK.put(p.getUUID(), p.position());
                if (prevMark != null) com.myriam.unrp.pass.TimePass.unmark(p, lv, prevMark);
                com.myriam.unrp.pass.TimePass.send(lv, com.myriam.unrp.pass.TimePass.mark(p, p.position(), 72000, false)); // TM_MARK : le point retenu reste visible
                Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f);
                BlockHitResult bh = lv.clip(new ClipContext(eye, eye.add(look.scale(8)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
                Vec3 to = bh.getType() == HitResult.Type.MISS ? eye.add(look.scale(8)) : bh.getLocation().subtract(look.scale(0.5));
                com.myriam.unrp.pass.TimePass.blink(p, lv, p.position(), new Vec3(to.x, to.y - 1.5, to.z)); // TM_BLINK
                p.teleportTo(to.x, to.y - 1.5, to.z);
                push(p, Vec3.ZERO);
                return true;
            }
            case "tm_freeze" -> { // Time Freeze : créatures et projectiles devant toi (cône, 20 blocs) figés 2 à 4 s
                Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f);
                long until = now + (long) (40 + 40 * Math.min(1, st[3] / 10.0));
                int n = 0;
                for (Entity e : lv.getEntities(p, p.getBoundingBox().inflate(20), e -> e.isAlive())) {
                    Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
                    if (to.length() > 20 || to.normalize().dot(look) < 0.5) continue;
                    if (e instanceof LivingEntity le) {
                        freeze(le, until, false);
                        com.myriam.unrp.pass.TimePass.tracking(le, com.myriam.unrp.pass.TimePass.hold(p, le, (int) (until - now), false)); // TM_HOLD : figé en gris
                        n++;
                    } else if (e instanceof Projectile pr) {
                        pr.setDeltaMovement(Vec3.ZERO);
                        pr.setNoGravity(true);
                        pr.hurtMarked = true;
                        n++;
                    }
                }
                if (n == 0) {
                    msg(p, "Rien à figer devant toi.");
                    return false;
                }
                com.myriam.unrp.pass.TimePass.freeze(p, lv, eye.add(look.scale(4)), 20, (int) (until - now)); // TM_FREEZE
                msg(p, "Temps figé (" + n + ")");
                return true;
            }
            case "tm_strike" -> { // Temporal Strike : tu frappes la cible (cône, 12 blocs) en surgissant derrière elle
                LivingEntity t = cone(p, 12);
                if (t == null) {
                    msg(p, "Aucune cible devant toi.");
                    return false;
                }
                Vec3 behind = t.position().subtract(t.getLookAngle().multiply(1, 0, 1).normalize().scale(1.2));
                com.myriam.unrp.pass.TimePass.tracking(t, com.myriam.unrp.pass.TimePass.hold(p, t, 15, true)); // TM_HOLD (frappe) puis TM_STRIKE
                com.myriam.unrp.pass.TimePass.strike(p, lv, t, p.position(), new Vec3(behind.x, t.getY(), behind.z));
                p.teleportTo(behind.x, t.getY(), behind.z);
                t.hurt(lv.damageSources().playerAttack(p), (float) dmg(st, 8));
                push(t, t.getDeltaMovement().add(0, 0.3, 0));
                return true;
            }
            case "tm_delayed" -> { // Delayed Strike : la cible (cône, 12 blocs) est marquée ; 3 s plus tard le coup tombe
                LivingEntity t = cone(p, 12);
                if (t == null) {
                    msg(p, "Aucune cible devant toi.");
                    return false;
                }
                DELAYED.add(new Delayed(p.getUUID(), t, now + 60, dmg(st, 11)));
                t.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
                com.myriam.unrp.pass.TimePass.tracking(t, com.myriam.unrp.pass.TimePass.delay(p, t, 59, false)); // TM_DELAY
                msg(p, "Coup différé : 3 s");
                return true;
            }
            default -> {
                return true; // interrupteurs gérés par whileOn (heal_self, martyr, water_form, nm_field, tm_flow)
            }
        }
    }

    private static LivingEntity cone(ServerPlayer p, double r) {
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f);
        LivingEntity best = null;
        double bd = r;
        for (LivingEntity e : p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r), e -> e != p && e.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            if (to.length() < bd && to.normalize().dot(look) > 0.88) {
                bd = to.length();
                best = e;
            }
        }
        return best;
    }

    private static void freeze(LivingEntity e, long until, boolean ice) {
        FROZEN.removeIf(f -> f.e == e);
        Frozen f = new Frozen();
        f.e = e;
        f.at = e.position();
        f.until = until;
        f.ice = ice;
        FROZEN.add(f);
    }

    /** Peur (Nightmare) : lenteur et obscurité ; niveau 2 : + cécité ; niveau 3 : + faiblesse. Les plus puissants résistent mieux. */
    private static void fear(ServerPlayer p, LivingEntity t, int level, int[] st) {
        int d = (int) (secs(st, 3 + level) * 20);
        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, d, level - 1));
        t.addEffect(new MobEffectInstance(MobEffects.DARKNESS, d, 0));
        if (level >= 2) t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, d / 2, 0));
        if (level >= 3) t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, d, 0));
        if (t instanceof ServerPlayer sp) whisper(sp);
    }

    private static final String[] WHISPERS = {"C'est derrière toi.", "Ne lève pas les yeux.", "Tu ne peux pas te réveiller.",
            "Ça connaît ton nom.", "Ne bouge pas.", "On te voit.", "Cours.", ":)"};

    private static void whisper(ServerPlayer t) {
        t.displayClientMessage(Component.literal("§8§o" + WHISPERS[t.getRandom().nextInt(WHISPERS.length)]), true);
    }

    // ------------------------------------------------------------------ interrupteurs actifs (appelé toutes les 4 ticks)
    public static void whileOn(ServerPlayer p, AbilityPower power, AbilityPower.Move m, int[] st, int stance, long now) {
        ServerLevel lv = p.serverLevel();
        int c = col(power);
        if (Specials2.handles(m.special)) {
            Specials2.run(p, power, m, st); // interrupteurs du lot 3 : maintenus en vie
            return;
        }
        if (Specials3.handles(m.special)) {
            Specials3.whileOn(p, m, now);
            return;
        }
        if (Specials4.handles(m.special)) {
            Specials4.whileOn(p, m, now);
            return;
        }
        if (Specials6.handles(m.special)) {
            Specials6.whileOn(p, m, now);
            return;
        }
        if (Specials7.handles(m.special)) {
            Specials7.whileOn(p, m, now);
            return;
        }
        if (Specials8.handles(m.special)) {
            Specials8.whileOn(p, m, now);
            return;
        }
        if (Specials9.handles(m.special)) {
            Specials9.whileOn(p, m, now);
            return;
        }
        if (Specials10.handles(m.special)) {
            Specials10.whileOn(p, m, now);
            return;
        }
        switch (m.special) {
            case "martyr" -> MARTYR.put(p.getUUID(), now);
            case "water_form" -> {
                WATER_FORM.put(p.getUUID(), now);
                Fx.sprite(lv, "drop", c, 0.9f, p.position().add(0, 0.2, 0), 3, 0.3, 0.02);
            }
            case "nm_field" -> { // Terror Field : terreur légère pour ce qui est à 8 blocs, murmures
                if (now % 20 >= 4) return;
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(8), e -> e != p && e.isAlive())) {
                    if (ally(p, e)) continue;
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0));
                    if (e instanceof ServerPlayer sp && p.getRandom().nextInt(3) == 0) whisper(sp);
                }
                Fx.wave(lv, p.position(), 8, c, 16);
            }
            case "tm_flow" -> { // Time Flow : posture utilitaire = tu accélères ; posture offensive = tout ralentit autour de toi
                // anim_clock / anim_counter (tags tm_accel / tm_decel du modèle) et TM_FLOW, recalé toutes les 20 ticks
                com.myriam.unrp.pass.PassSync.key(p, "anim_clock", stance == 0);
                com.myriam.unrp.pass.PassSync.key(p, "anim_counter", stance != 0);
                if (now % 20 == 0) com.myriam.unrp.pass.TimePass.flow(p, lv, stance != 0, stance != 0 ? 8.0 : 0.0, true);
                if (now % 20 >= 4) return;
                if (stance == 0) {
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, false, false));
                    p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 30, 1, false, false));
                    p.addEffect(new MobEffectInstance(MobEffects.JUMP, 30, 0, false, false));
                } else {
                    for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(8), e -> e != p && e.isAlive())) {
                        if (ally(p, e)) continue;
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1));
                        e.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30, 1));
                        e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 0));
                    }
                }
            }
            default -> { }
        }
    }

    // ------------------------------------------------------------------ chaque tick
    public static void tick(MinecraftServer server) {
        Specials2.tick(server);
        Specials3.tick(server);
        Specials4.tick(server);
        Specials5.tick(server);
        Specials6.tick(server);
        Specials7.tick(server);
        Specials8.tick(server);
        Specials9.tick(server);
        Specials10.tick(server);
        Specials17.tick(server);
        Alignment.tick(server);
        com.myriam.unrp.item.Phylactery.tick(server);
        Anim.tick(server);
        long now = server.overworld().getGameTime();
        // historique des positions (Temporal Rewind) : 3 s
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ArrayDeque<Snap> h = HISTORY.computeIfAbsent(p.getUUID(), k -> new ArrayDeque<>());
            h.addLast(new Snap(p.position(), p.getHealth()));
            while (h.size() > 60) h.removeFirst();
        }
        // objets tenus par télékinésie
        Iterator<Map.Entry<UUID, Lift>> li = LIFTS.entrySet().iterator();
        while (li.hasNext()) {
            var en = li.next();
            ServerPlayer p = server.getPlayerList().getPlayer(en.getKey());
            Lift l = en.getValue();
            if (p == null || !l.target.isAlive() || now >= l.until || !AbilityEngine.isOn(p, "telekinesis")) {
                l.target.setNoGravity(false);
                li.remove();
                continue;
            }
            Vec3 want = p.getEyePosition().add(p.getViewVector(1f).scale(4));
            Vec3 pos = l.target instanceof LivingEntity le ? le.getBoundingBox().getCenter() : l.target.position();
            push(l.target, want.subtract(pos).scale(0.35));
            if (l.target instanceof FallingBlockEntity fb) fb.time = 1; // ne retombe pas en bloc tant qu'il est tenu
        }
        // orbites
        Iterator<Map.Entry<UUID, Orbit>> oi = ORBITS.entrySet().iterator();
        while (oi.hasNext()) {
            var en = oi.next();
            ServerPlayer p = server.getPlayerList().getPlayer(en.getKey());
            if (p == null || now >= en.getValue().until || !AbilityEngine.isOn(p, "telekinesis")) {
                if (p != null) orbitResist(p, false);
                oi.remove();
                continue;
            }
            orbitResist(p, true);
            int c = 0xB07CFF;
            for (int k = 0; k < 3; k++) {
                double a = now * 0.25 + k * Math.PI * 2 / 3;
                Vec3 at = p.position().add(Math.cos(a) * 1.6, 1.1 + 0.3 * Math.sin(now * 0.1 + k), Math.sin(a) * 1.6);
                if (now % 2 == 0) Styles.moving(p.serverLevel(), "rock", c, 1.2f, 4, at, 0, 0, 0);
                for (LivingEntity e : p.serverLevel().getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(0.8), e -> e != p && e.isAlive())) {
                    Vec3 away = e.position().subtract(p.position()).multiply(1, 0, 1);
                    if (away.lengthSqr() > 1e-4) push(e, away.normalize().scale(0.7).add(0, 0.3, 0));
                    e.hurt(p.serverLevel().damageSources().indirectMagic(p, p), 2f);
                }
            }
        }
        // figés (glace ou temps)
        Iterator<Frozen> fi = FROZEN.iterator();
        while (fi.hasNext()) {
            Frozen f = fi.next();
            if (!f.e.isAlive() || now >= f.until) {
                if (f.e.isAlive() && !f.ice) com.myriam.unrp.pass.TimePass.free(f.e); // TM_FREE
                if (f.e.isAlive() && f.ice) {
                    ServerLevel lv = (ServerLevel) f.e.level();
                    lv.playSound(null, f.e.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.9f, 1.1f);
                    Fx.sprite(lv, "shard", 0xBFEFFF, 1.2f, f.e.getBoundingBox().getCenter(), 20, 0.5, 0.1);
                }
                fi.remove();
                continue;
            }
            f.e.teleportTo(f.at.x, f.at.y, f.at.z);
            f.e.setDeltaMovement(Vec3.ZERO);
            f.e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 10, false, false));
            f.e.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 10, 3, false, false));
            if (f.ice && now % 4 == 0) Fx.sprite((ServerLevel) f.e.level(), "shard", 0xBFEFFF, 1.0f,
                    f.e.getBoundingBox().getCenter(), 2, 0.4, 0.0);
        }
        // coups différés
        Iterator<Delayed> di = DELAYED.iterator();
        while (di.hasNext()) {
            Delayed d = di.next();
            if (now < d.at) continue;
            di.remove();
            if (!d.target.isAlive()) continue;
            ServerPlayer p = server.getPlayerList().getPlayer(d.caster);
            ServerLevel lv = (ServerLevel) d.target.level();
            d.target.hurt(p != null ? lv.damageSources().playerAttack(p) : lv.damageSources().magic(), (float) d.dmg);
            d.target.setDeltaMovement(d.target.getDeltaMovement().add(0, 0.7, 0));
            d.target.hurtMarked = true;
            com.myriam.unrp.pass.TimePass.landed(p, d.target); // TM_DELAY_HIT
        }
        MARTYR.entrySet().removeIf(e -> now - e.getValue() > 10);
        WATER_FORM.entrySet().removeIf(e -> now - e.getValue() > 10);
    }

    // ------------------------------------------------------------------ dégâts reçus
    public static void onHurt(LivingHurtEvent e) {
        LivingEntity v = e.getEntity();
        if (v.level().isClientSide) return;
        Specials2.onHurt(e);
        Specials3.onHurt(e);
        Specials4.onHurt(e);
        Specials5.onHurt(e);
        Specials6.onHurt(e);
        Specials7.onHurt(e);
        Specials8.onHurt(e);
        Specials9.onHurt(e);
        Specials17.onHurt(e);
        com.myriam.unrp.item.Phylactery.onHurt(e);
        // Water Form : les coups au corps à corps font moitié moins mal
        if (WATER_FORM.containsKey(v.getUUID()) && e.getSource().getDirectEntity() instanceof LivingEntity) e.setAmount(e.getAmount() * 0.5f);
        // Martyr : un allié à 8 blocs ne garde que 70 % des dégâts, tu prends les 30 % restants
        if (v instanceof ServerPlayer vp && !MARTYR.containsKey(v.getUUID())) {
            for (UUID mid : MARTYR.keySet()) {
                ServerPlayer m = vp.server.getPlayerList().getPlayer(mid);
                if (m == null || m.level() != v.level() || m.distanceToSqr(v) > 64 || !SocialService.allies(m.server, mid, vp.getUUID())) continue;
                float share = e.getAmount() * 0.3f;
                e.setAmount(e.getAmount() * 0.7f);
                m.hurt(m.damageSources().magic(), share);
                Fx.line(m.serverLevel(), m.getBoundingBox().getCenter(), v.getBoundingBox().getCenter(), 0x7CFFB2, Styles.LINE_WAVY, 0.08f, 6);
                break;
            }
        }
    }
}
