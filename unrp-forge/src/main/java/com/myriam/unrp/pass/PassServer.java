package com.myriam.unrp.pass;

import com.myriam.unrp.style.StyleService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class PassServer {
    private PassServer() {}

    public static final int UNSET = 0x8A4DFF;

    public static int color(Entity user) {
        if (!(user instanceof ServerPlayer sp)) return 0xFFFFFF;
        if (sp.getTags().contains("unrp.has.aura_manipulation")) return 0x26262C;
        int c = StyleService.get(sp.server, sp.getUUID()).auraColor & 0xFFFFFF;
        return c == UNSET ? 0xFFFFFF : c;
    }
}
