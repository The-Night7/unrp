package com.myriam.unrp.uo;




public final class LashShape {
   public static final double[] BODY = new double[]{0.05D, 0.042D, 0.07D};
   public static final double[] SMOKE = new double[]{0.11D, 0.08D, 0.16D};
   public static final double[] RIM = new double[]{0.35D, 0.9D, 0.95D};
   public static final double[] RAGE = new double[]{1.0D, 0.3D, 0.42D};
   public static final int WARD_TENDRILS = 6;
   public static final double WARD_SPIN = 0.32D;
   public static final double WARD_LEAD = 0.9D;
   public static final double WARD_ROOT_R = 0.28D;
   public static final double WARD_TIP_R = 2.7D;
   public static final double[] WARD_ROOT_Y = new double[]{1.0D, 1.28D, 1.12D, 1.38D, 0.96D, 1.22D};
   public static final double[] WARD_TIP_Y = new double[]{0.45D, 1.0D, 1.55D};
   public static final double WARD_ARCH = 0.25D;
   public static final double WARD_TRAIL = 1.1D;
   public static final int WARD_PTS = 22;
   public static final int WARD_TRAIL_PTS = 10;
   public static final double WARD_HAND_R = 2.4D;
   public static final double WARD_HAND_SWEEP = 0.9D;
   public static final double WARD_GROW = 5.0D;
   public static final double WARD_SHRINK = 6.0D;
   public static final double FRENZY_W0 = 0.24D;
   public static final double FRENZY_W1 = 0.07D;
   public static final double FRENZY_LEG_W0 = 0.2D;
   public static final double FRENZY_LEG_W1 = 0.11D;
   public static final int FRENZY_PTS = 28;
   public static final double FRENZY_GROW = 5.0D;
   public static final double FRENZY_FADE = 8.0D;
   public static final double FRENZY_BEAT = 10.0D;
   public static final double POUND_SPREAD = 11.0D;
   public static final int POUND_CRACKS = 10;

   private LashShape() {
   }

   public static double[] bez(double[] a, double[] c, double[] b, double s) {
      double u = 1.0D - s;
      return new double[]{u * u * a[0] + 2.0D * u * s * c[0] + s * s * b[0], u * u * a[1] + 2.0D * u * s * c[1] + s * s * b[1], u * u * a[2] + 2.0D * u * s * c[2] + s * s * b[2]};
   }

   public static double[][] basis(double[] n) {
      double[] up0 = Math.abs(n[1]) > 0.97D ? new double[]{1.0D, 0.0D, 0.0D} : new double[]{0.0D, 1.0D, 0.0D};
      double[] right = LashMath.norm(LashMath.cross(n, up0));
      double[] up = LashMath.norm(LashMath.cross(right, n));
      return new double[][]{right, up};
   }

   public static double[][] shape(double[] a, double[] b, int n, double extend, double amp, double waves, double phase, double curl, double arch, double sag) {
      int nn = n > 0 ? n : 22;
      double[] d = LashMath.sub(b, a);
      double len = LashMath.len(d);
      if (len == 0.0D) {
         len = 0.001D;
      }

      double[] dir = LashMath.mul(d, 1.0D / len);
      double[][] bs = basis(dir);
      double[] side = bs[0];
      double[] up = bs[1];
      double[] mid = LashMath.mul(LashMath.add(a, b), 0.5D);
      double[] ctrl = LashMath.add(mid, LashMath.add(LashMath.mul(side, curl * len), LashMath.add(LashMath.mul(up, arch * len), new double[]{0.0D, -sag * len, 0.0D})));
      double ext = LashMath.clamp01(extend);
      double wv = waves != 0.0D ? waves : Math.max(1.5D, len * 0.35D);
      double[][] pts = new double[nn + 1][];

      for(int i = 0; i <= nn; ++i) {
         double s = (double)i / (double)nn * ext;
         double[] p = bez(a, ctrl, b, s);
         double env = 4.0D * s * (1.0D - s);
         double w = amp * Math.sin(s * wv * Math.PI * 2.0D - phase) * env;
         double w2 = amp * 0.5D * Math.cos(s * wv * Math.PI * 1.3D - phase * 0.8D) * env;
         pts[i] = LashMath.add(p, LashMath.add(LashMath.mul(side, w), LashMath.mul(up, w2)));
      }

      return pts;
   }

