package com.myriam.unrp.pass;

import java.util.ArrayList;
import java.util.List;

public final class PassBr {
   public static final int ICE = 10409727;
   public static final int GOLD = 15914308;
   public static final int RAM_TICKS = 10;
   public static final int HITS_MOST = 10;
   public static final int GONE_KEEP = 40;
   public static final int SHATTER_LIFE = 60;
   public static final int SHIELD_BREAK_LIFE = 44;
   public static final int CAST_LIFE = 14;
   public static final int CAST_BACK_LIFE = 10;
   public static final int SQUEEZE_LIFE = 18;
   public static final int CRUSH_LIFE = 10;
   public static final int RAM_HIT_LIFE = 24;
   public static final double[] CRACK = new double[]{1.0D, 0.95D, 0.82D};
   public static final double PLATE_W = 1.2D;
   public static final double PLATE_H = 1.5D;
   public static final double STEP_PAD = 0.25D;
   public static final double[][] SHIELD_EDGE = new double[][]{{0.0D, 1.45D}, {0.78D, 1.12D}, {1.2D, 0.35D}, {1.0D, -0.55D}, {0.55D, -1.15D}, {0.0D, -1.5D}, {-0.55D, -1.15D}, {-1.0D, -0.55D}, {-1.2D, 0.35D}, {-0.78D, 1.12D}};
   public static final int[] SHIELD_SPIKES = new int[]{1, 2, 4, 6, 8, 9};
   public static final double[][][] FACETS = shieldFacets(7);

   private PassBr() {
   }

   public static PassBr.Cols cols(int rgb, boolean set, boolean copy) {
      if (copy) {
         return new PassBr.Cols(PassCast.SMOKE_MAIN, PassCast.SMOKE_HOT, PassCast.SMOKE_MAIN, PassCast.SMOKE_DARK, true);
      } else if (set) {
         double[] m = PassFx.rgb(rgb);
         return new PassBr.Cols(m, PassFx.whiter(m, 0.55D), PassFx.whiter(m, 0.35D), (double[])null, false);
      } else {
         double[] ice = PassFx.rgb(10409727);
         return new PassBr.Cols(ice, PassFx.whiter(ice, 0.6D), PassFx.rgb(15914308), (double[])null, false);
      }
   }

   public static PassBr.Hit blow(List<PassBr.Hit> hits, double t, double[] dir, double amount) {
      PassBr.Hit last = hits.isEmpty() ? null : (PassBr.Hit)hits.get(hits.size() - 1);
      boolean cont = last != null && t - last.t <= 3.0D && PassMath.dot(last.dir, dir) >= 0.9D;
      PassBr.Hit n = null;
      if (last == null || t - last.t >= 3.0D || !cont) {
         n = new PassBr.Hit(t, dir, Math.min(1.0D, 0.35D + amount * 0.12D), cont);
         hits.add(n);
      }

      while(hits.size() > 10) {
         hits.remove(0);
      }

      return n;
   }

   static double bulge(double x, double y) {
      return 0.42D * Math.max(0.0D, 1.0D - x / 1.3D * (x / 1.3D) - y / 1.6D * (y / 1.6D));
   }

   static double[][][] shieldFacets(int seed) {
      List<double[][]> out = new ArrayList();
      int n = SHIELD_EDGE.length;
      double[] mid = new double[]{0.0D, 0.05D, 0.44D};

      for(int i = 0; i < n; ++i) {
         double[] a = SHIELD_EDGE[i];
         double[] b = SHIELD_EDGE[(i + 1) % n];
         double mx = (a[0] + b[0]) / 2.0D * 0.72D + (PassMath.h((double)seed, (double)i, 3.0D) - 0.5D) * 0.16D;
         double my = (a[1] + b[1]) / 2.0D * 0.72D + (PassMath.h((double)seed, (double)i, 4.0D) - 0.5D) * 0.16D;
         double[] m = new double[]{mx, my, bulge(mx, my) + 0.05D * PassMath.h((double)seed, (double)i, 5.0D)};
         double[] aa = new double[]{a[0], a[1], 0.0D};
         double[] bb = new double[]{b[0], b[1], 0.0D};
         out.add(new double[][]{mid, aa, m});
         out.add(new double[][]{mid, m, bb});
         out.add(new double[][]{aa, bb, m});
      }

      for(int j = 0; j < SHIELD_SPIKES.length; ++j) {
         double[] p = SHIELD_EDGE[SHIELD_SPIKES[j]];
         double len = Math.hypot(p[0], p[1]);
         double[] o = new double[]{p[0] / len, p[1] / len};
         double[] tn = new double[]{-o[1], o[0]};
         double l = 0.16D + 0.12D * PassMath.h((double)seed, (double)j, 6.0D);
         out.add(new double[][]{{p[0] - tn[0] * 0.13D, p[1] - tn[1] * 0.13D, 0.02D}, {p[0] + o[0] * l, p[1] + o[1] * l, 0.08D}, {p[0] + tn[0] * 0.13D, p[1] + tn[1] * 0.13D, 0.02D}});
      }

      return (double[][][])out.toArray(new double[0][][]);
   }

   public static double[] toWorld(PassBr.Frame fr, double[] q) {
      return PassMath.add(fr.c, PassMath.add(PassMath.add(PassMath.mul(fr.r, q[0]), PassMath.mul(fr.u, q[1])), PassMath.mul(fr.f, q[2])));
   }

   static double[] triNormal(double[] a, double[] b, double[] c) {
      return PassMath.norm(PassMath.cross(PassMath.sub(b, a), PassMath.sub(c, a)));
   }

