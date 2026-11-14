package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;
import com.myriam.unrp.deal.Spectacle;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class CounterweightEvents {
   private static final String HAS = "unrp.has.counterweight";
   private static final String ACTIVE = "unrp.active.counterweight";
   private static final String WINDOW = "unrp.cw_window";
   private static final String HELD = "unrp.cw_guard_held";
   private static final String AURA = "unrp.aura";
   private static final String CHARGE = "unrp.cw_charge";
   private static final String MOMENTUM = "unrp.cw_momentum";
   private static final String STAGE = "unrp.cw_stage";
   private static final String T_ABSORB = "unrp.cw_absorb_trigger";
   private static final String T_WAVE = "unrp.cw_wave_trigger";
   private static final String T_FIST = "unrp.cw_fist_trigger";
   private static final String T_POINT = "unrp.cw_point_trigger";
   private static final String[] FEAT = new String[]{"", "", "unrp.cw_feat_recoil", "unrp.cw_feat_fulcrum", "unrp.cw_feat_crucible", "unrp.cw_feat_apex", "unrp.cw_feat_equilibrium"};
   public static final int WINDOW_TICKS = 8;
   public static final int PERFECT_TICKS = 2;
   public static final int WINDOW_CD = 50;
   public static final double BANK_MULT = 10.0D;
   public static final double PERFECT_MULT = 1.5D;
   public static final double REAR_PENALTY = 0.25D;
   public static final double FRONT_DOT = 0.5D;
   public static final double REAR_DOT = -0.3D;
   public static final double EXECUTE_FLOOR = 0.25D;
   public static final double GUARD_MULT = 4.0D;
   public static final int GUARD_AURA = 2;
   public static final int CHAIN_GAP = 10;
   public static final int CHAIN_CAP = 60;
   public static final double CHAIN_DECAY = 0.85D;
   public static final double PIERCE_FALLOFF = 0.85D;
   public static final double POINT_RANGE = 24.0D;
   public static final double PIERCE_RADIUS = 1.15D;
   public static final int VENT_CD = 60;
   public static final int PERFECT_HOLD = 3;
   public static final int REWIND_TICKS = 4;
   private static final int MODE_NONE = 0;
   private static final int MODE_PERFECT = 1;
   private static final int MODE_WINDOW = 2;
   private static final int MODE_CHAIN = 3;
   private static final int MODE_GUARD = 4;
   private static final int[] VAULT_CAP = new int[]{100, 100, 160, 220, 300, 400, 400};
   private static final int[] MOMENTUM_AT = new int[]{0, 0, 150, 400, 800, 1300, 2000};
   private static final String[] STAGE_NAME = new String[]{"", "Ballast", "Recoil", "Fulcrum", "Crucible", "Apex Lever", "Equilibrium"};
   private static final double[] CLASS_MULT = new double[]{1.0D, 1.35D, 2.0D};
   private static final String[] CLASS_NAME = new String[]{"blow", "shot", "blast"};
   private static final int AMBER = 16753964;
   private static final int SHOCK = 15398655;
   private static final int GUNMETAL = 12109008;
   private static final int WAVE_COST = 24;
   private static final int FIST_COST = 30;
   private static final int POINT_COST = 60;
   private static final int BOUND_COST = 18;
   private static final int REKNIT_TICKS = 60;
   private static final int REKNIT_PER_HEART = 6;
   private static final double WAVE_PER_CHARGE = 0.16D;
   private static final double FIST_PER_CHARGE = 0.3D;
   private static final double POINT_PER_CHARGE = 0.34D;
   private static final double POINT_BYPASS = 0.6D;
   private static final int FIGHT_TIMEOUT = 200;
   private static final long DIMINISH_WINDOW = 12000L;
   private static final Map<UUID, Integer> windowOpen = new HashMap();
   private static final Map<UUID, Integer> windowCd = new HashMap();
   private static final Map<UUID, Integer> fistArmed = new HashMap();
   private static final Map<UUID, int[]> reknit = new HashMap();
   private static final Map<UUID, int[]> fight = new HashMap();
   private static final Map<UUID, Map<UUID, ArrayDeque<Long>>> repeats = new HashMap();
   private static final Map<UUID, ArrayDeque<long[]>> caught = new HashMap();
   private static final Map<UUID, int[]> chain = new HashMap();
   private static final Map<UUID, Integer> ventCd = new HashMap();
   private static final Set<UUID> guarding = new HashSet();
   private static final Set<UUID> dealing = new HashSet();

   private static int score(ServerPlayer player, String obj) {
      ServerScoreboard sb = player.server.getScoreboard();
      Objective o = sb.getObjective(obj);
      return o == null ? 0 : sb.getOrCreatePlayerScore(player.getName().getString(), o).getScore();
   }

   private static void setScore(ServerPlayer player, String obj, int value) {
      ServerScoreboard sb = player.server.getScoreboard();
      Objective o = sb.getObjective(obj);
      if (o == null) {
         o = sb.addObjective(obj, ObjectiveCriteria.DUMMY, Component.literal(obj), RenderType.INTEGER);
      }

      sb.getOrCreatePlayerScore(player.getName().getString(), o).setScore(value);
   }

   private static void bar(ServerPlayer player, String msg) {
      player.displayClientMessage(Component.literal(msg), true);
   }

   private static boolean holds(ServerPlayer player) {
      return player.getTags().contains("unrp.has.counterweight");
   }

   private static boolean armed(ServerPlayer player) {
      Set<String> tags = player.getTags();
      return tags.contains("unrp.has.counterweight") && tags.contains("unrp.active.counterweight") && !tags.contains("unrp.power_disabled");
   }

   private static float floored(LivingEntity victim, float want) {
      if (!(victim instanceof Player)) {
         return want;
      } else {
         float head = victim.getHealth() - victim.getMaxHealth() * 0.25F;
         return head <= 0.0F ? want : Math.min(want, Math.max(0.0F, victim.getHealth() - 1.0F));
      }
   }

   public static int stage(ServerPlayer player) {
      boolean[] feats = new boolean[7];
      for (int step = 2; step <= 6; step++) {
         feats[step] = player.getTags().contains(FEAT[step]);
      }
      return stageOf(score(player, "unrp.cw_momentum"), feats);
   }

   public static int stageOf(int momentum, boolean[] feats) {
      int s = 1;

      for(int step = 2; step <= 6 && s == step - 1 && momentum >= MOMENTUM_AT[step] && feats[step]; s = step++) {
      }

      return s;
   }

   public static int ladderAt(int s) {
      return MOMENTUM_AT[Math.max(1, Math.min(6, s))];
   }

   public static String ladderName(int s) {
      return STAGE_NAME[Math.max(1, Math.min(6, s))];
   }

   public static boolean ladderTrial(ServerPlayer player, int s) {
      return s >= 2 && s <= 6 && player.getTags().contains(FEAT[s]);
   }

   public static int ladderMomentum(ServerPlayer player) {
      return Math.max(0, score(player, "unrp.cw_momentum"));
   }

   public static int vaultCap(ServerPlayer player) {
      int s = stage(player);
      int cap = VAULT_CAP[s];
      if (s >= 6) {
         cap += Math.max(0, (score(player, "unrp.cw_momentum") - MOMENTUM_AT[6]) / 40);
      }

      return cap;
   }

   private static int charge(ServerPlayer player) {
      return Math.max(0, score(player, "unrp.cw_charge"));
   }

   private static void syncStage(ServerPlayer player) {
      int s = stage(player);
      if (score(player, "unrp.cw_stage") != s) {
         setScore(player, "unrp.cw_stage", s);
      }

      for(int i = 2; i <= 6; ++i) {
         String tag = "unrp.cw_stage" + i;
         if (s >= i) {
            if (!player.getTags().contains(tag)) {
               player.addTag(tag);
            }
         } else if (player.getTags().contains(tag)) {
            player.removeTag(tag);
         }
      }

   }

   private static int classOf(DamageSource src) {
      if (src.is(DamageTypeTags.IS_EXPLOSION)) {
         return 2;
      } else {
         return !(src.getDirectEntity() instanceof Projectile) && !src.is(DamageTypeTags.IS_PROJECTILE) ? 0 : 1;
      }
   }

   private static boolean classAllowed(ServerPlayer player, int cls) {
      int s = stage(player);
      if (cls == 1) {
         return s >= 2;
      } else if (cls == 2) {
         return s >= 4;
      } else {
         return true;
      }
   }

   private static void openWindow(ServerPlayer player, ServerLevel level) {
      UUID id = player.getUUID();
      Integer cd = (Integer)windowCd.get(id);
      if (cd != null && player.tickCount < cd) {
         bar(player, "§8The guard is still settling.");
      } else {
         windowOpen.put(id, player.tickCount);
         windowCd.put(id, player.tickCount + 50);
         player.addTag("unrp.cw_window");
         CastFxEvents.pose(player, "cw_guard", 8);
         Vec3 at = player.position().add(0.0D, 1.0D, 0.0D);
         Fx.burst(level, Fx.glint(15398655), at, 16, 0.4D, 0.02D);
         Fx.ring(level, 15398655, at, 0.8D, 10);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:item.shield.block"), SoundSource.PLAYERS, 0.7F, 1.9F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:item.trident.return"), SoundSource.PLAYERS, 0.35F, 1.8F);
      }
   }

   private static void guardArc(ServerLevel level, ServerPlayer player, boolean perfect) {
      Vec3 look = player.getLookAngle();
      double yaw = Math.atan2(look.z, look.x);
      int pts = perfect ? 24 : 16;
      double r = 1.15D;

      for(double y : new double[]{player.getY() + 0.85D, player.getY() + 1.35D}) {
         for(int i = 0; i < pts; ++i) {
            double a_0 = yaw + Math.toRadians(-60.0D + 120.0D * (double)i / ((double)pts - 1.0D));
            double x_0 = player.getX() + Math.cos(a_0) * r;
            double z_0 = player.getZ() + Math.sin(a_0) * r;
            level.sendParticles(Fx.glint(perfect ? 16777215 : 15398655), x_0, y, z_0, 1, 0.0D, 0.0D, 0.0D, 0.0D);
         }
      }

      for(double edge : new double[]{-60.0D, 60.0D}) {
         double a_1 = yaw + Math.toRadians(edge);
         double x = player.getX() + Math.cos(a_1) * r;
         double z = player.getZ() + Math.sin(a_1) * r;
         level.sendParticles(Fx.flare(perfect ? 16777215 : 15398655), x, player.getY() + 1.1D, z, 2, 0.03D, 0.16D, 0.03D, 0.0D);
      }

      if (perfect) {
         for(int i = 0; i < 10; ++i) {
            double a = yaw + Math.toRadians(-52.0D + 104.0D * (double)i / 9.0D);
            level.sendParticles(Fx.flare(16777215), player.getX() + Math.cos(a) * 0.95D, player.getY() + 1.15D + Math.sin((double)i * 1.9D) * 0.18D, player.getZ() + Math.sin(a) * 0.95D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   private static void guardHoldArc(ServerLevel level, ServerPlayer player) {
      if (player.tickCount % 3 == 0) {
         Vec3 look = player.getLookAngle();
         double yaw = Math.atan2(look.z, look.x);
         double y = player.getY() + 1.15D;

         for(int i = 0; i < 11; ++i) {
            double a = yaw + Math.toRadians(-60.0D + 120.0D * (double)i / 10.0D);
            double x = player.getX() + Math.cos(a) * 1.05D;
            double z = player.getZ() + Math.sin(a) * 1.05D;
            level.sendParticles(Fx.ember(i % 2 == 0 ? 12109008 : 16753964), x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            if (i % 3 == 0) {
               level.sendParticles(Fx.glint(15398655), x, y + 0.22D, z, 1, 0.0D, -0.03D, 0.0D, 0.0D);
            }
         }

      }
   }

   private static void vaultTell(ServerLevel level, ServerPlayer player) {
      int have = charge(player);
      int cap = Math.max(1, vaultCap(player));
      double fill = Math.min(1.0D, (double)have / (double)cap);
      if (!(fill <= 0.02D)) {
         double spin = (double)player.tickCount * 0.22D;
         int motes = 1 + (int)(fill * 5.0D);

         for(int i = 0; i < motes; ++i) {
            double a_0 = spin + (Math.PI * 2D) * (double)i / (double)motes;
            Vec3 arm = armSpot(player, i % 2 == 0 ? 1.0D : -1.0D);
            level.sendParticles(Fx.ember(Fx.mix(16753964, 15398655, (float)(fill * 0.35D))), arm.x + Math.cos(a_0) * 0.22D, arm.y + Math.sin(spin + (double)i) * 0.1D, arm.z + Math.sin(a_0) * 0.22D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
         }

         if (fill >= 0.8D && player.tickCount % 8 == 0) {
            Fx.circle(level, Fx.ember(16753964), player.position().add(0.0D, 0.08D, 0.0D), 1.05D + Math.sin((double)player.tickCount * 0.09D) * 0.15D, 14, 0.02D, 0.0D);
         }

         if (fill >= 0.999D && player.tickCount % 36 == 0) {
            for(int i = 0; i < 14; ++i) {
               double h = (double)i * 0.16D;
               double a = spin + h * 2.4D;
               level.sendParticles(Fx.glint(15398655), player.getX() + Math.cos(a) * 0.55D, player.getY() + 0.2D + h, player.getZ() + Math.sin(a) * 0.55D, 1, 0.0D, 0.02D, 0.0D, 0.0D);
            }

            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.resonate"), SoundSource.PLAYERS, 0.4F, 0.7F);
         }

      }
   }

   private static void fistComet(ServerLevel level, ServerPlayer player) {
      double a = (double)player.tickCount * 0.55D;
      Vec3 fist = armSpot(player, 1.0D);
      Vec3 at = fist.add(Math.cos(a) * 0.3D, Math.sin(a * 0.7D) * 0.12D, Math.sin(a) * 0.3D);
      level.sendParticles(Fx.flare(16753964), at.x, at.y, at.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
      level.sendParticles(Fx.ember(Fx.mix(16753964, 15398655, 0.3F)), at.x - Math.cos(a) * 0.1D, at.y, at.z - Math.sin(a) * 0.1D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
      if (player.tickCount % 5 == 0) {
         level.sendParticles(Fx.glint(15398655), fist.x, fist.y, fist.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
      }

   }

   private static void groundCrack(ServerLevel level, Vec3 base, double radius, int spokes) {
      Fx.ring(level, 16753964, base.add(0.0D, 0.06D, 0.0D), radius, Math.max(10, (int)(radius * 12.0D)));

      for(int i = 0; i < spokes; ++i) {
         double a = (Math.PI * 2D) * (double)i / (double)spokes;
         Vec3 hub = base.add(0.0D, 0.06D, 0.0D);
         Fx.line(level, Fx.ember(12109008), hub, hub.add(Math.cos(a) * radius * 1.25D, 0.0D, Math.sin(a) * radius * 1.25D), 0.22D, 0.03D, 0.0D);
      }

   }

   private static void shockSpokes(ServerLevel level, Vec3 at, int n, double len, int rgb) {
      for(int i = 0; i < n; ++i) {
         double a = (Math.PI * 2D) * (double)i / (double)n;
         double pitch = i % 2 == 0 ? 0.35D : -0.15D;
         double flat = 1.0D - Math.abs(pitch);
         Fx.line(level, Fx.glint(rgb), at, at.add(Math.cos(a) * flat * len, pitch * len, Math.sin(a) * flat * len), 0.16D, 0.02D, 0.0D);
      }

   }

   private static void veins(ServerLevel level, ServerPlayer player) {
      int have = charge(player);
      if (have > 0) {
         double fill = Math.min(1.0D, (double)have / (double)Math.max(1, vaultCap(player)));
         int lines = 1 + stage(player) / 2;
         int col = Fx.mix(8014352, 16769184, (float)fill);
         Vec3 look = player.getLookAngle();
         Vec3 right = (new Vec3(-look.z, 0.0D, look.x)).normalize();

         for(double side : new double[]{-1.0D, 1.0D}) {
            Vec3 hand = armSpot(player, side);
            Vec3 elbow = player.position().add(0.0D, 1.3D, 0.0D).add(right.scale(side * 0.38D)).add(look.scale(0.08D));

            for(int k = 0; k < lines; ++k) {
               double off = ((double)k - (double)(lines - 1) * 0.5D) * 0.05D;
               Vec3 a = elbow.add(right.scale(off));
               Vec3 b = hand.add(right.scale(off));

               for(int i = 0; i < 2; ++i) {
                  Vec3 at = a.lerp(b, ((double)i + level.random.nextDouble()) / 2.0D);
                  level.sendParticles(Fx.vein(col), at.x, at.y, at.z, 0, 0.0D, -0.01D, 0.0D, 1.0D);
               }
            }
         }

      }
   }

   private static void lattice(ServerLevel level, ServerPlayer player) {
      int links = Math.min(8, chainLinks(player));
      if (links > 0) {
         Vec3 chest = player.position().add(0.0D, 1.1D, 0.0D);
         double spin = (double)player.tickCount * 0.05D;

         for(int k = 0; k < links; ++k) {
            double tilt = Math.toRadians(25.0D + 18.0D * (double)(k % 3));
            double turn = spin + Math.PI * (double)k / (double)links;

            for(int i = 0; i < 6; ++i) {
               double a = (Math.PI * 2D) * (double)i / 6.0D;
               double x = Math.cos(a);
               double y = Math.sin(a) * Math.sin(tilt);
               double z = Math.sin(a) * Math.cos(tilt);
               double rx = x * Math.cos(turn) - z * Math.sin(turn);
               double rz = x * Math.sin(turn) + z * Math.cos(turn);
               level.sendParticles(Fx.vein(i % 2 == 0 ? 15398655 : 16753964), chest.x + rx * 1.05D, chest.y + y * 1.05D, chest.z + rz * 1.05D, 0, 0.0D, 0.0D, 0.0D, 1.0D);
            }
         }

      }
   }

   private static void rearTell(ServerLevel level, ServerPlayer victim) {
      Vec3 look = victim.getLookAngle();
      Vec3 flat = new Vec3(look.x, 0.0D, look.z);
      Vec3 back = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
      Vec3 right = new Vec3(-back.z, 0.0D, back.x);
      Vec3 behind = victim.position().subtract(back.scale(0.32D));
      Vec3 from = behind.add(0.0D, 1.45D, 0.0D).add(right.scale(0.3D));
      Vec3 to = behind.add(0.0D, 0.85D, 0.0D).subtract(right.scale(0.28D));
      Fx.line(level, Fx.ember(12109008), from, to, 0.08D, 0.04D, 0.0D);
      Fx.line(level, Fx.glint(12109008), from, to, 0.16D, 0.02D, 0.0D);

      for(double side : new double[]{-1.0D, 1.0D}) {
         Vec3 arm = armSpot(victim, side);
         Fx.burst(level, Fx.vein(16753964), arm, 4, 0.1D, 0.0D);
      }

   }

   private static void heatRing(ServerLevel level, ServerPlayer player) {
      Vec3 fist = armSpot(player, 1.0D);
      Fx.ring(level, 16753964, fist, 0.3D, 6);
      Fx.circle(level, Fx.vein(Fx.mix(16753964, 16777215, 0.3F)), fist, 0.22D, 6, 0.02D, 0.03D);
   }

   private static void rewindLine(ServerLevel level, Vec3 from, Vec3 to) {
      Vec3 d = to.subtract(from);
      double len = d.length();
      if (!(len < 0.5D)) {
         Vec3 dir = d.scale(1.0D / len);

         for(int k = 0; k < 4; ++k) {
            int batch = k;
            Spectacle.after(batch, () -> {
               double a = len * (double)batch / 4.0D;
               double b = len * (double)(batch + 1) / 4.0D;

               for(double s = a; s < b; s += 0.45D) {
                  Vec3 at = from.add(dir.scale(s));
                  level.sendParticles(Fx.rewind(16753964), at.x, at.y, at.z, 0, dir.x * 0.12D, dir.y * 0.12D, dir.z * 0.12D, 1.0D);
               }

            });
         }

      }
   }

   private static Vec3 armSpot(ServerPlayer player, double side) {
      Vec3 look = player.getLookAngle();
      Vec3 right = (new Vec3(-look.z, 0.0D, look.x)).normalize();
      return player.position().add(0.0D, 1.15D, 0.0D).add(right.scale(side * 0.42D)).add(look.scale(0.32D));
   }

   private static boolean chainActive(ServerPlayer p) {
      int[] c = (int[])chain.get(p.getUUID());
      return c != null && p.tickCount - c[0] <= 10 && p.tickCount <= c[1];
   }

   public static int chainLinks(ServerPlayer p) {
      int[] c = (int[])chain.get(p.getUUID());
      return c == null ? 0 : c[2];
   }

   private static int guardMode(ServerPlayer p) {
      Integer opened = (Integer)windowOpen.get(p.getUUID());
      if (opened != null) {
         int age = p.tickCount - opened;
         if (age >= 0 && age < 2) {
            return 1;
         }

         if (age >= 0 && age <= 8) {
            return 2;
         }
      }

      if (chainActive(p)) {
         return 3;
      } else {
         return p.getTags().contains("unrp.cw_guard_held") ? 4 : 0;
      }
   }

   private static void armChain(ServerPlayer p) {
      chain.put(p.getUUID(), new int[]{p.tickCount, p.tickCount + 60, 0});
   }

   private static void touchChain(ServerPlayer p) {
      int[] c = (int[])chain.get(p.getUUID());
      if (c != null) {
         c[0] = p.tickCount;
         int var10002 = c[2]++;
      }
   }

   private static void endChain(ServerPlayer p, boolean flanked) {
      int[] c = (int[])chain.remove(p.getUUID());
      if (c != null) {
         Level at_0 = p.level();
         if (at_0 instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)at_0;
            Vec3 at = p.position().add(0.0D, 1.0D, 0.0D);
            if (!flanked && c[2] > 0) {
               Fx.shell(level, Fx.vein(16753964), at.add(0.0D, 0.1D, 0.0D), 1.0D, 10 + 4 * Math.min(8, c[2]), 0.32D);
            }

            if (flanked) {
               Fx.burst(level, Fx.ember(16734780), at, 18, 0.5D, 0.12D);
               level.playSound((Player)null, p.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.break"), SoundSource.PLAYERS, 0.8F, 0.7F);
            } else if (c[2] > 0) {
               Fx.ring(level, 16753964, at, 1.6D, 14);
               Fx.burst(level, Fx.glint(15398655), at, 12, 0.4D, 0.05D);
            }
         }

         if (c[2] > 0) {
            bar(p, flanked ? "§cCHAIN BROKEN§7: struck from behind after " + c[2] + " links." : "§7The chain settles. §e" + c[2] + " §7links.");
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onCatch(LivingHurtEvent event) {
      LivingEntity mode_0 = event.getEntity();
      if (mode_0 instanceof ServerPlayer victim) {
         if (armed(victim)) {
            int mode = guardMode(victim);
            if (mode == 0) {
               return;
            }

            Vec3 from = sourcePos(event);
            if (from == null) {
               return;
            }

            Vec3 to = from.subtract(victim.position().add(0.0D, 1.0D, 0.0D));
            if (to.lengthSqr() < 1.0E-4D) {
               return;
            }

            double dot = to.normalize().dot(victim.getLookAngle());
            if (dot < -0.3D) {
               event.setAmount(event.getAmount() * 1.25F);
               endChain(victim, true);
               Level var14 = victim.level();
               if (var14 instanceof ServerLevel) {
                  ServerLevel level = (ServerLevel)var14;
                  Fx.burst(level, Fx.ember(16734780), victim.position().add(0.0D, 1.1D, 0.0D).subtract(victim.getLookAngle().scale(0.5D)), 8, 0.3D, 0.05D);
                  rearTell(level, victim);
               }

               return;
            }

            if (dot < 0.5D) {
               return;
            }

            int cls = classOf(event.getSource());
            if (!classAllowed(victim, cls)) {
               return;
            }

            float raw = event.getAmount();
            if (raw <= 0.0F) {
               return;
            }

            event.setCanceled(true);
            Entity dust_0 = event.getSource().getDirectEntity();
            if (dust_0 instanceof Projectile) {
               Projectile shot = (Projectile)dust_0;
               Level var11 = victim.level();
               if (var11 instanceof ServerLevel) {
                  ServerLevel dust = (ServerLevel)var11;
                  Fx.burst(dust, Fx.vein(16753964), shot.position(), 10, 0.12D, 0.02D);
                  Fx.burst(dust, Fx.ember(12109008), shot.position(), 6, 0.1D, 0.01D);
               }

               shot.discard();
            }

            bank(victim, event.getSource().getEntity(), raw, mode, cls, from);
            return;
         }
      }

   }

   private static Vec3 sourcePos(LivingHurtEvent event) {
      Entity direct = event.getSource().getDirectEntity();
      if (direct != null) {
         return direct.position().add(0.0D, 0.6D, 0.0D);
      } else {
         Entity cause = event.getSource().getEntity();
         return cause != null ? cause.position().add(0.0D, 0.6D, 0.0D) : event.getSource().getSourcePosition();
      }
   }

   private static void bank(ServerPlayer victim, Entity attacker, float raw, int mode, int cls, Vec3 from) {
      boolean perfect = mode == 1;
      double mult = mode == 4 ? 4.0D : (perfect ? 15.0D : 10.0D);
      int cap = vaultCap(victim);
      int before = charge(victim);
      int gained = (int)Math.round((double)raw * mult);
      int after = Math.min(cap, before + gained);
      int spill = before + gained - after;
      setScore(victim, "unrp.cw_charge", after);
      if (perfect) {
         armChain(victim);
      } else if (mode == 3) {
         touchChain(victim);
      }

      awardMomentum(victim, attacker, raw, mode, cls);
      noteFight(victim, raw, after >= cap);
      trials(victim, attacker, raw, perfect, cls);
      Level live_0 = victim.level();
      if (live_0 instanceof ServerLevel level) {
         catchFx(level, victim, from, perfect, cls);
         if (mode == 3) {
            chainFx(level, victim, from);
         }

         if (spill > 0) {
            vent(victim, level, spill);
         }
      }

      String tag = perfect ? "§fPERFECT §6" : (mode == 3 ? "§6CHAIN x" + chainLinks(victim) + " §6" : (mode == 4 ? "§7GUARD §6" : "§6"));
      bar(victim, tag + "CAUGHT §e+" + (after - before) + " §7Charge §8(" + after + "/" + cap + ")");
      if (perfect && stage(victim) >= 6 && attacker instanceof LivingEntity live) {
         perfectCounter(victim, live);
      }

   }

   private static void chainFx(ServerLevel level, ServerPlayer victim, Vec3 from) {
      int links = Math.min(8, chainLinks(victim));
      Vec3 chest = victim.position().add(0.0D, 1.1D, 0.0D);
      double spin = (double)victim.tickCount * 0.08D;

      for(int i = 0; i < links; ++i) {
         double a = (Math.PI * 2D) * (double)i / (double)Math.max(1, links) + spin;
         double b = (Math.PI * 2D) * ((double)i + 0.45D) / (double)Math.max(1, links) + spin;
         Vec3 pa = chest.add(Math.cos(a) * 1.0D, 0.15D, Math.sin(a) * 1.0D);
         Vec3 pb = chest.add(Math.cos(b) * 1.0D, 0.15D, Math.sin(b) * 1.0D);
         Fx.line(level, Fx.ember(16753964), pa, pb, 0.16D, 0.01D, 0.0D);
         level.sendParticles(Fx.glint(15398655), pa.x, pa.y, pa.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
      }

      Fx.ring(level, 16753964, chest, 0.9D + (double)links * 0.06D, 8);
      level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.chain.place"), SoundSource.PLAYERS, 0.5F, 1.2F + (float)links * 0.09F);
   }

   private static void vent(ServerPlayer victim, ServerLevel level, int spill) {
      Integer cd = (Integer)ventCd.get(victim.getUUID());
      if (cd == null || victim.tickCount >= cd) {
         ventCd.put(victim.getUUID(), victim.tickCount + 60);
         int amp = spill >= 120 ? 2 : (spill >= 50 ? 1 : 0);
         victim.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, amp, false, true, true));
         Vec3 at = victim.position().add(0.0D, 1.0D, 0.0D);
         Fx.shell(level, Fx.flare(16753964), at, 1.8D, 48, 0.26D);
         Fx.shell(level, Fx.glint(15398655), at, 1.3D, 26, 0.18D);
         Fx.ring(level, 15398655, at, 2.0D, 16);
         Fx.ring(level, 16753964, at, 1.4D, 12);
         shockSpokes(level, at, 6, 1.6D, 16753964);
         level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.deactivate"), SoundSource.PLAYERS, 0.8F, 1.7F);
         bar(victim, "§6VAULT FULL§7: the overflow turns into Resistance.");
      }
   }

   private static void catchFx(ServerLevel level, ServerPlayer victim, Vec3 from, boolean perfect, int cls) {
      Vec3 chest = victim.position().add(0.0D, 1.1D, 0.0D);
      Vec3 dir = from.subtract(chest);
      Vec3 impact = dir.lengthSqr() < 1.0E-4D ? chest : chest.add(dir.normalize().scale(Math.min(1.1D, dir.length() * 0.55D)));
      if (perfect) {
         OriginLooks.hold(level, impact, 3);
      }

      Fx.ring(level, perfect ? 16777215 : 15398655, impact, perfect ? 1.25D : 0.85D, 14);
      Fx.ring(level, 16753964, impact, perfect ? 0.75D : 0.5D, 10);
      Fx.burst(level, Fx.flare(15398655), impact, perfect ? 30 : 18, 0.2D, 0.0D);

      for(double side : new double[]{-1.0D, 1.0D}) {
         Fx.line(level, Fx.ember(16753964), impact, armSpot(victim, side), 0.1D, 0.05D, 0.0D);
         Fx.line(level, Fx.glint(15398655), impact, armSpot(victim, side), 0.24D, 0.02D, 0.0D);
      }

      Fx.burst(level, Fx.ember(16753964), armSpot(victim, -1.0D), 10, 0.14D, 0.03D);
      Fx.burst(level, Fx.ember(16753964), armSpot(victim, 1.0D), 10, 0.14D, 0.03D);
      groundCrack(level, victim.position(), perfect ? 1.4D : 0.9D + (double)cls * 0.3D, perfect ? 8 : 5 + cls);
      if (perfect) {
         for(double shellR : new double[]{0.62D, 0.98D}) {
            for(int i = 0; i < 14; ++i) {
               double a = (Math.PI * 2D) * (double)i / 14.0D + shellR;
               level.sendParticles(Fx.glint(16777215), impact.x + Math.cos(a) * shellR, impact.y + Math.sin(a) * shellR * 0.6D, impact.z + Math.sin(a) * shellR, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
         }

         shockSpokes(level, impact, 8, 1.5D, 16777215);
         level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.chime"), SoundSource.PLAYERS, 1.0F, 1.9F);
         level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.anvil.land"), SoundSource.PLAYERS, 0.35F, 2.0F);
         level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.3F, 1.8F);
      } else {
         level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.hit"), SoundSource.PLAYERS, 0.8F, 1.4F);
         level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.glass.break"), SoundSource.PLAYERS, 0.25F, 1.9F);
      }

      if (cls == 2) {
         Fx.shell(level, Fx.ember(16753964), impact, 1.6D, 40, -0.2D);
         Fx.shell(level, Fx.glint(15398655), impact, 2.1D, 22, -0.26D);
      }

   }

   private static double diminish(ServerPlayer victim, Entity attacker) {
      if (attacker == null) {
         return 1.0D;
      } else {
         long now = victim.level().getGameTime();
         Map<UUID, ArrayDeque<Long>> per = (Map)repeats.computeIfAbsent(victim.getUUID(), (k) -> new HashMap());
         ArrayDeque<Long> dq = (ArrayDeque)per.computeIfAbsent(attacker.getUUID(), (k) -> new ArrayDeque());

         while(!dq.isEmpty() && now - dq.peekFirst() > 12000L) {
            dq.pollFirst();
         }

         int seen = dq.size();
         dq.addLast(now);
         if (seen < 3) {
            return 1.0D;
         } else {
            return seen < 6 ? 0.5D : 0.25D;
         }
      }
   }

   private static void awardMomentum(ServerPlayer victim, Entity attacker, float raw, int mode, int cls) {
      double val = (double)raw * CLASS_MULT[cls];
      if (mode == 1) {
         val *= 1.5D;
      } else if (mode == 4) {
         val *= 0.5D;
      } else if (mode == 3) {
         val *= Math.pow(0.85D, (double)Math.min(12, chainLinks(victim)));
      }

      if (!(attacker instanceof Player)) {
         if (attacker instanceof LivingEntity) {
            LivingEntity mob = (LivingEntity)attacker;
            if (mob.getMaxHealth() < 10.0F) {
               return;
            }
         }

         val *= 0.5D;
      }

      val *= diminish(victim, attacker);
      int gain = (int)Math.round(val);
      if (gain > 0) {
         int before = stage(victim);
         setScore(victim, "unrp.cw_momentum", score(victim, "unrp.cw_momentum") + gain);
         syncStage(victim);
         int now = stage(victim);
         if (now > before) {
            weighIn(victim, now);
         }

      }
   }

   private static void noteFight(ServerPlayer victim, float raw, boolean filled) {
      int[] f = (int[])fight.computeIfAbsent(victim.getUUID(), (k) -> new int[]{0, 0, 0});
      if (victim.tickCount - f[1] > 200) {
         f[0] = 0;
         f[2] = 0;
      }

      int var10002 = f[0]++;
      f[1] = victim.tickCount;
      if (filled) {
         f[2] = 1;
      }

      ArrayDeque<long[]> dq = (ArrayDeque)caught.computeIfAbsent(victim.getUUID(), (k) -> new ArrayDeque());
      long now = victim.level().getGameTime();
      dq.addLast(new long[]{now, (long)Math.ceil((double)raw)});

      while(!dq.isEmpty() && now - ((long[])dq.peekFirst())[0] > 600L) {
         dq.pollFirst();
      }

   }

   private static void grant(ServerPlayer player, int step, String why) {
      if (!player.getTags().contains(FEAT[step])) {
         player.addTag(FEAT[step]);
         player.sendSystemMessage(Component.literal("§6[Counterweight] §fTrial passed: §e" + why));
         Level var4 = player.level();
         if (var4 instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)var4;
            Fx.ring(level, 16753964, player.position().add(0.0D, 1.0D, 0.0D), 1.5D, 18);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.bell.use"), SoundSource.PLAYERS, 0.8F, 1.5F);
         }

         syncStage(player);
      }
   }

   private static void trials(ServerPlayer victim, Entity attacker, float raw, boolean perfect, int cls) {
      int[] f = (int[])fight.get(victim.getUUID());
      if (f != null && f[0] >= 10) {
         grant(victim, 2, "ten catches in one fight");
      }

      if (perfect && attacker instanceof Player) {
         grant(victim, 3, "a perfect frame on a player's hit");
      }

      ArrayDeque<long[]> dq = (ArrayDeque)caught.get(victim.getUUID());
      if (dq != null) {
         long sum = 0L;

         for(long[] e : dq) {
            sum += e[1];
         }

         if (sum >= 60L) {
            grant(victim, 4, "sixty damage caught inside thirty seconds");
         }
      }

      if (perfect && cls == 2 && raw >= 20.0F) {
         grant(victim, 5, "a perfect frame on a twenty-damage blast");
      }

   }

   private static void weighIn(ServerPlayer player, int reached) {
      player.sendSystemMessage(Component.literal("§6[Counterweight] §fThe scale settles at §e" + STAGE_NAME[reached] + "§f. Vault §e" + vaultCap(player) + "§f."));
      CastFxEvents.pose(player, "cw_guard", 20);
      Level base = player.level();
      if (base instanceof ServerLevel level) {
         Vec3 var9 = player.position();

         for(int i = 0; i < 26; ++i) {
            double h = 0.2D + (double)i * 0.28D;
            double r = 0.75D - (double)i * 0.012D;
            Fx.circle(level, Fx.ember(16753964), var9.add(0.0D, h, 0.0D), Math.max(0.15D, r), 8, 0.0D, 0.0D);
         }

         Fx.circle(level, Fx.flare(15398655), var9.add(0.0D, 0.1D, 0.0D), 1.7D, 30, 0.0D, 0.0D);
         Fx.ring(level, 16753964, var9.add(0.0D, 0.1D, 0.0D), 2.1D, 26);
         Fx.ring(level, 15398655, var9.add(0.0D, 0.1D, 0.0D), 1.2D, 20);
         CastFxEvents.poseLater(player, "cw_guard", 6, 16);
         OriginLooks.balance(player, reached, reached >= 6);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.anvil.land"), SoundSource.PLAYERS, 0.9F, 0.7F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.bell.use"), SoundSource.PLAYERS, 1.0F, 1.2F);
      }
   }

   private static LivingEntity aimed(ServerPlayer player, double range, double minDot) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      LivingEntity best = null;
      double bestDot = minDot;
      AABB box = player.getBoundingBox().inflate(range);

      for(LivingEntity live : player.serverLevel().getEntitiesOfClass(LivingEntity.class, box)) {
         if (live != player && live.isAlive() && !live.isSpectator()) {
            Vec3 to = live.position().add(0.0D, (double)live.getBbHeight() * 0.5D, 0.0D).subtract(eye);
            double dist = to.length();
            if (!(dist > range) && !(dist < 0.01D)) {
               double dot = to.normalize().dot(look);
               if (dot > bestDot) {
                  bestDot = dot;
                  best = live;
               }
            }
         }
      }

      return best;
   }

   private static int spend(ServerPlayer player, int min) {
      int have = charge(player);
      if (have < min) {
         bar(player, "§8The Vault is too light for that. §7(" + have + "/" + min + ")");
         return 0;
      } else {
         setScore(player, "unrp.cw_charge", 0);
         return have;
      }
   }

   private static void doWave(ServerPlayer player, ServerLevel level) {
      int spent = spend(player, 24);
      if (spent > 0) {
         CastFxEvents.pose(player, "cw_wave", 12);
         Vec3 eye = player.getEyePosition();
         Vec3 look = player.getLookAngle();
         float dmg = (float)((double)spent * 0.16D);

         for(LivingEntity live : player.serverLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6.5D))) {
            if (live != player && live.isAlive()) {
               Vec3 to = live.position().add(0.0D, 0.9D, 0.0D).subtract(eye);
               if (!(to.length() > 6.5D) && !(to.normalize().dot(look) < 0.35D)) {
                  live.hurt(level.damageSources().playerAttack(player), floored(live, dmg));
                  Vec3 push = to.normalize().scale(1.15D).add(0.0D, 0.42D, 0.0D);
                  live.setDeltaMovement(live.getDeltaMovement().add(push));
                  live.hurtMarked = true;
                  Fx.burst(level, Fx.wave(15398655), live.position().add(0.0D, 1.0D, 0.0D), 10, 0.35D, 0.08D);
               }
            }
         }

         Fx.cone(level, Fx.wave(15398655), eye.add(look.scale(0.6D)), look, Math.toRadians(34.0D), 110, 0.9D);
         Fx.cone(level, Fx.ember(16753964), eye.add(look.scale(0.6D)), look, Math.toRadians(26.0D), 55, 0.7D);
         Fx.cone(level, Fx.ember(12109008), eye.add(look.scale(0.4D)).add(0.0D, -0.8D, 0.0D), look, Math.toRadians(30.0D), 30, 0.45D);
         Fx.cone(level, Fx.glint(16777215), eye.add(look.scale(0.8D)), look, Math.toRadians(14.0D), 20, 1.05D);

         for(int i = 1; i <= 5; ++i) {
            Fx.ring(level, i % 2 == 0 ? 16753964 : 15398655, player.position().add(look.scale((double)i * 1.3D)).add(0.0D, 0.9D, 0.0D), 0.7D + (double)i * 0.55D, 12);
         }

         groundCrack(level, player.position().add(look.scale(1.2D)), 1.6D, 6);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.player.attack.sweep"), SoundSource.PLAYERS, 1.0F, 0.8F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:item.trident.riptide_1"), SoundSource.PLAYERS, 0.7F, 1.4F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.45F, 1.5F);
         bar(player, "§6WAVE: §7" + spent + " Charge spent wide.");
      }
   }

   private static void doFist(ServerPlayer player, ServerLevel level) {
      int spent = spend(player, 30);
      if (spent > 0) {
         fistArmed.put(player.getUUID(), spent);
         CastFxEvents.pose(player, "cw_fist", 12);
         Fx.burst(level, Fx.ember(16753964), armSpot(player, 1.0D), 24, 0.18D, 0.04D);
         Fx.ring(level, 16753964, armSpot(player, 1.0D), 0.75D, 14);
         Fx.ring(level, 15398655, armSpot(player, 1.0D), 0.45D, 10);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.anvil.place"), SoundSource.PLAYERS, 0.6F, 1.7F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.respawn_anchor.charge"), SoundSource.PLAYERS, 0.4F, 1.8F);
         bar(player, "§6FIST LOADED§7: " + spent + " Charge riding your next swing.");
      }
   }

   private static List<LivingEntity> pierceLine(ServerPlayer player, double range, double radius) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      List<LivingEntity> out = new ArrayList();
      AABB box = (new AABB(eye, eye.add(look.scale(range)))).inflate(radius + 1.0D);

      for(LivingEntity live : player.serverLevel().getEntitiesOfClass(LivingEntity.class, box)) {
         if (live != player && live.isAlive() && !live.isSpectator()) {
            Vec3 to = live.position().add(0.0D, (double)live.getBbHeight() * 0.5D, 0.0D).subtract(eye);
            double along = to.dot(look);
            if (!(along <= 0.5D) && !(along > range)) {
               double perp = to.subtract(look.scale(along)).length();
               if (!(perp > radius + (double)live.getBbWidth() * 0.5D)) {
                  out.add(live);
               }
            }
         }
      }

      out.sort((a, b) -> Double.compare(a.position().subtract(eye).dot(look), b.position().subtract(eye).dot(look)));
      return out;
   }

   private static void beam(ServerPlayer player, ServerLevel level, Vec3 from, Vec3 to, boolean counter) {
      Fx.line(level, Fx.flare(counter ? 16753964 : 15398655), from, to, 0.11D, 0.012D, 0.0D);
      Fx.line(level, Fx.glint(16777215), from, to, 0.12D, 0.0D, 0.0D);
      Fx.line(level, Fx.ember(16753964), from, to, 0.4D, 0.07D, 0.0D);
      Fx.line(level, Fx.wave(16753964), from, to, 0.5D, 0.16D, 0.0D);
      Fx.burst(level, Fx.flare(15398655), from, 12, 0.14D, 0.05D);
      Fx.ring(level, 15398655, from, 0.5D, 8);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.arrow.hit_player"), SoundSource.PLAYERS, 1.0F, counter ? 1.9F : 1.4F);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.chime"), SoundSource.PLAYERS, 0.8F, 1.7F);
   }

   private static void doPoint(ServerPlayer player, ServerLevel level) {
      if (stage(player) < 5) {
         bar(player, "§8The Point isn't yours yet.");
      } else {
         List<LivingEntity> line = pierceLine(player, 24.0D, 1.15D);
         if (line.isEmpty()) {
            bar(player, "§8Nothing on the line.");
         } else {
            int spent = spend(player, 60);
            if (spent > 0) {
               CastFxEvents.pose(player, "cw_point", 16);
               float total = (float)((double)spent * 0.34D);
               if (dealing.add(player.getUUID())) {
                  try {
                     for(int i = 0; i < line.size(); ++i) {
                        lanceOne(player, level, (LivingEntity)line.get(i), (float)((double)total * Math.pow(0.85D, (double)i)), Math.max(0.3D, Math.pow(0.85D, (double)i)));
                     }
                  } finally {
                     dealing.remove(player.getUUID());
                  }
               }

               Vec3 from = player.getEyePosition();
               beam(player, level, from, from.add(player.getLookAngle().scale(24.0D)), false);
               OriginLooks.scar(level, from, from.add(player.getLookAngle().scale(24.0D)));
               bar(player, "§6POINT: §7" + spent + " Charge through " + line.size() + (line.size() == 1 ? " target." : " targets."));
            }
         }
      }
   }

   private static void lanceOne(ServerPlayer player, ServerLevel level, LivingEntity target, float total, double size) {
      if (!(total <= 0.0F)) {
         float bypass = total * 0.6F;
         float rest = total - bypass;
         OriginMasteryEvents.hurt(player, target, level.damageSources().magic(), bypass);
         if (target.isAlive() && rest > 0.0F) {
            target.hurt(level.damageSources().playerAttack(player), rest);
         }

         Vec3 at = target.position().add(0.0D, (double)target.getBbHeight() * 0.5D, 0.0D);
         Fx.ring(level, 15398655, at, 0.95D * size, 16);
         Fx.ring(level, 16753964, at, 0.55D * size, 10);
         Fx.burst(level, Fx.flare(15398655), at, Math.max(6, (int)(24.0D * size)), 0.26D * size, 0.04D);
         Fx.cone(level, Fx.glint(15398655), at, player.getLookAngle(), Math.toRadians(12.0D), 10, 0.55D);
      }
   }

   private static void perfectCounter(ServerPlayer victim, LivingEntity attacker) {
      Level total_0 = victim.level();
      if (total_0 instanceof ServerLevel level) {
         if (attacker.isAlive()) {
            float total = (float)((double)Math.max(30, charge(victim)) * 0.34D * 0.5D);
            Vec3 chest = victim.position().add(0.0D, 1.1D, 0.0D);
            Vec3 at = attacker.position().add(0.0D, (double)attacker.getBbHeight() * 0.5D, 0.0D);
            if (dealing.add(victim.getUUID())) {
               try {
                  lanceOne(victim, level, attacker, total, 1.0D);
               } finally {
                  dealing.remove(victim.getUUID());
               }
            }

            rewindLine(level, chest, at);
            Spectacle.after(4, () -> beam(victim, level, chest, at, true));
            victim.sendSystemMessage(Component.literal("§6[Counterweight] §fSent straight back the way it came."));
            return;
         }
      }

   }

   private static void doBound(ServerPlayer player, ServerLevel level) {
      if (stage(player) < 2) {
         bar(player, "§8Kinetic Bound isn't yours yet.");
      } else if (charge(player) < 18) {
         bar(player, "§8The Vault is too light to leap.");
      } else {
         setScore(player, "unrp.cw_charge", charge(player) - 18);
         CastFxEvents.pose(player, "cw_bound", 10);
         Vec3 look = player.getLookAngle();
         player.setDeltaMovement(look.scale(1.45D).add(0.0D, 0.62D, 0.0D));
         player.hurtMarked = true;
         player.addTag("unrp.cw_softland");
         Fx.burst(level, Fx.ember(16753964), player.position(), 32, 0.45D, 0.16D);
         Fx.ring(level, 16753964, player.position().add(0.0D, 0.1D, 0.0D), 1.25D, 14);
         Fx.cone(level, Fx.ember(12109008), player.position().add(0.0D, 0.1D, 0.0D), look.scale(-1.0D).add(0.0D, -0.4D, 0.0D), Math.toRadians(24.0D), 16, 0.5D);
         groundCrack(level, player.position(), 1.1D, 6);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.player.attack.sweep"), SoundSource.PLAYERS, 0.8F, 1.6F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:item.trident.riptide_2"), SoundSource.PLAYERS, 0.5F, 1.3F);
         bar(player, "§6KINETIC BOUND");
      }
   }

   private static void doReknit(ServerPlayer player, ServerLevel level) {
      if (stage(player) < 4) {
         bar(player, "§8Reknit isn't yours yet.");
      } else if (charge(player) < 12) {
         bar(player, "§8Not enough banked to mend.");
      } else {
         reknit.put(player.getUUID(), new int[]{60, charge(player)});
         CastFxEvents.pose(player, "cw_reknit", 60);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.power_select"), SoundSource.PLAYERS, 0.8F, 1.3F);
         bar(player, "§6REKNIT§7: hold still.");
      }
   }

   @SubscribeEvent
   public static void onFistLand(LivingHurtEvent event) {
      Entity loaded_0 = event.getSource().getEntity();
      if (loaded_0 instanceof ServerPlayer player) {
         if (event.getSource().getDirectEntity() == player) {
            if (armed(player)) {
               Integer loaded = (Integer)fistArmed.remove(player.getUUID());
               if (loaded != null && loaded > 0) {
                  float bonus = (float)((double)loaded.intValue() * 0.3D);
                  LivingEntity victim = event.getEntity();
                  event.setAmount(event.getAmount() + bonus);
                  Level at_0 = player.level();
                  if (at_0 instanceof ServerLevel) {
                     ServerLevel level = (ServerLevel)at_0;
                     Vec3 at = victim.position().add(0.0D, (double)victim.getBbHeight() * 0.6D, 0.0D);
                     Vec3 foot = victim.position().add(0.0D, 0.05D, 0.0D);
                     Vec3 crown = victim.position().add(0.0D, (double)victim.getBbHeight() + 1.2D, 0.0D);
                     Fx.line(level, Fx.glint(15398655), foot, crown, 0.14D, 0.05D, 0.0D);
                     Fx.line(level, Fx.vein(16753964), foot, crown, 0.2D, 0.22D, 0.03D);
                     Fx.line(level, Fx.ember(16753964), armSpot(player, 1.0D), at, 0.1D, 0.06D, 0.0D);
                     Fx.line(level, Fx.glint(15398655), armSpot(player, 1.0D), at, 0.22D, 0.02D, 0.0D);
                     Fx.burst(level, Fx.flare(16753964), at, 34, 0.34D, 0.1D);
                     Fx.ring(level, 16753964, at, 1.5D, 16);
                     Fx.ring(level, 16753964, at, 1.05D, 14);
                     Fx.ring(level, 15398655, at, 0.6D, 10);
                     shockSpokes(level, at, 6, 1.3D, 16753964);
                     groundCrack(level, victim.position(), 1.3D, 6);

                     for(int i = 0; i < 10; ++i) {
                        double a = (Math.PI * 2D) * (double)i / 10.0D;
                        level.sendParticles(Fx.glint(16777215), at.x + Math.cos(a) * 0.7D, at.y + Math.sin(a * 1.7D) * 0.35D, at.z + Math.sin(a) * 0.7D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                     }

                     level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:entity.player.attack.crit"), SoundSource.PLAYERS, 1.0F, 0.9F);
                     level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.anvil.land"), SoundSource.PLAYERS, 0.5F, 1.6F);
                     level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.35F, 1.6F);
                  }

                  bar(player, "§6FIST §7returned " + (int)bonus + " damage.");
               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onFall(LivingHurtEvent event) {
      LivingEntity level_0 = event.getEntity();
      if (level_0 instanceof ServerPlayer player) {
         if (event.getSource().is(DamageTypeTags.IS_FALL)) {
            if (player.getTags().contains("unrp.cw_softland")) {
               if (!holds(player)) {
                  player.removeTag("unrp.cw_softland");
               } else {
                  player.removeTag("unrp.cw_softland");
                  event.setCanceled(true);
                  Level var3 = player.level();
                  if (var3 instanceof ServerLevel) {
                     ServerLevel level = (ServerLevel)var3;
                     Fx.ring(level, 16753964, player.position().add(0.0D, 0.1D, 0.0D), 1.4D, 12);
                     Fx.burst(level, Fx.ember(16753964), player.position(), 14, 0.35D, 0.05D);
                  }

               }
            }
         }
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player level_0 = event.player;
         if (level_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)level_0;
            Level id = player.level();
            if (id instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)id;
               UUID var15 = player.getUUID();
               Set<String> tags = player.getTags();
               if (tags.contains("unrp.cw_absorb_trigger")) {
                  player.removeTag("unrp.cw_absorb_trigger");
                  if (holds(player) && armed(player)) {
                     openWindow(player, level);
                  }
               }

               if (tags.contains("unrp.cw_wave_trigger")) {
                  player.removeTag("unrp.cw_wave_trigger");
                  if (holds(player) && armed(player)) {
                     if (player.isShiftKeyDown()) {
                        doBound(player, level);
                     } else {
                        doWave(player, level);
                     }
                  }
               }

               if (tags.contains("unrp.cw_fist_trigger")) {
                  player.removeTag("unrp.cw_fist_trigger");
                  if (holds(player) && armed(player)) {
                     if (player.isShiftKeyDown()) {
                        doReknit(player, level);
                     } else {
                        doFist(player, level);
                     }
                  }
               }

               if (tags.contains("unrp.cw_point_trigger")) {
                  player.removeTag("unrp.cw_point_trigger");
                  if (holds(player) && armed(player)) {
                     doPoint(player, level);
                  }
               }

               if (!holds(player)) {
                  if (windowOpen.containsKey(var15)) {
                     windowOpen.remove(var15);
                     player.removeTag("unrp.cw_window");
                  }

               } else {
                  Integer opened = (Integer)windowOpen.get(var15);
                  boolean held = tags.contains("unrp.cw_guard_held");
                  if (opened != null) {
                     int age_0 = player.tickCount - opened;
                     if (age_0 <= 8 && age_0 >= 0) {
                        guardArc(level, player, age_0 < 2);
                     } else {
                        windowOpen.remove(var15);
                        player.removeTag("unrp.cw_window");
                        opened = null;
                     }
                  }

                  if (held) {
                     guarding.add(var15);
                     if (opened == null) {
                        if (player.tickCount % 2 == 0) {
                           setScore(player, "unrp.aura", Math.max(0, score(player, "unrp.aura") - 2));
                        }

                        guardHoldArc(level, player);
                     }
                  } else if (guarding.remove(var15)) {
                     windowCd.put(var15, player.tickCount + 50);
                  }

                  if (chain.containsKey(var15) && !chainActive(player)) {
                     endChain(player, false);
                  }

                  boolean liveGuard = opened != null || held || chainActive(player);
                  if (player.tickCount % (liveGuard ? 2 : 10) == 0) {
                     int frame = 0;
                     if (opened != null) {
                        int age = player.tickCount - opened;
                        if (age >= 0 && age <= 8) {
                           frame = age + 1;
                        }
                     }

                     int flags = (held ? 1 : 0) | (chainActive(player) ? 2 : 0);
                     com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CwHudPacket(charge(player), vaultCap(player), score(player, "unrp.cw_momentum"), stage(player), frame, chainLinks(player), flags));
                  }

                  if (armed(player)) {
                     if (player.tickCount % 3 == 0) {
                        vaultTell(level, player);
                     }

                     Integer riding = (Integer)fistArmed.get(var15);
                     boolean seen = !player.isInvisible();
                     if (riding != null && riding > 0) {
                        fistComet(level, player);
                        if (seen && player.tickCount % 3 == 0) {
                           heatRing(level, player);
                        }
                     }

                     if (seen && player.tickCount % 4 == 1) {
                        veins(level, player);
                     }

                     if (seen && chainActive(player) && player.tickCount % 6 == 0) {
                        lattice(level, player);
                     }

                     if (tags.contains("unrp.cw_softland") && !player.onGround() && player.tickCount % 2 == 0) {
                        level.sendParticles(Fx.ember(16753964), player.getX(), player.getY() + 0.15D, player.getZ(), 2, 0.14D, 0.08D, 0.14D, 0.01D);
                        level.sendParticles(Fx.glint(15398655), player.getX(), player.getY() + 0.4D, player.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.0D);
                     }
                  }

                  int[] knit = (int[])reknit.get(var15);
                  if (knit != null) {
                     int var10002 = knit[0]--;
                     Vec3 chest = player.position().add(0.0D, 1.1D, 0.0D);
                     if (knit[0] % 3 == 0) {
                        Fx.circle(level, Fx.ember(16753964), chest, 0.95D, 7, 0.0D, -0.09D);
                        double rise = 1.0D - (double)knit[0] / 60.0D;
                        double a = (double)player.tickCount * 0.5D;
                        level.sendParticles(Fx.glint(15398655), player.getX() + Math.cos(a) * 0.6D * (1.0D - rise * 0.5D), player.getY() + 0.3D + rise * 1.3D, player.getZ() + Math.sin(a) * 0.6D * (1.0D - rise * 0.5D), 1, 0.0D, 0.04D, 0.0D, 0.0D);
                     }

                     if (player.hurtTime <= 0 && player.getTags().contains("unrp.active.counterweight")) {
                        if (knit[0] <= 0) {
                           reknit.remove(var15);
                           int have = charge(player);
                           int hearts = have / 6;
                           if (hearts > 0) {
                              setScore(player, "unrp.cw_charge", have - hearts * 6);
                              player.heal((float)hearts);
                              Fx.burst(level, Fx.flare(16753964), chest, 24, 0.3D, 0.05D);
                              Fx.ring(level, 16753964, chest, 1.2D, 16);
                              level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.activate"), SoundSource.PLAYERS, 0.8F, 1.6F);
                              bar(player, "§6REKNIT: §7" + hearts + " health mended.");
                           }
                        }
                     } else {
                        reknit.remove(var15);
                        bar(player, "§8The knit broke.");
                     }
                  }

                  if (player.tickCount % 20 == 0) {
                     syncStage(player);
                     int[] f = (int[])fight.get(var15);
                     if (f != null && f[1] > 0 && player.tickCount - f[1] > 200) {
                        if (f[2] == 1 && player.isAlive()) {
                           grant(player, 6, "a vault filled and carried out of the fight");
                        }

                        f[0] = 0;
                        f[2] = 0;
                        f[1] = 0;
                     }
                  }

                  if (player.tickCount % 4 == 0) {
                     idleTell(level, player);
                  }

               }
            }
         }
      }
   }

   private static void idleTell(ServerLevel level, ServerPlayer player) {
      int have = charge(player);
      if (have > 0) {
         double ratio = Math.min(1.0D, (double)have / (double)Math.max(1, vaultCap(player)));
         int motes = 1 + (int)Math.round(ratio * 3.0D);

         for(double side : new double[]{-1.0D, 1.0D}) {
            Vec3 arm = armSpot(player, side);
            level.sendParticles(Fx.ember(Fx.mix(12109008, 16753964, (float)ratio)), arm.x, arm.y, arm.z, motes, 0.08D, 0.12D, 0.08D, 0.0D);
         }

         if (ratio >= 0.8D) {
            double t = (double)(player.tickCount % 40) / 40.0D;
            Fx.circle(level, Fx.ember(16753964), player.position().add(0.0D, 0.08D, 0.0D), 1.05D + t * 0.35D, 9, 0.02D, 0.01D);
         }

         if (ratio >= 0.999D) {
            for(int i = 0; i < 3; ++i) {
               double a = level.random.nextDouble() * Math.PI * 2.0D;
               double h = 0.4D + level.random.nextDouble() * 1.4D;
               Vec3 s = player.position().add(Math.cos(a) * 0.36D, h, Math.sin(a) * 0.36D);
               Vec3 e = s.add((level.random.nextDouble() - 0.5D) * 0.75D, (level.random.nextDouble() - 0.5D) * 0.55D, (level.random.nextDouble() - 0.5D) * 0.75D);
               Fx.line(level, Fx.flare(16753964), s, e, 0.12D, 0.0D, 0.0D);
            }
         }

      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
      forget(event.getEntity().getUUID());
      event.getEntity().removeTag("unrp.cw_window");
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      forget(event.getEntity().getUUID());
   }

   static void forget(UUID id) {
      windowOpen.remove(id);
      windowCd.remove(id);
      fistArmed.remove(id);
      reknit.remove(id);
      fight.remove(id);
      chain.remove(id);
      ventCd.remove(id);
      guarding.remove(id);
      repeats.remove(id);
      caught.remove(id);
      dealing.remove(id);

      for(Map<UUID, ArrayDeque<Long>> per : repeats.values()) {
         per.remove(id);
      }

   }
}
