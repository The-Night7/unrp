package com.myriam.unrp.uo;



import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
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
public class BreakCineHandler {
   private static volatile int left;
   private static volatile int total;
   private static CameraType before;
   private static float spin;
   private static volatile boolean orbitApplied;

   public static void begin(int ticks, int tier) {
      Minecraft mc = Minecraft.getInstance();
      if (ticks <= 0) {
         end(mc);
      } else {
         if (left <= 0) {
            before = mc.options.getCameraType();
            spin = 0.0F;
         }

         left = ticks;
         total = ticks;
         mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
      }
   }

   private static void end(Minecraft mc) {
      if (left > 0 || total > 0) {
         left = 0;
         total = 0;
         orbitApplied = false;
         if (before != null && mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            mc.options.setCameraType(before);
         }

         before = null;
      }

   }

   public static void clearAll() {
      end(Minecraft.getInstance());
   }

   public static boolean active() {
      return left > 0;
   }

   public static double[] orbitPose(Entity entity, float partialTick) {
      if (left > 0 && total > 0) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null && entity == mc.player) {
            orbitApplied = true;
            float done = ((float)(total - left) + partialTick) / (float)total;
            double swing = Math.sin((double)done * Math.PI);
            double angle = Math.toRadians((double)done * 540.0D - 90.0D);
            double size = Math.max(1.0D, (double)entity.getBbWidth() / 0.6D);
            double radius = (3.4D - 1.1D * swing) * size;
            Vec3 eye = entity.getEyePosition(partialTick);
            double cx = eye.x + Math.cos(angle) * radius;
            double cy = eye.y + (0.35D + swing * 1.05D) * size;
            double cz = eye.z + Math.sin(angle) * radius;
            Vec3 focus = eye.add(0.0D, -0.25D * size, 0.0D);
            double dx = focus.x - cx;
            double dy = focus.y - cy;
            double dz = focus.z - cz;
            double flat = Math.sqrt(dx * dx + dz * dz);
            float camYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
            float camPitch = (float)(-Math.toDegrees(Math.atan2(dy, flat)));
            return new double[]{cx, cy, cz, (double)camYaw, (double)camPitch};
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         if (left > 0) {
            if (mc.player != null && !mc.isPaused()) {
               --left;
               if (left <= 0) {
                  end(mc);
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
      if (left > 0 && total > 0 && !orbitApplied) {
         float done = ((float)(total - left) + (float)event.getPartialTick()) / (float)total;
         spin = done * 360.0F;
         event.setYaw(event.getYaw() + spin);
         event.setPitch(12.0F + arc(done) * 6.0F);
      }
   }

   private static float arc(float done) {
      return (float)Math.sin((double)done * Math.PI);
   }
}
