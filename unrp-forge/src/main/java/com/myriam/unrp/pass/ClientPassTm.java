package com.myriam.unrp.pass;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class ClientPassTm {
   public static final String[] TM_POWERS = new String[]{"time_manipulation", "time_manipulation_am"};
   public static final double NEAR_USER = 40.0D;
   public static final int TRAIL = 10;
   public static final double MOVE_EPS = 1.0E-4D;
   public static final double STILL = 0.01D;
   public static final int HOLD_SLACK = 5;
   public static final int DELAY_LATE = 10;
   public static final int ON_KEEP = 4;
   public static final int FLOW_KEEP = 10;
   public static final int HIST_KEEP = 40;
   public static final int GRAY_RETRY = 200;
   public static final int GRAY_MOST = 64;
   public static final int ECHO_MOST = 6;
   public static final double LAYER_NEAR = 3.0D;
   public static final float TICK_ACCEL = 0.9F;
   public static final float TICK_DECEL = 0.45F;
   public static final float TICK_DELAY = 0.8F;
   public static final int TICKS_MOST = 3;
   public static final int GHOST_MOST = 256;
   public static final int ECHO_BODIES = 24;
   public static final double FIELD_MOST = 64.0D;
   static final int K_IGNITE = 0;
   static final int K_STEP = 1;
   static final int K_REWIND = 2;
   static final int K_BLINK = 3;
   static final int K_RECALL = 4;
   static final int K_FREEZE = 5;
   static final int K_STRIKE = 6;
   static final int K_DELAY = 7;
   static final Map<Integer, ClientPassTm.Tm> TM = new HashMap();
   static final List<ClientPassTm.Fx> FX = new ArrayList();
   static final Map<Integer, ClientPassTm.Mark> MARKS = new HashMap();
   static final Map<Integer, ClientPassTm.Held> HELD = new HashMap();
   static final Map<Integer, ClientPassTm.Pr> PROJ = new HashMap();
   static final Map<Integer, ClientPassTm.Hist> HIST = new HashMap();
   static final Map<Integer, ClientPassTm.Mov> MOVES = new HashMap();
   static final List<ClientPassTm.Ghost> GHOSTS = new ArrayList();
   static final Map<Integer, List<ClientPassTm.Echo>> ECHOES = new HashMap();
   static final Map<Integer, double[]> LAYERS = new HashMap();
   static final Map<Integer, Double> LAYER_AT = new HashMap();
   static final Map<Integer, ClientPass.Loop> TICKS = new HashMap();
   static final Map<Integer, Double> TICKS_AT = new HashMap();
   static final Map<ResourceLocation, ResourceLocation> GRAY = new HashMap();
   static final List<DynamicTexture> GRAY_MADE = new ArrayList();
   static final Map<ResourceLocation, Double> GRAY_FAIL = new HashMap();
   static final List<Object[]> SHADES = new ArrayList();
   static int grayMade;

   private ClientPassTm() {
   }

   static void clear() {
      for(ClientPassTm.Tm s : TM.values()) {
         if (s.loop != null) {
            s.loop.halt();
         }
      }

      for(ClientPass.Loop l : TICKS.values()) {
         l.halt();
      }

      TM.clear();
      FX.clear();
      MARKS.clear();
      HELD.clear();
      PROJ.clear();
      HIST.clear();
      MOVES.clear();
      GHOSTS.clear();
      ECHOES.clear();
      LAYERS.clear();
      LAYER_AT.clear();
      TICKS.clear();
      TICKS_AT.clear();
      SHADES.clear();

      for(DynamicTexture d : GRAY_MADE) {
         try {
            d.close();
         } catch (RuntimeException var3) {
         }
      }

      GRAY_MADE.clear();
      GRAY.clear();
      GRAY_FAIL.clear();
      grayMade = 0;
   }

   static boolean empty() {
      return TM.isEmpty() && FX.isEmpty() && MARKS.isEmpty() && GHOSTS.isEmpty();
   }

   static PassBridge.Holder holder(AbstractClientPlayer p) {
      return PassBridge.holder(p, TM_POWERS);
   }

   static int colorOf(AbstractClientPlayer p) {
      int c = PassBridge.color(p.getUUID());
      return c != -1 && (c & 16777215) != 16777215 ? c & 16777215 : 16740288;
   }

   static PassCast.Pal palOf(ClientPassTm.Tm s) {
      return PassCast.pal(s.color, s.copy);
   }

   static PassCast.Pal palOf(ClientLevel level, PassFxPacket m) {
      ClientPassTm.Tm s = (ClientPassTm.Tm)TM.get(m.caster);
      if (s != null) {
         return palOf(s);
      } else {
         Entity c = m.caster != 0 ? level.getEntity(m.caster) : null;
         if (c instanceof AbstractClientPlayer) {
            AbstractClientPlayer p = (AbstractClientPlayer)c;
            return PassCast.pal(colorOf(p), PassBridge.isAM(p.getUUID()) || m.has(1));
         } else {
            int col = m.color != 16777215 && m.color != 0 ? m.color : 16740288;
            return PassCast.pal(col, m.has(1));
         }
      }
   }

   static double[] feetOf(ClientLevel level, int id, double[] or) {
      Entity e = id != 0 ? level.getEntity(id) : null;
      return e != null && !e.isRemoved() ? ClientPass.a(e.position()) : or;
   }

   static void acted(int caster, double now) {
      ClientPassTm.Tm s = (ClientPassTm.Tm)TM.get(caster);
      if (s != null) {
         s.act = now;
      }

   }

   static PassBody snapOf(ClientLevel level, int id) {
      Entity e = id != 0 ? level.getEntity(id) : null;
      PassBody b = e != null ? PassBodies.last(e) : null;
      if (b != null && b.posed()) {
         return b;
      } else {
         ClientPassTm.Hist h = (ClientPassTm.Hist)HIST.get(id);
         if (h != null && !h.body.isEmpty()) {
            PassBody hb = (PassBody)h.body.get(h.body.size() - 1);
            return hb != null && hb.posed() ? hb : null;
         } else {
            return null;
         }
      }
   }

   static void apply(PassFxPacket m, ClientLevel level, double now) {
      PassCast.Pal pal = palOf(level, m);
      if (m.op == 22) {
         double[] feet = feetOf(level, m.caster, m.at());
         FX.add(new ClientPassTm.Fx(0, m.caster, 0, feet, (double[])null, pal, now, 40.0D));
         ClientPassTm.Tm s = (ClientPassTm.Tm)TM.get(m.caster);
         if (s != null) {
            s.onPacket = now;
            if (s.on && !Double.isNaN(s.onSince) && now - s.onSince > 4.0D) {
               s.onSince = now;
            }
         }

         acted(m.caster, now);
         if (ClientPass.near(feet)) {
            ClientPass.sound("tm_on", feet, 1.5F);
         }
      } else if (m.op == 23) {
         boolean decel = m.has(128);
         ClientPassTm.Tm s = (ClientPassTm.Tm)TM.get(m.caster);
         if (s != null && decel) {
            s.fieldR = m.v[3] > 0.0F ? Math.min(64.0D, (double)m.v[3]) : Double.NaN;
         }

         if (m.has(32)) {
            return;
         }

         int mode = decel ? 2 : 1;
         if (s != null) {
            s.pendFlow = mode;
            s.pendT = now;
            if (s.flowing(mode) && !s.flowTold && now - s.flowStart <= 20.0D) {
               s.flowStart = now;
               s.flowTold = true;
               if (mode == 2) {
                  s.fieldStart = now;
               }
            }
         }

         acted(m.caster, now);
         double[] feet = feetOf(level, m.caster, m.at());
         if (ClientPass.near(feet)) {
            ClientPass.sound(decel ? "tm_slow" : "tm_flow", feet, 1.5F);
         }
      } else if (m.op == 24) {
         double[] feet = feetOf(level, m.target, m.at());
         ClientPassTm.Fx f = new ClientPassTm.Fx(2, m.caster, m.target, feet, (double[])null, pal, now, 40.0D);
         ClientPassTm.Hist h = (ClientPassTm.Hist)HIST.get(m.target);
         if (h != null && !h.body.isEmpty()) {
            int back = Math.max(0, Math.min(h.body.size() - 1, m.aux));
            int from = h.body.size() - 1 - back;
            f.past = (PassBody[])h.body.subList(from, h.body.size()).toArray(new PassBody[0]);
         }

         FX.add(f);
         acted(m.caster, now);
         if (ClientPass.near(feet)) {
            ClientPass.sound("tm_rewind", feet, 1.5F);
         }

         ClientPass.kick(feet, now, 0.35D, 32.0D, 8.0D);
      } else if (m.op != 25 && m.op != 27) {
         if (m.op == 26) {
            if (m.aux <= 0) {
               MARKS.remove(m.caster);
               return;
            }

            int left = Math.min(600, m.aux);
            MARKS.put(m.caster, new ClientPassTm.Mark(m.at(), now - (double)(600 - left), now + (double)left, pal));
            if (!m.has(32)) {
               acted(m.caster, now);
               if (ClientPass.near(m.at())) {
                  ClientPass.sound("tm_mark", m.at(), 1.0F);
               }
            }
         } else if (m.op == 28) {
            double[] c = m.at();
            double r = Math.max(0.5D, Math.min(64.0D, (double)m.v[3]));
            ClientPassTm.Fx f = new ClientPassTm.Fx(5, m.caster, 0, c, (double[])null, pal, now, Math.max(1.0D, (double)m.v[4]) + 40.0D);
            f.r = r;
            f.until = now + Math.max(1.0D, (double)m.v[4]);
            List<Integer> held = new ArrayList();

            for(Map.Entry<Integer, ClientPassTm.Pr> en : PROJ.entrySet()) {
               ClientPassTm.Pr s = (ClientPassTm.Pr)en.getValue();
               Entity e = level.getEntity(en.getKey());
               Entity var10000;
               if (e instanceof Projectile) {
                  Projectile pr = (Projectile)e;
                  var10000 = pr.getOwner();
               } else {
                  var10000 = null;
               }

               Entity owner = var10000;
               if (!s.stuck && (owner == null || owner.getId() != m.caster) && !(PassMath.dist(s.pos, c) > r)) {
                  s.frozenUntil = f.until;
                  held.add((Integer)en.getKey());
               }
            }

            f.arrows = new int[held.size()];

            for(int i = 0; i < held.size(); ++i) {
               f.arrows[i] = held.get(i);
            }

            FX.add(f);
            acted(m.caster, now);
            if (ClientPass.near(c)) {
               ClientPass.sound("tm_freeze", c, 1.5F);
            }

            ClientPass.kick(c, now, 0.7D, 64.0D, 14.0D);
         } else if (m.op == 29) {
            ClientPassTm.Held h = (ClientPassTm.Held)HELD.computeIfAbsent(m.target, (k) -> new ClientPassTm.Held());
            h.until = now + (double)Math.max(1, m.aux);
            h.limit = h.until + 5.0D;
            h.strike = m.has(8);
            h.released = false;
            h.seen = now;
         } else if (m.op == 30) {
            ClientPassTm.Held h = (ClientPassTm.Held)HELD.get(m.target);
            if (h != null) {
               h.released = true;
            }
         } else if (m.op == 31) {
            Entity t = level.getEntity(m.target);
            double[] dest = m.vec();
            double[] from = m.at();
            double[] tpos = t != null && !t.isRemoved() ? ClientPass.a(t.position()) : null;
            double[] d = tpos != null ? PassMath.sub(tpos, dest) : PassMath.sub(dest, from);
            d[1] = 0.0D;
            if (PassMath.len(d) < 1.0E-4D) {
               d = PassMath.v(1.0D, 0.0D, 0.0D);
            }

            double[] mid = t != null && !t.isRemoved() ? ClientPass.mid(t, 1.0F) : PassMath.add(dest, PassMath.v(0.0D, 1.0D, 0.0D));
            ClientPassTm.Fx f = new ClientPassTm.Fx(6, m.caster, m.target, from, dest, pal, now, 40.0D);
            f.dir = PassMath.norm(d);
            f.mid = mid;
            f.snap = snapOf(level, m.caster);
            FX.add(f);
            acted(m.caster, now);
            double[] at = tpos != null ? tpos : dest;
            if (ClientPass.near(at)) {
               ClientPass.sound("tm_strike", at, 1.5F);
            }

            ClientPass.flash(at, 0.35D, 48.0D, pal.main);
            ClientPass.kick(at, now, 1.0D, 48.0D, 14.0D);
         } else if (m.op == 32) {
            int left = Math.max(1, Math.min(59, m.aux));
            double due = now + (double)left;
            if (m.has(32)) {
               for(ClientPassTm.Fx o : FX) {
                  if (o.kind == 7 && o.target == m.target && !o.landed && Math.abs(o.due - due) <= 2.0D) {
                     return;
                  }
               }
            }

            ClientPassTm.Fx f = new ClientPassTm.Fx(7, m.caster, m.target, feetOf(level, m.target, m.at()), (double[])null, pal, due - 59.0D, 69.0D);
            f.set = due - 59.0D;
            f.due = due;
            FX.add(f);
            if (!m.has(32)) {
               acted(m.caster, now);
               double[] at = feetOf(level, m.target, m.at());
               if (ClientPass.near(at)) {
                  ClientPass.sound("tm_mark", at, 1.0F);
               }
            }
         } else if (m.op == 33) {
            ClientPassTm.Fx f = null;

            for(ClientPassTm.Fx o : FX) {
               if (o.kind == 7 && o.target == m.target && !o.landed && (f == null || o.due < f.due)) {
                  f = o;
               }
            }

            if (f == null) {
               f = new ClientPassTm.Fx(7, m.caster, m.target, m.at(), (double[])null, pal, now - 59.0D, 86.0D);
               f.set = now - 59.0D;
               FX.add(f);
            }

            f.landed = true;
            f.due = now;
            f.life = now - f.born + 27.0D;
            double[] at = feetOf(level, m.target, m.at());
            if (ClientPass.near(at)) {
               ClientPass.sound("tm_delay", at, 1.5F);
            }

            ClientPass.kick(at, now, 0.8D, 48.0D, 12.0D);
            ClientPass.flash(at, 0.3D, 48.0D, f.pal.main);
         }
      } else {
         boolean back = m.op == 27;
         ClientPassTm.Fx f = new ClientPassTm.Fx(back ? 4 : 3, m.caster, 0, m.at(), m.vec(), pal, now, back ? 30.0D : 30.0D);
         f.snap = snapOf(level, m.caster);
         FX.add(f);
         if (back) {
            MARKS.remove(m.caster);
         }

         acted(m.caster, now);
         if (ClientPass.near(f.to)) {
            ClientPass.sound("tm_blink", f.to, 1.5F);
         }
      }

   }

   static void tick(Minecraft mc, ClientLevel level, double now) {
      List<ClientPassTm.Tm> on = tickUsers(mc, level, now);
      tickHistory(level, now, on);
      tickProjectiles(level, now, on);
      tickSlowed(level, now);
      tickHeld(level, now);
      Iterator<ClientPassTm.Fx> it = FX.iterator();

      while(it.hasNext()) {
         ClientPassTm.Fx f = (ClientPassTm.Fx)it.next();
         if (f.kind == 5 && !f.released && now >= f.until) {
            f.released = true;
            if (ClientPass.near(f.at)) {
               ClientPass.sound("tm_release", f.at, 1.5F);
            }

            ClientPass.kick(f.at, now, 0.5D, 48.0D, 10.0D);
         }

         if (f.kind == 7 && !f.dead) {
            LivingEntity tg = ClientPass.living(level, f.target);
            f.dead = tg != null && tg.isDeadOrDying();
         }

         if (now - f.born > f.life + 1.0D) {
            it.remove();
         }
      }

      MARKS.values().removeIf((k) -> now > k.until);
      GHOSTS.removeIf((g) -> now - g.born > g.life + 1.0D);
      var it1 = ECHOES.values().iterator();

      while(it1.hasNext()) {
         List<ClientPassTm.Echo> l = (List)it1.next();
         l.removeIf((x) -> now - x.born > 15.0D);
         if (l.isEmpty()) {
            it1.remove();
         }
      }

      tickLoops(mc, level, now);
   }

   static List<ClientPassTm.Tm> tickUsers(Minecraft mc, ClientLevel level, double now) {
      Set<Integer> here = new HashSet();
      List<ClientPassTm.Tm> on = new ArrayList();

      for(AbstractClientPlayer p : level.players()) {
         PassBridge.Holder h = holder(p);
         if (h != null) {
            ClientPassTm.Tm s = (ClientPassTm.Tm)TM.get(p.getId());
            boolean first = s == null;
            if (first) {
               s = new ClientPassTm.Tm(p.getId());
               TM.put(p.getId(), s);
            }

            here.add(p.getId());
            s.seen = now;
            boolean live = !p.isSpectator() && ClientPass.enabled(h, "timemanip_activation");
            boolean lit = live && ClientPass.unveiled(h);
            String path = ClientPass.pathOf(h);
            s.copy = path.endsWith("_am") || PassBridge.isAM(p.getUUID());
            s.color = colorOf(p);
            if (lit && !s.lit) {
               s.onSince = now - s.onPacket <= 4.0D ? s.onPacket : (first ? now - 20.0D : now);
            } else if (!lit) {
               s.onSince = Double.NaN;
            }

            s.on = live;
            s.lit = lit;
            boolean accel = live && ClientPass.enabled(h, "anim_clock");
            boolean decel = live && !accel && ClientPass.enabled(h, "anim_counter");
            int mode = accel ? 1 : (decel ? 2 : 0);
            if (mode != s.flow) {
               if (s.flow != 0 && s.flowEnd < 0.0D) {
                  s.flowEnd = now;
               }

               if (s.fieldStart >= 0.0D && s.fieldEnd < 0.0D) {
                  s.fieldEnd = now;
               }

               if (mode != 0) {
                  boolean told = s.pendFlow == mode && now - s.pendT <= 10.0D;
                  s.flowStart = told ? s.pendT : now - 10.0D;
                  s.flowTold = told;
                  s.flowEnd = -1.0D;
                  if (mode == 2) {
                     s.fieldStart = s.flowStart;
                     s.fieldEnd = -1.0D;
                  }
               }

               s.flow = mode;
            }

            if (s.fieldStart >= 0.0D && s.fieldEnd >= 0.0D && now - s.fieldEnd > 11.0D) {
               s.fieldStart = -1.0D;
            }

            s.spinPrev = s.spin;
            s.rate += (PassTm.rateWant(s.flow, now - s.act) - s.rate) * 0.18D;
            s.spin += 0.045D * s.rate;
            double[] feet = ClientPass.a(p.position());
            if (s.last != null) {
               double moved = Math.hypot(feet[0] - s.last[0], feet[2] - s.last[2]);
               s.still = moved < 0.01D ? s.still + 1 : 0;
            }

            s.last = feet;
            if (live) {
               on.add(s);
            }

            if (s.flowing(1) && (long)now % 2L == 0L && s.still < 2 && !p.isInvisible()) {
               PassBody b = PassBodies.last(p);
               if (b != null && b.posed()) {
                  ghost(new ClientPassTm.Ghost(b, now, palOf(s).main, 10.0D, 0.5D, palOf(s).copy));
               }
            }

            if (s.flowing(1) && p.onGround() && (long)now % 5L == 0L && s.still == 0 && !p.isInvisible()) {
               FX.add(new ClientPassTm.Fx(1, p.getId(), 0, (double[])feet.clone(), (double[])null, palOf(s), now, 14.0D));
            }
         }
      }

      Iterator<Map.Entry<Integer, ClientPassTm.Tm>> it = TM.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<Integer, ClientPassTm.Tm> e = (Map.Entry)it.next();
         ClientPassTm.Tm s = (ClientPassTm.Tm)e.getValue();
         if (!here.contains(e.getKey())) {
            s.on = false;
            s.lit = false;
            s.onSince = Double.NaN;
            if (s.flow != 0 && s.flowEnd < 0.0D) {
               s.flowEnd = now;
            }

            if (s.fieldStart >= 0.0D && s.fieldEnd < 0.0D) {
               s.fieldEnd = now;
            }

            s.flow = 0;
            if (s.loop != null) {
               s.loop.want = 0.0F;
            }

            if (now - s.seen > 40.0D) {
               if (s.loop != null) {
                  s.loop.halt();
               }

               LAYERS.remove(e.getKey());
               LAYER_AT.remove(e.getKey());
               it.remove();
            }
         }
      }

      return on;
   }

   static void ghost(ClientPassTm.Ghost g) {
      while(GHOSTS.size() >= 256) {
         GHOSTS.remove(0);
      }

      GHOSTS.add(g);
   }

   static boolean nearAny(List<ClientPassTm.Tm> on, ClientLevel level, double[] p) {
      for(ClientPassTm.Tm s : on) {
         Entity e = level.getEntity(s.id);
         if (e != null && e.distanceToSqr(p[0], p[1], p[2]) <= 1600.0D) {
            return true;
         }
      }

      return false;
   }

   static void tickHistory(ClientLevel level, double now, List<ClientPassTm.Tm> on) {
      boolean even = (long)now % 2L == 0L;
      if (!on.isEmpty() && even) {
         for(AbstractClientPlayer p : level.players()) {
            double[] feet = ClientPass.a(p.position());
            if (nearAny(on, level, feet)) {
               ClientPassTm.Hist h = (ClientPassTm.Hist)HIST.computeIfAbsent(p.getId(), (k) -> new ClientPassTm.Hist());
               h.seen = now;
               h.pos.add(feet);
               h.t.add(now);
               h.body.add(p.isInvisible() ? null : PassBodies.last(p));

               while(h.pos.size() > 60) {
                  h.pos.remove(0);
                  h.t.remove(0);
                  h.body.remove(0);
               }
            }
         }
      }

      HIST.values().removeIf((hx) -> now - hx.seen > 40.0D);
   }

   static void tickProjectiles(ClientLevel level, double now, List<ClientPassTm.Tm> on) {
      if (!on.isEmpty() || !PROJ.isEmpty()) {
         for(ClientPassTm.Tm u : on) {
            Entity ue = level.getEntity(u.id);
            if (ue != null) {
               for(Projectile pr : level.getEntitiesOfClass(Projectile.class, ue.getBoundingBox().inflate(40.0D), (x) -> !x.isRemoved())) {
                  if (!PROJ.containsKey(pr.getId()) && pr.distanceToSqr(ue) <= 1600.0D) {
                     ClientPassTm.Pr s = new ClientPassTm.Pr();
                     s.pos = ClientPass.a(pr.position());
                     s.prev = s.pos;
                     s.seen = now;
                     PROJ.put(pr.getId(), s);
                  }
               }
            }
         }

         for(Map.Entry<Integer, ClientPassTm.Pr> en : PROJ.entrySet()) {
            Entity e = level.getEntity(en.getKey());
            if (e instanceof Projectile) {
               Projectile pr = (Projectile)e;
               if (!pr.isRemoved()) {
                  double[] pos = ClientPass.a(pr.position());
                  ClientPassTm.Pr s = (ClientPassTm.Pr)en.getValue();
                  s.seen = now;
                  s.prev = s.pos;
                  s.pos = pos;
                  boolean frozen = s.frozenUntil >= now;
                  boolean moved = PassMath.dist(s.pos, s.prev) > 1.0E-4D;
                  s.stuck = !moved && !frozen;
                  if (moved && !frozen) {
                     s.trail.add(pos);
                     if (s.trail.size() > 10) {
                        s.trail.remove(0);
                     }
                  }

                  if (!s.stuck && !frozen) {
                     Entity owner = pr.getOwner();

                     for(ClientPassTm.Tm u : on) {
                        Entity ue = level.getEntity(u.id);
                        if (u.flowing(2) && !Double.isNaN(u.fieldR) && ue != null && owner != ue) {
                           double[] uf = ClientPass.a(ue.position());
                           if (!(PassMath.dist(pos, uf) > u.fieldR)) {
                              if (Double.isNaN(s.enterT)) {
                                 s.enterT = now;
                                 s.enterAt = PassTm.edge(s.prev, s.pos, uf, u.fieldR);
                                 double[] vel = ClientPass.a(pr.getDeltaMovement());
                                 double[] step = PassMath.sub(s.pos, s.prev);
                                 s.enterDir = PassMath.len(vel) > 1.0E-4D ? PassMath.norm(vel) : (PassMath.len(step) > 1.0E-4D ? PassMath.norm(step) : PassMath.v(1.0D, 0.0D, 0.0D));
                              }

                              s.slowed = now;
                              s.by = u.id;
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

         PROJ.values().removeIf((sx) -> now - sx.seen > 2.0D);
      }
   }

   static void tickSlowed(ClientLevel level, double now) {
      Set<Integer> seen = new HashSet();

      for(ClientPassTm.Tm u : TM.values()) {
         if (u.on && u.flowing(2) && !Double.isNaN(u.fieldR)) {
            Entity ue = level.getEntity(u.id);
            if (ue != null) {
               double r = u.fieldR;
               AABB box = ue.getBoundingBox().inflate(r);

               for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, (le) -> le.isAlive() && le != ue)) {
                  if (!(e.distanceToSqr(ue) > r * r) && !HELD.containsKey(e.getId()) && seen.add(e.getId())) {
                     ClientPassTm.Tm other = (ClientPassTm.Tm)TM.get(e.getId());
                     if (other == null || !other.on) {
                        ClientPassTm.Mov m = (ClientPassTm.Mov)MOVES.computeIfAbsent(e.getId(), (k) -> new ClientPassTm.Mov());
                        double[] feet = ClientPass.a(e.position());
                        if (m.last != null) {
                           double moved = Math.hypot(feet[0] - m.last[0], feet[2] - m.last[2]);
                           m.still = moved < 0.01D ? m.still + 1 : 0;
                        }

                        m.last = feet;
                        m.seen = now;
                        if ((long)now % 4L == 0L && m.still < 2 && !e.isInvisible()) {
                           PassBody b = PassBodies.last(e);
                           if (b != null && b.posed()) {
                              ghost(new ClientPassTm.Ghost(b, now, PassTm.GREY, 14.0D, 0.32D, palOf(u).copy));
                           } else if (ECHOES.containsKey(e.getId()) || ECHOES.size() < 24) {
                              List<ClientPassTm.Echo> l = (List)ECHOES.computeIfAbsent(e.getId(), (k) -> new ArrayList());
                              if (l.size() >= 6) {
                                 l.remove(0);
                              }

                              l.add(new ClientPassTm.Echo(feet, now, palOf(u).copy));
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      MOVES.values().removeIf((mx) -> now - mx.seen > 2.0D);
   }

   static void tickHeld(ClientLevel level, double now) {
      Iterator<Map.Entry<Integer, ClientPassTm.Held>> it = HELD.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<Integer, ClientPassTm.Held> en = (Map.Entry)it.next();
         ClientPassTm.Held h = (ClientPassTm.Held)en.getValue();
         h.satPrev = h.sat;
         if (!h.released && now >= h.limit) {
            h.released = true;
         }

         boolean holding = !h.released && now < h.limit;
         if (holding) {
            h.sat = Math.max(h.strike ? 0.15D : 0.12D, h.sat - (h.strike ? 0.3D : 0.22D));
         } else {
            h.sat = Math.min(1.0D, h.sat + 0.25D);
         }

         Entity e = level.getEntity(en.getKey());
         if (e != null && !e.isRemoved()) {
            h.seen = now;
            if (holding && e instanceof LivingEntity) {
               LivingEntity live = (LivingEntity)e;
               live.walkAnimation.update(0.0F, 1.0F);
               live.walkAnimation.update(0.0F, 1.0F);
            }
         }

         if (!holding && h.sat >= 1.0D && h.satPrev >= 1.0D || now - h.seen > 40.0D) {
            it.remove();
         }
      }

   }

   static void tickLoops(Minecraft mc, ClientLevel level, double now) {
      for(ClientPassTm.Tm s : TM.values()) {
         Entity e = level.getEntity(s.id);
         float want = s.on && e != null ? (s.flowing(1) ? 0.9F : (s.flowing(2) ? 0.45F : 0.0F)) : 0.0F;
         if (want > 0.0F && !ClientPass.near(ClientPass.a(e.position()))) {
            want = 0.0F;
         }

         ClientPass.Loop was = s.loop;
         s.loop = loop(mc, e, s.loop, want, now, s.loopAt);
         if (s.loop != null && s.loop != was) {
            s.loopAt = now;
         }
      }

      Set<Integer> ticking = new HashSet();

      for(ClientPassTm.Fx f : FX) {
         if (f.kind == 7 && !f.landed && !f.dead && now < f.due && ClientPass.living(level, f.target) != null) {
            ticking.add(f.target);
         }
      }

      for(Integer id : ticking) {
         ClientPass.Loop have = (ClientPass.Loop)TICKS.get(id);
         if (have != null && !ClientPass.alive(have) && now - TICKS_AT.getOrDefault(id, now) >= 20.0D) {
            have.halt();
            TICKS.remove(id);
            TICKS_AT.remove(id);
            have = null;
         }

         if (have == null && TICKS.size() < 3) {
            Entity e = level.getEntity(id);
            if (e != null && ClientPass.near(ClientPass.a(e.position()))) {
               ClientPass.Loop l = new ClientPass.Loop(PassBridge.sound("unrp:pass_ticking"), id, e, 0.8F);
               mc.getSoundManager().play(l);
               TICKS.put(id, l);
               TICKS_AT.put(id, now);
            }
         }
      }

      Iterator<Map.Entry<Integer, ClientPass.Loop>> it = TICKS.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<Integer, ClientPass.Loop> en = (Map.Entry)it.next();
         ClientPass.Loop l = (ClientPass.Loop)en.getValue();
         l.want = ticking.contains(en.getKey()) ? 0.8F : 0.0F;
         if (l.want <= 0.0F && !ClientPass.alive(l) || l.isStopped()) {
            l.halt();
            TICKS_AT.remove(en.getKey());
            it.remove();
         }
      }

   }

   static ClientPass.Loop loop(Minecraft mc, Entity e, ClientPass.Loop l, float want, double now, double at) {
      if (want > 0.0F && e != null) {
         if (l == null || l.isStopped() || !ClientPass.alive(l) && now - at >= 20.0D) {
            if (l != null) {
               l.halt();
            }

            l = new ClientPass.Loop(PassBridge.sound("unrp:pass_ticking"), e.getId(), e, want);
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
      for(ClientPassTm.Tm s : TM.values()) {
         if (s.on) {
            w.add(s.id);
         }
      }

      w.addAll(HIST.keySet());
      w.addAll(MOVES.keySet());

      for(ClientPassTm.Fx f : FX) {
         if (f.caster != 0) {
            w.add(f.caster);
         }
      }

   }

   public static float eyeHeat(AbstractClientPlayer p, float pt) {
      ClientPassTm.Tm s = (ClientPassTm.Tm)TM.get(p.getId());
      ClientLevel level = ClientPass.where;
      return s != null && s.on && !Double.isNaN(s.onSince) && !s.copy && level != null && p.level() == level ? (float)(0.8D * PassMath.clamp01(((double)((float)level.getGameTime() + pt) - s.onSince) / 8.0D)) : 0.0F;
   }

   public static void layer(AbstractClientPlayer p, PoseStack pose, float pt) {
      if (PassBodies.inWorld && TM.containsKey(p.getId())) {
         try {
            double[] m = PassBodies.layer(pose.last().pose());
            Vec3 at = p.getPosition(pt);
            double dx = m[3] - at.x;
            double dy = m[7] - at.y;
            double dz = m[11] - at.z;
            if (!(dx * dx + dy * dy + dz * dz <= 9.0D)) {
               return;
            }

            for(double x : m) {
               if (!Double.isFinite(x)) {
                  return;
               }
            }

            LAYERS.put(p.getId(), m);
            LAYER_AT.put(p.getId(), (double)p.tickCount + (double)pt);
         } catch (RuntimeException var16) {
            LAYERS.remove(p.getId());
         }

      }
   }

   static double[] layerOf(AbstractClientPlayer p, float pt) {
      double[] m = (double[])LAYERS.get(p.getId());
      Double s = (Double)LAYER_AT.get(p.getId());
      return m != null && s != null && Math.abs(s - (double)((float)p.tickCount + pt)) < 0.001D ? m : null;
   }

   static double[] apply(double[] m, double x, double y, double z) {
      return PassMath.v(m[0] * x + m[1] * y + m[2] * z + m[3], m[4] * x + m[5] * y + m[6] * z + m[7], m[8] * x + m[9] * y + m[10] * z + m[11]);
   }

   static PassTm.Dart arrow(ClientPassTm.Pr s) {
      PassTm.Dart a = new PassTm.Dart();
      a.enterT = s.enterT;
      a.enterAt = s.enterAt;
      a.enterDir = s.enterDir;
      a.slowed = s.slowed;
      a.prev = s.prev;
      a.pos = s.pos;
      a.stuck = s.stuck;
      a.trail = (double[][])s.trail.toArray(new double[0][]);
      return a;
   }

   static PassTm.Dart[] arrowsBy(int id) {
      List<PassTm.Dart> out = new ArrayList();

      for(ClientPassTm.Pr s : PROJ.values()) {
         if (s.by == id && (!Double.isNaN(s.enterT) || !Double.isNaN(s.slowed)) && s.enterAt != null) {
            out.add(arrow(s));
         }
      }

      return (PassTm.Dart[])out.toArray(new PassTm.Dart[0]);
   }

   static PassTm.Dart[] held(ClientPassTm.Fx f, double t) {
      List<PassTm.Dart> out = new ArrayList();

      for(int id : f.arrows) {
         ClientPassTm.Pr s = (ClientPassTm.Pr)PROJ.get(id);
         if (s != null && s.frozenUntil > t) {
            out.add(arrow(s));
         }
      }

      return (PassTm.Dart[])out.toArray(new PassTm.Dart[0]);
   }

   static void draw(PassQuads q, ClientLevel level, double t, float pt, double[] cam, double r) {
      for(ClientPassTm.Tm s : TM.values()) {
         Entity e = level.getEntity(s.id);
         if (e instanceof AbstractClientPlayer p) {
            if (!p.isRemoved() && !(p.distanceToSqr(cam[0], cam[1], cam[2]) > r * r)) {
               drawUser(q, level, p, s, t, pt);
            }
         }
      }

      for(Map.Entry<Integer, ClientPassTm.Mark> en : MARKS.entrySet()) {
         ClientPassTm.Mark m = (ClientPassTm.Mark)en.getValue();
         if (ClientPass.near(m.pos)) {
            q.dim = m.pal.copy ? PassCast.SMOKE_DARK : null;
            PassTm.mark(q, m.pal, m.pos, m.set, m.until, t);
            q.dim = null;
         }
      }

      for(ClientPassTm.Fx f : FX) {
         if (ClientPass.near(f.at)) {
            drawFx(q, level, f, t, pt);
         }
      }

      for(ClientPassTm.Ghost g : GHOSTS) {
         double k = 1.0D - (t - g.born) / g.life;
         if (k > 0.0D && ClientPass.near(g.body.pos)) {
            q.dim = g.copy ? PassCast.SMOKE_DARK : null;
            PassCast.ghost(q, g.body, g.rgb, g.a * k * k);
         }
      }

      q.dim = null;
   }

   static void drawUser(PassQuads q, ClientLevel level, AbstractClientPlayer p, ClientPassTm.Tm s, double t, float pt) {
      PassCast.Pal pal = palOf(s);
      double[] feet = ClientPass.a(p.getPosition(pt));
      q.dim = pal.copy ? PassCast.SMOKE_DARK : null;
      if (s.fieldStart >= 0.0D && !Double.isNaN(s.fieldR)) {
         PassTm.field(q, pal, feet, s.fieldR, t, s.fieldStart, s.fieldEnd, arrowsBy(s.id), (double)pt);
      }

      boolean shown = !p.isInvisible() && !p.isSpectator() && !p.isDeadOrDying();
      if (s.on && shown) {
         boolean fp = ClientPass.firstSelf(p);
         boolean lit = !Double.isNaN(s.onSince);
         double k = lit ? PassMath.clamp01((t - s.onSince) / 8.0D) : 0.0D;
         if (!fp && lit) {
            double[] lay = layerOf(p, pt);
            if (lay != null) {
               PassTm.gears(q, pal, (x, y, z) -> apply(lay, x, y, z), k, PassMath.lerp(s.spinPrev, s.spin, (double)pt));
            }

            PassTm.grains(q, pal, feet, t, k);
         }

         ClientPassTm.Hist h = (ClientPassTm.Hist)HIST.get(p.getId());
         if (s.flowing(1) && h != null) {
            int n = h.pos.size();
            double[][] hist = (double[][])h.pos.toArray(new double[0][]);
            double[] ht = new double[n];

            for(int i = 0; i < n; ++i) {
               ht[i] = h.t.get(i);
            }

            PassTm.haste(q, pal, feet, t, hist, ht, s.flowStart, !fp);
         }

         q.dim = null;
         if (lit) {
            PassBody body = PassBodies.get(p, pt);
            double fl = PassMath.clamp01(1.0D - (t - s.act) / 12.0D);
            if (!fp && body.posed() && pal.copy && !ClientPassAm.eyes(p, k, fl)) {
               PassCast.eyesTrailer(q, body, PassCast.EMBER, k, fl);
            }

            if (!pal.copy) {
               PassBridge.passFlare(p.getId(), fl, t);
            }

         }
      } else {
         q.dim = null;
      }
   }

   static void drawFx(PassQuads q, ClientLevel level, ClientPassTm.Fx f, double t, float pt) {
      double age = t - f.born;
      if (!(age < 0.0D) && !(age > f.life)) {
         PassCast.Pal pal = f.pal;
         q.dim = pal.copy ? PassCast.SMOKE_DARK : null;
         SHADES.clear();
         PassTm.Ghosts gh = (which, rgb, a) -> {
            PassBody b = f.kind == 2 ? (which >= 0 && which < f.past.length ? f.past[which] : null) : f.snap;
            if (b != null) {
               SHADES.add(new Object[]{b, rgb, a});
            }

         };
         if (f.kind == 0) {
            Entity e = level.getEntity(f.caster);
            double[] g = e != null && !e.isRemoved() ? ClientPass.a(e.getPosition(pt)) : f.at;
            PassTm.ignite(q, pal, g, age);
         } else if (f.kind == 1) {
            PassTm.step(q, pal, f.at, age);
         } else if (f.kind == 2) {
            Entity e = level.getEntity(f.target);
            if (e != null && !e.isRemoved()) {
               PassTm.rewind(q, pal, ClientPass.a(e.getPosition(pt)), age, f.past.length, f.born, gh);
            }
         } else if (f.kind == 3) {
            PassTm.blink(q, pal, f.at, f.to, f.born, age, gh);
         } else if (f.kind == 4) {
            PassTm.recall(q, pal, f.at, f.to, f.born, age, gh);
         } else if (f.kind == 5) {
            PassTm.freeze(q, pal, f.at, f.r, f.born, f.until, t, age, held(f, t));
         } else if (f.kind == 6) {
            LivingEntity tg = ClientPass.living(level, f.target);
            double[] c = tg != null ? ClientPass.mid(tg, pt) : f.mid;
            ClientPassTm.Held h = tg != null ? (ClientPassTm.Held)HELD.get(f.target) : null;
            double heldBy = h != null && h.strike && !h.released ? h.until : Double.NaN;
            PassTm.strike(q, pal, f.at, f.to, c, f.dir, f.born, age, f.snap != null, heldBy, t - (double)pt, t, gh, tg == null || !ClientPass.firstSelf(tg));
         } else if (f.kind == 7) {
            LivingEntity tg = ClientPass.living(level, f.target);
            boolean seen = tg != null && !f.dead && !tg.isDeadOrDying();
            if (seen || f.landed && f.feet != null) {
               double tt = t;
               if (!f.landed && t >= f.due) {
                  if (t > f.due + 10.0D) {
                     q.dim = null;
                     return;
                  }

                  tt = f.due - 0.001D;
               }

               if (seen) {
                  f.feet = ClientPass.a(tg.getPosition(pt));
                  f.top = PassMath.add(f.feet, PassMath.v(0.0D, (double)tg.getBbHeight() + 0.75D, 0.0D));
                  f.mid = ClientPass.mid(tg, pt);
               }

               Entity c = level.getEntity(f.caster);
               double[] hand = c != null && !c.isRemoved() ? ClientPass.handOf(c, pt) : null;
               PassFx.Ground g = (x, z, fromY) -> ClientPass.groundBelow(level, x, z, fromY);
               PassTm.delay(q, pal, g, hand, f.mid, f.feet, f.top, f.set, f.due, f.wait, tt, tt - f.set, tg == null || !ClientPass.firstSelf(tg));
            }
         }

         q.dim = pal.copy ? PassCast.SMOKE_DARK : null;

         for(Object[] s : SHADES) {
            PassCast.ghost(q, (PassBody)s[0], (double[])s[1], (Double)s[2]);
         }

         q.dim = null;
         SHADES.clear();
      }
   }

   public static void overlay(LivingEntityRenderer<?, ?> renderer, LivingEntity e, float pt, PoseStack pose, MultiBufferSource buffers, int light) {
      if ((!HELD.isEmpty() || !ECHOES.isEmpty()) && PassBodies.inWorld && !e.isInvisible()) {
         try {
            EntityModel model = renderer.getModel();
            ClientPassTm.Held h = (ClientPassTm.Held)HELD.get(e.getId());
            if (h != null) {
               double a = 1.0D - PassMath.lerp(h.satPrev, h.sat, (double)pt);
               if (a > 0.01D) {
                  ResourceLocation tex = ((LivingEntityRenderer)renderer).getTextureLocation(e);
                  ResourceLocation g = tex != null ? gray(tex, (double)e.level().getGameTime()) : null;
                  if (g != null) {
                     model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucent(g)), light, LivingEntityRenderer.getOverlayCoords(e, 0.0F), 1.0F, 1.0F, 1.0F, (float)Math.min(1.0D, a));
                  }
               }
            }

            List<ClientPassTm.Echo> l = (List)ECHOES.get(e.getId());
            if (l != null) {
               double t = (double)((float)e.level().getGameTime() + pt);
               Vec3 now = e.getPosition(pt);

               for(ClientPassTm.Echo ec : l) {
                  double k = 1.0D - (t - ec.born) / 14.0D;
                  float a = (float)(0.32D * k * k);
                  if (!(a <= 0.004F)) {
                     Vector3f off = PassBodies.toView((float)(ec.feet[0] - now.x), (float)(ec.feet[1] - now.y), (float)(ec.feet[2] - now.z));
                     pose.pushPose();

                     try {
                        Matrix4f m = pose.last().pose();
                        Matrix4f moved = (new Matrix4f()).translate(off.x(), off.y(), off.z());
                        moved.mul(m);
                        m.set(moved);
                        if (ec.copy) {
                           model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucent(ClientPass.WHITE)), 15728880, OverlayTexture.NO_OVERLAY, (float)PassCast.SMOKE_DARK[0], (float)PassCast.SMOKE_DARK[1], (float)PassCast.SMOKE_DARK[2], (float)((double)a * 0.7D));
                        } else {
                           model.renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(ClientPass.WHITE)), 15728880, OverlayTexture.NO_OVERLAY, (float)PassTm.GREY[0] * a, (float)PassTm.GREY[1] * a, (float)PassTm.GREY[2] * a, 1.0F);
                        }
                     } finally {
                        pose.popPose();
                     }
                  }
               }
            }
         } catch (LinkageError | RuntimeException var24) {
            HELD.clear();
            ECHOES.clear();
         }

      }
   }

   static ResourceLocation gray(ResourceLocation src, double now) {
      ResourceLocation have = (ResourceLocation)GRAY.get(src);
      if (have != null) {
         return have;
      } else {
         Double failed = (Double)GRAY_FAIL.get(src);
         if (failed != null && now - failed < 200.0D && now >= failed) {
            return null;
         } else if (GRAY.size() >= 64) {
            return null;
         } else {
            NativeImage img = null;
            NativeImage out = null;

            Object ex;
            try {
               img = PassBridge.download(src);
               if (img != null) {
                  int w = img.getWidth();
                  int h = img.getHeight();
                  out = new NativeImage(w, h, false);

                  for(int y = 0; y < h; ++y) {
                     for(int x = 0; x < w; ++x) {
                        out.setPixelRGBA(x, y, PassTm.gray(img.getPixelRGBA(x, y)));
                     }
                  }

                  ResourceLocation loc = new ResourceLocation("unrp", "tm_gray/" + grayMade++);
                  DynamicTexture made = new DynamicTexture(out);
                  out = null;
                  GRAY_MADE.add(made);
                  Minecraft.getInstance().getTextureManager().register(loc, made);
                  GRAY.put(src, loc);
                  GRAY_FAIL.remove(src);
                  return loc;
               }

               GRAY_FAIL.put(src, now);
               ex = null;
            } catch (RuntimeException var15) {
               GRAY_FAIL.put(src, now);
               return null;
            } finally {
               if (img != null) {
                  img.close();
               }

               if (out != null) {
                  out.close();
               }

            }

            return (ResourceLocation)ex;
         }
      }
   }

   static final class Echo {
      final double[] feet;
      final double born;
      final boolean copy;

      Echo(double[] feet, double born, boolean copy) {
         this.feet = feet;
         this.born = born;
         this.copy = copy;
      }
   }

   static final class Fx {
      final int kind;
      final int caster;
      final int target;
      final double[] at;
      final double[] to;
      final PassCast.Pal pal;
      final double born;
      double life;
      PassBody snap;
      PassBody[] past = new PassBody[0];
      double r;
      double until;
      double due;
      double set;
      double wait = 59.0D;
      boolean landed;
      boolean released;
      boolean dead;
      double[] dir;
      double[] mid;
      double[] feet;
      double[] top;
      int[] arrows = new int[0];

      Fx(int kind, int caster, int target, double[] at, double[] to, PassCast.Pal pal, double born, double life) {
         this.kind = kind;
         this.caster = caster;
         this.target = target;
         this.at = at;
         this.to = to;
         this.pal = pal;
         this.born = born;
         this.life = life;
      }
   }

   static final class Ghost {
      final PassBody body;
      final double born;
      final double[] rgb;
      final double life;
      final double a;
      final boolean copy;

      Ghost(PassBody body, double born, double[] rgb, double life, double a, boolean copy) {
         this.body = body;
         this.born = born;
         this.rgb = rgb;
         this.life = life;
         this.a = a;
         this.copy = copy;
      }
   }

   static final class Held {
      double until;
      double limit;
      boolean strike;
      boolean released;
      double sat = 1.0D;
      double satPrev = 1.0D;
      double seen;
   }

   static final class Hist {
      final List<double[]> pos = new ArrayList();
      final List<Double> t = new ArrayList();
      final List<PassBody> body = new ArrayList();
      double seen;
   }

   static final class Mark {
      final double[] pos;
      final double set;
      final double until;
      final PassCast.Pal pal;

      Mark(double[] pos, double set, double until, PassCast.Pal pal) {
         this.pos = pos;
         this.set = set;
         this.until = until;
         this.pal = pal;
      }
   }

   static final class Mov {
      double[] last;
      int still = 99;
      double seen;
   }

   static final class Pr {
      double[] prev;
      double[] pos;
      final List<double[]> trail = new ArrayList();
      boolean stuck;
      double frozenUntil = -1.0D;
      double enterT = Double.NaN;
      double[] enterAt;
      double[] enterDir;
      double slowed = Double.NaN;
      int by;
      double seen;
   }

   static final class Tm {
      final int id;
      boolean on;
      boolean lit;
      double onSince = Double.NaN;
      double onPacket = -1.0E9D;
      boolean copy;
      int color = 16740288;
      double act = -1.0E9D;
      int flow;
      double flowStart = -1.0D;
      double flowEnd = -1.0D;
      boolean flowTold;
      int pendFlow;
      double pendT = -1.0E9D;
      double fieldStart = -1.0D;
      double fieldEnd = -1.0D;
      double fieldR = Double.NaN;
      double rate = 1.0D;
      double spin;
      double spinPrev;
      double seen;
      double[] last;
      int still = 99;
      ClientPass.Loop loop;
      double loopAt = -1.0E9D;

      Tm(int id) {
         this.id = id;
      }

      boolean flowing(int mode) {
         return this.flow == mode && this.flowEnd < 0.0D;
      }
   }
}
