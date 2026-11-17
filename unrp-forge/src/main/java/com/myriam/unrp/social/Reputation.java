package com.myriam.unrp.social;

import com.myriam.unrp.ability.AbilityEngine;
import com.myriam.unrp.entity.Agent;
import com.myriam.unrp.entity.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Reputation {
    public static final String OBJECTIVE = "unrp.reputation";
    public static final String CONFIG = "unrp.config";
    private static final int CAP = 200;
    private static final int GUARDIAN_AT = 60;
    private static final int TYRANT_AT = -60;
    private static final int NOTORIOUS_AT = 120;
    private static final String GANG_TAG = "unrp.gang";
    private static final String HUNTER_TAG = "unrp.rep_hunter";
    static final String CUFFED = "unrp.cuffed";
    private static final String AUTHORITY_TAG = "unrp.authority_officer";
    private static final Map<UUID, Long> DEFENCE_GAIN = new HashMap<>();
    private static final Map<UUID, Long> RAMPAGE_CD = new HashMap<>();
    private static final Map<UUID, Long> HUNT_CD = new HashMap<>();
    private static final Map<UUID, Long> AUTH_CD = new HashMap<>();
    private static final Map<UUID, Long> AUTH_SUPPRESS = new HashMap<>();
    private static float civilFreq = 1.0F;

    // ------------------------------------------------------------------ score

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent e) {
        Scoreboard sb = e.getServer().getScoreboard();
        for (String name : new String[] {OBJECTIVE, CONFIG}) {
            if (sb.getObjective(name) == null)
                sb.addObjective(name, ObjectiveCriteria.DUMMY, Component.literal(name), ObjectiveCriteria.RenderType.INTEGER);
        }
    }

    public static int rep(ServerPlayer player) {
        Scoreboard sb = player.serverLevel().getScoreboard();
        Objective obj = sb.getObjective(OBJECTIVE);
        return obj == null ? 0 : sb.getOrCreatePlayerScore(player.getScoreboardName(), obj).getScore();
    }

    private static void setRep(ServerPlayer player, int value) {
        Scoreboard sb = player.serverLevel().getScoreboard();
        Objective obj = sb.getObjective(OBJECTIVE);
        if (obj != null) sb.getOrCreatePlayerScore(player.getScoreboardName(), obj).setScore(value);
    }

    public static void addRaw(ServerPlayer player, int delta) {
        Scoreboard sb = player.serverLevel().getScoreboard();
        Objective obj = sb.getObjective(OBJECTIVE);
        if (obj != null) sb.getOrCreatePlayerScore(player.getScoreboardName(), obj).add(delta);
    }

    public static void addRep(ServerPlayer player, int delta, String reason) {
        int before = rep(player);
        int after = Math.max(-CAP, Math.min(CAP, before + delta));
        if (after == before) return;
        setRep(player, after);
        String colour = delta > 0 ? "§b" : "§c";
        player.displayClientMessage(Component.literal(colour + (delta > 0 ? "+" : "") + delta + " §7" + reason), true);
        retag(player, before, after);
    }

    private static void retag(ServerPlayer player, int before, int after) {
        boolean guardian = after >= GUARDIAN_AT;
        boolean tyrant = after <= TYRANT_AT;
        if (guardian) player.addTag("unrp.guardian");
        else player.removeTag("unrp.guardian");
        if (tyrant) player.addTag("unrp.tyrant");
        else player.removeTag("unrp.tyrant");
        if (before < GUARDIAN_AT && after >= GUARDIAN_AT)
            player.displayClientMessage(Component.literal("§bLow-tiers are beginning to speak your name kindly."), false);
        if (before > TYRANT_AT && after <= TYRANT_AT)
            player.displayClientMessage(Component.literal("§cThey have started flinching when you pass."), false);
    }

    public static String titleOf(ServerPlayer player) {
        int r = rep(player);
        if (r >= NOTORIOUS_AT) return "§b[Gardien] §r";
        return r <= -NOTORIOUS_AT ? "§c[Tyran] §r" : "";
    }

    public static String standingOf(int r) {
        if (r >= 120) return "A guardian known to every low-tier who needed one";
        if (r >= 60) return "Regarded with quiet gratitude";
        if (r > 20) return "On parle de toi en bien, en passant";
        if (r > -20) return "Unremarkable. Nobody owes you anything, and you owe nothing";
        if (r > -60) return "People step aside when you pass";
        return r > -120 ? "Feared by those below you" : "A tyrant, and word is spreading";
    }

    private static boolean optIn(ServerPlayer a, ServerPlayer b) {
        return engaged(a) || engaged(b);
    }

    private static boolean engaged(ServerPlayer p) {
        SocialService.Duel d = SocialService.duelOf(p.getUUID());
        return d != null && ("formal".equals(d.kind) || "turf".equals(d.kind));
    }

    // ------------------------------------------------------------------ morts
    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        LivingEntity victim = event.getEntity();
        ServerLevel level = killer.serverLevel();
        long now = level.getGameTime();
        if (victim.getTags().contains(GANG_TAG)) {
            addRep(killer, 6, "you stood between them and the defenceless");
        } else if (victim instanceof Villager) {
            addRep(killer, -12, "they had no way to defend themselves");
        } else if (victim instanceof IronGolem) {
            addRep(killer, -4, "you brought down their protector");
        } else if (victim instanceof ServerPlayer fallen) {
            if (!optIn(killer, fallen)) return;
            int gap = powerLevel(killer) - powerLevel(fallen);
            if (gap >= 20) addRep(killer, -6, "you defeated someone who had no chance");
            else if (gap <= -20) addRep(killer, 5, "you stood against someone far stronger");
        } else if (victim instanceof Monster) {
            Long last = DEFENCE_GAIN.get(killer.getUUID());
            if (last != null && now - last < 100L) return;
            List<Villager> near = level.getEntitiesOfClass(Villager.class, victim.getBoundingBox().inflate(24.0));
            if (near.isEmpty()) return;
            DEFENCE_GAIN.put(killer.getUUID(), now);
            addRep(killer, 1, "you kept the village standing");
        }
    }

    private static int powerLevel(ServerPlayer p) {
        return (int) Math.round(com.myriam.unrp.data.ClaimData.get(p.server).profileOf(p.getUUID()).map(pr -> pr.level * 10).orElse(0.0));
    }

    // ------------------------------------------------------------------ PNJ
    private static Agent agent(ServerLevel level) {
        return ModEntities.AGENT.get().create(level);
    }

    private static void tryRampage(ServerPlayer player, ServerLevel level, long now) {
        Long cd = RAMPAGE_CD.get(player.getUUID());
        if (cd != null && now < cd) return;
        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(40.0));
        if (villagers.size() < 3) {
            RAMPAGE_CD.put(player.getUUID(), now + 400L);
            return;
        }
        if (player.getRandom().nextFloat() > Math.min(1.0F, 0.06F * civilFreq)) {
            RAMPAGE_CD.put(player.getUUID(), now + 600L);
            return;
        }
        RAMPAGE_CD.put(player.getUUID(), now + (long) (16000.0F / civilFreq));
        Villager focus = villagers.get(0);
        double ang = player.getRandom().nextDouble() * Math.PI * 2.0;
        int spawned = 0;
        for (int i = 0; i < 4; ++i) {
            int sx = (int) (focus.getX() + Math.cos(ang + i * 0.7) * 16.0);
            int sz = (int) (focus.getZ() + Math.sin(ang + i * 0.7) * 16.0);
            level.getChunk(sx >> 4, sz >> 4);
            int sy = level.getHeight(Heightmap.Types.WORLD_SURFACE, sx, sz);
            Agent thug = agent(level);
            if (thug == null) continue;
            thug.moveTo(sx + 0.5, sy, sz + 0.5, player.getRandom().nextFloat() * 360.0F, 0.0F);
            thug.addTag(GANG_TAG);
            thug.setCustomName(Component.literal("§cVoyou de gang"));
            level.addFreshEntity(thug);
            thug.setTarget(focus);
            ++spawned;
        }
        if (spawned > 0) player.displayClientMessage(Component.literal("§cA gang is marching on the village."), false);
    }

    private static void tryAuthority(ServerPlayer player, ServerLevel level, long now) {
        int r = rep(player);
        boolean tyrant = r <= -NOTORIOUS_AT;
        boolean vigilante = r >= NOTORIOUS_AT;
        if (!tyrant && !vigilante) return;
        Long cd = AUTH_CD.get(player.getUUID());
        if (cd != null && now < cd) return;
        if (player.getRandom().nextFloat() > Math.min(1.0F, 0.03F * civilFreq)) {
            AUTH_CD.put(player.getUUID(), now + 1200L);
            return;
        }
        AUTH_CD.put(player.getUUID(), now + (long) (48000.0F / civilFreq));
        int spawned = 0;
        for (int i = 0; i < 2; ++i) {
            double ang = player.getRandom().nextDouble() * Math.PI * 2.0;
            int sx = (int) (player.getX() + Math.cos(ang) * 24.0);
            int sz = (int) (player.getZ() + Math.sin(ang) * 24.0);
            level.getChunk(sx >> 4, sz >> 4);
            int sy = level.getHeight(Heightmap.Types.WORLD_SURFACE, sx, sz);
            Agent officer = agent(level);
            if (officer == null) continue;
            officer.setVariant(3);
            officer.moveTo(sx + 0.5, sy, sz + 0.5, 0.0F, 0.0F);
            officer.addTag(AUTHORITY_TAG);
            officer.setCustomName(Component.literal("§9Authority Officer"));
            level.addFreshEntity(officer);
            officer.setTarget(player);
            ++spawned;
        }
        if (spawned > 0) player.displayClientMessage(Component.literal(tyrant
                ? "§9⚖ The Authorities have a warrant for you." : "§9⚖ The Authorities are coming to suppress vigilantes."), false);
    }

    @SubscribeEvent
    public void onOfficerStrike(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        Entity src = event.getSource().getEntity();
        if (!(src instanceof Agent officer) || !officer.getTags().contains(AUTHORITY_TAG)) return;
        if (AUTH_SUPPRESS.containsKey(victim.getUUID())) return;
        if (!AbilityEngine.disabled(victim)) {
            AbilityEngine.disable(victim, 100);
            victim.addTag(CUFFED);
        }
        AUTH_SUPPRESS.put(victim.getUUID(), victim.serverLevel().getGameTime() + 100L);
        victim.displayClientMessage(Component.literal("§9⚖ Suppression cuffs! Power briefly disabled."), true);
    }

    private static void tryHunt(ServerPlayer player, ServerLevel level, long now) {
        if (rep(player) < NOTORIOUS_AT) return;
        Long cd = HUNT_CD.get(player.getUUID());
        if (cd != null && now < cd) return;
        if (player.getRandom().nextFloat() > Math.min(1.0F, 0.04F * civilFreq)) {
            HUNT_CD.put(player.getUUID(), now + 1200L);
            return;
        }
        HUNT_CD.put(player.getUUID(), now + (long) (36000.0F / civilFreq));
        int spawned = 0;
        for (int i = 0; i < 2; ++i) {
            double ang = player.getRandom().nextDouble() * Math.PI * 2.0;
            int sx = (int) (player.getX() + Math.cos(ang) * 22.0);
            int sz = (int) (player.getZ() + Math.sin(ang) * 22.0);
            level.getChunk(sx >> 4, sz >> 4);
            int sy = level.getHeight(Heightmap.Types.WORLD_SURFACE, sx, sz);
            Agent hunter = agent(level);
            if (hunter == null) continue;
            hunter.moveTo(sx + 0.5, sy, sz + 0.5, 0.0F, 0.0F);
            hunter.addTag(HUNTER_TAG);
            hunter.setCustomName(Component.literal("§8Chasseur"));
            level.addFreshEntity(hunter);
            hunter.setTarget(player);
            ++spawned;
        }
        if (spawned > 0) player.displayClientMessage(Component.literal("§8Someone paid to have you arrested."), false);
    }

    private static void tyrantPresence(ServerPlayer player, ServerLevel level) {
        if (rep(player) > TYRANT_AT) return;
        for (IronGolem golem : level.getEntitiesOfClass(IronGolem.class, player.getBoundingBox().inflate(16.0)))
            if (golem.getTarget() == null) golem.setTarget(player);
    }

    // ------------------------------------------------------------------ connexion
    @SubscribeEvent
    public void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        DEFENCE_GAIN.remove(p.getUUID());
        RAMPAGE_CD.remove(p.getUUID());
        HUNT_CD.remove(p.getUUID());
        AUTH_CD.remove(p.getUUID());
        AUTH_SUPPRESS.remove(p.getUUID());
        uncuff(p);
    }

    @SubscribeEvent
    public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && !AUTH_SUPPRESS.containsKey(p.getUUID())) uncuff(p);
    }

    static void uncuff(ServerPlayer p) {
        if (p.getTags().contains(CUFFED)) {
            p.removeTag(CUFFED);
            AbilityEngine.undisable(p);
        }
    }

    static boolean flag(MinecraftServer server, String key, boolean def) {
        Scoreboard sb = server.getScoreboard();
        Objective o = sb.getObjective(CONFIG);
        if (o == null || !sb.hasPlayerScore(key, o)) return def;
        return sb.getOrCreatePlayerScore(key, o).getScore() != 0;
    }

    static double factor(MinecraftServer server, String key, double def) {
        Scoreboard sb = server.getScoreboard();
        Objective o = sb.getObjective(CONFIG);
        if (o == null || !sb.hasPlayerScore(key, o)) return def;
        return sb.getOrCreatePlayerScore(key, o).getScore() / 1000.0;
    }

    // ------------------------------------------------------------------ chaque tick
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (server == null || server.getTickCount() % 100 != 0) return;
        boolean civil = flag(server, "npcs_enabled", true) && flag(server, "civil_enabled", true);
        civilFreq = (float) factor(server, "npc_freq_x1000", 1.0);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = player.serverLevel();
            long now = level.getGameTime();
            int standing = rep(player);
            if (standing <= TYRANT_AT) tyrantPresence(player, level);
            if (civil && standing >= NOTORIOUS_AT) tryHunt(player, level, now);
            if (civil) tryAuthority(player, level, now);
            Long supEnd = AUTH_SUPPRESS.get(player.getUUID());
            if (supEnd != null && now >= supEnd) {
                AUTH_SUPPRESS.remove(player.getUUID());
                uncuff(player);
            }
            if (civil) tryRampage(player, level, now);
        }
    }
}
