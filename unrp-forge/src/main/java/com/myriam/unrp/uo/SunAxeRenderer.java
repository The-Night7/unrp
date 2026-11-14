package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SunAxeRenderer extends EntityRenderer<SunAxeEntity> {
   public static final float SIZE = 2.2F;
   public static final double SPIN = 60.0D;
   public static final double SPIN0 = 55.0D;
   public static final double BANK = 40.0D;
   public static final double WHEEL = 1.1D;
   public static final double TIP_U = 10.5D;
   public static final double TIP_V = 4.0D;
   public static final double STEP = 0.25D;
   public static final int STEPS = 16;
   public static final double NEAR = 1.2D;
   public static final double NEAR_FADE = 2.0D;
   public static final double HALO = 1.3D;
   public static final double BEHIND = 1.2D;
   public static final double HEAD = 0.8D;
   private final ItemRenderer items;
   private ItemStack axe = ItemStack.EMPTY;

   public SunAxeRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.items = context.getItemRenderer();
   }

   public ResourceLocation getTextureLocation(SunAxeEntity entity) {
      return DaystarDraw.FX;
   }

   public boolean shouldRender(SunAxeEntity entity, Frustum frustum, double x, double y, double z) {
      return entity.shouldRender(x, y, z);
   }

   public static Vec3[] frame(double yaw, double bank) {
      double r = Math.toRadians(yaw);
      double b = Math.toRadians(bank);
      return new Vec3[]{new Vec3(-Math.sin(r), 0.0D, Math.cos(r)), new Vec3(-Math.cos(r) * Math.sin(b), Math.cos(b), -Math.sin(r) * Math.sin(b))};
   }

   private static float near(Vec3 eye, Vec3 p) {
      return DaystarDraw.clamp01((eye.distanceTo(p) - 1.2D) / 2.0D);
   }

   public void render(SunAxeEntity e, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
      super.render(e, yaw, pt, pose, buffers, light);
      if (this.axe.isEmpty()) {
         this.axe = new ItemStack(com.myriam.unrp.item.ModItems.SUNSTEEL_AXE.get());
      }

      double age = (double)((float)e.tickCount + pt);
      float in = DaystarDraw.clamp01(age / 2.0D);
      float sun = 0.6F + 0.4F * DaystarDraw.clamp01((double)e.gauge());
      double back0 = 1.0D - (double)pt;
      float turn = e.pastYaw(back0);
      Vec3 origin = e.past(back0);
      float bank = (float)(40.0D * (double)in);
      double spin = 55.0D + age * 60.0D;
      float sz = 2.2F * (0.7F + 0.3F * in);
      pose.pushPose();

      try {
         pose.mulPose(Axis.YP.rotationDegrees(-turn));
         pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
         pose.mulPose(Axis.XP.rotationDegrees(bank));
         pose.mulPose(Axis.ZP.rotationDegrees((float)(-spin)));
         pose.scale(sz, sz, sz);
         this.items.renderStatic(this.axe, ItemDisplayContext.FIXED, 15728880, OverlayTexture.NO_OVERLAY, pose, buffers, e.level(), e.getId());
      } finally {
         pose.popPose();
      }

      Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
      Vector3f l = camera.getLeftVector();
      Vector3f u = camera.getUpVector();
      Vec3 camRight = new Vec3((double)(-l.x()), (double)(-l.y()), (double)(-l.z()));
      Vec3 camUp = new Vec3((double)u.x(), (double)u.y(), (double)u.z());
      Vec3 eye = camera.getPosition().subtract(e.getPosition(pt));
      DaystarDraw.normals(pose.last().normal());
      Matrix4f m = pose.last().pose();
      Vec3[] cen = new Vec3[17];
      Vec3[] tip = new Vec3[17];
      float[] al = new float[17];
      int n = 0;

      for(int near0 = 0; near0 <= 16; ++near0) {
         double b_0 = back0 + (double)near0 * 0.25D;
         double a_0 = age - (double)near0 * 0.25D;
         if (b_0 > e.pastLimit() + 1.0E-6D || a_0 < 0.0D) {
            break;
         }

         Vec3 p = e.past(b_0).subtract(origin);
         double aj = (double)DaystarDraw.clamp01(a_0 / 2.0D);
         Vec3[] fr = frame((double)e.pastYaw(b_0), 40.0D * aj);
         double s = Math.toRadians(55.0D + a_0 * 60.0D);
         double tx = 0.37812500819563866D * (0.7D + 0.3D * aj);
         double ty = 0.8250000178813934D * (0.7D + 0.3D * aj);
         cen[n] = p;
         tip[n] = p.add(fr[0].scale(tx * Math.cos(s) + ty * Math.sin(s))).add(fr[1].scale(-tx * Math.sin(s) + ty * Math.cos(s)));
         al[n] = (float)(1.0D - (double)near0 / 16.0D) * in * sun * near(eye, p);
         ++n;
      }

      float near0 = near(eye, Vec3.ZERO) * sun;
      float[] sal = new float[n];

      for(int j = 0; j < n; ++j) {
         sal[j] = al[j] * DaystarDraw.clamp01(cen[j].length() / 0.8D);
      }

      Vec3[] f0 = frame((double)turn, (double)bank);
      double dcam = eye.length();
      Vec3 behind = dcam > 0.001D ? eye.scale(-1.2D / dcam) : Vec3.ZERO;
      double grow = dcam > 0.001D ? (dcam + 1.2D) / dcam : 1.0D;
      VertexConsumer soft = buffers.getBuffer(DaystarDraw.soft());
      if (n >= 2) {
         stream(soft, m, cen, sal, n, eye, 1.0D, 1.0F, 0.42F, 0.08F, 0.85F, false);
      }

      DaystarDraw.plane(soft, m, Vec3.ZERO, f0[0], f0[1], 1.3D * (double)in, 0.0D, 5, 1.0F, 0.5F, 0.12F, 0.45F * near0 * in, false);
      DaystarDraw.bill(soft, m, behind, camRight, camUp, 0.8D * grow * (double)in, 0.0D, 5, 1.0F, 0.45F, 0.1F, 0.5F * near0 * in, false);
      VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
      if (n >= 2) {
         stream(add, m, cen, sal, n, eye, 0.7D, 1.0F, 0.68F, 0.28F, 0.7F, true);

         for(int j = 0; j + 1 < n; ++j) {
            float a = 0.5F * (al[j] + al[j + 1]);
            DaystarDraw.ribbon(add, m, tip[j], tip[j + 1], eye, 0.12D + 0.3D * (double)a, 13, 1.0F, 0.7F, 0.3F, 0.75F * a, true);
            DaystarDraw.ribbon(add, m, tip[j], tip[j + 1], eye, 0.07D + 0.08D * (double)a, 13, 1.0F, 0.95F, 0.8F, 0.95F * a, true);
         }
      }

      double rad = Math.toRadians(spin);
      DaystarDraw.plane(add, m, Vec3.ZERO, f0[0], f0[1], 1.1D * (double)in, -rad, 4, 1.0F, 0.72F, 0.3F, 0.45F * in * sun, true);
      DaystarDraw.plane(add, m, Vec3.ZERO, f0[0], f0[1], 0.935D * (double)in, -rad * 1.3D, 1, 1.0F, 0.8F, 0.4F, 0.3F * in * sun, true);
      DaystarDraw.plane(add, m, Vec3.ZERO, f0[0], f0[1], 1.56D * (double)in, 0.0D, 5, 1.0F, 0.6F, 0.2F, 0.4F * near0 * in, true);
      DaystarDraw.bill(add, m, behind, camRight, camUp, 1.15D * grow * (double)in, 0.0D, 5, 1.0F, 0.6F, 0.2F, 0.55F * near0 * in, true);

      for(int k = 0; k < 10; ++k) {
         double h1 = DaystarDraw.hash(k, 3, 266);
         double h2 = DaystarDraw.hash(k, 4, 266);
         double h3 = DaystarDraw.hash(k, 5, 266);
         double ph = (age * 0.11D + h1) % 1.0D;
         double b = back0 + ph * 3.0D;
         if (!(b > e.pastLimit())) {
            Vec3 q = e.past(b).subtract(origin).add((h2 - 0.5D) * 0.6D * ph, ph * 0.5D, (h3 - 0.5D) * 0.6D * ph);
            DaystarDraw.bill(add, m, q, camRight, camUp, 0.06D, 0.0D, 7, 1.0F, 0.75F, 0.35F, (float)(1.0D - ph) * in * sun * near(eye, q), true);
         }
      }

   }

   private static void stream(VertexConsumer vc, Matrix4f m, Vec3[] p, float[] al, int n, Vec3 eye, double scale, float r, float g, float b, float k, boolean additive) {
      Vec3[] side = new Vec3[n];

      for(int j = 0; j < n; ++j) {
         Vec3 tg = p[Math.max(0, j - 1)].subtract(p[Math.min(n - 1, j + 1)]);
         Vec3 sd = tg.cross(eye.subtract(p[j]));
         side[j] = sd.lengthSqr() < 1.0E-12D ? Vec3.ZERO : sd.normalize();
      }

      for(int j = 0; j + 1 < n; ++j) {
         double w0 = (0.12D + 0.5D * (1.0D - (double)j / (double)(n - 1))) * scale;
         double w1 = (0.12D + 0.5D * (1.0D - (double)(j + 1) / (double)(n - 1))) * scale;
         float s0 = 1.0F - (float)j / (float)(n - 1);
         float s1 = 1.0F - (float)(j + 1) / (float)(n - 1);
         DaystarDraw.strip(vc, m, p[j].subtract(side[j].scale(w0)), p[j + 1].subtract(side[j + 1].scale(w1)), p[j + 1].add(side[j + 1].scale(w1)), p[j].add(side[j].scale(w0)), 9, true, s0, s1, r, g, b, k * al[j], k * al[j + 1], additive);
      }

   }
}
