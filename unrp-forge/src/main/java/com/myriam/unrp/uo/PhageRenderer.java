package com.myriam.unrp.uo;

import com.myriam.unrp.pass.UnrpPowerLayers;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public class PhageRenderer {
   private static final ResourceLocation TEX = new ResourceLocation("unrp", "textures/models/sarcophage_phage.png");
   private static final ResourceLocation GLOW = new ResourceLocation("unrp", "textures/models/sarcophage_phage_glow.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final float FIST_SCALE = 0.5F;
   private static final float BEAT = 30.0F;
   private static PhageModel model;
   private static PhageMotion motion;

   private PhageRenderer() {
   }

   public static void bake(EntityModelSet models) {
      model = new PhageModel(models.bakeLayer(OriginModels.PHAGE));
      motion = new PhageMotion(model);
   }

   public static boolean shown(AbstractClientPlayer player) {
      int variant = UnrpPowerLayers.powerVariant(player, "sarcophage");
      return variant != 0 && UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "phage");
   }

   public static float heartbeat(float t) {
      float p = t % 30.0F / 30.0F;
      float lub = Mth.clamp(1.0F - Math.abs(p - 0.08F) / 0.07F, 0.0F, 1.0F);
      float dub = Mth.clamp(1.0F - Math.abs(p - 0.26F) / 0.07F, 0.0F, 1.0F) * 0.7F;
      return 0.78F + 0.22F * Math.max(lub, dub);
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void before(RenderPlayerEvent.Pre event) {
      Player var2 = event.getEntity();
      if (var2 instanceof AbstractClientPlayer player) {
         if (!player.isSpectator() && !player.isInvisible() && model != null) {
            if (shown(player)) {
               event.setCanceled(true);
               draw(player, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick());
            }
         }
      }
   }

   private static void draw(AbstractClientPlayer player, PoseStack pose, MultiBufferSource buffers, int light, float partial) {
      long now = player.level().getGameTime();
      motion.pose(PhageState.drive(player, partial, now));
      PhageState.speed(player, motion.speed());
      float body = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
      float scale = Math.max(0.25F, player.getBbWidth() / 0.6F);
      pose.pushPose();

      try {
         if (player.isCrouching()) {
            pose.translate(0.0F, 0.125F / scale, 0.0F);
         }

         pose.mulPose(Axis.YP.rotationDegrees(180.0F - body));
         if (player.deathTime > 0) {
            float fall = Mth.sqrt(((float)player.deathTime + partial - 1.0F) / 20.0F * 1.6F);
            pose.mulPose(Axis.ZP.rotationDegrees(Math.min(1.0F, fall) * 90.0F));
         }

         pose.scale(-1.0F, -1.0F, 1.0F);
         pose.scale(0.9375F, 0.9375F, 0.9375F);
         pose.translate(0.0F, -1.501F, 0.0F);
         int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
         VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEX));
         model.renderToBuffer(pose, vc, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
         float glow = heartbeat((float)player.tickCount + partial);
         VertexConsumer g = buffers.getBuffer(RenderType.eyes(GLOW));
         model.renderToBuffer(pose, g, 15728880, OverlayTexture.NO_OVERLAY, glow, glow, glow, 1.0F);
      } finally {
         pose.popPose();
      }

   }

   @SubscribeEvent
   public static void firstPerson(RenderArmEvent event) {
      AbstractClientPlayer player = event.getPlayer();
      if (player != null && model != null && shown(player)) {
         event.setCanceled(true);
         if (!player.isInvisible()) {
            boolean right = event.getArm() == HumanoidArm.RIGHT;
            model.rest();
            ModelPart fore = model.parts[model.id(right ? "arm_r_fore" : "arm_l_fore")];
            fore.setPos(0.0F, 0.0F, 0.0F);
            fore.setRotation(0.0F, 0.0F, 0.0F);
            PoseStack pose = event.getPoseStack();
            pose.pushPose();

            try {
               pose.translate((right ? -6.0F : 6.0F) / 16.0F, 0.0625F, 0.0F);
               pose.scale(0.5F, 0.5F, 0.5F);
               VertexConsumer vc = event.getMultiBufferSource().getBuffer(RenderType.entityCutoutNoCull(TEX));
               fore.render(pose, vc, event.getPackedLight(), OverlayTexture.NO_OVERLAY);
               VertexConsumer g = event.getMultiBufferSource().getBuffer(RenderType.eyes(GLOW));
               fore.render(pose, g, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            } finally {
               pose.popPose();
               model.rest();
            }

         }
      }
   }

   @SubscribeEvent
   public static void tick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && !mc.isPaused()) {
            for(AbstractClientPlayer player : mc.level.players()) {
               if (shown(player) && !player.isSpectator()) {
                  PhageState.tick(player);
               } else {
                  PhageState.forget(player);
               }
            }

         }
      }
   }
}
