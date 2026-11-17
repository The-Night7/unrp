package com.myriam.unrp.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandMoveMixin {
    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0, shift = At.Shift.AFTER), require = 0, expect = 0)
    private void unrp$moveHand(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swing, ItemStack stack, float equip,
                               PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        com.myriam.unrp.anim.MoveAnimClient.hand(player, hand, partialTick, pose);
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", shift = At.Shift.AFTER), require = 0, expect = 0)
    private void unrp$wovenItem(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swing, ItemStack stack, float equip,
                                PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        com.myriam.unrp.uo.ImbuedItemLayer.firstPerson(player, hand, stack, pose, buffers, partialTick);
    }
}
