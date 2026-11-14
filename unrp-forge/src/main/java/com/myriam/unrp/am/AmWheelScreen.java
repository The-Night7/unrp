package com.myriam.unrp.am;

import com.myriam.unrp.pass.AmRoster;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

public class AmWheelScreen extends Screen {
   private static final int GOLD = 15250250;
   private static final int VIOLET = 10115816;
   private static final int BRONZE = 9071420;
   private static final int NIGHT = 722968;
   private static final int EMBER = 12077116;
   private static final int ASH = 3816002;
   private static final int COPY_COST = 65;
   private static final float SLOT_R = 12.0F;
   private static final float SPACING = 30.0F;
   private static final float HUB_R = 22.0F;
   private static final int[] PENTA = new int[]{0, 2, 4, 7, 9, 12, 14, 16, 19, 21};
   private static final ResourceLocation[] SLOT_TEX = new ResourceLocation[55];
   private static final ResourceLocation[] COMBO_TEX = new ResourceLocation[AmRoster.COMBO_ICONS.length];
   private static final ResourceLocation HUB_TEX = new ResourceLocation("unrp", "textures/icons/aura_manipulation_power_icon.png");
   private static volatile AmWheelPacket latest;
   private final List<AmWheelScreen.View> views = new ArrayList();
   private final List<AmWheelScreen.Mote> motes = new ArrayList();
   private final Random rng = new Random();
   private boolean comboPage;
   private boolean synced;
   private boolean flipping;
   private int page;
   private int pages = 1;
   private int cx;
   private int cy;
   private float ring;
   private float inner;
   private int poll;
   private int closing = -1;
   private float clock;
   private long lastNanos;
   private float shock = -1.0F;
   private int shockRgb = 15250250;
   private AmWheelScreen.View hovered;
   private int lastHoverKey = -1;
   private String shownKey = "";
   private Button tabCopy;
   private Button tabCombo;
   private Button release;
   private Button prev;
   private Button next;

   public AmWheelScreen() {
      super(Component.literal("The Copy Wheel"));
   }

   public static void accept(AmWheelPacket msg) {
      latest = msg;
      Minecraft mc = Minecraft.getInstance();
      if (!msg.open || mc.screen instanceof AmWheelScreen || mc.screen != null && !mc.screen.getClass().getName().startsWith("net.threetag.palladium")) {
         Screen var3 = mc.screen;
         if (var3 instanceof AmWheelScreen) {
            AmWheelScreen wheel = (AmWheelScreen)var3;
            wheel.sync(msg);
         }

      } else {
         mc.setScreen(new AmWheelScreen());
      }
   }

   private String dataKey(AmWheelPacket msg) {
      StringBuilder key = new StringBuilder();
      key.append(msg.copiedCount).append('|');

      for(int[] entry : msg.nearby) {
         key.append(entry[0]).append(':').append(entry[1]).append(',');
      }

      key.append('|');

      for(int i = 0; i < msg.comboReady.length; ++i) {
         key.append((char)(msg.comboReady[i] ? 'r' : '.')).append((char)(msg.comboHeld[i] ? 'h' : '.'));
      }

      return key.toString();
   }

   protected void init() {
      this.cx = this.width / 2;
      this.cy = this.height / 2 - 10;
      this.ring = Mth.clamp(Math.min((float)this.height / 2.0F - 66.0F, (float)this.width / 2.0F - 40.0F), 50.0F, 96.0F);
      this.inner = this.ring - 36.0F >= 42.0F ? this.ring - 36.0F : 0.0F;
      int rowY = (int)((float)this.cy + this.ring + 58.0F);
      this.tabCopy = (Button)this.addRenderableWidget(Button.builder(Component.literal("Copy"), (b) -> this.flipTo(false)).bounds(this.cx - 98, rowY, 56, 16).build());
      this.tabCombo = (Button)this.addRenderableWidget(Button.builder(Component.literal("Combine"), (b) -> this.flipTo(true)).bounds(this.cx - 38, rowY, 64, 16).build());
      this.release = (Button)this.addRenderableWidget(Button.builder(Component.literal("Release All"), (b) -> this.releaseAll()).bounds(this.cx + 30, rowY, 68, 16).build());
      int arrowY = (int)((float)this.cy + 22.0F + 6.0F);
      this.prev = (Button)this.addRenderableWidget(Button.builder(Component.literal("<"), (b) -> this.turn(-1)).bounds(this.cx - 27, arrowY, 14, 14).build());
      this.next = (Button)this.addRenderableWidget(Button.builder(Component.literal(">"), (b) -> this.turn(1)).bounds(this.cx + 13, arrowY, 14, 14).build());
      if (latest != null) {
         this.sync(latest);
      }

   }

