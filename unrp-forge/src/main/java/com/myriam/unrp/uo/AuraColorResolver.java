package com.myriam.unrp.uo;

import com.myriam.unrp.look.ClientSettingsCache;


import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class AuraColorResolver {
   public static final int PARTICLES_DEFAULT = 15160448;
   public static final int AM_DARK = 3026486;

   public static int forOwner(UUID uuid, int fallback) {
      if (uuid == null) {
         return fallback;
      } else if (ClientSettingsCache.isAM(uuid)) {
         return 3026486;
      } else {
         int c = ClientSettingsCache.getColor(uuid);
         return c == -1 ? fallback : c;
      }
   }

   public static int forViewer(int fallback) {
      LocalPlayer viewer = Minecraft.getInstance().player;
      return viewer == null ? fallback : forOwner(viewer.getUUID(), fallback);
   }
}
