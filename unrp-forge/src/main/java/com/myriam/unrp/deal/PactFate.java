package com.myriam.unrp.deal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(
   modid = "unrp"
)
public class PactFate {
   private static UUID collecting;
   private static final Map<UUID, Boolean> activity = new HashMap();
   private static final Set<UUID> tripping = new HashSet();

   public static boolean collecting(UUID signer) {
      return collecting != null && collecting.equals(signer);
   }

   private PactFate() {
   }

   public static boolean conditionsActive(MinecraftServer server, PactLedger.Pact pact) {
      for(PactLedger.Term t : pact.terms) {
         if (t.cat.equals("cond")) {
            ServerPlayer signer = pact.signer == null ? null : server.getPlayerList().getPlayer(pact.signer);
            boolean var10000;
            switch (t.key) {
               case "time":
                  ServerLevel var14;
                  label86: {
                     if (signer != null) {
                        Level var10 = signer.level();
                        if (var10 instanceof ServerLevel) {
                           ServerLevel sl = (ServerLevel)var10;
                           var14 = sl;
                           break label86;
                        }
                     }

                     var14 = server.overworld();
                  }

                  ServerLevel where = var14;
                  var10000 = t.a.equals("day") == where.isDay();
                  break;
               case "health":
                  var10000 = signer != null && (double)signer.getHealth() < (double)signer.getMaxHealth() * (t.v / 100.0D);
                  break;
               case "prox":
                  ServerPlayer other = server.getPlayerList().getPlayerByName(t.a);
                  if (signer != null && other != null && signer.level() == other.level()) {
                     boolean near = signer.distanceToSqr(other) <= 2304.0D;
                     var10000 = t.v >= 1.0D == near;
                  } else {
                     var10000 = t.v < 1.0D;
                  }
                  break;
               case "dim":
                  if (signer == null) {
                     var10000 = false;
                  } else {
                     boolean inside = signer.level().dimension().location().toString().equals(t.a);
                     var10000 = t.v >= 1.0D == inside;
                  }
                  break;
               default:
                  var10000 = true;
            }

            boolean holds = var10000;
            if (!holds) {
               return false;
            }
         }
      }

      return true;
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         MinecraftServer server = event.getServer();
         if (server != null && server.overworld() != null && server.overworld().getGameTime() % 20L == 0L) {
            PactLedger ledger = PactLedger.get(server);
            List<PactLedger.Pact> tripped = null;

            for(PactLedger.Pact pact_0 : ledger.allSigned()) {
               boolean conditional = false;
               PactLedger.Term timer = null;

               for(PactLedger.Term t : pact_0.terms) {
                  if (t.cat.equals("cond")) {
                     conditional = true;
                  } else if (t.cat.equals("bind") && t.key.equals("timer")) {
                     timer = t;
                  }
               }

               if (conditional) {
                  boolean now = conditionsActive(server, pact_0);
                  Boolean before = (Boolean)activity.put(pact_0.id, now);
                  if (before != null && before != now) {
                     PactEnforcement.refresh(server, pact_0.owner);
                     PactEnforcement.refresh(server, pact_0.signer);
                     tell(server, pact_0.owner, now ? "§6The contract stirs. Its terms wake." : "§8The contract sleeps. Its terms rest.");
                     tell(server, pact_0.signer, now ? "§6The contract stirs. Its terms wake." : "§8The contract sleeps. Its terms rest.");
                  }
               }

               if (timer != null && pact_0.signer != null && server.getPlayerList().getPlayer(pact_0.signer) != null && server.overworld().getGameTime() >= pact_0.signTime + (long)(timer.v * 1200.0D)) {
                  if (tripped == null) {
                     tripped = new ArrayList();
                  }

                  tripped.add(pact_0);
               }
            }

            if (tripped != null) {
               for(PactLedger.Pact pact : tripped) {
                  ServerLevel var10000;
                  label76: {
                     ServerPlayer signer = pact.signer == null ? null : server.getPlayerList().getPlayer(pact.signer);
                     if (signer != null) {
                        Level var17 = signer.level();
                        if (var17 instanceof ServerLevel) {
                           ServerLevel sl = (ServerLevel)var17;
                           var10000 = sl;
                           break label76;
                        }
                     }

                     var10000 = server.overworld();
                  }

                  ServerLevel where = var10000;
                  trip(where, ledger, pact, "the clock ran out");
               }
            }

         }
      }
   }

   private static void tell(MinecraftServer server, UUID who, String msg) {
      if (who != null) {
         ServerPlayer p = server.getPlayerList().getPlayer(who);
         if (p != null) {
            p.displayClientMessage(Component.literal(msg), true);
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onBindDeath(LivingDeathEvent event) {
      LivingEntity ledger_0 = event.getEntity();
      if (ledger_0 instanceof ServerPlayer player) {
         Level var8 = player.level();
         if (var8 instanceof ServerLevel level) {
            PactLedger ledger = PactLedger.get(level.getServer());

            for(PactLedger.Pact pact : ledger.signedPactsTouching(player.getUUID())) {
               if (player.getUUID().equals(pact.signer)) {
                  for(PactLedger.Term t : pact.terms) {
                     if (t.cat.equals("bind") && t.key.equals("death")) {
                        trip(level, ledger, pact, "the Signer died");
                        break;
                     }
                  }
               }
            }

            return;
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onBindHurt(LivingHurtEvent event) {
      if (!event.isCanceled()) {
         Entity ledger = event.getSource().getEntity();
         if (ledger instanceof ServerPlayer) {
            ServerPlayer attacker = (ServerPlayer)ledger;
            Level var10 = attacker.level();
            if (var10 instanceof ServerLevel) {
               ServerLevel level = (ServerLevel)var10;
               PactLedger var11 = PactLedger.get(level.getServer());
               LivingEntity pact_0 = event.getEntity();
               String var10000;
               if (pact_0 instanceof Player) {
                  Player vp = (Player)pact_0;
                  var10000 = vp.getName().getString();
               } else {
                  var10000 = "";
               }

               String victimName = var10000;

               for(PactLedger.Pact pact : var11.signedPactsTouching(attacker.getUUID())) {
                  if (attacker.getUUID().equals(pact.signer)) {
                     for(PactLedger.Term t : pact.terms) {
                        if (t.cat.equals("bind")) {
                           boolean hit = t.key.equals("violence") || t.key.equals("attack") && !victimName.isEmpty() && victimName.equalsIgnoreCase(t.a);
                           if (hit) {
                              trip(level, var11, pact, t.key.equals("violence") ? "the Signer raised a hand" : "the Signer struck " + t.a);
                              break;
                           }
                        }
                     }
                  }
               }

               return;
            }
         }
      }

   }

   @SubscribeEvent
   public static void onBindTravel(PlayerEvent.PlayerChangedDimensionEvent event) {
      Player ledger_0 = event.getEntity();
      if (ledger_0 instanceof ServerPlayer player) {
         Level var13 = player.level();
         if (var13 instanceof ServerLevel level) {
            if (!DealBridge.myWorldMoving(player.getUUID())) {
               PactLedger ledger = PactLedger.get(level.getServer());
               String dim = event.getTo().location().toString();

               for(PactLedger.Pact pact : ledger.signedPactsTouching(player.getUUID())) {
                  if (player.getUUID().equals(pact.signer)) {
                     for(PactLedger.Term t : pact.terms) {
                        if (t.cat.equals("bind") && t.key.equals("dim")) {
                           boolean hit;
                           if (t.a.startsWith("player:")) {
                              String named = t.a.substring("player:".length());
                              UUID namedId = null;
                              ServerPlayer online = level.getServer().getPlayerList().getPlayerByName(named);
                              if (online != null) {
                                 namedId = online.getUUID();
                              }

                              hit = event.getTo() == SoulPlaneEvents.PLANE && namedId != null && namedId.equals(ledger.ownerOfPlot(SoulPlaneEvents.plotIndexAt(player.getX())));
                           } else {
                              hit = dim.equals(t.a);
                           }

                           if (hit) {
                              trip(level, ledger, pact, "the Signer crossed into " + t.a);
                              break;
                           }
                        }
                     }
                  }
               }

               return;
            }
         }
      }

   }

   private static void trip(ServerLevel level, PactLedger ledger, PactLedger.Pact pact, String why) {
      if (ledger.pact(pact.id) != null && tripping.add(pact.id)) {
         try {
            tripInner(level, ledger, pact, why);
         } finally {
            tripping.remove(pact.id);
         }

      }
   }

   private static void tripInner(ServerLevel level, PactLedger ledger, PactLedger.Pact pact, String why) {
      UUID ownerId = pact.owner;
      UUID signerId = pact.signer;
      String signerName = pact.signerName;
      List<PactLedger.Term> punishments = new ArrayList();

      for(PactLedger.Term t_0 : pact.terms) {
         if (t_0.cat.equals("punish")) {
            punishments.add(t_0);
         }
      }

      activity.remove(pact.id);
      tell(level.getServer(), ownerId, "§4A bind snapped§7: " + why + ". The contract with " + signerName + " is destroyed.");
      tell(level.getServer(), signerId, "§4A bind snapped§7: " + why + ". The contract is destroyed" + (punishments.isEmpty() ? "." : ", §4and its punishment falls."));
      DealmakerEvents.destroyPact(level, ledger, pact, (ServerPlayer)null);

      for(PactLedger.Term t : punishments) {
         punish(level, ledger, t, ownerId, signerId, signerName);
      }

   }

   private static void punish(ServerLevel level, PactLedger ledger, PactLedger.Term t, UUID ownerId, UUID signerId, String signerName) {
      MinecraftServer server = level.getServer();
      ServerPlayer owner = ownerId == null ? null : server.getPlayerList().getPlayer(ownerId);
      ServerPlayer signer = signerId == null ? null : server.getPlayerList().getPlayer(signerId);
      switch (t.key) {
         case "soul":
            if (signerId != null && ownerId != null) {
               ledger.setSoulOwner(signerId, ownerId);
               ledger.bumpSerial(signerId);
               if (signer != null) {
                  signer.sendSystemMessage(Component.literal("§4The broken bind takes its price: your soul."));
               }

               if (owner != null) {
                  owner.sendSystemMessage(Component.literal("§6" + signerName + "'s soul falls to you, as agreed."));
               }
            }
            break;
         case "death":
            if (signer != null && !signer.isDeadOrDying()) {
               signer.sendSystemMessage(Component.literal("§4The broken bind demands everything."));
               UUID was = collecting;
               collecting = signer.getUUID();

               try {
                  TrueDamage.deal(level, owner, signer, 1000000.0F);
               } finally {
                  collecting = was;
               }
            }
            break;
         case "possessions":
            if (signer == null) {
               return;
            }

            List<ItemStack> taken = new ArrayList();

            for(int i = 0; i < signer.getInventory().getContainerSize(); ++i) {
               ItemStack s_0 = signer.getInventory().getItem(i);
               if (!s_0.isEmpty()) {
                  taken.add(s_0.copy());
                  signer.getInventory().setItem(i, ItemStack.EMPTY);
               }
            }

            if (taken.isEmpty()) {
               return;
            }

            if (owner != null) {
               for(ItemStack s : taken) {
                  if (!owner.getInventory().add(s)) {
                     owner.drop(s, false);
                  }
               }
            } else if (ownerId != null) {
               ListTag list = new ListTag();

               for(ItemStack s : taken) {
                  list.add(s.save(new CompoundTag()));
               }

               ledger.setPendingItems(ownerId, list);
            }

            signer.sendSystemMessage(Component.literal("§4Everything you carried now belongs to The Dealmaker."));
            break;
         case "contract":
            if (ownerId == null || signerId == null) {
               return;
            }

            PactLedger.Pact fresh = ledger.create(ownerId, owner != null ? owner.getName().getString() : "The Dealmaker");
            fresh.signer = signerId;
            fresh.signerName = signerName;
            fresh.proposed = true;
            fresh.submitted = true;
            ledger.setDirty();
            if (owner != null) {
               owner.sendSystemMessage(Component.literal("§6A blank page, pre-signed by " + signerName + ", lands in your Ledger."));
            }

            if (signer != null) {
               signer.sendSystemMessage(Component.literal("§4The broken bind leaves your name on a blank page in the Dealmaker's keeping."));
            }
            break;
         case "xp":
            if (signer == null) {
               return;
            }

            int levels = signer.experienceLevel;
            int pts = signer.totalExperience;
            signer.setExperienceLevels(0);
            signer.setExperiencePoints(0);
            if (owner != null) {
               owner.giveExperiencePoints(Math.max(pts, 0));
               if (pts <= 0 && levels > 0) {
                  owner.giveExperienceLevels(levels);
               }
            } else if (ownerId != null && levels > 0) {
               ledger.addPendingLevels(ownerId, levels);
            }

            signer.sendSystemMessage(Component.literal("§4Your experience drains into the pact."));
            break;
         case "levels":
            if (signer == null) {
               return;
            }

            int half = signer.experienceLevel / 2;
            if (half <= 0) {
               return;
            }

            signer.giveExperienceLevels(-half);
            if (owner != null) {
               owner.giveExperienceLevels(half);
            } else if (ownerId != null) {
               ledger.addPendingLevels(ownerId, half);
            }

            signer.sendSystemMessage(Component.literal("§4Half your levels flow to The Dealmaker."));
            break;
         case "seal":
            if (signer == null) {
               return;
            }

            String power = DealBridge.powerOf(signer);
            if (power.isEmpty()) {
               return;
            }

            if (power.equals("legacy")) {
               if (owner != null) {
                  owner.sendSystemMessage(Component.literal("§5The Legacy only goes where its holder gives it. The pact can't take it."));
               }

               signer.sendSystemMessage(Component.literal("§5The Legacy stays with you. Only you can pass it on."));
               return;
            }

            if (DealBridge.isOriginPower(power)) {
               if (owner != null) {
                  owner.sendSystemMessage(Component.literal("§5An otherworldly essence guards that gift. The pact can't strip an Origin."));
               }

               if (signer != null) {
                  signer.sendSystemMessage(Component.literal("§5The pact can't take your Origin."));
               }

               return;
            }

            DealBridge.run(server, "superpower remove unrp:" + power + " " + signer.getName().getString());
            signer.sendSystemMessage(Component.literal("§4Your ability is sealed away, claimed by the pact."));
            if (owner != null) {
               DealBridge.run(server, "superpower add unrp:" + power + " " + owner.getName().getString());
               owner.sendSystemMessage(Component.literal("§6" + signerName + "'s ability is yours now: " + com.myriam.unrp.am.PowerNames.display(power)));
            } else if (ownerId != null) {
               ledger.addPendingPower(ownerId, power);
            }
      }

   }

   public static void forget(UUID pactId) {
      activity.remove(pactId);
   }
}
