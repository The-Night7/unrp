package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class SaltMarkPacket {
   public static final int STATUE = 1;
   public static final int PILLAR = 2;
   public static final int ROOTED = 4;
   public static final int FLAGS = 7;
   public static final int MOST = 100;
   public final int entity;
   public final int salt;
   public final int flags;

   public SaltMarkPacket(int entity, int salt, int flags) {
      this.entity = Math.max(0, entity);
      this.salt = Math.max(0, Math.min(100, salt));
      this.flags = flags & 7;
   }

   public static void encode(SaltMarkPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.entity);
      buf.writeByte(msg.salt);
      buf.writeByte(msg.flags);
   }

   public static SaltMarkPacket decode(FriendlyByteBuf buf) {
      int entity = buf.readVarInt();
      int salt = buf.readUnsignedByte();
      int flags = buf.readUnsignedByte();
      return new SaltMarkPacket(entity, salt, flags);
   }

   public static void handle(SaltMarkPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSaltSeraph.mark(msg.entity, msg.salt, msg.flags)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
