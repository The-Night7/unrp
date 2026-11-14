package com.myriam.unrp.pass;

public final class PassQuads {
   public static final int SOFT = 0;
   public static final int ADD = 1;
   public static final int ADDN = 2;
   public static final int OVER = 3;
   public static final int DECAL = 4;
   public static final int DADD = 5;
   public static final int XHAIR = 6;
   public static final int LAYERS = 7;
   public static final int STRIDE = 24;
   public static final double DIM_A = 0.7D;
   public static final double[] FULL = new double[]{0.0D, 0.0D, 1.0D, 1.0D};
   public static final int T_GLOW = 24;
   public static final int T_CORE = 25;
   public static final int T_STAR4 = 26;
   public static final int T_STAR8 = 27;
   public static final int T_RING = 28;
   public static final int T_SHOCK = 29;
   public static final int T_BEAM = 30;
   public static final int T_BEAMCORE = 31;
   public static final int T_BOLT = 32;
   public static final int T_SPARK = 33;
   public static final int T_STREAK = 34;
   public static final int T_SMOKE = 35;
   public static final int T_WISP = 36;
   public static final int T_SHARD = 37;
   public static final int T_HEX = 38;
   public static final int T_HEXFILL = 39;
   public static final int T_WHITE = 40;
   public static final int T_GEAR = 41;
   public static final int T_CLOCK = 42;
   public static final int T_HAND = 43;
   public static final int T_GRAIN = 44;
   public static final int T_CLAW = 45;
   public static final int T_SLASH = 46;
   public static final int T_EYE = 47;
   public static final int T_RETICLE = 48;
   public static final int T_NOISE = 49;
   public static final int T_SPIRAL = 50;
   public static final int T_CROSS = 51;
   public static final int T_FLAME = 52;
   public static final int T_CRACK = 53;
   public static final int T_DOT = 54;
   public static final int T_RAIN = 55;
   public static final int T_FORK = 56;
   public static final int T_LICHTEN = 57;
   public static final int T_LENS = 58;
   public static final int T_PUFF = 59;
   public static final int T_RIPPLE = 60;
   public static final int T_GLYPH = 61;
   public static final int T_CHEV = 62;
   public static final int T_EMBER = 63;
   public static final int T_LATTICE = 64;
   public static final int V_GLOW = 1;
   public static final int V_ROCK = 23;
   public static final int M_FLARE0 = 14;
   public static final int M_FLARE1 = 15;
   public static final int M_FLARE2 = 16;
   public static final int CELLS = 65;
   public static final int D_SCORCH = 0;
   public static final int D_CRACKS = 1;
   public static final int D_CRATER = 2;
   public static final int D_FISSURE = 3;
   public static final int D_PUDDLE = 4;
   public static final int D_LICHTEN = 5;
   public static final int D_HEXFLOOR = 6;
   public static final int D_CLOCK = 7;
   public static final int D_GOUGES = 8;
   public static final int D_BURNRING = 9;
   public static final int D_SOFTRING = 10;
   public static final int DECALS = 11;
   public double[] eye = PassMath.v(0.0D, 0.0D, 0.0D);
   public double[] right = PassMath.v(1.0D, 0.0D, 0.0D);
   public double[] up = PassMath.v(0.0D, 1.0D, 0.0D);
   public double[] dim;
   public boolean air;
   private final double[][] data = new double[7][];
   private final int[] count = new int[7];

   public PassQuads() {
      for(int i = 0; i < 7; ++i) {
         this.data[i] = new double[1536];
      }

   }

   public void begin(double[] eye, double[] right, double[] up) {
      this.eye = eye;
      this.right = right;
      this.up = up;
      this.dim = null;
      this.air = false;

      for(int i = 0; i < 7; ++i) {
         this.count[i] = 0;
      }

   }

   public int count(int layer) {
      return this.count[layer];
   }

   public int[] counts() {
      return (int[])this.count.clone();
   }

   public void fade(int[] from, double k) {
      if (!(k >= 1.0D)) {
         double f = Math.max(0.0D, k);

         for(int layer = 0; layer < 7; ++layer) {
            double[] d = this.data[layer];

            for(int i = from[layer]; i < this.count[layer]; ++i) {
               int o = i * 24;
               d[o + 20] *= f;
               d[o + 21] *= f;
               d[o + 22] *= f;
               d[o + 23] *= f;
            }
         }

      }
   }

