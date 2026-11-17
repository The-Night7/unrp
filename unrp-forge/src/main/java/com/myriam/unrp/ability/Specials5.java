package com.myriam.unrp.ability;

import com.myriam.unrp.data.AbilityPower;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;

public final class Specials5 {
    private Specials5() {}

    private static final Set<String> ALL = Set.of("ms_missile", "ms_heavy", "ms_twin", "ms_barrage",
            "nd_shot", "nd_barrage", "nd_volley",
            "pf_fist", "pf_double", "pf_meteor", "pf_barrage",
            "pp_push", "pp_volley", "pp_palm", "pp_wave",
            "cs_slash", "cs_twin", "cs_volley", "cs_grand");

    public static boolean handles(String s) {
        return s != null && ALL.contains(s);
    }

    // ------------------------------------------------------------------ charges maintenues

    private static final Map<String, Long> CHG = new HashMap<>();

    private static final Map<String, Long> CD = new HashMap<>();
    private static final Map<String, Long> LAST_SHOT = new HashMap<>();

    private record Spec(int start, int max, String name, int rgb) {}

    private static final Map<String, Spec> CHARGES = Map.of(
            "ms_missile", new Spec(14, 60, "Missile", 0xE89078),
            "ms_heavy", new Spec(60, 80, "Missile lourd", 0xE89078),
            "pf_fist", new Spec(9, 55, "Phantom Fist", 0x3FD0E8),
            "pp_push", new Spec(8, 50, "Phantom Push", 0x6F8FFF),
            "cs_slash", new Spec(8, 60, "Croissant", 0xBFE8FF),
            "cs_grand", new Spec(42, 80, "Grand croissant", 0xBFE8FF));

    private static String key(ServerPlayer p, String special) {
        return p.getUUID() + ":" + special;
    }

    private static void msg(ServerPlayer p, String s) {
        p.displayClientMessage(Component.literal(s), true);
    }

