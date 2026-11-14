package com.myriam.unrp.uo;



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;

public final class PhageState {
   public static final double WALK_FULL = 0.85D;
   public static final double CYCLE_PX = 13.548387096774194D;
   private static final int FORGET = 200;
   private static final Map<Integer, PhageState.S> LIVE = new HashMap();

   private PhageState() {
   }

   private static PhageState.S of(AbstractClientPlayer player) {
      PhageState.S s = (PhageState.S)LIVE.get(player.getId());
      if (s == null) {
         s = new PhageState.S();
         LIVE.put(player.getId(), s);
         sweep(player.level().getGameTime());
      }

      return s;
   }

   public static void tick(AbstractClientPlayer player) {
      PhageState.S s = of(player);
      s.seen = player.level().getGameTime();
      double dx = player.getX() - player.xo;
      double dz = player.getZ() - player.zo;
      double v = Math.sqrt(dx * dx + dz * dz);
      double scale = Math.max(0.25D, (double)player.getBbWidth() / 0.6D);
      double px = v * 16.0D / (0.9375D * scale);
      double target = player.onGround() ? Mth.clamp(px / 0.85D, 0.0D, 1.0D) : 0.0D;
      s.walk += Mth.clamp(target - s.walk, -0.12D, 0.12D);
      s.gaitRate = px / 13.548387096774194D;
      s.gait += s.gaitRate;
      s.wave += s.speed / 20.0D;
   }

   public static void speed(AbstractClientPlayer player, double speed) {
      PhageState.S s = (PhageState.S)LIVE.get(player.getId());
      if (s != null) {
         s.speed = speed;
      }

   }

   public static PhageMotion.Drive drive(AbstractClientPlayer player, float partial, long now) {
      PhageState.S s = of(player);
      PhageMotion.Drive d = new PhageMotion.Drive();
      d.t = (double)((float)player.tickCount + partial) / 20.0D;
      d.wave = s.wave + s.speed * (double)partial / 20.0D;
      d.gait = s.gait + s.gaitRate * (double)partial;
      float head = Mth.rotLerp(partial, player.yHeadRotO, player.yHeadRot);
      float body = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
      d.lookYaw = Math.toRadians((double)Mth.wrapDegrees(head - body));
      d.lookPitch = Math.toRadians((double)Mth.lerp(partial, player.xRotO, player.getXRot()));
      double walk = s.walk;
      ClientSarcophage.Move m = ClientSarcophage.move(player.getId(), now);
      if (m != null) {
         double tau = (double)((float)now + partial - (float)m.start);
         PhageTimeline.Pose p = PhageTimeline.at(m.move, tau, (double)(m.end - m.start), m.hold, m.rampIn);
         if (p.move != 0) {
            d.move = p.move;
            d.c = p.c;
            d.swim = p.swim;
            d.weight = p.weight;
            walk = p.planted ? walk * (1.0D - p.weight) : 0.0D;
         }
      }

      d.walk = walk;
      return d;
   }

   public static void forget(AbstractClientPlayer player) {
      LIVE.remove(player.getId());
   }

   public static void clearAll() {
      LIVE.clear();
   }

   private static void sweep(long now) {
      if (LIVE.size() >= 16) {
         Iterator<Map.Entry<Integer, PhageState.S>> it = LIVE.entrySet().iterator();

         while(it.hasNext()) {
            if (now - ((PhageState.S)((Map.Entry)it.next()).getValue()).seen > 200L) {
               it.remove();
            }
         }

      }
   }

   private static final class S {
      double gait;
      double gaitRate;
      double walk;
      double wave;
      double speed = 1.0D;
      long seen;
   }
}
