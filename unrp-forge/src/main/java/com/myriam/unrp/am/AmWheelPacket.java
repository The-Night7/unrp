package com.myriam.unrp.am;

import com.myriam.unrp.pass.AmRoster;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class AmWheelPacket {
   public final int aura;
   public final int copiedCount;
   public final List<int[]> nearby;
   public final boolean[] comboReady;
   public final boolean[] comboHeld;
   public final boolean open;

   public AmWheelPacket(int aura, int copiedCount, List<int[]> nearby, boolean[] comboReady, boolean[] comboHeld, boolean open) {
      this.aura = aura;
      this.copiedCount = copiedCount;
      this.nearby = nearby;
      this.comboReady = comboReady;
      this.comboHeld = comboHeld;
      this.open = open;
   }

   public static void encode(AmWheelPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.aura);
      buf.writeVarInt(msg.copiedCount);
      buf.writeVarInt(msg.nearby.size());

      for(int[] entry : msg.nearby) {
         buf.writeByte(entry[0]);
         buf.writeBoolean(entry[1] != 0);
      }

      buf.writeVarInt(msg.comboReady.length);

      for(int i = 0; i < msg.comboReady.length; ++i) {
         buf.writeBoolean(msg.comboReady[i]);
         buf.writeBoolean(msg.comboHeld[i]);
      }

      buf.writeBoolean(msg.open);
   }

   public static AmWheelPacket decode(FriendlyByteBuf buf) {
      int aura = buf.readVarInt();
      int copied = buf.readVarInt();
      int n = buf.readVarInt();
      List<int[]> nearby = new ArrayList();

      for(int i = 0; i < n; ++i) {
         int slot = buf.readByte() & 255;
         boolean has = buf.readBoolean();
         nearby.add(new int[]{slot, has ? 1 : 0});
      }

      int c = buf.readVarInt();
      boolean[] ready = new boolean[c];
      boolean[] held = new boolean[c];

      for(int i = 0; i < c; ++i) {
         ready[i] = buf.readBoolean();
         held[i] = buf.readBoolean();
      }

      boolean open = buf.readBoolean();
      return new AmWheelPacket(aura, copied, nearby, ready, held, open);
   }

   public static void handle(AmWheelPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AmWheelScreen.accept(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
