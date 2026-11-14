package com.myriam.unrp.deal;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PowerParticles {
   public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "unrp");
   public static final RegistryObject<SimpleParticleType> PURPLE_FLAME = PARTICLES.register("purple_flame", () -> new SimpleParticleType(false));
   public static final RegistryObject<SimpleParticleType> MOTE = PARTICLES.register("aura_mote", () -> new SimpleParticleType(false));
   public static final RegistryObject<TintedParticleType> WATER_DROP = PARTICLES.register("water_drop", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> FROST_SHARD = PARTICLES.register("frost_shard", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> SHADOW_EMBER = PARTICLES.register("shadow_ember", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> STORM_ARC = PARTICLES.register("storm_arc", () -> new TintedParticleType(true));
   public static final RegistryObject<TintedParticleType> DREAD_WISP = PARTICLES.register("dread_wisp", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> SPARK_RING = PARTICLES.register("spark_ring", () -> new TintedParticleType(true));
   public static final RegistryObject<TintedParticleType> ENERGY_FLARE = PARTICLES.register("energy_flare", () -> new TintedParticleType(true));
   public static final RegistryObject<TintedParticleType> GALE_STREAK = PARTICLES.register("gale_streak", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> STEEL_GLINT = PARTICLES.register("steel_glint", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> PUNCH_WAVE = PARTICLES.register("punch_wave", () -> new TintedParticleType(true));
   public static final RegistryObject<TintedParticleType> VOID_FILAMENT = PARTICLES.register("void_filament", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> SCRIPT_GLYPH = PARTICLES.register("script_glyph", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> AMBER_VEIN = PARTICLES.register("amber_vein", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> REWIND_DASH = PARTICLES.register("rewind_dash", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> EDGE_FLAKE = PARTICLES.register("edge_flake", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> PRESSURE_STREAK = PARTICLES.register("pressure_streak", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> PACT_INK = PARTICLES.register("pact_ink", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> SOUL_FLAME = PARTICLES.register("soul_flame", () -> new TintedParticleType(false));
   public static final RegistryObject<TintedParticleType> PACT_HEART = PARTICLES.register("pact_heart", () -> new TintedParticleType(false));
}