   public static double[] shape(PassBr.Shell sh, double t) {
      double size = sh.size;
      double[] c = sh.at;
      double gk = (t - sh.growT) / 6.0D;
      if (gk >= 0.0D && gk < 1.0D) {
         double k = PassMath.outCubic(gk) + Math.sin(gk * Math.PI) * 0.12D;
         size = PassMath.lerp(sh.growFrom, sh.size, k);
         c = PassMath.v(sh.at[0], sh.at[1] - (sh.size - size) / 2.0D, sh.at[2]);
      }

      return new double[]{c[0], c[1], c[2], size / 2.0D};
   }

   static void ringAtHeight(PassQuads q, double[] c, double r, double y, double w, int cell, double[] col, double a) {
      double dy = y - c[1];
      if (!(Math.abs(dy) >= r) && a > 0.003D) {
         q.ring(1, PassMath.v(c[0], y, c[2]), Math.sqrt(r * r - dy * dy), w, cell, col, a, (int)Math.max(24.0D, Math.ceil(r * 12.0D)), (double[])null, 0.0D);
      }
   }

   static double bands(PassBr.Shell sh, double hg, double build, double diss, double gk) {
      double b = 0.0D;
      if (build < 1.0D) {
         b += 1.5D * Math.exp(-Math.pow((hg - build) / 0.06D, 2.0D));
      }

      if (diss >= 0.0D) {
         b += 1.4D * Math.exp(-Math.pow((hg - diss) / 0.06D, 2.0D));
      }

      if (gk >= 0.0D && gk < 1.0D) {
         double at = sh.growSign > 0.0D ? 1.0D - gk : gk;
         b += 0.9D * (1.0D - gk) * Math.exp(-Math.pow((hg - at) / 0.1D, 2.0D));
      }

      return b;
   }

   static double bump(PassBr.Shell sh, double[] n, double t) {
      double b = 0.0D;

      for(PassBr.Hit hit : sh.hits) {
         double ag = t - hit.t;
         if (!(ag < 0.0D) && !(ag > 16.0D)) {
            double ang = Math.acos(PassMath.clamp(PassMath.dot(n, hit.dir), -1.0D, 1.0D));
            b += 0.9D * (1.0D - ag / 16.0D) * Math.exp(-Math.pow((ang - (0.1D + ag * 0.2D)) / 0.2D, 2.0D));
            if (ag < 5.0D) {
               b += (hit.cont ? 0.4D : 0.9D) * hit.k * (1.0D - ag / 5.0D) * Math.exp(-Math.pow(ang / 0.3D, 2.0D));
            }
         }
      }

      return b;
   }

   public static void shell(PassQuads q, PassBr.Cols cl, PassFx.Ground ground, PassBr.Shell sh, double t) {
      shell(q, cl, ground, Double.NaN, sh, t);
   }

