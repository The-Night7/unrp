package com.myriam.unrp.uo;

import com.myriam.unrp.ability.AbilityEngine;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Ce que le code du modèle lit dans le monde (scores et tags posés par Palladium et ScoreboardBootstrap), tenu à jour
 * depuis notre moteur de pouvoirs :
 * - score « unrp.aura » = notre aura ; ce que le code du modèle y retire (ou y rend) est reporté sur notre aura ;
 * - tag « unrp.power_disabled » tant que le pouvoir est suspendu.
 */
@EventBusSubscriber(modid = "unrp")
public final class ModelEnv {
    private ModelEnv() {}

    public static final String AURA = "unrp.aura";
    public static final String DISABLED = "unrp.power_disabled";
    public static final String DEPLETED_TAG = "unrp.aura_depleted";
    private static final Map<UUID, Integer> WRITTEN = new HashMap<>();

    /** ScoreboardBootstrap du modèle : objectifs créés au démarrage du serveur. */
    private static final String[] OBJECTIVES = new String[]{"unrp.copy_selected", "unrp.expel_trigger", "unrp.copied_count", "unrp.nearby_1", "unrp.nearby_2", "unrp.nearby_3", "unrp.nearby_4", "unrp.nearby_5", "unrp.nearby_6", "unrp.nearby_7", "unrp.nearby_8", "unrp.nearby_9", "unrp.nearby_10", "unrp.nearby_11", "unrp.nearby_12", "unrp.nearby_13", "unrp.nearby_14", "unrp.nearby_15", "unrp.nearby_16", "unrp.nearby_17", "unrp.nearby_18", "unrp.nearby_19", "unrp.nearby_20", "unrp.nearby_21", "unrp.nearby_22", "unrp.nearby_23", "unrp.nearby_24", "unrp.nearby_25", "unrp.nearby_26", "unrp.nearby_27", "unrp.nearby_28", "unrp.nearby_29", "unrp.nearby_30", "unrp.nearby_31", "unrp.nearby_32", "unrp.nearby_33", "unrp.nearby_34", "unrp.nearby_35", "unrp.nearby_36", "unrp.nearby_37", "unrp.nearby_38", "unrp.nearby_39", "unrp.nearby_40", "unrp.nearby_41", "unrp.nearby_42", "unrp.nearby_43", "unrp.nearby_44", "unrp.nearby_45", "unrp.nearby_46", "unrp.nearby_47", "unrp.nearby_48", "unrp.mastery", "unrp.nether_kills", "unrp.mastery_xp", "unrp.aura_max", "unrp.pot_power", "unrp.pot_speed", "unrp.pot_trick", "unrp.pot_recovery", "unrp.pot_defense", "unrp.orig_power", "unrp.orig_speed", "unrp.orig_trick", "unrp.orig_recovery", "unrp.orig_defense", "unrp.copy_orig_power", "unrp.copy_orig_speed", "unrp.copy_orig_trick", "unrp.copy_orig_recovery", "unrp.copy_orig_defense", "unrp.copy_power_idx", "unrp.stats_copied", "power_level", "unrp.combine_selected", "unrp.combo_1", "unrp.combo_2", "unrp.combo_3", "unrp.combo_4", "unrp.combo_5", "unrp.combo_6", "unrp.combo_7", "unrp.combo_8", "unrp.combo_9", "unrp.combo_10", "unrp.combo_11", "unrp.combo_12", "unrp.combo_13", "unrp.combo_14", "unrp.stored_aura", "unrp.reputation", "unrp.temp_level", "unrp.amp_timer", "unrp.deamp_timer", "unrp.mroll_power", "unrp.mroll_speed", "unrp.mroll_trick", "unrp.mroll_recovery", "unrp.mroll_defense", "unrp.origin_mxp", "unrp.level_cap", "unrp.beyond", "unrp.dm_wounds", "unrp.disable_timer", "unrp.ember_cd", "unrp.spectre_cd", "unrp.spectre_base", "unrp.expel_pending", "unrp.shred_hud", "unrp.tent_cd", "unrp.camp_cd", "unrp.spectre_grace", "unrp.tent_pending", "unrp.camp_pending", "unrp.sc_reserve", "unrp.vital_sand", "unrp.sg_mass", "unrp.sg_absorbed", "unrp.sg_control", "unrp.sg_dial", "unrp.sg_fold", "unrp.sg_fold_world", "unrp.sg_grav", "unrp.lb_grit", "unrp.lb_ki", "unrp.og_level", "unrp.og_xp", "unrp.og_growth", "unrp.cw_charge", "unrp.cw_momentum", "unrp.cw_stage", "unrp.cm_weave", "unrp.cm_woven", "unrp.cm_stage", "unrp.cm_travel", "unrp.cm_rerouted", "unrp.hm_spent", "unrp.hm_stage", "unrp.hm_r0", "unrp.hm_r1", "unrp.hm_r2", "unrp.hm_r3", "unrp.hm_r4", "unrp.hm_r5", "unrp.hm_r6", "unrp.hm_r7", "unrp.hm_r8", "unrp.hm_r9", "unrp.hm_r10", "unrp.hm_r11", "unrp.hm_r12", "unrp.hm_r13", "unrp.nd_heft", "unrp.nd_poise", "unrp.nd_stage", "unrp.origin_choice", "unrp.study_aug", "unrp.study_med", "unrp.study_proj", "unrp.study_con", "unrp.study_qtm", "unrp.ps_pow", "unrp.ps_spd", "unrp.ps_trk", "unrp.ps_rec", "unrp.ps_def", "unrp.ps_form", "unrp.duel_power_trigger", "unrp.duel_level", "unrp.pattern", "unrp.nearby_49", "unrp.motes", "unrp.motes_max", "unrp.nxgen", "unrp.camp_last", "unrp.tent_last", "unrp.rules", "unrp.taken", "unrp.syn_count", "unrp.spectre_member", "unrp.sq_id", "unrp.sq_prog", "unrp.sq_state", "unrp.sq_start", "unrp.sq_next", "unrp.ember_member", "unrp.eq_id", "unrp.eq_prog", "unrp.eq_state", "unrp.eq_start", "unrp.eq_next", "unrp.config", "unrp.cfg_pool", "unrp.cfg_mobs", "unrp.cfg_mobchance", "unrp.cfg_mobpool", "unrp.rewrite_trigger", "unrp.at_sel", "unrp.nearby_50", "unrp.nearby_51", "unrp.nearby_52", "unrp.nearby_53", "unrp.nearby_54", "unrp.ad_slot_1", "unrp.ad_slot_2", "unrp.ad_slot_3", "unrp.ad_slot_4", "unrp.ad_slot_5", "unrp.ad_slot_6", "unrp.ad_slot_7", "unrp.ad_slot_8", "unrp.ad_slot_9", "unrp.ad_slot_10", "unrp.ad_slot_11", "unrp.ad_slot_12", "unrp.ad_sel", "unrp.precedent", "unrp.law_rank", "unrp.law_sel", "unrp.law_trials", "unrp.law_contest", "unrp.we_stacks", "unrp.we_slots", "unrp.we_cycles", "unrp.we_trials", "unrp.we_ruin_node", "unrp.we_ruin_buys", "unrp.we_fatigue"};

