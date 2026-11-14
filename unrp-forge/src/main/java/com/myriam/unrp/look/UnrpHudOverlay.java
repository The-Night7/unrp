package com.myriam.unrp.look;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Quaternionf;

public class UnrpHudOverlay implements IGuiOverlay {
   public static final UnrpHudOverlay INSTANCE = new UnrpHudOverlay();
   private static final ResourceLocation BACK = new ResourceLocation("unrp", "textures/gui/hud/rpg_hud_back.png");
   private static final ResourceLocation FRONT = new ResourceLocation("unrp", "textures/gui/hud/rpg_hud_front.png");
   private static final ResourceLocation WHITE = new ResourceLocation("unrp", "textures/gui/ability_bars/white.png");
   private static boolean headBroken;
   private static final int HUD_X = 8;
   private static final int HUD_Y = 8;
   private static final int HUD_W = 192;
   private static final int HUD_H = 124;
   private static final int PNG_W = 200;
   private static final int PNG_H = 80;
   private static final int HP_FILL = -4708826;
   private static final int HP_GHOST = -12969445;
   private static final int AURA_FROM = -6928676;
   private static final int AURA_TO = -10827536;
   private static final int AURA_GHOST = -14269872;
   private static final int AM_AURA_FROM = -15461352;
   private static final int AM_AURA_TO = -9145218;
   private static final int HUNGER_FILL = -2714072;
   private static final int HUNGER_GHOST = -12965100;
   private static final int ARMOR_FILL = -5920332;
   private static final int ARMOR_GHOST = -13684683;

   public void render(ForgeGui gui, GuiGraphics g, float partialTick, int screenWidth, int screenHeight) {
      if (!Minecraft.getInstance().options.renderDebug) {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer p = mc.player;
         if (p != null) {
            if (!mc.options.hideGui) {
               if (!p.isSpectator()) {
                  if (StatClientCache.hudMode == 0) {
                     float hs = ClientHudConfig.hudScale();
                     int anchor = ClientHudConfig.hudAnchor();
                     float tx = anchor != 1 && anchor != 3 ? (float)ClientHudConfig.hudOffX() : (float)screenWidth - 192.0F * hs - (float)ClientHudConfig.hudOffX();
                     float ty = anchor != 2 && anchor != 3 ? (float)ClientHudConfig.hudOffY() : (float)screenHeight - 124.0F * hs - (float)ClientHudConfig.hudOffY();
                     g.pose().pushPose();
                     g.pose().translate(tx, ty, 0.0F);
                     g.pose().scale(hs, hs, 1.0F);
                     g.pose().translate(-8.0F, -8.0F, 0.0F);
                     this.tex(g, BACK, 8, 8, 200, 80);
                     float hp = p.getHealth();
                     float hpMax = p.getMaxHealth();
                     float hpFrac = hpMax > 0.0F ? hp / hpMax : 0.0F;
                     this.bar(g, 77, 28, 197, 38, hpFrac, -4708826, -12969445, false, 0, 0);
                     int aura = StatClientCache.aura;
                     int auraMax = StatClientCache.auraMax;
                     float auraFrac = auraMax > 0 ? (float)aura / (float)auraMax : 0.0F;
                     int auraFrom = -6928676;
                     int auraTo = -10827536;
                     LocalPlayer lp = Minecraft.getInstance().player;
                     if (lp != null) {
                        if (ClientSettingsCache.isAM(lp.getUUID())) {
                           auraFrom = -15461352;
                           auraTo = -9145218;
                        } else {
                           int cc = ClientSettingsCache.getColor(lp.getUUID());
                           if (cc != -1) {
                              auraFrom = -16777216 | shade(cc, 0.72F);
                              auraTo = -16777216 | shade(cc, 1.35F);
                           }
                        }
                     }

                     this.bar(g, 77, 42, 196, 52, auraFrac, 0, -14269872, true, auraFrom, auraTo);
                     int momentum = StatClientCache.momentum;
                     if (momentum > 0) {
                        float momFrac = Math.min(1.0F, (float)momentum / 100.0F);
                        this.bar(g, 77, 53, 196, 56, momFrac, 0, -14019552, true, -5226370, -36928);
                     }

                     int food = p.getFoodData().getFoodLevel();
                     this.bar(g, 77, 57, 133, 64, (float)food / 20.0F, -2714072, -12965100, false, 0, 0);
                     int armor = p.getArmorValue();
                     float armorFrac = (float)Math.min(armor, 20) / 20.0F;
                     this.bar(g, 139, 57, 197, 64, armorFrac, -5920332, -13684683, false, 0, 0);
                     Font font = mc.font;
                     this.num(g, font, Math.round(hp) + " / " + (int)hpMax, 137, 33, 1.0F);
                     if (auraMax > 0) {
                        this.num(g, font, aura + " / " + auraMax, 136, 47, 1.0F);
                     }

                     this.num(g, font, food + " / 20", 105, 60, 0.8F);
                     this.num(g, font, String.valueOf(armor), 168, 60, 0.8F);
                     this.tex(g, FRONT, 8, 8, 200, 80);
                     g.pose().popPose();
                     if (!headBroken) {
                        try {
                           this.head(g, p, screenWidth, screenHeight);
                        } catch (Throwable var27) {
                           headBroken = true;
                        }
                     }

                  }
               }
            }
         }
      }
   }

