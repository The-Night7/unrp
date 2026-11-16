package com.myriam.unrp.client;

import com.google.gson.JsonObject;
import com.myriam.unrp.ability.CovenantLawChoices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

/** Forms generate the same commands as advanced input; authority and consent stay on the server. */
public final class CovenantWritingScreen extends Screen {
    private final String action;
    private final List<String> penalties=new ArrayList<>(), players=new ArrayList<>(), territories=new ArrayList<>();
    private final boolean[] terms=new boolean[5];
    private final String[] termNames={"weapons","armor","elytra","pearls","inventory"};
    private int give,take,target,territory,x,y,w;
    private String message="";
    private boolean submitted;
    public CovenantWritingScreen(String action,double level,JsonObject data) {
        super(Component.literal("Covenant — "+switch(action){case "terms"->"Terms of Entry";case "pact","sovereign_pact"->"Pact";case "tribute"->"Sovereign's Due";case "message"->"Writing on the Hand";default->"Jurisdiction";}));
        this.action=action;
        for(var p:CovenantLawChoices.available(level)) penalties.add(p.clause());
        if(data.has("players")) for(var p:data.getAsJsonArray("players")) players.add(p.getAsString());
        if(data.has("territories")) for(var t:data.getAsJsonArray("territories")) territories.add(t.getAsString());
        if(territories.isEmpty())territories.add("here");
        if(players.isEmpty())players.add("");
    }
    private void button(String label,int row,Runnable run){addRenderableWidget(Button.builder(Component.literal(label),b->run.run()).bounds(x+10,y+35+row*24,w-20,20).build());}
    private List<String> offers(){var list=new ArrayList<>(penalties);list.add("life");if(action.equals("sovereign_pact"))list.add("nothing");return list;}
    @Override protected void init(){
        clearWidgets();w=Math.min(380,width-16);x=(width-w)/2;y=(height-232)/2;
        switch(action){
            case "terms" -> {for(int i=0;i<terms.length;i++){final int n=i;button((terms[i]?"[x] ":"[ ] ")+termNames[i],i,()->{terms[n]=!terms[n];init();});}}
            case "pact","sovereign_pact" -> {
                if(action.equals("sovereign_pact"))button("Recipient: "+players.get(target),0,()->{target=(target+1)%players.size();init();});
                var offers=offers();button("You give: "+offers.get(give),1,()->{give=(give+1)%offers.size();init();});
                button("You receive: "+penalties.get(take),2,()->{take=(take+1)%penalties.size();init();});
            }
            case "tribute" -> button("Daily tribute: "+penalties.get(take),0,()->{take=(take+1)%penalties.size();init();});
            case "jurisdiction" -> button("Court: "+territories.get(territory),0,()->{territory=(territory+1)%territories.size();init();});
            case "message" -> {
                button("Recipient: "+players.get(target),0,()->{target=(target+1)%players.size();init();});
                EditBox input=new EditBox(font,x+10,y+65,w-20,20,Component.literal("Message"));input.setMaxLength(450);input.setValue(message);input.setResponder(value->message=value);addRenderableWidget(input);
            }
        }
        int bw=(w-28)/3;
        addRenderableWidget(Button.builder(Component.literal("Confirm"),b->submit()).bounds(x+10,y+202,bw,20).build());
        addRenderableWidget(Button.builder(Component.literal("Advanced..."),b->{submitted=true;minecraft.setScreen(new ChatScreen("/unrp covenant "+action+" "));}).bounds(x+14+bw,y+202,bw,20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).bounds(x+18+bw*2,y+202,bw,20).build());
    }
    private void submit(){
        String text=switch(action){
            case "terms" -> {var selected=new ArrayList<String>();for(int i=0;i<terms.length;i++)if(terms[i])selected.add(termNames[i]);yield String.join(", ",selected);}
            case "pact","sovereign_pact" -> (action.equals("sovereign_pact")?players.get(target)+" | ":"")+offers().get(give)+" for "+penalties.get(take);
            case "tribute" -> penalties.get(take);
            case "jurisdiction" -> territories.get(territory);
            default -> players.get(target)+" | "+message;
        };
        if(text.isBlank()||minecraft.player==null)return;
        if((action.equals("message")||action.equals("sovereign_pact"))&&players.get(target).isBlank())return;
        if(action.equals("message")&&message.isBlank())return;
        submitted=true;minecraft.player.connection.sendCommand("unrp covenant "+action+" "+text);minecraft.setScreen(null);
    }
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){if(!submitted&&minecraft.player!=null)minecraft.player.connection.sendCommand("unrp covenant cancel");minecraft.setScreen(null);}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g);UiSkin.panel(g,x,y,w,232,UiSkin.GOLD);g.drawString(font,title,x+10,y+12,UiSkin.HEAD,false);
        String hint=switch(action){case "pact","sovereign_pact"->"Click to cycle. The recipient must accept. You must hold the collateral you offer.";case "terms"->"Select what is collected when someone enters.";case "message"->"Choose a signer, then write your message.";default->"Click to cycle through available choices.";};
        g.drawWordWrap(font,Component.literal(hint),x+10,y+160,w-20,UiSkin.TEXT);super.render(g,mx,my,partial);
    }
}
