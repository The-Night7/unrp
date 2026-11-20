package com.myriam.unrp.ability;

import com.myriam.unrp.deal.ContractItem;
import com.myriam.unrp.deal.PactLedger;
import com.myriam.unrp.home.HomeCommands;
import java.util.Locale;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CovenantSpecificEffects {
    public record HomeReport(String player, String dimension, double x, double y, double z, boolean hasHome) {
        String pageText() {
            String heading = "COVENANT — HOME REPORT\n\nPlayer: " + player + "\n\n";
            if (!hasHome) {
                return heading + "No /sethome has been recorded.";
            }
            return heading
                + String.format(Locale.ROOT, "Dimension: %s\nX: %.2f\nY: %.2f\nZ: %.2f", dimension, x, y, z);
        }
    }

    private static final CovenantInventoryTransfer.StackAccess<ItemStack> STACKS =
        new CovenantInventoryTransfer.StackAccess<>() {
            public int count(ItemStack stack) {
                return stack.getCount();
            }
            public int limit(ItemStack stack) {
                return stack.getMaxStackSize();
            }
            public boolean canMerge(ItemStack first, ItemStack second) {
                return ItemStack.isSameItemSameTags(first, second);
            }
            public ItemStack copy(ItemStack stack) {
                return stack.copy();
            }
            public void adjustCount(ItemStack stack, int amount) {
                stack.grow(amount);
            }
        };

    private CovenantSpecificEffects() {}

    public static HomeReport captureHome(ServerPlayer visitor) {
        String name = visitor.getGameProfile().getName();
        var home = HomeCommands.home(visitor);
        if (home == null) {
            return new HomeReport(name, "", 0, 0, 0, false);
        }
        return new HomeReport(name, home.dimension(), home.x(), home.y(), home.z(), true);
    }

    public static int transferContracts(ServerPlayer visitor, ServerPlayer sovereign) {
        var ledger = PactLedger.get(visitor.server);
        int transferred = 0;

        for (int slot = 0; slot < visitor.getInventory().getContainerSize(); slot++) {
            ItemStack contract = visitor.getInventory().getItem(slot);
            if (!(contract.getItem() instanceof ContractItem) || ContractItem.isCopy(contract)) {
                continue;
            }

            var pactId = ContractItem.pactOf(contract);
            var pact = pactId == null ? null : ledger.pact(pactId);
            if (pact == null || !pact.signed) {
                continue;
            }

            ItemStack remainder = contract.copy();
            ContractItem.syncFromLedger(remainder, pact);
            int originalCount = remainder.getCount();
            insert(sovereign, remainder);

            int accepted = originalCount - remainder.getCount();
            contract.shrink(accepted);
            transferred += accepted;
        }

        if (transferred > 0) {
            syncInventory(visitor);
            syncInventory(sovereign);
        }
        return transferred;
    }

    public static ItemStack createHomeBook(HomeReport report, String author) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        var tag = book.getOrCreateTag();
        tag.putString("title", "Home: " + report.player());
        tag.putString("author", author);
        tag.putBoolean("resolved", true);

        var pages = new ListTag();
        pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(report.pageText()))));
        tag.put("pages", pages);
        return book;
    }

    public static boolean deliverHomeReport(HomeReport report, ServerPlayer sovereign) {
        ItemStack remainder = createHomeBook(report, sovereign.getGameProfile().getName());
        insert(sovereign, remainder);
        if (!remainder.isEmpty()) {
            return false;
        }

        syncInventory(sovereign);
        return true;
    }

    private static void insert(ServerPlayer player, ItemStack stack) {
        // Inventory.add can discard leftovers when the recipient is in creative mode.
        CovenantInventoryTransfer.insert(player.getInventory().items, stack, STACKS);
    }

    private static void syncInventory(ServerPlayer player) {
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }
}
