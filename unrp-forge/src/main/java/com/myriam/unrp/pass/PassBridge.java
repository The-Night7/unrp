package com.myriam.unrp.pass;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.myriam.unrp.data.StyleData;
import com.myriam.unrp.style.StyleService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ce que le moteur d'effets « Pass » du modèle lisait ailleurs (Palladium, ses caches client, d'autres pouvoirs),
 * servi par le mod : l'état des pouvoirs vient de {@link PassStatePacket}, la couleur et les yeux de l'onglet Style.
 */
public final class PassBridge {
    private PassBridge() {}

    /** Pouvoir porté par un joueur, vu du client (remplace IPowerHolder de Palladium). */
    public static final class Holder {
        public final String path;
        final Set<String> on;

        Holder(String path, Set<String> on) {
            this.path = path;
            this.on = on;
        }

        public boolean enabled(String key) {
            return on.contains(key);
        }
    }

    /** Pouvoirs détenus par un joueur (chemin -> clés), clés communes, et le reste de PassStatePacket. */
    record State(java.util.LinkedHashMap<String, Set<String>> powers, Set<String> common, boolean am, int locked, boolean transformed) {
        /** Toutes les clés (tous les pouvoirs + communes). */
        Set<String> on() {
            Set<String> all = new java.util.HashSet<>(common);
            for (Set<String> k : powers.values()) all.addAll(k);
            return all;
        }

        String path() {
            return powers.isEmpty() ? "" : powers.keySet().iterator().next();
        }
    }

    static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    /** Lecture pour d'autres paquets : clés allumées (tous pouvoirs), premier pouvoir, et la liste des pouvoirs. */
    public static final java.util.function.Function<UUID, Set<String>> STATE_VIEW = id -> {
        State s = STATES.get(id);
        return s == null ? null : s.on();
    };
    public static final java.util.function.Function<UUID, String> PATH_VIEW = id -> {
        State s = STATES.get(id);
        return s == null ? "" : s.path();
    };

    /** Clés communes (poses, tags lus par les calques). */
    public static Set<String> common(UUID id) {
        State s = STATES.get(id);
        return s == null ? Set.of() : s.common;
    }

    /** Pouvoirs détenus par ce joueur (chemins). */
    public static java.util.Set<String> paths(UUID id) {
        State s = STATES.get(id);
        return s == null ? Set.of() : s.powers.keySet();
    }

    /** Clés allumées de ce pouvoir (null s'il ne le détient pas), avec les clés communes. */
    public static Set<String> keysOf(UUID id, String path) {
        State s = STATES.get(id);
        if (s == null || !s.powers.containsKey(path)) return null;
        Set<String> k = new java.util.HashSet<>(s.powers.get(path));
        k.addAll(s.common);
        return k;
    }

    public static void accept(PassStatePacket p) {
        if (p.paths.isEmpty() && !p.am) {
            STATES.remove(p.player);
            return;
        }
        java.util.LinkedHashMap<String, Set<String>> pw = new java.util.LinkedHashMap<>();
        for (int i = 0; i < p.paths.size(); i++) pw.put(p.paths.get(i), Set.copyOf(p.keys.get(i)));
        STATES.put(p.player, new State(pw, Set.copyOf(p.common), p.am, p.locked, p.transformed));
    }

    public static void clear() {
        STATES.clear();
    }

    /** Le pouvoir du joueur parmi « paths » (lightning, lightning_am...), sinon null. */
    public static Holder holder(AbstractClientPlayer p, String[] paths) {
        if (p == null) return null;
        for (String path : paths) {
            Set<String> k = keysOf(p.getUUID(), path);
            if (k != null) return new Holder(path, k);
        }
        return null;
    }

    public static boolean isAM(UUID id) {
        State s = STATES.get(id);
        return s != null && s.am;
    }

    public static int lockedId(UUID id) {
        State s = STATES.get(id);
        return s == null ? -1 : s.locked;
    }

    public static boolean isTransformed(UUID id) {
        State s = STATES.get(id);
        return s != null && s.transformed;
    }

    /** Couleur d'aura choisie dans l'onglet Style (-1 = aucune). */
    public static int color(UUID id) {
        StyleData s = StyleService.CLIENT_CACHE.get(id);
        if (s == null || (s.auraColor & 0xFFFFFF) == PassServer.UNSET) return -1;
        return s.auraColor & 0xFFFFFF;
    }

    /** CastFxEvents.sound du modèle. */
    public static SoundEvent sound(String id) {
        ResourceLocation rl = new ResourceLocation(id);
        SoundEvent s = BuiltInRegistries.SOUND_EVENT.get(rl);
        return s != null ? s : SoundEvent.createVariableRangeEvent(rl);
    }

    /** Réglage « effets d'écran » du HUD du modèle (flash, voile gris). */
    public static boolean screenEffects() {
        return com.myriam.unrp.config.UnrpClientConfig.SCREEN_EFFECTS.get();
    }

    /** PowerEyes.passFlare : les yeux s'embrasent un instant (nos yeux de l'onglet Style). */
    public static void passFlare(int entity, double flare, double t) {
        FLARE.put(entity, new double[] {flare, t});
    }

    public static final Map<Integer, double[]> FLARE = new ConcurrentHashMap<>();

    /** Centre des yeux sur le visage, en pixels de tête (CustomEyesLayer.centers), d'après l'onglet Style. */
    public static double[][] eyeCenters(AbstractClientPlayer p) {
        StyleData s = StyleService.CLIENT_CACHE.get(p.getUUID());
        if (s == null || s.eyeStyle <= 0) return new double[][] {{-2.0, -3.5}, {2.0, -3.5}};
        double cy = -8.0 + 2.5 + s.eyeY * 0.6, half = s.eyeGap / 2.0;
        return new double[][] {{-half, cy}, {half, cy}};
    }

    /** DescentFade.download du modèle : copie une texture déjà chargée. */
    public static NativeImage download(ResourceLocation src) {
        AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(src);
        RenderSystem.bindTexture(tex.getId());
        int w = GlStateManager._getTexLevelParameter(3553, 0, 4096);
        int h = GlStateManager._getTexLevelParameter(3553, 0, 4097);
        if (w <= 0 || h <= 0 || w > 1024 || h > 1024) return null;
        NativeImage img = new NativeImage(w, h, false);
        img.downloadTexture(0, false);
        return img;
    }

    /** Penchée de Black Lash (LashLean.world du modèle). */
    public static Quaternionf lean(AbstractClientPlayer p, float yaw, float pt) {
        return com.myriam.unrp.uo.LashLean.world(p, yaw, pt);
    }

    /** ClientSaltSeraph.odd du modèle : créatures sans corps humanoïde exploitable. */
    public static boolean odd(LivingEntity e) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
        if (key == null) return true;
        String id = key.toString();
        return id.equals("minecraft:slime") || id.equals("minecraft:magma_cube") || id.equals("minecraft:ghast") || id.equals("minecraft:phantom")
                || id.equals("minecraft:giant") || id.equals("minecraft:pufferfish") || id.equals("minecraft:elder_guardian")
                || id.equals("minecraft:wither") || id.equals("minecraft:ender_dragon") || id.equals("minecraft:shulker");
    }

    /** ClientSaltSeraph.scaleOf du modèle : échelle de rendu propre à chaque créature. */
    public static float scaleOf(LivingEntity e) {
        return ScaleTable.of(e);
    }
}
