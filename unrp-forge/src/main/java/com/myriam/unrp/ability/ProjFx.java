package com.myriam.unrp.ability;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Dessin des projectiles en vol (lumière dessinée, aucune particule), un style par sorte :
 *  missile = orbe + sillage ondulé ; poing/paume = orbe + cinq doigts ; chute = main géante (colonne + orbe) ;
 *  croissant = arc de lumière ; aiguille = trait fin.
 */
final class ProjFx {
    private ProjFx() {}

    private static int next = 1 << 28;

    static synchronized int newId() {
        return next++;
    }

    static void body(ServerLevel lv, Projectiles.P p, Vec3 prev, Vec3 to) {
        Vec3 d = to.subtract(prev);
        Vec3 dir = d.lengthSqr() < 1e-8 ? (p.vel.lengthSqr() < 1e-8 ? new Vec3(0, 0, 1) : p.vel.normalize()) : d.normalize();
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
        Vec3 up = dir.cross(side).normalize();
        float s = Math.max(0.4f, p.size);
        switch (p.kind) {
            case MISSILE -> {
                Fx.shell(lv, p.fxId, to, 0.16 + 0.1 * s, p.rgb, 1f, false);
                Fx.line(lv, to.subtract(dir.scale(2.4)), to, p.rgb, Styles.LINE_WAVY, 0.05f + 0.03f * s, 6);
            }
            case HAND -> {
                Fx.shell(lv, p.fxId, to, 0.2 + 0.12 * s, p.rgb, 1f, false);
                for (int i = -2; i <= 2; i++) { // cinq doigts en éventail devant la paume
                    Vec3 f = dir.add(side.scale(0.22 * i)).add(up.scale(0.05 * Math.abs(i))).normalize();
                    Fx.line(lv, to, to.add(f.scale(0.6 + 0.3 * s)), p.rgb, Styles.LINE_NOCORE, 0.05f, 3);
                }
                Fx.line(lv, to.subtract(dir.scale(1.6)), to, p.rgb, Styles.LINE_NOCORE, 0.08f, 4);
            }
            case SLAM -> {
                Fx.shell(lv, p.fxId, to, 0.35 + 0.2 * s, p.rgb, 1f, false);
                Fx.line(lv, to.add(0, 3.5, 0), to, p.rgb, Styles.LINE_WAVY, 0.14f + 0.04f * s, 4); // le bras de la main qui tombe
                for (int i = -2; i <= 2; i++) Fx.line(lv, to, to.add(side.scale(0.18 * i * s)).add(0, -0.45, 0), p.rgb, Styles.LINE_NOCORE, 0.05f, 3);
            }
            case CRESCENT -> {
                double span = 0.8 + 0.9 * s;
                Vec3 prevPt = null;
                for (int i = 0; i <= 8; i++) { // arc dont la pointe regarde vers l'avant
                    double a = -Math.PI / 2 + Math.PI * i / 8.0;
                    Vec3 pt = to.add(side.scale(Math.sin(a) * span)).add(dir.scale(-Math.cos(a) * span * 0.45 + span * 0.45));
                    if (prevPt != null) Fx.line(lv, prevPt, pt, p.rgb, 0, 0.09f, 3);
                    prevPt = pt;
                }
            }
            case NEEDLE -> Fx.line(lv, to.subtract(dir.scale(1.2)), to, p.rgb, Styles.LINE_NOCORE, 0.035f, 3);
        }
    }

    static void finish(ServerLevel lv, Projectiles.P p) {
        switch (p.kind) {
            case MISSILE, HAND, SLAM -> Fx.shellGone(lv, p.fxId, p.pos, false);
            default -> { }
        }
        switch (p.kind) {
            case MISSILE -> {
                Vfx.glow(lv, Vfx.FLASH, p.rgb, p.pos, new Vec3(0.5 + p.blast * 0.5, 0.5 + p.blast * 0.5, 0), 1, 0);
                Fx.wave(lv, p.pos, 1.0 + p.blast, p.rgb, 8);
            }
            case HAND -> Vfx.glow(lv, Vfx.RING, p.rgb, p.pos, new Vec3(0.5, 0, 0), 1, 0);
            case SLAM -> Vfx.glow(lv, Vfx.SPARKS, p.rgb, p.pos, new Vec3(1.5, 0.3, 1.5), 14, 0.2);
            case CRESCENT -> Vfx.glow(lv, Vfx.SPARKS, p.rgb, p.pos, new Vec3(0.5, 0.5, 0.5), 8, 0.1);
            case NEEDLE -> Vfx.glow(lv, Vfx.SPARKS, p.rgb, p.pos, new Vec3(0.15, 0.15, 0.15), 4, 0.05);
        }
    }
}
