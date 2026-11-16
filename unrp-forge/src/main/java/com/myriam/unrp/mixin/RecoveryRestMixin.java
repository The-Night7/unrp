package com.myriam.unrp.mixin;

import com.myriam.unrp.item.RecoveryBedBlock;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep the actual sleeping pose without the black sleep fade or deep-sleep contribution. */
@Mixin(Player.class)
public abstract class RecoveryRestMixin {
    @Inject(method="getSleepTimer",at=@At("HEAD"),cancellable=true)
    private void unrp$clearMedicalView(CallbackInfoReturnable<Integer> ci) {
        if(RecoveryBedBlock.resting((Player)(Object)this))ci.setReturnValue(0);
    }
    @Inject(method="isSleepingLongEnough",at=@At("HEAD"),cancellable=true)
    private void unrp$notDeepSleep(CallbackInfoReturnable<Boolean> ci) {
        if(RecoveryBedBlock.resting((Player)(Object)this))ci.setReturnValue(false);
    }
}
