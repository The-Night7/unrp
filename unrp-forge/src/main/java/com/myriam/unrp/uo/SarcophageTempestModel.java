package com.myriam.unrp.uo;



import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;

public class SarcophageTempestModel extends HumanoidModel<AbstractClientPlayer> {
   public SarcophageTempestModel(ModelPart root) {
      super(root);
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition p0 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.525F, -4.625F, 10.0F, 8.0F, 9.0F, new CubeDeformation(-0.25F, 0.225F, 0.125F)).texOffs(19, 30).addBox(-4.0F, -8.3F, -5.45F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.1F, -0.1F, -0.1F)).texOffs(0, 18).addBox(-5.0F, -7.5F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(-0.05F, 0.1F, -0.05F)).texOffs(46, 51).addBox(-1.0F, -5.1F, 4.35F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F, 0.1F, 0.05F)).texOffs(39, 0).addBox(4.4F, -6.25F, -3.0F, 1.0F, 10.0F, 4.0F, new CubeDeformation(-0.05F, 0.25F, 0.2F)).texOffs(50, 0).addBox(-5.4F, -6.25F, -3.0F, 1.0F, 10.0F, 4.0F, new CubeDeformation(-0.05F, 0.25F, 0.2F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      p0.addOrReplaceChild("earfin_l", CubeListBuilder.create().texOffs(21, 42).addBox(-0.5F, -4.7F, -1.5F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F, -0.1F, 0.2F)), PartPose.offsetAndRotation(3.2F, -8.6F, 0.6F, -0.35F, 0.0F, 0.25F));
      p0.addOrReplaceChild("earfin_r", CubeListBuilder.create().texOffs(30, 42).addBox(-0.5F, -4.7F, -1.5F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F, -0.1F, 0.2F)), PartPose.offsetAndRotation(-3.2F, -8.6F, 0.6F, -0.35F, 0.0F, -0.25F));
      root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(25, 51).addBox(-4.5F, -0.35F, -2.95F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.1F, -0.05F, -0.2F)).texOffs(35, 59).addBox(-4.5F, -0.35F, 1.95F, 9.0F, 3.0F, 1.0F, new CubeDeformation(0.1F, -0.05F, -0.2F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition p2 = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(41, 18).addBox(-3.225F, 2.8F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.225F, 0.2F, -0.05F)).texOffs(38, 30).addBox(-4.4F, 8.4F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.1F, 0.2F, 0.1F)).texOffs(39, 42).addBox(-4.9F, 8.4F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(-0.25F, 0.0F, -0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
      p2.addOrReplaceChild("hclaw_r0", CubeListBuilder.create().texOffs(53, 51).addBox(-0.5F, 0.2F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F, 0.2F, -0.1F)), PartPose.offsetAndRotation(0.0F, 11.3F, -1.0F, -0.25F, 0.0F, 0.0F));
      p2.addOrReplaceChild("hclaw_r1", CubeListBuilder.create().texOffs(0, 59).addBox(-0.5F, 0.2F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F, 0.2F, -0.1F)), PartPose.offsetAndRotation(-1.6F, 11.3F, -1.0F, -0.25F, 0.0F, 0.0F));
      p2.addOrReplaceChild("hclaw_r2", CubeListBuilder.create().texOffs(7, 59).addBox(-0.5F, 0.2F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F, 0.2F, -0.1F)), PartPose.offsetAndRotation(-3.2F, 11.3F, -1.0F, -0.25F, 0.0F, 0.0F));
      PartDefinition p3 = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 30).addBox(-0.775F, 2.8F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.225F, 0.2F, -0.05F)).texOffs(0, 42).addBox(-0.6F, 8.4F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.1F, 0.2F, 0.1F)).texOffs(0, 51).addBox(-1.1F, 8.4F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(-0.25F, 0.0F, -0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
      p3.addOrReplaceChild("hclaw_l0", CubeListBuilder.create().texOffs(14, 59).addBox(-0.5F, 0.2F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F, 0.2F, -0.1F)), PartPose.offsetAndRotation(0.0F, 11.3F, -1.0F, -0.25F, 0.0F, 0.0F));
      p3.addOrReplaceChild("hclaw_l1", CubeListBuilder.create().texOffs(21, 59).addBox(-0.5F, 0.2F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F, 0.2F, -0.1F)), PartPose.offsetAndRotation(1.6F, 11.3F, -1.0F, -0.25F, 0.0F, 0.0F));
      p3.addOrReplaceChild("hclaw_l2", CubeListBuilder.create().texOffs(28, 59).addBox(-0.5F, 0.2F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F, 0.2F, -0.1F)), PartPose.offsetAndRotation(3.2F, 11.3F, -1.0F, -0.25F, 0.0F, 0.0F));
      root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
      root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
      root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      return LayerDefinition.create(mesh, 64, 64);
   }
}
