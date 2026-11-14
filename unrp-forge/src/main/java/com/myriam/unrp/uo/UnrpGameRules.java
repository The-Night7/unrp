package com.myriam.unrp.uo;



import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.Category;

public class UnrpGameRules {
   public static final GameRules.Key<GameRules.BooleanValue> POWERED_MOBS = GameRules.register("unrpPoweredMobs", Category.SPAWNING, BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> DISTORTION_WHISPERS = GameRules.register("unrpDistortionWhispers", Category.PLAYER, BooleanValue.create(false));
   public static final GameRules.Key<GameRules.BooleanValue> DISTORTION_SCREAMERS = GameRules.register("unrpDistortionScreamers", Category.PLAYER, BooleanValue.create(false));
   public static final GameRules.Key<GameRules.BooleanValue> REAL_CRATERS = GameRules.register("unrpRealCraters", Category.UPDATES, BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> CRATERS_HEAL = GameRules.register("unrpCratersHeal", Category.UPDATES, BooleanValue.create(false));
   public static final GameRules.Key<GameRules.BooleanValue> UNMAKING = GameRules.register("unrpUnmaking", Category.MOBS, BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> TITAN_SMASH = GameRules.register("unrpTitanSmash", Category.MOBS, BooleanValue.create(true));

   public static void init() {
   }
}
