package com.myriam.unrp.am;

import com.myriam.unrp.ability.AbilityEngine;
import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.net.Net;
import com.myriam.unrp.pass.AmPassLite;
import com.myriam.unrp.pass.AmRoster;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "unrp")
public final class AmWheelBridge {
    private AmWheelBridge() {}

    static final String[] MENTAL = {"nightmare", "hypnosis", "telekinesis"};
    static final double SCAN_RADIUS = 24.0;
    private static final Map<UUID, boolean[]> NEARBY = new HashMap<>();
    private static final Map<UUID, Integer> TICK = new HashMap<>();

    static boolean mental(String s) {
        for (String m : MENTAL) if (m.equals(s)) return true;
        return false;
    }

    static boolean has(ServerPlayer p) {
        return "aura_manipulation".equals(AbilityEngine.basePowerId(p));
    }

    static boolean armed(ServerPlayer p) {
        return has(p) && AbilityEngine.isOn(p, "aura_manipulation") && !AbilityEngine.disabled(p);
    }

    static String powerOf(Entity e) {
        if (e instanceof ServerPlayer sp) return AbilityEngine.basePowerId(sp);
        for (String t : e.getTags()) if (t.startsWith("unrp.power.")) return t.substring("unrp.power.".length());
        return null;
    }

    static int powerLevel(ServerPlayer p) {
        return (int) Math.round(ClaimData.get(p.server).profileOf(p.getUUID()).map(pr -> pr.level * 10).orElse(0.0));
    }

    static void bar(ServerPlayer p, String msg) {
        p.displayClientMessage(Component.literal(msg), true);
    }

    public static void open(ServerPlayer p) {
        scan(p);
        send(p, true);
        AmPassLite.sense(p, SCAN_RADIUS);
    }

    public static void ask(ServerPlayer p) {
        send(p, false);
    }

    static void send(ServerPlayer p, boolean open) {
        if (!has(p)) return;
        boolean[] near = NEARBY.getOrDefault(p.getUUID(), new boolean[55]);
        Set<String> copied = AbilityEngine.copies(p);
        List<int[]> nearby = new ArrayList<>();
        for (int slot = 1; slot <= 54; slot++) {
            String s = AmRoster.SLOT_SHORTS[slot];
            if (s == null) continue;
            boolean c = copied.contains(s);
            if (near[slot] || c) nearby.add(new int[] {slot, c ? 1 : 0});
        }
        int n = AmRoster.COMBOS.length;
        boolean[] ready = new boolean[n], held = new boolean[n];
        for (int i = 0; i < n; i++) held[i] = copied.contains(AmRoster.COMBOS[i][0]);
        Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new AmWheelPacket(AbilityEngine.aura(p), copied.size(), nearby, ready, held, open));
    }

    static void scan(ServerPlayer p) {
        boolean[] near = new boolean[55];
        AABB box = AABB.ofSize(p.position(), SCAN_RADIUS * 2, SCAN_RADIUS * 2, SCAN_RADIUS * 2);
        for (Entity t : p.serverLevel().getEntities(p, box)) {
            if (!(t instanceof LivingEntity) || t instanceof ArmorStand || !t.isAlive()) continue;
            String s = powerOf(t);
            int slot = s == null ? -1 : AmRoster.slotOf(s);
            if (slot > 0 && !mental(s)) near[slot] = true;
        }
        NEARBY.put(p.getUUID(), near);
    }

    static Entity findTarget(ServerPlayer p, String s) {
        AABB box = AABB.ofSize(p.position(), SCAN_RADIUS * 2, SCAN_RADIUS * 2, SCAN_RADIUS * 2);
        Entity best = null;
        double bd = Double.MAX_VALUE;
        for (Entity t : p.serverLevel().getEntities(p, box)) {
            if (!(t instanceof LivingEntity) || !t.isAlive() || !s.equals(powerOf(t))) continue;
            double d = t.distanceToSqr(p);
            if (d < bd) { bd = d; best = t; }
        }
        return best;
    }

    public static void pick(ServerPlayer p, int mode, int id) {
        if (!armed(p)) {
            bar(p, "§8L'aura sommeille.");
            return;
        }
        if (mode == 0) {
            if (id < 1 || id > 54 || AmRoster.SLOT_SHORTS[id] == null) return;
            doCopy(p, id);
        } else if (mode == 1) {
            bar(p, "§cThis fusion is not playable yet.");
        } else if (mode == 2) {
            if (AbilityEngine.copies(p).isEmpty()) return;
            AbilityEngine.setCopy(p, null, 0);
            AmPassLite.expel(p);
        }
        send(p, false);
    }

    static void doCopy(ServerPlayer p, int slot) {
        String s = AmRoster.SLOT_SHORTS[slot];
        if (mental(s)) { bar(p, "§c§lMental powers cannot be copied!"); return; }
        if (AbilityEngine.aura(p) <= 0) { bar(p, "§c§lPas assez d'aura !"); return; }
        if (AbilityEngine.copies(p).contains(s)) { bar(p, "§c§lAlready copied!"); return; }
        if (com.myriam.unrp.data.UnodexData.get().powers.get(s) == null) { bar(p, "§cThis power is not playable yet."); return; }
        boolean[] near = NEARBY.get(p.getUUID());
        if (near == null || !near[slot]) { bar(p, "§c§lThe target is no longer in range."); return; }
        Entity target = findTarget(p, s);
        if (target instanceof ServerPlayer escrowOwner && com.myriam.unrp.ability.Specials17.branchEscrowed(escrowOwner)) {
            bar(p, "§6This ability contains an escrowed branch. Copy refused."); return;
        }
        if (target instanceof ServerPlayer tp && powerLevel(tp) > powerLevel(p) + 5) {
            bar(p, "§c§lThe target's power level is too high to copy!");
            return;
        }
        AbilityEngine.setCopy(p, s, Long.MAX_VALUE);
        com.myriam.unrp.ability.Poses.timedTag(p, "unrp.pose.am_copy", 12);
        AbilityEngine.spendAura(p, 65);
        AmPassLite.take(p, s);
        bar(p, "§6§lCopied: " + PowerNames.display(s) + " !");
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        if (!has(p)) {
            NEARBY.remove(p.getUUID());
            return;
        }
        if (!AbilityEngine.isOn(p, "aura_manipulation")) {
            if (!AbilityEngine.copies(p).isEmpty()) {
                AbilityEngine.setCopy(p, null, 0);
                bar(p, "§7Powers released.");
            }
            return;
        }
        int t = TICK.merge(p.getUUID(), 1, Integer::sum);
        if (t % 5 == 0) scan(p);
        if (t % 10 == 0 && AbilityEngine.aura(p) <= 0 && !AbilityEngine.copies(p).isEmpty()) {
            AbilityEngine.setCopy(p, null, 0);
            AmPassLite.expel(p);
        }
    }
}
