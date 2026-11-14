package com.myriam.unrp.pass;

public final class PassPd {
   public static final double[] AMBER = PassFx.rgb(15242256);
   public static final double[] HEART = PassFx.rgb(15217722);
   public static final double[] COPY_HEART = PassFx.rgb(4863028);
   public static final double[] LAUNCH_ROCK = new double[]{0.45D, 0.35D, 0.25D};
   public static final double[] LAUNCH_DUST = new double[]{0.55D, 0.5D, 0.45D};
   public static final double[] WAVE_ROCK = new double[]{0.5D, 0.4D, 0.3D};
   public static final double[] WAVE_DUST = new double[]{0.55D, 0.48D, 0.4D};
   public static final double[] ROAR_DUST = new double[]{0.55D, 0.5D, 0.44D};
   public static final double[] BURN = new double[]{1.0D, 0.6D, 0.15D};
   public static final double[] BLADES = new double[]{-2.5D, -0.75D, 0.75D, 2.5D};
   public static final int MARK_CLOSE = 6;
   public static final int MARK_FLASH = 8;
   public static final int MARK_EVERY = 20;
   public static final int MARK_BEAT = 6;
   public static final int LOCKON_LIFE = 16;
   public static final int LOCKON_DRAW = 7;
   public static final int SONAR_EVERY = 20;
   public static final int SONAR_OFF = 10;
   public static final int SONAR_RUN = 12;
   public static final int SONAR_TAIL = 12;
   public static final int SONAR_GROW = 8;
   public static final int BEAT_LIFE = 24;
   public static final int BEAT_RING = 14;
   public static final int BEAT_GAP = 4;
   public static final int BEAT_EVERY = 40;
   public static final int RUSH_LIFE = 30;
   public static final int RUSH_GHOSTS = 12;
   public static final int GHOST_EVERY = 2;
   public static final int GHOST_LIFE = 9;
   public static final double GHOST_A = 0.5D;
   public static final int SLASH_LIFE = 30;
   public static final int WAVE_LIFE = 90;
   public static final int STEPS = 12;
   public static final int ROCK_EVERY = 3;
   public static final int ROAR_LIFE = 40;
   public static final int ROAR_AT = 8;
   public static final int ACT_FLARE = 12;
   public static final int STRIPES_IN = 14;
   public static final int CLAWS_IN = 6;
   public static final int CLAWS_FLARE = 10;
   public static final double CLAWS_LOW = 0.35D;
   public static final double FOCUS_FLARE = 0.8D;
   public static final double CHEST = 0.66D;
   public static final double BEAT_AT = 0.62D;

   private PassPd() {
   }

   public static PassCast.Pal pal(int rgb, boolean copy) {
      if (copy) {
         return new PassCast.Pal(PassCast.SMOKE_MAIN, PassCast.SMOKE_HOT, PassCast.SMOKE_DARK, true);
      } else {
         double[] m = PassFx.rgb(rgb);
         return new PassCast.Pal(m, PassFx.whiter(m, 0.6D), PassFx.scale(m, 0.25D), false);
      }
   }

   public static double[] heart(PassCast.Pal p) {
      return p.copy ? COPY_HEART : HEART;
   }

   public static double[] markColor(PassCast.Pal p) {
      return p.copy ? AMBER : p.main;
   }

   public static double[] preyTint(PassCast.Pal p) {
      return PassFx.whiter(p.main, 0.25D);
   }

   public static double[] sensedTint(PassCast.Pal p) {
      return PassFx.mix(p.main, PassMath.v(1.0D, 1.0D, 1.0D), 0.45D);
   }

   public static double stripes(double age) {
      return age < 0.0D ? 0.0D : PassMath.clamp01(age / 14.0D);
   }

   public static double burn(double age, double t) {
      return age < 0.0D ? 0.0D : (age < 3.0D ? 1.3D : 0.28D + 1.0D * Math.exp(-(age - 3.0D) / 7.0D)) * (0.85D + 0.15D * Math.sin(t * 0.5D));
   }

