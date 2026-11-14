package com.myriam.unrp.uo;

import com.myriam.unrp.look.UnrpHudOverlay;
import com.myriam.unrp.pass.UnrpPowerLayers;


import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.threetag.palladium.client.PalladiumKeyMappings;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public class SaltHudOverlay implements IGuiOverlay {
   public static final SaltHudOverlay INSTANCE = new SaltHudOverlay();
   static final int PIP_W = 10;
   static final int PIP_GAP = 3;
   static final int LIT = -724502;
   static final int READY = -1863265662;
   static final int W = 104;
   static final int BAR_H = 6;
   static final int TALL = 34;
   static final int BOSS_W = 182;
   static final int BOSS_Y = 14;
   static final int BOSS_TITLE = 10;
   static final int BOSS_TALL = 18;
   static final int PANEL_GAP = 12;
   static final int BOX_PAD = 2;
   private static int bossNext;

   @SubscribeEvent
   public static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
      bossNext = Math.max(bossNext, event.getY() + event.getIncrement());
   }

   static int bossY(int width, int height, int vanillaNext) {
      int y = Math.max(14, vanillaNext + 2);
      if (UnrpHudOverlay.panelShown()) {
         float px = UnrpHudOverlay.panelX(width);
         float py = UnrpHudOverlay.panelY(height);
         float pw = UnrpHudOverlay.panelWidth();
         float ph = UnrpHudOverlay.panelHeight();
         int x0 = width / 2 - 91 - 2;
         int x1 = width / 2 + 91 + 2;
         if (px < (float)x1 && px + pw > (float)x0 && py < (float)(y + 18) && py + ph > (float)(y - 10 - 1)) {
            y = Math.max(y, Math.round(py + ph) + 12);
         }
      }

      return y;
   }

   static String wakeLine(int left) {
      return "SERAPH WAKE " + Math.max(0, (int)Math.ceil((double)left / 20.0D)) + "s";
   }

   static String trumpetLine(boolean sounded, String key) {
      return sounded ? "TRUMPET SOUNDED" : "LAST TRUMPET: PRESS " + key;
   }

   static String wakeKey() {
      try {
         for(PalladiumKeyMappings.AbilityKeyMapping k : PalladiumKeyMappings.ABILITY_KEYS) {
            if (k != null && k.index == 4) {
               return k.getTranslatedKeyMessage().getString().toUpperCase(Locale.ROOT);
            }
         }

         return "SLOT 4";
      } catch (Throwable var4) {
         return "SLOT 4";
      }
   }

   public void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
      if (SaltHudState.fresh()) {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer p = mc.player;
         if (p != null && mc.level != null && !mc.options.hideGui && !mc.options.renderDebug && !p.isSpectator()) {
            double now = (double)((float)mc.level.getGameTime() + partialTick);
            boolean erupting = SaltHudState.flag(4);
            int vanillaNext = bossNext;
            bossNext = 0;
            if (!erupting && !SaltHudState.flag(8)) {
               int variant = UnrpPowerLayers.powerVariant(p, "salt_seraph");
               if (variant != 0) {
                  boolean sneak = p.isShiftKeyDown();
                  boolean waking = SaltHudState.flag(1);
                  double[] pips = variant == 2 ? new double[]{SaltHudState.cooldown(0, now)} : new double[]{SaltHudState.cooldown(sneak ? 1 : 0, now), SaltHudState.cooldown(2, now), SaltHudState.cooldown(sneak ? 4 : 3, now), waking ? 0.0D : SaltHudState.cooldown(5, now)};
                  if (mc.options.getCameraType().isFirstPerson()) {
                     pips(g, width / 2, height / 2 + 16, pips);
                  }

                  int left = SaltHudState.wakeLeft(now);
                  if (waking && left > 0) {
                     meter(g, mc, width, height, left);
                  }

               }
            } else {
               boss(g, mc, p, width, bossY(width, height, vanillaNext), erupting ? 6000 : SaltHudState.wakeLeft(now));
            }
         }
      }
   }

   static String bossLine(int left) {
      return "INSTINCT HAS THE BODY  " + SaltMath.mmss(left);
   }

   private static void boss(GuiGraphics g, Minecraft mc, LocalPlayer p, int width, int y, int left) {
      int x = width / 2 - 91;
      String title = "UNBOUND";
      g.drawString(mc.font, title, width / 2 - mc.font.width(title) / 2, y - 10, -44960, true);
      float k = Math.max(0.0F, Math.min(1.0F, p.getHealth() / Math.max(1.0F, p.getMaxHealth())));
      int fw = Math.round(182.0F * k);
      g.fill(x, y, x + 182, y + 5, -12973552);
      g.fill(x, y, x + fw, y + 5, -3135428);
      g.fill(x, y, x + fw, y + 1, -32624);

      for(int i = 1; i < 10; ++i) {
         int tx = x + Math.round((float)(182 * i) / 10.0F);
         g.fill(tx, y + 1, tx + 1, y + 4, Integer.MIN_VALUE);
      }

      String line = bossLine(left);
      g.drawString(mc.font, line, width / 2 - mc.font.width(line) / 2, y + 9, -1531736, true);
   }

   private static void pips(GuiGraphics g, int cx, int y, double[] pips) {
      boolean any = false;

      for(double v : pips) {
         any |= v > 0.0D;
      }

      if (any) {
         int n = pips.length;
         int total = 10 * n + 3 * (n - 1);
         int x0 = cx - total / 2;

         for(int i = 0; i < n; ++i) {
            int x = x0 + i * 13;
            g.fill(x - 1, y - 1, x + 10 + 1, y + 3, -1609295840);
            int lit = (int)Math.round(10.0D * (1.0D - pips[i]));
            if (pips[i] <= 0.0D) {
               g.fill(x, y, x + 10, y + 2, -1863265662);
            } else if (lit > 0) {
               g.fill(x, y, x + lit, y + 2, -724502);
            }
         }

      }
   }

   private static void meter(GuiGraphics g, Minecraft mc, int width, int height, int left) {
      int x;
      int y;
      if (UnrpHudOverlay.panelShown()) {
         int top = Math.round(UnrpHudOverlay.panelY(height));
         x = Math.round(UnrpHudOverlay.panelX(width)) + 4;
         y = Math.round(UnrpHudOverlay.panelY(height) + UnrpHudOverlay.panelHeight()) + 14;
         if (y + 34 > height && top - 34 - 4 >= 0) {
            y = top - 34 - 2;
         }
      } else {
         x = 8;
         y = height / 2;
      }

      boolean sounded = SaltHudState.flag(2);
      String wake = wakeLine(left);
      String trumpet = trumpetLine(sounded, wakeKey());
      int box = Math.max(104, Math.max(mc.font.width(wake), mc.font.width(trumpet)));
      x = Math.max(4, Math.min(x, width - box - 4));
      y = Math.max(4, Math.min(y, height - 34));
      double f = Math.max(0.0D, Math.min(1.0D, (double)left / 1200.0D));
      g.fill(x - 2, y - 2, x + box + 2, y + 6 + 26, -1291187184);
      g.fill(x - 1, y - 1, x + 104 + 1, y + 6 + 1, -661840);
      g.fill(x, y, x + 104, y + 6, -15000542);
      int fw = (int)Math.round(104.0D * f);
      g.fill(x, y, x + fw, y + 6, -2340);
      g.fill(x, y, x + fw, y + 1, -1);
      g.drawString(mc.font, wake, x, y + 6 + 3, -3640, true);
      g.drawString(mc.font, trumpet, x, y + 6 + 13, sounded ? -6641992 : -8054, true);
   }
}
