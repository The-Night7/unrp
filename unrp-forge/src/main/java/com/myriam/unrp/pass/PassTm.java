package com.myriam.unrp.pass;

import java.util.Arrays;

public final class PassTm {
   public static final double[] GREY = new double[]{0.66D, 0.68D, 0.78D};
   public static final double[] COPY_GLINT = PassFx.rgb(16761994);
   public static final double[][] GEARS = new double[][]{{0.95D, -0.55D, 0.15D, 0.55D, 1.0D}, {-0.85D, -1.35D, -0.25D, 0.4D, -1.6D}, {0.55D, -1.8D, -0.45D, 0.3D, 2.3D}};
   public static final int IGNITE_LIFE = 40;
   public static final int STEP_LIFE = 14;
   public static final int STEP_EVERY = 5;
   public static final int REWIND_LIFE = 40;
   public static final int BLINK_LIFE = 30;
   public static final int RECALL_LIFE = 30;
   public static final int STRIKE_LIFE = 40;
   public static final int MARK_LIFE = 600;
   public static final int FREEZE_TAIL = 40;
   public static final int DELAY_WAIT = 59;
   public static final int DELAY_TAIL = 30;
   public static final int FIELD_GROW = 10;
   public static final int FIELD_FADE = 10;
   public static final int HIST_MOST = 60;
   public static final int GHOST_LIFE = 10;
   public static final double GHOST_A = 0.5D;
   public static final int SLOW_LIFE = 14;
   public static final double SLOW_A = 0.32D;
   public static final int ACT_FLARE = 12;
   public static final int ACT_SPIN = 10;
   public static final double SPIN = 0.045D;
   public static final double SPIN_EASE = 0.18D;

   private PassTm() {
   }

   static void flatStrip(PassQuads q, double[] a, double[] b, double w, int cell, double[] col, double al) {
      double dx = b[0] - a[0];
      double dz = b[2] - a[2];
      double l = Math.hypot(dx, dz);
      if (!(l < 1.0E-5D)) {
         double sx = -dz / l * w / 2.0D;
         double sz = dx / l * w / 2.0D;
         q.quad(1, PassMath.v(a[0] - sx, a[1], a[2] - sz), PassMath.v(b[0] - sx, b[1], b[2] - sz), PassMath.v(b[0] + sx, b[1], b[2] + sz), PassMath.v(a[0] + sx, a[1], a[2] + sz), cell, col[0], col[1], col[2], al, (double[])null);
      }
   }

   static void flatChain(PassQuads q, double[][] pts, double[] ws, int cell, double[] col, double[] al) {
      for(int i = 0; i < pts.length - 1; ++i) {
         double[] a = pts[i];
         double[] b = pts[i + 1];
         double dx = b[0] - a[0];
         double dz = b[2] - a[2];
         double l = Math.hypot(dx, dz);
         if (!(l < 1.0E-4D)) {
            double sa = ws[i] / 2.0D;
            double sb = ws[i + 1] / 2.0D;
            double nx = -dz / l;
            double nz = dx / l;
            q.quad(1, PassMath.v(a[0] - nx * sa, a[1], a[2] - nz * sa), PassMath.v(b[0] - nx * sb, b[1], b[2] - nz * sb), PassMath.v(b[0] + nx * sb, b[1], b[2] + nz * sb), PassMath.v(a[0] + nx * sa, a[1], a[2] + nz * sa), cell, col[0], col[1], col[2], new double[]{al[i], al[i + 1], al[i + 1], al[i]}, (double[])null);
         }
      }

   }

   public static void groundClock(PassQuads q, double[] c, double r, PassCast.Pal p, double a, double[] hands, double spin) {
      if (a > 0.003D) {
         int segs = (int)Math.max(32.0D, Math.ceil(r * 10.0D));
         q.decalAt(7, c, r, spin, 0.6D * a, p.main, true);
         q.ring(1, PassMath.v(c[0], c[1] + 0.03D, c[2]), r * 1.03D, 0.06D + r * 0.012D, 32, p.hot, 0.85D * a, segs, (double[])null, 0.0D);
         q.ring(1, PassMath.v(c[0], c[1] + 0.025D, c[2]), r * 1.03D, 0.35D + r * 0.04D, 32, p.main, 0.25D * a, segs, (double[])null, 0.0D);
         double[] e = q.eye;
         double dd = Math.max(0.01D, PassMath.dist(e, c));
         double ha = a * PassMath.clamp01(((e[1] - c[1]) / dd - 0.3D) / 0.3D);
         if (ha > 0.003D && hands != null) {
            for(int i = 0; i < hands.length; ++i) {
               double hh = hands[i];
               double len = r * (i == 0 ? 0.5D : 0.8D);
               double w = i == 0 ? 0.08D + r * 0.02D : 0.05D + r * 0.011D;
               double[] tip = PassMath.v(c[0] + Math.sin(hh) * len, c[1] + 0.045D, c[2] - Math.cos(hh) * len);
               flatStrip(q, PassMath.v(c[0], c[1] + 0.045D, c[2]), tip, w * 2.6D, 32, p.main, 0.5D * ha);
               flatStrip(q, PassMath.v(c[0], c[1] + 0.05D, c[2]), tip, w, 31, p.hot, 0.85D * ha);
            }
         }

         q.pool(c, r * 1.1D, p.main, 0.16D * a);
      }
   }

