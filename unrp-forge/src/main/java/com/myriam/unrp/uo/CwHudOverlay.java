package com.myriam.unrp.uo;

import com.myriam.unrp.look.UnrpHudOverlay;
import com.myriam.unrp.pass.UnrpPowerLayers;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class CwHudOverlay implements IGuiOverlay {
   public static final CwHudOverlay INSTANCE = new CwHudOverlay();
   private static final String[] STAGE_NAME = new String[]{"", "Ballast", "Recoil", "Fulcrum", "Crucible", "Apex Lever", "Equilibrium"};
   private static final int[] MOMENTUM_AT = new int[]{0, 0, 150, 400, 800, 1300, 2000};
   private static final int AMBER = -23252;
   private static final int AMBER_HI = -11638;
   private static final int SHOCK = -1378561;
   private static final int STEEL = -4668208;
   private static final int STEEL_D = -10590092;
   private static final int BACK = -1072819946;

   public void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
      if (CwHudState.fresh()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && !mc.options.hideGui) {
            if (UnrpPowerLayers.powerVariant(mc.player, "counterweight") != 0) {
               int cap = Math.max(1, CwHudState.cap());
               int charge = Mth.clamp(CwHudState.charge(), 0, cap);
               int barW = 104;
               int barH = 8;
               int x;
               int y;
               if (UnrpHudOverlay.panelShown()) {
                  x = Math.round(UnrpHudOverlay.panelX(width)) + 4;
                  y = Math.round(UnrpHudOverlay.panelY(height) + UnrpHudOverlay.panelHeight()) + 14;
               } else {
                  x = 8;
                  y = height / 2;
               }

               if (y + barH + 26 > height) {
                  y = height - barH - 28;
               }

               boolean full = charge >= cap;
               boolean flash = CwHudState.flashing();
               g.fill(x - 2, y - 2, x + barW + 2, y + barH + 2, -1072819946);
               g.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, full ? -11638 : -10590092);
               g.fill(x, y, x + barW, y + barH, -535553502);
               int w = charge * barW / cap;
               g.fill(x, y, x + w, y + barH, -23252);
               g.fill(x, y, x + w, y + 2, -11638);
               if (flash) {
                  g.fill(x, y, x + w, y + barH, 1627389951);
               }

               for(int seg = 1; seg < 4; ++seg) {
                  int sx = x + barW * seg / 4;
                  g.fill(sx, y, sx + 1, y + barH, 1343099158);
               }

               int fy = y + barH + 3;
               int frame = CwHudState.frame();

               for(int i = 0; i < 8; ++i) {
                  int px = x + i * 13;
                  boolean perfect = i < 2;
                  boolean lit = frame > 0 && i < frame;
                  int col = lit ? (perfect ? -1378561 : -11638) : (perfect ? -12958126 : -14011844);
                  g.fill(px, fy, px + 11, fy + 3, col);
               }

               String label = "VAULT " + charge + "/" + cap;
               if (full) {
                  label = label + " §6FULL";
               }

               g.drawString(mc.font, label, x, fy + 6, full ? -11638 : -23252, true);
               int stage = Mth.clamp(CwHudState.stage(), 1, 6);
               String right = STAGE_NAME[stage];
               if (stage < 6) {
                  right = right + " §8" + CwHudState.momentum() + "/" + MOMENTUM_AT[stage + 1];
               }

               g.drawString(mc.font, right, x, fy + 16, -4668208, true);
               if (CwHudState.chained()) {
                  String chain = "§6CHAIN x" + CwHudState.links();
                  g.drawString(mc.font, chain, x + barW - mc.font.width(chain) + 2, y - 11, -11638, true);
               } else if (CwHudState.held()) {
                  String guard = "§7GUARD";
                  g.drawString(mc.font, guard, x + barW - mc.font.width(guard) + 2, y - 11, -4668208, true);
               }

            }
         }
      }
   }
}
