package com.myriam.unrp.deal;

import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class SoulItem extends Item {
   public static final String NBT_TARGET = "unrp_soul_target";
   public static final String NBT_NAME = "unrp_soul_name";
   public static final String NBT_SERIAL = "unrp_soul_serial";
   public static final FoodProperties SOUL_MEAL = (new FoodProperties.Builder()).nutrition(0).saturationMod(0.0F).alwaysEat().build();

   public SoulItem(Item.Properties properties) {
      super(properties);
   }

   public static UUID targetOf(ItemStack stack) {
      if (stack.hasTag() && stack.getTag().contains("unrp_soul_target")) {
         try {
            return UUID.fromString(stack.getTag().getString("unrp_soul_target"));
         } catch (IllegalArgumentException var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static String nameOf(ItemStack stack) {
      return stack.hasTag() ? stack.getTag().getString("unrp_soul_name") : "";
   }

   public static int serialOf(ItemStack stack) {
      return stack.hasTag() ? stack.getTag().getInt("unrp_soul_serial") : 0;
   }

   public static ItemStack forTarget(Item item, UUID target, String name, int serial) {
      ItemStack stack = new ItemStack(item);
      stack.getOrCreateTag().putString("unrp_soul_target", target.toString());
      stack.getOrCreateTag().putString("unrp_soul_name", name == null ? "" : name);
      stack.getOrCreateTag().putInt("unrp_soul_serial", serial);
      return stack;
   }

   public Component getName(ItemStack stack) {
      String name = nameOf(stack);
      return Component.literal(name.isEmpty() ? "Soul" : name + "'s Soul").withStyle(ChatFormatting.AQUA);
   }

   public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity eater) {
      if (level instanceof ServerLevel serverLevel) {
         if (eater instanceof ServerPlayer player) {
            SoulEvents.eaten(player, serverLevel, stack);
         }
      }

      return super.finishUsingItem(stack, level, eater);
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
         UUID target = targetOf(stack);
         if (target == null || serialOf(stack) != PactLedger.get(serverLevel.getServer()).serialOf(target)) {
            stack.shrink(stack.getCount());
         }

      }
   }

   public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
      if (entity.level().isClientSide()) {
         return false;
      } else {
         entity.setUnlimitedLifetime();
         entity.setInvulnerable(true);
         return SoulEvents.soulEntityTick((ServerLevel)entity.level(), entity, stack);
      }
   }

   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.literal("What's done to this is done to them.").withStyle(ChatFormatting.DARK_AQUA));
      tooltip.add(Component.literal("It can't be broken. Spend it and it's gone.").withStyle(ChatFormatting.DARK_GRAY));
   }
}
