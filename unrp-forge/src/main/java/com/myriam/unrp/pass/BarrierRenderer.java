package com.myriam.unrp.pass;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** BarrierRenderer du modèle : rien de plus que le rendu de base, la sphère est dessinée par ClientPassBr. */
public class BarrierRenderer extends EntityRenderer<BarrierEntity> {
    private static final ResourceLocation[] STAGES = {
            new ResourceLocation("unrp", "textures/entity/barrier_0.png"), new ResourceLocation("unrp", "textures/entity/barrier_1.png"),
            new ResourceLocation("unrp", "textures/entity/barrier_2.png"), new ResourceLocation("unrp", "textures/entity/barrier_3.png")};

    public BarrierRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(BarrierEntity e) {
        float f = e.hpFraction();
        return STAGES[f > 0.75F ? 0 : f > 0.5F ? 1 : f > 0.25F ? 2 : 3];
    }
}