   public static void airClock(PassQuads q, double[] c, double r, PassCast.Pal p, double a, double hand, double filled) {
      if (a > 0.003D) {
         double[] up = q.up;
         double[] rt = q.right;
         PassFx.glow(q, c, r * 1.8D, p.main, 0.35D * a);
         q.bill(1, c, r, 0.0D, 42, p.hot[0], p.hot[1], p.hot[2], 0.95D * a);
         if (!Double.isNaN(filled)) {
            int n = 36;

            for(int i = 0; i < n; ++i) {
               double u0 = (double)i / (double)n;
               double u1 = (double)(i + 1) / (double)n;
               if (u0 >= filled) {
                  break;
               }

               double a0 = u0 * 6.283D;
               double a1 = Math.min(filled, u1) * 6.283D;
               double[] p0 = PassMath.add(c, PassMath.add(PassMath.mul(up, Math.cos(a0) * r * 0.86D), PassMath.mul(rt, Math.sin(a0) * r * 0.86D)));
               double[] p1 = PassMath.add(c, PassMath.add(PassMath.mul(up, Math.cos(a1) * r * 0.86D), PassMath.mul(rt, Math.sin(a1) * r * 0.86D)));
               q.ribbon(1, p0, p1, r * 0.16D, 32, p.main[0], p.main[1], p.main[2], 0.9D * a, 0.9D * a);
            }
         }

         double[] tip = PassMath.add(c, PassMath.add(PassMath.mul(up, Math.cos(hand) * r * 0.78D), PassMath.mul(rt, Math.sin(hand) * r * 0.78D)));
         q.ribbon(1, c, tip, r * 0.09D, 31, 1.0D, 1.0D, 1.0D, a, a);
         PassFx.star(q, c, r * 0.35D, 0.0D, p.hot, 0.8D * a, false);
      }
   }

   public static void ignite(PassQuads q, PassCast.Pal p, double[] g, double age) {
      double k = PassMath.clamp01(1.0D - (age - 10.0D) / 24.0D);
      double r = 0.4D + 1.6D * PassMath.outCubic(age / 9.0D);
      groundClock(q, PassMath.v(g[0], g[1], g[2]), r, p, k, new double[]{age * 0.35D, age * 0.9D}, age * 0.02D);
      if (age < 8.0D) {
         PassFx.shock(q, PassMath.v(g[0], g[1] + 0.05D, g[2]), 0.5D + 2.8D * PassMath.outCubic(age / 8.0D), 0.3D, p.main, 1.0D - age / 8.0D, (double[])null);
      }

      for(int i = 0; i < 18; ++i) {
         double a0 = (double)i / 18.0D * 6.283D + age * 0.12D;
         double rr = 0.9D - age * 0.02D;
         double ag = age - (double)(i % 6);
         if (!(ag < 0.0D) && !(ag > 20.0D)) {
            double[] pp = PassMath.v(g[0] + Math.cos(a0) * rr, g[1] + 0.2D + ag * 0.09D, g[2] + Math.sin(a0) * rr);
            q.bill(1, pp, 0.07D, 0.0D, 33, p.hot[0], p.hot[1], p.hot[2], 1.0D - ag / 20.0D);
         }
      }

   }

   public static void step(PassQuads q, PassCast.Pal p, double[] at, double age) {
      double k = 1.0D - age / 14.0D;
      q.ring(1, PassMath.v(at[0], at[1] + 0.04D, at[2]), 0.3D + age * 0.07D, 0.12D, 32, p.main, 0.8D * k, 24, (double[])null, 0.0D);
   }

   public static void haste(PassQuads q, PassCast.Pal p, double[] at, double t, double[][] hist, double[] histT, double start, boolean body) {
      int n = hist.length;

      for(int j = 0; body && j < 3; ++j) {
         double lift = 0.4D + (double)j * 0.55D;
         double[][] pts = new double[8][];
         double[] ws = new double[8];
         double[] al = new double[8];
         int m = 0;
         pts[m] = PassMath.add(at, PassMath.v(0.0D, lift, 0.0D));
         ws[m] = 0.2D;
         al[m] = 0.8D;
         ++m;

         for(int i = n - 1; i >= Math.max(0, n - 7); --i) {
            double age = t - histT[i];
            if (histT[i] < start) {
               break;
            }

            pts[m] = PassMath.add(hist[i], PassMath.v(0.0D, lift, 0.0D));
            ws[m] = 0.2D * Math.max(0.0D, 1.0D - age / 15.0D);
            al[m] = 0.8D * Math.max(0.0D, 1.0D - age / 13.0D);
            ++m;
         }

         if (m > 1) {
            q.chain(1, (double[][])Arrays.copyOf(pts, m), Arrays.copyOf(ws, m), 32, p.main, Arrays.copyOf(al, m));
         }
      }

      double[][] gp = new double[22][];
      double[] gw = new double[22];
      double[] ga = new double[22];
      int m = 1;

      for(int i = n - 1; i >= Math.max(0, n - 20); --i) {
         double ag = t - histT[i];
         if (histT[i] < start) {
            break;
         }

         double[] hp = hist[i];
         gp[m] = PassMath.v(hp[0], hp[1] + 0.05D, hp[2]);
         gw[m] = 0.8D * (1.0D - ag / 42.0D) + 0.12D;
         ga[m] = 0.6D * Math.max(0.0D, 1.0D - ag / 40.0D);
         ++m;
      }

      if (m - 1 > 1) {
         gp[0] = PassMath.v(at[0], at[1] + 0.05D, at[2]);
         gw[0] = 0.6D;
         ga[0] = 0.6D;
         double[][] pts = (double[][])Arrays.copyOf(gp, m);
         double[] ws = Arrays.copyOf(gw, m);
         double[] as = Arrays.copyOf(ga, m);
         flatChain(q, pts, ws, 30, p.main, as);
         double[] core = new double[m];

         for(int i = 0; i < m; ++i) {
            core[i] = ws[i] * 0.25D;
         }

         flatChain(q, pts, core, 31, p.hot, as);
      }

   }

