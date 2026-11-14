package com.myriam.unrp.uo;

import com.myriam.unrp.look.UnrpHudOverlay;

import com.myriam.unrp.pass.UnrpPowerLayers;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class SandHudOverlay implements IGuiOverlay {
   public static final SandHudOverlay INSTANCE = new SandHudOverlay();

   public void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
      if (SandHudState.fresh()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && !mc.options.hideGui) {
            if (UnrpPowerLayers.powerVariant(mc.player, "sarcophage") != 0) {
               int cap = Math.max(1, SandHudState.cap());
               int reserve = Mth.clamp(SandHudState.reserve(), 0, cap);
               int barW = 96;
               int barH = 7;
               int x;
               int y;
               if (UnrpHudOverlay.panelShown()) {
                  x = Math.round(UnrpHudOverlay.panelX(width)) + 4;
                  y = Math.round(UnrpHudOverlay.panelY(height) + UnrpHudOverlay.panelHeight()) + 14;
               } else {
                  x = 8;
                  y = height / 2;
               }

               if (y + barH + 4 > height) {
                  y = height - barH - 6;
               }

               g.fill(x - 2, y - 2, x + barW + 2, y + barH + 2, -1072427514);
               g.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, -9743832);
               g.fill(x, y, x + barW, y + barH, -534768120);
               int w = reserve * barW / cap;
               g.fill(x, y, x + w, y + barH, -5208758);
               g.fill(x, y, x + w, y + 2, -2182560);

               for(int seg = 1; seg < 4; ++seg) {
                  int sx = x + barW * seg / 4;
                  g.fill(sx, y, sx + 1, y + barH, 1611927046);
               }

               String label = "SAND " + reserve + "/" + cap;
               g.drawString(mc.font, label, x, y + barH + 3, -2182560, true);
               String tails = "TAILS " + SandHudState.tails() + (SandHudState.severed() ? "§c (severed)" : "") + "  §7VITAL " + SandHudState.lifetime();
               g.drawString(mc.font, tails, x, y + barH + 13, -5208758, true);
            }
         }
      }
   }
}
