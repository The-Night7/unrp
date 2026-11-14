package com.myriam.unrp.uo;



import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class OriginLookPacket {
   public static final int COLOSSUS = 1;
   public static final int SHELL = 2;
   public static final int BANNERS = 3;
   public static final int RECORD = 4;
   public static final int HOLD = 5;
   public static final int BALANCE = 6;
   public static final int SCAR = 7;
   public static final int ASCEND = 8;
   public static final int AFTERIMAGE = 9;
   public static final int COUNTER = 10;
   public static final int STILLNESS = 11;
   public static final int NOVA = 12;
   public static final int SEAL = 13;
   public static final int RIP = 14;
   public static final int IMBUE = 15;
   public static final int BORROWED = 16;
   public static final int STONE = 17;
   public static final int EYES = 18;
   public static final int HELD = 19;
   public static final int SARC_HOLD = 20;
   public static final int PHAGE = 21;
   public static final int PH_DEVOUR = 1;
   public static final int PH_BURROW = 2;
   public static final int PH_ERUPT = 3;
   public static final int PH_ROAR = 4;
   public static final int PH_LASH = 5;
   public static final int PH_OBELISK = 6;
   public static final int PH_SINK = 7;
   public static final int PH_SPIT = 8;
   public static final int PH_RISE = 9;
   public static final int PH_HOLD = 10;
   public static final int SEAL_UNROLL = 10;
   public static final int SEAL_LINE = 5;
   public static final int SEAL_SIGN = 10;
   public static final int SEAL_WAX = 6;
   public static final int SEAL_THREAD = 16;
   public static final int SEAL_ROLL = 14;
   public static final int MOST_LINES = 8;
   public static final int MOST_VALUES = 64;
   public static final int MOST_NAMES = 4;
   public final int kind;
   public final int entity;
   public final double[] v;
   public final List<String> names;
   public final int life;

   public OriginLookPacket(int kind, int entity, double[] v, List<String> names, int life) {
      this.kind = kind;
      this.entity = entity;
      this.v = v;
      this.names = names;
      this.life = life;
   }

   public double get(int i) {
      return i >= 0 && i < this.v.length ? this.v[i] : 0.0D;
   }

   public static int sealWritten(int lines) {
      return 10 + 5 * lines;
   }

   public static int sealSigned(int lines) {
      return sealWritten(lines) + 10;
   }

   public static int sealWaxed(int lines) {
      return sealSigned(lines) + 6;
   }

   public static int sealThreaded(int lines) {
      return sealWaxed(lines) + 16;
   }

   public static int sealLife(int lines) {
      return sealThreaded(lines) + 14;
   }

   public static void encode(OriginLookPacket msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.kind);
      buf.writeVarInt(msg.entity);
      int n = Math.min(64, msg.v.length);
      buf.writeVarInt(n);

      for(int i = 0; i < n; ++i) {
         buf.writeDouble(msg.v[i]);
      }

      int k = Math.min(4, msg.names.size());
      buf.writeVarInt(k);

      for(int i = 0; i < k; ++i) {
         String name = (String)msg.names.get(i);
         buf.writeUtf(name.length() <= 256 ? name : name.substring(0, 256), 256);
      }

      buf.writeVarInt(msg.life);
   }

   public static OriginLookPacket decode(FriendlyByteBuf buf) {
      int kind = buf.readByte();
      int entity = buf.readVarInt();
      int n = Math.min(64, buf.readVarInt());
      double[] v = new double[n];

      for(int i = 0; i < n; ++i) {
         v[i] = buf.readDouble();
      }

      int k = Math.min(4, buf.readVarInt());
      List<String> names = new ArrayList();

      for(int i = 0; i < k; ++i) {
         names.add(buf.readUtf(256));
      }

      int life = buf.readVarInt();
      return new OriginLookPacket(kind, entity, v, names, life);
   }

   public static void handle(OriginLookPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientOriginLooks.accept(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
