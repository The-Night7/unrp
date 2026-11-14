package com.myriam.unrp.uo;



import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public final class OriginLooks {
   public static final double SEEN = 96.0D;
   public static final double HOLD_SEEN = 48.0D;
   public static final int ASCEND_SHOWN = 60;
   public static final int COPY_TICKS = 12;
   public static final int STILLNESS_TICKS = 30;
   public static final int NOVA_TICKS = 110;
   public static final double NOVA_GROUND = 3.0D;
   public static final int RIP_TICKS = 44;
   public static final int IMBUE_TICKS = 50;

   private OriginLooks() {
   }

   static void send(ServerLevel level, Vec3 near, double seen, OriginLookPacket msg) {
      double s2 = seen * seen;

      for(ServerPlayer p : level.players()) {
         if (p.position().distanceToSqr(near) <= s2) {
            com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), msg);
         }
      }

   }

   static OriginLookPacket look(int kind, int entity, double[] v, int life) {
      return new OriginLookPacket(kind, entity, v, new ArrayList(), life);
   }

   static void stone(ServerLevel level, Entity e, int left) {
      send(level, e.position(), 96.0D, look(17, e.getId(), new double[0], Math.max(0, left)));
   }

   static void held(ServerLevel level, Entity e, int left) {
      send(level, e.position(), 96.0D, look(19, e.getId(), new double[0], Math.max(0, left)));
   }

   static void eyes(ServerPlayer owner, int entity, int left) {
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> owner), look(18, entity, new double[0], Math.max(0, left)));
   }

   static void colossus(ServerPlayer player, int tier, int left) {
      send(player.serverLevel(), player.position(), 96.0D, look(1, player.getId(), new double[]{(double)tier}, Math.max(0, left)));
   }

   static void shell(ServerPlayer player, int left) {
      send(player.serverLevel(), player.position(), 96.0D, look(2, player.getId(), new double[0], Math.max(0, left)));
   }

   static void banners(ServerPlayer player) {
      Vec3 at = player.position();
      send(player.serverLevel(), at, 96.0D, look(3, player.getId(), new double[]{at.x, at.y, at.z}, 56));
   }

   static void record(ServerPlayer viewer, ServerPlayer member, String text) {
      List<String> names = new ArrayList();
      names.add(text);
      com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), new OriginLookPacket(4, member.getId(), new double[0], names, 70));
   }

   static void hold(ServerLevel level, Vec3 at, int ticks) {
      send(level, at, 48.0D, look(5, -1, new double[]{at.x, at.y, at.z}, ticks));
   }

   static void balance(ServerPlayer player, int stage, boolean even) {
      Vec3 at = player.position();
      send(player.serverLevel(), at, 96.0D, look(6, player.getId(), new double[]{at.x, at.y, at.z, (double)stage, even ? 1.0D : 0.0D}, even ? 54 : 44));
   }

   static void ascend(ServerPlayer player, int count) {
      send(player.serverLevel(), player.position(), 96.0D, look(8, player.getId(), new double[]{(double)count}, 60));
   }

   static void afterimage(ServerPlayer player, Vec3 at, float yaw, int rgb) {
      send(player.serverLevel(), at, 96.0D, look(9, player.getId(), new double[]{at.x, at.y, at.z, (double)yaw, (double)rgb}, 12));
   }

   static void counter(ServerPlayer player, int left) {
      send(player.serverLevel(), player.position(), 96.0D, look(10, player.getId(), new double[0], Math.max(0, left)));
   }

   static void stillness(ServerPlayer player) {
      Vec3 at = player.position();
      send(player.serverLevel(), at, 96.0D, look(11, player.getId(), new double[]{at.x, at.y, at.z}, 30));
   }

   static void nova(ServerPlayer player, Vec3 core, int rgb, double reach, double ground) {
      Vec3 at = player.position();
      send(player.serverLevel(), at, 96.0D, look(12, player.getId(), new double[]{at.x, at.y, at.z, core.x, core.y, core.z, (double)rgb, reach, ground}, 110));
   }

   static void seal(ServerLevel level, Vec3 at, ServerPlayer signer, ServerPlayer keeper, boolean soul, int lines, int seed) {
      Vec3 s = signer == null ? at : signer.position().add(0.0D, 1.1D, 0.0D);
      Vec3 k = keeper == null ? at : keeper.position().add(0.0D, 1.1D, 0.0D);
      send(level, at, 96.0D, look(13, keeper == null ? -1 : keeper.getId(), new double[]{at.x, at.y, at.z, signer == null ? -1.0D : (double)signer.getId(), soul ? 1.0D : 0.0D, (double)lines, (double)seed, s.x, s.y, s.z, k.x, k.y, k.z}, OriginLookPacket.sealLife(lines)));
   }

   static void rip(ServerLevel level, ServerPlayer keeper, ServerPlayer victim, Vec3 hand) {
      send(level, victim.position(), 96.0D, look(14, victim.getId(), new double[]{(double)keeper.getId(), hand.x, hand.y, hand.z}, 44));
   }

   static void imbue(ServerPlayer keeper, ServerPlayer ally, boolean near, boolean spared) {
      Vec3 a = ally.position().add(0.0D, 1.1D, 0.0D);
      send(keeper.serverLevel(), keeper.position(), 96.0D, look(15, keeper.getId(), new double[]{near ? (double)ally.getId() : -1.0D, a.x, a.y, a.z, spared ? 1.0D : 0.0D}, 50));
   }

   static void borrowed(ServerPlayer keeper, UUID ally, String power, int ticks, int delay) {
      List<String> names = new ArrayList();
      if (ally != null && ticks > 0) {
         names.add(ally.toString());
         names.add(power == null ? "" : power);
      }

      send(keeper.serverLevel(), keeper.position(), 96.0D, new OriginLookPacket(16, keeper.getId(), new double[]{(double)Math.max(0, delay)}, names, Math.max(0, ticks)));
   }

   static void sarcHold(ServerPlayer caster, Entity held, int left) {
      send(caster.serverLevel(), caster.position(), 96.0D, look(20, caster.getId(), new double[]{held == null ? -1.0D : (double)held.getId()}, held == null ? 0 : Math.max(0, left)));
   }

   static void phage(ServerPlayer player, int move, int ticks, double extra) {
      send(player.serverLevel(), player.position(), 96.0D, look(21, player.getId(), new double[]{(double)move, extra}, Math.max(0, ticks)));
   }

   static void scar(ServerLevel level, Vec3 from, Vec3 to) {
      send(level, from.add(to).scale(0.5D), 96.0D + from.distanceTo(to) * 0.5D, look(7, -1, new double[]{from.x, from.y, from.z, to.x, to.y, to.z}, 20));
   }
}
