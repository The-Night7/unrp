package com.myriam.unrp.pass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ClientPassBr {
   public static final String[] BR_POWERS = new String[]{"barrier", "barrier_am"};
   public static final double SHELL_SEEN = 160.0D;
   public static final int HIT_GAP = 4;
   public static final int ACT_FLARE = 12;
   public static final int FRESH = 40;
   public static final double RAM_AHEAD = 1.6D;
   public static final double RAM_LIFT = 0.1D;
   public static final int DEPLOY_KICK = 3;
   public static final int RAM_WINDOW = 16;
   public static final double RAM_NEAR = 4.0D;
   public static final int FRAME_FRESH = 2;
   public static final double REACH_MIN = 0.9D;
   public static final double FP_FADE = 0.3D;
   public static final double FADE_NEAR = 1.6D;
   public static final double FADE_FAR = 4.0D;
   public static final double FLOOR_STEP = 0.6D;
   public static final int FLOOR_PROBES = 8;
   public static final int SQUEEZE_GAP = 1;
   public static final int LATE_WAIT = 3;
   public static final int LATE_MOST = 64;
   static final int K_SHATTER = 0;
   static final int K_BREAK = 1;
   static final int K_CAST = 2;
   static final int K_SQUEEZE = 3;
   static final int K_CRUSH = 4;
   static final int K_RAM_HIT = 5;
   static final int K_SMOKE = 6;
   static final Map<Integer, ClientPassBr.Sh> SHELLS = new HashMap();
   static final List<ClientPassBr.Fx> FX = new ArrayList();
   static final Map<Integer, ClientPassBr.Br> BR = new HashMap();
   static final List<ClientPassBr.Late> LATE = new ArrayList();
   static final Map<Integer, Double> FLASH = new HashMap();

   private ClientPassBr() {
   }

   static void clear() {
      SHELLS.clear();
      FX.clear();
      BR.clear();
      LATE.clear();
      FLASH.clear();
   }

   static boolean empty() {
      return SHELLS.isEmpty() && FX.isEmpty() && BR.isEmpty() && LATE.isEmpty();
   }

   static PassBridge.Holder holder(AbstractClientPlayer p) {
      return PassBridge.holder(p, BR_POWERS);
   }

   static PassBr.Cols cols(UUID owner) {
      if (owner != null && PassBridge.isAM(owner)) {
         return PassBr.cols(0, false, true);
      } else {
         int c = owner == null ? -1 : PassBridge.color(owner);
         return c == -1 ? PassBr.cols(0, false, false) : PassBr.cols(c & 16777215, true, false);
      }
   }

   static PassBr.Cols colsOf(ClientLevel level, ClientPassBr.Sh s, PassFxPacket m) {
      UUID owner = s != null && s.owner != null ? s.owner : uuidOf(level, m.caster);
      if (owner != null) {
         return cols(owner);
      } else if (m.has(1)) {
         return PassBr.cols(0, false, true);
      } else {
         return m.color == 16777215 ? PassBr.cols(0, false, false) : PassBr.cols(m.color, true, false);
      }
   }

   static UUID uuidOf(ClientLevel level, int id) {
      Entity e = id != 0 ? level.getEntity(id) : null;
      return e != null ? e.getUUID() : null;
   }

   static double[] center(ClientLevel level, ClientPassBr.Sh s) {
      Entity e = level.getEntity(s.s.id);
      if (e instanceof BarrierEntity b) {
         if (!b.isRemoved()) {
            return ClientPass.a(b.center());
         }
      }

      return s.s.at;
   }

   static double[] seeded(int id, int k) {
      double a = PassMath.h((double)id, (double)k, 61.0D) * 6.283D;
      return PassMath.norm(PassMath.v(Math.cos(a), 0.25D, Math.sin(a)));
   }

   static void crack(ClientPassBr.Sh s, double now, double[] dir, boolean heard) {
      boolean once = heard;

      while(s.s.cracks.size() < Math.min(3, s.s.crossed)) {
         double[] d = dir != null ? dir : seeded(s.s.id, s.s.cracks.size());
         s.s.cracks.add(new PassBr.Crack(now, d));
         if (once && s.s.at != null) {
            once = false;
            double[] at = PassMath.add(s.s.at, PassMath.mul(d, s.s.size / 2.0D));
            if (ClientPass.near(at)) {
               ClientPass.sound("br_crack", at, 1.0F);
            }
         }
      }

   }

   static ClientPassBr.Sh track(ClientLevel level, BarrierEntity b, int clock, double now) {
      ClientPassBr.Sh s = new ClientPassBr.Sh(b.getId());
      int age = clock - b.getBorn();
      boolean fresh = b.getBorn() != 0 && age > -40 && age < 40;
      s.s.build = fresh ? now - (double)Math.max(0, age) : now - 40.0D;
      s.s.size = (double)b.getSize();
      s.s.at = ClientPass.a(b.center());
      s.owner = b.getOwnerId();
      s.ram = b.isRam();
      Double fl = (Double)FLASH.remove(b.getId());
      if (fl != null) {
         s.ramHitT = fl;
      }

      if (!fresh) {
         s.seenStage = b.getStage();
         s.s.crossed = Math.min(4, s.seenStage);
         crack(s, now - 40.0D, (double[])null, false);
      }

      SHELLS.put(b.getId(), s);
      Iterator<ClientPassBr.Late> it = LATE.iterator();

      while(it.hasNext()) {
         ClientPassBr.Late l = (ClientPassBr.Late)it.next();
         if (l.m.aux == s.s.id) {
            it.remove();
            blow(level, s, l.m, l.t);
         }
      }

      return s;
   }

   static ClientPassBr.Sh known(ClientLevel level, int id, double now) {
      ClientPassBr.Sh s = (ClientPassBr.Sh)SHELLS.get(id);
      if (s == null && id != 0) {
         Entity var6 = level.getEntity(id);
         if (var6 instanceof BarrierEntity) {
            BarrierEntity b = (BarrierEntity)var6;
            if (!b.isRemoved()) {
               s = track(level, b, (int)level.getGameTime(), now);
            }
         }
      }

      return s;
   }

   static double[] facing(PassFxPacket m) {
      double yaw = (double)m.v[4];
      double pitch = (double)m.v[5];
      return PassMath.v(Math.cos(yaw) * Math.cos(pitch), Math.sin(pitch), Math.sin(yaw) * Math.cos(pitch));
   }

   static PassBr.Frame breakFrame(ClientLevel level, ClientPassBr.Sh s, PassFxPacket m, double now) {
      if (s != null && s.frame != null && now - s.frameT <= 2.0D) {
         return s.frame;
      } else {
         if (s != null) {
            PassBr.Frame fr = ownerFrame(level, s, 1.0F, now);
            if (fr != null) {
               return fr;
            }
         }

         double[] c = s != null ? center(level, s) : m.at();
         double[] f = m.has(4) ? facing(m) : (s != null ? s.dir : PassMath.v(1.0D, 0.0D, 0.0D));
         return new PassBr.Frame(c, f);
      }
   }

   static void breakShield(ClientLevel level, ClientPassBr.Sh s, PassFxPacket m, PassBr.Cols cl, double now, boolean heard) {
      PassBr.Frame fr = breakFrame(level, s, m, now);
      ClientPassBr.Fx f = new ClientPassBr.Fx(1, m.aux, 0, fr.c, cl, now, 44.0D);
      f.frame = fr;
      FX.add(f);
      if (heard) {
         if (ClientPass.near(fr.c)) {
            ClientPass.sound("br_shatter", fr.c, 0.45F);
         }

         ClientPass.kick(fr.c, now, 0.3D, 48.0D, 12.0D);
      }

   }

   static void apply(PassFxPacket m, ClientLevel level, double now) {
      ClientPassBr.Sh s = known(level, m.aux, now);
      PassBr.Cols cl = colsOf(level, s, m);
      if (m.op == 13) {
         if (s == null) {
            if (m.aux != 0 && LATE.size() < 64) {
               LATE.add(new ClientPassBr.Late(m, now));
            }

            return;
         }

         blow(level, s, m, now);
      } else if (m.op == 14) {
         boolean ram = s != null ? s.ram : m.target == 1;
         if (s != null) {
            if (s.s.gone < 0.0D) {
               s.s.at = center(level, s);
            }

            s.s.gone = now;
            s.s.shattered = true;
         }

         if (ram) {
            breakShield(level, s, m, cl, now, true);
         } else {
            double[] c = s != null ? s.s.at : m.at();
            ClientPassBr.Fx f = new ClientPassBr.Fx(0, m.aux, 0, c, cl, now, 60.0D);
            f.r = s != null ? s.s.size / 2.0D : (double)m.v[3];
            f.floor = s != null ? s.floor : Double.NaN;
            FX.add(f);
            if (ClientPass.near(c)) {
               ClientPass.sound("br_shatter", c, 2.5F);
            }

            ClientPass.kick(c, now, 0.85D, 48.0D, 12.0D);
         }
      } else if (m.op == 15) {
         if (s != null && s.s.gone < 0.0D) {
            s.s.gone = now;
            if (s.ram) {
               s.s.shattered = true;
               breakShield(level, s, m, cl, now, false);
            } else if (cl.copy) {
               FX.add(new ClientPassBr.Fx(6, m.aux, 0, center(level, s), cl, now, 33.0D));
            }
         } else if (s == null && m.target == 1) {
            breakShield(level, (ClientPassBr.Sh)null, m, cl, now, false);
         } else if (s == null && cl.copy) {
            FX.add(new ClientPassBr.Fx(6, m.aux, 0, m.at(), cl, now, 33.0D));
         }
      } else if (m.op == 16) {
         Entity c = level.getEntity(m.caster);
         double[] feet = c != null ? ClientPass.a(c.position()) : m.at();
         acted(m.caster, now);
         boolean far = m.has(64);
         boolean back = m.has(128);
         if (far) {
            ClientPassBr.Fx f = new ClientPassBr.Fx(2, m.aux, m.caster, m.at(), cl, now, back ? 10.0D : 14.0D);
            f.back = back;
            FX.add(f);
         }

         if (!back && ClientPass.near(feet)) {
            ClientPass.sound("br_deploy", feet, 1.5F);
         }

         if (!far) {
            ClientPass.kick(feet, now + 3.0D, 0.45D, 40.0D, 8.0D);
         }
      } else if (m.op == 17) {
         Entity c = level.getEntity(m.caster);
         double[] feet = c != null ? ClientPass.a(c.position()) : m.at();
         acted(m.caster, now);
         if (c != null) {
            double sign = m.has(128) ? -1.0D : 1.0D;

            for(ClientPassBr.Sh o : SHELLS.values()) {
               if (!o.ram && o.s.gone < 0.0D && c.getUUID().equals(o.owner)) {
                  o.s.growT = now;
                  o.s.growFrom = o.s.size;
                  o.s.growSign = sign;
               }
            }
         }

         if (ClientPass.near(feet)) {
            ClientPass.sound(m.has(128) ? "br_shrink" : "br_grow", feet, 1.0F);
         }
      } else if (m.op == 18) {
         Entity c = level.getEntity(m.caster);
         double[] feet = c != null ? ClientPass.a(c.position()) : m.at();
         acted(m.caster, now);
         if (ClientPass.near(feet)) {
            ClientPass.sound("br_ram", feet, 1.5F);
         }
      } else if (m.op == 19) {
         ClientPassBr.Fx f = null;

         for(ClientPassBr.Fx x : FX) {
            if (x.kind == 3 && x.sid == m.aux && now - x.born <= 1.0D && x.from == (double)m.v[3]) {
               f = x;
               break;
            }
         }

         if (f == null) {
            f = new ClientPassBr.Fx(3, m.aux, 0, m.at(), cl, now, 18.0D);
            f.from = (double)m.v[3];
            f.fromY = (double)m.v[4];
            f.r = (double)m.v[5];
            FX.add(f);
         }

         if (m.target != 0 && !f.caught.contains(m.target)) {
            f.caught.add(m.target);
         }
      } else if (m.op == 20) {
         FX.add(new ClientPassBr.Fx(4, m.aux, m.target, m.at(), cl, now, 10.0D));
      } else if (m.op == 21) {
         if (s != null) {
            s.ramHitT = now;
         } else if (m.aux != 0) {
            FLASH.put(m.aux, now);
         }

         ClientPassBr.Fx f = new ClientPassBr.Fx(5, m.aux, m.target, m.at(), cl, now, 24.0D);
         double[] dir = m.vec();
         f.vec = PassMath.len(dir) > 0.5D ? PassMath.norm(dir) : PassMath.v(0.0D, 0.0D, 1.0D);
         FX.add(f);
         ClientPassBr.Br by = (ClientPassBr.Br)BR.get(m.caster);
         if (by == null || by.rammed != now) {
            if (by != null) {
               by.rammed = now;
            }

            Entity t = level.getEntity(m.target);
            double[] at = t != null ? ClientPass.a(t.position()) : m.at();
            if (ClientPass.near(at)) {
               ClientPass.sound("br_hit", at, 1.0F);
            }

            ClientPass.kick(at, now, 0.55D, 32.0D, 7.0D);
         }
      }

   }

   static void blow(ClientLevel level, ClientPassBr.Sh s, PassFxPacket m, double now) {
      if (!(s.s.gone >= 0.0D)) {
         double[] dir = m.at();
         dir = PassMath.len(dir) > 0.5D ? PassMath.norm(dir) : PassMath.v(0.0D, 1.0D, 0.0D);
         PassBr.blow(s.s.hits, now, dir, (double)m.v[3]);
         if (s.ram) {
            s.ramHitT = Math.max(s.ramHitT, now);
         }

         double[] c = center(level, s);
         double[] hp = PassMath.add(c, PassMath.mul(dir, s.s.size / 2.0D));
         if (now - s.sndT >= 4.0D) {
            s.sndT = now;
            if (ClientPass.near(hp)) {
               ClientPass.sound("br_hit", hp, 0.75F);
            }
         }

         int was = s.s.crossed;
         s.s.crossed = Math.max(s.s.crossed, Math.min(4, m.target));
         if (s.s.crossed > was) {
            s.s.at = c;
            crack(s, now, dir, true);
         }

      }
   }

   static void acted(int caster, double now) {
      ClientPassBr.Br b = (ClientPassBr.Br)BR.get(caster);
      if (b != null) {
         b.act = now;
      }

   }

   static double floorOf(ClientLevel level, double[] c, double r) {
      double from = c[1] + 0.5D;
      double g = ClientPass.groundBelow(level, c[0], c[2], from);
      double low = g;

      for(int k = 0; k < 8; ++k) {
         double a = (double)k * Math.PI * 2.0D / 8.0D;

         for(int j = 0; j < 2; ++j) {
            double rr = r * (j == 0 ? 0.55D : 0.9D);
            double gk = ClientPass.groundAt(level, c[0] + Math.cos(a) * rr, c[2] + Math.sin(a) * rr, from);
            if (gk <= from + 0.05D && gk < low) {
               low = gk;
            }
         }
      }

      return low < g - 0.6D ? Math.max(low, c[1] - r) : Double.NaN;
   }

   static void tick(Minecraft mc, ClientLevel level, double now) {
      tickPlayers(level, now);
      Set<Integer> here = new HashSet();
      int clock = (int)level.getGameTime();

      for(Entity e : level.entitiesForRendering()) {
         if (e instanceof BarrierEntity b) {
            if (!b.isRemoved()) {
               here.add(b.getId());
               ClientPassBr.Sh s = (ClientPassBr.Sh)SHELLS.get(b.getId());
               if (s == null) {
                  s = track(level, b, clock, now);
               }

               s.owner = b.getOwnerId();
               s.ram = b.isRam();
               float size = b.getSize();
               if ((double)size != s.s.size) {
                  s.s.growT = now;
                  s.s.growFrom = s.s.size;
                  s.s.growSign = Math.signum((double)size - s.s.size);
                  s.s.size = (double)size;
               }

               if (s.s.gone < 0.0D) {
                  s.s.at = ClientPass.a(b.center());
                  if (!s.ram) {
                     s.floor = floorOf(level, s.s.at, s.s.size / 2.0D);
                  } else {
                     if (s.lastAt != null && (s.owner == null || level.getPlayerByUUID(s.owner) == null)) {
                        double[] mv = PassMath.sub(s.s.at, s.lastAt);
                        if (PassMath.len(mv) > 0.05D) {
                           s.dir = PassMath.norm(mv);
                        }
                     }

                     s.lastAt = (double[])s.s.at.clone();
                  }
               }

               int st = b.getStage();
               if (st < s.seenStage) {
                  s.s.crossed = st;

                  while(s.s.cracks.size() > Math.min(3, st)) {
                     s.s.cracks.remove(s.s.cracks.size() - 1);
                  }
               } else if (st > s.seenStage && st > s.s.crossed) {
                  s.s.crossed = Math.min(4, st);
                  crack(s, now, (double[])null, false);
               }

               s.seenStage = st;
            }
         }
      }

      Iterator<Map.Entry<Integer, ClientPassBr.Sh>> it = SHELLS.entrySet().iterator();

      while(it.hasNext()) {
         ClientPassBr.Sh s = (ClientPassBr.Sh)((Map.Entry)it.next()).getValue();
         if (!here.contains(s.s.id) && (s.s.gone < 0.0D || now - s.s.gone > 40.0D)) {
            it.remove();
         }
      }

      LATE.removeIf((l) -> now - l.t > 3.0D);
      FLASH.values().removeIf((x) -> now - x > 3.0D);
      FX.removeIf((f) -> now - f.born > f.life + 1.0D);
   }

   static void tickPlayers(ClientLevel level, double now) {
      Set<Integer> here = new HashSet();

      for(AbstractClientPlayer p : level.players()) {
         PassBridge.Holder h = holder(p);
         if (h != null) {
            ClientPassBr.Br s = (ClientPassBr.Br)BR.get(p.getId());
            boolean first = s == null;
            if (first) {
               s = new ClientPassBr.Br(p.getId());
               BR.put(p.getId(), s);
            }

            here.add(p.getId());
            boolean on = !p.isSpectator() && ClientPass.enabled(h, "power_toggle");
            boolean lit = on && ClientPass.unveiled(h);
            s.copy = ClientPass.pathOf(h).endsWith("_am") || PassBridge.isAM(p.getUUID());
            if (lit && !s.lit) {
               s.onSince = first ? now - 20.0D : now;
            } else if (!lit) {
               s.onSince = Double.NaN;
            }

            s.on = on;
            s.lit = lit;
         }
      }

      BR.keySet().removeIf((id) -> !here.contains(id));
   }

   static void want(Set<Integer> w) {
      for(ClientPassBr.Br b : BR.values()) {
         if (b.on) {
            w.add(b.id);
         }
      }

      for(ClientPassBr.Fx f : FX) {
         if (f.kind == 2 && f.ent != 0) {
            w.add(f.ent);
         }
      }

   }

   public static float eyeHeat(AbstractClientPlayer p, float pt) {
      ClientPassBr.Br s = (ClientPassBr.Br)BR.get(p.getId());
      ClientLevel level = ClientPass.where;
      return s != null && s.on && !Double.isNaN(s.onSince) && !s.copy && level != null && p.level() == level ? (float)(0.8D * PassMath.clamp01(((double)((float)level.getGameTime() + pt) - s.onSince) / 6.0D)) : 0.0F;
   }

   static PassBr.Frame ownerFrame(ClientLevel level, ClientPassBr.Sh s, float pt, double t) {
      Player owner = s.owner != null ? level.getPlayerByUUID(s.owner) : null;
      if (owner != null && !owner.isRemoved() && !(t - s.s.build > 16.0D)) {
         double[] f = ClientPass.a(owner.getViewVector(pt));
         if (PassMath.len(f) < 0.5D) {
            return null;
         } else {
            f = PassMath.norm(f);
            double[] eye = ClientPass.a(owner.getEyePosition(pt));
            double[] c = PassMath.add(PassMath.add(eye, PassMath.mul(f, 1.6D)), PassMath.v(0.0D, 0.1D, 0.0D));
            if (s.s.at != null && PassMath.len(PassMath.sub(c, s.s.at)) > 4.0D) {
               return null;
            } else {
               s.dir = f;
               return new PassBr.Frame(c, f);
            }
         }
      } else {
         return null;
      }
   }

   static PassBr.Frame frameOf(ClientLevel level, ClientPassBr.Sh s, float pt, double t) {
      PassBr.Frame fr = ownerFrame(level, s, pt, t);
      return fr != null ? fr : new PassBr.Frame(s.s.at, s.dir);
   }

   public static double[] cut(ClientLevel level, double[] eye, double[] dir, double[] hit, double range, float pt, double t) {
      double hitD = hit[0];
      boolean miss = hit[1] > 0.5D;
      boolean body = !miss && hit[2] < 0.5D;
      double[] best = null;
      double stop = Double.MAX_VALUE;

      for(ClientPassBr.Sh s : SHELLS.values()) {
         Entity e = level.getEntity(s.s.id);
         if (e instanceof BarrierEntity b) {
            if (!b.isRemoved() && !(s.s.gone >= 0.0D)) {
               double h = (double)b.getSize() / 2.0D;
               PassBr.Frame fr = s.ram ? frameOf(level, s, pt, t) : null;
               double[] lo;
               double[] hi;
               double[] feet;
               if (fr != null) {
                  lo = PassMath.v(fr.c[0] - h, fr.c[1] - h, fr.c[2] - h);
                  hi = PassMath.v(fr.c[0] + h, fr.c[1] + h, fr.c[2] + h);
                  feet = PassMath.v(fr.c[0], fr.c[1] - h, fr.c[2]);
               } else {
                  AABB bb = b.getBoundingBox();
                  lo = PassMath.v(bb.minX, bb.minY, bb.minZ);
                  hi = PassMath.v(bb.maxX, bb.maxY, bb.maxZ);
                  feet = PassMath.v(b.getX(), b.getY(), b.getZ());
               }

               double ts = PassBr.step(eye, dir, lo, hi, feet, range);
               if (!(ts < 0.0D) && !(ts > hitD) && !(ts > stop)) {
                  double[] f = fr != null ? PassBr.plate(fr, eye, dir, ts) : PassBr.face(ClientPass.a(b.center()), h, eye, dir, ts);
                  if (f[0] > hitD && !body) {
                     if (!miss) {
                        continue;
                     }

                     f = new double[]{ts, -dir[0], -dir[1], -dir[2]};
                  }

                  f[0] = Math.max(f[0], Math.min(0.9D, hitD));
                  if (best == null || ts < stop || f[0] < best[0]) {
                     stop = ts;
                     best = f;
                  }
               }
            }
         }
      }

      return best;
   }

   static double close(double[] eye, double[] c) {
      double d = PassMath.len(PassMath.sub(c, eye));
      return 0.3D + 0.7D * PassMath.clamp01((d - 1.6D) / 2.4D);
   }

   static void draw(PassQuads q, ClientLevel level, double t, float pt, double[] cam, double r) {
      PassFx.Ground g = (x, z, fromY) -> ClientPass.groundBelow(level, x, z, fromY);
      double[] fwd = q.fwd();

      for(ClientPassBr.Sh s : SHELLS.values()) {
         Entity e = level.getEntity(s.s.id);
         if (e instanceof BarrierEntity b) {
            if (!b.isRemoved() && s.s.gone < 0.0D) {
               Vec3 p = b.getPosition(pt);
               s.s.at = PassMath.v(p.x, p.y + s.s.size / 2.0D, p.z);
            }
         }

         if (s.s.at != null) {
            double[] d = PassMath.sub(s.s.at, cam);
            double reach = s.s.size / 2.0D + 2.0D;
            double dd = PassMath.dot(d, d);
            if (!(dd > 25600.0D) && (!(PassMath.dot(d, fwd) < -reach) || !(dd > reach * reach))) {
               PassBr.Cols cl = cols(s.owner);
               q.dim = cl.copy ? PassCast.SMOKE_DARK : null;
               if (s.ram) {
                  drawShield(q, level, s, cl, g, t, pt);
               } else {
                  PassBr.shell(q, cl, g, s.floor, s.s, t);
               }

               q.dim = null;
            }
         }
      }

      for(ClientPassBr.Fx f : FX) {
         if (ClientPass.near(f.at)) {
            drawFx(q, level, f, g, t, pt);
         }
      }

      for(ClientPassBr.Br b : BR.values()) {
         if (b.on && !Double.isNaN(b.onSince)) {
            Entity e = level.getEntity(b.id);
            if (e instanceof AbstractClientPlayer) {
               AbstractClientPlayer p = (AbstractClientPlayer)e;
               if (!p.isRemoved() && !p.isInvisible() && !p.isSpectator() && !p.isDeadOrDying() && !ClientPass.firstSelf(p) && !(p.distanceToSqr(cam[0], cam[1], cam[2]) > r * r)) {
                  PassBody body = PassBodies.get(p, pt);
                  double k = PassMath.clamp01((t - b.onSince) / 6.0D);
                  double fl = PassMath.clamp01(1.0D - (t - b.act) / 12.0D);
                  if (!b.copy) {
                     PassBridge.passFlare(p.getId(), fl, t);
                  } else if (body.posed() && !ClientPassAm.eyes(p, k, fl)) {
                     PassCast.eyesTrailer(q, body, PassCast.EMBER, k, fl);
                  }
               }
            }
         }
      }

      q.dim = null;
   }

   static void drawShield(PassQuads q, ClientLevel level, ClientPassBr.Sh s, PassBr.Cols cl, PassFx.Ground g, double t, float pt) {
      if (!(s.s.gone >= 0.0D)) {
         PassBr.Frame fr = frameOf(level, s, pt, t);
         s.frame = fr;
         s.frameT = t;
         double flash = s.ramHitT >= 0.0D ? PassMath.clamp01(1.0D - (t - s.ramHitT) / 5.0D) : 0.0D;
         int[] mark = q.counts();
         PassBr.shield(q, cl, g, fr, s.s.id, s.s.build, flash, t);
         double age = t - s.s.build;
         if (age >= 0.0D && age <= 12.0D) {
            PassBr.ram(q, cl, fr, t, age);
         }

         q.fade(mark, close(q.eye, fr.c));
      }
   }

   static void drawFx(PassQuads q, ClientLevel level, ClientPassBr.Fx f, PassFx.Ground g, double t, float pt) {
      double age = t - f.born;
      if (!(age < 0.0D) && !(age > f.life)) {
         PassBr.Cols cl = f.cl;
         q.dim = cl.copy ? PassCast.SMOKE_DARK : null;
         int[] mark = q.counts();
         if (f.kind == 0) {
            PassBr.shatter(q, cl, g, f.floor, f.at, f.r, f.sid, false, age);
         } else if (f.kind == 1) {
            if (f.frame != null) {
               PassBr.shieldBreak(q, cl, f.frame, f.sid, age);
               q.fade(mark, close(q.eye, f.frame.c));
            }
         } else if (f.kind == 2) {
            Entity e = level.getEntity(f.ent);
            if (e != null) {
               PassBr.cast(q, cl, ClientPass.handOf(e, pt), f.at, f.back, age);
            }
         } else if (f.kind == 3) {
            double[][] caught = new double[f.caught.size()][];

            for(int i = 0; i < caught.length; ++i) {
               Entity e = level.getEntity(f.caught.get(i));
               caught[i] = e != null && !e.isRemoved() ? ClientPass.mid(e, pt) : null;
            }

            PassBr.squeeze(q, cl, g, f.at, f.from, f.fromY, f.r, f.sid, caught, age);
         } else if (f.kind == 4) {
            Entity e = level.getEntity(f.ent);
            if (e != null && !e.isRemoved()) {
               PassBr.crush(q, cl, ClientPass.mid(e, pt), f.sid, f.born, age);
            }
         } else if (f.kind == 5) {
            PassBr.ramHit(q, cl, f.at, f.vec, f.born, age);
            q.fade(mark, close(q.eye, f.at));
         } else if (f.kind == 6) {
            PassAm.shellSmoke(q, ClientPass.light(level, PassAm.shellSmokeAt(f.at), pt), f.at, age);
            q.fade(mark, close(q.eye, PassAm.shellSmokeAt(f.at)));
         }

         q.dim = null;
      }
   }

   static final class Br {
      final int id;
      boolean on;
      boolean lit;
      double onSince = Double.NaN;
      double act = -1.0E9D;
      double rammed = -1.0E9D;
      boolean copy;

      Br(int id) {
         this.id = id;
      }
   }

   static final class Fx {
      final int kind;
      final int sid;
      final int ent;
      final double[] at;
      final PassBr.Cols cl;
      final double born;
      final double life;
      double[] vec;
      double r;
      double from;
      double fromY;
      double floor = Double.NaN;
      boolean back;
      PassBr.Frame frame;
      final List<Integer> caught = new ArrayList();

      Fx(int kind, int sid, int ent, double[] at, PassBr.Cols cl, double born, double life) {
         this.kind = kind;
         this.sid = sid;
         this.ent = ent;
         this.at = at;
         this.cl = cl;
         this.born = born;
         this.life = life;
      }
   }

   static final class Late {
      final PassFxPacket m;
      final double t;

      Late(PassFxPacket m, double t) {
         this.m = m;
         this.t = t;
      }
   }

   static final class Sh {
      final PassBr.Shell s = new PassBr.Shell();
      UUID owner;
      boolean ram;
      int seenStage;
      double sndT = -1.0E9D;
      double ramHitT = -1.0D;
      double[] dir = new double[]{1.0D, 0.0D, 0.0D};
      double[] lastAt;
      double floor = Double.NaN;
      PassBr.Frame frame;
      double frameT = -1.0E9D;

      Sh(int id) {
         this.s.id = id;
      }
   }
}
