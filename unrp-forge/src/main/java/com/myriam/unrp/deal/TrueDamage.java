package com.myriam.unrp.deal;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class TrueDamage {
   public static final ResourceKey<DamageType> TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("unrp", "true"));

   private TrueDamage() {
   }

   public static DamageSource source(ServerLevel level, Entity attacker) {
      return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TYPE), attacker);
   }

   public static boolean deal(ServerLevel level, Entity attacker, LivingEntity target, float amount) {
      if (!(amount <= 0.0F) && !target.isDeadOrDying()) {
         int held = target.invulnerableTime;
         target.invulnerableTime = 0;
         boolean landed = target.hurt(source(level, attacker), amount);
         if (!landed) {
            target.invulnerableTime = held;
         }

         return landed;
      } else {
         return false;
      }
   }
}
