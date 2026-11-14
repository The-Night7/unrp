package com.myriam.unrp.pass;

public final class PassMath {
   private PassMath() {
   }

   public static double[] v(double x, double y, double z) {
      return new double[]{x, y, z};
   }

   public static double[] add(double[] a, double[] b) {
      return v(a[0] + b[0], a[1] + b[1], a[2] + b[2]);
   }

   public static double[] sub(double[] a, double[] b) {
      return v(a[0] - b[0], a[1] - b[1], a[2] - b[2]);
   }

   public static double[] mul(double[] a, double k) {
      return v(a[0] * k, a[1] * k, a[2] * k);
   }

   public static double dot(double[] a, double[] b) {
      return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
   }

   public static double[] cross(double[] a, double[] b) {
      return v(a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]);
   }

   public static double len(double[] a) {
      return Math.sqrt(dot(a, a));
   }

   public static double[] norm(double[] a) {
      double l = len(a);
      return l < 1.0E-9D ? v(0.0D, 0.0D, 0.0D) : v(a[0] / l, a[1] / l, a[2] / l);
   }

   public static double[] lerp(double[] a, double[] b, double t) {
      return v(a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t);
   }

   public static double dist(double[] a, double[] b) {
      return len(sub(a, b));
   }

   public static double clamp(double x, double a, double b) {
      return x < a ? a : (x > b ? b : x);
   }

   public static double clamp01(double x) {
      return x < 0.0D ? 0.0D : (x > 1.0D ? 1.0D : x);
   }

   public static double lerp(double a, double b, double t) {
      return a + (b - a) * t;
   }

   public static double smooth(double e0, double e1, double x) {
      double t = clamp01((x - e0) / (e1 - e0));
      return t * t * (3.0D - 2.0D * t);
   }

   public static double outCubic(double x) {
      double k = 1.0D - clamp01(x);
      return 1.0D - k * k * k;
   }

   public static double[] lookDir(double yaw, double pitch) {
      double y = yaw * Math.PI / 180.0D;
      double p = pitch * Math.PI / 180.0D;
      return v(-Math.sin(y) * Math.cos(p), -Math.sin(p), Math.cos(y) * Math.cos(p));
   }

   public static int int32(double d) {
      return (int)((long)d);
   }

   public static double hash3(int x, int y, int z) {
      int h = int32((double)x * 3.74761393E8D + (double)y * 6.68265263E8D + (double)z * (double)Integer.MAX_VALUE);
      h = (h ^ h >>> 13) * 1274126177;
      return (double)((long)(h ^ h >>> 16) & 4294967295L) / (double)4.2949673E9F;
   }

   public static double h(double i, double j, double k) {
      return hash3(int32(i), int32(j), int32(k));
   }

   public static double[] copy(double[] a) {
      return v(a[0], a[1], a[2]);
   }
}
