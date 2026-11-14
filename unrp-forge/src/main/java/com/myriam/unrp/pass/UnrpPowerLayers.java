package com.myriam.unrp.pass;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

public class UnrpPowerLayers {
   public static final ResourceLocation WHITE = new ResourceLocation("unrp", "textures/misc/white.png");

   public static int powerVariant(AbstractClientPlayer player, String shortName) {
      java.util.Set<String> k1 = PassBridge.keysOf(player.getUUID(), shortName);
      if (k1 != null && k1.contains("toggle")) return 1;
      java.util.Set<String> k2 = PassBridge.keysOf(player.getUUID(), shortName + "_am");
      return k2 != null && k2.contains("toggle") ? 2 : 0;
   }

   public static int lookVariant(AbstractClientPlayer player, String shortName) {
      int variant = powerVariant(player, shortName);
      return variant == 2 && !abilityOn(player, shortName, 2, "am_shown") ? 0 : variant;
   }

   public static boolean abilityOn(AbstractClientPlayer player, String shortName, int variant, String key) {
      java.util.Set<String> k = PassBridge.keysOf(player.getUUID(), variant == 2 ? shortName + "_am" : shortName);
      return k != null && k.contains(key);
   }

   static int auraColor(AbstractClientPlayer player, int variant, int fallback) {
      if (variant == 2) {
         return 4865594;
      } else {
         int col = PassBridge.color(player.getUUID());
         return col == -1 ? fallback : col;
      }
   }

   static int mix(int a, int b, float f) {
      f = Mth.clamp(f, 0.0F, 1.0F);
      int r = (int)((float)(a >> 16 & 255) + (float)((b >> 16 & 255) - (a >> 16 & 255)) * f);
      int g = (int)((float)(a >> 8 & 255) + (float)((b >> 8 & 255) - (a >> 8 & 255)) * f);
      int bl = (int)((float)(a & 255) + (float)((b & 255) - (a & 255)) * f);
      return r << 16 | g << 8 | bl;
   }

   static void quad(VertexConsumer vc, Matrix4f m, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, int col, int alpha, int light) {
      int r = col >> 16 & 255;
      int g = col >> 8 & 255;
      int b = col & 255;
      vtx(vc, m, p1, 0.0F, 1.0F, r, g, b, alpha, light);
      vtx(vc, m, p2, 1.0F, 1.0F, r, g, b, alpha, light);
      vtx(vc, m, p3, 1.0F, 0.0F, r, g, b, alpha, light);
      vtx(vc, m, p4, 0.0F, 0.0F, r, g, b, alpha, light);
      vtx(vc, m, p4, 0.0F, 0.0F, r, g, b, alpha, light);
      vtx(vc, m, p3, 1.0F, 0.0F, r, g, b, alpha, light);
      vtx(vc, m, p2, 1.0F, 1.0F, r, g, b, alpha, light);
      vtx(vc, m, p1, 0.0F, 1.0F, r, g, b, alpha, light);
   }

   static void vtx(VertexConsumer vc, Matrix4f m, Vec3 p, float u, float v, int r, int g, int b, int a, int light) {
      vc.vertex(m, (float)p.x, (float)p.y, (float)p.z).color(r, g, b, a).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
   }

   static Vec3[] frame(Vec3 dir) {
      Vec3 a = dir.cross(new Vec3(0.0D, 1.0D, 0.0D));
      if (a.lengthSqr() < 1.0E-5D) {
         a = dir.cross(new Vec3(1.0D, 0.0D, 0.0D));
      }

      a = a.normalize();
      Vec3 b = dir.cross(a).normalize();
      return new Vec3[]{a, b};
   }

   static void tube(VertexConsumer vc, Matrix4f m, Vec3 from, Vec3 to, double r0, double r1, int coreCol, int edgeCol, int alpha, int light) {
      Vec3 d = to.subtract(from);
      if (!(d.lengthSqr() < 1.0E-7D)) {
         Vec3[] fr = frame(d.normalize());

         for(int k = 0; k < 2; ++k) {
            Vec3 ax = fr[k];
            quad(vc, m, from.add(ax.scale(r0)), to.add(ax.scale(r1)), to.subtract(ax.scale(r1)), from.subtract(ax.scale(r0)), coreCol, alpha, light);
            Vec3 bx = fr[1 - k];
            quad(vc, m, from.add(ax.scale(r0 * 0.98D)).add(bx.scale(r0 * 0.35D)), to.add(ax.scale(r1 * 0.98D)).add(bx.scale(r1 * 0.35D)), to.subtract(ax.scale(r1 * 0.98D)).add(bx.scale(r1 * 0.35D)), from.subtract(ax.scale(r0 * 0.98D)).add(bx.scale(r0 * 0.35D)), edgeCol, alpha, light);
         }

      }
   }

