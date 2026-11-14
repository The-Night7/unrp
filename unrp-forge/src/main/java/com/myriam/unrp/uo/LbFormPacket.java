package com.myriam.unrp.uo;



import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class LbFormPacket {
   private final UUID uuid;
   private final int form;

   public LbFormPacket(UUID uuid, int form) {
      this.uuid = uuid;
      this.form = form;
   }

   public static void encode(LbFormPacket msg, FriendlyByteBuf buf) {
      buf.writeUUID(msg.uuid);
      buf.writeByte(msg.form);
   }

   public static LbFormPacket decode(FriendlyByteBuf buf) {
      UUID uuid = buf.readUUID();
      int form = buf.readByte() & 255;
      return new LbFormPacket(uuid, form);
   }

   public static void handle(LbFormPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientLbFormCache.put(msg.uuid, msg.form)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
