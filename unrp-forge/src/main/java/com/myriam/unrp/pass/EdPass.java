package com.myriam.unrp.pass;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class EdPass {
    private EdPass() {}

    public static void blast(ServerPlayer owner, int aux, Vec3 pos) {
        int flags = PassEvents.copy(owner) ? PassFxPacket.F_COPY : 0;
        PassEvents.send(owner.serverLevel(), PassEvents.at(pos), new PassFxPacket(PassFxPacket.ED_BLAST, owner.getId(), 0, aux, flags,
                PassEvents.color(owner), new double[] {pos.x, pos.y, pos.z, pos.x, pos.y, pos.z}));
    }
}
