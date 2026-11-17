package com.myriam.unrp.client;

import com.myriam.unrp.UnrpMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    public static final KeyMapping HUB_KEY = new KeyMapping("key.unrp.hub", GLFW.GLFW_KEY_H, "key.categories.unrp");

    public static final KeyMapping ABILITY_KEY = new KeyMapping("key.unrp.ability", GLFW.GLFW_KEY_R, "key.categories.unrp");

    /** Registers the move-bar overlay. */
    public static final String[] SLOT_LETTERS = {"X", "C", "V", "B", "N"};
    public static final KeyMapping[] MOVE_KEYS = {
            new KeyMapping("key.unrp.move1", GLFW.GLFW_KEY_X, "key.categories.unrp"),
            new KeyMapping("key.unrp.move2", GLFW.GLFW_KEY_C, "key.categories.unrp"),
            new KeyMapping("key.unrp.move3", GLFW.GLFW_KEY_V, "key.categories.unrp"),
            new KeyMapping("key.unrp.move4", GLFW.GLFW_KEY_B, "key.categories.unrp"),
            new KeyMapping("key.unrp.move5", GLFW.GLFW_KEY_N, "key.categories.unrp")};
    private static final boolean[] down = new boolean[5];

    public static final long[] holdStart = {-1, -1, -1, -1, -1};

    private ClientEvents() {}

    private static void act(int action) {
        com.myriam.unrp.net.Net.CHANNEL.sendToServer(new com.myriam.unrp.net.UseAbilityPacket(action));
    }

    @Mod.EventBusSubscriber(modid = UnrpMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void onKeys(RegisterKeyMappingsEvent event) {
            event.register(ABILITY_KEY);
            event.register(HUB_KEY);
        }

        @SubscribeEvent
        public static void onLayers(net.minecraftforge.client.event.EntityRenderersEvent.AddLayers event) {
            com.myriam.unrp.uo.ColossusRenderer.bake(event.getEntityModels());
            com.myriam.unrp.uo.PhageRenderer.bake(event.getEntityModels());
            for (String skin : event.getSkins()) {
                if (event.getSkin(skin) instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer pr) {

                    pr.addLayer(new com.myriam.unrp.look.CustomEyesLayer(pr));
                    pr.addLayer(new CosmeticLayer(pr, event.getEntityModels()));
                    pr.addLayer(new com.myriam.unrp.pass.PassLayers.TimeGears(pr));
                    pr.addLayer(new com.myriam.unrp.pass.ConjureShieldLayer(pr));
                    pr.addLayer(new com.myriam.unrp.pass.JokerEmberLayer(pr));
                    pr.addLayer(new com.myriam.unrp.pass.PassLayers.PredatorTrans(pr, event.getEntityModels()));
                    pr.addLayer(new com.myriam.unrp.pass.UnrpPowerLayers.HandBladesLayer(pr));
                    pr.addLayer(new CovenantSignatureLayer(pr));
                    pr.addLayer(new com.myriam.unrp.pass.UnrpPowerLayers.SpectralClawLayer(pr));
                    pr.addLayer(new com.myriam.unrp.pass.UnrpPowerLayers.ArachnidLegsLayer(pr));
                    pr.addLayer(new com.myriam.unrp.uo.SarcophageTailsLayer(pr, event.getEntityModels()));
                    pr.addLayer(new com.myriam.unrp.uo.LbFurLayer(pr, event.getEntityModels()));
                    pr.addLayer(new com.myriam.unrp.uo.LbHairLayer(pr, event.getEntityModels()));
                    pr.addLayer(new com.myriam.unrp.uo.LimitBreakerAuraLayer(pr));
                    pr.addLayer(new com.myriam.unrp.uo.LbEdgeLayer(pr));
                    pr.addLayer(new com.myriam.unrp.pass.HollowSigilLayer(pr));
                    pr.addLayer(new com.myriam.unrp.uo.ImbuedItemLayer(pr));
                    pr.addLayer(new com.myriam.unrp.uo.LashVeinsLayer(pr));
                    pr.addLayer(new com.myriam.unrp.uo.SaltEdgeLayer(pr));
                    pr.addLayer(new com.myriam.unrp.uo.SaltCracksLayer(pr));
                    pr.addLayer(new com.myriam.unrp.uo.DaystarFingerLayer(pr));
                }
            }
        }

        @SubscribeEvent
        public static void onParticles(net.minecraftforge.client.event.RegisterParticleProvidersEvent event) {
            for (var r : com.myriam.unrp.particle.ModParticles.ALL.values()) event.registerSpriteSet(r.get(), FxParticle.Provider::new);
        }

        @SubscribeEvent
        public static void onReloadListeners(net.minecraftforge.client.event.RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) rm -> PoseClient.invalidate());
        }

        @SubscribeEvent
        public static void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
            event.enqueueWork(() -> {

                net.threetag.palladium.client.dynamictexture.DynamicTextureManager.registerVariable(new com.myriam.unrp.uo.OwnerAuraColorVariable.Serializer());
                net.threetag.palladium.client.renderer.renderlayer.PackRenderLayerManager.registerRenderType(new net.minecraft.resources.ResourceLocation("unrp", "translucent"),
                        (source, texture, glint) -> net.minecraft.client.renderer.entity.ItemRenderer.getFoilBufferDirect(source, net.minecraft.client.renderer.RenderType.entityTranslucent(texture), false, glint));

                com.myriam.unrp.look.PowerRenderLayers.register();

                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(com.myriam.unrp.item.ModBlocks.BARRIER_BLOCK.get(), net.minecraft.client.renderer.RenderType.translucent());
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(com.myriam.unrp.item.ModBlocks.BARRIER_BLOCK_AM.get(), net.minecraft.client.renderer.RenderType.translucent());
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get(), net.minecraft.client.renderer.RenderType.translucent());
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(com.myriam.unrp.item.ModBlocks.PHYLACTERY.get(), net.minecraft.client.renderer.RenderType.cutout());

                net.minecraft.client.renderer.item.ItemProperties.register(com.myriam.unrp.item.ModItems.CONTRACT.get(),
                        new net.minecraft.resources.ResourceLocation("unrp", "scroll"), (stack, level, holder, seed) ->
                                stack.hasTag() && stack.getTag().getBoolean("unrp_pact_soul") ? 1f : com.myriam.unrp.deal.ContractItem.isProposed(stack) ? 2f : 0f);
                net.minecraft.client.renderer.item.ItemProperties.register(com.myriam.unrp.item.ModItems.CONTRACT.get(),
                        new net.minecraft.resources.ResourceLocation("unrp", "stamped"), (stack, level, holder, seed) ->
                                stack.hasTag() && stack.getTag().getBoolean("unrp_pact_signed") ? 1f : 0f);
                net.minecraft.client.renderer.item.ItemProperties.register(com.myriam.unrp.item.ModItems.ASCALON.get(),
                        new net.minecraft.resources.ResourceLocation("unrp", "mk2"),
                        (stack, level, holder, seed) -> com.myriam.unrp.item.UnItems.Ascalon.tierOf(stack) >= 2 ? 1f : 0f);
            });
        }

        @SubscribeEvent
        public static void onRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.AGENT.get(), AgentRenderer::new);
            net.threetag.palladium.client.dynamictexture.DynamicTextureManager.registerVariable(new com.myriam.unrp.uo.OwnerAuraColorVariable.Serializer());
            event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.BARRIER.get(), com.myriam.unrp.pass.BarrierRenderer::new);
            event.registerEntityRenderer(com.myriam.unrp.entity.ModEntities.BIND_VISUAL.get(), com.myriam.unrp.uo.BindVisualRenderer::new);
        }

        @SubscribeEvent
        public static void onLayerDefs(net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(AgentRenderer.CLAWS_LAYER, AgentRenderer::clawsLayer);
        }

        @SubscribeEvent
        public static void onOverlays(RegisterGuiOverlaysEvent event) {
            event.registerBelowAll("unrp_weave_swarm", com.myriam.unrp.uo.WeaveHudOverlay.INSTANCE);
            event.registerAboveAll("unrp_hud", com.myriam.unrp.look.UnrpHudOverlay.INSTANCE);
            event.registerAboveAll("unrp_struggle", com.myriam.unrp.uo.StruggleHudOverlay.INSTANCE);
            event.registerAboveAll("unrp_sand", com.myriam.unrp.uo.SandHudOverlay.INSTANCE);
            event.registerAboveAll("unrp_grit", com.myriam.unrp.uo.GritHudOverlay.INSTANCE);
            event.registerAboveAll("unrp_pact", com.myriam.unrp.deal.PactHudOverlay.INSTANCE);
            event.registerAboveAll("unrp_counterweight", com.myriam.unrp.uo.CwHudOverlay.INSTANCE);
            event.registerAboveAll("unrp_daystar_dial", com.myriam.unrp.uo.DaystarDialOverlay.INSTANCE);
            event.registerAboveAll("unrp_lash_ring", com.myriam.unrp.uo.LashRingOverlay.INSTANCE);
            event.registerAboveAll("unrp_salt_hud", com.myriam.unrp.uo.SaltHudOverlay.INSTANCE);
            event.registerAboveAll("particles_stance", com.myriam.unrp.uo.StanceIconOverlay.INSTANCE); // stance_hud_* de particles.json
            event.registerAboveAll("ability_bar", com.myriam.unrp.look.AbilityBar.INSTANCE); // Palladium move bar.
        }
    }

    @Mod.EventBusSubscriber(modid = UnrpMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBus {

        @SubscribeEvent
        public static void onPunchAir(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event) {
            com.myriam.unrp.pass.BarrierPunch.onLeftClickEmpty(event);
        }

        @SubscribeEvent
        public static void onMoveInput(net.minecraftforge.client.event.MovementInputUpdateEvent event) {
            if (!event.getEntity().hasEffect(com.myriam.unrp.item.ModEffects.DISORIENTED.get())) return;
            var in = event.getInput();
            in.forwardImpulse = -in.forwardImpulse;
            in.leftImpulse = -in.leftImpulse;
            boolean t = in.left;
            in.left = in.right;
            in.right = t;
            t = in.up;
            in.up = in.down;
            in.down = t;
        }

        @SubscribeEvent
        public static void onOverlay(net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre event) {

            if (com.myriam.unrp.look.StatClientCache.hudMode != 0) return;
            var id = event.getOverlay().id();
            if (id.equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.PLAYER_HEALTH.id())
                    || id.equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.FOOD_LEVEL.id())
                    || id.equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.ARMOR_LEVEL.id())) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            while (ABILITY_KEY.consumeClick()) {
                if (mc.player != null && mc.screen == null) act(0);
            }
            while (HUB_KEY.consumeClick()) {
                if (mc.player == null || mc.screen != null) continue;
                if (ClientState.get().hasProfile) {
                    mc.setScreen(new HubScreen());
                } else {

                    mc.player.connection.sendCommand("unrp menu");
                }
            }
        }
    }
}
