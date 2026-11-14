package com.myriam.unrp.ability;

import com.myriam.unrp.net.FxPacket;
import com.myriam.unrp.net.Net;
import com.myriam.unrp.particle.FxOptions;
import com.myriam.unrp.particle.ModParticles;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

/**
 * Côté serveur : annonce les effets visuels aux joueurs proches (64 blocs).
 * Rendu : client/FxRenderer (traits, barrières, boucliers, ondes) et client/FxParticle (particules teintées).
 */
public final class Fx {
    private static final double RANGE = 64;

    private Fx() {}

    /** > 0 : le moteur d'effets du modèle dessine ce pouvoir, nos effets dessinés se taisent. */
    public static int QUIET = 0;

    private static void send(ServerLevel level, Vec3 at, FxPacket p) {
        if (QUIET > 0) return;
        Net.CHANNEL.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(at.x, at.y, at.z, RANGE, level.dimension())), p);
    }

    /** Trait lumineux de a à b. flags = Styles.LINE_* (zigzag, ondulé, double hélice, sans cœur blanc) ; width en blocs ; life en ticks. */
    public static void line(ServerLevel level, Vec3 a, Vec3 b, int rgb, int flags, float width, int life) {
        FxPacket p = new FxPacket();
        p.kind = FxPacket.LINE;
        p.x1 = a.x; p.y1 = a.y; p.z1 = a.z;
        p.x2 = b.x; p.y2 = b.y; p.z2 = b.z;
        p.color = rgb;
        p.extra = flags;
        p.a = width;
        p.life = life;
        p.id = level.random.nextInt(1 << 20);
        send(level, a, p);
    }

    /** Barrière : création ou mise à jour (rayon, état des PV 0..1, éclair de choc si hit). */
    public static void shell(ServerLevel level, int id, Vec3 c, double radius, int rgb, float hpFrac, boolean hit) {
        FxPacket p = new FxPacket();
        p.kind = FxPacket.SHELL;
        p.id = id;
        p.x1 = c.x; p.y1 = c.y; p.z1 = c.z;
        p.x2 = hpFrac;
        p.a = (float) radius;
        p.color = rgb;
        p.extra = hit ? 1 : 0;
        send(level, c, p);
    }

    /** Barrière disparue ; shatter = brisée (éclats). */
    public static void shellGone(ServerLevel level, int id, Vec3 c, boolean shatter) {
        FxPacket p = new FxPacket();
        p.kind = FxPacket.SHELL_GONE;
        p.id = id;
        p.x1 = c.x; p.y1 = c.y; p.z1 = c.z;
        p.extra = shatter ? 1 : 0;
        send(level, c, p);
    }

    /** Bouclier lumineux devant une entité pendant life ticks (Bélier). */
    public static void shield(ServerLevel level, Entity e, int rgb, float radius, int life) {
        FxPacket p = new FxPacket();
        p.kind = FxPacket.SHIELD;
        p.extra = e.getId();
        p.color = rgb;
        p.a = radius;
        p.life = life;
        send(level, e.position(), p);
    }

    /** Onde circulaire au sol qui s'élargit jusqu'à radius. */
    public static void wave(ServerLevel level, Vec3 c, double radius, int rgb, int life) {
        FxPacket p = new FxPacket();
        p.kind = FxPacket.WAVE;
        p.x1 = c.x; p.y1 = c.y; p.z1 = c.z;
        p.a = (float) radius;
        p.color = rgb;
        p.life = life;
        send(level, c, p);
    }

    /** Particules du mod ("arc", "ring", "flare", "hex", "mote"), teintées. */
    public static void sprite(ServerLevel level, String name, int rgb, float size, Vec3 at, int count, double spread, double speed) {
        ParticleType<FxOptions> t = ModParticles.byName(name);
        if (t == null || count <= 0) return;
        Vfx.emit(level, new FxOptions(t, rgb, size, 0), at.x, at.y, at.z, count, spread, spread, spread, speed);
    }
}
