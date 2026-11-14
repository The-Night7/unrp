package com.myriam.unrp.deal;

import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class ContractItem extends Item {
   public static final String NBT_PACT = "unrp_pact";
   public static final String NBT_COPY = "unrp_pact_copy";
   public static final String NBT_SOUL = "unrp_pact_soul";
   public static final String NBT_SIGNED = "unrp_pact_signed";
   public static final String NBT_PROPOSED = "unrp_pact_proposed";
   public static final String NBT_SIGNER = "unrp_pact_signer_name";

   public ContractItem(Item.Properties properties) {
      super(properties);
   }

   public static UUID pactOf(ItemStack stack) {
      if (stack.hasTag() && stack.getTag().contains("unrp_pact")) {
         try {
            return UUID.fromString(stack.getTag().getString("unrp_pact"));
         } catch (IllegalArgumentException var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean isCopy(ItemStack stack) {
      return stack.hasTag() && stack.getTag().getBoolean("unrp_pact_copy");
   }

   public static boolean isProposed(ItemStack stack) {
      return stack.hasTag() && stack.getTag().getBoolean("unrp_pact_proposed");
   }

   public static ItemStack forPact(Item item, PactLedger.Pact pact) {
      ItemStack stack = new ItemStack(item);
      stack.getOrCreateTag().putString("unrp_pact", pact.id.toString());
      syncFromLedger(stack, pact);
      return stack;
   }

   public static void syncFromLedger(ItemStack stack, PactLedger.Pact pact) {
      if (pact != null && stack.hasTag() && stack.getTag().contains("unrp_pact")) {
         CompoundTag tag = stack.getTag();
         if (pact.soul) {
            tag.putBoolean("unrp_pact_soul", true);
         } else {
            tag.remove("unrp_pact_soul");
         }

         if (pact.signed) {
            tag.putBoolean("unrp_pact_signed", true);
            tag.putString("unrp_pact_signer_name", pact.signerName);
         } else {
            tag.remove("unrp_pact_signed");
            tag.remove("unrp_pact_signer_name");
         }

      }
   }

   public boolean hasCraftingRemainingItem(ItemStack stack) {
      return !isCopy(stack);
   }

   public ItemStack getCraftingRemainingItem(ItemStack stack) {
      return isCopy(stack) ? ItemStack.EMPTY : stack.copy();
   }

   public Component getName(ItemStack stack) {
      boolean soul = stack.hasTag() && stack.getTag().getBoolean("unrp_pact_soul");
      boolean proposed = isProposed(stack);
      String base = isCopy(stack) ? "Contract (Copy)" : (proposed ? "Proposed Contract" : "Contract");
      ChatFormatting color = soul ? ChatFormatting.AQUA : (proposed ? ChatFormatting.GOLD : ChatFormatting.RED);
      return Component.literal(base).withStyle(color);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (level instanceof ServerLevel serverLevel) {
         if (player instanceof ServerPlayer sp) {
            DealmakerEvents.openContract(sp, serverLevel, stack, hand);
         }
      }

      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (level instanceof ServerLevel serverLevel) {
         if (!isCopy(stack) && (entity.tickCount + slot) % 20 == 0) {
            UUID pactId = pactOf(stack);
            if (pactId == null) {
               return;
            }

            PactLedger ledger = PactLedger.get(serverLevel.getServer());
            PactLedger.Pact pact = ledger.pact(pactId);
            if (pact == null) {
               stack.shrink(stack.getCount());
               return;
            }

            syncFromLedger(stack, pact);
            String var10000;
            if (entity instanceof Player) {
               Player p = (Player)entity;
               var10000 = p.getName().getString();
            } else {
               var10000 = entity.getName().getString();
            }

            String who = var10000;
            ledger.setLocation(pactId, String.valueOf(serverLevel.dimension().location()) + "|" + entity.getBlockX() + "|" + entity.getBlockY() + "|" + entity.getBlockZ() + "|Carried by " + who);
            return;
         }
      }

   }

   public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
      if (entity.level().isClientSide()) {
         return false;
      } else {
         entity.setUnlimitedLifetime();
         entity.setInvulnerable(true);
         if (isCopy(stack)) {
            return false;
         } else {
            ServerLevel level = (ServerLevel)entity.level();
            UUID pactId = pactOf(stack);
            if (pactId == null) {
               return false;
            } else {
               PactLedger ledger = PactLedger.get(level.getServer());
               PactLedger.Pact pact = ledger.pact(pactId);
               if (pact == null) {
                  entity.discard();
                  return true;
               } else {
                  if (entity.tickCount % 20 == 0) {
                     syncFromLedger(stack, pact);
                     ledger.setLocation(pactId, String.valueOf(level.dimension().location()) + "|" + entity.getBlockX() + "|" + entity.getBlockY() + "|" + entity.getBlockZ() + "|Lying loose");
                  }

                  if (pact.ritual == 1 && level.dimension() == Level.NETHER && level.getBlockState(entity.blockPosition()).is(Blocks.SOUL_FIRE)) {
                     pact.ritual = 2;
                     ledger.setDirty();
                     DealmakerEvents.ritualAdvanced(level, entity, pact);
                  }

                  if (entity.getY() < (double)(level.getMinBuildHeight() - 40)) {
                     DealmakerEvents.contractVoided(level, entity, stack, pact);
                     return true;
                  } else {
                     return false;
                  }
               }
            }
         }
      }
   }

   public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
      boolean soul = stack.hasTag() && stack.getTag().getBoolean("unrp_pact_soul");
      boolean signed = stack.hasTag() && stack.getTag().getBoolean("unrp_pact_signed");
      boolean proposed = isProposed(stack);
      String signer = stack.hasTag() ? stack.getTag().getString("unrp_pact_signer_name") : "";
      if (isCopy(stack)) {
         tooltip.add(Component.literal("A written copy. The terms live elsewhere.").withStyle(ChatFormatting.DARK_GRAY));
         String copySigner = stack.hasTag() ? stack.getTag().getString("dm_copy_signer") : "";
         if (!copySigner.isEmpty() && !copySigner.equals("an unsigned page")) {
            tooltip.add(signatureLine(copySigner));
         }

      } else {
         if (soul) {
            tooltip.add(Component.literal("A pact with a soul on the table.").withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.literal("The paper is cold. Something waits inside it.").withStyle(ChatFormatting.DARK_AQUA));
         } else if (proposed) {
            tooltip.add(Component.literal("Half-blank, hopeful. The petitioner writes first.").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("Only The Dealmaker can countersign it into force.").withStyle(ChatFormatting.DARK_GRAY));
         } else {
            tooltip.add(Component.literal("Terms in red ink. They do not blur.").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.literal("The Dealmaker's terms, awaiting ink.").withStyle(ChatFormatting.DARK_GRAY));
         }

         tooltip.add(Component.literal("It won't burn or fade, and it finds its way back.").withStyle(ChatFormatting.DARK_GRAY));
         if (signed && !signer.isEmpty()) {
            tooltip.add(Component.literal(" "));
            tooltip.add(signatureLine(signer));
         } else {
            tooltip.add(Component.literal(" "));
            tooltip.add(Component.literal("x ______________").withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
         }

      }
   }

   private static Component signatureLine(String signer) {
      return Component.literal("x ").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal(signer).withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.ITALIC})).append(Component.literal("  [SEALED]").withStyle(ChatFormatting.DARK_RED));
   }
}
