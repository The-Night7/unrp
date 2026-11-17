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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Specials8 {
    private Specials8() {}

    private static final Set<String> ALL = Set.of("cd_shield", "cd_launch", "cd_hover",
            "cv_lash", "cv_entangle", "cv_spikes", "cv_grasp",
            "sk2_shot", "sk2_volley", "sk2_redirect", "sk2_field");

    public static boolean handles(String s) {
        return s != null && ALL.contains(s);
    }

    private static final Map<UUID, List<Integer>> REFORM = new HashMap<>();
    private static final Set<UUID> FLYING = new HashSet<>();
    private static final Set<UUID> LASH = new HashSet<>();
    private static final Map<UUID, UUID> LASH_GRAB = new HashMap<>();
    private static final Map<UUID, Long> GRASP_START = new HashMap<>();
    private static final Map<UUID, Vec3> GRASP_AT = new HashMap<>();
    private static final Map<UUID, Long> PRISON_UNTIL = new HashMap<>(), PRISON_CD = new HashMap<>();
    private static final Map<UUID, Vec3> PRISON_AT = new HashMap<>();
    private static final Map<UUID, int[]> ROW = new HashMap<>();
    private static final Map<UUID, Vec3> ROW_FROM = new HashMap<>(), ROW_DIR = new HashMap<>();
    private static final Map<UUID, long[]> FIELD = new HashMap<>();

    private static final class Spike {
        UUID owner;
        Vec3 pos, vel, aim;
        int mode;
        float damage, size, launch;
        int delay, stun, hold, life;
        boolean erupted;
        int rgb = 0x6B8E4E;
    }

    private static final List<Spike> SPIKES = new ArrayList<>();

    public static void onLogout(UUID id) {
        for (Map<UUID, ?> m : List.of(REFORM, LASH_GRAB, GRASP_START, GRASP_AT, PRISON_UNTIL, PRISON_AT, ROW, ROW_FROM, ROW_DIR, FIELD)) m.remove(id);
        FLYING.remove(id);
        LASH.remove(id);
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

    private static Vec3 aimPoint(ServerPlayer p, double range, boolean fallbackEnd) {
        Vec3 from = p.getEyePosition(), to = from.add(p.getViewVector(1f).scale(range));
        HitResult h = p.serverLevel().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        return h.getType() == HitResult.Type.MISS ? (fallbackEnd ? to : null) : h.getLocation();
    }

    private static Vec3 surfaceAt(ServerLevel lv, Vec3 near, double down) {
        HitResult h = lv.clip(new ClipContext(new Vec3(near.x, near.y + 2, near.z), new Vec3(near.x, near.y - down, near.z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        if (h.getType() == HitResult.Type.MISS) return null;
        return new Vec3(near.x, Math.floor(h.getLocation().y - 0.05) + 1.0, near.z);
    }

    private static void hit(ServerPlayer p, LivingEntity t, float dmg) {
        t.hurt(p.serverLevel().damageSources().playerAttack(p), dmg);
    }

    private static void bind(LivingEntity t, int ticks, int slowAmp) {
        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, slowAmp, false, false));
        t.addEffect(new MobEffectInstance(MobEffects.JUMP, ticks, 128, false, false));
        t.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false));
        if (t instanceof ServerPlayer sp) sp.setSprinting(false);
    }

    private static void spike(ServerPlayer p, Vec3 pos, float dmg, float size, int delay, float launch, int rgb) {
        Spike s = new Spike();
        s.owner = p.getUUID();
        s.pos = pos;
        s.damage = dmg;
        s.size = size;
        s.delay = delay;
        s.launch = launch;
        s.hold = 24;
        s.rgb = rgb;
        SPIKES.add(s);
    }

    private static boolean ground(ServerPlayer p, int refund) {
        if (p.onGround()) return true;
        msg(p, "§cSpikes require solid ground beneath you.");
        return false;
    }

    // ------------------------------------------------------------------ Conjure Disks
    private static int maxDisks(ServerPlayer p) {
        double lvl10 = ClaimData.get(p.server).profileOf(p.getUUID()).map(pr -> pr.level * 10).orElse(11.0);
        return lvl10 < 30 ? 3 : lvl10 < 60 ? 4 : lvl10 < 85 ? 5 : 6;
    }

    private static void launchDisk(ServerPlayer p, ServerLevel lv) {
        UUID id = p.getUUID();
        int max = maxDisks(p);
        int reserved = (AbilityEngine.toggled(p, "conjure_disks", 0) ? 2 : 0) + (AbilityEngine.toggled(p, "conjure_disks", 2) ? 1 : 0);
        int reforming = REFORM.containsKey(id) ? REFORM.get(id).size() : 0;
        int available = max - reserved - reforming;
        if (available < 1) {
            msg(p, "§b[Discs] §7No free disc (" + Math.max(0, available) + "/" + max + ").");
            return;
        }
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f).normalize();
        Vec3 end = eye.add(look.scale(28));
        var bc = lv.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        double maxDist = bc.getType() == HitResult.Type.BLOCK ? bc.getLocation().distanceTo(eye) : 28;
        LivingEntity target = null;
        double bestProj = maxDist;
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(eye, end).inflate(1.0), e -> e.isAlive() && e != p)) {
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye);
            double proj = to.dot(look);
            if (proj <= 0 || proj > maxDist) continue;
            if (to.subtract(look.scale(proj)).length() > 0.8 + e.getBbWidth() * 0.5 || proj >= bestProj) continue;
            bestProj = proj;
            target = e;
        }
        Fx.line(lv, eye.add(look.scale(0.6)).add(0, -0.3, 0), eye.add(look.scale(target != null ? bestProj : maxDist)), 0xE6C65A, 0, 0.2f, 6);
        if (target != null) {
            hit(p, target, lerp(7f, 11f, sf(p)));
            target.knockback(0.9, -look.x, -look.z);
            target.hurtMarked = true;
            Fx.sprite(lv, "shard", 0xE6C65A, 1.0f, target.getBoundingBox().getCenter(), 8, 0.3, 0.1);
        }
        REFORM.computeIfAbsent(id, k -> new ArrayList<>()).add((int) lerp(280f, 180f, sf(p)));
        msg(p, "§b[Disques] §7" + (available - 1) + "/" + max + " libres");
        lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.8f);
    }

    // ------------------------------------------------------------------ utilisations
    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m) {
        ServerLevel lv = p.serverLevel();
        long now = lv.getGameTime();
        UUID id = p.getUUID();
        Vec3 look = p.getViewVector(1f);
        int rgbVine = 0x6B8E4E;
        switch (m.special) {
            case "cd_launch" -> launchDisk(p, lv);
            case "cd_shield", "cd_hover" -> { }
            // ---------------- Conjure Vines
            case "cv_lash" -> {
                if (LASH.add(id)) AbilityEngine.spendAura(p, 4);
            }
            case "cv_entangle" -> {
                if (p.isShiftKeyDown()) {
                    if (now < PRISON_CD.getOrDefault(id, 0L)) {
                        msg(p, "§a✖ Thorn Prison is cooling down (" + (1 + (PRISON_CD.get(id) - now) / 20) + " s)");
                        return false;
                    }
                    AbilityEngine.spendAura(p, 26);
                    PRISON_CD.put(id, now + 400);
                    Vec3 aim = aimPoint(p, 16, true);
                    Vec3 center = surfaceAt(lv, aim, 8);
                    if (center == null) center = aim;
                    PRISON_UNTIL.put(id, now + 200);
                    PRISON_AT.put(id, center);
                    for (int i = 0; i < 10; i++) {
                        double a = Math.PI * 2 * i / 10;
                        Vec3 base = surfaceAt(lv, center.add(Math.cos(a) * 4.5, 0, Math.sin(a) * 4.5), 8);
                        if (base != null) {
                            Spike s = new Spike();
                            s.owner = id;
                            s.pos = base;
                            s.damage = 0;
                            s.size = 1.3f;
                            s.delay = 3 + i % 3 * 2;
                            s.hold = 210;
                            s.rgb = rgbVine;
                            SPIKES.add(s);
                        }
                    }
                } else {
                    Vec3 aim = aimPoint(p, 18, true);
                    boolean any = false;
                    for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(aim.x - 3, aim.y - 2, aim.z - 3, aim.x + 3, aim.y + 3, aim.z + 3), e -> e != p && e.isAlive())) {
                        bind(e, 100, 3);
                        Fx.sprite(lv, "leaf", rgbVine, 1.2f, e.getBoundingBox().getCenter(), 14, 0.5, 0.05);
                        any = true;
                    }
                    if (!any) msg(p, "§aThe vines find nothing to bind.");
                }
            }
            case "cv_spikes" -> {
                if (p.isShiftKeyDown()) {
                    Vec3 aim = aimPoint(p, 20, true);
                    for (int i = 0; i < 4; i++) {
                        double a = Math.PI * 2 * i / 4 + 0.4, r = i == 0 ? 0 : 1.4;
                        Vec3 base = surfaceAt(lv, aim.add(Math.cos(a) * r, 0, Math.sin(a) * r), 8);
                        if (base != null) {
                            Spike s = new Spike();
                            s.owner = id;
                            s.pos = base;
                            s.damage = 7;
                            s.size = 1.1f;
                            s.delay = 6 + i * 2;
                            s.stun = 30;
                            s.hold = 24;
                            s.rgb = rgbVine;
                            SPIKES.add(s);
                        }
                    }
                } else {
                    for (int i = 0; i < 5; i++) {
                        double jitter = (p.getRandom().nextDouble() - 0.5) * 8;
                        double rad = Math.toRadians(-16 + i * 8 + jitter), c = Math.cos(rad), sn = Math.sin(rad);
                        Vec3 dir = new Vec3(look.x * c - look.z * sn, look.y, look.x * sn + look.z * c);
                        flying(p, p.getEyePosition().add(dir.scale(0.8)).add(0, -0.2, 0), dir.scale(1.3), dir, 4f, 0.7f, rgbVine);
                    }
                }
            }
            case "cv_grasp" -> {
                if (!GRASP_START.containsKey(id)) {
                    AbilityEngine.spendAura(p, 6);
                    GRASP_START.put(id, now);
                    Vec3 aim = aimPoint(p, 16, true);
                    Vec3 c = surfaceAt(lv, aim, 8);
                    GRASP_AT.put(id, c == null ? aim : c);
                }
            }
            // ---------------- Spikes
            case "sk2_shot" -> {
                if (!ground(p, 10)) return false;
                ROW.put(id, new int[] {8});
                ROW_FROM.put(id, p.position());
                ROW_DIR.put(id, flat(look));
            }
            case "sk2_volley" -> {
                if (!ground(p, 18)) return false;
                Vec3 aim = aimPoint(p, 22, false);
                Vec3 center = aim == null ? null : surfaceAt(lv, aim, 6);
                if (center == null) {
                    msg(p, "§cNo ground for the spikes to emerge from.");
                    return false;
                }
                for (int i = 0; i < 5; i++) {
                    double a = p.getRandom().nextDouble() * Math.PI * 2, r = 0.8 + p.getRandom().nextDouble() * 1.4;
                    Vec3 base = surfaceAt(lv, center.add(Math.cos(a) * r, 0, Math.sin(a) * r), 6);
                    if (base != null) spike(p, base, 3.5f, 0.7f, 4 + i * 3, 0.4f, 0x8A8A94);
                }
            }
            case "sk2_redirect" -> {
                if (!ground(p, 22)) return false;
                Vec3 fl = flat(look);
                for (int i = 0; i < 4; i++) {
                    double side = (i % 2 == 0 ? 1 : -1) * (0.7 + 0.35 * (i / 2));
                    Vec3 perp = new Vec3(-fl.z, 0, fl.x).scale(side);
                    Vec3 pos = p.position().add(fl.scale(1.2)).add(perp.scale(1.6)).add(0, 0.2, 0);
                    flying(p, pos, new Vec3(perp.x * 0.18, 0.5, perp.z * 0.18), look, 4.5f, 0.8f, 0x8A8A94);
                }
                for (int i = 0; i < 2; i++) {
                    Vec3 perp = new Vec3(-fl.z, 0, fl.x).scale(i == 0 ? 0.3 : -0.3);
                    Vec3 pos = p.position().add(fl.scale(1.5)).add(perp).add(0, 0.2, 0);
                    flying(p, pos, new Vec3(perp.x * 0.1, 0.55, perp.z * 0.1), look, 4.5f, 0.8f, 0x8A8A94);
                }
            }
            case "sk2_field" -> {
                if (!ground(p, 34)) return false;
                if (p.isShiftKeyDown()) {
                    AbilityEngine.spendAura(p, 14);
                    Vec3 fl = flat(look), origin = p.position().add(fl.scale(2.5));
                    for (int i = 0; i < 9; i++) {
                        Vec3 perp = new Vec3(-fl.z, 0, fl.x).scale((i / 3 - 1) * 1.5);
                        Vec3 base = surfaceAt(lv, origin.add(fl.scale(0.5 + (i % 3) * 1.6)).add(perp), 6);
                        if (base != null) spike(p, base, 9f, 1.4f, 2 + i % 3 * 2, 0.8f, 0x8A8A94);
                    }
                } else {
                    Vec3 aim = aimPoint(p, 20, false);
                    Vec3 center = aim == null ? null : surfaceAt(lv, aim, 6);
                    if (center == null) {
                        msg(p, "§cNo ground for the spikes to emerge from.");
                        return false;
                    }
                    FIELD.put(id, new long[] {now + 160, now, Double.doubleToLongBits(center.x), Double.doubleToLongBits(center.y), Double.doubleToLongBits(center.z)});
                }
            }
            default -> { }
        }
        return true;
    }

    private static void flying(ServerPlayer p, Vec3 pos, Vec3 vel, Vec3 aim, float dmg, float size, int rgb) {
        Spike s = new Spike();
        s.owner = p.getUUID();
        s.mode = 1;
        s.pos = pos;
        s.vel = vel;
        s.aim = aim;
        s.damage = dmg;
        s.size = size;
        s.rgb = rgb;
        SPIKES.add(s);
    }

    public static void whileOn(ServerPlayer p, AbilityPower.Move m, long now) {
        UUID id = p.getUUID();
        ServerLevel lv = p.serverLevel();
        if ("cd_hover".equals(m.special) && !p.isCreative() && !p.isSpectator()) {
            if (FLYING.add(id)) {
                p.setDeltaMovement(p.getDeltaMovement().x, 0.4, p.getDeltaMovement().z);
                p.hurtMarked = true;
            }
            Pal.flight(p, FLIGHT_ID, "hover_flight", 0.45, true);
            p.fallDistance = 0;
        }
    }

    public static void stop(ServerPlayer p, String special) {
        UUID id = p.getUUID();
        switch (special) {
            case "cv_lash" -> {
                LASH.remove(id);
                LASH_GRAB.remove(id);
            }
            case "cv_grasp" -> {
                GRASP_START.remove(id);
                GRASP_AT.remove(id);
            }
            case "cd_hover" -> revokeFlight(p);
            default -> { }
        }
    }

    private static final UUID FLIGHT_ID = UUID.fromString("5a1f3c2e-8d47-4b91-a6e2-0f9d1c7b3e21"); // hover_flight

    private static void revokeFlight(ServerPlayer p) {
        Pal.flight(p, FLIGHT_ID, "hover_flight", 0.45, false);
        if (FLYING.remove(p.getUUID()) && !p.isCreative() && !p.isSpectator()) {
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, false, false));
        }
    }

    // ------------------------------------------------------------------ chaque tick
    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        for (Iterator<Map.Entry<UUID, List<Integer>>> it = REFORM.entrySet().iterator(); it.hasNext(); ) {
            var en = it.next();
            List<Integer> l = en.getValue();
            for (int i = l.size() - 1; i >= 0; i--) {
                int t = l.get(i) - 1;
                if (t <= 0) l.remove(i);
                else l.set(i, t);
            }
            if (l.isEmpty()) it.remove();
        }
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            UUID id = p.getUUID();
            ServerLevel lv = p.serverLevel();
            // Conjure Disks : bouclier orbital, vol
            boolean cd = has(p, "conjure_disks");
            if (cd && AbilityEngine.toggled(p, "conjure_disks", 0) && p.tickCount % 3 == 0) {
                for (int k = 0; k < 2; k++) {
                    double a = p.tickCount * 0.35 + k * Math.PI;
                    Fx.sprite(lv, "shard", 0xE6C65A, 0.9f, p.position().add(Math.cos(a) * 1.3, 1.0, Math.sin(a) * 1.3), 1, 0.02, 0);
                }
                for (Projectile pr : lv.getEntitiesOfClass(Projectile.class, p.getBoundingBox().inflate(2.5), x -> x.isAlive() && x.getOwner() != p)) {
                    Vec3 toProj = pr.position().subtract(p.getEyePosition()).normalize();
                    if (p.getViewVector(1f).dot(toProj) < 0.25 || AbilityEngine.aura(p) < 3) continue;
                    pr.setDeltaMovement(toProj.scale(-0.4).add(0, 0.25, 0));
                    pr.hurtMarked = true;
                    AbilityEngine.spendAura(p, 3);
                    Fx.sprite(lv, "glint", 0xE6C65A, 1.0f, pr.position(), 6, 0.2, 0.05);
                }
            }
            if (FLYING.contains(id) && (!cd || !AbilityEngine.toggled(p, "conjure_disks", 2))) revokeFlight(p);

            if (LASH.contains(id)) {
                if (!has(p, "conjure_vines")) {
                    LASH.remove(id);
                    LASH_GRAB.remove(id);
                } else lash(p, lv);
            }
            Long gs = GRASP_START.get(id);
            if (gs != null) {
                if (!has(p, "conjure_vines")) GRASP_START.remove(id);
                else {
                    long gt = now - gs;
                    double radius = Math.min(6.0, 2.0 + gt * 0.05);
                    Vec3 c = GRASP_AT.get(id);
                    if (gt % 8 == 0) {
                        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(c.x - radius, c.y - 1.5, c.z - radius, c.x + radius, c.y + 2.5, c.z + radius), e -> e != p && e.isAlive())) {
                            Vec3 d = e.position().subtract(c);
                            if (Math.sqrt(d.x * d.x + d.z * d.z) > radius) continue;
                            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 24, 3, false, false));
                            e.addEffect(new MobEffectInstance(MobEffects.JUMP, 24, 128, false, false));
                            hit(p, e, 1f);
                            if (p.getRandom().nextFloat() < 0.45f) bind(e, 30, 4);
                        }
                    }
                    if (gt % 4 == 0) {
                        for (int i = 0; i < 6; i++) {
                            double a = p.getRandom().nextDouble() * Math.PI * 2, r = Math.sqrt(p.getRandom().nextDouble()) * radius;
                            Fx.sprite(lv, "leaf", 0x6B8E4E, 0.9f, c.add(Math.cos(a) * r, 0.2, Math.sin(a) * r), 1, 0.1, 0);
                        }
                    }
                }
            }
            Long pu = PRISON_UNTIL.get(id);
            if (pu != null) {
                Vec3 c = PRISON_AT.get(id);
                if (now >= pu) {
                    PRISON_UNTIL.remove(id);
                    PRISON_AT.remove(id);
                } else {
                    for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(c.x - 5.5, c.y - 1.5, c.z - 5.5, c.x + 5.5, c.y + 4, c.z + 5.5), e -> e != p && e.isAlive())) {
                        Vec3 d = e.position().subtract(c);
                        double dist = Math.sqrt(d.x * d.x + d.z * d.z);
                        if (dist > 3.6 && dist < 5.5) {
                            Vec3 in = new Vec3(-d.x, 0, -d.z).normalize();
                            e.setDeltaMovement(in.x * 0.35, Math.max(e.getDeltaMovement().y, 0.05), in.z * 0.35);
                            e.hurtMarked = true;
                            if (now % 10 == 0) hit(p, e, 1.5f);
                        } else if (dist <= 3.6 && now % 40 == 0) bind(e, 45, 3);
                    }
                }
            }

            int[] row = ROW.get(id);
            if (row != null) {
                int idx = 8 - row[0];
                row[0]--;
                Vec3 f = ROW_FROM.get(id), d = ROW_DIR.get(id);
                Vec3 at = new Vec3(f.x + d.x * (1.6 + idx * 1.35), p.getY() + 1.0, f.z + d.z * (1.6 + idx * 1.35));
                Vec3 base = surfaceAt(lv, at, 6);
                if (base != null) spike(p, base, 4.5f, (float) (0.9 + idx * 0.09), 0, 0.55f, 0x8A8A94);
                if (row[0] <= 0) {
                    ROW.remove(id);
                    ROW_FROM.remove(id);
                    ROW_DIR.remove(id);
                }
            }
            long[] fd = FIELD.get(id);
            if (fd != null) {
                if (now >= fd[0] || !has(p, "spikes")) FIELD.remove(id);
                else if (now >= fd[1]) {
                    fd[1] = now + 12;
                    Vec3 c = new Vec3(Double.longBitsToDouble(fd[2]), Double.longBitsToDouble(fd[3]), Double.longBitsToDouble(fd[4]));
                    for (int i = 0; i < 2; i++) {
                        double a = p.getRandom().nextDouble() * Math.PI * 2, r = p.getRandom().nextDouble() * 3.5;
                        Vec3 base = surfaceAt(lv, c.add(Math.cos(a) * r, 0, Math.sin(a) * r), 6);
                        if (base != null) spike(p, base, 3f, 0.8f, 6, 0.4f, 0x8A8A94);
                    }
                }
            }
            attrGround(p, has(p, "spikes") && p.onGround());
        }
        stepSpikes(server);
    }

    private static void attrGround(ServerPlayer p, boolean want) {
        AttributeInstance kb = p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb == null) return;
        UUID mid = UUID.nameUUIDFromBytes("unrp:spikes_ground".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        boolean has = kb.getModifier(mid) != null;
        if (want && !has) kb.addTransientModifier(new AttributeModifier(mid, "spikes_ground_affinity", 0.1, AttributeModifier.Operation.ADDITION));
        else if (!want && has) kb.removeModifier(mid);
    }

    private static void lash(ServerPlayer p, ServerLevel lv) {
        UUID id = p.getUUID();
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f);
        Vec3 end = eye.add(look.scale(14));
        var bh = lv.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        Vec3 free = bh.getType() == HitResult.Type.MISS ? end : bh.getLocation();
        UUID gid = LASH_GRAB.get(id);
        LivingEntity grab = gid == null ? null : lv.getEntity(gid) instanceof LivingEntity le ? le : null;
        if (grab != null && grab.isAlive() && grab.distanceTo(p) < 20) {
            Vec3 hold = eye.add(look.scale(4.2));
            Vec3 ctr = grab.position().add(0, grab.getBbHeight() * 0.5, 0);
            Vec3 pull = hold.subtract(ctr).scale(0.4);
            if (pull.length() > 1.1) pull = pull.normalize().scale(1.1);
            grab.setDeltaMovement(pull);
            grab.hurtMarked = true;
            grab.fallDistance = 0;
            grab.addEffect(new MobEffectInstance(MobEffects.JUMP, 8, 128, false, false));
            if (grab instanceof ServerPlayer sp) sp.setSprinting(false);
            if (p.tickCount % 30 == 0) hit(p, grab, 1.5f);
            Fx.line(lv, eye.add(0, -0.4, 0), ctr, 0x6B8E4E, 0, 0.12f, 4);
            return;
        }
        LASH_GRAB.remove(id);
        LivingEntity cand = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(14), e -> e != p && e.isAlive())) {
            Vec3 rel = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye);
            double along = rel.dot(look);
            if (along < 1 || along > 14 || rel.subtract(look.scale(along)).length() > 0.9 + e.getBbWidth() * 0.5 || along >= best) continue;
            best = along;
            cand = e;
        }
        if (cand != null) {
            LASH_GRAB.put(id, cand.getUUID());
            return;
        }
        Fx.line(lv, eye.add(0, -0.4, 0), free, 0x6B8E4E, 0, 0.1f, 4);
        if (p.tickCount % 12 == 0) {
            for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(free.subtract(1.2, 1.2, 1.2), free.add(1.2, 1.2, 1.2)), e -> e != p && e.isAlive())) {
                hit(p, e, 4f);
                e.setDeltaMovement(e.getDeltaMovement().add(look.x * 0.25, 0.1, look.z * 0.25));
                e.hurtMarked = true;
            }
        }
    }

    private static void stepSpikes(MinecraftServer server) {
        for (Iterator<Spike> it = SPIKES.iterator(); it.hasNext(); ) {
            Spike s = it.next();
            ServerPlayer owner = server.getPlayerList().getPlayer(s.owner);
            if (owner == null) {
                it.remove();
                continue;
            }
            ServerLevel lv = owner.serverLevel();
            s.life++;
            if (s.mode == 0) {
                if (s.life < s.delay) {
                    if (s.life % 2 == 0) Fx.sprite(lv, "rock", s.rgb, 0.6f, s.pos.add(0, 0.1, 0), 2, 0.3, 0.02);
                } else {
                    if (!s.erupted) {
                        s.erupted = true;
                        Fx.sprite(lv, "shard", s.rgb, 1.0f + s.size * 0.4f, s.pos.add(0, 0.8 * s.size, 0), 10, 0.3 * s.size, 0.1);
                        AABB box = new AABB(s.pos.x - 0.9 * s.size, s.pos.y, s.pos.z - 0.9 * s.size, s.pos.x + 0.9 * s.size, s.pos.y + 1.9 * s.size, s.pos.z + 0.9 * s.size);
                        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && !e.getUUID().equals(s.owner))) {
                            if (s.damage > 0) e.hurt(lv.damageSources().playerAttack(owner), s.damage);
                            if (s.stun > 0) {
                                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, s.stun, 5, false, false));
                                e.addEffect(new MobEffectInstance(MobEffects.JUMP, s.stun, 128, false, false));
                            } else if (s.launch > 0.01f) {
                                Vec3 v = e.getDeltaMovement();
                                e.setDeltaMovement(v.x, Math.max(v.y, s.launch), v.z);
                                e.hurtMarked = true;
                            }
                        }
                        lv.playSound(null, net.minecraft.core.BlockPos.containing(s.pos), SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.PLAYERS, 0.8f, 0.8f);
                    }
                    Fx.line(lv, s.pos, s.pos.add(0, 1.9 * s.size, 0), s.rgb, 0, 0.12f * s.size, 3);
                }
                if (s.life > s.delay + s.hold) it.remove();
            } else {
                if (s.life > 50) {
                    it.remove();
                    continue;
                }
                if (s.aim != null) s.vel = s.vel.scale(0.93).add(s.aim.scale(0.11));
                Vec3 to = s.pos.add(s.vel);
                var bh = lv.clip(new ClipContext(s.pos, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
                boolean blocked = bh.getType() != HitResult.Type.MISS;
                Vec3 end = blocked ? bh.getLocation() : to;
                LivingEntity victim = null;
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(s.pos, end).inflate(0.4), e -> e.isAlive() && !e.getUUID().equals(s.owner))) {
                    victim = e;
                    break;
                }
                if (victim != null) victim.hurt(lv.damageSources().playerAttack(owner), s.damage);
                if (victim != null || blocked) {
                    Fx.sprite(lv, "shard", s.rgb, 1.0f, end, 6, 0.2, 0.08);
                    it.remove();
                    continue;
                }
                Fx.line(lv, s.pos, end, s.rgb, 0, 0.09f, 3);
                s.pos = end;
            }
        }
    }

    public static void onHurt(LivingHurtEvent e) {
        if (e.getEntity() instanceof ServerPlayer v && AbilityEngine.toggled(v, "conjure_disks", 0) && has(v, "conjure_disks") && !e.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            e.setAmount(e.getAmount() * (1f - lerp(0.16f, 0.28f, sf(v))));
        }
    }
}
