package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class LashWindPacket {
   public static final int PHASES = 5;
   public static final int MOST_TICKS = 6000;
   public static final int MOST_SCORE = 1000000000;
   public final int phase;
   public final int left;
   public final int total;
   public final int score;

   public LashWindPacket(int phase, int left, int total, int score) {
      this.phase = Math.max(0, Math.min(4, phase));
      this.left = Math.max(0, Math.min(6000, left));
      this.total = Math.max(1, Math.min(6000, total));
      this.score = Math.max(0, Math.min(1000000000, score));
   }

   public static void encode(LashWindPacket msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.phase);
      buf.writeVarInt(msg.left);
      buf.writeVarInt(msg.total);
      buf.writeVarInt(msg.score);
   }

   public static LashWindPacket decode(FriendlyByteBuf buf) {
      int phase = buf.readUnsignedByte();
      int left = buf.readVarInt();
      int total = buf.readVarInt();
      int score = buf.readVarInt();
      return new LashWindPacket(phase, left, total, score);
   }

   public static void handle(LashWindPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LashDownedScreen.accept(msg.phase, msg.left, msg.total, msg.score)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
