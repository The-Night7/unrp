package com.myriam.unrp.deal;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(
   modid = "unrp"
)
public class DealmakerEvents {
   public static final String HAS_TAG = "unrp.has.dealmaker";
   public static final String ACTIVE_TAG = "unrp.active.dealmaker";
   public static final String SUMMONS_TAG = "unrp.dm_summons";
   public static final String POWER_PRESS = "unrp.dm_power_press";
   public static final String FLIP_LEFT = "unrp.dm_flip_trigger";
   public static final String TOGGLE = "toggle";
   static final ResourceLocation POWER_ID = new ResourceLocation("unrp", "dealmaker");
   private static final String RITUAL_TAG = "unrp.dm_ritual";
   private static final String[] RANK_NAME = new String[]{"Lesser", "Middle", "Greater", "Arch"};
   private static final int GOLD = 15254378;
   private static final int SOULC = 5882600;
   private static final int INK_RED = 8000544;
   private static final int INK_SOUL = 3108510;
   private static final int WAX_RED = 9313580;
   private static final int WAX_SOUL = 3033740;
   public static final double SEAL_NEAR = 20.0D;
   public static final double SEAL_ASIDE = 1.3D;
   public static final double RIP_NEAR = 24.0D;
   public static final double THREAD_NEAR = 48.0D;
   public static final int RIP_FOLD = 30;
   public static final int IMBUE_THREAD = 6;
   public static final int IMBUE_STEPS = 8;
   public static final int IMBUE_ARRIVE = 24;
   public static final int HEARTS = 8;
   public static final double HEART_TURN = 0.12D;
   private static final double[] BIND_HEALTH = new double[]{0.25D, 0.5D, 1.0D, 1.5D};
   private static final double[] BIND_ARMOR = new double[]{10.0D, 15.0D, 20.0D, 35.0D};
   private static final double[] BIND_TOUGH = new double[]{5.0D, 6.0D, 12.0D, 18.0D};
   private static final double[] BIND_SPEED = new double[]{0.08D, 0.14D, 0.2D, 0.35D};
   private static final double[] BIND_STATS = new double[]{0.15D, 0.3D, 0.7D, 1.0D};
   private static final double[] IMBUE_SIZE = new double[]{1.25D, 1.5D, 2.0D, 2.5D};
   private static final double[] IMBUE_DMG = new double[]{1.3D, 1.6D, 2.0D, 2.5D};
   private static final double[] IMBUE_SPEED = new double[]{0.1D, 0.18D, 0.28D, 0.4D};
   private static final UUID HP_UUID = UUID.fromString("7374c6fc-0000-4a9d-ad61-000000000001");
   private static final UUID ARMOR_UUID = UUID.fromString("7374c6fc-0000-4a9d-ad61-000000000002");
   private static final UUID TOUGH_UUID = UUID.fromString("7374c6fc-0000-4a9d-ad61-000000000003");
   private static final UUID SPEED_UUID = UUID.fromString("7374c6fc-0000-4a9d-ad61-000000000004");
   private static final UUID ISPEED_UUID = UUID.fromString("7374c6fc-0000-4a9d-ad61-000000000005");
   private static final UUID SWREACH_UUID = UUID.fromString("7374c6fc-0000-4a9d-ad61-000000000006");
   private static final UUID AREACH_UUID = UUID.fromString("7374c6fc-0000-4a9d-ad61-000000000007");
   private static final Map<UUID, Integer> bindPick = new HashMap();
   private static final Map<UUID, Integer> protectPick = new HashMap();
   private static final Map<UUID, Integer> imbuePick = new HashMap();
   private static final Map<UUID, UUID> protectLink = new HashMap();
   private static final Map<UUID, long[]> pendingBind = new HashMap();
   private static final Map<UUID, UUID> pendingBinder = new HashMap();
   private static final Map<UUID, Integer> pendingRank = new HashMap();
   private static final Map<UUID, int[]> allyImbue = new HashMap();
   private static final Map<UUID, int[]> selfImbue = new HashMap();
   private static final Map<UUID, String> selfImbueCopy = new HashMap();
   private static final Map<UUID, UUID> borrowedFrom = new HashMap();
   private static final Map<UUID, Integer> selector = new HashMap();
   private static final Map<UUID, long[]> borrow = new HashMap();
   private static final Set<UUID> redirecting = new HashSet();
   private static final Set<UUID> chainKilling = new HashSet();
   private static final Map<UUID, UUID> remoteEdit = new HashMap();

   public static boolean chainKilled(UUID id) {
      return chainKilling.contains(id);
   }

   private DealmakerEvents() {
   }

   public static boolean imbued(UUID player) {
      return selfImbue.containsKey(player);
   }

   public static boolean isDealmaker(ServerPlayer player) {
      Set<String> tags = player.getTags();
      return tags.contains("unrp.has.dealmaker") && tags.contains("unrp.active.dealmaker") && !tags.contains("unrp.power_disabled");
   }

   public static int rank(ServerPlayer player) {
      return DealBridge.originLevel(player);
   }

   public static double reachFrac(ServerPlayer player) {
      return 1.0D - 1.0D / (1.0D + (double)rank(player) / 40.0D);
   }

   public static double capFor(ServerPlayer player, double min, double max) {
      return min + (max - min) * reachFrac(player);
   }

   private static int score(ServerPlayer player, String objective) {
      MinecraftServer server = player.getServer();
      if (server == null) {
         return 0;
      } else {
         Scoreboard sb = server.getScoreboard();
         Objective obj = sb.getObjective(objective);
         return obj == null ? 0 : sb.getOrCreatePlayerScore(player.getScoreboardName(), obj).getScore();
      }
   }

   private static void setScore(ServerPlayer player, String objective, int value) {
      MinecraftServer server = player.getServer();
      if (server != null) {
         Scoreboard sb = server.getScoreboard();
         Objective obj = sb.getObjective(objective);
         if (obj != null) {
            sb.getOrCreatePlayerScore(player.getScoreboardName(), obj).setScore(value);
         }
      }
   }

   private static void bar(ServerPlayer player, String msg) {
      player.displayClientMessage(Component.literal(msg), true);
   }

