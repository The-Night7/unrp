package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class SaltHudPacket {
   public static final int COOLDOWNS = 6;
   public static final int MOST_TICKS = 72000;
   public static final int MOST_JUDGED = 16;
   public static final int LANCE = 0;
   public static final int GREAT = 1;
   public static final int WALL = 2;
   public static final int SPIRES = 3;
   public static final int SEAL = 4;
   public static final int WAKE = 5;
   public static final int WAKING = 1;
   public static final int TRUMPETED = 2;
   public static final int ERUPTING = 4;
   public static final int UNBOUND = 8;
   public static final int FLAGS = 15;
   private final int[] left = new int[6];
   private final int[] full = new int[6];
   private final int wakeLeft;
   private final int flags;
   private final int[] judged;

   public SaltHudPacket(int[] left, int[] full, int wakeLeft, int flags, int[] judged) {
      for(int i = 0; i < 6; ++i) {
         this.left[i] = left != null && i < left.length ? Math.max(0, Math.min(72000, left[i])) : 0;
         this.full[i] = full != null && i < full.length ? Math.max(1, Math.min(72000, full[i])) : 1;
      }

      this.wakeLeft = Math.max(0, Math.min(72000, wakeLeft));
      this.flags = flags & 15;
      int n = judged == null ? 0 : Math.min(16, judged.length);
      this.judged = new int[n];

      for(int i = 0; i < n; ++i) {
         this.judged[i] = Math.max(0, judged[i]);
      }

   }

   public static void encode(SaltHudPacket msg, FriendlyByteBuf buf) {
      for(int i = 0; i < 6; ++i) {
         buf.writeVarInt(msg.left[i]);
         buf.writeVarInt(msg.full[i]);
      }

      buf.writeVarInt(msg.wakeLeft);
      buf.writeByte(msg.flags);
      buf.writeByte(msg.judged.length);

      for(int id : msg.judged) {
         buf.writeVarInt(id);
      }

   }

   public static SaltHudPacket decode(FriendlyByteBuf buf) {
      int[] left = new int[6];
      int[] full = new int[6];

      for(int i = 0; i < 6; ++i) {
         left[i] = buf.readVarInt();
         full[i] = buf.readVarInt();
      }

      int wakeLeft = buf.readVarInt();
      int flags = buf.readUnsignedByte();
      int n = Math.min(16, buf.readUnsignedByte());
      int[] judged = new int[n];

      for(int i = 0; i < n; ++i) {
         judged[i] = buf.readVarInt();
      }

      return new SaltHudPacket(left, full, wakeLeft, flags, judged);
   }

   public static void handle(SaltHudPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SaltHudState.set(msg.left, msg.full, msg.wakeLeft, msg.flags, msg.judged)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