   static Vec3 bez3(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
      double u = 1.0D - t;
      return p0.scale(u * u * u).add(p1.scale(3.0D * u * u * t)).add(p2.scale(3.0D * u * t * t)).add(p3.scale(t * t * t));
   }

   static Vec3 bez(Vec3 p0, Vec3 p1, Vec3 p2, double t) {
      double u = 1.0D - t;
      return p0.scale(u * u).add(p1.scale(2.0D * u * t)).add(p2.scale(t * t));
   }

   public static void drawBlade(PoseStack pose, MultiBufferSource buffers, int light, ModelPart armPart, double cxu, double sgn, int aura, float t) {
      int dim = mix(aura, 0, 0.55F);
      int bright = mix(aura, 16777215, 0.55F);
      float pulse = 0.82F + 0.18F * Mth.sin(t * 0.12F);
      VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(WHITE));
      pose.pushPose();
      armPart.translateAndRotate(pose);
      Matrix4f m = pose.last().pose();
      double s = 0.0625D;
      double cx = cxu * s;
      double outer = cx + sgn * 2.45D * s;
      double inner = cx - sgn * 2.05D * s;
      double zf = -2.35D * s;
      double zb = 2.35D * s;
      double yTop = 0.5D * s;
      double yBot = 12.2D * s;
      int segsV = 5;

      for(int i = 0; i < segsV; ++i) {
         double f0 = (double)i / (double)segsV;
         double f1 = (double)(i + 1) / (double)segsV;
         double y0 = yTop + (yBot - yTop) * f0;
         double y1 = yTop + (yBot - yTop) * f1;
         int cFront = mix(dim, aura, (float)(0.3D + 0.55D * f0));
         int cOuter = mix(dim, bright, (float)(0.35D + 0.55D * f0));
         int cInner = mix(dim, aura, (float)(0.15D + 0.35D * f0));
         quad(vc, m, new Vec3(outer, y0, zf), new Vec3(inner, y0, zf), new Vec3(inner, y1, zf), new Vec3(outer, y1, zf), cFront, 240, light);
         quad(vc, m, new Vec3(outer, y0, zb), new Vec3(inner, y0, zb), new Vec3(inner, y1, zb), new Vec3(outer, y1, zb), cFront, 240, light);
         quad(vc, m, new Vec3(outer, y0, zf), new Vec3(outer, y0, zb), new Vec3(outer, y1, zb), new Vec3(outer, y1, zf), cOuter, 245, light);
         quad(vc, m, new Vec3(inner, y0, zf), new Vec3(inner, y0, zb), new Vec3(inner, y1, zb), new Vec3(inner, y1, zf), cInner, 235, light);
      }

      quad(vc, m, new Vec3(outer, yTop, zf), new Vec3(inner, yTop, zf), new Vec3(inner, yTop, zb), new Vec3(outer, yTop, zb), dim, 240, light);
      quad(vc, m, new Vec3(outer, yBot, zf), new Vec3(inner, yBot, zf), new Vec3(inner, yBot, zb), new Vec3(outer, yBot, zb), mix(dim, aura, 0.4F), 240, light);

      for(double ex : new double[]{outer, inner}) {
         quad(vc, m, new Vec3(ex, yTop, zf - 0.15D * s), new Vec3(ex, yTop, zf + 0.15D * s), new Vec3(ex, yBot, zf + 0.15D * s), new Vec3(ex, yBot, zf - 0.15D * s), mix(bright, 16777215, 0.3F), (int)(170.0F * pulse), light);
      }

      double tipY = 18.5D * s;
      double halfD = 2.35D * s;

