package com.myriam.unrp.uo;



import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
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
public final class ClientBlackLash {
   static final double SEEN = 72.0D;
   static final int MOST_CASTS = 256;
   static final int MOST_SHOTS = 384;
   static final int STALE = 30;
   static final int STRAIN_STALE = 60;
   static final double NEAR_SELF = 0.6D;
   static final double SWING_LATCH = 2.0D;
   static final int[] PASSES = new int[]{2, 0, 1};
   static final double SETTLE = 4.0D;
   static final double STORM_STRUCK = 13.0D;
   static final double WIND_STRUCK = 12.0D;
   static final double TRAIL_W = 0.05D;
   static final double TRAIL_A = 0.35D;
   static final int FRENZY_LEG_ROWS = 6;
   static final double FRENZY_BURST_TICKS = 10.0D;
   static final double FRENZY_LIFTED = 0.25D;
   static final double FRENZY_REPLANT = 0.5D;
   static final int POUND_LIFE = 28;
   static final int POUND_ROWS = 5;
   static final int POUND_PUFFS = 14;
   static final double POUND_FLASH = 6.0D;
   static final double POUND_DUG = 3.0D;
   private static final Map<Long, ClientBlackLash.Cast> CASTS = new HashMap();
   private static final List<ClientBlackLash.Cast> SHOTS = new ArrayList();

   private ClientBlackLash() {
   }

   static long key(int caster, int kind, int slot) {
      return (long)caster << 16 | (long)(kind & 255) << 8 | (long)(slot & 255);
   }

   static boolean lasting(int kind) {
      return kind == 0 || kind == 2 || kind == 3 || kind == 6 || kind == 10 || kind == 12 || kind == 15 || kind == 16 || kind == 18 || kind == 19 || kind == 22;
   }

   static int shotLife(int kind) {
      switch (kind) {
         case 1:
            return 14;
         case 2:
         case 3:
         case 6:
         case 10:
         case 12:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 22:
         default:
            return 0;
         case 4:
            return 12;
         case 5:
            return 18;
         case 7:
            return 24;
         case 8:
            return 6;
         case 9:
            return 18;
         case 11:
            return 12;
         case 13:
            return 17;
         case 20:
            return 24;
         case 21:
            return 30;
         case 23:
            return 16;
         case 24:
            return 40;
         case 25:
            return LashMath.frenzyStroke() + 2;
         case 26:
            return 28;
      }
   }

   static int endFade(int kind) {
      switch (kind) {
         case 0:
            return 5;
         case 1:
         case 4:
         case 5:
         case 7:
         case 8:
         case 9:
         case 11:
         case 13:
         case 14:
         case 16:
         case 17:
         case 20:
         case 21:
         default:
            return 1;
         case 2:
            return 6;
         case 3:
            return 8;
         case 6:
            return 7;
         case 10:
            return 9;
         case 12:
            return 15;
         case 15:
            return 9;
         case 18:
            return 6;
         case 19:
            return 2;
         case 22:
            return 10;
      }
   }

