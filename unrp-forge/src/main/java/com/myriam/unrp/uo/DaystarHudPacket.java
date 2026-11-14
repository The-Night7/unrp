package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class DaystarHudPacket {
   public static final int MOST = 6000;
   private final int noonLeft;
   private final int burnLeft;
   private final boolean used;

   public DaystarHudPacket(int noonLeft, int burnLeft, boolean used) {
      this.noonLeft = noonLeft;
      this.burnLeft = burnLeft;
      this.used = used;
   }

   public static void encode(DaystarHudPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(Math.max(0, Math.min(6000, msg.noonLeft)));
      buf.writeVarInt(Math.max(0, Math.min(6000, msg.burnLeft)));
      buf.writeBoolean(msg.used);
   }

   public static DaystarHudPacket decode(FriendlyByteBuf buf) {
      int noon = Math.max(0, Math.min(6000, buf.readVarInt()));
      int burn = Math.max(0, Math.min(6000, buf.readVarInt()));
      boolean used = buf.readBoolean();
      return new DaystarHudPacket(noon, burn, used);
   }

   public static void handle(DaystarHudPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DaystarHudState.set(msg.noonLeft, msg.burnLeft, msg.used)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
