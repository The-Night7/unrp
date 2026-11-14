package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LashDraw {
   public static final ResourceLocation FX = new ResourceLocation("unrp", "textures/entity/black_lash_fx.png");
   public static final ResourceLocation FX_ADD = new ResourceLocation("unrp", "textures/entity/black_lash_fx_add.png");
   public static final ResourceLocation GROUND = new ResourceLocation("unrp", "textures/entity/black_lash_ground.png");
   public static final int CELLS = 8;
   public static final int S_TENDRIL = 0;
   public static final int S_SMOKE = 1;
   public static final int S_WISP = 2;
   public static final int S_HOOK = 3;
   public static final int S_PUFF = 4;
   public static final int A_RIM = 0;
   public static final int A_GLINT = 1;
   public static final int A_GLOW = 2;
   public static final int A_SPARK = 3;
   public static final int A_CRACK = 4;
   public static final int A_SHOCK = 5;
   public static final int A_HOOK = 6;
   private static final int LIGHT = 15728880;
   public static final double MIN_ARC = 0.0016D;
   private static final float[] UP = new float[]{0.0F, 1.0F, 0.0F};

   private LashDraw() {
   }

   public static void normals(Matrix3f nm) {
      Vector3f n = nm.transform(new Vector3f(0.0F, 1.0F, 0.0F));
      UP[0] = n.x();
      UP[1] = n.y();
      UP[2] = n.z();
   }

   static float once(float a) {
      float c = Math.max(0.0F, Math.min(1.0F, a));
      return c * (2.0F - c);
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

   static float c01(float x) {
      return x < 0.0F ? 0.0F : (x > 1.0F ? 1.0F : x);
   }

   private static void vtx(VertexConsumer vc, Matrix4f m, double[] p, double[] cam, float u, float v, float r, float g, float b, float a, int light) {
      Vector3f q = m.transformPosition(new Vector3f((float)(p[0] - cam[0]), (float)(p[1] - cam[1]), (float)(p[2] - cam[2])));
      vc.vertex(q.x(), q.y(), q.z(), c01(r), c01(g), c01(b), c01(a), u, v, OverlayTexture.NO_OVERLAY, light, UP[0], UP[1], UP[2]);
   }

   public static void quad(VertexConsumer vc, Matrix4f m, double[] cam, double[] p1, double[] p2, double[] p3, double[] p4, int cell, float r, float g, float b, float a1, float a2, float a3, float a4, boolean additive) {
      if (!(a1 <= 0.003F) || !(a2 <= 0.003F) || !(a3 <= 0.003F) || !(a4 <= 0.003F)) {
         float u0 = (float)cell * 0.125F;
         float u1 = u0 + 0.125F;
         float v0 = 0.0F;
         float v1 = 1.0F;
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
         } else {
            for(int i = 0; i < 4; ++i) {
               al[i] = once(al[i]);
            }
         }

         vtx(vc, m, p1, cam, u0, v1, rr[0], gg[0], bb[0], al[0], 15728880);
         vtx(vc, m, p2, cam, u1, v1, rr[1], gg[1], bb[1], al[1], 15728880);
         vtx(vc, m, p3, cam, u1, v0, rr[2], gg[2], bb[2], al[2], 15728880);
         vtx(vc, m, p4, cam, u0, v0, rr[3], gg[3], bb[3], al[3], 15728880);
         vtx(vc, m, p4, cam, u0, v0, rr[3], gg[3], bb[3], al[3], 15728880);
         vtx(vc, m, p3, cam, u1, v0, rr[2], gg[2], bb[2], al[2], 15728880);
         vtx(vc, m, p2, cam, u1, v1, rr[1], gg[1], bb[1], al[1], 15728880);
         vtx(vc, m, p1, cam, u0, v1, rr[0], gg[0], bb[0], al[0], 15728880);
      }
   }

   public static void plane(VertexConsumer vc, Matrix4f m, double[] cam, double[] c, double[] ax, double[] ay, double size, double spin, int cell, float r, float g, float b, float a, boolean additive) {
      double cs = Math.cos(spin);
      double sn = Math.sin(spin);
      double[] rr = LashMath.mul(LashMath.add(LashMath.mul(ax, cs), LashMath.mul(ay, sn)), size);
      double[] uu = LashMath.mul(LashMath.sub(LashMath.mul(ay, cs), LashMath.mul(ax, sn)), size);
      quad(vc, m, cam, LashMath.sub(LashMath.sub(c, rr), uu), LashMath.sub(LashMath.add(c, rr), uu), LashMath.add(LashMath.add(c, rr), uu), LashMath.add(LashMath.sub(c, rr), uu), cell, r, g, b, a, a, a, a, additive);
   }

   public static void bill(VertexConsumer vc, Matrix4f m, double[] cam, double[] c, double[] right, double[] up, double size, double spin, int cell, float r, float g, float b, float a, boolean additive) {
      if (!(a <= 0.003F)) {
         plane(vc, m, cam, c, right, up, size, spin, cell, r, g, b, a, additive);
      }
   }

   public static void ribbon(VertexConsumer vc, Matrix4f m, double[] cam, double[] a, double[] b, double w, int cell, float r, float g, float bl, float alpha, boolean additive) {
      double[] d = LashMath.sub(b, a);
      double[] mid = LashMath.mul(LashMath.add(a, b), 0.5D);
      double[] side = LashMath.cross(d, LashMath.sub(cam, mid));
      double l = LashMath.len(side);
      if (!(l < 1.0E-8D)) {
         side = LashMath.mul(side, w * 0.5D / l);
         quad(vc, m, cam, LashMath.sub(a, side), LashMath.sub(b, side), LashMath.add(b, side), LashMath.add(a, side), cell, r, g, bl, alpha, alpha, alpha, alpha, additive);
      }
   }

   public static void chain(VertexConsumer vc, Matrix4f m, double[] cam, double[][] pts, double[] widths, int cell, float r, float g, float b, double[] alphas, boolean additive) {
      int n = pts.length;
      if (n >= 2) {
         double[][] sides = new double[n][];

         for(int i = 0; i < n; ++i) {
            double[] p = pts[i];
            double[] t = LashMath.sub(pts[Math.min(n - 1, i + 1)], pts[Math.max(0, i - 1)]);
            double[] s = LashMath.cross(t, LashMath.sub(cam, p));
            double l = LashMath.len(s);
            double w = Math.max(widths[i], LashMath.dist(p, cam) * 0.0016D);
            sides[i] = l < 1.0E-8D ? new double[]{0.0D, 0.0D, 0.0D} : LashMath.mul(s, w * 0.5D / l);
         }

         for(int i = 0; i < n - 1; ++i) {
            float a0 = (float)alphas[i];
            float a1 = (float)alphas[i + 1];
            if (!(a0 <= 0.003F) || !(a1 <= 0.003F)) {
               quad(vc, m, cam, LashMath.sub(pts[i], sides[i]), LashMath.sub(pts[i + 1], sides[i + 1]), LashMath.add(pts[i + 1], sides[i + 1]), LashMath.add(pts[i], sides[i]), cell, r, g, b, a0, a1, a1, a0, additive);
            }
         }

      }
   }

   public static void ring(VertexConsumer vc, Matrix4f m, double[] cam, double[] c, double radius, double width, int segs, double[] normal, float r, float g, float b, float a) {
      if (!(radius <= 0.01D) && !(a <= 0.003F)) {
         int n = segs > 0 ? segs : Math.max(16, (int)Math.ceil(radius * 8.0D));
         double[][] bs = LashShape.basis(normal == null ? new double[]{0.0D, 1.0D, 0.0D} : normal);

         for(int i = 0; i < n; ++i) {
            double a0 = (double)i / (double)n * Math.PI * 2.0D;
            double a1 = (double)(i + 1) / (double)n * Math.PI * 2.0D;
            double[] d0 = LashMath.add(LashMath.mul(bs[0], Math.cos(a0)), LashMath.mul(bs[1], Math.sin(a0)));
            double[] d1 = LashMath.add(LashMath.mul(bs[0], Math.cos(a1)), LashMath.mul(bs[1], Math.sin(a1)));
            double[] p0i = LashMath.add(c, LashMath.mul(d0, radius - width / 2.0D));
            double[] p0o = LashMath.add(c, LashMath.mul(d0, radius + width / 2.0D));
            double[] p1i = LashMath.add(c, LashMath.mul(d1, radius - width / 2.0D));
            double[] p1o = LashMath.add(c, LashMath.mul(d1, radius + width / 2.0D));
            quad(vc, m, cam, p0i, p1i, p1o, p0o, 0, r, g, b, a, a, a, a, true);
         }

      }
   }

   public static void decal(MultiBufferSource buffers, Matrix4f m, double[] cam, double[] c, double size, double spin, float a, int light) {
      if (!(a <= 0.003F) && !(size <= 0.01D)) {
         VertexConsumer vc = buffers.getBuffer(ground());
         double cs = Math.cos(spin);
         double sn = Math.sin(spin);
         double[] at = new double[]{c[0], c[1] + 0.012D, c[2]};
         double[] rr = new double[]{cs * size, 0.0D, sn * size};
         double[] uu = new double[]{-sn * size, 0.0D, cs * size};
         double[] p1 = LashMath.sub(LashMath.sub(at, rr), uu);
         double[] p2 = LashMath.sub(LashMath.add(at, rr), uu);
         double[] p3 = LashMath.add(LashMath.add(at, rr), uu);
         double[] p4 = LashMath.add(LashMath.sub(at, rr), uu);
         float oa = once(a);
         vtx(vc, m, p1, cam, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, oa, light);
         vtx(vc, m, p2, cam, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, oa, light);
         vtx(vc, m, p3, cam, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, oa, light);
         vtx(vc, m, p4, cam, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, oa, light);
      }
   }
}
