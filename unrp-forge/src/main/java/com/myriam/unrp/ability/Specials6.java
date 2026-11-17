package com.myriam.unrp.ability;

import com.myriam.unrp.data.AbilityPower;
import com.myriam.unrp.data.ClaimData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Griffes et flammes (lot 6) : Spectral Claw, Demon Claw, Flame Claw, Shadow Flame, Energy Discharge. Comportements repris de
 * SpectralClawEvents, DemonClawEvents, FlameClawEvents, ShadowFlameEvents, EnergyWaveEvents et AbilityEvents. Fiches : docs/fidelity/.
 */
public final class Specials6 {
    private Specials6() {}

    private static final Set<String> ALL = Set.of("sc_grip", "sc_throw", "dc_lunge", "dc_ripper", "dc_feral",
            "fc_ring", "fc_surge", "fc_rush", "sf_bolt", "sf_volley", "ed_beam", "ed_sphere", "ed_burst", "ed_propel");

    public static boolean handles(String s) {
        return s != null && ALL.contains(s);
    }

    private static final Map<UUID, int[]> GRIP = new HashMap<>();
    private static final Map<UUID, UUID> GRIP_TARGET = new HashMap<>();
    private static final Map<UUID, int[]> THROWN = new HashMap<>();
    private static final Map<UUID, UUID> THROWN_TARGET = new HashMap<>();
    private static final Map<UUID, Integer> LUNGE = new HashMap<>(), RUSH = new HashMap<>();
    private static final Map<UUID, Vec3> DASH_DIR = new HashMap<>();
    private static final Map<UUID, Set<UUID>> DASH_HIT = new HashMap<>();
    private static final Set<UUID> LUNGE_USED = new HashSet<>();
    private static final Map<UUID, Long> PRIMED = new HashMap<>();
    private static final Map<UUID, Long> BOLT_START = new HashMap<>(), BOLT_CD = new HashMap<>();
    private static final Map<UUID, Long> BEAM_START = new HashMap<>();
    private static final Map<UUID, Boolean> BEAM_WAVE = new HashMap<>();
    private static final Map<UUID, Integer> BURST = new HashMap<>(), PROPEL = new HashMap<>();
    private static final Set<UUID> NO_FALL = new HashSet<>();
    private static final Set<UUID> BUSY = new HashSet<>();

    public static void onLogout(UUID id) {
        for (Map<UUID, ?> m : java.util.List.of(GRIP, GRIP_TARGET, THROWN, THROWN_TARGET, LUNGE, RUSH, DASH_DIR, DASH_HIT, PRIMED, BOLT_START, BEAM_START, BEAM_WAVE, BURST, PROPEL)) m.remove(id);
        LUNGE_USED.remove(id);
        NO_FALL.remove(id);
    }

