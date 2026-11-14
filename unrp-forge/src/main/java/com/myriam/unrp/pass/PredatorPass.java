package com.myriam.unrp.pass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class PredatorPass {
   public static final String ACTIVE_TAG = "unrp.active.predator";
   public static final String FOCUS_TAG = "unrp.predator_focus_drain";
   public static final String TRANS_TAG = "unrp.predator_trans";
   public static final int FOCUS_SYNC = 20;
   public static final String[] STALE_TAGS = new String[]{"unrp.predator_focus_drain", "unrp.predator_trans", "unrp.predator_trans_drain", "unrp.pred_trans_off"};
   public static final String PROWL_TAG = "unrp.pd_prowl";
   public static final String STALK_TAG = "unrp.pd_stalk";
   public static final double MOVE_AT = 0.005D;
   public static final double JUMP_AT = 4.0D;
   public static final int STILL_AFTER = 6;
   static final Set<UUID> FOCUSED = new HashSet();
   static final Set<UUID> TRANSFORMED = new HashSet();
   static final Map<UUID, double[]> PACE = new HashMap();

   private PredatorPass() {
   }

   static boolean predator(ServerPlayer p) {
      return p.getTags().contains("unrp.active.predator");
   }

   public static PassFxPacket packet(int op, Entity caster, int target, int aux, int flags, Vec3 at, Vec3 vec, double v3) {
      double[] v = new double[]{at.x, at.y, at.z, vec != null ? vec.x : v3, vec != null ? vec.y : 0.0D, vec != null ? vec.z : 0.0D};
      int f = flags | (PassEvents.copy(caster) ? 1 : 0);
      return new PassFxPacket(op, caster == null ? 0 : caster.getId(), target, aux, f, PassEvents.color(caster), v);
   }

   public static void send(ServerLevel level, PassFxPacket pkt) {
      PassEvents.send(level, new double[]{(double)pkt.v[0], (double)pkt.v[1], (double)pkt.v[2]}, pkt);
   }

   public static void sendNear(ServerLevel level, PassFxPacket pkt, Vec3 a, Vec3 b) {
      double r = 72.0D;

      for(ServerPlayer o : level.players()) {
         if (o.distanceToSqr(a) <= r * r || o.distanceToSqr(b) <= r * r) {
            TimePass.to(o, pkt);
         }
      }

   }

   public static void seed(ServerPlayer p) {
      UUID id = p.getUUID();
      if (p.getTags().contains("unrp.predator_focus_drain")) {
         FOCUSED.add(id);
      }

      if (p.getTags().contains("unrp.predator_trans")) {
         TRANSFORMED.add(id);
      }

   }

   public static void forget(UUID id) {
      FOCUSED.remove(id);
      TRANSFORMED.remove(id);
      PACE.remove(id);
   }

   public static void clear() {
      FOCUSED.clear();
      TRANSFORMED.clear();
      PACE.clear();
   }

   public static void unpace(ServerPlayer p) {
      PACE.remove(p.getUUID());
      p.removeTag("unrp.pd_prowl");
      p.removeTag("unrp.pd_stalk");
   }

   public static void pace(ServerPlayer p, boolean focus) {
      if (!focus) {
         unpace(p);
      } else {
         UUID id = p.getUUID();
         double[] s = (double[])PACE.get(id);
         if (s == null) {
            PACE.put(id, new double[]{p.getX(), p.getZ(), 6.0D, 0.0D});
         } else {
            double dx = p.getX() - s[0];
            double dz = p.getZ() - s[1];
            s[0] = p.getX();
            s[1] = p.getZ();
            double d2 = dx * dx + dz * dz;
            if (d2 <= 16.0D) {
               if (d2 > 2.5E-5D) {
                  s[2] = 0.0D;
                  s[3] = 1.0D;
               } else if (++s[2] >= 6.0D) {
                  s[3] = 0.0D;
               }
            }

            boolean moving = s[3] > 0.5D;
            if (p.removeTag(moving ? "unrp.pd_prowl" : "unrp.pd_stalk")) {
               com.myriam.unrp.ability.Poses.stop(p);
            }

            p.addTag(moving ? "unrp.pd_stalk" : "unrp.pd_prowl");
         }
      }
   }

   static boolean stale(ServerPlayer p) {
      if (predator(p)) {
         return false;
      } else {
         boolean had = false;

         for(String tag : STALE_TAGS) {
            had |= p.removeTag(tag);
         }

         return had;
      }
   }

   public static void tell(ServerPlayer p, ServerLevel level, PassFxPacket pkt) {
      if (p.isInvisible()) {
         TimePass.to(p, pkt);
      } else {
         send(level, pkt);
      }

   }

   public static void watch(ServerPlayer p, ServerLevel level, double focusR) {
      UUID id = p.getUUID();
      boolean pd = predator(p);
      double reach = (double)Math.round(focusR);
      boolean focus = pd && p.getTags().contains("unrp.predator_focus_drain");
      if (focus && FOCUSED.add(id)) {
         tell(p, level, packet(35, p, 0, 0, 0, p.position(), (Vec3)null, reach));
      } else if (focus && p.tickCount % 20 == 0) {
         send(level, packet(35, p, 0, 0, 32, p.position(), (Vec3)null, reach));
      } else if (!focus) {
         FOCUSED.remove(id);
      }

      pace(p, focus);
      boolean trans = pd && p.getTags().contains("unrp.predator_trans");
      if (trans && TRANSFORMED.add(id)) {
         tell(p, level, packet(40, p, 0, 0, 0, p.position(), (Vec3)null, 0.0D));
         com.myriam.unrp.ability.Poses.play(p, "pd_roar");
      } else if (!trans) {
         TRANSFORMED.remove(id);
      }

   }

   public static void lock(ServerPlayer p, ServerLevel level, LivingEntity prey) {
      sendNear(level, packet(34, p, prey.getId(), 0, 8, prey.position(), (Vec3)null, 0.0D), prey.position(), p.position());
   }

   public static void beat(ServerPlayer p, ServerLevel level, double radius, boolean focusing) {
      send(level, packet(36, p, 0, (int)Math.round(radius), focusing ? 4 : 0, p.position(), (Vec3)null, radius));
   }

   public static void rush(ServerPlayer p, ServerLevel level, Vec3 dir, boolean aimed) {
      send(level, packet(37, p, 0, 0, aimed ? 8 : 0, p.position(), dir, 0.0D));
   }

   public static void rushHit(ServerPlayer p, ServerLevel level, LivingEntity t) {
      Vec3 away = new Vec3(t.getX() - p.getX(), 0.0D, t.getZ() - p.getZ());
      Vec3 dir = away.lengthSqr() > 1.0E-8D ? away.normalize() : Vec3.directionFromRotation(0.0F, p.getYRot());
      Vec3 mid = t.position().add(0.0D, (double)t.getBbHeight() * 0.5D, 0.0D);
      send(level, packet(38, p, t.getId(), 0, 8, mid, dir, 0.0D));
   }

   public static void breaker(ServerPlayer p, ServerLevel level, Vec3 start, Vec3 dir) {
      send(level, packet(39, p, 0, 0, 0, start, dir, 0.0D));
   }
}
