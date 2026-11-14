package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class LashMovePacket {
   public static final float MOST_MOVE = 8.0F;
   public static final float MOST_ROPE = 64.0F;
   public final float dx;
   public final float dy;
   public final float dz;
   public final double ax;
   public final double ay;
   public final double az;
   public final float rope;

   public LashMovePacket(double dx, double dy, double dz, double ax, double ay, double az, double rope) {
      this.dx = fit(dx);
      this.dy = fit(dy);
      this.dz = fit(dz);
      this.ax = Double.isFinite(ax) ? ax : 0.0D;
      this.ay = Double.isFinite(ay) ? ay : 0.0D;
      this.az = Double.isFinite(az) ? az : 0.0D;
      this.rope = Double.isFinite(rope) ? (float)Math.max(0.0D, Math.min(64.0D, rope)) : 0.0F;
   }

   static float fit(double v) {
      return Double.isFinite(v) ? (float)Math.max(-8.0D, Math.min(8.0D, v)) : 0.0F;
   }

   public static void encode(LashMovePacket msg, FriendlyByteBuf buf) {
      buf.writeFloat(msg.dx);
      buf.writeFloat(msg.dy);
      buf.writeFloat(msg.dz);
      buf.writeDouble(msg.ax);
      buf.writeDouble(msg.ay);
      buf.writeDouble(msg.az);
      buf.writeFloat(msg.rope);
   }

   public static LashMovePacket decode(FriendlyByteBuf buf) {
      float dx = buf.readFloat();
      float dy = buf.readFloat();
      float dz = buf.readFloat();
      double ax = buf.readDouble();
      double ay = buf.readDouble();
      double az = buf.readDouble();
      float rope = buf.readFloat();
      return new LashMovePacket((double)dx, (double)dy, (double)dz, ax, ay, az, (double)rope);
   }

   public static void handle(LashMovePacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LashSwingInput.move((double)msg.dx, (double)msg.dy, (double)msg.dz, msg.ax, msg.ay, msg.az, (double)msg.rope)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
