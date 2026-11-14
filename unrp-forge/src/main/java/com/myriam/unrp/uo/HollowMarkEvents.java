package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;
import com.myriam.unrp.deal.LifeClock;
import com.myriam.unrp.deal.Spectacle;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class HollowMarkEvents {
   private static final String HAS = "unrp.has.hollow_mark";
   private static final String ACTIVE = "unrp.active.hollow_mark";
   private static final String SPENT = "unrp.hm_spent";
   private static final String STAGE = "unrp.hm_stage";
   private static final String RANK = "unrp.hm_r";
   private static final String T_BLINK = "unrp.hm_blink_trigger";
   private static final String T_GAZE = "unrp.hm_gaze_trigger";
   private static final String T_SLAM = "unrp.hm_slam_trigger";
   private static final String T_DARTS = "unrp.hm_darts_trigger";
   public static final String[] ABILITY = new String[]{"blink", "void_gaze", "gale_slam", "bone_darts", "grasp", "briar", "gray_veil", "thrall", "sympathy", "shade_step", "calcify", "doppel", "bend_time", "void_gift"};
   public static final String[] ABILITY_NAME = new String[]{"Blink", "Void Gaze", "Gale Slam", "Bone Darts", "Grasp", "Briar", "Gray Veil", "Thrall", "Sympathy", "Shade Step", "Calcify", "Doppel", "Bend Time", "Void Gift"};
   public static final int[] BASE_COST = new int[]{0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 5, 4, 4, 6};
   public static final int ROWS = 14;
   public static final int FIRST_ROWS = 7;
   public static final int[][] MILE_AT = new int[][]{{5, 8, 12}, {5, 8}, {4, 6}, {5, 9}, {5, 8}, {4, 7}, {4, 7}, {2, 5, 9}, {2, 5, 8}, {2, 5, 8}, {2, 5, 8}, {2, 3, 7}, {2, 3, 6}, {3, 6}};
   public static final String[][] MILE_NAME = new String[][]{{"the landing blinds", "echo step", "arrival backlash"}, {"weakpoints bared", "the unveiling"}, {"hard reflect", "fliers grounded"}, {"marrow hooks", "the marrow toll"}, {"the wring", "the throw"}, {"roots", "the briar drinks"}, {"veilwalk", "sapping mist"}, {"men and the walking dead", "a second thrall", "a third thrall"}, {"a fourth thread", "the shared wound", "the death knell"}, {"movement grace", "the first cut", "shade runner"}, {"the stone takes men", "shatter", "the stone spreads"}, {"it blinks", "two of them", "the decoy bursts"}, {"the true stop", "the long stop", "the moment after"}, {"two gifts at once", "the gift spreads"}};
   private static final String[] STAGE_NAME = new String[]{"", "Marked", "Favored", "Chosen", "Hollow"};
   private static final int[] STAGE_AT = new int[]{0, 0, 8, 20, 40};
   private static final int ASH = 10133670;
   private static final int BONE = 15262938;
   private static final int GLYPH = 8381416;
   private static final int PALE = 6253168;
   public static final double BLINK_RANGE_1 = 9.0D;
   public static final double BLINK_RANGE_2 = 12.0D;
   public static final double BLINK_RANGE_3 = 24.0D;
   public static final int BLINK_COST = 12;
   public static final int BLINK_FALL_GRACE = 40;
   public static final int GAZE_COST = 10;
   public static final int SLAM_COST = 18;
   public static final int DARTS_COST = 14;
   public static final int GRASP_COST = 20;
   public static final int BRIAR_COST = 22;
   public static final int VEIL_COST = 18;
   public static final int CHARM_SLOTS = 3;
   public static final int DART_CAP = 15;
   public static final int DART_MAX_PER_TARGET = 5;
   public static final float DART_SHRAPNEL = 0.4F;
   public static final double DART_SHRAPNEL_REACH = 1.5D;
   public static final float SLAM_ECHO = 0.6F;
   public static final int SLAM_ECHO_DELAY = 10;
   public static final int BACKLASH_DELAY = 8;
   public static final double THROW_REACH = 3.0D;
   public static final int MARK_CAP = 32;
   private static final Map<UUID, int[]> grasped = new HashMap();
   private static final Map<UUID, Object[]> briar = new HashMap();
   private static final Map<UUID, Object[]> veil = new HashMap();
   private static final Map<UUID, Integer> lastBlink = new HashMap();
   private static final Map<UUID, Integer> fallGrace = new HashMap();
   private static final Map<UUID, Map<UUID, Integer>> marked = new HashMap();
   private static final Map<UUID, UUID> graspTarget = new HashMap();

   public static float ruptureDamage(int r) {
      return 2.0F + 0.9F * (float)r;
   }

   public static double ruptureReach(int r) {
      return Math.min(8.0D, 2.5D + 0.15D * (double)r);
   }

   public static float slamDamage(int r) {
      return 5.0F + 2.4F * (float)r;
   }

   public static double slamEchoReach(int r) {
      return Math.min(10.0D, 3.0D + 0.25D * (double)r);
   }

   public static float dartDamage(int r) {
      return 5.0F + 2.2F * (float)r;
   }

   public static int dartCount(int r) {
      return Math.min(15, (r >= 3 ? 5 : 3) + Math.max(0, (r - 3) / 2));
   }

   public static float wring(int r) {
      return 1.0F + 0.3F * (float)r;
   }

   public static float throwDamage(int r) {
      return 4.0F + 1.0F * (float)r;
   }

   public static float briarBite(int r) {
      return 3.0F + 0.8F * (float)r;
   }

   public static float veilChoke(int r) {
      return 1.0F + 0.4F * (float)r;
   }

   public static float edgeMult(int stage) {
      return 1.35F + 0.1F * (float)stage;
   }

   private static DamageSource grey(ServerPlayer player, ServerLevel level) {
      return level.damageSources().indirectMagic(player, player);
   }

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
      return player.getTags().contains("unrp.has.hollow_mark");
   }

   public static boolean holdsMark(ServerPlayer player) {
      return holds(player);
   }

   public static boolean armedFor(ServerPlayer player) {
      return armed(player);
   }

   public static boolean payFor(ServerPlayer player, int cost) {
      return pay(player, cost);
   }

   public static void refundFor(ServerPlayer player, int amount) {
      refund(player, amount);
   }

   public static void say(ServerPlayer player, String msg) {
      bar(player, msg);
   }

   public static DamageSource greyFor(ServerPlayer player, ServerLevel level) {
      return grey(player, level);
   }

   private static boolean armed(ServerPlayer player) {
      Set<String> tags = player.getTags();
      return tags.contains("unrp.has.hollow_mark") && tags.contains("unrp.active.hollow_mark") && !tags.contains("unrp.power_disabled");
   }

   public static int rank(ServerPlayer player, int ability) {
      int r = score(player, "unrp.hm_r" + ability);
      return ability == 0 && r < 1 ? 1 : Math.max(0, r);
   }

   public static int costOf(ServerPlayer player, int ability) {
      return costOf(ability, rank(player, ability));
   }

   public static int costOf(int ability, int rank) {
      int next = rank + 1;
      int steps = Math.max(0, next - 1) / 10;
      if (steps >= 31) {
         return 999999;
      } else {
         long c = (long)(BASE_COST[ability] + (next - 1)) << steps;
         if (ability == 12 && next >= 2) {
            c += 3L << steps;
         }

         return (int)Math.max(1L, Math.min(999999L, c));
      }
   }

   public static String nextMile(int ability, int rank) {
      for(int i = 0; i < MILE_AT[ability].length; ++i) {
         if (rank < MILE_AT[ability][i]) {
            return "r" + MILE_AT[ability][i] + " " + MILE_NAME[ability][i];
         }
      }

      return "";
   }

   private static void refund(ServerPlayer player, int amount) {
      ServerScoreboard sb = player.server.getScoreboard();
      Objective o = sb.getObjective("unrp.aura");
      if (o != null) {
         Score s = sb.getOrCreatePlayerScore(player.getName().getString(), o);
         s.setScore(s.getScore() + amount);
      }
   }

   public static int stage(ServerPlayer player) {
      return stageFor(score(player, "unrp.hm_spent"));
   }

   public static int stageFor(int spent) {
      int s = 1;

      for(int i = 2; i <= 4; ++i) {
         if (spent >= STAGE_AT[i]) {
            s = i;
         }
      }

      return s;
   }

   public static int ladderAt(int s) {
      return STAGE_AT[Math.max(1, Math.min(4, s))];
   }

   public static String ladderName(int s) {
      return STAGE_NAME[Math.max(1, Math.min(4, s))];
   }

   public static int ladderSpent(ServerPlayer player) {
      return Math.max(0, score(player, "unrp.hm_spent"));
   }

   public static int ladderRunes(ServerPlayer player) {
      return runesHeld(player);
   }

   public static int charmCap(ServerPlayer player) {
      return stage(player) >= 4 ? 4 : 3;
   }

   private static List<String> charms(ServerPlayer player) {
      List<String> out = new ArrayList();

      for(String kind : BoneCharmItem.KINDS) {
         if (player.getTags().contains("unrp.hm_charm." + kind)) {
            out.add(kind);
         }
      }

      return out;
   }

   public static boolean attuneCharm(ServerPlayer player, String kind) {
      if (!holds(player)) {
         bar(player, "§8The charm is inert in your hand.");
         return false;
      } else {
         String tag = "unrp.hm_charm." + kind;
         if (player.getTags().contains(tag)) {
            unattune(player, kind);
            return false;
         } else if (charms(player).size() >= charmCap(player)) {
            bar(player, "§8All " + charmCap(player) + " charm slots are full.");
            return false;
         } else {
            player.addTag(tag);
            applyCharms(player);
            Level var4 = player.level();
            if (var4 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var4;
               Fx.ring(level, 8381416, player.position().add(0.0D, 1.0D, 0.0D), 1.1D, 14);
               Fx.burst(level, Fx.ember(15262938), player.position().add(0.0D, 1.1D, 0.0D), 12, 0.3D, 0.02D);
               level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.bone_block.place"), SoundSource.PLAYERS, 0.8F, 0.7F);
            }

            bar(player, "§bCharm attuned §8(" + charms(player).size() + "/" + charmCap(player) + ")");
            return true;
         }
      }
   }

   private static boolean unattune(ServerPlayer player, String kind) {
      String tag = "unrp.hm_charm." + kind;
      if (!player.getTags().contains(tag)) {
         return false;
      } else {
         player.removeTag(tag);
         applyCharms(player);
         ItemStack back = BoneCharmItem.of((Item)com.myriam.unrp.item.ModItems.BONE_CHARM.get(), kind);
         if (!player.getInventory().add(back)) {
            player.drop(back, false);
         }

         bar(player, "§7Charm released. It returns to your pack.");
         return true;
      }
   }

   public static void releaseCharm(ServerPlayer player, int index) {
      if (index >= 0 && index < BoneCharmItem.KINDS.length) {
         unattune(player, BoneCharmItem.KINDS[index]);
      }

      openCodex(player);
   }

   private static void applyCharms(ServerPlayer player) {
      List<String> worn = charms(player);
      if (worn.contains("sight")) {
         player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, false));
      }

      if (worn.contains("ward")) {
         player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0, true, false, false));
      }

   }

   private static void syncStage(ServerPlayer player) {
      int s = stage(player);
      if (score(player, "unrp.hm_stage") != s) {
         setScore(player, "unrp.hm_stage", s);
      }

      for(int i = 2; i <= 4; ++i) {
         String tag = "unrp.hm_stage" + i;
         if (s >= i) {
            if (!player.getTags().contains(tag)) {
               player.addTag(tag);
            }
         } else if (player.getTags().contains(tag)) {
            player.removeTag(tag);
         }
      }

   }

   private static int runesHeld(ServerPlayer player) {
      int n = 0;

      for(ItemStack stack : player.getInventory().items) {
         if (stack.is((Item)com.myriam.unrp.item.ModItems.CARVED_RUNE.get())) {
            n += stack.getCount();
         }
      }

      return n;
   }

   private static boolean takeRunes(ServerPlayer player, int want) {
      if (runesHeld(player) < want) {
         return false;
      } else {
         int left = want;

         for(ItemStack stack : player.getInventory().items) {
            if (left <= 0) {
               break;
            }

            if (stack.is((Item)com.myriam.unrp.item.ModItems.CARVED_RUNE.get())) {
               int take = Math.min(left, stack.getCount());
               stack.shrink(take);
               left -= take;
            }
         }

         return left <= 0;
      }
   }

   public static void openCodex(ServerPlayer player) {
      int[] ranks = new int[14];
      int[] costs = new int[14];

      for(int i = 0; i < 14; ++i) {
         ranks[i] = rank(player, i);
         costs[i] = costOf(player, i);
      }

      int worn = 0;

      for(int k = 0; k < BoneCharmItem.KINDS.length; ++k) {
         if (player.getTags().contains("unrp.hm_charm." + BoneCharmItem.KINDS[k])) {
            worn |= 1 << k;
         }
      }

      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CodexSyncPacket(runesHeld(player), score(player, "unrp.hm_spent"), stage(player), ranks, costs, worn, charmCap(player)));
   }

   public static void buyRank(ServerPlayer player, int ability) {
      if (holds(player) && ability >= 0 && ability < 14) {
         int cost = costOf(player, ability);
         if (cost > 0 && !takeRunes(player, cost)) {
            bar(player, "§8You don't have enough runes. §7(" + runesHeld(player) + "/" + cost + ")");
            openCodex(player);
         } else {
            setScore(player, "unrp.hm_r" + ability, rank(player, ability) + 1);
            HollowDeepFavors.syncOwned(player);
            int before = stage(player);
            setScore(player, "unrp.hm_spent", score(player, "unrp.hm_spent") + cost);
            syncStage(player);
            Level tease_0 = player.level();
            if (tease_0 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)tease_0;
               runeBreak(level, player);
            }

            int newRank = rank(player, ability);
            player.sendSystemMessage(Component.literal("§b[Hollow Mark] §f" + ABILITY_NAME[ability] + " §7-> rank §b" + newRank));

            for(int i = 0; i < MILE_AT[ability].length; ++i) {
               if (newRank == MILE_AT[ability][i]) {
                  player.sendSystemMessage(Component.literal("§b[Hollow Mark] §fA favor deepens: §b" + MILE_NAME[ability][i]));
                  Level var7 = player.level();
                  if (var7 instanceof ServerLevel) {
                     ServerLevel lv = (ServerLevel)var7;
                     Fx.ring(lv, 8381416, player.position().add(0.0D, 1.0D, 0.0D), 1.6D, 18);
                     Fx.shell(lv, Fx.ember(8381416), player.position().add(0.0D, 1.1D, 0.0D), 1.4D, 26, 0.3D);
                     lv.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sculk_shrieker.shriek"), SoundSource.PLAYERS, 1.0F, 1.3F);
                  }
               }
            }

            String tease = nextMile(ability, newRank);
            if (!tease.isEmpty()) {
               bar(player, "§8Next milestone: §7" + tease);
            }

            if (stage(player) > before) {
               brandDeepens(player, stage(player));
            }

            openCodex(player);
         }
      }
   }

   private static void runeBreak(ServerLevel level, ServerPlayer player) {
      Vec3 hand = player.position().add(0.0D, 1.15D, 0.0D);
      Fx.burst(level, Fx.ember(15262938), hand, 18, 0.25D, 0.05D);
      Fx.ring(level, 8381416, hand, 0.9D, 14);

      for(int i = 0; i < 10; ++i) {
         double a = (Math.PI * 2D) * (double)i / 10.0D;
         Fx.line(level, Fx.ember(8381416), hand, hand.add(Math.cos(a) * 0.7D, 0.35D, Math.sin(a) * 0.7D), 0.25D, 0.02D, 0.0D);
      }

      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sculk_shrieker.shriek"), SoundSource.PLAYERS, 0.7F, 0.6F);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.elder_guardian.curse"), SoundSource.PLAYERS, 0.35F, 1.6F);
   }

   private static void brandDeepens(ServerPlayer player, int reached) {
      player.sendSystemMessage(Component.literal("§b[Hollow Mark] §fThe brand spreads: §b" + STAGE_NAME[reached]));
      CastFxEvents.pose(player, "hm_brand", 26);
      Level c = player.level();
      if (c instanceof ServerLevel level) {
         Vec3 var5 = player.position().add(0.0D, 1.1D, 0.0D);

         for(int r = 1; r <= 4; ++r) {
            Fx.circle(level, Fx.ember(r % 2 == 0 ? 8381416 : 10133670), var5, (double)r * 0.55D, 14, 0.0D, 0.0D);
         }

         Fx.shell(level, Fx.ember(6253168), var5, 2.2D, 40, -0.14D);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sculk_shrieker.shriek"), SoundSource.PLAYERS, 1.0F, 0.5F);
      }
   }

   @SubscribeEvent
   public static void onBoneDrops(LivingDropsEvent event) {
      LivingEntity dead = event.getEntity();
      if (!dead.level().isClientSide()) {
         Level killer_0 = dead.level();
         if (killer_0 instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)killer_0;
            Entity id_0 = event.getSource().getEntity();
            if (id_0 instanceof ServerPlayer) {
               ServerPlayer killer = (ServerPlayer)id_0;
               if (armed(killer)) {
                  String id = BuiltInRegistries.ENTITY_TYPE.getKey(dead.getType()).toString();
                  int runes = 0;
                  int charms = 0;
                  if (id.equals("minecraft:warden")) {
                     runes = 3 + level.random.nextInt(3);
                     charms = 1;
                  } else if (!id.equals("minecraft:wither") && !id.equals("minecraft:ender_dragon")) {
                     if (!id.equals("minecraft:wither_skeleton") && !id.equals("minecraft:ravager")) {
                        if (!id.equals("minecraft:evoker") && !id.equals("minecraft:elder_guardian") && !id.equals("minecraft:piglin_brute")) {
                           return;
                        }

                        if (level.random.nextFloat() < 0.14F) {
                           runes = 1;
                        }
                     } else {
                        if (level.random.nextFloat() < 0.22F) {
                           runes = 1;
                        }

                        if (level.random.nextFloat() < 0.08F) {
                           charms = 1;
                        }
                     }
                  } else {
                     runes = 4 + level.random.nextInt(3);
                     charms = 1;
                  }

                  runes = HollowDeepFavors.greed(killer, runes, level);
                  if (runes > 0) {
                     event.getDrops().add(new ItemEntity(level, dead.getX(), dead.getY() + 0.4D, dead.getZ(), new ItemStack(com.myriam.unrp.item.ModItems.CARVED_RUNE.get(), runes)));
                  }

                  if (charms > 0) {
                     String kind = BoneCharmItem.randomKind(level.random);
                     event.getDrops().add(new ItemEntity(level, dead.getX(), dead.getY() + 0.4D, dead.getZ(), BoneCharmItem.of((Item)com.myriam.unrp.item.ModItems.BONE_CHARM.get(), kind)));
                  }

                  if (runes > 0 || charms > 0) {
                     Fx.burst(level, Fx.ember(8381416), dead.position().add(0.0D, 0.8D, 0.0D), 20, 0.4D, 0.05D);
                     Fx.ring(level, 15262938, dead.position().add(0.0D, 0.6D, 0.0D), 1.2D, 16);
                     level.playSound((Player)null, dead.blockPosition(), CastFxEvents.sound("minecraft:block.bone_block.break"), SoundSource.PLAYERS, 0.9F, 0.6F);
                     bar(killer, "§bThe bone gives up its cut.");
                  }

                  return;
               }
            }

            return;
         }
      }

   }

   private static boolean pay(ServerPlayer player, int cost) {
      ServerScoreboard sb = player.server.getScoreboard();
      Objective o = sb.getObjective("unrp.aura");
      if (o == null) {
         return true;
      } else {
         int have = sb.getOrCreatePlayerScore(player.getName().getString(), o).getScore();
         if (have < cost) {
            bar(player, "§8Not enough aura.");
            return false;
         } else {
            sb.getOrCreatePlayerScore(player.getName().getString(), o).setScore(have - cost);
            return true;
         }
      }
   }

   private static boolean clear(ServerLevel level, BlockPos at) {
      BlockState st = level.getBlockState(at);
      return st.getFluidState().isEmpty() && st.getCollisionShape(level, at).isEmpty();
   }

   private static Vec3 perch(ServerPlayer player, ServerLevel level, Vec3 look, double range, int rank) {
      Vec3 eye = player.getEyePosition();
      BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(range)), Block.COLLIDER, Fluid.NONE, player));
      if (hit.getType() == Type.BLOCK && (hit.getDirection() != Direction.DOWN || rank >= 4)) {
         BlockPos on = hit.getBlockPos();
         VoxelShape shape = level.getBlockState(on).getCollisionShape(level, on);
         double top = shape.isEmpty() ? 1.0D : shape.max(Axis.Y);
         return clear(level, on.above()) && clear(level, on.above(2)) && (!(top > 1.0D) || clear(level, on.above(3))) ? new Vec3((double)on.getX() + 0.5D, (double)on.getY() + top, (double)on.getZ() + 0.5D) : null;
      } else {
         return null;
      }
   }

   private static void doBlink(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 0);
      double range = r >= 4 ? Math.min(64.0D, 30.0D + 4.0D * (double)(r - 4)) : (r >= 3 ? 24.0D : (r >= 2 ? 12.0D : 9.0D));
      range += HollowDeepFavors.blinkBonus(player);
      int cost = Math.max(6, 12 - r / 3);
      Integer last = (Integer)lastBlink.get(player.getUUID());
      boolean echo = r >= MILE_AT[0][1] && last != null && LifeClock.now(player) >= last && LifeClock.now(player) - last <= 60;
      if (echo) {
         cost = Math.max(3, cost / 2);
      }

      if (pay(player, cost)) {
         Vec3 from = player.position();
         Vec3 look = player.getLookAngle();
         Vec3 best = from;
         Vec3 perch = perch(player, level, look, range, r);
         if (perch != null) {
            best = perch;
         }

         for(double d = 1.0D; perch == null && d <= range; d += 0.5D) {
            Vec3 probe = player.getEyePosition().add(look.scale(d));
            BlockPos bp = BlockPos.containing(probe);
            boolean open = level.getBlockState(bp).isAir() && level.getBlockState(bp.below()).isAir();
            if (open) {
               best = new Vec3(probe.x, probe.y - 1.2D, probe.z);
            } else if (r < 4) {
               break;
            }
         }

         if (best.equals(from)) {
            bar(player, "§8Nowhere to step.");
         } else {
            CastFxEvents.pose(player, "hm_blink", 10);
            afterimage(level, from, player.getBbHeight());
            trail(level, from, best);
            player.teleportTo(best.x, best.y, best.z);
            player.setDeltaMovement(look.x * 0.35D, Math.min(0.1D, player.getDeltaMovement().y), look.z * 0.35D);
            player.hurtMarked = true;
            player.fallDistance = 0.0F;
            if (r < 3) {
               player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 12, 1, false, false, false));
               bar(player, "§8You land badly.");
            }

            lastBlink.put(player.getUUID(), LifeClock.now(player));
            fallGrace.put(player.getUUID(), LifeClock.now(player) + 40);
            Vec3 landing = best.add(0.0D, 1.0D, 0.0D);
            Fx.burst(level, Fx.ember(10133670), landing, Math.min(60, 24 + 2 * r), 0.4D, 0.04D);
            Fx.burst(level, Fx.glint(8381416), landing, 10 + r, 0.3D, 0.05D);
            Fx.ring(level, 8381416, best.add(0.0D, 0.1D, 0.0D), 1.1D, 12);
            Fx.line(level, Fx.ember(15262938), best, best.add(0.0D, 2.1D, 0.0D), 0.24D, 0.06D, 0.0D);
            double reach = ruptureReach(r);
            float lash = 1.5F + 0.5F * (float)r;

            for(LivingEntity live : OriginStrike.foesNear(level, player, landing, reach)) {
               OriginStrike.hitFresh(live, level.damageSources().playerAttack(player), ruptureDamage(r));
               if (r >= MILE_AT[0][0]) {
                  live.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40 + 2 * r, 0, false, true, true));
                  live.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, false, true, true));
               }
            }

            Spectacle.shockwave(level, 8381416, best, reach);
            Spectacle.pillar(level, Fx.ember(6253168), best, 2.6D);
            Fx.circle(level, Fx.ember(10133670), landing, reach * 0.8D, 18, 0.0D, 0.02D);
            Fx.ring(level, 6253168, best.add(0.0D, 0.15D, 0.0D), reach, 16);
            if (r >= MILE_AT[0][2]) {
               Vec3 spot = best;
               Spectacle.after(8, () -> backlash(player, level, spot, reach, lash));
            }

            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.enderman.teleport"), SoundSource.PLAYERS, 0.7F, 1.7F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sculk_catalyst.bloom"), SoundSource.PLAYERS, 0.6F, 1.4F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_cluster.break"), SoundSource.PLAYERS, 0.4F, 0.7F);
         }
      }
   }

   private static void backlash(ServerPlayer player, ServerLevel level, Vec3 spot, double reach, float lash) {
      if (!player.isRemoved() && player.level() == level) {
         Vec3 mid = spot.add(0.0D, 1.0D, 0.0D);

         for(LivingEntity live : OriginStrike.foesNear(level, player, mid, reach)) {
            OriginStrike.hitFresh(live, grey(player, level), lash);
            Vec3 in = new Vec3(spot.x - live.getX(), 0.0D, spot.z - live.getZ());
            Vec3 dir = in.lengthSqr() < 1.0E-4D ? Vec3.ZERO : in.normalize();
            OriginStrike.shove(live, dir.x * 0.5D, 0.1D, dir.z * 0.5D);
         }

         Fx.shell(level, Fx.ember(8381416), mid, reach, 30, -0.25D);
         Spectacle.shockwave(level, 6253168, spot, reach * 0.7D);
         level.playSound((Player)null, BlockPos.containing(spot), CastFxEvents.sound("minecraft:block.sculk_shrieker.hit"), SoundSource.PLAYERS, 0.8F, 0.5F);
      }
   }

   private static void afterimage(ServerLevel level, Vec3 at, float height) {
      for(double h : new double[]{0.35D, 0.85D, 1.35D}) {
         Fx.circle(level, Fx.ember(10133670), at.add(0.0D, h, 0.0D), h > 1.0D ? 0.26D : 0.34D, 10, 0.0D, -0.02D);
      }

      Fx.line(level, Fx.ember(6253168), at, at.add(0.0D, (double)height, 0.0D), 0.16D, 0.08D, 0.0D);
      Fx.ring(level, 8381416, at.add(0.0D, 0.1D, 0.0D), 0.8D, 10);
      Fx.burst(level, Fx.glint(8381416), at.add(0.0D, (double)height * 0.6D, 0.0D), 8, 0.25D, -0.06D);
      level.playSound((Player)null, BlockPos.containing(at), CastFxEvents.sound("minecraft:block.sculk_shrieker.hit"), SoundSource.PLAYERS, 0.5F, 1.8F);
   }

   private static void trail(ServerLevel level, Vec3 from, Vec3 to) {
      Fx.line(level, Fx.ember(6253168), from.add(0.0D, 1.0D, 0.0D), to.add(0.0D, 1.0D, 0.0D), 0.22D, 0.05D, 0.0D);
      Fx.line(level, Fx.glint(8381416), from.add(0.0D, 1.1D, 0.0D), to.add(0.0D, 1.1D, 0.0D), 0.45D, 0.02D, 0.0D);

      for(int i = 1; i <= 6; ++i) {
         Vec3 ghost = from.add(to.subtract(from).scale((double)i / 7.0D)).add(0.0D, 1.0D, 0.0D);
         Fx.circle(level, Fx.ember(i % 2 == 0 ? 10133670 : 6253168), ghost, 0.36D - (double)i * 0.03D, 8, 0.0D, 0.0D);
      }

   }

   private static void doGaze(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 1);
      if (r < 1) {
         bar(player, "§8Void Gaze isn't yours yet.");
      } else if (pay(player, 10)) {
         double range = Math.min(120.0D, 18.0D + (double)r * 8.0D) * HollowDeepFavors.gazeBoost(player);
         int seen = 0;

         for(LivingEntity live : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range))) {
            if (live != player && live.isAlive()) {
               live.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120 + r * 40, 0, false, false, false));
               Map<UUID, Integer> marks = (Map)marked.computeIfAbsent(player.getUUID(), (k) -> new HashMap());
               if (OriginStrike.threat(player, live) && (marks.containsKey(live.getUUID()) || marks.size() < 32)) {
                  marks.put(live.getUUID(), LifeClock.now(player) + 120 + r * 40);
               }

               if (r >= MILE_AT[1][1]) {
                  live.removeEffect(MobEffects.INVISIBILITY);
               }

               ++seen;
               if (r >= 3 && !SgRules.outside(live)) {
                  float frac = live.getHealth() / Math.max(1.0F, live.getMaxHealth());
                  Fx.burst(level, Fx.ember(frac < 0.35F ? 13130330 : 15262938), live.position().add(0.0D, (double)live.getBbHeight() + 0.3D, 0.0D), 4, 0.15D, 0.0D);
               }
            }
         }

         if (r >= MILE_AT[1][1]) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false, false));
         }

         CastFxEvents.pose(player, "hm_gaze", 14);
         Fx.shell(level, Fx.ember(8381416), player.position().add(0.0D, 1.1D, 0.0D), 2.4D, 44, 0.2D);
         Fx.ring(level, 8381416, player.position().add(0.0D, 0.1D, 0.0D), 2.2D, 18);
         Fx.ring(level, 6253168, player.position().add(0.0D, 0.1D, 0.0D), 3.4D, 22);
         Fx.burst(level, Fx.glint(8381416), player.getEyePosition(), 8, 0.2D, 0.04D);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sculk_sensor.clicking"), SoundSource.PLAYERS, 0.8F, 0.7F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.ambient"), SoundSource.PLAYERS, 0.6F, 1.5F);
         bar(player, "§bVOID GAZE§8: " + seen + " shapes in the grey.");
      }
   }

   private static void doSlam(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 2);
      if (r < 1) {
         bar(player, "§8Gale Slam isn't yours yet.");
      } else if (pay(player, 18)) {
         CastFxEvents.pose(player, "hm_slam", 14);
         Vec3 eye = player.getEyePosition();
         Vec3 look = player.getLookAngle();
         double reach = Math.min(24.0D, 7.0D + (double)r * 1.5D);
         double arc = r >= 2 ? 0.25D : 0.4D;

         for(Entity e : level.getEntities(player, player.getBoundingBox().inflate(reach))) {
            Vec3 to = e.position().add(0.0D, 0.8D, 0.0D).subtract(eye);
            if (!(to.length() > reach) && !(to.normalize().dot(look) < arc)) {
               if (e instanceof Projectile) {
                  Projectile shot = (Projectile)e;
                  shot.setDeltaMovement(shot.getDeltaMovement().scale(-1.15D));
                  shot.hurtMarked = true;
                  if (r >= MILE_AT[2][0]) {
                     shot.setDeltaMovement(shot.getDeltaMovement().scale(1.6D));
                  }

                  Fx.burst(level, Fx.ember(15262938), shot.position(), 8, 0.2D, 0.05D);
               } else if (e instanceof LivingEntity) {
                  LivingEntity live = (LivingEntity)e;
                  if (OriginStrike.foe(player, live)) {
                     Vec3 push = to.normalize().scale(Math.min(2.2D, 1.25D + 0.06D * (double)r));
                     OriginStrike.shove(live, push.x, push.y + 0.38D, push.z);
                     OriginStrike.hitFresh(live, level.damageSources().playerAttack(player), slamDamage(r));
                     if (r >= 2) {
                        live.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, true, true));
                     }

                     if (r >= MILE_AT[2][1] && !live.onGround() && !TimeWeft.everpresent(live)) {
                        live.setDeltaMovement(live.getDeltaMovement().add(0.0D, -1.4D, 0.0D));
                        live.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, true, true));
                     }
                  }
               }
            }
         }

         Fx.cone(level, Fx.gale(10133670), eye.add(look.scale(0.6D)), look, Math.toRadians(r >= 2 ? 40.0D : 30.0D), 50, 0.85D);
         Fx.cone(level, Fx.ember(6253168), eye.add(look.scale(0.6D)), look, Math.toRadians(24.0D), 24, 0.6D);

         for(int i = 0; i < 8; ++i) {
            double ang = (Math.PI * 2D) * (double)i / 8.0D + level.random.nextDouble() * 0.25D;
            Vec3 crackFrom = player.position().add(0.0D, 0.1D, 0.0D);
            Vec3 crackTo = crackFrom.add(Math.cos(ang) * (2.0D + (double)r * 0.4D), 0.05D, Math.sin(ang) * (2.0D + (double)r * 0.4D));
            Fx.line(level, Fx.ember(6253168), crackFrom, crackTo, 0.4D, 0.05D, 0.0D);
         }

         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.player.attack.sweep"), SoundSource.PLAYERS, 1.0F, 0.6F);
         Spectacle.shockwave(level, 6253168, player.position(), 2.5D);
         Spectacle.after(10, () -> slamEcho(player, level, r));
         bar(player, "§bGALE SLAM");
      }
   }

   private static void slamEcho(ServerPlayer player, ServerLevel level, int r) {
      if (!player.isRemoved() && player.level() == level) {
         double reach = slamEchoReach(r);
         Vec3 at = player.position();

         for(LivingEntity live : OriginStrike.foesNear(level, player, at.add(0.0D, 1.0D, 0.0D), reach)) {
            OriginStrike.hitFresh(live, level.damageSources().playerAttack(player), slamDamage(r) * 0.6F);
            Vec3 out = new Vec3(live.getX() - at.x, 0.0D, live.getZ() - at.z);
            Vec3 dir = out.lengthSqr() < 1.0E-4D ? Vec3.ZERO : out.normalize();
            OriginStrike.shove(live, dir.x * 0.9D, 0.3D, dir.z * 0.9D);
         }

         Spectacle.shockwave(level, 10133670, at, reach);
         Fx.circle(level, Fx.gale(10133670), at.add(0.0D, 0.6D, 0.0D), 1.0D, 36, 0.05D, 0.45D + 0.04D * reach);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.sonic_boom"), SoundSource.PLAYERS, 0.4F, 1.6F);
      }
   }

   private static void doDarts(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 3);
      if (r < 1) {
         bar(player, "§8Bone Darts aren't yours yet.");
      } else if (pay(player, 14)) {
         CastFxEvents.pose(player, "hm_darts", 12);
         Vec3 eye = player.getEyePosition();
         Vec3 look = player.getLookAngle();
         int shots = dartCount(r);
         float dmg = dartDamage(r);
         double dartReach = Math.min(48.0D, 26.0D + (double)r);
         Map<UUID, Integer> struck = new HashMap();
         Map<UUID, Integer> shards = new HashMap();

         for(int i = 0; i < shots; ++i) {
            double spread = r >= 3 ? (double)(i - shots / 2) * 6.0D : (double)(i - shots / 2) * 3.0D;
            Vec3 dir = rotateYaw(look, spread).normalize();
            HitResult wall = level.clip(new ClipContext(eye, eye.add(dir.scale(dartReach)), Block.COLLIDER, Fluid.NONE, player));
            double limit = wall.getType() == Type.MISS ? dartReach : wall.getLocation().distanceTo(eye);
            LivingEntity hit = null;
            double bestD = limit;

            for(LivingEntity live : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(dartReach))) {
               if (live != player && live.isAlive() && OriginStrike.fair(player, live)) {
                  Vec3 to = live.position().add(0.0D, (double)live.getBbHeight() * 0.5D, 0.0D).subtract(eye);
                  double along = to.dot(dir);
                  if (!(along <= 0.5D) && !(along > bestD) && !(to.subtract(dir.scale(along)).length() > 0.9D + (double)live.getBbWidth() * 0.5D)) {
                     bestD = along;
                     hit = live;
                  }
               }
            }

            Vec3 end = hit != null ? hit.position().add(0.0D, (double)hit.getBbHeight() * 0.5D, 0.0D) : eye.add(dir.scale(limit));
            Vec3 muzzle = eye.add(dir.scale(0.6D));
            Vec3 apex = muzzle.add(end.subtract(muzzle).scale(0.45D)).add(0.0D, 0.55D + 0.18D * (double)(i % 3), 0.0D);
            Fx.line(level, Fx.ember(15262938), muzzle, apex, 0.32D, 0.045D, 0.0D);
            Fx.line(level, Fx.ember(15262938), apex, end, 0.32D, 0.045D, 0.0D);
            Fx.line(level, Fx.glint(6253168), muzzle, end, 0.9D, 0.1D, 0.0D);
            Fx.line(level, Fx.ember(8381416), muzzle, end, 1.6D, 0.16D, 0.0D);
            if (hit != null) {
               DamageSource src = r >= 2 ? grey(player, level) : level.damageSources().playerAttack(player);
               int k = struck.merge(hit.getUUID(), 1, Integer::sum);
               if (k <= 5) {
                  OriginStrike.hitFresh(hit, src, k == 1 ? dmg : dmg * 0.6F);
               }

               for(LivingEntity near : OriginStrike.foesNear(level, player, end, 1.5D)) {
                  if (near != hit && shards.merge(near.getUUID(), 1, Integer::sum) <= 3) {
                     OriginStrike.hitFresh(near, src, dmg * 0.4F);
                  }
               }

               Fx.ring(level, 8381416, end, 1.5D, 8);
               if (r >= MILE_AT[3][0]) {
                  hit.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, true, true));
               }

               if (r >= MILE_AT[3][1] && !hit.isAlive()) {
                  refund(player, 6);
                  bar(player, "§bThe marrow toll pays you back in aura.");
               }

               Fx.burst(level, Fx.ember(15262938), end, 16 + r, 0.3D, 0.08D);
               Fx.burst(level, Fx.glint(8381416), end, 6, 0.2D, 0.05D);
               Fx.ring(level, 15262938, end, 0.6D, 8);
               level.playSound((Player)null, hit.blockPosition(), CastFxEvents.sound("minecraft:block.bone_block.break"), SoundSource.PLAYERS, 0.6F, 1.5F);
            }
         }

         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.arrow.shoot"), SoundSource.PLAYERS, 0.9F, 1.7F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.skeleton.ambient"), SoundSource.PLAYERS, 0.5F, 0.6F);
         bar(player, "§bBONE DARTS§8: " + shots + " splinters.");
      }
   }

   private static Vec3 rotateYaw(Vec3 v, double degrees) {
      double rad = Math.toRadians(degrees);
      double cos = Math.cos(rad);
      double sin = Math.sin(rad);
      return new Vec3(v.x * cos - v.z * sin, v.y, v.x * sin + v.z * cos);
   }

   private static void doGrasp(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 4);
      if (r < 1) {
         bar(player, "§8Grasp isn't yours yet.");
      } else {
         Vec3 eye = player.getEyePosition();
         Vec3 look = player.getLookAngle();
         LivingEntity target = null;
         double best = 0.97D;

         for(LivingEntity live : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(18.0D))) {
            if (live != player && live.isAlive() && OriginStrike.fair(player, live) && (!(live instanceof Player) || r >= 2)) {
               Vec3 to = live.position().add(0.0D, (double)live.getBbHeight() * 0.5D, 0.0D).subtract(eye);
               if (!(to.length() > 18.0D)) {
                  double dot = to.normalize().dot(look);
                  if (dot > best) {
                     best = dot;
                     target = live;
                  }
               }
            }
         }

         if (target == null) {
            bar(player, "§8Nothing to take hold of.");
         } else if (pay(player, 20)) {
            CastFxEvents.pose(player, "hm_grasp", 30);
            grasped.put(player.getUUID(), new int[]{LifeClock.now(player) + 40 + r * 10, r});
            graspTarget.put(player.getUUID(), target.getUUID());
            bar(player, "§bGRASP");
         }
      }
   }

   private static void throwImpact(ServerPlayer player, ServerLevel level, LivingEntity thrown, float quake) {
      if (!player.isRemoved() && thrown.level() == level) {
         Vec3 at = thrown.position();

         for(LivingEntity live : OriginStrike.foesNear(level, player, at.add(0.0D, 0.8D, 0.0D), 3.0D)) {
            if (live != thrown) {
               OriginStrike.hitFresh(live, grey(player, level), quake);
               Vec3 out = new Vec3(live.getX() - at.x, 0.0D, live.getZ() - at.z);
               Vec3 dir = out.lengthSqr() < 1.0E-4D ? Vec3.ZERO : out.normalize();
               OriginStrike.shove(live, dir.x * 0.7D, 0.4D, dir.z * 0.7D);
            }
         }

         Spectacle.shockwave(level, 15262938, at, 3.0D);
         Spectacle.spikes(level, Fx.ember(6253168), at, 10, 3.0D, 1.4D);
      }
   }

   private static void doBriar(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 5);
      if (r < 1) {
         bar(player, "§8Briar isn't yours yet.");
      } else if (pay(player, 22)) {
         CastFxEvents.pose(player, "hm_slam", 16);
         Vec3 c = player.position().add(player.getLookAngle().scale(3.5D));
         briar.put(player.getUUID(), new Object[]{c, LifeClock.now(player) + 200 + r * 60, 2.5D + (double)r * 0.8D, r});
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.bone_block.place"), SoundSource.PLAYERS, 1.0F, 0.5F);
         bar(player, "§bBRIAR§8: the grey grows teeth.");
      }
   }

   private static void doVeil(ServerPlayer player, ServerLevel level) {
      int r = rank(player, 6);
      if (r < 1) {
         bar(player, "§8Gray Veil isn't yours yet.");
      } else if (pay(player, 18)) {
         CastFxEvents.pose(player, "hm_gaze", 16);
         Vec3 c = player.position().add(player.getLookAngle().scale(4.0D)).add(0.0D, 1.0D, 0.0D);
         veil.put(player.getUUID(), new Object[]{c, LifeClock.now(player) + 160 + r * 60, 4.0D + (double)r, r});
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.elder_guardian.curse"), SoundSource.PLAYERS, 0.6F, 1.4F);
         bar(player, "§bGRAY VEIL");
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
               UUID var17 = player.getUUID();
               Set tags = player.getTags();
               if (!fallGrace.isEmpty()) {
                  Integer until = (Integer)fallGrace.get(var17);
                  if (until != null) {
                     if (graced(player, until)) {
                        player.fallDistance = 0.0F;
                     } else {
                        fallGrace.remove(var17);
                     }
                  }
               }

               if (tags.contains("unrp.hm_blink_trigger")) {
                  player.removeTag("unrp.hm_blink_trigger");
                  if (holds(player) && armed(player)) {
                     if (player.isShiftKeyDown()) {
                        doGrasp(player, level);
                     } else {
                        doBlink(player, level);
                     }
                  }
               }

               if (tags.contains("unrp.hm_gaze_trigger")) {
                  player.removeTag("unrp.hm_gaze_trigger");
                  if (holds(player) && armed(player)) {
                     if (player.isShiftKeyDown()) {
                        doVeil(player, level);
                     } else {
                        doGaze(player, level);
                     }
                  }
               }

               if (tags.contains("unrp.hm_slam_trigger")) {
                  player.removeTag("unrp.hm_slam_trigger");
                  if (holds(player) && armed(player)) {
                     if (player.isShiftKeyDown()) {
                        doBriar(player, level);
                     } else {
                        doSlam(player, level);
                     }
                  }
               }

               if (tags.contains("unrp.hm_darts_trigger")) {
                  player.removeTag("unrp.hm_darts_trigger");
                  if (holds(player) && armed(player)) {
                     if (player.isShiftKeyDown()) {
                        openCodex(player);
                     } else {
                        doDarts(player, level);
                     }
                  }
               }

               if (holds(player)) {
                  int[] g = (int[])grasped.get(var17);
                  if (g != null) {
                     label265: {
                        UUID tid = (UUID)graspTarget.get(var17);
                        Entity raw_0 = tid == null ? null : level.getEntity(tid);
                        if (LifeClock.now(player) <= g[0] && raw_0 instanceof LivingEntity) {
                           LivingEntity held = (LivingEntity)raw_0;
                           if (held.isAlive() && !TimeWeft.everpresent(held)) {
                              Vec3 hold = player.getEyePosition().add(player.getLookAngle().scale(4.5D));
                              Vec3 pull = hold.subtract(held.position()).scale(Math.min(0.5D, 0.16D + 0.06D * (double)g[1]));
                              held.setDeltaMovement(pull);
                              held.hurtMarked = true;
                              if (player.tickCount % 10 == 0) {
                                 Fx.ring(level, 8381416, held.getX(), held.getY() - 0.2D, held.getZ(), 1.0D, 10);
                              }

                              held.fallDistance = 0.0F;
                              if (g[1] >= MILE_AT[4][0] && player.tickCount % 10 == 0) {
                                 OriginStrike.hitFresh(held, grey(player, level), wring(g[1]));
                                 Fx.burst(level, Fx.ember(8381416), held.position().add(0.0D, (double)held.getBbHeight() * 0.6D, 0.0D), 5, 0.2D, 0.03D);
                              }

                              if (player.tickCount % 3 == 0) {
                                 Fx.burst(level, Fx.ember(10133670), held.position().add(0.0D, 0.4D, 0.0D), 3, 0.25D, 0.02D);
                                 Fx.line(level, Fx.ember(6253168), player.getEyePosition(), held.position().add(0.0D, (double)held.getBbHeight() * 0.5D, 0.0D), 0.7D, 0.08D, 0.0D);
                              }
                              break label265;
                           }
                        }

                        if (g[1] >= MILE_AT[4][1] && LifeClock.now(player) > g[0] && raw_0 instanceof LivingEntity) {
                           LivingEntity flung = (LivingEntity)raw_0;
                           if (flung.isAlive()) {
                              if (!TimeWeft.everpresent(flung)) {
                                 flung.setDeltaMovement(0.0D, -1.6D, 0.0D);
                                 flung.hurtMarked = true;
                              }

                              OriginStrike.hitFresh(flung, grey(player, level), throwDamage(g[1]));
                              float quake = throwDamage(g[1]) * 0.5F;
                              Spectacle.after(6, () -> throwImpact(player, level, flung, quake));
                              Fx.burst(level, Fx.ember(15262938), flung.position().add(0.0D, 0.5D, 0.0D), 14, 0.4D, 0.06D);
                              level.playSound((Player)null, flung.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.big_fall"), SoundSource.PLAYERS, 0.8F, 0.8F);
                           }
                        }

                        grasped.remove(var17);
                        graspTarget.remove(var17);
                     }
                  }

                  Object[] b = briar.get(var17);
                  if (b != null) {
                     if (LifeClock.now(player) > ((Number)b[1]).intValue()) {
                        briar.remove(var17);
                     } else {
                        Vec3 c_0 = (Vec3)b[0];
                        double rad_0 = ((Number)b[2]).doubleValue();
                        int br = b.length > 3 ? ((Number)b[3]).intValue() : 1;
                        if (player.tickCount % 4 == 0) {
                           for(int i = 0; i < 16; ++i) {
                              double a = (Math.PI * 2D) * (double)i / 16.0D;
                              Vec3 p = c_0.add(Math.cos(a) * rad_0, 0.1D, Math.sin(a) * rad_0);
                              Fx.line(level, Fx.ember(6253168), p, p.add(0.0D, 0.9D, 0.0D), 0.4D, 0.06D, 0.0D);
                           }
                        }

                        if (player.tickCount % 10 == 0) {
                           for(LivingEntity live_0 : level.getEntitiesOfClass(LivingEntity.class, new AABB(c_0.subtract(rad_0, 2.0D, rad_0), c_0.add(rad_0, 3.0D, rad_0)))) {
                              if (OriginStrike.foe(player, live_0) && !((new Vec3(live_0.getX() - c_0.x, 0.0D, live_0.getZ() - c_0.z)).length() > rad_0)) {
                                 OriginStrike.hitFresh(live_0, grey(player, level), briarBite(br));
                                 Spectacle.pillar(level, Fx.ember(15262938), live_0.position(), 1.3D);
                                 live_0.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, br >= MILE_AT[5][0] ? 3 : 2, false, true, true));
                                 if (br >= MILE_AT[5][0]) {
                                    live_0.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true, true));
                                 }
                              }
                           }
                        }

                        if (br >= MILE_AT[5][1] && player.tickCount % 20 == 0 && player.position().distanceTo(c_0) <= rad_0) {
                           player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, false, false));
                           refund(player, 1);
                        }
                     }
                  }

                  Object[] v = veil.get(var17);
                  if (v != null) {
                     if (LifeClock.now(player) > ((Number)v[1]).intValue()) {
                        veil.remove(var17);
                     } else {
                        Vec3 c = (Vec3)v[0];
                        double rad = ((Number)v[2]).doubleValue();
                        int vr = v.length > 3 ? ((Number)v[3]).intValue() : 1;
                        if (player.tickCount % 3 == 0) {
                           Fx.shell(level, Fx.ember(10133670), c, rad, 30, -0.04D);
                           Fx.circle(level, Fx.ember(6253168), c, rad * 0.6D, 12, 0.05D, 0.02D);

                           for(int i = 0; i < 5; ++i) {
                              double ang = (double)player.tickCount * 0.02D + (Math.PI * 2D) * (double)i / 5.0D;
                              Vec3 wisp = c.add(Math.cos(ang) * rad, -rad * 0.4D, Math.sin(ang) * rad);
                              Fx.line(level, Fx.ember(6253168), wisp, wisp.add(0.0D, rad * 0.8D, 0.0D), 0.5D, 0.04D, 0.0D);
                           }
                        }

                        if (vr >= MILE_AT[6][0] && player.tickCount % 5 == 0 && player.position().add(0.0D, 1.0D, 0.0D).distanceTo(c) <= rad) {
                           player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, false, false, false));
                        }

                        if (player.tickCount % 20 == 0) {
                           for(LivingEntity live : OriginStrike.foesNear(level, player, c, rad)) {
                              OriginStrike.hitFresh(live, grey(player, level), veilChoke(vr));
                              live.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, true, true));
                              live.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, true, true));
                              if (vr >= MILE_AT[6][1]) {
                                 live.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, true, true));
                              }
                           }
                        }
                     }
                  }

                  if (player.tickCount % 20 == 0) {
                     syncStage(player);
                     applyCharms(player);
                     Map<UUID, Integer> marks = (Map)marked.get(var17);
                     if (marks != null) {
                        Iterator<Map.Entry<UUID, Integer>> mit = marks.entrySet().iterator();

                        while(mit.hasNext()) {
                           Map.Entry<UUID, Integer> m = (Map.Entry)mit.next();
                           Entity raw = level.getEntity((UUID)m.getKey());
                           if (m.getValue() > LifeClock.now(player) && raw instanceof LivingEntity) {
                              LivingEntity mark = (LivingEntity)raw;
                              if (mark.isAlive()) {
                                 Fx.ring(level, 8381416, mark.getX(), mark.getY() + 0.06D, mark.getZ(), 0.6D + (double)mark.getBbWidth(), 16);
                                 Fx.burst(level, Fx.glint(8381416), mark.position().add(0.0D, (double)mark.getBbHeight() + 0.35D, 0.0D), 3, 0.1D, 0.01D);
                                 continue;
                              }
                           }

                           mit.remove();
                        }
                     }
                  }

                  if (player.tickCount % 30 == 0 && armed(player) && stage(player) >= 3 && !player.isInvisible()) {
                     dreadPulse(player, level);
                  }

                  if (player.tickCount % 12 == 0 && armed(player) && stage(player) >= 4 && !player.isInvisible()) {
                     level.sendParticles(Fx.ember(10133670), player.getX(), player.getY() + 0.9D, player.getZ(), 2, 0.55D, 0.5D, 0.55D, 0.0D);
                  }

               }
            }
         }
      }
   }

   private static void dreadPulse(ServerPlayer player, ServerLevel level) {
      int s = stage(player);
      boolean spoke = false;

      for(LivingEntity live : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0D), (en) -> en.isAlive() && en != player)) {
         boolean rivalPlayer = live instanceof ServerPlayer && OriginStrike.foe(player, live);
         if (rivalPlayer || live instanceof Monster) {
            live.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0, false, false, false));
            live.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, false));
            if (s >= 4 && level.random.nextFloat() < 0.35F) {
               Vec3 away = live.position().subtract(player.position());
               Vec3 push = (new Vec3(away.x, 0.0D, away.z)).normalize();
               OriginStrike.shove(live, push.x * 0.35D, 0.08D, push.z * 0.35D);
               if (rivalPlayer) {
                  live.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 35, 0, false, false, false));
               }

               Fx.burst(level, Fx.ember(6253168), live.position().add(0.0D, 1.0D, 0.0D), 6, 0.3D, 0.02D);
               if (!spoke) {
                  spoke = true;
                  level.playSound((Player)null, live.blockPosition(), CastFxEvents.sound("minecraft:ambient.cave"), SoundSource.PLAYERS, 0.35F, 1.6F);
               }
            }
         }
      }

      Fx.circle(level, Fx.ember(10133670), player.position().add(0.0D, 0.2D, 0.0D), 8.0D, 20, 0.0D, 0.01D);
   }

   @SubscribeEvent
   public static void onShadowEdge(LivingHurtEvent event) {
      Entity victim_0 = event.getSource().getEntity();
      if (victim_0 instanceof ServerPlayer attacker) {
         if (armed(attacker)) {
            if (event.getSource().getDirectEntity() == attacker && !OriginStrike.active()) {
               LivingEntity victim = event.getEntity();
               if (victim == attacker) {
                  return;
               }

               Vec3 toAttacker = attacker.position().subtract(victim.position());
               if (toAttacker.lengthSqr() < 1.0E-4D) {
                  return;
               }

               double facing = victim.getLookAngle().dot((new Vec3(toAttacker.x, 0.0D, toAttacker.z)).normalize());
               if (facing > -0.35D) {
                  return;
               }

               float mult = edgeMult(stage(attacker));
               event.setAmount(event.getAmount() * mult);
               Level at_0 = attacker.level();
               if (at_0 instanceof ServerLevel) {
                  ServerLevel level = (ServerLevel)at_0;
                  Vec3 at = victim.position().add(0.0D, (double)victim.getBbHeight() * 0.7D, 0.0D);
                  Spectacle.slash(level, Fx.ember(6253168), at, 0.7D, (double)attacker.getYRot());
                  Fx.burst(level, Fx.ember(6253168), at, 8, 0.25D, 0.05D);
                  Fx.burst(level, Fx.glint(8381416), at, 4, 0.15D, 0.04D);
                  level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.sculk_shrieker.hit"), SoundSource.PLAYERS, 0.5F, 1.9F);
               }

               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onGreyAnswers(LivingHurtEvent event) {
      LivingEntity attacker = event.getEntity();
      if (attacker instanceof ServerPlayer player) {
         if (armed(player)) {
            if (stage(player) < 4) {
               return;
            }

            Entity level_0 = event.getSource().getEntity();
            if (level_0 instanceof LivingEntity) {
               attacker = (LivingEntity)level_0;
               if (attacker != player) {
                  Level away = player.level();
                  if (away instanceof ServerLevel) {
                     ServerLevel level = (ServerLevel)away;
                     Vec3 var8 = attacker.position().subtract(player.position());
                     Vec3 push = (new Vec3(var8.x, 0.0D, var8.z)).normalize();
                     OriginStrike.shove(attacker, push.x * 0.6D, 0.15D, push.z * 0.6D);
                     attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0, false, false, false));
                     Fx.burst(level, Fx.ember(8381416), attacker.position().add(0.0D, 1.2D, 0.0D), 10, 0.35D, 0.04D);
                     level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.sculk_shrieker.hit"), SoundSource.PLAYERS, 0.6F, 0.7F);
                     return;
                  }

                  return;
               }
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onWeakpoint(LivingHurtEvent event) {
      Entity r_0 = event.getSource().getEntity();
      if (r_0 instanceof ServerPlayer attacker) {
         if (armed(attacker)) {
            if (attacker == event.getEntity()) {
               return;
            }

            int r = rank(attacker, 1);
            if (r >= MILE_AT[1][0] && event.getEntity().hasEffect(MobEffects.GLOWING)) {
               float mult = 1.15F + Math.min(0.3F, 0.03F * (float)(r - MILE_AT[1][0]));
               event.setAmount(event.getAmount() * mult);
               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onCharmWard(LivingHurtEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         if (armed(player)) {
            if (!player.getTags().contains("unrp.hm_charm.ward")) {
               return;
            }

            event.setAmount(event.getAmount() * 0.92F);
            return;
         }
      }

   }

   private static boolean graced(ServerPlayer player, int until) {
      int now = LifeClock.now(player);
      return now <= until && now >= until - 40;
   }

   @SubscribeEvent
   public static void onBlinkLanding(LivingFallEvent event) {
      if (!fallGrace.isEmpty()) {
         LivingEntity until_0 = event.getEntity();
         if (until_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)until_0;
            Integer until = (Integer)fallGrace.get(player.getUUID());
            if (until != null && graced(player, until)) {
               event.setDistance(0.0F);
               event.setDamageMultiplier(0.0F);
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      UUID id = event.getEntity().getUUID();
      grasped.remove(id);
      graspTarget.remove(id);
      briar.remove(id);
      veil.remove(id);
      lastBlink.remove(id);
      fallGrace.remove(id);
      marked.remove(id);
   }
}
