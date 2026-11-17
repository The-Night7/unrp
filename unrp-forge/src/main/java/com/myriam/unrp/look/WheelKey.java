package com.myriam.unrp.look;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "unrp", value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class WheelKey {
    private WheelKey() {}

    public static final KeyMapping KEY = new KeyMapping("key.unrp.wheel", 71, "key.categories.unrp");

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (KEY.consumeClick()) {
            if (mc.player == null) continue;
            if (com.myriam.unrp.pass.UnrpPowerLayers.powerVariant(mc.player, "aura_manipulation") != 0) {
                mc.setScreen(new com.myriam.unrp.am.AmWheelScreen());
                com.myriam.unrp.net.Net.CHANNEL.sendToServer(new com.myriam.unrp.am.AmWheelAskPacket());
            }
        }
    }

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "unrp", bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static final class Keys {
        @SubscribeEvent
        public static void onRegister(RegisterKeyMappingsEvent event) {
            event.register(KEY);
        }
    }
}
