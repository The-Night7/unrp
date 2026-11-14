package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class SaltSteerPacket {
   public static final int OFF = 0;
   public static final int HOLD = 1;
   public static final int RUN = 2;
   public static final int SLOW = 3;
   public static final int CLOSE = 4;
   public static final int IDLE = 5;
   public static final int MODES = 6;
   public static final float FAR = 3.0E7F;
   public final int mode;
   public final int target;
   public final float x;
   public final float y;
   public final float z;

   public SaltSteerPacket(int mode, int target, float x, float y, float z) {
      this.mode = Math.max(0, Math.min(5, mode));
      this.target = Math.max(0, target);
      this.x = fit(x);
      this.y = fit(y);
      this.z = fit(z);
   }

   private static float fit(float v) {
      return Float.isNaN(v) ? 0.0F : Math.max(-3.0E7F, Math.min(3.0E7F, v));
   }

   public static void encode(SaltSteerPacket msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.mode);
      buf.writeVarInt(msg.target);
      buf.writeFloat(msg.x);
      buf.writeFloat(msg.y);
      buf.writeFloat(msg.z);
   }

   public static SaltSteerPacket decode(FriendlyByteBuf buf) {
      int mode = buf.readUnsignedByte();
      int target = buf.readVarInt();
      float x = buf.readFloat();
      float y = buf.readFloat();
      float z = buf.readFloat();
      return new SaltSteerPacket(mode, target, x, y, z);
   }

   public static void handle(SaltSteerPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SaltAutopilot.steer(msg.mode, msg.target, msg.x, msg.y, msg.z)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
