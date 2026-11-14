package com.myriam.unrp.uo;



import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

public class SunsteelAxeItem extends Item {
   public static final String MARK = "unrp_ds_axe";
   public static final String OWNER = "unrp_ds_owner";
   public static final String BORN = "unrp_ds_born";
   public static final String BRIEF = "unrp_ds_brief";
   public static final double DAMAGE = 10.0D;
   public static final double SPEED = -3.0D;
   public static final double REACH = 0.6D;
   public static final String NAME = "item.unrp.sunsteel_axe";
   private static final UUID REACH_UUID = UUID.fromString("1f15a541-501e-44ee-af9f-c9f6a6e43171");
   private Multimap<Attribute, AttributeModifier> mods;

   public SunsteelAxeItem(Item.Properties properties) {
      super(properties);
   }

   public static boolean isAxe(ItemStack stack) {
      CompoundTag tag = stack.getTag();
      return stack.getItem() instanceof SunsteelAxeItem && tag != null && tag.getBoolean("unrp_ds_axe");
   }

   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
      if (slot != EquipmentSlot.MAINHAND) {
         return super.getDefaultAttributeModifiers(slot);
      } else {
         if (this.mods == null) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
            b.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 10.0D, Operation.ADDITION));
            b.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -3.0D, Operation.ADDITION));
            b.put((Attribute)ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_UUID, "Sunsteel reach", 0.6D, Operation.ADDITION));
            this.mods = b.build();
         }

         return this.mods;
      }
   }

   public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
      return true;
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (!level.isClientSide()) {
         CompoundTag tag = stack.getTag();
         if (tag == null || !tag.getBoolean("unrp_ds_axe")) {
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

   public boolean canFitInsideContainerItems() {
      return false;
   }

   public Component getName(ItemStack stack) {
      return Component.translatable("item.unrp.sunsteel_axe").withStyle(ChatFormatting.GOLD);
   }

   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.literal("A great axe drawn out of the light.").withStyle(ChatFormatting.GOLD));
      tooltip.add(Component.literal("Its weight follows the sun. Dissolves when dropped or when Daystar ends.").withStyle(ChatFormatting.DARK_GRAY));
   }
}
