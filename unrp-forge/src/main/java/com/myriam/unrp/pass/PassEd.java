package com.myriam.unrp.pass;

public final class PassEd {
   public static final double BEAM_RANGE = 32.0D;
   public static final double BEAM_EXTEND = 0.5D;
   public static final int WAVE_CHARGE = 10;
   public static final double WAVE_RANGE = 32.0D;
   public static final double WAVE_RADIUS = 1.5D;
   public static final double WAVE_EXTEND = 0.25D;
   public static final double WAVE_SHRINK = 0.34D;
   public static final double SPHERE_SPEED = 1.5D;
   public static final double SPHERE_GRAVITY = 0.01D;
   public static final double SPHERE_DRAG = 0.99D;
   public static final int SPHERE_LIFE = 100;
   public static final int SPHERE_HIST = 12;
   public static final int BURST_DELAY = 5;
   public static final double BURST_REACH = 12.0D;
   public static final double PROPEL_SPEED = 2.5D;
   public static final int MARKS_MOST = 160;
   public static final int WAVE_KEEP = 70;
   public static final int BLAST_LIFE = 90;
   public static final int GATHER_LIFE = 7;
   public static final int BURST_LIFE = 70;
   public static final int LAUNCH_LIFE = 40;
   public static final int CONTRAIL_LIFE = 60;
   public static final int CONTRAIL_HIST = 34;
   public static final int LAND_LIFE = 40;
   public static final int IGNITE_LIFE = 24;
   public static final int HURL_LIFE = 14;
   public static final double[] PALM_SMOKE = new double[]{0.42D, 0.4D, 0.4D};
   public static final double[] DUST = new double[]{0.5D, 0.45D, 0.4D};
   public static final double[] FEET_DUST = new double[]{0.52D, 0.47D, 0.42D};
   public static final double[] HIT_SMOKE = new double[]{0.3D, 0.27D, 0.26D};
   public static final double[] FIZZLE = new double[]{0.3D, 0.28D, 0.28D};
   public static final double[] BEAM_SMOKE = new double[]{0.25D, 0.22D, 0.22D};
   public static final double[] BLAST_SMOKE = new double[]{0.24D, 0.21D, 0.2D};
   public static final double[] EARTH = new double[]{0.42D, 0.33D, 0.24D};
   public static final double[] LAND_EARTH = new double[]{0.4D, 0.32D, 0.24D};
   public static final double[] PUFF = new double[]{0.55D, 0.5D, 0.46D};
   public static final double[] RING_DUST = new double[]{0.52D, 0.47D, 0.42D};

   private PassEd() {
   }

   static double[] side(PassBody b, int part, double x) {
      return b.at(part, x, 4.5D, 0.0D);
   }

   static double[] wrist(PassBody b, int part, double x) {
      return b.at(part, x, 11.5D, 0.0D);
   }

   public static void laser(PassQuads q, double[] a, double[] b, double t, PassCast.Pal p, double al, boolean fp) {
      double l = PassMath.dist(a, b);
      if (!(l < 0.05D) && al > 0.01D) {
         double[] dir = PassMath.norm(PassMath.sub(b, a));
         double w = 0.11D;
         if (p.copy) {
            q.ribbon(3, a, b, w * 0.9D, 31, p.dark[0], p.dark[1], p.dark[2], 0.85D * al, 0.7D * al);
            q.ribbon(1, a, b, w * 2.2D, 30, p.main[0], p.main[1], p.main[2], 0.3D * al, 0.2D * al);
         } else {
            q.ribbon(1, a, b, w * 4.2D, 30, p.main[0], p.main[1], p.main[2], 0.26D * al, 0.18D * al);
            q.ribbon(1, a, b, w, 30, p.main[0], p.main[1], p.main[2], 0.95D * al, 0.85D * al);
            q.ribbon(1, a, b, w * 0.42D, 31, p.hot[0], p.hot[1], p.hot[2], al, 0.95D * al);
            q.ribbon(1, a, b, w * 0.16D, 31, 1.0D, 1.0D, 1.0D, al, 0.85D * al);

            for(int k = 0; k < 3; ++k) {
               double d0 = (t * 6.0D + (double)k * l / 3.0D) % l;
               double d1 = Math.min(l, d0 + 1.6D);
               q.ribbon(1, PassMath.add(a, PassMath.mul(dir, d0)), PassMath.add(a, PassMath.mul(dir, d1)), w * 1.6D, 30, p.hot[0], p.hot[1], p.hot[2], 0.0D, 0.75D * al * (1.0D - d0 / l));
            }

            double mz = fp ? 0.45D : 1.0D;
            PassFx.glow(q, a, 0.55D * mz, p.main, 0.85D * al);
            PassFx.star(q, a, 0.75D * mz, t * 0.12D, p.hot, 0.8D * al, false);
            if (!fp) {
               q.ring(1, PassMath.add(a, PassMath.mul(dir, 0.2D)), 0.15D, 0.05D, 32, p.hot, 0.55D * al, 16, dir, t * 0.4D);
            }

         }
      }
   }

