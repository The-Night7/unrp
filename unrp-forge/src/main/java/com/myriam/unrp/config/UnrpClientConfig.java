package com.myriam.unrp.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Réglages d'affichage du joueur (onglet Style du hub). Fichier : config/unrp-client.toml */
public final class UnrpClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue HUD_VISIBLE;
    public static final ForgeConfigSpec.IntValue HUD_SCALE;
    public static final ForgeConfigSpec.IntValue HUD_CORNER;
    public static final ForgeConfigSpec.IntValue AURA_COLOR;
    public static final ForgeConfigSpec.BooleanValue HUB_SOUNDS;
    public static final ForgeConfigSpec.BooleanValue SCREEN_EFFECTS;

    public static final String[] CORNERS = {"Bas gauche", "Bas droite", "Haut gauche", "Haut droite"};
    public static final String[] SCALES = {"Petit", "Normal", "Grand"};
    public static final float[] SCALE_VALUES = {0.75f, 1.0f, 1.25f};
    public static final String[] COLOR_NAMES = {"Violet", "Cyan", "Rouge", "Vert", "Or", "Blanc"};
    public static final int[] COLOR_VALUES = {0x8A4DFF, 0x33C3FF, 0xFF5A5A, 0x55E08A, 0xFFC94D, 0xFFFFFF};

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("hud");
        HUD_VISIBLE = b.comment("Afficher le HUD du personnage").define("visible", true);
        HUD_SCALE = b.comment("Taille : 0 petit, 1 normal, 2 grand").defineInRange("scale", 1, 0, 2);
        HUD_CORNER = b.comment("Coin : 0 bas gauche (sous le chat), 1 bas droite, 2 haut gauche (minimap), 3 haut droite (effets)")
                .defineInRange("corner", 2, 0, 3);
        AURA_COLOR = b.comment("Couleur de la barre d'aura (index)").defineInRange("aura_color", 0, 0, COLOR_VALUES.length - 1);
        HUB_SOUNDS = b.comment("Sons de clic dans le hub").define("hub_sounds", true);
        SCREEN_EFFECTS = b.comment("Effets d'écran des pouvoirs : flash, voile gris (screenEffects du modèle)").define("screen_effects", true);
        b.pop();
        SPEC = b.build();
    }

    private UnrpClientConfig() {}
}
