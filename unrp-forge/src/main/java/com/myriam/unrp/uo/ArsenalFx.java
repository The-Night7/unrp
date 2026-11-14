package com.myriam.unrp.uo;

import com.myriam.unrp.anim.MoveAnimClient;
import com.myriam.unrp.anim.MoveClipIds;


import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class ArsenalFx {
   private static final double SEEN = 64.0D;
   private static final double GATE_SEEN = 96.0D;
   public static final float HAND_GATE = 0.24F;
   public static final float HAND_GATE_NEAR = 0.15F;
   public static final float POINT_GATE = 0.11F;
   private static int aim = -2;
   private static int forge = -2;
   private static int recall = -2;
   private static int shatter = -2;
   private static int command = -2;

   private ArsenalFx() {
   }

   private static void ids() {
      if (aim == -2) {
         aim = MoveClipIds.id("ad_aim");
         forge = MoveClipIds.id("ad_forge");
         recall = MoveClipIds.id("ad_recall");
         shatter = MoveClipIds.id("ad_shatter");
         command = MoveClipIds.id("ad_command");
      }

   }

   public static int tierAt(double held) {
      return held >= 40.0D ? 3 : (held >= 16.0D ? 2 : 1);
   }

   public static double handGate(double t) {
      if (!(t < 0.0D) && !(t > 10.0D)) {
         double open = Math.min(1.0D, t / 2.0D);
         double shut = t > 6.0D ? (t - 6.0D) / 4.0D : 0.0D;
         return open * (1.0D - shut * shut);
      } else {
         return 0.0D;
      }
   }

   public static double pointGate(double t) {
      return t < 7.0D ? 0.0D : Math.min(1.0D, (t - 7.0D) / 2.0D);
   }

   private static double aimTime(int id, double age) {
      return MoveAnimClient.holding(id, aim) ? MoveAnimClient.clipTime(id, false, aim, age) : -1.0D;
   }

   private static double commandTime(int id, double age) {
      return MoveAnimClient.holding(id, command) ? MoveAnimClient.clipTime(id, false, command, age) : -1.0D;
   }

   private static double handTime(int id, double age) {
      return Math.max(MoveAnimClient.clipTime(id, true, forge, age), Math.max(MoveAnimClient.clipTime(id, true, recall, age), MoveAnimClient.clipTime(id, true, shatter, age)));
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         Minecraft mc = Minecraft.getInstance();
         ClientLevel level = mc.level;
         if (level != null) {
            ids();
            Camera camera = event.getCamera();
            Vec3 cam = camera.getPosition();
            Vector3f l = camera.getLeftVector();
            Vector3f u = camera.getUpVector();
            Vec3 camRight = new Vec3((double)(-l.x()), (double)(-l.y()), (double)(-l.z()));
            Vec3 camUp = new Vec3((double)u.x(), (double)u.y(), (double)u.z());
            float pt = event.getPartialTick();
            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            Matrix4f m = event.getPoseStack().last().pose();
            boolean drew = false;

            try {
               for(Entity e : level.entitiesForRendering()) {
                  if (e instanceof ArsenalGateEntity) {
                     ArsenalGateEntity g = (ArsenalGateEntity)e;
                     if (g.distanceToSqr(cam) < 9216.0D) {
                        ArsenalDraw.gateEntity(buffers, m, g.getPosition(pt).subtract(cam), ArsenalGateEntity.direction(g.yaw(pt), g.pitch(pt)), g.radius(), g.kind(), (float)g.tickCount + pt, (float)g.fireAt(), Vec3.ZERO, camRight, camUp);
                        drew = true;
                     }
                  }
               }

               if (aim >= 0) {
                  for(AbstractClientPlayer p : level.players()) {
                     if (!p.isInvisible() && !p.isSpectator() && p.distanceToSqr(cam) <= 4096.0D && hands(p, pt, buffers, m, cam, camRight, camUp)) {
                        drew = true;
                     }
                  }
               }
            } finally {
               if (drew) {
                  buffers.endBatch(ArsenalDraw.soft());
                  buffers.endBatch(ArsenalDraw.add());
               }

            }

         }
      }
   }

   private static boolean hands(AbstractClientPlayer p, float pt, MultiBufferSource buffers, Matrix4f m, Vec3 cam, Vec3 camRight, Vec3 camUp) {
      double age = (double)((float)p.tickCount + pt);
      int id = p.getId();
      double held = aimTime(id, age);
      double cmd = commandTime(id, age);
      double hand = handTime(id, age);
      if (held < 0.0D && cmd < 0.0D && hand < 0.0D) {
         return false;
      } else {
         Vec3 eye = p.getEyePosition(pt);
         Vec3 look = p.getViewVector(pt);
         Vec3[] b = ArsenalDraw.basis(look);
         HandCapture.Hands h = HandCapture.get(id, age);
         boolean real = h != null;
         Vec3 grip = real ? h.right : eye.add(b[0].scale(0.31D)).add(0.0D, -0.27D, 0.0D).add(look.scale(0.72D));
         Vec3 along = real ? h.rightDir : look;
         boolean drew = false;
         if (held >= 0.0D) {
            int tier = tierAt(held);
            double open = Math.min(1.0D, held / 3.0D);
            Vec3 tip_0 = grip.add(look.scale(real ? 0.16D : 0.22D)).subtract(cam);
            ArsenalDraw.gate(buffers, m, tip_0, look, (float)((0.1D + 0.05D * (double)tier) * open), (float)age, tier >= 3 ? 0.35F : 0.0F, (float)open, camRight, camUp);
            drew = true;
         }

         double pg = cmd >= 0.0D ? pointGate(cmd) : 0.0D;
         if (pg > 0.0D) {
            Vec3 tip = grip.add(along.scale(0.14D)).subtract(cam);
            ArsenalDraw.gate(buffers, m, tip, look, (float)((double)0.11F * pg), (float)age, cmd < 9.0D ? (float)(1.0D - Math.abs(cmd - 8.0D)) : 0.0F, (float)(0.85D * pg), camRight, camUp);
            drew = true;
         }

         double g = handGate(hand);
         if (g > 0.0D) {
            Vec3 at = grip.add(along.scale(real ? 0.1D : 0.08D)).subtract(cam);
            ArsenalDraw.gate(buffers, m, at, along, (float)((double)(real ? 0.24F : 0.15F) * g), (float)age, hand < 3.5D ? (float)(1.0D - Math.abs(hand - 3.0D) / 3.0D) : 0.0F, (float)g, camRight, camUp);
            drew = true;
         }

         return drew;
      }
   }
}
