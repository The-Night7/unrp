package com.myriam.unrp.anim;

public final class MoveRig {
   public static final double D2R = (Math.PI / 180D);
   public static final int ROOT = 0;
   public static final int PELVIS = 6;
   public static final int TORSO = 12;
   public static final int HEAD = 15;
   public static final int RARM = 18;
   public static final int LARM = 21;
   public static final int RLEG = 24;
   public static final int LLEG = 31;
   public static final int FPR = 38;
   public static final int FPL = 44;
   public static final int CAM = 50;
   public static final int TREM = 54;
   public static final int NCH = 56;
   static final int[] CH_OFF = new int[]{0, 6, 12, 15, 18, 21, 24, 31, 38, 44, 50, 54};
   static final int[] CH_LEN = new int[]{6, 6, 3, 3, 3, 3, 7, 7, 6, 6, 4, 2};
   public static final int LINEAR = 0;
   public static final int SMOOTH = 1;
   public static final int IN = 2;
   public static final int OUT = 3;
   public static final int BACK = 4;
   public static final int STEP = 5;
   public static final int SNAP = 6;
   public static final int SPRING = 7;
   public static final int CR = 8;
   public static final int SINE = 9;
   public static final int INQUAD = 10;
   public static final int OUTQUAD = 11;
   public static final int HEAVY = 12;
   public static final int F_HEAD = 1;
   public static final int F_TORSO = 2;
   public static final int F_RARM = 4;
   public static final int F_LARM = 8;
   public static final int F_RLEG = 16;
   public static final int F_LLEG = 32;
   public static final int CROUCH = 1;
   public static final int RIDING = 2;
   private static final double[] GROUND = new double[]{0.0D, 24.0D, 0.0D};
   private static final double[][] SHOULDER = new double[][]{{-5.0D, 2.0D, 0.0D}, {5.0D, 2.0D, 0.0D}};
   private static final double[][] HIP = new double[][]{{-1.9D, 0.0D, 0.1D}, {1.9D, 0.0D, 0.1D}};

   private MoveRig() {
   }

   static double clamp(double x, double a, double b) {
      return x < a ? a : (x > b ? b : x);
   }

   public static double smooth01(double x) {
      double c = clamp(x, 0.0D, 1.0D);
      return c * c * (3.0D - 2.0D * c);
   }

   static double ease(int code, double u) {
      switch (code) {
         case 0:
            return u;
         case 1:
            return u < 0.5D ? 4.0D * u * u * u : 1.0D - Math.pow(-2.0D * u + 2.0D, 3.0D) / 2.0D;
         case 2:
            return u * u * u;
         case 3:
            double v1 = 1.0D - u;
            return 1.0D - v1 * v1 * v1;
         case 4:
            double v2 = u - 1.0D;
            return 1.0D + 2.70158D * v2 * v2 * v2 + 1.70158D * v2 * v2;
         case 5:
            return u < 1.0D ? 0.0D : 1.0D;
         case 6:
            double v3 = 1.0D - u;
            return 1.0D - v3 * v3 * v3 * v3 * v3;
         case 7:
            return 1.0D - Math.exp(-6.0D * u) * Math.cos(u * 4.5D * Math.PI);
         case 8:
         default:
            return u;
         case 9:
            return 0.5D - 0.5D * Math.cos(u * Math.PI);
         case 10:
            return u * u;
         case 11:
            double v4 = 1.0D - u;
            return 1.0D - v4 * v4;
         case 12:
            return u * u * u * u;
      }
   }

