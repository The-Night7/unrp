package com.myriam.unrp.item;

import com.myriam.unrp.UnrpMod;
import com.myriam.unrp.ability.AbilityEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Persistent owner, source ability and treatment rate, keyed by dimension and bed head. */
@Mod.EventBusSubscriber(modid=UnrpMod.MOD_ID)
public final class RecoveryBeds extends SavedData {
    public record Settings(UUID owner,String power,double rate) {
        public boolean mayEdit(UUID actor) {return owner.equals(actor);}
        public boolean mayRemove(UUID actor,boolean sourceActive) {return mayEdit(actor)&&!sourceActive;}
    }
    private final Map<String,Settings> beds=new HashMap<>();
    public static RecoveryBeds get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(RecoveryBeds::load,RecoveryBeds::new,"unrp_recovery_beds");
    }
    private static RecoveryBeds load(CompoundTag tag) {
        var data=new RecoveryBeds();
        for(Tag entry:tag.getList("beds",Tag.TAG_COMPOUND)) {
            var bed=(CompoundTag)entry;
            if(bed.hasUUID("owner"))data.beds.put(bed.getString("key"),new Settings(bed.getUUID("owner"),bed.getString("power"),Math.max(.1,Math.min(20,bed.getDouble("rate")))));
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        var list=new ListTag();beds.forEach((key,value)->{
            var entry=new CompoundTag();entry.putString("key",key);entry.putUUID("owner",value.owner);entry.putString("power",value.power);entry.putDouble("rate",value.rate);list.add(entry);
        });tag.put("beds",list);return tag;
    }
    private static String key(ServerLevel level,BlockPos head){return level.dimension().location()+"/"+head.asLong();}
    public static BlockPos head(net.minecraft.world.level.Level level,BlockPos pos) {
        var state=level.getBlockState(pos);
        return state.getBlock() instanceof RecoveryBedBlock&&state.getValue(BedBlock.PART)==BedPart.FOOT?pos.relative(state.getValue(BedBlock.FACING)):pos;
    }
    public Settings settings(ServerLevel level,BlockPos pos){return beds.get(key(level,head(level,pos)));}
    public void placed(ServerPlayer owner,BlockPos head) {
        String power=AbilityEngine.basePowerId(owner);
        beds.put(key(owner.serverLevel(),head),new Settings(owner.getUUID(),power==null?"":power,1));setDirty();
    }
    public void remove(ServerLevel level,BlockPos head){if(beds.remove(key(level,head))!=null)setDirty();}
    public static boolean powered(ServerLevel level,Settings settings) {
        if(settings==null)return false;
        // A physically placed creative/crafted bed without a source power still belongs to its placer.
        if(settings.power.isEmpty())return true;
        var owner=level.getServer().getPlayerList().getPlayer(settings.owner);
        return owner!=null&&AbilityEngine.isOn(owner,settings.power)&&!AbilityEngine.disabled(owner);
    }
    @SubscribeEvent public static void explosion(net.minecraftforge.event.level.ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos->event.getLevel().getBlockState(pos).getBlock() instanceof RecoveryBedBlock);
    }
    public static boolean mayDestroy(ServerPlayer player,Settings settings) {
        return settings!=null&&settings.mayRemove(player.getUUID(),!settings.power.isEmpty()&&powered(player.serverLevel(),settings));
    }
    public static boolean setRate(ServerPlayer player,BlockPos pos,double rate) {
        var level=player.serverLevel();var data=get(level);var settings=data.settings(level,pos);
        if(settings==null||!settings.mayEdit(player.getUUID())) {
            player.displayClientMessage(Component.literal("Only this bed's owner can change its rate."),true);return false;
        }
        if(!Double.isFinite(rate)||rate<.1||rate>20)return false;
        data.beds.put(key(level,head(level,pos)),new Settings(settings.owner,settings.power,rate));data.setDirty();
        player.displayClientMessage(Component.literal("Recovery rate: "+rate+" health points per second."),true);return true;
    }
    public static int commandRate(ServerPlayer player,double rate) {
        var hit=player.pick(5,0,false);
        if(hit.getType()!=HitResult.Type.BLOCK||!(hit instanceof net.minecraft.world.phys.BlockHitResult block)
                ||!(player.level().getBlockState(block.getBlockPos()).getBlock() instanceof RecoveryBedBlock)) {
            player.displayClientMessage(Component.literal("Look at your recovery bed first."),true);return 0;
        }
        return setRate(player,block.getBlockPos(),rate)?1:0;
    }
    @SubscribeEvent public static void protect(BlockEvent.BreakEvent event) {
        if(!(event.getLevel() instanceof ServerLevel level)||!(event.getPlayer() instanceof ServerPlayer player)
                ||!(event.getState().getBlock() instanceof RecoveryBedBlock))return;
        if(!mayDestroy(player,get(level).settings(level,event.getPos()))) {
            event.setCanceled(true);player.displayClientMessage(Component.literal("Only the owner can remove this bed, after disabling its source ability."),true);
        }
    }
}
