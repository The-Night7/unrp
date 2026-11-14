package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class LbHairModel {
   private final ModelPart head;
   private final ModelPart[] locks = new ModelPart[581];
   private final ModelPart[][] joints = new ModelPart[581][];
   private final float[][] bindX = new float[581][];
   private final float[][] bindZ = new float[581][];
   private final boolean[] grown = new boolean[581];

   public LbHairModel(ModelPart root) {
      this.head = root.getChild("head");

      for(int i = 0; i < 581; ++i) {
         ModelPart lock = this.head.getChild(LbHairGeometry.NAME[i]);
         ModelPart[] all = (ModelPart[])lock.getAllParts().toArray((x$0) -> new ModelPart[x$0]);
         ModelPart[] chain = (ModelPart[])Arrays.copyOfRange(all, 1, all.length);
         this.locks[i] = lock;
         this.joints[i] = chain;
         this.bindX[i] = new float[chain.length];
         this.bindZ[i] = new float[chain.length];

         for(int j = 0; j < chain.length; ++j) {
            this.bindX[i][j] = chain[j].xRot;
            this.bindZ[i][j] = chain[j].zRot;
         }
      }

   }

   public static LayerDefinition createLayer() {
      return LbHairGeometry.createLayer();
   }

   public void pose(ModelPart parentHead) {
      this.head.copyFrom(parentHead);
   }

   public void shape(LbHairState s) {
      float[] st = s.phys;

      for(int i = 0; i < 581; ++i) {
         float g = s.grow[i];
         ModelPart lock = this.locks[i];
         this.grown[i] = g > 0.01F;
         if (!this.grown[i]) {
            lock.visible = false;
         } else {
            int kind = LbHairGeometry.KIND[i];
            float thick = kind == 2 ? s.thickRisen : (kind == 3 ? s.thickMane : 1.0F);
            float swell = 1.0F + s.swell;
            lock.xScale = g * thick * swell;
            lock.yScale = g * swell;
            lock.zScale = g * thick * swell;
            ModelPart[] chain = this.joints[i];
            int b = i * 8;
            float outer = chain.length > 1 ? 1.0F / (float)(chain.length - 1) : 0.0F;

            for(int j = 0; j < chain.length; ++j) {
               float x = j == 0 ? st[b] : st[b + 4] * outer;
               float z = j == 0 ? st[b + 1] : st[b + 5] * outer;
               chain[j].xRot = this.bindX[i][j] + x;
               chain[j].zRot = this.bindZ[i][j] + z;
            }
         }
      }

   }

   public void pass(boolean glow) {
      for(int i = 0; i < 581; ++i) {
         boolean wisp = LbHairGeometry.KIND[i] == 5;
         this.locks[i].visible = this.grown[i] && wisp == glow;
      }

   }

   public boolean anyGlow() {
      for(int i = 0; i < 581; ++i) {
         if (this.grown[i] && LbHairGeometry.KIND[i] == 5) {
            return true;
         }
      }

      return false;
   }

   public void render(PoseStack pose, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
      this.head.render(pose, vc, light, overlay, r, g, b, a);
   }
}
