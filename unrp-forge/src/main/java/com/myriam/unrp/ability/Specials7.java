package com.myriam.unrp.ability;

import com.myriam.unrp.data.AbilityPower;
import com.myriam.unrp.data.ClaimData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Pouvoirs quantiques (lot 7) : Explosion, Tremor, Illumination, Umbral Shift, Whirlwind. Comportements repris de
 * ExplosionEvents, TremorEvents, IlluminationEvents, UmbralShiftEvents et WhirlwindEvents. Fiches : docs/fidelity/.
 */
public final class Specials7 {
    private Specials7() {}

    private static final Set<String> ALL = Set.of("ex_detonate", "ex_shock", "ex_cluster",
            "tm_quake", "tm_fault", "tm_mag", "tm_burst",
            "il_light", "il_flash", "il_beacon", "il_pulse",
            "us_dive", "us_assault",
            "ww_blast", "ww_cyclone", "ww_flight", "ww_shield");

    public static boolean handles(String s) {
        return s != null && ALL.contains(s);
    }

    private static final Map<UUID, Long> DET_START = new HashMap<>(), DET_CD = new HashMap<>(), CLUSTER_AT = new HashMap<>();
    private static final Map<UUID, Long> QUAKE_START = new HashMap<>(), HIT_CD = new HashMap<>();
    private static final Map<UUID, double[]> FAULT = new HashMap<>(), FAULT_AT = new HashMap<>(), CATA = new HashMap<>();
    private static final Map<UUID, Long> BURST_AT = new HashMap<>(), CATA_CD = new HashMap<>();
    private static final Set<UUID> MAG = new HashSet<>();
    private static final Map<UUID, BlockPos> HAND_LIGHT = new HashMap<>();
    private static final Map<UUID, Integer> BEACON = new HashMap<>(), CYCLONE = new HashMap<>();
    private static final Map<UUID, Long> FLASH_LOCK = new HashMap<>();
    private static final Map<UUID, Double> PHASED = new HashMap<>();
    private static final Map<UUID, Long> EMERGED = new HashMap<>();
    private static final Set<UUID> FLYING = new HashSet<>();
    private static final Set<UUID> BUSY = new HashSet<>();

    public static void onLogout(UUID id) {
        for (Map<UUID, ?> m : List.of(DET_START, DET_CD, CLUSTER_AT, QUAKE_START, FAULT, FAULT_AT, CATA, BURST_AT, BEACON, CYCLONE, PHASED)) m.remove(id);
        MAG.remove(id);
        FLYING.remove(id);
    }