   public static void shell(PassQuads q, PassBr.Cols cl, PassFx.Ground ground, double floor, PassBr.Shell sh, double t) {
      double gone = sh.gone >= 0.0D ? t - sh.gone : -1.0D;
      if (!(gone >= 0.0D) || !sh.shattered && !(gone > 16.0D)) {
         double[] sp = shape(sh, t);
         double[] c = PassMath.v(sp[0], sp[1], sp[2]);
         double r = sp[3];
         double g = ground.at(c[0], c[2], c[1] + 0.5D);
         double cut = floor < g ? floor : g;
         boolean even = !(cut < g);
         double lo = Math.max(cut, c[1] - r);
         double top = c[1] + r;
         double span = Math.max(0.1D, top - lo);
         double build = PassMath.clamp01((t - sh.build) / 6.0D);
         double diss = gone >= 0.0D ? PassMath.clamp01(gone / 14.0D) * 1.08D : -1.0D;
         double flick = sh.crossed >= 3 ? 0.55D + 0.45D * Math.abs(Math.sin(t * 1.9D + Math.sin(t * 0.37D) * 3.0D)) : 1.0D;
         double[] eye = q.eye;
         double gk = (t - sh.growT) / 10.0D;
         double[] main = cl.main;
         double fade = gone >= 0.0D ? 1.0D - PassMath.clamp01(gone / 16.0D) : 1.0D;
         q.sphere(1, c, r, 36, 20, 40, main, (n, vv, u) -> 0.62D * alphaOf(sh, c, r, n, eye, cut, lo, span, build, diss, gk, flick, fade, t, 0.95D, 0.035D), (double[])null, 0.0D, 1.0D);
         q.sphere(1, c, r * 1.003D, 32, 16, 64, cl.rim, (n, vv, u) -> 0.5D * alphaOf(sh, c, r, n, eye, cut, lo, span, build, diss, gk, flick, fade, t, 0.6D, 0.16D), new double[]{8.0D, 4.0D}, 0.0D, 1.0D);
         if (cl.copy) {
            double rr = r * 0.99D;
            PassFx.shell(q, c, rr, cl.dark, 0.6D * fade * build, (new PassFx.O()).layer(3).base(0.32D).rim(0.25D).slices(24.0D).stacks(12.0D).alpha((n, vv, u) -> c[1] + n[1] * rr < cut - 0.05D ? 0.0D : 1.0D));
         }

         double dy = g - c[1];
         if (Math.abs(dy) < r) {
            double cr = Math.sqrt(r * r - dy * dy);
            double ga = fade * (diss >= 0.0D ? 1.0D - PassMath.clamp01(diss) : 1.0D);
            double bt = t - sh.build;
            double ringR = bt < 6.0D ? cr * (0.35D + 0.65D * PassMath.outCubic(bt / 6.0D)) : cr;
            int segs = (int)Math.max(32.0D, Math.ceil(cr * 12.0D));
            q.ring(1, PassMath.v(c[0], g + 0.04D, c[2]), ringR, 0.2D, 32, cl.gold, 0.9D * ga, segs, (double[])null, 0.0D);
            q.ring(1, PassMath.v(c[0], g + 0.03D, c[2]), ringR, 0.7D, 32, cl.gold, 0.22D * ga, segs, (double[])null, 0.0D);
            if (even) {
               q.decalAt(6, PassMath.v(c[0], g, c[2]), cr, t * 0.004D, (0.3D + (bt < 10.0D ? 0.6D * (1.0D - bt / 10.0D) : 0.0D)) * ga, cl.gold, true);
               q.pool(PassMath.v(c[0], g, c[2]), cr * 1.3D, main, 0.16D * ga);
            }

            if (even && bt >= 0.0D && bt < 10.0D) {
               q.ring(1, PassMath.v(c[0], g + 0.05D, c[2]), cr * (0.3D + 1.0D * PassMath.outCubic(bt / 10.0D)), 0.45D, 32, PassFx.whiter(cl.gold, 0.4D), 1.0D - bt / 10.0D, 48, (double[])null, 0.0D);
            }
         }

         if (build < 1.0D) {
            ringAtHeight(q, c, r * 1.004D, lo + span * build, 0.14D, 32, cl.rim, 0.95D * (1.0D - build * 0.5D));
         }

         if (diss >= 0.0D && diss < 1.0D) {
            ringAtHeight(q, c, r * 1.004D, lo + span * diss, 0.14D, 32, cl.rim, 0.9D * (1.0D - diss));
         }

         if (gk >= 0.0D && gk < 1.0D) {
            ringAtHeight(q, c, r * 1.004D, lo + span * (sh.growSign > 0.0D ? 1.0D - gk : gk), 0.12D, 32, cl.rim, 0.85D * (1.0D - gk));
         }

         if (gone < 0.0D && build >= 1.0D) {
            for(int k = 0; k < 2; ++k) {
               double u = (t * 0.011D + (double)k * 0.5D + (double)sh.id * 0.13D) % 1.0D;
               ringAtHeight(q, c, r * 1.004D, lo + span * u, 0.07D, 32, cl.rim, 0.32D * Math.sin(Math.PI * u) * flick);
            }

            for(int k = 0; k < 5; ++k) {
               double cyc = Math.floor((t + (double)(k * 4)) / 20.0D);
               double ag = (t + (double)(k * 4)) % 20.0D;
               double az = PassMath.h((double)sh.id, cyc, (double)k) * 6.283D;
               double el = Math.asin(PassMath.lerp(Math.max(-1.0D, (lo - c[1]) / r) + 0.05D, 0.98D, PassMath.h((double)sh.id, cyc, (double)(k + 9))));
               double[] n = PassMath.v(Math.cos(az) * Math.cos(el), Math.sin(el), Math.sin(az) * Math.cos(el));
               PassFx.star(q, PassMath.add(c, PassMath.mul(n, r * 1.01D)), 0.22D, ag * 0.05D, PassFx.whiter(cl.rim, 0.5D), 0.85D * Math.sin(Math.PI * ag / 20.0D) * flick, false);
            }
         }

         for(int i = 0; i < sh.hits.size(); ++i) {
            PassBr.Hit hit = (PassBr.Hit)sh.hits.get(i);
            double ha = t - hit.t;
            if (!(ha < 0.0D) && !(ha > 16.0D)) {
               double[] hd = hit.dir;
               double th = 0.1D + ha * 0.2D;
               double hk = hit.k;
               if (th < 2.8D) {
                  double[] rc = PassMath.add(c, PassMath.mul(hd, r * 1.006D * Math.cos(th)));
                  q.ring(1, rc, r * 1.006D * Math.sin(th), 0.13D, 32, cl.rim, 0.85D * (1.0D - ha / 16.0D) * fade, 40, hd, 0.0D);
               }

               double[] hp = PassMath.add(c, PassMath.mul(hd, r * 1.01D));
               if (ha < 8.0D) {
                  q.patch(1, c, r * 1.008D, hd, hit.cont ? 0.24D : 0.3D, (double)(sh.id + i), 39, PassFx.whiter(cl.rim, 0.3D), (PassQuads.AlphaUv)null, (hit.cont ? 0.6D : 0.9D) * (1.0D - ha / 8.0D) * fade, 0);
                  if (!hit.cont) {
                     PassFx.glow(q, hp, (0.9D * (1.0D - ha / 8.0D) + 0.2D) * hk, main, 0.6D * (1.0D - ha / 8.0D));
                     if (ha < 4.0D) {
                        PassFx.star(q, hp, 1.2D * hk * (1.0D - ha / 4.0D), hit.t, PassFx.whiter(cl.rim, 0.5D), 0.9D * (1.0D - ha / 4.0D), true);
                     }
                  }
               }

               if (ha < 10.0D) {
                  PassFx.sparks(q, hp, hit.t * 7.0D + (double)sh.id, ha, 7, 0.22D, PassFx.whiter(cl.rim, 0.4D), fade, (new PassFx.O()).dir(hd).cone(0.9D).life(10.0D).g(0.03D));
               }
            }
         }

         for(int qi = 0; qi < sh.cracks.size(); ++qi) {
            PassBr.Crack ck = (PassBr.Crack)sh.cracks.get(qi);
            double ct = t - ck.t;
            double hot = ct < 10.0D ? 1.0D - ct / 10.0D : 0.0D;

            for(int j = 0; j < 3; ++j) {
               double seed = (double)(sh.id * 31 + qi * 7 + j);
               double a0 = Math.atan2(ck.dir[2], ck.dir[0]) + (PassMath.h(seed, 1.0D, 5.0D) - 0.5D) * (j == 0 ? 0.6D : 2.6D);
               double e0 = Math.asin(PassMath.lerp(Math.max(-1.0D, (lo - c[1]) / r) + 0.12D, 0.85D, PassMath.h(seed, 2.0D, 5.0D)));
               double[] cn = PassMath.v(Math.cos(a0) * Math.cos(e0), Math.sin(e0), Math.sin(a0) * Math.cos(e0));
               double spread = PassMath.outCubic(ct / 4.0D);
               double ang = (0.26D + 0.2D * PassMath.h(seed, 3.0D, 5.0D)) * (0.45D + 0.55D * spread);
               double[] cc = cl.copy ? cl.rim : CRACK;
               q.patch(1, c, r * 1.01D, cn, ang, PassMath.h(seed, 4.0D, 5.0D) * 6.283D, 53, cc, (PassQuads.AlphaUv)null, (0.6D + 0.4D * hot) * fade * flick, 0);
               PassFx.glow(q, PassMath.add(c, PassMath.mul(cn, r * 1.01D)), 0.5D + 0.8D * hot, main, (0.2D + 0.6D * hot) * fade);
            }
         }

         if (sh.crossed >= 3 && gone < 0.0D && !sh.cracks.isEmpty()) {
            double sb = Math.floor(t / 7.0D);
            double sa = t % 7.0D;
            PassBr.Crack sc = (PassBr.Crack)sh.cracks.get((int)Math.floor(PassMath.h(sb, (double)sh.id, 3.0D) * (double)sh.cracks.size()));
            PassFx.sparks(q, PassMath.add(c, PassMath.mul(sc.dir, r)), sb * 13.0D + (double)sh.id, sa, 5, 0.18D, PassFx.whiter(cl.rim, 0.5D), 1.0D, (new PassFx.O()).dir(sc.dir).cone(1.0D).life(7.0D).g(0.04D));
         }

         if (diss >= 0.0D) {
            PassFx.embers(q, PassMath.v(c[0], lo + span * Math.min(1.0D, diss), c[2]), (double)(600 + sh.id), gone, 22, cl.rim, fade, (new PassFx.O()).life(16.0D).r(r * 0.8D).rise(0.12D));
         }

      }
   }

