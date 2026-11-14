package com.myriam.unrp.pass;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class PassDraw {
   public static final ResourceLocation SOFT = new ResourceLocation("unrp", "textures/fx/pass_soft.png");
   public static final ResourceLocation OVER = new ResourceLocation("unrp", "textures/fx/pass_over.png");
   public static final ResourceLocation ADD = new ResourceLocation("unrp", "textures/fx/pass_add.png");
   public static final ResourceLocation DECALS = new ResourceLocation("unrp", "textures/fx/pass_decals.png");
   public static final ResourceLocation DECALS_ADD = new ResourceLocation("unrp", "textures/fx/pass_decals_add.png");
   public static final ResourceLocation XHAIR = new ResourceLocation("unrp", "textures/gui/predator_crosshair.png");
   public static final int COLS = 8;
   public static final int ROWS = 9;
   public static final int SOURCE_CELL = 32;
   public static final int DCOLS = 4;
   public static final int DROWS = 3;
   public static final int SOURCE_DECAL = 128;
   public static final int[] ORDER = new int[]{4, 5, 0, 2, 1, 3, 6};
   public static final double PULL = 2.4E-6D;
   private static final int LIGHT = 15728880;
   private static final float[] UP = new float[]{0.0F, 1.0F, 0.0F};

   private PassDraw() {
   }

   public static void normals(Matrix3f nm) {
      Vector3f n = nm.transform(new Vector3f(0.0F, 1.0F, 0.0F));
      UP[0] = n.x();
      UP[1] = n.y();
      UP[2] = n.z();
   }

   public static RenderType type(int layer) {
      switch (layer) {
         case 0:
            return RenderType.beaconBeam(SOFT, true);
         case 1:
         case 2:
         default:
            return RenderType.eyes(ADD);
         case 3:
            return RenderType.beaconBeam(OVER, true);
         case 4:
            return RenderType.entityTranslucent(DECALS);
         case 5:
            return RenderType.eyes(DECALS_ADD);
         case 6:
            return RenderType.eyes(XHAIR);
      }
   }

   public static boolean additive(int layer) {
      return layer == 1 || layer == 2 || layer == 5 || layer == 6;
   }

   public static float c01(double x) {
      return x < 0.0D ? 0.0F : (x > 1.0D ? 1.0F : (float)x);
   }

   public static float[] uv(int layer, int cell, double s0, double s1, double s2, double s3) {
      double u0;
      double u1;
      double vTop;
      double vBot;
      double eu;
      double ev;
      if (layer == 6) {
         u0 = 0.0D;
         u1 = 1.0D;
         vTop = 0.0D;
         vBot = 1.0D;
         eu = 0.0D;
         ev = 0.0D;
      } else if (layer != 4 && layer != 5) {
         int cx = cell % 8;
         int cy = cell / 8;
         eu = 0.001953125D;
         ev = 0.001736111111111111D;
         u0 = (double)cx / 8.0D;
         u1 = (double)(cx + 1) / 8.0D;
         vTop = (double)cy / 9.0D;
         vBot = (double)(cy + 1) / 9.0D;
      } else {
         int cx = cell % 4;
         int cy = cell / 4;
         eu = 9.765625E-4D;
         ev = 0.0013020833333333333D;
         u0 = (double)cx / 4.0D;
         u1 = (double)(cx + 1) / 4.0D;
         vTop = (double)cy / 3.0D;
         vBot = (double)(cy + 1) / 3.0D;
      }

      u0 += eu;
      u1 -= eu;
      vTop += ev;
      vBot -= ev;
      double du = u1 - u0;
      double dv = vBot - vTop;
      return new float[]{(float)(u0 + du * s0), (float)(vBot - dv * s1), (float)(u0 + du * s2), (float)(vBot - dv * s3)};
   }

   public static double pull(double dx, double dy, double dz) {
      return Math.max(0.5D, 1.0D - Math.sqrt(dx * dx + dy * dy + dz * dz) * 2.4E-6D);
   }

   public static void vtx(VertexConsumer vc, Matrix4f m, double x, double y, double z, double[] cam, float u, float v, float r, float g, float b, float a, int light) {
      double dx = x - cam[0];
      double dy = y - cam[1];
      double dz = z - cam[2];
      double k = pull(dx, dy, dz);
      Vector3f q = m.transformPosition(new Vector3f((float)(dx * k), (float)(dy * k), (float)(dz * k)));
      vc.vertex(q.x(), q.y(), q.z(), c01((double)r), c01((double)g), c01((double)b), c01((double)a), u, v, OverlayTexture.NO_OVERLAY, light, UP[0], UP[1], UP[2]);
   }

   public static void flush(PassQuads q, MultiBufferSource.BufferSource buffers, Matrix4f m, double[] cam, ClientLevel level) {
      for(int layer : ORDER) {
         int n = q.count(layer);
         if (n != 0) {
            RenderType type = type(layer);
            VertexConsumer vc = buffers.getBuffer(type);
            double[] d = q.data(layer);
            boolean add = additive(layer);

            for(int i = 0; i < n; ++i) {
               int o = i * 24;
               int cell = (int)d[o + 12];
               float[] uv = uv(layer, cell, d[o + 13], d[o + 14], d[o + 15], d[o + 16]);
               float r = (float)d[o + 17];
               float g = (float)d[o + 18];
               float b = (float)d[o + 19];
               int light = 15728880;
               if (layer == 4 && level != null) {
                  double cx = (d[o] + d[o + 3] + d[o + 6] + d[o + 9]) / 4.0D;
                  double cy = (d[o + 1] + d[o + 4] + d[o + 7] + d[o + 10]) / 4.0D;
                  double cz = (d[o + 2] + d[o + 5] + d[o + 8] + d[o + 11]) / 4.0D;
                  light = LevelRenderer.getLightColor(level, BlockPos.containing(cx, cy + 0.5D, cz));
               }

               float[] us = new float[]{uv[0], uv[2], uv[2], uv[0]};
               float[] vs = new float[]{uv[1], uv[1], uv[3], uv[3]};
               float[] al = new float[4];
               float[] rr = new float[4];
               float[] gg = new float[4];
               float[] bb = new float[4];

               for(int k = 0; k < 4; ++k) {
                  al[k] = (float)d[o + 20 + k];
                  rr[k] = add ? r * al[k] : r;
                  gg[k] = add ? g * al[k] : g;
                  bb[k] = add ? b * al[k] : b;
               }

               for(int k = 0; k < 4; ++k) {
                  vtx(vc, m, d[o + k * 3], d[o + k * 3 + 1], d[o + k * 3 + 2], cam, us[k], vs[k], rr[k], gg[k], bb[k], al[k], light);
               }

               if (layer != 4) {
                  for(int k = 3; k >= 0; --k) {
                     vtx(vc, m, d[o + k * 3], d[o + k * 3 + 1], d[o + k * 3 + 2], cam, us[k], vs[k], rr[k], gg[k], bb[k], al[k], light);
                  }
               }
            }

            buffers.endBatch(type);
         }
      }

   }
}
