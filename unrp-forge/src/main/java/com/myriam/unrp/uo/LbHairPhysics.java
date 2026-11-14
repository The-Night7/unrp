package com.myriam.unrp.uo;



import net.minecraft.util.Mth;

public final class LbHairPhysics {
   public static final int STRIDE = 8;
   public static final int AIR_X = 0;
   public static final int AIR_Y = 1;
   public static final int AIR_Z = 2;
   public static final int GRAV_Y = 3;
   public static final int GRAV_Z = 4;
   public static final int SPIN = 5;
   public static final int LIFT = 6;
   public static final int ENERGY = 7;
   public static final int TIME = 8;
   public static final int INPUTS = 9;
   public static final float[] STIFF = new float[]{0.46F, 0.3F, 0.24F, 0.12F, 0.2F, 0.26F};
   public static final float[] DAMP = new float[]{0.62F, 0.4F, 0.33F, 0.21F, 0.24F, 0.3F};
   public static final float[] GIVE = new float[]{0.04F, 0.16F, 0.21F, 0.42F, 0.34F, 0.24F};
   public static final float[] REACH = new float[]{0.07F, 0.36F, 0.52F, 1.05F, 0.85F, 0.62F};
   public static final float[] WEIGHT = new float[]{0.0F, 0.12F, 0.42F, 0.9F, 0.28F, 0.0F};
   public static final float[] DRAG = new float[]{0.5F, 2.2F, 2.7F, 3.4F, 3.9F, 2.1F};
   public static final float[] TWIST = new float[]{0.6F, 4.5F, 6.0F, 8.5F, 8.0F, 4.0F};
   public static final float[] RISE = new float[]{0.0F, 0.35F, 0.6F, 0.7F, 1.6F, 3.4F};
   public static final float[] FLICK = new float[]{0.004F, 0.04F, 0.065F, 0.03F, 0.08F, 0.11F};
   public static final float[] WHIP = new float[]{1.0F, 1.3F, 1.45F, 1.6F, 1.85F, 1.5F};
   public static final float[] LAG = new float[]{0.0F, 1.6F, 2.0F, 2.6F, 3.0F, 2.2F};

   private LbHairPhysics() {
   }

   public static void step(float[] st, float[] in, float dt) {
      int n = 581;
      int steps = Math.max(1, Mth.ceil(dt * 2.0F));
      float h = dt / (float)steps;

      for(int i = 0; i < n; ++i) {
         int k = LbHairGeometry.KIND[i];
         int a = i * 6;
         int r = i * 2;
         int b = i * 8;
         float ph = LbHairGeometry.PHASE[i];
         float rx = LbHairGeometry.ROOT[r];
         float rz = LbHairGeometry.ROOT[r + 1];
         float fx = in[0] * DRAG[k] - in[5] * TWIST[k] * rz * 0.0625F;
         float fy = in[1] * DRAG[k] + in[3] * WEIGHT[k] - in[6] * RISE[k];
         float fz = in[2] * DRAG[k] + in[4] * WEIGHT[k] + in[5] * TWIST[k] * rx * 0.0625F;
         float lx = fx * LbHairGeometry.AXIS[a] + fy * LbHairGeometry.AXIS[a + 1] + fz * LbHairGeometry.AXIS[a + 2];
         float lz = fx * LbHairGeometry.AXIS[a + 3] + fy * LbHairGeometry.AXIS[a + 4] + fz * LbHairGeometry.AXIS[a + 5];
         float flick = FLICK[k] * in[7];
         float nx = flick * Mth.sin(in[8] * (0.23F + 0.07F * ph) + ph * 6.283F);
         float nz = flick * Mth.cos(in[8] * (0.19F + 0.05F * ph) + ph * 9.425F);
         float tx = Mth.clamp(-lz * GIVE[k] + nx, -REACH[k], REACH[k]);
         float tz = Mth.clamp(lx * GIVE[k] + nz, -REACH[k], REACH[k]);
         float soft = STIFF[k] * 0.7F;

         for(int s = 0; s < steps; ++s) {
            float ax = STIFF[k] * (tx - st[b]) - DAMP[k] * st[b + 2];
            float az = STIFF[k] * (tz - st[b + 1]) - DAMP[k] * st[b + 3];
            st[b + 2] += ax * h;
            st[b + 3] += az * h;
            st[b] += st[b + 2] * h;
            st[b + 1] += st[b + 3] * h;
            float wx = tx * (WHIP[k] - 1.0F) - st[b + 2] * LAG[k] + nx * 0.8F;
            float wz = tz * (WHIP[k] - 1.0F) - st[b + 3] * LAG[k] + nz * 0.8F;
            float bx = soft * (wx - st[b + 4]) - DAMP[k] * 0.8F * st[b + 6];
            float bz = soft * (wz - st[b + 5]) - DAMP[k] * 0.8F * st[b + 7];
            st[b + 6] += bx * h;
            st[b + 7] += bz * h;
            st[b + 4] += st[b + 6] * h;
            st[b + 5] += st[b + 7] * h;
         }

         st[b] = Mth.clamp(st[b], -REACH[k] * 1.5F, REACH[k] * 1.5F);
         st[b + 1] = Mth.clamp(st[b + 1], -REACH[k] * 1.5F, REACH[k] * 1.5F);
         st[b + 4] = Mth.clamp(st[b + 4], -REACH[k] * 1.5F, REACH[k] * 1.5F);
         st[b + 5] = Mth.clamp(st[b + 5], -REACH[k] * 1.5F, REACH[k] * 1.5F);
      }

   }

   public static void frame(float[] in, float ax, float ay, float az, float yaw, float pitch, float gravity) {
      float s = Mth.sin(yaw);
      float c = Mth.cos(yaw);
      float x = ax * c + az * s;
      float z = ax * s - az * c;
      float y = -ay;
      float cp = Mth.cos(pitch);
      float sp = Mth.sin(pitch);
      in[0] = x;
      in[1] = y * cp + z * sp;
      in[2] = -y * sp + z * cp;
      in[3] = cp * gravity;
      in[4] = -sp * gravity;
   }

   public static void kick(float[] st, float amount) {
      int n = 581;

      for(int i = 0; i < n; ++i) {
         int k = LbHairGeometry.KIND[i];
         int a = i * 6;
         int b = i * 8;
         float up = amount * RISE[k] * 0.05F;
         st[b + 2] += up * LbHairGeometry.AXIS[a + 4];
         st[b + 3] -= up * LbHairGeometry.AXIS[a + 1];
         st[b + 6] += up * LbHairGeometry.AXIS[a + 4] * 1.5F;
         st[b + 7] -= up * LbHairGeometry.AXIS[a + 1] * 1.5F;
      }

   }
}
