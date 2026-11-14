package com.myriam.unrp.uo;



public final class GravityFrame {
   public static final int DOWN = 0;
   public static final int UP = 1;
   public static final int NORTH = 2;
   public static final int SOUTH = 3;
   public static final int WEST = 4;
   public static final int EAST = 5;
   public static final int SIDES = 6;
   private static final double DEG = (Math.PI / 180D);

   private GravityFrame() {
   }

   public static boolean plain(int dir) {
      return dir <= 0 || dir >= 6;
   }

   public static double[] toWorld(int dir, double x, double y, double z) {
      switch (dir) {
         case 1:
            return new double[]{x, -y, -z};
         case 2:
            return new double[]{x, -z, y};
         case 3:
            return new double[]{x, z, -y};
         case 4:
            return new double[]{y, -x, z};
         case 5:
            return new double[]{-y, x, z};
         default:
            return new double[]{x, y, z};
      }
   }

   public static double[] toLocal(int dir, double x, double y, double z) {
      switch (dir) {
         case 1:
            return new double[]{x, -y, -z};
         case 2:
            return new double[]{x, z, -y};
         case 3:
            return new double[]{x, -z, y};
         case 4:
            return new double[]{-y, x, z};
         case 5:
            return new double[]{y, -x, z};
         default:
            return new double[]{x, y, z};
      }
   }

   public static double[] pull(int dir) {
      return toWorld(dir, 0.0D, -1.0D, 0.0D);
   }

   public static double[] up(int dir) {
      return toWorld(dir, 0.0D, 1.0D, 0.0D);
   }

   public static double[] boxMin(int dir, double x, double y, double z, double width, double height) {
      double h = width / 2.0D;
      double[] a = toWorld(dir, -h, 0.0D, -h);
      double[] b = toWorld(dir, h, height, h);
      return new double[]{x + Math.min(a[0], b[0]), y + Math.min(a[1], b[1]), z + Math.min(a[2], b[2])};
   }

   public static double[] boxMax(int dir, double x, double y, double z, double width, double height) {
      double h = width / 2.0D;
      double[] a = toWorld(dir, -h, 0.0D, -h);
      double[] b = toWorld(dir, h, height, h);
      return new double[]{x + Math.max(a[0], b[0]), y + Math.max(a[1], b[1]), z + Math.max(a[2], b[2])};
   }

   public static double[] offsetAlongUp(int dir, double x, double y, double z, double amount) {
      double[] u = up(dir);
      return new double[]{x + u[0] * amount, y + u[1] * amount, z + u[2] * amount};
   }

   public static double[] look(int dir, float yaw, float pitch) {
      double p = (double)(-pitch) * (Math.PI / 180D);
      double q = (double)(-yaw) * (Math.PI / 180D);
      double cp = Math.cos(p);
      double sp = Math.sin(p);
      double cq = Math.cos(q);
      double sq = Math.sin(q);
      return toWorld(dir, sq * cp, sp, cq * cp);
   }

   private static double[][] mul(double[][] a, double[][] b) {
      double[][] r = new double[3][3];

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 3; ++j) {
            r[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
         }
      }

