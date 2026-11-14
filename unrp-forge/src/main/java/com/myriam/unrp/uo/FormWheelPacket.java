package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class FormWheelPacket {
   public final int mask;
   public final int current;
   public final boolean rose;
   public final int level;
   public final int grit;
   public final int[] costs;
   public final boolean auraOff;

   public FormWheelPacket(int mask, int current, boolean rose, int level, int grit, int[] costs, boolean auraOff) {
      this.mask = mask;
      this.current = current;
      this.rose = rose;
      this.level = level;
      this.grit = grit;
      this.costs = costs;
      this.auraOff = auraOff;
   }

   public static void encode(FormWheelPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.mask);
      buf.writeByte(msg.current);
      buf.writeBoolean(msg.rose);
      buf.writeVarInt(msg.level);
      buf.writeVarInt(msg.grit);
      buf.writeVarIntArray(msg.costs);
      buf.writeBoolean(msg.auraOff);
   }

   public static FormWheelPacket decode(FriendlyByteBuf buf) {
      int mask = buf.readVarInt();
      int current = buf.readByte();
      boolean rose = buf.readBoolean();
      int level = buf.readVarInt();
      int grit = buf.readVarInt();
      int[] costs = buf.readVarIntArray();
      boolean auraOff = buf.readBoolean();
      return new FormWheelPacket(mask, current, rose, level, grit, costs, auraOff);
   }

   public static void handle(FormWheelPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FormWheelScreen.open(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
