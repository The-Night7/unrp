package com.myriam.unrp.mixin;

import com.myriam.unrp.item.RecoveryBedBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Medical rest must not vote to skip the night. Ordinary beds keep vanilla behaviour. */
@Mixin(SleepStatus.class)
public abstract class RecoverySleepStatusMixin {
    @Redirect(method="update",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;isSleeping()Z"))
    private boolean unrp$medicalRestIsNotNightSleep(ServerPlayer player) {
        return player.isSleeping()&&!RecoveryBedBlock.resting(player);
    }
}
