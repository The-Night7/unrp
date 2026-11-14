package com.myriam.unrp.uo;
import com.myriam.unrp.pass.ViewUp;

import com.myriam.unrp.look.ClientSettingsCache;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class ArcaneBoltRenderer extends EntityRenderer<ArcaneBoltEntity> {
   private static final ResourceLocation TEX = new ResourceLocation("unrp", "textures/models/arcane_bolt.png");
   public static final double NEAR = 0.5D;
   public static final double NEAR_FADE = 1.6D;

   public ArcaneBoltRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(ArcaneBoltEntity entity) {
      return TEX;
   }

   public void render(ArcaneBoltEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      super.render(entity, yaw, partialTick, pose, buffers, packedLight);
      int col = 11567080;
      UUID owner = entity.getOwnerId();
      if (owner != null) {
         int stored = ClientSettingsCache.getColor(owner);
         if (stored != -1) {
            col = stored;
         } else if (ClientSettingsCache.isAM(owner)) {
            col = 3023906;
         }
      }

      int tint = entity.getTint();
      if (tint != 0) {
         this.orb(entity, partialTick, pose, buffers, tint);
      } else {
         int cr = col >> 16 & 255;
         int cg = col >> 8 & 255;
         int cb = col & 255;
         float s = entity.getBoltSize();
         pose.pushPose();
         float bs = 0.55F + 0.75F * s;
         pose.scale(bs, bs, bs);
         pose.translate(0.0F, 0.2F, 0.0F);
         pose.mulPose(Axis.YP.rotationDegrees(((float)entity.tickCount + partialTick) * 9.0F));
         pose.mulPose(Axis.XP.rotationDegrees(((float)entity.tickCount + partialTick) * 5.0F));
         VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(TEX));
         Matrix4f m = pose.last().pose();
         float w = 0.28F * s;
         float h = 0.42F * s;
         float[][] eq = new float[][]{{-w, 0.0F, -w}, {w, 0.0F, -w}, {w, 0.0F, w}, {-w, 0.0F, w}};

         for(int k = 0; k < 4; ++k) {
            int k2 = (k + 1) % 4;
            this.tri(vc, m, 0.0F, h, 0.0F, eq[k2][0], 0.0F, eq[k2][2], eq[k][0], 0.0F, eq[k][2], cr, cg, cb);
            this.tri(vc, m, 0.0F, -h, 0.0F, eq[k][0], 0.0F, eq[k][2], eq[k2][0], 0.0F, eq[k2][2], cr, cg, cb);
         }

         pose.popPose();
      }
   }

   private void orb(ArcaneBoltEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int tint) {
      float t = (float)entity.tickCount + partialTick;
      float s = entity.getBoltSize();
      float pulse = 1.0F + 0.12F * (float)Math.sin((double)(t * 0.9F));
      float core = (0.3F + 0.34F * s) * pulse;
      int cr = tint >> 16 & 255;
      int cg = tint >> 8 & 255;
      int cb = tint & 255;
      Camera cam = Minecraft.getInstance().gameRenderer.getMainCamera();
      float seen = (float)Math.max(0.0D, Math.min(1.0D, (cam.getPosition().distanceTo(entity.getPosition(partialTick)) - 0.5D) / 1.6D));
      if (!(seen <= 0.0F)) {
         pose.pushPose();
         pose.mulPose(Axis.YP.rotationDegrees(-cam.getYRot()));
         pose.mulPose(Axis.XP.rotationDegrees(cam.getXRot()));
         VertexConsumer vc = buffers.getBuffer(RenderType.eyes(TEX));
         Matrix4f m = pose.last().pose();
         this.disc(vc, m, core * 2.6F, cr, cg, cb, Math.round(70.0F * seen));
         this.disc(vc, m, core * 1.6F, cr, cg, cb, Math.round(170.0F * seen));
         this.disc(vc, m, core, 255, 255, 255, Math.round(240.0F * seen));
         pose.popPose();
      }
   }

   private void disc(VertexConsumer vc, Matrix4f m, float r, int cr, int cg, int cb, int a) {
      int wedges = 18;

      for(int i = 0; i < wedges; ++i) {
         float a0 = (float)((double)i * Math.PI * 2.0D / (double)wedges);
         float a1 = (float)((double)(i + 1) * Math.PI * 2.0D / (double)wedges);
         float x0 = (float)Math.cos((double)a0) * r;
         float y0 = (float)Math.sin((double)a0) * r;
         float x1 = (float)Math.cos((double)a1) * r;
         float y1 = (float)Math.sin((double)a1) * r;
         this.wedge(vc, m, x0, y0, x1, y1, cr, cg, cb, a);
      }

   }

   private void wedge(VertexConsumer vc, Matrix4f m, float x0, float y0, float x1, float y1, int cr, int cg, int cb, int a) {
      this.soft(vc, m, 0.0F, 0.0F, cr, cg, cb, a);
      this.soft(vc, m, 0.0F, 0.0F, cr, cg, cb, a);
      this.soft(vc, m, x0, y0, cr, cg, cb, 0);
      this.soft(vc, m, x1, y1, cr, cg, cb, 0);
      this.soft(vc, m, x1, y1, cr, cg, cb, 0);
      this.soft(vc, m, x0, y0, cr, cg, cb, 0);
      this.soft(vc, m, 0.0F, 0.0F, cr, cg, cb, a);
      this.soft(vc, m, 0.0F, 0.0F, cr, cg, cb, a);
   }

   private void soft(VertexConsumer vc, Matrix4f m, float x, float y, int cr, int cg, int cb, int a) {
      int lit = Math.max(0, Math.min(255, a));
      vc.vertex(m, x, y, 0.0F).color(cr * lit / 255, cg * lit / 255, cb * lit / 255, lit).uv(0.5F, 0.5F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
   }

   private void tri(VertexConsumer vc, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int cr, int cg, int cb) {
      vc.vertex(m, x1, y1, z1).color(255, 255, 255, 240).uv(0.5F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
      vc.vertex(m, x2, y2, z2).color(cr, cg, cb, 225).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
      vc.vertex(m, x3, y3, z3).color(cr, cg, cb, 225).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
      vc.vertex(m, x3, y3, z3).color(cr, cg, cb, 225).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
   }

   @SubscribeEvent
   public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.ARCANE_BOLT.get(), ArcaneBoltRenderer::new);
   }
}