   static void sampleRaw(float[] keys, double t, double[] out) {
      int p = 0;

      for(int c = 0; c < 12; ++c) {
         int n = (int)keys[p++];
         int len = CH_LEN[c];
         int off = CH_OFF[c];
         int stride = 2 + len;
         int base = p;
         int last = p + (n - 1) * stride;
         if (n != 1 && !(t <= (double)keys[p])) {
            if (t >= (double)keys[last]) {
               for(int i = 0; i < len; ++i) {
                  out[off + i] = (double)keys[last + 2 + i];
               }
            } else {
               int k;
               for(k = 0; k < n - 2 && (double)keys[base + (k + 1) * stride] <= t; ++k) {
               }

               int a = base + k * stride;
               int b = a + stride;
               double t0 = (double)keys[a];
               double t1 = (double)keys[b];
               int code = (int)keys[b + 1];
               double span = t1 - t0;
               double u = span > 0.0D ? (t - t0) / span : 1.0D;
               if (code == 8) {
                  double h00 = 2.0D * u * u * u - 3.0D * u * u + 1.0D;
                  double h10 = u * u * u - 2.0D * u * u + u;
                  double h01 = -2.0D * u * u * u + 3.0D * u * u;
                  double h11 = u * u * u - u * u;

                  for(int i = 0; i < len; ++i) {
                     double v0 = (double)keys[a + 2 + i];
                     double v1 = (double)keys[b + 2 + i];
                     double m0 = 0.0D;
                     double m1 = 0.0D;
                     if (k > 0) {
                        int pa = a - stride;
                        m0 = (v1 - (double)keys[pa + 2 + i]) / (t1 - (double)keys[pa]);
                     }

                     if (k + 2 < n) {
                        int nb = b + stride;
                        m1 = ((double)keys[nb + 2 + i] - v0) / ((double)keys[nb] - t0);
                     }

                     out[off + i] = h00 * v0 + h10 * span * m0 + h01 * v1 + h11 * span * m1;
                  }
               } else {
                  double w = ease(code, u);

                  for(int i = 0; i < len; ++i) {
                     double x0 = (double)keys[a + 2 + i];
                     out[off + i] = x0 + ((double)keys[b + 2 + i] - x0) * w;
                  }
               }
            }
         } else {
            for(int i = 0; i < len; ++i) {
               out[off + i] = (double)keys[base + 2 + i];
            }
         }

         p = base + n * stride;
      }

   }

   public static double clipTime(MoveClips.Clip clip, double t, boolean held) {
      if (held && clip.loopEnd > clip.loopStart && t >= (double)clip.loopEnd) {
         double span = (double)(clip.loopEnd - clip.loopStart);
         return (double)clip.loopStart + (t - (double)clip.loopStart) % span;
      } else {
         return t;
      }
   }

   public static double weight(MoveClips.Clip clip, double t, boolean held) {
      double w = 1.0D;
      if (clip.fadeIn > 0.0F && t < (double)clip.fadeIn) {
         w = smooth01(t / (double)clip.fadeIn);
      }

      if (!held && clip.fadeOut > 0.0F && t > (double)(clip.dur - clip.fadeOut)) {
         w = Math.min(w, smooth01(((double)clip.dur - t) / (double)clip.fadeOut));
      }

      if (!held && t >= (double)clip.dur) {
         w = 0.0D;
      }

      if (t < 0.0D) {
         w = 0.0D;
      }

      return clamp(w, 0.0D, 1.0D);
   }

   public static double paced(MoveClips.Clip clip, double limb) {
      return clip.pace > 0.0F ? smooth01(limb / (double)clip.pace) : 1.0D;
   }

   private static void swap(double[] v, int a, int b, int n) {
      for(int i = 0; i < n; ++i) {
         double x = v[a + i];
         v[a + i] = v[b + i];
         v[b + i] = x;
      }

   }

   static void mirror(double[] v) {
      v[0] = -v[0];
      v[4] = -v[4];
      v[5] = -v[5];
      v[6] = -v[6];
      v[10] = -v[10];
      v[11] = -v[11];
      v[13] = -v[13];
      v[14] = -v[14];
      v[16] = -v[16];
      v[17] = -v[17];
      swap(v, 18, 21, 3);
      v[19] = -v[19];
      v[20] = -v[20];
      v[22] = -v[22];
      v[23] = -v[23];
      swap(v, 24, 31, 7);
      v[25] = -v[25];
      v[26] = -v[26];
      v[32] = -v[32];
      v[33] = -v[33];
      swap(v, 38, 44, 6);
      v[38] = -v[38];
      v[42] = -v[42];
      v[43] = -v[43];
      v[44] = -v[44];
      v[48] = -v[48];
      v[49] = -v[49];
      v[51] = -v[51];
      v[52] = -v[52];
   }

   static int mirrorFree(int f) {
      int o = f & 3;
      if ((f & 4) != 0) {
         o |= 8;
      }

      if ((f & 8) != 0) {
         o |= 4;
      }

      if ((f & 16) != 0) {
         o |= 32;
      }

      if ((f & 32) != 0) {
         o |= 16;
      }

      return o;
   }

