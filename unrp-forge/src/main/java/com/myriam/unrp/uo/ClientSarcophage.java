package com.myriam.unrp.uo;



import java.util.HashMap;
import java.util.Map;

public final class ClientSarcophage {
   private static final Map<Integer, ClientSarcophage.Hold> HOLDS = new HashMap();
   private static final Map<Integer, ClientSarcophage.Move> MOVES = new HashMap();

   private ClientSarcophage() {
   }

   public static void accept(OriginLookPacket msg, long now) {
      if (msg.kind == 20) {
         int target = (int)msg.get(0);
         if (msg.life > 0 && target >= 0) {
            HOLDS.put(msg.entity, new ClientSarcophage.Hold(target, now + (long)msg.life));
         } else {
            HOLDS.remove(msg.entity);
         }

      } else {
         int move = (int)msg.get(0);
         if (move > 0 && msg.life > 0) {
            if (move == 10) {
               ClientSarcophage.Move m = (ClientSarcophage.Move)MOVES.get(msg.entity);
               if (m != null && m.move == 1) {
                  m.hold = msg.life;
                  m.end = m.start + (long)msg.get(1);
               }

            } else {
               ClientSarcophage.Move prev = (ClientSarcophage.Move)MOVES.get(msg.entity);
               boolean ramp = move != 9 && (move != 3 || prev == null || prev.move != 2);
               MOVES.put(msg.entity, new ClientSarcophage.Move(move, now, msg.life, ramp));
            }
         } else {
            MOVES.remove(msg.entity);
         }
      }
   }

   public static int heldBy(int caster, long now) {
      ClientSarcophage.Hold h = (ClientSarcophage.Hold)HOLDS.get(caster);
      if (h == null) {
         return -1;
      } else if (now > h.until + 40L) {
         HOLDS.remove(caster);
         return -1;
      } else {
         return h.target;
      }
   }

   public static ClientSarcophage.Move move(int entity, long now) {
      ClientSarcophage.Move m = (ClientSarcophage.Move)MOVES.get(entity);
      if (m != null && now > m.end + 4L) {
         MOVES.remove(entity);
         return null;
      } else {
         return m;
      }
   }

   public static void forget(int entity) {
      HOLDS.remove(entity);
      MOVES.remove(entity);
   }

   public static void clearAll() {
      HOLDS.clear();
      MOVES.clear();
   }

   public static final class Hold {
      public final int target;
      public final long until;

      Hold(int target, long until) {
         this.target = target;
         this.until = until;
      }
   }

   public static final class Move {
      public final int move;
      public final long start;
      public long end;
      public final boolean rampIn;
      public int hold;

      Move(int move, long start, int life, boolean rampIn) {
         this.move = move;
         this.start = start;
         this.end = start + (long)life;
         this.rampIn = rampIn;
      }
   }
}
