package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class SandPropModel extends Model {
   public final ModelPart shard;
   public final ModelPart chunk;
   public final ModelPart crest;
   public final ModelPart slab;

   public SandPropModel(ModelPart root) {
      super(RenderType::entityCutoutNoCull);
      this.shard = root.getChild("shard");
      this.chunk = root.getChild("chunk");
      this.crest = root.getChild("crest");
      this.slab = root.getChild("slab");
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("shard", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.ZERO);
      root.addOrReplaceChild("chunk", CubeListBuilder.create().texOffs(14, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.ZERO);
      root.addOrReplaceChild("crest", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -10.0F, -0.5F, 6.0F, 10.0F, 1.0F), PartPose.ZERO);
      root.addOrReplaceChild("slab", CubeListBuilder.create().texOffs(0, 14).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 1.0F, 8.0F), PartPose.ZERO);
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void place(ModelPart part, float x, float y, float z, float xRot, float yRot, float zRot) {
      part.setPos(x, y, z);
      part.setRotation(xRot, yRot, zRot);
   }

   public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
   }
}
