package com.myriam.unrp.home;

import com.myriam.unrp.UnrpMod;
import com.myriam.unrp.item.UnItems;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** One actual, persistent /sethome per player; Covenant reads the same record. */
@Mod.EventBusSubscriber(modid=UnrpMod.MOD_ID)
public final class HomeCommands {
    public record Home(String dimension,double x,double y,double z,float yaw,float pitch) {}
    public static Home home(ServerPlayer player) {
        var data=UnItems.persisted(player);
        if(!data.contains("unrp_home",10))return null;
        var tag=data.getCompound("unrp_home");
        return new Home(tag.getString("dimension"),tag.getDouble("x"),tag.getDouble("y"),tag.getDouble("z"),tag.getFloat("yaw"),tag.getFloat("pitch"));
    }
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        var set=Commands.literal("sethome").executes(ctx->{
            var player=ctx.getSource().getPlayerOrException();var tag=new net.minecraft.nbt.CompoundTag();
            tag.putString("dimension",player.serverLevel().dimension().location().toString());
            tag.putDouble("x",player.getX());tag.putDouble("y",player.getY());tag.putDouble("z",player.getZ());
            tag.putFloat("yaw",player.getYRot());tag.putFloat("pitch",player.getXRot());
            UnItems.persisted(player).put("unrp_home",tag);
            player.sendSystemMessage(Component.literal("Home saved."));return 1;
        });
        var go=Commands.literal("home").executes(ctx->{
            var player=ctx.getSource().getPlayerOrException();var home=home(player);
            if(home==null){player.sendSystemMessage(Component.literal("Set your home with /sethome first."));return 0;}
            var level=player.server.getLevel(ResourceKey.create(Registries.DIMENSION,new ResourceLocation(home.dimension)));
            if(level==null){player.sendSystemMessage(Component.literal("Your home's dimension is unavailable."));return 0;}
            if(player.isPassenger())player.stopRiding();if(player.isSleeping())player.stopSleeping();
            player.teleportTo(level,home.x,home.y,home.z,home.yaw,home.pitch);player.fallDistance=0;return 1;
        });
        event.getDispatcher().register(Commands.literal("unrp").then(set).then(go));
        if(event.getDispatcher().getRoot().getChild("sethome")==null)event.getDispatcher().register(set);
        if(event.getDispatcher().getRoot().getChild("home")==null)event.getDispatcher().register(go);
    }
}
