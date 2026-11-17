package com.myriam.unrp.client;

import com.myriam.unrp.UnrpMod;
import com.myriam.unrp.net.Net;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class UnrpClient implements ClientModInitializer {
    private static KeyBinding useKey;

    @Override
    public void onInitializeClient() {
        useKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.unrp.use", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.unrp"));

        ClientPlayNetworking.registerGlobalReceiver(Net.SYNC, (client, handler, buf, sender) -> {
            String power = buf.readString();
            int aura = buf.readInt();
            int maxAura = buf.readInt();
            int[] stats = new int[5];
            for (int i = 0; i < 5; i++) stats[i] = buf.readInt();
            client.execute(() -> {
                ClientData.power = power;
                ClientData.aura = aura;
                ClientData.maxAura = maxAura;
                System.arraycopy(stats, 0, ClientData.stats, 0, 5);
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (useKey.wasPressed()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(Net.USE, PacketByteBufs.create());
                }
            }
        });

        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            if (ClientData.power.isEmpty()) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options.hudHidden || mc.player == null) return;

            int x = ctx.getScaledWindowWidth() / 2 - 91;
            int y = ctx.getScaledWindowHeight() - 54;
            int width = 182, height = 5;
            float ratio = ClientData.maxAura <= 0 ? 0f : Math.min(1f, (float) ClientData.aura / ClientData.maxAura);

            ctx.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xAA000000);
            ctx.fill(x, y, x + (int) (width * ratio), y + height, 0xFF8A4DFF);
            Text label = Text.translatable("power." + UnrpMod.MOD_ID + "." + ClientData.power)
                    .append(Text.literal("  " + ClientData.aura + "/" + ClientData.maxAura));
            ctx.drawTextWithShadow(mc.textRenderer, label, x, y - 11, 0xFFFFFF);
        });
    }
}