    private static int col(AbilityPower power, int def) {
        return power != null && power.look != null && power.look.rgb() >= 0 ? power.look.rgb() : def;
    }

    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m) {
        ServerLevel lv = p.serverLevel();
        long now = lv.getGameTime();
        String sp = m.special;
        Spec spec = CHARGES.get(sp);
        if (spec != null) {
            String k = key(p, sp);
            if (!CHG.containsKey(k)) {
                if (now < CD.getOrDefault(k, 0L)) {
                    if (now % 20 < 4) msg(p, "§c✖ " + spec.name + " is cooling down (" + (1 + (CD.get(k) - now) / 20) + " s)");
                    return false;
                }
                AbilityEngine.spendAura(p, spec.start);
                CHG.put(k, now);
                lv.playSound(null, p.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.8f, 1.2f);
            }
            double frac = Math.min(1.0, (now - CHG.get(k)) / (double) spec.max);
            Fx.sprite(lv, "mote", spec.rgb, (float) (0.5 + 0.6 * frac), p.position().add(0, 1.1, 0), 2 + (int) (5 * frac), 0.5 + 0.5 * frac, 0.02);
            return true;
        }
        Vec3 look = p.getViewVector(1f);
        int c = col(power, 0xFFFFFF);
        switch (sp) {
            // ---------------- Missiles
            case "ms_twin" -> {
                Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
                Vec3 perp = new Vec3(-flat.z, 0, flat.x);
                if (p.isShiftKeyDown()) {
                    AbilityEngine.spendAura(p, 10);
                    for (double ang : new double[] {-28, -10, 10, 28}) missile(p, rotY(look, ang), null, 1.8f, 1.1, look);
                } else {
                    missile(p, rotY(look, -4), perp.scale(0.45), 2.0f, 1.15, look);
                    missile(p, rotY(look, 4), perp.scale(-0.45), 2.0f, 1.15, look);
                }
            }
            case "ms_barrage" -> {
                String k = key(p, sp);
                if (!CHG.containsKey(k)) {
                    AbilityEngine.spendAura(p, 10);
                    CHG.put(k, now);
                    LAST_SHOT.put(k, now - 5);
                }
                if (AbilityEngine.aura(p) <= 0) {
                    msg(p, "§c✖ The barrage stops: no aura left.");
                    return false;
                }
                if (now - LAST_SHOT.get(k) >= 5) {
                    LAST_SHOT.put(k, now);
                    Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
                    Vec3 perp = new Vec3(-flat.z, 0, flat.x);
                    double side = (now / 5) % 2 == 0 ? 0.4 : -0.4;
                    double jitter = (p.getRandom().nextDouble() - 0.5) * 5.0;
                    missile(p, rotY(look, jitter), perp.scale(side), 1.6f, 1.2, look);
                    AbilityEngine.spendAura(p, 2);
                }
            }
            // ---------------- Needles
            case "nd_shot" -> needles(p, look, 1, 0, 3.0, 2f, 5f, 12, false, c);
            case "nd_barrage" -> needles(p, look, 7, 35, 2.8, 1.5f, 3.5f, 10, true, c);
            case "nd_volley" -> {
                String k = key(p, sp);
                if (!CHG.containsKey(k)) {
                    CHG.put(k, now);
                    LAST_SHOT.put(k, now - 5);
                }
                if (now - LAST_SHOT.get(k) >= 5) {
                    if (AbilityEngine.aura(p) < 6) {
                        msg(p, "§c✖ Plus assez d'aura.");
                        return false;
                    }
                    LAST_SHOT.put(k, now);
                    AbilityEngine.spendAura(p, 6);
                    needles(p, look, 5, 5, 3.5, 3f, 6f, 12, false, c);
                }
            }
            // ---------------- Phantom Fist
            case "pf_double" -> {
                Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
                Vec3 perp = new Vec3(-flat.z, 0, flat.x);
                fist(p, look, perp.scale(0.35), 6f, 0.8f, 0.6, 1.5, 36, 0x3FD0E8);
                later(p, 4, pl -> fist(pl, pl.getViewVector(1f), perp.scale(-0.35), 6f, 0.8f, 0.6, 1.5, 36, 0x3FD0E8));
            }
            case "pf_meteor" -> slamSequence(p, 16, 40, 30, 13, 13f, 2.8f, 0.9, 2.2, 26, 0, false, 80, 0x3FD0E8, "phantom_fist");
            case "pf_barrage" -> {
                msg(p, "§3Barrage! A volley of phantom fists!");
                SEQ.add(new Seq(p.getUUID(), 60, (pl, rem) -> {
                    if (AbilityEngine.aura(pl) <= 0) {
                        msg(pl, "§3The barrage fades: no aura left.");
                        return true;
                    }
                    if (rem % 2 == 0) jab(pl, rem);
                    return false;
                }));
            }
            // ---------------- Phantom Push
            case "pp_volley" -> {
                for (int i = 0; i < 5; i++) {
                    final int n = i;
                    later(p, 3 * i, pl -> {
                        double jitter = (pl.getRandom().nextDouble() - 0.5) * 14.0;
                        Vec3 lk = pl.getViewVector(1f);
                        hand(pl, rotY(lk, jitter), null, 3f, 0.55f, 0.7, 1.4, 36, lk, 0x6F8FFF);
                    });
                }
            }
            case "pp_palm" -> slamSequence(p, 14, 34, 26, 11, 11f, 2.4f, 1.8, 1.9, 16, 30, true, 70, 0x6F8FFF, "phantom_push");
            case "pp_wave" -> {
                for (Entity e : lv.getEntitiesOfClass(Entity.class, p.getBoundingBox().inflate(6), e -> e.isAlive() && e != p)) {
                    Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(p.getEyePosition());
                    if (to.length() > 6.5 || to.normalize().dot(look) < 0.35) continue;
                    if (e instanceof Projectile) {
                        e.setDeltaMovement(e.getDeltaMovement().scale(-1.1));
                        e.hurtMarked = true;
                    } else if (e instanceof LivingEntity le) {
                        Vec3 away = to.normalize();
                        le.hurt(lv.damageSources().playerAttack(p), 1f);
                        le.setDeltaMovement(away.x * 1.3, Math.max(0.3, away.y * 0.6 + 0.3), away.z * 1.3);
                        le.hurtMarked = true;
                    }
                }
                for (int i = 0; i < 6; i++) Fx.line(lv, p.getEyePosition().add(0, -0.3, 0), p.getEyePosition().add(rotY(look, -50 + 20 * i).scale(5)), 0x6F8FFF, 0, 0.12f, 6);
                Fx.wave(lv, p.position().add(look.scale(2)), 5, 0x6F8FFF, 8);
            }
            // ---------------- Crescent Slash
            case "cs_twin" -> {
                Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
                Vec3 perp = new Vec3(-flat.z, 0, flat.x);
                if (p.isShiftKeyDown()) {
                    crescent(p, rotY(look, -14), perp.scale(1.6), 9f, 1.15f, 3, 1.25, 26, c);
                    crescent(p, rotY(look, 14), perp.scale(-1.6), 9f, 1.15f, 3, 1.25, 26, c);
                } else {
                    crescent(p, rotY(look, -8), null, 6f, 0.9f, 1, 1.25, 24, c);
                    crescent(p, rotY(look, 8), null, 6f, 0.9f, 1, 1.25, 24, c);
                }
            }
            case "cs_volley" -> {
                double[] angles = {-24, -12, 0, 12, 24};
                for (int i = 0; i < 5; i++) {
                    final double a = angles[i];
                    later(p, 3 * i, pl -> crescent(pl, rotY(pl.getViewVector(1f), a), null, 4f, 0.6f, 0, 1.35, 22, c));
                }
            }
            default -> { }
        }
        return true;
    }

    public static void stop(ServerPlayer p, String special) {
        String k = key(p, special);
        if ("ms_barrage".equals(special) || "nd_volley".equals(special)) {
            CHG.remove(k);
            LAST_SHOT.remove(k);
            return;
        }
        Spec spec = CHARGES.get(special);
        if (spec == null) return;
        Long start = CHG.remove(k);
        if (start == null) return;
        ServerLevel lv = p.serverLevel();
        long now = lv.getGameTime();
        double f = Math.min(1.0, (now - start) / (double) spec.max);
        Vec3 look = p.getViewVector(1f);
        AbilityPower power = AbilityEngine.currentPower(p);
        int c = col(power, spec.rgb);
        int cd;
        switch (special) {
            case "ms_missile" -> {
                missile(p, look, null, (float) (1.0 + 1.2 * f), 1.0 + 0.5 * f, look);
                cd = 30 + (int) (20 * f);
            }
            case "ms_heavy" -> {
                missile(p, look, null, (float) (2.2 + 1.3 * f), 0.9, look);
                cd = 280 + (int) (80 * f);
            }
            case "pf_fist" -> {
                fist(p, look, null, (float) (7 + 6 * f), (float) (0.9 + 0.8 * f), 0.6 + 0.4 * f, 1.4 + 0.6 * f, 40, 0x3FD0E8);
                cd = 22 + (int) (18 * f);
            }
            case "pp_push" -> {
                hand(p, look, null, (float) (5 + 4 * f), (float) (1 + 0.8 * f), 1.1 + 0.8 * f, 1.3 + 0.6 * f, 40, look, 0x6F8FFF);
                cd = 25 + (int) (20 * f);
            }
            case "cs_slash" -> {
                crescent(p, look, null, (float) (6 + 6 * f), (float) (0.8 + 1.0 * f), 1, 1.2 + 0.5 * f, 24 + 16 * f, c);
                cd = 20 + (int) (15 * f);
            }
            case "cs_grand" -> {
                Vec3 flat = new Vec3(look.x, 0, look.z);
                flat = flat.lengthSqr() < 0.01 ? new Vec3(0, 0, 1) : flat.normalize();
                Vec3 perp = new Vec3(-flat.z, 0, flat.x);
                for (double side : new double[] {1.1, -1.1}) {
                    crescent(p, look, perp.scale(side), (float) (10 + 10 * f), (float) (1.7 + 1.5 * f), 5, 1.0 + 0.35 * f, 34 + 20 * f, c);
                }
                cd = 200 + (int) (120 * f);
            }
            default -> cd = 0;
        }
        CD.put(k, now + cd);
    }

    public static void onLogout(UUID id) {
        CHG.keySet().removeIf(k -> k.startsWith(id.toString()));
        LAST_SHOT.keySet().removeIf(k -> k.startsWith(id.toString()));
        SEQ.removeIf(s -> s.id.equals(id));
        LATER.removeIf(l -> l.id.equals(id));
    }

    public static void onHurt(LivingHurtEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || e.getAmount() <= 0) return;
        boolean interrupted = false;
        for (String s : CHARGES.keySet()) interrupted |= CHG.remove(key(p, s)) != null;
        if (interrupted) msg(p, "§c✖ Charge interrompue !");
    }

    private static Vec3 rotY(Vec3 v, double deg) {
        double r = Math.toRadians(deg), c = Math.cos(r), s = Math.sin(r);
        return new Vec3(v.x * c - v.z * s, v.y, v.x * s + v.z * c);
    }

    private static Vec3 start(ServerPlayer p, Vec3 dir, Vec3 offset, double fwd, double down) {
        Vec3 pos = p.getEyePosition().add(dir.scale(fwd)).add(0, -down, 0);
        return offset == null ? pos : pos.add(offset);
    }

    private static void missile(ServerPlayer p, Vec3 dir, Vec3 offset, float blast, double speed, Vec3 aim) {
        Projectiles.P m = new Projectiles.P();
        m.kind = Projectiles.Kind.MISSILE;
        m.owner = p.getUUID();
        m.pos = start(p, dir, offset, 0.7, 0.25);
        m.vel = dir.scale(speed);
        m.aim = aim;
        m.drag = 0.96;
        m.aimPull = 0.1;
        m.maxLife = 90;
        m.blast = blast;
        m.rgb = 0xE89078;
        m.sprite = "ember";
        Projectiles.spawn(m);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.8f, 1.1f);
    }

    private static void needles(ServerPlayer p, Vec3 look, int count, double spread, double speed, float dmin, float dmax, int life, boolean fan, int rgb) {
        ServerLevel lv = p.serverLevel();
        for (int i = 0; i < count; i++) {
            double yaw = 0, pitch = 0;
            if (count > 1) {
                if (fan) yaw = -spread / 2 + i * spread / (count - 1);
                else {
                    yaw = (p.getRandom().nextDouble() - 0.5) * spread;
                    pitch = (p.getRandom().nextDouble() - 0.5) * spread;
                }
            }
            Vec3 dir = rotY(look, yaw).add(0, Math.tan(Math.toRadians(pitch)) * 0.5, 0).normalize();
            Projectiles.P n = new Projectiles.P();
            n.kind = Projectiles.Kind.NEEDLE;
            n.owner = p.getUUID();
            n.pos = start(p, look, null, 0.4, 0.3);
            n.vel = dir.scale(speed);
            n.maxLife = life;
            n.damage = dmin;
            n.damageMax = dmax;
            n.size = 0.25f;
            n.rgb = rgb;
            n.lineTrail = true;
            Projectiles.spawn(n);
        }
        lv.playSound(null, p.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5f, 1.8f);
    }

    private static void hand(ServerPlayer p, Vec3 dir, Vec3 offset, float dmg, float size, double kb, double speed, int life, Vec3 aim, int rgb) {
        Projectiles.P h = new Projectiles.P();
        h.kind = Projectiles.Kind.HAND;
        h.owner = p.getUUID();
        h.pos = start(p, dir, offset, 0.8, 0.25);
        h.vel = dir.scale(speed);
        h.aim = aim;
        h.drag = 0.94;
        h.aimPull = 0.12;
        h.maxLife = life;
        h.damage = dmg;
        h.size = size;
        h.knock = kb;
        h.rgb = rgb;
        h.sprite = "flare";
        Projectiles.spawn(h);
    }

    private static void fist(ServerPlayer p, Vec3 dir, Vec3 offset, float dmg, float size, double kb, double speed, int life, int rgb) {
        hand(p, dir, offset, dmg, size, kb, speed, life, dir, rgb);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 0.8f, 0.9f);
    }

    private static void crescent(ServerPlayer p, Vec3 dir, Vec3 offset, float dmg, float size, int pierce, double speed, double range, int rgb) {
        Projectiles.P c = new Projectiles.P();
        c.kind = Projectiles.Kind.CRESCENT;
        c.owner = p.getUUID();
        c.pos = start(p, dir, offset, 0.8, 0.2);
        c.vel = dir.scale(speed);
        c.maxLife = 100;
        c.range = range;
        c.damage = dmg;
        c.size = size;
        c.pierce = pierce;
        c.rgb = rgb;
        c.lineTrail = true;
        Projectiles.spawn(c);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9f, 1.4f);
    }

    private static void jab(ServerPlayer p, int rem) {
        ServerLevel lv = p.serverLevel();
        Vec3 bl = p.getViewVector(1f);
        LivingEntity best = null;
        double bb = 0.45;
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(3.8), e -> e != p && e.isAlive())) {
            double dot = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(p.getEyePosition()).normalize().dot(bl);
            if (dot > bb) {
                bb = dot;
                best = e;
            }
        }
        if (best == null) return;
        best.invulnerableTime = 0;
        best.hurt(lv.damageSources().playerAttack(p), 1.6f);
        Fx.sprite(lv, "flare", 0x3FD0E8, 1.0f, best.getBoundingBox().getCenter(), 5, 0.25, 0.12);
    }

    private record Seq(UUID id, int total, BiPredicate<ServerPlayer, Integer> step) {
        int[] left() {
            return new int[] {total};
        }
    }

    private static final List<Seq> SEQ = new ArrayList<>();
    private static final Map<Seq, int[]> SEQ_LEFT = new HashMap<>();
    private record Later(UUID id, long at, java.util.function.Consumer<ServerPlayer> run) {}

    private static final List<Later> LATER = new ArrayList<>();

    private static void later(ServerPlayer p, int ticks, java.util.function.Consumer<ServerPlayer> run) {
        if (ticks <= 0) run.accept(p);
        else LATER.add(new Later(p.getUUID(), p.serverLevel().getGameTime() + ticks, run));
    }

    private static void slamSequence(ServerPlayer p, double aimRange, int total, int holdUntil, int spawnAt, float dmg, float size, double kb,
                                     double fall, int life, int stun, boolean glass, int slowFall, int rgb, String powerId) {
        ServerLevel lv = p.serverLevel();
        Vec3 v = p.getDeltaMovement();
        p.setDeltaMovement(v.x, powerId.equals("phantom_fist") ? 1.3 : 1.15, v.z);
        p.hurtMarked = true;
        p.fallDistance = 0;
        Vec3 from = p.getEyePosition(), look = p.getViewVector(1f);
        HitResult hit = lv.clip(new ClipContext(from, from.add(look.scale(aimRange)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        Vec3 spot = hit.getType() == HitResult.Type.MISS ? from.add(look.scale(10)) : hit.getLocation();
        double height = powerId.equals("phantom_fist") ? 13 : 7;
        SEQ.add(new Seq(p.getUUID(), total, (pl, rem) -> {
            if (!AbilityEngine.isOn(pl, powerId)) return true;
            ServerLevel l = pl.serverLevel();
            if (rem > holdUntil) {
                pl.fallDistance = 0;
            } else if (rem > spawnAt) {
                Vec3 cur = pl.getDeltaMovement();
                pl.setDeltaMovement(cur.x * 0.2, 0.015, cur.z * 0.2);
                pl.hurtMarked = true;
                pl.fallDistance = 0;
                double form = (holdUntil - rem) / (double) (holdUntil - spawnAt);
                Fx.sprite(l, "flare", rgb, (float) (1.0 + form), spot.add(0, height - form * 1.5, 0), 3, 1.4 - form, 0.01);
            } else if (rem == spawnAt) {
                Projectiles.P h = new Projectiles.P();
                h.kind = Projectiles.Kind.SLAM;
                h.owner = pl.getUUID();
                h.pos = spot.add(0, height, 0);
                h.vel = new Vec3(0, -fall, 0);
                h.maxLife = life;
                h.damage = dmg;
                h.size = size;
                h.knock = kb;
                h.stun = stun;
                h.breakGlass = glass;
                h.rgb = rgb;
                Projectiles.spawn(h);
                l.playSound(null, pl.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8f, 1.4f);
            } else if (rem == 1) {
                pl.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, slowFall, 0, false, false));
            }
            return false;
        }));
    }

    // ------------------------------------------------------------------ chaque tick
    public static void tick(MinecraftServer server) {
        Projectiles.tick(server);
        long now = server.overworld().getGameTime();
        for (Iterator<Later> it = LATER.iterator(); it.hasNext(); ) {
            Later l = it.next();
            if (now < l.at) continue;
            it.remove();
            ServerPlayer p = server.getPlayerList().getPlayer(l.id);
            if (p != null) l.run.accept(p);
        }
        for (Iterator<Seq> it = SEQ.iterator(); it.hasNext(); ) {
            Seq s = it.next();
            int[] left = SEQ_LEFT.computeIfAbsent(s, k -> s.left());
            ServerPlayer p = server.getPlayerList().getPlayer(s.id);
            boolean done = p == null || left[0] <= 0 || s.step.test(p, left[0]);
            left[0]--;
            if (done) {
                SEQ_LEFT.remove(s);
                it.remove();
            }
        }
    }
}
