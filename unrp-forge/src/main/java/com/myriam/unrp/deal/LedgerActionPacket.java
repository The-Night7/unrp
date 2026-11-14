package com.myriam.unrp.deal;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class LedgerActionPacket {
   public static final int OP_SUMMON = 0;
   public static final int OP_REFRESH = 1;
   public static final int OP_OPEN = 2;
   public final int op;
   public final UUID pactId;

   public LedgerActionPacket(int op, UUID pactId) {
      this.op = op;
      this.pactId = pactId;
   }

   public static void encode(LedgerActionPacket msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.op);
      buf.writeUUID(msg.pactId);
   }

   public static LedgerActionPacket decode(FriendlyByteBuf buf) {
      return new LedgerActionPacket(buf.readByte(), buf.readUUID());
   }

   public static void handle(LedgerActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
      ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
         ServerPlayer player = ((NetworkEvent.Context)ctx.get()).getSender();
         if (player != null) {
            DealmakerEvents.onLedgerAction(player, msg);
         }

      });
      ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
}
