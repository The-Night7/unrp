package com.myriam.unrp.uo;



import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class OriginVesselEvents {
   private static final String[] RIDERS = new String[]{"sarcophage", "edict", "wyrm_engine", "dealmaker"};

   private static boolean rides(ServerPlayer player) {
      Set<String> tags = player.getTags();

      for(String power : RIDERS) {
         if (tags.contains("unrp.has." + power)) {
            return true;
         }
      }

      return false;
   }

   static boolean meaningful(LivingEntity victim) {
      return !HollowDeepFavors.doppelKnown(victim) && (victim instanceof Player || victim.getMaxHealth() >= 10.0F);
   }

   private static void bar(ServerPlayer player, String msg) {
      player.displayClientMessage(Component.literal(msg), true);
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player var2 = event.player;
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            if (player.tickCount % 40 == 0) {
               com.myriam.unrp.ability.OriginGrowth.apply(player, "vessel", rides(player), 0.0D, 0.0D, 0.0D);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onDealt(LivingHurtEvent event) {
      Entity victim_0 = event.getSource().getEntity();
      if (victim_0 instanceof ServerPlayer attacker) {
         LivingEntity victim = event.getEntity();
         if (victim != attacker && rides(attacker) && meaningful(victim)) {
            int xp = Math.min(10, (int)Math.ceil((double)(event.getAmount() / 3.0F)));
            award(attacker, xp);
         }
      }
   }

   @SubscribeEvent
   public static void onKill(LivingDeathEvent event) {
      Entity victim_0 = event.getSource().getEntity();
      if (victim_0 instanceof ServerPlayer attacker) {
         LivingEntity victim = event.getEntity();
         if (victim != attacker && rides(attacker) && meaningful(victim)) {
            award(attacker, victim instanceof Player ? 30 : 15);
         }
      }
   }

   private static void award(ServerPlayer player, int xp) {
      int gained = com.myriam.unrp.ability.OriginGrowth.award(player, xp);
      if (gained > 0) {
         int lv = com.myriam.unrp.ability.OriginGrowth.level(player);
         bar(player, "§6The vessel deepens. §eLevel " + lv + " §7(+" + (double)com.myriam.unrp.ability.OriginGrowth.gainMilli(lv) / 1000.0D + " growth)");
      }

   }
}
