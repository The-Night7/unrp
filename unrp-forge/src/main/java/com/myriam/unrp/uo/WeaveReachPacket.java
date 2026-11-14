package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class WeaveReachPacket {
   public final int reach;

   public WeaveReachPacket(int reach) {
      this.reach = reach;
   }

   public static void encode(WeaveReachPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.reach);
   }

   public static WeaveReachPacket decode(FriendlyByteBuf buf) {
      return new WeaveReachPacket(buf.readVarInt());
   }

   public static void handle(WeaveReachPacket msg, Supplier<NetworkEvent.Context> ctx) {
      ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
         ServerPlayer sender = ((NetworkEvent.Context)ctx.get()).getSender();
         if (sender != null) {
            ParticlesEvents.setWeaveReach(sender, msg.reach);
         }

      });
      ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
}
