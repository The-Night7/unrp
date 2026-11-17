package com.myriam.unrp.ability;

import java.util.List;

final class CovenantInventoryTransfer {
    // Keeps the insertion logic testable without starting Forge.
    interface StackAccess<T> {
        int count(T stack);
        int limit(T stack);
        boolean canMerge(T first, T second);
        T copy(T stack);
        void adjustCount(T stack, int amount);
    }

    private CovenantInventoryTransfer() {}

    static <T> void insert(List<T> slots, T incoming, StackAccess<T> access) {
        for (T slot : slots) {
            if (access.count(incoming) == 0) {
                return;
            }
            if (access.count(slot) == 0 || !access.canMerge(slot, incoming)) {
                continue;
            }

            int space = Math.max(0, Math.min(64, access.limit(slot)) - access.count(slot));
            int transferred = Math.min(space, access.count(incoming));
            access.adjustCount(slot, transferred);
            access.adjustCount(incoming, -transferred);
        }

        for (int slot = 0; slot < slots.size() && access.count(incoming) > 0; slot++) {
            if (access.count(slots.get(slot)) > 0) {
                continue;
            }

            int transferred = Math.min(access.count(incoming), Math.min(64, access.limit(incoming)));
            T placed = access.copy(incoming);
            access.adjustCount(placed, transferred - access.count(placed));
            slots.set(slot, placed);
            access.adjustCount(incoming, -transferred);
        }
    }
}
