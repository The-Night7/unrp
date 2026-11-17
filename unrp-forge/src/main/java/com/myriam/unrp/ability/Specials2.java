package com.myriam.unrp.ability;

import com.myriam.unrp.data.AbilityPower;
import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.data.Models;
import com.myriam.unrp.data.UnodexData;
import com.myriam.unrp.player.PlayerStateService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Specials2 {
    private Specials2() {}

    private static final Set<String> ALL = Set.of(
            "gauge_touch", "gauge_scope",
            "ar_assault", "ar_guard", "ar_leap", "ar_rush",
            "arcane_charge", "arcane_volley",
            "suit_brace", "suit_crash", "suit_shed", "suit_volley",
            "am_copy", "am_expel",
            "storm_field", "storm_guard", "storm_flight", "storm_lance",
            "bm_blade", "bm_ranged", "bm_arc", "bm_manifest", "bm_domain",
            "cr_catalyze", "cr_clean", "cr_stasis", "cr_imbue");

    public static boolean handles(String s) {
        return s != null && ALL.contains(s);
    }

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

    private static void push(LivingEntity e, Vec3 v) {
        e.setDeltaMovement(v);
        e.hurtMarked = true;
    }

    private static List<LivingEntity> cone(ServerPlayer p, double r, double dot) {
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f);
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r), e -> e != p && e.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            if (to.length() <= r && to.normalize().dot(look) >= dot) out.add(e);
        }
        return out;
    }

    private static void attr(LivingEntity e, net.minecraft.world.entity.ai.attributes.Attribute a, String key, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = e.getAttribute(a);
        if (inst == null) return;
        UUID id = UUID.nameUUIDFromBytes(("unrp:sp2:" + key).getBytes(StandardCharsets.UTF_8));
        if (inst.getModifier(id) != null) inst.removeModifier(id);
        if (amount != 0) inst.addTransientModifier(new AttributeModifier(id, "unrp " + key, amount, op));
    }

    private static final Map<UUID, Long> ARCANE_CHARGE = new HashMap<>();
    private static final Map<UUID, Long> GUARD_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> BRACE_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> WIND_GUARD = new HashMap<>();
    private static final Map<UUID, Long> STORM = new HashMap<>();
    private static final Map<UUID, Long> FLIGHT = new HashMap<>();        // Azure Storm : vol
    private static final Map<UUID, String> BLADES = new HashMap<>();      // Blademaster : "dual" ou "great"
    private static final Map<UUID, Long> SCOPE = new HashMap<>();         // Ability Gauging : lunette d'aura
    private static final Map<UUID, Long> STRIPPED = new HashMap<>();

    private record MutDef(String id, String name, int weight, int kind, MobEffect effect, int effAmp, int effTicks) {}
    private record Mut(MutDef def, long until, int amp) {}
    private static final Map<UUID, List<Mut>> MUTS = new HashMap<>();    // Catalyst Ray : mutations
    private record Rain(UUID caster, Vec3 at, long until, int[] st, int col) {}
    private static final List<Rain> DOMAINS = new ArrayList<>();          // Thousand Blade Domain

    public static void onLogout(UUID id) {
        for (Map<UUID, ?> m : List.of(ARCANE_CHARGE, GUARD_UNTIL, BRACE_UNTIL, WIND_GUARD, STORM, FLIGHT, BLADES, SCOPE)) m.remove(id);
    }

    public static void stop(ServerPlayer p, String special) {
        if ("gauge_scope".equals(special)) SCOPE.remove(p.getUUID());
        if ("storm_field".equals(special)) STORM.remove(p.getUUID());
        if ("storm_flight".equals(special)) endFlight(p);
        if ("arcane_charge".equals(special)) release(p);
    }

    // ------------------------------------------------------------------ utilisations
    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m, int[] st) {
        ServerLevel lv = p.serverLevel();
        long now = lv.getGameTime();
        int c = col(power);
        boolean sneak = p.isShiftKeyDown();
        switch (m.special) {
            // ---------------- Ability Gauging
            case "gauge_touch" -> {
                LivingEntity t = Specials.aimed(p, 4.5);
                if (t == null) {
                    msg(p, "[Analysis] No target in range.");
                    return false;
                }
                scanReport(p, t, true);
                return true;
            }
            case "gauge_scope" -> {
                SCOPE.put(p.getUUID(), now);
                return true;
            }
            // ---------------- Arachnid
            case "ar_assault" -> {
                List<LivingEntity> ts = cone(p, 3.8, 0.35);
                if (ts.isEmpty()) {
                    msg(p, "No target ahead.");
                    return false;
                }
                for (LivingEntity e : ts) {
                    e.hurt(lv.damageSources().playerAttack(p), (float) dmg(st, 5));
                    Vec3 a = e.position().subtract(p.position()).multiply(1, 0, 1).normalize();
                    push(e, new Vec3(a.x * 0.8, 0.2, a.z * 0.8));
                    Styles.impact(lv, power.look, c, e.getBoundingBox().getCenter(), 6);
                }
                return true;
            }
            case "ar_guard" -> {
                GUARD_UNTIL.put(p.getUUID(), now + (long) (secs(st, 6) * 20));
                msg(p, "Pattes en garde");
                return true;
            }
            case "ar_leap" -> { // Arachnid Leap : grand bond
                Vec3 l = p.getViewVector(1f);
                p.setDeltaMovement(l.x * 1.4, Math.max(0.6, l.y + 0.7), l.z * 1.4);
                p.hurtMarked = true;
                return true;
            }
            case "ar_rush" -> {
                Vec3 l = p.getViewVector(1f).multiply(1, 0, 1).normalize();
                p.setDeltaMovement(l.x * 1.6, 0.1, l.z * 1.6);
                p.hurtMarked = true;
                for (LivingEntity e : cone(p, 5, 0.6)) {
                    e.hurt(lv.damageSources().playerAttack(p), (float) dmg(st, 7));
                    push(e, new Vec3(l.x * 0.9, 0.45, l.z * 0.9));
                    Styles.impact(lv, power.look, c, e.getBoundingBox().getCenter(), 8);
                }
                return true;
            }
            // ---------------- Arcane Shot
            case "arcane_charge" -> {
                ARCANE_CHARGE.putIfAbsent(p.getUUID(), now);
                long held = now - ARCANE_CHARGE.get(p.getUUID());
                Fx.sprite(lv, "flare", c, (float) (0.6 + Math.min(1, held / 50.0) * 1.6), p.getEyePosition().add(p.getViewVector(1f).scale(0.8)), 1, 0, 0);
                return true;
            }
            case "arcane_volley" -> {
                for (int k = -3; k <= 3; k++) {
                    Vec3 dir = rotY(p.getViewVector(1f), Math.toRadians(k * 9));
                    shoot(p, power, st, dir, 3.5, 1.6, 18);
                }
                return true;
            }
            // ---------------- Armor Suit
            case "suit_brace" -> {
                BRACE_UNTIL.put(p.getUUID(), now + 60);
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                msg(p, "Braced");
                return true;
            }
            case "suit_crash" -> {
                Vec3 l = p.getViewVector(1f).multiply(1, 0, 1).normalize();
                p.setDeltaMovement(l.x * 0.85, 0.1, l.z * 0.85);
                p.hurtMarked = true;
                int n = 0;
                for (LivingEntity e : cone(p, 3.5, 0.5)) {
                    e.hurt(lv.damageSources().playerAttack(p), (float) dmg(st, 3));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                    push(e, new Vec3(l.x * 0.6, 0.15, l.z * 0.6));
                    n++;
                }
                if (n > 0) msg(p, "Stunned!");
                return true;
            }
            case "suit_shed" -> {
                for (LivingEntity e : p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(3.5), e -> e != p && e.isAlive())) {
                    e.hurt(lv.damageSources().playerAttack(p), (float) dmg(st, 5));
                    Vec3 a = e.position().subtract(p.position()).multiply(1, 0, 1).normalize();
                    push(e, new Vec3(a.x * 0.9, 0.3, a.z * 0.9));
                }
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1));
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1));
                Fx.sprite(lv, "shard", c, 1.4f, p.getBoundingBox().getCenter(), 30, 0.8, 0.15);
                Fx.wave(lv, p.position(), 3.5, c, 10);
                msg(p, "Plates launched and reforged");
                return true;
            }
            case "suit_volley" -> {
                for (int k = -2; k <= 2; k++) shoot(p, power, st, rotY(p.getViewVector(1f), Math.toRadians(k * 6.5)), 4, 1.25, 20);
                msg(p, "Plates launched");
                return true;
            }
            // ---------------- Aura Manipulation
            case "am_copy" -> {
                com.myriam.unrp.am.AmWheelBridge.open(p);
                return true;
            }
            case "am_expel" -> {
                AbilityEngine.setCopy(p, null, 0);
                com.myriam.unrp.pass.AmPassLite.expel(p); // AM_EXPEL
                msg(p, "Powers expelled");
                return true;
            }
            // ---------------- Azure Storm
            case "storm_field" -> {
                STORM.put(p.getUUID(), now);
                return true;
            }
            case "storm_guard" -> {
                WIND_GUARD.put(p.getUUID(), now + (long) (secs(st, 6) * 20));
                msg(p, "Wind Guard");
                return true;
            }
            case "storm_flight" -> { // Storm Flight (interrupteur) : tu voles
                Pal.flight(p, STORM_FLIGHT_ID, "storm_flight", 0.7, true); // azure_storm.json storm_flight
                FLIGHT.put(p.getUUID(), now);
                return true;
            }
            case "storm_lance" -> {
                Vec3 eye = p.getEyePosition(), l = p.getViewVector(1f);
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(eye, eye.add(l.scale(20))).inflate(1.2), e -> e != p && e.isAlive())) {
                    Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
                    double t = to.dot(l);
                    if (t < 0 || t > 20 || eye.add(l.scale(t)).distanceTo(e.getBoundingBox().getCenter()) > 1.4) continue;
                    e.hurt(lv.damageSources().indirectMagic(p, p), (float) dmg(st, 7));
                    push(e, new Vec3(l.x * 0.4, 0.9, l.z * 0.4));
                }
                Fx.line(lv, eye.add(0, -0.2, 0), eye.add(l.scale(20)), c, Styles.LINE_HELIX, 0.2f, 10);
                return true;
            }
            // ---------------- Blademaster
            case "bm_blade" -> {
                String cur = BLADES.get(p.getUUID());
                String want = sneak ? "great" : "dual";
                clearBlades(p);
                if (want.equals(cur)) {
                    msg(p, "The blades dissipate");
                    return true;
                }
                BLADES.put(p.getUUID(), want);
                if (want.equals("dual")) {
                    attr(p, Attributes.ATTACK_DAMAGE, "bm_dmg", 3.5, AttributeModifier.Operation.ADDITION);
                    attr(p, Attributes.ATTACK_SPEED, "bm_spd", 0.4, AttributeModifier.Operation.ADDITION);
                    msg(p, "Twin katanas manifested");
                } else {
                    attr(p, Attributes.ATTACK_DAMAGE, "bm_dmg", 8, AttributeModifier.Operation.ADDITION);
                    attr(p, Attributes.ATTACK_SPEED, "bm_spd", -0.35, AttributeModifier.Operation.ADDITION);
                    attr(p, net.minecraftforge.common.ForgeMod.ENTITY_REACH.get(), "bm_reach", 2.0, AttributeModifier.Operation.ADDITION);
                    msg(p, "Great blade manifested");
                }
                Fx.sprite(lv, "glint", c, 1.4f, p.getBoundingBox().getCenter(), 14, 0.5, 0.05);
                return true;
            }
            case "bm_ranged" -> {
                for (int k = -1; k <= 1; k++) shoot(p, power, st, rotY(p.getViewVector(1f), Math.toRadians(k * 8)), 5, 1.8, 16);
                return true;
            }
            case "bm_arc" -> {
                for (LivingEntity e : cone(p, 10, 0.55)) {
                    e.hurt(lv.damageSources().playerAttack(p), (float) dmg(st, 8));
                    Styles.impact(lv, power.look, c, e.getBoundingBox().getCenter(), 6);
                }
                Vec3 eye = p.getEyePosition(), l = p.getViewVector(1f);
                Vec3 side = new Vec3(-l.z, 0, l.x).normalize();
                for (int k = -4; k < 4; k++) {
                    Vec3 a = eye.add(l.scale(3)).add(side.scale(k * 0.6)), b = eye.add(l.scale(3)).add(side.scale((k + 1) * 0.6));
                    Fx.line(lv, a, b.add(l.scale(0.4 * (4 - Math.abs(k)))), c, 0, 0.18f, 6);
                }
                return true;
            }
            case "bm_manifest" -> {
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(4), e -> e != p && e.isAlive())) {
                    e.hurt(lv.damageSources().playerAttack(p), (float) dmg(st, 6));
                    Fx.line(lv, e.getEyePosition().add(0, 2, 0), e.getBoundingBox().getCenter(), c, 0, 0.12f, 6);
                }
                Fx.wave(lv, p.position(), 4, c, 10);
                return true;
            }
            case "bm_domain" -> {
                Vec3 eye = p.getEyePosition();
                var bh = lv.clip(new net.minecraft.world.level.ClipContext(eye, eye.add(p.getViewVector(1f).scale(24)),
                        net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, p));
                DOMAINS.add(new Rain(p.getUUID(), bh.getLocation(), now + 100, st, c));
                msg(p, "Domain of a Thousand Blades");
                return true;
            }
            // ---------------- Catalyst Ray
            case "cr_catalyze" -> {
                LivingEntity t = sneak ? p : Specials.aimed(p, 16);
                if (t == null) {
                    msg(p, "No target.");
                    return false;
                }
                mutate(p, t, st, now, sneak && t == p);
                if (t != p) Fx.line(lv, p.getEyePosition().add(0, -0.2, 0), t.getBoundingBox().getCenter(), c, Styles.LINE_WAVY, 0.1f, 6);
                return true;
            }
            case "cr_clean" -> {
                LivingEntity t = sneak ? p : Specials.aimed(p, 12);
                if (t == null) {
                    msg(p, "No target.");
                    return false;
                }
                MUTS.remove(t.getUUID());
                t.removeAllEffects();
                Fx.sprite(lv, "mote", c, 1.2f, t.getBoundingBox().getCenter(), 18, 0.5, 0.03);
                msg(p, "Table rase");
                return true;
            }
            case "cr_stasis" -> {
                LivingEntity t = Specials.aimed(p, 28);
                if (!(t instanceof ServerPlayer tp)) {
                    msg(p, "Stasis does not affect this target.");
                    return false;
                }
                AbilityEngine.disable(tp, (int) (secs(st, 10) * 20));
                msg(p, "Genetic Stasis: ability suspended");
                msg(tp, "Your power is suspended!");
                return true;
            }
            case "cr_imbue" -> {
                List<Mut> ms = MUTS.get(p.getUUID());
                if (ms == null || ms.size() != 1) {
                    msg(p, "Exactly one mutation must be active.");
                    return false;
                }
                Mut mu = ms.get(0);
                ms.set(0, new Mut(mu.def, now + 1200, mu.amp + 1));
                msg(p, "Mutation enhanced: " + mu.def.name);
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    private static Vec3 rotY(Vec3 v, double a) {
        double c = Math.cos(a), s = Math.sin(a);
        return new Vec3(v.x * c - v.z * s, v.y, v.x * s + v.z * c);
    }

    private static void shoot(ServerPlayer p, AbilityPower power, int[] st, Vec3 dir, double base, double speed, int life) {
        ServerLevel lv = p.serverLevel();
        Vec3 pos = p.getEyePosition().add(dir.scale(0.6)).add(0, -0.15, 0);
        for (int i = 0; i < life; i++) {
            Vec3 next = pos.add(dir.scale(speed));
            var bh = lv.clip(new net.minecraft.world.level.ClipContext(pos, next, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, p));
            Vec3 end = bh.getType() == net.minecraft.world.phys.HitResult.Type.MISS ? next : bh.getLocation();
            for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(pos, end).inflate(0.5), e -> e != p && e.isAlive())) {
                if (e.getBoundingBox().inflate(0.3).clip(pos, end).isEmpty()) continue;
                Fx.line(lv, p.getEyePosition().add(0, -0.15, 0), e.getBoundingBox().getCenter(), col(power), Styles.of(power.look).line(), 0.07f, 5);
                e.hurt(lv.damageSources().indirectMagic(p, p), (float) dmg(st, base));
                if (AbilityEngine.holds(p, "armor_suit")) {
                    attr(e, Attributes.ARMOR, "strip_" + e.getUUID(), -2, AttributeModifier.Operation.ADDITION);
                    STRIPPED.put(e.getUUID(), lv.getGameTime() + 160);
                }
                Styles.impact(lv, power.look, col(power), e.getBoundingBox().getCenter(), 5);
                return;
            }
            if (end != next) {
                Fx.line(lv, p.getEyePosition().add(0, -0.15, 0), end, col(power), Styles.of(power.look).line(), 0.07f, 5);
                return;
            }
            pos = next;
        }
        Fx.line(lv, p.getEyePosition().add(0, -0.15, 0), pos, col(power), Styles.of(power.look).line(), 0.07f, 5);
    }

    private static void release(ServerPlayer p) {
        Long start = ARCANE_CHARGE.remove(p.getUUID());
        if (start == null) return;
        AbilityPower power = AbilityEngine.currentPower(p);
        int[] st = AbilityEngine.currentStats(p);
        if (power == null || st == null) return;
        double k = Math.min(1, (p.serverLevel().getGameTime() - start) / 50.0);
        shoot(p, power, st, p.getViewVector(1f), 4 + 8 * k, 2.2, 24);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.5f);
    }

    private static void clearBlades(ServerPlayer p) {
        BLADES.remove(p.getUUID());
        attr(p, Attributes.ATTACK_DAMAGE, "bm_dmg", 0, AttributeModifier.Operation.ADDITION);
        attr(p, Attributes.ATTACK_SPEED, "bm_spd", 0, AttributeModifier.Operation.ADDITION);
        attr(p, net.minecraftforge.common.ForgeMod.ENTITY_REACH.get(), "bm_reach", 0, AttributeModifier.Operation.ADDITION);
    }

    private static final UUID STORM_FLIGHT_ID = UUID.fromString("2c6d8f13-7a4e-4b25-9e07-3d1b5f8a2c41");

    private static void endFlight(ServerPlayer p) {
        FLIGHT.remove(p.getUUID());
        Pal.flight(p, STORM_FLIGHT_ID, "storm_flight", 0.7, false);
    }

    private static final List<MutDef> POOL = List.of(
            new MutDef("swift", "vive", 10, 1, MobEffects.MOVEMENT_SPEED, 1, 60),
            new MutDef("haste", "haste", 10, 1, MobEffects.DIG_SPEED, 1, 60),
            new MutDef("regenerative", "regeneration", 9, 1, MobEffects.REGENERATION, 0, 60),
            new MutDef("resilient", "resistance", 9, 1, MobEffects.DAMAGE_RESISTANCE, 0, 60),
            new MutDef("amplified", "amplification", 8, 1, MobEffects.DAMAGE_BOOST, 0, 60),
            new MutDef("burning_touch", "burning touch", 7, 1, null, 0, 0),
            new MutDef("lifesteal", "lifesteal", 6, 1, null, 0, 0),
            new MutDef("shrouded", "veiled", 6, 1, MobEffects.INVISIBILITY, 0, 60),
            new MutDef("second_wind", "second souffle", 2, 1, null, 0, 0),
            new MutDef("sparkler", "sparkling", 8, 0, null, 0, 0),
            new MutDef("phasing_step", "phase step", 7, 0, null, 0, 0),
            new MutDef("prismatic", "prismatique", 7, 0, null, 0, 0),
            new MutDef("sluggish", "lourde", 10, -1, MobEffects.MOVEMENT_SLOWDOWN, 0, 60),
            new MutDef("frail", "fragile", 10, -1, MobEffects.WEAKNESS, 0, 60),
            new MutDef("dimmed", "obscurcie", 9, -1, MobEffects.BLINDNESS, 0, 60),
            new MutDef("withering", "withering", 8, -1, MobEffects.WITHER, 0, 60),
            new MutDef("queasy", "nauseating", 8, -1, MobEffects.CONFUSION, 0, 80),
            new MutDef("glass_bones", "os de verre", 7, -1, null, 0, 0),
            new MutDef("leaden", "weighted", 7, -1, MobEffects.DIG_SLOWDOWN, 1, 60),
            new MutDef("unstable", "instable", 5, -1, null, 0, 0));

    private static int effWeight(MutDef m, boolean boosted) {
        if (!boosted) return m.weight;
        if (m.kind > 0) return m.weight * 3;
        if (m.kind < 0) return Math.max(1, m.weight / 2);
        return m.weight;
    }

    private static int capacity(LivingEntity t) {
        if (!(t instanceof ServerPlayer sp)) return 3;
        String pid = AbilityEngine.basePowerId(sp);
        if (pid == null) return 3;
        if ("catalyst_ray".equals(pid)) return Integer.MAX_VALUE;
        double lvl = com.myriam.unrp.player.PlayerStateService.effectiveLevel(sp);
        return Math.max(3, (int) Math.max(1.1, lvl));
    }

    private static void mutate(/*@Nullable*/ ServerPlayer caster, LivingEntity t, int[] st, long now, boolean boosted) {
        List<Mut> ms = MUTS.computeIfAbsent(t.getUUID(), k -> new ArrayList<>());
        if (ms.size() >= capacity(t)) {
            overload(t);
            return;
        }
        int total = 0;
        for (MutDef d : POOL) total += effWeight(d, boosted);
        int roll = t.getRandom().nextInt(Math.max(1, total));
        MutDef pick = POOL.get(0);
        for (MutDef d : POOL) {
            roll -= effWeight(d, boosted);
            if (roll < 0) {
                pick = d;
                break;
            }
        }
        long dur = 400L + (long) st[3] * 60L;
        ms.add(new Mut(pick, now + dur, 0));
        if (t.level() instanceof ServerLevel l) Fx.sprite(l, "glint", 0xC9A0FF, 1.0f, t.getBoundingBox().getCenter(), 12, 0.5, 0.05);
        String tag = pick.kind > 0 ? "§a(+) " : pick.kind < 0 ? "§c(-) " : "§7(~) ";
        String line = tag + "Mutation " + pick.name;
        if (t instanceof ServerPlayer sp) sp.displayClientMessage(Component.literal(line), true);
        if (caster != null && caster != t) caster.displayClientMessage(Component.literal(line + " §7sur " + t.getName().getString()), true);
    }

    private static void overload(LivingEntity t) {
        MUTS.remove(t.getUUID());
        t.hurt(t.damageSources().magic(), 6);
        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        if (t instanceof ServerPlayer sp) {
            AbilityEngine.disable(sp, 200);
            sp.displayClientMessage(Component.literal("§5☠ Surcharge de mutations !"), true);
        }
    }

    private static boolean hasMut(UUID id, String mutId) {
        List<Mut> ms = MUTS.get(id);
        if (ms == null) return false;
        for (Mut m : ms) if (m.def.id.equals(mutId)) return true;
        return false;
    }

    private static void scanReport(ServerPlayer p, LivingEntity t, boolean chat) {
        if (t instanceof net.minecraft.world.entity.player.Player tp && tp.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(com.myriam.unrp.item.ModItems.JOKER_MASK.get())) {
            if (chat) {
                p.sendSystemMessage(Component.literal("§b§l  ANALYSE D'AURA §r§7- ???"));
                p.sendSystemMessage(Component.literal("§7  Power: §f?????§7   Rank: §f?.?§7   Level: §f?.?"));
                p.sendSystemMessage(Component.literal("§8  The mask reveals nothing."));
            } else {
                msg(p, "§b??? §7· ????? · ?.? · the mask reveals nothing");
            }
            return;
        }
        String name = t.getName().getString(), power = "None", tier = "-", level = "-", top = "-", aura = "-";
        if (t instanceof ServerPlayer tp) {
            ClaimData.Profile pr = ClaimData.get(p.server).profileOf(tp.getUUID()).orElse(null);
            if (pr != null) {
                Models.Character ch = ClaimData.Profile.CHARACTER.equals(pr.kind) ? UnodexData.get().characters.get(pr.characterId) : null;
                String aid = pr.abilityOf(ch);
                var ab = aid == null ? null : UnodexData.get().abilities.get(aid);
                power = ab != null ? ab.name : aid == null ? "None" : aid;
                if (ch != null) {
                    name = ch.name;
                    tier = ch.tier == null ? "-" : ch.tier;
                } else {
                    if (pr.ocName != null) name = pr.ocName;
                    tier = pr.tier == null ? "-" : pr.tier;
                }
                level = String.format(java.util.Locale.ROOT, "%.1f", PlayerStateService.effectiveLevel(tp));
                int[] s = PlayerStateService.stats(p.server, tp.getUUID(), pr, ch);
                String[] names = {"Power", "Defense", "Recovery", "Trick", "Speed"};
                int bi = 0;
                for (int i = 1; i < 5; i++) if (s[i] > s[bi]) bi = i;
                top = names[bi] + " " + s[bi];
                aura = Math.max(0, pr.aura) + " / " + PlayerStateService.maxAura(s);
            }
        }
        if (chat) {
            p.sendSystemMessage(Component.literal("§b§l  ANALYSE D'AURA §r§7- " + name));
            p.sendSystemMessage(Component.literal("§7  Power: §f" + power + "§7   Rank: §f" + tier + "§7   Level: §f" + level));
            p.sendSystemMessage(Component.literal("§7  Meilleure stat : §f" + top + "§7   Aura : §f" + aura));
        } else {
            msg(p, "§b" + name + " §7· " + power + " · " + tier + " · niv. " + level + " · aura " + aura);
        }
    }

    // ------------------------------------------------------------------ chaque tick
    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            UUID id = p.getUUID();

            Long sc = SCOPE.get(id);
            if (sc != null && now - sc > 10) SCOPE.remove(id);
            else if (sc != null && now % 20 == 0) {
                LivingEntity t = Specials.aimed(p, 30);
                if (t != null) scanReport(p, t, false);
            }

            Long sto = STORM.get(id);
            if (sto != null && now - sto > 10) STORM.remove(id);
            else if (sto != null && now % 4 == 0) {
                ServerLevel lv = p.serverLevel();
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(5), e -> e != p && e.isAlive())) {
                    Vec3 tan = new Vec3(-(e.getZ() - p.getZ()), 0, e.getX() - p.getX()).normalize().scale(0.35);
                    push(e, new Vec3(tan.x, 0.25, tan.z));
                    if (now % 20 == 0) e.hurt(lv.damageSources().indirectMagic(p, p), 1.5f);
                }
                for (int k = 0; k < 4; k++) {
                    double a = now * 0.4 + k * Math.PI / 2;
                    Styles.moving(lv, "streak", 0x7FC8FF, 1.4f, 6, p.position().add(Math.cos(a) * 3, 0.3 + k * 0.5, Math.sin(a) * 3),
                            -Math.sin(a) * 0.4, 0.05, Math.cos(a) * 0.4);
                }
            }
            // vol
            Long fl = FLIGHT.get(id);
            if (fl != null && now - fl > 10) endFlight(p);

            Long wg = WIND_GUARD.get(id);
            if (wg != null && now < wg) {
                for (Projectile pr : p.serverLevel().getEntitiesOfClass(Projectile.class, p.getBoundingBox().inflate(3))) {
                    if (pr.getOwner() == p) continue;
                    Vec3 away = pr.position().subtract(p.position()).normalize().scale(0.8);
                    pr.setDeltaMovement(away.add(0, 0.3, 0));
                    pr.hurtMarked = true;
                }
            } else if (wg != null) WIND_GUARD.remove(id);

            if (BLADES.containsKey(id) && !AbilityEngine.isOn(p, "blademaster")) clearBlades(p);
            if (now % 3 == 0) looks(p, now);
            if (sto == null && fl == null && !AbilityEngine.isOn(p, "azure_storm")) { FLIGHT.remove(id); }
        }

        STRIPPED.entrySet().removeIf(e -> {
            if (now < e.getValue()) return false;
            for (ServerLevel lv : server.getAllLevels()) {
                var ent = lv.getEntity(e.getKey());
                if (ent instanceof LivingEntity le) attr(le, Attributes.ARMOR, "strip_" + e.getKey(), 0, AttributeModifier.Operation.ADDITION);
            }
            return true;
        });

        for (Iterator<Map.Entry<UUID, List<Mut>>> it = MUTS.entrySet().iterator(); it.hasNext(); ) {
            var en = it.next();
            en.getValue().removeIf(m -> now >= m.until);
            if (en.getValue().isEmpty()) {
                it.remove();
                continue;
            }
            if (now % 40 != 0) continue;
            LivingEntity le = null;
            for (ServerLevel lv : server.getAllLevels()) {
                if (lv.getEntity(en.getKey()) instanceof LivingEntity f) {
                    le = f;
                    break;
                }
            }
            if (le == null || !(le.level() instanceof ServerLevel lv)) continue;
            boolean reroll = false;
            for (Mut m : List.copyOf(en.getValue())) {
                MutDef d = m.def;
                if (d.effect != null) {
                    le.addEffect(new MobEffectInstance(d.effect, d.effTicks, d.effAmp + m.amp, false, true));
                    continue;
                }
                switch (d.id) {
                    case "sparkler" -> Fx.sprite(lv, "glint", randomRgb(lv), 0.8f, le.getBoundingBox().getCenter().add(0, 0.2, 0), 6, 0.5, 0);
                    case "prismatic" -> Fx.sprite(lv, "mote", randomRgb(lv), 1.0f, le.getBoundingBox().getCenter(), 9, 0.5, 0);
                    case "phasing_step" -> {
                        if (lv.random.nextInt(5) != 0) break;
                        double nx = le.getX() + (lv.random.nextDouble() - 0.5) * 8, nz = le.getZ() + (lv.random.nextDouble() - 0.5) * 8;
                        if (!lv.noCollision(le, le.getBoundingBox().move(nx - le.getX(), 0, nz - le.getZ()))) break;
                        Fx.sprite(lv, "mote", 0xC9A0FF, 1.0f, le.getBoundingBox().getCenter(), 10, 0.3, 0);
                        le.teleportTo(nx, le.getY(), nz);
                        lv.playSound(null, nx, le.getY(), nz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.5f, 1.4f);
                    }
                    case "unstable" -> reroll |= lv.random.nextInt(4) == 0;
                    default -> { }
                }
            }
            if (reroll) {
                int[] ust = le instanceof ServerPlayer sp ? AbilityEngine.currentStats(sp) : null;
                mutate(null, le, ust != null ? ust : new int[5], now, false);
            }
        }

        for (Iterator<Rain> it = DOMAINS.iterator(); it.hasNext(); ) {
            Rain r = it.next();
            ServerPlayer p = server.getPlayerList().getPlayer(r.caster);
            if (p == null || now >= r.until) {
                it.remove();
                continue;
            }
            if (now % 3 != 0) continue;
            ServerLevel lv = p.serverLevel();
            var rnd = lv.random;
            Vec3 at = r.at.add((rnd.nextDouble() - 0.5) * 12, 0, (rnd.nextDouble() - 0.5) * 12);
            Fx.line(lv, at.add(0, 6, 0), at, r.col, 0, 0.12f, 5);
            for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(1.5, 3, 1.5), e -> e != p && e.isAlive())) {
                e.hurt(lv.damageSources().playerAttack(p), (float) dmg(r.st, 3));
            }
        }
    }

    private static Vec3 hand(ServerPlayer p, double side) {
        Vec3 look = p.getViewVector(1f);
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        return p.getEyePosition().add(right.scale(0.42 * side)).add(0, -0.55, 0).add(look.scale(0.35));
    }

    private static void blades(ServerPlayer p, boolean great, int rgb, float width) {
        ServerLevel lv = p.serverLevel();
        Vec3 look = p.getViewVector(1f);
        if (great) {
            Vec3 h = hand(p, 1);
            Fx.line(lv, h.add(look.scale(-0.2)), h.add(look.scale(0.5)).add(0, 1.9, 0), rgb, 0, width * 1.6f, 6);
        } else {
            for (double side : new double[] {-1, 1}) {
                Vec3 h = hand(p, side);
                Fx.line(lv, h, h.add(look.scale(0.8)).add(0, 0.35, 0), rgb, 0, width, 6);
            }
        }
    }

    private static void looks(ServerPlayer p, long now) {
        UUID id = p.getUUID();
        if (!AbilityEngine.isOn(id)) return;
        ServerLevel lv = p.serverLevel();
        for (String pid : AbilityEngine.activePowers(p)) {
        AbilityPower pw = com.myriam.unrp.data.UnodexData.get().powers.get(pid);
        if (pw == null) continue;
        int c = col(pw);
        String b = BLADES.get(id);
        if (b != null && "blademaster".equals(pid)) blades(p, "great".equals(b), c, 0.06f);
        switch (pid) {
            case "hydrofreeze" -> {
                if (AbilityEngine.toggled(p, "hydrofreeze", 4)) blades(p, false, 0xBFEFFF, 0.06f);
            }
            case "armor_suit" -> {
                for (int k = 0; k < 4; k++) {
                    double a = now * 0.12 + k * Math.PI / 2;
                    Vec3 at = p.position().add(Math.cos(a) * 0.62, 0.5 + (k % 2) * 0.55, Math.sin(a) * 0.62);
                    Fx.sprite(lv, "shard", c, 0.9f, at, 1, 0.03, 0);
                }
            }
            case "phase_shift" -> {
                boolean def = AbilityEngine.toggled(p, "phase_shift", 0), atk = AbilityEngine.toggled(p, "phase_shift", 1);
                if (def || atk) Fx.sprite(lv, "wisp", def ? 0x6FA8FF : 0xFF7A6F, 1.3f, p.position().add(0, 0.9, 0), 3, 0.35, 0.01);
            }
            default -> { }
        }
        }
    }

    private static int randomRgb(ServerLevel lv) {
        return java.awt.Color.HSBtoRGB(lv.random.nextFloat(), 0.6f, 1f) & 0xFFFFFF;
    }

    public static boolean onDeath(net.minecraftforge.event.entity.living.LivingDeathEvent e) {
        LivingEntity v = e.getEntity();
        List<Mut> ms = MUTS.get(v.getUUID());
        if (ms == null || v.level().isClientSide) return false;
        for (Mut m : ms) {
            if (!m.def.id.equals("second_wind")) continue;
            ms.remove(m);
            e.setCanceled(true);
            v.setHealth(v.getMaxHealth() * 0.5f);
            v.removeAllEffects();
            if (v.level() instanceof ServerLevel lv) {
                Fx.sprite(lv, "flare", 0xFFE08A, 1.4f, v.getBoundingBox().getCenter(), 30, 0.6, 0.1);
                lv.playSound(null, v.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8f, 1.4f);
            }
            if (v instanceof ServerPlayer sp) msg(sp, "§e✦ Second souffle !");
            return true;
        }
        return false;
    }

    public static void onHurt(LivingHurtEvent e) {
        LivingEntity v = e.getEntity();
        long now = v.level().getGameTime();
        if (hasMut(v.getUUID(), "glass_bones")) e.setAmount(e.getAmount() * 1.2f);
        if (e.getSource().getEntity() instanceof LivingEntity at && at != v) {
            if (hasMut(at.getUUID(), "burning_touch")) v.setSecondsOnFire(3);
            if (hasMut(at.getUUID(), "lifesteal")) at.heal(e.getAmount() * 0.25f);
        }
        if (GUARD_UNTIL.getOrDefault(v.getUUID(), 0L) > now) e.setAmount(e.getAmount() * 0.65f);
        if (BRACE_UNTIL.getOrDefault(v.getUUID(), 0L) > now) e.setAmount(e.getAmount() * 0.5f);
        if (v instanceof ServerPlayer p && ARCANE_CHARGE.remove(p.getUUID()) != null) msg(p, "§5Charge interrompue !");
    }

    public static void modelTags(ServerPlayer p) {
        long now = p.serverLevel().getGameTime();
        Long g = GUARD_UNTIL.get(p.getUUID());
        tag(p, "unrp.ar_guard_on", g != null && now < g);
        String b = BLADES.get(p.getUUID());
        tag(p, "unrp.bm.dual", "dual".equals(b));
        tag(p, "unrp.bm.great", "great".equals(b));
    }

    private static void tag(ServerPlayer p, String t, boolean on) {
        if (on) p.addTag(t);
        else p.removeTag(t);
    }
}
