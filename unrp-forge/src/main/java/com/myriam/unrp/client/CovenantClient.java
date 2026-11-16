package com.myriam.unrp.client;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = "unrp", value = Dist.CLIENT)
public final class CovenantClient {
    public record Hand(boolean signed, int souls, boolean deaf, List<String> constitutions) {}
    private record Marker(String name, String dimension, double x, double y, double z, int debt) {}
    private static final List<Marker> MARKERS = new ArrayList<>();
    private static long markersAt;
    private static final Map<Integer, Hand> HANDS = new HashMap<>();
    private static String letter = "";
    private static long letterUntil;
    private static long receivedAt;
    public static Hand hand(int entity) { return System.currentTimeMillis() - receivedAt < 6000 ? HANDS.get(entity) : null; }
    public static void receive(String json) {
        var root = JsonParser.parseString(json).getAsJsonObject();
        if (root.has("writing")) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.screen == null) {
                String prompt=root.get("writing").getAsString();
                if(prompt.trim().equals("/unrp covenant law"))mc.setScreen(new CovenantLawScreen(root.has("level")?root.get("level").getAsDouble():1));
                else mc.setScreen(new net.minecraft.client.gui.screens.ChatScreen(prompt));
            }
        }
        if (root.has("people")) {
            HANDS.clear(); receivedAt = System.currentTimeMillis();
            for (var element : root.getAsJsonArray("people")) {
                var person = element.getAsJsonObject();
                List<String> laws = new ArrayList<>();
                for (var law : person.getAsJsonArray("constitutions")) laws.add(law.getAsString());
                HANDS.put(person.get("entity").getAsInt(), new Hand(person.get("signed").getAsBoolean(),
                        person.get("souls").getAsInt(), person.get("deaf").getAsBoolean(), laws));
            }
        }
        if (root.has("courts")) CovenantEffects.receive(root.getAsJsonArray("courts"));
        if (root.has("markers")) {
            MARKERS.clear(); markersAt = System.currentTimeMillis();
            for (var element : root.getAsJsonArray("markers")) {
                var m = element.getAsJsonObject();
                MARKERS.add(new Marker(m.get("name").getAsString(), m.get("dimension").getAsString(), m.get("x").getAsDouble(),
                        m.get("y").getAsDouble(), m.get("z").getAsDouble(), m.get("debt").getAsInt()));
            }
        }
        if (root.has("letter")) { letter = root.get("letter").getAsString(); letterUntil = System.currentTimeMillis() + 15000; }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { HANDS.clear(); MARKERS.clear(); letter = ""; CovenantEffects.clear(); }
    @SubscribeEvent public static void firstPerson(net.minecraftforge.client.event.RenderArmEvent event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || event.getArm() != net.minecraft.world.entity.HumanoidArm.RIGHT) return;
        Hand signature = hand(mc.player.getId());
        if (signature == null || !signature.signed()) return;
        var renderer = mc.getEntityRenderDispatcher().getRenderer(mc.player);
        if (renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer) {
            event.getPoseStack().pushPose();
            playerRenderer.getModel().rightArm.translateAndRotate(event.getPoseStack());
            CovenantSignatureLayer.signature(event.getPoseStack(), event.getMultiBufferSource(), signature.souls());
            event.getPoseStack().popPose();
        }
    }
    @SubscribeEvent public static void sound(PlaySoundEvent event) {
        var player = Minecraft.getInstance().player;
        Hand hand = player == null ? null : hand(player.getId());
        if (hand != null && hand.deaf) event.setSound(null);
    }
    @SubscribeEvent public static void signaturesInCourt(net.minecraftforge.client.event.RenderLevelStageEvent event) {
        if (event.getStage() != net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.options.hideGui || System.currentTimeMillis() - markersAt > 6000) return;
        var pose = event.getPoseStack(); var camera = event.getCamera(); var position = camera.getPosition();
        var buffer = mc.renderBuffers().bufferSource();
        for (Marker marker : MARKERS) {
            if (!marker.dimension.equals(mc.level.dimension().location().toString())) continue;
            String text = "✦ " + marker.name + (marker.debt > 0 ? " · debt " + marker.debt : " · signed");
            pose.pushPose(); pose.translate(marker.x - position.x, marker.y + 2.3 - position.y, marker.z - position.z);
            pose.mulPose(camera.rotation()); pose.scale(-0.025f, -0.025f, 0.025f);
            mc.font.drawInBatch(text, -mc.font.width(text) / 2f, 0, marker.debt > 0 ? 0xFFFFB66A : UiSkin.GOLD,
                    false, pose.last().pose(), buffer, net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, 0x550C0904, 0xF000F0);
            pose.popPose();
        }
        buffer.endBatch();
    }
    @SubscribeEvent public static void parchment(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.screen != null) return;
        String title = "Covenant Parchment", text = System.currentTimeMillis() < letterUntil ? letter : "";
        if (text.isEmpty() && mc.hitResult instanceof EntityHitResult hit && mc.player.distanceToSqr(hit.getEntity()) <= 64) {
            Hand hand = hand(hit.getEntity().getId());
            if (hand != null && !hand.constitutions.isEmpty()) {
                int page = (int) (System.currentTimeMillis() / 7000 % hand.constitutions.size());
                title = "Constitution " + (page + 1) + " / " + hand.constitutions.size(); text = hand.constitutions.get(page);
            }
        }
        if (text.isEmpty()) return;
        int width = Math.min(290, mc.getWindow().getGuiScaledWidth() - 20);
        var lines = mc.font.split(Component.literal(text), width - 28);
        int height = Math.min(mc.getWindow().getGuiScaledHeight() - 24, 30 + lines.size() * 10);
        int x = mc.getWindow().getGuiScaledWidth() - width - 10, y = 12;
        var g = event.getGuiGraphics();
        g.blit(CovenantEffects.PARCHMENT,x,y,width,height,0,0,32,48,32,48);
        g.drawString(mc.font, title, x + 12, y + 7, 0xFF59412A, false);
        g.fill(x+12,y+19,x+width-12,y+20,0xFFBBA071);
        int lineY = y + 23;
        for (var line : lines) {
            if (lineY + 9 > y + height - 4) break;
            g.drawString(mc.font, line, x + 12, lineY, 0xFF392C23, false); lineY += 10;
        }
    }
}