   public static double[][] line(double[] a, double[] b, double extend, double amp, double phase) {
      return shape(a, b, 22, extend, amp, 0.0D, phase, 0.0D, 0.0D, 0.0D);
   }

   public static double[][] helix(double[] center, double from, double to, double turns, double r, double s0, double s1, int n, double spin) {
      int nn = n > 0 ? n : 26;
      double[][] pts = new double[nn + 1][];

      for(int i = 0; i <= nn; ++i) {
         double s = s0 + (s1 - s0) * (double)i / (double)nn;
         double a = s * turns * Math.PI * 2.0D + spin;
         pts[i] = new double[]{center[0] + Math.cos(a) * r, from + (to - from) * s, center[2] + Math.sin(a) * r};
      }

      return pts;
   }

   public static double[][] whipPts(double[] rr, double[] tt, double k, double[] hint, int n) {
      int nn = n > 0 ? n : 26;
      double[] d = LashMath.sub(tt, rr);
      double lw = LashMath.len(d);
      if (lw == 0.0D) {
         lw = 0.001D;
      }

      double[] e1 = LashMath.mul(d, 1.0D / lw);
      double[] h = hint == null ? new double[]{0.0D, 1.0D, 0.0D} : hint;
      double[] e2 = LashMath.sub(h, LashMath.mul(e1, LashMath.dot(h, e1)));
      e2 = LashMath.len(e2) < 1.0E-4D ? basis(e1)[1] : LashMath.norm(e2);
      double kk = LashMath.clamp01(k);
      double[] f = LashMath.add(rr, LashMath.mul(e1, kk * lw));
      double r = Math.max(0.05D, Math.min(0.75D, (1.0D - kk) * lw * 0.22D));
      double[][] pts = new double[nn + 1][];

      for(int i = 0; i <= nn; ++i) {
         double u = (double)i / (double)nn;
         double[] p;
         if (u <= kk) {
            p = LashMath.add(rr, LashMath.mul(e1, u * lw));
         } else {
            double rem = (u - kk) * lw;
            if (rem <= Math.PI * r) {
               double th = rem / r;
               p = LashMath.add(f, LashMath.add(LashMath.mul(e2, r * (1.0D - Math.cos(th))), LashMath.mul(e1, r * Math.sin(th))));
            } else {
               p = LashMath.add(f, LashMath.add(LashMath.mul(e2, 2.0D * r), LashMath.mul(e1, -(rem - Math.PI * r))));
            }
         }

         pts[i] = p;
      }

      return pts;
   }

   public static double[][] strikePts(double[] rr, double[] tt, double lt, double[] cock, double t) {
      double[] c = cock == null ? new double[]{0.0D, 1.0D, 0.0D} : cock;
      if (lt < 3.0D) {
         double[][] full = whipPts(rr, tt, 0.0D, c, 28);
         int m = Math.max(2, (int)Math.round(28.0D * LashMath.outCubic(lt / 3.0D)) + 1);
         double[][] pts = new double[m][];
         System.arraycopy(full, 0, pts, 0, m);
         return pts;
      } else if (lt < 7.0D) {
         double k = Math.pow((lt - 3.0D) / 4.0D, 1.7D);
         return whipPts(rr, tt, k, c, 28);
      } else {
         double r = lt - 7.0D;
         double slack = LashMath.clamp01(r / 6.0D);
         double[] tip = LashMath.lerp(tt, LashMath.add(rr, LashMath.mul(LashMath.sub(tt, rr), 0.45D)), LashMath.outCubic(slack));
         return shape(rr, tip, 22, 1.0D, 0.35D * (1.0D - slack) + 0.05D, 0.0D, t * 1.3D, 0.1D, 0.0D, 0.08D * slack);
      }
   }

   public static double[][] concat(double[][] a, double[][] b) {
      double[][] out = new double[a.length + Math.max(0, b.length - 1)][];
      System.arraycopy(a, 0, out, 0, a.length);
      if (b.length > 1) {
         System.arraycopy(b, 1, out, a.length, b.length - 1);
      }

      return out;
   }

