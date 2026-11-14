package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class LashHudPacket {
   public static final int MOST_STRAIN = 100;
   public static final int MOST_TICKS = 6000;
   public static final int COOLDOWNS = 6;
   public static final int FULL = 255;
   public static final int WARDING = 1;
   public static final int STORMING = 2;
   public static final int HOLDING = 4;
   public static final int SWINGING = 8;
   public static final int DRAWING = 16;
   public static final int TWIN = 32;
   public static final int WIND_USED = 64;
   public static final int STORM_OPEN = 128;
   public static final int FLAGS = 255;
   public static final int PHASES = 5;
   public static final int MOST_HELD = 6;
   private final int strain;
   private final int frenzy;
   private final int rest;
   private final int[] cds;
   private final int flags;
   private final int wind;
   private final int windLeft;
   private final int held;
   private final int holdLeft;

   public LashHudPacket(int strain, int frenzy, int rest, int[] cds, int flags, int wind, int windLeft, int held, int holdLeft) {
      this.strain = Math.max(0, Math.min(100, strain));
      this.frenzy = Math.max(0, Math.min(6000, frenzy));
      this.rest = Math.max(0, Math.min(6000, rest));
      this.cds = new int[6];

      for(int i = 0; i < 6; ++i) {
         int v = cds != null && i < cds.length ? cds[i] : 0;
         this.cds[i] = Math.max(0, Math.min(255, v));
      }

      this.flags = flags & 255;
      this.wind = Math.max(0, Math.min(4, wind));
      this.windLeft = Math.max(0, Math.min(6000, windLeft));
      this.held = Math.max(0, Math.min(6, held));
      this.holdLeft = Math.max(0, Math.min(6000, holdLeft));
   }

   public static void encode(LashHudPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.strain);
      buf.writeVarInt(msg.frenzy);
      buf.writeVarInt(msg.rest);

      for(int i = 0; i < 6; ++i) {
         buf.writeByte(msg.cds[i]);
      }

      buf.writeByte(msg.flags);
      buf.writeByte(msg.wind);
      buf.writeVarInt(msg.windLeft);
      buf.writeByte(msg.held);
      buf.writeVarInt(msg.holdLeft);
   }

   public static LashHudPacket decode(FriendlyByteBuf buf) {
      int strain = Math.max(0, Math.min(100, buf.readVarInt()));
      int frenzy = Math.max(0, Math.min(6000, buf.readVarInt()));
      int rest = Math.max(0, Math.min(6000, buf.readVarInt()));
      int[] cds = new int[6];

      for(int i = 0; i < 6; ++i) {
         cds[i] = buf.readUnsignedByte();
      }

      int flags = buf.readUnsignedByte();
      int wind = Math.max(0, Math.min(4, buf.readUnsignedByte()));
      int windLeft = Math.max(0, Math.min(6000, buf.readVarInt()));
      int held = Math.max(0, Math.min(6, buf.readUnsignedByte()));
      int holdLeft = Math.max(0, Math.min(6000, buf.readVarInt()));
      return new LashHudPacket(strain, frenzy, rest, cds, flags, wind, windLeft, held, holdLeft);
   }

   public static void handle(LashHudPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LashHudState.set(msg.strain, msg.frenzy, msg.rest, msg.cds, msg.flags, msg.wind, msg.windLeft, msg.held, msg.holdLeft)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
