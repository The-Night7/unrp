package com.myriam.unrp.pass;

/** AmRoster du modèle : les 54 pouvoirs copiables par Aura Manipulation (numéro de case) et ses combinaisons. */
public final class AmRoster {
   public static final int SLOT_MAX = 54;
   public static final String[] SLOT_SHORTS = new String[55];
   public static final String[] SLOT_ICONS = new String[55];
   public static final String HUB_ICON = "textures/icons/aura_manipulation_power_icon.png";
   public static final String[][] COMBOS;
   public static final String[] COMBO_ICONS;

   private AmRoster() {
   }

   public static int slotOf(String shortName) {
      for(int i = 1; i <= 54; ++i) {
         if (shortName.equals(SLOT_SHORTS[i])) {
            return i;
         }
      }

      return -1;
   }

   static {
      SLOT_SHORTS[1] = "stone_skin";
      SLOT_SHORTS[2] = "energy_discharge";
      SLOT_SHORTS[3] = "regeneration";
      SLOT_SHORTS[4] = "healing";
      SLOT_SHORTS[5] = "strong_punch";
      SLOT_SHORTS[6] = "strong_kick";
      SLOT_SHORTS[7] = "phase_shift";
      SLOT_SHORTS[8] = "dizzy_punch";
      SLOT_SHORTS[9] = "barrier";
      SLOT_SHORTS[10] = "explosion";
      SLOT_SHORTS[11] = "hydrofreeze";
      SLOT_SHORTS[12] = "illumination";
      SLOT_SHORTS[13] = "needles";
      SLOT_SHORTS[14] = "ability_gauging";
      SLOT_SHORTS[15] = "rock_arm";
      SLOT_SHORTS[16] = "heat_palm";
      SLOT_SHORTS[17] = "contact_shred";
      SLOT_SHORTS[18] = "speed";
      SLOT_SHORTS[19] = "nightmare";
      SLOT_SHORTS[20] = "conjure_disks";
      SLOT_SHORTS[21] = "shadow_flame";
      SLOT_SHORTS[22] = "lightning";
      SLOT_SHORTS[23] = "flame_claw";
      SLOT_SHORTS[24] = "demon_claw";
      SLOT_SHORTS[25] = "hypnosis";
      SLOT_SHORTS[26] = "whirlwind";
      SLOT_SHORTS[27] = "predator";
      SLOT_SHORTS[28] = "time_manipulation";
      SLOT_SHORTS[29] = "catalyst_ray";
      SLOT_SHORTS[30] = "blademaster";
      SLOT_SHORTS[31] = "telekinesis";
      SLOT_SHORTS[32] = "invisibility";
      SLOT_SHORTS[33] = "armor_suit";
      SLOT_SHORTS[34] = "hand_blade";
      SLOT_SHORTS[35] = "spikes";
      SLOT_SHORTS[36] = "crescent_slash";
      SLOT_SHORTS[37] = "quick_strike";
      SLOT_SHORTS[38] = "arcane_shot";
      SLOT_SHORTS[39] = "missiles";
      SLOT_SHORTS[40] = "conjure_vines";
      SLOT_SHORTS[41] = "energy_conduit";
      SLOT_SHORTS[42] = "phantom_push";
      SLOT_SHORTS[43] = "phantom_fist";
      SLOT_SHORTS[44] = "tremor";
      SLOT_SHORTS[45] = "spectral_claw";
      SLOT_SHORTS[46] = "umbral_shift";
      SLOT_SHORTS[47] = "azure_storm";
      SLOT_SHORTS[48] = "arachnid";
      SLOT_SHORTS[49] = "particles";
      SLOT_SHORTS[50] = "aura_transference";
      SLOT_SHORTS[51] = "armament_dominion";
      SLOT_SHORTS[52] = "daystar";
      SLOT_SHORTS[53] = "black_lash";
      SLOT_SHORTS[54] = "salt_seraph";
      SLOT_ICONS[1] = "textures/icons/stone_skin_icon.png";
      SLOT_ICONS[2] = "textures/icons/energy_discharge_power_icon.png";
      SLOT_ICONS[3] = "textures/icons/regeneration.png";
      SLOT_ICONS[4] = "textures/icons/healing_power_icon.png";
      SLOT_ICONS[5] = "textures/icons/strong_punch_power_icon.png";
      SLOT_ICONS[6] = "textures/icons/strong_kick_power_icon.png";
      SLOT_ICONS[7] = "textures/icons/phase_shift_power_icon.png";
      SLOT_ICONS[8] = "textures/icons/dizzy_punch.png";
      SLOT_ICONS[9] = "textures/icons/barrier_deploy.png";
      SLOT_ICONS[10] = "textures/icons/explosion_icon.png";
      SLOT_ICONS[11] = "textures/icons/hydrofreeze_icon.png";
      SLOT_ICONS[12] = "textures/icons/illumination_icon.png";
      SLOT_ICONS[13] = "textures/icons/needles_icon.png";
      SLOT_ICONS[14] = "textures/icons/ability_gauging_icon.png";
      SLOT_ICONS[15] = "textures/icons/rock_arm_icon.png";
      SLOT_ICONS[16] = "textures/icons/heat_palm_icon.png";
      SLOT_ICONS[17] = "textures/icons/contact_shred_icon.png";
      SLOT_ICONS[18] = "textures/icons/speed_icon.png";
      SLOT_ICONS[19] = "textures/icons/nightmare_icon.png";
      SLOT_ICONS[20] = "textures/icons/conjure_disks_icon.png";
      SLOT_ICONS[21] = "textures/icons/shadow_flame_icon.png";
      SLOT_ICONS[22] = "textures/icons/lightning_icon.png";
      SLOT_ICONS[23] = "textures/icons/flame_claw_icon.png";
      SLOT_ICONS[24] = "textures/icons/demon_claw_icon.png";
      SLOT_ICONS[25] = "textures/icons/hypnosis_icon.png";
      SLOT_ICONS[26] = "textures/icons/whirlwind_icon.png";
      SLOT_ICONS[27] = "textures/icons/predator_activation.png";
      SLOT_ICONS[28] = "textures/icons/time_manipulation_power_icon.png";
      SLOT_ICONS[29] = "textures/icons/catalyst_ray_power_icon.png";
      SLOT_ICONS[30] = "textures/icons/blademaster.png";
      SLOT_ICONS[31] = "textures/icons/telekinesis.png";
      SLOT_ICONS[32] = "textures/icons/invisibility.png";
      SLOT_ICONS[33] = "textures/icons/armor_suit_icon.png";
      SLOT_ICONS[34] = "textures/icons/hand_blade_icon.png";
      SLOT_ICONS[35] = "textures/icons/spikes_icon.png";
      SLOT_ICONS[36] = "textures/icons/crescent_slash_icon.png";
      SLOT_ICONS[37] = "textures/icons/quick_strike_icon.png";
      SLOT_ICONS[38] = "textures/icons/arcane_shot_icon.png";
      SLOT_ICONS[39] = "textures/icons/missiles_icon.png";
      SLOT_ICONS[40] = "textures/icons/conjure_vines_icon.png";
      SLOT_ICONS[41] = "textures/icons/energy_conduit_icon.png";
      SLOT_ICONS[42] = "textures/icons/phantom_push_icon.png";
      SLOT_ICONS[43] = "textures/icons/phantom_fist_icon.png";
      SLOT_ICONS[44] = "textures/icons/tremor_icon.png";
      SLOT_ICONS[45] = "textures/icons/spectral_claw_icon.png";
      SLOT_ICONS[46] = "textures/icons/umbral_shift_icon.png";
      SLOT_ICONS[47] = "textures/icons/azure_storm_icon.png";
      SLOT_ICONS[48] = "textures/icons/arachnid_icon.png";
      SLOT_ICONS[49] = "textures/icons/particles_icon.png";
      SLOT_ICONS[50] = "textures/icons/aura_transference_icon.png";
      SLOT_ICONS[51] = "textures/icons/armament_dominion_icon.png";
      SLOT_ICONS[52] = "textures/icons/daystar_icon.png";
      SLOT_ICONS[53] = "textures/icons/black_lash_icon.png";
      SLOT_ICONS[54] = "textures/icons/salt_seraph_icon.png";
      COMBOS = new String[][]{{"tempest_claws", "Tempest Claws", "demon_claw", "whirlwind"}, {"aura_shred", "Aura Shred", "ability_gauging", "contact_shred"}, {"deadeye", "Deadeye", "predator", "energy_discharge"}, {"sanctuary", "Sanctuary", "barrier", "healing"}, {"volt_deadeye", "Volt Deadeye", "predator", "energy_discharge", "lightning"}, {"vine_barrier", "Briarhold", "conjure_vines", "barrier"}, {"explosive_missile", "Explosive Missiles", "missiles", "explosion"}, {"homing_explosive_missile", "Homing Explosive Missiles", "predator", "missiles", "explosion"}, {"blizzard", "Blizzard", "whirlwind", "hydrofreeze"}, {"livewire", "Livewire", "conjure_vines", "barrier", "lightning"}, {"arcroot", "Arcroot", "conjure_vines", "lightning"}, {"warhead", "Warhead", "missiles", "strong_punch"}, {"fusework", "Fusework", "needles", "explosion"}, {"missile_punch", "Missile Punch", "predator", "missiles"}};
      COMBO_ICONS = new String[]{"textures/icons/tempest_claws_icon.png", "textures/icons/aura_shred_icon.png", "textures/icons/deadeye_activation.png", "textures/icons/sanctuary.png", "textures/icons/volt_deadeye.png", "textures/icons/vine_barrier_icon.png", "textures/icons/explosive_missile_icon.png", "textures/icons/homing_explosive_missile_icon.png", "textures/icons/blizzard_icon.png", "textures/icons/livewire_icon.png", "textures/icons/arcroot_icon.png", "textures/icons/warhead_icon.png", "textures/icons/fusework_icon.png", "textures/icons/missile_punch_icon.png"};
   }
}
