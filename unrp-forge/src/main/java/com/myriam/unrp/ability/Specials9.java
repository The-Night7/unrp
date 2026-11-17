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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Esprit et chasse (lot 9) : Hypnosis, Predator. Comportements repris de HypnosisEvents et PredatorEvents. Fiches : docs/fidelity/.
 */
public final class Specials9 {
    private Specials9() {}

    private static final Set<String> ALL = Set.of("hy_sleep", "hy_gaze", "hy_deep", "hy_wipe",
            "pd_lock", "pd_focus", "pd_rush", "pd_breaker", "pd_trans");

    public static boolean handles(String s) {
        return s != null && ALL.contains(s);
    }

    private static final Map<UUID, int[]> CC = new HashMap<>();                 // Control state: type (sleep, gaze or deep hold), then remaining ticks.
    private static final Map<UUID, UUID> CHANNEL = new HashMap<>();
    private static final Map<UUID, Integer> FOCUS = new HashMap<>();
    private static final Map<UUID, UUID> LOCK = new HashMap<>();

    private static final java.util.Set<UUID> FOCUS_TOLD = new java.util.HashSet<>(), TRANS_TOLD = new java.util.HashSet<>();
    private static final Map<UUID, UUID> RUSH_TARGET = new HashMap<>();
    private static final Map<UUID, Long> RUSH_ARMED = new HashMap<>();
    private static final Set<UUID> FOCUSING = new HashSet<>();

    private static final class Wave {
        UUID owner;
        Vec3 pos, dir;
        float damage;
        int step;
        final Set<UUID> hit = new HashSet<>();
    }

    private static final List<Wave> WAVES = new ArrayList<>();

    public static void onLogout(UUID id) {
        CC.remove(id);
        for (Map<UUID, ?> m : List.of(CHANNEL, FOCUS, LOCK, RUSH_TARGET, RUSH_ARMED)) m.remove(id);
        FOCUSING.remove(id);
    }

    // ------------------------------------------------------------------ outils

    private static int lvl(ServerPlayer p) {
        return (int) Math.round(ClaimData.get(p.server).profileOf(p.getUUID()).map(pr -> pr.level * 10).orElse(11.0));
    }

