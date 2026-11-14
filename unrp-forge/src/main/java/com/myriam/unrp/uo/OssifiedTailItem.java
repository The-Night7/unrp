package com.myriam.unrp.uo;



import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class OssifiedTailItem extends SwordItem {
   public static final String NBT_BORN = "unrp_tail_born";
   public static final int LIFETIME_TICKS = 6000;

   public OssifiedTailItem(Item.Properties properties) {
      super(Tiers.IRON, 4, -2.8F, properties);
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      target.addTag("unrp.sc_club_hit");
      return super.hurtEnemy(stack, target, attacker);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide() && player instanceof ServerPlayer sp) {
         if (!sp.getTags().contains("unrp.has.sarcophage")) {
            return InteractionResultHolder.pass(stack);
         } else if (!sp.getTags().contains("unrp.sc_severed")) {
            sp.displayClientMessage(Component.literal("§7Your spine is whole. The club stays a club."), true);
            return InteractionResultHolder.fail(stack);
         } else {
            boolean regrown = SarcophageEvents.regrowFromClub(sp);
            if (!regrown) {
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

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (!level.isClientSide()) {
         if (stack.hasTag() && stack.getTag().contains("unrp_tail_born")) {
            long born = stack.getTag().getLong("unrp_tail_born");
            if (level.getGameTime() - born >= 6000L) {
               stack.shrink(stack.getCount());
               if (entity instanceof Player) {
                  Player player = (Player)entity;
                  player.displayClientMessage(Component.literal("§7The ossified tail crumbles back into drifting sand."), true);
               }
            }

         } else {
            stack.getOrCreateTag().putLong("unrp_tail_born", level.getGameTime());
         }
      }
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.literal("A severed tail, hardened to bone-club.").withStyle(ChatFormatting.GOLD));
      tooltip.add(Component.literal("Living sand. It crumbles after five minutes.").withStyle(ChatFormatting.DARK_GRAY));
      tooltip.add(Component.literal("The Sarcophage can consume it to regrow the tail.").withStyle(ChatFormatting.DARK_GRAY));
      tooltip.add(Component.literal("Anyone can carry it.").withStyle(ChatFormatting.DARK_GRAY));
   }
}
