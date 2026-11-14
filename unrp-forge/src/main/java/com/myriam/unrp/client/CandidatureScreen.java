package com.myriam.unrp.client;

import com.myriam.unrp.net.ClaimRequestPacket;
import com.myriam.unrp.net.CreateOcPacket;
import com.myriam.unrp.net.MenuDataPacket;
import com.myriam.unrp.net.Net;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Menu de candidature : onglets Lore / RP / OC. Le client n'affiche que ce que le serveur lui envoie
 * et ne fait que des demandes ; le serveur valide tout (personnage libre, nom, tirage du potentiel).
 */
public class CandidatureScreen extends Screen {
    private static final int ROW = 20, TOP = 62, PAD = 16, TAB_LORE = 0, TAB_RP = 1, TAB_OC = 2;

    private MenuDataPacket data;
    private int tab;
    private final List<MenuDataPacket.Entry> view = new ArrayList<>();
    private String selectedId;
    private int scroll;
    private String ocNameText = "";
    private int schoolIdx;
    private EditBox nameBox;
    private Button applyBtn;

    public CandidatureScreen(MenuDataPacket data) {
        super(Component.literal("Choisis ton personnage"));
        this.data = data;
        this.tab = data.entries.stream().anyMatch(e -> e.category.equals("rp") && !e.claimed) ? TAB_RP : TAB_LORE;
        computeView();
    }

    // ------------------------------------------------------------ données
    private void computeView() {
        view.clear();
        String cat = tab == TAB_LORE ? "lore" : "rp";
        for (MenuDataPacket.Entry e : data.entries) {
            if (e.category.equals(cat)) view.add(e);
        }
    }

    private MenuDataPacket.Entry selected() {
        if (selectedId == null) return null;
        for (MenuDataPacket.Entry e : view) {
            if (e.id.equals(selectedId)) return e;
        }
        return null;
    }

    private boolean canApply() {
        MenuDataPacket.Entry e = selected();
        return e != null && !e.claimed && !data.hasProfile;
    }

    /** Appelé quand le serveur renvoie des données (un autre joueur a pris un personnage, message d'erreur...). */
    public void update(MenuDataPacket p) {
        this.data = p;
        computeView();
        if (schoolIdx >= data.schools.size()) schoolIdx = 0;
        if (applyBtn != null) applyBtn.active = canApply();
    }

    // ------------------------------------------------------------ mise en page
    private int listRight() {
        return width * 2 / 5;
    }

    private int rightX() {
        return listRight() + PAD;
    }

    private int rightW() {
        return width - rightX() - PAD;
    }

    private int visibleRows() {
        return Math.max(1, (height - 50 - TOP) / ROW);
    }

