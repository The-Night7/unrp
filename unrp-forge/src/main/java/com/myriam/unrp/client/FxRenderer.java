package com.myriam.unrp.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.myriam.unrp.UnrpMod;
import com.myriam.unrp.net.FxPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = UnrpMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FxRenderer {

    private record Line(Vec3 a, Vec3 b, int rgb, int flags, float width, long born, int life, long seed) {}

    private static final class Shell {
        Vec3 c;
        float radius, hp = 1f;
        int rgb;
        long seen, flash = -100, born;
    }

    private record Burst(Vec3 c, float radius, int rgb, long born, int life, boolean shards, long seed) {}

    private record Shield(int entity, int rgb, float radius, long born, int life) {}

    private static final List<Line> LINES = new ArrayList<>();
    private static final Map<Integer, Shell> SHELLS = new HashMap<>();
    private static final List<Burst> BURSTS = new ArrayList<>();
    private static final List<Shield> SHIELDS = new ArrayList<>();

    private record Glow(Vec3 c, Vec3 spread, int shape, int count, int rgb, float speed, long born, int life, long seed) {}

    private static final List<Glow> GLOWS = new ArrayList<>();

    private FxRenderer() {}

    private static long now() {
        var l = Minecraft.getInstance().level;
        return l == null ? 0 : l.getGameTime();
    }

    public static void receive(FxPacket p) {
        long t = now();
        switch (p.kind) {
            case FxPacket.LINE -> {
                if (LINES.size() < 600) {
                    LINES.add(new Line(new Vec3(p.x1, p.y1, p.z1), new Vec3(p.x2, p.y2, p.z2), p.color, p.extra, p.a, t, Math.max(1, p.life), p.id));
                }
            }
            case FxPacket.SHELL -> {
                Shell s = SHELLS.computeIfAbsent(p.id, k -> {
                    Shell n = new Shell();
                    n.born = t;
                    return n;
                });
                s.c = new Vec3(p.x1, p.y1, p.z1);
                s.radius = p.a;
                s.hp = (float) p.x2;
                s.rgb = p.color;
                s.seen = t;
                if (p.extra == 1) s.flash = t;
            }
            case FxPacket.SHELL_GONE -> {
                Shell s = SHELLS.remove(p.id);
                if (s != null) BURSTS.add(new Burst(s.c, s.radius, s.rgb, t, p.extra == 1 ? 14 : 8, p.extra == 1, p.id));
            }
            case FxPacket.GLOW -> {
                if (GLOWS.size() < 900) GLOWS.add(new Glow(new Vec3(p.x1, p.y1, p.z1), new Vec3(p.x2, p.y2, p.z2), p.id, p.extra, p.color, p.a, t, Math.max(1, p.life), (long) (p.x1 * 73856093) ^ (long) (p.z1 * 19349663) ^ t));
            }
            case FxPacket.SHIELD -> SHIELDS.add(new Shield(p.extra, p.color, p.a, t, Math.max(1, p.life)));
            case FxPacket.WAVE -> BURSTS.add(new Burst(new Vec3(p.x1, p.y1, p.z1), -p.a, p.color, t, Math.max(1, p.life), false, 0));
            default -> { }
        }
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float pt = e.getPartialTick();
        double t = mc.level.getGameTime() + pt;
        Vec3 cam = e.getCamera().getPosition();
        PoseStack ps = e.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(RenderType.lightning());
        Matrix4f m = ps.last().pose();
        Draw d = new Draw(vc, m, cam);

        // traits
        Iterator<Line> li = LINES.iterator();
        while (li.hasNext()) {
            Line l = li.next();
            double age = t - l.born;
            if (age > l.life || age < -2) {
                li.remove();
                continue;
            }
            float fade = (float) Math.max(0, 1 - age / l.life);
            boolean core = (l.flags & 8) == 0;
            if ((l.flags & 4) != 0) {
                helix(d, l.a, l.b, l.rgb, l.width, fade, t, core);
            } else if ((l.flags & 2) != 0) {
                List<Vec3> pts = wavy(l.a, l.b, t, 0.18, 0);
                for (int i = 0; i + 1 < pts.size(); i++) glowSegment(d, pts.get(i), pts.get(i + 1), l.rgb, l.width, fade, core);
            } else if ((l.flags & 1) != 0) {
                long frame = (long) (t / 2);
                List<Vec3> pts = zigzag(l.a, l.b, l.seed * 31 + frame, l.width);
                for (int i = 0; i + 1 < pts.size(); i++) glowSegment(d, pts.get(i), pts.get(i + 1), l.rgb, l.width, fade);
                if (l.a.distanceTo(l.b) > 3) {
                    RandomSource r = RandomSource.create(l.seed * 7 + frame);
                    int k = 1 + r.nextInt(Math.max(1, pts.size() - 2));
                    Vec3 from = pts.get(Math.min(k, pts.size() - 1));
                    Vec3 to = from.add((r.nextDouble() - 0.5) * 1.6, (r.nextDouble() - 0.5) * 1.6, (r.nextDouble() - 0.5) * 1.6);
                    List<Vec3> br = zigzag(from, to, l.seed + frame * 13, l.width * 0.6f);
                    for (int i = 0; i + 1 < br.size(); i++) glowSegment(d, br.get(i), br.get(i + 1), l.rgb, l.width * 0.6f, fade * 0.7f);
                }
            } else {
                glowSegment(d, l.a, l.b, l.rgb, l.width, fade, core);
            }
        }

        Iterator<Map.Entry<Integer, Shell>> si = SHELLS.entrySet().iterator();
        while (si.hasNext()) {
            var en = si.next();
            Shell s = en.getValue();
            if (now() - s.seen > 60) {
                si.remove();
                continue;
            }
            drawShell(d, s, en.getKey(), t);
        }

        Iterator<Burst> bi = BURSTS.iterator();
        while (bi.hasNext()) {
            Burst b = bi.next();
            double age = t - b.born;
            if (age > b.life) {
                bi.remove();
                continue;
            }
            float k = (float) (age / b.life);
            if (b.radius < 0) { // onde au sol
                ring(d, b.c.add(0, 0.1, 0), -b.radius * (0.2f + 0.8f * k), b.rgb, 0.18f, (1 - k) * 0.9f);
            } else if (b.shards) {
                RandomSource r = RandomSource.create(b.seed);
                for (int i = 0; i < 40; i++) {
                    Vec3 dir = new Vec3(r.nextDouble() - 0.5, r.nextDouble() - 0.5, r.nextDouble() - 0.5).normalize();
                    Vec3 p = b.c.add(dir.scale(b.radius * (1 + k * 0.8)));
                    Vec3 q = p.add(new Vec3(r.nextDouble() - 0.5, r.nextDouble() - 0.5, r.nextDouble() - 0.5).scale(0.5));
                    glowSegment(d, p, q, b.rgb, 0.08f, 1 - k);
                }
            } else {
                sphereGrid(d, b.c, b.radius * (1 + 0.15f * k), b.rgb, (1 - k) * 0.35f, 8, 16);
            }
        }

        Iterator<Shield> hi = SHIELDS.iterator();
        while (hi.hasNext()) {
            Shield s = hi.next();
            double age = t - s.born;
            Entity en = mc.level.getEntity(s.entity);
            if (age > s.life || en == null) {
                hi.remove();
                continue;
            }
            Vec3 look = en.getViewVector(pt);
            Vec3 flat = new Vec3(look.x, 0, look.z);
            flat = flat.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : flat.normalize();
            Vec3 c = en.getPosition(pt).add(0, 1.0, 0).add(flat.scale(1.3));
            Vec3 side = new Vec3(-flat.z, 0, flat.x), up = new Vec3(0, 1, 0);
            float a = (float) Math.min(1, (s.life - age) / 4.0);
            hexDisk(d, c, side, up, s.radius * 0.6f, s.rgb, a);
        }

        Iterator<Glow> gi = GLOWS.iterator();
        while (gi.hasNext()) {
            Glow g = gi.next();
            double age = t - g.born;
            if (age > g.life || age < -2) {
                gi.remove();
                continue;
            }
            float k = (float) (age / g.life), fade = 1 - k;
            if (g.shape == 1) {
                float r = (float) (0.4 + Math.max(g.spread.x, 0.3) * 1.5) * (0.3f + 0.7f * k);
                ring(d, g.c, r, g.rgb, 0.08f, fade * 0.9f);
            } else if (g.shape == 2) {
                float r = (float) (0.6 + Math.max(g.spread.x, g.spread.y) * 1.2) * (0.4f + 0.9f * k);
                sphereGrid(d, g.c, r, g.rgb, fade * 0.45f, 6, 12);
                ring(d, g.c, r * 1.1f, g.rgb, 0.1f, fade * 0.8f);
            } else if (g.count < 0) {
                Vec3 v = g.spread.scale(Math.max(0.15, g.speed)).add(0, 0, 0);
                Vec3 a = g.c.add(v.scale(age)), b = a.add(v.scale(1.6));
                glowSegment(d, a, b, g.rgb, 0.06f, fade);
            } else {
                RandomSource r = RandomSource.create(g.seed);
                for (int i = 0; i < Math.max(1, g.count); i++) {
                    Vec3 o = new Vec3((r.nextDouble() * 2 - 1) * g.spread.x, (r.nextDouble() * 2 - 1) * g.spread.y, (r.nextDouble() * 2 - 1) * g.spread.z);
                    Vec3 dir = new Vec3(r.nextDouble() - 0.5, r.nextDouble() - 0.2, r.nextDouble() - 0.5).normalize();
                    double drift = Math.min(1.2, 0.25 + g.speed * 6) * age / g.life;
                    Vec3 a = g.c.add(o).add(dir.scale(drift)), b = a.add(dir.scale(0.12 + 0.2 * fade));
                    glowSegment(d, a, b, g.rgb, 0.05f, fade);
                }
            }
        }

        buffers.endBatch(RenderType.lightning());
    }

    // ------------------------------------------------------------------ formes
    private static void drawShell(Draw d, Shell s, int id, double t) {
        double grow = Math.min(1, (t - s.born) / 6.0);
        float r = (float) (s.radius * (0.3 + 0.7 * grow));
        double since = t - s.flash;
        float flash = since >= 0 && since < 6 ? (float) (1 - since / 6) : 0f;
        float pulse = 0.85f + 0.15f * (float) Math.sin(t * 0.15);
        int col = s.hp < 0.34f ? mix(s.rgb, 0xFF5050, 0.5f) : s.rgb;

        sphereFill(d, s.c, r, col, 0.05f + 0.15f * flash, 10, 20);

        sphereGrid(d, s.c, r, col, (0.28f + 0.5f * flash) * pulse, 10, 20, t * 0.004);
        // fissures
        if (s.hp < 0.67f) cracks(d, s.c, r, id, s.hp < 0.34f ? 9 : 4, mix(col, 0xFFFFFF, 0.6f));
    }

    private static void sphereGrid(Draw d, Vec3 c, float r, int rgb, float a, int stacks, int slices) {
        sphereGrid(d, c, r, rgb, a, stacks, slices, 0);
    }

    private static void sphereGrid(Draw d, Vec3 c, float r, int rgb, float a, int stacks, int slices, double rot) {
        float w = 0.035f;
        for (int i = 1; i < stacks; i++) {
            double phi = Math.PI * i / stacks;
            for (int j = 0; j < slices; j++) {
                glowSegmentFlat(d, sph(c, r, phi, 2 * Math.PI * j / slices + rot), sph(c, r, phi, 2 * Math.PI * (j + 1) / slices + rot), rgb, w, a);
            }
        }
        for (int j = 0; j < slices; j++) {
            double th = 2 * Math.PI * j / slices + rot;
            for (int i = 0; i < stacks; i++) {
                glowSegmentFlat(d, sph(c, r, Math.PI * i / stacks, th), sph(c, r, Math.PI * (i + 1) / stacks, th), rgb, w, a);
            }
        }
    }

    private static void sphereFill(Draw d, Vec3 c, float r, int rgb, float a, int stacks, int slices) {
        for (int i = 0; i < stacks; i++) {
            double p0 = Math.PI * i / stacks, p1 = Math.PI * (i + 1) / stacks;
            for (int j = 0; j < slices; j++) {
                double t0 = 2 * Math.PI * j / slices, t1 = 2 * Math.PI * (j + 1) / slices;
                d.quad(sph(c, r, p0, t0), sph(c, r, p0, t1), sph(c, r, p1, t1), sph(c, r, p1, t0), rgb, a);
            }
        }
    }

    private static void cracks(Draw d, Vec3 c, float r, int id, int n, int rgb) {
        RandomSource rs = RandomSource.create(id * 977L);
        for (int k = 0; k < n; k++) {
            double phi = Math.acos(2 * rs.nextDouble() - 1), th = rs.nextDouble() * 2 * Math.PI;
            Vec3 p = sph(c, r * 1.01f, phi, th);
            for (int s = 0; s < 5; s++) {
                phi += (rs.nextDouble() - 0.5) * 0.35;
                th += (rs.nextDouble() - 0.5) * 0.35;
                Vec3 q = sph(c, r * 1.01f, phi, th);
                glowSegment(d, p, q, rgb, 0.05f, 0.9f);
                p = q;
            }
        }
    }

    private static void ring(Draw d, Vec3 c, float r, int rgb, float w, float a) {
        int n = Math.max(16, (int) (r * 10));
        for (int i = 0; i < n; i++) {
            double a0 = 2 * Math.PI * i / n, a1 = 2 * Math.PI * (i + 1) / n;
            glowSegment(d, c.add(Math.cos(a0) * r, 0, Math.sin(a0) * r), c.add(Math.cos(a1) * r, 0, Math.sin(a1) * r), rgb, w, a);
        }
    }

    private static void hexDisk(Draw d, Vec3 c, Vec3 side, Vec3 up, float r, int rgb, float a) {
        Vec3[] pts = new Vec3[6];
        for (int i = 0; i < 6; i++) {
            double ang = Math.PI / 3 * i + Math.PI / 6;
            pts[i] = c.add(side.scale(Math.cos(ang) * r * 1.6)).add(up.scale(Math.sin(ang) * r * 1.6));
        }
        for (int i = 0; i < 6; i++) {
            d.quad(c, pts[i], pts[(i + 1) % 6], c, rgb, 0.18f * a);
            glowSegment(d, pts[i], pts[(i + 1) % 6], rgb, 0.08f, a);
        }
    }

    private static Vec3 sph(Vec3 c, float r, double phi, double th) {
        return c.add(r * Math.sin(phi) * Math.cos(th), r * Math.cos(phi), r * Math.sin(phi) * Math.sin(th));
    }

    private static List<Vec3> zigzag(Vec3 a, Vec3 b, long seed, float width) {
        RandomSource r = RandomSource.create(seed);
        double len = a.distanceTo(b);
        int n = Math.max(2, (int) (len / 0.6));
        double amp = Math.min(0.45, 0.12 + len * 0.02) + width;
        List<Vec3> pts = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            Vec3 p = a.add(b.subtract(a).scale(i / (double) n));
            if (i > 0 && i < n) p = p.add((r.nextDouble() - 0.5) * amp, (r.nextDouble() - 0.5) * amp, (r.nextDouble() - 0.5) * amp);
            pts.add(p);
        }
        return pts;
    }

    private static void glowSegment(Draw d, Vec3 a, Vec3 b, int rgb, float width, float alpha) {
        glowSegment(d, a, b, rgb, width, alpha, true);
    }

    private static void glowSegment(Draw d, Vec3 a, Vec3 b, int rgb, float width, float alpha, boolean core) {
        d.beam(a, b, width * 2.6f, rgb, 0.12f * alpha);
        d.beam(a, b, width, rgb, (core ? 0.45f : 0.6f) * alpha);
        d.beam(a, b, width * 0.35f, core ? mix(rgb, 0xFFFFFF, 0.75f) : mix(rgb, 0x000000, 0.2f), (core ? 0.9f : 0.5f) * alpha);
    }

    private static List<Vec3> wavy(Vec3 a, Vec3 b, double t, double amp, double phase) {
        Vec3 dir = b.subtract(a);
        double len = dir.length();
        Vec3 n = dir.cross(new Vec3(0, 1, 0));
        if (n.lengthSqr() < 1.0E-6) n = dir.cross(new Vec3(1, 0, 0));
        n = n.normalize();
        Vec3 m = dir.cross(n).normalize();
        int steps = Math.max(4, (int) (len / 0.25));
        List<Vec3> pts = new ArrayList<>(steps + 1);
        for (int i = 0; i <= steps; i++) {
            double u = i / (double) steps;
            double env = Math.sin(Math.PI * u);
            double w = u * len * 3.0 - t * 0.6 + phase;
            pts.add(a.add(dir.scale(u)).add(n.scale(Math.sin(w) * amp * env)).add(m.scale(Math.cos(w) * amp * 0.4 * env)));
        }
        return pts;
    }

    private static void helix(Draw d, Vec3 a, Vec3 b, int rgb, float width, float alpha, double t, boolean core) {
        Vec3 dir = b.subtract(a);
        double len = dir.length();
        if (len < 0.05) return;
        Vec3 n = dir.cross(new Vec3(0, 1, 0));
        if (n.lengthSqr() < 1.0E-6) n = dir.cross(new Vec3(1, 0, 0));
        n = n.normalize();
        Vec3 m = dir.cross(n).normalize();
        int steps = Math.max(6, (int) (len / 0.2));
        double r = 0.22;
        for (int s = 0; s < 2; s++) {
            Vec3 prev = null;
            for (int i = 0; i <= steps; i++) {
                double u = i / (double) steps;
                double ang = u * len * 2.2 + t * 0.4 + s * Math.PI;
                Vec3 p = a.add(dir.scale(u)).add(n.scale(Math.cos(ang) * r)).add(m.scale(Math.sin(ang) * r));
                if (prev != null) glowSegment(d, prev, p, rgb, width * 0.6f, alpha, core);
                prev = p;
            }
        }
        glowSegment(d, a, b, rgb, width * 0.3f, alpha * 0.6f, core);
    }

    private static void glowSegmentFlat(Draw d, Vec3 a, Vec3 b, int rgb, float width, float alpha) {
        d.beam(a, b, width * 2.2f, rgb, 0.25f * alpha);
        d.beam(a, b, width * 0.6f, mix(rgb, 0xFFFFFF, 0.5f), 0.7f * alpha);
    }

    static int mix(int c1, int c2, float t) {
        int r = Math.round(((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = Math.round(((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = Math.round((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (r << 16) | (g << 8) | b;
    }

    private record Draw(VertexConsumer vc, Matrix4f m, Vec3 cam) {
        void v(Vec3 p, float r, float g, float b, float a) {
            vc.vertex(m, (float) (p.x - cam.x), (float) (p.y - cam.y), (float) (p.z - cam.z)).color(r, g, b, a).endVertex();
        }

        void quad(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, int rgb, float a) {
            if (a <= 0.003f) return;
            float r = ((rgb >> 16) & 255) / 255f, g = ((rgb >> 8) & 255) / 255f, b = (rgb & 255) / 255f;
            v(p0, r, g, b, a); v(p1, r, g, b, a); v(p2, r, g, b, a); v(p3, r, g, b, a);
            v(p3, r, g, b, a); v(p2, r, g, b, a); v(p1, r, g, b, a); v(p0, r, g, b, a);
        }

        void beam(Vec3 a, Vec3 b, float w, int rgb, float alpha) {
            Vec3 dir = b.subtract(a);
            Vec3 toCam = cam.subtract(a.add(b).scale(0.5));
            Vec3 side = dir.cross(toCam);
            if (side.lengthSqr() < 1.0E-8) return;
            side = side.normalize().scale(w / 2);
            quad(a.subtract(side), a.add(side), b.add(side), b.subtract(side), rgb, alpha);
        }
    }
}
