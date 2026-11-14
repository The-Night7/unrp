package com.myriam.unrp.uo;

import com.myriam.unrp.look.StatClientCache;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class WeaveHudOverlay implements IGuiOverlay {
   public static final WeaveHudOverlay INSTANCE = new WeaveHudOverlay();
   private static final ResourceLocation MOTE = new ResourceLocation("unrp", "textures/particle/aura_mote_0.png");
   private static final int CELLS = 10;
   private static final int CELL_SIZE = 8;
   private static final int CELL_STEP = 10;

   public void render(ForgeGui gui, GuiGraphics g, float partialTick, int screenWidth, int screenHeight) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer p = mc.player;
      if (p != null) {
         if (!mc.options.hideGui) {
            if (!mc.options.renderDebug) {
               if (!p.isSpectator()) {
                  int flags = StatClientCache.weaveFlags;
                  if ((flags & 1) != 0) {
                     int max = StatClientCache.motesMax;
                     if (max > 0) {
                        int motes = Math.max(0, Math.min(max, StatClientCache.motes));
                        int color = AuraColorResolver.forViewer(15160448);
                        float r = (float)(color >> 16 & 255) / 255.0F;
                        float gc = (float)(color >> 8 & 255) / 255.0F;
                        float b = (float)(color & 255) / 255.0F;
                        int x = screenWidth - 8 - 6;
                        int yBottom = screenHeight / 2 + 50 - 10;
                        float fill = (float)motes / (float)max * 10.0F;

                        for(int i = 0; i < 10; ++i) {
                           float cellFrac = Math.max(0.0F, Math.min(1.0F, fill - (float)i));
                           float alpha = cellFrac >= 1.0F ? 1.0F : (cellFrac > 0.0F ? 0.35F + 0.5F * cellFrac : 0.12F);
                           g.setColor(r, gc, b, alpha);
                           g.blit(MOTE, x, yBottom - i * 10, 0.0F, 0.0F, 8, 8, 8, 8);
                        }

                        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                        String label = motes + "/" + max;
                        int tw = mc.font.width(label);
                        g.drawString(mc.font, label, x - tw - 4, screenHeight / 2 - 4, -16777216 | color, true);
                        String title = (flags & 2) != 0 ? "Swarm: Architect" : "Swarm";
                        int tt = mc.font.width(title);
                        g.drawString(mc.font, title, x - tt - 4, screenHeight / 2 - 16, -1513240, true);
                        if (WeaveInputHandler.weaveHeld()) {
                           String reachLabel = "Reach " + WeaveInputHandler.clientReach();
                           int rw = mc.font.width(reachLabel);
                           g.drawString(mc.font, reachLabel, x - rw - 4, screenHeight / 2 + 8, -4331265, true);
                        }

                     }
                  }
               }
            }
         }
      }
   }
}
