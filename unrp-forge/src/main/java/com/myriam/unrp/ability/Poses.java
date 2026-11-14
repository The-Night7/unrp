package com.myriam.unrp.ability;

import com.myriam.unrp.net.Net;
import com.myriam.unrp.net.PosePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

/** Côté serveur : fait jouer une pose (assets/unrp/poses.json) au joueur, vue par lui et par ceux qui le voient. */
public final class Poses {
    private Poses() {}

    /** Joue la pose une fois (sa durée est dans poses.json). */
    public static void play(ServerPlayer p, String clip) {
        if (clip == null || clip.isEmpty()) return;
        poseTag(p, clip, 12);
        if (model(clip)) com.myriam.unrp.anim.MoveAnim.play(p, clip); // clip du modèle (MoveClips)
        else send(p, clip, 0);
        Sounds.cue(p, clip);
    }

    /** Tient la pose pendant "ticks" (capacités maintenues, interrupteurs) ; renvoyer régulièrement pour prolonger. */
    public static void hold(ServerPlayer p, String clip, int ticks) {
        if (clip == null || clip.isEmpty()) return;
        poseTag(p, clip, Math.max(1, ticks));
        if (model(clip)) com.myriam.unrp.anim.MoveAnim.hold(p, clip, Math.max(1, ticks));
        else send(p, clip, Math.max(1, ticks));
        Sounds.cue(p, clip);
    }

    /** Pose d'une capacité : si elle a un son du modèle, il remplace la signature sonore de la pose. */
    public static void move(ServerPlayer p, com.myriam.unrp.data.AbilityPower.Move m, String clip, int hold) {
        if (m == null || m.sound == null || m.sound.isEmpty()) {
            if (hold > 0) hold(p, clip, hold);
            else play(p, clip);
            return;
        }
        if (clip != null && !clip.isEmpty()) {
            poseTag(p, clip, hold > 0 ? hold : 12);
            if (model(clip)) {
                if (hold > 0) com.myriam.unrp.anim.MoveAnim.hold(p, clip, hold);
                else com.myriam.unrp.anim.MoveAnim.play(p, clip);
            } else send(p, clip, hold);
        }
        Sounds.model(p, m.sound, m.soundVol);
    }

    /** Tient la pose sans rejouer de son (capacités maintenues, chaque tick). */
    public static void holdQuiet(ServerPlayer p, String clip, int ticks) {
        if (clip == null || clip.isEmpty()) return;
        poseTag(p, clip, Math.max(1, ticks));
        if (model(clip)) com.myriam.unrp.anim.MoveAnim.hold(p, clip, Math.max(1, ticks));
        else send(p, clip, Math.max(1, ticks));
    }

    /** Arrête la pose en cours (retour en douceur à la posture normale). */
    public static void stop(ServerPlayer p) {
        com.myriam.unrp.anim.MoveAnim.stop(p);
        send(p, "", 0);
    }

    private static final java.util.Map<java.util.UUID, java.util.Map<String, Long>> POSE_TAGS = new java.util.HashMap<>();

    /** CastFxEvents.pose du modèle : tag « unrp.pose.<geste> » posé « ticks » ticks (les poses tenues le lisent). */
    public static void poseTag(ServerPlayer p, String key, int ticks) {
        timedTag(p, "unrp.pose." + key, ticks);
    }

    /** Tag posé pour « ticks » ticks. */
    public static void timedTag(ServerPlayer p, String tag, int ticks) {
        p.addTag(tag);
        long until = p.serverLevel().getGameTime() + Math.max(1, ticks);
        POSE_TAGS.computeIfAbsent(p.getUUID(), k -> new java.util.HashMap<>()).merge(tag, until, Math::max);
    }

    /** Retire les tags de geste échus (chaque tick, par PassSync). */
    public static void tickTags(ServerPlayer p) {
        var m = POSE_TAGS.get(p.getUUID());
        if (m == null) return;
        long now = p.serverLevel().getGameTime();
        m.entrySet().removeIf(e -> {
            if (now < e.getValue()) return false;
            p.removeTag(e.getKey());
            return true;
        });
        if (m.isEmpty()) POSE_TAGS.remove(p.getUUID());
    }

    /** Le geste existe dans les clips du modèle (MoveClipIds) : c'est lui qui est joué. */
    public static boolean model(String clip) {
        return com.myriam.unrp.anim.MoveClipIds.id(clip) >= 0;
    }

    private static void send(ServerPlayer p, String clip, int hold) {
        Net.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> p), new PosePacket(p.getId(), clip, hold));
    }
}
