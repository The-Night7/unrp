package com.myriam.unrp.net;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerStatePacketTest {
    @Test void tenBorrowedPowersDoNotTruncateTheBarOrMisalignThePacket() {
        PlayerStatePacket state = new PlayerStatePacket();
        int moves = 125, powers = 11;
        state.barSlot = new int[moves]; state.barCd = new int[moves]; state.barLeft = new int[moves];
        state.barIcon = new String[moves]; state.barColor = new String[moves]; state.barName = new String[moves];
        state.barLocked = new boolean[moves]; state.barOn = new boolean[moves];
        state.barPower = new int[moves]; state.barMove = new int[moves];
        for (int i = 0; i < moves; i++) { state.barSlot[i] = i + 1; state.barMove[i] = i % 25; state.barName[i] = "Branch " + i; }
        state.powIcon = new String[powers]; state.powColor = new String[powers]; state.powName = new String[powers]; state.powOn = new boolean[powers];
        state.powName[10] = "tenth_borrowed_power"; state.barMore = true;
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            PlayerStatePacket.encode(state, buffer);
            PlayerStatePacket restored = PlayerStatePacket.decode(buffer);
            assertEquals(moves, restored.barSlot.length);
            assertEquals("Branch 124", restored.barName[124]);
            assertEquals(powers, restored.powName.length);
            assertEquals("tenth_borrowed_power", restored.powName[10]);
            assertTrue(restored.barMore);
            assertEquals(0, buffer.readableBytes());
        } finally { buffer.release(); }
    }
}
