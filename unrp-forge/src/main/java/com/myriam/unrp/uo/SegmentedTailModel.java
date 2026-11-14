package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class SegmentedTailModel extends Model {
   public static final int SEGMENTS = 8;
   public static final float SEG_LEN = 6.0F;
   public static final float TIP_LEN = 4.6F;
   public static final float RADIUS = 2.45F;
   public static final float TIP_RADIUS = 2.15F;
   public final ModelPart base;
   public final ModelPart[] seg = new ModelPart[8];
   public final ModelPart tip;
   public final ModelPart[] claw = new ModelPart[3];
   public final ModelPart[] clawTip = new ModelPart[3];
   private final float[][] clawRest = new float[3][];
   private final float[] clawTipRest = new float[3];

   public SegmentedTailModel(ModelPart root) {
      super(RenderType::entityCutoutNoCull);
      this.base = root.getChild("base");
      ModelPart cur = this.base;

      for(int i = 0; i < 8; ++i) {
         cur = cur.getChild("seg" + i);
         this.seg[i] = cur;
      }

      this.tip = cur.getChild("tip");

      for(int i = 0; i < 3; ++i) {
         this.claw[i] = this.tip.getChild("claw" + i);
         this.clawTip[i] = this.claw[i].getChild("clawt" + i);
         ModelPart c = this.claw[i];
         this.clawRest[i] = new float[]{c.x, c.y, c.z, c.xRot, c.yRot, c.zRot};
         this.clawTipRest[i] = this.clawTip[i].xRot;
      }

   }

   public static LayerDefinition createLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition p0 = root.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 2.0F));
      PartDefinition p0_0 = p0.addOrReplaceChild("seg0", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F).texOffs(0, 33).addBox(-2.5F, -2.5F, -0.65F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-0.05F, -0.05F, 0.0F)).texOffs(52, 49).addBox(-0.5F, 1.925F, 2.05F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.05F, 0.075F, 0.15F)), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition p0_0_0 = p0_0.addOrReplaceChild("seg1", CubeListBuilder.create().texOffs(21, 0).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F).texOffs(15, 33).addBox(-2.5F, -2.5F, -0.65F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-0.05F, -0.05F, 0.0F)).texOffs(0, 56).addBox(-0.5F, 1.925F, 2.05F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.05F, 0.075F, 0.15F)), PartPose.offset(0.0F, 0.0F, 6.0F));
      PartDefinition p0_0_0_0 = p0_0_0.addOrReplaceChild("seg2", CubeListBuilder.create().texOffs(42, 0).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F).texOffs(30, 33).addBox(-2.5F, -2.5F, -0.65F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-0.05F, -0.05F, 0.0F)).texOffs(9, 56).addBox(-0.5F, 1.925F, 2.05F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.05F, 0.075F, 0.15F)), PartPose.offset(0.0F, 0.0F, 6.0F));
      PartDefinition p0_0_0_0_0 = p0_0_0_0.addOrReplaceChild("seg3", CubeListBuilder.create().texOffs(0, 11).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F).texOffs(45, 33).addBox(-2.5F, -2.5F, -0.65F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-0.05F, -0.05F, 0.0F)).texOffs(18, 56).addBox(-0.5F, 1.925F, 2.05F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.05F, 0.075F, 0.15F)), PartPose.offset(0.0F, 0.0F, 6.0F));
      PartDefinition p0_0_0_0_0_0 = p0_0_0_0_0.addOrReplaceChild("seg4", CubeListBuilder.create().texOffs(21, 11).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F).texOffs(0, 41).addBox(-2.5F, -2.5F, -0.65F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-0.05F, -0.05F, 0.0F)).texOffs(27, 56).addBox(-0.5F, 1.925F, 2.05F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.05F, 0.075F, 0.15F)), PartPose.offset(0.0F, 0.0F, 6.0F));
      PartDefinition p0_0_0_0_0_0_0 = p0_0_0_0_0_0.addOrReplaceChild("seg5", CubeListBuilder.create().texOffs(42, 11).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F).texOffs(15, 41).addBox(-2.5F, -2.5F, -0.65F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-0.05F, -0.05F, 0.0F)).texOffs(36, 56).addBox(-0.5F, 1.925F, 2.05F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.05F, 0.075F, 0.15F)), PartPose.offset(0.0F, 0.0F, 6.0F));
      PartDefinition p0_0_0_0_0_0_0_0 = p0_0_0_0_0_0_0.addOrReplaceChild("seg6", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F).texOffs(30, 41).addBox(-2.5F, -2.5F, -0.65F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-0.05F, -0.05F, 0.0F)).texOffs(45, 56).addBox(-0.5F, 1.925F, 2.05F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.05F, 0.075F, 0.15F)), PartPose.offset(0.0F, 0.0F, 6.0F));
      PartDefinition p0_0_0_0_0_0_0_0_0 = p0_0_0_0_0_0_0_0.addOrReplaceChild("seg7", CubeListBuilder.create().texOffs(21, 22).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 6.0F).texOffs(45, 41).addBox(-2.5F, -2.5F, -0.65F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-0.05F, -0.05F, 0.0F)).texOffs(54, 56).addBox(-0.5F, 1.925F, 2.05F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.05F, 0.075F, 0.15F)), PartPose.offset(0.0F, 0.0F, 6.0F));
      PartDefinition p0_0_0_0_0_0_0_0_0_0 = p0_0_0_0_0_0_0_0_0.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(42, 22).addBox(-1.5F, -1.5F, -0.2F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.1F, 0.1F, -0.2F)).texOffs(0, 49).addBox(-2.0F, -2.0F, -0.7F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.15F, 0.15F, -0.25F)), PartPose.offset(0.0F, 0.0F, 6.0F));
      PartDefinition p0_0_0_0_0_0_0_0_0_0_0 = p0_0_0_0_0_0_0_0_0_0.addOrReplaceChild("claw0", CubeListBuilder.create().texOffs(13, 49).addBox(-1.0F, -1.0F, -0.2F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.1F, -0.1F, -0.2F)), PartPose.offsetAndRotation(0.0F, -0.9F, 3.6F, 0.2F, 0.0F, 0.0F));
      p0_0_0_0_0_0_0_0_0_0_0.addOrReplaceChild("clawt0", CubeListBuilder.create().texOffs(0, 61).addBox(-0.5F, -0.5F, 0.2F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.05F, 0.05F, 0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.3F, -0.45F, 0.0F, 0.0F));
      PartDefinition p0_0_0_0_0_0_0_0_0_0_1 = p0_0_0_0_0_0_0_0_0_0.addOrReplaceChild("claw1", CubeListBuilder.create().texOffs(26, 49).addBox(-1.0F, -1.0F, -0.2F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.1F, -0.1F, -0.2F)), PartPose.offsetAndRotation(-0.9F, 0.7F, 3.6F, -0.1F, -0.26F, 0.0F));
      p0_0_0_0_0_0_0_0_0_0_1.addOrReplaceChild("clawt1", CubeListBuilder.create().texOffs(7, 61).addBox(-0.5F, -0.5F, 0.2F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.05F, 0.05F, 0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.3F, 0.4F, 0.0F, 0.0F));
      PartDefinition p0_0_0_0_0_0_0_0_0_0_2 = p0_0_0_0_0_0_0_0_0_0.addOrReplaceChild("claw2", CubeListBuilder.create().texOffs(39, 49).addBox(-1.0F, -1.0F, -0.2F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.1F, -0.1F, -0.2F)), PartPose.offsetAndRotation(0.9F, 0.7F, 3.6F, -0.1F, 0.26F, 0.0F));
      p0_0_0_0_0_0_0_0_0_0_2.addOrReplaceChild("clawt2", CubeListBuilder.create().texOffs(14, 61).addBox(-0.5F, -0.5F, 0.2F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.05F, 0.05F, 0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.3F, 0.4F, 0.0F, 0.0F));
      return LayerDefinition.create(mesh, 64, 64);
   }

   public void reset() {
      this.base.setPos(0.0F, 9.0F, 2.0F);
      this.base.setRotation(0.0F, 0.0F, 0.0F);

      for(int i = 0; i < 8; ++i) {
         ModelPart p = this.seg[i];
         p.setPos(0.0F, 0.0F, i == 0 ? 0.0F : 6.0F);
         p.setRotation(0.0F, 0.0F, 0.0F);
         p.visible = true;
         p.xScale = 1.0F;
         p.yScale = 1.0F;
         p.zScale = 1.0F;
      }

      this.tip.setPos(0.0F, 0.0F, 6.0F);
      this.tip.setRotation(0.0F, 0.0F, 0.0F);
      this.tip.visible = true;
      this.tip.xScale = 1.0F;
      this.tip.yScale = 1.0F;
      this.tip.zScale = 1.0F;
      this.pinch(0.0F, 0.0F);
   }

   public void shape(float taper, float growth) {
      float span = growth * 8.0F;

      for(int i = 0; i < 8; ++i) {
         float f = Mth.clamp(span - (float)i, 0.0F, 1.0F);
         this.seg[i].visible = f > 0.02F;
         float girth = taper * (f < 1.0F ? 0.3F + 0.7F * f : 1.0F);
         this.seg[i].xScale = girth;
         this.seg[i].yScale = girth;
         this.seg[i].zScale = f < 1.0F ? Math.max(0.02F, f) : 1.0F;
      }

      this.tip.visible = span >= 8.0F;
   }

   public Vector3f frontier(float growth) {
      PoseStack ps = new PoseStack();
      this.base.translateAndRotate(ps);
      int n = Mth.clamp((int)(growth * 8.0F), 0, 7);

      for(int i = 0; i <= n; ++i) {
         this.seg[i].translateAndRotate(ps);
      }

      Vector3f v = new Vector3f(0.0F, 0.0F, 0.375F);
      v.mulPosition(ps.last().pose());
      return v;
   }

   public void pinch(float k, float roll) {
      float close = Math.max(0.0F, k);
      float open = Math.max(0.0F, -k);
      float cr = Mth.cos(roll);
      float sr = Mth.sin(roll);

      for(int q = 0; q < 3; ++q) {
         float[] r = this.clawRest[q];
         float x = r[3] + (q == 0 ? open * 0.75F - close * 0.62F : -open * 0.7F + close * 0.5F);
         this.claw[q].setPos(r[0] * cr - r[1] * sr, r[0] * sr + r[1] * cr, r[2]);
         this.claw[q].setRotation(x, r[4], roll);
         this.clawTip[q].xRot = this.clawTipRest[q] * (1.0F + close * 0.9F - open * 0.45F);
      }

   }

   public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
      this.base.render(pose, vc, light, overlay, r, g, b, a);
   }
}