   public static double[] widths(int n, double w0, double w1, double scale) {
      double a = (w0 != 0.0D ? w0 : 0.15D) * 1.4D;
      double b = (w1 != 0.0D ? w1 : 0.045D) * 1.4D;
      double[] out = new double[n];

      for(int i = 0; i < n; ++i) {
         double s = n > 1 ? (double)i / (double)(n - 1) : 0.0D;
         out[i] = (a + (b - a) * s) * scale;
      }

      return out;
   }

   public static double[] bodyAlpha(int n, double a) {
      double[] out = new double[n];

      for(int i = 0; i < n; ++i) {
         double s = n > 1 ? (double)i / (double)(n - 1) : 0.0D;
         double fade = Math.min(1.0D, (1.0D - s) * 8.0D + 0.6D);
         out[i] = 0.96D * a * fade;
      }

      return out;
   }

   public static double[] rimAlpha(int n, double a, double rimA, double t) {
      double ra = rimA != 0.0D ? rimA : 0.8D;
      double[] out = new double[n];

      for(int i = 0; i < n; ++i) {
         double s = n > 1 ? (double)i / (double)(n - 1) : 0.0D;
         out[i] = ra * a * (0.7D + 0.3D * Math.sin(s * 14.0D - t * 0.6D));
      }

      return out;
   }

   public static double glintPhase(double t, int k, double seed) {
      return (t * 0.045D + (double)k / 3.0D + seed * 0.13D) % 1.0D;
   }

   public static int glintIndex(double q, int n) {
      return Math.min(n - 1, (int)Math.floor(q * (double)(n - 1)));
   }

   public static double wispPhase(double t, int k, double seed) {
      return (t * 0.03D + (double)k / 4.0D + seed * 0.31D) % 1.0D;
   }

   public static int wispIndex(int k, double seed, int n) {
      return Math.min(n - 1, (int)Math.floor(((double)k * 0.29D + seed * 0.17D) % 1.0D * (double)(n - 1)));
   }

   public static double[] wispAt(double[] base, int k, double t, double ph) {
      return LashMath.add(base, new double[]{Math.sin((double)(k * 3) + t * 0.05D) * 0.15D * ph, ph * 0.45D, Math.cos((double)(k * 2)) * 0.15D * ph});
   }

   public static double[] sparkAt(double[] at, int i, double age) {
      double a = (double)i * 1.047D + 0.4D;
      return LashMath.add(at, new double[]{Math.cos(a) * 0.12D * age, Math.sin(a * 1.7D) * 0.08D * age, Math.sin(a) * 0.12D * age});
   }

   public static double wardAngle(double t, int k) {
      return (double)k * (Math.PI / 3D) + t * 0.32D;
   }

   public static double wardReach(double age, double left) {
      return LashMath.outCubic(Math.min(age / 5.0D, left / 6.0D));
   }

   public static double[] wardTip(double[] p, double scale, double t, int k, double reach) {
      double a = wardAngle(t, k);
      double r0 = 0.28D * scale;
      double r = r0 + (2.7D + 0.12D * Math.sin(t * 0.3D + (double)k) - r0) * LashMath.clamp01(reach);
      double y = p[1] + (WARD_TIP_Y[k % WARD_TIP_Y.length] + 0.15D * Math.sin(t * 0.22D + (double)k * 1.7D)) * scale;
      return new double[]{p[0] + Math.cos(a) * r, y, p[2] + Math.sin(a) * r};
   }

   public static double[] wardRootAt(double[] p, double scale, double t, int k) {
      double a = wardAngle(t, k) + 0.9D;
      double r = 0.28D * scale;
      return new double[]{p[0] + Math.cos(a) * r, p[1] + WARD_ROOT_Y[k % WARD_ROOT_Y.length] * scale, p[2] + Math.sin(a) * r};
   }

