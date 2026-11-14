package com.myriam.unrp.pass;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;
import net.threetag.palladium.entity.CustomProjectile;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "unrp")
public final class PassEvents {
   public static final double SEEN = 72.0D;
   public static final String BOLT_TAG = "unrp_lightning_bolt";
   public static final int BOLT_LIFE = 15;
   public static final String AM_TAG = "unrp.has.aura_manipulation";
   public static final String COPY_TAG = "unrp.am_black";
   public static final String SEEN_KEY = "unrp_pass_seen";
   public static final String COPY_KEY = "unrp_pass_copy";
   public static final String COLOR_KEY = "unrp_pass_color";
   public static final String ED_TAG = "unrp_ed_sphere";
   public static final String ENDED_KEY = "unrp_pass_ended";
   public static final int ED_LIFE = 100;
   public static final int PARK_AFTER = 2;
   public static final double FACE_OFF = 0.05D;
   public static final double CEILING_DROP = 1.4D;
   static final Map<Integer, PassEvents.Flight> FLIGHTS = new HashMap();

   private PassEvents() {
   }

   public static boolean copy(Entity e) {
      return e != null && (e.getTags().contains("unrp.has.aura_manipulation") || e.getTags().contains("unrp.am_black"));
   }

   public static int color(Entity e) {
      return copy(e) ? 0 : PassServer.color(e);
   }

   static double[] at(Vec3 v) {
      return new double[]{v.x, v.y, v.z};
   }

