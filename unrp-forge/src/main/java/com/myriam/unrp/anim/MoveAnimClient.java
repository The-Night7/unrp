package com.myriam.unrp.anim;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class MoveAnimClient {
   public static volatile boolean posesOff;
   static final double FADE = 3.0D;
   static final double QUICK = 0.5D;
   static final double MAX_LIFT = 6.0D;
   static final double STILL = 2.0D;
   static final double CALM = 6.0D;
   static final String[] SWING_THROUGH = new String[]{"cn_sovereign", "cn_fly", "ss_wake", "ds_flare", "lt_surge"};
   private static final Map<Integer, MoveAnimClient.State> STATES = new HashMap();
   private static final Set<PlayerModel<?>> DIRTY = Collections.newSetFromMap(new WeakHashMap());
   private static ClientLevel where;
   private static long mined = -1000L;

   private MoveAnimClient() {
   }

   public static void accept(MoveAnimPacket msg) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         if (mc.level != where) {
            where = mc.level;
            STATES.clear();
         }

         Entity e = mc.level.getEntity(msg.entity);
         if (e instanceof LivingEntity) {
            double now = (double)e.tickCount;
            MoveAnimClient.State s = (MoveAnimClient.State)STATES.get(msg.entity);
            if (s == null) {
               s = new MoveAnimClient.State();
               STATES.put(msg.entity, s);
            }

            boolean held = (msg.flags & 1) != 0;
            boolean mirror = (msg.flags & 2) != 0;
            if (msg.op == 0) {
               if (msg.clip < 0 || msg.clip >= MoveClips.CLIPS.length) {
                  return;
               }

               if (msg.at <= 0) {
                  s.played = Math.max(s.played, (double)(where.getGameTime() - (long)msg.at));
               }

               MoveAnimClient.Track t = new MoveAnimClient.Track(msg.clip, now - (double)msg.at, held, mirror);
               double len = Math.min(3.0D, Math.max(0.5D, (double)t.clip.fadeIn));
               if (held) {
                  s.base.set(t, now, len);
               } else {
                  s.over.set(t, now, len);
               }
            } else if (msg.op == 1) {
               MoveAnimClient.Track cur = s.base.cur;
               if (cur != null && cur.held && (msg.clip < 0 || cur.id == msg.clip)) {
                  double outro = cur.clip.loopEnd > cur.clip.loopStart ? (double)cur.clip.loopEnd : (double)cur.clip.dur;
                  s.base.set(new MoveAnimClient.Track(cur.id, now - outro, false, cur.mirror), now, 3.0D);
               }
            } else if (msg.op == 2) {
               stop(s.base, msg.clip, now);
               stop(s.over, msg.clip, now);
            }

         }
      }
   }

   public static double clipTime(int entity, boolean over, int clip, double age) {
      MoveAnimClient.State s = (MoveAnimClient.State)STATES.get(entity);
      if (s != null && clip >= 0) {
         MoveAnimClient.Track t = over ? s.over.cur : s.base.cur;
         return t != null && t.id == clip && !t.done(age) ? age - t.start : -1.0D;
      } else {
         return -1.0D;
      }
   }

   public static double weight(int entity, boolean over, int clip, double age) {
      MoveAnimClient.State s = (MoveAnimClient.State)STATES.get(entity);
      if (s != null && clip >= 0) {
         MoveAnimClient.Track t = over ? s.over.cur : s.base.cur;
         return t != null && t.id == clip && !t.done(age) ? MoveRig.weight(t.clip, age - t.start, t.held) : 0.0D;
      } else {
         return 0.0D;
      }
   }

   public static boolean holding(int entity, int clip) {
      MoveAnimClient.State s = (MoveAnimClient.State)STATES.get(entity);
      return s != null && s.base.cur != null && s.base.cur.id == clip && s.base.cur.held;
   }

   public static double played(int entity) {
      MoveAnimClient.State s = (MoveAnimClient.State)STATES.get(entity);
      return s == null ? -1.0E9D : s.played;
   }

   private static void stop(MoveAnimClient.Layer l, int clip, double now) {
      if (l.cur != null && (clip < 0 || l.cur.id == clip)) {
         l.prev = l.cur;
         l.fadeAt = now;
         l.fade = 3.0D;
         l.cur = null;
      }

   }

   public static void restore(PlayerModel<?> m) {
      if (DIRTY.remove(m)) {
         m.head.x = 0.0F;
         m.head.z = 0.0F;
         m.head.zRot = 0.0F;
         m.body.x = 0.0F;
         m.body.z = 0.0F;
         m.body.zRot = 0.0F;
         m.rightLeg.x = -1.9F;
         m.leftLeg.x = 1.9F;
      }
   }

   static boolean swingThrough(MoveClips.Clip clip, boolean mirror, boolean left) {
      int free = mirror ? MoveRig.mirrorFree(clip.free) : clip.free;
      if ((free & (left ? 8 : 4)) != 0) {
         return false;
      } else {
         for(String name : SWING_THROUGH) {
            if (name.equals(clip.name)) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean trackPose(MoveAnimClient.Track t, double age, float[] under, float[] van, int flags, float attack, boolean left, double limb, float[] out) {
      double tt = age - t.start;
      double w = MoveRig.weight(t.clip, tt, t.held) * MoveRig.paced(t.clip, limb);
      if (w <= 0.0D) {
         return false;
      } else {
         double[] ch = new double[56];
         MoveRig.sample(t.clip, tt, t.held, t.mirror, ch);
         float[] solved = new float[36];
         MoveRig.solve(t.clip, ch, van, flags, MoveRig.clipTime(t.clip, tt, t.held), t.mirror, solved);
         if (attack > 0.0F && swingThrough(t.clip, t.mirror, left)) {
            MoveRig.swing(solved, left, (double)attack, (double)van[3]);
         }

         MoveRig.blend(under, solved, w, out);
         return true;
      }
   }

   private static boolean layerPose(MoveAnimClient.Layer l, double age, float[] under, float[] van, int flags, double limb, float[] out) {
      return layerPose(l, age, under, van, flags, 0.0F, false, limb, out);
   }

   private static boolean layerPose(MoveAnimClient.Layer l, double age, float[] under, float[] van, int flags, float attack, boolean left, double limb, float[] out) {
      boolean has = l.cur != null && trackPose(l.cur, age, under, van, flags, attack, left, limb, out);
      if (!has) {
         System.arraycopy(under, 0, out, 0, 36);
      }

      if (l.prev != null) {
         double x = (age - l.fadeAt) / l.fade;
         float[] p = new float[36];
         if (x >= 0.0D && x < 1.0D && trackPose(l.prev, age, under, van, flags, attack, left, limb, p)) {
            float[] mixed = new float[36];
            MoveRig.blend(p, out, MoveRig.smooth01(x), mixed);
            System.arraycopy(mixed, 0, out, 0, 36);
            has = true;
         }
      }

      return has;
   }

   public static boolean evaluate(MoveAnimClient.State s, double age, float[] van, int flags, double limb, float[] out) {
      return evaluate(s, age, van, flags, 0.0F, false, limb, out);
   }

   public static boolean evaluate(MoveAnimClient.State s, double age, float[] van, int flags, float attack, boolean left, double limb, float[] out) {
      boolean a = !s.base.idle(age);
      boolean b = !s.over.idle(age);
      if (!a && !b) {
         return false;
      } else {
         float[] p1 = new float[36];
         boolean has1 = a && layerPose(s.base, age, van, van, flags, attack, left, limb, p1);
         if (!b) {
            if (has1) {
               System.arraycopy(p1, 0, out, 0, 36);
            }

            return has1;
         } else {
            boolean has2 = layerPose(s.over, age, has1 ? p1 : van, van, flags, limb, out);
            if (!has2 && has1) {
               System.arraycopy(p1, 0, out, 0, 36);
            }

            return has1 || has2;
         }
      }
   }

   private static double trackChannels(MoveAnimClient.Track t, double age, double limb, double[] out) {
      double tt = age - t.start;
      double w = MoveRig.weight(t.clip, tt, t.held) * MoveRig.paced(t.clip, limb);
      if (w <= 0.0D) {
         return 0.0D;
      } else {
         MoveRig.sample(t.clip, tt, t.held, t.mirror, out);
         return w;
      }
   }

   private static double layerChannels(MoveAnimClient.Layer l, double age, double limb, double[] out) {
      double[] c = new double[56];
      double w = l.cur != null ? trackChannels(l.cur, age, limb, c) : 0.0D;

      for(int i = 38; i < 54; ++i) {
         out[i] = c[i] * w;
      }

      double weight = w;
      if (l.prev != null) {
         double x = (age - l.fadeAt) / l.fade;
         double[] p = new double[56];
         double pw = x >= 0.0D && x < 1.0D ? trackChannels(l.prev, age, limb, p) : 0.0D;
         if (pw > 0.0D) {
            double k = MoveRig.smooth01(x);

            for(int i = 38; i < 54; ++i) {
               out[i] = p[i] * pw + (out[i] - p[i] * pw) * k;
            }

            weight = Math.max(w, pw * (1.0D - k));
         }
      }

      return weight;
   }

   static double[] channels(int entity, double age, double limb) {
      MoveAnimClient.State s = (MoveAnimClient.State)STATES.get(entity);
      if (s == null) {
         return null;
      } else {
         boolean a = !s.base.idle(age);
         boolean b = !s.over.idle(age);
         if (!a && !b) {
            return null;
         } else {
            double[] out = new double[56];
            if (a) {
               layerChannels(s.base, age, limb, out);
            }

            if (b) {
               double[] o = new double[56];
               double wo = Math.min(1.0D, layerChannels(s.over, age, limb, o));

               for(int i = 38; i < 54; ++i) {
                  out[i] = out[i] * (1.0D - wo) + o[i];
               }
            }

            return out;
         }
      }
   }

   public static void apply(PlayerModel<?> m, LivingEntity entity, float limbSwing, float limbAmount, float age, float headYaw, float headPitch) {
      if (age != 0.0F || limbSwing != 0.0F || headYaw != 0.0F || headPitch != 0.0F) {
         if (!STATES.isEmpty() && !posesOff) {
            MoveAnimClient.State s = (MoveAnimClient.State)STATES.get(entity.getId());
            if (s != null) {
               if (!entity.isSleeping() && !entity.isFallFlying() && !entity.isVisuallySwimming()) {
                  float[] van = read(m);
                  int flags = (entity.isCrouching() ? 1 : 0) | (entity.isPassenger() ? 2 : 0);
                  float attack = 0.0F;
                  boolean left = false;
                  if (entity instanceof Player) {
                     Player pl = (Player)entity;
                     if (m.attackTime > 0.0F) {
                        HumanoidArm arm = pl.swingingArm == InteractionHand.MAIN_HAND ? pl.getMainArm() : pl.getMainArm().getOpposite();
                        attack = m.attackTime;
                        left = arm == HumanoidArm.LEFT;
                     }
                  }

                  float[] out = new float[36];

                  try {
                     if (!evaluate(s, (double)age, van, flags, attack, left, (double)limbAmount, out)) {
                        return;
                     }
                  } catch (RuntimeException var14) {
                     STATES.remove(entity.getId());
                     return;
                  }

                  write(m, out);
                  DIRTY.add(m);
               }
            }
         }
      }
   }

   static float[] read(PlayerModel<?> m) {
      float[] v = new float[36];
      part(v, 0, m.head);
      part(v, 6, m.body);
      part(v, 12, m.rightArm);
      part(v, 18, m.leftArm);
      part(v, 24, m.rightLeg);
      part(v, 30, m.leftLeg);
      return v;
   }

   private static void part(float[] v, int o, ModelPart p) {
      v[o] = p.x;
      v[o + 1] = p.y;
      v[o + 2] = p.z;
      v[o + 3] = p.xRot;
      v[o + 4] = p.yRot;
      v[o + 5] = p.zRot;
   }

   private static void set(ModelPart p, float[] v, int o) {
      p.x = v[o];
      p.y = v[o + 1];
      p.z = v[o + 2];
      p.xRot = v[o + 3];
      p.yRot = v[o + 4];
      p.zRot = v[o + 5];
   }

   static void write(PlayerModel<?> m, float[] v) {
      set(m.head, v, 0);
      set(m.body, v, 6);
      set(m.rightArm, v, 12);
      set(m.leftArm, v, 18);
      set(m.rightLeg, v, 24);
      set(m.leftLeg, v, 30);
      m.hat.copyFrom(m.head);
      m.jacket.copyFrom(m.body);
      m.rightSleeve.copyFrom(m.rightArm);
      m.leftSleeve.copyFrom(m.leftArm);
      m.rightPants.copyFrom(m.rightLeg);
      m.leftPants.copyFrom(m.leftLeg);
   }

   public static void hand(AbstractClientPlayer player, InteractionHand hand, float partialTick, PoseStack pose) {
      if (!STATES.isEmpty() && player != null && !posesOff) {
         double[] ch = channels(player.getId(), (double)((float)player.tickCount + partialTick), (double)player.walkAnimation.speed(partialTick));
         if (ch != null) {
            HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
            boolean right = arm == HumanoidArm.RIGHT;
            int o = right ? 38 : 44;
            float side = right ? 1.0F : -1.0F;
            pose.translate(ch[o] / 16.0D * (double)side, ch[o + 1] / 16.0D, ch[o + 2] / 16.0D);
            pose.mulPose(Axis.XP.rotationDegrees((float)(-ch[o + 3])));
            pose.mulPose(Axis.YP.rotationDegrees((float)(-ch[o + 4] * (double)side)));
            pose.mulPose(Axis.ZP.rotationDegrees((float)(ch[o + 5] * (double)side)));
         }
      }
   }

   private static double effectScale(Minecraft mc) {
      try {
         return Math.max(0.0D, Math.min(1.0D, mc.options.screenEffectScale().get()));
      } catch (RuntimeException var2) {
         return 1.0D;
      }
   }

   static double stride(Entity entity, float partialTick) {
      double var10000;
      if (entity instanceof LivingEntity live) {
         var10000 = (double)live.walkAnimation.speed(partialTick);
      } else {
         var10000 = 1.0D;
      }

      return var10000;
   }

   public static void mined(long now) {
      mined = now;
   }

   static double calm(long now, double pt) {
      double since = (double)now + pt - (double)mined;
      if (!(since < 0.0D) && !(since >= 8.0D)) {
         return since < 2.0D ? 0.0D : (since - 2.0D) / 6.0D;
      } else {
         return 1.0D;
      }
   }

   public static double cameraLift(Entity entity, float partialTick, boolean detached) {
      Minecraft mc = Minecraft.getInstance();
      if (!detached && !STATES.isEmpty() && entity != null && entity == mc.player && !posesOff) {
         double[] ch = channels(entity.getId(), (double)((float)entity.tickCount + partialTick), stride(entity, partialTick));
         if (ch == null) {
            return 0.0D;
         } else {
            double lift = Math.max(-6.0D, Math.min(6.0D, ch[53]));
            return lift / 16.0D * effectScale(mc) * calm(entity.level().getGameTime(), (double)partialTick);
         }
      } else {
         return 0.0D;
      }
   }

   @SubscribeEvent
   public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
      Minecraft mc = Minecraft.getInstance();
      if (!STATES.isEmpty() && mc.player != null && mc.options.getCameraType().isFirstPerson()) {
         if (event.getCamera().getEntity() == mc.player) {
            double[] ch = channels(mc.player.getId(), (double)mc.player.tickCount + event.getPartialTick(), (double)mc.player.walkAnimation.speed((float)event.getPartialTick()));
            if (ch != null) {
               double k = effectScale(mc) * calm(mc.player.level().getGameTime(), event.getPartialTick());
               event.setPitch((float)((double)event.getPitch() + ch[50] * k));
               event.setYaw((float)((double)event.getYaw() + ch[51] * k));
               event.setRoll((float)((double)event.getRoll() + ch[52] * k));
            }
         }
      }
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END && !STATES.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.level == where) {
            if (mc.level.getGameTime() % 20L == 0L) {
               Iterator<Map.Entry<Integer, MoveAnimClient.State>> it = STATES.entrySet().iterator();

               while(it.hasNext()) {
                  Map.Entry<Integer, MoveAnimClient.State> e = (Map.Entry)it.next();
                  Entity en = mc.level.getEntity(e.getKey());
                  if (en != null && en.isAlive()) {
                     double age = (double)en.tickCount;
                     MoveAnimClient.State s = (MoveAnimClient.State)e.getValue();
                     if (s.base.idle(age) && s.over.idle(age)) {
                        it.remove();
                     }
                  } else {
                     it.remove();
                  }
               }

            }
         } else {
            STATES.clear();
         }
      }
   }

   static final class Layer {
      MoveAnimClient.Track cur;
      MoveAnimClient.Track prev;
      double fadeAt;
      double fade = 3.0D;

      void set(MoveAnimClient.Track t, double now, double len) {
         if (this.cur != null && !this.cur.done(now)) {
            this.prev = this.cur;
            this.fadeAt = now;
            this.fade = len;
         } else {
            this.prev = null;
         }

         this.cur = t;
      }

      boolean idle(double age) {
         if (this.cur != null && this.cur.done(age)) {
            this.cur = null;
         }

         if (this.prev != null && (age - this.fadeAt >= this.fade || age < this.fadeAt || this.prev.done(age))) {
            this.prev = null;
         }

         return this.cur == null && this.prev == null;
      }
   }

   static final class State {
      final MoveAnimClient.Layer base = new MoveAnimClient.Layer();
      final MoveAnimClient.Layer over = new MoveAnimClient.Layer();
      double played = -1.0E9D;
   }

   static final class Track {
      final MoveClips.Clip clip;
      final int id;
      final double start;
      final boolean held;
      final boolean mirror;

      Track(int id, double start, boolean held, boolean mirror) {
         this.clip = MoveClips.CLIPS[id];
         this.id = id;
         this.start = start;
         this.held = held;
         this.mirror = mirror;
      }

      boolean done(double age) {
         return !this.held && age - this.start >= (double)this.clip.dur;
      }
   }
}