   public static void beam(PassQuads q, PassCast.Pal p, PassFx.Ground ground, double[] light, double start, double end, double value, double hitD, boolean miss, double[] n, double[] eye, double[] dir, double t, boolean fp, double[][] palms, PassBody body) {
      double grow = PassMath.clamp01((t - start) / 2.0D);
      double fade = end >= 0.0D ? PassMath.clamp01(1.0D - (t - end) / 3.0D) : 1.0D;
      double[] full = PassMath.add(eye, PassMath.mul(dir, hitD));
      double flick = 0.9D + 0.1D * Math.sin(t * 3.1D) * Math.sin(t * 1.7D);
      int[] arms = new int[]{2, 3};
      double[] sides = new double[]{-1.0D, 1.0D};
      double[][] bds = new double[2][];

      for(int i = 0; i < 2; ++i) {
         double[] palm = palms[i];
         double[] bd = PassMath.norm(PassMath.sub(full, palm));
         double len = PassMath.dist(palm, full) * (end >= 0.0D ? 1.0D : grow);
         double[] to = PassMath.add(palm, PassMath.mul(bd, len));
         laser(q, palm, to, t + (double)i * 0.37D, p, fade * flick, fp);
         bds[i] = bd;
      }

      for(int i = 0; i < 2; ++i) {
         if (!fp && !p.copy) {
            double[] el = side(body, arms[i], sides[i]);
            double[] wr = wrist(body, arms[i], sides[i]);
            q.ribbon(1, el, wr, 0.5D, 30, p.main[0], p.main[1], p.main[2], 0.12D * fade, 0.38D * fade);
            q.ribbon(1, el, wr, 0.2D, 31, p.hot[0], p.hot[1], p.hot[2], 0.05D * fade, 0.3D * fade);
         }

         double st = t - start;
         if (st >= 0.0D && st < 5.0D && !p.copy) {
            double sk = 1.0D - st / 5.0D;
            PassFx.star(q, palms[i], (fp ? 0.8D : 1.7D) * sk, st * 0.3D, p.hot, sk, true);
            q.ring(1, PassMath.add(palms[i], PassMath.mul(bds[i], 0.25D)), 0.2D + 1.1D * PassMath.outCubic(st / 5.0D), 0.12D, 32, p.hot, 0.9D * sk, 24, bds[i], 0.0D);
         }
      }

      double[] mid = PassMath.lerp(palms[0], palms[1], 0.5D);
      double[] md = PassMath.norm(PassMath.sub(full, mid));
      double mlen = PassMath.dist(mid, full) * (end >= 0.0D ? 1.0D : grow);

      for(int k = 0; k <= 5 && mlen > 0.5D && !p.copy; ++k) {
         double[] qq = PassMath.add(mid, PassMath.mul(md, mlen * (double)k / 5.0D));
         double gy = ground.at(qq[0], qq[2], qq[1]);
         double hgt = qq[1] - gy;
         if (hgt < 3.0D) {
            q.pool(PassMath.v(qq[0], gy, qq[2]), 1.2D + hgt * 0.2D, p.main, 0.18D * fade * (1.0D - hgt / 3.0D));
         }
      }

      if (!miss && value >= 1.0D && fade > 0.05D) {
         double[] hp = PassMath.sub(full, PassMath.mul(md, 0.12D));
         if (p.copy) {
            q.bill(3, hp, 0.6D, t * 0.2D, 35, p.dark[0], p.dark[1], p.dark[2], 0.6D * fade);
            return;
         }

         PassFx.glow(q, hp, 1.05D, p.main, 0.8D * fade);
         PassFx.star(q, hp, 1.35D, t * 0.09D, p.hot, 0.9D * fade, true);
         double seed = Math.floor(t / 3.0D);
         double[] back = n != null ? PassMath.norm(PassMath.sub(md, PassMath.mul(n, 2.0D * PassMath.dot(md, n)))) : PassMath.mul(md, -1.0D);

         for(int s = 0; s < 2; ++s) {
            PassFx.sparks(q, hp, seed * 7.0D + (double)s, t % 3.0D + (double)s * 1.0D + 0.5D, 6, 0.3D, p.hot, 0.9D * fade, (new PassFx.O()).dir(back).cone(n != null ? 1.3D : 1.1D).life(8.0D).g(0.03D));
         }

         if (n == null) {
            double gy2 = ground.at(hp[0], hp[2], hp[1] + 0.5D);
            if (hp[1] - gy2 < 2.5D) {
               q.decalAt(0, PassMath.v(hp[0], gy2, hp[2]), 0.7D + 0.25D * Math.min(1.0D, (t - start) / 40.0D), 0.4D, 0.6D);
            }

            PassFx.smoke(q, light, hp, Math.floor(t / 20.0D), t % 20.0D + 20.0D, 3, BEAM_SMOKE, 0.3D, (new PassFx.O()).life(26.0D).rise(0.06D).stagger(5.0D));
         }
      }

   }

   public static void stream(PassQuads q, double[] light, double[] at, double seed, double t, double period, int n, double[] col, double a, PassFx.O o) {
      double c = Math.floor(t / period);
      double ag = t - c * period;
      PassFx.smoke(q, light, at, seed * 97.0D + c, ag, n, col, a, o);
      PassFx.smoke(q, light, at, seed * 97.0D + c - 1.0D, ag + period, n, col, a, o);
   }

