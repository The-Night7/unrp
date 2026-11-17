package com.myriam.unrp.data;

import net.minecraft.network.FriendlyByteBuf;

public class StyleData {
    public static final String[] EYE_STYLES = {"None", "Classique", "Rond", "Sharp", "Anneau"};
    public static final int[] PALETTE = {0x8A4DFF, 0x33C3FF, 0xFF5A5A, 0x55E08A, 0xFFC94D, 0xFFFFFF,
            0xFF8AD8, 0xFF8A33, 0x2F6BFF, 0x00E5C0, 0xB6FF3B, 0x9A9AAE};

    public int auraColor = 0x8A4DFF;

    public int eyeStyle = 0;
    public int eyeColor = 0x8A4DFF;

    public int eyeW = 2, eyeH = 2;

    public int eyeGap = 4, eyeY = 3;

    public int beamColor = -1;

    public String alias = "";

    public StyleData copy() {
        StyleData s = new StyleData();
        s.auraColor = auraColor;
        s.eyeStyle = eyeStyle;
        s.eyeColor = eyeColor;
        s.eyeW = eyeW;
        s.eyeH = eyeH;
        s.eyeGap = eyeGap;
        s.eyeY = eyeY;
        s.beamColor = beamColor;
        s.alias = alias;
        return s;
    }

    public StyleData clamp() {
        auraColor &= 0xFFFFFF;
        eyeColor &= 0xFFFFFF;
        if (beamColor >= 0) beamColor &= 0xFFFFFF;
        else beamColor = -1;
        eyeStyle = Math.max(0, Math.min(EYE_STYLES.length - 1, eyeStyle));
        eyeW = Math.max(1, Math.min(4, eyeW));
        eyeH = Math.max(1, Math.min(4, eyeH));
        eyeGap = Math.max(2, Math.min(6, eyeGap));
        eyeY = Math.max(0, Math.min(6, eyeY));
        alias = alias == null ? "" : alias.trim();
        if (!alias.matches("^[\\p{L}0-9 '\\-]{0,16}$")) alias = "";
        return this;
    }

    public void write(FriendlyByteBuf b) {
        b.writeVarInt(auraColor);
        b.writeVarInt(eyeStyle);
        b.writeVarInt(eyeColor);
        b.writeVarInt(eyeW);
        b.writeVarInt(eyeH);
        b.writeVarInt(eyeGap);
        b.writeVarInt(eyeY);
        b.writeInt(beamColor);
        b.writeUtf(alias, 32);
    }

    public static StyleData read(FriendlyByteBuf b) {
        StyleData s = new StyleData();
        s.auraColor = b.readVarInt();
        s.eyeStyle = b.readVarInt();
        s.eyeColor = b.readVarInt();
        s.eyeW = b.readVarInt();
        s.eyeH = b.readVarInt();
        s.eyeGap = b.readVarInt();
        s.eyeY = b.readVarInt();
        s.beamColor = b.readInt();
        s.alias = b.readUtf(32);
        return s.clamp();
    }
}
