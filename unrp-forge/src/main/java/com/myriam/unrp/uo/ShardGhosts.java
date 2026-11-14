package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayDeque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class ShardGhosts {
   private static final int MOST = 24;
   private static final double SEEN = 64.0D;
   private static final ArrayDeque<ShardGhosts.Ghost> GHOSTS = new ArrayDeque();
   private static final ShardDraw.Look LOOK = new ShardDraw.Look();
   private static ClientLevel where;

   private ShardGhosts() {
   }

   public static void shatter(ParticleConstructEntity c) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && c.level() == mc.level) {
         if (mc.level != where) {
            where = mc.level;
            GHOSTS.clear();
         }

         if (!(c.distanceToSqr(mc.player) > 4096.0D)) {
            ShardGhosts.Ghost g = new ShardGhosts.Ghost();
            g.variant = Math.min(3, Math.max(0, c.getVariant()));
            g.x = c.getX();
            g.y = c.getY();
            g.z = c.getZ();
            g.yaw = c.getYRot();
            g.color = ParticleConstructRenderer.colorOf(c);
            g.awake = c.isAwake();
            g.age = (float)c.tickCount;
            g.start = mc.level.getGameTime();
            g.tex = ParticleConstructRenderer.stageTexture(c.getVariant(), c.hpFraction());
            GHOSTS.add(g);

            while(GHOSTS.size() > 24) {
               GHOSTS.poll();
            }

         }
      }
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         if (!GHOSTS.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level != null && level == where) {
               long now = level.getGameTime();
               GHOSTS.removeIf((gx) -> now - gx.start >= 16L || now < gx.start - 200L);
               if (!GHOSTS.isEmpty()) {
                  float pt = event.getPartialTick();
                  Vec3 cam = event.getCamera().getPosition();
                  PoseStack pose = event.getPoseStack();
                  MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

                  try {
                     for(ShardGhosts.Ghost g : GHOSTS) {
                        float since = (float)(now - g.start) + pt;
                        float e = Math.max(0.001F, since / 16.0F);
                        if (!(e >= 1.0F)) {
                           ShardDraw.Look look = LOOK.begin(g.age + since, g.awake, g.color);
                           look.explode = e;
                           pose.pushPose();

                           try {
                              pose.translate(g.x - cam.x, g.y - cam.y, g.z - cam.z);
                              pose.mulPose(Axis.YP.rotationDegrees(-g.yaw));
                              ShardDraw.draw(ShardDraw.CONSTRUCTS[g.variant], look, pose, buffers, g.tex);
                           } finally {
                              pose.popPose();
                           }
                        }
                     }

                     buffers.endLastBatch();
                  } catch (Throwable var18) {
                     GHOSTS.clear();
                  }

               }
            } else {
               GHOSTS.clear();
            }
         }
      }
   }

   static final class Ghost {
      int variant;
      double x;
      double y;
      double z;
      float yaw;
      int color;
      boolean awake;
      float age;
      long start;
      ResourceLocation tex;
   }
}
