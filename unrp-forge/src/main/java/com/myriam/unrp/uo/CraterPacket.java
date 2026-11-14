package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class CraterPacket {
   public final double x;
   public final double y;
   public final double z;
   public final float radius;
   public final int keep;
   public final boolean real;

   public CraterPacket(double x, double y, double z, float radius, int keep, boolean real) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.radius = radius;
      this.keep = keep;
      this.real = real;
   }

   public static void encode(CraterPacket msg, FriendlyByteBuf buf) {
      buf.writeDouble(msg.x);
      buf.writeDouble(msg.y);
      buf.writeDouble(msg.z);
      buf.writeFloat(msg.radius);
      buf.writeVarInt(msg.keep);
      buf.writeBoolean(msg.real);
   }

   public static CraterPacket decode(FriendlyByteBuf buf) {
      return new CraterPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readVarInt(), buf.readBoolean());
   }

   public static void handle(CraterPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientVertigo.acceptCrater(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
