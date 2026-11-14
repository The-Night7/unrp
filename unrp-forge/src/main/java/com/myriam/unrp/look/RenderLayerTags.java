package com.myriam.unrp.look;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/** Tags lus par les conditions des calques de rendu du modèle (unrp_render_layers.json) : envoyés au client avec l'état des pouvoirs. */
public final class RenderLayerTags {
    private RenderLayerTags() {}

    private static Set<String> tags;

    public static synchronized Set<String> get() {
        if (tags != null) return tags;
        tags = new HashSet<>();
        try (var in = RenderLayerTags.class.getResourceAsStream("/unrp_render_layers.json")) {
            if (in != null) for (JsonElement t : JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("tags")) tags.add(t.getAsString());
        } catch (Exception ignored) {
        }
        return tags;
    }
}
