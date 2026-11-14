package com.myriam.unrp.uo;



import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public class ArmamentItem extends SwordItem {
   public static final String MARK = "unrp_ad";
   public static final String MODEL = "CustomModelData";
   public static final String[] VARIANTS = new String[]{"oak", "stone", "iron", "gold", "diamond", "netherite", "burst", "sear", "fang", "warp", "rot"};

   public ArmamentItem(Item.Properties properties) {
      super(Tiers.NETHERITE, 0, -2.4F, properties);
   }

   public static int variant(int worth, int proc) {
      if (proc >= 1 && proc <= 5) {
         return 6 + proc;
      } else if (worth >= 10) {
         return 6;
      } else if (worth >= 8) {
         return 5;
      } else if (worth >= 7) {
         return 4;
      } else if (worth >= 5) {
         return 3;
      } else {
         return worth >= 4 ? 2 : 1;
      }
   }

   public static ItemStack display(ItemStack base, int worth, int proc) {
      base.getOrCreateTag().putInt("CustomModelData", variant(worth, proc));
      return base;
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (!level.isClientSide()) {
         CompoundTag tag = stack.getTag();
         if (tag == null || !tag.getBoolean("unrp_ad")) {
            stack.setCount(0);
         }

      }
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
      return false;
   }

   public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
      return false;
   }
}
