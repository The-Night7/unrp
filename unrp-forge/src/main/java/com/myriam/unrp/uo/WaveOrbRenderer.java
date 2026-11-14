package com.myriam.unrp.uo;
import com.myriam.unrp.pass.ViewUp;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class WaveOrbRenderer extends EntityRenderer<WaveOrbEntity> {
   private static final ResourceLocation TEX = new ResourceLocation("unrp", "textures/gui/beam_glow.png");
   public static final double PALMS_AHEAD = 1.05D;
   public static final double PALMS_DROP = 0.18D;
   public static final double FIRST_AHEAD = 1.6D;
   public static final double FIRST_DROP = 0.55D;
   public static final float FIRST_SCALE = 0.45F;
   public static final int WEDGES = 24;
   public static final int SPARK_WEDGES = 8;

   public WaveOrbRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(WaveOrbEntity entity) {
      return TEX;
   }

   public void render(WaveOrbEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int packedLight) {
      float t;
      float r;
      float cr;
      float cg;
      float cb;
      Minecraft mc;
      Vec3 cam;
      Vec3 origin;
      Player var10000;
      label63: {
         super.render(entity, yaw, partialTick, pose, buffers, packedLight);
         t = (float)entity.tickCount + partialTick;
         r = entity.getRadius();
         int color = entity.getColor();
         cr = (float)(color >> 16 & 255) / 255.0F;
         cg = (float)(color >> 8 & 255) / 255.0F;
         cb = (float)(color & 255) / 255.0F;
         mc = Minecraft.getInstance();
         cam = mc.gameRenderer.getMainCamera().getPosition();
         origin = entity.getPosition(partialTick);
         Entity owner = entity.getOwner() >= 0 ? entity.level().getEntity(entity.getOwner()) : null;
         if (owner instanceof Player p) {
            if (p.isAlive()) {
               var10000 = p;
               break label63;
            }
         }

         var10000 = null;
      }

      Player holder = var10000;
      boolean first = holder != null && holder == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson();
      Vec3 hold = holder == null ? origin : palms(holder, partialTick, first);
      if (first) {
         r *= 0.45F;
      }

      Vec3 toCam = cam.subtract(hold);
      if (!(toCam.lengthSqr() < 1.0E-4D)) {
         Vec3 fwd = toCam.normalize();
         Vec3 up0 = Math.abs(fwd.y) > 0.95D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
         Vec3 right = fwd.cross(up0).normalize();
         Vec3 up = right.cross(fwd).normalize();
         pose.pushPose();
         pose.translate(hold.x - origin.x, hold.y - origin.y, hold.z - origin.z);
         VertexConsumer vc = buffers.getBuffer(RenderType.eyes(TEX));
         Matrix4f m = pose.last().pose();
         float pulse = 1.0F + 0.06F * (float)Math.sin((double)(t * 0.55F));
         this.disc(vc, m, right, up, r * 1.65F * pulse, t * 0.01F, cr, cg, cb, 0.22F);
         this.disc(vc, m, right, up, r * 1.05F * pulse, -t * 0.014F, cr, cg, cb, 0.65F);
         this.disc(vc, m, right, up, r * 0.7F * pulse, t * 0.02F, cr * 0.5F + 0.5F, cg * 0.5F + 0.5F, cb * 0.5F + 0.5F, 0.85F);
         this.disc(vc, m, right, up, r * 0.42F * pulse, -t * 0.026F, 1.0F, 1.0F, 1.0F, 0.95F);

         for(int i = 0; i < 8; ++i) {
            double a_0 = (double)t * 0.16D + (Math.PI * 2D) * (double)i / 8.0D;
            double tilt = Math.sin((double)t * 0.07D + (double)i) * 0.5D;
            Vec3 orbit = right.scale(Math.cos(a_0) * (double)r * 1.35D).add(up.scale(Math.sin(a_0) * (double)r * 1.35D * (0.6D + tilt * 0.4D)));
            this.spark(vc, m, orbit, right, up, r * 0.1F, 1.0F, 1.0F, 1.0F, 0.9F);
         }

         for(int i = 0; i < 5; ++i) {
            double a = (double)(-t) * 0.11D + (Math.PI * 2D) * (double)i / 5.0D + 0.7D;
            double reach = (double)r * (2.6D - ((double)t * 0.05D + (double)i * 0.37D) % 1.0D * 1.3D);
            Vec3 at = right.scale(Math.cos(a) * reach).add(up.scale(Math.sin(a) * reach));
            this.spark(vc, m, at, right, up, r * 0.06F, cr, cg, cb, 0.7F);
         }

         pose.popPose();
      }
   }

   static Vec3 palms(Player p, float partialTick, boolean first) {
      Vec3 eye = p.getEyePosition(partialTick);
      Vec3 view = p.getViewVector(partialTick);
      if (first) {
         Vec3 side = view.cross(new Vec3(0.0D, 1.0D, 0.0D));
         side = side.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : side.normalize();
         Vec3 lift = side.cross(view).normalize();
         return eye.add(view.scale(1.6D)).subtract(lift.scale(0.55D));
      } else {
         return eye.add(view.scale(1.05D)).add(0.0D, -0.18D, 0.0D);
      }
   }

   private void disc(VertexConsumer vc, Matrix4f m, Vec3 right, Vec3 up, float radius, float spin, float r, float g, float b, float a) {
      this.glow(vc, m, Vec3.ZERO, right, up, radius, spin, 24, r, g, b, a);
   }

   private void spark(VertexConsumer vc, Matrix4f m, Vec3 at, Vec3 right, Vec3 up, float radius, float r, float g, float b, float a) {
      this.glow(vc, m, at, right, up, radius, 0.0F, 8, r, g, b, a);
   }

   private void glow(VertexConsumer vc, Matrix4f m, Vec3 at, Vec3 right, Vec3 up, float radius, float spin, int wedges, float r, float g, float b, float a) {
      for(int i = 0; i < wedges; ++i) {
         double a0 = (double)spin + (Math.PI * 2D) * (double)i / (double)wedges;
         double a1 = (double)spin + (Math.PI * 2D) * (double)(i + 1) / (double)wedges;
         Vec3 p0 = at.add(right.scale(Math.cos(a0) * (double)radius)).add(up.scale(Math.sin(a0) * (double)radius));
         Vec3 p1 = at.add(right.scale(Math.cos(a1) * (double)radius)).add(up.scale(Math.sin(a1) * (double)radius));
         this.vtx(vc, m, at, r, g, b, a);
         this.vtx(vc, m, at, r, g, b, a);
         this.vtx(vc, m, p1, r, g, b, 0.0F);
         this.vtx(vc, m, p0, r, g, b, 0.0F);
         this.vtx(vc, m, p0, r, g, b, 0.0F);
         this.vtx(vc, m, p1, r, g, b, 0.0F);
         this.vtx(vc, m, at, r, g, b, a);
         this.vtx(vc, m, at, r, g, b, a);
      }

   }

   private void vtx(VertexConsumer vc, Matrix4f m, Vec3 p, float r, float g, float b, float a) {
      float lit = Math.max(0.0F, Math.min(1.0F, a));
      vc.vertex(m, (float)p.x, (float)p.y, (float)p.z).color(r * lit, g * lit, b * lit, lit).uv(0.5F, 0.5F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(ViewUp.UP[0], ViewUp.UP[1], ViewUp.UP[2]).endVertex();
   }

   public boolean shouldRender(WaveOrbEntity entity, Frustum frustum, double camX, double camY, double camZ) {
      return true;
   }

   @SubscribeEvent
   public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.WAVE_ORB.get(), WaveOrbRenderer::new);
   }
}
