package com.myriam.unrp.deal;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

public final class Fx {
   public static final int WATER = 6272767;
   public static final int FROST = 14087423;
   public static final int SHADOW = 5909416;
   public static final int STORM = 9427199;
   public static final int DREAD = 4923246;
   public static final int SPARK = 16769162;
   public static final int STEEL = 12109008;
   public static final int WHITE = 16777215;
   public static final double FAR = 160.0D;

   private Fx() {
   }

   public static TintedParticleOptions of(RegistryObject<TintedParticleType> type, int rgb) {
      return new TintedParticleOptions((ParticleType)type.get(), rgb);
   }

   public static TintedParticleOptions drop(int rgb) {
      return of(PowerParticles.WATER_DROP, rgb);
   }

   public static TintedParticleOptions shard(int rgb) {
      return of(PowerParticles.FROST_SHARD, rgb);
   }

   public static TintedParticleOptions ember(int rgb) {
      return of(PowerParticles.SHADOW_EMBER, rgb);
   }

   public static TintedParticleOptions arc(int rgb) {
      return of(PowerParticles.STORM_ARC, rgb);
   }

   public static TintedParticleOptions wisp(int rgb) {
      return of(PowerParticles.DREAD_WISP, rgb);
   }

   public static TintedParticleOptions flare(int rgb) {
      return of(PowerParticles.ENERGY_FLARE, rgb);
   }

   public static TintedParticleOptions gale(int rgb) {
      return of(PowerParticles.GALE_STREAK, rgb);
   }

   public static TintedParticleOptions glint(int rgb) {
      return of(PowerParticles.STEEL_GLINT, rgb);
   }

   public static TintedParticleOptions wave(int rgb) {
      return of(PowerParticles.PUNCH_WAVE, rgb);
   }

   public static TintedParticleOptions filament(int rgb) {
      return of(PowerParticles.VOID_FILAMENT, rgb);
   }

   public static TintedParticleOptions script(int rgb) {
      return of(PowerParticles.SCRIPT_GLYPH, rgb);
   }

   public static TintedParticleOptions vein(int rgb) {
      return of(PowerParticles.AMBER_VEIN, rgb);
   }

   public static TintedParticleOptions rewind(int rgb) {
      return of(PowerParticles.REWIND_DASH, rgb);
   }

   public static TintedParticleOptions flake(int rgb) {
      return of(PowerParticles.EDGE_FLAKE, rgb);
   }

   public static TintedParticleOptions press(int rgb) {
      return of(PowerParticles.PRESSURE_STREAK, rgb);
   }

   public static TintedParticleOptions ink(int rgb) {
      return of(PowerParticles.PACT_INK, rgb);
   }

   public static TintedParticleOptions soulflame(int rgb) {
      return of(PowerParticles.SOUL_FLAME, rgb);
   }

   public static TintedParticleOptions heart(int rgb) {
      return of(PowerParticles.PACT_HEART, rgb);
   }

   public static int aura(Entity user, int fallback) {
      int c = com.myriam.unrp.pass.PassServer.color(user);
      return c == 16777215 ? fallback : c;
   }

   public static int mix(int a, int b, float t) {
      int ar = a >> 16 & 255;
      int ag = a >> 8 & 255;
      int ab = a & 255;
      int br = b >> 16 & 255;
      int bg = b >> 8 & 255;
      int bb = b & 255;
      int r = Math.round((float)ar + (float)(br - ar) * t);
      int g = Math.round((float)ag + (float)(bg - ag) * t);
      int bl = Math.round((float)ab + (float)(bb - ab) * t);
      return r << 16 | g << 8 | bl;
   }

   public static void far(ServerLevel level, ParticleOptions p, Vec3 at, int count, double spread, double speed) {
      for(ServerPlayer viewer : level.players()) {
         if (viewer.distanceToSqr(at) <= 25600.0D) {
            level.sendParticles(viewer, p, true, at.x, at.y, at.z, count, spread, spread, spread, speed);
         }
      }

   }

   public static void farLine(ServerLevel level, ParticleOptions p, Vec3 from, Vec3 to, double step, double jitter) {
      Vec3 d = to.subtract(from);
      double len = d.length();
      if (!(len < 1.0E-4D)) {
         Vec3 unit = d.scale(1.0D / len);
         int n = Math.max(1, (int)(len / step));

         for(ServerPlayer viewer : level.players()) {
            if (!(viewer.distanceToSqr(from) > 25600.0D) || !(viewer.distanceToSqr(to) > 25600.0D)) {
               for(int i = 0; i <= n; ++i) {
                  Vec3 at = from.add(unit.scale((double)i * step));
                  level.sendParticles(viewer, p, true, at.x, at.y, at.z, 1, jitter, jitter, jitter, 0.0D);
               }
            }
         }

      }
   }

