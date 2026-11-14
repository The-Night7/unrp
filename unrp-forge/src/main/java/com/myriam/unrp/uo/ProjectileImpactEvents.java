package com.myriam.unrp.uo;

import com.myriam.unrp.pass.PassEvents;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/** ProjectileImpactEvents du modèle, réduit aux fonctions utilisées. Renvoi d'un projectile par une garde. */
@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public final class ProjectileImpactEvents {
   private ProjectileImpactEvents() {}

   public static void deflect(ServerLevel level, Projectile projectile, ServerPlayer guard) {
      projectile.addTag("unrp_deflected");
      Vec3 back = projectile.getDeltaMovement().scale(-0.6D);
      Vec3 away = projectile.position().subtract(guard.position());
      Vec3 flat = new Vec3(away.x, 0.0D, away.z);
      if (flat.lengthSqr() > 0.01D) {
         flat = flat.normalize().scale(back.length());
         back = new Vec3(flat.x, Math.max(0.05D, back.y), flat.z);
      }

      projectile.setDeltaMovement(back);
      projectile.hurtMarked = true;
      projectile.setOwner(guard);
      PassEvents.redirect(level, projectile);
      level.sendParticles(ParticleTypes.CRIT, projectile.getX(), projectile.getY(), projectile.getZ(), 6, 0.2D, 0.2D, 0.2D, 0.08D);
      level.playSound((Player)null, projectile.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.7F, 1.3F);
   }
}
