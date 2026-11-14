package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class LimitBreakerAuraLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   private static final ResourceLocation AURA_TEX = new ResourceLocation("unrp", "textures/entity/lb_aura.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final float PI = (float)Math.PI;
   public static final float FEET = 1.52F;
   public static final float HEIGHT = 3.15F;
   public static final int STEPS = 6;
   public static final int SEGS = 16;
   public static final int RINGS = 8;
   public static final int MANTLE_SEGS = 20;
   public static final int MANTLE_RINGS = 9;
   public static final float MANTLE_R = 0.8F;
   public static final float MANTLE_A = 0.5F;
   public static final float HAZE_A = 0.2F;
   public static final int CINDERS = 32;
   public static final int BAND = 24;
   public static final float BEAT = 22.0F;
   public static final int SET_FIELDS = 13;
   public static final float[] PACE = new float[]{1.0F, 1.0F, 1.35F, 0.9F, 1.0F, 1.1F, 1.2F, 1.0F, 1.25F, 1.1F, 0.95F, 1.05F, 0.55F, 0.5F, 1.15F};
   public static final float[] REACH = new float[]{1.0F, 1.0F, 0.86F, 0.94F, 1.0F, 1.05F, 1.12F, 1.0F, 1.1F, 1.1F, 1.0F, 1.08F, 0.72F, 0.64F, 1.18F};
   public static final float[] SETS = new float[]{26.0F, 15.0F, 1.06F, 0.52F, 0.4F, 0.02F, 0.1F, 0.3F, 1.0F, 0.95F, 0.0F, 0.0F, 0.8F, 9.0F, 11.0F, 0.92F, 0.36F, -0.3F, 0.03F, 0.2F, 0.42F, 0.85F, 1.1F, 0.0F, 0.0F, 0.7F, 26.0F, 15.0F, 1.06F, 0.24F, 0.4F, 0.02F, 0.35F, 0.3F, 1.0F, 0.95F, 2.0F, 1.0F, 0.52F, 12.0F, 10.0F, 0.62F, 0.28F, -0.5F, -0.035F, 0.6F, 0.38F, 0.62F, 0.95F, 1.0F, 1.0F, 0.66F};
   private final float[] px = new float[7];
   private final float[] py = new float[7];
   private final float[] pz = new float[7];
   private final float[] tx = new float[7];
   private final float[] tz = new float[7];
   private final float[] rx = new float[7];
   private final float[] rz = new float[7];
   private final float[] ww = new float[7];
   private final float[] vv = new float[7];
   private final float[] cr = new float[7];
   private final float[] cg = new float[7];
   private final float[] cb = new float[7];
   private final float[] ca = new float[7];
   private final float[] ax = new float[21];
   private final float[] az = new float[21];
   private final float[] bx = new float[21];
   private final float[] bz = new float[21];
   private final float[] qx = new float[4];
   private final float[] qy = new float[4];
   private final float[] qz = new float[4];
   private final float[] qu = new float[4];
   private final float[] qv = new float[4];
   private final float[] qr = new float[4];
   private final float[] qg = new float[4];
   private final float[] qb = new float[4];
   private final float[] qa = new float[4];

   public LimitBreakerAuraLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
      int form = ClientLbFormCache.form(player.getUUID());
      if (LimitBreakerForms.valid(form) && !player.isInvisible()) {
         if (!LimitBreakerForms.apeForm(form)) {
            if (!ClientLbFormCache.auraOff(player.getUUID())) {
               boolean roseTint = ClientLbFormCache.rose(player.getUUID());
               int packed = LimitBreakerForms.color(form, roseTint);
               float[] rgb = new float[]{(float)(packed >> 16 & 255) / 255.0F, (float)(packed >> 8 & 255) / 255.0F, (float)(packed & 255) / 255.0F};
               this.flames(buffers, pose, clock(player, partialTick), rgb, edge(player), form, ClientLbFormCache.flare(player.getUUID()), player.onGround());
            }
         }
      }
   }

   public static float clock(AbstractClientPlayer player, float partialTick) {
      return (float)(player.tickCount % 24000) + partialTick + (float)(player.getId() & 63) * 7.0F;
   }

   public static float edge(AbstractClientPlayer player) {
      float max = player.getMaxHealth();
      return max > 0.0F ? Mth.clamp(1.0F - player.getHealth() / max, 0.0F, 1.0F) : 0.0F;
   }

   public static float blaze(float edge) {
      return 0.8F + 0.2F * edge;
   }

   public static float beatQ(float t) {
      return Mth.frac(t / 22.0F);
   }

   public static float offbeat(float edge) {
      return Mth.clamp(edge * 1.5F - 0.2F, 0.0F, 1.0F);
   }

   public static float strength(float t, float edge) {
      return (Mth.floor(t / 22.0F) & 1) == 0 ? 1.0F : offbeat(edge);
   }

   public static float pulse(float q) {
      if (q < 0.08F) {
         return q / 0.08F;
      } else {
         float d = (1.0F - q) / 0.92F;
         return d * d * d;
      }
   }

   private void flames(MultiBufferSource buffers, PoseStack pose, float time, float[] rgb, float edge, int form, float flare, boolean ground) {
      Matrix4f m = pose.last().pose();
      float t = time * PACE[form];
      float blaze = blaze(edge);
      float q = beatQ(t);
      float surge = strength(t, edge);
      float pulse = surge * pulse(q);
      float grow = (1.0F + 0.05F * (float)LimitBreakerForms.era(form)) * (1.0F + 0.75F * flare) * (1.0F + 0.16F * pulse);
      float heat = blaze * (1.0F + 1.5F * flare) * (1.0F + 0.55F * pulse) * (0.9F + 0.1F * Mth.sin(t * 3.3F) * Mth.sin(t * 1.9F));
      float reach = REACH[form];
      float height = 3.15F * grow * Math.max(1.0F, reach) * (0.9F + 0.1F * blaze);
      VertexConsumer body = buffers.getBuffer(RenderType.entityTranslucentEmissive(AURA_TEX));
      this.mantle(body, m, t, rgb, grow, height, heat);

      for(int set = 0; set < SETS.length; set += 13) {
         if (SETS[set + 11] == 0.0F) {
            this.tongues(body, m, t, rgb, grow, height, heat, reach, set);
         }
      }

      VertexConsumer glow = buffers.getBuffer(RenderType.eyes(AURA_TEX));
      this.haze(glow, m, t, rgb, grow, height, heat);

      for(int set = 0; set < SETS.length; set += 13) {
         if (SETS[set + 11] != 0.0F) {
            this.tongues(glow, m, t, rgb, grow, height, heat, reach, set);
         }
      }

      this.cinders(glow, m, t, rgb, grow, height, heat, pulse);
      if (ground) {
         this.ripple(glow, m, t, rgb, grow, heat, flare, q, surge);
      }

   }

   public static float prof(float f) {
      return (0.5F + 0.48F * Mth.sin(f * 2.6F)) * (1.0F - 0.94F * f * f);
   }

   private void tongues(VertexConsumer vc, Matrix4f m, float t, float[] rgb, float grow, float height, float heat, float reach, int set) {
      int count = (int)SETS[set];
      float per = SETS[set + 1];
      float rad = SETS[set + 2];
      float wid = SETS[set + 3];
      float twist = SETS[set + 4];
      float spin = SETS[set + 5];
      float whiten = SETS[set + 6];
      float rise = SETS[set + 7];
      float length = SETS[set + 8];
      float top = SETS[set + 9];
      int column = (int)SETS[set + 10];
      boolean glow = SETS[set + 11] != 0.0F;
      float hot = heat * SETS[set + 12];
      float u0 = (float)column * 0.25F + 0.004F;
      float u1 = (float)column * 0.25F + 0.246F;
      float lr = mix(rgb[0], 1.0F, whiten);
      float lg = mix(rgb[1], 1.0F, whiten);
      float lb = mix(rgb[2], 1.0F, whiten);

      for(int k = 0; k < count; ++k) {
         float a = Mth.frac((float)k * 0.618034F);
         float b = Mth.frac((float)k * 0.414214F);
         float c = Mth.frac((float)k * 0.732051F);
         float d = Mth.frac((float)k * 0.236068F);
         float p = Mth.frac(t / (per * (0.8F + 0.4F * b)) + a);
         float env = Mth.sin((float)Math.PI * p);
         env *= env;
         float hb = 0.02F + rise * p;
         float ln = length * reach * (0.62F + 0.38F * c) * (0.35F + 0.65F * Mth.sin((float)Math.PI * p));
         float phi0 = (float)k * (((float)Math.PI * 2F) / (float)count) + (d - 0.5F) * 0.5F + t * spin;

         for(int i = 0; i <= 6; ++i) {
            float s = (float)i / 6.0F;
            float f = Math.min(top, hb + ln * s);
            float phi = phi0 + twist * s + 0.3F * s * Mth.sin(t * 0.9F + (float)k * 1.9F + s * 5.0F);
            float lick = 1.0F + s * s * (0.22F + 0.3F * Mth.sin(t * 1.1F + (float)k * 2.3F - s * 3.0F));
            float r = prof(Math.min(f, 0.97F)) * grow * rad * (0.94F + 0.12F * c) * lick;
            float cos = Mth.cos(phi);
            float sin = Mth.sin(phi);
            float fl = Math.min(f, 1.0F);
            this.px[i] = cos * r;
            this.py[i] = 1.52F - height * f;
            this.pz[i] = sin * r;
            this.tx[i] = -sin;
            this.tz[i] = cos;
            this.rx[i] = cos;
            this.rz[i] = sin;
            this.ww[i] = wid * grow * (float)Math.pow((double)(1.0F - s), (double)0.8F) * (0.55F + 0.45F * env);
            float inten = hot * env * (0.45F + 0.55F * (1.0F - s)) * Math.min(1.0F, 0.2F + s * 3.2F) * (1.0F - 0.2F * fl);
            if (glow) {
               this.cr[i] = lr * inten;
               this.cg[i] = lg * inten;
               this.cb[i] = lb * inten;
               this.ca[i] = inten;
            } else {
               this.cr[i] = lr;
               this.cg[i] = lg;
               this.cb[i] = lb;
               this.ca[i] = Math.min(1.0F, inten * 1.5F * (1.0F - 0.25F * fl));
            }

            this.vv[i] = s * 0.6F - t * 0.11F + a;
         }

         this.strip(vc, m, this.tx, this.tz, 1.0F, 1.0F, u0, u1, glow);
         this.strip(vc, m, this.rx, this.rz, 0.3F, 0.8F, u0, u1, glow);
      }

   }

   private void strip(VertexConsumer vc, Matrix4f m, float[] lx, float[] lz, float lo, float hi, float u0, float u1, boolean both) {
      for(int i = 0; i < 6; ++i) {
         int j = i + 1;
         this.corner(0, this.px[i] - lx[i] * this.ww[i] * lo, this.py[i], this.pz[i] - lz[i] * this.ww[i] * lo, u0, this.vv[i], i);
         this.corner(1, this.px[i] + lx[i] * this.ww[i] * hi, this.py[i], this.pz[i] + lz[i] * this.ww[i] * hi, u1, this.vv[i], i);
         this.corner(2, this.px[j] + lx[j] * this.ww[j] * hi, this.py[j], this.pz[j] + lz[j] * this.ww[j] * hi, u1, this.vv[j], j);
         this.corner(3, this.px[j] - lx[j] * this.ww[j] * lo, this.py[j], this.pz[j] - lz[j] * this.ww[j] * lo, u0, this.vv[j], j);
         this.emit(vc, m, both);
      }

   }

   private void corner(int n, float x, float y, float z, float u, float v, int i) {
      this.put(n, x, y, z, u, v, this.cr[i], this.cg[i], this.cb[i], this.ca[i]);
   }

   private void put(int n, float x, float y, float z, float u, float v, float r, float g, float b, float a) {
      this.qx[n] = x;
      this.qy[n] = y;
      this.qz[n] = z;
      this.qu[n] = u;
      this.qv[n] = v;
      this.qr[n] = r;
      this.qg[n] = g;
      this.qb[n] = b;
      this.qa[n] = a;
   }

   private void emit(VertexConsumer vc, Matrix4f m, boolean both) {
      for(int n = 0; n < 4; ++n) {
         this.vert(vc, m, n);
      }

      if (both) {
         for(int n = 3; n >= 0; --n) {
            this.vert(vc, m, n);
         }
      }

   }

   private void mantle(VertexConsumer vc, Matrix4f m, float t, float[] rgb, float grow, float height, float heat) {
      float lr = mix(rgb[0], 1.0F, 0.18F);
      float lg = mix(rgb[1], 1.0F, 0.18F);
      float lb = mix(rgb[2], 1.0F, 0.18F);
      float y0 = 1.52F;
      float a0 = 0.0F;
      float f0 = 0.0F;

      for(int j = 0; j <= 9; ++j) {
         float f = (float)j / 9.0F * 0.92F;
         float y = 1.52F - height * f;

         for(int i = 0; i <= 20; ++i) {
            float phi = (float)i * ((float)Math.PI / 10F) + t * 0.05F;
            float wob = 1.0F + 0.1F * Mth.sin(t * 0.9F + phi * 3.0F + f * 9.0F) + 0.06F * Mth.sin(t * 1.7F - phi * 5.0F + f * 13.0F);
            float r = prof(f) * grow * 0.8F * wob;
            this.bx[i] = Mth.cos(phi) * r;
            this.bz[i] = Mth.sin(phi) * r;
         }

         float al = heat * 0.5F * Math.min(1.0F, 0.3F + f * 5.0F) * (1.0F - 0.75F * f);
         if (j > 0) {
            float v0 = f0 * 1.5F - t * 0.13F;
            float v1 = f * 1.5F - t * 0.13F;

            for(int i = 0; i < 20; ++i) {
               this.put(0, this.ax[i], y0, this.az[i], 0.004F, v0, lr, lg, lb, a0);
               this.put(1, this.ax[i + 1], y0, this.az[i + 1], 0.246F, v0, lr, lg, lb, a0);
               this.put(2, this.bx[i + 1], y, this.bz[i + 1], 0.246F, v1, lr, lg, lb, al);
               this.put(3, this.bx[i], y, this.bz[i], 0.004F, v1, lr, lg, lb, al);
               this.emit(vc, m, false);
            }
         }

         System.arraycopy(this.bx, 0, this.ax, 0, 21);
         System.arraycopy(this.bz, 0, this.az, 0, 21);
         y0 = y;
         a0 = al;
         f0 = f;
      }

   }

   private void haze(VertexConsumer vc, Matrix4f m, float t, float[] rgb, float grow, float height, float heat) {
      float breath = 0.85F + 0.15F * Mth.sin(t * 0.31F);
      float y0 = 1.52F;
      float i0 = 0.0F;
      float f0 = 0.0F;

      for(int j = 0; j <= 8; ++j) {
         float f = (float)j / 8.0F;
         float r = prof(f) * grow * 1.08F;
         float y = 1.52F - height * f;

         for(int i = 0; i <= 16; ++i) {
            float phi = (float)i * ((float)Math.PI / 8F) + t * 0.03F;
            this.bx[i] = Mth.cos(phi) * r;
            this.bz[i] = Mth.sin(phi) * r;
         }

         float inten = heat * 0.2F * breath * (1.0F - 0.75F * f) * Math.min(1.0F, 0.4F + f * 4.0F);
         if (j > 0) {
            float v0 = f0 * 1.2F - t * 0.06F;
            float v1 = f * 1.2F - t * 0.06F;

            for(int i = 0; i < 16; ++i) {
               this.put(0, this.ax[i], y0, this.az[i], 0.504F, v0, rgb[0] * i0, rgb[1] * i0, rgb[2] * i0, i0);
               this.put(1, this.ax[i + 1], y0, this.az[i + 1], 0.746F, v0, rgb[0] * i0, rgb[1] * i0, rgb[2] * i0, i0);
               this.put(2, this.bx[i + 1], y, this.bz[i + 1], 0.746F, v1, rgb[0] * inten, rgb[1] * inten, rgb[2] * inten, inten);
               this.put(3, this.bx[i], y, this.bz[i], 0.504F, v1, rgb[0] * inten, rgb[1] * inten, rgb[2] * inten, inten);
               this.emit(vc, m, true);
            }
         }

         System.arraycopy(this.bx, 0, this.ax, 0, 17);
         System.arraycopy(this.bz, 0, this.az, 0, 17);
         y0 = y;
         i0 = inten;
         f0 = f;
      }

   }

   private void cinders(VertexConsumer vc, Matrix4f m, float t, float[] rgb, float grow, float height, float heat, float pulse) {
      float lr = mix(rgb[0], 1.0F, 0.45F);
      float lg = mix(rgb[1], 1.0F, 0.45F);
      float lb = mix(rgb[2], 1.0F, 0.45F);

      for(int k = 0; k < 32; ++k) {
         float a = Mth.frac((float)k * 0.618034F);
         float b = Mth.frac((float)k * 0.414214F);
         float c = Mth.frac((float)k * 0.732051F);
         float p = Mth.frac(t / (20.0F * (0.8F + 0.5F * b)) + a);
         float f = -0.02F + 1.2F * p;
         float phi = (float)k * 2.39996F + p * 2.2F + t * 0.02F;
         float r = (prof(Mth.clamp(f, 0.0F, 0.97F)) * grow * (0.75F + 0.6F * c) + 0.08F) * (1.0F + 0.35F * pulse);
         float x = Mth.cos(phi) * r;
         float z = Mth.sin(phi) * r;
         float y = 1.52F - height * f;
         float sz = 0.06F * grow * (1.0F - 0.5F * p);
         float tall = sz * 2.0F;
         float inten = heat * Mth.sin((float)Math.PI * p);
         float r0 = lr * inten;
         float g0 = lg * inten;
         float b0 = lb * inten;
         this.put(0, x - sz, y - tall, z, 0.754F, 0.0F, r0, g0, b0, inten);
         this.put(1, x + sz, y - tall, z, 0.996F, 0.0F, r0, g0, b0, inten);
         this.put(2, x + sz, y + tall, z, 0.996F, 0.124F, r0, g0, b0, inten);
         this.put(3, x - sz, y + tall, z, 0.754F, 0.124F, r0, g0, b0, inten);
         this.emit(vc, m, true);
         this.put(0, x, y - tall, z - sz, 0.754F, 0.0F, r0, g0, b0, inten);
         this.put(1, x, y - tall, z + sz, 0.996F, 0.0F, r0, g0, b0, inten);
         this.put(2, x, y + tall, z + sz, 0.996F, 0.124F, r0, g0, b0, inten);
         this.put(3, x, y + tall, z - sz, 0.754F, 0.124F, r0, g0, b0, inten);
         this.emit(vc, m, true);
         this.put(0, x - sz, y, z - sz, 0.754F, 0.0F, r0, g0, b0, inten);
         this.put(1, x + sz, y, z - sz, 0.996F, 0.0F, r0, g0, b0, inten);
         this.put(2, x + sz, y, z + sz, 0.996F, 0.124F, r0, g0, b0, inten);
         this.put(3, x - sz, y, z + sz, 0.754F, 0.124F, r0, g0, b0, inten);
         this.emit(vc, m, true);
      }

   }

   private void ripple(VertexConsumer vc, Matrix4f m, float t, float[] rgb, float grow, float heat, float flare, float q, float surge) {
      float y = 1.485F;

      for(int j = 0; j < 2; ++j) {
         float qq = Mth.frac(t / 14.0F + (float)j * 0.5F);
         float r0 = (0.45F + 1.4F * qq) * grow;
         float r1 = r0 + (0.1F + 0.16F * qq) * grow;
         float inten_0 = heat * 0.55F * (float)Math.pow((double)(1.0F - qq), 1.5D);
         this.band(vc, m, y, r0, r1, rgb[0] * inten_0, rgb[1] * inten_0, rgb[2] * inten_0, inten_0);
      }

      float r = 1.3F * grow;
      float inten = heat * 0.36F;
      float yp = y + 0.005F;
      this.put(0, -r, yp, -r, 0.754F, 0.0F, rgb[0] * inten, rgb[1] * inten, rgb[2] * inten, inten);
      this.put(1, r, yp, -r, 0.996F, 0.0F, rgb[0] * inten, rgb[1] * inten, rgb[2] * inten, inten);
      this.put(2, r, yp, r, 0.996F, 0.124F, rgb[0] * inten, rgb[1] * inten, rgb[2] * inten, inten);
      this.put(3, -r, yp, r, 0.754F, 0.124F, rgb[0] * inten, rgb[1] * inten, rgb[2] * inten, inten);
      this.emit(vc, m, true);
      if (q < 0.45F && surge > 0.01F) {
         float k0_0 = (0.6F + 7.0F * q) * grow;
         float ki_0 = heat * 1.1F * surge * (1.0F - q / 0.45F);
         this.band(vc, m, y, k0_0, k0_0 + (0.25F + 0.4F * q) * grow, mix(rgb[0], 1.0F, 0.35F) * ki_0, mix(rgb[1], 1.0F, 0.35F) * ki_0, mix(rgb[2], 1.0F, 0.35F) * ki_0, ki_0);
      }

      if (flare > 0.01F) {
         float k0 = (0.4F + 3.4F * (1.0F - flare)) * grow;
         float ki = heat * 0.9F * flare;
         this.band(vc, m, y, k0, k0 + 0.35F * grow, mix(rgb[0], 1.0F, 0.5F) * ki, mix(rgb[1], 1.0F, 0.5F) * ki, mix(rgb[2], 1.0F, 0.5F) * ki, ki);
      }

   }

   private void band(VertexConsumer vc, Matrix4f m, float y, float r0, float r1, float r, float g, float b, float a) {
      for(int i = 0; i < 24; ++i) {
         float a0 = (float)i * 0.2617994F;
         float a1 = (float)(i + 1) * 0.2617994F;
         float c0 = Mth.cos(a0);
         float s0 = Mth.sin(a0);
         float c1 = Mth.cos(a1);
         float s1 = Mth.sin(a1);
         this.put(0, c0 * r0, y, s0 * r0, 0.754F, 0.128F, r, g, b, a);
         this.put(1, c0 * r1, y, s0 * r1, 0.996F, 0.128F, r, g, b, a);
         this.put(2, c1 * r1, y, s1 * r1, 0.996F, 0.246F, r, g, b, a);
         this.put(3, c1 * r0, y, s1 * r0, 0.754F, 0.246F, r, g, b, a);
         this.emit(vc, m, true);
      }

   }

   private static float mix(float a, float b, float f) {
      return a + (b - a) * f;
   }

   private void vert(VertexConsumer vc, Matrix4f m, int n) {
      vc.vertex(m, this.qx[n], this.qy[n], this.qz[n]).color(Mth.clamp(this.qr[n], 0.0F, 1.0F), Mth.clamp(this.qg[n], 0.0F, 1.0F), Mth.clamp(this.qb[n], 0.0F, 1.0F), Mth.clamp(this.qa[n], 0.0F, 1.0F)).uv(this.qu[n], this.qv[n]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(0.0F, 1.0F, 0.0F).endVertex();
   }
}
