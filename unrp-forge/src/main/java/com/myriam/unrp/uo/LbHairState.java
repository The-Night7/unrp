package com.myriam.unrp.uo;



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public final class LbHairState {
   public static final float WILD_RATE = 0.09F;
   private static final float DEG = ((float)Math.PI / 180F);
   private static final float GROW_UP = 0.11F;
   private static final float GROW_DOWN = 0.3F;
   private static final float BLEND_TICKS = 14.0F;
   private static final int FORGET = 600;
   public final float[] phys = new float[4648];
   public final float[] grow = new float[581];
   public final float[] inputs = new float[9];
   public final float[] color = new float[3];
   private final float[] from = new float[3];
   private final float[] to = new float[3];
   public float shown;
   public float thickRisen = 0.86F;
   public float thickMane = 0.92F;
   public float swell;
   public float charge;
   public float flare;
   public float wild;
   private float wildTarget;
   private boolean wildWanted;
   private int target = -1;
   private float blend = 1.0F;
   private float last = Float.NaN;
   private float lastYaw = Float.NaN;
   private float spin;
   private int seen;
   private static final Map<Integer, LbHairState> LIVE = new HashMap();

   private LbHairState() {
   }

   public static LbHairState of(AbstractClientPlayer player) {
      int key = ClientLbFormCache.stateKey(player.getId());
      LbHairState s = (LbHairState)LIVE.get(key);
      if (s == null) {
         s = new LbHairState();
         LIVE.put(key, s);
         sweep(player.tickCount);
      }

      s.seen = player.tickCount;
      return s;
   }

   public static void clearAll() {
      LIVE.clear();
   }

   private static void sweep(int now) {
      if (LIVE.size() >= 16) {
         Iterator<Map.Entry<Integer, LbHairState>> it = LIVE.entrySet().iterator();

         while(it.hasNext()) {
            if (Math.abs(now - ((LbHairState)((Map.Entry)it.next()).getValue()).seen) > 600) {
               it.remove();
            }
         }

      }
   }

   private static float backOut(float x) {
      float y = x - 1.0F;
      return 1.0F + 2.70158F * y * y * y + 1.70158F * y * y;
   }

   private static float smooth(float x) {
      float t = Mth.clamp(x, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   public static float growthOf(int i, float shown) {
      float rank = (float)LbHairGeometry.RANK[i];
      float t = (shown - (rank - 1.0F) - LbHairGeometry.STAGGER[i] * 0.8F) / 0.55F;
      float up = Math.max(backOut(Mth.clamp(t, 0.0F, 1.0F)), 1.0F - Math.min(1.0F, rank));
      float leave = smooth((shown - ((float)LbHairGeometry.UNTIL[i] - 1.0F)) / 0.6F);
      return up * (1.0F - leave);
   }

   public static float growthStyled(int i, float shown, float wild) {
      float rank = (float)LbHairGeometry.RANK[i];
      float primal = Mth.clamp(rank - 6.0F, 0.0F, 1.0F);
      float ladder = Mth.clamp(rank - 1.0F, 0.0F, 1.0F) * (1.0F - primal);
      float k = smooth(wild);
      float own = growthOf(i, shown + 5.0F * primal);
      return own * (1.0F - ladder * k) * (1.0F - primal * (1.0F - k));
   }

   public static float wildStep(float wild, float want, float shown, float dt) {
      float hidden = Mth.clamp((1.0F - shown) * 1000.0F, 0.0F, 1.0F);
      float cap = 0.09F * dt;
      float step = Mth.clamp(want - wild, -cap, cap);
      return wild + step + hidden * (want - wild - step);
   }

   public void style(boolean wild) {
      this.wildWanted = wild;
   }

   public static float[] wind(Level level, BlockPos at, float partial) {
      float t = (float)level.getGameTime() + partial;
      float dir = t * 4.7E-4F + 1.3F * Mth.sin(t * 9.3E-4F);
      float gust = 0.5F + 0.5F * Mth.sin(t * 0.041F + 1.7F * Mth.sin(t * 0.013F));
      gust = gust * gust * gust;
      float calm = 0.55F + 0.45F * Mth.sin(t * 0.0027F);
      float storm = 1.0F + level.getRainLevel(partial) * 0.9F + level.getThunderLevel(partial) * 1.2F;
      float open = level.canSeeSky(at) ? 1.0F : 0.18F;
      float speed = (0.05F + 0.07F * calm + 0.16F * gust) * storm * open;
      return new float[]{Mth.cos(dir) * speed, Mth.sin(dir) * speed};
   }

   public void update(AbstractClientPlayer player, int tier, float[] rgb, boolean charging, boolean auraOn, boolean formed, float partial) {
      float time = (float)player.tickCount + partial;
      boolean first = Float.isNaN(this.last);
      float dt = first ? 0.0F : Mth.clamp(time - this.last, 0.0F, 3.0F);
      this.last = time;
      if (first) {
         this.shown = (float)tier;
         this.target = tier;

         for(int c = 0; c < 3; ++c) {
            this.color[c] = rgb[c];
            this.from[c] = rgb[c];
            this.to[c] = rgb[c];
         }
      }

      if (tier != this.target || rgb[0] != this.to[0] || rgb[1] != this.to[1] || rgb[2] != this.to[2]) {
         if (tier > this.target) {
            this.flare = 1.0F;
            LbHairPhysics.kick(this.phys, 2.0F);
         }

         this.target = tier;

         for(int c = 0; c < 3; ++c) {
            this.from[c] = this.color[c];
            this.to[c] = rgb[c];
         }

         this.blend = 0.0F;
      }

      if (tier > 0) {
         float want = this.wildWanted ? 1.0F : 0.0F;
         if (want != this.wildTarget && !first && this.shown >= 1.0F) {
            this.flare = 1.0F;
            LbHairPhysics.kick(this.phys, 2.0F);
         }

         this.wildTarget = want;
      }

      this.wild = first ? this.wildTarget : wildStep(this.wild, this.wildTarget, this.shown, dt);
      if (this.shown < (float)this.target) {
         this.shown = Math.max(this.shown, this.target >= 2 ? 1.0F : 0.0F);
         this.shown = Math.min((float)this.target, this.shown + 0.11F * dt);
      } else if (this.shown > (float)this.target) {
         this.shown = Math.max((float)this.target, this.shown - 0.3F * dt);
      }

      this.blend = Math.min(1.0F, this.blend + dt / 14.0F);
      float k = smooth(this.blend);

      for(int c = 0; c < 3; ++c) {
         this.color[c] = Mth.lerp(k, this.from[c], this.to[c]);
      }

      this.charge = Mth.clamp(this.charge + (charging ? dt / 40.0F : -dt / 10.0F), 0.0F, 1.0F);
      this.flare = Math.max(0.0F, this.flare - 0.05F * dt);
      this.swell = 0.06F * this.charge + 0.05F * this.flare;
      this.thickRisen = 0.86F + 0.05F * Mth.clamp(this.shown - 2.0F, 0.0F, 4.0F);
      this.thickMane = 0.92F + 0.06F * Mth.clamp(this.shown - 4.0F, 0.0F, 2.0F);

      for(int i = 0; i < 581; ++i) {
         this.grow[i] = growthStyled(i, this.shown, this.wild);
      }

      if (!(dt <= 0.0F)) {
         float yaw = Mth.rotLerp(partial, player.yHeadRotO, player.yHeadRot);
         if (Float.isNaN(this.lastYaw)) {
            this.lastYaw = yaw;
         }

         float rawSpin = Mth.wrapDegrees(yaw - this.lastYaw) * ((float)Math.PI / 180F) / dt;
         this.lastYaw = yaw;
         this.spin += (Mth.clamp(rawSpin, -0.6F, 0.6F) - this.spin) * Math.min(1.0F, 0.5F * dt);
         float pitch = Mth.lerp(partial, player.xRotO, player.getXRot()) * ((float)Math.PI / 180F);
         boolean wet = player.isInWater();
         float[] w = wet ? new float[]{0.0F, 0.0F} : wind(player.level(), BlockPos.containing(player.getEyePosition(partial)), partial);
         float ax = (float)((double)w[0] - (player.getX() - player.xo));
         float ay = (float)(-(player.getY() - player.yo));
         float az = (float)((double)w[1] - (player.getZ() - player.zo));
         LbHairPhysics.frame(this.inputs, ax, ay, az, yaw * ((float)Math.PI / 180F), pitch, wet ? 0.3F : 1.0F);
         this.inputs[5] = this.spin;
         this.inputs[6] = (auraOn ? 0.55F + 0.07F * (float)tier : 0.0F) + 2.0F * this.charge + 2.5F * this.flare;
         this.inputs[7] = (formed ? 0.35F + 0.1F * (float)tier : 0.08F) + 0.8F * this.charge + 0.6F * this.flare;
         this.inputs[8] = time;
         LbHairPhysics.step(this.phys, this.inputs, dt);
      }
   }
}
