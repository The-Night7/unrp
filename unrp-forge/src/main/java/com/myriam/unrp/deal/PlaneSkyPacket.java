package com.myriam.unrp.deal;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class PlaneSkyPacket {
   public final boolean present;
   public final UUID plotOwner;
   public final String power;

   public PlaneSkyPacket(boolean present, UUID plotOwner, String power) {
      this.present = present;
      this.plotOwner = plotOwner;
      this.power = power;
   }

   public static void encode(PlaneSkyPacket msg, FriendlyByteBuf buf) {
      buf.writeBoolean(msg.present);
      buf.writeUUID(msg.plotOwner == null ? new UUID(0L, 0L) : msg.plotOwner);
      buf.writeUtf(msg.power == null ? "" : msg.power, 64);
   }

   public static PlaneSkyPacket decode(FriendlyByteBuf buf) {
      return new PlaneSkyPacket(buf.readBoolean(), buf.readUUID(), buf.readUtf(64));
   }

   public static void handle(PlaneSkyPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SoulPlaneSky.accept(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
