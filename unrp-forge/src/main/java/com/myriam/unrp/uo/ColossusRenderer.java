package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public class ColossusRenderer {
   private static final ResourceLocation UMBER = new ResourceLocation("unrp", "textures/models/colossus.png");
   private static final ResourceLocation GILDED = new ResourceLocation("unrp", "textures/models/colossus_gilded.png");
   private static final ResourceLocation UMBER_GLOW = new ResourceLocation("unrp", "textures/models/colossus_glow.png");
   private static final ResourceLocation GILDED_GLOW = new ResourceLocation("unrp", "textures/models/colossus_gilded_glow.png");
   private static final int FULL_BRIGHT = 15728880;
   private static final float FIST_SCALE = 0.56F;
   private static ColossusModel model;

   private ColossusRenderer() {
   }

   public static void bake(EntityModelSet models) {
      model = new ColossusModel(models.bakeLayer(OriginModels.APE_SHAPE));
   }

   public static boolean wearing(Player player) {
      return shown(player) != 0;
   }

   public static int shown(Player player) {
      if (ClientLbFormCache.progress(player.getUUID()) >= 0.35F) {
         int next = ClientLbFormCache.target(player.getUUID());
         return LimitBreakerForms.apeForm(next) ? next : 0;
      } else {
         int form = ClientLbFormCache.form(player.getUUID());
         return LimitBreakerForms.apeForm(form) ? form : 0;
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void before(RenderPlayerEvent.Pre event) {
      Player form_0 = event.getEntity();
      if (form_0 instanceof AbstractClientPlayer player) {
         if (!player.isSpectator() && !player.isInvisible() && model != null) {
            int form = shown(player);
            if (form != 0) {
               event.setCanceled(true);
               draw(player, form, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick());
            }
         }
      }
   }

   private static void draw(AbstractClientPlayer player, int form, PoseStack pose, MultiBufferSource buffers, int light, float partial) {
      boolean gilded = form == 7;
      ColossusState.Drive d = ColossusState.drive(player, partial);
      model.rest();
      model.drive(d);
      float body = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
      float scale = Math.max(0.25F, player.getBbWidth() / 0.6F);
      pose.pushPose();
      if (player.isCrouching()) {
         pose.translate(0.0F, 0.125F / scale, 0.0F);
      }

      pose.mulPose(Axis.YP.rotationDegrees(180.0F - body));
      if (player.deathTime > 0) {
         float fall = Mth.sqrt(((float)player.deathTime + partial - 1.0F) / 20.0F * 1.6F);
         pose.mulPose(Axis.ZP.rotationDegrees(Math.min(1.0F, fall) * 90.0F));
      }

      pose.scale(-1.0F, -1.0F, 1.0F);
      pose.scale(0.9375F, 0.9375F, 0.9375F);
      pose.translate(0.0F, -1.501F, 0.0F);
      pose.translate(0.0F, 1.5F, 0.0F);
      pose.scale(0.62F, 0.62F, 0.62F);
      pose.translate(0.0F, -1.5F, 0.0F);
      int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0F);
      VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(gilded ? GILDED : UMBER));
      model.renderToBuffer(pose, vc, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
      float t = (float)player.tickCount + partial;
      float pulse = gilded ? 0.78F + 0.22F * Mth.sin(t * 0.09F) : 0.84F + 0.16F * Mth.sin(t * 0.11F);
      float rage = 1.0F + 0.25F * d.roar + 0.15F * d.hurt;
      float glow = Math.min(1.0F, pulse * rage);
      VertexConsumer g = buffers.getBuffer(RenderType.eyes(gilded ? GILDED_GLOW : UMBER_GLOW));
      model.renderToBuffer(pose, g, 15728880, OverlayTexture.NO_OVERLAY, glow, glow, glow, 1.0F);
      pose.popPose();
   }

   @SubscribeEvent
   public static void firstPerson(RenderArmEvent event) {
      AbstractClientPlayer player = event.getPlayer();
      if (player != null && model != null) {
         int form = shown(player);
         if (form != 0) {
            event.setCanceled(true);
            boolean right = event.getArm() == HumanoidArm.RIGHT;
            boolean gilded = form == 7;
            model.rest();
            ModelPart fore = right ? model.forearmR : model.forearmL;
            fore.x = 0.0F;
            fore.y = 0.0F;
            fore.z = 0.0F;
            fore.xRot = 0.0F;
            fore.yRot = 0.0F;
            fore.zRot = 0.0F;
            PoseStack pose = event.getPoseStack();
            pose.pushPose();
            pose.translate((right ? -6.0F : 6.0F) / 16.0F, 0.0625F, 0.0F);
            pose.scale(0.56F, 0.56F, 0.56F);
            VertexConsumer vc = event.getMultiBufferSource().getBuffer(RenderType.entityCutoutNoCull(gilded ? GILDED : UMBER));
            fore.render(pose, vc, event.getPackedLight(), OverlayTexture.NO_OVERLAY);
            pose.popPose();
            model.rest();
         }
      }
   }

   @SubscribeEvent
   public static void tick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && !mc.isPaused()) {
            for(AbstractClientPlayer player : mc.level.players()) {
               int form = shown(player);
               if (form != 0 && !player.isSpectator()) {
                  ColossusState.tick(player, form == 7);
               } else {
                  ColossusState.forget(player);
               }
            }

         }
      }
   }
}
