package com.myriam.unrp.ability;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sons des capacités : chaque pose (le geste) joue une signature sonore, faite de couches de sons Minecraft
 * (data/unrp/unodex/pose_sounds.json). Aucun fichier audio n'est ajouté. Déclenché par Poses.play / Poses.hold.
 */
public final class Sounds {
    private Sounds() {}

    private record Layer(SoundEvent ev, float vol, float pitch) {}

    private static Map<String, List<Layer>> byPose;
    private static final Map<UUID, Long> LAST = new HashMap<>();

    private static synchronized Map<String, List<Layer>> table() {
        if (byPose != null) return byPose;
        byPose = new HashMap<>();
        try (var in = Sounds.class.getResourceAsStream("/data/unrp/unodex/pose_sounds.json")) {
            if (in == null) return byPose;
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, List<Layer>> sig = new HashMap<>();
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("signatures").entrySet()) {
                List<Layer> ls = new ArrayList<>();
                for (JsonElement l : e.getValue().getAsJsonArray()) {
                    JsonArray a = l.getAsJsonArray();
                    SoundEvent ev = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(a.get(0).getAsString()));
                    if (ev != null) ls.add(new Layer(ev, a.get(1).getAsFloat(), a.get(2).getAsFloat()));
                }
                sig.put(e.getKey(), ls);
            }
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("poses").entrySet()) {
                List<Layer> ls = sig.get(e.getValue().getAsString());
                if (ls != null) byPose.put(e.getKey(), ls);
            }
        } catch (Exception ex) {
            // pas de sons si le fichier est illisible
        }
        return byPose;
    }

    /** Joue un son du modèle (unrp:<id>, défini par son sounds.json) à la position du joueur. */
    public static void model(ServerPlayer p, String id, float vol) {
        model(p, id, vol, 1.0f);
    }

    /** Son du modèle (« id » dans unrp, ou « espace:id » complet). */
    public static void model(ServerPlayer p, String id, float vol, float pitch) {
        ResourceLocation rl = id.contains(":") ? new ResourceLocation(id) : new ResourceLocation("unrp", id);
        SoundEvent ev = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getOptional(rl).orElseGet(() -> SoundEvent.createVariableRangeEvent(rl));
        p.serverLevel().playSound(null, p.getX(), p.getY(), p.getZ(), ev, SoundSource.PLAYERS, vol, pitch);
    }

    /** Joue la signature de la pose à la position du joueur (au plus une fois toutes les 6 ticks par joueur). */
    public static void cue(ServerPlayer p, String pose) {
        List<Layer> ls = table().get(pose);
        if (ls == null) return;
        long now = p.serverLevel().getGameTime();
        Long last = LAST.get(p.getUUID());
        if (last != null && now - last < 6) return;
        LAST.put(p.getUUID(), now);
        for (Layer l : ls) p.serverLevel().playSound(null, p.getX(), p.getY(), p.getZ(), l.ev, SoundSource.PLAYERS, l.vol, l.pitch);
    }
}
