package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ArsenalBoltRenderer extends EntityRenderer<ArsenalBoltEntity> {
   private final ItemRenderer items;

   public ArsenalBoltRenderer(EntityRendererProvider.Context context) {
      super(context);
      this.items = context.getItemRenderer();
   }

   public ResourceLocation getTextureLocation(ArsenalBoltEntity entity) {
      return ArsenalDraw.GATE;
   }

   public void render(ArsenalBoltEntity bolt, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      super.render(bolt, yaw, partialTick, pose, buffers, packedLight);
      float age = (float)bolt.tickCount + partialTick;
      float by = bolt.faceYaw();
      float bp = bolt.facePitch();
      Vec3 n = ArsenalGateEntity.direction(by, bp);
      boolean shot = bolt.mode() == 1;
      int stuck = bolt.stuckAt();
      float since = stuck > 0 ? Math.max(0.0F, age - (float)stuck) : 0.0F;
      float fade = stuck > 0 ? 1.0F - ArsenalDraw.clamp01((since - 11.0F) / 5.0F) : 1.0F;
      Camera cam = this.entityRenderDispatcher.camera;
      Vector3f l = cam.getLeftVector();
      Vector3f u = cam.getUpVector();
      Vec3 camRight = new Vec3((double)(-l.x()), (double)(-l.y()), (double)(-l.z()));
      Vec3 camUp = new Vec3((double)u.x(), (double)u.y(), (double)u.z());
      Vec3 eye = cam.getPosition().subtract(bolt.getPosition(partialTick));
      Matrix4f m = pose.last().pose();
      if (!shot) {
         ArsenalDraw.blade(this.items, bolt.getItem(), pose, buffers, Vec3.ZERO, by, bp, bolt.faceRoll(), 1.15F * fade, bolt.level(), bolt.getId());
      }

      float trail = stuck > 0 ? 1.0F - ArsenalDraw.clamp01(since / 4.0F) : 1.0F;
      if (trail > 0.0F) {
         double speed = stuck > 0 ? 2.2D : Math.max(0.6D, bolt.getDeltaMovement().length());
         ArsenalDraw.streak(buffers.getBuffer(ArsenalDraw.add()), m, n, eye, ArsenalDraw.streakLength(shot, speed, age, trail), shot, bolt.tier(), trail);
      }

      if (shot) {
         ArsenalDraw.shotGlow(buffers.getBuffer(ArsenalDraw.add()), m, camRight, camUp, bolt.tier(), age);
      }

   }
}
