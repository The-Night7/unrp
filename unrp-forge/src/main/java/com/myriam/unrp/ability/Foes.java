package com.myriam.unrp.ability;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Cibles « loyales » et coups directs, partagés par plusieurs capacités Origin. */
public final class Foes {
    private Foes() {}

    public static void hit(LivingEntity e, ServerPlayer by, float dmg) {
        e.invulnerableTime = 0;
        e.hurt(by.serverLevel().damageSources().playerAttack(by), dmg);
    }

    public static void shove(LivingEntity e, double x, double y, double z) {
        e.setDeltaMovement(x, y, z);
        e.hurtMarked = true;
    }

    public static List<LivingEntity> foesNear(ServerLevel lv, ServerPlayer p, Vec3 at, double r) {
        return lv.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(r), e -> fair(p, e) && e.position().add(0, e.getBbHeight() * 0.5, 0).distanceTo(at) <= r + e.getBbWidth() * 0.5);
    }

    public static boolean fair(ServerPlayer p, LivingEntity e) {
        if (e == p || !e.isAlive() || e.isSpectator()) return false;
        if (e instanceof Player o && (o.isCreative() || com.myriam.unrp.social.SocialService.allies(p.server, p.getUUID(), o.getUUID()))) return false;
        return true;
    }

    public static List<LivingEntity> foesAlong(ServerLevel lv, ServerPlayer p, Vec3 a, Vec3 b, double w) {
        List<LivingEntity> out = new ArrayList<>();
        Vec3 d = b.subtract(a);
        double len = d.length();
        if (len < 1e-4) return out;
        Vec3 u = d.scale(1 / len);
        for (LivingEntity e : lv.getEntitiesOfClass(LivingEntity.class, new AABB(a, b).inflate(w + 1), e -> fair(p, e))) {
            Vec3 to = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(a);
            double along = to.dot(u);
            if (along < -w || along > len + w) continue;
            if (to.subtract(u.scale(along)).length() <= w + e.getBbWidth() * 0.5) out.add(e);
        }
        return out;
    }
}
