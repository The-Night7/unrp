package com.myriam.unrp.uo;



import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class OriginModels {
   public static final ModelLayerLocation SEGMENTED_TAIL = loc("segmented_tail");
   public static final ModelLayerLocation SARC_TEMPEST = loc("sarcophage_tempest");
   public static final ModelLayerLocation SAND_PROPS = loc("sand_props");
   public static final ModelLayerLocation SAND_ROOT = loc("sand_root");
   public static final ModelLayerLocation PHAGE = loc("phage");
   public static final ModelLayerLocation ANATHEMA_PELT = loc("anathema_pelt");
   public static final ModelLayerLocation APE_SHAPE = loc("lb_ape_shape");
   public static final ModelLayerLocation LB_FUR = loc("lb_fur");
   public static final ModelLayerLocation LB_HAIR = loc("lb_hair");
   public static final ModelLayerLocation DREAD_PELT = loc("dread_pelt");
   public static final ModelLayerLocation WYRM_GAUNTLET = loc("wyrm_gauntlet");
   public static final ModelLayerLocation WYRM_PLATE = loc("wyrm_plate");
   public static final ModelLayerLocation WYRM_WINGS = loc("wyrm_wings");
   public static final ModelLayerLocation WYRM_TAIL = loc("wyrm_tail");

   private static ModelLayerLocation loc(String path) {
      return new ModelLayerLocation(new ResourceLocation("unrp", path), "main");
   }

   @SubscribeEvent
   public static void onLayerDefs(EntityRenderersEvent.RegisterLayerDefinitions event) {
      event.registerLayerDefinition(SEGMENTED_TAIL, SegmentedTailModel::createLayer);
      event.registerLayerDefinition(SARC_TEMPEST, SarcophageTempestModel::createLayer);
      event.registerLayerDefinition(SAND_PROPS, SandPropModel::createLayer);
      event.registerLayerDefinition(SAND_ROOT, SandRootModel::createLayer);
      event.registerLayerDefinition(PHAGE, PhageModel::createLayer);
      event.registerLayerDefinition(APE_SHAPE, ColossusGeometry::createLayer);
      event.registerLayerDefinition(LB_FUR, LbFurLayer::createLayer);
      event.registerLayerDefinition(LB_HAIR, LbHairModel::createLayer);
   }
}
