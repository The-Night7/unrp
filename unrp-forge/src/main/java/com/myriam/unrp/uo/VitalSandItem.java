package com.myriam.unrp.uo;



import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class VitalSandItem extends Item {
   public VitalSandItem(Item.Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide() && player instanceof ServerPlayer sp) {
         if (!sp.getTags().contains("unrp.has.sarcophage")) {
            sp.displayClientMessage(Component.literal("§7The grains slip through your fingers. They answer someone else."), true);
            return InteractionResultHolder.fail(stack);
         } else {
            boolean consumed = SarcophageEvents.consumeVitalSand(sp);
            if (!consumed) {
               return InteractionResultHolder.fail(stack);
            } else {
               stack.shrink(1);
               return InteractionResultHolder.consume(stack);
            }
         }
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.literal("Rust-flecked grains, still warm.").withStyle(ChatFormatting.GOLD));
      tooltip.add(Component.literal("Only the Sarcophage can feed on this.").withStyle(ChatFormatting.DARK_GRAY));
   }
}
