package com.myriam.unrp.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.myriam.unrp.config.UnrpClientConfig;
import com.myriam.unrp.data.StyleData;
import com.myriam.unrp.net.HubActionPacket;
import com.myriam.unrp.net.StyleSavePacket;
import com.myriam.unrp.style.StyleService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import java.util.UUID;
import com.myriam.unrp.net.Net;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Le hub (touche H) : 8 onglets en deux groupes, comme le mod modèle.
 *   Perso : Pouvoir, Capacités, Entraînement, Style
 *   Social : École, Groupe, Territoire, Duels
 * Le client ne fait qu'afficher l'état envoyé par le serveur (ClientHub) et envoyer des actions.
 */
public class HubScreen extends Screen {
    private static final String[] TABS = {"Pouvoir", "Capacités", "Entraînement", "Style", "École", "Groupe", "Territoire", "Duels"};
    private static final String[] GLYPH = {"P", "C", "E", "S", "É", "G", "T", "D"};
    private static final String[] STAT_NAMES = {"Puissance", "Défense", "Récupération", "Ruse", "Vitesse"};
    private static final int MAX_W = 560, MAX_H = 324, DOCK_W = 22, DOCK_H = 20, DOCK_GAP = 3, GROUP_GAP = 9;

    private static final int BG = UiSkin.WINDOW_SHADE, EDGE = UiSkin.BORDER_DIM, PANEL = UiSkin.PANEL_TOP, TXT = UiSkin.TEXT,
            DIM = UiSkin.SUB, ACCENT = UiSkin.GOLD, GOOD = 0xFF55E08A, BAD = 0xFFFF6B6B;

    private record Region(int x, int y, int w, int h, Runnable run) {
        boolean in(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private int tab;
    private int abilityNode = -1, treeScroll, treeScrollMax, detailScroll, detailScrollMax;
    private String treeIdentity = "";
    private Region treeViewport, detailViewport;
    /** échelle d'affichage : le hub est conçu en MAX_W x MAX_H et réduit en bloc si l'écran est plus petit */
    private float ui = 1f;
    private int x0, y0, winW, winH, cx, cy, cw;
    private final List<Region> regions = new ArrayList<>();
    private EditBox box;
    private boolean boxWanted;
    private boolean feedOpen;
    private String selected = "";
    private int scroll;
    private int mx, my;
    private final List<String> tooltip = new ArrayList<>();

    public HubScreen() {
        super(Component.literal("Hub"));
    }

    @Override
    protected void init() {
        // le hub garde toujours la même mise en page : sur un petit écran (ou une grande échelle d'interface),
        // il est réduit d'un bloc au lieu de tasser ou de faire déborder son contenu
        ui = Math.min(1f, Math.min((width - 12f) / MAX_W, (height - 12f) / MAX_H));
        int vw = Math.round(width / ui), vh = Math.round(height / ui);
        winW = MAX_W;
        winH = MAX_H;
        x0 = (vw - winW) / 2;
        y0 = (vh - winH) / 2;
        box = new EditBox(font, 0, 0, 150, 14, Component.literal("nom"));
        box.setMaxLength(24);
        box.setVisible(false);
        addRenderableWidget(box);
        hexBox = new EditBox(font, 0, 0, 64, 14, Component.literal("hex"));
        hexBox.setMaxLength(7);
        hexBox.setVisible(false);
        addRenderableWidget(hexBox);
        send("open", "", "");
    }

    // ------------------------------------------------------------------ utilitaires JSON
    private static JsonObject obj(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e != null && e.isJsonObject() ? e.getAsJsonObject() : null;
    }

    private static JsonArray arr(JsonObject o, String k) {
        JsonElement e = o == null ? null : o.get(k);
        return e != null && e.isJsonArray() ? e.getAsJsonArray() : new JsonArray();
    }

    private static String str(JsonObject o, String k) {
        JsonElement e = o == null ? null : o.get(k);
        return e == null || e.isJsonNull() ? "" : e.getAsString();
    }

    private static int num(JsonObject o, String k) {
        JsonElement e = o == null ? null : o.get(k);
        return e == null || e.isJsonNull() ? 0 : (int) Math.round(e.getAsDouble());
    }

    private static boolean bool(JsonObject o, String k) {
        JsonElement e = o == null ? null : o.get(k);
        return e != null && !e.isJsonNull() && e.getAsBoolean();
    }

    private static void send(String action, String a, String b) {
        Net.CHANNEL.sendToServer(new HubActionPacket(action, a, b));
    }

    // ------------------------------------------------------------------ dessin
    private void panel(GuiGraphics g, int x, int y, int w, int h) {
        UiSkin.panel(g, x, y, w, h, UiSkin.BORDER_DIM);
    }

    private void text(GuiGraphics g, String s, int x, int y, int color) {
        g.drawString(font, s, x, y, color | 0xFF000000, false);
    }

    private int wrap(GuiGraphics g, String s, int x, int y, int w, int color) {
        for (String para : s.split("\n", -1)) {
            if (para.isEmpty()) {
                y += 5;
                continue;
            }
            for (FormattedCharSequence line : font.split(Component.literal(para), w)) {
                if (y > y0 + winH - 14) return y;
                g.drawString(font, line, x, y, color | 0xFF000000, false);
                y += 10;
            }
        }
        return y;
    }

    private void bar(GuiGraphics g, int x, int y, int w, int h, double ratio, int color) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, UiSkin.OUTLINE);
        g.fill(x, y, x + w, y + h, UiSkin.SURFACE);
        int fw = (int) Math.round(w * Math.max(0, Math.min(1, ratio)));
        if (fw > 0) {
            int c = color & 0xFFFFFF;
            g.fillGradient(x, y, x + fw, y + h, 0xFF000000 | UiSkin.mix(c, 0xFFFFFF, 0.3f), 0xFF000000 | UiSkin.mix(c, 0x000000, 0.3f));
            g.fill(x, y, x + fw, y + 1, 0x55FFFFFF);
        }
        for (int t = 1; t < 10; t++) g.fill(x + w * t / 10, y + 1, x + w * t / 10 + 1, y + h - 1, 0x30000000);
    }

    /** Titre de section : majuscules espacées crème doré, petit filet dessous. */
    private void section(GuiGraphics g, String label, int x, int y) {
        UiSkin.spaced(g, font, label, x, y, UiSkin.SUB);
        int w = 0;
        for (char ch : label.toUpperCase(Locale.ROOT).toCharArray()) w += font.width(String.valueOf(ch)) + 1;
        g.fill(x, y + 10, x + Math.max(24, w), y + 11, UiSkin.WAVE);
    }

