package com.myriam.unrp.deal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class PactHudOverlay implements IGuiOverlay {
   public static final PactHudOverlay INSTANCE = new PactHudOverlay();
   private static final String[] SEAT = new String[]{"L", "M", "G"};

   public void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
      if (PactHudState.fresh()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && !mc.options.hideGui) {
            if (DealBridge.powerVariant(mc.player, "dealmaker") != 0) {
               int x = width - 118;
               int y = 40;
               boolean any = false;

               for(int i = 0; i < 3; ++i) {
                  if (!PactHudState.name(i).isEmpty()) {
                     any = true;
                  }
               }

               if (any || !PactHudState.info().isEmpty()) {
                  g.fill(x - 4, y - 4, x + 112, y + 12 + this.rowCount() * 11, -1878325242);
                  g.drawString(mc.font, "§6§lBOUND", x, y, 16777215, true);
                  int row = 0;

                  for(int i = 0; i < 3; ++i) {
                     String name = PactHudState.name(i);
                     int rowY_0 = y + 11 + row * 11;
                     String line;
                     if (name.isEmpty()) {
                        line = "§8" + SEAT[i] + " (empty seat)";
                     } else {
                        drawFace(g, mc, name, x, rowY_0, PactHudState.gone(i));
                        if (PactHudState.gone(i)) {
                           line = "§8" + SEAT[i] + " " + name + " §8(gone)";
                        } else {
                           int hp = PactHudState.hp(i);
                           String color = hp > 60 ? "§a" : (hp > 25 ? "§e" : "§c");
                           line = "§6" + SEAT[i] + " §f" + name + " " + color + hp + "%" + (PactHudState.warded(i) ? " §b*" : "") + (PactHudState.spent(i) ? " §8ash" : "");
                        }
                     }

                     g.drawString(mc.font, line, x + (name.isEmpty() ? 0 : 10), rowY_0, 16777215, true);
                     ++row;
                  }

                  for(String extra : PactHudState.info()) {
                     if (row >= 8) {
                        break;
                     }

                     int rowY = y + 11 + row * 11;
                     String bare = extra.replaceAll("§.", "").trim();
                     String who = bare.contains(" ") ? bare.substring(0, bare.indexOf(32)) : bare;
                     if (!who.isEmpty()) {
                        drawFace(g, mc, who, x, rowY, extra.startsWith("§8"));
                     }

                     g.drawString(mc.font, extra, x + (who.isEmpty() ? 0 : 10), rowY, 16777215, true);
                     ++row;
                  }

               }
            }
         }
      }
   }

   private static void drawFace(GuiGraphics g, Minecraft mc, String name, int x, int y, boolean gone) {
      ResourceLocation skin = null;
      ClientPacketListener conn = mc.getConnection();
      if (conn != null) {
         PlayerInfo info = conn.getPlayerInfo(name);
         if (info != null) {
            skin = info.getSkinLocation();
         }
      }

      if (skin == null) {
         skin = DefaultPlayerSkin.getDefaultSkin();
      }

      PlayerFaceRenderer.draw(g, skin, x, y, 8);
      if (gone) {
         g.fill(x, y, x + 8, y + 8, -1270857656);
      }

   }

   private int rowCount() {
      return 3 + Math.min(5, PactHudState.info().size());
   }
}
