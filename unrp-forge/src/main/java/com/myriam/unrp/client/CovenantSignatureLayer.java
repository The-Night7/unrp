package com.myriam.unrp.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Pixel geometry on the hand and gold Constitution strokes on the body, with fullbright ink. */
public final class CovenantSignatureLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation WHITE = new ResourceLocation("unrp", "textures/gui/ability_bars/white.png");
    public CovenantSignatureLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) { super(parent); }
    @Override public void render(PoseStack pose, MultiBufferSource buffer, int light, AbstractClientPlayer player,
                                 float limb, float amount, float partial, float age, float yaw, float pitch) {
        var hand = CovenantClient.hand(player.getId());
        if (hand == null || player.isInvisible()) return;
        if (hand.signed()) {
            pose.pushPose(); getParentModel().rightArm.translateAndRotate(pose);
            signature(pose, buffer, hand.souls()); pose.popPose();
        }
        if (!hand.constitutions().isEmpty()) {
            pose.pushPose(); getParentModel().body.translateAndRotate(pose);
            var consumer = buffer.getBuffer(RenderType.entityTranslucent(WHITE));
            for (int row = 0; row < hand.constitutions().size(); row++) {
                float y = 0.13f + row * 0.07f;
                stroke(pose, consumer, -0.19f, y, 0.19f, y, -0.131f, 0.006f);
                for (int rune = 0; rune < 6; rune++) stroke(pose, consumer, -0.17f + rune * 0.06f, y - 0.016f, -0.15f + rune * 0.06f, y + 0.016f, -0.132f, 0.005f);
            }
            pose.popPose();
        }
    }
    public static void signature(PoseStack pose, MultiBufferSource buffer, int souls) {
        var consumer = buffer.getBuffer(RenderType.entityTranslucent(WHITE));
        float size = Math.min(1.3f, 1 + souls * 0.02f);
        pose.translate(-0.0625, 0.61, -0.128); pose.scale(size, size, size);
        stroke(pose, consumer, -0.055f, 0, 0.055f, 0, 0, 0.009f);
        stroke(pose, consumer, -0.055f, 0, -0.055f, -0.065f, 0, 0.009f);
        stroke(pose, consumer, -0.055f, -0.065f, 0, -0.025f, 0, 0.009f);
        stroke(pose, consumer, 0, -0.025f, 0.055f, -0.065f, 0, 0.009f);
        stroke(pose, consumer, 0.055f, -0.065f, 0.055f, 0, 0, 0.009f);
        stroke(pose, consumer, 0, 0.015f, 0, 0.09f, 0, 0.009f);
        stroke(pose, consumer, -0.035f, 0.06f, 0.035f, 0.06f, 0, 0.009f);
    }
    private static void stroke(PoseStack pose, VertexConsumer consumer, float x0, float y0, float x1, float y1, float z, float thickness) {
        float length = (float) Math.hypot(x1 - x0, y1 - y0);
        if (length == 0) return;
        float nx = -(y1 - y0) / length * thickness / 2, ny = (x1 - x0) / length * thickness / 2;
        vertex(pose, consumer, x0 + nx, y0 + ny, z, 0, 0);
        vertex(pose, consumer, x1 + nx, y1 + ny, z, 0, 1);
        vertex(pose, consumer, x1 - nx, y1 - ny, z, 1, 1);
        vertex(pose, consumer, x0 - nx, y0 - ny, z, 1, 0);
    }
    private static void vertex(PoseStack pose, VertexConsumer consumer, float x, float y, float z, float u, float v) {
        consumer.vertex(pose.last().pose(), x, y, z).color(255, 209, 88, 235).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(pose.last().normal(), 0, 0, -1).endVertex();
    }
}