   public static void send(ServerLevel level, double[] at, PassFxPacket pkt) {
      double r = 72.0D;

      for(ServerPlayer o : level.players()) {
         if (o.distanceToSqr(at[0], at[1], at[2]) <= r * r) {
            com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> o), pkt);
         }
      }

   }

   public static void lightning(ServerLevel level, int op, Entity caster, Entity target, int aux, int extra, Vec3 pos, Vec3 vec) {
      int flags = extra | (copy(caster) ? 1 : 0) | (target != null ? 8 : 0);
      double[] v = new double[]{pos.x, pos.y, pos.z, vec == null ? 0.0D : vec.x, vec == null ? 0.0D : vec.y, vec == null ? 0.0D : vec.z};
      send(level, at(pos), new PassFxPacket(op, caster == null ? 0 : caster.getId(), target == null ? 0 : target.getId(), aux, flags, color(caster), v));
   }

   @SubscribeEvent
   public static void onJoin(EntityJoinLevelEvent event) {
      if (!event.getLevel().isClientSide()) {
         Level lv_bolt = event.getLevel();
         if (lv_bolt instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)lv_bolt;
            Entity var4 = event.getEntity();
            if (var4 instanceof Projectile) {
               Projectile pr = (Projectile)var4;
               boolean bolt = pr.getTags().contains("unrp_lightning_bolt");
               if (!bolt && !pr.getTags().contains("unrp_ed_sphere")) {
                  return;
               }

               if (event.loadedFromDisk()) {
                  resend(level, pr, bolt);
               } else if (bolt) {
                  bolt(level, pr, false);
               } else {
                  orb(level, pr, false);
               }

               return;
            }
         }
      }

   }

   static void resend(ServerLevel level, Projectile pr, boolean bolt) {
      keep(pr);
      send(level, at(pr.position()), flight(pr, bolt, 0, 32, false));
      FLIGHTS.put(pr.getId(), new PassEvents.Flight(pr, level, bolt));
   }

   public static void redirect(ServerLevel level, Projectile pr) {
      if (pr.getTags().contains("unrp_lightning_bolt")) {
         bolt(level, pr, true);
      } else if (pr.getTags().contains("unrp_ed_sphere")) {
         orb(level, pr, true);
      }

   }

   static void keep(Projectile pr) {
      CompoundTag pd = pr.getPersistentData();
      if (!pd.getBoolean("unrp_pass_seen")) {
         Entity owner = pr.getOwner();
         pd.putBoolean("unrp_pass_seen", true);
         pd.putBoolean("unrp_pass_copy", copy(owner));
         pd.putInt("unrp_pass_color", color(owner));
      }

   }

   static void orb(ServerLevel level, Projectile pr, boolean redirect) {
      boolean first = !pr.getPersistentData().getBoolean("unrp_pass_seen");
      keep(pr);
      Entity owner = pr.getOwner();
      boolean thrown = first && !redirect;
      send(level, at(pr.position()), flight(pr, false, thrown && owner != null ? owner.getId() : 0, 0, false));
      FLIGHTS.put(pr.getId(), new PassEvents.Flight(pr, level, false));
      if (thrown && owner instanceof ServerPlayer sp) {
         com.myriam.unrp.ability.Poses.play(sp, "ed_hurl");
      }

   }

   static PassFxPacket flight(Projectile pr, boolean bolt, int caster, int extra, boolean parked) {
      CompoundTag pd;
      int var10000;
      label40: {
         pd = pr.getPersistentData();
         int most = bolt ? 15 : 100;
         if (pr instanceof CustomProjectile cp) {
            if (cp.lifetime > 0) {
               var10000 = Math.max(1, cp.lifetime - pr.tickCount);
               break label40;
            }
         }

         var10000 = most;
      }

      int life = var10000;
      Vec3 pos = pr.position();
      Vec3 vel = parked ? Vec3.ZERO : pr.getDeltaMovement();
      int flags = extra | (pd.getBoolean("unrp_pass_copy") ? 1 : 0) | (!parked && !pr.isNoGravity() ? 0 : 16);
      return new PassFxPacket(bolt ? 1 : 6, caster, life, pr.getId(), flags, pd.getInt("unrp_pass_color"), new double[]{pos.x, pos.y, pos.z, vel.x, vel.y, vel.z});
   }

   static boolean drifted(PassEvents.Flight s) {
      Projectile pr = s.pr;
      double var10000;
      if (pr.isNoGravity()) {
         var10000 = 0.0D;
      } else if (pr instanceof CustomProjectile) {
         CustomProjectile cp = (CustomProjectile)pr;
         var10000 = (double)cp.gravity;
      } else {
         var10000 = 0.03D;
      }

      double g = var10000;
      Vec3 next = s.vel.scale(pr.isInWater() ? 0.8D : 0.99D).subtract(0.0D, g, 0.0D);
      return pr.position().distanceToSqr(s.pos.add(s.vel)) > 1.0E-6D || pr.getDeltaMovement().distanceToSqr(next) > 1.0E-8D;
   }

   static void flights() {
      Iterator<PassEvents.Flight> it = FLIGHTS.values().iterator();

      while(it.hasNext()) {
         PassEvents.Flight s = (PassEvents.Flight)it.next();
         Projectile pr = s.pr;
         if (!pr.isRemoved() && pr.level() == s.level) {
            boolean ticked = pr.tickCount != s.ticks;
            s.idle = ticked ? 0 : s.idle + 1;
            boolean park = s.idle >= 2;
            if (park != s.parked || ticked && drifted(s)) {
               send(s.level, at(pr.position()), flight(pr, s.bolt, 0, 32, park));
               s.parked = park;
            }

            s.ticks = pr.tickCount;
            s.pos = pr.position();
            s.vel = pr.getDeltaMovement();
         } else {
            if (!pr.getPersistentData().getBoolean("unrp_pass_ended")) {
               send(s.level, at(s.pos), new PassFxPacket(12, 0, 0, pr.getId(), 0, 0, at(s.pos)));
            }

            it.remove();
         }
      }

   }

   public static Vec3 off(HitResult hit) {
      Vec3 l = hit.getLocation();
      if (hit instanceof BlockHitResult bh) {
         if (hit.getType() == Type.BLOCK) {
            return l.add((double)bh.getDirection().getStepX() * 0.05D, (double)bh.getDirection().getStepY() * 0.05D, (double)bh.getDirection().getStepZ() * 0.05D);
         }
      }

      return l;
   }

   static Vec3 base(HitResult hit) {
      Vec3 o = off(hit);
      if (hit instanceof BlockHitResult bh) {
         if (bh.getDirection().getStepY() < 0) {
            return o.subtract(0.0D, 1.4D, 0.0D);
         }
      }

      return o;
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void onImpact(ProjectileImpactEvent event) {
      Projectile pr = event.getProjectile();
      if (pr != null && pr.getTags().contains("unrp_ed_sphere")) {
         Level lv_hit = pr.level();
         if (lv_hit instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)lv_hit;
            HitResult hit = event.getRayTraceResult();
            if (hit != null && hit.getType() != Type.MISS) {
               Entity var10000;
               if (hit instanceof EntityHitResult) {
                  EntityHitResult ehr = (EntityHitResult)hit;
                  var10000 = ehr.getEntity();
               } else {
                  var10000 = null;
               }

               Entity struck = var10000;
               if (struck != null && struck == pr.getOwner() && pr instanceof CustomProjectile) {
                  CustomProjectile cp = (CustomProjectile)pr;
                  if (cp.preventShooterInteraction) {
                     return;
                  }
               }

               blast(level, pr, struck, struck != null ? pr.position() : base(hit));
               return;
            }

            return;
         }
      }

   }

   public static void blast(ServerLevel level, Projectile pr, Entity struck, Vec3 pos) {
      keep(pr);
      CompoundTag pd = pr.getPersistentData();
      pd.putBoolean("unrp_pass_ended", true);
      Entity owner = pr.getOwner();
      int flags = (pd.getBoolean("unrp_pass_copy") ? 1 : 0) | (struck != null ? 8 : 0);
      Vec3 c = new Vec3(pr.getX(), pr.getEyeY(), pr.getZ());
      send(level, at(pos), new PassFxPacket(7, owner == null ? 0 : owner.getId(), struck == null ? 0 : struck.getId(), pr.getId(), flags, pd.getInt("unrp_pass_color"), new double[]{pos.x, pos.y, pos.z, c.x, c.y, c.z}));
   }

   public static void ed(ServerLevel level, int op, Entity caster, Vec3 pos, Vec3 vec) {
      lightning(level, op, caster, (Entity)null, 0, 0, pos, vec);
   }

   static void bolt(ServerLevel level, Projectile pr, boolean redirect) {
      CompoundTag pd = pr.getPersistentData();
      Entity owner = pr.getOwner();
      boolean first = !pd.getBoolean("unrp_pass_seen");
      if (first) {
         pd.putBoolean("unrp_pass_seen", true);
         pd.putBoolean("unrp_pass_copy", copy(owner));
         pd.putInt("unrp_pass_color", color(owner));
      }

      boolean thrown = first && !redirect;
      send(level, at(pr.position()), flight(pr, true, thrown && owner != null ? owner.getId() : 0, 0, false));
      FLIGHTS.put(pr.getId(), new PassEvents.Flight(pr, level, true));
      if (thrown && owner instanceof ServerPlayer sp) {
         com.myriam.unrp.ability.Poses.play(sp, "lt_cast");
      }

   }

   public static void boltHit(ServerLevel level, Projectile pr, Entity target, Vec3 pos) {
      CompoundTag pd = pr.getPersistentData();
      pd.putBoolean("unrp_pass_ended", true);
      Entity owner = pr.getOwner();
      boolean seen = pd.getBoolean("unrp_pass_seen");
      boolean dark = seen ? pd.getBoolean("unrp_pass_copy") : copy(owner);
      int flags = (dark ? 1 : 0) | (target != null ? 8 : 0);
      send(level, at(pos), new PassFxPacket(2, owner == null ? 0 : owner.getId(), target == null ? 0 : target.getId(), pr.getId(), flags, seen ? pd.getInt("unrp_pass_color") : color(owner), new double[]{pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D}));
   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END && !FLIGHTS.isEmpty()) {
         flights();
      }

   }

   @SubscribeEvent
   public static void onStartTracking(PlayerEvent.StartTracking event) {
      Entity var3 = event.getTarget();
      PassEvents.Flight var10000;
      if (var3 instanceof Projectile pr) {
         var10000 = (PassEvents.Flight)FLIGHTS.get(pr.getId());
      } else {
         var10000 = null;
      }

      PassEvents.Flight f = var10000;
      Player var5 = event.getEntity();
      if (var5 instanceof ServerPlayer watcher) {
         if (f != null && f.pr == event.getTarget() && !f.pr.isRemoved()) {
            com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> watcher), flight(f.pr, f.bolt, 0, 32, f.parked));
         }
      }

   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      FLIGHTS.clear();
   }

   static final class Flight {
      final Projectile pr;
      final ServerLevel level;
      final boolean bolt;
      Vec3 pos;
      Vec3 vel;
      int ticks;
      int idle;
      boolean parked;

      Flight(Projectile pr, ServerLevel level, boolean bolt) {
         this.pr = pr;
         this.level = level;
         this.bolt = bolt;
         this.pos = pr.position();
         this.vel = pr.getDeltaMovement();
         this.ticks = pr.tickCount;
      }
   }
}