      return r;
   }

   private static double[][] basis(int dir) {
      double[][] m = new double[3][3];

      for(int c = 0; c < 3; ++c) {
         double[] e = toWorld(dir, c == 0 ? 1.0D : 0.0D, c == 1 ? 1.0D : 0.0D, c == 2 ? 1.0D : 0.0D);
         m[0][c] = e[0];
         m[1][c] = e[1];
         m[2][c] = e[2];
      }

      return m;
   }

   private static double[][] rotY(double a) {
      double c = Math.cos(a);
      double s = Math.sin(a);
      return new double[][]{{c, 0.0D, s}, {0.0D, 1.0D, 0.0D}, {-s, 0.0D, c}};
   }

   private static double[][] rotX(double a) {
      double c = Math.cos(a);
      double s = Math.sin(a);
      return new double[][]{{1.0D, 0.0D, 0.0D}, {0.0D, c, -s}, {0.0D, s, c}};
   }

   public static float[] camera(int dir, float yaw, float pitch) {
      if (plain(dir)) {
         return new float[]{yaw, pitch, 0.0F};
      } else {
         double[][] local = mul(rotY(-((double)yaw + 180.0D) * (Math.PI / 180D)), rotX((double)(-pitch) * (Math.PI / 180D)));
         double[][] w = mul(basis(dir), local);
         double b = Math.asin(clamp(-w[1][2]));
         double a = Math.atan2(w[0][2], w[2][2]);
         double c = Math.atan2(w[1][0], w[1][1]);
         return new float[]{(float)(-a / (Math.PI / 180D) - 180.0D), (float)(-b / (Math.PI / 180D)), (float)(-c / (Math.PI / 180D))};
      }
   }

   public static double[][] cameraBasis(int dir, float yaw, float pitch) {
      double[][] local = mul(rotY(-((double)yaw + 180.0D) * (Math.PI / 180D)), rotX((double)(-pitch) * (Math.PI / 180D)));
      return plain(dir) ? local : mul(basis(dir), local);
   }

   public static float[] cameraAngles(double[][] w) {
      double b = Math.asin(clamp(-w[1][2]));
      double a = Math.atan2(w[0][2], w[2][2]);
      double c = Math.atan2(w[1][0], w[1][1]);
      return new float[]{(float)(-a / (Math.PI / 180D) - 180.0D), (float)(-b / (Math.PI / 180D)), (float)(-c / (Math.PI / 180D))};
   }

   public static double[][] blend(double[][] from, double[][] to, double t) {
      double[][] r = mul(to, transpose(from));
      double angle = Math.acos(clamp((r[0][0] + r[1][1] + r[2][2] - 1.0D) / 2.0D));
      if (angle < 1.0E-6D) {
         return to;
      } else {
         double[] axis;
         if (Math.PI - angle > 0.001D) {
            double s = 2.0D * Math.sin(angle);
            axis = new double[]{(r[2][1] - r[1][2]) / s, (r[0][2] - r[2][0]) / s, (r[1][0] - r[0][1]) / s};
         } else {
            int k = r[0][0] >= r[1][1] && r[0][0] >= r[2][2] ? 0 : (r[1][1] >= r[2][2] ? 1 : 2);
            axis = new double[]{(r[0][k] + (k == 0 ? 1.0D : 0.0D)) / 2.0D, (r[1][k] + (k == 1 ? 1.0D : 0.0D)) / 2.0D, (r[2][k] + (k == 2 ? 1.0D : 0.0D)) / 2.0D};
            if (axis[0] * from[0][2] + axis[1] * from[1][2] + axis[2] * from[2][2] > 0.0D) {
               axis = new double[]{-axis[0], -axis[1], -axis[2]};
            }
         }

         double n = Math.sqrt(axis[0] * axis[0] + axis[1] * axis[1] + axis[2] * axis[2]);
         return n < 1.0E-9D ? to : mul(rotAxis(axis[0] / n, axis[1] / n, axis[2] / n, angle * t), from);
      }
   }

   private static double[][] transpose(double[][] m) {
      return new double[][]{{m[0][0], m[1][0], m[2][0]}, {m[0][1], m[1][1], m[2][1]}, {m[0][2], m[1][2], m[2][2]}};
   }

   private static double[][] rotAxis(double x, double y, double z, double a) {
      double c = Math.cos(a);
      double s = Math.sin(a);
      double k = 1.0D - c;
      return new double[][]{{c + x * x * k, x * y * k - z * s, x * z * k + y * s}, {y * x * k + z * s, c + y * y * k, y * z * k - x * s}, {z * x * k - y * s, z * y * k + x * s, c + z * z * k}};
   }

   public static float[] angles(double lx, double ly, double lz, float fallbackYaw) {
      double len = Math.sqrt(lx * lx + ly * ly + lz * lz);
      if (len < 1.0E-9D) {
         return new float[]{fallbackYaw, 0.0F};
      } else {
         double pitch = -Math.asin(clamp(ly / len)) / (Math.PI / 180D);
         double flat = Math.sqrt(lx * lx + lz * lz) / len;
         double yaw = flat < 1.0E-6D ? (double)fallbackYaw : Math.atan2(-lx, lz) / (Math.PI / 180D);
         return new float[]{(float)yaw, (float)pitch};
      }
   }

   public static float[] keepLook(int from, int to, float yaw, float pitch) {
      double[] world = look(from, yaw, pitch);
      double[] local = toLocal(to, world[0], world[1], world[2]);
      return angles(local[0], local[1], local[2], yaw);
   }

   public static int relative(int pick, int down, float yaw) {
      if (pick >= 2 && pick <= 5) {
         int dir = plain(down) ? 0 : down;
         double[] fwd = look(dir, yaw, 0.0F);
         int ahead = nearest(fwd[0], fwd[1], fwd[2]);
         double[] a = pull(ahead);
         double[] u = up(dir);
         double rx = a[1] * u[2] - a[2] * u[1];
         double ry = a[2] * u[0] - a[0] * u[2];
         double rz = a[0] * u[1] - a[1] * u[0];
         if (pick == 2) {
            return ahead;
         } else if (pick == 3) {
            return nearest(-a[0], -a[1], -a[2]);
         } else {
            return pick == 5 ? nearest(rx, ry, rz) : nearest(-rx, -ry, -rz);
         }
      } else {
         return pick;
      }
   }

   public static int nearest(double x, double y, double z) {
      int best = 0;
      double top = -2.0D;

      for(int d = 0; d < 6; ++d) {
         double[] p = pull(d);
         double dot = p[0] * x + p[1] * y + p[2] * z;
         if (dot > top + 1.0E-9D) {
            top = dot;
            best = d;
         }
      }

      return best;
   }

   public static double[] shiftFeet(int from, int to, double x, double y, double z, double height) {
      double[] a = up(from);
      double[] b = up(to);
      double half = height / 2.0D;
      return new double[]{x + (a[0] - b[0]) * half, y + (a[1] - b[1]) * half, z + (a[2] - b[2]) * half};
   }

   private static double clamp(double v) {
      return v < -1.0D ? -1.0D : (v > 1.0D ? 1.0D : v);
   }
}
