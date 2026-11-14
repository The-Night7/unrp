package com.myriam.unrp.deal;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PactEditPacket {
   public static final int OP_ADD = 0;
   public static final int OP_REMOVE = 1;
   public static final int OP_SIGN = 2;
   public static final int OP_DESTROY = 3;
   public static final int OP_SUBMIT = 4;
   public static final int OP_COUNTERSIGN = 5;
   public final int op;
   public final UUID pactId;
   public final int index;
   public final PactLedger.Term term;

   public PactEditPacket(int op, UUID pactId, int index, PactLedger.Term term) {
      this.op = op;
      this.pactId = pactId;
      this.index = index;
      this.term = term == null ? new PactLedger.Term() : term;
   }

   public static void encode(PactEditPacket msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.op);
      buf.writeUUID(msg.pactId);
      buf.writeVarInt(msg.index);
      buf.writeUtf(msg.term.cat, 32);
      buf.writeUtf(msg.term.key, 32);
      buf.writeUtf(msg.term.a, 256);
      buf.writeDouble(msg.term.v);
      buf.writeVarInt(msg.term.x1);
      buf.writeVarInt(msg.term.z1);
      buf.writeVarInt(msg.term.x2);
      buf.writeVarInt(msg.term.z2);
      buf.writeByte(msg.term.side);
      buf.writeUtf(msg.term.text, 256);
      buf.writeBoolean(msg.term.locked);
   }

   public static PactEditPacket decode(FriendlyByteBuf buf) {
      int op = buf.readByte();
      UUID id = buf.readUUID();
      int index = buf.readVarInt();
      PactLedger.Term t = new PactLedger.Term();
      t.cat = buf.readUtf(32);
      t.key = buf.readUtf(32);
      t.a = buf.readUtf(256);
      t.v = buf.readDouble();
      t.x1 = buf.readVarInt();
      t.z1 = buf.readVarInt();
      t.x2 = buf.readVarInt();
      t.z2 = buf.readVarInt();
      t.side = buf.readByte();
      t.text = buf.readUtf(256);
      t.locked = buf.readBoolean();
      return new PactEditPacket(op, id, index, t);
   }

   public static void handle(PactEditPacket msg, Supplier<NetworkEvent.Context> ctx) {
      ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
         ServerPlayer sender = ((NetworkEvent.Context)ctx.get()).getSender();
         if (sender != null) {
            DealmakerEvents.onPactEdit(sender, msg);
         }

      });
      ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
}
