package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class LashFxPacket {
   public static final int LINE = 0;
   public static final int VAULT = 1;
   public static final int SWING = 2;
   public static final int SLING = 3;
   public static final int SNAP = 4;
   public static final int LAND = 5;
   public static final int COIL = 6;
   public static final int THROWN = 7;
   public static final int SLAM = 8;
   public static final int WHIP = 9;
   public static final int WARD = 10;
   public static final int WARD_LASH = 11;
   public static final int STORM = 12;
   public static final int STORM_STRIKE = 13;
   public static final int FINALE = 14;
   public static final int FRENZY = 15;
   public static final int STRAIN = 16;
   public static final int END = 17;
   public static final int CATCH = 18;
   public static final int HOLD = 19;
   public static final int FALL = 20;
   public static final int RISE = 21;
   public static final int WIND = 22;
   public static final int WIND_LASH = 23;
   public static final int SPENT = 24;
   public static final int FRENZY_STRIKE = 25;
   public static final int POUND = 26;
   public static final int KINDS = 27;
   public static final int PULL = 1;
   public static final int MISS = 2;
   public static final int LEFT = 4;
   public static final int UP = 8;
   public static final int RAGE = 16;
   public static final int BIG = 32;
   public static final int FLAGS = 63;
   public static final int MOST_AGE = 32000;
   public static final int MOST_VALUE = 10000;
   public static final int MOST_SLOT = 31;
   public static final float FAR = 3.0E7F;
   public final int caster;
   public final int kind;
   public final int slot;
   public final int target;
   public final float x;
   public final float y;
   public final float z;
   public final float x2;
   public final float y2;
   public final float z2;
   public final int age;
   public final int value;
   public final int flags;

   public LashFxPacket(int caster, int kind, int slot, int target, float x, float y, float z, float x2, float y2, float z2, int age, int value, int flags) {
      this.caster = caster;
      this.kind = Math.max(0, Math.min(26, kind));
      this.slot = Math.max(0, Math.min(31, slot));
      this.target = Math.max(0, target);
      this.x = fit(x);
      this.y = fit(y);
      this.z = fit(z);
      this.x2 = fit(x2);
      this.y2 = fit(y2);
      this.z2 = fit(z2);
      this.age = Math.max(0, Math.min(32000, age));
      this.value = Math.max(0, Math.min(10000, value));
      this.flags = flags & 63;
   }

   private static float fit(float v) {
      return Float.isNaN(v) ? 0.0F : Math.max(-3.0E7F, Math.min(3.0E7F, v));
   }

   public static void encode(LashFxPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.caster);
      buf.writeByte(msg.kind);
      buf.writeByte(msg.slot);
      buf.writeVarInt(msg.target);
      buf.writeFloat(msg.x);
      buf.writeFloat(msg.y);
      buf.writeFloat(msg.z);
      buf.writeFloat(msg.x2);
      buf.writeFloat(msg.y2);
      buf.writeFloat(msg.z2);
      buf.writeVarInt(msg.age);
      buf.writeVarInt(msg.value);
      buf.writeByte(msg.flags);
   }

   public static LashFxPacket decode(FriendlyByteBuf buf) {
      int caster = buf.readVarInt();
      int kind = buf.readUnsignedByte();
      int slot = buf.readUnsignedByte();
      int target = buf.readVarInt();
      float x = buf.readFloat();
      float y = buf.readFloat();
      float z = buf.readFloat();
      float x2 = buf.readFloat();
      float y2 = buf.readFloat();
      float z2 = buf.readFloat();
      int age = buf.readVarInt();
      int value = buf.readVarInt();
      int flags = buf.readUnsignedByte();
      return new LashFxPacket(caster, kind, slot, target, x, y, z, x2, y2, z2, age, value, flags);
   }

   public static void handle(LashFxPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientBlackLash.accept(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
