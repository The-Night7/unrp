package com.myriam.unrp.pass;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class PassFxPacket {
   public static final int LT_ON = 0;
   public static final int LT_BOLT = 1;
   public static final int LT_HIT = 2;
   public static final int LT_TOUCH = 3;
   public static final int LT_RETORT = 4;
   public static final int ED_ORB = 6;
   public static final int ED_BLAST = 7;
   public static final int ED_GATHER = 8;
   public static final int ED_BURST = 9;
   public static final int ED_LAUNCH = 10;
   public static final int ED_LAND = 11;
   public static final int GONE = 12;
   public static final int BR_HIT = 13;
   public static final int BR_SHATTER = 14;
   public static final int BR_GONE = 15;
   public static final int BR_DEPLOY = 16;
   public static final int BR_GROW = 17;
   public static final int BR_RAM = 18;
   public static final int BR_SQUEEZE = 19;
   public static final int BR_CRUSH = 20;
   public static final int BR_RAM_HIT = 21;
   public static final int TM_ON = 22;
   public static final int TM_FLOW = 23;
   public static final int TM_REWIND = 24;
   public static final int TM_BLINK = 25;
   public static final int TM_MARK = 26;
   public static final int TM_RECALL = 27;
   public static final int TM_FREEZE = 28;
   public static final int TM_HOLD = 29;
   public static final int TM_FREE = 30;
   public static final int TM_STRIKE = 31;
   public static final int TM_DELAY = 32;
   public static final int TM_DELAY_HIT = 33;
   public static final int PD_LOCK = 34;
   public static final int PD_FOCUS = 35;
   public static final int PD_BEAT = 36;
   public static final int PD_RUSH = 37;
   public static final int PD_RUSH_HIT = 38;
   public static final int PD_BREAKER = 39;
   public static final int PD_ROAR = 40;
   public static final int AM_ON = 41;
   public static final int AM_SENSE = 42;
   public static final int AM_TAKE = 43;
   public static final int AM_EXPEL = 44;
   public static final int OPS = 45;
   public static final int F_COPY = 1;
   public static final int F_ROOF = 2;
   public static final int F_ON = 4;
   public static final int F_TARGET = 8;
   public static final int F_FLOAT = 16;
   public static final int F_SYNC = 32;
   public static final int F_FAR = 64;
   public static final int F_BACK = 128;
   public static final int NV = 6;
   public static final float FAR = 3.0E7F;
   public final int op;
   public final int caster;
   public final int target;
   public final int aux;
   public final int flags;
   public final int color;
   public final float[] v;

   public PassFxPacket(int op, int caster, int target, int aux, int flags, int color, double[] v) {
      this.op = Math.max(0, Math.min(44, op));
      this.caster = caster;
      this.target = target;
      this.aux = aux;
      this.flags = flags & 255;
      this.color = color & 16777215;
      this.v = new float[6];

      for(int i = 0; i < 6; ++i) {
         double x = v != null && i < v.length ? v[i] : 0.0D;
         this.v[i] = Double.isFinite(x) ? (float)Math.max(-3.0E7D, Math.min(3.0E7D, x)) : 0.0F;
      }

   }

   public boolean has(int flag) {
      return (this.flags & flag) != 0;
   }

   public double[] at() {
      return new double[]{(double)this.v[0], (double)this.v[1], (double)this.v[2]};
   }

   public double[] vec() {
      return new double[]{(double)this.v[3], (double)this.v[4], (double)this.v[5]};
   }

   public static void encode(PassFxPacket msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.op);
      buf.writeVarInt(msg.caster);
      buf.writeVarInt(msg.target);
      buf.writeVarInt(msg.aux);
      buf.writeByte(msg.flags);
      buf.writeInt(msg.color);

      for(float x : msg.v) {
         buf.writeFloat(x);
      }

   }

   public static PassFxPacket decode(FriendlyByteBuf buf) {
      int op = buf.readByte();
      int caster = buf.readVarInt();
      int target = buf.readVarInt();
      int aux = buf.readVarInt();
      int flags = buf.readByte() & 255;
      int color = buf.readInt();
      double[] v = new double[6];

      for(int i = 0; i < 6; ++i) {
         v[i] = (double)buf.readFloat();
      }

      return new PassFxPacket(op, caster, target, aux, flags, color, v);
   }

   public static void handle(PassFxPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPass.accept(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