   public static void waveCharge(PassQuads q, PassCast.Pal p, double[] light, double[] c, double u, double t, boolean fp, double[] g, boolean aloft) {
      double k = PassMath.outCubic(PassMath.clamp01(u));
      PassFx.orb(q, c, 0.08D + 0.3D * k, t, p.main, 0.55D + 0.45D * k, (new PassFx.O()).motes((double)Math.round(2.0D + 6.0D * k)).noFlare(k < 0.5D).dark(p.darkOrNull()));

      for(int i = 0; i < 18; ++i) {
         double az = PassMath.h((double)i, 11.0D, 5.0D) * 6.283D;
         double el = (PassMath.h((double)i, 12.0D, 5.0D) - 0.45D) * 1.6D;
         double cyc = (t * 0.2D + PassMath.h((double)i, 13.0D, 5.0D)) % 1.0D;
         double r = (2.6D + 1.6D * PassMath.h((double)i, 14.0D, 5.0D)) * (1.0D - cyc * cyc);
         double[] dir = PassMath.v(Math.cos(az) * Math.cos(el), Math.sin(el), Math.sin(az) * Math.cos(el));
         double[] p1 = PassMath.add(c, PassMath.mul(dir, r));
         double[] p0 = PassMath.add(c, PassMath.mul(dir, r + 0.7D * (1.0D - cyc)));
         q.ribbon(1, p0, p1, 0.06D, 32, p.hot[0], p.hot[1], p.hot[2], 0.0D, 0.85D * cyc * (0.35D + 0.65D * k));
      }

      if (!fp) {
         for(int j = 0; j < 3; ++j) {
            double b = Math.floor(t / 2.0D);
            double ang = PassMath.h(b, (double)j, 21.0D) * 6.283D;
            double[] to = PassMath.add(c, PassMath.v(Math.cos(ang) * 0.45D * (0.6D + k), (PassMath.h(b, (double)j, 22.0D) - 0.5D) * 0.6D, Math.sin(ang) * 0.45D * (0.6D + k)));
            PassFx.bolt(q, PassFx.jag(c, to, (double)(600 + j), Math.floor(t), 2, 0.3D, 1.0D), 0.02D, p.hot, 0.8D * k, (new PassFx.O()).noGlow(true).taper(true).dark(p.darkOrNull()));
         }
      }

      q.ring(1, PassMath.v(g[0], g[1] + 0.05D, g[2]), 3.6D * (1.0D - k) + 0.7D, 0.22D, 32, p.main, 0.75D * k, 48, (double[])null, 0.0D);
      q.ring(1, PassMath.v(g[0], g[1] + 0.06D, g[2]), 3.6D * (1.0D - k) + 0.7D, 0.07D, 32, p.hot, 0.9D * k, 48, (double[])null, 0.0D);
      if (!aloft) {
         q.pool(g, 2.0D + 2.5D * k, p.main, 0.4D * k);

         for(int s = 0; s < 12; ++s) {
            double a0 = PassMath.h((double)s, 31.0D, 5.0D) * 6.283D + t * 0.14D;
            double ph = (t * 0.06D + PassMath.h((double)s, 32.0D, 5.0D)) % 1.0D;
            double rr = 3.2D * (1.0D - ph);
            double[] qq = PassMath.v(g[0] + Math.cos(a0) * rr, g[1] + 0.12D + 0.18D * PassMath.h((double)s, 33.0D, 5.0D) + ph * 0.25D, g[2] + Math.sin(a0) * rr);
            double dk = 0.5D + 0.1D * PassMath.h((double)s, 35.0D, 5.0D);
            q.bill(0, qq, 0.22D + 0.18D * PassMath.h((double)s, 34.0D, 5.0D), a0, 35, dk * light[0], dk * 0.92D * light[1], dk * 0.86D * light[2], 0.28D * k * Math.sin(Math.PI * ph));
         }

         for(int m = 0; m < 9; ++m) {
            double a1 = PassMath.h((double)m, 41.0D, 5.0D) * 6.283D;
            double d1 = 1.1D + 1.9D * PassMath.h((double)m, 42.0D, 5.0D);
            double lift = (t * 0.035D * (0.6D + PassMath.h((double)m, 43.0D, 5.0D)) + PassMath.h((double)m, 45.0D, 5.0D)) % 1.4D;
            double rock = 0.42D * (0.75D + 0.35D * PassMath.h((double)m, 46.0D, 5.0D));
            q.bill(0, PassMath.v(g[0] + Math.cos(a1) * d1, g[1] + 0.08D + lift, g[2] + Math.sin(a1) * d1), 0.07D + 0.06D * PassMath.h((double)m, 44.0D, 5.0D), t * 0.12D + (double)m, 23, rock * light[0], rock * 0.8D * light[1], rock * 0.6D * light[2], k * PassMath.clamp01((1.4D - lift) / 0.4D));
         }

      }
   }

