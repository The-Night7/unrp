package com.myriam.unrp.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.myriam.unrp.UnrpMod;
import com.myriam.unrp.net.PosePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PoseClient {

    static final String[] PARTS = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};

    private record Key(double t, float[][] rot) {}

    private record Clip(int len, String ease, int fadeIn, int fadeOut, List<Key> keys) {}

    private static final class State {
        Clip clip;
        double start;
        double holdUntil;
        double stopAt = -1;
    }

    private static Map<String, Clip> clips;
    private static final Map<Integer, State> STATES = new HashMap<>();

    private PoseClient() {}

    private static double now(float partial) {
        var l = Minecraft.getInstance().level;
        return l == null ? 0 : l.getGameTime() + partial;
    }

    public static void invalidate() {
        clips = null;
    }

    private static Map<String, Clip> clips() {
        if (clips != null) return clips;
        Map<String, Clip> m = new HashMap<>();
        try {
            var res = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(UnrpMod.MOD_ID, "poses.json"));
            if (res.isPresent()) {
                try (Reader r = new InputStreamReader(res.get().open(), StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
                    for (var e : root.entrySet()) {
                        if (e.getKey().startsWith("_") || !e.getValue().isJsonObject()) continue;
                        m.put(e.getKey(), parse(e.getValue().getAsJsonObject()));
                    }
                }
            }
        } catch (Exception ex) {
            com.mojang.logging.LogUtils.getLogger().warn("[unrp] poses.json illisible : {}", ex.toString());
        }
        clips = m;
        return m;
    }

    private static Clip parse(JsonObject o) {
        int len = o.has("len") ? o.get("len").getAsInt() : 12;
        String ease = o.has("ease") ? o.get("ease").getAsString() : "in_out_sine";
        int fin = o.has("fade_in") ? o.get("fade_in").getAsInt() : 3;
        int fout = o.has("fade_out") ? o.get("fade_out").getAsInt() : 4;
        List<Key> keys = new ArrayList<>();
        for (JsonElement ke : o.getAsJsonArray("keys")) {
            JsonObject k = ke.getAsJsonObject();
            float[][] rot = new float[PARTS.length][];
            for (int i = 0; i < PARTS.length; i++) {
                if (!k.has(PARTS[i])) continue;
                JsonArray a = k.getAsJsonArray(PARTS[i]);
                rot[i] = new float[]{a.get(0).getAsFloat(), a.size() > 1 ? a.get(1).getAsFloat() : 0, a.size() > 2 ? a.get(2).getAsFloat() : 0};
            }
            keys.add(new Key(k.get("t").getAsDouble(), rot));
        }
        keys.sort((a, b) -> Double.compare(a.t, b.t));
        return new Clip(len, ease, fin, fout, keys);
    }

    public static void receive(PosePacket p) {
        double t = now(0);
        if (p.clip.isEmpty()) {
            State s = STATES.get(p.entity);
            if (s != null && s.stopAt < 0) s.stopAt = t;
            return;
        }
        Clip c = clips().get(p.clip);
        if (c == null) return;
        State s = STATES.get(p.entity);
        if (s != null && s.clip == c && p.hold > 0 && s.stopAt < 0) {
            s.holdUntil = t + p.hold;
            return;
        }
        s = new State();
        s.clip = c;
        s.start = t;
        s.holdUntil = p.hold > 0 ? t + p.hold : -1;
        STATES.put(p.entity, s);
    }

    private static double ease(String e, double x) {
        x = Math.max(0, Math.min(1, x));
        return switch (e) {
            case "linear" -> x;
            case "out_cubic" -> 1 - Math.pow(1 - x, 3);
            case "in_cubic" -> x * x * x;
            case "out_back" -> {
                double c1 = 1.70158, c3 = c1 + 1;
                yield 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
            }
            default -> -(Math.cos(Math.PI * x) - 1) / 2; // in_out_sine
        };
    }

    public static void apply(PlayerModel<?> model, LivingEntity e, float ageInTicks) {
        State s = STATES.get(e.getId());
        if (s == null) return;
        Clip c = s.clip;
        double t = now(Minecraft.getInstance().getFrameTime());
        double age = t - s.start;
        boolean held = s.holdUntil > 0;
        double end = s.stopAt >= 0 ? s.stopAt : held ? Math.max(s.holdUntil, s.start + c.len) : s.start + c.len;
        if (t > end + c.fadeOut) {
            STATES.remove(e.getId());
            return;
        }
        double w = Math.min(1, age / Math.max(1, c.fadeIn));
        if (t > end) w = Math.min(w, 1 - (t - end) / Math.max(1, c.fadeOut));
        w = Math.max(0, w);
        double ct = held ? Math.min(age, c.len) : Math.min(age, c.len);
        ModelPart[] parts = {model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg};
        for (int i = 0; i < PARTS.length; i++) {
            float[] r = sample(c, i, ct);
            if (r == null) continue;
            ModelPart mp = parts[i];
            float rx = (float) Math.toRadians(r[0]), ry = (float) Math.toRadians(r[1]), rz = (float) Math.toRadians(r[2]);
            mp.xRot = (float) (mp.xRot + (rx - mp.xRot) * w);
            mp.yRot = (float) (mp.yRot + (ry - mp.yRot) * w);
            mp.zRot = (float) (mp.zRot + (rz - mp.zRot) * w);
        }
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
    }

    private static float[] sample(Clip c, int part, double ct) {
        Key prev = null, next = null;
        for (Key k : c.keys) {
            if (k.rot[part] == null) continue;
            if (k.t <= ct) prev = k;
            else {
                next = k;
                break;
            }
        }
        if (prev == null && next == null) return null;
        if (prev == null) return next.rot[part];
        if (next == null) return prev.rot[part];
        double x = ease(c.ease, (ct - prev.t) / Math.max(0.001, next.t - prev.t));
        float[] a = prev.rot[part], b = next.rot[part];
        return new float[]{(float) (a[0] + (b[0] - a[0]) * x), (float) (a[1] + (b[1] - a[1]) * x), (float) (a[2] + (b[2] - a[2]) * x)};
    }
}
