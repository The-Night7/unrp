package com.myriam.unrp.uo;

import com.myriam.unrp.look.ClientSettingsCache;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.UUID;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
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
public class ParticleConstructRenderer extends EntityRenderer<ParticleConstructEntity> {
   private static final String[] VARIANT_NAMES = new String[]{"pylon", "wall", "tower", "hand"};
   private static final ResourceLocation[][] STAGES = buildStages();
   private static final ShardDraw.Look LOOK = new ShardDraw.Look();

   private static ResourceLocation[][] buildStages() {
      ResourceLocation[][] out = new ResourceLocation[4][4];

      for(int v = 0; v < 4; ++v) {
         for(int s = 0; s < 4; ++s) {
            out[v][s] = new ResourceLocation("unrp", "textures/entity/particle_construct_" + VARIANT_NAMES[v] + "_" + s + ".png");
         }
      }

      return out;
   }

   public ParticleConstructRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   static ResourceLocation stageTexture(int kind, float f) {
      int stage = f > 0.75F ? 0 : (f > 0.5F ? 1 : (f > 0.25F ? 2 : 3));
      int variant = Math.min(3, Math.max(0, kind));
      return STAGES[variant][stage];
   }

   public ResourceLocation getTextureLocation(ParticleConstructEntity entity) {
      return stageTexture(entity.getVariant(), entity.hpFraction());
   }

   static int colorOf(ParticleConstructEntity entity) {
      int col = 15160448;
      UUID owner = entity.getOwnerId();
      if (owner != null) {
         if (ClientSettingsCache.isAM(owner)) {
            col = 3026486;
         } else {
            int stored = ClientSettingsCache.getColor(owner);
            if (stored != -1) {
               col = stored;
            }
         }
      }

      return col;
   }

   public void render(ParticleConstructEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      super.render(entity, yaw, partialTick, pose, buffers, packedLight);
      int variant = Math.min(3, Math.max(0, entity.getVariant()));
      float t = (float)entity.tickCount + partialTick;
      ShardDraw.Look look = LOOK.begin(t, entity.isAwake(), colorOf(entity));
      int born = entity.getBorn();
      if (born != 0) {
         float age = (float)((int)entity.level().getGameTime() - born) + partialTick;
         if (age > -40.0F && age < 26.0F) {
            look.spawn = Math.max(0.0F, age);
         }
      }

      pose.pushPose();

      try {
         pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
         ShardDraw.draw(ShardDraw.CONSTRUCTS[variant], look, pose, buffers, this.getTextureLocation(entity));
      } finally {
         pose.popPose();
      }

   }

   @SubscribeEvent
   public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.PARTICLE_CONSTRUCT.get(), ParticleConstructRenderer::new);
   }
}
