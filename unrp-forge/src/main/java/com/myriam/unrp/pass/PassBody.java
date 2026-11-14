package com.myriam.unrp.pass;

public final class PassBody {
   public static final int HEAD = 0;
   public static final int BODY = 1;
   public static final int RARM = 2;
   public static final int LARM = 3;
   public static final int RLEG = 4;
   public static final int LLEG = 5;
   public static final int PARTS = 6;
   public final double[][] m;
   public final boolean slim;
   public final double[] feet;
   public final double[] pos;
   public final double height;

   public PassBody(double[][] m, boolean slim, double[] feet, double[] pos, double height) {
      this.m = m;
      this.slim = slim;
      this.feet = feet;
      this.pos = pos;
      this.height = height;
   }

   public static PassBody box(double[] feet, double[] pos, double height) {
      return new PassBody((double[][])null, false, feet, pos, height);
   }

   public boolean posed() {
      return this.m != null;
   }

   public double[] at(int part, double x, double y, double z) {
      if (this.m == null) {
         return this.mid();
      } else {
         double[] a = this.m[part];
         return PassMath.v(a[0] * x + a[1] * y + a[2] * z + a[3], a[4] * x + a[5] * y + a[6] * z + a[7], a[8] * x + a[9] * y + a[10] * z + a[11]);
      }
   }

   public double[] mid() {
      return PassMath.v(this.pos[0], this.pos[1] + this.height * 0.5D, this.pos[2]);
   }

   public double[] chest() {
      return PassMath.v(this.pos[0], this.pos[1] + this.height * 0.72D, this.pos[2]);
   }

   public double[] hand(boolean right) {
      return this.m == null ? this.chest() : this.at(right ? 2 : 3, this.slim ? 0.0D : (right ? -1.0D : 1.0D), 11.0D, 0.0D);
   }

   public double[][] hands() {
      return new double[][]{this.hand(true), this.hand(false)};
   }

   public double[][] points() {
      if (this.m == null) {
         double[] c = this.feet;
         double h = this.height;
         return new double[][]{PassMath.v(c[0] - 0.6D, c[1] + h * 0.8D, c[2]), PassMath.v(c[0] + 0.6D, c[1] + h * 0.8D, c[2]), PassMath.v(c[0], c[1] + h, c[2]), PassMath.v(c[0] - 0.4D, c[1] + h * 0.4D, c[2] + 0.3D), PassMath.v(c[0] + 0.4D, c[1] + h * 0.4D, c[2] - 0.3D), PassMath.v(c[0], c[1] + 0.2D, c[2])};
      } else {
         return new double[][]{this.hand(true), this.hand(false), this.at(0, 0.0D, -5.0D, 0.0D), this.at(1, -4.0D, 1.0D, 0.0D), this.at(1, 4.0D, 1.0D, 0.0D), this.at(1, -3.0D, 11.0D, 0.0D), this.at(1, 3.0D, 11.0D, 0.0D), this.at(4, 0.0D, 11.0D, 0.0D), this.at(5, 0.0D, 11.0D, 0.0D)};
      }
   }
}