    @Override
    protected void init() {
        applyBtn = null;
        nameBox = null;
        addRenderableWidget(Button.builder(Component.literal("Lore"), b -> setTab(TAB_LORE)).bounds(PAD, 30, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("RP"), b -> setTab(TAB_RP)).bounds(PAD + 74, 30, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Créer un OC"), b -> setTab(TAB_OC)).bounds(PAD + 148, 30, 110, 20).build());

        int rx = rightX();
        if (tab == TAB_OC) {
            nameBox = new EditBox(font, rx, TOP + 14, rightW(), 18, Component.literal("Nom"));
            nameBox.setMaxLength(24);
            nameBox.setValue(ocNameText);
            nameBox.setResponder(s -> ocNameText = s);
            addRenderableWidget(nameBox);
            addRenderableWidget(Button.builder(schoolLabel(), b -> {
                if (!data.schools.isEmpty()) schoolIdx = (schoolIdx + 1) % data.schools.size();
                b.setMessage(schoolLabel());
            }).bounds(rx, TOP + 48, rightW(), 20).build());
            Button submit = addRenderableWidget(Button.builder(Component.literal("Valider et tirer mon potentiel"),
                    b -> submitOc()).bounds(rx, height - 40, rightW(), 20).build());
            submit.active = !data.hasProfile && !data.schools.isEmpty();
        } else {
            applyBtn = addRenderableWidget(Button.builder(Component.literal("Postuler pour ce personnage"),
                    b -> apply()).bounds(rx, height - 40, rightW(), 20).build());
            applyBtn.active = canApply();
        }
    }

    private Component schoolLabel() {
        if (data.schools.isEmpty()) return Component.literal("École : aucune disponible");
        return Component.literal("École : " + data.schools.get(schoolIdx).name + "  (clic = suivante)");
    }

    private void setTab(int t) {
        tab = t;
        selectedId = null;
        scroll = 0;
        computeView();
        rebuildWidgets();
    }

    // ------------------------------------------------------------ actions
    private void apply() {
        MenuDataPacket.Entry e = selected();
        if (e != null && canApply()) Net.CHANNEL.sendToServer(new ClaimRequestPacket(e.id));
    }

    private void submitOc() {
        if (data.hasProfile || data.schools.isEmpty()) return;
        Net.CHANNEL.sendToServer(new CreateOcPacket(ocNameText, data.schools.get(schoolIdx).id));
    }

    // ------------------------------------------------------------ souris
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (tab != TAB_OC && button == 0 && mx >= PAD && mx < listRight() && my >= TOP) {
            int row = (int) ((my - TOP) / ROW);
            int idx = scroll + row;
            if (row < visibleRows() && idx >= 0 && idx < view.size()) {
                selectedId = view.get(idx).id;
                if (applyBtn != null) applyBtn.active = canApply();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (tab == TAB_OC) return super.mouseScrolled(mx, my, delta);
        int max = Math.max(0, view.size() - visibleRows());
        scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(delta)));
        return true;
    }

    // ------------------------------------------------------------ rendu
    private static int tierColor(String tier) {
        return TierStyle.color(tier);
    }

    private static String tierLabel(String tier) {
        return TierStyle.label(tier);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        g.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        if (tab != TAB_OC) renderList(g, mx, my);
        renderDetail(g);
        super.render(g, mx, my, partial);
        if (data.message != null && !data.message.isEmpty()) {
            g.drawCenteredString(font, data.message, width / 2, height - 14, 0xFFD27F);
        }
    }

    private void renderList(GuiGraphics g, int mx, int my) {
        int rows = visibleRows();
        g.fill(PAD, TOP, listRight(), TOP + rows * ROW, 0x66000000);
        if (view.isEmpty()) {
            g.drawString(font, "Aucun personnage.", PAD + 6, TOP + 6, 0xAAAAAA);
            return;
        }
        for (int i = 0; i < rows; i++) {
            int idx = scroll + i;
            if (idx >= view.size()) break;
            MenuDataPacket.Entry e = view.get(idx);
            int y = TOP + i * ROW;
            boolean hover = mx >= PAD && mx < listRight() && my >= y && my < y + ROW;
            if (e.id.equals(selectedId)) g.fill(PAD, y, listRight(), y + ROW, 0x55FFFFFF);
            else if (hover) g.fill(PAD, y, listRight(), y + ROW, 0x33FFFFFF);
            int color = e.claimed ? 0x666666 : 0xFFFFFF;
            g.drawString(font, e.name + (e.claimed ? "  (pris)" : ""), PAD + 6, y + 6, color);
            String t = tierLabel(e.tier);
            g.drawString(font, t, listRight() - 6 - font.width(t), y + 6, e.claimed ? 0x666666 : tierColor(e.tier));
        }
        if (view.size() > rows) {
            g.drawString(font, (scroll + 1) + "-" + Math.min(view.size(), scroll + rows) + " / " + view.size(),
                    PAD, TOP + rows * ROW + 4, 0xAAAAAA);
        }
    }

    private void renderDetail(GuiGraphics g) {
        int rx = rightX(), rw = rightW(), bottom = height - 48;
        g.fill(rx - 6, TOP, width - PAD + 6, bottom, 0x66000000);
        int y = TOP + 4;
        if (tab == TAB_OC) {
            g.drawString(font, "Nom du personnage", rx, TOP + 2, 0xFFFFFF);
            g.drawString(font, "École", rx, TOP + 38, 0xFFFFFF);
            y = TOP + 78;
            y = wrap(g, "Ton potentiel est tiré au hasard par le serveur, une seule fois. Il est définitif.", rx, y, rw, 0xDDDDDD, bottom);
            y += 6;
            g.drawString(font, "Probabilités :", rx, y, 0xFFFFFF);
            y += 12;
            for (MenuDataPacket.RarityInfo r : data.rarities) {
                if (y > bottom - 10) break;
                String pct = r.percent < 1 ? String.format(Locale.ROOT, "%.2f", r.percent) : String.format(Locale.ROOT, "%.1f", r.percent);
                g.drawString(font, r.label + " : " + pct + " %", rx + 6, y, 0xBBBBBB);
                y += 10;
            }
            return;
        }
        MenuDataPacket.Entry e = selected();
        if (e == null) {
            wrap(g, "Choisis un personnage dans la liste.", rx, y, rw, 0xAAAAAA, bottom);
            return;
        }
        g.drawString(font, e.name, rx, y, 0xFFFFFF);
        y += 14;
        g.drawString(font, tierLabel(e.tier) + " - niveau " + e.level, rx, y, tierColor(e.tier));
        y += 14;
        y = wrap(g, "Capacité : " + e.ability, rx, y, rw, 0xFFFFFF, bottom);
        if (!e.schools.isEmpty()) y = wrap(g, "École : " + e.schools, rx, y + 2, rw, 0xDDDDDD, bottom);
        y += 6;
        if (!e.abilityDesc.isEmpty()) y = wrap(g, e.abilityDesc, rx, y, rw, 0xAAAAAA, bottom);
        y += 6;
        if (e.claimed) wrap(g, "Déjà pris par un joueur.", rx, y, rw, 0xFF7777, bottom);
        else if (data.hasProfile) wrap(g, "Tu as déjà un personnage.", rx, y, rw, 0xFFD27F, bottom);
    }

    private int wrap(GuiGraphics g, String text, int x, int y, int w, int color, int bottom) {
        for (FormattedCharSequence line : font.split(Component.literal(text), w)) {
            if (y > bottom - 10) break;
            g.drawString(font, line, x, y, color);
            y += 10;
        }
        return y;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
