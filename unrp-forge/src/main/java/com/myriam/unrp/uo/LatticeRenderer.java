package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class LatticeRenderer implements BlockEntityRenderer<LatticeBlockEntity> {
   public static final ResourceLocation ATLAS = new ResourceLocation("unrp", "textures/entity/weave_atlas.png");
   public static final ResourceLocation GLOW_TEX = new ResourceLocation("unrp", "textures/entity/weave_glow.png");
   private static final int SPECKS = 26;
   private static final int GLOWS = 6;
   public static final int FORM = 14;
   public static final int FADE = 60;
   public static final long OLD = -9223372036854775807L;
   private static final float[][] MESH = new float[][]{{0.5F, 0.0F, 0.0F, 0.5F}, {1.0F, 0.0F, 0.0F, 1.0F}, {1.0F, 0.5F, 0.5F, 1.0F}, {0.0F, 0.5F, 0.5F, 1.0F}, {0.0F, 0.0F, 1.0F, 1.0F}, {0.5F, 0.0F, 1.0F, 0.5F}};
   private static final float[][] NODES = new float[][]{{0.25F, 0.25F}, {0.75F, 0.25F}, {0.25F, 0.75F}, {0.75F, 0.75F}, {0.5F, 0.5F}};
   private static final float[][] EDGES = new float[][]{{0.0F, 0.0F, 1.0F, 0.0F}, {0.0F, 1.0F, 1.0F, 1.0F}, {0.0F, 0.0F, 0.0F, 1.0F}, {1.0F, 0.0F, 1.0F, 1.0F}};
   private static final int[][][] SIDES = new int[][][]{{{0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}}, {{0, -1, 0}, {0, 1, 0}, {-1, 0, 0}, {1, 0, 0}}, {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}}};

   public LatticeRenderer(BlockEntityRendererProvider.Context ctx) {
   }

   public static RenderType soft() {
      return RenderType.entityTranslucentEmissive(ATLAS);
   }

   public static RenderType bloom() {
      return RenderType.eyes(GLOW_TEX);
   }

   public void render(LatticeBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
      if (be.getLevel() != null) {
         LatticeFx.seen(be);
      }
   }

   static float hash(long h, int salt) {
      long x = h ^ (long)salt * -7046029254386353131L;
      x ^= x >>> 33;
      x *= -49064778989728563L;
      x ^= x >>> 33;
      return (float)(x >>> 40 & 16777215L) / 1.6777216E7F;
   }

   static float[] table(BlockPos pos) {
      long seed = pos.asLong();
      float[] out = new float[320];

      for(int i = 0; i < 32; ++i) {
         long h = seed * 31L + (long)i * 7919L;

         for(int s = 0; s < 10; ++s) {
            out[i * 10 + s] = hash(h, s + 1);
         }
      }

      return out;
   }

   static int orientIndex(ParticleBlock.Orientation o) {
      return o == ParticleBlock.Orientation.WALL_NS ? 1 : (o == ParticleBlock.Orientation.WALL_EW ? 2 : 0);
   }

   static float clamp01(float v) {
      return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
   }

   static float smooth(float k) {
      k = clamp01(k);
      return k * k * (3.0F - 2.0F * k);
   }

   static float ease3(float k) {
      k = clamp01(k);
      float j = 1.0F - k;
      return 1.0F - j * j * j;
   }

   static float pulse(float t, float a) {
      float s = Mth.sin(a * 1.15F - t * 0.16F);
      if (s <= 0.0F) {
         return 0.0F;
      } else {
         float s2 = s * s;
         float s4 = s2 * s2;
         return s4 * s4;
      }
   }

   private static boolean joins(Level level, BlockPos pos, int o, int k, int[] d) {
      if (o == 0) {
         for(int dy = -1; dy <= 1; ++dy) {
            BlockPos at = pos.offset(d[0], dy, d[2]);
            BlockState n_0 = level.getBlockState(at);
            if (n_0.is((Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get()) && n_0.getValue(ParticleBlock.ORIENTATION) == ParticleBlock.Orientation.FLOOR) {
               int kk = 4 * at.getY() + n_0.getValue(ParticleBlock.LAYER) + 1;
               if (Math.abs(kk - k) <= 2) {
                  return true;
               }
            }
         }

         return false;
      } else {
         BlockState n = level.getBlockState(pos.offset(d[0], d[1], d[2]));
         return n.is((Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get()) && orientIndex((ParticleBlock.Orientation)n.getValue(ParticleBlock.ORIENTATION)) == o;
      }
   }

   static int rims(Level level, BlockPos pos, int o, int layer) {
      int k = 4 * pos.getY() + layer + 1;
      int mask = 0;

      for(int e = 0; e < 4; ++e) {
         if (!joins(level, pos, o, k, SIDES[o][e])) {
            mask |= 1 << e;
         }
      }

      return mask;
   }

   static void place(double[] out, int o, int bx, int by, int bz, float top, float u, float v, float w, float t) {
      float lx;
      float ly;
      float lz;
      if (o == 1) {
         lx = u;
         ly = v;
         lz = 0.5F + w;
      } else if (o == 2) {
         lx = 0.5F + w;
         ly = v;
         lz = u;
      } else {
         lx = u;
         ly = top - 0.05F + w;
         lz = v;
      }

      float wx = (float)bx;
      float wz = (float)bz;
      float wave = 0.06F * Mth.sin(t * 0.1F + (wx + lx) * 1.7F + (wz + lz) * 1.3F);
      out[0] = (double)((float)bx + lx + (o == 2 ? wave : 0.0F));
      out[1] = (double)((float)by + ly + (o == 0 ? wave : 0.0F));
      out[2] = (double)((float)bz + lz + (o == 1 ? wave : 0.0F));
   }

   static float[] toward(float r, float g, float b, float k) {
      return new float[]{r + (1.0F - r) * k, g + (1.0F - g) * k, b + (1.0F - b) * k};
   }

   static void drawPane(WeaveQuads q, LatticeBlockEntity be, long now, float partialTick) {
      Level level = be.getLevel();
      if (level != null) {
         BlockState state = be.getBlockState();
         if (state.is((Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get())) {
            if (be.firstAt == Long.MIN_VALUE) {
               be.firstAt = now;
            }

            if (be.seenAt == Long.MIN_VALUE) {
               long born = be.getBorn();
               if (born > 0L) {
                  be.seenAt = now - born < 40L ? now : -9223372036854775807L;
               } else {
                  if (now - be.firstAt < 3L) {
                     return;
                  }

                  be.seenAt = -9223372036854775807L;
               }
            }

            int color = be.getOwner() == null ? AuraColorResolver.forViewer(15160448) : AuraColorResolver.forOwner(be.getOwner(), 15160448);
            float r = (float)(color >> 16 & 255) / 255.0F;
            float g = (float)(color >> 8 & 255) / 255.0F;
            float b = (float)(color & 255) / 255.0F;
            int o = orientIndex((ParticleBlock.Orientation)state.getValue(ParticleBlock.ORIENTATION));
            int blockLayer = state.getValue(ParticleBlock.LAYER);
            float top = (float)(blockLayer + 1) * 0.25F;
            float t = (float)(now % 24000L) + partialTick;
            BlockPos pos = be.getBlockPos();
            int bx = pos.getX();
            int by = pos.getY();
            int bz = pos.getZ();
            if (be.specks == null) {
               be.specks = table(pos);
            }

            if (be.rimAt != now) {
               be.rimAt = now;
               be.rimMask = rims(level, pos, o, blockLayer);
            }

            float f = be.seenAt == -9223372036854775807L ? 1.0F : clamp01(((float)(now - be.seenAt) + partialTick) / 14.0F);
            float fk = be.getFade() > 0L ? clamp01(((float)(now - be.getFade()) + partialTick) / 60.0F) : 0.0F;
            float nx = o == 2 ? 1.0F : 0.0F;
            float ny = o == 0 ? 1.0F : 0.0F;
            float nz = o == 1 ? 1.0F : 0.0F;
            int wu = o == 2 ? bz : bx;
            int wv = o == 0 ? bz : by;
            double dcx = (double)bx + 0.5D - q.cx;
            double dcy = (double)by + 0.5D - q.cy;
            double dcz = (double)bz + 0.5D - q.cz;
            boolean far = dcx * dcx + dcy * dcy + dcz * dcz > 784.0D;
            double[] A = new double[3];
            double[] B = new double[3];
            float grow0 = smooth((f - 0.1F) / 0.7F);
            if (grow0 > 0.0F) {
               float filmA = 0.075F * grow0 * (1.0F - 0.6F * fk);
               double[] c = new double[12];
               place(A, o, bx, by, bz, top, 0.0F, 0.0F, 0.0F, t);
               c[0] = A[0];
               c[1] = A[1];
               c[2] = A[2];
               place(A, o, bx, by, bz, top, 1.0F, 0.0F, 0.0F, t);
               c[3] = A[0];
               c[4] = A[1];
               c[5] = A[2];
               place(A, o, bx, by, bz, top, 1.0F, 1.0F, 0.0F, t);
               c[6] = A[0];
               c[7] = A[1];
               c[8] = A[2];
               place(A, o, bx, by, bz, top, 0.0F, 1.0F, 0.0F, t);
               c[9] = A[0];
               c[10] = A[1];
               c[11] = A[2];
               q.fill(c, r, g, b, filmA);
            }

            float meshA = 0.3F * (1.0F - 0.65F * fk);
            if (fk > 0.0F) {
               meshA *= 0.7F + 0.3F * Mth.sin(t * 0.9F + (float)(wu + wv) * 2.1F);
            }

            float grow = smooth((f - 0.2F) / 0.6F);
            if (grow > 0.0F) {
               for(float[] s : MESH) {
                  float mu = (s[0] + s[2]) * 0.5F;
                  float mv = (s[1] + s[3]) * 0.5F;
                  float hu = (s[2] - s[0]) * 0.5F * grow;
                  float hv = (s[3] - s[1]) * 0.5F * grow;
                  place(A, o, bx, by, bz, top, mu - hu, mv - hv, 0.01F, t);
                  place(B, o, bx, by, bz, top, mu + hu, mv + hv, 0.01F, t);
                  float pa = pulse(t, (float)wu + mu - hu + (float)wv + mv - hv);
                  float pb = pulse(t, (float)wu + mu + hu + (float)wv + mv + hv);
                  q.flat(A[0], A[1], A[2], B[0], B[1], B[2], nx, ny, nz, 0.046F, r, g, b, meshA * grow * (1.0F + 1.7F * pa), meshA * grow * (1.0F + 1.7F * pb), 4);
               }
            }

            for(int e = 0; e < 4; ++e) {
               if ((be.rimMask & 1 << e) != 0) {
                  float[] E = EDGES[e];
                  place(A, o, bx, by, bz, top, E[0], E[1], 0.012F, t);
                  place(B, o, bx, by, bz, top, E[2], E[3], 0.012F, t);
                  float rimA = 0.55F * grow * (1.0F - 0.4F * fk) * (fk > 0.0F ? 0.6F + 0.4F * Mth.sin(t * 0.7F) : 1.0F);
                  q.flat(A[0], A[1], A[2], B[0], B[1], B[2], nx, ny, nz, 0.07F, r, g, b, rimA, rimA, 4);
                  if (!far) {
                     place(A, o, bx, by, bz, top, (E[0] + E[2]) * 0.5F, (E[1] + E[3]) * 0.5F, 0.015F, t);
                     q.bill(A[0], A[1], A[2], 0.05F, r, g, b, rimA * 1.4F, 2, 1);
                  }
               }
            }

            if (!far && f > 0.45F) {
               float pop = 1.0F + 0.8F * (1.0F - smooth((f - 0.45F) / 0.4F));
               float[] kc = toward(r, g, b, 0.25F);

               for(float[] nd : NODES) {
                  float pk = pulse(t, (float)wu + nd[0] + (float)wv + nd[1]);
                  float na = (0.55F + 0.45F * pk) * (1.0F - 0.6F * fk);
                  place(A, o, bx, by, bz, top, nd[0], nd[1], 0.015F, t);
                  q.bill(A[0], A[1], A[2], 0.05F * pop, kc[0], kc[1], kc[2], na, 2, 1);
               }
            }

            float[] gl = toward(r, g, b, 0.45F);
            float[] h = be.specks;

            for(int i = 0; i < 32; ++i) {
               boolean glow = i >= 26;
               if (!far || glow || (i & 1) != 1) {
                  int at = i * 10;
                  if (glow || !(fk > 0.0F) || !(h[at + 8] < fk * 0.7F)) {
                     float phase = h[at + 3] * 6.2832F;
                     float speed = 0.03F + h[at + 4] * 0.03F;
                     float drift = 0.06F * Mth.sin(t * speed + phase);
                     float lu = h[at] + drift;
                     float lv = o == 0 ? h[at + 1] - drift * 0.5F : h[at + 1];
                     float lw = o == 0 ? -0.09F + h[at + 2] * 0.16F : (h[at + 2] - 0.5F) * 0.16F;
                     place(A, o, bx, by, bz, top, lu, lv, lw, t);
                     float twinkle = 0.45F + 0.55F * Mth.abs(Mth.sin(t * (0.07F + h[at + 5] * 0.05F) + phase));
                     boolean glint = !glow && i % 4 == 0;
                     float size = glow ? 0.14F + h[at + 6] * 0.1F : (glint ? 0.045F + h[at + 6] * 0.03F : 0.05F + h[at + 6] * 0.05F);
                     float a = glow ? 0.1F + 0.08F * twinkle : twinkle;
                     double px = A[0];
                     double py = A[1];
                     double pz = A[2];
                     if (f < 1.0F) {
                        float kk = clamp01((f - h[at + 7] * 0.45F) / 0.55F);
                        float ee = ease3(kk);
                        float ang = (1.0F - ee) * 2.4F + h[at + 9] * 6.2832F;
                        float r0 = 0.55F + 0.35F * h[at + 9];
                        double cx = (double)bx + 0.5D + (double)nx * 0.3D;
                        double cy = (double)((float)by + (o == 0 ? top : 0.5F)) + (double)ny * 0.3D;
                        double cz = (double)bz + 0.5D + (double)nz * 0.3D;
                        float ca = Mth.cos(ang) * r0;
                        float sa = Mth.sin(ang) * r0;
                        double sx;
                        double sy;
                        double sz;
                        if (o == 0) {
                           sx = cx + (double)ca;
                           sy = cy;
                           sz = cz + (double)sa;
                        } else if (o == 1) {
                           sx = cx + (double)ca;
                           sy = cy + (double)sa;
                           sz = cz;
                        } else {
                           sx = cx;
                           sy = cy + (double)sa;
                           sz = cz + (double)ca;
                        }

                        px = sx + (px - sx) * (double)ee;
                        py = sy + (py - sy) * (double)ee;
                        pz = sz + (pz - sz) * (double)ee;
                        a *= clamp01(kk * 1.8F);
                        size *= 1.0F + 0.7F * (1.0F - ee);
                     }

                     if (fk > 0.0F && !glow) {
                        a *= (1.0F - 0.35F * fk) * (0.6F + 0.4F * Mth.sin(t * 1.9F + h[at + 8] * 40.0F));
                     }

                     if (glow) {
                        q.bill(px, py, pz, size, r, g, b, a, 3, 2);
                     } else if (glint) {
                        q.bill(px, py, pz, size, gl[0], gl[1], gl[2], a, 1, 1);
                     } else {
                        q.bill(px, py, pz, size, r, g, b, a, 0, 1);
                     }
                  }
               }
            }

            if (f < 1.0F) {
               place(A, o, bx, by, bz, top, 0.5F, 0.5F, 0.02F, t);
               float fl = 1.0F - f;
               q.bill(A[0], A[1], A[2], 0.25F + 0.75F * ease3(f), r, g, b, 0.55F * fl * (float)Math.sqrt((double)fl), 3, 2);
            }

         }
      }
   }

   @SubscribeEvent
   public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.LATTICE.get(), LatticeRenderer::new);
   }
}