   public static double[][] wardPts(double[] p, double scale, double t, int k, double reach) {
      double a1 = wardAngle(t, k);
      double a0 = a1 + 0.9D;
      double[] root = wardRootAt(p, scale, t, k);
      double[] tip = wardTip(p, scale, t, k, reach);
      double r0 = 0.28D * scale;
      double r1 = Math.hypot(tip[0] - p[0], tip[2] - p[2]);
      double[][] pts = new double[23][];

      for(int i = 0; i <= 22; ++i) {
         double s = (double)i / 22.0D;
         double ang = a0 + (a1 - a0) * s * s;
         double r = r0 + (r1 - r0) * s;
         double env = 4.0D * s * (1.0D - s);
         double y = root[1] + (tip[1] - root[1]) * s + (0.25D * scale + 0.08D * Math.sin(s * 9.0D - t * 0.9D - (double)k)) * env;
         pts[i] = new double[]{p[0] + Math.cos(ang) * r, y, p[2] + Math.sin(ang) * r};
      }

      return pts;
   }

   public static double[][] wardTrail(double[] p, double scale, double t, int k, double reach) {
      double[][] pts = new double[11][];

      for(int i = 0; i <= 10; ++i) {
         pts[i] = wardTip(p, scale, t - (double)i / 10.0D * 1.1D / 0.32D, k, reach);
      }

      return pts;
   }

   public static double[] wardHandTip(double[] p, double yawDeg, double scale, double t, int side) {
      double[] r = LashMath.right(yawDeg);
      double base = Math.atan2(r[2] * (double)side, r[0] * (double)side);
      double a = base + 0.9D * Math.sin(t * 0.3D + (double)side);
      return new double[]{p[0] + Math.cos(a) * 2.4D, p[1] + (1.35D + 0.35D * Math.sin(t * 0.21D + (double)side * 2.0D)) * scale, p[2] + Math.sin(a) * 2.4D};
   }

   public static double[][] wardHandPts(double[] hand, double[] p, double yawDeg, double scale, double t, int side, double reach) {
      double[] tip = LashMath.lerp(hand, wardHandTip(p, yawDeg, scale, t, side), LashMath.clamp01(reach));
      double curl = -0.25D * Math.cos(t * 0.3D + (double)side);
      return shape(hand, tip, 18, 1.0D, 0.1D, 0.0D, t * 1.1D + (double)side, curl, 0.12D, 0.0D);
   }

   public static double[] wardStrikeRoot(double[] p, double scale, double[] target) {
      double dx = target[0] - p[0];
      double dz = target[2] - p[2];
      double l = Math.hypot(dx, dz);
      double ux = l < 1.0E-6D ? 1.0D : dx / l;
      double uz = l < 1.0E-6D ? 0.0D : dz / l;
      return new double[]{p[0] + ux * 0.28D * scale, p[1] + 1.15D * scale, p[2] + uz * 0.28D * scale};
   }

   public static double[] stormRoot(double[] p, double yawDeg, double scale, int i) {
      double[] f = LashMath.fwd(yawDeg);
      double[] r = LashMath.right(yawDeg);
      double[] back = LashMath.add(LashMath.add(p, new double[]{0.0D, 1.35D * scale, 0.0D}), LashMath.mul(f, -0.25D));
      int side = i % 2 != 0 ? 1 : -1;
      int lvl = i / 2;
      return LashMath.add(back, LashMath.add(LashMath.mul(r, (double)side * (0.14D + 0.04D * (double)lvl)), new double[]{0.0D, 0.2D - (double)lvl * 0.16D, 0.0D}));
   }

   public static double[] stormRest(double[] p, double yawDeg, double scale, int i, double t) {
      double[] f = LashMath.fwd(yawDeg);
      double[] r = LashMath.right(yawDeg);
      int side = i % 2 != 0 ? 1 : -1;
      int lvl = i / 2;
      double[] root = stormRoot(p, yawDeg, scale, i);
      double sway = Math.sin(t * 0.15D + (double)i) * 0.3D;
      return LashMath.add(root, LashMath.add(LashMath.mul(r, (double)side * (1.5D + (double)lvl * 0.35D)), LashMath.add(new double[]{0.0D, 1.3D - (double)lvl * 0.45D + sway, 0.0D}, LashMath.mul(f, -0.7D))));
   }

   public static double[] stormFinale(double[] p, int i) {
      double a = (double)i / 8.0D * Math.PI * 2.0D + 0.3D;
      return new double[]{p[0] + Math.cos(a) * 4.5D, p[1] + 0.1D, p[2] + Math.sin(a) * 4.5D};
   }

