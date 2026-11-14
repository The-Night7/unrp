package com.myriam.unrp.pass;

import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class PassBodies {
   public static final double MOST_OFF = 1.0D;
   static final Set<Integer> WANT = new HashSet();
   private static final Map<Integer, PassBody> FRAMES = new HashMap();
   private static final Map<Integer, Double> STAMPS = new HashMap();
   private static final Matrix4f VIEW = new Matrix4f();
   private static final Matrix4f VIEW_ROT = new Matrix4f();
   private static Vec3 cam = Vec3.ZERO;
   public static boolean inWorld;

   private PassBodies() {
   }

   public static PassBody get(Entity e, float pt) {
      PassBody b = (PassBody)FRAMES.get(e.getId());
      Double s = (Double)STAMPS.get(e.getId());
      return b != null && s != null && Math.abs(s - (double)((float)e.tickCount + pt)) < 0.001D ? b : box(e, pt);
   }

   public static PassBody last(Entity e) {
      PassBody b = (PassBody)FRAMES.get(e.getId());
      Double s = (Double)STAMPS.get(e.getId());
      return b != null && s != null && Math.abs(s - (double)e.tickCount) <= 1.0D ? b : null;
   }

   public static PassBody box(Entity e, float pt) {
      Vec3 p = e.getPosition(pt);
      double[] at = new double[]{p.x, p.y, p.z};
      return PassBody.box(at, at, (double)e.getBbHeight());
   }

   public static void clear() {
      FRAMES.clear();
      STAMPS.clear();
      WANT.clear();
   }

   static boolean upright(LivingEntity e, float pt) {
      if (!e.isSleeping() && !e.hasPose(Pose.SLEEPING) && !e.isFallFlying() && !e.isAutoSpinAttack() && !e.hasPose(Pose.SWIMMING) && !e.isVisuallySwimming() && e.deathTime <= 0 && true) {
         if (e instanceof AbstractClientPlayer) {
            AbstractClientPlayer pl = (AbstractClientPlayer)e;
            if (pl.getSwimAmount(pt) > 0.0F) {
               return false;
            }
         }

         return !PassBridge.odd(e);
      } else {
         return false;
      }
   }

   static boolean plain(LivingEntity e, float pt) {
      return !e.isBaby() && upright(e, pt);
   }

   static double[] rows(Matrix4f m, Vec3 base) {
      Vector3f o = m.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
      Vector3f x = m.transformPosition(new Vector3f(1.0F, 0.0F, 0.0F));
      Vector3f y = m.transformPosition(new Vector3f(0.0F, 1.0F, 0.0F));
      Vector3f z = m.transformPosition(new Vector3f(0.0F, 0.0F, 1.0F));
      return new double[]{(double)(x.x() - o.x()), (double)(y.x() - o.x()), (double)(z.x() - o.x()), (double)o.x() + base.x, (double)(x.y() - o.y()), (double)(y.y() - o.y()), (double)(z.y() - o.y()), (double)o.y() + base.y, (double)(x.z() - o.z()), (double)(y.z() - o.z()), (double)(z.z() - o.z()), (double)o.z() + base.z};
   }

   static float bodyYaw(LivingEntity e, float pt) {
      float f = Mth.rotLerp(pt, e.yBodyRotO, e.yBodyRot);
      boolean sit = e.isPassenger() && e.getVehicle() != null && e.getVehicle().shouldRiderSit();
      if (sit) {
         Entity veh = e.getVehicle();
         if (veh instanceof LivingEntity) {
            LivingEntity v = (LivingEntity)veh;
            float head = Mth.rotLerp(pt, e.yHeadRotO, e.yHeadRot);
            f = Mth.rotLerp(pt, v.yBodyRotO, v.yBodyRot);
            float d = Mth.wrapDegrees(head - f);
            if (d < -85.0F) {
               d = -85.0F;
            }

            if (d >= 85.0F) {
               d = 85.0F;
            }

            f = head - d;
            if (d * d > 2500.0F) {
               f += d * 0.2F;
            }
         }
      }

      return f;
   }

   static double[] part(ModelPart p, float body, float scale, Quaternionf lean, Matrix4f root, Vec3 base) {
      Matrix4f m = new Matrix4f(root);
      if (lean != null) {
         m.rotate(lean);
      }

      m.rotate(Axis.YP.rotationDegrees(180.0F - body));
      m.scale(-scale, -scale, scale);
      m.translate(0.0F, -1.501F, 0.0F);
      m.translate(p.x / 16.0F, p.y / 16.0F, p.z / 16.0F);
      m.rotate((new Quaternionf()).rotationZYX(p.zRot, p.yRot, p.xRot));
      m.scale(0.0625F);
      return rows(m, base);
   }

   public static double[] layer(Matrix4f pose) {
      return rows((new Matrix4f(VIEW)).mul(pose), cam);
   }

   public static Matrix4f world(Matrix4f pose) {
      return inWorld ? (new Matrix4f(VIEW)).mul(pose) : null;
   }

   public static double[] camAt() {
      return new double[]{cam.x, cam.y, cam.z};
   }

   public static Vector3f toView(float x, float y, float z) {
      Vector3f v = new Vector3f(x, y, z);
      VIEW_ROT.transformDirection(v);
      return v;
   }

   public static PassBody capture(LivingEntity e, HumanoidModel<?> model, float pt, Matrix4f pose) {
      float body = bodyYaw(e, pt);
      float scale = PassBridge.scaleOf(e);
      Quaternionf var10000;
      if (e instanceof AbstractClientPlayer pl) {
         var10000 = PassBridge.lean(pl, body, pt);
      } else {
         var10000 = null;
      }

      Quaternionf lean = var10000;
      Vec3 at = e.getPosition(pt);
      Matrix4f root = (new Matrix4f(VIEW)).mul(pose);
      Vector3f o = root.transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
      double dx = (double)o.x() + cam.x - at.x;
      double dy = (double)o.y() + cam.y - at.y;
      double dz = (double)o.z() + cam.z - at.z;
      if (!(dx * dx + dy * dy + dz * dz <= 1.0D)) {
         return null;
      } else {
         ModelPart[] parts = new ModelPart[]{model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg};
         double[][] m = new double[6][];

         for(int i = 0; i < 6; ++i) {
            m[i] = part(parts[i], body, scale, lean, root, cam);

            for(double x : m[i]) {
               if (!Double.isFinite(x)) {
                  return null;
               }
            }
         }

         boolean var28;
         label31: {
            if (e instanceof AbstractClientPlayer) {
               AbstractClientPlayer pl = (AbstractClientPlayer)e;
               if ("slim".equals(pl.getModelName())) {
                  var28 = true;
                  break label31;
               }
            }

            var28 = false;
         }

         boolean slim = var28;
         double[] feet = new double[]{at.x, at.y, at.z};
         return new PassBody(m, slim, feet, feet, (double)e.getBbHeight());
      }
   }

   @SubscribeEvent
   public static void onStage(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_SKY) {
         FRAMES.clear();
         STAMPS.clear();
         VIEW_ROT.set(event.getPoseStack().last().pose());
         VIEW.set(event.getPoseStack().last().pose()).invert();
         cam = event.getCamera().getPosition();
         inWorld = true;
      } else if (event.getStage() == Stage.AFTER_ENTITIES || event.getStage() == Stage.AFTER_LEVEL) {
         inWorld = false;
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
      if (inWorld && !WANT.isEmpty()) {
         LivingEntity e = event.getEntity();
         if (WANT.contains(e.getId()) && !e.isInvisible()) {
            float pt = event.getPartialTick();
            EntityModel ex = event.getRenderer().getModel();
            if (ex instanceof HumanoidModel) {
               HumanoidModel<?> model = (HumanoidModel)ex;
               if (plain(e, pt)) {
                  try {
                     PassBody b = capture(e, model, pt, event.getPoseStack().last().pose());
                     if (b != null) {
                        FRAMES.put(e.getId(), b);
                        STAMPS.put(e.getId(), (double)e.tickCount + (double)pt);
                     }
                  } catch (RuntimeException var5) {
                     FRAMES.remove(e.getId());
                     STAMPS.remove(e.getId());
                  }

                  return;
               }
            }

         }
      }
   }
}
