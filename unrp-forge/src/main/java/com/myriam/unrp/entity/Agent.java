package com.myriam.unrp.entity;

import com.myriam.unrp.data.ClaimData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * AgentEntity du modèle : PNJ humanoïde hostile à quatre apparences (0 agent, 1 meneur, 2 Spectre, 3 Autorité).
 * Le meneur (1) s'embrase à mi-vie (dégâts 15, vitesse 0,36), fait un anneau de feu au contact et fonce de loin.
 * Les agents chassent d'eux-mêmes les joueurs de niveau 5 ou plus ; les officiers d'Autorité et les chasseurs
 * (réputation) chassent leur cible quoi qu'il arrive.
 */
public class Agent extends Monster {
    public static final String CAMP_TAG = "unrp.ember_camp";
    public static final String CAMP_HOME = "unrp.camp_home";
    public static final double CAMP_GUARD_RANGE = 18.0;
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(Agent.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_SURGE = SynchedEntityData.defineId(Agent.class, EntityDataSerializers.BOOLEAN);
    private boolean forcedPersist = false;
    private int ringCd = 0;
    private int rushCd = 0;

    public Agent(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 48.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_VARIANT, (byte) 0);
        entityData.define(DATA_SURGE, false);
    }

    public boolean isSurging() { return entityData.get(DATA_SURGE); }
    public void setSurging(boolean b) { entityData.set(DATA_SURGE, b); }
    public int getVariant() { return entityData.get(DATA_VARIANT); }
    public void setVariant(int v) { entityData.set(DATA_VARIANT, (byte) v); }

