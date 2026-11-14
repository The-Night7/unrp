package com.myriam.unrp.look;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Les capacités « palladium:render_layer » des pouvoirs du modèle (modèles 3D GeckoLib de assets/unrp/geo,
 * peaux et calques de assets/unrp/palladium/render_layers) : la table unrp_render_layers.json est tirée de
 * data/unrp/palladium/powers, et Palladium dessine lui-même les calques retenus, comme dans le modèle.
 */
public final class PowerRenderLayers {
    private PowerRenderLayers() {}

    record Entry(String layer, JsonObject when) {}

    private static Map<String, List<Entry>> table;
    private static Set<String> tags;

    static synchronized void load() {
        if (table != null) return;
        table = new HashMap<>();
        tags = new HashSet<>();
        try (var in = PowerRenderLayers.class.getResourceAsStream("/unrp_render_layers.json")) {
            if (in == null) return;
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("powers").entrySet()) {
                List<Entry> ls = new ArrayList<>();
                for (JsonElement l : e.getValue().getAsJsonArray()) {
                    JsonObject o = l.getAsJsonObject();
                    ls.add(new Entry(o.get("layer").getAsString(), o.getAsJsonObject("when")));
                }
                table.put(e.getKey(), ls);
            }
            for (JsonElement t : root.getAsJsonArray("tags")) tags.add(t.getAsString());
        } catch (Exception ignored) {
        }
    }

    static boolean test(JsonObject c, Set<String> keys, Set<String> common) {
        if (c.has("true")) return true;
        if (c.has("false")) return false;
        if (c.has("key")) return keys.contains(c.get("key").getAsString());
        if (c.has("tag")) return common.contains(c.get("tag").getAsString());
        if (c.has("not")) return !test(c.getAsJsonObject("not"), keys, common);
        if (c.has("any")) {
            for (JsonElement x : c.getAsJsonArray("any")) if (test(x.getAsJsonObject(), keys, common)) return true;
            return false;
        }
        if (c.has("all")) {
            JsonArray a = c.getAsJsonArray("all");
            for (JsonElement x : a) if (!test(x.getAsJsonObject(), keys, common)) return false;
            return true;
        }
        return false;
    }

    /** Branché sur PackRenderLayerManager.registerProvider (côté client). */
    public static void register() {
        load();
        net.threetag.palladium.client.renderer.renderlayer.PackRenderLayerManager.registerProvider((entity, out) -> addLayers(entity, out));
    }

    static void addLayers(Entity entity, java.util.function.BiConsumer<net.threetag.palladium.util.context.DataContext, net.threetag.palladium.client.renderer.renderlayer.IPackRenderLayer> out) {
        if (!(entity instanceof Player p)) return;
        Set<String> common = com.myriam.unrp.pass.PassBridge.common(p.getUUID());
        for (String path : com.myriam.unrp.pass.PassBridge.paths(p.getUUID())) {
            List<Entry> ls = table.get(path);
            if (ls == null) continue;
            Set<String> keys = com.myriam.unrp.pass.PassBridge.keysOf(p.getUUID(), path);
            if (keys == null) continue;
            for (Entry e : ls) {
                if (!test(e.when, keys, common)) continue;
                var layer = net.threetag.palladium.client.renderer.renderlayer.PackRenderLayerManager.getInstance().getLayer(new ResourceLocation(e.layer));
                if (layer != null) out.accept(net.threetag.palladium.util.context.DataContext.forEntity(p), layer);
            }
        }
    }
}