   public static void clearAll() {
      headBroken = false;
   }

   public static float panelX(int screenWidth) {
      int anchor = ClientHudConfig.hudAnchor();
      float hs = ClientHudConfig.hudScale();
      return anchor != 1 && anchor != 3 ? (float)ClientHudConfig.hudOffX() : (float)screenWidth - 192.0F * hs - (float)ClientHudConfig.hudOffX();
   }

   public static float panelY(int screenHeight) {
      int anchor = ClientHudConfig.hudAnchor();
      float hs = ClientHudConfig.hudScale();
      return anchor != 2 && anchor != 3 ? (float)ClientHudConfig.hudOffY() : (float)screenHeight - 124.0F * hs - (float)ClientHudConfig.hudOffY();
   }

   public static float panelWidth() {
      return 192.0F * ClientHudConfig.hudScale();
   }

   public static float panelHeight() {
      return 124.0F * ClientHudConfig.hudScale();
   }

   public static boolean panelShown() {
      return StatClientCache.hudMode == 0;
   }

   private void head(GuiGraphics g, LocalPlayer p, int screenWidth, int screenHeight) {
      float hs = ClientHudConfig.hudScale();
      int anchor = ClientHudConfig.hudAnchor();
      float ox = anchor != 1 && anchor != 3 ? (float)ClientHudConfig.hudOffX() : (float)screenWidth - 192.0F * hs - (float)ClientHudConfig.hudOffX();
      float oy = anchor != 2 && anchor != 3 ? (float)ClientHudConfig.hudOffY() : (float)screenHeight - 124.0F * hs - (float)ClientHudConfig.hudOffY();
      int x0 = Math.round(ox + 13.0F * hs);
      int y0 = Math.round(oy + 18.0F * hs);
      int x1 = Math.round(ox + 59.0F * hs);
      int y1 = Math.round(oy + 64.0F * hs);
      int cx = (x0 + x1) / 2;
      int scale = Math.round(55.0F * hs);
      int anchorY = Math.round(oy + 117.0F * hs);
      float rot = (float)(System.currentTimeMillis() % 8000L) / 8000.0F * 360.0F;
      Quaternionf pose = (new Quaternionf()).rotateX((float)Math.toRadians(190.0D)).rotateY((float)Math.toRadians((double)rot));
      float sHead = p.yHeadRot;
      float sYRot = p.getYRot();
      float sXRot = p.getXRot();
      float sBodyO = p.yBodyRotO;
      float sBody = p.yBodyRot;
      p.yHeadRot = 180.0F;
      p.setYRot(180.0F);
      p.setXRot(0.0F);
      p.yBodyRot = p.getYRot();
      p.yBodyRotO = p.getYRot();
      g.enableScissor(x0, y0, x1, y1);

      try {
         InventoryScreen.renderEntityInInventory(g, cx, anchorY, scale, pose, (Quaternionf)null, p);
      } finally {
         g.disableScissor();
         p.yHeadRot = sHead;
         p.setYRot(sYRot);
         p.setXRot(sXRot);
         p.yBodyRotO = sBodyO;
         p.yBodyRot = sBody;
      }

   }

