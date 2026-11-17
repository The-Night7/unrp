package com.myriam.unrp.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public class FxOptions implements ParticleOptions {
    private final ParticleType<FxOptions> type;
    public final int rgb;
    public final float size;
    public final int life;

    public FxOptions(ParticleType<FxOptions> type, int rgb, float size, int life) {
        this.type = type;
        this.rgb = rgb & 0xFFFFFF;
        this.size = size;
        this.life = life;
    }

    @Override
    public ParticleType<FxOptions> getType() {
        return type;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeInt(rgb);
        buf.writeFloat(size);
        buf.writeVarInt(life);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %06x %.2f %d", ForgeRegistries.PARTICLE_TYPES.getKey(type), rgb, size, life);
    }

    public static Codec<FxOptions> codec(ParticleType<FxOptions> type) {
        return RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("rgb").forGetter(o -> o.rgb),
                Codec.FLOAT.fieldOf("size").forGetter(o -> o.size),
                Codec.INT.fieldOf("life").forGetter(o -> o.life)
        ).apply(i, (rgb, size, life) -> new FxOptions(type, rgb, size, life)));
    }

    @SuppressWarnings("deprecation")
    public static final ParticleOptions.Deserializer<FxOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public FxOptions fromCommand(ParticleType<FxOptions> type, StringReader r) throws CommandSyntaxException {
            r.expect(' ');
            int rgb = r.readInt();
            r.expect(' ');
            float size = r.readFloat();
            r.expect(' ');
            int life = r.readInt();
            return new FxOptions(type, rgb, size, life);
        }

        @Override
        public FxOptions fromNetwork(ParticleType<FxOptions> type, FriendlyByteBuf buf) {
            return new FxOptions(type, buf.readInt(), buf.readFloat(), buf.readVarInt());
        }
    };
}
