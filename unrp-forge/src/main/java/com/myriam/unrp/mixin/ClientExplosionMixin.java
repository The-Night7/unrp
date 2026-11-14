package com.myriam.unrp.mixin;

import com.myriam.unrp.pass.ClientPassEd;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** ClientExplosionMixin du modèle : l'explosion d'une sphère d'Energy Discharge n'affiche pas l'explosion vanilla (le moteur Pass la dessine). */
@Mixin(ClientPacketListener.class)
public abstract class ClientExplosionMixin {
    @Redirect(method = "handleExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Explosion;finalizeExplosion(Z)V"), require = 0, expect = 0)
    private void unrp$quietSphere(Explosion explosion, boolean particles, ClientboundExplodePacket packet) {
        if (!packet.getToBlow().isEmpty() || !ClientPassEd.quiet(packet.getX(), packet.getY(), packet.getZ())) explosion.finalizeExplosion(particles);
    }
}