   static boolean low(double[] c, double[] n, double r) {
      return c[1] + n[1] * r < c[1] - 0.05D;
   }

   static boolean facing(double[] c, double[] n, double r, double[] eye) {
      double[] pp = PassMath.v(c[0] + n[0] * r, c[1] + n[1] * r, c[2] + n[2] * r);
      return PassMath.dot(n, PassMath.sub(eye, pp)) > 0.0D;
   }

   public static boolean field(PassQuads q, PassCast.Pal p, double[] c, double decelR, double t, double start, double end, PassTm.Dart[] arrows, double pt) {
      double grow = PassMath.outCubic(PassMath.clamp01((t - start) / 10.0D));
      double fade = end >= 0.0D ? PassMath.clamp01(1.0D - (t - end) / 10.0D) : 1.0D;
      if (end >= 0.0D && t - end > 10.0D) {
         return false;
      } else {
         double rr = decelR * (0.2D + 0.8D * grow);
         double a = fade * (0.5D + 0.5D * grow);
         groundClock(q, c, rr, p, 0.75D * a, new double[]{t * 0.004D + 1.1D, t * 0.03D}, t * 0.0015D);
         double[] eye = q.eye;
         double r2 = rr * 0.995D;
         PassFx.shell(q, c, rr, p.main, 0.55D * a, (new PassFx.O()).base(0.015D).rim(0.55D).slices(44.0D).stacks(22.0D).alpha((nx, vv, u) -> low(c, nx, rr) ? 0.0D : 1.0D));
         PassFx.shell(q, c, r2, GREY, 0.5D * a, (new PassFx.O()).layer(3).base(0.14D).rim(0.12D).slices(32.0D).stacks(16.0D).alpha((nx, vv, u) -> low(c, nx, r2) ? 0.0D : (facing(c, nx, r2, eye) ? 1.0D : 0.0D)));
         double pu = (t - start) % 40.0D / 40.0D;
         q.ring(1, PassMath.v(c[0], c[1] + 0.06D, c[2]), rr * pu, 0.3D, 32, p.hot, 0.6D * (1.0D - pu) * a, 64, (double[])null, 0.0D);

         for(PassTm.Dart ar : arrows) {
            double ea = Double.isNaN(ar.enterT) ? -1.0D : t - ar.enterT;
            if (ea >= 0.0D && ea < 16.0D) {
               double u = ea / 16.0D;
               double[][] bs = PassQuads.basis(ar.enterDir);
               double ka = (1.0D - u) * (1.0D - u) * a;
               double sz = 0.5D + 1.9D * PassMath.outCubic(u);
               q.plane(1, ar.enterAt, bs[0], bs[1], sz, ea * 0.05D, 60, p.hot[0], p.hot[1], p.hot[2], 0.9D * ka, sz);
               q.plane(1, ar.enterAt, bs[0], bs[1], sz * 0.55D, -ea * 0.08D, 42, p.main[0], p.main[1], p.main[2], 0.55D * ka, sz * 0.55D);
            }

            if (!Double.isNaN(ar.slowed) && ar.slowed != 0.0D && !(t - ar.slowed > 30.0D)) {
               double[] hp = PassMath.lerp(ar.prev, ar.pos, pt);
               if (!ar.stuck) {
                  q.bill(1, hp, 0.55D, 0.0D, 24, p.main[0], p.main[1], p.main[2], 0.32D * a);
               }

               int n = ar.trail.length + 1;
               if (n >= 2) {
                  double[][] tr = (double[][])Arrays.copyOf(ar.trail, n);
                  tr[n - 1] = hp;
                  double[] ws = new double[n];
                  double[] al = new double[n];

                  for(int i = 0; i < n; ++i) {
                     ws[i] = 0.12D * (double)i / (double)(n - 1);
                     al[i] = 0.8D * (double)i / (double)(n - 1);
                  }

                  q.chain(1, tr, ws, 32, p.hot, al);
               }
            }
         }

         return true;
      }
   }

