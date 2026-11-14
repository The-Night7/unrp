package com.myriam.unrp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.myriam.unrp.uo.LashLean;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** PlayerLeanMixin du modèle : la penchée de Black Lash. */
@Mixin(PlayerRenderer.class)
public abstract class PlayerLeanMixin {
    @Inject(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V", at = @At("TAIL"), require = 0, expect = 0)
    private void unrp$lean(AbstractClientPlayer player, PoseStack pose, float bob, float yaw, float pt, CallbackInfo ci) {
        LashLean.turn(player, pose, yaw, pt);
    }
}
