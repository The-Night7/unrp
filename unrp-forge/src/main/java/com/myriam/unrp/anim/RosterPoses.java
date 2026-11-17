package com.myriam.unrp.anim;

import com.myriam.unrp.pass.PassBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "unrp", value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class RosterPoses {
    private RosterPoses() {}

    record E(String power, String timer, int max, String tag, String ease, boolean carry, float[][] parts) {}

    static final E[] ROSTER = {
        new E("barrier", "anim_ward", 5, "unrp.barrier_active", "OutCubic", false, new float[][] {{4.0f,0.0f,0.0f,0.0f},{-4.0f,0.0f,0.0f,1.0f},{-85.0f,-6.0f,0.0f,0.0f},{-85.0f,6.0f,0.0f,0.0f},null,null}),
        new E("barrier_am", "anim_ward", 5, "unrp.barrier_active", "OutCubic", false, new float[][] {{4.0f,0.0f,0.0f,0.0f},{-4.0f,0.0f,0.0f,1.0f},{-85.0f,-6.0f,0.0f,0.0f},{-85.0f,6.0f,0.0f,0.0f},null,null}),
        new E("healing", "anim_mend_self", 5, "unrp.self_heal_active", "InOutSine", false, new float[][] {{-8.0f,0.0f,0.0f,0.0f},null,{-55.0f,-45.0f,0.0f,0.0f},{-55.0f,45.0f,0.0f,0.0f},null,null}),
        new E("healing_am", "anim_mend_self", 5, "unrp.self_heal_active", "InOutSine", false, new float[][] {{-8.0f,0.0f,0.0f,0.0f},null,{-55.0f,-45.0f,0.0f,0.0f},{-55.0f,45.0f,0.0f,0.0f},null,null}),
        new E("healing", "anim_mend_other", 5, "unrp.external_heal_active", "InOutSine", false, new float[][] {{2.0f,0.0f,0.0f,0.0f},null,{-80.0f,-4.0f,0.0f,0.0f},{-35.0f,10.0f,0.0f,0.0f},null,null}),
        new E("healing_am", "anim_mend_other", 5, "unrp.external_heal_active", "InOutSine", false, new float[][] {{2.0f,0.0f,0.0f,0.0f},null,{-80.0f,-4.0f,0.0f,0.0f},{-35.0f,10.0f,0.0f,0.0f},null,null}),
        new E("quick_strike", "anim_stance", 5, "unrp.active.qs_stance", "OutCubic", false, new float[][] {{6.0f,0.0f,0.0f,0.0f},{6.0f,0.0f,0.0f,-1.0f},{-50.0f,-25.0f,0.0f,0.0f},{-50.0f,25.0f,0.0f,0.0f},null,null}),
        new E("quick_strike_am", "anim_stance", 5, "unrp.active.qs_stance", "OutCubic", false, new float[][] {{6.0f,0.0f,0.0f,0.0f},{6.0f,0.0f,0.0f,-1.0f},{-50.0f,-25.0f,0.0f,0.0f},{-50.0f,25.0f,0.0f,0.0f},null,null}),
        new E("azure_storm", "anim_vortex", 6, "unrp.as_storm_on", "InOutCubic", false, new float[][] {{-15.0f,0.0f,0.0f,0.0f},{-6.0f,0.0f,0.0f,1.0f},{-140.0f,0.0f,20.0f,0.0f},{-140.0f,0.0f,-20.0f,0.0f},null,null}),
        new E("azure_storm_am", "anim_vortex", 6, "unrp.as_storm_on", "InOutCubic", false, new float[][] {{-15.0f,0.0f,0.0f,0.0f},{-6.0f,0.0f,0.0f,1.0f},{-140.0f,0.0f,20.0f,0.0f},{-140.0f,0.0f,-20.0f,0.0f},null,null}),
        new E("rock_arm", "anim_guard", 4, "unrp.rock_arm_guard_drain", "OutCubic", false, new float[][] {{10.0f,0.0f,0.0f,0.0f},null,{-80.0f,-55.0f,0.0f,0.0f},{-80.0f,55.0f,0.0f,0.0f},null,null}),
        new E("rock_arm_am", "anim_guard", 4, "unrp.rock_arm_guard_drain", "OutCubic", false, new float[][] {{10.0f,0.0f,0.0f,0.0f},null,{-80.0f,-55.0f,0.0f,0.0f},{-80.0f,55.0f,0.0f,0.0f},null,null}),
        new E("heat_palm", "anim_smelt", 5, "unrp.heat_palm_smelt_drain", "InOutSine", false, new float[][] {{12.0f,0.0f,0.0f,0.0f},{6.0f,0.0f,0.0f,-1.0f},{-60.0f,-8.0f,0.0f,0.0f},null,null,null}),
        new E("heat_palm_am", "anim_smelt", 5, "unrp.heat_palm_smelt_drain", "InOutSine", false, new float[][] {{12.0f,0.0f,0.0f,0.0f},{6.0f,0.0f,0.0f,-1.0f},{-60.0f,-8.0f,0.0f,0.0f},null,null,null}),
        new E("illumination", "anim_beacon", 5, "unrp.illumination_hand_light_drain", "InOutSine", false, new float[][] {{-10.0f,0.0f,0.0f,0.0f},null,{-150.0f,0.0f,6.0f,0.0f},null,null,null}),
        new E("illumination_am", "anim_beacon", 5, "unrp.illumination_hand_light_drain", "InOutSine", false, new float[][] {{-10.0f,0.0f,0.0f,0.0f},null,{-150.0f,0.0f,6.0f,0.0f},null,null,null}),
        new E("hypnosis", "anim_gaze", 6, "unrp.hypnosis_deep_drain", "InOutSine", false, new float[][] {{-4.0f,0.0f,0.0f,0.0f},{-2.0f,0.0f,0.0f,0.0f},{-85.0f,-8.0f,0.0f,0.0f},{-25.0f,8.0f,0.0f,0.0f},null,null}),
        new E("hypnosis_am", "anim_gaze", 6, "unrp.hypnosis_deep_drain", "InOutSine", false, new float[][] {{-4.0f,0.0f,0.0f,0.0f},{-2.0f,0.0f,0.0f,0.0f},{-85.0f,-8.0f,0.0f,0.0f},{-25.0f,8.0f,0.0f,0.0f},null,null}),
        new E("energy_conduit", "anim_siphon", 5, "unrp.ec_transfer_on", "InOutCubic", false, new float[][] {null,{0.0f,-10.0f,0.0f,0.0f},{-95.0f,-10.0f,0.0f,0.0f},{-130.0f,0.0f,-18.0f,0.0f},null,null}),
        new E("energy_conduit_am", "anim_siphon", 5, "unrp.ec_transfer_on", "InOutCubic", false, new float[][] {null,{0.0f,-10.0f,0.0f,0.0f},{-95.0f,-10.0f,0.0f,0.0f},{-130.0f,0.0f,-18.0f,0.0f},null,null}),
        new E("energy_conduit", "anim_restore", 5, "unrp.ec_restore_on", "InOutSine", false, new float[][] {{-6.0f,0.0f,0.0f,0.0f},null,{-70.0f,-12.0f,0.0f,0.0f},{-70.0f,12.0f,0.0f,0.0f},null,null}),
        new E("energy_conduit_am", "anim_restore", 5, "unrp.ec_restore_on", "InOutSine", false, new float[][] {{-6.0f,0.0f,0.0f,0.0f},null,{-70.0f,-12.0f,0.0f,0.0f},{-70.0f,12.0f,0.0f,0.0f},null,null}),
        new E("missiles", "anim_battery", 4, "unrp.ms_barrage_on", "OutCubic", false, new float[][] {null,{5.0f,0.0f,0.0f,-1.0f},{-85.0f,-8.0f,0.0f,0.0f},{-85.0f,8.0f,0.0f,0.0f},null,null}),
        new E("missiles_am", "anim_battery", 4, "unrp.ms_barrage_on", "OutCubic", false, new float[][] {null,{5.0f,0.0f,0.0f,-1.0f},{-85.0f,-8.0f,0.0f,0.0f},{-85.0f,8.0f,0.0f,0.0f},null,null}),
        new E("whirlwind", "anim_eye", 5, "unrp.whirlwind_shield_drain", "InOutSine", false, new float[][] {{-6.0f,0.0f,0.0f,0.0f},null,{-10.0f,0.0f,80.0f,0.0f},{-10.0f,0.0f,-80.0f,0.0f},null,null}),
        new E("whirlwind_am", "anim_eye", 5, "unrp.whirlwind_shield_drain", "InOutSine", false, new float[][] {{-6.0f,0.0f,0.0f,0.0f},null,{-10.0f,0.0f,80.0f,0.0f},{-10.0f,0.0f,-80.0f,0.0f},null,null}),
        new E("blizzard", "anim_squall", 6, "unrp.bz_storm_on", "InOutCubic", false, new float[][] {{-10.0f,0.0f,0.0f,0.0f},null,{-120.0f,0.0f,30.0f,0.0f},{-120.0f,0.0f,-30.0f,0.0f},null,null}),
        new E("blizzard_am", "anim_squall", 6, "unrp.bz_storm_on", "InOutCubic", false, new float[][] {{-10.0f,0.0f,0.0f,0.0f},null,{-120.0f,0.0f,30.0f,0.0f},{-120.0f,0.0f,-30.0f,0.0f},null,null}),
        new E("demon_claw", "anim_flare", 5, "unrp.demon_claw_focus_drain", "OutCubic", false, new float[][] {{8.0f,0.0f,0.0f,0.0f},{8.0f,0.0f,0.0f,-1.0f},{-20.0f,0.0f,35.0f,0.0f},{-20.0f,0.0f,-35.0f,0.0f},null,null}),
        new E("demon_claw_am", "anim_flare", 5, "unrp.demon_claw_focus_drain", "OutCubic", false, new float[][] {{8.0f,0.0f,0.0f,0.0f},{8.0f,0.0f,0.0f,-1.0f},{-20.0f,0.0f,35.0f,0.0f},{-20.0f,0.0f,-35.0f,0.0f},null,null}),
        new E("arachnid", "anim_skitter", 5, "unrp.ar_guard_on", "OutCubic", false, new float[][] {null,{10.0f,0.0f,0.0f,-1.0f},{-45.0f,-30.0f,0.0f,0.0f},{-45.0f,30.0f,0.0f,0.0f},null,null}),
        new E("arachnid_am", "anim_skitter", 5, "unrp.ar_guard_on", "OutCubic", false, new float[][] {null,{10.0f,0.0f,0.0f,-1.0f},{-45.0f,-30.0f,0.0f,0.0f},{-45.0f,30.0f,0.0f,0.0f},null,null}),
        new E("flame_claw", "anim_wings", 5, "unrp.flame_claw_surge_drain", "OutCubic", false, new float[][] {{-6.0f,0.0f,0.0f,0.0f},{8.0f,0.0f,0.0f,-1.0f},{30.0f,0.0f,25.0f,0.0f},{30.0f,0.0f,-25.0f,0.0f},null,null}),
        new E("flame_claw_am", "anim_wings", 5, "unrp.flame_claw_surge_drain", "OutCubic", false, new float[][] {{-6.0f,0.0f,0.0f,0.0f},{8.0f,0.0f,0.0f,-1.0f},{30.0f,0.0f,25.0f,0.0f},{30.0f,0.0f,-25.0f,0.0f},null,null}),
        new E("hydrofreeze", "anim_envelop", 5, "unrp.water_envelop_active", "InOutSine", false, new float[][] {{6.0f,0.0f,0.0f,0.0f},null,{-70.0f,-30.0f,0.0f,0.0f},{-70.0f,30.0f,0.0f,0.0f},null,null}),
        new E("hydrofreeze_am", "anim_envelop", 5, "unrp.water_envelop_active", "InOutSine", false, new float[][] {{6.0f,0.0f,0.0f,0.0f},null,{-70.0f,-30.0f,0.0f,0.0f},{-70.0f,30.0f,0.0f,0.0f},null,null}),
        new E("arcane_shot", "anim_aim", 4, "unrp.pose.arc_aim", "OutCubic", false, new float[][] {{2.0f,0.0f,0.0f,0.0f},{0.0f,-14.0f,0.0f,0.0f},{-95.0f,-4.0f,0.0f,0.0f},{-75.0f,30.0f,0.0f,0.0f},null,null}),
        new E("arcane_shot_am", "anim_aim", 4, "unrp.pose.arc_aim", "OutCubic", false, new float[][] {{2.0f,0.0f,0.0f,0.0f},{0.0f,-14.0f,0.0f,0.0f},{-95.0f,-4.0f,0.0f,0.0f},{-75.0f,30.0f,0.0f,0.0f},null,null}),
        new E("missiles", "anim_aim", 4, "unrp.pose.ms_aim", "OutCubic", false, new float[][] {null,{4.0f,0.0f,0.0f,0.0f},{-85.0f,-8.0f,0.0f,0.0f},{-85.0f,8.0f,0.0f,0.0f},null,null}),
        new E("missiles_am", "anim_aim", 4, "unrp.pose.ms_aim", "OutCubic", false, new float[][] {null,{4.0f,0.0f,0.0f,0.0f},{-85.0f,-8.0f,0.0f,0.0f},{-85.0f,8.0f,0.0f,0.0f},null,null}),
        new E("crescent_slash", "anim_windup", 4, "unrp.pose.cres_charge", "OutCubic", false, new float[][] {{0.0f,-10.0f,0.0f,0.0f},{0.0f,18.0f,0.0f,0.0f},{-110.0f,35.0f,0.0f,0.0f},null,null,null}),
        new E("crescent_slash_am", "anim_windup", 4, "unrp.pose.cres_charge", "OutCubic", false, new float[][] {{0.0f,-10.0f,0.0f,0.0f},{0.0f,18.0f,0.0f,0.0f},{-110.0f,35.0f,0.0f,0.0f},null,null,null}),
        new E("explosion", "anim_prime", 4, "unrp.pose.ex_prime", "OutBack", false, new float[][] {{-14.0f,0.0f,0.0f,0.0f},{-8.0f,0.0f,0.0f,1.0f},{-160.0f,0.0f,8.0f,0.0f},null,null,null}),
        new E("explosion_am", "anim_prime", 4, "unrp.pose.ex_prime", "OutBack", false, new float[][] {{-14.0f,0.0f,0.0f,0.0f},{-8.0f,0.0f,0.0f,1.0f},{-160.0f,0.0f,8.0f,0.0f},null,null,null}),
        new E("phantom_push", "anim_palm", 4, "unrp.pose.pp_palm", "OutCubic", false, new float[][] {null,{4.0f,0.0f,0.0f,-1.0f},{-90.0f,-6.0f,0.0f,0.0f},{-90.0f,6.0f,0.0f,0.0f},null,null}),
        new E("phantom_push_am", "anim_palm", 4, "unrp.pose.pp_palm", "OutCubic", false, new float[][] {null,{4.0f,0.0f,0.0f,-1.0f},{-90.0f,-6.0f,0.0f,0.0f},{-90.0f,6.0f,0.0f,0.0f},null,null}),
        new E("shadow_flame", "anim_cast", 4, "unrp.pose.sf_cast", "OutCubic", false, new float[][] {null,{4.0f,0.0f,0.0f,0.0f},{-90.0f,-5.0f,0.0f,0.0f},{-40.0f,12.0f,0.0f,0.0f},null,null}),
        new E("shadow_flame_am", "anim_cast", 4, "unrp.pose.sf_cast", "OutCubic", false, new float[][] {null,{4.0f,0.0f,0.0f,0.0f},{-90.0f,-5.0f,0.0f,0.0f},{-40.0f,12.0f,0.0f,0.0f},null,null}),
        new E("conjure_disks", "anim_orbit", 5, "unrp.conjure_disks_shield", "InOutSine", false, new float[][] {{-2.0f,0.0f,0.0f,0.0f},null,{-35.0f,-20.0f,0.0f,0.0f},{-35.0f,20.0f,0.0f,0.0f},null,null}),
        new E("conjure_disks_am", "anim_orbit", 5, "unrp.conjure_disks_shield", "InOutSine", false, new float[][] {{-2.0f,0.0f,0.0f,0.0f},null,{-35.0f,-20.0f,0.0f,0.0f},{-35.0f,20.0f,0.0f,0.0f},null,null}),
        new E("conjure_disks", "anim_surf", 6, "unrp.conjure_disks_hover", "InOutSine", false, new float[][] {{0.0f,-25.0f,0.0f,0.0f},{0.0f,30.0f,0.0f,0.0f},{-15.0f,0.0f,50.0f,0.0f},{-10.0f,0.0f,-60.0f,0.0f},{-15.0f,0.0f,8.0f,0.0f},{18.0f,0.0f,-8.0f,0.0f}}),
        new E("conjure_disks_am", "anim_surf", 6, "unrp.conjure_disks_hover", "InOutSine", false, new float[][] {{0.0f,-25.0f,0.0f,0.0f},{0.0f,30.0f,0.0f,0.0f},{-15.0f,0.0f,50.0f,0.0f},{-10.0f,0.0f,-60.0f,0.0f},{-15.0f,0.0f,8.0f,0.0f},{18.0f,0.0f,-8.0f,0.0f}}),
        new E("telekinesis", "anim_crush", 6, "unrp.tk_pressure_active", "InOutCubic", false, new float[][] {null,{6.0f,0.0f,0.0f,-1.0f},{-85.0f,-18.0f,0.0f,0.0f},{-85.0f,18.0f,0.0f,0.0f},null,null}),
        new E("telekinesis", "anim_orbit", 6, "unrp.tk_orbit_active", "InOutSine", false, new float[][] {{-8.0f,0.0f,0.0f,0.0f},null,{-120.0f,-10.0f,0.0f,0.0f},null,null,null}),
        new E("umbral_shift", "anim_wraith", 8, "unrp.us_phased", "InOutSine", false, new float[][] {{-4.0f,0.0f,0.0f,0.0f},{-6.0f,0.0f,0.0f,1.0f},{25.0f,0.0f,15.0f,0.0f},{25.0f,0.0f,-15.0f,0.0f},{12.0f,0.0f,0.0f,0.0f},{12.0f,0.0f,0.0f,0.0f}}),
        new E("umbral_shift_am", "anim_wraith", 8, "unrp.us_phased", "InOutSine", false, new float[][] {{-4.0f,0.0f,0.0f,0.0f},{-6.0f,0.0f,0.0f,1.0f},{25.0f,0.0f,15.0f,0.0f},{25.0f,0.0f,-15.0f,0.0f},{12.0f,0.0f,0.0f,0.0f},{12.0f,0.0f,0.0f,0.0f}}),
        new E("stone_skin", "anim_immovable", 5, "unrp.stone_immovable", "OutCubic", false, new float[][] {null,{4.0f,0.0f,0.0f,-1.0f},{-30.0f,-30.0f,0.0f,0.0f},{-30.0f,30.0f,0.0f,0.0f},{0.0f,0.0f,14.0f,0.0f},{0.0f,0.0f,-14.0f,0.0f}}),
        new E("stone_skin_am", "anim_immovable", 5, "unrp.stone_immovable", "OutCubic", false, new float[][] {null,{4.0f,0.0f,0.0f,-1.0f},{-30.0f,-30.0f,0.0f,0.0f},{-30.0f,30.0f,0.0f,0.0f},{0.0f,0.0f,14.0f,0.0f},{0.0f,0.0f,-14.0f,0.0f}}),
        new E("stone_skin", "anim_harden", 4, "unrp.stone_harden", "OutBack", false, new float[][] {{6.0f,0.0f,0.0f,0.0f},{-8.0f,0.0f,0.0f,1.0f},{-5.0f,0.0f,25.0f,0.0f},{-5.0f,0.0f,-25.0f,0.0f},null,null}),
        new E("stone_skin_am", "anim_harden", 4, "unrp.stone_harden", "OutBack", false, new float[][] {{6.0f,0.0f,0.0f,0.0f},{-8.0f,0.0f,0.0f,1.0f},{-5.0f,0.0f,25.0f,0.0f},{-5.0f,0.0f,-25.0f,0.0f},null,null}),
        new E("hand_blade", "anim_engarde", 6, "unrp.active.hand_blade_form", "OutCubic", false, new float[][] {{0.0f,-25.0f,0.0f,0.0f},{0.0f,35.0f,0.0f,0.0f},{-100.0f,25.0f,0.0f,0.0f},{-45.0f,-15.0f,0.0f,0.0f},{-12.0f,0.0f,0.0f,0.0f},{14.0f,0.0f,0.0f,0.0f}}),
        new E("hand_blade_am", "anim_engarde", 6, "unrp.active.hand_blade_form", "OutCubic", false, new float[][] {{0.0f,-25.0f,0.0f,0.0f},{0.0f,35.0f,0.0f,0.0f},{-100.0f,25.0f,0.0f,0.0f},{-45.0f,-15.0f,0.0f,0.0f},{-12.0f,0.0f,0.0f,0.0f},{14.0f,0.0f,0.0f,0.0f}}),
        new E("invisibility", "anim_wrap", 6, "unrp.invis_cloaked", "InOutSine", false, new float[][] {{10.0f,0.0f,0.0f,0.0f},null,{-60.0f,50.0f,0.0f,0.0f},{20.0f,-20.0f,0.0f,0.0f},null,null}),
        new E("invisibility_am", "anim_wrap", 6, "unrp.invis_cloaked", "InOutSine", false, new float[][] {{10.0f,0.0f,0.0f,0.0f},null,{-60.0f,50.0f,0.0f,0.0f},{20.0f,-20.0f,0.0f,0.0f},null,null}),
        new E("ability_gauging", "anim_scan", 8, "unrp.ability_gauging_drain", "InOutSine", false, new float[][] {{4.0f,0.0f,0.0f,0.0f},null,{-75.0f,-6.0f,0.0f,0.0f},{15.0f,35.0f,0.0f,0.0f},null,null}),
        new E("ability_gauging_am", "anim_scan", 8, "unrp.ability_gauging_drain", "InOutSine", false, new float[][] {{4.0f,0.0f,0.0f,0.0f},null,{-75.0f,-6.0f,0.0f,0.0f},{15.0f,35.0f,0.0f,0.0f},null,null}),
        new E("ability_gauging", "anim_scope", 6, "unrp.ability_gauging_scope_drain", "InOutSine", false, new float[][] {{-6.0f,0.0f,0.0f,0.0f},null,{-120.0f,-55.0f,0.0f,0.0f},{-120.0f,55.0f,0.0f,0.0f},null,null}),
        new E("ability_gauging_am", "anim_scope", 6, "unrp.ability_gauging_scope_drain", "InOutSine", false, new float[][] {{-6.0f,0.0f,0.0f,0.0f},null,{-120.0f,-55.0f,0.0f,0.0f},{-120.0f,55.0f,0.0f,0.0f},null,null}),
        new E("regeneration", "anim_exhale", 8, "unrp.pose.regen_exhale", "InOutSine", false, new float[][] {{-22.0f,0.0f,0.0f,0.0f},{-8.0f,0.0f,0.0f,1.0f},{-10.0f,0.0f,25.0f,0.0f},{-10.0f,0.0f,-25.0f,0.0f},null,null}),
        new E("blademaster", "anim_dualguard", 5, "unrp.bm.dual", "OutCubic", false, new float[][] {null,null,{-40.0f,-40.0f,0.0f,0.0f},{-40.0f,40.0f,0.0f,0.0f},null,null}),
        new E("blademaster_am", "anim_dualguard", 5, "unrp.bm.dual", "OutCubic", false, new float[][] {null,null,{-40.0f,-40.0f,0.0f,0.0f},{-40.0f,40.0f,0.0f,0.0f},null,null}),
        new E("blademaster", "anim_greatgrip", 5, "unrp.bm.great", "OutCubic", false, new float[][] {{-8.0f,0.0f,0.0f,0.0f},{-10.0f,0.0f,0.0f,1.0f},{-155.0f,-12.0f,0.0f,0.0f},{-155.0f,12.0f,0.0f,0.0f},null,null}),
        new E("blademaster_am", "anim_greatgrip", 5, "unrp.bm.great", "OutCubic", false, new float[][] {{-8.0f,0.0f,0.0f,0.0f},{-10.0f,0.0f,0.0f,1.0f},{-155.0f,-12.0f,0.0f,0.0f},{-155.0f,12.0f,0.0f,0.0f},null,null}),
        new E("phase_shift", "anim_bulwark", 5, "unrp.phase_defense_active", "OutCubic", false, new float[][] {{8.0f,0.0f,0.0f,0.0f},{10.0f,0.0f,0.0f,-1.0f},{-50.0f,-35.0f,0.0f,0.0f},{-50.0f,35.0f,0.0f,0.0f},null,null}),
        new E("phase_shift_am", "anim_bulwark", 5, "unrp.phase_defense_active", "OutCubic", false, new float[][] {{8.0f,0.0f,0.0f,0.0f},{10.0f,0.0f,0.0f,-1.0f},{-50.0f,-35.0f,0.0f,0.0f},{-50.0f,35.0f,0.0f,0.0f},null,null}),
        new E("phase_shift", "anim_streamline", 6, "unrp.phase_speed_active", "InOutSine", false, new float[][] {{-4.0f,0.0f,0.0f,0.0f},{8.0f,0.0f,0.0f,-1.0f},{18.0f,0.0f,8.0f,0.0f},{18.0f,0.0f,-8.0f,0.0f},null,null}),
        new E("phase_shift_am", "anim_streamline", 6, "unrp.phase_speed_active", "InOutSine", false, new float[][] {{-4.0f,0.0f,0.0f,0.0f},{8.0f,0.0f,0.0f,-1.0f},{18.0f,0.0f,8.0f,0.0f},{18.0f,0.0f,-8.0f,0.0f},null,null}),
        new E("sanctuary_am", "anim_grace", 8, "unrp.sanct.circuit", "InOutSine", false, new float[][] {{12.0f,0.0f,0.0f,0.0f},null,{-20.0f,0.0f,35.0f,0.0f},{-20.0f,0.0f,-35.0f,0.0f},null,null}),
        new E("deadeye_am", "anim_snipe", 5, "unrp.deadeye_sniper", "OutCubic", false, new float[][] {null,{0.0f,-20.0f,0.0f,0.0f},{-90.0f,-4.0f,0.0f,0.0f},{-70.0f,55.0f,0.0f,0.0f},null,null}),
        new E("deadeye_am", "anim_fan", 4, "unrp.deadeye_barrage", "OutCubic", false, new float[][] {null,null,{-85.0f,-6.0f,0.0f,0.0f},{-85.0f,6.0f,0.0f,0.0f},null,null}),
        new E("volt_deadeye_am", "anim_snipe", 5, "unrp.deadeye_sniper", "OutCubic", false, new float[][] {null,{0.0f,-20.0f,0.0f,0.0f},{-90.0f,-4.0f,0.0f,0.0f},{-70.0f,55.0f,0.0f,0.0f},null,null}),
        new E("volt_deadeye_am", "anim_fan", 4, "unrp.deadeye_barrage", "OutCubic", false, new float[][] {null,null,{-85.0f,-6.0f,0.0f,0.0f},{-85.0f,6.0f,0.0f,0.0f},null,null}),
        new E("tempest_claws_am", "anim_soar", 6, "unrp.am_shown.tempest_claws", "InOutSine", false, new float[][] {null,{6.0f,0.0f,0.0f,-1.0f},{-170.0f,0.0f,4.0f,0.0f},{25.0f,0.0f,-6.0f,0.0f},{8.0f,0.0f,0.0f,0.0f},{8.0f,0.0f,0.0f,0.0f}}),
        new E("tempest_claws_am", "anim_cyclone", 5, "unrp.tempest_cyclone", "InOutSine", false, new float[][] {{-6.0f,0.0f,0.0f,0.0f},null,{-10.0f,0.0f,80.0f,0.0f},{-10.0f,0.0f,-80.0f,0.0f},null,null}),
        new E("aura_shred_am", "anim_latch", 5, "unrp.aura_shred_latch", "OutCubic", false, new float[][] {null,{8.0f,0.0f,0.0f,-1.0f},{-88.0f,-8.0f,0.0f,0.0f},{-30.0f,12.0f,0.0f,0.0f},null,null}),
        new E("contact_shred", "anim_grip", 5, "unrp.contact_shred_drain", "OutCubic", false, new float[][] {{8.0f,0.0f,0.0f,0.0f},{10.0f,0.0f,0.0f,-1.0f},{-70.0f,-14.0f,0.0f,0.0f},{-40.0f,18.0f,0.0f,0.0f},null,null}),
        new E("contact_shred_am", "anim_grip", 5, "unrp.contact_shred_drain", "OutCubic", false, new float[][] {{8.0f,0.0f,0.0f,0.0f},{10.0f,0.0f,0.0f,-1.0f},{-70.0f,-14.0f,0.0f,0.0f},{-40.0f,18.0f,0.0f,0.0f},null,null}),
        new E("conjure_disks", "anim_coil", 3, "unrp.pose.dl_coil", "OutCubic", false, new float[][] {{0.0f,-12.0f,0.0f,0.0f},{0.0f,25.0f,0.0f,0.0f},{-60.0f,55.0f,0.0f,0.0f},null,null,null}),
        new E("conjure_disks_am", "anim_coil", 3, "unrp.pose.dl_coil", "OutCubic", false, new float[][] {{0.0f,-12.0f,0.0f,0.0f},{0.0f,25.0f,0.0f,0.0f},{-60.0f,55.0f,0.0f,0.0f},null,null,null}),
        new E("conjure_disks", "anim_snap", 4, "unrp.pose.dl_snap", "OutBack", false, new float[][] {null,{0.0f,-12.0f,0.0f,0.0f},{-85.0f,-15.0f,0.0f,0.0f},null,null,null}),
        new E("conjure_disks_am", "anim_snap", 4, "unrp.pose.dl_snap", "OutBack", false, new float[][] {null,{0.0f,-12.0f,0.0f,0.0f},{-85.0f,-15.0f,0.0f,0.0f},null,null,null}),
        new E("nightmare", "anim_clutch", 4, "unrp.pose.np_clutch", "OutCubic", false, new float[][] {{18.0f,0.0f,0.0f,0.0f},{12.0f,0.0f,0.0f,-1.0f},{-140.0f,-70.0f,0.0f,0.0f},{-140.0f,70.0f,0.0f,0.0f},null,null}),
        new E("nightmare_am", "anim_clutch", 4, "unrp.pose.np_clutch", "OutCubic", false, new float[][] {{18.0f,0.0f,0.0f,0.0f},{12.0f,0.0f,0.0f,-1.0f},{-140.0f,-70.0f,0.0f,0.0f},{-140.0f,70.0f,0.0f,0.0f},null,null}),
        new E("nightmare", "anim_fling", 4, "unrp.pose.np_fling", "OutBack", false, new float[][] {{-10.0f,0.0f,0.0f,0.0f},null,{-60.0f,-40.0f,0.0f,0.0f},{-60.0f,40.0f,0.0f,0.0f},null,null}),
        new E("nightmare_am", "anim_fling", 4, "unrp.pose.np_fling", "OutBack", false, new float[][] {{-10.0f,0.0f,0.0f,0.0f},null,{-60.0f,-40.0f,0.0f,0.0f},{-60.0f,40.0f,0.0f,0.0f},null,null}),
        new E("nightmare", "anim_reach", 5, "unrp.pose.nm_reach", "InOutSine", false, new float[][] {{4.0f,0.0f,12.0f,0.0f},null,{-75.0f,-25.0f,20.0f,0.0f},null,null,null}),
        new E("nightmare_am", "anim_reach", 5, "unrp.pose.nm_reach", "InOutSine", false, new float[][] {{4.0f,0.0f,12.0f,0.0f},null,{-75.0f,-25.0f,20.0f,0.0f},null,null,null}),
        new E("spectral_claw", "anim_gripclaw", 4, "unrp.pose.sc_grip", "OutBack", false, new float[][] {null,{14.0f,0.0f,0.0f,-1.0f},{-95.0f,-6.0f,0.0f,0.0f},{30.0f,10.0f,0.0f,0.0f},null,null}),
        new E("spectral_claw_am", "anim_gripclaw", 4, "unrp.pose.sc_grip", "OutBack", false, new float[][] {null,{14.0f,0.0f,0.0f,-1.0f},{-95.0f,-6.0f,0.0f,0.0f},{30.0f,10.0f,0.0f,0.0f},null,null}),
        new E("spectral_claw", "anim_lob", 4, "unrp.pose.sc_throw", "OutCubic", false, new float[][] {null,{-8.0f,0.0f,0.0f,1.0f},{-150.0f,10.0f,0.0f,0.0f},null,null,null}),
        new E("spectral_claw_am", "anim_lob", 4, "unrp.pose.sc_throw", "OutCubic", false, new float[][] {null,{-8.0f,0.0f,0.0f,1.0f},{-150.0f,10.0f,0.0f,0.0f},null,null,null}),
        new E("spikes", "anim_geolift", 4, "unrp.pose.spk_field", "OutBack", false, new float[][] {null,{-6.0f,0.0f,0.0f,1.0f},{-45.0f,-10.0f,0.0f,0.0f},{-45.0f,10.0f,0.0f,0.0f},null,null}),
        new E("spikes_am", "anim_geolift", 4, "unrp.pose.spk_field", "OutBack", false, new float[][] {null,{-6.0f,0.0f,0.0f,1.0f},{-45.0f,-10.0f,0.0f,0.0f},{-45.0f,10.0f,0.0f,0.0f},null,null}),
        new E("armor_suit", "anim_brace", 5, "unrp.pose.as_brace", "OutCubic", false, new float[][] {{12.0f,0.0f,0.0f,0.0f},{8.0f,0.0f,0.0f,-1.0f},{-85.0f,-58.0f,0.0f,0.0f},{-85.0f,58.0f,0.0f,0.0f},null,null}),
        new E("armor_suit_am", "anim_brace", 5, "unrp.pose.as_brace", "OutCubic", false, new float[][] {{12.0f,0.0f,0.0f,0.0f},{8.0f,0.0f,0.0f,-1.0f},{-85.0f,-58.0f,0.0f,0.0f},{-85.0f,58.0f,0.0f,0.0f},null,null}),
        new E("armor_suit", "anim_fan", 4, "unrp.pose.as_fan", "OutCubic", false, new float[][] {null,{-4.0f,0.0f,0.0f,1.0f},{-30.0f,0.0f,30.0f,0.0f},{-30.0f,0.0f,-30.0f,0.0f},null,null}),
        new E("armor_suit_am", "anim_fan", 4, "unrp.pose.as_fan", "OutCubic", false, new float[][] {null,{-4.0f,0.0f,0.0f,1.0f},{-30.0f,0.0f,30.0f,0.0f},{-30.0f,0.0f,-30.0f,0.0f},null,null}),
        new E("livewire", "anim_dome", 5, "unrp.pose.lw_dome", "OutCubic", false, new float[][] {{-14.0f,0.0f,0.0f,0.0f},null,{-150.0f,0.0f,35.0f,0.0f},{-150.0f,0.0f,-35.0f,0.0f},null,null}),
        new E("livewire_am", "anim_dome", 5, "unrp.pose.lw_dome", "OutCubic", false, new float[][] {{-14.0f,0.0f,0.0f,0.0f},null,{-150.0f,0.0f,35.0f,0.0f},{-150.0f,0.0f,-35.0f,0.0f},null,null}),
        new E("arcroot", "anim_whip", 5, "unrp.pose.ar_lash", "OutElastic", false, new float[][] {null,{6.0f,8.0f,0.0f,0.0f},{-50.0f,-20.0f,0.0f,0.0f},null,null,null}),
        new E("arcroot_am", "anim_whip", 5, "unrp.pose.ar_lash", "OutElastic", false, new float[][] {null,{6.0f,8.0f,0.0f,0.0f},{-50.0f,-20.0f,0.0f,0.0f},null,null,null}),
        new E("vine_barrier", "anim_wallraise", 5, "unrp.pose.vb_wall", "OutCubic", false, new float[][] {null,{-4.0f,0.0f,0.0f,1.0f},{-70.0f,-8.0f,0.0f,0.0f},{-70.0f,8.0f,0.0f,0.0f},null,null}),
        new E("vine_barrier_am", "anim_wallraise", 5, "unrp.pose.vb_wall", "OutCubic", false, new float[][] {null,{-4.0f,0.0f,0.0f,1.0f},{-70.0f,-8.0f,0.0f,0.0f},{-70.0f,8.0f,0.0f,0.0f},null,null}),
        new E("dizzy_punch", "anim_spin", 10, "unrp.pose.dz_spin", "Linear", true, new float[][] {null,{0.0f,360.0f,0.0f,0.0f},{-10.0f,0.0f,70.0f,0.0f},{-10.0f,0.0f,-70.0f,0.0f},null,null}),
        new E("dizzy_punch_am", "anim_spin", 10, "unrp.pose.dz_spin", "Linear", true, new float[][] {null,{0.0f,360.0f,0.0f,0.0f},{-10.0f,0.0f,70.0f,0.0f},{-10.0f,0.0f,-70.0f,0.0f},null,null}),
        new E("catalyst_ray", "anim_ray", 4, "unrp.pose.cr_ray", "OutCubic", false, new float[][] {null,{0.0f,-10.0f,0.0f,0.0f},{-88.0f,-4.0f,0.0f,0.0f},{-35.0f,45.0f,0.0f,0.0f},null,null}),
        new E("catalyst_ray_am", "anim_ray", 4, "unrp.pose.cr_ray", "OutCubic", false, new float[][] {null,{0.0f,-10.0f,0.0f,0.0f},{-88.0f,-4.0f,0.0f,0.0f},{-35.0f,45.0f,0.0f,0.0f},null,null}),
        new E("contact_shred", "anim_rakeup", 3, "unrp.pose.cs_rake_up", "OutCubic", false, new float[][] {null,{0.0f,20.0f,0.0f,0.0f},{-130.0f,40.0f,0.0f,0.0f},null,null,null}),
        new E("contact_shred_am", "anim_rakeup", 3, "unrp.pose.cs_rake_up", "OutCubic", false, new float[][] {null,{0.0f,20.0f,0.0f,0.0f},{-130.0f,40.0f,0.0f,0.0f},null,null,null}),
        new E("contact_shred", "anim_rakedown", 3, "unrp.pose.cs_rake_down", "OutCubic", false, new float[][] {null,{8.0f,-15.0f,0.0f,-1.0f},{-30.0f,-30.0f,0.0f,0.0f},null,null,null}),
        new E("contact_shred_am", "anim_rakedown", 3, "unrp.pose.cs_rake_down", "OutCubic", false, new float[][] {null,{8.0f,-15.0f,0.0f,-1.0f},{-30.0f,-30.0f,0.0f,0.0f},null,null,null}),
        new E("sarcophage", "sc_t_grab", 12, "unrp.pose.sc_grab", "OutBack", false, new float[][] {{4.0f,0.0f,0.0f,0.0f},{10.0f,-12.0f,0.0f,-1.0f},{-95.0f,-8.0f,0.0f,0.0f},{25.0f,10.0f,0.0f,0.0f},null,null}),
        new E("sarcophage", "sc_t_spray", 10, "unrp.pose.sc_spray", "OutCubic", false, new float[][] {null,{4.0f,0.0f,0.0f,0.0f},{-92.0f,-20.0f,0.0f,0.0f},{-92.0f,20.0f,0.0f,0.0f},null,null}),
        new E("sarcophage", "sc_t_sever", 12, "unrp.pose.sc_sever", "OutCubic", false, new float[][] {{0.0f,-10.0f,0.0f,0.0f},{0.0f,25.0f,0.0f,0.0f},{-120.0f,45.0f,0.0f,0.0f},null,null,null}),
        new E("sarcophage", "sc_t_coffin", 12, "unrp.pose.sc_coffin", "OutBack", false, new float[][] {{-8.0f,0.0f,0.0f,0.0f},{-6.0f,0.0f,0.0f,1.0f},{-100.0f,-30.0f,0.0f,0.0f},{-100.0f,30.0f,0.0f,0.0f},null,null}),
        new E("sarcophage", "sc_t_shroud", 8, "unrp.pose.sc_shroud", "InOutSine", false, new float[][] {null,{4.0f,0.0f,0.0f,0.0f},{-35.0f,-15.0f,0.0f,0.0f},{-35.0f,15.0f,0.0f,0.0f},null,null}),
        new E("sarcophage", "sc_t_tempest", 10, "unrp.pose.sc_tempest", "InOutSine", false, new float[][] {{-10.0f,0.0f,0.0f,0.0f},{-8.0f,0.0f,0.0f,1.0f},{-70.0f,0.0f,60.0f,0.0f},{-70.0f,0.0f,-60.0f,0.0f},null,null}),
        new E("sarcophage", "sc_t_sprout", 20, "unrp.pose.sc_sprout", "OutBack", false, new float[][] {{-18.0f,0.0f,0.0f,0.0f},{-14.0f,0.0f,0.0f,1.0f},{-50.0f,0.0f,40.0f,0.0f},{-50.0f,0.0f,-40.0f,0.0f},null,null}),
        new E("edict", "law_t_pass", 12, "unrp.pose.law_pass", "OutBack", false, new float[][] {{-10.0f,0.0f,0.0f,0.0f},{-8.0f,0.0f,0.0f,0.0f},{-155.0f,-8.0f,6.0f,0.0f},{-70.0f,30.0f,0.0f,0.0f},null,null}),
        new E("edict", "law_t_gavel", 12, "unrp.pose.law_gavel", "OutCubic", false, new float[][] {{6.0f,0.0f,0.0f,0.0f},{16.0f,-10.0f,0.0f,-1.0f},{-95.0f,-20.0f,0.0f,0.0f},{15.0f,8.0f,0.0f,0.0f},null,null}),
        new E("edict", "law_t_seizure", 12, "unrp.pose.law_seizure", "OutBack", false, new float[][] {null,{8.0f,0.0f,0.0f,0.0f},{-90.0f,-35.0f,0.0f,0.0f},{-90.0f,35.0f,0.0f,0.0f},null,null}),
        new E("edict", "law_t_rankup", 20, "unrp.pose.law_rankup", "InOutSine", false, new float[][] {{-16.0f,0.0f,0.0f,0.0f},{-12.0f,0.0f,0.0f,1.0f},{-70.0f,0.0f,55.0f,0.0f},{-70.0f,0.0f,-55.0f,0.0f},null,null}),
        new E("edict", "law_t_colossus", 10, "unrp.pose.law_colossus", "InOutSine", false, new float[][] {{-6.0f,0.0f,0.0f,0.0f},{-6.0f,0.0f,0.0f,0.0f},{-25.0f,0.0f,30.0f,0.0f},{-25.0f,0.0f,-30.0f,0.0f},null,null}),
        new E("edict", "law_t_inviolable", 10, "unrp.pose.law_inviolable", "InOutSine", false, new float[][] {{-4.0f,0.0f,0.0f,0.0f},{-4.0f,0.0f,0.0f,0.0f},{-40.0f,0.0f,20.0f,0.0f},{-40.0f,0.0f,-20.0f,0.0f},null,null}),
        new E("wyrm_engine", "we_t_boost", 10, "unrp.pose.we_boost", "OutBack", false, new float[][] {{4.0f,0.0f,0.0f,0.0f},{6.0f,0.0f,0.0f,0.0f},{-65.0f,-25.0f,0.0f,0.0f},{-65.0f,25.0f,0.0f,0.0f},null,null}),
        new E("wyrm_engine", "we_t_shot", 12, "unrp.pose.we_shot", "OutCubic", false, new float[][] {{2.0f,0.0f,0.0f,0.0f},{8.0f,-14.0f,0.0f,-1.0f},{-92.0f,-10.0f,0.0f,0.0f},{20.0f,12.0f,0.0f,0.0f},null,null}),
        new E("wyrm_engine", "we_t_art", 16, "unrp.pose.we_art", "OutBack", false, new float[][] {{-8.0f,0.0f,0.0f,0.0f},{-6.0f,0.0f,0.0f,0.0f},{-110.0f,-30.0f,0.0f,0.0f},{-110.0f,30.0f,0.0f,0.0f},null,null}),
        new E("wyrm_engine", "we_t_form", 18, "unrp.pose.we_form", "InOutSine", false, new float[][] {{-12.0f,0.0f,0.0f,0.0f},{-10.0f,0.0f,0.0f,1.0f},{-55.0f,0.0f,65.0f,0.0f},{-55.0f,0.0f,-65.0f,0.0f},null,null}),
        new E("wyrm_engine", "we_t_lance", 20, "unrp.pose.we_lance", "OutCubic", false, new float[][] {{-14.0f,0.0f,0.0f,0.0f},{-10.0f,0.0f,0.0f,0.0f},{-155.0f,-12.0f,0.0f,0.0f},{-155.0f,12.0f,0.0f,0.0f},null,null}),
        new E("wyrm_engine", "we_t_rankup", 18, "unrp.pose.we_rankup", "OutBack", false, new float[][] {{-8.0f,0.0f,0.0f,0.0f},{-6.0f,0.0f,0.0f,0.0f},{-80.0f,0.0f,30.0f,0.0f},{-35.0f,20.0f,0.0f,0.0f},null,null}),
    };

    record Cw(String timer, int max, String tag, String ease, float[][] fp, float[][] tp, float chest, float head) {}

    static final Cw[] CW = {
        new Cw("t_cw_guard", 8, "unrp.pose.cw_guard", "OutBack", new float[][] {{-38, -22, 0}, {-38, 22, 0}}, new float[][] {{-74, -38, 0, 1}, {-74, 38, 0, 1}}, -6, 10),
        new Cw("t_cw_wave", 12, "unrp.pose.cw_wave", "OutCubic", new float[][] {{-52, 0, 8}, {-52, 0, -8}}, new float[][] {{-88, -10, 6, 2}, {-88, 10, -6, 2}}, -12, -6),
        new Cw("t_cw_fist", 12, "unrp.pose.cw_fist", "OutBack", new float[][] {{-58, 0, 0}, {30, 0, 0}}, new float[][] {{-94, -6, 0, 3}, {46, 8, 0, -1}}, -14, -8),
        new Cw("t_cw_point", 16, "unrp.pose.cw_point", "OutQuad", new float[][] {{-56, -4, 0}, {22, 0, 0}}, new float[][] {{-90, -2, 0, 2}, {38, 10, 0, -1}}, -8, -10),
        new Cw("t_cw_bound", 10, "unrp.pose.cw_bound", "OutCubic", new float[][] {{26, 0, 6}, {26, 0, -6}}, new float[][] {{52, 14, 10, -2}, {52, -14, -10, -2}}, 18, 14),
        new Cw("t_cw_reknit", 60, "unrp.pose.cw_reknit", "InOutCubic", new float[][] {{-30, -28, 0}, {-30, 28, 0}}, new float[][] {{-52, -46, 0, 1}, {-52, 46, 0, 1}}, 10, 16),
    };

    private static final Map<Integer, Map<String, int[]>> TIMERS = new HashMap<>();

    static boolean has(java.util.UUID id, String tag) {
        var s = PassBridge.STATE_VIEW.apply(id);
        return s != null && s.contains(tag);
    }

    static String path(java.util.UUID id) {
        return PassBridge.PATH_VIEW.apply(id);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            TIMERS.clear();
            return;
        }
        if (mc.isPaused()) return;
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (AbstractClientPlayer p : mc.level.players()) {
            seen.add(p.getId());
            java.util.Set<String> held = PassBridge.paths(p.getUUID());
            Map<String, int[]> t = TIMERS.computeIfAbsent(p.getId(), k -> new HashMap<>());
            for (E e : ROSTER) step(t, e.power + "/" + e.timer, e.max, held.contains(e.power) && has(p.getUUID(), e.tag));
            for (Cw c : CW) step(t, "counterweight/" + c.timer, c.max, held.contains("counterweight") && has(p.getUUID(), c.tag));
        }
        TIMERS.keySet().retainAll(seen);
    }

    private static void step(Map<String, int[]> t, String key, int max, boolean on) {
        int[] v = t.get(key);
        if (v == null) {
            if (!on) return;
            v = new int[2];
            t.put(key, v);
        }
        v[0] = v[1];
        v[1] = on ? Math.min(max, v[1] + 1) : Math.max(0, v[1] - 1);
    }

    static double value(Map<String, int[]> t, String key, int max, float pt) {
        int[] v = t == null ? null : t.get(key);
        if (v == null || max <= 0) return 0;
        double x = (v[0] + (v[1] - v[0]) * pt) / max;
        return x < 1 ? x : 1; // poseShown
    }

    static double ease(String name, double x) {
        return switch (name) {
            case "Linear" -> x;
            case "InOutSine" -> -(Math.cos(Math.PI * x) - 1) / 2;
            case "OutCubic" -> 1 - Math.pow(1 - x, 3);
            case "InOutCubic" -> x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
            case "OutBack" -> 1 + 2.70158 * Math.pow(x - 1, 3) + 1.70158 * Math.pow(x - 1, 2);
            case "OutElastic" -> x <= 0 ? 0 : x >= 1 ? 1 : Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * (2 * Math.PI / 3)) + 1;
            case "OutQuad" -> 1 - (1 - x) * (1 - x);
            default -> x;
        };
    }

    private static final float D2R = (float) (Math.PI / 180.0);

    static float lerp(double e, float a, float b) {
        return (float) (a + (b - a) * e);
    }

    public static void apply(PlayerModel<?> m, LivingEntity entity, float age) {
        if (!(entity instanceof AbstractClientPlayer p)) return;
        Map<String, int[]> t = TIMERS.get(p.getId());
        if (t == null || t.isEmpty()) return;
        float pt = Minecraft.getInstance().getFrameTime();
        java.util.Set<String> held = PassBridge.paths(p.getUUID());
        boolean touched = false;
        ModelPart[] parts = {m.head, m.body, m.rightArm, m.leftArm, m.rightLeg, m.leftLeg};
        for (E e : ROSTER) {
            if (!held.contains(e.power)) continue;
            double anim = value(t, e.power + "/" + e.timer, e.max, pt);
            if (anim <= 0) continue;
            double k = ease(e.ease, anim);
            for (int i = 0; i < 6; i++) {
                float[] v = e.parts[i];
                if (v == null) continue;
                ModelPart part = parts[i];
                part.xRot = lerp(k, part.xRot, v[0] * D2R);
                if (v[1] != 0) part.yRot = lerp(k, part.yRot, v[1] * D2R);
                if (v[2] != 0) part.zRot = lerp(k, part.zRot, v[2] * D2R);
            }
            float[] chest = e.parts[1];
            if (chest != null && Math.abs(k) >= 0.0001) {
                double th = chest[0] * k * Math.PI / 180, ps = chest[1] * k * Math.PI / 180;
                double st = Math.sin(th), ct = Math.cos(th), sp = Math.sin(ps), cp = Math.cos(ps);
                double nx = -12 * st * sp, ny = 12 - 12 * ct, nz = -12 * st * cp;
                double[][] at = {{nx, ny, nz}, {nx, ny, nz},
                        {nx + 5 - 5 * cp + 2 * st * sp, ny + 2 * ct - 2, nz + 5 * sp + 2 * st * cp},
                        {nx - 5 + 5 * cp + 2 * st * sp, ny + 2 * ct - 2, nz - 5 * sp + 2 * st * cp}};
                for (int i = 0; i < 4; i++) {
                    ModelPart part = parts[i];
                    part.x += (float) at[i][0];
                    part.y += (float) at[i][1];
                    part.z += (float) at[i][2];
                    if (e.carry && i != 1) part.yRot += (float) (chest[1] * k) * D2R;
                }
            }
            touched = true;
        }
        if (held.contains("counterweight")) {
            for (Cw c : CW) {
                double anim = value(t, "counterweight/" + c.timer, c.max, pt);
                if (anim <= 0) continue;
                double k = ease(c.ease, anim);
                ModelPart[] arms = {m.rightArm, m.leftArm};
                for (int i = 0; i < 2; i++) {
                    float[] v = c.tp[i];
                    arms[i].xRot = lerp(k, arms[i].xRot, v[0] * D2R);
                    if (v[1] != 0) arms[i].yRot = lerp(k, arms[i].yRot, v[1] * D2R);
                    if (v[2] != 0) arms[i].zRot = lerp(k, arms[i].zRot, v[2] * D2R);
                    if (v.length > 3 && v[3] != 0) arms[i].z += (float) (v[3] * k);
                }
                m.body.xRot = lerp(k, m.body.xRot, c.chest * D2R);
                m.head.xRot = lerp(k, m.head.xRot, c.head * D2R);
                touched = true;
            }
        }
        if (touched) {
            m.hat.copyFrom(m.head);
            m.jacket.copyFrom(m.body);
            m.rightSleeve.copyFrom(m.rightArm);
            m.leftSleeve.copyFrom(m.leftArm);
            m.rightPants.copyFrom(m.rightLeg);
            m.leftPants.copyFrom(m.leftLeg);
        }
    }

    public static void firstPerson(AbstractClientPlayer p, ModelPart arm, boolean right) {
        Map<String, int[]> t = TIMERS.get(p.getId());
        if (t == null) return;
        float pt = Minecraft.getInstance().getFrameTime();
        java.util.Set<String> held = PassBridge.paths(p.getUUID());
        for (E e : ROSTER) {
            if (!held.contains(e.power)) continue;
            double anim = value(t, e.power + "/" + e.timer, e.max, pt);
            float[] v = e.parts[right ? 2 : 3];
            if (anim <= 0 || v == null) continue;
            double k = ease(e.ease, anim);
            arm.xRot = lerp(k, arm.xRot, v[0] * 0.35f * D2R);
            arm.zRot = lerp(k, arm.zRot, v[2] * 0.3f * D2R);
        }
    }
}