   public static void rewind(PassQuads q, PassCast.Pal p, double[] c, double age, int past, double born, PassTm.Ghosts gh) {
      double k = PassMath.clamp01(1.0D - (age - 16.0D) / 18.0D);
      groundClock(q, PassMath.v(c[0], c[1], c[2]), 1.5D + 0.3D * PassMath.outCubic(age / 8.0D), p, k, new double[]{-age * 0.6D + 2.0D, -age * 2.4D}, -age * 0.05D);
      double head = PassMath.clamp01(age / 14.0D);
      double[][] pts = new double[41][];
      double[] ws = new double[41];
      double[] al = new double[41];
      int m = 0;

      for(int i = 0; i <= 40; ++i) {
         double u = (double)i / 40.0D;
         if (u > head) {
            break;
         }

         double y = 2.3D - u * 2.2D;
         double an = -u * 14.0D - age * 0.25D;
         double r = 0.75D + 0.1D * Math.sin(u * 9.0D);
         pts[m] = PassMath.v(c[0] + Math.cos(an) * r, c[1] + y, c[2] + Math.sin(an) * r);
         ws[m] = 0.12D + 0.05D * Math.sin(u * 20.0D);
         al[m] = k * (0.35D + 0.65D * u / Math.max(0.01D, head));
         ++m;
      }

      if (m > 1) {
         double[][] ps = (double[][])Arrays.copyOf(pts, m);
         double[] w3 = new double[m];
         double[] a4 = new double[m];

         for(int i = 0; i < m; ++i) {
            w3[i] = ws[i] * 3.0D;
            a4[i] = al[i] * 0.4D;
         }

         q.chain(1, ps, w3, 32, p.main, a4);
         q.chain(1, ps, Arrays.copyOf(ws, m), 31, p.hot, Arrays.copyOf(al, m));
      }

      for(int g = 0; g < 6 && past > 0; ++g) {
         int idx = (int)Math.max(0.0D, (double)(past - 1) - Math.floor((double)((g + 1) * past) / 6.5D));
         double peak = 2.0D + (double)g * 2.2D;
         double ga = 1.0D - Math.abs(age - peak) / 5.0D;
         if (!(ga <= 0.0D)) {
            gh.at(idx, p.main, 0.55D * ga);
         }
      }

      if (age < 10.0D) {
         PassFx.sparks(q, PassMath.add(c, PassMath.v(0.0D, 1.0D, 0.0D)), 333.0D + born, age, 16, 0.2D, p.hot, 1.0D - age / 10.0D, (new PassFx.O()).life(12.0D).g(-0.01D).up(0.3D));
      }

   }

   public static void blink(PassQuads q, PassCast.Pal p, double[] from, double[] to, double born, double age, PassTm.Ghosts gh) {
      double k = PassMath.clamp01(1.0D - age / 14.0D);
      gh.at(0, p.main, 0.7D * k * k);
      double[] a = PassMath.add(from, PassMath.v(0.0D, 1.0D, 0.0D));
      double[] b = PassMath.add(to, PassMath.v(0.0D, 1.0D, 0.0D));
      if (age < 10.0D) {
         q.ribbon(1, a, b, 0.5D * (1.0D - age / 10.0D), 30, p.main[0], p.main[1], p.main[2], 0.15D, 0.6D * (1.0D - age / 10.0D));
         q.ribbon(1, a, b, 0.08D, 31, p.hot[0], p.hot[1], p.hot[2], 0.0D, 1.0D - age / 10.0D);
      }

      for(int i = 0; i < 26; ++i) {
         double u = PassMath.h((double)i, born, 3.0D);
         double lag = u * 6.0D;
         double ag = age - lag;
         if (!(ag < 0.0D) && !(ag > 16.0D)) {
            double[] pp = PassMath.add(PassMath.lerp(a, b, u), PassMath.v((PassMath.h((double)i, 1.0D, 9.0D) - 0.5D) * 0.6D, (PassMath.h((double)i, 2.0D, 9.0D) - 0.5D) * 1.2D + ag * 0.02D, (PassMath.h((double)i, 3.0D, 9.0D) - 0.5D) * 0.6D));
            q.bill(1, pp, 0.07D, 0.0D, 33, p.hot[0], p.hot[1], p.hot[2], 1.0D - ag / 16.0D);
         }
      }

      double[][] ends = new double[][]{from, to};

      for(int j = 0; j < 2; ++j) {
         double[] g = ends[j];
         double ag = age - (double)(j * 1);
         if (!(ag < 0.0D) && !(ag > 12.0D)) {
            PassFx.shock(q, PassMath.v(g[0], g[1] + 0.05D, g[2]), 0.4D + 1.6D * PassMath.outCubic(ag / 10.0D), 0.25D, p.main, 1.0D - ag / 12.0D, (double[])null);
            if (j == 1 && ag < 5.0D) {
               PassFx.glow(q, PassMath.add(g, PassMath.v(0.0D, 1.0D, 0.0D)), 1.6D * (1.0D - ag / 5.0D), p.main, 1.0D - ag / 5.0D);
            }
         }
      }

   }

   public static void mark(PassQuads q, PassCast.Pal p, double[] pos, double set, double until, double t) {
      double born = t - set;
      double k = PassMath.clamp01(born / 6.0D);
      double left = (until - t) / 600.0D;
      groundClock(q, pos, 0.95D, p, 0.85D * k, new double[]{left * 6.283D, t * 0.105D}, 0.0D);
      if (born < 10.0D) {
         PassFx.shock(q, PassMath.v(pos[0], pos[1] + 0.05D, pos[2]), 0.3D + 1.6D * PassMath.outCubic(born / 10.0D), 0.25D, p.main, 1.0D - born / 10.0D, (double[])null);
      }

      double[] top = PassMath.add(pos, PassMath.v(0.0D, 2.4D, 0.0D));
      q.ribbon(1, PassMath.add(pos, PassMath.v(0.0D, 0.05D, 0.0D)), top, 0.5D, 30, p.main[0], p.main[1], p.main[2], 0.32D * k, 0.0D);
      q.ribbon(1, PassMath.add(pos, PassMath.v(0.0D, 0.05D, 0.0D)), top, 0.06D, 31, p.hot[0], p.hot[1], p.hot[2], 0.7D * k, 0.0D);
   }

