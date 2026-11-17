package com.myriam.unrp.pass;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class LightningPass {
    private LightningPass() {}

    private static int next = 0;

    public static int newAux() {
        next = next >= 1_000_000 ? 1 : next + 1;
        return -next;
    }

    static double[] v(Vec3 a, Vec3 b) {
        return new double[] {a.x, a.y, a.z, b == null ? 0 : b.x, b == null ? 0 : b.y, b == null ? 0 : b.z};
    }

    public static void call(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        boolean roof = !level.canSeeSky(BlockPos.containing(p.getEyePosition()));
        PassEvents.lightning(level, PassFxPacket.LT_ON, p, null, 0, roof ? PassFxPacket.F_ROOF : 0, p.position(), null);
    }

    public static void launch(ServerPlayer owner, int op, int aux, Vec3 pos, Vec3 vel, int life, boolean gravity) {
        int flags = (PassEvents.copy(owner) ? PassFxPacket.F_COPY : 0) | (gravity ? 0 : PassFxPacket.F_FLOAT);
        PassEvents.send(owner.serverLevel(), PassEvents.at(pos),
                new PassFxPacket(op, owner.getId(), life, aux, flags, PassEvents.color(owner), v(pos, vel)));
    }

    public static void sync(ServerPlayer owner, int op, int aux, Vec3 pos, Vec3 vel, int lifeLeft, boolean gravity) {
        int flags = PassFxPacket.F_SYNC | (PassEvents.copy(owner) ? PassFxPacket.F_COPY : 0) | (gravity ? 0 : PassFxPacket.F_FLOAT);
        PassEvents.send(owner.serverLevel(), PassEvents.at(pos),
                new PassFxPacket(op, 0, lifeLeft, aux, flags, PassEvents.color(owner), v(pos, vel)));
    }

    public static void hit(ServerPlayer owner, int aux, Entity target, Vec3 pos) {
        int flags = (PassEvents.copy(owner) ? PassFxPacket.F_COPY : 0) | (target != null ? PassFxPacket.F_TARGET : 0);
        PassEvents.send(owner.serverLevel(), PassEvents.at(pos),
                new PassFxPacket(PassFxPacket.LT_HIT, owner.getId(), target == null ? 0 : target.getId(), aux, flags, PassEvents.color(owner), v(pos, null)));
    }

    public static void gone(ServerLevel level, int aux, Vec3 pos) {
        PassEvents.send(level, PassEvents.at(pos), new PassFxPacket(PassFxPacket.GONE, 0, 0, aux, 0, 0, v(pos, null)));
    }

    public static void touch(ServerPlayer p, Entity target, Vec3 to) {
        PassEvents.lightning(p.serverLevel(), PassFxPacket.LT_TOUCH, p, target, 0, 0, to, null);
    }

    public static void retort(ServerPlayer victim, Entity attacker, Vec3 at) {
        PassEvents.lightning(victim.serverLevel(), PassFxPacket.LT_RETORT, victim, attacker, 0, 0, at, null);
    }
}
