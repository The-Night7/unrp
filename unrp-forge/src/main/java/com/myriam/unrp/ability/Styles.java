package com.myriam.unrp.ability;

import com.myriam.unrp.data.AbilityPower;
import com.myriam.unrp.particle.FxOptions;
import com.myriam.unrp.particle.ModParticles;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Styles visuels des pouvoirs (champ "look.style" dans ability_powers.json). Un style fixe :
 *  - sprite  : la particule principale (traînées, éclats) ;
 *  - sprite2 : la particule des impacts ;
 *  - line    : la forme des traits lumineux (LINE_* combinables) ;
 *  - width   : leur épaisseur ;
 *  - aura    : comment l'aura se montre autour du joueur quand le pouvoir est actif.
 * Liste et rendu : docs/POUVOIRS.md §12.
 */
public final class Styles {
    public static final int LINE_JAGGED = 1, LINE_WAVY = 2, LINE_HELIX = 4, LINE_NOCORE = 8;

    public record Preset(String sprite, String sprite2, int line, float width, String aura) {}

    public static final Map<String, Preset> ALL = new HashMap<>();

    static {
        ALL.put("lightning", new Preset("arc", "flare", LINE_JAGGED, 0.11f, "arcs"));
        ALL.put("fire", new Preset("ember", "smoke", LINE_WAVY, 0.16f, "rise"));
        ALL.put("ice", new Preset("shard", "glint", 0, 0.12f, "orbit"));
        ALL.put("water", new Preset("drop", "ring", LINE_WAVY, 0.14f, "drip"));
        ALL.put("wind", new Preset("streak", "mote", LINE_HELIX, 0.08f, "swirl"));
        ALL.put("shadow", new Preset("smoke", "wisp", LINE_WAVY | LINE_NOCORE, 0.2f, "rise"));
        ALL.put("light", new Preset("flare", "glint", 0, 0.2f, "orbit"));
        ALL.put("blood", new Preset("drop", "smoke", LINE_WAVY | LINE_NOCORE, 0.14f, "drip"));
        ALL.put("nature", new Preset("leaf", "mote", LINE_WAVY, 0.1f, "fall"));
        ALL.put("mind", new Preset("glyph", "ring", LINE_HELIX, 0.08f, "orbit"));
        ALL.put("gravity", new Preset("ring", "wisp", LINE_HELIX, 0.12f, "pull"));
        ALL.put("barrier", new Preset("hex", "glint", 0, 0.12f, "orbit"));
        ALL.put("cloak", new Preset("mote", "glint", 0, 0.08f, "shimmer"));
        ALL.put("speed", new Preset("streak", "mote", 0, 0.08f, "behind"));
        ALL.put("strength", new Preset("glint", "ring", 0, 0.18f, "rise"));
        ALL.put("heal", new Preset("cross", "mote", LINE_WAVY, 0.1f, "rise"));
        ALL.put("poison", new Preset("drop", "smoke", LINE_WAVY, 0.12f, "rise"));
        ALL.put("earth", new Preset("rock", "smoke", 0, 0.16f, "orbit"));
        ALL.put("metal", new Preset("glint", "shard", 0, 0.12f, "orbit"));
        ALL.put("sound", new Preset("ring", "ring", LINE_WAVY, 0.1f, "pulse"));
        ALL.put("space", new Preset("wisp", "glint", LINE_HELIX, 0.12f, "orbit"));
        ALL.put("energy", new Preset("flare", "mote", 0, 0.14f, "motes"));
    }

    private Styles() {}

    /** Le style d'un pouvoir : look.style, sinon déduit (zigzag -> lightning), sinon energy. */
    public static Preset of(AbilityPower.Look look) {
        if (look != null && look.style != null && ALL.containsKey(look.style)) return ALL.get(look.style);
        if (look != null && look.jagged) return ALL.get("lightning");
        return ALL.get("energy");
    }

    /** Particule principale (look.sprite si renseigné, sinon celle du style). */
    public static String sprite(AbilityPower.Look look) {
        return look != null && look.sprite != null ? look.sprite : of(look).sprite();
    }

    /** Une particule avec une vitesse précise (count 0 : la vitesse est exactement (vx, vy, vz)). */
    static void moving(ServerLevel level, String name, int rgb, float size, int life, Vec3 at, double vx, double vy, double vz) {
        ParticleType<FxOptions> t = ModParticles.byName(name);
        if (t == null) return;
        Vfx.emit(level, new FxOptions(t, rgb, size, life), at.x, at.y, at.z, 0, vx, vy, vz, 1.0);
    }

