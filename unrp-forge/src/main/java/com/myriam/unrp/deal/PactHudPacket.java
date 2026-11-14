package com.myriam.unrp.deal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class PactHudPacket {
   public final String[] allyNames;
   public final int[] allyHp;
   public final int[] allyFlags;
   public final List<String> info;

   public PactHudPacket(String[] allyNames, int[] allyHp, int[] allyFlags, List<String> info) {
      this.allyNames = allyNames;
      this.allyHp = allyHp;
      this.allyFlags = allyFlags;
      this.info = info;
   }

   public static void encode(PactHudPacket msg, FriendlyByteBuf buf) {
      for(int i = 0; i < 3; ++i) {
         buf.writeUtf(msg.allyNames[i] == null ? "" : msg.allyNames[i], 40);
         buf.writeByte(msg.allyHp[i]);
         buf.writeByte(msg.allyFlags[i]);
      }

      buf.writeVarInt(msg.info.size());

      for(String s : msg.info) {
         buf.writeUtf(s, 128);
      }

   }

   public static PactHudPacket decode(FriendlyByteBuf buf) {
      String[] names = new String[3];
      int[] hp = new int[3];
      int[] flags = new int[3];

      for(int i = 0; i < 3; ++i) {
         names[i] = buf.readUtf(40);
         hp[i] = buf.readByte();
         flags[i] = buf.readByte();
      }

      int n = buf.readVarInt();
      List<String> info = new ArrayList();

      for(int i = 0; i < n; ++i) {
         info.add(buf.readUtf(128));
      }

      return new PactHudPacket(names, hp, flags, info);
   }

   public static void handle(PactHudPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PactHudState.set(msg.allyNames, msg.allyHp, msg.allyFlags, msg.info)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