   public double[] fwd() {
      return PassMath.cross(this.up, this.right);
   }

   public int total() {
      int n = 0;

      for(int i = 0; i < 7; ++i) {
         n += this.count[i];
      }

      return n;
   }

   public double[] data(int layer) {
      return this.data[layer];
   }

   public static boolean glowy(int cell) {
      return cell == 24 || cell == 25 || cell == 26 || cell == 27 || cell == 58 || cell == 47 || cell == 1 || cell == 14 || cell == 15 || cell == 16;
   }

   public void quad(int layer, double[] p1, double[] p2, double[] p3, double[] p4, int cell, double r, double g, double b, double a, double[] sub) {
      this.quad(layer, p1, p2, p3, p4, cell, r, g, b, new double[]{a, a, a, a}, sub);
   }

   public void quad(int layer, double[] p1, double[] p2, double[] p3, double[] p4, int cell, double r, double g, double b, double[] a, double[] sub) {
      double[] al = a;
      if (this.dim != null) {
         if (layer != 1 && layer != 2 && layer != 5) {
            if (layer == 4) {
               r *= 0.3D;
               g *= 0.28D;
               b *= 0.28D;
            }
         } else {
            if (layer != 5 && glowy(cell)) {
               return;
            }

            layer = layer == 5 ? 4 : 3;
            r = this.dim[0];
            g = this.dim[1];
            b = this.dim[2];
            al = new double[]{a[0] * 0.7D, a[1] * 0.7D, a[2] * 0.7D, a[3] * 0.7D};
         }
      }

      if (!(al[0] <= 0.003D) || !(al[1] <= 0.003D) || !(al[2] <= 0.003D) || !(al[3] <= 0.003D)) {
         double[] s = sub == null ? FULL : sub;
         int n = this.count[layer];
         double[] d = this.data[layer];
         if ((n + 1) * 24 > d.length) {
            double[] grown = new double[d.length * 2];
            System.arraycopy(d, 0, grown, 0, n * 24);
            this.data[layer] = grown;
            d = grown;
         }

         int o = n * 24;
         double[][] ps = new double[][]{p1, p2, p3, p4};

         for(int k = 0; k < 4; ++k) {
            d[o + k * 3] = ps[k][0];
            d[o + k * 3 + 1] = ps[k][1];
            d[o + k * 3 + 2] = ps[k][2];
         }

         d[o + 12] = (double)cell;
         d[o + 13] = s[0];
         d[o + 14] = s[1];
         d[o + 15] = s[2];
         d[o + 16] = s[3];
         d[o + 17] = r;
         d[o + 18] = g;
         d[o + 19] = b;
         d[o + 20] = al[0];
         d[o + 21] = al[1];
         d[o + 22] = al[2];
         d[o + 23] = al[3];
         this.count[layer] = n + 1;
      }
   }

   public void plane(int layer, double[] c, double[] ax, double[] ay, double size, double spin, int cell, double r, double g, double b, double a, double sizeY) {
      double cs = Math.cos(spin);
      double sn = Math.sin(spin);
      double[] rr = PassMath.mul(PassMath.add(PassMath.mul(ax, cs), PassMath.mul(ay, sn)), size);
      double[] uu = PassMath.mul(PassMath.sub(PassMath.mul(ay, cs), PassMath.mul(ax, sn)), sizeY);
      this.quad(layer, PassMath.sub(PassMath.sub(c, rr), uu), PassMath.sub(PassMath.add(c, rr), uu), PassMath.add(PassMath.add(c, rr), uu), PassMath.add(PassMath.sub(c, rr), uu), cell, r, g, b, a, (double[])null);
   }

   public void bill(int layer, double[] c, double size, double spin, int cell, double r, double g, double b, double a) {
      this.plane(layer, c, this.right, this.up, size, spin, cell, r, g, b, a, size);
   }

   public void bill(int layer, double[] c, double size, double spin, int cell, double r, double g, double b, double a, double sizeY) {
      this.plane(layer, c, this.right, this.up, size, spin, cell, r, g, b, a, sizeY);
   }

