package com.myriam.unrp.deal;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class MoteParticle extends TextureSheetParticle {
   public MoteParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, double xd, double yd, double zd) {
      super(level, x, y, z, 0.0D, 0.0D, 0.0D);
      this.xd = xd * 0.2D;
      this.yd = yd * 0.2D;
      this.zd = zd * 0.2D;
      this.gravity = 0.0F;
      this.hasPhysics = false;
      this.friction = 0.82F;
      this.lifetime = 16 + this.random.nextInt(14);
      this.quadSize = 0.05F + this.random.nextFloat() * 0.05F;
      this.pickSprite(sprites);
      int color = DealBridge.colorForViewer(15160448);
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      if (this.random.nextInt(3) == 0) {
         this.rCol = r + (1.0F - r) * 0.7F;
         this.gCol = g + (1.0F - g) * 0.7F;
         this.bCol = b + (1.0F - b) * 0.7F;
      } else {
         float jitter = 0.88F + this.random.nextFloat() * 0.24F;
         this.rCol = Mth.clamp(r * jitter, 0.0F, 1.0F);
         this.gCol = Mth.clamp(g * jitter, 0.0F, 1.0F);
         this.bCol = Mth.clamp(b * jitter, 0.0F, 1.0F);
      }

   }

   public void tick() {
      super.tick();
      float phase = ((float)this.age + this.random.nextFloat()) * 0.55F;
      this.alpha = 0.55F + 0.45F * Mth.abs(Mth.sin(phase));
      if (this.age > this.lifetime - 6) {
         this.quadSize *= 0.86F;
      }

   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public int getLightColor(float partialTick) {
      return 15728880;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
         return new MoteParticle(level, x, y, z, this.sprites, xd, yd, zd);
      }
   }
}
