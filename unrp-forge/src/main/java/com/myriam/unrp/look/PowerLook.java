package com.myriam.unrp.look;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class PowerLook {
   public static final int FALLBACK_RGB = 15262964;
   public static final double READABLE = 0.5D;
   public static final String FALLBACK_ICON = "unrp:textures/icons/aura_manipulation_power_icon.png";
   private static final Map<String, Integer> RGB = new HashMap();
   private static final Map<String, String> ICON = new HashMap();

   private PowerLook() {
   }

   private static void look(String shortName, int rgb, String icon) {
      RGB.put(shortName, rgb);
      ICON.put(shortName, icon);
   }

   public static int color(String shortName) {
      Integer c = (Integer)RGB.get(shortName);
      return c == null ? 15262964 : c;
   }

   public static String icon(String shortName) {
      String i = (String)ICON.get(shortName);
      return i == null ? "unrp:textures/icons/aura_manipulation_power_icon.png" : i;
   }

   public static boolean known(String shortName) {
      return RGB.containsKey(shortName);
   }

   public static Set<String> all() {
      return new TreeSet(RGB.keySet());
   }

   public static String tenths(int v) {
      int a = Math.max(0, v);
      return a / 10 + "." + a % 10;
   }

   public static int readable(int rgb) {
      int r = rgb >> 16 & 255;
      int g = rgb >> 8 & 255;
      int b = rgb & 255;
      double l = (0.2126D * (double)r + 0.7152D * (double)g + 0.0722D * (double)b) / 255.0D;
      if (l >= 0.5D) {
         return rgb & 16777215;
      } else {
         double k = (0.5D - l) / (1.0D - l);
         int nr = (int)Math.round((double)r + (double)(255 - r) * k);
         int ng = (int)Math.round((double)g + (double)(255 - g) * k);
         int nb = (int)Math.round((double)b + (double)(255 - b) * k);
         return nr << 16 | ng << 8 | nb;
      }
   }

   static {
      look("covenant", 0xF2C94C, "unrp:textures/icons/covenant_posture.png");
      look("stone_skin", 9474192, "unrp:textures/icons/stone_skin_icon.png");
      look("energy_discharge", 15022389, "unrp:textures/icons/energy_discharge_power_icon.png");
      look("regeneration", 1880082, "unrp:textures/icons/regeneration.png");
      look("healing", 16733695, "unrp:textures/icons/healing_power_icon.png");
      look("strong_punch", 10092456, "unrp:textures/icons/strong_punch_power_icon.png");
      look("strong_kick", 10092543, "unrp:textures/icons/strong_kick_power_icon.png");
      look("phase_shift", 4161504, "unrp:textures/icons/phase_shift_power_icon.png");
      look("dizzy_punch", 16739968, "unrp:textures/icons/dizzy_punch.png");
      look("barrier", 15914308, "unrp:textures/icons/barrier_deploy.png");
      look("aura_manipulation", 5592405, "unrp:textures/icons/aura_manipulation_power_icon.png");
      look("explosion", 15229722, "unrp:textures/icons/explosion_icon.png");
      look("hydrofreeze", 5232352, "unrp:textures/icons/hydrofreeze_icon.png");
      look("illumination", 16425017, "unrp:textures/icons/illumination_icon.png");
      look("needles", 13498241, "unrp:textures/icons/needles_icon.png");
      look("ability_gauging", 16767626, "unrp:textures/icons/ability_gauging_icon.png");
      look("rock_arm", 11897382, "unrp:textures/icons/rock_arm_icon.png");
      look("heat_palm", 16732449, "unrp:textures/icons/heat_palm_icon.png");
      look("contact_shred", 8851986, "unrp:textures/icons/contact_shred_icon.png");
      look("speed", 16771932, "unrp:textures/icons/speed_icon.png");
      look("nightmare", 7212800, "unrp:textures/icons/nightmare_icon.png");
      look("conjure_disks", 14269440, "unrp:textures/icons/conjure_disks_icon.png");
      look("shadow_flame", 7471334, "unrp:textures/icons/shadow_flame_icon.png");
      look("lightning", 4174079, "unrp:textures/icons/lightning_icon.png");
      look("flame_claw", 14562304, "unrp:textures/icons/flame_claw_icon.png");
      look("demon_claw", 13111342, "unrp:textures/icons/demon_claw_icon.png");
      look("hypnosis", 11554353, "unrp:textures/icons/hypnosis_icon.png");
      look("whirlwind", 13226714, "unrp:textures/icons/whirlwind_icon.png");
      look("predator", 15242256, "unrp:textures/icons/predator_activation.png");
      look("time_manipulation", 16740288, "unrp:textures/icons/time_manipulation_power_icon.png");
      look("catalyst_ray", 15258938, "unrp:textures/icons/catalyst_ray_power_icon.png");
      look("blademaster", 5636095, "unrp:textures/icons/blademaster.png");
      look("telekinesis", 16733695, "unrp:textures/icons/telekinesis.png");
      look("invisibility", 11184810, "unrp:textures/icons/invisibility.png");
      look("armor_suit", 9414840, "unrp:textures/icons/armor_suit_icon.png");
      look("hand_blade", 15259040, "unrp:textures/icons/hand_blade_icon.png");
      look("spikes", 13216240, "unrp:textures/icons/spikes_icon.png");
      look("crescent_slash", 9427176, "unrp:textures/icons/crescent_slash_icon.png");
      look("quick_strike", 15769680, "unrp:textures/icons/quick_strike_icon.png");
      look("arcane_shot", 11832552, "unrp:textures/icons/arcane_shot_icon.png");
      look("missiles", 15241336, "unrp:textures/icons/missiles_icon.png");
      look("conjure_vines", 5892204, "unrp:textures/icons/conjure_vines_icon.png");
      look("energy_conduit", 7264488, "unrp:textures/icons/energy_conduit_icon.png");
      look("phantom_push", 5875944, "unrp:textures/icons/phantom_push_icon.png");
      look("phantom_fist", 5234920, "unrp:textures/icons/phantom_fist_icon.png");
      look("tremor", 14198869, "unrp:textures/icons/tremor_icon.png");
      look("spectral_claw", 10178520, "unrp:textures/icons/spectral_claw_icon.png");
      look("umbral_shift", 9067752, "unrp:textures/icons/umbral_shift_icon.png");
      look("azure_storm", 5220584, "unrp:textures/icons/azure_storm_icon.png");
      look("arachnid", 14174815, "unrp:textures/icons/arachnid_icon.png");
      look("particles", 15160448, "unrp:textures/icons/particles_icon.png");
      look("aura_transference", 5692644, "unrp:textures/icons/aura_transference_icon.png");
      look("armament_dominion", 15254106, "unrp:textures/icons/armament_dominion_icon.png");
      look("daystar", 16757311, "unrp:textures/icons/daystar_icon.png");
      look("black_lash", 5891826, "unrp:textures/icons/black_lash_icon.png");
      look("salt_seraph", 16052714, "unrp:textures/icons/salt_seraph_icon.png");
      look("chained_one", 10771711, "unrp:textures/icons/chained_one_icon.png");
      look("sarcophage", 35195, "unrp:textures/icons/sarcophage_icon.png");
      look("edict", 13840175, "unrp:textures/icons/edict_icon.png");
      look("wyrm_engine", 16757504, "unrp:textures/icons/wyrm_engine_icon.png");
      look("singularity", 8172354, "unrp:textures/icons/singularity_icon.png");
      look("limit_breaker", 16011550, "unrp:textures/icons/limit_breaker_icon.png");
      look("dealmaker", 2001125, "unrp:textures/icons/dealmaker_icon.png");
      look("counterweight", 9315498, "unrp:textures/icons/counterweight_icon.png");
      look("crimson_mark", 44225, "unrp:textures/icons/crimson_mark_icon.png");
      look("hollow_mark", 6174129, "unrp:textures/icons/hollow_mark_icon.png");
      look("nadir", 14162784, "unrp:textures/icons/nadir_icon.png");
      look("legacy", 15255146, "unrp:textures/icons/legacy_icon.png");

      // titans (TitanMath) : pouvoirs pas portés

   }
}
