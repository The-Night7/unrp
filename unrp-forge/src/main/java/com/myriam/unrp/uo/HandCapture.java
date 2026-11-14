package com.myriam.unrp.uo;



import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
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
public final class HandCapture {
   private static final Map<Integer, HandCapture.Hands> HANDS = new HashMap();

   private HandCapture() {
   }

   public static HandCapture.Hands get(int id, double age) {
      HandCapture.Hands h = (HandCapture.Hands)HANDS.get(id);
      return h != null && Math.abs(h.stamp - age) < 0.001D ? h : null;
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_SKY) {
         HANDS.clear();
      }

   }

   @SubscribeEvent
   public static void onRenderPlayer(RenderPlayerEvent.Post event) {
      Player pt = event.getEntity();
      if (pt instanceof AbstractClientPlayer p) {
         float var12 = event.getPartialTick();
         if (!p.isFallFlying() && !(p.getSwimAmount(var12) > 0.0F) && !p.isAutoSpinAttack() && !p.isSleeping() && !p.isPassenger() && p.deathTime <= 0) {
            PlayerModel<AbstractClientPlayer> model = (PlayerModel)event.getRenderer().getModel();
            float body = Mth.rotLerp(var12, p.yBodyRotO, p.yBodyRot);
            float hx = "slim".equals(p.getModelName()) ? 0.5F : 1.0F;
            ModelPart r = model.rightArm;
            ModelPart l = model.leftArm;
            Vec3[] fr = handFrame(body, r.x, r.y, r.z, r.xRot, r.yRot, r.zRot, -hx);
            Vec3[] fl = handFrame(body, l.x, l.y, l.z, l.xRot, l.yRot, l.zRot, hx);
            if (!(fr[1].lengthSqr() < 1.0E-6D) && !(fl[1].lengthSqr() < 1.0E-6D)) {
               Quaternionf lean = LashLean.world(p, body, var12);
               if (lean != null) {
                  fr = turned(fr, lean);
                  fl = turned(fl, lean);
               }

               Vec3 base = p.getPosition(var12).add(event.getRenderer().getRenderOffset(p, var12));
               HANDS.put(p.getId(), new HandCapture.Hands(base.add(fr[0]), fr[1].normalize(), base.add(fl[0]), fl[1].normalize(), (double)((float)p.tickCount + var12)));
            }
         }
      }
   }

   public static Quaternionf around(float bodyYaw, Quaternionf turn) {
      float a = (180.0F - bodyYaw) * ((float)Math.PI / 180F);
      return (new Quaternionf()).rotationY(a).mul(turn).rotateY(-a);
   }

   public static Vec3[] turned(Vec3[] frame, Quaternionf q) {
      Vec3[] out = new Vec3[frame.length];

      for(int i = 0; i < frame.length; ++i) {
         Vector3f v = q.transform(new Vector3f((float)frame[i].x, (float)frame[i].y, (float)frame[i].z));
         out[i] = new Vec3((double)v.x(), (double)v.y(), (double)v.z());
      }

      return out;
   }

   public static Vec3[] handFrame(float bodyYaw, float px, float py, float pz, float xRot, float yRot, float zRot, float hx) {
      Matrix4f m = new Matrix4f();
      m.rotate(Axis.YP.rotationDegrees(180.0F - bodyYaw));
      m.scale(-1.0F, -1.0F, 1.0F);
      m.scale(0.9375F);
      m.translate(0.0F, -1.501F, 0.0F);
      m.translate(px / 16.0F, py / 16.0F, pz / 16.0F);
      m.rotate((new Quaternionf()).rotationZYX(zRot, yRot, xRot));
      Vector3f hand = m.transformPosition(new Vector3f(hx / 16.0F, 0.625F, 0.0F));
      Vector3f top = m.transformPosition(new Vector3f(hx / 16.0F, 0.0F, 0.0F));
      return new Vec3[]{new Vec3((double)hand.x(), (double)hand.y(), (double)hand.z()), new Vec3((double)(hand.x() - top.x()), (double)(hand.y() - top.y()), (double)(hand.z() - top.z()))};
   }

   public static final class Hands {
      public final Vec3 right;
      public final Vec3 rightDir;
      public final Vec3 left;
      public final Vec3 leftDir;
      final double stamp;

      Hands(Vec3 right, Vec3 rightDir, Vec3 left, Vec3 leftDir, double stamp) {
         this.right = right;
         this.rightDir = rightDir;
         this.left = left;
         this.leftDir = leftDir;
         this.stamp = stamp;
      }
   }
}
