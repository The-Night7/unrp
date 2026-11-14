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
public final class BodyCapture {
   private static final Map<Integer, BodyCapture.Body> BODIES = new HashMap();

   private BodyCapture() {
   }

   public static BodyCapture.Body get(int id, double age) {
      BodyCapture.Body b = (BodyCapture.Body)BODIES.get(id);
      return b != null && Math.abs(b.stamp - age) < 0.001D ? b : null;
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_SKY) {
         BODIES.clear();
      }

   }

   @SubscribeEvent
   public static void onRenderPlayer(RenderPlayerEvent.Post event) {
      Player pt_0 = event.getEntity();
      if (pt_0 instanceof AbstractClientPlayer p) {
         if (!p.isSleeping() && p.deathTime <= 0) {
            float pt = event.getPartialTick();
            PlayerModel<AbstractClientPlayer> model = (PlayerModel)event.getRenderer().getModel();
            float body = Mth.rotLerp(pt, p.yBodyRotO, p.yBodyRot);
            Vec3 base = p.getPosition(pt).add(event.getRenderer().getRenderOffset(p, pt));
            Vec3[] right = new Vec3[4];
            Vec3[] left = new Vec3[4];
            double[][] r = SaltShape.shoulderLocal(1);
            double[][] l = SaltShape.shoulderLocal(-1);

            for(int i = 0; i < 4; ++i) {
               right[i] = base.add(partPoint(body, model.body, r[i]));
               left[i] = base.add(partPoint(body, model.body, l[i]));
            }

            Vec3 mouth = base.add(partPoint(body, model.head, SaltShape.MOUTH));
            BODIES.put(p.getId(), new BodyCapture.Body(right, left, mouth, (double)((float)p.tickCount + pt)));
            return;
         }
      }

   }

   public static Vec3 partPoint(float bodyYaw, ModelPart part, double[] px) {
      Matrix4f m = new Matrix4f();
      m.rotate(Axis.YP.rotationDegrees(180.0F - bodyYaw));
      m.scale(-1.0F, -1.0F, 1.0F);
      m.scale(0.9375F);
      m.translate(0.0F, -1.501F, 0.0F);
      m.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
      m.rotate((new Quaternionf()).rotationZYX(part.zRot, part.yRot, part.xRot));
      Vector3f v = m.transformPosition(new Vector3f((float)px[0] / 16.0F, (float)px[1] / 16.0F, (float)px[2] / 16.0F));
      return new Vec3((double)v.x(), (double)v.y(), (double)v.z());
   }

   public static Vec3 restPoint(float bodyYaw, double[] px) {
      Matrix4f m = new Matrix4f();
      m.rotate(Axis.YP.rotationDegrees(180.0F - bodyYaw));
      m.scale(-1.0F, -1.0F, 1.0F);
      m.scale(0.9375F);
      m.translate(0.0F, -1.501F, 0.0F);
      Vector3f v = m.transformPosition(new Vector3f((float)px[0] / 16.0F, (float)px[1] / 16.0F, (float)px[2] / 16.0F));
      return new Vec3((double)v.x(), (double)v.y(), (double)v.z());
   }

   public static final class Body {
      public final Vec3[] right;
      public final Vec3[] left;
      public final Vec3 mouth;
      final double stamp;

      Body(Vec3[] right, Vec3[] left, Vec3 mouth, double stamp) {
         this.right = right;
         this.left = left;
         this.mouth = mouth;
         this.stamp = stamp;
      }
   }
}
