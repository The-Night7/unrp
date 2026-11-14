package com.myriam.unrp.uo;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RuneCodexScreen extends Screen {
   private static final String[] ROW_LABELS = new String[]{"Blink", "Void Gaze", "Gale Slam", "Bone Darts", "Grasp", "Briar", "Gray Veil", "Thrall", "Sympathy", "Shade Step", "Calcify", "Doppel", "Bend Time", "Void Gift"};
   private static final String[] ROW_TEXT = new String[]{"Step through the grey. Longer, then cleaner. Later, through thin walls.", "Shapes glow through stone. Further, then their vitals with them.", "A cone of dead air that shoves, and turns arrows back the way they came.", "Splinters from the fingertips. More of them, and they stop caring about armor.", "Take hold at a distance. Then of people, then hard enough to throw.", "Petrified thorns rise from the ash and keep the ground.", "A grey cloud that blinds foes inside it.", "Bend a beast to your side. Then men and the walking dead, then more.", "Thread foes together, so a wound to one is a wound to all of them.", "Stand still in the grey and it hides you. Walk softly and it keeps you.", "Still a foe as stone. Then men slow, then the stone shatters and spreads.", "A double in your skin walks off and draws their eyes. Then it blinks, then two.", "Slow the hour around you. Then stop it, then hold it longer.", "Lend an ally a charm you wear. Then two at once, then those beside them."};
   private static final String[] STAGE_NAME = new String[]{"", "Marked", "Favored", "Chosen", "Hollow"};
   private static final String[] PAGE_NAME = new String[]{"First Favors", "Deep Favors", "Charms"};
   private static final String[] PAGE_FOOT = new String[]{"Runes are destroyed here. Nothing else pays.", "Deep favors ride the bar's second page (X).", "A released charm returns to your pack."};
   private static final int FIRST_ROWS = 7;
   private static final int CHARM_PAGE = 2;
   private static final int CHARM_ROWS = 6;
   private static final int WIN_W = 300;
   private static final int WIN_H = 236;
   private static final int ROW_TOP = 52;
   private static final int BACK = -267645424;
   private static final int EDGE = -12959160;
   private static final int ASH = -6643546;
   private static final int BONE = -1514278;
   private static final int GLYPH = -8395800;
   private static final int DIM = -10524048;
   private static CodexSyncPacket data;
   private static int page;
   private int winX;
   private int winY;
   private final List<Button> frees = new ArrayList();
   private final int[] shown = new int[6];

   public RuneCodexScreen() {
      super(Component.literal("The Codex"));
   }

   public static void open(CodexSyncPacket msg) {
      data = msg;
      Minecraft mc = Minecraft.getInstance();
      if (!(mc.screen instanceof RuneCodexScreen)) {
         mc.setScreen(new RuneCodexScreen());
      }
   }

   private static int from() {
      return page == 0 ? 0 : 7;
   }

   private static int to() {
      return page == 0 ? 7 : 14;
   }

   private static int charmAt(int slot) {
      if (data == null) {
         return -1;
      } else {
         int seen = 0;

         for(int k = 0; k < BoneCharmItem.KINDS.length; ++k) {
            if ((data.charms & 1 << k) != 0) {
               if (seen == slot) {
                  return k;
               }

               ++seen;
            }
         }

         return -1;
      }
   }

   protected void init() {
      this.winX = (this.width - 300) / 2;
      this.winY = (this.height - 236) / 2;
      com.myriam.unrp.net.Net.CHANNEL.sendToServer(new CodexActionPacket(0, 0));
      this.addRenderableWidget(Button.builder(Component.literal("Close"), (b) -> this.onClose()).bounds(this.winX + 300 - 50, this.winY + 236 - 20, 44, 14).build());

      for(int p0 = 0; p0 < PAGE_NAME.length; ++p0) {
         final int p = p0;
         Button tab = Button.builder(Component.literal(PAGE_NAME[p]), (b) -> {
            page = p;
            this.rebuildWidgets();
         }).bounds(this.winX + 10 + p * 94, this.winY + 31, 90, 14).build();
         tab.active = page != p;
         this.addRenderableWidget(tab);
      }

      this.frees.clear();
      Arrays.fill(this.shown, -1);
      if (page == 2) {
         for(int i0 = 0; i0 < 6; ++i0) {
            final int i = i0;
            Button free = Button.builder(Component.literal("Release"), (b) -> {
               int kind = this.shown[i];
               if (kind >= 0) {
                  this.shown[i] = -1;
                  com.myriam.unrp.net.Net.CHANNEL.sendToServer(new CodexActionPacket(2, kind));
               }

            }).bounds(this.winX + 300 - 56, this.winY + 52 + 4 + i * 22, 46, 14).build();
            free.visible = false;
            this.frees.add(free);
            this.addRenderableWidget(free);
         }

      } else {
         for(int i0 = from(); i0 < to(); ++i0) {
            final int i = i0;
            this.addRenderableWidget(Button.builder(Component.literal("Cut"), (b) -> com.myriam.unrp.net.Net.CHANNEL.sendToServer(new CodexActionPacket(1, i))).bounds(this.winX + 300 - 46, this.winY + 52 + 4 + (i - from()) * 22, 36, 14).build());
         }

      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(g);
      g.fill(this.winX - 1, this.winY - 1, this.winX + 300 + 1, this.winY + 236 + 1, -12959160);
      g.fill(this.winX, this.winY, this.winX + 300, this.winY + 236, -267645424);

      for(int i = 0; i < 300; i += 6) {
         g.fill(this.winX + i, this.winY + 26, this.winX + i + 3, this.winY + 27, 820569306);
      }

      g.drawString(this.font, "THE CODEX", this.winX + 10, this.winY + 8, -1514278, false);
      if (data == null) {
         g.drawString(this.font, "The brand is cold.", this.winX + 10, this.winY + 52, -6643546, false);
         super.render(g, mouseX, mouseY, partialTick);
      } else {
         String head = "Runes held: " + data.runes + "   Spent: " + data.spent + "   " + STAGE_NAME[Math.max(1, Math.min(4, data.stage))];
         g.drawString(this.font, head, this.winX + 300 - 10 - this.font.width(head), this.winY + 8, -8395800, false);
         if (page == 2) {
            this.renderCharms(g, mouseX, mouseY, partialTick);
         } else {
            int hoverRow = -1;

            for(int i = from(); i < to() && i < data.ranks.length; ++i) {
               int y = this.winY + 52 + (i - from()) * 22;
               int rank = data.ranks[i];
               int cost = data.costs[i];
               boolean owned = rank > 0;
               boolean afford = cost <= data.runes;
               g.fill(this.winX + 8, y - 2, this.winX + 300 - 52, y + 18, owned ? 545252328 : 419430399);
               g.drawString(this.font, ROW_LABELS[i], this.winX + 12, y + 1, owned ? -1514278 : -6643546, false);
               g.drawString(this.font, "rank " + rank, this.winX + 92, y + 1, owned ? -8395800 : -10524048, false);
               String mile_0 = HollowMarkEvents.nextMile(i, rank);
               String blurb = mile_0.isEmpty() ? ROW_TEXT[i] : "next: " + mile_0;
               int blurbMax = 228;
               if (this.font.width(blurb) > blurbMax) {
                  blurb = this.font.plainSubstrByWidth(blurb, blurbMax - 6) + "..";
               }

               g.drawString(this.font, blurb, this.winX + 12, y + 10, mile_0.isEmpty() ? -10524048 : -11093824, false);
               String price = cost + " rune" + (cost == 1 ? "" : "s");
               g.drawString(this.font, price, this.winX + 300 - 100, y + 1, afford ? -1514278 : -7714230, false);
               if (mouseX >= this.winX + 8 && mouseX < this.winX + 300 - 52 && mouseY >= y - 2 && mouseY < y + 18) {
                  hoverRow = i;
               }
            }

            g.drawString(this.font, PAGE_FOOT[page], this.winX + 10, this.winY + 236 - 17, -10524048, false);
            super.render(g, mouseX, mouseY, partialTick);
            if (hoverRow >= 0) {
               List<Component> lines = new ArrayList();
               lines.add(Component.literal("§f" + ROW_LABELS[hoverRow] + " §8(grows with every rank, forever)"));
               lines.add(Component.literal("§7" + ROW_TEXT[hoverRow]));
               String mile = HollowMarkEvents.nextMile(hoverRow, data.ranks[hoverRow]);
               if (!mile.isEmpty()) {
                  lines.add(Component.literal("§3next milestone: §b" + mile));
               }

               g.renderComponentTooltip(this.font, lines, mouseX, mouseY);
            }

         }
      }
   }

   private void renderCharms(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      int hover = -1;
      int worn = 0;

      for(int i = 0; i < 6 && i < this.frees.size(); ++i) {
         int kind = charmAt(i);
         this.shown[i] = kind;
         ((Button)this.frees.get(i)).visible = kind >= 0;
         if (kind >= 0) {
            ++worn;
            int y = this.winY + 52 + i * 22;
            boolean corrupt = BoneCharmItem.corrupted(BoneCharmItem.KINDS[kind]);
            g.fill(this.winX + 8, y - 2, this.winX + 300 - 60, y + 18, corrupt ? 548421696 : 545252328);
            g.drawString(this.font, BoneCharmItem.KIND_NAME[kind], this.winX + 12, y + 1, corrupt ? -4165520 : -1514278, false);
            String text = BoneCharmItem.KIND_TEXT[kind];
            int max = 220;
            if (this.font.width(text) > max) {
               text = this.font.plainSubstrByWidth(text, max - 6) + "..";
            }

            g.drawString(this.font, text, this.winX + 12, y + 10, -10524048, false);
            if (mouseX >= this.winX + 8 && mouseX < this.winX + 300 - 60 && mouseY >= y - 2 && mouseY < y + 18) {
               hover = kind;
            }
         }
      }

      if (worn == 0) {
         g.drawString(this.font, "No charm is attuned yet.", this.winX + 12, this.winY + 52 + 1, -6643546, false);
      }

      g.drawString(this.font, "Attuned " + worn + " of " + data.charmCap, this.winX + 12, this.winY + 52 + 132 + 4, -8395800, false);
      g.drawString(this.font, PAGE_FOOT[page], this.winX + 10, this.winY + 236 - 17, -10524048, false);
      super.render(g, mouseX, mouseY, partialTick);
      if (hover >= 0) {
         List<Component> lines = new ArrayList();
         lines.add(Component.literal("§f" + BoneCharmItem.KIND_NAME[hover]));
         lines.add(Component.literal("§7" + BoneCharmItem.KIND_TEXT[hover]));
         if (!BoneCharmItem.KIND_COST[hover].isEmpty()) {
            lines.add(Component.literal("§4" + BoneCharmItem.KIND_COST[hover]));
         }

         g.renderComponentTooltip(this.font, lines, mouseX, mouseY);
      }

   }
}
