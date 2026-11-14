package com.myriam.unrp.deal;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class DialogueChoicePacket {
   public final boolean yes;
   public final int org;

   public DialogueChoicePacket(boolean yes, int org) {
      this.yes = yes;
      this.org = org;
   }

   public static void encode(DialogueChoicePacket msg, FriendlyByteBuf buf) {
      buf.writeBoolean(msg.yes);
      buf.writeByte(msg.org);
   }

   public static DialogueChoicePacket decode(FriendlyByteBuf buf) {
      return new DialogueChoicePacket(buf.readBoolean(), buf.readByte());
   }

   public static void handle(DialogueChoicePacket msg, Supplier<NetworkEvent.Context> ctx) {
      ((NetworkEvent.Context)ctx.get()).enqueueWork(() -> {
         ServerPlayer player = ((NetworkEvent.Context)ctx.get()).getSender();
         if (player != null) {
            if (msg.org == 5) DealmakerEvents.onChoice(player, msg.yes, 5); // les autres dialogues du modèle (écoles, quêtes) ne sont pas portés
         }
      });
      ((NetworkEvent.Context)ctx.get()).setPacketHandled(true);
   }
}
