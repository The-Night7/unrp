package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
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
public class ArsenalGateRenderer extends EntityRenderer<ArsenalGateEntity> {
   private final ItemRenderer items;

   public ArsenalGateRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.items = context.getItemRenderer();
   }

   public ResourceLocation getTextureLocation(ArsenalGateEntity entity) {
      return ArsenalDraw.GATE;
   }

   public boolean shouldRender(ArsenalGateEntity entity, Frustum frustum, double x, double y, double z) {
      return entity.shouldRender(x, y, z);
   }

   public void render(ArsenalGateEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      super.render(entity, yaw, partialTick, pose, buffers, packedLight);
      float age = (float)entity.tickCount + partialTick;
      float fire = (float)entity.fireAt();
      ItemStack blade = entity.blade();
      if (!blade.isEmpty() && !(age >= fire) && !(ArsenalDraw.gateSize(age, fire) <= 0.01F)) {
         float e = ArsenalDraw.emerge(age);
         if (!(e <= 0.0F)) {
            float gy = entity.yaw(partialTick);
            float gp = entity.pitch(partialTick);
            Vec3 n = ArsenalGateEntity.direction(gy, gp);
            ArsenalDraw.blade(this.items, blade, pose, buffers, n.scale(ArsenalDraw.bladeOffset(e, age, entity.roll())), gy, gp, entity.roll(), ArsenalDraw.bladeScale(e), entity.level(), entity.getId());
         }
      }
   }

   @SubscribeEvent
   public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.ARSENAL_GATE.get(), ArsenalGateRenderer::new);
      event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.ARSENAL_BOLT.get(), ArsenalBoltRenderer::new);
   }
}