   public static double hash3(int x, int y, int z) {
      double s = (double)x * 3.74761393E8D + (double)y * 6.68265263E8D + (double)z * (double)Integer.MAX_VALUE;
      int h = (int)((long)s);
      h = (h ^ h >>> 13) * 1274126177;
      return (double)((long)(h ^ h >>> 16) & 4294967295L) / (double)4.2949673E9F;
   }

   public static double[] frenzyRoot(double[] p, double yawDeg, double scale, int i) {
      double[] f = LashMath.fwd(yawDeg);
      double[] r = LashMath.right(yawDeg);
      int side = i % 2 == 0 ? 1 : -1;
      int lvl = Math.max(1, i / 2);
      double[] back = LashMath.add(LashMath.add(p, new double[]{0.0D, 1.38D * scale, 0.0D}), LashMath.mul(f, -0.24D * scale));
      return LashMath.add(back, LashMath.add(LashMath.mul(r, (double)side * (0.13D + 0.05D * (double)lvl) * scale), new double[]{0.0D, (0.3D - (double)lvl * 0.16D) * scale, 0.0D}));
   }

   public static double[] frenzyRest(double[] p, double yawDeg, double scale, int i, double t) {
      double[] f = LashMath.fwd(yawDeg);
      double[] r = LashMath.right(yawDeg);
      int side = i % 2 == 0 ? 1 : -1;
      int lvl = i / 2;
      double sway = Math.sin(t * 0.11D + (double)i * 1.7D) * 0.5D;
      double bob = Math.cos(t * 0.087D + (double)i * 2.3D) * 0.4D;
      double[] base = LashMath.add(p, new double[]{0.0D, 1.35D * scale, 0.0D});
      return lvl == 0 ? LashMath.add(base, LashMath.add(LashMath.mul(r, (double)side * (1.7D + 0.3D * sway)), LashMath.add(LashMath.mul(f, 1.4D + 0.3D * bob), new double[]{0.0D, 0.3D + 0.4D * bob, 0.0D}))) : LashMath.add(base, LashMath.add(LashMath.mul(r, (double)side * (1.4D + (double)lvl * 0.6D + 0.3D * bob)), LashMath.add(LashMath.mul(f, -0.4D - (double)lvl * 0.3D + 0.4D * sway), new double[]{0.0D, 2.9D - (double)lvl * 0.5D + sway, 0.0D})));
   }

   public static double[][] frenzyIdle(double[] root, double[] rest, double t, int i, double grow) {
      int side = i % 2 == 0 ? 1 : -1;
      return shape(root, rest, 28, grow, 0.32D, 0.0D, t * 0.9D + (double)i, (double)side * 0.18D * Math.sin(t * 0.05D + (double)i), 0.22D, 0.0D);
   }

   public static double[][] frenzyStroke(double[] rr, double[] tt, double lt, double[] cock, double t, double[][] idle) {
      double[] c = cock == null ? new double[]{0.0D, 1.0D, 0.0D} : cock;
      if (lt < 3.0D) {
         return blend(idle, whipPts(rr, tt, 0.0D, c, 28), LashMath.smooth01(lt / 3.0D));
      } else if (lt < 7.0D) {
         return whipPts(rr, tt, Math.pow((lt - 3.0D) / 4.0D, 1.7D), c, 28);
      } else {
         double pin = lt - 7.0D;
         double[][] held = shape(rr, tt, 28, 1.0D, 0.1D + 0.12D * Math.max(0.0D, 1.0D - pin / 3.0D), 0.0D, t * 1.7D, 0.08D, 0.12D, 0.04D);
         return pin < 8.0D ? held : blend(held, idle, LashMath.smooth01((pin - 8.0D) / 5.0D));
      }
   }

   public static double[][] blend(double[][] from, double[][] to, double k) {
      if (from != null && from.length == to.length) {
         double[][] out = new double[to.length][];

         for(int j = 0; j < to.length; ++j) {
            out[j] = LashMath.lerp(from[j], to[j], k);
         }

         return out;
      } else {
         return to;
      }
   }

