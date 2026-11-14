package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class WeaveFxPacket {
   public static final int ACTIVE = 1;
   public static final int WEAVING = 2;
   public static final int SPENT = 4;
   public final int entity;
   public final int flags;
   public final int reach;
   public final int orient;
   public final int plane;
   public final int swarm;

   public WeaveFxPacket(int entity, int flags, int reach, int orient, int plane, int swarm) {
      this.entity = entity;
      this.flags = flags;
      this.reach = reach;
      this.orient = orient;
      this.plane = plane;
      this.swarm = swarm;
   }

   public static void encode(WeaveFxPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.entity);
      buf.writeByte(msg.flags);
      buf.writeByte(msg.reach);
      buf.writeByte(msg.orient);
      buf.writeInt(msg.plane);
      buf.writeByte(msg.swarm);
   }

   public static WeaveFxPacket decode(FriendlyByteBuf buf) {
      int entity = buf.readVarInt();
      int flags = buf.readByte();
      int reach = buf.readByte();
      int orient = buf.readByte();
      int plane = buf.readInt();
      int swarm = buf.readByte();
      return new WeaveFxPacket(entity, flags, reach, orient, plane, swarm);
   }

   public static void handle(WeaveFxPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LatticeFx.accept(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
