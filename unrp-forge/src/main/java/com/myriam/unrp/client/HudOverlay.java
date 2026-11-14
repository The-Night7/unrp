package com.myriam.unrp.client;

import com.myriam.unrp.config.UnrpClientConfig;
import com.myriam.unrp.net.PlayerStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Locale;

/**
 * HUD stylisé : carte du personnage (accent de rareté, aura segmentée, halo pulsant quand le pouvoir est actif)
 * et barre de mouvements (cadres lumineux, touche, initiale, coût, recharge qui se vide).
 */
public final class HudOverlay implements IGuiOverlay {
    public static final HudOverlay INSTANCE = new HudOverlay();
    private static final int CW = 166;

    private HudOverlay() {}

    private static int rgb(int c) {
        return c & 0xFFFFFF;
    }

    private static int argb(int a, int c) {
        return (a << 24) | rgb(c);
    }

    private static int mix(int c1, int c2, float t) {
        int r = Math.round(((c1 >> 16) & 255) * (1 - t) + ((c2 >> 16) & 255) * t);
        int g = Math.round(((c1 >> 8) & 255) * (1 - t) + ((c2 >> 8) & 255) * t);
        int b = Math.round((c1 & 255) * (1 - t) + (c2 & 255) * t);
        return (r << 16) | (g << 8) | b;
    }

    /** Cadre de 1 px, coins coupés. */
    private static void frame(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x + 1, y, x + w - 1, y + 1, color);
        g.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    /** Panneau du style de l'interface (voir UiSkin). */
    private static void panel(GuiGraphics g, int x, int y, int w, int h, int border) {
        UiSkin.panel(g, x, y, w, h, border);
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int screenW, int screenH) {
        PlayerStatePacket s = ClientState.get();
        Minecraft mc = Minecraft.getInstance();
        if (!s.hasProfile || !UnrpClientConfig.HUD_VISIBLE.get() || mc.options.hideGui || mc.level == null) return;
        float time = mc.level.getGameTime() + partialTick;
        int aura = rgb(UnrpClientConfig.COLOR_VALUES[UnrpClientConfig.AURA_COLOR.get()]);
        float pulse = 0.5f + 0.5f * (float) Math.sin(time * 0.15);
        card(g, s, mc, screenW, screenH, aura, pulse);
        if (s.hasAbility) moveBar(g, s, mc, partialTick, screenW, screenH, aura, pulse);
    }

    // ------------------------------------------------------------------ panneau d'état (comme le modèle)
    /** Taille du panneau, en pixels d'interface avant l'échelle choisie dans l'onglet Style. */
    public static final int PW = 200, PH = 80;
    /** valeurs "fantômes" : la partie perdue s'efface lentement (traînée sombre derrière la barre) */
    private static float ghostHp = -1, ghostAura = -1;

    /** Couleurs du panneau (même esprit que le modèle : noir violacé, rouge, dégradé violet -> cyan, or). */
    private static final int WINDOW = 0xEE07050B, TRACK = 0xFF221D2C, EDGE_OUT = 0xFF000000;
    private static final int HP_FILL = 0xB82626, HP_GHOST = 0xFF5A1A1B, ABS_FILL = 0xF0C846;
    private static final int AURA_FROM = 0x9646DC, AURA_TO = 0x5AC8F0, AURA_GHOST = 0xFF264250;
    private static final int FOOD_FILL = 0xD69628, ARMOR_FILL = 0xA5A9B4;
    private static final int HEAD = 0xFFE8D9A8, SUB = 0xFFA8A4B8;

    /** Rectangle du panneau d'état à l'écran {x, y, largeur, hauteur}, ou null s'il n'est pas affiché. */
    public static int[] cardRect(int sw, int sh) {
        if (!ClientState.get().hasProfile || !UnrpClientConfig.HUD_VISIBLE.get()) return null;
        float sc = UnrpClientConfig.SCALE_VALUES[UnrpClientConfig.HUD_SCALE.get()];
        int pw = Math.round(PW * sc), ph = Math.round(PH * sc), margin = 6;
        int corner = UnrpClientConfig.HUD_CORNER.get();
        int px = (corner == 1 || corner == 3) ? sw - pw - margin : margin;
        int py = corner < 2 ? sh - ph - margin - 40 : margin;
        return new int[] {px, py, pw, ph};
    }

