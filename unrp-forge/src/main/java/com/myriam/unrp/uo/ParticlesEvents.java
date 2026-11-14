package com.myriam.unrp.uo;

import com.myriam.unrp.pass.BarrierEntity;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.scores.Objective;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class ParticlesEvents {
   public static final String[] PATTERN_NAMES = new String[]{"Guardian", "Warden", "Vanguard", "Architect"};
   public static final int GUARDIAN = 0;
   public static final int WARDEN = 1;
   public static final int VANGUARD = 2;
   public static final int ARCHITECT = 3;
   private static final String MARK_TAG = "unrp.pmarked";
   private static final long MARK_TICKS = 100L;
   private static final double EMBER_RADIUS = 3.5D;
   private static final float PENALTY_FRACTION = 0.07F;
   private static final float PENALTY_WINDOW_CAP = 0.1F;
   private static final long PENALTY_WINDOW = 100L;
   private static final long CONSTRUCT_CD_TICKS = 600L;
   private static final int COST_CONDENSE = 12;
   private static final int COST_CONSTRUCT = 20;
   private static final int COST_FIST = 10;
   private static final int COST_SLAM = 14;
   private static final int MOTE_CONDENSE = 6;
   private static final int MOTE_CONSTRUCT = 14;
   private static final int MOTE_FIST = 8;
   private static final int MOTE_SLAM = 12;
   private static final int[] UPKEEP_PER_SEC = new int[]{3, 2, 4, 3};
   private static final Map<UUID, Long> MARK_EXPIRY = new HashMap();
   private static final Map<UUID, float[]> PENALTY = new HashMap();
   private static final Map<String, Long> CONSTRUCT_CD = new HashMap();
   private static final Map<UUID, UUID> TETHERS = new HashMap();
   private static final Map<UUID, Long> TETHER_UNTIL = new HashMap();
   private static final Map<UUID, Long> CUFFS = new HashMap();
   private static final Map<UUID, Long> WARDS = new HashMap();
   private static final double WEAVE_RANGE = 14.0D;
   private static final long WEAVE_LIFE = 200L;
   private static final int WEAVE_RECLAIM_MAX = 12;
   private static final long WEAVE_FADE = 60L;
   private static final int UNRAVEL_SPAN = 24;
   private static final int GLOW_EVERY = 3;
   private static final double FX_SEEN = 64.0D;
   private static final Map<GlobalPos, ParticlesEvents.WeavePane> WEAVE = new HashMap();
   private static final Map<UUID, ParticlesEvents.WeaveCast> WEAVING = new HashMap();
   private static final Map<UUID, Integer> WEAVE_REACH = new HashMap();
   private static final Set<ParticlesEvents.WeaveGroup> GROUPS = new HashSet();
   private static final Map<GlobalPos, long[]> ENDING = new HashMap();
   private static final Map<UUID, int[]> FX_SENT = new HashMap();

   private static int score(ServerPlayer p, String obj) {
      try {
         ServerScoreboard sb = p.server.getScoreboard();
         Objective o = sb.getObjective(obj);
         return o == null ? 0 : sb.getOrCreatePlayerScore(p.getScoreboardName(), o).getScore();
      } catch (Exception var4) {
         return 0;
      }
   }

   private static void setScore(ServerPlayer p, String obj, int val) {
      try {
         ServerScoreboard sb = p.server.getScoreboard();
         Objective o = sb.getObjective(obj);
         if (o != null) {
            sb.getOrCreatePlayerScore(p.getScoreboardName(), o).setScore(val);
         }
      } catch (Exception var5) {
      }

   }

   private static boolean spendAura(ServerPlayer p, int cost) {
      int cur = score(p, "unrp.aura");
      if (cur < cost) {
         p.displayClientMessage(Component.literal("§dNot enough aura."), true);
         return false;
      } else {
         setScore(p, "unrp.aura", cur - cost);
         return true;
      }
   }

   private static boolean spendSwarm(ServerPlayer p, int auraCost, int moteCost) {
      int motes = score(p, "unrp.motes");
      if (motes < moteCost) {
         p.displayClientMessage(Component.literal("§dThe swarm is spent."), true);
         return false;
      } else {
         int aura = score(p, "unrp.aura");
         if (aura < auraCost) {
            p.displayClientMessage(Component.literal("§dNot enough aura."), true);
            return false;
         } else {
            setScore(p, "unrp.motes", motes - moteCost);
            setScore(p, "unrp.aura", aura - auraCost);
            com.myriam.unrp.player.PlayerStateService.send(p);
            return true;
         }
      }
   }

   private static void refundMotes(ServerPlayer p, int amount) {
      if (amount > 0) {
         int cap = Math.max(0, score(p, "unrp.motes_max"));
         setScore(p, "unrp.motes", Math.min(cap, score(p, "unrp.motes") + amount));
         com.myriam.unrp.player.PlayerStateService.send(p);
      }
   }

   public static void onConstructReclaimed(ParticleConstructEntity c) {
      UUID ownerId = c.getOwnerId();
      if (ownerId != null && c.getServer() != null) {
         ServerPlayer owner = c.getServer().getPlayerList().getPlayer(ownerId);
         if (owner != null) {
            int back = 7;
            refundMotes(owner, back);
            owner.displayClientMessage(Component.literal("§dConstruct reclaimed: §f+" + back + " §dmotes."), true);
         }
      }
   }

   private static boolean allied(ServerPlayer owner, LivingEntity other) {
      if (other == owner) {
         return true;
      } else if (com.myriam.unrp.social.SocialService.allied(other, owner)) {
         return true;
      } else if (other instanceof ServerPlayer) {
         ServerPlayer sp = (ServerPlayer)other;

         try {
            return com.myriam.unrp.social.SocialService.sameGroup(owner.server, owner.getUUID(), sp.getUUID());
         } catch (Exception var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static int pattern(ServerPlayer p) {
      int v = score(p, "unrp.pattern");
      return v >= 0 && v <= 3 ? v : 0;
   }

   private static void syncStanceTags(ServerPlayer p, int active) {
      for(int i = 0; i < 4; ++i) {
         String tag = "unrp.stance." + i;
         boolean has = p.getTags().contains(tag);
         if (i == active && !has) {
            p.addTag(tag);
         }

         if (i != active && has) {
            p.removeTag(tag);
         }
      }

   }

   public static void markTarget(LivingEntity target, long now) {
      if (!SgRules.outside(target)) {
         target.addTag("unrp.pmarked");
         target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, false, false));
         MARK_EXPIRY.put(target.getUUID(), now + 100L);
      }
   }

   public static boolean isMarked(Entity e) {
      return e.getTags().contains("unrp.pmarked");
   }

   private static double trickEff(ServerPlayer p) {
      return PowerScalingEvents.effectiveStat(p, "unrp.pot_trick", "unrp.mult_trick");
   }

   public static float constructMaxHp(ServerPlayer p, int variant) {
      float[] mult = new float[]{1.6F, 2.0F, 1.0F, 0.8F};
      double level = Math.max(1.0D, Math.min(10.0D, (double)score(p, "power_level") / 10.0D));
      double hp = (30.0D + trickEff(p) * 6.0D + level * 2.0D) * (double)mult[variant];
      return (float)hp;
   }

   private static List<ParticleConstructEntity> ownConstructs(ServerPlayer p) {
      return p.serverLevel().getEntitiesOfClass(ParticleConstructEntity.class, p.getBoundingBox().inflate(48.0D), (c) -> c.isAlive() && p.getUUID().equals(c.getOwnerId()));
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player level_0 = event.player;
         if (level_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)level_0;
            if (!player.getTags().contains("unrp.active.particles")) {
               if (player.tickCount % 100 == 0) {
                  player.removeTag("unrp.pt_weave_on");
                  WEAVING.remove(player.getUUID());
               }

               if (FX_SENT.remove(player.getUUID()) != null) {
                  sendFx(player, new int[]{0, 0, -1, 0, 0});
               }

            } else {
               ServerLevel level = player.serverLevel();
               long now = level.getGameTime();
               if (player.getTags().contains("unrp.pt_switch")) {
                  player.removeTag("unrp.pt_switch");
                  int cur = pattern(player);
                  int next = player.isShiftKeyDown() ? (cur + 3) % 4 : (cur + 1) % 4;
                  setScore(player, "unrp.pattern", next);
                  syncStanceTags(player, next);
                  if (next != 3) {
                     player.removeTag("unrp.pt_weave_on");
                     WEAVING.remove(player.getUUID());
                  }

                  player.displayClientMessage(Component.literal("§dPattern: §f" + PATTERN_NAMES[next]), true);
                  level.playSound((Player)null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.PLAYERS, 1.0F, 0.8F + (float)next * 0.15F);
               }

               if (player.getTags().contains("unrp.pt_condense")) {
                  player.removeTag("unrp.pt_condense");
                  doCondense(player, level, now);
               }

               if (player.getTags().contains("unrp.pt_construct")) {
                  player.removeTag("unrp.pt_construct");
                  doConstruct(player, level, now);
               }

               if (player.getTags().contains("unrp.pt_fist")) {
                  player.removeTag("unrp.pt_fist");
                  doFist(player, level, now);
               }

               boolean weaveHeld = player.getTags().contains("unrp.pt_weave_on");
               if (weaveHeld && pattern(player) != 3) {
                  player.removeTag("unrp.pt_weave_on");
                  weaveHeld = false;
               }

               if (weaveHeld) {
                  ParticlesEvents.WeaveCast held = (ParticlesEvents.WeaveCast)WEAVING.get(player.getUUID());
                  if (held != null && held.released) {
                     WEAVING.remove(player.getUUID());
                  }

                  tickWeave(player, level, now);
               } else {
                  ParticlesEvents.WeaveCast left = (ParticlesEvents.WeaveCast)WEAVING.get(player.getUUID());
                  if (left != null) {
                     left.released = true;
                     if (!left.consumed && left.brush.pending() != 0) {
                        drainWeave(player, level, now, left);
                     } else {
                        WEAVING.remove(player.getUUID());
                     }
                  }
               }

               weaveFx(player, weaveHeld);
               if (player.tickCount % 20 == 0) {
                  int cap = 120 + (int)Math.round(120.0D * weaveLevelFactor(player));
                  setScore(player, "unrp.motes_max", cap);
                  int motes = score(player, "unrp.motes");
                  int gain = player.isShiftKeyDown() && !weaveHeld ? 6 : 3;
                  setScore(player, "unrp.motes", Math.min(cap, Math.max(0, motes) + gain));
                  com.myriam.unrp.player.PlayerStateService.send(player);
               }

               if (player.tickCount % 50 == 0 && !AmPass.veiled(player, "particles")) {
                  level.playSound((Player)null, player.blockPosition(), (SoundEvent)ModSounds.PARTICLES_BUZZ.get(), SoundSource.PLAYERS, 0.35F, 0.95F + (player.tickCount % 150 == 0 ? 0.1F : 0.0F));
               }

               if (player.tickCount % 10 == 0) {
                  syncStanceTags(player, pattern(player));
                  if (!AmPass.veiled(player, "particles")) {
                     for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3.5D), (t) -> t != player && t.isAlive())) {
                        if (!allied(player, e)) {
                           markTarget(e, now);
                        }
                     }
                  }
               }

            }
         }
      }
   }

   private static boolean signatureCraft(ServerPlayer player, ServerLevel level) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      ParticleConstructEntity bestC = null;
      BarrierEntity bestB = null;
      double bestDot = 0.9D;

      for(ParticleConstructEntity c : level.getEntitiesOfClass(ParticleConstructEntity.class, player.getBoundingBox().inflate(12.0D), (x) -> x.isAlive() && player.getUUID().equals(x.getOwnerId()) && x.hpFraction() < 1.0F)) {
         Vec3 to_0 = c.position().add(0.0D, 1.2D, 0.0D).subtract(eye);
         if (!(to_0.length() > 12.0D)) {
            double dot_0 = to_0.normalize().dot(look);
            if (dot_0 > bestDot) {
               bestDot = dot_0;
               bestC = c;
            }
         }
      }

      for(BarrierEntity b : level.getEntitiesOfClass(BarrierEntity.class, player.getBoundingBox().inflate(12.0D), (x) -> x.isAlive() && x.hpFraction() < 1.0F)) {
         UUID bOwner = b.getOwnerId();
         if (bOwner != null) {
            ServerPlayer bp = player.server.getPlayerList().getPlayer(bOwner);
            if (bp != null && allied(player, bp)) {
               Vec3 to = b.position().add(0.0D, (double)b.getSize() / 2.0D, 0.0D).subtract(eye);
               if (!(to.length() > 12.0D)) {
                  double dot = to.normalize().dot(look);
                  if (dot > bestDot) {
                     bestDot = dot;
                     bestB = b;
                     bestC = null;
                  }
               }
            }
         }
      }

      Entity target;
      if (bestC != null) {
         bestC.mend(bestC.getMaxHp() * 0.25F);
         target = bestC;
      } else {
         if (bestB == null) {
            return false;
         }

         bestB.mend(bestB.getMaxHp() * 0.25F);
         target = bestB;
      }

      level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), target.getX(), target.getY() + 1.2D, target.getZ(), 12, 0.4D, 0.6D, 0.4D, 0.04D);
      level.playSound((Player)null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.1F);
      player.displayClientMessage(Component.literal("§dSignature Craft: the lattice is rewoven."), true);
      return true;
   }

   private static void doCondense(ServerPlayer player, ServerLevel level, long now) {
      if (spendSwarm(player, 12, 6)) {
         int pat = pattern(player);
         if (pat == 0) {
            if (!player.isShiftKeyDown() && signatureCraft(player, level)) {
               return;
            }

            LivingEntity target_0 = (LivingEntity)(player.isShiftKeyDown() ? player : lookTarget(player, level, 12.0D, true));
            if (target_0 == null) {
               target_0 = player;
            }

            int nearMarks = level.getEntitiesOfClass(LivingEntity.class, target_0.getBoundingBox().inflate(6.0D), ParticlesEvents::isMarked).size();
            int amp = nearMarks >= 2 ? 1 : 0;
            int dur_0 = 100 + (int)(trickEff(player) * 20.0D);
            target_0.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, dur_0, amp, false, true));
            target_0.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, true));
            WARDS.put(target_0.getUUID(), now + (long)dur_0);
            level.sendParticles(ParticleTypes.HEART, target_0.getX(), target_0.getY() + 1.6D, target_0.getZ(), 3, 0.3D, 0.2D, 0.3D, 0.02D);
            player.displayClientMessage(Component.literal("§dGuardian ward laid."), true);
         } else if (pat == 1) {
            LivingEntity target = lookTarget(player, level, 18.0D, false);
            if (target == null) {
               player.displayClientMessage(Component.literal("§dNo target for the cuff."), true);
               return;
            }

            markTarget(target, now);
            sparkLine(level, player.getEyePosition().add(player.getLookAngle().scale(0.6D)), target.position().add(0.0D, (double)target.getBbHeight() * 0.6D, 0.0D));

            for(int ring = 0; ring < 8; ++ring) {
               double ang = (Math.PI * 2D) * (double)ring / 8.0D;
               level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), target.getX() + Math.cos(ang) * 0.7D, target.getY() + 0.3D, target.getZ() + Math.sin(ang) * 0.7D, 1, 0.02D, 0.02D, 0.02D, 0.0D);
            }

            int dur = 80;
            ParticleConstructEntity anchor = nearestConstruct(player, target, 8.0D);
            if (anchor != null) {
               dur = 160;
               TETHERS.put(target.getUUID(), anchor.getUUID());
               TETHER_UNTIL.put(target.getUUID(), now + (long)dur);
            }

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, dur, 2, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, dur, 1, false, true));
            CUFFS.put(target.getUUID(), now + (long)dur);
            level.playSound((Player)null, target.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0F, 0.7F);
            player.displayClientMessage(Component.literal(anchor != null ? "§dCuffed and tethered." : "§dCuffed."), true);
         } else if (pat == 2) {
            Vec3 look = player.getLookAngle();
            int struck = 0;

            for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0D), (t) -> t != player && t.isAlive())) {
               if (!allied(player, e)) {
                  Vec3 to = e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D).subtract(player.getEyePosition()).normalize();
                  if (!(to.dot(look) < 0.5D)) {
                     ++struck;
                     if (struck >= 4) {
                        break;
                     }
                  }
               }
            }

            Vec3 eye = player.getEyePosition();
            Vec3 flat = (new Vec3(look.x, 0.0D, look.z)).normalize();
            Vec3 perp = new Vec3(-flat.z, 0.0D, flat.x);

            for(int i = 0; i < 4; ++i) {
               double side = ((double)i - 1.5D) * 0.45D;
               Vec3 at = eye.add(look.scale(1.0D)).add(perp.scale(side)).add(0.0D, -0.2D, 0.0D);
               ParticleBoltEntity arrow = (ParticleBoltEntity)(com.myriam.unrp.entity.ModEntities.PARTICLE_BOLT.get()).create(level);
               if (arrow != null) {
                  arrow.setPos(at.x, at.y, at.z);
                  Vec3 dir = look.add(perp.scale(side * 0.06D)).normalize();
                  arrow.configure(player.getUUID(), 0, 4.0F, 1.6F, dir.scale(1.4D), 30);
                  level.addFreshEntity(arrow);
               }
            }

            for(ParticleConstructEntity c : ownConstructs(player)) {
               fireSpikeAt(c, player, level, 3.0F, 12.0D);
            }

            level.playSound((Player)null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0F, 1.2F);
            player.displayClientMessage(Component.literal("§dSpike volley loosed."), true);
         }

      }
   }

   private static double weaveLevelFactor(ServerPlayer p) {
      int pl = score(p, "power_level");
      double v = pl > 100 ? 100.0D : (double)(pl < 0 ? 0 : pl);
      return Math.sqrt(v / 100.0D);
   }

   public static void setWeaveReach(ServerPlayer player, int reach) {
      WEAVE_REACH.put(player.getUUID(), reach < 2 ? 2 : (reach > 14 ? 14 : reach));
   }

   private static int weaveReach(ServerPlayer player) {
      Integer v = (Integer)WEAVE_REACH.get(player.getUUID());
      int r = v == null ? 9 : v;
      return r < 2 ? 2 : (r > 14 ? 14 : r);
   }

   private static int orientIndex(BlockState state) {
      ParticleBlock.Orientation o = (ParticleBlock.Orientation)state.getValue(ParticleBlock.ORIENTATION);
      if (o == ParticleBlock.Orientation.WALL_NS) {
         return 1;
      } else {
         return o == ParticleBlock.Orientation.WALL_EW ? 2 : 0;
      }
   }

   private static boolean glowAt(BlockPos pos, int orient) {
      int a = orient == 2 ? pos.getZ() : pos.getX();
      int b = orient == 0 ? pos.getZ() : pos.getY();
      return Math.floorMod(a, 3) == 0 && Math.floorMod(b, 3) == 0;
   }

   private static ParticlesEvents.Aim brushAim(ServerPlayer player, ServerLevel level, int reach) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      Vec3 end = eye.add(look.scale((double)reach));
      BlockHitResult ray = level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, player));
      ParticlesEvents.Aim aim = new ParticlesEvents.Aim();
      if (ray.getType() == Type.BLOCK) {
         if (level.getBlockState(ray.getBlockPos()).is((net.minecraft.world.level.block.Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get())) {
            aim.pane = ray.getBlockPos();
            aim.point = ray.getLocation().add(look.scale(0.02D));
            return aim;
         } else {
            aim.point = ray.getLocation().add(look.scale(-0.25D));
            return aim;
         }
      } else {
         aim.point = end;
         return aim;
      }
   }

   private static void swarmFail(ServerPlayer player, int code) {
      player.displayClientMessage(Component.literal(code == -2 ? "§dNot enough aura to weave." : "§dThe swarm is spent."), true);
   }

   public static boolean woven(Level level, BlockPos pos) {
      GlobalPos gp = GlobalPos.of(level.dimension(), pos);
      return WEAVE.containsKey(gp) || ENDING.containsKey(gp);
   }

   private static ParticlesEvents.WeavePane ownPane(ServerLevel level, BlockPos pos, UUID owner) {
      if (pos == null) {
         return null;
      } else {
         ParticlesEvents.WeavePane pane = (ParticlesEvents.WeavePane)WEAVE.get(GlobalPos.of(level.dimension(), pos));
         return pane != null && owner.equals(pane.owner) ? pane : null;
      }
   }

   private static ParticlesEvents.WeavePane forgetPane(GlobalPos gp) {
      ParticlesEvents.WeavePane pane = (ParticlesEvents.WeavePane)WEAVE.remove(gp);
      if (pane != null && pane.group != null) {
         pane.group.members.remove(gp);
      }

      return pane;
   }

   private static void setFade(ServerLevel level, BlockPos pos, long when) {
      if (level.isLoaded(pos)) {
         BlockEntity var5 = level.getBlockEntity(pos);
         if (var5 instanceof LatticeBlockEntity) {
            LatticeBlockEntity lattice = (LatticeBlockEntity)var5;
            lattice.setFade(when);
         }
      }

   }

   private static void refreshGroup(ServerLevel level, ParticlesEvents.WeaveGroup g, long now) {
      g.expiry = now + 200L;
      g.unravel = null;
      if (g.fading) {
         g.fading = false;

         for(GlobalPos m : g.members) {
            setFade(level, m.pos(), 0L);
         }
      }

   }

   private static ParticlesEvents.WeaveGroup mergeGroups(ParticlesEvents.WeaveGroup a, ParticlesEvents.WeaveGroup b) {
      ParticlesEvents.WeaveGroup big = a.members.size() >= b.members.size() ? a : b;
      ParticlesEvents.WeaveGroup small = big == a ? b : a;

      for(GlobalPos m : small.members) {
         ParticlesEvents.WeavePane q = (ParticlesEvents.WeavePane)WEAVE.get(m);
         if (q != null) {
            q.group = big;
         }
      }

      big.members.addAll(small.members);
      big.expiry = Math.max(big.expiry, small.expiry);
      big.fading = big.fading || small.fading;
      small.members.clear();
      GROUPS.remove(small);
      return big;
   }

   private static void bindPane(ServerLevel level, GlobalPos gp, ParticlesEvents.WeavePane pane, ParticlesEvents.WeaveCast cast, long now) {
      ParticlesEvents.WeaveGroup g = cast.group;
      if (g != null && (!GROUPS.contains(g) || !g.dimension.equals(level.dimension()))) {
         g = null;
      }

      if (pane.group != null && GROUPS.contains(pane.group) && pane.group != g) {
         g = g == null ? pane.group : mergeGroups(g, pane.group);
      }

      if (g == null) {
         g = new ParticlesEvents.WeaveGroup(pane.owner, level.dimension(), now + 200L);
         GROUPS.add(g);
      }

      BlockPos pos = gp.pos();

      for(int dx = -1; dx <= 1; ++dx) {
         for(int dy = -1; dy <= 1; ++dy) {
            for(int dz = -1; dz <= 1; ++dz) {
               if (dx != 0 || dy != 0 || dz != 0) {
                  ParticlesEvents.WeavePane q = (ParticlesEvents.WeavePane)WEAVE.get(GlobalPos.of(level.dimension(), pos.offset(dx, dy, dz)));
                  if (q != null && q.group != null && q.group != g && pane.owner.equals(q.owner) && GROUPS.contains(q.group)) {
                     g = mergeGroups(g, q.group);
                  }
               }
            }
         }
      }

      pane.group = g;
      g.members.add(gp);
      cast.group = g;
      refreshGroup(level, g, now);
   }

   private static int weavePane(ServerPlayer player, ServerLevel level, long now, BlockPos pos, int orient, int layer, ParticlesEvents.WeaveCast cast) {
      BlockState existing = level.getBlockState(pos);
      ParticleBlock.Orientation po = orient == 1 ? ParticleBlock.Orientation.WALL_NS : (orient == 2 ? ParticleBlock.Orientation.WALL_EW : ParticleBlock.Orientation.FLOOR);
      int useLayer = orient == 0 ? layer : 3;
      boolean glow = glowAt(pos, orient);
      GlobalPos gp = GlobalPos.of(level.dimension(), pos);
      if (!existing.is((net.minecraft.world.level.block.Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get())) {
         if (!existing.isAir()) {
            return 0;
         } else {
            int motes = score(player, "unrp.motes");
            if (motes < 1) {
               return -1;
            } else {
               int aura = score(player, "unrp.aura");
               if (aura < 1) {
                  return -2;
               } else {
                  level.setBlockAndUpdate(pos, (BlockState)((BlockState)((BlockState)((net.minecraft.world.level.block.Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get()).defaultBlockState().setValue(ParticleBlock.ORIENTATION, po)).setValue(ParticleBlock.LAYER, useLayer)).setValue(ParticleBlock.GLOW, glow));
                  BlockEntity var16 = level.getBlockEntity(pos);
                  if (var16 instanceof LatticeBlockEntity) {
                     LatticeBlockEntity lattice = (LatticeBlockEntity)var16;
                     lattice.weave(player.getUUID(), now);
                  }

                  ParticlesEvents.WeavePane pane_0 = new ParticlesEvents.WeavePane(player.getUUID(), (ParticlesEvents.WeaveGroup)null, 4.0F);
                  WEAVE.put(gp, pane_0);
                  bindPane(level, gp, pane_0, cast, now);
                  setScore(player, "unrp.motes", motes - 1);
                  setScore(player, "unrp.aura", aura - 1);
                  cast.dirty = true;
                  return 1;
               }
            }
         }
      } else {
         ParticlesEvents.WeavePane pane = (ParticlesEvents.WeavePane)WEAVE.get(gp);
         if (pane != null && player.getUUID().equals(pane.owner) && !ENDING.containsKey(gp)) {
            bindPane(level, gp, pane, cast, now);
            if (existing.getValue(ParticleBlock.ORIENTATION) != po || existing.getValue(ParticleBlock.LAYER) != useLayer || existing.getValue(ParticleBlock.GLOW) != glow) {
               level.setBlockAndUpdate(pos, (BlockState)((BlockState)((BlockState)existing.setValue(ParticleBlock.ORIENTATION, po)).setValue(ParticleBlock.LAYER, useLayer)).setValue(ParticleBlock.GLOW, glow));
            }
         }

         return 0;
      }
   }

   private static void endPane(ServerLevel level, GlobalPos gp, int kind, long now) {
      if (level.isLoaded(gp.pos())) {
         BlockEntity var6 = level.getBlockEntity(gp.pos());
         if (var6 instanceof LatticeBlockEntity) {
            LatticeBlockEntity lattice = (LatticeBlockEntity)var6;
            lattice.setEnd(kind);
         }
      }

      ENDING.put(gp, new long[]{now + 1L, (long)kind});
   }

   private static void reclaimLattice(ServerPlayer player, ServerLevel level, BlockPos start) {
      List<BlockPos> found = new ArrayList();
      List<BlockPos> frontier = new ArrayList();
      Set<BlockPos> seen = new HashSet();
      frontier.add(start);
      seen.add(start);

      for(int guard = 0; guard < 128 && !frontier.isEmpty() && found.size() < 12; ++guard) {
         BlockPos cur = (BlockPos)frontier.remove(frontier.size() - 1);
         GlobalPos gp_0 = GlobalPos.of(level.dimension(), cur);
         ParticlesEvents.WeavePane pane = (ParticlesEvents.WeavePane)WEAVE.get(gp_0);
         if (pane != null && player.getUUID().equals(pane.owner) && !ENDING.containsKey(gp_0) && level.getBlockState(cur).is((net.minecraft.world.level.block.Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get())) {
            found.add(cur);

            for(Direction d : Direction.values()) {
               BlockPos n = cur.relative(d);
               if (seen.add(n)) {
                  frontier.add(n);
               }
            }
         }
      }

      if (!found.isEmpty()) {
         long now = level.getGameTime();

         for(BlockPos pos : found) {
            GlobalPos gp = GlobalPos.of(level.dimension(), pos);
            forgetPane(gp);
            endPane(level, gp, 1, now);
         }

         int refund = (int)((double)found.size() * 0.6D);
         int cap = score(player, "unrp.motes_max");
         setScore(player, "unrp.motes", Math.min(cap, score(player, "unrp.motes") + refund));
         level.playSound((Player)null, start, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
         player.displayClientMessage(Component.literal("§dLattice reclaimed: §f+" + refund + " §dmotes."), true);
         com.myriam.unrp.player.PlayerStateService.send(player);
      }
   }

   private static void scatterPane(ServerLevel level, BlockPos pos, boolean gentle) {
      if (level.getBlockState(pos).is((net.minecraft.world.level.block.Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get())) {
         level.removeBlock(pos, false);
      }

      if (!gentle) {
         level.sendParticles(ParticleTypes.CRIT, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.6D, (double)pos.getZ() + 0.5D, 4, 0.3D, 0.3D, 0.3D, 0.08D);
         level.sendParticles(ParticleTypes.SMOKE, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.6D, (double)pos.getZ() + 0.5D, 3, 0.25D, 0.25D, 0.25D, 0.01D);
         level.playSound((Player)null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.6F, 0.7F);
      }

   }

   private static void tickWeave(ServerPlayer player, ServerLevel level, long now) {
      UUID id = player.getUUID();
      ParticlesEvents.WeaveCast cast = (ParticlesEvents.WeaveCast)WEAVING.get(id);
      int reach = weaveReach(player);
      ParticlesEvents.Aim aim = brushAim(player, level, reach);
      Vec3 point = aim.point;
      Vec3 look = player.getLookAngle();
      ParticlesEvents.WeavePane aimed = ownPane(level, aim.pane, id);
      if (aimed != null && ENDING.containsKey(GlobalPos.of(level.dimension(), aim.pane))) {
         aimed = null;
      }

      if (cast == null) {
         cast = new ParticlesEvents.WeaveCast();
         WEAVING.put(id, cast);
         if (player.isShiftKeyDown() && aimed != null) {
            reclaimLattice(player, level, aim.pane);
            cast.consumed = true;
            return;
         }

         if (score(player, "unrp.aura") < 3) {
            player.displayClientMessage(Component.literal("§dNot enough aura to weave."), true);
            cast.consumed = true;
            return;
         }

         if (score(player, "unrp.motes") < 1) {
            player.displayClientMessage(Component.literal("§dThe swarm is spent."), true);
            cast.consumed = true;
            return;
         }

         int[] seed = null;
         if (aimed != null) {
            BlockState st = level.getBlockState(aim.pane);
            seed = new int[]{aim.pane.getX(), aim.pane.getY(), aim.pane.getZ(), orientIndex(st), st.getValue(ParticleBlock.LAYER)};
            cast.group = aimed.group;
         }

         cast.brush.begin(point.x, point.y, point.z, player.getXRot(), look.x, look.z, player.isShiftKeyDown(), seed);
         level.playSound((Player)null, BlockPos.containing(point.x, point.y, point.z), (SoundEvent)ModSounds.PARTICLES_BUZZ.get(), SoundSource.PLAYERS, 0.6F, 1.25F);
         cast.dirty = true;
      } else {
         if (cast.consumed) {
            return;
         }

         cast.brush.step(point.x, point.y, point.z, player.getXRot(), look.x, look.z, player.isShiftKeyDown());
      }

      if (aimed != null && aimed.group != null && GROUPS.contains(aimed.group)) {
         refreshGroup(level, aimed.group, now);
      }

      drainWeave(player, level, now, cast);
      if (now % 10L == 0L) {
         level.playSound((Player)null, BlockPos.containing(point.x, point.y, point.z), (SoundEvent)ModSounds.PARTICLES_BUZZ.get(), SoundSource.PLAYERS, 0.4F, 1.2F);
      }

   }

   private static void drainWeave(ServerPlayer player, ServerLevel level, long now, ParticlesEvents.WeaveCast cast) {
      if (!cast.consumed) {
         int placed = 0;
         int polls = 0;

         while(placed < 6 && polls < 24) {
            int[] cell = cast.brush.poll();
            if (cell == null) {
               break;
            }

            ++polls;
            BlockPos at = new BlockPos(cell[0], cell[1], cell[2]);
            if (!(at.distToCenterSqr(player.getX(), player.getY(), player.getZ()) > 196.0D)) {
               int r = weavePane(player, level, now, at, cell[3], cell[4], cast);
               if (r < 0) {
                  swarmFail(player, r);
                  cast.consumed = true;
                  cast.brush.clear();
                  cast.dirty = true;
                  break;
               }

               placed += r;
            }
         }

         if (cast.dirty && (now % 4L == 0L || cast.consumed)) {
            cast.dirty = false;
            com.myriam.unrp.player.PlayerStateService.send(player);
         }

      }
   }

   private static void weaveFx(ServerPlayer player, boolean weaveHeld) {
      UUID id = player.getUUID();
      ParticlesEvents.WeaveCast cast = (ParticlesEvents.WeaveCast)WEAVING.get(id);
      int flags = AmPass.veiled(player, "particles") ? 0 : 1;
      int orient = -1;
      int plane = 0;
      if (weaveHeld && cast != null && !cast.released) {
         flags |= cast.consumed ? 4 : 2;
         orient = cast.brush.orient();
         plane = cast.brush.plane();
      }

      int mmax = Math.max(1, score(player, "unrp.motes_max"));
      int mcur = Math.min(mmax, Math.max(0, score(player, "unrp.motes")));
      int swarm = (int)Math.round(8.0D * (double)mcur / (double)mmax);
      int[] next = new int[]{flags, weaveReach(player), orient, plane, swarm};
      int[] prev = (int[])FX_SENT.get(id);
      if (prev == null || !Arrays.equals(prev, next) || player.tickCount % 20 == 0) {
         FX_SENT.put(id, next);
         sendFx(player, next);
      }
   }

   private static void sendFx(ServerPlayer player, int[] v) {
      WeaveFxPacket msg = new WeaveFxPacket(player.getId(), v[0], v[1], v[2], v[3], v[4]);
      double seen = 4096.0D;

      for(ServerPlayer p : player.serverLevel().players()) {
         if (p.distanceToSqr(player) <= seen) {
            com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), msg);
         }
      }

   }

   private static void tickLattice(MinecraftServer server, long now) {
      if (!ENDING.isEmpty()) {
         Iterator<Map.Entry<GlobalPos, long[]>> eit = ENDING.entrySet().iterator();

         while(eit.hasNext()) {
            Map.Entry<GlobalPos, long[]> e = (Map.Entry)eit.next();
            if (((long[])e.getValue())[0] <= now) {
               eit.remove();
               ServerLevel level_0 = server.getLevel(((GlobalPos)e.getKey()).dimension());
               if (level_0 != null && level_0.isLoaded(((GlobalPos)e.getKey()).pos())) {
                  scatterPane(level_0, ((GlobalPos)e.getKey()).pos(), ((long[])e.getValue())[1] != 2L);
               }
            }
         }
      }

      if (!GROUPS.isEmpty()) {
         List<ParticlesEvents.WeaveGroup> spent = new ArrayList();

         for(ParticlesEvents.WeaveGroup g : GROUPS) {
            if (g.unravel != null) {
               ServerLevel level = server.getLevel(g.dimension);
               ServerPlayer owner = server.getPlayerList().getPlayer(g.owner);

               for(int k = 0; k < g.rate && !g.unravel.isEmpty(); ++k) {
                  GlobalPos gp = (GlobalPos)g.unravel.poll();
                  ParticlesEvents.WeavePane pane = (ParticlesEvents.WeavePane)WEAVE.get(gp);
                  if (pane != null && pane.group == g) {
                     forgetPane(gp);
                     BlockPos pos = gp.pos();
                     if (level != null && level.isLoaded(pos)) {
                        if (owner != null && owner.level() == level && owner.distanceToSqr((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) < 576.0D && level.random.nextFloat() < 0.3F) {
                           int cap = score(owner, "unrp.motes_max");
                           setScore(owner, "unrp.motes", Math.min(cap, score(owner, "unrp.motes") + 1));
                        }

                        scatterPane(level, pos, true);
                     }
                  }
               }

               if (g.unravel.isEmpty()) {
                  g.unravel = null;
               }

               if (g.members.isEmpty()) {
                  spent.add(g);
               }
            }
         }

         GROUPS.removeAll(spent);
      }
   }

   private static void sweepLattice(MinecraftServer server, long now) {
      Set<ParticlesEvents.WeaveGroup> near = new HashSet();
      Iterator<Map.Entry<GlobalPos, ParticlesEvents.WeavePane>> bit = WEAVE.entrySet().iterator();
      List<GlobalPos> broken = new ArrayList();

      while(bit.hasNext()) {
         Map.Entry<GlobalPos, ParticlesEvents.WeavePane> e = (Map.Entry)bit.next();
         ParticlesEvents.WeavePane pane = (ParticlesEvents.WeavePane)e.getValue();
         ServerLevel level_0 = server.getLevel(((GlobalPos)e.getKey()).dimension());
         BlockPos pos = ((GlobalPos)e.getKey()).pos();
         if (level_0 != null && level_0.isLoaded(pos) && level_0.getBlockState(pos).is((net.minecraft.world.level.block.Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get())) {
            if (pane.integrity <= 0.0F) {
               broken.add((GlobalPos)e.getKey());
            } else if (pane.group != null && !near.contains(pane.group)) {
               ServerPlayer owner_0 = pane.owner == null ? null : server.getPlayerList().getPlayer(pane.owner);
               if (owner_0 != null && owner_0.level() == level_0 && owner_0.distanceToSqr((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D) < 6.25D) {
                  near.add(pane.group);
               }
            }
         } else {
            if (pane.group != null) {
               pane.group.members.remove(e.getKey());
            }

            bit.remove();
         }
      }

      for(GlobalPos gp : broken) {
         forgetPane(gp);
         ServerLevel level_1 = server.getLevel(gp.dimension());
         if (level_1 != null) {
            endPane(level_1, gp, 2, now);
         }
      }

      List<ParticlesEvents.WeaveGroup> spent = new ArrayList();

      for(ParticlesEvents.WeaveGroup g : GROUPS) {
         if (g.members.isEmpty()) {
            spent.add(g);
         } else {
            ServerLevel level = server.getLevel(g.dimension);
            if (level != null) {
               if (near.contains(g)) {
                  refreshGroup(level, g, now);
               } else if (g.unravel == null) {
                  if (now >= g.expiry) {
                     List<GlobalPos> order = new ArrayList(g.members);
                     ServerPlayer owner = server.getPlayerList().getPlayer(g.owner);
                     if (owner != null && owner.level() == level) {
                        double ox = owner.getX();
                        double oy = owner.getY();
                        double oz = owner.getZ();
                        order.sort((p, q) -> Double.compare(q.pos().distToCenterSqr(ox, oy, oz), p.pos().distToCenterSqr(ox, oy, oz)));
                     }

                     g.unravel = new ArrayDeque(order);
                     g.rate = Math.max(2, (order.size() + 24 - 1) / 24);
                  } else if (!g.fading && now >= g.expiry - 60L) {
                     g.fading = true;

                     for(GlobalPos m : g.members) {
                        setFade(level, m.pos(), now);
                     }
                  }
               }
            }
         }
      }

      GROUPS.removeAll(spent);
   }

   @SubscribeEvent
   public static void onLatticeStruck(PlayerInteractEvent.LeftClickBlock event) {
      Player level_0 = event.getEntity();
      if (level_0 instanceof ServerPlayer attacker) {
         Level pos = event.getLevel();
         if (pos instanceof ServerLevel level) {
            BlockPos var10 = event.getPos();
            if (level.getBlockState(var10).is((net.minecraft.world.level.block.Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get())) {
               GlobalPos gp = GlobalPos.of(level.dimension(), var10);
               ParticlesEvents.WeavePane pane = (ParticlesEvents.WeavePane)WEAVE.get(gp);
               if (pane != null) {
                  long now = level.getGameTime();
                  if (now - pane.lastHit >= 10L) {
                     pane.lastHit = now;
                     if (attacker.getUUID().equals(pane.owner)) {
                        forgetPane(gp);
                        scatterPane(level, var10, true);
                        if (level.random.nextFloat() < 0.6F) {
                           int cap = score(attacker, "unrp.motes_max");
                           setScore(attacker, "unrp.motes", Math.min(cap, score(attacker, "unrp.motes") + 1));
                        }

                     } else {
                        float dmg = (float)Math.max(1.0D, attacker.getAttributeValue(Attributes.ATTACK_DAMAGE));
                        pane.integrity -= dmg;
                        level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), (double)var10.getX() + 0.5D, (double)var10.getY() + 0.6D, (double)var10.getZ() + 0.5D, 2, 0.2D, 0.2D, 0.2D, 0.02D);
                        if (pane.integrity <= 0.0F) {
                           forgetPane(gp);
                           endPane(level, gp, 2, now);
                        }

                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLatticeShot(ProjectileImpactEvent event) {
      HitResult level_0 = event.getRayTraceResult();
      if (level_0 instanceof BlockHitResult bhr) {
         Level pos = event.getProjectile().level();
         if (pos instanceof ServerLevel level) {
            BlockPos var8 = bhr.getBlockPos();
            if (level.getBlockState(var8).is((net.minecraft.world.level.block.Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get())) {
               GlobalPos gp = GlobalPos.of(level.dimension(), var8);
               ParticlesEvents.WeavePane pane = (ParticlesEvents.WeavePane)WEAVE.get(gp);
               if (pane != null) {
                  Entity shooter = event.getProjectile().getOwner();
                  if (shooter == null || !shooter.getUUID().equals(pane.owner)) {
                     pane.integrity -= 2.0F;
                     if (pane.integrity <= 0.0F) {
                        forgetPane(gp);
                        endPane(level, gp, 2, level.getGameTime());
                     }

                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLatticeCatch(BlockEvent.EntityPlaceEvent event) {
      if (event.getEntity() instanceof FallingBlockEntity) {
         LevelAccessor below = event.getLevel();
         if (below instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)below;
            BlockPos var4 = event.getPos().below();
            ParticlesEvents.WeavePane pane = (ParticlesEvents.WeavePane)WEAVE.get(GlobalPos.of(level.dimension(), var4));
            if (pane != null) {
               pane.integrity = 0.0F;
            }
         }
      }
   }

   private static void doConstruct(ServerPlayer player, ServerLevel level, long now) {
      int pat = pattern(player);
      String cdKey = String.valueOf(player.getUUID()) + "|" + pat;
      Long cd = (Long)CONSTRUCT_CD.get(cdKey);
      if (cd != null && now < cd) {
         player.displayClientMessage(Component.literal("§dThat blueprint is still reforming: §f" + (cd - now) / 20L + "s"), true);
      } else if (spendSwarm(player, 20, 14)) {
         for(ParticleConstructEntity c_0 : ownConstructs(player)) {
            if (c_0.getVariant() == pat) {
               c_0.dissolve(false);
            }
         }

         Vec3 look = player.getLookAngle();
         Vec3 at = player.position().add(look.x * 4.0D, 0.0D, look.z * 4.0D);
         BlockPos ground = BlockPos.containing(at.x, player.getY() + 0.5D, at.z);

         for(int drop = 0; drop < 6 && level.getBlockState(ground.below()).isAir(); ++drop) {
            ground = ground.below();
         }

         ParticleConstructEntity c = (ParticleConstructEntity)(com.myriam.unrp.entity.ModEntities.PARTICLE_CONSTRUCT.get()).create(level);
         if (c != null) {
            c.moveTo((double)ground.getX() + 0.5D, (double)ground.getY(), (double)ground.getZ() + 0.5D, player.getYRot(), 0.0F);
            c.setYRot(player.getYRot());
            c.configure(player.getUUID(), pat, constructMaxHp(player, pat));
            level.addFreshEntity(c);
            level.playSound((Player)null, ground, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.PLAYERS, 1.0F, 0.7F);
            level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), c.getX(), c.getY() + 1.0D, c.getZ(), 16, 0.4D, 0.8D, 0.4D, 0.05D);
            player.displayClientMessage(Component.literal("§d" + PATTERN_NAMES[pat] + " construct raised."), true);
         }
      }
   }

   private static void doFist(ServerPlayer player, ServerLevel level, long now) {
      boolean slam = player.isShiftKeyDown();
      if (slam) {
         LivingEntity target = null;
         double best = 784.0D;

         for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(28.0D), (t) -> t != player && t.isAlive() && isMarked(t))) {
            double d = player.distanceToSqr(e);
            if (d < best) {
               best = d;
               target = e;
            }
         }

         if (target == null) {
            player.displayClientMessage(Component.literal("§dThe slam needs a Marked target nearby."), true);
            return;
         }

         if (!spendSwarm(player, 14, 12)) {
            return;
         }

         ParticleFistEntity fist_0 = (ParticleFistEntity)(com.myriam.unrp.entity.ModEntities.PARTICLE_FIST.get()).create(level);
         if (fist_0 == null) {
            return;
         }

         fist_0.setPos(target.getX(), target.getY() + 9.0D, target.getZ());
         fist_0.configure(player.getUUID(), 6.0F, 3.6F, true, new Vec3(0.0D, -1.6D, 0.0D), 16);
         level.addFreshEntity(fist_0);
         level.playSound((Player)null, target.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1.0F, 0.5F);
      } else {
         if (!spendSwarm(player, 10, 8)) {
            return;
         }

         Vec3 look = player.getLookAngle();
         Vec3 at = player.getEyePosition().add(look.scale(2.4D)).add(0.0D, -0.3D, 0.0D);
         ParticleFistEntity fist = (ParticleFistEntity)(com.myriam.unrp.entity.ModEntities.PARTICLE_FIST.get()).create(level);
         if (fist == null) {
            return;
         }

         fist.setPos(at.x, at.y, at.z);
         fist.configure(player.getUUID(), 5.0F, 2.6F, false, look.scale(1.5D), 26);
         level.addFreshEntity(fist);
         level.playSound((Player)null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.7F);
      }

   }

   public static void boltImpact(ParticleBoltEntity bolt, LivingEntity target, float damage) {
      UUID ownerId = bolt.getOwnerId();
      if (ownerId != null && bolt.getServer() != null) {
         ServerPlayer owner = bolt.getServer().getPlayerList().getPlayer(ownerId);
         Level var6 = bolt.level();
         if (var6 instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)var6;
            if (owner == null || !allied(owner, target)) {
               if (owner != null) {
                  target.hurt(level.damageSources().playerAttack(owner), damage);
               } else {
                  target.hurt(level.damageSources().magic(), damage);
               }

               markTarget(target, level.getGameTime());
            }
         }
      }
   }

   public static void fistImpact(ParticleFistEntity fist, LivingEntity target, float damage) {
      UUID ownerId = fist.getOwnerId();
      if (ownerId != null && fist.getServer() != null) {
         ServerPlayer owner = fist.getServer().getPlayerList().getPlayer(ownerId);
         Level now_0 = fist.level();
         if (now_0 instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)now_0;
            if (owner == null || !allied(owner, target)) {
               long now = level.getGameTime();
               if (owner != null) {
                  target.hurt(level.damageSources().playerAttack(owner), damage);
               } else {
                  target.hurt(level.damageSources().magic(), damage);
               }

               markTarget(target, now);
               if (fist.isSlam()) {
                  if (!TimeWeft.everpresent(target)) {
                     target.setDeltaMovement(target.getDeltaMovement().x * 0.2D, -1.2D, target.getDeltaMovement().z * 0.2D);
                     target.hurtMarked = true;
                  }

                  target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, true));
               } else if (owner != null) {
                  ParticleConstructEntity toward = nearestConstruct(owner, target, 32.0D);
                  Vec3 dir = toward != null ? toward.position().subtract(target.position()).normalize() : fist.getDeltaMovement().normalize();
                  target.setDeltaMovement(dir.x * 1.1D, 0.25D, dir.z * 1.1D);
                  target.hurtMarked = true;
               }

            }
         }
      }
   }

   private static void sparkLine(ServerLevel level, Vec3 from, Vec3 to) {
      int steps = (int)Math.max(4.0D, from.distanceTo(to) * 2.0D);

      for(int i = 0; i <= steps; ++i) {
         Vec3 p = from.lerp(to, (double)i / (double)steps);
         level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0.03D, 0.03D, 0.03D, 0.0D);
         if (i % 2 == 0) {
            level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), p.x, p.y, p.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
         }
      }

   }

   private static LivingEntity lookTarget(ServerPlayer player, ServerLevel level, double range, boolean allies) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      LivingEntity best = null;
      double bestDot = 0.85D;

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range), (t) -> t != player && t.isAlive())) {
         boolean ally = allied(player, e);
         if (allies == ally) {
            Vec3 to = e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D).subtract(eye);
            if (!(to.length() > range)) {
               double dot = to.normalize().dot(look);
               if (dot > bestDot) {
                  bestDot = dot;
                  best = e;
               }
            }
         }
      }

      return best;
   }

   private static ParticleConstructEntity nearestConstruct(ServerPlayer owner, Entity near, double range) {
      ParticleConstructEntity best = null;
      double bestD = range * range;

      for(ParticleConstructEntity c : owner.serverLevel().getEntitiesOfClass(ParticleConstructEntity.class, near.getBoundingBox().inflate(range), (x) -> x.isAlive() && owner.getUUID().equals(x.getOwnerId()))) {
         double d = c.distanceToSqr(near);
         if (d < bestD) {
            bestD = d;
            best = c;
         }
      }

      return best;
   }

   private static void fireSpikeAt(ParticleConstructEntity c, ServerPlayer owner, ServerLevel level, float dmg, double range) {
      LivingEntity best = null;
      double bestD = range * range;

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, c.getBoundingBox().inflate(range), (t) -> t.isAlive() && isMarked(t))) {
         if (e instanceof ServerPlayer sp) {
            if (allied(owner, sp)) {
               continue;
            }
         }

         double d = c.distanceToSqr(e);
         if (d < bestD) {
            bestD = d;
            best = e;
         }
      }

      if (best != null) {
         Vec3 from = c.position().add(0.0D, (double)c.getBbHeight() * 0.8D, 0.0D);
         Vec3 to = best.position().add(0.0D, (double)best.getBbHeight() * 0.5D, 0.0D);
         Vec3 dir = to.subtract(from).normalize();
         ParticleBoltEntity stake = (ParticleBoltEntity)(com.myriam.unrp.entity.ModEntities.PARTICLE_BOLT.get()).create(level);
         if (stake != null) {
            stake.setPos(from.x, from.y, from.z);
            stake.configure(owner.getUUID(), 1, dmg, 1.4F, dir.scale(1.2D), 26);
            level.addFreshEntity(stake);
            level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), from.x, from.y, from.z, 4, 0.15D, 0.15D, 0.15D, 0.02D);
         }
      }
   }

   public static void tickConstruct(ParticleConstructEntity c, ServerPlayer owner) {
      int pat = pattern(owner);
      boolean awake = pat == c.getVariant() || pat == 3;
      c.setAwake(awake);
      ServerLevel level = owner.serverLevel();
      if (awake) {
         int base = UPKEEP_PER_SEC[c.getVariant()];
         int cost = pat == 3 ? Math.max(1, (base + 2) / 3) : base;
         int cur = score(owner, "unrp.aura");
         if (cur < cost) {
            c.dissolve(false);
            return;
         }

         setScore(owner, "unrp.aura", cur - cost);
      }

      c.setCustomName(Component.literal((awake ? "§d" : "§7") + PATTERN_NAMES[c.getVariant()] + " §f" + (int)c.getHp() + "/" + (int)c.getMaxHp()));
      c.setCustomNameVisible(true);
      if (c.tickCount % 60 == 0 && awake) {
         level.playSound((Player)null, c.blockPosition(), (SoundEvent)ModSounds.PARTICLES_BUZZ.get(), SoundSource.PLAYERS, 0.3F, 0.8F);
      }

      int variant = c.getVariant();
      if (variant == 0 && awake && c.tickCount % 40 == 0) {
         for(LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, c.getBoundingBox().inflate(8.0D), (t) -> t.isAlive())) {
            if (allied(owner, ally)) {
               Vec3 from = c.position().add(0.0D, 2.0D, 0.0D);
               Vec3 to = ally.position().add(0.0D, (double)ally.getBbHeight() * 0.6D, 0.0D);

               for(int i = 0; i <= 6; ++i) {
                  Vec3 pt = from.lerp(to, (double)i / 6.0D);
                  level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), pt.x, pt.y, pt.z, 1, 0.01D, 0.01D, 0.01D, 0.0D);
               }
            }
         }
      }

      if (variant == 2) {
         int cadence = awake ? 60 : 100;
         if (c.tickCount % cadence < 20) {
            fireSpikeAt(c, owner, level, 4.0F, 14.0D);
            if (awake) {
               fireSpikeAt(c, owner, level, 4.0F, 14.0D);
            }
         }
      } else if (variant == 3) {
         label128: {
            Iterator var15 = level.getEntitiesOfClass(LivingEntity.class, c.getBoundingBox().inflate(8.0D), (t) -> t.isAlive() && isMarked(t)).iterator();

            LivingEntity e;
            ServerPlayer sp;
            do {
               if (!var15.hasNext()) {
                  break label128;
               }

               e = (LivingEntity)var15.next();
               if (!(e instanceof ServerPlayer)) {
                  break;
               }

               sp = (ServerPlayer)e;
            } while(allied(owner, sp));

            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, awake ? 5 : 2, false, true));
            sparkLine(level, c.position().add(0.0D, 1.4D, 0.0D), e.position().add(0.0D, (double)e.getBbHeight() * 0.5D, 0.0D));
            if (awake && c.tickCount % 40 < 20) {
               e.hurt(level.damageSources().playerAttack(owner), 1.5F);

               for(int ring = 0; ring < 8; ++ring) {
                  double ang = (Math.PI * 2D) * (double)ring / 8.0D;
                  level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), e.getX() + Math.cos(ang) * 0.8D, e.getY() + (double)e.getBbHeight() * 0.5D, e.getZ() + Math.sin(ang) * 0.8D, 1, 0.03D, 0.03D, 0.03D, 0.0D);
               }
            }
         }
      }

      if (c.tickCount % 40 == 0) {
         level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), c.getX(), c.getY() + 1.2D, c.getZ(), awake ? 4 : 1, 0.3D, 0.6D, 0.3D, 0.01D);
      }

   }

   public static void onConstructDestroyed(ParticleConstructEntity c, DamageSource source) {
      UUID ownerId = c.getOwnerId();
      if (ownerId != null && c.getServer() != null) {
         ServerPlayer owner = c.getServer().getPlayerList().getPlayer(ownerId);
         long now = c.level().getGameTime();
         CONSTRUCT_CD.put(String.valueOf(ownerId) + "|" + c.getVariant(), now + 600L);
         if (owner != null) {
            float[] win = (float[])PENALTY.get(ownerId);
            if (win == null || (float)now - win[0] > 100.0F) {
               win = new float[]{(float)now, 0.0F};
            }

            float room = 0.1F - win[1];
            float bite = Math.min(0.07F, Math.max(0.0F, room));
            if (bite > 0.0F) {
               win[1] += bite;
               PENALTY.put(ownerId, win);
               owner.hurt(owner.serverLevel().damageSources().generic(), owner.getMaxHealth() * bite);
               owner.displayClientMessage(Component.literal("§dThe " + PATTERN_NAMES[c.getVariant()] + " construct shattered, and the feedback hurts you."), true);
            } else {
               PENALTY.put(ownerId, win);
               owner.displayClientMessage(Component.literal("§dThe " + PATTERN_NAMES[c.getVariant()] + " construct shattered."), true);
            }

         }
      }
   }

   @SubscribeEvent
   public static void onHurt(LivingHurtEvent event) {
      if (!event.getEntity().level().isClientSide()) {
         LivingEntity victim = event.getEntity();
         ServerLevel level = (ServerLevel)victim.level();
         Entity attacker = event.getSource().getEntity();
         if (attacker instanceof LivingEntity) {
            LivingEntity la = (LivingEntity)attacker;
            if (isMarked(la)) {
               for(ServerPlayer warden : level.getEntitiesOfClass(ServerPlayer.class, victim.getBoundingBox().inflate(16.0D), (w) -> w.getTags().contains("unrp.active.particles") && pattern(w) == 1)) {
                  if (victim == warden || allied(warden, victim)) {
                     event.setAmount(event.getAmount() * 0.85F);
                     level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), victim.getX(), victim.getY() + (double)victim.getBbHeight() * 0.7D, victim.getZ(), 3, 0.25D, 0.25D, 0.25D, 0.02D);
                     break;
                  }
               }
            }
         }

         for(ParticleConstructEntity pylon : level.getEntitiesOfClass(ParticleConstructEntity.class, victim.getBoundingBox().inflate(8.0D), (c) -> c.isAlive() && c.getVariant() == 0)) {
            UUID ownerId = pylon.getOwnerId();
            if (ownerId != null) {
               ServerPlayer owner = victim.getServer() == null ? null : victim.getServer().getPlayerList().getPlayer(ownerId);
               if (owner != null && allied(owner, victim)) {
                  boolean awake = pylon.isAwake();
                  if (awake || nearestAllyOf(pylon, owner, level) == victim) {
                     float share = awake ? 0.4F : 0.25F;
                     float absorbed = event.getAmount() * share;
                     event.setAmount(event.getAmount() - absorbed);
                     pylon.hurt(level.damageSources().generic(), absorbed);
                     level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), pylon.getX(), pylon.getY() + 1.5D, pylon.getZ(), 3, 0.2D, 0.3D, 0.2D, 0.02D);
                     break;
                  }
               }
            }
         }

      }
   }

   private static LivingEntity nearestAllyOf(ParticleConstructEntity pylon, ServerPlayer owner, ServerLevel level) {
      LivingEntity best = null;
      double bestD = 64.0D;

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, pylon.getBoundingBox().inflate(8.0D), (t) -> t.isAlive())) {
         if (allied(owner, e)) {
            double d = pylon.distanceToSqr(e);
            if (d < bestD) {
               bestD = d;
               best = e;
            }
         }
      }

      return best;
   }

   @SubscribeEvent
   public static void onHeal(LivingHealEvent event) {
      LivingEntity healed = event.getEntity();
      if (!healed.level().isClientSide()) {
         if (isMarked(healed)) {
            ServerLevel level = (ServerLevel)healed.level();

            for(ServerPlayer vanguard : level.getEntitiesOfClass(ServerPlayer.class, healed.getBoundingBox().inflate(16.0D), (w) -> w.getTags().contains("unrp.active.particles") && pattern(w) == 2)) {
               if (!allied(vanguard, healed)) {
                  event.setAmount(event.getAmount() * 0.65F);
                  level.sendParticles(ParticleTypes.SMOKE, healed.getX(), healed.getY() + 1.8D, healed.getZ(), 3, 0.2D, 0.1D, 0.2D, 0.01D);
                  return;
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onKnockback(LivingKnockBackEvent event) {
      LivingEntity victim = event.getEntity();
      if (!victim.level().isClientSide()) {
         if (isMarked(victim)) {
            Level var3 = victim.level();
            if (var3 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var3;

               for(ServerPlayer vanguard : level.getEntitiesOfClass(ServerPlayer.class, victim.getBoundingBox().inflate(16.0D), (w) -> w.getTags().contains("unrp.active.particles") && pattern(w) == 2)) {
                  if (!allied(vanguard, victim)) {
                     event.setStrength(event.getStrength() * 1.4F);
                     return;
                  }
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         MinecraftServer server = event.getServer();
         if (server != null) {
            long dirty = 0L;
            Iterator<ServerLevel> lit = server.getAllLevels().iterator();
            Iterator<Map.Entry<UUID, Long>> it;
            if (lit.hasNext()) {
               ServerLevel level_0 = (ServerLevel)lit.next();
               dirty = level_0.getGameTime();
            }

            tickLattice(server, dirty);
            if (server.getTickCount() % 10 == 0) {
               it = MARK_EXPIRY.entrySet().iterator();

               while(it.hasNext()) {
                  Map.Entry<UUID, Long> e_0 = (Map.Entry)it.next();
                  if (dirty >= e_0.getValue()) {
                     it.remove();

                     for(ServerLevel level_1 : server.getAllLevels()) {
                        Entity ent_0 = level_1.getEntity((UUID)e_0.getKey());
                        if (ent_0 != null) {
                           ent_0.removeTag("unrp.pmarked");
                           break;
                        }
                     }
                  }
               }

               Iterator<Map.Entry<UUID, UUID>> tit = TETHERS.entrySet().iterator();

               while(tit.hasNext()) {
                  Map.Entry<UUID, UUID> e_1 = (Map.Entry)tit.next();
                  Long until = (Long)TETHER_UNTIL.get(e_1.getKey());
                  Entity target = null;
                  Entity anchor = null;

                  for(ServerLevel level_2 : server.getAllLevels()) {
                     if (target == null) {
                        target = level_2.getEntity((UUID)e_1.getKey());
                     }

                     if (anchor == null) {
                        anchor = level_2.getEntity((UUID)e_1.getValue());
                     }
                  }

                  if (until != null && dirty < until && target != null && anchor != null && target.isAlive() && anchor.isAlive() && !TimeWeft.everpresent(target)) {
                     if (target.distanceToSqr(anchor) > 9.0D) {
                        Vec3 pull = anchor.position().subtract(target.position()).normalize().scale(0.35D);
                        target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.05D, pull.z));
                        target.hurtMarked = true;
                        Level spin_0 = target.level();
                        if (spin_0 instanceof ServerLevel) {
                           ServerLevel tl = (ServerLevel)spin_0;
                           sparkLine(tl, anchor.position().add(0.0D, 1.2D, 0.0D), target.position().add(0.0D, (double)target.getBbHeight() * 0.5D, 0.0D));
                        }
                     }
                  } else {
                     tit.remove();
                     TETHER_UNTIL.remove(e_1.getKey());
                  }
               }

               Iterator<Map.Entry<UUID, Long>> cit = CUFFS.entrySet().iterator();

               while(cit.hasNext()) {
                  Map.Entry<UUID, Long> e_2 = (Map.Entry)cit.next();
                  if (dirty >= e_2.getValue()) {
                     cit.remove();
                  } else {
                     for(ServerLevel level_3 : server.getAllLevels()) {
                        Entity ent_1 = level_3.getEntity((UUID)e_2.getKey());
                        if (ent_1 != null && ent_1.isAlive()) {
                           for(double h : new double[]{0.15D, (double)ent_1.getBbHeight() * 0.55D}) {
                              for(int ring = 0; ring < 6; ++ring) {
                                 double ang_0 = (Math.PI * 2D) * (double)ring / 6.0D;
                                 level_3.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), ent_1.getX() + Math.cos(ang_0) * 0.42D, ent_1.getY() + h, ent_1.getZ() + Math.sin(ang_0) * 0.42D, 1, 0.01D, 0.01D, 0.01D, 0.0D);
                              }
                           }
                           break;
                        }
                     }
                  }
               }

               Iterator<Map.Entry<UUID, Long>> wit = WARDS.entrySet().iterator();

               while(wit.hasNext()) {
                  Map.Entry<UUID, Long> e = (Map.Entry)wit.next();
                  if (dirty >= e.getValue()) {
                     wit.remove();
                  } else {
                     for(ServerLevel level : server.getAllLevels()) {
                        Entity ent = level.getEntity((UUID)e.getKey());
                        if (ent != null && ent.isAlive()) {
                           double spin = (double)(dirty % 60L) / 60.0D * Math.PI * 2.0D;

                           for(int ring = 0; ring < 3; ++ring) {
                              double ang = spin + (Math.PI * 2D) * (double)ring / 3.0D;
                              level.sendParticles(com.myriam.unrp.deal.PowerParticles.MOTE.get(), ent.getX() + Math.cos(ang) * 0.85D, ent.getY() + (double)ent.getBbHeight() * 0.7D, ent.getZ() + Math.sin(ang) * 0.85D, 1, 0.02D, 0.02D, 0.02D, 0.0D);
                           }
                           break;
                        }
                     }
                  }
               }

               sweepLattice(server, dirty);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player id = event.getEntity();
      if (id instanceof ServerPlayer player) {
         UUID var7 = player.getUUID();
         PENALTY.remove(var7);
         MARK_EXPIRY.remove(var7);
         TETHERS.remove(var7);
         TETHER_UNTIL.remove(var7);
         CUFFS.remove(var7);
         WARDS.remove(var7);
         ArrayList<String> stale = new ArrayList<>();

         for(String key_0 : CONSTRUCT_CD.keySet()) {
            if (key_0.startsWith(var7.toString())) {
               stale.add(key_0);
            }
         }

         for(String key : stale) {
            CONSTRUCT_CD.remove(key);
         }

         player.removeTag("unrp.pt_weave_on");
         WEAVING.remove(var7);
         WEAVE_REACH.remove(var7);
         Iterator<Map.Entry<GlobalPos, ParticlesEvents.WeavePane>> wvit = WEAVE.entrySet().iterator();

         while(wvit.hasNext()) {
            Map.Entry<GlobalPos, ParticlesEvents.WeavePane> we = (Map.Entry)wvit.next();
            if (var7.equals(((ParticlesEvents.WeavePane)we.getValue()).owner)) {
               ServerLevel wl = player.server.getLevel(((GlobalPos)we.getKey()).dimension());
               if (wl != null && wl.isLoaded(((GlobalPos)we.getKey()).pos()) && wl.getBlockState(((GlobalPos)we.getKey()).pos()).is((net.minecraft.world.level.block.Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get())) {
                  wl.removeBlock(((GlobalPos)we.getKey()).pos(), false);
               }

               wvit.remove();
            }
         }

         GROUPS.removeIf((g) -> var7.equals(g.owner));
         FX_SENT.remove(var7);

         for(ParticleConstructEntity c : player.serverLevel().getEntitiesOfClass(ParticleConstructEntity.class, player.getBoundingBox().inflate(64.0D), (x) -> var7.equals(x.getOwnerId()))) {
            c.dissolve(false);
         }

      }
   }

   private static class Aim {
      Vec3 point;
      BlockPos pane;
   }

   private static class WeaveCast {
      final WeaveBrush brush = new WeaveBrush();
      ParticlesEvents.WeaveGroup group;
      boolean consumed;
      boolean released;
      boolean dirty;
   }

   private static class WeaveGroup {
      final UUID owner;
      final ResourceKey<Level> dimension;
      final Set<GlobalPos> members = new HashSet();
      long expiry;
      boolean fading;
      ArrayDeque<GlobalPos> unravel;
      int rate;

      WeaveGroup(UUID owner, ResourceKey<Level> dimension, long expiry) {
         this.owner = owner;
         this.dimension = dimension;
         this.expiry = expiry;
      }
   }

   private static class WeavePane {
      UUID owner;
      ParticlesEvents.WeaveGroup group;
      float integrity;
      long lastHit;

      WeavePane(UUID owner, ParticlesEvents.WeaveGroup group, float integrity) {
         this.owner = owner;
         this.group = group;
         this.integrity = integrity;
         this.lastHit = 0L;
      }
   }
}
