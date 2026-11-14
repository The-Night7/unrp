package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LbFurLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   private static final ResourceLocation FUR_TEX = new ResourceLocation("unrp", "textures/models/lb_fur.png");
   public static final float[] GROW_START = new float[]{0.0F, 0.0F, 0.15F, 0.15F, 0.3F, 0.3F};
   public static final float GROW_SPAN = 0.45F;
   public static final float TAIL_START = 0.45F;
   public static final float TAIL_STEP = 0.06F;
   public static final float TAIL_SPAN = 0.12F;
   private final ModelPart body;
   private final ModelPart chest;
   private final ModelPart armR;
   private final ModelPart armL;
   private final ModelPart legR;
   private final ModelPart legL;
   private final ModelPart[] coat;
   private final ModelPart[] tail = new ModelPart[8];
   private final float[] yaw = new float[8];
   private final float[] pitch = new float[8];

   public LbFurLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet modelSet) {
      super(parent);
      ModelPart root = modelSet.bakeLayer(OriginModels.LB_FUR);
      this.body = root.getChild("body");
      this.chest = root.getChild("chest");
      this.armR = root.getChild("arm_r");
      this.armL = root.getChild("arm_l");
      this.legR = root.getChild("leg_r");
      this.legL = root.getChild("leg_l");
      this.coat = new ModelPart[]{this.chest, this.body, this.armR, this.armL, this.legR, this.legL};
      ModelPart seg = this.body.getChild("tail0");

      for(int i = 0; i < 8; ++i) {
         this.tail[i] = seg;
         if (i + 1 < 8) {
            seg = seg.getChild("tail" + (i + 1));
         }
      }

   }

   public static LayerDefinition createLayer() {
      return LbFurGeometry.createLayer();
   }

   private static boolean furForm(int form) {
      return form == 8 || form == 14;
   }

   public static float grown(float g, float start, float span) {
      float x = Mth.clamp((g - start) / span, 0.0F, 1.0F);
      return x * x * (3.0F - 2.0F * x);
   }

   public static float forming(int form, int next, float u) {
      if (furForm(form)) {
         return 1.0F;
      } else {
         return !(u < 0.0F) && furForm(next) ? (u - 0.25F) / 0.75F : 0.0F;
      }
   }

   public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
      if (!player.isInvisible()) {
         int form = ClientLbFormCache.form(player.getUUID());
         int next = ClientLbFormCache.target(player.getUUID());
         float grow = forming(form, next, ClientLbFormCache.progress(player.getUUID()));
         if (!(grow <= 0.0F)) {
            int look = furForm(form) ? form : next;
            PlayerModel<AbstractClientPlayer> pm = (PlayerModel)this.getParentModel();
            this.body.copyFrom(pm.body);
            this.chest.copyFrom(pm.body);
            this.armR.copyFrom(pm.rightArm);
            this.armL.copyFrom(pm.leftArm);
            this.legR.copyFrom(pm.rightLeg);
            this.legL.copyFrom(pm.leftLeg);

            for(int i = 0; i < this.coat.length; ++i) {
               float k_0 = grown(grow, GROW_START[i], 0.45F);
               this.coat[i].yScale = k_0;
               this.coat[i].visible = k_0 > 0.01F;
            }

            float t = (float)player.tickCount + partialTick;
            tailPose(this.yaw, this.pitch, look == 8, t, limbSwing, limbSwingAmount, player.isCrouching(), LbStance.weight(player), LbStance.burst(look, t));

            for(int i = 0; i < 8; ++i) {
               float k = grown(grow, 0.45F + (float)i * 0.06F, 0.12F);
               this.tail[i].yRot = this.yaw[i];
               this.tail[i].xRot = this.pitch[i];
               this.tail[i].xScale = k;
               this.tail[i].yScale = k;
               this.tail[i].zScale = k;
               this.tail[i].visible = k > 0.01F;
            }

            boolean white = look == 14;
            float r = white ? 0.96F : 0.76F;
            float g = white ? 0.97F : 0.18F;
            float b = white ? 1.0F : 0.24F;
            VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(FUR_TEX));
            this.chest.render(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            this.body.render(pose, vc, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.armR.render(pose, vc, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.armL.render(pose, vc, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.legR.render(pose, vc, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.legL.render(pose, vc, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
         }
      }
   }

   public static void tailPose(float[] yaw, float[] pitch, boolean feral, float t, float walk, float amount, boolean crouch, float still, float burst) {
      int n = yaw.length;
      float move = Mth.clamp(amount * 1.6F, 0.0F, 1.0F);
      float freq;
      float amp;
      float lift;
      float curl;
      float sweep;
      if (feral) {
         freq = 0.21F;
         amp = 0.13F + 0.12F * burst;
         lift = -0.85F + 0.4F * still;
         curl = 0.11F * still + 0.08F * burst;
         sweep = 0.0F;
      } else {
         freq = 0.11F;
         amp = 0.03F;
         lift = -0.6F + 0.1F * still;
         curl = 0.12F * still + 0.1F * burst;
         sweep = 0.62F * still;
      }

      lift += (-0.45F - lift) * move;
      amp += 0.06F * move;
      if (crouch) {
         lift -= 0.2F;
      }

      for(int i = 0; i < n; ++i) {
         float k = (float)i / (float)(n - 1);
         float wave = Mth.sin(t * freq - (float)i * 0.55F) * amp * (0.5F + k);
         float gait = Mth.sin(walk * 0.6662F - (float)i * 0.5F) * 0.1F * move;
         float swing = Mth.sin(t * 0.025F - (float)i * 0.3F) * (i == 0 ? 1.0F : 0.26F);
         yaw[i] = wave + gait + sweep * swing * (1.0F - move);
         float bend = i == 0 ? lift : curl * (0.6F + 0.8F * k);
         pitch[i] = bend + 0.04F * Mth.sin(t * 0.07F + (float)i) * (1.0F - move);
      }

   }
}
