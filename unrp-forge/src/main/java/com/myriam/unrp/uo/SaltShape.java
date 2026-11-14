package com.myriam.unrp.uo;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SaltShape {
   public static final int S_GRAINS = 0;
   public static final int S_DUST = 1;
   public static final int S_SHARD = 2;
   public static final int S_LANCE = 3;
   public static final int S_SPIKE = 4;
   public static final int S_DOT = 5;
   public static final int S_SPEAR = 6;
   public static final int S_BRANCH = 7;
   public static final int S_DRIP = 8;
   public static final int S_WAVE = 9;
   public static final int A_GLINT = 0;
   public static final int A_GLOW = 1;
   public static final int A_RIM = 2;
   public static final int A_BEAM = 3;
   public static final int A_DOT = 4;
   public static final int A_WAVE = 5;
   public static final int A_SLASH = 6;
   public static final double TRUMPET_COLUMN = 40.2D;
   public static final int D_FLATS = 0;
   public static final int D_CRACKS = 1;
   public static final double[] SALT = new double[]{0.95D, 0.95D, 0.93D};
   public static final double[] CRYS = new double[]{0.9D, 0.95D, 1.0D};
   public static final double[] LIGHT = new double[]{1.0D, 0.94D, 0.76D};
   public static final double[] GOLDLT = new double[]{1.0D, 0.86D, 0.5D};
   public static final double[] BARK = new double[]{0.075D, 0.062D, 0.07D};
   public static final double[] SHEEN = new double[]{0.5D, 0.06D, 0.09D};
   public static final double[] BLOOD = new double[]{0.3D, 0.02D, 0.035D};
   public static final double[] BLOOD_HI = new double[]{0.7D, 0.07D, 0.1D};
   public static final double[] CRACKS = new double[]{0.95D, 0.92D, 0.86D};
   public static final int THORN_RINGS = 4;
   public static final double REND_STEP = 1.3D;
   private static final Map<String, List<SaltShape.Limb>> TREES = new HashMap();
   public static final double SPEAR_HALF = 1.2D;
   public static final double[] MOUTH = new double[]{0.0D, -2.5D, -4.5D};

   private SaltShape() {
   }

   public static double hash3(int x, int y, int z) {
      double s = (double)x * 3.74761393E8D + (double)y * 6.68265263E8D + (double)z * (double)Integer.MAX_VALUE;
      int h = (int)((long)s);
      h = (h ^ h >>> 13) * 1274126177;
      return (double)((long)(h ^ h >>> 16) & 4294967295L) / (double)4.2949673E9F;
   }

   static double[] v(double x, double y, double z) {
      return new double[]{x, y, z};
   }

   static double[] cross(double[] a, double[] b) {
      return v(a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]);
   }

   public static double[][] basis(double[] n) {
      double[] up0 = Math.abs(n[1]) > 0.97D ? v(1.0D, 0.0D, 0.0D) : v(0.0D, 1.0D, 0.0D);
      double[] right = SaltMath.norm(cross(n, up0));
      double[] up = SaltMath.norm(cross(right, n));
      return new double[][]{right, up};
   }

   public static void gather(SaltShape.Sink sink, double[] handR, double[] handL, double age) {
      double[] mid = SaltMath.mul(SaltMath.add(handR, handL), 0.5D);

      for(int i = 0; i < 14; ++i) {
         double ang = (double)i * 0.449D + age * 0.35D;
         double r = 0.5D * (1.0D - SaltMath.clamp01(age / 8.0D)) + 0.08D;
         double[] p = SaltMath.add(mid, v(Math.cos(ang) * r, Math.sin(ang * 1.6D) * r * 0.6D, Math.sin(ang) * r));
         sink.bill(false, p, 0.05D, ang, 0, SALT[0], SALT[1], SALT[2], 0.9D);
      }

      sink.bill(true, mid, 0.25D, age * 0.2D, 0, 1.0D, 0.95D, 0.85D, 0.8D);
   }

   public static void lanceTrail(SaltShape.Sink sink, double[] p, double[] dir, double age) {
      for(int i = 1; i < 10; ++i) {
         double[] q = SaltMath.sub(p, SaltMath.mul(dir, (double)i * 0.35D));
         sink.bill(false, SaltMath.add(q, v(Math.sin((double)i * 2.1D + age) * 0.06D, -0.03D * (double)i, Math.cos((double)i * 1.7D) * 0.06D)), 0.05D, (double)i, 0, SALT[0], SALT[1], SALT[2], 0.9D * (1.0D - (double)i / 10.0D));
      }

      sink.bill(true, SaltMath.add(p, SaltMath.mul(dir, 0.8D)), 0.25D, age * 0.5D, 0, 1.0D, 0.97D, 0.9D, 0.8D);
   }

   public static void spear(SaltShape.Sink sink, double[] c, double[] dir, double a) {
      double[][] bs = basis(dir);
      double[] tail = SaltMath.sub(c, SaltMath.mul(dir, 1.2D));
      double[] tip = SaltMath.add(c, SaltMath.mul(dir, 1.2D));

      for(int k = 0; k < 2; ++k) {
         double[] n = SaltMath.mul(bs[k], 1.2D);
         sink.quad(false, tail, SaltMath.add(c, n), tip, SaltMath.sub(c, n), 6, 1.0D, 1.0D, 1.0D, a);
      }

   }

   public static void burst(SaltShape.Sink sink, double[] at, double age, double sc) {
      double k = SaltMath.clamp01(age / 14.0D);

      for(int i = 0; i < 14; ++i) {
         double a = (double)i * 0.449D;
         double el = Math.sin((double)i * 1.9D) * 0.8D;
         double sp = (0.1D + 0.05D * hash3(i, 1, 2)) * sc;
         double[] p = v(at[0] + Math.cos(a) * Math.cos(el) * sp * age, at[1] + Math.sin(el) * sp * age - 0.008D * age * age, at[2] + Math.sin(a) * Math.cos(el) * sp * age);
         sink.bill(false, p, 0.06D * sc, a, 0, SALT[0], SALT[1], SALT[2], 1.0D - k);
      }

      sink.bill(false, at, (0.3D + 0.6D * k) * sc, 0.0D, 1, 1.0D, 1.0D, 1.0D, 0.6D * (1.0D - k));
      sink.bill(true, at, 0.4D * sc, age * 0.3D, 0, 1.0D, 0.95D, 0.85D, 1.0D - k);
   }

   public static void shards(SaltShape.Sink sink, double[] at, double[] dir, double age, double sc) {
      double k = SaltMath.clamp01(age / 20.0D);

      for(int i = 0; (double)i < 10.0D * sc; ++i) {
         double a = (double)i * 0.628D;
         double[] d = SaltMath.norm(SaltMath.add(SaltMath.mul(dir, -0.4D), v(Math.cos(a), 0.6D + 0.4D * Math.sin((double)i), Math.sin(a))));
         double sp = (0.12D + 0.06D * hash3(i, 3, 1)) * sc;
         double[] p = SaltMath.add(at, SaltMath.mul(d, sp * age));
         p[1] -= 0.01D * age * age;
         sink.bill(false, p, 0.09D * Math.sqrt(sc), a + age * 0.3D, 2, CRYS[0], CRYS[1], CRYS[2], 1.0D - k);
      }

   }

   public static void pillarForm(SaltShape.Sink sink, double[] p, double h, double age) {
      double k = SaltMath.clamp01(age / 12.0D);

      for(int i = 0; i < 10; ++i) {
         double a = (double)i * 0.628D + age * 0.2D;
         double y = p[1] + k * h * ((double)i / 10.0D + 0.1D);
         sink.bill(true, v(p[0] + Math.cos(a) * 0.55D, y, p[2] + Math.sin(a) * 0.55D), 0.12D, a, 0, 1.0D, 0.97D, 0.9D, 1.0D - k * 0.6D);
      }

      sink.decal(0, p, 0.9D, 0.3D, 0.9D);
   }

   public static void unsalt(SaltShape.Sink sink, double[] at, double h, double age) {
      double k = SaltMath.clamp01(age / 22.0D);

      for(int i = 0; i < 18; ++i) {
         double a = (double)i * 0.349D;
         double y0 = at[1] + h * (0.1D + 0.85D * hash3(i, 4, 9));
         double r = 0.35D + 0.5D * k;
         sink.bill(false, v(at[0] + Math.cos(a) * r, y0 - 0.02D * age * age * 0.4D, at[2] + Math.sin(a) * r), 0.08D, a + age * 0.3D, 2, 0.97D, 0.97D, 0.95D, 1.0D - k);
      }

      sink.bill(false, v(at[0], at[1] + h * 0.5D, at[2]), 0.6D + 0.8D * k, 0.0D, 1, 1.0D, 1.0D, 1.0D, 0.5D * (1.0D - k));
      sink.decal(0, at, 0.8D, 0.5D, 0.7D * (1.0D - k * 0.5D));
   }

   public static void shatter(SaltShape.Sink sink, double[] at, double age) {
      double k = SaltMath.clamp01(age / 24.0D);

      for(int i = 0; i < 22; ++i) {
         double a = (double)i * 0.286D;
         double el = 0.2D + 0.8D * hash3(i, 2, 5);
         double sp = 0.12D + 0.12D * hash3(i, 1, 5);
         double[] p = v(at[0] + Math.cos(a) * sp * age, at[1] - 0.5D + (double)(i % 4) * 0.4D + el * sp * age * 1.2D - 0.014D * age * age, at[2] + Math.sin(a) * sp * age);
         sink.bill(false, p, 0.14D, a + age * 0.4D, 2, 0.97D, 0.97D, 0.95D, 1.0D - k);
      }

      for(int i = 0; i < 6; ++i) {
         sink.bill(false, v(at[0] + Math.cos((double)i) * 0.4D * k, at[1] - 0.3D + k, at[2] + Math.sin((double)i) * 0.4D * k), 0.4D + 0.6D * k, (double)i, 1, 1.0D, 1.0D, 1.0D, 0.7D * (1.0D - k));
      }

      sink.decal(0, v(at[0], at[1] - 0.95D, at[2]), 1.0D, 0.5D, 0.9D * (1.0D - k * 0.5D));
   }

   public static void crumble(SaltShape.Sink sink, double[] at, double h, double age) {
      double k = SaltMath.clamp01(age / 30.0D);

      for(int i = 0; i < 26; ++i) {
         double a = (double)i * 0.24D;
         double r = 0.1D + 0.3D * hash3(i, 1, 9);
         double y0 = at[1] + h * (0.1D + 0.85D * hash3(i, 2, 9));
         double fall = Math.min(y0 - at[1], 0.012D * age * age * (0.6D + hash3(i, 3, 9)));
         sink.bill(false, v(at[0] + Math.cos(a) * r * (1.0D + k), y0 - fall, at[2] + Math.sin(a) * r * (1.0D + k)), 0.07D, a, 0, SALT[0], SALT[1], SALT[2], 1.0D - Math.max(0.0D, k - 0.6D) * 2.5D);
      }

      for(int i = 0; i < 5; ++i) {
         sink.bill(false, v(at[0], at[1] + 0.3D + (double)i * 0.35D + k * 0.5D, at[2]), 0.35D + 0.5D * k, (double)i, 1, 1.0D, 1.0D, 1.0D, 0.55D * (1.0D - k));
      }

      sink.decal(0, at, 0.75D * SaltMath.clamp01(age / 10.0D), 0.8D, 0.95D);
   }

   public static void crystalLance(SaltShape.Sink sink, double[] tail, double[] tip, double w, double a, double t) {
      double[] axis = SaltMath.sub(tip, tail);
      double len = SaltMath.len(axis);
      if (!(len < 0.05D) && !(a <= 0.003D)) {
         double[] d = SaltMath.mul(axis, 1.0D / len);
         double[][] bs = basis(d);

         for(int k = 0; k < 3; ++k) {
            double ang = (double)k * Math.PI / 3.0D + 0.4D;
            double[] side = SaltMath.add(SaltMath.mul(bs[0], Math.cos(ang) * w), SaltMath.mul(bs[1], Math.sin(ang) * w));
            sink.quad(false, SaltMath.sub(tail, side), SaltMath.add(tail, side), SaltMath.add(tip, side), SaltMath.sub(tip, side), 3, CRYS[0], CRYS[1], CRYS[2], 0.97D * a);
         }

         sink.ribbon(true, tail, SaltMath.sub(tip, SaltMath.mul(d, len * 0.2D)), w * 0.5D, 2, 1.0D, 0.96D, 0.88D, 0.35D * a);

         for(int i = 0; i < 5; ++i) {
            double q = (t * 0.05D + (double)i / 5.0D) % 1.0D;
            sink.bill(true, SaltMath.lerp(tail, tip, q), 0.16D + 0.1D * w, t * 0.2D + (double)i, 0, 1.0D, 0.95D, 0.85D, 0.8D * a * Math.sin(Math.PI * q));
         }

      }
   }

   public static void greatForm(SaltShape.Sink sink, double[] hand, double[] aimAt, double lt, double t) {
      double grow = SaltMath.smooth(0.0D, 1.0D, (lt - 4.0D) / 18.0D);
      double len = 6.0D * (0.15D + 0.85D * grow);
      double wind = SaltMath.smooth(0.0D, 1.0D, (lt - 22.0D) / 4.0D);
      double[] aim = SaltMath.norm(SaltMath.sub(aimAt, hand));
      double[] dir = SaltMath.norm(SaltMath.add(aim, v(0.0D, 0.12D + 0.25D * wind, 0.0D)));
      double[] c = SaltMath.add(hand, v(0.0D, 0.55D + 0.25D * grow, 0.0D));
      c = SaltMath.sub(c, SaltMath.mul(SaltMath.flat(aim), 0.9D * wind));
      double[] tail = SaltMath.sub(c, SaltMath.mul(dir, len * 0.55D));
      double[] tip = SaltMath.add(c, SaltMath.mul(dir, len * 0.45D));
      crystalLance(sink, tail, tip, 0.2D + 0.35D * grow, 0.35D + 0.65D * grow, t);

      for(int i = 0; i < 26; ++i) {
         double ph = (lt * 0.07D + (double)i / 26.0D) % 1.0D;
         if (lt > 22.0D) {
            break;
         }

         double[] at = SaltMath.lerp(tail, tip, (double)i * 0.37D % 1.0D);
         double r = 2.4D * (1.0D - ph) + 0.1D;
         double ang = (double)i * 2.4D + lt * 0.3D;
         double[] p = SaltMath.add(at, v(Math.cos(ang) * r, Math.sin(ang * 0.7D) * r * 0.6D, Math.sin(ang) * r));
         sink.bill(false, p, 0.06D, ang, 0, SALT[0], SALT[1], SALT[2], 0.9D * ph);
      }

      sink.bill(true, c, 0.9D + 1.2D * grow, 0.0D, 1, LIGHT[0], LIGHT[1], LIGHT[2], 0.3D * grow);
   }

   public static void greatFlight(SaltShape.Sink sink, double[] from, double[] dir, double age, double t) {
      double[] tip = SaltMath.add(from, SaltMath.mul(dir, 2.4D * age));
      double[] tail = SaltMath.sub(tip, SaltMath.mul(dir, 6.0D));
      crystalLance(sink, tail, tip, 0.55D, 1.0D, t);
      sink.bill(true, SaltMath.lerp(tail, tip, 0.6D), 2.6D, 0.0D, 1, LIGHT[0], LIGHT[1], LIGHT[2], 0.4D);

      for(int i = 0; i < 16; ++i) {
         double[] q = SaltMath.sub(tail, SaltMath.mul(dir, (double)i * 0.45D));
         double sp = 0.1D + (double)i * 0.03D;
         sink.bill(false, SaltMath.add(q, v(Math.sin((double)i * 2.1D + age) * sp, Math.cos((double)i * 1.3D + age) * sp - 0.02D * (double)i, Math.cos((double)i * 1.7D) * sp)), 0.07D + 0.01D * (double)i, (double)i, 0, SALT[0], SALT[1], SALT[2], 0.9D * (1.0D - (double)i / 16.0D));
      }

   }

   public static void greatStuck(SaltShape.Sink sink, double[] stuck, double[] dir, double since, double t) {
      double k = SaltMath.clamp01(since / 40.0D);
      double[] tip0 = SaltMath.add(stuck, SaltMath.mul(dir, 0.6D));
      crystalLance(sink, SaltMath.sub(tip0, SaltMath.mul(dir, 6.0D * (1.0D - k * 0.6D))), tip0, 0.5D, 1.0D - k, t);
   }

   public static void greatBurst(SaltShape.Sink sink, double[] at, double[] dir, double groundY, double age) {
      double k = SaltMath.clamp01(age / 26.0D);
      double radius = 3.0D;
      double[] g = v(at[0], groundY, at[2]);
      sink.decal(0, g, radius * SaltMath.outCubic(age / 6.0D), 0.3D, 0.95D * (1.0D - k * 0.3D));
      sink.bill(true, at, 1.5D + 3.0D * k, 0.0D, 1, LIGHT[0], LIGHT[1], LIGHT[2], 0.8D * (1.0D - k));

      for(int i = 0; i < 14; ++i) {
         double a = (double)i * 0.449D + 0.2D;
         double rr = radius * (0.3D + 0.7D * hash3(i, 7, 2));
         double h = (0.6D + 1.2D * hash3(i, 7, 3)) * SaltMath.outCubic(age / 4.0D) * (1.0D - SaltMath.smooth(0.6D, 1.0D, k));
         spike(sink, v(at[0] - dir[0] * 0.8D + Math.cos(a) * rr, g[1], at[2] - dir[2] * 0.8D + Math.sin(a) * rr), h, a, 1.0D, (double[])null);
      }

      for(int i = 0; i < 20; ++i) {
         double b = (double)i * 0.314D;
         double el = 0.3D + 0.6D * hash3(i, 2, 8);
         double sp = 0.15D + 0.15D * hash3(i, 1, 8);
         double[] p = v(at[0] + Math.cos(b) * sp * age, at[1] + el * sp * age * 1.4D - 0.012D * age * age, at[2] + Math.sin(b) * sp * age);
         sink.bill(false, p, 0.12D, b + age * 0.4D, 2, 0.97D, 0.97D, 0.95D, 1.0D - k);
      }

      sink.bill(false, at, 1.0D + 2.5D * k, 0.0D, 1, 1.0D, 1.0D, 1.0D, 0.6D * (1.0D - k));
   }

   public static void spike(SaltShape.Sink sink, double[] base, double h, double ang, double a, double[] lean) {
      if (!(h <= 0.02D) && !(a <= 0.003D)) {
         double[] l = lean == null ? v(0.0D, 0.0D, 0.0D) : lean;
         double[] top = SaltMath.add(base, SaltMath.add(v(0.0D, h, 0.0D), SaltMath.mul(l, h)));

         for(int k = 0; k < 2; ++k) {
            double an = k == 0 ? ang : ang + (Math.PI / 2D);
            double[] w = v(Math.cos(an) * h * 0.24D, 0.0D, Math.sin(an) * h * 0.24D);
            sink.quad(false, SaltMath.sub(base, w), SaltMath.add(base, w), SaltMath.add(top, w), SaltMath.sub(top, w), 4, CRYS[0] * (k == 1 ? 0.9D : 1.0D), CRYS[1] * (k == 1 ? 0.93D : 1.0D), CRYS[2], 0.97D * a);
         }

         sink.bill(true, SaltMath.lerp(base, top, 0.85D), 0.1D + 0.08D * h, ang * 3.0D, 0, 1.0D, 0.93D, 0.8D, 0.5D * a);
      }
   }

   public static void dustPuff(SaltShape.Sink sink, double[] at, double age) {
      double k = SaltMath.clamp01(age / 12.0D);

      for(int i = 0; i < 4; ++i) {
         sink.bill(false, v(at[0] + Math.cos((double)i * 1.6D) * 0.5D * (0.5D + k), at[1] + k * 0.3D, at[2] + Math.sin((double)i * 1.6D) * 0.5D * (0.5D + k)), 0.25D + 0.3D * k, (double)i, 1, 1.0D, 1.0D, 1.0D, 0.6D * (1.0D - k));
      }

      sink.bill(true, v(at[0], at[1] + 0.6D, at[2]), 0.3D, age * 0.3D, 0, 1.0D, 0.96D, 0.86D, 0.7D * (1.0D - k));
   }

   public static void blockCrumble(SaltShape.Sink sink, double[] at, double age) {
      double k = SaltMath.clamp01(age / 20.0D);

      for(int i = 0; i < 8; ++i) {
         double a = (double)i * 0.785D;
         double[] p = v(at[0] + Math.cos(a) * 0.3D * (1.0D + k), at[1] + 0.3D - 0.02D * age * age * 0.5D, at[2] + Math.sin(a) * 0.3D * (1.0D + k));
         sink.bill(false, p, 0.12D, a + age * 0.2D, 2, 0.97D, 0.97D, 0.95D, 1.0D - k);
         sink.bill(false, v(p[0], p[1] - 0.2D, p[2]), 0.06D, a, 0, SALT[0], SALT[1], SALT[2], 1.0D - k);
      }

   }

   public static void fizz(SaltShape.Sink sink, double[] at, double age) {
      double k = SaltMath.clamp01(age / 20.0D);

      for(int i = 0; i < 8; ++i) {
         double ph = (age * 0.06D + (double)i / 8.0D) % 1.0D;
         sink.bill(false, v(at[0] + Math.cos((double)i) * 0.35D, at[1] - 0.3D + ph * 1.0D, at[2] + Math.sin((double)i) * 0.35D), 0.08D, 0.0D, 5, 0.85D, 0.92D, 1.0D, (1.0D - ph) * (1.0D - k));
      }

   }

   public static double[] spiresSpike(double[] at, double[] dir, double[] side, int age, int k, int n, double front) {
      double width = SaltMath.spiresWidth(front);
      double lat = n == 1 ? 0.0D : ((double)k / (double)(n - 1) - 0.5D) * width;
      lat += (hash3(age, k, 3) - 0.5D) * 0.5D;
      double[] p = SaltMath.add(at, SaltMath.add(SaltMath.mul(dir, front + (hash3(age, k, 4) - 0.5D) * 0.6D), SaltMath.mul(side, lat)));
      double h = 0.9D + 1.4D * hash3(age, k, 5) + front * 0.03D;
      double ang = hash3(age, k, 6) * 3.0D;
      double[] lean = SaltMath.mul(SaltMath.add(dir, SaltMath.mul(side, lat / Math.max(1.0D, front))), 0.18D);
      return new double[]{p[0], p[1], p[2], h, ang, lean[0], lean[1], lean[2]};
   }

   public static void spiresDraw(SaltShape.Sink sink, double[] base, double sh, double sang, double[] lean, double la) {
      if (!(la < 0.0D)) {
         double stand = 60.0D;
         double grow = SaltMath.outCubic(la / 2.5D);
         double fall = SaltMath.smooth(stand, stand + 10.0D, la);
         double h = sh * grow * (1.0D - 0.6D * fall);
         spike(sink, base, h, sang, 1.0D - fall, lean);
         if (la < 10.0D) {
            sink.bill(false, SaltMath.add(base, v(0.0D, 0.3D + la * 0.05D, 0.0D)), 0.4D + la * 0.06D, sang, 1, 1.0D, 1.0D, 1.0D, 0.5D * (1.0D - la / 10.0D));
         }

         sink.decal(0, base, 0.7D, sang, 0.85D * (1.0D - fall));
         if (fall > 0.0D && fall < 1.0D) {
            for(int i = 0; i < 3; ++i) {
               sink.bill(false, SaltMath.add(base, v(Math.cos((double)(i * 2) + sang) * 0.3D, sh * 0.5D * (1.0D - fall), Math.sin((double)(i * 2) + sang) * 0.3D)), 0.08D, (double)i, 2, 0.97D, 0.97D, 0.95D, 1.0D - fall);
            }
         }

      }
   }

   public static void sealCast(SaltShape.Sink sink, double[] hand, double[] tp, double age) {
      double k = SaltMath.clamp01(age / 6.0D);

      for(int i = 0; i < 10; ++i) {
         double q = SaltMath.clamp01(k * 1.3D - (double)i * 0.05D);
         double[] p = SaltMath.lerp(hand, v(tp[0], tp[1] + 0.2D, tp[2]), q);
         p[1] += Math.sin(Math.PI * q) * 0.6D;
         sink.bill(false, p, 0.06D, (double)i, 0, SALT[0], SALT[1], SALT[2], 0.9D * (1.0D - k * 0.5D));
      }

   }

   public static void seal(SaltShape.Sink sink, double[] c, double age) {
      double root = 60.0D;
      double fall = SaltMath.smooth(root, root + 14.0D, age);
      sink.decal(0, c, 1.3D, 0.2D, 0.95D * (1.0D - fall));

      for(int i = 0; i < 9; ++i) {
         double a = (double)i * 0.698D + 0.3D;
         double r = 0.75D + 0.15D * hash3(i, 5, 1);
         double[] base = v(c[0] + Math.cos(a) * r, c[1], c[2] + Math.sin(a) * r);
         double h = (0.8D + 0.6D * hash3(i, 5, 2)) * SaltMath.outCubic(age / 3.0D) * (1.0D - 0.5D * fall);
         spike(sink, base, h, a, 1.0D - fall, v(-Math.cos(a) * 0.45D, 0.0D, -Math.sin(a) * 0.45D));
      }

      if (age < 12.0D) {
         for(int i = 0; i < 6; ++i) {
            sink.bill(false, v(c[0] + Math.cos((double)i) * 0.9D, c[1] + 0.4D + age * 0.05D, c[2] + Math.sin((double)i) * 0.9D), 0.4D + age * 0.05D, (double)i, 1, 1.0D, 1.0D, 1.0D, 0.5D * (1.0D - age / 12.0D));
         }
      }

   }

   public static synchronized List<SaltShape.Limb> tree(double size, int seed) {
      String key = size + ":" + seed;
      List<SaltShape.Limb> had = (List)TREES.get(key);
      if (had != null) {
         return had;
      } else {
         List<SaltShape.Limb> limbs = new ArrayList();
         grow(limbs, seed, v(0.0D, 0.0D, 0.0D), SaltMath.norm(v(1.0D, 0.62D, 0.42D)), 1.45D * size, 0.27D * size, 0, 0);
         List<SaltShape.Limb> fixed = Collections.unmodifiableList(limbs);
         TREES.put(key, fixed);
         return fixed;
      }
   }

   private static void grow(List<SaltShape.Limb> limbs, int seed, double[] p, double[] dir, double len, double w, int depth, int idx) {
      int n = 5;
      double[][] pts = new double[n + 1][];
      pts[0] = (double[])p.clone();
      double[] q = (double[])p.clone();
      double[] d = (double[])dir.clone();

      for(int i = 1; i <= n; ++i) {
         double[] j = v(hash3(seed, idx * 7 + i, 1) - 0.5D, hash3(seed, idx * 7 + i, 2) - 0.45D, hash3(seed, idx * 7 + i, 3) - 0.5D);
         d = SaltMath.norm(SaltMath.add(d, SaltMath.mul(j, 0.5D)));
         q = SaltMath.add(q, SaltMath.mul(d, len / (double)n));
         pts[i] = (double[])q.clone();
      }

      limbs.add(new SaltShape.Limb(pts, w, w * (depth >= 3 ? 0.12D : 0.5D), depth, idx, depth >= 3));
      if (depth < 3) {
         int kids = depth <= 1 ? 3 : 2;

         for(int k = 0; k < kids; ++k) {
            int at = Math.min(n, 2 + k);
            double[] perp = SaltMath.norm(cross(d, v(0.0D, 0.0D, 1.0D)));
            double spread = (k % 2 == 1 ? 1.0D : -1.0D) * (0.55D + 0.45D * hash3(seed, idx, 10 + k));
            double[] cd = SaltMath.norm(SaltMath.add(SaltMath.add(d, SaltMath.mul(perp, spread)), v(0.0D, 0.12D, 0.3D * (hash3(seed, idx, 20 + k) - 0.35D))));
            grow(limbs, seed, pts[at], cd, len * (0.58D + 0.14D * hash3(seed, idx, 30 + k)), w * 0.58D, depth + 1, idx * 4 + k + 1);
         }

      }
   }

   public static double[][] shoulderLocal(int side) {
      return new double[][]{v((double)side * 2.4D, 2.6D, 2.2D), v((double)side * 2.4D + 1.0D, 2.6D, 2.2D), v((double)side * 2.4D, 1.6D, 2.2D), v((double)side * 2.4D, 2.6D, 3.2D)};
   }

   public static double[][] shoulder(double[] p0, double[] px, double[] py, double[] pz, int side) {
      return new double[][]{p0, SaltMath.mul(SaltMath.norm(SaltMath.sub(px, p0)), (double)side), SaltMath.norm(SaltMath.sub(py, p0)), SaltMath.norm(SaltMath.sub(pz, p0))};
   }

   public static List<double[]> branches(SaltShape.Sink sink, double[][] fr, double size, int seed, double grow, double stagger, double span, double alpha) {
      List<SaltShape.Limb> limbs = tree(size, seed);
      List<double[]> tips = new ArrayList();

      for(SaltShape.Limb limb : limbs) {
         double appear = SaltMath.clamp01((grow - (double)limb.depth * stagger) / span);
         if (!(appear <= 0.0D)) {
            int n = limb.pts.length;
            double vis = appear * (double)(n - 1);
            List<double[]> pts = new ArrayList();
            List<Double> ws = new ArrayList();
            List<Double> al = new ArrayList();
            List<Double> sh = new ArrayList();

            for(int i = 0; i < n && !((double)i > Math.ceil(vis)); ++i) {
               double[] lp = limb.pts[i];
               if ((double)i > vis) {
                  lp = SaltMath.lerp(limb.pts[i - 1], limb.pts[i], vis - (double)(i - 1));
               }

               double s = (double)i / (double)(n - 1);
               pts.add(SaltMath.add(fr[0], SaltMath.add(SaltMath.mul(fr[1], lp[0]), SaltMath.add(SaltMath.mul(fr[2], lp[1]), SaltMath.mul(fr[3], lp[2])))));
               ws.add((limb.w0 + (limb.w1 - limb.w0) * s) * (0.6D + 0.4D * appear));
               al.add(alpha * 0.98D);
               sh.add(alpha * (0.22D + 0.25D * Math.max(0.0D, Math.sin((double)i * 1.3D + (double)limb.idx * 0.7D))));
            }

            if (pts.size() >= 2) {
               double[][] pa = (double[][])pts.toArray(new double[0][]);
               double[] wa = new double[ws.size()];
               double[] w2 = new double[ws.size()];
               double[] aa = new double[al.size()];
               double[] sa = new double[sh.size()];

               for(int i = 0; i < wa.length; ++i) {
                  wa[i] = ws.get(i);
                  w2[i] = ws.get(i) * 0.35D;
                  aa[i] = al.get(i);
                  sa[i] = sh.get(i);
               }

               sink.chain(false, pa, wa, 7, BARK, aa);
               sink.chain(true, pa, w2, 2, SHEEN, sa);
               if (limb.tip && appear >= 1.0D) {
                  tips.add(new double[]{pa[pa.length - 1][0], pa[pa.length - 1][1], pa[pa.length - 1][2], (double)limb.idx});
               }
            }
         }
      }

      return tips;
   }

   public static void drips(SaltShape.Sink sink, List<double[]> tips, double[] grounds, double t, double alpha) {
      for(int j = 0; j < tips.size(); ++j) {
         double[] tp = (double[])tips.get(j);
         double[] p = new double[]{tp[0], tp[1], tp[2]};
         int idx = (int)tp[3];
         double g = grounds[j];
         double fall = Math.sqrt(Math.max(0.0D, p[1] - 0.25D - g) / 0.025D);
         double period = Math.ceil(18.0D + fall + (double)(idx * 7 % 15));
         double a = (t + (double)j * 5.3D) % period;
         if (a < 4.0D) {
            sink.bill(false, SaltMath.add(p, v(0.0D, -0.06D * a, 0.0D)), 0.05D + 0.03D * a, 0.0D, 8, BLOOD[0], BLOOD[1], BLOOD[2], 0.95D * alpha);
         } else if (a < 4.0D + fall) {
            double fallY = p[1] - 0.25D - 0.025D * (a - 4.0D) * (a - 4.0D);
            sink.bill(false, v(p[0], fallY, p[2]), 0.15D, 0.0D, 8, BLOOD[0], BLOOD[1], BLOOD[2], 0.95D * alpha);
            sink.bill(true, v(p[0], fallY + 0.03D, p[2]), 0.04D, 0.0D, 4, BLOOD_HI[0], BLOOD_HI[1], BLOOD_HI[2], 0.5D * alpha);
         } else if (a < 10.0D + fall) {
            double k = (a - 4.0D - fall) / 6.0D;
            sink.bill(false, v(p[0], g + 0.06D + 0.08D * k, p[2]), 0.08D + 0.16D * k, (double)j, 8, BLOOD_HI[0], BLOOD_HI[1], BLOOD_HI[2], 0.7D * (1.0D - k) * alpha);

            for(int m = 0; m < 2; ++m) {
               double sx = (m == 1 ? 0.14D : -0.14D) * k;
               double sy = 0.32D * k - 0.36D * k * k;
               sink.bill(false, v(p[0] + sx, g + 0.06D + sy, p[2] - sx * 0.5D), 0.04D, 0.0D, 8, BLOOD_HI[0], BLOOD_HI[1], BLOOD_HI[2], 0.8D * (1.0D - k) * alpha);
            }
         }
      }

   }

   public static double wingGrow(double age, double life) {
      double left = life - age;
      return Math.min(age, 24.0D) - Math.max(0.0D, 24.0D - left) * 1.4D;
   }

   public static double wingAlpha(double age, double life) {
      return SaltMath.clamp01((life - age) / 6.0D);
   }

   public static void rise(SaltShape.Sink sink, double[] p, double g, double age) {
      double k = SaltMath.clamp01(age / 20.0D);
      sink.ring(true, v(p[0], g + 0.05D, p[2]), 0.5D + 3.0D * k, 0.4D, 2, new double[]{0.9D, 0.86D, 0.82D}, 0.7D * (1.0D - k));

      for(int i = 0; i < 10; ++i) {
         sink.bill(false, v(p[0] + Math.cos((double)i) * (0.5D + 2.0D * k), g + 0.2D + k * 0.4D, p[2] + Math.sin((double)i) * (0.5D + 2.0D * k)), 0.3D + 0.4D * k, (double)i, 1, 1.0D, 1.0D, 1.0D, 0.5D * (1.0D - k));
      }

   }

   public static void horn(SaltShape.Sink sink, double[] mouth, double[] hand, double age, double life) {
      double a = Math.min(1.0D, Math.min(age / 4.0D, (life - age) / 6.0D));
      double[] dir = SaltMath.norm(SaltMath.add(SaltMath.sub(hand, mouth), v(0.0D, 0.9D, 0.0D)));
      double[][] pts = new double[11][];
      double[] ws = new double[11];
      double[] w2 = new double[11];
      double[] a1 = new double[11];
      double[] a2 = new double[11];

      for(int i = 0; i <= 10; ++i) {
         double s = (double)i / 10.0D;
         pts[i] = SaltMath.add(mouth, SaltMath.add(SaltMath.mul(dir, s * 1.4D), v(0.0D, -0.12D * Math.sin(Math.PI * s), 0.0D)));
         ws[i] = 0.05D + 0.45D * Math.pow(s, 2.5D);
         w2[i] = ws[i] * 0.5D;
         a1[i] = a * 0.9D;
         a2[i] = a * 0.7D;
      }

      sink.chain(true, pts, ws, 2, GOLDLT, a1);
      sink.chain(true, pts, w2, 2, new double[]{1.0D, 1.0D, 1.0D}, a2);
      sink.bill(true, pts[10], 0.6D, 0.0D, 1, 1.0D, 0.92D, 0.7D, 0.7D * a);
   }

   public static void trumpet(SaltShape.Sink sink, double[] c, double age) {
      double radius = 15.0D;
      double k = SaltMath.outCubic(age / 16.0D);
      double fade = SaltMath.clamp01(1.0D - (age - 14.0D) / 24.0D);
      sink.ring(true, v(c[0], c[1] + 0.1D, c[2]), radius * k, 0.8D, 2, LIGHT, 0.95D * fade);
      sink.ring(true, v(c[0], c[1] + 0.12D, c[2]), radius * k * 0.96D, 0.3D, 2, new double[]{1.0D, 1.0D, 1.0D}, 0.8D * fade);
      sink.upright(true, v(c[0], c[1], c[2]), 2.4D, 40.2D, 3, LIGHT[0], LIGHT[1], LIGHT[2], 0.5D * fade);

      for(int i = 0; i < 10; ++i) {
         double a = (double)i * 0.628D + 0.3D;
         double[] foot = v(c[0] + Math.cos(a) * radius * k * 0.8D, c[1], c[2] + Math.sin(a) * radius * k * 0.8D);
         sink.ribbon(true, SaltMath.add(foot, v(Math.cos(a) * -6.0D, 24.0D, Math.sin(a) * -6.0D)), foot, 0.5D, 3, LIGHT[0], LIGHT[1], LIGHT[2], 0.35D * fade);
      }

   }

   public static void smite(SaltShape.Sink sink, double[] at, double age) {
      double k = SaltMath.clamp01(age / 14.0D);
      sink.bill(true, at, 1.2D * (1.0D - k * 0.5D), 0.0D, 1, 1.0D, 1.0D, 1.0D, 0.9D * (1.0D - k));
      sink.upright(true, v(at[0], at[1] - 1.0D, at[2]), 0.8D, 6.0D, 3, 1.0D, 0.98D, 0.9D, 0.7D * (1.0D - k));
   }

   public static void erupt(SaltShape.Sink sink, double[] p, double[][] fr, double age) {
      if (!(age < 8.0D)) {
         double k = SaltMath.clamp01((age - 8.0D) / 30.0D);

         for(int i = 0; i < 26; ++i) {
            double b = (double)i * 0.242D;
            double sp = 0.18D + 0.2D * hash3(i, 2, 4);
            double[] dir = SaltMath.norm(SaltMath.add(SaltMath.add(fr[1], SaltMath.mul(fr[2], 0.6D + Math.sin(b) * 0.8D)), SaltMath.mul(fr[3], Math.cos(b) * 0.9D)));
            double la = age - 8.0D;
            double[] q = SaltMath.add(fr[0], SaltMath.add(SaltMath.mul(dir, sp * la), v(0.0D, -0.025D * la * la, 0.0D)));
            if (!(q[1] < p[1])) {
               sink.bill(false, q, 0.12D, b, 8, BLOOD[0], BLOOD[1], BLOOD[2], 0.95D * (1.0D - k));
            }
         }

         sink.bill(true, fr[0], 1.8D * (1.0D - k) + 0.4D, 0.0D, 1, 1.0D, 0.3D, 0.35D, 0.5D * (1.0D - k));
      } else {
         double k0 = age / 8.0D;

         for(int i = 0; i < 12; ++i) {
            double a = (double)i * 0.524D;
            sink.bill(false, v(p[0] + Math.cos(a) * 2.5D * (1.0D - k0), p[1] + 0.2D + 1.2D * k0, p[2] + Math.sin(a) * 2.5D * (1.0D - k0)), 0.15D, a, 0, SALT[0], SALT[1], SALT[2], k0);
         }

      }
   }

   public static void blast(SaltShape.Sink sink, double[] c, double age) {
      double k = SaltMath.clamp01(age / 34.0D);
      double r0 = 18.0D;
      double grow = SaltMath.outCubic(age / 12.0D);
      sink.ring(true, v(c[0], c[1] + 0.08D, c[2]), r0 * grow, 0.9D, 2, new double[]{1.0D, 0.9D, 0.86D}, 0.85D * (1.0D - k));
      sink.ring(false, v(c[0], c[1] + 0.06D, c[2]), r0 * grow * 0.92D, 1.6D, 1, new double[]{1.0D, 1.0D, 1.0D}, 0.5D * (1.0D - k));

      for(int i = 0; i < 24; ++i) {
         double a = (double)i * 0.262D;
         double r = r0 * grow * (0.8D + 0.2D * hash3(i, 1, 1));
         sink.bill(false, v(c[0] + Math.cos(a) * r, c[1] + 0.6D + hash3(i, 2, 1) * 1.5D, c[2] + Math.sin(a) * r), 1.2D + 1.5D * k, a, 1, 1.0D, 1.0D, 1.0D, 0.6D * (1.0D - k));
      }

      sink.decal(1, c, 4.5D, 0.4D, 0.9D * (1.0D - k * 0.5D));
      sink.decal(0, c, 6.5D * SaltMath.outCubic(age / 10.0D), 1.1D, 0.8D * (1.0D - k * 0.4D));
   }

   public static void blink(SaltShape.Sink sink, double[] at, double age) {
      double k = SaltMath.clamp01(age / 16.0D);

      for(int i = 0; i < 16; ++i) {
         double a = (double)i * 0.393D;
         double el = (hash3(i, 3, 3) - 0.3D) * 1.4D;
         double[] p = v(at[0] + Math.cos(a) * 0.12D * age, at[1] + el * 0.1D * age - 0.006D * age * age, at[2] + Math.sin(a) * 0.12D * age);
         sink.bill(false, p, 0.07D, a, 0, SALT[0], SALT[1], SALT[2], 1.0D - k);
      }

      sink.bill(false, at, 0.6D + 1.4D * k, 0.0D, 1, 1.0D, 1.0D, 1.0D, 0.7D * (1.0D - k));
      sink.bill(true, at, 0.9D * (1.0D - k), age * 0.2D, 0, 1.0D, 0.5D, 0.5D, 0.8D * (1.0D - k));
   }

   public static double[] rakeAt(double[] feet, double yaw) {
      return SaltMath.add(feet, SaltMath.add(v(0.0D, 1.2D, 0.0D), SaltMath.mul(SaltMath.fwd(yaw), 1.3D)));
   }

   public static void rake(SaltShape.Sink sink, double[] c, double yaw, double age) {
      double k = age / 8.0D;
      double[] r = SaltMath.right(yaw);

      for(int i = 0; i < 3; ++i) {
         double[] o = SaltMath.add(c, v(0.0D, (double)(i - 1) * 0.3D, 0.0D));
         sink.ribbon(true, SaltMath.add(o, SaltMath.mul(r, 1.0D - 2.0D * k)), SaltMath.add(o, SaltMath.mul(r, 0.4D - 2.0D * k)), 0.08D, 6, 1.0D, 0.85D, 0.85D, 0.9D * (1.0D - k));
      }

   }

   public static void howl(SaltShape.Sink sink, double[] at, double age) {
      double k = SaltMath.clamp01(age / 26.0D);

      for(int j = 0; j < 3; ++j) {
         double kk = SaltMath.clamp01((age - (double)(j * 4)) / 18.0D);
         if (!(kk <= 0.0D) && !(kk >= 1.0D)) {
            double r = 10.0D * SaltMath.outCubic(kk);
            sink.ring(true, v(at[0], at[1] + 1.4D, at[2]), r, 0.35D, 2, new double[]{1.0D, 0.86D, 0.86D}, 0.7D * (1.0D - kk));
            sink.ring(false, v(at[0], at[1] + 0.1D, at[2]), r, 1.2D, 1, new double[]{1.0D, 1.0D, 1.0D}, 0.45D * (1.0D - kk));
         }
      }

      sink.decal(1, at, 2.6D, 0.9D, 0.8D * (1.0D - k));
   }

   public static int thornCount(int ring) {
      return 6 + ring * 5;
   }

   public static double[] thornSpot(double[] at, int ring, int i) {
      int n = thornCount(ring);
      double r = (double)ring * 8.0D / 4.0D;
      double a = (double)i / (double)n * Math.PI * 2.0D + (double)ring * 0.4D;
      double rr = r + (hash3(i, ring, 4) - 0.5D) * 0.8D;
      return v(at[0] + Math.cos(a) * rr, a, at[2] + Math.sin(a) * rr);
   }

   public static void thorns(SaltShape.Sink sink, double[] at, double[][] bases, double age) {
      double fall = SaltMath.smooth(32.0D, 50.0D, age);
      int idx = 0;

      for(int ring = 1; ring <= 4; ++ring) {
         int n = thornCount(ring);
         double la = age - (double)ring * 1.2D;
         if (la < 0.0D) {
            idx += n;
         } else {
            for(int i = 0; i < n; ++i) {
               double[] b = bases[idx++];
               double a = (double)i / (double)n * Math.PI * 2.0D + (double)ring * 0.4D;
               double h = (1.0D + 1.4D * hash3(i, ring, 7)) * SaltMath.outCubic(la / 2.0D) * (1.0D - 0.6D * fall);
               spike(sink, b, h, a, 1.0D - fall, v(Math.cos(a) * 0.35D, 0.0D, Math.sin(a) * 0.35D));
            }
         }
      }

      sink.decal(0, at, 8.0D * SaltMath.outCubic(age / 5.0D), 0.3D, 0.85D * (1.0D - fall));
   }

   public static int rendSteps() {
      return (int)Math.floor(46.15384615384615D);
   }

   public static double[] rendSpot(double[] at, double[] dir, double[] side, int k2, int l2) {
      double d = (double)k2 * 1.3D + 0.6D;
      return SaltMath.add(at, SaltMath.add(SaltMath.mul(dir, d + (hash3(k2, l2, 7) - 0.5D) * 0.6D), SaltMath.mul(side, (double)l2 * 1.1D + (hash3(k2, l2, 8) - 0.5D) * 0.4D)));
   }

   public static void rend(SaltShape.Sink sink, double[] at, double[] dir, double[] side, double age, double frontGround, double[][] spots, double[] stripY) {
      double reach = Math.min(60.0D, 2.0D * age);
      boolean running = 2.0D * age <= 60.0D;
      double fade = running ? 1.0D : SaltMath.clamp01(1.0D - (age - 30.0D) / 14.0D);
      double[] front = SaltMath.add(at, SaltMath.mul(dir, reach));
      double g0 = frontGround;
      double hh = 50.0D;
      if (fade > 0.0D) {
         for(int i = 0; i < 12; ++i) {
            double y0 = (double)i * hh / 12.0D;
            double y1 = (double)(i + 1) * hh / 12.0D;
            double lag0 = Math.pow(y0 / hh, 1.5D) * 26.0D;
            double lag1 = Math.pow(y1 / hh, 1.5D) * 26.0D;
            double w0 = 9.0D * (1.0D - 0.8D * y0 / hh);
            double w1 = 9.0D * (1.0D - 0.8D * y1 / hh);
            double[] p0 = SaltMath.sub(front, SaltMath.mul(dir, lag0));
            double[] p1 = SaltMath.sub(front, SaltMath.mul(dir, lag1));
            double al = fade * (0.95D - 0.6D * ((double)i / 12.0D));
            double[] a0 = v(p0[0], g0 + y0, p0[2]);
            double[] b0 = SaltMath.sub(a0, SaltMath.mul(dir, w0));
            double[] a1 = v(p1[0], g0 + y1, p1[2]);
            double[] b1 = SaltMath.sub(a1, SaltMath.mul(dir, w1));
            double v0 = (double)i / 12.0D;
            double v1 = (double)(i + 1) / 12.0D;
            sink.slice(false, b0, a0, a1, b1, 9, SALT[0], SALT[1], SALT[2], al * 0.85D, v0, v1);
            sink.slice(true, b0, a0, a1, b1, 5, LIGHT[0], LIGHT[1], LIGHT[2], al * 0.35D, v0, v1);
            double[] s0 = SaltMath.mul(side, 1.2D * (1.0D - y0 / hh));
            double[] s1 = SaltMath.mul(side, 1.2D * (1.0D - y1 / hh));
            sink.slice(false, SaltMath.sub(a0, s0), SaltMath.add(a0, s0), SaltMath.add(a1, s1), SaltMath.sub(a1, s1), 9, SALT[0], SALT[1], SALT[2], al * 0.4D, v0, v1);
            sink.ribbon(true, a0, a1, 0.5D * (1.0D - 0.6D * y0 / hh), 3, 1.0D, 0.98D, 0.94D, al * 0.9D);
         }

         sink.bill(true, v(front[0], g0 + 2.0D, front[2]), 4.5D, 0.0D, 1, LIGHT[0], LIGHT[1], LIGHT[2], 0.5D * fade);
         int fa = (int)Math.floor(age);

         for(int j = 0; j < 10; ++j) {
            double[] q = SaltMath.add(front, SaltMath.add(SaltMath.mul(side, (hash3(j, fa, 1) - 0.5D) * 4.0D), SaltMath.mul(dir, -hash3(j, fa, 2) * 2.0D)));
            sink.bill(false, v(q[0], g0 + 0.5D + hash3(j, 3, fa) * 3.0D, q[2]), 1.0D + hash3(j, 4, 1), (double)j, 1, 1.0D, 1.0D, 1.0D, 0.55D * fade);
         }
      }

      int steps = (int)Math.floor(reach / 1.3D);

      for(int k2 = 0; k2 < steps; ++k2) {
         double d = (double)k2 * 1.3D + 0.6D;
         double born = d / 2.0D;
         double la = age - born;
         double life = 1.0D - SaltMath.smooth(30.0D, 46.0D, la);
         if (!(life <= 0.0D)) {
            for(int l2 = -1; l2 <= 1; ++l2) {
               double[] bp = spots[k2 * 3 + l2 + 1];
               double h = (1.4D + 1.8D * hash3(k2, l2, 9) * (l2 == 0 ? 1.4D : 1.0D)) * SaltMath.outCubic(la / 2.0D) * (0.4D + 0.6D * life);
               spike(sink, bp, h, (double)k2 * 1.7D + (double)l2, life, SaltMath.mul(SaltMath.add(dir, SaltMath.mul(side, (double)l2 * 0.6D)), 0.15D));
            }

            double[] s0 = SaltMath.add(at, SaltMath.mul(dir, d - 0.65D));
            double[] s1 = SaltMath.add(at, SaltMath.mul(dir, d + 0.65D));
            s0[1] = stripY[k2];
            s1[1] = stripY[k2];
            sink.strip(0, s0, s1, 3.6D, 0.9D * life);
         }
      }

   }

   public static final class Limb {
      public final double[][] pts;
      public final double w0;
      public final double w1;
      public final int depth;
      public final int idx;
      public final boolean tip;

      Limb(double[][] pts, double w0, double w1, int depth, int idx, boolean tip) {
         this.pts = pts;
         this.w0 = w0;
         this.w1 = w1;
         this.depth = depth;
         this.idx = idx;
         this.tip = tip;
      }
   }

   public interface Sink {
      void bill(boolean var1, double[] var2, double var3, double var5, int var7, double var8, double var10, double var12, double var14);

      void quad(boolean var1, double[] var2, double[] var3, double[] var4, double[] var5, int var6, double var7, double var9, double var11, double var13);

      void slice(boolean var1, double[] var2, double[] var3, double[] var4, double[] var5, int var6, double var7, double var9, double var11, double var13, double var15, double var17);

      void ribbon(boolean var1, double[] var2, double[] var3, double var4, int var6, double var7, double var9, double var11, double var13);

      void decal(int var1, double[] var2, double var3, double var5, double var7);

      void strip(int var1, double[] var2, double[] var3, double var4, double var6);

      void chain(boolean var1, double[][] var2, double[] var3, int var4, double[] var5, double[] var6);

      void ring(boolean var1, double[] var2, double var3, double var5, int var7, double[] var8, double var9);

      void upright(boolean var1, double[] var2, double var3, double var5, int var7, double var8, double var10, double var12, double var14);
   }
}
