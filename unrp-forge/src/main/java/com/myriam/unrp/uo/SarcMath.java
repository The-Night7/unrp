package com.myriam.unrp.uo;



public final class SarcMath {
   public static final double PI = Math.PI;

   private SarcMath() {
   }

   public static double clamp(double v, double a, double b) {
      return v < a ? a : (v > b ? b : v);
   }

   public static double env(double c, double a, double b) {
      return clamp((c - a) / (b - a), 0.0D, 1.0D);
   }

   public static double smooth(double x) {
      x = clamp(x, 0.0D, 1.0D);
      return x * x * (3.0D - 2.0D * x);
   }

   public static double smoother(double x) {
      x = clamp(x, 0.0D, 1.0D);
      return x * x * x * (x * (x * 6.0D - 15.0D) + 10.0D);
   }

   public static double outCubic(double x) {
      x = clamp(x, 0.0D, 1.0D);
      double u = 1.0D - x;
      return 1.0D - u * u * u;
   }

   public static double inCubic(double x) {
      x = clamp(x, 0.0D, 1.0D);
      return x * x * x;
   }

   public static double ring(double x, double freq, double decay) {
      return x <= 0.0D ? 0.0D : Math.exp(-decay * x) * Math.sin(freq * x);
   }

   public static double bump(double c, double a, double b) {
      return Math.sin(Math.PI * env(c, a, b));
   }

   public static double lerp(double a, double b, double k) {
      return a + (b - a) * k;
   }

   public static double frac(double x) {
      return x - Math.floor(x);
   }

   public static double[] v(double x, double y, double z) {
      return new double[]{x, y, z};
   }

   public static double[] lerp3(double[] a, double[] b, double k) {
      return new double[]{lerp(a[0], b[0], k), lerp(a[1], b[1], k), lerp(a[2], b[2], k)};
   }

   public static double[] add(double[] a, double[] b) {
      return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
   }

   public static double[] sub(double[] a, double[] b) {
      return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
   }

   public static double[] scale(double[] a, double k) {
      return new double[]{a[0] * k, a[1] * k, a[2] * k};
   }

   public static double dot(double[] a, double[] b) {
      return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
   }

