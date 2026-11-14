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

public class CarvedRuneItem extends Item {
   public CarvedRuneItem(Item.Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide() && player instanceof ServerPlayer sp) {
         if (sp.getTags().contains("unrp.has.hollow_mark")) {
            HollowMarkEvents.openCodex(sp);
            return InteractionResultHolder.success(stack);
         }
      }

      return InteractionResultHolder.pass(stack);
   }

   public void appendHoverText(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
      tip.add(Component.literal("Cut from an ancient bone.").withStyle(ChatFormatting.DARK_GRAY));
      tip.add(Component.literal("The Void only trades in these.").withStyle(ChatFormatting.DARK_GRAY));
      tip.add(Component.literal("Right-click to open the Codex.").withStyle(ChatFormatting.AQUA));
   }
}
