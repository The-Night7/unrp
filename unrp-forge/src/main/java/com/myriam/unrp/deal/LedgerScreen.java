package com.myriam.unrp.deal;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class LedgerScreen extends Screen {
   private static final int WIN_W = 360;
   private static final int WIN_H = 230;
   private static final int ROWS = 9;
   private LedgerViewPacket data;
   private int winX;
   private int winY;
   private int page;
   private UUID selected;
   private int termPage;

   public LedgerScreen(LedgerViewPacket data) {
      super(Component.literal("The Ledger"));
      this.data = data;
   }

   public static void open(LedgerViewPacket msg) {
      Minecraft mc = Minecraft.getInstance();
      Screen var3 = mc.screen;
      if (var3 instanceof LedgerScreen ledger) {
         ledger.refresh(msg);
      } else {
         mc.setScreen(new LedgerScreen(msg));
      }

   }

   private void refresh(LedgerViewPacket msg) {
      this.data = msg;
      if (this.selectedEntry() == null) {
         this.selected = null;
         this.termPage = 0;
      }

      this.rebuild();
   }

   private LedgerViewPacket.Entry selectedEntry() {
      if (this.selected == null) {
         return null;
      } else {
         for(LedgerViewPacket.Entry e : this.data.entries) {
            if (this.selected.equals(e.pactId)) {
               return e;
            }
         }

         return null;
      }
   }

   protected void init() {
      this.winX = (this.width - 360) / 2;
      this.winY = (this.height - 230) / 2;
      this.rebuild();
   }

   private void rebuild() {
      this.clearWidgets();
      int maxPage = Math.max(0, (this.data.entries.size() - 1) / 9);
      if (this.page > maxPage) {
         this.page = maxPage;
      }

      int listX = this.winX + 8;
      int listY = this.winY + 34;

      for(int i = 0; i < 9; ++i) {
         int idx = this.page * 9 + i;
         if (idx >= this.data.entries.size()) {
            break;
         }

         LedgerViewPacket.Entry e = (LedgerViewPacket.Entry)this.data.entries.get(idx);
         String label = (e.proposed ? "§6" : (e.soul ? "§b" : (e.signed ? "§6" : "§7"))) + e.label;
         if (e.ritual > 0) {
            label = "§4! " + label;
         }

         boolean isSel = e.pactId.equals(this.selected);
         String shown = "  " + (isSel ? "§f> " : "") + label;
         this.addRenderableWidget(new StyledButton(listX, listY + i * 19, 150, 17, Component.literal(shown), (b) -> {
            this.selected = e.pactId;
            this.termPage = 0;
            this.rebuild();
         }));
      }

      if (this.page > 0) {
         this.addRenderableWidget(new StyledButton(listX, this.winY + 230 - 24, 40, 16, Component.literal("<"), (b) -> {
            --this.page;
            this.rebuild();
         }));
      }

      if ((this.page + 1) * 9 < this.data.entries.size()) {
         this.addRenderableWidget(new StyledButton(listX + 44, this.winY + 230 - 24, 40, 16, Component.literal(">"), (b) -> {
            ++this.page;
            this.rebuild();
         }));
      }

      this.addRenderableWidget(new StyledButton(listX + 110, this.winY + 230 - 24, 48, 16, Component.literal("Refresh"), (b) -> com.myriam.unrp.net.Net.CHANNEL.sendToServer(new LedgerActionPacket(1, new UUID(0L, 0L)))));
      LedgerViewPacket.Entry sel = this.selectedEntry();
      if (sel != null && sel.proposed) {
         this.addRenderableWidget(new StyledButton(this.winX + 170, this.winY + 230 - 24, 90, 16, Component.literal("§6Answer petition"), (b) -> com.myriam.unrp.net.Net.CHANNEL.sendToServer(new LedgerActionPacket(2, sel.pactId))));
      } else if (sel != null) {
         this.addRenderableWidget(new StyledButton(this.winX + 170, this.winY + 230 - 24, 90, 16, Component.literal("§6Summon to hand"), (b) -> com.myriam.unrp.net.Net.CHANNEL.sendToServer(new LedgerActionPacket(0, sel.pactId))));
         if (sel.terms.size() > 11) {
            this.addRenderableWidget(new StyledButton(this.winX + 266, this.winY + 230 - 24, 20, 16, Component.literal("v"), (b) -> {
               int maxTerm = Math.max(0, (sel.terms.size() - 1) / 11);
               this.termPage = this.termPage >= maxTerm ? 0 : this.termPage + 1;
            }));
         }
      }

      this.addRenderableWidget(new StyledButton(this.winX + 360 - 56, this.winY + 230 - 24, 48, 16, Component.literal("Close"), (b) -> this.onClose()));
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(g);
      g.fill(this.winX - 2, this.winY - 2, this.winX + 360 + 2, this.winY + 230 + 2, -401601528);
      g.fill(this.winX, this.winY, this.winX + 360, this.winY + 230, -266463475);
      g.fill(this.winX + 164, this.winY + 30, this.winX + 165, this.winY + 230 - 28, 1625867114);
      g.drawString(this.font, "§6§lTHE LEDGER§8: every page you own", this.winX + 8, this.winY + 8, 16777215, true);
      g.drawString(this.font, "§7" + this.data.entries.size() + " contract" + (this.data.entries.size() == 1 ? "" : "s") + " bear your name.", this.winX + 8, this.winY + 20, 16777215, true);
      if (this.data.entries.isEmpty()) {
         g.drawString(this.font, "§8No contracts bear your name yet.", this.winX + 12, this.winY + 40, 16777215, true);
      }

      LedgerViewPacket.Entry sel = this.selectedEntry();
      int rx = this.winX + 170;
      if (sel == null) {
         g.drawString(this.font, "§8Pick a page to read it.", rx, this.winY + 40, 16777215, true);
      } else {
         g.drawString(this.font, (sel.soul ? "§b" : "§6") + trim(sel.label, 30), rx, this.winY + 34, 16777215, true);
         g.drawString(this.font, "§8" + trim(sel.where.isEmpty() ? "Whereabouts unknown." : sel.where, 30), rx, this.winY + 46, 16777215, true);
         if (sel.ritual > 0) {
            g.drawString(this.font, "§4A destruction ritual is underway: step " + sel.ritual + " of 3.", rx, this.winY + 58, 16777215, true);
         }

         int y = this.winY + 72;
         int start = this.termPage * 11;
         if (sel.terms.isEmpty()) {
            g.drawString(this.font, "§8No terms written.", rx, y, 16777215, true);
         }

         for(int i = 0; i < 11 && start + i < sel.terms.size(); ++i) {
            g.drawString(this.font, "§7" + trim((String)sel.terms.get(start + i), 31), rx, y + i * 11, 16777215, true);
         }

         if (sel.terms.size() > 11) {
            g.drawString(this.font, "§8" + (this.termPage * 11 + 1) + "-" + Math.min(sel.terms.size(), this.termPage * 11 + 11) + " / " + sel.terms.size(), rx + 120, this.winY + 60, 16777215, true);
         }
      }

      super.render(g, mouseX, mouseY, partialTick);
      int listY = this.winY + 34;

      for(int i = 0; i < 9; ++i) {
         int idx = this.page * 9 + i;
         if (idx >= this.data.entries.size()) {
            break;
         }

         g.renderItem(iconFor((LedgerViewPacket.Entry)this.data.entries.get(idx)), this.winX + 9, listY + i * 19 + 1);
      }

   }

   private static ItemStack iconFor(LedgerViewPacket.Entry e) {
      ItemStack stack = new ItemStack((ItemLike)com.myriam.unrp.item.ModItems.CONTRACT.get());
      CompoundTag tag = stack.getOrCreateTag();
      if (e.soul) {
         tag.putBoolean("unrp_pact_soul", true);
      }

      if (e.proposed) {
         tag.putBoolean("unrp_pact_proposed", true);
      }

      if (e.signed) {
         tag.putBoolean("unrp_pact_signed", true);
      }

      return stack;
   }

   private static String trim(String s, int max) {
      return s.length() > max ? s.substring(0, max) : s;
   }

   public boolean isPauseScreen() {
      return false;
   }
}
