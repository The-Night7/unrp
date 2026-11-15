package com.myriam.unrp.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;

/** Initializes signs in protochunks, where block entities need not have a Level yet. */
final class SchoolSignText {
    private SchoolSignText() {}

    static void initialize(SignBlockEntity sign, String label) {
        CompoundTag data = sign.saveWithoutMetadata();
        populate(data, label);
        // The live setters broadcast through Level and crash on unattached generation entities.
        sign.load(data);
        sign.setChanged();
    }

    static void populate(CompoundTag data, String label) {
        String[] lines = {"", "", "", ""};
        StringBuilder line = new StringBuilder();
        int row = 0;
        for (String word : label.split(" ")) {
            if (line.length() + word.length() + 1 > 16 && row < 3) {
                lines[row++] = line.toString();
                line.setLength(0);
            }
            if (!line.isEmpty()) line.append(' ');
            line.append(word);
        }
        lines[row] = line.toString();
        ListTag messages = new ListTag();
        for (String value : lines) messages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(value))));
        CompoundTag text = new CompoundTag();
        text.put("messages", messages);
        text.putString("color", "black");
        text.putBoolean("has_glowing_text", false);
        data.put("front_text", text);
        data.put("back_text", text.copy());
        data.putBoolean("is_waxed", true);
    }
}
