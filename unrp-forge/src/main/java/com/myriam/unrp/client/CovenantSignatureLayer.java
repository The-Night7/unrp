package com.myriam.unrp.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/** Pixel geometry on the hand and gold Constitution strokes on the body, with fullbright ink. */
public final class CovenantSignatureLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation SIGNATURE = new ResourceLocation("unrp", "textures/covenant/signature.png");
    private static final ResourceLocation INK = new ResourceLocation("unrp", "textures/covenant/constitution_ink.png");
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
            var consumer = buffer.getBuffer(RenderType.entityTranslucent(INK));
            for (int row = 0; row < hand.constitutions().size(); row++) {
                pose.pushPose();pose.translate(0,0.13f+row*0.07f,-0.195f);
                CovenantEffects.quad(pose,consumer,-.19f,-.025f,.19f,.025f,0xFFFFFF,1);
                pose.popPose();
            }
            pose.popPose();
        }
    }
    public static void signature(PoseStack pose, MultiBufferSource buffer, int souls) {
        float size = Math.min(1.3f, 1 + souls * 0.02f);
        pose.translate(-0.0625, 0.68, -0.195);pose.scale(size,size,size);
        CovenantEffects.quad(pose,buffer.getBuffer(RenderType.entityTranslucent(SIGNATURE)),-.055f,-.055f,.055f,.055f,0xFFFFFF,.94f);
    }
}
