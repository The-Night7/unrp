package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class FormPickPacket {
   private final int form;

   public FormPickPacket(int form) {
      this.form = form;
   }

   public static void encode(FormPickPacket msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.form);
   }

   public static FormPickPacket decode(FriendlyByteBuf buf) {
      return new FormPickPacket(buf.readByte());
   }

   public static void handle(FormPickPacket msg, Supplier<NetworkEvent.Context> ctx) {
      ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
         ServerPlayer sender = ((NetworkEvent.Context)ctx.get()).getSender();
         if (sender != null) {
            LimitBreakerEvents.pickForm(sender, msg.form);
         }

      });
      ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
}
