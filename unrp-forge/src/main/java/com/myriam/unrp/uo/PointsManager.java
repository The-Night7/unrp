package com.myriam.unrp.uo;

import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraftforge.network.PacketDistributor;

public final class PointsManager {
   private PointsManager() {}

   public static int score(ServerPlayer player, String objName) {
      ServerScoreboard sb = player.server.getScoreboard();
      Objective o = sb.getObjective(objName);
      return o == null ? 0 : sb.getOrCreatePlayerScore(player.getName().getString(), o).getScore();
   }

   public static void setScore(ServerPlayer player, String objName, int value) {
      ServerScoreboard sb = player.server.getScoreboard();
      Objective o = sb.getObjective(objName);
      if (o == null) {
         o = sb.addObjective(objName, ObjectiveCriteria.DUMMY, Component.literal(objName), RenderType.INTEGER);
      }

      sb.getOrCreatePlayerScore(player.getName().getString(), o).setScore(value);
   }
}
