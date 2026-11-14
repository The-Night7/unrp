package com.myriam.unrp.net;

import com.myriam.unrp.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Serveur -> client : tout ce qu'il faut pour afficher le menu (le client n'a pas les JSON).
 * open  = ouvrir le menu s'il est fermé ; close = le fermer (candidature acceptée) ; message = texte d'état.
 */
public class MenuDataPacket {
    public static class Entry {
        public String id, name, category, tier, level, ability, abilityDesc, schools;
        public boolean claimed;
    }

    public static class SchoolInfo {
        public String id, name;
    }

    public static class RarityInfo {
        public String label;
        public double percent;
    }

    public boolean open, close, hasProfile;
    public String message = "";
    public final List<Entry> entries = new ArrayList<>();
    public final List<SchoolInfo> schools = new ArrayList<>();
    public final List<RarityInfo> rarities = new ArrayList<>();

    public static void encode(MenuDataPacket p, FriendlyByteBuf buf) {
        buf.writeBoolean(p.open);
        buf.writeBoolean(p.close);
        buf.writeBoolean(p.hasProfile);
        buf.writeUtf(p.message, 512);
        buf.writeVarInt(p.entries.size());
        for (Entry e : p.entries) {
            buf.writeUtf(e.id, 64);
            buf.writeUtf(e.name, 64);
            buf.writeUtf(e.category, 16);
            buf.writeUtf(e.tier, 16);
            buf.writeUtf(e.level, 8);
            buf.writeUtf(e.ability, 96);
            buf.writeUtf(e.abilityDesc, 400);
            buf.writeUtf(e.schools, 160);
            buf.writeBoolean(e.claimed);
        }
        buf.writeVarInt(p.schools.size());
        for (SchoolInfo s : p.schools) {
            buf.writeUtf(s.id, 64);
            buf.writeUtf(s.name, 64);
        }
        buf.writeVarInt(p.rarities.size());
        for (RarityInfo r : p.rarities) {
            buf.writeUtf(r.label, 48);
            buf.writeDouble(r.percent);
        }
    }

    public static MenuDataPacket decode(FriendlyByteBuf buf) {
        MenuDataPacket p = new MenuDataPacket();
        p.open = buf.readBoolean();
        p.close = buf.readBoolean();
        p.hasProfile = buf.readBoolean();
        p.message = buf.readUtf(512);
        int n = buf.readVarInt();
        for (int i = 0; i < n; i++) {
            Entry e = new Entry();
            e.id = buf.readUtf(64);
            e.name = buf.readUtf(64);
            e.category = buf.readUtf(16);
            e.tier = buf.readUtf(16);
            e.level = buf.readUtf(8);
            e.ability = buf.readUtf(96);
            e.abilityDesc = buf.readUtf(400);
            e.schools = buf.readUtf(160);
            e.claimed = buf.readBoolean();
            p.entries.add(e);
        }
        n = buf.readVarInt();
        for (int i = 0; i < n; i++) {
            SchoolInfo s = new SchoolInfo();
            s.id = buf.readUtf(64);
            s.name = buf.readUtf(64);
            p.schools.add(s);
        }
        n = buf.readVarInt();
        for (int i = 0; i < n; i++) {
            RarityInfo r = new RarityInfo();
            r.label = buf.readUtf(48);
            r.percent = buf.readDouble();
            p.rarities.add(r);
        }
        return p;
    }

    public static void handle(MenuDataPacket p, Supplier<NetworkEvent.Context> ctx) {
        // ClientHooks n'est chargé que côté client (jamais sur un serveur dédié).
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.onMenuData(p)));
        ctx.get().setPacketHandled(true);
    }
}
