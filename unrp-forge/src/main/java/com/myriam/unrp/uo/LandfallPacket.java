package com.myriam.unrp.uo;



import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class LandfallPacket {
   public static final int MOST_NAMES = 64;
   public static final int MOST_SPOTS = 512;
   public final UUID owner;
   public final double x;
   public final double y;
   public final double z;
   public final long began;
   public final List<String> names;
   public final List<int[]> spots;
   public final boolean end;

   public LandfallPacket(UUID owner, double x, double y, double z, long began, List<String> names, List<int[]> spots, boolean end) {
      this.owner = owner;
      this.x = x;
      this.y = y;
      this.z = z;
      this.began = began;
      this.names = names;
      this.spots = spots;
      this.end = end;
   }

   public static void encode(LandfallPacket msg, FriendlyByteBuf buf) {
      buf.writeUUID(msg.owner);
      buf.writeDouble(msg.x);
      buf.writeDouble(msg.y);
      buf.writeDouble(msg.z);
      buf.writeLong(msg.began);
      buf.writeBoolean(msg.end);
      int n = Math.min(64, msg.names.size());
      buf.writeVarInt(n);

      for(int i = 0; i < n; ++i) {
         String name = (String)msg.names.get(i);
         buf.writeUtf(name.length() <= 256 ? name : "minecraft:dirt", 256);
      }

      int s = Math.min(512, msg.spots.size());
      buf.writeVarInt(s);

      for(int i = 0; i < s; ++i) {
         int[] p = (int[])msg.spots.get(i);
         buf.writeByte(p[0]);
         buf.writeByte(p[1]);
         buf.writeByte(p[2]);
         buf.writeVarInt(Math.max(0, p[3]));
      }

   }

   public static LandfallPacket decode(FriendlyByteBuf buf) {
      UUID owner = buf.readUUID();
      double x = buf.readDouble();
      double y = buf.readDouble();
      double z = buf.readDouble();
      long began = buf.readLong();
      boolean end = buf.readBoolean();
      int n = Math.min(64, buf.readVarInt());
      List<String> names = new ArrayList();

      for(int i = 0; i < n; ++i) {
         names.add(buf.readUtf(256));
      }

      int s = Math.min(512, buf.readVarInt());
      List<int[]> spots = new ArrayList();

      for(int i = 0; i < s; ++i) {
         int dx = buf.readByte();
         int dy = buf.readByte();
         int dz = buf.readByte();
         int at = buf.readVarInt();
         spots.add(new int[]{dx, dy, dz, at});
      }

      return new LandfallPacket(owner, x, y, z, began, names, spots, end);
   }

   public static void handle(LandfallPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientVertigo.acceptBall(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
