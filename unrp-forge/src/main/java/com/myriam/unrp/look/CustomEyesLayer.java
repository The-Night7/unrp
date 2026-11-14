package com.myriam.unrp.look;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class CustomEyesLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   private static final ResourceLocation[] STYLES = new ResourceLocation[]{new ResourceLocation("unrp", "textures/gui/eye_sharp.png"), new ResourceLocation("unrp", "textures/gui/eye_round.png"), new ResourceLocation("unrp", "textures/gui/eye_ring.png")};
   public static final Map<String, Integer> DEFAULTS = new HashMap();
   private static final int FULL_BRIGHT = 15728880;
   public static final double AM_SHAPE_WHITE = 0.55D;

   public CustomEyesLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
      if (ClientSettingsCache.isAM(player.getUUID())) {
         if (!player.isInvisible()) {
            this.amEyes(poseStack, buffer, player, partialTick);
         }

      } else {
         String power = ClientEyeCache.get(player.getUUID());
         int over = -1;
         if (!power.isEmpty() || over >= 0) {
            if (!power.equals("sarcophage")) {
               if (!player.isInvisible()) {
                  CompoundTag t = ClientSettingsCache.getTag(player.getUUID());
                  int color = ClientSettingsCache.getEyeColor(player.getUUID());
                  if (color == -1) {
                     color = ClientSettingsCache.getColor(player.getUUID());
                  }

                  if (color == -1) {
                     color = fallback(power);
                  }

                  if (over >= 0) {
                     color = over;
                  }

                  int style = clamp(intOr(t, "EyeStyle", 0), 0, 2);
                  int w = clamp(intOr(t, "EyeW", 2), 1, 4);
                  int h = clamp(intOr(t, "EyeH", 1), 1, 3);
                  boolean leftOn = intOr(t, "EyeL", 1) == 1;
                  boolean rightOn = intOr(t, "EyeR", 1) == 1;
                  int rxOff = clamp(intOr(t, "EyeRX", 0), -4, 4);
                  int ryOff = clamp(intOr(t, "EyeRY", 0), -4, 4);
                  int lxOff = clamp(intOr(t, "EyeLX", 0), -4, 4);
                  int lyOff = clamp(intOr(t, "EyeLY", 0), -4, 4);
                  boolean trail = intOr(t, "EyeTrail", 1) == 1;
                  double now = (double)((float)player.level().getGameTime() + partialTick);
                  double[] lk = PowerEyes.look(player, now);
                  float lit = (float)lk[0];
                  if (!(lit <= 0.004F)) {
                     float r = (float)(color >> 16 & 255) / 255.0F * lit;
                     float g = (float)(color >> 8 & 255) / 255.0F * lit;
                     float b = (float)(color & 255) / 255.0F * lit;
                     poseStack.pushPose();
                     ((PlayerModel)this.getParentModel()).head.translateAndRotate(poseStack);
                     VertexConsumer vc = buffer.getBuffer(RenderType.eyes(STYLES[style]));
                     float z = -0.25625F;
                     float hot = com.myriam.unrp.pass.ClientPass.eyeHeat(player, partialTick);
                     hot = (float)com.myriam.unrp.pass.EyeGlowMath.hot((double)hot, lk[1]) * lit;
                     if (rightOn) {
                        float x1 = (-1.0F - (float)w + (float)rxOff) / 16.0F;
                        float x2 = (-1.0F + (float)rxOff) / 16.0F;
                        float y1 = (-4.0F + (float)ryOff) / 16.0F;
                        float y2 = y1 + (float)h / 16.0F;
                        quad(poseStack, vc, x1, y1, x2, y2, z, r, g, b);
                        if (hot > 0.004F) {
                           quad(poseStack, vc, x1, y1, x2, y2, z, hot, hot, hot);
                        }
                     }

                     if (leftOn) {
                        float x1 = (1.0F + (float)lxOff) / 16.0F;
                        float x2 = x1 + (float)w / 16.0F;
                        float y1 = (-4.0F + (float)lyOff) / 16.0F;
                        float y2 = y1 + (float)h / 16.0F;
                        quad(poseStack, vc, x1, y1, x2, y2, z, r, g, b);
                        if (hot > 0.004F) {
                           quad(poseStack, vc, x1, y1, x2, y2, z, hot, hot, hot);
                        }
                     }

                     PowerEyes.draw(buffer.getBuffer(com.myriam.unrp.pass.PassDraw.type(1)), poseStack.last().pose(), player, sides(t), style, w, h, trail, color, lk, now, partialTick);
                     poseStack.popPose();
                  }
               }
            }
         }
      }
   }

   private void amEyes(PoseStack poseStack, MultiBufferSource buffer, AbstractClientPlayer player, float partialTick) {
      double now = (double)((float)player.level().getGameTime() + partialTick);
      double[] lk = com.myriam.unrp.pass.ClientPassAm.look(player, now);
      if (lk != null && !(lk[0] <= 0.004D)) {
         CompoundTag t = ClientSettingsCache.getTag(player.getUUID());
         int style = clamp(intOr(t, "EyeStyle", 0), 0, 2);
         int w = clamp(intOr(t, "EyeW", 2), 1, 4);
         int h = clamp(intOr(t, "EyeH", 1), 1, 3);
         boolean trail = intOr(t, "EyeTrail", 1) == 1;
         int color = (int)lk[4];
         double[] hot = com.myriam.unrp.pass.EyeGlowMath.whiter(com.myriam.unrp.pass.EyeGlowMath.rgb(color), 0.55D);
         float lit = (float)lk[0];
         double[][] at = sides(t);
         poseStack.pushPose();
         ((PlayerModel)this.getParentModel()).head.translateAndRotate(poseStack);
         VertexConsumer vc = buffer.getBuffer(RenderType.eyes(STYLES[style]));
         float z = -0.25625F;

         for(double[] e : at) {
            if (e != null) {
               float x1 = (float)((e[0] - (double)w * 0.5D) / 16.0D);
               float y1 = (float)((e[1] - (double)h * 0.5D) / 16.0D);
               quad(poseStack, vc, x1, y1, x1 + (float)w / 16.0F, y1 + (float)h / 16.0F, z, (float)hot[0] * lit, (float)hot[1] * lit, (float)hot[2] * lit);
            }
         }

         PowerEyes.draw(buffer.getBuffer(com.myriam.unrp.pass.PassDraw.type(1)), poseStack.last().pose(), player, at, style, w, h, trail, color, lk, now, partialTick);
         poseStack.popPose();
      }
   }

   static int fallback(String power) {
      Integer c = (Integer)DEFAULTS.get(power);
      if (c != null) {
         return c;
      } else {
         return PowerLook.known(power) ? PowerLook.color(power) : 16777215;
      }
   }

   public static double[][] sides(CompoundTag t) {
      int w = clamp(intOr(t, "EyeW", 2), 1, 4);
      int h = clamp(intOr(t, "EyeH", 1), 1, 3);
      double[][] out = new double[2][];
      if (intOr(t, "EyeR", 1) == 1) {
         out[0] = new double[]{-1.0D - (double)w * 0.5D + (double)clamp(intOr(t, "EyeRX", 0), -4, 4), -4.0D + (double)clamp(intOr(t, "EyeRY", 0), -4, 4) + (double)h * 0.5D};
      }

      if (intOr(t, "EyeL", 1) == 1) {
         out[1] = new double[]{1.0D + (double)w * 0.5D + (double)clamp(intOr(t, "EyeLX", 0), -4, 4), -4.0D + (double)clamp(intOr(t, "EyeLY", 0), -4, 4) + (double)h * 0.5D};
      }

      return out;
   }

   public static int colorOf(AbstractClientPlayer player) {
      String power = ClientEyeCache.get(player.getUUID());
      int over = -1;
      if ((!power.isEmpty() || over >= 0) && !power.equals("sarcophage") && !player.isInvisible() && !ClientSettingsCache.isAM(player.getUUID())) {
         if (over >= 0) {
            return over;
         } else {
            int color = ClientSettingsCache.getEyeColor(player.getUUID());
            if (color == -1) {
               color = ClientSettingsCache.getColor(player.getUUID());
            }

            if (color == -1) {
               color = fallback(power);
            }

            return color & 16777215;
         }
      } else {
         return -1;
      }
   }

   public static double[][] centers(AbstractClientPlayer player) {
      CompoundTag t = ClientSettingsCache.getTag(player.getUUID());
      int w = clamp(intOr(t, "EyeW", 2), 1, 4);
      int h = clamp(intOr(t, "EyeH", 1), 1, 3);
      boolean leftOn = intOr(t, "EyeL", 1) == 1;
      boolean rightOn = intOr(t, "EyeR", 1) == 1;
      int rxOff = clamp(intOr(t, "EyeRX", 0), -4, 4);
      int ryOff = clamp(intOr(t, "EyeRY", 0), -4, 4);
      int lxOff = clamp(intOr(t, "EyeLX", 0), -4, 4);
      int lyOff = clamp(intOr(t, "EyeLY", 0), -4, 4);
      int n = (rightOn ? 1 : 0) + (leftOn ? 1 : 0);
      double[][] out = new double[n][];
      int i = 0;
      if (rightOn) {
         out[i++] = new double[]{-1.0D - (double)w * 0.5D + (double)rxOff, -4.0D + (double)ryOff + (double)h * 0.5D};
      }

      if (leftOn) {
         out[i] = new double[]{1.0D + (double)w * 0.5D + (double)lxOff, -4.0D + (double)lyOff + (double)h * 0.5D};
      }

      return out;
   }

   private static void quad(PoseStack ps, VertexConsumer vc, float x1, float y1, float x2, float y2, float z, float r, float g, float b) {
      PoseStack.Pose pose = ps.last();
      vertex(vc, pose, x1, y1, z, 0.0F, 0.0F, r, g, b);
      vertex(vc, pose, x1, y2, z, 0.0F, 1.0F, r, g, b);
      vertex(vc, pose, x2, y2, z, 1.0F, 1.0F, r, g, b);
      vertex(vc, pose, x2, y1, z, 1.0F, 0.0F, r, g, b);
      vertex(vc, pose, x2, y1, z, 1.0F, 0.0F, r, g, b);
      vertex(vc, pose, x2, y2, z, 1.0F, 1.0F, r, g, b);
      vertex(vc, pose, x1, y2, z, 0.0F, 1.0F, r, g, b);
      vertex(vc, pose, x1, y1, z, 0.0F, 0.0F, r, g, b);
   }

   private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, float u, float v, float r, float g, float b) {
      vc.vertex(pose.pose(), x, y, z).color(r, g, b, 1.0F).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pose.normal(), 0.0F, 0.0F, -1.0F).endVertex();
   }

   private static int intOr(CompoundTag t, String key, int def) {
      return t != null && t.contains(key, 99) ? t.getInt(key) : def;
   }

   private static int clamp(int v, int lo, int hi) {
      return Math.max(lo, Math.min(hi, v));
   }

   static {
      DEFAULTS.put("ability_gauging", 16767626);
      DEFAULTS.put("barrier", 16766976);
      DEFAULTS.put("conjure_disks", 14885888);
      DEFAULTS.put("contact_shred", 15445046);
      DEFAULTS.put("demon_claw", 13111342);
      DEFAULTS.put("dizzy_punch", 14378559);
      DEFAULTS.put("energy_discharge", 16711680);
      DEFAULTS.put("explosion", 8863024);
      DEFAULTS.put("flame_claw", 14562304);
      DEFAULTS.put("healing", 5364563);
      DEFAULTS.put("heat_palm", 2281471);
      DEFAULTS.put("hydrofreeze", 65415);
      DEFAULTS.put("hypnosis", 11554353);
      DEFAULTS.put("illumination", 16425016);
      DEFAULTS.put("lightning", 4174079);
      DEFAULTS.put("needles", 13498240);
      DEFAULTS.put("nightmare", 14927616);
      DEFAULTS.put("phase_shift", 2743247);
      DEFAULTS.put("regeneration", 32526);
      DEFAULTS.put("rock_arm", 11897382);
      DEFAULTS.put("shadow_flame", 7471334);
      DEFAULTS.put("speed", 16580403);
      DEFAULTS.put("aura_transference", 5692644);
      DEFAULTS.put("armament_dominion", 15254106);
      DEFAULTS.put("daystar", 16763210);
      DEFAULTS.put("black_lash", 5891826);
      DEFAULTS.put("salt_seraph", 15782530);
      DEFAULTS.put("chained_one", 10771711);
      DEFAULTS.put("stone_skin", 9656569);
      DEFAULTS.put("strong_kick", 2205067);
      DEFAULTS.put("strong_punch", 2591831);
      DEFAULTS.put("predator", 15242256);
      DEFAULTS.put("whirlwind", 15462645);
      DEFAULTS.put("time_manipulation", 16740288);
      DEFAULTS.put("catalyst_ray", 15258938);
      DEFAULTS.put("armor_suit", 5220607);
      DEFAULTS.put("vine_barrier", 8183914);
      DEFAULTS.put("explosive_missile", 16742972);
      DEFAULTS.put("homing_explosive_missile", 16734762);
      DEFAULTS.put("blizzard", 10479871);
      DEFAULTS.put("livewire", 13172570);
      DEFAULTS.put("arcroot", 12576842);
      DEFAULTS.put("warhead", 13159892);
      DEFAULTS.put("fusework", 14256714);
      DEFAULTS.put("missile_punch", 16757575);
      DEFAULTS.put("hand_blade", 15781984);
      DEFAULTS.put("spikes", 12550143);
      DEFAULTS.put("crescent_slash", 8377599);
      DEFAULTS.put("quick_strike", 16748608);
      DEFAULTS.put("arcane_shot", 11567080);
      DEFAULTS.put("missiles", 16740432);
      DEFAULTS.put("conjure_vines", 4055130);
      DEFAULTS.put("energy_conduit", 7264488);
      DEFAULTS.put("phantom_push", 5884159);
      DEFAULTS.put("phantom_fist", 5234920);
      DEFAULTS.put("tremor", 14198869);
      DEFAULTS.put("spectral_claw", 10178520);
      DEFAULTS.put("umbral_shift", 9067752);
      DEFAULTS.put("azure_storm", 5220584);
      DEFAULTS.put("arachnid", 14174815);
      DEFAULTS.put("particles", 15160448);
      DEFAULTS.put("sarcophage", 14594656);
      DEFAULTS.put("edict", 15254090);
      DEFAULTS.put("wyrm_engine", 3532906);
      DEFAULTS.put("singularity", 13156080);
      DEFAULTS.put("dealmaker", 15254378);
      DEFAULTS.put("limit_breaker", 16765519);
      DEFAULTS.put("counterweight", 16753964);
      DEFAULTS.put("crimson_mark", 13114940);
      DEFAULTS.put("hollow_mark", 8381416);
      DEFAULTS.put("nadir", 14477554);
      DEFAULTS.put("legacy", 16770976);
   }
}