   private static boolean spend(ServerPlayer player, int cost) {
      int aura = score(player, "unrp.aura");
      if (aura < cost) {
         bar(player, "§7Not enough aura for the fee.");
         return false;
      } else {
         setScore(player, "unrp.aura", aura - cost);
         return true;
      }
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player tags_0 = event.player;
         if (tags_0 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)tags_0;
            Level var5 = player.level();
            if (var5 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var5;
               Set<String> tags = player.getTags();
               if (tags.contains("unrp.has.soul_bound")) {
                  tickSoulBound(player, level, tags);
               }

               if (!tags.contains("unrp.has.dealmaker")) {
                  return;
               }

               PactLedger ledger = PactLedger.get(level.getServer());
               if (player.tickCount % 100 == 0 && ledger.soulOwnerOf(player.getUUID()) == null) {
                  ledger.setSoulOwner(player.getUUID(), player.getUUID());
               }

               tags.remove("unrp.dm_summons");
               tags.remove("unrp.dm_flip_trigger");
               if (tags.remove("unrp.dm_power_press")) {
                  if (player.isShiftKeyDown() && tags.contains("unrp.active.dealmaker")) {
                     openLedger(player, level, ledger);
                  } else {
                     flipPower(player);
                  }
               }

               if (tags.remove("unrp.dm_a1_trigger")) {
                  doCreateContract(player, level, ledger);
               }

               if (tags.remove("unrp.dm_a2_trigger")) {
                  doSelector(player, level, ledger);
               }

               if (tags.remove("unrp.dm_a3_trigger")) {
                  doSoulSummon(player, level, ledger);
               }

               if (tags.remove("unrp.dm_a4_trigger")) {
                  doSoulRip(player, level, ledger);
               }

               if (tags.remove("unrp.dm_a6_trigger")) {
                  doBindAbility(player, level, ledger);
               }

               if (tags.remove("unrp.dm_a7_trigger")) {
                  doProtection(player, level, ledger);
               }

               if (tags.remove("unrp.dm_a8_trigger")) {
                  doAllyImbue(player, level, ledger);
               }

               if (tags.remove("unrp.dm_a9_trigger")) {
                  doSelfImbue(player, level, ledger);
               }

               if (protectLink.containsKey(player.getUUID()) && !isDealmaker(player)) {
                  protectLink.remove(player.getUUID());
                  bar(player, "§7The aegis link fades with your power.");
               }

               tickImbues(player, level);
               if (player.tickCount % 20 == 0 && isDealmaker(player)) {
                  sendHud(player, level, ledger);
                  tickProtectionUpkeep(player, ledger);
               }

               return;
            }
         }
      }

   }

   static boolean flipPower(ServerPlayer player) {
      com.myriam.unrp.ability.AbilityEngine.input(player, 0);
      return true;
   }

   private static void tickSoulBound(ServerPlayer player, ServerLevel level, Set<String> tags) {
      PactLedger ledger = PactLedger.get(level.getServer());
      if (tags.remove("unrp.sb1_trigger")) {
         doOwnSoulSummon(player, level, ledger);
      }

      if (tags.remove("unrp.sb2_trigger")) {
         doSanctuary(player, level, ledger);
      }

      if (tags.remove("unrp.sb3_trigger")) {
         doBorrow(player, level, ledger);
      }

      if (tags.remove("unrp.sb4_trigger")) {
         doSoulSword(player, level, ledger);
      }

      long[] b = (long[])borrow.get(player.getUUID());
      if (b != null && (long)LifeClock.now(player) > b[0]) {
         borrow.remove(player.getUUID());
         bar(player, "§7The borrowed power recedes.");
      }

      UUID master = ledger.dealmakerOf(player.getUUID());
      if (master != null && player.tickCount % 40 == 0) {
         int rank = ledger.rankOf(master, player.getUUID());
         applyBindPackage(player, effectiveRank(player, rank));
      }

   }

   private static int effectiveRank(ServerPlayer ally, int base) {
      int[] imbued = (int[])allyImbue.get(ally.getUUID());
      return imbued != null && LifeClock.now(ally) <= imbued[1] ? Math.min(3, base + 1) : base;
   }

   private static void applyBindPackage(ServerPlayer ally, int rank) {
      if (rank < 0) {
         stripBindPackage(ally);
      } else {
         pin(ally.getAttribute(Attributes.MAX_HEALTH), HP_UUID, "Pact vitality", BIND_HEALTH[rank], Operation.MULTIPLY_TOTAL);
         pin(ally.getAttribute(Attributes.ARMOR), ARMOR_UUID, "Pact plate", BIND_ARMOR[rank], Operation.ADDITION);
         pin(ally.getAttribute(Attributes.ARMOR_TOUGHNESS), TOUGH_UUID, "Pact temper", BIND_TOUGH[rank], Operation.ADDITION);
         pin(ally.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_UUID, "Pact stride", BIND_SPEED[rank], Operation.MULTIPLY_TOTAL);
         if (rank >= 2) {
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 90, rank >= 3 ? 4 : 2, false, false));
         }

         if (rank >= 3 && !ally.getAbilities().mayfly) {
            ally.getAbilities().mayfly = true;
            ally.onUpdateAbilities();
            ally.addTag("unrp.dm_wings");
         }

      }
   }

   public static void stripBindPackage(ServerPlayer ally) {
      unpin(ally.getAttribute(Attributes.MAX_HEALTH), HP_UUID);
      unpin(ally.getAttribute(Attributes.ARMOR), ARMOR_UUID);
      unpin(ally.getAttribute(Attributes.ARMOR_TOUGHNESS), TOUGH_UUID);
      unpin(ally.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_UUID);
      dropWings(ally);
   }

   private static void dropWings(ServerPlayer ally) {
      if (ally.getTags().contains("unrp.dm_wings")) {
         ally.removeTag("unrp.dm_wings");
         if (!ally.isCreative() && !ally.isSpectator()) {
            ally.getAbilities().mayfly = false;
            ally.getAbilities().flying = false;
            ally.onUpdateAbilities();
         }
      }

   }

   private static void pin(AttributeInstance attr, UUID id, String name, double value, AttributeModifier.Operation op) {
      if (attr != null) {
         AttributeModifier old = attr.getModifier(id);
         if (old == null || old.getAmount() != value) {
            if (old != null) {
               attr.removeModifier(id);
            }

            attr.addPermanentModifier(new AttributeModifier(id, name, value, op));
         }
      }
   }

   private static void unpin(AttributeInstance attr, UUID id) {
      if (attr != null && attr.getModifier(id) != null) {
         attr.removeModifier(id);
      }

   }

   private static ServerPlayer front(ServerPlayer player, double range) {
      Vec3 eye = player.getEyePosition();
      Vec3 look = player.getLookAngle();
      ServerPlayer best = null;
      double bestDot = 0.86D;

      for(ServerPlayer other : player.serverLevel().players()) {
         if (other != player && !other.isSpectator()) {
            Vec3 to = other.getEyePosition().subtract(eye);
            double dist = to.length();
            if (!(dist > range) && !(dist < 0.01D)) {
               double dot = to.normalize().dot(look);
               if (dot > bestDot) {
                  bestDot = dot;
                  best = other;
               }
            }
         }
      }

      return best;
   }

   private static void doBindAbility(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      if (player.isShiftKeyDown()) {
         int pick = (bindPick.getOrDefault(player.getUUID(), 0) + 1) % 3;
         bindPick.put(player.getUUID(), pick);
         bar(player, "§6Binding as: §f" + RANK_NAME[pick]);
      } else {
         ServerPlayer target = front(player, 6.0D);
         if (target == null) {
            bar(player, "§7There's no one in front of you.");
         } else {
            UUID owner = ledger.soulOwnerOf(target.getUUID());
            if (owner != null && owner.equals(player.getUUID())) {
               int rank = bindPick.getOrDefault(player.getUUID(), 0);
               int already = ledger.rankOf(player.getUUID(), target.getUUID());
               if (already >= 0) {
                  rearrange(player, level, ledger, target, already, rank);
               } else if (ledger.bindsOf(player.getUUID())[rank] != null) {
                  bar(player, "§7The " + RANK_NAME[rank] + " seat is taken. Sneak to cycle seats.");
               } else if (spend(player, 10)) {
                  if (!DealBridge.sgOutside(target) && target.getHealth() < target.getMaxHealth() * 0.25F) {
                     finishBind(player, level, ledger, target, rank, true);
                  } else {
                     pendingBind.put(target.getUUID(), new long[]{level.getGameTime() + 400L});
                     pendingBinder.put(target.getUUID(), player.getUUID());
                     pendingRank.put(target.getUUID(), rank);
                     com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target), new DialoguePacket("The Dealmaker", player.getName().getString() + " offers you a seat at their side as their " + RANK_NAME[rank] + " bound. Your strength grows; your life becomes theirs to spend. Do you accept the binding?", true, 5));
                     bar(player, "§7The offer is made. " + target.getName().getString() + " must accept.");
                  }
               }
            } else {
               bar(player, "§7You don't hold " + target.getName().getString() + "'s soul.");
            }
         }
      }
   }

   public static void onChoice(ServerPlayer target, boolean yes, int org) {
      if (org == 5) {
         Level window = target.level();
         if (window instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)window;
            long[] var8 = (long[])pendingBind.remove(target.getUUID());
            UUID binderId = (UUID)pendingBinder.remove(target.getUUID());
            Integer rank = (Integer)pendingRank.remove(target.getUUID());
            if (var8 != null && binderId != null && rank != null && level.getGameTime() <= var8[0]) {
               ServerPlayer binder = level.getServer().getPlayerList().getPlayer(binderId);
               if (binder == null) {
                  return;
               }

               if (!yes) {
                  bar(binder, "§7" + target.getName().getString() + " declined the binding.");
                  return;
               }

               finishBind(binder, level, PactLedger.get(level.getServer()), target, rank, false);
               return;
            }

            return;
         }
      }

   }

   private static void finishBind(ServerPlayer player, ServerLevel level, PactLedger ledger, ServerPlayer target, int rank, boolean forced) {
      ledger.setBind(player.getUUID(), rank, target.getUUID(), target.getName().getString());
      DealBridge.run(player.server, "superpower add unrp:soul_bound " + target.getName().getString());
      target.addTag("unrp.has.soul_bound");
      applyBindPackage(target, rank);
      Fx.ring(level, 15254378, target.position().add(0.0D, 0.1D, 0.0D), 1.6D, 14);
      Fx.line(level, Fx.glint(15254378), player.position().add(0.0D, 1.2D, 0.0D), target.position().add(0.0D, 1.2D, 0.0D), 0.6D, 0.15D, 0.02D);
      level.playSound((Player)null, target.blockPosition(), DealBridge.sound("minecraft:block.beacon.power_select"), SoundSource.PLAYERS, 1.3F, 0.6F);
      bar(player, "§6" + target.getName().getString() + "§7 is bound as your §f" + RANK_NAME[rank] + "§7.");
      bar(target, forced ? "§6Too weak to refuse. You're bound as the Dealmaker's " + RANK_NAME[rank] + "." : "§6You're bound as the Dealmaker's " + RANK_NAME[rank] + ".");
   }

   private static void rearrange(ServerPlayer player, ServerLevel level, PactLedger ledger, ServerPlayer target, int from, int to) {
      UUID[] seats = ledger.bindsOf(player.getUUID());
      String[] names = ledger.bindNamesOf(player.getUUID());
      UUID otherId = seats[to];
      String otherName = names[to];
      ledger.setBind(player.getUUID(), to, target.getUUID(), target.getName().getString());
      ledger.setBind(player.getUUID(), from, otherId, otherName);
      applyBindPackage(target, to);
      if (otherId != null) {
         ServerPlayer other = level.getServer().getPlayerList().getPlayer(otherId);
         if (other != null) {
            applyBindPackage(other, from);
         }
      }

      bar(player, "§7Seats rearranged: §f" + target.getName().getString() + "§7 now holds §f" + RANK_NAME[to] + "§7.");
   }

   private static void doProtection(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      UUID[] seats = ledger.bindsOf(player.getUUID());
      if (player.isShiftKeyDown()) {
         int pick_0 = (protectPick.getOrDefault(player.getUUID(), 0) + 1) % 3;
         protectPick.put(player.getUUID(), pick_0);
         bar(player, "§6Protection aimed at the §f" + RANK_NAME[pick_0] + "§6 seat§7" + (seats[pick_0] == null ? " (empty)" : ""));
      } else if (protectLink.remove(player.getUUID()) != null) {
         bar(player, "§7The aegis link is severed.");
      } else {
         int pick = protectPick.getOrDefault(player.getUUID(), 0);
         UUID ward = seats[pick];
         if (ward == null) {
            bar(player, "§7That seat is empty.");
         } else {
            protectLink.put(player.getUUID(), ward);
            bar(player, "§6Under your protection: §f" + ledger.bindNamesOf(player.getUUID())[pick] + "§7. Their wounds are yours now.");
            ServerPlayer wardP = level.getServer().getPlayerList().getPlayer(ward);
            if (wardP != null) {
               bar(wardP, "§6The Dealmaker takes your wounds upon themselves.");
            }

         }
      }
   }

   private static void tickProtectionUpkeep(ServerPlayer player, PactLedger ledger) {
      UUID ward = (UUID)protectLink.get(player.getUUID());
      if (ward != null) {
         int aura = score(player, "unrp.aura");
         if (aura <= 0) {
            protectLink.remove(player.getUUID());
            bar(player, "§7Out of aura. The aegis link breaks.");
         } else {
            setScore(player, "unrp.aura", aura - 1);
            ServerPlayer wardP = player.server.getPlayerList().getPlayer(ward);
            if (wardP != null) {
               Level var6 = wardP.level();
               if (var6 instanceof ServerLevel) {
                  ServerLevel wl = (ServerLevel)var6;
                  Fx.circle(wl, Fx.glint(15254378), wardP.position().add(0.0D, 2.4D, 0.0D), 0.35D, 6, 0.0D, 0.0D);
               }
            }

         }
      }
   }

   private static void doAllyImbue(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      UUID[] seats = ledger.bindsOf(player.getUUID());
      if (player.isShiftKeyDown()) {
         int pick_0 = (imbuePick.getOrDefault(player.getUUID(), 0) + 1) % 3;
         imbuePick.put(player.getUUID(), pick_0);
         bar(player, "§6Imbuement aimed at the §f" + RANK_NAME[pick_0] + "§6 seat§7" + (seats[pick_0] == null ? " (empty)" : ""));
      } else {
         int pick = imbuePick.getOrDefault(player.getUUID(), 0);
         UUID allyId = seats[pick];
         if (allyId == null) {
            bar(player, "§7That seat is empty.");
         } else {
            ServerPlayer ally = level.getServer().getPlayerList().getPlayer(allyId);
            if (ally == null) {
               bar(player, "§7They're out of reach right now.");
            } else if (spend(player, 20)) {
               int dur = Math.min(6000, 20 * (45 + rank(player) * 2));
               allyImbue.put(allyId, new int[]{pick, LifeClock.now(ally) + dur});
               int up = Math.min(3, pick + 1);
               applyBindPackage(ally, up);
               Fx.shell(level, Fx.flare(15254378), ally.position().add(0.0D, 1.0D, 0.0D), 1.4D, 40, 1.2D);
               level.playSound((Player)null, ally.blockPosition(), DealBridge.sound("minecraft:block.enchantment_table.use"), SoundSource.PLAYERS, 1.4F, 0.7F);
               bar(player, "§6" + ally.getName().getString() + "§7 rises to §f" + RANK_NAME[up] + "§7 for a time.");
               bar(ally, up == 3 ? "§6ARCH§7. Wings of the pact unfold from your back." : "§6The Dealmaker's favor lifts you to " + RANK_NAME[up] + ".");
            }
         }
      }
   }

   private static void doSelfImbue(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      UUID[] seats = ledger.bindsOf(player.getUUID());
      String[] names = ledger.bindNamesOf(player.getUUID());
      int low = -1;

      for(int i = 0; i < 3; ++i) {
         if (seats[i] != null && level.getServer().getPlayerList().getPlayer(seats[i]) != null) {
            low = i;
            break;
         }
      }

      if (low < 0) {
         bar(player, "§7No bound soul is in the world to spend.");
      } else {
         ServerPlayer ally = level.getServer().getPlayerList().getPlayer(seats[low]);
         long now = level.getGameTime();
         if (ledger.reviveCdOf(ally.getUUID()) > now) {
            bar(player, "§7" + names[low] + "'s soul is still ash. It can't be spent yet.");
         } else if (spend(player, 30)) {
            boolean spared = false;
            if (low == 2 && ledger.greaterCdOf(ally.getUUID()) <= now) {
               ledger.setGreaterCd(ally.getUUID(), now + 20L * (effectiveRank(ally, low) >= 3 ? 120L : 300L));
               Fx.shell(level, Fx.glint(15254378), ally.position().add(0.0D, 1.0D, 0.0D), 1.2D, 30, 1.0D);
               spared = true;
            }

            int dur = Math.min(4800, 20 * (30 + rank(player)));
            boolean arch = false;
            int[] existing = (int[])allyImbue.get(ally.getUUID());
            if (existing != null && LifeClock.now(ally) <= existing[1] && low == 2) {
               arch = true;
            }

            if (!spared) {
               chainKilling.add(ally.getUUID());
               ledger.setReviveCd(ally.getUUID(), now + 12000L);
               ally.hurt(level.damageSources().magic(), 1000000.0F);
               chainKilling.remove(ally.getUUID());
               level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§6" + names[low] + "§7's soul has been absorbed by The Dealmaker."), false);
            } else {
               ledger.setReviveCd(ally.getUUID(), now + 12000L);
               bar(ally, "§6The Dealmaker spends your standing revival instead of your life.");
            }

            int useRank = arch ? 3 : low;
            selfImbue.put(player.getUUID(), new int[]{useRank, LifeClock.now(player) + dur});
            player.setAbsorptionAmount(player.getAbsorptionAmount() + ally.getMaxHealth());
            DealBridge.run(player.server, "scale set pehkui:base " + IMBUE_SIZE[useRank] + " " + player.getName().getString());
            pin(player.getAttribute(Attributes.MOVEMENT_SPEED), ISPEED_UUID, "Imbued stride", IMBUE_SPEED[useRank], Operation.MULTIPLY_TOTAL);
            if (useRank >= 3) {
               AttributeInstance reach = player.getAttribute((Attribute)ForgeMod.ENTITY_REACH.get());
               pin(reach, AREACH_UUID, "Arch reach", 2.5D, Operation.ADDITION);
               if (!player.getAbilities().mayfly) {
                  player.getAbilities().mayfly = true;
                  player.onUpdateAbilities();
                  player.addTag("unrp.dm_wings");
               }
            }

            grantCopyOf(player, ally, dur);
            Fx.shell(level, Fx.flare(15254378), player.position().add(0.0D, 1.2D, 0.0D), 1.8D, 60, 1.6D);
            Fx.ring(level, 5882600, player.position().add(0.0D, 0.1D, 0.0D), 2.2D, 16);
            level.playSound((Player)null, player.blockPosition(), DealBridge.sound("minecraft:entity.evoker.cast_spell"), SoundSource.PLAYERS, 1.6F, 0.5F);
            bar(player, "§6" + RANK_NAME[useRank] + " imbuement.§7 Their strength is yours for a time.");
            imbueLook(level, player, ally, spared, dur, IMBUE_SIZE[useRank]);
         }
      }
   }

   private static void imbueLook(ServerLevel level, ServerPlayer keeper, ServerPlayer ally, boolean spared, int dur, double size) {
      ServerLevel allyLevel = ally.serverLevel();
      boolean near = allyLevel == level && ally.distanceToSqr(keeper) <= 2304.0D && !ally.isInvisible();
      borrowedFrom.put(keeper.getUUID(), ally.getUUID());
      DealBridge.look_borrowed(keeper, ally.getUUID(), normalPowerOf(ally), dur, 24);
      Vec3 from = ally.position().add(0.0D, 1.0D, 0.0D);
      if (!spared && !ally.isInvisible()) {
         allyLevel.sendParticles(Fx.soulflame(15254378), from.x, from.y, from.z, 24, 0.3D, 0.6D, 0.3D, 0.02D);
      }

      if (keeper.isInvisible()) {
         Spectacle.after(24, () -> {
            if (!keeper.isRemoved()) {
               keeper.serverLevel().playSound((Player)null, keeper.blockPosition(), DealBridge.sound("minecraft:block.bell.use"), SoundSource.PLAYERS, 1.6F, 0.5F);
            }

         });
      } else {
         DealBridge.look_imbue(keeper, ally, near, spared);

         for(int k = 0; k <= 8; ++k) {
            int step = k;
            Spectacle.after(6 + step * 2, () -> {
               if (!keeper.isRemoved() && !keeper.isInvisible()) {
                  ServerLevel kl_0 = keeper.serverLevel();
                  Vec3 to = keeper.position().add(0.0D, (double)keeper.getBbHeight() * 0.6D, 0.0D);
                  if (near && kl_0 == allyLevel) {
                     Vec3 at = from.lerp(to, (double)step / 8.0D);
                     kl_0.sendParticles(Fx.soulflame(15254378), at.x, at.y, at.z, 3, 0.08D, 0.08D, 0.08D, 0.01D);
                  } else if (step >= 6) {
                     Fx.shell(kl_0, Fx.soulflame(15254378), to, 1.4D, 8, -0.08D);
                  }

               }
            });
         }

         Spectacle.after(24, () -> {
            if (!keeper.isRemoved()) {
               ServerLevel kl = keeper.serverLevel();
               Vec3 c = keeper.position().add(0.0D, (double)keeper.getBbHeight() * 0.55D, 0.0D);
               double r = 0.9D * size;

               for(int i = 0; i < 8 && !keeper.isInvisible(); ++i) {
                  kl.sendParticles(Fx.heart(15254378), c.x, c.y, c.z, 0, r, (Math.PI * 2D) * (double)i / 8.0D, 0.12D, 1.0D);
               }

               kl.playSound((Player)null, keeper.blockPosition(), DealBridge.sound("minecraft:block.bell.use"), SoundSource.PLAYERS, 1.6F, 0.5F);
            }
         });
      }
   }

   private static void grantCopyOf(ServerPlayer user, ServerPlayer source, int dur) {
      String power = normalPowerOf(source);
      if (power != null) {
         String cmdAdd = "superpower add unrp:" + power + "_am " + user.getName().getString();
         DealBridge.run(user.server, cmdAdd);
         DealBridge.lend(user, power);
         selfImbueCopy.put(user.getUUID(), power);
      }
   }

   public static String normalPowerOf(ServerPlayer source) {
      for(String tag : source.getTags()) {
         if (tag.startsWith("unrp.has.")) {
            String shortName = tag.substring("unrp.has.".length());
            if (!DealBridge.systemPower(shortName) && !DealBridge.isOriginPower(shortName) && !shortName.equals("dealmaker") && !shortName.equals("legacy")) {
               return shortName;
            }
         }
      }

      return null;
   }

   public static void dropCopy(ServerPlayer user) {
      String power = (String)selfImbueCopy.remove(user.getUUID());
      if (power != null) {
         DealBridge.run(user.server, "superpower remove unrp:" + power + "_am " + user.getName().getString());
      }
   }

   private static void tickImbues(ServerPlayer player, ServerLevel level) {
      int[] self = (int[])selfImbue.get(player.getUUID());
      if (self != null && LifeClock.now(player) > self[1]) {
         selfImbue.remove(player.getUUID());
         DealBridge.run(player.server, "scale set pehkui:base " + PactEnforcement.sizeOf(player.server, player.getUUID()) + " " + player.getName().getString());
         unpin(player.getAttribute(Attributes.MOVEMENT_SPEED), ISPEED_UUID);
         unpin(player.getAttribute((Attribute)ForgeMod.ENTITY_REACH.get()), AREACH_UUID);
         dropWings(player);
         dropCopy(player);
         bar(player, "§7The borrowed soul burns out.");
         borrowedFrom.remove(player.getUUID());
         DealBridge.look_borrowed(player, (UUID)null, "", 0, 0);
      }

      if (self != null && LifeClock.now(player) <= self[1] && player.tickCount % 100 == 0) {
         UUID from = (UUID)borrowedFrom.get(player.getUUID());
         if (from != null) {
            DealBridge.look_borrowed(player, from, (String)selfImbueCopy.get(player.getUUID()), self[1] - LifeClock.now(player), 0);
         }
      }

      int[] ally = (int[])allyImbue.get(player.getUUID());
      if (ally != null && LifeClock.now(player) > ally[1]) {
         allyImbue.remove(player.getUUID());
         PactLedger ledger = PactLedger.get(level.getServer());
         UUID master = ledger.dealmakerOf(player.getUUID());
         int base = master == null ? -1 : ledger.rankOf(master, player.getUUID());
         applyBindPackage(player, base);
         if (base < 3) {
            dropWings(player);
         }

         bar(player, "§7The imbuement fades. You return to your seat.");
      }

   }

   private static void doCreateContract(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
         ItemStack held = player.getInventory().getItem(i);
         if (held.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && !ContractItem.isCopy(held)) {
            UUID pactId = ContractItem.pactOf(held);
            PactLedger.Pact pact_0 = ledger.pact(pactId);
            if (pact_0 != null && !pact_0.signed && pact_0.owner.equals(player.getUUID())) {
               ledger.remove(pactId);
               held.shrink(held.getCount());
               bar(player, "§7The unsigned contract is voided. The paper forgets.");
               return;
            }
         }
      }

      if (spend(player, 5)) {
         PactLedger.Pact pact = ledger.create(player.getUUID(), player.getName().getString());
         ItemStack stack = ContractItem.forPact((Item)com.myriam.unrp.item.ModItems.CONTRACT.get(), pact);
         if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
         }

         Fx.burst(level, Fx.glint(15254378), player.position().add(0.0D, 1.2D, 0.0D), 12, 0.4D, 0.04D);
         level.playSound((Player)null, player.blockPosition(), DealBridge.sound("minecraft:item.book.put"), SoundSource.PLAYERS, 1.2F, 1.1F);
         bar(player, "§6A blank contract. §7Hold it and use it to write your terms.");
      }
   }

   private static void doSelector(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      List<UUID> owned = ledger.soulsOwnedBy(player.getUUID());
      owned.remove(player.getUUID());
      if (owned.isEmpty()) {
         bar(player, "§7You hold no souls but your own.");
      } else if (player.isShiftKeyDown()) {
         selector.put(player.getUUID(), -1);
         bar(player, "§6Selected: §fALL §7(" + owned.size() + " souls)");
      } else {
         int idx = selector.getOrDefault(player.getUUID(), -1) + 1;
         if (idx >= owned.size()) {
            idx = 0;
         }

         selector.put(player.getUUID(), idx);
         bar(player, "§6Selected soul: §f" + nameOf(level, (UUID)owned.get(idx)) + " §8(" + (idx + 1) + "/" + owned.size() + ")");
      }
   }

   private static String nameOf(ServerLevel level, UUID id) {
      ServerPlayer online = level.getServer().getPlayerList().getPlayer(id);
      if (online != null) {
         return online.getName().getString();
      } else {
         Optional<GameProfile> cached = level.getServer().getProfileCache() == null ? Optional.empty() : level.getServer().getProfileCache().get(id);
         return (String)cached.map(GameProfile::getName).orElse("someone");
      }
   }

   private static List<UUID> selectedSouls(ServerPlayer player, PactLedger ledger) {
      List<UUID> owned = ledger.soulsOwnedBy(player.getUUID());
      owned.remove(player.getUUID());
      List<UUID> out = new ArrayList();
      if (owned.isEmpty()) {
         return out;
      } else {
         int idx = selector.getOrDefault(player.getUUID(), -1);
         if (idx < 0) {
            out.addAll(owned);
         } else if (idx < owned.size()) {
            out.add((UUID)owned.get(idx));
         } else {
            out.add((UUID)owned.get(0));
         }

         return out;
      }
   }

   private static void doSoulSummon(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      List<UUID> picks = selectedSouls(player, ledger);
      if (picks.isEmpty()) {
         bar(player, "§7No soul selected. Cycle the Selector first.");
      } else if (player.isShiftKeyDown()) {
         ServerPlayer target_0 = level.getServer().getPlayerList().getPlayer((UUID)picks.get(0));
         if (target_0 == null) {
            bar(player, "§7They're not in the world right now.");
         } else if (spend(player, 15)) {
            Fx.burst(level, Fx.ember(5882600), player.position().add(0.0D, 1.0D, 0.0D), 20, 0.5D, 0.05D);
            player.teleportTo(target_0.serverLevel(), target_0.getX(), target_0.getY(), target_0.getZ(), player.getYRot(), player.getXRot());
            level.playSound((Player)null, target_0.blockPosition(), DealBridge.sound("minecraft:entity.enderman.teleport"), SoundSource.PLAYERS, 1.2F, 0.6F);
            bar(player, "§6You cross the distance to " + target_0.getName().getString() + ".");
         }
      } else {
         boolean anyone = false;

         for(UUID id_0 : picks) {
            if (level.getServer().getPlayerList().getPlayer(id_0) != null) {
               anyone = true;
            }
         }

         if (!anyone) {
            bar(player, "§7No owned soul is in the world right now.");
         } else if (spend(player, 15)) {
            int pulled = 0;

            for(UUID id : picks) {
               ServerPlayer target = level.getServer().getPlayerList().getPlayer(id);
               if (target != null) {
                  ServerLevel from = target.serverLevel();
                  Fx.burst(from, Fx.ember(5882600), target.position().add(0.0D, 1.0D, 0.0D), 20, 0.5D, 0.05D);
                  target.teleportTo(player.serverLevel(), player.getX(), player.getY(), player.getZ(), target.getYRot(), target.getXRot());
                  bar(target, "§6The pact pulls. You stand before the Dealmaker.");
                  ++pulled;
               }
            }

            if (pulled == 0) {
               bar(player, "§7No owned soul is in the world right now.");
            } else {
               level.playSound((Player)null, player.blockPosition(), DealBridge.sound("minecraft:entity.enderman.teleport"), SoundSource.PLAYERS, 1.4F, 0.5F);
               bar(player, "§6" + pulled + " summoned across the world to you.");
            }
         }
      }
   }

   private static void doSoulRip(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      ItemStack main = player.getMainHandItem();
      if (main.getItem() == com.myriam.unrp.item.ModItems.SOUL.get()) {
         UUID target_0 = SoulItem.targetOf(main);
         if (player.isShiftKeyDown() && target_0 != null && !target_0.equals(player.getUUID())) {
            if (spend(player, 40)) {
               ServerPlayer victim_0 = level.getServer().getPlayerList().getPlayer(target_0);
               String name_0 = SoulItem.nameOf(main);
               main.shrink(main.getCount());
               ledger.bumpSerial(target_0);
               if (victim_0 != null) {
                  chainKilling.add(victim_0.getUUID());
                  victim_0.hurt(level.damageSources().magic(), 1000000.0F);
                  chainKilling.remove(victim_0.getUUID());
               }

               level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§6" + name_0 + "§7's soul was ripped to shreds by The Dealmaker."), false);
               Fx.burst(level, Fx.flare(5882600), player.position().add(0.0D, 1.2D, 0.0D), 40, 0.8D, 0.1D);
               level.playSound((Player)null, player.blockPosition(), DealBridge.sound("minecraft:entity.warden.sonic_boom"), SoundSource.PLAYERS, 1.5F, 1.4F);
            }
         } else {
            String name = SoulItem.nameOf(main);
            main.shrink(main.getCount());
            bar(player, "§7" + name + "'s soul goes back to them.");
         }
      } else if (main.hasTag() && main.getTag().contains("dm_soulbound_target")) {
         String boundName = main.getTag().getString("dm_soulbound_name");
         main.shrink(main.getCount());
         bar(player, "§7The bound thing returns to " + boundName + "'s keeping.");
      } else {
         List<UUID> picks = selectedSouls(player, ledger);
         UUID target = picks.isEmpty() ? player.getUUID() : (UUID)picks.get(0);
         if (spend(player, 10)) {
            int serial = ledger.bumpSerial(target);
            ItemStack soul = manifestSoul(ledger, target, nameOf(level, target), serial);
            if (!player.getInventory().add(soul)) {
               player.drop(soul, false);
            }

            Fx.burst(level, Fx.ember(5882600), player.position().add(0.0D, 1.2D, 0.0D), 24, 0.5D, 0.06D);
            level.playSound((Player)null, player.blockPosition(), DealBridge.sound("minecraft:particle.soul_escape"), SoundSource.PLAYERS, 1.6F, 0.8F);
            ripLook(level, player, target);
            ServerPlayer victim = level.getServer().getPlayerList().getPlayer(target);
            if (victim != null && victim != player) {
               bar(victim, "§3Something of yours is in another's hand.");
            }

         }
      }
   }

   private static void ripLook(ServerLevel level, ServerPlayer keeper, UUID target) {
      if (!keeper.isInvisible()) {
         Vec3 hand = handOf(keeper);
         ServerPlayer victim = level.getServer().getPlayerList().getPlayer(target);
         boolean near = victim != null && victim != keeper && victim.level() == level && !victim.isInvisible() && victim.distanceToSqr(keeper) <= 576.0D;
         if (!near) {
            level.sendParticles(Fx.soulflame(5882600), hand.x, hand.y, hand.z, 6, 0.06D, 0.08D, 0.06D, 0.01D);
         } else {
            DealBridge.look_rip(level, keeper, victim, hand);
            Spectacle.after(30, () -> {
               if (!keeper.isRemoved() && keeper.level() == level && !keeper.isInvisible()) {
                  Vec3 h = handOf(keeper);
                  level.sendParticles(Fx.soulflame(5882600), h.x, h.y, h.z, 6, 0.06D, 0.08D, 0.06D, 0.01D);
               }
            });
         }
      }
   }

   private static Vec3 handOf(ServerPlayer p) {
      double yaw = Math.toRadians((double)p.yBodyRot);
      double side = p.getMainArm() == HumanoidArm.LEFT ? -1.0D : 1.0D;
      double rx = -Math.cos(yaw) * side;
      double rz = -Math.sin(yaw) * side;
      double fx = -Math.sin(yaw);
      double fz = Math.cos(yaw);
      double s = (double)p.getBbHeight() / 1.8D;
      return p.position().add((rx * 0.36D + fx * 0.25D) * s, 0.85D * s, (rz * 0.36D + fz * 0.25D) * s);
   }

   private static void doOwnSoulSummon(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      ItemStack main = player.getMainHandItem();
      if ((main.getItem() != com.myriam.unrp.item.ModItems.SOUL.get() || !player.getUUID().equals(SoulItem.targetOf(main))) && !boundOf(main, player.getUUID())) {
         UUID master = ledger.dealmakerOf(player.getUUID());
         if (master != null) {
            int serial = ledger.bumpSerial(player.getUUID());
            ItemStack soul = manifestSoul(ledger, player.getUUID(), player.getName().getString(), serial);
            if (!player.getInventory().add(soul)) {
               player.drop(soul, false);
            }

            bar(player, "§6Your own soul, in your own hands. The Dealmaker still holds the deed.");
         }
      } else {
         main.shrink(main.getCount());
         bar(player, "§7Your soul settles back where it belongs.");
      }
   }

   private static boolean boundOf(ItemStack stack, UUID target) {
      if (stack.hasTag() && stack.getTag().contains("dm_soulbound_target")) {
         try {
            return target.equals(UUID.fromString(stack.getTag().getString("dm_soulbound_target")));
         } catch (IllegalArgumentException var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static ItemStack manifestSoul(PactLedger ledger, UUID target, String name, int serial) {
      CompoundTag bound = ledger.boundItemOf(target);
      if (bound != null) {
         ItemStack stack = ItemStack.of(bound);
         if (!stack.isEmpty()) {
            stack.getOrCreateTag().putInt("dm_soulbound_serial", serial);
            return stack;
         }
      }

      return SoulItem.forTarget((Item)com.myriam.unrp.item.ModItems.SOUL.get(), target, name, serial);
   }

   private static void doSanctuary(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      UUID master = ledger.dealmakerOf(player.getUUID());
      if (master != null) {
         SoulPlaneEvents.sanctuary(player, master);
      }
   }

   private static void doBorrow(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      UUID master = ledger.dealmakerOf(player.getUUID());
      if (master != null) {
         int rank = ledger.rankOf(master, player.getUUID());
         if (rank < 1) {
            bar(player, "§7Only the Middle seat and above may borrow.");
         } else {
            ServerPlayer dealmaker = level.getServer().getPlayerList().getPlayer(master);
            if (dealmaker == null) {
               bar(player, "§7The Dealmaker is beyond reach.");
            } else {
               int lend = Math.max(1, rank(dealmaker) / 10);
               borrow.put(player.getUUID(), new long[]{(long)LifeClock.now(player) + 1200L, (long)lend});
               Fx.line(level, Fx.glint(15254378), dealmaker.position().add(0.0D, 1.4D, 0.0D), player.position().add(0.0D, 1.4D, 0.0D), 0.7D, 0.2D, 0.02D);
               bar(player, "§6Borrowing §f" + lend + "§6 levels of the Dealmaker's power§7 for a minute.");
            }
         }
      }
   }

   private static void doSoulSword(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      UUID master = ledger.dealmakerOf(player.getUUID());
      if (master != null) {
         int rank = ledger.rankOf(master, player.getUUID());
         if (rank < 2) {
            bar(player, "§7Only the Greater seat may draw the Soul Sword.");
         } else {
            for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
               ItemStack held = player.getInventory().getItem(i);
               if (held.getItem() == com.myriam.unrp.item.ModItems.SOUL_SWORD.get()) {
                  ledger.setSwordTag(player.getUUID(), held.getTag() == null ? null : held.getTag().copy());
                  held.shrink(held.getCount());
                  bar(player, "§7The Soul Sword sheathes into the pact.");
                  return;
               }
            }

            ServerPlayer dealmaker = level.getServer().getPlayerList().getPlayer(master);
            int dmRank = dealmaker == null ? 10 : rank(dealmaker);
            ItemStack sword = SoulSwordItem.forAlly((SoulSwordItem)com.myriam.unrp.item.ModItems.SOUL_SWORD.get(), player.getUUID(), dmRank);
            CompoundTag kept = ledger.swordTag(player.getUUID());
            if (kept != null && kept.contains("Enchantments", 9)) {
               ListTag ench = kept.getList("Enchantments", 10);
               sword.getOrCreateTag().put("Enchantments", ench.copy());
            }

            if (!player.getInventory().add(sword)) {
               bar(player, "§7No room in your hands for the blade.");
            } else {
               Fx.burst(level, Fx.glint(5882600), player.position().add(0.0D, 1.2D, 0.0D), 18, 0.4D, 0.05D);
               level.playSound((Player)null, player.blockPosition(), DealBridge.sound("minecraft:item.trident.return"), SoundSource.PLAYERS, 1.2F, 0.7F);
               bar(player, "§bThe Soul Sword answers, edged with the Dealmaker's rank.");
            }
         }
      }
   }

   public static void swordDissolved(ServerPlayer holder, ItemStack stack) {
      UUID ally = SoulSwordItem.allyOf(stack);
      if (ally != null && holder.getServer() != null) {
         PactLedger.get(holder.getServer()).setSwordTag(ally, stack.getTag() == null ? null : stack.getTag().copy());
      }

   }

   public static void tickSwordReach(ServerPlayer player, ItemStack stack, boolean selected) {
      AttributeInstance reach = player.getAttribute((Attribute)ForgeMod.ENTITY_REACH.get());
      if (reach != null) {
         double bonus = stack.hasTag() ? stack.getTag().getDouble("unrp_sword_reach") : 0.0D;
         boolean holding = selected && player.getMainHandItem() == stack;
         AttributeModifier old = reach.getModifier(SWREACH_UUID);
         if (holding && bonus > 0.0D) {
            if (old == null || old.getAmount() != bonus) {
               if (old != null) {
                  reach.removeModifier(SWREACH_UUID);
               }

               reach.addTransientModifier(new AttributeModifier(SWREACH_UUID, "Soul sword reach", bonus, Operation.ADDITION));
            }
         } else if (old != null) {
            reach.removeModifier(SWREACH_UUID);
         }

      }
   }

   public static void openLedger(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      List<LedgerViewPacket.Entry> entries = new ArrayList();

      for(PactLedger.Pact pact : ledger.pactsOwnedBy(player.getUUID())) {
         if (entries.size() >= 100) {
            break;
         }

         if (!pact.proposed || pact.submitted) {
            String label = pact.proposed ? "PETITION from " + pact.signerName : (pact.signed ? "Signed by " + pact.signerName : "Unsigned draft");
            String where = friendlyWhere(player, ledger, pact);
            List<String> lines = new ArrayList();

            for(PactLedger.Term t : pact.terms) {
               lines.add((t.locked ? "§6[theirs] §7" : "") + PactEnforcement.describe(t));
            }

            LedgerViewPacket.Entry entry = new LedgerViewPacket.Entry(pact.id, label, pact.soul, pact.signed, pact.ritual, where, lines);
            entry.proposed = pact.proposed;
            entries.add(entry);
         }
      }

      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new LedgerViewPacket(entries));
      level.playSound((Player)null, player.blockPosition(), DealBridge.sound("minecraft:item.book.put"), SoundSource.PLAYERS, 1.0F, 0.8F);
   }

   private static String friendlyWhere(ServerPlayer player, PactLedger ledger, PactLedger.Pact pact) {
      if (carriesPact(player, pact.id)) {
         return "In your keeping.";
      } else {
         String raw = ledger.locationOf(pact.id);
         String[] parts = raw.split("\\|");
         if (parts.length < 5) {
            return "Whereabouts unknown.";
         } else {
            String dim = parts[0].contains(":") ? parts[0].substring(parts[0].indexOf(58) + 1) : parts[0];
            return parts[4] + " at " + parts[1] + " " + parts[2] + " " + parts[3] + " (" + dim.replace('_', ' ') + ")";
         }
      }
   }

   private static boolean carriesPact(ServerPlayer player, UUID pactId) {
      for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
         ItemStack s = player.getInventory().getItem(i);
         if (s.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && !ContractItem.isCopy(s) && pactId.equals(ContractItem.pactOf(s))) {
            return true;
         }
      }

      return false;
   }

   public static void onLedgerAction(ServerPlayer player, LedgerActionPacket msg) {
      Level ledger_0 = player.level();
      if (ledger_0 instanceof ServerLevel level) {
         if (isDealmaker(player)) {
            PactLedger ledger = PactLedger.get(level.getServer());
            if (msg.op == 1) {
               openLedger(player, level, ledger);
               return;
            }

            if (msg.op == 0) {
               PactLedger.Pact pact_0 = ledger.pact(msg.pactId);
               if (pact_0 == null || !pact_0.owner.equals(player.getUUID()) || pact_0.proposed && !pact_0.submitted) {
                  return;
               }

               summonContract(player, level, ledger, pact_0);
               openLedger(player, level, ledger);
            }

            if (msg.op == 2) {
               PactLedger.Pact pact = ledger.pact(msg.pactId);
               if (pact == null || !pact.owner.equals(player.getUUID()) || !pact.proposed || !pact.submitted || pact.signed) {
                  return;
               }

               remoteEdit.put(player.getUUID(), pact.id);
               sendView(player, level, pact, 0);
            }

            return;
         }
      }

   }

   private static void summonContract(ServerPlayer player, ServerLevel level, PactLedger ledger, PactLedger.Pact pact) {
      if (carriesPact(player, pact.id)) {
         bar(player, "§7That page is already in your keeping.");
      } else {
         ItemStack found = pullFromPlayers(level, ledger, pact);
         if (found == null) {
            found = pullFromWorld(level.getServer(), ledger, pact);
         }

         if (found == null) {
            bar(player, "§7The Ledger can't reach that page right now. Its keeper isn't in the world.");
         } else {
            ContractItem.syncFromLedger(found, pact);
            if (player.getMainHandItem().isEmpty()) {
               player.setItemInHand(InteractionHand.MAIN_HAND, found);
            } else if (!player.getInventory().add(found)) {
               player.drop(found, false);
            }

            ledger.setLocation(pact.id, String.valueOf(player.level().dimension().location()) + "|" + player.getBlockX() + "|" + player.getBlockY() + "|" + player.getBlockZ() + "|Carried by " + player.getName().getString());
            Fx.ring(level, 15254378, player.position().add(0.0D, 0.1D, 0.0D), 1.4D, 12);
            level.playSound((Player)null, player.blockPosition(), DealBridge.sound("minecraft:item.book.page_turn"), SoundSource.PLAYERS, 1.3F, 1.1F);
            bar(player, "§6The Ledger delivers.§7 The contract is in your keeping.");
         }
      }
   }

   private static ItemStack pullFromPlayers(ServerLevel level, PactLedger ledger, PactLedger.Pact pact) {
      for(ServerPlayer holder : level.getServer().getPlayerList().getPlayers()) {
         for(int i = 0; i < holder.getInventory().getContainerSize(); ++i) {
            ItemStack s = holder.getInventory().getItem(i);
            if (s.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && !ContractItem.isCopy(s) && pact.id.equals(ContractItem.pactOf(s))) {
               ItemStack taken = s.copy();
               holder.getInventory().setItem(i, ItemStack.EMPTY);
               bar(holder, "§7The contract slips out of your hands, summoned away.");
               return taken;
            }
         }
      }

      return null;
   }

   private static ItemStack pullFromWorld(MinecraftServer server, PactLedger ledger, PactLedger.Pact pact) {
      String raw = ledger.locationOf(pact.id);
      String[] parts = raw.split("\\|");
      if (parts.length < 5) {
         return null;
      } else {
         ServerLevel where = null;

         for(ServerLevel candidate : server.getAllLevels()) {
            if (candidate.dimension().location().toString().equals(parts[0])) {
               where = candidate;
               break;
            }
         }

         if (where == null) {
            return null;
         } else {
            int z;
            int x;
            try {
               x = Integer.parseInt(parts[1]);
               int y = Integer.parseInt(parts[2]);
               z = Integer.parseInt(parts[3]);
            } catch (NumberFormatException var19) {
               return null;
            }

            where.getChunk(x >> 4, z >> 4);
            AABB box = new AABB((double)(x - 24), (double)(where.getMinBuildHeight() - 64), (double)(z - 24), (double)(x + 24), (double)where.getMaxBuildHeight(), (double)(z + 24));

            for(ItemEntity drop : where.getEntitiesOfClass(ItemEntity.class, box)) {
               ItemStack s_0 = drop.getItem();
               if (s_0.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && !ContractItem.isCopy(s_0) && pact.id.equals(ContractItem.pactOf(s_0))) {
                  ItemStack taken_0 = s_0.copy();
                  drop.discard();
                  return taken_0;
               }
            }

            for(ItemFrame frame : where.getEntitiesOfClass(ItemFrame.class, box)) {
               ItemStack s = frame.getItem();
               if (s.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && !ContractItem.isCopy(s) && pact.id.equals(ContractItem.pactOf(s))) {
                  ItemStack taken = s.copy();
                  frame.setItem(ItemStack.EMPTY);
                  return taken;
               }
            }

            for(int cx = (x >> 4) - 1; cx <= (x >> 4) + 1; ++cx) {
               for(int cz = (z >> 4) - 1; cz <= (z >> 4) + 1; ++cz) {
                  LevelChunk chunk = where.getChunk(cx, cz);

                  for(BlockEntity be : chunk.getBlockEntities().values()) {
                     if (be instanceof Container) {
                        Container container = (Container)be;

                        for(int i = 0; i < container.getContainerSize(); ++i) {
                           ItemStack s = container.getItem(i);
                           if (s.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && !ContractItem.isCopy(s) && pact.id.equals(ContractItem.pactOf(s))) {
                              ItemStack taken = s.copy();
                              container.setItem(i, ItemStack.EMPTY);
                              container.setChanged();
                              return taken;
                           }
                        }
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   public static UUID resolveDealmaker(MinecraftServer server) {
      for(ServerPlayer p : server.getPlayerList().getPlayers()) {
         if (p.getTags().contains("unrp.has.dealmaker")) {
            return p.getUUID();
         }
      }

      for(Map.Entry<String, Integer> e : DealBridge.seats().entrySet()) {
         if (e.getValue() == 7) {
            try {
               return UUID.fromString((String)e.getKey());
            } catch (IllegalArgumentException var4) {
            }
         }
      }

      return null;
   }

   public static void openContract(ServerPlayer player, ServerLevel level, ItemStack stack, InteractionHand hand) {
      PactLedger ledger = PactLedger.get(level.getServer());
      if (ContractItem.isCopy(stack)) {
         sendCopyView(player, stack);
      } else {
         UUID pactId = ContractItem.pactOf(stack);
         if (pactId == null && ContractItem.isProposed(stack)) {
            UUID dealmaker = resolveDealmaker(level.getServer());
            if (dealmaker == null) {
               bar(player, "§7No Dealmaker walks this world. The page stays blank.");
            } else if (dealmaker.equals(player.getUUID())) {
               bar(player, "§7You ARE the Dealmaker. Conjure a true contract instead.");
            } else {
               String ownerName = "The Dealmaker";
               ServerPlayer dm = level.getServer().getPlayerList().getPlayer(dealmaker);
               if (dm != null) {
                  ownerName = dm.getName().getString();
               }

               PactLedger.Pact fresh = ledger.create(dealmaker, ownerName);
               fresh.proposed = true;
               fresh.signer = player.getUUID();
               fresh.signerName = player.getName().getString();
               ledger.setDirty();
               ItemStack page = stack;
               if (stack.getCount() > 1) {
                  page = stack.copy();
                  page.setCount(1);
                  stack.shrink(1);
                  if (!player.getInventory().add(page)) {
                     player.drop(page, false);
                  }
               }

               page.getOrCreateTag().putString("unrp_pact", fresh.id.toString());
               bar(player, "§6A petition takes shape.§7 Write what you ask for.");
               sendView(player, level, fresh, 4);
            }
         } else {
            PactLedger.Pact pact = ledger.pact(pactId);
            if (pact == null) {
               stack.shrink(stack.getCount());
               bar(player, "§7This contract's pact is gone. The paper crumbles.");
            } else {
               int mode;
               if (pact.proposed && !pact.signed) {
                  if (pact.owner.equals(player.getUUID())) {
                     mode = pact.submitted ? 0 : 3;
                  } else if (player.getUUID().equals(pact.signer)) {
                     mode = pact.submitted ? 3 : 4;
                  } else {
                     mode = 3;
                  }
               } else if (pact.owner.equals(player.getUUID())) {
                  mode = pact.signed ? 2 : 0;
               } else {
                  mode = pact.signed ? 3 : 1;
               }

               sendView(player, level, pact, mode);
            }
         }
      }
   }

   private static void sendView(ServerPlayer player, ServerLevel level, PactLedger.Pact pact, int mode) {
      List<String> players = new ArrayList();

      for(ServerPlayer p_0 : level.getServer().getPlayerList().getPlayers()) {
         players.add(p_0.getName().getString());
      }

      PactViewPacket msg = new PactViewPacket(mode, pact.id, pact.ownerName, pact.signerName, pact.signed, pact.soul, reachFrac(player), pact.terms, players);
      msg.proposed = pact.proposed;
      msg.submitted = pact.submitted;
      if (mode == 0 || mode == 4) {
         for(String p : DealBridge.grantablePowers(level.getServer())) {
            msg.powers.add(p);
         }

         for(Map.Entry<String, List<DealBridge.MoveInfo>> entry : DealBridge.table(level.getServer()).entrySet()) {
            if (!DealBridge.isOriginPower((String)entry.getKey()) && !((String)entry.getKey()).endsWith("_am") && !((String)entry.getKey()).startsWith("boon_") && !((String)entry.getKey()).equals("soul_bound")) {
               for(DealBridge.MoveInfo m : entry.getValue()) {
                  if (m.slot >= 0) {
                     msg.moves.add((String)entry.getKey() + ":" + m.slot + "|" + m.title);
                  }
               }
            }
         }
      }

      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
   }

   private static void sendCopyView(ServerPlayer player, ItemStack stack) {
      List<PactLedger.Term> terms = new ArrayList();
      String owner = "";
      String signer = "";
      if (stack.hasTag()) {
         owner = stack.getTag().getString("dm_copy_owner");
         signer = stack.getTag().getString("dm_copy_signer");
         ListTag lines = stack.getTag().getList("dm_copy_lines", 8);

         for(int i = 0; i < lines.size(); ++i) {
            PactLedger.Term t = new PactLedger.Term();
            t.cat = "note";
            t.text = lines.getString(i);
            terms.add(t);
         }
      }

      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PactViewPacket(3, UUID.randomUUID(), owner, signer, true, false, 0.0D, terms, new ArrayList()));
   }

   public static void onPactEdit(ServerPlayer player, PactEditPacket msg) {
      Level ledger_0 = player.level();
      if (ledger_0 instanceof ServerLevel level) {
         PactLedger ledger = PactLedger.get(level.getServer());
         PactLedger.Pact pact = ledger.pact(msg.pactId);
         if (pact != null) {
            boolean holding = holdsPact(player, msg.pactId) || msg.pactId.equals(remoteEdit.get(player.getUUID()));
            if (holding) {
               boolean proposerTurn = pact.proposed && !pact.signed && !pact.submitted && player.getUUID().equals(pact.signer);
               boolean dealmakerTurn = pact.proposed && !pact.signed && pact.submitted && pact.owner.equals(player.getUUID());
               int backMode = proposerTurn ? 4 : 0;
               if (msg.op == 0) {
                  boolean plainEdit_0 = !pact.proposed && pact.owner.equals(player.getUUID()) && !pact.signed;
                  if (!plainEdit_0 && !proposerTurn && !dealmakerTurn) {
                     return;
                  }

                  PactLedger.Term term = msg.term;
                  if (proposerTurn) {
                     term.side = 0;
                     term.locked = true;
                  } else {
                     term.locked = false;
                  }

                  ServerPlayer capsBy = player;
                  if (proposerTurn) {
                     ServerPlayer dm_0 = level.getServer().getPlayerList().getPlayer(pact.owner);
                     if (dm_0 != null) {
                        capsBy = dm_0;
                     }
                  }

                  if (!PactEnforcement.validateTerm(capsBy, term)) {
                     bar(player, proposerTurn ? "§7The Dealmaker's rank can't underwrite that ask yet." : "§7Your rank can't underwrite that term yet.");
                     sendView(player, level, pact, backMode);
                     return;
                  }

                  pact.terms.add(term);
                  if (term.cat.equals("soul_own") && term.side == 1) {
                     pact.soul = true;
                     markSoulStack(player, msg.pactId);
                  }

                  ledger.setDirty();
                  sendView(player, level, pact, backMode);
               } else if (msg.op == 1) {
                  boolean plainEdit = !pact.proposed && pact.owner.equals(player.getUUID()) && !pact.signed;
                  if (!plainEdit && !proposerTurn && !dealmakerTurn) {
                     return;
                  }

                  if (msg.index >= 0 && msg.index < pact.terms.size()) {
                     if (dealmakerTurn && ((PactLedger.Term)pact.terms.get(msg.index)).locked) {
                        bar(player, "§7The petitioner wrote that line. It is not yours to strike.");
                        sendView(player, level, pact, backMode);
                        return;
                     }

                     pact.terms.remove(msg.index);
                     boolean soulLeft = false;

                     for(PactLedger.Term t : pact.terms) {
                        if (t.cat.equals("soul_own") && t.side == 1) {
                           soulLeft = true;
                           break;
                        }
                     }

                     pact.soul = soulLeft;
                     ledger.setDirty();
                  }

                  sendView(player, level, pact, backMode);
               } else if (msg.op == 2) {
                  if (pact.proposed) {
                     return;
                  }

                  signPact(player, level, ledger, pact);
               } else if (msg.op == 4) {
                  if (!proposerTurn) {
                     return;
                  }

                  pact.submitted = true;
                  ledger.setDirty();
                  bar(player, "§6Pre-signed and sent.§7 It waits in the Dealmaker's Ledger.");
                  ServerPlayer dm = level.getServer().getPlayerList().getPlayer(pact.owner);
                  if (dm != null) {
                     dm.sendSystemMessage(Component.literal("§6" + player.getName().getString() + " sends a petition.§7 It waits in your Ledger."));
                  }

                  player.closeContainer();
               } else if (msg.op == 5) {
                  if (!dealmakerTurn) {
                     return;
                  }

                  countersign(player, level, ledger, pact);
               } else if (msg.op == 3) {
                  if (!pact.owner.equals(player.getUUID()) || !pact.signed && !pact.proposed) {
                     return;
                  }

                  destroyPact(level, ledger, pact, player);
               }

            }
         }
      }
   }

   private static void countersign(ServerPlayer dealmaker, ServerLevel level, PactLedger ledger, PactLedger.Pact pact) {
      ServerPlayer signer = pact.signer == null ? null : level.getServer().getPlayerList().getPlayer(pact.signer);
      if (signer == null) {
         bar(dealmaker, "§7The petitioner must walk the world when the pact seals.");
      } else {
         for(PactLedger.Term t_0 : pact.terms) {
            if (!PactEnforcement.validateTerm(dealmaker, t_0)) {
               bar(dealmaker, "§7Your rank can't_0 underwrite this yet: " + PactEnforcement.describe(t_0));
               return;
            }
         }

         if (pact.soul) {
            UUID keeper = ledger.soulOwnerOf(signer.getUUID());
            boolean ownSoul = keeper == null || keeper.equals(signer.getUUID());
            if (!ownSoul || ledger.soulPactExists(signer.getUUID())) {
               bar(dealmaker, "§7Their soul is already spoken for. The seal refuses.");
               return;
            }
         }

         for(PactLedger.Term t : pact.terms) {
            if (t.cat.equals("item") && t.side == 1 && !drain(signer, t.a, (int)t.v)) {
               bar(dealmaker, "§7The petitioner doesn't have the goods their page promises: " + t.a + " x" + (int)t.v);
               return;
            }
         }

         pact.signed = true;
         pact.proposed = false;
         pact.signTime = level.getGameTime();
         ledger.setDirty();
         ServerPlayer owner = dealmaker;

         for(PactLedger.Term t : pact.terms) {
            PactEnforcement.onSigned(level, ledger, pact, t, owner, signer);
         }

         PactEnforcement.refresh(level.getServer(), pact.owner);
         PactEnforcement.refresh(level.getServer(), pact.signer);
         Fx.ring(level, pact.soul ? 5882600 : 15254378, signer.position().add(0.0D, 0.1D, 0.0D), 1.8D, 14);
         level.playSound((Player)null, dealmaker.blockPosition(), DealBridge.sound("minecraft:block.beacon.activate"), SoundSource.PLAYERS, 1.0F, 1.4F);
         bar(dealmaker, "§6Sealed.§7 The petition is now a binding contract.");
         signer.sendSystemMessage(Component.literal("§6The Dealmaker seals your petition.§7 Every line on it now binds."));
         dealmaker.closeContainer();
         remoteEdit.remove(dealmaker.getUUID());
         sealLook(pact, signer, dealmaker, dealmaker);
      }
   }

   private static void sealLook(PactLedger.Pact pact, ServerPlayer signer, ServerPlayer keeper, ServerPlayer actor) {
      if (!actor.isInvisible()) {
         boolean together = keeper != null && keeper != signer && keeper.level() == signer.level() && keeper.distanceToSqr(signer) <= 400.0D && !keeper.isInvisible() && !signer.isInvisible();
         ServerLevel where = actor.serverLevel();
         Vec3 spot;
         if (together) {
            Vec3 line = keeper.position().subtract(signer.position());
            Vec3 flat_0 = new Vec3(line.x, 0.0D, line.z);
            flat_0 = flat_0.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : flat_0.normalize();
            spot = signer.position().add(keeper.position()).scale(0.5D).add(-flat_0.z * 1.3D, 1.45D, flat_0.x * 1.3D);
         } else {
            Vec3 look = actor.getLookAngle();
            Vec3 flat = new Vec3(look.x, 0.0D, look.z);
            flat = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
            spot = actor.position().add(flat.scale(1.8D)).add(-flat.z * 0.55D, 1.4D, flat.x * 0.55D);
         }

         ServerPlayer shownSigner = !together && actor != signer ? null : signer;
         ServerPlayer shownKeeper = !together && actor != keeper ? null : keeper;
         int lines = Math.max(1, Math.min(8, pact.terms.size()));
         String name = pact.signerName != null && !pact.signerName.isEmpty() ? pact.signerName : signer.getName().getString();
         boolean soul = pact.soul;
         DealBridge.look_seal(where, spot, shownSigner, shownKeeper, soul, lines, name.hashCode());
         int ink = soul ? 3108510 : 8000544;

         for(int i = 0; i < lines; ++i) {
            double w = 0.3D - (double)i * 0.065D;
            Spectacle.after(10 + i * 5 + 2, () -> where.sendParticles(Fx.ink(ink), spot.x, spot.y + w, spot.z, 2, 0.22D, 0.03D, 0.22D, 0.0D));
         }

         int wax = soul ? 3033740 : 9313580;
         Spectacle.after(DealBridge.sealSigned(lines) + 3, () -> where.sendParticles(Fx.ink(wax), spot.x, spot.y - 0.31D, spot.z, 7, 0.1D, 0.05D, 0.1D, 0.03D));
         if (soul && shownSigner != null && shownKeeper != null) {
            int from = DealBridge.sealWaxed(lines);

            for(int k = 0; k <= 6; ++k) {
               int step = k;
               Spectacle.after(from + step * 2, () -> {
                  if (!signer.isRemoved() && !keeper.isRemoved() && signer.level() == where && keeper.level() == where && !signer.isInvisible() && !keeper.isInvisible()) {
                     Vec3 a = signer.position().add(0.0D, 1.1D, 0.0D);
                     Vec3 b = keeper.position().add(0.0D, 1.1D, 0.0D);
                     Vec3 at = a.lerp(b, (double)step / 6.0D);
                     where.sendParticles(Fx.soulflame(5882600), at.x, at.y, at.z, 2, 0.05D, 0.05D, 0.05D, 0.005D);
                  }
               });
            }
         }

      }
   }

   private static boolean holdsPact(ServerPlayer player, UUID pactId) {
      for(InteractionHand hand : InteractionHand.values()) {
         ItemStack held = player.getItemInHand(hand);
         if (held.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && pactId.equals(ContractItem.pactOf(held))) {
            return true;
         }
      }

      return false;
   }

   private static void markSoulStack(ServerPlayer player, UUID pactId) {
      for(InteractionHand hand : InteractionHand.values()) {
         ItemStack held = player.getItemInHand(hand);
         if (held.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && pactId.equals(ContractItem.pactOf(held))) {
            held.getOrCreateTag().putBoolean("unrp_pact_soul", true);
         }
      }

   }

   private static void signPact(ServerPlayer signer, ServerLevel level, PactLedger ledger, PactLedger.Pact pact) {
      if (!pact.signed && !pact.proposed && !pact.owner.equals(signer.getUUID())) {
         ServerPlayer owner = level.getServer().getPlayerList().getPlayer(pact.owner);
         if (pact.soul) {
            UUID keeper = ledger.soulOwnerOf(signer.getUUID());
            boolean ownSoul = keeper == null || keeper.equals(signer.getUUID());
            if (!ownSoul || ledger.soulPactExists(signer.getUUID())) {
               bar(signer, "§7Your soul is already spoken for. The ink refuses.");
               return;
            }
         }

         for(PactLedger.Term t_0 : pact.terms) {
            if (t_0.cat.equals("item") && t_0.side == 1 && !drain(signer, t_0.a, (int)t_0.v)) {
               bar(signer, "§7You don't_0 have the goods the contract demands: " + t_0.a + " x" + (int)t_0.v);
               return;
            }
         }

         pact.signed = true;
         pact.signer = signer.getUUID();
         pact.signerName = signer.getName().getString();
         pact.signTime = level.getGameTime();
         ledger.setDirty();

         for(PactLedger.Term t : pact.terms) {
            PactEnforcement.onSigned(level, ledger, pact, t, owner, signer);
         }

         ItemStack moved = null;

         for(InteractionHand hand : InteractionHand.values()) {
            ItemStack held = signer.getItemInHand(hand);
            if (held.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && pact.id.equals(ContractItem.pactOf(held))) {
               ContractItem.syncFromLedger(held, pact);
               moved = held.copy();
               held.shrink(held.getCount());
            }
         }

         if (moved != null && owner != null) {
            if (!owner.getInventory().add(moved)) {
               owner.drop(moved, false);
            }
         } else if (moved != null && !signer.getInventory().add(moved)) {
            signer.drop(moved, false);
         }

         PactEnforcement.refresh(level.getServer(), pact.owner);
         PactEnforcement.refresh(level.getServer(), pact.signer);
         Fx.ring(level, pact.soul ? 5882600 : 15254378, signer.position().add(0.0D, 0.1D, 0.0D), 1.8D, 14);
         level.playSound((Player)null, signer.blockPosition(), DealBridge.sound("minecraft:item.book.page_turn"), SoundSource.PLAYERS, 1.4F, 0.5F);
         level.playSound((Player)null, signer.blockPosition(), DealBridge.sound("minecraft:block.beacon.activate"), SoundSource.PLAYERS, 1.0F, 1.4F);
         bar(signer, "§6Signed.§7 The terms hold until the contract is destroyed.");
         if (owner != null) {
            bar(owner, "§6" + signer.getName().getString() + " signed your contract.§7 It returns to your keeping.");
         }

         signer.closeContainer();
         sealLook(pact, signer, owner, signer);
      }
   }

   private static boolean drain(ServerPlayer player, String itemId, int count) {
      Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
      if (item == null) {
         return false;
      } else {
         int have = 0;

         for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack s_0 = player.getInventory().getItem(i);
            if (s_0.getItem() == item) {
               have += s_0.getCount();
            }
         }

         if (have < count) {
            return false;
         } else {
            int left = count;

            for(int i = 0; i < player.getInventory().getContainerSize() && left > 0; ++i) {
               ItemStack s = player.getInventory().getItem(i);
               if (s.getItem() == item) {
                  int take = Math.min(left, s.getCount());
                  s.shrink(take);
                  left -= take;
               }
            }

            return true;
         }
      }
   }

   public static void destroyPact(ServerLevel level, PactLedger ledger, PactLedger.Pact pact, ServerPlayer destroyer) {
      if (pact.soul && pact.signer != null) {
         UUID keeper = ledger.soulOwnerOf(pact.signer);
         if (keeper != null && keeper.equals(pact.owner)) {
            ledger.setSoulOwner(pact.signer, (UUID)null);
            ledger.bumpSerial(pact.signer);
         }
      }

      for(PactLedger.Term t : pact.terms) {
         PactEnforcement.onDestroyed(level, ledger, pact, t);
      }

      ledger.setLocation(pact.id, (String)null);
      PactFate.forget(pact.id);
      ledger.remove(pact.id);
      if (destroyer != null) {
         for(InteractionHand hand : InteractionHand.values()) {
            ItemStack held = destroyer.getItemInHand(hand);
            if (held.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && pact.id.equals(ContractItem.pactOf(held))) {
               held.shrink(held.getCount());
            }
         }

         bar(destroyer, "§6The contract burns in your grip. §7All terms are void.");
         Level var14 = destroyer.level();
         if (var14 instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)var14;
            Fx.burst(sl, Fx.flare(15254378), destroyer.position().add(0.0D, 1.2D, 0.0D), 26, 0.5D, 0.08D);
         }
      }

      PactEnforcement.refresh(level.getServer(), pact.owner);
      if (pact.signer != null) {
         PactEnforcement.refresh(level.getServer(), pact.signer);
      }

      ServerPlayer other = pact.signer == null ? null : level.getServer().getPlayerList().getPlayer(pact.signer);
      if (other != null) {
         bar(other, "§7A contract binding you has been destroyed. Its terms release you.");
      }

   }

   @SubscribeEvent
   public static void onRitualStart(PlayerInteractEvent.RightClickItem event) {
      Player off_0 = event.getEntity();
      if (off_0 instanceof ServerPlayer player) {
         Level var8 = player.level();
         if (var8 instanceof ServerLevel level) {
            if (event.getHand() != InteractionHand.MAIN_HAND) {
               return;
            }

            ItemStack off = player.getOffhandItem();
            ItemStack main = player.getMainHandItem();
            if (off.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && main.getItem() instanceof SwordItem) {
               UUID pactId = ContractItem.pactOf(off);
               PactLedger ledger = PactLedger.get(level.getServer());
               PactLedger.Pact pact = ledger.pact(pactId);
               if (pact != null && pact.signed && pact.ritual == 0 && player.getUUID().equals(pact.signer)) {
                  pact.ritual = 1;
                  pact.ritualBy = player.getUUID();
                  ledger.setDirty();
                  player.addTag("unrp.dm_ritual");
                  player.hurt(level.damageSources().magic(), player.getMaxHealth() * 0.5F);
                  Fx.ring(level, 11546672, player.position().add(0.0D, 0.1D, 0.0D), 2.0D, 16);
                  level.playSound((Player)null, player.blockPosition(), DealBridge.sound("minecraft:entity.wither.hurt"), SoundSource.PLAYERS, 1.2F, 0.6F);
                  player.sendSystemMessage(Component.literal("§4The blood price is paid.§7 You won't heal until the ritual ends, or until you die, and the ritual resets with you. First, soul fire in the Nether. Then the void."));
                  return;
               }

               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onRitualDeath(LivingDeathEvent event) {
      LivingEntity ledger_0 = event.getEntity();
      if (ledger_0 instanceof ServerPlayer player) {
         Level var4 = player.level();
         if (var4 instanceof ServerLevel level) {
            PactLedger ledger = PactLedger.get(level.getServer());
            if (!ledger.ritualRunning(player.getUUID()) && !player.getTags().contains("unrp.dm_ritual")) {
               return;
            }

            ledger.resetRitualsBy(player.getUUID());
            player.removeTag("unrp.dm_ritual");
            player.sendSystemMessage(Component.literal("§4Death interrupts the ritual.§7 Its steps reset, and you owe the blood price again. You can heal."));
            return;
         }
      }

   }

   public static void ritualAdvanced(ServerLevel level, ItemEntity entity, PactLedger.Pact pact) {
      Fx.shell(level, Fx.ember(5882600), entity.position(), 1.0D, 30, 0.8D);
      level.playSound((Player)null, entity.blockPosition(), DealBridge.sound("minecraft:block.soul_sand.break"), SoundSource.PLAYERS, 1.4F, 0.5F);
      ServerPlayer signer = pact.signer == null ? null : level.getServer().getPlayerList().getPlayer(pact.signer);
      if (signer != null) {
         signer.sendSystemMessage(Component.literal("§3The soul fire has tasted the contract.§7 One step remains: the void."));
      }

   }

   public static void contractVoided(ServerLevel level, ItemEntity entity, ItemStack stack, PactLedger.Pact pact) {
      PactLedger ledger = PactLedger.get(level.getServer());
      if (pact.ritual >= 2) {
         UUID runner = pact.ritualBy != null ? pact.ritualBy : pact.signer;
         ServerPlayer walker = runner == null ? null : level.getServer().getPlayerList().getPlayer(runner);
         destroyPact(level, ledger, pact, (ServerPlayer)null);
         if (walker != null) {
            walker.removeTag("unrp.dm_ritual");
            walker.sendSystemMessage(Component.literal("§6The void takes the contract.§7 Every term is unwritten. You can heal again."));
         }

         entity.discard();
      } else {
         ServerPlayer owner = level.getServer().getPlayerList().getPlayer(pact.owner);
         if (owner != null) {
            ItemStack back = stack.copy();
            entity.discard();
            if (!owner.getInventory().add(back)) {
               owner.drop(back, false);
            }

            bar(owner, "§7Your contract finds its way back to you.");
         } else {
            SoulPlaneEvents.stashInPlane(level.getServer(), entity, pact.owner);
         }

      }
   }

   @SubscribeEvent
   public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
      Player out_0 = event.getEntity();
      if (out_0 instanceof ServerPlayer player) {
         Level var12 = player.level();
         if (var12 instanceof ServerLevel level) {
            ItemStack out = event.getCrafting();
            if (out.getItem() != com.myriam.unrp.item.ModItems.CONTRACT.get()) {
               return;
            }

            PactLedger ledger = PactLedger.get(level.getServer());

            for(int i = 0; i < event.getInventory().getContainerSize(); ++i) {
               ItemStack in = event.getInventory().getItem(i);
               if (in.getItem() == com.myriam.unrp.item.ModItems.CONTRACT.get() && !ContractItem.isCopy(in)) {
                  PactLedger.Pact pact = ledger.pact(ContractItem.pactOf(in));
                  CompoundTag tag_0 = out.getOrCreateTag();
                  tag_0.remove("unrp_pact");
                  tag_0.putBoolean("unrp_pact_copy", true);
                  if (pact != null && pact.signed) {
                     tag_0.putString("dm_copy_owner", pact.ownerName);
                     tag_0.putString("dm_copy_signer", pact.signerName);
                     if (pact.soul) {
                        tag_0.putBoolean("unrp_pact_soul", true);
                     }

                     ListTag lines = new ListTag();

                     for(PactLedger.Term t : pact.terms) {
                        lines.add(StringTag.valueOf(PactEnforcement.describe(t)));
                     }

                     tag_0.put("dm_copy_lines", lines);
                     return;
                  }

                  tag_0.putString("dm_copy_owner", "nobody");
                  tag_0.putString("dm_copy_signer", "an unsigned page");
                  return;
               }
            }

            CompoundTag tag = out.getOrCreateTag();
            tag.putBoolean("unrp_pact_proposed", true);
            player.sendSystemMessage(Component.literal("§6A blank petition.§7 Write your asks on it, pre-sign, and it will find the Dealmaker's Ledger."));
            return;
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onDealmakerDeath(LivingDeathEvent event) {
      LivingEntity ledger_0 = event.getEntity();
      if (ledger_0 instanceof ServerPlayer player) {
         Level var11 = player.level();
         if (var11 instanceof ServerLevel level) {
            if (chainKilling.contains(player.getUUID())) {
               return;
            }

            PactLedger ledger = PactLedger.get(level.getServer());
            UUID master = ledger.dealmakerOf(player.getUUID());
            if (master != null) {
               long now_0 = level.getGameTime();
               int rank = ledger.rankOf(master, player.getUUID());
               if (rank == 2 && ledger.greaterCdOf(player.getUUID()) <= now_0) {
                  ledger.setGreaterCd(player.getUUID(), now_0 + 20L * (effectiveRank(player, rank) >= 3 ? 120L : 300L));
                  event.setCanceled(true);
                  player.setHealth(player.getMaxHealth());
                  player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 4, false, false));
                  Fx.shell(level, Fx.glint(15254378), player.position().add(0.0D, 1.0D, 0.0D), 1.4D, 40, 1.4D);
                  bar(player, "§6The Greater seat doesn't fall that easily.");
                  return;
               }
            }

            if (!player.getTags().contains("unrp.has.dealmaker")) {
               return;
            }

            UUID[] seats = ledger.bindsOf(player.getUUID());
            String[] names = ledger.bindNamesOf(player.getUUID());
            long now = level.getGameTime();

            for(int i = 0; i < 3; ++i) {
               if (seats[i] != null && ledger.reviveCdOf(seats[i]) <= now) {
                  ServerPlayer ally = level.getServer().getPlayerList().getPlayer(seats[i]);
                  if (ally != null && !ally.isDeadOrDying()) {
                     ledger.setReviveCd(seats[i], now + 12000L);
                     event.setCanceled(true);
                     chainKilling.add(seats[i]);
                     ally.hurt(level.damageSources().magic(), 1000000.0F);
                     chainKilling.remove(seats[i]);
                     player.setHealth(player.getMaxHealth());
                     player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 4, false, false));
                     player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 2, false, false));
                     level.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§6" + names[i] + "§7 died in The Dealmaker's stead."), false);
                     Fx.ring(level, 15254378, player.position().add(0.0D, 0.1D, 0.0D), 2.4D, 18);
                     Fx.shell(level, Fx.flare(15254378), player.position().add(0.0D, 1.2D, 0.0D), 1.4D, 50, 1.8D);
                     level.playSound((Player)null, player.blockPosition(), DealBridge.sound("minecraft:block.respawn_anchor.charge"), SoundSource.PLAYERS, 1.6F, 0.4F);
                     return;
                  }
               }
            }

            return;
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGH
   )
   public static void onHurt(LivingHurtEvent event) {
      LivingEntity ledger = event.getEntity();
      if (ledger instanceof ServerPlayer victim) {
         Level var9 = victim.level();
         if (var9 instanceof ServerLevel level) {
            PactLedger var10 = PactLedger.get(level.getServer());
            Entity e_0 = event.getSource().getEntity();
            if (e_0 instanceof ServerPlayer attacker) {
               UUID master = var10.dealmakerOf(attacker.getUUID());
               if (master != null && master.equals(victim.getUUID())) {
                  event.setCanceled(true);
                  bar(attacker, "§7Your hand refuses. The pact does not bite its holder.");
                  return;
               }

               int[] imbued = (int[])selfImbue.get(attacker.getUUID());
               if (imbued != null && LifeClock.now(attacker) <= imbued[1]) {
                  event.setAmount((float)((double)event.getAmount() * IMBUE_DMG[imbued[0]]));
               }

               UUID atkMaster = var10.dealmakerOf(attacker.getUUID());
               if (atkMaster != null) {
                  int rank = effectiveRank(attacker, var10.rankOf(atkMaster, attacker.getUUID()));
                  if (rank >= 0) {
                     event.setAmount((float)((double)event.getAmount() * (1.0D + BIND_STATS[rank])));
                  }
               }

               long[] borrowed = (long[])borrow.get(attacker.getUUID());
               if (borrowed != null && (long)LifeClock.now(attacker) <= borrowed[0]) {
                  event.setAmount((float)((double)event.getAmount() * (1.0D + (double)borrowed[1] * 0.04D)));
               }
            }

            if (!redirecting.contains(victim.getUUID()) && !DealBridge.inFight(victim.getUUID())) {
               for(Map.Entry<UUID, UUID> e : protectLink.entrySet()) {
                  if (((UUID)e.getValue()).equals(victim.getUUID())) {
                     ServerPlayer keeper = level.getServer().getPlayerList().getPlayer((UUID)e.getKey());
                     if (keeper != null && !keeper.isDeadOrDying() && isDealmaker(keeper)) {
                        event.setCanceled(true);
                        redirecting.add(keeper.getUUID());
                        keeper.hurt(event.getSource(), event.getAmount());
                        redirecting.remove(keeper.getUUID());
                        Fx.line(level, Fx.glint(15254378), victim.position().add(0.0D, 1.2D, 0.0D), keeper.position().add(0.0D, 1.2D, 0.0D), 0.8D, 0.1D, 0.02D);
                        return;
                     }
                  }
               }

               return;
            }

            return;
         }
      }

   }

   @SubscribeEvent
   public static void onDrops(LivingDropsEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         event.getDrops().removeIf((drop) -> drop.getItem().getItem() == com.myriam.unrp.item.ModItems.SOUL_SWORD.get());
      }
   }

   private static void sendHud(ServerPlayer player, ServerLevel level, PactLedger ledger) {
      UUID[] seats = ledger.bindsOf(player.getUUID());
      String[] names = ledger.bindNamesOf(player.getUUID());
      String[] outNames = new String[3];
      int[] hp = new int[3];
      int[] flags = new int[3];
      long now = level.getGameTime();

      for(int i = 0; i < 3; ++i) {
         if (seats[i] == null) {
            outNames[i] = "";
         } else {
            outNames[i] = names[i];
            ServerPlayer ally = level.getServer().getPlayerList().getPlayer(seats[i]);
            if (ally == null) {
               flags[i] = 2;
            } else {
               flags[i] = 1;
               hp[i] = (int)Math.round(100.0D * (double)ally.getHealth() / (double)ally.getMaxHealth());
               if (ally.isDeadOrDying()) {
                  flags[i] = 2;
               }
            }

            if (ledger.reviveCdOf(seats[i]) > now) {
               flags[i] |= 8;
            }

            UUID ward = (UUID)protectLink.get(player.getUUID());
            if (ward != null && ward.equals(seats[i])) {
               flags[i] |= 4;
            }
         }
      }

      List<String> info = PactEnforcement.infoLines(level.getServer(), player);
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PactHudPacket(outNames, hp, flags, info));
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      UUID id = event.getEntity().getUUID();
      remoteEdit.remove(id);
      bindPick.remove(id);
      protectPick.remove(id);
      imbuePick.remove(id);
      protectLink.remove(id);
      pendingBind.remove(id);
      pendingBinder.remove(id);
      pendingRank.remove(id);
      selector.remove(id);
      borrow.remove(id);
      borrowedFrom.remove(id);
      Player self_0 = event.getEntity();
      if (self_0 instanceof ServerPlayer player) {
         int[] self = (int[])selfImbue.remove(id);
         if (self != null) {
            DealBridge.run(player.server, "scale set pehkui:base 1.0 " + player.getName().getString());
            unpin(player.getAttribute(Attributes.MOVEMENT_SPEED), ISPEED_UUID);
            dropCopy(player);
         }
      }

   }

   @SubscribeEvent
   public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
      Player ledger_0 = event.getEntity();
      if (ledger_0 instanceof ServerPlayer player) {
         Level var5 = player.level();
         if (var5 instanceof ServerLevel level) {
            PactLedger ledger = PactLedger.get(level.getServer());
            String alias = ledger.takePendingAlias(player.getUUID());
            if (alias != null) {
               PactEnforcement.applyAlias(player, alias);
            }

            PactEnforcement.refresh(level.getServer(), player.getUUID());
            return;
         }
      }

   }
}
