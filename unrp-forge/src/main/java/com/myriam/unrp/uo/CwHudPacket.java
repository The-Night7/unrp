package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class CwHudPacket {
   private final int charge;
   private final int cap;
   private final int momentum;
   private final int stage;
   private final int frame;
   private final int links;
   private final int flags;

   public CwHudPacket(int charge, int cap, int momentum, int stage, int frame, int links, int flags) {
      this.charge = charge;
      this.cap = cap;
      this.momentum = momentum;
      this.stage = stage;
      this.frame = frame;
      this.links = links;
      this.flags = flags;
   }

   public static void encode(CwHudPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.charge);
      buf.writeVarInt(msg.cap);
      buf.writeVarInt(msg.momentum);
      buf.writeByte(msg.stage);
      buf.writeByte(msg.frame);
      buf.writeByte(msg.links);
      buf.writeByte(msg.flags);
   }

   public static CwHudPacket decode(FriendlyByteBuf buf) {
      int charge = buf.readVarInt();
      int cap = buf.readVarInt();
      int momentum = buf.readVarInt();
      int stage = buf.readByte();
      int frame = buf.readByte();
      int links = buf.readByte();
      int flags = buf.readByte();
      return new CwHudPacket(charge, cap, momentum, stage, frame, links, flags);
   }

   public static void handle(CwHudPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CwHudState.set(msg.charge, msg.cap, msg.momentum, msg.stage, msg.frame, msg.links, msg.flags)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
