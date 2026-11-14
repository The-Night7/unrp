package com.myriam.unrp.pass;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

public class JokerEmberLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   private static final ResourceLocation MOTE = new ResourceLocation("unrp", "textures/misc/ember_mote.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final float[][] MOTES = new float[][]{{0.0F, 1.0F, 17.0F, 1.0F, 0.55F, 0.05F}, {0.4F, 1.3F, 23.0F, -1.0F, 0.42F, 0.35F}, {0.75F, 0.8F, 29.0F, 1.0F, 0.62F, 0.7F}};

   public JokerEmberLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      if (!player.isInvisible()) {
         if (PassBridge.isAM(player.getUUID())) {
            if (player.getItemBySlot(EquipmentSlot.HEAD).is(com.myriam.unrp.item.ModItems.JOKER_MASK.get())) {
               poseStack.pushPose();
               ((PlayerModel)this.getParentModel()).head.translateAndRotate(poseStack);
               VertexConsumer vc = buffer.getBuffer(RenderType.eyes(MOTE));
               PoseStack.Pose pose = poseStack.last();

               for(float side : new float[]{-1.0F, 1.0F}) {
                  float ex = side * 2.0F / 16.0F;

                  for(float[] m : MOTES) {
                     float phase = m[0] + (side > 0.0F ? 0.5F : 0.0F);
                     float t = (ageInTicks / m[2] + phase) % 1.0F;
                     float rise = t * m[1] * 3.0F / 16.0F;
                     float wob = (float)Math.sin((double)(ageInTicks * 0.22F + phase * 9.0F)) * m[3] * 0.6F / 16.0F;
                     float size = m[4] * (1.0F - t * 0.55F) / 16.0F;
                     float alpha = t < 0.15F ? t / 0.15F : 1.0F - (t - 0.15F) / 0.85F;
                     float g = 0.32F + m[5] * 0.35F;
                     moteQuad(vc, pose, ex + wob, -0.25F - rise, -0.30625F, size, 1.0F, g, 0.04F, alpha * 0.9F);
                  }
               }

               poseStack.popPose();
            }
         }
      }
   }

   private static void moteQuad(VertexConsumer vc, PoseStack.Pose pose, float cx, float cy, float cz, float s, float r, float g, float b, float a) {
      vertex(vc, pose, cx - s, cy - s, cz, 0.0F, 0.0F, r, g, b, a);
      vertex(vc, pose, cx - s, cy + s, cz, 0.0F, 1.0F, r, g, b, a);
      vertex(vc, pose, cx + s, cy + s, cz, 1.0F, 1.0F, r, g, b, a);
      vertex(vc, pose, cx + s, cy - s, cz, 1.0F, 0.0F, r, g, b, a);
      vertex(vc, pose, cx + s, cy - s, cz, 1.0F, 0.0F, r, g, b, a);
      vertex(vc, pose, cx + s, cy + s, cz, 1.0F, 1.0F, r, g, b, a);
      vertex(vc, pose, cx - s, cy + s, cz, 0.0F, 1.0F, r, g, b, a);
      vertex(vc, pose, cx - s, cy - s, cz, 0.0F, 0.0F, r, g, b, a);
   }

   private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
      vc.vertex(pose.pose(), x, y, z).color(r, g, b, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pose.normal(), 0.0F, 0.0F, -1.0F).endVertex();
   }
}
