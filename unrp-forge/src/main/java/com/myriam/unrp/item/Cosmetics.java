package com.myriam.unrp.item;

import com.myriam.unrp.net.CosmeticPacket;
import com.myriam.unrp.net.Net;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

public final class Cosmetics {
    private Cosmetics() {}

    public static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static final Map<UUID, String[]> CLIENT = new ConcurrentHashMap<>();

    private static int idx(EquipmentSlot s) {
        for (int i = 0; i < SLOTS.length; i++) if (SLOTS[i] == s) return i;
        return 1;
    }

    private static String[] read(ServerPlayer p) {
        CompoundTag d = UnItems.persisted(p).getCompound("unrp_cosmetics");
        String[] out = new String[4];
        for (int i = 0; i < 4; i++) out[i] = d.getString(SLOTS[i].getName());
        return out;
    }

    private static void write(ServerPlayer p, String[] v) {
        CompoundTag d = new CompoundTag();
        for (int i = 0; i < 4; i++) if (!v[i].isEmpty()) d.putString(SLOTS[i].getName(), v[i]);
        UnItems.persisted(p).put("unrp_cosmetics", d);
    }

    private static String idOf(UnItems.Outfit o) {
        ResourceLocation k = ForgeRegistries.ITEMS.getKey(o);
        return k == null ? "" : k.getPath();
    }

    public static void toggle(ServerPlayer p, UnItems.Outfit o) {
        String[] v = read(p);
        int i = idx(o.slot);
        String id = idOf(o);
        if (id.equals(v[i])) {
            v[i] = "";
            p.displayClientMessage(Component.literal("§7Clothing removed."), true);
        } else {
            v[i] = id;
            p.displayClientMessage(Component.literal("§dClothing equipped."), true);
        }
        write(p, v);
        send(p, PacketDistributor.ALL.noArg(), v);
    }

    /** Retire tout (commande ou nettoyage). */
    public static void clear(ServerPlayer p) {
        String[] v = {"", "", "", ""};
        write(p, v);
        send(p, PacketDistributor.ALL.noArg(), v);
    }

    private static void send(ServerPlayer owner, PacketDistributor.PacketTarget target, String[] v) {
        Net.CHANNEL.send(target, new CosmeticPacket(owner.getUUID(), v));
    }

    private static void prune(ServerPlayer p, String[] v) {
        for (int i = 0; i < 4; i++) {
            if (v[i].isEmpty()) continue;
            boolean has = false;
            for (ItemStack s : p.getInventory().items) {
                if (s.getItem() instanceof UnItems.Outfit o && idOf(o).equals(v[i])) {
                    has = true;
                    break;
                }
            }
            if (!has) v[i] = "";
        }
    }

    public static void onLogin(ServerPlayer joiner) {
        String[] mine = read(joiner);
        prune(joiner, mine);
        write(joiner, mine);
        for (ServerPlayer other : joiner.server.getPlayerList().getPlayers()) {
            if (other != joiner) Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> joiner), new CosmeticPacket(other.getUUID(), read(other)));
        }
        send(joiner, PacketDistributor.ALL.noArg(), mine);
    }
}
