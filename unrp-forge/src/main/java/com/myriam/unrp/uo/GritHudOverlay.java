package com.myriam.unrp.uo;
import com.myriam.unrp.look.UnrpHudOverlay;

import com.myriam.unrp.pass.UnrpPowerLayers;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class GritHudOverlay implements IGuiOverlay {
   public static final GritHudOverlay INSTANCE = new GritHudOverlay();
   private static final String[] FORM_NAME = new String[]{"", "SURGE", "RADIANCE", "AZURE", "SECOND NATURE"};
   private static final int[] FORM_COLOR = new int[]{-3618616, -46532, -11697, -10903297, -1380363};

   public void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
      if (GritHudState.fresh()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && !mc.options.hideGui) {
            if (UnrpPowerLayers.powerVariant(mc.player, "limit_breaker") != 0) {
               int cap = Math.max(1, GritHudState.cap());
               int grit = Mth.clamp(GritHudState.grit(), 0, cap);
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

               if (y + barH + 44 > height) {
                  y = height - barH - 46;
               }

               int form = Mth.clamp(GritHudState.form(), 0, 4);
               int accent = FORM_COLOR[form == 0 ? Mth.clamp(GritHudState.tier(), 1, 4) : form];
               g.fill(x - 2, y - 2, x + barW + 2, y + barH + 2, -1073084926);
               g.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, form > 0 ? -16777216 | accent & 16777215 : -10073810);
               g.fill(x, y, x + barW, y + barH, -535294460);
               int w = grit * barW / cap;
               g.fill(x, y, x + w, y + barH, -5211100);
               g.fill(x, y, x + w, y + 2, -11697);

               for(int seg = 1; seg < 4; ++seg) {
                  int sx_0 = x + barW * seg / 4;
                  g.fill(sx_0, y, sx_0 + 1, y + barH, 1611269634);
               }

               String label = "GRIT " + GritHudState.grit() + "/" + cap;
               g.drawString(mc.font, label, x, y + barH + 3, -10080, true);
               int kiCap = Math.max(1, GritHudState.kiCap());
               int ki = Mth.clamp(GritHudState.ki(), 0, kiCap);
               int ky = y + barH + 14;
               int kiH = 5;
               g.fill(x - 2, ky - 2, x + barW + 2, ky + kiH + 2, -1073608180);
               g.fill(x - 1, ky - 1, x + barW + 1, ky + kiH + 1, -14792108);
               g.fill(x, ky, x + barW, ky + kiH, -536603624);
               int kw = ki * barW / kiCap;
               g.fill(x, ky, x + kw, ky + kiH, -12601140);
               g.fill(x, ky, x + kw, ky + 1, -4656390);

               for(int seg = 1; seg < 4; ++seg) {
                  int sx = x + barW * seg / 4;
                  g.fill(sx, ky, sx + 1, ky + kiH, 1610746380);
               }

               g.drawString(mc.font, "KI " + GritHudState.ki() + "/" + kiCap, x, ky + kiH + 3, -5707534, true);
               String line = "LV " + GritHudState.level() + " §8" + GritHudState.xp() + "/" + GritHudState.xpNext() + "xp§6  EDGE +" + GritHudState.edge() + "%";
               if (form > 0) {
                  line = FORM_NAME[form] + "§6  EDGE +" + GritHudState.edge() + "%";
               }

               int lv = GritHudState.level();
               if (GritHudState.tier() >= 4 && lv > 45) {
                  line = line + "  §f+ASC " + (lv - 45) / 5;
               }

               g.drawString(mc.font, line, x, ky + kiH + 13, form > 0 ? accent : -3627928, true);
            }
         }
      }
   }
}
