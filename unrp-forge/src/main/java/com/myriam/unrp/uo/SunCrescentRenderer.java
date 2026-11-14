package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SunCrescentRenderer extends EntityRenderer<SunCrescentEntity> {
   public static final int SEGS = 16;
   public static final double WIDTH = 1.9D;
   public static final double WAKE = 2.6D;

   public SunCrescentRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(SunCrescentEntity entity) {
      return DaystarDraw.FX;
   }

   public boolean shouldRender(SunCrescentEntity entity, Frustum frustum, double x, double y, double z) {
      return entity.shouldRender(x, y, z);
   }

   static Vec3 point(Vec3 f, Vec3 r, Vec3 u, double span, double th) {
      double c = Math.cos(1.35D);
      double lead = (Math.cos(th) - c) / (1.0D - c);
      return r.scale(span * Math.sin(th) / Math.sin(1.35D)).add(u.scale(span * 0.45D * lead)).subtract(f.scale(span * 0.35D * (1.0D - lead)));
   }

   static double taper(double th) {
      return 0.3D + 0.7D * Math.cos(th / 1.35D * Math.PI * 0.5D);
   }

   public void render(SunCrescentEntity e, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
      super.render(e, yaw, pt, pose, buffers, light);
      Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
      Vector3f l = camera.getLeftVector();
      Vector3f cu = camera.getUpVector();
      Vec3 camRight = new Vec3((double)(-l.x()), (double)(-l.y()), (double)(-l.z()));
      Vec3 camUp = new Vec3((double)cu.x(), (double)cu.y(), (double)cu.z());
      Vec3 eyeRel = camera.getPosition().subtract(e.getPosition(pt));
      DaystarDraw.normals(pose.last().normal());
      Matrix4f m = pose.last().pose();
      double age = (double)((float)e.tickCount + pt);
      double t = age;
      int stop = e.stop();
      float out = stop < 0 ? 1.0F : DaystarDraw.clamp01(1.0D - e.stopAge(age) / 8.0D);
      float in = DaystarDraw.outCubic(age / 2.0D);
      float a = in * out;
      if (!(a <= 0.003F)) {
         float g = e.gauge();
         Vec3 f = e.heading();
         Vec3 u = e.up();
         Vec3 r = e.right();
         double span = (double)e.span() * (0.6D + 0.4D * (double)in) * (stop < 0 ? 1.0D : 1.0D + 0.35D * (1.0D - (double)out));
         double wake = 2.6D * (0.5D + 0.5D * (double)in) * (stop < 0 ? 1.0D : (double)out);
         Vec3[] arc = new Vec3[17];
         double[] wid = new double[17];

         for(int s = 0; s <= 16; ++s) {
            double th_0 = -1.35D + 2.7D * (double)s / 16.0D;
            arc[s] = point(f, r, u, span, th_0);
            wid[s] = taper(th_0);
         }

         VertexConsumer soft = buffers.getBuffer(DaystarDraw.soft());

         for(int s = 0; s < 16; ++s) {
            Vec3 b0_0 = arc[s].subtract(f.scale(wake * wid[s]));
            Vec3 b1_0 = arc[s + 1].subtract(f.scale(wake * wid[s + 1]));
            DaystarDraw.quad(soft, m, arc[s], arc[s + 1], b1_0, b0_0, 9, 1.0F, 0.52F, 0.13F, 0.85F * a, false);
         }

         for(int s = 0; s < 16; ++s) {
            DaystarDraw.ribbon(soft, m, arc[s], arc[s + 1], eyeRel, 0.95D * (wid[s] + wid[s + 1]), 8, 1.0F, 0.58F, 0.16F, 0.9F * a, false);
         }

         VertexConsumer add = buffers.getBuffer(DaystarDraw.add());

         for(int s = 0; s < 16; ++s) {
            Vec3 b0 = arc[s].subtract(f.scale(wake * 0.7D * wid[s]));
            Vec3 b1 = arc[s + 1].subtract(f.scale(wake * 0.7D * wid[s + 1]));
            DaystarDraw.quad(add, m, arc[s], arc[s + 1], b1, b0, 9, 1.0F, 0.72F, 0.32F, 0.55F * a * (0.8F + 0.2F * g), true);
         }

         for(int s = 0; s < 16; ++s) {
            double w = 0.5D * (wid[s] + wid[s + 1]);
            DaystarDraw.ribbon(add, m, arc[s], arc[s + 1], eyeRel, 1.4249999999999998D * w, 8, 1.0F, 0.7F, 0.3F, 0.75F * a, true);
            DaystarDraw.ribbon(add, m, arc[s], arc[s + 1], eyeRel, 0.722D * w, 13, 1.0F, 0.94F, 0.72F, 0.95F * a, true);
         }

         for(int k = 0; k < 5; ++k) {
            double th_1 = -1.08D + 2.16D * (double)k / 4.0D;
            DaystarDraw.bill(add, m, point(f, r, u, span, th_1), camRight, camUp, span * 0.6D * taper(th_1), 0.0D, 5, 1.0F, 0.55F, 0.18F, 0.45F * a, true);
         }

         for(int k = 0; k < 12; ++k) {
            double ph = (t * 0.11D + (double)k * 0.083D) % 1.0D;
            double th = (DaystarDraw.hash(k, 5, 43) * 2.0D - 1.0D) * 1.35D;
            Vec3 c = point(f, r, u, span, th).subtract(f.scale(0.4D + ph * 3.2D)).add(u.scale((DaystarDraw.hash(k, 6, 43) - 0.5D) * 0.8D));
            DaystarDraw.bill(add, m, c, camRight, camUp, 0.07D, 0.0D, 7, 1.0F, 0.75F, 0.35F, (float)(1.0D - ph) * a, true);
         }

         DaystarDraw.bill(add, m, u.scale(span * 0.45D * 0.6D), camRight, camUp, span * 1.4D, t * 0.02D, 3, 1.0F, 0.85F, 0.6F, 0.12F * a, true);
         if (stop >= 0) {
            float b = DaystarDraw.clamp01(1.0D - e.stopAge(age) / 8.0D);
            double grow = 1.0D - (double)b;
            DaystarDraw.bill(add, m, Vec3.ZERO, camRight, camUp, span * (0.8D + 1.4D * grow), t * 0.06D, 1, 1.0F, 0.75F, 0.35F, 0.85F * b, true);
            DaystarDraw.bill(add, m, Vec3.ZERO, camRight, camUp, span * (1.2D + 1.6D * grow), 0.0D, 5, 1.0F, 0.6F, 0.2F, 0.7F * b, true);
         }

      }
   }
}