   static double alphaOf(PassBr.Shell sh, double[] c, double r, double[] n, double[] eye, double g, double lo, double span, double build, double diss, double gk, double flick, double fade, double t, double rim, double face) {
      double[] p = PassMath.v(c[0] + n[0] * r, c[1] + n[1] * r, c[2] + n[2] * r);
      if (p[1] < g - 0.05D) {
         return 0.0D;
      } else {
         double hg = PassMath.clamp01((p[1] - lo) / span);
         if (hg > build * 1.04D) {
            return 0.0D;
         } else if (diss >= 0.0D && hg < diss) {
            return 0.0D;
         } else {
            double[] vd = PassMath.norm(PassMath.sub(eye, p));
            double f = 1.0D - Math.abs(PassMath.dot(n, vd));
            return (face + rim * f * f * f + bands(sh, hg, build, diss, gk) + bump(sh, n, t)) * flick * fade;
         }
      }
   }

   public static void shield(PassQuads q, PassBr.Cols cl, PassFx.Ground ground, PassBr.Frame fr, int id, double start, double flash, double t) {
      double[] eye = q.eye;
      double[] rim = PassFx.whiter(cl.rim, 0.25D);

      for(int i = 0; i < FACETS.length; ++i) {
         double[][] tri = FACETS[i];
         double k = PassMath.outCubic(PassMath.clamp01((t - start - (double)(i % 10) * 0.18D) / 3.0D));
         if (!(k <= 0.01D)) {
            double[][] w = new double[][]{toWorld(fr, tri[0]), toWorld(fr, tri[1]), toWorld(fr, tri[2])};
            double[] cen = PassMath.mul(PassMath.add(PassMath.add(w[0], w[1]), w[2]), 0.3333333333333333D);
            double[] n = triNormal(w[0], w[1], w[2]);
            double[] lift = PassMath.add(PassMath.mul(n, (1.0D - k) * 0.9D), PassMath.mul(PassMath.sub(cen, fr.c), (1.0D - k) * 0.5D));

            for(int j = 0; j < 3; ++j) {
               w[j] = PassMath.add(w[j], lift);
            }

            double[] vd = PassMath.norm(PassMath.sub(eye, cen));
            double ndv = Math.abs(PassMath.dot(n, vd));
            double lx = tri[0][0] + tri[1][0] + tri[2][0];
            double ly = tri[0][1] + tri[1][1] + tri[2][1];
            double sweep = Math.pow(Math.max(0.0D, Math.sin((lx * 0.27D + ly * 0.17D) * 3.0D - t * 0.35D)), 6.0D) * 0.5D;
            double a = (0.09D + 0.5D * (1.0D - ndv) * (1.0D - ndv) + sweep + 0.6D * flash) * k;
            q.quad(1, w[0], w[1], w[2], w[2], 40, cl.main[0], cl.main[1], cl.main[2], 0.5D * a, (double[])null);
            double ea = (0.35D + 0.4D * sweep + 0.5D * flash) * k;
            q.ribbon(1, w[0], w[1], 0.028D, 32, rim[0], rim[1], rim[2], ea, ea);
            q.ribbon(1, w[1], w[2], 0.028D, 32, rim[0], rim[1], rim[2], ea, ea);
            q.ribbon(1, w[2], w[0], 0.028D, 32, rim[0], rim[1], rim[2], ea, ea);
         }
      }

      double built = PassMath.clamp01((t - start - 1.0D) / 3.0D);

      for(int j = 0; j < SHIELD_EDGE.length && built > 0.0D; ++j) {
         double[] p0 = toWorld(fr, PassMath.v(SHIELD_EDGE[j][0], SHIELD_EDGE[j][1], 0.0D));
         double[] e1 = SHIELD_EDGE[(j + 1) % SHIELD_EDGE.length];
         double[] p1 = toWorld(fr, PassMath.v(e1[0], e1[1], 0.0D));
         q.ribbon(1, p0, p1, 0.07D, 32, cl.gold[0], cl.gold[1], cl.gold[2], 0.9D * built, 0.9D * built);
      }

      PassFx.glow(q, toWorld(fr, PassMath.v(0.0D, 0.05D, 0.3D)), 1.6D, cl.main, (0.28D + 0.4D * flash) * built);

      for(int g = 0; g < 3 && built >= 1.0D; ++g) {
         double cyc = Math.floor((t + (double)(g * 5)) / 14.0D);
         double ag = (t + (double)(g * 5)) % 14.0D;
         double[] gp = SHIELD_EDGE[(int)Math.floor(PassMath.h((double)id, cyc, (double)g) * (double)SHIELD_EDGE.length)];
         PassFx.star(q, toWorld(fr, PassMath.v(gp[0] * 0.85D, gp[1] * 0.85D, 0.12D)), 0.3D, ag * 0.05D, PassFx.whiter(cl.rim, 0.5D), Math.sin(Math.PI * ag / 14.0D), false);
      }

      double gy = ground.at(fr.c[0], fr.c[2], fr.c[1]);
      if (fr.c[1] - gy < 4.0D) {
         q.pool(PassMath.v(fr.c[0], gy, fr.c[2]), 1.8D, cl.main, 0.18D * (1.0D - (fr.c[1] - gy) / 4.0D) * built);
      }

   }

