package com.myriam.unrp.uo;



import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public class ImbuedItemTooltip {
   @SubscribeEvent
   public static void onTooltip(ItemTooltipEvent event) {
      CompoundTag tag = event.getItemStack().getTag();
      if (tag != null && tag.contains("unrp_at_trait")) {
         String trait = tag.getString("unrp_at_trait");
         String label = (String)AuraTransferEvents.TRAIT_LABEL.getOrDefault(trait, trait);
         event.getToolTip().add(Component.literal("Imbued Trait: " + label).withStyle(ChatFormatting.AQUA));
      }
   }
}
