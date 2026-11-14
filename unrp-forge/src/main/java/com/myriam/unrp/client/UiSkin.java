package com.myriam.unrp.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Le style visuel de l'interface (HUD et hub) : panneaux pixel-art brun sombre à bord doré et coins lumineux,
 * ciel étoilé ambré, vagues dorées, barre d'aura à rubans. Toutes les couleurs sont ici.
 * Les textures (aura_back, aura_front, stars) sont dessinées par tools/make_gui_textures.py.
 */
public final class UiSkin {
    public static final ResourceLocation AURA_BACK = new ResourceLocation("unrp", "textures/gui/aura_back.png");
    public static final ResourceLocation AURA_FRONT = new ResourceLocation("unrp", "textures/gui/aura_front.png");
    public static final ResourceLocation STARS = new ResourceLocation("unrp", "textures/gui/stars.png");

    public static final int OUTLINE = 0xFF0C0904;
    public static final int PANEL_TOP = 0xFF251D10, PANEL_BOTTOM = 0xFF100D07;
    public static final int BORDER = 0xFFC89B3C, BORDER_DIM = 0xFF6C5125, GOLD = 0xFFF0C846;
    /** titres (crème doré), texte, texte secondaire : la palette du hub du modèle */
    public static final int HEAD = 0xFFFFE2A0, TEXT = 0xFFF3EBD7, SUB = 0xFFB8A888;
    public static final int SURFACE = 0xFF302615, SELECTED = 0xFF4A391B;
    public static final int BUTTON_TOP = 0xFF302513, BUTTON_BOTTOM = 0xFF171108;
    public static final int HOVER_TOP = 0xFF4A3718, HOVER_BOTTOM = 0xFF291D0B;
    public static final int DISABLED = 0xFF30291B, DISABLED_TEXT = 0xFF796C50;
    public static final int MUTED = 0xFF897B5E, WINDOW_SHADE = 0xB80C0904;
    public static final int WAVE = 0x66D9AD48;

    /** taille de la texture de la barre d'aura et zone intérieure remplie par le jeu */
    public static final int AURA_W = 108, AURA_H = 20, AURA_IX = 9, AURA_IY = 6, AURA_IW = 90, AURA_IH = 8;

    private UiSkin() {}

    /** Panneau : contour noir, bord coloré, fond en dégradé brun sombre, reflet, coins dorés. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h, int border) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, OUTLINE);
        g.fill(x, y, x + w, y + h, border);
        g.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_TOP, PANEL_BOTTOM);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x28FFFFFF);
        if (w >= 8 && h >= 8) {
            g.fill(x, y, x + 2, y + 2, GOLD);
            g.fill(x + w - 2, y, x + w, y + 2, GOLD);
            g.fill(x, y + h - 2, x + 2, y + h, GOLD);
            g.fill(x + w - 2, y + h - 2, x + w, y + h, GOLD);
        }
    }

    /** Ciel étoilé répété sur la zone. */
    public static void stars(GuiGraphics g, int x, int y, int w, int h) {
        RenderSystem.enableBlend();
        for (int ty = 0; ty < h; ty += 64) {
            for (int tx = 0; tx < w; tx += 64) {
                g.blit(STARS, x + tx, y + ty, 0, 0, Math.min(64, w - tx), Math.min(64, h - ty), 64, 64);
            }
        }
        RenderSystem.disableBlend();
    }

    /** Deux vagues dorées animées (décor derrière les panneaux). */
    public static void waves(GuiGraphics g, int x, int y, int w, int h, float time, int color) {
        for (int k = 0; k < 2; k++) {
            double ph = time * 0.05 + k * Math.PI;
            for (int i = 0; i < w; i++) {
                int yy = y + h / 2 + (int) Math.round(Math.sin(i / 9.0 + ph) * (h / 2.0 - 1));
                g.fill(x + i, yy, x + i + 1, yy + 1, color);
            }
        }
    }

    /** Barre d'aura à rubans. ratio 0..1 ; rgb = couleur de remplissage ; depleted = cadre rougi. */
    public static void auraBar(GuiGraphics g, int x, int y, float ratio, int rgb, boolean depleted, float time) {
        RenderSystem.enableBlend();
        if (depleted) RenderSystem.setShaderColor(1f, 0.45f, 0.45f, 1f);
        g.blit(AURA_BACK, x, y, 0, 0, AURA_W, AURA_H, AURA_W, AURA_H);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int fw = Math.round(AURA_IW * Math.max(0f, Math.min(1f, ratio)));
        int ix = x + AURA_IX, iy = y + AURA_IY;
        if (fw > 0) {
            int light = mix(rgb, 0xFFFFFF, 0.45f), dark = mix(rgb, 0x000000, 0.35f);
            g.fillGradient(ix, iy, ix + fw, iy + AURA_IH, 0xFF000000 | light, 0xFF000000 | dark);
            g.fill(ix, iy, ix + fw, iy + 1, 0x66FFFFFF);
            // reflet qui glisse le long de la barre
            int sx = (int) ((time * 1.5f) % (AURA_IW + 20)) - 10;
            for (int i = 0; i < 6; i++) {
                int cx = ix + sx + i;
                if (cx >= ix && cx < ix + fw) g.fill(cx, iy + 1, cx + 1, iy + AURA_IH - 1, 0x30FFFFFF);
            }
            g.fill(ix + fw - 1, iy, ix + fw, iy + AURA_IH, 0xAAFFFFFF);
        }
        g.blit(AURA_FRONT, x, y, 0, 0, AURA_W, AURA_H, AURA_W, AURA_H);
        RenderSystem.disableBlend();
    }

    /** Titre en majuscules espacées (comme les titres du hub du modèle). */
    public static void spaced(GuiGraphics g, net.minecraft.client.gui.Font font, String t, int x, int y, int color) {
        int cx = x;
        for (char ch : t.toUpperCase(java.util.Locale.ROOT).toCharArray()) {
            String c = String.valueOf(ch);
            g.drawString(font, c, cx, y, color, true);
            cx += font.width(c) + 1;
        }
    }

    public static int mix(int c1, int c2, float t) {
        int r = Math.round(((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int gg = Math.round(((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = Math.round((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (r << 16) | (gg << 8) | b;
    }
}
