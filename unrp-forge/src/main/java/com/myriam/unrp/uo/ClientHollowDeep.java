package com.myriam.unrp.uo;

import com.myriam.unrp.pass.UnrpPowerLayers;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class ClientHollowDeep {
   private static final ResourceLocation STONE_SKIN = new ResourceLocation("unrp", "textures/entity/hm_stone.png");
   private static final float STONE_ALPHA = 0.9F;
   private static final int STONE_IN = 6;
   private static final int STONE_OUT = 10;
   private static final float EYES_WASH = 0.2F;
   private static final int EYES_FADE = 6;
   private static final Map<Integer, long[]> STONES = new HashMap();
   private static final Map<Integer, long[]> HELD = new HashMap();
   private static final float HELD_ALPHA = 0.5F;
   private static ClientLevel where;
   private static boolean inWorld;
   private static int eyeEntity = -1;
   private static long eyeFrom;
   private static long eyeUntil;
   private static boolean borrowed;

   private ClientHollowDeep() {
   }

   public static void accept(OriginLookPacket msg, long now) {
      if (msg.kind == 17) {
         if (msg.life <= 0) {
            STONES.remove(msg.entity);
         } else {
            long[] had = (long[])STONES.get(msg.entity);
            STONES.put(msg.entity, new long[]{now + (long)msg.life, had != null && had[0] > now ? had[1] : now});
         }
      } else if (msg.kind == 19) {
         if (msg.life <= 0) {
            HELD.remove(msg.entity);
         } else {
            long[] had = (long[])HELD.get(msg.entity);
            HELD.put(msg.entity, new long[]{now + (long)msg.life, had != null && had[0] > now ? had[1] : now});
         }
      } else if (msg.kind == 18) {
         if (msg.life > 0 && msg.entity >= 0) {
            eyeEntity = msg.entity;
            eyeFrom = now;
            eyeUntil = now + (long)msg.life;
         } else {
            eyeUntil = Math.min(eyeUntil, now);
         }
      }

   }

   public static boolean stone(int entityId) {
      return STONES.containsKey(entityId);
   }

   public static float wash(double now) {
      if (eyeEntity >= 0 && borrowed) {
         double in = Math.min(1.0D, (now - (double)eyeFrom) / 6.0D);
         double out = Math.min(1.0D, Math.max(0.0D, ((double)eyeUntil - now) / 6.0D));
         return (float)((double)0.2F * Math.max(0.0D, Math.min(in, out)));
      } else {
         return 0.0F;
      }
   }

   private static void endEyes(Minecraft mc) {
      eyeEntity = -1;
      if (borrowed && mc.player != null && mc.getCameraEntity() != mc.player) {
         mc.setCameraEntity(mc.player);
      }

      borrowed = false;
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.player != null) {
            if (mc.level != where) {
               endEyes(mc);
               STONES.clear();
               HELD.clear();
               where = mc.level;
            }

            long now = mc.level.getGameTime();
            if (eyeEntity >= 0) {
               Entity seen = mc.level.getEntity(eyeEntity);
               if (now < eyeUntil && seen != null && seen.isAlive() && !mc.player.isDeadOrDying()) {
                  if (mc.getCameraEntity() != seen) {
                     mc.setCameraEntity(seen);
                     borrowed = true;
                  }
               } else {
                  endEyes(mc);
               }
            }

            if (!STONES.isEmpty() || !HELD.isEmpty()) {
               STONES.values().removeIf((s) -> s[0] <= now);
               HELD.values().removeIf((s) -> s[0] <= now);

               for(Map<Integer, long[]> still : List.of(STONES, HELD)) {
                  for(int id : still.keySet()) {
                     Entity var9 = mc.level.getEntity(id);
                     if (var9 instanceof LivingEntity) {
                        LivingEntity live = (LivingEntity)var9;
                        live.walkAnimation.update(0.0F, 1.0F);
                        live.walkAnimation.update(0.0F, 1.0F);
                     }
                  }
               }

            }
         } else {
            STONES.clear();
            HELD.clear();
            eyeEntity = -1;
            borrowed = false;
            where = null;
         }
      }
   }

   @SubscribeEvent
   public static void onStage(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_SKY) {
         inWorld = true;
      } else if (event.getStage() == Stage.AFTER_ENTITIES || event.getStage() == Stage.AFTER_LEVEL) {
         inWorld = false;
      }

   }

   private static boolean odd(LivingEntity e) {
      ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
      if (key == null) {
         return true;
      } else {
         String id = key.toString();
         return id.equals("minecraft:slime") || id.equals("minecraft:magma_cube") || id.equals("minecraft:ghast") || id.equals("minecraft:phantom") || id.equals("minecraft:giant") || id.equals("minecraft:pufferfish") || id.equals("minecraft:elder_guardian") || id.equals("minecraft:wither") || id.equals("minecraft:ender_dragon") || id.equals("minecraft:shulker");
      }
   }

   private static float scaleOf(LivingEntity e) {
      if (!(e instanceof Player) && !(e instanceof Witch) && !(e instanceof AbstractIllager) && !(e instanceof HollowDoppelEntity)) {
         ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
         String id = key == null ? "" : key.toString();
         if (id.equals("minecraft:cave_spider")) {
            return 0.7F;
         } else if (!id.equals("minecraft:wither_skeleton") && !id.equals("minecraft:polar_bear")) {
            if (id.equals("minecraft:husk")) {
               return 1.0625F;
            } else if (e instanceof AbstractVillager) {
               return e.isBaby() ? 0.46875F : 0.9375F;
            } else {
               return 1.0F;
            }
         } else {
            return 1.2F;
         }
      } else {
         return 0.9375F;
      }
   }

   @SubscribeEvent
   public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
      if (inWorld && (!STONES.isEmpty() || !HELD.isEmpty())) {
         LivingEntity e = event.getEntity();
         long[] s = (long[])STONES.get(e.getId());
         boolean held = false;
         if (s == null) {
            s = (long[])HELD.get(e.getId());
            held = s != null;
         }

         if (s != null && !e.isInvisible() && e.deathTime <= 0 && e.level() == where && !odd(e)) {
            try {
               float pt = event.getPartialTick();
               double now = (double)e.level().getGameTime() + (double)pt;
               double in = Math.min(1.0D, (now - (double)s[1]) / 6.0D);
               double out = Math.min(1.0D, Math.max(0.0D, ((double)s[0] - now) / 10.0D));
               float a = (float)((double)(held ? 0.5F : 0.9F) * Math.max(0.0D, Math.min(in, out)));
               if (a <= 0.02F) {
                  return;
               }

               EntityModel<?> model = event.getRenderer().getModel();
               PoseStack ps = event.getPoseStack();
               ps.pushPose();

               try {
                  ps.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(pt, e.yBodyRotO, e.yBodyRot)));
                  float scale = scaleOf(e) * 1.01F;
                  ps.scale(-scale, -scale, scale);
                  ps.translate(0.0D, -1.501D, 0.0D);
                  if (held) {
                     model.renderToBuffer(ps, event.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(UnrpPowerLayers.WHITE, false)), event.getPackedLight(), OverlayTexture.NO_OVERLAY, 0.55F, 0.66F, 0.68F, a);
                  } else {
                     model.renderToBuffer(ps, event.getMultiBufferSource().getBuffer(RenderType.entityTranslucent(STONE_SKIN, false)), event.getPackedLight(), OverlayTexture.NO_OVERLAY, 0.86F, 0.87F, 0.88F, a);
                  }
               } finally {
                  ps.popPose();
               }
            } catch (Throwable var19) {
               STONES.clear();
               HELD.clear();
            }

         }
      }
   }
}
