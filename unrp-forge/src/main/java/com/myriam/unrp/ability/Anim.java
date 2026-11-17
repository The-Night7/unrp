package com.myriam.unrp.ability;

import com.myriam.unrp.item.ModItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Anim {
    private Anim() {}

    private static final Map<UUID, Double> BASE_Y = new HashMap<>();

    private static boolean tag(ItemStack s, String k) {
        return !s.isEmpty() && s.hasTag() && s.getTag().getBoolean(k);
    }

    public static void tick(MinecraftServer server) {
        long t = server.overworld().getGameTime();
        if (t % 2 != 0) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ServerLevel lv = p.serverLevel();
            ItemStack main = p.getMainHandItem();
            boolean axe = tag(main, "unrp_ds_axe"), arm = tag(main, "unrp_ad");
            if (axe || arm) heldBlade(lv, p, axe ? 0xFFBF3F : 0xE6C65A, axe ? 1.7 : 1.3, axe, t);
            if (p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.JOKER_MASK.get()) && t % 10 == 0) {
                Vfx.glow(lv, Vfx.RING, 0xE040A0, p.position().add(0, p.getBbHeight() + 0.25, 0), new Vec3(0.2, 0, 0), 1, 0);
            }
            if (t % 4 == 0) {
                AABB box = p.getBoundingBox().inflate(40);
                for (ArmorStand c : lv.getEntitiesOfClass(ArmorStand.class, box, a -> a.getTags().contains("unrp.pconstruct"))) construct(lv, c, t);
                for (FallingBlockEntity fb : lv.getEntitiesOfClass(FallingBlockEntity.class, box, f -> f.time <= 1)) cube(lv, fb, t);
            }
        }
    }

    private static void heldBlade(ServerLevel lv, ServerPlayer p, int rgb, double len, boolean axe, long t) {
        Vec3 look = p.getLookAngle().normalize();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 hand = p.position().add(0, 1.15, 0).add(right.scale(-0.35)).add(look.scale(0.45));
        float w = (float) (0.05 + 0.02 * Math.sin(t * 0.4));
        Vec3 tip = hand.add(look.scale(len));
        Fx.line(lv, hand, tip, rgb, Styles.LINE_NOCORE, w, 3);
        if (axe) {
            Vec3 up = new Vec3(0, 1, 0);
            Fx.line(lv, tip.add(up.scale(0.35)), tip.add(up.scale(-0.35)).add(look.scale(0.12)), rgb, 0, w * 1.6f, 3);
        }
        if (p.swinging) {
            for (int i = -1; i <= 1; i++) {
                Vec3 d = look.add(right.scale(0.45 * i)).normalize();
                Fx.line(lv, hand, hand.add(d.scale(len * 1.5)), rgb, Styles.LINE_WAVY, 0.1f, 6);
            }
        }
    }

    private static void construct(ServerLevel lv, ArmorStand c, long t) {
        double base = BASE_Y.computeIfAbsent(c.getUUID(), k -> c.getY());
        c.setYRot(c.getYRot() + 6f);
        c.setYBodyRot(c.getYRot());
        c.setYHeadRot(c.getYRot());
        c.setPos(c.getX(), base + 0.12 * Math.sin(t * 0.12), c.getZ());
        if (t % 12 == 0) {
            Vfx.glow(lv, Vfx.RING, 0xE0A0FF, new Vec3(c.getX(), base + 0.05, c.getZ()), new Vec3(0.5, 0, 0), 1, 0);
            Fx.line(lv, new Vec3(c.getX(), base, c.getZ()), new Vec3(c.getX(), base + 3.0, c.getZ()), 0xE0A0FF, Styles.LINE_WAVY | Styles.LINE_NOCORE, 0.05f, 12);
        }
    }

    private static void cube(ServerLevel lv, FallingBlockEntity fb, long t) {
        Vec3 c = fb.position().add(0, 0.5, 0);
        double a = t * 0.15, r = 0.62;
        Vec3[] top = new Vec3[4], bot = new Vec3[4];
        for (int i = 0; i < 4; i++) {
            double ang = a + i * Math.PI / 2;
            Vec3 o = new Vec3(Math.cos(ang) * r, 0, Math.sin(ang) * r);
            top[i] = c.add(o).add(0, 0.55, 0);
            bot[i] = c.add(o).add(0, -0.55, 0);
        }
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) % 4;
            Fx.line(lv, top[i], top[j], 0x9AC8FF, Styles.LINE_NOCORE, 0.03f, 5);
            Fx.line(lv, bot[i], bot[j], 0x9AC8FF, Styles.LINE_NOCORE, 0.03f, 5);
            Fx.line(lv, top[i], bot[i], 0x9AC8FF, Styles.LINE_NOCORE, 0.03f, 5);
        }
    }
}
