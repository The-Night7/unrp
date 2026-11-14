package com.myriam.unrp.uo;



import java.util.ArrayList;
import java.util.List;

public final class SarcophagePaths {
   public static final double[] GRAB_ROOT = new double[]{-3.29D, 12.16D, 3.3D};
   public static final double[] BEHIND = new double[]{-3.8D, 13.52D, 7.69D};
   public static final double[] BEHIND_LEG = new double[]{-3.37D, -1.55D, 0.0D};
   public static final double[] BEHIND_ARM = new double[]{0.0D, 5.81D, 4.75D};
   public static final double[] FLANK = new double[]{-11.25D, 13.82D, 12.89D};
   public static final double[] FLANK_ARM = new double[]{0.0D, -0.63D, 1.36D};
   public static final double[] FORE = new double[]{-19.38D, 11.99D, -1.97D};
   public static final double[] FORE_ARM = new double[]{0.0D, 0.0D, -2.4D};
   public static final double WRAP_MIN = 4.0D;
   public static final double WRAP_MAX = 16.0D;
   public static final double WRAP_SPAN = 26.0D;
   public static final double PRE = 5.5D;
   public static final int MIN_REACH_SEGS = 4;
   public static final int MAX_REACH_SEGS = 18;
   public static final double SEG = 6.0D;
   public static final double TIP = 4.6D;
   public static final int WRAP_SEGS = 2;

   private SarcophagePaths() {
   }

   public static List<double[]> curve(double[][] pts, int per) {
      int n = pts.length;
      double[][] p = new double[n + 2][];
      p[0] = SarcMath.sub(SarcMath.scale(pts[0], 2.0D), pts[1]);

      for(int i = 0; i < n; ++i) {
         p[i + 1] = pts[i];
      }

      p[n + 1] = SarcMath.sub(SarcMath.scale(pts[n - 1], 2.0D), pts[n - 2]);
      List<double[]> out = new ArrayList();

      for(int j = 1; j < p.length - 2; ++j) {
         double[] p0 = p[j - 1];
         double[] p1 = p[j];
         double[] p2 = p[j + 1];
         double[] p3 = p[j + 2];
         double t0 = 0.0D;
         double t1 = t0 + Math.max(0.001D, Math.sqrt(SarcMath.dist(p0, p1)));
         double t2 = t1 + Math.max(0.001D, Math.sqrt(SarcMath.dist(p1, p2)));
         double t3 = t2 + Math.max(0.001D, Math.sqrt(SarcMath.dist(p2, p3)));

         for(int k = 0; k < per; ++k) {
            double tt = t1 + (t2 - t1) * (double)k / (double)per;
            double[] a1 = SarcMath.lerp3(p0, p1, (tt - t0) / (t1 - t0));
            double[] a2 = SarcMath.lerp3(p1, p2, (tt - t1) / (t2 - t1));
            double[] a3 = SarcMath.lerp3(p2, p3, (tt - t2) / (t3 - t2));
            double[] b1 = SarcMath.lerp3(a1, a2, (tt - t0) / (t2 - t0));
            double[] b2 = SarcMath.lerp3(a2, a3, (tt - t1) / (t3 - t1));
            out.add(SarcMath.lerp3(b1, b2, (tt - t1) / (t2 - t1)));
         }
      }

      out.add((double[])pts[n - 1].clone());
      return out;
   }

   public static double length(List<double[]> p) {
      double l = 0.0D;

      for(int i = 1; i < p.size(); ++i) {
         l += SarcMath.dist((double[])p.get(i - 1), (double[])p.get(i));
      }

      return l;
   }

   public static List<double[]> arc(double[] start, double[] dir, double[] k, double len, double step) {
      List<double[]> out = new ArrayList();
      out.add((double[])start.clone());
      double[] d = SarcMath.norm(dir);
      double kk = SarcMath.len(k);
      double[] axis = kk > 1.0E-6D ? SarcMath.scale(k, 1.0D / kk) : null;
      double[] p = (double[])start.clone();

      for(double s = step; s <= len + 1.0E-6D; s += step) {
         if (axis != null) {
            d = turn(d, axis, kk * step);
         }

         p = SarcMath.add(p, SarcMath.scale(d, step));
         out.add((double[])p.clone());
      }

      return out;
   }

