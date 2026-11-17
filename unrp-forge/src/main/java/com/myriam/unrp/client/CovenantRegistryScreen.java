package com.myriam.unrp.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.myriam.unrp.ability.CovenantLawChoices;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Private, server-supplied law book. Every mutation is checked again on the server. */
public final class CovenantRegistryScreen extends Screen {
    private final JsonArray courts, proposals;
    private final List<CovenantLawChoices.Penalty> penalties;
    private final double level;
    private int court, page, selected = -1, action, penalty, left, top, widthPanel, heightPanel, pageSize;
    private boolean showingProposals;
    private EditBox input;
    private String draft = "";

    public CovenantRegistryScreen(JsonObject data) {
        super(Component.literal("Covenant — Law Registry"));
        courts = data.getAsJsonArray("courts");
        proposals = data.getAsJsonArray("proposals");
        level = data.get("level").getAsDouble();
        penalties = CovenantLawChoices.available(level);
    }

    public void restoreSelection(CovenantRegistryScreen previous) {
        if (previous.currentCourt() != null) {
            String id = previous.currentCourt().get("id").getAsString();
            for (int i = 0; i < courts.size(); i++)
                if (courts.get(i).getAsJsonObject().get("id").getAsString().equals(id)) court = i;
        }
        showingProposals = previous.showingProposals;
        page = previous.page;
        selected = previous.selected;
        if (selected >= rows().size()) selected = -1;
        if (selected >= 0) draft = showingProposals ? rows().get(selected).getAsString()
                : rows().get(selected).getAsJsonObject().get("text").getAsString();
    }

    private JsonObject currentCourt() {
        return courts.isEmpty() ? null : courts.get(court).getAsJsonObject();
    }

    private JsonArray rows() {
        return showingProposals ? proposals : currentCourt() == null ? new JsonArray()
                : currentCourt().getAsJsonArray("laws");
    }

    private JsonObject currentLaw() {
        return !showingProposals && selected >= 0 && selected < rows().size()
                ? rows().get(selected).getAsJsonObject() : null;
    }

    private void button(String text, int x, int y, int w, Runnable run, boolean active) {
        Button button = Button.builder(Component.literal(text), b -> run.run())
                .bounds(x, y, w, 20).build();
        button.active = active;
        addRenderableWidget(button);
    }

    private void send(String command) {
        if (minecraft.player != null) minecraft.player.connection.sendCommand("unrp covenant " + command);
    }

    private void mutate(String operation) {
        JsonObject law = currentLaw();
        if (law != null) send("lawbook " + currentCourt().get("id").getAsString() + " "
                + law.get("id").getAsInt() + " " + operation);
    }

    private void chooseText() {
        draft = CovenantLawChoices.text(CovenantLawChoices.ACTIONS.get(action), penalties.get(penalty));
        input.setValue(draft);
    }

    @Override
    protected void init() {
        clearWidgets();
        widthPanel = Math.min(580, width - 12);
        heightPanel = Math.min(360, height - 12);
        left = (width - widthPanel) / 2;
        top = (height - heightPanel) / 2;
        pageSize = Math.max(1, (heightPanel - 218) / 22);
        int pages = Math.max(1, (rows().size() + pageSize - 1) / pageSize);
        page = Math.min(page, pages - 1);
        button(showingProposals ? "Back to laws" : "Proposals (not enforced)", left + widthPanel - 196, top + 8, 186, () -> {
            showingProposals = !showingProposals;
            selected = -1;
            page = 0;
            draft = "";
            init();
        }, true);
        String courtName = currentCourt() == null ? "No active Court" : currentCourt().get("name").getAsString();
        button(showingProposals ? "Saved ideas — implementation required" : "Court: " + courtName,
                left + 10, top + 36, widthPanel - 100, () -> {
                    court = (court + 1) % courts.size();
                    page = 0;
                    selected = -1;
                    draft = "";
                    init();
                }, !showingProposals && courts.size() > 1);
        button((page + 1) + "/" + pages + " >", left + widthPanel - 84, top + 36, 74, () -> {
            page = (page + 1) % pages;
            init();
        }, pages > 1);
        for (int row = 0; row < pageSize && page * pageSize + row < rows().size(); row++) {
            int index = page * pageSize + row;
            String text;
            if (showingProposals) text = rows().get(index).getAsString();
            else {
                JsonObject law = rows().get(index).getAsJsonObject();
                text = "#" + law.get("id").getAsInt() + (law.get("hidden").getAsBoolean() ? " [Hidden] " : " [Public] ")
                        + law.get("text").getAsString();
            }
            String label = (index == selected ? "> " : "") + font.plainSubstrByWidth(text, widthPanel - 42);
            button(label, left + 10, top + 64 + row * 22, widthPanel - 20, () -> {
                selected = index;
                draft = showingProposals ? rows().get(index).getAsString()
                        : rows().get(index).getAsJsonObject().get("text").getAsString();
                init();
            }, true);
        }
        int editor = top + 68 + pageSize * 22;
        input = new EditBox(font, left + 10, editor, widthPanel - 20, 20, Component.literal("Law text or proposal"));
        input.setMaxLength(512);
        input.setValue(draft);
        input.setResponder(value -> draft = value);
        addRenderableWidget(input);
        if (!showingProposals) {
            int half = (widthPanel - 24) / 2;
            button("Action: " + CovenantLawChoices.ACTIONS.get(action).label(), left + 10, editor + 26, half, () -> {
                action = (action + 1) % CovenantLawChoices.ACTIONS.size();
                chooseText();
                init();
            }, !penalties.isEmpty());
            button("Penalty: " + (penalties.isEmpty() ? "None" : penalties.get(penalty).label()), left + 14 + half, editor + 26, half, () -> {
                penalty = (penalty + 1) % penalties.size();
                chooseText();
                init();
            }, !penalties.isEmpty());
        }
        int part = (widthPanel - 32) / 4;
        JsonObject law = currentLaw();
        button("Save edit (10 aura)", left + 10, editor + 54, part, () -> mutate("edit " + draft), law != null);
        button(law != null && law.get("hidden").getAsBoolean() ? "Reveal" : "Hide", left + 14 + part, editor + 54, part,
                () -> mutate("visibility"), law != null && level >= 2 && !currentCourt().get("constitution").getAsBoolean());
        button("Delete...", left + 18 + part * 2, editor + 54, part, () -> minecraft.setScreen(new ConfirmScreen(answer -> {
            if (answer) {
                if (showingProposals) send("discard_proposal " + selected);
                else mutate("delete");
            }
            minecraft.setScreen(this);
        }, Component.literal("Delete this entry?"), Component.literal(showingProposals ? "This removes your saved note." : "Other Courts keep their own copies."))), selected >= 0);
        button("Save proposal", left + 22 + part * 3, editor + 54, part, () -> send("proposal " + draft), true);
        button("Close", left + widthPanel - 80, top + heightPanel - 28, 70, this::onClose, true);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        UiSkin.panel(graphics, left, top, widthPanel, heightPanel, UiSkin.GOLD);
        graphics.drawString(font, "Covenant — Laws", left + 10, top + 13, UiSkin.HEAD, false);
        String hint = showingProposals ? "Proposals are private notes. They do not bind anyone or trigger penalties."
                : "Select a law to edit it. Changes apply only to this Court. Use the choices for supported rules.";
        graphics.drawWordWrap(font, Component.literal(hint), left + 10, top + 148 + pageSize * 22, widthPanel - 20, UiSkin.TEXT);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
