package com.myriam.unrp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.myriam.unrp.pass.ClientPassTm;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** LivingHeldMixin du modèle : les créatures figées par Time Manipulation passent en gris par-dessus leur rendu. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingHeldMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", shift = At.Shift.AFTER),
            require = 0, expect = 0)
    private void unrp$held(LivingEntity entity, float yaw, float pt, PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        ClientPassTm.overlay((LivingEntityRenderer<?, ?>) (Object) this, entity, pt, pose, buffers, light);
    }
}