   private void tex(GuiGraphics g, ResourceLocation t, int x, int y, int w, int h) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      g.blit(t, x, y, 0.0F, 0.0F, w, h, w, h);
      RenderSystem.disableBlend();
   }

   private static int shade(int rgb, float factor) {
      int r = Math.min(255, (int)((float)(rgb >> 16 & 255) * factor));
      int gg = Math.min(255, (int)((float)(rgb >> 8 & 255) * factor));
      int b = Math.min(255, (int)((float)(rgb & 255) * factor));
      return r << 16 | gg << 8 | b;
   }

   private void bar(GuiGraphics g, int x0, int y0, int x1, int y1, float frac, int fill, int ghost, boolean grad, int gFrom, int gTo) {
      if (frac < 0.0F) {
         frac = 0.0F;
      }

      if (frac > 1.0F) {
         frac = 1.0F;
      }

      this.rect(g, x0, y0, x1, y1, ghost);
      int total = x1 - x0;
      if (total > 0) {
         if (!grad) {
            int fw = Math.round((float)total * frac);
            if (fw >= 2) {
               this.rect(g, x0, y0, x0 + fw, y1, fill);
            }

         } else {
            int fwe = Math.round((float)total * frac);
            fwe -= fwe % 2;

            for(int i = 0; i < fwe; i += 2) {
               float t = total > 1 ? (float)i / (float)(total - 1) : 0.0F;
               this.rect(g, x0 + i, y0, x0 + i + 2, y1, this.lerpColor(gFrom, gTo, t));
            }

         }
      }
   }

   private void rect(GuiGraphics g, int x0, int y0, int x1, int y1, int argb) {
      if (x1 <= x0) {
         x1 = x0 + 1;
      }

      if (y1 <= y0) {
         y1 = y0 + 1;
      }

      float a = (float)(argb >>> 24 & 255) / 255.0F;
      float r = (float)(argb >>> 16 & 255) / 255.0F;
      float gr = (float)(argb >>> 8 & 255) / 255.0F;
      float b = (float)(argb & 255) / 255.0F;
      if (a == 0.0F) {
         a = 1.0F;
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      g.setColor(r, gr, b, a);
      g.blit(WHITE, x0, y0, 0.0F, 0.0F, x1 - x0, y1 - y0, 8, 8);
      g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
   }

   private int lerpColor(int a, int b, float t) {
      int ar = a >> 16 & 255;
      int ag = a >> 8 & 255;
      int ab = a & 255;
      int br = b >> 16 & 255;
      int bg = b >> 8 & 255;
      int bb = b & 255;
      int r = Math.round((float)ar + (float)(br - ar) * t);
      int gg = Math.round((float)ag + (float)(bg - ag) * t);
      int bl = Math.round((float)ab + (float)(bb - ab) * t);
      return -16777216 | r << 16 | gg << 8 | bl;
   }

   private void num(GuiGraphics g, Font font, String s, int cx, int cy, float scale) {
      float w = (float)font.width(s) * scale;
      g.pose().pushPose();
      g.pose().translate((float)cx - w / 2.0F, (float)cy - 9.0F * scale / 2.0F, 0.0F);
      g.pose().scale(scale, scale, 1.0F);
      g.drawString(font, s, 0, 0, -1, true);
      g.pose().popPose();
   }
}