   public static double clawsIn(double age) {
      return age < 0.0D ? 0.0D : PassMath.clamp01(age / 6.0D);
   }

   public static double clawsFlare(double age) {
      return age < 0.0D ? 0.0D : (age < 10.0D ? 1.0D : 0.35D);
   }

   public static double[] chestFront(double[] feet, double height, double[] eye) {
      double[] p = PassMath.v(feet[0], feet[1] + height * 0.66D, feet[2]);
      double[] to = PassMath.sub(eye, p);
      double d = PassMath.len(to);
      return d > 0.001D ? PassMath.add(p, PassMath.mul(to, Math.min(0.42D, d * 0.5D) / d)) : p;
   }

   public static void mark(PassQuads q, double[] p, double[] c, double age, double t) {
      double close = PassMath.outCubic(PassMath.clamp01(age / 6.0D));
      double size = 0.3D * (1.0D + 1.2D * (1.0D - close)) * (1.0D + 0.04D * Math.sin(t * 0.3D));
      double a = Math.min(1.0D, 0.4D + close);
      double[] r = PassMath.mul(q.right, size);
      double[] u = PassMath.mul(q.up, size);
      q.quad(6, PassMath.sub(PassMath.sub(p, r), u), PassMath.sub(PassMath.add(p, r), u), PassMath.add(PassMath.add(p, r), u), PassMath.add(PassMath.sub(p, r), u), -1, c[0] * a, c[1] * a, c[2] * a, 1.0D, (double[])null);
      if (age < 8.0D) {
         PassFx.glow(q, p, 0.7D * (1.0D - age / 8.0D) + 0.2D, c, 0.6D * (1.0D - age / 8.0D));
      }

   }

   public static void lockon(PassQuads q, PassCast.Pal p, double[] eye, double[] chest, double age) {
      if (age < 7.0D) {
         double k = 1.0D - age / 7.0D;
         q.ribbon(1, eye, chest, 0.05D, 31, p.hot[0], p.hot[1], p.hot[2], 0.9D * k, 0.2D * k);
      }

   }

   public static double[] sonarAt(PassFx.Ground ground, double[] feet) {
      return PassMath.v(feet[0], ground.at(feet[0], feet[2], feet[1] + 0.5D) + 0.05D, feet[2]);
   }

   public static boolean sonarDone(double t, double end) {
      return end >= 0.0D && t - end > 12.0D;
   }

   public static void sonar(PassQuads q, PassCast.Pal p, double[] g, double reach, double t, double start, double end) {
      double since = t - start;

      for(int k = 0; k < 2; ++k) {
         double ph = since - (double)(k * 10);
         double cyc = ph % 20.0D;
         if (!(ph < 0.0D) && (!(end >= 0.0D) || !(ph - cyc > end - start))) {
            double u = cyc / 12.0D;
            if (!(u > 1.0D)) {
               double r = reach * PassMath.outCubic(u);
               if (r > 1.5D) {
                  q.ring(1, g, r - 1.3D, 2.2D, 32, p.main, 0.16D * (1.0D - u), 96, (double[])null, 0.0D);
               }

               q.ring(1, g, r, 0.5D + r * 0.02D, 32, p.main, 0.5D * (1.0D - u), 96, (double[])null, 0.0D);
               q.ring(1, PassMath.v(g[0], g[1] + 0.01D, g[2]), r, 0.12D, 32, p.hot, 0.8D * (1.0D - u), 96, (double[])null, 0.0D);
            }
         }
      }

      double on = end >= 0.0D ? PassMath.clamp01(1.0D - (t - end) / 12.0D) : PassMath.clamp01(since / 8.0D);
      q.pool(g, 2.2D, p.main, 0.25D * on);
   }