      for(int seg = 0; seg < 5; ++seg) {
         double f0 = (double)seg / 5.0D;
         double f1 = (double)(seg + 1) / 5.0D;
         double y0 = yBot + (tipY - yBot) * f0;
         double y1 = yBot + (tipY - yBot) * f1;
         double d0 = halfD * (1.0D - f0 * f0 * 0.92D);
         double d1 = halfD * (1.0D - f1 * f1 * 0.92D);
         int col0 = mix(dim, bright, (float)f0);
         quad(vc, m, new Vec3(cx - 0.45D * s, y0, -d0), new Vec3(cx + 0.45D * s, y0, -d0), new Vec3(cx + 0.45D * s, y1, -d1), new Vec3(cx - 0.45D * s, y1, -d1), col0, 245, light);
         quad(vc, m, new Vec3(cx - 0.45D * s, y0, d0), new Vec3(cx + 0.45D * s, y0, d0), new Vec3(cx + 0.45D * s, y1, d1), new Vec3(cx - 0.45D * s, y1, d1), col0, 245, light);
         quad(vc, m, new Vec3(cx, y0, -d0 - 0.4D * s), new Vec3(cx, y0, d0 + 0.4D * s), new Vec3(cx, y1, d1 + 0.4D * s), new Vec3(cx, y1, -d1 - 0.4D * s), mix(col0, 16777215, 0.4F), (int)(200.0F * pulse), light);
      }

