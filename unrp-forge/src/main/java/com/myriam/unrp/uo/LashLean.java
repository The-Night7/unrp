package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Quaternionf;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class LashLean {
   private static final Map<Integer, double[]> LEANS = new HashMap();

   private LashLean() {
   }

   static boolean hanging(AbstractClientPlayer p) {
      return ClientBlackLash.swingAnchor(p.getId()) != null && !p.onGround() && p.isAlive() && p.deathTime == 0 && !p.isPassenger() && !p.isSpectator() && !p.isFallFlying() && !p.isSwimming() && !p.isSleeping();
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level == null) {
            LEANS.clear();
         } else if (!mc.isPaused()) {
            Set<Integer> here = new HashSet();

            for(AbstractClientPlayer p : mc.level.players()) {
               boolean on = hanging(p);
               double[] s = (double[])LEANS.get(p.getId());
               if (s != null || on) {
                  if (s == null) {
                     s = new double[4];
                     LEANS.put(p.getId(), s);
                  }

                  s[0] = s[1];
                  s[1] = LashMath.leanStep(s[1], on);
                  here.add(p.getId());
               }
            }

            LEANS.entrySet().removeIf((e) -> !here.contains(e.getKey()) || ((double[])e.getValue())[0] <= 0.0D && ((double[])e.getValue())[1] <= 0.0D);
         }
      }
   }

   static double[] lean(AbstractClientPlayer p, float yaw, float pt) {
      double[] s = (double[])LEANS.get(p.getId());
      if (s == null) {
         return null;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if ((p != mc.player || mc.screen == null && !mc.options.getCameraType().isFirstPerson()) && GravityFrame.plain(GravityGuard.direction(p))) {
            double w = LashMath.smooth01(Mth.lerp((double)pt, s[0], s[1]));
            if (w <= 1.0E-4D) {
               return null;
            } else {
               double[] anchor = ClientBlackLash.swingAnchor(p.getId());
               if (anchor != null) {
                  Vec3 at = p.getPosition(pt);
                  double[] hand = new double[]{at.x, at.y + 1.9D * (double)p.getBbHeight() / 1.8D, at.z};
                  double[] l = LashMath.swingLean(LashMath.norm(LashMath.sub(anchor, hand)), (double)yaw);
                  s[2] = l[0];
                  s[3] = l[1];
               }

               return new double[]{s[2] * w, s[3] * w};
            }
         } else {
            return null;
         }
      }
   }

   static Quaternionf quat(double[] lean) {
      double[] q = LashMath.leanQuat(lean);
      return new Quaternionf((float)q[0], (float)q[1], (float)q[2], (float)q[3]);
   }

   public static void turn(AbstractClientPlayer p, PoseStack pose, float yaw, float pt) {
      double[] l = lean(p, yaw, pt);
      if (l != null) {
         pose.mulPose(quat(l));
      }

   }

   public static Quaternionf world(AbstractClientPlayer p, float yaw, float pt) {
      double[] l = lean(p, yaw, pt);
      return l == null ? null : HandCapture.around(yaw, quat(l));
   }
}
