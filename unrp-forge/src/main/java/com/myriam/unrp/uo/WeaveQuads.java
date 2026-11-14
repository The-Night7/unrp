package com.myriam.unrp.uo;

import com.myriam.unrp.pass.ViewUp;


import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;

public final class WeaveQuads {
   public static final int MOTE = 0;
   public static final int GLINT = 1;
   public static final int NODE = 2;
   public static final int GLOW = 3;
   public static final int LINE = 4;
   public static final int RING = 5;
   public static final int SPARK = 6;
   public static final int DASH = 7;
   public static final int SOFT = 1;
   public static final int ADD = 2;
   private static final int LIGHT = 15728880;
   VertexConsumer vc;
   Matrix4f m;
   int pass;
   double cx;
   double cy;
   double cz;
   float rx;
   float ry;
   float rz;
   float ux;
   float uy;
   float uz;
   double ex;
   double ey;
   double ez;

   void begin(VertexConsumer vc, Matrix4f m, int pass, double cx, double cy, double cz, float rx, float ry, float rz, float ux, float uy, float uz) {
      this.vc = vc;
      this.m = m;
      this.pass = pass;
      this.cx = cx;
      this.cy = cy;
      this.cz = cz;
      this.rx = rx;
      this.ry = ry;
      this.rz = rz;
      this.ux = ux;
      this.uy = uy;
      this.uz = uz;
      this.ex = cx;
      this.ey = cy;
      this.ez = cz;
   }

   void eye(double x, double y, double z) {
      this.ex = x;
      this.ey = y;
      this.ez = z;
   }

   static float u0(int spr) {
      return (float)(spr % 4) * 0.25F;
   }

   static float v0(int spr) {
      return (float)(spr / 4) * 0.5F;
   }

   private void vtx(double x, double y, double z, float u, float v, float r, float g, float b, float a) {
      this.vc.vertex(this.m, (float)(x - this.cx), (float)(y - this.cy), (float)(z - this.cz)).color(r, g, b, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
   }

   private static float clamp(float a) {
      return a < 0.0F ? 0.0F : (a > 1.0F ? 1.0F : a);
   }

   void bill(double x, double y, double z, float s, float r, float g, float b, float a, int spr, int p) {
      if (p == this.pass) {
         a = clamp(a);
         if (!(a <= 0.003F)) {
            float au = u0(spr);
            float av = v0(spr);
            float bu = au + 0.25F;
            float bv = av + 0.5F;
            if (p == 2) {
               r *= a;
               g *= a;
               b *= a;
               au = 0.0F;
               av = 0.0F;
               bu = 1.0F;
               bv = 1.0F;
            }

            double ax = (double)(this.rx * s);
            double ay = (double)(this.ry * s);
            double az = (double)(this.rz * s);
            double bx = (double)(this.ux * s);
            double by = (double)(this.uy * s);
            double bz = (double)(this.uz * s);
            this.vtx(x - ax - bx, y - ay - by, z - az - bz, au, bv, r, g, b, a);
            this.vtx(x + ax - bx, y + ay - by, z + az - bz, bu, bv, r, g, b, a);
            this.vtx(x + ax + bx, y + ay + by, z + az + bz, bu, av, r, g, b, a);
            this.vtx(x - ax + bx, y - ay + by, z - az + bz, au, av, r, g, b, a);
         }
      }
   }

   void flat(double ax, double ay, double az, double bx, double by, double bz, float nx, float ny, float nz, float w, float r, float g, float b, float aA, float aB, int spr) {
      if (this.pass == 1) {
         aA = clamp(aA);
         aB = clamp(aB);
         if (!(aA <= 0.003F) || !(aB <= 0.003F)) {
            double dx = bx - ax;
            double dy = by - ay;
            double dz = bz - az;
            double px = (double)ny * dz - (double)nz * dy;
            double py = (double)nz * dx - (double)nx * dz;
            double pz = (double)nx * dy - (double)ny * dx;
            double l = Math.sqrt(px * px + py * py + pz * pz);
            if (!(l < 1.0E-9D)) {
               double k = (double)w * 0.5D / l;
               px *= k;
               py *= k;
               pz *= k;
               float au = u0(spr);
               float av = v0(spr);
               float bu = au + 0.25F;
               float bv = av + 0.5F;
               this.vtx(ax - px, ay - py, az - pz, au, bv, r, g, b, aA);
               this.vtx(bx - px, by - py, bz - pz, bu, bv, r, g, b, aB);
               this.vtx(bx + px, by + py, bz + pz, bu, av, r, g, b, aB);
               this.vtx(ax + px, ay + py, az + pz, au, av, r, g, b, aA);
            }
         }
      }
   }

   void ribbon(double ax, double ay, double az, double bx, double by, double bz, float w, float r, float g, float b, float aA, float aB, int spr) {
      if (this.pass == 1) {
         aA = clamp(aA);
         aB = clamp(aB);
         if (!(aA <= 0.003F) || !(aB <= 0.003F)) {
            double dx = bx - ax;
            double dy = by - ay;
            double dz = bz - az;
            double vx = (ax + bx) * 0.5D - this.ex;
            double vy = (ay + by) * 0.5D - this.ey;
            double vz = (az + bz) * 0.5D - this.ez;
            double px = dy * vz - dz * vy;
            double py = dz * vx - dx * vz;
            double pz = dx * vy - dy * vx;
            double l = Math.sqrt(px * px + py * py + pz * pz);
            if (!(l < 1.0E-9D)) {
               double k = (double)w * 0.5D / l;
               px *= k;
               py *= k;
               pz *= k;
               float au = u0(spr);
               float av = v0(spr);
               float bu = au + 0.25F;
               float bv = av + 0.5F;
               this.vtx(ax - px, ay - py, az - pz, au, bv, r, g, b, aA);
               this.vtx(bx - px, by - py, bz - pz, bu, bv, r, g, b, aB);
               this.vtx(bx + px, by + py, bz + pz, bu, av, r, g, b, aB);
               this.vtx(ax + px, ay + py, az + pz, au, av, r, g, b, aA);
            }
         }
      }
   }

   void fill(double[] p, float r, float g, float b, float a) {
      if (this.pass == 1) {
         a = clamp(a);
         if (!(a <= 0.003F)) {
            float au = 0.075F;
            float bu = 0.175F;
            float av = 0.7265625F;
            float bv = 0.7421875F;
            this.vtx(p[0], p[1], p[2], au, bv, r, g, b, a);
            this.vtx(p[3], p[4], p[5], bu, bv, r, g, b, a);
            this.vtx(p[6], p[7], p[8], bu, av, r, g, b, a);
            this.vtx(p[9], p[10], p[11], au, av, r, g, b, a);
         }
      }
   }
}
