package com.myriam.unrp.uo;

import com.myriam.unrp.look.ClientEyeCache;
import com.myriam.unrp.look.ClientSettingsCache;
import com.myriam.unrp.pass.UnrpPowerLayers;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class LbEdgeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   public static final float SHOW_FROM = 0.95F;
   public static final float EYES_AT = 0.1F;
   public static final float[] AT_HEALTH = new float[]{1.0F, 0.6F, 0.3F, 0.1F};
   public static final float[] AT_Y = new float[]{23.6F, 18.0F, 4.0F, -7.2F};
   public static final int SEGS = 32;
   public static final int BARE = 16738876;
   public static final double NOTCH_RADIUS = 1.12D;
   private static final double PX = 0.0625D;
   private static final float[] PURE = new float[]{1.0F, 1.0F, 1.0F};
   public static final float CROUCH_DROP = 0.125F;
   public static final float MODEL_SCALE = 0.9375F;

   public LbEdgeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   public static boolean ours(AbstractClientPlayer player) {
      return "limit_breaker".equals(ClientEyeCache.get(player.getUUID()));
   }

   public static float healthOf(AbstractClientPlayer player) {
      float max = player.getMaxHealth();
      return max > 0.0F ? Mth.clamp(player.getHealth() / max, 0.0F, 1.0F) : 1.0F;
   }

   public static int colorOf(AbstractClientPlayer player) {
      int form = ClientLbFormCache.form(player.getUUID());
      return LimitBreakerForms.valid(form) ? LimitBreakerForms.color(form, ClientLbFormCache.rose(player.getUUID())) : 16738876;
   }

   public static float heightPx(float h) {
      if (h >= AT_HEALTH[0]) {
         return AT_Y[0];
      } else {
         for(int i = 1; i < AT_HEALTH.length; ++i) {
            if (h >= AT_HEALTH[i]) {
               float t = (AT_HEALTH[i - 1] - h) / (AT_HEALTH[i - 1] - AT_HEALTH[i]);
               return Mth.lerp(t, AT_Y[i - 1], AT_Y[i]);
            }
         }

         return AT_Y[AT_Y.length - 1];
      }
   }

   public static float radiusPx(float y) {
      float legs = 5.5F;
      float body = 9.0F;
      float head = 5.6F;
      if (y >= 13.5F) {
         return legs;
      } else if (y >= 10.5F) {
         return Mth.lerp((13.5F - y) / 3.0F, legs, body);
      } else if (y >= 1.5F) {
         return body;
      } else {
         return y >= -1.5F ? Mth.lerp((1.5F - y) / 3.0F, body, head) : head;
      }
   }

   public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
      if (!player.isInvisible() && ours(player)) {
         float h = healthOf(player);
         float show = Mth.clamp((0.95F - h) / 0.05F, 0.0F, 1.0F);
         float[] col = ClientLbLooks.rgb(colorOf(player));
         float t = (float)player.tickCount + partialTick;
         if (show > 0.01F) {
            line(pose, buffers, col, heightPx(h), t, show, h);
         }

         if (h <= 0.1F && !ClientSettingsCache.isAM(player.getUUID())) {
            this.eyes(pose, buffers, player, col, t);
         }

         if (ClientLbLooks.inWorld()) {
            notches(pose, buffers, player, col, t);
         }

      }
   }

   private static void line(PoseStack pose, MultiBufferSource buffers, float[] col, float yPx, float t, float show, float h) {
      Matrix4f m = pose.last().pose();
      double y = (double)yPx * 0.0625D;
      double r = (double)radiusPx(yPx) * 0.0625D;
      double spin = (double)t * (0.08D + 0.12D * (1.0D - (double)h));
      VertexConsumer band = buffers.getBuffer(RenderType.entityTranslucentEmissive(UnrpPowerLayers.WHITE, false));

      for(int i = 0; i < 32; ++i) {
         double a0_0 = (Math.PI * 2D) * (double)i / 32.0D;
         double a1_0 = (Math.PI * 2D) * (double)(i + 1) / 32.0D;
         float a = 0.75F * show * glow(a0_0, a1_0, spin);
         strip(band, m, col, r, y, 0.034375D, a0_0, a1_0, a);
      }

      VertexConsumer core = buffers.getBuffer(RenderType.eyes(UnrpPowerLayers.WHITE));

      for(int i = 0; i < 32; ++i) {
         double a0 = (Math.PI * 2D) * (double)i / 32.0D;
         double a1 = (Math.PI * 2D) * (double)(i + 1) / 32.0D;
         float k = 0.95F * show * glow(a0, a1, spin);
         float[] hot = new float[]{k, k, k};
         strip(core, m, hot, r * 0.995D, y, 0.0125D, a0, a1, 1.0F);
         strip(core, m, hot, r * 0.995D, y, 0.0125D, a1, a0, 1.0F);
      }

   }

   private static float glow(double a0, double a1, double spin) {
      double c = Math.max(0.0D, Math.cos((a0 + a1) * 0.5D - spin));
      return (float)(0.55D + 0.45D * Math.pow(c, 6.0D));
   }

   private static void strip(VertexConsumer vc, Matrix4f m, float[] col, double r, double y, double half, double a0, double a1, float a) {
      double x0 = Math.cos(a0) * r;
      double z0 = Math.sin(a0) * r;
      double x1 = Math.cos(a1) * r;
      double z1 = Math.sin(a1) * r;
      ClientVertigo.quad(vc, m, col, x0, y - half, z0, x1, y - half, z1, x1, y + half, z1, x0, y + half, z0, a, a, a, a);
   }

   private void eyes(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, float[] col, float t) {
      CompoundTag tag = ClientSettingsCache.getTag(player.getUUID());
      int w = Mth.clamp(intOr(tag, "EyeW", 2), 1, 4);
      int hh = Mth.clamp(intOr(tag, "EyeH", 1), 1, 3);
      boolean leftOn = intOr(tag, "EyeL", 1) == 1;
      boolean rightOn = intOr(tag, "EyeR", 1) == 1;
      int rx = Mth.clamp(intOr(tag, "EyeRX", 0), -4, 4);
      int ry = Mth.clamp(intOr(tag, "EyeRY", 0), -4, 4);
      int lx = Mth.clamp(intOr(tag, "EyeLX", 0), -4, 4);
      int ly = Mth.clamp(intOr(tag, "EyeLY", 0), -4, 4);
      float pulse = 0.75F + 0.25F * Mth.sin(t * 0.35F);
      float[] glow = new float[]{col[0] * 0.55F * pulse, col[1] * 0.55F * pulse, col[2] * 0.55F * pulse};
      pose.pushPose();

      try {
         ((PlayerModel)this.getParentModel()).head.translateAndRotate(pose);
         Matrix4f m = pose.last().pose();
         VertexConsumer vc = buffers.getBuffer(RenderType.eyes(UnrpPowerLayers.WHITE));
         double z = -0.303125D;
         if (rightOn) {
            halo(vc, m, glow, (-1.0D - (double)w + (double)rx) * 0.0625D, (-1.0D + (double)rx) * 0.0625D, (-4.0D + (double)ry) * 0.0625D, (-4.0D + (double)ry + (double)hh) * 0.0625D, z);
         }

         if (leftOn) {
            halo(vc, m, glow, (1.0D + (double)lx) * 0.0625D, (1.0D + (double)lx + (double)w) * 0.0625D, (-4.0D + (double)ly) * 0.0625D, (-4.0D + (double)ly + (double)hh) * 0.0625D, z);
         }
      } finally {
         pose.popPose();
      }

   }

   private static void halo(VertexConsumer vc, Matrix4f m, float[] glow, double x0, double x1, double y0, double y1, double z) {
      double e = 0.075D;
      both(vc, m, glow, x0 - e, y0 - e, x1 + e, y1 + e, z);
      float[] hot = new float[]{glow[0] * 0.8F, glow[1] * 0.8F, glow[2] * 0.8F};
      both(vc, m, hot, x0, y0, x1, y1, z - 0.00125D);
   }

   private static void both(VertexConsumer vc, Matrix4f m, float[] rgb, double x0, double y0, double x1, double y1, double z) {
      ClientVertigo.quad(vc, m, rgb, x0, y0, z, x1, y0, z, x1, y1, z, x0, y1, z, 1.0F, 1.0F, 1.0F, 1.0F);
      ClientVertigo.quad(vc, m, rgb, x0, y1, z, x1, y1, z, x1, y0, z, x0, y0, z, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   static float crouchLift(AbstractClientPlayer player) {
      return player.isCrouching() ? 0.125F / (0.9375F * Math.max(0.25F, player.getBbWidth() / 0.6F)) : 0.0F;
   }

   private static void notches(PoseStack pose, MultiBufferSource buffers, AbstractClientPlayer player, float[] col, float t) {
      int count = Math.min(60, ClientLbLooks.ascension(player.getId()));
      if (count > 0 && player.onGround()) {
         int form = ClientLbFormCache.form(player.getUUID());
         if (LimitBreakerForms.valid(form) && !LimitBreakerForms.apeForm(form) && !ClientLbFormCache.auraOff(player.getUUID())) {
            Matrix4f m = pose.last().pose();
            double y = 1.4779999809265136D - (double)crouchLift(player);
            double r = 1.12D * (1.0D + 0.05D * (double)LimitBreakerForms.era(form));
            VertexConsumer ring = buffers.getBuffer(RenderType.entityTranslucentEmissive(UnrpPowerLayers.WHITE, false));

            for(int i = 0; i < 48; ++i) {
               double a0 = (Math.PI * 2D) * (double)i / 48.0D;
               double a1 = (Math.PI * 2D) * (double)(i + 1) / 48.0D;
               flat(ring, m, col, r - 0.025D, r + 0.025D, a0, a1, y, 0.35F);
            }

            double turn = (double)t * 0.004D;
            double half = 0.0175D / r;

            for(int i = 0; i < count; ++i) {
               double th_0 = turn + (Math.PI * 2D) * (double)i / (double)count;
               flat(ring, m, col, r - 0.1D, r - 0.025D, th_0 - half * 1.8D, th_0 + half * 1.8D, y, 0.6F);
               flat(ring, m, col, r + 0.025D, r + 0.1D, th_0 - half * 1.8D, th_0 + half * 1.8D, y, 0.6F);
            }

            VertexConsumer cut = buffers.getBuffer(RenderType.eyes(UnrpPowerLayers.WHITE));
            float[] hot = new float[]{0.95F, 0.95F, 0.95F};

            for(int i = 0; i < count; ++i) {
               double th = turn + (Math.PI * 2D) * (double)i / (double)count;
               flat(cut, m, hot, r - 0.085D, r + 0.085D, th - half, th + half, y + 0.002D, 1.0F);
               flat(cut, m, hot, r - 0.085D, r + 0.085D, th + half, th - half, y + 0.002D, 1.0F);
            }

         }
      }
   }

   private static void flat(VertexConsumer vc, Matrix4f m, float[] col, double r0, double r1, double a0, double a1, double y, float a) {
      ClientVertigo.quad(vc, m, col, Math.cos(a0) * r0, y, Math.sin(a0) * r0, Math.cos(a1) * r0, y, Math.sin(a1) * r0, Math.cos(a1) * r1, y, Math.sin(a1) * r1, Math.cos(a0) * r1, y, Math.sin(a0) * r1, a, a, a, a);
   }

   private static int intOr(CompoundTag t, String key, int def) {
      return t != null && t.contains(key, 99) ? t.getInt(key) : def;
   }
}
