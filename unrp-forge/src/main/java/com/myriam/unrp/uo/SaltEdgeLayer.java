package com.myriam.unrp.uo;

import com.myriam.unrp.pass.UnrpPowerLayers;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SaltEdgeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   public static final int CRYSTALS = 6;

   public SaltEdgeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   static double[] spot(int i) {
      double s = 0.05D + (double)i * 0.07D;
      return new double[]{0.5D + s, 0.5D + s + 0.08D, 0.5D};
   }

   public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbAmount, float partialTick, float age, float headYaw, float headPitch) {
      if (!player.isInvisible() && !player.isSpectator() && UnrpPowerLayers.lookVariant(player, "salt_seraph") != 0) {
         Minecraft mc = Minecraft.getInstance();
         if (player != mc.player || !mc.options.getCameraType().isFirstPerson()) {
            boolean rightMain = player.getMainArm() == HumanoidArm.RIGHT;
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof SwordItem) {
               HumanoidArm arm = rightMain ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
               boolean left = arm == HumanoidArm.LEFT;
               double t = (double)((float)player.tickCount + partialTick);
               pose.pushPose();

               try {
                  ((PlayerModel)this.getParentModel()).translateToHand(arm, pose);
                  pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
                  pose.mulPose(Axis.YP.rotationDegrees(180.0F));
                  pose.translate((left ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
                  Matrix4f m = ImbuedItemLayer.itemSpace(pose, player, stack, left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, left);
                  Matrix4f none = new Matrix4f();
                  double[] cam = new double[]{0.0D, 0.0D, 0.0D};
                  double[] ax = new double[]{1.0D, 0.0D, 0.0D};
                  double[] ay = new double[]{0.0D, 1.0D, 0.0D};
                  double[][] cs = new double[6][];

                  for(int i = 0; i < 6; ++i) {
                     double[] s = spot(i);
                     Vector3f p = m.transformPosition(new Vector3f((float)s[0], (float)s[1], (float)s[2]));
                     cs[i] = new double[]{(double)p.x(), (double)p.y(), (double)p.z()};
                  }

                  VertexConsumer soft = buffers.getBuffer(SaltDraw.soft());

                  for(int i = 0; i < 6; ++i) {
                     SaltDraw.bill(soft, none, cam, cs[i], ax, ay, 0.05D, (double)i * 1.3D, 2, (float)SaltShape.CRYS[0], (float)SaltShape.CRYS[1], (float)SaltShape.CRYS[2], 0.95F, false);
                  }

                  VertexConsumer add = buffers.getBuffer(SaltDraw.add());

                  for(int i = 1; i < 6; i += 2) {
                     SaltDraw.bill(add, none, cam, cs[i], ax, ay, 0.05D, t * 0.2D + (double)i, 0, 1.0F, 0.95F, 0.85F, 0.6F, true);
                  }
               } finally {
                  pose.popPose();
               }

            }
         }
      }
   }
}
