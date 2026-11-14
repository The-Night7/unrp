package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class SaltCrust implements VertexConsumer {
   static final double RIM = 0.06D;
   static final double RAGGED = 0.12D;
   static final float TONE = 0.86F;
   static final float EDGE = 1.0F;
   static final int H = 12;
   static final int LX = 13;
   static final int LZ = 14;
   static final int SIZE = 15;
   private final VertexConsumer out;
   private final Matrix4f back;
   private final double fill;
   private final int seed;
   private final float[][] quad = new float[4][];
   private int n;
   private float x;
   private float y;
   private float z;
   private float r = 1.0F;
   private float g = 1.0F;
   private float b = 1.0F;
   private float a = 1.0F;
   private float u;
   private float v;
   private float nx;
   private float ny = 1.0F;
   private float nz;
   private int overlay;
   private int light;

   SaltCrust(VertexConsumer out, Matrix4f back, double fill, int seed) {
      this.out = out;
      this.back = back;
      this.fill = fill;
      this.seed = seed;
   }

   public VertexConsumer vertex(double px, double py, double pz) {
      this.x = (float)px;
      this.y = (float)py;
      this.z = (float)pz;
      return this;
   }

   public VertexConsumer color(int cr, int cg, int cb, int ca) {
      this.r = (float)cr / 255.0F;
      this.g = (float)cg / 255.0F;
      this.b = (float)cb / 255.0F;
      this.a = (float)ca / 255.0F;
      return this;
   }

   public VertexConsumer uv(float cu, float cv) {
      this.u = cu;
      this.v = cv;
      return this;
   }

   public VertexConsumer overlayCoords(int ou, int ov) {
      this.overlay = ou & '\uffff' | ov << 16;
      return this;
   }

   public VertexConsumer uv2(int lu, int lv) {
      this.light = lu & '\uffff' | lv << 16;
      return this;
   }

   public VertexConsumer normal(float fx, float fy, float fz) {
      this.nx = fx;
      this.ny = fy;
      this.nz = fz;
      return this;
   }

   public void endVertex() {
      Vector3f local = this.back.transformPosition(new Vector3f(this.x, this.y, this.z));
      this.quad[this.n] = new float[]{this.x, this.y, this.z, this.r, this.g, this.b, this.a, this.u, this.v, this.nx, this.ny, this.nz, local.y(), local.x(), local.z()};
      ++this.n;
      if (this.n == 4) {
         this.emit();
         this.n = 0;
      }

   }

   public void defaultColor(int dr, int dg, int db, int da) {
   }

   public void unsetDefaultColor() {
   }

   static float[] lerp(float[] p, float[] q, float t) {
      float[] o = new float[15];

      for(int i = 0; i < 15; ++i) {
         o[i] = p[i] + (q[i] - p[i]) * t;
      }

      return o;
   }

   static List<float[]> clip(List<float[]> poly, double c, boolean below) {
      List<float[]> res = new ArrayList(poly.size() + 2);
      int count = poly.size();

      for(int i = 0; i < count; ++i) {
         float[] p = (float[])poly.get(i);
         float[] q = (float[])poly.get((i + 1) % count);
         boolean pin = below ? (double)p[12] <= c : (double)p[12] >= c;
         boolean qin = below ? (double)q[12] <= c : (double)q[12] >= c;
         if (pin) {
            res.add(p);
         }

         if (pin != qin) {
            res.add(lerp(p, q, (float)((c - (double)p[12]) / (double)(q[12] - p[12]))));
         }
      }

      return res;
   }

   private void emit() {
      List<float[]> poly = new ArrayList(4);
      float cx = 0.0F;
      float cz = 0.0F;

      for(float[] p : this.quad) {
         poly.add(p);
         cx += p[13] * 0.25F;
         cz += p[14] * 0.25F;
      }

      double top = this.fill + (SaltShape.hash3((int)Math.floor((double)(cx * 16.0F)), (int)Math.floor((double)(cz * 16.0F)), this.seed) - 0.5D) * 0.12D;
      this.put(clip(poly, top - 0.06D, true), 0.86F);
      this.put(clip(clip(poly, top, true), top - 0.06D, false), 1.0F);
   }

   private void put(List<float[]> poly, float tone) {
      int count = poly.size();
      if (count >= 3) {
         if (count == 4) {
            for(float[] p : poly) {
               this.send(p, tone);
            }

         } else {
            for(int i = 1; i < count - 1; ++i) {
               this.send((float[])poly.get(0), tone);
               this.send((float[])poly.get(i), tone);
               this.send((float[])poly.get(i + 1), tone);
               this.send((float[])poly.get(i + 1), tone);
            }

         }
      }
   }

   private void send(float[] p, float tone) {
      this.out.vertex(p[0], p[1], p[2], p[3] * tone, p[4] * tone, p[5] * tone, p[6], p[7], p[8], this.overlay, this.light, p[9], p[10], p[11]);
   }
}
