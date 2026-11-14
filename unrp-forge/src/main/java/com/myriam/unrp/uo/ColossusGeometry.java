package com.myriam.unrp.uo;



import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class ColossusGeometry {
   public static final int ATLAS_W = 256;
   public static final int ATLAS_H = 256;
   public static final float MODEL_SCALE = 0.62F;
   public static final int TAIL_SEGMENTS = 4;

   private ColossusGeometry() {
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition p0 = root.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(112, 30).addBox(-8.0F, -3.0F, -6.0F, 16.0F, 8.0F, 12.0F), PartPose.offset(0.0F, 3.961F, 1.0F));
      PartDefinition p0_0 = p0.addOrReplaceChild("thigh_r", CubeListBuilder.create().texOffs(39, 54).addBox(-4.5F, -1.0F, -4.5F, 9.0F, 9.0F, 9.0F).texOffs(138, 101).addBox(-4.0F, 0.0F, 4.6F, 8.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(-5.0F, 3.0F, 0.0F, -0.3F, 0.0F, 0.07F));
      PartDefinition p0_0_0 = p0_0.addOrReplaceChild("shin_r", CubeListBuilder.create().texOffs(97, 54).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 8.0F, 8.0F).texOffs(213, 101).addBox(-3.5F, 0.5F, 2.6F, 7.0F, 5.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 7.5F, 0.5F, 0.52F, 0.0F, 0.0F));
      p0_0_0.addOrReplaceChild("foot_r", CubeListBuilder.create().texOffs(45, 73).addBox(-4.5F, 0.0F, -7.5F, 9.0F, 3.0F, 11.0F).texOffs(38, 111).addBox(-4.5F, 0.5F, -9.5F, 9.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 6.5F, -0.5F, -0.22F, 0.0F, 0.0F));
      PartDefinition p0_1 = p0.addOrReplaceChild("thigh_l", CubeListBuilder.create().mirror().texOffs(39, 54).addBox(-4.5F, -1.0F, -4.5F, 9.0F, 9.0F, 9.0F).texOffs(138, 101).addBox(-4.0F, 0.0F, 4.6F, 8.0F, 7.0F, 1.0F).mirror(false), PartPose.offsetAndRotation(5.0F, 3.0F, 0.0F, -0.3F, 0.0F, -0.07F));
      PartDefinition p0_1_0 = p0_1.addOrReplaceChild("shin_l", CubeListBuilder.create().mirror().texOffs(97, 54).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 8.0F, 8.0F).texOffs(213, 101).addBox(-3.5F, 0.5F, 2.6F, 7.0F, 5.0F, 2.0F).mirror(false), PartPose.offsetAndRotation(0.0F, 7.5F, 0.5F, 0.52F, 0.0F, 0.0F));
      p0_1_0.addOrReplaceChild("foot_l", CubeListBuilder.create().mirror().texOffs(45, 73).addBox(-4.5F, 0.0F, -7.5F, 9.0F, 3.0F, 11.0F).texOffs(38, 111).addBox(-4.5F, 0.5F, -9.5F, 9.0F, 2.0F, 2.0F).mirror(false), PartPose.offsetAndRotation(0.0F, 6.5F, -0.5F, -0.22F, 0.0F, 0.0F));
      PartDefinition p0_2 = p0.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 88).addBox(-2.5F, -2.5F, 0.0F, 5.0F, 5.0F, 7.0F), PartPose.offsetAndRotation(0.0F, -1.0F, 5.5F, 0.78F, 0.0F, 0.0F));
      PartDefinition p0_2_0 = p0_2.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(73, 88).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 7.0F, -0.28F, 0.0F, 0.0F));
      PartDefinition p0_2_0_0 = p0_2_0.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(139, 88).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 7.0F, -0.28F, 0.0F, 0.0F));
      PartDefinition p0_2_0_0_0 = p0_2_0_0.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(23, 101).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 6.0F, -0.28F, 0.0F, 0.0F));
      p0_2_0_0_0.addOrReplaceChild("tail_brush", CubeListBuilder.create().texOffs(169, 30).addBox(-4.5F, -4.5F, -1.0F, 9.0F, 9.0F, 10.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 6.0F, -0.3F, 0.0F, 0.0F));
      PartDefinition p0_3 = p0.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(51, 30).addBox(-8.5F, -9.0F, -6.5F, 17.0F, 9.0F, 13.0F).texOffs(0, 101).addBox(-5.0F, -8.5F, -7.5F, 10.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.3F, 0.0F, 0.0F));
      p0_3.addOrReplaceChild("flank_r", CubeListBuilder.create().texOffs(208, 30).addBox(-0.5F, 0.0F, -5.5F, 1.0F, 8.0F, 11.0F), PartPose.offsetAndRotation(-8.6F, -8.5F, 0.0F, 0.0F, 0.0F, 0.25F));
      p0_3.addOrReplaceChild("flank_l", CubeListBuilder.create().mirror().texOffs(208, 30).addBox(-0.5F, 0.0F, -5.5F, 1.0F, 8.0F, 11.0F).mirror(false), PartPose.offsetAndRotation(8.6F, -8.5F, 0.0F, 0.0F, 0.0F, -0.25F));
      PartDefinition p0_3_2 = p0_3.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -13.0F, -8.0F, 24.0F, 13.0F, 16.0F).texOffs(130, 54).addBox(-9.5F, -15.5F, -2.0F, 19.0F, 4.0F, 11.0F).texOffs(125, 73).addBox(-13.5F, -16.0F, -9.5F, 27.0F, 5.0F, 8.0F).texOffs(69, 101).addBox(-10.0F, -11.0F, -10.0F, 9.0F, 6.0F, 2.0F).texOffs(92, 101).addBox(1.0F, -11.0F, -10.0F, 9.0F, 6.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, 0.1F, 0.0F, 0.0F));
      p0_3_2.addOrReplaceChild("mantle_r", CubeListBuilder.create().texOffs(81, 0).addBox(-0.5F, 0.0F, -7.0F, 1.0F, 11.0F, 14.0F), PartPose.offsetAndRotation(-10.5F, -15.5F, 1.0F, 0.0F, 0.0F, 0.45F));
      p0_3_2.addOrReplaceChild("mantle_l", CubeListBuilder.create().mirror().texOffs(81, 0).addBox(-0.5F, 0.0F, -7.0F, 1.0F, 11.0F, 14.0F).mirror(false), PartPose.offsetAndRotation(10.5F, -15.5F, 1.0F, 0.0F, 0.0F, -0.45F));
      p0_3_2.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(96, 88).addBox(-10.0F, 0.0F, 0.0F, 20.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -15.5F, 6.5F, 0.4F, 0.0F, 0.0F));
      p0_3_2.addOrReplaceChild("cape_low", CubeListBuilder.create().texOffs(160, 88).addBox(-9.0F, 0.0F, 0.0F, 18.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -10.5F, 8.6F, 0.22F, 0.0F, 0.0F));
      PartDefinition p0_3_2_4 = p0_3_2.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(191, 54).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 6.0F, 9.0F), PartPose.offsetAndRotation(0.0F, -13.0F, -5.5F, -0.2F, 0.0F, 0.0F));
      PartDefinition p0_3_2_4_0 = p0_3_2_4.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 30).addBox(-6.5F, -10.0F, -6.5F, 13.0F, 11.0F, 12.0F).texOffs(48, 88).addBox(-1.5F, -12.0F, -4.0F, 3.0F, 2.0F, 9.0F).texOffs(199, 88).addBox(-5.5F, -8.5F, -7.4F, 11.0F, 8.0F, 1.0F).texOffs(0, 111).addBox(-6.0F, -9.5F, -9.0F, 12.0F, 2.0F, 3.0F).texOffs(224, 88).addBox(-4.0F, -5.0F, -10.0F, 8.0F, 5.0F, 4.0F).texOffs(61, 111).addBox(-2.0F, -5.0F, -10.6F, 4.0F, 2.0F, 1.0F).texOffs(72, 111).addBox(-3.0F, 0.0F, -10.2F, 1.0F, 2.0F, 1.0F).texOffs(77, 111).addBox(2.0F, 0.0F, -10.2F, 1.0F, 2.0F, 1.0F).texOffs(196, 73).addBox(-9.0F, -6.5F, -4.5F, 3.0F, 6.0F, 7.0F).texOffs(217, 73).addBox(6.0F, -6.5F, -4.5F, 3.0F, 6.0F, 7.0F), PartPose.offsetAndRotation(0.0F, -2.0F, -3.0F, 0.12F, 0.0F, 0.0F));
      p0_3_2_4_0.addOrReplaceChild("ear_r", CubeListBuilder.create().texOffs(31, 111).addBox(-1.0F, -2.0F, -1.0F, 1.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-6.5F, -6.0F, -1.0F, 0.0F, 0.0F, -0.25F));
      p0_3_2_4_0.addOrReplaceChild("ear_l", CubeListBuilder.create().mirror().texOffs(31, 111).addBox(0.0F, -2.0F, -1.0F, 1.0F, 3.0F, 2.0F).mirror(false), PartPose.offsetAndRotation(6.5F, -6.0F, -1.0F, 0.0F, 0.0F, 0.25F));
      p0_3_2_4_0.addOrReplaceChild("eye_r", CubeListBuilder.create().texOffs(82, 111).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F), PartPose.offset(-2.7F, -7.0F, -7.6F));
      p0_3_2_4_0.addOrReplaceChild("eye_l", CubeListBuilder.create().texOffs(89, 111).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F), PartPose.offset(2.7F, -7.0F, -7.6F));
      p0_3_2_4_0.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(42, 101).addBox(-4.0F, 0.0F, -4.8F, 8.0F, 3.0F, 5.0F).texOffs(96, 111).addBox(-3.0F, -1.0F, -4.4F, 1.0F, 1.0F, 1.0F).texOffs(101, 111).addBox(2.0F, -1.0F, -4.4F, 1.0F, 1.0F, 1.0F).texOffs(186, 101).addBox(-4.5F, 2.0F, -3.5F, 9.0F, 3.0F, 4.0F), PartPose.offsetAndRotation(0.0F, -1.0F, -5.0F, 0.08F, 0.0F, 0.0F));
      PartDefinition p0_3_2_5 = p0_3_2.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(0, 54).addBox(-6.0F, -3.5F, -5.0F, 9.0F, 8.0F, 10.0F).texOffs(76, 54).addBox(-6.6F, -2.0F, -4.6F, 1.0F, 9.0F, 9.0F), PartPose.offsetAndRotation(-12.0F, -10.0F, -1.0F, 0.0F, 0.0F, 0.1F));
      PartDefinition p0_3_2_5_0 = p0_3_2_5.addOrReplaceChild("upper_arm_r", CubeListBuilder.create().texOffs(153, 0).addBox(-4.5F, -1.0F, -4.5F, 9.0F, 15.0F, 9.0F).texOffs(190, 0).addBox(-5.6F, 0.0F, -5.0F, 1.0F, 14.0F, 10.0F).texOffs(238, 73).addBox(-4.0F, 1.0F, 4.6F, 8.0F, 12.0F, 1.0F).texOffs(115, 101).addBox(-4.0F, 11.0F, 3.0F, 8.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(-2.0F, 3.0F, 0.0F, -0.15F, 0.0F, 0.05F));
      PartDefinition p0_3_2_5_0_0 = p0_3_2_5_0.addOrReplaceChild("forearm_r", CubeListBuilder.create().texOffs(112, 0).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 14.0F, 10.0F).texOffs(0, 73).addBox(-5.5F, 10.0F, -5.5F, 11.0F, 3.0F, 11.0F).texOffs(25, 88).addBox(-5.0F, 0.5F, 5.0F, 10.0F, 11.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, -0.34F, 0.0F, 0.0F));
      PartDefinition p0_3_2_5_0_0_0 = p0_3_2_5_0_0.addOrReplaceChild("hand_r", CubeListBuilder.create().texOffs(86, 73).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 5.0F, 9.0F), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.28F, 0.0F, 0.0F));
      p0_3_2_5_0_0_0.addOrReplaceChild("fingers_r", CubeListBuilder.create().texOffs(157, 101).addBox(-5.0F, -1.0F, -2.0F, 10.0F, 3.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 4.0F, -4.0F, 1.1F, 0.0F, 0.0F));
      p0_3_2_5_0_0_0.addOrReplaceChild("thumb_r", CubeListBuilder.create().texOffs(232, 101).addBox(-1.0F, -1.0F, -2.5F, 2.0F, 3.0F, 3.0F), PartPose.offsetAndRotation(4.0F, 2.0F, -3.0F, 0.6F, 0.0F, -0.3F));
      PartDefinition p0_3_2_6 = p0_3_2.addOrReplaceChild("shoulder_l", CubeListBuilder.create().mirror().texOffs(0, 54).addBox(-3.0F, -3.5F, -5.0F, 9.0F, 8.0F, 10.0F).texOffs(76, 54).addBox(5.6F, -2.0F, -4.6F, 1.0F, 9.0F, 9.0F).mirror(false), PartPose.offsetAndRotation(12.0F, -10.0F, -1.0F, 0.0F, 0.0F, -0.1F));
      PartDefinition p0_3_2_6_0 = p0_3_2_6.addOrReplaceChild("upper_arm_l", CubeListBuilder.create().mirror().texOffs(153, 0).addBox(-4.5F, -1.0F, -4.5F, 9.0F, 15.0F, 9.0F).texOffs(190, 0).addBox(4.6F, 0.0F, -5.0F, 1.0F, 14.0F, 10.0F).texOffs(238, 73).addBox(-4.0F, 1.0F, 4.6F, 8.0F, 12.0F, 1.0F).texOffs(115, 101).addBox(-4.0F, 11.0F, 3.0F, 8.0F, 5.0F, 3.0F).mirror(false), PartPose.offsetAndRotation(2.0F, 3.0F, 0.0F, -0.15F, 0.0F, -0.05F));
      PartDefinition p0_3_2_6_0_0 = p0_3_2_6_0.addOrReplaceChild("forearm_l", CubeListBuilder.create().mirror().texOffs(112, 0).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 14.0F, 10.0F).texOffs(0, 73).addBox(-5.5F, 10.0F, -5.5F, 11.0F, 3.0F, 11.0F).texOffs(25, 88).addBox(-5.0F, 0.5F, 5.0F, 10.0F, 11.0F, 1.0F).mirror(false), PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, -0.34F, 0.0F, 0.0F));
      PartDefinition p0_3_2_6_0_0_0 = p0_3_2_6_0_0.addOrReplaceChild("hand_l", CubeListBuilder.create().mirror().texOffs(86, 73).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 5.0F, 9.0F).mirror(false), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.28F, 0.0F, 0.0F));
      p0_3_2_6_0_0_0.addOrReplaceChild("fingers_l", CubeListBuilder.create().mirror().texOffs(157, 101).addBox(-5.0F, -1.0F, -2.0F, 10.0F, 3.0F, 4.0F).mirror(false), PartPose.offsetAndRotation(0.0F, 4.0F, -4.0F, 1.1F, 0.0F, 0.0F));
      p0_3_2_6_0_0_0.addOrReplaceChild("thumb_l", CubeListBuilder.create().mirror().texOffs(232, 101).addBox(-1.0F, -1.0F, -2.5F, 2.0F, 3.0F, 3.0F).mirror(false), PartPose.offsetAndRotation(-4.0F, 2.0F, -3.0F, 0.6F, 0.0F, 0.3F));
      return LayerDefinition.create(mesh, 256, 256);
   }
}
