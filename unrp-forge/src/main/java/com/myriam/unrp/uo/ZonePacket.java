package com.myriam.unrp.uo;



import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class ZonePacket {
   public final UUID owner;
   public final boolean heavy;
   public final double x;
   public final double y;
   public final double z;
   public final float radius;
   public final int dir;
   public final long began;
   public final long until;
   public final boolean end;

   public ZonePacket(UUID owner, boolean heavy, double x, double y, double z, float radius, int dir, long began, long until, boolean end) {
      this.owner = owner;
      this.heavy = heavy;
      this.x = x;
      this.y = y;
      this.z = z;
      this.radius = radius;
      this.dir = dir;
      this.began = began;
      this.until = until;
      this.end = end;
   }

   public static void encode(ZonePacket msg, FriendlyByteBuf buf) {
      buf.writeUUID(msg.owner);
      buf.writeBoolean(msg.heavy);
      buf.writeDouble(msg.x);
      buf.writeDouble(msg.y);
      buf.writeDouble(msg.z);
      buf.writeFloat(msg.radius);
      buf.writeByte(msg.dir);
      buf.writeLong(msg.began);
      buf.writeLong(msg.until);
      buf.writeBoolean(msg.end);
   }

   public static ZonePacket decode(FriendlyByteBuf buf) {
      UUID owner = buf.readUUID();
      boolean heavy = buf.readBoolean();
      double x = buf.readDouble();
      double y = buf.readDouble();
      double z = buf.readDouble();
      float radius = buf.readFloat();
      int dir = buf.readByte();
      long began = buf.readLong();
      long until = buf.readLong();
      boolean end = buf.readBoolean();
      return new ZonePacket(owner, heavy, x, y, z, radius, dir, began, until, end);
   }

   public static void handle(ZonePacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientVertigo.acceptZone(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
