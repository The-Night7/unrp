package com.myriam.unrp.deal;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class SoulSwordItem extends SwordItem {
   public static final String NBT_ALLY = "unrp_sword_ally";
   public static final String NBT_DMG = "unrp_sword_dmg";
   public static final String NBT_REACH = "unrp_sword_reach";
   public static final double TRUE_SHARE = 0.25D;
   private static final UUID DMG_UUID = UUID.fromString("8c1b9a44-2f77-4c3e-9d55-7374c6fc0001");
   private static final UUID SPD_UUID = UUID.fromString("8c1b9a44-2f77-4c3e-9d55-7374c6fc0002");

   public SoulSwordItem(Item.Properties properties) {
      super(Tiers.NETHERITE, 3, -2.4F, properties);
   }

   public static ItemStack forAlly(SoulSwordItem item, UUID ally, int rank) {
      ItemStack stack = new ItemStack(item);
      stack.getOrCreateTag().putString("unrp_sword_ally", ally.toString());
      stack.getOrCreateTag().putDouble("unrp_sword_dmg", 7.0D + (double)rank * 0.12D);
      stack.getOrCreateTag().putDouble("unrp_sword_reach", Math.min(3.0D, (double)rank * 0.05D));
      return stack;
   }

   public static double swordDamage(ItemStack stack) {
      return 1.0D + (stack.hasTag() && stack.getTag().contains("unrp_sword_dmg") ? stack.getTag().getDouble("unrp_sword_dmg") : 7.0D);
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      boolean out = super.hurtEnemy(stack, target, attacker);
      Level cut_0 = attacker.level();
      if (cut_0 instanceof ServerLevel level) {
         if (attacker instanceof ServerPlayer sp) {
            if (allyOf(stack) != null && allyOf(stack).equals(sp.getUUID()) && target.isAlive()) {
               float cut = (float)(swordDamage(stack) * 0.25D);
               if (TrueDamage.deal(level, sp, target, cut)) {
                  Fx.burst(level, Fx.ember(5882600), target.position().add(0.0D, (double)target.getBbHeight() * 0.6D, 0.0D), 6, 0.25D, 0.03D);
               }
            }
         }
      }

      return out;
   }

   public static UUID allyOf(ItemStack stack) {
      if (stack.hasTag() && stack.getTag().contains("unrp_sword_ally")) {
         try {
            return UUID.fromString(stack.getTag().getString("unrp_sword_ally"));
         } catch (IllegalArgumentException var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
      if (slot != EquipmentSlot.MAINHAND) {
         return super.getAttributeModifiers(slot, stack);
      } else {
         double dmg = stack.hasTag() && stack.getTag().contains("unrp_sword_dmg") ? stack.getTag().getDouble("unrp_sword_dmg") : 7.0D;
         ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
         builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(DMG_UUID, "Soul sword damage", dmg, Operation.ADDITION));
         builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(SPD_UUID, "Soul sword speed", -2.2D, Operation.ADDITION));
         return builder.build();
      }
   }

   public boolean onDroppedByPlayer(ItemStack item, Player player) {
      if (!player.level().isClientSide() && player instanceof ServerPlayer sp) {
         sp.displayClientMessage(Component.literal("§bThe Soul Sword belongs to the pact. It stays."), true);
      }

      return false;
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (!level.isClientSide()) {
         if (entity instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)entity;
            UUID ally = allyOf(stack);
            if (ally == null || !ally.equals(sp.getUUID())) {
               if (ally != null && sp.getUUID().equals(PactLedger.get(sp.server).dealmakerOf(ally))) {
                  return;
               }

               DealmakerEvents.swordDissolved(sp, stack);
               stack.shrink(stack.getCount());
               return;
            }

            DealmakerEvents.tickSwordReach(sp, stack, selected);
         }

      }
   }

   public boolean isEnchantable(ItemStack stack) {
      return true;
   }

   public int getEnchantmentValue() {
      return 18;
   }

   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.literal("Drawn from the Dealmaker's own rank.").withStyle(ChatFormatting.AQUA));
      tooltip.add(Component.literal(String.format("A quarter of its edge (%.1f) lands as True Damage.", swordDamage(stack) * 0.25D)).withStyle(ChatFormatting.DARK_AQUA));
      tooltip.add(Component.literal("Can't be dropped. Fades on death, but the pact remembers its edge.").withStyle(ChatFormatting.DARK_GRAY));
   }
}
