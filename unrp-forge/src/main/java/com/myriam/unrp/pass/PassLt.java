package com.myriam.unrp.pass;

public final class PassLt {
   public static final double[] PAT = new double[]{1.0D, 0.25D, 1.0D, 0.85D, 0.45D, 0.2D, 0.08D};
   public static final double[] SMOKE_GRAY = new double[]{0.4D, 0.42D, 0.46D};
   public static final int STRIKE_LIFE = 60;
   public static final int HIT_LIFE = 60;
   public static final int TOUCH_LIFE = 50;
   public static final int SURGE_KEEP = 40;
   public static final double FIRST_FOCUS = 2.5D;
   public static final double THIRD_FOCUS = 1.1D;

   private PassLt() {
   }

   public static void bodyArcs(PassQuads q, double[][] pts, double t, double seed, int n, double[] col, double a, double w) {
      double b = Math.floor(t / 2.0D);

      for(int i = 0; i < n; ++i) {
         int i0 = (int)Math.floor(PassMath.h(seed, b, (double)(i * 2)) * (double)pts.length);
         int i1 = (int)Math.floor(PassMath.h(seed, b, (double)(i * 2 + 1)) * (double)pts.length);
         if (i0 == i1) {
            i1 = (i1 + 1) % pts.length;
         }

         double flick = (Math.floor(t) + (double)i) % 3.0D == 0.0D ? 0.35D : 1.0D;
         PassFx.bolt(q, PassFx.jag(pts[i0], pts[i1], seed * 3.0D + (double)i, b, 3, 0.22D, 1.0D), w == 0.0D ? 0.022D : w, col, a * flick, (new PassFx.O()).noGlow(true));
      }

   }

