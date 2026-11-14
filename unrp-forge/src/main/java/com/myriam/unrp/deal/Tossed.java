package com.myriam.unrp.deal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;

public final class Tossed {
   public static final String THROWER = "Thrower";

   private Tossed() {
   }

   public static boolean byHand(ItemEntity e) {
      return e.saveWithoutId(new CompoundTag()).hasUUID("Thrower");
   }
}
