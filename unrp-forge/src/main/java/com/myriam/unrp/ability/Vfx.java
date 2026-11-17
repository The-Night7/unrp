package com.myriam.unrp.ability;

import com.myriam.unrp.net.FxPacket;
import com.myriam.unrp.net.Net;
import com.myriam.unrp.particle.FxOptions;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public final class Vfx {
    private Vfx() {}

    public static final int SPARKS = 0, RING = 1, FLASH = 2;

    public static void emit(ServerLevel lv, ParticleOptions po, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        String name = BuiltInRegistries.PARTICLE_TYPE.getKey(po.getType()) == null ? "" : BuiltInRegistries.PARTICLE_TYPE.getKey(po.getType()).getPath();
        int rgb = colorOf(po, name);
        int shape = switch (name) {
            case "explosion", "explosion_emitter", "flash" -> FLASH;
            case "sweep_attack" -> RING;
            default -> name.equals("ring") ? RING : SPARKS;
        };
        glow(lv, shape, rgb, new Vec3(x, y, z), new Vec3(dx, dy, dz), count, speed);
    }

    public static void glow(ServerLevel lv, int shape, int rgb, Vec3 at, Vec3 spread, int count, double speed) {
        if (Fx.QUIET > 0) return;
        FxPacket p = new FxPacket();
        p.kind = FxPacket.GLOW;
        p.id = shape;
        p.color = rgb;
        p.life = shape == SPARKS ? 9 : 12;
        p.extra = count <= 0 ? -1 : Math.min(14, (count + 1) / 2);
        p.x1 = at.x; p.y1 = at.y; p.z1 = at.z;
        p.x2 = spread.x; p.y2 = spread.y; p.z2 = spread.z;
        p.a = (float) speed;
        Net.CHANNEL.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(at.x, at.y, at.z, 48, lv.dimension())), p);
    }

    private static int colorOf(ParticleOptions po, String n) {
        if (po instanceof DustParticleOptions d) return pack(d.getColor().x(), d.getColor().y(), d.getColor().z());
        if (po instanceof DustColorTransitionOptions d) return pack(d.getFromColor().x(), d.getFromColor().y(), d.getFromColor().z());
        if (po instanceof FxOptions f) return f.rgb;
        return switch (n) {
            case "flame", "small_flame", "campfire_cosy_smoke" -> 0xFF9A2E;
            case "lava", "drip_lava", "falling_lava" -> 0xFF5A1A;
            case "soul_fire_flame", "soul" -> 0x5AD6FF;
            case "end_rod", "flash", "explosion", "explosion_emitter", "sweep_attack" -> 0xFFFFFF;
            case "crit" -> 0xFFE7A8;
            case "enchanted_hit", "enchant" -> 0x9AE0FF;
            case "cloud", "poof" -> 0xE8EEF2;
            case "smoke", "large_smoke" -> 0x6A6A6A;
            case "squid_ink", "ash" -> 0x1A1A24;
            case "electric_spark" -> 0xAFE4FF;
            case "glow", "glow_squid_ink" -> 0xFFE08A;
            case "witch", "portal", "reverse_portal" -> 0xB070FF;
            case "damage_indicator", "angry_villager" -> 0xC0202A;
            case "heart", "happy_villager" -> 0x7CE08A;
            case "bubble", "splash", "rain", "dripping_water" -> 0x8CC8FF;
            case "snowflake" -> 0xEAF6FF;
            default -> 0xFFFFFF;
        };
    }

    private static int pack(float r, float g, float b) {
        return (Math.round(r * 255) << 16) | (Math.round(g * 255) << 8) | Math.round(b * 255);
    }
}
