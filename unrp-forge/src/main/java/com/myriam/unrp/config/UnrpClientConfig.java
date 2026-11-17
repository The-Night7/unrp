package com.myriam.unrp.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class UnrpClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue HUD_VISIBLE;
    public static final ForgeConfigSpec.IntValue HUD_SCALE;
    public static final ForgeConfigSpec.IntValue HUD_CORNER;
    public static final ForgeConfigSpec.IntValue AURA_COLOR;
    public static final ForgeConfigSpec.BooleanValue HUB_SOUNDS;
    public static final ForgeConfigSpec.BooleanValue SCREEN_EFFECTS;

    public static final String[] CORNERS = {"Bottom left", "Bottom right", "Top left", "Top right"};
    public static final String[] SCALES = {"Small", "Normal", "Large"};
    public static final float[] SCALE_VALUES = {0.75f, 1.0f, 1.25f};
    public static final String[] COLOR_NAMES = {"Purple", "Cyan", "Red", "Green", "Gold", "White"};
    public static final int[] COLOR_VALUES = {0x8A4DFF, 0x33C3FF, 0xFF5A5A, 0x55E08A, 0xFFC94D, 0xFFFFFF};

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("hud");
        HUD_VISIBLE = b.comment("Show the character HUD").define("visible", true);
        HUD_SCALE = b.comment("Size: 0 small, 1 normal, 2 large").defineInRange("scale", 1, 0, 2);
        HUD_CORNER = b.comment("Corner: 0 bottom left (below chat), 1 bottom right, 2 top left (minimap), 3 top right (effects)")
                .defineInRange("corner", 2, 0, 3);
        AURA_COLOR = b.comment("Aura bar colour index").defineInRange("aura_color", 0, 0, COLOR_VALUES.length - 1);
        HUB_SOUNDS = b.comment("Hub click sounds").define("hub_sounds", true);
        SCREEN_EFFECTS = b.comment("Ability screen effects: flashes and grayscale overlays").define("screen_effects", true);
        b.pop();
        SPEC = b.build();
    }

    private UnrpClientConfig() {}
}
