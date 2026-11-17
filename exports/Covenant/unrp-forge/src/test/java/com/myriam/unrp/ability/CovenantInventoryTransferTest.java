package com.myriam.unrp.ability;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Exercises the production transaction algorithm without booting Forge's transformed event bus in JUnit. */
class CovenantInventoryTransferTest {
    private static class Stack {
        int count;
        final String signature;
        Stack(int count, String signature) {
            this.count = count;
            this.signature = signature;
        }
    }
    private static final CovenantInventoryTransfer.StackAccess<Stack> STACKS =
        new CovenantInventoryTransfer.StackAccess<>() {
            public int count(Stack stack) {
                return stack.count;
            }
            public int limit(Stack stack) {
                return 64;
            }
            public boolean canMerge(Stack first, Stack second) {
                return first.signature.equals(second.signature);
            }
            public Stack copy(Stack stack) {
                return new Stack(stack.count, stack.signature);
            }
            public void adjustCount(Stack stack, int count) {
                stack.count += count;
            }
        };
    @Test
    void fullInventoryDoesNotConsumeTheSourceStack() {
        var slots = new ArrayList<>(List.of(new Stack(64, "Stone")));
        var incoming = new Stack(1, "Signer");
        CovenantInventoryTransfer.insert(slots, incoming, STACKS);
        assertEquals(1, incoming.count);
        assertEquals("Signer", incoming.signature);
    }
    @Test
    void successfulInsertionPreservesSignaturesAndOnlyConsumesInsertedItems() {
        var slots = new ArrayList<>(List.of(new Stack(0, "")));
        var incoming = new Stack(1, "Signer");
        CovenantInventoryTransfer.insert(slots, incoming, STACKS);
        assertEquals(0, incoming.count);
        assertEquals("Signer", slots.get(0).signature);
        assertEquals(1, slots.get(0).count);
        assertNotSame(incoming, slots.get(0));
    }
    @Test
    void partialStackSpaceLeavesTheUndeliveredRemainder() {
        var slots = new ArrayList<>(List.of(new Stack(63, "Signer")));
        var incoming = new Stack(5, "Signer");
        CovenantInventoryTransfer.insert(slots, incoming, STACKS);
        assertEquals(64, slots.get(0).count);
        assertEquals(4, incoming.count);
    }
}
