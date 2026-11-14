package com.myriam.unrp.uo;



import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class LashSwingInput {
   private static double[] anchor;
   private static double rope;
   private static long ropeAt = -1000L;
   private static Object cast;

   private LashSwingInput() {
   }

   public static void move(double dx, double dy, double dz, double ax, double ay, double az, double len) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer p = mc.player;
      if (p != null && mc.level != null) {
         p.setDeltaMovement(dx, dy, dz);
         anchor = new double[]{ax, ay, az};
         rope = len;
         ropeAt = mc.level.getGameTime();
         cast = ClientBlackLash.swingCast(p.getId());
      }
   }

   static boolean swinging(LocalPlayer p) {
      return ClientBlackLash.swingAnchor(p.getId()) != null && !p.onGround() && p.isAlive() && !p.isPassenger() && !p.isSpectator();
   }

   static int unit(float v) {
      return Math.round(Math.max(-1.0F, Math.min(1.0F, v)) * 100.0F);
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void onInput(MovementInputUpdateEvent event) {
      Player in_0 = event.getEntity();
      if (in_0 instanceof LocalPlayer p) {
         if (swinging(p)) {
            Input in = event.getInput();
            int flags = (in.shiftKeyDown ? 1 : 0) | (in.jumping ? 2 : 0);
            com.myriam.unrp.net.Net.CHANNEL.sendToServer(new LashSwingPacket(unit(in.forwardImpulse), unit(-in.leftImpulse), flags));
            in.forwardImpulse = 0.0F;
            in.leftImpulse = 0.0F;
            in.up = false;
            in.down = false;
            in.left = false;
            in.right = false;
            in.jumping = false;
            in.shiftKeyDown = false;
            long now = p.level().getGameTime();
            if (anchor != null && cast != null && ClientBlackLash.swingCast(p.getId()) == cast && now - ropeAt <= 3L && now >= ropeAt) {
               Vec3 dm = p.getDeltaMovement();
               double[] hand = new double[]{p.getX(), p.getY() + 1.9D * (double)p.getBbHeight() / 1.8D, p.getZ()};
               double[] fit = LashMath.swingHold(hand, new double[]{dm.x, dm.y, dm.z}, anchor, rope);
               if (fit[0] != dm.x || fit[1] != dm.y || fit[2] != dm.z) {
                  p.setDeltaMovement(fit[0], fit[1], fit[2]);
               }

               return;
            }

            return;
         }
      }

   }
}
