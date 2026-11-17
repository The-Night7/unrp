package com.myriam.unrp.ability;

import com.myriam.unrp.deal.ContractItem;
import com.myriam.unrp.deal.PactLedger;
import com.myriam.unrp.home.HomeCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;

/** Inventory transactions preserve the real stack/NBT and leave undelivered items at their source. */
public final class CovenantSpecificEffects {
    public record Report(String player,String dimension,double x,double y,double z,boolean hasHome) {}
    public static Report snapshot(ServerPlayer subject) {
        var home=HomeCommands.home(subject);
        return home==null?new Report(subject.getGameProfile().getName(),"",0,0,0,false)
                :new Report(subject.getGameProfile().getName(),home.dimension(),home.x(),home.y(),home.z(),true);
    }
    private static final CovenantInventoryTransfer.Stacks<ItemStack> STACKS=new CovenantInventoryTransfer.Stacks<>() {
        public int count(ItemStack stack){return stack.getCount();}
        public int limit(ItemStack stack){return stack.getMaxStackSize();}
        public boolean matches(ItemStack first,ItemStack second){return ItemStack.isSameItemSameTags(first,second);}
        public ItemStack copy(ItemStack stack){return stack.copy();}
        public void change(ItemStack stack,int count){stack.grow(count);}
    };
    private static void insert(java.util.List<ItemStack> slots,ItemStack incoming) {
        CovenantInventoryTransfer.insert(slots,incoming,STACKS);
    }
    public static int transferContracts(ServerPlayer subject,ServerPlayer sovereign) {
        var ledger=PactLedger.get(subject.server);int count=0;
        for(int slot=0;slot<subject.getInventory().getContainerSize();slot++) {
            var source=subject.getInventory().getItem(slot);
            if(!(source.getItem() instanceof ContractItem)||ContractItem.isCopy(source))continue;
            var id=ContractItem.pactOf(source);var pact=id==null?null:ledger.pact(id);
            if(pact==null||!pact.signed)continue;
            var moving=source.copy();ContractItem.syncFromLedger(moving,pact);
            int original=moving.getCount();insert(sovereign.getInventory().items,moving);
            int moved=original-moving.getCount();
            if(moved>0){source.shrink(moved);count+=moved;}
        }
        if(count>0){subject.getInventory().setChanged();sovereign.getInventory().setChanged();subject.containerMenu.broadcastChanges();sovereign.containerMenu.broadcastChanges();}
        return count;
    }
    public static ItemStack book(Report report,String sovereign) {
        var book=new ItemStack(Items.WRITTEN_BOOK);var tag=book.getOrCreateTag();
        tag.putString("title","Home: "+report.player);tag.putString("author",sovereign);tag.putBoolean("resolved",true);
        String text="COVENANT — HOME REPORT\n\nPlayer: "+report.player+"\n\n"+(report.hasHome
                ?String.format(java.util.Locale.ROOT,"Dimension: %s\nX: %.2f\nY: %.2f\nZ: %.2f",report.dimension,report.x,report.y,report.z)
                :"No /sethome has been recorded.");
        var pages=new net.minecraft.nbt.ListTag();pages.add(net.minecraft.nbt.StringTag.valueOf(Component.Serializer.toJson(Component.literal(text))));tag.put("pages",pages);
        return book;
    }
    public static boolean deliver(Report report,ServerPlayer sovereign) {
        var item=book(report,sovereign.getGameProfile().getName());insert(sovereign.getInventory().items,item);
        if(!item.isEmpty())return false;
        sovereign.getInventory().setChanged();sovereign.containerMenu.broadcastChanges();return true;
    }
}