   public static double[] frenzyLegRoot(double[] p, double yawDeg, double scale, int k) {
      double[] f = LashMath.fwd(yawDeg);
      double[] r = LashMath.right(yawDeg);
      int side = k == 0 ? 1 : -1;
      return LashMath.add(p, LashMath.add(LashMath.mul(r, (double)side * 0.14D * scale), LashMath.add(LashMath.mul(f, -0.2D * scale), new double[]{0.0D, 0.95D * scale, 0.0D})));
   }

   public static double[] frenzyLegSpot(double[] from, double yawDeg, int k) {
      double[] f = LashMath.fwd(yawDeg);
      double[] r = LashMath.right(yawDeg);
      int side = k == 0 ? 1 : -1;
      return LashMath.add(from, LashMath.add(LashMath.mul(r, (double)side * 1.7D), LashMath.mul(f, -1.2D)));
   }

   public static double[][] frenzyLeg(double[] root, double[] foot, double t, int k, double grow) {
      return shape(root, foot, 18, grow, 0.05D, 0.0D, t * 0.6D + (double)k, (double)(k == 0 ? 1 : -1) * 0.12D, 0.3D, 0.0D);
   }

   public static double frenzyGrow(double age, double life) {
      return Math.min(LashMath.outCubic(age / 5.0D), LashMath.clamp01((life - age) / 8.0D));
   }

   public static double frenzyPulse(double age) {
      if (!(age < 0.0D) && !(age >= 120.0D)) {
         double k = age % 40.0D / 10.0D;
         return k >= 1.0D ? 0.0D : (1.0D - k) * (1.0D - k);
      } else {
         return 0.0D;
      }
   }

   public static double poundRing(double reach, double age) {
      double r = Math.max(0.6D, reach);
      return 0.6D + (r - 0.6D) * LashMath.outCubic(LashMath.clamp01(age / 11.0D));
   }

   public static double[] poundCrack(double[] at, double reach, int i) {
      int x = (int)Math.floor(at[0]);
      int z = (int)Math.floor(at[2]);
      double a = (double)i * (Math.PI / 5D) + (hash3(x, z, i) - 0.5D) * 0.5D;
      double d = Math.max(0.6D, reach) * (i % 2 == 0 ? 0.32D : 0.55D) * (0.85D + 0.3D * hash3(z, x, i + 7));
      return new double[]{at[0] + Math.cos(a) * d, at[2] + Math.sin(a) * d, d, 0.8D + 0.5D * hash3(x, i, z), a};
   }

   public static double[] holdRoot(double[] p, double yawDeg, double scale, int slot) {
      double[] f = LashMath.fwd(yawDeg);
      double[] r = LashMath.right(yawDeg);
      int side = slot % 2 == 0 ? 1 : -1;
      int row = slot / 2;
      return LashMath.add(p, LashMath.add(LashMath.mul(r, (double)side * 0.2D * scale), LashMath.add(LashMath.mul(f, -0.22D * scale), new double[]{0.0D, (1.45D - 0.12D * (double)row) * scale, 0.0D})));
   }

   public static double[][] catchPts(double[] hand, double[] ep, double t, double launch, boolean grip) {
      double[] waist = new double[]{ep[0], ep[1] + 0.9D, ep[2]};
      double[][] pts = shape(hand, waist, 22, LashMath.outCubic(launch / 2.5D), grip ? 0.05D : 0.2D, 0.0D, t * 0.9D, 0.0D, 0.1D, 0.0D);
      if (grip) {
         pts = concat(pts, helix(ep, ep[1] + 1.05D, ep[1] + 0.75D, 1.3D, 0.42D, 0.0D, 1.0D, 12, 0.0D));
      }

      return pts;
   }

