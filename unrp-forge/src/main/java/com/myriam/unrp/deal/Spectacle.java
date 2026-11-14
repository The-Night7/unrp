package com.myriam.unrp.deal;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.slf4j.Logger;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public final class Spectacle {
   public static final int MAX_PENDING = 4096;
   private static final Logger LOG = LogUtils.getLogger();
   private static final List<Object[]> PENDING = new ArrayList();
   private static long clock = 0L;

   private Spectacle() {
   }

   public static void after(int ticks, Runnable job) {
      if (ticks <= 0) {
         job.run();
      } else if (PENDING.size() < 4096) {
         PENDING.add(new Object[]{clock + (long)ticks, job});
      }
   }

   public static int pending() {
      return PENDING.size();
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         ++clock;
         if (!PENDING.isEmpty()) {
            List<Runnable> due = new ArrayList();
            Iterator<Object[]> it = PENDING.iterator();

            while(it.hasNext()) {
               Object[] job_0 = it.next();
               if ((Long)job_0[0] <= clock) {
                  due.add((Runnable)job_0[1]);
                  it.remove();
               }
            }

            for(Runnable job : due) {
               try {
                  job.run();
               } catch (RuntimeException var6) {
                  LOG.error("[unrp] a delayed power effect failed and was dropped", var6);
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent event) {
      PENDING.clear();
      clock = 0L;
   }

   public static void shockwave(ServerLevel level, int rgb, Vec3 at, double radius) {
      Fx.ring(level, rgb, at.x, at.y + 0.08D, at.z, radius, 14);
      Fx.ring(level, Fx.mix(rgb, 16777215, 0.45F), at.x, at.y + 0.12D, at.z, radius * 0.6D, 10);
      int n = (int)Math.min(90.0D, 16.0D + radius * 8.0D);
      Fx.circle(level, Fx.ember(rgb), at.add(0.0D, 0.15D, 0.0D), Math.max(0.4D, radius * 0.25D), n, 0.04D, 0.12D + radius * 0.05D);
      debris(level, at, radius, (int)Math.min(40.0D, 6.0D + radius * 4.0D));
   }

   public static void debris(ServerLevel level, Vec3 at, double radius, int count) {
      BlockState ground = level.getBlockState(BlockPos.containing(at.x, at.y - 0.2D, at.z));
      if (!ground.isAir()) {
         level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.1D, at.z, count, radius * 0.35D, 0.1D, radius * 0.35D, 0.15D);
      }
   }

   public static void pillar(ServerLevel level, ParticleOptions p, Vec3 base, double height) {
      Fx.line(level, p, base, base.add(0.0D, height, 0.0D), 0.22D, 0.18D, 0.02D);
      Fx.burst(level, p, base.add(0.0D, height, 0.0D), 6, 0.2D, 0.05D);
   }

   public static void slash(ServerLevel level, ParticleOptions p, Vec3 c, double size, double yawDeg) {
      double r = Math.toRadians(yawDeg);
      Vec3 side = (new Vec3(Math.cos(r), 0.0D, Math.sin(r))).scale(size);
      Vec3 up = new Vec3(0.0D, size, 0.0D);
      Fx.line(level, p, c.add(side).add(up), c.subtract(side).subtract(up), 0.12D, 0.02D, 0.0D);
      Fx.line(level, p, c.subtract(side).add(up), c.add(side).subtract(up), 0.12D, 0.02D, 0.0D);
   }

   public static void spikes(ServerLevel level, ParticleOptions p, Vec3 c, int count, double radius, double height) {
      for(int i = 0; i < count; ++i) {
         double a = (Math.PI * 2D) * (double)i / (double)count + level.random.nextDouble() * 0.3D;
         Vec3 dir = new Vec3(Math.cos(a), 0.0D, Math.sin(a));
         Vec3 from = c.add(dir.scale(radius * 0.25D));
         Vec3 to = c.add(dir.scale(radius)).add(0.0D, height * (0.6D + level.random.nextDouble() * 0.4D), 0.0D);
         Fx.line(level, p, from, to, 0.25D, 0.05D, 0.0D);
      }

   }

   public static void warn(ServerLevel level, int rgb, Vec3 at, double radius) {
      Fx.circle(level, Fx.ember(rgb), at.add(0.0D, 0.1D, 0.0D), radius, (int)Math.min(64.0D, 12.0D + radius * 6.0D), 0.0D, 0.0D);
      Fx.ring(level, rgb, at.x, at.y + 0.05D, at.z, radius, 20);
   }
}