   public static void shatter(PassQuads q, PassBr.Cols cl, PassFx.Ground ground, double[] c, double r, int sid, boolean small, double age) {
      shatter(q, cl, ground, Double.NaN, c, r, sid, small, age);
   }

   public static void shatter(PassQuads q, PassBr.Cols cl, PassFx.Ground ground, double floor, double[] c, double r, int sid, boolean small, double age) {
      double g = ground.at(c[0], c[2], c[1] + 0.5D);
      double cut = floor < g ? floor : g;
      boolean even = !(cut < g);
      if (age < 5.0D) {
         double bk = 1.0D - age / 5.0D;
         PassFx.shell(q, c, r * (1.0D + 0.32D * PassMath.outCubic(age / 5.0D)), PassFx.whiter(cl.main, 0.5D), bk * bk, (new PassFx.O()).base(0.22D).rim(1.4D).slices(30.0D).stacks(16.0D).alpha((nx, vv, u) -> c[1] + nx[1] * r * (1.0D + 0.32D * PassMath.outCubic(age / 5.0D)) < cut - 0.05D && !small ? 0.0D : 1.0D));
      }

      double fl = PassMath.clamp01(1.0D - age / 8.0D);
      double[] mid = small ? c : PassMath.v(c[0], Math.max(g, c[1]) + r * 0.45D, c[2]);
      PassFx.glow(q, mid, r * (small ? 2.0D : 3.2D) * fl + 0.4D, cl.main, fl);
      PassFx.star(q, mid, r * (small ? 2.2D : 3.4D) * fl, 0.3D, PassFx.whiter(cl.main, 0.7D), fl, true);
      if (!small && even) {
         PassFx.shock(q, PassMath.v(c[0], g + 0.06D, c[2]), r * (1.0D + 1.8D * PassMath.outCubic(age / 12.0D)), 0.6D, cl.main, PassMath.clamp01(1.0D - age / 14.0D), (double[])null);
         PassFx.shock(q, PassMath.v(c[0], g + 0.07D, c[2]), r * (0.6D + 2.6D * PassMath.outCubic(age / 18.0D)), 0.3D, cl.gold, 0.7D * PassMath.clamp01(1.0D - age / 20.0D), (double[])null);
         q.decalAt(6, PassMath.v(c[0], g, c[2]), r, 0.0D, 0.9D * PassMath.clamp01(1.0D - age / 16.0D), cl.gold, true);
         q.pool(PassMath.v(c[0], g, c[2]), r * 2.4D * fl + r, cl.main, 0.5D * fl);
      }

      int n = small ? 26 : 90;
      double[] hot = PassFx.whiter(cl.rim, 0.35D);

      for(int i = 0; i < n; ++i) {
         double az = PassMath.h((double)i, (double)sid, 41.0D) * 6.283D;
         double el = Math.asin(PassMath.h((double)i, (double)sid, 42.0D) * 2.0D - 1.0D);
         double[] dir = PassMath.v(Math.cos(az) * Math.cos(el), Math.sin(el), Math.sin(az) * Math.cos(el));
         double[] p0 = PassMath.add(c, PassMath.mul(dir, r));
         if (small || !(p0[1] < cut - 0.05D)) {
            double land = p0[1] < g - 0.05D ? cut : g;
            double vel = (0.1D + 0.16D * PassMath.h((double)i, (double)sid, 43.0D)) * (small ? 1.2D : 1.0D);
            double gy = 0.014D + 0.006D * PassMath.h((double)i, (double)sid, 44.0D);
            double[] p = shard(p0, dir, vel, gy, age);
            double[] pb = shard(p0, dir, vel, gy, Math.max(0.0D, age - 1.8D));
            boolean landed = p[1] < land + 0.04D;
            if (landed) {
               p[1] = land + 0.04D;
               pb = p;
            }

            double life = 34.0D + 18.0D * PassMath.h((double)i, (double)sid, 45.0D);
            double k = PassMath.clamp01(1.0D - (age - life + 10.0D) / 10.0D);
            if (!(k <= 0.0D)) {
               double spinA = landed ? 0.0D : age * (0.25D + 0.35D * PassMath.h((double)i, (double)sid, 46.0D));
               double[] ax = PassMath.norm(PassMath.v(dir[0] + Math.sin(spinA + (double)i), dir[1] + 0.6D, dir[2] + Math.cos(spinA * 0.8D + (double)i)));
               double[][] bs = PassQuads.basis(landed ? PassMath.v(0.0D, 1.0D, 0.0D) : ax);
               double sz = (0.2D + 0.22D * PassMath.h((double)i, (double)sid, 47.0D)) * (small ? 0.7D : 1.0D);
               double tw = 0.75D + 0.25D * Math.sin(age * 0.9D + (double)i * 1.7D);
               q.plane(1, p, bs[0], bs[1], sz, (double)i, 39, cl.main[0], cl.main[1], cl.main[2], 0.95D * k * tw, sz);
               if (!landed && age < 16.0D) {
                  q.ribbon(1, pb, p, sz * 0.9D, 32, hot[0], hot[1], hot[2], 0.0D, 0.7D * k * (1.0D - age / 16.0D));
               }

               if (i % 3 == 0) {
                  q.bill(1, p, 0.1D, 0.0D, 26, 1.0D, 1.0D, 1.0D, k * Math.max(0.0D, Math.sin(age * 0.7D + (double)i)));
               }
            }
         }
      }

      PassFx.sparks(q, mid, (double)(700 + sid), age, small ? 12 : 30, 0.5D, PassFx.whiter(cl.main, 0.5D), 1.0D, (new PassFx.O()).life(18.0D).g(0.03D).up(0.3D));
      if (!small) {
         PassFx.embers(q, PassMath.v(c[0], g + 0.2D, c[2]), (double)(800 + sid), age - 3.0D, 20, cl.rim, 0.9D, (new PassFx.O()).life(34.0D).r(r * 1.2D).rise(0.05D).stagger(1.0D));
      }

   }

