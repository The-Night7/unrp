package com.myriam.unrp.deal;

import java.util.UUID;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class SoulBindRecipe extends CustomRecipe {
   public static final String NBT_TARGET = "dm_soulbound_target";
   public static final String NBT_SERIAL = "dm_soulbound_serial";
   public static final String NBT_NAME = "dm_soulbound_name";

   public SoulBindRecipe(ResourceLocation id, CraftingBookCategory category) {
      super(id, category);
   }

   public static boolean eligible(ItemStack stack) {
      return stack.getItem() instanceof TieredItem || stack.getItem() instanceof ArmorItem || stack.getItem() instanceof ElytraItem || stack.getItem() == Items.TOTEM_OF_UNDYING;
   }

   private static ItemStack[] pair(CraftingContainer container) {
      ItemStack soul = ItemStack.EMPTY;
      ItemStack item = ItemStack.EMPTY;

      for(int i = 0; i < container.getContainerSize(); ++i) {
         ItemStack s = container.getItem(i);
         if (!s.isEmpty()) {
            if (s.getItem() == com.myriam.unrp.item.ModItems.SOUL.get()) {
               if (!soul.isEmpty()) {
                  return null;
               }

               soul = s;
            } else {
               if (!eligible(s)) {
                  return null;
               }

               if (!item.isEmpty()) {
                  return null;
               }

               item = s;
            }
         }
      }

      return !soul.isEmpty() && !item.isEmpty() ? new ItemStack[]{soul, item} : null;
   }

   public boolean matches(CraftingContainer container, Level level) {
      ItemStack[] found = pair(container);
      return found != null && SoulItem.targetOf(found[0]) != null;
   }

   public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
      ItemStack[] found = pair(container);
      if (found == null) {
         return ItemStack.EMPTY;
      } else {
         UUID target = SoulItem.targetOf(found[0]);
         if (target == null) {
            return ItemStack.EMPTY;
         } else {
            ItemStack out = found[1].copy();
            out.setCount(1);
            CompoundTag tag = out.getOrCreateTag();
            tag.putString("dm_soulbound_target", target.toString());
            tag.putInt("dm_soulbound_serial", SoulItem.serialOf(found[0]));
            tag.putString("dm_soulbound_name", SoulItem.nameOf(found[0]));
            return out;
         }
      }
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return (RecipeSerializer)DealBridge.SOUL_BIND.get();
   }
}
