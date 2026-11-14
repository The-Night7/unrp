package com.myriam.unrp.uo;



import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class StruggleHudOverlay implements IGuiOverlay {
   public static final StruggleHudOverlay INSTANCE = new StruggleHudOverlay();
   public static final int KIND_GRAVITY = 4;
   public static final int KIND_LASH = 5;
   public static final int KIND_SALT = 6;
   public static final int KIND_CHAIN = 7;
   private static final int MOON = 14477554;
   private static final int INDIGO = 4867488;
   private static final int ABYSS = 2304580;
   private static final int SLATE = 6188677;
   private static float shown;
   private static long lastFrame;

   public void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
      if (!StruggleClientState.active()) {
         shown = 0.0F;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && !mc.options.hideGui) {
            int progress = StruggleClientState.progress();
            int kind = StruggleClientState.kind();
            if (kind == 4) {
               clash(g, mc, width, height, progress);
            } else {
               int fill = kind == 0 ? -2182560 : (kind == 1 ? -10504449 : (kind == 2 ? -2689793 : (kind == 5 ? -10885390 : (kind == 6 ? -724502 : (kind == 7 ? -6005505 : -1)))));
               int dim = kind == 0 ? -9743832 : (kind == 1 ? -14793370 : (kind == 2 ? -10847608 : (kind == 5 ? -14992316 : (kind == 6 ? -8748400 : (kind == 7 ? -13757360 : -10066330)))));
               long t = System.currentTimeMillis() - StruggleClientState.shownAt();
               float pulse = (float)(Math.sin((double)t * 0.02D) * 0.5D + 0.5D);
               int barW = 102;
               int barH = 7;
               int x = (width - barW) / 2;
               int y = height - 57;
               int jitter = progress > 55 ? (int)(Math.sin((double)t * 0.09D) * 1.6D) : 0;
               x += jitter;
               int border = -16777216 | mixColor(2105376, fill & 16777215, pulse * 0.6F);
               g.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, border);
               g.fill(x, y, x + barW, y + barH, -535818224);
               int w = Mth.clamp(progress, 0, 100) * (barW - 2) / 100;
               g.fill(x + 1, y + 1, x + 1 + w, y + barH - 1, -16777216 | dim & 16777215);
               int hi = Math.max(0, w - 1);
               g.fill(x + 1, y + 1, x + 1 + hi, y + 2 + (barH - 3) / 2, -16777216 | fill & 16777215);
               String label = "BREAK FREE";
               int tw = mc.font.width(label);
               int textColor = -16777216 | mixColor(16777215, fill & 16777215, 0.5F + pulse * 0.5F);
               g.drawString(mc.font, label, (width - tw) / 2 + jitter, y - 11, textColor, true);
            }
         }
      }
   }

   private static void clash(GuiGraphics g, Minecraft mc, int width, int height, int progress) {
      long nowNs = System.nanoTime();
      float dt = lastFrame == 0L ? 0.0F : Math.min(0.1F, (float)(nowNs - lastFrame) / 1.0E9F);
      lastFrame = nowNs;
      float target = (float)Mth.clamp(progress, 0, 100) / 100.0F;
      shown += (target - shown) * Math.min(1.0F, dt * 14.0F);
      long t = System.currentTimeMillis() % 3600000L;
      int barW = 150;
      int barH = 9;
      int x = (width - barW) / 2;
      int y = height - 57;
      float strain = 0.5F + 0.5F * (float)Math.sin((double)t * 0.031D);
      int shake = shown > 0.75F ? (int)(Math.sin((double)t * 0.11D) * (double)1.8F) : 0;
      x += shake;
      int edge = -16777216 | mixColor(2304580, 14477554, 0.25F + strain * 0.35F);
      g.fill(x - 2, y - 2, x + barW + 2, y + barH + 2, -1073214958);
      g.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, edge);
      g.fill(x, y, x + barW, y + barH, -267381728);
      int inner = barW - 2;
      int seam = x + 1 + Math.round((float)inner * shown) + (int)Math.round(Math.sin((double)t * 0.07D) * 1.2D);
      seam = Mth.clamp(seam, x + 1, x + 1 + inner);
      g.fill(x + 1, y + 1, seam, y + barH - 1, -16777216 | mixColor(6188677, 14477554, 0.55F));
      g.fill(x + 1, y + 1, seam, y + 3, -2299662);
      int hold = -16777216 | mixColor(2304580, 4867488, 0.6F + 0.4F * strain);
      g.fill(seam, y + 1, x + 1 + inner, y + barH - 1, hold);
      int room = x + 1 + inner - seam;

      for(int i = 0; room > 2 && i < 6; ++i) {
         int band = (int)((t / 18L + (long)i * 23L) % (long)room);
         int bx = x + inner - band;
         if (bx > seam + 1) {
            g.fill(bx, y + 1, bx + 1, y + barH - 1, 1610612736 | mixColor(4867488, 14477554, 0.35F));
         }
      }

      int glow = -16777216 | mixColor(14477554, 16777215, strain);
      g.fill(seam - 1, y - 2, seam + 1, y + barH + 2, glow);

      for(int i = 0; i < 5; ++i) {
         double a = (double)t * 0.013D + (double)i * 1.7D;
         int sx = seam + (int)Math.round(Math.cos(a * 3.1D) * (2.0D + 3.0D * (double)strain));
         int sy = y + barH / 2 + (int)Math.round(Math.sin(a * 2.3D) * ((double)barH * 0.5D + 2.0D));
         g.fill(sx, sy, sx + 1, sy + 1, -16777216 | (i % 2 == 0 ? 14477554 : 8025520));
      }

      String label = "BREAK FREE";
      int textColor = -16777216 | mixColor(16777215, 14477554, 0.5F + strain * 0.5F);
      g.drawString(mc.font, label, x, y - 11, textColor, true);
      String held = "HELD";
      g.drawString(mc.font, held, x + barW - mc.font.width(held), y - 11, -16777216 | mixColor(4867488, 14477554, 0.4F + 0.25F * strain), true);
   }

   private static int mixColor(int a, int b, float f) {
      f = Mth.clamp(f, 0.0F, 1.0F);
      int r = (int)((float)(a >> 16 & 255) + (float)((b >> 16 & 255) - (a >> 16 & 255)) * f);
      int gr = (int)((float)(a >> 8 & 255) + (float)((b >> 8 & 255) - (a >> 8 & 255)) * f);
      int bl = (int)((float)(a & 255) + (float)((b & 255) - (a & 255)) * f);
      return r << 16 | gr << 8 | bl;
   }
}
