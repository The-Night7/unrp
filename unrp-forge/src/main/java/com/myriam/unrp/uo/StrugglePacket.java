package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class StrugglePacket {
   private final boolean active;
   private final int kind;
   private final int progress;

   public StrugglePacket(boolean active, int kind, int progress) {
      this.active = active;
      this.kind = kind;
      this.progress = progress;
   }

   public static void encode(StrugglePacket msg, FriendlyByteBuf buf) {
      buf.writeBoolean(msg.active);
      buf.writeByte(msg.kind);
      buf.writeByte(msg.progress);
   }

   public static StrugglePacket decode(FriendlyByteBuf buf) {
      boolean active = buf.readBoolean();
      int kind = buf.readByte();
      int progress = buf.readByte();
      return new StrugglePacket(active, kind, progress);
   }

   public static void handle(StrugglePacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> StruggleClientState.set(msg.active, msg.kind, msg.progress)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
