package com.myriam.unrp.anim;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class MoveAnimPacket {
   public static final int PLAY = 0;
   public static final int RELEASE = 1;
   public static final int STOP = 2;
   public static final int HELD = 1;
   public static final int MIRROR = 2;
   public final int entity;
   public final int op;
   public final int clip;
   public final int at;
   public final int flags;

   public MoveAnimPacket(int entity, int op, int clip, int at, int flags) {
      this.entity = entity;
      this.op = op;
      this.clip = clip;
      this.at = Math.max(-32000, Math.min(32000, at));
      this.flags = flags;
   }

   public static void encode(MoveAnimPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.entity);
      buf.writeByte(msg.op);
      buf.writeVarInt(msg.clip + 1);
      buf.writeShort(msg.at);
      buf.writeByte(msg.flags);
   }

   public static MoveAnimPacket decode(FriendlyByteBuf buf) {
      int entity = buf.readVarInt();
      int op = buf.readByte();
      int clip = buf.readVarInt() - 1;
      int at = buf.readShort();
      int flags = buf.readByte();
      return new MoveAnimPacket(entity, op, clip, at, flags);
   }

   public static void handle(MoveAnimPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MoveAnimClient.accept(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
