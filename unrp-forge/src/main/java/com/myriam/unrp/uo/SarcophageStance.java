package com.myriam.unrp.uo;

import com.myriam.unrp.pass.UnrpPowerLayers;


import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class SarcophageStance {
   public static final float FLARE_OUT = 0.24F;
   public static final float FLARE_FWD = 0.18F;
   public static final float HANG = 0.07F;
   public static final float SWING = 0.7F;
   public static final float BRACE = 0.2F;
   private static final float IN = 0.06F;
   private static final float OUT = 0.25F;
   private static final Map<Integer, float[]> LIVE = new HashMap();

   private SarcophageStance() {
   }

   public static void apply(PlayerModel<?> m, LivingEntity entity, float limbSwing, float amount, float age) {
      if (entity instanceof AbstractClientPlayer player) {
         if (age != 0.0F || limbSwing != 0.0F) {
            int variant = UnrpPowerLayers.powerVariant(player, "sarcophage");
            if (variant != 0 && UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "tempest")) {
               float[] s = (float[])LIVE.get(player.getId());
               if (s == null) {
                  s = new float[]{0.0F, age, 0.0F};
                  LIVE.put(player.getId(), s);
               }

               float dt = Mth.clamp(age - s[1], 0.0F, 4.0F);
               s[1] = age;
               boolean grabbing = UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "grabbing");
               boolean still = amount < 0.06F && player.onGround() && !player.isCrouching() && !player.swinging && !player.isUsingItem() || grabbing;
               float target = still ? 1.0F : 0.0F;
               float rate = still ? 0.06F : 0.25F;
               s[0] += Mth.clamp(target - s[0], -rate * dt, rate * dt);
               float w = s[0] * s[0] * (3.0F - 2.0F * s[0]);
               float bt = grabbing ? 1.0F : 0.0F;
               float br = grabbing ? 0.06F : 0.25F;
               s[2] += Mth.clamp(bt - s[2], -br * dt, br * dt);
               float brace = s[2] * s[2] * (3.0F - 2.0F * s[2]);
               float swing = Mth.cos(limbSwing * 0.6662F) * amount * (1.0F - Mth.lerp(brace, 0.7F, 0.2F));
               float hang = 0.07F * Mth.clamp(amount / 0.5F, 0.0F, 1.0F);
               boolean rightFree = m.rightArmPose == ArmPose.EMPTY || m.rightArmPose == ArmPose.ITEM;
               boolean leftFree = m.leftArmPose == ArmPose.EMPTY || m.leftArmPose == ArmPose.ITEM;
               if (rightFree) {
                  m.rightArm.xRot += swing - 0.18F * w;
                  m.rightArm.zRot += hang + 0.24F * w;
               }

               if (leftFree) {
                  m.leftArm.xRot += -swing - 0.18F * w;
                  m.leftArm.zRot -= hang + 0.24F * w;
               }

               m.rightSleeve.copyFrom(m.rightArm);
               m.leftSleeve.copyFrom(m.leftArm);
            } else {
               LIVE.remove(player.getId());
            }
         }
      }
   }

   public static void clearAll() {
      LIVE.clear();
   }
}
