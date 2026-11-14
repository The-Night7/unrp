package com.myriam.unrp.pass;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public final class TimePass {
   public static final String CALL_TAG = "unrp.tm_call";
   public static final int FIELD_SYNC = 20;
   public static final int CALL_GAP = 20;
   static final Map<UUID, Long> CALLED = new HashMap();

   private TimePass() {
   }

   public static PassFxPacket packet(int op, Entity caster, int target, int aux, int flags, Vec3 at, Vec3 vec, double v3, double v4) {
      double[] v = new double[]{at.x, at.y, at.z, vec != null ? vec.x : v3, vec != null ? vec.y : v4, vec != null ? vec.z : 0.0D};
      int f = flags | (PassEvents.copy(caster) ? 1 : 0);
      return new PassFxPacket(op, caster == null ? 0 : caster.getId(), target, aux, f, PassEvents.color(caster), v);
   }

   public static void send(ServerLevel level, PassFxPacket pkt) {
      PassEvents.send(level, new double[]{(double)pkt.v[0], (double)pkt.v[1], (double)pkt.v[2]}, pkt);
   }

   public static void to(ServerPlayer watcher, PassFxPacket pkt) {
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> watcher), pkt);
   }

   public static void tracking(Entity about, PassFxPacket pkt) {
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> about), pkt);
   }

   public static void sendNear(ServerLevel level, PassFxPacket pkt, Vec3 a, Vec3 b) {
      double r = 72.0D;

      for(ServerPlayer o : level.players()) {
         if (o.distanceToSqr(a) <= r * r || o.distanceToSqr(b) <= r * r) {
            to(o, pkt);
         }
      }

   }

   public static void on(ServerPlayer p, ServerLevel level) {
      long now = level.getGameTime();
      Long last = (Long)CALLED.get(p.getUUID());
      if (last == null || now < last || now - last >= 20L) {
         CALLED.put(p.getUUID(), now);
         send(level, packet(22, p, 0, 0, 0, p.position(), (Vec3)null, 0.0D, 0.0D));
         com.myriam.unrp.ability.Poses.play(p, "tm_wind");
      }
   }

   public static void forget(UUID id) {
      CALLED.remove(id);
   }

   public static void flow(ServerPlayer p, ServerLevel level, boolean decel, double radius, boolean sync) {
      send(level, packet(23, p, 0, 0, (decel ? 128 : 0) | (sync ? 32 : 0), p.position(), (Vec3)null, radius, 0.0D));
   }

   public static void rewind(ServerPlayer caster, ServerPlayer target, ServerLevel level, int back) {
      send(level, packet(24, caster, target.getId(), back, 8, target.position(), (Vec3)null, 0.0D, 0.0D));
      com.myriam.unrp.ability.Poses.play(caster, "tm_rewind");
   }

   public static void blink(ServerPlayer p, ServerLevel level, Vec3 from, Vec3 to) {
      send(level, packet(25, p, 0, 0, 0, from, to, 0.0D, 0.0D));
      com.myriam.unrp.ability.Poses.play(p, "tm_blink");
   }

   public static PassFxPacket mark(ServerPlayer p, Vec3 pos, int left, boolean sync) {
      return packet(26, p, 0, left, sync ? 32 : 0, pos, (Vec3)null, 0.0D, 0.0D);
   }

   public static void unmark(ServerPlayer p, ServerLevel there, Vec3 pos) {
      PassFxPacket gone = packet(26, p, 0, 0, 0, pos, (Vec3)null, 0.0D, 0.0D);

      for(ServerPlayer o : there.players()) {
         to(o, gone);
      }

   }

   public static void recall(ServerPlayer p, ServerLevel level, ServerLevel there, Vec3 from, Vec3 to) {
      unmark(p, there, to);
      if (there == level) {
         sendNear(level, packet(27, p, 0, 0, 0, from, to, 0.0D, 0.0D), from, to);
      } else {
         send(level, packet(27, p, 0, 0, 0, from, from, 0.0D, 0.0D));
      }

      com.myriam.unrp.ability.Poses.play(p, "tm_blink");
   }

   public static void arrive(ServerPlayer p, ServerLevel there, Vec3 to) {
      send(there, packet(27, p, 0, 0, 0, to, to, 0.0D, 0.0D));
   }

   public static void freeze(ServerPlayer p, ServerLevel level, Vec3 center, double radius, int ticks) {
      send(level, packet(28, p, 0, 0, 0, center, (Vec3)null, radius, (double)ticks));
      com.myriam.unrp.ability.Poses.play(p, "tm_freeze");
   }

   public static PassFxPacket hold(Entity caster, Entity held, int ticks, boolean strike) {
      return packet(29, caster, held.getId(), Math.max(1, ticks), strike ? 8 : 0, held.position(), (Vec3)null, 0.0D, 0.0D);
   }

   public static void free(Entity held) {
      tracking(held, packet(30, (Entity)null, held.getId(), 0, 8, held.position(), (Vec3)null, 0.0D, 0.0D));
   }

   public static void strike(ServerPlayer p, ServerLevel level, Entity target, Vec3 from, Vec3 dest) {
      send(level, packet(31, p, target.getId(), 0, 8, from, dest, 0.0D, 0.0D));
      com.myriam.unrp.ability.Poses.play(p, "tm_strike");
   }

   public static PassFxPacket delay(Entity caster, Entity target, int left, boolean sync) {
      return packet(32, caster, target.getId(), Math.max(1, left), 8 | (sync ? 32 : 0), target.position(), (Vec3)null, 0.0D, 0.0D);
   }

   public static void landed(Entity caster, Entity target) {
      tracking(target, packet(33, caster, target.getId(), 0, 8, target.position(), (Vec3)null, 0.0D, 0.0D));
   }
}
