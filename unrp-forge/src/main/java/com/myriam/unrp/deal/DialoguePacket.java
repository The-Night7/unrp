package com.myriam.unrp.deal;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

public class DialoguePacket {
   public final String speaker;
   public final String text;
   public final boolean question;
   public final int org;

   public DialoguePacket(String speaker, String text, boolean question, int org) {
      this.speaker = speaker;
      this.text = text;
      this.question = question;
      this.org = org;
   }

   public static void encode(DialoguePacket msg, FriendlyByteBuf buf) {
      buf.writeUtf(msg.speaker);
      buf.writeUtf(msg.text);
      buf.writeBoolean(msg.question);
      buf.writeByte(msg.org);
   }

   public static DialoguePacket decode(FriendlyByteBuf buf) {
      return new DialoguePacket(buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readByte());
   }

   public static void handle(DialoguePacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (((NetworkEvent.Context)ctx.get()).getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      } else {
         ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> net.minecraft.client.Minecraft.getInstance().setScreen(new LeilahScreen(msg.speaker, msg.text, msg.question, msg.org))));
         ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
      }
   }
}
