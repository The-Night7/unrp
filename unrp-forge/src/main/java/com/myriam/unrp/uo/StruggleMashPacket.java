package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class StruggleMashPacket {
   public static void encode(StruggleMashPacket msg, FriendlyByteBuf buf) {
   }

   public static StruggleMashPacket decode(FriendlyByteBuf buf) {
      return new StruggleMashPacket();
   }

   public static void handle(StruggleMashPacket msg, Supplier<NetworkEvent.Context> ctx) {
      ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
         ServerPlayer sender = ((NetworkEvent.Context)ctx.get()).getSender();
         if (sender != null) {
            StruggleEvents.mash(sender);
         }

      });
      ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
}
