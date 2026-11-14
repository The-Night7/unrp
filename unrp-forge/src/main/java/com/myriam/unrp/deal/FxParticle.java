package com.myriam.unrp.deal;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FxParticle extends TextureSheetParticle {
   private final FxParticle.Style style;
   private final SpriteSet sprites;
   private final float baseSize;
   private final float spin;
   private final float maxRadius;
   private final float swirl;
   private final double homeX;
   private final double homeY;
   private final double homeZ;
   private int mode;
   private double orbit;
   private double rate;
   private double stillX;
   private double stillY;
   private double stillZ;

   public FxParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, FxParticle.Style style, double xd, double yd, double zd, int rgb) {
      super(level, x, y, z, 0.0D, 0.0D, 0.0D);
      this.style = style;
      this.sprites = sprites;
      this.homeX = x;
      this.homeY = y;
      this.homeZ = z;
      this.xd = xd;
      this.yd = yd;
      this.zd = zd;
      float r = (float)(rgb >> 16 & 255) / 255.0F;
      float g = (float)(rgb >> 8 & 255) / 255.0F;
      float b = (float)(rgb & 255) / 255.0F;
      float jitter = 0.9F + this.random.nextFloat() * 0.2F;
      this.rCol = Mth.clamp(r * jitter, 0.0F, 1.0F);
      this.gCol = Mth.clamp(g * jitter, 0.0F, 1.0F);
      this.bCol = Mth.clamp(b * jitter, 0.0F, 1.0F);
      float radius = 1.0F;
      float turn = 0.0F;
      float size;
      switch (style) {
         case DROP:
            this.gravity = 0.9F;
            this.hasPhysics = true;
            this.friction = 0.96F;
            this.lifetime = 12 + this.random.nextInt(10);
            size = 0.07F + this.random.nextFloat() * 0.07F;
            this.rCol = Mth.clamp(this.rCol + (1.0F - this.rCol) * 0.25F, 0.0F, 1.0F);
            this.gCol = Mth.clamp(this.gCol + (1.0F - this.gCol) * 0.25F, 0.0F, 1.0F);
            this.bCol = Mth.clamp(this.bCol + (1.0F - this.bCol) * 0.25F, 0.0F, 1.0F);
            break;
         case SHARD:
            this.gravity = 0.25F;
            this.hasPhysics = false;
            this.friction = 0.95F;
            this.lifetime = 14 + this.random.nextInt(12);
            size = 0.06F + this.random.nextFloat() * 0.07F;
            turn = (this.random.nextFloat() - 0.5F) * 0.6F;
            break;
         case EMBER:
            this.gravity = -0.12F;
            this.hasPhysics = false;
            this.friction = 0.91F;
            this.lifetime = 12 + this.random.nextInt(12);
            size = 0.1F + this.random.nextFloat() * 0.09F;
            break;
         case ARC:
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.friction = 0.98F;
            this.lifetime = 3 + this.random.nextInt(5);
            size = 0.22F + this.random.nextFloat() * 0.22F;
            turn = (this.random.nextFloat() - 0.5F) * 1.2F;
            this.roll = this.random.nextFloat() * 6.2832F;
            this.oRoll = this.roll;
            break;
         case WISP:
            this.gravity = -0.03F;
            this.hasPhysics = false;
            this.friction = 0.93F;
            this.lifetime = 24 + this.random.nextInt(18);
            size = 0.16F + this.random.nextFloat() * 0.12F;
            this.alpha = 0.0F;
            radius = 0.35F + this.random.nextFloat() * 0.3F;
            break;
         case FLARE:
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.friction = 0.9F;
            this.lifetime = 5 + this.random.nextInt(4);
            size = 0.24F + this.random.nextFloat() * 0.16F;
            this.roll = this.random.nextFloat() * 6.2832F;
            this.oRoll = this.roll;
            turn = (this.random.nextFloat() - 0.5F) * 0.25F;
            break;
         case GALE:
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.friction = 0.94F;
            this.lifetime = 10 + this.random.nextInt(8);
            size = 0.15F + this.random.nextFloat() * 0.11F;
            this.roll = this.random.nextFloat() * 6.2832F;
            this.oRoll = this.roll;
            turn = (this.random.nextFloat() - 0.5F) * 0.8F;
            this.alpha = 0.0F;
            break;
         case GLINT:
            this.gravity = 0.04F;
            this.hasPhysics = false;
            this.friction = 0.9F;
            this.lifetime = 8 + this.random.nextInt(8);
            size = 0.05F + this.random.nextFloat() * 0.05F;
            break;
         case WAVE:
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.friction = 0.86F;
            this.lifetime = 5 + this.random.nextInt(3);
            size = 0.26F + this.random.nextFloat() * 0.14F;
            this.roll = this.random.nextFloat() * 6.2832F;
            this.oRoll = this.roll;
            break;
         case FILAMENT:
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.friction = 0.94F;
            this.lifetime = 12 + this.random.nextInt(5);
            size = 0.045F + this.random.nextFloat() * 0.04F;
            this.alpha = 0.0F;
            this.setSize(2.4F, 2.4F);
            break;
         case SCRIPT:
            this.gravity = -0.015F;
            this.hasPhysics = false;
            this.friction = 0.92F;
            this.lifetime = 18 + this.random.nextInt(11);
            size = 0.1F + this.random.nextFloat() * 0.06F;
            this.alpha = 0.0F;
            this.roll = (this.random.nextFloat() - 0.5F) * 0.5F;
            this.oRoll = this.roll;
            break;
         case VEIN:
            this.gravity = 0.35F;
            this.hasPhysics = false;
            this.friction = 0.86F;
            this.lifetime = 6 + this.random.nextInt(5);
            size = 0.05F + this.random.nextFloat() * 0.03F;
            this.roll = this.random.nextFloat() * 6.2832F;
            this.oRoll = this.roll;
            break;
         case REWIND:
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.friction = 0.98F;
            this.lifetime = 10 + this.random.nextInt(5);
            size = 0.04F + this.random.nextFloat() * 0.02F;
            this.alpha = 0.0F;
            this.setSize(2.4F, 2.4F);
            break;
         case FLAKE:
            this.gravity = 0.012F;
            this.hasPhysics = false;
            this.friction = 0.93F;
            this.lifetime = 26 + this.random.nextInt(15);
            size = 0.05F + this.random.nextFloat() * 0.04F;
            this.roll = this.random.nextFloat() * 6.2832F;
            this.oRoll = this.roll;
            turn = (this.random.nextFloat() - 0.5F) * 0.12F;
            break;
         case PRESS:
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.friction = 0.88F;
            this.lifetime = 8 + this.random.nextInt(5);
            size = 0.05F + this.random.nextFloat() * 0.025F;
            this.alpha = 0.0F;
            this.setSize(2.4F, 2.4F);
            break;
         case INK:
            this.gravity = 0.55F;
            this.hasPhysics = true;
            this.friction = 0.96F;
            this.lifetime = 16 + this.random.nextInt(9);
            size = 0.035F + this.random.nextFloat() * 0.035F;
            break;
         case SOULFLAME:
            this.gravity = -0.015F;
            this.hasPhysics = false;
            this.friction = 0.9F;
            this.lifetime = 14 + this.random.nextInt(9);
            size = 0.09F + this.random.nextFloat() * 0.07F;
            this.alpha = 0.0F;
            break;
         case HEART:
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.friction = 1.0F;
            radius = (float)Mth.clamp(xd, 0.3D, 3.0D);
            this.orbit = yd;
            this.rate = Mth.clamp(zd, -0.4D, 0.4D);
            this.xd = 0.0D;
            this.yd = 0.0D;
            this.zd = 0.0D;
            this.lifetime = 36 + this.random.nextInt(8);
            size = 0.11F + this.random.nextFloat() * 0.04F;
            this.alpha = 0.0F;
            break;
         default:
            this.gravity = 0.0F;
            this.hasPhysics = false;
            this.friction = 1.0F;
            this.xd = 0.0D;
            this.yd = 0.0D;
            this.zd = 0.0D;
            radius = xd > 0.05D ? (float)xd : 1.6F;
            this.lifetime = yd > 0.5D ? (int)yd : 11;
            size = radius;
            this.alpha = 0.85F;
      }

      this.baseSize = size;
      this.quadSize = size;
      this.spin = turn;
      this.maxRadius = radius;
      this.swirl = (this.random.nextFloat() - 0.5F) * 0.25F;
      this.pickSprite(sprites);
   }

   public void tick() {
      int held = 0; // ClientSgHold (Singularity) pas porté
      int was = this.mode;
      this.mode = held;
      if (held == 1) {
         if (was != 1) {
            this.stillX = this.x - this.xo;
            this.stillY = this.y - this.yo;
            this.stillZ = this.z - this.zo;
         }

         this.xo = this.x;
         this.yo = this.y;
         this.zo = this.z;
         this.oRoll = this.roll;
      } else if (held == 2) {
         this.back();
      } else {
         super.tick();
         float life = (float)this.age / (float)this.lifetime;
         switch (this.style) {
            case DROP:
               if (this.onGround) {
                  this.quadSize *= 0.7F;
                  this.alpha *= 0.75F;
               }

               if (life > 0.75F) {
                  this.alpha = Math.max(0.0F, 1.0F - (life - 0.75F) * 4.0F);
               }
               break;
            case SHARD:
               this.oRoll = this.roll;
               this.roll += this.spin;
               float twinkle_0 = 0.65F + 0.35F * Mth.abs(Mth.sin((float)this.age * 0.9F + this.spin * 10.0F));
               this.alpha = life > 0.7F ? twinkle_0 * (1.0F - (life - 0.7F) / 0.3F) : twinkle_0;
               break;
            case EMBER:
               this.setSpriteFromAge(this.sprites);
               this.quadSize = this.baseSize * (1.0F - life * 0.6F);
               this.alpha = 0.75F + 0.25F * Mth.sin((float)this.age * 1.7F);
               if (life > 0.7F) {
                  this.alpha *= 1.0F - (life - 0.7F) / 0.3F;
               }

               this.xd += (double)(this.random.nextFloat() - 0.5F) * 0.01D;
               this.zd += (double)(this.random.nextFloat() - 0.5F) * 0.01D;
               break;
            case ARC:
               if (this.age % 2 == 0) {
                  this.pickSprite(this.sprites);
               }

               this.oRoll = this.roll;
               this.roll += this.spin;
               this.alpha = life > 0.5F ? 1.0F - (life - 0.5F) * 2.0F : 1.0F;
               break;
            case WISP:
               this.setSpriteFromAge(this.sprites);
               this.quadSize = this.baseSize * (1.0F + life * 1.2F);
               float fade_0 = life < 0.25F ? life / 0.25F : (life > 0.6F ? (1.0F - life) / 0.4F : 1.0F);
               this.alpha = 0.7F * fade_0;
               double vx_0 = this.xd;
               double vz_0 = this.zd;
               double cs_0 = Math.cos((double)this.swirl);
               double sn_0 = Math.sin((double)this.swirl);
               this.xd = vx_0 * cs_0 - vz_0 * sn_0;
               this.zd = vx_0 * sn_0 + vz_0 * cs_0;
               break;
            case FLARE:
               if (this.age % 2 == 0) {
                  this.pickSprite(this.sprites);
               }

               this.oRoll = this.roll;
               this.roll += this.spin;
               this.quadSize = this.baseSize * (1.0F + life * 0.7F);
               this.alpha = life > 0.4F ? 1.0F - (life - 0.4F) / 0.6F : 1.0F;
               break;
            case GALE:
               this.setSpriteFromAge(this.sprites);
               this.oRoll = this.roll;
               this.roll += this.spin;
               float fade1 = life < 0.15F ? life / 0.15F : (life > 0.55F ? (1.0F - life) / 0.45F : 1.0F);
               this.alpha = 0.85F * fade1;
               this.quadSize = this.baseSize * (1.0F + life * 0.5F);
               double vx = this.xd;
               double vz = this.zd;
               double cs = Math.cos((double)this.swirl * 0.6D);
               double sn = Math.sin((double)this.swirl * 0.6D);
               this.xd = vx * cs - vz * sn;
               this.zd = vx * sn + vz * cs;
               break;
            case GLINT:
               this.setSpriteFromAge(this.sprites);
               float twinkle = 0.6F + 0.4F * Mth.abs(Mth.sin((float)this.age * 1.1F + this.swirl * 20.0F));
               this.alpha = life > 0.65F ? twinkle * (1.0F - (life - 0.65F) / 0.35F) : twinkle;
               break;
            case WAVE:
               this.setSpriteFromAge(this.sprites);
               this.oRoll = this.roll;
               this.quadSize = this.baseSize * (1.0F + life * 1.6F);
               this.alpha = 1.0F - life * life;
               break;
            case FILAMENT:
               float fade2 = life < 0.2F ? life / 0.2F : (life > 0.7F ? (1.0F - life) / 0.3F : 1.0F);
               this.alpha = 0.95F * fade2;
               this.quadSize = this.baseSize * (1.0F - life * 0.5F);
               break;
            case SCRIPT:
               float fade3 = life < 0.15F ? life / 0.15F : (life > 0.6F ? (1.0F - life) / 0.4F : 1.0F);
               this.alpha = 0.95F * fade3;
               break;
            case VEIN:
               float flicker = 0.6F + 0.4F * Mth.abs(Mth.sin((float)this.age * 1.3F + this.swirl * 20.0F));
               this.alpha = life > 0.6F ? flicker * (1.0F - (life - 0.6F) / 0.4F) : flicker;
               this.quadSize = this.baseSize * (1.0F - life * 0.4F);
               break;
            case REWIND:
               float fade4 = life < 0.15F ? life / 0.15F : (life > 0.55F ? (1.0F - life) / 0.45F : 1.0F);
               this.alpha = 0.9F * fade4;
               break;
            case FLAKE:
               this.oRoll = this.roll;
               this.roll += this.spin;
               this.xd += Math.sin((double)this.age * 0.25D + (double)this.swirl * 30.0D) * 0.002D;
               this.zd += Math.cos((double)this.age * 0.21D + (double)this.swirl * 30.0D) * 0.002D;
               float shimmer = 0.75F + 0.25F * Mth.abs(Mth.sin((float)this.age * 0.5F + this.swirl * 20.0F));
               this.alpha = life > 0.6F ? shimmer * (1.0F - (life - 0.6F) / 0.4F) : shimmer;
               break;
            case PRESS:
               float fade5 = life < 0.15F ? life / 0.15F : (life > 0.5F ? (1.0F - life) / 0.5F : 1.0F);
               this.alpha = 0.85F * fade5;
               break;
            case INK:
               if (this.onGround) {
                  this.quadSize = Math.min(this.baseSize * 1.6F, this.quadSize * 1.08F);
                  this.xd = 0.0D;
                  this.zd = 0.0D;
               }

               if (life > 0.7F) {
                  this.alpha = Math.max(0.0F, 1.0F - (life - 0.7F) / 0.3F);
               }
               break;
            case SOULFLAME:
               this.setSpriteFromAge(this.sprites);
               float fade6 = life < 0.12F ? life / 0.12F : (life > 0.55F ? (1.0F - life) / 0.45F : 1.0F);
               this.alpha = 0.9F * fade6 * (0.85F + 0.15F * Mth.sin((float)this.age * 1.9F));
               this.quadSize = this.baseSize * (1.0F - life * 0.35F);
               break;
            case HEART:
               double ang = this.orbit + this.rate * (double)this.age;
               this.setPos(this.homeX + Math.cos(ang) * (double)this.maxRadius, this.homeY + 0.25D * (double)life + 0.06D * Math.sin((double)this.age * 0.4D), this.homeZ + Math.sin(ang) * (double)this.maxRadius);
               float fade7 = life < 0.15F ? life / 0.15F : (life > 0.7F ? (1.0F - life) / 0.3F : 1.0F);
               this.alpha = 0.95F * fade7;
               break;
            default:
               if (this.age >= this.lifetime) {
                  this.remove();
               }
         }

      }
   }

   private void back() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      this.oRoll = this.roll;
      if (this.style == FxParticle.Style.RING) {
         --this.age;
         if (this.age <= 0) {
            this.remove();
         }

      } else if (this.age <= 0) {
         this.remove();
      } else {
         double dx = this.homeX - this.x;
         double dy = this.homeY - this.y;
         double dz = this.homeZ - this.z;
         double k = 1.0D / (double)this.age;
         this.setPos(this.x + dx * k, this.y + dy * k, this.z + dz * k);
         --this.age;
      }
   }

   public void render(VertexConsumer vc, Camera camera, float partialTick) {
      if (this.style == FxParticle.Style.REWIND) {
         this.filament(vc, camera, partialTick);
      } else if (this.style == FxParticle.Style.FILAMENT) {
         this.filament(vc, camera, partialTick);
      } else if (this.style == FxParticle.Style.PRESS) {
         this.filament(vc, camera, partialTick);
      } else if (this.style != FxParticle.Style.RING) {
         super.render(vc, camera, partialTick);
      } else {
         Vec3 cam = camera.getPosition();
         float px = (float)(Mth.lerp((double)partialTick, this.xo, this.x) - cam.x());
         float py = (float)(Mth.lerp((double)partialTick, this.yo, this.y) - cam.y());
         float pz = (float)(Mth.lerp((double)partialTick, this.zo, this.z) - cam.z());
         float step = this.mode == 1 ? 1.0F : (this.mode == 2 ? 2.0F - partialTick : partialTick);
         float t = Mth.clamp(((float)this.age + step) / (float)this.lifetime, 0.0F, 1.0F);
         float inv = 1.0F - t;
         float r = this.maxRadius * (1.0F - inv * inv);
         float a = this.alpha * (1.0F - t * t);
         int light = this.getLightColor(partialTick);
         float u0 = this.getU0();
         float u1 = this.getU1();
         float v0 = this.getV0();
         float v1 = this.getV1();
         vc.vertex((double)(px - r), (double)py, (double)(pz - r)).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
         vc.vertex((double)(px - r), (double)py, (double)(pz + r)).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
         vc.vertex((double)(px + r), (double)py, (double)(pz + r)).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
         vc.vertex((double)(px + r), (double)py, (double)(pz - r)).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
         vc.vertex((double)(px + r), (double)py, (double)(pz - r)).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
         vc.vertex((double)(px + r), (double)py, (double)(pz + r)).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
         vc.vertex((double)(px - r), (double)py, (double)(pz + r)).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
         vc.vertex((double)(px - r), (double)py, (double)(pz - r)).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
      }
   }

   private void filament(VertexConsumer vc, Camera camera, float partialTick) {
      Vec3 cam = camera.getPosition();
      double px = Mth.lerp((double)partialTick, this.xo, this.x) - cam.x();
      double py = Mth.lerp((double)partialTick, this.yo, this.y) - cam.y();
      double pz = Mth.lerp((double)partialTick, this.zo, this.z) - cam.z();
      double vx = this.mode == 1 ? this.stillX : this.x - this.xo;
      double vy = this.mode == 1 ? this.stillY : this.y - this.yo;
      double vz = this.mode == 1 ? this.stillZ : this.z - this.zo;
      double sp = Math.sqrt(vx * vx + vy * vy + vz * vz);
      if (sp < 1.0E-4D) {
         super.render(vc, camera, partialTick);
      } else {
         double ax = vx / sp;
         double ay = vy / sp;
         double az = vz / sp;
         double sx = ay * -pz - az * -py;
         double sy = az * -px - ax * -pz;
         double sz = ax * -py - ay * -px;
         double sl = Math.sqrt(sx * sx + sy * sy + sz * sz);
         if (sl < 1.0E-6D) {
            super.render(vc, camera, partialTick);
         } else {
            double w = (double)this.quadSize;
            sx = sx / sl * w;
            sy = sy / sl * w;
            sz = sz / sl * w;
            double len = Math.min(2.4D, w * 2.0D + sp * 1.8D);
            float hx = (float)(px + ax * w);
            float hy = (float)(py + ay * w);
            float hz = (float)(pz + az * w);
            float tx = (float)(px - ax * len);
            float ty = (float)(py - ay * len);
            float tz = (float)(pz - az * len);
            int light = this.getLightColor(partialTick);
            float u0 = this.getU0();
            float u1 = (this.getU0() + this.getU1()) * 0.5F;
            float v0 = this.getV0();
            float v1 = this.getV1();
            float a = this.alpha;
            vc.vertex((double)tx + sx, (double)ty + sy, (double)tz + sz).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
            vc.vertex((double)hx + sx, (double)hy + sy, (double)hz + sz).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
            vc.vertex((double)hx - sx, (double)hy - sy, (double)hz - sz).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
            vc.vertex((double)tx - sx, (double)ty - sy, (double)tz - sz).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
            vc.vertex((double)tx - sx, (double)ty - sy, (double)tz - sz).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
            vc.vertex((double)hx - sx, (double)hy - sy, (double)hz - sz).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
            vc.vertex((double)hx + sx, (double)hy + sy, (double)hz + sz).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
            vc.vertex((double)tx + sx, (double)ty + sy, (double)tz + sz).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, a).uv2(light).endVertex();
         }
      }
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public int getLightColor(float partialTick) {
      return this.style == FxParticle.Style.DROP ? super.getLightColor(partialTick) : 15728880;
   }

   public static class Provider implements ParticleProvider<TintedParticleOptions> {
      private final SpriteSet sprites;
      private final FxParticle.Style style;

      public Provider(SpriteSet sprites, FxParticle.Style style) {
         this.sprites = sprites;
         this.style = style;
      }

      public Particle createParticle(TintedParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
         return new FxParticle(level, x, y, z, this.sprites, this.style, xd, yd, zd, options.rgb());
      }
   }

   public static enum Style {
      DROP,
      SHARD,
      EMBER,
      ARC,
      WISP,
      RING,
      FLARE,
      GALE,
      GLINT,
      WAVE,
      FILAMENT,
      SCRIPT,
      VEIN,
      REWIND,
      FLAKE,
      PRESS,
      INK,
      SOULFLAME,
      HEART;

      // $FF: synthetic method
      private static FxParticle.Style[] $values() {
         return new FxParticle.Style[]{DROP, SHARD, EMBER, ARC, WISP, RING, FLARE, GALE, GLINT, WAVE, FILAMENT, SCRIPT, VEIN, REWIND, FLAKE, PRESS, INK, SOULFLAME, HEART};
      }
   }
}
