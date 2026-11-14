package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class DaystarDraw {
   public static final ResourceLocation FX = new ResourceLocation("unrp", "textures/entity/daystar_fx.png");
   public static final ResourceLocation FX_ADD = new ResourceLocation("unrp", "textures/entity/daystar_fx_add.png");
   public static final ResourceLocation GROUND = new ResourceLocation("unrp", "textures/entity/daystar_ground.png");
   public static final int SUN = 0;
   public static final int RAYS = 1;
   public static final int FLAME = 2;
   public static final int SHIMMER = 3;
   public static final int RING = 4;
   public static final int GLOW = 5;
   public static final int SIGIL = 6;
   public static final int SPARK = 7;
   public static final int FISSURE = 8;
   public static final int FLAME2 = 9;
   public static final int SMOKE = 10;
   public static final int SHARD = 11;
   public static final int BEAM = 12;
   public static final int RIM = 13;
   public static final int DOT = 14;
   public static final int CRACK = 15;
   public static final int SCORCH = 0;
   public static final int CRATER = 1;
   private static final int LIGHT = 15728880;
   public static final int SOFT_COPIES = 2;
   private static final float[] UP = new float[]{0.0F, 1.0F, 0.0F};

   private DaystarDraw() {
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

   public static float clamp01(double x) {
      return x < 0.0D ? 0.0F : (x > 1.0D ? 1.0F : (float)x);
   }

   public static float outCubic(double x) {
      float c = clamp01(x);
      float k = 1.0F - c;
      return 1.0F - k * k * k;
   }

   public static double hash(int a, int b, int c) {
      long h = (long)a * 374761393L + (long)b * 668265263L + (long)c * 2147483647L;
      h = (h ^ h >>> 13) * 1274126177L;
      h ^= h >>> 16;
      return (double)(h & 16777215L) / 1.6777215E7D;
   }

   private static void vtx(VertexConsumer vc, Vector3f q, float u, float v, float r, float g, float b, float a, int light) {
      vc.vertex(q.x(), q.y(), q.z(), r, g, b, a, u, v, OverlayTexture.NO_OVERLAY, light, UP[0], UP[1], UP[2]);
   }

   private static Vector3f at(Matrix4f m, Vec3 p) {
      return m.transformPosition(new Vector3f((float)p.x, (float)p.y, (float)p.z));
   }

   private static void quadUv(VertexConsumer vc, Matrix4f m, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, float u0, float v0, float u1, float v1, float r, float g, float b, float a, boolean additive, int light, int copies) {
      if (!(a <= 0.003F)) {
         float al = clamp01((double)a);
         float rr = clamp01(additive ? (double)(r * al) : (double)r);
         float gg = clamp01(additive ? (double)(g * al) : (double)g);
         float bb = clamp01(additive ? (double)(b * al) : (double)b);
         Vector3f q1 = at(m, p1);
         Vector3f q2 = at(m, p2);
         Vector3f q3 = at(m, p3);
         Vector3f q4 = at(m, p4);

         for(int c = 0; c < copies; ++c) {
            vtx(vc, q1, u0, v1, rr, gg, bb, al, light);
            vtx(vc, q2, u1, v1, rr, gg, bb, al, light);
            vtx(vc, q3, u1, v0, rr, gg, bb, al, light);
            vtx(vc, q4, u0, v0, rr, gg, bb, al, light);
            vtx(vc, q4, u0, v0, rr, gg, bb, al, light);
            vtx(vc, q3, u1, v0, rr, gg, bb, al, light);
            vtx(vc, q2, u1, v1, rr, gg, bb, al, light);
            vtx(vc, q1, u0, v1, rr, gg, bb, al, light);
         }

      }
   }

   public static void quad(VertexConsumer vc, Matrix4f m, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, int cell, float r, float g, float b, float a, boolean additive) {
      float u0 = (float)(cell % 8) * 0.125F;
      float v0 = (float)(cell / 8) * 0.5F;
      quadUv(vc, m, p1, p2, p3, p4, u0, v0, u0 + 0.125F, v0 + 0.5F, r, g, b, a, additive, 15728880, additive ? 1 : 2);
   }

   public static void strip(VertexConsumer vc, Matrix4f m, Vec3 a0, Vec3 a1, Vec3 b1, Vec3 b0, int cell, boolean lengthwise, float s0, float s1, float r, float g, float b, float al0, float al1, boolean additive) {
      if (!(al0 <= 0.003F) || !(al1 <= 0.003F)) {
         float cu = (float)(cell % 8) * 0.125F;
         float cv = (float)(cell / 8) * 0.5F;
         float x0 = clamp01((double)al0);
         float x1 = clamp01((double)al1);
         float[] u = lengthwise ? new float[]{0.0F, 0.0F, 1.0F, 1.0F} : new float[]{s0, s1, s1, s0};
         float[] v = lengthwise ? new float[]{s0, s1, s1, s0} : new float[]{1.0F, 1.0F, 0.0F, 0.0F};
         float[] al = new float[]{x0, x1, x1, x0};
         Vector3f[] q = new Vector3f[]{at(m, a0), at(m, a1), at(m, b1), at(m, b0)};
         int[] order = new int[]{0, 1, 2, 3, 3, 2, 1, 0};
         int copies = additive ? 1 : 2;

         for(int c = 0; c < copies; ++c) {
            for(int k : order) {
               float a = al[k];
               float rr = clamp01(additive ? (double)(r * a) : (double)r);
               float gg = clamp01(additive ? (double)(g * a) : (double)g);
               float bb = clamp01(additive ? (double)(b * a) : (double)b);
               vtx(vc, q[k], cu + u[k] * 0.125F, cv + v[k] * 0.5F, rr, gg, bb, a, 15728880);
            }
         }

      }
   }

   public static void plane(VertexConsumer vc, Matrix4f m, Vec3 c, Vec3 ax, Vec3 ay, double size, double spin, int cell, float r, float g, float b, float a, boolean additive) {
      double cs = Math.cos(spin);
      double sn = Math.sin(spin);
      Vec3 rr = ax.scale(cs).add(ay.scale(sn)).scale(size);
      Vec3 uu = ay.scale(cs).subtract(ax.scale(sn)).scale(size);
      quad(vc, m, c.subtract(rr).subtract(uu), c.add(rr).subtract(uu), c.add(rr).add(uu), c.subtract(rr).add(uu), cell, r, g, b, a, additive);
   }

   public static void bill(VertexConsumer vc, Matrix4f m, Vec3 c, Vec3 camRight, Vec3 camUp, double size, double spin, int cell, float r, float g, float b, float a, boolean additive) {
      plane(vc, m, c, camRight, camUp, size, spin, cell, r, g, b, a, additive);
   }

   public static void flat(VertexConsumer vc, Matrix4f m, Vec3 c, double size, double spin, int cell, float r, float g, float b, float a, boolean additive) {
      plane(vc, m, c, new Vec3(1.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 1.0D), size, spin, cell, r, g, b, a, additive);
   }

   public static void upright(VertexConsumer vc, Matrix4f m, Vec3 base, Vec3 camRight, double width, double height, int cell, float r, float g, float b, float a, boolean additive) {
      Vec3 side = new Vec3(camRight.x, 0.0D, camRight.z);
      if (side.lengthSqr() < 1.0E-6D) {
         side = new Vec3(1.0D, 0.0D, 0.0D);
      }

      side = side.normalize().scale(width * 0.5D);
      Vec3 top = base.add(0.0D, height, 0.0D);
      quad(vc, m, base.subtract(side), base.add(side), top.add(side), top.subtract(side), cell, r, g, b, a, additive);
   }

   public static void ribbon(VertexConsumer vc, Matrix4f m, Vec3 a, Vec3 b, Vec3 eye, double w, int cell, float r, float g, float bl, float alpha, boolean additive) {
      Vec3 d = b.subtract(a);
      Vec3 mid = a.add(b).scale(0.5D);
      Vec3 side = d.cross(eye.subtract(mid));
      if (!(side.lengthSqr() < 1.0E-10D)) {
         side = side.normalize().scale(w * 0.5D);
         quad(vc, m, b.subtract(side), a.subtract(side), a.add(side), b.add(side), cell, r, g, bl, alpha, additive);
      }
   }

   public static void ring(VertexConsumer vc, Matrix4f m, Vec3 c, double radius, double width, int segs, float r, float g, float b, float a) {
      if (!(radius <= 0.01D) && !(a <= 0.003F)) {
         for(int i = 0; i < segs; ++i) {
            double a0 = (double)i * Math.PI * 2.0D / (double)segs;
            double a1 = (double)(i + 1) * Math.PI * 2.0D / (double)segs;
            Vec3 o0 = new Vec3(Math.cos(a0), 0.0D, Math.sin(a0));
            Vec3 o1 = new Vec3(Math.cos(a1), 0.0D, Math.sin(a1));
            Vec3 p1 = c.add(o0.scale(radius - width * 0.5D));
            Vec3 p2 = c.add(o1.scale(radius - width * 0.5D));
            Vec3 p3 = c.add(o1.scale(radius + width * 0.5D));
            Vec3 p4 = c.add(o0.scale(radius + width * 0.5D));
            quad(vc, m, p1, p2, p3, p4, 13, r, g, b, a, true);
         }

      }
   }

   public static void decal(MultiBufferSource buffers, Matrix4f m, Vec3 c, double size, double spin, int cell, float a, int light) {
      if (!(a <= 0.003F) && !(size <= 0.01D)) {
         VertexConsumer vc = buffers.getBuffer(ground());
         double cs = Math.cos(spin);
         double sn = Math.sin(spin);
         Vec3 rr = (new Vec3(cs, 0.0D, sn)).scale(size);
         Vec3 uu = (new Vec3(-sn, 0.0D, cs)).scale(size);
         float u0 = (float)cell * 0.5F;
         quadUv(vc, m, c.subtract(rr).subtract(uu), c.add(rr).subtract(uu), c.add(rr).add(uu), c.subtract(rr).add(uu), u0, 0.0F, u0 + 0.5F, 1.0F, 1.0F, 1.0F, 1.0F, a, false, light, 1);
      }
   }

   public static void orb(MultiBufferSource buffers, Matrix4f m, Vec3 c, double r, double t, boolean ember, float alpha, Vec3 camRight, Vec3 camUp) {
      if (ember) {
         VertexConsumer add_0 = buffers.getBuffer(add());
         bill(add_0, m, c, camRight, camUp, r * 2.2D, 0.0D, 5, 1.0F, 0.42F, 0.1F, 0.55F * alpha, true);
         bill(add_0, m, c, camRight, camUp, r, t * 0.05D, 0, 1.0F, 0.62F, 0.28F, alpha, true);

         for(int e = 0; e < 3; ++e) {
            double ea = t * 0.2D + (double)e * 2.1D;
            Vec3 p_0 = c.add(Math.cos(ea) * r * 1.4D, Math.sin(ea * 1.7D) * r, Math.sin(ea) * r * 1.4D);
            bill(add_0, m, p_0, camRight, camUp, 0.03D, 0.0D, 7, 1.0F, 0.55F, 0.2F, 0.8F * alpha, true);
         }

      } else {
         float big = clamp01((r - 0.4D) / 2.5D);
         VertexConsumer add = buffers.getBuffer(add());
         bill(add, m, c, camRight, camUp, r * (3.0D - 0.6D * (double)big), 0.0D, 5, 1.0F, 0.6F - 0.15F * big, 0.2F - 0.12F * big, (0.42F - 0.2F * big) * alpha, true);
         bill(add, m, c, camRight, camUp, r * (2.3D - 0.3D * (double)big), t * 0.025D / (1.0D + r * 0.3D), 1, 1.0F, 0.82F - 0.3F * big, 0.38F - 0.28F * big, (0.75F - 0.45F * big) * alpha, true);
         bill(add, m, c, camRight, camUp, r * (1.9D - 0.2D * (double)big), -t * 0.018D / (1.0D + r * 0.3D) + 0.26D, 1, 1.0F, 0.58F - 0.2F * big, 0.18F - 0.1F * big, (0.5F - 0.3F * big) * alpha, true);
         VertexConsumer soft = buffers.getBuffer(soft());
         if (big > 0.0F) {
            bill(soft, m, c, camRight, camUp, r * 1.12D, 0.0D, 5, 1.0F, 0.5F, 0.12F, 0.55F * big * alpha, false);
            bill(soft, m, c, camRight, camUp, r, t * 0.004D, 0, 1.0F, 0.66F, 0.2F, 0.96F * big * alpha, false);
            bill(soft, m, c, camRight, camUp, r * 0.8D, -t * 0.006D, 0, 1.0F, 0.84F, 0.42F, 0.8F * big * alpha, false);
         }

         bill(soft, m, c, camRight, camUp, r * 2.6D, t * 0.012D, 3, 1.0F, 0.9F, 0.72F, 0.16F * alpha, false);
         int nf = r > 1.2D ? 10 : 0;

         for(int k = 0; k < nf; ++k) {
            double a_0 = (double)k / (double)nf * Math.PI * 2.0D + t * 0.01D;
            Vec3 surf_0 = c.add(camRight.scale(Math.cos(a_0) * r * 0.92D)).add(camUp.scale(Math.sin(a_0) * r * 0.92D));
            float fl_0 = 0.55F + 0.45F * (float)Math.sin(t * 0.21D + (double)k * 2.3D);
            bill(soft, m, surf_0, camRight, camUp, r * 0.34D * (double)fl_0, a_0 + (Math.PI / 2D), 2, 1.0F, 0.48F, 0.1F, 0.75F * alpha, false);
         }

         add = buffers.getBuffer(add());
         bill(add, m, c, camRight, camUp, r * (1.05D - 0.4D * (double)big), 0.0D, 0, 1.0F, 0.96F - 0.1F * big, 0.82F - 0.3F * big, (1.0F - 0.45F * big) * alpha, true);

         for(int k = 0; k < nf; ++k) {
            double a = (double)k / (double)nf * Math.PI * 2.0D + t * 0.01D;
            Vec3 surf = c.add(camRight.scale(Math.cos(a) * r * 0.92D)).add(camUp.scale(Math.sin(a) * r * 0.92D));
            float fl = 0.55F + 0.45F * (float)Math.sin(t * 0.21D + (double)k * 2.3D);
            bill(add, m, surf, camRight, camUp, r * 0.22D * (double)fl, a + (Math.PI / 2D), 2, 1.0F, 0.7F, 0.3F, 0.35F * alpha, true);
         }

         int ns = 6 + (int)Math.round(r * 3.0D);

         for(int k = 0; k < ns; ++k) {
            double b = t * 0.13D / (1.0D + r * 0.2D) + (double)k * 6.283D / (double)ns;
            Vec3 p = c.add(Math.cos(b) * r * 1.45D, Math.sin(b * 1.3D + (double)k) * r * 0.45D, Math.sin(b) * r * 1.45D);
            bill(add, m, p, camRight, camUp, 0.045D + 0.03D * r, 0.0D, 7, 1.0F, 0.86F, 0.5F, 0.9F * alpha, true);
         }

      }
   }
}
