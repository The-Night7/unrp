package com.myriam.unrp;

import com.myriam.unrp.command.UnrpCommands;
import com.myriam.unrp.data.UnodexLoader;
import com.myriam.unrp.config.UnrpClientConfig;
import com.myriam.unrp.menu.MenuService;
import com.myriam.unrp.net.Net;
import com.myriam.unrp.player.PlayerStateService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(UnrpMod.MOD_ID)
public class UnrpMod {
    public static final String MOD_ID = "unrp";

    public UnrpMod() {
        Net.register();
        com.myriam.unrp.item.ModEffects.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.myriam.unrp.item.ModBlocks.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.myriam.unrp.deal.PowerParticles.PARTICLES.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus()); // particules du modèle (unrp:*)
        com.myriam.unrp.deal.DealBridge.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.myriam.unrp.entity.ModEntities.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.myriam.unrp.uo.ModBlockEntities.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.myriam.unrp.uo.ModSounds.SOUND_EVENTS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.myriam.unrp.uo.ModLoot.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.myriam.unrp.item.ModItems.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        com.myriam.unrp.particle.ModParticles.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, UnrpClientConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new com.myriam.unrp.social.SocialEvents());
        MinecraftForge.EVENT_BUS.register(new com.myriam.unrp.social.Reputation());
        MinecraftForge.EVENT_BUS.register(new com.myriam.unrp.style.StyleEvents());
        MinecraftForge.EVENT_BUS.register(new com.myriam.unrp.ability.AbilityEvents());
    }

    /** Les données Unodex sont rechargées avec /reload, comme un datapack. */
    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new UnodexLoader());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        UnrpCommands.register(event.getDispatcher());
    }

    /** À la connexion, un joueur sans personnage voit le menu de candidature. */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MenuService.onLogin(player);
            PlayerStateService.send(player);
            com.myriam.unrp.style.StyleService.onLogin(player);
            com.myriam.unrp.item.Cosmetics.onLogin(player);
            com.myriam.unrp.ability.Specials17.onLogin(player);
        }
    }

    /** À l'arrêt du serveur, les barrières temporaires sont retirées. */
    @SubscribeEvent
    public void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        com.myriam.unrp.ability.AbilityEngine.restoreWalls();
    }

    /** Une fois par seconde : régénération de l'aura. */
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) com.myriam.unrp.ability.AbilityEngine.tickFast(event.getServer());
        if (event.phase == TickEvent.Phase.END && event.getServer().getTickCount() % 4 == 0) {
            com.myriam.unrp.ability.AbilityEngine.tick(event.getServer());
        }
        if (event.phase == TickEvent.Phase.END && event.getServer().getTickCount() % 20 == 0) {
            PlayerStateService.tick(event.getServer());
        }
    }
}
