package com.myriam.unrp.look;

import com.myriam.unrp.data.StyleData;
import com.myriam.unrp.style.StyleService;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public final class ClientSettingsCache {
    private ClientSettingsCache() {}

    public static boolean isAM(UUID id) {
        return com.myriam.unrp.pass.PassBridge.isAM(id);
    }

    public static int getColor(UUID id) {
        return com.myriam.unrp.pass.PassBridge.color(id);
    }

    public static int getEyeColor(UUID id) {
        StyleData s = StyleService.CLIENT_CACHE.get(id);
        return s == null || s.eyeColor == s.auraColor ? -1 : s.eyeColor & 0xFFFFFF;
    }

    public static CompoundTag getTag(UUID id) {
        StyleData s = StyleService.CLIENT_CACHE.get(id);
        CompoundTag t = new CompoundTag();
        if (s == null) return t;

        t.putInt("EyeStyle", s.eyeStyle == 2 ? 1 : s.eyeStyle == 4 ? 2 : 0);
        t.putInt("EyeW", Math.max(1, Math.min(4, s.eyeW)));
        t.putInt("EyeH", Math.max(1, Math.min(3, s.eyeH)));
        t.putInt("EyeL", s.eyeStyle == 0 ? 0 : 1);
        t.putInt("EyeR", s.eyeStyle == 0 ? 0 : 1);
        int w = Math.max(1, Math.min(4, s.eyeW));
        int off = (int) Math.round(-s.eyeGap / 2.0 + 1 + w / 2.0);
        t.putInt("EyeRX", Math.max(-4, Math.min(4, off)));
        t.putInt("EyeLX", Math.max(-4, Math.min(4, -off)));
        int dy = (int) Math.round(-8.0 + 2.5 + s.eyeY * 0.6 + 4 - 0.5);
        t.putInt("EyeRY", Math.max(-4, Math.min(4, dy)));
        t.putInt("EyeLY", Math.max(-4, Math.min(4, dy)));
        t.putInt("EyeTrail", 1);
        return t;
    }
}