   public static void sample(MoveClips.Clip clip, double t, boolean held, boolean mir, double[] out) {
      sampleRaw(clip.keys, clipTime(clip, t, held), out);
      if (mir) {
         mirror(out);
      }

   }

   static double[] qzyx(double x, double y, double z) {
      double cx = Math.cos(x * 0.5D);
      double sx = Math.sin(x * 0.5D);
      double cy = Math.cos(y * 0.5D);
      double sy = Math.sin(y * 0.5D);
      double cz = Math.cos(z * 0.5D);
      double sz = Math.sin(z * 0.5D);
      return new double[]{cz * cy * cx + sz * sy * sx, cz * cy * sx - sz * sy * cx, cz * sy * cx + sz * cy * sx, sz * cy * cx - cz * sy * sx};
   }

   static double[] qmul(double[] a, double[] b) {
      return new double[]{a[0] * b[0] - a[1] * b[1] - a[2] * b[2] - a[3] * b[3], a[0] * b[1] + a[1] * b[0] + a[2] * b[3] - a[3] * b[2], a[0] * b[2] - a[1] * b[3] + a[2] * b[0] + a[3] * b[1], a[0] * b[3] + a[1] * b[2] - a[2] * b[1] + a[3] * b[0]};
   }

   static double[] qinv(double[] q) {
      return new double[]{q[0], -q[1], -q[2], -q[3]};
   }

   static double[] qrot(double[] q, double[] v) {
      double w = q[0];
      double x = q[1];
      double y = q[2];
      double z = q[3];
      double tx = 2.0D * (y * v[2] - z * v[1]);
      double ty = 2.0D * (z * v[0] - x * v[2]);
      double tz = 2.0D * (x * v[1] - y * v[0]);
      return new double[]{v[0] + w * tx + (y * tz - z * ty), v[1] + w * ty + (z * tx - x * tz), v[2] + w * tz + (x * ty - y * tx)};
   }

