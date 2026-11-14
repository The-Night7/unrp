package com.myriam.unrp.uo;

import com.myriam.unrp.anim.MoveClips;

import com.myriam.unrp.anim.MoveAnimClient;
import com.myriam.unrp.anim.MoveClipIds;
import com.myriam.unrp.pass.UnrpPowerLayers;


import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
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
public final class ClientDaystar {
   private static final double SEEN = 64.0D;
   public static final double NOON_LIFE = 44.0D;
   public static final double COLUMN_TOP = 44.0D;
   public static final int FEET_ROWS = 4;
   public static final double FEET_FADE = 2.0D;
   private static final int MOST_CLOCKS = 64;
   private static final Map<Integer, Double> NOON_AT = new HashMap();
   private static final Map<Integer, BladeTrail.Strokes> STROKES = new HashMap();
   private static final int[] TRAILS = new int[BladeTrail.NAMES.length];
   private static int draw = -2;
   private static int noon = -2;

   private ClientDaystar() {
   }

   private static void ids() {
      if (draw == -2) {
         for(int k = 0; k < TRAILS.length; ++k) {
            TRAILS[k] = MoveClipIds.id(BladeTrail.NAMES[k]);
         }

         draw = MoveClipIds.id("ds_draw");
         noon = MoveClipIds.id("ds_noon");
      }

   }

   public static void clearAll() {
      NOON_AT.clear();
      STROKES.clear();
   }

   private static BladeTrail.Strokes strokes(int id) {
      BladeTrail.Strokes st = (BladeTrail.Strokes)STROKES.get(id);
      if (st == null) {
         if (STROKES.size() >= 64) {
            STROKES.clear();
         }

         st = new BladeTrail.Strokes();
         STROKES.put(id, st);
      }

      return st;
   }

   public static double open(AbstractClientPlayer p) {
      Level level = p.level();
      BlockPos head = BlockPos.containing(p.getX(), p.getEyeY(), p.getZ());
      boolean sky = level.canSeeSky(head);
      boolean wet = sky && level.isRainingAt(head);
      return DaystarMath.open(DaystarEvents.dimOf(level), level.getDayTime(), sky, wet, level.isThundering());
   }

   public static double gauge(AbstractClientPlayer p) {
      return DaystarMath.held(open(p), p.isShiftKeyDown());
   }

   public static int variant(AbstractClientPlayer p) {
      return UnrpPowerLayers.powerVariant(p, "daystar");
   }

   public static boolean state(AbstractClientPlayer p, int variant, String key) {
      return variant != 0 && UnrpPowerLayers.abilityOn(p, "daystar", variant, key);
   }

   public static double scale(AbstractClientPlayer p) {
      Pose pose = p.getPose();
      double stand = pose == Pose.CROUCHING ? 1.5D : (pose != Pose.SWIMMING && !p.isFallFlying() && !p.isAutoSpinAttack() ? (pose == Pose.SLEEPING ? 0.2D : 1.8D) : 0.6D);
      return (double)p.getBbHeight() / stand;
   }

