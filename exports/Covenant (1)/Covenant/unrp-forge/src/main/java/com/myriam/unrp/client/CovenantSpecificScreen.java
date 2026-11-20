package com.myriam.unrp.client;

import com.myriam.unrp.ability.CovenantSpecificRules;
import com.myriam.unrp.ability.CovenantSpecificRules.Kind;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CovenantSpecificScreen extends Screen {
    private static final int PANEL_HEIGHT = 212;

    private final Screen parent;
    private Kind selectedRule = Kind.SIGNED_CONTRACTS;
    private String target = "*";
    private Button enactButton;
    private int left;
    private int top;
    private int panelWidth;

    public CovenantSpecificScreen(Screen parent) {
        super(Component.literal("Covenant — Specific Rules"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelWidth = Math.min(390, width - 16);
        left = (width - panelWidth) / 2;
        top = (height - PANEL_HEIGHT) / 2;

        addRuleButton(Kind.SIGNED_CONTRACTS, "Transfer signed contracts", 34);
        addRuleButton(Kind.HOME_REPORT, "Report a visitor's home", 58);

        var targetInput = new EditBox(
            font, left + 10, top + 98, panelWidth - 20, 20, Component.literal("Player name or *"));
        targetInput.setMaxLength(16);
        targetInput.setValue(target);
        targetInput.setEditable(selectedRule == Kind.HOME_REPORT);
        targetInput.setResponder(value -> {
            target = value;
            enactButton.active = validSelection();
        });
        addRenderableWidget(targetInput);

        int buttonWidth = (panelWidth - 24) / 2;
        enactButton = addRenderableWidget(Button.builder(Component.literal("Enact"), button -> enact())
                .bounds(left + 10, top + 182, buttonWidth, 20)
                .build());
        enactButton.active = validSelection();
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose())
                .bounds(left + 14 + buttonWidth, top + 182, buttonWidth, 20)
                .build());
    }

    private void addRuleButton(Kind rule, String label, int offsetY) {
        String text = (selectedRule == rule ? "> " : "") + label;
        addRenderableWidget(Button
                .builder(Component.literal(text),
                    button -> {
                        selectedRule = rule;
                        rebuildWidgets();
                    })
                .bounds(left + 10, top + offsetY, panelWidth - 20, 20)
                .build());
    }

    private boolean validSelection() {
        return selectedRule == Kind.SIGNED_CONTRACTS || CovenantSpecificRules.validTarget(target);
    }

    private void enact() {
        if (minecraft.player == null || !validSelection()) {
            return;
        }
        String subject = selectedRule == Kind.HOME_REPORT ? target : "*";
        var rule = new CovenantSpecificRules.Rule(selectedRule, subject);
        minecraft.player.connection.sendCommand("unrp covenant law " + rule.text());
        minecraft.setScreen(null);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        UiSkin.panel(graphics, left, top, panelWidth, PANEL_HEIGHT, UiSkin.GOLD);
        graphics.drawString(font, title, left + 10, top + 12, UiSkin.HEAD, false);
        graphics.drawString(font, "Player name (* = any visitor)", left + 10, top + 85, UiSkin.SUB, false);

        String description = switch (selectedRule) {
            case SIGNED_CONTRACTS ->
                "Every signed original contract carried inside the Court is transferred to your inventory. "
                    + "Full inventory: transfer waits.";
            case HOME_REPORT ->
                "When the selected player enters, receive a book containing their name and saved home "
                    + "coordinates.";
        };
        graphics.drawWordWrap(
            font, Component.literal(description), left + 10, top + 128, panelWidth - 20, UiSkin.TEXT);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