    private boolean button(GuiGraphics g, int x, int y, int w, int h, String label, boolean enabled, Runnable run) {
        boolean hover = enabled && mx >= x && mx < x + w && my >= y && my < y + h;
        // bouton : contour noir, bord ambré (or clair au survol), fond en dégradé sombre
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, UiSkin.OUTLINE);
        g.fill(x, y, x + w, y + h, !enabled ? UiSkin.DISABLED : hover ? UiSkin.GOLD : UiSkin.BORDER_DIM);
        g.fillGradient(x + 1, y + 1, x + w - 1, y + h - 1, hover ? UiSkin.HOVER_TOP : UiSkin.BUTTON_TOP, hover ? UiSkin.HOVER_BOTTOM : UiSkin.BUTTON_BOTTOM);
        if (enabled) g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x22FFFFFF);
        String lab = font.plainSubstrByWidth(label, w - 4);
        int tw = font.width(lab);
        g.drawString(font, lab, x + (w - tw) / 2, y + (h - 8) / 2 + 1, !enabled ? UiSkin.DISABLED_TEXT : hover ? UiSkin.HEAD : UiSkin.TEXT, false);
        if (enabled) regions.add(new Region(x, y, w, h, run));
        return hover;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        g.pose().pushPose();
        g.pose().scale(ui, ui, 1f);
        renderHub(g, (int) (mouseX / ui), (int) (mouseY / ui), partial);
        super.render(g, (int) (mouseX / ui), (int) (mouseY / ui), partial); // champs de saisie, à la même échelle
        g.pose().popPose();
        if (!tooltip.isEmpty()) {
            List<Component> lines = new ArrayList<>();
            tooltip.forEach(t -> lines.add(Component.literal(t)));
            g.renderComponentTooltip(font, lines, mouseX, mouseY);
        }
    }

    private void renderHub(GuiGraphics g, int mouseX, int mouseY, float partial) {
        mx = mouseX;
        my = mouseY;
        regions.clear();
        tooltip.clear();
        boxWanted = false;
        hexBox.setVisible(false);
        JsonObject st = ClientHub.get();

        // fenêtre : contour, bord doré à coins lumineux, ciel étoilé assombri
        g.fill(x0 - 2, y0 - 2, x0 + winW + 2, y0 + winH + 2, UiSkin.OUTLINE);
        g.fill(x0 - 1, y0 - 1, x0 + winW + 1, y0 + winH + 1, UiSkin.BORDER);
        UiSkin.stars(g, x0, y0, winW, winH);
        g.fill(x0, y0, x0 + winW, y0 + winH, UiSkin.WINDOW_SHADE);
        g.fill(x0 - 1, y0 - 1, x0 + 2, y0 + 2, UiSkin.GOLD);
        g.fill(x0 + winW - 2, y0 - 1, x0 + winW + 1, y0 + 2, UiSkin.GOLD);
        g.fill(x0 - 1, y0 + winH - 2, x0 + 2, y0 + winH + 1, UiSkin.GOLD);
        g.fill(x0 + winW - 2, y0 + winH - 2, x0 + winW + 1, y0 + winH + 1, UiSkin.GOLD);
        g.fill(x0 + 4, y0 + 21, x0 + winW - 4, y0 + 22, UiSkin.WAVE); // filet sous l'en-tête

        cx = x0 + 8 + DOCK_W + 10;
        cy = y0 + 28;
        cw = winW - (cx - x0) - 8;

        // dock
        int dy = y0 + 28;
        for (int i = 0; i < TABS.length; i++) {
            if (i == 4) dy += GROUP_GAP;
            final int idx = i;
            boolean on = i == tab;
            boolean hover = mx >= x0 + 8 && mx < x0 + 8 + DOCK_W && my >= dy && my < dy + DOCK_H;
            UiSkin.panel(g, x0 + 8, dy, DOCK_W, DOCK_H, on ? UiSkin.BORDER : hover ? UiSkin.GOLD : UiSkin.BORDER_DIM);
            if (on) g.fill(x0 + 6, dy, x0 + 8, dy + DOCK_H, ACCENT);
            g.drawString(font, GLYPH[i], x0 + 8 + (DOCK_W - font.width(GLYPH[i])) / 2, dy + 6, 0xFFFFFFFF, false);
            if (hover) tooltip.add((i + 1) + " - " + TABS[i]);
            regions.add(new Region(x0 + 8, dy, DOCK_W, DOCK_H, () -> switchTab(idx)));
            dy += DOCK_H + DOCK_GAP;
        }

        // en-tête
        UiSkin.spaced(g, font, TABS[tab], cx, y0 + 8, UiSkin.HEAD);
        String hint = "Échap : fermer  .  1-8 : onglets";
        text(g, hint, x0 + winW - 8 - font.width(hint), y0 + winH - 11, UiSkin.MUTED);
        if (!bool(st, "profile")) {
            wrap(g, "Pas de personnage. Ferme ce menu et utilise /unrp menu.", cx, cy, cw, DIM);
            return;
        }
        JsonObject me = obj(st, "me");
        String who = str(me, "name") + "  -  " + TierStyle.label(str(me, "tier")) + " " + str(me, "level");
        text(g, who, x0 + winW - 8 - font.width(who) - 26, y0 + 9, TierStyle.color(str(me, "tier")));
        // cloche
        int unread = num(st, "unread");
        int bx = x0 + winW - 22, by = y0 + 5;
        boolean bh = button(g, bx, by, 16, 14, "!", true, () -> {
            feedOpen = !feedOpen;
            if (!feedOpen) send("read_all", "", "");
        });
        if (unread > 0) g.fill(bx + 11, by - 2, bx + 18, by + 5, 0xFFFF4444);
        if (bh) tooltip.add("Notifications");

        int top = cy;
        // offres reçues
        JsonArray offers = arr(st, "offers");
        for (int i = 0; i < Math.min(2, offers.size()); i++) {
            JsonObject of = offers.get(i).getAsJsonObject();
            panel(g, cx, top, cw, 18);
            text(g, font.plainSubstrByWidth(str(of, "text"), cw - 120), cx + 5, top + 5, ACCENT & 0xFFFFFF);
            final String id = str(of, "id");
            button(g, cx + cw - 112, top + 2, 52, 14, "Accepter", true, () -> send("offer_accept", id, ""));
            button(g, cx + cw - 56, top + 2, 52, 14, "Refuser", true, () -> send("offer_decline", id, ""));
            top += 22;
        }
        cy = top;

        switch (tab) {
            case 0 -> power(g, st);
            case 1 -> abilities(g, st);
            case 2 -> training(g, st);
            case 3 -> style(g);
            case 4 -> school(g, st);
            case 5 -> group(g, st);
            case 6 -> turf(g, st);
            case 7 -> duels(g, st);
            default -> {}
        }
        if (feedOpen) feed(g, st);

        box.setVisible(boxWanted);
        box.setEditable(boxWanted);
    }

    private void switchTab(int i) {
        if (tab == 3 && i != 3) leaveStyle();
        tab = i;
        abilityNode = -1;
        treeScroll = detailScroll = 0;
        selected = "";
        scroll = 0;
        box.setValue("");
        send("open", "", "");
    }

    // ------------------------------------------------------------------ onglets personnels
    private void power(GuiGraphics g, JsonObject st) {
        JsonObject me = obj(st, "me"), pw = obj(st, "power");
        int leftW = cw / 2 - 10, rightX = cx + leftW + 18, rightW = cw - leftW - 18;
        int y = cy;
        section(g, "NIVEAU DE POUVOIR", cx, y);
        g.pose().pushPose();
        g.pose().translate(cx, y + 16, 0);
        g.pose().scale(1.7f, 1.7f, 1);
        g.drawString(font, TierStyle.label(str(me, "tier")) + " " + str(me, "level"), 0, 0,
                TierStyle.color(str(me, "tier")) | 0xFF000000, false);
        g.pose().popPose();
        int mastery = num(pw, "mastery");
        if (pw.has("mastery") && mastery >= 0) {
            text(g, "Maîtrise", cx, y + 40, DIM);
            text(g, mastery + " / 100", cx + leftW - font.width(mastery + " / 100"), y + 40, TXT);
            bar(g, cx, y + 51, leftW, 5, mastery / 100.0, ACCENT);
        } else text(g, "Level de fiche fixe", cx, y + 42, DIM);
        String school = str(me, "school_name"), rank = str(me, "rank");
        if (!school.isEmpty()) text(g, font.plainSubstrByWidth(school + (rank.isEmpty() ? "" : " · " + rankFr(rank)), leftW), cx, y + 62, DIM);
        section(g, "STATISTIQUES", cx, y + 77);
        JsonArray stats = arr(pw, "stats");
        for (int i = 0; i < 5; i++) {
            int value = i < stats.size() ? stats.get(i).getAsInt() : 0;
            int rowY = y + 93 + i * 12;
            text(g, STAT_NAMES[i], cx, rowY, TXT);
            bar(g, cx + 80, rowY, leftW - 103, 6, value / 10.0, UiSkin.GOLD);
            text(g, String.valueOf(value), cx + leftW - 15, rowY, ACCENT);
        }
        section(g, "RÉPUTATION", cx, y + 157);
        boundedWrap(g, str(pw, "standing"), cx, y + 172, leftW, y + 199, TXT);
        section(g, "TA BARRE D'AURA", cx, y + 202);
        if (bool(pw, "has_ability")) {
            int aura = num(pw, "aura"), max = Math.max(1, num(pw, "max_aura"));
            bar(g, cx, y + 217, leftW, 7, aura / (double) max,
                    UnrpClientConfig.COLOR_VALUES[UnrpClientConfig.AURA_COLOR.get()]);
            text(g, aura + " / " + max + "  (+" + num(pw, "regen") + "/s)", cx, y + 229, DIM);
        } else text(g, "Pas de capacité : pas d'aura utilisable.", cx, y + 217, DIM);

        section(g, "TON POUVOIR", rightX, y);
        powerIcon(g, str(pw, "icon"), rightX, y + 17, 20);
        text(g, font.plainSubstrByWidth(bool(pw, "has_ability") ? str(pw, "ability") : "Sans capacité", rightW - 28), rightX + 27, y + 23, UiSkin.HEAD);
        radar(g, stats, rightX + rightW / 2, y + 108, 51);
        int descriptionY = y + 181;
        int descriptionH = Math.max(28, y0 + winH - 22 - descriptionY);
        panel(g, rightX, descriptionY, rightW, descriptionH);
        String description = str(pw, "desc");
        boundedWrap(g, description.isEmpty() ? "Aucune description de pouvoir." : description,
                rightX + 7, descriptionY + 7, rightW - 14, descriptionY + descriptionH - 4, TXT);
    }

    private void radar(GuiGraphics g, JsonArray stats, int x, int y, int radius) {
        double[] values = new double[5];
        for (int i = 0; i < Math.min(5, stats.size()); i++) values[i] = stats.get(i).getAsDouble();
        double[] full = {10, 10, 10, 10, 10};
        List<HubGraphLayout.Point> outer = HubGraphLayout.radar(full, radius);
        polygon(g, outer, x, y, 0x550C0904);
        for (int ring = 1; ring <= 5; ring++) {
            List<HubGraphLayout.Point> points = HubGraphLayout.radar(full, radius * ring / 5f);
            outline(g, points, x, y, ring == 5 ? UiSkin.BORDER : UiSkin.BORDER_DIM);
        }
        for (var point : outer) line(g, x, y, x + Math.round(point.x()), y + Math.round(point.y()), UiSkin.BORDER_DIM);
        List<HubGraphLayout.Point> current = HubGraphLayout.radar(values, radius);
        polygon(g, current, x, y, 0x66F0C846);
        outline(g, current, x, y, UiSkin.GOLD);
        for (int axis = 0; axis < 5; axis++) {
            var point = outer.get(axis);
            var valuePoint = current.get(axis);
            int vx = x + Math.round(valuePoint.x()), vy = y + Math.round(valuePoint.y());
            g.fill(vx - 1, vy - 1, vx + 2, vy + 2, UiSkin.HEAD);
            int stat = HubGraphLayout.RADAR_ORDER[axis];
            int lx = x + Math.round(point.x() * 1.40f), ly = y + Math.round(point.y() * 1.24f);
            String label = STAT_NAMES[stat], value = String.format(Locale.ROOT, "%.1f", values[stat]);
            text(g, label, lx - font.width(label) / 2, ly - 6, DIM);
            text(g, value, lx - font.width(value) / 2, ly + 5, ACCENT);
        }
    }

    private void powerIcon(GuiGraphics g, String icon, int x, int y, int size) {
        var texture = icon == null || icon.isBlank() ? null : net.minecraft.resources.ResourceLocation.tryParse(icon);
        if (texture == null) texture = net.minecraft.resources.ResourceLocation.tryParse(com.myriam.unrp.look.PowerLook.FALLBACK_ICON);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        g.blit(texture, x, y, 0, 0, size, size, size, size);
    }

    private void boundedWrap(GuiGraphics g, String value, int x, int y, int width, int bottom, int color) {
        List<FormattedCharSequence> lines = font.split(Component.literal(value), width);
        for (int i = 0; i < lines.size() && y + 9 <= bottom; i++, y += 11) {
            if (y + 20 > bottom && i + 1 < lines.size()) {
                text(g, "…", x, y, color);
                if (mx >= x && mx < x + width && my >= y - i * 11 && my < bottom) {
                    for (FormattedCharSequence remaining : lines) {
                        StringBuilder line = new StringBuilder();
                        remaining.accept((index, style, codepoint) -> { line.appendCodePoint(codepoint); return true; });
                        tooltip.add(line.toString());
                    }
                }
                break;
            }
            g.drawString(font, lines.get(i), x, y, color, false);
        }
    }

    private static void line(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1, error = dx + dy;
        while (true) {
            g.fill(x0, y0, x0 + 1, y0 + 1, color);
            if (x0 == x1 && y0 == y1) break;
            int twice = 2 * error;
            if (twice >= dy) { error += dy; x0 += sx; }
            if (twice <= dx) { error += dx; y0 += sy; }
        }
    }

    private static void outline(GuiGraphics g, List<HubGraphLayout.Point> points, int x, int y, int color) {
        for (int i = 0; i < points.size(); i++) {
            var a = points.get(i); var b = points.get((i + 1) % points.size());
            line(g, x + Math.round(a.x()), y + Math.round(a.y()), x + Math.round(b.x()), y + Math.round(b.y()), color);
        }
    }

    private static void polygon(GuiGraphics g, List<HubGraphLayout.Point> points, int x, int y, int color) {
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (var p : points) { minY = Math.min(minY, (int) Math.floor(p.y())); maxY = Math.max(maxY, (int) Math.ceil(p.y())); }
        for (int row = minY; row <= maxY; row++) {
            List<Float> crossings = new ArrayList<>();
            for (int i = 0; i < points.size(); i++) {
                var a = points.get(i); var b = points.get((i + 1) % points.size());
                float scan = row + 0.5f;
                if ((a.y() <= scan && b.y() > scan) || (b.y() <= scan && a.y() > scan)) {
                    crossings.add(a.x() + (scan - a.y()) * (b.x() - a.x()) / (b.y() - a.y()));
                }
            }
            crossings.sort(Float::compare);
            for (int i = 0; i + 1 < crossings.size(); i += 2) {
                g.fill(x + (int) Math.ceil(crossings.get(i)), y + row, x + (int) Math.floor(crossings.get(i + 1)) + 1, y + row + 1, color);
            }
        }
    }

    private static String rankFr(String r) {
        return switch (r) {
            case "king" -> "Roi";
            case "queen" -> "Reine";
            case "jack" -> "Valet";
            default -> r;
        };
    }

    private static String kindFr(String k) {
        return switch (k) {
            case "strike" -> "Coup au corps à corps";
            case "beam" -> "Rayon";
            case "blast" -> "Onde autour de soi";
            case "buff" -> "Renforcement";
            case "dash" -> "Élan";
            case "teleport" -> "Téléportation";
            case "heal" -> "Soin";
            case "debuff" -> "Affaiblissement ciblé";
            case "pulse" -> "Zone";
            case "scan" -> "Détection";
            case "dispel" -> "Dissipation";
            default -> k;
        };
    }

    private void abilities(GuiGraphics g, JsonObject st) {
        JsonObject pw = obj(st, "power");
        if (!bool(pw, "has_ability")) {
            wrap(g, "Ce personnage n'a pas de capacité.", cx, cy, cw, DIM);
            return;
        }
        var ps = ClientState.get();
        JsonArray moves = arr(pw, "tree");
        String identity = str(pw, "ability");
        if (!identity.equals(treeIdentity) || abilityNode >= moves.size()) {
            treeIdentity = identity;
            abilityNode = -1;
            treeScroll = detailScroll = 0;
        }
        text(g, font.plainSubstrByWidth(identity, cw - 185), cx, cy + 2, UiSkin.HEAD);
        text(g, "PA " + ps.abilityPts + "   PS " + num(obj(st, "train"), "points"), cx + cw - 98, cy + 2, ACCENT);
        int top = cy + 19, bottom = y0 + winH - 43;
        int treeW = cw * 55 / 100, detailX = cx + treeW + 9, detailW = cw - treeW - 9;
        int height = bottom - top;
        panel(g, cx, top, treeW, height);
        panel(g, detailX, top, detailW, height);
        int[] parents = new int[moves.size()], ranks = new int[moves.size()];
        for (int i = 0; i < moves.size(); i++) {
            JsonObject move = moves.get(i).getAsJsonObject();
            parents[i] = num(move, "requires");
            ranks[i] = num(move, "rank");
        }
        var layout = HubGraphLayout.tree(parents, ranks, treeW - 12);
        treeScrollMax = Math.max(0, layout.height() + 18 - height);
        treeScroll = Math.max(0, Math.min(treeScroll, treeScrollMax));
        int rootX = cx + 6, rootY = bottom - 30 + treeScroll;
        treeViewport = new Region(cx + 2, top + 2, treeW - 4, height - 4, () -> {});
        clip(g, cx + 2, top + 2, treeW - 4, height - 4);
        for (int i = 0; i < moves.size(); i++) {
            JsonObject move = moves.get(i).getAsJsonObject();
            int parent = parents[i] >= 0 && parents[i] < moves.size() && parents[i] != i ? parents[i] + 1 : 0;
            var a = layout.nodes().get(parent); var b = layout.nodes().get(i + 1);
            line(g, rootX + Math.round(a.x()), rootY + Math.round(a.y()), rootX + Math.round(b.x()), rootY + Math.round(b.y()),
                    bool(move, "locked") ? UiSkin.BORDER_DIM : UiSkin.BORDER);
        }
        for (int node = 0; node < layout.nodes().size(); node++) {
            int index = node - 1;
            var point = layout.nodes().get(node);
            int nx = rootX + Math.round(point.x()), ny = rootY + Math.round(point.y());
            JsonObject move = index < 0 ? null : moves.get(index).getAsJsonObject();
            boolean locked = bool(move, "locked"), selectedNode = abilityNode == index;
            List<HubGraphLayout.Point> hex = new ArrayList<>();
            for (int side = 0; side < 6; side++) {
                double angle = side * Math.PI / 3;
                hex.add(new HubGraphLayout.Point((float) Math.cos(angle) * 15, (float) Math.sin(angle) * 15));
            }
            polygon(g, hex, nx, ny, selectedNode ? UiSkin.SELECTED : UiSkin.PANEL_BOTTOM);
            outline(g, hex, nx, ny, selectedNode ? UiSkin.HEAD : locked ? UiSkin.BORDER_DIM : UiSkin.GOLD);
            powerIcon(g, move == null ? str(pw, "icon") : str(move, "icon"), nx - 8, ny - 8, 16);
            if (locked) {
                g.fill(nx - 10, ny - 10, nx + 10, ny + 10, 0x880C0904);
                text(g, "×", nx + 5, ny + 3, UiSkin.SUB);
            }
            if (node == 0) {
                String name = font.plainSubstrByWidth(identity, treeW - 12);
                text(g, name, nx - font.width(name) / 2, ny + 18, UiSkin.HEAD);
            }
            if (ny - 15 >= top + 2 && ny + 15 <= bottom - 2) {
                final int selectedIndex = index;
                regions.add(new Region(nx - 16, ny - 16, 32, 32, () -> { abilityNode = selectedIndex; detailScroll = 0; }));
                if (Math.abs(mx - nx) <= 16 && Math.abs(my - ny) <= 16) {
                    tooltip.add(move == null ? identity : str(move, "name"));
                    tooltip.add(locked ? "Verrouillé · sélectionner pour les conditions" : "Appris · sélectionner pour les détails");
                }
            }
        }
        g.disableScissor();
        if (treeScrollMax > 0) {
            text(g, "Molette : parcourir l'arbre", cx + 7, top + 6, DIM);
            int track = height - 28, thumb = Math.max(12, track * height / (height + treeScrollMax));
            int thumbY = top + 20 + (track - thumb) * (treeScrollMax - treeScroll) / treeScrollMax;
            g.fill(cx + treeW - 5, top + 20, cx + treeW - 3, bottom - 8, UiSkin.BORDER_DIM);
            g.fill(cx + treeW - 5, thumbY, cx + treeW - 3, thumbY + thumb, UiSkin.GOLD);
        }
        if (moves.isEmpty()) boundedWrap(g, "Aucun mouvement défini pour ce pouvoir.", cx + 12, top + 20, treeW - 24, top + 70, DIM);
        abilityDetails(g, moves, pw, detailX, top, detailW, height);

        int buttonW = (cw - 12) / 3;
        for (int rank = 2; rank <= 4; rank++) {
            final int unlockRank = rank;
            boolean owned = (ps.unlockMask & (1 << rank)) != 0;
            button(g, cx + (rank - 2) * (buttonW + 6), bottom + 7, buttonW, 15,
                    owned ? "Rank " + rank + " : acquis" : "Rank " + rank + " : " + (rank - 1) + " AP",
                    !owned && ps.abilityPts >= rank - 1, () -> send("unlock", String.valueOf(unlockRank), ""));
        }
    }

    private void abilityDetails(GuiGraphics g, JsonArray moves, JsonObject pw, int x, int y, int width, int height) {
        JsonObject move = abilityNode < 0 ? null : moves.get(abilityNode).getAsJsonObject();
        var ps = ClientState.get();
        powerIcon(g, move == null ? str(pw, "icon") : str(move, "icon"), x + 8, y + 9, 22);
        String name = move == null ? str(pw, "ability") : str(move, "name");
        text(g, font.plainSubstrByWidth(name, width - 46), x + 38, y + 10, UiSkin.HEAD);
        boolean locked = bool(move, "locked");
        text(g, locked ? "VERROUILLÉ" : "APPRIS", x + 38, y + 23, locked ? UiSkin.SUB : GOOD);
        g.fill(x + 7, y + 39, x + width - 7, y + 40, UiSkin.WAVE);

        int descriptionTop = y + 47, descriptionH = Math.max(22, height - 133);
        String description = move == null ? str(pw, "desc") : str(move, "desc");
        if (description.isEmpty()) description = "Aucune description disponible.";
        List<FormattedCharSequence> lines = font.split(Component.literal(description), width - 18);
        detailScrollMax = Math.max(0, lines.size() * 11 - descriptionH);
        detailScroll = Math.max(0, Math.min(detailScroll, detailScrollMax));
        detailViewport = new Region(x + 7, descriptionTop, width - 14, descriptionH, () -> {});
        clip(g, x + 7, descriptionTop, width - 14, descriptionH);
        for (int i = 0; i < lines.size(); i++) {
            int lineY = descriptionTop + i * 11 - detailScroll;
            if (lineY + 9 >= descriptionTop && lineY < descriptionTop + descriptionH) g.drawString(font, lines.get(i), x + 9, lineY, TXT, false);
        }
        g.disableScissor();
        if (detailScrollMax > 0) text(g, "↕", x + width - 12, descriptionTop, UiSkin.GOLD);
        int infoY = y + height - 78;
        g.fill(x + 7, infoY - 6, x + width - 7, infoY - 5, UiSkin.WAVE);
        if (move == null) {
            text(g, "R: activate / deactivate", x + 9, infoY, DIM);
            text(g, ps.active ? "Power active" : "Power inactive", x + 9, infoY + 14, ps.active ? GOOD : DIM);
            boundedWrap(g, "Select a move to see its requirements and cost.", x + 9, infoY + 32, width - 18, y + height - 5, TXT);
            return;
        }
        String input = switch (str(move, "input")) {
            case "held" -> "Hold";
            case "toggle" -> "Toggle";
            case "charge" -> "Charge and release";
            default -> "Press";
        };
        text(g, "Slot " + num(move, "slot") + " · " + input, x + 9, infoY, DIM);
        text(g, num(move, "cost") > 0 ? num(move, "cost") + " aura" : "No direct cost", x + 9, infoY + 12, ACCENT);
        text(g, "Cooldown: " + String.format(Locale.ROOT, "%.1f s", num(move, "cooldown") / 20.0), x + 9, infoY + 24, DIM);
        String requirement = "Available from the start";
        int rank = num(move, "rank"), parent = num(move, "requires");
        if (rank >= 2) requirement = "Rank " + rank + " · " + (rank - 1) + " AP";
        if (move.has("min_level") && move.get("min_level").getAsDouble() > 0) {
            requirement = "Level " + String.format(Locale.ROOT, "%.1f", move.get("min_level").getAsDouble()) + " required";
        }
        if (bool(move, "mastery") && !bool(move, "mastered")) requirement = "Full mastery required";
        text(g, requirement, x + 9, infoY + 40, locked ? ACCENT : GOOD);
        if (parent >= 0 && parent < moves.size()) {
            String prerequisite = "Requires: " + str(moves.get(parent).getAsJsonObject(), "name");
            text(g, font.plainSubstrByWidth(prerequisite, width - 18), x + 9, infoY + 52, DIM);
            if (mx >= x && mx < x + width && my >= infoY + 50 && my < infoY + 63) tooltip.add(prerequisite);
        }
    }

    private void clip(GuiGraphics g, int x, int y, int width, int height) {
        g.enableScissor((int) Math.floor(x * ui), (int) Math.floor(y * ui),
                (int) Math.ceil((x + width) * ui), (int) Math.ceil((y + height) * ui));
    }

    private void training(GuiGraphics g, JsonObject st) {
        JsonObject tr = obj(st, "train"), pw = obj(st, "power");
        int y = cy;
        int points = num(tr, "points"), total = num(tr, "total"), maxSteps = Math.max(1, num(tr, "max_steps"));
        section(g, "POINTS DE STATS", cx, y);
        y += 15;
        g.pose().pushPose();
        g.pose().translate(cx, y, 0);
        g.pose().scale(2f, 2f, 1f);
        g.drawString(font, String.valueOf(points), 0, 0, (points > 0 ? GOOD : DIM) | 0xFF000000, false);
        g.pose().popPose();
        text(g, "sur " + total + " gagnés : 1 par niveau (dès le niveau 2 ou après 20 min de jeu).", cx + 50, y + 4, DIM & 0xFFFFFF);
        text(g, "Un palier monte le potentiel de +0,5 ; il coûte 1, 2, 3 puis 4 points.", cx + 50, y + 16, DIM & 0xFFFFFF);
        y += 38;
        JsonArray stats = arr(pw, "stats"), steps = arr(tr, "steps"), pots = arr(tr, "pot"), costs = arr(tr, "cost");
        int labelW = 84, barW = Math.min(150, cw - labelW - 230);
        for (int i = 0; i < 5; i++) {
            int v = i < stats.size() ? stats.get(i).getAsInt() : 0;
            int sp = i < steps.size() ? steps.get(i).getAsInt() : 0;
            int pot = i < pots.size() ? pots.get(i).getAsInt() : 0;
            int cost = i < costs.size() ? costs.get(i).getAsInt() : -1;
            text(g, STAT_NAMES[i], cx, y + 3, TXT);
            bar(g, cx + labelW, y + 3, barW, 8, v / 12.0, UiSkin.SUB);
            text(g, v + "   potentiel " + String.format(Locale.ROOT, "%.1f", pot / 10.0) + "   " + sp + "/" + maxSteps, cx + labelW + barW + 6, y + 3, TXT);
            final String idx = String.valueOf(i);
            int bx = cx + labelW + barW + 150;
            button(g, bx, y, 70, 14, cost < 0 ? (sp >= maxSteps ? "Maximum" : "Plafond") : "+0,5 (" + cost + ")", cost >= 0 && points >= cost, () -> send("train", idx, ""));
            button(g, bx + 74, y, 16, 14, "-", sp > 0, () -> send("untrain", idx, ""));
            y += 20;
        }
        y += 6;
        wrap(g, "Le potentiel ne dépasse jamais 10,0 ; 4 paliers au plus par stat (+2,0). Retirer un palier te rend ses points. La stat actuelle suit le niveau : potentiel x racine(niveau / 10).", cx, y, cw, DIM);
    }

    // ------------------------------------------------------------------ Style
    private StyleData draft, saved;
    private int colorTarget;
    private EditBox hexBox;

    private static UUID myId() {
        return Minecraft.getInstance().player.getUUID();
    }

    private void enterStyle() {
        StyleData cur = StyleService.CLIENT_CACHE.get(myId());
        saved = (cur == null ? new StyleData() : cur).copy();
        draft = saved.copy();
        box.setValue(draft.alias);
        hexBox.setValue("");
    }

    private void leaveStyle() {
        if (draft != null) StyleService.CLIENT_CACHE.put(myId(), saved.copy());
        draft = null;
    }

    private int colorOf() {
        return colorTarget == 0 ? draft.auraColor : colorTarget == 1 ? draft.eyeColor : (draft.beamColor < 0 ? draft.auraColor : draft.beamColor);
    }

    private void setColor(int c) {
        switch (colorTarget) {
            case 0 -> draft.auraColor = c;
            case 1 -> draft.eyeColor = c;
            default -> draft.beamColor = c;
        }
    }

    private void stepper(GuiGraphics g, int x, int y, String label, int value, int min, int max, java.util.function.IntConsumer set) {
        text(g, label, x, y + 4, TXT);
        int bx = x + 78;
        button(g, bx, y, 14, 14, "-", value > min, () -> set.accept(value - 1));
        String v = String.valueOf(value);
        text(g, v, bx + 14 + (22 - font.width(v)) / 2, y + 4, TXT);
        button(g, bx + 36, y, 14, 14, "+", value < max, () -> set.accept(value + 1));
    }

    private boolean styleDirty() {
        StyleData a = draft, b = saved;
        return a.auraColor != b.auraColor || a.eyeStyle != b.eyeStyle || a.eyeColor != b.eyeColor || a.eyeW != b.eyeW
                || a.eyeH != b.eyeH || a.eyeGap != b.eyeGap || a.eyeY != b.eyeY || a.beamColor != b.beamColor
                || !a.alias.equals(b.alias);
    }

    private void style(GuiGraphics g) {
        if (draft == null) enterStyle();
        draft.alias = box.getValue().trim();
        StyleService.CLIENT_CACHE.put(myId(), draft); // aperçu en direct sur ton personnage
        int x = cx, y = cy;
        int colW = Math.min(300, cw - 150);

        // couleur
        section(g, "COULEUR DE", x, y);
        String[] targets = {"Aura", "Yeux", "Rayon"};
        for (int i = 0; i < 3; i++) {
            final int t = i;
            boolean on = colorTarget == i;
            button(g, x + 84 + i * 56, y - 3, 52, 14, (on ? "> " : "") + targets[i], true, () -> colorTarget = t);
        }
        y += 16;
        for (int i = 0; i < StyleData.PALETTE.length; i++) {
            int col = StyleData.PALETTE[i], sx = x + i * 17;
            boolean sel = (colorOf() & 0xFFFFFF) == col && !(colorTarget == 2 && draft.beamColor < 0);
            g.fill(sx - 1, y - 1, sx + 15, y + 15, sel ? 0xFFFFFFFF : 0xFF000000);
            g.fill(sx, y, sx + 14, y + 14, 0xFF000000 | col);
            regions.add(new Region(sx, y, 14, 14, () -> setColor(col)));
        }
        y += 19;
        hexBox.setX(x);
        hexBox.setY(y);
        hexBox.setWidth(64);
        hexBox.setVisible(true);
        button(g, x + 70, y, 40, 14, "Hex OK", hexBox.getValue().replace("#", "").matches("[0-9a-fA-F]{6}"), () -> {
            setColor(Integer.parseInt(hexBox.getValue().replace("#", ""), 16));
            hexBox.setValue("");
        });
        if (colorTarget == 2) {
            button(g, x + 116, y, 100, 14, "Comme l'aura", draft.beamColor >= 0, () -> draft.beamColor = -1);
        }
        y += 22;

        // yeux
        section(g, "YEUX", x, y);
        button(g, x + 84, y - 3, 110, 14, "Style : " + StyleData.EYE_STYLES[draft.eyeStyle], true,
                () -> draft.eyeStyle = (draft.eyeStyle + 1) % StyleData.EYE_STYLES.length);
        y += 16;
        stepper(g, x, y, "Largeur", draft.eyeW, 1, 4, v -> draft.eyeW = v);
        stepper(g, x + 140, y, "Hauteur", draft.eyeH, 1, 4, v -> draft.eyeH = v);
        y += 17;
        stepper(g, x, y, "Écart", draft.eyeGap, 2, 6, v -> draft.eyeGap = v);
        stepper(g, x + 140, y, "Position", draft.eyeY, 0, 6, v -> draft.eyeY = v);
        y += 22;

        // alias
        section(g, "Alias", x, y);
        text(g, "affiché au-dessus de ta tête et dans le chat", x + 52, y, UiSkin.MUTED);
        y += 15;
        boxAt(x, y, 170);
        y += 22;

        // affichage
        section(g, "AFFICHAGE", x, y);
        y += 15;
        int bw = (colW - 6) / 2;
        button(g, x, y, bw, 14, "HUD : " + (UnrpClientConfig.HUD_VISIBLE.get() ? "affiché" : "masqué"), true, () -> {
            UnrpClientConfig.HUD_VISIBLE.set(!UnrpClientConfig.HUD_VISIBLE.get());
            UnrpClientConfig.SPEC.save();
        });
        button(g, x + bw + 6, y, bw, 14, "Taille : " + UnrpClientConfig.SCALES[UnrpClientConfig.HUD_SCALE.get()], true, () -> {
            UnrpClientConfig.HUD_SCALE.set((UnrpClientConfig.HUD_SCALE.get() + 1) % UnrpClientConfig.SCALES.length);
            UnrpClientConfig.SPEC.save();
        });
        y += 17;
        button(g, x, y, bw, 14, "Coin : " + UnrpClientConfig.CORNERS[UnrpClientConfig.HUD_CORNER.get()], true, () -> {
            UnrpClientConfig.HUD_CORNER.set((UnrpClientConfig.HUD_CORNER.get() + 1) % UnrpClientConfig.CORNERS.length);
            UnrpClientConfig.SPEC.save();
        });
        button(g, x + bw + 6, y, bw, 14, "Sons : " + (UnrpClientConfig.HUB_SOUNDS.get() ? "oui" : "non"), true, () -> {
            UnrpClientConfig.HUB_SOUNDS.set(!UnrpClientConfig.HUB_SOUNDS.get());
            UnrpClientConfig.SPEC.save();
        });
        y += 17;
        button(g, x, y, colW, 14, "Barre d'aura du HUD : " + UnrpClientConfig.COLOR_NAMES[UnrpClientConfig.AURA_COLOR.get()], true, () -> {
            UnrpClientConfig.AURA_COLOR.set((UnrpClientConfig.AURA_COLOR.get() + 1) % UnrpClientConfig.COLOR_VALUES.length);
            UnrpClientConfig.SPEC.save();
        });

        // aperçu
        int px = x + colW + 24, pw = cw - colW - 24;
        if (pw > 80) {
            int pcx = px + pw / 2, py = cy + 150;
            panel(g, px, cy, pw, 188);
            section(g, "APERÇU", px + 6, cy + 5);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                InventoryScreen.renderEntityInInventoryFollowsMouse(g, pcx, py, 56, pcx - mx, py - 90 - my, mc.player);
            }
            g.fill(px + 6, cy + 160, px + pw - 6, cy + 166, 0xFF000000 | (draft.auraColor & 0xFFFFFF));
            text(g, "aura", px + 6, cy + 170, DIM & 0xFFFFFF);
            boolean dirty = styleDirty();
            text(g, dirty ? "Non enregistré" : "Enregistré", px, cy + 194, dirty ? ACCENT & 0xFFFFFF : GOOD & 0xFFFFFF);
            button(g, px, cy + 206, pw / 2 - 2, 16, "Appliquer", dirty, () -> {
                draft.alias = box.getValue().trim();
                Net.CHANNEL.sendToServer(new StyleSavePacket(draft.copy()));
                saved = draft.copy();
            });
            button(g, px + pw / 2 + 2, cy + 206, pw / 2 - 2, 16, "Réinitialiser", dirty, () -> {
                draft = saved.copy();
                box.setValue(draft.alias);
            });
        }
    }

    // ------------------------------------------------------------------ onglets sociaux
    private void school(GuiGraphics g, JsonObject st) {
        JsonObject so = obj(st, "school"), me = obj(st, "me");
        int leftW = Math.min(300, cw * 3 / 5);
        int y = cy;
        if (so == null) {
            text(g, "PAS INSCRIT", cx, y, BAD & 0xFFFFFF);
            wrap(g, "Ton personnage n'a pas d'école. Parle à un principal sur un campus pour t'inscrire.", cx, y + 14, leftW, DIM);
        } else {
            String myRank = str(me, "rank");
            boolean king = "king".equals(myRank);
            text(g, str(so, "name").toUpperCase(Locale.ROOT), cx, y, TXT);
            y += 14;
            for (JsonElement el : arr(so, "ranks")) {
                JsonObject r = el.getAsJsonObject();
                panel(g, cx, y, leftW, 22);
                text(g, str(r, "label"), cx + 6, y + 7, ACCENT & 0xFFFFFF);
                text(g, font.plainSubstrByWidth(str(r, "holder"), leftW - 130), cx + 60, y + 7, TXT);
                if (bool(r, "online")) g.fill(cx + leftW - 70, y + 9, cx + leftW - 65, y + 14, GOOD);
                final String rk = str(r, "rank");
                if (king && bool(r, "granted") && !"king".equals(rk)) {
                    button(g, cx + leftW - 62, y + 4, 58, 14, "Retirer", true, () -> send("school_revoke", rk, ""));
                }
                y += 25;
            }
            if (!king) {
                y += 2;
                boolean has = !"Trône vide".equals(rankHolder(so, "king"));
                button(g, cx, y, 150, 16, "Défier le Roi", has, () -> send("duel_formal", "", ""));
                if (!has) tooltip.add("Cette école n'a pas de Roi pour répondre.");
                y += 20;
                wrap(g, "Pour la couronne, un contre un. Le combat s'arrête à 2 cœurs.", cx, y, leftW, DIM);
            } else {
                text(g, "ÉLÈVES  (clique pour donner un siège)", cx, y + 2, DIM & 0xFFFFFF);
                y += 14;
                JsonArray stu = arr(so, "students");
                int rows = Math.max(1, (cy + (winH - 60) - y) / 13);
                scroll = Math.max(0, Math.min(scroll, Math.max(0, stu.size() - rows)));
                for (int i = scroll; i < stu.size() && i < scroll + rows; i++) {
                    JsonObject s = stu.get(i).getAsJsonObject();
                    final String id = str(s, "id");
                    boolean sel = id.equals(selected);
                    g.fill(cx, y, cx + leftW, y + 12, sel ? UiSkin.SELECTED : UiSkin.PANEL_TOP);
                    text(g, str(s, "name") + (str(s, "rank").isEmpty() ? "" : " (" + str(s, "rank") + ")"), cx + 4, y + 2, TXT);
                    if (bool(s, "online")) g.fill(cx + leftW - 10, y + 4, cx + leftW - 5, y + 9, GOOD);
                    regions.add(new Region(cx, y, leftW, 12, () -> selected = id));
                    y += 13;
                }
                y += 4;
                boolean ok = !selected.isEmpty();
                button(g, cx, y, 72, 14, "Faire Reine", ok, () -> send("school_promote", selected, "queen"));
                button(g, cx + 76, y, 72, 14, "Faire Valet", ok, () -> send("school_promote", selected, "jack"));
            }
        }
        // classement des écoles
        int rx = cx + leftW + 12, rw = cw - leftW - 12;
        if (rw > 110) {
            int ry = cy;
            section(g, "ÉCOLES", rx, ry);
            ry += 15;
            for (JsonElement el : arr(st, "schools")) {
                if (ry > y0 + winH - 40) break;
                JsonObject s = el.getAsJsonObject();
                panel(g, rx, ry, rw, 34);
                text(g, font.plainSubstrByWidth(str(s, "name"), rw - 8), rx + 4, ry + 3, TXT);
                text(g, font.plainSubstrByWidth("Roi : " + str(s, "king"), rw - 8), rx + 4, ry + 14, UiSkin.SUB);
                text(g, font.plainSubstrByWidth(str(s, "queen") + " / " + str(s, "jack"), rw - 8), rx + 4, ry + 24, DIM & 0xFFFFFF);
                ry += 37;
            }
        }
    }

    private static String rankHolder(JsonObject so, String rank) {
        for (JsonElement el : arr(so, "ranks")) {
            JsonObject r = el.getAsJsonObject();
            if (rank.equals(str(r, "rank"))) return str(r, "holder");
        }
        return "";
    }

    private void nearList(GuiGraphics g, JsonObject st, int x, int y, int w, String verb, boolean enabled, java.util.function.Consumer<String> act, int maxDist) {
        JsonArray near = arr(st, "near");
        int shown = 0;
        for (JsonElement el : near) {
            JsonObject n = el.getAsJsonObject();
            if (num(n, "dist") > maxDist) continue;
            if (shown++ >= 6) break;
            panel(g, x, y, w, 18);
            text(g, font.plainSubstrByWidth(str(n, "name"), w - 130), x + 5, y + 5, TXT);
            text(g, num(n, "dist") + " m", x + w - 124, y + 5, DIM & 0xFFFFFF);
            final String id = str(n, "id");
            button(g, x + w - 86, y + 2, 82, 14, verb, enabled, () -> act.accept(id));
            y += 21;
        }
        if (shown == 0) wrap(g, maxDist >= 64 ? "Personne à moins de 64 blocs." : "Personne à moins de " + maxDist + " blocs.", x, y, w, DIM);
    }

    private void group(GuiGraphics g, JsonObject st) {
        JsonObject gr = obj(st, "group");
        int y = cy, w = Math.min(cw, 380);
        if (gr == null) {
            section(g, "CRÉER UN GROUPE", cx, y);
            y += 14;
            boxAt(cx, y, 170);
            boolean ok = box.getValue().trim().length() >= 2;
            button(g, cx + 178, y - 1, 70, 16, "Créer", ok, () -> {
                send("group_create", box.getValue().trim(), "");
                box.setValue("");
            });
            y += 24;
            wrap(g, "Ce que tu dis dans le chat reste visible de tous ; le groupe sert à s'organiser et à être invité par ses camarades.", cx, y, w, DIM);
            y += 34;
            section(g, "À PROXIMITÉ", cx, y);
            y += 15;
            wrap(g, "Crée un groupe pour pouvoir inviter.", cx, y, w, DIM);
            return;
        }
        boolean owner = bool(gr, "owner");
        text(g, str(gr, "name").toUpperCase(Locale.ROOT), cx, y, TXT);
        button(g, cx + w - 100, y - 3, 100, 14, owner ? "Dissoudre" : "Quitter", true,
                () -> send(owner ? "group_disband" : "group_leave", "", ""));
        y += 16;
        section(g, "MEMBRES", cx, y);
        y += 15;
        for (JsonElement el : arr(gr, "members")) {
            JsonObject m = el.getAsJsonObject();
            panel(g, cx, y, w, 16);
            text(g, str(m, "name") + (bool(m, "leader") ? "  (chef)" : ""), cx + 5, y + 4, TXT);
            if (bool(m, "online")) g.fill(cx + w - 96, y + 5, cx + w - 91, y + 10, GOOD);
            final String id = str(m, "id");
            if (owner && !bool(m, "leader")) button(g, cx + w - 86, y + 1, 82, 14, "Retirer", true, () -> send("group_kick", id, ""));
            y += 19;
        }
        y += 6;
        section(g, "INVITER UN CAMARADE", cx, y);
        y += 15;
        nearList(g, st, cx, y, w, "Inviter", owner, id -> send("group_invite", id, ""), 64);
    }

    private void boxAt(int x, int y, int w) {
        box.setX(x);
        box.setY(y);
        box.setWidth(w);
        boxWanted = true;
    }

    private void turf(GuiGraphics g, JsonObject st) {
        JsonObject me = obj(st, "me"), here = obj(st, "here");
        boolean king = "king".equals(str(me, "rank"));
        int y = cy, w = Math.min(cw, 420);
        section(g, "TU ES SUR", cx, y);
        y += 15;
        if (here != null) {
            panel(g, cx, y, w, 34);
            text(g, str(here, "name"), cx + 6, y + 5, TXT);
            text(g, "Tenu par : " + str(here, "school"), cx + 6, y + 19, UiSkin.SUB);
            final String id = str(here, "id");
            boolean can = king && !bool(here, "mine");
            button(g, cx + w - 130, y + 9, 124, 16, "Déclarer la guerre", can, () -> send("turf_war", id, ""));
            if (!can && king) tooltip.add("Ton école le tient déjà.");
            y += 40;
            if (!king) {
                wrap(g, "Seul un Roi peut déclarer une guerre.", cx, y, w, DIM);
                y += 14;
            }
        } else {
            wrap(g, "Aucun territoire ici.", cx, y, w, DIM);
            y += 14;
            if (king) {
                boxAt(cx, y, 170);
                button(g, cx + 178, y - 1, 120, 16, "Revendiquer ici", box.getValue().trim().length() >= 2, () -> {
                    send("turf_claim", box.getValue().trim(), "");
                    box.setValue("");
                });
                y += 22;
            } else {
                wrap(g, "Seul un Roi peut revendiquer du terrain.", cx, y, w, DIM);
                y += 14;
            }
        }
        y += 6;
        section(g, "TERRITOIRES", cx, y);
        y += 15;
        JsonArray turfs = arr(st, "turfs");
        if (turfs.size() == 0) wrap(g, "Aucun territoire n'a encore été revendiqué.", cx, y, w, DIM);
        for (JsonElement el : turfs) {
            if (y > y0 + winH - 34) break;
            JsonObject t = el.getAsJsonObject();
            panel(g, cx, y, w, 16);
            text(g, str(t, "name"), cx + 5, y + 4, bool(t, "mine") ? GOOD & 0xFFFFFF : TXT);
            String info = str(t, "school") + "  (" + num(t, "x") + ", " + num(t, "z") + ")";
            text(g, info, cx + w - 5 - font.width(info), y + 4, UiSkin.SUB);
            y += 19;
        }
    }

    private void duels(GuiGraphics g, JsonObject st) {
        JsonObject du = obj(st, "duel"), me = obj(st, "me");
        int y = cy, w = Math.min(cw, 420);
        if (du != null) {
            section(g, "TON DUEL", cx, y);
            y += 15;
            panel(g, cx, y, w, 40);
            String kind = switch (str(du, "kind")) {
                case "formal" -> "Combat pour la couronne";
                case "turf" -> "Guerre de territoire";
                default -> "Duel amical";
            };
            text(g, kind, cx + 6, y + 6, ACCENT & 0xFFFFFF);
            text(g, "Contre " + str(du, "opp") + " - s'arrête à 2 cœurs", cx + 6, y + 21, TXT);
            button(g, cx + w - 80, y + 12, 74, 16, "Abandonner", true, () -> send("duel_yield", "", ""));
            return;
        }
        section(g, "DUEL AMICAL", cx, y);
        y += 15;
        wrap(g, "Un contre un, sans enjeu. Il s'arrête à 2 cœurs.", cx, y, w, DIM);
        y += 14;
        nearList(g, st, cx, y, w, "Défier", true, id -> send("duel_friendly", id, ""), 14);
        y += 6 * 21 + 8;
        if (y > y0 + winH - 60) y = y0 + winH - 60;
        section(g, "COMBAT FORMEL", cx, y);
        y += 15;
        boolean enrolled = !str(me, "school").isEmpty() && !"king".equals(str(me, "rank"));
        button(g, cx, y, 150, 16, "Défier le Roi", enrolled, () -> send("duel_formal", "", ""));
        wrap(g, enrolled ? "Pour la couronne, un contre un." : "Il faut être élève d'une école, sans en être le Roi.", cx + 158, y + 4, w - 158, DIM);
    }

    private void feed(GuiGraphics g, JsonObject st) {
        int w = Math.min(220, cw - 20), x = x0 + winW - w - 8, y = y0 + 24, h = winH - 50;
        panel(g, x, y, w, h);
        g.fill(x - 1, y - 1, x + w + 1, y, EDGE);
        section(g, "NOTIFICATIONS", x + 6, y + 5);
        button(g, x + w - 76, y + 2, 72, 12, "Tout lu", true, () -> send("read_all", "", ""));
        int ty = y + 20;
        JsonArray feed = arr(st, "feed");
        if (feed.size() == 0) text(g, "Rien de nouveau.", x + 6, ty, DIM & 0xFFFFFF);
        for (JsonElement el : feed) {
            if (ty > y + h - 12) break;
            JsonObject n = el.getAsJsonObject();
            int before = ty;
            ty = wrap(g, str(n, "text"), x + 8, ty, w - 14, bool(n, "read") ? UiSkin.MUTED : 0xFFFFFF);
            if (!bool(n, "read")) g.fill(x + 3, before + 1, x + 6, before + 6, ACCENT);
            ty += 3;
        }
    }

    // ------------------------------------------------------------------ entrées
    @Override
    public boolean mouseClicked(double rawX, double rawY, int button) {
        double mouseX = rawX / ui, mouseY = rawY / ui;
        if (button == 0) {
            for (int i = regions.size() - 1; i >= 0; i--) {
                Region r = regions.get(i);
                if (r.in(mouseX, mouseY)) {
                    if (UnrpClientConfig.HUB_SOUNDS.get()) {
                        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    }
                    r.run().run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double rawX, double rawY, int button) {
        return super.mouseReleased(rawX / ui, rawY / ui, button);
    }

    @Override
    public boolean mouseDragged(double rawX, double rawY, int button, double dx, double dy) {
        return super.mouseDragged(rawX / ui, rawY / ui, button, dx / ui, dy / ui);
    }

    @Override
    public boolean mouseScrolled(double rawX, double rawY, double delta) {
        if (tab == 1 && !feedOpen) {
            double x = rawX / ui, y = rawY / ui;
            if (treeViewport != null && treeViewport.in(x, y)) {
                treeScroll = Math.max(0, Math.min(treeScrollMax, treeScroll + (int) Math.round(delta * 24)));
                return true;
            }
            if (detailViewport != null && detailViewport.in(x, y)) {
                detailScroll = Math.max(0, Math.min(detailScrollMax, detailScroll - (int) Math.round(delta * 22)));
                return true;
            }
        }
        scroll = Math.max(0, scroll - (int) Math.signum(delta));
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (!(box != null && box.isFocused() && box.visible) && !(hexBox != null && hexBox.isFocused() && hexBox.visible)) {
            if (key >= 49 && key <= 56) {
                switchTab(key - 49);
                return true;
            }
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void onClose() {
        leaveStyle();
        if (feedOpen) send("read_all", "", "");
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