   static double[] shard(double[] p0, double[] dir, double vel, double gy, double tt) {
      double dr = Math.pow(0.94D, tt);
      double s = vel * (1.0D - dr) / 0.06D;
      return new double[]{p0[0] + dir[0] * s, p0[1] + dir[1] * s + 0.05D * tt - gy * tt * tt, p0[2] + dir[2] * s};
   }

   public static void cast(PassQuads q, PassBr.Cols cl, double[] hnd, double[] at, boolean back, double age) {
      double[] to = PassMath.add(at, PassMath.v(0.0D, back ? 1.2D : 0.6D, 0.0D));
      double k = PassMath.clamp01(1.0D - age / 10.0D);
      double u = back ? 1.0D - PassMath.clamp01(age / 5.0D) : PassMath.clamp01(age / 3.0D);
      double[] head = PassMath.lerp(hnd, to, PassMath.outCubic(u));
      if (age < 10.0D) {
         double[] tail = PassMath.lerp(hnd, to, Math.max(0.0D, PassMath.outCubic(u) - 0.35D));
         q.ribbon(1, tail, head, 0.3D, 30, cl.main[0], cl.main[1], cl.main[2], 0.0D, 0.7D * k);
         q.ribbon(1, tail, head, 0.09D, 31, cl.rim[0], cl.rim[1], cl.rim[2], 0.0D, k);
         PassFx.glow(q, head, 0.9D, cl.main, k);
         q.bill(1, head, 0.32D, age * 0.4D, 38, cl.gold[0], cl.gold[1], cl.gold[2], k);
      }

      PassFx.star(q, hnd, 1.1D * k, age * 0.2D, cl.rim, k, false);
      PassFx.glow(q, hnd, 0.6D * k, cl.main, 0.8D * k);
   }

   public static void squeeze(PassQuads q, PassBr.Cols cl, PassFx.Ground ground, double[] c, double from, double fromY, double r, int sid, double[][] caught, double age) {
      double g = ground.at(c[0], c[2], c[1] + 0.5D);
      double k = PassMath.clamp01(1.0D - age / 12.0D);
      double dy = g - c[1];
      double dy0 = g - fromY;
      if (Math.abs(dy0) < from) {
         double r0 = Math.sqrt(from * from - dy0 * dy0);
         double r1 = Math.abs(dy) < r ? Math.sqrt(r * r - dy * dy) : 0.2D;
         double rr = PassMath.lerp(r0 + 0.6D, r1, PassMath.outCubic(age / 5.0D));
         PassFx.shock(q, PassMath.v(c[0], g + 0.07D, c[2]), rr, 0.35D, cl.gold, k, (double[])null);
      }

      for(int i = 0; i < caught.length; ++i) {
         double[] m = caught[i];
         if (m != null) {
            if (age < 6.0D) {
               PassFx.star(q, m, 1.4D * (1.0D - age / 6.0D), (double)i + age * 0.3D, PassMath.v(1.0D, 1.0D, 1.0D), 1.0D - age / 6.0D, true);
               PassFx.glow(q, m, 1.1D, cl.main, 0.8D * (1.0D - age / 6.0D));
            }

            PassFx.sparks(q, m, (double)(900 + i * 17 + sid), age, 12, 0.25D, PassFx.whiter(cl.rim, 0.5D), 1.0D, (new PassFx.O()).life(12.0D).g(0.04D).up(0.1D));
         }
      }

   }

