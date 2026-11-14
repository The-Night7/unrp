package com.myriam.unrp.deal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class LedgerViewPacket {
   public final List<LedgerViewPacket.Entry> entries;

   public LedgerViewPacket(List<LedgerViewPacket.Entry> entries) {
      this.entries = entries;
   }

   public static void encode(LedgerViewPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.entries.size());

      for(LedgerViewPacket.Entry e : msg.entries) {
         buf.writeUUID(e.pactId);
         buf.writeUtf(e.label, 64);
         buf.writeBoolean(e.soul);
         buf.writeBoolean(e.signed);
         buf.writeByte(e.ritual);
         buf.writeUtf(e.where, 128);
         buf.writeVarInt(e.terms.size());

         for(String t : e.terms) {
            buf.writeUtf(t, 256);
         }

         buf.writeBoolean(e.proposed);
      }

   }

   public static LedgerViewPacket decode(FriendlyByteBuf buf) {
      int n = buf.readVarInt();
      List<LedgerViewPacket.Entry> entries = new ArrayList();

      for(int i = 0; i < n; ++i) {
         UUID id = buf.readUUID();
         String label = buf.readUtf(64);
         boolean soul = buf.readBoolean();
         boolean signed = buf.readBoolean();
         int ritual = buf.readByte();
         String where = buf.readUtf(128);
         int tn = buf.readVarInt();
         List<String> terms = new ArrayList();

         for(int t = 0; t < tn; ++t) {
            terms.add(buf.readUtf(256));
         }

         LedgerViewPacket.Entry entry = new LedgerViewPacket.Entry(id, label, soul, signed, ritual, where, terms);
         entry.proposed = buf.readBoolean();
         entries.add(entry);
      }

      return new LedgerViewPacket(entries);
   }

   public static void handle(LedgerViewPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LedgerScreen.open(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }

   public static class Entry {
      public final UUID pactId;
      public final String label;
      public final boolean soul;
      public final boolean signed;
      public final int ritual;
      public final String where;
      public final List<String> terms;
      public boolean proposed;

      public Entry(UUID pactId, String label, boolean soul, boolean signed, int ritual, String where, List<String> terms) {
         this.pactId = pactId;
         this.label = label;
         this.soul = soul;
         this.signed = signed;
         this.ritual = ritual;
         this.where = where;
         this.terms = terms;
      }
   }
}