   public static double[][] holdTogether(double[] p, double yawDeg, double t, int i) {
      double[] f0 = LashMath.fwd(yawDeg);
      double[] r0 = LashMath.right(yawDeg);
      double u = ((double)i * 0.37D + 0.11D) % 1.0D;
      double[] along = LashMath.add(p, LashMath.add(LashMath.mul(r0, -0.95D + 1.9D * u), new double[]{0.0D, 0.2D, 0.0D}));
      double[] a = LashMath.add(along, LashMath.mul(f0, 0.38D + 0.1D * Math.sin(t * 0.2D + (double)i)));
      double[] c = LashMath.add(along, LashMath.mul(f0, -0.38D - 0.1D * Math.cos(t * 0.25D + (double)i)));
      return shape(a, c, 10, 1.0D, 0.12D, 0.0D, t * 0.5D + (double)i, 0.0D, 0.35D, 0.0D);
   }

   public static double holdTogetherAlpha(double t, int i) {
      return 0.5D + 0.4D * Math.sin(t * 0.3D + (double)i);
   }

   public static double[][] riseIn(double[] p, double t, double age, int i) {
      double g = age / 5.0D;
      double a = (double)i * 0.628D;
      double[] from = new double[]{p[0] + Math.cos(a) * 2.5D * (1.0D - g), p[1] + 0.1D, p[2] + Math.sin(a) * 2.5D * (1.0D - g)};
      return shape(from, new double[]{p[0], p[1] + 1.0D, p[2]}, 10, 1.0D, 0.2D, 0.0D, t + (double)i, 0.0D, 0.0D, 0.0D);
   }

   public static double riseK(double age) {
      return LashMath.clamp01((age - 5.0D) / 20.0D);
   }

   public static double riseReach(double age) {
      return 5.5D * LashMath.outCubic(riseK(age) * 1.5D);
   }

   public static double[][] riseOut(double[] p, double t, double age, int i) {
      double k = riseK(age);
      double b = (double)i * 0.5236D + 0.2D;
      double reach = riseReach(age);
      double[] from = new double[]{p[0], p[1] + 1.1D, p[2]};
      double[] to = new double[]{p[0] + Math.cos(b) * reach, p[1] + 0.4D + 0.8D * Math.sin((double)i * 1.3D), p[2] + Math.sin(b) * reach};
      return shape(from, to, 22, 1.0D, 0.3D * (1.0D - k), 0.0D, t * 1.2D + (double)i, 0.15D, 0.0D, 0.0D);
   }

   public static double[] windRoot(double[] p, int i) {
      return LashMath.add(p, new double[]{Math.cos((double)i * 1.257D) * 0.2D, 1.1D + 0.15D * (double)(i % 2), Math.sin((double)i * 1.257D) * 0.2D});
   }

   public static double[] windTip(double[] p, double t, int i) {
      double ang = (double)i * 1.257D + t * 0.03D;
      return LashMath.add(p, new double[]{Math.cos(ang) * 2.2D, 1.6D + 0.6D * Math.sin(t * 0.07D + (double)i), Math.sin(ang) * 2.2D});
   }

   public static double[][] windPts(double[] p, double t, int i) {
      return shape(windRoot(p, i), windTip(p, t, i), 18, 1.0D, 0.28D, 0.0D, t * 0.7D + (double)i, 0.25D, 0.2D, 0.0D);
   }

   public static double[] spentWisp(double[] p, double age, int i) {
      double k = LashMath.clamp01((age - (double)(i * 2)) / 30.0D);
      return !(k <= 0.0D) && !(k >= 1.0D) ? new double[]{p[0] + Math.cos((double)i * 2.3D) * 0.4D, p[1] + 1.0D + k * 1.6D, p[2] + Math.sin((double)i * 2.3D) * 0.4D, 0.2D + 0.4D * k, 0.6D * (1.0D - k)} : null;
   }

   public static double[] fallPuff(double[] p, double age, int i) {
      double k = LashMath.clamp01(age / 24.0D);
      return new double[]{p[0] + Math.cos((double)i) * 0.6D * k, p[1] + 0.2D + 0.4D * k, p[2] + Math.sin((double)i) * 0.6D * k, 0.3D + 0.3D * k, 0.5D * (1.0D - k)};
   }

   public static double[] passiveTip(double[] hand, double t, int i, double k) {
      double a = t * 0.2D + (double)(i * 3);
      return LashMath.add(hand, new double[]{Math.cos(a) * 0.4D * k, -0.2D + Math.sin(a * 1.3D) * 0.25D, Math.sin(a) * 0.4D * k});
   }
}
