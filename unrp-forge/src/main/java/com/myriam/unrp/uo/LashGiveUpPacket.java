package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class LashGiveUpPacket {
   public static void encode(LashGiveUpPacket msg, FriendlyByteBuf buf) {
   }

   public static LashGiveUpPacket decode(FriendlyByteBuf buf) {
      return new LashGiveUpPacket();
   }

   public static void handle(LashGiveUpPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_SERVER) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ServerPlayer sender = ((NetworkEvent.Context)ctx.get()).getSender();
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
            if (sender != null) {
               BlackLashEvents.giveUp(sender);
            }

         });
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
