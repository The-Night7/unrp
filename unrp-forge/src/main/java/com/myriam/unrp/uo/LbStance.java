package com.myriam.unrp.uo;



import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class LbStance {
   public static final String[] STANCE = new String[]{"Discreet", "Grounded", "Centered"};
   public static final int T_YAW = 0;
   public static final int T_HIP = 1;
   public static final int T_LEAN = 2;
   public static final int T_HEAD_X = 3;
   public static final int T_ARM_OUT = 4;
   public static final int T_ARM_X = 5;
   public static final int T_WIDTH = 6;
   public static final int T_STEP = 7;
   public static final int T_RATE = 8;
   public static final int T_DEPTH = 9;
   public static final int T_B_LEAN = 10;
   public static final int T_DRIFT = 11;
   public static final int T_DRIFT_RATE = 12;
   public static final int T_EVERY = 13;
   public static final int T_SPAN = 14;
   public static final int T_ATTACK = 15;
   public static final int T_RELEASE = 16;
   public static final int G_YAW = 17;
   public static final int G_RISE = 18;
   public static final int G_OUT = 19;
   public static final int G_HEAD_X = 20;
   private static final float[][] TURN = new float[][]{{0.2F, 0.5F, 0.0F, 0.0F, 0.05F, 0.04F, 0.03F, 0.025F, 0.06F, 0.3F, 0.006F, 0.015F, 0.023F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F}, {0.32F, 0.55F, 0.035F, 0.06F, 0.08F, 0.05F, 0.07F, 0.04F, 0.085F, 0.45F, 0.012F, 0.02F, 0.029F, 190.0F, 70.0F, 0.3F, 0.35F, -0.08F, 0.4F, 0.03F, 0.0F}, {0.36F, 0.6F, 0.0F, -0.02F, 0.06F, 0.04F, 0.045F, 0.035F, 0.05F, 0.4F, 0.01F, 0.015F, 0.019F, 260.0F, 90.0F, 0.4F, 0.4F, 0.0F, 0.45F, 0.03F, -0.05F}};
   private static final float IN = 0.055F;
   private static final float OUT = 0.22F;
   private static final Map<Integer, float[]> LIVE = new HashMap();
   private static final Map<PlayerModel<?>, float[]> LAID = new IdentityHashMap();

   private LbStance() {
   }

   public static int stanceOf(int form) {
      if (form == 8) {
         return 1;
      } else {
         return form == 14 ? 2 : 0;
      }
   }

   public static float[] row(int form) {
      return LimitBreakerForms.valid(form) && !LimitBreakerForms.apeForm(form) ? TURN[stanceOf(form)] : null;
   }

   public static float gesture(float[] q, float age) {
      if (!(q[13] <= 0.0F) && !(q[14] <= 0.0F)) {
         float p = age % q[13];
         return p >= q[14] ? -1.0F : p / q[14];
      } else {
         return -1.0F;
      }
   }

   private static float smooth(float x) {
      float c = Mth.clamp(x, 0.0F, 1.0F);
      return c * c * (3.0F - 2.0F * c);
   }

   public static float envelope(float u, float attack, float release) {
      if (!(u <= 0.0F) && !(u >= 1.0F)) {
         if (u < attack) {
            return smooth(u / attack);
         } else {
            return u > 1.0F - release ? smooth((1.0F - u) / release) : 1.0F;
         }
      } else {
         return 0.0F;
      }
   }

   public static float burst(int form, float age) {
      float[] q = row(form);
      if (q == null) {
         return 0.0F;
      } else {
         float u = gesture(q, age);
         return u < 0.0F ? 0.0F : envelope(u, q[15], q[16]);
      }
   }

   public static float weight(Player player) {
      float[] s = (float[])LIVE.get(ClientLbFormCache.stateKey(player.getId()));
      if (s == null) {
         return 0.0F;
      } else {
         float w = Mth.clamp(s[0], 0.0F, 1.0F);
         return w * w * (3.0F - 2.0F * w);
      }
   }

   static float[] kept(PlayerModel<?> m) {
      return new float[]{m.rightLeg.x, m.leftLeg.x, m.body.x, m.body.z, m.head.x, m.head.z};
   }

   static void lift(PlayerModel<?> m) {
      float[] l = (float[])LAID.remove(m);
      if (l != null) {
         float[] now = kept(m);
         if (now[0] == l[0]) {
            m.rightLeg.x -= l[6];
         }

         if (now[1] == l[1]) {
            m.leftLeg.x -= l[7];
         }

         if (now[2] == l[2]) {
            m.body.x -= l[8];
         }

         if (now[3] == l[3]) {
            m.body.z -= l[9];
         }

         if (now[4] == l[4]) {
            m.head.x -= l[10];
         }

         if (now[5] == l[5]) {
            m.head.z -= l[11];
         }

      }
   }

   static void lay(PlayerModel<?> m, float[] before) {
      float[] after = kept(m);
      float[] l = new float[12];

      for(int i = 0; i < 6; ++i) {
         l[i] = after[i];
         l[i + 6] = after[i] - before[i];
      }

      LAID.put(m, l);
   }

   public static void apply(PlayerModel<?> m, LivingEntity entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch) {
      lift(m);
      if (entity instanceof Player player) {
         if (age != 0.0F || limbSwing != 0.0F || headYaw != 0.0F || headPitch != 0.0F) {
            int form = ClientLbFormCache.form(player.getUUID());
            if (form > 0 && !LimitBreakerForms.apeForm(form)) {
               float[] q = row(form);
               if (q != null) {
                  boolean still = limbSwingAmount < 0.06F && player.onGround() && !player.isCrouching() && !player.swinging && !player.isUsingItem() && !player.isPassenger() && !player.isVisuallySwimming() && !player.isFallFlying() && !player.isSleeping() && !player.isInWater();
                  int key = ClientLbFormCache.stateKey(player.getId());
                  float[] s = (float[])LIVE.get(key);
                  if (s == null) {
                     s = new float[]{0.0F, age};
                     LIVE.put(key, s);
                     sweep();
                  }

                  float dt = Mth.clamp(age - s[1], 0.0F, 4.0F);
                  s[1] = age;
                  float target = still ? 1.0F : 0.0F;
                  float rate = still ? 0.055F : 0.22F;
                  s[0] += Mth.clamp(target - s[0], -rate * dt, rate * dt);
                  float w = s[0] * s[0] * (3.0F - 2.0F * s[0]);
                  if (!(w <= 0.001F)) {
                     boolean armsFree = m.rightArmPose == ArmPose.EMPTY || m.rightArmPose == ArmPose.ITEM;
                     boolean leftFree = m.leftArmPose == ArmPose.EMPTY || m.leftArmPose == ArmPose.ITEM;
                     float[] before = kept(m);
                     turn(m, q, age, w, armsFree, leftFree);
                     lay(m, before);
                     m.hat.copyFrom(m.head);
                     m.jacket.copyFrom(m.body);
                     m.rightSleeve.copyFrom(m.rightArm);
                     m.leftSleeve.copyFrom(m.leftArm);
                     m.rightPants.copyFrom(m.rightLeg);
                     m.leftPants.copyFrom(m.leftLeg);
                  }
               }
            } else {
               LIVE.remove(ClientLbFormCache.stateKey(player.getId()));
            }
         }
      }
   }

   private static void turn(PlayerModel<?> m, float[] q, float age, float w, boolean armsFree, boolean leftFree) {
      float br = Mth.sin(age * q[8]);
      float u = gesture(q, age);
      float e = u < 0.0F ? 0.0F : envelope(u, q[15], q[16]);
      float yaw = (q[0] + q[11] * Mth.sin(age * q[12]) + q[17] * e) * w;
      float hips = q[0] * q[1] * w;
      float lean = (q[2] + q[10] * br) * w;
      float width = q[6] * w;
      float step = q[7] * w;
      float sh = Mth.sin(hips);
      float ch = Mth.cos(hips);
      float hip = 12.0F * (1.0F - (Mth.cos(step) * Mth.cos(width) + Mth.sin(step) * sh * Mth.sin(width)));
      m.rightLeg.x += 1.9F - 1.9F * ch;
      m.rightLeg.y += hip;
      m.rightLeg.z += 1.9F * sh;
      m.leftLeg.x += 1.9F * ch - 1.9F;
      m.leftLeg.y += hip;
      m.leftLeg.z -= 1.9F * sh;
      m.rightLeg.yRot += hips;
      m.leftLeg.yRot += hips;
      m.rightLeg.zRot += width;
      m.leftLeg.zRot -= width;
      m.rightLeg.xRot += step;
      m.leftLeg.xRot -= step;
      float cl = Mth.cos(lean);
      float sl = Mth.sin(lean);
      float cy = Mth.cos(yaw);
      float sy = Mth.sin(yaw);
      float bx = -12.0F * sl * sy;
      float by = hip + 12.0F * (1.0F - cl);
      float bz = -12.0F * sl * cy;
      m.body.x += bx;
      m.body.y += by;
      m.body.z += bz;
      m.body.xRot += lean;
      m.body.yRot += yaw;
      float rise = (q[9] * 0.5F * (br + 1.0F) + q[18] * e) * w;
      m.head.x += bx;
      m.head.y += by - 0.25F * rise;
      m.head.z += bz;
      m.head.xRot += (q[3] + q[20] * e) * w;
      m.rightArm.x += bx + 5.0F - 5.0F * cy + 2.0F * sl * sy;
      m.rightArm.y += by + 2.0F * cl - 2.0F - rise;
      m.rightArm.z += bz + 5.0F * sy + 2.0F * sl * cy;
      m.leftArm.x += bx - 5.0F + 5.0F * cy + 2.0F * sl * sy;
      m.leftArm.y += by + 2.0F * cl - 2.0F - rise;
      m.leftArm.z += bz - 5.0F * sy + 2.0F * sl * cy;
      float out = (q[4] + q[19] * e) * w;
      float swing = q[5] * w;
      if (armsFree) {
         m.rightArm.xRot += swing;
         m.rightArm.yRot += yaw;
         m.rightArm.zRot += out;
      }

      if (leftFree) {
         m.leftArm.xRot -= swing;
         m.leftArm.yRot += yaw;
         m.leftArm.zRot -= out;
      }

   }

   public static void clearAll() {
      LIVE.clear();
   }

   private static void sweep() {
      if (LIVE.size() >= 24) {
         Iterator<Map.Entry<Integer, float[]>> it = LIVE.entrySet().iterator();

         while(it.hasNext()) {
            if (((float[])((Map.Entry)it.next()).getValue())[0] <= 0.001F) {
               it.remove();
            }
         }

      }
   }
}
