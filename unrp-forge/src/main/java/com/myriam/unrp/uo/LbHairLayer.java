package com.myriam.unrp.uo;

import com.myriam.unrp.pass.UnrpPowerLayers;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LbHairLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   private static final ResourceLocation HAIR_TEX = new ResourceLocation("unrp", "textures/models/lb_hair.png");
   private static final int FULL_BRIGHT = 15728880;
   private final LbHairModel model;

   public LbHairLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet modelSet) {
      super(parent);
      this.model = new LbHairModel(modelSet.bakeLayer(OriginModels.LB_HAIR));
   }

   public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
      if (!player.isInvisible()) {
         int variant = UnrpPowerLayers.powerVariant(player, "limit_breaker");
         if (variant != 0) {
            int form = ClientLbFormCache.form(player.getUUID());
            if (!ColossusRenderer.wearing(player)) {
               float u = ClientLbFormCache.progress(player.getUUID());
               int next = ClientLbFormCache.target(player.getUUID());
               boolean toward = next > 0 && !LimitBreakerForms.apeForm(next);
               int shape = toward && u >= 0.1F ? next : form;
               boolean tinted = toward && u >= 0.45F;
               int tint = tinted ? next : form;
               boolean rose = tinted ? ClientLbFormCache.targetRose(player.getUUID()) : ClientLbFormCache.rose(player.getUUID());
               int tier = shape <= 0 ? 0 : LimitBreakerForms.HAIR[shape];
               float[] c;
               if (tint == 8) {
                  c = new float[]{0.24F, 0.24F, 0.33F};
               } else if (tint == 14) {
                  c = new float[]{0.97F, 0.98F, 1.0F};
               } else if (tint == 12) {
                  c = new float[]{0.22F, 0.24F, 0.31F};
               } else if (tint == 13) {
                  c = new float[]{0.84F, 0.88F, 0.94F};
               } else if (tint == 3) {
                  c = new float[]{0.26F, 0.22F, 0.17F};
               } else if (tint > 0 && tint != 2) {
                  int packed = LimitBreakerForms.color(tint, rose);
                  c = new float[]{(float)(packed >> 16 & 255) / 255.0F, (float)(packed >> 8 & 255) / 255.0F, (float)(packed & 255) / 255.0F};
               } else {
                  c = new float[]{0.3F, 0.26F, 0.24F};
               }

               boolean charging = UnrpPowerLayers.abilityOn(player, "limit_breaker", variant, "lb_t_break");
               boolean auraOn = form > 0 && !ClientLbFormCache.auraOff(player.getUUID());
               LbHairState s = LbHairState.of(player);
               s.style(shape == 8 || shape == 14);
               s.update(player, tier, c, charging, auraOn, form > 0, partialTick);
               this.model.pose(((PlayerModel)this.getParentModel()).head);
               this.model.shape(s);
               VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(HAIR_TEX));
               int lit = form <= 0 && !(s.charge > 0.2F) ? light : 15728880;
               this.model.pass(false);
               this.model.render(pose, vc, lit, OverlayTexture.NO_OVERLAY, s.color[0], s.color[1], s.color[2], 1.0F);
               if (this.model.anyGlow()) {
                  this.model.pass(true);
                  float t = (float)player.tickCount + partialTick;
                  float glow = 0.85F + 0.15F * Mth.sin(t * 0.21F);
                  this.model.render(pose, buffers.getBuffer(RenderType.eyes(HAIR_TEX)), 15728880, OverlayTexture.NO_OVERLAY, glow, glow, glow, 1.0F);
               }

            }
         }
      }
   }
}