   public static void crush(PassQuads q, PassBr.Cols cl, double[] m, int sid, double born, double age) {
      double k = 1.0D - age / 10.0D;
      PassFx.glow(q, m, 0.9D * k + 0.2D, cl.main, 0.6D * k);
      PassFx.sparks(q, m, (double)(950 + sid) + born, age, 6, 0.16D, PassFx.whiter(cl.rim, 0.5D), k, (new PassFx.O()).life(8.0D).g(0.04D));
   }

   public static void ram(PassQuads q, PassBr.Cols cl, PassBr.Frame fr, double tt, double age) {
      for(int i = 0; i < SHIELD_EDGE.length; ++i) {
         double[] e0 = SHIELD_EDGE[i];
         double ph = (tt * 0.45D + PassMath.h((double)i, 3.0D, 7.0D)) % 1.0D;
         double[] a0 = PassMath.add(toWorld(fr, PassMath.v(e0[0] * 1.05D, e0[1] * 1.05D, 0.0D)), PassMath.mul(fr.f, -ph * 2.6D));
         double[] a1 = PassMath.add(a0, PassMath.mul(fr.f, -1.3D));
         q.ribbon(1, a0, a1, 0.07D, 32, cl.rim[0], cl.rim[1], cl.rim[2], 0.75D * (1.0D - ph), 0.0D);
      }

      if (age < 5.0D) {
         double pk = 1.0D - age / 5.0D;
         PassFx.glow(q, fr.c, 1.6D * pk + 0.3D, cl.main, 0.6D * pk);
         PassFx.star(q, toWorld(fr, PassMath.v(0.0D, 0.05D, 0.4D)), 2.0D * pk, 0.3D, PassFx.whiter(cl.rim, 0.5D), pk, true);
      }

   }

   public static void shieldBreak(PassQuads q, PassBr.Cols cl, PassBr.Frame fr, int sid, double age) {
      double fl = PassMath.clamp01(1.0D - age / 6.0D);
      PassFx.glow(q, fr.c, 2.2D * fl + 0.3D, cl.main, fl);
      PassFx.star(q, fr.c, 2.6D * fl, 0.4D, PassFx.whiter(cl.main, 0.7D), fl, true);
      double[] rim = PassFx.whiter(cl.rim, 0.25D);

      for(int i = 0; i < FACETS.length; ++i) {
         double[][] tri = FACETS[i];
         double life = 26.0D + 14.0D * PassMath.h((double)i, (double)sid, 3.0D);
         double k = PassMath.clamp01(1.0D - (age - life + 10.0D) / 10.0D);
         if (!(k <= 0.0D)) {
            double[][] w = new double[][]{toWorld(fr, tri[0]), toWorld(fr, tri[1]), toWorld(fr, tri[2])};
            double[] cen = PassMath.mul(PassMath.add(PassMath.add(w[0], w[1]), w[2]), 0.3333333333333333D);
            double[] n = triNormal(w[0], w[1], w[2]);
            double[] out = PassMath.norm(PassMath.add(PassMath.mul(n, 0.7D), PassMath.sub(cen, fr.c)));
            double vel = 0.12D + 0.12D * PassMath.h((double)i, (double)sid, 4.0D);
            double dr = (1.0D - Math.pow(0.93D, age)) / 0.07D;
            double[] move = PassMath.add(PassMath.mul(out, vel * dr), PassMath.v(0.0D, 0.02D * age - 0.012D * age * age, 0.0D));
            double spin = age * (0.15D + 0.25D * PassMath.h((double)i, (double)sid, 5.0D)) * (i % 2 != 0 ? 1.0D : -1.0D);
            double[] ax = PassMath.norm(PassMath.cross(n, out));
            double cs = Math.cos(spin);
            double sn = Math.sin(spin);

            for(int j = 0; j < 3; ++j) {
               double[] d = PassMath.sub(w[j], cen);
               double[] rot = PassMath.add(PassMath.add(PassMath.mul(d, cs), PassMath.mul(PassMath.cross(ax, d), sn)), PassMath.mul(ax, PassMath.dot(ax, d) * (1.0D - cs)));
               w[j] = PassMath.add(PassMath.add(cen, rot), move);
            }

            double tw = 0.7D + 0.3D * Math.sin(age * 0.9D + (double)i * 1.7D);
            q.quad(1, w[0], w[1], w[2], w[2], 40, cl.main[0], cl.main[1], cl.main[2], 0.45D * k * tw, (double[])null);
            q.ribbon(1, w[0], w[1], 0.025D, 32, rim[0], rim[1], rim[2], 0.8D * k, 0.8D * k);
            q.ribbon(1, w[1], w[2], 0.025D, 32, rim[0], rim[1], rim[2], 0.8D * k, 0.8D * k);
            q.ribbon(1, w[2], w[0], 0.025D, 32, rim[0], rim[1], rim[2], 0.8D * k, 0.8D * k);
         }
      }

      PassFx.sparks(q, fr.c, (double)(700 + sid), age, 16, 0.45D, PassFx.whiter(cl.main, 0.5D), 1.0D, (new PassFx.O()).life(16.0D).g(0.03D).up(0.2D));
   }

   public static void ramHit(PassQuads q, PassBr.Cols cl, double[] at, double[] dir, double born, double age) {
      double k = PassMath.clamp01(1.0D - age / 8.0D);
      PassFx.glow(q, at, 1.7D * k + 0.2D, cl.main, k);
      PassFx.star(q, at, 2.4D * k, 0.4D, PassFx.whiter(cl.main, 0.6D), k, true);
      PassFx.sparks(q, at, 611.0D + born, age, 18, 0.32D, PassFx.whiter(cl.main, 0.5D), 1.0D, (new PassFx.O()).dir(dir).cone(0.9D).life(14.0D));

      for(int i = 0; i < 9; ++i) {
         double a = (double)i * 2.39D;
         double[] p = PassMath.add(at, PassMath.v(Math.cos(a) * age * 0.11D + dir[0] * age * 0.05D, Math.sin((double)i * 1.7D) * age * 0.07D + 0.02D * age, Math.sin(a) * age * 0.11D + dir[2] * age * 0.05D));
         q.bill(1, p, 0.13D, (double)i + age * 0.3D, 39, cl.main[0], cl.main[1], cl.main[2], 0.85D * PassMath.clamp01(1.0D - age / 16.0D));
      }

   }