   public void upright(int layer, double[] base, double w, double h, int cell, double r, double g, double b, double a) {
      double[] d = PassMath.sub(this.eye, base);
      double hl = Math.sqrt(d[0] * d[0] + d[2] * d[2]);
      if (hl == 0.0D) {
         hl = 1.0D;
      }

      double[] side = PassMath.v(d[2] / hl * w / 2.0D, 0.0D, -d[0] / hl * w / 2.0D);
      this.quad(layer, PassMath.sub(base, side), PassMath.add(base, side), PassMath.add(PassMath.add(base, side), PassMath.v(0.0D, h, 0.0D)), PassMath.add(PassMath.sub(base, side), PassMath.v(0.0D, h, 0.0D)), cell, r, g, b, a, (double[])null);
   }

   public void flat(int layer, double[] c, double size, double spin, int cell, double r, double g, double b, double a) {
      if (!this.air) {
         this.plane(layer, c, PassMath.v(1.0D, 0.0D, 0.0D), PassMath.v(0.0D, 0.0D, 1.0D), size, spin, cell, r, g, b, a, size);
      }
   }

   public void ribbon(int layer, double[] a, double[] b, double w, int cell, double r, double g, double bl, double alA, double alB, double[] sub) {
      double[] d = PassMath.sub(b, a);
      double[] mid = PassMath.mul(PassMath.add(a, b), 0.5D);
      double[] side = PassMath.cross(d, PassMath.sub(this.eye, mid));
      double l = PassMath.len(side);
      if (!(l < 1.0E-8D)) {
         side = PassMath.mul(side, w * 0.5D / l);
         this.quad(layer, PassMath.sub(a, side), PassMath.sub(b, side), PassMath.add(b, side), PassMath.add(a, side), cell, r, g, bl, new double[]{alA, alB, alB, alA}, sub == null ? FULL : sub);
      }
   }

   public void ribbon(int layer, double[] a, double[] b, double w, int cell, double r, double g, double bl, double alA, double alB) {
      this.ribbon(layer, a, b, w, cell, r, g, bl, alA, alB, (double[])null);
   }

   public void chain(int layer, double[][] pts, double[] widths, int cell, double[][] rgb, double[] alphas, double[] us) {
      int n = pts.length;
      if (n >= 2) {
         double[][] sides = new double[n][];

         for(int i = 0; i < n; ++i) {
            double[] p = pts[i];
            double[] t = PassMath.sub(pts[Math.min(n - 1, i + 1)], pts[Math.max(0, i - 1)]);
            double[] s = PassMath.cross(t, PassMath.sub(this.eye, p));
            double l = PassMath.len(s);
            double w = widths[i];
            sides[i] = l < 1.0E-8D ? PassMath.v(0.0D, 0.0D, 0.0D) : PassMath.mul(s, w * 0.5D / l);
         }

         for(int i = 0; i < n - 1; ++i) {
            double a0 = alphas[i];
            double a1 = alphas[i + 1];
            if (!(a0 <= 0.003D) || !(a1 <= 0.003D)) {
               double[] c0 = rgb.length == 1 ? rgb[0] : rgb[i];
               double[] c1 = rgb.length == 1 ? rgb[0] : rgb[i + 1];
               double[] cc = PassMath.v((c0[0] + c1[0]) / 2.0D, (c0[1] + c1[1]) / 2.0D, (c0[2] + c1[2]) / 2.0D);
               double[] su = us != null ? new double[]{us[i], 0.0D, us[i + 1], 1.0D} : FULL;
               this.quad(layer, PassMath.sub(pts[i], sides[i]), PassMath.sub(pts[i + 1], sides[i + 1]), PassMath.add(pts[i + 1], sides[i + 1]), PassMath.add(pts[i], sides[i]), cell, cc[0], cc[1], cc[2], new double[]{a0, a1, a1, a0}, su);
            }
         }

      }
   }

   public void chain(int layer, double[][] pts, double[] widths, int cell, double[] rgb, double[] alphas) {
      this.chain(layer, pts, widths, cell, new double[][]{rgb}, alphas, (double[])null);
   }

