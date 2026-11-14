package com.myriam.unrp.uo;



import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SaltSpearItem extends Item {
   public SaltSpearItem(Item.Properties properties) {
      super(properties);
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (!level.isClientSide()) {
         stack.setCount(0);
      }

   }
}
