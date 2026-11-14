package com.myriam.unrp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Poses tenues du modèle à la première personne (power_animations.js : bras seulement, X x 0,35, Z x 0,3). */
@Mixin(PlayerRenderer.class)
public abstract class PlayerHandPoseMixin {
    @Inject(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V", ordinal = 0), require = 0, expect = 0)
    private void unrp$rosterHand(PoseStack ps, MultiBufferSource buf, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        var model = ((PlayerRenderer) (Object) this).getModel();
        com.myriam.unrp.anim.RosterPoses.firstPerson(player, arm, arm == model.rightArm);
        sleeve.copyFrom(arm);
    }
}
