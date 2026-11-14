package com.myriam.unrp.am;

import com.myriam.unrp.pass.AmRoster;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class AmWheelAskPacket {
   public static void encode(AmWheelAskPacket msg, FriendlyByteBuf buf) {
   }

   public static AmWheelAskPacket decode(FriendlyByteBuf buf) {
      return new AmWheelAskPacket();
   }

   public static void handle(AmWheelAskPacket msg, Supplier<NetworkEvent.Context> ctx) {
      ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
         ServerPlayer sender = ((NetworkEvent.Context)ctx.get()).getSender();
         if (sender != null) {
            AmWheelBridge.ask(sender);
         }

      });
      ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
}