   public static void beat(PassQuads q, double[] c, double[] col, double age) {
      double[] f = q.fwd();

      for(int j = 0; j < 2; ++j) {
         double a = age - (double)(j * 4);
         if (!(a < 0.0D) && !(a > 14.0D)) {
            double u = a / 14.0D;
            q.ring(1, c, 0.25D + 0.9D * PassMath.outCubic(u), 0.12D, 32, col, 0.85D * (1.0D - u), 28, f, 0.0D);
            if (a < 4.0D) {
               PassFx.glow(q, c, 0.5D * (1.0D - a / 4.0D) + 0.2D, col, 0.7D * (1.0D - a / 4.0D));
            }
         }
      }

   }

   public static void rush(PassQuads q, PassCast.Pal p, PassFx.Ground ground, double[] light, double[] g, double[] dir, double age, double[] eye) {
      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.06D, g[2]), 0.5D + 2.4D * PassMath.outCubic(age / 9.0D), 0.35D, p.main, PassMath.clamp01(1.0D - age / 10.0D), (double[])null);
      PassFx.debris(q, ground, light, PassMath.v(g[0], g[1] + 0.15D, g[2]), 51.0D, age, 8, 0.2D, LAUNCH_ROCK, (new PassFx.O()).lie(14.0D));

      for(int i = 0; i < 6; ++i) {
         double az = Math.atan2(-dir[2], -dir[0]) + ((double)i - 2.5D) * 0.35D;
         PassFx.smoke(q, light, PassMath.v(g[0] + Math.cos(az) * 0.4D, g[1] + 0.2D, g[2] + Math.sin(az) * 0.4D), (double)(60 + i), age, 1, LAUNCH_DUST, 0.45D, (new PassFx.O()).life(20.0D).rise(0.02D).size(0.45D));
      }

      if (eye != null && age < 10.0D) {
         double[] back = PassMath.mul(dir, -1.6D * (1.0D - age / 10.0D));
         q.ribbon(1, eye, PassMath.add(eye, back), 0.08D, 31, p.hot[0], p.hot[1], p.hot[2], 0.9D * (1.0D - age / 10.0D), 0.0D);
      }

   }

   public static void slash(PassQuads q, PassCast.Pal p, PassFx.Ground ground, double[] c, double[] dir, double born, double age, boolean core) {
      double k = PassMath.clamp01(1.0D - age / 10.0D);
      double[] up = PassMath.v(0.0D, 1.0D, 0.0D);
      double[] side = PassMath.norm(PassMath.cross(dir, up));
      double[] ax = PassMath.norm(PassMath.add(PassMath.mul(side, 0.75D), PassMath.mul(up, -0.65D)));
      double[] ay = PassMath.norm(PassMath.cross(ax, dir));
      if (core && age < 8.0D) {
         double s1 = 1.3D + age * 0.05D;
         double s2 = 1.6D + age * 0.06D;
         q.plane(1, PassMath.sub(c, PassMath.mul(dir, 0.3D)), ax, ay, s1, 0.0D, 45, p.hot[0], p.hot[1], p.hot[2], 1.0D - age / 8.0D, s1);
         q.plane(1, PassMath.sub(c, PassMath.mul(dir, 0.35D)), ax, ay, s2, 0.0D, 45, p.main[0], p.main[1], p.main[2], 0.7D * (1.0D - age / 8.0D), s2);
      }

      if (core) {
         PassFx.glow(q, c, 2.0D * k + 0.2D, p.main, k);
      }

      if (core && age < 4.0D) {
         PassFx.star(q, c, 2.6D * (1.0D - age / 4.0D), 0.3D, p.hot, 1.0D - age / 4.0D, true);
      }

      PassFx.sparks(q, c, 333.0D + born, age, 22, 0.4D, p.hot, 1.0D, (new PassFx.O()).dir(dir).cone(0.8D).life(14.0D).g(0.03D));
      double[] g = PassMath.v(c[0], ground.at(c[0], c[2], c[1]) + 0.06D, c[2]);
      PassFx.shock(q, g, 0.4D + 2.2D * PassMath.outCubic(age / 10.0D), 0.3D, p.main, PassMath.clamp01(1.0D - age / 12.0D), (double[])null);
   }

   public static double[][] path(PassPd.Solid solid, double[] start, double[] dir) {
      double[][] out = new double[13][];
      out[0] = (double[])start.clone();
      double[] pos = (double[])start.clone();
      int n = 1;

      for(int step = 1; step <= 12; ++step) {
         double[] next = PassMath.add(pos, dir);
         int bx = (int)Math.floor(next[0]);
         int by = (int)Math.floor(next[1]);
         int bz = (int)Math.floor(next[2]);
         if (solid.at(bx, by - 1, bz)) {
            if (solid.at(bx, by, bz)) {
               if (solid.at(bx, by + 1, bz)) {
                  break;
               }

               next = PassMath.v(next[0], next[1] + 1.0D, next[2]);
            }
         } else {
            if (!solid.at(bx, by - 2, bz)) {
               break;
            }

            next = PassMath.v(next[0], next[1] - 1.0D, next[2]);
         }

         pos = next;
         out[n++] = (double[])next.clone();
      }

      double[][] p = new double[n][];
      System.arraycopy(out, 0, p, 0, n);
      return p;
   }

   public static void wave(PassQuads q, PassCast.Pal p, PassFx.Ground ground, double[] light, double[][] path, double age) {
      int n = path.length;

      for(int i = 1; i < n; ++i) {
         double[] a = path[i - 1];
         double[] b = path[i];
         double segAge = age - (double)i;
         if (segAge < 0.0D) {
            break;
         }

         double hot = PassMath.clamp01(1.0D - segAge / 26.0D);
         double fade = PassMath.clamp01(1.0D - (segAge - 40.0D) / 30.0D);
         if (!(fade <= 0.0D)) {
            double[] ga = PassMath.v(a[0], ground.at(a[0], a[2], a[1] + 0.6D), a[2]);
            double[] gb = PassMath.v(b[0], ground.at(b[0], b[2], b[1] + 0.6D), b[2]);
            PassFx.crackLine(q, ga, gb, 0.75D, PassFx.mix(p.main, p.hot, hot), (0.25D + 0.75D * hot) * fade, 0.9D * fade);
            if (segAge < 14.0D) {
               PassFx.debris(q, ground, light, PassMath.v(gb[0], gb[1] + 0.2D, gb[2]), (double)(900 + i * 7), segAge, 6, 0.3D, WAVE_ROCK, (new PassFx.O()).lie(18.0D).size(0.22D).lift(1.4D));
               PassFx.smoke(q, light, PassMath.v(gb[0], gb[1] + 0.3D, gb[2]), (double)(960 + i), segAge, 2, WAVE_DUST, 0.6D, (new PassFx.O()).life(22.0D).rise(0.04D).spread(0.05D).size(0.55D));
               if (segAge < 6.0D) {
                  PassFx.glow(q, PassMath.v(gb[0], gb[1] + 0.4D, gb[2]), 1.2D * (1.0D - segAge / 6.0D), p.main, 1.0D - segAge / 6.0D);
                  PassFx.sparks(q, PassMath.v(gb[0], gb[1] + 0.2D, gb[2]), (double)(970 + i), segAge, 6, 0.25D, p.hot, 1.0D - segAge / 6.0D, (new PassFx.O()).life(8.0D).g(0.04D).up(0.9D));
               }
            }

            q.pool(gb, 1.0D, p.main, 0.3D * hot);
         }
      }

      double[] s0 = path[0];
      if (age < 3.0D) {
         PassFx.shock(q, PassMath.v(s0[0], s0[1] + 0.06D, s0[2]), 0.5D + 2.6D * PassMath.outCubic(age / 3.0D), 0.5D, p.main, 1.0D - age / 3.0D, (double[])null);
      }

      if (age < 20.0D) {
         PassFx.shock(q, PassMath.v(s0[0], s0[1] + 0.05D, s0[2]), 0.6D + 3.2D * PassMath.outCubic(age / 14.0D), 0.4D, p.hot, PassMath.clamp01(1.0D - age / 16.0D), (double[])null);
      }

      q.decalAt(1, s0, 1.8D, 0.4D, 0.8D * PassMath.clamp01(1.0D - (age - 60.0D) / 30.0D));
   }

   public static void roar(PassQuads q, PassCast.Pal p, double[] light, double[] g, double[] chest, double age, boolean rings) {
      double k = PassMath.clamp01(1.0D - age / 14.0D);
      PassFx.shock(q, PassMath.v(g[0], g[1] + 0.06D, g[2]), 0.5D + 4.5D * PassMath.outCubic(age / 12.0D), 0.6D, p.main, k, (double[])null);
      double[] f = q.fwd();

      for(int r = 0; rings && r < 3; ++r) {
         double ra = age - (double)(r * 3);
         if (!(ra < 0.0D) && !(ra > 12.0D)) {
            q.ring(1, chest, 0.6D + ra * 0.35D, 0.22D, 32, PassFx.mix(p.main, p.hot, 0.35D), 0.55D * (1.0D - ra / 12.0D), 36, f, ra * 0.1D);
         }
      }

      for(int i = 0; i < 12; ++i) {
         double az = (double)i / 12.0D * 6.283D;
         double rr = 0.6D + 4.0D * PassMath.outCubic((age - PassMath.h((double)i, 1.0D, 4.0D) * 2.0D) / 18.0D);
         PassFx.smoke(q, light, PassMath.v(g[0] + Math.cos(az) * rr, g[1] + 0.25D, g[2] + Math.sin(az) * rr), (double)(500 + i), age, 1, ROAR_DUST, 0.5D, (new PassFx.O()).life(26.0D).rise(0.02D).size(0.6D));
      }

      PassFx.embers(q, PassMath.v(g[0], g[1] + 0.6D, g[2]), 77.0D, age, 16, p.main, 0.9D, (new PassFx.O()).life(30.0D).r(0.7D).rise(0.06D));
   }

   public static void eyeTrails(PassQuads q, PassCast.Pal p, PassBody head, double[][] at, double[] pos, double[] prev, double[] look, boolean focusing) {
      double sp = Math.hypot(pos[0] - prev[0], pos[2] - prev[2]);
      double k = PassMath.clamp01((sp - 0.15D) / 0.4D) + (focusing ? 0.35D : 0.0D);
      if (!(k <= 0.02D)) {
         double[] vel = PassMath.sub(pos, prev);
         double[] back = PassMath.len(vel) > 0.001D ? PassMath.mul(PassMath.norm(vel), -Math.min(1.6D, 0.4D + sp * 2.2D)) : PassMath.mul(look, -0.5D);

         for(double[] e : at) {
            double[] pp = head.at(0, e[0], e[1], -4.6D);
            q.ribbon(1, pp, PassMath.add(pp, back), 0.07D, 31, p.main[0], p.main[1], p.main[2], 0.9D * Math.min(1.0D, k), 0.0D);
         }

      }
   }

   public static void sensed(PassQuads q, PassCast.Pal p, double[] mid) {
      PassFx.glow(q, mid, 1.4D, p.main, 0.3D);
   }

   public static void claws(PassQuads q, PassCast.Pal p, PassBody b, double k, double flare) {
      if (!(k <= 0.01D) && flare > 0.0D) {
         for(int arm = 0; arm < 2; ++arm) {
            int part = arm == 0 ? 2 : 3;
            double x0 = arm == 0 ? -3.5D : -1.5D;
            double bx0 = arm == 0 ? -3.5D : 0.5D;

            for(double z : BLADES) {
               PassFx.glow(q, b.at(part, bx0 + 1.5D, 15.5D, z), 0.18D + 0.25D * flare, p.main, 0.55D * flare);
            }

            PassFx.glow(q, b.at(part, x0 + 2.5D, 10.0D, 0.0D), 0.5D + 0.5D * flare, p.main, 0.35D * flare);
         }

      }
   }

   public interface Solid {
      boolean at(int var1, int var2, int var3);
   }
}
