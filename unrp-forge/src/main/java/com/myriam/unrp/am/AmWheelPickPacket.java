package com.myriam.unrp.am;

import com.myriam.unrp.pass.AmRoster;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class AmWheelPickPacket {
   private final int mode;
   private final int id;

   public AmWheelPickPacket(int mode, int id) {
      this.mode = mode;
      this.id = id;
   }

   public static void encode(AmWheelPickPacket msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.mode);
      buf.writeByte(msg.id);
   }

   public static AmWheelPickPacket decode(FriendlyByteBuf buf) {
      int mode = buf.readByte() & 255;
      int id = buf.readByte() & 255;
      return new AmWheelPickPacket(mode, id);
   }

   public static void handle(AmWheelPickPacket msg, Supplier<NetworkEvent.Context> ctx) {
      ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
         ServerPlayer sender = ((NetworkEvent.Context)ctx.get()).getSender();
         if (sender != null) {
            AmWheelBridge.pick(sender, msg.mode, msg.id);
         }

      });
      ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
}
