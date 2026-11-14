package com.myriam.unrp.uo;



import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class ParticleBlockColorHandler {
   @SubscribeEvent
   public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
      event.register((state, level, pos, tintIndex) -> -16777216 | AuraColorResolver.forViewer(15160448), new Block[]{(Block)com.myriam.unrp.item.ModBlocks.PARTICLE_BLOCK.get()});
   }
}
