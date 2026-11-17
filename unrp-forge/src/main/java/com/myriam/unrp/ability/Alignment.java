package com.myriam.unrp.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class Alignment {
    private Alignment() {}

    public static final int ROSE_AT = -20;

    public static int of(ServerPlayer p) {
        return com.myriam.unrp.item.UnItems.persisted(p).getInt("unrp_align");
    }

    private static int add(ServerPlayer p, int delta) {
        int now = Math.max(-100, Math.min(50, of(p) + delta));
        if (delta != 0) com.myriam.unrp.item.UnItems.persisted(p).putInt("unrp_align", now);
        return now;
    }

    public static boolean rose(ServerPlayer p) {
        return of(p) <= ROSE_AT;
    }

    public static void onDeath(LivingDeathEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer k)) return;
        LivingEntity v = e.getEntity();
        if (v == k) return;
        int delta = 0;
        if (v instanceof AbstractVillager) delta = -5;
        else if (v instanceof IronGolem g && g.isPlayerCreated()) delta = -8;
        else if (v instanceof TamableAnimal pet && pet.isTame() && !k.getUUID().equals(pet.getOwnerUUID())) delta = -6;
        if (delta == 0) return;
        int before = of(k), now = add(k, delta);
        if (before > ROSE_AT && now <= ROSE_AT) k.sendSystemMessage(Component.literal("§d[Unrp] §7Something is curdling inside your power. §dIt suits you."));
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) if (p.tickCount != 0 && p.tickCount % 36000 == 0 && of(p) < 0) add(p, 1);
    }
}
