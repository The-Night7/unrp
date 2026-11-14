package com.myriam.unrp.uo;



import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public class IlluminationFlashOverlay implements IGuiOverlay {
   public static final IlluminationFlashOverlay INSTANCE = new IlluminationFlashOverlay();
   private static long flashStartMs = 0L;
   private static long flashDurationMs = 0L;

   public static void trigger(int durationTicks) {
      flashStartMs = System.currentTimeMillis();
      flashDurationMs = (long)durationTicks * 50L;
   }

   public void render(ForgeGui gui, GuiGraphics g, float partialTick, int screenWidth, int screenHeight) {
      if (flashDurationMs > 0L) {
         long elapsed = System.currentTimeMillis() - flashStartMs;
         if (elapsed >= flashDurationMs) {
            flashDurationMs = 0L;
         } else {
            double remaining = 1.0D - (double)elapsed / (double)flashDurationMs;
            int alpha = (int)(255.0D * remaining);
            if (alpha > 0) {
               if (alpha > 255) {
                  alpha = 255;
               }

               int color = alpha << 24 | 16777215;
               g.fill(0, 0, screenWidth, screenHeight, color);
            }
         }
      }
   }

   @EventBusSubscriber(
      modid = "unrp",
      bus = Bus.MOD,
      value = {Dist.CLIENT}
   )
   public static class IlluminationFlashRegistrar {
      private static boolean done = false;

      @SubscribeEvent
      public static void onRegister(RegisterGuiOverlaysEvent event) {
         if (!done) {
            done = true;
            event.registerAboveAll("unrp_flash", IlluminationFlashOverlay.INSTANCE);
         }
      }
   }
}
