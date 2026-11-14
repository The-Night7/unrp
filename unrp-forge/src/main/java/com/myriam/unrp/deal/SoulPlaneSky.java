package com.myriam.unrp.deal;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.DimensionSpecialEffects.SkyType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public class SoulPlaneSky {
   public static final ResourceLocation EFFECTS_ID = new ResourceLocation("unrp", "soul_planes");
   private static final ResourceLocation PLANE_DIM = new ResourceLocation("unrp", "soul_planes");
   private static final int FALLBACK = 4930188;
   private static boolean present;
   private static UUID plotOwner;
   private static String plotPower = "";
   private static float curR = -1.0F;
   private static float curG;
   private static float curB;

   private SoulPlaneSky() {
   }

   public static void accept(PlaneSkyPacket msg) {
      present = msg.present;
      plotOwner = msg.present ? msg.plotOwner : null;
      plotPower = msg.power == null ? "" : msg.power;
   }

   public static void clearAll() {
      present = false;
      plotOwner = null;
      plotPower = "";
      curR = -1.0F;
   }

   private static int targetColor() {
      if (!present) {
         return 4930188;
      } else {
         int powerDefault = DealBridge.EYE_DEFAULTS.getOrDefault(plotPower, 4930188);
         return DealBridge.colorForOwner(plotOwner, powerDefault);
      }
   }

   private static class PlaneEffects extends DimensionSpecialEffects {
      PlaneEffects() {
         super(Float.NaN, false, SkyType.NONE, false, false);
      }

      public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
         return fogColor;
      }

      public boolean isFoggyAt(int x, int z) {
         return false;
      }

      public float[] getSunriseColor(float timeOfDay, float partialTicks) {
         return null;
      }
   }

   @EventBusSubscriber(
      modid = "unrp",
      value = {Dist.CLIENT}
   )
   public static class SoulPlaneForgeBus {
      @SubscribeEvent
      public static void onFogColor(ViewportEvent.ComputeFogColor event) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.level.dimension().location().equals(SoulPlaneSky.PLANE_DIM)) {
            int target = SoulPlaneSky.targetColor();
            float tr = (float)(target >> 16 & 255) / 255.0F * 0.55F;
            float tg = (float)(target >> 8 & 255) / 255.0F * 0.55F;
            float tb = (float)(target & 255) / 255.0F * 0.62F;
            if (SoulPlaneSky.curR < 0.0F) {
               SoulPlaneSky.curR = tr;
               SoulPlaneSky.curG = tg;
               SoulPlaneSky.curB = tb;
            } else {
               float k = Math.min(1.0F, (float)event.getPartialTick() * 0.08F + 0.02F);
               SoulPlaneSky.curR = Mth.lerp(k, SoulPlaneSky.curR, tr);
               SoulPlaneSky.curG = Mth.lerp(k, SoulPlaneSky.curG, tg);
               SoulPlaneSky.curB = Mth.lerp(k, SoulPlaneSky.curB, tb);
            }

            event.setRed(SoulPlaneSky.curR);
            event.setGreen(SoulPlaneSky.curG);
            event.setBlue(SoulPlaneSky.curB);
         } else {
            SoulPlaneSky.curR = -1.0F;
         }
      }
   }

   @EventBusSubscriber(
      modid = "unrp",
      bus = Bus.MOD,
      value = {Dist.CLIENT}
   )
   public static class SoulPlaneModBus {
      @SubscribeEvent
      public static void onRegisterEffects(RegisterDimensionSpecialEffectsEvent event) {
         event.register(SoulPlaneSky.EFFECTS_ID, new SoulPlaneSky.PlaneEffects());
      }
   }
}
