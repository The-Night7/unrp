package com.myriam.unrp.item;

import com.myriam.unrp.UnrpMod;
import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.data.Models;
import com.myriam.unrp.data.UnodexData;
import com.myriam.unrp.player.PlayerStateService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Real two-part medical bed: synchronized vanilla lying pose, gradual server-side treatment. */
@Mod.EventBusSubscriber(modid=UnrpMod.MOD_ID)
public final class RecoveryBedBlock extends BedBlock {
    public RecoveryBedBlock(Properties properties) {super(DyeColor.CYAN,properties);}
    @Override public RenderShape getRenderShape(BlockState state) {return RenderShape.MODEL;}
    // Static model; no vanilla coloured-bed renderer or block entity is needed.
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) {return null;}
    @Override public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context) {
        var shape=super.getShape(state,level,pos,context);
        if(state.getValue(PART)==BedPart.HEAD)shape=net.minecraft.world.phys.shapes.Shapes.or(shape,
                MachineShapes.rotate(net.minecraft.world.level.block.Block.box(11,9,0,16,20,3),state.getValue(FACING)));
        return shape;
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(player.isSpectator()||!player.isAlive()||player.isSleeping())return InteractionResult.CONSUME;
        BlockPos head=state.getValue(PART)==BedPart.HEAD?pos:pos.relative(state.getValue(FACING));
        BlockState pillow=level.getBlockState(head);
        if(!pillow.is(this))return InteractionResult.CONSUME;
        if(pillow.getValue(OCCUPIED)) {
            player.displayClientMessage(Component.literal("This recovery bed is occupied."),true);return InteractionResult.CONSUME;
        }
        BlockPos foot=head.relative(pillow.getValue(FACING).getOpposite());
        if(!level.getBlockState(foot).is(this))return InteractionResult.CONSUME;
        if(!level.getBlockState(head.above()).getCollisionShape(level,head.above()).isEmpty()
                ||!level.getBlockState(foot.above()).getCollisionShape(level,foot.above()).isEmpty()) {
            player.displayClientMessage(Component.literal("Clear the space above the recovery bed."),true);return InteractionResult.CONSUME;
        }
        // Bypass night, monster and dimension restrictions; this is treatment, never a spawn-setting bed.
        player.startSleeping(head);
        player.displayClientMessage(Component.literal("Recovery started. Leave the bed at any time to stop treatment."),true);
        return InteractionResult.CONSUME;
    }
    public static boolean resting(Player player) {
        return player.isSleeping()&&player.getSleepingPos().filter(pos->player.level().getBlockState(pos).getBlock() instanceof RecoveryBedBlock).isPresent();
    }
    @SubscribeEvent public static void sleepingTime(SleepingTimeCheckEvent event) {
        if(event.getSleepingLocation().filter(pos->event.getEntity().level().getBlockState(pos).getBlock() instanceof RecoveryBedBlock).isPresent())event.setResult(Event.Result.ALLOW);
    }
    @SubscribeEvent public static void treatment(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer player)||!resting(player)||!player.isAlive())return;
        if(player.tickCount%20!=0)return;
        // One heart every two seconds, rather than an instant full heal.
        player.heal(1.0f);
        ClaimData data=ClaimData.get(player.server);
        var profile=data.profileOf(player.getUUID()).orElse(null);
        if(profile!=null) {
            Models.Character character=ClaimData.Profile.CHARACTER.equals(profile.kind)?UnodexData.get().characters.get(profile.characterId):null;
            int maximum=PlayerStateService.maxAura(PlayerStateService.stats(player.server,player.getUUID(),profile,character));
            if(profile.abilityOf(character)!=null&&!profile.lifelong&&profile.aura>=0&&profile.aura<maximum
                    &&!com.myriam.unrp.ability.AbilityEngine.blocksRegen(player.getUUID())) {
                profile.aura=RecoveryTreatment.nextAura(profile.aura,maximum);
                data.setDirty();PlayerStateService.send(player);
            }
        }
        player.displayClientMessage(Component.literal("Recovery bed · Health "+(int)Math.ceil(player.getHealth())+" / "+(int)Math.ceil(player.getMaxHealth())+" · Leave bed to stop"),true);
    }
}
