package com.myriam.unrp.client;

import com.myriam.unrp.ability.CovenantSpecificRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Two complete rules with explicit effects, rather than a free-form scripting language. */
public final class CovenantSpecificScreen extends Screen {
    private final Screen parent;
    private int choice,x,y,w;
    private String target="*";
    private Button enact;
    public CovenantSpecificScreen(Screen parent){super(Component.literal("Covenant — Specific Rules"));this.parent=parent;}
    @Override protected void init(){
        w=Math.min(390,width-16);x=(width-w)/2;y=(height-212)/2;
        addRenderableWidget(Button.builder(Component.literal((choice==0?"> ":"")+"Transfer signed contracts"),b->{choice=0;init();}).bounds(x+10,y+34,w-20,20).build());
        addRenderableWidget(Button.builder(Component.literal((choice==1?"> ":"")+"Report a visitor's home"),b->{choice=1;init();}).bounds(x+10,y+58,w-20,20).build());
        var input=new EditBox(font,x+10,y+98,w-20,20,Component.literal("Player name or *"));input.setMaxLength(16);input.setValue(target);input.setEditable(choice==1);
        input.setResponder(value->{target=value;if(enact!=null)enact.active=valid();});addRenderableWidget(input);
        enact=addRenderableWidget(Button.builder(Component.literal("Enact"),b->{
            if(minecraft.player==null||!valid())return;
            var rule=new CovenantSpecificRules.Rule(choice==0?CovenantSpecificRules.Kind.SIGNED_CONTRACTS:CovenantSpecificRules.Kind.HOME_REPORT,choice==0?"*":target);
            minecraft.player.connection.sendCommand("unrp covenant law "+rule.text());minecraft.setScreen(null);
        }).bounds(x+10,y+182,(w-24)/2,20).build());enact.active=valid();
        addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(x+14+(w-24)/2,y+182,(w-24)/2,20).build());
    }
    private boolean valid(){return choice==0||target.matches("[A-Za-z0-9_]{1,16}|\\*");}
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g);UiSkin.panel(g,x,y,w,212,UiSkin.GOLD);g.drawString(font,title,x+10,y+12,UiSkin.HEAD,false);
        g.drawString(font,"Player name (* = any visitor)",x+10,y+85,UiSkin.SUB,false);
        g.drawWordWrap(font,Component.literal(choice==0?"Every signed original contract carried inside the Court is transferred to your inventory. Full inventory: transfer waits.":"When the selected player enters, receive a book containing their name and saved home coordinates."),x+10,y+128,w-20,UiSkin.TEXT);
        super.render(g,mx,my,partial);
    }
}
