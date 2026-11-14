package com.myriam.unrp.pass;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Energy Discharge vers le moteur d'effets du modèle (PassEvents.blast). */
public final class EdPass {
    private EdPass() {}

    /** ED_BLAST : la sphère éclate (centre = position du projectile, comme pr.getEyeY d'une petite entité). */
    public static void blast(ServerPlayer owner, int aux, Vec3 pos) {
        int flags = PassEvents.copy(owner) ? PassFxPacket.F_COPY : 0;
        PassEvents.send(owner.serverLevel(), PassEvents.at(pos), new PassFxPacket(PassFxPacket.ED_BLAST, owner.getId(), 0, aux, flags,
                PassEvents.color(owner), new double[] {pos.x, pos.y, pos.z, pos.x, pos.y, pos.z}));
    }
}
