package com.myriam.unrp.ability;

import com.myriam.unrp.data.AbilityPower;
import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.item.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Specials3 {
    private Specials3() {}

    private static final Set<String> ALL = Set.of("sp_haymaker", "sp_break", "sp_smash", "sk_axe", "sk_sweep", "sk_smash",
            "dz_spin", "dz_sucker", "dz_weave", "st_harden", "st_immovable");

    public static boolean handles(String s) {
        return s != null && ALL.contains(s);
    }

    private static final Map<UUID, Long> HAYMAKER = new HashMap<>(), GUARDBREAK = new HashMap<>(), SPIN = new HashMap<>();
    private static final Map<UUID, Long> HARDENED = new HashMap<>(), GUARD_BROKEN = new HashMap<>(), DIZZY_IMMUNE = new HashMap<>();
    private static final Map<UUID, Long> BURST_IMMUNE = new HashMap<>(), WEAVING = new HashMap<>(), IMMOVABLE = new HashMap<>();
    private static final Map<UUID, Map<UUID, long[]>> WOOZE = new HashMap<>();
    private static final Map<UUID, Integer> PENDING_SMASH = new HashMap<>();   // Strong Punch : ticks avant l'onde
    private static final Set<UUID> NO_FALL = new java.util.HashSet<>();
    private static boolean redirecting;

    private static final class Smash {
        int phase, timer;
        Vec3 dir;
        boolean done;
    }

    private static final Map<UUID, Smash> KICK = new HashMap<>();

    public static void onLogout(UUID id) {
        for (Map<UUID, ?> m : List.of(HAYMAKER, GUARDBREAK, SPIN, HARDENED, GUARD_BROKEN, DIZZY_IMMUNE, BURST_IMMUNE, WEAVING, IMMOVABLE,
                WOOZE, PENDING_SMASH, KICK)) m.remove(id);
        NO_FALL.remove(id);
    }

    // ------------------------------------------------------------------ outils
    private static float level(ServerPlayer p) {
        double l = com.myriam.unrp.player.PlayerStateService.effectiveLevel(p);
        return (float) Math.max(1, Math.min(10, l));
    }

    private static float scale(ServerPlayer p, float lo, float hi) {
        return lo + (hi - lo) * (level(p) - 1f) / 9f;
    }

    private static void msg(ServerPlayer p, String s) {
        p.displayClientMessage(Component.literal(s), true);
    }

    private static int col(AbilityPower power) {
        return power.look != null && power.look.rgb() >= 0 ? power.look.rgb() : 0xFFFFFF;
    }

    private static Vec3 fist(ServerPlayer p) {
        Vec3 look = p.getViewVector(1f);
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize().scale(0.35);
        return p.getEyePosition().add(look.scale(0.7)).add(right).add(0, -0.35, 0);
    }

    private static void motion(LivingEntity e, double x, double y, double z) {
        e.setDeltaMovement(x, y, z);
        e.hurtMarked = true;
    }

    private static void armFx(ServerPlayer p, int rgb, String sprite) {
        Fx.sprite(p.serverLevel(), sprite, rgb, 0.8f, fist(p), 6, 0.12, 0.02);
    }

    static void aoe(ServerPlayer p, double radius, double strength, double damage) {
        ServerLevel lv = p.serverLevel();
        double horiz = strength / 2.0 * 0.08;
        double lift = Math.min(0.4 + strength * 0.015, 1.0);
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(radius), e -> e != p && e.isAlive())) {
            Vec3 d = e.position().subtract(p.position());
            double dist = Math.sqrt(d.x * d.x + d.y * d.y + d.z * d.z);
            if (dist == 0) dist = 1;
            Vec3 cur = e.getDeltaMovement();
            double vy = e.onGround() ? lift : cur.y + d.y / dist * strength / 2.0 * 0.12;
            motion(e, cur.x + d.x / dist * horiz, vy, cur.z + d.z / dist * horiz);
            if (damage > 0) e.hurt(lv.damageSources().playerAttack(p), (float) damage);
        }
    }

    private static void shake(ServerPlayer p, int rgb, double r) {
        ServerLevel lv = p.serverLevel();
        Fx.wave(lv, p.position().add(0, 0.1, 0), r, rgb, 11);
        Fx.sprite(lv, "rock", rgb, 1.2f, p.position().add(0, 0.2, 0), 24, 1.6, 0.22);
    }

    // ------------------------------------------------------------------ utilisations
    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m) {
        ServerLevel lv = p.serverLevel();
        long now = lv.getGameTime();
        UUID id = p.getUUID();
        int c = col(power);
        switch (m.special) {
            case "sp_haymaker" -> {
                HAYMAKER.put(id, now + 100);
                msg(p, "§6⚡ Haymaker ready!");
                armFx(p, 0xFFE08A, "flare");
            }
            case "sp_break" -> {
                GUARDBREAK.put(id, now + 100);
                msg(p, "§c⚔ Guard Break ready!");
                armFx(p, 0xFFFFFF, "glint");
            }
            case "sp_smash" -> PENDING_SMASH.put(id, 4); // l'onde part 4 ticks plus tard
            case "dz_spin" -> {
                SPIN.put(id, now + 100);
                msg(p, "§d⟳ Spinning Haymaker ready!");
            }
            case "st_harden" -> {
                HARDENED.put(id, now + 80);
                msg(p, "§7■ Durci !");
                Fx.sprite(lv, "rock", 0x9A9AA6, 1.0f, p.position().add(0, 1, 0), 20, 0.5, 0.02);
                lv.playSound(null, p.blockPosition(), SoundEvents.DEEPSLATE_PLACE, SoundSource.PLAYERS, 1.0f, 0.6f);
            }
            case "sk_axe" -> {
                Vec3 look = p.getViewVector(1f);
                Vec3 f = p.position().add(look.x * 2, 0, look.z * 2);
                AABB box = new AABB(f.x - 2.5, f.y - 1.5, f.z - 2.5, f.x + 2.5, f.y + 2.5, f.z + 2.5);
                float dmg = scale(p, 5f, 11f);
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, box, e -> e != p && e.isAlive())) {
                    e.hurt(lv.damageSources().playerAttack(p), dmg);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
                }
                Fx.sprite(lv, "streak", c, 1.2f, f.add(0, 1, 0), 8, 1.2, 0.4);
                Fx.wave(lv, f, 2.4, c, 8);
            }
            case "sk_sweep" -> { // Sweep : balayage bas, tout ce qui est autour tombe
                Vec3 look = p.getViewVector(1f);
                Vec3 lookH = new Vec3(look.x, 0, look.z).normalize();
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(3, 1.2, 3), e -> e != p && e.isAlive())) {
                    Vec3 away = e.position().subtract(p.position()).multiply(1, 0, 1);
                    if (away.lengthSqr() > 0.01 && away.normalize().dot(lookH) < 0) away = Vec3.ZERO;
                    Vec3 v = away.lengthSqr() > 0.01 ? away.normalize().scale(0.42) : lookH.scale(0.42);
                    motion(e, v.x, 0.35, v.z);
                    if (e instanceof ServerPlayer sp) sp.setSprinting(false);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2, false, true));
                }
                Fx.wave(lv, p.position().add(0, 0.2, 0), 3, c, 8);
            }
            case "sk_smash" -> {
                Smash s = new Smash();
                s.dir = p.getViewVector(1f);
                KICK.put(id, s);
                NO_FALL.add(id);
                motion(p, 0, 1.85, 0);
                Fx.sprite(lv, "streak", c, 1.2f, p.position().add(0, 1, 0), 15, 0.3, 0.1);
            }
            case "dz_sucker" -> suckerSwing(p, lv, now, c);
            case "dz_weave" -> {
                WEAVING.put(id, now + 120);
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 0, false, true));
                msg(p, "§d〜 Drunk on punches...");
                lv.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9f, 0.7f);
            }
            case "st_immovable" -> IMMOVABLE.put(id, now);
            default -> { }
        }
        return true;
    }

    public static void whileOn(ServerPlayer p, AbilityPower.Move m, long now) {
        if ("st_immovable".equals(m.special)) IMMOVABLE.put(p.getUUID(), now);
    }

    public static void stop(ServerPlayer p, String special) {
        if ("st_immovable".equals(special)) IMMOVABLE.remove(p.getUUID());
    }

    private static void suckerSwing(ServerPlayer p, ServerLevel lv, long now, int c) {
        Vec3 look = p.getViewVector(1f);
        p.push(look.x * 0.55, 0.1, look.z * 0.55);
        p.hurtMarked = true;
        LivingEntity best = null;
        double bd = 999;
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(3), e -> e != p && e.isAlive())) {
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(p.getEyePosition()).normalize();
            if (to.dot(look) < 0.6) continue;
            double d = e.distanceToSqr(p);
            if (d < bd) {
                bd = d;
                best = e;
            }
        }
        if (best == null) {
            Fx.sprite(lv, "streak", c, 1.2f, p.getEyePosition().add(look.scale(1.2)), 1, 0, 0);
            return;
        }
        boolean watching = best.getViewVector(1f).dot(p.position().subtract(best.position()).normalize()) > 0.5;
        best.hurt(lv.damageSources().playerAttack(p), watching ? 4.5f : 3.0f);
        if (watching) {
            if (now >= BURST_IMMUNE.getOrDefault(best.getUUID(), 0L)) {
                best.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, true));
                best.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0, false, true));
                BURST_IMMUNE.put(best.getUUID(), now + 100);
            }
            msg(p, "§d✴ The feint worked!");
        }
        Fx.sprite(lv, "glint", c, 1.0f, best.getBoundingBox().getCenter(), 6, 0.25, 0.1);
        lv.playSound(null, best.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.8f, 1.3f);
    }

    private static void woozeBurst(ServerPlayer p, LivingEntity t, long now, LivingHurtEvent e) {
        e.setAmount(e.getAmount() + 2f);
        if (now >= BURST_IMMUNE.getOrDefault(t.getUUID(), 0L)) {
            t.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, true));
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, true));
            BURST_IMMUNE.put(t.getUUID(), now + 120);
        }
        Fx.sprite(p.serverLevel(), "glint", 0xFF9AE0, 1.0f, t.getBoundingBox().getCenter(), 9, 0.3, 0.05);
        p.serverLevel().playSound(null, t.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.9f, 0.6f);
        msg(p, "§d☆ Drunken Burst!");
    }

    // ------------------------------------------------------------------ chaque tick
    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            UUID id = p.getUUID();
            ServerLevel lv = p.serverLevel();
            Long hm = HAYMAKER.get(id), gb = GUARDBREAK.get(id), sp = SPIN.get(id);
            if (hm != null) {
                if (now > hm) HAYMAKER.remove(id);
                else if (now % 3 == 0) Fx.sprite(lv, "flare", 0xFFE08A, 0.5f, fist(p), 1, 0.12, 0.01);
            }
            if (gb != null) {
                if (now > gb) GUARDBREAK.remove(id);
                else if (now % 4 == 0) Fx.sprite(lv, "glint", 0xFFFFFF, 0.5f, fist(p), 1, 0.12, 0.01);
            }
            if (sp != null && now > sp) SPIN.remove(id);
            Long hd = HARDENED.get(id);
            if (hd != null) {
                if (now > hd) HARDENED.remove(id);
                else p.setSprinting(false); // durci : pas de course
            }
            Long wv = WEAVING.get(id);
            if (wv != null) {
                if (now > wv) WEAVING.remove(id);
                else if (now % 10 == 0) Fx.sprite(lv, "wisp", 0xFF9AE0, 0.9f, p.getBoundingBox().getCenter(), 1, 0.25, 0.005);
            }

            Long im = IMMOVABLE.get(id);
            if (im != null) {
                if (now - im > 8 || !AbilityEngine.isOn(p, "stone_skin")) IMMOVABLE.remove(id);
                else {
                    p.setSprinting(false);
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 9, false, false));
                    p.addEffect(new MobEffectInstance(MobEffects.JUMP, 8, 128, false, false));
                }
            }

            Integer ps = PENDING_SMASH.get(id);
            if (ps != null) {
                if (ps > 1) PENDING_SMASH.put(id, ps - 1);
                else {
                    PENDING_SMASH.remove(id);
                    aoe(p, 14.0, 3.5, 8.0);
                    shake(p, 0xFFE08A, 7.5);
                    lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0f, 0.9f);
                    Fx.sprite(lv, "rock", 0x9A9AA6, 1.2f, p.position().add(0, 0.2, 0), 48, 1.6, 0.25);
                }
            }

            Smash s = KICK.get(id);
            if (s != null) kickTick(p, s);
        }

        long limit = now;
        for (Map<UUID, Long> m : List.of(GUARD_BROKEN, DIZZY_IMMUNE, BURST_IMMUNE)) m.values().removeIf(t -> t < limit - 400);
        if (now % 200 == 0) for (Iterator<Map<UUID, long[]>> it = WOOZE.values().iterator(); it.hasNext(); ) {
            Map<UUID, long[]> mm = it.next();
            mm.values().removeIf(v -> now > v[1]);
            if (mm.isEmpty()) it.remove();
        }
    }

    private static void kickTick(ServerPlayer p, Smash s) {
        UUID id = p.getUUID();
        ServerLevel lv = p.serverLevel();
        s.timer++;
        if (s.phase == 0) {
            if (s.timer > 5 && p.getDeltaMovement().y <= 0) {
                s.dir = p.getViewVector(1f);
                s.phase = 1;
                s.timer = 0;
                motion(p, s.dir.x * 1.6, s.dir.y * 1.4, s.dir.z * 1.6);
            }
        } else if (s.phase == 1) {
            if (s.timer < 100) {
                Vec3 cur = p.getDeltaMovement();
                motion(p, cur.x * 0.5 + s.dir.x * 0.85, cur.y * 0.5 + s.dir.y * 0.85, cur.z * 0.5 + s.dir.z * 0.85);
            }
            if (!s.done && ((p.onGround() && s.timer > 5) || s.timer >= 100)) {
                s.done = true;
                aoe(p, 8.0, 3.0, 10.0);
                shake(p, 0xFFFFFF, 6);
                lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0f, 0.9f);
                KICK.remove(id);
                NO_FALL.remove(id);
                return;
            }
        }
        if (s.timer > 160) {
            KICK.remove(id);
            NO_FALL.remove(id);
        }
    }

    public static boolean noFall(LivingEntity e) {
        return NO_FALL.contains(e.getUUID());
    }

    public static void onHurt(LivingHurtEvent e) {
        LivingEntity v = e.getEntity();
        if (!(v.level() instanceof ServerLevel lv)) return;
        long now = lv.getGameTime();
        if (e.getSource().getEntity() instanceof ServerPlayer a && a != v) {
            UUID aid = a.getUUID();
            Long hm = HAYMAKER.get(aid);
            if (hm != null && now <= hm) {
                HAYMAKER.remove(aid);
                e.setAmount(e.getAmount() + scale(a, 8f, 16f));
                Vec3 look = a.getViewVector(1f);
                motion(v, look.x * 1.6, 0.5, look.z * 1.6);
                Vec3 at = v.position().add(0, v.getBbHeight() * 0.6, 0);
                Fx.wave(lv, at, 2.6, 0xFFE08A, 9);
                Fx.sprite(lv, "flare", 0xFFFFFF, 1.4f, at, 10, 0.5, 0.2);
                shake(a, 0xFFE08A, 6);
            } else if (hm != null) HAYMAKER.remove(aid);
            Long spn = SPIN.get(aid);
            if (spn != null && now <= spn) {
                SPIN.remove(aid);
                v.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, true));
                if (now >= DIZZY_IMMUNE.getOrDefault(v.getUUID(), 0L)) {
                    v.addEffect(new MobEffectInstance(ModEffects.DISORIENTED.get(), 50, 0, false, true));
                    DIZZY_IMMUNE.put(v.getUUID(), now + 290);
                }
                Fx.sprite(lv, "glint", 0xFF9AE0, 1.2f, v.getBoundingBox().getCenter(), 10, 0.4, 0.1);
            } else if (spn != null) SPIN.remove(aid);
            Long gbk = GUARDBREAK.get(aid);
            if (gbk != null && now <= gbk) {
                GUARDBREAK.remove(aid);
                if (v instanceof Player pl) pl.disableShield(true);
                GUARD_BROKEN.put(v.getUUID(), now + 100);
                Vec3 at = v.position().add(0, v.getBbHeight() * 0.6, 0);
                Fx.sprite(lv, "shard", 0xFFFFFF, 1.3f, at, 26, 1.1, 0.3);
                lv.playSound(null, v.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.8f, 1.3f);
                msg(a, "§cGuard broken!");
            } else if (gbk != null) GUARDBREAK.remove(aid);
            woozeHit(a, v, e, now);
        }
        if (v instanceof ServerPlayer vp) {
            Long hd = HARDENED.get(vp.getUUID());
            if (hd != null) {
                if (now <= hd) {
                    e.setAmount(e.getAmount() * (1f - scale(vp, 0.6f, 0.85f)));
                    if (!redirecting && e.getSource().getEntity() instanceof LivingEntity at && at != vp && at.isAlive()) {
                        redirecting = true;
                        at.hurt(lv.damageSources().thorns(vp), 1.5f);
                        redirecting = false;
                    }
                } else HARDENED.remove(vp.getUUID());
            }
            if (IMMOVABLE.containsKey(vp.getUUID())) e.setAmount(e.getAmount() * 0.75f);

            Long wv = WEAVING.get(vp.getUUID());
            if (wv != null && now <= wv && e.getSource().getEntity() instanceof LivingEntity at && at != vp
                    && e.getSource().getDirectEntity() == at) {
                e.setAmount(e.getAmount() * 0.75f);
                at.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, true));
                at.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0, false, true));
                Fx.sprite(lv, "wisp", 0xFF9AE0, 0.9f, vp.getBoundingBox().getCenter(), 3, 0.3, 0.01);
            }
        }
        Long gbr = GUARD_BROKEN.get(v.getUUID());
        if (gbr != null) {
            if (now <= gbr) e.setAmount(e.getAmount() * 1.25f);
            GUARD_BROKEN.remove(v.getUUID());
        }
    }

    private static void woozeHit(ServerPlayer a, LivingEntity v, LivingHurtEvent e, long now) {
        if (e.getSource().getDirectEntity() != a) return;
        if (!AbilityEngine.isOn(a, "dizzy_punch")) return;
        Map<UUID, long[]> per = WOOZE.computeIfAbsent(a.getUUID(), k -> new HashMap<>());
        long[] w = per.get(v.getUUID());
        if (w == null) {
            if (per.size() >= 6) return;
            w = new long[2];
            per.put(v.getUUID(), w);
        }
        if (now > w[1]) w[0] = 0;
        w[0]++;
        w[1] = now + 100;
        if (w[0] >= 3) {
            per.remove(v.getUUID());
            woozeBurst(a, v, now, e);
        }
    }
}
