package com.myriam.unrp.power;

import com.myriam.unrp.data.PlayerData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class Powers {
    private static final Map<String, Power> REGISTRY = new LinkedHashMap<>();
    private static final Random RANDOM = new Random();

    private Powers() {}

    public static void register(Power power) {
        REGISTRY.put(power.id, power);
    }

    public static Power get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<String> ids() {
        return REGISTRY.keySet();
    }

    public static String randomId() {
        List<String> ids = new ArrayList<>(REGISTRY.keySet());
        return ids.get(RANDOM.nextInt(ids.size()));
    }

    public static void init() {

        register(new Power("speed", 20, 100, (player, data) -> {
            int speed = data.stats[PlayerData.SPEED];
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, 200 + 20 * speed, speed / 3));
        }));

        register(new Power("stone_skin", 25, 200, (player, data) -> {
            int defense = data.stats[PlayerData.DEFENSE];
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE, 200 + 20 * defense, defense / 4));
        }));

        register(new Power("healing", 30, 200, (player, data) -> {
            player.heal(2.0f + data.stats[PlayerData.RECOVERY]);
            player.getServerWorld().spawnParticles(ParticleTypes.HEART,
                    player.getX(), player.getBodyY(0.5), player.getZ(), 6, 0.3, 0.3, 0.3, 0.0);
        }));

        register(new Power("explosion", 40, 100, (player, data) -> {
            HitResult hit = player.raycast(24.0, 1.0f, false);
            Vec3d pos = hit.getPos();
            float strength = 1.0f + 0.3f * data.stats[PlayerData.POWER];
            player.getServerWorld().createExplosion(player, pos.x, pos.y, pos.z,
                    strength, World.ExplosionSourceType.NONE);
        }));
    }
}