   private static double[] turn(double[] v, double[] axis, double angle) {
      double c = Math.cos(angle);
      double s = Math.sin(angle);
      double[] cr = SarcMath.cross(axis, v);
      double d = SarcMath.dot(axis, v);
      return new double[]{v[0] * c + cr[0] * s + axis[0] * d * (1.0D - c), v[1] * c + cr[1] * s + axis[1] * d * (1.0D - c), v[2] * c + cr[2] * s + axis[2] * d * (1.0D - c)};
   }

   public static SarcophagePaths.Chain aimChain(double[] base, List<double[]> poly, double[] lens) {
      SarcophagePaths.Chain ch = new SarcophagePaths.Chain(lens.length);
      double[] rm = SarcMath.identity();
      double[] o = (double[])base.clone();
      int idx = 1;

      for(int s = 0; s < lens.length; ++s) {
         double l = lens[s];
         double[] target = null;

         for(int i = idx; i < poly.size(); ++i) {
            double[] p = (double[])poly.get(i);
            if (SarcMath.dist(p, o) >= l) {
               double[] q = (double[])poly.get(i - 1);
               double ax = p[0] - q[0];
               double ay = p[1] - q[1];
               double az = p[2] - q[2];
               double bx = q[0] - o[0];
               double by = q[1] - o[1];
               double bz = q[2] - o[2];
               double a = ax * ax + ay * ay + az * az;
               double b = bx * ax + by * ay + bz * az;
               double c = bx * bx + by * by + bz * bz - l * l;
               double u = a > 1.0E-9D ? SarcMath.clamp((-b + Math.sqrt(Math.max(0.0D, b * b - a * c))) / a, 0.0D, 1.0D) : 1.0D;
               target = new double[]{q[0] + ax * u, q[1] + ay * u, q[2] + az * u};
               idx = i;
               break;
            }
         }

         if (target == null) {
            double[] e1 = (double[])poly.get(poly.size() - 1);
            double[] e0 = (double[])poly.get(poly.size() - 2);
            double[] ed = SarcMath.sub(e1, e0);
            double el = SarcMath.len(ed);
            if (el < 1.0E-9D) {
               el = 1.0D;
            }

            target = SarcMath.add(o, SarcMath.scale(ed, l / el));
         }

         double[] dv = SarcMath.sub(target, o);
         double dl = SarcMath.len(dv);
         if (dl < 1.0E-9D) {
            dv = new double[]{0.0D, 0.0D, 1.0D};
         } else {
            dv = SarcMath.scale(dv, 1.0D / dl);
         }

         double lx = rm[0] * dv[0] + rm[4] * dv[1] + rm[8] * dv[2];
         double ly = rm[1] * dv[0] + rm[5] * dv[1] + rm[9] * dv[2];
         double lz = rm[2] * dv[0] + rm[6] * dv[1] + rm[10] * dv[2];
         double xr = -Math.asin(SarcMath.clamp(ly, -1.0D, 1.0D));
         double yr = Math.atan2(lx, lz);
         rm = SarcMath.mul(rm, SarcMath.compose(0.0D, 0.0D, 0.0D, xr, yr, 0.0D, 1.0D, 1.0D, 1.0D));
         ch.origin[s] = (double[])o.clone();
         ch.turn[s] = SarcMath.rotOf(rm);
         ch.length[s] = l;
         o = target;
      }

      System.arraycopy(rm, 0, ch.frame, 0, 16);
      return ch;
   }

   public static double[] tipOf(SarcophagePaths.Chain ch) {
      int s = ch.count - 1;
      double[] m = SarcMath.qMatrix(ch.turn[s]);
      double[] d = SarcMath.applyDir(m, new double[]{0.0D, 0.0D, ch.length[s]});
      return SarcMath.add(ch.origin[s], d);
   }