   private void sync(AmWheelPacket msg) {
      this.shownKey = this.dataKey(msg);
      List<int[]> want = new ArrayList();
      if (this.comboPage) {
         int n = Math.min(msg.comboReady.length, AmRoster.COMBOS.length);

         for(int i = 0; i < n; ++i) {
            want.add(new int[]{i, msg.comboHeld[i] ? 1 : 0, msg.comboReady[i] ? 1 : 0});
         }
      } else {
         for(int[] entry : msg.nearby) {
            if (entry[0] >= 1 && entry[0] <= 54 && AmRoster.SLOT_SHORTS[entry[0]] != null) {
               want.add(new int[]{entry[0], entry[1], 0});
            }
         }
      }

      int capO = Math.max(1, (int)((Math.PI * 2D) * (double)this.ring / 30.0D));
      int capI = this.inner > 0.0F ? (int)((Math.PI * 2D) * (double)this.inner / 30.0D) : 0;
      int cap = capO + capI;
      this.pages = Math.max(1, (want.size() + cap - 1) / cap);
      this.page = Mth.clamp(this.page, 0, this.pages - 1);
      int from = this.page * cap;
      int shown = Math.max(0, Math.min(want.size(), from + cap) - from);
      int outerN = Math.min(shown, capO);
      int innerN = shown - outerN;
      boolean live = this.synced && !this.flipping;
      boolean arrived = false;
      boolean readied = false;
      Set<Integer> keep = new HashSet();

      for(int i = 0; i < shown; ++i) {
         int[] e = (int[])want.get(from + i);
         int key = (this.comboPage ? 1000 : 0) + e[0];
         keep.add(key);
         boolean onOuter = i < outerN;
         int idx = onOuter ? i : i - outerN;
         int count = onOuter ? outerN : innerN;
         float target = spread(idx, count, onOuter ? 0.0F : 0.5F);
         AmWheelScreen.View v = this.find(key);
         if (v == null) {
            v = new AmWheelScreen.View(key, e[0], this.comboPage);
            v.angle = target;
            v.rad = 0.0F;
            v.delay = live ? 0.0F : (float)i * 0.03F;
            this.views.add(v);
            if (live && !this.comboPage) {
               v.fresh = true;
               arrived = true;
            }
         }

         v.leaving = false;
         v.tAngle = target;
         v.tRad = onOuter ? this.ring : this.inner;
         v.held = e[1] != 0;
         boolean ready = e[2] != 0;
         if (live && this.comboPage && ready && !v.ready && !v.held) {
            v.fresh = true;
            readied = true;
         }

         v.ready = ready;
         v.poor = !this.comboPage && !v.held && msg.aura < 65;
      }

      boolean departed = false;

      for(AmWheelScreen.View v : this.views) {
         if (!keep.contains(v.key) && !v.leaving) {
            v.leaving = true;
            if (live && !v.combo) {
               departed = true;
            }
         }
      }

      if (!this.synced) {
         ui("minecraft:block.enchantment_table.use", 1.5F, 0.35F);
      }

      if (arrived) {
         ui("minecraft:block.amethyst_block.chime", 1.3F, 0.55F);
      }

      if (readied) {
         ui("minecraft:block.amethyst_block.chime", 1.8F, 0.5F);
      }

      if (departed) {
         ui("minecraft:block.amethyst_cluster.break", 1.4F, 0.25F);
      }

      this.synced = true;
   }

   private AmWheelScreen.View find(int key) {
      for(AmWheelScreen.View v : this.views) {
         if (v.key == key) {
            return v;
         }
      }

      return null;
   }

   private static float spread(int i, int count, float offset) {
      return (float)((-Math.PI / 2D) + (Math.PI * 2D) * (double)i / (double)Math.max(1, count) + (Math.PI * 2D) * (double)offset / (double)Math.max(1, count));
   }

   private static float shortest(float d) {
      float t = ((float)Math.PI * 2F);
      d %= t;
      if ((double)d > Math.PI) {
         d -= t;
      }

      if ((double)d < -Math.PI) {
         d += t;
      }

      return d;
   }

   private void flipTo(boolean combo) {
      if (this.comboPage != combo && this.closing < 0) {
         this.comboPage = !this.comboPage;
         this.page = 0;
         this.relayout();
      }
   }

   private void turn(int dir) {
      if (this.closing < 0) {
         int want = Mth.clamp(this.page + dir, 0, this.pages - 1);
         if (want != this.page) {
            this.page = want;
            this.relayout();
         }
      }
   }

   private void relayout() {
      for(AmWheelScreen.View v : this.views) {
         v.leaving = true;
      }

      this.hovered = null;
      this.flipping = true;
      ui("minecraft:item.book.page_turn", 1.0F, 0.6F);
      if (latest != null) {
         this.sync(latest);
      }

      this.flipping = false;
   }

   public void tick() {
      if (this.closing > 0) {
         --this.closing;
         if (this.closing == 0) {
            this.onClose();
         }

      } else {
         if (++this.poll >= 20) {
            this.poll = 0;
            com.myriam.unrp.net.Net.CHANNEL.sendToServer(new AmWheelAskPacket());
         }

         this.ambient();
         if (this.hovered != null && !this.hovered.leaving) {
            this.sparks(this.hovered, 2);
         }

      }
   }

