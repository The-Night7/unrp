package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class LashSwingPacket {
   public static final int REEL = 1;
   public static final int SLACK = 2;
   public static final int ALL = 3;
   public final int forward;
   public final int right;
   public final int flags;

   public LashSwingPacket(int forward, int right, int flags) {
      this.forward = Math.max(-100, Math.min(100, forward));
      this.right = Math.max(-100, Math.min(100, right));
      this.flags = flags & 3;
   }

   public static void encode(LashSwingPacket msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.forward);
      buf.writeByte(msg.right);
      buf.writeByte(msg.flags);
   }

   public static LashSwingPacket decode(FriendlyByteBuf buf) {
      return new LashSwingPacket(buf.readByte(), buf.readByte(), buf.readByte());
   }

   public static void handle(LashSwingPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_SERVER) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ServerPlayer p = ((NetworkEvent.Context)ctx.get()).getSender();
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
            if (p != null) {
               BlackLashEvents.swingInput(p, msg.forward, msg.right, msg.flags);
            }

         });
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
