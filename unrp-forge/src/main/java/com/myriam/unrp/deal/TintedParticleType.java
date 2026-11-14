package com.myriam.unrp.deal;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class TintedParticleType extends ParticleType<TintedParticleOptions> {
   private final Codec<TintedParticleOptions> codec = TintedParticleOptions.codec(this);

   public TintedParticleType(boolean overrideLimiter) {
      super(overrideLimiter, TintedParticleOptions.DESERIALIZER);
   }

   public Codec<TintedParticleOptions> codec() {
      return this.codec;
   }
}
