package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class HomingBeamRenderer extends EntityRenderer<HomingBeamEntity> {
   private static final ResourceLocation BEAM = new ResourceLocation("unrp", "textures/gui/beam_glow.png");
   private static final int FULL_BRIGHT = 15728880;
   public static final float NEAR = 0.8F;
   public static final float NEAR_FADE = 2.4F;
   public static final float FLARE_NEAR = 1.5F;
   public static final float FLARE_FADE = 3.0F;
   public static final float TAIL_FADE = 0.3F;
   public static final int FLARE_WEDGES = 24;
   private float spent = 1.0F;

   public HomingBeamRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public boolean shouldRender(HomingBeamEntity beam, Frustum frustum, double camX, double camY, double camZ) {
      return true;
   }

   public void render(HomingBeamEntity beam, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      this.spent = 1.0F - Math.min(1.0F, beam.spentFor(partialTick) / 6.0F);
      if (!(this.spent <= 0.0F)) {
         this.renderBeam(beam, partialTick, poseStack, buffer);
      }
   }

   static float near(Vec3 p, Vec3 cam, float from, float span) {
      return (float)Math.max(0.0D, Math.min(1.0D, (p.distanceTo(cam) - (double)from) / (double)span));
   }

   static float tail(float t) {
      return Math.min(1.0F, t / 0.3F);
   }

   private void renderBeam(HomingBeamEntity beam, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
      List<Vec3> points = new ArrayList();
      double hx = Mth.lerp((double)partialTick, beam.xOld, beam.getX());
      double hy = Mth.lerp((double)partialTick, beam.yOld, beam.getY());
      double hz = Mth.lerp((double)partialTick, beam.zOld, beam.getZ());
      Vec3 head = new Vec3(hx, hy, hz);
      points.add(head);
      points.addAll(beam.clientTrail);
      if (points.size() >= 2) {
         Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
         int color = beam.getColor();
         float or = (float)(color >> 16 & 255) / 255.0F;
         float og = (float)(color >> 8 & 255) / 255.0F;
         float ob = (float)(color & 255) / 255.0F;
         float w = beam.getBeamWidth();
         VertexConsumer vc = buffer.getBuffer(RenderType.eyes(BEAM));
         PoseStack.Pose pose = poseStack.last();
         boolean wave = beam.isWaveVisual();
         float t = (float)beam.tickCount + partialTick;
         if (wave) {
            this.ribbon(vc, pose, points, head, cam, w * 1.9F, or, og, ob, 0.22F, 0.75F);
            this.ribbon(vc, pose, points, head, cam, w * 1.15F, or, og, ob, 0.8F, 0.8F);
            this.ribbon(vc, pose, points, head, cam, w * 0.62F, or * 0.45F + 0.55F, og * 0.45F + 0.55F, ob * 0.45F + 0.55F, 0.92F, 0.85F);
            this.ribbon(vc, pose, points, head, cam, w * 0.32F, 1.0F, 1.0F, 1.0F, 1.0F, 0.9F);
            this.helix(vc, pose, points, head, cam, w * 1.25F, t, or, og, ob);
            this.flare(vc, pose, head, cam, w * (2.1F + 0.25F * (float)Math.sin((double)(t * 0.7F))), or, og, ob);
         } else {
            this.ribbon(vc, pose, points, head, cam, w * 1.5F, or, og, ob, 0.3F, 0.0F);
            this.ribbon(vc, pose, points, head, cam, w, or, og, ob, 0.9F, 0.0F);
            this.ribbon(vc, pose, points, head, cam, w * 0.4F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F);
         }

      }
   }

   private void ribbon(VertexConsumer vc, PoseStack.Pose pose, List<Vec3> points, Vec3 origin, Vec3 cam, float width, float r, float g, float b, float alpha, float floorTaper) {
      int n = points.size();

      for(int i = 0; i < n - 1; ++i) {
         Vec3 p0 = (Vec3)points.get(i);
         Vec3 p1 = (Vec3)points.get(i + 1);
         Vec3 seg = p1.subtract(p0);
         if (!(seg.lengthSqr() < 1.0E-6D)) {
            Vec3 toCam = cam.subtract(p0);
            Vec3 side = seg.cross(toCam).normalize();
            if (!(side.lengthSqr() < 1.0E-6D)) {
               float t0 = 1.0F - (float)i / (float)n;
               float t1 = 1.0F - (float)(i + 1) / (float)n;
               float w0 = width * (floorTaper + (1.0F - floorTaper) * t0);
               float w1 = width * (floorTaper + (1.0F - floorTaper) * t1);
               float k0 = alpha * this.spent * tail(t0) * near(p0, cam, 0.8F, 2.4F);
               float k1 = alpha * this.spent * tail(t1) * near(p1, cam, 0.8F, 2.4F);
               Vec3 c0 = p0.subtract(origin);
               Vec3 c1 = p1.subtract(origin);
               Vec3 a = c0.add(side.scale((double)w0));
               Vec3 c = c0.subtract(side.scale((double)w0));
               Vec3 d = c1.add(side.scale((double)w1));
               Vec3 e = c1.subtract(side.scale((double)w1));
               this.glowQuad(vc, pose, a, d, c1, c0, r, g, b, 0.0F, 0.0F, k1, k0);
               this.glowQuad(vc, pose, c0, c1, e, c, r, g, b, k0, k1, 0.0F, 0.0F);
            }
         }
      }

   }

   private void helix(VertexConsumer vc, PoseStack.Pose pose, List<Vec3> points, Vec3 origin, Vec3 cam, float radius, float t, float r, float g, float b) {
      int n = points.size();

      for(int i = 0; i < n - 1; ++i) {
         Vec3 p0 = (Vec3)points.get(i);
         Vec3 p1 = (Vec3)points.get(i + 1);
         Vec3 seg = p1.subtract(p0);
         if (!(seg.lengthSqr() < 1.0E-6D)) {
            Vec3 dir = seg.normalize();
            Vec3 up0 = Math.abs(dir.y) > 0.95D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 sideA = dir.cross(up0).normalize();
            Vec3 sideB = dir.cross(sideA).normalize();

            for(int strand = 0; strand < 2; ++strand) {
               double a0 = (double)t * 0.5D + (double)i * 0.9D + (double)strand * Math.PI;
               double a1 = a0 + 0.9D;
               Vec3 h0 = p0.add(sideA.scale(Math.cos(a0) * (double)radius)).add(sideB.scale(Math.sin(a0) * (double)radius)).subtract(origin);
               Vec3 h1 = p1.add(sideA.scale(Math.cos(a1) * (double)radius)).add(sideB.scale(Math.sin(a1) * (double)radius)).subtract(origin);
               Vec3 toCam = cam.subtract(p0);
               Vec3 side = seg.cross(toCam).normalize().scale((double)radius * 0.1D);
               if (!(side.lengthSqr() < 1.0E-8D)) {
                  float k0 = 0.75F * this.spent * tail(1.0F - (float)i / (float)n) * near(p0, cam, 0.8F, 2.4F);
                  float k1 = 0.75F * this.spent * tail(1.0F - (float)(i + 1) / (float)n) * near(p1, cam, 0.8F, 2.4F);
                  this.glowQuad(vc, pose, h0.add(side), h1.add(side), h1.subtract(side), h0.subtract(side), r * 0.5F + 0.5F, g * 0.5F + 0.5F, b * 0.5F + 0.5F, k0, k1, k1, k0);
               }
            }
         }
      }

   }

   private void flare(VertexConsumer vc, PoseStack.Pose pose, Vec3 head, Vec3 cam, float radius, float r, float g, float b) {
      Vec3 toCam = cam.subtract(head);
      if (!(toCam.lengthSqr() < 1.0E-4D)) {
         float seen = this.spent * near(head, cam, 1.5F, 3.0F);
         if (!(seen <= 0.0F)) {
            Vec3 fwd = toCam.normalize();
            Vec3 up0 = Math.abs(fwd.y) > 0.95D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 right = fwd.cross(up0).normalize();
            Vec3 up = right.cross(fwd).normalize();

            for(float[] layer : new float[][]{{1.0F, 0.35F}, {0.62F, 0.7F}, {0.34F, 0.95F}}) {
               float white = layer[0] < 0.5F ? 1.0F : 0.0F;
               float cr = r + (1.0F - r) * white;
               float cg = g + (1.0F - g) * white;
               float cb = b + (1.0F - b) * white;
               float k = layer[1] * seen;

               for(int i = 0; i < 24; ++i) {
                  double a0 = (Math.PI * 2D) * (double)i / 24.0D;
                  double a1 = (Math.PI * 2D) * (double)(i + 1) / 24.0D;
                  Vec3 p0 = right.scale(Math.cos(a0) * (double)radius * (double)layer[0]).add(up.scale(Math.sin(a0) * (double)radius * (double)layer[0]));
                  Vec3 p1 = right.scale(Math.cos(a1) * (double)radius * (double)layer[0]).add(up.scale(Math.sin(a1) * (double)radius * (double)layer[0]));
                  this.glowQuad(vc, pose, Vec3.ZERO, Vec3.ZERO, p1, p0, cr, cg, cb, k, k, 0.0F, 0.0F);
               }
            }

         }
      }
   }

   private void glowQuad(VertexConsumer vc, PoseStack.Pose pose, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, float r, float g, float b, float k1, float k2, float k3, float k4) {
      this.vertex(vc, pose, v1, r, g, b, k1);
      this.vertex(vc, pose, v2, r, g, b, k2);
      this.vertex(vc, pose, v3, r, g, b, k3);
      this.vertex(vc, pose, v4, r, g, b, k4);
      this.vertex(vc, pose, v4, r, g, b, k4);
      this.vertex(vc, pose, v3, r, g, b, k3);
      this.vertex(vc, pose, v2, r, g, b, k2);
      this.vertex(vc, pose, v1, r, g, b, k1);
   }

   private void vertex(VertexConsumer vc, PoseStack.Pose pose, Vec3 v, float r, float g, float b, float k) {
      float lit = Math.max(0.0F, Math.min(1.0F, k));
      vc.vertex(pose.pose(), (float)v.x, (float)v.y, (float)v.z).color(r * lit, g * lit, b * lit, lit).uv(0.5F, 0.5F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
   }

   public ResourceLocation getTextureLocation(HomingBeamEntity entity) {
      return BEAM;
   }
}
