package com.myriam.unrp.ability;

import com.myriam.unrp.data.AbilityEffect;
import com.myriam.unrp.data.AbilityPower;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.data.Models;
import com.myriam.unrp.data.UnodexData;
import com.myriam.unrp.net.PlayerStatePacket;
import com.myriam.unrp.player.PlayerStateService;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import com.myriam.unrp.data.StyleData;
import com.myriam.unrp.style.StyleService;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.MinecraftServer;
import org.joml.Vector3f;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Moteur des pouvoirs. Il reproduit le fonctionnement du mod modèle (Palladium + scripts d'aura) :
 *
 *  - R active / désactive le pouvoir. Il reste actif sans recharge. Tant qu'il est actif : passifs, attributs,
 *    et une perte d'aura régulière (-1 aura toutes les "drain_every" ticks).
 *  - X C V B N : les capacités de la barre. 4 façons de les déclencher (champ "input") :
 *      action (une pression, recharge propre), charge (maintenir puis relâcher), held (tant que c'est maintenu),
 *      toggle (une pression allume, une autre éteint).
 *  - Aura : elle ne remonte QUE si rien ne la consomme. À 0, le pouvoir se coupe, l'aura est bloquée 10 secondes,
 *    puis revient à 25 % du maximum (minimum 10). Comme dans le modèle.
 *
 * Tout est calculé côté serveur. Référence des fichiers de données : docs/POUVOIRS.md.
 */
public final class AbilityEngine {
    public static final int SLOTS = 5;
    /** capacités par pouvoir au maximum (toutes postures confondues) */
    public static final int MAX_MOVES = AbilityInputProtocol.CAPACITY;
    /** blocage après épuisement de l'aura (200 ticks = 10 s, comme le modèle) */
    private static final int DEPLETION_LOCKOUT = 200;
    private static final double HELD_K = 0.25;

    static final class St {
        String powerId;
        boolean on;
        int mode;
        /** posture courante (pouvoirs à plusieurs postures : Hydrofreeze, Time Manipulation) */
        int stance;
        final long[] cdEnd = new long[MAX_MOVES];
        final boolean[] held = new boolean[MAX_MOVES];
        final boolean[] charging = new boolean[MAX_MOVES];
        final long[] chargeAt = new long[MAX_MOVES];
        final boolean[] toggled = new boolean[MAX_MOVES];
        /** fraction d'aura en attente de retrait (pertes régulières) */
        double drainAcc;
        /** vrai si quelque chose consomme l'aura en ce moment : la régénération est alors bloquée */
        boolean draining;
        /** fin du blocage après épuisement (0 = pas bloqué) */
        long lockUntil;
        /** Bélier en cours */
        long ramUntil;
        Ctx ramCtx;
        final Set<UUID> ramHit = new HashSet<>();
        /** cape allumée ; attaquer la trouble jusqu'à distortUntil */
        boolean cloaked;
        long distortUntil;
        /** Aura Manipulation : pouvoir copié et fin de la copie */
        String copyId;
        long copyUntil;
        /** Aura Manipulation : pouvoirs copiés gardés en réserve ; copyId = celui qu'on a en main (null = le sien) */
        final java.util.LinkedHashSet<String> reserve = new java.util.LinkedHashSet<>();
        /** pouvoir suspendu jusqu'à (Genetic Stasis, surcharge de mutations) */
        long disabledUntil;
    }

    /** cape partagée : allié -> fin (tick) */
    private static final Map<UUID, Long> SHARED = new HashMap<>();
    /** riposte : (victime, attaquant) -> prochain tick possible */
    private static final Map<String, Long> RETAL = new HashMap<>();

    /** Pouvoir « courant » de chaque joueur (celui que lisent setup() et le code des pouvoirs). */
    private static final Map<UUID, St> STATE = new HashMap<>();
    /**
     * États des autres pouvoirs détenus (copies d'Aura Manipulation, pouvoirs accordés) : comme les pouvoirs Palladium
     * du modèle, chacun a son interrupteur et ses capacités, et reste allumé en arrière-plan. Clé : id ("" = le sien).
     */
    private static final Map<UUID, Map<String, St>> OTHERS = new HashMap<>();

    static String key(St s) {
        return s.copyId == null ? "" : s.copyId;
    }

    /** Pouvoirs détenus, dans l'ordre de la barre : "" (le sien, s'il n'est pas scellé), copies, pouvoirs accordés. */
    public static List<String> heldKeys(ServerPlayer p) {
        List<String> out = new ArrayList<>();
        String base = basePowerId(p);
        if (base != null && !Superpowers.revoked(p).contains(base)) out.add("");
        St st = STATE.get(p.getUUID());
        if (st != null) for (String r : st.reserve) if (!out.contains(r)) out.add(r);
        for (String g : Superpowers.granted(p)) if (!out.contains(g)) out.add(g);
        return out;
    }

    private static St stateOf(ServerPlayer p, String k) {
        UUID id = p.getUUID();
        St focus = STATE.computeIfAbsent(id, x -> new St());
        if (key(focus).equals(k)) return focus;
        return OTHERS.computeIfAbsent(id, x -> new HashMap<>()).computeIfAbsent(k, kk -> {
            St n = new St();
            n.copyId = kk.isEmpty() ? null : kk;
            n.copyUntil = Long.MAX_VALUE;
            return n;
        });
    }

    /** Exécute r avec le pouvoir k comme pouvoir courant, puis remet le précédent (réserve, suspension, blocage partagés). */
    static void withPower(ServerPlayer p, String k, Runnable r) {
        UUID id = p.getUUID();
        St focus = STATE.computeIfAbsent(id, x -> new St());
        if (key(focus).equals(k)) {
            r.run();
            return;
        }
        Map<String, St> o = OTHERS.computeIfAbsent(id, x -> new HashMap<>());
        St s = o.remove(k);
        if (s == null) {
            s = new St();
            s.copyId = k.isEmpty() ? null : k;
            s.copyUntil = Long.MAX_VALUE;
        }
        s.reserve.clear();
        s.reserve.addAll(focus.reserve);
        s.disabledUntil = focus.disabledUntil;
        s.lockUntil = focus.lockUntil;
        String fk = key(focus);
        o.put(fk, focus); // le courant est rangé avec les autres pendant l'appel (appels imbriqués cohérents)
        STATE.put(id, s);
        try {
            r.run();
        } finally {
            focus.reserve.clear();
            focus.reserve.addAll(s.reserve);
            focus.disabledUntil = Math.max(focus.disabledUntil, s.disabledUntil);
            focus.lockUntil = Math.max(focus.lockUntil, s.lockUntil);
            if (o.get(fk) == focus) o.remove(fk);
            STATE.put(id, focus);
            if (s.copyId == null || focus.reserve.contains(s.copyId) || Superpowers.granted(p).contains(s.copyId)) o.put(k, s);
        }
    }

    /** Tous les états d'un joueur (le courant d'abord). */
    private static List<St> allStates(UUID id) {
        List<St> out = new ArrayList<>();
        St f = STATE.get(id);
        if (f != null) out.add(f);
        Map<String, St> o = OTHERS.get(id);
        if (o != null) out.addAll(o.values());
        return out;
    }

    /** Coupe tous les pouvoirs du joueur. */
    public static void allOff(ServerPlayer p) {
        St f = STATE.get(p.getUUID());
        if (f != null && f.on) turnOff(p, setup(p, false), f, null);
        Map<String, St> o = OTHERS.get(p.getUUID());
        if (o != null) for (String k : new ArrayList<>(o.keySet())) {
            St s = o.get(k);
            if (s != null && s.on) withPower(p, k, () -> turnOff(p, setup(p, false), STATE.get(p.getUUID()), null));
        }
    }

    /** Ce pouvoir est détenu (le sien, une copie, un pouvoir accordé). */
    public static boolean holds(ServerPlayer p, String power) {
        if (power == null) return false;
        if (power.equals(basePowerId(p)) && !Superpowers.revoked(p).contains(power)) return true;
        return heldKeys(p).contains(power);
    }

    private static St stateFor(ServerPlayer p, String power) {
        String k = power.equals(basePowerId(p)) && !Superpowers.revoked(p).contains(power) ? "" : power;
        St f = STATE.get(p.getUUID());
        if (f != null && key(f).equals(k)) return f;
        Map<String, St> o = OTHERS.get(p.getUUID());
        return o == null ? null : o.get(k);
    }

    /** Pouvoirs allumés du joueur (ids). */
    public static List<String> activePowers(ServerPlayer p) {
        List<String> out = new ArrayList<>();
        String base = basePowerId(p);
        for (St s : allStates(p.getUUID())) {
            if (!s.on) continue;
            String id = s.copyId != null ? s.copyId : base;
            if (id != null && !out.contains(id)) out.add(id);
        }
        return out;
    }

    /** Ce pouvoir est détenu et allumé. */
    public static boolean isOn(ServerPlayer p, String power) {
        if (!holds(p, power)) return false;
        St s = stateFor(p, power);
        return s != null && s.on;
    }

    /** Interrupteur i de ce pouvoir allumé. */
    public static boolean toggled(ServerPlayer p, String power, int i) {
        St s = stateFor(p, power);
        return s != null && s.on && i >= 0 && i < MAX_MOVES && s.toggled[i];
    }
    private static final List<Shell> SHELLS = new ArrayList<>();
    private static final List<Bolt> BOLTS = new ArrayList<>();
    private static int shellIds;

    private AbilityEngine() {}

    public static boolean isOn(UUID id) {
        for (St s : allStates(id)) if (s.on) return true;
        return false;
    }

    /** Retire de l'aura au joueur (coût d'une charge, d'une pulsation...). Compte pour la maîtrise ; épuise le pouvoir à 0. */
    public static void spendAura(ServerPlayer p, int amount) {
        Setup su = setup(p, false);
        if (su == null || amount <= 0) return;
        St st = STATE.computeIfAbsent(p.getUUID(), k -> new St());
        spend(p, su, st, amount);
    }

    /** Aura actuelle du joueur (0 si pas de pouvoir). */
    public static int aura(ServerPlayer p) {
        Setup su = setup(p, false);
        return su == null ? 0 : Math.max(0, su.pr.aura);
    }

    /** Rend de l'aura au joueur (remboursement). */
    public static void refundAura(ServerPlayer p, int amount) {
        Setup su = setup(p, false);
        if (su == null || amount <= 0) return;
        su.pr.aura = Math.min(PlayerStateService.maxAura(su.st), su.pr.aura + amount);
        su.claims.setDirty();
    }

    /** Interrupteur i allumé (pour les effets visuels, ability/Specials2.looks). */
    public static boolean toggled(UUID id, int i) {
        St s = STATE.get(id);
        return s != null && s.on && i >= 0 && i < MAX_MOVES && s.toggled[i];
    }

    /** La régénération d'aura (PlayerStateService) ne s'applique pas si l'aura est consommée ou bloquée. */
    public static boolean blocksRegen(UUID id) {
        for (St s : allStates(id)) if (s.draining || s.lockUntil > 0) return true;
        return false;
    }

    public static void onLogout(UUID id) {
        STATE.remove(id);
        OTHERS.remove(id);
        Specials.onLogout(id);
        SHELLS.removeIf(sh -> sh.owner.equals(id));
        BOLTS.removeIf(b -> b.c.p().getUUID().equals(id));
    }

    public static void restoreWalls() {
        Walls.restoreAll();
        SHELLS.clear();
        BOLTS.clear();
    }

    private record Setup(ClaimData claims, ClaimData.Profile pr, String powerId, AbilityPower power, int[] st) {}

    private static Setup setup(ServerPlayer p, boolean speak) {
        ClaimData claims = ClaimData.get(p.server);
        ClaimData.Profile pr = claims.profileOf(p.getUUID()).orElse(null);
        if (pr == null) {
            if (speak) bar(p, "Tu n'as pas encore de personnage (touche H).");
            return null;
        }
        UnodexData data = UnodexData.get();
        Models.Character c = ClaimData.Profile.CHARACTER.equals(pr.kind) ? data.characters.get(pr.characterId) : null;
        claims.ensureOcAbility(pr); // OC créé avant le tirage de capacité
        String aid = pr.abilityOf(c);
        if (pr.lifelong || aid == null) {
            if (speak) bar(p, "Ton personnage n'a pas de capacité utilisable.");
            return null;
        }
        AbilityPower power = data.powerFor(aid);
        if (Superpowers.revoked(p).contains(data.aliases.getOrDefault(aid, aid))) power = null; // pouvoir scellé par un pacte
        St cst = STATE.get(p.getUUID());
        String copied = null;
        if (cst != null && cst.copyId != null) { // pouvoir copié (Aura Manipulation)
            if (p.serverLevel().getGameTime() < cst.copyUntil && data.powers.containsKey(cst.copyId)) {
                copied = cst.copyId;
                power = data.powers.get(copied);
            } else {
                cst.copyId = null;
            }
        }
        if (power == null) {
            if (speak) bar(p, Superpowers.revoked(p).contains(data.aliases.getOrDefault(aid, aid))
                    ? "Ton pouvoir est scellé par un pacte." : "Cette capacité n'a pas encore de pouvoir défini (" + aid + ").");
            return null;
        }
        String id = copied != null ? copied : data.aliases.getOrDefault(aid, aid);
        power = Specials17.borrowedView(p, id, power);
        int[] st = Specials17.borrowedStats(p, id, PlayerStateService.stats(p.server, p.getUUID(), pr, c));
        if (pr.aura < 0) pr.aura = PlayerStateService.maxAura(st);
        return new Setup(claims, pr, id, power, st);
    }

    /** Remplit l'état envoyé au client : actif, forme courante, capacités, recharges. */
    public static void describe(ServerPlayer p, PlayerStatePacket s, String abilityId) {
        s.active = isOn(p.getUUID());
        AbilityPower power = UnodexData.get().powerFor(abilityId);
        St st = STATE.get(p.getUUID());
        long now = p.serverLevel().getGameTime();
        if (st != null && st.copyId != null && now < st.copyUntil && UnodexData.get().powers.containsKey(st.copyId)) {
            power = UnodexData.get().powers.get(st.copyId); // pouvoir copié
            s.modeName = "Copie : " + st.copyId;
        }
        s.unlockMask = Unlocks.mask(p.server, p);
        s.abilityPts = Unlocks.available(p.server, p);
        s.abilityPtsTotal = Unlocks.total(p.server, p);
        boolean copiedNow = st != null && st.copyId != null && now < st.copyUntil;
        describeBar(p, s); // barre de Palladium : tous les pouvoirs détenus
        if (power == null) return;
        if (!power.modes.isEmpty()) s.modeName = power.modes.get(st == null ? 0 : st.mode % power.modes.size()).name;
        if (!power.stances.isEmpty()) s.modeName = power.stances.get(st == null ? 0 : st.stance % power.stances.size());
        power = Specials17.borrowedView(p, st != null && st.copyId != null ? st.copyId : basePowerId(p), power);
        List<Integer> vis = visible(power, st, p);
        int n = vis.size();
        s.moveNames = new String[n];
        s.moveDesc = new String[n];
        s.moveHeld = new boolean[n];
        s.moveToggle = new boolean[n];
        s.moveOn = new boolean[n];
        s.moveCharge = new int[n];
        s.moveCost = new int[n];
        s.moveCd = new int[n];
        s.moveLeft = new int[n];
        s.moveLocked = new boolean[n];
        s.moveRank = new int[n];
        for (int k = 0; k < n; k++) {
            int i = vis.get(k);
            AbilityPower.Move m = power.moves.get(i);
            s.moveRank[k] = m.unlock;
            s.moveLocked[k] = Specials17.moveLevel(p, st != null && st.copyId != null ? st.copyId : basePowerId(p), i) < m.minLevel || (!copiedNow && Unlocks.locked(s.unlockMask, power, i, Unlocks.mastered(p.server, p)))
                    || !Specials17.borrowedMoveAllowed(p, st != null && st.copyId != null ? st.copyId : AbilityEngine.basePowerId(p), i);
            s.moveNames[k] = m.name == null ? "?" : (m.name.length() > 40 ? m.name.substring(0, 40) : m.name);
            s.moveDesc[k] = m.desc == null ? "" : (m.desc.length() > 200 ? m.desc.substring(0, 200) : m.desc);
            s.moveHeld[k] = "held".equals(m.input);
            s.moveToggle[k] = "toggle".equals(m.input);
            s.moveOn[k] = st != null && (st.toggled[i] || st.held[i]);
            s.moveCharge[k] = "charge".equals(m.input) ? Math.max(1, m.charge) : 0;
            s.moveCost[k] = m.cost;
            s.moveCd[k] = m.cooldown;
            s.moveLeft[k] = st == null ? 0 : (int) Math.max(0, st.cdEnd[i] - now);
        }
    }

    /**
     * Barre de Palladium : les capacités de chaque pouvoir détenu (le sien, les copies, les pouvoirs accordés), chacun
     * avec son interrupteur, ses cases, ses icônes ; verrouillées si hors posture/mode ou pas débloquées.
     */
    private static void describeBar(ServerPlayer p, PlayerStatePacket s) {
        List<String> keys = heldKeys(p);
        List<Integer> bp = new ArrayList<>(), bslot = new ArrayList<>(), bcd = new ArrayList<>(), bleft = new ArrayList<>();
        List<String> bicon = new ArrayList<>(), bcol = new ArrayList<>(), bname = new ArrayList<>();
        List<Boolean> block = new ArrayList<>(), bon = new ArrayList<>();
        List<Integer> bmove = new ArrayList<>();
        int n = keys.size();
        s.powIcon = new String[n];
        s.powColor = new String[n];
        s.powName = new String[n];
        s.powOn = new boolean[n];
        long now = p.serverLevel().getGameTime();
        for (int pi = 0; pi < n; pi++) {
            final int idx = pi;
            withPower(p, keys.get(pi), () -> {
                Setup su = setup(p, false);
                St st = STATE.get(p.getUUID());
                if (su == null) {
                    s.powIcon[idx] = "";
                    s.powColor[idx] = "";
                    s.powName[idx] = "";
                    return;
                }
                AbilityPower power = su.power;
                s.powIcon[idx] = power.icon == null ? "" : power.icon;
                s.powColor[idx] = power.barColor == null ? "" : power.barColor;
                s.powName[idx] = su.powerId;
                s.powOn[idx] = st != null && st.on;
                boolean borrowed = st != null && st.copyId != null;
                int mask = Unlocks.mask(p.server, p);
                List<Integer> vis = visible(power, st, p);
                for (int i = 0; i < Math.min(power.moves.size(), MAX_MOVES); i++) {
                    AbilityPower.Move m = power.moves.get(i);
                    bp.add(idx);
                    bmove.add(i);
                    bslot.add(power.branchSource != null ? i + 1 : m.barSlot >= 0 ? m.barSlot : i + 1);
                    bcd.add(Math.max(0, m.cooldown));
                    bleft.add(st == null ? 0 : (int) Math.max(0, st.cdEnd[i] - now));
                    bicon.add(m.icon == null ? "" : m.icon);
                    bcol.add(m.barColor == null ? "" : m.barColor);
                    bname.add(m.name == null ? "?" : (m.name.length() > 40 ? m.name.substring(0, 40) : m.name));
                    block.add(!vis.contains(i) || Specials17.moveLevel(p, su.powerId, i) < m.minLevel || !Specials17.borrowedMoveAllowed(p, su.powerId, i)
                            || Specials17.branchBlocked(p, su.powerId, i) || (!borrowed && Unlocks.locked(mask, power, i, Unlocks.mastered(p.server, p))));
                    bon.add(st != null && (st.toggled[i] || st.held[i] || st.charging[i]));
                }
            });
        }
        int m = bp.size();
        s.barPower = new int[m]; s.barMove = new int[m];
        s.barSlot = new int[m]; s.barCd = new int[m]; s.barLeft = new int[m];
        s.barIcon = new String[m]; s.barColor = new String[m]; s.barName = new String[m];
        s.barLocked = new boolean[m]; s.barOn = new boolean[m];
        for (int k = 0; k < m; k++) {
            s.barPower[k] = bp.get(k); s.barMove[k] = bmove.get(k);
            s.barSlot[k] = bslot.get(k); s.barCd[k] = bcd.get(k); s.barLeft[k] = bleft.get(k);
            s.barIcon[k] = bicon.get(k); s.barColor[k] = bcol.get(k); s.barName[k] = bname.get(k);
            s.barLocked[k] = block.get(k); s.barOn[k] = bon.get(k);
        }
    }

    private static double levelOf(ServerPlayer player) { return Specials17.levelOf(player); }

    // ------------------------------------------------------------------ entrées (touches)
    /** action : 0 = activer/désactiver ; 10+i = appui sur la capacité i ; 20+i = relâchement. */
    public static void input(ServerPlayer p, int action) {
        if (action >= 1000) { // barre de Palladium : chaque pouvoir détenu a ses listes ; l'action vise ce pouvoir
            List<String> keys = heldKeys(p);
            int idx = action / 1000 - 1;
            if (idx < 0 || idx >= keys.size()) return;
            int code = action % 1000;
            withPower(p, keys.get(idx), () -> input(p, code));
            return;
        }
        if (action == 140) { // touche de liste de Palladium, après la dernière page : pouvoir suivant (copies, pouvoirs accordés)
            St st0 = STATE.computeIfAbsent(p.getUUID(), k -> new St());
            if (!st0.reserve.isEmpty() || !Superpowers.granted(p).isEmpty()) cycleCopy(p, st0);
            return;
        }
        if (AbilityInputProtocol.isMove(action)) {
            boolean press = AbilityInputProtocol.isPress(action);
            int index = AbilityInputProtocol.index(action);
            Setup setup = setup(p, press);
            if (setup == null) return;
            St state = STATE.computeIfAbsent(p.getUUID(), k -> new St());
            int key = visible(setup.power, state, p).indexOf(index);
            if (key >= 0) input(p, press ? 10 : 20, key);
            return;
        }
        if (action >= 100 && action < 140) { // case de la barre : capacité désignée par son rang dans le pouvoir
            boolean press = action < 120;
            int idx = action % 20;
            Setup su0 = setup(p, press);
            if (su0 == null) return;
            St st0 = STATE.computeIfAbsent(p.getUUID(), k -> new St());
            List<Integer> vis0 = visible(su0.power, st0, p);
            int key = vis0.indexOf(idx);
            if (key < 0) return; // capacité verrouillée par la posture ou le mode
            input(p, press ? 10 : 20, key);
            return;
        }
        input(p, action, -1);
    }

    /** keyOverride >= 0 : rang de la capacité parmi celles de la posture (au-delà de 9 touches). */
    private static void input(ServerPlayer p, int action, int keyOverride) {
        Setup su = setup(p, action < 20);
        if (su == null) return;
        St st = STATE.computeIfAbsent(p.getUUID(), k -> new St());
        st.powerId = su.powerId;
        long now = p.serverLevel().getGameTime();
        if (Specials17.powerBlocked(p)) { bar(p, "Ton pouvoir est détenu en Gage ou suspendu par arbitrage."); return; }
        if (action == 0 && p.isShiftKeyDown() && pagedBar(su.power)) { // pouvoirs à pages (Hollow Mark) : Maj + R change de page
            st.stance = (st.stance + 1) % su.power.stances.size();
            bar(p, su.power.stances.get(st.stance));
            PlayerStateService.send(p);
            return;
        }
        if (action == 0) {
            if (st.on) {
                turnOff(p, su, st, "Pouvoir désactivé");
            } else if (st.disabledUntil > now) {
                bar(p, "Pouvoir suspendu : encore " + (int) Math.ceil((st.disabledUntil - now) / 20.0) + " s");
            } else if (st.lockUntil > now) {
                bar(p, "Aura épuisée : encore " + (int) Math.ceil((st.lockUntil - now) / 20.0) + " s");
            } else if (su.pr.aura <= 0) {
                bar(p, "Plus d'aura : attends qu'elle remonte.");
            } else {
                st.on = true;
                bar(p, "Pouvoir activé");
                onActivate(p, su);
                syncAttributes(p, su, st);
                PlayerStateService.send(p);
            }
            return;
        }
        boolean press = action >= 10 && action < 20;
        List<Integer> vis = visible(su.power, st, p);
        int key = keyOverride >= 0 ? keyOverride : action % 10;
        if (key >= vis.size()) return;
        int slot = vis.get(key); // touche -> capacité de la posture courante
        AbilityPower.Move m = su.power.moves.get(slot);
        if (!press) {
            if (st.held[slot]) {
                st.held[slot] = false;
                if (m.releaseTag != null) p.addTag(m.releaseTag);
                if (m.cooldown > 0) st.cdEnd[slot] = p.serverLevel().getGameTime() + m.cooldown; // recharge Palladium d'une capacité maintenue
                Poses.stop(p);
                Specials.stop(p, m.special);
                syncAttributes(p, su, st);
                PlayerStateService.send(p);
            }
            if (st.charging[slot]) {
                st.charging[slot] = false;
                releaseCharge(p, su, st, slot, m);
            }
            return;
        }
        if (Specials17.moveLevel(p, su.powerId, slot) < m.minLevel) {
            bar(p, "Niveau " + m.minLevel + " requis."); return;
        }
        if (!st.on) {
            bar(p, "Active d'abord ton pouvoir (R).");
            return;
        }
        boolean copied = st.copyId != null && now < st.copyUntil;
        if (!copied && Unlocks.locked(Unlocks.mask(p.server, p), su.power, slot, Unlocks.mastered(p.server, p))) {
            int rk = su.power.moves.get(slot).unlock;
            bar(p, m.name + " : verrouillé" + (rk >= Unlocks.MIN_RANK ? " (rang " + rk + ", " + Unlocks.cost(rk) + " pt) : touche H, onglet Capacités." : " : débloque d'abord la capacité précédente."));
            return;
        }
        if ((Specials17.branchBlocked(p, su.powerId, slot) || !Specials17.borrowedMoveAllowed(p, su.powerId, slot))) {
            bar(p, "§cCette capacité est prise en gage (Covenant).");
            return;
        }
        if (!st.toggled[slot] && !tagsAllow(p, m)) return; // capacité Palladium désactivée par ses conditions de tag
        int need = m.minAura > 0 ? m.minAura : Math.max(1, m.cost);
        switch (m.input == null ? "action" : m.input) {
            case "held" -> {
                if (now < st.cdEnd[slot]) {
                    bar(p, String.format(Locale.ROOT, "%s : %.1f s", m.name, (st.cdEnd[slot] - now) / 20.0));
                    return;
                }
                if (su.pr.aura < Math.max(Math.max(1, m.minAura), m.pressCost)) {
                    bar(p, "Aura insuffisante.");
                    return;
                }
                if (m.pressCost > 0) spend(p, su, st, m.pressCost);
                st.held[slot] = true; // la première pulsation part au prochain tick serveur
                if (m.triggerTag != null) p.addTag(m.triggerTag); // « first_tick_commands » d'une capacité maintenue du modèle
                if (m.holdTag != null) p.addTag(m.holdTag);
                Poses.move(p, m, poseOf(p, m), 10);
                syncAttributes(p, su, st);
                PlayerStateService.send(p);
            }
            case "toggle" -> {
                if (!st.toggled[slot] && su.pr.aura < Math.max(1, m.minAura)) {
                    bar(p, "Aura insuffisante.");
                    return;
                }
                if (!st.toggled[slot] && now < st.cdEnd[slot]) {
                    bar(p, String.format(Locale.ROOT, "%s : %.1f s", m.name, (st.cdEnd[slot] - now) / 20.0));
                    return;
                }
                st.toggled[slot] = !st.toggled[slot];
                if (!st.toggled[slot]) {
                    st.cdEnd[slot] = now + Math.max(0, m.cooldown);
                    Specials.stop(p, m.special);
                } else if (m.group != null) { // un seul interrupteur allumé par groupe
                    for (int j = 0; j < su.power.moves.size() && j < MAX_MOVES; j++) {
                        if (j != slot && st.toggled[j] && m.group.equals(su.power.moves.get(j).group)) {
                            st.toggled[j] = false;
                            Specials.stop(p, su.power.moves.get(j).special);
                        }
                    }
                }
                bar(p, m.name + (st.toggled[slot] ? " : activé" : " : désactivé"));
                if (st.toggled[slot] && m.fx != null) perform(p, su, st, m, 1.0, true);
                if (st.toggled[slot]) Poses.move(p, m, poseOf(p, m), 0);
                syncAttributes(p, su, st);
                PlayerStateService.send(p);
            }
            case "charge" -> {
                if (now < st.cdEnd[slot]) {
                    bar(p, String.format(Locale.ROOT, "%s : %.1f s", m.name, (st.cdEnd[slot] - now) / 20.0));
                    return;
                }
                if (su.pr.aura < (m.minAura > 0 ? m.minAura : Math.max(1, m.cost / 3))) {
                    bar(p, "Aura insuffisante pour charger.");
                    return;
                }
                st.charging[slot] = true;
                st.chargeAt[slot] = now; // l'effet part au relâchement
            }
            default -> { // action
                if (now < st.cdEnd[slot]) {
                    bar(p, String.format(Locale.ROOT, "%s : %.1f s", m.name, (st.cdEnd[slot] - now) / 20.0));
                    return;
                }
                if (su.pr.aura < need) {
                    bar(p, "Aura insuffisante (" + su.pr.aura + "/" + need + ").");
                    return;
                }
                String pose = poseOf(p, m); // choisie avant l'effet (l'effet peut dépendre de l'accroupissement)
                if (!perform(p, su, st, m, 1.0, true)) return;
                Poses.move(p, m, pose, 0);
                spend(p, su, st, m.cost);
                st.cdEnd[slot] = now + Math.max(0, m.cooldown);
                PlayerStateService.send(p);
            }
        }
    }

    /** Effet d'activation : la foudre tombe sur le joueur s'il voit le ciel (Lightning), pose d'activation. */
    private static void onActivate(ServerPlayer p, Setup su) {
        if (su.power.activeTag != null) p.addTag(su.power.activeTag);
        for (String t : su.power.extraActiveTags) p.addTag(t);
        if (su.power.activateSound != null) {
            Poses.holdQuiet(p, su.power.activatePose, 1);
            Sounds.model(p, su.power.activateSound, su.power.activateVol, su.power.activatePitch);
        } else Poses.play(p, su.power.activatePose);
        if ("am_on".equals(su.power.onActivate)) {
            com.myriam.unrp.pass.AmPassLite.on(p); // AM_ON
        } else if ("tm_on".equals(su.power.onActivate)) {
            com.myriam.unrp.pass.TimePass.on(p, p.serverLevel()); // TimeManipulationEvents : TM_ON + pose tm_wind
        } else if ("sky_strike".equals(su.power.onActivate) && su.power.passOn != null) {
            com.myriam.unrp.pass.LightningPass.call(p); // LightningEvents.call : la foudre du moteur d'effets (F_ROOF sous un toit)
        } else if ("sky_strike".equals(su.power.onActivate) && p.serverLevel().canSeeSky(BlockPos.containing(p.getEyePosition()))) {
            int col = su.power.look != null && su.power.look.rgb() >= 0 ? su.power.look.rgb() : 0xFFFFFF;
            Vec3 top = p.position().add((p.getRandom().nextDouble() - 0.5) * 2, 28, (p.getRandom().nextDouble() - 0.5) * 2);
            Fx.line(p.serverLevel(), top, p.position().add(0, 1, 0), col, Styles.LINE_JAGGED, 0.35f, 8);
            Fx.wave(p.serverLevel(), p.position(), 3, col, 10);
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6f, 1.4f);
        }
    }

    /** Relâchement d'une capacité chargée : puissance et coût proportionnels au temps de charge (minimum 30 %). */
    private static void releaseCharge(ServerPlayer p, Setup su, St st, int slot, AbilityPower.Move m) {
        if (!st.on) return;
        long now = p.serverLevel().getGameTime();
        double k = Math.max(0.3, Math.min(1.0, (now - st.chargeAt[slot]) / (double) Math.max(1, m.charge)));
        int cost = (int) Math.max(1, Math.round(m.cost * k));
        if (su.pr.aura < cost) {
            bar(p, "Aura insuffisante (" + su.pr.aura + "/" + cost + ").");
            return;
        }
        String pose = poseOf(p, m);
        if (!perform(p, su, st, m, k, true)) return;
        Poses.move(p, m, pose, 0);
        spend(p, su, st, cost);
        st.cdEnd[slot] = now + Math.max(0, m.cooldown);
        PlayerStateService.send(p);
    }

    private static void spend(ServerPlayer p, Setup su, St st, int amount) {
        if (amount <= 0) return;
        su.pr.aura = Math.max(0, su.pr.aura - amount);
        su.claims.setDirty();
        Mastery.add(p, su.claims, su.pr, amount);
        if (su.pr.aura == 0) deplete(p, su, st);
    }

    /** Aura à 0 : le pouvoir se coupe et l'aura reste bloquée DEPLETION_LOCKOUT ticks. */
    private static void deplete(ServerPlayer p, Setup su, St st) {
        turnOff(p, su, st, null);
        st.lockUntil = p.serverLevel().getGameTime() + DEPLETION_LOCKOUT;
        DEPLETED.add(p.getUUID()); // les autres pouvoirs se coupent au tick (hors de tout changement de pouvoir courant)
        p.displayClientMessage(Component.literal("Aura épuisée !").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD), true);
    }

    private static final Set<UUID> DEPLETED = new HashSet<>();

    /** Aura à 0 : tous les pouvoirs se coupent, les copies sont expulsées, l'aura reste bloquée. */
    private static void depleteAll(ServerPlayer p) {
        St st = STATE.get(p.getUUID());
        boolean hadCopies = st != null && !st.reserve.isEmpty();
        allOff(p);
        if (hadCopies) {
            setCopy(p, null, 0);
            p.displayClientMessage(Component.literal("Aura épuisée : pouvoirs expulsés !").withStyle(net.minecraft.ChatFormatting.RED), true);
        }
        St f = STATE.computeIfAbsent(p.getUUID(), k -> new St());
        f.lockUntil = Math.max(f.lockUntil, p.serverLevel().getGameTime() + DEPLETION_LOCKOUT);
    }

    private static void turnOff(ServerPlayer p, Setup su, St st, String msg) {
        st.on = false;
        if (su != null && su.power.activeTag != null) p.removeTag(su.power.activeTag);
        if (su != null) {
            for (String t : su.power.extraActiveTags) p.removeTag(t);
            if (su.power.offTag != null) p.addTag(su.power.offTag);
            for (int i = 0; i < su.power.moves.size() && i < MAX_MOVES; i++)
                if (st.held[i] && su.power.moves.get(i).releaseTag != null) p.addTag(su.power.moves.get(i).releaseTag);
        }
        if (su != null) for (AbilityPower.Move mv : su.power.moves) Specials.stop(p, mv.special);
        java.util.Arrays.fill(st.held, false);
        java.util.Arrays.fill(st.charging, false);
        java.util.Arrays.fill(st.toggled, false);
        st.ramUntil = 0;
        st.draining = false;
        Iterator<Shell> it = SHELLS.iterator();
        while (it.hasNext()) {
            Shell sh = it.next();
            if (sh.owner.equals(p.getUUID())) {
                sh.dissipate();
                it.remove();
            }
        }
        if (su != null) syncAttributes(p, su, st);
        if (msg != null) bar(p, msg);
        PlayerStateService.send(p);
    }

    private static boolean perform(ServerPlayer p, Setup su, St st, AbilityPower.Move m, double k, boolean speak) {
        int[] originalStrength = Specials17.borrowedMoveStats(p, su.powerId, su.power.moves.indexOf(m), su.st);
        su = new Setup(su.claims, su.pr, su.powerId, su.power, originalStrength);
        if (!"stance_next".equals(m.special) && !"mode_next".equals(m.special)) Specials17.onCast(p);
        if (!st.on || Specials17.powerBlocked(p) || (su.power.branchSource != null && !Specials17.hasLease(p, su.powerId))) return false;
        if ("stance_next".equals(m.special)) {
            if (su.power.stances.isEmpty()) return false;
            st.stance = (st.stance + 1) % su.power.stances.size();
            dropHidden(p, su.power, st);
            bar(p, "Posture : " + su.power.stances.get(st.stance));
            return true;
        }
        if (m.special != null && Specials.handles(m.special)) return Specials.run(p, su.power, m, su.st, su.power.branchSource != null && m.stance >= 0 ? m.stance : st.stance);
        if ("mode_next".equals(m.special)) {
            if (su.power.modes.isEmpty()) return false;
            st.mode = (st.mode + 1) % su.power.modes.size();
            dropHidden(p, su.power, st);
            AbilityPower.Mode md = su.power.modes.get(st.mode);
            bar(p, "Mode : " + md.name + statText(md.stats));
            return true;
        }
        if (m.triggerTag != null) p.addTag(m.triggerTag); // le code du modèle porté réagit au tag (BarrierEvents...)
        if (m.flashTag != null) Poses.timedTag(p, m.flashTag, m.flashTicks);
        if (m.fx == null) return true; // capacité purement passive (toggle)
        StyleData style = StyleService.get(p.server, p.getUUID());
        AbilityPower.Look lk = su.power.look;
        int glow = lk != null && lk.rgb() >= 0 ? lk.rgb() : -1;
        if ("beam".equals(m.fx.kind) && style.beamColor >= 0) glow = style.beamColor;
        Ctx ctx = new Ctx(p, p.serverLevel(), scale(m.fx, k), su.st, glow, speak, lk);
        if ("ram".equals(m.fx.kind)) st.ramCtx = ctx;
        boolean pass = su.power.passOn != null; // dessiné par le moteur d'effets du modèle
        if (pass) Fx.QUIET++;
        boolean ok;
        try {
            ok = run(ctx);
        } finally {
            if (pass) Fx.QUIET--;
        }
        if (ok && "ram".equals(m.fx.kind)) {
            st.ramUntil = p.serverLevel().getGameTime() + Math.max(4, m.fx.life > 0 ? m.fx.life : 12);
            st.ramHit.clear();
        }
        return ok;
    }

    // ------------------------------------------------------------------ attributs (vitesse, armure...)
    /** Ajoute ou retire les modificateurs d'attributs selon ce qui est actif. Idempotent. */
    private static void syncAttributes(ServerPlayer p, Setup su, St st) {
        for (int i = 0; i < su.power.attributes.size(); i++) {
            applyAttr(p, su.powerId + ":p" + i, su.power.attributes.get(i), st.on);
        }
        for (int j = 0; j < su.power.moves.size() && j < MAX_MOVES; j++) {
            AbilityPower.Move m = su.power.moves.get(j);
            boolean active = st.on && (st.toggled[j] || st.held[j]);
            for (int i = 0; i < m.attributes.size(); i++) applyAttr(p, su.powerId + ":m" + j + "_" + i, m.attributes.get(i), active);
        }
    }

    private static void applyAttr(ServerPlayer p, String key, AbilityPower.Attr a, boolean active) {
        ResourceLocation rl = a.attribute == null ? null : ResourceLocation.tryParse(a.attribute);
        Attribute attr = rl == null ? null : ForgeRegistries.ATTRIBUTES.getValue(rl);
        AttributeInstance inst = attr == null ? null : p.getAttribute(attr);
        if (inst == null) return;
        UUID id = UUID.nameUUIDFromBytes(("unrp:" + key).getBytes(StandardCharsets.UTF_8));
        boolean has = inst.getModifier(id) != null;
        if (active && !has) {
            inst.addTransientModifier(new AttributeModifier(id, "unrp " + key, a.amount,
                    AttributeModifier.Operation.fromValue(Math.max(0, Math.min(2, a.operation)))));
        } else if (!active && has) {
            inst.removeModifier(id);
        }
    }

    // ------------------------------------------------------------------ boucle serveur
    /** Toutes les 4 ticks : capacités maintenues, pertes d'aura, passifs, épuisement. */
    public static void tick(MinecraftServer server) {
        Walls.tick(server);
        Unlocks.tickPlay(server);
        long now = server.overworld().getGameTime();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            St st = STATE.get(p.getUUID());
            if (st == null) continue;
            if (Specials17.powerBlocked(p)) { allOff(p); continue; }
            if (st.lockUntil > 0) {
                lockout(p, st, now);
                continue;
            }
            if (st.on) tickOne(server, p, now);
            Map<String, St> others = OTHERS.get(p.getUUID());
            if (others != null) for (String k : new ArrayList<>(others.keySet())) {
                St o = others.get(k);
                if (o != null && o.on) withPower(p, k, () -> tickOne(server, p, now));
            }
            if (DEPLETED.remove(p.getUUID())) depleteAll(p);
        }
    }

    /** Un tick du pouvoir courant du joueur (allumé). */
    private static void tickOne(MinecraftServer server, ServerPlayer p, long now) {
        St st = STATE.get(p.getUUID());
        if (st == null || !st.on) return;
        Setup su = setup(p, false);
        if (su == null) {
            turnOff(p, null, st, null);
            return;
        }
        st.powerId = su.powerId;
        boolean dirty = false;
        double rate = su.power.drainWhenHurt && p.getHealth() >= p.getMaxHealth() ? 0 : rate(su.power.drainEvery, su.power.drain);
        if (su.power.healEvery > 0 && now % su.power.healEvery < 4 && p.getHealth() < p.getMaxHealth()) p.heal(su.power.healAmount);
        List<AbilityEffect.Eff> passive = new ArrayList<>(su.power.passive);
        if (!su.power.modes.isEmpty()) {
            AbilityPower.Mode mode = su.power.modes.get(st.mode % su.power.modes.size());
            rate += rate(mode.drainEvery, mode.drain);
            passive.addAll(mode.passive);
        }
        for (int i = 0; i < MAX_MOVES && i < su.power.moves.size(); i++) {
            AbilityPower.Move m = su.power.moves.get(i);
            boolean active = st.held[i] || st.toggled[i];
            if (active) {
                if (m.healEvery > 0 && now % m.healEvery < 4 && p.getHealth() < p.getMaxHealth()) p.heal(m.healAmount);
                if (st.toggled[i] && m.special != null) Specials.whileOn(p, su.power, m, Specials17.borrowedMoveStats(p, su.powerId, i, su.st), su.power.branchSource != null && m.stance >= 0 ? m.stance : st.stance, now);
                passive.addAll(m.passive);
                if (m.drainEvery > 0) rate += 1.0 / m.drainEvery;
            }
            if (m.fx != null && "shell".equals(m.fx.kind) && m.drainEvery > 0) rate += shellCount(p.getUUID()) / (double) m.drainEvery; // par barrière
            if (st.held[i]) { // la pose reste tant que la touche est tenue
                if (m.sound != null) Poses.holdQuiet(p, poseOf(p, m), 10);
                else Poses.hold(p, poseOf(p, m), 10);
            }
            if (st.held[i] && m.fx != null) { // pulsation d'une capacité maintenue
                int cost = m.drainEvery > 0 ? 0 : Math.max(1, (int) Math.round(m.cost * 0.2));
                if (cost > 0 && su.pr.aura < cost) {
                    st.held[i] = false;
                    bar(p, "Plus assez d'aura.");
                    return;
                }
                if (perform(p, su, st, m, m.k != null ? m.k : HELD_K, false) && cost > 0) {
                    su.pr.aura -= cost;
                    su.claims.setDirty();
                    Mastery.add(p, su.claims, su.pr, cost);
                    dirty = true;
                }
            }
        }
        for (int i = 0; i < MAX_MOVES; i++) if (st.charging[i]) chargeFx(p, su, st, i, now);
        st.cloaked = false;
        for (int i = 0; i < MAX_MOVES && i < su.power.moves.size(); i++) if (st.toggled[i] && su.power.moves.get(i).cloak) st.cloaked = true;
        st.draining = rate > 0;
        st.drainAcc += rate * 4;
        int loss = (int) Math.floor(st.drainAcc);
        if (loss > 0) {
            st.drainAcc -= loss;
            su.pr.aura = Math.max(0, su.pr.aura - loss);
            su.claims.setDirty();
            Mastery.add(p, su.claims, su.pr, loss);
            dirty = true;
        }
        if (su.pr.aura <= 0) {
            deplete(p, su, st);
            return;
        }
        if (now % 20 < 4) {
            for (AbilityEffect.Eff e : passive) {
                ResourceLocation rl = e.id == null ? null : ResourceLocation.tryParse(e.id);
                MobEffect m = rl == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(rl);
                if (m == MobEffects.INVISIBILITY && now < st.distortUntil) return; // cape troublée après une attaque
                if (m != null) p.addEffect(new MobEffectInstance(m, Math.max(30, (int) (e.secs * 20)), Math.max(0, e.amp), false, false));
            }
            if (su.power.senseRadius > 0) { // sens électrique : ce qui vit autour se met à briller
                for (LivingEntity o : p.serverLevel().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(su.power.senseRadius),
                        o -> o != p && o.isAlive())) {
                    o.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
                }
            }
            dirty = true;
        }
        syncAttributes(p, su, st);
        if (dirty) PlayerStateService.send(p);
        int ac = StyleService.get(server, p.getUUID()).auraColor;
        if (ac == 0x8A4DFF && su.power.look != null && su.power.look.rgb() >= 0) ac = su.power.look.rgb(); // couleur du webtoon tant que tu n'as pas choisi la tienne
        if (su.power.passOn == null) Styles.aura(p, su.power.look, ac, p.hasEffect(MobEffects.INVISIBILITY), now); // aura visible selon le style (sinon : moteur du modèle)
    }

    /** -1 aura toutes les N ticks => N ; ancien format en aura/seconde => aura/20 par tick. */
    private static double rate(int every, double perSecond) {
        if (every > 0) return 1.0 / every;
        return perSecond > 0 ? perSecond / 20.0 : 0;
    }

    private static void lockout(ServerPlayer p, St st, long now) {
        if (now >= st.lockUntil) {
            st.lockUntil = 0;
            Setup su = setup(p, false);
            if (su != null) {
                int max = PlayerStateService.maxAura(su.st);
                su.pr.aura = Math.min(max, Math.max(10, max / 4));
                su.claims.setDirty();
            }
            p.displayClientMessage(Component.literal("Aura restaurée").withStyle(net.minecraft.ChatFormatting.AQUA, net.minecraft.ChatFormatting.BOLD), true);
            PlayerStateService.send(p);
        } else if ((st.lockUntil - now) % 20 < 4) {
            p.displayClientMessage(Component.literal("Aura récupérée dans " + (int) Math.ceil((st.lockUntil - now) / 20.0) + " s")
                    .withStyle(net.minecraft.ChatFormatting.RED), true);
        }
    }

    private static void chargeFx(ServerPlayer p, Setup su, St st, int i, long now) {
        if (i >= su.power.moves.size()) return;
        AbilityPower.Move cm = su.power.moves.get(i);
        double prog = Math.min(1.0, (now - st.chargeAt[i]) / (double) Math.max(1, cm.charge));
        int col = su.power.look != null && su.power.look.rgb() >= 0 ? su.power.look.rgb() : 0xFFFFFF;
        Vfx.emit(p.serverLevel(), new DustParticleOptions(vec(col), (float) (0.9 + prog)), p.getX(), p.getY() + 1.0, p.getZ(),
                4 + (int) (prog * 12), 0.6 - 0.3 * prog, 0.8, 0.6 - 0.3 * prog, 0.0);
        if (su.power.look != null) accent(p.serverLevel(), su.power.look, p.position().add(0, 1.0, 0), 1 + (int) (prog * 4), 0.5);
    }

    /** Chaque tick : projectiles, barrières, bélier. */
    public static void tickFast(MinecraftServer server) {
        Specials.tick(server);
        Iterator<Bolt> bi = BOLTS.iterator();
        while (bi.hasNext()) if (!bi.next().step()) bi.remove();
        Iterator<Shell> si = SHELLS.iterator();
        while (si.hasNext()) {
            Shell sh = si.next();
            if (!sh.step(server)) si.remove();
        }
        long now = server.overworld().getGameTime();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            St st = STATE.get(p.getUUID());
            if (st != null && st.ramUntil > now && st.ramCtx != null) ramStep(st, p);
            if (st != null && st.on) {
                surge(p, st);
                AbilityPower pw = powerOf(p);
                if (pw != null && pw.climb && p.horizontalCollision) { // grimper aux murs
                    p.setDeltaMovement(p.getDeltaMovement().x, p.isShiftKeyDown() ? 0 : 0.2, p.getDeltaMovement().z);
                    p.fallDistance = 0;
                    p.hurtMarked = true;
                }
            }
            Long until = SHARED.get(p.getUUID());
            if (until != null) {
                if (now >= until) {
                    SHARED.remove(p.getUUID());
                    p.removeEffect(MobEffects.INVISIBILITY);
                    bar(p, "Ta cape empruntée s'estompe.");
                } else if (now % 20 == 0) {
                    p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, false, false));
                }
            }
        }
    }

    /** Course foudroyante : tant que la capacité "surge" est tenue, le joueur file droit devant. */
    private static void surge(ServerPlayer p, St st) {
        AbilityPower power = powerOf(p);
        if (power == null) return;
        for (int i = 0; i < MAX_MOVES && i < power.moves.size(); i++) {
            AbilityPower.Move m = power.moves.get(i);
            if (!st.held[i] || m.surge <= 0) continue;
            Vec3 look = p.getViewVector(1f);
            Vec3 flat = new Vec3(look.x, 0, look.z);
            if (flat.lengthSqr() < 1.0E-4) return;
            flat = flat.normalize().scale(m.surge);
            p.setDeltaMovement(flat.x, p.getDeltaMovement().y, flat.z);
            p.hurtMarked = true;
            return;
        }
    }

    /** Multiplicateur de dégâts de chute du pouvoir actif (1 = normal). */
    public static float fallMult(LivingEntity e) {
        if (!(e instanceof ServerPlayer p) || !isOn(p.getUUID())) return 1f;
        float m = 1f;
        for (String id : activePowers(p)) {
            AbilityPower pw = UnodexData.get().powers.get(id);
            if (pw != null && pw.fallMult > 0) m *= (float) pw.fallMult;
        }
        return m;
    }

    /** Cape (la sienne ou partagée) : vrai si le joueur est camouflé. */
    public static boolean cloaked(LivingEntity e) {
        if (SHARED.containsKey(e.getUUID())) return true;
        for (St st : allStates(e.getUUID())) if (st.on && st.cloaked) return true;
        return false;
    }

    // ------------------------------------------------------------------ événements (barrières, riposte)
    /** Une attaque qui traverse une barrière est arrêtée : c'est la barrière qui encaisse. */
    private static final String[] NO_DODGE = {"fall", "fire", "lava", "magic", "drown", "explosion", "wither", "starve", "cactus", "thorns",
            "freeze", "lightning", "hot_floor", "in_wall", "fly_into_wall", "out_of_world", "sweet_berry", "sting"};

    /** Esquive (SpeedDodgeEvents du modèle) : tout joueur a 12 % x (Vitesse / 10) de chances d'annuler une attaque, hors dégâts d'environnement. */
    private static boolean dodge(LivingAttackEvent e, ServerPlayer p) {
        if (!p.isAlive()) return false;
        String id = e.getSource().type().msgId();
        for (String k : NO_DODGE) if (id.contains(k)) return false;
        int[] st = currentStats(p);
        if (st == null) return false;
        double speed = Math.min(10, st[4]);
        if (speed <= 0 || p.getRandom().nextDouble() >= 0.12 * speed / 10.0) return false;
        e.setCanceled(true);
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.6f, 1.6f);
        Fx.sprite(p.serverLevel(), "mote", 0xFFFFFF, 0.9f, p.getBoundingBox().getCenter(), 8, 0.4, 0.02);
        bar(p, "Esquive !");
        return true;
    }

    public static void onAttack(LivingAttackEvent e) {
        LivingEntity victim = e.getEntity();
        Entity attacker = e.getSource().getEntity();
        if (!victim.level().isClientSide && Specials7.onAttack(e)) return;
        if (!victim.level().isClientSide) Specials9.onAttack(e);
        if (victim instanceof ServerPlayer vp && !vp.level().isClientSide && dodge(e, vp)) return;
        if (attacker == null || victim.level().isClientSide) return;
        if (attacker instanceof ServerPlayer ap) { // attaquer sous la cape la trouble 1 seconde
            St ast = null;
            for (St s0 : allStates(ap.getUUID())) if (s0.on && s0.cloaked) ast = s0;
            if (ast != null) {
                for (St s0 : allStates(ap.getUUID())) s0.distortUntil = ap.serverLevel().getGameTime() + 20;
                ap.removeEffect(MobEffects.INVISIBILITY);
                Fx.sprite(ap.serverLevel(), "mote", 0xD8E6FF, 0.8f, ap.position().add(0, 1, 0), 8, 0.3, 0.01);
            }
        }
        for (Shell sh : SHELLS) {
            if (sh.level != victim.level()) continue;
            if (attacker.getUUID().equals(sh.owner) || victim.getUUID().equals(sh.owner)) continue;
            if (sh.inside(victim) != sh.inside(attacker)) {
                e.setCanceled(true);
                sh.damage(e.getAmount());
                return;
            }
        }
    }

    /** "thorns" : qui te frappe au corps à corps prend des dégâts (Lightning : "hitting you hurts"). */
    public static void onHurt(LivingHurtEvent e) {
        Specials.onHurt(e);
        if (e.getSource().getEntity() instanceof ServerPlayer ap && e.getSource().getDirectEntity() == ap && ap != e.getEntity()) {
            for (St ast : allStates(ap.getUUID())) {
            AbilityPower apw = ast.powerId == null ? null : UnodexData.get().powers.get(ast.powerId);
            if (ast.on && apw != null) { // coups au corps à corps avec un interrupteur "on_hit" allumé (chaque pouvoir allumé)
                for (int i = 0; i < apw.moves.size() && i < MAX_MOVES; i++) {
                    if (!ast.toggled[i]) continue;
                    for (AbilityEffect.Eff ef : apw.moves.get(i).onHit) {
                        ResourceLocation rl = ef.id == null ? null : ResourceLocation.tryParse(ef.id);
                        MobEffect me = rl == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(rl);
                        if (me != null) e.getEntity().addEffect(new MobEffectInstance(me, (int) (ef.secs * 20), ef.amp));
                    }
                }
            }
            }
        }
        if (!(e.getEntity() instanceof ServerPlayer p) || e.getSource().is(DamageTypes.THORNS)) return;
        if (!(e.getSource().getEntity() instanceof LivingEntity att) || e.getSource().getDirectEntity() != att || att == p) return;
        if (!isOn(p.getUUID())) return;
        AbilityPower power = null;
        for (String id : activePowers(p)) { // riposte du premier pouvoir allumé qui en a une
            AbilityPower pw = UnodexData.get().powers.get(id);
            if (pw != null && pw.thorns > 0) { power = pw; break; }
        }
        if (power == null || power.thorns <= 0) return;
        long now = p.serverLevel().getGameTime();
        String key = p.getUUID() + ">" + att.getUUID();
        if (RETAL.getOrDefault(key, 0L) > now) return; // une riposte par attaquant et par seconde
        RETAL.put(key, now + 20);
        att.hurt(p.damageSources().thorns(p), (float) power.thorns);
        att.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0));
        if (power.passOn != null) {
            com.myriam.unrp.pass.LightningPass.retort(p, att, att.position().add(0, att.getBbHeight() * 0.5, 0));
        } else if (power.look != null) {
            accent(p.serverLevel(), power.look, att.getBoundingBox().getCenter(), 6, 0.3);
            Fx.line(p.serverLevel(), p.getBoundingBox().getCenter(), att.getBoundingBox().getCenter(), power.look.rgb() >= 0 ? power.look.rgb() : 0xFFFFFF,
                    Styles.of(power.look).line(), 0.08f, 4);
        }
    }

    private static AbilityPower powerOf(ServerPlayer p) {
        St st = STATE.get(p.getUUID());
        return st == null || st.powerId == null ? null : UnodexData.get().powers.get(st.powerId);
    }

    private static boolean hasShell(UUID owner) {
        return shellCount(owner) > 0;
    }

    private static int shellCount(UUID owner) {
        int n = 0;
        for (Shell sh : SHELLS) if (sh.owner.equals(owner) && !sh.ram) n++;
        return n;
    }

    /** Bonus/malus de stats en cours : mode du pouvoir + capacités allumées ou tenues (pouvoir actif seulement). */
    public static int[] statDelta(UUID id) {
        int[] d = new int[5];
        add(d, Specials17.statAdj(id)); // gages de Covenant (pris, gardés, prêtés)
        for (St st : allStates(id)) { // chaque pouvoir allumé
            if (!st.on || st.powerId == null) continue;
            AbilityPower power = UnodexData.get().powers.get(st.powerId);
            if (power == null) continue;
            if (!power.modes.isEmpty()) add(d, power.modes.get(st.mode % power.modes.size()).stats);
            for (int i = 0; i < power.moves.size() && i < MAX_MOVES; i++) {
                if (st.toggled[i] || st.held[i]) add(d, power.moves.get(i).stats);
            }
        }
        return d;
    }

    private static final String[] STAT_SHORT = {"Puissance", "Défense", "Récup.", "Ruse", "Vitesse"};

    /** " (Puissance +3, Défense -1)" pour le message de changement de mode. */
    private static String statText(int[] s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 5 && i < s.length; i++) {
            if (s[i] == 0) continue;
            b.append(b.length() == 0 ? " (" : ", ").append(STAT_SHORT[i]).append(s[i] > 0 ? " +" : " ").append(s[i]);
        }
        return b.length() == 0 ? "" : b.append(")").toString();
    }

    private static void add(int[] d, int[] s) {
        if (s == null) return;
        for (int i = 0; i < 5 && i < s.length; i++) d[i] += s[i];
    }

    /** Après un changement de mode ou de posture : ce qui n'est plus sur la barre s'éteint. */
    private static void dropHidden(ServerPlayer p, AbilityPower power, St st) {
        List<Integer> vis = visible(power, st, p);
        for (int i = 0; i < power.moves.size() && i < MAX_MOVES; i++) {
            if (vis.contains(i) || !(st.toggled[i] || st.held[i] || st.charging[i])) continue;
            st.toggled[i] = false;
            st.held[i] = false;
            st.charging[i] = false;
            Specials.stop(p, power.moves.get(i).special);
        }
    }

    public static void suspendBranch(ServerPlayer player, String power, int move) {
        if (power == null || move < 0 || move >= MAX_MOVES) return;
        String key = power.equals(basePowerId(player)) ? "" : power;
        withPower(player, key, () -> {
            St state = STATE.get(player.getUUID());
            AbilityPower definition = UnodexData.get().powers.get(power);
            if (state == null || definition == null || move >= definition.moves.size()) return;
            state.held[move] = state.toggled[move] = state.charging[move] = false;
            Specials.stop(player, definition.moves.get(move).special);
        });
        PlayerStateService.send(player);
    }

    /** Identifiant du pouvoir de base du joueur (sans copie), ou null. */
    public static String basePowerId(ServerPlayer p) {
        ClaimData.Profile pr = ClaimData.get(p.server).profileOf(p.getUUID()).orElse(null);
        if (pr == null) return null;
        Models.Character c = ClaimData.Profile.CHARACTER.equals(pr.kind) ? UnodexData.get().characters.get(pr.characterId) : null;
        String aid = pr.abilityOf(c);
        if (aid == null) return null;
        return UnodexData.get().aliases.getOrDefault(aid, aid);
    }

    /** Ajoute un pouvoir à la réserve sans le prendre en main (superpouvoir « _am » du modèle). */
    public static void addCopy(ServerPlayer p, String id) {
        St st = STATE.computeIfAbsent(p.getUUID(), k -> new St());
        st.reserve.add(id);
        PlayerStateService.send(p);
    }

    /** Retire un pouvoir de la réserve (et de la main s'il y était). */
    public static void removeCopy(ServerPlayer p, String id) {
        if (Specials17.hasLease(p, id)) return; // a contract loan ends through its collateral ledger
        St st = STATE.get(p.getUUID());
        if (st == null) return;
        Map<String, St> o = OTHERS.get(p.getUUID());
        if (o != null && o.containsKey(id)) { // copie allumée en arrière-plan : elle se coupe
            if (o.get(id).on) withPower(p, id, () -> turnOff(p, setup(p, false), STATE.get(p.getUUID()), null));
            o.remove(id);
        }
        st.reserve.remove(id);
        if (id.equals(st.copyId)) {
            Setup su = setup(p, false);
            if (st.on && su != null) turnOff(p, su, st, null);
            focus(p, "");
        }
        PlayerStateService.send(p);
    }

    /** Coupe le pouvoir s'il est actif (pouvoir retiré). */
    public static void forceOff(ServerPlayer p) {
        St s = stateOf(p, "");
        if (s.on) withPower(p, "", () -> turnOff(p, setup(p, false), STATE.get(p.getUUID()), null));
    }

    /** Pouvoirs copiés en réserve (Aura Manipulation). */
    public static java.util.Set<String> copies(ServerPlayer p) {
        St st = STATE.get(p.getUUID());
        return st == null ? java.util.Set.of() : java.util.Collections.unmodifiableSet(st.reserve);
    }

    /** Copie un pouvoir jusqu'à "until" (id null = fin de la copie). Le pouvoir se coupe pour changer de barre. */
    public static void setCopy(ServerPlayer p, String id, long until) {
        St st = STATE.computeIfAbsent(p.getUUID(), k -> new St());
        if (id == null) { // fin de toutes les copies : elles se coupent
            for (String r : new ArrayList<>(st.reserve)) removeCopy(p, r);
            st = STATE.get(p.getUUID());
            st.reserve.removeIf(idToRemove -> !Specials17.hasLease(p, idToRemove));
            focus(p, "");
        } else {
            st.reserve.add(id);
            focus(p, id); // la copie passe en main, le pouvoir quitté reste allumé
        }
        PlayerStateService.send(p);
    }

    /** Change le pouvoir courant (celui que montrent le hub et la première liste) sans rien couper. */
    private static void focus(ServerPlayer p, String k) {
        UUID id = p.getUUID();
        St cur = STATE.computeIfAbsent(id, x -> new St());
        if (key(cur).equals(k)) return;
        Map<String, St> o = OTHERS.computeIfAbsent(id, x -> new HashMap<>());
        St next = o.remove(k);
        if (next == null) {
            next = new St();
            next.copyId = k.isEmpty() ? null : k;
            next.copyUntil = Long.MAX_VALUE;
        }
        next.reserve.clear();
        next.reserve.addAll(cur.reserve);
        next.disabledUntil = cur.disabledUntil;
        next.lockUntil = cur.lockUntil;
        o.put(key(cur), cur);
        STATE.put(id, next);
    }

    /** Maj + R : passe au pouvoir copié suivant de la réserve, puis revient au sien (Aura Manipulation). */
    private static void cycleCopy(ServerPlayer p, St st) {
        List<String> order = heldKeys(p);
        if (order.isEmpty()) return;
        int next = (order.indexOf(key(st)) + 1) % order.size();
        focus(p, order.get(next));
        St now = STATE.get(p.getUUID());
        bar(p, now.copyId == null ? "Ton pouvoir" : "En main : " + now.copyId);
        PlayerStateService.send(p);
    }

    /**
     * État vu par le moteur d'effets du modèle : identifiant (avec « _am » pour une copie d'Aura Manipulation) et clés
     * des capacités allumées (passOn du pouvoir, pass des capacités tenues ou allumées, puis celles posées par le code
     * des pouvoirs via {@link com.myriam.unrp.pass.PassSync#key}).
     */
    public static com.myriam.unrp.pass.PassStatePacket passState(ServerPlayer p, java.util.Set<String> extra, int locked, boolean transformed) {
        String base = basePowerId(p);
        boolean am = "aura_manipulation".equals(base);
        List<String> paths = new ArrayList<>();
        List<List<String>> keys = new ArrayList<>();
        for (String k : heldKeys(p)) { // chaque pouvoir détenu, comme les « power holders » de Palladium
            withPower(p, k, () -> {
                St st = STATE.get(p.getUUID());
                Setup su = setup(p, false);
                if (su == null) return;
                boolean copy = st != null && st.copyId != null;
                List<String> on = new ArrayList<>();
                if (st != null && st.on && !disabled(p)) {
                    if (su.power.passOn != null) on.add(su.power.passOn);
                    on.add("toggle"); // l'interrupteur « toggle » des pouvoirs Palladium du modèle (calques)
                    if (am && copy) on.add("am_shown"); // AmPass.reveal : la copie se dévoile dès qu'on s'en sert
                    for (int i = 0; i < su.power.moves.size() && i < MAX_MOVES; i++) {
                        AbilityPower.Move m = su.power.moves.get(i);
                        if (m.pass != null && (st.toggled[i] || st.held[i])) on.add(m.pass);
                    }
                }
                java.util.Collections.sort(on);
                paths.add(su.powerId + (am && copy ? "_am" : ""));
                keys.add(on);
            });
        }
        List<String> common = new ArrayList<>(extra);
        for (String t : p.getTags()) {
            if (com.myriam.unrp.anim.PoseTags.ALL.contains(t) && !common.contains(t)) common.add(t); // poses tenues
            if (t.startsWith("unrp.key.") && !common.contains(t.substring(14))) common.add(t.substring(14)); // capacités brèves (calques)
            if (com.myriam.unrp.look.RenderLayerTags.get().contains(t) && !common.contains(t)) common.add(t); // conditions des calques de rendu du modèle
        }
        java.util.Collections.sort(common);
        return new com.myriam.unrp.pass.PassStatePacket(p.getUUID(), paths, keys, common, am, locked, transformed);
    }

    /** Conditions de tag d'une capacité (need_tag présent, block_tag absent). */
    static boolean tagsAllow(ServerPlayer p, AbilityPower.Move m) {
        if (m.needTag != null && !p.getTags().contains(m.needTag)) return false;
        if (m.needAny != null && !m.needAny.isEmpty()) {
            boolean any = false;
            for (String c : m.needAny) {
                if (c.startsWith("score:")) {
                    net.minecraft.world.scores.Objective o = p.server.getScoreboard().getObjective(c.substring(6));
                    if (o != null && p.server.getScoreboard().hasPlayerScore(p.getScoreboardName(), o)
                            && p.server.getScoreboard().getOrCreatePlayerScore(p.getScoreboardName(), o).getScore() >= 1) any = true;
                } else if (p.getTags().contains(c)) any = true;
            }
            if (!any) return false;
        }
        if (m.blockTag != null) for (String t : m.blockTag.split(",")) if (p.getTags().contains(t.trim())) return false;
        return true;
    }

    /** Pose/retire les « hold_tag » des capacités selon qu'elles sont tenues ou allumées (appelé chaque tick par PassSync). */
    public static void syncHoldTags(ServerPlayer p) {
        for (String k : heldKeys(p)) withPower(p, k, () -> { // hold_tag de chaque pouvoir détenu
            St st = STATE.get(p.getUUID());
            Setup su = setup(p, false);
            if (su == null) return;
            for (int i = 0; i < su.power.moves.size() && i < MAX_MOVES; i++) {
                AbilityPower.Move m = su.power.moves.get(i);
                if (m.holdTag == null) continue;
                boolean on = st != null && st.on && !disabled(p) && (st.toggled[i] || st.held[i]) && tagsAllow(p, m);
                if (on) p.addTag(m.holdTag);
                else p.removeTag(m.holdTag);
            }
        });
    }

    /** Pouvoir suspendu en ce moment (le tag « power_disabled » du modèle). */
    public static boolean disabled(ServerPlayer p) {
        St st = STATE.get(p.getUUID());
        return Specials17.powerBlocked(p) || (st != null && st.disabledUntil > p.serverLevel().getGameTime());
    }

    /** A recoverable suspension, distinct from another Sovereign still holding an ability as collateral. */
    public static boolean hasRecoverableDisable(ServerPlayer p) {
        St state=STATE.get(p.getUUID());
        return state!=null&&state.disabledUntil>p.serverLevel().getGameTime();
    }

    /** Medical recovery advances only during treatment; a new disable starts a fresh course. */
    public static double recoverDisabledAbility(ServerPlayer p,double percentPerSecond) {
        St state=STATE.get(p.getUUID());
        if(state==null||state.disabledUntil<=p.serverLevel().getGameTime())return 100;
        var data=com.myriam.unrp.item.UnItems.persisted(p);
        long episode=data.getLong("unrp_recovery_episode");
        double previous=episode==state.disabledUntil?data.getDouble("unrp_recovery_progress"):0;
        double progress=com.myriam.unrp.item.RecoveryTreatment.advance(previous,percentPerSecond);
        data.putLong("unrp_recovery_episode",state.disabledUntil);
        data.putDouble("unrp_recovery_progress",progress);
        if(progress>=100) {
            undisable(p);
            com.myriam.unrp.uo.ModelEnv.sync(p);
            com.myriam.unrp.player.PlayerStateService.send(p);
        }
        return progress;
    }

    /** Lève une suspension en cours (menottes retirées). */
    public static void undisable(ServerPlayer p) {
        St st = STATE.get(p.getUUID());
        if (st != null) st.disabledUntil = 0;
    }

    /** Suspend le pouvoir du joueur "ticks" ticks (il se coupe et ne peut pas être réactivé). */
    public static void disable(ServerPlayer p, int ticks) {
        if (Specials17.branchEscrowed(p)) { bar(p, "Une branche sous séquestre ne peut pas être scellée."); return; }
        allOff(p); // tous les pouvoirs du joueur
        St st = STATE.computeIfAbsent(p.getUUID(), k -> new St());
        st.disabledUntil = p.serverLevel().getGameTime() + ticks;
        var recovery=com.myriam.unrp.item.UnItems.persisted(p);
        recovery.putLong("unrp_recovery_episode",st.disabledUntil);
        recovery.putDouble("unrp_recovery_progress",0);
        bar(p, "Pouvoir suspendu");
    }

    /** Pouvoir et stats effectifs du joueur (pour les capacités qui se déclenchent au relâchement). */
    public static AbilityPower currentPower(ServerPlayer p) {
        Setup su = setup(p, false);
        return su == null ? null : su.power;
    }

    public static int[] currentStats(ServerPlayer p) {
        Setup su = setup(p, false);
        return su == null ? null : su.st;
    }

    /** Barre à pages : des postures, mais aucune capacité « stance_next » (la page change par Maj + R, comme la touche de liste du modèle). */
    static boolean pagedBar(AbilityPower power) {
        if (power.stances.isEmpty()) return false;
        for (AbilityPower.Move m : power.moves) if ("stance_next".equals(m.special)) return false;
        return true;
    }

    /** Capacités affichées sur la barre : celles de la posture courante et celles sans posture (stance -1), 5 au maximum. */
    static List<Integer> visible(AbilityPower power, St st, ServerPlayer p) {
        List<Integer> out = new ArrayList<>();
        int cur = st == null || power.stances.isEmpty() ? 0 : st.stance % power.stances.size();
        for (int i = 0; i < power.moves.size() && i < MAX_MOVES; i++) {
            AbilityPower.Move mv = power.moves.get(i);
            int sv = mv.stance;
            boolean stanceOk = sv < 0 || power.stances.isEmpty() || sv == cur;
            boolean modeOk = mv.mode < 0 || power.modes.isEmpty() || (st != null && mv.mode == st.mode % power.modes.size()) || (st == null && mv.mode == 0);
            boolean tagOk = mv.showTag == null || p == null || (mv.showTag.startsWith("!") ? !p.getTags().contains(mv.showTag.substring(1)) : p.getTags().contains(mv.showTag));
            boolean borrowed = power.branchSource != null;
            if ((borrowed || (stanceOk && modeOk && tagOk)) && (!borrowed || p == null || Specials17.borrowedMoveAllowed(p, power.branchSource, i))) out.add(i);
        }
        return out;
    }

    // ------------------------------------------------------------------ poses
    /** Pose jouée par une capacité : celle du JSON (variante accroupi si définie), sinon une pose par défaut selon l'effet. */
    private static String poseOf(ServerPlayer p, AbilityPower.Move m) {
        if (p.isShiftKeyDown() && m.poseSneak != null) return m.poseSneak;
        if (m.pose != null) return m.pose;
        if (m.fx == null || m.fx.kind == null) return null;
        return switch (m.fx.kind) {
            case "beam" -> "cast_beam";
            case "bolt", "teleport" -> "cast_bolt";
            case "strike" -> "cast_strike";
            case "blast" -> "cast_blast";
            case "pulse", "scan" -> "cast_pulse";
            case "buff" -> "cast_buff";
            case "dash", "ram" -> "cast_dash";
            case "heal", "share" -> "cast_heal";
            case "debuff", "dispel" -> "cast_mind";
            case "wall", "shell", "grow" -> "cast_wall";
            default -> null;
        };
    }

    /** Mort : le pouvoir se coupe et les barrières disparaissent (comme le modèle). */
    public static void onDeath(ServerPlayer p) {
        allOff(p);
        St st = STATE.get(p.getUUID());
        if (st != null) {
            setCopy(p, null, 0);
        }
    }

    /** Coup de poing dans le vide : s'il traverse la paroi d'une barrière (pas la sienne), elle encaisse le coup. */
    public static void punch(ServerPlayer p) {
        Specials17.onAttackAttempt(p);
        Vec3 eye = p.getEyePosition(), end = eye.add(p.getViewVector(1f).scale(4));
        double dmg = p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        for (Shell sh : SHELLS) {
            if (sh.level != p.level() || sh.owner.equals(p.getUUID()) || sh.ram) continue;
            boolean in0 = eye.distanceTo(sh.center) < sh.radius;
            // point du segment le plus proche du centre
            Vec3 d = end.subtract(eye);
            double t = Math.max(0, Math.min(1, sh.center.subtract(eye).dot(d) / d.lengthSqr()));
            boolean crosses = in0 ? end.distanceTo(sh.center) >= sh.radius : eye.add(d.scale(t)).distanceTo(sh.center) <= sh.radius;
            if (!crosses) continue;
            sh.damage(Math.max(1, dmg));
            p.serverLevel().playSound(null, BlockPos.containing(sh.center), SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 1.0f, 1.2f);
            return;
        }
    }

    /** Rend de l'aura (rappel d'une barrière). */
    private static void refund(ServerPlayer p, int amount) {
        Setup su = setup(p, false);
        if (su == null) return;
        su.pr.aura = Math.min(PlayerStateService.maxAura(su.st), su.pr.aura + amount);
        su.claims.setDirty();
    }

    private static void auraParticles(ServerPlayer p, int rgb) {
        Vfx.emit(p.serverLevel(), new DustParticleOptions(vec(rgb), 1.1f), p.getX(), p.getY() + 1.0, p.getZ(),
                2, 0.35, 0.7, 0.35, 0.0);
        Fx.sprite(p.serverLevel(), "mote", rgb, 0.8f, p.position().add(0, 0.9, 0), 3, 0.4, 0.01); // halo d'aura lumineux
    }

    private static Vector3f vec(int rgb) {
        return new Vector3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f);
    }

    /** Copie de l'effet mise à l'échelle de l'intensité k. */
    private static AbilityEffect scale(AbilityEffect f, double k) {
        double soft = 0.5 + 0.5 * k;
        AbilityEffect o = new AbilityEffect();
        o.kind = f.kind;
        o.cost = f.cost;
        o.particle = f.particle;
        o.hits = f.hits;
        o.cleanse = f.cleanse;
        o.report = f.report;
        o.bolt = f.bolt;
        o.block = f.block;
        o.up = f.up;
        o.speed = f.speed;
        o.life = f.life;
        o.max = f.max;
        o.recoil = f.recoil;
        o.dmg = f.dmg * k;
        o.kb = f.kb * k;
        o.range = f.range > 0 ? f.range * (0.6 + 0.4 * k) : 0;
        o.radius = f.radius > 0 ? f.radius * soft : 0;
        o.power = f.power * (0.4 + 0.6 * k);
        o.amount = f.amount * k;
        o.explosion = f.explosion * k >= 0.6 ? f.explosion * k : 0;
        o.lifesteal = f.lifesteal;
        o.glow = f.glow * soft;
        o.root = f.root * soft;
        o.fire = (int) Math.round(f.fire * k);
        for (AbilityEffect.Eff e : f.selfEffects) o.selfEffects.add(copy(e, soft));
        for (AbilityEffect.Eff e : f.effects) o.effects.add(copy(e, soft));
        if (k >= 1.0) { // pleine intensité : les valeurs d'un mouvement "action" ne sont pas rognées
            o.range = f.range;
            o.radius = f.radius;
            o.amount = f.amount;
            o.glow = f.glow;
            o.power = f.power;
        }
        return o;
    }

    private static AbilityEffect.Eff copy(AbilityEffect.Eff e, double soft) {
        AbilityEffect.Eff c = new AbilityEffect.Eff();
        c.id = e.id;
        c.amp = e.amp;
        c.secs = e.secs * soft;
        return c;
    }


    // ------------------------------------------------------------------ contexte
    /** Tout ce qu'un effet doit savoir : lanceur, monde, effet (déjà mis à l'échelle), stats, couleur, apparence. */
    private record Ctx(ServerPlayer p, ServerLevel level, AbilityEffect fx, int[] st, int glow, boolean speak, AbilityPower.Look look) {
        double dmg(double base) {
            return base * (0.7 + 0.15 * st[0]);
        }

        double secs(double base) {
            return base * (0.8 + 0.05 * st[3]);
        }

        double heal(double base) {
            return base * (0.7 + 0.15 * st[2]);
        }
    }

    private record Hit(Vec3 end, LivingEntity target, double dist) {}

    /** Aiguillage : "kind" de l'effet -> fonction. Pour ajouter un type : une ligne ici + une fonction. */
    private static boolean run(Ctx c) {
        return switch (c.fx.kind == null ? "" : c.fx.kind) {
            case "strike" -> strike(c);
            case "beam" -> beam(c);
            case "blast" -> blast(c);
            case "buff" -> {
                effects(c, c.p, c.fx.selfEffects);
                burst(c, c.p.position().add(0, 1, 0), 0.6, 20);
                yield true;
            }
            case "dash" -> dash(c);
            case "teleport" -> teleport(c);
            case "heal" -> heal(c);
            case "debuff" -> debuff(c);
            case "pulse" -> pulse(c);
            case "scan" -> scan(c);
            case "dispel" -> dispel(c);
            case "wall" -> wall(c);
            case "bolt" -> bolt(c);
            case "shell" -> shell(c);
            case "grow" -> grow(c);
            case "ram" -> ram(c);
            case "share" -> share(c);
            default -> {
                bar(c.p, "Type d'effet inconnu : " + c.fx.kind);
                yield false;
            }
        };
    }

    // ------------------------------------------------------------------ projectile (bolt)
    /** Projectile qui avance de "speed" blocs par tick pendant "life" ticks (Lightning Bolt du modèle : 2 et 15). */
    private static boolean bolt(Ctx c) {
        Vec3 look = c.p.getViewVector(1f);
        Vec3 start = c.p.getEyePosition().add(look.scale(0.5)).add(0, -0.1, 0);
        Bolt b = new Bolt(c, start, look.scale(c.fx.speed > 0 ? c.fx.speed : 2.0), c.fx.life > 0 ? c.fx.life : 15);
        BOLTS.add(b);
        if (b.pass) com.myriam.unrp.pass.LightningPass.launch(c.p, c.fx.passOp >= 0 ? c.fx.passOp : com.myriam.unrp.pass.PassFxPacket.LT_BOLT, b.aux, b.pos, b.vel, b.life, false);
        else c.level.playSound(null, c.p.blockPosition(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 0.35f, 1.8f);
        return true;
    }

    private static final class Bolt {
        final Ctx c;
        Vec3 pos;
        Vec3 vel;
        int life;
        /** dessiné par le moteur d'effets du modèle (numéro de projectile simulé : aux) */
        final boolean pass;
        final int aux;

        Bolt(Ctx c, Vec3 pos, Vec3 vel, int life) {
            this.c = c;
            this.pos = pos;
            this.vel = vel;
            this.life = life;
            AbilityPower pw = currentPower(c.p);
            this.pass = pw != null && pw.passOn != null;
            this.aux = pass ? com.myriam.unrp.pass.LightningPass.newAux() : 0;
        }

        boolean step() {
            if (!pass) return step0();
            Fx.QUIET++;
            try {
                boolean alive = step0();
                if (!alive && !ended) com.myriam.unrp.pass.LightningPass.gone(c.level, aux, pos);
                // projectile de Palladium : inertie 0,99 par tick (0,8 dans l'eau), comme le client la prévoit
                if (alive) vel = vel.scale(c.level.getFluidState(BlockPos.containing(pos)).isEmpty() ? 0.99 : 0.8);
                return alive;
            } finally {
                Fx.QUIET--;
            }
        }

        boolean ended;

        /** Avance d'un tick ; false = le projectile disparaît. */
        boolean step0() {
            if (c.p.isRemoved() || life-- <= 0) return false;
            Vec3 next = pos.add(vel);
            BlockHitResult bh = c.level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, c.p));
            Vec3 end = bh.getType() == HitResult.Type.MISS ? next : bh.getLocation();
            LivingEntity best = null;
            double bestD = Double.MAX_VALUE;
            for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, end).inflate(0.6),
                    e -> e != c.p && e.isAlive() && !e.isSpectator())) {
                var hit = e.getBoundingBox().inflate(0.3).clip(pos, end);
                if (hit.isPresent() && pos.distanceTo(hit.get()) < bestD) {
                    bestD = pos.distanceTo(hit.get());
                    best = e;
                }
            }
            if (best != null) end = pos.add(end.subtract(pos).normalize().scale(bestD));
            trail(c, pos, end);
            Fx.shell(c.level, (1 << 27) + c.p.getId(), end, 0.2, col(c), 1f, false); // tête lumineuse du projectile (orbe)
            pos = end;
            if (best != null) {
                if (c.fx.dmg > 0) hurt(c, best, c.dmg(c.fx.dmg));
                if (c.fx.kb != 0) best.knockback(c.fx.kb * 0.5, -vel.x, -vel.z);
                if (c.fx.fire > 0) best.setSecondsOnFire(c.fx.fire);
                effects(c, best, c.fx.effects);
                spawn(c, end, 12, 0.3);
                Fx.sprite(c.level, "ring", col(c), 2.5f, end, 1, 0, 0);
                Fx.shellGone(c.level, (1 << 27) + c.p.getId(), end, false);
                if (pass) { ended = true; com.myriam.unrp.pass.LightningPass.hit(c.p, aux, best, end); }
                return false;
            }
            if (bh.getType() != HitResult.Type.MISS) {
                if (pass) { // PassEvents.off : le point d'impact, décollé de la face touchée
                    ended = true;
                    Vec3 off = end.add(bh.getDirection().getStepX() * 0.05, bh.getDirection().getStepY() * 0.05, bh.getDirection().getStepZ() * 0.05);
                    com.myriam.unrp.pass.LightningPass.hit(c.p, aux, null, off);
                }
                spawn(c, end, 8, 0.2);
                Fx.shellGone(c.level, (1 << 27) + c.p.getId(), end, false);
                return false;
            }
            return true;
        }
    }

    // ------------------------------------------------------------------ barrière (shell / grow / ram)
    /**
     * Barrière en forme de sphère autour du lanceur (Barrier Deploy du modèle).
     *  - le lanceur la traverse librement ; personne d'autre ne passe, dans un sens comme dans l'autre (piège) ;
     *  - une attaque d'un côté vers l'autre est arrêtée : la barrière perd des PV à la place ;
     *  - les projectiles venant de l'extérieur sont renvoyés à leur tireur ;
     *  - elle se fissure (2/3, 1/3 des PV), et quand elle se brise le lanceur prend "recoil" dégâts ;
     *  - elle coûte -1 aura toutes les "drain_every" ticks tant qu'au moins une existe ; elle disparaît
     *    quand le pouvoir est coupé ou après "glow" secondes.
     */
    private static boolean shell(Ctx c) {
        UUID me = c.p.getUUID();
        Vec3 eye = c.p.getEyePosition(), look = c.p.getViewVector(1f);
        int col = c.glow >= 0 ? c.glow : 0x7FD4FF;
        double hp = c.heal(c.fx.amount > 0 ? c.fx.amount : 20) * (0.8 + 0.1 * c.st[1]);
        long until = c.level.getGameTime() + ticks(c.fx.glow > 0 ? c.fx.glow : 60);
        double r = c.fx.radius > 0 ? c.fx.radius : 2.5;
        if (c.p.isShiftKeyDown()) {
            // accroupi + regarder une de ses barrières (28 blocs) : rappel, l'aura est rendue
            Shell aimed = null;
            for (Shell sh : SHELLS) {
                if (!sh.owner.equals(me) || sh.ram || sh.level != c.level) continue;
                Vec3 toC = sh.center.subtract(eye);
                double t = toC.dot(look);
                if (t < 0 || t > 28) continue;
                if (eye.add(look.scale(t)).distanceTo(sh.center) <= sh.radius + 0.4) {
                    aimed = sh;
                    break;
                }
            }
            if (aimed != null) {
                aimed.dissipate();
                SHELLS.remove(aimed);
                refund(c.p, 15 + Math.max(0, c.fx.cost)); // le coût de ce lancer + 15, comme le modèle
                bar(c.p, "Barrière rappelée");
                return true;
            }
            // accroupi ailleurs : barrière externe là où tu vises (24 blocs), 2 au maximum
            BlockHitResult hit = c.level.clip(new ClipContext(eye, eye.add(look.scale(24)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, c.p));
            if (hit.getType() == HitResult.Type.MISS) {
                bar(c.p, "Vise un endroit à 24 blocs maximum.");
                return false;
            }
            List<Shell> ext = new ArrayList<>();
            for (Shell sh : SHELLS) if (sh.owner.equals(me) && !sh.self && !sh.ram) ext.add(sh);
            int max = c.fx.max > 0 ? c.fx.max : 2;
            while (ext.size() >= max) {
                Shell old = ext.remove(0);
                old.dissipate();
                SHELLS.remove(old);
            }
            Vec3 at = hit.getLocation().add(look.scale(-0.3)).add(0, r * 0.5, 0);
            SHELLS.add(new Shell(me, c.level, at, r, hp, until, col, c.fx.recoil > 0 ? c.fx.recoil : 4, false));
            bar(c.p, "Barrière externe dressée");
        } else {
            // barrière autour de soi : une seule à la fois (la nouvelle remplace l'ancienne)
            Iterator<Shell> it = SHELLS.iterator();
            while (it.hasNext()) {
                Shell sh = it.next();
                if (sh.owner.equals(me) && sh.self) {
                    sh.dissipate();
                    it.remove();
                }
            }
            SHELLS.add(new Shell(me, c.level, c.p.getBoundingBox().getCenter(), r, hp, until, col, c.fx.recoil > 0 ? c.fx.recoil : 4, true));
            bar(c.p, "Barrière dressée");
        }
        c.level.playSound(null, c.p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.8f);
        effects(c, c.p, c.fx.selfEffects);
        return true;
    }

    /**
     * Agrandit toutes tes barrières de "amount" blocs de rayon (maximum "range").
     * Accroupi : les contracte (minimum 1,2), et ce qui est à l'intérieur est écrasé (dégâts + lenteur).
     */
    private static boolean grow(Ctx c) {
        boolean shrink = c.p.isShiftKeyDown();
        double step = c.fx.amount > 0 ? c.fx.amount : 0.75;
        double maxR = c.fx.range > 0 ? c.fx.range : 8;
        boolean any = false;
        for (Shell sh : SHELLS) {
            if (!sh.owner.equals(c.p.getUUID()) || sh.ram) continue;
            any = true;
            if (shrink) {
                sh.radius = Math.max(1.2, sh.radius - step);
                for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, new AABB(sh.center, sh.center).inflate(sh.radius),
                        e -> e != c.p && e.isAlive() && sh.inside(e))) {
                    hurt(c, e, c.dmg(c.fx.dmg > 0 ? c.fx.dmg : 4));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1));
                }
            } else {
                sh.radius = Math.min(maxR, sh.radius + step);
            }
            sh.sides.clear(); // ce qui est maintenant englobé est "dedans"
            sh.sync(true);
        }
        if (!any) {
            bar(c.p, "Aucune barrière active.");
            return false;
        }
        bar(c.p, shrink ? "Barrières contractées : ceux à l'intérieur sont écrasés" : "Barrières agrandies");
        c.level.playSound(null, c.p.blockPosition(), shrink ? SoundEvents.AMETHYST_BLOCK_BREAK : SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS, 1.0f, shrink ? 0.7f : 1.2f);
        return true;
    }

    /** Bélier : élan vers l'avant derrière un bouclier ; ce qui est devant est projeté et blessé une fois. */
    private static boolean ram(Ctx c) {
        Vec3 look = c.p.getViewVector(1f);
        Vec3 flat = new Vec3(look.x, 0, look.z);
        flat = flat.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : flat.normalize();
        double power = c.fx.power > 0 ? c.fx.power : 1.6;
        c.p.setDeltaMovement(flat.x * power, 0.1, flat.z * power);
        c.p.hurtMarked = true;
        c.level.playSound(null, c.p.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0f, 0.6f);
        Fx.shield(c.level, c.p, col(c), (float) (c.fx.radius > 0 ? c.fx.radius : 2.5), c.fx.life > 0 ? c.fx.life : 12);
        effects(c, c.p, c.fx.selfEffects);
        return true;
    }

    private static void ramStep(St st, ServerPlayer p) {
        Ctx c = st.ramCtx;
        Vec3 look = p.getViewVector(1f);
        Vec3 flat = new Vec3(look.x, 0, look.z);
        flat = flat.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : flat.normalize();
        double power = c.fx.power > 0 ? c.fx.power : 1.6;
        p.setDeltaMovement(flat.x * power * 0.7, p.getDeltaMovement().y, flat.z * power * 0.7);
        p.hurtMarked = true;
        p.fallDistance = 0;
        Vec3 front = p.position().add(0, 1.0, 0).add(flat.scale(1.3));
        double r = c.fx.radius > 0 ? c.fx.radius : 2.5;
        // disque du bouclier, perpendiculaire à la course
        Vec3 side = new Vec3(-flat.z, 0, flat.x);
        for (int i = 0; i < 16; i += 4) {
            double a = 2 * Math.PI * i / 16;
            Vec3 pt = front.add(side.scale(Math.cos(a) * 1.2)).add(0, Math.sin(a) * 1.2, 0);
            Vfx.emit(c.level, particle(c), pt.x, pt.y, pt.z, 1, 0, 0, 0, 0);
        }
        for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, new AABB(front, front).inflate(r),
                e -> e != p && e.isAlive() && !e.isSpectator() && e.getBoundingBox().getCenter().distanceTo(front) <= r)) {
            if (!st.ramHit.add(e.getUUID())) continue;
            if (c.fx.dmg > 0) hurt(c, e, c.dmg(c.fx.dmg));
            Vec3 away = e.position().subtract(p.position());
            away = new Vec3(away.x, 0, away.z);
            away = away.lengthSqr() < 1.0E-4 ? flat : away.normalize();
            double kb = c.fx.kb != 0 ? c.fx.kb : 2.0;
            push(e, away.x * kb * 0.6, 0.35 + c.fx.up, away.z * kb * 0.6);
            effects(c, e, c.fx.effects);
            spawn(c, e.getBoundingBox().getCenter(), 8, 0.3);
        }
    }

    private static final class Shell {
        final UUID owner;
        final ServerLevel level;
        final Vec3 center;
        double radius;
        double hp;
        final double maxHp;
        final long until;
        final int color;
        final double recoil;
        int stage;
        int age;
        final int id = ++shellIds;
        /** côté de chaque créature proche (true = dedans), mémorisé la première fois qu'on la voit */
        final Map<UUID, Boolean> sides = new HashMap<>();

        /** barrière autour de soi (une seule) ou externe (2 maximum) ; ram = bouclier du Bélier (non utilisé ici) */
        final boolean self;
        boolean ram;

        Shell(UUID owner, ServerLevel level, Vec3 center, double radius, double hp, long until, int color, double recoil, boolean self) {
            this.self = self;
            this.owner = owner;
            this.level = level;
            this.center = center;
            this.radius = radius;
            this.hp = hp;
            this.maxHp = hp;
            this.until = until;
            this.color = color;
            this.recoil = recoil;
            Fx.shell(level, id, center, radius, color, 1f, false);
        }

        /** Renvoie l'état aux clients proches (rendu de la sphère : client/FxRenderer). */
        void sync(boolean hit) {
            Fx.shell(level, id, center, radius, color, (float) Math.max(0, hp / maxHp), hit);
        }

        boolean inside(Entity e) {
            return e.getBoundingBox().getCenter().distanceTo(center) < radius;
        }

        /** Un tick ; false = la barrière disparaît. */
        boolean step(MinecraftServer server) {
            age++;
            ServerPlayer o = server.getPlayerList().getPlayer(owner);
            if (o == null || !isOn(owner) || level.getGameTime() >= until) {
                dissipate();
                return false;
            }
            // personne ne traverse, sauf le lanceur
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius + 2),
                    e -> e.isAlive() && !e.isSpectator() && !e.getUUID().equals(owner))) {
                boolean in = inside(e);
                Boolean was = sides.putIfAbsent(e.getUUID(), in);
                if (was != null && was != in) keepOnSide(e, was);
            }
            // projectiles venant de l'autre côté : renvoyés
            for (Projectile pr : level.getEntitiesOfClass(Projectile.class, new AABB(center, center).inflate(radius + 1.5))) {
                Vec3 pos = pr.position();
                double d = pos.distanceTo(center);
                Entity shooter = pr.getOwner();
                if (shooter != null && shooter.getUUID().equals(owner)) continue;
                boolean shooterIn = shooter != null && inside(shooter);
                boolean crossing = shooterIn ? d > radius - 0.8 && d < radius + 0.5 : d < radius + 0.8 && d > radius - 0.5;
                Vec3 v = pr.getDeltaMovement();
                boolean towardsSurface = shooterIn ? v.dot(pos.subtract(center)) > 0 : v.dot(center.subtract(pos)) > 0;
                if (crossing && towardsSurface) {
                    pr.setDeltaMovement(v.scale(-1));
                    pr.setOwner(o);
                    pr.hurtMarked = true;
                    damage(2);
                }
            }
            if (age % 10 == 0) sync(false);
            if (age % 3 == 0) draw();
            return hp > 0;
        }

        private void keepOnSide(LivingEntity e, boolean in) {
            Vec3 c = e.getBoundingBox().getCenter();
            Vec3 dir = c.subtract(center);
            dir = dir.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : dir.normalize();
            double target = in ? radius - 0.7 : radius + 0.7;
            Vec3 to = center.add(dir.scale(target)).subtract(c.subtract(e.position()));
            e.teleportTo(to.x, to.y, to.z);
            e.setDeltaMovement(dir.scale(in ? -0.2 : 0.3));
            e.hurtMarked = true;
            Vfx.emit(level, dust(1.2f), c.x, c.y, c.z, 6, 0.2, 0.3, 0.2, 0);
        }

        private DustParticleOptions dust(float size) {
            float f = (float) Math.max(0.35, hp / maxHp);
            Vector3f v = vec(color);
            return new DustParticleOptions(new Vector3f(v.x * f + (1 - f), v.y * f, v.z * f), size); // rougit en s'abîmant
        }

        /** Quelques éclats hexagonaux qui scintillent sur la surface (la sphère elle-même est dessinée par le client). */
        private void draw() {
            for (int i = 0; i < 3; i++) {
                double y = level.random.nextDouble() * 2 - 1, th = level.random.nextDouble() * Math.PI * 2, rr = Math.sqrt(1 - y * y);
                Vec3 p = center.add(Math.cos(th) * rr * radius, y * radius, Math.sin(th) * rr * radius);
                Fx.sprite(level, "hex", color, 1.4f, p, 1, 0, 0);
            }
        }

        void damage(double amount) {
            hp -= amount;
            if (hp > 0) sync(true);
            int ns = hp <= maxHp / 3 ? 2 : hp <= maxHp * 2 / 3 ? 1 : 0;
            if (ns > stage) {
                stage = ns;
                level.playSound(null, BlockPos.containing(center), SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 1.2f, 0.7f);
                Vfx.emit(level, ParticleTypes.CRIT, center.x, center.y, center.z, 20, radius * 0.5, radius * 0.5, radius * 0.5, 0.1);
            }
            if (hp <= 0) shatter();
        }

        /** Brisée par les coups : le lanceur est blessé (comme dans le webtoon). */
        void shatter() {
            hp = 0;
            level.playSound(null, BlockPos.containing(center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5f, 0.8f);
            Fx.shellGone(level, id, center, true);
            Vfx.emit(level, dust(1.5f), center.x, center.y, center.z, 60, radius * 0.6, radius * 0.6, radius * 0.6, 0.05);
            ServerPlayer o = level.getServer().getPlayerList().getPlayer(owner);
            if (o != null) {
                o.hurt(o.damageSources().magic(), (float) recoil);
                o.displayClientMessage(Component.literal("Ta barrière s'est brisée !"), true);
            }
        }

        /** Disparition normale (pouvoir coupé, durée écoulée). */
        void dissipate() {
            Fx.shellGone(level, id, center, false);
            Vfx.emit(level, dust(1.0f), center.x, center.y, center.z, 25, radius * 0.5, radius * 0.5, radius * 0.5, 0.02);
        }
    }

    // ------------------------------------------------------------------ partage (Shared Cloak)
    /** Cape partagée : l'allié visé (même groupe ou même école, "range" blocs) est camouflé "glow" secondes ; "max" alliés à la fois. */
    private static boolean share(Ctx c) {
        double range = c.fx.range > 0 ? c.fx.range : 12;
        Vec3 eye = c.p.getEyePosition(), look = c.p.getViewVector(1f);
        ServerPlayer best = null;
        double bt = Double.MAX_VALUE;
        for (ServerPlayer o : c.level.players()) { // l'allié visé (dans l'axe du regard)
            if (o == c.p || o.isSpectator() || !com.myriam.unrp.social.SocialService.allies(c.p.server, c.p.getUUID(), o.getUUID())) continue;
            Vec3 to = o.getBoundingBox().getCenter().subtract(eye);
            double t = to.dot(look);
            if (t < 0 || t > range) continue;
            if (eye.add(look.scale(t)).distanceTo(o.getBoundingBox().getCenter()) <= 1.2 && t < bt) {
                bt = t;
                best = o;
            }
        }
        if (best == null) {
            bar(c.p, "Aucun allié en vue (même groupe ou même école, " + (int) range + " blocs).");
            return false;
        }
        int maxShared = c.fx.max > 0 ? c.fx.max : 4;
        long now = c.level.getGameTime();
        int count = 0;
        for (Long until : SHARED.values()) if (until > now) count++;
        if (count >= maxShared && !SHARED.containsKey(best.getUUID())) {
            bar(c.p, "Trop d'alliés déjà camouflés (" + maxShared + " maximum).");
            return false;
        }
        double secs = c.secs(c.fx.glow > 0 ? c.fx.glow : 10);
        SHARED.put(best.getUUID(), now + ticks(secs));
        best.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, false, false));
        trail(c, c.p.getEyePosition().add(0, -0.3, 0), best.getBoundingBox().getCenter());
        effects(c, c.p, c.fx.selfEffects);
        burst(c, best.getBoundingBox().getCenter(), 0.6, 20);
        c.level.playSound(null, best.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9f, 1.4f);
        bar(best, "Tu as été camouflé(e) par " + c.p.getName().getString());
        bar(c.p, "Cape partagée avec " + best.getName().getString());
        return true;
    }


    // ------------------------------------------------------------------ types d'effet
    /** Coup au corps à corps dans un cône devant soi. */
    private static boolean strike(Ctx c) {
        ServerPlayer p = c.p;
        Vec3 look = p.getViewVector(1f).normalize();
        Vec3 eye = p.getEyePosition();
        double reach = c.fx.range > 0 ? c.fx.range : 3.5;
        List<LivingEntity> targets = new ArrayList<>();
        for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(reach),
                e -> e != p && e.isAlive() && !e.isSpectator())) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(eye);
            if (to.length() <= reach + 0.5 && to.normalize().dot(look) > 0.4) targets.add(e);
        }
        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr(p)));
        if (targets.size() > (c.fx.single ? 1 : 5)) targets = targets.subList(0, c.fx.single ? 1 : 5);

        boolean selfOnly = targets.isEmpty() && !c.fx.selfEffects.isEmpty();
        if (targets.isEmpty() && !selfOnly) {
            bar(p, "Aucune cible à portée.");
            return false;
        }
        effects(c, p, c.fx.selfEffects);
        if (c.fx.passOp == com.myriam.unrp.pass.PassFxPacket.LT_TOUCH) { // LightningEvents.shockTouch
            LivingEntity t0 = targets.isEmpty() ? null : targets.get(0);
            com.myriam.unrp.pass.LightningPass.touch(p, t0, t0 != null ? t0.position().add(0, t0.getBbHeight() * 0.5, 0) : eye.add(look.scale(2.0)));
        }
        spawn(c, p.getEyePosition().add(look.scale(Math.min(reach, 2.0))), 6, 0.4);
        sweep(c, p, look, Math.min(reach, 2.6));
        for (LivingEntity e : targets) {
            hurt(c, e, c.dmg(c.fx.dmg) * Math.max(1, c.fx.hits));
            if (c.fx.kb != 0) e.knockback(c.fx.kb * 0.5, -look.x, -look.z);
            if (c.fx.up != 0) e.setDeltaMovement(e.getDeltaMovement().add(0, c.fx.up, 0));
            if (c.fx.fire > 0) e.setSecondsOnFire(c.fx.fire);
            effects(c, e, c.fx.effects);
            spawn(c, e.getBoundingBox().getCenter(), 8, 0.3);
            Fx.line(c.level, p.getEyePosition().add(look.scale(0.6)).add(0, -0.4, 0), e.getBoundingBox().getCenter(), col(c),
                    Styles.of(c.look).line(), 0.1f, 4);
            Fx.sprite(c.level, "ring", col(c), 1.2f, e.getBoundingBox().getCenter(), 1, 0, 0);
        }
        return true;
    }

    /** Rayon : touche la première créature ou le premier bloc sur la ligne de visée. */
    private static boolean beam(Ctx c) {
        ServerPlayer p = c.p;
        double range = c.fx.range > 0 ? c.fx.range : 24;
        Hit hit = ray(p, range);
        trail(c, p.getEyePosition().add(0, -0.2, 0), hit.end);
        LivingEntity t = hit.target;
        if (t != null) {
            if (c.fx.dmg > 0) {
                double d = c.dmg(c.fx.dmg);
                hurt(c, t, d);
                if (c.fx.lifesteal > 0) p.heal((float) (d * c.fx.lifesteal));
            }
            Vec3 dir = hit.end.subtract(p.getEyePosition()).normalize();
            if (c.fx.kb != 0) t.knockback(c.fx.kb * 0.5, -dir.x, -dir.z);
            if (c.fx.up != 0) push(t, 0, c.fx.up, 0);
            if (c.fx.fire > 0) t.setSecondsOnFire(c.fx.fire);
            effects(c, t, c.fx.effects);
        } else if (c.fx.dmg > 0 && c.fx.explosion <= 0 && c.fx.range <= 6) {
            bar(p, "Aucune cible à portée."); // capacités de contact : on ne gaspille pas l'aura
            return false;
        }
        if (c.fx.bolt) {
            LightningBolt lb = EntityType.LIGHTNING_BOLT.create(c.level);
            if (lb != null) {
                lb.moveTo(hit.end.x, hit.end.y, hit.end.z);
                lb.setVisualOnly(true);
                c.level.addFreshEntity(lb);
            }
        }
        if (c.fx.explosion > 0) explode(c, hit.end, c.fx.explosion);
        spawn(c, hit.end, 10, 0.3);
        return true;
    }

    /** Onde autour de soi. Un kb négatif attire au lieu de repousser. */
    private static boolean blast(Ctx c) {
        ServerPlayer p = c.p;
        double r = c.fx.radius > 0 ? c.fx.radius : 6;
        Vec3 origin = p.position();
        for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r),
                e -> e != p && e.isAlive() && !e.isSpectator() && e.distanceToSqr(p) <= r * r)) {
            if (c.fx.dmg > 0) hurt(c, e, c.dmg(c.fx.dmg));
            Vec3 away = e.position().subtract(origin);
            away = new Vec3(away.x, 0, away.z);
            if (away.lengthSqr() > 1.0E-4 && c.fx.kb != 0) {
                Vec3 d = away.normalize().scale(c.fx.kb * 0.6);
                push(e, d.x, c.fx.up, d.z);
            } else if (c.fx.up != 0) {
                push(e, 0, c.fx.up, 0);
            }
            effects(c, e, c.fx.effects);
        }
        if (c.fx.explosion > 0) explode(c, origin.add(0, 0.5, 0), c.fx.explosion);
        ring(c, origin, r);
        return true;
    }

    private static boolean dash(Ctx c) {
        ServerPlayer p = c.p;
        Vec3 look = p.getViewVector(1f);
        double power = c.fx.power > 0 ? c.fx.power : 2.0;
        p.setDeltaMovement(look.x * power, Math.max(look.y * power * 0.5, 0) + c.fx.up, look.z * power);
        p.hurtMarked = true;
        p.fallDistance = 0;
        effects(c, p, c.fx.selfEffects);
        burst(c, p.position().add(0, 0.5, 0), 0.5, 18);
        return true;
    }

    private static boolean teleport(Ctx c) {
        ServerPlayer p = c.p;
        double range = c.fx.range > 0 ? c.fx.range : 10;
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getViewVector(1f);
        BlockHitResult bh = c.level.clip(new ClipContext(eye, eye.add(look.scale(range)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        double max = bh.getType() == HitResult.Type.MISS ? range : Math.max(0, eye.distanceTo(bh.getLocation()) - 0.6);
        for (double d = max; d >= 1.0; d -= 0.5) {
            Vec3 off = look.scale(d);
            if (c.level.noCollision(p, p.getBoundingBox().move(off.x, off.y, off.z))) {
                burst(c, p.position().add(0, 1, 0), 0.5, 25);
                Vec3 before = p.position().add(0, 1, 0);
                p.teleportTo(p.getX() + off.x, p.getY() + off.y, p.getZ() + off.z);
                trail(c, before, p.position().add(0, 1, 0));
                p.fallDistance = 0;
                burst(c, p.position().add(0, 1, 0), 0.5, 25);
                effects(c, p, c.fx.selfEffects);
                return true;
            }
        }
        bar(p, "Pas de place pour arriver là.");
        return false;
    }

    private static boolean heal(Ctx c) {
        ServerPlayer p = c.p;
        LivingEntity target = p;
        if (c.fx.range > 0) {
            Hit hit = ray(p, c.fx.range);
            if (hit.target != null) target = hit.target;
            trail(c, p.getEyePosition().add(0, -0.2, 0), target == p ? p.getEyePosition() : target.getBoundingBox().getCenter());
        }
        if (c.fx.amount > 0) target.heal((float) c.heal(c.fx.amount));
        if (c.fx.cleanse) {
            for (MobEffectInstance inst : new ArrayList<>(target.getActiveEffects())) {
                if (inst.getEffect().getCategory() == MobEffectCategory.HARMFUL) target.removeEffect(inst.getEffect());
            }
        }
        effects(c, target, c.fx.effects);
        effects(c, p, c.fx.selfEffects);
        burst(c, target.getBoundingBox().getCenter(), 0.5, 15);
        return true;
    }

    private static boolean debuff(Ctx c) {
        ServerPlayer p = c.p;
        Hit hit = ray(p, c.fx.range > 0 ? c.fx.range : 16);
        if (hit.target == null) {
            bar(p, "Aucune cible en vue.");
            return false;
        }
        LivingEntity t = hit.target;
        trail(c, p.getEyePosition().add(0, -0.2, 0), t.getBoundingBox().getCenter());
        if (c.fx.dmg > 0) hurt(c, t, c.dmg(c.fx.dmg));
        effects(c, t, c.fx.effects);
        if (c.fx.root > 0) {
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks(c.secs(c.fx.root)), 7));
        }
        effects(c, p, c.fx.selfEffects);
        burst(c, t.getBoundingBox().getCenter(), 0.5, 20);
        return true;
    }

    private static boolean pulse(Ctx c) {
        ServerPlayer p = c.p;
        double r = c.fx.radius > 0 ? c.fx.radius : 8;
        for (LivingEntity e : c.level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r),
                e -> e != p && e.isAlive() && !e.isSpectator() && e.distanceToSqr(p) <= r * r)) {
            if (c.fx.dmg > 0) hurt(c, e, c.dmg(c.fx.dmg));
            effects(c, e, c.fx.effects);
        }
        effects(c, p, c.fx.selfEffects);
        ring(c, p.position(), r);
        return true;
    }

    /** Détection : met en surbrillance et/ou liste les personnages proches (nom, tier, niveau, distance). */
    private static boolean scan(Ctx c) {
        ServerPlayer p = c.p;
        double r = c.fx.radius > 0 ? c.fx.radius : 24;
        List<LivingEntity> found = c.level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(r),
                e -> e != p && e.isAlive() && !e.isSpectator() && e.distanceToSqr(p) <= r * r);
        List<String> lines = new ArrayList<>();
        int others = 0;
        for (LivingEntity e : found) {
            if (c.fx.glow > 0) {
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks(c.secs(c.fx.glow)), 0));
            }
            if (e instanceof ServerPlayer sp) {
                PlayerStatePacket s = PlayerStateService.build(sp);
                String who = s.hasProfile ? s.name + " (" + tierName(s.tier) + " " + s.level + ")" : sp.getName().getString();
                lines.add(who + " - " + Math.round(Math.sqrt(e.distanceToSqr(p))) + " m");
            } else {
                others++;
            }
        }
        effects(c, p, c.fx.selfEffects);
        ring(c, p.position(), Math.min(r, 12));
        if (c.fx.report) {
            if (lines.isEmpty() && others == 0) {
                p.sendSystemMessage(Component.literal("[Détection] Personne dans un rayon de " + (int) r + " m."));
            } else {
                p.sendSystemMessage(Component.literal("[Détection] " + lines.size() + " personnage(s), " + others + " autre(s) être(s) vivant(s) :"));
                lines.stream().limit(8).forEach(l -> p.sendSystemMessage(Component.literal(" - " + l)));
            }
        } else {
            bar(p, found.size() + " présence(s) détectée(s).");
        }
        return true;
    }

    private static boolean dispel(Ctx c) {
        Hit hit = ray(c.p, c.fx.range > 0 ? c.fx.range : 20);
        LivingEntity t = hit.target != null ? hit.target : c.p;
        t.removeAllEffects();
        trail(c, c.p.getEyePosition().add(0, -0.2, 0), t == c.p ? c.p.getEyePosition().add(0, 0, 1) : t.getBoundingBox().getCenter());
        burst(c, t.getBoundingBox().getCenter(), 0.5, 25);
        return true;
    }

    /** Barrière : mur de blocs temporaires devant soi (largeur 2*radius+1, hauteur amount, à range blocs). */
    private static boolean wall(Ctx c) {
        ServerPlayer p = c.p;
        Vec3 look = p.getViewVector(1f);
        Vec3 f = new Vec3(look.x, 0, look.z);
        f = f.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : f.normalize();
        Vec3 side = new Vec3(-f.z, 0, f.x);
        double dist = c.fx.range > 0 ? c.fx.range : 3;
        int half = (int) Math.round(c.fx.radius > 0 ? c.fx.radius : 2);
        int height = (int) Math.max(1, Math.round(c.fx.amount > 0 ? c.fx.amount : 3));
        long ttl = ticks(c.fx.glow > 0 ? c.fx.glow : 10);
        ResourceLocation rl = ResourceLocation.tryParse(c.fx.block == null ? "light_blue_stained_glass" : c.fx.block);
        Block blk = rl == null ? null : ForgeRegistries.BLOCKS.getValue(rl);
        BlockState state = (blk == null || blk == Blocks.AIR ? Blocks.LIGHT_BLUE_STAINED_GLASS : blk).defaultBlockState();
        Vec3 base = p.position().add(f.scale(dist));
        int placed = 0;
        for (int w = -half; w <= half; w++) {
            for (int h = 0; h < height; h++) {
                Vec3 v = base.add(side.scale(w));
                BlockPos pos = BlockPos.containing(v.x, p.getY() + h, v.z);
                if (Walls.place(c.level, pos, state, ttl)) placed++;
            }
        }
        if (placed == 0) {
            bar(p, "Pas de place pour une barrière.");
            return false;
        }
        Vfx.emit(c.level, particle(c), base.x, p.getY() + height / 2.0, base.z, 12, half * 0.5, height * 0.3, 0.2, 0.01);
        return true;
    }

    // ------------------------------------------------------------------ outils
    private static Hit ray(ServerPlayer p, double range) {
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getViewVector(1f);
        Vec3 end = eye.add(look.scale(range));
        BlockHitResult bh = p.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        if (bh.getType() != HitResult.Type.MISS) end = bh.getLocation();
        AABB box = p.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult eh = ProjectileUtil.getEntityHitResult(p, eye, end, box,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e != p, range * range);
        if (eh != null && eh.getEntity() instanceof LivingEntity le) {
            return new Hit(eh.getLocation(), le, eye.distanceTo(eh.getLocation()));
        }
        return new Hit(end, null, eye.distanceTo(end));
    }

    private static void hurt(Ctx c, LivingEntity t, double amount) {
        if (amount > 0) t.hurt(c.p.damageSources().playerAttack(c.p), (float) amount);
    }

    private static void push(Entity e, double x, double y, double z) {
        e.setDeltaMovement(e.getDeltaMovement().add(x, y, z));
        e.hurtMarked = true;
    }

    private static void explode(Ctx c, Vec3 at, double radius) {
        c.level.explode(c.p, at.x, at.y, at.z, (float) radius, Level.ExplosionInteraction.NONE);
    }

    private static int ticks(double secs) {
        return (int) Math.max(1, Math.round(secs * 20));
    }

    private static void effects(Ctx c, LivingEntity target, List<AbilityEffect.Eff> list) {
        for (AbilityEffect.Eff e : list) {
            ResourceLocation rl = e.id == null ? null : ResourceLocation.tryParse(e.id);
            MobEffect m = rl == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(rl);
            if (m != null) target.addEffect(new MobEffectInstance(m, ticks(c.secs(e.secs)), Math.max(0, e.amp)));
        }
    }

    private static String tierName(String tier) {
        return switch (tier == null ? "" : tier) {
            case "cripple", "cripple_life" -> "Cripple";
            case "low" -> "Low";
            case "mid" -> "Mid";
            case "elite" -> "Elite";
            case "high" -> "High";
            case "god" -> "God";
            case "exo_god" -> "Exo-god";
            default -> "?";
        };
    }

    private static void bar(ServerPlayer p, String text) {
        p.displayClientMessage(Component.literal(text), true);
    }

    // ------------------------------------------------------------------ particules
    private static ParticleOptions particle(Ctx c) {
        if (c.glow >= 0) {
            return new DustParticleOptions(new Vector3f(((c.glow >> 16) & 255) / 255f, ((c.glow >> 8) & 255) / 255f, (c.glow & 255) / 255f), 1.0f);
        }
        ResourceLocation rl = ResourceLocation.tryParse(c.fx.particle == null ? "crit" : c.fx.particle);
        Object type = rl == null ? null : ForgeRegistries.PARTICLE_TYPES.getValue(rl);
        return type instanceof ParticleOptions po ? po : ParticleTypes.CRIT;
    }

    private static void spawn(Ctx c, Vec3 at, int count, double spread) {
        Vfx.emit(c.level, particle(c), at.x, at.y, at.z, Math.max(1, count / 2), spread, spread, spread, 0.02);
        Styles.impact(c.level, c.look, col(c), at, Math.max(3, count / 2)); // éclats du style + anneau
    }

    /** Particule propre au pouvoir (étincelles de foudre, flammes, neige...). */
    private static void accent(ServerLevel level, AbilityPower.Look look, Vec3 at, int count, double spread) {
        int rgb = look.rgb() >= 0 ? look.rgb() : 0xFFFFFF;
        Fx.sprite(level, Styles.sprite(look), rgb, 1.0f, at, count, spread, 0.02); // particule du style, teintée
        ResourceLocation rl = look.particle == null ? null : ResourceLocation.tryParse(look.particle);
        Object type = rl == null ? null : ForgeRegistries.PARTICLE_TYPES.getValue(rl);
        if (type instanceof ParticleOptions po && count > 1) Vfx.emit(level, po, at.x, at.y, at.z, count / 2, spread, spread, spread, 0.03);
    }

    /** Arc de balayage lumineux devant le lanceur (coups au corps à corps), dans le style du pouvoir. */
    private static void sweep(Ctx c, ServerPlayer p, Vec3 look, double r) {
        Vec3 flat = new Vec3(look.x, 0, look.z);
        flat = flat.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : flat.normalize();
        Vec3 side = new Vec3(-flat.z, 0, flat.x);
        Vec3 o = p.getEyePosition().add(0, -0.45, 0);
        int n = 6;
        Vec3 prev = null;
        for (int i = 0; i <= n; i++) {
            double a = -1.05 + 2.1 * i / n;
            Vec3 pt = o.add(flat.scale(Math.cos(a) * r)).add(side.scale(Math.sin(a) * r)).add(0, 0.25 * Math.sin(a), 0);
            if (prev != null) Fx.line(c.level, prev, pt, col(c), Styles.of(c.look).line() & ~Styles.LINE_HELIX, 0.12f, 4);
            prev = pt;
        }
    }

    /** Couleur des effets lumineux d'un lancer : couleur du rayon (Style) ou du pouvoir, sinon blanc. */
    private static int col(Ctx c) {
        if (c.glow >= 0) return c.glow;
        return c.look != null && c.look.rgb() >= 0 ? c.look.rgb() : 0xFFFFFF;
    }

    private static void burst(Ctx c, Vec3 at, double spread, int count) {
        spawn(c, at, count, spread);
    }

    private static void trail(Ctx c, Vec3 from, Vec3 to) {
        Vec3 d = to.subtract(from);
        double len = d.length();
        if (len < 0.1) return;
        Vec3 dir = d.normalize();
        boolean jag = c.look != null && c.look.jagged;
        Styles.Preset st = Styles.of(c.look);
        Fx.line(c.level, from, to, col(c), st.line(), st.width(), 5); // le trait lumineux du style (dessiné par le client)
        double step = 0.7;
        int n = (int) Math.min(160, len / step);
        for (int i = 0; i <= n; i++) {
            Vec3 pos = from.add(dir.scale(i * step));
            if (jag && i > 0 && i < n) {
                pos = pos.add((c.level.random.nextDouble() - 0.5) * 0.45, (c.level.random.nextDouble() - 0.5) * 0.45,
                        (c.level.random.nextDouble() - 0.5) * 0.45);
            }
            Vfx.emit(c.level, particle(c), pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            if (c.look != null && i % 3 == 0) accent(c.level, c.look, pos, 1, 0.05);
        }
    }

    private static void ring(Ctx c, Vec3 center, double r) {
        Fx.wave(c.level, center, r, col(c), 12);
        int n = (int) Math.min(24, Math.max(8, r * 2));
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            Vfx.emit(c.level, particle(c), center.x + Math.cos(a) * r, center.y + 0.2, center.z + Math.sin(a) * r, 1, 0, 0.1, 0, 0.01);
        }
    }
}