    /** Vrai quand notre panneau remplace les cœurs, la faim et l'armure de Minecraft. */
    public static boolean replacesVanilla() {
        Minecraft mc = Minecraft.getInstance();
        return ClientState.get().hasProfile && UnrpClientConfig.HUD_VISIBLE.get() && mc.player != null
                && mc.gameMode != null && mc.gameMode.canHurtPlayer();
    }

    /**
     * Panneau d'état en haut à gauche : portrait 3D qui tourne, nom, rareté et niveau, barres de vie, d'aura,
     * de faim et d'armure (avec traînée quand la valeur baisse). Remplace les cœurs / la faim / l'armure vanilla.
     */
    private void card(GuiGraphics g, PlayerStatePacket s, Minecraft mc, int sw, int sh, int aura, float pulse) {
        float sc = UnrpClientConfig.SCALE_VALUES[UnrpClientConfig.HUD_SCALE.get()];
        int pw = Math.round(PW * sc), ph = Math.round(PH * sc), margin = 6;
        int corner = UnrpClientConfig.HUD_CORNER.get();
        int px = (corner == 1 || corner == 3) ? sw - pw - margin : margin;
        int py = corner < 2 ? sh - ph - margin - 40 : margin;
        var pl = mc.player;
        boolean depleted = s.hasAbility && s.aura <= 0 && s.maxAura > 0;
        int edge = depleted ? 0xFFB03A48 : s.active ? 0xFF000000 | mix(aura, 0xFFFFFF, 0.35f * pulse) : UiSkin.BORDER;

        g.pose().pushPose();
        g.pose().translate(px, py, 0);
        g.pose().scale(sc, sc, 1f);

        // fond et cadres
        box(g, 0, 0, 62, PH, edge);
        box(g, 65, 0, PW - 65, PH, edge);
        // portrait : modèle 3D du joueur qui tourne lentement
        int sx0 = px + Math.round(3 * sc), sy0 = py + Math.round(3 * sc);
        int sx1 = px + Math.round(59 * sc), sy1 = py + Math.round((PH - 3) * sc);
        g.fillGradient(3, 3, 59, PH - 3, 0xFF1A1426, 0xFF07050B);
        g.pose().popPose();
        g.enableScissor(sx0, sy0, sx1, sy1);
        float spin = (System.currentTimeMillis() % 8000L) / 8000f * (float) (Math.PI * 2);
        org.joml.Quaternionf pose = new org.joml.Quaternionf().rotateZ((float) Math.PI).rotateY(spin);
        // le joueur est tourné face à la caméra le temps du rendu, puis remis comme avant
        float bodyRot = pl.yBodyRot, yRot = pl.getYRot(), xRot = pl.getXRot(), headO = pl.yHeadRotO, head = pl.yHeadRot;
        pl.yBodyRot = 180f;
        pl.setYRot(180f);
        pl.setXRot(0f);
        pl.yHeadRot = 180f;
        pl.yHeadRotO = 180f;
        try {
            net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventory(g,
                    px + Math.round(31 * sc), py + Math.round(73 * sc), Math.round(30 * sc), pose, null, pl);
        } catch (Exception ignored) {
            // un autre mod peut gêner le rendu du joueur : on laisse le portrait vide
        } finally {
            pl.yBodyRot = bodyRot;
            pl.setYRot(yRot);
            pl.setXRot(xRot);
            pl.yHeadRotO = headO;
            pl.yHeadRot = head;
        }
        g.disableScissor();
        g.pose().pushPose();
        g.pose().translate(px, py, 200);
        g.pose().scale(sc, sc, 1f);
        int tier = 0xFF000000 | (TierStyle.color(s.tier) & 0xFFFFFF);
        g.fill(3, PH - 5, 59, PH - 3, tier); // liseré de rareté sous le portrait

        // en-tête : nom, rareté, niveau
        g.drawString(mc.font, mc.font.plainSubstrByWidth(s.name, 128), 70, 6, HEAD, true);
        String tl = TierStyle.label(s.tier).toUpperCase(Locale.ROOT);
        g.drawString(mc.font, tl, 70, 16, tier, false);
        g.drawString(mc.font, "niv. " + s.level, 70 + mc.font.width(tl) + 6, 16, SUB, false);
        if (s.active) {
            String on = "ACTIF";
            int w = mc.font.width(on);
            g.fill(PW - 9 - w, 15, PW - 5, 25, 0xFF000000 | mix(aura, 0x000000, 0.5f));
            g.drawString(mc.font, on, PW - 7 - w, 16, 0xFFFFFFFF, false);
        }

        // vie
        float hp = pl.getHealth(), maxHp = Math.max(1f, pl.getMaxHealth()), abs = pl.getAbsorptionAmount();
        float hpR = Math.min(1f, hp / maxHp);
        ghostHp = ghost(ghostHp, hpR);
        bar(g, 70, 28, 194, 37, hpR, ghostHp, HP_FILL, mix(HP_FILL, 0x000000, 0.35f), HP_GHOST);
        if (abs > 0) g.fill(71, 29, 71 + Math.round(123 * Math.min(1f, abs / maxHp)), 31, 0xFF000000 | ABS_FILL);
        num(g, mc, (int) Math.ceil(hp + abs) + " / " + (int) maxHp, 132, 30, 0.75f);

        // aura
        if (s.hasAbility) {
            float aR = s.maxAura <= 0 ? 0f : Math.min(1f, (float) s.aura / s.maxAura);
            ghostAura = ghost(ghostAura, aR);
            boolean custom = UnrpClientConfig.AURA_COLOR.get() != 0; // couleur choisie dans l'onglet Style
            int from = custom ? mix(aura, 0xFFFFFF, 0.25f) : AURA_FROM, to = custom ? mix(aura, 0x000000, 0.3f) : AURA_TO;
            bar(g, 70, 41, 194, 50, aR, ghostAura, from, to, AURA_GHOST);
            num(g, mc, depleted ? "ÉPUISÉE" : s.aura + " / " + s.maxAura, 132, 43, 0.75f);
        }

        // faim et armure
        float food = pl.getFoodData().getFoodLevel() / 20f, armor = Math.min(1f, pl.getArmorValue() / 20f);
        bar(g, 70, 55, 129, 62, food, food, FOOD_FILL, mix(FOOD_FILL, 0x000000, 0.3f), 0xFF3A2B14);
        bar(g, 135, 55, 194, 62, armor, armor, ARMOR_FILL, mix(ARMOR_FILL, 0x000000, 0.3f), 0xFF2F3035);
        num(g, mc, String.valueOf(pl.getFoodData().getFoodLevel()), 99, 56, 0.7f);
        num(g, mc, String.valueOf(pl.getArmorValue()), 164, 56, 0.7f);
        g.drawString(mc.font, "faim", 70, 66, 0xFF7A768A, false);
        g.drawString(mc.font, "armure", 194 - mc.font.width("armure"), 66, 0xFF7A768A, false);
        g.pose().popPose();
    }

