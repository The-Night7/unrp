package com.myriam.unrp.deal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class AuraFlameParticle extends TextureSheetParticle {
   private final SpriteSet sprites;

   public AuraFlameParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, double xd, double yd, double zd) {
      super(level, x, y, z, xd, yd, zd);
      this.sprites = sprites;
      this.xd = xd;
      this.yd = yd;
      this.zd = zd;
      this.scale(this.random.nextFloat() * 0.4F + 0.4F);
      this.lifetime = 6 + (int)(Math.random() * 6.0D);
      this.setSpriteFromAge(sprites);
      this.gravity = -0.015F;
      this.hasPhysics = false;
      int color = resolveColor();
      this.rCol = (float)(color >> 16 & 255) / 255.0F;
      this.gCol = (float)(color >> 8 & 255) / 255.0F;
      this.bCol = (float)(color & 255) / 255.0F;
   }

   private static int resolveColor() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 10359551;
      } else if (com.myriam.unrp.pass.PassBridge.isAM(mc.player.getUUID())) {
         return 10359551;
      } else {
         int c = com.myriam.unrp.pass.PassBridge.color(mc.player.getUUID());
         return c == -1 ? 10359551 : c;
      }
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.sprites);
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_LIT;
   }

   public int getLightColor(float partialTick) {
      return 240;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
         return new AuraFlameParticle(level, x, y, z, this.sprites, xd, yd, zd);
      }
   }
}