    private static float sf(ServerPlayer p) {
        return Math.max(0f, Math.min(1f, (lvl(p) - 11) / 89f));
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

    private static LivingEntity rayTarget(ServerPlayer p, double range) {
        ServerLevel lv = p.serverLevel();
        Vec3 eye = p.getEyePosition(), end = eye.add(p.getViewVector(1f).scale(range));
        var bh = lv.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        if (bh.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) end = bh.getLocation();
        LivingEntity best = null;
        double bd = Double.MAX_VALUE;
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(eye, end).inflate(1.0), e -> e != p && e.isAlive())) {
            var c = e.getBoundingBox().inflate(0.3).clip(eye, end);
            if (c.isPresent() && eye.distanceToSqr(c.get()) < bd) {
                bd = eye.distanceToSqr(c.get());
                best = e;
            }
        }
        return best;
    }

    private static LivingEntity coneTarget(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f);
        LivingEntity best = null;
        double bestDot = 0.985;
        for (LivingEntity e : p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(range), e -> e != p && e.isAlive())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            double d = to.length();
            if (d > range || d < 0.5) continue;
            double dot = to.normalize().dot(look) + Math.min(0.012, e.getBbWidth() * 0.4 / Math.max(4.0, d));
            if (dot > bestDot) {
                bestDot = dot;
                best = e;
            }
        }
        return best;
    }

    private static float resist(ServerPlayer caster, LivingEntity target) {
        int diff = (target instanceof ServerPlayer sp ? lvl(sp) : 0) - lvl(caster);
        return diff >= 15 ? 0.55f : diff <= -15 ? 1.25f : 1.0f;
    }

    private static void addCc(LivingEntity t, int type, int ticks) {
        if (ticks >= 1) CC.put(t.getUUID(), new int[] {type, ticks});
    }

    // ------------------------------------------------------------------ utilisations
    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m) {
        ServerLevel lv = p.serverLevel();
        long now = lv.getGameTime();
        UUID id = p.getUUID();
        float t = sf(p);
        switch (m.special) {
            case "hy_sleep" -> {
                double radius = lerp(6.5f, 10f, t);
                int base = (int) lerp(60f, 110f, t);
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(radius), e -> e != p && e.isAlive() && e.distanceToSqr(p) <= radius * radius)) {
                    addCc(e, 1, (int) (base * resist(p, e)));
                }
                Fx.wave(lv, p.position().add(0, 1, 0), radius, 0xB27CFF, 14);
                lv.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f, 0.6f);
            }
            case "hy_gaze" -> {
                LivingEntity tgt = rayTarget(p, lerp(16f, 26f, t));
                if (tgt != null) {
                    addCc(tgt, 2, (int) (lerp(80f, 140f, t) * resist(p, tgt)));
                    Fx.sprite(lv, "glyph", 0xB27CFF, 1.4f, tgt.getBoundingBox().getCenter(), 14, 0.4, 0.05);
                }
            }
            case "hy_wipe" -> {
                LivingEntity tgt = rayTarget(p, lerp(6f, 12f, t));
                if (tgt != null) {
                    tgt.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) lerp(20f, 40f, t), 0, false, true));
                    tgt.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0, false, true));
                    tgt.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
                    Fx.sprite(lv, "smoke", 0xB27CFF, 1.4f, tgt.getBoundingBox().getCenter(), 16, 0.4, 0.03);
                }
            }
            case "hy_deep" -> { // Deep hold prevents other actions while maintained.
                double range = lerp(14f, 22f, t);
                int windup = (int) lerp(20f, 10f, t);
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6, 1, false, false));
                LivingEntity tgt = rayTarget(p, range);
                if (tgt == null) {
                    CHANNEL.remove(id);
                    FOCUS.put(id, 0);
                } else if (tgt.getUUID().equals(CHANNEL.get(id))) {
                    int fc = FOCUS.getOrDefault(id, 0) + 4;
                    FOCUS.put(id, fc);
                    if (fc >= windup) {
                        addCc(tgt, 3, 8);
                        Vec3 a = p.getEyePosition().add(0, -0.3, 0), b = tgt.getBoundingBox().getCenter();
                        Fx.line(lv, a, b, 0xB27CFF, 0, 0.12f, 5);
                    }
                } else {
                    CHANNEL.put(id, tgt.getUUID());
                    FOCUS.put(id, 0);
                }
            }
            // ---------------- Predator
            case "pd_lock" -> {
                double range = Math.min(60.0, 40.0 + lvl(p) * 0.15 + (AbilityEngine.toggled(p, "predator", 4) ? 5 : 0));
                LivingEntity hit = coneTarget(p, range);
                if (hit == null) {
                    msg(p, "§6[Predator] §7No prey in sight.");
                    return false;
                }
                LOCK.put(id, hit.getUUID());
                msg(p, "§6[Predator] §fTarget locked: " + hit.getName().getString());
                com.myriam.unrp.pass.PredatorPass.lock(p, lv, hit); // PD_LOCK
            }
            case "pd_focus" -> {
                if (FOCUSING.add(id)) AbilityEngine.spendAura(p, 10);
            }
            case "pd_rush" -> {
                LivingEntity target = lockedTarget(p);
                double maxDash = 20.0 + lvl(p) * 0.05;
                if (target != null && p.distanceTo(target) <= maxDash) {
                    Vec3 dir = target.position().subtract(p.position()).normalize();
                    p.setDeltaMovement(dir.x * 1.7, Math.max(0.25, dir.y * 1.2 + 0.15), dir.z * 1.7);
                    RUSH_ARMED.put(id, now + 40);
                    RUSH_TARGET.put(id, target.getUUID());
                    com.myriam.unrp.pass.PredatorPass.rush(p, lv, dir, true);
                } else {
                    Vec3 look = p.getViewVector(1f);
                    p.setDeltaMovement(look.x * 1.25, 0.4, look.z * 1.25);
                    com.myriam.unrp.pass.PredatorPass.rush(p, lv, look, false);
                }
                p.hurtMarked = true;
            }
            case "pd_breaker" -> {
                Vec3 look = p.getViewVector(1f);
                Vec3 flat = new Vec3(look.x, 0, look.z);
                if (flat.lengthSqr() < 0.01) return false;
                flat = flat.normalize();
                Wave w = new Wave();
                w.owner = id;
                w.pos = p.position().add(flat.scale(1.2));
                w.dir = flat;
                w.damage = 5f + lvl(p) * 0.04f;
                WAVES.add(w);
                com.myriam.unrp.pass.PredatorPass.breaker(p, lv, w.pos, w.dir);
            }
            case "pd_trans" -> { }
            default -> { }
        }
        return true;
    }

    private static LivingEntity lockedTarget(ServerPlayer p) {
        UUID tid = LOCK.get(p.getUUID());
        return tid != null && p.serverLevel().getEntity(tid) instanceof LivingEntity le && le.isAlive() ? le : null;
    }

    public static void stop(ServerPlayer p, String special) {
        UUID id = p.getUUID();
        if ("hy_deep".equals(special)) {
            CHANNEL.remove(id);
            FOCUS.remove(id);
        } else if ("pd_focus".equals(special)) FOCUSING.remove(id);
    }

    public static void whileOn(ServerPlayer p, AbilityPower.Move m, long now) { }

    // ------------------------------------------------------------------ chaque tick
    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        for (Iterator<Map.Entry<UUID, int[]>> it = CC.entrySet().iterator(); it.hasNext(); ) {
            var en = it.next();
            LivingEntity le = null;
            for (ServerLevel lv : server.getAllLevels()) {
                if (lv.getEntity(en.getKey()) instanceof LivingEntity f) {
                    le = f;
                    break;
                }
            }
            if (le == null || !le.isAlive()) {
                it.remove();
                continue;
            }
            int[] st = en.getValue();
            le.setDeltaMovement(0, Math.min(le.getDeltaMovement().y, 0), 0);
            le.hurtMarked = true;
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 6, false, false));
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 8, 2, false, false));
            le.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 8, 3, false, false));
            if (st[0] == 1) le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 8, 0, false, false));
            le.addEffect(new MobEffectInstance(MobEffects.GLOWING, 8, 0, false, false));
            if (le.tickCount % 10 == 0 && le.level() instanceof ServerLevel l) {
                Fx.sprite(l, "glyph", 0xB27CFF, 0.9f, le.position().add(0, le.getBbHeight() + 0.2, 0), 1, 0.2, 0);
            }
            if (--st[1] <= 0) it.remove();
        }
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            UUID id = p.getUUID();
            ServerLevel lv = p.serverLevel();
            boolean pd = has(p, "predator");
            if (!pd || !AbilityEngine.isOn(p, "predator")) {
                if (FOCUS_TOLD.remove(id) | TRANS_TOLD.remove(id)) com.myriam.unrp.pass.PassSync.predator(p, -1, false);
            }
            if (pd && AbilityEngine.isOn(p, "predator")) {
                boolean focusing = FOCUSING.contains(id);
                double focusR = Math.round(24.0 + lvl(p) * 0.15);
                if (focusing && FOCUS_TOLD.add(id)) com.myriam.unrp.pass.PredatorPass.tell(p, lv, com.myriam.unrp.pass.PredatorPass.packet(35, p, 0, 0, 0, p.position(), null, focusR));
                else if (focusing && p.tickCount % 20 == 0) com.myriam.unrp.pass.PredatorPass.send(lv, com.myriam.unrp.pass.PredatorPass.packet(35, p, 0, 0, 32, p.position(), null, focusR));
                else if (!focusing) FOCUS_TOLD.remove(id);
                com.myriam.unrp.pass.PredatorPass.pace(p, focusing);
                boolean trans = AbilityEngine.toggled(p, "predator", 4);
                if (trans && TRANS_TOLD.add(id)) {
                    com.myriam.unrp.pass.PredatorPass.tell(p, lv, com.myriam.unrp.pass.PredatorPass.packet(40, p, 0, 0, 0, p.position(), null, 0.0)); // PD_ROAR
                    Poses.play(p, "pd_roar");
                } else if (!trans) TRANS_TOLD.remove(id);
                if (p.tickCount % 40 == 11) com.myriam.unrp.pass.PredatorPass.beat(p, lv, focusing ? 24.0 : 12.0, focusing);
                LivingEntity lk = lockedTarget(p);
                com.myriam.unrp.pass.PassSync.predator(p, lk == null ? -1 : lk.getId(), trans);
            }
            if (!pd) {
                LOCK.remove(id);
                RUSH_ARMED.remove(id);
                RUSH_TARGET.remove(id);
                FOCUSING.remove(id);
                continue;
            }

            Long armed = RUSH_ARMED.get(id);
            UUID rt = RUSH_TARGET.get(id);
            if (armed != null && rt != null) {
                if (now > armed) {
                    RUSH_ARMED.remove(id);
                    RUSH_TARGET.remove(id);
                } else if (lv.getEntity(rt) instanceof LivingEntity rle && rle.isAlive() && p.distanceTo(rle) <= 2.5) {
                    rle.hurt(lv.damageSources().playerAttack(p), Math.min(14f, 8f + lvl(p) * 0.08f));
                    rle.knockback(1.2, p.getX() - rle.getX(), p.getZ() - rle.getZ());
                    rle.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, true));
                    rle.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, false, true));
                    p.setDeltaMovement(p.getDeltaMovement().scale(0.2));
                    p.hurtMarked = true;
                    RUSH_ARMED.remove(id);
                    RUSH_TARGET.remove(id);
                    com.myriam.unrp.pass.PredatorPass.rushHit(p, lv, rle); // PD_RUSH_HIT
                }
            }

            if (p.tickCount % 20 == 7 && LOCK.containsKey(id)) {
                LivingEntity lt = lockedTarget(p);
                if (lt == null) LOCK.remove(id);
                else if (p.distanceTo(lt) > 60) {
                    LOCK.remove(id);
                    msg(p, "§7Mark lost: target out of range");
                    lt.addEffect(new MobEffectInstance(MobEffects.GLOWING, 45, 0, false, false));
                }
            }
            // vision de chasseur
            if (FOCUSING.contains(id) && p.tickCount % 10 == 4) {
                double radius = 24.0 + lvl(p) * 0.15;
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(radius), e -> e != p && e.isAlive())) {
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 15, 0, false, false));
                }
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 15, 1, false, false));
            }
        }
        for (Iterator<Wave> it = WAVES.iterator(); it.hasNext(); ) {
            Wave w = it.next();
            ServerPlayer owner = server.getPlayerList().getPlayer(w.owner);
            if (owner == null || ++w.step > 12) {
                it.remove();
                continue;
            }
            ServerLevel lv = owner.serverLevel();
            Vec3 next = w.pos.add(w.dir);
            BlockPos bp = BlockPos.containing(next);
            if (!lv.getBlockState(bp.below()).isAir()) {
                if (!lv.getBlockState(bp).isAir()) {
                    if (!lv.getBlockState(bp.above()).isAir() || lv.getBlockState(bp.above().below()).isAir()) {
                        it.remove();
                        continue;
                    }
                    next = next.add(0, 1, 0);
                }
            } else {
                if (lv.getBlockState(bp.below(2)).isAir()) {
                    it.remove();
                    continue;
                }
                next = next.add(0, -1, 0);
            }
            w.pos = next;
            for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(next.x - 1.6, next.y - 0.5, next.z - 1.6, next.x + 1.6, next.y + 2.0, next.z + 1.6), e -> e != owner && e.isAlive())) {
                if (!w.hit.add(e.getUUID())) continue;
                e.hurt(lv.damageSources().playerAttack(owner), w.damage);
                e.setDeltaMovement(e.getDeltaMovement().x, 0.55, e.getDeltaMovement().z);
                e.hurtMarked = true;
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
            }
        }
    }

    public static void onAttack(LivingAttackEvent e) {
        if (e.getSource().getEntity() != null && CC.containsKey(e.getSource().getEntity().getUUID())) e.setCanceled(true);
    }

    public static void onHurt(LivingHurtEvent e) {
        LivingEntity v = e.getEntity();
        int[] st = CC.get(v.getUUID());
        if (st != null) {
            float a = e.getAmount();
            if (st[0] == 1 && a >= 6f) CC.remove(v.getUUID());
            else if (st[0] == 2 && a >= 8f) st[1] /= 2;
            else if (st[0] == 3 && a >= 12f) CC.remove(v.getUUID());
        }
        if (e.getSource().getEntity() instanceof ServerPlayer a && has(a, "predator")) {
            UUID tid = LOCK.get(a.getUUID());
            if (tid != null && tid.equals(v.getUUID())) {
                e.setAmount(e.getAmount() * Math.min(1.5f, 1.2f + lvl(a) * 0.002f));
                Long armed = RUSH_ARMED.remove(a.getUUID());
                if (armed != null && a.serverLevel().getGameTime() <= armed) {
                    e.setAmount(e.getAmount() + 6f);
                    v.knockback(1.2, a.getX() - v.getX(), a.getZ() - v.getZ());
                    v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, true));
                    v.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1, false, true));
                    com.myriam.unrp.pass.PredatorPass.rushHit(a, a.serverLevel(), v);
                }
            }
        }
    }
}
