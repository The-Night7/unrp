package com.myriam.unrp.uo;



import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ColossusModel extends HierarchicalModel<Entity> {
   private static final float PI = (float)Math.PI;
   private final ModelPart root;
   public final ModelPart hips;
   public final ModelPart torso;
   public final ModelPart chest;
   public final ModelPart neck;
   public final ModelPart head;
   public final ModelPart jaw;
   public final ModelPart earR;
   public final ModelPart earL;
   public final ModelPart eyeR;
   public final ModelPart eyeL;
   public final ModelPart thighR;
   public final ModelPart shinR;
   public final ModelPart footR;
   public final ModelPart thighL;
   public final ModelPart shinL;
   public final ModelPart footL;
   public final ModelPart shoulderR;
   public final ModelPart upperArmR;
   public final ModelPart forearmR;
   public final ModelPart handR;
   public final ModelPart fingersR;
   public final ModelPart thumbR;
   public final ModelPart shoulderL;
   public final ModelPart upperArmL;
   public final ModelPart forearmL;
   public final ModelPart handL;
   public final ModelPart fingersL;
   public final ModelPart thumbL;
   public final ModelPart cape;
   public final ModelPart capeLow;
   public final ModelPart mantleR;
   public final ModelPart mantleL;
   public final ModelPart[] tail = new ModelPart[5];
   private final ModelPart[] driven;
   private final float[][] bind;
   private final float restLean;

   public ColossusModel(ModelPart root) {
      this.root = root;
      this.hips = root.getChild("hips");
      this.thighR = this.hips.getChild("thigh_r");
      this.shinR = this.thighR.getChild("shin_r");
      this.footR = this.shinR.getChild("foot_r");
      this.thighL = this.hips.getChild("thigh_l");
      this.shinL = this.thighL.getChild("shin_l");
      this.footL = this.shinL.getChild("foot_l");
      this.torso = this.hips.getChild("torso");
      this.chest = this.torso.getChild("chest");
      this.neck = this.chest.getChild("neck");
      this.head = this.neck.getChild("head");
      this.jaw = this.head.getChild("jaw");
      this.earR = this.head.getChild("ear_r");
      this.earL = this.head.getChild("ear_l");
      this.eyeR = this.head.getChild("eye_r");
      this.eyeL = this.head.getChild("eye_l");
      this.shoulderR = this.chest.getChild("shoulder_r");
      this.upperArmR = this.shoulderR.getChild("upper_arm_r");
      this.forearmR = this.upperArmR.getChild("forearm_r");
      this.handR = this.forearmR.getChild("hand_r");
      this.fingersR = this.handR.getChild("fingers_r");
      this.thumbR = this.handR.getChild("thumb_r");
      this.shoulderL = this.chest.getChild("shoulder_l");
      this.upperArmL = this.shoulderL.getChild("upper_arm_l");
      this.forearmL = this.upperArmL.getChild("forearm_l");
      this.handL = this.forearmL.getChild("hand_l");
      this.fingersL = this.handL.getChild("fingers_l");
      this.thumbL = this.handL.getChild("thumb_l");
      this.cape = this.chest.getChild("cape");
      this.capeLow = this.chest.getChild("cape_low");
      this.mantleR = this.chest.getChild("mantle_r");
      this.mantleL = this.chest.getChild("mantle_l");
      this.tail[0] = this.hips.getChild("tail1");

      for(int i = 1; i < 4; ++i) {
         this.tail[i] = this.tail[i - 1].getChild("tail" + (i + 1));
      }

      this.tail[4] = this.tail[3].getChild("tail_brush");
      this.driven = (ModelPart[])root.getAllParts().toArray((x$0) -> new ModelPart[x$0]);
      this.bind = new float[this.driven.length][];

      for(int i = 0; i < this.driven.length; ++i) {
         ModelPart p = this.driven[i];
         this.bind[i] = new float[]{p.xRot, p.yRot, p.zRot, p.x, p.y, p.z};
      }

      this.restLean = this.hips.xRot + this.torso.xRot + this.chest.xRot;
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch) {
   }

   public void rest() {
      for(int i = 0; i < this.driven.length; ++i) {
         ModelPart p = this.driven[i];
         float[] r = this.bind[i];
         p.xRot = r[0];
         p.yRot = r[1];
         p.zRot = r[2];
         p.x = r[3];
         p.y = r[4];
         p.z = r[5];
         p.xScale = 1.0F;
         p.yScale = 1.0F;
         p.zScale = 1.0F;
         p.visible = true;
         p.skipDraw = false;
      }

   }

   public void drive(ColossusState.Drive d) {
      this.stance(d);
      this.breathe(d);
      this.knuckleWalk(d);
      this.gallop(d);
      this.climbing(d);
      this.swimming(d);
      this.airborne(d);
      this.flying(d);
      this.crouching(d);
      this.chestBeat(d);
      this.smash(d);
      this.flinch(d);
      this.gaze(d);
      this.tailSway(d);
      this.furSway(d);
      this.faceWork(d);
   }

   private static float pos(float x) {
      return Math.max(0.0F, x);
   }

   private static float sm(float a, float b, float x) {
      float t = Mth.clamp((x - a) / (b - a), 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   private static float spike(float x) {
      float s = pos(Mth.sin(x));
      float s2 = s * s;
      return s2 * s2;
   }

   private static float twitch(float x) {
      float s = spike(x);
      float s16 = s * s * s * s;
      return s16 * s16;
   }

   private static float ground(ColossusState.Drive d) {
      return Mth.clamp(1.0F - Math.max(d.swim, Math.max(d.air, d.climb)), 0.0F, 1.0F);
   }

   private void stance(ColossusState.Drive d) {
      float g = ground(d);
      float ks = g * (1.0F - d.display);
      this.torso.xRot += 0.34F * ks;
      this.chest.xRot += 0.08F * ks;
      this.hips.y += 0.7F * ks;
      this.thighR.xRot += -0.2F * ks;
      this.thighL.xRot += -0.2F * ks;
      this.shinR.xRot += 0.32F * ks;
      this.shinL.xRot += 0.32F * ks;
      this.footR.xRot += -0.12F * ks;
      this.footL.xRot += -0.12F * ks;
      this.upperArmR.xRot += -0.55F * ks;
      this.upperArmL.xRot += -0.55F * ks;
      this.forearmR.xRot += 0.1F * ks;
      this.forearmL.xRot += 0.1F * ks;
      this.handR.xRot += -0.1F * ks;
      this.handL.xRot += -0.1F * ks;
      float shift = Mth.sin(d.time * 0.021F) * d.idle * g;
      this.hips.x += 0.7F * shift;
      this.hips.zRot += 0.025F * shift;
      this.torso.zRot += -0.035F * shift;
      this.shoulderR.y += 0.35F * shift;
      this.shoulderL.y += -0.35F * shift;
      float knead = Mth.sin(d.time * 0.047F) * d.idle;
      this.fingersR.xRot += 0.06F * knead;
      this.fingersL.xRot += -0.06F * knead;
   }

   private void breathe(ColossusState.Drive d) {
      float b = d.breath;
      this.chest.xScale = 1.0F + 0.016F * b;
      this.chest.zScale = 1.0F + 0.024F * b;
      this.torso.xRot += -0.014F * b;
      this.shoulderR.y += -0.3F * b;
      this.shoulderL.y += -0.3F * b;
      this.shoulderR.zRot += 0.012F * b;
      this.shoulderL.zRot += -0.012F * b;
   }

   private void knuckleWalk(ColossusState.Drive d) {
      float w = d.amount * ground(d) * (1.0F - d.run) * (1.0F - d.crouch * 0.4F);
      float p = d.walk;
      float s = Mth.sin(p);
      float c = Mth.cos(p);
      float liftR = pos(c);
      float liftL = pos(-c);
      float legLiftR = pos(-c);
      float legLiftL = pos(c);
      float armR = -0.32F * s;
      float armL = 0.32F * s;
      this.upperArmR.xRot += armR * w;
      this.upperArmL.xRot += armL * w;
      this.forearmR.xRot += -0.62F * liftR * w;
      this.forearmL.xRot += -0.62F * liftL * w;
      this.handR.xRot += (0.4F * liftR - armR * 0.55F) * w;
      this.handL.xRot += (0.4F * liftL - armL * 0.55F) * w;
      this.fingersR.xRot += 0.22F * liftR * w;
      this.fingersL.xRot += 0.22F * liftL * w;
      this.shoulderR.y += -0.9F * liftR * w;
      this.shoulderL.y += -0.9F * liftL * w;
      this.shoulderR.zRot += 0.06F * liftR * w;
      this.shoulderL.zRot += -0.06F * liftL * w;
      float legR = 0.38F * s;
      float legL = -0.38F * s;
      float kneeR = 0.62F * legLiftR;
      float kneeL = 0.62F * legLiftL;
      this.thighR.xRot += legR * w;
      this.thighL.xRot += legL * w;
      this.shinR.xRot += kneeR * w;
      this.shinL.xRot += kneeL * w;
      this.footR.xRot += -(legR + kneeR) * 0.85F * w;
      this.footL.xRot += -(legL + kneeL) * 0.85F * w;
      this.hips.y += 0.95F * (0.5F - Mth.abs(c)) * w;
      this.hips.yRot += 0.05F * s * w;
      this.torso.zRot += 0.06F * s * w;
      this.chest.yRot += -0.09F * s * w;
      this.chest.zRot += 0.03F * s * w;
      this.torso.xRot += 0.03F * Mth.abs(c) * w;
   }

   private void gallop(ColossusState.Drive d) {
      float r = d.amount * ground(d) * d.run;
      float q = d.gallop;
      float aR = -Mth.cos(q);
      float aL = -Mth.cos(q - 0.45F);
      float liftR = pos(-Mth.sin(q));
      float liftL = pos(-Mth.sin(q - 0.45F));
      float lR = -Mth.cos(q + (float)Math.PI + 0.25F);
      float lL = -Mth.cos(q + (float)Math.PI + 0.6F);
      float kR = pos(-Mth.sin(q + (float)Math.PI + 0.25F));
      float kL = pos(-Mth.sin(q + (float)Math.PI + 0.6F));
      this.torso.xRot += 0.1F * r - 0.11F * Mth.cos(q) * r;
      this.chest.xRot += 0.04F * r - 0.05F * Mth.cos(q + 0.5F) * r;
      this.hips.xRot += 0.07F * Mth.cos(q) * r;
      this.hips.y += (0.2F - 1.3F * Mth.sin(q - 0.3F)) * r;
      this.upperArmR.xRot += (-0.12F + 0.46F * aR) * r;
      this.upperArmL.xRot += (-0.12F + 0.46F * aL) * r;
      this.forearmR.xRot += -0.85F * liftR * r;
      this.forearmL.xRot += -0.85F * liftL * r;
      this.handR.xRot += (0.55F * liftR - 0.3F * aR) * r;
      this.handL.xRot += (0.55F * liftL - 0.3F * aL) * r;
      this.fingersR.xRot += 0.25F * liftR * r;
      this.fingersL.xRot += 0.25F * liftL * r;
      this.shoulderR.y += -1.0F * liftR * r;
      this.shoulderL.y += -1.0F * liftL * r;
      this.shoulderR.zRot += 0.08F * r;
      this.shoulderL.zRot += -0.08F * r;
      float legR = 0.46F * lR;
      float legL = 0.46F * lL;
      float kneeR = 0.85F * kR;
      float kneeL = 0.85F * kL;
      this.thighR.xRot += legR * r;
      this.thighL.xRot += legL * r;
      this.shinR.xRot += kneeR * r;
      this.shinL.xRot += kneeL * r;
      this.footR.xRot += -(legR + kneeR) * 0.8F * r;
      this.footL.xRot += -(legL + kneeL) * 0.8F * r;
      this.chest.yRot += -0.05F * Mth.sin(q) * r;
      this.torso.zRot += 0.035F * Mth.sin(q + 1.0F) * r;
   }

   private void climbing(ColossusState.Drive d) {
      float cl = d.climb;
      float k = d.climbing;
      float s = Mth.sin(k);
      this.torso.xRot += -0.46F * cl;
      this.chest.xRot += -0.06F * cl;
      this.hips.z += 1.5F * cl;
      this.upperArmR.xRot += (-2.35F + 0.5F * s) * cl;
      this.upperArmL.xRot += (-2.35F - 0.5F * s) * cl;
      this.forearmR.xRot += (-0.55F - 0.35F * pos(-s)) * cl;
      this.forearmL.xRot += (-0.55F - 0.35F * pos(s)) * cl;
      this.shoulderR.zRot += -0.12F * cl;
      this.shoulderL.zRot += 0.12F * cl;
      this.handR.xRot += -0.35F * cl;
      this.handL.xRot += -0.35F * cl;
      this.fingersR.xRot += 0.35F * cl;
      this.fingersL.xRot += 0.35F * cl;
      this.thighR.xRot += (-0.8F - 0.42F * s) * cl;
      this.thighL.xRot += (-0.8F + 0.42F * s) * cl;
      this.shinR.xRot += (0.95F + 0.35F * pos(s)) * cl;
      this.shinL.xRot += (0.95F + 0.35F * pos(-s)) * cl;
      this.footR.xRot += -0.2F * cl;
      this.footL.xRot += -0.2F * cl;
      this.head.xRot += -0.2F * cl;
      this.torso.zRot += 0.05F * s * cl;
   }

   private void swimming(ColossusState.Drive d) {
      float sw = d.swim * (1.0F - d.dive);
      float dv = d.dive;
      float t = d.time;
      float pr = Mth.sin(t * 0.2F);
      float pl = Mth.sin(t * 0.2F + (float)Math.PI);
      this.torso.xRot += -0.16F * sw;
      this.upperArmR.xRot += (-1.15F + 0.55F * pr) * sw;
      this.upperArmL.xRot += (-1.15F + 0.55F * pl) * sw;
      this.forearmR.xRot += (-0.55F + 0.3F * Mth.cos(t * 0.2F)) * sw;
      this.forearmL.xRot += (-0.55F + 0.3F * Mth.cos(t * 0.2F + (float)Math.PI)) * sw;
      this.shoulderR.zRot += (0.3F + 0.2F * pos(pr)) * sw;
      this.shoulderL.zRot += (-0.3F - 0.2F * pos(pl)) * sw;
      this.thighR.xRot += (-0.35F + 0.32F * Mth.sin(t * 0.22F)) * sw;
      this.thighL.xRot += (-0.35F + 0.32F * Mth.sin(t * 0.22F + (float)Math.PI)) * sw;
      this.shinR.xRot += (0.45F + 0.25F * pos(Mth.cos(t * 0.22F))) * sw;
      this.shinL.xRot += (0.45F + 0.25F * pos(Mth.cos(t * 0.22F + (float)Math.PI))) * sw;
      this.hips.xRot += 1.3F * dv;
      this.torso.xRot += -0.25F * dv;
      float sr = Mth.sin(t * 0.16F);
      float sl = Mth.sin(t * 0.16F + (float)Math.PI);
      this.upperArmR.xRot += (-2.1F + 1.0F * sr) * dv;
      this.upperArmL.xRot += (-2.1F + 1.0F * sl) * dv;
      this.forearmR.xRot += (-0.35F - 0.4F * pos(-sr)) * dv;
      this.forearmL.xRot += (-0.35F - 0.4F * pos(-sl)) * dv;
      this.shoulderR.zRot += 0.18F * dv;
      this.shoulderL.zRot += -0.18F * dv;
      this.thighR.xRot += (0.1F + 0.34F * Mth.sin(t * 0.34F)) * dv;
      this.thighL.xRot += (0.1F + 0.34F * Mth.sin(t * 0.34F + (float)Math.PI)) * dv;
      this.shinR.xRot += 0.3F * dv;
      this.shinL.xRot += 0.3F * dv;
      this.torso.zRot += 0.06F * Mth.sin(t * 0.08F) * (sw + dv);
   }

   private void airborne(ColossusState.Drive d) {
      float a = d.air * (1.0F - d.climb) * (1.0F - d.fly);
      float up = pos(d.rise) * a;
      float down = pos(-d.rise) * a;
      float hang = a * (1.0F - Mth.abs(d.rise));
      this.upperArmR.xRot += -1.55F * up - 0.55F * down - 0.9F * hang;
      this.upperArmL.xRot += -1.55F * up - 0.55F * down - 0.9F * hang;
      this.shoulderR.zRot += 0.15F * up + 0.55F * down + 0.35F * hang;
      this.shoulderL.zRot += -0.15F * up - 0.55F * down - 0.35F * hang;
      this.forearmR.xRot += -0.7F * up - 0.1F * down - 0.4F * hang;
      this.forearmL.xRot += -0.7F * up - 0.1F * down - 0.4F * hang;
      this.thighR.xRot += -0.7F * up - 0.2F * down - 0.45F * hang;
      this.thighL.xRot += -0.55F * up - 0.25F * down - 0.45F * hang;
      this.shinR.xRot += 0.95F * up + 0.2F * down + 0.6F * hang;
      this.shinL.xRot += 0.85F * up + 0.25F * down + 0.6F * hang;
      this.footR.xRot += -0.25F * up + 0.2F * down;
      this.footL.xRot += -0.25F * up + 0.2F * down;
      this.torso.xRot += -0.14F * up + 0.1F * down;
      this.hips.xRot += 0.06F * down;
      float land = d.land;
      this.hips.y += 2.5F * land;
      this.torso.xRot += 0.3F * land;
      this.thighR.xRot += -0.5F * land;
      this.thighL.xRot += -0.5F * land;
      this.shinR.xRot += 1.0F * land;
      this.shinL.xRot += 1.0F * land;
      this.footR.xRot += -1.04F * land;
      this.footL.xRot += -1.04F * land;
      this.upperArmR.xRot += -1.0F * land * (1.0F - d.slam);
      this.upperArmL.xRot += -1.0F * land * (1.0F - d.slam);
      this.shoulderR.zRot += 0.35F * land;
      this.shoulderL.zRot += -0.35F * land;
      this.head.xRot += 0.18F * land;
   }

   private void flying(ColossusState.Drive d) {
      float f = d.fly * (1.0F - d.climb);
      float up = pos(d.rise) * f;
      float down = pos(-d.rise) * f;
      float level = f * (1.0F - Mth.abs(d.rise));
      float hov = level * (1.0F - d.cruise);
      float fwd = level * d.cruise;
      float bob = Mth.sin(d.time * 0.08F);
      float sway = Mth.sin(d.time * 0.08F - 0.9F);
      float wade = Mth.sin(d.time * 0.11F);
      this.hips.y += 0.8F * bob * f;
      this.torso.xRot += -0.06F * level;
      this.chest.xRot += 0.12F * level;
      this.head.xRot += 0.12F * hov;
      this.shoulderR.y += -0.35F * level;
      this.shoulderL.y += -0.35F * level;
      this.shoulderR.zRot += 0.24F * level;
      this.shoulderL.zRot += -0.24F * level;
      this.upperArmR.xRot += (0.04F + 0.06F * sway) * hov;
      this.upperArmL.xRot += (0.04F + 0.06F * sway) * hov;
      this.forearmR.xRot += -0.22F * level;
      this.forearmL.xRot += -0.22F * level;
      this.fingersR.xRot += 0.55F * f + 0.2F * down;
      this.fingersL.xRot += 0.55F * f + 0.2F * down;
      this.thighR.xRot += -0.16F * hov;
      this.thighL.xRot += -0.04F * hov;
      this.shinR.xRot += 0.36F * hov;
      this.shinL.xRot += 0.18F * hov;
      this.footR.xRot += 0.4F * level;
      this.footL.xRot += 0.4F * level;
      this.tail[0].xRot += -0.45F * hov;
      this.hips.xRot += 0.3F * fwd;
      this.torso.xRot += 0.06F * fwd;
      this.upperArmR.xRot += (0.42F + 0.14F * wade) * fwd;
      this.upperArmL.xRot += (0.42F - 0.14F * wade) * fwd;
      this.thighR.xRot += (0.24F - 0.12F * wade) * fwd;
      this.thighL.xRot += (0.24F + 0.12F * wade) * fwd;
      this.shinR.xRot += 0.3F * fwd;
      this.shinL.xRot += 0.3F * fwd;
      this.tail[0].xRot += -0.25F * fwd;
      this.cape.xRot += 0.55F * fwd;
      this.capeLow.xRot += 0.45F * fwd;
      this.torso.xRot += -0.22F * up;
      this.chest.xRot += -0.1F * up;
      this.neck.xRot += -0.18F * up;
      this.head.xRot += -0.3F * up;
      this.jaw.xRot += 0.25F * up;
      this.upperArmR.xRot += 0.35F * up;
      this.upperArmL.xRot += 0.35F * up;
      this.shoulderR.zRot += 0.3F * up;
      this.shoulderL.zRot += -0.3F * up;
      this.forearmR.xRot += -0.1F * up;
      this.forearmL.xRot += -0.1F * up;
      this.fingersR.xRot += 0.55F * up;
      this.fingersL.xRot += 0.55F * up;
      this.shinR.xRot += 0.1F * up;
      this.shinL.xRot += 0.1F * up;
      this.footR.xRot += 0.5F * up;
      this.footL.xRot += 0.5F * up;
      this.tail[0].xRot += -0.55F * up;
      this.upperArmR.xRot += -2.75F * down;
      this.upperArmL.xRot += -2.75F * down;
      this.shoulderR.zRot += -0.32F * down;
      this.shoulderL.zRot += 0.32F * down;
      this.forearmR.xRot += -0.7F * down;
      this.forearmL.xRot += -0.7F * down;
      this.torso.xRot += -0.18F * down;
      this.thighR.xRot += -0.85F * down;
      this.thighL.xRot += -0.85F * down;
      this.shinR.xRot += 1.15F * down;
      this.shinL.xRot += 1.15F * down;
      this.jaw.xRot += 0.5F * down;
      float sl = d.slam;
      this.upperArmR.xRot += -0.2F * sl;
      this.upperArmL.xRot += -0.2F * sl;
      this.shoulderR.y += -0.6F * sl;
      this.shoulderL.y += -0.6F * sl;
      this.fingersR.xRot += 0.6F * sl;
      this.fingersL.xRot += 0.6F * sl;
      this.torso.xRot += 0.1F * sl;
      this.head.xRot += -0.12F * sl;
      this.jaw.xRot += 0.45F * sl;
   }

   private void crouching(ColossusState.Drive d) {
      float cr = d.crouch * ground(d);
      this.hips.y += 2.6F * cr;
      this.thighR.xRot += -0.59F * cr;
      this.thighL.xRot += -0.59F * cr;
      this.shinR.xRot += 0.68F * cr;
      this.shinL.xRot += 0.68F * cr;
      this.footR.xRot += -0.22F * cr;
      this.footL.xRot += -0.22F * cr;
      this.torso.xRot += 0.18F * cr;
      this.upperArmR.xRot += -0.73F * cr;
      this.upperArmL.xRot += -0.73F * cr;
      this.forearmR.xRot += -0.08F * cr;
      this.forearmL.xRot += -0.08F * cr;
      this.shoulderR.zRot += 0.08F * cr;
      this.shoulderL.zRot += -0.08F * cr;
   }

   private void chestBeat(ColossusState.Drive d) {
      float dsp = d.display;
      float b = d.beat;
      float hitR = spike(b);
      float hitL = spike(b + (float)Math.PI);
      this.torso.xRot += -0.4F * dsp;
      this.chest.xRot += -0.06F * dsp + 0.05F * (hitR + hitL) * dsp;
      this.chest.zRot += 0.035F * (hitL - hitR) * dsp;
      this.hips.xRot += -0.1F * dsp;
      this.thighR.xRot += 0.1F * dsp;
      this.thighL.xRot += 0.1F * dsp;
      this.hips.y += -0.8F * dsp;
      this.chest.xScale += 0.035F * dsp;
      this.chest.zScale += 0.05F * dsp;
      this.upperArmR.xRot += (0.05F - 0.48F * hitR) * dsp;
      this.upperArmL.xRot += (0.05F - 0.48F * hitL) * dsp;
      this.shoulderR.zRot += (0.93F - 0.25F * hitR) * dsp;
      this.shoulderL.zRot += (-0.93F + 0.25F * hitL) * dsp;
      this.upperArmR.yRot += (-0.45F - 0.4F * hitR) * dsp;
      this.upperArmL.yRot += (0.45F + 0.4F * hitL) * dsp;
      this.forearmR.xRot += (-1.88F + 0.06F * hitR) * dsp;
      this.forearmL.xRot += (-1.88F + 0.06F * hitL) * dsp;
      this.handR.xRot += 0.25F * dsp;
      this.handL.xRot += 0.25F * dsp;
      this.fingersR.xRot += 0.45F * dsp;
      this.fingersL.xRot += 0.45F * dsp;
      float roar = d.roar;
      this.jaw.xRot += 0.82F * roar;
      this.head.xRot += -0.42F * roar;
      this.neck.xRot += -0.16F * roar;
      this.shoulderR.y += -1.0F * roar;
      this.shoulderL.y += -1.0F * roar;
   }

   private void smash(ColossusState.Drive d) {
      float sR = d.strike * pos(d.side);
      float sL = d.strike * pos(-d.side);
      float liftR = sm(0.0F, 0.3F, sR) * (1.0F - sm(0.3F, 0.5F, sR));
      float slamR = sm(0.3F, 0.48F, sR) * (1.0F - sm(0.62F, 1.0F, sR));
      float liftL = sm(0.0F, 0.3F, sL) * (1.0F - sm(0.3F, 0.5F, sL));
      float slamL = sm(0.3F, 0.48F, sL) * (1.0F - sm(0.62F, 1.0F, sL));
      this.upperArmR.xRot += -2.4F * liftR - 0.95F * slamR;
      this.upperArmL.xRot += -2.4F * liftL - 0.95F * slamL;
      this.shoulderR.zRot += 0.25F * liftR - 0.1F * slamR;
      this.shoulderL.zRot += -0.25F * liftL + 0.1F * slamL;
      this.forearmR.xRot += -1.0F * liftR + 0.15F * slamR;
      this.forearmL.xRot += -1.0F * liftL + 0.15F * slamL;
      this.handR.xRot += 0.3F * liftR - 0.2F * slamR;
      this.handL.xRot += 0.3F * liftL - 0.2F * slamL;
      this.fingersR.xRot += 0.55F * (liftR + slamR);
      this.fingersL.xRot += 0.55F * (liftL + slamL);
      this.chest.yRot += 0.3F * (liftR - liftL) - 0.26F * (slamR - slamL);
      this.torso.xRot += -0.22F * (liftR + liftL) + 0.3F * (slamR + slamL);
      this.hips.y += 1.2F * (slamR + slamL);
      this.jaw.xRot += 0.3F * (liftR + liftL + slamR + slamL);
   }

   private void flinch(ColossusState.Drive d) {
      float h = d.hurt;
      this.torso.xRot += -0.18F * h;
      this.chest.zRot += 0.08F * h;
      this.head.xRot += -0.22F * h;
      this.head.yRot += 0.3F * h;
      this.jaw.xRot += 0.38F * h;
      this.shoulderR.zRot += 0.25F * h;
      this.shoulderL.zRot += -0.25F * h;
      this.forearmR.xRot += -0.3F * h;
      this.forearmL.xRot += -0.3F * h;
      float dead = d.death;
      this.jaw.xRot += 0.45F * dead;
      this.head.xRot += 0.35F * dead;
      this.shoulderR.zRot += 0.4F * dead;
      this.shoulderL.zRot += -0.4F * dead;
   }

   private void gaze(ColossusState.Drive d) {
      float lean = this.hips.xRot + this.torso.xRot + this.chest.xRot - this.restLean;
      float roll = this.hips.zRot + this.torso.zRot + this.chest.zRot;
      float twist = this.hips.yRot + this.torso.yRot + this.chest.yRot;
      float yaw = Mth.clamp(d.yaw, -1.2F, 1.2F);
      float pitch = Mth.clamp(d.pitch, -0.85F, 0.85F);
      this.neck.xRot += -0.45F * lean + 0.35F * pitch;
      this.head.xRot += -0.45F * lean + 0.65F * pitch;
      this.neck.yRot += -0.5F * twist + 0.35F * yaw;
      this.head.yRot += -0.4F * twist + 0.65F * yaw;
      this.head.zRot += -0.7F * roll - 0.1F * yaw;
      this.earR.zRot += -0.1F * yaw;
      this.earL.zRot += -0.1F * yaw;
   }

   private void tailSway(ColossusState.Drive d) {
      float w = d.amount * ground(d) * (1.0F - d.run);
      float r = d.amount * ground(d) * d.run;
      float wet = d.swim;
      float a = d.air;
      this.tail[0].xRot += -0.55F * r + 0.35F * pos(-d.rise) * a - 0.6F * d.climb - 0.25F * d.crouch - 0.2F * d.display - 1.6F * d.dive;
      float calm = (1.0F - r) * (1.0F - wet);

      for(int i = 0; i < this.tail.length; ++i) {
         float lag = (float)i * 0.55F;
         this.tail[i].yRot += 0.16F * Mth.sin(d.time * 0.05F - lag) * (1.0F - r) + 0.07F * Mth.sin(d.time * 0.14F - (float)i * 0.95F) * calm + 0.15F * Mth.sin(d.walk - lag) * w + 0.24F * Mth.sin(d.time * 0.3F - (float)i * 0.8F) * wet;
         this.tail[i].xRot += (0.045F + 0.035F * calm) * Mth.sin(d.time * 0.043F - (float)i * 0.5F) - 0.05F * calm * d.idle + 0.12F * Mth.sin(d.gallop - (float)i * 0.6F) * r + 0.1F * Mth.sin(d.time * 0.25F - (float)i * 0.7F) * a;
      }

   }

   private void furSway(ColossusState.Drive d) {
      float w = d.amount * ground(d) * (1.0F - d.run);
      float r = d.amount * ground(d) * d.run;
      float calm = (1.0F - r) * (1.0F - d.swim);
      float back = this.hips.xRot + this.torso.xRot + this.chest.xRot;
      float top = back + this.cape.xRot;
      float low = back + this.capeLow.xRot;
      float stream = 0.1F * w + 0.22F * r + 0.25F * d.swim + 0.3F * pos(-d.rise) * d.air;
      float flap = 0.035F * Mth.sin(d.time * 0.11F) * calm + 0.07F * Mth.sin(d.walk * 2.0F) * w + 0.12F * Mth.sin(d.gallop - 0.9F) * r;
      float trail = 0.035F * Mth.sin(d.time * 0.11F - 0.8F) * calm + 0.07F * Mth.sin(d.walk * 2.0F - 0.8F) * w + 0.12F * Mth.sin(d.gallop - 1.7F) * r;
      this.cape.xRot += Math.max(back + 0.05F, Math.min(1.57F, top + stream + flap)) - top;
      this.capeLow.xRot += Math.max(back + 0.04F, Math.min(1.5F, low + 0.8F * stream + trail)) - low;
      float lift = 0.03F * d.breath + 0.04F * Mth.sin(d.walk * 2.0F + 0.5F) * w + 0.07F * Mth.sin(d.gallop - 1.4F) * r + 0.08F * d.swim;
      this.mantleR.zRot += lift;
      this.mantleL.zRot -= lift;
   }

   private void faceWork(ColossusState.Drive d) {
      float lid = 1.0F - 0.88F * d.blink;
      this.eyeR.yScale = lid;
      this.eyeL.yScale = lid;
      this.jaw.xRot += 0.035F * (d.breath + 1.0F) + 0.14F * d.amount * d.run;
      float twR = twitch(d.time * 0.041F);
      float twL = twitch(d.time * 0.033F + 1.7F);
      float pin = 0.3F * d.run * d.amount + 0.45F * d.roar + 0.4F * d.hurt;
      this.earR.zRot += -0.28F * twR - pin;
      this.earL.zRot += 0.28F * twL + pin;
      this.earR.xRot += -0.2F * pin;
      this.earL.xRot += -0.2F * pin;
   }
}
