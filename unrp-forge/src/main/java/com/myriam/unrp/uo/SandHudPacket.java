package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class SandHudPacket {
   private final int reserve;
   private final int cap;
   private final int lifetime;
   private final int tails;
   private final boolean severed;

   public SandHudPacket(int reserve, int cap, int lifetime, int tails, boolean severed) {
      this.reserve = reserve;
      this.cap = cap;
      this.lifetime = lifetime;
      this.tails = tails;
      this.severed = severed;
   }

   public static void encode(SandHudPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.reserve);
      buf.writeVarInt(msg.cap);
      buf.writeVarInt(msg.lifetime);
      buf.writeByte(msg.tails);
      buf.writeBoolean(msg.severed);
   }

   public static SandHudPacket decode(FriendlyByteBuf buf) {
      int reserve = buf.readVarInt();
      int cap = buf.readVarInt();
      int lifetime = buf.readVarInt();
      int tails = buf.readByte();
      boolean severed = buf.readBoolean();
      return new SandHudPacket(reserve, cap, lifetime, tails, severed);
   }

   public static void handle(SandHudPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SandHudState.set(msg.reserve, msg.cap, msg.lifetime, msg.tails, msg.severed)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
