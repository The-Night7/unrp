package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Fx;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class ClientSaltSeraph {
   static final double SEEN = 72.0D;
   static final double MARK_SEEN = 32.0D;
   static final int MOST_FX = 512;
   static final int MOST_MARKS = 1024;
   static final int CRUMBLE_HIDE = 60;
   static final double SPEAR_AHEAD = 0.4D;
   static final double END_FADE = 6.0D;
   static final double DECAL_LIFT = 1.0D;
   static final double DECAL_REACH = 4.0D;
   static final double TREE_FULL = 4.499999999999999D;
   static final int[] PASSES = new int[]{2, 0, 1};
   private static final List<ClientSaltSeraph.Fx> FX = new ArrayList();
   private static final Map<Integer, ClientSaltSeraph.Mark> MARKS = new HashMap();
   private static final Map<Integer, Double> CRUMBLED = new HashMap();
   private static boolean inWorld;
   private static final float[] METER_UP = new float[]{0.0F, 1.0F, 0.0F};

   public static void clearAll() {
      FX.clear();
      MARKS.clear();
      CRUMBLED.clear();
      SaltHudState.clear();
   }

   private ClientSaltSeraph() {
   }

   static float f(double v) {
      return (float)v;
   }

   static double[] a(Vec3 v) {
      return new double[]{v.x, v.y, v.z};
   }

   static double[] at(Entity e, float pt) {
      return a(e.getPosition(pt));
   }

   static int life(int kind, int value) {
      switch (kind) {
         case 1:
            return 26;
         case 2:
         case 9:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         default:
            return Math.max(1, value);
         case 3:
            return 16;
         case 4:
         case 14:
         case 15:
            return 20;
         case 5:
            return 14;
         case 6:
            return 22;
         case 7:
            return 24;
         case 8:
            return 30;
         case 10:
            return 44;
         case 11:
            return 40;
         case 12:
            return 26;
         case 13:
            return 12;
         case 26:
            return 60;
         case 27:
            return 34;
         case 28:
            return 16;
         case 29:
            return 8;
         case 30:
            return 26;
         case 31:
            return 50;
         case 32:
            return SaltMath.rendLife();
      }
   }

   public static void accept(SaltFxPacket msg) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc.level;
      if (level != null) {
         double t = (double)level.getGameTime();
         int kind = msg.kind;
         if (kind == 24) {
            for(ClientSaltSeraph.Fx fx_0 : FX) {
               if (fx_0.caster == msg.caster && fx_0.kind == msg.value && Double.isNaN(fx_0.ended)) {
                  fx_0.ended = t;
               }
            }

            if (msg.value == 25 && (msg.flags & 4) != 0 && mine(mc, msg.caster)) {
               title(mc, "CONTROL RETURNS", 15787738, "5 minutes gone", 13156544);
            }

         } else {
            if (kind == 25) {
               for(ClientSaltSeraph.Fx fx_1 : FX) {
                  if (fx_1.caster == msg.caster && fx_1.kind == 25 && Double.isNaN(fx_1.ended)) {
                     fx_1.born = t - (double)msg.age;
                     fx_1.value = msg.value;
                     return;
                  }
               }
            }

            if (kind == 27 && mine(mc, msg.caster)) {
               title(mc, "UNBOUND", 16730714, "the instinct takes the body", 15784152);
            }

            double[] p1 = new double[]{(double)msg.x, (double)msg.y, (double)msg.z};
            double[] p2 = new double[]{(double)msg.x2, (double)msg.y2, (double)msg.z2};
            if (kind != 2 && kind != 11) {
               if (kind == 20) {
                  for(ClientSaltSeraph.Fx fx_2 : FX) {
                     if (fx_2.caster == msg.caster && fx_2.kind == 20 && Double.isNaN(fx_2.ended)) {
                        fx_2.born = t - (double)msg.age;
                        fx_2.value = msg.value;
                        return;
                     }
                  }
               }

               if (kind == 8 && msg.target != 0) {
                  CRUMBLED.put(msg.target, t);
               }

               add(msg.caster, kind, msg.slot, msg.target, p1, p2, t - (double)msg.age, msg.value, msg.flags);
            } else {
               boolean great = kind == 11;
               int flight = great ? 10 : 1;

               for(ClientSaltSeraph.Fx fx : FX) {
                  if (fx.caster == msg.caster && fx.kind == flight && fx.slot == msg.slot && Double.isNaN(fx.ended)) {
                     fx.ended = t;
                  }
               }

               if (great && (msg.flags & 1) != 0) {
                  add(msg.caster, 11, msg.slot, 0, p1, p2, t, 40, 0);
               } else {
                  add(msg.caster, 4, 0, 0, p1, p2, t, great ? 20 : 10, 0);
               }

            }
         }
      }
   }

   static boolean mine(Minecraft mc, int caster) {
      return mc.player != null && mc.player.getId() == caster;
   }

   static void title(Minecraft mc, String head, int headRgb, String sub, int subRgb) {
      mc.gui.setTimes(4, 40, 10);
      mc.gui.setSubtitle(Component.literal(sub).withStyle((st) -> st.withColor(subRgb)));
      mc.gui.setTitle(Component.literal(head).withStyle((st) -> st.withColor(headRgb)));
   }

   static ClientSaltSeraph.Fx unboundOf(int id) {
      for(ClientSaltSeraph.Fx fx : FX) {
         if (fx.caster == id && fx.kind == 25) {
            return fx;
         }
      }

      return null;
   }

   public static double veins(int id, double t) {
      ClientSaltSeraph.Fx fx = unboundOf(id);
      if (fx == null) {
         return 0.0D;
      } else {
         int beast = (int)Math.floor(t - fx.born - 60.0D);
         if (Double.isNaN(fx.ended)) {
            return SaltMath.veins(beast);
         } else {
            double at = SaltMath.veins((int)Math.floor(fx.ended - fx.born - 60.0D));
            return SaltMath.veinsAfter(at, (int)Math.floor(t - fx.ended));
         }
      }
   }

   public static boolean spear(int id, double t) {
      ClientSaltSeraph.Fx fx = unboundOf(id);
      return fx != null && Double.isNaN(fx.ended) && t - fx.born >= 60.0D;
   }

   private static void add(int caster, int kind, int slot, int target, double[] p1, double[] p2, double born, int value, int flags) {
      if (FX.size() >= 512) {
         FX.remove(0);
      }

      ClientSaltSeraph.Fx fx = new ClientSaltSeraph.Fx();
      fx.caster = caster;
      fx.kind = kind;
      fx.slot = slot;
      fx.target = target;
      fx.p1 = p1;
      fx.p2 = p2;
      fx.born = born;
      fx.value = value;
      fx.flags = flags;
      FX.add(fx);
   }

   public static void mark(int entity, int salt, int flags) {
      if (salt <= 0 && flags == 0) {
         MARKS.remove(entity);
      } else {
         ClientSaltSeraph.Mark m = (ClientSaltSeraph.Mark)MARKS.get(entity);
         if (m == null) {
            if (MARKS.size() >= 1024) {
               return;
            }

            m = new ClientSaltSeraph.Mark();
            MARKS.put(entity, m);
         }

         m.salt = salt;
         m.flags = flags;
      }
   }

   private static void prune(ClientLevel level, double t) {
      Iterator<ClientSaltSeraph.Fx> it = FX.iterator();

      while(it.hasNext()) {
         ClientSaltSeraph.Fx fx = (ClientSaltSeraph.Fx)it.next();
         boolean flight = fx.kind == 1 || fx.kind == 10;
         boolean gone = fx.kind == 25 && !Double.isNaN(fx.ended) && t - fx.ended > 40.0D;
         if (flight && !Double.isNaN(fx.ended) || gone || t - fx.born > (double)(life(fx.kind, fx.value) + 2) || t < fx.born - 40.0D) {
            it.remove();
         }
      }

      CRUMBLED.values().removeIf((at) -> t - at > 60.0D || t < at - 40.0D);
      if (!MARKS.isEmpty() && level.getGameTime() % 40L == 0L) {
         MARKS.keySet().removeIf((id) -> level.getEntity(id) == null);
      }

   }

   static boolean firstSelf(ClientSaltSeraph.Ctx ctx, Entity p) {
      return p == ctx.mc.player && ctx.mc.options.getCameraType().isFirstPerson();
   }

   private static AbstractClientPlayer casterOf(ClientSaltSeraph.Ctx ctx, ClientSaltSeraph.Fx fx) {
      Entity e = ctx.level.getEntity(fx.caster);
      if (e instanceof AbstractClientPlayer p) {
         if (!p.isRemoved() && !p.isInvisible() && !p.isSpectator() && !p.isDeadOrDying() && p.deathTime <= 0) {
            if (p.distanceToSqr(ctx.cam[0], ctx.cam[1], ctx.cam[2]) > 5184.0D) {
               return null;
            }

            return p;
         }
      }

      return null;
   }

   static LivingEntity living(ClientSaltSeraph.Ctx ctx, int id) {
      if (id == 0) {
         return null;
      } else {
         Entity e = ctx.level.getEntity(id);
         if (e instanceof LivingEntity) {
            LivingEntity live = (LivingEntity)e;
            if (!e.isRemoved()) {
               return live;
            }
         }

         return null;
      }
   }

   static double bodyYaw(AbstractClientPlayer p, float pt) {
      return (double)Mth.rotLerp(pt, p.yBodyRotO, p.yBodyRot);
   }

   static double[] hand(ClientSaltSeraph.Ctx ctx, AbstractClientPlayer p, boolean left) {
      boolean first = firstSelf(ctx, p);
      if (!first) {
         HandCapture.Hands h = HandCapture.get(p.getId(), (double)((float)p.tickCount + ctx.pt));
         if (h != null) {
            Vec3 v = left ? h.left.add(h.leftDir.scale(0.08D)) : h.right.add(h.rightDir.scale(0.08D));
            return a(v);
         }
      }

      double side = left ? -1.0D : 1.0D;
      if (first) {
         double yaw_0 = (double)Mth.lerp(ctx.pt, p.yRotO, p.getYRot());
         double[] r = SaltMath.right(yaw_0);
         double[] look = a(p.getViewVector(ctx.pt));
         double[] eye = a(p.getEyePosition(ctx.pt));
         return SaltMath.add(SaltMath.add(eye, SaltMath.mul(r, 0.31D * side)), SaltMath.add(new double[]{0.0D, -0.27D, 0.0D}, SaltMath.mul(look, 0.72D)));
      } else {
         double yaw = bodyYaw(p, ctx.pt);
         double sc = (double)p.getBbHeight() / 1.8D;
         double[] feet = at(p, ctx.pt);
         return SaltMath.add(SaltMath.add(feet, SaltMath.mul(SaltMath.right(yaw), 0.38D * side * sc)), SaltMath.add(new double[]{0.0D, 1.3D * sc, 0.0D}, SaltMath.mul(SaltMath.fwd(yaw), 0.35D)));
      }
   }

   static double[][] frame(ClientSaltSeraph.Ctx ctx, AbstractClientPlayer p, int side) {
      BodyCapture.Body b = BodyCapture.get(p.getId(), (double)((float)p.tickCount + ctx.pt));
      if (b != null) {
         Vec3[] q = side > 0 ? b.right : b.left;
         return SaltShape.shoulder(a(q[0]), a(q[1]), a(q[2]), a(q[3]), side);
      } else {
         float yaw = (float)bodyYaw(p, ctx.pt);
         double[] feet = at(p, ctx.pt);
         double[][] local = SaltShape.shoulderLocal(side);
         double[][] w = new double[4][];

         for(int i = 0; i < 4; ++i) {
            Vec3 o = BodyCapture.restPoint(yaw, local[i]);
            w[i] = new double[]{feet[0] + o.x, feet[1] + o.y, feet[2] + o.z};
         }

         return SaltShape.shoulder(w[0], w[1], w[2], w[3], side);
      }
   }

   static double[] mouth(ClientSaltSeraph.Ctx ctx, AbstractClientPlayer p) {
      BodyCapture.Body b = BodyCapture.get(p.getId(), (double)((float)p.tickCount + ctx.pt));
      if (b != null) {
         return a(b.mouth);
      } else {
         double[] eye = a(p.getEyePosition(ctx.pt));
         double[] look = a(p.getViewVector(ctx.pt));
         return SaltMath.add(eye, SaltMath.add(new double[]{0.0D, -0.2D, 0.0D}, SaltMath.mul(look, 0.28D)));
      }
   }

   static double ground(ClientSaltSeraph.Ctx ctx, double x, double y, double z) {
      BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
      int bx = Mth.floor(x);
      int bz = Mth.floor(z);
      int top = Mth.floor(y);

      for(int by = top; by > top - 24; --by) {
         m.set(bx, by, bz);
         BlockState st = ctx.level.getBlockState(m);
         VoxelShape shape = st.getCollisionShape(ctx.level, m);
         double h = Double.NaN;
         if (!shape.isEmpty()) {
            h = (double)by + shape.max(Axis.Y);
         }

         FluidState fl = st.getFluidState();
         if (!fl.isEmpty()) {
            double fh = (double)((float)by + fl.getHeight(ctx.level, m));
            h = Double.isNaN(h) ? fh : Math.max(h, fh);
         }

         if (!Double.isNaN(h) && h <= y + 0.001D) {
            return h;
         }
      }

      return y - 24.0D;
   }

   private static void drawFx(ClientSaltSeraph.Ctx ctx, ClientSaltSeraph.Pen pen, ClientSaltSeraph.Fx fx) {
      double t = ctx.t;
      double age = t - fx.born;
      if (!(age < 0.0D)) {
         ctx.end = SaltMath.clamp01(((double)life(fx.kind, fx.value) - age) / 6.0D);
         switch (fx.kind) {
            case 0:
               AbstractClientPlayer p_0 = casterOf(ctx, fx);
               if (p_0 != null) {
                  double[] hr = hand(ctx, p_0, false);
                  if (age < 8.0D) {
                     SaltShape.gather(pen, hr, hand(ctx, p_0, true), age);
                  }

                  if (age >= 3.0D && age < 8.0D) {
                     double alpha = age < 4.0D ? 0.3D : (age < 5.0D ? 0.7D : 1.0D);
                     double[] look = a(p_0.getViewVector(ctx.pt));
                     double[] dir = SaltMath.norm(SaltMath.add(look, new double[]{0.0D, 0.15D, 0.0D}));
                     SaltShape.spear(pen, SaltMath.add(hr, SaltMath.mul(dir, 0.4D)), dir, alpha);
                  }
               }
               break;
            case 1:
               if (Double.isNaN(fx.ended)) {
                  double[] pl_0 = SaltMath.lanceAt(fx.p1, fx.p2, age);
                  SaltShape.spear(pen, pl_0, fx.p2, 1.0D);
                  SaltShape.lanceTrail(pen, pl_0, fx.p2, age);
               }
            case 2:
            case 24:
            default:
               break;
            case 3:
               SaltShape.burst(pen, fx.p1, age, Math.max(0.5D, (double)fx.value / 10.0D));
               break;
            case 4:
               SaltShape.shards(pen, fx.p1, fx.p2, age, (double)fx.value / 10.0D);
               break;
            case 5:
               LivingEntity e_0 = living(ctx, fx.target);
               SaltShape.pillarForm(pen, e_0 != null ? at(e_0, ctx.pt) : fx.p1, (double)fx.value / 100.0D, age);
               break;
            case 6:
               SaltShape.unsalt(pen, fx.p1, (double)fx.value / 100.0D, age);
               break;
            case 7:
               SaltShape.shatter(pen, fx.p1, age);
               break;
            case 8:
               SaltShape.crumble(pen, fx.p1, (double)fx.value / 100.0D, age);
               break;
            case 9:
               AbstractClientPlayer p_1 = casterOf(ctx, fx);
               if (p_1 != null && !(age < 4.0D) && !(age >= 28.0D) && !thrown(fx)) {
                  double[] eye = a(p_1.getEyePosition(ctx.pt));
                  double reach = Mth.clamp(SaltMath.dist(eye, fx.p1), 4.0D, 64.0D);
                  double[] aim = SaltMath.add(eye, SaltMath.mul(a(p_1.getViewVector(ctx.pt)), reach));
                  SaltShape.greatForm(pen, hand(ctx, p_1, false), aim, age, t);
               }
               break;
            case 10:
               if (Double.isNaN(fx.ended)) {
                  SaltShape.greatFlight(pen, fx.p1, fx.p2, age, t);
               }
               break;
            case 11:
               SaltShape.greatStuck(pen, fx.p1, fx.p2, age, t);
               break;
            case 12:
               if (Double.isNaN(fx.ground)) {
                  fx.ground = ground(ctx, fx.p1[0] - fx.p2[0], fx.p1[1] + 0.5D, fx.p1[2] - fx.p2[2]);
               }

               SaltShape.greatBurst(pen, fx.p1, fx.p2, fx.ground, age);
               break;
            case 13:
               SaltShape.dustPuff(pen, fx.p1, age);
               break;
            case 14:
               SaltShape.blockCrumble(pen, fx.p1, age);
               break;
            case 15:
               SaltShape.fizz(pen, fx.p1, age);
               break;
            case 16:
               drawSpires(ctx, pen, fx, age);
               break;
            case 17:
               AbstractClientPlayer p_2 = casterOf(ctx, fx);
               LivingEntity e_1 = living(ctx, fx.target);
               if (p_2 != null && e_1 != null) {
                  SaltShape.sealCast(pen, hand(ctx, p_2, false), at(e_1, ctx.pt), age);
               }
               break;
            case 18:
               LivingEntity e_2 = living(ctx, fx.target);
               SaltShape.seal(pen, e_2 != null ? at(e_2, ctx.pt) : fx.p1, age);
               break;
            case 19:
               AbstractClientPlayer p_3 = casterOf(ctx, fx);
               if (p_3 != null) {
                  double[] pp = at(p_3, ctx.pt);
                  SaltShape.rise(pen, pp, ground(ctx, pp[0], pp[1] + 0.5D, pp[2]), age);
               }
               break;
            case 20:
               drawWings(ctx, pen, fx, age);
               break;
            case 21:
               AbstractClientPlayer p_4 = casterOf(ctx, fx);
               if (p_4 != null) {
                  double[] mid = SaltMath.mul(SaltMath.add(hand(ctx, p_4, false), hand(ctx, p_4, true)), 0.5D);
                  SaltShape.horn(pen, mouth(ctx, p_4), mid, age, (double)life(fx.kind, fx.value));
               }
               break;
            case 22:
               SaltShape.trumpet(pen, fx.p1, age);
               break;
            case 23:
               LivingEntity e = living(ctx, fx.target);
               double[] at = e != null ? SaltMath.mid(at(e, ctx.pt), (double)e.getBbHeight()) : fx.p1;
               SaltShape.smite(pen, at, age);
               break;
            case 25:
               drawTree(ctx, pen, fx, age);
               break;
            case 26:
               AbstractClientPlayer p_5 = casterOf(ctx, fx);
               if (p_5 != null) {
                  SaltShape.erupt(pen, at(p_5, ctx.pt), frame(ctx, p_5, rightSide(ctx, p_5)), age);
               }
               break;
            case 27:
               SaltShape.blast(pen, fx.p1, age);
               break;
            case 28:
               SaltShape.blink(pen, fx.p1, age);
               break;
            case 29:
               AbstractClientPlayer p = casterOf(ctx, fx);
               if (p != null) {
                  double yaw = (double)Mth.lerp(ctx.pt, p.yRotO, p.getYRot());
                  SaltShape.rake(pen, SaltShape.rakeAt(at(p, ctx.pt), yaw), yaw, age);
               }
               break;
            case 30:
               SaltShape.howl(pen, fx.p1, age);
               break;
            case 31:
               drawThorns(ctx, pen, fx, age);
               break;
            case 32:
               drawRend(ctx, pen, fx, age);
         }

      }
   }

   private static boolean thrown(ClientSaltSeraph.Fx form) {
      for(ClientSaltSeraph.Fx fx : FX) {
         if (fx.kind == 10 && fx.caster == form.caster && fx.born >= form.born) {
            return true;
         }
      }

      return false;
   }

   private static void drawSpires(ClientSaltSeraph.Ctx ctx, ClientSaltSeraph.Pen pen, ClientSaltSeraph.Fx fx, double age) {
      if (fx.spikes == null) {
         List<double[]> spikes = new ArrayList();
         double[] dir = fx.p2;
         double[] side = new double[]{-dir[2], 0.0D, dir[0]};

         for(int row = 0; SaltMath.spiresRunning(row); ++row) {
            double front = SaltMath.spiresFront(row);
            int n = SaltMath.spiresCount(front);

            for(int k = 0; k < n; ++k) {
               double[] s = SaltShape.spiresSpike(fx.p1, dir, side, row, k, n, front);
               double g = ground(ctx, s[0], fx.p1[1] + 3.0D, s[2]);
               spikes.add(new double[]{s[0], g, s[2], s[3], s[4], s[5], s[6], s[7], (double)row});
            }
         }

         fx.spikes = spikes;
      }

      for(double[] s : fx.spikes) {
         SaltShape.spiresDraw(pen, new double[]{s[0], s[1], s[2]}, s[3], s[4], new double[]{s[5], s[6], s[7]}, age - s[8]);
      }

   }

   static int rightSide(ClientSaltSeraph.Ctx ctx, AbstractClientPlayer p) {
      double[][] fr = frame(ctx, p, 1);
      double[] r = SaltMath.right(bodyYaw(p, ctx.pt));
      return SaltMath.dot(fr[1], r) > 0.0D ? 1 : -1;
   }

   private static void drawTree(ClientSaltSeraph.Ctx ctx, ClientSaltSeraph.Pen pen, ClientSaltSeraph.Fx fx, double age) {
      AbstractClientPlayer p = casterOf(ctx, fx);
      if (p != null && !firstSelf(ctx, p)) {
         double la = age - 7.0D;
         if (!(la < 0.0D)) {
            double shrink = Double.isNaN(fx.ended) ? 0.0D : SaltMath.treeShrink(ctx.t - fx.ended);
            if (!(shrink >= 1.0D)) {
               double alpha = 1.0D - shrink;
               List<double[]> tips = SaltShape.branches(pen, frame(ctx, p, rightSide(ctx, p)), 3.3D, 11, SaltMath.treeGrow(Math.min(la, 4.499999999999999D), shrink), 1.4D, 3.0D, alpha);
               double[] grounds = fx.grounds[0];
               if (ctx.pass == 0 || grounds == null || grounds.length != tips.size()) {
                  grounds = new double[tips.size()];

                  for(int j = 0; j < grounds.length; ++j) {
                     double[] tp = (double[])tips.get(j);
                     grounds[j] = ground(ctx, tp[0], tp[1], tp[2]);
                  }

                  fx.grounds[0] = grounds;
               }

               SaltShape.drips(pen, tips, grounds, ctx.t, alpha);
            }
         }
      }
   }

   private static void drawThorns(ClientSaltSeraph.Ctx ctx, ClientSaltSeraph.Pen pen, ClientSaltSeraph.Fx fx, double age) {
      if (fx.spikes == null) {
         List<double[]> bases = new ArrayList();

         for(int ring = 1; ring <= 4; ++ring) {
            int n = SaltShape.thornCount(ring);

            for(int i = 0; i < n; ++i) {
               double[] s = SaltShape.thornSpot(fx.p1, ring, i);
               bases.add(new double[]{s[0], ground(ctx, s[0], fx.p1[1] + 3.0D, s[2]), s[2]});
            }
         }

         fx.spikes = bases;
      }

      SaltShape.thorns(pen, fx.p1, (double[][])fx.spikes.toArray(new double[0][]), age);
   }

   private static void drawRend(ClientSaltSeraph.Ctx ctx, ClientSaltSeraph.Pen pen, ClientSaltSeraph.Fx fx, double age) {
      double[] dir = fx.p2;
      double[] side = new double[]{-dir[2], 0.0D, dir[0]};
      if (fx.spikes == null) {
         List<double[]> spots = new ArrayList();
         int steps = SaltShape.rendSteps();
         double[] ys = new double[steps];

         for(int k2 = 0; k2 < steps; ++k2) {
            for(int l2 = -1; l2 <= 1; ++l2) {
               double[] bp = SaltShape.rendSpot(fx.p1, dir, side, k2, l2);
               double g = ground(ctx, bp[0], bp[1] + 6.0D, bp[2]);
               spots.add(new double[]{bp[0], g, bp[2]});
               if (l2 == 0) {
                  ys[k2] = g;
               }
            }
         }

         fx.spikes = spots;
         fx.grounds[1] = ys;
      }

      double reach = Math.min(60.0D, 2.0D * age);
      double[] front = SaltMath.add(fx.p1, SaltMath.mul(dir, reach));
      double g0 = ground(ctx, front[0], front[1] + 4.0D, front[2]);
      SaltShape.rend(pen, fx.p1, dir, side, age, g0, (double[][])fx.spikes.toArray(new double[0][]), fx.grounds[1]);
   }

   private static void drawWings(ClientSaltSeraph.Ctx ctx, ClientSaltSeraph.Pen pen, ClientSaltSeraph.Fx fx, double age) {
      AbstractClientPlayer p = casterOf(ctx, fx);
      if (p != null && !firstSelf(ctx, p)) {
         double lifeT = (double)life(fx.kind, fx.value);
         double grow = SaltShape.wingGrow(age, lifeT);
         double alpha = SaltShape.wingAlpha(age, lifeT);
         if (!(grow <= 0.0D) && !(alpha <= 0.0D)) {
            for(int i = 0; i < 2; ++i) {
               int side = i == 0 ? 1 : -1;
               List<double[]> tips = SaltShape.branches(pen, frame(ctx, p, side), 1.5D, side > 0 ? 3 : 5, grow, 2.2D, 4.0D, alpha);
               double[] grounds = fx.grounds[i];
               if (ctx.pass == 0 || grounds == null || grounds.length != tips.size()) {
                  grounds = new double[tips.size()];

                  for(int j = 0; j < grounds.length; ++j) {
                     double[] tp = (double[])tips.get(j);
                     grounds[j] = ground(ctx, tp[0], tp[1], tp[2]);
                  }

                  fx.grounds[i] = grounds;
               }

               SaltShape.drips(pen, tips, grounds, ctx.t, alpha);
            }

         }
      }
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_SKY) {
         inWorld = true;
      } else if (event.getStage() == Stage.AFTER_LEVEL) {
         inWorld = false;
      }

      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         Minecraft mc = Minecraft.getInstance();
         ClientLevel level = mc.level;
         if (level == null) {
            clearAll();
         } else {
            float pt = event.getPartialTick();
            double t = (double)((float)level.getGameTime() + pt);
            prune(level, t);
            if (!FX.isEmpty()) {
               Camera camera = event.getCamera();
               Vector3f l = camera.getLeftVector();
               Vector3f u = camera.getUpVector();
               ClientSaltSeraph.Ctx ctx = new ClientSaltSeraph.Ctx();
               ctx.cam = a(camera.getPosition());
               ctx.right = new double[]{(double)(-l.x()), (double)(-l.y()), (double)(-l.z())};
               ctx.up = new double[]{(double)u.x(), (double)u.y(), (double)u.z()};
               ctx.m = event.getPoseStack().last().pose();
               SaltDraw.normals(event.getPoseStack().last().normal());
               ctx.level = level;
               ctx.mc = mc;
               ctx.t = t;
               ctx.pt = pt;
               MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
               ctx.buffers = buffers;
               ClientSaltSeraph.Pen pen = new ClientSaltSeraph.Pen();
               pen.ctx = ctx;
               List<ClientSaltSeraph.Fx> all = new ArrayList(FX);

               for(int pass : PASSES) {
                  ctx.pass = pass;
                  ctx.vc = pass == 0 ? buffers.getBuffer(SaltDraw.soft()) : (pass == 1 ? buffers.getBuffer(SaltDraw.add()) : null);

                  try {
                     for(ClientSaltSeraph.Fx fx : all) {
                        drawFx(ctx, pen, fx);
                     }
                  } finally {
                     if (pass == 0) {
                        buffers.endBatch(SaltDraw.soft());
                     } else if (pass == 1) {
                        buffers.endBatch(SaltDraw.add());
                     } else {
                        buffers.endBatch(SaltDraw.ground());
                        buffers.endBatch(SaltDraw.cracks());
                     }

                  }
               }

            }
         }
      }
   }

   static boolean odd(LivingEntity e) {
      ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
      if (key == null) {
         return true;
      } else {
         String id = key.toString();
         return id.equals("minecraft:slime") || id.equals("minecraft:magma_cube") || id.equals("minecraft:ghast") || id.equals("minecraft:phantom") || id.equals("minecraft:giant") || id.equals("minecraft:pufferfish") || id.equals("minecraft:elder_guardian") || id.equals("minecraft:wither") || id.equals("minecraft:ender_dragon") || id.equals("minecraft:shulker");
      }
   }

   static float scaleOf(LivingEntity e) {
      if (!(e instanceof Player) && !(e instanceof Witch) && !(e instanceof AbstractIllager)) {
         ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
         String id = key == null ? "" : key.toString();
         if (id.equals("minecraft:cave_spider")) {
            return 0.7F;
         } else if (!id.equals("minecraft:wither_skeleton") && !id.equals("minecraft:polar_bear")) {
            if (id.equals("minecraft:husk")) {
               return 1.0625F;
            } else if (id.equals("minecraft:horse")) {
               return 1.1F;
            } else if (id.equals("minecraft:donkey")) {
               return 0.87F;
            } else if (id.equals("minecraft:mule")) {
               return 0.92F;
            } else if (id.equals("minecraft:cat")) {
               return 0.8F;
            } else if (id.equals("minecraft:bat")) {
               return 0.35F;
            } else if (e instanceof AbstractVillager) {
               return e.isBaby() ? 0.46875F : 0.9375F;
            } else {
               return 1.0F;
            }
         } else {
            return 1.2F;
         }
      } else {
         return 0.9375F;
      }
   }

   static boolean posed(LivingEntity e, float pt) {
      if (!e.hasPose(Pose.SLEEPING) && !e.isFallFlying() && !e.isAutoSpinAttack() && !e.hasPose(Pose.SWIMMING)) {
         if (e instanceof Player) {
            Player pl = (Player)e;
            if (pl.getSwimAmount(pt) > 0.0F) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   @SubscribeEvent
   public static void onRenderPre(RenderLivingEvent.Pre<?, ?> event) {
      if (!CRUMBLED.isEmpty()) {
         LivingEntity e = event.getEntity();
         if (CRUMBLED.containsKey(e.getId()) && (e.isDeadOrDying() || e.deathTime > 0)) {
            event.setCanceled(true);
         }

      }
   }

   @SubscribeEvent
   public static void onRenderPost(RenderLivingEvent.Post<?, ?> event) {
      if (inWorld && (!MARKS.isEmpty() || SaltHudState.fresh())) {
         LivingEntity e = event.getEntity();
         if (!e.isInvisible() && e.deathTime <= 0) {
            ClientSaltSeraph.Mark mark = (ClientSaltSeraph.Mark)MARKS.get(e.getId());
            boolean judged = SaltHudState.judged(e.getId());
            if (mark != null || judged) {
               Minecraft mc = Minecraft.getInstance();
               if (mc.getCameraEntity() == null || !(e.distanceToSqr(mc.getCameraEntity()) > 4096.0D)) {
                  float pt = event.getPartialTick();

                  try {
                     if (mark != null && !odd(e) && !posed(e, pt)) {
                        crust(event, e, mark, pt);
                     }

                     boolean near = mc.getCameraEntity() == null || e.distanceToSqr(mc.getCameraEntity()) <= 1024.0D;
                     if (near && mark != null && (mark.flags & 1) == 0 && (mark.salt > 0 || (mark.flags & 2) != 0)) {
                        meter(event, mc, e, mark);
                     }

                     if (near && judged) {
                        judgment(event, mc, e, pt);
                     }
                  } catch (Throwable var7) {
                     MARKS.remove(e.getId());
                  }

               }
            }
         }
      }
   }

   private static void crust(RenderLivingEvent.Post<?, ?> event, LivingEntity e, ClientSaltSeraph.Mark mark, float pt) {
      boolean statue = (mark.flags & 1) != 0;
      boolean pillar = (mark.flags & 2) != 0;
      double frac = pillar ? 1.0D : (double)mark.salt / 100.0D;
      if (statue || !(frac <= 0.01D)) {
         double fill = (double)e.getBbHeight() * (!statue && !pillar ? Math.min(1.0D, frac * 1.05D) : 1.2D);
         EntityModel<?> model = event.getRenderer().getModel();
         PoseStack ps = event.getPoseStack();
         Matrix4f back = (new Matrix4f(ps.last().pose())).invert();
         ps.pushPose();

         try {
            ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(pt, e.yBodyRotO, e.yBodyRot)));
            float scale = scaleOf(e) * 1.02F;
            ps.scale(-scale, -scale, scale);
            ps.translate(0.0D, -1.501D, 0.0D);
            SaltCrust skin = new SaltCrust(event.getMultiBufferSource().getBuffer(SaltDraw.crust()), back, fill, e.getId());
            float alpha = !statue && !pillar ? 0.95F : 1.0F;
            model.renderToBuffer(ps, skin, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, alpha);
         } finally {
            ps.popPose();
         }

      }
   }

   private static void meter(RenderLivingEvent.Post<?, ?> event, Minecraft mc, LivingEntity e, ClientSaltSeraph.Mark mark) {
      boolean pillar = (mark.flags & 2) != 0;
      int v = pillar ? 100 : mark.salt;
      boolean tag = e instanceof Player ? e != mc.player : e.hasCustomName();
      String label = pillar ? "SALT PILLAR" : ((double)v >= 50.0D ? "SLOWED" : "");
      PoseStack ps = event.getPoseStack();
      ps.pushPose();

      try {
         ps.translate(0.0D, (double)e.getBbHeight() + (tag ? 0.2D : 0.45D), 0.0D);
         up(ps);
         ps.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
         ps.scale(-0.025F, -0.025F, 0.025F);
         Matrix4f m = ps.last().pose();
         VertexConsumer vc = event.getMultiBufferSource().getBuffer(SaltDraw.flat());
         int w = 22;
         int h = 3;
         float x0 = (float)(-w) / 2.0F;
         rect(vc, m, x0 - 1.0F, -1.0F, x0 + (float)w + 1.0F, (float)h + 1.0F, 20, 24, 30, 0.75F);
         float fillW = (float)w * Math.max(0.0F, Math.min(1.0F, (float)v / 100.0F));
         int rgb = v >= 100 ? 16777215 : (v >= 50 ? 15265522 : 12897492);
         rect(vc, m, x0, 0.0F, x0 + fillW, (float)h, rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, 1.0F);
         float half = x0 + (float)w * 0.5F;
         rect(vc, m, half, -1.0F, half + 1.0F, (float)h + 1.0F, 120, 130, 140, 0.9F);
         if (!label.isEmpty()) {
            Font font = mc.font;
            float lw = (float)font.width(label) * 0.6F;
            ps.pushPose();

            try {
               ps.translate(-lw / 2.0F, tag ? (float)h + 2.0F : -7.0F, -0.01F);
               ps.scale(0.6F, 0.6F, 0.6F);
               font.drawInBatch(label, 0.0F, 0.0F, v >= 100 ? -1 : -2235158, false, ps.last().pose(), event.getMultiBufferSource(), DisplayMode.NORMAL, 0, 15728880);
            } finally {
               ps.popPose();
            }
         }
      } finally {
         ps.popPose();
      }

   }

   private static void judgment(RenderLivingEvent.Post<?, ?> event, Minecraft mc, LivingEntity e, float pt) {
      double t = (double)((float)e.level().getGameTime() + pt);
      float alpha = (float)(0.6D + 0.4D * Math.sin(t * 0.5D));
      PoseStack ps = event.getPoseStack();
      ps.pushPose();

      try {
         ps.translate(0.0D, (double)e.getBbHeight() + 0.9D, 0.0D);
         up(ps);
         ps.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
         ps.scale(-0.025F, -0.025F, 0.025F);
         Matrix4f m = ps.last().pose();
         VertexConsumer vc = event.getMultiBufferSource().getBuffer(SaltDraw.flat());
         rect(vc, m, -1.0F, -7.0F, 2.0F, 2.0F, 255, 226, 140, alpha);
         rect(vc, m, -4.0F, -4.0F, 5.0F, -1.0F, 255, 226, 140, alpha);
      } finally {
         ps.popPose();
      }

   }

   static void rect(VertexConsumer vc, Matrix4f m, float x0, float y0, float x1, float y1, int r, int g, int b, float a) {
      if (!(x1 <= x0) && !(y1 <= y0) && !(a <= 0.003F)) {
         float rr = (float)r / 255.0F;
         float gg = (float)g / 255.0F;
         float bb = (float)b / 255.0F;
         float[][] pts = new float[][]{{x0, y1}, {x1, y1}, {x1, y0}, {x0, y0}};

         for(int i = 0; i < 4; ++i) {
            corner(vc, m, pts[i][0], pts[i][1], rr, gg, bb, a);
         }

         for(int i = 3; i >= 0; --i) {
            corner(vc, m, pts[i][0], pts[i][1], rr, gg, bb, a);
         }

      }
   }

   private static void up(PoseStack ps) {
      Vector3f n = ps.last().normal().transform(new Vector3f(0.0F, 1.0F, 0.0F));
      METER_UP[0] = n.x();
      METER_UP[1] = n.y();
      METER_UP[2] = n.z();
   }

   private static void corner(VertexConsumer vc, Matrix4f m, float x, float y, float r, float g, float b, float a) {
      vc.vertex(m, x, y, 0.0F).color(r, g, b, a).uv(0.5F, 0.5F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(METER_UP[0], METER_UP[1], METER_UP[2]).endVertex();
   }

   static final class Ctx {
      int pass;
      VertexConsumer vc;
      MultiBufferSource.BufferSource buffers;
      Matrix4f m;
      double[] cam;
      double[] right;
      double[] up;
      ClientLevel level;
      Minecraft mc;
      double t;
      float pt;
      double end = 1.0D;
   }

   static final class Fx {
      int caster;
      int kind;
      int slot;
      int target;
      int value;
      int flags;
      double[] p1;
      double[] p2;
      double born;
      double ended = Double.NaN;
      double ground = Double.NaN;
      List<double[]> spikes;
      double[][] grounds = new double[2][];
   }

   static final class Mark {
      int salt;
      int flags;
   }

   static final class Pen implements SaltShape.Sink {
      ClientSaltSeraph.Ctx ctx;

      boolean mine(boolean add) {
         return this.ctx.pass == (add ? 1 : 0);
      }

      public void bill(boolean add, double[] c, double size, double spin, int cell, double r, double g, double b, double a) {
         if (this.mine(add)) {
            SaltDraw.bill(this.ctx.vc, this.ctx.m, this.ctx.cam, c, this.ctx.right, this.ctx.up, size, spin, cell, ClientSaltSeraph.f(r), ClientSaltSeraph.f(g), ClientSaltSeraph.f(b), ClientSaltSeraph.f(a), add);
         }

      }

      public void quad(boolean add, double[] p1, double[] p2, double[] p3, double[] p4, int cell, double r, double g, double b, double a) {
         if (this.mine(add)) {
            SaltDraw.quad(this.ctx.vc, this.ctx.m, this.ctx.cam, p1, p2, p3, p4, cell, ClientSaltSeraph.f(r), ClientSaltSeraph.f(g), ClientSaltSeraph.f(b), ClientSaltSeraph.f(a), ClientSaltSeraph.f(a), ClientSaltSeraph.f(a), ClientSaltSeraph.f(a), add);
         }

      }

      public void ribbon(boolean add, double[] a, double[] b, double w, int cell, double r, double g, double bl, double alpha) {
         if (this.mine(add)) {
            SaltDraw.ribbon(this.ctx.vc, this.ctx.m, this.ctx.cam, a, b, w, cell, ClientSaltSeraph.f(r), ClientSaltSeraph.f(g), ClientSaltSeraph.f(bl), ClientSaltSeraph.f(alpha), add);
         }

      }

      public void slice(boolean add, double[] p1, double[] p2, double[] p3, double[] p4, int cell, double r, double g, double b, double a, double v0, double v1) {
         if (this.mine(add)) {
            SaltDraw.slice(this.ctx.vc, this.ctx.m, this.ctx.cam, p1, p2, p3, p4, cell, ClientSaltSeraph.f(r), ClientSaltSeraph.f(g), ClientSaltSeraph.f(b), ClientSaltSeraph.f(a), v0, v1, add);
         }

      }

      public void decal(int kind, double[] c, double radius, double spin, double a) {
         if (this.ctx.pass == 2) {
            double g = ClientSaltSeraph.ground(this.ctx, c[0], c[1] + 1.0D, c[2]);
            if (c[1] + 1.0D - g > 4.0D) {
               return;
            }

            double[] on = new double[]{c[0], g, c[2]};
            int lit = LevelRenderer.getLightColor(this.ctx.level, BlockPos.containing(on[0], on[1] + 0.2D, on[2]));
            SaltDraw.decal(this.ctx.buffers, this.ctx.m, this.ctx.cam, kind, on, radius, spin, ClientSaltSeraph.f(a * this.ctx.end), lit);
         }

      }

      public void strip(int kind, double[] a, double[] b, double w, double alpha) {
         if (this.ctx.pass == 2) {
            int lit = LevelRenderer.getLightColor(this.ctx.level, BlockPos.containing((a[0] + b[0]) * 0.5D, a[1] + 0.2D, (a[2] + b[2]) * 0.5D));
            SaltDraw.strip(this.ctx.buffers, this.ctx.m, this.ctx.cam, kind, a, b, w, ClientSaltSeraph.f(alpha * this.ctx.end), lit);
         }

      }

      public void chain(boolean add, double[][] pts, double[] widths, int cell, double[] rgb, double[] alphas) {
         if (this.mine(add)) {
            SaltDraw.chain(this.ctx.vc, this.ctx.m, this.ctx.cam, pts, widths, cell, ClientSaltSeraph.f(rgb[0]), ClientSaltSeraph.f(rgb[1]), ClientSaltSeraph.f(rgb[2]), alphas, add);
         }

      }

      public void ring(boolean add, double[] c, double radius, double w, int cell, double[] rgb, double a) {
         if (this.mine(add)) {
            SaltDraw.ring(this.ctx.vc, this.ctx.m, this.ctx.cam, c, radius, w, cell, ClientSaltSeraph.f(rgb[0]), ClientSaltSeraph.f(rgb[1]), ClientSaltSeraph.f(rgb[2]), ClientSaltSeraph.f(a), add);
         }

      }

      public void upright(boolean add, double[] base, double w, double h, int cell, double r, double g, double b, double a) {
         if (this.mine(add)) {
            SaltDraw.upright(this.ctx.vc, this.ctx.m, this.ctx.cam, base, w, h, cell, ClientSaltSeraph.f(r), ClientSaltSeraph.f(g), ClientSaltSeraph.f(b), ClientSaltSeraph.f(a), add);
         }

      }
   }
}
