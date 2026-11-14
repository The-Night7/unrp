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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class ClientPassEd {
   public static final String[] ED_POWERS = new String[]{"energy_discharge", "energy_discharge_am"};
   public static final double SPHERE_HALF = 0.8D;
   public static final double SPHERE_WET = 0.8D;
   public static final int ORB_MOST = 200;
   public static final double MARK_GRID = 0.8D;
   public static final double MARK_LOW = 1.8D;
   public static final int GHOST_LIFE = 9;
   public static final double GHOST_A = 0.55D;
   public static final long QUIET_NS = 2000000000L;
   public static final double QUIET_R = 0.5D;
   public static final double FEET_AIR = 1.0D;
   public static final double WAVE_AIR = 3.0D;
   public static final double BLAST_AIR = 4.5D;
   public static final double SPHERE_WIDE = 0.8D;
   public static final double SPHERE_TALL = 1.6D;
   public static final double GATHER_BUILD = 6.0D;
   public static final double GATHER_WAIT = 5.0D;
   public static final double CONTRAIL_GRACE = 5.0D;
   public static final double HIT_BACK = 0.15D;
   static final int K_IGNITE = 0;
   static final int K_HURL = 1;
   static final int K_BLAST = 2;
   static final int K_GATHER = 3;
   static final int K_BURST = 4;
   static final int K_LAUNCH = 5;
   static final int K_CONTRAIL = 6;
   static final int K_LAND = 7;
   static final Map<Integer, ClientPassEd.Ed> ED = new HashMap();
   static final List<ClientPassEd.Wave> WAVES = new ArrayList();
   static final Map<Integer, ClientPassEd.Orb> ORBS = new HashMap();
   static final List<ClientPassEd.Fx> FX = new ArrayList();
   static final List<ClientPassEd.Ghost> GHOSTS = new ArrayList();
   static final List<ClientPassEd.Quiet> QUIET = new ArrayList();
   static double glare;
   static double[] glareRgb = PassFx.rgb(16726586);
   static double glareFade = 0.1D;

   private ClientPassEd() {
   }

   static void clear() {
      for(ClientPassEd.Ed s : ED.values()) {
         halt(s);
      }

      ED.clear();
      WAVES.clear();
      ORBS.clear();
      FX.clear();
      GHOSTS.clear();
      QUIET.clear();
      glare = 0.0D;
   }

   static boolean empty() {
      return ED.isEmpty() && WAVES.isEmpty() && ORBS.isEmpty() && FX.isEmpty() && GHOSTS.isEmpty();
   }

   static void halt(ClientPassEd.Ed s) {
      if (s.whine != null) {
         s.whine.halt();
         s.whine = null;
      }

      if (s.plasma != null) {
         s.plasma.halt();
         s.plasma = null;
      }

   }

   static PassBridge.Holder holder(AbstractClientPlayer p) {
      return PassBridge.holder(p, ED_POWERS);
   }

   static int auraOf(AbstractClientPlayer p) {
      int c = PassBridge.color(p.getUUID());
      return c != -1 && (c & 16777215) != 16777215 ? c & 16777215 : 16726586;
   }

   static PassCast.Pal pal(int color, boolean copy) {
      return PassCast.pal(color == 16777215 ? 16726586 : color, copy);
   }

   static double[] up(double[] p, double dy) {
      return new double[]{p[0], p[1] + dy, p[2]};
   }

   static double[] onGround(ClientLevel level, double[] p, double most) {
      double gy = ClientPass.groundAt(level, p[0], p[2], p[1] + 0.5D);
      return p[1] - gy <= most && gy <= p[1] + 0.6D ? new double[]{p[0], gy, p[2]} : null;
   }

   static ClientPassEd.Fx grounded(ClientLevel level, int kind, int caster, double[] at, double[] vec, boolean copy, int color, double now, double life) {
      double[] g = onGround(level, at, 1.0D);
      ClientPassEd.Fx f = new ClientPassEd.Fx(kind, caster, g != null ? g : at, vec, copy, color, now, life);
      f.air = g == null;
      f.lift = g != null ? Math.max(0.0D, at[1] - g[1]) : 0.0D;
      return f;
   }

   static void gone(int id) {
      ORBS.remove(id);
   }

   static void glare(double[] at, double k, double reach, double[] rgb, double fade) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.gameRenderer != null && at != null) {
         Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
         double d = cam.distanceTo(new Vec3(at[0], at[1], at[2]));
         double near = reach * 0.25D;
         double fall = d <= near ? 1.0D : Math.max(0.0D, 1.0D - (d - near) / (reach - near));
         double v = Math.min(1.2D, k * fall);
         if (v > 0.01D && v >= glare) {
            glare = v;
            glareRgb = rgb;
            glareFade = fade;
         }

      }
   }

   static void quietAt(double[] c) {
      long now = System.nanoTime();
      QUIET.removeIf((q) -> now - q.at > 2000000000L);
      QUIET.add(new ClientPassEd.Quiet(c[0], c[1], c[2], now));
   }

   public static boolean quiet(double x, double y, double z) {
      long now = System.nanoTime();
      Iterator<ClientPassEd.Quiet> it = QUIET.iterator();

      while(it.hasNext()) {
         ClientPassEd.Quiet q = (ClientPassEd.Quiet)it.next();
         if (now - q.at > 2000000000L) {
            it.remove();
         } else {
            double dx = q.x - x;
            double dy = q.y - y;
            double dz = q.z - z;
            if (dx * dx + dy * dy + dz * dz <= 0.25D) {
               it.remove();
               return true;
            }
         }
      }

      return false;
   }

   public static float eyeHeat(AbstractClientPlayer p, float pt) {
      ClientPassEd.Ed s = (ClientPassEd.Ed)ED.get(p.getId());
      ClientLevel level = ClientPass.where;
      if (s != null && s.on && !Double.isNaN(s.onSince) && !s.copy && level != null && p.level() == level) {
         double on = (double)((float)level.getGameTime() + pt) - s.onSince;
         return (float)(0.8D * PassMath.clamp01(on / 6.0D));
      } else {
         return 0.0F;
      }
   }

   static void apply(PassFxPacket m, ClientLevel level, double now) {
      double[] at = m.at();
      boolean copy = m.has(1);
      PassCast.Pal p = pal(m.color, copy);
      if (m.op == 6) {
         int life = m.target > 0 && m.target <= 200 ? m.target : 100;
         double[] vel = m.vec();
         ClientPassEd.Orb was = (ClientPassEd.Orb)ORBS.get(m.aux);
         if (m.has(32) && was != null) {
            was.pos = at;
            was.vel = vel;
            was.floats = m.has(16);
            was.life = was.ticks + life;
            return;
         }

         ORBS.put(m.aux, new ClientPassEd.Orb(copy, m.color, life, at, vel, m.has(16)));
         if (m.caster != 0) {
            double[] dir = PassMath.len(vel) > 1.0E-6D ? PassMath.norm(vel) : PassMath.v(0.0D, 0.0D, 1.0D);
            FX.add(new ClientPassEd.Fx(1, m.caster, at, dir, copy, m.color, now, 14.0D));
            Entity c = level.getEntity(m.caster);
            double[] from = c != null ? ClientPass.a(c.position()) : at;
            if (ClientPass.near(from)) {
               ClientPass.sound("ed_hurl", from, 1.5F);
            }

            ClientPass.kick(at, now, 0.35D, 24.0D, 6.0D);
         }
      } else if (m.op == 7) {
         ORBS.remove(m.aux);
         double[] c = up(at, 0.8D);
         ClientPassEd.Fx f = new ClientPassEd.Fx(2, m.caster, c, m.vec(), copy, m.color, now, 90.0D);
         f.ground = ClientPass.groundBelow(level, c[0], c[2], c[1] + 0.3D);
         if (c[1] - f.ground > 4.5D) {
            f.air = true;
            f.ground = c[1] - 0.8D;
         }

         FX.add(f);
         if (ClientPass.near(c)) {
            ClientPass.sound("ed_blast", c, 4.0F);
         }

         glare(c, 0.45D, 64.0D, p.main, 0.09D);
         ClientPass.kick(c, now, 1.0D, 64.0D, 16.0D);
      } else if (m.op == 8) {
         FX.add(new ClientPassEd.Fx(3, m.caster, at, (double[])null, copy, m.color, now, 7.0D));
         if (ClientPass.near(at)) {
            ClientPass.sound("ed_gather", at, 1.5F);
         }
      } else if (m.op == 9) {
         FX.removeIf((x) -> x.kind == 3 && x.caster == m.caster);
         FX.add(grounded(level, 4, m.caster, at, (double[])null, copy, m.color, now, 70.0D));
         if (ClientPass.near(at)) {
            ClientPass.sound("ed_burst", at, 4.0F);
         }

         glare(at, 0.55D, 72.0D, p.main, 0.09D);
         ClientPass.kick(at, now, 1.3D, 72.0D, 18.0D);
      } else if (m.op == 10) {
         double[] dir = m.vec();
         dir = PassMath.len(dir) > 1.0E-6D ? PassMath.norm(dir) : PassMath.v(0.0D, 1.0D, 0.0D);
         FX.add(grounded(level, 5, m.caster, at, dir, copy, m.color, now, 40.0D));
         FX.add(new ClientPassEd.Fx(6, m.caster, at, dir, copy, m.color, now, 60.0D));
         if (ClientPass.near(at)) {
            ClientPass.sound("ed_propel", at, 2.5F);
         }

         glare(at, 0.3D, 48.0D, p.main, 0.1D);
         ClientPass.kick(at, now, 0.8D, 48.0D, 12.0D);
      } else if (m.op == 11) {
         FX.add(grounded(level, 7, m.caster, at, (double[])null, copy, m.color, now, 40.0D));
         if (ClientPass.near(at)) {
            ClientPass.sound("ed_blast", at, 0.35F);
         }

         ClientPass.kick(at, now, 0.5D, 32.0D, 8.0D);
      }

   }

   static void tick(Minecraft mc, ClientLevel level, double now) {
      tickPlayers(mc, level, now);
      Iterator<Map.Entry<Integer, ClientPassEd.Orb>> it = ORBS.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<Integer, ClientPassEd.Orb> en = (Map.Entry)it.next();
         ClientPassEd.Orb o = (ClientPassEd.Orb)en.getValue();
         Entity e = level.getEntity(en.getKey());
         o.prev = o.pos;
         if (e != null && !e.isRemoved()) {
            o.pos = ClientPass.a(e.position());
            o.vel = ClientPass.a(e.getDeltaMovement());
         } else {
            boolean wet = ClientPass.wet(level, o.pos, 0.8D, 1.6D);
            o.pos = PassMath.add(o.pos, o.vel);
            o.vel = PassMath.mul(o.vel, wet ? 0.8D : 0.99D);
            if (!o.floats) {
               o.vel = PassMath.v(o.vel[0], o.vel[1] - 0.01D, o.vel[2]);
            }
         }

         o.hist.add(up(o.pos, 0.8D));
         if (o.hist.size() > 12) {
            o.hist.remove(0);
         }

         ++o.ticks;
         if (o.ticks >= o.life) {
            it.remove();
         }
      }

      var it1 = FX.iterator();

      while(it1.hasNext()) {
         ClientPassEd.Fx f = (ClientPassEd.Fx)it1.next();
         double age = now - f.born;
         if (f.kind == 6) {
            Entity e = level.getEntity(f.caster);
            if (e != null) {
               if (age > 5.0D && e.onGround()) {
                  f.landed = true;
               }

               if (age < 34.0D && !f.landed) {
                  f.hist.add(up(ClientPass.a(e.position()), 0.9D));
               }

               if ((long)age % 3L == 1L && age < 24.0D && !e.onGround() && !e.isInvisible()) {
                  PassBody b = PassBodies.last(e);
                  if (b != null && b.posed()) {
                     GHOSTS.add(new ClientPassEd.Ghost(b, now, pal(f.color, f.copy).main, f.copy));
                  }
               }
            }
         }

         if (age > f.life + 1.0D) {
            it1.remove();
         }
      }

      var it2 = WAVES.iterator();

      while(it2.hasNext()) {
         ClientPassEd.Wave w = (ClientPassEd.Wave)it2.next();
         if (w.end >= 0.0D && now - w.end > 71.0D) {
            it2.remove();
         }
      }

      GHOSTS.removeIf((g) -> now - g.born > 10.0D);
      glare = Math.max(0.0D, glare - glareFade);
   }

   static void tickPlayers(Minecraft mc, ClientLevel level, double now) {
      Set<Integer> here = new HashSet();

      for(AbstractClientPlayer p : level.players()) {
         PassBridge.Holder h = holder(p);
         ClientPassEd.Ed s = (ClientPassEd.Ed)ED.get(p.getId());
         if (h != null || s != null && !idle(s)) {
            boolean first = s == null;
            if (first) {
               s = new ClientPassEd.Ed(p.getId());
               ED.put(p.getId(), s);
            }

            here.add(p.getId());
            s.seen = now;
            boolean on = h != null && !p.isSpectator() && ClientPass.enabled(h, "power_toggle");
            boolean lit = on && ClientPass.unveiled(h);
            String path = h == null ? "" : ClientPass.pathOf(h);
            s.copy = path.endsWith("_am") || PassBridge.isAM(p.getUUID());
            s.color = auraOf(p);
            double[] feet = ClientPass.a(p.position());
            if (first) {
               s.onSince = lit ? now - 20.0D : Double.NaN;
            } else if (lit && !s.lit) {
               s.onSince = now;
               if (!s.copy) {
                  FX.add(new ClientPassEd.Fx(0, p.getId(), feet, (double[])null, false, s.color, now, 24.0D));
                  if (ClientPass.near(feet)) {
                     ClientPass.sound("ed_on", feet, 1.5F);
                  }
               }
            } else if (!lit && s.lit) {
               s.onSince = Double.NaN;
            }

            s.on = on;
            s.lit = lit;
            boolean beam = on && ClientPass.enabled(h, "long_range_beam");
            if (beam && !s.beaming) {
               s.beamStart = now;
               s.beamEnd = -1.0D;
               s.beamValue = 0.0D;
               if (!first && ClientPass.near(feet)) {
                  ClientPass.sound("ed_beam", feet, 2.0F);
               }
            } else if (!beam && s.beaming) {
               s.beamEnd = now;
               if (ClientPass.near(feet)) {
                  ClientPass.sound("ed_beam_end", feet, 1.0F);
               }
            }

            s.beaming = beam;
            if (s.beamStart >= 0.0D) {
               s.beamValue = s.beamEnd < 0.0D ? Math.min(1.0D, s.beamValue + 0.5D) : Math.max(0.0D, s.beamValue - 0.5D);
               if (s.beamEnd >= 0.0D && s.beamValue <= 0.0D) {
                  s.beamStart = -1.0D;
               }
            }

            boolean hold = h != null && ClientPass.enabled(h, "wave");
            boolean run = hold && ClientPass.enabled(h, "wave_run");
            tickWave(level, p, s, hold, run, feet, now, first);
            loops(mc, p, s, feet, now);
         } else if (s != null) {
            halt(s);
            ED.remove(p.getId());
         }
      }

      Iterator<Map.Entry<Integer, ClientPassEd.Ed>> it = ED.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<Integer, ClientPassEd.Ed> e = (Map.Entry)it.next();
         ClientPassEd.Ed s = (ClientPassEd.Ed)e.getValue();
         if (!here.contains(e.getKey())) {
            if (s.beaming) {
               s.beamEnd = now;
            }

            if (s.wave != null) {
               s.wave.end = now;
               s.wave = null;
            }

            s.on = false;
            s.lit = false;
            s.beaming = false;
            s.beamStart = -1.0D;
            halt(s);
            if (now - s.seen > 40.0D) {
               it.remove();
            }
         }
      }

   }

   static boolean idle(ClientPassEd.Ed s) {
      return !s.on && s.beamStart < 0.0D && s.wave == null && !ClientPass.alive(s.whine) && !ClientPass.alive(s.plasma);
   }

   static void tickWave(ClientLevel level, AbstractClientPlayer p, ClientPassEd.Ed s, boolean hold, boolean run, double[] feet, double now, boolean first) {
      ClientPassEd.Wave w = s.wave;
      if (hold && w == null) {
         w = new ClientPassEd.Wave(p.getId(), now, s.copy, s.color);
         s.wave = w;
         s.last = w;
         WAVES.add(w);
         if (!first && !run && ClientPass.near(feet)) {
            ClientPass.sound("ed_wave_charge", feet, 2.0F);
         }
      }

      if (w != null && run && w.start < 0.0D) {
         w.start = now;
         if (!first) {
            if (ClientPass.near(feet)) {
               ClientPass.sound("ed_wave", feet, 4.0F);
            }

            glare(feet, 0.45D, 72.0D, pal(w.color, w.copy).main, 0.08D);
            ClientPass.kick(feet, now, 1.4D, 72.0D, 18.0D);
         }
      }

      if (w != null && !hold) {
         w.end = now;
         if (ClientPass.near(feet)) {
            if (w.start >= 0.0D) {
               ClientPass.sound("ed_wave_end", feet, 1.5F);
            } else {
               ClientPass.sound("ed_beam_end", feet, 0.6F);
            }
         }

         s.wave = null;
      } else if (w != null && w.start >= 0.0D) {
         w.value = Math.min(1.0D, w.value + 0.25D);
         long since = (long)(now - w.start);
         if (since % 6L == 5L) {
            ClientPass.kick(feet, now, 0.35D, 40.0D, 6.0D);
         }

         if (since % 2L == 0L) {
            lay(level, p, w, now);
         }
      }

      ClientPassEd.Wave l = s.last;
      if (l != null && l.end >= 0.0D) {
         l.value = Math.max(0.0D, l.value - 0.34D);
         if (l.value <= 0.0D || l.start < 0.0D) {
            s.last = null;
         }
      }

   }

   static double[] blocks(ClientLevel level, Entity p, Vec3 eye, Vec3 dir, double range) {
      BlockHitResult bh = level.clip(new ClipContext(eye, eye.add(dir.scale(range)), Block.COLLIDER, Fluid.NONE, p));
      return bh != null && bh.getType() != Type.MISS ? new double[]{bh.getLocation().distanceTo(eye), 0.0D} : new double[]{range, 1.0D};
   }

   static void lay(ClientLevel level, AbstractClientPlayer p, ClientPassEd.Wave w, double now) {
      Vec3 eye = p.getEyePosition(1.0F);
      Vec3 dir = p.getViewVector(1.0F);
      double reach = blocks(level, p, eye, dir, 32.0D)[0] * w.value;
      int n = (int)Math.ceil(reach / 0.8D);
      double[] e = ClientPass.a(eye);
      double[] d = ClientPass.a(dir);

      for(int k = 1; k <= n; ++k) {
         double[] q = PassMath.add(e, PassMath.mul(d, reach * (double)k / (double)n));
         double gy = ClientPass.groundBelow(level, q[0], q[2], q[1]);
         if (!(q[1] - gy >= 1.8D) && w.marked.add(cell(q[0], q[2]))) {
            w.marks.add(new double[]{q[0], gy, q[2], now, 0.85D + 0.4D * PassMath.h(now, (double)k, 3.0D), PassMath.h(now, (double)k, 4.0D) * 6.283D});
         }
      }

      while(w.marks.size() > 160) {
         double[] old = (double[])w.marks.remove(0);
         w.marked.remove(cell(old[0], old[2]));
      }

   }

   static long cell(double x, double z) {
      return Math.round(x / 0.8D) << 32 ^ Math.round(z / 0.8D) & 4294967295L;
   }

   static void loops(Minecraft mc, AbstractClientPlayer p, ClientPassEd.Ed s, double[] feet, double now) {
      boolean beamOn = s.beamStart >= 0.0D && s.beamEnd < 0.0D;
      boolean waveOn = s.wave != null && s.wave.start >= 0.0D;
      boolean heard = ClientPass.near(feet);
      s.whine = loop(mc, p, s.whine, "unrp:pass_whine", beamOn && heard ? 1.0F : 0.0F, now, s, true);
      s.plasma = loop(mc, p, s.plasma, "unrp:pass_plasma", waveOn && heard ? 2.0F : 0.0F, now, s, false);
   }

   static ClientPass.Loop loop(Minecraft mc, AbstractClientPlayer p, ClientPass.Loop l, String sound, float want, double now, ClientPassEd.Ed s, boolean whine) {
      if (want > 0.0F) {
         double at = whine ? s.whineAt : s.plasmaAt;
         if (l == null || l.isStopped() || !ClientPass.alive(l) && now - at >= 20.0D) {
            if (l != null) {
               l.halt();
            }

            l = new ClientPass.Loop(PassBridge.sound(sound), p.getId(), p, want);
            if (whine) {
               s.whineAt = now;
            } else {
               s.plasmaAt = now;
            }

            mc.getSoundManager().play(l);
         }

         l.want = want;
         return l;
      } else {
         if (l != null) {
            l.want = 0.0F;
            if (!ClientPass.alive(l)) {
               l.halt();
               return null;
            }
         }

         return l;
      }
   }

   static void want(Set<Integer> w) {
      for(ClientPassEd.Ed s : ED.values()) {
         if (s.on || s.beamStart >= 0.0D || s.wave != null) {
            w.add(s.id);
         }
      }

      for(ClientPassEd.Wave v : WAVES) {
         w.add(v.caster);
      }

      for(ClientPassEd.Fx f : FX) {
         if (f.caster != 0) {
            w.add(f.caster);
         }
      }

   }

   static void draw(PassQuads q, ClientLevel level, double t, float pt, double[] cam, double r) {
      for(ClientPassEd.Ed s : ED.values()) {
         Entity e = level.getEntity(s.id);
         if (e instanceof AbstractClientPlayer p) {
            if (!p.isRemoved() && !(p.distanceToSqr(cam[0], cam[1], cam[2]) > r * r)) {
               drawPlayer(q, level, p, s, t, pt);
            }
         }
      }

      for(ClientPassEd.Wave w : WAVES) {
         Entity e = level.getEntity(w.caster);
         if (e instanceof AbstractClientPlayer p) {
            if (!p.isRemoved() && p.distanceToSqr(cam[0], cam[1], cam[2]) <= r * r) {
               drawWave(q, level, p, w, t, pt);
               continue;
            }
         }

         if (!w.marks.isEmpty()) {
            drawMarks(q, w, t, cam, r);
         }
      }

      for(ClientPassEd.Orb o : ORBS.values()) {
         if (ClientPass.near(o.pos)) {
            drawOrb(q, level, o, t, pt);
         }
      }

      for(ClientPassEd.Fx f : FX) {
         if (ClientPass.near(f.at)) {
            drawFx(q, level, f, t, pt);
         }
      }

      for(ClientPassEd.Ghost g : GHOSTS) {
         double k = 1.0D - (t - g.born) / 9.0D;
         if (k > 0.0D && ClientPass.near(g.body.pos)) {
            q.dim = g.copy ? PassCast.SMOKE_DARK : null;
            PassCast.ghost(q, g.body, g.rgb, 0.55D * k * k);
         }
      }

      q.dim = null;
   }

   static void drawPlayer(PassQuads q, ClientLevel level, AbstractClientPlayer p, ClientPassEd.Ed s, double t, float pt) {
      if (!p.isDeadOrDying()) {
         PassCast.Pal pal = pal(s.color, s.copy);
         boolean fp = ClientPass.firstSelf(p);
         boolean shown = !p.isInvisible() && !p.isSpectator();
         PassBody body = PassBodies.get(p, pt);
         double[][] hands = ClientPass.hands(p, pt, body);
         double[] dim = s.copy ? PassCast.SMOKE_DARK : null;
         boolean beamOn = s.beamStart >= 0.0D && s.beamEnd < 0.0D;
         boolean waveOn = s.wave != null;
         if (s.on && !Double.isNaN(s.onSince) && shown) {
            double k = PassMath.clamp01((t - s.onSince) / 6.0D);
            double flare = !beamOn && !waveOn ? 0.0D : 1.0D;
            if (!fp && body.posed() && s.copy && !ClientPassAm.eyes(p, k, flare)) {
               PassCast.eyesTrailer(q, body, PassCast.EMBER, k, flare);
            }

            if (!s.copy) {
               PassBridge.passFlare(p.getId(), flare, t);
            }

            if (!beamOn && !waveOn && s.last == null) {
               q.dim = dim;
               PassEd.hands(q, pal, hands, t, k);
               q.dim = null;
            }
         }

         if (s.beamStart >= 0.0D) {
            Vec3 eye = p.getEyePosition(pt);
            Vec3 dir = p.getViewVector(pt);
            double[] hit = ClientPass.beamHit(level, p, eye, dir, 32.0D);
            double[] ea = ClientPass.a(eye);
            double[] da = ClientPass.a(dir);
            double[] cut = ClientPassBr.cut(level, ea, da, hit, 32.0D, pt, t);
            double reach = cut != null ? cut[0] : hit[0];
            PassFx.Ground g = (x, z, fromY) -> ClientPass.groundBelow(level, x, z, fromY);
            double[] l = ClientPass.light(level, PassMath.add(ea, PassMath.mul(da, Math.max(0.0D, reach - 0.15D))), pt);
            q.dim = dim;
            PassEd.beam(q, pal, g, l, s.beamStart, s.beamEnd, s.beamValue, reach, cut == null && hit[1] > 0.5D, cut != null ? new double[]{cut[1], cut[2], cut[3]} : null, ea, da, t, fp, hands, body);
            q.dim = null;
         }

      }
   }

   static void drawWave(PassQuads q, ClientLevel level, AbstractClientPlayer p, ClientPassEd.Wave w, double t, float pt) {
      PassCast.Pal pal = pal(w.color, w.copy);
      boolean fp = ClientPass.firstSelf(p);
      PassBody body = PassBodies.get(p, pt);
      double[][] hands = ClientPass.hands(p, pt, body);
      Vec3 eye = p.getEyePosition(pt);
      Vec3 dir = p.getViewVector(pt);
      double[] hit = blocks(level, p, eye, dir, 32.0D);
      double[] feet = ClientPass.a(p.getPosition(pt));
      double[] under = onGround(level, feet, 3.0D);
      double[] l = ClientPass.light(level, up(feet, 1.0D), pt);
      PassFx.Ground g = (x, z, fromY) -> ClientPass.groundBelow(level, x, z, fromY);
      q.dim = w.copy ? PassCast.SMOKE_DARK : null;
      PassEd.wave(q, pal, g, l, w.press, w.start, w.end, (double[][])w.marks.toArray(new double[0][]), hit[0], hit[1] > 0.5D, (double[])null, ClientPass.a(eye), ClientPass.a(dir), t, fp, hands, body, under != null ? under : feet, under == null);
      q.dim = null;
   }

   static void drawMarks(PassQuads q, ClientPassEd.Wave w, double t, double[] cam, double r) {
      List<double[]> near = new ArrayList();

      for(double[] mk : w.marks) {
         double dx = mk[0] - cam[0];
         double dy = mk[1] - cam[1];
         double dz = mk[2] - cam[2];
         if (dx * dx + dy * dy + dz * dz <= r * r) {
            near.add(mk);
         }
      }

      q.dim = w.copy ? PassCast.SMOKE_DARK : null;
      PassEd.waveMarks(q, (double[][])near.toArray(new double[0][]), t, w.end >= 0.0D ? t - w.end : -1.0D);
      q.dim = null;
   }

   static void drawOrb(PassQuads q, ClientLevel level, ClientPassEd.Orb o, double t, float pt) {
      PassCast.Pal pal = pal(o.color, o.copy);
      double[] p = up(PassMath.lerp(o.prev, o.pos, (double)pt), 0.8D);
      PassFx.Ground g = (x, z, fromY) -> ClientPass.groundBelow(level, x, z, fromY);
      q.dim = o.copy ? PassCast.SMOKE_DARK : null;
      PassEd.orb(q, pal, g, p, (double[][])o.hist.toArray(new double[0][]), t);
      q.dim = null;
   }

   static void drawFx(PassQuads q, ClientLevel level, ClientPassEd.Fx f, double t, float pt) {
      double age = t - f.born;
      if (!(age < 0.0D) && !(age > f.life)) {
         PassCast.Pal pal = pal(f.color, f.copy);
         Entity e = f.caster != 0 ? level.getEntity(f.caster) : null;
         q.dim = f.copy ? PassCast.SMOKE_DARK : null;
         q.air = f.air;
         if (f.kind == 0) {
            if (e instanceof AbstractClientPlayer) {
               AbstractClientPlayer p = (AbstractClientPlayer)e;
               double[] feet = ClientPass.a(p.getPosition(pt));
               double[] g = onGround(level, feet, 1.0D);
               q.air = g == null;
               PassEd.ignite(q, pal, g != null ? g : feet, ClientPass.hands(p, pt, PassBodies.get(p, pt)), age);
            }
         } else if (f.kind == 1) {
            if (e != null) {
               PassEd.hurl(q, pal, ClientPass.handOf(e, pt), f.at, f.vec, age);
            }
         } else if (f.kind == 2) {
            PassFx.Ground g = (x, z, fromY) -> ClientPass.groundBelow(level, x, z, fromY);
            PassEd.blast(q, pal, g, ClientPass.light(level, f.at, pt), f.at, f.ground, age);
         } else if (f.kind == 3) {
            double[] feet = e != null ? ClientPass.a(e.getPosition(pt)) : f.at;
            double[] g = onGround(level, feet, 1.0D);
            q.air = g == null;
            PassEd.gather(q, pal, g != null ? g : feet, g != null ? Math.max(0.0D, feet[1] - g[1]) : 0.0D, age * 6.0D / 5.0D);
         } else if (f.kind == 4) {
            PassEd.burst(q, pal, ClientPass.light(level, up(f.at, 1.0D + f.lift), pt), f.at, f.lift, age);
         } else if (f.kind == 5) {
            double[] feet = e != null ? ClientPass.a(e.getPosition(pt)) : null;
            PassEd.launch(q, pal, ClientPass.light(level, up(f.at, 1.0D + f.lift), pt), f.at, f.lift, f.vec, feet, age);
         } else if (f.kind == 6) {
            if (f.hist.size() > 1) {
               PassEd.contrail(q, pal, ClientPass.light(level, (double[])f.hist.get(f.hist.size() - 1), pt), (double[][])f.hist.toArray(new double[0][]), age);
            }
         } else if (f.kind == 7) {
            PassFx.Ground g = (x, z, fromY) -> ClientPass.groundBelow(level, x, z, fromY);
            PassEd.land(q, pal, g, ClientPass.light(level, f.at, pt), f.at, age);
         }

         q.dim = null;
         q.air = false;
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void onFogColor(ViewportEvent.ComputeFogColor event) {
      double k = Math.min(0.7D, glare * 0.65D);
      if (!(k <= 0.01D) && PassBridge.screenEffects() && ClientPass.flashes()) {
         event.setRed((float)((double)event.getRed() + (glareRgb[0] - (double)event.getRed()) * k));
         event.setGreen((float)((double)event.getGreen() + (glareRgb[1] - (double)event.getGreen()) * k));
         event.setBlue((float)((double)event.getBlue() + (glareRgb[2] - (double)event.getBlue()) * k));
      }
   }

   static final class Ed {
      final int id;
      boolean on;
      boolean lit;
      double onSince = Double.NaN;
      boolean copy;
      int color = 16726586;
      double seen;
      boolean beaming;
      double beamStart = -1.0D;
      double beamEnd = -1.0D;
      double beamValue;
      ClientPassEd.Wave wave;
      ClientPassEd.Wave last;
      ClientPass.Loop whine;
      double whineAt = -1.0E9D;
      ClientPass.Loop plasma;
      double plasmaAt = -1.0E9D;

      Ed(int id) {
         this.id = id;
      }
   }

   static final class Fx {
      final int kind;
      final int caster;
      final double[] at;
      final double[] vec;
      final boolean copy;
      final int color;
      final double born;
      final double life;
      double ground = Double.NaN;
      double lift;
      boolean air;
      boolean landed;
      final List<double[]> hist = new ArrayList();

      Fx(int kind, int caster, double[] at, double[] vec, boolean copy, int color, double born, double life) {
         this.kind = kind;
         this.caster = caster;
         this.at = at;
         this.vec = vec;
         this.copy = copy;
         this.color = color;
         this.born = born;
         this.life = life;
      }
   }

   static final class Ghost {
      final PassBody body;
      final double born;
      final double[] rgb;
      final boolean copy;

      Ghost(PassBody body, double born, double[] rgb, boolean copy) {
         this.body = body;
         this.born = born;
         this.rgb = rgb;
         this.copy = copy;
      }
   }

   static final class Orb {
      final boolean copy;
      final int color;
      int life;
      boolean floats;
      double[] pos;
      double[] prev;
      double[] vel;
      final List<double[]> hist = new ArrayList();
      int ticks;

      Orb(boolean copy, int color, int life, double[] pos, double[] vel, boolean floats) {
         this.copy = copy;
         this.color = color;
         this.life = life;
         this.floats = floats;
         this.pos = pos;
         this.prev = pos;
         this.vel = vel;
         this.hist.add(PassMath.add(pos, PassMath.v(0.0D, 0.8D, 0.0D)));
      }
   }

   static final class Quiet {
      final double x;
      final double y;
      final double z;
      final long at;

      Quiet(double x, double y, double z, long at) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.at = at;
      }
   }

   static final class Wave {
      final int caster;
      final double press;
      final boolean copy;
      final int color;
      double start = -1.0D;
      double end = -1.0D;
      double value;
      final List<double[]> marks = new ArrayList();
      final Set<Long> marked = new HashSet();

      Wave(int caster, double press, boolean copy, int color) {
         this.caster = caster;
         this.press = press;
         this.copy = copy;
         this.color = color;
      }
   }
}