   public static void recall(PassQuads q, PassCast.Pal p, double[] from, double[] to, double born, double age, PassTm.Ghosts gh) {
      double k = PassMath.clamp01(1.0D - age / 14.0D);
      gh.at(0, p.main, 0.65D * k * k);
      double[] a = PassMath.add(from, PassMath.v(0.0D, 1.0D, 0.0D));
      double[] b = PassMath.add(to, PassMath.v(0.0D, 1.0D, 0.0D));

      for(int i = 0; i < 30; ++i) {
         double u = PassMath.h((double)i, born, 5.0D);
         double ag = age - (1.0D - u) * 6.0D;
         if (!(ag < 0.0D) && !(ag > 14.0D)) {
            double[] pp = PassMath.add(PassMath.lerp(a, b, PassMath.clamp01(u + ag * 0.04D)), PassMath.v(0.0D, (PassMath.h((double)i, 2.0D, 9.0D) - 0.5D) * 1.2D, 0.0D));
            q.bill(1, pp, 0.07D, 0.0D, 33, p.hot[0], p.hot[1], p.hot[2], 1.0D - ag / 14.0D);
         }
      }

      if (age < 8.0D) {
         q.ribbon(1, a, b, 0.07D, 31, p.hot[0], p.hot[1], p.hot[2], 1.0D - age / 8.0D, 0.2D);
         groundClock(q, to, 0.95D * (1.0D - age / 8.0D), p, 1.0D - age / 8.0D, new double[]{age * 1.2D, -age * 2.0D}, 0.0D);
         PassFx.glow(q, b, 1.8D * (1.0D - age / 8.0D), p.main, 1.0D - age / 8.0D);
      }

      if (age < 12.0D) {
         PassFx.shock(q, PassMath.v(to[0], to[1] + 0.05D, to[2]), 0.4D + 1.8D * PassMath.outCubic(age / 10.0D), 0.25D, p.main, 1.0D - age / 12.0D, (double[])null);
      }

   }

