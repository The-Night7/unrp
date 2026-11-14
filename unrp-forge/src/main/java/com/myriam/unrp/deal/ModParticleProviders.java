package com.myriam.unrp.deal;

import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class ModParticleProviders {
   @SubscribeEvent
   public static void registerProviders(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet((ParticleType)PowerParticles.PURPLE_FLAME.get(), AuraFlameParticle.Provider::new);
      event.registerSpriteSet((ParticleType)PowerParticles.MOTE.get(), MoteParticle.Provider::new);
      event.registerSpriteSet((ParticleType)PowerParticles.WATER_DROP.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.DROP));
      event.registerSpriteSet((ParticleType)PowerParticles.FROST_SHARD.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.SHARD));
      event.registerSpriteSet((ParticleType)PowerParticles.SHADOW_EMBER.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.EMBER));
      event.registerSpriteSet((ParticleType)PowerParticles.STORM_ARC.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.ARC));
      event.registerSpriteSet((ParticleType)PowerParticles.DREAD_WISP.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.WISP));
      event.registerSpriteSet((ParticleType)PowerParticles.SPARK_RING.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.RING));
      event.registerSpriteSet((ParticleType)PowerParticles.ENERGY_FLARE.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.FLARE));
      event.registerSpriteSet((ParticleType)PowerParticles.GALE_STREAK.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.GALE));
      event.registerSpriteSet((ParticleType)PowerParticles.STEEL_GLINT.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.GLINT));
      event.registerSpriteSet((ParticleType)PowerParticles.PUNCH_WAVE.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.WAVE));
      event.registerSpriteSet((ParticleType)PowerParticles.VOID_FILAMENT.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.FILAMENT));
      event.registerSpriteSet((ParticleType)PowerParticles.SCRIPT_GLYPH.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.SCRIPT));
      event.registerSpriteSet((ParticleType)PowerParticles.AMBER_VEIN.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.VEIN));
      event.registerSpriteSet((ParticleType)PowerParticles.REWIND_DASH.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.REWIND));
      event.registerSpriteSet((ParticleType)PowerParticles.EDGE_FLAKE.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.FLAKE));
      event.registerSpriteSet((ParticleType)PowerParticles.PRESSURE_STREAK.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.PRESS));
      event.registerSpriteSet((ParticleType)PowerParticles.PACT_INK.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.INK));
      event.registerSpriteSet((ParticleType)PowerParticles.SOUL_FLAME.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.SOULFLAME));
      event.registerSpriteSet((ParticleType)PowerParticles.PACT_HEART.get(), (sprites) -> new FxParticle.Provider(sprites, FxParticle.Style.HEART));
   }
}