   public static double[] cross(double[] a, double[] b) {
      return new double[]{a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
   }

   public static double len(double[] a) {
      return Math.sqrt(dot(a, a));
   }

   public static double dist(double[] a, double[] b) {
      double x = a[0] - b[0];
      double y = a[1] - b[1];
      double z = a[2] - b[2];
      return Math.sqrt(x * x + y * y + z * z);
   }

   public static double[] norm(double[] a) {
      double l = len(a);
      return l < 1.0E-12D ? new double[]{0.0D, 0.0D, 0.0D} : scale(a, 1.0D / l);
   }

   public static double[] identity() {
      return new double[]{1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D};
   }

   public static double[] compose(double x, double y, double z, double rx, double ry, double rz, double sx, double sy, double sz) {
      double ca = Math.cos(rx);
      double sa = Math.sin(rx);
      double cb = Math.cos(ry);
      double sb = Math.sin(ry);
      double cc = Math.cos(rz);
      double sc = Math.sin(rz);
      double m00 = cb * cc;
      double m01 = sa * sb * cc - ca * sc;
      double m02 = ca * sb * cc + sa * sc;
      double m10 = cb * sc;
      double m11 = sa * sb * sc + ca * cc;
      double m12 = ca * sb * sc - sa * cc;
      double m20 = -sb;
      double m21 = sa * cb;
      double m22 = ca * cb;
      return new double[]{m00 * sx, m01 * sy, m02 * sz, x, m10 * sx, m11 * sy, m12 * sz, y, m20 * sx, m21 * sy, m22 * sz, z, 0.0D, 0.0D, 0.0D, 1.0D};
   }

   public static double[] mul(double[] a, double[] b) {
      double[] o = new double[16];

      for(int r = 0; r < 4; ++r) {
         for(int c = 0; c < 4; ++c) {
            double s = 0.0D;

            for(int k = 0; k < 4; ++k) {
               s += a[r * 4 + k] * b[k * 4 + c];
            }

            o[r * 4 + c] = s;
         }
      }

      return o;
   }

   public static double[] invert(double[] m) {
      double a00 = m[0];
      double a01 = m[1];
      double a02 = m[2];
      double a10 = m[4];
      double a11 = m[5];
      double a12 = m[6];
      double a20 = m[8];
      double a21 = m[9];
      double a22 = m[10];
      double c00 = a11 * a22 - a12 * a21;
      double c01 = a02 * a21 - a01 * a22;
      double c02 = a01 * a12 - a02 * a11;
      double c10 = a12 * a20 - a10 * a22;
      double c11 = a00 * a22 - a02 * a20;
      double c12 = a02 * a10 - a00 * a12;
      double c20 = a10 * a21 - a11 * a20;
      double c21 = a01 * a20 - a00 * a21;
      double c22 = a00 * a11 - a01 * a10;
      double det = a00 * c00 + a01 * c10 + a02 * c20;
      double id = Math.abs(det) < 1.0E-15D ? 0.0D : 1.0D / det;
      double i00 = c00 * id;
      double i01 = c01 * id;
      double i02 = c02 * id;
      double i10 = c10 * id;
      double i11 = c11 * id;
      double i12 = c12 * id;
      double i20 = c20 * id;
      double i21 = c21 * id;
      double i22 = c22 * id;
      double tx = m[3];
      double ty = m[7];
      double tz = m[11];
      return new double[]{i00, i01, i02, -(i00 * tx + i01 * ty + i02 * tz), i10, i11, i12, -(i10 * tx + i11 * ty + i12 * tz), i20, i21, i22, -(i20 * tx + i21 * ty + i22 * tz), 0.0D, 0.0D, 0.0D, 1.0D};
   }

   public static double[] apply(double[] m, double x, double y, double z) {
      return new double[]{m[0] * x + m[1] * y + m[2] * z + m[3], m[4] * x + m[5] * y + m[6] * z + m[7], m[8] * x + m[9] * y + m[10] * z + m[11]};
   }

   public static double[] apply(double[] m, double[] p) {
      return apply(m, p[0], p[1], p[2]);
   }

   public static double[] applyDir(double[] m, double[] d) {
      return new double[]{m[0] * d[0] + m[1] * d[1] + m[2] * d[2], m[4] * d[0] + m[5] * d[1] + m[6] * d[2], m[8] * d[0] + m[9] * d[1] + m[10] * d[2]};
   }

   public static double[] qEuler(double x, double y, double z) {
      double c1 = Math.cos(x / 2.0D);
      double c2 = Math.cos(y / 2.0D);
      double c3 = Math.cos(z / 2.0D);
      double s1 = Math.sin(x / 2.0D);
      double s2 = Math.sin(y / 2.0D);
      double s3 = Math.sin(z / 2.0D);
      return new double[]{s1 * c2 * c3 - c1 * s2 * s3, c1 * s2 * c3 + s1 * c2 * s3, c1 * c2 * s3 - s1 * s2 * c3, c1 * c2 * c3 + s1 * s2 * s3};
   }

   public static double[] qMul(double[] a, double[] b) {
      double ax = a[0];
      double ay = a[1];
      double az = a[2];
      double aw = a[3];
      double bx = b[0];
      double by = b[1];
      double bz = b[2];
      double bw = b[3];
      return new double[]{ax * bw + aw * bx + ay * bz - az * by, ay * bw + aw * by + az * bx - ax * bz, az * bw + aw * bz + ax * by - ay * bx, aw * bw - ax * bx - ay * by - az * bz};
   }

   public static double[] qInv(double[] q) {
      double n = q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3];
      return n < 1.0E-15D ? new double[]{0.0D, 0.0D, 0.0D, 1.0D} : new double[]{-q[0] / n, -q[1] / n, -q[2] / n, q[3] / n};
   }

   public static double[] qNorm(double[] q) {
      double n = Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
      return n < 1.0E-15D ? new double[]{0.0D, 0.0D, 0.0D, 1.0D} : new double[]{q[0] / n, q[1] / n, q[2] / n, q[3] / n};
   }

   public static double[] qSlerp(double[] a, double[] b, double t) {
      if (t <= 0.0D) {
         return (double[])a.clone();
      } else if (t >= 1.0D) {
         return (double[])b.clone();
      } else {
         double x = a[0];
         double y = a[1];
         double z = a[2];
         double w = a[3];
         double bx = b[0];
         double by = b[1];
         double bz = b[2];
         double bw = b[3];
         double cosHalf = w * bw + x * bx + y * by + z * bz;
         if (cosHalf < 0.0D) {
            bx = -bx;
            by = -by;
            bz = -bz;
            bw = -bw;
            cosHalf = -cosHalf;
         }

         if (cosHalf >= 1.0D) {
            return (double[])a.clone();
         } else {
            double sqrSin = 1.0D - cosHalf * cosHalf;
            if (sqrSin <= 1.0E-12D) {
               double s = 1.0D - t;
               return qNorm(new double[]{s * x + t * bx, s * y + t * by, s * z + t * bz, s * w + t * bw});
            } else {
               double sinHalf = Math.sqrt(sqrSin);
               double half = Math.atan2(sinHalf, cosHalf);
               double ra = Math.sin((1.0D - t) * half) / sinHalf;
               double rb = Math.sin(t * half) / sinHalf;
               return new double[]{x * ra + bx * rb, y * ra + by * rb, z * ra + bz * rb, w * ra + bw * rb};
            }
         }
      }
   }

   public static double[] qRotate(double[] q, double[] p) {
      double[] m = qMatrix(q);
      return applyDir(m, p);
   }

