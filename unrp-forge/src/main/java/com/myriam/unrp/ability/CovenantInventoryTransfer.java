package com.myriam.unrp.ability;

import java.util.List;

/** Lossless insertion independent of player game mode; no creative-mode deletion of leftovers. */
final class CovenantInventoryTransfer {
    interface Stacks<T> {
        int count(T stack);
        int limit(T stack);
        boolean matches(T first,T second);
        T copy(T stack);
        void change(T stack,int amount);
    }
    static <T> void insert(List<T> slots,T incoming,Stacks<T> stacks) {
        for(var slot:slots) {
            if(stacks.count(incoming)<=0)return;
            if(stacks.count(slot)<=0||!stacks.matches(slot,incoming))continue;
            int room=Math.min(64,stacks.limit(slot))-stacks.count(slot);
            int count=Math.min(Math.max(0,room),stacks.count(incoming));
            stacks.change(slot,count);stacks.change(incoming,-count);
        }
        for(int i=0;i<slots.size()&&stacks.count(incoming)>0;i++)if(stacks.count(slots.get(i))<=0) {
            int count=Math.min(stacks.count(incoming),Math.min(64,stacks.limit(incoming)));
            T placed=stacks.copy(incoming);stacks.change(placed,count-stacks.count(placed));slots.set(i,placed);stacks.change(incoming,-count);
        }
    }
}
