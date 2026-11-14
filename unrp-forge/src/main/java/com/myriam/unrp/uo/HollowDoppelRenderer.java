package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public class HollowDoppelRenderer extends HumanoidMobRenderer<HollowDoppelEntity, PlayerModel<HollowDoppelEntity>> {
   private final PlayerModel<HollowDoppelEntity> wide = (PlayerModel)this.model;
   private final PlayerModel<HollowDoppelEntity> slim;

   public HollowDoppelRenderer(EntityRendererProvider.Context context) {
      super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
      this.slim = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
   }

   private static PlayerInfo infoOf(HollowDoppelEntity entity) {
      UUID owner = entity.getOwnerId();
      ClientPacketListener conn = Minecraft.getInstance().getConnection();
      return owner != null && conn != null ? conn.getPlayerInfo(owner) : null;
   }

   public void render(HollowDoppelEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
      PlayerInfo info = infoOf(entity);
      this.model = info != null && "slim".equals(info.getModelName()) ? this.slim : this.wide;
      super.render(entity, yaw, partialTick, pose, buffers, light);
   }

   protected void scale(HollowDoppelEntity entity, PoseStack pose, float partialTick) {
      pose.scale(0.9375F, 0.9375F, 0.9375F);
   }

   public ResourceLocation getTextureLocation(HollowDoppelEntity entity) {
      PlayerInfo info = infoOf(entity);
      return info != null ? info.getSkinLocation() : DefaultPlayerSkin.getDefaultSkin();
   }

   @EventBusSubscriber(
      modid = "unrp",
      bus = Bus.MOD,
      value = {Dist.CLIENT}
   )
   public static final class HollowDoppelRenders {
      private HollowDoppelRenders() {
      }

      @SubscribeEvent
      public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
         event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.HOLLOW_DOPPEL.get(), HollowDoppelRenderer::new);
      }
   }
}
