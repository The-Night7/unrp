package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class ShardDraw {
   public static final ShardDraw.Model PYLON = new ShardDraw.Model(1.4F, ShardModels.PYLON_SHAPE, ShardModels.PYLON_GROUPS, ShardModels.PYLON_PARTS);
   public static final ShardDraw.Model WALL = new ShardDraw.Model(1.0F, ShardModels.WALL_SHAPE, ShardModels.WALL_GROUPS, ShardModels.WALL_PARTS);
   public static final ShardDraw.Model TOWER = new ShardDraw.Model(1.4F, ShardModels.TOWER_SHAPE, ShardModels.TOWER_GROUPS, ShardModels.TOWER_PARTS);
   public static final ShardDraw.Model HAND = new ShardDraw.Model(2.0F, ShardModels.HAND_SHAPE, ShardModels.HAND_GROUPS, ShardModels.HAND_PARTS);
   public static final ShardDraw.Model FIST = new ShardDraw.Model(2.6F, ShardModels.FIST_SHAPE, ShardModels.FIST_GROUPS, ShardModels.FIST_PARTS);
   public static final ShardDraw.Model ARROW = new ShardDraw.Model(1.6F, ShardModels.ARROW_SHAPE, ShardModels.ARROW_GROUPS, ShardModels.ARROW_PARTS);
   public static final ShardDraw.Model STAKE = new ShardDraw.Model(1.4F, ShardModels.STAKE_SHAPE, ShardModels.STAKE_GROUPS, ShardModels.STAKE_PARTS);
   public static final ShardDraw.Model[] CONSTRUCTS = new ShardDraw.Model[]{PYLON, WALL, TOWER, HAND};
   public static final int BUILD = 26;
   public static final int SHATTER = 16;
   public static final int MOST_STREAKS = 16;
   private static final int EDGE = 3;
   private static final int LIGHT = 15728880;
   private static final double D2R = (Math.PI / 180D);
   private static final float PIXEL = 0.0018F;
   private static final float[][] FACE_N = new float[][]{{0.0F, 0.0F, 1.0F}, {0.0F, 0.0F, -1.0F}, {1.0F, 0.0F, 0.0F}, {-1.0F, 0.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {0.0F, -1.0F, 0.0F}};
   private static final float[][][] FACE_C = new float[][][]{{{-1.0F, -1.0F, 1.0F}, {1.0F, -1.0F, 1.0F}, {1.0F, 1.0F, 1.0F}, {-1.0F, 1.0F, 1.0F}}, {{1.0F, -1.0F, -1.0F}, {-1.0F, -1.0F, -1.0F}, {-1.0F, 1.0F, -1.0F}, {1.0F, 1.0F, -1.0F}}, {{1.0F, -1.0F, 1.0F}, {1.0F, -1.0F, -1.0F}, {1.0F, 1.0F, -1.0F}, {1.0F, 1.0F, 1.0F}}, {{-1.0F, -1.0F, -1.0F}, {-1.0F, -1.0F, 1.0F}, {-1.0F, 1.0F, 1.0F}, {-1.0F, 1.0F, -1.0F}}, {{-1.0F, 1.0F, 1.0F}, {1.0F, 1.0F, 1.0F}, {1.0F, 1.0F, -1.0F}, {-1.0F, 1.0F, -1.0F}}, {{-1.0F, -1.0F, -1.0F}, {1.0F, -1.0F, -1.0F}, {1.0F, -1.0F, 1.0F}, {-1.0F, -1.0F, 1.0F}}};
   private static final int[][] EDGES = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
   private static final float[][] CORNERS = new float[][]{{-1.0F, -1.0F, -1.0F}, {1.0F, -1.0F, -1.0F}, {1.0F, 1.0F, -1.0F}, {-1.0F, 1.0F, -1.0F}, {-1.0F, -1.0F, 1.0F}, {1.0F, -1.0F, 1.0F}, {1.0F, 1.0F, 1.0F}, {-1.0F, 1.0F, 1.0F}};
   private static final float[][] OCTV = new float[][]{{1.0F, 0.0F, 0.0F}, {-1.0F, 0.0F, 0.0F}, {0.0F, 1.0F, 0.0F}, {0.0F, -1.0F, 0.0F}, {0.0F, 0.0F, 1.0F}, {0.0F, 0.0F, -1.0F}};
   private static final int[][] OCTF = new int[][]{{0, 2, 4}, {4, 2, 1}, {1, 2, 5}, {5, 2, 0}, {4, 3, 0}, {1, 3, 4}, {5, 3, 1}, {0, 3, 5}};
   private static final int[][] OCTE = new int[][]{{0, 2}, {1, 2}, {4, 2}, {5, 2}, {0, 3}, {1, 3}, {4, 3}, {5, 3}, {0, 4}, {4, 1}, {1, 5}, {5, 0}};
   private static final float[] OCT_UV = new float[]{0.2F, 0.8F, 0.8F, 0.8F, 0.5F, 0.2F, 0.5F, 0.2F};
   private static final WeaveQuads Q = new WeaveQuads();
   private static final int MOST_PARTS = most(true);
   private static final int MOST_GROUPS = most(false);
   private static final float[][] GM = new float[MOST_GROUPS][12];
   private static final float[][] PM = new float[MOST_PARTS][12];
   private static final float[][] BM = new float[MOST_PARTS][12];
   private static final float[] PA = new float[MOST_PARTS];
   private static final float[] GROW = new float[MOST_PARTS];
   private static final boolean[] LIVE = new boolean[MOST_PARTS];
   private static final float[] ROOT = new float[12];
   private static final float[] A = new float[12];
   private static final float[] B = new float[12];
   private static final float[] T = new float[12];
   private static final float[] PTS = new float[24];
   private static final float[] UV = new float[8];
   private static final float[] V = new float[3];
   private static final float[] W = new float[3];
   private static final float[] POST = new float[12];

   private ShardDraw() {
   }

   private static int most(boolean parts) {
      int n = 1;

      for(ShardDraw.Model m : new ShardDraw.Model[]{PYLON, WALL, TOWER, HAND, FIST, ARROW, STAKE}) {
         n = Math.max(n, parts ? m.parts.length : m.groups.length);
      }

      return n;
   }

   static void ident(float[] o) {
      for(int i = 0; i < 12; ++i) {
         o[i] = 0.0F;
      }

      o[0] = 1.0F;
      o[5] = 1.0F;
      o[10] = 1.0F;
   }

   static void trans(float[] o, float x, float y, float z) {
      ident(o);
      o[3] = x;
      o[7] = y;
      o[11] = z;
   }

   static void scl(float[] o, float k) {
      ident(o);
      o[0] = k;
      o[5] = k;
      o[10] = k;
   }

   static void rot(float[] o, int axis, float deg) {
      ident(o);
      double a = (double)deg * (Math.PI / 180D);
      float c = (float)Math.cos(a);
      float s = (float)Math.sin(a);
      if (axis == 0) {
         o[5] = c;
         o[6] = -s;
         o[9] = s;
         o[10] = c;
      } else if (axis == 1) {
         o[0] = c;
         o[2] = s;
         o[8] = -s;
         o[10] = c;
      } else {
         o[0] = c;
         o[1] = -s;
         o[4] = s;
         o[5] = c;
      }

   }

   static void mul(float[] a, float[] b, float[] o) {
      for(int r = 0; r < 3; ++r) {
         float a0 = a[r * 4];
         float a1 = a[r * 4 + 1];
         float a2 = a[r * 4 + 2];
         o[r * 4] = a0 * b[0] + a1 * b[4] + a2 * b[8];
         o[r * 4 + 1] = a0 * b[1] + a1 * b[5] + a2 * b[9];
         o[r * 4 + 2] = a0 * b[2] + a1 * b[6] + a2 * b[10];
         o[r * 4 + 3] = a0 * b[3] + a1 * b[7] + a2 * b[11] + a[r * 4 + 3];
      }

   }

   static void post(float[] m, float[] b) {
      mul(m, b, POST);
      System.arraycopy(POST, 0, m, 0, 12);
   }

   static void app(float[] m, float x, float y, float z, float[] out, int o) {
      out[o] = m[0] * x + m[1] * y + m[2] * z + m[3];
      out[o + 1] = m[4] * x + m[5] * y + m[6] * z + m[7];
      out[o + 2] = m[8] * x + m[9] * y + m[10] * z + m[11];
   }

   static void dir(float[] m, float x, float y, float z, float[] out) {
      out[0] = m[0] * x + m[1] * y + m[2] * z;
      out[1] = m[4] * x + m[5] * y + m[6] * z;
      out[2] = m[8] * x + m[9] * y + m[10] * z;
   }

   static float sin(double x) {
      return (float)Math.sin(x);
   }

   static float lerp(float a, float b, float k) {
      return a + (b - a) * k;
   }

   static float clamp01(float v) {
      return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
   }

   static float ease3(float k) {
      k = clamp01(k);
      float j = 1.0F - k;
      return 1.0F - j * j * j;
   }

   static float hashf(int i, int salt) {
      double x = Math.sin((double)i * 127.1D + (double)salt * 311.7D) * 43758.5453D;
      return (float)(x - Math.floor(x));
   }

   static float shadeK(int sh) {
      return sh == 1 ? 0.45F : (sh == 2 ? 0.2F : (sh == 3 ? 0.25F : 0.0F));
   }

   private static void anim(float[] o, float[] g, float t, float aw) {
      float a = g[5];
      float b = g[6];
      float c = g[7];
      float d = g[8];
      float e = g[9];
      float f = g[10];
      int kind = (int)g[4];
      if (kind == 1) {
         rot(o, (int)a, d + t * lerp(b, c, aw));
      } else if (kind == 2) {
         trans(o, 0.0F, a * sin((double)(t * b + c)), 0.0F);
      } else if (kind == 3) {
         rot(o, 0, (lerp(a, b, aw) + aw * c * sin((double)(t * d + e))) * f);
      } else if (kind == 4) {
         rot(o, (int)a, lerp(b, c, aw * (0.5F + 0.5F * sin((double)(t * d + e)))));
      } else if (kind == 5) {
         trans(o, 0.0F, 0.0F, a * sin((double)(t * b + c)) * (0.3F + 0.7F * aw));
      } else if (kind == 6) {
         rot(o, (int)a, b);
      } else {
         ident(o);
      }

   }

   private static void prepare(ShardDraw.Model m, ShardDraw.Look o) {
      float aw = o.awake ? 1.0F : 0.0F;

      for(int i = 0; i < m.groups.length; ++i) {
         float[] g = m.groups[i];
         trans(A, g[1], g[2], g[3]);
         anim(T, g, o.t, aw);
         mul(A, T, B);
         trans(T, -g[1], -g[2], -g[3]);
         if (i == 0) {
            mul(B, T, GM[0]);
         } else {
            mul(B, T, A);
            mul(GM[(int)g[0]], A, GM[i]);
         }
      }

      scl(T, m.scale);
      mul(o.root, T, ROOT);
      int n = m.parts.length;
      float step = 18.0F / (float)n;
      float core = m.shape[2];

      for(int k = 0; k < n; ++k) {
         float[] p = m.parts[k];
         int gi = (int)p[1];
         float pa = p[11] * o.alpha;
         float pulse = m.groups[gi][11];
         if (pulse >= 0.0F && o.awake) {
            pa *= 1.0F + 0.35F * Math.max(0.0F, sin((double)(o.t * 0.12F - pulse)));
         }

         float grow = 1.0F;
         LIVE[k] = true;
         if (o.spawn >= 0.0F) {
            float pk = clamp01((o.spawn - (float)k * step) / 6.0F);
            if (pk <= 0.0F) {
               LIVE[k] = false;
               continue;
            }

            grow = 0.25F + 0.75F * ease3(pk);
            pa *= pk;
         }

         float ox = 0.0F;
         float oy = 0.0F;
         float oz = 0.0F;
         float e = o.explode;
         if (e > 0.0F) {
            ox = p[2] * e * 1.3F + (hashf(k, 1) - 0.5F) * e;
            oy = (p[3] - core) * e * 0.8F - e * e * 0.6F;
            oz = p[4] * e * 1.3F + (hashf(k, 2) - 0.5F) * e;
            pa *= (float)Math.pow((double)(1.0F - e), 1.5D);
            grow *= 1.0F - 0.5F * e;
         }

         PA[k] = pa;
         GROW[k] = grow;
         mul(ROOT, GM[gi], A);
         trans(T, p[2] + ox, p[3] + oy, p[4] + oz);
         mul(A, T, BM[k]);
         rot(T, 1, p[9]);
         mul(BM[k], T, A);
         rot(T, 0, p[8]);
         mul(A, T, B);
         rot(T, 2, p[10]);
         mul(B, T, A);
         scl(T, grow);
         mul(A, T, PM[k]);
      }

   }

   public static void draw(ShardDraw.Model m, ShardDraw.Look o, PoseStack pose, MultiBufferSource buffers, ResourceLocation glass) {
      PoseStack.Pose top = pose.last();
      Matrix4f pm = top.pose();
      Matrix3f nm = top.normal();
      Matrix4f inv = (new Matrix4f(pm)).invert();
      Vector3f eye = inv.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
      Vector3f right = inv.transformDirection(new Vector3f(1.0F, 0.0F, 0.0F)).normalize();
      Vector3f up = inv.transformDirection(new Vector3f(0.0F, 1.0F, 0.0F)).normalize();
      prepare(m, o);
      int n = m.parts.length;
      float wake = o.awake ? 1.0F : 0.6F;
      float shimmer = o.awake ? 1.0F + 0.07F * sin((double)(o.t * 0.25F)) : 1.0F;
      if (!o.ghost) {
         VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(glass));

         for(int k = 0; k < n; ++k) {
            if (LIVE[k]) {
               glassPart(vc, pm, nm, m, o, k, PA[k] * wake * shimmer);
            }
         }
      }

      float dx = ROOT[3] - eye.x();
      float dy = ROOT[7] - eye.y();
      float dz = ROOT[11] - eye.z();
      float floor = (float)Math.sqrt((double)(dx * dx + dy * dy + dz * dz)) * 0.0018F;
      Q.begin(buffers.getBuffer(LatticeRenderer.soft()), pm, 1, 0.0D, 0.0D, 0.0D, right.x(), right.y(), right.z(), up.x(), up.y(), up.z());
      Q.eye((double)eye.x(), (double)eye.y(), (double)eye.z());

      for(int k = 0; k < n; ++k) {
         if (LIVE[k]) {
            softPart(m, o, k, floor);
         }
      }

      float br = o.r + (1.0F - o.r) * 0.45F;
      float bg = o.g + (1.0F - o.g) * 0.45F;
      float bb = o.b + (1.0F - o.b) * 0.45F;

      for(int i = 0; i < o.streakCount; ++i) {
         int s = i * 6;
         float[] st = o.streaks;
         app(ROOT, st[s], st[s + 1], st[s + 2], PTS, 0);
         app(ROOT, st[s], st[s + 1], st[s + 2] - st[s + 3], PTS, 3);
         Q.ribbon((double)PTS[0], (double)PTS[1], (double)PTS[2], (double)PTS[3], (double)PTS[4], (double)PTS[5], Math.max(st[s + 4], floor), br, bg, bb, st[s + 5], 0.0F, 4);
      }

      swarm(m, o);
      if (!o.ghost) {
         boolean glows = false;

         for(int k = 0; k < n; ++k) {
            if (LIVE[k] && (int)m.parts[k][0] == 5) {
               glows = true;
               break;
            }
         }

         if (glows) {
            Q.begin(buffers.getBuffer(LatticeRenderer.bloom()), pm, 2, 0.0D, 0.0D, 0.0D, right.x(), right.y(), right.z(), up.x(), up.y(), up.z());
            Q.eye((double)eye.x(), (double)eye.y(), (double)eye.z());

            for(int k = 0; k < n; ++k) {
               float[] p = m.parts[k];
               if (LIVE[k] && (int)p[0] == 5) {
                  app(PM[k], 0.0F, 0.0F, 0.0F, PTS, 0);
                  float size = p[5] * m.scale * GROW[k] * (0.9F + 0.1F * sin((double)(o.t * 0.2F + (float)k)));
                  Q.bill((double)PTS[0], (double)PTS[1], (double)PTS[2], size, o.r, o.g, o.b, PA[k] * (o.awake ? 1.0F : 0.65F), 3, 2);
               }
            }

         }
      }
   }

   private static void glassPart(VertexConsumer vc, Matrix4f pm, Matrix3f nm, ShardDraw.Model m, ShardDraw.Look o, int k, float a) {
      float[] p = m.parts[k];
      int kind = (int)p[0];
      if (kind == 0 || kind == 1 || kind == 3) {
         float sk = shadeK((int)p[12]);
         float cr = o.r + (1.0F - o.r) * sk;
         float cg = o.g + (1.0F - o.g) * sk;
         float cb = o.b + (1.0F - o.b) * sk;
         float[] M = PM[k];
         float hx = p[5];
         float hy = p[6];
         float hz = p[7];
         if (kind == 3) {
            for(int[] f : OCTF) {
               for(int i = 0; i < 3; ++i) {
                  float[] c = OCTV[f[i]];
                  app(M, c[0] * hx, c[1] * hy, c[2] * hz, PTS, i * 3);
               }

               System.arraycopy(PTS, 6, PTS, 9, 3);
               float ux = PTS[3] - PTS[0];
               float uy = PTS[4] - PTS[1];
               float uz = PTS[5] - PTS[2];
               float vx = PTS[6] - PTS[0];
               float vy = PTS[7] - PTS[1];
               float vz = PTS[8] - PTS[2];
               quad(vc, pm, nm, uy * vz - uz * vy, uz * vx - ux * vz, ux * vy - uy * vx, OCT_UV, cr, cg, cb, a);
            }

         } else {
            for(int fi = 0; fi < 6; ++fi) {
               float[][] fc = FACE_C[fi];

               for(int i = 0; i < 4; ++i) {
                  app(M, fc[i][0] * hx, fc[i][1] * hy, fc[i][2] * hz, PTS, i * 3);
               }

               dir(M, FACE_N[fi][0], FACE_N[fi][1], FACE_N[fi][2], V);
               float fw = fi < 2 ? hx : (fi < 4 ? hz : hx);
               float fh = fi < 4 ? hy : hz;
               float uw = Math.min(1.0F, fw * m.scale);
               float vh = Math.min(1.0F, fh * m.scale);
               float u0 = hashf(k * 7 + fi, 3) * (1.0F - uw);
               float v0 = hashf(k * 7 + fi, 4) * (1.0F - vh);
               UV[0] = u0;
               UV[1] = v0 + vh;
               UV[2] = u0 + uw;
               UV[3] = v0 + vh;
               UV[4] = u0 + uw;
               UV[5] = v0;
               UV[6] = u0;
               UV[7] = v0;
               quad(vc, pm, nm, V[0], V[1], V[2], UV, cr, cg, cb, a);
            }

         }
      }
   }

   private static void quad(VertexConsumer vc, Matrix4f pm, Matrix3f nm, float nx, float ny, float nz, float[] uv, float r, float g, float b, float a) {
      float l = (float)Math.sqrt((double)(nx * nx + ny * ny + nz * nz));
      if (!(l < 1.0E-9F)) {
         if (a > 1.0F) {
            a = 1.0F;
         }

         if (!(a <= 0.003F)) {
            nx /= l;
            ny /= l;
            nz /= l;

            for(int i = 0; i < 4; ++i) {
               vc.vertex(pm, PTS[i * 3], PTS[i * 3 + 1], PTS[i * 3 + 2]).color(r, g, b, a).uv(uv[i * 2], uv[i * 2 + 1]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(nm, nx, ny, nz).endVertex();
            }

         }
      }
   }

   private static void softPart(ShardDraw.Model m, ShardDraw.Look o, int k, float floor) {
      float[] p = m.parts[k];
      int kind = (int)p[0];
      int shade = (int)p[12];
      float pa = PA[k];
      float wakeE = o.awake ? 1.0F : 0.75F;
      float es = Math.min(m.scale, 2.0F);
      float sk = shadeK(shade == 0 ? 3 : shade);
      float er = o.r + (1.0F - o.r) * sk;
      float eg = o.g + (1.0F - o.g) * sk;
      float eb = o.b + (1.0F - o.b) * sk;
      float[] M = PM[k];
      float hx = p[5];
      float hy = p[6];
      float hz = p[7];
      if (kind != 0 && kind != 2) {
         if (kind == 3) {
            for(int i = 0; i < 6; ++i) {
               app(M, OCTV[i][0] * hx, OCTV[i][1] * hy, OCTV[i][2] * hz, PTS, i * 3);
            }

            float ea_0 = (o.ghost ? pa : Math.min(1.0F, pa * 1.15F)) * wakeE;
            float w_0 = Math.max(0.014F * es, floor);

            for(int[] e_0 : OCTE) {
               int a_0 = e_0[0] * 3;
               int b_0 = e_0[1] * 3;
               Q.ribbon((double)PTS[a_0], (double)PTS[a_0 + 1], (double)PTS[a_0 + 2], (double)PTS[b_0], (double)PTS[b_0 + 1], (double)PTS[b_0 + 2], w_0, er, eg, eb, ea_0, ea_0, 4);
            }
         } else if (kind == 4 && !o.ghost) {
            float ck = shadeK(shade);
            float cr = o.r + (1.0F - o.r) * ck;
            float cg = o.g + (1.0F - o.g) * ck;
            float cb = o.b + (1.0F - o.b) * ck;
            float lr = o.r + (1.0F - o.r) * 0.45F;
            float lg = o.g + (1.0F - o.g) * 0.45F;
            float lb = o.b + (1.0F - o.b) * 0.45F;
            float wakeC = o.awake ? 1.0F : 0.5F;
            int count = (int)p[13];

            for(int i = 0; i < count; ++i) {
               float h1 = hashf(k * 31 + i, 5);
               float h2 = hashf(k * 31 + i, 6);
               float h3 = hashf(k * 31 + i, 7);
               float ly = (h2 * 2.0F - 1.0F) * hy;
               if (p[8] > 0.0F) {
                  float ph = o.t * p[8] + h2;
                  ly = -hy + (ph - (float)Math.floor((double)ph)) * 2.0F * hy;
               }

               float lx = (h1 * 2.0F - 1.0F) * hx + 0.03F * sin((double)(o.t * 0.11F + (float)i * 1.9F));
               float lz = (h3 * 2.0F - 1.0F) * hz + 0.03F * (float)Math.cos((double)(o.t * 0.09F + (float)i * 2.3F));
               app(BM[k], lx, ly, lz, PTS, 0);
               float tw = 0.55F + 0.45F * Math.abs(sin((double)(o.t * 0.13F + (float)i * 1.7F)));
               float size = (0.035F + 0.025F * h1) * m.scale * (count == 1 ? 1.3F : 1.0F);
               boolean glint = i % 3 == 0;
               Q.bill((double)PTS[0], (double)PTS[1], (double)PTS[2], size, glint ? lr : cr, glint ? lg : cg, glint ? lb : cb, pa * tw * wakeC, glint ? 1 : 0, 1);
            }
         }
      } else {
         for(int i = 0; i < 8; ++i) {
            app(M, CORNERS[i][0] * hx, CORNERS[i][1] * hy, CORNERS[i][2] * hz, PTS, i * 3);
         }

         float ea = (kind != 2 && !o.ghost ? Math.min(1.0F, pa * 1.15F) : pa) * wakeE;
         float w = Math.max((kind == 2 ? 0.024F : 0.016F) * es, floor);

         for(int[] e : EDGES) {
            int a = e[0] * 3;
            int b = e[1] * 3;
            Q.ribbon((double)PTS[a], (double)PTS[a + 1], (double)PTS[a + 2], (double)PTS[b], (double)PTS[b + 1], (double)PTS[b + 2], w, er, eg, eb, ea, ea, 4);
         }
      }

   }

   private static void swarm(ShardDraw.Model m, ShardDraw.Look o) {
      if (!(o.spawn < 0.0F) && !(o.spawn >= 26.0F)) {
         float sk = clamp01(o.spawn / 22.0F);
         float lr = o.r + (1.0F - o.r) * 0.45F;
         float lg = o.g + (1.0F - o.g) * 0.45F;
         float lb = o.b + (1.0F - o.b) * 0.45F;

         for(int s = 0; s < 22; ++s) {
            float ang = (float)s * 2.4F + o.spawn * 0.25F;
            float r0 = (1.0F - ease3(sk)) * (0.8F + 0.6F * hashf(s, 9)) + 0.15F;
            float yy = 0.2F + hashf(s, 10) * m.shape[1];
            app(ROOT, (float)Math.cos((double)ang) * r0 * m.shape[0], yy, sin((double)ang) * r0, PTS, 0);
            boolean lit = s % 3 == 0;
            Q.bill((double)PTS[0], (double)PTS[1], (double)PTS[2], 0.06F * m.scale, lit ? lr : o.r, lit ? lg : o.g, lit ? lb : o.b, (1.0F - sk) * 0.9F, 0, 1);
         }

      }
   }

   public static final class Look {
      public float t;
      public boolean awake = true;
      public float r = 1.0F;
      public float g = 1.0F;
      public float b = 1.0F;
      public float alpha = 1.0F;
      public float spawn = -1.0F;
      public float explode;
      public boolean ghost;
      final float[] root = new float[12];
      final float[] streaks = new float[96];
      int streakCount;

      public ShardDraw.Look begin(float t, boolean awake, int rgb) {
         this.t = t;
         this.awake = awake;
         this.r = (float)(rgb >> 16 & 255) / 255.0F;
         this.g = (float)(rgb >> 8 & 255) / 255.0F;
         this.b = (float)(rgb & 255) / 255.0F;
         this.alpha = 1.0F;
         this.spawn = -1.0F;
         this.explode = 0.0F;
         this.ghost = false;
         this.streakCount = 0;
         ShardDraw.ident(this.root);
         return this;
      }

      public ShardDraw.Look move(float x, float y, float z) {
         ShardDraw.trans(ShardDraw.T, x, y, z);
         ShardDraw.post(this.root, ShardDraw.T);
         return this;
      }

      public ShardDraw.Look turn(int axis, float deg) {
         ShardDraw.rot(ShardDraw.T, axis, deg);
         ShardDraw.post(this.root, ShardDraw.T);
         return this;
      }

      public ShardDraw.Look grow(float k) {
         ShardDraw.scl(ShardDraw.T, k);
         ShardDraw.post(this.root, ShardDraw.T);
         return this;
      }

      public ShardDraw.Look streak(float x, float y, float z, float len, float w, float a) {
         if (this.streakCount < 16) {
            int o = this.streakCount * 6;
            this.streaks[o] = x;
            this.streaks[o + 1] = y;
            this.streaks[o + 2] = z;
            this.streaks[o + 3] = len;
            this.streaks[o + 4] = w;
            this.streaks[o + 5] = a;
            ++this.streakCount;
         }

         return this;
      }
   }

   public static final class Model {
      final float scale;
      final float[] shape;
      final float[][] groups;
      final float[][] parts;

      Model(float scale, float[] shape, float[][] groups, float[][] parts) {
         this.scale = scale;
         this.shape = shape;
         this.groups = groups;
         this.parts = parts;
      }

      public float scale() {
         return this.scale;
      }
   }
}
