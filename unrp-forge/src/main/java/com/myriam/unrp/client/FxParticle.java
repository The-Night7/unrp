package com.myriam.unrp.client;

import com.myriam.unrp.particle.FxOptions;
import com.myriam.unrp.particle.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class FxParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final String kind;
    private final float baseSize;
    private final float spin;

    FxParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, FxOptions o, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.kind = ModParticles.nameOf(o.getType());
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.rCol = ((o.rgb >> 16) & 255) / 255f;
        this.gCol = ((o.rgb >> 8) & 255) / 255f;
        this.bCol = (o.rgb & 255) / 255f;
        this.baseSize = 0.15f * Math.max(0.1f, o.size);
        this.quadSize = baseSize;
        this.hasPhysics = false;
        this.alpha = 1f;
        this.gravity = 0f;
        this.friction = 0.92f;
        int def = switch (kind) {
            case "ring" -> 10;
            case "arc" -> 5;
            case "smoke" -> 24 + random.nextInt(10);
            case "leaf", "drop" -> 30 + random.nextInt(10);
            case "ember" -> 14 + random.nextInt(8);
            default -> 12 + random.nextInt(6);
        };
        this.lifetime = o.life > 0 ? o.life : def;
        switch (kind) {
            case "drop" -> this.gravity = 0.6f;
            case "leaf" -> this.gravity = 0.04f;
            case "ember" -> this.gravity = -0.02f;
            default -> { }
        }
        this.spin = switch (kind) {
            case "shard", "rock", "glyph", "leaf", "hex" -> (random.nextFloat() - 0.5f) * 0.3f;
            default -> 0f;
        };
        this.roll = random.nextFloat() * 6.28f;
        this.oRoll = roll;
        if (spin == 0f) {
            this.roll = 0f;
            this.oRoll = 0f;
        }
        pickSprite();
    }

    private void pickSprite() {
        if ("arc".equals(kind) || "glyph".equals(kind) || "rock".equals(kind) || "shard".equals(kind) || "leaf".equals(kind)) {
            setSprite(sprites.get(random.nextInt(100), 99));
        } else {
            setSpriteFromAge(sprites);
        }
    }

    @Override
    public void tick() {
        super.tick();
        float t = age / (float) Math.max(1, lifetime);
        alpha = 1f - t * t;
        oRoll = roll;
        roll += spin;
        switch (kind) {
            case "ring" -> quadSize = baseSize * (1f + 3f * t);
            case "smoke" -> {
                quadSize = baseSize * (1f + 1.5f * t);
                alpha = 0.75f * (1f - t);
            }
            case "ember" -> quadSize = baseSize * (1f - 0.6f * t);
            case "arc" -> setSprite(sprites.get(random.nextInt(100), 99));
            case "leaf" -> xd += Math.sin(age * 0.3) * 0.004;
            default -> {
                if (!"glyph".equals(kind) && !"rock".equals(kind) && !"shard".equals(kind)) setSpriteFromAge(sprites);
            }
        }
    }

    @Override
    public int getLightColor(float partial) {
        return "smoke".equals(kind) ? super.getLightColor(partial) : 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<FxOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(FxOptions o, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
            return new FxParticle(level, x, y, z, vx, vy, vz, o, sprites);
        }
    }
}
