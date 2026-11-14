package com.myriam.unrp.uo;



import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class AlignmentEvents {
   public static final String ALIGN = "unrp.alignment";
   public static final int ROSE_AT = -20;

   private static int score(ServerPlayer player, int delta) {
      Scoreboard sb = player.server.getScoreboard();
      Objective o = sb.getObjective("unrp.alignment");
      if (o == null) {
         o = sb.addObjective("unrp.alignment", ObjectiveCriteria.DUMMY, Component.literal("unrp.alignment"), RenderType.INTEGER);
      }

      Score row = sb.getOrCreatePlayerScore(player.getScoreboardName(), o);
      int now = Math.max(-100, Math.min(50, row.getScore() + delta));
      if (delta != 0) {
         row.setScore(now);
      }

      return now;
   }

   public static int of(ServerPlayer player) {
      return score(player, 0);
   }

   @SubscribeEvent
   public static void onDeath(LivingDeathEvent event) {
      Entity victim_0 = event.getSource().getEntity();
      if (victim_0 instanceof ServerPlayer killer) {
         LivingEntity victim = event.getEntity();
         if (victim != killer) {
            int delta = 0;
            if (victim instanceof AbstractVillager) {
               delta = -5;
            } else {
               label36: {
                  if (victim instanceof IronGolem) {
                     IronGolem golem = (IronGolem)victim;
                     if (golem.isPlayerCreated()) {
                        delta = -8;
                        break label36;
                     }
                  }

                  if (victim instanceof TamableAnimal) {
                     TamableAnimal pet = (TamableAnimal)victim;
                     if (pet.isTame() && !killer.getUUID().equals(pet.getOwnerUUID())) {
                        delta = -6;
                     }
                  }
               }
            }

            if (delta != 0) {
               int before = of(killer);
               int now = score(killer, delta);
               if (before > -20 && now <= -20) {
                  killer.sendSystemMessage(Component.literal("§d[Unrp] §7Something in your power curdles. §dIt suits you."));
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void tick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player var2 = event.player;
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            if (player.tickCount % '負' == 0 && player.tickCount != 0) {
               if (of(player) < 0) {
                  score(player, 1);
               }

            }
         }
      }
   }
}
