package com.myriam.unrp.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class Walls {
    private record Pending(ServerLevel level, BlockPos pos, BlockState placed, long until) {}

    private static final List<Pending> LIST = new ArrayList<>();

    private Walls() {}

    static boolean place(ServerLevel level, BlockPos pos, BlockState state, long ttlTicks) {
        if (!level.getBlockState(pos).isAir()) return false;
        level.setBlock(pos, state, 3);
        LIST.add(new Pending(level, pos.immutable(), state, level.getGameTime() + ttlTicks));
        Vfx.glow(level, Vfx.SPARKS, 0xCFE8FF, net.minecraft.world.phys.Vec3.atCenterOf(pos), new net.minecraft.world.phys.Vec3(0.4, 0.4, 0.4), 6, 0.02);
        return true;
    }

    static void tick(MinecraftServer server) {
        Iterator<Pending> it = LIST.iterator();
        while (it.hasNext()) {
            Pending p = it.next();
            if (p.level.getGameTime() >= p.until) {
                restore(p);
                it.remove();
            }
        }
    }

    static void restoreAll() {
        LIST.forEach(Walls::restore);
        LIST.clear();
    }

    private static void restore(Pending p) {

        if (p.level.getBlockState(p.pos) == p.placed) {
            p.level.setBlock(p.pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            Vfx.glow(p.level, Vfx.SPARKS, 0xCFE8FF, net.minecraft.world.phys.Vec3.atCenterOf(p.pos), new net.minecraft.world.phys.Vec3(0.4, 0.4, 0.4), 10, 0.05); // il s'effrite
        }
    }
}
