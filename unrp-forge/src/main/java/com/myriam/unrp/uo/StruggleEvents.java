package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class StruggleEvents {
   public static final int KIND_SAND = 0;
   public static final int KIND_WATER = 1;
   public static final int KIND_ICE = 2;
   public static final int KIND_GENERIC = 3;
   public static final int KIND_GRAVITY = 4;
   public static final int KIND_LASH = 5;
   public static final int KIND_SALT = 6;
   public static final int KIND_CHAIN = 7;
   public static final int MASH_GAP = 3;
   public static final int GRAVITY_TINT = 14477554;
   public static final int LASH_TINT = 5891826;
   public static final int SALT_TINT = 16052714;
   public static final int CHAIN_TINT = 10771711;
   private static final Map<UUID, StruggleEvents.Bind> binds = new HashMap();

   public static void begin(ServerPlayer victim, int presses, int kind, int maxTicks, Runnable onBreak) {
      begin(victim, presses, kind, maxTicks, 0.0D, 3, onBreak);
   }

   public static void begin(ServerPlayer victim, int presses, int kind, int maxTicks, double push, int gap, Runnable onBreak) {
      StruggleEvents.Bind b = new StruggleEvents.Bind();
      b.need = Math.max(1, presses);
      b.kind = kind;
      b.expires = victim.serverLevel().getGameTime() + (long)Math.max(20, maxTicks);
      b.push = Math.max(0.0D, push);
      b.gap = Math.max(1, gap);
      b.onBreak = onBreak;
      binds.put(victim.getUUID(), b);
      send(victim, b, true);
   }

   public static void end(ServerPlayer victim) {
      if (binds.remove(victim.getUUID()) != null) {
         send(victim, (StruggleEvents.Bind)null, false);
      }

   }

   public static void end(ServerPlayer victim, int kind) {
      StruggleEvents.Bind b = (StruggleEvents.Bind)binds.get(victim.getUUID());
      if (b != null && b.kind == kind) {
         end(victim);
      }

   }

   public static int kindOf(UUID victim) {
      StruggleEvents.Bind b = (StruggleEvents.Bind)binds.get(victim);
      return b == null ? -1 : b.kind;
   }

   public static boolean isBound(UUID victim) {
      return binds.containsKey(victim);
   }

   public static void mash(ServerPlayer victim) {
      StruggleEvents.Bind b = (StruggleEvents.Bind)binds.get(victim.getUUID());
      if (b != null) {
         long now = victim.serverLevel().getGameTime();
         if (now - b.lastMash >= (long)b.gap) {
            b.lastMash = now;
            ++b.done;
            ServerLevel level = victim.serverLevel();
            Vec3 at = victim.position().add(0.0D, (double)victim.getBbHeight() * 0.6D, 0.0D);
            int tint = b.kind == 0 ? 14594656 : (b.kind == 1 ? 6272767 : (b.kind == 2 ? 14087423 : (b.kind == 4 ? 14477554 : (b.kind == 5 ? 5891826 : (b.kind == 6 ? 16052714 : (b.kind == 7 ? 10771711 : 16777215))))));
            Fx.burst(level, Fx.glint(tint), at, 5, 0.4D, 0.06D);
            level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound(b.kind == 2 ? "minecraft:block.glass.hit" : (b.kind == 1 ? "minecraft:entity.generic.splash" : (b.kind == 4 ? "minecraft:block.amethyst_block.hit" : (b.kind != 5 && b.kind != 7 ? "minecraft:block.sand.hit" : "minecraft:block.chain.hit")))), SoundSource.PLAYERS, 0.6F, b.kind == 4 ? 0.8F + 0.6F * (float)Math.min(1.0D, b.done / (double)b.need) : 1.3F);
            if (b.done >= (double)b.need) {
               binds.remove(victim.getUUID());
               send(victim, (StruggleEvents.Bind)null, false);
               Fx.shell(level, Fx.glint(tint), at, 1.0D, 26, 0.22D);
               Fx.ring(level, tint, victim.getX(), victim.getY() + 0.1D, victim.getZ(), 1.5D, 10);
               level.playSound((Player)null, victim.blockPosition(), CastFxEvents.sound(b.kind == 4 ? "minecraft:block.amethyst_cluster.break" : (b.kind != 5 && b.kind != 7 ? (b.kind == 6 ? "minecraft:block.glass.break" : "minecraft:entity.zombie.break_wooden_door") : "minecraft:block.chain.break")), SoundSource.PLAYERS, 0.8F, 1.4F);
               victim.displayClientMessage(Component.literal("§aYou break free!"), true);
               if (b.onBreak != null) {
                  b.onBreak.run();
               }
            } else {
               send(victim, b, true);
            }

         }
      }
   }

   private static void send(ServerPlayer victim, StruggleEvents.Bind b, boolean active) {
      int progress = active && b != null ? Math.min(100, (int)(b.done * 100.0D / (double)b.need)) : 0;
      if (active && b != null) {
         if (progress == b.sent) {
            return;
         }

         b.sent = progress;
      }

      int kind = b == null ? 0 : b.kind;
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> victim), new StrugglePacket(active, kind, progress));
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         if (!binds.isEmpty()) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
               long now = server.overworld().getGameTime();
               List<UUID> unheld = null;
               Iterator<Map.Entry<UUID, StruggleEvents.Bind>> it = binds.entrySet().iterator();

               while(it.hasNext()) {
                  Map.Entry<UUID, StruggleEvents.Bind> e = (Map.Entry)it.next();
                  ServerPlayer victim_0 = server.getPlayerList().getPlayer((UUID)e.getKey());
                  if (victim_0 != null && victim_0.isAlive() && now < ((StruggleEvents.Bind)e.getValue()).expires) {
                     if (TimeWeft.everpresent(victim_0)) {
                        if (unheld == null) {
                           unheld = new ArrayList();
                        }

                        unheld.add((UUID)e.getKey());
                     } else if (((StruggleEvents.Bind)e.getValue()).push > 0.0D) {
                        hold(victim_0, (StruggleEvents.Bind)e.getValue(), now);
                     }
                  } else {
                     it.remove();
                     if (victim_0 != null) {
                        send(victim_0, (StruggleEvents.Bind)null, false);
                     }
                  }
               }

               if (unheld != null) {
                  for(UUID id : unheld) {
                     StruggleEvents.Bind b = (StruggleEvents.Bind)binds.remove(id);
                     ServerPlayer victim = server.getPlayerList().getPlayer(id);
                     if (victim != null) {
                        send(victim, (StruggleEvents.Bind)null, false);
                        victim.displayClientMessage(Component.literal("§5You're everywhere at once, so nothing can hold you."), true);
                     }

                     if (b != null && b.onBreak != null) {
                        b.onBreak.run();
                     }
                  }

               }
            }
         }
      }
   }

   private static void hold(ServerPlayer victim, StruggleEvents.Bind b, long now) {
      b.done = Math.max(0.0D, b.done - b.push);
      if (now % 2L == 0L) {
         send(victim, b, true);
      }

      if (b.kind == 4 && now % 10L == 0L) {
         victim.serverLevel().playSound((Player)null, victim.blockPosition(), CastFxEvents.sound("minecraft:block.respawn_anchor.ambient"), SoundSource.PLAYERS, 0.5F, 0.6F);
      }

   }

   @SubscribeEvent
   public static void onJump(LivingEvent.LivingJumpEvent event) {
      LivingEntity var2 = event.getEntity();
      if (var2 instanceof ServerPlayer victim) {
         if (binds.containsKey(victim.getUUID())) {
            mash(victim);
         }

      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player var2 = event.getEntity();
      if (var2 instanceof ServerPlayer player) {
         binds.remove(player.getUUID());
      }
   }

   private static class Bind {
      int need;
      double done;
      int kind;
      long expires;
      long lastMash;
      double push;
      int gap = 3;
      int sent = -1;
      Runnable onBreak;
   }
}
