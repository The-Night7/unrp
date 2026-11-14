package com.myriam.unrp.ability;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Projectiles des pouvoirs (missiles, mains fantômes, croissants, aiguilles...), remplacent les entités du modèle
 * (MissileEntity, PhantomHandEntity, CrescentEntity). Une seule boucle, réglée par les champs de {@link P}.
 */
public final class Projectiles {
    private Projectiles() {}

    public enum Kind {
        /** explose à la fin (coup, mur ou durée) */
        MISSILE,
        /** poing / paume : repousse vers l'avant, s'arrête au premier touché */
        HAND,
        /** chute d'une grande main : onde sur la zone à l'impact */
        SLAM,
        /** croissant : traverse (pierce + 1 cibles), s'arrête sur un mur, portée limitée */
        CRESCENT,
        /** aiguille : dégâts aléatoires, s'arrête au premier touché */
        NEEDLE
    }

    public static final class P {
        public Kind kind = Kind.NEEDLE;
        public UUID owner;
        public Vec3 pos, vel, aim;
        public double drag = 1.0, aimPull = 0.0, gravity = 0.0;
        /** projectile de Palladium (CustomProjectile du modèle) : avance, puis inertie 0,99 (0,8 dans l'eau) et gravité,
         *  dans cet ordre, comme le moteur d'effets le prévoit côté client ; il n'est alors pas dessiné par nous */
        public boolean palladium;
        public int life, maxLife = 40;
        public float damage = 1f, damageMax = -1f, blast = 1f, size = 1f;
        public double knock = 0.0, range = 1e9;
        public int pierce = 0, stun = 0;
        public boolean breakGlass;
        public int rgb = 0xFFFFFF;
        public String sprite = "mote";
        public boolean lineTrail;
        /** appelé à chaque cible touchée */
        public BiConsumer<P, LivingEntity> onHit;
        /** appelé à la fin (impact, mur, durée) */
        public Consumer<P> onEnd;
        final Set<UUID> hit = new HashSet<>();
        int hitsLeft = 1;
        int fxId = ProjFx.newId();
    }

    private static final List<P> ALL = new ArrayList<>();

    public static void spawn(P p) {
        p.hitsLeft = p.pierce + 1;
        ALL.add(p);
    }

    public static void clear() {
        ALL.clear();
    }

    private static net.minecraft.world.damagesource.DamageSource source(ServerLevel lv, ServerPlayer owner) {
        return owner != null ? lv.damageSources().playerAttack(owner) : lv.damageSources().magic();
    }

    public static void tick(MinecraftServer server) {
        for (Iterator<P> it = ALL.iterator(); it.hasNext(); ) {
            P p = it.next();
            ServerPlayer owner = server.getPlayerList().getPlayer(p.owner);
            if (owner == null) {
                it.remove();
                continue;
            }
            ServerLevel lv = owner.serverLevel();
            if (!step(lv, owner, p)) it.remove();
        }
    }

