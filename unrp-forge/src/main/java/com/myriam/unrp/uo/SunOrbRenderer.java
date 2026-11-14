package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class SunOrbRenderer extends EntityRenderer<SunOrbEntity> {
   public static final int GLOW_ROWS = 30;
   public static final int FEET_ROWS = 4;
   public static final double FEET_FADE = 2.0D;
   public static final int FLAME_ROWS = 5;
   public static final int STREAK = 18;

   public SunOrbRenderer(EntityRendererProvider.Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(SunOrbEntity entity) {
      return DaystarDraw.FX;
   }

   public boolean shouldRender(SunOrbEntity entity, Frustum frustum, double x, double y, double z) {
      return entity.shouldRender(x, y, z);
   }

   public static Vec3 heldCenter(Player p, float pt, double size) {
      return heldCenter(p, pt, size, 0);
   }

   public static Vec3 heldCenter(Player p, float pt, double size, int mode) {
      return DaystarEvents.heldAt(p.getPosition(pt), Mth.rotLerp(pt, p.yBodyRotO, p.yBodyRot), DaystarEvents.standScale(p), size, mode);
   }

   static float inside(Vec3 cam, Vec3 c, double r) {
      return DaystarDraw.clamp01((cam.distanceTo(c) - r * 0.35D) / (r * 0.8D));
   }

   static boolean firstSelf(Entity owner) {
      Minecraft mc = Minecraft.getInstance();
      return owner != null && mc.getCameraEntity() == owner && mc.options.getCameraType().isFirstPerson();
   }

   static float fade(double age, double span, int life) {
      return DaystarDraw.clamp01(1.0D - age / Math.min(span, (double)life - 2.0D));
   }

   static float near(float drop, double r) {
      return drop < 0.0F ? 0.0F : DaystarDraw.clamp01(1.0D - (double)drop / (r + 1.0D));
   }

   public void render(SunOrbEntity e, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
      super.render(e, yaw, pt, pose, buffers, light);
      Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
      Vector3f l = camera.getLeftVector();
      Vector3f u = camera.getUpVector();
      Vec3 camRight = new Vec3((double)(-l.x()), (double)(-l.y()), (double)(-l.z()));
      Vec3 camUp = new Vec3((double)u.x(), (double)u.y(), (double)u.z());
      DaystarDraw.normals(pose.last().normal());
      Matrix4f m = pose.last().pose();
      double t = (double)((float)e.tickCount + pt);
      Vec3 here = e.getPosition(pt);
      int state = e.state();
      double r = (double)e.size() * 0.5D;
      Level level = e.level();
      int mode = e.mode();
      Vec3 cam = camera.getPosition();
      if (state != 0 && state != 1) {
         if (state == 2) {
            Vec3 v = e.getDeltaMovement();
            if (!e.ember()) {
               double gy = GroundScan.below(level, here.x, here.y - 1.0D, here.z, 30);
               if (!Double.isNaN(gy)) {
                  double h = here.y - gy;
                  VertexConsumer under = buffers.getBuffer(DaystarDraw.add());
                  DaystarDraw.flat(under, m, new Vec3(0.0D, gy - here.y + 0.04D, 0.0D), r * 2.2D + h * 0.3D, t * 0.01D, 5, 1.0F, 0.62F, 0.22F, 0.5F * DaystarDraw.clamp01(1.0D - h / 30.0D), true);
               }
            }

            if (mode == 2) {
               streak(buffers, m, v, r, cam.subtract(here), camRight, camUp);
            }

            DaystarDraw.orb(buffers, m, Vec3.ZERO, r, t, e.ember(), inside(cam, here, r), camRight, camUp);
            VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
            int n = e.ember() ? 8 : 12;

            for(int i = 1; i < n; ++i) {
               Vec3 q = v.scale((double)(-i) * 0.7D);
               float k = 1.0F - (float)i / (float)n;
               DaystarDraw.bill(add, m, q, camRight, camUp, r * (0.9D * (double)k + 0.2D), 0.0D, 5, 1.0F, e.ember() ? 0.4F : 0.62F, 0.18F, 0.45F * k, true);
               if (i % 2 == 1) {
                  DaystarDraw.bill(add, m, q.add(0.0D, 0.05D * (double)i, 0.0D), camRight, camUp, 0.05D + 0.02D * r, 0.0D, 7, 1.0F, 0.8F, 0.4F, 0.8F * k, true);
               }
            }

         } else {
            Vec3 at = e.position();
            Vec3 fix = at.subtract(here);
            double wide = Math.max(0.5D, (double)e.blast());
            boolean self = firstSelf(level.getEntity(e.ownerId()));
            float near = self ? DaystarDraw.clamp01((cam.distanceTo(at) - wide - 1.5D) / 3.0D) : 1.0F;
            float beam = self ? DaystarDraw.clamp01((Math.hypot(cam.x - at.x, cam.z - at.z) - wide * 0.45D - 0.3D) / 3.0D) : 1.0F;
            pose.pushPose();

            try {
               pose.translate(fix.x, fix.y, fix.z);
               if (mode == 1) {
                  burst(buffers, pose.last().pose(), level, e, at, t, camRight, camUp, near);
               } else {
                  blast(buffers, pose.last().pose(), level, e, at, t, camRight, camUp, near);
                  if (mode == 2) {
                     pillar(buffers, pose.last().pose(), e, at, t, camRight, beam);
                  }
               }
            } finally {
               pose.popPose();
            }

         }
      } else {
         Entity owner = level.getEntity(e.ownerId());
         if (mode == 1) {
            if (owner instanceof Player) {
               Player p_0 = (Player)owner;
               kindle(buffers, m, level, e, p_0, pt, here, t, camRight, camUp);
            }

         } else {
            Vec3 var10000;
            if (owner instanceof Player) {
               Player p_1 = (Player)owner;
               var10000 = heldCenter(p_1, pt, (double)e.size());
            } else {
               var10000 = here;
            }

            Vec3 c = var10000;
            if (mode == 2 && owner instanceof Player) {
               Player p_2 = (Player)owner;
               c = SunOrbEntity.swoopAt(c, p_2.getPosition(pt), e.low(), e.stateAge(t));
            }

            float a = DaystarDraw.clamp01(t / 4.0D) * inside(cam, c, r);
            if (!e.ember() && owner instanceof Player) {
               Player p = (Player)owner;
               if (state == 0) {
                  radiate(buffers, m, level, p.getPosition(pt), here, c, r, e.charge(), t, camRight, camUp);
               }
            }

            if (owner instanceof Player && state == 0) {
               fingertip(buffers, m, c.subtract(here), r, e.charge(), e.ember(), t, camRight, camUp);
            }

            DaystarDraw.orb(buffers, m, c.subtract(here), r, t, e.ember(), a, camRight, camUp);
            if (!e.ember() && owner instanceof Player && state == 0) {
               charged(buffers, m, c.subtract(here), r, e.charge(), t, camRight, camUp);
            }

         }
      }
   }

   private static void fingertip(MultiBufferSource buffers, Matrix4f m, Vec3 sc, double r, float charge, boolean ember, double t, Vec3 camRight, Vec3 camUp) {
      float up = DaystarDraw.clamp01((t - 3.0D) / 3.0D);
      if (!(up <= 0.0F)) {
         Vec3 tip = sc.add(0.0D, -r - 0.1D, 0.0D);
         float pulse = 0.8F + 0.2F * (float)Math.sin(t * 0.6D);
         VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
         DaystarDraw.upright(add, m, tip, camRight, 0.07D, r * 0.7D + 0.1D, 12, 1.0F, 0.95F, 0.75F, 0.7F * up, true);
         DaystarDraw.bill(add, m, tip, camRight, camUp, 0.3D + 0.06D * (double)pulse, 0.0D, 5, 1.0F, 0.8F, 0.4F, 0.6F * up, true);
         DaystarDraw.bill(add, m, tip, camRight, camUp, 0.11D * (double)pulse, t * 0.05D, 0, 1.0F, 0.97F, 0.86F, 0.95F * up, true);
         DaystarDraw.bill(add, m, tip, camRight, camUp, 0.38D + 0.08D * (double)pulse, -t * 0.03D, 1, 1.0F, 0.86F, 0.46F, 0.5F * up, true);
         if (!ember && !(charge >= 1.0F)) {
            for(int i = 0; i < 4; ++i) {
               double ph = (t * 0.07D + (double)i * 0.25D) % 1.0D;
               double sway = Math.sin(t * 0.3D + (double)i * 1.9D) * 0.05D;
               Vec3 q = tip.add(sway, ph * (r * 0.8D + 0.1D), Math.cos(t * 0.27D + (double)i * 2.3D) * 0.05D);
               DaystarDraw.bill(add, m, q, camRight, camUp, 0.05D, 0.0D, 7, 1.0F, 0.9F, 0.55F, 0.9F * up * (float)(1.0D - ph), true);
            }

         }
      }
   }

   private static void kindle(MultiBufferSource buffers, Matrix4f m, Level level, SunOrbEntity e, Player p, float pt, Vec3 here, double t, Vec3 camRight, Vec3 camUp) {
      Vec3 feet = p.getPosition(pt);
      double sc = DaystarEvents.standScale(p);
      boolean ember = e.ember();
      boolean self = firstSelf(p);
      float k = ember ? 0.45F : DaystarDraw.clamp01((double)e.charge());
      float a = DaystarDraw.clamp01(t / 4.0D);
      double shell = (0.8D + 0.9D * (double)k) * sc;
      double reach = DaystarMath.flareReach((double)e.size(), ember);
      Vec3 cc = DaystarEvents.heldAt(feet, 0.0F, sc, (double)e.size(), 1).subtract(here);
      Vec3 f = feet.subtract(here);
      VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
      if (!self) {
         DaystarDraw.bill(add, m, cc, camRight, camUp, shell * 1.9D, 0.0D, 5, 1.0F, 0.55F, 0.16F, (0.3F + 0.3F * k) * a, true);
         DaystarDraw.bill(add, m, cc, camRight, camUp, shell * 1.35D, t * 0.02D, 1, 1.0F, 0.78F, 0.32F, (0.25F + 0.45F * k) * a, true);
         DaystarDraw.bill(add, m, cc, camRight, camUp, shell * 1.05D, -t * 0.015D + 0.3D, 1, 1.0F, 0.6F, 0.2F, (0.2F + 0.3F * k) * a, true);
         DaystarDraw.bill(add, m, cc, camRight, camUp, shell, t * 0.004D, 0, 1.0F, 0.86F, 0.5F, (0.16F + 0.2F * k) * a, true);

         for(int i = 0; i < 14; ++i) {
            double ph = (t * 0.035D + (double)i / 14.0D) % 1.0D;
            double ang_0 = (double)i * 0.449D + t * 0.12D;
            double rad = shell * (0.55D + 0.2D * Math.sin(t * 0.2D + (double)i));
            Vec3 q = f.add(Math.cos(ang_0) * rad, ph * 2.2D * sc, Math.sin(ang_0) * rad);
            DaystarDraw.bill(add, m, q, camRight, camUp, 0.05D, 0.0D, 7, 1.0F, 0.84F, 0.42F, (0.4F + 0.6F * k) * a * (float)(1.0D - ph), true);
         }
      }

      double gy = GroundScan.below(level, feet.x, feet.y, feet.z, 4);
      float floor = Double.isNaN(gy) ? 0.0F : DaystarDraw.clamp01(1.0D - (feet.y - gy) / 2.0D);
      if (!(floor <= 0.0F)) {
         Vec3 g = (new Vec3(feet.x, gy, feet.z)).subtract(here);
         int lit = LevelRenderer.getLightColor(level, BlockPos.containing(feet.x, gy + 0.5D, feet.z));
         DaystarDraw.decal(buffers, m, g.add(0.0D, 0.02D, 0.0D), (0.8D + 1.6D * (double)k) * sc, 0.4D, 0, 0.65F * k * floor * a, lit);
         add = buffers.getBuffer(DaystarDraw.add());
         DaystarDraw.flat(add, m, g.add(0.0D, 0.04D, 0.0D), (1.4D + 1.8D * (double)k) * sc, t * 0.01D, 5, 1.0F, 0.58F, 0.2F, (0.24F + 0.2F * k) * floor * a, true);
         float beat = 0.6F + 0.4F * (float)Math.sin(t * 0.35D);
         DaystarDraw.ring(add, m, g.add(0.0D, 0.06D, 0.0D), reach, 0.16D + 0.06D * (double)k, 56, 1.0F, 0.7F, 0.3F, (0.12F + 0.3F * k) * beat * floor * a);
         VertexConsumer soft = buffers.getBuffer(DaystarDraw.soft());
         int n = 12;
         double ring = (self ? 1.5D : 0.55D) * sc + 0.3D * (double)k;

         for(int i = 0; i < n; ++i) {
            double ang = (double)i / (double)n * Math.PI * 2.0D + t * 0.015D;
            double bx = feet.x + Math.cos(ang) * ring;
            double bz = feet.z + Math.sin(ang) * ring;
            double by = GroundScan.below(level, bx, gy + 0.5D, bz, 5);
            if (!Double.isNaN(by)) {
               Vec3 base = (new Vec3(bx, by, bz)).subtract(here);
               double fl = 0.7D + 0.3D * Math.sin(t * 0.8D + (double)i * 1.7D);
               double h = (self ? 0.5D + 0.5D * (double)k : 0.6D + 1.5D * (double)k) * sc * fl;
               DaystarDraw.upright(soft, m, base, camRight, 0.42D + 0.2D * (double)k, h, i % 2 == 0 ? 2 : 9, 1.0F, 0.6F, 0.2F, 0.85F * floor * a, false);
            }
         }

      }
   }

   private static void streak(MultiBufferSource buffers, Matrix4f m, Vec3 v, double r, Vec3 eye, Vec3 camRight, Vec3 camUp) {
      double speed = v.length();
      if (!(speed < 0.001D)) {
         VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
         Vec3 back = v.scale(-1.0D / speed);
         double step = (4.0D + speed * 3.0D + r) / 18.0D;
         Vec3 prev = Vec3.ZERO;

         for(int i = 1; i <= 18; ++i) {
            Vec3 q = back.scale((double)i * step);
            float k = 1.0F - (float)i / 19.0F;
            DaystarDraw.ribbon(add, m, prev, q, eye, r * (1.3D * (double)k + 0.25D), 8, 1.0F, 0.7F, 0.28F, 0.6F * k, true);
            DaystarDraw.ribbon(add, m, prev, q, eye, r * (0.45D * (double)k + 0.08D), 13, 1.0F, 0.95F, 0.78F, 0.8F * k, true);
            if (i % 3 == 0) {
               DaystarDraw.bill(add, m, q, camRight, camUp, r * 0.6D * (double)k + 0.1D, (double)i, 5, 1.0F, 0.6F, 0.2F, 0.4F * k, true);
            }

            prev = q;
         }

      }
   }

   private static void pillar(MultiBufferSource buffers, Matrix4f m, SunOrbEntity e, Vec3 here, double t, Vec3 camRight, float seen) {
      double age = e.stateAge(t);
      float k = DaystarDraw.clamp01(1.0D - age / 16.0D);
      if (!(k <= 0.0F)) {
         double r = Math.max(0.5D, (double)e.blast());
         double gy = here.y - (double)Math.max(0.0F, e.drop());
         Vec3 floor = new Vec3(0.0D, gy - here.y, 0.0D);
         double tall = 14.0D + r * 3.0D;
         VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
         if (seen > 0.0F) {
            DaystarDraw.upright(add, m, floor, camRight, r * 0.9D + 0.6D, tall, 12, 1.0F, 0.86F, 0.5F, 0.55F * k * seen, true);
            DaystarDraw.upright(add, m, floor, camRight, r * 0.35D + 0.25D, tall, 12, 1.0F, 0.98F, 0.88F, 0.8F * k * seen, true);
         }

         if (e.drop() >= 0.0F) {
            DaystarDraw.ring(add, m, floor.add(0.0D, 0.08D, 0.0D), r * 3.0D * (double)DaystarDraw.outCubic(age / 20.0D), 0.5D, 64, 1.0F, 0.82F, 0.5F, 0.6F * fade(age, 24.0D, e.ember() ? 18 : 44));
         }

      }
   }

   private static void burst(MultiBufferSource buffers, Matrix4f m, Level level, SunOrbEntity e, Vec3 here, double t, Vec3 camRight, Vec3 camUp, float seen) {
      double age = e.stateAge(t);
      boolean ember = e.ember();
      double reach = Math.max(1.0D, (double)e.blast());
      int life = ember ? 18 : 44;
      float drop = e.drop();
      boolean grounded = drop >= 0.0F;
      double gy = here.y - (double)Math.max(0.0F, drop);
      Vec3 floor = new Vec3(0.0D, gy - here.y, 0.0D);
      float grow = DaystarDraw.outCubic(age / 9.0D);
      double rad = reach * (double)grow;
      float flash = DaystarDraw.clamp01(1.0D - age / 7.0D);
      float ringFade = fade(age, 26.0D, life);
      VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
      DaystarDraw.bill(add, m, Vec3.ZERO, camRight, camUp, reach * (0.4D + 0.7D * (double)grow), 0.0D, 5, 1.0F, 0.85F, 0.5F, (0.3F + 0.6F * seen) * flash, true);
      if (seen > 0.0F) {
         DaystarDraw.bill(add, m, Vec3.ZERO, camRight, camUp, reach * (0.5D + (double)grow), age * 0.05D, 1, 1.0F, 0.75F, 0.3F, 0.8F * flash * seen, true);
      }

      if (grounded) {
         int lit = LevelRenderer.getLightColor(level, BlockPos.containing(here.x, gy + 0.5D, here.z));
         DaystarDraw.decal(buffers, m, floor.add(0.0D, 0.03D, 0.0D), reach * 0.8D * (double)grow, 1.1D, 0, 0.85F * DaystarDraw.clamp01(1.0D - (age - (double)life + 12.0D) / 12.0D), lit);
         add = buffers.getBuffer(DaystarDraw.add());
         DaystarDraw.ring(add, m, floor.add(0.0D, 0.06D, 0.0D), rad, 0.7D + 0.22D * reach, 64, 1.0F, 0.62F, 0.22F, 0.95F * ringFade);
         DaystarDraw.ring(add, m, floor.add(0.0D, 0.07D, 0.0D), rad * 1.06D + 0.2D, 0.25D, 64, 1.0F, 0.93F, 0.7F, 0.7F * ringFade);
         DaystarDraw.ring(add, m, floor.add(0.0D, 0.05D, 0.0D), reach * 1.4D * (double)DaystarDraw.outCubic(age / 16.0D), 0.35D, 64, 1.0F, 0.8F, 0.5F, 0.4F * fade(age, 20.0D, life));
         VertexConsumer soft_0 = buffers.getBuffer(DaystarDraw.soft());
         int n = 16 + (int)Math.round(reach * 3.0D);
         Vec3[] bases = new Vec3[n];
         float[] lives = new float[n];

         for(int i = 0; i < n; ++i) {
            double ang_0 = ((double)i + 0.4D * DaystarDraw.hash(i, 3, 9)) / (double)n * Math.PI * 2.0D;
            double bx = here.x + Math.cos(ang_0) * rad;
            double bz = here.z + Math.sin(ang_0) * rad;
            double by = GroundScan.below(level, bx, gy + 0.6D, bz, 5);
            if (!Double.isNaN(by)) {
               lives[i] = fade(age, 16.0D + 8.0D * DaystarDraw.hash(i, 5, 9), life);
               bases[i] = (new Vec3(bx, by, bz)).subtract(here);
               double h_0 = (0.9D + 0.32D * reach) * (double)lives[i] * (0.8D + 0.2D * Math.sin(t * 0.9D + (double)i));
               DaystarDraw.upright(soft_0, m, bases[i], camRight, 0.7D + 0.1D * reach, h_0, i % 2 == 0 ? 2 : 9, 1.0F, 0.58F, 0.18F, 0.92F * lives[i], false);
            }
         }

         add = buffers.getBuffer(DaystarDraw.add());

         for(int i = 0; i < n; ++i) {
            if (bases[i] != null) {
               double h = (0.7D + 0.25D * reach) * (double)lives[i];
               DaystarDraw.upright(add, m, bases[i], camRight, 0.45D, h, 2, 1.0F, 0.78F, 0.34F, 0.55F * lives[i], true);
            }
         }
      }

      add = buffers.getBuffer(DaystarDraw.add());
      int sparks = ember ? 12 : 30;

      for(int i = 0; i < sparks; ++i) {
         double b = (double)i * 6.2832D / (double)sparks + DaystarDraw.hash(i, 9, 2);
         double sp = (0.22D + 0.12D * DaystarDraw.hash(i, 4, 2)) * (0.6D + reach * 0.12D);
         Vec3 q_0 = new Vec3(Math.cos(b) * sp * age, 0.3D + (0.12D + 0.1D * DaystarDraw.hash(i, 6, 2)) * age - 0.012D * age * age, Math.sin(b) * sp * age);
         DaystarDraw.bill(add, m, q_0, camRight, camUp, 0.06D, 0.0D, 7, 1.0F, 0.84F, 0.44F, fade(age, 24.0D, life), true);
      }

      if (age > 8.0D) {
         VertexConsumer soft = buffers.getBuffer(DaystarDraw.soft());
         double sm = (age - 8.0D) / Math.max(1.0D, (double)life - 8.0D);

         for(int i = 0; i < 8; ++i) {
            double ang = (double)i * 0.785D + 0.3D;
            Vec3 q = floor.add(Math.cos(ang) * reach * 0.8D, 0.4D + sm * (1.8D + reach * 0.4D), Math.sin(ang) * reach * 0.8D);
            DaystarDraw.bill(soft, m, q, camRight, camUp, 0.5D + sm * (0.8D + reach * 0.2D), (double)i, 10, 0.22F, 0.2F, 0.19F, 0.5F * DaystarDraw.clamp01(1.0D - sm), false);
         }
      }

   }

   private static void radiate(MultiBufferSource buffers, Matrix4f m, Level level, Vec3 feet, Vec3 here, Vec3 c, double r, float charge, double t, Vec3 camRight, Vec3 camUp) {
      float k = charge < 0.6F ? 0.0F : (charge - 0.6F) / 0.4F;
      double gy = GroundScan.below(level, feet.x, feet.y, feet.z, 4);
      float floor = Double.isNaN(gy) ? 0.0F : DaystarDraw.clamp01(1.0D - (feet.y - gy) / 2.0D);
      if (!(floor <= 0.0F)) {
         Vec3 f = (new Vec3(feet.x, gy, feet.z)).subtract(here);
         if (k > 0.0F) {
            int lit = LevelRenderer.getLightColor(level, BlockPos.containing(feet.x, gy + 0.5D, feet.z));
            DaystarDraw.decal(buffers, m, f.add(0.0D, 0.02D, 0.0D), (r + 1.5D) * (double)DaystarDraw.outCubic((double)k), 0.0D, 0, 0.75F * k * floor, lit);
         }

         VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
         DaystarDraw.flat(add, m, f.add(0.0D, 0.04D, 0.0D), 2.0D + 2.2D * r, t * 0.01D, 5, 1.0F, 0.58F, 0.2F, (0.16F + 0.16F * k) * floor, true);
         if (!(k <= 0.0F)) {
            VertexConsumer soft = buffers.getBuffer(DaystarDraw.soft());

            for(int i = 0; i < 10; ++i) {
               double ang = (double)i * 0.628D + t * 0.02D;
               double rr = (r + 1.5D) * (0.4D + 0.6D * DaystarDraw.hash(i, 1, 33));
               double bx = feet.x + Math.cos(ang) * rr;
               double bz = feet.z + Math.sin(ang) * rr;
               double by = GroundScan.below(level, bx, gy + 0.5D, bz, 5);
               if (!Double.isNaN(by)) {
                  Vec3 base = (new Vec3(bx, by, bz)).subtract(here);
                  double ph = (t * 0.04D + (double)i * 0.1D) % 1.0D;
                  DaystarDraw.bill(soft, m, base.add(0.0D, 0.2D + ph * 1.6D, 0.0D), camRight, camUp, 0.5D + 0.4D * ph, (double)i + t * 0.02D, 3, 1.0F, 0.9F, 0.7F, 0.3F * k * floor * (float)(1.0D - ph), false);
                  if (i % 2 == 0) {
                     double fl = 0.7D + 0.3D * Math.sin(t * 0.8D + (double)i);
                     DaystarDraw.upright(soft, m, base, camRight, 0.35D, (0.4D + 0.6D * (double)k) * fl, 9, 1.0F, 0.6F, 0.2F, 0.85F * k * floor, false);
                  }
               }
            }

         }
      }
   }

   private static void charged(MultiBufferSource buffers, Matrix4f m, Vec3 sc, double r, float charge, double t, Vec3 camRight, Vec3 camUp) {
      float k = charge < 0.6F ? 0.0F : (charge - 0.6F) / 0.4F;
      if (!(k <= 0.0F)) {
         VertexConsumer add = buffers.getBuffer(DaystarDraw.add());

         for(int i = 0; i < 8; ++i) {
            double q = (t * 0.06D + (double)i / 8.0D) % 1.0D;
            Vec3 p = sc.add(Math.cos((double)i * 0.785D) * r * (1.0D + q), -r * q * 1.2D, Math.sin((double)i * 0.785D) * r * (1.0D + q));
            DaystarDraw.bill(add, m, p, camRight, camUp, 0.08D, 0.0D, 7, 1.0F, 0.85F, 0.45F, k * (float)(1.0D - q), true);
         }

         if (charge >= 1.0F) {
            DaystarDraw.plane(add, m, sc, camRight, camUp, r * 1.25D, 0.0D, 4, 1.0F, 0.95F, 0.75F, 0.5F + 0.3F * (float)Math.sin(t * 0.5D), true);
         }

      }
   }

   private static void blast(MultiBufferSource buffers, Matrix4f m, Level level, SunOrbEntity e, Vec3 here, double t, Vec3 camRight, Vec3 camUp, float seen) {
      double age = e.stateAge(t);
      boolean ember = e.ember();
      double r = Math.max(0.5D, (double)e.blast());
      double d = (double)e.size();
      int life = ember ? 18 : 44;
      float drop = e.drop();
      float near = near(drop, r);
      double gy = here.y - (double)Math.max(0.0F, drop);
      Vec3 floor = new Vec3(0.0D, gy - here.y, 0.0D);
      float k = DaystarDraw.clamp01(age / 12.0D);
      float flash = DaystarDraw.clamp01(1.0D - age / 8.0D);
      double big = ember ? 0.4D : 1.0D + d * 0.12D;
      if (near > 0.0F && !e.dug()) {
         int lit = LevelRenderer.getLightColor(level, BlockPos.containing(here.x, gy + 0.5D, here.z));
         DaystarDraw.decal(buffers, m, floor.add(0.0D, 0.03D, 0.0D), ember ? r * 0.85D : r * 0.95D, 0.7D, ember ? 0 : 1, 0.95F * near * DaystarDraw.clamp01(1.0D - (age - (double)life + 10.0D) / 10.0D), lit);
      }

      VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
      DaystarDraw.bill(add, m, new Vec3(0.0D, r * 0.4D, 0.0D), camRight, camUp, r * (0.8D + (double)k) * big, 0.0D, 5, 1.0F, 0.85F, 0.5F, (0.3F + 0.7F * seen) * flash, true);
      if (!ember && seen > 0.0F) {
         DaystarDraw.bill(add, m, new Vec3(0.0D, r * 0.5D, 0.0D), camRight, camUp, r * (0.7D + 1.2D * (double)k), age * 0.04D, 1, 1.0F, 0.75F, 0.3F, 0.85F * flash * seen, true);
         float dome = DaystarDraw.clamp01(1.0D - age / 22.0D);
         DaystarDraw.bill(add, m, new Vec3(0.0D, r * 0.3D, 0.0D), camRight, camUp, r * (0.6D + 0.6D * (double)DaystarDraw.outCubic(age / 10.0D)), age * 0.02D, 0, 1.0F, 0.9F, 0.6F, 0.8F * dome * seen, true);
      }

      if (near > 0.0F) {
         DaystarDraw.ring(add, m, floor.add(0.0D, 0.06D, 0.0D), r * 1.3D * (double)DaystarDraw.outCubic(age / 12.0D), 0.6D + 0.2D * r, 40, 1.0F, 0.6F, 0.2F, 0.9F * near * fade(age, 18.0D, life));
         DaystarDraw.ring(add, m, floor.add(0.0D, 0.07D, 0.0D), r * 2.2D * (double)DaystarDraw.outCubic(age / 18.0D), 0.4D, 48, 1.0F, 0.85F, 0.6F, 0.5F * near * fade(age, 22.0D, life));
      }

      VertexConsumer soft = buffers.getBuffer(DaystarDraw.soft());
      int n = ember ? 5 : (int)Math.round(10.0D + r * 5.0D);
      Vec3[] bases = new Vec3[n];
      float[] lives = new float[n];

      for(int i = 0; i < n; ++i) {
         lives[i] = fade(age, ember ? 10.0D + 6.0D * DaystarDraw.hash(i, 2, 4) : 18.0D + 14.0D * DaystarDraw.hash(i, 2, 4), life);
         if (!(near <= 0.0F)) {
            double a = (double)i / (double)n * Math.PI * 2.0D + 0.2D;
            double rr = r * (double)DaystarDraw.outCubic(age / 10.0D) * (0.55D + 0.45D * DaystarDraw.hash(i, 1, 4));
            double bx = here.x + Math.cos(a) * rr;
            double bz = here.z + Math.sin(a) * rr;
            double by = GroundScan.below(level, bx, gy + 0.5D, bz, 5);
            if (!Double.isNaN(by)) {
               bases[i] = (new Vec3(bx, by, bz)).subtract(here);
               DaystarDraw.upright(soft, m, bases[i], camRight, 0.5D + 0.12D * r, (0.8D + 0.3D * r) * (double)lives[i], i % 2 == 1 ? 2 : 9, 1.0F, 0.58F, 0.18F, 0.92F * lives[i], false);
            }
         }
      }

      int sparks = ember ? 6 : 26;
      Vec3[] bits = new Vec3[sparks];

      for(int i = 0; i < sparks; ++i) {
         double b = (double)i * 0.2417D;
         double el = 0.3D + 0.6D * DaystarDraw.hash(i, 7, 1);
         double sp = (0.14D + 0.12D * DaystarDraw.hash(i, 8, 1)) * (0.6D + r * 0.12D);
         bits[i] = new Vec3(Math.cos(b) * Math.cos(el) * sp * age, 0.3D + Math.sin(el) * sp * age * 1.6D - 0.012D * age * age, Math.sin(b) * Math.cos(el) * sp * age);
         if (!ember && i % 2 == 0) {
            DaystarDraw.bill(soft, m, bits[i], camRight, camUp, 0.18D, b + age * 0.2D, 11, 0.34F, 0.27F, 0.22F, fade(age, 30.0D, life), false);
         }
      }

      if (age > 6.0D) {
         int ns = ember ? 4 : 10;
         double sm = (age - 6.0D) / Math.max(1.0D, (double)life - 6.0D);

         for(int i = 0; i < ns; ++i) {
            Vec3 q = new Vec3(Math.cos((double)i * 1.7D) * r * 0.5D, 0.5D + sm * (2.4D + r * 0.8D) + (double)i * 0.15D, Math.sin((double)i * 1.7D) * r * 0.5D);
            DaystarDraw.bill(soft, m, q, camRight, camUp, 0.4D + sm * (0.8D + r * 0.25D), (double)i, 10, 0.22F, 0.2F, 0.19F, 0.6F * DaystarDraw.clamp01(1.0D - sm), false);
         }
      }

      add = buffers.getBuffer(DaystarDraw.add());

      for(int i = 0; i < n; ++i) {
         if (bases[i] != null) {
            DaystarDraw.upright(add, m, bases[i], camRight, 0.35D, (0.5D + 0.2D * r) * (double)lives[i], 2, 1.0F, 0.72F, 0.3F, 0.5F * lives[i], true);
         }
      }

      for(int i = 0; i < sparks; ++i) {
         DaystarDraw.bill(add, m, bits[i], camRight, camUp, 0.06D, 0.0D, 7, 1.0F, 0.82F, 0.42F, fade(age, 26.0D, life), true);
      }

   }

   @SubscribeEvent
   public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.SUN_ORB.get(), SunOrbRenderer::new);
      event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.SUN_FISSURE.get(), SunFissureRenderer::new);
      event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.SUN_CRESCENT.get(), SunCrescentRenderer::new);
      event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.SUN_AXE.get(), SunAxeRenderer::new);
   }
}