   static double[] qslerp(double[] a, double[] b, double t) {
      if (t <= 0.0D) {
         return new double[]{a[0], a[1], a[2], a[3]};
      } else if (t >= 1.0D) {
         return new double[]{b[0], b[1], b[2], b[3]};
      } else {
         double d = a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3];
         double s = 1.0D;
         if (d < 0.0D) {
            d = -d;
            s = -1.0D;
         }

         if (d > 0.9995D) {
            double ka = 1.0D - t;
            double kb = t * s;
            double[] r = new double[]{a[0] * ka + b[0] * kb, a[1] * ka + b[1] * kb, a[2] * ka + b[2] * kb, a[3] * ka + b[3] * kb};
            double l = Math.sqrt(r[0] * r[0] + r[1] * r[1] + r[2] * r[2] + r[3] * r[3]);
            return new double[]{r[0] / l, r[1] / l, r[2] / l, r[3] / l};
         } else {
            double th = Math.acos(d);
            double sn = Math.sin(th);
            double ka = Math.sin((1.0D - t) * th) / sn;
            double kb = Math.sin(t * th) / sn * s;
            return new double[]{a[0] * ka + b[0] * kb, a[1] * ka + b[1] * kb, a[2] * ka + b[2] * kb, a[3] * ka + b[3] * kb};
         }
      }
   }

   static double[] euler(double[] q) {
      double w = q[0];
      double x = q[1];
      double y = q[2];
      double z = q[3];
      double r20 = 2.0D * (x * z - w * y);
      double ey = Math.asin(clamp(-r20, -1.0D, 1.0D));
      double ex;
      double ez;
      if (Math.abs(r20) < 0.999999D) {
         ex = Math.atan2(2.0D * (y * z + w * x), 1.0D - 2.0D * (x * x + y * y));
         ez = Math.atan2(2.0D * (x * y + w * z), 1.0D - 2.0D * (y * y + z * z));
      } else {
         ex = Math.atan2(-2.0D * (y * z - w * x), 1.0D - 2.0D * (x * x + z * z));
         ez = 0.0D;
      }

      return new double[]{ex, ey, ez};
   }

   private static double[] add(double[] a, double[] b) {
      return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
   }

   private static double[] sub(double[] a, double[] b) {
      return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
   }

   private static void put(float[] out, int off, double[] p, double[] q) {
      double[] e = euler(q);
      out[off] = (float)p[0];
      out[off + 1] = (float)p[1];
      out[off + 2] = (float)p[2];
      out[off + 3] = (float)e[0];
      out[off + 4] = (float)e[1];
      out[off + 5] = (float)e[2];
   }

   private static double[] vq(float[] van, int off) {
      return qzyx((double)van[off + 3], (double)van[off + 4], (double)van[off + 5]);
   }

   public static void solve(MoveClips.Clip clip, double[] v, float[] van, int flags, double tt, boolean mir, float[] out) {
      int free = mir ? mirrorFree(clip.free) : clip.free;
      boolean riding = (flags & 2) != 0;
      if (riding) {
         free |= 48;
      }

      double[] rq = qzyx(v[3] * (Math.PI / 180D), v[4] * (Math.PI / 180D), v[5] * (Math.PI / 180D));
      double[] rt = new double[]{-v[0], -v[1], -v[2]};
      if (riding) {
         rt = new double[]{0.0D, 0.0D, 0.0D};
      }

      if ((flags & 1) != 0) {
         rt[1] -= 2.0D;
      }

      double[] pl = new double[]{-v[6], 12.0D - v[7], -v[8]};
      if (riding) {
         pl = new double[]{0.0D, 12.0D, 0.0D};
      }

      double[] pelvis = add(add(GROUND, qrot(rq, sub(pl, GROUND))), rt);
      double[] pq = qmul(rq, qzyx(v[9] * (Math.PI / 180D), v[10] * (Math.PI / 180D), v[11] * (Math.PI / 180D)));
      double amp = v[54];
      double n1 = 0.0D;
      double n2 = 0.0D;
      double n3 = 0.0D;
      if (amp > 0.0D) {
         double a = tt * v[55];
         n1 = (Math.sin(a * 2.31D) * 0.6D + Math.sin(a * 3.77D + 1.3D) * 0.4D) * amp;
         n2 = (Math.sin(a * 2.83D + 0.7D) * 0.6D + Math.sin(a * 4.21D + 2.1D) * 0.4D) * amp;
         n3 = (Math.sin(a * 3.13D + 1.9D) * 0.6D + Math.sin(a * 1.97D + 0.4D) * 0.4D) * amp;
      }

      double[] bodyV = vq(van, 6);
      double[] tq;
      if ((free & 2) != 0) {
         tq = qmul(pq, bodyV);
      } else {
         tq = qmul(pq, qzyx((v[12] + n1 * 0.5D) * (Math.PI / 180D), (v[13] + n2 * 0.5D) * (Math.PI / 180D), (v[14] + n3 * 0.5D) * (Math.PI / 180D)));
      }

      double[] neck = add(pelvis, qrot(tq, new double[]{0.0D, -12.0D, 0.0D}));
      put(out, 6, neck, tq);
      double[] hq = qslerp(rq, tq, (double)clip.follow);
      if ((free & 1) == 0) {
         hq = qmul(hq, qzyx((v[15] + n2 * 0.4D) * (Math.PI / 180D), (v[16] + n3 * 0.4D) * (Math.PI / 180D), (v[17] + n1 * 0.4D) * (Math.PI / 180D)));
      }

      hq = qmul(hq, vq(van, 0));
      put(out, 0, neck, hq);
      double[] bodyInv = qinv(bodyV);

      for(int s = 0; s < 2; ++s) {
         double[] sh = add(neck, qrot(tq, SHOULDER[s]));
         int ao = s == 0 ? 18 : 21;
         double[] aq;
         if ((free & (s == 0 ? 4 : 8)) != 0) {
            aq = qmul(bodyInv, vq(van, 12 + 6 * s));
         } else {
            double sg = s == 0 ? 1.0D : -1.0D;
            aq = qzyx((v[ao] + n1 * 0.6D) * (Math.PI / 180D), (v[ao + 1] + n2 * 0.6D * sg) * (Math.PI / 180D), (v[ao + 2] + n3 * 0.6D * sg) * (Math.PI / 180D));
         }

         double[] fq = qmul(tq, aq);
         double aim = s == 0 != mir ? (double)clip.aimR : (double)clip.aimL;
         if (aim > 0.0D && (free & (s == 0 ? 4 : 8)) == 0) {
            double[] la = qslerp(new double[]{1.0D, 0.0D, 0.0D, 0.0D}, qzyx((double)van[3], (double)van[4], 0.0D), aim);
            fq = qmul(qmul(qmul(rq, la), qinv(rq)), fq);
         }

         put(out, 12 + 6 * s, sh, fq);
      }

      for(int s = 0; s < 2; ++s) {
         double[] hip = add(pelvis, qrot(pq, HIP[s]));
         double[] lq;
         if ((free & (s == 0 ? 16 : 32)) != 0) {
            lq = qmul(pq, vq(van, 24 + 6 * s));
         } else {
            int o = s == 0 ? 24 : 31;
            double[] local = qzyx(v[o] * (Math.PI / 180D), v[o + 1] * (Math.PI / 180D), v[o + 2] * (Math.PI / 180D));
            double ik = v[o + 6];
            if (ik > 0.0D) {
               double sgn = s == 0 ? -1.0D : 1.0D;
               double[] foot = new double[]{sgn * (1.9D + v[o + 3]), 24.0D - v[o + 5], 0.1D - v[o + 4]};
               double[] hipR = add(qrot(qinv(rq), sub(sub(hip, rt), GROUND)), GROUND);
               double[] d = sub(foot, hipR);
               double[] dir;
               if (d[1] >= 11.999D) {
                  double dl = Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);
                  dir = new double[]{d[0] / dl, d[1] / dl, d[2] / dl};
               } else {
                  double hx = d[0];
                  double hz = d[2];
                  double hl = Math.sqrt(hx * hx + hz * hz);
                  if (hl < 1.0E-4D) {
                     hx = sgn;
                     hz = 0.0D;
                     hl = 1.0D;
                  }

                  double hd = Math.sqrt(Math.max(0.0D, 144.0D - d[1] * d[1])) / 12.0D;
                  dir = new double[]{hx / hl * hd, d[1] / 12.0D, hz / hl * hd};
               }

               double[] lv = qrot(qinv(pq), qrot(rq, dir));
               double lx = Math.asin(clamp(lv[2], -1.0D, 1.0D));
               double lz = Math.atan2(-lv[0], lv[1]);
               double[] ikq = qmul(qzyx(lx, 0.0D, lz), qzyx(0.0D, v[o + 1] * (Math.PI / 180D), 0.0D));
               local = ik >= 1.0D ? ikq : qslerp(local, ikq, ik);
            }

            lq = qmul(pq, local);
         }

         put(out, 24 + 6 * s, hip, lq);
      }

   }

   public static void swing(float[] pose, boolean left, double t, double headX) {
      if (!(t <= 0.0D) && !(t >= 1.0D)) {
         double body = Math.sin(Math.sqrt(t) * Math.PI * 2.0D) * 0.2D;
         if (left) {
            body = -body;
         }

         double f = 1.0D - t;
         f *= f;
         f *= f;
         f = 1.0D - f;
         double rise = Math.sin(f * Math.PI) * 1.2D + Math.sin(t * Math.PI) * -(headX - 0.7D) * 0.75D - (left ? body : 0.0D);
         double roll = Math.sin(t * Math.PI) * -0.4D;
         int o = left ? 18 : 12;
         double[] tq = qzyx((double)pose[9], (double)pose[10], (double)pose[11]);
         double[] e = euler(qmul(qinv(tq), qzyx((double)pose[o + 3], (double)pose[o + 4], (double)pose[o + 5])));
         double[] r = euler(qmul(tq, qzyx(e[0] - rise, e[1] + body * 3.0D, e[2] + roll)));
         pose[o + 3] = (float)r[0];
         pose[o + 4] = (float)r[1];
         pose[o + 5] = (float)r[2];
      }
   }

   public static void blend(float[] a, float[] b, double w, float[] out) {
      for(int p = 0; p < 6; ++p) {
         int o = p * 6;

         for(int i = 0; i < 3; ++i) {
            out[o + i] = (float)((double)a[o + i] + (double)(b[o + i] - a[o + i]) * w);
         }

         double[] q = qslerp(qzyx((double)a[o + 3], (double)a[o + 4], (double)a[o + 5]), qzyx((double)b[o + 3], (double)b[o + 4], (double)b[o + 5]), w);
         double[] e = euler(q);
         out[o + 3] = (float)e[0];
         out[o + 4] = (float)e[1];
         out[o + 5] = (float)e[2];
      }

   }
}
