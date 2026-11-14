package com.myriam.unrp.deal;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class LeilahScreen extends Screen {
   private final String speaker;
   private final String fullText;
   private final boolean question;
   private final int org;
   private int shown = 0;
   private int doneTicks = 0;
   private boolean answered = false;
   private Button yesButton;
   private Button noButton;
   private Button closeButton;
   private int panelH = 96;

   public LeilahScreen(String speaker, String text, boolean question, int org) {
      super(Component.literal(speaker));
      this.speaker = speaker;
      this.fullText = text;
      this.question = question;
      this.org = org;
   }

   protected void init() {
      int panelW = Math.min(340, this.width - 40);
      int fullLines = this.font.split(Component.literal(this.fullText), panelW - 16).size();
      this.panelH = Math.min(this.height - 60, 26 + fullLines * 10 + 36);
      int panelX = (this.width - panelW) / 2;
      int panelBottom = this.height - 24;
      int btnY = panelBottom - 26;
      if (this.question) {
         this.yesButton = Button.builder(Component.literal("Yes"), (b) -> this.choose(true)).bounds(panelX + panelW / 2 - 84, btnY, 80, 20).build();
         this.noButton = Button.builder(Component.literal("No"), (b) -> this.choose(false)).bounds(panelX + panelW / 2 + 4, btnY, 80, 20).build();
         this.yesButton.visible = false;
         this.noButton.visible = false;
         this.addRenderableWidget(this.yesButton);
         this.addRenderableWidget(this.noButton);
      } else {
         this.closeButton = Button.builder(Component.literal("Close"), (b) -> this.onClose()).bounds(panelX + panelW / 2 - 40, btnY, 80, 20).build();
         this.closeButton.visible = false;
         this.addRenderableWidget(this.closeButton);
      }

   }

   private void choose(boolean yes) {
      if (!this.answered) {
         this.answered = true;
         com.myriam.unrp.net.Net.CHANNEL.sendToServer(new DialogueChoicePacket(yes, this.org));
         this.onClose();
      }
   }

   public void tick() {
      if (this.shown < this.fullText.length()) {
         this.shown = Math.min(this.fullText.length(), this.shown + 2);
         if (this.shown >= this.fullText.length()) {
            this.doneTicks = 0;
         }
      } else {
         ++this.doneTicks;
         if (this.question) {
            if (this.yesButton != null && !this.answered) {
               this.yesButton.visible = true;
               this.noButton.visible = true;
            }
         } else {
            if (this.closeButton != null) {
               this.closeButton.visible = true;
            }

            if (this.doneTicks > 70) {
               this.onClose();
            }
         }
      }

   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      int panelW = Math.min(340, this.width - 40);
      int panelX = (this.width - panelW) / 2;
      int panelY = this.height - 24 - this.panelH;
      boolean ember = this.org == 2;
      g.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + this.panelH + 2, ember ? -34258 : -7701505);
      g.fill(panelX, panelY, panelX + panelW, panelY + this.panelH, ember ? -401077758 : -401602014);
      String header = (ember ? "§6§l" : "§d§l") + this.speaker;
      g.drawString(this.font, Component.literal(header), panelX + 8, panelY + 6, 16777215);
      String visible = this.fullText.substring(0, Math.min(this.shown, this.fullText.length()));
      List<FormattedCharSequence> lines = this.font.split(Component.literal(visible), panelW - 16);
      int ty = panelY + 20;

      for(FormattedCharSequence line : lines) {
         if (ty > panelY + this.panelH - 36) {
            break;
         }

         g.drawString(this.font, line, panelX + 8, ty, 15461365);
         ty += 10;
      }

      super.render(g, mouseX, mouseY, partialTick);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.shown < this.fullText.length()) {
         this.shown = this.fullText.length();
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
