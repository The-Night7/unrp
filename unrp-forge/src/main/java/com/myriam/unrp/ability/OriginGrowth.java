package com.myriam.unrp.ability;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Croissance « Vessel » des capacités Origin, reprise de points/OriginGrowth du modèle : un niveau qui monte par l'expérience
 * gagnée en combat (40 + 6 x niveau par palier, sans plafond), et des points de croissance (1 par niveau jusqu'à 20, puis de moins
 * en moins) qui donnent PV, dégâts, armure et résistance au recul. Sauvegardé sur le joueur.
 */
public final class OriginGrowth {
    private OriginGrowth() {}

    public static final double HP_PER_POINT = 0.5, DMG_PER_POINT = 0.15, ARMOR_PER_POINT = 0.25, KB_PER_POINT = 0.005;
    private static final UUID HEALTH_ID = UUID.fromString("9c2e64b7-3f18-4a52-8d0b-71ce95a4f206");
    private static final UUID DAMAGE_ID = UUID.fromString("4b8f1d92-6ca7-40e3-b915-2fd80c67a381");
    private static final UUID ARMOR_ID = UUID.fromString("e1573a48-90bd-4f26-a7c9-08643d1b5e72");
    private static final UUID KB_ID = UUID.fromString("07f8b264-d31a-4e95-bc07-6529ade18c43");
    private static int[] sums = {0};
    private static final Map<UUID, String> OWNER = new HashMap<>();

    private static CompoundTag data(ServerPlayer p) {
        CompoundTag root = com.myriam.unrp.item.UnItems.persisted(p);
        if (!root.contains("unrp_og")) root.put("unrp_og", new CompoundTag());
        return root.getCompound("unrp_og");
    }

    public static int level(ServerPlayer p) {
        return Math.max(0, data(p).getInt("level"));
    }

    public static int xp(ServerPlayer p) {
        return Math.max(0, data(p).getInt("xp"));
    }

    public static double points(ServerPlayer p) {
        return growthMilli(level(p)) / 1000.0;
    }

    public static synchronized int growthMilli(int level) {
        if (level <= 0) return 0;
        int lv = Math.min(level, 100000);
        if (lv >= sums.length) {
            int[] next = Arrays.copyOf(sums, Math.min(100001, Math.max(lv + 1, sums.length * 2)));
            for (int l = sums.length; l < next.length; l++) next[l] = next[l - 1] + gainMilli(l);
            sums = next;
        }
        return sums[lv] + (level - lv);
    }

    /** Points de croissance gagnés au niveau donné, en millièmes : 1000 jusqu'au 20, puis -50 tous les 5 niveaux, puis x0,95. */
    public static int gainMilli(int level) {
        if (level <= 20) return 1000;
        int step = (level - 20 - 1) / 5;
        if (step < 19) return 950 - 50 * step;
        int t = step - 19;
        double g = 10.0;
        for (int i = 0; i < t && g > 1.0; i++) g *= 0.95;
        return Math.max(1, (int) g);
    }

    public static int xpToNext(int level) {
        return 40 + 6 * level;
    }

    /** Ajoute de l'expérience ; renvoie le nombre de niveaux gagnés. */
    public static int award(ServerPlayer p, int xp) {
        if (xp <= 0) return 0;
        int lv = level(p), have = xp(p) + xp, gained = 0;
        while (have >= xpToNext(lv)) {
            have -= xpToNext(lv);
            lv++;
            gained++;
        }
        data(p).putInt("level", lv);
        data(p).putInt("xp", have);
        return gained;
    }

    /** Pose les bonus de croissance (le pouvoir « key » en est propriétaire) ; base = malus de départ du pouvoir. */
    public static void apply(ServerPlayer p, String key, boolean has, double baseHp, double baseDmg, double baseArmor) {
        String owner = OWNER.get(p.getUUID());
        if (has) OWNER.put(p.getUUID(), key);
        else {
            if (owner == null || !owner.equals(key)) return;
            OWNER.remove(p.getUUID());
        }
        double pts = has ? points(p) : 0;
        mod(p, Attributes.MAX_HEALTH, HEALTH_ID, "unrp_growth_hp", has ? baseHp + pts * HP_PER_POINT : 0);
        mod(p, Attributes.ATTACK_DAMAGE, DAMAGE_ID, "unrp_growth_dmg", has ? baseDmg + pts * DMG_PER_POINT : 0);
        mod(p, Attributes.ARMOR, ARMOR_ID, "unrp_growth_armor", has ? baseArmor + pts * ARMOR_PER_POINT : 0);
        mod(p, Attributes.KNOCKBACK_RESISTANCE, KB_ID, "unrp_growth_kb", has ? Math.min(0.35, pts * KB_PER_POINT) : 0);
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }

    static void mod(ServerPlayer p, Attribute attr, UUID id, String name, double amount) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        AttributeModifier had = inst.getModifier(id);
        if (had == null && Math.abs(amount) < 0.001) return;
        if (had != null && Math.abs(had.getAmount() - amount) < 0.001) return;
        if (had != null) inst.removeModifier(id);
        if (Math.abs(amount) > 0.001) inst.addTransientModifier(new AttributeModifier(id, name, amount, AttributeModifier.Operation.ADDITION));
    }

    public static void forget(UUID id) {
        OWNER.remove(id);
    }
}
