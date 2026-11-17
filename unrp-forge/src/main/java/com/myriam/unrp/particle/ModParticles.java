package com.myriam.unrp.particle;

import com.mojang.serialization.Codec;
import com.myriam.unrp.UnrpMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModParticles {
    public static final String[] NAMES = {"arc", "ring", "flare", "hex", "mote", "ember", "shard", "drop", "streak",
            "glyph", "smoke", "wisp", "glint", "leaf", "cross", "rock"};

    public static final DeferredRegister<ParticleType<?>> REG = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, UnrpMod.MOD_ID);
    public static final Map<String, RegistryObject<ParticleType<FxOptions>>> ALL = new LinkedHashMap<>();

    static {
        for (String n : NAMES) ALL.put(n, make(n));
    }

    private ModParticles() {}

    @SuppressWarnings("deprecation")
    private static RegistryObject<ParticleType<FxOptions>> make(String name) {
        return REG.register(name, () -> new ParticleType<FxOptions>(true, FxOptions.DESERIALIZER) {
            @Override
            public Codec<FxOptions> codec() {
                return FxOptions.codec(this);
            }
        });
    }

    public static void register(IEventBus modBus) {
        REG.register(modBus);
    }

    public static ParticleType<FxOptions> byName(String name) {
        RegistryObject<ParticleType<FxOptions>> r = name == null ? null : ALL.get(name);
        return r == null ? null : r.get();
    }

    public static String nameOf(ParticleType<?> type) {
        for (var e : ALL.entrySet()) if (e.getValue().get() == type) return e.getKey();
        return "";
    }
}
