package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class SaltCracksLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   public SaltCracksLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
      if (!player.isInvisible() && !player.isSpectator()) {
         double t = (double)((float)player.level().getGameTime() + partialTick);
         int k = LashVeinsLayer.stage(ClientSaltSeraph.veins(player.getId(), t));
         if (k > 0) {
            PlayerModel<AbstractClientPlayer> model = (PlayerModel)this.getParentModel();
            model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucent(LashVeinsLayer.LINES[k - 1])), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            float glow = (float)(0.5D + 0.5D * Math.sin(t * 0.35D));
            float lit = 0.55F + 0.45F * glow;
            double[] rim = SaltShape.CRACKS;
            model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(LashVeinsLayer.GLOW[k - 1])), light, OverlayTexture.NO_OVERLAY, (float)rim[0] * lit, (float)rim[1] * lit, (float)rim[2] * lit, 0.45F + 0.55F * glow);
         }
      }
   }
}
