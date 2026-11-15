package com.myriam.unrp.look;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** A portrait-and-bars HUD with native pixel corners and restrained gold trim. */
public final class UnrpHudOverlay implements IGuiOverlay {
    public static final UnrpHudOverlay INSTANCE = new UnrpHudOverlay();
    private static final int WIDTH = 196, HEIGHT = 78;
    private static final int GOLD = 0xFFD6B570, INK = 0xED13171E, LIGHT = 0xFFF3E8D0;
    private UnrpHudOverlay() {}

    @Override public void render(ForgeGui gui, GuiGraphics g, float partialTick, int sw, int sh) {
        var mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null || p.isSpectator() || mc.options.hideGui || mc.options.renderDebug || !panelShown()) return;
        g.pose().pushPose();
        g.pose().translate(panelX(sw), panelY(sh), 0);
        float scale = ClientHudConfig.hudScale();
        g.pose().scale(scale, scale, 1);
        // The stepped silhouette and isolated corner brackets replace the ornamental box.
        g.fill(3, 0, WIDTH-3, HEIGHT, INK);
        g.fill(0, 3, WIDTH, HEIGHT-3, INK);
        corner(g, 2, 2, false, false);
        corner(g, WIDTH-3, 2, true, false);
        corner(g, 2, HEIGHT-3, false, true);
        corner(g, WIDTH-3, HEIGHT-3, true, true);
        g.fill(49, 10, 50, 67, 0xFF353843);

        g.fill(6, 13, 46, 53, 0xFF080C12);
        g.fill(7, 14, 45, 52, 0xFF76623F);
        PlayerFaceRenderer.draw(g, p.getSkinTextureLocation(), 8, 15, 36);
        g.fill(9, 56, 43, 57, 0xFF665438);
        g.fill(9, 56, 21, 57, GOLD);
        // Small decorative identity mark, outside the skin, so hats remain readable.
        g.fill(23, 60, 28, 61, GOLD);
        g.fill(25, 58, 26, 63, GOLD);

        g.drawString(mc.font, mc.font.plainSubstrByWidth(p.getName().getString(), 132), 54, 7, LIGHT, true);
        float maxHp = Math.max(1, p.getMaxHealth());
        heart(g, 54, 24, 0xFFE0BB70);
        meter(g, 66, 22, 120, 11, p.getHealth()/maxHp, 0xFFD2AC65);
        if (p.getAbsorptionAmount() > 0) {
            int w = Math.round(118*Math.min(1,p.getAbsorptionAmount()/maxHp));
            g.fill(67, 23, 67+w, 25, 0xFFFFECC0);
        }
        centered(g, (int)Math.ceil(p.getHealth())+" / "+(int)maxHp, 126, 23);

        int aura = StatClientCache.aura, maxAura = StatClientCache.auraMax;
        int custom = ClientSettingsCache.getColor(p.getUUID());
        int auraColor = custom == -1 ? 0xFF6AACA8 : 0xFF000000 | custom;
        if (ClientSettingsCache.isAM(p.getUUID())) auraColor = 0xFFD9D4B1;
        droplet(g,54,40,auraColor);
        meter(g,66,39,120,10,maxAura>0?(float)aura/maxAura:0,auraColor);
        centered(g,aura+" / "+maxAura,126,40);