   public static double rollToward(SarcophagePaths.Chain ch, double[] point) {
      int s = ch.count - 1;
      double[] tip = ch.origin[s];
      double nx = point[0] - tip[0];
      double ny = point[1] - tip[1];
      double nz = point[2] - tip[2];
      double[] e = ch.frame;
      double lx = e[0] * nx + e[4] * ny + e[8] * nz;
      double ly = e[1] * nx + e[5] * ny + e[9] * nz;
      return Math.atan2(lx, -ly);
   }

   public static double[][] route(double legR, double armR) {
      double legBack = Math.max(0.0D, legR);
      double armBack = Math.max(0.0D, armR);
      double armFore = Math.max(0.0D, -armR);
      return new double[][]{GRAB_ROOT, SarcMath.add(SarcMath.add(BEHIND, SarcMath.scale(BEHIND_LEG, legBack)), SarcMath.scale(BEHIND_ARM, armBack)), SarcMath.add(FLANK, SarcMath.scale(FLANK_ARM, armBack)), SarcMath.add(FORE, SarcMath.scale(FORE_ARM, armFore))};
   }

   public static SarcophagePaths.Chain hold(double[] target, double wrap) {
      return hold(target, wrap, 0.0D, 0.0D);
   }

   public static SarcophagePaths.Chain hold(double[] target, double wrap, double legR, double armR) {
      double r = SarcMath.clamp(wrap, 4.0D, 16.0D);
      double[][] flank = route(legR, armR);
      double[] last = flank[flank.length - 1];
      double[] a3 = SarcMath.norm(SarcMath.sub(target, last));
      if (SarcMath.len(a3) < 0.5D) {
         a3 = new double[]{0.0D, 0.0D, -1.0D};
      }

      double[] right = SarcMath.cross(a3, new double[]{0.0D, -1.0D, 0.0D});
      if (SarcMath.len(right) < 0.2D) {
         right = new double[]{-1.0D, 0.0D, 0.0D};
      }

      right = SarcMath.norm(right);
      double[] contact = SarcMath.add(target, SarcMath.scale(right, r));
      double[] pre = SarcMath.add(SarcMath.sub(contact, SarcMath.scale(a3, 5.5D)), new double[]{0.0D, 0.6D, 0.0D});
      double[][] ctrl = new double[flank.length + 2][];

      for(int i = 0; i < flank.length; ++i) {
         ctrl[i] = flank[i];
      }

      ctrl[flank.length] = pre;
      ctrl[flank.length + 1] = contact;
      List<double[]> approach = curve(ctrl, 10);
      double la = length(approach);
      double[] e1 = (double[])approach.get(approach.size() - 1);
      double[] e0 = (double[])approach.get(approach.size() - 3);
      double[] dir = SarcMath.sub(e1, e0);
      double[] axis = SarcMath.norm(SarcMath.cross(right, a3));
      List<double[]> end = arc(e1, dir, SarcMath.scale(axis, 1.0D / r), 26.0D, 0.5D);
      List<double[]> poly = new ArrayList(approach);

      for(int i = 1; i < end.size(); ++i) {
         poly.add((double[])end.get(i));
      }

      int n = (int)Math.round(la / 6.0D);
      if (n < 4) {
         n = 4;
      }

      if (n > 18) {
         n = 18;
      }

      double k = la / (6.0D * (double)n);
      double[] lens = new double[n + 2 + 1];

      for(int i = 0; i < n; ++i) {
         lens[i] = 6.0D * k;
      }

      for(int i = 0; i < 2; ++i) {
         lens[n + i] = 6.0D;
      }

      lens[n + 2] = 4.6D;
      return aimChain(GRAB_ROOT, poly, lens);
   }

   public static final class Chain {
      public final int count;
      public final double[][] origin;
      public final double[][] turn;
      public final double[] length;
      public final double[] frame;

      Chain(int count) {
         this.count = count;
         this.origin = new double[count][];
         this.turn = new double[count][];
         this.length = new double[count];
         this.frame = SarcMath.identity();
      }

      public double reach() {
         double s = 0.0D;

         for(double l : this.length) {
            s += l;
         }

         return s;
      }
   }
}