    /** Un tick d'un projectile ; false = fini. */
    private static boolean step(ServerLevel lv, ServerPlayer owner, P p) {
        p.life++;
        if (p.life > p.maxLife || p.range <= 0) {
            end(lv, owner, p);
            return false;
        }
        if (!p.palladium) {
            if (p.aim != null && p.aimPull > 0) p.vel = p.vel.scale(p.drag).add(p.aim.scale(p.aimPull));
            if (p.gravity != 0) p.vel = p.vel.add(0, -p.gravity, 0);
        }
        Vec3 from = p.pos, to = from.add(p.vel);
        if (p.palladium) {
            boolean wet = !lv.getFluidState(net.minecraft.core.BlockPos.containing(from)).isEmpty();
            p.vel = p.vel.scale(wet ? 0.8 : 0.99).add(0, -p.gravity, 0);
        }
        p.range -= p.vel.length();
        HitResult block = lv.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        boolean blocked = block.getType() != HitResult.Type.MISS;
        Vec3 end = blocked ? block.getLocation() : to;
        double sweep = switch (p.kind) {
            case MISSILE -> 1.0;
            case HAND, SLAM -> 0.3 + 0.4 * p.size;
            case CRESCENT -> 0.35 + 0.35 * p.size;
            case NEEDLE -> 0.35;
        };
        boolean stop = false;
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(from, end).inflate(sweep), e -> e.isAlive() && !e.getUUID().equals(p.owner))) {
            if (p.kind == Kind.CRESCENT && !p.hit.add(e.getUUID())) continue;
            hurt(lv, owner, p, e);
            if (p.kind == Kind.CRESCENT) {
                if (--p.hitsLeft <= 0) {
                    stop = true;
                    break;
                }
            } else {
                stop = true;
                break;
            }
        }
        if (stop) {
            p.pos = end;
            end(lv, owner, p);
            return false;
        }
        if (blocked) {
            p.pos = end;
            end(lv, owner, p);
            return false;
        }
        Vec3 prev = p.pos;
        p.pos = to;
        if (!p.palladium) ProjFx.body(lv, p, prev, to);
        return true;
    }

    private static void hurt(ServerLevel lv, ServerPlayer owner, P p, LivingEntity e) {
        float dmg = p.damageMax > p.damage ? p.damage + e.getRandom().nextFloat() * (p.damageMax - p.damage) : p.damage;
        if (p.kind == Kind.MISSILE) dmg = 4f + p.blast * 2.5f;
        e.invulnerableTime = 0;
        if (dmg > 0 && p.kind != Kind.SLAM) e.hurt(source(lv, owner), dmg);
        if (p.stun > 0 && p.kind != Kind.SLAM) e.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, p.stun, 3, false, false));
        if (p.kind == Kind.HAND && p.knock > 0) {
            Vec3 dir = p.vel.lengthSqr() > 0.01 ? p.vel.normalize() : new Vec3(0, -1, 0);
            e.setDeltaMovement(dir.x * p.knock, Math.max(0.25, dir.y * p.knock * 0.5 + 0.2), dir.z * p.knock);
            e.hurtMarked = true;
        }
        if (p.onHit != null) p.onHit.accept(p, e);
    }

    private static void end(ServerLevel lv, ServerPlayer owner, P p) {
        switch (p.kind) {
            case MISSILE -> {
                lv.explode(owner, p.pos.x, p.pos.y, p.pos.z, p.blast, Level.ExplosionInteraction.NONE);
            }
            case SLAM -> {
                AABB area = new AABB(p.pos.x - 3.5, p.pos.y - 1.0, p.pos.z - 3.5, p.pos.x + 3.5, p.pos.y + 3.0, p.pos.z + 3.5);
                for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && !e.getUUID().equals(p.owner))) {
                    e.hurt(source(lv, owner), p.damage);
                    if (p.stun > 0) e.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, p.stun, 3, false, false));
                    Vec3 v = e.getDeltaMovement();
                    e.setDeltaMovement(v.x, Math.max(v.y, p.knock), v.z);
                    e.hurtMarked = true;
                }
                if (p.breakGlass) {
                    net.minecraft.core.BlockPos c = net.minecraft.core.BlockPos.containing(p.pos);
                    for (net.minecraft.core.BlockPos b : net.minecraft.core.BlockPos.betweenClosed(c.offset(-3, -1, -3), c.offset(3, 3, 3))) {
                        var bs = lv.getBlockState(b);
                        if (bs.is(net.minecraftforge.common.Tags.Blocks.GLASS) || bs.is(net.minecraftforge.common.Tags.Blocks.GLASS_PANES)) lv.destroyBlock(b, false);
                    }
                }
                Fx.wave(lv, p.pos, 3.5, p.rgb, 10);
            }
            default -> { }
        }
        ProjFx.finish(lv, p);
        if (p.onEnd != null) p.onEnd.accept(p);
    }
}
