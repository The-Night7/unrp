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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class ClientPassPd {
   public static final String[] PD_POWERS = new String[]{"predator", "predator_am"};
   public static final int TOLD_KEEP = 10;
   public static final double LATE = 100.0D;
   public static final double FIRST_ON = 20.0D;
   public static final int KEEP = 40;
   public static final int EYES_IN = 6;
   public static final double REACH = 24.0D;
   public static final double REACH_LEAST = 4.0D;
   public static final double REACH_MOST = 64.0D;
   public static final double HEART_MOST = 32.0D;
   public static final int PREY_KEEP = 46;
   public static final int SENSED_KEEP = 16;
   public static final int SENSE_EVERY = 5;
   public static final int BEATS_MOST = 256;
   public static final int GHOSTS_MOST = 128;
   public static final int FX_MOST = 256;
   public static final float LOCK_VOL = 1.0F;
   public static final float PULSE_VOL = 0.5F;
   public static final float BEAT_VOL = 1.0F;
   public static final float ROCK_VOL = 0.8F;
   public static final double EYE_LINE_AHEAD = 0.6D;
   public static final double EYE_LINE_DROP = 0.22D;
   static final int K_LOCKON = 0;
   static final int K_RUSH = 1;
   static final int K_SLASH = 2;
   static final int K_WAVE = 3;
   static final int K_ROAR = 4;
   static final Map<Integer, ClientPassPd.Pd> PD = new HashMap();
   static final List<ClientPassPd.Fx> FX = new ArrayList();
   static final List<ClientPassPd.Beat> BEATS = new ArrayList();
   static final List<ClientPassPd.Ghost> GHOSTS = new ArrayList();
   static final Map<Integer, ClientPassPd.Tint> TINTS = new HashMap();

   private ClientPassPd() {
   }

   static void clear() {
      PD.clear();
      FX.clear();
      BEATS.clear();
      GHOSTS.clear();
      TINTS.clear();
   }

   static boolean empty() {
      return PD.isEmpty() && FX.isEmpty() && BEATS.isEmpty() && GHOSTS.isEmpty();
   }

   static PassBridge.Holder holder(AbstractClientPlayer p) {
      return PassBridge.holder(p, PD_POWERS);
   }

   public static boolean marks(AbstractClientPlayer p, int target) {
      ClientPassPd.Pd s = p == null ? null : (ClientPassPd.Pd)PD.get(p.getId());
      return s != null && s.known && s.on && target >= 0;
   }

   static void early(PassFxPacket m, ClientLevel level) {
      if (m.caster != 0 && (m.op == 40 || m.op == 34)) {
         double now = (double)level.getGameTime();
         ClientPassPd.Pd s = pd(m.caster, now);
         if (m.op == 40) {
            s.roarTold = now;
         } else {
            s.lock = m.target;
            s.lockT = now;
            Entity var6 = level.getEntity(m.caster);
            if (var6 instanceof AbstractClientPlayer) {
               AbstractClientPlayer cp = (AbstractClientPlayer)var6;
               s.told = PassBridge.lockedId(cp.getUUID());
            }

         }
      }
   }

   static boolean mine(Entity e) {
      return e != null && e == Minecraft.getInstance().player;
   }

   static boolean shows(Entity e) {
      return mine(e);
   }

   static boolean heard(Entity c) {
      return c == null || !c.isInvisible() || mine(c);
   }

   static int colorOf(AbstractClientPlayer p) {
      int c = PassBridge.color(p.getUUID());
      return c != -1 && (c & 16777215) != 16777215 ? c & 16777215 : 15242256;
   }

   static int rgb(double[] c) {
      int r = (int)Math.round(PassMath.clamp01(c[0]) * 255.0D);
      int g = (int)Math.round(PassMath.clamp01(c[1]) * 255.0D);
      int b = (int)Math.round(PassMath.clamp01(c[2]) * 255.0D);
      return r << 16 | g << 8 | b;
   }

   static ClientPassPd.Pd pd(int id, double now) {
      ClientPassPd.Pd s = (ClientPassPd.Pd)PD.get(id);
      if (s == null) {
         s = new ClientPassPd.Pd(id);
         s.seen = now;
         PD.put(id, s);
      }

      return s;
   }

   static PassCast.Pal palOf(ClientLevel level, PassFxPacket m, ClientPassPd.Pd s) {
      if (s.known) {
         return s.pal();
      } else {
         Entity c = level.getEntity(m.caster);
         if (c instanceof AbstractClientPlayer) {
            AbstractClientPlayer p = (AbstractClientPlayer)c;
            return PassPd.pal(colorOf(p), PassBridge.isAM(p.getUUID()) || m.has(1));
         } else {
            int col = m.color != 16777215 && m.color != 0 ? m.color : 15242256;
            return PassPd.pal(col, m.has(1));
         }
      }
   }

   static double[] feetOf(ClientLevel level, int id, double[] or) {
      Entity e = id != 0 ? level.getEntity(id) : null;
      return e != null && !e.isRemoved() ? ClientPass.a(e.position()) : or;
   }

   static void fx(ClientPassPd.Fx f) {
      while(FX.size() >= 256) {
         FX.remove(0);
      }

      FX.add(f);
   }

   static void beat(ClientPassPd.Beat b) {
      while(BEATS.size() >= 256) {
         BEATS.remove(0);
      }

      BEATS.add(b);
   }

   static void ghost(ClientPassPd.Ghost g) {
      while(GHOSTS.size() >= 128) {
         GHOSTS.remove(0);
      }

      GHOSTS.add(g);
   }

   static void tint(int id, double now, double[] rgb, boolean prey, int by) {
      ClientPassPd.Tint x = (ClientPassPd.Tint)TINTS.computeIfAbsent(id, (k) -> new ClientPassPd.Tint());
      if (prey) {
         x.preyUntil = now + 46.0D;
         x.prey = rgb(rgb);
      } else {
         if (x.by == by || x.sensedUntil < now || now - x.byAt > 5.0D) {
            x.sensed = rgb(rgb);
            x.by = by;
            x.byAt = now;
         }

         x.sensedUntil = now + 16.0D;
      }

   }

   static void apply(PassFxPacket m, ClientLevel level, double now) {
      if (m.caster != 0) {
         ClientPassPd.Pd s = pd(m.caster, now);
         PassCast.Pal pal = palOf(level, m, s);
         Entity caster = level.getEntity(m.caster);
         double[] feet = feetOf(level, m.caster, m.at());
         if (m.op == 34) {
            if (s.lock != m.target) {
               s.lock = m.target;
               if (caster instanceof AbstractClientPlayer) {
                  AbstractClientPlayer cp = (AbstractClientPlayer)caster;
                  s.told = PassBridge.lockedId(cp.getUUID());
               }
            }

            s.lockT = now;
            s.act = now;
            fx(new ClientPassPd.Fx(0, m.caster, m.target, m.at(), (double[])null, pal, now, 16.0D));
            if (ClientPass.near(feet)) {
               ClientPass.sound("pd_lock", feet, 1.0F);
            }
         } else if (m.op == 35) {
            if (m.v[3] > 0.0F) {
               s.reach = PassMath.clamp((double)m.v[3], 4.0D, 64.0D);
            }

            if (m.has(32)) {
               return;
            }

            s.focusTold = now;
            if (s.focus && now - s.focusSince <= 10.0D) {
               s.focusStart = now;
            }

            s.act = now;
            if (ClientPass.near(feet) && heard(caster)) {
               ClientPass.sound("pd_on", feet, 1.5F);
            }
         } else if (m.op == 36) {
            if (caster == null || caster.isRemoved()) {
               return;
            }

            double r = PassMath.clamp((double)m.aux, 1.0D, 32.0D);
            double[] heart = PassPd.heart(pal);

            for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(r), (le) -> le != caster && le.isAlive())) {
               beat(new ClientPassPd.Beat(e.getId(), m.caster, now, heart, pal.copy));
            }

            if (m.has(4) && ClientPass.near(feet) && heard(caster)) {
               ClientPass.sound("pd_pulse", feet, 1.0F);
            }
         } else if (m.op == 37) {
            s.act = now;
            if (ClientPass.near(feet)) {
               ClientPass.sound("pd_rush", feet, 1.5F);
            }

            if (m.has(8)) {
               double[] dir = PassMath.norm(m.vec());
               if (PassMath.len(dir) < 0.5D) {
                  dir = PassMath.v(1.0D, 0.0D, 0.0D);
               }

               fx(new ClientPassPd.Fx(1, m.caster, 0, m.at(), dir, pal, now, 30.0D));
               s.rushStart = now;
               s.rushHit = false;
               ClientPass.kick(m.at(), now, 0.4D, 32.0D, 6.0D);
            }
         } else if (m.op == 38) {
            s.rushHit = true;
            s.act = now;
            double[] dir = m.vec();
            dir[1] = 0.0D;
            dir = PassMath.len(dir) < 1.0E-4D ? PassMath.v(1.0D, 0.0D, 0.0D) : PassMath.norm(dir);
            fx(new ClientPassPd.Fx(2, m.caster, m.target, m.at(), dir, pal, now, 30.0D));
            double[] at = feetOf(level, m.target, m.at());
            if (ClientPass.near(at)) {
               ClientPass.sound("pd_rush_hit", at, 1.5F);
            }

            ClientPass.kick(at, now, 1.0D, 40.0D, 12.0D);
            ClientPass.flash(at, 0.3D, 48.0D, pal.main);
         } else if (m.op == 39) {
            s.act = now;
            ClientPassPd.Fx f = new ClientPassPd.Fx(3, m.caster, 0, m.at(), m.vec(), pal, now, 90.0D);
            BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
            f.path = PassPd.path((x, y, z) -> !level.getBlockState(mp.set(x, y, z)).isAir(), m.at(), m.vec());
            fx(f);
            if (ClientPass.near(feet)) {
               ClientPass.sound("pd_breaker", feet, 2.0F);
            }
         } else if (m.op == 40) {
            s.roarTold = now;
            if (s.trans) {
               s.transOn = now;
            }

            s.act = now;
            fx(new ClientPassPd.Fx(4, m.caster, 0, feet, (double[])null, pal, now + 8.0D, 40.0D));
            if (ClientPass.near(feet) && heard(caster)) {
               ClientPass.sound("pd_roar", feet, 2.0F);
            }
         }

      }
   }

   static void tick(Minecraft mc, ClientLevel level, double now) {
      tickUsers(level, now);
      Iterator<ClientPassPd.Fx> it = FX.iterator();

      while(it.hasNext()) {
         ClientPassPd.Fx f = (ClientPassPd.Fx)it.next();
         if (f.kind == 4 && !f.fired && now >= f.born) {
            f.fired = true;
            f.at = feetOf(level, f.caster, f.at);
            ClientPass.kick(f.at, now, 1.2D, 56.0D, 18.0D);
         }

         if (f.kind == 3 && f.path != null) {
            if (!f.fired && now >= f.born + 1.0D) {
               f.fired = true;
               double[] at = feetOf(level, f.caster, f.at);
               ClientPass.kick(at, now, 1.1D, 48.0D, 14.0D);
               ClientPass.flash(at, 0.2D, 48.0D, f.pal.main);
            }

            int step = (int)Math.min((double)(f.path.length - 1), now - f.born);

            for(int i = f.rocks + 1; i <= step; ++i) {
               if (i % 3 == 0 && ClientPass.near(f.path[i])) {
                  ClientPass.sound("pd_rock", f.path[i], 0.8F);
               }
            }

            f.rocks = Math.max(f.rocks, step);
         }

         if (now - f.born > f.life + 1.0D) {
            it.remove();
         }
      }

      BEATS.removeIf((b) -> now - b.born >= 24.0D);
      GHOSTS.removeIf((g) -> now - g.born > 10.0D);
      TINTS.values().removeIf((x) -> x.preyUntil < now && x.sensedUntil < now);
   }

   static void tickUsers(ClientLevel level, double now) {
      Set<Integer> here = new HashSet();

      for(AbstractClientPlayer p : level.players()) {
         PassBridge.Holder h = holder(p);
         if (h != null) {
            ClientPassPd.Pd s = pd(p.getId(), now);
            s.known = true;
            here.add(p.getId());
            s.seen = now;
            UUID u = p.getUUID();
            boolean live = !p.isSpectator() && ClientPass.enabled(h, "predator_activation");
            boolean lit = live && ClientPass.unveiled(h);
            boolean first = live && !s.seenLive;
            if (live) {
               s.seenLive = true;
            }

            s.copy = ClientPass.pathOf(h).endsWith("_am") || PassBridge.isAM(u);
            s.color = colorOf(p);
            if (lit && !s.lit) {
               s.onSince = first ? now - 20.0D : now;
            } else if (!lit) {
               s.onSince = Double.NaN;
            }

            s.on = live;
            s.lit = lit;
            boolean focus = live && ClientPass.enabled(h, "sense_focus");
            if (focus && !s.focus) {
               s.focusSince = now;
               s.focusStart = now - s.focusTold <= 10.0D ? s.focusTold : (first ? now - 20.0D : now);
               s.focusEnd = -1.0D;
            } else if (!focus && s.focus) {
               s.focusEnd = now;
            }

            s.focus = focus;
            boolean trans = live && PassBridge.isTransformed(u);
            if (trans && !s.trans) {
               s.transOn = s.transFrom(now);
            } else if (!trans) {
               s.transOn = Double.NaN;
            }

            s.trans = trans;
            int cached = live ? PassBridge.lockedId(u) : -1;
            if (cached != s.told) {
               s.told = cached;
               if (cached != s.lock) {
                  s.lock = cached;
                  s.lockT = now - 100.0D;
               }
            }

            int lock = s.lock;
            PassCast.Pal pal = s.pal();
            if (lock >= 0 && shows(p)) {
               tint(lock, now, PassPd.preyTint(pal), true, s.id);
               if (now >= s.lockT && (long)(now - s.lockT) % 20L == 6L) {
                  beat(new ClientPassPd.Beat(lock, s.id, now, pal.main, pal.copy));
               }
            }

            double[] feet = ClientPass.a(p.position());
            if (focus) {
               if ((long)now % 5L == 0L) {
                  for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(s.reach), (le) -> le != p && le.isAlive())) {
                     tint(e.getId(), now, PassPd.sensedTint(pal), false, s.id);
                  }
               }

               double since = now - s.focusStart;
               if (since > 0.0D && (long)since % 10L == 0L && heard(p) && ClientPass.near(feet)) {
                  ClientPass.sound("pd_pulse", feet, 0.5F);
               }
            }

            double rush = now - s.rushStart;
            if (rush >= 0.0D && rush < 12.0D && (long)rush % 2L == 0L && !s.rushHit && !p.isInvisible()) {
               PassBody b = PassBodies.last(p);
               if (b != null && b.posed()) {
                  ghost(new ClientPassPd.Ghost(b, now, pal.main, pal.copy));
               }
            }
         }
      }

      Iterator<Map.Entry<Integer, ClientPassPd.Pd>> it = PD.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<Integer, ClientPassPd.Pd> e = (Map.Entry)it.next();
         if (!here.contains(e.getKey())) {
            ClientPassPd.Pd s = (ClientPassPd.Pd)e.getValue();
            if (s.focus) {
               s.focusEnd = now;
            }

            s.known = false;
            s.seenLive = false;
            s.on = false;
            s.lit = false;
            s.onSince = Double.NaN;
            s.focus = false;
            s.trans = false;
            s.transOn = Double.NaN;
            s.lock = -1;
            s.told = -2;
            if (now - s.seen > 40.0D) {
               it.remove();
            }
         }
      }

   }

   static void want(Set<Integer> w) {
      for(ClientPassPd.Pd s : PD.values()) {
         if (s.on) {
            w.add(s.id);
         }
      }

      for(ClientPassPd.Fx f : FX) {
         if (f.caster != 0) {
            w.add(f.caster);
         }
      }

   }

   public static float eyeHeat(AbstractClientPlayer p, float pt) {
      ClientPassPd.Pd s = (ClientPassPd.Pd)PD.get(p.getId());
      ClientLevel level = ClientPass.where;
      return s != null && s.on && !Double.isNaN(s.onSince) && !s.copy && level != null && p.level() == level ? (float)(0.8D * PassMath.clamp01(((double)((float)level.getGameTime() + pt) - s.onSince) / 6.0D)) : 0.0F;
   }

   public static double transAge(AbstractClientPlayer p, float pt) {
      ClientPassPd.Pd s = (ClientPassPd.Pd)PD.get(p.getId());
      ClientLevel level = ClientPass.where;
      if (s != null && s.known && level != null && p.level() == level) {
         double now = (double)level.getGameTime();
         double on = s.trans && !Double.isNaN(s.transOn) ? s.transOn : s.transFrom(now);
         return now + (double)pt - on - 8.0D;
      } else {
         return 100.0D;
      }
   }

   public static int tint(Entity e) {
      if (!TINTS.isEmpty() && e != null) {
         ClientPassPd.Tint x = (ClientPassPd.Tint)TINTS.get(e.getId());
         ClientLevel level = ClientPass.where;
         if (x != null && level != null && e.level() == level && e.isCurrentlyGlowing()) {
            double now = (double)level.getGameTime();
            if (x.preyUntil >= now) {
               return x.prey;
            } else {
               return x.sensedUntil >= now ? x.sensed : -1;
            }
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   static void draw(PassQuads q, ClientLevel level, double t, float pt, double[] cam, double r) {
      PassFx.Ground ground = (x, z, fromY) -> ClientPass.groundBelow(level, x, z, fromY);

      for(ClientPassPd.Pd s : PD.values()) {
         Entity e = level.getEntity(s.id);
         if (e instanceof AbstractClientPlayer p) {
            if (!p.isRemoved() && !(p.distanceToSqr(cam[0], cam[1], cam[2]) > r * r)) {
               drawUser(q, level, p, s, t, pt, ground);
            }
         }
      }

      Minecraft mc = Minecraft.getInstance();
      int me = mc.player == null ? 0 : mc.player.getId();

      for(ClientPassPd.Beat b : BEATS) {
         LivingEntity o = ClientPass.living(level, b.target);
         if (o != null && o.isAlive() && !ClientPass.firstSelf(o) && (!o.isInvisible() || b.by == me)) {
            Vec3 at = o.getPosition(pt);
            double[] c = PassMath.v(at.x, at.y + (double)o.getBbHeight() * 0.62D, at.z);
            if (ClientPass.near(c)) {
               q.dim = b.copy ? PassCast.SMOKE_DARK : null;
               PassPd.beat(q, c, b.rgb, t - b.born);
            }
         }
      }

      q.dim = null;

      for(ClientPassPd.Fx f : FX) {
         if (f.at != null && ClientPass.near(f.at)) {
            drawFx(q, level, f, t, pt, ground);
         }
      }

      for(ClientPassPd.Ghost g : GHOSTS) {
         double k = 1.0D - (t - g.born) / 9.0D;
         if (k > 0.0D && ClientPass.near(g.body.pos)) {
            q.dim = g.copy ? PassCast.SMOKE_DARK : null;
            PassCast.ghost(q, g.body, g.rgb, 0.5D * k * k);
         }
      }

      q.dim = null;
   }

   static void drawUser(PassQuads q, ClientLevel level, AbstractClientPlayer p, ClientPassPd.Pd s, double t, float pt, PassFx.Ground ground) {
      PassCast.Pal pal = s.pal();
      if (s.lock >= 0 && s.on && shows(p)) {
         mark(q, level, p, s, pal, t, pt);
      }

      boolean shown = !p.isInvisible() && !p.isSpectator() && !p.isDeadOrDying();
      if (shown) {
         double[] feet = ClientPass.a(p.getPosition(pt));
         if (s.focusStart >= 0.0D && (s.focus || !PassPd.sonarDone(t, s.focusEnd))) {
            q.dim = pal.copy ? PassCast.SMOKE_DARK : null;
            PassPd.sonar(q, pal, PassPd.sonarAt(ground, feet), s.reach, t, s.focusStart, s.focus ? -1.0D : s.focusEnd);
            q.dim = null;
         }

         if (s.on) {
            boolean fp = ClientPass.firstSelf(p);
            boolean lit = !Double.isNaN(s.onSince);
            PassBody body = PassBodies.get(p, pt);
            double k = lit ? PassMath.clamp01((t - s.onSince) / 6.0D) : 0.0D;
            double flare = Math.max(PassMath.clamp01(1.0D - (t - s.act) / 12.0D), s.focus ? 0.8D : 0.0D);
            double[][] eyes = pal.copy ? PassCast.EYE_AT : PassBridge.eyeCenters(p);
            if (lit && !fp && body.posed() && pal.copy && !ClientPassAm.eyes(p, k, flare)) {
               PassCast.eyesTrailer(q, body, PassCast.EMBER, k, flare);
            }

            if (lit && !pal.copy) {
               PassBridge.passFlare(p.getId(), flare, t);
            }

            q.dim = pal.copy ? PassCast.SMOKE_DARK : null;
            if (lit && !fp && body.posed()) {
               PassPd.eyeTrails(q, pal, body, eyes, ClientPass.a(p.position()), PassMath.v(p.xo, p.yo, p.zo), ClientPass.a(p.getLookAngle()), s.focus);
            }

            if (s.focus) {
               double rr = s.reach + 1.0D;

               for(Map.Entry<Integer, ClientPassPd.Tint> en : TINTS.entrySet()) {
                  ClientPassPd.Tint x = (ClientPassPd.Tint)en.getValue();
                  boolean prey = en.getKey() == s.lock && x.preyUntil >= t - 1.0D;
                  if (prey || x.sensedUntil >= t - 1.0D) {
                     LivingEntity o = ClientPass.living(level, en.getKey());
                     if (o != null && o != p && o.isAlive() && o.isCurrentlyGlowing() && !ClientPass.firstSelf(o) && (prey || !(Math.abs(o.getX() - p.getX()) > rr) && !(Math.abs(o.getY() - p.getY()) > rr) && !(Math.abs(o.getZ() - p.getZ()) > rr))) {
                        PassPd.sensed(q, pal, ClientPass.mid(o, pt));
                     }
                  }
               }
            }

            if (s.trans && !Double.isNaN(s.transOn) && !fp && body.posed()) {
               double ag = t - s.transOn - 8.0D;
               PassPd.claws(q, pal, body, PassPd.clawsIn(ag), PassPd.clawsFlare(ag));
            }

            q.dim = null;
         }
      }
   }

   static void mark(PassQuads q, ClientLevel level, AbstractClientPlayer p, ClientPassPd.Pd s, PassCast.Pal pal, double t, float pt) {
      LivingEntity tg = ClientPass.living(level, s.lock);
      if (tg != null && tg.isAlive() && !ClientPass.firstSelf(tg)) {
         double[] chest = PassPd.chestFront(ClientPass.a(tg.getPosition(pt)), (double)tg.getBbHeight(), q.eye);
         if (ClientPass.near(chest)) {
            q.dim = pal.copy ? PassCast.SMOKE_DARK : null;
            PassPd.mark(q, chest, PassPd.markColor(pal), t - s.lockT, t);
            boolean fp = ClientPass.firstSelf(p);
            if (fp || !p.isInvisible()) {
               for(ClientPassPd.Fx f : FX) {
                  if (f.kind == 0 && f.caster == s.id && f.target == s.lock) {
                     double age = t - f.born;
                     if (age >= 0.0D && age < 7.0D) {
                        double[] eye = ClientPass.a(p.getEyePosition(pt));
                        if (fp) {
                           eye = PassMath.add(PassMath.add(eye, PassMath.mul(ClientPass.a(p.getViewVector(pt)), 0.6D)), PassMath.v(0.0D, -0.22D, 0.0D));
                        }

                        PassPd.lockon(q, pal, eye, chest, age);
                     }
                  }
               }
            }

            q.dim = null;
         }
      }
   }

   static void drawFx(PassQuads q, ClientLevel level, ClientPassPd.Fx f, double t, float pt, PassFx.Ground ground) {
      double age = t - f.born;
      if (!(age < 0.0D) && !(age > f.life) && f.kind != 0) {
         q.dim = f.pal.copy ? PassCast.SMOKE_DARK : null;
         if (f.kind == 1) {
            Entity c = level.getEntity(f.caster);
            boolean seen = c != null && !c.isRemoved() && !c.isInvisible() && !ClientPass.firstSelf(c);
            PassPd.rush(q, f.pal, ground, ClientPass.light(level, f.at, pt), f.at, f.dir, age, seen ? ClientPass.a(c.getEyePosition(pt)) : null);
         } else if (f.kind == 2) {
            Entity tg = level.getEntity(f.target);
            PassPd.slash(q, f.pal, ground, f.at, f.dir, f.born, age, tg == null || !ClientPass.firstSelf(tg));
         } else if (f.kind == 3 && f.path != null) {
            PassPd.wave(q, f.pal, ground, ClientPass.light(level, f.at, pt), f.path, age);
         } else if (f.kind == 4 && f.fired) {
            Entity c = level.getEntity(f.caster);
            boolean here = c != null && !c.isRemoved();
            if (!here || !c.isInvisible()) {
               double[] base = here ? ClientPass.a(c.getPosition(pt)) : f.at;
               PassPd.roar(q, f.pal, ClientPass.light(level, f.at, pt), f.at, PassMath.add(base, PassMath.v(0.0D, 1.5D, 0.0D)), age, !here || !ClientPass.firstSelf(c));
            }
         }

         q.dim = null;
      }
   }

   static final class Beat {
      final int target;
      final int by;
      final double born;
      final double[] rgb;
      final boolean copy;

      Beat(int target, int by, double born, double[] rgb, boolean copy) {
         this.target = target;
         this.by = by;
         this.born = born;
         this.rgb = rgb;
         this.copy = copy;
      }
   }

   static final class Fx {
      final int kind;
      final int caster;
      final int target;
      double[] at;
      final double[] dir;
      final PassCast.Pal pal;
      final double born;
      final double life;
      double[][] path;
      boolean fired;
      int rocks;

      Fx(int kind, int caster, int target, double[] at, double[] dir, PassCast.Pal pal, double born, double life) {
         this.kind = kind;
         this.caster = caster;
         this.target = target;
         this.at = at;
         this.dir = dir;
         this.pal = pal;
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

   static final class Pd {
      final int id;
      boolean known;
      boolean seenLive;
      boolean on;
      boolean lit;
      double onSince = Double.NaN;
      boolean copy;
      int color = 15242256;
      double act = -1.0E9D;
      boolean focus;
      double focusSince = -1.0E9D;
      double focusStart = -1.0D;
      double focusEnd = -1.0D;
      double focusTold = -1.0E9D;
      double reach = 24.0D;
      boolean trans;
      double transOn = Double.NaN;
      double roarTold = -1.0E9D;
      int lock = -1;
      int told = -2;
      double lockT = -1.0E9D;
      double rushStart = -1.0E9D;
      boolean rushHit;
      double seen;

      Pd(int id) {
         this.id = id;
      }

      PassCast.Pal pal() {
         return PassPd.pal(this.color, this.copy);
      }

      double transFrom(double now) {
         return now - this.roarTold <= 10.0D ? this.roarTold : now - 100.0D;
      }
   }

   static final class Tint {
      double preyUntil = -1.0E9D;
      double sensedUntil = -1.0E9D;
      double byAt = -1.0E9D;
      int prey;
      int sensed;
      int by;
   }
}
