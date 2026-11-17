package com.myriam.unrp.pass;

import com.myriam.unrp.ability.AbilityEngine;
import com.myriam.unrp.net.Net;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "unrp")
public final class PassSync {
    private PassSync() {}

    private static final Map<UUID, PassStatePacket> SENT = new HashMap<>();
    private static final Map<UUID, Set<String>> EXTRA = new HashMap<>();
    private static final Map<UUID, Integer> LOCKED = new HashMap<>();
    private static final Map<UUID, Boolean> TRANSFORMED = new HashMap<>();

    public static void key(ServerPlayer p, String key, boolean on) {
        Set<String> s = EXTRA.computeIfAbsent(p.getUUID(), k -> new HashSet<>());
        if (on) s.add(key);
        else s.remove(key);
    }

    public static void predator(ServerPlayer p, int locked, boolean transformed) {
        LOCKED.put(p.getUUID(), locked);
        TRANSFORMED.put(p.getUUID(), transformed);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.getServer() == null) return;
        for (ServerPlayer p : e.getServer().getPlayerList().getPlayers()) {
            UUID id = p.getUUID();

            com.myriam.unrp.ability.Superpowers.syncTags(p); // unrp.has.* (dont aura_manipulation)
            AbilityEngine.syncHoldTags(p);
            com.myriam.unrp.ability.Specials2.modelTags(p);
            com.myriam.unrp.ability.Specials4.modelTags(p);
            com.myriam.unrp.ability.Specials7.modelTags(p);
            com.myriam.unrp.ability.Poses.tickTags(p);
            PassStatePacket now = AbilityEngine.passState(p, EXTRA.getOrDefault(id, Set.of()), LOCKED.getOrDefault(id, -1), TRANSFORMED.getOrDefault(id, false));
            if (!now.equals(SENT.get(id))) {
                SENT.put(id, now);
                Net.CHANNEL.send(PacketDistributor.ALL.noArg(), now);
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer joiner)) return;
        for (PassStatePacket s : SENT.values()) Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> joiner), s);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        UUID id = e.getEntity().getUUID();
        SENT.remove(id);
        EXTRA.remove(id);
        LOCKED.remove(id);
        TRANSFORMED.remove(id);
        Net.CHANNEL.send(PacketDistributor.ALL.noArg(), new PassStatePacket(id, java.util.List.of(), java.util.List.of(), java.util.List.of(), false, -1, false));
    }
}
