package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;
import com.myriam.unrp.anim.MoveAnim;
import com.myriam.unrp.deal.Spectacle;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;
import org.joml.Vector3f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class LimitBreakerEvents {
   private static final String HAS = "unrp.has.limit_breaker";
   private static final String ACTIVE = "unrp.active.limit_breaker";
   private static final String GRIT = "unrp.lb_grit";
   private static final String KI = "unrp.lb_ki";
   private static final String FINE = "unrp.lb_fine";
   private static final String AURA_OFF = "unrp.lb_aura_off";
   private static final UUID APE_DMG_ID = UUID.fromString("3f7a9c15-82d4-4b6e-a1c9-57e30b28d946");
   private static final UUID APE_KB_ID = UUID.fromString("8c24e6b0-5f91-4d37-bd68-0a4c72e9153f");
   private static final String SURGE_TAG = "unrp.lb_surge";
   private static final String RADIANCE_TAG = "unrp.lb_radiance";
   private static final String AZURE_TAG = "unrp.lb_azure";
   private static final String NATURE_TAG = "unrp.lb_nature";
   private static final String[] FORM_TAGS = new String[]{"unrp.lb_surge", "unrp.lb_radiance", "unrp.lb_azure", "unrp.lb_nature"};
   private static final String[] FORM_NAMES = new String[]{"First Break: Surge", "Second Break: Radiance", "Third Break: Azure", "Final Break: Second Nature"};
   private static final String TIER2_TAG = "unrp.lb_tier2";
   private static final String TIER3_TAG = "unrp.lb_tier3";
   private static final String TIER4_TAG = "unrp.lb_tier4";
   private static final String FEAT_GOLD = "unrp.lb_feat_gold";
   private static final String FEAT_BLUE = "unrp.lb_feat_blue";
   private static final String FEAT_SILVER = "unrp.lb_feat_silver";
   private static final String[] TIER_NAMES = new String[]{"Low", "Medium", "High", "Max"};
   public static final double EXECUTE_FLOOR = 0.25D;
   public static final double BASE_HP = -4.0D;
   public static final double BASE_DMG = -0.5D;
   public static final int SURGE_LEVEL = 4;
   public static final int TIER2_LEVEL = 10;
   public static final int TIER3_LEVEL = 25;
   public static final int TIER4_LEVEL = 45;
   public static final int BOLT_COST = 4;
   public static final int BARRAGE_COST = 12;
   public static final int WAVE_COST = 20;
   public static final int NOVA_COST = 16;
   public static final int RUSH_COST = 6;
   public static final int CRASH_COST = 10;
   public static final int[] IGNITE_COST = new int[]{10, 14, 18, 22};
   public static final int[] UPKEEP = new int[]{1, 2, 3, 5};
   public static final int KI_BASE_CAP = 100;
   public static final int KI_PER_ERA = 10;
   public static final int WAVE_CHANNEL = 50;
   public static final int BREAK_CHANNEL = 60;
   public static final int[] GROW_AT = new int[]{12, 24, 36};
   public static final float[] GROW_SHARE = new float[]{0.15F, 0.55F, 1.0F};
   public static final int RUSH_TICKS = 14;
   public static final int OUTRO_LEAD = 4;
   public static final double RUSH_RANGE = 28.0D;
   public static final int AIR_GRACE = 20;
   public static final int BOLT_CD = 10;
   public static final int BARRAGE_CD = 30;
   public static final int WAVE_CD = 60;
   public static final int RUSH_CD = 40;
   private static final double[] EDGE_K = new double[]{0.3D, 0.8D, 1.2D, 1.7D, 1.5D};
   private static final double[] FORM_SPEED = new double[]{0.0D, 0.02D, 0.03D, 0.05D, 0.04D};
   private static final double[] FORM_ATKSPD = new double[]{0.0D, 0.4D, 0.6D, 1.0D, 0.8D};
   private static final double[] FORM_ARMOR = new double[]{0.0D, 0.0D, 2.0D, 4.0D, 3.0D};
   private static final int RED = 16730684;
   private static final int GOLD = 16765519;
   private static final int BLUE = 5873919;
   private static final int SILVER = 15396853;
   private static final int[] TIER_RGB = new int[]{16730684, 16765519, 5873919, 15396853};
   private static final Vector3f RED_D = new Vector3f(1.0F, 0.29F, 0.24F);
   private static final Vector3f GOLD_D = new Vector3f(1.0F, 0.82F, 0.31F);
   private static final Vector3f BLUE_D = new Vector3f(0.35F, 0.63F, 1.0F);
   private static final Vector3f SILVER_D = new Vector3f(0.92F, 0.94F, 0.96F);
   private static final Vector3f[] TIER_DUST = new Vector3f[]{RED_D, GOLD_D, BLUE_D, SILVER_D};
   private static final UUID SPEED_ID = UUID.fromString("6a91c3e7-25d8-4b06-9f42-c17ab35d80e9");
   private static final UUID ATKSPD_ID = UUID.fromString("38d5f0a2-b764-4e19-8c53-90e12fa6b4d7");
   private static final UUID FORM_ARMOR_ID = UUID.fromString("d70b46f1-583e-4c28-a9b6-14f27c09e853");
   private static final Map<UUID, int[]> becoming = new HashMap();
   private static final Map<UUID, Integer> waveCharge = new HashMap();
   private static final Map<UUID, Integer> waveOrb = new HashMap();
   private static final Map<UUID, int[]> rushing = new HashMap();
   private static final Map<UUID, HashSet<UUID>> rushHit = new HashMap();
   private static final Map<UUID, Integer> rushAim = new HashMap();
   private static final Map<UUID, Integer> boltCd = new HashMap();
   private static final Map<UUID, Integer> waveCd = new HashMap();
   private static final Map<UUID, Integer> rushCd = new HashMap();
   private static final Map<UUID, float[]> hurtLow = new HashMap();
   private static final Map<UUID, Integer> afterglow = new HashMap();
   private static final Map<UUID, Vec3> breakAnchor = new HashMap();
   private static final Map<UUID, Integer> lastForm = new HashMap();
   private static final Map<UUID, Integer> aloft = new HashMap();
   private static final Set<UUID> counterReady = new HashSet();
   public static final int COUNTER_SHOWN = 60;
   public static final double NOVA_REACH = 5.0D;
   public static final double COPY_TOWARD = 0.4D;
   public static final double COPY_ASIDE = 0.5D;

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

   private static boolean isActive(ServerPlayer player) {
      return player.getTags().contains("unrp.active.limit_breaker");
   }

   private static double edge(ServerPlayer player) {
      float max = player.getMaxHealth();
      if (max <= 0.0F) {
         return 0.0D;
      } else {
         double e = 1.0D - (double)(player.getHealth() / max);
         return Math.max(0.0D, Math.min(1.0D, e));
      }
   }

   public static int form(ServerPlayer player) {
      Set<String> tags = player.getTags();

      for(int i = FORM_TAGS.length - 1; i >= 0; --i) {
         if (tags.contains(FORM_TAGS[i])) {
            return i + 1;
         }
      }

      return 0;
   }

   public static int fineForm(ServerPlayer player) {
      int era = form(player);
      if (era <= 0) {
         return 0;
      } else {
         int fine = score(player, "unrp.lb_fine");
         return LimitBreakerForms.valid(fine) && LimitBreakerForms.era(fine) == era ? fine : LimitBreakerForms.firstOfEra(era);
      }
   }

   public static boolean rose(ServerPlayer player) {
      return AlignmentEvents.of(player) <= -20;
   }

   public static boolean[] unlockedForms(ServerPlayer player) {
      boolean[] out = new boolean[15];
      int top = unlockedTop(player);
      int lv = com.myriam.unrp.ability.OriginGrowth.level(player);
      Set<String> tags = player.getTags();

      for(int f = 1; f < 15; ++f) {
         if (LimitBreakerForms.ERA[f] <= top && lv >= LimitBreakerForms.LEVEL_REQ[f]) {
            if (f == 14) {
               boolean feats = tags.contains("unrp.lb_feat_gold") && tags.contains("unrp.lb_feat_blue") && tags.contains("unrp.lb_feat_silver");
               if (!feats || ascend(player) < 25) {
                  continue;
               }
            }

            out[f] = true;
         }
      }

      return out;
   }

   private static int composite(ServerPlayer player, int fine) {
      return fine <= 0 ? 0 : fine | (rose(player) ? 64 : 0) | (player.getTags().contains("unrp.lb_aura_off") ? 128 : 0);
   }

   private static void applyScale(ServerPlayer player, float want) {
      MinecraftServer server = player.getServer();
      if (server != null) {
         server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "scale set pehkui:base " + want + " " + player.getName().getString());
      }
   }

   private static boolean roomFor(ServerPlayer player, int want) {
      float grow = LimitBreakerForms.SCALE[want];
      if (!(grow <= LimitBreakerForms.SCALE[fineForm(player)]) && ModList.get().isLoaded("pehkui")) {
         double half = 0.3D * (double)grow;
         Vec3 at = player.position();
         AABB box = (new AABB(at.x - half, at.y, at.z - half, at.x + half, at.y + 1.8D * (double)grow, at.z + half)).deflate(1.0E-7D);
         return player.level().noCollision(player, box);
      } else {
         return true;
      }
   }

   private static void cramped(ServerPlayer player, int want, boolean refunded) {
      int high = (int)Math.ceil((double)(1.8F * LimitBreakerForms.SCALE[want]));
      bar(player, "§8No room here for §6" + LimitBreakerForms.displayName(want, rose(player)) + "§8 to rise. §7(needs open space about " + high + " blocks high" + (refunded ? "; grit returned)" : ")"));
   }

   private static int unlockedTop(ServerPlayer player) {
      Set<String> tags = player.getTags();
      if (tags.contains("unrp.lb_tier4")) {
         return 4;
      } else if (tags.contains("unrp.lb_tier3")) {
         return 3;
      } else if (tags.contains("unrp.lb_tier2")) {
         return 2;
      } else {
         return com.myriam.unrp.ability.OriginGrowth.level(player) >= 4 ? 1 : 0;
      }
   }

   public static double dmgMult(ServerPlayer player) {
      int fine = fineForm(player);
      double ladder = LimitBreakerForms.powerOverEra(fine) * (fine == 14 ? 1.3D : 1.0D);
      return (1.0D + edge(player) * EDGE_K[form(player)]) * (1.0D + (double)ascend(player) * 0.03D) * ladder;
   }

   public static int ascend(ServerPlayer player) {
      int lv = com.myriam.unrp.ability.OriginGrowth.level(player);
      return lv > 45 && unlockedTop(player) >= 4 ? (lv - 45) / 5 : 0;
   }

   private static int gritCap(ServerPlayer player) {
      return 120 + 3 * com.myriam.unrp.ability.OriginGrowth.level(player);
   }

   private static boolean pay(ServerPlayer player, int cost) {
      int have = score(player, "unrp.lb_grit");
      if (have < cost) {
         bar(player, "§8Not enough grit. §7(" + have + "/" + cost + ")");
         return false;
      } else {
         setScore(player, "unrp.lb_grit", have - cost);
         gritHud(player);
         return true;
      }
   }

   public static int kiCap(ServerPlayer player) {
      return 100 + 10 * Math.max(0, unlockedTop(player));
   }

   private static boolean payKi(ServerPlayer player, int cost) {
      int have = score(player, "unrp.lb_ki");
      if (have < cost) {
         bar(player, "§8Not enough Ki. §7(" + have + "/" + cost + ")");
         return false;
      } else {
         setScore(player, "unrp.lb_ki", have - cost);
         gritHud(player);
         return true;
      }
   }

   static void gritHud(ServerPlayer player) {
      int f = form(player);
      int lv = com.myriam.unrp.ability.OriginGrowth.level(player);
      int edgePct = (int)Math.round(edge(player) * EDGE_K[f] * 100.0D);
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new GritHudPacket(score(player, "unrp.lb_grit"), gritCap(player), lv, com.myriam.unrp.ability.OriginGrowth.xp(player), com.myriam.unrp.ability.OriginGrowth.xpToNext(lv), edgePct, f, Math.max(1, unlockedTop(player)), score(player, "unrp.lb_ki"), kiCap(player)));
   }

   private static void bank(ServerPlayer player, float amount) {
      if (!(amount <= 0.0F)) {
         double frac = 1.0D - edge(player);
         if (!(frac >= 0.6D)) {
            int gain = Math.max(1, Math.round(amount * (1.0F + (float)edge(player))));
            int have = score(player, "unrp.lb_grit");
            int cap = gritCap(player);
            if (have < cap) {
               setScore(player, "unrp.lb_grit", Math.min(cap, have + gain));
            }
         }
      }
   }

   private static float floored(LivingEntity victim, float want) {
      if (!(victim instanceof Player)) {
         return want;
      } else {
         float head = victim.getHealth() - victim.getMaxHealth() * 0.25F;
         return head <= 0.0F ? want : Math.min(want, Math.max(0.0F, victim.getHealth() - 1.0F));
      }
   }

   private static boolean meaningful(LivingEntity victim) {
      return !HollowDeepFavors.doppelKnown(victim) && (victim instanceof Player || victim.getMaxHealth() >= 10.0F);
   }

   private static Vector3f fineVec(ServerPlayer player, int fine) {
      int rgb = LimitBreakerForms.color(fine, rose(player));
      return new Vector3f((float)(rgb >> 16 & 255) / 255.0F, (float)(rgb >> 8 & 255) / 255.0F, (float)(rgb & 255) / 255.0F);
   }

   private static Vector3f dustColor(ServerPlayer player) {
      int fine = fineForm(player);
      if (fine > 0) {
         return fineVec(player, fine);
      } else {
         int f = form(player);
         if (f > 0) {
            return TIER_DUST[f - 1];
         } else {
            int top = unlockedTop(player);
            return TIER_DUST[Math.max(0, top - 1)];
         }
      }
   }

   private static void dust(ServerLevel level, Vector3f color, Vec3 at, int count, double spread, float size) {
      level.sendParticles(new DustParticleOptions(color, size), at.x, at.y, at.z, count, spread, spread * 0.8D, spread, 0.01D);
   }

   private static void xpEvent(ServerPlayer player, int xp) {
      int gained = com.myriam.unrp.ability.OriginGrowth.award(player, xp);
      if (gained > 0) {
         int lv = com.myriam.unrp.ability.OriginGrowth.level(player);
         int milli = com.myriam.unrp.ability.OriginGrowth.gainMilli(lv);
         bar(player, "§6Potential deepens. §eLevel " + lv + " §7(+" + (double)milli / 1000.0D + " growth)");
         Level var6 = player.level();
         if (var6 instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)var6;
            Fx.ring(level, TIER_RGB[Math.max(0, unlockedTop(player) - 1)], player.position(), 1.4D, 16);
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
               UUID var8 = player.getUUID();
               tickDown(boltCd, var8);
               tickDown(waveCd, var8);
               tickDown(rushCd, var8);
               if (!player.isAlive()) {
                  if (waveCharge.remove(var8) != null) {
                     orbDrop(player, level);
                     MoveAnim.stop(player, "lb_wave");
                  }

                  rushing.remove(var8);
                  rushHit.remove(var8);
                  player.removeTag("unrp.lb_bolt_trigger");
                  player.removeTag("unrp.lb_wave_trigger");
                  player.removeTag("unrp.lb_break_trigger");
                  player.removeTag("unrp.lb_rush_trigger");
               } else {
                  Set<String> tags = player.getTags();
                  boolean armed = tags.contains("unrp.has.limit_breaker");
                  if (tags.contains("unrp.lb_bolt_trigger")) {
                     player.removeTag("unrp.lb_bolt_trigger");
                     if (armed && player.isShiftKeyDown()) {
                        doBarrage(player, level);
                     } else if (armed) {
                        doBolt(player, level);
                     }
                  }

                  if (tags.contains("unrp.lb_wave_trigger")) {
                     player.removeTag("unrp.lb_wave_trigger");
                     if (armed && player.isShiftKeyDown()) {
                        doNova(player, level);
                     } else if (armed) {
                        startWave(player, level);
                     }
                  }

                  if (tags.contains("unrp.lb_break_trigger")) {
                     player.removeTag("unrp.lb_break_trigger");
                     if (armed && player.isShiftKeyDown()) {
                        releaseForm(player, true);
                     } else if (armed) {
                        doBreak(player, level);
                     }
                  }

                  if (tags.contains("unrp.lb_rush_trigger")) {
                     player.removeTag("unrp.lb_rush_trigger");
                     if (armed) {
                        doRush(player, level, player.isSprinting());
                     }
                  }

                  tickBecoming(player, level);
                  tickWave(player, level);
                  tickRush(player, level);
                  tickAloft(player, armed);
                  if (armed && player.tickCount % 40 == 7 && counterReady.contains(var8)) {
                     OriginLooks.counter(player, 60);
                  }

                  if (player.tickCount % 10 == 0 && armed) {
                     tickAura(player, level);
                  }

                  if (player.tickCount % 20 == 0) {
                     slowTick(player, level);
                  }

                  if (player.tickCount % 40 == 0) {
                     boolean has = player.getTags().contains("unrp.has.limit_breaker");
                     com.myriam.unrp.ability.OriginGrowth.apply(player, "limit_breaker", has, -4.0D, -0.5D, 0.0D);
                     formAttrs(player, has ? form(player) : 0, has ? fineForm(player) : 0);
                  }

               }
            }
         }
      }
   }

   private static void tickDown(Map<UUID, Integer> map, UUID id) {
      Integer left = (Integer)map.get(id);
      if (left != null) {
         if (left <= 1) {
            map.remove(id);
         } else {
            map.put(id, left - 1);
         }
      }

   }

   private static void slowTick(ServerPlayer player, ServerLevel level) {
      boolean has = player.getTags().contains("unrp.has.limit_breaker");
      if (has) {
         tierSync(player, level);
         int f = form(player);
         if (f > 0 && f > unlockedTop(player)) {
            releaseForm(player, false);
            f = 0;
         }

         boolean active = isActive(player);
         if (f > 0 && !active) {
            releaseForm(player, false);
            f = 0;
         }

         boolean flying = aloft.getOrDefault(player.getUUID(), 0) > 20;
         if (f > 0) {
            int keep = (int)Math.ceil((double)UPKEEP[f - 1] * LimitBreakerForms.UPKEEP_MULT[fineForm(player)]);
            if (flying) {
               ++keep;
            }

            int have_0 = score(player, "unrp.lb_grit");
            if (have_0 < keep) {
               releaseForm(player, false);
               bar(player, "§8The grit runs dry. The limit closes back over you.");
               f = 0;
            } else {
               setScore(player, "unrp.lb_grit", have_0 - keep);
            }
         } else if (flying) {
            int have = score(player, "unrp.lb_grit");
            if (have > 0) {
               setScore(player, "unrp.lb_grit", have - 1);
            }
         }

         int ki = score(player, "unrp.lb_ki");
         int kiMax = kiCap(player);
         if (ki < kiMax) {
            setScore(player, "unrp.lb_ki", Math.min(kiMax, ki + 3 + (f > 0 ? 1 : 0)));
         }

         if (f > 0) {
            syncForm(player, composite(player, fineForm(player)));
         }

         if (player.tickCount % 20 == 0) {
            int lv = com.myriam.unrp.ability.OriginGrowth.level(player);
            int edgePct = (int)Math.round(edge(player) * EDGE_K[f] * 100.0D);
            com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new GritHudPacket(score(player, "unrp.lb_grit"), gritCap(player), lv, com.myriam.unrp.ability.OriginGrowth.xp(player), com.myriam.unrp.ability.OriginGrowth.xpToNext(lv), edgePct, f, Math.max(1, unlockedTop(player)), score(player, "unrp.lb_ki"), kiCap(player)));
         }

         if (f > 0 && player.tickCount % 40 == 0) {
            int asc = ascend(player);
            if (asc > 0) {
               OriginLooks.ascend(player, asc);
            }
         }

         float[] low = (float[])hurtLow.get(player.getUUID());
         if (low != null && (float)level.getGameTime() - low[1] > 600.0F) {
            hurtLow.remove(player.getUUID());
         }

      } else {
         if (form(player) > 0) {
            for(String t : FORM_TAGS) {
               player.removeTag(t);
            }

            syncForm(player, 0);
         }

         if (becoming.remove(player.getUUID()) != null) {
            MoveAnim.stop(player, "lb_break");
         }

         if (waveCharge.remove(player.getUUID()) != null) {
            orbDrop(player, level);
            MoveAnim.stop(player, "lb_wave");
         }

         rushing.remove(player.getUUID());
         rushHit.remove(player.getUUID());
      }
   }

   private static void tickAura(ServerPlayer player, ServerLevel level) {
      UUID id = player.getUUID();
      Integer glowLeft = (Integer)afterglow.get(id);
      if (glowLeft != null) {
         if (glowLeft <= 10) {
            afterglow.remove(id);
         } else {
            afterglow.put(id, glowLeft - 10);
         }
      }

      if (isActive(player)) {
         int f = form(player);
         double e = edge(player);
         Vector3f color = dustColor(player);
         Vec3 base = player.position();
         if (f > 0) {
            Fx.circle(level, new DustParticleOptions(color, 1.2F), base.add(0.0D, 0.15D, 0.0D), 0.85D, 10, 0.03D, 0.0D);
            dust(level, color, base.add(0.0D, 1.1D, 0.0D), 4 + (int)(e * 8.0D), 0.4D, 1.1F + (float)e * 0.5F);
            if (e > 0.3D) {
               level.sendParticles(ParticleTypes.FLAME, base.x, base.y + 0.7D, base.z, 1 + (int)(e * 5.0D), 0.3D, 0.5D, 0.3D, 0.01D);
            }

            if (f == 4 && player.tickCount % 40 == 0) {
               Fx.shell(level, Fx.glint(15396853), base.add(0.0D, 1.1D, 0.0D), 1.3D, 10, 0.15D);
            }
         } else if (e > 0.35D) {
            dust(level, color, base.add(0.0D, 1.0D, 0.0D), (int)(e * 5.0D), 0.4D, 0.9F);
         }

         if (glowLeft != null) {
            float t = (float)glowLeft.intValue() / 60.0F;
            Fx.ring(level, TIER_RGB[Math.max(0, (f > 0 ? f : unlockedTop(player)) - 1)], base.x, base.y + 0.08D, base.z, 1.2D + (double)(1.0F - t) * 1.6D, 8);
         }

      }
   }

   private static boolean airborne(ServerPlayer player) {
      return !player.onGround() && !player.isInWater() && !player.isInLava() && !player.isPassenger() && !player.isFallFlying() && !player.onClimbable() && !player.getAbilities().flying;
   }

   private static void tickAloft(ServerPlayer player, boolean armed) {
      UUID id = player.getUUID();
      if (!player.isCreative() && !player.isSpectator() && airborne(player)) {
         Integer run = (Integer)aloft.get(id);
         if (armed && isActive(player) && score(player, "unrp.lb_grit") > 0) {
            aloft.put(id, run == null ? 1 : run + 1);
            if (run != null && run >= 20) {
               player.fallDistance = 0.0F;
            }
         } else if (run != null) {
            aloft.remove(id);
            if (run > 20) {
               player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 80, 0, false, false));
               if (armed && score(player, "unrp.lb_grit") <= 0) {
                  bar(player, "§8The grit runs out under you.");
               }
            }
         }

      } else {
         aloft.remove(id);
      }
   }

   private static void formAttrs(ServerPlayer player, int f, int fine) {
      boolean ape = LimitBreakerForms.apeForm(fine);
      double speed = FORM_SPEED[f] + (ape ? -0.015D : 0.0D);
      attr(player, Attributes.MOVEMENT_SPEED, SPEED_ID, "unrp_lb_speed", speed);
      attr(player, Attributes.ATTACK_SPEED, ATKSPD_ID, "unrp_lb_atkspd", FORM_ATKSPD[f]);
      attr(player, Attributes.ARMOR, FORM_ARMOR_ID, "unrp_lb_armor", FORM_ARMOR[f] + (ape ? 6.0D : 0.0D));
      attr(player, Attributes.ATTACK_DAMAGE, APE_DMG_ID, "unrp_lb_ape_dmg", ape ? (fine == 7 ? 7.0D : 4.0D) : 0.0D);
      attr(player, Attributes.KNOCKBACK_RESISTANCE, APE_KB_ID, "unrp_lb_ape_kb", ape ? 1.0D : 0.0D);
   }

   private static void attr(ServerPlayer player, Attribute attribute, UUID id, String name, double amount) {
      AttributeInstance inst = player.getAttribute(attribute);
      if (inst != null) {
         AttributeModifier had = inst.getModifier(id);
         if (had != null || !(Math.abs(amount) < 0.001D)) {
            if (had == null || !(Math.abs(had.getAmount() - amount) < 0.001D)) {
               if (had != null) {
                  inst.removeModifier(id);
               }

               if (Math.abs(amount) > 0.001D) {
                  inst.addTransientModifier(new AttributeModifier(id, name, amount, Operation.ADDITION));
               }

            }
         }
      }
   }

   public static int ladderStep(ServerPlayer player) {
      Set<String> tags = player.getTags();
      if (tags.contains("unrp.lb_tier4")) {
         return 5;
      } else if (tags.contains("unrp.lb_tier3")) {
         return 4;
      } else if (tags.contains("unrp.lb_tier2")) {
         return 3;
      } else {
         return com.myriam.unrp.ability.OriginGrowth.level(player) >= 4 ? 2 : 1;
      }
   }

   public static int ladderLevel(int step) {
      return step <= 1 ? 0 : (step == 2 ? 4 : (step == 3 ? 10 : (step == 4 ? 25 : 45)));
   }

   public static String ladderName(int step) {
      return FORM_NAMES[Math.max(2, Math.min(5, step)) - 2];
   }

   public static boolean ladderFeat(ServerPlayer player, int step) {
      String feat = step == 3 ? "unrp.lb_feat_gold" : (step == 4 ? "unrp.lb_feat_blue" : (step == 5 ? "unrp.lb_feat_silver" : null));
      return feat != null && player.getTags().contains(feat);
   }

   private static void tierSync(ServerPlayer player, ServerLevel level) {
      int lv = com.myriam.unrp.ability.OriginGrowth.level(player);
      Set<String> tags = player.getTags();
      boolean t2 = lv >= 10 && tags.contains("unrp.lb_feat_gold");
      boolean t3 = t2 && lv >= 25 && tags.contains("unrp.lb_feat_blue");
      boolean t4 = t3 && lv >= 45 && tags.contains("unrp.lb_feat_silver");
      tier(player, level, "unrp.lb_tier2", t2, 2);
      tier(player, level, "unrp.lb_tier3", t3, 3);
      tier(player, level, "unrp.lb_tier4", t4, 4);
   }

   private static void tier(ServerPlayer player, ServerLevel level, String tag, boolean want, int idx) {
      Set<String> tags = player.getTags();
      if (want && !tags.contains(tag)) {
         player.addTag(tag);
         ceremony(player, level, idx);
      } else if (!want && tags.contains(tag)) {
         player.removeTag(tag);
      }

   }

   private static void ceremony(ServerPlayer player, ServerLevel level, int idx) {
      Vector3f color = TIER_DUST[idx - 1];
      Vec3 base = player.position();
      if (idx == 4) {
         if (!player.isInvisible()) {
            OriginLooks.stillness(player);
         }

         Fx.shell(level, Fx.glint(15396853), base.add(0.0D, 1.2D, 0.0D), 2.2D, 60, 0.0D);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.resonate"), SoundSource.PLAYERS, 1.2F, 0.5F);
      } else {
         for(int h = 0; h < 20; ++h) {
            dust(level, color, base.add(0.0D, 0.3D + (double)h * 0.5D, 0.0D), 10, 0.4D, 1.7F);
         }

         level.sendParticles(ParticleTypes.END_ROD, base.x, base.y + 4.0D, base.z, 40, 0.5D, 3.5D, 0.5D, 0.05D);

         for(int i = 1; i <= 3; ++i) {
            Fx.ring(level, TIER_RGB[idx - 1], base.x, base.y + 0.05D, base.z, 1.8D * (double)i, 18);
         }

         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.blaze.shoot"), SoundSource.PLAYERS, 1.4F, 0.6F + (float)idx * 0.1F);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.resonate"), SoundSource.PLAYERS, 1.2F, 0.7F + (float)idx * 0.12F);
      }

      MinecraftServer server = player.getServer();
      if (server != null) {
         server.getPlayerList().broadcastSystemMessage(Component.literal("§6" + player.getName().getString() + "§e has broken through to §6" + TIER_NAMES[idx - 1] + " Potential§e."), false);
      }

      bar(player, "§6A limit gives way. §e" + TIER_NAMES[idx - 1] + " Potential.");
   }

   private static void doBolt(ServerPlayer player, ServerLevel level) {
      if (!boltCd.containsKey(player.getUUID())) {
         boolean free = counterReady.remove(player.getUUID());
         if (free) {
            OriginLooks.counter(player, 0);
         }

         if (free || payKi(player, 4)) {
            Vec3 look = player.getLookAngle();
            float boltDmg = (float)((double)(free ? 7.0F : 4.0F) * dmgMult(player));
            spawnBolt(player, level, look, boltDmg, free ? 2.6D : 2.2D);
            CastFxEvents.pose(player, "lb_bolt", 8);
            Fx.cone(level, new DustParticleOptions(dustColor(player), 1.2F), player.getEyePosition().add(look.scale(0.5D)), look, 0.3D, 10, 0.3D);
            boltCd.put(player.getUUID(), free ? 2 : 10);
            Vector3f color = dustColor(player);
            dust(level, color, player.getEyePosition().add(look.scale(0.8D)), 8, 0.15D, 1.1F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.blaze.shoot"), SoundSource.PLAYERS, 1.0F, free ? 1.8F : 1.3F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.lightning_bolt.impact"), SoundSource.PLAYERS, 0.5F, 1.9F);
         }
      }
   }

   private static void doBarrage(ServerPlayer player, ServerLevel level) {
      if (!boltCd.containsKey(player.getUUID())) {
         if (payKi(player, 12)) {
            Vec3 look = player.getLookAngle();
            float fanDmg = (float)(3.0D * dmgMult(player));

            for(int i = -2; i <= 2; ++i) {
               spawnBolt(player, level, rotateYaw(look, (double)i * 7.0D), fanDmg, 1.9D);
            }

            CastFxEvents.pose(player, "lb_bolt", 8);
            boltCd.put(player.getUUID(), 30);
            dust(level, dustColor(player), player.getEyePosition().add(look.scale(0.8D)), 16, 0.3D, 1.2F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.blaze.shoot"), SoundSource.PLAYERS, 1.0F, 0.9F);
         }
      }
   }

   private static void spawnBolt(ServerPlayer player, ServerLevel level, Vec3 dir, float dmg, double speed) {
      Vec3 pos = player.getEyePosition().add(dir.scale(0.7D)).add(0.0D, -0.15D, 0.0D);
      ArcaneBoltEntity b = new ArcaneBoltEntity(com.myriam.unrp.entity.ModEntities.ARCANE_BOLT.get(), level);
      b.setPos(pos.x, pos.y, pos.z);
      b.configure(player.getUUID(), dmg, 0.55F, 1.5F, dir.scale(speed));
      int f = form(player);
      b.tint(TIER_RGB[Math.max(0, (f > 0 ? f : unlockedTop(player)) - 1)]);
      level.addFreshEntity(b);
   }

   private static Vec3 rotateYaw(Vec3 v, double degrees) {
      double rad = Math.toRadians(degrees);
      double cos = Math.cos(rad);
      double sin = Math.sin(rad);
      return new Vec3(v.x * cos - v.z * sin, v.y, v.x * sin + v.z * cos);
   }

   private static void startWave(ServerPlayer player, ServerLevel level) {
      UUID id = player.getUUID();
      if (!waveCharge.containsKey(id) && !waveCd.containsKey(id)) {
         if (unlockedTop(player) < 2) {
            bar(player, "§8The Wave opens past the second limit.");
         } else if (payKi(player, 20)) {
            waveCharge.put(id, 50);
            orbSync(player, level, 0.0F);
            CastFxEvents.pose(player, "lb_wave", 50);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.respawn_anchor.charge"), SoundSource.PLAYERS, 1.0F, 0.7F);
         }
      }
   }

   private static void orbSync(ServerPlayer player, ServerLevel level, float t) {
      UUID id = player.getUUID();
      Vec3 palms = player.getEyePosition().add(player.getLookAngle().scale(1.05D)).add(0.0D, -0.18D, 0.0D);
      WaveOrbEntity orb = null;
      Integer eid = (Integer)waveOrb.get(id);
      if (eid != null) {
         Entity var8 = level.getEntity(eid);
         if (var8 instanceof WaveOrbEntity) {
            WaveOrbEntity e = (WaveOrbEntity)var8;
            if (e.isAlive()) {
               orb = e;
            }
         }
      }

      if (orb == null) {
         orb = new WaveOrbEntity(com.myriam.unrp.entity.ModEntities.WAVE_ORB.get(), level);
         orb.setPos(palms.x, palms.y, palms.z);
         orb.setOwner(player.getId());
         level.addFreshEntity(orb);
         waveOrb.put(id, orb.getId());
      }

      orb.lease();
      orb.setPos(palms.x, palms.y, palms.z);
      orb.setRadius(0.22F + t * 0.75F);
      int f = form(player);
      orb.setColor(TIER_RGB[Math.max(0, (f > 0 ? f : unlockedTop(player)) - 1)]);
   }

   private static void orbDrop(ServerPlayer player, ServerLevel level) {
      Integer eid = (Integer)waveOrb.remove(player.getUUID());
      if (eid != null) {
         Entity var4 = level.getEntity(eid);
         if (var4 instanceof WaveOrbEntity) {
            WaveOrbEntity e = (WaveOrbEntity)var4;
            e.discard();
         }
      }

   }

   private static void tickWave(ServerPlayer player, ServerLevel level) {
      UUID id = player.getUUID();
      Integer left = (Integer)waveCharge.get(id);
      if (left != null) {
         player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 3, false, false));
         Vec3 palms = player.getEyePosition().add(player.getLookAngle().scale(0.9D)).add(0.0D, -0.2D, 0.0D);
         float t = (float)(50 - left) / 50.0F;
         Vector3f color = dustColor(player);
         orbSync(player, level, t);
         Fx.shell(level, new DustParticleOptions(color, 1.1F + t * 0.9F), palms, 1.7D - (double)t * 0.5D, 4, -1.4D - (double)t * 1.0D);
         if (left % 4 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, palms.x, palms.y, palms.z, 1, 0.05D, 0.05D, 0.05D, 0.01D);
         }

         if (left % 10 == 0) {
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.note_block.harp"), SoundSource.PLAYERS, 0.7F, 0.6F + t);
         }

         if (left == 5) {
            MoveAnim.release(player, "lb_wave");
         }

         waveCharge.put(id, left - 1);
         if (left <= 1) {
            waveCharge.remove(id);
            orbDrop(player, level);
            fireWave(player, level);
         }
      }
   }

   private static void fireWave(ServerPlayer player, ServerLevel level) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      boolean deep = form(player) >= 3;
      float base = deep ? 22.0F : 16.0F;
      float full = (float)((double)base * dmgMult(player));
      int f = form(player);
      int tier = TIER_RGB[Math.max(0, (f > 0 ? f : unlockedTop(player)) - 1)];
      Vector3f color = dustColor(player);
      DustParticleOptions sheath = new DustParticleOptions(color, 1.9F);
      Fx.cone(level, sheath, eye.add(look.scale(0.8D)), look, 0.42D, 60, 0.7D);
      Fx.shell(level, Fx.glint(tier), eye.add(look.scale(1.0D)), 0.8D, 40, 1.6D);
      Fx.ring(level, tier, eye.add(look.scale(1.4D)), 1.3D, 14);
      Fx.ring(level, 16777215, eye.add(look.scale(1.2D)), 0.7D, 10);
      Vec3 kick = look.scale(-0.35D);
      player.setDeltaMovement(player.getDeltaMovement().add(kick.x, 0.05D, kick.z));
      player.hurtMarked = true;
      HomingBeamEntity beam = (HomingBeamEntity)(com.myriam.unrp.entity.ModEntities.HOMING_BEAM.get()).create(level);
      if (beam != null) {
         Vec3 start = eye.add(look.scale(1.2D)).add(0.0D, -0.1D, 0.0D);
         beam.moveTo(start.x, start.y, start.z, player.getYRot(), player.getXRot());
         beam.configure(player, -1, look, 1.8D, 0.0D, full, deep ? 140 : 110, deep ? 2.2F : 1.8F, 0, 1.0D, tier);
         beam.waveMode();
         beam.carve(deep ? 200 : 140);
         level.addFreshEntity(beam);
      }

      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.sonic_boom"), SoundSource.PLAYERS, 1.8F, deep ? 0.8F : 1.0F);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.lightning_bolt.impact"), SoundSource.PLAYERS, 1.0F, 1.5F);
      waveCd.put(player.getUUID(), 60);
   }

   private static void doNova(ServerPlayer player, ServerLevel level) {
      if (!waveCd.containsKey(player.getUUID()) && !waveCharge.containsKey(player.getUUID())) {
         if (unlockedTop(player) < 2) {
            bar(player, "§8The Wave opens past the second limit.");
         } else if (payKi(player, 16)) {
            float full = (float)(7.0D * dmgMult(player));
            Vec3 heart = player.position().add(0.0D, 1.0D, 0.0D);

            for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0D), (en) -> en.isAlive() && en != player)) {
               OriginMasteryEvents.hurt(player, e, level.damageSources().magic(), floored(e, full));
               Vec3 out = e.position().subtract(player.position());
               Vec3 dir = (new Vec3(out.x, 0.0D, out.z)).normalize();
               e.setDeltaMovement(dir.x * 1.4D, 0.55D, dir.z * 1.4D);
               e.hurtMarked = true;
            }

            Vector3f color = dustColor(player);
            DustParticleOptions veil = new DustParticleOptions(color, 1.8F);
            Fx.shell(level, Fx.flare(16765519), heart, 1.2D, 50, 2.2D);
            Fx.shell(level, veil, heart, 2.4D, 46, 1.4D);
            Fx.burst(level, Fx.glint(TIER_RGB[Math.max(0, form(player) - 1)]), heart, 30, 1.0D, 0.3D);
            level.sendParticles(ParticleTypes.END_ROD, heart.x, heart.y, heart.z, 40, 0.6D, 0.6D, 0.6D, 0.3D);

            for(int hp = 0; hp < 10; ++hp) {
               dust(level, color, player.position().add(0.0D, 0.3D + (double)hp * 0.55D, 0.0D), 2, 0.12D, 1.6F);
            }

            for(int i = 1; i <= 4; ++i) {
               Fx.ring(level, TIER_RGB[Math.max(0, form(player) - 1)], player.position().x, player.position().y + 0.05D, player.position().z, 1.5D * (double)i, 14);
            }

            dust(level, color, heart, 40, 1.8D, 1.6F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 1.2F, 1.3F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.sonic_boom"), SoundSource.PLAYERS, 1.0F, 1.6F);
            waveCd.put(player.getUUID(), 60);
            novaLook(player, level);
         }
      }
   }

   private static void novaLook(ServerPlayer player, ServerLevel level) {
      if (!player.isInvisible()) {
         int rgb = formRgb(player);
         Vec3 look = player.getLookAngle();
         Vec3 flat = new Vec3(look.x, 0.0D, look.z);
         flat = flat.lengthSqr() < 1.0E-4D ? Vec3.ZERO : flat.normalize();
         Vec3 at = player.position();
         Vec3 core = at.add(0.0D, 1.15D, 0.0D).add(flat.scale(0.55D));
         double gy = DaystarEvents.groundUnder(level, at, 3.0D);
         OriginLooks.nova(player, core, rgb, 5.0D, gy);
         if (!Double.isNaN(gy)) {
            Vec3 floor = new Vec3(at.x, gy, at.z);
            Fx.circle(level, Fx.press(rgb), floor.add(0.0D, 0.12D, 0.0D), 0.9D, 40, 0.0D, 0.55D);
            Spectacle.after(3, () -> Fx.circle(level, Fx.press(rgb), floor.add(0.0D, 0.12D, 0.0D), 2.4D, 32, 0.0D, 0.45D));
         }
      }
   }

   private static int formRgb(ServerPlayer player) {
      int fine = fineForm(player);
      return LimitBreakerForms.valid(fine) ? LimitBreakerForms.color(fine, rose(player)) : TIER_RGB[Math.max(0, unlockedTop(player) - 1)];
   }

   private static void dodgeLook(ServerPlayer player, ServerLevel level, Entity from, Vec3 side) {
      OriginLooks.counter(player, 60);
      if (!player.isInvisible()) {
         int rgb = formRgb(player);
         Vec3 at = player.position();
         Vec3 toward = from == null ? Vec3.ZERO : from.position().subtract(at);
         toward = new Vec3(toward.x, 0.0D, toward.z);
         toward = toward.lengthSqr() < 1.0E-4D ? Vec3.ZERO : toward.normalize().scale(0.4D);
         Vec3 copy = at.add(toward).subtract(side.normalize().scale(0.5D));
         OriginLooks.afterimage(player, copy, player.yBodyRot, rgb);
         Spectacle.after(4, () -> level.sendParticles(Fx.flake(rgb), copy.x, copy.y + 0.95D, copy.z, 16, 0.22D, 0.45D, 0.22D, 0.01D));
         Spectacle.after(8, () -> level.sendParticles(Fx.flake(rgb), copy.x, copy.y + 0.95D, copy.z, 10, 0.25D, 0.5D, 0.25D, 0.01D));
      }
   }

   private static int igniteCost(int fine) {
      int era = LimitBreakerForms.era(fine);
      return IGNITE_COST[era - 1] + 2 * (fine - LimitBreakerForms.firstOfEra(era));
   }

   private static void doBreak(ServerPlayer player, ServerLevel level) {
      if (!becoming.containsKey(player.getUUID())) {
         if (unlockedTop(player) <= 0) {
            bar(player, "§8Your potential hasn't found its first break yet.");
         } else {
            boolean[] open = unlockedForms(player);
            int[] costs = new int[15];
            int mask = 0;

            for(int f = 1; f < 15; ++f) {
               if (open[f]) {
                  mask |= 1 << f;
                  costs[f] = igniteCost(f);
               }
            }

            com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new FormWheelPacket(mask, fineForm(player), rose(player), com.myriam.unrp.ability.OriginGrowth.level(player), score(player, "unrp.lb_grit"), costs, player.getTags().contains("unrp.lb_aura_off")));
         }
      }
   }

   public static void pickForm(ServerPlayer player, int want) {
      Level id = player.level();
      if (id instanceof ServerLevel level) {
         UUID var8 = player.getUUID();
         if (!becoming.containsKey(var8)) {
            if (want == -1) {
               if (player.getTags().contains("unrp.lb_aura_off")) {
                  player.removeTag("unrp.lb_aura_off");
                  bar(player, "§6The aura returns to you.");
               } else {
                  player.addTag("unrp.lb_aura_off");
                  bar(player, "§8The aura goes quiet.");
               }

               int fine_0 = fineForm(player);
               if (fine_0 > 0) {
                  syncForm(player, composite(player, fine_0));
               }

            } else if (want <= 0) {
               releaseForm(player, true);
            } else if (LimitBreakerForms.valid(want) && unlockedForms(player)[want]) {
               int fine = fineForm(player);
               if (want == fine) {
                  bar(player, "§8You already hold that shape.");
               } else {
                  int era = form(player);
                  int wantEra = LimitBreakerForms.era(want);
                  if (!roomFor(player, want)) {
                     cramped(player, want, false);
                  } else if (era == wantEra && era > 0) {
                     if (pay(player, 4)) {
                        setScore(player, "unrp.lb_fine", want);
                        formAttrs(player, era, want);
                        applyScale(player, LimitBreakerForms.SCALE[want]);
                        syncForm(player, composite(player, want));
                        Vec3 heart = player.position().add(0.0D, 1.1D, 0.0D);
                        Fx.shell(level, new DustParticleOptions(fineVec(player, want), 1.6F), heart, 1.1D, 40, 1.2D);
                        Fx.ring(level, LimitBreakerForms.color(want, rose(player)), player.position().add(0.0D, 0.08D, 0.0D), 1.8D, 14);
                        level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.resonate"), SoundSource.PLAYERS, 1.0F, 0.9F + (float)want * 0.04F);
                        bar(player, "§6" + LimitBreakerForms.displayName(want, rose(player)) + "§e settles over you.");
                     }
                  } else if (pay(player, igniteCost(want))) {
                     becoming.put(var8, new int[]{60, wantEra, want});
                     breakAnchor.put(var8, player.position());
                     CastFxEvents.pose(player, "lb_break", 60);
                     com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new BreakCinePacket(60, wantEra));
                     com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.ALL.noArg(), new LbBecomingPacket(var8, composite(player, want), 60));
                     level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.heartbeat"), SoundSource.PLAYERS, 1.2F, 0.6F + (float)wantEra * 0.15F);
                  }
               }
            } else {
               bar(player, "§8That form hasn't opened to you yet.");
            }
         }
      }
   }

   private static void tickBecoming(ServerPlayer player, ServerLevel level) {
      UUID id = player.getUUID();
      int[] state = (int[])becoming.get(id);
      if (state != null) {
         int left = state[0];
         int target = state[1];
         int fine = state.length > 2 ? state[2] : LimitBreakerForms.firstOfEra(target);
         float t = (float)(60 - left) / 60.0F;
         Vec3 anchor = (Vec3)breakAnchor.getOrDefault(id, player.position());
         if (player.position().distanceToSqr(anchor) > 0.02D) {
            player.teleportTo(anchor.x, anchor.y, anchor.z);
         }

         player.setDeltaMovement(0.0D, Math.min(0.0D, player.getDeltaMovement().y), 0.0D);
         player.hurtMarked = true;
         player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 8, 5, false, false));
         Vec3 base = anchor;
         Vec3 heart = anchor.add(0.0D, 1.1D, 0.0D);
         Vector3f color = fineVec(player, fine);
         DustParticleOptions veil = new DustParticleOptions(color, 1.5F);
         Fx.shell(level, veil, heart, 1.05D + (double)t * 0.35D, 18 + (int)(t * 26.0F), 0.0D);
         double swirl = (double)(60 - left) * 0.42D;

         for(int strand = 0; strand < 4; ++strand) {
            double dirSpin = strand < 2 ? 1.0D : -1.0D;
            double ang = swirl * dirSpin + (double)strand * Math.PI * 0.5D;
            double rr = (strand < 2 ? 1.15D : 1.55D) - (double)t * 0.25D;
            dust(level, color, base.add(Math.cos(ang) * rr, 0.2D + (double)t * 2.2D + (double)strand * 0.45D, Math.sin(ang) * rr), 2, 0.03D, 1.7F);
         }

         if (left % 2 == 0) {
            for(int hp = 0; hp < 10; ++hp) {
               dust(level, color, base.add(0.0D, 0.4D + (double)hp * (0.9D + (double)t * 0.6D), 0.0D), 1, 0.1D + (double)t * 0.1D, 1.3F);
            }

            for(int hp = 0; hp < 5; ++hp) {
               level.sendParticles(ParticleTypes.END_ROD, base.x, base.y + 0.3D + (double)hp * (1.2D + (double)t * 1.4D), base.z, 1, 0.2D, 0.15D, 0.2D, 0.01D);
            }
         }

         if (left % 12 == 0) {
            Fx.ring(level, TIER_RGB[target - 1], base.x, base.y + 0.05D, base.z, 1.6D + (double)t * 3.4D, 12);
            Fx.ring(level, 16777215, base.x, base.y + 1.6D, base.z, 0.8D + (double)t * 1.6D, 10);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.beacon.power_select"), SoundSource.PLAYERS, 0.9F + t, 0.6F + t * 0.9F);
         }

         if (left % 20 == 0) {
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.heartbeat"), SoundSource.PLAYERS, 0.9F + t * 0.8F, 0.7F + t * 0.6F);
         }

         int elapsed = 60 - left;

         for(int k = 0; k < GROW_AT.length; ++k) {
            if (elapsed == GROW_AT[k]) {
               float from = LimitBreakerForms.SCALE[fineForm(player)];
               float to = LimitBreakerForms.SCALE[fine];
               if (from != to) {
                  applyScale(player, from + (to - from) * GROW_SHARE[k]);
               }
            }
         }

         if (left == 5) {
            MoveAnim.release(player, "lb_break");
         }

         state[0] = left - 1;
         if (left <= 1) {
            becoming.remove(id);
            breakAnchor.remove(id);
            if (!roomFor(player, fine)) {
               setScore(player, "unrp.lb_grit", Math.min(gritCap(player), score(player, "unrp.lb_grit") + igniteCost(fine)));
               com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new BreakCinePacket(0, 0));
               unbecome(player, state, true);
               cramped(player, fine, true);
            } else {
               for(String tag : FORM_TAGS) {
                  player.removeTag(tag);
               }

               player.addTag(FORM_TAGS[target - 1]);
               setScore(player, "unrp.lb_fine", fine);
               formAttrs(player, target, fine);
               applyScale(player, LimitBreakerForms.SCALE[fine]);
               syncForm(player, composite(player, fine));
               afterglow.put(id, 60);
               Fx.shell(level, Fx.flare(TIER_RGB[target - 1]), heart, 1.0D, 110, 2.8D);
               Fx.shell(level, veil, heart, 1.6D, 90, 1.9D);
               Fx.burst(level, Fx.glint(TIER_RGB[target - 1]), heart, 70, 1.3D, 0.36D);
               level.sendParticles(ParticleTypes.END_ROD, heart.x, heart.y, heart.z, 80, 0.4D, 0.9D, 0.4D, 0.45D);

               for(int hp = 0; hp < 18; ++hp) {
                  dust(level, color, base.add(0.0D, 0.4D + (double)hp * 0.75D, 0.0D), 2, 0.14D, 1.8F);
               }

               for(int i = 1; i <= 9; ++i) {
                  Fx.ring(level, i % 2 == 0 ? TIER_RGB[target - 1] : 16765519, base.x, base.y + 0.05D, base.z, 1.1D * (double)i, 16);
               }

               for(int i = 0; i < 8; ++i) {
                  double a = (Math.PI * 2D) * (double)i / 8.0D;
                  double up = i % 2 == 0 ? 0.55D : 0.2D;
                  Fx.line(level, Fx.glint(16777215), heart, heart.add(Math.cos(a) * 5.0D * (1.0D - up * 0.5D), up * 5.0D, Math.sin(a) * 5.0D * (1.0D - up * 0.5D)), 0.3D, 0.04D, 0.0D);
               }

               level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.lightning_bolt.thunder"), SoundSource.PLAYERS, 0.8F, 1.3F);
               level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 1.6F, 0.8F);
               level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.sonic_boom"), SoundSource.PLAYERS, 1.2F, 1.4F);
               level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.amethyst_block.resonate"), SoundSource.PLAYERS, 1.4F, 0.5F + (float)target * 0.15F);
               float hit = (float)((double)(3.0F + 2.0F * (float)target) * dmgMult(player));

               for(LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(7.0D), (en) -> en.isAlive() && en != player)) {
                  Vec3 out = mob.position().subtract(heart);
                  Vec3 dir = (new Vec3(out.x, 0.0D, out.z)).normalize();
                  mob.setDeltaMovement(dir.x * 1.6D, 0.65D, dir.z * 1.6D);
                  mob.hurtMarked = true;
                  OriginMasteryEvents.hurt(player, mob, level.damageSources().magic(), floored(mob, hit));
               }

               bar(player, "§6" + FORM_NAMES[target - 1] + "§8: §6" + LimitBreakerForms.displayName(fine, rose(player)) + "§e holds.");
            }
         }
      }
   }

   private static void unbecome(ServerPlayer player, int[] state, boolean shrink) {
      if (shrink && state != null && state.length > 2 && LimitBreakerForms.SCALE[state[2]] != LimitBreakerForms.SCALE[fineForm(player)]) {
         applyScale(player, LimitBreakerForms.SCALE[fineForm(player)]);
      }

      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.ALL.noArg(), new LbBecomingPacket(player.getUUID(), 0, 0));
   }

   private static void releaseForm(ServerPlayer player, boolean spoken) {
      UUID id = player.getUUID();
      int[] was = (int[])becoming.remove(id);
      if (was != null) {
         bar(player, "§8The break falters.");
         com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new BreakCinePacket(0, 0));
         unbecome(player, was, true);
      }

      breakAnchor.remove(id);
      int f = form(player);
      int fine = fineForm(player);

      for(String tag : FORM_TAGS) {
         player.removeTag(tag);
      }

      if (f > 0) {
         setScore(player, "unrp.lb_fine", 0);
         formAttrs(player, 0, 0);
         if (LimitBreakerForms.SCALE[fine] != 1.0F) {
            applyScale(player, 1.0F);
         }

         syncForm(player, 0);
         if (spoken) {
            bar(player, "§7The limit settles back into place.");
         }
      }

   }

   private static void syncForm(ServerPlayer player, int f) {
      Integer prev = (Integer)lastForm.get(player.getUUID());
      if (prev == null || prev != f) {
         lastForm.put(player.getUUID(), f);
         com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.ALL.noArg(), new LbFormPacket(player.getUUID(), f));
      }
   }

   private static void doRush(ServerPlayer player, ServerLevel level, boolean crash) {
      UUID id = player.getUUID();
      if (!rushing.containsKey(id) && !rushCd.containsKey(id)) {
         if (payKi(player, crash ? 10 : 6)) {
            Vec3 look = player.getLookAngle();
            LivingEntity mark = rushMark(player, level, look);
            if (mark != null) {
               rushAim.put(id, mark.getId());
            }

            double speed = crash ? 2.3D : 1.9D;
            player.setDeltaMovement(look.x * speed, Math.max(0.18D, look.y * speed * 0.5D), look.z * speed);
            player.hurtMarked = true;
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 16, 2, false, false));
            rushing.put(id, new int[]{14, crash ? 1 : 0});
            rushHit.put(id, new HashSet());
            rushCd.put(id, 40);
            CastFxEvents.pose(player, "lb_rush", 14);
            Fx.ring(level, TIER_RGB[Math.max(0, form(player) - 1)], player.position().x, player.position().y + 0.05D, player.position().z, 1.6D, 12);
            dust(level, dustColor(player), player.position().add(0.0D, 1.0D, 0.0D), 14, 0.5D, 1.6F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:item.trident.riptide_3"), SoundSource.PLAYERS, 1.0F, crash ? 0.8F : 1.1F);
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.sonic_charge"), SoundSource.PLAYERS, 0.7F, 1.6F);
         }
      }
   }

   private static LivingEntity rushMark(ServerPlayer player, ServerLevel level, Vec3 look) {
      Vec3 eye = player.getEyePosition();
      LivingEntity best = null;
      double bestDot = 0.86D;

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(28.0D), (en) -> en.isAlive() && en != player)) {
         Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
         double d = to.length();
         if (!(d < 2.0D) && !(d > 28.0D)) {
            double dot = to.normalize().dot(look);
            if (dot > bestDot) {
               bestDot = dot;
               best = e;
            }
         }
      }

      return best;
   }

   private static void tickRush(ServerPlayer player, ServerLevel level) {
      UUID id = player.getUUID();
      int[] state = (int[])rushing.get(id);
      if (state != null) {
         boolean crash = state[1] == 1;
         double speed = crash ? 2.3D : 1.9D;
         Vec3 dir = player.getLookAngle();
         Integer aimId = (Integer)rushAim.get(id);
         LivingEntity mark = null;
         if (aimId != null) {
            Entity wake_0 = level.getEntity(aimId);
            if (wake_0 instanceof LivingEntity) {
               LivingEntity le = (LivingEntity)wake_0;
               if (le.isAlive()) {
                  mark = le;
                  Vec3 to = le.getBoundingBox().getCenter().subtract(player.position().add(0.0D, 0.9D, 0.0D));
                  if (to.lengthSqr() > 1.0E-4D) {
                     dir = to.normalize();
                  }
               }
            }
         }

         player.setDeltaMovement(dir.scale(speed));
         player.hurtMarked = true;
         player.fallDistance = 0.0F;
         Vector3f color = dustColor(player);
         Vec3 wake = player.position().subtract(dir.scale(0.9D));
         dust(level, color, wake.add(0.0D, 1.0D, 0.0D), 10, 0.35D, 1.7F);
         dust(level, color, player.position().add(0.0D, 1.0D, 0.0D), 6, 0.25D, 1.3F);

         for(int s = 1; s <= 3; ++s) {
            Vec3 ghost = player.position().subtract(dir.scale(0.7D * (double)s));
            level.sendParticles(ParticleTypes.END_ROD, ghost.x, ghost.y + 1.0D, ghost.z, 2, 0.1D, 0.45D, 0.1D, 0.0D);
         }

         level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.2D, player.getZ(), 3, 0.2D, 0.05D, 0.2D, 0.02D);
         if (state[0] % 4 == 0) {
            level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.player.attack.sweep"), SoundSource.PLAYERS, 0.6F, 0.7F);
         }

         HashSet<UUID> hit = (HashSet)rushHit.get(id);
         float base = (float)((double)(crash ? 7.0F : 5.0F) * dmgMult(player));
         boolean landed = false;

         for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(1.9D), (en) -> en.isAlive() && en != player)) {
            if (hit == null || hit.add(e.getUUID())) {
               e.hurt(level.damageSources().playerAttack(player), floored(e, base));
               Vec3 aside = e.position().subtract(player.position());
               Vec3 shove = (new Vec3(aside.x, 0.0D, aside.z)).normalize();
               e.setDeltaMovement(shove.x * 1.1D, 0.5D, shove.z * 1.1D);
               e.hurtMarked = true;
               if (mark != null && e == mark) {
                  landed = true;
               }
            }
         }

         if (landed) {
            rushArrive(player, level, mark, crash, color);
            endRush(player, level, crash, color);
         } else if (player.horizontalCollision) {
            rushSmash(player, level, color);
            endRush(player, level, crash, color);
         } else {
            int var10002 = state[0]--;
            if (state[0] <= 0) {
               endRush(player, level, crash, color);
            }
         }
      }
   }

   private static void rushArrive(ServerPlayer player, ServerLevel level, LivingEntity mark, boolean crash, Vector3f color) {
      mark.invulnerableTime = 0;
      float slam = (float)((double)(crash ? 6.0F : 4.0F) * dmgMult(player));
      mark.hurt(level.damageSources().playerAttack(player), floored(mark, slam));
      mark.setDeltaMovement(mark.getDeltaMovement().add(0.0D, 0.85D, 0.0D));
      mark.hurtMarked = true;
      mark.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2, false, true));
      Vec3 at = mark.getBoundingBox().getCenter();
      Fx.shell(level, new DustParticleOptions(color, 1.7F), at, 1.4D, 26, 1.5D);
      Fx.ring(level, TIER_RGB[Math.max(0, form(player) - 1)], at.x, at.y - 0.8D, at.z, 2.2D, 14);
      level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
      level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 18, 0.5D, 0.5D, 0.5D, 0.25D);
      level.playSound((Player)null, mark.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 1.1F, 1.2F);
      level.playSound((Player)null, mark.blockPosition(), CastFxEvents.sound("minecraft:entity.warden.sonic_boom"), SoundSource.PLAYERS, 0.8F, 1.7F);
      player.setDeltaMovement(player.getDeltaMovement().scale(0.25D));
      player.hurtMarked = true;
   }

   private static void rushSmash(ServerPlayer player, ServerLevel level, Vector3f color) {
      Vec3 heart = player.position().add(0.0D, 1.0D, 0.0D);
      float shock = (float)(3.0D * dmgMult(player));

      for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3.0D), (en) -> en.isAlive() && en != player)) {
         OriginMasteryEvents.hurt(player, e, level.damageSources().magic(), floored(e, shock));
         Vec3 out = e.position().subtract(player.position());
         Vec3 away = (new Vec3(out.x, 0.0D, out.z)).normalize();
         e.setDeltaMovement(away.x * 1.0D, 0.4D, away.z * 1.0D);
         e.hurtMarked = true;
      }

      Fx.ring(level, TIER_RGB[Math.max(0, form(player) - 1)], player.position().x, player.position().y + 0.05D, player.position().z, 2.6D, 14);
      dust(level, color, heart, 24, 1.2D, 1.7F);
      level.sendParticles(ParticleTypes.EXPLOSION, heart.x, heart.y, heart.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:block.anvil.land"), SoundSource.PLAYERS, 0.9F, 0.7F);
      level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.8F, 1.4F);
   }

   private static void endRush(ServerPlayer player, ServerLevel level, boolean crash, Vector3f color) {
      UUID id = player.getUUID();
      rushing.remove(id);
      rushHit.remove(id);
      rushAim.remove(id);
      if (crash) {
         float burst = (float)(4.0D * dmgMult(player));

         for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(2.5D), (en) -> en.isAlive() && en != player)) {
            OriginMasteryEvents.hurt(player, e, level.damageSources().magic(), floored(e, burst));
            Vec3 out = e.position().subtract(player.position());
            Vec3 away = (new Vec3(out.x, 0.0D, out.z)).normalize();
            e.setDeltaMovement(away.x * 1.1D, 0.45D, away.z * 1.1D);
            e.hurtMarked = true;
         }

         dust(level, color, player.position().add(0.0D, 1.0D, 0.0D), 30, 1.4D, 1.7F);
         Fx.shell(level, new DustParticleOptions(color, 1.5F), player.position().add(0.0D, 1.0D, 0.0D), 1.8D, 30, 1.2D);
         Fx.ring(level, TIER_RGB[Math.max(0, form(player) - 1)], player.position().x, player.position().y + 0.05D, player.position().z, 2.4D, 12);
         level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.generic.explode"), SoundSource.PLAYERS, 0.9F, 1.5F);
      }
   }

   @SubscribeEvent
   public static void onOutgoing(LivingHurtEvent event) {
      Entity amount_0 = event.getSource().getEntity();
      if (amount_0 instanceof ServerPlayer attacker) {
         if (attacker != event.getEntity()) {
            if (attacker.getTags().contains("unrp.has.limit_breaker") && isActive(attacker)) {
               float amount = event.getAmount();
               event.setAmount((float)((double)amount * dmgMult(attacker)));
               LivingEntity victim = event.getEntity();
               if (meaningful(victim)) {
                  bank(attacker, event.getAmount() * 1.0F);
                  double frac = 1.0D - edge(attacker);
                  if (frac < 0.5D) {
                     int xp = Math.min(12, (int)Math.ceil((double)(event.getAmount() / 2.0F)));
                     if (frac < 0.25D) {
                        xp *= 2;
                     }

                     xpEvent(attacker, xp);
                  }

               }
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onIncoming(LivingHurtEvent event) {
      LivingEntity cap_0 = event.getEntity();
      if (cap_0 instanceof ServerPlayer player) {
         if (player.getTags().contains("unrp.has.limit_breaker")) {
            if (!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
               if (becoming.containsKey(player.getUUID())) {
                  event.setCanceled(true);
                  Level var3 = player.level();
                  if (var3 instanceof ServerLevel) {
                     ServerLevel level_0 = (ServerLevel)var3;
                     dust(level_0, dustColor(player), player.position().add(0.0D, 1.0D, 0.0D), 10, 0.5D, 1.4F);
                  }

               } else if (player.getTags().contains("unrp.lb_nature") && isActive(player)) {
                  if (event.getSource().getEntity() != null) {
                     double cap = fineForm(player) >= 13 ? 0.75D : 0.6D;
                     double chance = Math.min(cap, 0.25D + 0.35D * edge(player));
                     Level side_0 = player.level();
                     if (side_0 instanceof ServerLevel) {
                        ServerLevel level = (ServerLevel)side_0;
                        if (level.random.nextDouble() < chance) {
                           event.setCanceled(true);
                           counterReady.add(player.getUUID());
                           Vec3 side = player.getLookAngle().cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize().scale(level.random.nextInt(2) == 0 ? 0.6D : -0.6D);
                           dust(level, SILVER_D, player.position().add(side).add(0.0D, 1.0D, 0.0D), 14, 0.35D, 1.2F);
                           level.playSound((Player)null, player.blockPosition(), CastFxEvents.sound("minecraft:entity.player.attack.sweep"), SoundSource.PLAYERS, 0.8F, 1.7F);
                           bar(player, "§fThe body answered first. §7Next bolt is free.");
                           dodgeLook(player, level, event.getSource().getEntity(), side);
                        }
                     }

                  }
               }
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onTaken(LivingHurtEvent event) {
      LivingEntity attacker = event.getEntity();
      if (attacker instanceof ServerPlayer player) {
         if (player.getTags().contains("unrp.has.limit_breaker")) {
            Entity amount_0 = event.getSource().getEntity();
            if (amount_0 instanceof LivingEntity) {
               attacker = (LivingEntity)amount_0;
               if (attacker != player) {
                  float amount = event.getAmount();
                  bank(player, amount * 0.6F);
                  double frac = 1.0D - edge(player);
                  if (frac < 0.5D) {
                     if (amount >= 6.0F) {
                        xpEvent(player, 5);
                     }

                     float[] low = (float[])hurtLow.computeIfAbsent(player.getUUID(), (k) -> new float[]{0.0F, 0.0F});
                     low[0] += amount;
                     Level var8 = player.level();
                     if (var8 instanceof ServerLevel) {
                        ServerLevel level = (ServerLevel)var8;
                        low[1] = (float)level.getGameTime();
                     }

                     if (low[0] >= 40.0F && !player.getTags().contains("unrp.lb_feat_blue")) {
                        player.addTag("unrp.lb_feat_blue");
                        bar(player, "§bSomething gives way. §7You stood in the fire and didn't fall.");
                     }
                  }

                  return;
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onKill(LivingDeathEvent event) {
      Entity victim_0 = event.getSource().getEntity();
      if (victim_0 instanceof ServerPlayer attacker) {
         LivingEntity victim = event.getEntity();
         if (victim != attacker && attacker.getTags().contains("unrp.has.limit_breaker")) {
            if (meaningful(victim)) {
               xpEvent(attacker, victim instanceof Player ? 30 : 15);
               double frac = 1.0D - edge(attacker);
               if (frac < 0.3D && !attacker.getTags().contains("unrp.lb_feat_gold")) {
                  attacker.addTag("unrp.lb_feat_gold");
                  bar(attacker, "§6Something gives way. §7You won a fight you'd already lost.");
               }

               if (frac < 0.1D && (victim instanceof Player || victim.getMaxHealth() >= 20.0F) && !attacker.getTags().contains("unrp.lb_feat_silver")) {
                  attacker.addTag("unrp.lb_feat_silver");
                  bar(attacker, "§fSomething gives way. §7You had the last word, right at the edge.");
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
      Player has_0 = event.getEntity();
      if (has_0 instanceof ServerPlayer player) {
         if (becoming.remove(player.getUUID()) != null) {
            unbecome(player, (int[])null, false);
         }

         breakAnchor.remove(player.getUUID());
         waveCharge.remove(player.getUUID());
         Level var3 = player.level();
         if (var3 instanceof ServerLevel lvl) {
            orbDrop(player, lvl);
         }

         rushing.remove(player.getUUID());
         rushHit.remove(player.getUUID());
         hurtLow.remove(player.getUUID());
         aloft.remove(player.getUUID());

         for(String tag : FORM_TAGS) {
            player.removeTag(tag);
         }

         syncForm(player, 0);
         boolean has = player.getTags().contains("unrp.has.limit_breaker");
         com.myriam.unrp.ability.OriginGrowth.apply(player, "limit_breaker", has, -4.0D, -0.5D, 0.0D);
         formAttrs(player, 0, 0);
      }
   }

   @SubscribeEvent
   public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer joiner) {
         for(Map.Entry<UUID, Integer> entry : lastForm.entrySet()) {
            if (entry.getValue() > 0) {
               com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> joiner), new LbFormPacket((UUID)entry.getKey(), entry.getValue()));
            }
         }

      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      UUID id = event.getEntity().getUUID();
      Integer had = (Integer)lastForm.remove(id);
      if (had != null && had > 0) {
         Player leaving_0 = event.getEntity();
         if (leaving_0 instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)leaving_0;
            com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.ALL.noArg(), new LbFormPacket(id, 0));
         }
      }

      breakAnchor.remove(id);
      int[] was = (int[])becoming.remove(id);
      if (was != null) {
         Player lvl_0 = event.getEntity();
         if (lvl_0 instanceof ServerPlayer) {
            ServerPlayer gone = (ServerPlayer)lvl_0;
            unbecome(gone, was, true);
         }
      }

      waveCharge.remove(id);
      Player var6 = event.getEntity();
      if (var6 instanceof ServerPlayer leaving) {
         Level var11 = leaving.level();
         if (var11 instanceof ServerLevel lvl) {
            orbDrop(leaving, lvl);
         }
      }

      waveOrb.remove(id);
      rushing.remove(id);
      rushHit.remove(id);
      boltCd.remove(id);
      waveCd.remove(id);
      rushCd.remove(id);
      hurtLow.remove(id);
      counterReady.remove(id);
      aloft.remove(id);
   }

   public static void clearAll() {
      breakAnchor.clear();
      lastForm.clear();
      becoming.clear();
      waveCharge.clear();
      rushing.clear();
      rushHit.clear();
      boltCd.clear();
      waveCd.clear();
      rushCd.clear();
      hurtLow.clear();
      counterReady.clear();
      aloft.clear();
   }
}
