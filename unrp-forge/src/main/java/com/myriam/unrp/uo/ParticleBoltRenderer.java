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
public class ParticleBoltRenderer extends EntityRenderer<ParticleBoltEntity> {
   private static final ResourceLocation TEX = new ResourceLocation("unrp", "textures/entity/particle_construct_tower_0.png");
   private static final float[][] WINGS = new float[][]{{0.2F, 0.0F}, {-0.2F, 0.0F}, {0.0F, 0.2F}, {0.0F, -0.2F}};
   private static final ShardDraw.Look LOOK = new ShardDraw.Look();

   public ParticleBoltRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(ParticleBoltEntity entity) {
      return TEX;
   }

   public void render(ParticleBoltEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight) {
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

      int cr = Math.min(255, (main >> 16 & 255) + 35);
      int cg = Math.min(255, (main >> 8 & 255) + 35);
      int cb = Math.min(255, (main & 255) + 35);
      float size = entity.getBoltSize();
      Vec3 vel = entity.getDeltaMovement();
      Vec3 face = vel.lengthSqr() > 0.01D ? vel : new Vec3(0.0D, 0.0D, 1.0D);
      float ry = (float)(Mth.atan2(face.x, face.z) * (180D / Math.PI));
      float rp = (float)(Mth.atan2(face.y, Math.sqrt(face.x * face.x + face.z * face.z)) * (180D / Math.PI));
      float len = Math.min(3.0F, (float)vel.length() * 1.6F) / size;
      ShardDraw.Look look = LOOK.begin((float)entity.tickCount + partialTick, true, cr << 16 | cg << 8 | cb);
      pose.pushPose();

      try {
         pose.mulPose(Axis.YP.rotationDegrees(ry));
         pose.mulPose(Axis.XP.rotationDegrees(-rp));
         if (entity.getStyle() == 0) {
            this.drawArrow(pose, buffers, look, size, len);
         } else {
            this.drawStake(pose, buffers, look, size, len);
         }
      } finally {
         pose.popPose();
      }

   }

   private void drawArrow(PoseStack pose, MultiBufferSource buffers, ShardDraw.Look look, float size, float len) {
      look.grow(size / ShardDraw.ARROW.scale());
      look.streak(0.0F, 0.0F, -0.45F, len * 1.4F, 0.05F, 0.55F);

      for(float[] w : WINGS) {
         look.streak(w[0], w[1], 0.44F, len, 0.03F, 0.3F);
      }

      ShardDraw.draw(ShardDraw.ARROW, look, pose, buffers, TEX);
   }

   private void drawStake(PoseStack pose, MultiBufferSource buffers, ShardDraw.Look look, float size, float len) {
      look.grow(size / ShardDraw.STAKE.scale());
      look.streak(0.0F, 0.0F, -0.64F, len * 1.4F, 0.07F, 0.55F);
      ShardDraw.draw(ShardDraw.STAKE, look, pose, buffers, TEX);
   }

   @SubscribeEvent
   public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.PARTICLE_BOLT.get(), ParticleBoltRenderer::new);
   }
}
