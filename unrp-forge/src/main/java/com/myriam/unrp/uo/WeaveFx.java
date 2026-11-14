package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
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
public final class WeaveFx {
   private static final double SEEN = 64.0D;
   private static final Map<Integer, WeaveFx.Woven> WOVEN = new HashMap();

   private WeaveFx() {
   }

   public static void woven(int key, Matrix4f itemToView, int rgb, float age, float intro) {
      WOVEN.put(key, new WeaveFx.Woven(itemToView, rgb, age, intro));
   }

   public static void drawWoven(VertexConsumer add, Matrix4f itemToView, int rgb, float age, float intro) {
      Matrix4f inv = (new Matrix4f(itemToView)).invert();
      Vector3f eye = inv.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
      Vector3f r = inv.transformDirection(new Vector3f(1.0F, 0.0F, 0.0F)).normalize();
      Vector3f u = inv.transformDirection(new Vector3f(0.0F, 1.0F, 0.0F)).normalize();
      WeaveDraw.blade(add, itemToView, new Vec3((double)eye.x(), (double)eye.y(), (double)eye.z()), new Vec3((double)r.x(), (double)r.y(), (double)r.z()), new Vec3((double)u.x(), (double)u.y(), (double)u.z()), age, rgb, intro);
   }

   public static boolean hidden(Entity e) {
      if (!e.isInvisible()) {
         if (e instanceof Player) {
            Player p = (Player)e;
            if (p.isSpectator()) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public static Vec3 chest(Entity e, float pt) {
      return e.getPosition(pt).add(0.0D, (double)e.getBbHeight() * 0.6D, 0.0D);
   }

   public static Vec3 palm(Entity e, float pt) {
      if (e instanceof AbstractClientPlayer p) {
         HandCapture.Hands h = HandCapture.get(p.getId(), (double)((float)p.tickCount + pt));
         if (h != null) {
            return h.right.add(h.rightDir.scale(0.08D));
         }

         if (p == Minecraft.getInstance().player) {
            Vec3 look = p.getViewVector(pt);
            Vec3 right = ArsenalDraw.basis(look)[0];
            return p.getEyePosition(pt).add(right.scale(0.31D)).add(0.0D, -0.27D, 0.0D).add(look.scale(0.72D));
         }
      }

      return chest(e, pt);
   }

   public static float phase(int id) {
      return (float)Math.floorMod(id, 7) * 0.9F;
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_SKY) {
         WOVEN.clear();
      } else if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         Minecraft mc = Minecraft.getInstance();
         ClientLevel level = mc.level;
         if (level != null) {
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
               for(WeaveFx.Woven w : WOVEN.values()) {
                  drawWoven(buffers.getBuffer(WeaveDraw.add()), w.pose, w.rgb, w.age, w.intro);
                  drew = true;
               }

               for(Entity e : level.entitiesForRendering()) {
                  if (e instanceof AuraThreadEntity) {
                     AuraThreadEntity t = (AuraThreadEntity)e;
                     if (t.distanceToSqr(cam) < 4096.0D && thread(t, level, buffers.getBuffer(WeaveDraw.add()), m, cam, camRight, camUp, pt)) {
                        drew = true;
                     }
                  }
               }
            } finally {
               WOVEN.clear();
               if (drew) {
                  buffers.endBatch(WeaveDraw.add());
               }

            }

         }
      }
   }

   private static boolean thread(AuraThreadEntity t, ClientLevel level, VertexConsumer add, Matrix4f m, Vec3 cam, Vec3 camRight, Vec3 camUp, float pt) {
      Entity src = level.getEntity(t.source());
      Entity dst = level.getEntity(t.sink());
      if (dst != null && !hidden(dst)) {
         float age = (float)t.tickCount + pt;
         int rgb = t.color();
         switch (t.kind()) {
            case 0:
               if (src == null || hidden(src)) {
                  return false;
               }

               WeaveDraw.siphon(add, m, chest(src, pt).subtract(cam), palm(dst, pt).subtract(cam), Vec3.ZERO, age, rgb, camRight, camUp);
               break;
            case 1:
               if (src == null || hidden(src)) {
                  return false;
               }

               WeaveDraw.bestow(add, m, palm(src, pt).subtract(cam), chest(dst, pt).subtract(cam), Vec3.ZERO, age, rgb, camRight, camUp);
               break;
            case 2:
               boolean link = src != null && src != dst && !hidden(src);
               Vec3 to = chest(dst, pt).subtract(cam);
               WeaveDraw.tether(add, m, link ? chest(src, pt).subtract(cam) : to, to, Vec3.ZERO, age, rgb, camRight, camUp, phase(t.getId()), link);
               break;
            case 3:
               WeaveDraw.unravel(add, m, chest(dst, pt).subtract(cam), age, rgb, camRight, camUp);
               break;
            default:
               return false;
         }

         return true;
      } else {
         return false;
      }
   }

   @EventBusSubscriber(
      modid = "unrp",
      bus = Bus.MOD,
      value = {Dist.CLIENT}
   )
   public static final class Registry {
      private Registry() {
      }

      @SubscribeEvent
      public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
         event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.AURA_THREAD.get(), NoopRenderer::new);
      }
   }

   private static final class Woven {
      final Matrix4f pose;
      final int rgb;
      final float age;
      final float intro;

      Woven(Matrix4f pose, int rgb, float age, float intro) {
         this.pose = pose;
         this.rgb = rgb;
         this.age = age;
         this.intro = intro;
      }
   }
}
