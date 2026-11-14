package com.myriam.unrp.uo;



import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

public final class WheelArt {
   private WheelArt() {
   }

   public static void disc(GuiGraphics g, int r, int color) {
      for(int dy = -r; dy <= r; ++dy) {
         int half = (int)Math.floor(Math.sqrt((double)r * (double)r - (double)dy * (double)dy));
         g.fill(-half, dy, half + 1, dy + 1, color);
      }

   }

   public static void annulus(GuiGraphics g, int ro, int ri, int color) {
      for(int dy = -ro; dy <= ro; ++dy) {
         int ho = (int)Math.floor(Math.sqrt((double)ro * (double)ro - (double)dy * (double)dy));
         if (Math.abs(dy) > ri) {
            g.fill(-ho, dy, ho + 1, dy + 1, color);
         } else {
            int hi = (int)Math.floor(Math.sqrt((double)ri * (double)ri - (double)dy * (double)dy));
            g.fill(-ho, dy, -hi, dy + 1, color);
            g.fill(hi + 1, dy, ho + 1, dy + 1, color);
         }
      }

   }

   public static void ellipse(GuiGraphics g, int rx, int ry, int color) {
      for(int dy = -ry; dy <= ry; ++dy) {
         double f = 1.0D - (double)dy * (double)dy / ((double)ry * (double)ry);
         int half = (int)Math.floor((double)rx * Math.sqrt(Math.max(0.0D, f)));
         g.fill(-half, dy, half + 1, dy + 1, color);
      }

   }

   public static void dots(GuiGraphics g, int r, int n, float spin, int color) {
      for(int i = 0; i < n; ++i) {
         float a = spin + (float)((Math.PI * 2D) * (double)i / (double)n);
         int x = (int)Math.floor((double)(Mth.cos(a) * (float)r));
         int y = (int)Math.floor((double)(Mth.sin(a) * (float)r));
         g.fill(x, y, x + 1, y + 1, color);
      }

   }

   public static int argb(int alpha, int rgb) {
      return Mth.clamp(alpha, 0, 255) << 24 | rgb & 16777215;
   }

   public static int mix(int a, int b, float f) {
      int r = (int)((float)(a >> 16 & 255) * (1.0F - f) + (float)(b >> 16 & 255) * f);
      int gg = (int)((float)(a >> 8 & 255) * (1.0F - f) + (float)(b >> 8 & 255) * f);
      int bl = (int)((float)(a & 255) * (1.0F - f) + (float)(b & 255) * f);
      return r << 16 | gg << 8 | bl;
   }

   public static void icon(GuiGraphics g, ResourceLocation tex, float x, float y, float s, float r, float gg, float b, float a) {
      if (tex != null && !(a <= 0.01F)) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         g.setColor(r, gg, b, a);
         g.pose().pushPose();
         g.pose().translate(x, y, 0.0F);
         g.pose().scale(s, s, 1.0F);
         g.blit(tex, -8, -8, 0.0F, 0.0F, 16, 16, 16, 16);
         g.pose().popPose();
         g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
      }
   }

   public static void ui(String id, float pitch, float volume) {
      SoundEvent ev = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation(id));
      if (ev != null) {
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ev, pitch, volume));
      }

   }

   public static float shortest(float d) {
      float t = ((float)Math.PI * 2F);
      d %= t;
      if ((double)d > Math.PI) {
         d -= t;
      }

      if ((double)d < -Math.PI) {
         d += t;
      }

      return d;
   }

   public static float pop(float appear) {
      float t = Mth.clamp(appear, 0.0F, 1.0F);
      float u = t - 1.0F;
      return Math.max(0.0F, 1.0F + 2.70158F * u * u * u + 1.70158F * u * u);
   }
}