    // ------------------------------------------------------------------ outils
    private static float sf(ServerPlayer p) {
        double lvl = ClaimData.get(p.server).profileOf(p.getUUID()).map(pr -> pr.level).orElse(1.1);
        return (float) Math.max(0, Math.min(1, (lvl * 10 - 11) / 89.0));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static void msg(ServerPlayer p, String s) {
        p.displayClientMessage(Component.literal(s), true);
    }

    private static boolean has(ServerPlayer p, String power) {
        return AbilityEngine.isOn(p, power);
    }

    private static Vec3 flat(Vec3 look) {
        Vec3 f = new Vec3(look.x, 0, look.z);
        return f.lengthSqr() < 0.01 ? new Vec3(0, 0, 1) : f.normalize();
    }

    private static void hit(ServerPlayer p, LivingEntity t, float dmg) {
        BUSY.add(p.getUUID());
        t.hurt(p.serverLevel().damageSources().playerAttack(p), dmg);
        BUSY.remove(p.getUUID());
    }

    private static void dust(ServerLevel lv, Vec3 at, int count, double spread) {
        Fx.sprite(lv, "rock", 0x8A7A66, 1.0f, at, count, spread, 0.12);
    }

    private static boolean onGroundOrRefund(ServerPlayer p, int refund, String name) {
        if (p.onGround()) return true;
        msg(p, "§6✖ " + name + " requires solid ground beneath you.");
        return false;
    }

    private static void root(ServerPlayer p) {
        p.setDeltaMovement(0, Math.min(0, p.getDeltaMovement().y), 0);
        p.hurtMarked = true;
        p.setSprinting(false);
    }

    // ------------------------------------------------------------------ Explosion
    private static Vec3 rayTarget(ServerPlayer p, double max) {
        ServerLevel lv = p.serverLevel();
        Vec3 eye = p.getEyePosition(), end = eye.add(p.getViewVector(1f).scale(max));
        double best = max * max;
        Vec3 hitAt = null;
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().expandTowards(p.getViewVector(1f).scale(max)).inflate(1.0), e -> e != p && e.isAlive() && !e.isSpectator())) {
            var c = e.getBoundingBox().inflate(0.3).clip(eye, end);
            if (c.isPresent() && eye.distanceToSqr(c.get()) < best) {
                best = eye.distanceToSqr(c.get());
                hitAt = c.get();
            }
        }
        if (hitAt != null) return hitAt;
        var bh = lv.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        return bh.getType() != HitResult.Type.MISS ? bh.getLocation() : end;
    }

    private static boolean explosionImmune(LivingEntity e) {
        return e instanceof ServerPlayer sp && has(sp, "explosion");
    }

    private static void explode(ServerPlayer src, Vec3 center, double radius, float dmg, double kb) {
        ServerLevel lv = src.serverLevel();
        double r = radius * (1.0 + 0.4 * sf(src)), rSq = r * r;
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(center.subtract(r, r, r), center.add(r, r, r)), e -> e.isAlive() && e != src && e.distanceToSqr(center) <= rSq)) {
            if (explosionImmune(e)) continue;
            e.hurt(lv.damageSources().explosion(src, src), dmg);
            Vec3 push = e.position().subtract(center);
            push = push.lengthSqr() < 1e-4 ? new Vec3(0, 1, 0) : push.normalize();
            e.push(push.x * kb, push.y * kb * 0.5 + 0.3, push.z * kb);
            e.hurtMarked = true;
        }
        Fx.sprite(lv, "flare", 0xFFB347, (float) (1.2 + radius * 0.3), center, 12 + (int) (radius * 4), radius * 0.4, 0.1);
        Fx.wave(lv, center, r, 0xFFB347, 10);
        lv.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    // ------------------------------------------------------------------ utilisations
    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m) {
        ServerLevel lv = p.serverLevel();
        long now = lv.getGameTime();
        UUID id = p.getUUID();
        Vec3 look = p.getViewVector(1f);
        float t = sf(p);
        switch (m.special) {
            case "ex_detonate" -> {
                if (!DET_START.containsKey(id)) {
                    if (now < DET_CD.getOrDefault(id, 0L)) {
                        if (now % 20 < 4) msg(p, "§c✖ Detonation is cooling down (" + (1 + (DET_CD.get(id) - now) / 20) + " s)");
                        return false;
                    }
                    AbilityEngine.spendAura(p, 8);
                    DET_START.put(id, now);
                }
                double f = Math.min(1.0, (now - DET_START.get(id)) / 200.0);
                Fx.sprite(lv, "ember", 0xFFB347, (float) (0.4 + 0.5 * f), p.position().add(0, 1.1, 0), 3 + (int) (9 * f), 0.7 + 0.9 * f, 0.02);
            }
            case "ex_shock" -> explode(p, p.position(), 10.0, lerp(12f, 14f, t), lerp(2.0f, 4.5f, t));
            case "ex_cluster" -> {
                long last = CLUSTER_AT.getOrDefault(id, now - 12);
                if (now - last >= 12) {
                    if (AbilityEngine.aura(p) < 10) {
                        msg(p, "§c✖ Plus assez d'aura.");
                        return false;
                    }
                    CLUSTER_AT.put(id, now);
                    AbilityEngine.spendAura(p, 10);
                    explode(p, rayTarget(p, 10), 1.75, lerp(3f, 7f, t), lerp(1.4f, 2.8f, t));
                }
            }
            // ---------------- Tremor
            case "tm_quake" -> {
                if (!QUAKE_START.containsKey(id)) {
                    if (!p.onGround()) {
                        msg(p, "§6✖ Seismic attacks require solid ground beneath you.");
                        return false;
                    }
                    AbilityEngine.spendAura(p, 4);
                    QUAKE_START.put(id, now);
                }
                if (p.onGround()) {
                    double radius = MAG.contains(id) ? 18 : 11;
                    root(p);
                    long held = now - QUAKE_START.get(id);
                    if (held >= 8 && held % 8 < 4) seismic(p, p.position(), radius, MAG.contains(id) ? 2.5f : 1.5f, 0.25, 0, 12);
                    for (int k = 0; k < 3; k++) {
                        double a = p.getRandom().nextDouble() * Math.PI * 2, r = 1 + p.getRandom().nextDouble() * (radius - 1);
                        dust(lv, new Vec3(p.getX() + Math.cos(a) * r, p.getY(), p.getZ() + Math.sin(a) * r), 3, 0.3);
                    }
                }
            }
            case "tm_fault" -> {
                if (!onGroundOrRefund(p, 24, "Faille")) return false;
                Vec3 f = flat(look);
                FAULT_AT.put(id, new double[] {now + 4, f.x, f.z});
            }
            case "tm_mag" -> {
                if (MAG.remove(id)) {
                    msg(p, "§6Magnitude control released.");
                } else {
                    MAG.add(id);
                    msg(p, "§6Magnitude control engaged. §7Every hit is stronger.");
                }
            }
            case "tm_burst" -> {
                if (p.isShiftKeyDown()) { // Cataclysme
                    if (now < CATA_CD.getOrDefault(id, 0L)) {
                        msg(p, "§6✖ Cataclysm is cooling down (" + (1 + (CATA_CD.get(id) - now) / 20) + " s)");
                        return false;
                    }
                    if (!onGroundOrRefund(p, 34, "Cataclysme")) return false;
                    AbilityEngine.spendAura(p, 18);
                    CATA_CD.put(id, now + 420);
                    CATA.put(id, new double[] {now, p.getX(), p.getY(), p.getZ()});
                } else {
                    if (!onGroundOrRefund(p, 34, "Seismic Shard")) return false;
                    BURST_AT.put(id, now + 7);
                }
            }
            // ---------------- Illumination
            case "il_light" -> { }
            case "il_flash" -> {
                int blind = (int) (40 + 60 * t);
                Vec3 eye = p.getEyePosition(), dir = look.normalize();
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(eye.subtract(8, 8, 8), eye.add(8, 8, 8)), e -> e.isAlive() && e != p)) {
                    Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye);
                    if (to.length() > 8 || to.normalize().dot(dir) < 0.5) continue;
                    flash(e, blind);
                }
                for (double d = 1; d <= 8; d += 0.7) Fx.sprite(lv, "glint", 0xFFFFE0, 1.0f, eye.add(dir.scale(d)), 2, 0.12 + d * 0.16, 0.0);
            }
            case "il_beacon" -> BEACON.put(id, 200);
            case "il_pulse" -> {
                double radius = 4 + 3 * t;
                int blind = (int) (60 + 60 * t);
                Vec3 c = p.position().add(0, 1, 0);
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(c.subtract(radius, radius, radius), c.add(radius, radius, radius)), e -> e.isAlive() && e != p && e.distanceToSqr(c) <= radius * radius)) {
                    flash(e, blind);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, true));
                }
                Fx.wave(lv, c, radius, 0xFFFFE0, 12);
                Fx.sprite(lv, "glint", 0xFFFFE0, 1.4f, c, 40, radius * 0.5, 0.05);
            }
            // ---------------- Umbral Shift
            case "us_dive" -> {
                if (PHASED.containsKey(id)) emerge(p, lv, false);
                else if (!p.onGround() && !p.horizontalCollision) {
                    msg(p, "§5✖ Touch the ground or a wall before diving.");
                    return false;
                } else {
                    PHASED.put(id, 0.0);
                    p.setInvisible(true);
                    Fx.sprite(lv, "smoke", 0x2A1A4A, 1.4f, p.position(), 14, 0.5, 0.05);
                }
            }
            case "us_assault" -> {
                long since = now - EMERGED.getOrDefault(id, -1000L);
                boolean ambush = since >= 0 && since <= 40;
                float dmg = ambush ? 13f : 9f;
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(4.5), e -> e != p && e.isAlive())) {
                    hit(p, e, dmg);
                    Vec3 d = e.position().subtract(p.position());
                    Vec3 fl = new Vec3(d.x, 0, d.z);
                    fl = fl.lengthSqr() < 0.01 ? look : fl.normalize();
                    double k = ambush ? 1.4 : 1.0;
                    e.setDeltaMovement(fl.x * k, 0.4, fl.z * k);
                    e.hurtMarked = true;
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ambush ? 30 : 15, ambush ? 3 : 1, false, false));
                }
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, true));
                Fx.sprite(lv, "smoke", 0x2A1A4A, 1.6f, p.position(), 26, 1.2, 0.15);
                if (ambush) msg(p, "§5Shadow Eruption! §7Ambush strike.");
            }
            // ---------------- Whirlwind
            case "ww_blast" -> {
                Vec3 eye = p.getEyePosition(), dir = look.normalize();
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(9.5), e -> e != p && e.isAlive())) {
                    Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye);
                    double d = to.length();
                    if (d > 9.5 || d < 0.001 || dir.dot(to.normalize()) < 0.45) continue;
                    hit(p, e, 7f);
                    e.setDeltaMovement(dir.x * 2.5, 0.4, dir.z * 2.5);
                    e.hurtMarked = true;
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, true));
                }
                for (double d = 1; d <= 9.5; d += 0.8) Fx.sprite(lv, "wisp", 0xBFE8FF, 1.0f, eye.add(dir.scale(d)), 2, d * 0.18, 0.02);
                lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.6f);
            }
            case "ww_cyclone" -> CYCLONE.put(id, 20);
            case "ww_flight", "ww_shield" -> { }
            default -> { }
        }
        return true;
    }

    private static void flash(LivingEntity e, int blind) {
        long now = e.level().getGameTime();
        if (now < FLASH_LOCK.getOrDefault(e.getUUID(), 0L)) return;
        FLASH_LOCK.put(e.getUUID(), now + blind + 100);
        e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blind, 0, false, true));
    }

    private static void seismic(ServerPlayer p, Vec3 center, double radius, float dmg, double kb, int slowAmp, int slowTicks) {
        ServerLevel lv = p.serverLevel();
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(center.x - radius, center.y - 2, center.z - radius, center.x + radius, center.y + 2.5, center.z + radius), e -> e != p && e.isAlive() && e.onGround())) {
            Vec3 away = e.position().subtract(center);
            if (Math.sqrt(away.x * away.x + away.z * away.z) > radius) continue;
            hit(p, e, dmg);
            Vec3 dir = new Vec3(away.x, 0, away.z);
            dir = dir.lengthSqr() < 0.01 ? new Vec3(1, 0, 0) : dir.normalize();
            e.setDeltaMovement(e.getDeltaMovement().add(dir.x * kb, 0.28, dir.z * kb));
            e.hurtMarked = true;
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, slowAmp, false, false));
        }
    }

    private static boolean open(ServerLevel lv, BlockPos pos) {
        BlockState a = lv.getBlockState(pos), b = lv.getBlockState(pos.above());
        return a.getCollisionShape(lv, pos).isEmpty() && b.getCollisionShape(lv, pos.above()).isEmpty();
    }

    private static Vec3 findAir(ServerLevel lv, Vec3 from) {
        BlockPos base = BlockPos.containing(from);
        if (open(lv, base)) return from;
        for (int dy = 1; dy <= 16; dy++) if (open(lv, base.above(dy))) return new Vec3(from.x, base.getY() + dy + 0.05, from.z);
        for (int r = 1; r <= 6; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    for (int dy = -2; dy <= 4; dy++) {
                        BlockPos q = base.offset(dx, dy, dz);
                        if (open(lv, q)) return new Vec3(q.getX() + 0.5, q.getY() + 0.05, q.getZ() + 0.5);
                    }
                }
            }
        }
        for (int dy = 1; dy <= 24; dy++) {
            BlockPos d = base.below(dy);
            if (d.getY() <= lv.getMinBuildHeight() + 1) break;
            if (open(lv, d)) return new Vec3(from.x, d.getY() + 0.05, from.z);
        }
        return from;
    }

    private static void emerge(ServerPlayer p, ServerLevel lv, boolean forced) {
        PHASED.remove(p.getUUID());
        p.setInvisible(false);
        Vec3 spot = findAir(lv, p.position());
        p.teleportTo(spot.x, spot.y, spot.z);
        p.fallDistance = 0;
        EMERGED.put(p.getUUID(), lv.getGameTime());
        Fx.sprite(lv, "smoke", 0x2A1A4A, 1.4f, spot, 18, 0.6, 0.08);
        if (forced) msg(p, "§5Les ombres te recrachent.");
    }

    public static boolean isPhased(LivingEntity e) {
        return PHASED.containsKey(e.getUUID());
    }

    public static void whileOn(ServerPlayer p, AbilityPower.Move m, long now) {
        UUID id = p.getUUID();
        ServerLevel lv = p.serverLevel();
        switch (m.special) {
            case "il_light" -> {
                BlockPos at = BlockPos.containing(p.getX() + p.getViewVector(1f).x * 0.6, p.getY() + 1.0, p.getZ() + p.getViewVector(1f).z * 0.6);
                BlockPos prev = HAND_LIGHT.get(id);
                if (prev != null && prev.distSqr(at) <= 2.0) return;
                if (lv.getBlockState(at).isAir()) {
                    clearLight(lv, prev);
                    lv.setBlock(at, Blocks.LIGHT.defaultBlockState(), 3);
                    HAND_LIGHT.put(id, at);
                }
            }
            case "ww_flight" -> {
                if (!p.isCreative() && !p.isSpectator()) {
                    if (FLYING.add(id)) {
                        p.setDeltaMovement(p.getDeltaMovement().x, 0.4, p.getDeltaMovement().z);
                        p.hurtMarked = true;
                    }
                    Pal.flight(p, FLIGHT_ID, "wind_flight", 0.6, true);
                    p.fallDistance = 0;
                }
            }
            case "ww_shield" -> windShield(p, lv);
            default -> { }
        }
    }

    private static void clearLight(ServerLevel lv, BlockPos pos) {
        if (pos != null && lv.getBlockState(pos).is(Blocks.LIGHT)) lv.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    private static void windShield(ServerPlayer p, ServerLevel lv) {
        Vec3 center = p.position().add(0, p.getBbHeight() * 0.5, 0);
        double radius = 4.0;
        for (Entity pr : lv.getEntitiesOfClass(Entity.class, p.getBoundingBox().inflate(radius), e -> (e instanceof AbstractArrow || e instanceof ThrowableItemProjectile) && e.isAlive())) {
            if (pr instanceof AbstractArrow a && a.getOwner() == p) continue;
            if (pr instanceof ThrowableItemProjectile tp && tp.getOwner() == p) continue;
            Vec3 away = pr.position().subtract(center);
            double dist = away.length();
            if (dist > radius || dist < 0.001) continue;
            Vec3 vel = pr.getDeltaMovement();
            if (vel.dot(away) >= 0) continue;
            Vec3 n = away.normalize();
            pr.setDeltaMovement(vel.subtract(n.scale(vel.dot(n))).add(n.scale(0.35)));
            pr.hurtMarked = true;
            if (pr instanceof AbstractArrow a) a.setOwner(p);
        }
        if (p.tickCount % 3 == 0) {
            for (int i = 0; i < 8; i++) {
                double a = p.tickCount * 0.5 + i * Math.PI / 4;
                Fx.sprite(lv, "wisp", 0xBFE8FF, 0.8f, center.add(Math.cos(a) * 1.4, 0, Math.sin(a) * 1.4), 1, 0.05, 0);
            }
        }
    }

    public static void stop(ServerPlayer p, String special) {
        UUID id = p.getUUID();
        ServerLevel lv = p.serverLevel();
        long now = lv.getGameTime();
        switch (special) {
            case "ex_detonate" -> {
                Long st = DET_START.remove(id);
                if (st == null) return;
                double f = Math.min(1.0, (now - st) / 200.0);
                float t = sf(p);
                explode(p, rayTarget(p, 12), 2.5 * (1 + 3 * f), Math.min(14f, lerp(7f, 11f, t) * (float) (1 + f)), lerp(1.2f, 2.8f, t) * (1 + 0.5 * f));
                DET_CD.put(id, now + 30 + (long) (90 * f));
            }
            case "tm_quake" -> QUAKE_START.remove(id);
            case "il_light" -> {
                clearLight(lv, HAND_LIGHT.remove(id));
            }
            case "ww_flight" -> revokeFlight(p);
            default -> { }
        }
    }

    private static final UUID FLIGHT_ID = UUID.fromString("9b7e2d41-3c58-4fa6-b1d0-7e2a9c4f6d31"); // wind_flight

    private static void revokeFlight(ServerPlayer p) {
        Pal.flight(p, FLIGHT_ID, "wind_flight", 0.6, false);
        if (FLYING.remove(p.getUUID()) && !p.isCreative() && !p.isSpectator()) {
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false));
        }
    }

    // ------------------------------------------------------------------ chaque tick
    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            UUID id = p.getUUID();
            ServerLevel lv = p.serverLevel();

            double[] fa = FAULT_AT.get(id);
            if (fa != null && now >= (long) fa[0]) {
                FAULT_AT.remove(id);
                FAULT.put(id, new double[] {p.getX(), p.getY(), p.getZ(), fa[1], fa[2], MAG.contains(id) ? 20 : 14});
                dust(lv, p.position(), 10, 0.5);
            }
            double[] f = FAULT.get(id);
            if (f != null) {
                f[0] += f[3] * 1.2;
                f[2] += f[4] * 1.2;
                f[5]--;
                var ground = lv.clip(new ClipContext(new Vec3(f[0], f[1] + 3, f[2]), new Vec3(f[0], f[1] - 5, f[2]), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
                if (ground.getType() != HitResult.Type.MISS && f[5] > 0) {
                    Vec3 at = ground.getLocation();
                    f[1] = at.y;
                    dust(lv, at, 8, 0.5);
                    for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(at.x - 1.7, at.y - 1, at.z - 1.7, at.x + 1.7, at.y + 2.2, at.z + 1.7), e -> e != p && e.isAlive())) {
                        long last = e.getPersistentData().getLong("unrp_tm_hit");
                        if (now - last < 15) continue;
                        e.getPersistentData().putLong("unrp_tm_hit", now);
                        hit(p, e, MAG.contains(id) ? 8f : 6f);
                        e.setDeltaMovement(f[3], 0.45, f[4]);
                        e.hurtMarked = true;
                        e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false));
                    }
                } else FAULT.remove(id);
            }
            Long ba = BURST_AT.get(id);
            if (ba != null) {
                if (!p.isAlive() || now > ba + 40) BURST_AT.remove(id);
                else if (now < ba) root(p);
                else {
                    BURST_AT.remove(id);
                    double radius = MAG.contains(id) ? 22 : 16;
                    seismic(p, p.position(), radius, MAG.contains(id) ? 9f : 7f, 1.3, 1, 20);
                    dust(lv, p.position(), 16, 0.8);
                    for (int i = 0; i < 36; i++) {
                        double a = Math.PI * 2 * i / 36;
                        dust(lv, p.position().add(Math.cos(a) * radius * 0.85, 0, Math.sin(a) * radius * 0.85), 3, 0.3);
                    }
                    lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.6f);
                }
            }
            double[] ca = CATA.get(id);
            if (ca != null) {
                long el = now - (long) ca[0];
                Vec3 center = new Vec3(ca[1], ca[2], ca[3]);
                if (p.onGround()) root(p);
                if (el < 20) {
                    if (el % 2 == 0) dust(lv, center, 6, 1.0);
                } else if (el <= 68 && (el - 20) % 12 == 0) {
                    int wave = (int) ((el - 20) / 12);
                    double radius = 5 + wave * 5;
                    seismic(p, center, radius, new float[] {13f, 10f, 8f, 6.5f, 5f}[wave], 1.7 - wave * 0.2, 4, 60);
                    for (int i = 0; i < 26; i++) {
                        double a = Math.PI * 2 * i / 26;
                        dust(lv, center.add(Math.cos(a) * radius * 0.85, 0, Math.sin(a) * radius * 0.85), 4, 0.4);
                    }
                    lv.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.5f);
                    if (el == 68) CATA.remove(id);
                } else if (el > 68) CATA.remove(id);
            }
            if (MAG.contains(id) && (!has(p, "tremor") || AbilityEngine.aura(p) <= 0)) {
                MAG.remove(id);
                msg(p, "§6Magnitude control collapses.");
            } else if (MAG.contains(id) && now % 18 == 0) AbilityEngine.spendAura(p, 1); // -1 aura / 18 ticks

            Integer bc = BEACON.get(id);
            if (bc != null) {
                if (bc <= 0 || !has(p, "illumination")) BEACON.remove(id);
                else {
                    double radius = 12 + 12 * sf(p);
                    Vec3 c = p.position().add(0, 1, 0);
                    for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(c.subtract(radius, radius, radius), c.add(radius, radius, radius)), e -> e.isAlive() && e != p && e.isInvisible() && e.distanceToSqr(c) <= radius * radius)) {
                        e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false));
                    }
                    if (p.tickCount % 5 == 0) Fx.wave(lv, p.position().add(0, 0.2, 0), 3 + (200 - bc) * 0.05, 0xFFFFE0, 6);
                    BEACON.put(id, bc - 1);
                }
            }
            if (!has(p, "illumination") && HAND_LIGHT.containsKey(id)) clearLight(lv, HAND_LIGHT.remove(id));

            boolean us = has(p, "umbral_shift");
            if (us && p.tickCount % 30 == 0) p.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 0, true, false));
            Double travelled = PHASED.get(id);
            if (travelled != null) {
                if (us && AbilityEngine.aura(p) > 0 && travelled < 40.0) {
                    Vec3 lk = p.getViewVector(1f), fl = flat(lk), cur = p.position();
                    Vec3 ahead = cur.add(fl.scale(0.45)).add(0, lk.y * 0.45 * 0.5, 0);
                    int curSurf = surfaceY(lv, cur.x, cur.z), nextSurf = surfaceY(lv, ahead.x, ahead.z);
                    boolean blocked = nextSurf - curSurf > 2;
                    Vec3 next = blocked ? cur : ahead;
                    double ny = Math.max(lv.getMinBuildHeight() + 2, Math.min((blocked ? curSurf : nextSurf) - 1.8, next.y));
                    p.teleportTo(next.x, ny, next.z);
                    p.fallDistance = 0;
                    p.setInvisible(true);
                    if (blocked) {
                        if (p.tickCount % 30 == 0) msg(p, "§5The shadows cannot climb that.");
                    } else PHASED.put(id, travelled + 0.45);
                    Fx.sprite(lv, "smoke", 0x2A1A4A, 0.9f, new Vec3(next.x, ny + 1.05, next.z), 2, 0.3, 0.02);
                    if (p.tickCount % 7 == 0) AbilityEngine.spendAura(p, 1);
                } else emerge(p, lv, true);
            }
            // Whirlwind : cyclone, vol
            Integer cy = CYCLONE.get(id);
            if (cy != null) {
                if (cy <= 1) CYCLONE.remove(id);
                else CYCLONE.put(id, cy - 1);
                Vec3 center = p.position().add(0, 1, 0);
                double radius = 8.5;
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius)), e -> e != p && e.isAlive())) {
                    Vec3 to = center.subtract(e.position());
                    double d = to.length();
                    if (d > radius || d < 0.6) continue;
                    Vec3 pull = to.normalize().scale(0.18);
                    e.setDeltaMovement(e.getDeltaMovement().scale(0.6).add(pull.x, pull.y * 0.4, pull.z));
                    e.hurtMarked = true;
                }
                for (int i = 0; i < 12; i++) {
                    double a = p.tickCount * 0.4 + i * Math.PI / 6, r = 2 + (i % 3) * 1.2;
                    Fx.sprite(lv, "wisp", 0xBFE8FF, 0.8f, center.add(Math.cos(a) * r, Math.sin(p.tickCount * 0.2 + i) * 0.8, Math.sin(a) * r), 1, 0.05, 0);
                }
            }
            if (FLYING.contains(id) && (!has(p, "whirlwind") || !AbilityEngine.toggled(p, "whirlwind", 2))) revokeFlight(p);
        }
    }

    private static int surfaceY(ServerLevel lv, double x, double z) {
        return lv.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
    }

    public static boolean onAttack(LivingAttackEvent e) {
        if (e.getSource().type().msgId().contains("explosion") && explosionImmune(e.getEntity())) {
            e.setCanceled(true);
            return true;
        }
        return false;
    }

    public static boolean cancelKnockback(LivingEntity e) {
        return explosionImmune(e);
    }

    public static void onHurt(LivingHurtEvent e) {
        LivingEntity v = e.getEntity();
        if (v instanceof ServerPlayer vp) {
            if (isPhased(vp)) {
                e.setCanceled(true);
                return;
            }
            if (e.getSource().is(DamageTypeTags.IS_FALL)) {
                if (has(vp, "tremor")) e.setAmount(e.getAmount() * 0.7f);
                if (has(vp, "umbral_shift")) e.setAmount(e.getAmount() * 0.6f);
            }
            if (has(vp, "whirlwind") && e.getSource().is(DamageTypeTags.IS_FALL)) e.setAmount(e.getAmount() * 0.1f);
            DET_START.remove(vp.getUUID());
        }
        if (e.getSource().getEntity() instanceof ServerPlayer a && isPhased(a)) {
            e.setCanceled(true);
            msg(a, "§5You cannot attack from the shadows.");
        }
    }

    public static void modelTags(ServerPlayer p) {
        long now = p.serverLevel().getGameTime();
        tag(p, "unrp.us_phased", PHASED.containsKey(p.getUUID()));
    }

    private static void tag(ServerPlayer p, String t, boolean on) {
        if (on) p.addTag(t);
        else p.removeTag(t);
    }
}
