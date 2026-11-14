package com.myriam.unrp.deal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public class StyledButton extends Button {
   public StyledButton(int x, int y, int w, int h, Component message, Button.OnPress onPress) {
      super(x, y, w, h, message, onPress, DEFAULT_NARRATION);
   }

   public void onPress() {
      Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(DealBridge.sound("unrp:button_click"), 1.0F, 0.7F));
      super.onPress();
   }

   public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      int x0 = this.getX();
      int y0 = this.getY();
      int x1 = x0 + this.width;
      int y1 = y0 + this.height;
      boolean hov = this.isHoveredOrFocused();
      g.fill(x0, y0, x1, y1, hov ? -1875234987 : 1612256037);
      int border = hov ? -4609816 : -11122056;
      g.fill(x0, y0, x1, y0 + 1, border);
      g.fill(x0, y1 - 1, x1, y1, border);
      g.fill(x0, y0, x0 + 1, y1, border);
      g.fill(x1 - 1, y0, x1, y1, border);
      int tc = this.active ? (hov ? -988929 : -1514252) : -7831392;
      g.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), (x0 + x1) / 2, y0 + (this.height - 8) / 2, tc);
   }
}
