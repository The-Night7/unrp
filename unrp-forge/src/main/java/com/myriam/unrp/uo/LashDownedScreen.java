package com.myriam.unrp.uo;



import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class LashDownedScreen extends Screen {
   static final int FADE_TOP = 1615855616;
   static final int FADE_BOTTOM = -1602211792;
   static final int BAR_W = 160;
   static final int FRESH_MS = 3000;
   private static volatile int phase;
   private static volatile int left;
   private static volatile int score;
   private static volatile long at;
   private static volatile long seenMs;
   private static volatile long titleAt = -1000L;
   static final int TITLE_TICKS = 44;
   private static boolean confirming;
   private int delay;
   private boolean gaveUp;
   private Button giveUp;
   private Button quit;

   public LashDownedScreen() {
      super(Component.translatable("deathScreen.title"));
   }

   public static void accept(int phaseNow, int leftNow, int total, int scoreNow) {
      Minecraft mc = Minecraft.getInstance();
      phase = phaseNow;
      left = leftNow;
      score = scoreNow;
      at = mc.level == null ? 0L : mc.level.getGameTime();
      seenMs = System.currentTimeMillis();
      if (phaseNow == 1) {
         if (mc.player != null && !(mc.screen instanceof LashDownedScreen) && !confirming) {
            mc.setScreen(new LashDownedScreen());
         }

      } else {
         confirming = false;
         if (mc.screen instanceof LashDownedScreen) {
            mc.setScreen((Screen)null);
         }

         if (phaseNow == 2) {
            titleAt = at;
            mc.gui.setTimes(4, 30, 10);
            mc.gui.setSubtitle(Component.literal("held together").withStyle((st) -> st.withColor(15259903)));
            mc.gui.setTitle(Component.literal("SECOND WIND").withStyle((st) -> st.withColor(16747176)));
         }

      }
   }

   public static boolean titleShowing(long now) {
      return now >= titleAt && now - titleAt < 44L;
   }

   public static boolean showing() {
      return phase == 1 && System.currentTimeMillis() - seenMs < 3000L;
   }

   static int leftNow() {
      Minecraft mc = Minecraft.getInstance();
      long now = mc.level == null ? at : mc.level.getGameTime();
      return (int)Math.max(0L, (long)left - Math.max(0L, now - at));
   }

   protected void init() {
      this.giveUp = (Button)this.addRenderableWidget(Button.builder(Component.literal("Respawn (give up)"), (b) -> {
         this.gaveUp = true;
         b.active = false;
         com.myriam.unrp.net.Net.CHANNEL.sendToServer(new LashGiveUpPacket());
      }).bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build());
      this.quit = (Button)this.addRenderableWidget(Button.builder(Component.translatable("deathScreen.titleScreen"), (b) -> this.askQuit()).bounds(this.width / 2 - 100, this.height / 4 + 96, 200, 20).build());
      this.arm();
   }

   private void arm() {
      boolean on = this.delay >= 20;
      if (this.giveUp != null) {
         this.giveUp.active = on && !this.gaveUp;
      }

      if (this.quit != null) {
         this.quit.active = on;
      }

   }

   private void askQuit() {
      Minecraft mc = this.minecraft;
      if (mc != null) {
         confirming = true;
         ConfirmScreen confirm = new ConfirmScreen((yes) -> {
            confirming = false;
            if (yes) {
               if (mc.level != null) {
                  mc.level.disconnect();
               }

               mc.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
               mc.setScreen(new TitleScreen());
            } else {
               mc.setScreen(showing() ? new LashDownedScreen() : null);
            }

         }, Component.translatable("deathScreen.quit.confirm"), CommonComponents.EMPTY, Component.translatable("deathScreen.titleScreen"), CommonComponents.GUI_BACK);
         mc.setScreen(confirm);
         confirm.setDelay(20);
      }
   }

   public void tick() {
      ++this.delay;
      if (this.delay == 20) {
         this.arm();
      }

      if (!showing() && this.minecraft != null) {
         this.minecraft.setScreen((Screen)null);
      }

   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      g.fillGradient(0, 0, this.width, this.height, 1615855616, -1602211792);
      g.pose().pushPose();

      try {
         g.pose().scale(2.0F, 2.0F, 2.0F);
         g.drawCenteredString(this.font, this.title, this.width / 2 / 2, 30, 16777215);
      } finally {
         g.pose().popPose();
      }

      int l = leftNow();
      int secs = LashMath.downedSecs(l);
      String text = secs > 0 ? "The tendrils hold you together... " + secs : "Get up";
      g.drawCenteredString(this.font, text, this.width / 2, 85, 13153535);
      int x0 = this.width / 2 - 80;
      int y = 97;
      int lit = (int)Math.round(160.0D * LashMath.clamp01(LashMath.downedK(l)));
      g.fill(x0 - 1, y - 1, x0 + 160 + 1, y + 5, -434894306);
      g.fill(x0, y, x0 + lit, y + 4, -6523649);
      g.fill(x0, y, x0 + lit, y + 1, -2569985);
      Component scoreLine = Component.translatable("deathScreen.score").append(": ").append(Component.literal(Integer.toString(score)).withStyle(ChatFormatting.YELLOW));
      g.drawCenteredString(this.font, scoreLine, this.width / 2, 112, 16777215);
      super.render(g, mouseX, mouseY, partialTick);
   }
}
