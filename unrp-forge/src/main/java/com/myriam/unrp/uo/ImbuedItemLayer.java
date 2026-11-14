package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.ForgeHooksClient;
import org.joml.Matrix4f;

public class ImbuedItemLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   public ImbuedItemLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   public static int woven(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      return tag != null && tag.contains("unrp_at_trait") ? AuraTransferEvents.traitColor(tag.getString("unrp_at_trait")) : -1;
   }

   public static float intro(ItemStack stack, Level level, float partialTick) {
      CompoundTag tag = stack.getTag();
      return tag != null && tag.contains("unrp_at_born") ? Math.max(0.0F, (float)(level.getGameTime() - tag.getLong("unrp_at_born")) + partialTick) : 16.0F;
   }

   public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbAmount, float partialTick, float age, float headYaw, float headPitch) {
      if (!player.isInvisible()) {
         boolean rightMain = player.getMainArm() == HumanoidArm.RIGHT;
         this.arm(pose, player, rightMain ? player.getMainHandItem() : player.getOffhandItem(), HumanoidArm.RIGHT, partialTick);
         this.arm(pose, player, rightMain ? player.getOffhandItem() : player.getMainHandItem(), HumanoidArm.LEFT, partialTick);
      }
   }

   private void arm(PoseStack pose, AbstractClientPlayer player, ItemStack stack, HumanoidArm arm, float partialTick) {
      int rgb = woven(stack);
      if (rgb >= 0) {
         boolean left = arm == HumanoidArm.LEFT;
         pose.pushPose();

         try {
            ((PlayerModel)this.getParentModel()).translateToHand(arm, pose);
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.translate((left ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
            Matrix4f m = itemSpace(pose, player, stack, left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, left);
            WeaveFx.woven(player.getId() * 2 + (left ? 1 : 0), m, rgb, (float)player.tickCount + partialTick, intro(stack, player.level(), partialTick));
         } finally {
            pose.popPose();
         }

      }
   }

   static Matrix4f itemSpace(PoseStack pose, LivingEntity holder, ItemStack stack, ItemDisplayContext context, boolean left) {
      BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, holder.level(), holder, holder.getId());
      pose.pushPose();

      Matrix4f var6;
      try {
         ForgeHooksClient.handleCameraTransforms(pose, model, context, left);
         pose.translate(-0.5F, -0.5F, -0.5F);
         var6 = new Matrix4f(pose.last().pose());
      } finally {
         pose.popPose();
      }

      return var6;
   }

   public static void firstPerson(AbstractClientPlayer player, InteractionHand hand, ItemStack stack, PoseStack pose, MultiBufferSource buffers, float partialTick) {
      int rgb = woven(stack);
      if (rgb >= 0) {
         HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
         boolean left = arm == HumanoidArm.LEFT;
         Matrix4f m = itemSpace(pose, player, stack, left ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, left);
         if (buffers instanceof MultiBufferSource.BufferSource) {
            MultiBufferSource.BufferSource source = (MultiBufferSource.BufferSource)buffers;
            source.endBatch(Sheets.translucentCullBlockSheet());
         }

         WeaveFx.drawWoven(buffers.getBuffer(WeaveDraw.add()), m, rgb, (float)player.tickCount + partialTick, intro(stack, player.level(), partialTick));
      }
   }
}
