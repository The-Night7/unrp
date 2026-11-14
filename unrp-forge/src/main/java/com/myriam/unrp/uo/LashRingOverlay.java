package com.myriam.unrp.uo;

import com.myriam.unrp.pass.UnrpPowerLayers;


import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class LashRingOverlay implements IGuiOverlay {
   public static final LashRingOverlay INSTANCE = new LashRingOverlay();
   static final int R = 11;
   static final int THICK = 3;
   static final int TRACK = -1945499634;
   static final int LOW = 2761280;
   static final int HIGH = 10253567;
   static final int HOT = 16734858;
   static final int TEAL = -10885390;
   static final int PIP_W = 10;
   static final int PIP_GAP = 3;
   static final int UNDER_INDICATOR = 10;
   static final int LINE = 11;
   static final int NOTCH_OFF = -12954272;
   static final int STORM_SLOT = 5;
   static final int HOLD_TEXT = 9433078;

   static int lerp(int a, int b, double k) {
      double c = LashMath.clamp01(k);
      int r = (int)Math.round((double)(a >> 16 & 255) + (double)((b >> 16 & 255) - (a >> 16 & 255)) * c);
      int gg = (int)Math.round((double)(a >> 8 & 255) + (double)((b >> 8 & 255) - (a >> 8 & 255)) * c);
      int bl = (int)Math.round((double)(a & 255) + (double)((b & 255) - (a & 255)) * c);
      return -16777216 | r << 16 | gg << 8 | bl;
   }

   static int ringColor(double s, boolean frenzy, boolean rest, long tick) {
      if (frenzy) {
         return tick / 2L % 2L == 1L ? -46486 : -11556;
      } else if (rest) {
         return -10856346;
      } else {
         return s > 0.75D ? lerp(10253567, 16734858, (s - 0.75D) / 0.25D) : lerp(2761280, 10253567, Math.min(1.0D, s * 1.3D));
      }
   }

   static double ringFill(double s, boolean rest, double restLeft) {
      return rest ? 1.0D - restLeft : s;
   }

   static int pulse(long tick, float partialTick) {
      return lerp(-10885390, -1, 0.5D + 0.5D * Math.sin((double)((float)tick + partialTick) * 0.25D));
   }

   private static void notch(GuiGraphics g, int cx, int cy, int argb) {
      g.fill(cx - 1, cy + 11 - 3, cx + 1, cy + 11 + 3, argb);
   }

   static String windLine(int wind, int left, boolean used) {
      if (wind == 3) {
         return "SECOND WIND " + LashMath.mmss(left);
      } else if (wind == 4 && left > 0) {
         return "SPENT: WEAK, SLOW, BLIND";
      } else {
         return used && wind == 0 ? "SECOND WIND USED TODAY" : null;
      }
   }

   static String holdLine(int held, int left) {
      return held <= 0 ? null : "HOLDING " + held + "  " + LashMath.mmss(left);
   }

   static int windColor(int wind) {
      if (wind == 3) {
         return 16747176;
      } else {
         return wind == 4 ? 10132136 : 9078936;
      }
   }

   private static void arc(GuiGraphics g, int cx, int cy, double a0, double a1, int argb) {
      int n = Math.max(2, (int)Math.ceil(Math.abs(a1 - a0) * 11.0D * 1.6D));
      int lx = Integer.MIN_VALUE;
      int ly = Integer.MIN_VALUE;
      int half = 1;

      for(int i = 0; i <= n; ++i) {
         double a = a0 + (a1 - a0) * (double)i / (double)n;
         int px = (int)Math.round((double)cx + Math.cos(a) * 11.0D) - half;
         int py = (int)Math.round((double)cy + Math.sin(a) * 11.0D) - half;
         if (px != lx || py != ly) {
            lx = px;
            ly = py;
            g.fill(px, py, px + 3, py + 3, argb);
         }
      }

   }

   private static void centred(GuiGraphics g, Minecraft mc, String text, int cx, int y, int rgb, float scale) {
      g.pose().pushPose();

      try {
         g.pose().translate((float)cx, (float)y, 0.0F);
         g.pose().scale(scale, scale, 1.0F);
         int w = mc.font.width(text);
         g.drawString(mc.font, text, -w / 2, 0, -16777216 | rgb, true);
      } finally {
         g.pose().popPose();
      }

   }

   public void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
      if (LashHudState.fresh()) {
         Minecraft mc = Minecraft.getInstance();
         LocalPlayer p = mc.player;
         if (p != null && mc.level != null && !mc.options.hideGui && !mc.options.renderDebug && !p.isSpectator()) {
            if (mc.options.getCameraType().isFirstPerson()) {
               int variant = UnrpPowerLayers.powerVariant(p, "black_lash");
               if (variant != 0) {
                  if (LashHudState.wind() != 1 && !LashDownedScreen.showing()) {
                     long now = mc.level.getGameTime();
                     if (!LashDownedScreen.titleShowing(now)) {
                        int cx = width / 2;
                        int cy = height / 2;
                        int dy = mc.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR ? 10 : 0;
                        if (variant != 1) {
                           boolean sneakCopy = p.isShiftKeyDown();
                           pips(g, cx, cy + 16 + dy, new double[]{LashHudState.cooldown(sneakCopy ? 1 : 0), LashHudState.cooldown(2)});
                        } else {
                           double s = LashMath.clamp01((double)LashHudState.strain() / 100.0D);
                           boolean frenzy = LashHudState.frenzyLeft(now) > 0;
                           boolean rest = !frenzy && LashHudState.restLeft(now) > 0;
                           double restLeft = (double)LashHudState.restLeft(now) / 200.0D;
                           arc(g, cx, cy, 0.0D, (Math.PI * 2D), -1945499634);
                           double fill = ringFill(frenzy ? 1.0D : s, rest, restLeft);
                           if (fill > 0.0D) {
                              arc(g, cx, cy, (-Math.PI / 2D), (-Math.PI / 2D) + (Math.PI * 2D) * fill, ringColor(s, frenzy, rest, now));
                           }

                           boolean open = LashHudState.flag(128);
                           boolean ready = LashMath.stormReady((double)LashHudState.strain(), open, !frenzy && !rest && !LashHudState.flag(2), LashHudState.cooldown(5) <= 0.0D);
                           if (open && !frenzy && !rest) {
                              notch(g, cx, cy, ready ? pulse(now, partialTick) : -12954272);
                           }

                           if (frenzy) {
                              centred(g, mc, "FRENZY", cx, cy + 16 + dy, 16738952, 1.0F);
                           } else if (rest) {
                              centred(g, mc, "RESTING", cx, cy + 16 + dy, 9078936, 1.0F);
                           } else {
                              centred(g, mc, String.valueOf(LashHudState.strain()), cx, cy + 16 + dy, 12101887, 0.85F);
                           }

                           int below = 27;
                           if (ready) {
                              centred(g, mc, "STORM READY", cx, cy + below + dy, pulse(now, partialTick) & 16777215, 0.85F);
                              below += 11;
                           }

                           String line = windLine(LashHudState.wind(), LashHudState.windLeft(now), LashHudState.flag(64));
                           if (line != null) {
                              centred(g, mc, line, cx, cy + below + dy, windColor(LashHudState.wind()), 1.0F);
                              below += 11;
                           }

                           String hold = holdLine(LashHudState.held(), LashHudState.holdLeft(now));
                           if (hold != null) {
                              centred(g, mc, hold, cx, cy + below + dy, 9433078, 0.85F);
                              below += 11;
                           }

                           boolean sneak = p.isShiftKeyDown();
                           double[] pips = new double[]{LashHudState.cooldown(sneak ? 1 : 0), LashHudState.cooldown(2), LashHudState.cooldown(sneak ? 4 : 3)};
                           if (open) {
                              pips = new double[]{pips[0], pips[1], pips[2], LashHudState.cooldown(5)};
                           }

                           pips(g, cx, cy + below + dy, pips);
                        }
                     }
                  }
               }
            }
         }
      }
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
            g.fill(x - 1, y - 1, x + 10 + 1, y + 3, -1609561064);
            int lit = (int)Math.round(10.0D * (1.0D - pips[i]));
            if (pips[i] <= 0.0D) {
               g.fill(x, y, x + 10, y + 2, -1873156366);
            } else if (lit > 0) {
               g.fill(x, y, x + lit, y + 2, -10885390);
            }
         }

      }
   }
}
