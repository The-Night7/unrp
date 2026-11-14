package com.myriam.unrp.uo;



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class SarcophageTailState {
   public static final int MAX_TAILS = 6;
   public static final int GRAB = 5;
   private static final int[][] LAYOUT = new int[][]{new int[0], {0}, {1, 2}, {1, 2, 0}, {1, 2, 3, 4}, {1, 2, 3, 4, 0}};
   private static final float SPROUT = 0.085F;
   private static final float WITHER = 0.15F;
   private static final float GRAB_SPROUT = 0.25F;
   private static final float GRAB_WITHER = 0.2F;
   private static final float SWING_TICKS = 6.0F;
   private static final float FORGET = 300.0F;
   private static final Map<Integer, SarcophageTailState.State> LIVE = new HashMap();

   public static int[] slotsFor(int tails) {
      return LAYOUT[Mth.clamp(tails, 0, LAYOUT.length - 1)];
   }

   private SarcophageTailState() {
   }

   public static SarcophageTailState.State sample(Player player, float t, int tails, boolean grabbing) {
      SarcophageTailState.State s = (SarcophageTailState.State)LIVE.get(player.getId());
      if (s == null) {
         s = new SarcophageTailState.State();
         LIVE.put(player.getId(), s);
         sweep(t);
      }

      s.seen = t;
      float dt = Float.isNaN(s.last) ? 0.0F : Mth.clamp(t - s.last, 0.0F, 4.0F);
      s.last = t;
      boolean[] live = new boolean[6];

      for(int slot : slotsFor(tails)) {
         live[slot] = true;
      }

      live[5] = grabbing;

      for(int i = 0; i < 6; ++i) {
         boolean want = live[i];
         float step = i == 5 ? (want ? 0.25F : -0.2F) : (want ? 0.085F : -0.15F);
         float before = s.grow[i];
         s.grow[i] = Mth.clamp(before + step * dt, 0.0F, 1.0F);
         s.spawning[i] = want && s.grow[i] < 0.995F && s.grow[i] > 0.001F;
      }

      return s;
   }

   private static void sweep(float now) {
      if (LIVE.size() >= 16) {
         Iterator<Map.Entry<Integer, SarcophageTailState.State>> it = LIVE.entrySet().iterator();

         while(it.hasNext()) {
            SarcophageTailState.State v = (SarcophageTailState.State)((Map.Entry)it.next()).getValue();
            if (now - v.seen > 300.0F) {
               it.remove();
            }
         }

      }
   }

   public static final class State {
      public final float[] grow = new float[6];
      public final boolean[] spawning = new boolean[6];
      public int lastTick = -1;
      private float last = Float.NaN;
      private float seen;
      private float swingStart = -1000.0F;
      private float lastAttack;

      public double whip(Player player, float partial) {
         float a = player.getAttackAnim(partial);
         float t = (float)player.tickCount + partial;
         if (a > 0.0F && (this.lastAttack <= 0.0F || a < this.lastAttack - 0.2F)) {
            this.swingStart = t - a * 6.0F;
         }

         this.lastAttack = a;
         double ticks = (double)(t - this.swingStart);
         return ticks <= 20.0D ? ticks : -1.0D;
      }

      public boolean showing() {
         for(float g : this.grow) {
            if (g > 0.02F) {
               return true;
            }
         }

         return false;
      }
   }
}
