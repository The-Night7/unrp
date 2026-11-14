package com.myriam.unrp.uo;

import com.myriam.unrp.anim.MoveAnim;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class CastFxEvents {
   public static final String CAST_PREFIX = "unrp.cast.";
   public static final String CAST_BIG_PREFIX = "unrp.cast2.";
   public static final String POSE_PREFIX = "unrp.pose.";
   public static final int CAST_TICKS = 12;
   public static final int HOLD_TICKS = 3;
   private static final Map<UUID, Map<String, Long>> ACTIVE = new HashMap();
   private static final Map<UUID, Map<String, long[]>> POSES = new HashMap();

   public static String baseOf(String powerPath) {
      String s = powerPath;
      if (powerPath.endsWith("_am") || powerPath.endsWith("_enh") || powerPath.endsWith("_syn")) {
         s = powerPath.substring(0, powerPath.lastIndexOf(95));
      }

      return s;
   }

   public static void cast(LivingEntity caster, String base, int ticks, boolean big) {
      if (caster instanceof ServerPlayer player) {
         if (base != null && !base.isEmpty()) {
            String tag = (big ? "unrp.cast2." : "unrp.cast.") + base;
            long now = player.serverLevel().getGameTime();
            player.addTag(tag);
            ((Map)ACTIVE.computeIfAbsent(player.getUUID(), (k) -> new HashMap())).put(tag, now + (long)Math.max(1, ticks));
         }
      }
   }

   public static SoundEvent sound(String id) {
      SoundEvent s = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation(id));
      return s != null ? s : SoundEvents.PLAYER_ATTACK_SWEEP;
   }

   public static void pose(LivingEntity caster, String key, int ticks) {
      poseLater(caster, key, 0, ticks);
   }

   public static void poseLater(LivingEntity caster, String key, int delay, int ticks) {
      if (caster instanceof ServerPlayer player) {
         if (key != null && !key.isEmpty()) {
            String tag = "unrp.pose." + key;
            long now = player.serverLevel().getGameTime();
            long start = now + (long)Math.max(0, delay);
            if (delay <= 0) {
               player.addTag(tag);
            }

            ((Map)POSES.computeIfAbsent(player.getUUID(), (k) -> new HashMap())).put(tag, new long[]{start, start + (long)Math.max(1, ticks)});
            MoveAnim.cue(player, key, Math.max(0, delay), Math.max(1, ticks));
         }
      }
   }

   public static void swing(LivingEntity caster, boolean both) {
      caster.swing(InteractionHand.MAIN_HAND, true);
      if (both) {
         caster.swing(InteractionHand.OFF_HAND, true);
      }

   }

   public static void strike(LivingEntity caster, String base, boolean big) {
      cast(caster, base, 12, big);
      swing(caster, big);
   }

   public static void hold(LivingEntity caster, String base, boolean big) {
      cast(caster, base, 3, big);
   }

   public static boolean casting(ServerPlayer player) {
      Map<String, Long> m = (Map)ACTIVE.get(player.getUUID());
      return m != null && !m.isEmpty();
   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END) {
         Player now = event.player;
         if (now instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)now;
            long var8 = player.serverLevel().getGameTime();
            Map<String, Long> m = (Map)ACTIVE.get(player.getUUID());
            if (m != null && !m.isEmpty()) {
               Iterator<Map.Entry<String, Long>> it = m.entrySet().iterator();

               while(it.hasNext()) {
                  Map.Entry<String, Long> e_0 = (Map.Entry)it.next();
                  if (var8 >= e_0.getValue()) {
                     player.removeTag((String)e_0.getKey());
                     it.remove();
                  }
               }

               if (m.isEmpty()) {
                  ACTIVE.remove(player.getUUID());
               }
            }

            Map<String, long[]> pm = (Map)POSES.get(player.getUUID());

            for(String tag : player.getTags()) {
               if (tag.startsWith("unrp.pose.") && (pm == null || !pm.containsKey(tag))) {
                  pm = (Map)POSES.computeIfAbsent(player.getUUID(), (k) -> new HashMap());
                  pm.put(tag, new long[]{var8, var8 + 12L});
               }
            }

            if (pm != null && !pm.isEmpty()) {
               Iterator<Map.Entry<String, long[]>> pit = pm.entrySet().iterator();

               while(pit.hasNext()) {
                  Map.Entry<String, long[]> e = (Map.Entry)pit.next();
                  if (var8 >= ((long[])e.getValue())[1]) {
                     player.removeTag((String)e.getKey());
                     pit.remove();
                  } else if (var8 >= ((long[])e.getValue())[0]) {
                     player.addTag((String)e.getKey());
                  }
               }

               if (pm.isEmpty()) {
                  POSES.remove(player.getUUID());
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      Player m_0 = event.getEntity();
      if (m_0 instanceof ServerPlayer player) {
         Map<String, Long> m = (Map)ACTIVE.remove(player.getUUID());
         if (m != null) {
            for(String tag_0 : m.keySet()) {
               player.removeTag(tag_0);
            }
         }

         Map<String, long[]> pm = (Map)POSES.remove(player.getUUID());
         if (pm != null) {
            for(String tag : pm.keySet()) {
               player.removeTag(tag);
            }
         }

      }
   }
}
