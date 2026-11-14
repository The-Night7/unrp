package com.myriam.unrp.uo;

import com.myriam.unrp.pass.UnrpPowerLayers;


import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class SaltDraw {
   public static final ResourceLocation FX = new ResourceLocation("unrp", "textures/entity/salt_fx.png");
   public static final ResourceLocation FX_ADD = new ResourceLocation("unrp", "textures/entity/salt_fx_add.png");
   public static final ResourceLocation GROUND = new ResourceLocation("unrp", "textures/entity/salt_ground.png");
   public static final ResourceLocation CRUST = new ResourceLocation("unrp", "textures/entity/salt_crust.png");
   public static final ResourceLocation CRACKS = new ResourceLocation("unrp", "textures/entity/salt_cracks.png");
   public static final int SOFT_CELLS = 16;
   public static final int ADD_CELLS = 8;
   private static final int LIGHT = 15728880;
   public static final int SOFT_COPIES = 2;
   public static final float BEAM_ROW = 0.9F;
   private static final float[] UP = new float[]{0.0F, 1.0F, 0.0F};

   private SaltDraw() {
   }

   public static void normals(Matrix3f nm) {
      Vector3f n = nm.transform(new Vector3f(0.0F, 1.0F, 0.0F));
      UP[0] = n.x();
      UP[1] = n.y();
      UP[2] = n.z();
   }

   public static RenderType soft() {
      return RenderType.beaconBeam(FX, true);
   }

   public static RenderType add() {
      return RenderType.eyes(FX_ADD);
   }

   public static RenderType ground() {
      return RenderType.entityTranslucent(GROUND);
   }

   public static RenderType cracks() {
      return RenderType.entityTranslucent(CRACKS);
   }

   public static RenderType decalType(int kind) {
      return kind == 1 ? cracks() : ground();
   }

   public static RenderType crust() {
      return RenderType.entityTranslucent(CRUST);
   }

   public static RenderType flat() {
      return RenderType.entityTranslucentEmissive(UnrpPowerLayers.WHITE);
   }

   static float c01(float x) {
      return x < 0.0F ? 0.0F : (x > 1.0F ? 1.0F : x);
   }

   static void vtx(VertexConsumer vc, Matrix4f m, double[] p, double[] cam, float u, float v, float r, float g, float b, float a, int light) {
      Vector3f q = m.transformPosition(new Vector3f((float)(p[0] - cam[0]), (float)(p[1] - cam[1]), (float)(p[2] - cam[2])));
      vc.vertex(q.x(), q.y(), q.z(), c01(r), c01(g), c01(b), c01(a), u, v, OverlayTexture.NO_OVERLAY, light, UP[0], UP[1], UP[2]);
   }

   public static void quad(VertexConsumer vc, Matrix4f m, double[] cam, double[] p1, double[] p2, double[] p3, double[] p4, int cell, float r, float g, float b, float a1, float a2, float a3, float a4, boolean additive) {
      int cells = additive ? 8 : 16;
      float u0 = (float)cell * (1.0F / (float)cells);
      float u1 = u0 + 1.0F / (float)cells;
      mapped(vc, m, cam, p1, p2, p3, p4, r, g, b, a1, a2, a3, a4, additive, new float[]{u0, u1, u1, u0}, new float[]{1.0F, 1.0F, 0.0F, 0.0F});
   }

   static float[][] acrossUv(int cell) {
      float u0 = (float)cell * 0.125F;
      float u1 = u0 + 0.125F;
      float[] vs = cell == 3 ? new float[]{0.9F, 0.9F, 0.9F, 0.9F} : new float[]{0.0F, 1.0F, 1.0F, 0.0F};
      return new float[][]{{u0, u0, u1, u1}, vs};
   }

   static boolean across(int cell, boolean additive) {
      return additive && (cell == 3 || cell == 6);
   }

   static void mapped(VertexConsumer vc, Matrix4f m, double[] cam, double[] p1, double[] p2, double[] p3, double[] p4, float r, float g, float b, float a1, float a2, float a3, float a4, boolean additive, float[] us, float[] vs) {
      if (!(a1 <= 0.003F) || !(a2 <= 0.003F) || !(a3 <= 0.003F) || !(a4 <= 0.003F)) {
         float[] al = new float[]{a1, a2, a3, a4};
         float[] rr = new float[]{r, r, r, r};
         float[] gg = new float[]{g, g, g, g};
         float[] bb = new float[]{b, b, b, b};
         if (additive) {
            for(int i = 0; i < 4; ++i) {
               rr[i] = r * al[i];
               gg[i] = g * al[i];
               bb[i] = b * al[i];
            }
         }

         int copies = additive ? 1 : 2;

         for(int c = 0; c < copies; ++c) {
            vtx(vc, m, p1, cam, us[0], vs[0], rr[0], gg[0], bb[0], al[0], 15728880);
            vtx(vc, m, p2, cam, us[1], vs[1], rr[1], gg[1], bb[1], al[1], 15728880);
            vtx(vc, m, p3, cam, us[2], vs[2], rr[2], gg[2], bb[2], al[2], 15728880);
            vtx(vc, m, p4, cam, us[3], vs[3], rr[3], gg[3], bb[3], al[3], 15728880);
            vtx(vc, m, p4, cam, us[3], vs[3], rr[3], gg[3], bb[3], al[3], 15728880);
            vtx(vc, m, p3, cam, us[2], vs[2], rr[2], gg[2], bb[2], al[2], 15728880);
            vtx(vc, m, p2, cam, us[1], vs[1], rr[1], gg[1], bb[1], al[1], 15728880);
            vtx(vc, m, p1, cam, us[0], vs[0], rr[0], gg[0], bb[0], al[0], 15728880);
         }

      }
   }

   public static void bill(VertexConsumer vc, Matrix4f m, double[] cam, double[] c, double[] ax, double[] ay, double size, double spin, int cell, float r, float g, float b, float a, boolean additive) {
      if (!(a <= 0.003F) && !(size <= 0.0D)) {
         double cs = Math.cos(spin);
         double sn = Math.sin(spin);
         double[] rr = SaltMath.mul(SaltMath.add(SaltMath.mul(ax, cs), SaltMath.mul(ay, sn)), size);
         double[] uu = SaltMath.mul(SaltMath.sub(SaltMath.mul(ay, cs), SaltMath.mul(ax, sn)), size);
         quad(vc, m, cam, SaltMath.sub(SaltMath.sub(c, rr), uu), SaltMath.sub(SaltMath.add(c, rr), uu), SaltMath.add(SaltMath.add(c, rr), uu), SaltMath.add(SaltMath.sub(c, rr), uu), cell, r, g, b, a, a, a, a, additive);
      }
   }

   public static void ribbon(VertexConsumer vc, Matrix4f m, double[] cam, double[] a, double[] b, double w, int cell, float r, float g, float bl, float alpha, boolean additive) {
      double[] d = SaltMath.sub(b, a);
      double[] mid = SaltMath.mul(SaltMath.add(a, b), 0.5D);
      double[] side = SaltShape.cross(d, SaltMath.sub(cam, mid));
      double l = SaltMath.len(side);
      if (!(l < 1.0E-8D)) {
         side = SaltMath.mul(side, w * 0.5D / l);
         if (across(cell, additive)) {
            float[][] uv = acrossUv(cell);
            mapped(vc, m, cam, SaltMath.sub(a, side), SaltMath.sub(b, side), SaltMath.add(b, side), SaltMath.add(a, side), r, g, bl, alpha, alpha, alpha, alpha, true, uv[0], uv[1]);
         } else {
            quad(vc, m, cam, SaltMath.sub(a, side), SaltMath.sub(b, side), SaltMath.add(b, side), SaltMath.add(a, side), cell, r, g, bl, alpha, alpha, alpha, alpha, additive);
         }
      }
   }

   public static void chain(VertexConsumer vc, Matrix4f m, double[] cam, double[][] pts, double[] widths, int cell, float r, float g, float b, double[] alphas, boolean additive) {
      int n = pts.length;
      if (n >= 2) {
         double[][] sides = new double[n][];

         for(int i = 0; i < n; ++i) {
            double[] t = SaltMath.sub(pts[Math.min(n - 1, i + 1)], pts[Math.max(0, i - 1)]);
            double[] s = SaltShape.cross(t, SaltMath.sub(cam, pts[i]));
            double l = SaltMath.len(s);
            sides[i] = l < 1.0E-8D ? new double[]{0.0D, 0.0D, 0.0D} : SaltMath.mul(s, widths[i] * 0.5D / l);
         }

         for(int i = 0; i < n - 1; ++i) {
            float a0 = (float)alphas[i];
            float a1 = (float)alphas[i + 1];
            if (!(a0 <= 0.003F) || !(a1 <= 0.003F)) {
               quad(vc, m, cam, SaltMath.sub(pts[i], sides[i]), SaltMath.sub(pts[i + 1], sides[i + 1]), SaltMath.add(pts[i + 1], sides[i + 1]), SaltMath.add(pts[i], sides[i]), cell, r, g, b, a0, a1, a1, a0, additive);
            }
         }

      }
   }

   public static void ring(VertexConsumer vc, Matrix4f m, double[] cam, double[] c, double radius, double width, int cell, float r, float g, float b, float a, boolean additive) {
      if (!(radius <= 0.01D) && !(a <= 0.003F)) {
         int n = Math.max(16, Math.min(96, (int)Math.ceil(radius * 8.0D)));

         for(int i = 0; i < n; ++i) {
            double a0 = (double)i / (double)n * Math.PI * 2.0D;
            double a1 = (double)(i + 1) / (double)n * Math.PI * 2.0D;
            double ri = Math.max(0.0D, radius - width / 2.0D);
            double ro = radius + width / 2.0D;
            double[] p0i = new double[]{c[0] + Math.cos(a0) * ri, c[1], c[2] + Math.sin(a0) * ri};
            double[] p0o = new double[]{c[0] + Math.cos(a0) * ro, c[1], c[2] + Math.sin(a0) * ro};
            double[] p1i = new double[]{c[0] + Math.cos(a1) * ri, c[1], c[2] + Math.sin(a1) * ri};
            double[] p1o = new double[]{c[0] + Math.cos(a1) * ro, c[1], c[2] + Math.sin(a1) * ro};
            quad(vc, m, cam, p0i, p1i, p1o, p0o, cell, r, g, b, a, a, a, a, additive);
         }

      }
   }

   public static void upright(VertexConsumer vc, Matrix4f m, double[] cam, double[] base, double w, double h, int cell, float r, float g, float b, float a, boolean additive) {
      if (!(a <= 0.003F) && !(h <= 0.0D)) {
         double dx = cam[0] - base[0];
         double dz = cam[2] - base[2];
         double l = Math.sqrt(dx * dx + dz * dz);
         double[] side = l < 1.0E-6D ? new double[]{w * 0.5D, 0.0D, 0.0D} : new double[]{-dz / l * w * 0.5D, 0.0D, dx / l * w * 0.5D};
         double[] top = new double[]{base[0], base[1] + h, base[2]};
         quad(vc, m, cam, SaltMath.sub(base, side), SaltMath.add(base, side), SaltMath.add(top, side), SaltMath.sub(top, side), cell, r, g, b, a, a, a, a, additive);
      }
   }

   public static void slice(VertexConsumer vc, Matrix4f m, double[] cam, double[] p1, double[] p2, double[] p3, double[] p4, int cell, float r, float g, float b, float a, double v0, double v1, boolean additive) {
      if (!(a <= 0.003F)) {
         int cells = additive ? 8 : 16;
         float u0 = (float)cell * (1.0F / (float)cells);
         float u1 = u0 + 1.0F / (float)cells;
         float vb = (float)(1.0D - v0);
         float vt = (float)(1.0D - v1);
         float rr = additive ? r * a : r;
         float gg = additive ? g * a : g;
         float bb = additive ? b * a : b;
         int copies = additive ? 1 : 2;

         for(int c = 0; c < copies; ++c) {
            vtx(vc, m, p1, cam, u0, vb, rr, gg, bb, a, 15728880);
            vtx(vc, m, p2, cam, u1, vb, rr, gg, bb, a, 15728880);
            vtx(vc, m, p3, cam, u1, vt, rr, gg, bb, a, 15728880);
            vtx(vc, m, p4, cam, u0, vt, rr, gg, bb, a, 15728880);
            vtx(vc, m, p4, cam, u0, vt, rr, gg, bb, a, 15728880);
            vtx(vc, m, p3, cam, u1, vt, rr, gg, bb, a, 15728880);
            vtx(vc, m, p2, cam, u1, vb, rr, gg, bb, a, 15728880);
            vtx(vc, m, p1, cam, u0, vb, rr, gg, bb, a, 15728880);
         }

      }
   }

   public static void decal(MultiBufferSource buffers, Matrix4f m, double[] cam, int kind, double[] c, double size, double spin, float a, int light) {
      if (!(a <= 0.003F) && !(size <= 0.01D)) {
         VertexConsumer vc = buffers.getBuffer(decalType(kind));
         double cs = Math.cos(spin);
         double sn = Math.sin(spin);
         double[] at = new double[]{c[0], c[1] + 0.012D, c[2]};
         double[] rr = new double[]{cs * size, 0.0D, sn * size};
         double[] uu = new double[]{-sn * size, 0.0D, cs * size};
         lay(vc, m, cam, SaltMath.sub(SaltMath.sub(at, rr), uu), SaltMath.sub(SaltMath.add(at, rr), uu), SaltMath.add(SaltMath.add(at, rr), uu), SaltMath.add(SaltMath.sub(at, rr), uu), a, light);
      }
   }

   public static void strip(MultiBufferSource buffers, Matrix4f m, double[] cam, int kind, double[] a, double[] b, double w, float alpha, int light) {
      if (!(alpha <= 0.003F)) {
         double dx = b[0] - a[0];
         double dz = b[2] - a[2];
         double l = Math.sqrt(dx * dx + dz * dz);
         if (!(l < 1.0E-6D)) {
            double[] side = new double[]{-dz / l * w / 2.0D, 0.0D, dx / l * w / 2.0D};
            double[] a0 = new double[]{a[0], a[1] + 0.014D, a[2]};
            double[] b0 = new double[]{b[0], b[1] + 0.014D, b[2]};
            lay(buffers.getBuffer(decalType(kind)), m, cam, SaltMath.sub(a0, side), SaltMath.sub(b0, side), SaltMath.add(b0, side), SaltMath.add(a0, side), alpha, light);
         }
      }
   }

   static void lay(VertexConsumer vc, Matrix4f m, double[] cam, double[] p1, double[] p2, double[] p3, double[] p4, float a, int light) {
      vtx(vc, m, p1, cam, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, a, light);
      vtx(vc, m, p2, cam, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, a, light);
      vtx(vc, m, p3, cam, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, a, light);
      vtx(vc, m, p4, cam, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, a, light);
      vtx(vc, m, p4, cam, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, a, light);
      vtx(vc, m, p3, cam, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, a, light);
      vtx(vc, m, p2, cam, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, a, light);
      vtx(vc, m, p1, cam, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, a, light);
   }
}
