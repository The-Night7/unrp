package com.myriam.unrp.uo;

import com.myriam.unrp.pass.ViewUp;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class ArsenalDraw {
   public static final ResourceLocation GATE = new ResourceLocation("unrp", "textures/entity/arsenal_gate.png");
   public static final ResourceLocation GATE_ADD = new ResourceLocation("unrp", "textures/entity/arsenal_gate_add.png");
   public static final int RIPPLE = 0;
   public static final int RUNES = 1;
   public static final int HALO = 2;
   public static final int STREAK = 3;
   public static final int STAR = 4;
   public static final int CORE = 5;
   public static final int THIN = 6;
   public static final int SPARK = 7;
   public static final float GR = 1.0F;
   public static final float GG = 0.8F;
   public static final float GB = 0.36F;
   public static final float RUNE_RING = 1.06F;
   public static final int WAVES = 3;
   private static final int LIGHT = 15728880;

   private ArsenalDraw() {
   }

   public static RenderType soft() {
      return RenderType.entityTranslucentEmissive(GATE);
   }

   public static RenderType add() {
      return RenderType.eyes(GATE_ADD);
   }

   public static float clamp01(float x) {
      return x < 0.0F ? 0.0F : (x > 1.0F ? 1.0F : x);
   }

   public static float out(float x) {
      float c = clamp01(x);
      float k = 1.0F - c;
      return 1.0F - k * k * k;
   }

   public static float back(float x) {
      float c = clamp01(x) - 1.0F;
      return 1.0F + c * c * (2.7F * c + 1.7F);
   }

   public static float gateSize(float age, float fire) {
      float open = back(age / 5.0F);
      float shut = age > fire ? clamp01((age - fire) / 6.0F) : 0.0F;
      return Math.max(0.0F, open) * (1.0F - shut * shut);
   }

   public static float gateFlash(float age, float fire) {
      return age >= fire ? Math.max(0.0F, 1.0F - (age - fire) / 3.0F) : clamp01((age - (fire - 2.0F)) / 2.0F);
   }

   public static float emerge(float age) {
      return clamp01((age - 1.5F) / 6.0F);
   }

   public static float bladeScale(float e) {
      return 1.15F * (0.35F + 0.65F * out(e));
   }

   public static double bladeOffset(float e, float age, float roll) {
      double bob = e >= 1.0F ? 0.035D * Math.sin((double)(age * 0.55F + roll)) : 0.0D;
      return 0.7071D * (double)bladeScale(e) * ((double)out(e) * 0.9D - 0.35D) + bob;
   }

   public static double streakLength(boolean shot, double speed, float age, float trail) {
      return Math.min(shot ? 3.6D : 2.6D, speed * (double)age * 0.9D) * (double)trail;
   }

   public static Vec3[] basis(Vec3 n) {
      Vec3 up0 = Math.abs(n.y) > 0.97D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
      Vec3 right = n.cross(up0).normalize();
      Vec3 up = right.cross(n).normalize();
      return new Vec3[]{right, up};
   }

   private static void vtx(VertexConsumer vc, Matrix4f m, Vec3 p, float u, float v, float r, float g, float b, float a) {
      vc.vertex(m, (float)p.x, (float)p.y, (float)p.z).color(r, g, b, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
   }

   private static void quad(VertexConsumer vc, Matrix4f m, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, int cell, float r, float g, float b, float a, boolean additive) {
      if (!(a <= 0.003F)) {
         if (additive) {
            r *= a;
            g *= a;
            b *= a;
         }

         float u0 = (float)(cell % 4) * 0.25F;
         float v0 = (float)(cell / 4) * 0.5F;
         float u1 = u0 + 0.25F;
         float v1 = v0 + 0.5F;
         vtx(vc, m, p1, u0, v1, r, g, b, a);
         vtx(vc, m, p2, u1, v1, r, g, b, a);
         vtx(vc, m, p3, u1, v0, r, g, b, a);
         vtx(vc, m, p4, u0, v0, r, g, b, a);
         vtx(vc, m, p4, u0, v0, r, g, b, a);
         vtx(vc, m, p3, u1, v0, r, g, b, a);
         vtx(vc, m, p2, u1, v1, r, g, b, a);
         vtx(vc, m, p1, u0, v1, r, g, b, a);
      }
   }

   public static void plane(VertexConsumer vc, Matrix4f m, Vec3 c, Vec3 ax, Vec3 ay, float size, float spin, int cell, float r, float g, float b, float a, boolean additive) {
      double cs = Math.cos((double)spin);
      double sn = Math.sin((double)spin);
      Vec3 rr = ax.scale(cs).add(ay.scale(sn)).scale((double)size);
      Vec3 uu = ay.scale(cs).subtract(ax.scale(sn)).scale((double)size);
      quad(vc, m, c.subtract(rr).subtract(uu), c.add(rr).subtract(uu), c.add(rr).add(uu), c.subtract(rr).add(uu), cell, r, g, b, a, additive);
   }

   public static void ribbon(VertexConsumer vc, Matrix4f m, Vec3 a, Vec3 b, Vec3 eye, float w, int cell, float r, float g, float bl, float alpha, boolean additive) {
      Vec3 d = b.subtract(a);
      Vec3 mid = a.add(b).scale(0.5D);
      Vec3 side = d.cross(eye.subtract(mid));
      if (!(side.lengthSqr() < 1.0E-10D)) {
         side = side.normalize().scale((double)w * 0.5D);
         quad(vc, m, b.subtract(side), a.subtract(side), a.add(side), b.add(side), cell, r, g, bl, alpha, additive);
      }
   }

   public static void gate(MultiBufferSource buffers, Matrix4f m, Vec3 c, Vec3 n, float radius, float age, float flash, float alpha, Vec3 camRight, Vec3 camUp) {
      if (!(radius <= 0.01F) && !(alpha <= 0.01F)) {
         Vec3[] b = basis(n);
         Vec3 ax = b[0];
         Vec3 ay = b[1];
         float pulse = 0.85F + 0.15F * (float)Math.sin((double)(age * 0.45F));
         VertexConsumer soft = buffers.getBuffer(soft());
         plane(soft, m, c, ax, ay, radius, age * 0.05F, 0, 0.95F, 0.68F, 0.28F, 0.6F * alpha, false);
         plane(soft, m, c.subtract(n.scale(0.005D)), ax, ay, radius * 1.06F, -age * 0.06F, 1, 0.86F, 0.56F, 0.14F, 0.8F * alpha, false);
         plane(soft, m, c.add(n.scale(0.01D)), ax, ay, radius * 0.62F, -age * 0.08F, 0, 1.0F, 0.9F, 0.62F, 0.32F * alpha, false);
         VertexConsumer add = buffers.getBuffer(add());
         plane(add, m, c, ax, ay, radius * 1.36F, 0.0F, 2, 1.0F, 0.8F, 0.36F, 0.3F * alpha * pulse, true);

         for(int w = 0; w < 3; ++w) {
            float p = (age * 0.07F + (float)w / 3.0F) % 1.0F;
            plane(add, m, c.add(n.scale(0.015D)), ax, ay, radius * (0.22F + 0.86F * p), 0.0F, 6, 1.0F, 0.86F, 0.5F, 0.55F * (float)Math.sin(Math.PI * (double)p) * alpha, true);
         }

         plane(add, m, c.add(n.scale(0.02D)), ax, ay, radius * 1.06F, -age * 0.06F, 1, 1.0F, 0.86F, 0.48000002F, 0.72F * alpha, true);
         plane(add, m, c.add(n.scale(0.04D)), camRight, camUp, radius * (0.3F + 0.7F * flash), 0.0F, 5, 1.0F, 0.94F, 0.78F, Math.min(1.0F, 0.45F + flash) * alpha, true);

         for(int k = 0; k < 4; ++k) {
            double ang = (double)age * 0.05D + (double)k * (Math.PI / 2D);
            Vec3 at = c.add(ax.scale(Math.cos(ang) * (double)radius * (double)1.06F)).add(ay.scale(Math.sin(ang) * (double)radius * (double)1.06F));
            float tw = 0.55F + 0.45F * (float)Math.sin((double)(age * 0.5F + (float)k * 1.7F));
            plane(add, m, at, camRight, camUp, radius * 0.16F * tw, 0.0F, 4, 1.0F, 0.95F, 0.8F, 0.7F * alpha, true);
         }

         if (flash > 0.0F) {
            plane(add, m, c, ax, ay, radius * (1.1F + 1.4F * (1.0F - flash)), 0.0F, 6, 1.0F, 0.9F, 0.6F, flash * alpha, true);
         }

      }
   }

   public static void streak(VertexConsumer add, Matrix4f m, Vec3 n, Vec3 eye, double len, boolean shot, int tier, float trail) {
      Vec3 head = n.scale(shot ? 0.1D : -0.25D);
      Vec3 tail = head.subtract(n.scale(len));
      float w = shot ? 0.22F + 0.07F * (float)tier : 0.34F;
      ribbon(add, m, head, tail, eye, w, 3, 1.0F, 0.84F, 0.42F, 0.85F * trail, true);
      ribbon(add, m, head, head.subtract(n.scale(len * 0.55D)), eye, w * 0.4F, 3, 1.0F, 0.97F, 0.86F, 0.9F * trail, true);
   }

   public static void shotGlow(VertexConsumer add, Matrix4f m, Vec3 camRight, Vec3 camUp, int tier, float age) {
      float s = 0.16F + 0.06F * (float)tier;
      float pulse = 0.9F + 0.1F * (float)Math.sin((double)(age * 1.7F));
      plane(add, m, Vec3.ZERO, camRight, camUp, s * 2.2F * pulse, 0.0F, 2, 1.0F, 0.8F, 0.36F, 0.7F, true);
      plane(add, m, Vec3.ZERO, camRight, camUp, s * 1.3F, age * 0.3F, 4, 1.0F, 0.95F, 0.8F, 0.9F, true);
      plane(add, m, Vec3.ZERO, camRight, camUp, s, 0.0F, 5, 1.0F, 0.98F, 0.9F, 1.0F, true);
   }

   public static void claimStreaks(VertexConsumer add, Matrix4f m, Vec3 c, Vec3 n, Vec3 eye, float r, float age, float size) {
      Vec3[] b = basis(n);

      for(int k = 0; k < 8; ++k) {
         double ang = (double)k * (Math.PI / 4D) + (double)age * 0.04D;
         float t = (age * 0.09F + (float)k * 0.37F) % 1.0F;
         double reach = (double)r * (2.6D - 2.2D * (double)t);
         Vec3 from = b[0].scale(Math.cos(ang) * reach).add(b[1].scale(Math.sin(ang) * reach));
         ribbon(add, m, c.add(from.scale(0.55D)), c.add(from), eye, 0.08F, 3, 1.0F, 0.86F, 0.5F, 0.8F * (1.0F - t) * size, true);
      }

   }

   public static void gateEntity(MultiBufferSource buffers, Matrix4f m, Vec3 c, Vec3 n, float radius, int kind, float age, float fire, Vec3 eye, Vec3 camRight, Vec3 camUp) {
      float size = gateSize(age, fire);
      if (!(size <= 0.01F)) {
         float r = radius * size;
         gate(buffers, m, c, n, r, age, gateFlash(age, fire), Math.min(1.0F, size * 1.2F), camRight, camUp);
         if (kind == 1) {
            claimStreaks(buffers.getBuffer(add()), m, c, n, eye, r, age, size);
         }

      }
   }

   public static void blade(ItemRenderer items, ItemStack stack, PoseStack pose, MultiBufferSource buffers, Vec3 offset, float yaw, float pitch, float roll, float scale, Level level, int seed) {
      if (!stack.isEmpty() && !(scale <= 0.01F)) {
         pose.pushPose();

         try {
            pose.translate(offset.x, offset.y, offset.z);
            pose.mulPose(Axis.YP.rotationDegrees(yaw));
            pose.mulPose(Axis.XP.rotationDegrees(-pitch));
            pose.mulPose(Axis.ZP.rotationDegrees(roll));
            pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(45.0F));
            pose.scale(scale, scale, scale);
            items.renderStatic(stack, ItemDisplayContext.NONE, 15728880, OverlayTexture.NO_OVERLAY, pose, buffers, level, seed);
         } finally {
            pose.popPose();
         }

      }
   }
}
