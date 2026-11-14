package com.myriam.unrp.anim;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public final class MoveAnim {
   public static final boolean OWN = true;
   private static final Map<UUID, MoveAnim.Slots> ACTIVE = new HashMap();

   private MoveAnim() {
   }

   private static MoveAnim.Slots slots(ServerPlayer p) {
      MoveAnim.Slots s = (MoveAnim.Slots)ACTIVE.get(p.getUUID());
      if (s == null) {
         s = new MoveAnim.Slots();
         ACTIVE.put(p.getUUID(), s);
      }

      return s;
   }

   public static void play(ServerPlayer p, String key) {
      play(p, key, 0, false);
   }

   public static void play(ServerPlayer p, String key, boolean own) {
      if (!own) {
         play(p, key);
      } else {
         int id = MoveClipIds.id(key);
         if (p != null && id >= 0 && !MoveClipIds.LOOPS[id]) {
            com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new MoveAnimPacket(p.getId(), 0, id, 0, 0));
         }
      }
   }

   public static void play(ServerPlayer p, String key, int delay, boolean mirror) {
      int id = MoveClipIds.id(key);
      if (p != null && id >= 0) {
         long now = p.serverLevel().getGameTime();
         MoveAnim.Active a = new MoveAnim.Active();
         a.clip = id;
         a.start = now + (long)Math.max(0, delay);
         a.mirror = mirror;
         if (MoveClipIds.LOOPS[id]) {
            a.held = true;
            a.holdUntil = a.start + 2L;
            slots(p).base = a;
         } else {
            slots(p).over = a;
         }

         send(p, new MoveAnimPacket(p.getId(), 0, id, -Math.max(0, delay), flags(a)));
      }
   }

   public static void hold(ServerPlayer p, String key, int ticks) {
      int id = MoveClipIds.id(key);
      if (p != null && id >= 0) {
         holdFrom(p, id, 0, ticks, false);
      }
   }

   public static void cue(ServerPlayer p, String key, int delay, int ticks) {
      int id = MoveClipIds.id(key);
      if (p != null && id >= 0) {
         if (MoveClipIds.LOOPS[id]) {
            holdFrom(p, id, Math.max(0, delay), ticks, false);
         } else {
            MoveAnim.Slots s = (MoveAnim.Slots)ACTIVE.get(p.getUUID());
            long now = p.serverLevel().getGameTime();
            if (s == null || s.over == null || s.over.clip != id || now + (long)Math.max(0, delay) - s.over.start >= (long)Math.max(1, MoveClipIds.RETRIGGER[id])) {
               play(p, key, delay, false);
            }
         }
      }
   }

   private static void holdFrom(ServerPlayer p, int id, int delay, int ticks, boolean yield) {
      long now = p.serverLevel().getGameTime();
      MoveAnim.Slots s = slots(p);
      if (s.base != null && s.base.clip == id && s.base.held) {
         s.base.holdUntil = Math.max(s.base.holdUntil, now + (long)delay + (long)Math.max(1, ticks));
      } else if (!yield || s.base == null || !s.base.held || now >= s.base.holdUntil) {
         MoveAnim.Active a = new MoveAnim.Active();
         a.clip = id;
         a.start = now + (long)delay;
         a.held = MoveClipIds.LOOPS[id];
         a.holdUntil = a.start + (long)Math.max(1, ticks);
         s.base = a;
         send(p, new MoveAnimPacket(p.getId(), 0, id, -delay, flags(a)));
      }
   }

   public static void release(ServerPlayer p, String key) {
      MoveAnim.Slots s = (MoveAnim.Slots)ACTIVE.get(p.getUUID());
      if (s != null && s.base != null && s.base.held) {
         int id = key == null ? s.base.clip : MoveClipIds.id(key);
         if (s.base.clip == id) {
            long now = p.serverLevel().getGameTime();
            releaseNow(p, s.base, now);
         }
      }
   }

   private static void releaseNow(ServerPlayer p, MoveAnim.Active a, long now) {
      a.held = false;
      a.start = now - (long)MoveClipIds.LOOP_END[a.clip];
      send(p, new MoveAnimPacket(p.getId(), 1, a.clip, 0, a.mirror ? 2 : 0));
   }

   public static void stop(ServerPlayer p) {
      if (ACTIVE.remove(p.getUUID()) != null) {
         send(p, new MoveAnimPacket(p.getId(), 2, -1, 0, 0));
      }

   }

   public static void stop(ServerPlayer p, String key) {
      int id = MoveClipIds.id(key);
      if (p != null && id >= 0) {
         MoveAnim.Slots s = (MoveAnim.Slots)ACTIVE.get(p.getUUID());
         if (s != null) {
            boolean hit = false;
            if (s.base != null && s.base.clip == id) {
               s.base = null;
               hit = true;
            }

            if (s.over != null && s.over.clip == id) {
               s.over = null;
               hit = true;
            }

            if (hit) {
               send(p, new MoveAnimPacket(p.getId(), 2, id, 0, 0));
            }

         }
      }
   }

   public static int holding(ServerPlayer p) {
      MoveAnim.Slots s = (MoveAnim.Slots)ACTIVE.get(p.getUUID());
      return s != null && s.base != null && s.base.held ? s.base.clip : -1;
   }

   public static int playing(ServerPlayer p) {
      MoveAnim.Slots s = (MoveAnim.Slots)ACTIVE.get(p.getUUID());
      return s != null && !over(s.over, p.serverLevel().getGameTime()) ? s.over.clip : -1;
   }

   private static int flags(MoveAnim.Active a) {
      return (a.held ? 1 : 0) | (a.mirror ? 2 : 0);
   }

   private static void send(ServerPlayer p, MoveAnimPacket msg) {
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p), msg);
   }

   private static boolean over(MoveAnim.Active a, long now) {
      return a == null || !a.held && now >= a.start + (long)MoveClipIds.LENGTH[a.clip];
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         MinecraftServer server = event.getServer();
         if (MoveClipIds.HOLD_TAG.length > 0) {
            for(ServerPlayer p : server.getPlayerList().getPlayers()) {
               for(int i = 0; i < MoveClipIds.HOLD_TAG.length; ++i) {
                  if (p.getTags().contains(MoveClipIds.HOLD_TAG[i])) {
                     holdFrom(p, MoveClipIds.HOLD_CLIP[i], 0, 3, true);
                  }
               }
            }
         }

         if (!ACTIVE.isEmpty()) {
            Iterator<Map.Entry<UUID, MoveAnim.Slots>> it = ACTIVE.entrySet().iterator();

            while(it.hasNext()) {
               Map.Entry<UUID, MoveAnim.Slots> e = (Map.Entry)it.next();
               ServerPlayer p = server.getPlayerList().getPlayer((UUID)e.getKey());
               if (p == null) {
                  it.remove();
               } else {
                  long now = p.serverLevel().getGameTime();
                  MoveAnim.Slots s = (MoveAnim.Slots)e.getValue();
                  if (s.base != null && s.base.held && now >= s.base.holdUntil) {
                     releaseNow(p, s.base, now);
                  }

                  if (over(s.base, now)) {
                     s.base = null;
                  }

                  if (over(s.over, now)) {
                     s.over = null;
                  }

                  if (s.base == null && s.over == null) {
                     it.remove();
                  }
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onTrack(PlayerEvent.StartTracking event) {
      Player pl0 = event.getEntity();
      if (pl0 instanceof ServerPlayer watcher) {
         Entity tg0 = event.getTarget();
         if (tg0 instanceof ServerPlayer seen) {
            com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> watcher), new MoveAnimPacket(seen.getId(), 2, -1, 0, 0));
            MoveAnim.Slots s = (MoveAnim.Slots)ACTIVE.get(seen.getUUID());
            if (s != null) {
               long now = seen.serverLevel().getGameTime();

               for(MoveAnim.Active a : new MoveAnim.Active[]{s.base, s.over}) {
                  if (a != null && !over(a, now)) {
                     MoveAnimPacket msg = new MoveAnimPacket(seen.getId(), 0, a.clip, (int)(now - a.start), flags(a));
                     com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> watcher), msg);
                  }
               }

            }
         }
      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      ACTIVE.remove(event.getEntity().getUUID());
   }

   @SubscribeEvent
   public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
      ACTIVE.remove(event.getEntity().getUUID());
   }

   @SubscribeEvent
   public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
      ACTIVE.remove(event.getEntity().getUUID());
   }

   static final class Active {
      int clip;
      long start;
      boolean held;
      long holdUntil;
      boolean mirror;
   }

   static final class Slots {
      MoveAnim.Active base;
      MoveAnim.Active over;
   }
}
