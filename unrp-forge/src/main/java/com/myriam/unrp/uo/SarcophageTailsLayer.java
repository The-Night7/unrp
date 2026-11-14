package com.myriam.unrp.uo;

import com.myriam.unrp.pass.UnrpPowerLayers;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SarcophageTailsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   public static final ResourceLocation TAIL_TEX = new ResourceLocation("unrp", "textures/models/sarcophage_tail.png");
   public static final ResourceLocation TAIL_GLOW = new ResourceLocation("unrp", "textures/models/sarcophage_tail_glow.png");
   private static final ResourceLocation TEMPEST_TEX = new ResourceLocation("unrp", "textures/models/sarcophage_tempest.png");
   private static final ResourceLocation TEMPEST_GLOW = new ResourceLocation("unrp", "textures/models/sarcophage_tempest_glow.png");
   private static final ResourceLocation PROPS_TEX = new ResourceLocation("unrp", "textures/models/sarcophage_props.png");
   private static final ResourceLocation SHROUD_TEX = new ResourceLocation("unrp", "textures/models/sarcophage_shroud.png");
   public static final int FULL_BRIGHT = 15728880;
   public static final float[] GLOW = new float[]{0.0F, 0.35F, 0.4F, 0.45F, 0.55F, 0.85F};
   public static final double[] REACH_DEFAULT = new double[]{-5.0D, 4.0D, -30.0D};
   public static final int GRAB_SLOT = 3;
   public static final int RELEASE_HOLD = 12;
   private static final int MOST_HELD = 64;
   private static final Map<Integer, double[]> HELD_AT = new HashMap();
   private final int[] lastSpawn = new int[6];
   private final SegmentedTailModel tail;
   private final SarcophageTempestModel tempestModel;
   private final SandPropModel props;
   private final SandRootModel root;

   public SarcophageTailsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet modelSet) {
      super(parent);
      this.tail = new SegmentedTailModel(modelSet.bakeLayer(OriginModels.SEGMENTED_TAIL));
      this.tempestModel = new SarcophageTempestModel(modelSet.bakeLayer(OriginModels.SARC_TEMPEST));
      this.props = new SandPropModel(modelSet.bakeLayer(OriginModels.SAND_PROPS));
      this.root = new SandRootModel(modelSet.bakeLayer(OriginModels.SAND_ROOT));
   }

   public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
      if (!player.isInvisible()) {
         int variant = UnrpPowerLayers.powerVariant(player, "sarcophage");
         boolean live = variant != 0;
         int tails = tailCount(player, variant);
         boolean tempest = live && UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "tempest");
         boolean shroud = live && UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "shroud");
         boolean surf = live && UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "surf");
         boolean grabbing = live && UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "grabbing");
         float t = (float)player.tickCount + partialTick;
         SarcophageTailState.State drive = SarcophageTailState.sample(player, t, tails, grabbing);
         if (live || drive.showing()) {
            PlayerModel<AbstractClientPlayer> body = (PlayerModel)this.getParentModel();
            SarcophageTailPose.Motion m = SarcophageTailPose.motion(tempest, (double)Mth.clamp(limbSwingAmount, 0.0F, 1.0F), (double)limbSwing, drive.whip(player, partialTick), 1.0D);
            m.legR = (double)body.rightLeg.xRot;
            m.legL = (double)body.leftLeg.xRot;
            m.armR = (double)body.rightArm.xRot;
            m.armL = (double)body.leftArm.xRot;
            m.headPitch = (double)(body.head.xRot - body.body.xRot);
            m.tense = (double)drive.grow[5];
            float glow = GLOW[Mth.clamp(tempest ? 5 : tails, 0, 5)];
            if (shroud) {
               body.renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucent(SHROUD_TEX)), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.85F);
            }

            float grab = drive.grow[5];
            if (grab <= 0.02F) {
               HELD_AT.remove(player.getId());
            }

            pose.pushPose();

            try {
               body.body.translateAndRotate(pose);
               boolean sprouting = false;
               VertexConsumer tailVc = buffers.getBuffer(RenderType.entityCutoutNoCull(TAIL_TEX));

               for(int i = 0; i < 5; ++i) {
                  float plug_0 = rootGrow(drive, i);
                  if (plug_0 > 0.02F) {
                     this.root.place(i, plug_0);
                     this.root.renderToBuffer(pose, tailVc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                  }

                  float grow_0 = tailGrow(drive, i);
                  if (!(grow_0 <= 0.02F)) {
                     SarcophageTailPose.pose(this.tail, i, (double)t, m);
                     this.tail.shape(1.0F, grow_0);
                     this.tail.renderToBuffer(pose, tailVc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                     if (drive.spawning[i]) {
                        sprouting = true;
                        this.sand(player, partialTick, grow_0, i);
                     }
                  }
               }

               if (grab > 0.02F) {
                  this.poseGrabTendril(pose, tailVc, light, 1.0F, player, partialTick, grab, 0);
               }

               VertexConsumer glowVc = buffers.getBuffer(RenderType.eyes(TAIL_GLOW));

               for(int i = 0; i < 5; ++i) {
                  float plug = rootGrow(drive, i);
                  if (plug > 0.02F) {
                     this.root.place(i, plug);
                     this.root.renderToBuffer(pose, glowVc, 15728880, OverlayTexture.NO_OVERLAY, glow, glow, glow, 1.0F);
                  }

                  float grow = tailGrow(drive, i);
                  if (!(grow <= 0.02F)) {
                     SarcophageTailPose.pose(this.tail, i, (double)t, m);
                     this.tail.shape(1.0F, grow);
                     this.tail.renderToBuffer(pose, glowVc, 15728880, OverlayTexture.NO_OVERLAY, glow, glow, glow, 1.0F);
                  }
               }

               if (grab > 0.02F) {
                  this.poseGrabTendril(pose, glowVc, 15728880, glow, player, partialTick, grab, 0);
               }

               if (sprouting) {
                  drive.lastTick = player.tickCount;
               }

               if (shroud) {
                  this.renderShroudOrbit(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(PROPS_TEX)), t, light);
               }
            } finally {
               pose.popPose();
            }

            if (surf || tempest) {
               VertexConsumer propVc = buffers.getBuffer(RenderType.entityCutoutNoCull(PROPS_TEX));
               if (surf) {
                  this.renderWave(pose, propVc, t, light);
               }

               if (tempest) {
                  this.renderGroundRing(pose, propVc, t, light);
               }
            }

            if (tempest) {
               body.copyPropertiesTo(this.tempestModel);
               this.tempestModel.renderToBuffer(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEMPEST_TEX)), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
               this.tempestModel.renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(TEMPEST_GLOW)), 15728880, OverlayTexture.NO_OVERLAY, glow, glow, glow, 1.0F);
            }

         }
      }
   }

   public static int tailCount(AbstractClientPlayer player, int variant) {
      if (variant == 0) {
         return 0;
      } else {
         int tails = 1;
         if (UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "tail_2")) {
            tails = 2;
         }

         if (UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "tail_3")) {
            tails = 3;
         }

         if (UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "tail_4")) {
            tails = 4;
         }

         if (UnrpPowerLayers.abilityOn(player, "sarcophage", variant, "tempest")) {
            tails = Math.min(5, tails + 1);
         }

         return tails;
      }
   }

   public static double[] bodyPose(ModelPart body) {
      return new double[]{(double)body.x, (double)body.y, (double)body.z, (double)body.xRot, (double)body.yRot, (double)body.zRot};
   }

   public static double[] toBody(AbstractClientPlayer player, float partial, Vec3 world, double[] body) {
      double scale = Math.max(0.25D, (double)player.getBbWidth() / 0.6D);
      double k = 0.9375D * scale;
      double px = Mth.lerp((double)partial, player.xo, player.getX());
      double py = Mth.lerp((double)partial, player.yo, player.getY()) - (player.isCrouching() ? 0.125D : 0.0D);
      double pz = Mth.lerp((double)partial, player.zo, player.getZ());
      double dx = (world.x - px) / k;
      double dy = (world.y - py) / k;
      double dz = (world.z - pz) / k;
      double yaw = Math.toRadians(180.0D - (double)Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot));
      double c = Math.cos(yaw);
      double s = Math.sin(yaw);
      double lx = dx * c - dz * s;
      double lz = dx * s + dz * c;
      double[] m = new double[]{-lx * 16.0D, (1.501D - dy) * 16.0D, lz * 16.0D};
      double[] inv = SarcMath.invert(SarcMath.compose(body[0], body[1], body[2], body[3], body[4], body[5], 1.0D, 1.0D, 1.0D));
      return SarcMath.apply(inv, m);
   }

   public static Vec3 toWorld(AbstractClientPlayer player, float partial, double[] bodyPx, double[] body) {
      double[] m = SarcMath.apply(SarcMath.compose(body[0], body[1], body[2], body[3], body[4], body[5], 1.0D, 1.0D, 1.0D), bodyPx);
      double scale = Math.max(0.25D, (double)player.getBbWidth() / 0.6D);
      double k = 0.9375D * scale;
      double lx = -m[0] / 16.0D;
      double ly = 1.501D - m[1] / 16.0D;
      double lz = m[2] / 16.0D;
      double yaw = Math.toRadians(180.0D - (double)Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot));
      double c = Math.cos(yaw);
      double s = Math.sin(yaw);
      double wx = lx * c + lz * s;
      double wz = -lx * s + lz * c;
      return new Vec3(Mth.lerp((double)partial, player.xo, player.getX()) + wx * k, Mth.lerp((double)partial, player.yo, player.getY()) + ly * k - (player.isCrouching() ? 0.125D : 0.0D), Mth.lerp((double)partial, player.zo, player.getZ()) + wz * k);
   }

   static float rootGrow(SarcophageTailState.State drive, int i) {
      return i == 3 ? Math.max(drive.grow[i], drive.grow[5]) : drive.grow[i];
   }

   static float tailGrow(SarcophageTailState.State drive, int i) {
      float grow = drive.grow[i];
      if (i == 3) {
         grow *= 1.0F - drive.grow[5];
      }

      return grow;
   }

   private void poseGrabTendril(PoseStack pose, VertexConsumer vc, int light, float shade, AbstractClientPlayer player, float partial, float grow, int skip) {
      PlayerModel<AbstractClientPlayer> body = (PlayerModel)this.getParentModel();
      drawGrab(this.tail, pose, vc, light, shade, player, partial, grow, skip, bodyPose(body.body), (double)body.rightLeg.xRot, (double)body.rightArm.xRot);
   }

   public static void drawGrab(SegmentedTailModel tail, PoseStack pose, VertexConsumer vc, int light, float shade, AbstractClientPlayer player, float partial, float grow, int skip, double[] bodyPart, double legR, double armR) {
      long now = player.level().getGameTime();
      int id = ClientSarcophage.heldBy(player.getId(), now);
      Entity held = id >= 0 ? player.level().getEntity(id) : null;
      double[] last = (double[])HELD_AT.get(player.getId());
      double[] target;
      double wrap;
      if (held != null) {
         Vec3 c = new Vec3(Mth.lerp((double)partial, held.xo, held.getX()), Mth.lerp((double)partial, held.yo, held.getY()) + (double)held.getBbHeight() * 0.5D, Mth.lerp((double)partial, held.zo, held.getZ()));
         target = toBody(player, partial, c, bodyPart);
         double scale = Math.max(0.25D, (double)player.getBbWidth() / 0.6D);
         wrap = (double)held.getBbWidth() * 0.5D * 16.0D / (0.9375D * scale) + 2.6D;
         if (HELD_AT.size() >= 64) {
            HELD_AT.clear();
         }

         HELD_AT.put(player.getId(), new double[]{target[0], target[1], target[2], wrap, (double)now});
      } else if (last != null && now - (long)last[4] <= 12L && now >= (long)last[4]) {
         target = new double[]{last[0], last[1], last[2]};
         wrap = last[3];
      } else {
         target = (double[])REACH_DEFAULT.clone();
         wrap = 6.0D;
      }

      SarcophagePaths.Chain ch = SarcophagePaths.hold(target, wrap, legR, armR);
      float t = (float)player.tickCount + partial;
      float squeeze = 0.9F + 0.1F * Mth.sin(t * 0.45F);
      float roll = (float)SarcophagePaths.rollToward(ch, target);
      tail.reset();
      tail.pinch(0.8F * squeeze, roll);
      float span = grow * (float)ch.count;

      for(int s = skip; s < ch.count; ++s) {
         float f = Mth.clamp(span - (float)s, 0.0F, 1.0F);
         if (f <= 0.02F) {
            break;
         }

         boolean tip = s == ch.count - 1;
         ModelPart part = tip ? tail.tip : tail.seg[s % 8];
         ModelPart child = tip ? null : (s % 8 == 7 ? tail.tip : tail.seg[s % 8 + 1]);
         float base = tip ? 4.6F : 6.0F;
         float girth = f < 1.0F ? 0.3F + 0.7F * f : 1.0F;
         double[] q = ch.turn[s];
         double[] o = ch.origin[s];
         pose.pushPose();

         try {
            pose.translate(o[0] / 16.0D, o[1] / 16.0D, o[2] / 16.0D);
            pose.mulPose(new Quaternionf((float)q[0], (float)q[1], (float)q[2], (float)q[3]));
            part.setPos(0.0F, 0.0F, 0.0F);
            part.setRotation(0.0F, 0.0F, 0.0F);
            part.xScale = girth;
            part.yScale = girth;
            part.zScale = (float)(ch.length[s] / (double)base) * (f < 1.0F ? Math.max(0.02F, f) : 1.0F);
            part.visible = true;
            if (child != null) {
               child.visible = false;
            }

            part.render(pose, vc, light, OverlayTexture.NO_OVERLAY, shade, shade, shade, 1.0F);
         } finally {
            if (child != null) {
               child.visible = true;
            }

            pose.popPose();
         }
      }

      tail.reset();
   }

   private void sand(AbstractClientPlayer player, float partial, float grow, int index) {
      if (player.tickCount != this.lastSpawn[index]) {
         this.lastSpawn[index] = player.tickCount;
         Vector3f tip = this.tail.frontier(grow);
         double[] bodyPx = new double[]{(double)tip.x() * 16.0D, (double)tip.y() * 16.0D, (double)tip.z() * 16.0D};
         Vec3 w = toWorld(player, partial, bodyPx, bodyPose(((PlayerModel)this.getParentModel()).body));
         BlockParticleOption grain = new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState());

         for(int k = 0; k < 3; ++k) {
            double jx = (player.getRandom().nextDouble() - 0.5D) * 0.55D;
            double jy = (player.getRandom().nextDouble() - 0.5D) * 0.55D;
            double jz = (player.getRandom().nextDouble() - 0.5D) * 0.55D;
            player.level().addParticle(grain, w.x + jx, w.y + jy, w.z + jz, -jx * 0.35D, -jy * 0.35D + 0.02D, -jz * 0.35D);
         }

      }
   }

   private void renderShroudOrbit(PoseStack pose, VertexConsumer vc, float t, int light) {
      int count = 12;

      for(int k = 0; k < count; ++k) {
         float ang = (float)((Math.PI * 2D) * (double)k / (double)count) + t * 0.045F;
         float y = 6.0F + 6.5F * Mth.sin(t * 0.05F + (float)k * 1.3F);
         float rad = 9.0F + 1.2F * Mth.sin(t * 0.07F + (float)k);
         ModelPart part = k % 3 == 0 ? this.props.chunk : this.props.shard;
         this.props.place(part, Mth.cos(ang) * rad, y, Mth.sin(ang) * rad, t * 0.06F + (float)k, ang, t * 0.04F + (float)k * 0.5F);
         part.render(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }

   }

   private void renderWave(PoseStack pose, VertexConsumer vc, float t, int light) {
      int arcs = 9;

      for(int i = 0; i < arcs; ++i) {
         float f = ((float)i - (float)(arcs - 1) / 2.0F) / (float)(arcs - 1);
         float roll = Mth.sin(t * 0.35F + (float)i * 0.9F) * 1.2F;
         float rad = 12.0F + 3.0F * Math.abs(f) + roll;
         float ang_0 = f * 1.9F;
         this.props.place(this.props.crest, Mth.sin(ang_0) * rad, 25.0F - roll * 0.4F, -Mth.cos(ang_0) * rad, -0.55F - Math.abs(f) * 0.35F, ang_0, f * 0.5F);
         this.props.crest.render(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }

      for(int i = 0; i < 4; ++i) {
         float ang = (float)(Math.PI * (double)(0.35F + (float)i * 0.1F)) + t * 0.02F;
         this.props.place(this.props.slab, Mth.cos(ang) * 11.0F, 24.5F, Mth.sin(ang) * 11.0F, 0.0F, ang, 0.0F);
         this.props.slab.render(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }

   }

   private void renderGroundRing(PoseStack pose, VertexConsumer vc, float t, int light) {
      int count = 10;

      for(int i = 0; i < count; ++i) {
         float ang = (float)((Math.PI * 2D) * (double)i / (double)count) + t * 0.04F;
         float rise = 1.6F * Mth.sin(t * 0.13F + (float)i * 1.1F);
         this.props.place(this.props.slab, Mth.cos(ang) * 13.0F, 22.5F - rise, Mth.sin(ang) * 13.0F, 0.12F * Mth.sin(ang), ang, 0.12F * Mth.cos(ang));
         this.props.slab.render(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      }

   }
}
