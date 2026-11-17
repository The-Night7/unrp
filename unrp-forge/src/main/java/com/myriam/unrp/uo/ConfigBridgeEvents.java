package com.myriam.unrp.uo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public final class ConfigBridgeEvents {
   private ConfigBridgeEvents() {}

   public static boolean flag(MinecraftServer server, String key, boolean def) {
      try {
         Scoreboard sb = server.getScoreboard();
         Objective o = sb.getObjective("unrp.config");
         if (o == null) {
            return def;
         } else if (!sb.hasPlayerScore(key, o)) {
            return def;
         } else {
            return sb.getOrCreatePlayerScore(key, o).getScore() != 0;
         }
      } catch (Exception var5) {
         return def;
      }
   }

   public static double factor(MinecraftServer server, String key, double def) {
      try {
         Scoreboard sb = server.getScoreboard();
         Objective o = sb.getObjective("unrp.config");
         if (o == null) {
            return def;
         } else {
            return !sb.hasPlayerScore(key, o) ? def : (double)sb.getOrCreatePlayerScore(key, o).getScore() / 1000.0D;
         }
      } catch (Exception var6) {
         return def;
      }
   }
}
