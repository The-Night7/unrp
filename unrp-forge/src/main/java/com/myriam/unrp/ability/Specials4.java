package com.myriam.unrp.ability;

import com.myriam.unrp.data.AbilityPower;
import com.myriam.unrp.data.ClaimData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Specials4 {
    private Specials4() {}

    private static final Set<String> ALL = Set.of("qs_blitz", "qs_stance", "qs_combo", "qs_pursuit", "ra_smash", "ra_guard",
            "hp_palm", "hp_smelt", "hb_thrust", "hb_release", "cs_grip", "cs_rake");

    public static boolean handles(String s) {
        return s != null && ALL.contains(s);
    }

    private static final Map<UUID, Integer> BLITZ = new HashMap<>(), PURSUIT = new HashMap<>(), STANCE = new HashMap<>(), SMELT = new HashMap<>();
    private static final Map<UUID, UUID> PURSUIT_TARGET = new HashMap<>();
    private static final Map<UUID, int[]> COMBO = new HashMap<>();         // {ticks restants, pas, ...}
    private static final Map<UUID, UUID> COMBO_TARGET = new HashMap<>();
    private static final Map<UUID, Integer> THRUST = new HashMap<>();      // Hand Blade : ticks de fente restants
    private static final Map<UUID, Vec3> THRUST_DIR = new HashMap<>();
    private static final Map<UUID, Integer> RAKE = new HashMap<>();
    private static final Map<UUID, Set<UUID>> RAKE_HIT = new HashMap<>();
    private static final Set<UUID> BUSY = new HashSet<>();
    private static final Map<UUID, Integer> MINOR_CD = new HashMap<>();

    private static final class Mark {
        ServerPlayer owner;
        LivingEntity target;
        int type, timer, charge, chargeCounter;
        boolean grip;
    }

    private static final Map<UUID, Mark> MARKS = new HashMap<>();

    private static final class Crescent {
        UUID owner;
        Vec3 pos, dir;
        double left = 22;
        int pierce = 4;
        final Set<UUID> hit = new HashSet<>();
    }

    private static final List<Crescent> CRESCENTS = new ArrayList<>();

    public static void onLogout(UUID id) {
        for (Map<UUID, ?> m : List.of(BLITZ, PURSUIT, STANCE, SMELT, PURSUIT_TARGET, COMBO, COMBO_TARGET, THRUST, THRUST_DIR, RAKE, RAKE_HIT)) m.remove(id);
        BUSY.remove(id);
    }

    // ------------------------------------------------------------------ outils
    private static double level(ServerPlayer p) {
        return ClaimData.get(p.server).profileOf(p.getUUID()).map(pr -> pr.level).orElse(1.1);
    }

    private static float sf(ServerPlayer p) {
        return (float) Math.max(0, Math.min(1, (level(p) * 10 - 11) / 89.0));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static void msg(ServerPlayer p, String s) {
        p.displayClientMessage(Component.literal(s), true);
    }

    private static int col(AbilityPower power) {
        return power.look != null && power.look.rgb() >= 0 ? power.look.rgb() : 0xFFFFFF;
    }

    private static void motion(LivingEntity e, double x, double y, double z) {
        e.setDeltaMovement(x, y, z);
        e.hurtMarked = true;
    }

    private static boolean has(ServerPlayer p, String power) {
        return AbilityEngine.isOn(p, power);
    }

    private static boolean melee(LivingHurtEvent e) {
        return "player".equals(e.getSource().type().msgId());
    }

    private static LivingEntity nearestInCone(ServerPlayer p, double range, double dot) {
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f).normalize();
        LivingEntity best = null;
        double bd = Double.MAX_VALUE;
        for (LivingEntity e : p.serverLevel().getEntitiesOfClass(LivingEntity.class, new AABB(eye.subtract(range, range, range), eye.add(range, range, range)),
                e -> e != p && e.isAlive())) {
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye);
            double d = to.length();
            if (d > range || to.normalize().dot(look) < dot) continue;
            if (d < bd) {
                bd = d;
                best = e;
            }
        }
        return best;
    }

    private static void attr(LivingEntity e, net.minecraft.world.entity.ai.attributes.Attribute a, String key, double amount, boolean on) {
        AttributeInstance inst = e.getAttribute(a);
        if (inst == null) return;
        UUID id = UUID.nameUUIDFromBytes(("unrp:sp4:" + key).getBytes(StandardCharsets.UTF_8));
        boolean has = inst.getModifier(id) != null;
        if (on && !has) inst.addTransientModifier(new AttributeModifier(id, "unrp " + key, amount, AttributeModifier.Operation.ADDITION));
        else if (!on && has) inst.removeModifier(id);
    }

    // ------------------------------------------------------------------ utilisations
    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m) {
        ServerLevel lv = p.serverLevel();
        long now = lv.getGameTime();
        UUID id = p.getUUID();
        int c = col(power);
        Vec3 look = p.getViewVector(1f);
        switch (m.special) {
            // ---------------- Quick Strike
            case "qs_blitz" -> {
                Vec3 h = new Vec3(look.x, 0, look.z).normalize();
                motion(p, h.x * 1.5, Math.max(0.1, look.y * 0.3) * 1.5, h.z * 1.5);
                BLITZ.put(id, 8);
            }
            case "qs_stance" -> {
                STANCE.put(id, 400);
                msg(p, "§6Assault stance engaged!");
            }
            case "qs_combo" -> {
                LivingEntity t = nearestInCone(p, 3.5, 0.3);
                COMBO.put(id, new int[] {10, 0});
                COMBO_TARGET.remove(id);
                if (t != null) COMBO_TARGET.put(id, t.getUUID());
            }
            case "qs_pursuit" -> {
                LivingEntity t = nearestInCone(p, 12, 0.5);
                if (t != null) {
                    Vec3 d = t.position().subtract(p.position());
                    double dist = d.length();
                    Vec3 n = d.normalize();
                    double sp = Math.min(1.6, 0.4 + dist * 0.25);
                    motion(p, n.x * sp, Math.max(n.y * sp * 0.08 * 0.5, 0.1) * 1.0, n.z * sp);
                    PURSUIT.put(id, 12);
                    PURSUIT_TARGET.put(id, t.getUUID());
                } else {
                    Vec3 h = new Vec3(look.x, 0, look.z).normalize();
                    motion(p, h.x * 1.3, 0.12 * 1.3, h.z * 1.3);
                }
            }
            // ---------------- Rock Arm
            case "ra_smash" -> rockSmash(p, lv, c);
            case "ra_guard" -> { }
            // ---------------- Heat Palm
            case "hp_palm" -> palmStrike(p, lv, c);
            case "hp_smelt" -> { }
            // ---------------- Hand Blade
            case "hb_thrust" -> {
                THRUST_DIR.put(id, look);
                motion(p, look.x * 1.6, Math.max(0.1, look.y * 0.6) * 1.6, look.z * 1.6);
                THRUST.put(id, 5);
            }
            case "hb_release" -> {
                Crescent cr = new Crescent();
                cr.owner = id;
                cr.dir = look.normalize();
                cr.pos = p.getEyePosition().add(look.scale(0.8)).add(0, -0.2, 0);
                CRESCENTS.add(cr);
                lv.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
            // ---------------- Contact Shred
            case "cs_grip" -> {
                LivingEntity t = nearestInCone(p, 3.0, 0.5);
                if (t == null) {
                    msg(p, "§c[Laceration] §7Nobody in range.");
                    return false;
                }
                BUSY.add(id);
                t.hurt(lv.damageSources().playerAttack(p), lerp(2f, 4f, sf(p)));
                BUSY.remove(id);
                applyMark(p, t, 2);
            }
            case "cs_rake" -> {
                RAKE.put(id, 8);
                RAKE_HIT.put(id, new HashSet<>());
            }
            default -> { }
        }
        return true;
    }

    public static void whileOn(ServerPlayer p, AbilityPower.Move m, long now) {
        if (!"hp_smelt".equals(m.special)) return;
        int interval = Math.max(4, (int) (30 - 20 * sf(p)));
        int t = SMELT.getOrDefault(p.getUUID(), 0) + 4;
        if (t >= interval) {
            t = 0;
            smeltOne(p, p.serverLevel());
        }
        SMELT.put(p.getUUID(), t);
    }

    public static void stop(ServerPlayer p, String special) {
        if ("hp_smelt".equals(special)) SMELT.remove(p.getUUID());
    }

    // ------------------------------------------------------------------ Rock Arm / Heat Palm
    private static void rockSmash(ServerPlayer p, ServerLevel lv, int c) {
        double r = 3.5;
        float dmg = (float) (6.0 + 4.0 * sf(p));
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f).normalize();
        boolean hit = false;
        BUSY.add(p.getUUID());
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(eye.subtract(r, r, r), eye.add(r, r, r)), e -> e != p && e.isAlive())) {
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye);
            if (to.length() > r || to.normalize().dot(look) < 0.906) continue;
            e.hurt(lv.damageSources().playerAttack(p), dmg);
            e.knockback(0.8, -look.x, -look.z);
            hit = true;
        }
        BUSY.remove(p.getUUID());
        Fx.sprite(lv, "rock", c, 1.2f, eye.add(look.scale(2)), 25, 0.4, 0.15);
        lv.playSound(null, p.blockPosition(), hit ? SoundEvents.PLAYER_ATTACK_KNOCKBACK : SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.9f);
    }

    private static void palmStrike(ServerPlayer p, ServerLevel lv, int c) {
        double r = 3.0;
        float s = sf(p);
        float dmg = 5f + 4f * s + (2f + 2f * s);
        int fire = (int) (20f + 20f * s);
        Vec3 eye = p.getEyePosition(), look = p.getViewVector(1f).normalize();
        boolean hit = false;
        BUSY.add(p.getUUID());
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(eye.subtract(r, r, r), eye.add(r, r, r)), e -> e != p && e.isAlive())) {
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(eye);
            if (to.length() > r || to.normalize().dot(look) < 0.924) continue;
            e.hurt(lv.damageSources().playerAttack(p), dmg);
            e.setRemainingFireTicks(Math.max(e.getRemainingFireTicks(), fire));
            e.knockback(0.3, -look.x, -look.z);
            hit = true;
        }
        BUSY.remove(p.getUUID());
        Fx.sprite(lv, "ember", 0xFF7A3A, 1.2f, eye.add(look.scale(1.8)), 18, 0.3, 0.02);
        lv.playSound(null, p.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9f, hit ? 1.2f : 0.8f);
    }

    private static void smeltOne(ServerPlayer p, ServerLevel lv) {
        ItemStack held = p.getMainHandItem();
        if (held.isEmpty()) return;
        var rec = lv.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(held.copyWithCount(1)), lv);
        if (rec.isEmpty()) return;
        ItemStack out = rec.get().getResultItem(lv.registryAccess()).copy();
        if (out.isEmpty()) return;
        held.shrink(1);
        if (!p.getInventory().add(out)) p.drop(out, false);
        Fx.sprite(lv, "ember", 0xFF7A3A, 0.8f, p.getEyePosition().add(p.getViewVector(1f).scale(0.6)).add(0, -0.4, 0), 6, 0.12, 0.01);
        lv.playSound(null, p.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    // ------------------------------------------------------------------ Contact Shred
    private static void applyMark(ServerPlayer owner, LivingEntity t, int type) {
        Mark old = MARKS.get(t.getUUID());
        if (old != null && old.type > type) return;
        Mark m = new Mark();
        m.owner = owner;
        m.target = t;
        m.type = type;
        m.grip = type == 2;
        m.timer = type == 2 ? (int) lerp(30f, 50f, sf(owner)) : type == 1 ? 30 : 20;
        MARKS.put(t.getUUID(), m);
    }

    private static void rupture(Mark m) {
        if (m.target == null || !(m.target.level() instanceof ServerLevel lv)) return;
        float s = m.owner != null ? sf(m.owner) : 0;
        float dmg;
        if (m.type == 2) dmg = lerp(4f, 7f, s) + m.charge * lerp(1.25f, 1.75f, s);
        else if (m.type == 1) dmg = lerp(3.5f, 5.5f, s) + m.charge * lerp(1f, 1.5f, s);
        else dmg = lerp(2.5f, 4f, s);
        if (m.owner != null && m.owner.isAlive()) {
            BUSY.add(m.owner.getUUID());
            m.target.hurt(lv.damageSources().playerAttack(m.owner), dmg);
            BUSY.remove(m.owner.getUUID());
        } else m.target.hurt(lv.damageSources().magic(), dmg);
        if (m.type == 2) m.target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, true));
        Vec3 at = m.target.position().add(0, m.target.getBbHeight() * 0.5, 0);
        Fx.sprite(lv, "shard", 0xC8283C, 1.4f, at, 24, 0.6, 0.2);
        Fx.wave(lv, at, 1.6, 0xC8283C, 8);
        lv.playSound(null, m.target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 0.6f);
    }

    // ------------------------------------------------------------------ chaque tick
    public static void tick(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            UUID id = p.getUUID();
            ServerLevel lv = p.serverLevel();
            boolean qs = has(p, "quick_strike");
            attr(p, Attributes.KNOCKBACK_RESISTANCE, "qs_sprint_kb", 0.15, qs && p.isSprinting());
            // Quick Strike : posture d'assaut
            Integer stance = STANCE.get(id);
            boolean inStance = stance != null && stance > 0 && qs;
            if (stance != null && (!qs || stance <= 0 || (ClaimData.get(server).profileOf(id).map(pr -> pr.aura).orElse(1) <= 0))) {
                STANCE.remove(id);
                if (stance > 0) msg(p, "§6La posture d'assaut s'estompe.");
                inStance = false;
            } else if (stance != null) {
                STANCE.put(id, stance - 1);
                if (stance == 1) msg(p, "§6La posture d'assaut prend fin.");
                if (stance % 3 == 0) Fx.sprite(lv, "streak", 0xFFC857, 0.7f, p.position().add(0, 1, 0), 2, 0.3, 0.02);
            }
            attr(p, Attributes.ATTACK_SPEED, "qs_stance_as", 1.2, inStance);
            attr(p, Attributes.MOVEMENT_SPEED, "qs_stance_sp", 0.03, inStance);
            attr(p, Attributes.KNOCKBACK_RESISTANCE, "qs_stance_kb", 0.3, inStance);
            blitzTick(p, lv, id);
            comboTick(p, lv, id);
            pursuitTick(p, lv, id);
            thrustTick(p, lv, id);
            rakeTick(p, lv, id);
        }

        for (Iterator<Crescent> it = CRESCENTS.iterator(); it.hasNext(); ) {
            Crescent c = it.next();
            ServerPlayer o = server.getPlayerList().getPlayer(c.owner);
            if (o == null || c.left <= 0 || c.pierce <= 0) {
                it.remove();
                continue;
            }
            ServerLevel lv = o.serverLevel();
            double step = 1.25;
            Vec3 next = c.pos.add(c.dir.scale(step));
            var bh = lv.clip(new net.minecraft.world.level.ClipContext(c.pos, next, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE, o));
            boolean blocked = bh.getType() != net.minecraft.world.phys.HitResult.Type.MISS;
            Vec3 end = blocked ? bh.getLocation() : next;
            Fx.line(lv, c.pos, end, 0xBFE8FF, 0, 0.12f, 4);
            for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(c.pos, end).inflate(1.3), e -> e != o && e.isAlive())) {
                if (c.pierce <= 0 || !c.hit.add(e.getUUID())) continue;
                BUSY.add(o.getUUID());
                e.hurt(lv.damageSources().playerAttack(o), 7f);
                BUSY.remove(o.getUUID());
                c.pierce--;
                Fx.sprite(lv, "shard", 0xBFE8FF, 1.0f, e.getBoundingBox().getCenter(), 8, 0.3, 0.1);
            }
            c.pos = end;
            c.left -= step;
            if (blocked) c.left = 0;
        }
        // Contact Shred : marques
        MINOR_CD.replaceAll((k, v) -> v - 1);
        MINOR_CD.values().removeIf(v -> v <= 0);
        for (Iterator<Mark> it = MARKS.values().iterator(); it.hasNext(); ) {
            Mark m = it.next();
            boolean ownerOk = m.owner != null && m.owner.isAlive() && !m.owner.isRemoved() && has(m.owner, "contact_shred");
            boolean targetOk = m.target != null && m.target.isAlive() && !m.target.isRemoved();
            if (!targetOk) {
                it.remove();
                continue;
            }
            if (!ownerOk) {
                rupture(m);
                it.remove();
                continue;
            }
            if (m.grip && m.timer > 0 && ++m.chargeCounter % 10 == 0) m.charge++;
            m.timer--;
            if (m.target.level() instanceof ServerLevel lv && m.target.tickCount % 6 == 0) {
                Fx.sprite(lv, "ember", 0xC8283C, 0.6f, m.target.position().add(0, m.target.getBbHeight() * 0.5, 0), 1 + m.charge, 0.35, 0.0);
            }
            if (m.timer <= 0) {
                rupture(m);
                it.remove();
            }
        }
    }

    private static void blitzTick(ServerPlayer p, ServerLevel lv, UUID id) {
        Integer b = BLITZ.get(id);
        if (b == null) return;
        if (b <= 0) {
            BLITZ.remove(id);
            return;
        }
        BLITZ.put(id, b - 1);
        LivingEntity t = nearestInCone(p, 2.4, 0.25);
        if (t == null) return;
        BLITZ.remove(id);
        BUSY.add(id);
        t.hurt(lv.damageSources().playerAttack(p), 7f);
        BUSY.remove(id);
        t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, true));
        p.setDeltaMovement(p.getDeltaMovement().scale(0.2));
        p.hurtMarked = true;
        p.swing(InteractionHand.MAIN_HAND, true);
        Fx.sprite(lv, "streak", 0xFFC857, 1.0f, t.getBoundingBox().getCenter(), 10, 0.3, 0.15);
    }

    private static void comboTick(ServerPlayer p, ServerLevel lv, UUID id) {
        int[] st = COMBO.get(id);
        if (st == null) return;
        st[0]--;
        UUID tid = COMBO_TARGET.get(id);
        LivingEntity t = tid == null ? null : lv.getEntity(tid) instanceof LivingEntity le ? le : null;
        if (t == null || !t.isAlive() || t.distanceTo(p) >= 4.5
                || t.position().add(0, t.getBbHeight() * 0.5, 0).subtract(p.getEyePosition()).normalize().dot(p.getViewVector(1f)) <= 0.2) {
            t = nearestInCone(p, 3.5, 0.3);
            if (t != null) COMBO_TARGET.put(id, t.getUUID());
        }
        if (st[0] <= 0 && st[1] >= 5 || st[0] < -4) {
            COMBO.remove(id);
            COMBO_TARGET.remove(id);
            return;
        }
        if (t == null || (10 - st[0]) % 2 != 1 || st[1] >= 5) return;
        st[1]++;
        t.invulnerableTime = 0;
        t.hurtTime = 0;
        BUSY.add(id);
        if (st[1] >= 5) { // dernier coup : fort, repousse
            t.hurt(lv.damageSources().playerAttack(p), 5f);
            Vec3 a = t.position().subtract(p.position()).multiply(1, 0, 1).normalize();
            motion(t, a.x * 1.1, 0.4, a.z * 1.1);
            COMBO.remove(id);
            COMBO_TARGET.remove(id);
        } else {
            t.hurt(lv.damageSources().playerAttack(p), 1.6f);
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6, 3, false, false));
        }
        BUSY.remove(id);
        p.swing(InteractionHand.MAIN_HAND, true);
        Fx.sprite(lv, "streak", 0xFFC857, 0.8f, t.getBoundingBox().getCenter(), 4, 0.25, 0.1);
    }

    private static void pursuitTick(ServerPlayer p, ServerLevel lv, UUID id) {
        Integer n = PURSUIT.get(id);
        if (n == null) return;
        if (n <= 0) {
            PURSUIT.remove(id);
            PURSUIT_TARGET.remove(id);
            return;
        }
        PURSUIT.put(id, n - 1);
        UUID tid = PURSUIT_TARGET.get(id);
        if (tid != null && lv.getEntity(tid) instanceof LivingEntity t && t.isAlive() && t.distanceTo(p) < 2.6) {
            PURSUIT.remove(id);
            PURSUIT_TARGET.remove(id);
            BUSY.add(id);
            p.attack(t);
            BUSY.remove(id);
            p.swing(InteractionHand.MAIN_HAND, true);
        }
    }

    private static void thrustTick(ServerPlayer p, ServerLevel lv, UUID id) {
        Integer n = THRUST.get(id);
        if (n == null) return;
        if (n <= 0) {
            THRUST.remove(id);
            return;
        }
        THRUST.put(id, n - 1);
        Vec3 d = THRUST_DIR.getOrDefault(id, p.getViewVector(1f));
        motion(p, d.x * 1.45, Math.max(-0.2, d.y * 0.9), d.z * 1.45);
        p.fallDistance = 0;
        Vec3 look = p.getViewVector(1f);
        LivingEntity best = null;
        double bd = 0.2;
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(2.2), e -> e != p && e.isAlive())) {
            double dot = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(p.getEyePosition()).normalize().dot(look);
            if (dot > bd) {
                bd = dot;
                best = e;
            }
        }
        if (best == null) return;
        THRUST.remove(id);
        BUSY.add(id);
        best.hurt(lv.damageSources().playerAttack(p), 9f);
        BUSY.remove(id);
        best.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, true)); // « saigne »
        best.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
        Fx.sprite(lv, "shard", 0xBFE8FF, 1.0f, best.getBoundingBox().getCenter(), 12, 0.3, 0.15);
    }

    private static void rakeTick(ServerPlayer p, ServerLevel lv, UUID id) {
        Integer n = RAKE.get(id);
        if (n == null) return;
        if (n <= 0) {
            RAKE.remove(id);
            RAKE_HIT.remove(id);
            return;
        }
        RAKE.put(id, n - 1);
        Vec3 look = p.getViewVector(1f).normalize();
        motion(p, look.x * 1.15, Math.max(p.getDeltaMovement().y, 0.18) * 1.15, look.z * 1.15);
        Set<UUID> hit = RAKE_HIT.computeIfAbsent(id, k -> new HashSet<>());
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(2.5), e -> e != p && e.isAlive())) {
            if (!hit.add(e.getUUID())) continue;
            BUSY.add(id);
            e.hurt(lv.damageSources().playerAttack(p), lerp(3f, 5.5f, sf(p)));
            BUSY.remove(id);
            applyMark(p, e, 1);
        }
    }

    public static void onHurt(LivingHurtEvent e) {
        LivingEntity v = e.getEntity();
        if (e.getSource().getEntity() instanceof ServerPlayer a && a != v && melee(e) && !BUSY.contains(a.getUUID())) {
            if (has(a, "rock_arm")) e.setAmount(e.getAmount() + (float) (2.0 + 2.0 * sf(a))); // poing de pierre
            if (has(a, "heat_palm")) {
                e.setAmount(e.getAmount() + (float) (1.5 + 1.5 * sf(a)));
                int secs = (int) (0.5 + 1.0 * sf(a));
                if (secs > 0) v.setRemainingFireTicks(Math.max(v.getRemainingFireTicks(), secs * 20));
            }
            if (has(a, "contact_shred") && !MINOR_CD.containsKey(v.getUUID()) && a.level() instanceof ServerLevel) {
                applyMark(a, v, 0);
                MINOR_CD.put(v.getUUID(), 40);
            }
        }
        if (v instanceof ServerPlayer vp && has(vp, "rock_arm") && AbilityEngine.toggled(vp, "rock_arm", 1)) {
            e.setAmount(e.getAmount() * 0.85f);
        }
    }

    public static void modelTags(ServerPlayer p) {
        long now = p.serverLevel().getGameTime();
        tag(p, "unrp.active.qs_stance", STANCE.containsKey(p.getUUID()));
    }

    private static void tag(ServerPlayer p, String t, boolean on) {
        if (on) p.addTag(t);
        else p.removeTag(t);
    }
}
