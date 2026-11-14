package com.myriam.unrp.uo;



import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class LbFurGeometry {
   public static final int ATLAS_W = 80;
   public static final int ATLAS_H = 80;
   public static final int TAIL = 8;
   public static final int PARTS = 45;

   private LbFurGeometry() {
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.32F)).texOffs(46, 17).addBox(-4.5F, -1.5F, -2.5F, 9.0F, 3.0F, 5.0F, new CubeDeformation(0.5F)).texOffs(17, 17).addBox(-4.5F, 10.5F, -2.5F, 9.0F, 2.5F, 5.0F, new CubeDeformation(0.35F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      body.addOrReplaceChild("collar2", CubeListBuilder.create().texOffs(45, 49).addBox(-1.75F, 0.0F, -0.01F, 3.5F, 3.0F, 0.02F), PartPose.offsetAndRotation(3.7528F, -1.2F, -1.9952F, 0.42F, 2.244F, 0.0F));
      body.addOrReplaceChild("collar3", CubeListBuilder.create().texOffs(20, 49).addBox(-1.75F, 0.0F, -0.01F, 3.5F, 4.0F, 0.02F), PartPose.offsetAndRotation(4.6797F, -1.2F, -0.7121F, 0.58F, 1.7952F, 0.0F));
      body.addOrReplaceChild("collar4", CubeListBuilder.create().texOffs(11, 49).addBox(-1.75F, 0.0F, -0.01F, 3.5F, 4.0F, 0.02F), PartPose.offsetAndRotation(4.6797F, -1.2F, 0.7121F, 0.58F, 1.3464F, 0.0F));
      body.addOrReplaceChild("collar5", CubeListBuilder.create().texOffs(70, 41).addBox(-1.75F, 0.0F, -0.01F, 3.5F, 5.0F, 0.02F), PartPose.offsetAndRotation(3.7528F, -1.2F, 1.9952F, 0.62F, 0.8976F, 0.0F));
      body.addOrReplaceChild("collar6", CubeListBuilder.create().texOffs(61, 41).addBox(-1.75F, 0.0F, -0.01F, 3.5F, 5.0F, 0.02F), PartPose.offsetAndRotation(2.0826F, -1.2F, 2.8831F, 0.62F, 0.4488F, 0.0F));
      body.addOrReplaceChild("collar7", CubeListBuilder.create().texOffs(70, 41).addBox(-1.75F, 0.0F, -0.01F, 3.5F, 5.0F, 0.02F), PartPose.offsetAndRotation(0.0F, -1.2F, 3.2F, 0.62F, 0.0F, 0.0F));
      body.addOrReplaceChild("collar8", CubeListBuilder.create().texOffs(61, 41).addBox(-1.75F, 0.0F, -0.01F, 3.5F, 5.0F, 0.02F), PartPose.offsetAndRotation(-2.0826F, -1.2F, 2.8831F, 0.62F, -0.4488F, 0.0F));
      body.addOrReplaceChild("collar9", CubeListBuilder.create().texOffs(70, 41).addBox(-1.75F, 0.0F, -0.01F, 3.5F, 5.0F, 0.02F), PartPose.offsetAndRotation(-3.7528F, -1.2F, 1.9952F, 0.62F, -0.8976F, 0.0F));
      body.addOrReplaceChild("collar10", CubeListBuilder.create().texOffs(11, 49).addBox(-1.75F, 0.0F, -0.01F, 3.5F, 4.0F, 0.02F), PartPose.offsetAndRotation(-4.6797F, -1.2F, 0.7121F, 0.58F, -1.3464F, 0.0F));
      body.addOrReplaceChild("collar11", CubeListBuilder.create().texOffs(20, 49).addBox(-1.75F, 0.0F, -0.01F, 3.5F, 4.0F, 0.02F), PartPose.offsetAndRotation(-4.6797F, -1.2F, -0.7121F, 0.58F, -1.7952F, 0.0F));
      body.addOrReplaceChild("collar12", CubeListBuilder.create().texOffs(45, 49).addBox(-1.75F, 0.0F, -0.01F, 3.5F, 3.0F, 0.02F), PartPose.offsetAndRotation(-3.7528F, -1.2F, -1.9952F, 0.42F, -2.244F, 0.0F));
      body.addOrReplaceChild("hip0", CubeListBuilder.create().texOffs(70, 49).addBox(-1.5F, 0.0F, -0.01F, 3.0F, 3.5F, 0.02F), PartPose.offsetAndRotation(-5.0F, 10.6F, -1.0F, 0.5F, -1.5708F, 0.0F));
      body.addOrReplaceChild("hip1", CubeListBuilder.create().texOffs(0, 55).addBox(-1.5F, 0.0F, -0.01F, 3.0F, 3.5F, 0.02F), PartPose.offsetAndRotation(-5.0F, 10.6F, 1.0F, 0.5F, -1.5708F, 0.0F));
      body.addOrReplaceChild("hip2", CubeListBuilder.create().texOffs(70, 49).addBox(-1.5F, 0.0F, -0.01F, 3.0F, 3.5F, 0.02F), PartPose.offsetAndRotation(5.0F, 10.6F, -1.0F, 0.5F, 1.5708F, 0.0F));
      body.addOrReplaceChild("hip3", CubeListBuilder.create().texOffs(0, 55).addBox(-1.5F, 0.0F, -0.01F, 3.0F, 3.5F, 0.02F), PartPose.offsetAndRotation(5.0F, 10.6F, 1.0F, 0.5F, 1.5708F, 0.0F));
      PartDefinition p16 = body.addOrReplaceChild("tail0", CubeListBuilder.create().texOffs(36, 41).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 2.75F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, 10.6F, 2.2F, -0.9F, 0.0F, 0.0F));
      PartDefinition p17 = p16.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(36, 41).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 2.75F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 0.0F, 2.75F));
      PartDefinition p18 = p17.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(36, 41).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 2.75F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 2.75F));
      PartDefinition p19 = p18.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(49, 41).addBox(-1.25F, -1.25F, 0.0F, 2.5F, 2.5F, 2.75F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 2.75F));
      PartDefinition p20 = p19.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(49, 41).addBox(-1.25F, -1.25F, 0.0F, 2.5F, 2.5F, 2.75F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 2.75F));
      PartDefinition p21 = p20.addOrReplaceChild("tail5", CubeListBuilder.create().texOffs(0, 49).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 2.75F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 2.75F));
      PartDefinition p22 = p21.addOrReplaceChild("tail6", CubeListBuilder.create().texOffs(0, 49).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 2.75F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, 0.0F, 2.75F));
      PartDefinition p23 = p22.addOrReplaceChild("tail7", CubeListBuilder.create().texOffs(0, 49).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 2.75F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 0.0F, 2.75F));
      PartDefinition p24 = p23.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(23, 41).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 2.75F));
      p24.addOrReplaceChild("brush0", CubeListBuilder.create().texOffs(22, 55).addBox(-1.0F, 0.0F, -0.01F, 2.0F, 3.0F, 0.02F), PartPose.offsetAndRotation(0.0F, 0.0F, 1.6F, 1.1208F, 0.0F, 0.0F));
      p24.addOrReplaceChild("brush1", CubeListBuilder.create().texOffs(28, 55).addBox(-1.0F, 0.0F, -0.01F, 2.0F, 3.0F, 0.02F), PartPose.offsetAndRotation(0.0F, 0.0F, 1.6F, 1.1208F, 0.0F, 1.2566F));
      p24.addOrReplaceChild("brush2", CubeListBuilder.create().texOffs(22, 55).addBox(-1.0F, 0.0F, -0.01F, 2.0F, 3.0F, 0.02F), PartPose.offsetAndRotation(0.0F, 0.0F, 1.6F, 1.1208F, 0.0F, 2.5133F));
      p24.addOrReplaceChild("brush3", CubeListBuilder.create().texOffs(28, 55).addBox(-1.0F, 0.0F, -0.01F, 2.0F, 3.0F, 0.02F), PartPose.offsetAndRotation(0.0F, 0.0F, 1.6F, 1.1208F, 0.0F, 3.7699F));
      p24.addOrReplaceChild("brush4", CubeListBuilder.create().texOffs(22, 55).addBox(-1.0F, 0.0F, -0.01F, 2.0F, 3.0F, 0.02F), PartPose.offsetAndRotation(0.0F, 0.0F, 1.6F, 1.1208F, 0.0F, 5.0265F));
      root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(25, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.27F)).texOffs(62, 49).addBox(-3.15F, 1.0F, -2.77F, 3.0F, 3.0F, 0.5F).texOffs(54, 49).addBox(0.15F, 1.0F, -2.77F, 3.0F, 3.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition arm_r = root.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(0, 17).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.32F)).texOffs(21, 32).addBox(-3.5F, 5.5F, -2.5F, 5.0F, 2.5F, 5.0F, new CubeDeformation(0.4F)).texOffs(0, 41).addBox(-3.75F, 7.0F, -2.75F, 5.5F, 1.5F, 5.5F, new CubeDeformation(0.3F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
      arm_r.addOrReplaceChild("shoulder0", CubeListBuilder.create().texOffs(29, 49).addBox(-1.5F, 0.0F, -0.01F, 3.0F, 4.0F, 0.02F), PartPose.offsetAndRotation(-3.75F, -2.6F, -1.3F, 0.55F, -1.5708F, 0.0F));
      arm_r.addOrReplaceChild("shoulder1", CubeListBuilder.create().texOffs(37, 49).addBox(-1.5F, 0.0F, -0.01F, 3.0F, 4.0F, 0.02F), PartPose.offsetAndRotation(-3.75F, -2.6F, 0.0F, 0.55F, -1.5708F, 0.0F));
      arm_r.addOrReplaceChild("shoulder2", CubeListBuilder.create().texOffs(29, 49).addBox(-1.5F, 0.0F, -0.01F, 3.0F, 4.0F, 0.02F), PartPose.offsetAndRotation(-3.75F, -2.6F, 1.3F, 0.55F, -1.5708F, 0.0F));
      arm_r.addOrReplaceChild("elbow0", CubeListBuilder.create().texOffs(8, 55).addBox(-1.25F, 0.0F, -0.01F, 2.5F, 3.5F, 0.02F), PartPose.offsetAndRotation(-3.6F, 3.6F, 1.0F, 0.75F, -0.9708F, 0.0F));
      arm_r.addOrReplaceChild("elbow1", CubeListBuilder.create().texOffs(15, 55).addBox(-1.25F, 0.0F, -0.01F, 2.5F, 3.5F, 0.02F), PartPose.offsetAndRotation(-2.4F, 4.2F, 2.6F, 0.7F, 0.0F, 0.0F));
      PartDefinition arm_l = root.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.32F)).texOffs(21, 32).addBox(-1.5F, 5.5F, -2.5F, 5.0F, 2.5F, 5.0F, new CubeDeformation(0.4F)).texOffs(0, 41).addBox(-1.75F, 7.0F, -2.75F, 5.5F, 1.5F, 5.5F, new CubeDeformation(0.3F)), PartPose.offset(5.0F, 2.0F, 0.0F));
      arm_l.addOrReplaceChild("shoulder0", CubeListBuilder.create().texOffs(29, 49).addBox(-1.5F, 0.0F, -0.01F, 3.0F, 4.0F, 0.02F), PartPose.offsetAndRotation(3.75F, -2.6F, -1.3F, 0.55F, 1.5708F, 0.0F));
      arm_l.addOrReplaceChild("shoulder1", CubeListBuilder.create().texOffs(37, 49).addBox(-1.5F, 0.0F, -0.01F, 3.0F, 4.0F, 0.02F), PartPose.offsetAndRotation(3.75F, -2.6F, 0.0F, 0.55F, 1.5708F, 0.0F));
      arm_l.addOrReplaceChild("shoulder2", CubeListBuilder.create().texOffs(29, 49).addBox(-1.5F, 0.0F, -0.01F, 3.0F, 4.0F, 0.02F), PartPose.offsetAndRotation(3.75F, -2.6F, 1.3F, 0.55F, 1.5708F, 0.0F));
      arm_l.addOrReplaceChild("elbow0", CubeListBuilder.create().texOffs(8, 55).addBox(-1.25F, 0.0F, -0.01F, 2.5F, 3.5F, 0.02F), PartPose.offsetAndRotation(3.6F, 3.6F, 1.0F, 0.75F, 0.9708F, 0.0F));
      arm_l.addOrReplaceChild("elbow1", CubeListBuilder.create().texOffs(15, 55).addBox(-1.25F, 0.0F, -0.01F, 2.5F, 3.5F, 0.02F), PartPose.offsetAndRotation(2.4F, 4.2F, 2.6F, 0.7F, 0.0F, 0.0F));
      root.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(50, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.32F)).texOffs(0, 32).addBox(-2.5F, 9.0F, -2.5F, 5.0F, 2.5F, 5.0F, new CubeDeformation(0.4F)).texOffs(42, 32).addBox(-2.75F, 10.5F, -2.75F, 5.5F, 1.5F, 5.5F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
      root.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(50, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.32F)).texOffs(0, 32).addBox(-2.5F, 9.0F, -2.5F, 5.0F, 2.5F, 5.0F, new CubeDeformation(0.4F)).texOffs(42, 32).addBox(-2.75F, 10.5F, -2.75F, 5.5F, 1.5F, 5.5F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));
      return LayerDefinition.create(mesh, 80, 80);
   }
}