   public static void freeze(PassQuads q, PassCast.Pal p, double[] c, double rr, double start, double until, double t, double age, PassTm.Dart[] held) {
      double dur = until - start;
      double end = age - dur;
      double form = PassMath.clamp01(age / 7.0D);
      double[] eye = q.eye;
      if (end < 0.0D) {
         double front = 1.0D - PassMath.outCubic(form);
         PassFx.shell(q, c, rr, p.main, 0.7D, (new PassFx.O()).base(0.02D).rim(0.75D).slices(44.0D).stacks(22.0D).alpha((n, vvx, u) -> {
            if (low(c, n, rr)) {
               return 0.0D;
            } else {
               double hgt = (c[1] + n[1] * rr - c[1]) / rr;
               return hgt >= front - 0.02D ? 1.0D + 3.0D * Math.exp(-Math.pow((hgt - front) / 0.05D, 2.0D)) : 0.0D;
            }
         }));
         double r2 = rr * 0.99D;
         PassFx.shell(q, c, r2, GREY, 0.55D * form, (new PassFx.O()).layer(3).base(0.18D).rim(0.12D).slices(32.0D).stacks(16.0D).alpha((n, vvx, u) -> low(c, n, r2) ? 0.0D : (facing(c, n, r2, eye) ? 1.0D : 0.0D)));
         groundClock(q, c, rr, p, 0.9D * form, new double[]{0.7D, 4.2D}, 0.0D);
         q.ring(1, PassMath.v(c[0], c[1] + 0.07D, c[2]), rr * (1.0D - front), 0.4D, 32, p.hot, front > 0.02D ? 0.9D : 0.0D, 64, (double[])null, 0.0D);

         for(int i = 0; i < 46; ++i) {
            double az = PassMath.h((double)i, 7.0D, start) * 6.283D;
            double r = Math.sqrt(PassMath.h((double)i, 8.0D, start)) * rr * 0.92D;
            double y = c[1] + 0.3D + PassMath.h((double)i, 9.0D, start) * Math.sqrt(Math.max(0.0D, rr * rr - r * r)) * 0.55D;
            double[] pp = PassMath.v(c[0] + Math.cos(az) * r, y, c[2] + Math.sin(az) * r);
            double tw = 0.55D + 0.45D * Math.sin(t * 0.08D + (double)i * 1.3D);
            q.bill(1, pp, 0.06D + 0.05D * PassMath.h((double)i, 4.0D, 4.0D), (double)i, i % 4 == 0 ? 26 : 33, p.hot[0], p.hot[1], p.hot[2], 0.9D * form * tw);
         }

         for(PassTm.Dart a : held) {
            int n = a.trail.length + 1;
            double[][] tr = (double[][])Arrays.copyOf(a.trail, n);
            tr[n - 1] = a.pos;
            if (n > 1) {
               double[] ws = new double[n];
               double[] al = new double[n];

               for(int j = 0; j < n; ++j) {
                  ws[j] = 0.1D * (double)j / (double)(n - 1);
                  al[j] = 0.8D * (double)j / (double)(n - 1);
               }

               q.chain(1, tr, ws, 32, GREY, al);
            }

            PassFx.glow(q, a.pos, 0.5D, p.main, 0.5D);
         }

         if (age < 10.0D) {
            PassFx.shock(q, PassMath.v(c[0], c[1] + 0.06D, c[2]), 0.5D + rr * PassMath.outCubic(age / 10.0D), 0.6D, p.main, 1.0D - age / 10.0D, (double[])null);
         }
      } else {
         double k = PassMath.clamp01(1.0D - end / 30.0D);
         if (end < 4.0D) {
            PassFx.shell(q, c, rr * (1.0D + 0.04D * end), PassMath.v(1.0D, 1.0D, 1.0D), 1.0D - end / 4.0D, (new PassFx.O()).base(0.12D).rim(1.3D).slices(40.0D).stacks(20.0D).alpha((n, vvx, u) -> low(c, n, rr * (1.0D + 0.04D * end)) ? 0.0D : 1.0D));
         }

         for(int s = 0; s < 70; ++s) {
            double a0 = PassMath.h((double)s, 11.0D, start) * 6.283D;
            double el = Math.asin(PassMath.h((double)s, 12.0D, start));
            double[] dir = PassMath.v(Math.cos(a0) * Math.cos(el), Math.sin(el), Math.sin(a0) * Math.cos(el));
            double[] p0 = PassMath.add(c, PassMath.mul(dir, rr));
            double vv = 0.08D + 0.12D * PassMath.h((double)s, 13.0D, start);
            double[] pp = PassMath.v(p0[0] + dir[0] * vv * end, p0[1] + dir[1] * vv * end - 0.014D * end * end, p0[2] + dir[2] * vv * end);
            if (!(pp[1] < c[1] + 0.05D)) {
               double[][] bs = PassQuads.basis(PassMath.norm(PassMath.v(dir[0] + Math.sin(end * 0.3D + (double)s), dir[1] + 0.4D, dir[2] + Math.cos(end * 0.2D + (double)s))));
               double sz = 0.25D + 0.2D * PassMath.h((double)s, 14.0D, start);
               q.plane(1, pp, bs[0], bs[1], sz, (double)s, 37, p.hot[0], p.hot[1], p.hot[2], 0.9D * k, sz);
            }
         }

         groundClock(q, c, rr * (1.0D + 0.1D * end / 10.0D), p, 0.9D * PassMath.clamp01(1.0D - end / 12.0D), new double[]{0.7D + end * 0.8D, 4.2D + end * 2.5D}, 0.0D);
         PassFx.shock(q, PassMath.v(c[0], c[1] + 0.06D, c[2]), rr * (1.0D + 0.6D * PassMath.outCubic(end / 12.0D)), 0.5D, p.main, PassMath.clamp01(1.0D - end / 14.0D), (double[])null);
      }

   }

   public static void strike(PassQuads q, PassCast.Pal p, double[] from, double[] to, double[] c, double[] dir, double born, double age, boolean ghost, double heldBy, double tick, double t, PassTm.Ghosts gh, boolean core) {
      double k = PassMath.clamp01(1.0D - age / 12.0D);
      if (ghost && age < 12.0D) {
         gh.at(0, p.main, 0.7D * k * k);
      }

      double[] a = PassMath.add(from, PassMath.v(0.0D, 1.0D, 0.0D));
      double[] b = PassMath.add(to, PassMath.v(0.0D, 1.0D, 0.0D));
      if (age < 8.0D) {
         q.ribbon(1, a, b, 0.7D * (1.0D - age / 8.0D), 30, p.main[0], p.main[1], p.main[2], 0.1D, 0.7D * (1.0D - age / 8.0D));
         q.ribbon(1, a, b, 0.1D, 31, p.hot[0], p.hot[1], p.hot[2], 0.0D, 1.0D - age / 8.0D);
      }

      if (core && age < 9.0D) {
         double sk = 1.0D - age / 9.0D;
         double[] up = PassMath.v(0.0D, 1.0D, 0.0D);
         double[] side = PassMath.norm(PassMath.cross(dir, up));
         double[] ax = PassMath.norm(PassMath.add(PassMath.mul(side, 0.8D), PassMath.mul(up, 0.6D)));
         double[] ay = PassMath.norm(PassMath.cross(ax, dir));
         double s1 = 1.6D + age * 0.08D;
         double s2 = 1.9D + age * 0.1D;
         q.plane(1, PassMath.add(c, PassMath.mul(dir, -0.2D)), ax, ay, s1, 0.0D, 46, p.hot[0], p.hot[1], p.hot[2], sk, s1);
         q.plane(1, PassMath.add(c, PassMath.mul(dir, -0.25D)), ax, ay, s2, 0.0D, 46, p.main[0], p.main[1], p.main[2], 0.6D * sk, s2);
      }

      if (core && age < 16.0D) {
         q.bill(1, c, 1.4D + age * 0.03D, born, 53, 1.0D, 0.9D, 0.95D, PassMath.clamp01(1.0D - age / 16.0D));
         PassFx.glow(q, c, 2.2D * k + 0.3D, p.main, k);
      }

      if (core && age < 4.0D) {
         PassFx.star(q, c, 3.0D * (1.0D - age / 4.0D), 0.4D, p.hot, 1.0D - age / 4.0D, true);
      }

      for(int i = 0; i < 22; ++i) {
         double az = PassMath.h((double)i, born, 21.0D) * 6.283D;
         double el = (PassMath.h((double)i, born, 22.0D) - 0.3D) * 1.4D;
         double[] d = PassMath.v(Math.cos(az) * Math.cos(el), Math.sin(el), Math.sin(az) * Math.cos(el));
         double vv = 0.12D + 0.12D * PassMath.h((double)i, born, 23.0D);
         double[] pp = PassMath.v(c[0] + d[0] * vv * age, c[1] + d[1] * vv * age - 0.01D * age * age, c[2] + d[2] * vv * age);
         double[][] bs = PassQuads.basis(PassMath.norm(PassMath.v(d[0] + Math.sin(age * 0.4D + (double)i), d[1] + 0.5D, d[2])));
         double sz = 0.14D + 0.1D * PassMath.h((double)i, born, 24.0D);
         q.plane(1, pp, bs[0], bs[1], sz, (double)i, 37, p.hot[0], p.hot[1], p.hot[2], PassMath.clamp01(1.0D - age / 22.0D), sz);
      }

      if (core && !Double.isNaN(heldBy) && heldBy != 0.0D && tick < heldBy) {
         double hk = PassMath.clamp01((heldBy - t) / 4.0D);
         q.ring(1, c, 1.1D, 0.08D, 32, p.hot, 0.8D * hk, 40, PassMath.norm(PassMath.v(dir[0], 0.0D, dir[2])), t * 0.02D);
         airClock(q, PassMath.add(c, PassMath.v(0.0D, 1.35D, 0.0D)), 0.42D, p, hk, 0.0D, Double.NaN);
      }

   }

