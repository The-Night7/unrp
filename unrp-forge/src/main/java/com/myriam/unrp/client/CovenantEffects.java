package com.myriam.unrp.client;

import com.google.gson.JsonArray;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.List;

/** The actual Covenant seal, law parchments and protection glyphs, rendered from server visual state. */
@Mod.EventBusSubscriber(modid="unrp",value=Dist.CLIENT)
public final class CovenantEffects {
    public static final ResourceLocation PARCHMENT=texture("parchment");
    private static final ResourceLocation WHITE=new ResourceLocation("unrp","textures/misc/white.png");
    private static final ResourceLocation SIGNATURE=texture("signature");
    private static final ResourceLocation SEAL=texture("court_seal"),SHIELD=texture("indemnity");
    private record Law(int id,String text) {}
    private record Court(String dimension,int entity,boolean follow,boolean district,Vec3 center,double radius,int tier,boolean breach,boolean covered,List<Law> laws,int total) {}
    private static final List<Court> COURTS=new ArrayList<>();
    private static long updated;
    private CovenantEffects() {}
    private static ResourceLocation texture(String name) {return new ResourceLocation("unrp","textures/covenant/"+name+".png");}
    public static void clear() {COURTS.clear();updated=0;}
    public static void receive(JsonArray array) {
        COURTS.clear();updated=System.currentTimeMillis();
        for(var item:array) {
            if(COURTS.size()>=8)break;
            var o=item.getAsJsonObject();var laws=new ArrayList<Law>();
            for(var entry:o.getAsJsonArray("laws")) {if(laws.size()>=3)break;var law=entry.getAsJsonObject();laws.add(new Law(law.get("id").getAsInt(),law.get("text").getAsString()));}
            COURTS.add(new Court(o.get("dimension").getAsString(),o.get("entity").getAsInt(),o.get("follow").getAsBoolean(),o.get("district").getAsBoolean(),
                    new Vec3(o.get("x").getAsDouble(),o.get("y").getAsDouble(),o.get("z").getAsDouble()),o.get("radius").getAsDouble(),
                    o.get("tier").getAsInt(),o.get("breach").getAsBoolean(),o.get("covered").getAsBoolean(),List.copyOf(laws),o.get("total").getAsInt()));
        }
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var mc=Minecraft.getInstance();
        if(mc.level==null||System.currentTimeMillis()-updated>6000)return;
        var pose=event.getPoseStack();var camera=event.getCamera();var origin=camera.getPosition();var buffer=mc.renderBuffers().bufferSource();
        for(var court:COURTS) {
            if(!court.dimension.equals(mc.level.dimension().location().toString()))continue;
            Vec3 center=court.center;var entity=mc.level.getEntity(court.entity);
            if(court.follow&&entity!=null)center=entity.getPosition(event.getPartialTick());
            boolean centerVisible=center.distanceToSqr(origin)<=160*160&&mc.level.hasChunkAt(net.minecraft.core.BlockPos.containing(center));
            boolean animated=com.myriam.unrp.look.ClientHudConfig.screenEffects();
            double clock=animated?mc.level.getGameTime()+event.getPartialTick():0;
            float alpha=0.8f;
            if(com.myriam.unrp.look.ClientHudConfig.screenEffects())alpha+=0.1f*(float)Math.sin((mc.level.getGameTime()+event.getPartialTick())*.045);
            if(centerVisible) {
                pose.pushPose();pose.translate(center.x-origin.x,ground(mc.level,center)+.035-origin.y,center.z-origin.z);
                horizontal(pose,buffer.getBuffer(RenderType.entityTranslucent(SEAL)),1.9f,court.breach?0xE6A16D:0xFFFFFF,alpha);
                // Fine segmented inlay around the seal: opaque pixel edges, no blurred glow.
                pose.translate(0,.006,0);
                var gold=buffer.getBuffer(RenderType.entityTranslucent(WHITE));
                int ink=court.breach?0xE9A067:0xE4BD64;
                for(int tick=0;tick<48;tick++) {
                    double angle=tick*Math.PI/24;
                    arc(pose,gold,2.08,angle,angle+.085,.022f,ink,.65f);
                    if(tick%4==0) {
                        pose.pushPose();pose.translate(Math.cos(angle)*2.28,.004,Math.sin(angle)*2.28);
                        pose.mulPose(Axis.YP.rotationDegrees((float)(-angle*180/Math.PI+45)));
                        horizontal(pose,gold,.045f,0xFFE5A0,alpha);pose.popPose();
                    }
                }
                pose.popPose();
            }
            // A sparse local view of the actual boundary, never a misleading radius-16 ring for a 500m Court.
            int segments=Math.min(384,Math.max(64,(int)(court.radius*3)));
            for(int i=0;i<segments;i++) {
                double angle=i*Math.PI*2/segments;double x=center.x+Math.cos(angle)*court.radius,z=center.z+Math.sin(angle)*court.radius;
                if((x-origin.x)*(x-origin.x)+(z-origin.z)*(z-origin.z)>80*80)continue;
                var at=net.minecraft.core.BlockPos.containing(x,center.y,z);
                if(!mc.level.hasChunkAt(at))continue;
                int y=mc.level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,at.getX(),at.getZ());
                if(!court.district) {
                    // Ordinary Courts are spherical; district jurisdiction uses a horizontal radius.
                    double dy=y-center.y;
                    if(Math.abs(dy)>=court.radius)continue;
                    double radius=Math.sqrt(court.radius*court.radius-dy*dy);
                    x=center.x+Math.cos(angle)*radius;z=center.z+Math.sin(angle)*radius;
                    at=net.minecraft.core.BlockPos.containing(x,center.y,z);
                    if(!mc.level.hasChunkAt(at))continue;
                    y=mc.level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,at.getX(),at.getZ());
                }
                pose.pushPose();pose.translate(x-origin.x,y+.035-origin.y,z-origin.z);
                // Short tangent strokes make the real boundary readable without a wall of particles.
                pose.mulPose(Axis.YP.rotationDegrees((float)(-angle*180/Math.PI)));
                var edge=buffer.getBuffer(RenderType.entityTranslucent(WHITE));
                float half=(float)Math.min(.7,court.radius*Math.PI/segments*.8);
                strip(pose,edge,-.025f,-half,.025f,half,court.breach?0xE9A067:0xD4AA53,.52f);
                if(i%8==0) {
                    horizontal(pose,buffer.getBuffer(RenderType.entityTranslucent(SIGNATURE)),.19f,0xFFFFFF,.85f);
                    strip(pose,edge,-.12f,-.025f,.12f,.025f,0xFFE5A0,.85f);
                }
                pose.popPose();
            }
            if(centerVisible&&court.covered)for(int i=0;i<3;i++) {
                double angle=i*Math.PI*2/3+clock*.012;
                pose.pushPose();pose.translate(center.x+Math.cos(angle)*.8-origin.x,center.y+1.15+Math.sin(clock*.04+i*2)*.07-origin.y,center.z+Math.sin(angle)*.8-origin.z);
                pose.mulPose(camera.rotation());quad(pose,buffer.getBuffer(RenderType.entityTranslucent(SHIELD)),-.18f,-.24f,.18f,.24f,0xFFFFFF,.75f);pose.popPose();
            }
            if(center.distanceToSqr(origin)>24*24)continue;
            for(int i=0;i<court.laws.size();i++) {
                var law=court.laws.get(i);double angle=i*Math.PI*2/Math.max(3,court.laws.size())+.45;
                float opacity=(float)Math.min(1,Math.max(0,(24-center.distanceTo(origin))/6));
                int textAlpha=Math.max(4,(int)(opacity*255))<<24;
                var lines=mc.font.split(Component.literal(law.text),156);int height=30+lines.size()*10;
                pose.pushPose();pose.translate(center.x+Math.cos(angle)*2.3-origin.x,center.y+2.8+Math.sin(clock*.025+i*2)*.045-origin.y,center.z+Math.sin(angle)*2.3-origin.z);
                pose.mulPose(camera.rotation());pose.scale(-.011f,-.011f,.011f);
                quad(pose,buffer.getBuffer(RenderType.entityTranslucent(PARCHMENT)),-90,0,90,height,0xFFFFFF,opacity);
                pose.translate(0,0,.03);
                mc.font.drawInBatch("COVENANT  /  LAW "+law.id,-76,8,textAlpha|0x59412A,false,pose.last().pose(),buffer,net.minecraft.client.gui.Font.DisplayMode.NORMAL,0,0xF000F0);
                int y=25;for(var line:lines){mc.font.drawInBatch(line,-76,y,textAlpha|0x392C23,false,pose.last().pose(),buffer,net.minecraft.client.gui.Font.DisplayMode.NORMAL,0,0xF000F0);y+=10;}
                pose.popPose();
            }
        }
        buffer.endBatch();
    }
    private static void strip(PoseStack pose,VertexConsumer out,float x0,float z0,float x1,float z1,int color,float alpha) {
        vertex(pose,out,x0,0,z0,0,0,color,alpha,0,1,0);vertex(pose,out,x0,0,z1,0,1,color,alpha,0,1,0);
        vertex(pose,out,x1,0,z1,1,1,color,alpha,0,1,0);vertex(pose,out,x1,0,z0,1,0,color,alpha,0,1,0);
    }
    private static void arc(PoseStack pose,VertexConsumer out,double radius,double a,double b,float thickness,int color,float alpha) {
        double inner=radius-thickness,outer=radius+thickness;
        vertex(pose,out,(float)(Math.cos(a)*inner),0,(float)(Math.sin(a)*inner),0,0,color,alpha,0,1,0);
        vertex(pose,out,(float)(Math.cos(b)*inner),0,(float)(Math.sin(b)*inner),0,1,color,alpha,0,1,0);
        vertex(pose,out,(float)(Math.cos(b)*outer),0,(float)(Math.sin(b)*outer),1,1,color,alpha,0,1,0);
        vertex(pose,out,(float)(Math.cos(a)*outer),0,(float)(Math.sin(a)*outer),1,0,color,alpha,0,1,0);
    }
    private static double ground(net.minecraft.client.multiplayer.ClientLevel level,Vec3 center) {
        var at=net.minecraft.core.BlockPos.containing(center);
        for(int i=0;i<=8;i++) {
            var pos=at.below(i);var shape=level.getBlockState(pos).getCollisionShape(level,pos);
            if(!shape.isEmpty())return Math.min(center.y,pos.getY()+shape.max(net.minecraft.core.Direction.Axis.Y));
        }
        return Math.min(center.y,level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,at.getX(),at.getZ()));
    }
    private static void horizontal(PoseStack pose,VertexConsumer out,float r,int color,float alpha) {
        vertex(pose,out,-r,0,-r,0,0,color,alpha,0,1,0);vertex(pose,out,-r,0,r,0,1,color,alpha,0,1,0);
        vertex(pose,out,r,0,r,1,1,color,alpha,0,1,0);vertex(pose,out,r,0,-r,1,0,color,alpha,0,1,0);
    }
    public static void quad(PoseStack pose,VertexConsumer out,float x0,float y0,float x1,float y1,int color,float alpha) {
        vertex(pose,out,x0,y1,0,0,1,color,alpha,0,0,-1);vertex(pose,out,x1,y1,0,1,1,color,alpha,0,0,-1);
        vertex(pose,out,x1,y0,0,1,0,color,alpha,0,0,-1);vertex(pose,out,x0,y0,0,0,0,color,alpha,0,0,-1);
    }
    private static void vertex(PoseStack pose,VertexConsumer out,float x,float y,float z,float u,float v,int color,float alpha,float nx,float ny,float nz) {
        out.vertex(pose.last().pose(),x,y,z).color((color>>16)&255,(color>>8)&255,color&255,(int)(alpha*255)).uv(u,v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(pose.last().normal(),nx,ny,nz).endVertex();
    }
}
