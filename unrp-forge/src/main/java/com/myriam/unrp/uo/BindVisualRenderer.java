package com.myriam.unrp.uo;

import com.myriam.unrp.pass.ViewUp;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BindVisualRenderer extends EntityRenderer<BindVisualEntity> {
   private static final ResourceLocation TEX = new ResourceLocation("unrp", "textures/particle/aura_mote_0.png");

   public BindVisualRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(BindVisualEntity entity) {
      return TEX;
   }

   public boolean shouldRender(BindVisualEntity entity, Frustum frustum, double camX, double camY, double camZ) {
      return true;
   }

   public void render(BindVisualEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      super.render(entity, yaw, partialTick, pose, buffers, packedLight);
      Entity raw = entity.level().getEntity(entity.getVictimId());
      if (raw instanceof LivingEntity victim) {
         Vec3 self = entity.getPosition(partialTick);
         Vec3 at = victim.getPosition(partialTick);
         float t = (float)entity.tickCount + partialTick;
         pose.pushPose();
         pose.translate(at.x - self.x, at.y - self.y, at.z - self.z);
         VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(TEX));
         if (entity.getMode() == 0) {
            this.renderBubble(pose, vc, victim, t, packedLight);
         } else if (entity.getMode() == 2) {
            this.renderSand(pose, vc, victim, t, packedLight);
         } else {
            this.renderIce(pose, vc, victim, t, packedLight);
         }

         pose.popPose();
      }
   }

   private void renderBubble(PoseStack pose, VertexConsumer vc, LivingEntity victim, float t, int light) {
      float headY = victim.getBbHeight() * 0.82F;
      float r = Math.max(0.34F, victim.getBbWidth() * 0.62F);
      float wx = 1.0F + 0.05F * Mth.sin(t * 0.31F);
      float wy = 1.0F + 0.05F * Mth.sin(t * 0.27F + 2.1F);
      float wz = 1.0F + 0.05F * Mth.sin(t * 0.35F + 4.2F);
      pose.pushPose();
      pose.translate(0.0D, (double)headY, 0.0D);
      Matrix4f m = pose.last().pose();
      int lat = 7;
      int lon = 12;

      for(int i = 0; i < lat; ++i) {
         float p0 = (float)(Math.PI * (double)i / (double)lat - (Math.PI / 2D));
         float p1 = (float)(Math.PI * (double)(i + 1) / (double)lat - (Math.PI / 2D));

         for(int j = 0; j < lon; ++j) {
            float a0 = (float)((Math.PI * 2D) * (double)j / (double)lon) + t * 0.02F;
            float a1 = (float)((Math.PI * 2D) * (double)(j + 1) / (double)lon) + t * 0.02F;
            float shade = 0.85F + 0.15F * Mth.sin(p0 + 1.2F);
            int cr = (int)(111.0F * shade);
            int cg = (int)(194.0F * shade);
            int cb = (int)(240.0F * shade);
            int alpha = 96 + (int)(26.0F * Mth.sin(p0 * 2.0F + t * 0.15F));
            this.sphereQuad(vc, m, r, wx, wy, wz, p0, p1, a0, a1, cr, cg, cb, alpha, light);
         }
      }

      float hr = r * 0.22F;
      float hx = -r * 0.45F * wx;
      float hy = r * 0.5F * wy;
      float hz = -r * 0.45F * wz;
      this.quad(vc, m, hx - hr, hy - hr, hz, hx - hr, hy + hr, hz, hx + hr, hy + hr, hz, hx + hr, hy - hr, hz, 235, 248, 255, 190, light);
      pose.popPose();
   }

   private void sphereQuad(VertexConsumer vc, Matrix4f m, float r, float wx, float wy, float wz, float p0, float p1, float a0, float a1, int cr, int cg, int cb, int alpha, int light) {
      float y0 = Mth.sin(p0) * r * wy;
      float y1 = Mth.sin(p1) * r * wy;
      float c0 = Mth.cos(p0) * r;
      float c1 = Mth.cos(p1) * r;
      float x00 = Mth.cos(a0) * c0 * wx;
      float z00 = Mth.sin(a0) * c0 * wz;
      float x01 = Mth.cos(a1) * c0 * wx;
      float z01 = Mth.sin(a1) * c0 * wz;
      float x10 = Mth.cos(a0) * c1 * wx;
      float z10 = Mth.sin(a0) * c1 * wz;
      float x11 = Mth.cos(a1) * c1 * wx;
      float z11 = Mth.sin(a1) * c1 * wz;
      this.quad(vc, m, x00, y0, z00, x01, y0, z01, x11, y1, z11, x10, y1, z10, cr, cg, cb, alpha, light);
      this.quad(vc, m, x10, y1, z10, x11, y1, z11, x01, y0, z01, x00, y0, z00, cr, cg, cb, alpha, light);
   }

   private void renderIce(PoseStack pose, VertexConsumer vc, LivingEntity victim, float t, int light) {
      Matrix4f m = pose.last().pose();
      float base = Math.max(0.45F, victim.getBbWidth() * 0.85F);
      float disc = base + 0.25F;
      int shimmer = (int)(16.0F * Mth.sin(t * 0.2F));
      this.quad(vc, m, -disc, 0.03F, -disc, -disc, 0.03F, disc, disc, 0.03F, disc, disc, 0.03F, -disc, 222, 240, 252, 165 + shimmer, light);
      this.quad(vc, m, disc, 0.031F, -disc, disc, 0.031F, disc, -disc, 0.031F, disc, -disc, 0.031F, -disc, 222, 240, 252, 165 + shimmer, light);
      int shards = 6;

      for(int k = 0; k < shards; ++k) {
         float ang = (float)((Math.PI * 2D) * (double)k / (double)shards) + 0.35F;
         float h = k % 2 == 0 ? victim.getBbHeight() * 0.62F : victim.getBbHeight() * 0.4F;
         float rad = base * (k % 2 == 0 ? 0.85F : 1.0F);
         this.shard(vc, m, Mth.cos(ang) * rad, Mth.sin(ang) * rad, h, 0.16F, 0.35F, light, shimmer);
      }

      this.shard(vc, m, 0.12F, 0.1F, victim.getBbHeight() * 0.85F, 0.2F, 0.15F, light, shimmer);
   }

   private void shard(VertexConsumer vc, Matrix4f m, float cx, float cz, float h, float w, float lean, int light, int shimmer) {
      float tx = cx * (1.0F - lean);
      float tz = cz * (1.0F - lean);
      int baseA = 205 + shimmer;

      for(int f = 0; f < 4; ++f) {
         float a0 = (float)((Math.PI / 2D) * (double)f + (Math.PI / 4D));
         float a1 = (float)((Math.PI / 2D) * (double)(f + 1) + (Math.PI / 4D));
         float x0 = cx + Mth.cos(a0) * w;
         float z0 = cz + Mth.sin(a0) * w;
         float x1 = cx + Mth.cos(a1) * w;
         float z1 = cz + Mth.sin(a1) * w;
         int shade = 190 + (int)(40.0F * Mth.cos(a0));
         this.quad(vc, m, x0, 0.0F, z0, x1, 0.0F, z1, tx, h, tz, tx, h, tz, shade, 225, 250, baseA, light);
      }

   }

   private void renderSand(PoseStack pose, VertexConsumer vc, LivingEntity victim, float t, int light) {
      Matrix4f m = pose.last().pose();
      float rBase = victim.getBbWidth() * 0.85F + 0.2F;
      float h = victim.getBbHeight() * 1.15F;
      this.curtain(vc, m, rBase, h, 10, 9, t * 0.08F, 222, 178, 96, 240, light);
      this.curtain(vc, m, rBase + 0.14F, h * 0.97F, 10, 8, -t * 0.06F + 1.3F, 206, 158, 78, 190, light);
      this.curtain(vc, m, rBase + 0.28F, h * 0.9F, 12, 7, t * 0.045F + 2.6F, 178, 128, 62, 140, light);
      int vortex = 8;

      for(int k = 0; k < vortex; ++k) {
         float ang_0 = (float)((Math.PI * 2D) * (double)k / (double)vortex) - t * 0.16F;
         float f = (float)k / (float)vortex;
         float cy_0 = h + 0.1F + f * 0.001F + 0.12F * Mth.sin(t * 0.2F + (float)k);
         float rad_0 = rBase * (0.75F - 0.25F * Mth.sin(t * 0.1F + (float)k * 0.7F));
         float cx_0 = Mth.cos(ang_0 + t * 0.05F) * rad_0;
         float cz_0 = Mth.sin(ang_0 + t * 0.05F) * rad_0;
         float s_0 = 0.1F;
         this.quad(vc, m, cx_0 - s_0, cy_0 - s_0, cz_0, cx_0 - s_0, cy_0 + s_0, cz_0, cx_0 + s_0, cy_0 + s_0, cz_0, cx_0 + s_0, cy_0 - s_0, cz_0, 226, 186, 108, 220, light);
         this.quad(vc, m, cx_0 + s_0, cy_0 - s_0, cz_0, cx_0 + s_0, cy_0 + s_0, cz_0, cx_0 - s_0, cy_0 + s_0, cz_0, cx_0 - s_0, cy_0 - s_0, cz_0, 226, 186, 108, 220, light);
      }

      int chunks = 9;

      for(int k = 0; k < chunks; ++k) {
         float ang = (float)((Math.PI * 2D) * (double)k / (double)chunks) + t * 0.11F * (k % 2 == 0 ? 1.0F : -0.8F);
         float cy = 0.2F + (float)(k % 4) * h * 0.24F + 0.1F * Mth.sin(t * 0.2F + (float)k * 1.7F);
         float rad = rBase + 0.26F + 0.05F * Mth.sin(t * 0.15F + (float)k);
         float cx = Mth.cos(ang) * rad;
         float cz = Mth.sin(ang) * rad;
         float s = 0.11F + 0.04F * (float)(k % 2);
         int cr = k % 3 == 0 ? 150 : 222;
         int cg = k % 3 == 0 ? 72 : 178;
         int cb = k % 3 == 0 ? 40 : 96;
         this.quad(vc, m, cx - s, cy - s, cz, cx - s, cy + s, cz, cx + s, cy + s, cz, cx + s, cy - s, cz, cr, cg, cb, 245, light);
         this.quad(vc, m, cx + s, cy - s, cz, cx + s, cy + s, cz, cx - s, cy + s, cz, cx - s, cy - s, cz, cr, cg, cb, 245, light);
      }

   }

   private void curtain(VertexConsumer vc, Matrix4f m, float radius, float height, int segments, int drawn, float spin, int r, int g, int b, int a, int light) {
      float taper = 0.85F;

      for(int i = 0; i < drawn; ++i) {
         float a0 = (float)((Math.PI * 2D) * (double)i / (double)segments) + spin;
         float a1 = (float)((Math.PI * 2D) * (double)((float)i + 0.8F) / (double)segments) + spin;
         float x0b = Mth.cos(a0) * radius;
         float z0b = Mth.sin(a0) * radius;
         float x1b = Mth.cos(a1) * radius;
         float z1b = Mth.sin(a1) * radius;
         float x0t = Mth.cos(a0) * radius * taper;
         float z0t = Mth.sin(a0) * radius * taper;
         float x1t = Mth.cos(a1) * radius * taper;
         float z1t = Mth.sin(a1) * radius * taper;
         int shade = 200 + (int)(30.0F * Mth.sin(a0));
         int rr = r * shade / 230;
         int gg = g * shade / 230;
         int bb = b * shade / 230;
         this.quad(vc, m, x0b, 0.0F, z0b, x1b, 0.0F, z1b, x1t, height, z1t, x0t, height, z0t, rr, gg, bb, a, light);
         this.quad(vc, m, x0t, height, z0t, x1t, height, z1t, x1b, 0.0F, z1b, x0b, 0.0F, z0b, rr, gg, bb, a, light);
      }

   }

   private void quad(VertexConsumer vc, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int b, int a, int light) {
      vc.vertex(m, x0, y0, z0).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
      vc.vertex(m, x1, y1, z1).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
      vc.vertex(m, x2, y2, z2).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
      vc.vertex(m, x3, y3, z3).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
   }
}
