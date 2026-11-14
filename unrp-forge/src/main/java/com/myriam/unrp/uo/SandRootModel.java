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
import net.minecraft.util.Mth;

public class SandRootModel extends Model {
   public static final float LENGTH = 6.0F;
   public final ModelPart root;

   public SandRootModel(ModelPart root) {
      super(RenderType::entityCutoutNoCull);
      this.root = root.getChild("root");
   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      root.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F), PartPose.ZERO);
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void place(int slot, float grow) {
      float f = Mth.clamp(grow * 8.0F, 0.0F, 1.0F);
      double[] p = SarcophageTailPose.rootPose(slot);
      float girth = 0.3F + 0.7F * f;
      this.root.setPos((float)p[0], (float)p[1], (float)p[2]);
      this.root.setRotation((float)p[3], (float)p[4], (float)p[5]);
      this.root.xScale = girth;
      this.root.yScale = girth;
      this.root.zScale = (float)(p[6] / 6.0D) * Math.max(0.02F, f);
      this.root.visible = f > 0.02F;
   }

   public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
      this.root.render(pose, vc, light, overlay, r, g, b, a);
   }
}
