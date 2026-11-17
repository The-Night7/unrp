package com.myriam.unrp.uo;

public final class AmPass {
    private AmPass() {}

    public static boolean veiled(net.minecraft.world.entity.Entity e, String s) {
        java.util.Set<String> tags = e.getTags();
        return tags.contains("unrp.copied." + s) && !tags.contains("unrp.am_shown." + s);
    }
}
