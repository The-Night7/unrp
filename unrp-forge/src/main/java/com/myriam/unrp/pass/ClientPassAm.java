package com.myriam.unrp.pass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ClientPassAm {
   public static final String AM_POWER = "aura_manipulation";
   public static final String TOGGLE = "power_toggle";
   public static final double FIRST_ON = 20.0D;
   public static final int KEEP = 40;
   public static final int FX_MOST = 128;
   public static final int TOLD_MOST = 64;
   public static final double TOLD_KEEP = 2.0D;
   public static final float ON_VOL = 1.5F;
   public static final float TAKE_VOL = 1.0F;
   public static final float EXPEL_VOL = 2.0F;
   public static final double CHEST = 0.72D;
   public static final double FED_KEEP = 2.0D;
   static final int K_IGNITE = 0;
   static final int K_SENSE = 1;
   static final int K_LANDED = 2;
   static final int K_EXPEL = 3;
   static final Map<Integer, ClientPassAm.Am> AM = new HashMap();
   static final List<ClientPassAm.Fx> FX = new ArrayList();

   private ClientPassAm() {
   }

   static void clear() {
      AM.clear();
      FX.clear();
   }

   static boolean empty() {
      return AM.isEmpty() && FX.isEmpty();
   }

   static PassBridge.Holder holder(AbstractClientPlayer p) {
      return PassBridge.holder(p, new String[]{"aura_manipulation"});
   }

   static boolean mine(Entity e) {
      return e != null && e == Minecraft.getInstance().player;
   }

   static boolean heard(Entity c) {
      return c == null || !c.isInvisible() || mine(c);
   }

   static ClientPassAm.Am am(int id, double now) {
      ClientPassAm.Am s = (ClientPassAm.Am)AM.get(id);
      if (s == null) {
         s = new ClientPassAm.Am(id, now);
         AM.put(id, s);
      }

      return s;
   }

   static void fx(ClientPassAm.Fx f) {
      while(FX.size() >= 128) {
         FX.remove(0);
      }

      FX.add(f);
   }

   static double[] feetOf(ClientLevel level, int id, double[] or) {
      Entity e = id != 0 ? level.getEntity(id) : null;
      return e != null && !e.isRemoved() ? ClientPass.a(e.position()) : or;
   }

   static ClientPassAm.Fx last(int kind, int caster, double now) {
      for(int i = FX.size() - 1; i >= 0; --i) {
         ClientPassAm.Fx f = (ClientPassAm.Fx)FX.get(i);
         if (f.kind == kind && f.caster == caster && now - f.born <= 2.0D) {
            return f;
         }
      }

      return null;
   }

   static void apply(PassFxPacket m, ClientLevel level, double now) {
      if (m.caster != 0) {
         Entity caster = level.getEntity(m.caster);
         double[] feet = feetOf(level, m.caster, m.at());
         boolean told = m.has(8);
         if (m.op == 41) {
            am(m.caster, now).toldOn = now;
            fx(new ClientPassAm.Fx(0, m.caster, ClientPass.groundUnder(level, feet), (double[])null, now, 40.0D));
            if (ClientPass.near(feet) && heard(caster)) {
               ClientPass.sound("am_on", feet, 1.5F);
            }
         } else if (m.op == 42) {
            if (!mine(caster)) {
               return;
            }

            if (!told) {
               fx(new ClientPassAm.Fx(1, m.caster, ClientPass.groundUnder(level, feet), (double[])null, now, 30.0D));
               return;
            }

            ClientPassAm.Fx f = last(1, m.caster, now);
            if (f != null && f.told.size() < 64) {
               f.told.put(m.target, PassAm.col(m.color));
            }
         } else if (m.op == 43) {
            if (!mine(caster)) {
               return;
            }

            ClientPassAm.Am s = am(m.caster, now);
            double[] col = PassAm.col(m.color);
            s.takeAt = now;
            s.takeCol = col;
            fx(new ClientPassAm.Fx(2, m.caster, feet, col, now + 12.0D, 30.0D));
            if (ClientPass.near(feet)) {
               ClientPass.sound("am_take", feet, 1.0F);
            }
         } else if (m.op == 44 && !told) {
            fx(new ClientPassAm.Fx(3, m.caster, ClientPass.groundUnder(level, feet), (double[])null, now + 5.0D, 60.0D));
         }

      }
   }

   static void tick(Minecraft mc, ClientLevel level, double now) {
      tickUsers(level, now);
      Iterator<ClientPassAm.Fx> it = FX.iterator();

      while(it.hasNext()) {
         ClientPassAm.Fx f = (ClientPassAm.Fx)it.next();
         if (f.kind == 1 && now - f.born <= f.life) {
            gather(level, f);
         }

         if (f.kind == 3 && !f.fired && now >= f.born) {
            f.fired = true;
            Entity c = level.getEntity(f.caster);
            if (!heard(c)) {
               it.remove();
               continue;
            }

            if (c != null && !c.isRemoved()) {
               f.at = ClientPass.groundUnder(level, ClientPass.a(c.position()));
            }

            if (ClientPass.near(f.at)) {
               ClientPass.sound("am_expel", f.at, 2.0F);
            }

            ClientPass.kick(f.at, now, 0.4D, 32.0D, 8.0D);
            cut(f.caster, f.born - 5.0D, now);
         }

         if (now - f.born > f.life + 1.0D || !Double.isNaN(f.cut)) {
            it.remove();
         }
      }

   }

   static void cut(int caster, double decided, double now) {
      for(ClientPassAm.Fx x : FX) {
         if (x.caster == caster && Double.isNaN(x.cut) && x.kind == 2 && x.born > now && x.born - 12.0D <= decided) {
            x.cut = now;
         }
      }

      ClientPassAm.Am s = (ClientPassAm.Am)AM.get(caster);
      if (s != null && !Double.isNaN(s.takeAt) && s.takeAt <= decided && s.takeAt + 12.0D > now) {
         s.takeAt = Double.NaN;
      }

   }

   static void tickUsers(ClientLevel level, double now) {
      Set<Integer> here = new HashSet();

      for(AbstractClientPlayer p : level.players()) {
         PassBridge.Holder h = holder(p);
         if (h != null) {
            ClientPassAm.Am s = am(p.getId(), now);
            here.add(p.getId());
            boolean first = !s.known;
            s.known = true;
            s.seen = now;
            boolean live = !p.isSpectator() && ClientPass.enabled(h, "power_toggle");
            if (live && !s.on) {
               double back = Double.isNaN(s.offAt) ? 0.0D : PassAm.eyesOut(now - s.offAt, s.litAtOff) * 8.0D;
               s.onSince = first && now - s.toldOn > 2.0D ? now - 20.0D : now - back;
               s.offAt = Double.NaN;
            } else if (!live && s.on) {
               s.litAtOff = Double.isNaN(s.onSince) ? 0.0D : PassAm.eyesIn(now - s.onSince);
               s.offAt = now;
               s.onSince = Double.NaN;
            }

            s.on = live;
         }
      }

      Iterator<Map.Entry<Integer, ClientPassAm.Am>> it = AM.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<Integer, ClientPassAm.Am> e = (Map.Entry)it.next();
         if (!here.contains(e.getKey())) {
            ClientPassAm.Am s = (ClientPassAm.Am)e.getValue();
            s.known = false;
            s.on = false;
            s.onSince = Double.NaN;
            s.offAt = Double.NaN;
            if (now - s.seen > 40.0D) {
               it.remove();
            }
         }
      }

   }

   static void want(Set<Integer> w) {
      for(ClientPassAm.Am s : AM.values()) {
         if (s.on) {
            w.add(s.id);
         }
      }

      for(ClientPassAm.Fx f : FX) {
         w.add(f.caster);
      }

   }

   static void gather(ClientLevel level, ClientPassAm.Fx f) {
      f.bodies.clear();
      double rr = 24.0D;
      AABB box = new AABB(f.at[0] - rr, f.at[1] - rr, f.at[2] - rr, f.at[0] + rr, f.at[1] + rr, f.at[2] + rr);

      for(LivingEntity o : level.getEntitiesOfClass(LivingEntity.class, box, (le) -> le.getId() != f.caster && le.isAlive() && !(le instanceof ArmorStand))) {
         f.bodies.add(o.getId());
      }

   }

   static boolean owns(AbstractClientPlayer p) {
      ClientPassAm.Am s = (ClientPassAm.Am)AM.get(p.getId());
      return s != null && s.known && s.on && !Double.isNaN(s.onSince);
   }

   public static boolean eyes(AbstractClientPlayer p, double k, double flare) {
      if (!owns(p)) {
         return false;
      } else {
         ClientPassAm.Am s = (ClientPassAm.Am)AM.get(p.getId());
         double now = (double)p.level().getGameTime();
         if (now != s.fedAt) {
            s.fedK = 0.0D;
            s.fedFlare = 0.0D;
         }

         s.fedK = Math.max(s.fedK, k);
         s.fedFlare = Math.max(s.fedFlare, flare);
         s.fedAt = now;
         return true;
      }
   }

   public static double[] look(AbstractClientPlayer p, double t) {
      ClientPassAm.Am s = (ClientPassAm.Am)AM.get(p.getId());
      if (s != null && s.known) {
         double lit;
         if (s.on && !Double.isNaN(s.onSince)) {
            lit = PassAm.eyesIn(t - s.onSince);
         } else {
            if (Double.isNaN(s.offAt)) {
               return null;
            }

            lit = PassAm.eyesOut(t - s.offAt, s.litAtOff);
         }

         double fa = PassAm.flareAge(t, s.takeAt, mine(p));
         double flare = PassAm.eyeFlare(fa);
         if (s.on && t - s.fedAt <= 2.0D) {
            lit = Math.max(lit, s.fedK);
            flare = Math.max(flare, s.fedFlare);
         }

         double[] col = PassAm.eyeColor(fa, s.takeCol);
         double burst = s.on ? EyeGlowMath.burst(t - s.toldOn) : 0.0D;
         return new double[]{lit, flare, EyeGlowMath.pulse(t, p.getId()), burst, (double)rgbOf(col)};
      } else {
         return null;
      }
   }

   static int rgbOf(double[] c) {
      int r = (int)Math.round(PassMath.clamp01(c[0]) * 255.0D);
      int g = (int)Math.round(PassMath.clamp01(c[1]) * 255.0D);
      int b = (int)Math.round(PassMath.clamp01(c[2]) * 255.0D);
      return r << 16 | g << 8 | b;
   }

   static double[] chestOf(Entity e, float pt) {
      Vec3 p = e.getPosition(pt);
      return new double[]{p.x, p.y + (double)e.getBbHeight() * 0.72D, p.z};
   }

   static void draw(PassQuads q, ClientLevel level, double t, float pt, double[] cam, double r) {
      q.dim = null;

      try {
         for(ClientPassAm.Fx f : FX) {
            if (f.at != null && ClientPass.near(f.at)) {
               drawFx(q, level, f, t, pt);
            }
         }
      } finally {
         q.dim = null;
      }

   }

   static void drawFx(PassQuads q, ClientLevel level, ClientPassAm.Fx f, double t, float pt) {
      double age = t - f.born;
      if (!(age < 0.0D) && !(age > f.life)) {
         Entity c = level.getEntity(f.caster);
         boolean here = c != null && !c.isRemoved();
         if (f.kind == 0) {
            PassAm.ignite(q, f.at, age);
         } else if (f.kind == 1) {
            if (!mine(c)) {
               return;
            }

            PassAm.sense(q, f.at, age);

            for(int id : f.bodies) {
               LivingEntity o = ClientPass.living(level, id);
               if (o != null && o != c && o.isAlive() && !o.isInvisible() && !o.isSpectator() && !ClientPass.firstSelf(o)) {
                  double since = PassAm.reached(Math.hypot(o.getX() - f.at[0], o.getZ() - f.at[2]), age);
                  if (PassAm.flares(since)) {
                     double[] col = (double[])f.told.get(o.getId());
                     PassAm.flare(q, ClientPass.a(o.getPosition(pt)), chestOf(o, pt), since, col != null ? col : PassAm.EMBER);
                  }
               }
            }
         } else if (f.kind == 2) {
            if (!here || !mine(c)) {
               return;
            }

            PassAm.landed(q, ClientPass.handOf(c, pt), age, f.col, ClientPass.firstSelf(c));
         } else if (f.kind == 3) {
            PassAm.expel(q, ClientPass.light(level, f.at, pt), f.at, age);
         }

      }
   }

   static final class Am {
      final int id;
      boolean known;
      boolean on;
      double onSince = Double.NaN;
      double offAt = Double.NaN;
      double litAtOff;
      double toldOn = -1.0E9D;
      double takeAt = Double.NaN;
      double[] takeCol = PassAm.EMBER;
      double fedK;
      double fedFlare;
      double fedAt = -1.0E9D;
      double seen;

      Am(int id, double now) {
         this.id = id;
         this.seen = now;
      }
   }

   static final class Fx {
      final int kind;
      final int caster;
      double[] at;
      final double born;
      final double life;
      final double[] col;
      final Map<Integer, double[]> told = new HashMap();
      final List<Integer> bodies = new ArrayList();
      boolean fired;
      double cut = Double.NaN;

      Fx(int kind, int caster, double[] at, double[] col, double born, double life) {
         this.kind = kind;
         this.caster = caster;
         this.at = at;
         this.col = col;
         this.born = born;
         this.life = life;
      }
   }
}