   public void decalAt(int kind, double[] c, double r, double spin, double a, double[] rgb, boolean glow) {
      if (!this.air) {
         double[] col = rgb == null ? PassMath.v(1.0D, 1.0D, 1.0D) : rgb;
         this.plane(glow ? 5 : 4, PassMath.v(c[0], c[1] + (glow ? 0.012D : 0.005D), c[2]), PassMath.v(1.0D, 0.0D, 0.0D), PassMath.v(0.0D, 0.0D, 1.0D), r, spin, kind, col[0], col[1], col[2], a, r);
      }
   }

   public void decalAt(int kind, double[] c, double r, double spin, double a) {
      this.decalAt(kind, c, r, spin, a, (double[])null, false);
   }

   public void decalStrip(int kind, double[] a, double[] b, double w, double al, double[] rgb, double[] sub) {
      if (!this.air) {
         double[] col = rgb == null ? PassMath.v(1.0D, 1.0D, 1.0D) : rgb;
         double[] d = PassMath.sub(b, a);
         d[1] = 0.0D;
         double l = PassMath.len(d);
         if (!(l < 1.0E-6D)) {
            double[] side = PassMath.v(-d[2] / l * w / 2.0D, 0.0D, d[0] / l * w / 2.0D);
            double[] a0 = PassMath.v(a[0], a[1] + 0.006D, a[2]);
            double[] b0 = PassMath.v(b[0], b[1] + 0.006D, b[2]);
            this.quad(4, PassMath.sub(a0, side), PassMath.sub(b0, side), PassMath.add(b0, side), PassMath.add(a0, side), kind, col[0], col[1], col[2], al, sub == null ? FULL : sub);
         }
      }
   }

   public void ring(int layer, double[] c, double r, double w, int cell, double[] rgb, double a, int segs, double[] n, double spin) {
      this.ring(layer, c, r, w, cell, rgb, (PassQuads.AlphaAt)null, a, segs, n, spin);
   }

   public void ring(int layer, double[] c, double r, double w, int cell, double[] rgb, PassQuads.AlphaAt af, double a, int segs, double[] n, double spin) {
      int sg = segs != 0 ? segs : Math.max(16, Math.min(96, (int)Math.ceil(r * 8.0D)));
      double[] ny = n == null ? PassMath.v(0.0D, 1.0D, 0.0D) : n;
      double[][] bs = basis(ny);

      for(int i = 0; i < sg; ++i) {
         double a0 = (double)i / (double)sg * Math.PI * 2.0D + spin;
         double a1 = (double)(i + 1) / (double)sg * Math.PI * 2.0D + spin;
         double[] d0 = PassMath.add(PassMath.mul(bs[0], Math.cos(a0)), PassMath.mul(bs[1], Math.sin(a0)));
         double[] d1 = PassMath.add(PassMath.mul(bs[0], Math.cos(a1)), PassMath.mul(bs[1], Math.sin(a1)));
         double[] p0i = PassMath.add(c, PassMath.mul(d0, Math.max(0.0D, r - w / 2.0D)));
         double[] p0o = PassMath.add(c, PassMath.mul(d0, r + w / 2.0D));
         double[] p1i = PassMath.add(c, PassMath.mul(d1, Math.max(0.0D, r - w / 2.0D)));
         double[] p1o = PassMath.add(c, PassMath.mul(d1, r + w / 2.0D));
         double al = af != null ? af.at((double)i / (double)sg) : a;
         this.quad(layer, p0i, p1i, p1o, p0o, cell, rgb[0], rgb[1], rgb[2], al, FULL);
      }

   }

