package com.myriam.unrp.pass;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class HollowSigilLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   private static final ResourceLocation GLYPHS = new ResourceLocation("unrp", "textures/models/hm_glyphs.png");
   private static final int FULL_BRIGHT = 15728880;

   public HollowSigilLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
      if (!player.isInvisible()) {
         if (UnrpPowerLayers.powerVariant(player, "hollow_mark") != 0) {
            float t = (float)player.tickCount + partialTick;
            VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucentEmissive(GLYPHS, false));
            this.band(pose, vc, ((PlayerModel)this.getParentModel()).leftArm, t, 8.4F, 0.052F);
            this.band(pose, vc, ((PlayerModel)this.getParentModel()).leftArm, t + 40.0F, 10.6F, -0.038F);
         }
      }
   }

   private void band(PoseStack pose, VertexConsumer vc, ModelPart armPart, float t, float yPx, float speed) {
      pose.pushPose();
      armPart.translateAndRotate(pose);
      Matrix4f m = pose.last().pose();
      float s = 0.0625F;
      float rad = 3.3F * s;
      float y = yPx * s + Mth.sin(t * 0.045F) * 0.35F * s;
      float half = 1.7F * s;

      for(int i = 0; i < 5; ++i) {
         float a = t * speed + (float)((Math.PI * 2D) * (double)i) / 5.0F;
         float cx = 1.0F * s + Mth.cos(a) * rad;
         float cz = Mth.sin(a) * rad;
         float u0 = (float)i / 5.0F;
         float u1 = (float)(i + 1) / 5.0F;
         float glow = 0.55F + 0.45F * Mth.sin(t * 0.11F + (float)i * 1.7F);
         this.quadBoth(vc, m, cx, y, cz, half, u0, u1, glow);
      }

      pose.popPose();
   }

   private void quadBoth(VertexConsumer vc, Matrix4f m, float cx, float y, float cz, float half, float u0, float u1, float glow) {
      float a = 0.55F + 0.4F * glow;
      this.vert(vc, m, cx - half, y - half, cz, u0, 0.0F, a);
      this.vert(vc, m, cx + half, y - half, cz, u1, 0.0F, a);
      this.vert(vc, m, cx + half, y + half, cz, u1, 1.0F, a);
      this.vert(vc, m, cx - half, y + half, cz, u0, 1.0F, a);
      this.vert(vc, m, cx - half, y + half, cz, u0, 1.0F, a);
      this.vert(vc, m, cx + half, y + half, cz, u1, 1.0F, a);
      this.vert(vc, m, cx + half, y - half, cz, u1, 0.0F, a);
      this.vert(vc, m, cx - half, y - half, cz, u0, 0.0F, a);
      this.vert(vc, m, cx, y - half, cz - half, u0, 0.0F, a);
      this.vert(vc, m, cx, y - half, cz + half, u1, 0.0F, a);
      this.vert(vc, m, cx, y + half, cz + half, u1, 1.0F, a);
      this.vert(vc, m, cx, y + half, cz - half, u0, 1.0F, a);
      this.vert(vc, m, cx, y + half, cz - half, u0, 1.0F, a);
      this.vert(vc, m, cx, y + half, cz + half, u1, 1.0F, a);
      this.vert(vc, m, cx, y - half, cz + half, u1, 0.0F, a);
      this.vert(vc, m, cx, y - half, cz - half, u0, 0.0F, a);
   }

   private void vert(VertexConsumer vc, Matrix4f m, float x, float y, float z, float u, float v, float a) {
      vc.vertex(m, x, y, z).color(1.0F, 1.0F, 1.0F, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
   }
}
