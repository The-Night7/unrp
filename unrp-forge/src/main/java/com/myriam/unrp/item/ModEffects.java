package com.myriam.unrp.item;

import com.myriam.unrp.UnrpMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    private ModEffects() {}

    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, UnrpMod.MOD_ID);

    public static final RegistryObject<MobEffect> DISORIENTED = EFFECTS.register("disoriented",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xB86FD8) {});

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
