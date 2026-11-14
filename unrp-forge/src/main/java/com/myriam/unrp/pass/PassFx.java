package com.myriam.unrp.pass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PassFx {
   private static final PassFx.O NONE = new PassFx.O();

   private PassFx() {
   }

   static double or(double x, double d) {
      return !Double.isNaN(x) && x != 0.0D ? x : d;
   }

   static double def(double x, double d) {
      return Double.isNaN(x) ? d : x;
   }

   public static double[] rgb(int hex) {
      return PassMath.v((double)(hex >> 16 & 255) / 255.0D, (double)(hex >> 8 & 255) / 255.0D, (double)(hex & 255) / 255.0D);
   }

   public static double[] mix(double[] a, double[] b, double t) {
      return PassMath.v(a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t);
   }

   public static double[] whiter(double[] c, double t) {
      return mix(c, PassMath.v(1.0D, 1.0D, 1.0D), t);
   }

   public static double[] scale(double[] c, double k) {
      return PassMath.v(c[0] * k, c[1] * k, c[2] * k);
   }

   public static double[] fill(int n, double x) {
      double[] a = new double[n];
      Arrays.fill(a, x);
      return a;
   }

   public static void glow(PassQuads q, double[] p, double r, double[] c, double a) {
      q.bill(1, p, r, 0.0D, 24, c[0], c[1], c[2], a);
   }

   public static void star(PassQuads q, double[] p, double r, double spin, double[] c, double a, boolean eight) {
      q.bill(1, p, r, spin, eight ? 27 : 26, c[0], c[1], c[2], a);
   }

   public static void orb(PassQuads q, double[] c, double r, double t, double[] col, double a, PassFx.O o) {
      PassFx.O op = o == null ? NONE : o;
      double[] hot = whiter(col, 0.55D);
      glow(q, c, r * 3.2D, col, 0.32D * a);
      glow(q, c, r * 1.9D, col, 0.65D * a);
      q.bill(1, c, r * 1.25D, t * 0.11D, 49, hot[0], hot[1], hot[2], 0.85D * a);
      q.bill(1, c, r * 1.15D, -t * 0.07D + 1.3D, 49, col[0], col[1], col[2], 0.7D * a);
      if (op.dark != null) {
         q.bill(3, c, r * 1.0D, t * 0.05D, 49, op.dark[0], op.dark[1], op.dark[2], 0.95D * a);
         q.bill(3, c, r * 0.8D, 0.0D, 25, op.dark[0], op.dark[1], op.dark[2], a);
         q.bill(1, c, r * 1.2D, t * 0.04D, 28, hot[0], hot[1], hot[2], 0.8D * a);
      } else {
         q.bill(1, c, r * 0.9D, 0.0D, 25, 1.0D, 1.0D, 1.0D, a);
      }

      if (!op.noFlare) {
         star(q, c, r * 2.6D, t * 0.02D, hot, 0.55D * a, false);
      }

      int n = (int)def(op.motes, 6.0D);

      for(int k = 0; k < n; ++k) {
         double b = t * 0.21D + (double)k * 6.283D / (double)n;
         double[] p = PassMath.add(c, PassMath.v(Math.cos(b) * r * 1.5D, Math.sin(b * 1.7D + (double)k) * r * 0.6D, Math.sin(b) * r * 1.5D));
         q.bill(1, p, 0.05D + 0.03D * r, 0.0D, 33, hot[0], hot[1], hot[2], 0.9D * a);
      }

   }

   public static double[][] jag(double[] a, double[] b, double seed, double tick, int depth, double amp, double hold) {
      double bucket = Math.floor(tick / or(hold, 1.0D));
      List<double[]> pts = new ArrayList();
      pts.add(a);
      pts.add(b);
      double l = PassMath.dist(a, b);
      double[] dir = PassMath.norm(PassMath.sub(b, a));
      double[][] bs = PassQuads.basis(dir);

      for(int d = 0; d < depth; ++d) {
         List<double[]> out = new ArrayList();
         out.add((double[])pts.get(0));

         for(int i = 0; i < pts.size() - 1; ++i) {
            double[] m = PassMath.lerp((double[])pts.get(i), (double[])pts.get(i + 1), 0.5D);
            double s = l * amp / Math.pow(2.0D, (double)d);
            double o1 = (PassMath.h(seed + (double)(d * 31), bucket, (double)(i * 2)) - 0.5D) * 2.0D * s;
            double o2 = (PassMath.h(seed + (double)(d * 31), bucket, (double)(i * 2 + 1)) - 0.5D) * 2.0D * s;
            out.add(PassMath.add(m, PassMath.add(PassMath.mul(bs[0], o1), PassMath.mul(bs[1], o2))));
            out.add((double[])pts.get(i + 1));
         }

         pts = out;
      }

      return (double[][])pts.toArray(new double[0][]);
   }

   public static void bolt(PassQuads q, double[][] pts, double w, double[] col, double a, PassFx.O o) {
      PassFx.O op = o == null ? NONE : o;
      double[] hot = whiter(col, 0.6D);
      int n = pts.length;
      double[] al = new double[n];
      double[] wGlow = new double[n];
      double[] wBody = new double[n];
      double[] wCore = new double[n];

      for(int i = 0; i < n; ++i) {
         double tip = op.taper ? 1.0D - 0.7D * (double)i / (double)(n - 1) : 1.0D;
         al[i] = a * (!Double.isNaN(op.fadeTo) ? PassMath.lerp(1.0D, op.fadeTo, (double)i / (double)(n - 1)) : 1.0D);
         wGlow[i] = w * 6.0D * tip;
         wBody[i] = w * 2.2D * tip;
         wCore[i] = w * 0.9D * tip;
      }

      if (!op.noGlow) {
         q.chain(1, pts, wGlow, 32, col, scaled(al, 0.22D));
      }

      q.chain(1, pts, wBody, 32, col, scaled(al, 0.9D));
      if (op.dark != null) {
         q.chain(3, pts, scaled(wCore, 1.3D), 31, op.dark, scaled(al, 0.95D));
      } else {
         q.chain(1, pts, wCore, 32, hot, al);
      }

   }

   static double[] scaled(double[] a, double k) {
      double[] out = new double[a.length];

      for(int i = 0; i < a.length; ++i) {
         out[i] = a[i] * k;
      }

      return out;
   }

   public static void forks(PassQuads q, double[] from, double seed, double tick, int n, double l, double w, double[] col, double a, PassFx.O o) {
      PassFx.O op = o == null ? NONE : o;

      for(int k = 0; k < n; ++k) {
         double az = PassMath.h(seed, (double)k, 1.0D) * 6.283D;
         double el = (op.down ? -1.0D : 1.0D) * (op.flat ? 0.05D : (PassMath.h(seed, (double)k, 2.0D) - 0.3D) * 1.2D);
         double ln = l * (0.6D + 0.4D * PassMath.h(seed, (double)k, 3.0D));
         double[] dir = op.dir != null ? PassMath.norm(PassMath.add(op.dir, PassMath.v(Math.cos(az) * 0.8D, el * 0.8D, Math.sin(az) * 0.8D))) : PassMath.v(Math.cos(az) * Math.cos(el), Math.sin(el), Math.sin(az) * Math.cos(el));
         double[] to = PassMath.add(from, PassMath.mul(dir, ln));
         if (!Double.isNaN(op.ground) && to[1] < op.ground) {
            to[1] = op.ground + 0.02D;
         }

         double[][] pts = jag(from, to, seed * 7.0D + (double)k, tick, 3, 0.18D, or(op.hold, 1.0D));
         bolt(q, pts, w, col, a, (new PassFx.O()).taper(true).noGlow(op.noGlow).dark(op.dark));
         if (op.sub && ln > 1.2D) {
            double[] mid = pts[(int)Math.floor((double)pts.length / 2.0D)];
            double[] to2 = PassMath.add(mid, PassMath.mul(PassMath.norm(PassMath.add(dir, PassMath.v(PassMath.h(seed, (double)k, 9.0D) - 0.5D, PassMath.h(seed, (double)k, 8.0D) - 0.5D, PassMath.h(seed, (double)k, 7.0D) - 0.5D))), ln * 0.45D));
            bolt(q, jag(mid, to2, seed * 13.0D + (double)k, tick, 2, 0.2D, or(op.hold, 1.0D)), w * 0.6D, col, a * 0.8D, (new PassFx.O()).taper(true).noGlow(true).dark(op.dark));
         }
      }

   }

   public static void beam(PassQuads q, double[] a, double[] b, double t, double[] col, double w, double al, PassFx.O o) {
      PassFx.O op = o == null ? NONE : o;
      double[] hot = whiter(col, 0.65D);
      double l = PassMath.dist(a, b);
      if (!(l < 0.05D)) {
         double[] dir = PassMath.norm(PassMath.sub(b, a));
         double seg = Math.max(0.6D, w * 5.0D);
         double ph = seg - t * or(op.flow, 0.9D) % seg;

         for(double d0 = -ph; d0 < l; d0 += seg) {
            double s0 = Math.max(0.0D, d0);
            double s1 = Math.min(l, d0 + seg);
            if (!(s1 - s0 < 1.0E-4D)) {
               double u0 = (s0 - d0) / seg;
               double u1 = (s1 - d0) / seg;
               double[] p0 = PassMath.add(a, PassMath.mul(dir, s0));
               double[] p1 = PassMath.add(a, PassMath.mul(dir, s1));
               double fa = al * (op.fadeEnd ? 1.0D - 0.6D * s0 / l : 1.0D);
               double fb = al * (op.fadeEnd ? 1.0D - 0.6D * s1 / l : 1.0D);
               double[] su = new double[]{u0, 0.0D, u1, 1.0D};
               q.ribbon(1, p0, p1, w * 3.6D, 30, col[0], col[1], col[2], fa * 0.34D, fb * 0.34D, su);
               q.ribbon(1, p0, p1, w * 1.7D, 30, col[0], col[1], col[2], fa * 0.85D, fb * 0.85D, su);
               if (op.dark != null) {
                  q.ribbon(3, p0, p1, w * 0.95D, 31, op.dark[0], op.dark[1], op.dark[2], fa * 0.95D, fb * 0.95D);
               } else {
                  q.ribbon(1, p0, p1, w * 0.62D, 31, hot[0], hot[1], hot[2], fa, fb);
                  q.ribbon(1, p0, p1, w * 0.26D, 31, 1.0D, 1.0D, 1.0D, fa, fb);
               }
            }
         }

         double[][] bs = PassQuads.basis(dir);
         if (!op.noBraid) {
            double[] bc = mix(col, hot, 0.55D);

            for(int s = 0; s < 2; ++s) {
               int steps = (int)Math.min(120.0D, Math.max(8.0D, Math.ceil(l * 4.0D)));
               double[][] spts = new double[steps + 1][];
               double[] sw = new double[steps + 1];
               double[] sa = new double[steps + 1];

               for(int k = 0; k <= steps; ++k) {
                  double dd = l * (double)k / (double)steps;
                  double ang = dd * 2.4D - t * 0.7D + (double)s * Math.PI;
                  double rr = w * (0.62D + 0.12D * Math.sin(dd * 0.9D + t * 0.3D));
                  spts[k] = PassMath.add(PassMath.add(a, PassMath.mul(dir, dd)), PassMath.add(PassMath.mul(bs[0], Math.cos(ang) * rr), PassMath.mul(bs[1], Math.sin(ang) * rr)));
                  sw[k] = w * 0.22D;
                  sa[k] = al * 0.6D * Math.min(1.0D, dd / 0.8D);
               }

               q.chain(1, spts, sw, 32, bc, sa);
            }

            int nq = (int)Math.min(14.0D, Math.ceil(l / 2.0D));

            for(int qq = 0; qq < nq; ++qq) {
               double ph2 = Math.floor(t / 4.0D);
               double ag = t % 4.0D + (double)qq * 0.3D;
               double fd = l * PassMath.h(ph2, (double)qq, 17.0D);
               double az = PassMath.h(ph2, (double)qq, 18.0D) * 6.283D;
               double[] out = PassMath.add(PassMath.mul(bs[0], Math.cos(az)), PassMath.mul(bs[1], Math.sin(az)));
               double[] sp0 = PassMath.add(PassMath.add(a, PassMath.mul(dir, fd)), PassMath.mul(out, w * 0.5D));
               double[] sp1 = PassMath.add(sp0, PassMath.add(PassMath.mul(out, 0.09D * ag), PassMath.mul(dir, 0.05D * ag)));
               q.ribbon(1, sp0, sp1, 0.04D, 32, hot[0], hot[1], hot[2], 0.0D, al * (1.0D - ag / 5.5D));
            }
         }

         int np = Double.isNaN(op.pulses) ? (int)Math.max(1.0D, Math.floor(l / 5.0D)) : (int)op.pulses;

         for(int k = 0; k < np; ++k) {
            double pd = (t * 1.6D + (double)k * l / (double)np) % l;
            double[] pp = PassMath.add(a, PassMath.mul(dir, pd));
            q.ring(1, pp, w * 2.1D, w * 0.4D, 32, hot, al * 0.8D * (1.0D - pd / l), 20, dir, t * 0.3D);
         }

         if (!op.noMuzzle) {
            glow(q, a, w * 5.0D, col, 0.6D * al);
            star(q, a, w * 6.0D, t * 0.05D, hot, 0.8D * al, true);
            q.ring(1, PassMath.add(a, PassMath.mul(dir, 0.35D)), w * 2.4D, w * 0.5D, 32, hot, 0.5D * al, 20, dir, -t * 0.2D);
         }

      }
   }

   public static void shock(PassQuads q, double[] c, double r, double w, double[] col, double a, double[] n) {
      double[] hot = whiter(col, 0.5D);
      q.ring(1, c, r, w, 32, col, a * 0.55D, 0, n, 0.0D);
      q.ring(1, c, r + w * 0.3D, w * 0.28D, 32, hot, a, 0, n, 0.0D);
   }

   public static void shell(PassQuads q, double[] c, double r, double[] col, double a, PassFx.O o) {
      PassFx.O op = o == null ? NONE : o;
      double[] eye = q.eye;
      int slices = (int)or(op.slices, 24.0D);
      int stacks = (int)or(op.stacks, 14.0D);
      double base = def(op.base, 0.06D);
      double rim = def(op.rim, 0.9D);
      PassQuads.AlphaOf extra = op.alpha;
      q.sphere(op.layer >= 0 ? op.layer : 1, c, r, slices, stacks, op.cell >= 0 ? op.cell : 40, col, (n, vv, u) -> {
         double[] p = PassMath.v(c[0] + n[0] * r, c[1] + n[1] * r, c[2] + n[2] * r);
         double[] vd = PassMath.norm(PassMath.sub(eye, p));
         double f = 1.0D - Math.abs(PassMath.dot(n, vd));
         double al = a * (base + rim * f * f * f);
         if (extra != null) {
            al *= extra.at(n, vv, u);
         }

         return al;
      }, op.rep, def(op.lat0, 0.0D), def(op.lat1, 1.0D));
   }

   public static void sparks(PassQuads q, double[] at, double seed, double age, int n, double spd, double[] col, double a, PassFx.O o) {
      PassFx.O op = o == null ? NONE : o;
      double grav = def(op.g, 0.035D);
      double life = or(op.life, 18.0D);
      double up = def(op.up, 0.35D);

      for(int i = 0; i < n; ++i) {
         double li = life * (0.6D + 0.4D * PassMath.h(seed, (double)i, 5.0D));
         if (!(age > li)) {
            double az = PassMath.h(seed, (double)i, 1.0D) * 6.283D;
            double el = (PassMath.h(seed, (double)i, 2.0D) - 0.5D) * 2.0D * def(op.spread, 1.2D) + up;
            double cone = or(op.cone, 0.6D);
            double[] dir = op.dir != null ? PassMath.norm(PassMath.add(op.dir, PassMath.v(Math.cos(az) * Math.cos(el) * cone, Math.sin(el) * cone, Math.sin(az) * Math.cos(el) * cone))) : PassMath.v(Math.cos(az) * Math.cos(el), Math.sin(el), Math.sin(az) * Math.cos(el));
            double vv = spd * (0.4D + 0.6D * PassMath.h(seed, (double)i, 3.0D));
            double tt0 = Math.max(0.0D, age - 1.6D);
            double[] p1 = PassMath.v(at[0] + dir[0] * vv * age, at[1] + dir[1] * vv * age - grav * age * age * 0.5D, at[2] + dir[2] * vv * age);
            double[] p0 = PassMath.v(at[0] + dir[0] * vv * tt0, at[1] + dir[1] * vv * tt0 - grav * tt0 * tt0 * 0.5D, at[2] + dir[2] * vv * tt0);
            double k = 1.0D - age / li;
            double wk = or(op.w, 1.0D);
            q.ribbon(1, p0, p1, 0.05D * wk, 32, col[0], col[1], col[2], 0.0D, a * k);
            q.bill(1, p1, 0.06D * wk, 0.0D, 33, col[0], col[1], col[2], a * k);
         }
      }

   }

   public static void debris(PassQuads q, PassFx.Ground ground, double[] light, double[] at, double seed, double age, int n, double spd, double[] col, PassFx.O o) {
      if (!q.air) {
         PassFx.O op = o == null ? NONE : o;

         for(int i = 0; i < n; ++i) {
            double az = PassMath.h(seed, (double)i, 11.0D) * 6.283D;
            double el = 0.5D + 0.9D * PassMath.h(seed, (double)i, 12.0D);
            double vv = spd * (0.4D + 0.6D * PassMath.h(seed, (double)i, 13.0D));
            double vx = Math.cos(az) * Math.cos(el) * vv;
            double vy = Math.sin(el) * vv * or(op.lift, 1.0D);
            double vz = Math.sin(az) * Math.cos(el) * vv;
            double g = 0.04D;
            double tl = (vy + Math.sqrt(vy * vy + 2.0D * g * 0.2D)) / g;
            double tt = Math.min(age, tl);
            double[] p = PassMath.v(at[0] + vx * tt, at[1] + vy * tt - g * tt * tt * 0.5D, at[2] + vz * tt);
            double gy = ground.at(p[0], p[2], p[1] + 1.0D) + 0.08D;
            if (p[1] < gy) {
               p[1] = gy;
            }

            double k = PassMath.clamp01(1.0D - (age - or(op.lie, 30.0D)) / 10.0D);
            if (!(k <= 0.0D)) {
               double sz = or(op.size, 0.16D) * (0.6D + 0.8D * PassMath.h(seed, (double)i, 14.0D));
               double[] c = scale(col, 0.75D + 0.35D * PassMath.h(seed, (double)i, 15.0D));
               q.bill(0, p, sz, PassMath.h(seed, (double)i, 16.0D) * 6.0D + (age < tl ? age * 0.4D : tl * 0.4D), 23, c[0] * light[0], c[1] * light[1], c[2] * light[2], k);
            }
         }

      }
   }

   public static void smoke(PassQuads q, double[] light, double[] at, double seed, double age, int n, double[] col, double a, PassFx.O o) {
      PassFx.O op = o == null ? NONE : o;
      if (!op.raised || !q.air) {
         double life = or(op.life, 40.0D);
         double rise = def(op.rise, 0.05D);
         double spread = or(op.spread, 0.06D);

         for(int i = 0; i < n; ++i) {
            double t0 = or(op.stagger, 0.0D) * (double)i;
            double ag = age - t0;
            if (!(ag < 0.0D) && !(ag > life)) {
               double u = ag / life;
               double az = PassMath.h(seed, (double)i, 21.0D) * 6.283D;
               double sp = spread * (0.5D + PassMath.h(seed, (double)i, 22.0D));
               double[] p = PassMath.v(at[0] + Math.cos(az) * sp * ag, at[1] + rise * ag * (0.6D + 0.6D * PassMath.h(seed, (double)i, 23.0D)), at[2] + Math.sin(az) * sp * ag);
               double size = or(op.size, 0.4D) * (0.6D + 1.6D * u);
               double[] c = scale(col, 1.0D - 0.3D * u);
               q.bill(0, p, size, PassMath.h(seed, (double)i, 24.0D) * 6.0D + ag * 0.02D, 35, c[0] * light[0], c[1] * light[1], c[2] * light[2], a * (1.0D - u) * Math.min(1.0D, ag / 3.0D));
            }
         }

      }
   }

   public static void embers(PassQuads q, double[] at, double seed, double age, int n, double[] col, double a, PassFx.O o) {
      PassFx.O op = o == null ? NONE : o;
      double life = or(op.life, 30.0D);
      double rad = or(op.r, 0.6D);

      for(int i = 0; i < n; ++i) {
         double li = life * (0.5D + 0.5D * PassMath.h(seed, (double)i, 31.0D));
         double ag = age - or(op.stagger, 0.0D) * (double)i;
         if (!(ag < 0.0D) && !(ag > li)) {
            double az = PassMath.h(seed, (double)i, 32.0D) * 6.283D;
            double rr = rad * PassMath.h(seed, (double)i, 33.0D);
            double[] p = PassMath.v(at[0] + Math.cos(az) * rr + Math.sin(ag * 0.2D + (double)i) * 0.08D, at[1] + ag * or(op.rise, 0.05D) * (0.5D + PassMath.h(seed, (double)i, 34.0D)), at[2] + Math.sin(az) * rr);
            double fl = 0.6D + 0.4D * Math.sin(ag * 0.9D + (double)i * 1.7D);
            q.bill(1, p, or(op.size, 0.07D), 0.0D, 63, col[0], col[1], col[2], a * fl * (1.0D - ag / li));
         }
      }

   }

   public static void crackLine(PassQuads q, double[] a, double[] b, double w, double[] col, double glowA, double decalA) {
      q.decalStrip(3, a, b, w, decalA, PassMath.v(1.0D, 1.0D, 1.0D), (double[])null);
      if (glowA > 0.0D) {
         double[] a1 = PassMath.v(a[0], a[1] + 0.03D, a[2]);
         double[] b1 = PassMath.v(b[0], b[1] + 0.03D, b[2]);
         double[] d = PassMath.sub(b1, a1);
         d[1] = 0.0D;
         double l = PassMath.len(d);
         if (l < 1.0E-6D) {
            return;
         }

         double[] side = PassMath.v(-d[2] / l * w * 0.5D, 0.0D, d[0] / l * w * 0.5D);
         q.quad(1, PassMath.sub(a1, side), PassMath.sub(b1, side), PassMath.add(b1, side), PassMath.add(a1, side), 32, col[0], col[1], col[2], glowA, new double[]{0.0D, 0.0D, 1.0D, 1.0D});
      }

   }

   public interface Ground {
      double at(double var1, double var3, double var5);
   }

   public static final class O {
      public double[] dark;
      public double[] dir;
      public boolean noFlare;
      public boolean taper;
      public boolean noGlow;
      public boolean down;
      public boolean flat;
      public boolean sub;
      public boolean fadeEnd;
      public boolean noBraid;
      public boolean noMuzzle;
      public boolean raised;
      public double motes = Double.NaN;
      public double fadeTo = Double.NaN;
      public double ground = Double.NaN;
      public double hold = Double.NaN;
      public double flow = Double.NaN;
      public double pulses = Double.NaN;
      public double g = Double.NaN;
      public double life = Double.NaN;
      public double up = Double.NaN;
      public double spread = Double.NaN;
      public double cone = Double.NaN;
      public double w = Double.NaN;
      public double lift = Double.NaN;
      public double lie = Double.NaN;
      public double size = Double.NaN;
      public double rise = Double.NaN;
      public double stagger = Double.NaN;
      public double r = Double.NaN;
      public double base = Double.NaN;
      public double rim = Double.NaN;
      public double slices = Double.NaN;
      public double stacks = Double.NaN;
      public double lat0 = Double.NaN;
      public double lat1 = Double.NaN;
      public int layer = -1;
      public int cell = -1;
      public double[] rep;
      public PassQuads.AlphaOf alpha;

      public PassFx.O dark(double[] x) {
         this.dark = x;
         return this;
      }

      public PassFx.O dir(double[] x) {
         this.dir = x;
         return this;
      }

      public PassFx.O noFlare(boolean x) {
         this.noFlare = x;
         return this;
      }

      public PassFx.O taper(boolean x) {
         this.taper = x;
         return this;
      }

      public PassFx.O noGlow(boolean x) {
         this.noGlow = x;
         return this;
      }

      public PassFx.O down(boolean x) {
         this.down = x;
         return this;
      }

      public PassFx.O flat(boolean x) {
         this.flat = x;
         return this;
      }

      public PassFx.O sub(boolean x) {
         this.sub = x;
         return this;
      }

      public PassFx.O raised(boolean x) {
         this.raised = x;
         return this;
      }

      public PassFx.O fadeEnd(boolean x) {
         this.fadeEnd = x;
         return this;
      }

      public PassFx.O noBraid(boolean x) {
         this.noBraid = x;
         return this;
      }

      public PassFx.O noMuzzle(boolean x) {
         this.noMuzzle = x;
         return this;
      }

      public PassFx.O motes(double x) {
         this.motes = x;
         return this;
      }

      public PassFx.O fadeTo(double x) {
         this.fadeTo = x;
         return this;
      }

      public PassFx.O ground(double x) {
         this.ground = x;
         return this;
      }

      public PassFx.O hold(double x) {
         this.hold = x;
         return this;
      }

      public PassFx.O flow(double x) {
         this.flow = x;
         return this;
      }

      public PassFx.O pulses(double x) {
         this.pulses = x;
         return this;
      }

      public PassFx.O g(double x) {
         this.g = x;
         return this;
      }

      public PassFx.O life(double x) {
         this.life = x;
         return this;
      }

      public PassFx.O up(double x) {
         this.up = x;
         return this;
      }

      public PassFx.O spread(double x) {
         this.spread = x;
         return this;
      }

      public PassFx.O cone(double x) {
         this.cone = x;
         return this;
      }

      public PassFx.O w(double x) {
         this.w = x;
         return this;
      }

      public PassFx.O lift(double x) {
         this.lift = x;
         return this;
      }

      public PassFx.O lie(double x) {
         this.lie = x;
         return this;
      }

      public PassFx.O size(double x) {
         this.size = x;
         return this;
      }

      public PassFx.O rise(double x) {
         this.rise = x;
         return this;
      }

      public PassFx.O stagger(double x) {
         this.stagger = x;
         return this;
      }

      public PassFx.O r(double x) {
         this.r = x;
         return this;
      }

      public PassFx.O base(double x) {
         this.base = x;
         return this;
      }

      public PassFx.O rim(double x) {
         this.rim = x;
         return this;
      }

      public PassFx.O slices(double x) {
         this.slices = x;
         return this;
      }

      public PassFx.O stacks(double x) {
         this.stacks = x;
         return this;
      }

      public PassFx.O lat(double a, double b) {
         this.lat0 = a;
         this.lat1 = b;
         return this;
      }

      public PassFx.O layer(int x) {
         this.layer = x;
         return this;
      }

      public PassFx.O cell(int x) {
         this.cell = x;
         return this;
      }

      public PassFx.O rep(double[] x) {
         this.rep = x;
         return this;
      }

      public PassFx.O alpha(PassQuads.AlphaOf x) {
         this.alpha = x;
         return this;
      }
   }
}
