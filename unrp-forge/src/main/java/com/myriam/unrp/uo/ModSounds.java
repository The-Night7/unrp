package com.myriam.unrp.uo;



import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
   public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "unrp");
   public static final RegistryObject<SoundEvent> BUTTON_CLICK = register("button_click");
   public static final RegistryObject<SoundEvent> ADVANCEMENT_NOTIFICATION = register("advancement_notification");
   public static final RegistryObject<SoundEvent> NXGEN_LAB = register("nxgen_lab");
   public static final RegistryObject<SoundEvent> SPECIMEN = register("specimen");
   public static final RegistryObject<SoundEvent> BREAKTHROUGH = register("breakthrough");
   public static final RegistryObject<SoundEvent> AFTERNOON_WALK = register("afternoon_walk");
   public static final RegistryObject<SoundEvent> MORNING_BELL = register("morning_bell");
   public static final RegistryObject<SoundEvent> COLD_HALLS = register("cold_halls");
   public static final RegistryObject<SoundEvent> AURORA = register("aurora");
   public static final RegistryObject<SoundEvent> NIGHTFALL = register("nightfall");
   public static final RegistryObject<SoundEvent> AFTERMATH = register("aftermath");
   public static final RegistryObject<SoundEvent> DISC_TURF_WAR = register("music_disc.turf_war");
   public static final RegistryObject<SoundEvent> DISC_CROWN_MATCH = register("music_disc.crown_match");
   public static final RegistryObject<SoundEvent> DISC_GUARDIAN = register("music_disc.guardian");
   public static final RegistryObject<SoundEvent> DISC_TYRANT = register("music_disc.tyrant");
   public static final RegistryObject<SoundEvent> DISC_SURGE = register("music_disc.surge");
   public static final RegistryObject<SoundEvent> DISC_THE_HUNT = register("music_disc.the_hunt");
   public static final RegistryObject<SoundEvent> PARTICLES_BUZZ = register("particles_buzz");
   public static final RegistryObject<SoundEvent> AURA_ROAR = register("aura_roar");
   public static final RegistryObject<SoundEvent> AURA_SURGE = register("aura_surge");
   public static final String[] CHAINED = new String[]{"cn_mantle_up", "cn_mantle_down", "cn_ward_break", "cn_ward_whole", "cn_godfall", "cn_lash", "cn_lash_hit", "cn_bind", "cn_bind_grab", "cn_slam", "cn_lashstorm", "cn_lashstorm_whirl", "cn_pick", "cn_wheel_tick", "cn_shape_out", "cn_shape_away", "cn_wingbeat", "cn_swat", "cn_swat_hit", "cn_grab", "cn_grip", "cn_throw", "cn_throw_land", "cn_starfall_dive", "cn_starfall", "cn_cataclysm_rise", "cn_cataclysm", "cn_blade_swing", "cn_sever", "cn_sever_hit", "cn_eclipse_draw", "cn_eclipse", "cn_hammer_heft", "cn_smash", "cn_hammer_swell", "cn_worldbreaker", "cn_reap", "cn_reap_hit", "cn_harvest", "cn_harvest_spin", "cn_harvest_home", "cn_guard", "cn_block", "cn_recoil", "cn_recoil_hit", "cn_aegis_cast", "cn_aegis", "cn_aegis_end", "cn_ripple", "cn_lance_draw", "cn_lance_throw", "cn_lance_strike", "cn_rain_call", "cn_rain_strike", "cn_maw_lunge", "cn_devour", "cn_maw_feed", "cn_abyss_call", "cn_abyss", "cn_gate_open", "cn_gate_step", "cn_exile_tear", "cn_exile_fall", "cn_flock", "cn_flock_home", "cn_peck", "cn_murmur_rise", "cn_murmur", "cn_murmur_wings", "cn_murmur_end", "cn_spire_call", "cn_spire", "cn_citadel_call", "cn_citadel", "cn_wall_rise", "cn_wall_fall", "cn_hellfall", "cn_rift", "cn_rock_fall", "cn_rock_land", "cn_great_land", "cn_null", "cn_null_end", "cn_genesis_rise", "cn_genesis_pull", "cn_genesis_blast", "cn_genesis_wall", "cn_ascend", "cn_descend", "cn_tatter_open", "cn_tatter_close", "cn_comet_dive", "cn_comet_land", "cn_rake", "cn_step", "cn_split", "cn_decree", "cn_kneel", "cn_dread", "cn_unchain", "cn_roar", "cn_rechain", "cn_rampage", "cn_rampage_hit", "cn_rend", "cn_breath_start", "cn_breath", "cn_tenfold", "cn_tenfold_pull", "cn_beast_claw", "cn_pounce", "cn_legion_open", "cn_summon", "cn_rise", "cn_choir", "cn_hounds", "cn_hound_bite", "cn_wraith_dive", "cn_dismiss", "cn_unmake", "cn_echo_bolt", "cn_echo_arc", "cn_echo_claw", "cn_echo_burst", "cn_echo_quake", "cn_echo_fissure", "cn_echo_blink", "cn_finn_yowl", "cn_finn_pounce", "cn_finn_land", "cn_finn_swipe", "cn_colossus_rise", "cn_colossus_stomp", "cn_colossus_slam", "cn_colossus_break", "cn_colossus_step", "cn_wyrm_roar", "cn_wyrm_wings", "cn_wyrm_takeoff", "cn_wyrm_breath", "cn_wyrm_land", "cn_wyrm_bite", "cn_wyrm_claw", "cn_wyrm_tail", "cn_descent_score", "cn_descent_blip", "cn_descent_strike", "cn_descent_strike_last", "cn_descent_boom_key", "cn_descent_boom_ul", "cn_descent_boom_pillar", "cn_descent_boom_land", "cn_descent_crack", "cn_descent_snap_0", "cn_descent_snap_1", "cn_descent_snap_2", "cn_descent_cue", "cn_descent_bell_0", "cn_descent_bell_1", "cn_descent_bell_2", "cn_descent_bell_3", "cn_descent_bell_4", "cn_descent_bell_5", "cn_descent_bell_6", "cn_descent_bell_7", "cn_descent_bell_8", "cn_descent_bell_9", "cn_descent_beat", "cn_descent_beat_soft", "cn_shard_fall_0", "cn_shard_fall_3", "cn_shard_fall_6", "cn_shard_fall_9", "cn_shard_take_0", "cn_shard_take_1", "cn_shard_take_2", "cn_shard_take_3", "cn_shard_take_4", "cn_shard_take_5", "cn_shard_take_6", "cn_shard_take_7", "cn_shard_take_8", "cn_shard_take_9", "cn_evolve", "cn_shard_crumble"};
   public static final String[] HUB = new String[]{"hub_click", "hub_hover", "hub_tab", "hub_open", "hub_close", "hub_select", "hub_lift", "hub_place", "hub_wrong", "hub_partial", "hub_unlock", "hub_lose", "hub_toast", "hub_hit", "hub_miss", "hub_charge", "hub_tick", "hub_level", "hub_slide"};
   public static final String[] TITAN;
   public static final String[] PASS;

   private static RegistryObject<SoundEvent> register(String name) {
      return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("unrp", name)));
   }

   static {
      for(String name : CHAINED) {
         register(name);
      }

      TITAN = new String[]{"ti_step", "ti_stomp", "ti_boom", "ti_smash", "ti_thunder", "ti_roar", "ti_swipe", "ti_throw", "ti_lift", "ti_grip", "ti_block", "ti_punch", "ti_partial", "ti_fall", "ti_eject", "ti_out", "ti_bell", "ti_impact", "ti_vent", "ti_pyro", "ti_armour", "ti_shatter", "ti_frost", "ti_blizzard", "ti_clap", "ti_crackle", "ti_rock", "ti_quake", "ti_gust", "ti_tornado", "ti_wave", "ti_splash", "ti_whirl", "ti_glare", "ti_beam", "ti_dusk", "ti_vein", "ti_shell", "ti_sunder", "ti_heat", "ti_roots"};

      for(String hub : HUB) {
         register(hub);
      }

      for(String ti : TITAN) {
         register(ti);
      }

      PASS = new String[]{"lt_on", "lt_bolt", "lt_hit", "lt_touch", "lt_surge", "lt_beam", "pass_crackle", "ed_on", "ed_beam", "ed_beam_end", "ed_wave_charge", "ed_wave", "ed_wave_end", "ed_hurl", "ed_blast", "ed_gather", "ed_burst", "ed_propel", "pass_whine", "pass_plasma", "br_deploy", "br_hit", "br_crack", "br_shatter", "br_grow", "br_shrink", "br_ram", "tm_on", "tm_flow", "tm_slow", "tm_rewind", "tm_blink", "tm_mark", "tm_freeze", "tm_release", "tm_strike", "tm_delay", "pass_ticking", "pd_on", "pd_lock", "pd_pulse", "pd_rush", "pd_rush_hit", "pd_breaker", "pd_rock", "pd_roar", "am_on", "am_take", "am_expel"};

      for(String pass : PASS) {
         register(pass);
      }

   }
}