   public static void strike(PassQuads q, PassCast.Pal p, double[] light, double[] g, double[] head, double age, double[][] body, double t) {
      double[] top = PassMath.v(g[0] + 3.0D, g[1] + 46.0D, g[2] - 2.0D);
      int ai = (int)Math.floor(age);
      if (ai < PAT.length) {
         double a = PAT[ai];
         double[][] main = PassFx.jag(top, head, 11.0D, ai < 3 ? 0.0D : 1.0D, 6, 0.07D, 1.0D);
         PassFx.bolt(q, main, 0.3D, p.main, a, (PassFx.O)null);
         q.chain(1, main, PassFx.fill(main.length, 3.2D), 32, p.main, PassFx.fill(main.length, a * 0.25D));

         for(int k = 0; k < 4; ++k) {
            double[] from = main[(int)Math.floor((double)main.length * (0.2D + 0.18D * (double)k))];
            double[] dirB = PassMath.v(PassMath.h(11.0D, (double)k, 1.0D) - 0.5D, -0.7D - PassMath.h(11.0D, (double)k, 2.0D) * 0.4D, PassMath.h(11.0D, (double)k, 3.0D) - 0.5D);
            double[] to = PassMath.add(from, PassMath.mul(PassMath.norm(dirB), 5.0D + 6.0D * PassMath.h(11.0D, (double)k, 4.0D)));
            PassFx.bolt(q, PassFx.jag(from, to, (double)(31 + k), 0.0D, 4, 0.12D, 1.0D), 0.12D, p.main, a * 0.8D, (new PassFx.O()).taper(true));
         }

         PassFx.glow(q, head, 3.5D, p.main, a);
         PassFx.star(q, PassMath.v(g[0], g[1] + 0.3D, g[2]), 4.5D, 0.2D, p.hot, a, true);
      }

      double k2 = PassMath.clamp01(1.0D - age / 14.0D);
      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.06D, g[2]), 0.5D + 4.5D * PassMath.outCubic(age / 10.0D), 0.6D, p.main, k2, (double[])null);
      q.decalAt(5, g, 2.6D, 0.7D, 0.95D * PassMath.clamp01(1.0D - (age - 45.0D) / 15.0D));
      q.flat(1, PassMath.v(g[0], g[1] + 0.03D, g[2]), 2.6D, 0.7D, 57, p.hot[0], p.hot[1], p.hot[2], 0.9D * PassMath.clamp01(1.0D - age / 22.0D));
      PassFx.sparks(q, PassMath.v(g[0], g[1] + 0.2D, g[2]), 21.0D, age, 26, 0.45D, p.hot, 1.0D, (new PassFx.O()).life(18.0D).g(0.04D).up(0.6D));
      PassFx.smoke(q, light, PassMath.v(g[0], g[1] + 0.3D, g[2]), 23.0D, age - 2.0D, 6, SMOKE_GRAY, 0.45D, (new PassFx.O()).life(40.0D).rise(0.04D).size(0.6D).stagger(2.0D));
      if (body != null && age < 30.0D) {
         bodyArcs(q, body, t, 77.0D, 5, p.hot, PassMath.clamp01(1.0D - age / 30.0D), 0.04D);
      }

      q.pool(g, 6.0D * k2 + 1.5D, p.main, 0.6D * k2 + 0.1D);
   }

   public static void burst(PassQuads q, PassCast.Pal p, double[] light, double[] g, double[] c, double age, double[][] body, double t) {
      int ai = (int)Math.floor(age);
      if (ai < PAT.length) {
         double a = PAT[ai];
         PassFx.forks(q, c, 11.0D, ai < 3 ? 0.0D : 1.0D, 9, 3.4D, 0.07D, p.main, a, (new PassFx.O()).sub(true).ground(g[1]).dark(p.darkOrNull()));
         PassFx.glow(q, c, 3.0D, p.main, a);
         PassFx.star(q, c, 3.6D, 0.2D, p.hot, a, true);
      }

      double k2 = PassMath.clamp01(1.0D - age / 14.0D);
      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.06D, g[2]), 0.5D + 3.5D * PassMath.outCubic(age / 10.0D), 0.5D, p.main, k2, (double[])null);
      q.decalAt(5, g, 1.8D, 0.7D, 0.9D * PassMath.clamp01(1.0D - (age - 45.0D) / 15.0D));
      q.flat(1, PassMath.v(g[0], g[1] + 0.03D, g[2]), 1.8D, 0.7D, 57, p.hot[0], p.hot[1], p.hot[2], 0.8D * PassMath.clamp01(1.0D - age / 22.0D));
      PassFx.sparks(q, c, 21.0D, age, 22, 0.4D, p.hot, 1.0D, (new PassFx.O()).life(16.0D).g(0.04D).up(0.3D));
      PassFx.smoke(q, light, PassMath.v(g[0], g[1] + 0.3D, g[2]), 23.0D, age - 2.0D, 4, SMOKE_GRAY, 0.4D, (new PassFx.O()).life(36.0D).rise(0.04D).size(0.5D).stagger(2.0D));
      if (body != null && age < 30.0D) {
         bodyArcs(q, body, t, 77.0D, 6, p.hot, PassMath.clamp01(1.0D - age / 30.0D), 0.04D);
      }

      q.pool(g, 5.0D * k2 + 1.2D, p.main, 0.55D * k2 + 0.1D);
   }

   public static void bolt(PassQuads q, PassCast.Pal p, PassFx.Ground ground, double[] pos, double[][] hist, double born, double t, double age, double[] hand) {
      PassFx.orb(q, pos, 0.28D, t * 2.0D, p.main, 1.0D, (new PassFx.O()).motes(0.0D).dark(p.darkOrNull()));
      int tn = 1;
      double[][] tail = new double[4][];
      tail[0] = pos;

      for(int i = hist.length - 2; i >= 0 && tn < 4; --i) {
         tail[tn++] = hist[i];
      }

      if (tn > 1) {
         double[] far = tail[tn - 1];
         double[][] pts = PassFx.jag(pos, far, 41.0D + born, Math.floor(t), 4, 0.06D, 1.0D);
         PassFx.bolt(q, pts, 0.08D, p.main, 0.95D, (new PassFx.O()).taper(true).fadeTo(0.1D).dark(p.darkOrNull()));

         for(int k = 0; k < 2; ++k) {
            double[] from = pts[(int)Math.floor((double)pts.length * (0.3D + 0.3D * (double)k))];
            double ft = Math.floor(t);
            double[] side = PassMath.v(PassMath.h(born, ft, (double)(k * 3)) - 0.5D, PassMath.h(born, ft, (double)(k * 3 + 1)) - 0.5D, PassMath.h(born, ft, (double)(k * 3 + 2)) - 0.5D);
            PassFx.bolt(q, PassFx.jag(from, PassMath.add(from, PassMath.mul(PassMath.norm(side), 0.9D)), (double)(51 + k), ft, 2, 0.25D, 1.0D), 0.025D, p.main, 0.8D, (new PassFx.O()).taper(true).noGlow(true));
         }
      }

      double gy = ground.at(pos[0], pos[2], pos[1]);
      if (pos[1] - gy < 4.0D) {
         q.pool(PassMath.v(pos[0], gy, pos[2]), 2.0D, p.main, 0.35D * (1.0D - (pos[1] - gy) / 4.0D));
      }

      if (age < 6.0D) {
         PassFx.star(q, hand, 1.4D * (1.0D - age / 6.0D), age, p.hot, 1.0D - age / 6.0D, true);
         PassFx.glow(q, hand, 0.9D, p.main, 0.8D * (1.0D - age / 6.0D));
      }

   }

   public static void hit(PassQuads q, PassCast.Pal p, double[] light, double[] c, double groundY, double age, double[][] body, double ent, double t) {
      double[] g = PassMath.v(c[0], groundY, c[2]);
      double fl = PassMath.clamp01(1.0D - age / 6.0D);
      PassFx.glow(q, c, 2.6D * (0.5D + 0.5D * fl), p.main, fl);
      PassFx.star(q, c, 3.2D * fl, age * 0.3D, p.hot, fl, true);
      if (age < 7.0D) {
         PassFx.forks(q, c, 61.0D + Math.floor(age / 2.0D), Math.floor(age), 5, 1.6D + age * 0.2D, 0.05D, p.main, 1.0D - age / 7.0D, (new PassFx.O()).sub(true).dark(p.darkOrNull()));
         double[] gp = PassMath.v(g[0], g[1] + 0.05D, g[2]);
         if (c[1] - g[1] < 3.5D) {
            PassFx.bolt(q, PassFx.jag(c, gp, 71.0D, Math.floor(age), 3, 0.2D, 1.0D), 0.05D, p.main, 1.0D - age / 7.0D, (new PassFx.O()).taper(true));
         }
      }

      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.06D, g[2]), 0.4D + 2.2D * PassMath.outCubic(age / 8.0D), 0.35D, p.main, PassMath.clamp01(1.0D - age / 10.0D), (double[])null);
      if (c[1] - g[1] < 3.5D) {
         q.decalAt(5, g, 1.5D, age * 0.0D + 1.1D, 0.9D * PassMath.clamp01(1.0D - (age - 40.0D) / 15.0D));
         q.flat(1, PassMath.v(g[0], g[1] + 0.03D, g[2]), 1.5D, 1.1D, 57, p.hot[0], p.hot[1], p.hot[2], 0.8D * PassMath.clamp01(1.0D - age / 16.0D));
      }

      PassFx.sparks(q, c, 81.0D, age, 18, 0.32D, p.hot, 1.0D, (new PassFx.O()).life(16.0D).g(0.04D));
      PassFx.smoke(q, light, c, 83.0D, age - 1.0D, 4, new double[]{0.45D, 0.47D, 0.52D}, 0.35D, (new PassFx.O()).life(30.0D).rise(0.04D).size(0.4D).stagger(2.0D));
      if (body != null && age < 16.0D) {
         bodyArcs(q, body, t, 90.0D + ent, 3, p.hot, PassMath.clamp01(1.0D - age / 16.0D), 0.022D);
      }

      q.pool(g, 3.0D * fl + 1.0D, p.main, 0.5D * fl + 0.05D);
   }

   public static void touch(PassQuads q, PassCast.Pal p, double[] palm, double[] to, double age, double[][] tgBody, double tgt, double[] mark, double[] user, double t) {
      double k = PassMath.clamp01(1.0D - age / 6.0D);
      if (age < 6.0D) {
         double fa = Math.floor(age);
         double[][] main = PassFx.jag(palm, to, 91.0D, fa, 4, 0.12D, 1.0D);
         PassFx.bolt(q, main, 0.13D, p.main, k, (PassFx.O)null);

         for(int j = 0; j < 3; ++j) {
            double[] from = main[(int)Math.floor((double)main.length * (0.25D + 0.2D * (double)j))];
            double[] to2 = PassMath.add(from, PassMath.mul(PassMath.norm(PassMath.v(PassMath.h((double)j, fa, 3.0D) - 0.5D, PassMath.h((double)j, fa, 4.0D) - 0.3D, PassMath.h((double)j, fa, 5.0D) - 0.5D)), 0.9D));
            PassFx.bolt(q, PassFx.jag(from, to2, (double)(101 + j), fa, 2, 0.25D, 1.0D), 0.03D, p.main, k * 0.8D, (new PassFx.O()).taper(true).noGlow(true));
         }

         PassFx.glow(q, palm, 1.4D, p.main, k);
         PassFx.star(q, palm, 2.2D * k, age * 0.3D, p.hot, k, true);
         PassFx.glow(q, to, 2.0D, p.main, k);
      }

      if (tgBody != null && age < 22.0D) {
         bodyArcs(q, tgBody, t, 120.0D + tgt, 5, p.hot, PassMath.clamp01(1.0D - age / 22.0D), 0.028D);
      }

      PassFx.sparks(q, to, 131.0D, age, 22, 0.35D, p.hot, 1.0D, (new PassFx.O()).life(16.0D).g(0.04D));
      if (mark != null) {
         q.decalAt(5, mark, 1.3D, 0.4D, 0.85D * PassMath.clamp01(1.0D - (age - 35.0D) / 15.0D));
      }

      q.pool(PassMath.add(user, PassMath.v(0.0D, 0.0D, 0.0D)), 2.4D, p.main, 0.5D * k);
   }

   public static double[] tint(PassCast.Pal p, double age) {
      return age < 8.0D ? PassFx.whiter(p.main, 0.5D) : null;
   }

   public static void surge(PassQuads q, PassCast.Pal p, double[][] hist, double[] histT, double end, double[] cur, double t, double[][] body) {
      surge(q, p, hist, histT, end, cur, t, body, 0);
   }

   public static void surge(PassQuads q, PassCast.Pal p, double[][] hist, double[] histT, double end, double[] cur, double t, double[][] body, int base) {
      int n = hist.length;
      double fade = end >= 0.0D ? PassMath.clamp01(1.0D - (t - end) / 8.0D) : 1.0D;
      if (n > 2 && fade > 0.0D) {
         double[][] lines = new double[][]{{0.0D, 1.3D, 0.25D}, {0.0D, 0.95D, -0.2D}, {0.0D, 0.6D, 0.1D}};
         double tb = Math.floor(t / 2.0D);

         for(int li = 0; li < lines.length; ++li) {
            double[] ln = lines[li];
            int lo = Math.max(0, n - 7);
            double[][] pts = new double[1 + (n - lo)][];
            pts[0] = PassMath.add(cur, PassMath.v(0.0D, ln[1], 0.0D));
            int o = 1;

            for(int i = n - 1; i >= lo; --i) {
               int ii = base + i;
               double[] off = PassMath.v((PassMath.h((double)li, tb, (double)ii) - 0.5D) * 0.5D, ln[1] + (PassMath.h((double)(li + 3), tb, (double)ii) - 0.5D) * 0.5D, (PassMath.h((double)(li + 6), tb, (double)ii) - 0.5D) * 0.5D);
               pts[o++] = PassMath.add(hist[i], off);
            }

            PassFx.bolt(q, pts, 0.085D, p.main, 0.95D * fade, (new PassFx.O()).taper(true).fadeTo(0.0D));
         }

         int lo2 = Math.max(0, n - 12);
         double[][] tr = new double[1 + (n - lo2)][];
         double[] tw = new double[tr.length];
         double[] ta = new double[tr.length];
         tr[0] = PassMath.add(cur, PassMath.v(0.0D, 0.95D, 0.0D));
         tw[0] = 0.9D;
         ta[0] = 0.5D * fade;
         int o2 = 1;

         for(int i2 = n - 1; i2 >= lo2; --i2) {
            double kk = (double)(n - 1 - i2) / 12.0D;
            tr[o2] = PassMath.add(hist[i2], PassMath.v(0.0D, 0.95D, 0.0D));
            tw[o2] = 0.9D + kk * 0.6D;
            ta[o2] = 0.5D * fade * (1.0D - kk);
            ++o2;
         }

         q.chain(1, tr, tw, 30, p.main, ta);
         if (body != null) {
            bodyArcs(q, body, t, 141.0D, 4, p.hot, 0.95D * fade, 0.034D);
         }

         q.pool(cur, 2.6D, p.main, 0.4D * fade);
      }

      for(int j = (4 - base % 4) % 4; j < n; j += 4) {
         double ag = t - histT[j];
         if (!(ag > 40.0D)) {
            int jj = base + j;
            double[] g = PassMath.v(hist[j][0], hist[j][1], hist[j][2]);
            q.decalAt(5, g, 0.6D, (double)jj * 1.3D, 0.75D * PassMath.clamp01(1.0D - (ag - 25.0D) / 15.0D));
            if (ag < 10.0D) {
               q.flat(1, PassMath.v(g[0], g[1] + 0.03D, g[2]), 0.6D, (double)jj * 1.3D, 57, p.hot[0], p.hot[1], p.hot[2], 0.9D * (1.0D - ag / 10.0D));
               PassFx.sparks(q, PassMath.v(g[0], g[1] + 0.1D, g[2]), (double)(150 + jj), ag, 5, 0.18D, p.hot, 0.9D, (new PassFx.O()).life(9.0D).g(0.05D).up(0.7D));
            }
         }
      }

   }

   public static void surgeStart(PassQuads q, PassCast.Pal p, double[] g, double[] dir, double age) {
      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.06D, g[2]), 0.5D + 3.0D * PassMath.outCubic(age / 9.0D), 0.4D, p.main, PassMath.clamp01(1.0D - age / 11.0D), (double[])null);
      if (age < 8.0D) {
         q.ring(1, PassMath.add(g, PassMath.add(PassMath.v(0.0D, 1.0D, 0.0D), PassMath.mul(dir, 1.2D + age * 0.4D))), 0.6D + age * 0.25D, 0.15D, 32, p.hot, 0.8D * (1.0D - age / 8.0D), 28, dir, 0.0D);
      }

      if (age < 4.0D) {
         PassFx.forks(q, PassMath.v(g[0], g[1] + 0.2D, g[2]), 160.0D, Math.floor(age), 5, 1.8D, 0.035D, p.main, 1.0D - age / 4.0D, (new PassFx.O()).flat(true));
      }

      q.decalAt(5, g, 1.4D, 0.3D, 0.85D * PassMath.clamp01(1.0D - (age - 15.0D) / 15.0D));
   }

   public static void beam(PassQuads q, PassCast.Pal p, PassFx.Ground ground, double start, double end, double value, double hitD, boolean miss, double[] hitN, double[] eye, double[] dir, double t, boolean fp, double[][] hands) {
      double tb = Math.floor(t);
      double grow = PassMath.clamp01((t - start) / 2.0D);
      double fade = end >= 0.0D ? PassMath.clamp01(1.0D - (t - end) / 3.0D) : 1.0D;
      double fd = fp ? 2.5D : 1.1D;
      double[] focus = PassMath.add(PassMath.add(eye, PassMath.mul(dir, fd)), PassMath.v(0.0D, -0.3D, 0.0D));
      double len = Math.max(0.0D, hitD * (end >= 0.0D ? fade : grow) - fd);
      double[] to = PassMath.add(focus, PassMath.mul(dir, len));

      for(int i = 0; i < hands.length; ++i) {
         double[] hnd = hands[i];
         PassFx.bolt(q, PassFx.jag(hnd, focus, (double)(170 + i), tb, 2, 0.2D, 1.0D), 0.08D, p.main, fade, (new PassFx.O()).noGlow(true));
         PassFx.glow(q, hnd, fp ? 0.4D : 0.9D, p.main, (fp ? 0.5D : 0.9D) * fade);
      }

      if (len > 0.2D) {
         double[][] main = PassFx.jag(focus, to, 180.0D, tb, 5, 0.035D, 1.0D);
         q.ribbon(1, focus, to, fp ? 0.8D : 1.5D, 30, p.main[0], p.main[1], p.main[2], 0.3D * fade, 0.3D * fade);
         PassFx.bolt(q, main, 0.2D, p.main, fade, (new PassFx.O()).dark(p.darkOrNull()));
         double[][] twin = PassFx.jag(focus, to, 190.0D, tb, 5, 0.05D, 1.0D);
         PassFx.bolt(q, twin, 0.09D, p.main, 0.85D * fade, (new PassFx.O()).noGlow(true));
         int nf = (int)Math.min(6.0D, Math.floor(len / 3.0D));

         for(int k = 0; k < nf; ++k) {
            double[] from = main[(int)Math.floor((double)main.length * PassMath.h(tb, (double)k, 7.0D))];
            double[] side = PassMath.norm(PassMath.v(PassMath.h(tb, (double)k, 8.0D) - 0.5D, PassMath.h(tb, (double)k, 9.0D) - 0.5D, PassMath.h(tb, (double)k, 10.0D) - 0.5D));
            PassFx.bolt(q, PassFx.jag(from, PassMath.add(from, PassMath.mul(side, 1.2D + PassMath.h(tb, (double)k, 11.0D))), (double)(200 + k), tb, 3, 0.2D, 1.0D), 0.06D, p.main, 0.85D * fade, (new PassFx.O()).taper(true).noGlow(true));
         }

         for(int qq = 0; qq <= 5; ++qq) {
            double[] qp = PassMath.add(focus, PassMath.mul(dir, len * (double)qq / 5.0D));
            double gy = ground.at(qp[0], qp[2], qp[1]);
            if (qp[1] - gy < 4.0D) {
               q.pool(PassMath.v(qp[0], gy, qp[2]), 1.8D, p.main, 0.25D * fade * (1.0D - (qp[1] - gy) / 4.0D));
            }
         }
      }

      PassFx.glow(q, focus, fp ? 0.45D : 1.2D, p.main, (fp ? 0.35D : 0.7D) * fade);
      if (!miss && value >= 1.0D && fade > 0.05D) {
         double[] hp = PassMath.sub(to, PassMath.mul(dir, 0.1D));
         PassFx.orb(q, hp, 0.45D, t * 3.0D, p.main, fade, (new PassFx.O()).motes(0.0D));
         PassFx.forks(q, hp, 210.0D + tb, tb, 4, 1.4D, 0.03D, p.main, fade, (new PassFx.O()).sub(true).noGlow(true));
         double[] back = hitN != null ? PassMath.norm(PassMath.sub(dir, PassMath.mul(hitN, 2.0D * PassMath.dot(dir, hitN)))) : PassMath.mul(dir, -1.0D);
         PassFx.sparks(q, hp, Math.floor(t / 3.0D) * 5.0D, t % 3.0D + 0.5D, 10, 0.3D, p.hot, fade, (new PassFx.O()).dir(back).cone(1.2D).life(10.0D).g(0.04D));
         double gh = ground.at(hp[0], hp[2], hp[1] + 0.5D);
         if (hp[1] - gh < 3.0D && hitN == null) {
            PassFx.bolt(q, PassFx.jag(hp, PassMath.v(hp[0] + 0.3D, gh + 0.05D, hp[2] - 0.2D), 230.0D, tb, 3, 0.2D, 1.0D), 0.04D, p.main, fade, (new PassFx.O()).taper(true));
            q.decalAt(5, PassMath.v(hp[0], gh, hp[2]), 1.6D, 0.9D, 0.8D);
         }
      }

   }

   public static void passive(PassQuads q, PassCast.Pal p, double[] eyeCol, double on, double flare, boolean surging, double[][] body, double[][] hands, PassBody head, double t, double[] dim) {
      double k = PassMath.clamp01((on - 4.0D) / 6.0D);
      if (!(on < 4.0D)) {
         if (head != null) {
            PassCast.eyesTrailer(q, head, eyeCol, k, flare);
         }

         q.dim = dim;
         if (!surging && body != null) {
            bodyArcs(q, body, t, 300.0D, 2, p.hot, 0.9D * k, 0.026D);
         }

         for(int i = 0; i < hands.length; ++i) {
            double[] hnd = hands[i];
            PassFx.glow(q, hnd, 0.28D * k, p.main, 0.6D * k);
            double t3 = Math.floor(t / 3.0D);
            if ((t3 + (double)i) % 4.0D == 0.0D) {
               PassFx.bolt(q, PassFx.jag(hnd, PassMath.add(hnd, PassMath.v(PassMath.h((double)i, t3, 1.0D) - 0.5D, -0.4D, PassMath.h((double)i, t3, 2.0D) - 0.5D)), (double)(310 + i), Math.floor(t), 2, 0.3D, 1.0D), 0.02D, p.main, 0.8D * k, (new PassFx.O()).taper(true).noGlow(true));
            }
         }

         q.dim = null;
      }
   }

   public static double flareOf(boolean beam, boolean surge) {
      return !beam && !surge ? 0.3D : 1.0D;
   }

   public static double distance(double[] a, double[] b) {
      return PassMath.dist(a, b);
   }
}