   public static void delay(PassQuads q, PassCast.Pal p, PassFx.Ground ground, double[] hand, double[] c, double[] feet, double[] top, double set, double due, double wait, double t, double age, boolean core) {
      if (age < 8.0D && hand != null) {
         q.ribbon(1, hand, c, 0.07D, 31, p.hot[0], p.hot[1], p.hot[2], 1.0D - age / 8.0D, 0.4D * (1.0D - age / 8.0D));
         if (core) {
            PassFx.glow(q, c, 1.2D * (1.0D - age / 8.0D), p.main, 1.0D - age / 8.0D);
         }
      }

      double left = due - t;
      if (left > 0.0D) {
         double u = 1.0D - left / wait;
         double pop = PassMath.outCubic(PassMath.clamp01(age / 5.0D));
         double tk = Math.floor(u * 12.0D) / 12.0D;
         airClock(q, top, 0.5D * pop, p, pop, tk * 6.283D, u);
         if (left < 12.0D) {
            PassFx.glow(q, top, 0.9D, p.hot, (1.0D - left / 12.0D) * (0.5D + 0.5D * Math.sin(t * 2.0D)));
         }

         q.ring(1, PassMath.v(c[0], feet[1] + 0.05D, c[2]), 0.75D, 0.12D, 32, p.main, 0.6D * pop, 28, (double[])null, 0.0D);
      } else {
         double a = -left;
         if (a > 26.0D) {
            return;
         }

         double k = PassMath.clamp01(1.0D - a / 10.0D);
         if (a < 4.0D) {
            airClock(q, top, 0.5D * (1.0D - a / 4.0D), p, 1.0D - a / 4.0D, 0.0D, 1.0D);
         }

         if (core) {
            PassFx.glow(q, c, 2.6D * k + 0.3D, p.main, k);
         }

         if (core && a < 5.0D) {
            PassFx.star(q, c, 3.2D * (1.0D - a / 5.0D), 0.2D, p.hot, 1.0D - a / 5.0D, true);
         }

         double[] g = PassMath.v(c[0], ground.at(c[0], c[2], c[1]), c[2]);
         PassFx.shock(q, PassMath.v(g[0], g[1] + 0.06D, g[2]), 0.4D + 2.8D * PassMath.outCubic(a / 10.0D), 0.4D, p.main, PassMath.clamp01(1.0D - a / 12.0D), (double[])null);
         PassFx.sparks(q, c, 77.0D + set, a, 24, 0.38D, p.hot, 1.0D, (new PassFx.O()).life(18.0D).g(0.03D).up(0.6D));

         for(int i = 0; i < 12; ++i) {
            double an = (double)i / 12.0D * 6.283D;
            double[] pp = PassMath.add(top, PassMath.v(Math.cos(an) * (0.5D + a * 0.12D), Math.sin(an) * (0.5D + a * 0.12D) * 0.4D, Math.sin(an) * 0.1D));
            q.bill(1, pp, 0.12D, (double)i, 37, p.hot[0], p.hot[1], p.hot[2], PassMath.clamp01(1.0D - a / 16.0D));
         }
      }

   }