   public static void waveBody(PassQuads q, double[] a, double[] b, double t, PassCast.Pal p, double wd, double al) {
      double l = PassMath.dist(a, b);
      if (!(l < 0.05D)) {
         double[] dir = PassMath.norm(PassMath.sub(b, a));
         double[] hot = p.hot;
         double seg = wd * 5.0D;
         double ph = seg - t * 1.8D % seg;

         for(double d0 = -ph; d0 < l; d0 += seg) {
            double s0 = Math.max(0.0D, d0);
            double s1 = Math.min(l, d0 + seg);
            if (!(s1 - s0 < 1.0E-4D)) {
               double[] sb = new double[]{(s0 - d0) / seg, 0.0D, (s1 - d0) / seg, 1.0D};
               double[] p0 = PassMath.add(a, PassMath.mul(dir, s0));
               double[] p1 = PassMath.add(a, PassMath.mul(dir, s1));
               q.ribbon(1, p0, p1, wd * 7.0D, 30, p.main[0], p.main[1], p.main[2], 0.16D * al, 0.16D * al, sb);
               q.ribbon(1, p0, p1, wd * 3.4D, 30, p.main[0], p.main[1], p.main[2], 0.7D * al, 0.7D * al, sb);
               if (p.copy) {
                  q.ribbon(3, p0, p1, wd * 1.5D, 31, p.dark[0], p.dark[1], p.dark[2], 0.95D * al, 0.95D * al);
               } else {
                  q.ribbon(1, p0, p1, wd * 1.8D, 30, hot[0], hot[1], hot[2], 0.85D * al, 0.85D * al, sb);
                  q.ribbon(1, p0, p1, wd * 1.1D, 31, 1.0D, 1.0D, 1.0D, al, al);
               }
            }
         }

         double[][] bs = PassQuads.basis(dir);
         double[] bc = PassFx.mix(p.main, hot, 0.55D);

         for(int s = 0; s < 2; ++s) {
            int steps = (int)Math.min(120.0D, Math.max(8.0D, Math.ceil(l * 3.0D)));
            double[][] spts = new double[steps + 1][];
            double[] sw = new double[steps + 1];
            double[] sa = new double[steps + 1];

            for(int k = 0; k <= steps; ++k) {
               double dd = l * (double)k / (double)steps;
               double ang = dd * 1.6D - t * 0.8D + (double)s * Math.PI;
               double rr = wd * (1.0D + 0.12D * Math.sin(dd * 0.7D + t * 0.3D));
               spts[k] = PassMath.add(PassMath.add(a, PassMath.mul(dir, dd)), PassMath.add(PassMath.mul(bs[0], Math.cos(ang) * rr), PassMath.mul(bs[1], Math.sin(ang) * rr)));
               sw[k] = wd * 0.25D;
               sa[k] = al * 0.7D * Math.min(1.0D, dd / 0.8D);
            }

            q.chain(1, spts, sw, 32, bc, sa);
         }

         int nq = (int)Math.min(18.0D, Math.ceil(l / 1.6D));

         for(int k = 0; k < nq; ++k) {
            double ph2 = Math.floor(t / 4.0D);
            double ag = t % 4.0D + (double)k * 0.3D;
            double fd = l * PassMath.h(ph2, (double)k, 17.0D);
            double az = PassMath.h(ph2, (double)k, 18.0D) * 6.283D;
            double[] out = PassMath.add(PassMath.mul(bs[0], Math.cos(az)), PassMath.mul(bs[1], Math.sin(az)));
            double[] sp0 = PassMath.add(PassMath.add(a, PassMath.mul(dir, fd)), PassMath.mul(out, wd * 1.2D));
            double[] sp1 = PassMath.add(sp0, PassMath.add(PassMath.mul(out, 0.14D * ag), PassMath.mul(dir, 0.06D * ag)));
            q.ribbon(1, sp0, sp1, 0.06D, 32, hot[0], hot[1], hot[2], 0.0D, al * (1.0D - ag / 5.5D));
         }

         int np = (int)Math.max(2.0D, Math.floor(l / 6.0D));

         for(int k = 0; k < np; ++k) {
            double pd = (t * 1.6D + (double)k * l / (double)np) % l;
            q.ring(1, PassMath.add(a, PassMath.mul(dir, pd)), wd * 2.0D, wd * 0.36D, 32, hot, al * 0.8D * (1.0D - pd / l), 24, dir, t * 0.3D);
         }

      }
   }

   public static void waveMarks(PassQuads q, double[][] marks, double t, double gone) {
      if (marks != null) {
         for(double[] mk : marks) {
            double mka = PassMath.clamp01((t - mk[3]) / 3.0D) * (gone > 30.0D ? PassMath.clamp01(1.0D - (gone - 30.0D) / 30.0D) : 1.0D);
            q.decalAt(0, PassMath.v(mk[0], mk[1], mk[2]), mk[4], mk[5], 0.62D * mka);
         }

      }
   }

