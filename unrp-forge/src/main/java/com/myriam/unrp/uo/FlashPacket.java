package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class FlashPacket {
   private final int durationTicks;

   public FlashPacket(int durationTicks) {
      this.durationTicks = durationTicks;
   }

   public static void encode(FlashPacket msg, FriendlyByteBuf buf) {
      buf.writeInt(msg.durationTicks);
   }

   public static FlashPacket decode(FriendlyByteBuf buf) {
      return new FlashPacket(buf.readInt());
   }

   public static void handle(FlashPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         NetworkEvent.Context context = (NetworkEvent.Context)ctx.get();
         context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> IlluminationFlashOverlay.trigger(msg.durationTicks)));
         context.setPacketHandled(true);
      }
   }
}
