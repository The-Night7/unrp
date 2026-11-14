package com.myriam.unrp.uo;



import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class LbBecomingPacket {
   private final UUID uuid;
   private final int form;
   private final int ticks;

   public LbBecomingPacket(UUID uuid, int form, int ticks) {
      this.uuid = uuid;
      this.form = form;
      this.ticks = ticks;
   }

   public static void encode(LbBecomingPacket msg, FriendlyByteBuf buf) {
      buf.writeUUID(msg.uuid);
      buf.writeByte(msg.form);
      buf.writeVarInt(msg.ticks);
   }

   public static LbBecomingPacket decode(FriendlyByteBuf buf) {
      UUID uuid = buf.readUUID();
      int form = buf.readByte() & 255;
      int ticks = buf.readVarInt();
      return new LbBecomingPacket(uuid, form, ticks);
   }

   public static void handle(LbBecomingPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientLbFormCache.becoming(msg.uuid, msg.form, msg.ticks)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
