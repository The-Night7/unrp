package com.myriam.unrp.uo;



import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WeaveBrush {
   public static final int FLOOR = 0;
   public static final int WALL_NS = 1;
   public static final int WALL_EW = 2;
   public static final int PER_TICK = 6;
   public static final int MOST_POLLS = 24;
   public static final int STROKE_CELLS = 32;
   public static final int QUEUE_CAP = 96;
   public static final float STEEP = 30.0F;
   public static final float STEEP_HOLD = 4.0F;
   public static final double AXIS_HOLD = 1.3D;
   public static final int SETTLE = 1;
   public static final int RAMP = 2;
   public static final double DEPTH_HOLD = 1.5D;
   public static final int SURFACE = 8;
   public static final int MEMORY = 2048;
   public static final int X = 0;
   public static final int Y = 1;
   public static final int Z = 2;
   public static final int ORIENT = 3;
   public static final int LAYER = 4;
   public static final int PLANE = 5;
   public static final int STATE = 6;
   private static final int QUEUED = 0;
   private static final int TAKEN = 1;
   private static final int[][] PLATE = plateOrder();
   private int orient = -1;
   private int baseOrient = -1;
   private boolean steep;
   private boolean alongX;
   private boolean locked;
   private int plane;
   private int basePlane;
   private boolean started;
   private double lx;
   private double ly;
   private double lz;
   private long lastCenter = Long.MIN_VALUE;
   private final ArrayDeque<int[]> queue = new ArrayDeque();
   private final Map<Long, List<int[]>> cols = new HashMap();

   public int orient() {
      return this.orient;
   }

   public int plane() {
      return this.plane;
   }

   public boolean locked() {
      return this.locked;
   }

   public int pending() {
      return this.queue.size();
   }

   public void clear() {
      this.queue.clear();
      this.cols.clear();
   }

   public int[] poll() {
      int[] e = (int[])this.queue.poll();
      if (e != null) {
         e[6] = 1;
      }

      return e;
   }

   public static int heightOf(double y) {
      return (int)Math.floor(y * 4.0D) + 1;
   }

   public static int cellYOf(int k) {
      return Math.floorDiv(k - 1, 4);
   }

   public static int layerOfHeight(int k) {
      return k - 1 - 4 * Math.floorDiv(k - 1, 4);
   }

   public static int rawOrient(float pitch, double lookX, double lookZ, boolean sneaking) {
      boolean horizontal = pitch >= 30.0F || pitch <= -30.0F;
      if (sneaking) {
         horizontal = !horizontal;
      }

      return horizontal ? 0 : (Math.abs(lookX) > Math.abs(lookZ) ? 2 : 1);
   }

   private static int[][] plateOrder() {
      List<int[]> out = new ArrayList();

      for(int ring = 0; ring <= 2; ++ring) {
         for(int a = -1; a <= 1; ++a) {
            for(int b = -1; b <= 1; ++b) {
               if (Math.abs(a) + Math.abs(b) == ring) {
                  out.add(new int[]{a, b});
               }
            }
         }
      }

      return (int[][])out.toArray(new int[0][]);
   }

   private static long key(int a, int b, int c) {
      return ((long)a & 67108863L) << 38 | ((long)b & 67108863L) << 12 | (long)c & 4095L;
   }

   private static double depthOf(int o, double x, double z) {
      return o == 1 ? z : x;
   }

   private void seed(double x, double y, double z) {
      if (this.orient == 0) {
         this.plane = heightOf(y);
      } else {
         this.plane = (int)Math.floor(depthOf(this.orient, x, z));
      }

      this.lastCenter = Long.MIN_VALUE;
   }

   public void begin(double x, double y, double z, float pitch, double lookX, double lookZ, boolean sneaking, int[] own) {
      this.clear();
      this.steep = pitch >= 30.0F || pitch <= -30.0F;
      this.alongX = Math.abs(lookX) > Math.abs(lookZ);
      if (own != null) {
         this.locked = true;
         this.orient = own[3];
         this.baseOrient = this.orient;
         if (this.orient == 0) {
            this.plane = 4 * own[1] + own[4] + 1;
         } else {
            this.plane = this.orient == 1 ? own[2] : own[0];
         }

         this.basePlane = this.plane;
      } else {
         this.locked = false;
         this.orient = rawOrient(pitch, lookX, lookZ, sneaking);
         this.baseOrient = this.orient;
         this.seed(x, y, z);
      }

      int[] c = this.anchorCell(x, y, z);

      for(int[] ab : PLATE) {
         int[] at = this.inPlane(c, ab[0], ab[1]);
         this.enqueue(at[0], at[1], at[2], c[3], ab[0] == 0 && ab[1] == 0);
      }

      this.lastCenter = this.walkKey(c);
      this.lx = x;
      this.ly = y;
      this.lz = z;
      this.started = true;
   }

   private int[] anchorCell(double x, double y, double z) {
      if (this.orient == 0) {
         return new int[]{(int)Math.floor(x), cellYOf(this.plane), (int)Math.floor(z), layerOfHeight(this.plane)};
      } else {
         return this.orient == 1 ? new int[]{(int)Math.floor(x), (int)Math.floor(y), this.plane, 3} : new int[]{this.plane, (int)Math.floor(y), (int)Math.floor(z), 3};
      }
   }

   private int[] inPlane(int[] c, int a, int b) {
      if (this.orient == 0) {
         return new int[]{c[0] + a, c[1], c[2] + b};
      } else {
         return this.orient == 1 ? new int[]{c[0] + a, c[1] + b, c[2]} : new int[]{c[0], c[1] + b, c[2] + a};
      }
   }

   private long walkKey(int[] c) {
      if (this.orient == 0) {
         return key(c[0], c[2], this.orient);
      } else {
         return this.orient == 1 ? key(c[0], c[1], this.orient) : key(c[2], c[1], this.orient);
      }
   }

   private int aim(float pitch, double lookX, double lookZ, boolean sneaking) {
      if (this.locked) {
         if (!sneaking) {
            return this.baseOrient;
         } else if (this.baseOrient != 0) {
            return 0;
         } else {
            return Math.abs(lookX) > Math.abs(lookZ) ? 2 : 1;
         }
      } else {
         float ap = Math.abs(pitch);
         this.steep = this.steep ? ap >= 26.0F : ap >= 34.0F;
         double ax = Math.abs(lookX);
         double az = Math.abs(lookZ);
         this.alongX = this.alongX ? ax * 1.3D >= az : ax > az * 1.3D;
         boolean horizontal = sneaking ? !this.steep : this.steep;
         return horizontal ? 0 : (this.alongX ? 2 : 1);
      }
   }

   public void step(double x, double y, double z, float pitch, double lookX, double lookZ, boolean sneaking) {
      if (!this.started) {
         this.begin(x, y, z, pitch, lookX, lookZ, sneaking, (int[])null);
      } else {
         int want = this.aim(pitch, lookX, lookZ, sneaking);
         if (want != this.orient) {
            this.orient = want;
            this.seed(x, y, z);
            if (this.locked && this.orient == this.baseOrient) {
               this.plane = this.basePlane;
            }

            this.lx = x;
            this.ly = y;
            this.lz = z;
         }

         if (this.orient == 0) {
            this.walk(this.lx, this.lz, this.ly, x, z, y);
         } else if (this.orient == 1) {
            this.walk(this.lx, this.ly, this.lz, x, y, z);
         } else {
            this.walk(this.lz, this.ly, this.lx, z, y, x);
         }

      }
   }

   private void walk(double a0, double b0, double c0, double a1, double b1, double c1) {
      int ca = (int)Math.floor(a0);
      int cb = (int)Math.floor(b0);
      int ea = (int)Math.floor(a1);
      int eb = (int)Math.floor(b1);
      double da = a1 - a0;
      double db = b1 - b0;
      int sa = da > 0.0D ? 1 : (da < 0.0D ? -1 : 0);
      int sb = db > 0.0D ? 1 : (db < 0.0D ? -1 : 0);
      double big = Double.MAX_VALUE;
      double tda = sa != 0 ? Math.abs(1.0D / da) : big;
      double tdb = sb != 0 ? Math.abs(1.0D / db) : big;
      double tma = sa > 0 ? ((double)(ca + 1) - a0) / da : (sa < 0 ? (a0 - (double)ca) / -da : big);
      double tmb = sb > 0 ? ((double)(cb + 1) - b0) / db : (sb < 0 ? (b0 - (double)cb) / -db : big);
      boolean alongA = Math.abs(da) >= Math.abs(db);
      double t = 0.0D;
      int visited = 1;

      while(true) {
         if (visited <= 33) {
            double c = c0 + (c1 - c0) * t;
            this.stamp(ca, cb, c, alongA);
            if (ca != ea || cb != eb) {
               double next = Math.min(tma, tmb);
               if (!(next > 1.0D)) {
                  if (visited < 32 && this.queue.size() < 96) {
                     if (tma < tmb) {
                        t = tma;
                        tma += tda;
                        ca += sa;
                     } else {
                        t = tmb;
                        tmb += tdb;
                        cb += sb;
                     }

                     ++visited;
                     continue;
                  }

                  double tr = Math.min(1.0D, next + 1.0E-6D);
                  this.setLast(a0 + da * tr, b0 + db * tr, c0 + (c1 - c0) * tr);
                  return;
               }
            }
         }

         this.setLast(a1, b1, c1);
         return;
      }
   }

   private void setLast(double a, double b, double c) {
      if (this.orient == 0) {
         this.lx = a;
         this.lz = b;
         this.ly = c;
      } else if (this.orient == 1) {
         this.lx = a;
         this.ly = b;
         this.lz = c;
      } else {
         this.lz = a;
         this.ly = b;
         this.lx = c;
      }

   }

   private void stamp(int ca, int cb, double third, boolean alongA) {
      int[] c;
      if (this.orient == 0) {
         c = new int[]{ca, 0, cb, 0};
      } else if (this.orient == 1) {
         c = new int[]{ca, cb, 0, 3};
      } else {
         c = new int[]{0, cb, ca, 3};
      }

      long ck = key(ca, cb, this.orient);
      boolean moved = ck != this.lastCenter;
      if (this.orient == 0) {
         if (moved) {
            this.follow(heightOf(third));
         }

         c[1] = cellYOf(this.plane);
         c[3] = layerOfHeight(this.plane);
      } else {
         if (moved) {
            this.followDepth(third);
         }

         if (this.orient == 1) {
            c[2] = this.plane;
         } else {
            c[0] = this.plane;
         }
      }

      this.lastCenter = ck;
      int[][] offs = new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
      int[] order = alongA ? new int[]{0, 3, 4, 1, 2} : new int[]{0, 1, 2, 3, 4};

      for(int i : order) {
         int[] at = this.inPlane(c, offs[i][0], offs[i][1]);
         this.enqueue(at[0], at[1], at[2], c[3], i == 0);
      }

   }

   private void follow(int raw) {
      int diff = raw - this.plane;
      if (diff > 1) {
         this.plane += Math.min(2, diff - 1);
      } else if (diff < -1) {
         this.plane -= Math.min(2, -diff - 1);
      }

   }

   private void followDepth(double d) {
      double off = d - ((double)this.plane + 0.5D);
      if (off > 1.5D) {
         ++this.plane;
      } else if (off < -1.5D) {
         --this.plane;
      }

   }

   private void enqueue(int x, int y, int z, int layer, boolean center) {
      int surface;
      long col;
      if (this.orient == 0) {
         surface = 4 * y + layer + 1;
         col = key(x, z, 0);
      } else if (this.orient == 1) {
         surface = z;
         col = key(x, y, 1);
      } else {
         surface = x;
         col = key(z, y, 2);
      }

      int near = this.orient == 0 ? 8 : 2;
      List<int[]> list = (List)this.cols.get(col);
      if (list != null) {
         for(int[] e : list) {
            if (e[3] == this.orient && Math.abs(e[5] - surface) < near) {
               if (center && e[6] == 0 && e[5] != surface) {
                  e[0] = x;
                  e[1] = y;
                  e[2] = z;
                  e[4] = layer;
                  e[5] = surface;
               }

               return;
            }
         }
      }

      if (this.cols.size() > 2048) {
         this.cols.clear();
         list = null;
      }

      if (list == null) {
         list = new ArrayList(1);
         this.cols.put(col, list);
      }

      int[] e = new int[]{x, y, z, this.orient, layer, surface, 0};
      list.add(e);
      this.queue.add(e);
   }
}
