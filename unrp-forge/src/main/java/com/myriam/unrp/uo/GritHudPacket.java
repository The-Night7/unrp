package com.myriam.unrp.uo;



import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class GritHudPacket {
   private final int grit;
   private final int cap;
   private final int level;
   private final int xp;
   private final int xpNext;
   private final int edge;
   private final int form;
   private final int tier;
   private final int ki;
   private final int kiCap;

   public GritHudPacket(int grit, int cap, int level, int xp, int xpNext, int edge, int form, int tier, int ki, int kiCap) {
      this.grit = grit;
      this.cap = cap;
      this.level = level;
      this.xp = xp;
      this.xpNext = xpNext;
      this.edge = edge;
      this.form = form;
      this.tier = tier;
      this.ki = ki;
      this.kiCap = kiCap;
   }

   public static void encode(GritHudPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.grit);
      buf.writeVarInt(msg.cap);
      buf.writeVarInt(msg.level);
      buf.writeVarInt(msg.xp);
      buf.writeVarInt(msg.xpNext);
      buf.writeByte(msg.edge);
      buf.writeByte(msg.form);
      buf.writeByte(msg.tier);
      buf.writeVarInt(msg.ki);
      buf.writeVarInt(msg.kiCap);
   }

   public static GritHudPacket decode(FriendlyByteBuf buf) {
      int grit = buf.readVarInt();
      int cap = buf.readVarInt();
      int level = buf.readVarInt();
      int xp = buf.readVarInt();
      int xpNext = buf.readVarInt();
      int edge = buf.readByte();
      int form = buf.readByte();
      int tier = buf.readByte();
      int ki = buf.readVarInt();
      int kiCap = buf.readVarInt();
      return new GritHudPacket(grit, cap, level, xp, xpNext, edge, form, tier, ki, kiCap);
   }

   public static void handle(GritHudPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> GritHudState.set(msg.grit, msg.cap, msg.level, msg.xp, msg.xpNext, msg.edge, msg.form, msg.tier, msg.ki, msg.kiCap)));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
