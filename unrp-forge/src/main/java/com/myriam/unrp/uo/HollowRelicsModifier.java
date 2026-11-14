package com.myriam.unrp.uo;



import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class HollowRelicsModifier extends LootModifier {
   public static final Supplier<Codec<HollowRelicsModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create((inst) -> codecStart(inst).apply(inst, HollowRelicsModifier::new)));
   public static final float[] VANILLA = new float[]{0.02F, 0.004F};
   public static final float[] DEEP = new float[]{0.05F, 0.01F};
   public static final float[] CAMP = new float[]{0.1F, 0.025F};
   public static final float[] VAULT = new float[]{0.2F, 0.05F};
   public static final float SECOND_RUNE = 0.25F;

   public HollowRelicsModifier(LootItemCondition[] conditions) {
      super(conditions);
   }

   public static float[] odds(String namespace, String path) {
      if (namespace != null && path != null && path.startsWith("chests/")) {
         if (namespace.equals("minecraft")) {
            if (path.equals("chests/spawn_bonus_chest")) {
               return null;
            } else {
               return !path.startsWith("chests/ancient_city") && !path.startsWith("chests/stronghold_") ? VANILLA : DEEP;
            }
         } else {
            if (namespace.equals("unrp")) {
               if (path.equals("chests/nxgen_vault")) {
                  return VAULT;
               }

               if (path.startsWith("chests/bandit_") || path.startsWith("chests/ember_")) {
                  return CAMP;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
      ResourceLocation table = ctx.getQueriedLootTableId();
      float[] o = table == null ? null : odds(table.getNamespace(), table.getPath());
      if (o == null) {
         return loot;
      } else {
         RandomSource r = ctx.getRandom();
         if (r.nextFloat() < o[0]) {
            loot.add(new ItemStack(com.myriam.unrp.item.ModItems.CARVED_RUNE.get(), r.nextFloat() < 0.25F ? 2 : 1));
         }

         if (r.nextFloat() < o[1]) {
            loot.add(BoneCharmItem.of((Item)com.myriam.unrp.item.ModItems.BONE_CHARM.get(), BoneCharmItem.randomKind(r)));
         }

         return loot;
      }
   }

   public Codec<? extends IGlobalLootModifier> codec() {
      return (Codec)CODEC.get();
   }
}