    public static boolean noFall(LivingEntity e) {
        return NO_FALL.contains(e.getUUID());
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

    private static void motion(LivingEntity e, double x, double y, double z) {
        e.setDeltaMovement(x, y, z);
        e.hurtMarked = true;
    }

    private static boolean isBoss(LivingEntity e) {
        return e instanceof EnderDragon || e instanceof WitherBoss || e instanceof Warden;
    }

    private static LivingEntity nearestInCone(ServerPlayer p, double range, double dot, boolean noBoss) {
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f);
        LivingEntity best = null;
        double bd = Double.MAX_VALUE;
        for (LivingEntity e : p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(range), e -> e != p && e.isAlive() && (!noBoss || !isBoss(e)))) {
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye);
            double d = to.length();
            if (d > range || to.normalize().dot(look) < dot || d >= bd) continue;
            bd = d;
            best = e;
        }
        return best;
    }

    private static Vec3 flat(Vec3 look) {
        Vec3 f = new Vec3(look.x, 0, look.z);
        return f.lengthSqr() < 1e-4 ? new Vec3(0, 0, 1) : f.normalize();
    }

    private static Vec3 rotY(Vec3 v, double deg) {
        double r = Math.toRadians(deg), c = Math.cos(r), s = Math.sin(r);
        return new Vec3(v.x * c - v.z * s, v.y, v.x * s + v.z * c);
    }

    private static void hit(ServerPlayer p, LivingEntity t, float dmg) {
        BUSY.add(p.getUUID());
        t.hurt(p.serverLevel().damageSources().playerAttack(p), dmg);
        BUSY.remove(p.getUUID());
    }

    // ------------------------------------------------------------------ utilisations
    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m) {
        ServerLevel lv = p.serverLevel();
        long now = lv.getGameTime();
        UUID id = p.getUUID();
        Vec3 look = p.getViewVector(1f);
        float t = sf(p);
        int c = power.look != null && power.look.rgb() >= 0 ? power.look.rgb() : 0xFFFFFF;
        switch (m.special) {
            // ---------------- Spectral Claw
            case "sc_grip" -> {
                LivingEntity target = nearestInCone(p, 4.5, 0.5, true);
                if (target == null) {
                    msg(p, "§5✖ The claw closes on empty air. §7Aura refunded.");
                    return false;
                }
                GRIP.put(id, new int[] {target.getId(), 160});
                GRIP_TARGET.put(id, target.getUUID());
                hit(p, target, 3f);
                Fx.sprite(lv, "glyph", 0x9A5CFF, 1.2f, target.getBoundingBox().getCenter(), 10, 0.4, 0.05);
            }
            case "sc_throw" -> {
                int[] held = GRIP.remove(id);
                GRIP_TARGET.remove(id);
                LivingEntity target = held == null ? null : lv.getEntity(held[0]) instanceof LivingEntity le ? le : null;
                if (target == null || !target.isAlive()) {
                    msg(p, "§5✖ Nothing in the claw to throw. §7Aura refunded.");
                    return false;
                }
                motion(target, look.x * 2.2, Math.max(0.5, look.y * 2.2 + 0.3), look.z * 2.2);
                hit(p, target, 6f);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false));
                THROWN.put(id, new int[] {target.getId(), 24});
                THROWN_TARGET.put(id, target.getUUID());
            }
            // ---------------- Demon Claw
            case "dc_lunge" -> {
                LUNGE.put(id, 6);
                DASH_DIR.put(id, flat(look));
                LUNGE_USED.remove(id);
                lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.7f);
            }
            case "dc_ripper" -> {
                boolean feral = AbilityEngine.toggled(p, "demon_claw", 2);
                float dmg = lerp(9f, 18f, t) + (feral ? 3f : 0f);
                Vec3 fl = flat(look), eye = p.getEyePosition();
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(4.5), e -> e != p && e.isAlive())) {
                    Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye);
                    Vec3 tf = new Vec3(to.x, 0, to.z);
                    if (to.length() > 4.5 || to.length() < 0.001 || tf.lengthSqr() < 1e-4 || fl.dot(tf.normalize()) < 0.707) continue;
                    hit(p, e, dmg);
                    e.knockback(0.4, -fl.x, -fl.z);
                    e.hurtMarked = true;
                }
                Fx.line(lv, eye.add(fl.scale(0.5)).add(0, -0.4, -0.0), eye.add(fl.scale(3.6)).add(0, 0.4, 0), c, 0, 0.14f, 6);
                Fx.line(lv, eye.add(fl.scale(0.5)).add(0, 0.4, 0), eye.add(fl.scale(3.6)).add(0, -0.4, 0), c, 0, 0.14f, 6);
                lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.5f);
            }
            case "dc_feral" -> { }
            // ---------------- Flame Claw
            case "fc_ring" -> {
                float dmg = lerp(6f, 14f, t);
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(5, 2, 5), e -> e != p && e.isAlive())) {
                    if (e.distanceToSqr(p.getX(), p.getY(), p.getZ()) > 25) continue;
                    hit(p, e, dmg);
                    e.setSecondsOnFire(5);
                    e.knockback(0.9, p.getX() - e.getX(), p.getZ() - e.getZ());
                    e.hurtMarked = true;
                }
                for (int i = 0; i < 60; i += 2) {
                    double a = Math.PI * 2 * i / 60.0;
                    Fx.sprite(lv, "ember", 0xFF7A1A, 1.0f, p.position().add(Math.cos(a) * 2.5, 0.3, Math.sin(a) * 2.5), 1, 0.1, 0.05);
                }
                lv.playSound(null, p.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.7f);
            }
            case "fc_surge" -> { }
            case "fc_rush" -> {
                RUSH.put(id, 8);
                DASH_DIR.put(id, flat(look));
                DASH_HIT.put(id, new HashSet<>());
                PRIMED.put(id, now + 100);
                lv.playSound(null, p.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 1.3f);
            }
            // ---------------- Shadow Flame
            case "sf_volley" -> {
                for (int i = 0; i < 3; i++) {
                    Vec3 dir = rotY(look, -5 + 5.0 * i).add(0, (p.getRandom().nextDouble() - 0.5) * 0.05, 0).normalize();
                    flameBolt(p, dir, 1.5, 24, 0.5f, 0.0, true);
                }
            }
            case "sf_bolt" -> {
                if (!BOLT_START.containsKey(id)) {
                    if (now < BOLT_CD.getOrDefault(id, 0L)) {
                        if (now % 20 < 4) msg(p, "§5✖ Fireball is cooling down (" + (1 + (BOLT_CD.get(id) - now) / 20) + " s)");
                        return false;
                    }
                    AbilityEngine.spendAura(p, 14);
                    BOLT_START.put(id, now);
                }
                double f = Math.min(1.0, (now - BOLT_START.get(id)) / 200.0);
                Fx.sprite(lv, "wisp", 0x9A4CFF, (float) (0.5 + 0.6 * f), p.getEyePosition().add(look.scale(0.9)).add(0, -0.25, 0), 3 + (int) (9 * f), 0.6 + 0.8 * f, 0.02);
            }
            // ---------------- Energy Discharge
            case "ed_beam" -> {
                Fx.QUIET++;
                try {
                    beam(p, lv, now, c);
                } finally {
                    Fx.QUIET--;
                }
            }
            case "ed_sphere" -> {
                Vec3 right = new Vec3(-flat(look).z, 0, flat(look).x);
                Projectiles.P s = new Projectiles.P();
                s.kind = Projectiles.Kind.NEEDLE;
                s.owner = id;
                s.pos = p.getEyePosition().add(look.scale(0.3)).add(right.scale(0.25)).add(0, -0.5, 0);
                s.vel = look.scale(1.5);
                s.gravity = 0.01;
                s.maxLife = 100;
                s.damage = 20f;
                s.size = 1.6f;
                s.rgb = c;
                s.sprite = "flare";
                s.palladium = true;
                int aux = com.myriam.unrp.pass.LightningPass.newAux();
                s.onEnd = pr -> {
                    com.myriam.unrp.pass.EdPass.blast(p, aux, pr.pos);
                    lv.explode(p, pr.pos.x, pr.pos.y, pr.pos.z, 2.0f, Level.ExplosionInteraction.NONE);
                };
                Projectiles.spawn(s);
                com.myriam.unrp.pass.LightningPass.launch(p, com.myriam.unrp.pass.PassFxPacket.ED_ORB, aux, s.pos, s.vel, s.maxLife, true);
                AbilityEngine.spendAura(p, 20);
            }
            case "ed_burst" -> {
                BURST.put(id, 6);
                com.myriam.unrp.pass.PassEvents.ed(lv, com.myriam.unrp.pass.PassFxPacket.ED_GATHER, p, p.position(), null);
            }
            case "ed_propel" -> {
                motion(p, look.x * 2.5, look.y * 2.5, look.z * 2.5);
                com.myriam.unrp.pass.PassEvents.ed(lv, com.myriam.unrp.pass.PassFxPacket.ED_LAUNCH, p, p.position(), look);
                NO_FALL.add(id);
                PROPEL.put(id, 100);
            }
            default -> { }
        }
        return true;
    }

    public static void whileOn(ServerPlayer p, AbilityPower.Move m, long now) {

    }

    public static void stop(ServerPlayer p, String special) {
        UUID id = p.getUUID();
        if ("sf_bolt".equals(special)) {
            Long st = BOLT_START.remove(id);
            if (st == null) return;
            long now = p.serverLevel().getGameTime();
            double f = Math.min(1.0, (now - st) / 200.0);
            flameBolt(p, p.getViewVector(1f), 1.5 + 0.4 * f, 24 + (int) (8 * f), (float) (0.5 + 0.6 * f), f, false);
            BOLT_CD.put(id, now + 40 + (long) (100 * f));
            p.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        } else if ("ed_beam".equals(special)) {
            BEAM_START.remove(id);
            BEAM_WAVE.remove(id);
            com.myriam.unrp.pass.PassSync.key(p, "long_range_beam", false);
            com.myriam.unrp.pass.PassSync.key(p, "wave", false);
            com.myriam.unrp.pass.PassSync.key(p, "wave_run", false);
        }
    }

    private static void flameBolt(ServerPlayer p, Vec3 dir, double speed, int life, float size, double f, boolean dual) {
        ServerLevel lv = p.serverLevel();
        float s = sf(p);
        float direct = (float) Math.min(14.0, (dual ? lerp(6f, 7.5f, s) : lerp(7f, 9f, s)) * (1.0 + f));
        float splash = (float) ((dual ? lerp(3f, 3.8f, s) : lerp(3.5f, 4.5f, s)) * (1.0 + f));
        double blast = 2.5 * (1.0 + 3.0 * f);
        LivingEntity[] directHit = new LivingEntity[1];
        Projectiles.P b = new Projectiles.P();
        b.kind = Projectiles.Kind.NEEDLE;
        b.owner = p.getUUID();
        b.pos = p.getEyePosition().add(dir.scale(0.5)).add(0, -0.1, 0);
        b.vel = dir.scale(speed);
        b.maxLife = life;
        b.damage = direct;
        b.size = size;
        b.rgb = 0x9A4CFF;
        b.sprite = "wisp";
        b.onHit = (pr, e) -> {
            directHit[0] = e;
            e.setSecondsOnFire(3 + (int) (4 * f));
            Vec3 d = pr.vel.normalize();
            e.knockback(0.7, -d.x, -d.z);
            e.setDeltaMovement(e.getDeltaMovement().add(0, 0.2, 0));
            e.hurtMarked = true;
        };
        b.onEnd = pr -> {
            Vec3 loc = pr.pos;
            for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(loc.subtract(blast, blast, blast), loc.add(blast, blast, blast)), e -> e != p && e != directHit[0] && e.isAlive())) {
                if (e.distanceToSqr(loc.x, loc.y, loc.z) > blast * blast) continue;
                hit(p, e, splash);
                e.setSecondsOnFire(2 + (int) (3 * f));
            }
            Fx.sprite(lv, "wisp", 0x9A4CFF, 1.6f, loc, (int) (30 * (1 + f)), 0.55 * (1 + f), 0.1);
            lv.playSound(null, p.blockPosition(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 0.8f, 1.0f);
        };
        Projectiles.spawn(b);
        lv.playSound(null, p.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 0.7f);
    }

    private static void beam(ServerPlayer p, ServerLevel lv, long now, int c) {
        UUID id = p.getUUID();
        if (!BEAM_START.containsKey(id)) {
            BEAM_START.put(id, now);
            BEAM_WAVE.put(id, p.isShiftKeyDown());
        }
        long held = now - BEAM_START.get(id);
        boolean waveMode = BEAM_WAVE.get(id);
        com.myriam.unrp.pass.PassSync.key(p, "long_range_beam", !waveMode);
        com.myriam.unrp.pass.PassSync.key(p, "wave", waveMode);
        com.myriam.unrp.pass.PassSync.key(p, "wave_run", waveMode && held >= 10);
        Vec3 eye = p.getEyePosition(), dir = p.getViewVector(1f);
        var bh = lv.clip(new ClipContext(eye, eye.add(dir.scale(32)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        double reach = bh.getType() == net.minecraft.world.phys.HitResult.Type.MISS ? 32 : bh.getLocation().distanceTo(eye);
        if (BEAM_WAVE.get(id)) {
            if (held < 10) {
                Fx.sprite(lv, "mote", c, 0.8f, eye.add(dir.scale(0.8)), 2, 0.3, 0.02);
                return;
            }
            double value = Math.min(1.0, (held - 10) * 0.25);
            double len = reach * value;
            Fx.line(lv, eye.add(dir.scale(0.5)).add(0, -0.3, 0), eye.add(dir.scale(len)), c, 0, 0.5f, 5);
            for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(eye, eye.add(dir.scale(len + 0.5))).inflate(3.5), e -> e != p && e.isAlive())) {
                Vec3 ctr = e.position().add(0, e.getBbHeight() * 0.5, 0);
                double along = ctr.subtract(eye).dot(dir);
                if (along < 0 || along > len + 0.5) continue;
                if (ctr.distanceTo(eye.add(dir.scale(along))) > 1.5 + e.getBbWidth() * 0.5) continue;
                hit(p, e, 2f);
            }
            if (held % 3 == 0) AbilityEngine.spendAura(p, 2);
            return;
        }
        Fx.line(lv, eye.add(dir.scale(0.5)).add(0, -0.3, 0), eye.add(dir.scale(reach)), c, 0, 0.18f, 5);
        LivingEntity best = null;
        double bd = Double.MAX_VALUE;
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(eye, eye.add(dir.scale(reach))).inflate(1.0), e -> e != p && e.isAlive())) {
            if (e.getBoundingBox().inflate(0.3).clip(eye, eye.add(dir.scale(reach))).isEmpty()) continue;
            double d = e.distanceToSqr(p);
            if (d < bd) {
                bd = d;
                best = e;
            }
        }
        if (best != null) hit(p, best, 1f);
    }

    // ------------------------------------------------------------------ chaque tick
    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            UUID id = p.getUUID();
            ServerLevel lv = p.serverLevel();
            boolean sc = has(p, "spectral_claw");
            attr(p, net.minecraftforge.common.ForgeMod.ENTITY_REACH.get(), "sc_reach", 1.5, sc);
            // Spectral Claw : prise
            int[] g = GRIP.get(id);
            if (g != null) {
                LivingEntity t = lv.getEntity(g[0]) instanceof LivingEntity le ? le : null;
                g[1]--;
                if (t != null && t.isAlive() && sc && g[1] > 0 && t.distanceTo(p) <= 8) {
                    Vec3 hold = p.getEyePosition().add(p.getViewVector(1f).scale(2.6));
                    Vec3 pull = hold.subtract(t.position().add(0, t.getBbHeight() * 0.5, 0)).scale(0.45);
                    if (pull.length() > 1.2) pull = pull.normalize().scale(1.2);
                    motion(t, pull.x, pull.y, pull.z);
                    t.fallDistance = 0;
                    t.addEffect(new MobEffectInstance(MobEffects.JUMP, 8, 128, false, false));
                    if (t instanceof ServerPlayer sp) sp.setSprinting(false);
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 0, false, false));
                    if (g[1] % 20 == 0) hit(p, t, 1.5f);
                } else {
                    GRIP.remove(id);
                    GRIP_TARGET.remove(id);
                    if (t != null && t.isAlive()) msg(p, "§5The claw releases its grip.");
                }
            }
            int[] th = THROWN.get(id);
            if (th != null) {
                LivingEntity t = lv.getEntity(th[0]) instanceof LivingEntity le ? le : null;
                if (t != null && t.isAlive() && --th[1] > 0) {
                    if (t.horizontalCollision) {
                        THROWN.remove(id);
                        hit(p, t, 5f);
                        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 3, false, false));
                        Fx.sprite(lv, "rock", 0x9A5CFF, 1.2f, t.getBoundingBox().getCenter(), 14, 0.5, 0.15);
                    }
                } else THROWN.remove(id);
            }
            // Demon Claw : bond
            boolean dc = has(p, "demon_claw");
            attr(p, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, "dc_feral", 0.12, dc && AbilityEngine.toggled(p, "demon_claw", 2));
            Integer lg = LUNGE.get(id);
            if (lg != null) {
                if (!dc || lg <= 0) {
                    LUNGE.remove(id);
                } else {
                    Vec3 d = DASH_DIR.get(id);
                    motion(p, d.x * 1.2, Math.max(p.getDeltaMovement().y, -0.2), d.z * 1.2);
                    p.fallDistance = 0;
                    if (!LUNGE_USED.contains(id)) {
                        float dmg = lerp(7f, 15f, sf(p)) + (AbilityEngine.toggled(p, "demon_claw", 2) ? 2f : 0f);
                        LivingEntity best = null;
                        double bd = Double.MAX_VALUE;
                        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(1.5), e -> e != p && e.isAlive())) {
                            double dd = e.distanceToSqr(p);
                            if (dd < bd) {
                                bd = dd;
                                best = e;
                            }
                        }
                        if (best != null) {
                            hit(p, best, dmg);
                            best.knockback(0.6, -d.x, -d.z);
                            best.setDeltaMovement(best.getDeltaMovement().add(0, 0.25, 0));
                            best.hurtMarked = true;
                            LUNGE_USED.add(id);
                            Fx.sprite(lv, "shard", 0xC8283C, 1.2f, best.getBoundingBox().getCenter(), 12, 0.4, 0.15);
                        }
                    }
                    Fx.sprite(lv, "ember", 0xC8283C, 0.8f, p.position().add(0, 0.6, 0), 2, 0.3, 0.02);
                    if (p.horizontalCollision || --lg <= 0) LUNGE.remove(id);
                    else LUNGE.put(id, lg);
                }
            }

            boolean fc = has(p, "flame_claw");
            Integer rs = RUSH.get(id);
            if (rs != null) {
                if (!fc || rs <= 0) {
                    RUSH.remove(id);
                } else {
                    Vec3 d = DASH_DIR.get(id);
                    motion(p, d.x * 1.25, Math.max(p.getDeltaMovement().y, -0.2), d.z * 1.25);
                    p.fallDistance = 0;
                    Set<UUID> hs = DASH_HIT.computeIfAbsent(id, k -> new HashSet<>());
                    float dmg = lerp(8f, 16f, sf(p));
                    for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(1.75), e -> e != p && e.isAlive())) {
                        if (!hs.add(e.getUUID())) continue;
                        hit(p, e, dmg);
                        e.setSecondsOnFire(6);
                        e.knockback(0.7, -d.x, -d.z);
                        e.hurtMarked = true;
                    }
                    Fx.sprite(lv, "ember", 0xFF7A1A, 1.0f, p.position().add(0, 0.6, 0), 4, 0.3, 0.03);
                    if (p.horizontalCollision || --rs <= 0) RUSH.remove(id);
                    else RUSH.put(id, rs);
                }
            }
            Long pr = PRIMED.get(id);
            if (pr != null && (now > pr || !fc)) PRIMED.remove(id);

            Integer bu = BURST.get(id);
            if (bu != null) {
                if (bu > 1) BURST.put(id, bu - 1);
                else {
                    BURST.remove(id);
                    Specials3.aoe(p, 12.0, 28.0, 0.0);
                    com.myriam.unrp.pass.PassEvents.ed(lv, com.myriam.unrp.pass.PassFxPacket.ED_BURST, p, p.position(), null);
                }
            }
            Integer pp = PROPEL.get(id);
            if (pp != null) {
                int rem = pp - 1;
                if (rem <= 0 || (rem < 95 && p.onGround())) {
                    PROPEL.remove(id);
                    NO_FALL.remove(id);
                    if (rem > 0) com.myriam.unrp.pass.PassEvents.ed(lv, com.myriam.unrp.pass.PassFxPacket.ED_LAND, p, p.position(), null);
                } else {
                    PROPEL.put(id, rem);
                }
            }
        }
    }

    private static void attr(LivingEntity e, net.minecraft.world.entity.ai.attributes.Attribute a, String key, double amount, boolean on) {
        var inst = e.getAttribute(a);
        if (inst == null) return;
        UUID id = UUID.nameUUIDFromBytes(("unrp:sp6:" + key).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        boolean has = inst.getModifier(id) != null;
        if (on && !has) inst.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, "unrp " + key, amount,
                key.equals("dc_feral") ? net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL : net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        else if (!on && has) inst.removeModifier(id);
    }

    public static void onHurt(LivingHurtEvent e) {
        LivingEntity v = e.getEntity();
        if (v instanceof ServerPlayer vp) {
            if (has(vp, "flame_claw") && e.getSource().is(DamageTypeTags.IS_FIRE)) e.setAmount(e.getAmount() * 0.5f);
            if (GRIP.remove(vp.getUUID()) != null) {
                GRIP_TARGET.remove(vp.getUUID());
                msg(vp, "§5Your grip was broken!");
            }
            BOLT_START.remove(vp.getUUID());
        }
        if (!(e.getSource().getEntity() instanceof ServerPlayer a) || a == v || e.getSource().getDirectEntity() != a || BUSY.contains(a.getUUID())) return;
        float t = sf(a);
        if (has(a, "demon_claw")) { // Claw strikes.
            e.setAmount(e.getAmount() + (AbilityEngine.toggled(a, "demon_claw", 2) ? lerp(4f, 11f, t) : lerp(2f, 7f, t)));
            Fx.sprite(a.serverLevel(), "shard", 0xC8283C, 1.0f, v.getBoundingBox().getCenter(), 6, 0.3, 0.12);
        }
        if (has(a, "flame_claw")) {
            boolean surge = AbilityEngine.toggled(a, "flame_claw", 1);
            float bonus = surge ? lerp(4f, 12f, t) : lerp(2f, 8f, t);
            float burn = surge ? lerp(6f, 10f, t) : lerp(3f, 6f, t);
            Long pr = PRIMED.get(a.getUUID());
            if (pr != null && a.serverLevel().getGameTime() <= pr) {
                PRIMED.remove(a.getUUID());
                bonus += lerp(8f, 20f, t);
                burn = lerp(8f, 12f, t);
            }
            e.setAmount(e.getAmount() + bonus);
            v.setSecondsOnFire((int) burn);
            Fx.sprite(a.serverLevel(), "ember", 0xFF7A1A, 1.0f, v.getBoundingBox().getCenter(), 8, 0.3, 0.08);
        }
    }
}
