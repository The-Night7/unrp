package com.myriam.unrp.pass;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "unrp")
public class BarrierEvents {
   /** Niveau 1 à 10 (power_level / 10 du modèle). */
   static float levelOf(ServerPlayer player) {
      double l = com.myriam.unrp.data.ClaimData.get(player.server).profileOf(player.getUUID()).map(pr -> pr.level).orElse(1.0);
      return (float) Math.max(1.0, Math.min(10.0, l));
   }

   /** Statistique en dixièmes (tags « sp_<stat>.N » du modèle) : nos stats sont Puissance, Défense, Récupération, Ruse, Vitesse. */
   private static int statTenths(ServerPlayer player, String key) {
      int[] st = com.myriam.unrp.ability.AbilityEngine.currentStats(player);
      if (st == null) return 0;
      int i = switch (key) { case "power" -> 0; case "defense" -> 1; case "recovery" -> 2; case "trick" -> 3; case "speed" -> 4; default -> -1; };
      return i < 0 || i >= st.length ? 0 : st[i] * 10;
   }

   private static void drainAura(ServerPlayer player, int amount) {
      com.myriam.unrp.ability.AbilityEngine.spendAura(player, amount);
   }

   private static int auraOf(ServerPlayer player) {
      return com.myriam.unrp.ability.AbilityEngine.aura(player);
   }

   private static void addAura(ServerPlayer player, int amount) {
      com.myriam.unrp.ability.AbilityEngine.refundAura(player, amount);
   }

   private static List<BarrierEntity> ownedBarriers(ServerPlayer player, double range) {
      return player.serverLevel().getEntitiesOfClass(BarrierEntity.class, player.getBoundingBox().inflate(range), (b) -> b.isAlive() && player.getUUID().equals(b.getOwnerId()));
   }

   private static float maxHpFor(ServerPlayer player) {
      return 40.0F + levelOf(player) * 10.0F + (float)statTenths(player, "defense") / 10.0F * 4.0F;
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player pl0 = event.player;
         if (pl0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)pl0;
            ServerLevel level = player.serverLevel();
            long now = level.getGameTime();
            boolean barrierActive = player.getTags().contains("unrp.active.barrier");
            if (barrierActive) {
               player.getPersistentData().putBoolean("unrp_barrier_was", true);
            } else if (player.getPersistentData().getBoolean("unrp_barrier_was")) {
               player.getPersistentData().putBoolean("unrp_barrier_was", false);

               for(BarrierEntity b : ownedBarriers(player, 128.0D)) {
                  if (!b.getTags().contains("unrp.sanctuary")) {
                     b.dissipate();
                  }
               }
            }

            if (player.getTags().contains("unrp.barrier_deploy_trigger")) {
               player.removeTag("unrp.barrier_deploy_trigger");
               boolean external = player.isShiftKeyDown();
               List<BarrierEntity> owned = ownedBarriers(player, 64.0D);
               Vec3 eye0 = player.getEyePosition();
               Vec3 dir0 = player.getLookAngle();

               for(BarrierEntity b : owned) {
                  float r0 = b.getSize() / 2.0F;
                  Vec3 c0 = new Vec3(b.getX(), b.getY() + (double)r0, b.getZ());
                  Vec3 toB = c0.subtract(eye0);
                  double along = toB.dot(dir0);
                  if (!(along < 0.0D) && !(along > 28.0D)) {
                     double offAxis = toB.subtract(dir0.scale(along)).length();
                     if (offAxis < (double)r0 + 0.4D || b.isInside(player)) {
                        com.myriam.unrp.ability.Poses.play(player, "br_point");
                        BarrierPass.deploy(player, b, c0, 192);
                        b.dissipate();
                        addAura(player, 15);

                        player.displayClientMessage(Component.literal("§b◈ Barrière rappelée"), true);
                        return;
                     }
                  }
               }

               Vec3 spawnPos;
               int chosenSlot;
               if (!external) {
                  long selfLock = player.getPersistentData().getLong("unrp_barrier_self_lock");
                  if (now < selfLock) {
                     addAura(player, 15);
                     long secs = Math.max(1L, (selfLock - now + 19L) / 20L);
                     player.displayClientMessage(Component.literal("§cTa barrière se régénère (" + secs + " s)"), true);
                     return;
                  }

                  chosenSlot = 0;
                  spawnPos = player.position();

                  for(BarrierEntity b : owned) {
                     if (b.isSelfBarrier()) {
                        b.dissipate();
                     }
                  }
               } else {
                  BarrierEntity slot1 = null;
                  BarrierEntity slot2 = null;

                  for(BarrierEntity b : owned) {
                     if (!b.isSelfBarrier()) {
                        if (b.getSlot() == 1) {
                           slot1 = b;
                        } else if (b.getSlot() == 2) {
                           slot2 = b;
                        }
                     }
                  }

                  boolean lock1 = now < player.getPersistentData().getLong("unrp_barrier_ext_lock_0");
                  boolean lock2 = now < player.getPersistentData().getLong("unrp_barrier_ext_lock_1");
                  BarrierEntity toReplace = null;
                  chosenSlot = -1;
                  if (slot1 == null && !lock1) {
                     chosenSlot = 1;
                  } else if (slot2 == null && !lock2) {
                     chosenSlot = 2;
                  } else {
                     BarrierEntity best = null;
                     int bestSlot = -1;
                     if (slot1 != null && !lock1) {
                        best = slot1;
                        bestSlot = 1;
                     }

                     if (slot2 != null && !lock2 && (best == null || slot2.getId() < best.getId())) {
                        best = slot2;
                        bestSlot = 2;
                     }

                     if (best != null) {
                        chosenSlot = bestSlot;
                        toReplace = best;
                     }
                  }

                  if (chosenSlot == -1) {
                     addAura(player, 15);
                     long l0 = player.getPersistentData().getLong("unrp_barrier_ext_lock_0");
                     long l1 = player.getPersistentData().getLong("unrp_barrier_ext_lock_1");
                     long soonest = Math.min(lock1 ? l0 : Long.MAX_VALUE, lock2 ? l1 : Long.MAX_VALUE);
                     long secs = soonest == Long.MAX_VALUE ? 30L : Math.max(1L, (soonest - now + 19L) / 20L);
                     player.displayClientMessage(Component.literal("§cBarrières externes en régénération (" + secs + " s)"), true);
                     return;
                  }

                  if (toReplace != null) {
                     toReplace.dissipate();
                  }

                  Vec3 eye = player.getEyePosition();
                  Vec3 end = eye.add(player.getLookAngle().scale(24.0D));
                  HitResult hit = level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, player));
                  spawnPos = hit.getLocation();
               }

