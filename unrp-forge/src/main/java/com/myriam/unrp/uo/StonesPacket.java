package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class StonesPacket {
   public final int entity;
   public final String block;
   public final long until;

   public StonesPacket(int entity, String block, long until) {
      this.entity = entity;
      this.block = block;
      this.until = until;
   }

   public static void encode(StonesPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.entity);
      buf.writeUtf(msg.block.length() <= 256 ? msg.block : "minecraft:cobblestone", 256);
      buf.writeLong(msg.until);
   }

   public static StonesPacket decode(FriendlyByteBuf buf) {
      return new StonesPacket(buf.readVarInt(), buf.readUtf(256), buf.readLong());
   }

   public static void handle(StonesPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientVertigo.acceptStones(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
