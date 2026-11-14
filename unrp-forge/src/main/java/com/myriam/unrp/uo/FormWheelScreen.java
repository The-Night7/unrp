package com.myriam.unrp.uo;



import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FormWheelScreen extends Screen {
   private static final String[] ERA_NAME = new String[]{"", "First Break", "Second Break", "Third Break", "Final Break"};
   private static final int BRONZE = 9071420;
   private static final int NIGHT = 722968;
   private static final int EMBER = 12077116;
   private static final int ASH = 3816002;
   private static final int GOLD = 15250250;
   private static final float SLOT_R = 12.0F;
   private static final int POSITIONS = 18;
   private static final int[] PENTA = new int[]{0, 2, 4, 7, 9, 12, 14, 16, 19, 21};
   private static final ResourceLocation[] SIGIL = new ResourceLocation[15];
   private final FormWheelPacket data;
   private final List<FormWheelScreen.View> views = new ArrayList();
   private final List<FormWheelScreen.Mote> motes = new ArrayList();
   private final Random rng = new Random();
   private int cx;
   private int cy;
   private float ring;
   private float slotScale = 1.0F;
   private float clock;
   private long lastNanos;
   private int closing = -1;
   private float shock = -1.0F;
   private int shockRgb = 15250250;
   private FormWheelScreen.View hovered;
   private int lastHoverKey = -1;
   private boolean opened;
   private boolean previewBroken;

   private FormWheelScreen(FormWheelPacket data) {
      super(Component.literal("The Ladder"));
      this.data = data;
   }

   public static void open(FormWheelPacket data) {
      Minecraft.getInstance().setScreen(new FormWheelScreen(data));
   }

   private boolean unlocked(int f) {
      return (this.data.mask & 1 << f) != 0;
   }

   private boolean poor(int f) {
      return this.unlocked(f) && f != this.data.current && this.data.costs != null && f < this.data.costs.length && this.data.costs[f] > this.data.grit;
   }

   private static int positionOf(int f) {
      return f - 1 + (LimitBreakerForms.era(f) - 1);
   }

   private static float angleAt(float p) {
      return (float)((-Math.PI / 2D) + (Math.PI * 2D) * (double)(p + 1.0F) / 18.0D);
   }

   protected void init() {
      this.cx = this.width / 2;
      this.cy = this.height / 2 - 10;
      this.ring = Mth.clamp(Math.min((float)this.height / 2.0F - 66.0F, (float)this.width / 2.0F - 64.0F), 50.0F, 100.0F);
      float spacing = (float)((Math.PI * 2D) * (double)this.ring / 18.0D);
      this.slotScale = Mth.clamp(spacing / 30.0F, 0.72F, 1.0F);
      if (this.views.isEmpty()) {
         for(int f = 1; f < 15; ++f) {
            FormWheelScreen.View v_0 = new FormWheelScreen.View(f, LimitBreakerForms.color(f, this.data.rose));
            v_0.tAngle = angleAt((float)positionOf(f));
            v_0.angle = v_0.tAngle;
            v_0.delay = (float)(f - 1) * 0.028F;
            this.views.add(v_0);
         }
      }

      for(FormWheelScreen.View v : this.views) {
         v.tRad = this.ring;
      }

      int rowY = (int)((float)this.cy + this.ring + 58.0F);
      this.addRenderableWidget(Button.builder(Component.literal("Release"), (b) -> this.release()).bounds(this.cx - 78, rowY, 74, 16).build());
      this.addRenderableWidget(Button.builder(Component.literal(this.data.auraOff ? "Aura: Off" : "Aura: On"), (b) -> this.pick(-1)).bounds(this.cx + 4, rowY, 74, 16).build());
      if (!this.opened) {
         this.opened = true;
         WheelArt.ui("minecraft:block.beacon.power_select", 1.6F, 0.35F);
      }

   }

   private void pick(int form) {
      if (this.closing < 0) {
         com.myriam.unrp.net.Net.CHANNEL.sendToServer(new FormPickPacket(form));
         if (form == -1) {
            WheelArt.ui(this.data.auraOff ? "minecraft:item.firecharge.use" : "minecraft:block.fire.extinguish", 1.2F, 0.5F);
            this.closing = 4;
         } else {
            this.closing = 12;
         }
      }
   }

   private void release() {
      if (this.closing < 0) {
         for(FormWheelScreen.View v : this.views) {
            if (v.form == this.data.current) {
               float[] p = this.pos(v);
               this.burst(p[0], p[1], 10132134, 18, 1.8F);
            }
         }

         this.shock = 0.0F;
         this.shockRgb = 10132134;
         WheelArt.ui("minecraft:block.beacon.deactivate", 1.1F, 0.5F);
         this.pick(0);
      }
   }

   private void choose(FormWheelScreen.View v) {
      if (this.unlocked(v.form) && v.form != this.data.current) {
         v.pulled = true;
         this.shock = 0.0F;
         this.shockRgb = v.hue;
         float[] p = this.pos(v);
         this.burst(p[0], p[1], v.hue, 30, 2.4F);
         this.burst((float)this.cx, (float)this.cy + this.ring * 0.3F, WheelArt.mix(v.hue, 16777215, 0.5F), 22, 1.8F);
         WheelArt.ui("minecraft:block.beacon.activate", 1.3F, 0.55F);
         WheelArt.ui("minecraft:block.amethyst_block.chime", 1.6F, 0.6F);
         this.pick(v.form);
      } else {
         v.shake = 1.0F;
         float[] p = this.pos(v);
         this.burst(p[0], p[1], 6974066, 8, 1.0F);
         WheelArt.ui("minecraft:block.note_block.bass", 0.6F, 0.45F);
      }
   }

   public void tick() {
      if (this.closing > 0) {
         --this.closing;
         if (this.closing == 0) {
            this.onClose();
         }

      } else {
         this.ambient();
         if (this.hovered != null) {
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
         this.choose(this.hovered);
         return true;
      } else {
         return false;
      }
   }

   private float[] pos(FormWheelScreen.View v) {
      float r = v.rad + Mth.sin(this.clock * 1.8F + (float)v.form * 0.7F) * 1.2F * Mth.clamp(v.appear, 0.0F, 1.0F);
      float shake = v.shake > 0.0F ? Mth.sin(this.clock * 70.0F) * 2.5F * v.shake : 0.0F;
      return new float[]{(float)this.cx + Mth.cos(v.angle) * r + shake, (float)this.cy + Mth.sin(v.angle) * r};
   }

   private float scaleOf(FormWheelScreen.View v) {
      float s = WheelArt.pop(v.appear) * this.slotScale * (1.0F + 0.3F * v.hover);
      if (v.pulled && v.tRad > 1.0F) {
         s *= 1.0F + 0.7F * (1.0F - Mth.clamp(v.rad / v.tRad, 0.0F, 1.0F));
      }

      return s;
   }

   private void step(float dt, int mx, int my) {
      float k = 1.0F - (float)Math.exp((double)(-dt * 9.0F));
      float kf = 1.0F - (float)Math.exp((double)(-dt * 16.0F));

      for(FormWheelScreen.View v_0 : this.views) {
         if (v_0.delay > 0.0F) {
            v_0.delay -= dt;
         } else {
            v_0.angle += WheelArt.shortest(v_0.tAngle - v_0.angle) * k;
            float goal = v_0.pulled ? this.ring * 0.3F : v_0.tRad;
            v_0.rad += (goal - v_0.rad) * (v_0.pulled ? kf : k);
            v_0.appear += (1.0F - v_0.appear) * Math.min(1.0F, k * 1.4F);
            v_0.shake = Math.max(0.0F, v_0.shake - dt * 2.5F);
         }
      }

      FormWheelScreen.View best = null;
      if (this.closing < 0) {
         float bestD = Float.MAX_VALUE;

         for(FormWheelScreen.View v : this.views) {
            if (!(v.delay > 0.0F) && !(v.appear < 0.6F)) {
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

      for(FormWheelScreen.View v : this.views) {
         v.hover += ((v == this.hovered ? 1.0F : 0.0F) - v.hover) * kf;
      }

      int key = this.hovered == null ? -1 : this.hovered.form;
      if (key != this.lastHoverKey) {
         this.lastHoverKey = key;
         if (this.hovered != null) {
            int note = PENTA[Math.floorMod(this.hovered.form, PENTA.length)];
            WheelArt.ui("minecraft:block.note_block.chime", (float)Math.pow(2.0D, (double)(note - 12) / 12.0D), 0.18F);
         }
      }

      float s = dt * 20.0F;
      float drag = (float)Math.pow(0.94D, (double)s);
      Iterator<FormWheelScreen.Mote> mi = this.motes.iterator();

      while(mi.hasNext()) {
         FormWheelScreen.Mote m = (FormWheelScreen.Mote)mi.next();
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

   private void add(FormWheelScreen.Mote m) {
      if (this.motes.size() < 420) {
         this.motes.add(m);
      }

   }

   private int focusRgb() {
      if (this.hovered != null) {
         return this.hovered.hue;
      } else {
         return LimitBreakerForms.valid(this.data.current) ? LimitBreakerForms.color(this.data.current, this.data.rose) : 15250250;
      }
   }

   private void ambient() {
      int accent = this.focusRgb();

      for(int i = 0; i < 2; ++i) {
         float a = this.rng.nextFloat() * ((float)Math.PI * 2F);
         float r = this.ring + (this.rng.nextFloat() - 0.5F) * 30.0F;
         FormWheelScreen.Mote m_0 = new FormWheelScreen.Mote();
         m_0.x = (float)this.cx + Mth.cos(a) * r;
         m_0.y = (float)this.cy + Mth.sin(a) * r;
         m_0.vx = -Mth.sin(a) * 0.35F;
         m_0.vy = Mth.cos(a) * 0.35F - 0.1F;
         m_0.lift = -0.012F;
         m_0.max = 24.0F + this.rng.nextFloat() * 24.0F;
         m_0.life = m_0.max;
         m_0.size = this.rng.nextInt(4) == 0 ? 2 : 1;
         m_0.rgb = WheelArt.mix(accent, 16777215, this.rng.nextFloat() * 0.5F);
         this.add(m_0);
      }

      float footY = (float)this.cy + this.ring * 0.3F;
      FormWheelScreen.Mote m = new FormWheelScreen.Mote();
      m.x = (float)this.cx + (this.rng.nextFloat() - 0.5F) * 52.0F;
      m.y = footY;
      m.vx = (this.rng.nextFloat() - 0.5F) * 0.2F;
      m.vy = -0.6F - this.rng.nextFloat() * 0.6F;
      m.lift = -0.02F;
      m.max = 18.0F + this.rng.nextFloat() * 18.0F;
      m.life = m.max;
      m.size = 1;
      m.rgb = WheelArt.mix(accent, 16777215, 0.3F);
      m.front = true;
      this.add(m);
   }

   private void sparks(FormWheelScreen.View v, int n) {
      float[] p = this.pos(v);
      float reach = 12.0F * this.scaleOf(v);

      for(int i = 0; i < n; ++i) {
         float a = this.rng.nextFloat() * ((float)Math.PI * 2F);
         float sp = 0.5F + this.rng.nextFloat() * 0.9F;
         FormWheelScreen.Mote m = new FormWheelScreen.Mote();
         m.x = p[0] + Mth.cos(a) * reach;
         m.y = p[1] + Mth.sin(a) * reach;
         m.vx = Mth.cos(a) * sp;
         m.vy = Mth.sin(a) * sp;
         m.lift = -0.02F;
         m.max = 10.0F + this.rng.nextFloat() * 10.0F;
         m.life = m.max;
         m.size = this.rng.nextInt(3) == 0 ? 2 : 1;
         m.rgb = WheelArt.mix(v.hue, 16777215, 0.35F + this.rng.nextFloat() * 0.4F);
         m.front = true;
         this.add(m);
      }

   }

   private void burst(float x, float y, int rgb, int n, float power) {
      for(int i = 0; i < n; ++i) {
         float a = this.rng.nextFloat() * ((float)Math.PI * 2F);
         float sp = power * (0.6F + this.rng.nextFloat() * 0.8F);
         FormWheelScreen.Mote m = new FormWheelScreen.Mote();
         m.x = x;
         m.y = y;
         m.vx = Mth.cos(a) * sp;
         m.vy = Mth.sin(a) * sp;
         m.lift = 0.01F;
         m.max = 12.0F + this.rng.nextFloat() * 14.0F;
         m.life = m.max;
         m.size = this.rng.nextInt(3) == 0 ? 2 : 1;
         m.rgb = WheelArt.mix(rgb, 16777215, this.rng.nextFloat() * 0.45F);
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
      int accent = this.focusRgb();
      g.drawManaged(() -> {
         this.vignette(g);
         this.ornaments(g);
         this.motes(g, false);
      });
      this.eraLabels(g);
      int showing = this.hovered != null ? this.hovered.form : this.data.current;
      this.mirror(g, showing, mouseX, mouseY, accent);
      List<FormWheelScreen.View> rest = new ArrayList();

      for(FormWheelScreen.View v : this.views) {
         if (v != this.hovered && v.delay <= 0.0F && v.appear > 0.01F) {
            rest.add(v);
         }
      }

      this.drawSet(g, rest);
      FormWheelScreen.View focus = this.hovered;
      if (focus != null) {
         g.drawManaged(() -> this.thread(g, focus));
         this.drawSet(g, Collections.singletonList(focus));
      }

      g.drawManaged(() -> {
         this.motes(g, true);
         this.shockwave(g);
      });
      this.header(g, accent);
      this.info(g, focus);
      super.render(g, mouseX, mouseY, partialTick);
   }

   private void drawSet(GuiGraphics g, List<FormWheelScreen.View> set) {
      g.drawManaged(() -> {
         for(FormWheelScreen.View v_0 : set) {
            this.medallion(g, v_0);
         }

      });

      for(FormWheelScreen.View v : set) {
         this.sigil(g, v);
      }

      g.drawManaged(() -> {
         for(FormWheelScreen.View v : set) {
            this.overlay(g, v);
         }

      });
   }

   private void vignette(GuiGraphics g) {
      float r = this.ring + 36.0F;
      g.pose().pushPose();
      g.pose().translate((float)this.cx, (float)this.cy, 0.0F);
      WheelArt.disc(g, (int)r, WheelArt.argb(70, 722968));
      WheelArt.disc(g, (int)(r - 14.0F), WheelArt.argb(58, 722968));
      WheelArt.disc(g, (int)(this.ring - 20.0F), WheelArt.argb(48, 722968));
      g.pose().popPose();
   }

   private void ornaments(GuiGraphics g) {
      float reveal = Mth.clamp(this.clock * 2.4F, 0.0F, 1.0F);
      float spin = this.clock * 0.12F;
      float ro = this.ring + 24.0F;
      int n = 108;

      for(int i = 0; (float)i < (float)n * reveal; ++i) {
         float a_0 = spin + (float)((Math.PI * 2D) * (double)i / (double)n) - ((float)Math.PI / 2F);
         int x_0 = (int)((float)this.cx + Mth.cos(a_0) * ro);
         int y_0 = (int)((float)this.cy + Mth.sin(a_0) * ro);
         g.fill(x_0, y_0, x_0 + 1, y_0 + 1, WheelArt.argb(96, 9071420));
      }

      for(int era = 1; era <= 4; ++era) {
         int first = LimitBreakerForms.firstOfEra(era);
         int last = first;

         for(int f = first; f < 15 && LimitBreakerForms.era(f) == era; last = f++) {
         }

         float a0 = angleAt((float)positionOf(first) - 0.42F);
         float a1 = angleAt((float)positionOf(last) + 0.42F);
         int col = LimitBreakerForms.color(first, this.data.rose);
         int steps = (int)((a1 - a0) * (this.ring + 16.0F) / 2.0F);

         for(int s = 0; (float)s <= (float)steps * reveal; ++s) {
            float a = a0 + (a1 - a0) * (float)s / (float)Math.max(1, steps);

            for(int d = 0; d < 2; ++d) {
               float rr = this.ring + 15.0F + (float)d;
               int x = (int)((float)this.cx + Mth.cos(a) * rr);
               int y = (int)((float)this.cy + Mth.sin(a) * rr);
               g.fill(x, y, x + 1, y + 1, WheelArt.argb(160 - d * 64, col));
            }
         }

         for(float a : new float[]{a0, a1}) {
            int x = (int)((float)this.cx + Mth.cos(a) * (this.ring + 15.5F));
            int y = (int)((float)this.cy + Mth.sin(a) * (this.ring + 15.5F));
            g.fill(x - 1, y - 1, x + 2, y + 2, WheelArt.argb(208, col));
         }
      }

      int guide = 120;

      for(int i = 0; (float)i < (float)guide * reveal; ++i) {
         float a = (float)((Math.PI * 2D) * (double)i / (double)guide);
         int x = (int)((float)this.cx + Mth.cos(a) * this.ring);
         int y = (int)((float)this.cy + Mth.sin(a) * this.ring);
         g.fill(x, y, x + 1, y + 1, WheelArt.argb(48, 15250250));
      }

   }

   private void eraLabels(GuiGraphics g) {
      float reveal = Mth.clamp(this.clock * 2.0F - 0.3F, 0.0F, 1.0F);
      if (!(reveal <= 0.05F)) {
         for(int era = 1; era <= 4; ++era) {
            int first = LimitBreakerForms.firstOfEra(era);
            int last = first;

            for(int f = first; f < 15 && LimitBreakerForms.era(f) == era; last = f++) {
            }

            float a = angleAt((float)(positionOf(first) + positionOf(last)) / 2.0F);
            float rr = this.ring + 30.0F;
            int x = (int)((float)this.cx + Mth.cos(a) * rr);
            int y = (int)((float)this.cy + Mth.sin(a) * rr) - 4;
            int col = LimitBreakerForms.color(first, this.data.rose);
            int alpha = (int)(255.0F * reveal);
            int tint = WheelArt.argb(Math.max(8, alpha), WheelArt.mix(col, 16777215, 0.25F));
            int w = this.font.width(ERA_NAME[era]);
            float c = Mth.cos(a);
            int left = c > 0.2F ? x + 2 : (c < -0.2F ? x - w - 2 : x - w / 2);
            g.drawString(this.font, ERA_NAME[era], left, y, tint, true);
         }

      }
   }

   private void mirror(GuiGraphics g, int form, int mouseX, int mouseY, int accent) {
      float footY = (float)this.cy + this.ring * 0.3F;
      float breathe = 0.5F + 0.5F * Mth.sin(this.clock * 2.2F);
      float flash = this.shock >= 0.0F ? Math.max(0.0F, 1.0F - this.shock / 0.35F) : 0.0F;
      g.drawManaged(() -> {
         g.pose().pushPose();
         g.pose().translate((float)this.cx, footY, 0.0F);
         WheelArt.ellipse(g, 38, 11, WheelArt.argb((int)(34.0F + 38.0F * breathe + 96.0F * flash), accent));
         WheelArt.ellipse(g, 30, 8, WheelArt.argb(255, 9071420));
         WheelArt.ellipse(g, 28, 7, WheelArt.argb(240, 722968));
         WheelArt.ellipse(g, 22, 5, WheelArt.argb((int)(56.0F + 48.0F * breathe), accent));

         for(int i = 0; i < 16; ++i) {
            float a = this.clock * 0.8F + (float)((Math.PI * 2D) * (double)i / 16.0D);
            int x = (int)Math.floor((double)(Mth.cos(a) * 33.0F));
            int y = (int)Math.floor((double)(Mth.sin(a) * 9.0F));
            g.fill(x, y, x + 1, y + 1, WheelArt.argb(160, accent));
         }

         g.pose().popPose();
      });
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && !this.previewBroken) {
         float live = Math.max(0.2F, mc.player.getBbHeight() / (mc.player.isCrouching() ? 1.5F : 1.8F));
         int scale = (int)Mth.clamp(this.ring * 0.27F / (live > 1.2F ? live : 1.0F), 6.0F, 30.0F);
         UUID id = mc.player.getUUID();
         Integer saved = ClientLbFormCache.raw(id);
         boolean known = form <= 0 || this.unlocked(form);
         int composite = form <= 0 ? 0 : form | (this.data.rose ? 64 : 0) | (this.data.auraOff ? 128 : 0);
         int m = (int)(this.ring - 8.0F);
         g.enableScissor(this.cx - m, this.cy - m, this.cx + m, this.cy + m);

         try {
            ClientLbFormCache.beginPreview();
            ClientLbFormCache.put(id, composite);
            if (!known) {
               RenderSystem.setShaderColor(0.1F, 0.09F, 0.14F, 1.0F);
            }

            InventoryScreen.renderEntityInInventoryFollowsMouse(g, this.cx, (int)footY, scale, (float)(this.cx - mouseX), footY - (float)scale * 1.6F - (float)mouseY, mc.player);
         } catch (Throwable var21) {
            this.previewBroken = true;
         } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            ClientLbFormCache.restore(id, saved);
            ClientLbFormCache.endPreview();
            g.disableScissor();
         }

         RenderSystem.clear(256, Minecraft.ON_OSX);
         if (!known && form == 14) {
            g.drawCenteredString(this.font, "§8§l?", this.cx, (int)(footY - 34.0F), 16777215);
         }

      } else {
         if (LimitBreakerForms.valid(form)) {
            WheelArt.icon(g, SIGIL[form], (float)this.cx, footY - 20.0F, 2.0F, 1.0F, 1.0F, 1.0F, 1.0F);
         }

      }
   }

   private void thread(GuiGraphics g, FormWheelScreen.View v) {
      float[] p = this.pos(v);
      float sx = (float)this.cx;
      float sy = (float)this.cy + this.ring * 0.3F - 16.0F;
      float dx = p[0] - sx;
      float dy = p[1] - sy;
      float len = Mth.sqrt(dx * dx + dy * dy);
      if (!(len < 1.0F)) {
         float ux = dx / len;
         float uy = dy / len;
         float start = 26.0F;
         float end = len - 12.0F * this.scaleOf(v) - 3.0F;
         if (!(end <= start)) {
            float head = this.clock * 1.3F % 1.0F;
            int color = WheelArt.argb((int)(144.0F * v.hover), v.hue);
            int pulse = WheelArt.mix(v.hue, 16777215, 0.55F);

            for(float d = start; d < end; d += 2.0F) {
               float f = (d - start) / (end - start);
               float glow = Math.max(0.0F, 1.0F - Math.abs(f - head) * 7.0F) * v.hover;
               int x = (int)(sx + ux * d);
               int y = (int)(sy + uy * d);
               if (glow > 0.05F) {
                  g.fill(x - 1, y - 1, x + 1, y + 1, WheelArt.argb((int)(255.0F * glow), pulse));
               } else {
                  g.fill(x, y, x + 1, y + 1, color);
               }
            }

         }
      }
   }

   private int rimOf(FormWheelScreen.View v) {
      if (v.form == this.data.current) {
         return WheelArt.mix(16777215, v.hue, 0.25F + 0.2F * Mth.sin(this.clock * 4.0F));
      } else if (!this.unlocked(v.form)) {
         return 3816002;
      } else {
         return this.poor(v.form) ? 12077116 : WheelArt.mix(9071420, v.hue, 0.45F);
      }
   }

   private void medallion(GuiGraphics g, FormWheelScreen.View v) {
      float s = this.scaleOf(v);
      if (!(s <= 0.01F)) {
         float[] p = this.pos(v);
         float vis = Mth.clamp(v.appear, 0.0F, 1.0F);
         boolean open = this.unlocked(v.form);
         int rim = this.rimOf(v);
         g.pose().pushPose();
         g.pose().translate(p[0], p[1], 0.0F);
         g.pose().scale(s, s, 1.0F);
         if (v.hover > 0.02F) {
            WheelArt.disc(g, 19, WheelArt.argb((int)(38.0F * v.hover), v.hue));
            WheelArt.disc(g, 16, WheelArt.argb((int)(52.0F * v.hover), v.hue));
         }

         if (LimitBreakerForms.apeForm(v.form)) {
            for(int side = -1; side <= 1; side += 2) {
               g.pose().pushPose();
               g.pose().translate((float)side * 9.0F, -10.0F, 0.0F);
               WheelArt.disc(g, 4, WheelArt.argb((int)(255.0F * vis), rim));
               WheelArt.disc(g, 2, WheelArt.argb((int)(230.0F * vis), 722968));
               g.pose().popPose();
            }
         }

         g.pose().pushPose();
         g.pose().translate(1.0F, 1.5F, 0.0F);
         WheelArt.disc(g, 13, WheelArt.argb((int)(112.0F * vis), 0));
         g.pose().popPose();
         WheelArt.annulus(g, 13, 11, WheelArt.argb((int)(255.0F * vis), rim));
         WheelArt.disc(g, 11, WheelArt.argb((int)(230.0F * vis), 722968));
         if (open) {
            WheelArt.disc(g, 9, WheelArt.argb((int)(64.0F * vis), v.hue));
         }

         g.fill(-5, -12, 6, -11, WheelArt.argb((int)(128.0F * vis), 16773836));
         g.pose().popPose();
      }
   }

   private void sigil(GuiGraphics g, FormWheelScreen.View v) {
      float s = this.scaleOf(v);
      if (!(s <= 0.01F)) {
         float[] p = this.pos(v);
         float vis = Mth.clamp(v.appear, 0.0F, 1.0F);
         boolean open = this.unlocked(v.form);
         if (!open && v.form == 14) {
            g.pose().pushPose();
            g.pose().translate(p[0], p[1], 0.0F);
            g.pose().scale(s, s, 1.0F);
            g.drawCenteredString(this.font, "§8?", 0, -4, 16777215);
            g.pose().popPose();
         } else {
            float r = (float)(v.hue >> 16 & 255) / 255.0F;
            float gg = (float)(v.hue >> 8 & 255) / 255.0F;
            float b = (float)(v.hue & 255) / 255.0F;
            if (!open) {
               r = 0.26F;
               gg = 0.26F;
               b = 0.3F;
            } else if (this.poor(v.form)) {
               r *= 0.75F;
               gg *= 0.6F;
               b *= 0.6F;
            }

            WheelArt.icon(g, SIGIL[v.form], p[0], p[1], s, Math.min(1.0F, r * 1.25F), Math.min(1.0F, gg * 1.25F), Math.min(1.0F, b * 1.25F), vis);
         }
      }
   }

   private void overlay(GuiGraphics g, FormWheelScreen.View v) {
      float s = this.scaleOf(v);
      if (!(s <= 0.01F)) {
         float[] p = this.pos(v);
         float vis = Mth.clamp(v.appear, 0.0F, 1.0F);
         g.pose().pushPose();
         g.pose().translate(p[0], p[1], 0.0F);
         g.pose().scale(s, s, 1.0F);
         if (v.form == this.data.current) {
            for(int k = 0; k < 3; ++k) {
               float a = this.clock * 2.6F + (float)v.form - (float)k * 0.22F;
               int x = (int)Math.floor((double)(Mth.cos(a) * 15.0F));
               int y = (int)Math.floor((double)(Mth.sin(a) * 15.0F));
               int al = (int)((float)(224 - k * 80) * vis);
               g.fill(x - 1, y - 1, x + 1, y + 1, WheelArt.argb(al, k == 0 ? 16777215 : v.hue));
            }

            WheelArt.dots(g, 16, 22, -this.clock * 0.6F, WheelArt.argb((int)(112.0F * vis), v.hue));
         }

         if (!this.unlocked(v.form)) {
            g.fill(5, 5, 11, 11, WheelArt.argb((int)(224.0F * vis), 1315866));
            g.fill(6, 8, 10, 11, WheelArt.argb((int)(240.0F * vis), 8026758));
            g.fill(7, 5, 9, 6, WheelArt.argb((int)(240.0F * vis), 8026758));
            g.fill(6, 6, 7, 8, WheelArt.argb((int)(240.0F * vis), 8026758));
            g.fill(9, 6, 10, 8, WheelArt.argb((int)(240.0F * vis), 8026758));
         } else if (this.poor(v.form)) {
            g.fill(6, 6, 11, 11, WheelArt.argb((int)(208.0F * vis), 2755082));
            g.fill(7, 7, 10, 10, WheelArt.argb((int)(240.0F * vis), 12077116));
         }

         g.pose().popPose();
      }
   }

   private void shockwave(GuiGraphics g) {
      if (!(this.shock < 0.0F)) {
         float f = this.shock / 0.7F;
         int r = (int)(30.0F + (this.ring + 30.0F - 30.0F) * f);
         int alpha = (int)(192.0F * (1.0F - f));
         g.pose().pushPose();
         g.pose().translate((float)this.cx, (float)this.cy + this.ring * 0.3F - 16.0F, 0.0F);
         WheelArt.dots(g, r, Math.max(24, r), 0.0F, WheelArt.argb(alpha, this.shockRgb));
         WheelArt.dots(g, Math.max(1, r - 3), Math.max(24, r), 0.3F, WheelArt.argb(alpha / 2, 16777215));
         g.pose().popPose();
      }
   }

   private void motes(GuiGraphics g, boolean front) {
      for(FormWheelScreen.Mote m : this.motes) {
         if (m.front == front) {
            float f = Mth.clamp(m.life / m.max, 0.0F, 1.0F);
            int x = (int)m.x;
            int y = (int)m.y;
            g.fill(x, y, x + m.size, y + m.size, WheelArt.argb((int)(224.0F * f), m.rgb));
         }
      }

   }

   private void header(GuiGraphics g, int accent) {
      String title = "§6§lTHE LADDER";
      String now = LimitBreakerForms.valid(this.data.current) ? "§8   ·   §f" + LimitBreakerForms.displayName(this.data.current, this.data.rose) : "§8   ·   §7base";
      String sub = "§7Level §6" + this.data.level + "§8   ·   §7Grit §6" + this.data.grit + now;
      int ty = Math.max(8, (int)((float)this.cy - this.ring - 50.0F));
      int w = Math.max(this.font.width(title), this.font.width(sub)) / 2 + 16;
      g.fill(this.cx - w, ty - 6, this.cx + w, ty + 23, WheelArt.argb(184, 722968));
      g.fill(this.cx - w, ty - 6, this.cx + w, ty - 5, WheelArt.argb(224, accent));
      g.fill(this.cx - w, ty + 22, this.cx + w, ty + 23, WheelArt.argb(224, accent));
      g.fill(this.cx - w - 3, ty + 7, this.cx - w, ty + 10, WheelArt.argb(224, accent));
      g.fill(this.cx + w, ty + 7, this.cx + w + 3, ty + 10, WheelArt.argb(224, accent));
      g.drawCenteredString(this.font, title, this.cx, ty, 16777215);
      g.drawCenteredString(this.font, sub, this.cx, ty + 11, 16777215);
   }

   private static String times(double v) {
      return "x" + (double)Math.round(v * 100.0D) / 100.0D;
   }

   private void info(GuiGraphics g, FormWheelScreen.View focus) {
      int y0 = (int)((float)this.cy + this.ring + 22.0F);
      if (focus == null) {
         g.drawCenteredString(this.font, "§8Pick a form. Release returns you to base.", this.cx, y0, 16777215);
      } else {
         int hovered = focus.form;
         String name = LimitBreakerForms.displayName(hovered, this.data.rose);
         int era = LimitBreakerForms.era(hovered);
         boolean secret = !this.unlocked(hovered) && hovered == 14;
         g.drawCenteredString(this.font, secret ? "§8§l? ? ?" : "§f§l" + name, this.cx, y0, 16777215);
         String line2;
         if (hovered == this.data.current) {
            line2 = "§7held now";
         } else if (this.unlocked(hovered)) {
            int cost = this.data.costs != null && hovered < this.data.costs.length ? this.data.costs[hovered] : 0;
            line2 = "§7" + ERA_NAME[era] + "§8: " + (this.poor(hovered) ? "§c" : "§6") + cost + " grit" + (this.poor(hovered) ? "§8 (you hold " + this.data.grit + ")" : "");
         } else if (secret) {
            line2 = "§8beyond every trial, past level " + LimitBreakerForms.LEVEL_REQ[hovered];
         } else {
            line2 = "§8locked§8: " + ERA_NAME[era] + ", level " + LimitBreakerForms.LEVEL_REQ[hovered];
         }

         g.drawCenteredString(this.font, line2, this.cx, y0 + 12, 16777215);
         if (!secret) {
            String line3 = "§7power §6" + times(LimitBreakerForms.POWER[hovered]) + "§8   ·   §7upkeep §6" + times(LimitBreakerForms.UPKEEP_MULT[hovered]) + (LimitBreakerForms.apeForm(hovered) ? "§8   ·   §7beast" : "");
            g.drawCenteredString(this.font, line3, this.cx, y0 + 24, 16777215);
         }

      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   static {
      for(int f = 1; f < 15; ++f) {
         SIGIL[f] = new ResourceLocation("unrp", "textures/gui/lb_forms/sigil_" + f + ".png");
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
      final int form;
      final int hue;
      float angle;
      float tAngle;
      float rad;
      float tRad;
      float appear;
      float hover;
      float delay;
      float shake;
      boolean pulled;

      View(int form, int hue) {
         this.form = form;
         this.hue = hue;
      }
   }
}