    /** L'aura visible autour d'un joueur dont le pouvoir est actif (appelé toutes les 4 ticks). */
    static void aura(ServerPlayer p, AbilityPower.Look look, int rgb, boolean invisible, long time) {
        ServerLevel lv = p.serverLevel();
        Preset s = of(look);
        String spr = sprite(look);
        var r = p.getRandom();
        Vec3 base = p.position();
        if (invisible) { // seulement un léger miroitement
            moving(lv, "mote", rgb, 0.45f, 10, base.add((r.nextDouble() - 0.5) * 0.7, 0.3 + r.nextDouble() * 1.4, (r.nextDouble() - 0.5) * 0.7), 0, 0.005, 0);
            return;
        }
        moving(lv, "mote", rgb, 0.7f, 14, base.add((r.nextDouble() - 0.5) * 0.8, 0.1 + r.nextDouble() * 1.7, (r.nextDouble() - 0.5) * 0.8), 0, 0.02, 0);
        switch (s.aura()) {
            case "arcs" -> {
                for (int k = 0; k < 2; k++) {
                    Vec3 a = base.add((r.nextDouble() - 0.5) * 0.9, r.nextDouble() * 1.9, (r.nextDouble() - 0.5) * 0.9);
                    Vec3 b = a.add((r.nextDouble() - 0.5) * 0.8, (r.nextDouble() - 0.5) * 0.8, (r.nextDouble() - 0.5) * 0.8);
                    Fx.line(lv, a, b, rgb, LINE_JAGGED, 0.04f, 3);
                }
            }
            case "rise" -> {
                for (int k = 0; k < 3; k++) {
                    Vec3 at = base.add((r.nextDouble() - 0.5) * 0.9, r.nextDouble() * 0.6, (r.nextDouble() - 0.5) * 0.9);
                    moving(lv, spr, rgb, 0.9f, 0, at, (r.nextDouble() - 0.5) * 0.01, 0.06 + r.nextDouble() * 0.04, (r.nextDouble() - 0.5) * 0.01);
                }
            }
            case "orbit" -> {
                for (int k = 0; k < 3; k++) {
                    double ang = time * 0.12 + k * Math.PI * 2 / 3;
                    Vec3 at = base.add(Math.cos(ang) * 0.95, 0.4 + k * 0.55, Math.sin(ang) * 0.95);
                    moving(lv, spr, rgb, 0.8f, 6, at, 0, 0, 0);
                }
            }
            case "drip" -> {
                for (int k = 0; k < 2; k++) {
                    Vec3 at = base.add((r.nextDouble() - 0.5) * 0.7, 1.2 + r.nextDouble() * 0.6, (r.nextDouble() - 0.5) * 0.7);
                    moving(lv, spr, rgb, 0.6f, 0, at, 0, -0.02, 0);
                }
            }
            case "swirl" -> {
                for (int k = 0; k < 3; k++) {
                    double ang = time * 0.3 + k * Math.PI * 2 / 3;
                    Vec3 at = base.add(Math.cos(ang) * 0.8, 0.2 + ((time + k * 7) % 20) / 10.0, Math.sin(ang) * 0.8);
                    moving(lv, spr, rgb, 0.9f, 8, at, -Math.sin(ang) * 0.12, 0.02, Math.cos(ang) * 0.12);
                }
            }
            case "fall" -> {
                Vec3 at = base.add((r.nextDouble() - 0.5) * 1.6, 2.0 + r.nextDouble() * 0.4, (r.nextDouble() - 0.5) * 1.6);
                moving(lv, spr, rgb, 0.8f, 0, at, (r.nextDouble() - 0.5) * 0.02, -0.02, (r.nextDouble() - 0.5) * 0.02);
            }
            case "pull" -> {
                for (int k = 0; k < 3; k++) {
                    double ang = r.nextDouble() * Math.PI * 2;
                    Vec3 at = base.add(Math.cos(ang) * 1.6, 0.3 + r.nextDouble() * 1.4, Math.sin(ang) * 1.6);
                    Vec3 v = base.add(0, 1, 0).subtract(at).scale(0.08);
                    moving(lv, "mote", rgb, 0.7f, 12, at, v.x, v.y, v.z);
                }
                if (time % 16 == 0) moving(lv, spr, rgb, 3.0f, 12, base.add(0, 1, 0), 0, 0, 0);
            }
            case "behind" -> {
                Vec3 view = p.getViewVector(1f);
                for (int k = 0; k < 2; k++) {
                    Vec3 at = base.add(0, 0.4 + r.nextDouble() * 1.2, 0).subtract(view.scale(0.4)).add((r.nextDouble() - 0.5) * 0.6, 0, (r.nextDouble() - 0.5) * 0.6);
                    moving(lv, spr, rgb, 0.9f, 6, at, -view.x * 0.15, 0, -view.z * 0.15);
                }
            }
            case "pulse" -> {
                if (time % 12 == 0) moving(lv, spr, rgb, 4.0f, 12, base.add(0, 1, 0), 0, 0, 0);
            }
            case "shimmer" -> moving(lv, spr, rgb, 0.6f, 10, base.add((r.nextDouble() - 0.5) * 0.8, r.nextDouble() * 1.8, (r.nextDouble() - 0.5) * 0.8), 0, 0.01, 0);
            default -> { // motes
                moving(lv, spr, rgb, 0.8f, 14, base.add((r.nextDouble() - 0.5) * 0.8, r.nextDouble() * 1.8, (r.nextDouble() - 0.5) * 0.8), 0, 0.02, 0);
            }
        }
    }

    /** Éclats d'impact : particules d'impact projetées vers l'extérieur + un anneau. */
    static void impact(ServerLevel lv, AbilityPower.Look look, int rgb, Vec3 at, int count) {
        Preset s = of(look);
        var r = lv.random;
        for (int k = 0; k < count; k++) {
            Vec3 v = new Vec3(r.nextDouble() - 0.5, r.nextDouble() - 0.3, r.nextDouble() - 0.5).normalize().scale(0.12 + r.nextDouble() * 0.1);
            moving(lv, k % 2 == 0 ? sprite(look) : s.sprite2(), rgb, 1.0f, 0, at, v.x, v.y, v.z);
        }
        moving(lv, "ring", rgb, 2.2f, 10, at, 0, 0, 0);
    }
}