   public static void gears(PassQuads q, PassCast.Pal p, PassTm.Xf xf, double on, double spin) {
      for(int i = 0; i < GEARS.length; ++i) {
         double[] g = GEARS[i];
         double ang = spin * g[4];
         double cs = Math.cos(ang);
         double sn = Math.sin(ang);
         double sz = g[3] * (0.6D + 0.4D * PassMath.outCubic(on));
         double[] body = p.copy ? PassCast.SMOKE_DARK : PassFx.mix(p.main, PassMath.v(1.0D, 1.0D, 1.0D), 0.12D);
         double[] rim = p.copy ? PassCast.SMOKE_HOT : p.main;
         double[] hot = p.copy ? COPY_GLINT : p.hot;
         double[] ctr = xf.at(g[0], g[1], g[2]);
         PassFx.glow(q, ctr, sz * 2.4D, rim, 0.4D * on);
         double s1 = sz * 1.12D;
         q.quad(1, corner(xf, g, cs, sn, -s1, -s1, 0.06D), corner(xf, g, cs, sn, s1, -s1, 0.06D), corner(xf, g, cs, sn, s1, s1, 0.06D), corner(xf, g, cs, sn, -s1, s1, 0.06D), 41, rim[0], rim[1], rim[2], 0.55D * on, (double[])null);
         q.quad(0, corner(xf, g, cs, sn, -sz, -sz, 0.0D), corner(xf, g, cs, sn, sz, -sz, 0.0D), corner(xf, g, cs, sn, sz, sz, 0.0D), corner(xf, g, cs, sn, -sz, sz, 0.0D), 41, body[0], body[1], body[2], 0.95D * on, (double[])null);
         q.quad(1, corner(xf, g, cs, sn, -sz, -sz, -0.01D), corner(xf, g, cs, sn, sz, -sz, -0.01D), corner(xf, g, cs, sn, sz, sz, -0.01D), corner(xf, g, cs, sn, -sz, sz, -0.01D), 41, hot[0], hot[1], hot[2], 0.35D * on, (double[])null);
         double ga = spin * 3.0D + (double)i * 2.1D;
         double[] tip = xf.at(g[0] + Math.cos(ga) * sz * 0.92D, g[1] + Math.sin(ga) * sz * 0.92D, g[2] - 0.02D);
         PassFx.star(q, tip, 0.16D + 0.08D * Math.sin(spin * 7.0D + (double)i), spin, PassMath.v(1.0D, 1.0D, 1.0D), 0.8D * on, false);
      }

   }

   static double[] corner(PassTm.Xf xf, double[] g, double cs, double sn, double x, double y, double dz) {
      return xf.at(g[0] + x * cs - y * sn, g[1] + x * sn + y * cs, g[2] + dz);
   }

   public static void grains(PassQuads q, PassCast.Pal p, double[] c, double t, double k) {
      for(int i = 0; i < 8; ++i) {
         double ph = t * 0.035D + (double)i * 0.785D;
         double r = 0.75D + 0.15D * Math.sin(t * 0.05D + (double)i);
         double[] pp = PassMath.v(c[0] + Math.cos(ph) * r, c[1] + 0.3D + (t * 0.02D + (double)i * 0.27D) % 1.0D * 1.8D, c[2] + Math.sin(ph) * r);
         double tw = 0.5D + 0.5D * Math.sin(t * 0.3D + (double)i * 2.3D);
         q.bill(1, pp, 0.07D, t * 0.1D + (double)i, 33, p.hot[0], p.hot[1], p.hot[2], 0.85D * k * tw);
      }

   }

   public static double rateWant(int flow, double sinceAct) {
      double want = flow == 1 ? 4.0D : (flow == 2 ? 0.35D : 1.0D);
      return sinceAct < 10.0D ? Math.max(want, 3.2D) : want;
   }

   public static double[] edge(double[] p, double[] q, double[] c, double r) {
      double[] d = PassMath.sub(q, p);
      double[] f = PassMath.sub(p, c);
      double a = PassMath.dot(d, d);
      double b = 2.0D * PassMath.dot(f, d);
      double cc = PassMath.dot(f, f) - r * r;
      if (!(a < 1.0E-9D) && !(cc <= 0.0D)) {
         double disc = b * b - 4.0D * a * cc;
         if (disc < 0.0D) {
            return (double[])q.clone();
         } else {
            double u = (-b - Math.sqrt(disc)) / (2.0D * a);
            return PassMath.add(p, PassMath.mul(d, PassMath.clamp01(u)));
         }
      } else {
         return (double[])q.clone();
      }
   }

   public static int gray(int abgr) {
      int a = abgr >>> 24;
      int b = abgr >> 16 & 255;
      int g = abgr >> 8 & 255;
      int r = abgr & 255;
      int l = (int)Math.round(0.299D * (double)r + 0.587D * (double)g + 0.114D * (double)b);
      l = Math.max(0, Math.min(255, l));
      return a << 24 | l << 16 | l << 8 | l;
   }

   public static final class Dart {
      public double enterT = Double.NaN;
      public double[] enterAt;
      public double[] enterDir;
      public double slowed = Double.NaN;
      public double[] prev;
      public double[] pos;
      public boolean stuck;
      public double[][] trail = new double[0][];
   }

   public interface Ghosts {
      void at(int var1, double[] var2, double var3);
   }

   public interface Xf {
      double[] at(double var1, double var3, double var5);
   }
}
