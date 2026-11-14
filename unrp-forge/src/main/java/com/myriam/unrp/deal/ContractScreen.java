package com.myriam.unrp.deal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ContractScreen extends Screen {
   private static final int WIN_W = 360;
   private static final int WIN_H = 230;
   private static final String[][] CATS = new String[][]{{"hearts", "Hearts"}, {"attr", "Attributes"}, {"effects", "Statuses"}, {"items", "Items"}, {"soul", "Soul"}, {"rules", "Rules"}, {"protect", "Protection"}, {"tribute", "Tribute"}, {"restrict", "Restrictions"}, {"info", "Information"}, {"identity", "Identity"}, {"ability", "Ability"}, {"boons", "Boons"}, {"curses", "Curses"}, {"cond", "Conditions"}, {"binds", "Binds"}, {"punish", "Punishment"}, {"text", "Text"}};
   private static final String[][] BOONS = new String[][]{{"immortal", "Immortality"}, {"projection", "Projection"}, {"echo", "The Echo"}, {"adapt", "Adaptation"}, {"travel", "Travel"}, {"amplify", "Amplification"}, {"overflow", "Overflow"}, {"lucky", "The Lucky"}, {"allied", "The Allied"}};
   private static final String[][] CURSES = new String[][]{{"gills", "Gills"}, {"uninvited", "The Uninvited"}, {"greed", "Greed"}, {"betrayal", "Betrayal"}, {"decay", "Decay"}, {"repetition", "Repetition"}, {"witnessing", "Witnessing"}, {"knowledge", "Knowledge"}, {"wounds", "Open Wounds"}, {"dampen", "Dampening"}, {"vanishing", "Vanishing"}, {"binding", "Binding"}};
   private static final String[][] PUNISHMENTS = new String[][]{{"soul", "Strip Soul"}, {"death", "Death"}, {"possessions", "Strip Possessions"}, {"contract", "New Contract"}, {"xp", "Strip Experience"}, {"levels", "Strip Levels"}, {"seal", "Seal Ability"}};
   private static final String[][] ATTRS = new String[][]{{"atk", "Attack Damage x", "0", "10"}, {"atkspd", "Attack Speed x", "0", "10"}, {"armor", "Armor +", "0", "30"}, {"armorlose", "Armor -", "0", "30"}, {"tough", "Toughness +", "0", "20"}, {"toughlose", "Toughness -", "0", "20"}, {"speed", "Move Speed x", "0", "5"}, {"kb", "Knockback x", "0", "5"}, {"kbres", "KB Resist 0-1", "0", "1"}, {"reach", "Reach blocks", "0", "15"}, {"mine", "Mining Speed x", "0", "30"}, {"jump", "Jump Height x", "0", "10"}, {"grav", "Gravity x", "-5", "5"}, {"size", "Size x", "0.1", "5"}, {"dmgred", "Dmg Reduction %", "0", "100"}};
   private static final String[][] RULES = new String[][]{{"keepinv", "Keep Inventory"}, {"regen", "Natural Regen"}, {"hunger", "Hunger Depletion"}, {"durability", "Durability"}, {"insomnia", "Insomnia"}, {"sleep", "Sleep"}, {"deathmsg", "Death Messages"}};
   private static final String[] DMG_TYPES = new String[]{"fire", "fall", "explosion", "projectile", "drown", "freeze", "lightning", "magic", "physical"};
   private PactViewPacket data;
   private int winX;
   private int winY;
   private int termPage;
   private String cat = "";
   private String sub = "";
   private String pickedId = "";
   private int pickerPage;
   private EditBox valueBox;
   private EditBox textBox;
   private EditBox searchBox;
   private EditBox x1Box;
   private EditBox z1Box;
   private EditBox x2Box;
   private EditBox z2Box;
   private EditBox dimBox;
   private boolean confirmDestroy;
   private boolean confirmSubmit;
   private boolean confirmSeal;
   private int pendingSide;
   private String numberLabel = "";
   private String pickerFilter = "";
   private boolean needsRefresh;

   public ContractScreen(PactViewPacket data) {
      super(Component.literal("Contract"));
      this.data = data;
   }

   public static void open(PactViewPacket msg) {
      Minecraft mc = Minecraft.getInstance();
      mc.setScreen(new ContractScreen(msg));
   }

   private boolean editing() {
      return this.data.mode == 0 || this.data.mode == 4;
   }

   private boolean proposing() {
      return this.data.mode == 4;
   }

   protected void init() {
      this.winX = (this.width - 360) / 2;
      this.winY = (this.height - 230) / 2;
      this.rebuild();
   }

   private void clear() {
      this.clearWidgets();
      this.valueBox = null;
      this.textBox = null;
      this.searchBox = null;
      this.x1Box = null;
      this.z1Box = null;
      this.x2Box = null;
      this.z2Box = null;
      this.dimBox = null;
   }

   private void rebuild() {
      this.clear();
      int lx = this.winX + 8;
      int listBottom = this.winY + 230 - 22;
      int perPage = this.termsPerPage();
      int pages = Math.max(1, (this.data.terms.size() + perPage - 1) / perPage);
      if (this.termPage >= pages) {
         this.termPage = pages - 1;
      }

      if (pages > 1) {
         this.addRenderableWidget(Button.builder(Component.literal("<"), (b) -> {
            this.termPage = Math.max(0, this.termPage - 1);
            this.rebuild();
         }).bounds(lx, listBottom, 16, 14).build());
         this.addRenderableWidget(Button.builder(Component.literal(">"), (b) -> {
            this.termPage = Math.min(pages - 1, this.termPage + 1);
            this.rebuild();
         }).bounds(lx + 20, listBottom, 16, 14).build());
      }

      if (this.editing()) {
         int start = this.termPage * perPage;

         for(int i = 0; i < perPage && start + i < this.data.terms.size(); ++i) {
            int idx = start + i;
            if (this.data.mode != 0 || !((PactLedger.Term)this.data.terms.get(idx)).locked) {
               this.addRenderableWidget(Button.builder(Component.literal("x"), (b) -> com.myriam.unrp.net.Net.CHANNEL.sendToServer(new PactEditPacket(1, this.data.pactId, idx, (PactLedger.Term)null))).bounds(lx, this.winY + 34 + i * 13, 12, 12).build());
            }
         }
      }

      if (this.data.mode == 4) {
         this.addRenderableWidget(Button.builder(Component.literal(this.confirmSubmit ? "§4PRE-SIGN? Are you sure?" : "Submit & Pre-sign"), (b) -> {
            if (!this.confirmSubmit) {
               this.confirmSubmit = true;
               this.rebuild();
            } else {
               com.myriam.unrp.net.Net.CHANNEL.sendToServer(new PactEditPacket(4, this.data.pactId, 0, (PactLedger.Term)null));
               this.onClose();
            }
         }).bounds(this.winX + 360 - 190, this.winY + 230 - 22, 182, 16).build());
      }

      if (this.data.mode == 0 && this.data.proposed && this.data.submitted) {
         this.addRenderableWidget(Button.builder(Component.literal(this.confirmSeal ? "§6SEAL: it binds at once. Sure?" : "Seal the Pact"), (b) -> {
            if (!this.confirmSeal) {
               this.confirmSeal = true;
               this.rebuild();
            } else {
               com.myriam.unrp.net.Net.CHANNEL.sendToServer(new PactEditPacket(5, this.data.pactId, 0, (PactLedger.Term)null));
            }
         }).bounds(this.winX + 360 - 190, this.winY + 230 - 22, 182, 16).build());
      }

      if (this.data.mode == 1) {
         this.addRenderableWidget(Button.builder(Component.literal("SIGN"), (b) -> com.myriam.unrp.net.Net.CHANNEL.sendToServer(new PactEditPacket(2, this.data.pactId, 0, (PactLedger.Term)null))).bounds(this.winX + 360 - 130, this.winY + 230 - 22, 60, 16).build());
         this.addRenderableWidget(Button.builder(Component.literal("Refuse"), (b) -> this.onClose()).bounds(this.winX + 360 - 66, this.winY + 230 - 22, 58, 16).build());
      }

      if (this.data.mode == 2) {
         this.addRenderableWidget(Button.builder(Component.literal(this.confirmDestroy ? "DESTROY? Are you sure?" : "Destroy Contract"), (b) -> {
            if (!this.confirmDestroy) {
               this.confirmDestroy = true;
               this.rebuild();
            } else {
               com.myriam.unrp.net.Net.CHANNEL.sendToServer(new PactEditPacket(3, this.data.pactId, 0, (PactLedger.Term)null));
               this.onClose();
            }
         }).bounds(this.winX + 360 - 168, this.winY + 230 - 22, 160, 16).build());
      }

      this.addRenderableWidget(Button.builder(Component.literal("Close"), (b) -> this.onClose()).bounds(this.winX + 360 - 50, this.winY + 6, 44, 14).build());
      if (this.editing()) {
         this.buildEditor();
      }

   }

   private int termsPerPage() {
      return 12;
   }

   private void buildEditor() {
      int rx = this.winX + 196;
      int ry = this.winY + 24;
      if (this.cat.isEmpty()) {
         for(int i = 0; i < CATS.length; ++i) {
            String key_0 = CATS[i][0];
            this.addRenderableWidget(Button.builder(Component.literal(CATS[i][1]), (b) -> {
               this.cat = key_0;
               this.sub = "";
               this.pickedId = "";
               this.pickerPage = 0;
               this.rebuild();
            }).bounds(rx + i % 2 * 80, ry + i / 2 * 17, 76, 15).build());
         }

      } else {
         this.addRenderableWidget(Button.builder(Component.literal("< Back"), (b) -> {
            if (this.sub.isEmpty() && this.pickedId.isEmpty()) {
               this.cat = "";
            } else {
               this.sub = "";
               this.pickedId = "";
            }

            this.pickerPage = 0;
            this.rebuild();
         }).bounds(rx, this.winY + 230 - 40, 50, 14).build());
         switch (this.cat) {
            case "hearts":
               if (this.sub.isEmpty()) {
                  this.twoButtons(rx, ry, "Gain", "Lose", "gain", "lose");
               } else {
                  this.numberThenSide(rx, ry, "Hearts (1-" + (int)this.cap(3.0D, 50.0D) + ")", (v) -> this.term("hearts", this.sub, "", v));
               }
               break;
            case "attr":
               if (this.sub.isEmpty()) {
                  for(int i = 0; i < ATTRS.length; ++i) {
                     String key = ATTRS[i][0];
                     this.addRenderableWidget(Button.builder(Component.literal(ATTRS[i][1]), (b) -> {
                        this.sub = key;
                        this.rebuild();
                     }).bounds(rx + i % 2 * 80, ry + i / 2 * 16, 76, 14).build());
                  }
               } else {
                  String label = "";

                  for(String[] a : ATTRS) {
                     if (a[0].equals(this.sub)) {
                        label = a[1] + " (" + a[2] + " to " + a[3] + ")";
                     }
                  }

                  this.numberThenSide(rx, ry, label, (v) -> this.term("attr", this.sub, "", v));
               }
               break;
            case "effects":
               if (this.sub.isEmpty()) {
                  this.twoButtons(rx, ry, "Grant Permanence", "Grant Immunity", "perm", "immune");
               } else if (this.pickedId.isEmpty()) {
                  this.registryPicker(rx, ry, "effect");
               } else if (this.sub.equals("perm")) {
                  this.numberThenSide(rx, ry, "Effect level", (v) -> this.term("effect", "", this.pickedId, Math.max(1.0D, v)));
               } else {
                  this.sideButtons(rx, ry + 40, (s) -> this.send(this.make("immune", "", this.pickedId, 0.0D, s)));
               }
               break;
            case "items":
               if (this.pickedId.isEmpty()) {
                  this.registryPicker(rx, ry, "item");
               } else {
                  this.numberThenSide(rx, ry, "Quantity (1-" + (int)this.cap(16.0D, 256.0D) + ")", (v) -> this.term("item", "", this.pickedId, Math.max(1.0D, v)));
               }
               break;
            case "soul":
               if (this.sub.isEmpty()) {
                  this.twoButtons(rx, ry, "Soul Ownership", "Soul Release", "own", "release");
               } else if (this.sub.equals("own")) {
                  this.sideButtons(rx, ry, (s) -> this.send(this.make("soul_own", "", "", 0.0D, s)));
               } else {
                  this.textThenAdd(rx, ry, "Whose soul do you release?", (t) -> this.send(this.make("soul_release", "", t, 0.0D, 0)));
               }
               break;
            case "rules":
               if (this.sub.isEmpty()) {
                  for(int i = 0; i < RULES.length; ++i) {
                     String key = RULES[i][0];
                     this.addRenderableWidget(Button.builder(Component.literal(RULES[i][1]), (b) -> {
                        this.sub = key;
                        this.rebuild();
                     }).bounds(rx + i % 2 * 80, ry + i / 2 * 17, 76, 15).build());
                  }
               } else {
                  this.addRenderableWidget(Button.builder(Component.literal("True"), (b) -> {
                     this.pickedId = "1";
                     this.rebuild();
                  }).bounds(rx, ry, 60, 15).build());
                  this.addRenderableWidget(Button.builder(Component.literal("False"), (b) -> {
                     this.pickedId = "0";
                     this.rebuild();
                  }).bounds(rx + 66, ry, 60, 15).build());
                  if (!this.pickedId.isEmpty()) {
                     double v_0 = this.pickedId.equals("1") ? 1.0D : 0.0D;
                     this.sideButtons(rx, ry + 22, (s) -> this.send(this.make("rule", this.sub, "", v_0, s)));
                  }
               }
               break;
            case "protect":
               if (this.sub.isEmpty()) {
                  this.twoButtons(rx, ry, "From a Player", "From a Damage Type", "player", "type");
               } else if (this.sub.equals("player")) {
                  if (this.pickedId.isEmpty()) {
                     this.playerPicker(rx, ry, (name) -> {
                        this.pickedId = name;
                        this.rebuild();
                     });
                  } else {
                     this.sideButtons(rx, ry, (s) -> this.send(this.make("protect_player", "", this.pickedId, 0.0D, s)));
                  }
               } else {
                  for(int i = 0; i < DMG_TYPES.length; ++i) {
                     String type = DMG_TYPES[i];
                     this.addRenderableWidget(Button.builder(Component.literal(type), (b) -> {
                        this.pickedId = type;
                        this.rebuild();
                     }).bounds(rx + i % 3 * 53, ry + i / 3 * 16, 50, 14).build());
                  }

                  if (!this.pickedId.isEmpty()) {
                     this.sideButtons(rx, ry + 60, (s) -> this.send(this.make("protect_type", "", this.pickedId, 0.0D, s)));
                  }
               }
               break;
            case "tribute":
               if (this.sub.isEmpty()) {
                  this.twoButtons(rx, ry, "XP Share", "Drop Claim", "xp", "drop");
               } else if (this.sub.equals("xp")) {
                  this.numberThenSide(rx, ry, "Percent (1-" + (int)this.cap(15.0D, 100.0D) + ")", (v_0) -> this.term("xp_share", "", "", v_0));
               } else if (this.pickedId.isEmpty()) {
                  this.registryPicker(rx, ry, "item");
               } else {
                  this.sideButtons(rx, ry + 40, (s) -> this.send(this.make("drop_claim", "", this.pickedId, 0.0D, s)));
               }
               break;
            case "restrict":
               if (this.sub.isEmpty()) {
                  String[][] subs_0 = new String[][]{{"player", "Player"}, {"mob", "Mob Type"}, {"area", "Area"}, {"dim", "Dimension"}, {"item", "Item"}};

                  for(int i = 0; i < subs_0.length; ++i) {
                     String key = subs_0[i][0];
                     this.addRenderableWidget(Button.builder(Component.literal(subs_0[i][1]), (b) -> {
                        this.sub = key;
                        this.rebuild();
                     }).bounds(rx + i % 2 * 80, ry + i / 2 * 17, 76, 15).build());
                  }
               } else if (this.sub.equals("player")) {
                  if (this.pickedId.isEmpty()) {
                     this.playerPicker(rx, ry, (name) -> {
                        this.pickedId = name;
                        this.rebuild();
                     });
                  } else {
                     this.sideButtons(rx, ry, (s) -> this.send(this.make("prohibit_player", "", this.pickedId, 0.0D, s)));
                  }
               } else if (this.sub.equals("mob")) {
                  if (this.pickedId.isEmpty()) {
                     this.registryPicker(rx, ry, "mob");
                  } else {
                     this.sideButtons(rx, ry + 40, (s) -> this.send(this.make("prohibit_mob", "", this.pickedId, 0.0D, s)));
                  }
               } else if (this.sub.equals("item")) {
                  if (this.pickedId.isEmpty()) {
                     this.registryPicker(rx, ry, "item");
                  } else {
                     this.sideButtons(rx, ry + 40, (s) -> this.send(this.make("prohibit_item", "", this.pickedId, 0.0D, s)));
                  }
               } else if (this.sub.equals("dim")) {
                  this.dimBox = this.box(rx, ry + 12, 150, "minecraft:the_nether");
                  this.sideButtons(rx, ry + 34, (s) -> this.send(this.make("prohibit_dim", "", this.dimBox.getValue().trim(), 0.0D, s)));
               } else {
                  this.x1Box = this.box(rx, ry + 12, 48, "x1");
                  this.z1Box = this.box(rx + 52, ry + 12, 48, "z1");
                  this.x2Box = this.box(rx, ry + 32, 48, "x2");
                  this.z2Box = this.box(rx + 52, ry + 32, 48, "z2");
                  this.dimBox = this.box(rx, ry + 52, 150, "minecraft:overworld");
                  this.sideButtons(rx, ry + 74, (s) -> {
                     PactLedger.Term t = this.make("prohibit_area", "", this.dimBox.getValue().trim(), 0.0D, s);
                     t.x1 = this.parseInt(this.x1Box);
                     t.z1 = this.parseInt(this.z1Box);
                     t.x2 = this.parseInt(this.x2Box);
                     t.z2 = this.parseInt(this.z2Box);
                     this.send(t);
                  });
               }
               break;
            case "info":
               if (this.sub.isEmpty()) {
                  this.twoButtons(rx, ry, "Reveal Location", "Reveal Health", "loc", "hp");
               } else {
                  this.sideButtons(rx, ry, (s) -> this.send(this.make(this.sub.equals("loc") ? "reveal_loc" : "reveal_health", "", "", 0.0D, s)));
               }
               break;
            case "identity":
               if (this.sub.isEmpty()) {
                  this.twoButtons(rx, ry, "Alias", "Title", "alias", "title");
                  this.addRenderableWidget(Button.builder(Component.literal("Disguise"), (b) -> {
                     this.sub = "disguise";
                     this.rebuild();
                  }).bounds(rx, ry + 40, 150, 16).build());
               } else if (this.sub.equals("disguise")) {
                  this.textThenAdd(rx, ry, "Wear whose name? (Signer only)", (t) -> this.send(this.make("disguise", "", t, 0.0D, 0)));
               } else {
                  this.textThenAdd(rx, ry, this.sub.equals("alias") ? "New alias (Signer only)" : "Title text (Signer only)", (t) -> this.send(this.make(this.sub, "", t, 0.0D, 0)));
               }
               break;
            case "ability":
               if (this.sub.isEmpty()) {
                  this.addRenderableWidget(Button.builder(Component.literal("Gain Ability"), (b) -> {
                     this.sub = "gain";
                     this.rebuild();
                  }).bounds(rx, ry, 150, 15).build());
                  this.addRenderableWidget(Button.builder(Component.literal("Lose Ability"), (b) -> {
                     this.sub = "lose";
                     this.rebuild();
                  }).bounds(rx, ry + 18, 150, 15).build());
                  this.addRenderableWidget(Button.builder(Component.literal("Seal a Move"), (b) -> {
                     this.sub = "move_off";
                     this.rebuild();
                  }).bounds(rx, ry + 36, 150, 15).build());
               } else if (this.pickedId.isEmpty()) {
                  if (this.sub.equals("move_off")) {
                     this.listPicker(rx, ry, this.data.moves, true);
                  } else {
                     this.listPicker(rx, ry, this.data.powers, false);
                  }
               } else {
                  this.sideButtons(rx, ry, (s) -> this.send(this.make("ability", this.sub, this.pickedId, 0.0D, s)));
               }
               break;
            case "boons":
               for(int i = 0; i < BOONS.length; ++i) {
                  String key = BOONS[i][0];
                  this.addRenderableWidget(Button.builder(Component.literal(BOONS[i][1]), (b) -> this.send(this.make("boon", key, "", 0.0D, 0))).bounds(rx + i % 2 * 80, ry + i / 2 * 17, 76, 15).build());
               }
               break;
            case "curses":
               for(int i = 0; i < CURSES.length; ++i) {
                  String key = CURSES[i][0];
                  this.addRenderableWidget(Button.builder(Component.literal(CURSES[i][1]), (b) -> this.send(this.make("curse", key, "", 0.0D, 0))).bounds(rx + i % 2 * 80, ry + i / 2 * 17, 76, 15).build());
               }
               break;
            case "cond":
               if (this.sub.isEmpty()) {
                  String[][] subs = new String[][]{{"time", "Time of Day"}, {"health", "Health %"}, {"prox", "Proximity"}, {"dim", "In Dimension"}};

                  for(int i = 0; i < subs.length; ++i) {
                     String key = subs[i][0];
                     this.addRenderableWidget(Button.builder(Component.literal(subs[i][1]), (b) -> {
                        this.sub = key;
                        this.rebuild();
                     }).bounds(rx, ry + i * 18, 150, 15).build());
                  }
               } else if (this.sub.equals("time")) {
                  this.addRenderableWidget(Button.builder(Component.literal("Only by Day"), (b) -> this.send(this.make("cond", "time", "day", 0.0D, 0))).bounds(rx, ry, 150, 15).build());
                  this.addRenderableWidget(Button.builder(Component.literal("Only by Night"), (b) -> this.send(this.make("cond", "time", "night", 0.0D, 0))).bounds(rx, ry + 18, 150, 15).build());
               } else if (this.sub.equals("health")) {
                  this.valueBox = this.box(rx, ry + 12, 70, "");
                  this.numberLabel = "Active under this % health";
                  this.addRenderableWidget(Button.builder(Component.literal("Add condition"), (b) -> {
                     try {
                        double v = Double.parseDouble(this.valueBox.getValue().trim());
                        this.send(this.make("cond", "health", "", Math.max(0.0D, Math.min(100.0D, v)), 0));
                     } catch (NumberFormatException var4) {
                        this.numberLabel = "Enter a number first";
                     }

                  }).bounds(rx, ry + 32, 150, 15).build());
               } else if (this.sub.equals("prox")) {
                  if (this.pickedId.isEmpty()) {
                     this.playerPicker(rx, ry, (name) -> {
                        this.pickedId = name;
                        this.rebuild();
                     });
                  } else {
                     this.addRenderableWidget(Button.builder(Component.literal("Only NEAR " + this.pickedId), (b) -> this.send(this.make("cond", "prox", this.pickedId, 1.0D, 0))).bounds(rx, ry, 150, 15).build());
                     this.addRenderableWidget(Button.builder(Component.literal("Only AWAY from " + this.pickedId), (b) -> this.send(this.make("cond", "prox", this.pickedId, 0.0D, 0))).bounds(rx, ry + 18, 150, 15).build());
                  }
               } else {
                  this.dimBox = this.box(rx, ry + 12, 150, "minecraft:overworld");
                  this.addRenderableWidget(Button.builder(Component.literal("Only INSIDE it"), (b) -> this.send(this.make("cond", "dim", this.dimBox.getValue().trim(), 1.0D, 0))).bounds(rx, ry + 32, 150, 15).build());
                  this.addRenderableWidget(Button.builder(Component.literal("Only OUTSIDE it"), (b) -> this.send(this.make("cond", "dim", this.dimBox.getValue().trim(), 0.0D, 0))).bounds(rx, ry + 50, 150, 15).build());
               }
               break;
            case "binds":
               if (this.sub.isEmpty()) {
                  String[][] subs = new String[][]{{"timer", "Timer"}, {"death", "Until Death"}, {"attack", "Until Attack"}, {"dimw", "Until Dimension"}, {"dimp", "Until a Plane"}, {"violence", "Until Violence"}};

                  for(int i = 0; i < subs.length; ++i) {
                     String key = subs[i][0];
                     this.addRenderableWidget(Button.builder(Component.literal(subs[i][1]), (b) -> {
                        this.sub = key;
                        this.rebuild();
                     }).bounds(rx + i % 2 * 80, ry + i / 2 * 17, 76, 15).build());
                  }
               } else if (this.sub.equals("timer")) {
                  this.valueBox = this.box(rx, ry + 12, 70, "");
                  this.numberLabel = "Minutes until it burns";
                  this.addRenderableWidget(Button.builder(Component.literal("Add bind"), (b) -> {
                     try {
                        double v = Double.parseDouble(this.valueBox.getValue().trim());
                        this.send(this.make("bind", "timer", "", Math.max(1.0D, v), 0));
                     } catch (NumberFormatException var4) {
                        this.numberLabel = "Enter a number first";
                     }

                  }).bounds(rx, ry + 32, 150, 15).build());
               } else if (!this.sub.equals("death") && !this.sub.equals("violence")) {
                  if (this.sub.equals("attack")) {
                     this.playerPicker(rx, ry, (name) -> this.send(this.make("bind", "attack", name, 0.0D, 0)));
                  } else if (this.sub.equals("dimp")) {
                     this.playerPicker(rx, ry, (name) -> this.send(this.make("bind", "dim", "player:" + name, 0.0D, 0)));
                  } else {
                     this.dimBox = this.box(rx, ry + 12, 150, "minecraft:the_nether");
                     this.addRenderableWidget(Button.builder(Component.literal("Add bind"), (b) -> this.send(this.make("bind", "dim", this.dimBox.getValue().trim(), 0.0D, 0))).bounds(rx, ry + 32, 150, 15).build());
                  }
               } else {
                  String key = this.sub;
                  this.addRenderableWidget(Button.builder(Component.literal("Add bind"), (b) -> this.send(this.make("bind", key, "", 0.0D, 0))).bounds(rx, ry, 150, 15).build());
               }
               break;
            case "punish":
               for(int i = 0; i < PUNISHMENTS.length; ++i) {
                  String key = PUNISHMENTS[i][0];
                  this.addRenderableWidget(Button.builder(Component.literal(PUNISHMENTS[i][1]), (b) -> this.send(this.make("punish", key, "", 0.0D, 0))).bounds(rx + i % 2 * 80, ry + i / 2 * 17, 76, 15).build());
               }
               break;
            case "text":
               this.textThenAdd(rx, ry, "A written note, unenforced", (t) -> {
                  PactLedger.Term term = this.make("note", "", "", 0.0D, 0);
                  term.text = t;
                  this.send(term);
               });
         }

      }
   }

   private void listPicker(int rx, int ry, List<String> entries, boolean piped) {
      int perPage = 8;
      int start = this.pickerPage * perPage;

      for(int i = 0; i < perPage && start + i < entries.size(); ++i) {
         String raw = (String)entries.get(start + i);
         String id = piped ? raw.substring(0, raw.indexOf(124)) : raw;
         String label = piped ? raw.substring(raw.indexOf(124) + 1) : raw.replace('_', ' ');
         if (label.length() > 24) {
            label = label.substring(0, 24);
         }

         this.addRenderableWidget(Button.builder(Component.literal(label), (b) -> {
            this.pickedId = id;
            this.rebuild();
         }).bounds(rx, ry + i * 16, 150, 14).build());
      }

      this.pickerNav(rx, ry + perPage * 16 + 2, entries.size(), perPage);
   }

   private void twoButtons(int rx, int ry, String labelA, String labelB, String keyA, String keyB) {
      this.addRenderableWidget(Button.builder(Component.literal(labelA), (b) -> {
         this.sub = keyA;
         this.rebuild();
      }).bounds(rx, ry, 150, 16).build());
      this.addRenderableWidget(Button.builder(Component.literal(labelB), (b) -> {
         this.sub = keyB;
         this.rebuild();
      }).bounds(rx, ry + 20, 150, 16).build());
   }

   private void numberThenSide(int rx, int ry, String label, ContractScreen.DoubleTake apply) {
      this.valueBox = this.box(rx, ry + 12, 70, "");
      if (this.proposing()) {
         this.addRenderableWidget(Button.builder(Component.literal("Ask for it §8(you)"), (b) -> {
            this.pendingSide = 0;
            this.applyNumber(apply);
         }).bounds(rx, ry + 32, 150, 15).build());
      } else {
         this.addRenderableWidget(Button.builder(Component.literal("Signer Receives"), (b) -> {
            this.pendingSide = 0;
            this.applyNumber(apply);
         }).bounds(rx, ry + 32, 150, 15).build());
         this.addRenderableWidget(Button.builder(Component.literal("Owner Receives"), (b) -> {
            this.pendingSide = 1;
            this.applyNumber(apply);
         }).bounds(rx, ry + 50, 150, 15).build());
      }

      this.numberLabel = label;
   }

   private void applyNumber(ContractScreen.DoubleTake apply) {
      try {
         double v = Double.parseDouble(this.valueBox.getValue().trim());
         apply.take(v);
      } catch (NullPointerException | NumberFormatException var4) {
         this.numberLabel = "Enter a number first";
      }

      this.pendingSide = 0;
   }

   private void term(String cat, String key, String a, double v) {
      this.send(this.make(cat, key, a, v, this.pendingSide));
   }

   private PactLedger.Term make(String cat, String key, String a, double v, int side) {
      PactLedger.Term t = new PactLedger.Term();
      t.cat = cat;
      t.key = key;
      t.a = a;
      t.v = v;
      t.side = side;
      return t;
   }

   private void send(PactLedger.Term t) {
      com.myriam.unrp.net.Net.CHANNEL.sendToServer(new PactEditPacket(0, this.data.pactId, 0, t));
      this.cat = "";
      this.sub = "";
      this.pickedId = "";
   }

   private void sideButtons(int rx, int ry, ContractScreen.SideTake take) {
      if (this.proposing()) {
         this.addRenderableWidget(Button.builder(Component.literal("Ask for it §8(you)"), (b) -> take.take(0)).bounds(rx, ry, 150, 15).build());
      } else {
         this.addRenderableWidget(Button.builder(Component.literal("Signer Receives"), (b) -> take.take(0)).bounds(rx, ry, 150, 15).build());
         this.addRenderableWidget(Button.builder(Component.literal("Owner Receives"), (b) -> take.take(1)).bounds(rx, ry + 18, 150, 15).build());
      }
   }

   private void textThenAdd(int rx, int ry, String label, ContractScreen.TextTake take) {
      this.textBox = this.box(rx, ry + 12, 150, "");
      this.textBox.setMaxLength(120);
      this.numberLabel = label;
      this.addRenderableWidget(Button.builder(Component.literal("Add to contract"), (b) -> {
         String t = this.textBox.getValue().trim();
         if (!t.isEmpty()) {
            take.take(t);
         }

      }).bounds(rx, ry + 32, 150, 15).build());
   }

   private void playerPicker(int rx, int ry, ContractScreen.NameTake take) {
      List<String> names = this.data.players;
      int perPage = 6;
      int start = this.pickerPage * perPage;

      for(int i = 0; i < perPage && start + i < names.size(); ++i) {
         String name = (String)names.get(start + i);
         this.addRenderableWidget(Button.builder(Component.literal(name), (b) -> take.take(name)).bounds(rx, ry + i * 16, 150, 14).build());
      }

      this.pickerNav(rx, ry + perPage * 16 + 4, names.size(), perPage);
   }

   private void registryPicker(int rx, int ry, String kind) {
      this.searchBox = this.box(rx, ry, 150, this.pickerFilter);
      this.searchBox.setResponder((s) -> {
         if (!s.equals(this.pickerFilter)) {
            this.pickerFilter = s;
            this.pickerPage = 0;
            this.needsRefresh = true;
         }

      });
      List<String> ids = this.idsFor(kind, this.pickerFilter);
      int perPage = 7;
      int start = this.pickerPage * perPage;

      for(int i = 0; i < perPage && start + i < ids.size(); ++i) {
         String id = (String)ids.get(start + i);
         String label = id.substring(id.indexOf(58) + 1).replace('_', ' ');
         if (label.length() > 24) {
            label = label.substring(0, 24);
         }

         this.addRenderableWidget(Button.builder(Component.literal(label), (b) -> {
            this.pickedId = id;
            this.rebuild();
         }).bounds(rx, ry + 18 + i * 16, 150, 14).build());
      }

      this.pickerNav(rx, ry + 18 + perPage * 16 + 2, ids.size(), perPage);
   }

   private void pickerNav(int rx, int ry, int total, int perPage) {
      int pages = Math.max(1, (total + perPage - 1) / perPage);
      if (pages > 1) {
         this.addRenderableWidget(Button.builder(Component.literal("<"), (b) -> {
            this.pickerPage = Math.max(0, this.pickerPage - 1);
            this.rebuild();
         }).bounds(rx, ry, 20, 13).build());
         this.addRenderableWidget(Button.builder(Component.literal(">"), (b) -> {
            this.pickerPage = Math.min(pages - 1, this.pickerPage + 1);
            this.rebuild();
         }).bounds(rx + 24, ry, 20, 13).build());
      }
   }

   private List<String> idsFor(String kind, String filter) {
      List<String> out = new ArrayList();
      String needle = filter.toLowerCase(Locale.ROOT);
      if (kind.equals("item")) {
         for(ResourceLocation id_0 : ForgeRegistries.ITEMS.getKeys()) {
            if (needle.isEmpty() || id_0.getPath().contains(needle)) {
               out.add(id_0.toString());
            }
         }
      } else if (kind.equals("effect")) {
         for(ResourceLocation id : ForgeRegistries.MOB_EFFECTS.getKeys()) {
            if (needle.isEmpty() || id.getPath().contains(needle)) {
               out.add(id.toString());
            }
         }
      } else {
         for(ResourceLocation id : ForgeRegistries.ENTITY_TYPES.getKeys()) {
            if (needle.isEmpty() || id.getPath().contains(needle)) {
               out.add(id.toString());
            }
         }
      }

      out.sort(String::compareTo);
      return out;
   }

   private EditBox box(int x, int y, int w, String preset) {
      EditBox b = new EditBox(this.font, x, y, w, 14, Component.literal(""));
      b.setValue(preset);
      this.addRenderableWidget(b);
      return b;
   }

   private int parseInt(EditBox b) {
      try {
         return Integer.parseInt(b.getValue().trim());
      } catch (NumberFormatException var3) {
         return 0;
      }
   }

   private double cap(double min, double max) {
      return min + (max - min) * this.data.reachFrac;
   }

   public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(g);
      g.fill(this.winX - 2, this.winY - 2, this.winX + 360 + 2, this.winY + 230 + 2, -401601528);
      g.fill(this.winX, this.winY, this.winX + 360, this.winY + 230, -266463475);
      g.fill(this.winX + 190, this.winY + 20, this.winX + 191, this.winY + 230 - 26, 1625867114);
      String var10000;
      switch (this.data.mode) {
         case 0:
            var10000 = this.data.proposed ? "§6§lPETITION§8: yours to answer" : "§6§lCONTRACT§8: being written";
            break;
         case 1:
            var10000 = "§6§lCONTRACT§8: awaiting your signature";
            break;
         case 2:
            var10000 = "§6§lCONTRACT§8: signed and binding";
            break;
         case 3:
         default:
            var10000 = this.data.proposed && this.data.submitted ? "§6§lPETITION§8: sent, awaiting the Dealmaker" : "§6§lCONTRACT§8: a record";
            break;
         case 4:
            var10000 = "§6§lPETITION§8: write your asks";
      }

      String title = var10000;
      g.drawString(this.font, title, this.winX + 8, this.winY + 8, 16777215, true);
      String parties = "§7Owner: §6" + this.data.ownerName + (this.data.signed ? " §7Signer: §6" + this.data.signerName : " §7Signer: §8unsigned") + (this.data.soul ? " §3[SOUL]" : "");
      g.drawString(this.font, parties, this.winX + 8, this.winY + 20, 16777215, true);
      int perPage = this.termsPerPage();
      int start = this.termPage * perPage;
      if (this.data.terms.isEmpty()) {
         g.drawString(this.font, "§8No terms written yet.", this.winX + 24, this.winY + 36, 16777215, true);
      }

      String hoverTerm = null;

      for(int i = 0; i < perPage && start + i < this.data.terms.size(); ++i) {
         PactLedger.Term t = (PactLedger.Term)this.data.terms.get(start + i);
         String line = PactEnforcement.describe(t);
         int rowX = this.winX + (this.editing() ? 24 : 10);
         int rowY = this.winY + 36 + i * 13;
         int maxW = this.winX + 186 - rowX;
         String shown = line;
         if (this.font.width(line) > maxW) {
            shown = this.font.plainSubstrByWidth(line, maxW - 8) + "§8..";
            if (mouseX >= rowX && mouseX <= this.winX + 186 && mouseY >= rowY - 1 && mouseY <= rowY + 11) {
               hoverTerm = line;
            }
         }

         String prefix = t.locked && this.data.mode == 0 ? "§6# §7" : "§7";
         g.drawString(this.font, prefix + shown, rowX, rowY, 16777215, true);
      }

      if (hoverTerm != null) {
         g.renderTooltip(this.font, Component.literal("§7" + hoverTerm), mouseX, mouseY);
      }

      if (this.editing() && !this.numberLabel.isEmpty() && (this.valueBox != null || this.textBox != null)) {
         g.drawString(this.font, "§7" + this.numberLabel, this.winX + 196, this.winY + 26, 16777215, true);
      }

      if (this.editing() && this.cat.isEmpty()) {
         g.drawString(this.font, "§8Pick a category to add a term.", this.winX + 196, this.winY + 230 - 36, 16777215, true);
      }

      if (this.data.mode == 4 && this.confirmSubmit) {
         g.drawString(this.font, "§4Pre-signing binds you to whatever", this.winX + 8, this.winY + 230 - 52, 16777215, true);
         g.drawString(this.font, "§4The Dealmaker adds. Everything.", this.winX + 8, this.winY + 230 - 40, 16777215, true);
      }

      if (this.data.mode == 0 && this.data.proposed) {
         g.drawString(this.font, "§8# marks their asks: not yours to strike.", this.winX + 8, this.winY + 230 - 40, 16777215, true);
      }

      if (this.data.mode == 1) {
         g.drawString(this.font, "§cSigning binds you until it's destroyed.", this.winX + 8, this.winY + 230 - 40, 16777215, true);
      }

      super.render(g, mouseX, mouseY, partialTick);
   }

   public void tick() {
      if (this.needsRefresh) {
         this.needsRefresh = false;
         String keep = this.pickerFilter;
         this.rebuild();
         if (this.searchBox != null) {
            this.searchBox.setValue(keep);
            this.setFocused(this.searchBox);
         }
      }

   }

   public boolean isPauseScreen() {
      return false;
   }

   private interface DoubleTake {
      void take(double var1);
   }

   private interface NameTake {
      void take(String var1);
   }

   private interface SideTake {
      void take(int var1);
   }

   private interface TextTake {
      void take(String var1);
   }
}