   public static void wave(PassQuads q, PassCast.Pal p, PassFx.Ground ground, double[] light, double press, double start, double end, double[][] marks, double hitD, boolean miss, double[] n, double[] eye, double[] dir, double t, boolean fp, double[][] palms, PassBody body, double[] g, boolean aloft) {
      double[] mid = PassMath.lerp(palms[0], palms[1], 0.5D);
      double gone = end >= 0.0D ? t - end : -1.0D;
      waveMarks(q, marks, t, gone);
      if (start < 0.0D) {
         if (end < 0.0D) {
            waveCharge(q, p, light, mid, (t - press) / 10.0D, t, fp, g, aloft);
         } else if (gone < 12.0D) {
            PassFx.smoke(q, light, mid, 950.0D, gone, 5, FIZZLE, 0.45D, (new PassFx.O()).life(18.0D).rise(0.04D).size(0.25D));
            PassFx.sparks(q, mid, 951.0D, gone, 8, 0.12D, p.hot, 0.8D, (new PassFx.O()).life(9.0D).g(0.02D));
         }

      } else {
         double[] full = PassMath.add(eye, PassMath.mul(dir, hitD));
         double[] bd = PassMath.norm(PassMath.sub(full, mid));
         double lf = PassMath.dist(mid, full);
         double since = t - start;
         double head = gone >= 0.0D ? lf : lf * PassMath.clamp01((since + 1.0D) * 0.25D);
         double tail = gone >= 0.0D ? lf * PassMath.clamp01(gone / 4.0D) : 0.0D;
         double fade = gone >= 0.0D ? PassMath.clamp01(1.0D - gone / 4.0D) : 1.0D;
         if (gone >= 0.0D && gone < 40.0D) {
            for(int i = 0; i < 2; ++i) {
               PassFx.smoke(q, light, palms[i], (double)(960 + i), gone, 6, PALM_SMOKE, 0.4D, (new PassFx.O()).life(30.0D).rise(0.05D).spread(0.02D).size(0.16D).stagger(3.0D));
               PassFx.embers(q, palms[i], (double)(962 + i), gone, 6, p.main, 0.8D, (new PassFx.O()).life(20.0D).r(0.15D).rise(0.04D).stagger(2.0D));
            }
         }

         if (!(head - tail < 0.05D) && !(fade <= 0.0D)) {
            double[] a = PassMath.add(mid, PassMath.mul(bd, tail));
            double[] b = PassMath.add(mid, PassMath.mul(bd, head));
            double pulse = 0.92D + 0.08D * Math.sin(t * 2.3D);
            double wd = 0.85D * (0.88D + 0.12D * Math.sin(t * 1.7D)) * (since < 3.0D ? 0.55D + 0.45D * since / 3.0D : 1.0D);
            waveBody(q, a, b, t, p, wd, fade * pulse);
            if (gone < 0.0D && head < lf - 0.05D) {
               PassFx.glow(q, b, wd * 6.0D, p.main, 0.85D);
               PassFx.star(q, b, wd * 7.0D, t * 0.1D, p.hot, 0.9D, true);
               q.ring(1, b, wd * 2.4D, wd * 0.6D, 32, p.hot, 0.8D, 24, bd, 0.0D);
            }

            if (tail < 0.5D) {
               double rk = PassMath.clamp01(1.0D - since / 6.0D);
               if (rk > 0.0D) {
                  PassFx.star(q, mid, (fp ? 1.4D : 3.2D) * rk * rk, 0.2D, p.hot, rk, true);
                  PassFx.glow(q, mid, (fp ? 1.0D : 2.2D) * rk + 0.6D, p.main, 0.85D * rk);

                  for(int r = 0; r < 2; ++r) {
                     double ru = PassMath.clamp01((since - (double)r * 2.0D) / 7.0D);
                     if (ru > 0.0D && ru < 1.0D) {
                        q.ring(1, PassMath.add(mid, PassMath.mul(bd, 0.6D + ru * 3.5D)), 0.5D + ru * 2.8D, 0.3D * (1.0D - ru) + 0.08D, 32, p.hot, 0.85D * (1.0D - ru), 40, bd, t * 0.2D);
                     }
                  }
               }

               PassFx.glow(q, mid, fp ? 0.5D : 1.1D, p.main, 0.9D * fade);
               if (!fp && !p.copy) {
                  int[] arms = new int[]{2, 3};
                  double[] sides = new double[]{-1.0D, 1.0D};

                  for(int i = 0; i < 2; ++i) {
                     double[] el = side(body, arms[i], sides[i]);
                     double[] wr = wrist(body, arms[i], sides[i]);
                     q.ribbon(1, el, wr, 0.6D, 30, p.main[0], p.main[1], p.main[2], 0.18D * fade, 0.5D * fade);
                     q.ribbon(1, el, wr, 0.24D, 31, p.hot[0], p.hot[1], p.hot[2], 0.08D * fade, 0.4D * fade);
                  }
               }
            }

            for(int k = 0; k <= 12; ++k) {
               double dd = tail + (head - tail) * (double)k / 12.0D;
               double[] qq = PassMath.add(mid, PassMath.mul(bd, dd));
               double gy = ground.at(qq[0], qq[2], qq[1]);
               double hgt = qq[1] - gy;
               if (!(hgt > 3.5D)) {
                  double near = 1.0D - hgt / 3.5D;
                  q.pool(PassMath.v(qq[0], gy, qq[2]), 1.6D + hgt * 0.4D, p.main, 0.32D * fade * near);
                  if (gone < 0.0D && hgt < 2.4D) {
                     stream(q, light, PassMath.v(qq[0], gy + 0.2D, qq[2]), (double)(700 + k), t, 9.0D, 2, DUST, 0.5D * near, (new PassFx.O()).life(18.0D).rise(0.06D).spread(0.07D).size(0.45D));
                  }
               }
            }

            if (gone < 0.0D) {
               double[] back = PassMath.v(g[0] - bd[0] * 0.4D, g[1] + 0.15D, g[2] - bd[2] * 0.4D);
               if (!aloft) {
                  stream(q, light, back, 790.0D, t, 7.0D, 3, FEET_DUST, 0.55D, (new PassFx.O()).life(14.0D).rise(0.02D).spread(0.09D).size(0.4D));
               }

               double rp = t * 0.25D % 1.0D;
               q.ring(1, PassMath.v(g[0], g[1] + 0.05D, g[2]), 0.9D + rp * 1.8D, 0.25D, 32, p.main, 0.5D * (1.0D - rp), 32, (double[])null, 0.0D);
            }

            if (!miss && head >= lf - 0.05D) {
               double[] hp = PassMath.sub(full, PassMath.mul(bd, 0.3D));
               double ip = 0.85D + 0.15D * Math.sin(t * 1.9D);
               PassFx.glow(q, hp, 3.2D * ip, p.main, 0.85D * fade);
               PassFx.star(q, hp, 4.2D * ip, t * 0.07D, p.hot, 0.9D * fade, true);
               double cp = t * 0.3D % 1.0D;
               q.ring(1, hp, 1.2D + cp * 2.4D, 0.3D, 32, p.hot, 0.75D * (1.0D - cp) * fade, 32, PassMath.mul(bd, -1.0D), 0.0D);
               double[] bk = n != null ? PassMath.norm(PassMath.sub(bd, PassMath.mul(n, 2.0D * PassMath.dot(bd, n)))) : PassMath.mul(bd, -1.0D);

               for(int s2 = 0; s2 < 3; ++s2) {
                  PassFx.sparks(q, hp, Math.floor(t / 3.0D) * 5.0D + (double)s2, t % 3.0D + (double)s2, 7, 0.45D, p.hot, 0.9D * fade, (new PassFx.O()).dir(bk).cone(1.4D).life(10.0D).g(0.03D));
               }

               if (gone < 0.0D) {
                  stream(q, light, hp, 820.0D, t, 12.0D, 3, HIT_SMOKE, 0.6D, (new PassFx.O()).life(30.0D).rise(0.08D).spread(0.06D).size(0.8D));
               }

               double gyh = ground.at(hp[0], hp[2], hp[1] + 0.5D);
               if (hp[1] - gyh < 3.0D) {
                  q.decalAt(2, PassMath.v(hp[0], gyh, hp[2]), 1.2D + 1.4D * Math.min(1.0D, since / 30.0D), 0.7D, 0.9D);
               }
            }

         }
      }
   }

