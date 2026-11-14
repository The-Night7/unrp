package com.myriam.unrp.uo;

import com.myriam.unrp.pass.UnrpPowerLayers;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class SarcophageGrabView {
   public static final int SKIP = 3;
   private static final float CROUCH_LEAN = 0.5F;
   private static final float CROUCH_DROP = 3.2F;
   private static final float CROUCH_ARM = 0.4F;
   private static SegmentedTailModel tail;

   private SarcophageGrabView() {
   }

   @SubscribeEvent
   public static void onRender(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_ENTITIES) {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer player = mc.player;
         if (player != null && mc.level != null && !player.isInvisible() && !player.isSpectator() && mc.options.getCameraType().isFirstPerson()) {
            int variant = UnrpPowerLayers.powerVariant(player, "sarcophage");
            if (variant != 0 && !PhageRenderer.shown(player)) {
               boolean grabbing = UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "grabbing");
               float partial = event.getPartialTick();
               float t = (float)player.tickCount + partial;
               int tails = SarcophageTailsLayer.tailCount(player, variant);
               SarcophageTailState.State st = SarcophageTailState.sample(player, t, tails, grabbing);
               float grow = st.grow[5];
               if (!(grow <= 0.02F)) {
                  if (tail == null) {
                     tail = new SegmentedTailModel(mc.getEntityModels().bakeLayer(OriginModels.SEGMENTED_TAIL));
                  }

                  boolean tempest = UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "tempest");
                  float glow = SarcophageTailsLayer.GLOW[Mth.clamp(tempest ? 5 : tails, 0, 5)];
                  Vec3 cam = event.getCamera().getPosition();
                  PoseStack ps = event.getPoseStack();
                  MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
                  RenderType cut = RenderType.entityCutoutNoCull(SarcophageTailsLayer.TAIL_TEX);
                  RenderType eyes = RenderType.eyes(SarcophageTailsLayer.TAIL_GLOW);
                  boolean crouch = player.isCrouching();
                  double[] body = new double[]{0.0D, crouch ? (double)3.2F : 0.0D, 0.0D, crouch ? 0.5D : 0.0D, 0.0D, 0.0D};
                  float limb = player.walkAnimation.position(partial);
                  float amount = Math.min(1.0F, player.walkAnimation.speed(partial));
                  float swing = Mth.cos(limb * 0.6662F);
                  double legR = (double)(swing * 1.4F * amount);
                  double armR = (double)(-swing * amount + (crouch ? 0.4F : 0.0F));
                  float scale = Math.max(0.25F, player.getBbWidth() / 0.6F);
                  float yaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
                  int light = mc.getEntityRenderDispatcher().getPackedLightCoords(player, partial);
                  ps.pushPose();

                  try {
                     ps.translate(Mth.lerp((double)partial, player.xo, player.getX()) - cam.x, Mth.lerp((double)partial, player.yo, player.getY()) - cam.y - (crouch ? 0.125D : 0.0D), Mth.lerp((double)partial, player.zo, player.getZ()) - cam.z);
                     ps.scale(scale, scale, scale);
                     ps.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
                     ps.scale(-1.0F, -1.0F, 1.0F);
                     ps.scale(0.9375F, 0.9375F, 0.9375F);
                     ps.translate(0.0F, -1.501F, 0.0F);
                     ps.translate(body[0] / 16.0D, body[1] / 16.0D, body[2] / 16.0D);
                     ps.mulPose(Axis.XP.rotation((float)body[3]));
                     SarcophageTailsLayer.drawGrab(tail, ps, buffers.getBuffer(cut), light, 1.0F, player, partial, grow, 3, body, legR, armR);
                     SarcophageTailsLayer.drawGrab(tail, ps, buffers.getBuffer(eyes), 15728880, glow, player, partial, grow, 3, body, legR, armR);
                  } finally {
                     ps.popPose();
                  }

                  buffers.endBatch(cut);
                  buffers.endBatch(eyes);
               }
            }
         }
      }
   }
}
