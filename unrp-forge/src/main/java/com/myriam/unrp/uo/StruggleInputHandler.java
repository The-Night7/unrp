package com.myriam.unrp.uo;



import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public class StruggleInputHandler {
   private static boolean jumpWasDown;
   private static boolean wasActive;
   private static long lastSent;

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && mc.level != null) {
            if (!StruggleClientState.active()) {
               jumpWasDown = mc.options.keyJump.isDown();
               wasActive = false;
            } else {
               int clicks;
               for(clicks = 0; mc.options.keyJump.consumeClick(); ++clicks) {
               }

               boolean down = mc.options.keyJump.isDown();
               boolean pressed = wasActive && !jumpWasDown && (down || clicks > 0);
               wasActive = true;
               jumpWasDown = down;
               if (pressed) {
                  long now = System.currentTimeMillis();
                  long pace = StruggleClientState.kind() == 4 ? 90L : 140L;
                  if (now - lastSent > pace) {
                     lastSent = now;
                     com.myriam.unrp.net.Net.CHANNEL.sendToServer(new StruggleMashPacket());
                  }
               }

            }
         } else {
            jumpWasDown = false;
            wasActive = false;
            StruggleClientState.clear();
         }
      }
   }
}