   public static void orb(PassQuads q, PassCast.Pal p, PassFx.Ground ground, double[] pos, double[][] hist, double t) {
      PassFx.orb(q, pos, 0.55D, t, p.main, 1.0D, (new PassFx.O()).motes(7.0D).dark(p.darkOrNull()));
      int n = hist.length;
      if (n > 1) {
         double[][] pts = new double[n][];
         double[] ws = new double[n];
         double[] als = new double[n];
         double[] ws2 = new double[n];
         double[] als2 = new double[n];
         int i = n - 1;

         for(int j = 0; i >= 0; ++j) {
            double k = (double)(n - 1 - i) / (double)Math.max(1, n - 1);
            pts[j] = i == n - 1 ? pos : hist[i];
            ws[j] = 0.9D * (1.0D - k) + 0.05D;
            als[j] = 0.85D * (1.0D - k);
            ws2[j] = ws[j] * 1.9D;
            als2[j] = als[j] * 0.45D;
            --i;
         }

         q.chain(1, pts, ws2, 32, p.main, als2);
         q.chain(1, pts, ws, 31, p.hot, als);
      }

      PassFx.sparks(q, pos, Math.floor(t / 2.0D), t % 2.0D + 0.2D, 5, 0.12D, p.hot, 0.8D, (new PassFx.O()).life(8.0D).g(0.05D).up(-0.2D));
      double gy = ground.at(pos[0], pos[2], pos[1]);
      double hgt = pos[1] - gy;
      if (hgt < 5.0D) {
         q.pool(PassMath.v(pos[0], gy, pos[2]), 2.4D + hgt * 0.3D, p.main, 0.4D * (1.0D - hgt / 5.0D));
      }

   }

   public static void hurl(PassQuads q, PassCast.Pal p, double[] hand, double[] at, double[] dir, double age) {
      double k = PassMath.clamp01(1.0D - age / 10.0D);
      PassFx.glow(q, hand, 1.1D * k + 0.3D, p.main, 0.9D * k);
      PassFx.star(q, hand, 1.8D * k, age * 0.2D, p.hot, k, true);
      q.ring(1, PassMath.add(at, PassMath.mul(dir, 0.6D + age * 0.25D)), 0.5D + age * 0.22D, 0.18D, 32, p.hot, 0.8D * k, 24, dir, 0.0D);
      PassFx.sparks(q, hand, 41.0D, age, 10, 0.3D, p.hot, k, (new PassFx.O()).dir(dir).cone(0.5D).life(10.0D).g(0.02D));
   }

