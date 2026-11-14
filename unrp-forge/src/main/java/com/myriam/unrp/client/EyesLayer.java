package com.myriam.unrp.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.myriam.unrp.UnrpMod;
import com.myriam.unrp.data.StyleData;
import com.myriam.unrp.style.StyleService;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Yeux lumineux dessinés sur le visage du joueur, d'après son style (onglet Style). Visibles par tous. */
public class EyesLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    // round / sharp / ring : les images du modèle (CustomEyesLayer) ; classic n'existe que chez nous.
    private static final ResourceLocation[] TEX = {null, tex("classic"), model("eye_round"), model("eye_sharp"), model("eye_ring")};

    public EyesLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    private static ResourceLocation model(String name) {
        return new ResourceLocation("unrp", "textures/gui/" + name + ".png");
    }

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(UnrpMod.MOD_ID, "textures/eyes/" + name + ".png");
    }

    @Override
    public void render(PoseStack ps, MultiBufferSource buffers, int light, AbstractClientPlayer pl, float limbSwing,
                       float limbSwingAmount, float partial, float age, float headYaw, float headPitch) {
        if (pl.isInvisible()) return;
        StyleData s = StyleService.CLIENT_CACHE.get(pl.getUUID());
        if (s == null || s.eyeStyle <= 0 || s.eyeStyle >= TEX.length) return;
        ps.pushPose();
        getParentModel().head.translateAndRotate(ps);
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(TEX[s.eyeStyle]));
        float r = ((s.eyeColor >> 16) & 255) / 255f, g = ((s.eyeColor >> 8) & 255) / 255f, b = (s.eyeColor & 255) / 255f;
        float cy = -8f + 2.5f + s.eyeY * 0.6f;
        float half = s.eyeGap / 2f;
        quad(vc, ps, -half - s.eyeW / 2f + 0.0f, cy - s.eyeH / 2f, s.eyeW, s.eyeH, r, g, b, false);
        quad(vc, ps, half - s.eyeW / 2f, cy - s.eyeH / 2f, s.eyeW, s.eyeH, r, g, b, true);
        ps.popPose();
    }

    /** Un carré sur la face avant de la tête (z = -4), dessiné dans les deux sens pour ne pas dépendre du sens des faces. */
    private static void quad(VertexConsumer vc, PoseStack ps, float x, float y, float w, float h, float r, float g, float b, boolean mirror) {
        Matrix4f m = ps.last().pose();
        Matrix3f n = ps.last().normal();
        float z = -4.04f;
        float u0 = mirror ? 1f : 0f, u1 = mirror ? 0f : 1f;
        float s = 1f / 16f;
        float x0 = x * s, x1 = (x + w) * s, y0 = y * s, y1 = (y + h) * s;
        v(vc, m, n, x0, y1, z * s, u0, 1, r, g, b);
        v(vc, m, n, x1, y1, z * s, u1, 1, r, g, b);
        v(vc, m, n, x1, y0, z * s, u1, 0, r, g, b);
        v(vc, m, n, x0, y0, z * s, u0, 0, r, g, b);
        v(vc, m, n, x0, y0, z * s, u0, 0, r, g, b);
        v(vc, m, n, x1, y0, z * s, u1, 0, r, g, b);
        v(vc, m, n, x1, y1, z * s, u1, 1, r, g, b);
        v(vc, m, n, x0, y1, z * s, u0, 1, r, g, b);
    }

    private static void v(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float z, float u, float v, float r, float g, float b) {
        vc.vertex(m, x, y, z).color(r, g, b, 1f).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(n, 0f, 0f, -1f).endVertex();
    }
}
