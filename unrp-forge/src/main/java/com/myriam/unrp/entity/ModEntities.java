package com.myriam.unrp.entity;

import com.myriam.unrp.UnrpMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Créatures du mod. */
public final class ModEntities {
    private ModEntities() {}

    private static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, UnrpMod.MOD_ID);

    /** Le double de Hollow Mark (HollowDoppelEntity du modèle). */
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.HollowDoppelEntity>> HOLLOW_DOPPEL = TYPES.register("hollow_doppel",
            () -> EntityType.Builder.of(com.myriam.unrp.uo.HollowDoppelEntity::new, MobCategory.MISC).sized(0.6f, 1.8f).clientTrackingRange(10).noSummon().build("hollow_doppel"));

    /** AgentEntity du modèle (gangs, officiers de l'Autorité, chasseurs de la réputation). */
    public static final RegistryObject<EntityType<Agent>> AGENT = TYPES.register("agent",
            () -> EntityType.Builder.of(Agent::new, MobCategory.MONSTER).sized(0.6f, 1.8f).clientTrackingRange(10).build("agent"));

    /** BarrierEntity du modèle (Barrier). */
    public static final RegistryObject<EntityType<com.myriam.unrp.pass.BarrierEntity>> BARRIER = TYPES.register("barrier",
            () -> EntityType.Builder.<com.myriam.unrp.pass.BarrierEntity>of(com.myriam.unrp.pass.BarrierEntity::new, MobCategory.MISC)
                    .sized(1.75f, 1.75f).clientTrackingRange(10).updateInterval(2).fireImmune().build("barrier"));

    /** Projectiles de Limit Breaker (ArcaneBoltEntity, HomingBeamEntity, WaveOrbEntity du modèle). */
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.ArcaneBoltEntity>> ARCANE_BOLT = TYPES.register("arcane_bolt",
            () -> EntityType.Builder.<com.myriam.unrp.uo.ArcaneBoltEntity>of(com.myriam.unrp.uo.ArcaneBoltEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f).clientTrackingRange(12).updateInterval(1).fireImmune().build("arcane_bolt"));
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.HomingBeamEntity>> HOMING_BEAM = TYPES.register("homing_beam",
            () -> EntityType.Builder.<com.myriam.unrp.uo.HomingBeamEntity>of(com.myriam.unrp.uo.HomingBeamEntity::new, MobCategory.MISC)
                    .sized(0.35f, 0.35f).clientTrackingRange(12).updateInterval(1).fireImmune().build("homing_beam"));
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.WaveOrbEntity>> WAVE_ORB = TYPES.register("wave_orb",
            () -> EntityType.Builder.<com.myriam.unrp.uo.WaveOrbEntity>of(com.myriam.unrp.uo.WaveOrbEntity::new, MobCategory.MISC)
                    .sized(0.6f, 0.6f).clientTrackingRange(24).updateInterval(1).fireImmune().noSummon().build("wave_orb"));

    /** BindVisualEntity du modèle (cercueil de sable de Sarcophage). */
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.BindVisualEntity>> BIND_VISUAL = TYPES.register("bind_visual",
            () -> EntityType.Builder.<com.myriam.unrp.uo.BindVisualEntity>of(com.myriam.unrp.uo.BindVisualEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).updateInterval(2).fireImmune().build("bind_visual"));

    /** Entités de Daystar (SunOrb/SunFissure/SunCrescent/SunAxe du modèle). */
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.SunOrbEntity>> SUN_ORB = TYPES.register("sun_orb",
            () -> EntityType.Builder.<com.myriam.unrp.uo.SunOrbEntity>of(com.myriam.unrp.uo.SunOrbEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(12).updateInterval(1).fireImmune().noSummon().build("sun_orb"));
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.SunFissureEntity>> SUN_FISSURE = TYPES.register("sun_fissure",
            () -> EntityType.Builder.<com.myriam.unrp.uo.SunFissureEntity>of(com.myriam.unrp.uo.SunFissureEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).updateInterval(4).fireImmune().noSummon().build("sun_fissure"));
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.SunCrescentEntity>> SUN_CRESCENT = TYPES.register("sun_crescent",
            () -> EntityType.Builder.<com.myriam.unrp.uo.SunCrescentEntity>of(com.myriam.unrp.uo.SunCrescentEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).updateInterval(1).fireImmune().noSummon().build("sun_crescent"));
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.SunAxeEntity>> SUN_AXE = TYPES.register("sun_axe",
            () -> EntityType.Builder.<com.myriam.unrp.uo.SunAxeEntity>of(com.myriam.unrp.uo.SunAxeEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).updateInterval(1).fireImmune().noSummon().build("sun_axe"));

    /** AuraThreadEntity du modèle (fil d'Aura Transference). */
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.AuraThreadEntity>> AURA_THREAD = TYPES.register("aura_thread",
            () -> EntityType.Builder.<com.myriam.unrp.uo.AuraThreadEntity>of(com.myriam.unrp.uo.AuraThreadEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(6).updateInterval(4).fireImmune().noSummon().build("aura_thread"));

    /** Entités d'Armament Dominion (ArsenalBoltEntity / ArsenalGateEntity du modèle). */
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.ArsenalBoltEntity>> ARSENAL_BOLT = TYPES.register("arsenal_bolt",
            () -> EntityType.Builder.<com.myriam.unrp.uo.ArsenalBoltEntity>of(com.myriam.unrp.uo.ArsenalBoltEntity::new, MobCategory.MISC)
                    .sized(0.35f, 0.35f).clientTrackingRange(8).updateInterval(2).fireImmune().build("arsenal_bolt"));
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.ArsenalGateEntity>> ARSENAL_GATE = TYPES.register("arsenal_gate",
            () -> EntityType.Builder.<com.myriam.unrp.uo.ArsenalGateEntity>of(com.myriam.unrp.uo.ArsenalGateEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(8).updateInterval(2).fireImmune().noSummon().build("arsenal_gate"));

    /** Entités de Particles (ParticleConstruct/Bolt/Fist du modèle). */
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.ParticleConstructEntity>> PARTICLE_CONSTRUCT = TYPES.register("particle_construct",
            () -> EntityType.Builder.<com.myriam.unrp.uo.ParticleConstructEntity>of(com.myriam.unrp.uo.ParticleConstructEntity::new, MobCategory.MISC)
                    .sized(1.2f, 2.2f).clientTrackingRange(10).updateInterval(2).fireImmune().build("particle_construct"));
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.ParticleBoltEntity>> PARTICLE_BOLT = TYPES.register("particle_bolt",
            () -> EntityType.Builder.<com.myriam.unrp.uo.ParticleBoltEntity>of(com.myriam.unrp.uo.ParticleBoltEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).updateInterval(1).fireImmune().build("particle_bolt"));
    public static final RegistryObject<EntityType<com.myriam.unrp.uo.ParticleFistEntity>> PARTICLE_FIST = TYPES.register("particle_fist",
            () -> EntityType.Builder.<com.myriam.unrp.uo.ParticleFistEntity>of(com.myriam.unrp.uo.ParticleFistEntity::new, MobCategory.MISC)
                    .sized(0.8f, 0.8f).clientTrackingRange(10).updateInterval(1).fireImmune().build("particle_fist"));

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        bus.addListener((EntityAttributeCreationEvent e) -> {
            e.put(HOLLOW_DOPPEL.get(), com.myriam.unrp.uo.HollowDoppelEntity.createAttributes().build());
            e.put(AGENT.get(), Agent.createAttributes().build());
        });
    }
}
