package com.myriam.unrp.uo;



import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.threetag.palladium.power.IPowerHolder;
import net.threetag.palladium.power.PowerHandler;
import net.threetag.palladium.power.PowerManager;
import net.threetag.palladium.power.ability.AbilityInstance;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public class WeaveInputHandler {
   private static int reach = 9;

   public static int clientReach() {
      return reach;
   }

   public static boolean weaveHeld() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null) {
         return false;
      } else {
         Optional<PowerHandler> handler = PowerManager.getPowerHandler(player);
         if (handler.isEmpty()) {
            return false;
         } else {
            for(IPowerHolder holder : ((PowerHandler)handler.get()).getPowerHolders().values()) {
               AbilityInstance inst = (AbilityInstance)holder.getAbilities().get("weave");
               if (inst != null && inst.isEnabled()) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   @SubscribeEvent
   public static void onScroll(InputEvent.MouseScrollingEvent event) {
      if (weaveHeld()) {
         double delta = event.getScrollDelta();
         if (delta != 0.0D) {
            int next = reach + (delta > 0.0D ? 1 : -1);
            reach = next < 2 ? 2 : (next > 14 ? 14 : next);
            com.myriam.unrp.net.Net.CHANNEL.sendToServer(new WeaveReachPacket(reach));
            event.setCanceled(true);
         }
      }
   }
}
