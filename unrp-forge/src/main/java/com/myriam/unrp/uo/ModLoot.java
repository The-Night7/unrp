package com.myriam.unrp.uo;

import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** ModLoot du modèle : le modificateur « unrp:hollow_relics » (runes et charmes dans les coffres). */
public final class ModLoot {
    private ModLoot() {}

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "unrp");
    public static final RegistryObject<Codec<HollowRelicsModifier>> HOLLOW_RELICS = MODIFIERS.register("hollow_relics", HollowRelicsModifier.CODEC);

    public static void register(IEventBus bus) {
        MODIFIERS.register(bus);
    }
}
