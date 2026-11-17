package com.myriam.unrp.pass;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class BarrierPunch {
    private BarrierPunch() {}

    private static long lastPunch = 0L;

    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        long now = player.level().getGameTime();
        if (now - lastPunch < 5L) return;
        for (BarrierEntity b : player.level().getEntitiesOfClass(BarrierEntity.class, player.getBoundingBox().inflate(8.0))) {
            float r = b.getSize() / 2.0F;
            Vec3 center = new Vec3(b.getX(), b.getY() + r, b.getZ());
            Vec3 pc = new Vec3(player.getX(), player.getY() + player.getBbHeight() / 2.0, player.getZ());
            if (pc.distanceTo(center) < r) {
                lastPunch = now;
                com.myriam.unrp.net.Net.CHANNEL.sendToServer(new com.myriam.unrp.net.PunchPacket());
                player.swing(InteractionHand.MAIN_HAND);
                return;
            }
        }
        Vec3 eye = player.getEyePosition(), look = player.getLookAngle();
        for (BarrierEntity b : player.level().getEntitiesOfClass(BarrierEntity.class, player.getBoundingBox().inflate(8.0))) {
            Vec3 c = b.center();
            double t = BarrierReflect.entry(eye.x, eye.y, eye.z, look.x, look.y, look.z, c.x, c.y, c.z, b.getSize() / 2.0);
            if (t >= 0.0 && t <= 3.0) {
                lastPunch = now;
                com.myriam.unrp.net.Net.CHANNEL.sendToServer(new com.myriam.unrp.net.PunchPacket());
                return;
            }
        }
    }
}
