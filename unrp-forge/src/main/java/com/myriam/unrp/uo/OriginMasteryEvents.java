package com.myriam.unrp.uo;



import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public class OriginMasteryEvents {
   public static final String PENDING = "unrp.origin_mxp";
   public static final int HIT_MOST = 12;
   public static final int KILL_MOB = 30;
   public static final int KILL_PLAYER = 60;
   public static final int PENDING_MOST = 600;
   private static final String[] ORIGINS = new String[]{"edict", "limit_breaker", "wyrm_engine", "singularity", "sarcophage", "crimson_mark", "dealmaker", "hollow_mark", "counterweight", "nadir", "chained_one"};
   private static ServerPlayer ambient;
   private static int quiet;

   public static boolean holds(ServerPlayer player) {
      Set<String> tags = player.getTags();

      for(String origin : ORIGINS) {
         if (tags.contains("unrp.has." + origin)) {
            return true;
         }
      }

      return false;
   }

   public static int hitXp(float amount) {
      return amount > 0.0F && !Float.isInfinite(amount) ? Math.min(12, (int)Math.ceil((double)(amount / 2.0F))) : 0;
   }

   public static int pendingAfter(int pending, int add) {
      long sum = (long)Math.max(0, pending) + (long)Math.max(0, add);
      return (int)Math.min(600L, sum);
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public static void onTickOpen(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.START) {
         Player var2 = event.player;
         if (var2 instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)var2;
            ambient = holds(player) ? player : null;
         }
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onTickClose(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.END && event.player instanceof ServerPlayer) {
         ambient = null;
      }

   }

   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == Phase.END) {
         ambient = null;
         quiet = 0;
      }

   }

   private static ServerPlayer attackerOf(DamageSource source) {
      Entity by = source.getEntity();
      if (by instanceof ServerPlayer) {
         return (ServerPlayer)by;
      } else {
         return by == null && quiet == 0 ? ambient : null;
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onDealt(LivingHurtEvent event) {
      LivingEntity victim = event.getEntity();
      if (!victim.level().isClientSide()) {
         ServerPlayer attacker = attackerOf(event.getSource());
         if (attacker != null && victim != attacker && holds(attacker) && OriginVesselEvents.meaningful(victim)) {
            add(attacker, hitXp(event.getAmount()));
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onKill(LivingDeathEvent event) {
      LivingEntity victim = event.getEntity();
      if (!victim.level().isClientSide()) {
         ServerPlayer attacker = attackerOf(event.getSource());
         if (attacker != null && victim != attacker && holds(attacker) && OriginVesselEvents.meaningful(victim)) {
            add(attacker, victim instanceof Player ? 60 : 30);
         }
      }
   }

   public static boolean hurt(ServerPlayer caster, LivingEntity victim, DamageSource source, float amount) {
      return source.getEntity() != null ? victim.hurt(source, amount) : landed(caster, victim, amount, () -> victim.hurt(source, amount));
   }

   public static boolean landed(ServerPlayer caster, LivingEntity victim, float amount, BooleanSupplier hit) {
      boolean alive = victim.isAlive();
      ++quiet;

      boolean struck;
      try {
         struck = hit.getAsBoolean();
      } finally {
         --quiet;
      }

      if (struck) {
         credit(caster, victim, amount, alive && !victim.isAlive());
      }

      return struck;
   }

   public static boolean hurtBy(UUID owner, LivingEntity victim, DamageSource source, float amount) {
      MinecraftServer server = victim.getServer();
      ServerPlayer player = owner != null && server != null ? server.getPlayerList().getPlayer(owner) : null;
      return hurt(player, victim, source, amount);
   }

   public static void credit(ServerPlayer player, LivingEntity victim, float amount, boolean killed) {
      if (player != null && victim != null && victim != player && holds(player) && OriginVesselEvents.meaningful(victim)) {
         int xp = hitXp(amount);
         if (killed) {
            xp += victim instanceof Player ? 60 : 30;
         }

         add(player, xp);
      }
   }

   private static void add(ServerPlayer player, int xp) {
      if (xp > 0) {
         PointsManager.setScore(player, "unrp.origin_mxp", pendingAfter(PointsManager.score(player, "unrp.origin_mxp"), xp));
      }
   }
}
