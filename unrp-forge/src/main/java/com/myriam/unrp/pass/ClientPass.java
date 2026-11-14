package com.myriam.unrp.pass;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class ClientPass {
   public static final double SEEN = 72.0D;
   public static final String[] LT_POWERS = new String[]{"lightning", "lightning_enh", "lightning_syn", "lightning_am"};
   public static final String AM_SHOWN = "am_shown";
   public static final int LEAD = 4;
   public static final double BOLT_DRAG = 0.99D;
   public static final double BOLT_WET = 0.8D;
   public static final int BOLT_LIFE = 15;
   public static final int BOLT_MOST = 200;
   public static final double BOLT_HALF = 0.25D;
   public static final double ABOVE = 0.05D;
   public static final int GHOST_LIFE = 10;
   public static final double GHOST_A = 0.85D;
   public static final double BEAM_EXTEND = 0.6D;
   public static final int GROUND_SCAN = 24;
   public static final int HIST_SENT = 64;
   public static final int TOUCH_TINT = 8;
   public static final double STRIKE_OWN = 4.2D;
   public static final double[] FLASH_RGB = new double[]{0.85D, 0.88D, 1.0D};
   public static final int PENDING_MOST = 512;
   public static final int LOOP_RETRY = 20;
   public static final double HIT_AIR = 3.5D;
   public static final double MARK_AIR = 3.0D;
   public static final ResourceLocation WHITE = UnrpPowerLayers.WHITE;
   static final int K_ON = 0;
   static final int K_HIT = 1;
   static final int K_TOUCH = 2;
   static final int K_SURGE = 3;
   static ClientLevel where;
   static double glare;
   static double[] glareRgb = FLASH_RGB;
   static final List<ClientPass.Fx> FX = new ArrayList();
   static final Map<Integer, ClientPass.Lt> LT = new HashMap();
   static final Map<Integer, ClientPass.Bolt> BOLTS = new HashMap();
   static final List<ClientPass.Ghost> GHOSTS = new ArrayList();
   static final List<ClientPass.Kick> KICKS = new ArrayList();
   static final List<ClientPass.Queued> PENDING = new ArrayList();
   static final PassQuads QUADS = new PassQuads();

   private ClientPass() {
   }

   static void clearAll() {
      for(ClientPass.Lt s : LT.values()) {
         if (s.loop != null) {
            s.loop.halt();
         }
      }

      FX.clear();
      LT.clear();
      BOLTS.clear();
      GHOSTS.clear();
      KICKS.clear();
      PassBodies.clear();
      ClientPassEd.clear();
      ClientPassBr.clear();
      ClientPassTm.clear();
      ClientPassPd.clear();
      ClientPassAm.clear();
      glare = 0.0D;
      glareRgb = FLASH_RGB;
      where = null;
   }

   static void sync(ClientLevel level) {
      if (level != where) {
         clearAll();
         where = level;
      }

   }

   static double[] a(Vec3 v) {
      return new double[]{v.x, v.y, v.z};
   }

   static boolean firstSelf(Entity e) {
      Minecraft mc = Minecraft.getInstance();
      return e != null && e == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson();
   }

   static boolean flashes() {
      Minecraft mc = Minecraft.getInstance();
      return !mc.options.hideLightningFlash().get();
   }

   static boolean alive(ClientPass.Loop l) {
      return l != null && !l.isStopped() && Minecraft.getInstance().getSoundManager().isActive(l);
   }

   static PassBridge.Holder holder(AbstractClientPlayer p) {
      return PassBridge.holder(p, LT_POWERS);
   }

   static boolean enabled(PassBridge.Holder h, String key) {
      return h != null && h.enabled(key);
   }

   static String pathOf(PassBridge.Holder h) {
      return h == null ? "" : h.path;
   }

   static boolean unveiled(PassBridge.Holder h) {
      return !pathOf(h).endsWith("_am") || enabled(h, "am_shown");
   }

   static int auraOf(AbstractClientPlayer p) {
      int c = PassBridge.color(p.getUUID());
      return c != -1 && (c & 16777215) != 16777215 ? c & 16777215 : 4568319;
   }

   static PassCast.Pal pal(int color, boolean copy) {
      return PassCast.pal(color == 16777215 ? 4568319 : color, copy);
   }

   static double curve(int l) {
      double f = (double)l / 15.0D;
      return f / (4.0D - 3.0D * f);
   }

   static double gammaOf(double c) {
      double x = Math.max(0.0D, Math.min(1.0D, c * 0.96D + 0.03D));
      double g = 1.0D - Math.pow(1.0D - x, 4.0D);
      x += (g - x) * 0.5D;
      return Math.max(0.0D, Math.min(1.0D, x * 0.96D + 0.03D));
   }

   static double[] light(ClientLevel level, double[] p, float pt) {
      int packed = LevelRenderer.getLightColor(level, BlockPos.containing(p[0], p[1], p[2]));
      int blk = packed >> 4 & 15;
      int sky = packed >> 20 & 15;
      double f = (double)level.getSkyDarken(pt);
      double f1 = f * 0.95D + 0.05D;
      double tint = f + (1.0D - f) * 0.35D;
      double s = curve(sky) * f1;
      double b = curve(blk) * 1.5D;
      double g = b * ((b * 0.6D + 0.4D) * 0.6D + 0.4D);
      double bl = b * (b * b * 0.6D + 0.4D);
      return new double[]{gammaOf(b + tint * s), gammaOf(g + tint * s), gammaOf(bl + s)};
   }

   static double groundAt(ClientLevel level, double x, double z, double fromY) {
      int bx = Mth.floor(x);
      int bz = Mth.floor(z);
      int top = Mth.floor(fromY);
      BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

      for(int i = 0; i <= 24; ++i) {
         int y = top - i;
         if (y < level.getMinBuildHeight()) {
            break;
         }

         mp.set(bx, y, bz);
         BlockState st = level.getBlockState(mp);
         if (!st.isAir()) {
            VoxelShape sh = st.getCollisionShape(level, mp);
            if (!sh.isEmpty()) {
               return (double)y + sh.max(Axis.Y);
            }
         }
      }

      return fromY - 24.0D - 1.0D;
   }

   static double groundBelow(ClientLevel level, double x, double z, double fromY) {
      double g = groundAt(level, x, z, fromY);
      return g > fromY + 0.05D ? fromY - 24.0D - 1.0D : g;
   }

   public static boolean wet(ClientLevel level, double[] p, double half, double h) {
      double minX = p[0] - half + 0.001D;
      double minY = p[1] + 0.001D;
      double minZ = p[2] - half + 0.001D;
      double maxX = p[0] + half - 0.001D;
      double maxY = p[1] + h - 0.001D;
      double maxZ = p[2] + half - 0.001D;
      BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();

      for(int x = Mth.floor(minX); x < Mth.ceil(maxX); ++x) {
         for(int y = Mth.floor(minY); y < Mth.ceil(maxY); ++y) {
            for(int z = Mth.floor(minZ); z < Mth.ceil(maxZ); ++z) {
               mp.set(x, y, z);
               FluidState fs = level.getFluidState(mp);
               if (fs.is(FluidTags.WATER) && (double)((float)y + fs.getHeight(level, mp)) >= minY) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   static double[] groundUnder(ClientLevel level, double[] at) {
      double g = groundAt(level, at[0], at[2], at[1] + 0.5D);
      return at[1] - g <= 4.0D && g <= at[1] + 0.6D ? new double[]{at[0], g, at[2]} : (double[])at.clone();
   }

   static double[] hand(AbstractClientPlayer p, float pt, boolean right, PassBody body) {
      if (firstSelf(p)) {
         double side = right ? 1.0D : -1.0D;
         double yaw = (double)Mth.lerp(pt, p.yRotO, p.getYRot()) * Math.PI / 180.0D;
         double[] r = new double[]{-Math.cos(yaw), 0.0D, -Math.sin(yaw)};
         double[] look = a(p.getViewVector(pt));
         double[] eye = a(p.getEyePosition(pt));
         return PassMath.add(PassMath.add(eye, PassMath.mul(r, 0.31D * side)), PassMath.add(new double[]{0.0D, -0.27D, 0.0D}, PassMath.mul(look, 0.72D)));
      } else {
         return body.hand(right);
      }
   }

   static double[][] hands(AbstractClientPlayer p, float pt, PassBody body) {
      return new double[][]{hand(p, pt, true, body), hand(p, pt, false, body)};
   }

   static double[] handOf(Entity e, float pt) {
      if (e instanceof AbstractClientPlayer p) {
         return hand(p, pt, true, PassBodies.get(p, pt));
      } else {
         return PassBodies.get(e, pt).hand(true);
      }
   }

   static double[] mid(Entity e, float pt) {
      Vec3 p = e.getPosition(pt);
      return new double[]{p.x, p.y + (double)e.getBbHeight() * 0.5D, p.z};
   }

   static ClientPass.Lt lt(int id) {
      return (ClientPass.Lt)LT.computeIfAbsent(id, ClientPass.Lt::new);
   }

   static void sound(String id, double[] at, float vol) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && at != null) {
         mc.level.playLocalSound(at[0], at[1], at[2], PassBridge.sound("unrp:" + id), SoundSource.PLAYERS, vol, 1.0F, false);
      }
   }

   static void kick(double[] at, double now, double k, double reach, double life) {
      KICKS.add(new ClientPass.Kick((double[])at.clone(), now, k, reach, life));
   }

   static void flash(double[] at, double k, double reach, double[] rgb) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.gameRenderer != null && at != null) {
         Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
         double d = cam.distanceTo(new Vec3(at[0], at[1], at[2]));
         double near = reach * 0.25D;
         double fall = d <= near ? 1.0D : Math.max(0.0D, 1.0D - (d - near) / (reach - near));
         if (k * fall > glare) {
            glare = k * fall;
            glareRgb = rgb;
         }

      }
   }

   static double[] glareOf(ClientPass.Fx f) {
      return f.has(1) ? PassCast.SMOKE_MAIN : FLASH_RGB;
   }

   static boolean near(double[] at) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.gameRenderer != null && at != null) {
         Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
         double r = 72.0D;
         return cam.distanceToSqr(at[0], at[1], at[2]) <= r * r;
      } else {
         return false;
      }
   }

   public static float eyeHeat(AbstractClientPlayer p, float pt) {
      float ed = ClientPassEd.eyeHeat(p, pt);
      ed = Math.max(ed, ClientPassBr.eyeHeat(p, pt));
      ed = Math.max(ed, ClientPassTm.eyeHeat(p, pt));
      ed = Math.max(ed, ClientPassPd.eyeHeat(p, pt));
      ClientPass.Lt s = (ClientPass.Lt)LT.get(p.getId());
      if (s != null && s.on && !Double.isNaN(s.onSince) && !s.copy && where != null && p.level() == where) {
         double on = (double)((float)where.getGameTime() + pt) - s.onSince;
         return Math.max(ed, (float)(0.8D * PassMath.clamp01((on - 4.0D) / 6.0D)));
      } else {
         return ed;
      }
   }

   public static void accept(PassFxPacket m) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         if (m.op == 7) {
            ClientPassEd.quietAt(m.vec());
         }

         ClientPassPd.early(m, level);
         if (PENDING.size() >= 512) {
            PENDING.remove(0);
         }

         PENDING.add(new ClientPass.Queued(level, m));
      }
   }

   static void drain(ClientLevel level, double now) {
      if (!PENDING.isEmpty()) {
         List<ClientPass.Queued> todo = new ArrayList(PENDING);
         PENDING.clear();

         for(ClientPass.Queued q : todo) {
            if (q.level == level) {
               apply(q.msg, level, now);
            }
         }

      }
   }

   static void apply(PassFxPacket m, ClientLevel level, double now) {
      double[] at = m.at();
      if (m.op == 0) {
         double[] g = groundUnder(level, at);
         ClientPass.Fx f = new ClientPass.Fx(0, m.caster, 0, g, m.vec(), m.flags, m.color, now + 4.0D, 60.0D);
         f.air = at[1] - groundBelow(level, at[0], at[2], at[1] + 0.5D) > 4.0D;
         FX.add(f);
         ClientPass.Lt s = lt(m.caster);
         if (Double.isNaN(s.onSince) || now - s.onSince > 40.0D || !s.on) {
            s.onSince = now;
         }
      } else if (m.op == 1) {
         int life = m.target > 0 && m.target <= 200 ? m.target : 15;
         ClientPass.Bolt b = (ClientPass.Bolt)BOLTS.get(m.aux);
         if (m.has(32) && b != null) {
            if (b.dead < 0.0D) {
               b.pos = at;
               b.vel = m.vec();
               b.life = b.ticks + life;
            }
         } else {
            BOLTS.put(m.aux, new ClientPass.Bolt(m.caster, m.flags & -33, m.color, now, life, at, m.vec()));
            if (!m.has(32)) {
               sound("lt_bolt", at, 1.5F);
            }
         }
      } else if (m.op == 12) {
         ClientPass.Bolt b = (ClientPass.Bolt)BOLTS.get(m.aux);
         if (b != null && b.dead < 0.0D) {
            b.dead = now;
         }

         ClientPassEd.gone(m.aux);
      } else if (m.op == 2) {
         ClientPass.Bolt b = (ClientPass.Bolt)BOLTS.get(m.aux);
         if (b != null && b.dead < 0.0D) {
            b.land(at);
            b.dead = now;
         }

         ClientPass.Fx f = new ClientPass.Fx(1, m.caster, m.has(8) ? m.target : 0, at, m.vec(), m.flags, m.color, now, 60.0D);
         f.ground = groundBelow(level, at[0], at[2], at[1] + 0.5D);
         if (at[1] - f.ground > 3.5D) {
            f.air = true;
            f.ground = at[1] - 0.5D;
         }

         FX.add(f);
      } else if (m.op >= 41) {
         ClientPassAm.apply(m, level, now);
      } else if (m.op >= 34) {
         ClientPassPd.apply(m, level, now);
      } else if (m.op >= 22) {
         ClientPassTm.apply(m, level, now);
      } else if (m.op >= 13) {
         ClientPassBr.apply(m, level, now);
      } else if (m.op >= 6) {
         ClientPassEd.apply(m, level, now);
      } else if (m.op == 3 || m.op == 4) {
         boolean retort = m.op == 4;
         ClientPass.Fx f = new ClientPass.Fx(2, m.caster, m.has(8) ? m.target : 0, at, m.vec(), m.flags | (retort ? 256 : 0), m.color, now, retort ? 40.0D : 50.0D);
         FX.add(f);
      }

   }

   static void fire(ClientPass.Fx f, ClientLevel level, double now) {
      f.fired = true;
      if (f.kind == 0) {
         sound("lt_on", f.at, 3.0F);
         kick(f.at, now, 1.2D, 96.0D, 16.0D);
         flash(f.at, 1.0D, 96.0D, glareOf(f));
         if (!f.has(2) && !f.has(1) && flashes()) {
            level.setSkyFlashTime(2);
         }
      } else if (f.kind == 1) {
         sound("lt_hit", f.at, 1.5F);
         kick(f.at, now, 0.5D, 40.0D, 8.0D);
         flash(f.at, 0.35D, 40.0D, glareOf(f));
      } else if (f.kind == 2) {
         if (f.has(256)) {
            Entity t = level.getEntity(f.target);
            sound("lt_hit", t != null ? a(t.position()) : f.at, 1.0F);
         } else {
            Entity c = level.getEntity(f.caster);
            sound("lt_touch", c != null ? a(c.position()) : f.at, 1.5F);
            kick(f.at, now, 0.6D, 40.0D, 8.0D);
            flash(f.at, 0.3D, 40.0D, glareOf(f));
         }
      }

   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         ClientLevel level = mc.level;
         if (level == null) {
            if (where != null) {
               clearAll();
            }

         } else {
            sync(level);
            if (!mc.isPaused()) {
               double now = (double)level.getGameTime();
               drain(level, now);
               tickPlayers(mc, level, now);
               tickBolts(level, now);
               ClientPassEd.tick(mc, level, now);
               ClientPassBr.tick(mc, level, now);
               ClientPassTm.tick(mc, level, now);
               ClientPassPd.tick(mc, level, now);
               ClientPassAm.tick(mc, level, now);
               Iterator<ClientPass.Fx> it = FX.iterator();

               while(it.hasNext()) {
                  ClientPass.Fx f = (ClientPass.Fx)it.next();
                  if (!f.fired && now >= f.born) {
                     fire(f, level, now);
                  }

                  if (f.kind == 0 && f.fired && !f.has(2) && !f.has(1) && now - f.born == 2.0D && flashes()) {
                     level.setSkyFlashTime(2);
                  }

                  if (now - f.born > f.life + 1.0D) {
                     it.remove();
                  }
               }

               GHOSTS.removeIf((g) -> now - g.born > 11.0D);
               KICKS.removeIf((k) -> now - k.born > k.life + 1.0D);
               glare = Math.max(0.0D, glare - 0.25D);
               want(level);
            }
         }
      }
   }

   static void tickPlayers(Minecraft mc, ClientLevel level, double now) {
      Set<Integer> here = new HashSet();

      for(AbstractClientPlayer p : level.players()) {
         PassBridge.Holder h = holder(p);
         ClientPass.Lt s = (ClientPass.Lt)LT.get(p.getId());
         if (h != null || s != null && !idle(s)) {
            if (s == null) {
               s = lt(p.getId());
            }

            here.add(p.getId());
            s.seen = now;
            boolean on = h != null && !p.isSpectator() && enabled(h, "lightning_activation");
            boolean lit = on && unveiled(h);
            String path = h == null ? "" : pathOf(h);
            s.copy = path.endsWith("_am") || PassBridge.isAM(p.getUUID());
            s.color = auraOf(p);
            s.range = path.endsWith("_syn") ? 35.0D : 30.0D;
            if (lit && !s.lit) {
               s.onEdge = now;
               if (!Double.isNaN(s.onSince) && now - s.onSince > 40.0D) {
                  s.onSince = Double.NaN;
               }
            } else if (!lit && s.lit) {
               s.onSince = Double.NaN;
            }

            s.on = on;
            s.lit = lit;
            if (lit && Double.isNaN(s.onSince) && now - s.onEdge >= 3.0D) {
               s.onSince = now - 20.0D;
            }

            boolean surge = on && enabled(h, "surge_run");
            boolean beam = on && enabled(h, "beam");
            double[] feet = a(p.position());
            if (surge && !s.surging) {
               s.surgeStart = now;
               s.surgeEnd = -1.0D;
               s.hist.clear();
               s.histT.clear();
               s.base = 0;
               Vec3 look = p.getViewVector(1.0F);
               double[] fl = PassMath.norm(new double[]{look.x, 0.0D, look.z});
               ClientPass.Fx f = new ClientPass.Fx(3, p.getId(), 0, (double[])feet.clone(), fl, s.copy ? 1 : 0, s.color, now, 30.0D);
               f.fired = true;
               FX.add(f);
               if (near(feet)) {
                  sound("lt_surge", feet, 1.5F);
                  kick(feet, now, 0.5D, 40.0D, 8.0D);
               }
            } else if (!surge && s.surging) {
               s.surgeEnd = now;
            }

            s.surging = surge;
            if (surge) {
               s.hist.add(feet);
               s.histT.add(now);
               if (s.hist.size() > 256) {
                  int drop = s.hist.size() - 128;
                  s.hist.subList(0, drop).clear();
                  s.histT.subList(0, drop).clear();
                  s.base += drop;
               }

               double age = now - s.surgeStart;
               if ((long)age % 2L == 0L && !p.isInvisible()) {
                  PassBody b = PassBodies.last(p);
                  if (b != null && b.posed()) {
                     GHOSTS.add(new ClientPass.Ghost(b, now, pal(s.color, s.copy).main, s.copy));
                  }
               }
            } else if (s.surgeStart >= 0.0D && s.surgeEnd >= 0.0D && now - s.surgeEnd > 40.0D) {
               s.surgeStart = -1.0D;
               s.hist.clear();
               s.histT.clear();
               s.base = 0;
            }

            if (beam && !s.beaming) {
               s.beamStart = now;
               s.beamEnd = -1.0D;
               s.beamValue = 0.0D;
               if (near(feet)) {
                  sound("lt_beam", feet, 1.5F);
               }
            } else if (!beam && s.beaming) {
               s.beamEnd = now;
            }

            s.beaming = beam;
            if (s.beamStart >= 0.0D) {
               s.beamValue = s.beamEnd < 0.0D ? Math.min(1.0D, s.beamValue + 0.6D) : Math.max(0.0D, s.beamValue - 0.6D);
               if (s.beamEnd >= 0.0D && s.beamValue <= 0.0D) {
                  s.beamStart = -1.0D;
               }
            }

            float want = (float)Math.max(s.surging ? 0.8D : 0.0D, s.beaming ? 1.0D : 0.0D);
            if (want > 0.0F && near(feet)) {
               if (s.loop == null || s.loop.isStopped() || !alive(s.loop) && now - s.loopAt >= 20.0D) {
                  if (s.loop != null) {
                     s.loop.halt();
                  }

                  s.loop = new ClientPass.Loop(p.getId(), p, want);
                  s.loopAt = now;
                  mc.getSoundManager().play(s.loop);
               }

               s.loop.want = want;
            } else if (s.loop != null) {
               s.loop.want = 0.0F;
               if (!alive(s.loop)) {
                  s.loop.halt();
                  s.loop = null;
               }
            }
         } else if (s != null) {
            LT.remove(p.getId());
         }
      }

      Iterator<Map.Entry<Integer, ClientPass.Lt>> it = LT.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<Integer, ClientPass.Lt> e = (Map.Entry)it.next();
         ClientPass.Lt s = (ClientPass.Lt)e.getValue();
         if (!here.contains(e.getKey()) && now - s.seen > 40.0D) {
            if (s.loop != null) {
               s.loop.halt();
            }

            it.remove();
         } else if (!here.contains(e.getKey())) {
            if (s.surging) {
               s.surgeEnd = now;
            }

            if (s.beaming) {
               s.beamEnd = now;
            }

            s.on = false;
            s.lit = false;
            s.surging = false;
            s.beaming = false;
            if (s.loop != null) {
               s.loop.want = 0.0F;
            }
         }
      }

   }

   static boolean idle(ClientPass.Lt s) {
      return !s.on && Double.isNaN(s.onSince) && s.surgeStart < 0.0D && s.beamStart < 0.0D && !alive(s.loop);
   }

   static void tickBolts(ClientLevel level, double now) {
      Iterator<Map.Entry<Integer, ClientPass.Bolt>> it = BOLTS.entrySet().iterator();

      while(it.hasNext()) {
         Map.Entry<Integer, ClientPass.Bolt> en = (Map.Entry)it.next();
         ClientPass.Bolt b = (ClientPass.Bolt)en.getValue();
         if (b.dead < 0.0D) {
            Entity e = level.getEntity(en.getKey());
            b.prev = b.pos;
            if (e != null && !e.isRemoved()) {
               b.pos = a(e.position());
               b.vel = a(e.getDeltaMovement());
            } else {
               boolean wet = wet(level, b.pos, 0.25D, 0.5D);
               b.pos = PassMath.add(b.pos, b.vel);
               b.vel = PassMath.mul(b.vel, wet ? 0.8D : 0.99D);
            }

            b.hist.add(b.pos);
            ++b.ticks;
            if (b.ticks >= b.life) {
               b.dead = now;
            }
         } else if (now - b.dead > 2.0D) {
            it.remove();
         }
      }

   }

   static void want(ClientLevel level) {
      Set<Integer> w = PassBodies.WANT;
      w.clear();

      for(ClientPass.Lt s : LT.values()) {
         if (s.on) {
            w.add(s.id);
         }
      }

      for(ClientPass.Fx f : FX) {
         if (f.target != 0) {
            w.add(f.target);
         }

         if (f.caster != 0) {
            w.add(f.caster);
         }
      }

      ClientPassEd.want(w);
      ClientPassBr.want(w);
      ClientPassTm.want(w);
      ClientPassPd.want(w);
      ClientPassAm.want(w);
   }

   static double[][] points(Entity e, float pt) {
      return e != null && !firstSelf(e) && !e.isInvisible() ? PassBodies.get(e, pt).points() : null;
   }

   static LivingEntity living(ClientLevel level, int id) {
      if (id == 0) {
         return null;
      } else {
         Entity e = level.getEntity(id);
         if (e instanceof LivingEntity) {
            LivingEntity le = (LivingEntity)e;
            if (!e.isRemoved()) {
               return le;
            }
         }

         return null;
      }
   }

   static double[] beamHit(ClientLevel level, AbstractClientPlayer p, Vec3 eye, Vec3 dir, double range) {
      Vec3 end = eye.add(dir.scale(range));
      double d = range;
      boolean miss = true;
      double[] n = null;
      BlockHitResult bh = level.clip(new ClipContext(eye, end, Block.COLLIDER, Fluid.NONE, p));
      if (bh != null && bh.getType() != Type.MISS) {
         d = bh.getLocation().distanceTo(eye);
         miss = false;
         Direction f = bh.getDirection();
         n = new double[]{(double)f.getStepX(), (double)f.getStepY(), (double)f.getStepZ()};
      }

      Vec3 end2 = eye.add(dir.scale(d));
      EntityHitResult eh = ProjectileUtil.getEntityHitResult(p, eye, end2, (new AABB(eye, end2)).inflate(1.0D), (e) -> !e.isSpectator() && e.isPickable() && e != p, d * d);
      if (eh != null) {
         d = eh.getLocation().distanceTo(eye);
         miss = false;
         n = null;
      }

      return new double[]{d, miss ? 1.0D : 0.0D, n == null ? 0.0D : 1.0D, n == null ? 0.0D : n[0], n == null ? 0.0D : n[1], n == null ? 0.0D : n[2]};
   }

   static void drawPlayer(PassQuads q, ClientLevel level, AbstractClientPlayer p, ClientPass.Lt s, double t, float pt) {
      if (!p.isDeadOrDying()) {
         PassCast.Pal pal = pal(s.color, s.copy);
         boolean fp = firstSelf(p);
         boolean shown = !p.isInvisible() && !p.isSpectator();
         PassBody body = PassBodies.get(p, pt);
         double[][] pts = shown && !fp ? body.points() : null;
         double[][] hands = hands(p, pt, body);
         double[] dim = s.copy ? PassCast.SMOKE_DARK : null;
         boolean surgeOn = s.surgeStart >= 0.0D && s.surgeEnd < 0.0D;
         boolean beamOn = s.beamStart >= 0.0D && s.beamEnd < 0.0D;
         if (s.on && !Double.isNaN(s.onSince) && shown) {
            double on = t - s.onSince;
            double flare = PassLt.flareOf(beamOn, surgeOn);
            double k = PassMath.clamp01((on - 4.0D) / 6.0D);
            if (on >= 4.0D && !fp && body.posed() && s.copy && !ClientPassAm.eyes(p, k, flare)) {
               PassCast.eyesTrailer(q, body, PassCast.EMBER, k, flare);
            }

            if (!s.copy) {
               PassBridge.passFlare(p.getId(), flare, t);
            }

            PassLt.passive(q, pal, pal.main, on, flare, surgeOn, pts, hands, (PassBody)null, t, dim);
         }

         q.dim = dim;
         if (s.surgeStart >= 0.0D && !s.hist.isEmpty()) {
            int n = s.hist.size();
            int from = Math.max(0, n - 64);
            double[][] hist = new double[n - from][];
            double[] histT = new double[n - from];

            for(int i = from; i < n; ++i) {
               hist[i - from] = (double[])s.hist.get(i);
               histT[i - from] = s.histT.get(i);
            }

            double end = s.surgeEnd;
            PassLt.surge(q, pal, hist, histT, end, a(p.getPosition(pt)), t, pts, s.base + from);
         }

         if (s.beamStart >= 0.0D) {
            Vec3 eye = p.getEyePosition(pt);
            Vec3 dir = p.getViewVector(pt);
            double[] hit = beamHit(level, p, eye, dir, s.range);
            double[] cut = ClientPassBr.cut(level, a(eye), a(dir), hit, s.range, pt, t);
            PassFx.Ground g = (x, z, fromY) -> groundBelow(level, x, z, fromY);
            PassLt.beam(q, pal, g, s.beamStart, s.beamEnd, s.beamValue, cut != null ? cut[0] : hit[0], cut == null && hit[1] > 0.5D, cut != null ? new double[]{cut[1], cut[2], cut[3]} : null, a(eye), a(dir), t, fp, hands);
         }

         q.dim = null;
      }
   }

   static void drawFx(PassQuads q, ClientLevel level, ClientPass.Fx f, double t, float pt) {
      double age = t - f.born;
      if (!(age < 0.0D) && !(age > f.life)) {
         boolean copy = f.has(1);
         PassCast.Pal pal = pal(f.color, copy);
         q.dim = copy ? PassCast.SMOKE_DARK : null;
         q.air = f.air;
         if (f.kind == 0) {
            LivingEntity e = living(level, f.caster);
            double[] g = f.at;
            double[] l = light(level, g, pt);
            double[][] body = points(e, pt);
            if (f.has(2)) {
               double[] c = e != null ? PassMath.add(a(e.getPosition(pt)), new double[]{0.0D, 1.1D, 0.0D}) : new double[]{g[0], g[1] + 1.1D, g[2]};
               PassLt.burst(q, pal, l, g, c, age, body, t);
            } else {
               double[] head = e == null ? new double[]{g[0], g[1] + 1.9D, g[2]} : PassMath.add(a(e.getPosition(pt)), new double[]{0.0D, firstSelf(e) ? 4.2D : 1.9D, 0.0D});
               PassLt.strike(q, pal, l, g, head, age, body, t);
            }
         } else if (f.kind == 1) {
            LivingEntity e = living(level, f.target);
            double[][] body = e != null && e.isAlive() ? points(e, pt) : null;
            PassLt.hit(q, pal, light(level, f.at, pt), f.at, f.ground, age, body, (double)f.target, t);
         } else if (f.kind == 2) {
            Entity c = level.getEntity(f.caster);
            if (c == null) {
               q.dim = null;
               return;
            }

            LivingEntity tg = living(level, f.target);
            double[] palm = handOf(c, pt);
            double[] to = tg != null ? mid(tg, pt) : f.at;
            double[][] tgBody = tg != null && tg.isAlive() ? points(tg, pt) : null;
            double[] mark = null;
            if (tg != null) {
               Vec3 tp = tg.position();
               double gy = groundBelow(level, tp.x, tp.z, tp.y + 0.5D);
               double[] g = tp.y - gy <= 3.0D ? new double[]{tp.x, gy, tp.z} : null;
               if (age < 3.0D) {
                  f.mark = g;
               }

               mark = age < 3.0D ? g : f.mark;
            }

            PassLt.touch(q, pal, palm, to, age, tgBody, (double)f.target, mark, a(c.getPosition(pt)), t);
         } else if (f.kind == 3) {
            PassLt.surgeStart(q, pal, f.at, f.vec, age);
         }

         q.dim = null;
         q.air = false;
      }
   }

   static void drawBolt(PassQuads q, ClientLevel level, ClientPass.Bolt b, double t, float pt) {
      if (!(b.dead >= 0.0D) || !(Math.floor(t) > b.dead)) {
         boolean copy = (b.flags & 1) != 0;
         PassCast.Pal pal = pal(b.color, copy);
         double age = t - b.born;
         double[] p = PassMath.lerp(b.prev, b.pos, (double)pt);
         Entity c = level.getEntity(b.caster);
         double[] hnd = c != null ? handOf(c, pt) : (double[])b.hist.get(0);
         PassFx.Ground g = (x, z, fromY) -> groundBelow(level, x, z, fromY);
         q.dim = copy ? PassCast.SMOKE_DARK : null;
         PassLt.bolt(q, pal, g, p, (double[][])b.hist.toArray(new double[0][]), b.born, t, age, hnd);
         q.dim = null;
      }
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         Minecraft mc = Minecraft.getInstance();
         ClientLevel level = mc.level;
         if (level != null && level == where && (!FX.isEmpty() || !LT.isEmpty() || !BOLTS.isEmpty() || !GHOSTS.isEmpty() || !ClientPassEd.empty() || !ClientPassBr.empty() || !ClientPassTm.empty() || !ClientPassPd.empty() || !ClientPassAm.empty())) {
            float pt = event.getPartialTick();
            double t = (double)((float)level.getGameTime() + pt);
            Camera camera = event.getCamera();
            Vector3f l = camera.getLeftVector();
            Vector3f u = camera.getUpVector();
            double[] cam = a(camera.getPosition());
            PassQuads q = QUADS;
            q.begin(cam, new double[]{(double)(-l.x()), (double)(-l.y()), (double)(-l.z())}, new double[]{(double)u.x(), (double)u.y(), (double)u.z()});
            double r = 72.0D;

            try {
               for(ClientPass.Lt s : LT.values()) {
                  Entity e = level.getEntity(s.id);
                  if (e instanceof AbstractClientPlayer) {
                     AbstractClientPlayer p = (AbstractClientPlayer)e;
                     if (!p.isRemoved() && !(p.distanceToSqr(cam[0], cam[1], cam[2]) > r * r)) {
                        drawPlayer(q, level, p, s, t, pt);
                     }
                  }
               }

               for(ClientPass.Fx f : FX) {
                  if (near(f.at)) {
                     drawFx(q, level, f, t, pt);
                  }
               }

               for(ClientPass.Bolt b : BOLTS.values()) {
                  if (near(b.pos)) {
                     drawBolt(q, level, b, t, pt);
                  }
               }

               for(ClientPass.Ghost g : GHOSTS) {
                  double k = 1.0D - (t - g.born) / 10.0D;
                  if (k > 0.0D && near(g.body.pos)) {
                     q.dim = g.copy ? PassCast.SMOKE_DARK : null;
                     PassCast.ghost(q, g.body, g.rgb, 0.85D * k * k);
                  }
               }

               q.dim = null;
               ClientPassEd.draw(q, level, t, pt, cam, r);
               ClientPassBr.draw(q, level, t, pt, cam, r);
               ClientPassTm.draw(q, level, t, pt, cam, r);
               ClientPassPd.draw(q, level, t, pt, cam, r);
               ClientPassAm.draw(q, level, t, pt, cam, r);
            } catch (RuntimeException var17) {
               clearAll();
               return;
            }

            if (q.total() != 0) {
               PassDraw.normals(event.getPoseStack().last().normal());
               Matrix4f m = event.getPoseStack().last().pose();
               MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
               PassDraw.flush(q, buffers, m, cam, level);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
      if (PassBodies.inWorld && !FX.isEmpty()) {
         LivingEntity e = event.getEntity();
         float pt = event.getPartialTick();
         double t = (double)((float)e.level().getGameTime() + pt);
         double best = 0.0D;
         double[] rgb = null;

         for(ClientPass.Fx f : FX) {
            if (f.kind == 2 && f.target == e.getId()) {
               double age = t - f.born;
               if (!(age < 0.0D) && !(age >= 8.0D)) {
                  double k = 1.0D - age / 8.0D;
                  if (k * k > best) {
                     best = k * k;
                     rgb = PassLt.tint(pal(f.color, f.has(1)), age);
                  }
               }
            }
         }

         if (rgb != null && !(best <= 0.01D) && !e.isInvisible() && PassBodies.upright(e, pt)) {
            try {
               EntityModel<?> model = event.getRenderer().getModel();
               PoseStack ps = event.getPoseStack();
               float scale = PassBridge.scaleOf(e) * 1.01F;
               ps.pushPose();

               try {
                  ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - PassBodies.bodyYaw(e, pt)));
                  ps.scale(-scale, -scale, scale);
                  ps.translate(0.0D, -1.501D, 0.0D);
                  float k = (float)(0.6D * best);
                  model.renderToBuffer(ps, event.getMultiBufferSource().getBuffer(RenderType.eyes(WHITE)), 15728880, OverlayTexture.NO_OVERLAY, (float)rgb[0] * k, (float)rgb[1] * k, (float)rgb[2] * k, 1.0F);
               } finally {
                  ps.popPose();
               }
            } catch (Throwable var18) {
               FX.removeIf((f) -> f.kind == 2);
            }

         }
      }
   }

   @SubscribeEvent
   public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
      if (!KICKS.isEmpty() && PassBridge.screenEffects()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            double tt = (double)mc.level.getGameTime() + event.getPartialTick();
            Vec3 pos = event.getCamera().getPosition();
            double amp = 0.0D;
            double roll = 0.0D;

            for(ClientPass.Kick k : KICKS) {
               double age = tt - k.born;
               if (!(age < 0.0D) && !(age > k.life)) {
                  double dd = pos.distanceTo(new Vec3(k.at[0], k.at[1], k.at[2]));
                  double near = k.reach * 0.25D;
                  double fall = dd <= near ? 1.0D : PassMath.clamp01(1.0D - (dd - near) / (k.reach - near));
                  double e2 = Math.pow(1.0D - age / k.life, 2.0D) * k.k * fall;
                  amp += e2 * 0.16D;
                  roll += e2 * 1.2D * Math.sin(age * 2.1D + k.born);
               }
            }

            if (!(amp <= 0.0D) || roll != 0.0D) {
               double deg = amp * 6.0D;
               event.setRoll((float)((double)event.getRoll() + roll));
               event.setPitch((float)((double)event.getPitch() + deg * Math.sin(tt * 3.3D + 1.0D)));
               event.setYaw((float)((double)event.getYaw() + deg * Math.sin(tt * 2.7D)));
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void onFogColor(ViewportEvent.ComputeFogColor event) {
      double k = Math.min(0.8D, glare * 0.8D);
      if (!(k <= 0.01D) && PassBridge.screenEffects() && flashes()) {
         double[] c = glareRgb;
         event.setRed((float)((double)event.getRed() + (c[0] - (double)event.getRed()) * k));
         event.setGreen((float)((double)event.getGreen() + (c[1] - (double)event.getGreen()) * k));
         event.setBlue((float)((double)event.getBlue() + (c[2] - (double)event.getBlue()) * k));
      }
   }

   @SubscribeEvent
   public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
      clearAll();
      PENDING.clear();
      PassBridge.clear();
   }

   static final class Bolt {
      final int caster;
      final int flags;
      final int color;
      final double born;
      int life;
      double[] pos;
      double[] prev;
      double[] vel;
      final List<double[]> hist = new ArrayList();
      double dead = -1.0D;
      int ticks;

      Bolt(int caster, int flags, int color, double born, int life, double[] pos, double[] vel) {
         this.caster = caster;
         this.flags = flags;
         this.color = color;
         this.born = born;
         this.life = life;
         this.pos = pos;
         this.prev = pos;
         this.vel = vel;
         this.hist.add(pos);
      }

      void land(double[] at) {
         double[] start = (double[])this.hist.get(0);
         double[] dir = PassMath.norm(PassMath.sub(at, start));
         double reach = PassMath.dot(PassMath.sub(at, start), dir);

         while(this.hist.size() > 1 && PassMath.dot(PassMath.sub((double[])this.hist.get(this.hist.size() - 1), start), dir) >= reach) {
            this.hist.remove(this.hist.size() - 1);
         }

         this.prev = (double[])this.hist.get(this.hist.size() - 1);
         this.pos = at;
         this.hist.add(at);
      }
   }

   static final class Fx {
      final int kind;
      final int caster;
      final int target;
      final double[] at;
      final double[] vec;
      final int flags;
      final int color;
      final double born;
      final double life;
      double ground = Double.NaN;
      double[] mark;
      boolean fired;
      boolean air;

      Fx(int kind, int caster, int target, double[] at, double[] vec, int flags, int color, double born, double life) {
         this.kind = kind;
         this.caster = caster;
         this.target = target;
         this.at = at;
         this.vec = vec;
         this.flags = flags;
         this.color = color;
         this.born = born;
         this.life = life;
      }

      boolean has(int f) {
         return (this.flags & f) != 0;
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

   static final class Kick {
      final double[] at;
      final double born;
      final double k;
      final double reach;
      final double life;

      Kick(double[] at, double born, double k, double reach, double life) {
         this.at = at;
         this.born = born;
         this.k = k;
         this.reach = reach;
         this.life = life;
      }
   }

   static final class Loop extends AbstractTickableSoundInstance {
      final int id;
      float want;

      Loop(int id, Entity e, float want) {
         this(PassBridge.sound("unrp:pass_crackle"), id, e, want);
      }

      Loop(SoundEvent sound, int id, Entity e, float want) {
         super(sound, SoundSource.PLAYERS, RandomSource.create());
         this.id = id;
         this.want = want;
         this.looping = true;
         this.delay = 0;
         this.volume = Math.max(0.05F, want);
         this.pitch = 1.0F;
         this.x = e.getX();
         this.y = e.getY() + 1.0D;
         this.z = e.getZ();
      }

      void halt() {
         this.stop();
      }

      public void tick() {
         Minecraft mc = Minecraft.getInstance();
         Entity e = mc.level == null ? null : mc.level.getEntity(this.id);
         if (e != null && !e.isRemoved()) {
            this.x = e.getX();
            this.y = e.getY() + 1.0D;
            this.z = e.getZ();
            this.volume += (this.want - this.volume) * 0.25F;
            if (this.want <= 0.0F && this.volume < 0.03F) {
               this.stop();
            }

         } else {
            this.stop();
         }
      }
   }

   static final class Lt {
      final int id;
      boolean on;
      boolean lit;
      double onSince = Double.NaN;
      double onEdge = Double.NaN;
      boolean copy;
      int color = 4568319;
      double range = 30.0D;
      boolean surging;
      double surgeStart = -1.0D;
      double surgeEnd = -1.0D;
      int base;
      final List<double[]> hist = new ArrayList();
      final List<Double> histT = new ArrayList();
      boolean beaming;
      double beamStart = -1.0D;
      double beamEnd = -1.0D;
      double beamValue;
      ClientPass.Loop loop;
      double loopAt = -1.0E9D;
      double seen;

      Lt(int id) {
         this.id = id;
      }
   }

   static final class Queued {
      final ClientLevel level;
      final PassFxPacket msg;

      Queued(ClientLevel level, PassFxPacket msg) {
         this.level = level;
         this.msg = msg;
      }
   }
}