   public static void blast(PassQuads q, PassCast.Pal p, PassFx.Ground ground, double[] light, double[] c, double groundY, double age) {
      double[] g = PassMath.v(c[0], groundY, c[2]);
      double fl = PassMath.clamp01(1.0D - age / 7.0D);
      PassFx.glow(q, c, 5.5D * (0.6D + 0.4D * fl), p.main, 0.9D * fl);
      PassFx.star(q, c, 7.0D * fl, 0.3D, p.hot, fl, true);
      double fb = PassMath.clamp01(1.0D - age / 22.0D);
      if (fb > 0.0D) {
         double r = 0.8D + 2.2D * PassMath.outCubic(age / 10.0D);
         q.bill(1, c, r, age * 0.04D, 49, p.hot[0], p.hot[1], p.hot[2], 0.95D * fb);
         q.bill(1, c, r * 0.8D, -age * 0.06D + 1.0D, 49, p.main[0], p.main[1], p.main[2], 0.9D * fb);
         PassFx.glow(q, c, r * 1.8D, p.main, 0.6D * fb);
         if (p.copy) {
            q.bill(3, c, r * 0.85D, age * 0.05D, 49, p.dark[0], p.dark[1], p.dark[2], 0.9D * fb);
         }
      }

      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.06D, g[2]), 1.0D + 5.0D * PassMath.outCubic(age / 12.0D), 0.7D, p.main, PassMath.clamp01(1.0D - age / 16.0D), (double[])null);
      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.08D, g[2]), 0.6D + 8.5D * PassMath.outCubic(age / 18.0D), 0.35D, p.hot, 0.6D * PassMath.clamp01(1.0D - age / 20.0D), (double[])null);
      if (age < 12.0D) {
         q.ring(1, c, 1.0D + 7.0D * PassMath.outCubic(age / 12.0D), 0.25D, 32, p.hot, 0.45D * (1.0D - age / 12.0D), 40, q.fwd(), 0.0D);
      }

      q.decalAt(2, g, 2.6D, 0.7D, 0.95D * PassMath.clamp01(1.0D - (age - 70.0D) / 20.0D));
      q.decalAt(9, g, 4.2D, 1.9D, 0.75D * PassMath.clamp01(1.0D - (age - 70.0D) / 20.0D));
      PassFx.debris(q, ground, light, PassMath.v(g[0], g[1] + 0.3D, g[2]), 77.0D, age, 22, 0.42D, EARTH, (new PassFx.O()).lie(45.0D).size(0.17D));
      PassFx.sparks(q, c, 91.0D, age, 28, 0.55D, p.hot, 1.0D, (new PassFx.O()).life(22.0D).g(0.03D).up(0.4D));
      PassFx.embers(q, PassMath.v(g[0], g[1] + 0.2D, g[2]), 13.0D, age - 6.0D, 16, p.main, 0.9D, (new PassFx.O()).life(40.0D).r(2.2D).rise(0.04D).stagger(1.5D));
      PassFx.smoke(q, light, PassMath.v(c[0], c[1] + 0.4D, c[2]), 31.0D, age - 4.0D, 12, BLAST_SMOKE, 0.75D, (new PassFx.O()).life(70.0D).rise(0.07D).spread(0.035D).size(0.9D).stagger(2.0D));
      q.pool(g, 6.0D * fl + 2.0D, p.main, 0.7D * fl + 0.15D * PassMath.clamp01(1.0D - age / 40.0D));
   }

   public static void gather(PassQuads q, PassCast.Pal p, double[] g, double lift, double age) {
      double k = PassMath.clamp01(age / 6.0D);
      double[] c = PassMath.add(g, PassMath.v(0.0D, 1.05D + lift, 0.0D));
      PassFx.glow(q, c, 0.6D + 1.6D * k, p.main, 0.5D + 0.4D * k);
      q.bill(1, c, 0.4D + 0.5D * k, age * 0.4D, 49, p.hot[0], p.hot[1], p.hot[2], 0.8D * k);

      for(int i = 0; i < 16; ++i) {
         double az = PassMath.h((double)i, 1.0D, 5.0D) * 6.283D;
         double el = (PassMath.h((double)i, 2.0D, 5.0D) - 0.4D) * 1.4D;
         double r0 = 3.2D + 1.5D * PassMath.h((double)i, 3.0D, 5.0D);
         double u = PassMath.clamp01((age + PassMath.h((double)i, 4.0D, 5.0D) * 2.0D) / 6.0D);
         double r = r0 * (1.0D - u * u);
         double[] dir = PassMath.v(Math.cos(az) * Math.cos(el), Math.sin(el), Math.sin(az) * Math.cos(el));
         double[] p1 = PassMath.add(c, PassMath.mul(dir, r));
         double[] p0 = PassMath.add(c, PassMath.mul(dir, r + 0.9D * (1.0D - u)));
         q.ribbon(1, p0, p1, 0.07D, 32, p.hot[0], p.hot[1], p.hot[2], 0.0D, 0.9D * u);
      }

      q.ring(1, PassMath.v(g[0], g[1] + 0.05D, g[2]), 3.2D * (1.0D - k) + 0.6D, 0.2D, 32, p.main, 0.7D * k, 40, (double[])null, 0.0D);
      q.pool(g, 2.0D + 2.0D * k, p.main, 0.3D * k);
   }

   public static void burst(PassQuads q, PassCast.Pal p, double[] light, double[] g, double lift, double age) {
      double[] c = PassMath.v(g[0], g[1] + 1.0D + lift, g[2]);
      double fl = PassMath.clamp01(1.0D - age / 6.0D);
      PassFx.glow(q, c, 4.0D + 3.0D * fl, p.main, fl);
      PassFx.star(q, c, 8.0D * fl, 0.0D, p.hot, fl, true);
      double r = 12.0D * PassMath.outCubic(age / 11.0D);
      double sa = PassMath.clamp01(1.0D - age / 14.0D);
      if (sa > 0.0D) {
         PassFx.shell(q, c, Math.max(0.3D, r), p.main, sa, (new PassFx.O()).base(0.04D).rim(1.1D).slices(28.0D).stacks(16.0D));
      }

      if (sa > 0.0D && p.copy) {
         PassFx.shell(q, c, Math.max(0.3D, r * 0.98D), p.dark, sa * 0.8D, (new PassFx.O()).layer(3).base(0.1D).rim(0.6D).slices(20.0D).stacks(12.0D));
      }

      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.06D, g[2]), 0.5D + 12.0D * PassMath.outCubic(age / 9.0D), 0.8D, p.main, PassMath.clamp01(1.0D - age / 13.0D), (double[])null);
      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.07D, g[2]), 0.5D + 6.0D * PassMath.outCubic(age / 16.0D), 0.4D, p.hot, 0.7D * PassMath.clamp01(1.0D - age / 18.0D), (double[])null);

      for(int i = 0; i < 14; ++i) {
         double az = (double)i / 14.0D * 6.283D + 0.2D;
         double rr = 0.5D + 7.5D * PassMath.outCubic((age - PassMath.h((double)i, 1.0D, 2.0D) * 3.0D) / 22.0D);
         double[] pp = PassMath.v(g[0] + Math.cos(az) * rr, g[1] + 0.35D, g[2] + Math.sin(az) * rr);
         PassFx.smoke(q, light, pp, (double)(200 + i), age - 2.0D, 2, RING_DUST, 0.5D, (new PassFx.O()).life(34.0D).rise(0.02D).spread(0.02D).size(0.75D).raised(true));
      }

      if (age < 5.0D) {
         PassFx.forks(q, c, 61.0D, Math.floor(age), 7, 4.5D + age, 0.06D, p.hot, 1.0D - age / 5.0D, (new PassFx.O()).flat(true).sub(true));
      }

      PassFx.sparks(q, c, 72.0D, age, 30, 0.6D, p.hot, 1.0D, (new PassFx.O()).life(18.0D).g(0.03D).up(0.2D));
      q.decalAt(9, g, 5.2D, 0.4D, 0.85D * PassMath.clamp01(1.0D - (age - 50.0D) / 20.0D));
      q.decalAt(1, g, 2.6D, 1.3D, 0.8D * PassMath.clamp01(1.0D - (age - 50.0D) / 20.0D));
      PassFx.embers(q, PassMath.v(g[0], g[1] + 0.3D, g[2]), 23.0D, age - 4.0D, 18, p.main, 0.9D, (new PassFx.O()).life(34.0D).r(3.5D).stagger(1.0D));
      q.pool(g, 9.0D * fl + 3.0D, p.main, 0.7D * fl + 0.12D);
   }

   public static void launch(PassQuads q, PassCast.Pal p, double[] light, double[] g, double lift, double[] dir, double[] feet, double age) {
      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.06D, g[2]), 0.6D + 3.5D * PassMath.outCubic(age / 10.0D), 0.5D, p.main, PassMath.clamp01(1.0D - age / 12.0D), (double[])null);
      q.decalAt(0, g, 1.6D, 0.3D, 0.85D * PassMath.clamp01(1.0D - (age - 25.0D) / 15.0D));

      for(int i = 0; i < 10; ++i) {
         double az = (double)i / 10.0D * 6.283D;
         double[] pp = PassMath.v(g[0] + Math.cos(az) * (0.5D + age * 0.12D), g[1] + 0.3D, g[2] + Math.sin(az) * (0.5D + age * 0.12D));
         PassFx.smoke(q, light, pp, (double)(300 + i), age, 1, PUFF, 0.55D, (new PassFx.O()).life(26.0D).rise(0.03D).size(0.6D).raised(true));
      }

      if (feet != null && age < 8.0D) {
         double k = 1.0D - age / 8.0D;
         double[] back = PassMath.mul(dir, -1.0D);

         for(int j = 0; j < 3; ++j) {
            double[] a = PassMath.add(feet, PassMath.v(0.0D, 0.5D + (double)j * 0.35D, 0.0D));
            double[] b = PassMath.add(a, PassMath.mul(back, 2.2D * k + 0.4D));
            q.ribbon(1, a, b, 0.6D * k, 30, p.main[0], p.main[1], p.main[2], 0.9D * k, 0.0D);
            q.ribbon(1, a, PassMath.add(a, PassMath.mul(back, 1.2D * k)), 0.25D * k, 31, p.hot[0], p.hot[1], p.hot[2], k, 0.0D);
         }

         PassFx.glow(q, PassMath.add(feet, PassMath.v(0.0D, 0.8D, 0.0D)), 1.4D * k, p.main, 0.9D * k);
      }

      if (age < 9.0D) {
         double[] c2 = PassMath.add(g, PassMath.add(PassMath.v(0.0D, 1.0D + lift, 0.0D), PassMath.mul(dir, 2.5D)));
         q.ring(1, c2, 0.8D + age * 0.35D, 0.22D, 32, p.hot, 0.7D * (1.0D - age / 9.0D), 32, dir, 0.0D);
      }

      q.pool(g, 3.0D, p.main, 0.5D * PassMath.clamp01(1.0D - age / 10.0D));
   }

   public static void contrail(PassQuads q, PassCast.Pal p, double[] light, double[][] hist, double age) {
      int n = hist.length;
      if (n >= 2) {
         double[] w1 = new double[n];
         double[] w2 = new double[n];
         double[] a1 = new double[n];
         double[] a2 = new double[n];

         for(int i = 0; i < n; ++i) {
            double old = age - (double)i;
            double k = PassMath.clamp01(1.0D - old / 22.0D);
            w1[i] = 0.42D + 0.9D * (1.0D - k);
            w2[i] = 0.2D * k + 0.03D;
            a1[i] = 0.55D * k;
            a2[i] = 0.95D * k * k;
         }

         q.chain(1, hist, w1, 32, p.main, a1);
         q.chain(1, hist, w2, 31, p.hot, a2);

         for(int i = 0; i < n; i += 3) {
            double ag = age - (double)i;
            if (!(ag > 26.0D)) {
               q.bill(0, PassMath.add(hist[i], PassMath.v(0.0D, ag * 0.015D, 0.0D)), 0.4D + ag * 0.05D, (double)i, 35, 0.5D * light[0], 0.46D * light[1], 0.44D * light[2], 0.48D * (1.0D - ag / 26.0D));
            }
         }

      }
   }

   public static void land(PassQuads q, PassCast.Pal p, PassFx.Ground ground, double[] light, double[] g, double age) {
      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.06D, g[2]), 0.5D + 3.0D * PassMath.outCubic(age / 10.0D), 0.45D, p.main, PassMath.clamp01(1.0D - age / 12.0D), (double[])null);

      for(int i = 0; i < 8; ++i) {
         double az = (double)i / 8.0D * 6.283D + 0.4D;
         PassFx.smoke(q, light, PassMath.v(g[0] + Math.cos(az) * (0.4D + age * 0.1D), g[1] + 0.25D, g[2] + Math.sin(az) * (0.4D + age * 0.1D)), (double)(400 + i), age, 1, PUFF, 0.5D, (new PassFx.O()).life(24.0D).rise(0.025D).size(0.55D).raised(true));
      }

      PassFx.debris(q, ground, light, PassMath.v(g[0], g[1] + 0.2D, g[2]), 88.0D, age, 8, 0.25D, LAND_EARTH, (new PassFx.O()).lie(25.0D));
      q.decalAt(1, g, 1.4D, 0.7D, 0.7D * PassMath.clamp01(1.0D - (age - 25.0D) / 15.0D));
   }

   public static void ignite(PassQuads q, PassCast.Pal p, double[] g, double[][] palms, double age) {
      double k = PassMath.clamp01(1.0D - age / 18.0D);
      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.05D, g[2]), 0.4D + 2.6D * PassMath.outCubic(age / 12.0D), 0.35D, p.main, k, (double[])null);

      for(int i = 0; i < 2; ++i) {
         PassFx.star(q, palms[i], 1.2D * k, age * 0.2D + (double)i, p.hot, k, true);
         PassFx.sparks(q, palms[i], (double)(70 + i), age, 8, 0.18D, p.hot, k, (new PassFx.O()).life(12.0D).g(0.02D));
      }

      PassFx.embers(q, PassMath.v(g[0], g[1] + 0.2D, g[2]), 5.0D, age, 10, p.main, 0.9D, (new PassFx.O()).life(26.0D).r(0.7D).rise(0.07D));
   }

   public static void hands(PassQuads q, PassCast.Pal p, double[][] palms, double t, double k) {
      for(int i = 0; i < 2; ++i) {
         double fl = 0.75D + 0.25D * Math.sin(t * 0.9D + (double)i * 2.1D);
         PassFx.glow(q, palms[i], 0.32D * fl * k, p.main, 0.7D * k);
         q.bill(1, palms[i], 0.14D * k, t * 0.3D + (double)i, 33, p.hot[0], p.hot[1], p.hot[2], 0.8D * k);
         if ((Math.floor(t / 7.0D) + (double)i) % 3.0D == 0.0D) {
            PassFx.sparks(q, palms[i], Math.floor(t / 7.0D) * 3.0D + (double)i, t % 7.0D, 3, 0.08D, p.hot, 0.8D * k, (new PassFx.O()).life(6.0D).g(0.02D));
         }
      }

   }
}