   static int section(double v) {
      return (int)Math.floor(v) >> 4;
   }

   public static double step(double[] o, double[] d, double[] lo, double[] hi, double[] feet, double range) {
      int sx = section(feet[0]);
      int sy = section(feet[1]);
      int sz = section(feet[2]);

      for(int i = 0; (double)i < range * 2.0D; ++i) {
         double f = (double)((float)i / 2.0F);
         double x = o[0] + d[0] * f;
         double y = o[1] + d[1] * f;
         double z = o[2] + d[2] * f;
         if (lo[0] < x + 0.25D && hi[0] > x - 0.25D && lo[1] < y + 0.25D && hi[1] > y - 0.25D && lo[2] < z + 0.25D && hi[2] > z - 0.25D && sx >= section(x - 0.25D - 2.0D) && sx <= section(x + 0.25D + 2.0D) && sy >= section(y - 0.25D - 4.0D) && sy <= section(y + 0.25D) && sz >= section(z - 0.25D - 2.0D) && sz <= section(z + 0.25D + 2.0D)) {
            return f;
         }
      }

      return -1.0D;
   }

   public static double[] face(double[] c, double r, double[] eye, double[] dir, double ts) {
      double[] oc = PassMath.sub(eye, c);
      double b = PassMath.dot(oc, dir);
      double cc = PassMath.dot(oc, oc) - r * r;
      double disc = b * b - cc;
      if (cc < 0.0D) {
         double t = -b + Math.sqrt(Math.max(0.0D, disc));
         double[] n = PassMath.norm(PassMath.sub(c, PassMath.add(eye, PassMath.mul(dir, t))));
         return new double[]{t, n[0], n[1], n[2]};
      } else {
         if (disc >= 0.0D) {
            double t = -b - Math.sqrt(disc);
            if (t > 0.0D) {
               double[] n = PassMath.norm(PassMath.sub(PassMath.add(eye, PassMath.mul(dir, t)), c));
               return new double[]{t, n[0], n[1], n[2]};
            }
         }

         return new double[]{ts, -dir[0], -dir[1], -dir[2]};
      }
   }

   public static double[] plate(PassBr.Frame fr, double[] eye, double[] dir, double ts) {
      double dn = PassMath.dot(dir, fr.f);
      if (Math.abs(dn) > 1.0E-4D) {
         double t = PassMath.dot(PassMath.sub(fr.c, eye), fr.f) / dn;
         if (t > 0.0D) {
            double[] d = PassMath.sub(PassMath.add(eye, PassMath.mul(dir, t)), fr.c);
            double x = PassMath.dot(d, fr.r) / 1.2D;
            double y = PassMath.dot(d, fr.u) / 1.5D;
            if (x * x + y * y <= 1.0D) {
               double k = dn < 0.0D ? 1.0D : -1.0D;
               return new double[]{t, fr.f[0] * k, fr.f[1] * k, fr.f[2] * k};
            }
         }
      }

      return new double[]{ts, -dir[0], -dir[1], -dir[2]};
   }

   public static double raySphere(double[] from, double[] dir, double[] c, double r) {
      double[] oc = PassMath.sub(from, c);
      double b = PassMath.dot(oc, dir);
      double cc = PassMath.dot(oc, oc) - r * r;
      if (cc <= 0.0D) {
         return -1.0D;
      } else {
         double disc = b * b - cc;
         if (disc < 0.0D) {
            return -1.0D;
         } else {
            double t = -b - Math.sqrt(disc);
            return t > 0.0D ? t : -1.0D;
         }
      }
   }

   public static final class Cols {
      public final double[] main;
      public final double[] rim;
      public final double[] gold;
      public final double[] dark;
      public final boolean copy;

      Cols(double[] main, double[] rim, double[] gold, double[] dark, boolean copy) {
         this.main = main;
         this.rim = rim;
         this.gold = gold;
         this.dark = dark;
         this.copy = copy;
      }
   }

   public static final class Crack {
      public final double t;
      public final double[] dir;

      public Crack(double t, double[] dir) {
         this.t = t;
         this.dir = dir;
      }
   }

   public static final class Frame {
      public final double[] c;
      public final double[] f;
      public final double[] r;
      public final double[] u;

      public Frame(double[] c, double[] f) {
         double[][] bs = PassQuads.basis(f);
         this.c = c;
         this.f = f;
         this.r = bs[0];
         this.u = bs[1];
      }

      public Frame(double[] c, double[] f, double[] r, double[] u) {
         this.c = c;
         this.f = f;
         this.r = r;
         this.u = u;
      }
   }

   public static final class Hit {
      public final double t;
      public final double[] dir;
      public final double k;
      public final boolean cont;

      public Hit(double t, double[] dir, double k, boolean cont) {
         this.t = t;
         this.dir = dir;
         this.k = k;
         this.cont = cont;
      }
   }

   public static final class Shell {
      public int id;
      public double[] at;
      public double size;
      public double build;
      public double gone = -1.0D;
      public boolean shattered;
      public int crossed;
      public double growT = -99.0D;
      public double growFrom;
      public double growSign;
      public final List<PassBr.Hit> hits = new ArrayList();
      public final List<PassBr.Crack> cracks = new ArrayList();
   }
}
