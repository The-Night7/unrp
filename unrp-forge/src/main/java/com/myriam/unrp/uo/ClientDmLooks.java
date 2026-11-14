package com.myriam.unrp.uo;

import com.myriam.unrp.look.ClientSettingsCache;
import com.myriam.unrp.look.CustomEyesLayer;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class ClientDmLooks {
   public static final double SEEN = 96.0D;
   public static final double PAPER_W = 0.9D;
   public static final double PAPER_H = 1.0D;
   public static final double FIRST_LINE = 0.3D;
   public static final double LINE_GAP = 0.065D;
   public static final double SIGN_AT = -0.31D;
   public static final double SEAL_U = 0.3D;
   public static final double WAX_R = 0.075D;
   public static final int RIP_HOOK = 8;
   public static final int RIP_DRAW = 12;
   public static final int RIP_ARRIVE = 30;
   public static final int RIP_FOLD = 6;
   public static final int RIP_LET_GO = 34;
   public static final int KNEEL_FROM = 10;
   public static final int KNEEL_TO = 38;
   public static final double KNEEL_DIP = 0.32D;
   public static final float GREY_MOST = 0.35F;
   public static final int THREAD_OUT = 6;
   public static final int THREAD_SINK = 26;
   public static final int THREAD_GONE = 36;
   public static final int MOST = 48;
   public static final int GOLD_RGB = 15254378;
   private static final ResourceLocation WHITE = new ResourceLocation("unrp", "textures/misc/white.png");
   private static final ResourceLocation INK_WHITE = new ResourceLocation("unrp", "textures/misc/white_ink.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final float[] PAPER_TOP = rgb(14467986);
   private static final float[] PAPER_LOW = rgb(12491892);
   private static final float[] SOUL_TOP = rgb(13551540);
   private static final float[] EDGE = rgb(6044190);
   private static final float[] SOUL_EDGE = rgb(3033692);
   private static final float[] WOOD = rgb(4861462);
   private static final float[] WOOD_LIT = rgb(8016432);
   private static final float[] BRASS = rgb(11569724);
   private static final float[] INK = rgb(4863264);
   private static final float[] INK_SLATE = rgb(3033692);
   private static final float[] SIGN_RED = rgb(7214112);
   private static final float[] SIGN_SOUL = rgb(3041948);
   private static final float[] WAX_RIM = rgb(4852496);
   private static final float[] WAX = rgb(9313580);
   private static final float[] WAX_EMBLEM = rgb(11546688);
   private static final float[] WAX_RIM_SOUL = rgb(731722);
   private static final float[] WAX_SOUL = rgb(1920654);
   private static final float[] WAX_EMBLEM_SOUL = rgb(3174576);
   private static final float[] FLASH = rgb(16767392);
   private static final float[] FLASH_SOUL = rgb(10477823);
   private static final float[] THREAD = rgb(10099236);
   private static final float[] THREAD_SOUL = rgb(5882600);
   private static final float[] CHAIN = rgb(3108510);
   private static final float[] CHAIN_LIT = rgb(9427186);
   private static final float[] SOUL = rgb(5882600);
   private static final float[] GOLD = rgb(15254378);
   private static final float[] GREY = new float[]{0.55F, 0.56F, 0.6F};
   private static final List<ClientDmLooks.Look> LOOKS = new ArrayList();
   private static final Map<Integer, ClientDmLooks.Look> RIPS = new HashMap();
   private static final Map<Integer, long[]> BORROWED = new HashMap();
   private static boolean inWorld;

   private ClientDmLooks() {
   }

   static void accept(OriginLookPacket msg, long now) {
      switch (msg.kind) {
         case 13:
         case 15:
            if (LOOKS.size() >= 48) {
               LOOKS.remove(0);
            }

            LOOKS.add(new ClientDmLooks.Look(msg, now));
            break;
         case 14:
            RIPS.put(msg.entity, new ClientDmLooks.Look(msg, now));
            break;
         case 16:
            if (msg.life > 0 && msg.names.size() >= 2) {
               long from = now + (long)msg.get(0);
               long[] had = (long[])BORROWED.get(msg.entity);
               if (had != null && now <= had[1]) {
                  from = had[0];
               }

               BORROWED.put(msg.entity, new long[]{from, now + (long)msg.life, (long)colourOf((String)msg.names.get(0), (String)msg.names.get(1))});
            } else {
               BORROWED.remove(msg.entity);
            }
      }

   }

   private static int colourOf(String id, String power) {
      UUID uuid;
      try {
         uuid = UUID.fromString(id);
      } catch (IllegalArgumentException var4) {
         return 15254378;
      }

      int c = ClientSettingsCache.getEyeColor(uuid);
      if (c == -1) {
         c = ClientSettingsCache.getColor(uuid);
      }

      if (c == -1) {
         c = CustomEyesLayer.DEFAULTS.getOrDefault(power, 15254378);
      }

      return c;
   }

   public static int borrowed(int entity, long now) {
      long[] b = (long[])BORROWED.get(entity);
      return b != null && now >= b[0] && now <= b[1] ? (int)b[2] : -1;
   }

   public static boolean busy() {
      return !LOOKS.isEmpty() || !RIPS.isEmpty() || !BORROWED.isEmpty();
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level == null) {
            if (busy()) {
               clearAll();
            }

         } else {
            long now = mc.level.getGameTime();
            LOOKS.removeIf((l) -> now > l.born + (long)l.msg.life + 2L || now < l.born - 200L);
            RIPS.values().removeIf((l) -> now > l.born + (long)l.msg.life + 2L || now < l.born - 200L);
            BORROWED.values().removeIf((b) -> now > b[1]);
         }
      }
   }

   private static double kneel(double age) {
      return smooth((age - 10.0D) / 4.0D) * (1.0D - smooth((age - 38.0D) / 6.0D));
   }

   private static double dip(Entity e, double age) {
      return 0.32D * kneel(age) * (double)e.getBbHeight() / 1.8D;
   }

   private static double scaleOf(Entity e) {
      return e instanceof Player ? Math.max(0.05D, (double)e.getBbWidth() / 0.6D) : 1.0D;
   }

   public static float grey(double now) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && !RIPS.isEmpty()) {
         ClientDmLooks.Look l = (ClientDmLooks.Look)RIPS.get(mc.player.getId());
         return l == null ? 0.0F : (float)(kneel(now - (double)l.born) * (double)0.35F);
      } else {
         return 0.0F;
      }
   }

   @SubscribeEvent
   public static void onRenderPre(RenderLivingEvent.Pre<?, ?> event) {
      if (inWorld && !RIPS.isEmpty()) {
         LivingEntity e = event.getEntity();
         ClientDmLooks.Look l = (ClientDmLooks.Look)RIPS.get(e.getId());
         if (l != null && !e.isInvisible() && GravityFrame.plain(GravityGuard.direction(e))) {
            double age = (double)e.level().getGameTime() + (double)event.getPartialTick() - (double)l.born;
            double down = dip(e, age);
            if (down > 0.001D) {
               event.getPoseStack().translate(0.0D, -down / scaleOf(e), 0.0D);
            }

         }
      }
   }

   @SubscribeEvent
   public static void onRenderPost(RenderLivingEvent.Post<?, ?> event) {
      if (inWorld && !RIPS.isEmpty()) {
         LivingEntity e = event.getEntity();
         ClientDmLooks.Look l = (ClientDmLooks.Look)RIPS.get(e.getId());
         if (l != null && !e.isInvisible() && GravityFrame.plain(GravityGuard.direction(e))) {
            float pt = event.getPartialTick();
            double age = (double)e.level().getGameTime() + (double)pt - (double)l.born;
            double down = dip(e, age);
            double pk = scaleOf(e);
            PoseStack ps = event.getPoseStack();

            try {
               EntityModel<?> model = event.getRenderer().getModel();
               MultiBufferSource buffers = event.getMultiBufferSource();
               float body = Mth.rotLerp(pt, e.yBodyRotO, e.yBodyRot);
               float scale = e instanceof Player ? 0.9375F : 1.0F;
               double k = kneel(age);
               if (k > 0.01D) {
                  ps.pushPose();

                  try {
                     ps.mulPose(Axis.YP.rotationDegrees(180.0F - body));
                     ps.scale(-scale * 1.02F, -scale * 1.02F, scale * 1.02F);
                     ps.translate(0.0D, -1.501D, 0.0D);
                     model.renderToBuffer(ps, buffers.getBuffer(RenderType.entityTranslucent(WHITE, false)), event.getPackedLight(), OverlayTexture.NO_OVERLAY, GREY[0], GREY[1], GREY[2], (float)(0.5D * k));
                  } finally {
                     ps.popPose();
                  }
               }

               double s = (age - 12.0D) / 18.0D;
               double fold = smooth((age - 30.0D) / 6.0D);
               if (!(s <= 0.0D) && !(fold >= 1.0D)) {
                  Vec3 hand = ripHand(e.level(), l.msg, pt);
                  if (hand != null) {
                     double ex = Mth.lerp((double)pt, e.xo, e.getX());
                     double ey = Mth.lerp((double)pt, e.yo, e.getY());
                     double ez = Mth.lerp((double)pt, e.zo, e.getZ());
                     double h = (double)e.getBbHeight();
                     Vec3 d = hand.subtract(new Vec3(ex, ey - down + h * 0.62D, ez)).scale(smooth(s));
                     double yaw = Math.toDegrees(Math.atan2(-(hand.x - ex), hand.z - ez));
                     double stretch = Math.sin(Math.PI * Math.min(1.0D, s));
                     float a = (float)(0.45D * smooth((age - 12.0D) / 3.0D) * (1.0D - fold));
                     float shrink = (float)(1.0D - fold);
                     if (!(a < 0.02F) && !(shrink < 0.02F)) {
                        ps.pushPose();

                        try {
                           ps.translate(d.x / pk, (d.y + h * 0.62D) / pk, d.z / pk);
                           ps.mulPose(Axis.YP.rotationDegrees((float)(180.0D - yaw)));
                           ps.mulPose(Axis.XP.rotationDegrees((float)(-22.0D * stretch)));
                           ps.scale(shrink, shrink * (float)(1.0D - 0.12D * stretch), shrink * (float)(1.0D + 1.4D * stretch));
                           ps.translate(0.0D, -h * 0.62D / pk, 0.0D);
                           ps.scale(-scale, -scale, scale);
                           ps.translate(0.0D, -1.501D, 0.0D);
                           model.renderToBuffer(ps, buffers.getBuffer(RenderType.entityTranslucentEmissive(WHITE, false)), 15728880, OverlayTexture.NO_OVERLAY, SOUL[0], SOUL[1], SOUL[2], a);
                           return;
                        } finally {
                           ps.popPose();
                        }
                     }

                     return;
                  }

                  return;
               }
            } catch (Throwable var53) {
               RIPS.clear();
               return;
            } finally {
               if (down > 0.001D) {
                  ps.translate(0.0D, down / pk, 0.0D);
               }

            }

         }
      }
   }

   @SubscribeEvent
   public static void onStage(RenderLevelStageEvent event) {
      RenderLevelStageEvent.Stage stage = event.getStage();
      if (stage == Stage.AFTER_SKY) {
         inWorld = true;
      } else if (stage != Stage.AFTER_ENTITIES && stage != Stage.AFTER_LEVEL) {
         if (stage == Stage.AFTER_TRANSLUCENT_BLOCKS) {
            if (!LOOKS.isEmpty() || !RIPS.isEmpty()) {
               Minecraft mc = Minecraft.getInstance();
               ClientLevel level = mc.level;
               if (level != null) {
                  Vec3 cam = event.getCamera().getPosition();
                  float pt = event.getPartialTick();
                  double now = (double)level.getGameTime() + (double)pt;
                  MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
                  RenderType paper = RenderType.entityTranslucentEmissive(WHITE);
                  RenderType ink = RenderType.entityTranslucentEmissive(WHITE, false);
                  RenderType string = RenderType.entityTranslucentEmissive(INK_WHITE, false);
                  RenderType glow = RenderType.eyes(WHITE);
                  Matrix4f m = event.getPoseStack().last().pose();
                  float fogStart = RenderSystem.getShaderFogStart();
                  RenderSystem.setShaderFogStart(1000000.0F);

                  try {
                     VertexConsumer vc = buffers.getBuffer(paper);

                     for(ClientDmLooks.Look l_0 : LOOKS) {
                        double age_0 = now - (double)l_0.born;
                        if (!(age_0 < 0.0D) && !(age_0 > (double)l_0.msg.life)) {
                           if (l_0.msg.kind == 13) {
                              ClientDmLooks.Frame f_0 = frame(level, l_0.msg, cam, age_0, pt);
                              if (f_0 != null) {
                                 sealPaper(vc, m, cam, f_0, l_0.msg.get(4) > 0.5D);
                              }
                           } else if (l_0.msg.kind == 15) {
                              goldThread(vc, m, cam, level, l_0.msg, age_0, pt);
                           }
                        }
                     }

                     for(ClientDmLooks.Look l : RIPS.values()) {
                        double age = now - (double)l.born;
                        if (age >= 0.0D && age <= (double)l.msg.life) {
                           chain(vc, m, cam, level, l.msg, age, pt);
                        }
                     }

                     VertexConsumer details = buffers.getBuffer(ink);

                     for(ClientDmLooks.Look l : LOOKS) {
                        double age = now - (double)l.born;
                        if (l.msg.kind == 13 && !(age < 0.0D) && !(age > (double)l.msg.life)) {
                           ClientDmLooks.Frame f = frame(level, l.msg, cam, age, pt);
                           if (f != null) {
                              sealInk(details, m, cam, l.msg, f, age);
                           }
                        }
                     }

                     VertexConsumer strings = buffers.getBuffer(string);

                     for(ClientDmLooks.Look l : LOOKS) {
                        double age = now - (double)l.born;
                        if (l.msg.kind == 13 && !(age < 0.0D) && !(age > (double)l.msg.life)) {
                           ClientDmLooks.Frame f = frame(level, l.msg, cam, age, pt);
                           if (f != null) {
                              threads(strings, m, cam, level, l.msg, f, age, pt);
                           }
                        }
                     }

                     VertexConsumer flash = buffers.getBuffer(glow);

                     for(ClientDmLooks.Look l : LOOKS) {
                        double age = now - (double)l.born;
                        if (l.msg.kind == 13 && !(age < 0.0D) && !(age > (double)l.msg.life)) {
                           ClientDmLooks.Frame f = frame(level, l.msg, cam, age, pt);
                           if (f != null) {
                              sealFlash(flash, m, cam, l.msg, f, age);
                           }
                        }
                     }

                     buffers.endBatch(glow);
                  } catch (Throwable var27) {
                     LOOKS.clear();
                     RIPS.clear();
                  } finally {
                     RenderSystem.setShaderFogStart(fogStart);
                  }

               }
            }
         }
      } else {
         inWorld = false;
      }
   }

   private static int lines(OriginLookPacket msg) {
      return Mth.clamp((int)msg.get(5), 1, 8);
   }

   private static ClientDmLooks.Frame frame(ClientLevel level, OriginLookPacket msg, Vec3 cam, double age, float pt) {
      int lines = lines(msg);
      Vec3 spot = new Vec3(msg.get(0), msg.get(1), msg.get(2));
      double unroll = smooth(age / 10.0D);
      double roll = smooth((age - (double)OriginLookPacket.sealThreaded(lines)) / 14.0D);
      Vec3 hand = keeperHand(level, msg, cam, pt);
      Vec3 c = hand == null ? spot.add(0.0D, 0.6D * roll, 0.0D) : spot.lerp(hand, roll);
      double dist = c.distanceTo(cam);
      if (!(dist > 96.0D) && !(dist < 0.9D)) {
         Vec3 fwd = new Vec3(c.x - cam.x, 0.0D, c.z - cam.z);
         ClientDmLooks.Frame f = new ClientDmLooks.Frame();
         f.c = c;
         f.right = fwd.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : fwd.normalize().cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
         f.s = 1.0D - 0.72D * roll;
         f.top = 0.5D;
         f.bot = 0.5D - 1.0D * unroll * (1.0D - roll);
         f.a = (float)((hand == null ? 1.0D - roll : 1.0D - smooth((age - (double)msg.life + 3.0D) / 3.0D)) * smooth((dist - 0.9D) / 0.9D));
         return f.a < 0.02F ? null : f;
      } else {
         return null;
      }
   }

   private static Vec3 at(ClientDmLooks.Frame f, double u, double w) {
      return new Vec3(f.c.x + f.right.x * u * f.s, f.c.y + w * f.s, f.c.z + f.right.z * u * f.s);
   }

   private static Vec3 keeperHand(ClientLevel level, OriginLookPacket msg, Vec3 cam, float pt) {
      if (msg.entity < 0) {
         return null;
      } else {
         Entity e = level.getEntity(msg.entity);
         Minecraft mc = Minecraft.getInstance();
         if (e != null && e == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
            return cam.add(mc.player.getViewVector(pt).scale(0.7D)).add(0.0D, -0.3D, 0.0D);
         } else if (e instanceof LivingEntity) {
            LivingEntity le = (LivingEntity)e;
            return le.isInvisible() ? null : handOf(le, pt);
         } else {
            return new Vec3(msg.get(10), msg.get(11) - 0.25D, msg.get(12));
         }
      }
   }

   private static Vec3 ripHand(Level level, OriginLookPacket msg, float pt) {
      Entity e = level.getEntity((int)msg.get(0));
      Minecraft mc = Minecraft.getInstance();
      if (e != null && e == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
         Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
         return cam.add(mc.player.getViewVector(pt).scale(0.7D)).add(0.0D, -0.3D, 0.0D);
      } else if (e instanceof LivingEntity) {
         LivingEntity le = (LivingEntity)e;
         return le.isInvisible() ? null : handOf(le, pt);
      } else {
         return new Vec3(msg.get(1), msg.get(2), msg.get(3));
      }
   }

   static Vec3 handOf(LivingEntity e, float pt) {
      double yaw = Math.toRadians((double)Mth.rotLerp(pt, e.yBodyRotO, e.yBodyRot));
      double side = e.getMainArm() == HumanoidArm.LEFT ? -1.0D : 1.0D;
      double rx = -Math.cos(yaw) * side;
      double rz = -Math.sin(yaw) * side;
      double fx = -Math.sin(yaw);
      double fz = Math.cos(yaw);
      double s = (double)e.getBbHeight() / 1.8D;
      return new Vec3(Mth.lerp((double)pt, e.xo, e.getX()) + (rx * 0.36D + fx * 0.25D) * s, Mth.lerp((double)pt, e.yo, e.getY()) + 0.85D * s, Mth.lerp((double)pt, e.zo, e.getZ()) + (rz * 0.36D + fz * 0.25D) * s);
   }

   private static Vec3 chest(ClientLevel level, int id, double fx, double fy, double fz, float pt) {
      if (id < 0) {
         return null;
      } else {
         Entity e = level.getEntity(id);
         if (e == null) {
            return new Vec3(fx, fy, fz);
         } else {
            return e.isInvisible() ? null : new Vec3(Mth.lerp((double)pt, e.xo, e.getX()), Mth.lerp((double)pt, e.yo, e.getY()) + (double)e.getBbHeight() * 0.6D, Mth.lerp((double)pt, e.zo, e.getZ()));
         }
      }
   }

   private static void sealPaper(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientDmLooks.Frame f, boolean soul) {
      if (!(f.top - f.bot < 0.005D)) {
         rect(vc, m, cam, f, -0.45D, f.bot, 0.45D, f.top, PAPER_LOW, soul ? SOUL_TOP : PAPER_TOP, 0.93F * f.a);
      }
   }

   private static void sealInk(VertexConsumer vc, Matrix4f m, Vec3 cam, OriginLookPacket msg, ClientDmLooks.Frame f, double age) {
      boolean soul = msg.get(4) > 0.5D;
      int lines = lines(msg);
      int seed = (int)msg.get(6);
      float a = f.a;
      boolean open = f.top - f.bot > 0.06D;
      if (open) {
         float[] edge = soul ? SOUL_EDGE : EDGE;
         seg(vc, m, cam, f, -0.45D, f.bot + 0.012D, -0.45D, f.top - 0.012D, 0.012D, edge, 0.8F * a);
         seg(vc, m, cam, f, 0.45D, f.bot + 0.012D, 0.45D, f.top - 0.012D, 0.012D, edge, 0.8F * a);
      }

      roller(vc, m, cam, f, f.top, false, a);
      if (open) {
         roller(vc, m, cam, f, f.bot, true, a);
      }

      for(int i = 0; i < lines; ++i) {
         double w = 0.3D - (double)i * 0.065D;
         double prog = Mth.clamp((age - 10.0D - (double)(i * 5)) / 5.0D, 0.0D, 1.0D);
         if (!(prog <= 0.0D) && !(w - 0.012D < f.bot + 0.03D)) {
            words(vc, m, cam, f, w, prog, seed * 13 + i * 31, i == 0 ? 0.024D : 0.017D, soul ? INK_SLATE : INK, a);
         }
      }

      double sp = Mth.clamp((age - (double)OriginLookPacket.sealWritten(lines)) / 10.0D, 0.0D, 1.0D);
      if (sp > 0.0D && -0.37D > f.bot + 0.02D) {
         signature(vc, m, cam, f, sp, seed, soul ? SIGN_SOUL : SIGN_RED, a);
      }

      double wp = Mth.clamp((age - (double)OriginLookPacket.sealSigned(lines)) / 6.0D, 0.0D, 1.0D);
      if (wp > 0.0D && -0.41000000000000003D > f.bot + 0.01D) {
         double r = 0.075D * (1.0D + 0.9D * (1.0D - smooth(wp)));
         float wa = (float)((double)a * Math.min(1.0D, wp * 2.0D));
         disc(vc, m, cam, f, 0.3D, -0.31D, 0.0D, r * 0.35D, soul ? WAX_EMBLEM_SOUL : WAX_EMBLEM, wa, 12);
         disc(vc, m, cam, f, 0.3D, -0.31D, r * 0.35D, r * 0.8D, soul ? WAX_SOUL : WAX, wa, 16);
         disc(vc, m, cam, f, 0.3D, -0.31D, r * 0.8D, r, soul ? WAX_RIM_SOUL : WAX_RIM, wa, 16);
      }

   }

   private static void sealFlash(VertexConsumer vc, Matrix4f m, Vec3 cam, OriginLookPacket msg, ClientDmLooks.Frame f, double age) {
      double ra = age - (double)OriginLookPacket.sealWaxed(lines(msg));
      if (!(ra < 0.0D) && !(ra > 8.0D)) {
         boolean soul = msg.get(4) > 0.5D;
         double r = 0.1D + 0.45D * smooth(ra / 8.0D);
         float k = (float)(0.8D * (double)f.a * (1.0D - ra / 8.0D));
         float[] base = soul ? FLASH_SOUL : FLASH;
         float[] col = new float[]{base[0] * k, base[1] * k, base[2] * k};

         for(int i = 0; i < 32; ++i) {
            double a0 = (Math.PI * 2D) * (double)i / 32.0D;
            double a1 = (Math.PI * 2D) * (double)(i + 1) / 32.0D;
            Vec3 p0 = at(f, 0.3D + Math.cos(a0) * (r - 0.02D), -0.31D + Math.sin(a0) * (r - 0.02D));
            Vec3 p1 = at(f, 0.3D + Math.cos(a1) * (r - 0.02D), -0.31D + Math.sin(a1) * (r - 0.02D));
            Vec3 p2 = at(f, 0.3D + Math.cos(a1) * r, -0.31D + Math.sin(a1) * r);
            Vec3 p3 = at(f, 0.3D + Math.cos(a0) * r, -0.31D + Math.sin(a0) * r);
            ClientVertigo.quad(vc, m, col, p0.x - cam.x, p0.y - cam.y, p0.z - cam.z, p1.x - cam.x, p1.y - cam.y, p1.z - cam.z, p2.x - cam.x, p2.y - cam.y, p2.z - cam.z, p3.x - cam.x, p3.y - cam.y, p3.z - cam.z, 1.0F, 1.0F, 1.0F, 1.0F);
            ClientVertigo.quad(vc, m, col, p3.x - cam.x, p3.y - cam.y, p3.z - cam.z, p2.x - cam.x, p2.y - cam.y, p2.z - cam.z, p1.x - cam.x, p1.y - cam.y, p1.z - cam.z, p0.x - cam.x, p0.y - cam.y, p0.z - cam.z, 1.0F, 1.0F, 1.0F, 1.0F);
         }

      }
   }

   private static void threads(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientLevel level, OriginLookPacket msg, ClientDmLooks.Frame f, double age, float pt) {
      double tp = (age - (double)OriginLookPacket.sealWaxed(lines(msg))) / 16.0D;
      if (!(tp <= 0.0D) && !(tp >= 1.0D)) {
         double head = smooth(tp / 0.5D);
         double tail = smooth((tp - 0.5D) / 0.5D);
         if (!(head <= tail + 0.01D)) {
            boolean soul = msg.get(4) > 0.5D;
            Vec3 from = at(f, 0.3D, -0.31D);
            Vec3 signer = chest(level, (int)msg.get(3), msg.get(7), msg.get(8), msg.get(9), pt);
            Vec3 keeper = chest(level, msg.entity, msg.get(10), msg.get(11), msg.get(12), pt);

            for(Vec3 to : new Vec3[]{signer, keeper}) {
               if (to != null && !(to.distanceToSqr(from) > 1600.0D)) {
                  bezier(vc, m, cam, from, to, tail, head, 0.03D, soul ? THREAD_SOUL : THREAD, 0.9F * f.a);
               }
            }

         }
      }
   }

   private static void goldThread(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientLevel level, OriginLookPacket msg, double age, float pt) {
      int allyId = (int)msg.get(0);
      if (allyId >= 0) {
         Entity dm = level.getEntity(msg.entity);
         if (dm != null && !dm.isInvisible()) {
            Minecraft mc = Minecraft.getInstance();
            boolean mine = dm == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON;
            Vec3 to = mine ? cam.add(0.0D, -0.45D, 0.0D) : new Vec3(Mth.lerp((double)pt, dm.xo, dm.getX()), Mth.lerp((double)pt, dm.yo, dm.getY()) + (double)dm.getBbHeight() * 0.6D, Mth.lerp((double)pt, dm.zo, dm.getZ()));
            Entity ally = level.getEntity(allyId);
            if (ally == null || !ally.isInvisible()) {
               Vec3 from = ally != null ? new Vec3(Mth.lerp((double)pt, ally.xo, ally.getX()), Mth.lerp((double)pt, ally.yo, ally.getY()) + (double)ally.getBbHeight() * 0.6D, Mth.lerp((double)pt, ally.zo, ally.getZ())) : new Vec3(msg.get(1), msg.get(2), msg.get(3));
               if (!(from.distanceToSqr(to) > 4096.0D) && !(to.distanceToSqr(cam) > 9216.0D)) {
                  double head = smooth(age / 6.0D);
                  double sink = smooth((age - 26.0D) / 10.0D);
                  double reach = Math.min(head, 1.0D - sink);
                  if (!(reach <= 0.01D)) {
                     bezier(vc, m, cam, to, from, 0.0D, reach, 0.022D, GOLD, 0.85F);
                  }
               }
            }
         }
      }
   }

   private static void chain(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientLevel level, OriginLookPacket msg, double age, float pt) {
      Entity victim = level.getEntity(msg.entity);
      if (victim != null && !victim.isInvisible()) {
         Vec3 chest = new Vec3(Mth.lerp((double)pt, victim.xo, victim.getX()), Mth.lerp((double)pt, victim.yo, victim.getY()) + (double)victim.getBbHeight() * 0.62D - dip(victim, age), Mth.lerp((double)pt, victim.zo, victim.getZ()));
         Vec3 hand = ripHand(level, msg, pt);
         if (hand != null) {
            double out = smooth(age / 8.0D);
            double back = smooth((age - 34.0D) / (double)Math.max(1, msg.life - 34));
            double reach = out * (1.0D - back);
            Vec3 d = chest.subtract(hand);
            double len = d.length();
            if (!(reach < 0.02D) && !(len < 0.3D) && !(len > 40.0D) && !(chest.distanceToSqr(cam) > 9216.0D)) {
               Vec3 dir = d.scale(1.0D / len);
               Vec3 tip = hand.add(d.scale(reach));
               Vec3 side = dir.cross(cam.subtract(hand.add(tip).scale(0.5D)));
               if (side.lengthSqr() < 1.0E-6D) {
                  side = dir.cross(new Vec3(0.0D, 1.0D, 0.0D));
               }

               if (side.lengthSqr() < 1.0E-6D) {
                  side = new Vec3(1.0D, 0.0D, 0.0D);
               }

               side = side.normalize();
               float a = (float)(0.9D * (1.0D - back * 0.5D));
               double step = 0.13D;
               int n = (int)(len * reach / step);
               Vec3 o = side.scale(0.032D);

               for(int i = 0; i < n; ++i) {
                  Vec3 p0 = hand.add(dir.scale((double)i * step + 0.01D));
                  Vec3 p1 = hand.add(dir.scale((double)(i + 1) * step - 0.01D));
                  if (i % 2 == 0) {
                     ClientVertigo.line(vc, m, cam, p0.add(o), p1.add(o), 0.016D, CHAIN, a);
                     ClientVertigo.line(vc, m, cam, p0.subtract(o), p1.subtract(o), 0.016D, CHAIN, a);
                  } else {
                     ClientVertigo.line(vc, m, cam, p0.subtract(dir.scale(0.03D)), p1.add(dir.scale(0.03D)), 0.022D, CHAIN_LIT, a);
                  }
               }

               if (age >= 8.0D && back < 0.5D) {
                  Vec3 up = side.cross(dir).normalize();
                  ClientVertigo.line(vc, m, cam, tip, tip.subtract(dir.scale(0.12D)).add(up.scale(0.08D)), 0.02D, CHAIN_LIT, a);
                  ClientVertigo.line(vc, m, cam, tip, tip.subtract(dir.scale(0.12D)).subtract(up.scale(0.08D)), 0.02D, CHAIN_LIT, a);
               }

            }
         }
      }
   }

   private static void roller(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientDmLooks.Frame f, double w, boolean low, float a) {
      double half = 0.49D;
      double w0 = low ? w - 0.035D : w - 0.01D;
      double w1 = low ? w + 0.01D : w + 0.035D;
      double l0 = low ? w0 - 0.015D : w1;
      double l1 = low ? w0 : w1 + 0.015D;
      rect(vc, m, cam, f, -half, w0, half, w1, WOOD, WOOD, a);
      rect(vc, m, cam, f, -half, l0, half, l1, WOOD_LIT, WOOD_LIT, a);
      double b0 = Math.min(w0, l0) - 0.01D;
      double b1 = Math.max(w1, l1) + 0.01D;
      rect(vc, m, cam, f, half, b0, half + 0.06D, b1, BRASS, BRASS, a);
      rect(vc, m, cam, f, -half - 0.06D, b0, -half, b1, BRASS, BRASS, a);
   }

   private static void words(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientDmLooks.Frame f, double w, double prog, int seed, double th, float[] col, float a) {
      double x0 = -0.36D;
      double x1 = 0.36D - 0.12D * ClientVertigo.hash(seed);
      double end = x0 + (x1 - x0) * prog;
      double pos = x0;

      for(int k = 0; k < 24 && pos < end; ++k) {
         double len = 0.04D + 0.09D * ClientVertigo.hash(seed * 7 + k);
         double stop = Math.min(Math.min(pos + len, x1), end);
         if (stop > pos) {
            rect(vc, m, cam, f, pos, w - th * 0.5D, stop, w + th * 0.5D, col, col, a);
         }

         pos += len + 0.03D;
      }

   }

   private static void signature(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientDmLooks.Frame f, double prog, int seed, float[] col, float a) {
      Random r = new Random((long)seed * 31L + 7L);
      double phase = r.nextDouble() * 6.28D;
      double amp = 0.022D + r.nextDouble() * 0.016D;
      double freq = 18.0D + r.nextDouble() * 10.0D;
      double x0 = -0.35D;
      double x1 = 0.13D;
      int n = 36;
      int shown = (int)Math.ceil((double)n * prog);
      double pu = x0;
      double pw = -0.31D + Math.sin(phase) * amp + Math.sin(phase * 2.0D) * amp * 0.35D;

      for(int i = 1; i <= shown; ++i) {
         double t = (double)i / (double)n;
         double u = x0 + (x1 - x0) * t;
         double fall = 1.0D - t * 0.6D;
         double w = -0.31D + (Math.sin(t * freq + phase) * amp + Math.sin(t * freq * 2.7D + phase * 2.0D) * amp * 0.35D) * fall;
         seg(vc, m, cam, f, pu, pw, u, w, 0.012D, col, a);
         pu = u;
         pw = w;
      }

   }

   private static void rect(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientDmLooks.Frame f, double u0, double w0, double u1, double w1, float[] low, float[] high, float a) {
      Vec3 p0 = at(f, u0, w0);
      Vec3 p1 = at(f, u1, w0);
      Vec3 p2 = at(f, u1, w1);
      Vec3 p3 = at(f, u0, w1);
      ClientVertigo.vtx(vc, m, low, p0.x - cam.x, p0.y - cam.y, p0.z - cam.z, 0.0F, 1.0F, a);
      ClientVertigo.vtx(vc, m, low, p1.x - cam.x, p1.y - cam.y, p1.z - cam.z, 1.0F, 1.0F, a);
      ClientVertigo.vtx(vc, m, high, p2.x - cam.x, p2.y - cam.y, p2.z - cam.z, 1.0F, 0.0F, a);
      ClientVertigo.vtx(vc, m, high, p3.x - cam.x, p3.y - cam.y, p3.z - cam.z, 0.0F, 0.0F, a);
   }

   private static void seg(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientDmLooks.Frame f, double u0, double w0, double u1, double w1, double th, float[] col, float a) {
      double du = u1 - u0;
      double dw = w1 - w0;
      double len = Math.sqrt(du * du + dw * dw);
      if (!(len < 1.0E-6D)) {
         double nu = -dw / len * th * 0.5D;
         double nw = du / len * th * 0.5D;
         Vec3 p0 = at(f, u0 + nu, w0 + nw);
         Vec3 p1 = at(f, u1 + nu, w1 + nw);
         Vec3 p2 = at(f, u1 - nu, w1 - nw);
         Vec3 p3 = at(f, u0 - nu, w0 - nw);
         ClientVertigo.quad(vc, m, col, p0.x - cam.x, p0.y - cam.y, p0.z - cam.z, p1.x - cam.x, p1.y - cam.y, p1.z - cam.z, p2.x - cam.x, p2.y - cam.y, p2.z - cam.z, p3.x - cam.x, p3.y - cam.y, p3.z - cam.z, a, a, a, a);
      }
   }

   private static void disc(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientDmLooks.Frame f, double cu, double cw, double r0, double r1, float[] col, float a, int n) {
      for(int i = 0; i < n; ++i) {
         double a0 = (Math.PI * 2D) * (double)i / (double)n;
         double a1 = (Math.PI * 2D) * (double)(i + 1) / (double)n;
         Vec3 p0 = at(f, cu + Math.cos(a0) * r0, cw + Math.sin(a0) * r0);
         Vec3 p1 = at(f, cu + Math.cos(a1) * r0, cw + Math.sin(a1) * r0);
         Vec3 p2 = at(f, cu + Math.cos(a1) * r1, cw + Math.sin(a1) * r1);
         Vec3 p3 = at(f, cu + Math.cos(a0) * r1, cw + Math.sin(a0) * r1);
         ClientVertigo.quad(vc, m, col, p0.x - cam.x, p0.y - cam.y, p0.z - cam.z, p1.x - cam.x, p1.y - cam.y, p1.z - cam.z, p2.x - cam.x, p2.y - cam.y, p2.z - cam.z, p3.x - cam.x, p3.y - cam.y, p3.z - cam.z, a, a, a, a);
      }

   }

   private static void bezier(VertexConsumer vc, Matrix4f m, Vec3 cam, Vec3 a, Vec3 b, double s0, double s1, double width, float[] col, float alpha) {
      Vec3 mid = a.add(b).scale(0.5D).add(0.0D, -Math.min(0.35D, a.distanceTo(b) * 0.12D), 0.0D);
      int n = 12;
      Vec3 prev = curve(a, mid, b, s0);

      for(int i = 1; i <= n; ++i) {
         Vec3 p = curve(a, mid, b, s0 + (s1 - s0) * (double)i / (double)n);
         ClientVertigo.line(vc, m, cam, prev, p, width, col, alpha);
         prev = p;
      }

   }

   private static Vec3 curve(Vec3 a, Vec3 c, Vec3 b, double t) {
      double u = 1.0D - t;
      return a.scale(u * u).add(c.scale(2.0D * u * t)).add(b.scale(t * t));
   }

   private static float[] rgb(int c) {
      return new float[]{(float)(c >> 16 & 255) / 255.0F, (float)(c >> 8 & 255) / 255.0F, (float)(c & 255) / 255.0F};
   }

   private static double smooth(double t) {
      double k = Mth.clamp(t, 0.0D, 1.0D);
      return k * k * (3.0D - 2.0D * k);
   }

   public static void clearAll() {
      LOOKS.clear();
      RIPS.clear();
      BORROWED.clear();
   }

   private static final class Frame {
      Vec3 c;
      Vec3 right;
      double s;
      double top;
      double bot;
      float a;
   }

   private static final class Look {
      final OriginLookPacket msg;
      final long born;

      Look(OriginLookPacket msg, long born) {
         this.msg = msg;
         this.born = born;
      }
   }
}
