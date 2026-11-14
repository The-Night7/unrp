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

public class PactViewPacket {
   public static final int MODE_EDIT = 0;
   public static final int MODE_SIGN = 1;
   public static final int MODE_OWNER = 2;
   public static final int MODE_READ = 3;
   public static final int MODE_PROPOSE = 4;
   public final int mode;
   public final UUID pactId;
   public final String ownerName;
   public final String signerName;
   public final boolean signed;
   public final boolean soul;
   public final double reachFrac;
   public final List<PactLedger.Term> terms;
   public final List<String> players;
   public boolean proposed;
   public boolean submitted;
   public List<String> powers = new ArrayList();
   public List<String> moves = new ArrayList();

   public PactViewPacket(int mode, UUID pactId, String ownerName, String signerName, boolean signed, boolean soul, double reachFrac, List<PactLedger.Term> terms, List<String> players) {
      this.mode = mode;
      this.pactId = pactId;
      this.ownerName = ownerName;
      this.signerName = signerName;
      this.signed = signed;
      this.soul = soul;
      this.reachFrac = reachFrac;
      this.terms = terms;
      this.players = players;
   }

   public static void encode(PactViewPacket msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.mode);
      buf.writeUUID(msg.pactId);
      buf.writeUtf(msg.ownerName, 64);
      buf.writeUtf(msg.signerName, 64);
      buf.writeBoolean(msg.signed);
      buf.writeBoolean(msg.soul);
      buf.writeDouble(msg.reachFrac);
      buf.writeVarInt(msg.terms.size());

      for(PactLedger.Term t : msg.terms) {
         buf.writeUtf(t.cat, 32);
         buf.writeUtf(t.key, 32);
         buf.writeUtf(t.a, 256);
         buf.writeDouble(t.v);
         buf.writeVarInt(t.x1);
         buf.writeVarInt(t.z1);
         buf.writeVarInt(t.x2);
         buf.writeVarInt(t.z2);
         buf.writeByte(t.side);
         buf.writeUtf(t.text, 256);
         buf.writeBoolean(t.locked);
      }

      buf.writeVarInt(msg.players.size());

      for(String p_0 : msg.players) {
         buf.writeUtf(p_0, 40);
      }

      buf.writeBoolean(msg.proposed);
      buf.writeBoolean(msg.submitted);
      buf.writeVarInt(msg.powers.size());

      for(String p : msg.powers) {
         buf.writeUtf(p, 96);
      }

      buf.writeVarInt(msg.moves.size());

      for(String m : msg.moves) {
         buf.writeUtf(m, 96);
      }

   }

   public static PactViewPacket decode(FriendlyByteBuf buf) {
      int mode = buf.readByte();
      UUID id = buf.readUUID();
      String ownerName = buf.readUtf(64);
      String signerName = buf.readUtf(64);
      boolean signed = buf.readBoolean();
      boolean soul = buf.readBoolean();
      double reachFrac = buf.readDouble();
      int n = buf.readVarInt();
      List<PactLedger.Term> terms = new ArrayList();

      for(int i = 0; i < n; ++i) {
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
         terms.add(t);
      }

      int pn = buf.readVarInt();
      List<String> players = new ArrayList();

      for(int i = 0; i < pn; ++i) {
         players.add(buf.readUtf(40));
      }

      PactViewPacket out = new PactViewPacket(mode, id, ownerName, signerName, signed, soul, reachFrac, terms, players);
      out.proposed = buf.readBoolean();
      out.submitted = buf.readBoolean();
      int pw = buf.readVarInt();

      for(int i = 0; i < pw; ++i) {
         out.powers.add(buf.readUtf(96));
      }

      int mv = buf.readVarInt();

      for(int i = 0; i < mv; ++i) {
         out.moves.add(buf.readUtf(96));
      }

      return out;
   }

   public static void handle(PactViewPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ContractScreen.open(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
