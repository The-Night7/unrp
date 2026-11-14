package com.myriam.unrp.mixin;

import com.myriam.unrp.client.PoseClient;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Après que Minecraft a placé les membres du joueur (marche, regard...), on applique la pose de la capacité en cours.
 * La logique est dans client/PoseClient ; les poses sont dans assets/unrp/poses.json.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void unrp$applyPose(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                float netHeadYaw, float headPitch, CallbackInfo ci) {
        PlayerModel<?> m = (PlayerModel<?>) (Object) this;
        com.myriam.unrp.anim.MoveAnimClient.restore(m); // PlayerModelStanceMixin du modèle
        PoseClient.apply(m, entity, ageInTicks);
        com.myriam.unrp.anim.RosterPoses.apply(m, entity, ageInTicks); // power_animations.js du modèle
        com.myriam.unrp.anim.MoveAnimClient.apply(m, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
