package com.myriam.unrp.ability;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

public final class Scale {
    private Scale() {}

    public static boolean available() {
        return ModList.get().isLoaded("pehkui");
    }

    public static void set(ServerPlayer p, double base) {
        run(p, "scale set pehkui:base " + base + " " + p.getGameProfile().getName());
    }

    public static void motion(ServerPlayer p, double motion) {
        run(p, "scale set pehkui:motion " + motion + " " + p.getGameProfile().getName());
    }

    private static void run(ServerPlayer p, String cmd) {
        MinecraftServer s = p.getServer();
        if (s == null || !available()) return;
        s.getCommands().performPrefixedCommand(s.createCommandSourceStack().withSuppressedOutput(), cmd);
    }

    public static boolean roomFor(ServerPlayer p, float grow, float current) {
        if (grow <= current || !available()) return true;
        double half = 0.3 * grow;
        Vec3 at = p.position();
        AABB box = new AABB(at.x - half, at.y, at.z - half, at.x + half, at.y + 1.8 * grow, at.z + half).deflate(1e-7);
        return p.level().noCollision(p, box);
    }
}
