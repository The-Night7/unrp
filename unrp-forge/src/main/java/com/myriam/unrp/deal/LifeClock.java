package com.myriam.unrp.deal;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;

public final class LifeClock {
   private LifeClock() {
   }

   public static int now(LivingEntity e) {
      if (e == null) {
         return 0;
      } else {
         MinecraftServer server = e.getServer();
         return server == null ? e.tickCount : server.getTickCount();
      }
   }
}
