package com.myriam.unrp.pass;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class BarrierReflect {
   public static final float SHARE = 0.5F;
   public static final float LEAST = 1.0F;
   public static final float MOST_BASE = 4.0F;
   public static final float MOST_STEP = 1.0F;
   public static final double REACH = 3.0D;
   public static final double SLACK = 1.0D;
   public static final String SANCTUARY = "unrp.sanctuary";
   public static final String TOLD = "§bThe barrier reflects your attack.";
   private static boolean turning;

   private BarrierReflect() {
   }

   public static float most(float level) {
      float l = level >= 1.0F ? Math.min(level, 10.0F) : 1.0F;
      return 4.0F + 1.0F * l;
   }

   public static float amount(float blow, float stood, float level) {
      float stopped = Math.min(blow, stood);
      return !(stopped > 0.0F) ? 0.0F : Math.min(stopped, Math.min(most(level), Math.max(1.0F, stopped * 0.5F)));
   }

   public static boolean turns(boolean sanctuary, boolean turned, boolean living, boolean owner, boolean ally, boolean inside) {
      return !sanctuary && !turned && living && !owner && !ally && !inside;
   }

   public static float punch(float damage, float strength) {
      float s = strength >= 0.0F ? Math.min(strength, 1.0F) : 0.0F;
      return Math.max(1.0F, damage * (0.2F + s * s * 0.8F));
   }

   public static double entry(double ex, double ey, double ez, double dx, double dy, double dz, double cx, double cy, double cz, double r) {
      double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (len > 1.0E-9D && r > 0.0D) {
         double ux = dx / len;
         double uy = dy / len;
         double uz = dz / len;
         double ox = ex - cx;
         double oy = ey - cy;
         double oz = ez - cz;
         double c = ox * ox + oy * oy + oz * oz - r * r;
         if (c <= 0.0D) {
            return -1.0D;
         } else {
            double b = ox * ux + oy * uy + oz * uz;
            double disc = b * b - c;
            if (disc < 0.0D) {
               return -1.0D;
            } else {
               double t = -b - Math.sqrt(disc);
               return t > 0.0D ? t : -1.0D;
            }
         }
      } else {
         return -1.0D;
      }
   }

   static boolean friend(ServerLevel level, Entity by, ServerPlayer owner, UUID own) {
      if (by == null) {
         return false;
      } else if (owner != null && com.myriam.unrp.social.SocialService.allied(by, owner)) {
         return true;
      } else {
         return own != null && by instanceof ServerPlayer && com.myriam.unrp.social.SocialService.sameGroup(level.getServer(), by.getUUID(), own);
      }
   }

   public static void turn(BarrierEntity b, DamageSource source, float blow, float stood) {
      if (!turning) {
         Level lv_by = b.level();
         if (lv_by instanceof ServerLevel) {
            ServerLevel level;
            Entity var15;
            UUID own;
            ServerPlayer owner;
            boolean var10000;
            label146: {
               level = (ServerLevel)lv_by;
               var15 = source.getEntity();
               own = b.getOwnerId();
               owner = own == null ? null : level.getServer().getPlayerList().getPlayer(own);
               if (var15 instanceof LivingEntity) {
                  LivingEntity alive = (LivingEntity)var15;
                  if (alive.isAlive()) {
                     var10000 = true;
                     break label146;
                  }
               }

               var10000 = false;
            }

            boolean living = var10000;
            if (!turns(b.getTags().contains("unrp.sanctuary"), source.is(DamageTypes.THORNS), living, var15 != null && var15.getUUID().equals(own), friend(level, var15, owner, own), var15 != null && b.isInside(var15))) {
               return;
            }

            LivingEntity striker = (LivingEntity)var15;
            float back = amount(blow, stood, owner == null ? 1.0F : BarrierEvents.levelOf(owner));
            if (!(back > 0.0F)) {
               return;
            }

            turning = true;

            try {
               if (striker.hurt(level.damageSources().thorns((Entity)(owner != null ? owner : b)), back)) {
                  BarrierPass.reflect(b, striker);
                  if (striker instanceof ServerPlayer) {
                     ServerPlayer told = (ServerPlayer)striker;
                     told.displayClientMessage(Component.literal("§bThe barrier reflects your attack."), true);
                  }
               }
            } finally {
               turning = false;
            }

            return;
         }
      }

   }
}
