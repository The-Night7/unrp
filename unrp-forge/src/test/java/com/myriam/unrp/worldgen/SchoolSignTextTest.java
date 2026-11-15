package com.myriam.unrp.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SchoolSignTextTest {
    @Test void generatedDataContainsFourJsonMessagesOnBothFaces() {
        var data = new CompoundTag();
        data.putString("unrelated", "preserved");
        SchoolSignText.populate(data, "Reception and Administration");
        for (String face : new String[]{"front_text", "back_text"}) {
            var text = data.getCompound(face).getList("messages", Tag.TAG_STRING);
            assertEquals(4, text.size());
            assertEquals("Reception and", Component.Serializer.fromJson(text.getString(0)).getString());
            assertEquals("Administration", Component.Serializer.fromJson(text.getString(1)).getString());
            assertEquals("", Component.Serializer.fromJson(text.getString(2)).getString());
        }
        assertTrue(data.getBoolean("is_waxed"));
        assertEquals("preserved", data.getString("unrelated"));
    }
}