   public static double[] qMatrix(double[] q) {
      double x = q[0];
      double y = q[1];
      double z = q[2];
      double w = q[3];
      double x2 = x + x;
      double y2 = y + y;
      double z2 = z + z;
      double xx = x * x2;
      double xy = x * y2;
      double xz = x * z2;
      double yy = y * y2;
      double yz = y * z2;
      double zz = z * z2;
      double wx = w * x2;
      double wy = w * y2;
      double wz = w * z2;
      return new double[]{1.0D - (yy + zz), xy - wz, xz + wy, 0.0D, xy + wz, 1.0D - (xx + zz), yz - wx, 0.0D, xz - wy, yz + wx, 1.0D - (xx + yy), 0.0D, 0.0D, 0.0D, 0.0D, 1.0D};
   }

   public static double[] qFromRotation(double m11, double m12, double m13, double m21, double m22, double m23, double m31, double m32, double m33) {
      double trace = m11 + m22 + m33;
      if (trace > 0.0D) {
         double s_0 = 0.5D / Math.sqrt(trace + 1.0D);
         return new double[]{(m32 - m23) * s_0, (m13 - m31) * s_0, (m21 - m12) * s_0, 0.25D / s_0};
      } else if (m11 > m22 && m11 > m33) {
         double s = 2.0D * Math.sqrt(1.0D + m11 - m22 - m33);
         return new double[]{0.25D * s, (m12 + m21) / s, (m13 + m31) / s, (m32 - m23) / s};
      } else if (m22 > m33) {
         double s = 2.0D * Math.sqrt(1.0D + m22 - m11 - m33);
         return new double[]{(m12 + m21) / s, 0.25D * s, (m23 + m32) / s, (m13 - m31) / s};
      } else {
         double s = 2.0D * Math.sqrt(1.0D + m33 - m11 - m22);
         return new double[]{(m13 + m31) / s, (m23 + m32) / s, 0.25D * s, (m21 - m12) / s};
      }
   }

   public static double[] qBasis(double[] x, double[] y, double[] z) {
      return qFromRotation(x[0], y[0], z[0], x[1], y[1], z[1], x[2], y[2], z[2]);
   }

   public static double[] rotOf(double[] m) {
      double[] c0 = norm(new double[]{m[0], m[4], m[8]});
      double[] c1 = norm(new double[]{m[1], m[5], m[9]});
      double[] c2 = norm(new double[]{m[2], m[6], m[10]});
      return qBasis(c0, c1, c2);
   }

   public static double[] eulerOf(double[] q) {
      double[] m = qMatrix(q);
      double m11 = m[0];
      double m12 = m[1];
      double m21 = m[4];
      double m22 = m[5];
      double m31 = m[8];
      double m32 = m[9];
      double m33 = m[10];
      double y = Math.asin(-clamp(m31, -1.0D, 1.0D));
      double x;
      double z;
      if (Math.abs(m31) < 0.9999999D) {
         x = Math.atan2(m32, m33);
         z = Math.atan2(m21, m11);
      } else {
         x = 0.0D;
         z = Math.atan2(-m12, m22);
      }

      return new double[]{x, y, z};
   }

   public static double[] aimQ(double[] aim, double[] up) {
      double[] z = norm(aim);
      double[] x = norm(sub(up, scale(z, dot(up, z))));
      double[] y = cross(z, x);
      return qBasis(x, y, z);
   }

   public static double[] basisQ(double[] yAxis, double[] zAxis) {
      double[] y = norm(yAxis);
      double[] z = norm(sub(zAxis, scale(y, dot(zAxis, y))));
      double[] x = cross(y, z);
      return qBasis(x, y, z);
   }

   public static double segDist(double[] p1, double[] q1, double[] p2, double[] q2) {
      double[] d1 = sub(q1, p1);
      double[] d2 = sub(q2, p2);
      double[] r = sub(p1, p2);
      double a = dot(d1, d1);
      double e = dot(d2, d2);
      double f = dot(d2, r);
      if (a <= 1.0E-9D && e <= 1.0E-9D) {
         return dist(p1, p2);
      } else {
         double s;
         double t;
         if (a <= 1.0E-9D) {
            s = 0.0D;
            t = clamp(f / e, 0.0D, 1.0D);
         } else {
            double c = dot(d1, r);
            if (e <= 1.0E-9D) {
               t = 0.0D;
               s = clamp(-c / a, 0.0D, 1.0D);
            } else {
               double b = dot(d1, d2);
               double den = a * e - b * b;
               s = den != 0.0D ? clamp((b * f - c * e) / den, 0.0D, 1.0D) : 0.0D;
               t = (b * s + f) / e;
               if (t < 0.0D) {
                  t = 0.0D;
                  s = clamp(-c / a, 0.0D, 1.0D);
               } else if (t > 1.0D) {
                  t = 1.0D;
                  s = clamp((b - c) / a, 0.0D, 1.0D);
               }
            }
         }

         double[] c1 = add(p1, scale(d1, s));
         double[] c2 = add(p2, scale(d2, t));
         return dist(c1, c2);
      }
   }
}
