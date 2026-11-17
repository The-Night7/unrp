package com.myriam.unrp.pass;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.AbstractVillager;

final class ScaleTable {
    private ScaleTable() {}

   static float of(LivingEntity e) {
      if (!(e instanceof Player) && !(e instanceof Witch) && !(e instanceof AbstractIllager)) {
         ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
         String id = key == null ? "" : key.toString();
         if (id.equals("minecraft:cave_spider")) {
            return 0.7F;
         } else if (!id.equals("minecraft:wither_skeleton") && !id.equals("minecraft:polar_bear")) {
            if (id.equals("minecraft:husk")) {
               return 1.0625F;
            } else if (id.equals("minecraft:horse")) {
               return 1.1F;
            } else if (id.equals("minecraft:donkey")) {
               return 0.87F;
            } else if (id.equals("minecraft:mule")) {
               return 0.92F;
            } else if (id.equals("minecraft:cat")) {
               return 0.8F;
            } else if (id.equals("minecraft:bat")) {
               return 0.35F;
            } else if (e instanceof AbstractVillager) {
               return e.isBaby() ? 0.46875F : 0.9375F;
            } else {
               return 1.0F;
            }
         } else {
            return 1.2F;
         }
      } else {
         return 0.9375F;
      }
   }
}