   public static void burst(ServerLevel level, ParticleOptions p, Vec3 at, int count, double spread, double speed) {
      level.sendParticles(p, at.x, at.y, at.z, count, spread, spread, spread, speed);
   }

   public static void burst(ServerLevel level, ParticleOptions p, double x, double y, double z, int count, double spread, double speed) {
      level.sendParticles(p, x, y, z, count, spread, spread, spread, speed);
   }

   public static void ring(ServerLevel level, int rgb, Vec3 at, double radius, int life) {
      level.sendParticles(of(PowerParticles.SPARK_RING, rgb), at.x, at.y, at.z, 0, radius, (double)life, 0.0D, 1.0D);
   }

   public static void ring(ServerLevel level, int rgb, double x, double y, double z, double radius, int life) {
      level.sendParticles(of(PowerParticles.SPARK_RING, rgb), x, y, z, 0, radius, (double)life, 0.0D, 1.0D);
   }

   public static void shell(ServerLevel level, ParticleOptions p, Vec3 center, double radius, int count, double outward) {
      for(int i = 0; i < count; ++i) {
         double u = level.random.nextDouble() * 2.0D - 1.0D;
         double ang = level.random.nextDouble() * Math.PI * 2.0D;
         double h = Math.sqrt(Math.max(0.0D, 1.0D - u * u));
         Vec3 dir = new Vec3(h * Math.cos(ang), u, h * Math.sin(ang));
         Vec3 at = center.add(dir.scale(radius));
         level.sendParticles(p, at.x, at.y, at.z, 0, dir.x * outward, dir.y * outward, dir.z * outward, 1.0D);
      }

   }

   public static void circle(ServerLevel level, ParticleOptions p, Vec3 center, double radius, int count, double rise, double outward) {
      for(int i = 0; i < count; ++i) {
         double ang = (Math.PI * 2D) * (double)i / (double)count + level.random.nextDouble() * 0.2D;
         double cx = Math.cos(ang);
         double cz = Math.sin(ang);
         level.sendParticles(p, center.x + cx * radius, center.y, center.z + cz * radius, 0, cx * outward, rise, cz * outward, 1.0D);
      }

   }

   public static void line(ServerLevel level, ParticleOptions p, Vec3 from, Vec3 to, double step, double jitter, double drift) {
      Vec3 d = to.subtract(from);
      double len = d.length();
      if (!(len < 1.0E-4D)) {
         Vec3 unit = d.scale(1.0D / len);
         int n = Math.max(1, (int)(len / step));

         for(int i = 0; i <= n; ++i) {
            Vec3 at = from.add(unit.scale((double)i * step));
            double jx = (level.random.nextDouble() - 0.5D) * jitter;
            double jy = (level.random.nextDouble() - 0.5D) * jitter;
            double jz = (level.random.nextDouble() - 0.5D) * jitter;
            level.sendParticles(p, at.x + jx, at.y + jy, at.z + jz, 0, (level.random.nextDouble() - 0.5D) * drift, (level.random.nextDouble() - 0.5D) * drift, (level.random.nextDouble() - 0.5D) * drift, 1.0D);
         }

      }
   }

   public static void cone(ServerLevel level, ParticleOptions p, Vec3 origin, Vec3 dir, double halfAngle, int count, double speed) {
      Vec3 f = dir.normalize();
      Vec3 up = Math.abs(f.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
      Vec3 right = f.cross(up).normalize();
      Vec3 top = right.cross(f).normalize();

      for(int i = 0; i < count; ++i) {
         double a = level.random.nextDouble() * Math.PI * 2.0D;
         double t = Math.tan(halfAngle) * level.random.nextDouble();
         Vec3 v = f.add(right.scale(Math.cos(a) * t)).add(top.scale(Math.sin(a) * t)).normalize().scale(speed * (0.6D + level.random.nextDouble() * 0.4D));
         level.sendParticles(p, origin.x, origin.y, origin.z, 0, v.x, v.y, v.z, 1.0D);
      }

   }
}
