package com.myriam.unrp.mixin;

import com.myriam.unrp.pass.ClientPassPd;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityTintMixin {
    @Inject(method = "getTeamColor()I", at = @At("RETURN"), cancellable = true, require = 0, expect = 0)
    private void unrp$prey(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        Integer was = cir.getReturnValue();
        if (self.level() != null && self.level().isClientSide() && was != null && was == 0xFFFFFF) {
            int c = ClientPassPd.tint(self);
            if (c >= 0) cir.setReturnValue(c);
        }
    }
}