        int food=p.getFoodData().getFoodLevel(), armor=p.getArmorValue();
        foodIcon(g,54,58);
        meter(g,66,56,48,10,food/20f,0xFFB98553);
        centered(g,String.valueOf(food),90,57);
        shield(g,123,57);
        meter(g,135,56,51,10,Math.min(armor,20)/20f,0xFF879AAA);
        centered(g,String.valueOf(armor),160,57);
        if (StatClientCache.momentum>0) meter(g,54,70,132,3,StatClientCache.momentum/100f,GOLD);
        g.pose().popPose();
    }

    private static void corner(GuiGraphics g,int x,int y,boolean right,boolean bottom) {
        int dx=right?-1:1,dy=bottom?-1:1;
        for(int i=0;i<8;i++) g.fill(x+dx*i,y,x+dx*i+1,y+1,GOLD);
        for(int i=0;i<5;i++) g.fill(x,y+dy*i,x+1,y+dy*i+1,GOLD);
    }
    private static void heart(GuiGraphics g,int x,int y,int color) {
        g.fill(x,y,x+3,y+3,color);g.fill(x+4,y,x+7,y+3,color);
        g.fill(x+1,y+2,x+6,y+4,color);g.fill(x+2,y+4,x+5,y+5,color);g.fill(x+3,y+5,x+4,y+6,color);
    }
    private static void droplet(GuiGraphics g,int x,int y,int color) {
        g.fill(x+3,y,x+4,y+2,color);g.fill(x+2,y+2,x+5,y+4,color);
        g.fill(x+1,y+4,x+6,y+6,color);g.fill(x+2,y+6,x+5,y+7,color);
    }
    private static void foodIcon(GuiGraphics g,int x,int y) {
        g.fill(x+1,y,x+5,y+4,0xFFC89B68);g.fill(x,y+1,x+6,y+3,0xFFC89B68);
        g.fill(x+4,y+3,x+6,y+5,0xFFE0D6BF);g.fill(x+5,y+5,x+7,y+6,0xFFE0D6BF);
    }
    private static void shield(GuiGraphics g,int x,int y) {
        g.fill(x,y,x+7,y+5,0xFF98A7B1);g.fill(x+1,y+5,x+6,y+6,0xFF98A7B1);g.fill(x+2,y+6,x+5,y+7,0xFF98A7B1);
        g.fill(x+2,y+1,x+5,y+4,0xFF465666);
    }
    private static void centered(GuiGraphics g,String text,int x,int y) {
        var font=Minecraft.getInstance().font;
        g.drawString(font,text,x-font.width(text)/2,y,LIGHT,true);
    }
    private static void meter(GuiGraphics g,int x,int y,int w,int h,float fraction,int color) {
        // Cut tips, a one-pixel rim and two discrete shade bands. No texture scaling.
        g.fill(x+1,y-1,x+w-1,y+h+1,0xFF080B11);
        g.fill(x,y,x+w,y+h,0xFF080B11);
        g.fill(x+1,y,x+w-1,y+h,0xFF3A3D42);
        g.fill(x+2,y+1,x+w-2,y+h-1,0xFF22272F);
        int filled=Math.round((w-4)*Math.max(0,Math.min(1,fraction)));
        if(filled>0) {
            g.fill(x+2,y+1,x+2+filled,y+h-1,color);
            if(h>4) {
                g.fill(x+2,y+1,x+2+filled,y+2,shade(color,1.2f));
                g.fill(x+2,y+h-3,x+2+filled,y+h-1,shade(color,.65f));
            }
        }
    }
    private static int shade(int color,float factor) {
        int r=Math.min(255,Math.round(((color>>16)&255)*factor));
        int g=Math.min(255,Math.round(((color>>8)&255)*factor));
        int b=Math.min(255,Math.round((color&255)*factor));
        return 0xFF000000|r<<16|g<<8|b;
    }
    public static void clearAll() {}
    public static float panelX(int sw) {
        int anchor=ClientHudConfig.hudAnchor();
        return anchor==1||anchor==3?sw-panelWidth()-ClientHudConfig.hudOffX():ClientHudConfig.hudOffX();
    }
    public static float panelY(int sh) {
        int anchor=ClientHudConfig.hudAnchor();
        return anchor==2||anchor==3?sh-panelHeight()-ClientHudConfig.hudOffY():ClientHudConfig.hudOffY();
    }
    public static float panelWidth() {return WIDTH*ClientHudConfig.hudScale();}
    public static float panelHeight() {return HEIGHT*ClientHudConfig.hudScale();}
    public static boolean panelShown() {return StatClientCache.hudMode==0;}
}
