package com.myriam.unrp.uo;

import com.myriam.unrp.anim.MoveAnimClient;
import com.myriam.unrp.anim.MoveClipIds;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class DaystarFingerLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   public static final float LONG = 3.0F;
   public static final float HAND = 10.0F;
   public static final float FRONT = -0.75F;
   static final float SIDE_U0 = 0.578125F;
   static final float SIDE_U1 = 0.59375F;
   static final float SIDE_V0 = 0.953125F;
   static final float SIDE_V1 = 1.0F;
   static final float TIP_U0 = 0.625F;
   static final float TIP_U1 = 0.640625F;
   static final float TIP_V0 = 0.765625F;
   static final float TIP_V1 = 0.78125F;
   private static int charge = -2;
   private static int lift = -2;
   private static int heave = -2;
   private static int flick = -2;
   private static int slam = -2;

   public DaystarFingerLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   private static void ids() {
      if (charge == -2) {
         charge = MoveClipIds.id("ds_charge");
         lift = MoveClipIds.id("ds_lift");
         heave = MoveClipIds.id("ds_heave");
         flick = MoveClipIds.id("ds_throw");
         slam = MoveClipIds.id("ds_slam");
      }

   }

   public static float raised(int id, double age) {
      ids();
      double w = Math.max(MoveAnimClient.weight(id, false, charge, age), MoveAnimClient.weight(id, false, lift, age));
      w = Math.max(w, Math.max(MoveAnimClient.weight(id, true, heave, age), MoveAnimClient.weight(id, true, flick, age)));
      w = Math.max(w, MoveAnimClient.weight(id, true, slam, age));
      return (float)Math.max(0.0D, Math.min(1.0D, w));
   }

   public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
      if (!player.isInvisible() && !player.isSpectator() && !player.isSleeping() && !player.isFallFlying() && !player.isVisuallySwimming()) {
         float k = raised(player.getId(), (double)age);
         if (!(k <= 0.02F)) {
            float x0 = "slim".equals(player.getModelName()) ? 0.0F : 0.5F;
            pose.pushPose();

            try {
               ((PlayerModel)this.getParentModel()).leftArm.translateAndRotate(pose);
               VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(player.getSkinTextureLocation()));
               box(pose, vc, light, LivingEntityRenderer.getOverlayCoords(player, 0.0F), x0, 10.0F, -0.75F, x0 + 1.0F, 10.0F + 3.0F * k, 0.25F);
            } finally {
               pose.popPose();
            }

         }
      }
   }

   static void box(PoseStack pose, VertexConsumer vc, int light, int overlay, float x0, float y0, float z0, float x1, float y1, float z1) {
      Matrix4f m = pose.last().pose();
      Matrix3f n = pose.last().normal();
      float a0 = x0 / 16.0F;
      float b0 = y0 / 16.0F;
      float c0 = z0 / 16.0F;
      float a1 = x1 / 16.0F;
      float b1 = y1 / 16.0F;
      float c1 = z1 / 16.0F;
      face(vc, m, n, light, overlay, 0.0F, 0.0F, -1.0F, new float[]{a0, b0, c0, a1, b0, c0, a1, b1, c0, a0, b1, c0}, 0.578125F, 0.953125F, 0.59375F, 1.0F);
      face(vc, m, n, light, overlay, 0.0F, 0.0F, 1.0F, new float[]{a1, b0, c1, a0, b0, c1, a0, b1, c1, a1, b1, c1}, 0.578125F, 0.953125F, 0.59375F, 1.0F);
      face(vc, m, n, light, overlay, -1.0F, 0.0F, 0.0F, new float[]{a0, b0, c1, a0, b0, c0, a0, b1, c0, a0, b1, c1}, 0.578125F, 0.953125F, 0.59375F, 1.0F);
      face(vc, m, n, light, overlay, 1.0F, 0.0F, 0.0F, new float[]{a1, b0, c0, a1, b0, c1, a1, b1, c1, a1, b1, c0}, 0.578125F, 0.953125F, 0.59375F, 1.0F);
      face(vc, m, n, light, overlay, 0.0F, 1.0F, 0.0F, new float[]{a0, b1, c0, a1, b1, c0, a1, b1, c1, a0, b1, c1}, 0.625F, 0.765625F, 0.640625F, 0.78125F);
   }

   private static void face(VertexConsumer vc, Matrix4f m, Matrix3f nm, int light, int overlay, float nx, float ny, float nz, float[] q, float u0, float v0, float u1, float v1) {
      Vector3f nn = nm.transform(new Vector3f(nx, ny, nz));

      for(int i = 0; i < 4; ++i) {
         Vector3f p = m.transformPosition(new Vector3f(q[i * 3], q[i * 3 + 1], q[i * 3 + 2]));
         float u = i != 0 && i != 3 ? u1 : u0;
         float v = i < 2 ? v0 : v1;
         vc.vertex(p.x(), p.y(), p.z(), 1.0F, 1.0F, 1.0F, 1.0F, u, v, overlay, light, nn.x(), nn.y(), nn.z());
      }

   }
}