   public static double noonAge(int id, double t, double clip) {
      if (clip >= 0.0D) {
         double at_0 = t - clip;
         Double was = (Double)NOON_AT.get(id);
         if (was == null || Math.abs(was - at_0) > 2.0D) {
            if (NOON_AT.size() >= 64) {
               NOON_AT.clear();
            }

            NOON_AT.put(id, at_0);
         }
      }

      Double at = (Double)NOON_AT.get(id);
      if (at == null) {
         return -1.0D;
      } else {
         double age = t - at;
         if (!(age >= 44.0D) && !(age < -1.0D)) {
            return Math.max(0.0D, age);
         } else {
            NOON_AT.remove(id);
            return -1.0D;
         }
      }
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         Minecraft mc = Minecraft.getInstance();
         ClientLevel level = mc.level;
         if (level != null) {
            ids();
            Camera camera = event.getCamera();
            Vec3 cam = camera.getPosition();
            Vector3f l = camera.getLeftVector();
            Vector3f u = camera.getUpVector();
            Vec3 camRight = new Vec3((double)(-l.x()), (double)(-l.y()), (double)(-l.z()));
            Vec3 camUp = new Vec3((double)u.x(), (double)u.y(), (double)u.z());
            float pt = event.getPartialTick();
            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            DaystarDraw.normals(event.getPoseStack().last().normal());
            Matrix4f m = event.getPoseStack().last().pose();
            boolean first = mc.options.getCameraType().isFirstPerson();
            boolean drew = false;

            try {
               for(AbstractClientPlayer p : level.players()) {
                  if (!p.isInvisible() && !p.isSpectator() && !(p.distanceToSqr(cam) > 4096.0D)) {
                     int v = variant(p);
                     if (v != 0 && player(p, v, pt, buffers, m, cam, camRight, camUp, first && p == mc.getCameraEntity())) {
                        drew = true;
                     }
                  }
               }
            } finally {
               if (drew) {
                  buffers.endBatch(DaystarDraw.soft());
                  buffers.endBatch(DaystarDraw.add());
               }

            }

         }
      }
   }

   private static boolean player(AbstractClientPlayer p, int v, float pt, MultiBufferSource buffers, Matrix4f m, Vec3 cam, Vec3 camRight, Vec3 camUp, boolean self) {
      double t = (double)((float)p.tickCount + pt);
      Vec3 feet = p.getPosition(pt);
      double sc = scale(p);
      double gy = GroundScan.below(p.level(), feet.x, feet.y, feet.z, 4);
      float floor = Double.isNaN(gy) ? 0.0F : DaystarDraw.clamp01(1.0D - (feet.y - gy) / 2.0D);
      Vec3 ground = new Vec3(feet.x, Double.isNaN(gy) ? feet.y : gy, feet.z);
      boolean drew = false;
      boolean noonOn = state(p, v, "noon_state");
      boolean heatOn = state(p, v, "heat_state");
      if (noonOn) {
         noonAura(p, feet, ground, floor, sc, t, pt, buffers, m, cam, camRight, camUp, self);
         drew = true;
      }

      if (heatOn || noonOn) {
         double g = gauge(p);
         float k_0 = noonOn ? 1.0F : (float)((g - 0.5D) / 0.5D * 0.85D + 0.15D);
         heatAura(feet, ground, floor, sc, t, DaystarDraw.clamp01((double)k_0), buffers, m, cam, camRight, camUp, self);
         drew = true;
      }

      int id = p.getId();
      if (!self) {
         boolean posed = p.getMainArm() == HumanoidArm.RIGHT && !p.isSleeping() && !p.isFallFlying() && !p.isVisuallySwimming();
         boolean axe = posed && SunsteelAxeItem.isAxe(p.getMainHandItem());

         for(int k_0 = 0; axe && k_0 < TRAILS.length; ++k_0) {
            double ct = TRAILS[k_0] >= 0 ? MoveAnimClient.clipTime(id, true, TRAILS[k_0], t) : -1.0D;
            if (BladeTrail.shown(k_0, ct)) {
               strokes(id).see(k_0, t - ct, ct);
            }
         }

         if (!posed) {
            STROKES.remove(id);
         }

         BladeTrail.Strokes st = (BladeTrail.Strokes)STROKES.get(id);
         int live = st == null ? 0 : st.live(t);

         for(int i = 0; i < live; ++i) {
            int k = st.kind(i);
            trail(p, feet, sc, pt, k, MoveClips.CLIPS[TRAILS[k]], t - st.start(i), st.last(i), st.start(i), buffers, m, cam, camRight, camUp);
            drew = true;
         }

         if (st != null && live == 0) {
            STROKES.remove(id);
         }

         double dt = draw >= 0 ? MoveAnimClient.clipTime(id, true, draw, t) : -1.0D;
         if (dt >= 0.0D && dt < 14.0D) {
            gather(p, feet, sc, t, dt, buffers, m, cam, camRight, camUp);
            drew = true;
         }
      }

      double nt = noonAge(id, t, noon >= 0 ? MoveAnimClient.clipTime(id, true, noon, t) : -1.0D);
      if (nt >= 0.0D) {
         noonCall(feet, ground, floor, sc, nt, buffers, m, cam, camRight, camUp, self);
         drew = true;
      }

      return drew;
   }

   private static void heatAura(Vec3 feet, Vec3 ground, float floor, double sc, double t, float k, MultiBufferSource buffers, Matrix4f m, Vec3 cam, Vec3 camRight, Vec3 camUp, boolean self) {
      if (!(k <= 0.01F)) {
         Vec3 p = feet.subtract(cam);
         double h = 1.8D * sc;
         double wide = 0.55D * (1.0D + 0.4D * sc);
         if (!self) {
            VertexConsumer soft = buffers.getBuffer(DaystarDraw.soft());

            for(int i = 0; i < 5; ++i) {
               double a = (double)i * 1.257D + t * 0.018D;
               double rise = (t * 0.025D + (double)i * 0.2D) % 1.0D;
               Vec3 q = p.add(Math.cos(a) * wide, 0.15D + rise * h * 0.95D, Math.sin(a) * wide);
               DaystarDraw.bill(soft, m, q, camRight, camUp, 0.42D, t * 0.02D + (double)i, 3, 1.0F, 0.88F, 0.62F, 0.2F * k * (float)Math.sin(Math.PI * rise), false);
            }
         }

         VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
         if (!self) {
            for(int i = 0; i < 12; ++i) {
               double ph = (t * 0.018D + (double)i * 0.083D) % 1.0D;
               int cyc = (int)Math.floor(t * 0.018D + (double)i * 0.083D);
               double b = (double)i * 2.4D + (double)cyc * 0.7D;
               double rr = 0.35D + 0.5D * DaystarDraw.hash(i, cyc, 3);
               Vec3 mote = p.add(Math.cos(b) * rr, 0.1D + ph * h * 1.25D, Math.sin(b) * rr);
               DaystarDraw.bill(add, m, mote, camRight, camUp, 0.04D, 0.0D, 7, 1.0F, 0.78F, 0.36F, k * (float)(1.0D - ph) * 0.95F, true);
            }
         }

         if (floor > 0.0F) {
            DaystarDraw.flat(add, m, ground.subtract(cam).add(0.0D, 0.03D, 0.0D), 1.5D + 0.6D * (double)k, t * 0.01D, 5, 1.0F, 0.55F, 0.2F, 0.16F * k * floor, true);
         }

      }
   }

   private static void noonAura(AbstractClientPlayer pl, Vec3 feet, Vec3 ground, float floor, double sc, double t, float pt, MultiBufferSource buffers, Matrix4f m, Vec3 cam, Vec3 camRight, Vec3 camUp, boolean self) {
      Vec3 p = feet.subtract(cam);
      VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
      if (!self) {
         Vec3 look = pl.getViewVector(pt);
         Vec3 flat = new Vec3(look.x, 0.0D, look.z);
         flat = flat.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
         Vec3 behind = p.add(0.0D, 1.65D * sc, 0.0D).subtract(flat.scale(0.4D * sc));
         DaystarDraw.bill(add, m, behind, camRight, camUp, 0.85D * sc, t * 0.01D, 6, 1.0F, 0.84F, 0.4F, 0.85F, true);
         DaystarDraw.bill(add, m, behind, camRight, camUp, 1.35D * sc, -t * 0.008D, 1, 1.0F, 0.75F, 0.3F, 0.6F, true);
         DaystarDraw.bill(add, m, p.add(0.0D, 1.1D * sc, 0.0D), camRight, camUp, 1.6D * sc, 0.0D, 5, 1.0F, 0.8F, 0.4F, 0.22F, true);

         for(int i = 0; i < 10; ++i) {
            double ang = (double)i * 0.628D + t * 0.02D;
            Vec3 base = p.add(Math.cos(ang) * 0.55D * sc, 0.0D, Math.sin(ang) * 0.55D * sc);
            double fl = 0.7D + 0.3D * Math.sin(t * 0.7D + (double)i * 1.3D);
            DaystarDraw.upright(add, m, base, camRight, 0.5D * sc, 1.2D * sc * fl, 2, 1.0F, 0.8F, 0.36F, 0.55F, true);
         }
      }

      if (floor > 0.0F) {
         DaystarDraw.flat(add, m, ground.subtract(cam).add(0.0D, 0.03D, 0.0D), 6.0D, t * 0.004D, 4, 1.0F, 0.62F, 0.2F, 0.18F * floor, true);
      }

   }

   private static void noonCall(Vec3 feet, Vec3 ground, float floor, double sc, double age, MultiBufferSource buffers, Matrix4f m, Vec3 cam, Vec3 camRight, Vec3 camUp, boolean self) {
      Vec3 p = feet.subtract(cam);
      float down = DaystarDraw.clamp01(age / 10.0D);
      float fade = DaystarDraw.clamp01(1.0D - (age - 22.0D) / 22.0D);
      double bot = 44.0D - 44.0D * (double)DaystarDraw.outCubic((double)down);
      double w = 1.8D * sc;
      VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
      if (!self && 44.0D - bot > 0.05D) {
         DaystarDraw.upright(add, m, p.add(0.0D, bot, 0.0D), camRight, w * 2.4D, 44.0D - bot, 12, 1.0F, 0.86F, 0.5F, 0.45F * fade, true);
         DaystarDraw.upright(add, m, p.add(0.0D, bot, 0.0D), camRight, w * 0.9D, 44.0D - bot, 12, 1.0F, 0.98F, 0.86F, 0.65F * fade, true);
      }

      if (down >= 1.0F) {
         float k = DaystarDraw.clamp01((age - 10.0D) / 12.0D);
         if (floor > 0.0F) {
            DaystarDraw.ring(add, m, ground.subtract(cam).add(0.0D, 0.05D, 0.0D), 1.0D + 9.0D * (double)k, 0.7D, 48, 1.0F, 0.85F, 0.45F, 0.9F * (1.0F - k) * floor);
         }

         if (!self) {
            DaystarDraw.bill(add, m, p.add(0.0D, 1.4D, 0.0D), camRight, camUp, 2.5D + 4.0D * (double)k, 0.0D, 5, 1.0F, 0.95F, 0.75F, 0.9F * (1.0F - k), true);
         }
      }

   }

   private static Vec3 hand(AbstractClientPlayer pl, Vec3 feet, double sc, float pt) {
      HandCapture.Hands h = HandCapture.get(pl.getId(), (double)((float)pl.tickCount + pt));
      if (h != null) {
         return feet.add(h.right.subtract(feet).scale(sc));
      } else {
         Vec3 look = pl.getViewVector(pt);
         return pl.getEyePosition(pt).add(look.scale(0.6D)).add(0.0D, -0.5D, 0.0D);
      }
   }

   private static void gather(AbstractClientPlayer pl, Vec3 feet, double sc, double t, double age, MultiBufferSource buffers, Matrix4f m, Vec3 cam, Vec3 camRight, Vec3 camUp) {
      Vec3 c = hand(pl, feet, sc, (float)(t - Math.floor(t))).add(0.0D, 0.12D, 0.0D).subtract(cam);
      float k = DaystarDraw.clamp01(age / 8.0D);
      VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
      if (age < 10.0D) {
         DaystarDraw.bill(add, m, c, camRight, camUp, 0.1D + 0.3D * (double)k, age * 0.1D, 0, 1.0F, 0.95F, 0.8F, 0.6F + 0.4F * k, true);
         DaystarDraw.bill(add, m, c, camRight, camUp, 0.4D + 0.5D * (double)k, age * 0.05D, 1, 1.0F, 0.8F, 0.35F, 0.6F * k, true);

         for(int i = 0; i < 10; ++i) {
            double a = (double)i * 0.628D + age * 0.08D;
            double el = (double)(i % 3 - 1) * 0.5D;
            double r = 1.4D * (1.0D - (double)DaystarDraw.clamp01((age * 0.12D + (double)i * 0.07D) % 1.0D));
            Vec3 p = c.add(Math.cos(a) * Math.cos(el) * r, Math.sin(el) * r, Math.sin(a) * Math.cos(el) * r);
            DaystarDraw.bill(add, m, p, camRight, camUp, 0.05D, 0.0D, 7, 1.0F, 0.85F, 0.45F, 0.9F, true);
         }
      } else {
         float b = DaystarDraw.clamp01((age - 9.0D) / 5.0D);
         DaystarDraw.bill(add, m, c, camRight, camUp, 0.8D + 2.0D * (double)b, 0.0D, 5, 1.0F, 0.85F, 0.5F, 0.9F * (1.0F - b), true);
         DaystarDraw.bill(add, m, c, camRight, camUp, 0.7D + 1.6D * (double)b, age * 0.1D, 1, 1.0F, 0.8F, 0.4F, 0.8F * (1.0F - b), true);
      }

   }

   private static void trail(AbstractClientPlayer pl, Vec3 feet, double sc, float pt, int which, MoveClips.Clip clip, double ct, double last, double start, MultiBufferSource buffers, Matrix4f m, Vec3 cam, Vec3 camRight, Vec3 camUp) {
      double head = Math.min(Math.min(ct, last), BladeTrail.HIT[which]);
      double tail = Math.max(BladeTrail.FROM[which], head - 1.2D);
      if (!(head - tail < 0.02D)) {
         float fade = BladeTrail.fade(which, ct, last) * (0.6F + 0.4F * DaystarDraw.clamp01(open(pl)));
         int seed = 266 + (int)(Math.floor(start) % 997.0D);
         double yaw = Math.toRadians((double)Mth.rotLerp(pt, pl.yBodyRotO, pl.yBodyRot));
         Vec3 flat = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
         Vec3 side = new Vec3(-flat.z, 0.0D, flat.x);
         Vec3 base = feet.subtract(cam).add(0.0D, pl.isCrouching() ? -0.125D : 0.0D, 0.0D);
         int flags = (pl.isCrouching() ? 1 : 0) | (pl.isPassenger() ? 2 : 0);
         int n = 24;
         Vec3[] edge = new Vec3[n + 1];
         Vec3[] dir = new Vec3[n + 1];
         double[] len = new double[n + 1];

         for(int i = 0; i <= n; ++i) {
            double[] h = BladeTrail.hand(clip, tail + (head - tail) * (double)i / (double)n, flags);
            double[] e = BladeTrail.texel(h, 11.0D, 5.5D);
            double[] b = BladeTrail.texel(h, 16.6D, 15.4D);
            edge[i] = base.add(side.scale(e[0] * sc)).add(0.0D, e[1] * sc, 0.0D).add(flat.scale(e[2] * sc));
            Vec3 bk = base.add(side.scale(b[0] * sc)).add(0.0D, b[1] * sc, 0.0D).add(flat.scale(b[2] * sc));
            dir[i] = edge[i].subtract(bk);
            len[i] = i == 0 ? 0.0D : len[i - 1] + edge[i].distanceTo(edge[i - 1]);
         }

         double[] s = BladeTrail.along(len);
         float[] al = new float[n + 1];

         for(int i = 0; i <= n; ++i) {
            al[i] = fade * BladeTrail.ramp(s[i]);
         }

         double since = ct - BladeTrail.HIT[which];
         boolean flash = BladeTrail.FLASH[which] && last >= BladeTrail.HIT[which] && since >= 0.0D && since < 3.0D;
         float fb = flash ? (float)(1.0D - since / 3.0D) : 0.0F;
         Vec3 fc = new Vec3(edge[n].x, Math.max(edge[n].y, base.y + 0.2D), edge[n].z);
         VertexConsumer soft = buffers.getBuffer(DaystarDraw.soft());
         band(soft, m, edge, dir, s, al, BladeTrail.BODY, 13, false, 1.0F, 0.38F, 0.06F, 0.85F, false);
         band(soft, m, edge, dir, s, al, BladeTrail.FLAMES, 9, true, 1.0F, 0.45F, 0.1F, 0.9F, false);
         if (flash) {
            DaystarDraw.bill(soft, m, fc, camRight, camUp, 0.9D * sc * (double)fb, 0.0D, 5, 1.0F, 0.5F, 0.12F, 0.6F * fb, false);
         }

         VertexConsumer add = buffers.getBuffer(DaystarDraw.add());
         band(add, m, edge, dir, s, al, BladeTrail.HOT, 9, true, 1.0F, 0.68F, 0.28F, 0.75F, true);
         double[] core = new double[]{0.35D, 0.0D, 0.35D, 0.0D};
         band(add, m, edge, dir, s, al, core, 13, false, 1.0F, 0.88F, 0.6F, 0.9F, true);

         for(int i = 0; i < n; ++i) {
            double k = 0.5D * (s[i] + s[i + 1]);
            float a = 0.5F * (al[i] + al[i + 1]);
            DaystarDraw.ribbon(add, m, edge[i], edge[i + 1], Vec3.ZERO, (0.4D + 0.7D * k) * sc, 13, 1.0F, 0.6F, 0.2F, 0.45F * a, true);
            DaystarDraw.ribbon(add, m, edge[i], edge[i + 1], Vec3.ZERO, (0.06D + 0.16D * k) * sc, 13, 1.0F, 0.95F, 0.82F, 0.95F * a, true);
         }

         for(int i = 0; i <= n; i += 3) {
            DaystarDraw.bill(add, m, edge[i], camRight, camUp, (0.2D + 0.4D * s[i]) * sc, 0.0D, 5, 1.0F, 0.55F, 0.15F, 0.32F * al[i], true);
         }

         for(int j = 0; j < 20; ++j) {
            double h1 = DaystarDraw.hash(j, 11, seed);
            double h2 = DaystarDraw.hash(j, 12, seed);
            double h3 = DaystarDraw.hash(j, 13, seed);
            double sj = 0.2D + 0.8D * h1;
            int i = BladeTrail.seg(s, sj);
            double ds = s[i + 1] - s[i];
            double f = ds < 1.0E-9D ? 0.0D : (sj - s[i]) / ds;
            Vec3 p = edge[i].add(edge[i + 1].subtract(edge[i]).scale(f));
            Vec3 d = dir[i].add(dir[i + 1].subtract(dir[i]).scale(f));
            double ph = ((ct - BladeTrail.FROM[which]) * (0.07D + 0.05D * h3) + h2) % 1.0D;
            Vec3 q = p.add(d.scale(0.1D + ph * (1.0D + 1.5D * h3))).add(0.0D, ph * ph * 0.8D * sc, 0.0D);
            DaystarDraw.bill(add, m, q, camRight, camUp, (0.05D + 0.05D * h3) * sc * (1.0D - 0.5D * ph), 0.0D, 7, 1.0F, (float)(0.72D + 0.2D * h3), 0.3F, (float)((1.0D - ph) * (0.4D + 0.6D * sj)) * fade, true);
         }

         if (flash) {
            DaystarDraw.bill(add, m, fc, camRight, camUp, 1.3D * sc * (double)fb, 0.0D, 5, 1.0F, 0.68F, 0.28F, 0.85F * fb, true);
            DaystarDraw.bill(add, m, fc, camRight, camUp, sc * (1.3D - 0.3D * (double)fb), ct * 0.25D, 1, 1.0F, 0.82F, 0.45F, 0.75F * fb, true);
         }

      }
   }

   private static void band(VertexConsumer vc, Matrix4f m, Vec3[] edge, Vec3[] dir, double[] s, float[] al, double[] w, int cell, boolean lengthwise, float r, float g, float b, float k, boolean additive) {
      for(int i = 0; i + 1 < edge.length; ++i) {
         Vec3 i0 = edge[i].subtract(dir[i].scale(w[0] + w[1] * s[i]));
         Vec3 i1 = edge[i + 1].subtract(dir[i + 1].scale(w[0] + w[1] * s[i + 1]));
         Vec3 o1 = edge[i + 1].add(dir[i + 1].scale(w[2] + w[3] * s[i + 1]));
         Vec3 o0 = edge[i].add(dir[i].scale(w[2] + w[3] * s[i]));
         DaystarDraw.strip(vc, m, i0, i1, o1, o0, cell, lengthwise, (float)s[i], (float)s[i + 1], r, g, b, k * al[i], k * al[i + 1], additive);
      }

   }
}