      tube(vc, m, new Vec3(cx, tipY, 0.0D), new Vec3(cx, tipY + 1.6D * s, 0.0D), 0.9D * s, 0.1D * s, bright, 16777215, (int)(230.0F * pulse), light);
      pose.popPose();
   }

   public static void drawClaw(PoseStack pose, MultiBufferSource buffers, int light, ModelPart armPart, int aura, float t, boolean firstPerson) {
      int core = mix(aura, 0, 0.72F);
      int glow = mix(aura, 16777215, 0.45F);
      VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(WHITE));
      pose.pushPose();
      armPart.translateAndRotate(pose);
      if (firstPerson) {
         pose.mulPose(Axis.YP.rotationDegrees(180.0F));
      }

      Matrix4f m = pose.last().pose();
      double s = 0.0625D;
      double ox = -1.2D * s;
      int sides = 8;

      for(int i = 0; i < sides; ++i) {
         double a0 = (double)i * Math.PI * 2.0D / (double)sides;
         double a1 = (double)(i + 1) * Math.PI * 2.0D / (double)sides;

         for(int band = 0; band < 3; ++band) {
            double y0 = (3.0D + (double)band * 3.2D) * s;
            double y1 = y0 + 3.0D * s;
            double r = (4.0D + (double)band * 0.35D) * s;
            Vec3 p0 = new Vec3(ox + Math.cos(a0) * r, y0, Math.sin(a0) * r);
            Vec3 p1 = new Vec3(ox + Math.cos(a1) * r, y0, Math.sin(a1) * r);
            Vec3 p2 = new Vec3(ox + Math.cos(a1) * r * 1.06D, y1, Math.sin(a1) * r * 1.06D);
            Vec3 p3 = new Vec3(ox + Math.cos(a0) * r * 1.06D, y1, Math.sin(a0) * r * 1.06D);
            int shade = mix(core, aura, (float)(0.35D + 0.35D * Math.sin(a0 + (double)band)));
            quad(vc, m, p0, p1, p2, p3, shade, 240, light);
         }

         double yb0 = 12.6D * s;
         double yb1 = 14.4D * s;
         double rb0 = 4.55D * s;
         double rb1 = 2.9D * s;
         Vec3 q0 = new Vec3(ox + Math.cos(a0) * rb0, yb0, Math.sin(a0) * rb0);
         Vec3 q1 = new Vec3(ox + Math.cos(a1) * rb0, yb0, Math.sin(a1) * rb0);
         Vec3 q2 = new Vec3(ox + Math.cos(a1) * rb1, yb1, Math.sin(a1) * rb1);
         Vec3 q3 = new Vec3(ox + Math.cos(a0) * rb1, yb1, Math.sin(a0) * rb1);
         quad(vc, m, q0, q1, q2, q3, mix(core, aura, (float)(0.3D + 0.35D * Math.sin(a0 + 1.7D))), 240, light);
         Vec3 c = new Vec3(ox, yb1 + 0.1D * s, 0.0D);
         quad(vc, m, c, q3, q2, c, mix(core, aura, 0.28F), 240, light);
      }

      double[] baseX = new double[]{-3.1D, -1.2D, 0.7D, 2.6D};
      double[] lens = new double[]{11.0D, 13.0D, 12.5D, 10.5D};
      double[] baseY = new double[]{13.7D, 14.15D, 14.05D, 13.55D};
      double[] baseZ = new double[]{-1.7D, -2.35D, -2.25D, -1.55D};

      for(int f = 0; f < 4; ++f) {
         double bx = ox + baseX[f] * s;
         double flex = 0.92D + 0.08D * Math.sin((double)f * 1.4D);
         double spread = baseX[f] * 0.95D;
         Vec3 p0 = new Vec3(bx, baseY[f] * s, baseZ[f] * s);
         Vec3 p1 = new Vec3(bx + spread * 0.5D * s, (baseY[f] + lens[f] * 0.55D * flex) * s, (baseZ[f] - 3.4D) * s);
         Vec3 p2 = new Vec3(ox + (baseX[f] + spread) * s, (baseY[f] + lens[f] * flex) * s, (baseZ[f] + 0.8D) * s);
         talon(vc, m, p0, p1, p2, 1.1D * s, core, aura, glow, light);
      }

      double thT = 0.95D;
      talon(vc, m, new Vec3(ox + 3.6D * s, 11.8D * s, 0.6D * s), new Vec3(ox + 7.0D * s, (11.8D + 4.2D * thT) * s, -1.6D * s), new Vec3(ox + 6.2D * s, (11.8D + 8.6D * thT) * s, -3.4D * s), 1.0D * s, core, aura, glow, light);
      pose.popPose();
   }

   static void talon(VertexConsumer vc, Matrix4f m, Vec3 p0, Vec3 p1, Vec3 p2, double baseR, int core, int aura, int glow, int light) {
      int steps = 6;
      Vec3 prev = p0;

      for(int i = 1; i <= steps; ++i) {
         double f = (double)i / (double)steps;
         Vec3 cur = bez(p0, p1, p2, f);
         double r0 = baseR * (1.0D - (double)(i - 1) / (double)steps * 0.85D);
         double r1 = baseR * (1.0D - f * 0.85D);
         int col = mix(core, glow, (float)(f * f));
         tube(vc, m, prev, cur, r0, r1, col, mix(aura, 16777215, (float)f * 0.6F), 245, light);
         prev = cur;
      }

   }

   public static class ArachnidLegsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
      private static final Map<UUID, float[]> BLEND = new HashMap();
      private static final String[] POSE_KEYS = new String[]{"legs_attack", "legs_spear", "legs_leap", "legs_guard"};

      public static void clearBlend() {
         BLEND.clear();
      }

      public ArachnidLegsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
         super(parent);
      }

      private static float[] target(String poseKey) {
         switch (poseKey == null ? "idle" : poseKey) {
            case "legs_attack":
               return new float[]{2.5F, 14.0F, 16.0F, -2.0F, 10.0F, 14.0F, -22.0F, 8.0F, 4.8F, 17.0F, 13.0F, 0.0F, 13.0F, 10.0F, -24.0F, 7.0F, 7.0F, 18.0F, 10.0F, 2.0F, 15.0F, 5.0F, -22.0F, 6.0F, 9.5F, 16.0F, 6.0F, 3.0F, 15.0F, 0.0F, -18.0F, 5.0F};
            case "legs_spear":
               return new float[]{2.5F, 9.0F, 14.0F, -6.0F, 4.0F, 10.0F, -30.0F, 4.0F, 4.8F, 10.0F, 12.0F, -6.0F, 5.0F, 8.0F, -32.0F, 4.0F, 7.0F, 10.0F, 9.0F, -6.0F, 5.0F, 5.0F, -32.0F, 4.0F, 9.5F, 9.0F, 6.0F, -5.0F, 4.0F, 2.0F, -30.0F, 4.0F};
            case "legs_leap":
               return new float[]{2.5F, 20.0F, 8.0F, 4.0F, 26.0F, 0.0F, 10.0F, 4.0F, 4.8F, 22.0F, 6.0F, 5.0F, 28.0F, -3.0F, 12.0F, 4.0F, 7.0F, 22.0F, 4.0F, 6.0F, 28.0F, -5.0F, 13.0F, 4.0F, 9.5F, 20.0F, 2.0F, 6.0F, 26.0F, -6.0F, 12.0F, 4.0F};
            case "legs_guard":
               return new float[]{2.5F, 15.0F, 15.0F, -3.0F, 4.0F, 16.0F, -17.0F, 15.0F, 4.8F, 17.0F, 12.0F, -2.0F, 5.0F, 12.0F, -18.0F, 14.0F, 7.0F, 17.0F, 9.0F, -1.0F, 6.0F, 8.0F, -17.0F, 13.0F, 9.5F, 15.0F, 5.0F, 0.0F, 6.0F, 3.0F, -14.0F, 11.0F};
            default:
               return new float[]{2.5F, 16.0F, 18.0F, 1.0F, 12.0F, 27.0F, -24.0F, 15.0F, 4.8F, 20.0F, 15.0F, 2.0F, 18.0F, 20.0F, -21.0F, 13.0F, 7.0F, 22.0F, 11.0F, 3.0F, 22.0F, 10.0F, -16.0F, 11.0F, 9.5F, 20.0F, 6.0F, 4.0F, 21.0F, -2.0F, -11.0F, 9.0F};
         }
      }

      public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
         if (!player.isInvisible()) {
            int variant = UnrpPowerLayers.lookVariant(player, "arachnid");
            if (variant == 0) {
               BLEND.remove(player.getUUID());
            } else {
               String poseKey = null;

               for(String k : POSE_KEYS) {
                  if (UnrpPowerLayers.abilityOn(player, "arachnid", variant, k)) {
                     poseKey = k;
                     break;
                  }
               }

               float[] tgt = target(poseKey);
               float[] cur = (float[])BLEND.computeIfAbsent(player.getUUID(), (u) -> (float[])tgt.clone());

               for(int i = 0; i < cur.length; ++i) {
                  cur[i] += (tgt[i] - cur[i]) * 0.16F;
               }

               int aura = UnrpPowerLayers.auraColor(player, variant, 14174815);
               int chitin = 1512474;
               int chitinEdge = UnrpPowerLayers.mix(chitin, aura, 0.22F);
               int jointGlow = UnrpPowerLayers.mix(aura, 16777215, 0.3F);
               float t = (float)player.tickCount + partialTick;
               VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucent(UnrpPowerLayers.WHITE));
               pose.pushPose();
               ((PlayerModel)this.getParentModel()).body.translateAndRotate(pose);
               Matrix4f m = pose.last().pose();
               double s = 0.0625D;

               for(int side = 0; side < 2; ++side) {
                  double sx = side == 0 ? 1.0D : -1.0D;

                  for(int i = 0; i < 4; ++i) {
                     int o = i * 8;
                     double mY = (double)cur[o];
                     double c1Out = (double)cur[o + 1];
                     double c1Up = (double)cur[o + 2];
                     double c1Z = (double)cur[o + 3];
                     double tipOut = (double)cur[o + 4];
                     double tipUp = (double)cur[o + 5];
                     double tipZ = (double)cur[o + 6];
                     double wrap = (double)cur[o + 7];
                     double sway = Math.sin((double)t * 0.055D + (double)i * 1.5D + (double)side * 3.1D) * 1.1D;
                     double skitter = Math.sin((double)limbSwing * 0.7D + (double)i * 1.9D + (double)side * 2.2D) * (double)limbSwingAmount * 2.4D;
                     Vec3 mount = new Vec3(sx * (1.4D + (double)i * 0.35D) * s, mY * s, (2.2D + (double)i * 0.25D) * s);
                     Vec3 c1 = new Vec3(sx * (c1Out + sway * 0.4D) * s, (mY - c1Up) * s, (2.2D + c1Z) * s);
                     Vec3 c2 = new Vec3(sx * (tipOut + wrap) * s, (mY - tipUp * 0.9D - c1Up * 0.35D) * s, (tipZ * 0.55D + c1Z * 0.45D + 2.2D) * s);
                     Vec3 tip = new Vec3(sx * (tipOut + sway) * s, (mY - tipUp - skitter) * s, (2.2D + tipZ) * s);
                     Vec3 j1 = UnrpPowerLayers.bez3(mount, c1, c2, tip, 0.32D);
                     Vec3 j2 = UnrpPowerLayers.bez3(mount, c1, c2, tip, 0.62D);
                     UnrpPowerLayers.tube(vc, m, mount, j1, 1.5D * s, 1.1D * s, chitin, chitinEdge, 250, light);
                     UnrpPowerLayers.tube(vc, m, j1, j2, 1.1D * s, 0.65D * s, chitin, chitinEdge, 250, light);
                     UnrpPowerLayers.tube(vc, m, j2, tip, 0.65D * s, 0.12D * s, chitin, chitinEdge, 250, light);

                     for(Vec3 j : new Vec3[]{j1, j2}) {
                        UnrpPowerLayers.tube(vc, m, j.add(0.0D, -0.4D * s, 0.0D), j.add(0.0D, 0.4D * s, 0.0D), 1.15D * s, 1.15D * s, jointGlow, jointGlow, 190, light);
                     }

                     UnrpPowerLayers.tube(vc, m, tip, tip.add(tip.subtract(j2).normalize().scale(1.8D * s)), 0.32D * s, 0.02D * s, jointGlow, 16777215, 235, light);
                  }
               }

               pose.popPose();
            }
         }
      }
   }

   @EventBusSubscriber(
      modid = "unrp",
      bus = Bus.FORGE,
      value = {Dist.CLIENT}
   )
   public static class FirstPersonPowerArms {
      @SubscribeEvent
      public static void onRenderArm(RenderArmEvent event) {
         Minecraft mc = Minecraft.getInstance();
         AbstractClientPlayer player = mc.player;
         if (player != null) {
            EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
            EntityRenderer model = dispatcher.getRenderer(player);
            if (model instanceof PlayerRenderer) {
               PlayerRenderer renderer = (PlayerRenderer)model;
               PlayerModel var12 = (PlayerModel)renderer.getModel();
               ModelPart armPart = event.getArm() == HumanoidArm.RIGHT ? var12.rightArm : var12.leftArm;
               float t = (float)player.tickCount + mc.getPartialTick();
               int bladeVariant = UnrpPowerLayers.lookVariant(player, "hand_blade");
               if (bladeVariant != 0) {
                  int aura = UnrpPowerLayers.auraColor(player, bladeVariant, 15781984);
                  double cxu = event.getArm() == HumanoidArm.RIGHT ? -1.0D : 1.0D;
                  UnrpPowerLayers.drawBlade(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), armPart, cxu, cxu, aura, t);
               }

               if (event.getArm() == HumanoidArm.RIGHT) {
                  int clawVariant = UnrpPowerLayers.lookVariant(player, "spectral_claw");
                  if (clawVariant != 0) {
                     int aura = UnrpPowerLayers.auraColor(player, clawVariant, 10178520);
                     UnrpPowerLayers.drawClaw(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), armPart, aura, t, true);
                  }
               }

            }
         }
      }
   }

   public static class HandBladesLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
      public HandBladesLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
         super(parent);
      }

      public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
         if (!player.isInvisible()) {
            int variant = UnrpPowerLayers.lookVariant(player, "hand_blade");
            if (variant != 0) {
               int aura = UnrpPowerLayers.auraColor(player, variant, 15781984);
               float t = (float)player.tickCount + partialTick;
               UnrpPowerLayers.drawBlade(pose, buffers, light, ((PlayerModel)this.getParentModel()).rightArm, -1.0D, -1.0D, aura, t);
               UnrpPowerLayers.drawBlade(pose, buffers, light, ((PlayerModel)this.getParentModel()).leftArm, 1.0D, 1.0D, aura, t + 40.0F);
            }
         }
      }
   }

   public static class SpectralClawLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
      public SpectralClawLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
         super(parent);
      }

      public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
         if (!player.isInvisible()) {
            int variant = UnrpPowerLayers.lookVariant(player, "spectral_claw");
            if (variant != 0) {
               int aura = UnrpPowerLayers.auraColor(player, variant, 10178520);
               float t = (float)player.tickCount + partialTick;
               ModelPart arm = ((PlayerModel)this.getParentModel()).rightArm;
               float bobX = Mth.sin(age * 0.067F) * 0.05F;
               float bobZ = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
               float saveX = arm.xRot;
               float saveZ = arm.zRot;
               arm.xRot -= bobX;
               arm.zRot -= bobZ;
               UnrpPowerLayers.drawClaw(pose, buffers, light, arm, aura, t, false);
               arm.xRot = saveX;
               arm.zRot = saveZ;
            }
         }
      }
   }
}