   public void sphere(int layer, double[] c, double r, int slices, int stacks, int cell, double[] rgb, PassQuads.AlphaOf alphaOf, double[] rep, double lat0, double lat1) {
      double rx = rep != null ? rep[0] : 1.0D;
      double ry = rep != null ? rep[1] : 1.0D;
      double[][][] pp = new double[stacks + 1][slices + 1][];
      double[][][] nn = new double[stacks + 1][slices + 1][];
      double[][] uu = new double[stacks + 1][slices + 1];
      double[][] vv = new double[stacks + 1][slices + 1];
      double[][] aa = new double[stacks + 1][slices + 1];

      for(int j = 0; j <= stacks; ++j) {
         double vj = lat0 + (lat1 - lat0) * (double)j / (double)stacks;
         double th = vj * Math.PI;

         for(int i = 0; i <= slices; ++i) {
            double u = (double)i / (double)slices;
            double ph = u * Math.PI * 2.0D;
            double[] nrm = PassMath.v(Math.sin(th) * Math.cos(ph), Math.cos(th), Math.sin(th) * Math.sin(ph));
            pp[j][i] = PassMath.v(c[0] + nrm[0] * r, c[1] + nrm[1] * r, c[2] + nrm[2] * r);
            nn[j][i] = nrm;
            uu[j][i] = u;
            vv[j][i] = vj;
            aa[j][i] = alphaOf != null ? alphaOf.at(nrm, vj, u) : 1.0D;
         }
      }

      for(int j = 0; j < stacks; ++j) {
         for(int i = 0; i < slices; ++i) {
            double su0 = uu[j + 1][i] * rx % 1.0D;
            double su1 = su0 + rx / (double)slices;
            double sv0 = (1.0D - vv[j + 1][i]) * ry % 1.0D;
            double sv1 = sv0 + ry * (vv[j + 1][i] - vv[j][i]);
            this.quad(layer, pp[j + 1][i], pp[j + 1][i + 1], pp[j][i + 1], pp[j][i], cell, rgb[0], rgb[1], rgb[2], new double[]{aa[j + 1][i], aa[j + 1][i + 1], aa[j][i + 1], aa[j][i]}, new double[]{su0, sv0, su1, sv1});
         }
      }

   }

   public void patch(int layer, double[] c, double r, double[] n, double ang, double spin, int cell, double[] rgb, PassQuads.AlphaUv af, double a, int segs) {
      int nn = segs != 0 ? segs : 4;
      double[][] bs = basis(n);
      double cs = Math.cos(spin);
      double sn = Math.sin(spin);
      double[] ax = PassMath.add(PassMath.mul(bs[0], cs), PassMath.mul(bs[1], sn));
      double[] ay = PassMath.sub(PassMath.mul(bs[1], cs), PassMath.mul(bs[0], sn));
      double[][][] grid = new double[nn + 1][nn + 1][];
      double[][] al = new double[nn + 1][nn + 1];

      for(int j = 0; j <= nn; ++j) {
         for(int i = 0; i <= nn; ++i) {
            double x = ((double)i / (double)nn * 2.0D - 1.0D) * ang;
            double y = ((double)j / (double)nn * 2.0D - 1.0D) * ang;
            double[] d = PassMath.norm(PassMath.add(n, PassMath.add(PassMath.mul(ax, Math.tan(x)), PassMath.mul(ay, Math.tan(y)))));
            grid[j][i] = PassMath.v(c[0] + d[0] * r, c[1] + d[1] * r, c[2] + d[2] * r);
            al[j][i] = af != null ? af.at((double)i / (double)nn, (double)j / (double)nn) : a;
         }
      }

      for(int j = 0; j < nn; ++j) {
         for(int i = 0; i < nn; ++i) {
            this.quad(layer, grid[j][i], grid[j][i + 1], grid[j + 1][i + 1], grid[j + 1][i], cell, rgb[0], rgb[1], rgb[2], new double[]{al[j][i], al[j][i + 1], al[j + 1][i + 1], al[j + 1][i]}, new double[]{(double)i / (double)nn, (double)j / (double)nn, (double)(i + 1) / (double)nn, (double)(j + 1) / (double)nn});
         }
      }

   }

   public void pool(double[] c, double r, double[] rgb, double a) {
      this.flat(1, PassMath.v(c[0], c[1] + 0.02D, c[2]), r, 0.0D, 24, rgb[0], rgb[1], rgb[2], a);
   }

   public static double[][] basis(double[] n) {
      double[] up0 = Math.abs(n[1]) > 0.97D ? PassMath.v(1.0D, 0.0D, 0.0D) : PassMath.v(0.0D, 1.0D, 0.0D);
      double[] right = PassMath.norm(PassMath.cross(n, up0));
      double[] up = PassMath.norm(PassMath.cross(right, n));
      return new double[][]{right, up};
   }

   public interface AlphaAt {
      double at(double var1);
   }

   public interface AlphaOf {
      double at(double[] var1, double var2, double var4);
   }

   public interface AlphaUv {
      double at(double var1, double var3);
   }
}
