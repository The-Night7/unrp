package com.myriam.unrp.worldgen;

import java.util.ArrayList;
import java.util.List;
import static com.myriam.unrp.worldgen.SchoolBlueprint.Material.*;

/** Layout six: old architectural identities with deeper facades, planted courts and a terrain apron. */
public final class SchoolPolish {
    public static final int APRON=8;
    private final List<SchoolBlueprint.Box> boxes=new ArrayList<>();
    private final List<SchoolBlueprint.Label> labels=new ArrayList<>();
    private final SchoolPlanGrid old;
    private final int size,height;
    private SchoolPolish(SchoolBlueprint.Plan base) {
        old=new SchoolPlanGrid(base);size=base.size();height=base.height();
        for(var b:base.boxes()) boxes.add(new SchoolBlueprint.Box(b.material(),b.x1()+APRON,b.y1(),b.z1()+APRON,b.x2()+APRON,b.y2(),b.z2()+APRON));
        for(var l:base.labels())labels.add(new SchoolBlueprint.Label(l.x()+APRON,l.y(),l.z()+APRON,l.text()));
    }
    private void box(SchoolBlueprint.Material m,int x1,int y1,int z1,int x2,int y2,int z2) {
        if(x1<0||z1<0||x2>=size||z2>=size||y1< -1||y2>=height)return;
        boxes.add(new SchoolBlueprint.Box(m,x1+APRON,y1,z1+APRON,x2+APRON,y2,z2+APRON));
    }
    private void at(SchoolBlueprint.Material m,int x,int y,int z) {box(m,x,y,z,x,y,z);}
    public static SchoolBlueprint.Plan apply(SchoolSites.School school,SchoolBlueprint.Plan base) {
        var p=new SchoolPolish(base);p.facades();p.interiors();p.streetAndRoofs();p.gardens();
        int total=base.size()+2*APRON;
        var all=new ArrayList<SchoolBlueprint.Box>();
        all.add(new SchoolBlueprint.Box(AIR,0,1,0,total-1,base.height()-1,total-1));
        all.add(new SchoolBlueprint.Box(GRASS,0,0,0,total-1,0,total-1));
        all.add(new SchoolBlueprint.Box(FOUNDATION,0,-1,0,total-1,-1,total-1));
        all.addAll(p.boxes);
        // Continue the existing entrance through the apron; terrain blending places this at the local grade.
        all.add(new SchoolBlueprint.Box(PAVING,total/2-4,0,0,total/2+4,0,APRON-1));
        return new SchoolBlueprint.Plan(List.copyOf(all),List.copyOf(p.labels),base.height(),total);
    }
    private static boolean wall(SchoolBlueprint.Material m) {return m==WALL||m==TOWER_WALL||m==CENTRE_PANEL||m==AGWIN_YELLOW||m==AGWIN_PINK;}
    private static boolean glazing(SchoolBlueprint.Material m) {return m==GLASS||m==TOWER_GLASS||m==WINDOW_MULLION;}
    private void facades() {
        // Only exterior cells open to sky are touched. Room partitions, doors and circulation remain intact.
        for(int x=1;x<size-1;x++) for(int z=1;z<size-1;z++) {
            boolean sky=true;for(int y=1;y<height;y++)if(!old.clear(x,y,z)){sky=false;break;}
            if(!sky)continue;
            for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int wx=x+d[0],wz=z+d[1];
                for(int y=1;y<height-1;y++) {
                    if(wall(old.at(wx,y,wz))) {
                        if(old.at(wx,y-1,wz)==FLOOR||old.at(wx,y-1,wz)==FOUNDATION)at(FACADE_BASE,wx,y,wz);
                        else if((d[0]==0?wx:wz)%12==0)at(FACADE_PIER,wx,y,wz);
                    }
                    if(glazing(old.at(wx,y,wz)) && wall(old.at(wx,y-1,wz))) {
                        at(WINDOW_SILL,x,y-1,z);
                    }
                    if(old.at(wx,y,wz)==ROOF && (wall(old.at(wx,y-1,wz))||glazing(old.at(wx,y-1,wz))||old.at(wx,y-1,wz)==TRIM))at(CORNICE,x,y,z);
                }
                if(old.at(wx,1,wz)==DOOR_LOWER) {
                    // A thin projecting canopy above the existing door, never at foot/head level.
                    for(int a=-2;a<=2;a++) for(int depth=0;depth<2;depth++) {
                        int cx=x-d[0]*depth+(d[0]==0?a:0),cz=z-d[1]*depth+(d[1]==0?a:0);
                        if(old.clear(cx,4,cz)&&old.clear(cx,5,cz))at(CORNICE,cx,4,cz);
                    }
                }
            }
        }
    }
    private void interiors() {
        for(int x=1;x<size-1;x++)for(int z=1;z<size-1;z++)for(int y=0;y<height-1;y++) {
            if(old.at(x,y,z)==LIGHT&&(old.at(x,y+1,z)==FLOOR||old.at(x,y+1,z)==ROOF))at(PENDANT,x,y,z);
            if(old.at(x,y,z)==FLOOR&&old.clear(x,y+1,z)) {
                if(old.at(x-1,y+1,z)==PARTITION||old.at(x+1,y+1,z)==PARTITION||old.at(x,y+1,z-1)==PARTITION||old.at(x,y+1,z+1)==PARTITION)at(FLOOR_INLAY,x,y,z);
            }
        }
    }
    private void streetAndRoofs() {
        int vents=0;
        for(int x=2;x<size-2;x++)for(int z=2;z<size-2;z++) {
            if(old.at(x,3,z)==LIGHT&&old.at(x,2,z)==ACCENT&&old.at(x,1,z)==ACCENT) {
                box(STREET_POST,x,1,z,x,2,z);at(STREET_LANTERN,x,3,z);
            }
            if(old.at(x,1,z)==LOG&&old.at(x,3,z)==LOG) {
                for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int y=4;y<=6;y++) {
                    if(old.at(x+dx,y,z+dz)!=LEAVES)continue;
                    if(Math.abs(dx)+Math.abs(dz)>(y==6?2:3))at(AIR,x+dx,y,z+dz);
                    else if(y==6)at(SHRUB,x+dx,y,z+dz);
                }
            }
            if(vents<6&&x%24==8&&z%24==8)for(int y=6;y<height-2;y++) {
                boolean free=true;
                for(int dx=0;dx<2;dx++)for(int dz=0;dz<2;dz++)if(old.at(x+dx,y,z+dz)!=ROOF||!old.clear(x+dx,y+1,z+dz)||!old.clear(x+dx,y+2,z+dz))free=false;
                if(free){box(ROOF_VENT,x,y+1,z,x+1,y+1,z+1);vents++;break;}
            }
        }
    }
    private void gardens() {
        // Native stone edging for path surfaces, without adding ankle-high obstacles.
        for(int x=1;x<size-1;x++)for(int z=1;z<size-1;z++) {
            if(old.at(x,0,z)!=PAVING||!old.clear(x,1,z))continue;
            boolean edge=old.at(x-1,0,z)==GRASS||old.at(x+1,0,z)==GRASS||old.at(x,0,z-1)==GRASS||old.at(x,0,z+1)==GRASS;
            at(edge?PATH_EDGE:PATH_TILE,x,0,z);
        }
        int planted=0;
        for(int x=9;x<size-8;x+=16)for(int z=9;z<size-8;z+=16) {
            if(planted>=18)return;
            boolean free=true;
            for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++) {
                if(old.at(x+dx,0,z+dz)!=GRASS)free=false;
                for(int y=1;y<=7&&y<height;y++)if(!old.clear(x+dx,y,z+dz))free=false;
            }
            if(!free)continue;
            for(var l:labels)if(Math.abs(l.x()-APRON-x)<4&&Math.abs(l.z()-APRON-z)<4)free=false;
            if(!free)continue;
            planted++;
            if(planted%3==0) {
                box(PLANTER_SOIL,x-2,0,z-2,x+2,0,z+2);
                box(SHRUB,x-1,1,z-1,x+1,1,z+1);
                at(FLOWER,x-2,1,z);at(FLOWER,x+2,1,z);
            } else {
                box(LOG,x,1,z,x,4,z);
                for(int y=3;y<=6;y++) {
                    int radius=y==6?1:2;
                    for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)
                        if(Math.abs(dx)+Math.abs(dz)<=radius+1&&(dx!=0||dz!=0||y>4))at(y==6?SHRUB:LEAVES,x+dx,y,z+dz);
                }
            }
        }
    }
}
