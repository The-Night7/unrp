package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class CodexSyncPacket {
   public static final int ROWS = 14;
   public final int runes;
   public final int spent;
   public final int stage;
   public final int[] ranks;
   public final int[] costs;
   public final int charms;
   public final int charmCap;

   public CodexSyncPacket(int runes, int spent, int stage, int[] ranks, int[] costs, int charms, int charmCap) {
      this.runes = runes;
      this.spent = spent;
      this.stage = stage;
      this.ranks = ranks;
      this.costs = costs;
      this.charms = charms;
      this.charmCap = charmCap;
   }

   public static void encode(CodexSyncPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.runes);
      buf.writeVarInt(msg.spent);
      buf.writeByte(msg.stage);

      for(int i = 0; i < 14; ++i) {
         buf.writeVarInt(msg.ranks[i]);
      }

      for(int i = 0; i < 14; ++i) {
         buf.writeVarInt(msg.costs[i]);
      }

      buf.writeVarInt(msg.charms);
      buf.writeByte(msg.charmCap);
   }

   public static CodexSyncPacket decode(FriendlyByteBuf buf) {
      int runes = buf.readVarInt();
      int spent = buf.readVarInt();
      int stage = buf.readByte();
      int[] ranks = new int[14];
      int[] costs = new int[14];

      for(int i = 0; i < 14; ++i) {
         ranks[i] = buf.readVarInt();
      }

      for(int i = 0; i < 14; ++i) {
         costs[i] = buf.readVarInt();
      }

      int charms = buf.readVarInt();
      int charmCap = buf.readByte();
      return new CodexSyncPacket(runes, spent, stage, ranks, costs, charms, charmCap);
   }

   public static void handle(CodexSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RuneCodexScreen.open(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