   public boolean mouseClicked(double mx, double my, int button) {
      if (this.closing >= 0) {
         return true;
      } else if (super.mouseClicked(mx, my, button)) {
         return true;
      } else if (button == 0 && this.hovered != null) {
         AmWheelScreen.View v = this.hovered;
         if (v.combo) {
            if (!v.ready || v.held) {
               this.deny(v);
               return true;
            }

            com.myriam.unrp.net.Net.CHANNEL.sendToServer(new AmWheelPickPacket(1, v.id + 1));
         } else {
            if (v.held) {
               this.deny(v);
               return true;
            }

            com.myriam.unrp.net.Net.CHANNEL.sendToServer(new AmWheelPickPacket(0, v.id));
         }

         this.pull(v);
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int key, int scan, int mods) {
      if (com.myriam.unrp.look.WheelKey.KEY.matches(key, scan)) {
         this.onClose();
         return true;
      } else {
         return super.keyPressed(key, scan, mods);
      }
   }

   private void pull(AmWheelScreen.View v) {
      v.pulled = true;
      this.closing = 12;
      this.shock = 0.0F;
      this.shockRgb = v.hue;
      float[] p = this.pos(v);
      this.burst(p[0], p[1], v.hue, 26, 2.2F);
      this.burst((float)this.cx, (float)this.cy, mix(v.hue, 16777215, 0.5F), 18, 1.6F);
      ui("minecraft:entity.illusioner.cast_spell", 1.25F, 0.45F);
      ui("minecraft:block.amethyst_block.chime", 1.6F, 0.6F);
   }

   private void deny(AmWheelScreen.View v) {
      v.shake = 1.0F;
      float[] p = this.pos(v);
      this.burst(p[0], p[1], 6974066, 8, 1.0F);
      ui("minecraft:block.note_block.bass", 0.6F, 0.45F);
   }

   private void releaseAll() {
      if (this.closing < 0) {
         com.myriam.unrp.net.Net.CHANNEL.sendToServer(new AmWheelPickPacket(2, 0));

         for(AmWheelScreen.View v : this.views) {
            if (v.held && !v.leaving) {
               float[] p = this.pos(v);
               this.burst(p[0], p[1], 10115816, 14, 2.0F);
               v.leaving = true;
            }
         }

         this.shock = 0.0F;
         this.shockRgb = 10115816;
         this.closing = 10;
         ui("minecraft:block.amethyst_cluster.break", 0.8F, 0.6F);
      }
   }

   private static void ui(String id, float pitch, float volume) {
      SoundEvent ev = (SoundEvent)BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation(id));
      if (ev != null) {
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ev, pitch, volume));
      }

   }

   private float[] pos(AmWheelScreen.View v) {
      float r = v.rad + Mth.sin(this.clock * 1.8F + (float)v.id * 0.7F) * 1.2F * Mth.clamp(v.appear, 0.0F, 1.0F);
      float shake = v.shake > 0.0F ? Mth.sin(this.clock * 70.0F) * 2.5F * v.shake : 0.0F;
      return new float[]{(float)this.cx + Mth.cos(v.angle) * r + shake, (float)this.cy + Mth.sin(v.angle) * r};
   }

   private float scaleOf(AmWheelScreen.View v) {
      float t = Mth.clamp(v.appear, 0.0F, 1.0F);
      float u = t - 1.0F;
      float pop = 1.0F + 2.70158F * u * u * u + 1.70158F * u * u;
      float s = Math.max(0.0F, pop) * (1.0F + 0.3F * v.hover);
      if (v.pulled && v.tRad > 1.0F) {
         s *= 1.0F + 0.7F * (1.0F - Mth.clamp(v.rad / v.tRad, 0.0F, 1.0F));
      }

      return s;
   }

   private void step(float dt, int mx, int my) {
      float k = 1.0F - (float)Math.exp((double)(-dt * 9.0F));
      float kf = 1.0F - (float)Math.exp((double)(-dt * 16.0F));
      Iterator<AmWheelScreen.View> it = this.views.iterator();

      while(it.hasNext()) {
         AmWheelScreen.View v = (AmWheelScreen.View)it.next();
         if (v.delay > 0.0F) {
            v.delay -= dt;
         } else {
            v.angle += shortest(v.tAngle - v.angle) * k;
            float goal = v.pulled ? 0.0F : v.tRad;
            v.rad += (goal - v.rad) * (v.pulled ? kf : k);
            float want = v.leaving ? 0.0F : 1.0F;
            v.appear += (want - v.appear) * (v.leaving ? kf : Math.min(1.0F, k * 1.4F));
            v.shake = Math.max(0.0F, v.shake - dt * 2.5F);
            if (v.fresh && v.appear > 0.5F) {
               v.fresh = false;
               float[] p = this.pos(v);
               this.burst(p[0], p[1], v.hue, 14, 1.8F);
            }

            if (v.leaving && v.appear < 0.02F) {
               it.remove();
            }
         }
      }

      AmWheelScreen.View best = null;
      if (this.closing < 0) {
         float bestD = Float.MAX_VALUE;

         for(AmWheelScreen.View v : this.views) {
            if (!v.leaving && !(v.delay > 0.0F) && !(v.appear < 0.6F)) {
               float[] p = this.pos(v);
               float d = Mth.sqrt(((float)mx - p[0]) * ((float)mx - p[0]) + ((float)my - p[1]) * ((float)my - p[1]));
               if (d <= 12.0F * this.scaleOf(v) + 2.0F && d < bestD) {
                  bestD = d;
                  best = v;
               }
            }
         }
      }

      this.hovered = best;

      for(AmWheelScreen.View v : this.views) {
         v.hover += ((v == this.hovered ? 1.0F : 0.0F) - v.hover) * kf;
      }

      int key = this.hovered == null ? -1 : this.hovered.key;
      if (key != this.lastHoverKey) {
         this.lastHoverKey = key;
         if (this.hovered != null) {
            int note = PENTA[Math.floorMod(this.hovered.id, PENTA.length)];
            ui("minecraft:block.note_block.chime", (float)Math.pow(2.0D, (double)(note - 12) / 12.0D), 0.18F);
         }
      }

      float s = dt * 20.0F;
      float drag = (float)Math.pow(0.94D, (double)s);
      Iterator<AmWheelScreen.Mote> mi = this.motes.iterator();

      while(mi.hasNext()) {
         AmWheelScreen.Mote m = (AmWheelScreen.Mote)mi.next();
         m.x += m.vx * s;
         m.y += m.vy * s;
         m.vx *= drag;
         m.vy = m.vy * drag + m.lift * s;
         m.life -= s;
         if (m.life <= 0.0F) {
            mi.remove();
         }
      }

      if (this.shock >= 0.0F) {
         this.shock += dt;
         if (this.shock > 0.7F) {
            this.shock = -1.0F;
         }
      }

   }

   private void add(AmWheelScreen.Mote m) {
      if (this.motes.size() < 420) {
         this.motes.add(m);
      }

   }

   private void ambient() {
      int accent = this.comboPage ? 10115816 : 15250250;

      for(int i = 0; i < 2; ++i) {
         float a = this.rng.nextFloat() * ((float)Math.PI * 2F);
         float r = this.ring + (this.rng.nextFloat() - 0.5F) * 30.0F;
         AmWheelScreen.Mote m = new AmWheelScreen.Mote();
         m.x = (float)this.cx + Mth.cos(a) * r;
         m.y = (float)this.cy + Mth.sin(a) * r;
         m.vx = -Mth.sin(a) * 0.35F;
         m.vy = Mth.cos(a) * 0.35F - 0.1F;
         m.lift = -0.012F;
         m.max = 24.0F + this.rng.nextFloat() * 24.0F;
         m.life = m.max;
         m.size = this.rng.nextInt(4) == 0 ? 2 : 1;
         m.rgb = mix(accent, 16777215, this.rng.nextFloat() * 0.5F);
         m.front = false;
         this.add(m);
      }

   }

   private void sparks(AmWheelScreen.View v, int n) {
      float[] p = this.pos(v);
      float reach = 12.0F * this.scaleOf(v);

      for(int i = 0; i < n; ++i) {
         float a = this.rng.nextFloat() * ((float)Math.PI * 2F);
         float sp = 0.5F + this.rng.nextFloat() * 0.9F;
         AmWheelScreen.Mote m = new AmWheelScreen.Mote();
         m.x = p[0] + Mth.cos(a) * reach;
         m.y = p[1] + Mth.sin(a) * reach;
         m.vx = Mth.cos(a) * sp;
         m.vy = Mth.sin(a) * sp;
         m.lift = -0.02F;
         m.max = 10.0F + this.rng.nextFloat() * 10.0F;
         m.life = m.max;
         m.size = this.rng.nextInt(3) == 0 ? 2 : 1;
         m.rgb = mix(v.hue, 16777215, 0.35F + this.rng.nextFloat() * 0.4F);
         m.front = true;
         this.add(m);
      }

   }

   private void burst(float x, float y, int rgb, int n, float power) {
      for(int i = 0; i < n; ++i) {
         float a = this.rng.nextFloat() * ((float)Math.PI * 2F);
         float sp = power * (0.6F + this.rng.nextFloat() * 0.8F);
         AmWheelScreen.Mote m = new AmWheelScreen.Mote();
         m.x = x;
         m.y = y;
         m.vx = Mth.cos(a) * sp;
         m.vy = Mth.sin(a) * sp;
         m.lift = 0.01F;
         m.max = 12.0F + this.rng.nextFloat() * 14.0F;
         m.life = m.max;
         m.size = this.rng.nextInt(3) == 0 ? 2 : 1;
         m.rgb = mix(rgb, 16777215, this.rng.nextFloat() * 0.45F);
         m.front = true;
         this.add(m);
      }

   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      long now = System.nanoTime();
      float dt = this.lastNanos == 0L ? 0.0F : Math.min(0.1F, (float)(now - this.lastNanos) / 1.0E9F);
      this.lastNanos = now;
      this.clock += dt;
      this.renderBackground(g);
      this.step(dt, mouseX, mouseY);
      AmWheelPacket data = latest;
      int accent = this.comboPage ? 10115816 : 15250250;
      g.drawManaged(() -> {
         this.vignette(g);
         this.ornaments(g, accent);
         this.spokes(g);
         this.motes(g, false);
      });
      List<AmWheelScreen.View> rest = new ArrayList();

      for(AmWheelScreen.View v : this.views) {
         if (v != this.hovered && v.delay <= 0.0F && v.appear > 0.01F) {
            rest.add(v);
         }
      }

      this.drawSet(g, rest);
      AmWheelScreen.View focus = this.hovered;
      if (focus != null) {
         g.drawManaged(() -> this.line(g, focus, 2.0F, argb((int)(144.0F * focus.hover), focus.hue), focus.hover, mix(focus.hue, 16777215, 0.55F)));
         this.drawSet(g, Collections.singletonList(focus));
      }

      this.hub(g, data, accent, focus);
      g.drawManaged(() -> {
         this.motes(g, true);
         this.shockwave(g);
      });
      this.header(g, data, accent);
      this.info(g, data, focus);
      this.buttons(g, data, accent);
      super.render(g, mouseX, mouseY, partialTick);
   }

   private void drawSet(GuiGraphics g, List<AmWheelScreen.View> set) {
      g.drawManaged(() -> {
         for(AmWheelScreen.View v : set) {
            this.medallion(g, v);
         }

      });

      for(AmWheelScreen.View v : set) {
         this.iconOf(g, v);
      }

      g.drawManaged(() -> {
         for(AmWheelScreen.View v : set) {
            this.overlay(g, v);
         }

      });
   }

   private void vignette(GuiGraphics g) {
      float r = this.ring + 34.0F;
      g.pose().pushPose();
      g.pose().translate((float)this.cx, (float)this.cy, 0.0F);
      disc(g, (int)r, argb(70, 722968));
      disc(g, (int)(r - 14.0F), argb(58, 722968));
      disc(g, (int)(this.ring - 20.0F), argb(48, 722968));
      g.pose().popPose();
   }

   private void ornaments(GuiGraphics g, int accent) {
      float reveal = Mth.clamp(this.clock * 2.4F, 0.0F, 1.0F);
      int n = 96;
      float spin = this.clock * 0.12F;
      float ro = this.ring + 22.0F;

      for(int i = 0; (float)i < (float)n * reveal; ++i) {
         float a = spin + (float)((Math.PI * 2D) * (double)i / (double)n) - ((float)Math.PI / 2F);
         int x = (int)((float)this.cx + Mth.cos(a) * ro);
         int y = (int)((float)this.cy + Mth.sin(a) * ro);
         if (i % 8 == 0) {
            g.fill(x - 1, y - 1, x + 1, y + 1, argb(192, accent));
         } else {
            g.fill(x, y, x + 1, y + 1, argb(112, accent));
         }
      }

      int ticks = 48;
      float spin2 = -this.clock * 0.2F;

      for(int i = 0; (float)i < (float)ticks * reveal; ++i) {
         float a = spin2 + (float)((Math.PI * 2D) * (double)i / (double)ticks);
         float c = Mth.cos(a);
         float s = Mth.sin(a);

         for(int d = 0; d < 3; ++d) {
            float rr = this.ring + 14.0F + (float)d;
            int x = (int)((float)this.cx + c * rr);
            int y = (int)((float)this.cy + s * rr);
            g.fill(x, y, x + 1, y + 1, argb(80, 9071420));
         }
      }

      int guide = 120;

      for(int i = 0; (float)i < (float)guide * reveal; ++i) {
         float a = (float)((Math.PI * 2D) * (double)i / (double)guide);
         int x = (int)((float)this.cx + Mth.cos(a) * this.ring);
         int y = (int)((float)this.cy + Mth.sin(a) * this.ring);
         g.fill(x, y, x + 1, y + 1, argb(56, accent));
         if (this.inner > 0.0F && i % 2 == 0) {
            int xi = (int)((float)this.cx + Mth.cos(a) * this.inner);
            int yi = (int)((float)this.cy + Mth.sin(a) * this.inner);
            g.fill(xi, yi, xi + 1, yi + 1, argb(40, accent));
         }
      }

   }

   private void spokes(GuiGraphics g) {
      for(AmWheelScreen.View v : this.views) {
         if (!(v.delay > 0.0F) && !(v.appear < 0.05F) && v != this.hovered) {
            boolean strong = v.held || v.combo && v.ready;
            int col = strong ? (v.held ? 10115816 : 15250250) : 9071420;
            int alpha = (int)((float)(strong ? 72 : 24) * Mth.clamp(v.appear, 0.0F, 1.0F));
            this.line(g, v, 5.0F, argb(alpha, col), 0.0F, 0);
         }
      }

   }

   private void line(GuiGraphics g, AmWheelScreen.View v, float step, int color, float pulseAmt, int pulseColor) {
      float[] p = this.pos(v);
      float dx = p[0] - (float)this.cx;
      float dy = p[1] - (float)this.cy;
      float len = Mth.sqrt(dx * dx + dy * dy);
      if (!(len < 1.0F)) {
         float ux = dx / len;
         float uy = dy / len;
         float start = 26.0F;
         float end = len - 12.0F * this.scaleOf(v) - 3.0F;
         if (!(end <= start)) {
            float head = this.clock * 1.3F % 1.0F;

            for(float d = start; d < end; d += step) {
               float f = (d - start) / (end - start);
               float glow = pulseAmt <= 0.0F ? 0.0F : Math.max(0.0F, 1.0F - Math.abs(f - head) * 7.0F) * pulseAmt;
               int x = (int)((float)this.cx + ux * d);
               int y = (int)((float)this.cy + uy * d);
               if (glow > 0.05F) {
                  g.fill(x - 1, y - 1, x + 1, y + 1, argb((int)(255.0F * glow), pulseColor));
               } else {
                  g.fill(x, y, x + 1, y + 1, color);
               }
            }

         }
      }
   }

   private boolean lit(AmWheelScreen.View v) {
      return v.combo ? v.ready || v.held : !v.poor;
   }

   private int rimOf(AmWheelScreen.View v) {
      if (v.held) {
         return 10115816;
      } else if (v.combo) {
         if (v.ready) {
            float pulse = 0.5F + 0.5F * Mth.sin(this.clock * 6.0F + (float)v.id);
            return mix(15250250, 16777215, pulse * 0.35F);
         } else {
            return 3816002;
         }
      } else {
         return v.poor ? 12077116 : mix(9071420, v.hue, 0.35F);
      }
   }

   private void medallion(GuiGraphics g, AmWheelScreen.View v) {
      float s = this.scaleOf(v);
      if (!(s <= 0.01F)) {
         float[] p = this.pos(v);
         float vis = Mth.clamp(v.appear, 0.0F, 1.0F);
         g.pose().pushPose();
         g.pose().translate(p[0], p[1], 0.0F);
         g.pose().scale(s, s, 1.0F);
         if (v.hover > 0.02F) {
            disc(g, 19, argb((int)(38.0F * v.hover), v.hue));
            disc(g, 16, argb((int)(52.0F * v.hover), v.hue));
         }

         g.pose().pushPose();
         g.pose().translate(1.0F, 1.5F, 0.0F);
         disc(g, 13, argb((int)(112.0F * vis), 0));
         g.pose().popPose();
         annulus(g, 13, 11, argb((int)(255.0F * vis), this.rimOf(v)));
         disc(g, 11, argb((int)(230.0F * vis), 722968));
         if (this.lit(v)) {
            disc(g, 9, argb((int)(58.0F * vis), v.hue));
         }

         g.fill(-5, -12, 6, -11, argb((int)(128.0F * vis), 16773836));
         g.pose().popPose();
      }
   }

   private void iconOf(GuiGraphics g, AmWheelScreen.View v) {
      float s = this.scaleOf(v);
      if (!(s <= 0.01F)) {
         float[] p = this.pos(v);
         ResourceLocation tex = v.combo ? COMBO_TEX[v.id] : SLOT_TEX[v.id];
         float vis = Mth.clamp(v.appear, 0.0F, 1.0F);
         float r = 1.0F;
         float gg = 1.0F;
         float b = 1.0F;
         if (v.combo && !v.ready && !v.held) {
            r = 0.34F;
            gg = 0.34F;
            b = 0.38F;
         } else if (v.poor) {
            r = 0.75F;
            gg = 0.5F;
            b = 0.5F;
         }

         icon(g, tex, p[0], p[1], s, r, gg, b, vis);
      }
   }

   private static void icon(GuiGraphics g, ResourceLocation tex, float x, float y, float s, float r, float gg, float b, float a) {
      if (tex != null && !(a <= 0.01F)) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         g.setColor(r, gg, b, a);
         g.pose().pushPose();
         g.pose().translate(x, y, 0.0F);
         g.pose().scale(s, s, 1.0F);
         g.blit(tex, -8, -8, 0.0F, 0.0F, 16, 16, 16, 16);
         g.pose().popPose();
         g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
      }
   }

   private void overlay(GuiGraphics g, AmWheelScreen.View v) {
      float s = this.scaleOf(v);
      if (!(s <= 0.01F)) {
         float[] p = this.pos(v);
         float vis = Mth.clamp(v.appear, 0.0F, 1.0F);
         g.pose().pushPose();
         g.pose().translate(p[0], p[1], 0.0F);
         g.pose().scale(s, s, 1.0F);
         if (v.held) {
            for(int k = 0; k < 3; ++k) {
               float a = this.clock * 2.6F + (float)v.id - (float)k * 0.22F;
               int x = (int)Math.floor((double)(Mth.cos(a) * 15.0F));
               int y = (int)Math.floor((double)(Mth.sin(a) * 15.0F));
               int al = (int)((float)(224 - k * 80) * vis);
               g.fill(x - 1, y - 1, x + 1, y + 1, argb(al, k == 0 ? 15127295 : 10115816));
            }
         }

         if (v.combo && v.ready && !v.held) {
            float pulse = 0.5F + 0.5F * Mth.sin(this.clock * 6.0F + (float)v.id);
            dots(g, 15 + (int)(pulse * 2.0F), 20, this.clock * 0.8F, argb((int)((96.0F + 112.0F * pulse) * vis), 15250250));

            for(int k = 0; k < 4; ++k) {
               float a = -this.clock * 1.4F + (float)k * 1.5708F;
               int x = (int)(Mth.cos(a) * 17.0F);
               int y = (int)(Mth.sin(a) * 17.0F);
               g.fill(x, y - 1, x + 1, y + 2, argb((int)(208.0F * vis), 16773296));
               g.fill(x - 1, y, x + 2, y + 1, argb((int)(208.0F * vis), 16773296));
            }
         }

         if (v.poor && !v.held) {
            g.fill(6, 6, 11, 11, argb((int)(208.0F * vis), 2755082));
            g.fill(7, 7, 10, 10, argb((int)(240.0F * vis), 12077116));
         }

         g.pose().popPose();
      }
   }

   private void hub(GuiGraphics g, AmWheelPacket data, int accent, AmWheelScreen.View focus) {
      int glowRgb = focus != null ? focus.hue : accent;
      float breathe = 0.5F + 0.5F * Mth.sin(this.clock * 2.2F);
      float flash = this.shock >= 0.0F ? Math.max(0.0F, 1.0F - this.shock / 0.35F) : 0.0F;
      g.drawManaged(() -> {
         g.pose().pushPose();
         g.pose().translate((float)this.cx, (float)this.cy, 0.0F);
         disc(g, 29, argb((int)(32.0F + 40.0F * breathe + 96.0F * flash), glowRgb));
         annulus(g, 22, 20, argb(255, 9071420));
         disc(g, 20, argb(240, 722968));
         disc(g, 17, argb((int)(48.0F + 48.0F * breathe), glowRgb));
         dots(g, 25, 12, this.clock * 0.9F, argb(144, accent));
         dots(g, 27, 18, -this.clock * 0.5F, argb(80, 9071420));
         g.pose().popPose();
      });
      if (focus != null) {
         ResourceLocation tex = focus.combo ? COMBO_TEX[focus.id] : SLOT_TEX[focus.id];
         float dim = focus.combo && !focus.ready && !focus.held ? 0.45F : 1.0F;
         icon(g, tex, (float)this.cx, (float)this.cy, 2.0F, dim, dim, Math.min(1.0F, dim + 0.05F), 1.0F);
         if (focus.combo) {
            this.constellation(g, focus, data);
         }
      } else {
         icon(g, HUB_TEX, (float)this.cx, (float)this.cy, 2.0F, 1.0F, 1.0F, 1.0F, 0.35F + 0.25F * breathe);
      }

   }

   private void constellation(GuiGraphics g, AmWheelScreen.View focus, AmWheelPacket data) {
      String[] combo = AmRoster.COMBOS[focus.id];
      int n = combo.length - 2;
      float rad = 36.0F;
      List<float[]> spots = new ArrayList();

      for(int i = 0; i < n; ++i) {
         float a = (float)((-Math.PI / 2D) + (Math.PI * 2D) * (double)i / (double)n) + this.clock * 0.35F;
         spots.add(new float[]{(float)this.cx + Mth.cos(a) * rad, (float)this.cy + Mth.sin(a) * rad});
      }

      g.drawManaged(() -> {
         for(int i = 0; i < n; ++i) {
            float[] sp = (float[])spots.get(i);
            boolean have = data != null && this.hasThread(data, combo[i + 2]);
            g.pose().pushPose();
            g.pose().translate(sp[0], sp[1], 0.0F);
            annulus(g, 10, 8, argb(224, have ? 10115816 : 3816002));
            disc(g, 8, argb(224, 722968));
            g.pose().popPose();
         }

      });

      for(int i = 0; i < n; ++i) {
         float[] sp = (float[])spots.get(i);
         int slot = AmRoster.slotOf(combo[i + 2]);
         boolean have = data != null && this.hasThread(data, combo[i + 2]);
         float c = have ? 1.0F : 0.35F;
         icon(g, slot > 0 ? SLOT_TEX[slot] : null, sp[0], sp[1], 1.0F, c, c, c, 1.0F);
      }

   }

   private void shockwave(GuiGraphics g) {
      if (!(this.shock < 0.0F)) {
         float f = this.shock / 0.7F;
         int r = (int)(22.0F + (this.ring + 30.0F - 22.0F) * f);
         int alpha = (int)(192.0F * (1.0F - f));
         g.pose().pushPose();
         g.pose().translate((float)this.cx, (float)this.cy, 0.0F);
         dots(g, r, Math.max(24, r), 0.0F, argb(alpha, this.shockRgb));
         dots(g, Math.max(1, r - 3), Math.max(24, r), 0.3F, argb(alpha / 2, 16777215));
         g.pose().popPose();
      }
   }

   private void motes(GuiGraphics g, boolean front) {
      for(AmWheelScreen.Mote m : this.motes) {
         if (m.front == front) {
            float f = Mth.clamp(m.life / m.max, 0.0F, 1.0F);
            int x = (int)m.x;
            int y = (int)m.y;
            g.fill(x, y, x + m.size, y + m.size, argb((int)(224.0F * f), m.rgb));
         }
      }

   }

   private void header(GuiGraphics g, AmWheelPacket data, int accent) {
      String title = this.comboPage ? "§5§lTHE COMBINATION WHEEL" : "§6§lTHE COPY WHEEL";
      String sub = data == null ? "§8listening for auras..." : "§7Aura §6" + data.aura + "§8   ·   §7Threads §6" + data.copiedCount + (this.pages > 1 ? "§8   ·   §7page " + (this.page + 1) + "/" + this.pages : "");
      int ty = (int)((float)this.cy - this.ring - 48.0F);
      int w = Math.max(this.font.width(title), this.font.width(sub)) / 2 + 16;
      g.fill(this.cx - w, ty - 6, this.cx + w, ty + 23, argb(184, 722968));
      g.fill(this.cx - w, ty - 6, this.cx + w, ty - 5, argb(224, accent));
      g.fill(this.cx - w, ty + 22, this.cx + w, ty + 23, argb(224, accent));
      g.fill(this.cx - w - 3, ty + 7, this.cx - w, ty + 10, argb(224, accent));
      g.fill(this.cx + w, ty + 7, this.cx + w + 3, ty + 10, argb(224, accent));
      g.drawCenteredString(this.font, title, this.cx, ty, 16777215);
      g.drawCenteredString(this.font, sub, this.cx, ty + 11, 16777215);
   }

   private void info(GuiGraphics g, AmWheelPacket data, AmWheelScreen.View v) {
      int y0 = (int)((float)this.cy + this.ring + 22.0F);
      if (data == null) {
         g.drawCenteredString(this.font, "§8Listening for auras...", this.cx, y0, 16777215);
      } else if (!this.comboPage && data.nearby.isEmpty()) {
         g.drawCenteredString(this.font, "§8No auras within reach. The wheel waits.", this.cx, y0, 16777215);
      } else if (v == null) {
         g.drawCenteredString(this.font, this.comboPage ? "§8Fuse copied threads into a combination." : "§8Auras in reach hang on the wheel. Pull a thread.", this.cx, y0, 16777215);
      } else if (!v.combo) {
         String name = PowerNames.display(AmRoster.SLOT_SHORTS[v.id]);
         g.drawCenteredString(this.font, "§f§l" + name, this.cx, y0, 16777215);
         String line = v.held ? "§8already woven into you" : (v.poor ? "§cnot enough aura§8: 65 needed" : "§765 aura §8to §7pull the thread");
         g.drawCenteredString(this.font, line, this.cx, y0 + 12, 16777215);
      } else {
         String[] combo = AmRoster.COMBOS[v.id];
         g.drawCenteredString(this.font, "§f§l" + combo[1], this.cx, y0, 16777215);
         StringBuilder needs = new StringBuilder();

         for(int i = 2; i < combo.length; ++i) {
            if (needs.length() > 0) {
               needs.append("§8 + ");
            }

            needs.append(this.hasThread(data, combo[i]) ? "§f" : "§8").append(PowerNames.display(combo[i]));
         }

         g.drawCenteredString(this.font, needs.toString(), this.cx, y0 + 12, 16777215);
         String state = v.held ? "§5held" : (v.ready ? "§6fuse now" : "§8missing threads");
         g.drawCenteredString(this.font, state, this.cx, y0 + 24, 16777215);
      }
   }

   private void buttons(GuiGraphics g, AmWheelPacket data, int accent) {
      this.tabCopy.active = this.closing < 0;
      this.tabCombo.active = this.closing < 0;
      this.release.active = data != null && data.copiedCount > 0 && this.closing < 0;
      this.prev.visible = this.pages > 1;
      this.next.visible = this.pages > 1;
      this.prev.active = this.page > 0;
      this.next.active = this.page < this.pages - 1;
      Button on = this.comboPage ? this.tabCombo : this.tabCopy;
      g.fill(on.getX() + 3, on.getY() + on.getHeight() + 1, on.getX() + on.getWidth() - 3, on.getY() + on.getHeight() + 3, argb(255, accent));
   }

   private boolean hasThread(AmWheelPacket data, String shortName) {
      for(int[] entry : data.nearby) {
         if (entry[0] > 0 && entry[0] <= 54 && shortName.equals(AmRoster.SLOT_SHORTS[entry[0]]) && entry[1] != 0) {
            return true;
         }
      }

      return false;
   }

   private static void disc(GuiGraphics g, int r, int color) {
      for(int dy = -r; dy <= r; ++dy) {
         int half = (int)Math.floor(Math.sqrt((double)r * (double)r - (double)dy * (double)dy));
         g.fill(-half, dy, half + 1, dy + 1, color);
      }

   }

   private static void annulus(GuiGraphics g, int ro, int ri, int color) {
      for(int dy = -ro; dy <= ro; ++dy) {
         int ho = (int)Math.floor(Math.sqrt((double)ro * (double)ro - (double)dy * (double)dy));
         if (Math.abs(dy) > ri) {
            g.fill(-ho, dy, ho + 1, dy + 1, color);
         } else {
            int hi = (int)Math.floor(Math.sqrt((double)ri * (double)ri - (double)dy * (double)dy));
            g.fill(-ho, dy, -hi, dy + 1, color);
            g.fill(hi + 1, dy, ho + 1, dy + 1, color);
         }
      }

   }

   private static void dots(GuiGraphics g, int r, int n, float spin, int color) {
      for(int i = 0; i < n; ++i) {
         float a = spin + (float)((Math.PI * 2D) * (double)i / (double)n);
         int x = (int)Math.floor((double)(Mth.cos(a) * (float)r));
         int y = (int)Math.floor((double)(Mth.sin(a) * (float)r));
         g.fill(x, y, x + 1, y + 1, color);
      }

   }

   private static int argb(int alpha, int rgb) {
      return Mth.clamp(alpha, 0, 255) << 24 | rgb & 16777215;
   }

   private static int mix(int a, int b, float f) {
      int r = (int)((float)(a >> 16 & 255) * (1.0F - f) + (float)(b >> 16 & 255) * f);
      int gg = (int)((float)(a >> 8 & 255) * (1.0F - f) + (float)(b >> 8 & 255) * f);
      int bl = (int)((float)(a & 255) * (1.0F - f) + (float)(b & 255) * f);
      return r << 16 | gg << 8 | bl;
   }

   private static int hue(String name) {
      float h = (float)((name.hashCode() % 360 + 360) % 360) / 360.0F;
      return Mth.hsvToRgb(h, 0.55F, 0.85F);
   }

   public boolean isPauseScreen() {
      return false;
   }

   static {
      for(int i = 1; i <= 54; ++i) {
         if (AmRoster.SLOT_ICONS[i] != null) {
            SLOT_TEX[i] = new ResourceLocation("unrp", AmRoster.SLOT_ICONS[i]);
         }
      }

      for(int i = 0; i < AmRoster.COMBO_ICONS.length; ++i) {
         COMBO_TEX[i] = new ResourceLocation("unrp", AmRoster.COMBO_ICONS[i]);
      }

   }

   private static final class Mote {
      float x;
      float y;
      float vx;
      float vy;
      float lift;
      float life;
      float max;
      int size;
      int rgb;
      boolean front;
   }

   private static final class View {
      final int key;
      final int id;
      final boolean combo;
      final int hue;
      float angle;
      float tAngle;
      float rad;
      float tRad;
      float appear;
      float hover;
      float delay;
      float shake;
      boolean leaving;
      boolean pulled;
      boolean fresh;
      boolean held;
      boolean ready;
      boolean poor;

      View(int key, int id, boolean combo) {
         this.key = key;
         this.id = id;
         this.combo = combo;
         this.hue = AmWheelScreen.hue(combo ? AmRoster.COMBOS[id][0] : AmRoster.SLOT_SHORTS[id]);
      }
   }
}
