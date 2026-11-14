package com.myriam.unrp.uo;



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class ColossusState {
   private static final float PI = (float)Math.PI;
   private static final float WALK_NORM = 0.216F;
   private static final float WALK_RATE = 0.28559935F;
   private static final float GALLOP_RATE = 0.48332196F;
   private static final float CLIMB_RATE = 2.4F;
   private static final int DISPLAY_LEN = 76;
   private static final int BEAT_START = 16;
   private static final int BEAT_END = 60;
   private static final float BEAT_PERIOD = 8.0F;
   private static final int ROAR_AT = 10;
   private static final int FORGET = 200;
   public static final double PULL = -0.045D;
   public static final double PLUMMET = -1.0D;
   public static final int FLY_AFTER = 4;
   public static final int FALL_AFTER = 6;
   public static final float CRUISE_NORM = 0.08F;
   public static final double FLY_VY = 0.12D;
   public static final float SLAM_DIVE = 0.6F;
   public static final float DIVE_FADE = 0.2F;
   public static final int SLAM_DUST = 28;
   public static final int SLAM_DEPTH = 3;
   private static final Map<Integer, ColossusState.State> LIVE = new HashMap();

   private ColossusState() {
   }

   private static float toward(float now, float want, float rate, float dt) {
      float d = want - now;
      float move = rate * dt;
      return Math.abs(d) <= move ? want : now + (d > 0.0F ? move : -move);
   }

   private static float sm(float a, float b, float x) {
      float t = Mth.clamp((x - a) / (b - a), 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   private static float scaleOf(Player player) {
      return Math.max(0.25F, player.getBbWidth() / 0.6F);
   }

   public static void forget(Player player) {
      LIVE.remove(player.getId());
   }

   public static void clearAll() {
      LIVE.clear();
   }

   public static void tick(Player player, boolean gilded) {
      ColossusState.State s = (ColossusState.State)LIVE.get(player.getId());
      if (s == null) {
         s = new ColossusState.State();
         LIVE.put(player.getId(), s);
         s.nextDisplay = 260 + Math.floorMod(player.getId() * 7919, 200);
         sweep(player.tickCount);
      }

      s.seen = player.tickCount;
      s.gilded = gilded;
      s.slamKick = Math.max(0.0F, s.slamKick - 0.09F);
      float scale = scaleOf(player);
      double dx = player.getX() - player.xo;
      double dz = player.getZ() - player.zo;
      float norm = (float)Math.sqrt(dx * dx + dz * dz) / scale;
      float cadence = norm < 0.004F ? 0.0F : Mth.clamp(Mth.sqrt(norm / 0.216F), 0.0F, 1.6F);
      float before = s.stride;
      s.strideO = s.stride;
      s.stride += cadence;
      if (s.stride > 100000.0F) {
         s.stride -= 100000.0F;
         s.strideO -= 100000.0F;
         before -= 100000.0F;
      }

      s.amountTarget = Mth.clamp(norm / 0.1F, 0.0F, 1.0F);
      float vy = (float)(player.getY() - player.yo) / scale;
      s.riseTarget = Mth.clamp(vy / 0.42F, -1.0F, 1.0F);
      s.climbO = s.climbPhase;
      if (player.onClimbable() && !player.onGround()) {
         s.climbPhase += Math.abs(vy) * 2.4F;
      }

      float body = player.yBodyRot;
      if (!Float.isNaN(s.lastBody)) {
         s.turnTarget = Mth.clamp(Mth.wrapDegrees(body - s.lastBody) / 11.0F, -1.0F, 1.0F);
      }

      s.lastBody = body;
      boolean air = !player.onGround() && !player.isInWater() && !player.onClimbable() && !player.isPassenger();
      if (air) {
         s.fallSpeed = Math.max(s.fallSpeed, -vy);
      }

      double rawVy;
      boolean flew;
      boolean var10001;
      label217: {
         label216: {
            rawVy = player.getY() - player.yo;
            boolean ballistic = air && !Double.isNaN(s.lastVy) && (rawVy - s.lastVy < -0.045D || rawVy < -1.0D);
            s.calm = air && !ballistic ? s.calm + 1 : 0;
            s.fall = ballistic ? s.fall + 1 : 0;
            s.lastVy = air ? rawVy : Double.NaN;
            flew = s.flying;
            if (air) {
               if (flew) {
                  if (s.fall < 6) {
                     break label216;
                  }
               } else if (s.calm >= 4) {
                  break label216;
               }
            }

            var10001 = false;
            break label217;
         }

         var10001 = true;
      }

      s.flying = var10001;
      s.cruiseTarget = Mth.clamp(norm / 0.08F, 0.0F, 1.0F);
      if (s.flying) {
         s.riseTarget = Mth.clamp((float)(rawVy / 0.12D), -1.0F, 1.0F);
         s.diveMax = Math.max(Math.max(0.0F, -s.riseTarget), s.diveMax - 0.2F);
      }

      if (s.wasAir && player.onGround()) {
         float hit = Mth.clamp(s.fallSpeed / 0.9F, 0.0F, 1.0F);
         s.kick = Math.max(s.kick, 0.3F + 0.7F * hit);
         sound(player, SoundEvents.RAVAGER_STEP, 0.45F + 0.75F * hit, (gilded ? 0.42F : 0.48F) + 0.05F * (1.0F - hit));
         if (flew && s.diveMax >= 0.6F) {
            s.slamKick = 1.0F;
            if (!player.isInvisible()) {
               slam(player, gilded);
            }
         }

         s.fallSpeed = 0.0F;
      }

      if (!air) {
         s.diveMax = 0.0F;
      }

      s.wasAir = air;
      boolean still = norm < 0.012F && player.onGround() && !player.isCrouching() && !player.swinging && player.hurtTime == 0 && !player.isInWater();
      s.idleTicks = still ? s.idleTicks + 1 : 0;
      if (s.displayAge < 0 && player.onGround() && !player.isInWater() && (s.entrance || s.idleTicks >= s.nextDisplay)) {
         s.displayAge = 0;
         ++s.displays;
         s.nextDisplay = 260 + Math.floorMod(player.getId() * 7919 + s.displays * 104729, 220);
      }

      s.entrance = false;
      if (s.displayAge >= 0) {
         int age = s.displayAge;
         if (age == 10) {
            sound(player, SoundEvents.RAVAGER_ROAR, 1.5F, gilded ? 0.78F : 0.66F);
         }

         if (age >= 16 && age < 60) {
            float a0 = (float)(age - 16) / 4.0F + 0.5F;
            float a1 = (float)(age + 1 - 16) / 4.0F + 0.5F;
            if (Mth.floor(a1) != Mth.floor(a0)) {
               sound(player, (SoundEvent)SoundEvents.NOTE_BLOCK_BASEDRUM.value(), 1.1F, (gilded ? 0.62F : 0.54F) + 0.06F * (float)(Mth.floor(a1) & 1));
            }
         }

         ++s.displayAge;
         boolean broken = age > 4 && (!player.onGround() || norm > 0.03F || player.swinging || player.isCrouching() || player.hurtTime > 0 || player.isInWater());
         if (s.displayAge >= 76 || broken) {
            s.displayAge = -1;
            s.idleTicks = 0;
         }
      }

      boolean ground = player.onGround() && !player.isInWater();
      if (ground && cadence > 0.0F) {
         boolean running = player.isSprinting();
         float rate = running ? 0.48332196F : 0.28559935F;
         float p0 = before * rate / (float)Math.PI;
         float p1 = s.stride * rate / (float)Math.PI;
         if (Mth.floor(p1) != Mth.floor(p0)) {
            float vol = (running ? 0.55F : 0.32F) * Math.min(1.0F, s.amountTarget + 0.2F);
            sound(player, SoundEvents.RAVAGER_STEP, vol, (gilded ? 0.5F : 0.58F) + 0.04F * (float)(Mth.floor(p1) & 1));
         }
      }

   }

   private static void slam(Player player, boolean gilded) {
      sound(player, SoundEvents.GENERIC_EXPLODE, 0.55F, gilded ? 0.5F : 0.56F);
      Level level = player.level();
      BlockPos at = BlockPos.containing(player.getX(), player.getY() - 0.2D, player.getZ());
      BlockState below = level.getBlockState(at);

      for(int i = 0; i < 3 && below.isAir(); ++i) {
         at = at.below();
         below = level.getBlockState(at);
      }

      if (!below.isAir()) {
         BlockParticleOption dust = new BlockParticleOption(ParticleTypes.BLOCK, below);
         float reach = scaleOf(player) * 0.9F;
         double top = (double)at.getY() + 1.1D;

         for(int i = 0; i < 28; ++i) {
            double a = (double)i * 0.2243994752564138D;
            double c = Math.cos(a);
            double n = Math.sin(a);
            level.addParticle(dust, player.getX() + c * (double)reach, top, player.getZ() + n * (double)reach, c * 0.25D, 0.18D, n * 0.25D);
         }

      }
   }

   private static void sound(Player player, SoundEvent ev, float volume, float pitch) {
      player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), ev, SoundSource.PLAYERS, volume, pitch, false);
   }

   public static ColossusState.Drive drive(Player player, float partial) {
      int key = ClientLbFormCache.stateKey(player.getId());
      ColossusState.State s = (ColossusState.State)LIVE.get(key);
      if (s == null) {
         s = new ColossusState.State();
         s.entrance = key == player.getId();
         LIVE.put(key, s);
      }

      float t = (float)player.tickCount + partial;
      if (t != s.last) {
         s.dt = Float.isNaN(s.last) ? 1.0F : Mth.clamp(t - s.last, 0.0F, 4.0F);
         s.last = t;
      }

      float dt = s.dt;
      ColossusState.Drive d = s.drive;
      d.time = t;
      float stride = Mth.lerp(partial, s.strideO, s.stride);
      d.walk = stride * 0.28559935F;
      d.gallop = stride * 0.48332196F;
      d.climbing = Mth.lerp(partial, s.climbO, s.climbPhase);
      d.amount = toward(d.amount, s.amountTarget, 0.12F, dt);
      d.run = toward(d.run, player.isSprinting() && !player.isInWater() ? 1.0F : 0.0F, 0.08F, dt);
      d.crouch = toward(d.crouch, player.isCrouching() ? 1.0F : 0.0F, 0.15F, dt);
      d.swim = toward(d.swim, player.isInWater() && !player.onGround() ? 1.0F : 0.0F, 0.1F, dt);
      d.dive = toward(d.dive, player.isVisuallySwimming() ? 1.0F : 0.0F, 0.08F, dt);
      boolean air = !player.onGround() && !player.isInWater() && !player.onClimbable() && !player.isPassenger();
      d.air = toward(d.air, air ? 1.0F : 0.0F, 0.22F, dt);
      d.rise = toward(d.rise, s.riseTarget, 0.2F, dt);
      d.climb = toward(d.climb, player.onClimbable() && !player.onGround() ? 1.0F : 0.0F, 0.12F, dt);
      d.land = Math.max(d.land, s.kick);
      s.kick = 0.0F;
      d.land = Math.max(0.0F, d.land - 0.085F * dt);
      d.fly = toward(d.fly, s.flying ? 1.0F : 0.0F, player.onGround() ? 0.3F : 0.1F, dt);
      d.cruise = toward(d.cruise, s.flying ? s.cruiseTarget : 0.0F, 0.08F, dt);
      d.slam = Math.max(d.slam, s.slamKick);
      s.slamKick = 0.0F;
      d.slam = Math.max(0.0F, d.slam - 0.09F * dt);
      d.idle = toward(d.idle, s.idleTicks > 6 ? 1.0F : 0.0F, 0.05F, dt);
      float envelope = 0.0F;
      float roar = 0.0F;
      if (s.displayAge >= 0) {
         float age = (float)s.displayAge + partial;
         envelope = sm(0.0F, 12.0F, age) * (1.0F - sm(62.0F, 76.0F, age));
         roar = sm(6.0F, 12.0F, age) * (1.0F - sm(26.0F, 36.0F, age));
         d.beat = Math.max(0.0F, age - 16.0F) * (float)Math.PI * 2.0F / 8.0F * (1.0F - sm(58.0F, 64.0F, age)) + ((float)Math.PI * 1.5F);
      }

      d.display = toward(d.display, envelope, 0.2F, dt);
      d.roar = toward(d.roar, roar, 0.25F, dt);
      d.strike = player.getAttackAnim(partial);
      boolean mainRight = player.getMainArm() == HumanoidArm.RIGHT;
      boolean main = player.swingingArm != InteractionHand.OFF_HAND;
      d.side = main == mainRight ? 1.0F : -1.0F;
      d.hurt = toward(d.hurt, player.hurtTime > 0 ? 1.0F : 0.0F, 0.25F, dt);
      d.turn = toward(d.turn, s.turnTarget, 0.07F, dt);
      float body = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
      float head = Mth.rotLerp(partial, player.yHeadRotO, player.yHeadRot);
      float wantYaw = Mth.wrapDegrees(head - body) * ((float)Math.PI / 180F) + d.idle * 0.24F * Mth.sin(t * 0.013F) * Mth.sin(t * 0.037F + 1.0F);
      float wantPitch = Mth.lerp(partial, player.xRotO, player.getXRot()) * ((float)Math.PI / 180F);
      if (Float.isNaN(s.yaw)) {
         s.yaw = wantYaw;
         s.pitch = wantPitch;
      }

      float follow = Math.min(1.0F, 0.3F * dt);
      s.yaw += (wantYaw - s.yaw) * follow;
      s.pitch += (wantPitch - s.pitch) * follow;
      d.yaw = s.yaw;
      d.pitch = s.pitch;
      float lid = (t + (float)Math.floorMod(player.getId() * 37, 97)) % 130.0F;
      d.blink = lid < 6.0F ? Mth.sin(lid / 6.0F * (float)Math.PI) : 0.0F;
      d.breath = Mth.sin(t * 0.07F);
      d.death = player.deathTime > 0 ? Math.min(1.0F, ((float)player.deathTime + partial) / 20.0F) : 0.0F;
      return d;
   }

   private static void sweep(int now) {
      if (LIVE.size() >= 12) {
         Iterator<Map.Entry<Integer, ColossusState.State>> it = LIVE.entrySet().iterator();

         while(it.hasNext()) {
            ColossusState.State v = (ColossusState.State)((Map.Entry)it.next()).getValue();
            if (Math.abs(now - v.seen) > 200) {
               it.remove();
            }
         }

      }
   }

   public static final class Drive {
      public float time;
      public float walk;
      public float gallop;
      public float climbing;
      public float amount;
      public float run;
      public float crouch;
      public float swim;
      public float dive;
      public float air;
      public float rise;
      public float climb;
      public float land;
      public float idle;
      public float display;
      public float beat;
      public float roar;
      public float strike;
      public float side;
      public float hurt;
      public float turn;
      public float yaw;
      public float pitch;
      public float blink;
      public float breath;
      public float death;
      public float fly;
      public float cruise;
      public float slam;
   }

   private static final class State {
      final ColossusState.Drive drive = new ColossusState.Drive();
      float last = Float.NaN;
      float dt = 1.0F;
      float stride;
      float strideO;
      float climbPhase;
      float climbO;
      float amountTarget;
      float riseTarget;
      float turnTarget;
      float lastBody = Float.NaN;
      float yaw = Float.NaN;
      float pitch = Float.NaN;
      float kick;
      float fallSpeed;
      int idleTicks;
      int displayAge = -1;
      int nextDisplay = 300;
      int displays;
      int seen;
      boolean wasAir;
      boolean entrance = true;
      boolean gilded;
      int calm;
      int fall;
      double lastVy = Double.NaN;
      boolean flying;
      float cruiseTarget;
      float diveMax;
      float slamKick;
   }
}