    public void setPersist(boolean b) {
        forcedPersist = b;
        if (b) setPersistenceRequired();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("AgentVariant", (byte) getVariant());
        tag.putBoolean("ForcedPersist", forcedPersist);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AgentVariant")) setVariant(tag.getByte("AgentVariant"));
        if (tag.contains("ForcedPersist")) forcedPersist = tag.getBoolean("ForcedPersist");
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, e -> isValidHunt(this, e)));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Agent.class, 10, true, false,
                e -> e instanceof Agent other && (other.getVariant() == 2) != (getVariant() == 2)));
    }

    /** Niveau de pouvoir au sens du modèle (power_level : notre niveau x 10). */
    static int powerLevel(ServerPlayer p) {
        return (int) Math.round(ClaimData.get(p.server).profileOf(p.getUUID()).map(pr -> pr.level * 10).orElse(0.0));
    }

    private static boolean isValidHunt(Agent self, LivingEntity e) {
        if (!(e instanceof ServerPlayer sp) || sp.isCreative() || sp.isSpectator()) return false;
        if (self.getTags().contains("unrp.enforcer") || self.getTags().contains("unrp.spectre_enforcer")) return true;
        if (self.getTags().contains("unrp.authority_officer") || self.getTags().contains("unrp.rep_hunter")) return true;
        if (com.myriam.unrp.ability.AbilityEngine.disabled(sp)) return false;
        if (sp.getTags().contains("unrp.pact_allied")) return false;
        if (guarding(self, sp)) return true;
        return powerLevel(sp) >= 50;
    }

    public static boolean guarding(Agent self, LivingEntity e) {
        if (!self.getTags().contains(CAMP_TAG) || !self.getPersistentData().contains(CAMP_HOME)) return false;
        BlockPos home = BlockPos.of(self.getPersistentData().getLong(CAMP_HOME));
        return e.level() == self.level() && e.distanceToSqr(Vec3.atCenterOf(home)) <= CAMP_GUARD_RANGE * CAMP_GUARD_RANGE;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (getTags().contains(CAMP_TAG) && !getPersistentData().contains(CAMP_HOME)) getPersistentData().putLong(CAMP_HOME, blockPosition().asLong());
        if (tickCount % 40 == 0 && getTarget() == null) {
            Player near = level().getNearestPlayer(this, 32.0);
            if (near instanceof ServerPlayer sp && isValidHunt(this, sp)) setTarget(sp);
            if (getTarget() == null && level() instanceof ServerLevel sl) {
                boolean selfSpectre = getVariant() == 2;
                Agent rival = null;
                double best = Double.MAX_VALUE;
                for (Agent other : sl.getEntitiesOfClass(Agent.class, getBoundingBox().inflate(24.0),
                        a -> a != this && a.isAlive() && (a.getVariant() == 2) != selfSpectre)) {
                    double d = distanceToSqr(other);
                    if (d < best) { best = d; rival = other; }
                }
                if (rival != null) setTarget(rival);
            }
        }
        if (tickCount % 20 == 0 && getTarget() instanceof ServerPlayer tp && getLastHurtByMob() != tp && !isValidHunt(this, tp)) setTarget(null);
        if (getVariant() == 1 && level() instanceof ServerLevel sl) tickLeader(sl);
    }

    private void tickLeader(ServerLevel sl) {
        if (!isSurging() && getHealth() <= getMaxHealth() * 0.5F) {
            setSurging(true);
            AttributeInstance dmg = getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmg != null) dmg.setBaseValue(15.0);
            AttributeInstance spd = getAttribute(Attributes.MOVEMENT_SPEED);
            if (spd != null) spd.setBaseValue(0.36);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.0, getZ(), 40, 0.5, 0.8, 0.5, 0.05);
            sl.playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.2F, 0.6F);
        }
        if (ringCd > 0) --ringCd;
        if (rushCd > 0) --rushCd;
        SimpleParticleType flame = isSurging() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
        LivingEntity tgt = getTarget();
        if (tgt != null && tgt.isAlive()) {
            double dist = distanceTo(tgt);
            if (ringCd <= 0 && dist < 3.5) {
                fireRing(sl, flame);
                ringCd = 160;
            } else if (rushCd <= 0 && dist > 6.0 && dist < 15.0) {
                Vec3 dir = tgt.position().add(0, tgt.getBbHeight() * 0.5, 0).subtract(position()).normalize();
                setDeltaMovement(dir.scale(1.35).add(0, 0.25, 0));
                addTag("unrp.leader_primed");
                sl.sendParticles(flame, getX(), getY() + 0.8, getZ(), 14, 0.3, 0.4, 0.3, 0.03);
                sl.playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 1.0F, 1.3F);
                rushCd = 140;
            }
        }
        if (tickCount % 3 == 0) {
            double yaw = Math.toRadians(yBodyRot);
            double rx = Math.cos(yaw) * 0.38, rz = Math.sin(yaw) * 0.38;
            double fx = -Math.sin(yaw) * 0.12, fz = Math.cos(yaw) * 0.12;
            sl.sendParticles(flame, getX() + rx + fx, getY() + 0.85, getZ() + rz + fz, 1, 0.05, 0.1, 0.05, 0.01);
            sl.sendParticles(flame, getX() - rx + fx, getY() + 0.85, getZ() - rz + fz, 1, 0.05, 0.1, 0.05, 0.01);
        }
    }

    private void fireRing(ServerLevel sl, SimpleParticleType flame) {
        float dmgV = isSurging() ? 7.0F : 5.0F;
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.8))) {
            if (e != this && !e.getTags().contains("unrp.ember_agent")) {
                e.hurt(sl.damageSources().mobAttack(this), dmgV);
                e.setSecondsOnFire(4);
            }
        }
        for (int i = 0; i < 24; ++i) {
            double a = Math.PI * 2 * i / 24.0;
            sl.sendParticles(flame, getX() + Math.cos(a) * 3.0, getY() + 0.4, getZ() + Math.sin(a) * 3.0, 2, 0.05, 0.15, 0.05, 0.01);
        }
        sl.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.0F, 0.9F);
    }

    @Override
    public void checkDespawn() {
        if (forcedPersist || isPersistenceRequired()) return;
        Player nearest = level().getNearestPlayer(this, -1.0);
        if (nearest == null || distanceToSqr(nearest) > 16384.0) discard();
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !forcedPersist && !isPersistenceRequired();
    }
}
