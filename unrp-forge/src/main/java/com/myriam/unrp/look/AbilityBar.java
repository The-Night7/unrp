package com.myriam.unrp.look;

import com.mojang.blaze3d.systems.RenderSystem;
import com.myriam.unrp.client.ClientState;
import com.myriam.unrp.net.PlayerStatePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.threetag.palladium.PalladiumConfig;
import net.threetag.palladium.client.PalladiumKeyMappings;
import net.threetag.palladium.client.screen.AbilityBarRenderer;

import java.util.ArrayList;
import java.util.List;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "unrp", value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class AbilityBar implements IGuiOverlay {
    public static final AbilityBar INSTANCE = new AbilityBar();
    static final ResourceLocation TEXTURE = new ResourceLocation("unrp", "textures/gui/ability_bars/unrp_powers.png");
    static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};

    static int[][] lists = new int[0][];

    static int[] listPower = new int[0];
    static int selected = 0;
    private static final boolean[] down = new boolean[5];
    private static boolean switchDown;

    private AbilityBar() {}

    static boolean locked(PlayerStatePacket s, int e) {
        return e >= 0 && (e >= s.barLocked.length || s.barLocked[e]);
    }

    static void rebuild(PlayerStatePacket s) {
        List<int[]> out = new ArrayList<>();
        List<Integer> owners = new ArrayList<>();
        for (int pi = 0; pi < s.powName.length; pi++) {
            if (s.powName[pi] == null || s.powName[pi].isEmpty()) continue;
            int maxSlot = 0;
            for (int k = 0; k < s.barSlot.length; k++) if (s.barPower[k] == pi) maxSlot = Math.max(maxSlot, s.barSlot[k]);
            int n = maxSlot / 5 + 1;
            for (int l = 0; l < n; l++) {
                int[] shown = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
                boolean any = false, anyUnlocked = false;
                for (int i = 0; i < 5; i++) {
                    int slot = l * 5 + i;
                    if (slot == 0) {
                        shown[i] = -1;
                        any = anyUnlocked = true;
                        continue;
                    }
                    for (int k = 0; k < s.barSlot.length; k++) {
                        if (s.barPower[k] != pi || s.barSlot[k] != slot) continue;
                        if (shown[i] == Integer.MIN_VALUE || (locked(s, shown[i]) && !locked(s, k))) shown[i] = k;
                    }
                    if (shown[i] != Integer.MIN_VALUE) {
                        any = true;
                        if (!locked(s, shown[i])) anyUnlocked = true;
                    }
                }
                if (any && anyUnlocked) {
                    out.add(shown);
                    owners.add(pi);
                }
            }
        }
        lists = out.toArray(new int[0][]);
        listPower = owners.stream().mapToInt(Integer::intValue).toArray();
        if (selected >= lists.length) selected = Math.max(0, lists.length - 1);
    }

    static boolean simple(PlayerStatePacket s) {
        if (lists.length > 1) return false;
        int unlocked = 0;
        for (int[] l : lists) for (int e : l) if (e != Integer.MIN_VALUE && !locked(s, e)) unlocked++;
        return unlocked == 1;
    }

    // ------------------------------------------------------------------ touches
    static void send(int action) {
        com.myriam.unrp.net.Net.CHANNEL.sendToServer(new com.myriam.unrp.net.UseAbilityPacket(action));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        PlayerStatePacket s = ClientState.get();
        rebuild(s);
        boolean free = mc.player != null && mc.screen == null;
        KeyMapping sw = PalladiumKeyMappings.SWITCH_ABILITY_LIST;
        boolean sd = free && sw != null && sw.isDown();
        if (sd && !switchDown && lists.length > 0) {
            boolean back = mc.player.isCrouching();
            int next = selected + (back ? -1 : 1);
            if (next >= lists.length) next = 0;
            else if (next < 0) next = lists.length - 1;
            selected = next;
        }
        switchDown = sd;
        int sel = Math.min(selected, Math.max(0, lists.length - 1));
        int[] list = lists.length == 0 ? null : lists[sel];
        int base = lists.length == 0 ? 0 : (listPower[sel] + 1) * 1000;
        for (int i = 0; i < 5; i++) {
            KeyMapping k = PalladiumKeyMappings.ABILITY_KEYS == null || i >= PalladiumKeyMappings.ABILITY_KEYS.length ? null : PalladiumKeyMappings.ABILITY_KEYS[i];
            boolean d = free && k != null && k.isDown();
            if (list != null && list[i] != Integer.MIN_VALUE) {
                int e = list[i];
                if (d && !down[i]) send(base + (e < 0 ? 0 : com.myriam.unrp.ability.AbilityInputProtocol.press(s.barMove[e])));
                else if (!d && down[i] && e >= 0) send(base + com.myriam.unrp.ability.AbilityInputProtocol.release(s.barMove[e]));
            }
            down[i] = d;
        }
    }

    // ------------------------------------------------------------------ rendering (AbilityBarRenderer)
    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        PlayerStatePacket s = ClientState.get();
        if (mc.player == null || mc.options.hideGui || lists.length == 0) return;
        AbilityBarRenderer.Position position = PalladiumConfig.Client.ABILITY_BAR_POSITION.get();
        if (position == AbilityBarRenderer.Position.HIDDEN) return;
        boolean top = position == AbilityBarRenderer.Position.TOP_LEFT || position == AbilityBarRenderer.Position.TOP_RIGHT;
        boolean left = position == AbilityBarRenderer.Position.TOP_LEFT || position == AbilityBarRenderer.Position.BOTTOM_LEFT;
        if (top && mc.options.renderDebug) return;
        if (!top && mc.screen instanceof ChatScreen) top = true;
        int sel = Math.min(selected, lists.length - 1);
        int[] list = lists[sel];
        int pw = listPower[sel];
        boolean simple = simple(s);
        var ps = g.pose();
        int indicatorWidth = 52, indicatorHeight = 28;
        if (!simple) {
            ps.pushPose();
            if (!top) ps.translate(0, height - indicatorHeight, 0);
            if (!left) ps.translate(width - indicatorWidth, 0, 0);
            boolean showKey = lists.length > 1;
            RenderSystem.setShaderColor(1, 1, 1, 1);
            g.blit(TEXTURE, 0, 0, left ? 52 : 0, top ? 28 : 0, 52, 28);
            icon(g, s.powIcon[pw], showKey ? (left ? 30 : 6) : (left ? 17 : 19), top ? 5 : 7);
            if (showKey) {
                FormattedText key = mc.font.substrByWidth(PalladiumKeyMappings.SWITCH_ABILITY_LIST.getTranslatedKeyMessage(), 10);
                int length = mc.font.width(key) + 10;
                g.drawString(mc.font, Component.literal(key.getString()), (int) ((left ? 15 : 37) - length / 2.0F + 10.0F), top ? 10 : 12, -1, false);
                g.blit(TEXTURE, (left ? 15 : 37) - length / 2, top ? 9 : 11, 78, mc.player.isCrouching() ? 64 : 56, 8, 8);
            }
            ps.popPose();
        }
        ps.pushPose();
        int abilitiesWidth = 24, abilitiesHeight = 112;
        if (!simple) {
            if (top) ps.translate(!left ? width - abilitiesWidth : 0, indicatorHeight - 1, 0);
            else ps.translate(!left ? width - abilitiesWidth : 0, height - indicatorHeight - abilitiesHeight + 1, 0);
        } else if (top) ps.translate(!left ? width - abilitiesWidth : 0, 0, 0);
        else ps.translate(!left ? width - abilitiesWidth : 0, height - 24, 0);
        boolean showName = mc.screen instanceof ChatScreen || PalladiumConfig.Client.ALWAYS_SHOW_ABILITY_NAMES.get();
        int count = simple ? 1 : 5;
        int[] shown = list;
        if (simple) for (int e : list) if (e != Integer.MIN_VALUE && !locked(s, e)) shown = new int[] {e, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        for (int i = 0; i < count; i++) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            g.blit(TEXTURE, 3, i * 22 + 3, 60, 56, 18, 18);
            int e = shown[i];
            if (e == Integer.MIN_VALUE) continue;
            boolean on = e < 0 ? s.powOn[pw] : e < s.barOn.length && s.barOn[e];
            boolean unlocked = !locked(s, e);
            g.blit(TEXTURE, 3, i * 22 + 3, on ? 42 : 24, unlocked ? 56 : 74, 18, 18);
            if (e >= 0 && e < s.barLeft.length && s.barLeft[e] > 0 && s.barCd[e] > 0) {
                int w = (int) ((float) s.barLeft[e] / s.barCd[e] * 18.0F);
                g.blit(TEXTURE, 3, i * 22 + 3, 60, 74, Math.min(18, w), 18);
            }
            if (!unlocked) g.blit(TEXTURE, 3, i * 22 + 3, 42, 74, 18, 18);
            else icon(g, e < 0 ? s.powIcon[pw] : s.barIcon[e], 4, 4 + i * 22);
            if (showName) {
                String name = e < 0 ? com.myriam.unrp.am.PowerNames.display(s.powName[pw]) : s.barName[e];
                int nw = mc.font.width(name);
                g.fill(left ? 24 : -nw - 10, i * 22 + 5, (left ? 24 : -nw - 10) + 10 + nw, i * 22 + 19, 0x80000000);
                g.drawString(mc.font, name, left ? 29 : -nw - 5, i * 22 + 8, -1, false);
            }
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (!simple) g.blit(TEXTURE, 0, 0, 0, 56, 24, 112);
        else g.blit(TEXTURE, 0, 0, 0, 168, 24, 24);
        for (int i = 0; i < count; i++) {
            int e = shown[i];
            if (e == Integer.MIN_VALUE) continue;
            boolean unlocked = !locked(s, e);
            if (!unlocked) g.blit(TEXTURE, 3, i * 22 + 3, 42, 74, 18, 18);
            int[] c = color(e < 0 ? s.powColor[pw] : s.barColor[e]);
            g.blit(TEXTURE, 0, i * 22, c[0], c[1], 24, 24);
            if (unlocked && PalladiumKeyMappings.ABILITY_KEYS != null && i < PalladiumKeyMappings.ABILITY_KEYS.length) {
                ps.pushPose();
                ps.translate(0, 0, 200);
                Component keyName = PalladiumKeyMappings.ABILITY_KEYS[i].getTranslatedKeyMessage();
                g.drawString(mc.font, keyName, 22 - mc.font.width(keyName), 5 + i * 22 + 7, 0xFFFFFF, false);
                ps.popPose();
            }
        }
        ps.popPose();
    }

    static int[] color(String name) {
        int i = 0;
        for (int k = 0; k < COLORS.length; k++) if (COLORS[k].equalsIgnoreCase(name == null ? "" : name)) i = k;
        return i < 8 ? new int[] {104, i * 24} : new int[] {128, (i - 8) * 24};
    }

    static void icon(GuiGraphics g, String tex, int x, int y) {
        if (tex == null || tex.isEmpty()) return;
        ResourceLocation rl = ResourceLocation.tryParse(tex);
        if (rl == null) return;
        RenderSystem.enableBlend();
        g.blit(rl, x, y, 0, 0, 16, 16, 16, 16);
    }
}
