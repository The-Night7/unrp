package com.myriam.unrp.uo;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * Capacités « palladium:gui_overlay » stance_hud_* de particles.json : l'icône de la posture (16 x 16, x1,5),
 * en haut à gauche, décalée de (10, 30), tant que le pouvoir est actif.
 */
public final class StanceIconOverlay implements IGuiOverlay {
    public static final StanceIconOverlay INSTANCE = new StanceIconOverlay();
    private static final ResourceLocation[] ICONS = {
            new ResourceLocation("unrp", "textures/icons/particles_guardian_icon.png"),
            new ResourceLocation("unrp", "textures/icons/particles_warden_icon.png"),
            new ResourceLocation("unrp", "textures/icons/particles_vanguard_icon.png"),
            new ResourceLocation("unrp", "textures/icons/particles_architect_icon.png")};

    private StanceIconOverlay() {}

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float pt, int w, int h) {
        int f = com.myriam.unrp.look.StatClientCache.weaveFlags;
        if ((f & 1) == 0 || (f >> 4) < 4) return;
        int stance = (f >> 4) - 4;
        if (stance < 0 || stance > 3) return;
        RenderSystem.enableBlend();
        g.pose().pushPose();
        g.pose().translate(10, 30, 0);
        g.pose().scale(1.5f, 1.5f, 1f);
        g.blit(ICONS[stance], 0, 0, 0, 0, 16, 16, 16, 16);
        g.pose().popPose();
    }
}
