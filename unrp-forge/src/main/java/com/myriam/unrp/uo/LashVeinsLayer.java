package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class LashVeinsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   public static final int STAGES = 8;
   static final ResourceLocation[] LINES = new ResourceLocation[8];
   static final ResourceLocation[] GLOW = new ResourceLocation[8];

   public LashVeinsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   public static int stage(double amount) {
      return amount <= 0.0D ? 0 : Math.max(1, Math.min(8, (int)Math.ceil(amount * 8.0D - 1.0E-9D)));
   }

   public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
      if (!player.isInvisible() && !player.isSpectator()) {
         double t = (double)((float)player.level().getGameTime() + partialTick);
         int k = stage(ClientBlackLash.veins(player.getId(), t));
         if (k > 0) {
            PlayerModel<AbstractClientPlayer> model = (PlayerModel)this.getParentModel();
            model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucent(LINES[k - 1])), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            float glow = (float)(0.5D + 0.5D * Math.sin(t * 0.35D));
            float lit = 0.55F + 0.45F * glow;
            double[] rage = LashShape.RAGE;
            model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(GLOW[k - 1])), light, OverlayTexture.NO_OVERLAY, (float)rage[0] * lit, (float)rage[1] * lit, (float)rage[2] * lit, 0.45F + 0.55F * glow);
         }
      }
   }

   static {
      for(int i = 0; i < 8; ++i) {
         LINES[i] = new ResourceLocation("unrp", "textures/entity/black_lash_veins_" + (i + 1) + ".png");
         GLOW[i] = new ResourceLocation("unrp", "textures/entity/black_lash_veins_glow_" + (i + 1) + ".png");
      }

   }
}
