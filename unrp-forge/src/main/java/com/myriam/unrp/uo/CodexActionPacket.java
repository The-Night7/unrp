package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class CodexActionPacket {
   public static final int SYNC = 0;
   public static final int BUY = 1;
   public static final int RELEASE = 2;
   private final int action;
   private final int arg;

   public CodexActionPacket(int action, int arg) {
      this.action = action;
      this.arg = arg;
   }

   public static void encode(CodexActionPacket msg, FriendlyByteBuf buf) {
      buf.writeInt(msg.action);
      buf.writeInt(msg.arg);
   }

   public static CodexActionPacket decode(FriendlyByteBuf buf) {
      return new CodexActionPacket(buf.readInt(), buf.readInt());
   }

   public static void handle(CodexActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
      ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
         ServerPlayer player = ((NetworkEvent.Context)ctx.get()).getSender();
         if (player != null) {
            if (msg.action == 0) {
               HollowMarkEvents.openCodex(player);
            } else if (msg.action == 1 && msg.arg >= 0 && msg.arg < 14) {
               HollowMarkEvents.buyRank(player, msg.arg);
            } else if (msg.action == 2) {
               HollowMarkEvents.releaseCharm(player, msg.arg);
            }

         }
      });
      ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
}
