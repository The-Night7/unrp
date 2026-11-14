package com.myriam.unrp.uo;



import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class BoneCharmItem extends Item {
   public static final String NBT_KIND = "unrp_charm_kind";
   public static final String[] KINDS = new String[]{"swift", "vigor", "ward", "reach", "sight", "grave_stride", "hollow_sight", "marrow_ward", "bone_greed"};
   public static final int PURE = 5;
   public static final float CORRUPT_CHANCE = 0.25F;
   public static final String[] KIND_NAME = new String[]{"Charm of the Quick Step", "Charm of the Long Breath", "Charm of the Grey Ward", "Charm of the Far Hand", "Charm of the Open Eye", "Corrupted Charm of the Grave Stride", "Corrupted Charm of the Hollow Sight", "Corrupted Charm of the Marrow Ward", "Corrupted Charm of Bone Greed"};
   public static final String[] KIND_TEXT = new String[]{"Your steps come a little easier.", "The hollow leaves you a little more health.", "The grey drinks a little of the blow.", "Your Blink reaches a little farther.", "You see a little better in the dark.", "Your Blink reaches six blocks farther.", "Your Void Gaze sees half again as far.", "The grey drinks a fifth of every blow.", "The bones give up more runes."};
   public static final String[] KIND_COST = new String[]{"", "", "", "", "", "It takes four of your health while worn.", "Now and then the dark closes over your eyes.", "Healing reaches you a third weaker.", "A hunger that won't settle."};

   public static boolean corrupted(String kind) {
      return indexOf(kind) >= 5;
   }

   public static String randomKind(RandomSource rnd) {
      return rnd.nextFloat() < 0.25F ? KINDS[5 + rnd.nextInt(KINDS.length - 5)] : KINDS[rnd.nextInt(5)];
   }

   public BoneCharmItem(Item.Properties properties) {
      super(properties);
   }

   public static String kindOf(ItemStack stack) {
      return stack.hasTag() && stack.getOrCreateTag().contains("unrp_charm_kind") ? stack.getOrCreateTag().getString("unrp_charm_kind") : KINDS[0];
   }

   public static ItemStack of(Item item, String kind) {
      ItemStack stack = new ItemStack(item);
      stack.getOrCreateTag().putString("unrp_charm_kind", kind);
      return stack;
   }

   public static int indexOf(String kind) {
      for(int i = 0; i < KINDS.length; ++i) {
         if (KINDS[i].equals(kind)) {
            return i;
         }
      }

      return 0;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide() && player instanceof ServerPlayer sp) {
         if (HollowMarkEvents.attuneCharm(sp, kindOf(stack))) {
            stack.shrink(1);
            return InteractionResultHolder.success(stack);
         } else {
            return InteractionResultHolder.fail(stack);
         }
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   public Component getName(ItemStack stack) {
      return Component.literal(KIND_NAME[indexOf(kindOf(stack))]);
   }

   public void appendHoverText(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
      int i = indexOf(kindOf(stack));
      tip.add(Component.literal(KIND_TEXT[i]).withStyle(ChatFormatting.DARK_GRAY));
      if (!KIND_COST[i].isEmpty()) {
         tip.add(Component.literal(KIND_COST[i]).withStyle(ChatFormatting.DARK_RED));
         tip.add(Component.literal("It whispers while attuned.").withStyle(ChatFormatting.DARK_PURPLE));
      }

      tip.add(Component.literal("Right-click to attune.").withStyle(ChatFormatting.AQUA));
   }
}