    /** Cadre : fond presque noir, contour noir, bord coloré, liseré intérieur clair en haut, coins dorés. */
    private static void box(GuiGraphics g, int x, int y, int w, int h, int edge) {
        g.fill(x, y, x + w, y + h, WINDOW);
        frame(g, x, y, w, h, EDGE_OUT);
        frame(g, x + 1, y + 1, w - 2, h - 2, edge);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, 0x30FFFFFF);
        g.fill(x + 1, y + 1, x + 3, y + 3, UiSkin.GOLD);
        g.fill(x + w - 3, y + 1, x + w - 1, y + 3, UiSkin.GOLD);
        g.fill(x + 1, y + h - 3, x + 3, y + h - 1, UiSkin.GOLD);
        g.fill(x + w - 3, y + h - 3, x + w - 1, y + h - 1, UiSkin.GOLD);
    }

    /** Barre : rail sombre, traînée "fantôme", remplissage en dégradé horizontal, reflet, graduations. */
    private static void bar(GuiGraphics g, int x0, int y0, int x1, int y1, float r, float ghost, int from, int to, int ghostCol) {
        g.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, EDGE_OUT);
        g.fill(x0, y0, x1, y1, TRACK);
        int w = x1 - x0;
        int fw = Math.round(w * Math.max(0f, Math.min(1f, r)));
        int gw = Math.round(w * Math.max(0f, Math.min(1f, ghost)));
        if (gw > fw) g.fill(x0 + fw, y0, x0 + gw, y1, ghostCol);
        for (int i = 0; i < fw; i++) { // dégradé horizontal colonne par colonne
            g.fill(x0 + i, y0, x0 + i + 1, y1, 0xFF000000 | mix(from, to, w <= 1 ? 0f : i / (float) (w - 1)));
        }
        if (fw > 0) {
            g.fill(x0, y0, x0 + fw, y0 + 1, 0x55FFFFFF);
            g.fill(x0, y1 - 1, x0 + fw, y1, 0x40000000);
        }
        for (int t = 1; t < 10; t++) {
            int tx = x0 + w * t / 10;
            g.fill(tx, y0 + 1, tx + 1, y1 - 1, 0x28000000);
        }
    }

    private static float ghost(float g, float r) {
        if (g < 0 || r >= g) return r;
        return Math.max(r, g - Math.max(0.002f, (g - r) * 0.03f));
    }

    /** Nombre centré sur une barre, avec ombre. */
    private static void num(GuiGraphics g, Minecraft mc, String t, int cx, int y, float scale) {
        g.pose().pushPose();
        g.pose().translate(cx, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(mc.font, t, -mc.font.width(t) / 2, 0, 0xFFFFFFFF, true);
        g.pose().popPose();
    }

    /**
     * Barre de capacités verticale à gauche de l'écran, comme la barre de pouvoirs du modèle :
     * une case par capacité (touche, initiale, coût), son nom à droite. Allumée = prête ou en cours,
     * voile = recharge, jauge montante = charge, cadre plein = interrupteur allumé.
     */
    private void moveBar(GuiGraphics g, PlayerStatePacket s, Minecraft mc, float partial, int sw, int sh, int aura, float pulse) {
        int n = s.moveNames.length;
        if (n == 0) return;
        int slot = 26, gap = 4;
        int total = n * slot + (n - 1) * gap;
        float hs = UnrpClientConfig.SCALE_VALUES[UnrpClientConfig.HUD_SCALE.get()];
        int x = 8, y0 = Math.max(Math.round(PH * hs) + 30, sh / 2 - total / 2); // sous le panneau d'état
        int dim = 0xFF55586A;

        String top = s.active ? (s.modeName.isEmpty() ? "POUVOIR ACTIF" : s.modeName.toUpperCase(Locale.ROOT)) : "R : ACTIVER";
        int tw = mc.font.width(top) + 14;
        int ty = y0 - 18;
        panel(g, x - 2, ty, tw, 13, s.active ? argb(255, aura) : dim);
        if (s.active) g.fill(x + 2, ty + 5, x + 5, ty + 8, argb(Math.round(120 + 135 * pulse), aura));
        g.drawString(mc.font, top, x + (s.active ? 8 : 5), ty + 3, s.active ? 0xFFFFFFFF : 0xFF9AA0B5);

        for (int i = 0; i < n; i++) {
            int y = y0 + i * (slot + gap);
            float left = ClientState.cooldownLeft(i, partial);
            float cd = Math.max(1, s.moveCd[i]);
            boolean on = s.moveOn.length > i && s.moveOn[i];
            boolean locked = s.moveLocked.length > i && s.moveLocked[i];
            boolean ready = s.active && left <= 0 && !locked;
            int border = !s.active ? dim : on ? 0xFFFFFFFF : left > 0 ? mix(aura, 0x000000, 0.55f) | 0xFF000000
                    : argb(255, mix(aura, 0xFFFFFF, 0.25f * pulse));
            if (ready && !on) frame(g, x - 1, y - 1, slot + 2, slot + 2, argb(Math.round(40 + 70 * pulse), aura));
            panel(g, x, y, slot, slot, border);
            if (on) g.fill(x + 1, y + 1, x + slot - 1, y + slot - 1, argb(Math.round(70 + 60 * pulse), aura));
            String ini = s.moveNames[i].isEmpty() ? "?" : s.moveNames[i].substring(0, 1).toUpperCase(Locale.ROOT);
            g.pose().pushPose();
            g.pose().translate(x + slot / 2f, y + 6, 0);
            g.pose().scale(1.6f, 1.6f, 1f);
            g.drawString(mc.font, ini, -mc.font.width(ini) / 2, 0, ready ? argb(255, mix(aura, 0xFFFFFF, 0.4f)) : 0xFF6A6D80, false);
            g.pose().popPose();
            if (left > 0) {
                int h = Math.round((slot - 2) * Math.min(1f, left / cd));
                g.fill(x + 1, y + slot - 1 - h, x + slot - 1, y + slot - 1, 0xB0000000);
                String t = left >= 20 ? String.format(Locale.ROOT, "%.0f", left / 20f) : String.format(Locale.ROOT, "%.1f", left / 20f);
                g.drawString(mc.font, t, x + slot / 2 - mc.font.width(t) / 2, y + slot / 2 - 4, 0xFFFFFFFF);
            }
            if (s.moveCharge[i] > 0 && s.active && left <= 0 && ClientEvents.holdStart[i] >= 0) {
                float pr = Math.min(1f, (mc.level.getGameTime() + partial - ClientEvents.holdStart[i]) / s.moveCharge[i]);
                int h = Math.round((slot - 2) * pr);
                g.fill(x + 1, y + slot - 1 - h, x + slot - 1, y + slot - 1, argb(pr >= 1f ? 200 : 130, mix(aura, 0xFFFFFF, 0.3f)));
            }
            // touche (badge), nom et coût à droite
            int kb = x + slot - 6, ky = y - 3;
            g.fill(kb, ky, kb + 11, ky + 11, 0xFF0C0C14);
            frame(g, kb, ky, 11, 11, border);
            g.drawString(mc.font, ClientEvents.SLOT_LETTERS[i], kb + 3, ky + 2, 0xFFFFE9A0, false);
            String kind = s.moveToggle.length > i && s.moveToggle[i] ? (on ? "ON" : "OFF")
                    : s.moveHeld[i] ? "maintenir" : s.moveCharge[i] > 0 ? "charger" : "";
            if (locked) {
                g.fill(x + 1, y + 1, x + slot - 1, y + slot - 1, 0xC0000000);
                String lr = "R" + (s.moveRank.length > i && s.moveRank[i] >= 2 ? s.moveRank[i] : "?");
                g.drawString(mc.font, lr, x + slot / 2 - mc.font.width(lr) / 2, y + slot / 2 - 4, 0xFFFF8A8A);
            }
            String nm = mc.font.plainSubstrByWidth(s.moveNames[i], 110);
            g.drawString(mc.font, nm, x + slot + 6, y + 4, s.active ? 0xFFE6E8F2 : 0xFF666A7C);
            String sub = (s.moveCost[i] > 0 ? s.moveCost[i] + " aura" : "") + (kind.isEmpty() ? "" : (s.moveCost[i] > 0 ? " · " : "") + kind);
            g.drawString(mc.font, sub, x + slot + 6, y + 14, ready ? 0xFF8FE3FF : 0xFF5A6A78, false);
        }
    }
}
