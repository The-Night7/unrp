package com.myriam.unrp.pass;

import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BarrierPass {
   public static final double SEARCH = 8.0D;
   public static final double BACK = 64.0D;
   public static final double AIM_NEAR = 0.5D;
   public static Vec3 AIM;
   static boolean taken;

   private BarrierPass() {
   }

   static double[] at(Vec3 v) {
      return new double[]{v.x, v.y, v.z};
   }

   static Vec3 mid(Entity e) {
      return new Vec3(e.getX(), e.getY() + (double)e.getBbHeight() / 2.0D, e.getZ());
   }

   static ServerPlayer owner(ServerLevel level, BarrierEntity b) {
      UUID id = b.getOwnerId();
      return id == null ? null : level.getServer().getPlayerList().getPlayer(id);
   }

   static int dress(Entity e, int flags) {
      return flags | (PassEvents.copy(e) ? 1 : 0);
   }

   public static void send(BarrierEntity b, int op, int target, int flags, double[] v) {
      Level lv_o = b.level();
      if (lv_o instanceof ServerLevel level) {
         ServerPlayer o = owner(level, b);
         PassEvents.send(level, at(b.center()), new PassFxPacket(op, o == null ? 0 : o.getId(), target, b.getId(), dress(o, flags), PassEvents.color(o), v));
      }

   }

   public static void sendBy(ServerPlayer p, double[] where, int op, int target, int aux, int flags, double[] v) {
      PassEvents.send(p.serverLevel(), where, new PassFxPacket(op, p.getId(), target, aux, dress(p, flags), PassEvents.color(p), v));
   }

   public static Vec3 hurt(LivingEntity le, DamageSource src, float amount, Vec3 face) {
      Vec3 was = AIM;
      boolean wasTaken = taken;
      AIM = face;
      taken = false;

      Vec3 var6;
      try {
         le.hurt(src, amount);
         var6 = face != null && taken ? face : null;
      } finally {
         AIM = was;
         taken = wasTaken;
      }

      return var6;
   }

   public static void hit(BarrierEntity b, DamageSource src, float amount) {
      Vec3 c = b.center();
      double r = (double)b.getSize() / 2.0D;
      boolean aimed = AIM != null && Math.abs(AIM.distanceTo(c) - r) < 0.5D;
      if (aimed) {
         taken = true;
      }

      Vec3 d = (aimed ? AIM : toward(c, r, src, b.getBoundingBox())).subtract(c);
      Vec3 n = d.lengthSqr() > 1.0E-8D ? d.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
      send(b, 13, b.getStage(), 0, new double[]{n.x, n.y, n.z, (double)amount, 0.0D, 0.0D});
   }

   static Vec3 toward(Vec3 c, double r, DamageSource src, AABB box) {
      Entity direct = src.getDirectEntity();
      Entity cause = src.getEntity();
      if (direct != null && direct != cause) {
         Vec3 p = mid(direct);
         Vec3 v = direct.getDeltaMovement();
         if (p.distanceToSqr(c) < r * r && v.lengthSqr() > 1.0E-6D) {
            Vec3 back = v.normalize().scale(-1.0D);
            double t = exit(p, back, c, r);
            if (t >= 0.0D) {
               return p.add(back.scale(t));
            }
         }

         return p;
      } else if (cause instanceof LivingEntity) {
         LivingEntity le = (LivingEntity)cause;
         Vec3 eye = le.getEyePosition();
         Vec3 look = le.getLookAngle();
         boolean in = eye.distanceToSqr(c) < r * r;
         double t = in ? exit(eye, look, c, r) : entry(eye, look, c, r);
         if (t >= 0.0D) {
            return eye.add(look.scale(t));
         } else {
            if (!in && crosses(eye, look, box)) {
               Vec3 q = eye.add(look.scale(Math.max(0.0D, c.subtract(eye).dot(look)))).subtract(c);
               if (q.lengthSqr() > 1.0E-8D) {
                  return c.add(q.normalize().scale(r));
               }
            }

            return eye;
         }
      } else {
         return cause != null ? mid(cause) : c.add(0.0D, r, 0.0D);
      }
   }

   static boolean crosses(Vec3 from, Vec3 dir, AABB box) {
      double[] o = new double[]{from.x, from.y, from.z};
      double[] d = new double[]{dir.x, dir.y, dir.z};
      double[] lo = new double[]{box.minX, box.minY, box.minZ};
      double[] hi = new double[]{box.maxX, box.maxY, box.maxZ};
      double near = 0.0D;
      double far = Double.MAX_VALUE;

      for(int k = 0; k < 3; ++k) {
         if (Math.abs(d[k]) < 1.0E-9D) {
            if (o[k] < lo[k] || o[k] > hi[k]) {
               return false;
            }
         } else {
            double a = (lo[k] - o[k]) / d[k];
            double b = (hi[k] - o[k]) / d[k];
            near = Math.max(near, Math.min(a, b));
            far = Math.min(far, Math.max(a, b));
         }
      }

      return near <= far;
   }

   static double entry(Vec3 from, Vec3 dir, Vec3 c, double r) {
      Vec3 oc = from.subtract(c);
      double b = oc.dot(dir);
      double disc = b * b - (oc.lengthSqr() - r * r);
      if (disc < 0.0D) {
         return -1.0D;
      } else {
         double t = -b - Math.sqrt(disc);
         return t > 0.0D ? t : -1.0D;
      }
   }

   static double exit(Vec3 from, Vec3 dir, Vec3 c, double r) {
      Vec3 oc = from.subtract(c);
      double b = oc.dot(dir);
      double disc = b * b - (oc.lengthSqr() - r * r);
      if (disc < 0.0D) {
         return -1.0D;
      } else {
         double t = -b + Math.sqrt(disc);
         return t >= 0.0D ? t : -1.0D;
      }
   }

   public static void end(BarrierEntity b, int op) {
      Vec3 c = b.center();
      double[] v = new double[]{c.x, c.y, c.z, (double)b.getSize() / 2.0D, 0.0D, 0.0D};
      int flags = 0;
      if (b.isRam()) {
         Level lv_o = b.level();
         if (lv_o instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)lv_o;
            ServerPlayer o = owner(level, b);
            if (o != null) {
               Vec3 f = o.getLookAngle();
               v[4] = Math.atan2(f.z, f.x);
               v[5] = Math.asin(Math.max(-1.0D, Math.min(1.0D, f.y)));
               flags = 4;
            }
         }
      }

      send(b, op, b.isRam() ? 1 : 0, flags, v);
   }

   public static void shatter(BarrierEntity b) {
      end(b, 14);
   }

   public static void gone(BarrierEntity b) {
      end(b, 15);
   }

   public static void deploy(ServerPlayer p, BarrierEntity b, Vec3 to, int flags) {
      sendBy(p, at(p.position()), 16, 0, b.getId(), flags, at(to));
   }

   public static void grow(ServerPlayer p, boolean shrink) {
      sendBy(p, at(p.position()), 17, 0, 0, shrink ? 128 : 0, at(p.position()));
   }

   public static void ram(ServerPlayer p, BarrierEntity shield) {
      sendBy(p, at(p.position()), 18, 0, shield.getId(), 0, at(p.position()));
   }

   public static void squeeze(BarrierEntity b, float before, List<LivingEntity> caught) {
      Vec3 c = b.center();
      double[] v = new double[]{c.x, c.y, c.z, (double)before / 2.0D, b.getY() + (double)before / 2.0D, (double)b.getSize() / 2.0D};
      if (caught.isEmpty()) {
         send(b, 19, 0, 0, v);
      }

      for(LivingEntity le : caught) {
         send(b, 19, le.getId(), 0, v);
      }

   }

   public static void crush(BarrierEntity b, LivingEntity le) {
      send(b, 20, le.getId(), 0, at(mid(le)));
   }

   public static void ramHit(ServerPlayer p, BarrierEntity shield, LivingEntity le, Vec3 look) {
      Vec3 m = mid(le);
      sendBy(p, at(m), 21, le.getId(), shield == null ? 0 : shield.getId(), 0, new double[]{m.x, m.y, m.z, look.x, look.y, look.z});
   }

   public static void reflect(BarrierEntity b, LivingEntity le) {
      Level lv_m = b.level();
      if (lv_m instanceof ServerLevel level) {
         Vec3 m = mid(le);
         Vec3 d = m.subtract(b.center());
         Vec3 n = d.lengthSqr() > 1.0E-8D ? d.normalize() : new Vec3(0.0D, 1.0D, 0.0D);
         ServerPlayer o = owner(level, b);
         PassEvents.send(level, at(m), new PassFxPacket(21, o == null ? 0 : o.getId(), le.getId(), b.getId(), dress(o, 0), PassEvents.color(o), new double[]{m.x, m.y, m.z, n.x, n.y, n.z}));
      }
   }

   public static Vec3 cut(ServerLevel level, Vec3 at, Vec3 vel, Entity victim, Entity attacker) {
      if (victim != null && attacker != null && attacker != victim && !(vel.lengthSqr() < 1.0E-8D)) {
         List<BarrierEntity> shells = level.getEntitiesOfClass(BarrierEntity.class, victim.getBoundingBox().inflate(8.0D), (s) -> s.isAlive() && s.isInside(victim));
         if (shells.isEmpty()) {
            return null;
         } else {
            for(BarrierEntity s : shells) {
               if (s.isInside(attacker) || attacker.getUUID().equals(s.getOwnerId())) {
                  return null;
               }
            }

            BarrierEntity s = (BarrierEntity)shells.get(0);
            Vec3 c = s.center();
            double r = (double)s.getSize() / 2.0D;
            Vec3 dir = vel.normalize();
            Vec3 from = at.subtract(dir.scale(64.0D));
            double t = entry(from, dir, c, r);
            if (t > 0.0D && t < 64.0D + vel.length() + 0.5D) {
               return from.add(dir.scale(t));
            } else {
               Vec3 d = at.subtract(c);
               return c.add((d.lengthSqr() > 1.0E-8D ? d.normalize() : dir.scale(-1.0D)).scale(r));
            }
         }
      } else {
         return null;
      }
   }
}