    @SubscribeEvent
    public static void onServerStarted(net.minecraftforge.event.server.ServerStartedEvent e) {
        ServerScoreboard sb = e.getServer().getScoreboard();
        for (String name : OBJECTIVES)
            if (sb.getObjective(name) == null) sb.addObjective(name, ObjectiveCriteria.DUMMY, Component.literal(name), ObjectiveCriteria.RenderType.INTEGER);
        if (sb.getObjective(AURA) == null) sb.addObjective(AURA, ObjectiveCriteria.DUMMY, Component.literal(AURA), ObjectiveCriteria.RenderType.INTEGER);
    }

    static Objective objective(ServerPlayer p, String name) {
        ServerScoreboard sb = p.server.getScoreboard();
        Objective o = sb.getObjective(name);
        if (o == null) o = sb.addObjective(name, ObjectiveCriteria.DUMMY, Component.literal(name), ObjectiveCriteria.RenderType.INTEGER);
        return o;
    }

    /** Reporte sur notre aura ce que le code du modèle a changé au score, puis réécrit le score. */
    public static void sync(ServerPlayer p) {
        ServerScoreboard sb = p.server.getScoreboard();
        Objective o = objective(p, AURA);
        var score = sb.getOrCreatePlayerScore(p.getScoreboardName(), o);
        Integer last = WRITTEN.get(p.getUUID());
        if (last != null) {
            int delta = score.getScore() - last;
            if (delta < 0) AbilityEngine.spendAura(p, -delta);
            else if (delta > 0) AbilityEngine.refundAura(p, delta);
        }
        int now = AbilityEngine.aura(p);
        score.setScore(now);
        WRITTEN.put(p.getUUID(), now);
        Objective lv = p.server.getScoreboard().getObjective("power_level");
        if (lv != null) sb.getOrCreatePlayerScore(p.getScoreboardName(), lv).setScore(powerLevel(p)); // niveau x 10, comme le modèle
        if (now <= 0 && !com.myriam.unrp.ability.Superpowers.held(p).isEmpty()) p.addTag(DEPLETED_TAG); // aura vide
        else p.removeTag(DEPLETED_TAG);
        if (AbilityEngine.disabled(p)) p.addTag(DISABLED);
        if (p.tickCount % 5 == 0 && p.getTags().contains("unrp.has.particles")) { // part « essaim » du StatSyncPacket
            int flags = (p.getTags().contains("unrp.active.particles") ? 1 : 0) | (p.getTags().contains("unrp.stance.3") ? 2 : 0);
            for (int i = 0; i < 4; i++) if (p.getTags().contains("unrp.stance." + i)) flags |= (4 + i) << 4; // posture pour l'icône Palladium
            com.myriam.unrp.net.Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> p),
                    new WeaveSyncPacket(read(p, "unrp.motes"), read(p, "unrp.motes_max"), flags));
        }
        else p.removeTag(DISABLED);
    }

    static int read(ServerPlayer p, String name) {
        Objective o = p.server.getScoreboard().getObjective(name);
        return o == null || !p.server.getScoreboard().hasPlayerScore(p.getScoreboardName(), o) ? 0 : p.server.getScoreboard().getOrCreatePlayerScore(p.getScoreboardName(), o).getScore();
    }

    static int powerLevel(ServerPlayer p) {
        return (int) Math.round(com.myriam.unrp.data.ClaimData.get(p.server).profileOf(p.getUUID()).map(pr -> pr.level * 10 * AbilityEngine.restoredPercent(p)/100).orElse(0.0));
    }

    /** Avant tout le code du modèle (abonné en phase END) : état à jour. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onTick(TickEvent.PlayerTickEvent e) {
        if (e.player instanceof ServerPlayer p) sync(p);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        WRITTEN.remove(e.getEntity().getUUID());
    }
}
