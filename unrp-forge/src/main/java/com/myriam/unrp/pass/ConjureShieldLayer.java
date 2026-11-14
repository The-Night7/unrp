package com.myriam.unrp.pass;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ConjureShieldLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   private static final ResourceLocation DISK = new ResourceLocation("unrp", "textures/misc/conjure_shield_disk.png");
   private static final int FULL_BRIGHT = 15728880;

   public ConjureShieldLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      if (!player.isInvisible()) {
         if (shieldEnabled(player)) {
            int color = 15220800;
            if (PassBridge.isAM(player.getUUID())) {
               color = 5263450;
            } else {
               int cc = PassBridge.color(player.getUUID());
               if (cc != -1) {
                  color = cc;
               }
            }

            float r = (float)(color >> 16 & 255) / 255.0F;
            float g = (float)(color >> 8 & 255) / 255.0F;
            float b = (float)(color & 255) / 255.0F;
            VertexConsumer vc = buffer.getBuffer(RenderType.eyes(DISK));
            PoseStack.Pose pose = poseStack.last();
            float orbit = ageInTicks * 0.06F;
            diskQuad(vc, pose, orbit, 1.15F, 0.45F, 0.7F, r, g, b);
            diskQuad(vc, pose, orbit + (float)Math.PI, 1.15F, 0.45F, 0.7F, r, g, b);
         }
      }
   }

   private static boolean shieldEnabled(AbstractClientPlayer player) {
      java.util.Set<String> on = PassBridge.STATE_VIEW.apply(player.getUUID());
      return on != null && on.contains("orbit_shield");
   }

   private static void diskQuad(VertexConsumer vc, PoseStack.Pose pose, float angle, float radius, float cy, float size, float r, float g, float b) {
      float cx = (float)Math.cos((double)angle) * radius;
      float cz = (float)Math.sin((double)angle) * radius;
      float tx = (float)(-Math.sin((double)angle));
      float tz = (float)Math.cos((double)angle);
      float spin = angle * 3.0F;
      float cs = (float)Math.cos((double)spin);
      float sn = (float)Math.sin((double)spin);
      float[][] corners = new float[][]{{-size, -size}, {-size, size}, {size, size}, {size, -size}};
      float[] xs = new float[4];
      float[] ys = new float[4];
      float[] zs = new float[4];

      for(int i = 0; i < 4; ++i) {
         float u = corners[i][0] * cs - corners[i][1] * sn;
         float v = corners[i][0] * sn + corners[i][1] * cs;
         xs[i] = cx + tx * u;
         ys[i] = cy + v;
         zs[i] = cz + tz * u;
      }

      vertex(vc, pose, xs[0], ys[0], zs[0], 0.0F, 0.0F, r, g, b);
      vertex(vc, pose, xs[1], ys[1], zs[1], 0.0F, 1.0F, r, g, b);
      vertex(vc, pose, xs[2], ys[2], zs[2], 1.0F, 1.0F, r, g, b);
      vertex(vc, pose, xs[3], ys[3], zs[3], 1.0F, 0.0F, r, g, b);
      vertex(vc, pose, xs[3], ys[3], zs[3], 1.0F, 0.0F, r, g, b);
      vertex(vc, pose, xs[2], ys[2], zs[2], 1.0F, 1.0F, r, g, b);
      vertex(vc, pose, xs[1], ys[1], zs[1], 0.0F, 1.0F, r, g, b);
      vertex(vc, pose, xs[0], ys[0], zs[0], 0.0F, 0.0F, r, g, b);
   }

   private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, float r, float g, float b) {
      vc.vertex(pose.pose(), x, y, z).color(r, g, b, 0.8F).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pose.normal(), 0.0F, 0.0F, -1.0F).endVertex();
   }
}
