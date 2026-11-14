package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class SaltFxPacket {
   public static final int GATHER = 0;
   public static final int LANCE = 1;
   public static final int LANCE_STOP = 2;
   public static final int BURST = 3;
   public static final int SHARDS = 4;
   public static final int PILLAR = 5;
   public static final int UNSALT = 6;
   public static final int SHATTER = 7;
   public static final int CRUMBLE = 8;
   public static final int GREAT_FORM = 9;
   public static final int GREAT = 10;
   public static final int GREAT_STOP = 11;
   public static final int GREAT_BURST = 12;
   public static final int PUFF = 13;
   public static final int BLOCK_CRUMBLE = 14;
   public static final int FIZZ = 15;
   public static final int SPIRES = 16;
   public static final int SEAL_CAST = 17;
   public static final int SEAL = 18;
   public static final int RISE = 19;
   public static final int WINGS = 20;
   public static final int HORN = 21;
   public static final int TRUMPET = 22;
   public static final int SMITE = 23;
   public static final int END = 24;
   public static final int UNBOUND = 25;
   public static final int ERUPT = 26;
   public static final int BLAST = 27;
   public static final int BLINK = 28;
   public static final int RAKE = 29;
   public static final int HOWL = 30;
   public static final int THORNS = 31;
   public static final int REND = 32;
   public static final int KINDS = 33;
   public static final int STUCK = 1;
   public static final int WAKING = 2;
   public static final int WOKE = 4;
   public static final int FLAGS = 7;
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

   public SaltFxPacket(int caster, int kind, int slot, int target, float x, float y, float z, float x2, float y2, float z2, int age, int value, int flags) {
      this.caster = caster;
      this.kind = Math.max(0, Math.min(32, kind));
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
      this.flags = flags & 7;
   }

   private static float fit(float v) {
      return Float.isNaN(v) ? 0.0F : Math.max(-3.0E7F, Math.min(3.0E7F, v));
   }

   public static void encode(SaltFxPacket msg, FriendlyByteBuf buf) {
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

   public static SaltFxPacket decode(FriendlyByteBuf buf) {
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
      return new SaltFxPacket(caster, kind, slot, target, x, y, z, x2, y2, z2, age, value, flags);
   }

   public static void handle(SaltFxPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSaltSeraph.accept(msg)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
