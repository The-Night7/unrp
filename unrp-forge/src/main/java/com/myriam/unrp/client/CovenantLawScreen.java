package com.myriam.unrp.client;

import com.myriam.unrp.ability.CovenantLawChoices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.List;

/** Click-to-enact form. The server still validates the generated command and every tier/cost limit. */
public final class CovenantLawScreen extends Screen {
    private final double level;
    private final String command;
    private final List<CovenantLawChoices.Penalty> penalties;
    private int action=0,penalty=0,page=0,actionPage=0,left,top,w,h,rows,perPage;
    private boolean submitted;
    public CovenantLawScreen(double level) {
        this(level,"law");
    }
    public CovenantLawScreen(double level,String command) {
        super(Component.literal("Covenant — " + switch(command){case "constitution" -> "Constitution";case "retroactive" -> "Retroactive Law";case "forfeit" -> "Total Forfeit";default -> "Enact";}));this.command=command;this.level=level;penalties=command.equals("forfeit")?CovenantLawChoices.PENALTIES.stream().filter(p->p.kind()==com.myriam.unrp.ability.CovenantParse.Kind.ABILITY).toList():CovenantLawChoices.available(level);
    }
    @Override protected void init() {
        clearWidgets();w=Math.min(480,width-16);h=Math.min(238,height-12);left=(width-w)/2;top=(height-h)/2;
        rows=Math.max(2,Math.min(6,(h-110)/18));perPage=rows*2;
        int half=(w-30)/2,bw=(half-6)/2;
        // The action list fits five rows at ordinary GUI scales. On a tiny window it uses a page too.
        int actionRows=Math.min(5,rows);int actionPages=(CovenantLawChoices.ACTIONS.size()+actionRows*2-1)/(actionRows*2);actionPage=Math.min(actionPage,actionPages-1);int actionStart=actionPage*(actionRows*2);
        for(int j=0;j<Math.min(actionRows*2,CovenantLawChoices.ACTIONS.size()-actionStart);j++) {
            int index=actionStart+j;var choice=CovenantLawChoices.ACTIONS.get(index);
            addRenderableWidget(Button.builder(label(choice.label(),index==action),b->{action=index;init();})
                    .bounds(left+10+(j%2)*(bw+4),top+45+(j/2)*18,bw,17).build());
        }
        if(actionRows<5) addRenderableWidget(Button.builder(Component.literal("More >"),b->{actionPage=(actionPage+1)%actionPages;init();})
                .bounds(left+10+half-62,top+25,62,16).build());
        int pages=(penalties.size()+perPage-1)/perPage;page=Math.min(page,pages-1);
        int start=page*perPage;
        for(int j=0;j<Math.min(perPage,penalties.size()-start);j++) {
            int index=start+j;var choice=penalties.get(index);
            addRenderableWidget(Button.builder(label(choice.label(),index==penalty),b->{penalty=index;init();})
                    .bounds(left+20+half+(j%2)*(bw+4),top+45+(j/2)*18,bw,17).build());
        }
        if(pages>1) addRenderableWidget(Button.builder(Component.literal((page+1)+" / "+pages+" >"),b->{page=(page+1)%pages;init();})
                .bounds(left+w-74,top+25,64,16).build());
        if(command.equals("law")) {
            var special=Button.builder(Component.literal("Specific rules..."),b->minecraft.setScreen(new CovenantSpecificScreen(this)))
                    .bounds(left+w-122,top+7,112,16).build();special.active=level>=3.5;addRenderableWidget(special);
        }
        int footer=(w-28)/3;
        addRenderableWidget(Button.builder(Component.literal("Confirm").withStyle(ChatFormatting.GOLD),b->submit())
                .bounds(left+10,top+h-27,footer,20).build());
        addRenderableWidget(Button.builder(Component.literal("Advanced..."),b->{submitted=true;minecraft.setScreen(new ChatScreen("/unrp covenant "+command+" "));})
                .bounds(left+14+footer,top+h-27,footer,20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose())
                .bounds(left+18+footer*2,top+h-27,footer,20).build());
    }
    private Component label(String text,boolean selected) {return Component.literal((selected?"> ":"")+text).withStyle(selected?ChatFormatting.GOLD:ChatFormatting.WHITE);}
    private void submit() {
        if(minecraft.player==null)return;
        submitted=true;
        minecraft.player.connection.sendCommand("unrp covenant "+command+" "+CovenantLawChoices.text(CovenantLawChoices.ACTIONS.get(action),penalties.get(penalty)));
        minecraft.setScreen(null);
    }
    @Override public void onClose() {
        if(!submitted&&minecraft.player!=null)minecraft.player.connection.sendCommand("unrp covenant cancel");
        minecraft.setScreen(null);
    }
    @Override public boolean isPauseScreen() {return false;}
    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
        renderBackground(g);UiSkin.panel(g,left,top,w,h,UiSkin.GOLD);
        g.drawString(font,title,left+10,top+10,UiSkin.HEAD,false);
        g.drawString(font,"Action",left+10,top+29,UiSkin.SUB,false);
        g.drawString(font,"Penalty",left+20+(w-30)/2,top+29,UiSkin.SUB,false);
        String sentence=CovenantLawChoices.text(CovenantLawChoices.ACTIONS.get(action),penalties.get(penalty));
        g.drawWordWrap(font,Component.literal(sentence),left+10,top+h-54,w-20,UiSkin.TEXT);
        super.render(g,mx,my,partial);
    }
}