   public static void accept(LashFxPacket msg) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc.level;
      if (level != null) {
         double t = (double)level.getGameTime();
         if (msg.kind == 17) {
            ClientBlackLash.Cast c_0 = (ClientBlackLash.Cast)CASTS.get(key(msg.caster, msg.value, msg.slot));
            if (c_0 != null && Double.isNaN(c_0.ended)) {
               c_0.ended = t;
            }

         } else if (msg.kind == 14) {
            ClientBlackLash.Cast c_1 = (ClientBlackLash.Cast)CASTS.get(key(msg.caster, 12, 0));
            if (c_1 != null) {
               c_1.finale = t;
            }

         } else {
            ClientBlackLash.Cast c;
            if (lasting(msg.kind)) {
               long k = key(msg.caster, msg.kind, msg.slot);
               c = (ClientBlackLash.Cast)CASTS.get(k);
               if (c == null || !Double.isNaN(c.ended) || msg.age == 0) {
                  if (c == null && CASTS.size() >= 256) {
                     return;
                  }

                  c = new ClientBlackLash.Cast();
                  c.born = t - (double)msg.age;
                  CASTS.put(k, c);
                  if (msg.kind == 2) {
                     CASTS.remove(key(msg.caster, 0, 0));
                  }
               }
            } else {
               if (SHOTS.size() >= 384) {
                  SHOTS.remove(0);
               }

               c = new ClientBlackLash.Cast();
               c.born = t;
               SHOTS.add(c);
            }

            c.caster = msg.caster;
            c.kind = msg.kind;
            c.slot = msg.slot;
            c.target = msg.target;
            c.value = msg.value;
            c.flags = msg.flags;
            c.p1 = new double[]{(double)msg.x, (double)msg.y, (double)msg.z};
            c.p2 = new double[]{(double)msg.x2, (double)msg.y2, (double)msg.z2};
            c.seen = t;
         }
      }
   }

   private static void prune(double t) {
      Iterator<ClientBlackLash.Cast> it = CASTS.values().iterator();

      while(it.hasNext()) {
         ClientBlackLash.Cast c_0 = (ClientBlackLash.Cast)it.next();
         boolean gone;
         if (!Double.isNaN(c_0.ended)) {
            gone = t - c_0.ended > (double)endFade(c_0.kind);
         } else if (c_0.kind == 0) {
            gone = t - c_0.born > 60.0D;
         } else if (c_0.kind != 10 && c_0.kind != 12 && c_0.kind != 15 && c_0.kind != 18 && c_0.kind != 19 && c_0.kind != 22) {
            gone = t - c_0.seen > (double)(c_0.kind == 16 ? 60 : 30);
         } else {
            gone = t - c_0.born > (double)(c_0.value + 2) || t - c_0.seen > (double)(30 + c_0.value);
         }

         if (gone || t < c_0.born - 40.0D) {
            it.remove();
         }
      }

      Iterator<ClientBlackLash.Cast> si = SHOTS.iterator();

      while(si.hasNext()) {
         ClientBlackLash.Cast c = (ClientBlackLash.Cast)si.next();
         if (t - c.born > (double)(shotLife(c.kind) + 1) || t < c.born - 40.0D) {
            si.remove();
         }
      }

   }

   static double[] a(Vec3 v) {
      return new double[]{v.x, v.y, v.z};
   }

   static double[] at(Entity e, float pt) {
      return a(e.getPosition(pt));
   }

   static double bodyYaw(AbstractClientPlayer p, float pt) {
      return (double)Mth.rotLerp(pt, p.yBodyRotO, p.yBodyRot);
   }

   static double[] hand(ClientBlackLash.Ctx ctx, AbstractClientPlayer p, boolean left) {
      boolean firstSelf = p == ctx.mc.player && ctx.mc.options.getCameraType().isFirstPerson();
      if (!firstSelf) {
         HandCapture.Hands h = HandCapture.get(p.getId(), (double)((float)p.tickCount + ctx.pt));
         if (h != null) {
            Vec3 v = left ? h.left.add(h.leftDir.scale(0.08D)) : h.right.add(h.rightDir.scale(0.08D));
            return a(v);
         }
      }

      double side = left ? -1.0D : 1.0D;
      if (firstSelf) {
         double yaw_0 = (double)Mth.lerp(ctx.pt, p.yRotO, p.getYRot());
         double[] r = LashMath.right(yaw_0);
         double[] look = a(p.getViewVector(ctx.pt));
         double[] eye = a(p.getEyePosition(ctx.pt));
         return LashMath.add(LashMath.add(eye, LashMath.mul(r, 0.31D * side)), LashMath.add(new double[]{0.0D, -0.27D, 0.0D}, LashMath.mul(look, 0.72D)));
      } else {
         double yaw = bodyYaw(p, ctx.pt);
         double sc = (double)p.getBbHeight() / 1.8D;
         double[] feet = at(p, ctx.pt);
         return LashMath.add(LashMath.add(feet, LashMath.mul(LashMath.right(yaw), 0.38D * side * sc)), LashMath.add(new double[]{0.0D, 1.3D * sc, 0.0D}, LashMath.mul(LashMath.fwd(yaw), 0.35D)));
      }
   }

   static boolean firstSelf(ClientBlackLash.Ctx ctx, AbstractClientPlayer p) {
      return p == ctx.mc.player && ctx.mc.options.getCameraType().isFirstPerson();
   }

   static boolean selfCast(ClientBlackLash.Ctx ctx, int caster) {
      return ctx.mc.player != null && caster == ctx.mc.player.getId() && ctx.mc.options.getCameraType().isFirstPerson();
   }

   static double[][] nearCut(ClientBlackLash.Ctx ctx, double[][] pts) {
      int i0;
      for(i0 = 0; i0 < pts.length - 1 && LashMath.dist(pts[i0], ctx.cam) < 0.6D; ++i0) {
      }

      if (i0 == 0) {
         return pts;
      } else {
         double[][] out = new double[pts.length - i0][];
         System.arraycopy(pts, i0, out, 0, out.length);
         return out;
      }
   }

   static int segs(double len) {
      return Math.max(22, Math.min(96, (int)Math.ceil(len * 1.6D)));
   }

   static double waves(double len) {
      return Math.max(1.5D, Math.min(5.0D, len * 0.35D));
   }

   static double[][] resample(double[][] pts, int n) {
      double[][] out = new double[n][];
      int m = pts.length;

      for(int i = 0; i < n; ++i) {
         double f = n > 1 ? (double)(i * (m - 1)) / (double)(n - 1) : 0.0D;
         int i0 = Math.min(m - 2, (int)Math.floor(f));
         out[i] = LashMath.lerp(pts[i0], pts[i0 + 1], LashMath.clamp01(f - (double)i0));
      }

      return out;
   }

   static double[][] settle(double[][] struck, double[][] idle, double w) {
      double[][] from = struck.length == idle.length ? struck : resample(struck, idle.length);
      double k = LashMath.smooth01(w);
      double[][] out = new double[idle.length][];

      for(int i = 0; i < idle.length; ++i) {
         out[i] = LashMath.lerp(from[i], idle[i], k);
      }

      return out;
   }

   public static void clearAll() {
      CASTS.clear();
      SHOTS.clear();
   }

   static LivingEntity living(ClientBlackLash.Ctx ctx, int id) {
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

   static double[] rim(ClientBlackLash.Cast c) {
      return (c.flags & 16) != 0 ? LashShape.RAGE : LashShape.RIM;
   }

   static float f(double v) {
      return (float)v;
   }

   static void softChain(ClientBlackLash.Ctx ctx, double[][] pts, double[] widths, int cell, double[] rgb, double[] alphas) {
      if (ctx.pass == 0) {
         LashDraw.chain(ctx.vc, ctx.m, ctx.cam, pts, widths, cell, f(rgb[0]), f(rgb[1]), f(rgb[2]), alphas, false);
      }

   }

   static void addChain(ClientBlackLash.Ctx ctx, double[][] pts, double[] widths, int cell, double[] rgb, double[] alphas) {
      if (ctx.pass == 1) {
         LashDraw.chain(ctx.vc, ctx.m, ctx.cam, pts, widths, cell, f(rgb[0]), f(rgb[1]), f(rgb[2]), alphas, true);
      }

   }

   static void softBill(ClientBlackLash.Ctx ctx, double[] c, double size, double spin, int cell, double r, double g, double b, double a) {
      if (ctx.pass == 0) {
         LashDraw.bill(ctx.vc, ctx.m, ctx.cam, c, ctx.right, ctx.up, size, spin, cell, f(r), f(g), f(b), f(a), false);
      }

   }

   static void addBill(ClientBlackLash.Ctx ctx, double[] c, double size, double spin, int cell, double r, double g, double b, double a) {
      if (ctx.pass == 1) {
         LashDraw.bill(ctx.vc, ctx.m, ctx.cam, c, ctx.right, ctx.up, size, spin, cell, f(r), f(g), f(b), f(a), true);
      }

   }

   static void addRing(ClientBlackLash.Ctx ctx, double[] c, double radius, double width, double[] rgb, double a, int segs, double[] normal) {
      if (ctx.pass == 1) {
         LashDraw.ring(ctx.vc, ctx.m, ctx.cam, c, radius, width, segs, normal, f(rgb[0]), f(rgb[1]), f(rgb[2]), f(a));
      }

   }

   static void streak(ClientBlackLash.Ctx ctx, double[][] pts, double[] rgb, double a) {
      int n = pts.length;
      if (n >= 2 && !(a <= 0.003D)) {
         double[] widths = new double[n];
         double[] glow = new double[n];
         double[] smoke = new double[n];

         for(int i = 0; i < n; ++i) {
            double s = (double)i / (double)(n - 1);
            widths[i] = 0.05D * (1.0D - 0.7D * s);
            glow[i] = a * (1.0D - s);
            smoke[i] = 0.4D * a * (1.0D - s);
         }

         softChain(ctx, pts, widths, 1, LashShape.SMOKE, smoke);
         addChain(ctx, pts, widths, 0, rgb, glow);
      }
   }

   static void decal(ClientBlackLash.Ctx ctx, double[] c, double size, double spin, double a) {
      if (ctx.pass == 2) {
         int lit = LevelRenderer.getLightColor(ctx.level, BlockPos.containing(c[0], c[1] + 0.2D, c[2]));
         LashDraw.decal(ctx.buffers, ctx.m, ctx.cam, c, size, spin, f(a), lit);
      }

   }

   static void draw(ClientBlackLash.Ctx ctx, double[][] given, double t, double[] rim, double alpha, double seed, double w0, double w1, double rimA, boolean noHook) {
      double[][] pts = ctx.self ? nearCut(ctx, given) : given;
      int n = pts.length;
      if (n >= 2 && !(alpha <= 0.003D)) {
         double[] widths = LashShape.widths(n, w0, w1, 1.0D);
         double[][] tipDir = new double[][]{pts[n - 1], LashMath.norm(LashMath.sub(pts[n - 1], pts[Math.max(0, n - 3)]))};
         if (ctx.pass == 0) {
            double[] halo = new double[n];
            double[] halA = new double[n];

            for(int i = 0; i < n; ++i) {
               halo[i] = widths[i] * 2.3D;
               halA[i] = 0.32D * alpha;
            }

            softChain(ctx, pts, halo, 1, LashShape.SMOKE, halA);
            softChain(ctx, pts, widths, 0, LashShape.BODY, LashShape.bodyAlpha(n, alpha));
            if (!noHook) {
               double[] tip = tipDir[0];
               double[] dir = tipDir[1];
               LashDraw.ribbon(ctx.vc, ctx.m, ctx.cam, LashMath.sub(tip, LashMath.mul(dir, 0.3D)), LashMath.add(tip, LashMath.mul(dir, 0.1D)), 0.3D, 3, f(LashShape.BODY[0]), f(LashShape.BODY[1]), f(LashShape.BODY[2]), f(0.95D * alpha), false);
            }

            for(int k = 0; k < 4; ++k) {
               double ph = LashShape.wispPhase(t, k, seed);
               double[] base = pts[LashShape.wispIndex(k, seed, n)];
               double[] wp = LashShape.wispAt(base, k, t, ph);
               softBill(ctx, wp, 0.1D + 0.12D * ph, (double)k + t * 0.02D, 2, LashShape.SMOKE[0], LashShape.SMOKE[1], LashShape.SMOKE[2], 0.5D * alpha * (1.0D - ph));
            }

         } else if (ctx.pass == 1) {
            double[] rimW = new double[n];

            for(int i = 0; i < n; ++i) {
               rimW[i] = widths[i] * 0.62D;
            }

            addChain(ctx, pts, rimW, 0, rim, LashShape.rimAlpha(n, alpha, rimA, t));
            if (!noHook) {
               double[] tip = tipDir[0];
               double[] dir = tipDir[1];
               LashDraw.ribbon(ctx.vc, ctx.m, ctx.cam, LashMath.sub(tip, LashMath.mul(dir, 0.28D)), LashMath.add(tip, LashMath.mul(dir, 0.08D)), 0.24D, 6, f(rim[0] * 0.6D), f(rim[1] * 0.6D), f(rim[2] * 0.6D), f(0.45D * alpha), true);
            }

            for(int k = 0; k < 3; ++k) {
               double q = LashShape.glintPhase(t, k, seed);
               double fi = q * (double)(n - 1);
               int i0 = Math.min(n - 2, (int)Math.floor(fi));
               double[] p = LashMath.lerp(pts[i0], pts[i0 + 1], LashMath.clamp01(fi - (double)i0));
               addBill(ctx, p, 0.08D, t * 0.3D + (double)k, 1, rim[0], rim[1], rim[2], 0.85D * alpha * Math.sin(Math.PI * q));
            }

         }
      }
   }

   static void crack(ClientBlackLash.Ctx ctx, double[] at, double age, double[] rim, double[] dir, boolean big) {
      double k = LashMath.clamp01(age / 7.0D);
      if (!(k >= 1.0D)) {
         double sz = big ? 1.35D : 0.95D;
         addBill(ctx, at, sz * (0.5D + 0.8D * k), age * 0.3D, 4, 1.0D, 1.0D, 1.0D, 1.0D - k);
         addBill(ctx, at, sz * 1.4D * (0.6D + k), 0.0D, 2, rim[0], rim[1], rim[2], 0.7D * (1.0D - k));
         if (dir != null && ctx.pass == 1) {
            double[] toEye = LashMath.sub(ctx.cam, at);
            double[] ay = LashMath.cross(dir, toEye);
            if (LashMath.len(ay) > 1.0E-4D) {
               ay = LashMath.norm(ay);
               double[] c = LashMath.add(at, LashMath.mul(dir, -0.3D + 1.1D * LashMath.outCubic(k)));
               LashDraw.plane(ctx.vc, ctx.m, ctx.cam, c, dir, ay, sz * (0.5D + 1.2D * LashMath.outCubic(k)), 0.0D, 5, 1.0F, 1.0F, 1.0F, f(0.85D * (1.0D - k)), true);
            }
         }

         addRing(ctx, at, sz * (0.3D + 1.6D * LashMath.outCubic(k)), 0.1D, new double[]{1.0D, 1.0D, 1.0D}, 0.5D * (1.0D - k), 20, LashMath.mul(ctx.fwd, -1.0D));

         for(int i = 0; i < 6; ++i) {
            addBill(ctx, LashShape.sparkAt(at, i, age), 0.05D, 0.0D, 3, rim[0], rim[1], rim[2], 1.0D - k);
         }

         if (ctx.pass == 2) {
            double g = GroundScan.below(ctx.level, at[0], at[1] + 0.5D, at[2], 8);
            if (!Double.isNaN(g) && at[1] - g < 0.8D) {
               decal(ctx, new double[]{at[0], g, at[2]}, 0.7D, (at[0] * 1.7D + at[2] * 2.3D) % (Math.PI * 2D), 0.9D * (1.0D - k));
            }
         }

      }
   }

   static double[] strike(ClientBlackLash.Ctx ctx, double[] r, double[] tgt, double lt, double[] rim, double[] cock, double seed, double fade, double w0, double w1, boolean big) {
      double t = ctx.t;
      double[][] pts = LashShape.strikePts(r, tgt, lt, cock, t);
      if (lt >= 7.0D) {
         crack(ctx, tgt, lt - 7.0D, rim, LashMath.norm(LashMath.sub(tgt, r)), big);
      }

      draw(ctx, pts, t, rim, fade, seed, w0, w1, 0.0D, false);
      return pts[pts.length - 1];
   }

   private static AbstractClientPlayer casterOf(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c) {
      Entity e = ctx.level.getEntity(c.caster);
      if (e instanceof AbstractClientPlayer p) {
         if (!p.isRemoved() && !p.isInvisible() && !p.isSpectator()) {
            if (p.distanceToSqr(ctx.cam[0], ctx.cam[1], ctx.cam[2]) > 5184.0D) {
               return null;
            }

            return p;
         }
      }

      return null;
   }

   private static void drawLine(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, AbstractClientPlayer p) {
      double t = ctx.t;
      double launch = t - (c.born + 1.5D);
      if (!(launch < 0.0D)) {
         int gripTicks = c.value >= 8 ? c.value / 8 : 4;
         int seed = c.value % 8;
         boolean miss = (c.flags & 2) != 0;
         boolean pull = (c.flags & 1) != 0;
         LivingEntity target = living(ctx, c.target);
         double[] a = hand(ctx, p, false);
         double[] b = target != null ? LashMath.add(at(target, ctx.pt), new double[]{0.0D, (double)target.getBbHeight() * (pull ? 0.6D : 0.55D), 0.0D}) : c.p1;
         double ext = LashMath.outCubic(launch / Math.max(1.0D, (double)gripTicks - 1.5D));
         double grip = c.born + (double)gripTicks;
         double taut = !miss && t >= grip ? LashMath.clamp01((t - grip) / 2.0D) : 0.0D;
         boolean ended = !Double.isNaN(c.ended);
         double fade = ended ? LashMath.clamp01(1.0D - (t - c.ended) / 4.0D) : 1.0D;
         double len = LashMath.dist(a, b);
         double[][] pts = LashShape.shape(a, b, segs(len), ext * (ended ? 1.0D - 0.7D * (1.0D - fade) : 1.0D), 0.22D * (1.0D - taut) + 0.03D, waves(len), t * 0.9D, 0.08D * (1.0D - taut), 0.06D * (1.0D - taut), 0.0D);
         double[] rim = rim(c);
         draw(ctx, pts, t, rim, fade, (double)seed, 0.0D, 0.0D, 0.0D, false);
         if (!miss && !(t < grip)) {
            if (target != null && taut < 1.0D) {
               addBill(ctx, b, 0.4D * (1.0D - taut), 0.0D, 2, rim[0], rim[1], rim[2], 0.6D * (1.0D - taut));
            }

            if (target == null) {
               addBill(ctx, b, 0.3D, 0.0D, 1, rim[0], rim[1], rim[2], 0.7D * fade);
               softBill(ctx, b, 0.35D, 0.0D, 2, LashShape.SMOKE[0], LashShape.SMOKE[1], LashShape.SMOKE[2], 0.5D * fade);
            }

         }
      }
   }

   private static void drawSwing(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, AbstractClientPlayer p) {
      double t = ctx.t;
      double age = t - c.born;
      double[] a = hand(ctx, p, false);
      double[] b = c.p1;
      double ext = LashMath.outCubic(age / 2.0D);
      boolean ended = !Double.isNaN(c.ended);
      double fade = ended ? LashMath.clamp01(1.0D - (t - c.ended) / 5.0D) : 1.0D;
      double latch = ended ? 1.0D : LashMath.clamp01((age - 2.0D) / 2.0D);
      double len = LashMath.dist(a, b);
      double[][] pts = LashShape.shape(a, b, segs(len), ext * (ended ? fade : 1.0D), ended ? 0.25D : 0.04D + 0.21D * (1.0D - latch), waves(len), t * 0.9D, 0.08D * (1.0D - latch), 0.06D * (1.0D - latch), ended ? 0.1D : 0.0D);
      double[] rim = rim(c);
      draw(ctx, pts, t, rim, fade, 3.0D, 0.0D, 0.0D, 0.0D, false);
      if (!ended) {
         addBill(ctx, b, 0.35D, t * 0.1D, 1, rim[0], rim[1], rim[2], 0.8D);
         softBill(ctx, b, 0.4D, 0.0D, 2, LashShape.SMOKE[0], LashShape.SMOKE[1], LashShape.SMOKE[2], 0.6D);
      }

   }

   private static void drawSling(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, AbstractClientPlayer p) {
      double t = ctx.t;
      double age = t - c.born;
      double ext = LashMath.outCubic(age / 3.0D);
      boolean ended = !Double.isNaN(c.ended);
      double fade = ended ? LashMath.clamp01(1.0D - (t - c.ended) / 6.0D) : 1.0D;
      double charge = ended ? 1.0D : LashMath.clamp01(age / 40.0D);
      double[] rim = rim(c);
      double[][] hands = new double[][]{hand(ctx, p, false), hand(ctx, p, true)};
      double[][] ends = new double[][]{c.p2, c.p1};

      for(int i = 0; i < 2; ++i) {
         double[] aa = hands[i];
         double[] bb = ends[i];
         if (ended) {
            aa = LashMath.lerp(aa, bb, LashMath.clamp01((t - c.ended) / 4.0D));
         }

         double stretch = LashMath.dist(aa, bb);
         double quiver = ended ? 0.3D : 0.03D + 0.05D * charge * (0.5D + 0.5D * Math.sin(t * 2.3D + (double)i * 1.7D));
         double[][] pts = LashShape.shape(aa, bb, 18, ext, quiver, 0.0D, t * (0.8D + 2.4D * charge) + (double)i, 0.0D, 0.0D, 0.0D);
         double thin = LashMath.clamp(5.0D / (stretch + 1.0D), 0.45D, 1.0D) * (1.0D - 0.25D * charge);
         draw(ctx, pts, t, rim, fade, (double)(i + 2), 0.12D * thin, 0.07D * thin, 0.6D + 0.4D * charge, true);
         addBill(ctx, bb, 0.35D, t * 0.1D + (double)i, 1, rim[0], rim[1], rim[2], 0.8D * fade);
         softBill(ctx, bb, 0.45D, (double)i, 2, LashShape.SMOKE[0], LashShape.SMOKE[1], LashShape.SMOKE[2], 0.6D * fade);
         if (!ended && charge > 0.0D) {
            addBill(ctx, aa, 0.2D + 0.3D * charge, t * 0.2D + (double)i, 2, rim[0], rim[1], rim[2], 0.45D * charge);
         }
      }

   }

   private static void drawCoil(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, AbstractClientPlayer p) {
      LivingEntity target = living(ctx, c.target);
      if (target != null) {
         double t = ctx.t;
         double age = t - c.born;
         boolean ended = !Double.isNaN(c.ended);
         if ((!ended || !(t - c.ended > 6.0D)) && !(age < 2.0D)) {
            double[] rim = rim(c);
            double[] handAt = c.slot < 2 ? hand(ctx, p, (c.flags & 4) != 0) : LashShape.holdRoot(at(p, ctx.pt), bodyYaw(p, ctx.pt), (double)p.getBbHeight() / 1.8D, c.slot);
            double[] tp = at(target, ctx.pt);
            double fly = LashMath.clamp01((age - 2.0D) / 2.5D);
            double wrap = LashMath.clamp01((age - 4.5D) / 2.5D);
            double fade = ended ? LashMath.clamp01(1.0D - (t - c.ended) / 6.0D) : 1.0D;
            double r = 0.42D * LashMath.clamp((double)target.getBbWidth() / 0.6D, 0.6D, 3.0D);
            double top = tp[1] + (double)target.getBbHeight() * 0.8D;
            double bot = tp[1] + 0.3D;
            double[] entry = new double[]{tp[0] + r, top, tp[2]};
            if (wrap <= 0.0D) {
               double[][] pts = LashShape.shape(handAt, LashMath.lerp(handAt, entry, fly), 22, 1.0D, 0.2D, 0.0D, t * 0.9D, 0.1D, 0.0D, 0.0D);
               draw(ctx, pts, t, rim, fade, (double)c.value, 0.0D, 0.0D, 0.0D, false);
            } else {
               double[][] lead = LashShape.shape(handAt, entry, 16, 1.0D, 0.05D + 0.03D * Math.sin(t * 0.3D), 0.0D, t * 0.6D, 0.0D, 0.0D, 0.08D);
               double[][] coil = LashShape.helix(tp, top, bot, 2.4D, r, 0.0D, wrap, 30, t * 0.05D);
               draw(ctx, LashShape.concat(lead, coil), t, rim, fade, (double)c.value, 0.14D, 0.08D, 0.0D, false);
            }
         }
      }
   }

   private static void drawWard(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, AbstractClientPlayer p) {
      double t = ctx.t;
      double age = t - c.born;
      double life = (double)c.value;
      double fade = Math.min(1.0D, Math.min(age / 4.0D, (life - age) / 8.0D));
      double left = life - age;
      if (!Double.isNaN(c.ended)) {
         fade = Math.min(fade, 1.0D - (t - c.ended) / 8.0D);
         left = Math.min(left, 6.0D - (t - c.ended));
      }

      if (!(fade <= 0.0D)) {
         double[] rim = rim(c);
         double[] pp = at(p, ctx.pt);
         double sc = (double)p.getBbHeight() / 1.8D;
         double yaw = bodyYaw(p, ctx.pt);
         double reach = LashShape.wardReach(age, left);

         for(int k = 0; k < 6; ++k) {
            draw(ctx, LashShape.wardPts(pp, sc, t, k, reach), t, rim, fade, (double)k, 0.13D, 0.04D, 0.0D, k % 2 == 1);
            streak(ctx, LashShape.wardTrail(pp, sc, t, k, reach), rim, 0.35D * fade * reach);
         }

         for(int side = -1; side <= 1; side += 2) {
            draw(ctx, LashShape.wardHandPts(hand(ctx, p, side < 0), pp, yaw, sc, t, side, reach), t, rim, fade, (double)(side + 7), 0.11D, 0.04D, 0.0D, false);
         }

         addRing(ctx, new double[]{pp[0], pp[1] + 0.05D, pp[2]}, 3.0D, 0.12D, rim, 0.35D * fade, 0, (double[])null);

         for(ClientBlackLash.Cast l : SHOTS) {
            if (l.kind == 11 && l.caster == c.caster) {
               double since = t - l.born;
               if (!(since > 12.0D) && !(since < 0.0D)) {
                  LivingEntity target = living(ctx, l.target);
                  double lt = since + 7.0D - 2.0D;
                  double[] tgt = l.locked != null ? l.locked : (target != null ? LashMath.add(at(target, ctx.pt), new double[]{0.0D, (double)target.getBbHeight() * 0.55D, 0.0D}) : l.p1);
                  if (lt >= 7.0D && l.locked == null) {
                     l.locked = tgt;
                  }

                  strike(ctx, LashShape.wardStrikeRoot(pp, sc, tgt), tgt, lt, rim, new double[]{0.0D, 1.0D, 0.0D}, (double)l.value, LashMath.clamp01((12.0D - since) / 4.0D), 0.0D, 0.0D, false);
               }
            }
         }

      }
   }

   private static void drawStorm(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, AbstractClientPlayer p) {
      double t = ctx.t;
      double age = t - c.born;
      double life = (double)c.value;
      double end = life - 24.0D;
      double fade = age > end ? LashMath.clamp01(1.0D - (age - end) / 14.0D) : 1.0D;
      if (!Double.isNaN(c.ended)) {
         fade = Math.min(fade, LashMath.clamp01(1.0D - (t - c.ended) / 14.0D));
      }

      if (!(fade <= 0.0D)) {
         double ext = LashMath.outCubic(age / 4.0D);
         double[] rim = rim(c);
         double[] pp = at(p, ctx.pt);
         double yaw = bodyYaw(p, ctx.pt);
         double sc = (double)p.getBbHeight() / 1.8D;
         boolean finale = !Double.isNaN(c.finale) && t - c.finale >= 0.0D;

         for(int i = 0; i < 8; ++i) {
            double[] root = LashShape.stormRoot(pp, yaw, sc, i);
            double[] rest = LashShape.stormRest(pp, yaw, sc, i, t);
            if (finale) {
               strike(ctx, root, LashShape.stormFinale(pp, i), t - c.finale, rim, new double[]{0.0D, 1.0D, 0.0D}, (double)i, fade, 0.15D, 0.05D, true);
            } else {
               ClientBlackLash.Cast last = null;

               for(int s = SHOTS.size() - 1; s >= 0; --s) {
                  ClientBlackLash.Cast st = (ClientBlackLash.Cast)SHOTS.get(s);
                  if (st.kind == 13 && st.caster == c.caster && st.slot == i && st.born <= t) {
                     last = st;
                     break;
                  }
               }

               double since = last != null ? t - last.born : 99.0D;
               if (since < 13.0D) {
                  LivingEntity target = living(ctx, last.target);
                  double[] tgt = last.locked != null ? last.locked : (target != null ? LashMath.add(at(target, ctx.pt), new double[]{0.0D, (double)target.getBbHeight() * 0.62D, 0.0D}) : last.p1);
                  if (since >= 7.0D && last.locked == null) {
                     last.locked = tgt;
                  }

                  double[] cock = LashMath.norm(LashMath.sub(rest, root));
                  strike(ctx, root, tgt, since, rim, cock, (double)i, fade, 0.14D, 0.04D, false);
               } else {
                  double[][] pts = LashShape.shape(root, rest, 22, ext, 0.2D, 0.0D, t * 0.8D + (double)i, (double)(i % 2 != 0 ? 1 : -1) * 0.15D, 0.18D, 0.0D);
                  double w = 1.0D;
                  if (last != null && last.locked != null && since < 17.0D) {
                     w = (since - 13.0D) / 4.0D;
                     pts = settle(LashShape.strikePts(root, last.locked, 13.0D, LashMath.norm(LashMath.sub(rest, root)), t), pts, w);
                  }

                  draw(ctx, pts, t, rim, fade, (double)i, 0.14D, 0.04D + 0.01D * LashMath.smooth01(w), 0.0D, false);
               }
            }
         }

         if (!Double.isNaN(c.finale) && t - c.finale > 7.0D) {
            if (c.floor == null) {
               double g = GroundScan.below(ctx.level, pp[0], pp[1] + 0.5D, pp[2], 8);
               c.floor = new double[]{pp[0], Double.isNaN(g) ? pp[1] : g, pp[2], Double.isNaN(g) ? 0.0D : 1.0D};
            }

            double[] fl = new double[]{c.floor[0], c.floor[1], c.floor[2]};
            double k = LashMath.clamp01((t - c.finale - 7.0D) / 14.0D);
            addRing(ctx, new double[]{fl[0], fl[1] + 0.06D, fl[2]}, 1.0D + 6.0D * LashMath.outCubic(k), 0.5D, rim, 0.85D * (1.0D - k), 0, (double[])null);
            if (c.floor[3] > 0.0D) {
               decal(ctx, fl, 3.4D, 0.2D, 0.9D * (1.0D - k));
            }
         }

         if (!ctx.self) {
            addBill(ctx, LashMath.add(pp, new double[]{0.0D, 1.3D, 0.0D}), 1.6D, 0.0D, 2, rim[0], rim[1], rim[2], 0.18D * fade);
         }

      }
   }

   private static ClientBlackLash.Cast lastStrike(int caster, int slot, double t) {
      for(int s = SHOTS.size() - 1; s >= 0; --s) {
         ClientBlackLash.Cast st = (ClientBlackLash.Cast)SHOTS.get(s);
         if (st.kind == 25 && st.caster == caster && st.slot == slot && st.born <= t) {
            return t - st.born < (double)LashMath.frenzyStroke() ? st : null;
         }
      }

      return null;
   }

   private static void frenzyFloor(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, double yaw) {
      if (c.floor == null || !(LashMath.dist(new double[]{c.floor[0], c.floor[1], c.floor[2]}, c.p1) < 0.5D)) {
         double[] fl = new double[15];
         System.arraycopy(c.p1, 0, fl, 0, 3);

         for(int k = 0; k < 3; ++k) {
            double[] spot = k == 0 ? c.p1 : LashShape.frenzyLegSpot(c.p1, yaw, k - 1);
            double g = GroundScan.below(ctx.level, spot[0], c.p1[1] + 1.0D, spot[2], 6);
            fl[3 + k * 4] = spot[0];
            fl[4 + k * 4] = Double.isNaN(g) ? c.p1[1] - 0.8D : g;
            fl[5 + k * 4] = spot[2];
            fl[6 + k * 4] = Double.isNaN(g) ? 0.0D : 1.0D;
         }

         c.floor = fl;
      }
   }

   private static void drawFrenzyLegs(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, double[] pp, double yaw, double sc, double grow, double rimA) {
      if (c.p1 != null && c.p2 != null && !(c.p2[1] - c.p1[1] < 0.25D)) {
         double t = ctx.t;
         double[] rage = LashShape.RAGE;

         for(int k = 0; k < 2; ++k) {
            int at = 7 + k * 4;
            double[] foot = new double[]{c.floor[at], c.floor[at + 1], c.floor[at + 2]};
            double[] root = LashShape.frenzyLegRoot(pp, yaw, sc, k);
            draw(ctx, LashShape.frenzyLeg(root, foot, t, k, grow), t, rage, grow, (double)(k + 8), 0.2D, 0.11D, rimA, true);
            if (c.floor[at + 3] > 0.0D && grow >= 1.0D) {
               decal(ctx, foot, 0.9D, (double)k * 1.7D + 0.4D, 0.85D * grow);
            }
         }

      }
   }

   private static void drawFrenzy(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, AbstractClientPlayer p) {
      double t = ctx.t;
      double age = t - c.born;
      double grow = LashShape.frenzyGrow(age, (double)c.value);
      if (!Double.isNaN(c.ended)) {
         grow = Math.min(grow, LashMath.clamp01(1.0D - (t - c.ended) / 8.0D));
      }

      if (!(grow <= 0.0D)) {
         double[] rage = LashShape.RAGE;
         double[] pp = at(p, ctx.pt);
         double yaw = bodyYaw(p, ctx.pt);
         double sc = (double)p.getBbHeight() / 1.8D;
         double pulse = LashShape.frenzyPulse(age);
         double rimA = 0.75D + 0.25D * pulse;
         boolean live = Double.isNaN(c.ended);
         frenzyFloor(ctx, c, yaw);
         drawFrenzyLegs(ctx, c, pp, yaw, sc, grow, rimA);

         for(int i = 0; i < 8; ++i) {
            double[] root = i < 2 ? hand(ctx, p, i == 1) : LashShape.frenzyRoot(pp, yaw, sc, i);
            double[] rest = LashShape.frenzyRest(pp, yaw, sc, i, t);
            double[][] idle = LashShape.frenzyIdle(root, rest, t, i, grow);
            double[][] pts = idle;
            ClientBlackLash.Cast last = live ? lastStrike(c.caster, i, t) : null;
            if (last != null) {
               double since = t - last.born;
               LivingEntity target = living(ctx, last.target);
               double[] tgt = last.locked != null ? last.locked : (target != null ? LashMath.add(at(target, ctx.pt), new double[]{0.0D, (double)target.getBbHeight() * 0.62D, 0.0D}) : last.p1);
               if (since >= 15.0D && last.locked == null) {
                  last.locked = tgt;
               }

               pts = LashShape.frenzyStroke(root, tgt, since, LashMath.norm(LashMath.sub(rest, root)), t, idle);
               if (since >= 7.0D) {
                  crack(ctx, tgt, since - 7.0D, rage, LashMath.norm(LashMath.sub(tgt, root)), true);
               }
            }

            draw(ctx, pts, t, rage, grow, (double)i, 0.24D, 0.07D, rimA, false);
         }

         double[] chest = new double[]{pp[0], pp[1] + 1.05D * sc, pp[2]};
         if (!ctx.self) {
            addBill(ctx, chest, 1.7D + 1.3D * pulse, 0.0D, 2, rage[0], rage[1] * 0.5D, rage[2] * 0.5D, (0.25D + 0.35D * pulse) * grow);
         }

         if (pulse > 0.0D) {
            addRing(ctx, chest, 0.4D + 2.2D * (1.0D - pulse), 0.16D, rage, 0.7D * pulse, 24, LashMath.mul(ctx.fwd, -1.0D));
         }

         if (age < 10.0D && c.floor[6] > 0.0D) {
            double k = age / 10.0D;
            double[] fl = new double[]{c.floor[3], c.floor[4], c.floor[5]};
            addRing(ctx, new double[]{fl[0], fl[1] + 0.06D, fl[2]}, 0.5D + 4.0D * LashMath.outCubic(k), 0.4D, rage, 0.85D * (1.0D - k), 0, (double[])null);
            decal(ctx, fl, 2.8D, 0.9D, 0.9D * (1.0D - k));
         }

      }
   }

   private static void drawStrain(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, AbstractClientPlayer p) {
      if (!firstSelf(ctx, p) && Double.isNaN(c.ended)) {
         double k = (double)c.value / 100.0D;
         if (!(k < 0.5D)) {
            double t = ctx.t;
            double[] h = hand(ctx, p, false);

            for(int i = 0; i < 2; ++i) {
               double[] tip = LashShape.passiveTip(h, t, i, k);
               double[][] pts = LashShape.shape(h, tip, 8, 1.0D, 0.08D, 0.0D, t, 0.0D, 0.0D, 0.0D);
               draw(ctx, pts, t, LashShape.RIM, (k - 0.5D) * 2.0D * 0.8D, (double)i, 0.05D, 0.02D, 0.0D, true);
            }

         }
      }
   }

   private static void drawCast(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c) {
      AbstractClientPlayer p = casterOf(ctx, c);
      if (p != null) {
         ctx.self = firstSelf(ctx, p);
         switch (c.kind) {
            case 0:
               drawLine(ctx, c, p);
            case 1:
            case 4:
            case 5:
            case 7:
            case 8:
            case 9:
            case 11:
            case 13:
            case 14:
            case 17:
            case 20:
            case 21:
            default:
               break;
            case 2:
               drawSwing(ctx, c, p);
               break;
            case 3:
               drawSling(ctx, c, p);
               break;
            case 6:
               drawCoil(ctx, c, p);
               break;
            case 10:
               drawWard(ctx, c, p);
               break;
            case 12:
               drawStorm(ctx, c, p);
               break;
            case 15:
               drawFrenzy(ctx, c, p);
               break;
            case 16:
               drawStrain(ctx, c, p);
               break;
            case 18:
               drawCatch(ctx, c, p);
               break;
            case 19:
               drawHold(ctx, c, p);
               break;
            case 22:
               drawWind(ctx, c, p);
         }

      }
   }

   private static void drawCatch(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, AbstractClientPlayer p) {
      LivingEntity e = living(ctx, c.target);
      if (e != null) {
         double t = ctx.t;
         double launch = t - (c.born + 1.5D);
         if (!(launch < 0.0D)) {
            boolean grip = t >= c.born + 4.0D;
            double fade = !Double.isNaN(c.ended) ? LashMath.clamp01(1.0D - (t - c.ended) / 6.0D) : 1.0D;
            double[][] pts = LashShape.catchPts(hand(ctx, p, false), at(e, ctx.pt), t, launch, grip);
            draw(ctx, pts, t, rim(c), fade, 5.0D, 0.0D, 0.0D, 0.0D, grip);
         }
      }
   }

   private static void drawHold(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, AbstractClientPlayer p) {
      if (Double.isNaN(c.ended)) {
         double t = ctx.t;
         int left = (int)Math.max(0.0D, 200.0D - (t - c.born));
         int n = LashMath.holdTogetherCount(left);
         double[] pp = at(p, ctx.pt);
         double yaw = bodyYaw(p, ctx.pt);

         for(int i = 0; i < n; ++i) {
            draw(ctx, LashShape.holdTogether(pp, yaw, t, i), t, LashShape.RAGE, LashShape.holdTogetherAlpha(t, i), (double)i, 0.05D, 0.04D, 0.9D, true);
         }

      }
   }

   private static ClientBlackLash.Cast busyLash(int caster, int slot, double t, double within) {
      for(int s = SHOTS.size() - 1; s >= 0; --s) {
         ClientBlackLash.Cast st = (ClientBlackLash.Cast)SHOTS.get(s);
         if (st.kind == 23 && st.caster == caster && st.slot == slot && st.born <= t && t - st.born < within) {
            return st;
         }
      }

      return null;
   }

   private static void drawWind(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, AbstractClientPlayer p) {
      double t = ctx.t;
      double age = t - c.born;
      double a = Math.min(1.0D, age / 10.0D);
      if (!Double.isNaN(c.ended)) {
         a *= LashMath.clamp01(1.0D - (t - c.ended) / 10.0D);
      }

      if (!(a <= 0.0D)) {
         double[] pp = at(p, ctx.pt);
         double[] rage = LashShape.RAGE;

         for(int i = 0; i < 5; ++i) {
            double[] root = LashShape.windRoot(pp, i);
            ClientBlackLash.Cast lash = Double.isNaN(c.ended) ? busyLash(c.caster, i, t, 12.0D) : null;
            if (lash != null) {
               LivingEntity target = living(ctx, lash.target);
               double lt = t - lash.born;
               double[] tgt = lash.locked != null ? lash.locked : (target != null ? LashMath.add(at(target, ctx.pt), new double[]{0.0D, (double)target.getBbHeight() * 0.62D, 0.0D}) : lash.p1);
               if (lt >= 7.0D && lash.locked == null) {
                  lash.locked = tgt;
               }

               strike(ctx, root, tgt, lt, rage, new double[]{0.0D, 1.0D, 0.0D}, (double)i, a, 0.11D, 0.035D, false);
            } else {
               double[][] idle = LashShape.windPts(pp, t, i);
               ClientBlackLash.Cast late = Double.isNaN(c.ended) ? busyLash(c.caster, i, t, 16.0D) : null;
               double k = 1.0D;
               if (late != null && late.locked != null) {
                  double w = (t - late.born - 12.0D) / 4.0D;
                  k = LashMath.smooth01(w);
                  idle = settle(LashShape.strikePts(root, late.locked, 12.0D, new double[]{0.0D, 1.0D, 0.0D}, t), idle, w);
               }

               draw(ctx, idle, t, rage, (1.0D - 0.15D * k) * a, (double)i, 0.11D - 0.01D * k, 0.035D - 0.005D * k, 0.0D, false);
            }
         }

         addRing(ctx, new double[]{pp[0], pp[1] + 0.05D, pp[2]}, 5.0D, 0.1D, rage, 0.25D * a, 0, (double[])null);
         if (!ctx.self) {
            addBill(ctx, LashMath.add(pp, new double[]{0.0D, 1.1D, 0.0D}), 1.4D, 0.0D, 2, 1.0D, 0.3D, 0.45D, 0.16D * a);
         }

      }
   }

   public static double[] swingAnchor(int caster) {
      ClientBlackLash.Cast c = (ClientBlackLash.Cast)CASTS.get(key(caster, 2, 0));
      return c != null && Double.isNaN(c.ended) && c.p1 != null ? c.p1 : null;
   }

   static Object swingCast(int caster) {
      ClientBlackLash.Cast c = (ClientBlackLash.Cast)CASTS.get(key(caster, 2, 0));
      return c != null && Double.isNaN(c.ended) ? c : null;
   }

   public static double veins(int caster, double t) {
      ClientBlackLash.Cast w = (ClientBlackLash.Cast)CASTS.get(key(caster, 22, 0));
      if (w != null && Double.isNaN(w.ended)) {
         return Math.min(1.0D, 0.72D + 0.05D * Math.max(0.0D, t - w.born));
      } else {
         for(int s = SHOTS.size() - 1; s >= 0; --s) {
            ClientBlackLash.Cast st = (ClientBlackLash.Cast)SHOTS.get(s);
            if (st.caster == caster) {
               if (st.kind == 24) {
                  return Math.max(0.0D, 1.0D - 0.04D * Math.max(0.0D, t - st.born));
               }

               if (st.kind == 21) {
                  return Math.min(1.0D, 0.06D * Math.max(0.0D, Math.min(12.0D, t - st.born)));
               }
            }
         }

         return w != null ? Math.max(0.0D, 1.0D - 0.04D * Math.max(0.0D, t - w.ended)) : 0.0D;
      }
   }

   private static void drawPound(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c, double age, double[] rim) {
      double reach = Math.max(1.0D, (double)c.value / 10.0D);
      double fade = LashMath.clamp01(1.0D - age / 28.0D);
      double k = LashMath.clamp01(age / 11.0D);
      double ring = LashShape.poundRing(reach, age);
      double[] f = c.p1;
      if (c.floor == null && ((c.flags & 32) == 0 || age >= 3.0D)) {
         double[] fl = new double[44];

         for(int i = 0; i <= 10; ++i) {
            double[] cr = i == 0 ? new double[]{f[0], f[2]} : LashShape.poundCrack(f, reach, i - 1);
            double g = GroundScan.below(ctx.level, cr[0], f[1] + 1.5D, cr[1], 5);
            fl[i * 4] = cr[0];
            fl[i * 4 + 1] = Double.isNaN(g) ? f[1] : g;
            fl[i * 4 + 2] = cr[1];
            fl[i * 4 + 3] = Double.isNaN(g) ? 0.0D : 1.0D;
         }

         c.floor = fl;
      }

      addRing(ctx, new double[]{f[0], f[1] + 0.06D, f[2]}, ring, 0.6D, rim, 0.9D * (1.0D - k), 0, (double[])null);
      addRing(ctx, new double[]{f[0], f[1] + 0.08D, f[2]}, ring * 0.72D, 0.25D, new double[]{1.0D, 1.0D, 1.0D}, 0.55D * (1.0D - k), 0, (double[])null);
      if (c.floor != null) {
         if (c.floor[3] > 0.0D) {
            decal(ctx, new double[]{c.floor[0], c.floor[1], c.floor[2]}, 2.0D + 0.25D * reach, 0.6D, 0.95D * fade);
         }

         for(int i = 0; i < 10; ++i) {
            double[] cr = LashShape.poundCrack(f, reach, i);
            int at = (i + 1) * 4;
            if (c.floor[at + 3] > 0.0D && ring >= cr[2]) {
               decal(ctx, new double[]{c.floor[at], c.floor[at + 1], c.floor[at + 2]}, cr[3], cr[4], 0.85D * fade);
            }
         }
      }

      if (!ctx.self || ring > 1.5D) {
         for(int i = 0; i < 14; ++i) {
            double ang = (double)i * 0.4487989505128276D + 0.2D;
            softBill(ctx, new double[]{f[0] + Math.cos(ang) * ring * 0.92D, f[1] + 0.25D + 0.5D * k, f[2] + Math.sin(ang) * ring * 0.92D}, 0.45D + 0.7D * k, ang, 4, 0.6D, 0.56D, 0.5D, 0.55D * (1.0D - k));
         }
      }

      if (age < 6.0D && !ctx.self) {
         addBill(ctx, new double[]{f[0], f[1] + 0.8D, f[2]}, 2.6D + 0.2D * reach, 0.0D, 2, rim[0], rim[1], rim[2], 0.8D * (1.0D - age / 6.0D));
      }

   }

   private static void drawShot(ClientBlackLash.Ctx ctx, ClientBlackLash.Cast c) {
      if (c.kind != 11 && c.kind != 13 && c.kind != 23 && c.kind != 25) {
         double t = ctx.t;
         double age = t - c.born;
         if (!(age < 0.0D)) {
            ctx.self = selfCast(ctx, c.caster);
            double[] rim = rim(c);
            switch (c.kind) {
               case 1:
                  double k_0 = LashMath.clamp01(age / 14.0D);
                  addBill(ctx, c.p1, 0.4D + 1.2D * k_0, age * 0.2D, 4, rim[0], rim[1], rim[2], 0.8D * (1.0D - k_0));

                  for(int i = 0; i < 6; ++i) {
                     softBill(ctx, LashMath.add(c.p1, new double[]{Math.cos((double)i) * k_0 * 0.8D, -k_0 * 0.4D, Math.sin((double)i) * k_0 * 0.8D}), 0.2D + 0.3D * k_0, (double)i, 2, LashShape.SMOKE[0], LashShape.SMOKE[1], LashShape.SMOKE[2], 0.6D * (1.0D - k_0));
                  }
               case 2:
               case 3:
               case 6:
               case 10:
               case 11:
               case 12:
               case 13:
               case 14:
               case 15:
               case 16:
               case 17:
               case 18:
               case 19:
               case 22:
               case 23:
               case 25:
               default:
                  break;
               case 4:
                  double k_1 = LashMath.clamp01(age / 12.0D);
                  addBill(ctx, c.p1, 0.4D + 0.9D * k_1, age * 0.2D, 4, rim[0], rim[1], rim[2], 0.8D * (1.0D - k_1));

                  for(int i = 0; i < 8; ++i) {
                     double ang_0 = (double)i * 0.785D + 0.3D;
                     softBill(ctx, LashMath.add(c.p1, new double[]{Math.cos(ang_0) * (0.3D + 1.4D * k_1), -0.6D + 0.3D * Math.sin(ang_0 * 2.0D), Math.sin(ang_0) * (0.3D + 1.4D * k_1)}), 0.25D + 0.35D * k_1, ang_0, 4, 0.62D, 0.58D, 0.52D, 0.55D * (1.0D - k_1));
                  }
                  break;
               case 5:
                  double k_2 = LashMath.clamp01(age / 18.0D);
                  double[] f = c.p1;
                  addRing(ctx, new double[]{f[0], f[1] + 0.06D, f[2]}, 0.5D + 2.5D * LashMath.outCubic(k_2), 0.3D, rim, 0.7D * (1.0D - k_2), 0, (double[])null);

                  for(int i = 0; i < 8; ++i) {
                     double ang = (double)i * 0.785D;
                     softBill(ctx, new double[]{f[0] + Math.cos(ang) * (0.4D + 1.6D * k_2), f[1] + 0.2D + 0.3D * k_2, f[2] + Math.sin(ang) * (0.4D + 1.6D * k_2)}, 0.3D + 0.4D * k_2, ang, 4, 0.6D, 0.56D, 0.5D, 0.5D * (1.0D - k_2));
                  }

                  decal(ctx, f, (c.flags & 32) != 0 ? 1.6D : 1.2D, 0.4D, 0.8D * (1.0D - k_2));
                  break;
               case 7:
                  LivingEntity target_0 = living(ctx, c.target);
                  if (target_0 != null) {
                     double[] mid = LashMath.mid(at(target_0, ctx.pt), (double)target_0.getBbHeight());
                     double k_3 = LashMath.clamp01(age / 24.0D);

                     for(int i = 0; i < 4; ++i) {
                        softBill(ctx, LashMath.add(mid, new double[]{Math.sin((double)(i * 2) + age) * 0.2D, -0.1D * (double)i, Math.cos((double)(i * 3)) * 0.2D}), 0.25D + 0.1D * (double)i, (double)i, 2, LashShape.SMOKE[0], LashShape.SMOKE[1], LashShape.SMOKE[2], 0.5D * (1.0D - k_3));
                     }
                  }
                  break;
               case 8:
                  AbstractClientPlayer p_0 = casterOf(ctx, c);
                  LivingEntity target_1 = living(ctx, c.target);
                  if (p_0 != null && target_1 != null && !(age > 6.0D)) {
                     double[] h = hand(ctx, p_0, (c.flags & 4) != 0);
                     double[] to = LashMath.add(at(target_1, ctx.pt), new double[]{0.0D, (double)target_1.getBbHeight() * 0.6D, 0.0D});
                     draw(ctx, LashShape.shape(h, to, 14, 1.0D, 0.06D, 0.0D, t, 0.0D, 0.0D, 0.0D), t, rim, 1.0D - age / 6.0D, (double)c.value, 0.0D, 0.0D, 0.0D, false);
                  }
                  break;
               case 9:
                  AbstractClientPlayer p_1 = casterOf(ctx, c);
                  if (p_1 != null) {
                     boolean left = (c.flags & 4) != 0;
                     double[] r = hand(ctx, p_1, left);
                     LivingEntity target = living(ctx, c.target);
                     double[] tgt = c.locked != null ? c.locked : (target != null ? LashMath.add(at(target, ctx.pt), new double[]{0.0D, (double)target.getBbHeight() * 0.62D, 0.0D}) : c.p1);
                     if (age >= 7.0D && c.locked == null) {
                        c.locked = tgt;
                     }

                     double[] cock = (c.flags & 8) != 0 ? new double[]{0.0D, 1.0D, 0.0D} : LashMath.add(LashMath.mul(LashMath.right(bodyYaw(p_1, ctx.pt)), left ? -1.0D : 1.0D), new double[]{0.0D, 0.35D, 0.0D});
                     double fade = LashMath.clamp01(((double)shotLife(c.kind) - age) / 6.0D);
                     strike(ctx, r, tgt, age, rim, cock, (double)c.value, fade, 0.13D, 0.035D, (c.flags & 32) != 0);
                  }
                  break;
               case 20:
                  if (casterOf(ctx, c) != null) {
                     for(int i = 0; i < 6; ++i) {
                        double[] pf = LashShape.fallPuff(c.p1, age, i);
                        softBill(ctx, new double[]{pf[0], pf[1], pf[2]}, pf[3], (double)i, 4, 0.5D, 0.48D, 0.46D, pf[4]);
                     }
                  }
                  break;
               case 21:
                  AbstractClientPlayer p_2 = casterOf(ctx, c);
                  if (p_2 != null) {
                     double[] pp = at(p_2, ctx.pt);
                     double[] rage = LashShape.RAGE;
                     if (age < 5.0D) {
                        for(int i = 0; i < 10; ++i) {
                           draw(ctx, LashShape.riseIn(pp, t, age, i), t, rage, age / 5.0D, (double)i, 0.08D, 0.05D, 0.0D, true);
                        }
                     } else {
                        double k = LashShape.riseK(age);

                        for(int i = 0; i < 12; ++i) {
                           draw(ctx, LashShape.riseOut(pp, t, age, i), t, rage, 1.0D - k, (double)i, 0.0D, 0.0D, 0.0D, false);
                        }

                        addRing(ctx, new double[]{pp[0], pp[1] + 0.06D, pp[2]}, LashShape.riseReach(age), 0.4D, rage, 0.8D * (1.0D - k), 0, (double[])null);
                        decal(ctx, pp, 2.6D, 0.7D, 0.9D * (1.0D - k));
                        if (!ctx.self) {
                           addBill(ctx, LashMath.add(pp, new double[]{0.0D, 1.1D, 0.0D}), 2.5D * (1.0D - k * 0.5D), 0.0D, 2, 1.0D, 0.3D, 0.45D, 0.6D * (1.0D - k));
                        }
                     }
                  }
                  break;
               case 24:
                  AbstractClientPlayer p = casterOf(ctx, c);
                  if (p != null) {
                     double[] pp = at(p, ctx.pt);

                     for(int i = 0; i < 8; ++i) {
                        double[] w = LashShape.spentWisp(pp, age, i);
                        if (w != null) {
                           softBill(ctx, new double[]{w[0], w[1], w[2]}, w[3], (double)i, 2, LashShape.SMOKE[0], LashShape.SMOKE[1], LashShape.SMOKE[2], w[4]);
                        }
                     }
                  }
                  break;
               case 26:
                  drawPound(ctx, c, age, rim);
            }

         }
      }
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         Minecraft mc = Minecraft.getInstance();
         ClientLevel level = mc.level;
         if (level == null) {
            CASTS.clear();
            SHOTS.clear();
         } else {
            float pt = event.getPartialTick();
            double t = (double)((float)level.getGameTime() + pt);
            prune(t);
            if (!CASTS.isEmpty() || !SHOTS.isEmpty()) {
               Camera camera = event.getCamera();
               Vector3f l = camera.getLeftVector();
               Vector3f u = camera.getUpVector();
               Vector3f fw = camera.getLookVector();
               ClientBlackLash.Ctx ctx = new ClientBlackLash.Ctx();
               ctx.cam = a(camera.getPosition());
               ctx.right = new double[]{(double)(-l.x()), (double)(-l.y()), (double)(-l.z())};
               ctx.up = new double[]{(double)u.x(), (double)u.y(), (double)u.z()};
               ctx.fwd = new double[]{(double)fw.x(), (double)fw.y(), (double)fw.z()};
               ctx.m = event.getPoseStack().last().pose();
               LashDraw.normals(event.getPoseStack().last().normal());
               ctx.buffers = mc.renderBuffers().bufferSource();
               ctx.level = level;
               ctx.mc = mc;
               ctx.t = t;
               ctx.pt = pt;
               List<ClientBlackLash.Cast> casts = new ArrayList<>(CASTS.values());
               MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

               for(int pass : PASSES) {
                  ctx.pass = pass;
                  ctx.vc = pass == 0 ? buffers.getBuffer(LashDraw.soft()) : (pass == 1 ? buffers.getBuffer(LashDraw.add()) : null);

                  try {
                     for(ClientBlackLash.Cast c_0 : casts) {
                        drawCast(ctx, c_0);
                     }

                     for(ClientBlackLash.Cast c : SHOTS) {
                        drawShot(ctx, c);
                     }
                  } finally {
                     if (pass == 0) {
                        buffers.endBatch(LashDraw.soft());
                     } else if (pass == 1) {
                        buffers.endBatch(LashDraw.add());
                     } else {
                        buffers.endBatch(LashDraw.ground());
                     }

                  }
               }

            }
         }
      }
   }

   static final class Cast {
      int caster;
      int kind;
      int slot;
      int target;
      int value;
      int flags;
      double[] p1;
      double[] p2;
      double born;
      double seen;
      double ended = Double.NaN;
      double finale = Double.NaN;
      double[] locked;
      double[] floor;
   }

   static final class Ctx {
      int pass;
      VertexConsumer vc;
      MultiBufferSource buffers;
      Matrix4f m;
      double[] cam;
      double[] right;
      double[] up;
      double[] fwd;
      ClientLevel level;
      Minecraft mc;
      double t;
      float pt;
      boolean self;
   }
}
