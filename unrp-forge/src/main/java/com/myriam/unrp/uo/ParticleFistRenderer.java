package com.myriam.unrp.uo;

import com.myriam.unrp.look.ClientSettingsCache;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.UUID;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class ParticleFistRenderer extends EntityRenderer<ParticleFistEntity> {
   private static final ResourceLocation TEX = new ResourceLocation("unrp", "textures/entity/particle_construct_hand_0.png");
   private static final float[] KNUCKLES = new float[]{-0.243F, -0.081F, 0.081F, 0.243F};
   private static final ShardDraw.Look LOOK = new ShardDraw.Look();

   public ParticleFistRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(ParticleFistEntity entity) {
      return TEX;
   }

   public void render(ParticleFistEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      super.render(entity, yaw, partialTick, pose, buffers, packedLight);
      int main = 15160448;
      UUID owner = entity.getOwnerId();
      if (owner != null) {
         if (ClientSettingsCache.isAM(owner)) {
            main = 3026486;
         } else {
            int stored = ClientSettingsCache.getColor(owner);
            if (stored != -1) {
               main = stored;
            }
         }
      }

      int cr = Math.min(255, (main >> 16 & 255) + 40);
      int cg = Math.min(255, (main >> 8 & 255) + 40);
      int cb = Math.min(255, (main & 255) + 40);
      float size = entity.getFistSize();
      Vec3 vel = entity.getDeltaMovement();
      Vec3 face = vel.lengthSqr() > 0.01D ? vel : new Vec3(0.0D, -1.0D, 0.0D);
      float ry = (float)(Mth.atan2(face.x, face.z) * (180D / Math.PI));
      float rp = (float)(Mth.atan2(face.y, Math.sqrt(face.x * face.x + face.z * face.z)) * (180D / Math.PI));
      float t = (float)entity.tickCount + partialTick;
      float pump = 1.0F + 0.06F * Mth.sin(t * 0.9F);
      float len = Math.min(3.0F, (float)vel.length() * 1.6F) / size;
      ShardDraw.Look look = LOOK.begin(t, true, cr << 16 | cg << 8 | cb).grow(pump * size / ShardDraw.FIST.scale());

      for(int k = 0; k < 4; ++k) {
         look.streak(KNUCKLES[k], 0.144F, 0.264F, len * 1.6F, 0.04F, 0.38F);
      }

      look.streak(-0.396F, -0.048F, 0.0F, len * 1.3F, 0.04F, 0.26F);
      look.streak(0.3F, 0.0F, -0.12F, len * 1.3F, 0.04F, 0.26F);
      pose.pushPose();

      try {
         pose.mulPose(Axis.YP.rotationDegrees(ry));
         pose.mulPose(Axis.XP.rotationDegrees(-rp));
         ShardDraw.draw(ShardDraw.FIST, look, pose, buffers, TEX);
      } finally {
         pose.popPose();
      }

   }

   @SubscribeEvent
   public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.PARTICLE_FIST.get(), ParticleFistRenderer::new);
   }
}
