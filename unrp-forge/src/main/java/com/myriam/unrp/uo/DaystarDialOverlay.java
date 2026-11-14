package com.myriam.unrp.uo;

import com.myriam.unrp.look.StatClientCache;
import com.myriam.unrp.look.UnrpHudOverlay;


import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class DaystarDialOverlay implements IGuiOverlay {
   public static final DaystarDialOverlay INSTANCE = new DaystarDialOverlay();
   static final int W = 136;
   static final int R = 24;
   static final int BAR_H = 6;
   static final int TALL = 62;
   private long lastDay = Long.MIN_VALUE;
   private long lastTick = Long.MIN_VALUE;
   private long still;

   static double angle(long dayTime) {
      long t = DaystarMath.dayClock(dayTime);
      return Math.PI + Math.PI * ((double)(t < 12000L ? t : t - 12000L) / 12000.0D);
   }

   static int lerp(int a, int b, double k) {
      double c = DaystarMath.clamp01(k);
      int r = (int)Math.round((double)(a >> 16 & 255) + (double)((b >> 16 & 255) - (a >> 16 & 255)) * c);
      int gg = (int)Math.round((double)(a >> 8 & 255) + (double)((b >> 8 & 255) - (a >> 8 & 255)) * c);
      int bl = (int)Math.round((double)(a & 255) + (double)((b & 255) - (a & 255)) * c);
      return -16777216 | r << 16 | gg << 8 | bl;
   }

   private static void arc(GuiGraphics g, int cx, int cy, int r, double a0, double a1, int argb, int thick) {
      int n = Math.max(2, (int)Math.ceil(Math.abs(a1 - a0) * (double)r * 1.5D));
      int lx = Integer.MIN_VALUE;
      int ly = Integer.MIN_VALUE;

      for(int i = 0; i <= n; ++i) {
         double a = a0 + (a1 - a0) * (double)i / (double)n;
         int px = (int)Math.round((double)cx + Math.cos(a) * (double)r);
         int py = (int)Math.round((double)cy + Math.sin(a) * (double)r);
         if (px != lx || py != ly) {
            lx = px;
            ly = py;
            g.fill(px, py, px + thick, py + thick, argb);
         }
      }

   }

   private static void disc(GuiGraphics g, int cx, int cy, double r, int argb) {
      int rr = (int)Math.ceil(r);

      for(int dy = -rr; dy <= rr; ++dy) {
         double h = r * r - (double)(dy * dy);
         if (!(h < 0.0D)) {
            int half = (int)Math.round(Math.sqrt(h));
            g.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, argb);
         }
      }

   }

   private static SunOrbEntity heldOrb(LocalPlayer p) {
      for(SunOrbEntity o : p.level().getEntitiesOfClass(SunOrbEntity.class, p.getBoundingBox().inflate(16.0D))) {
         if (o.ownerId() == p.getId() && o.state() == 0) {
            return o;
         }
      }

      return null;
   }

   public void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer p = mc.player;
      Level level = mc.level;
      if (p != null && level != null && !mc.options.hideGui) {
         int v = ClientDaystar.variant(p);
         if (v != 0) {
            boolean copy = v == 2;
            long now = level.getGameTime();
            long day = level.getDayTime();
            if (now != this.lastTick) {
               if (this.lastTick != Long.MIN_VALUE && now > this.lastTick && day == this.lastDay) {
                  this.still += now - this.lastTick;
               } else {
                  this.still = 0L;
               }

               this.lastDay = day;
               this.lastTick = now;
            }

            long t = DaystarMath.dayClock(day);
            double open = ClientDaystar.open(p);
            double gauge = ClientDaystar.gauge(p);
            boolean noon = ClientDaystar.state(p, v, "noon_state");
            boolean heat = ClientDaystar.state(p, v, "heat_state");
            boolean burn = ClientDaystar.state(p, v, "burnout_state");
            boolean fresh = DaystarHudState.fresh();
            boolean used = fresh && DaystarHudState.used();
            BlockPos head = BlockPos.containing(p.getX(), p.getEyeY(), p.getZ());
            boolean skyDim = level.dimensionType().hasSkyLight() && level.dimension() != Level.END && level.dimension() != Level.NETHER;
            boolean sky = skyDim && level.canSeeSky(head);
            boolean wet = sky && level.isRainingAt(head);
            boolean window = t >= 5000L && t <= 7000L;
            boolean ready = !copy && fresh && DaystarMath.noonOpen(day, sky, used);
            double pulse = 0.5D + 0.5D * Math.sin((double)((float)now + partialTick) * 0.18D);
            int x;
            int y;
            if (UnrpHudOverlay.panelShown()) {
               int top = Math.round(UnrpHudOverlay.panelY(height));
               x = Math.round(UnrpHudOverlay.panelX(width)) + 4;
               y = Math.round(UnrpHudOverlay.panelY(height) + UnrpHudOverlay.panelHeight()) + 14;
               if (y + 62 > height && top - 62 - 4 >= 0) {
                  y = top - 62 - 2;
               }
            } else {
               x = 8;
               y = height / 2;
            }

            x = Math.max(4, Math.min(x, width - 136 - 4));
            y = Math.max(4, Math.min(y, height - 62));
            SunOrbEntity orb = copy ? null : heldOrb(p);
            String line = "";
            int lineColor = -5720;
            if (orb != null) {
               if (orb.ember()) {
                  line = "EMBER: NO SUN TO DRAW ON";
                  lineColor = -6641976;
               } else {
                  line = "SUN " + String.format(Locale.ROOT, "%.1f", orb.size()) + " WIDE  " + Math.round(orb.charge() * 100.0F) + "%";
                  lineColor = (double)orb.charge() >= 0.6D ? -3656 : -11654;
               }
            } else if (noon) {
               line = (fresh ? "HIGH NOON " + DaystarMath.mmss((long)DaystarHudState.noonLeft(now)) : "HIGH NOON") + "  -95% HURT";
               lineColor = pulse > 0.5D ? -1 : -3656;
            } else if (burn) {
               line = fresh ? "BURNOUT " + DaystarMath.mmss((long)DaystarHudState.burnLeft(now)) : "BURNOUT";
               lineColor = -30128;
            } else if (ready) {
               if (StatClientCache.aura < 55) {
                  line = "HIGH NOON: 55 AURA";
                  lineColor = -4675448;
               } else {
                  line = "HIGH NOON READY";
                  lineColor = pulse > 0.5D ? -2352 : -10128;
               }
            } else if (!copy && fresh && window && !used && skyDim && !sky) {
               line = "HIGH NOON: OPEN SKY";
               lineColor = -4675448;
            } else if (DaystarMath.night(gauge)) {
               line = copy ? "NIGHT: LOW PROFILE" : "NIGHT: EMBER, HEAVY AXE";
               lineColor = -6641976;
            } else if (heat) {
               line = "HEAT  3 BLOCKS";
               lineColor = -20368;
            } else if (p.isShiftKeyDown() && DaystarMath.heatOn(open)) {
               line = "HEAT HELD IN";
               lineColor = -3627928;
            } else if (p.isInWater() && DaystarMath.heatOn(gauge)) {
               line = "HEAT DOUSED";
               lineColor = -7358264;
            }

            int cx = x + 68;
            int cy = y + 26;
            int by = cy + 4;
            int bottom = line.isEmpty() ? by + 6 + 13 : by + 6 + 23;
            g.fill(x - 2, y - 2, x + 136 + 2, bottom, -1207039482);
            arc(g, cx, cy, 24, Math.PI, (Math.PI * 2D), 1509938828, 1);
            g.fill(x + 2, cy, x + 136 - 2, cy + 1, -2130712898);

            for(int k = 0; k <= 4; ++k) {
               double a = Math.PI + (double)k * Math.PI / 4.0D;
               int tx = (int)Math.round((double)cx + Math.cos(a) * 26.0D);
               int tyy = (int)Math.round((double)cy + Math.sin(a) * 26.0D);
               g.fill(tx, tyy, tx + 1, tyy + 1, -1711286644);
            }

            if (!copy && (noon || ready || window && used)) {
               double a0 = 4.4505895925855405D;
               double a1 = 4.974188368183839D;
               arc(g, cx, cy, 24, a0, a1, !noon && !ready ? -1870630816 : -218107734, 2);
            }

            double ang = angle(day);
            int sx = (int)Math.round((double)cx + Math.cos(ang) * 24.0D);
            int sy = (int)Math.round((double)cy + Math.sin(ang) * 24.0D);
            if (t < 12000L) {
               double glow = 0.35D + 0.65D * gauge;
               int alpha = (int)Math.round(89.25D * glow * (noon ? 0.75D + 0.25D * pulse : 1.0D));
               disc(g, sx, sy, 6.0D + 3.0D * gauge + (noon ? 2.0D * pulse : 0.0D), alpha << 24 | 16762960);
               g.fill(sx - 3, sy - 3, sx + 3, sy + 3, -9872);
               g.fill(sx - 2, sy - 2, sx + 2, sy + 2, -2344);
            } else {
               g.fill(sx - 3, sy - 3, sx + 3, sy + 3, -3550492);
               g.fill(sx - 1, sy - 2, sx + 1, sy, -6641985);
            }

            g.fill(x - 1, by - 1, x + 136 + 1, by + 6 + 1, !(gauge >= 0.999D) && !noon ? (burn ? -8771050 : -9745892) : -3656);
            g.fill(x, by, x + 136, by + 6, -14937594);
            int fw = (int)Math.round(136.0D * DaystarMath.clamp01(gauge));
            int ow = (int)Math.round(136.0D * DaystarMath.clamp01(open));
            if (ow > fw) {
               g.fill(x + fw, by, x + ow, by + 6, 1623762024);
            }

            for(int i = 0; i < fw; i += 2) {
               g.fill(x + i, by, x + Math.min(fw, i + 2), by + 6, lerp(13123610, 16769146, (double)i / 136.0D));
            }

            g.fill(x, by, x + fw, by + 1, -419432746);

            for(int q = 1; q <= 3; ++q) {
               int qx = x + Math.round((float)(136 * q) / 4.0F);
               g.fill(qx, by, qx + 1, by + 6, -1727133178);
            }

            g.drawString(mc.font, "SUN " + Math.round(gauge * 100.0D) + "%", x, by + 6 + 3, -10080, true);
            String note;
            if (level.dimension() == Level.NETHER) {
               note = "NETHER";
            } else if (level.dimension() == Level.END) {
               note = "THE END";
            } else if (!level.dimensionType().hasSkyLight()) {
               note = "NO SKY";
            } else if (!sky) {
               note = "UNDER COVER";
            } else if (wet && level.isThundering()) {
               note = "THUNDER";
            } else if (wet) {
               note = "RAIN";
            } else if (this.still >= 40L) {
               note = "CLOCK STOPPED";
            } else {
               note = DaystarMath.clockText(day);
            }

            g.drawString(mc.font, note, x + 136 - mc.font.width(note), by + 6 + 3, -3627928, true);
            if (!line.isEmpty()) {
               g.drawString(mc.font, line, x, by + 6 + 13, lineColor, true);
            }

         }
      }
   }
}
