package com.myriam.unrp.pass;

import com.myriam.unrp.data.AbilityPower;
import com.myriam.unrp.data.UnodexData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class AmPassLite {
    private AmPassLite() {}

    public static final String[] FAMILY = {"lightning", "barrier", "energy_discharge", "time_manipulation", "predator"};
    public static final int[] FAMILY_RGB = {4568319, 15914308, 16726586, 16740288, 15242256};

    static PassFxPacket packet(int op, ServerPlayer p, int target, int aux, int flags, int color) {
        Vec3 at = p.position();
        return new PassFxPacket(op, p.getId(), target, aux, flags, color, new double[] {at.x, at.y, at.z, 0, 0, 0});
    }

    static void tell(ServerPlayer p, PassFxPacket pkt) {
        if (p.isInvisible()) TimePass.to(p, pkt);
        else TimePass.send(p.serverLevel(), pkt);
    }

    static int lift(int rgb) {
        int r = rgb >> 16 & 255, g = rgb >> 8 & 255, b = rgb & 255;
        double l = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0;
        if (l >= 0.3) return rgb & 0xFFFFFF;
        double k = (0.3 - l) / (1.0 - l);
        return (int) Math.round(r + (255 - r) * k) << 16 | (int) Math.round(g + (255 - g) * k) << 8 | (int) Math.round(b + (255 - b) * k);
    }

    static int lookColor(String s) {
        AbilityPower pw = UnodexData.get().powers.get(s);
        return pw != null && pw.look != null && pw.look.rgb() >= 0 ? pw.look.rgb() : 0xFFFFFF;
    }

    static int one(String s) {
        for (int i = 0; i < FAMILY.length; i++) if (FAMILY[i].equals(s)) return FAMILY_RGB[i];
        return lift(lookColor(s));
    }

    public static int rgb(String s) {
        for (String[] c : AmRoster.COMBOS) {
            if (!c[0].equals(s)) continue;
            int n = c.length - 2, r = 0, g = 0, b = 0;
            for (int i = 2; i < c.length; i++) {
                int x = one(c[i]);
                r += x >> 16 & 255;
                g += x >> 8 & 255;
                b += x & 255;
            }
            return Math.round((float) r / n) << 16 | Math.round((float) g / n) << 8 | Math.round((float) b / n);
        }
        return one(s);
    }

    public static void sense(ServerPlayer p, double radius) {
        TimePass.to(p, packet(PassFxPacket.AM_SENSE, p, 0, (int) radius, 0, 0));
        java.util.List<net.minecraft.world.entity.Entity> near = new java.util.ArrayList<>();
        var box = net.minecraft.world.phys.AABB.ofSize(p.position(), radius * 2, radius * 2, radius * 2);
        for (var t : p.serverLevel().getEntities(p, box)) {
            if (!(t instanceof net.minecraft.world.entity.LivingEntity) || t instanceof net.minecraft.world.entity.decoration.ArmorStand || !t.isAlive()) continue;
            String s = t instanceof ServerPlayer sp ? com.myriam.unrp.ability.AbilityEngine.basePowerId(sp) : null;
            if (s != null && AmRoster.slotOf(s) > 0 && !s.equals("nightmare") && !s.equals("hypnosis") && !s.equals("telekinesis")) near.add(t);
        }
        near.sort(java.util.Comparator.comparingDouble(t -> t.distanceToSqr(p)));
        for (int i = 0; i < near.size() && i < 48; i++) {
            var t = near.get(i);
            String s = com.myriam.unrp.ability.AbilityEngine.basePowerId((ServerPlayer) t);
            TimePass.to(p, packet(PassFxPacket.AM_SENSE, p, t.getId(), 0, PassFxPacket.F_TARGET, rgb(s)));
        }
    }

    public static void on(ServerPlayer p) {
        tell(p, packet(PassFxPacket.AM_ON, p, 0, 0, 0, 0));
    }

    public static void take(ServerPlayer p, String s) {
        TimePass.to(p, packet(PassFxPacket.AM_TAKE, p, 0, AmRoster.slotOf(s), 0, rgb(s)));
        com.myriam.unrp.ability.Poses.play(p, "am_take");
    }

    public static void expel(ServerPlayer p) {
        com.myriam.unrp.ability.Poses.play(p, "am_castoff");
        tell(p, packet(PassFxPacket.AM_EXPEL, p, 0, 0, 0, 0));
    }
}
