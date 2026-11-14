package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class BreakCinePacket {
   private final int ticks;
   private final int tier;

   public BreakCinePacket(int ticks, int tier) {
      this.ticks = ticks;
      this.tier = tier;
   }

   public static void encode(BreakCinePacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.ticks);
      buf.writeByte(msg.tier);
   }

   public static BreakCinePacket decode(FriendlyByteBuf buf) {
      int ticks = buf.readVarInt();
      int tier = buf.readByte();
      return new BreakCinePacket(ticks, tier);
   }

   public static void handle(BreakCinePacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BreakCineHandler.begin(msg.ticks, msg.tier)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