               BarrierEntity barrier = new BarrierEntity(com.myriam.unrp.entity.ModEntities.BARRIER.get(), level);
               float size = 5.0F;
               barrier.setPos(spawnPos.x, spawnPos.y - (double)size / 2.0D, spawnPos.z);
               barrier.configure(player.getUUID(), maxHpFor(player), !external, size);
               barrier.setSize(size);
               barrier.setSlot(chosenSlot);
               level.addFreshEntity(barrier);
               com.myriam.unrp.ability.Poses.play(player, external ? "br_point" : "br_ward");
               BarrierPass.deploy(player, barrier, spawnPos, external ? 64 : 0);
               player.displayClientMessage(Component.literal(external ? "§b◈ Barrière externe dressée" : "§b◈ Barrière dressée"), true);
            }

            if (player.getTags().contains("unrp.barrier_growth_trigger")) {
               player.removeTag("unrp.barrier_growth_trigger");
               boolean shrink = player.isShiftKeyDown();
               List<BarrierEntity> owned = ownedBarriers(player, 64.0D);
               if (owned.isEmpty()) {
                  player.displayClientMessage(Component.literal("§cAucune barrière active."), true);
               } else {
                  for(BarrierEntity b : owned) {
                     float before = b.getSize();
                     b.setSize(before + (shrink ? -0.75F : 0.75F));
                     if (shrink) {
                        Level lv_var52 = b.level();
                        if (lv_var52 instanceof ServerLevel) {
                           ServerLevel sl = (ServerLevel)lv_var52;
                           float r = b.getSize() / 2.0F;
                           Vec3 center = new Vec3(b.getX(), b.getY() + (double)r, b.getZ());
                           float crushDmg = 4.0F + levelOf(player) * 0.5F;
                           List<LivingEntity> caught = new ArrayList();

                           for(LivingEntity le : sl.getEntitiesOfClass(LivingEntity.class, b.getBoundingBox().inflate(1.0D), (e) -> e.isAlive() && !e.getUUID().equals(player.getUUID()))) {
                              Vec3 lc = new Vec3(le.getX(), le.getY() + (double)le.getBbHeight() / 2.0D, le.getZ());
                              if (lc.distanceTo(center) < (double)(r + 0.5F)) {
                                 le.hurt(sl.damageSources().magic(), crushDmg);
                                 le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, true));
                                 caught.add(le);
                              }
                           }

                           if (!b.isRam()) {
                              BarrierPass.squeeze(b, before, caught);
                           }
                        }
                     }
                  }

                  com.myriam.unrp.ability.Poses.play(player, shrink ? "br_shrink" : "br_grow");
                  BarrierPass.grow(player, shrink);
                  player.displayClientMessage(Component.literal(shrink ? "§b▼ Les barrières se contractent et écrasent ceux qui sont dedans" : "§b▲ Les barrières s'agrandissent"), true);
               }
            }

            if (player.getTags().contains("unrp.barrier_ram_trigger")) {
               player.removeTag("unrp.barrier_ram_trigger");
               float lvl = levelOf(player);
               Vec3 look = player.getLookAngle();
               player.setDeltaMovement(look.x * 1.2D, Math.max(0.15D, look.y * 0.6D), look.z * 1.2D);
               player.connection.send(new ClientboundSetEntityMotionPacket(player));
               Vec3 sp0 = player.getEyePosition().add(look.scale(1.6D));
               BarrierEntity shield = new BarrierEntity(com.myriam.unrp.entity.ModEntities.BARRIER.get(), level);
               shield.setPos(sp0.x, sp0.y - 1.0D, sp0.z);
               shield.configure(player.getUUID(), 20.0F, false, 2.2F);
               shield.addTag("unrp.ram_shield");
               shield.markRam();
               level.addFreshEntity(shield);
               player.getPersistentData().putInt("unrp_ram_ticks", 10);
               com.myriam.unrp.ability.Poses.play(player, "br_ram");
               BarrierPass.ram(player, shield);
               player.displayClientMessage(Component.literal("§b❈ Bélier de barrière !"), true);
            }

            if (player.getPersistentData().getInt("unrp_ram_ticks") > 0) {
               int rt = player.getPersistentData().getInt("unrp_ram_ticks");
               player.getPersistentData().putInt("unrp_ram_ticks", rt - 1);
               float lvl = levelOf(player);
               float dmg = 6.0F + lvl * 0.7F;
               Vec3 look = player.getLookAngle();
               if (rt > 6) {
                  player.setDeltaMovement(look.x * 1.1D, player.getDeltaMovement().y, look.z * 1.1D);
                  player.connection.send(new ClientboundSetEntityMotionPacket(player));
               }

               Vec3 sh = player.getEyePosition().add(look.scale(1.6D));
               BarrierEntity shield = null;

               for(BarrierEntity b : level.getEntitiesOfClass(BarrierEntity.class, player.getBoundingBox().inflate(6.0D), (e) -> e.isAlive() && e.getTags().contains("unrp.ram_shield") && player.getUUID().equals(e.getOwnerId()))) {
                  b.setPos(sh.x, sh.y - 1.0D, sh.z);
                  shield = b;
               }

               Vec3 c = player.position().add(look.scale(2.2D));
               AABB box = new AABB(c.x - 1.8D, c.y - 0.5D, c.z - 1.8D, c.x + 1.8D, c.y + 2.2D, c.z + 1.8D);

               for(LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, box, (e) -> e != player && e.isAlive() && e.hurtTime == 0)) {
                  boolean landed = le.hurt(level.damageSources().playerAttack(player), dmg);
                  le.setDeltaMovement(look.x * 0.9D, 0.35D, look.z * 0.9D);
                  if (le instanceof ServerPlayer) {
                     ServerPlayer sp2 = (ServerPlayer)le;
                     sp2.connection.send(new ClientboundSetEntityMotionPacket(le));
                  }

                  if (landed) {
                     BarrierPass.ramHit(player, shield, le, look);
                  }
               }

               if (player.getPersistentData().getInt("unrp_ram_ticks") <= 0) {
                  for(BarrierEntity b : level.getEntitiesOfClass(BarrierEntity.class, player.getBoundingBox().inflate(6.0D), (e) -> e.isAlive() && e.getTags().contains("unrp.ram_shield") && player.getUUID().equals(e.getOwnerId()))) {
                     b.shatter(false);
                  }
               }
            }

            if (now % 40L == 0L) {
               int count = ownedBarriers(player, 128.0D).size();
               if (count > 0) {
                  drainAura(player, count);
                  if (auraOf(player) <= 0) {
                     for(BarrierEntity b : ownedBarriers(player, 128.0D)) {
                        b.dissipate();
                     }

                     player.displayClientMessage(Component.literal("§7Tes barrières se dissipent..."), true);
                  }
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onLivingAttack(LivingAttackEvent event) {
      LivingEntity victim = event.getEntity();
      if (!victim.level().isClientSide) {
         List<BarrierEntity> shells = victim.level().getEntitiesOfClass(BarrierEntity.class, victim.getBoundingBox().inflate(8.0D), (b) -> b.isAlive() && b.isInside(victim));
         if (!shells.isEmpty()) {
            DamageSource source = event.getSource();
            Entity attacker = source.getEntity();
            if (attacker != null && attacker != victim) {
               for(BarrierEntity s : shells) {
                  if (s.isInside(attacker) || attacker.getUUID().equals(s.getOwnerId())) {
                     return;
                  }
               }

               event.setCanceled(true);
               ((BarrierEntity)shells.get(0)).struck(source, event.getAmount());
            } else {
               if (attacker == null && source.is(DamageTypeTags.IS_FIRE)) {
                  event.setCanceled(true);
                  victim.clearFire();
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void onOwnerDeath(LivingDeathEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         for(BarrierEntity b : player.serverLevel().getEntitiesOfClass(BarrierEntity.class, player.getBoundingBox().inflate(128.0D), (e) -> e.isAlive() && player.getUUID().equals(e.getOwnerId()))) {
            b.dissipate();
         }

      }
   }

   public static void punchFromInside(ServerPlayer player) {
      for(BarrierEntity b : player.serverLevel().getEntitiesOfClass(BarrierEntity.class, player.getBoundingBox().inflate(8.0D), Entity::isAlive)) {
         float r = b.getSize() / 2.0F;
         Vec3 center = new Vec3(b.getX(), b.getY() + (double)r, b.getZ());
         Vec3 pc = new Vec3(player.getX(), player.getY() + (double)player.getBbHeight() / 2.0D, player.getZ());
         if (pc.distanceTo(center) < (double)r) {
            boolean occupantPresent = !player.serverLevel().getEntitiesOfClass(LivingEntity.class, b.getBoundingBox().inflate(1.0D), (le) -> le != player && le.isAlive() && b.isInside(le)).isEmpty();
            if (occupantPresent) {
               return;
            }

            float dmg = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            b.hurt(player.serverLevel().damageSources().playerAttack(player), Math.max(1.0F, dmg));
            return;
         }
      }

      punchFromOutside(player);
   }

   private static void punchFromOutside(ServerPlayer player) {
      ServerLevel level = player.serverLevel();
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      BarrierEntity near = null;
      double best = 4.0D;

      for(BarrierEntity b : level.getEntitiesOfClass(BarrierEntity.class, player.getBoundingBox().inflate(8.0D), Entity::isAlive)) {
         Vec3 c = b.center();
         double t = BarrierReflect.entry(eye.x, eye.y, eye.z, look.x, look.y, look.z, c.x, c.y, c.z, (double)b.getSize() / 2.0D);
         if (t >= 0.0D && t <= best) {
            best = t;
            near = b;
         }
      }

      if (near != null) {
         HitResult wall = level.clip(new ClipContext(eye, eye.add(look.scale(best)), Block.COLLIDER, Fluid.NONE, player));
         if (wall.getType() == Type.MISS) {
            float force = BarrierReflect.punch((float)player.getAttributeValue(Attributes.ATTACK_DAMAGE), player.getAttackStrengthScale(0.5F));
            player.resetAttackStrengthTicker();
            near.struck(level.damageSources().playerAttack(player), force);
         }
      }
   }
}
