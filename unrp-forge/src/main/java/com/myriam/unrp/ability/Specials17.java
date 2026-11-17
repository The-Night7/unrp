package com.myriam.unrp.ability;

import com.myriam.unrp.ability.CovenantParse.Kind;
import com.myriam.unrp.ability.CovenantParse.Law;
import com.myriam.unrp.ability.CovenantParse.Pen;
import com.myriam.unrp.ability.CovenantParse.Term;
import com.myriam.unrp.ability.CovenantParse.Viol;
import com.myriam.unrp.data.AbilityPower;
import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.data.UnodexData;
import com.myriam.unrp.social.SocialService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Covenant (Origin VII-I), capacité originale : un Souverain dont le Tribunal (rayon) lie tous ceux qui y entrent aux lois qu'il écrit.
 * Plan et choix : docs/PLAN_COVENANT.md. Fiche : docs/fidelite/covenant.md. Pas de modèle : conçue d'après la fiche de l'auteure.
 * Ledger, Courts, Constitutions, signatures, collateral, accepted Pacts and pending remote offers are world-saved.
 */
public final class Specials17 {
    private Specials17() {}

    private static final Set<String> ALL = Set.of("cv_enact", "cv_terms", "cv_fine", "cv_foreclose", "cv_pact", "cv_endow", "cv_proxy",
            "cv_appropriate", "cv_immunity", "cv_indemnity", "cv_escrow", "cv_arbitrate", "cv_exile",
            "cv_constitution", "cv_retroactive", "cv_forfeit", "cv_class_action", "cv_sovereign_pact", "cv_due",
            "cv_jurisdiction", "cv_message", "cv_ledger", "cv_return", "cv_spend", "cv_summon");
    private static final double[] RADIUS = {10, 30, 75, 150, 500};
    private static final int[] LAW_CAP = {2, 5, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
    private static final String[] STAT = {"Power", "Defense", "Recovery", "Trick", "Speed"};
    private static final UUID SOUL_MOD = UUID.fromString("c0a1e7a2-7a11-4c0d-9e60-2d1b0a5e0c01");
    private static final String VAULT = "unrp_cv_vault";

    public static boolean handles(String s) {
        return s != null && ALL.contains(s);
    }

    // ------------------------------------------------------------------ données
    private static final class Court {
        UUID id = UUID.randomUUID();
        UUID owner;
        String dim = "minecraft:overworld";
        Vec3 center;
        double radius;
        boolean follow = true, constitutional, district;
        String name = "Court";
        long expires = Long.MAX_VALUE;
        final List<Act> history = new ArrayList<>();
        final Map<String, Long> lastRecorded = new HashMap<>();
        final List<LawRec> laws = new ArrayList<>();
        int next = 1;
        double ownerLevel = 1;
        final Set<Term> terms = EnumSet.noneOf(Term.class);
        final Set<UUID> inside = new HashSet<>();
        final Set<UUID> physical = new HashSet<>();
        final Set<UUID> exiled = new HashSet<>();
        final Map<UUID, Integer> breaches = new HashMap<>();
        final Map<UUID, Long> lastBreach = new HashMap<>();
        final Map<UUID, List<Pen>> debt = new HashMap<>();
        final Map<String, Long> gate = new HashMap<>();
        final List<UUID> parchments = new ArrayList<>();
        int tier;
    }

    private static final class LawRec {
        int id;
        String text;
        Law law;
        CovenantSpecificRules.Rule specific;
        final Map<UUID,CovenantSpecificEffects.Report> reports=new HashMap<>();
        boolean hidden;
        long activeAt;
        final Map<UUID, Long> grace = new HashMap<>();
    }

    /** Un gage : ce qui a été pris à quelqu'un. */
    private static final class Coll {
        UUID id = UUID.randomUUID();
        UUID court;
        int tier;
        int[] originalStats = new int[] {1, 1, 1, 1, 1};
        Pen pen;
        int amt = 1;
        UUID from;
        String fromName;
        String power;
        double originalLevel = 1;
        int move = -1;
        String memo = "";
        boolean permanent;
        UUID by;
        UUID holder; // qui en profite (Souverain ou allié)
        long until = Long.MAX_VALUE; // retour à la victime
        boolean higher;

        String label() {
            String s = pen.label() + (pen.kind() == Kind.STAT && amt > 1 ? " ×" + amt : "") + " from " + fromName;
            if (pen.kind() == Kind.BRANCH && power != null) s = "branch " + (move + 1) + " from " + power + " from " + fromName;
            if (pen.kind() == Kind.MEMORY && !memo.isEmpty()) s += " (« " + memo + " »)";
            return s;
        }
    }

    private static final class Sov {
        Court court;
        Court sealed;
        int selectedCourt = -1;
        Court constitution;
        final List<Court> jurisdictions = new ArrayList<>();
        final Map<UUID, Long> signatures = new HashMap<>();
        final Map<UUID, Subject> subjects = new HashMap<>();
        final Map<UUID, List<String>> handMail = new HashMap<>();
        final List<PactRec> pacts = new ArrayList<>();
        Pen tribute;
        long tributeAt;
        int escrowBranch = -1;
        final List<Coll> stock = new ArrayList<>();
        boolean bound;
        int sel, branchSelection, nextLaw = 1;
        int escrow = -1;
        long indemUntil, proxyUntil, daily;
        UUID proxy, proxyCourt, target;
        LawRec dailyLaw;
    }

    private record Offer(UUID by, Pen give, Pen take, long until, boolean sovereign, UUID reserved, String text) {}
    private record Act(UUID actor, Viol violation, long at) {}
    private record PactRec(UUID target, String text, long at) {}
    private record VaultEntry(UUID court, UUID owner, UUID from, String item, boolean released) {}
    private static final class Lease {
        UUID collateral, holder;
        String power;
        int move;
        int[] stats;
        double originalLevel = 1;
        boolean full, ownedBefore;
    }

    private enum Await { LAW, TERMS, PACT, CONSTITUTION, RETROACTIVE, FORFEIT, SOVEREIGN_PACT, TRIBUTE, MESSAGE, JURISDICTION }

    private record Pending(Await what, long until, UUID target) {}

    private static final Map<UUID, Sov> SOV = new HashMap<>();
    private static final List<Court> SEALED = new ArrayList<>();
    private static final List<Coll> APPLIED = new ArrayList<>();
    private static final Map<UUID, List<Coll>> ENDOW = new HashMap<>();
    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final Map<UUID, Offer> OFFERS = new HashMap<>();
    private static final Map<UUID, UUID> ARB = new HashMap<>();
    private static final Map<UUID, String> LAST_CHAT = new HashMap<>();
    private static final Map<UUID, Long> CD = new HashMap<>();
    private static final List<Lease> LEASES = new ArrayList<>();
    private static final List<VaultEntry> VAULT_ITEMS = new ArrayList<>();
    private static final Map<String, Long> PACT_IMMUNITY = new HashMap<>();
    private static final Map<String, Long> COLLECTION_GATE = new HashMap<>();
    private static final Set<UUID> DIRTY_PLAYERS = new HashSet<>();
    private static final Set<UUID> PROXY_DAMAGE = new HashSet<>();
    private static MinecraftServer activeServer;


    private static Sov sov(UUID id) {
        return SOV.computeIfAbsent(id, k -> new Sov());
    }

    private static void bar(ServerPlayer p, String s) {
        p.displayClientMessage(Component.literal(s), true);
    }

    private static boolean on(ServerPlayer p) {
        return AbilityEngine.isOn(p, "covenant");
    }

    private static boolean cd(ServerPlayer p, String key, int ticks, String name) {
        long now = p.serverLevel().getGameTime();
        UUID k = UUID.nameUUIDFromBytes((p.getUUID() + key).getBytes());
        long at = CD.getOrDefault(k, 0L);
        if (now < at) {
            bar(p, "§7" + name + " : " + String.format("%.1f", (at - now) / 20.0) + " s");
            return false;
        }
        CD.put(k, now + ticks);
        return true;
    }

    private static boolean pay(ServerPlayer p, int cost) {
        if (AbilityEngine.aura(p) < cost) {
            bar(p, "§cRequires " + cost + " aura.");
            return false;
        }
        AbilityEngine.spendAura(p, cost);
        return true;
    }

    // ------------------------------------------------------------------ niveau
    static double levelOf(ServerPlayer p) {
        var pr = ClaimData.get(p.server).profileOf(p.getUUID()).orElse(null);
        if (pr == null) return 1;
        if (ClaimData.Profile.CHARACTER.equals(pr.kind)) {
            var c = UnodexData.get().characters.get(pr.characterId);
            return (c != null && c.level != null ? c.level : 10)*AbilityEngine.restoredPercent(p)/100;
        }
        return pr.level*AbilityEngine.restoredPercent(p)/100;
    }

    private static double courtLevelOf(ServerPlayer player) {
        double level = levelOf(player);
        for (Lease lease : LEASES) if (lease.holder.equals(player.getUUID()) && "covenant".equals(lease.power)) level = Math.max(level, lease.originalLevel);
        return level;
    }

    static int tierOf(double level) {
        return CovenantRules.tier(level);
    }

    // ------------------------------------------------------------------ stats (lues par AbilityEngine.statDelta)
    /** Bonus/malus de stats dus à Covenant : gages pris, gages gardés, gages prêtés. */
    public static int[] statAdj(UUID id) {
        int[] d = new int[5];
        for (Coll c : APPLIED) if (c.from.equals(id) && c.pen.kind() == Kind.STAT) d[Integer.parseInt(c.pen.key())] -= c.amt;
        Sov s = SOV.get(id);
        if (s != null) for (Coll c : s.stock) if (c.pen.kind() == Kind.STAT && c.holder == null) d[Integer.parseInt(c.pen.key())] += c.amt;
        List<Coll> e = ENDOW.get(id);
        if (e != null) for (Coll c : e) if (c.pen.kind() == Kind.STAT) d[Integer.parseInt(c.pen.key())] += c.amt;
        return d;
    }

    /** La capacité (slot de barre) est-elle suspendue par une branche prise ? */
    public static boolean branchBlocked(ServerPlayer p, int slot) {
        if (APPLIED.isEmpty()) return false;
        return branchBlocked(p, AbilityEngine.basePowerId(p), slot);
    }

    public static boolean branchBlocked(ServerPlayer p, String pid, int slot) {
        for (Coll c : APPLIED) if (c.from.equals(p.getUUID()) && c.pen.kind() == Kind.BRANCH && c.move == slot && c.power != null && c.power.equals(pid)) return true;
        return false;
    }

    // ------------------------------------------------------------------ tribunal
    private static Court open(ServerPlayer p) {
        Sov s = sov(p.getUUID());
        if (s.court != null) return s.court;
        Court c = new Court();
        c.owner = p.getUUID();
        c.dim = p.serverLevel().dimension().location().toString();
        c.center = p.position();
        c.ownerLevel = courtLevelOf(p);
        c.tier = tierOf(c.ownerLevel);
        c.radius = RADIUS[c.tier];
        if (s.dailyLaw != null) { /* la loi du jour n'est pas une loi du Tribunal */ }
        s.court = c;
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1f, 0.6f);
        Fx.wave(p.serverLevel(), p.position(), Math.min(c.radius, 14), 0xF2C94C, 24);
        bar(p, "§6Court opened: " + (int) c.radius + " m. Write your laws.");
        return c;
    }

    private static void close(ServerPlayer owner, MinecraftServer server) {
        Sov s = SOV.get(owner.getUUID());
        if (s == null || s.court == null) return;
        closeCourt(server, s.court, s);
        s.court = null;
    }

    private static void closeCourt(MinecraftServer server, Court c, Sov s) {
        long now = server.overworld().getGameTime();
        for (UUID id : c.parchments) {
            for (ServerLevel lv : server.getAllLevels()) {
                var e = lv.getEntity(id);
                if (e != null) e.discard();
            }
        }
        c.parchments.clear();
        for (Coll k : APPLIED) {
            if (!k.court.equals(c.id) || k.until != Long.MAX_VALUE || k.permanent || k.tier >= 4) continue;
            k.until = now + (k.pen.kind() == Kind.SENSE ? 12000 : CovenantRules.HOUR);
        }
        for (int i = 0; i < VAULT_ITEMS.size(); i++) {
            VaultEntry item = VAULT_ITEMS.get(i);
            if (item.court.equals(c.id)) VAULT_ITEMS.set(i, new VaultEntry(item.court, item.owner, item.from, item.item, true));
        }
        returnItems(server);
        // objets confisqués : rendus à leur propriétaire s'il est là, sinon gardés dans le coffre (sauvegardé)
        returnVault(server, c.owner);
        // les prêts s'arrêtent
        for (List<Coll> list : ENDOW.values()) for (Coll collateral : new ArrayList<>(list)) {
            if (!collateral.court.equals(c.id)) continue;
            list.remove(collateral); dropLease(server, collateral.id); collateral.holder = null;
            if (!s.stock.contains(collateral)) s.stock.add(collateral);
        }
        for (UUID id : c.inside) {
            ServerPlayer v = server.getPlayerList().getPlayer(id);
            if (v != null) bar(v, "§7The Court closes.");
        }
        c.inside.clear();
        ARB.values().removeIf(o -> o.equals(c.owner));
        Sov so = SOV.get(c.owner);
        if (so != null && c.id.equals(so.proxyCourt)) so.proxy = null;
    }

    private static boolean insideOf(Court c, ServerPlayer v) {
        return v.serverLevel().dimension().location().toString().equals(c.dim) && (c.district
                ? v.position().multiply(1, 0, 1).distanceTo(c.center.multiply(1, 0, 1)) <= c.radius
                : v.position().distanceTo(c.center) <= c.radius);
    }

    private static List<Court> courtsOf(ServerPlayer v) {
        List<Court> out = new ArrayList<>();
        for (Court c : allCourts()) if (subject(c, v)) out.add(c);
        return out;
    }

    // ------------------------------------------------------------------ pénalités
    private static boolean allowed(int tier, Kind k) {
        return switch (k) {
            case STAT, MOVE -> true;
            case BRANCH, SENSE -> tier >= 2;
            case MEMORY, SOUL -> tier >= 3;
            case ABILITY, NONE -> tier >= 4;
            case LIFE -> false;
        };
    }

    private static Coll makeColl(Court c, ServerPlayer v, Pen pen, boolean pact) {
        Sov sv = sov(c.owner);
        Pen use = pen;
        if (use.kind() == Kind.NONE || !allowed(c.tier, use.kind())) return null;
        if (use.kind() == Kind.ABILITY && AbilityEngine.basePowerId(v) == null) return null;
        if (use.kind() == Kind.STAT) {
            int[] remaining = AbilityEngine.currentStats(v);
            if (remaining == null || remaining[Integer.parseInt(use.key())] <= 1) return null;
        }
        if (use.kind() == Kind.MOVE || use.kind() == Kind.SENSE || use.kind() == Kind.ABILITY) {
            Pen requested = use;
            if (APPLIED.stream().anyMatch(k -> k.from.equals(v.getUUID()) && k.pen.equals(requested))) return null;
        }
        if (use.kind() == Kind.ABILITY && branchEscrowed(v)) return null;
        if (use.kind() == Kind.SOUL && APPLIED.stream().anyMatch(k -> k.from.equals(v.getUUID()) && k.pen.kind() == Kind.SOUL)) return null;
        if (use.kind() == Kind.STAT && sov(v.getUUID()).escrow == Integer.parseInt(use.key())) return null; // Escrow
        if (use.kind() == Kind.BRANCH) {
            long held = APPLIED.stream().filter(k -> k.by.equals(c.owner) && k.pen.kind() == Kind.BRANCH).count();
            AbilityPower pw = UnodexData.get().powerFor(AbilityEngine.basePowerId(v));
            List<Integer> idx = new ArrayList<>();
            if (pw != null) {
                for (int i = 0; i < pw.moves.size() && i < AbilityEngine.MAX_MOVES; i++) {
                    if (sov(v.getUUID()).escrowBranch == i) continue;
                    if ("stance_next".equals(pw.moves.get(i).special) || "mode_next".equals(pw.moves.get(i).special)) continue;
                    boolean taken = false;
                    for (Coll k : APPLIED) if (k.from.equals(v.getUUID()) && k.pen.kind() == Kind.BRANCH && k.move == i) taken = true;
                    boolean matches = use.key().isBlank() || use.key().equals(String.valueOf(i + 1))
                            || CovenantParse.norm(pw.moves.get(i).name).contains(CovenantParse.norm(use.key()));
                    if (!taken && matches) idx.add(i);
                }
            }
            if ((c.tier < 4 && held >= 5) || idx.isEmpty()) return null;
            else {
                Coll k = base(c, v, use, pact);
                k.power = AbilityEngine.basePowerId(v);
                k.move = idx.get(v.getRandom().nextInt(idx.size()));
                return k;
            }
        }
        if (use.kind() == Kind.SENSE && use.key().isEmpty()) use = new Pen(Kind.SENSE, "sight");
        return base(c, v, use, pact);
    }

    private static Coll base(Court c, ServerPlayer v, Pen pen, boolean pact) {
        Coll k = new Coll();
        k.pen = pen;
        k.court = c.id;
        k.tier = c.tier;
        int[] sourceStats = AbilityEngine.currentStats(v);
        if (sourceStats != null) k.originalStats = sourceStats.clone();
        k.originalLevel = levelOf(v);
        k.from = v.getUUID();
        k.fromName = v.getGameProfile().getName();
        k.by = c.owner;
        k.permanent = pact && pen.kind() == Kind.SOUL && c.tier >= 3;
        k.higher = c.tier < 4 && levelOfSafe(v) > c.ownerLevel;
        if (pen.kind() == Kind.BRANCH && k.higher) k.until = v.server.overworld().getGameTime() + 12_000;
        if (pen.kind() == Kind.ABILITY) k.power = AbilityEngine.basePowerId(v);
        if (pen.kind() == Kind.MEMORY) k.memo = LAST_CHAT.getOrDefault(v.getUUID(), "");
        return k;
    }

    private static double levelOfSafe(ServerPlayer p) {
        return levelOf(p);
    }

    private static double levelOfOwner(MinecraftServer s, UUID id) {
        ServerPlayer o = s.getPlayerList().getPlayer(id);
        return o == null ? 0 : levelOf(o);
    }

    private static boolean collectUnit(Court c, ServerPlayer v, Pen pen, boolean pact, boolean announce) {
        Sov sv = sov(c.owner);
        Coll k = makeColl(c, v, pen, pact);
        if (k == null) return false;
        APPLIED.add(k);
        sv.stock.add(k);
        DIRTY_PLAYERS.add(k.from); DIRTY_PLAYERS.add(k.by);
        if (k.pen.kind() == Kind.MEMORY) {
            v.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
            AbilityEngine.spendAura(v, (int) (AbilityEngine.aura(v) * 0.4));
        }
        ServerPlayer owner = v.server.getPlayerList().getPlayer(c.owner);
        if (announce) {
            bar(v, "§6Breach. Collected: " + k.pen.label());
            if (owner != null) bar(owner, "§6Collateral received: " + k.label());
        }
        if (k.pen.kind() == Kind.ABILITY) AbilityEngine.allOff(v);
        if (k.pen.kind() == Kind.BRANCH) AbilityEngine.suspendBranch(v, k.power, k.move);
        v.serverLevel().playSound(null, v.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 0.6f);
        Vfx.glow(v.serverLevel(), Vfx.SPARKS, 0xF2C94C, v.position().add(0, 1, 0), new Vec3(0.4, 0.6, 0.4), 8, 0.05);
        return true;
    }

    /** Un manquement : chaque loi qui correspond prélève son gage, le reste de la peine devient une dette. */
    private static void breach(ServerPlayer v, Viol viol) { breach(v, viol, null); }

    private static void breach(ServerPlayer v, Viol viol, Court boundary) {
        long now = v.server.overworld().getGameTime();
        for (Court c : allCourts()) {
            if (boundary != null && c != boundary) continue;
            if (!subject(c, v) && !(viol == Viol.LEAVE && c.inside.contains(v.getUUID()))) continue;
            if (!c.constitutional && (insideOf(c, v) || (viol == Viol.LEAVE && c.inside.contains(v.getUUID())))) {
                String actionKey = v.getUUID() + ":" + viol;
                if (now - c.lastRecorded.getOrDefault(actionKey, -20L) >= 20) {
                    c.history.add(new Act(v.getUUID(), viol, now)); c.lastRecorded.put(actionKey, now);
                }
            }
            Sov so = SOV.get(c.owner);
            for (LawRec law : new ArrayList<>(c.laws)) {
                boolean self = c.owner.equals(v.getUUID());
                if (self && (so == null || !so.bound) && (so == null || so.dailyLaw == null || so.dailyLaw.id != law.id || c.tier >= 4)) continue;
                if (law.law == null || law.law.viol() != viol || now < law.activeAt || now < law.grace.getOrDefault(v.getUUID(), 0L)) continue;
                String key = c.owner + ":" + v.getUUID() + "#" + law.id;
                if (now < COLLECTION_GATE.getOrDefault(key, 0L)) continue;
                COLLECTION_GATE.put(key, now + 20);
                collectBreach(c, v, law.law.pen(), now);
            }
        }
    }

    private static void collectBreach(Court c, ServerPlayer victim, Pen penalty, long now) {
        boolean paid = collectUnit(c, victim, penalty, false, true);
        c.breaches.merge(victim.getUUID(), 1, Integer::sum);
        c.lastBreach.put(victim.getUUID(), now);
        // Debt is unpaid collateral, never an invented extra penalty on an already paid clause.
        if (!paid) c.debt.computeIfAbsent(victim.getUUID(), k -> new ArrayList<>()).add(penalty);
        revokeEndow(victim);
    }

    private static void revokeEndow(ServerPlayer v) {
        revokeEndow(v.server, v.getUUID());
    }

    private static void revokeEndow(MinecraftServer server, UUID id) {
        List<Coll> e = ENDOW.remove(id);
        if (e == null) return;
        DIRTY_PLAYERS.add(id);
        for (Coll k : e) {
            DIRTY_PLAYERS.add(k.by);
            k.holder = null;
            dropLease(server, k.id);
            Sov s = SOV.get(k.by);
            if (s != null) s.stock.add(k);
        }
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) bar(player, "§cYou broke a law: your endowment has been revoked.");
    }

    // ------------------------------------------------------------------ événements de jeu (branchés par AbilityEvents)
    public static void onAttackAttempt(ServerPlayer player) { breach(player, Viol.ATTACK); }

    public static void onCast(ServerPlayer p) {
        if (!SOV.isEmpty() || !SEALED.isEmpty()) breach(p, Viol.ABILITY);
    }

    public static void onJump(ServerPlayer p) {
        for (Coll k : APPLIED) {
            if (k.from.equals(p.getUUID()) && k.pen.kind() == Kind.MOVE && k.pen.key().equals("jump")) {
                Vec3 m = p.getDeltaMovement();
                p.setDeltaMovement(m.x, 0, m.z);
                p.hurtMarked = true;
                break;
            }
        }
        breach(p, Viol.JUMP);
    }

    public static void onBreak(ServerPlayer p) {
        breach(p, Viol.BREAK);
    }

    public static void onPlace(ServerPlayer p) {
        breach(p, Viol.PLACE);
    }

    /** Chat : capture des lois, termes et pactes ; sinon, parler peut être un manquement. */
    public static void onChat(ServerChatEvent e) {
        ServerPlayer p = e.getPlayer();
        String text = e.getRawText();
        UUID id = p.getUUID();
        long now = p.serverLevel().getGameTime();
        if (controlChat(p, text)) { e.setCanceled(true); return; }
        Offer offer = OFFERS.get(id);
        if (offer != null) {
            String n = CovenantParse.norm(text).trim();
            if (now > offer.until) OFFERS.remove(id);
            else if (n.startsWith("accept") || n.equals("oui") || n.equals("yes")) {
                e.setCanceled(true);
                OFFERS.remove(id);
                acceptPact(p, offer);
                return;
            } else if (n.startsWith("refus") || n.equals("non") || n.equals("no") || n.startsWith("decline")) {
                e.setCanceled(true);
                OFFERS.remove(id);
                if (offer.sovereign) PACT_IMMUNITY.put(offer.by + ":" + id, now + CovenantRules.HOUR);
                saveState(p.server);
                bar(p, offer.sovereign ? "§7Offer refused: immune to their Pacts for one hour." : "§7Offer refused.");
                return;
            }
        }
        Pending pd = PENDING.get(id);
        if (pd != null) {
            PENDING.remove(id);
            if (now <= pd.until) {
                e.setCanceled(true);
                p.server.execute(() -> finish(p, pd, text));
                return;
            }
        }
        LAST_CHAT.put(id, text.length() > 60 ? text.substring(0, 60) : text);
        breach(p, Viol.SPEAK);
    }

    public static void cancelWriting(ServerPlayer player) { PENDING.remove(player.getUUID()); }

    /** Commands and the bar prompt share the same authoritative enforcement path. */
    public static boolean submitWriting(ServerPlayer player, String action, String text) {
        if (!on(player) || AbilityEngine.disabled(player)) {
            player.sendSystemMessage(Component.literal("§7Activate Covenant first using R or the power toggle on its bar."));
            return false;
        }
        Await what = switch (action) {
            case "law" -> Await.LAW;
            case "pact" -> Await.PACT;
            case "terms" -> Await.TERMS;
            case "constitution" -> Await.CONSTITUTION;
            case "retroactive" -> Await.RETROACTIVE;
            case "forfeit" -> Await.FORFEIT;
            case "tribute" -> Await.TRIBUTE;
            case "jurisdiction" -> Await.JURISDICTION;
            case "message" -> Await.MESSAGE;
            case "sovereign_pact" -> Await.SOVEREIGN_PACT;
            default -> null;
        };
        if (what == null || text.isBlank() || text.length() > 512) return false;
        if (what == Await.PACT && courtLevelOf(player) < CovenantRules.MID) return false;
        if (what != Await.LAW && what != Await.TERMS && what != Await.PACT && levelOf(player) < CovenantRules.GOD) {
            player.sendSystemMessage(Component.literal("§7This action requires God tier (6.0).")); return false;
        }
        open(player); // Do not depend on the next server tick to establish the personal Court.
        onCast(player);
        if (!on(player) || AbilityEngine.disabled(player)) return false;
        Pending previous = PENDING.remove(player.getUUID());
        if (what == Await.PACT && (previous == null || previous.what != Await.PACT || previous.until < player.server.overworld().getGameTime())) return false;
        finish(player, new Pending(what, player.server.overworld().getGameTime(), what == Await.PACT ? previous.target : null), text);
        return true;
    }

    public static boolean openLawEditor(ServerPlayer player) {
        if(!on(player)){bar(player,"§cActivate Covenant before enacting a law.");return false;}
        writingPrompt(player,"law");return true;
    }
    private static void writingPrompt(ServerPlayer player, String action) {
        com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
        payload.addProperty("writing", "/unrp covenant " + action + " ");
        payload.addProperty("level",courtLevelOf(player));
        com.google.gson.JsonArray players = new com.google.gson.JsonArray();
        for (ServerPlayer person : player.server.getPlayerList().getPlayers()) if (person != player) players.add(person.getGameProfile().getName());
        payload.add("players", players);
        com.google.gson.JsonArray territories = new com.google.gson.JsonArray(); territories.add("here");
        for (var turf : com.myriam.unrp.social.SocialData.get(player.server).store.turfs) if (turf.name != null) territories.add(turf.name);
        payload.add("territories", territories);
        com.myriam.unrp.net.Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), new com.myriam.unrp.net.CovenantPacket(payload.toString()));
    }

    private static void finish(ServerPlayer p, Pending pending, String text) {
        Sov state = sov(p.getUUID());
        Court court = editingCourt(state);
        if (court == null) { bar(p, "§7The Court is closed."); return; }
        long now = p.server.overworld().getGameTime();
        if(pending.what==Await.LAW) {
            var specific=CovenantSpecificRules.parse(text);
            if(specific!=null) {
                if(court.tier<2){bar(p,"§7Specific Court rules unlock at Elite tier (3.5).");return;}
                if(court.laws.size()>=LAW_CAP[court.tier]){bar(p,"§7Law limit reached.");return;}
                if(court.laws.stream().anyMatch(law->specific.equals(law.specific))){bar(p,"§7This specific rule is already enacted.");return;}
                if(!pay(p,10))return;
                var record=new LawRec();record.id=state.nextLaw++;record.text=specific.text();record.specific=specific;record.activeAt=now;
                court.laws.add(record);announce(p,court,"§6Law #"+record.id+" : §f"+record.text);rebuildParchments(p,court);saveState(p.server);return;
            }
        }
        switch (pending.what) {
            case LAW, RETROACTIVE, FORFEIT -> {
                Law law = CovenantParse.law(text);
                if (law == null || !allowed(court.tier, law.pen().kind())) {
                    bar(p, "§7Unrecognized law or penalty unavailable at this level. Example: No running. Penalty: Speed"); return;
                }
                if (pending.what == Await.FORFEIT && law.pen().kind() != Kind.ABILITY) {
                    bar(p, "§7Total Forfeit: use 'Penalty: whole ability'."); return;
                }
                if (court.laws.size() >= LAW_CAP[court.tier]) { bar(p, "§7Law limit reached."); return; }
                if (!pay(p, 10)) return;
                LawRec record = new LawRec(); record.id = state.nextLaw++; record.text = text.length() > 512 ? text.substring(0, 512) : text;
                record.law = law; record.activeAt = now;
                if (court.tier == 0) for (ServerPlayer person : p.server.getPlayerList().getPlayers()) {
                    if (insideOf(court, person)) record.grace.put(person.getUUID(), now + 60);
                }
                court.laws.add(record);
                announce(p, court, "§6Law #" + record.id + " : §f" + record.text);
                rebuildParchments(p, court);
                if (pending.what == Await.RETROACTIVE) {
                    // Only actions recorded while physically inside this Court qualify.
                    for (Act act : new ArrayList<>(court.history)) {
                        if (!CovenantRules.retroactiveEligible(now, act.at, true) || act.violation != law.viol()) continue;
                        ServerPlayer person = p.server.getPlayerList().getPlayer(act.actor);
                        if (act.actor.equals(court.owner) && !state.bound) continue;
                        if (person != null) collectBreach(court, person, law.pen(), now);
                        else {
                            boolean paid = collectOffline(p.server, court, act.actor, law.pen());
                            court.breaches.merge(act.actor, 1, Integer::sum);
                            List<Pen> debt = court.debt.computeIfAbsent(act.actor, key -> new ArrayList<>());
                            if (!paid) debt.add(law.pen());
                            revokeEndow(p.server, act.actor);
                        }
                    }
                }
            }
            case TERMS -> {
                Set<Term> terms = CovenantParse.terms(text);
                if (terms.isEmpty()) { bar(p, "§7Terms: weapons, armor, elytra, pearls, inventory."); return; }
                if (!pay(p, 8)) return;
                court.terms.clear(); court.terms.addAll(terms);
                announce(p, court, "§6Terms of Entry: " + text);
            }
            case PACT, SOVEREIGN_PACT -> offerPact(p, pending, text, court);
            case CONSTITUTION -> {
                Court constitution = constitution(p.getUUID(), court);
                if (constitution.laws.size() >= 5) { bar(p, "§7Maximum five Constitutions: use cv repeal <id> to repeal one."); return; }
                LawRec record;
                String number = text.trim().replace("#", "");
                if (number.matches("[0-9]+")) {
                    int id = Integer.parseInt(number);
                    LawRec existing = court.laws.stream().filter(l -> l.id == id).findFirst().orElse(null);
                    if (existing == null) { bar(p, "§7Unknown law."); return; }
                    if(existing.specific!=null){bar(p,"§7Specific rules apply inside a Court and cannot become Constitutions.");return;}
                    record = copyLaw(existing); court.laws.remove(existing); record.hidden = false;
                } else {
                    Law law = CovenantParse.law(text);
                    if (law == null || !allowed(4, law.pen().kind())) { bar(p, "§7Unrecognized Constitution."); return; }
                    record = new LawRec(); record.id = state.nextLaw++; record.text = text; record.law = law; record.activeAt = now;
                }
                constitution.laws.add(record);
                rebuildParchments(p, court);
                announce(p, court, "§6Constitution engraved #" + record.id + " : " + text);
            }
            case TRIBUTE -> {
                Pen penalty = CovenantParse.pen(text);
                if (penalty == null || !allowed(4, penalty.kind()) || penalty.kind() == Kind.NONE) { bar(p, "§7Unrecognized tribute."); return; }
                state.tribute = penalty;
                constitution(p.getUUID(), court);
                if (state.tributeAt == 0) state.tributeAt = now; // choosing again does not bypass the daily schedule
                bar(p, "§6Sovereign's Due: " + penalty.label() + " once per day.");
            }
            case MESSAGE -> {
                int separator = text.indexOf('|');
                if (separator < 1) { bar(p, "§7Write: PlayerName | your message"); return; }
                String name = text.substring(0, separator).trim();
                ServerPlayer target = p.server.getPlayerList().getPlayerByName(name);
                Subject known = target == null ? known(name) : remember(court, target);
                if (known == null || !signed(p.getUUID(), known.id, now)) { bar(p, "§7This person is not a marked signer."); return; }
                String message = p.getGameProfile().getName() + " : " + text.substring(separator + 1).trim();
                if (target != null) {
                    target.sendSystemMessage(Component.literal("§6On your hand: §f" + message));
                    showPactParchment(target, "Covenant", message);
                } else state.handMail.computeIfAbsent(known.id, id -> new ArrayList<>()).add(message);
            }
            case JURISDICTION -> jurisdiction(p, court, text);
        }
        saveState(p.server);
    }

    private static void showPactParchment(ServerPlayer target, String from, String text) {
        com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
        payload.addProperty("letter", from + " : " + text);
        com.myriam.unrp.net.Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> target), new com.myriam.unrp.net.CovenantPacket(payload.toString()));
    }

    private static Coll findStock(Sov s, Pen want) {
        for (Coll k : s.stock) if (k.holder == null && k.pen.kind() == want.kind() && (want.key().isEmpty() || k.pen.key().equals(want.key()))) return k;
        return null;
    }

    private static void acceptPact(ServerPlayer target, Offer o) {
        ServerPlayer by = target.server.getPlayerList().getPlayer(o.by);
        Sov s = SOV.get(o.by);
        if (s == null || (!o.sovereign && (by == null || s.court == null))) {
            bar(target, "§7The Sovereign is no longer present.");
            return;
        }
        Court c = s.court != null ? s.court : s.constitution;
        if (c == null) return;
        if (!allowed(c.tier, o.take.kind()) && o.take.kind() != Kind.LIFE) return;
        if (o.reserved != null && s.stock.stream().noneMatch(k -> k.id.equals(o.reserved))) {
            bar(target, "§7The reserved consideration is no longer available: nothing is collected."); return;
        }
        // Validate the receiving penalty before committing either side.
        Pen take = o.take.kind() == Kind.LIFE ? new Pen(Kind.SOUL, "") : o.take;
        Coll incoming = take.kind() == Kind.NONE ? null : makeColl(c, target, take, true);
        if (take.kind() != Kind.NONE && incoming == null) { bar(target, "§7This Pact cannot be fulfilled: neither party pays."); return; }
        // ce que le Souverain donne
        if (o.give.kind() == Kind.LIFE) {
            target.setHealth(target.getMaxHealth());
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
            if (by != null) by.hurt(by.damageSources().magic(), Math.max(0, Math.min(by.getHealth() - 1, by.getMaxHealth() * 0.25f)));
        } else {
            Coll k = o.reserved == null ? null : s.stock.stream().filter(x -> x.id.equals(o.reserved)).findFirst().orElse(null);
            if (k != null) {
                if ((k.pen.kind() == Kind.BRANCH || k.pen.kind() == Kind.ABILITY) && !lendBranch(target, k)) return;
                s.stock.remove(k); k.holder = target.getUUID();
                if (k.from.equals(target.getUUID())) { returnCollateral(target.server, k); k = null; }
                // Accepted consideration is a transfer, not a revocable Endowment loan.
                if (k != null) { k.by = target.getUUID(); k.holder = null; sov(target.getUUID()).stock.add(k); }
            } else if (c.tier < 4) return;
        }
        if (incoming != null) {
            APPLIED.add(incoming); s.stock.add(incoming);
            if (incoming.pen.kind() == Kind.ABILITY) AbilityEngine.allOff(target);
        }
        s.pacts.add(new PactRec(target.getUUID(), o.text, target.server.overworld().getGameTime()));
        if (c.tier >= 4) s.signatures.put(target.getUUID(), target.server.overworld().getGameTime() + CovenantRules.WEEK);
        saveState(target.server);
        target.sendSystemMessage(Component.literal("§6Pact accepted. Both parties are bound."));
        if (by != null) bar(by, "§6Pact accepted with " + target.getGameProfile().getName() + ".");
        target.serverLevel().playSound(null, target.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 0.8f);
    }

    private static void announce(ServerPlayer owner, Court c, String msg) {
        owner.sendSystemMessage(Component.literal(msg));
        for (UUID id : c.inside) {
            ServerPlayer v = owner.server.getPlayerList().getPlayer(id);
            if (v == null) continue;
            LawRec last = c.laws.isEmpty() ? null : c.laws.get(c.laws.size() - 1);
            if (last != null && last.hidden) continue;
            v.sendSystemMessage(Component.literal(msg));
        }
        owner.serverLevel().playSound(null, owner.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.7f, 1.2f);
    }

    // ------------------------------------------------------------------ parchemins dans l'air
    private static void rebuildParchments(ServerPlayer owner, Court c) {
        ServerLevel lv = owner.server.getLevel(courtDimension(c));
        if (lv == null) return;
        Vec3 anchor = c.follow ? owner.position() : c.center;
        for (UUID id : c.parchments) {
            var e = lv.getEntity(id);
            if (e != null) e.discard();
        }
        c.parchments.clear();
        // Public law cards are now rendered from the server-owned visual packet.
        // Remove loaded legacy name-tag stands from an older version of this Court.
        for (ArmorStand stand : lv.getEntitiesOfClass(ArmorStand.class,
                new net.minecraft.world.phys.AABB(anchor.subtract(8, 2, 8), anchor.add(8, 12, 8)), e -> e.getTags().contains("unrp.cv_law"))) stand.discard();

    }

    // ------------------------------------------------------------------ coffre d'objets confisqués (sauvegardé)
    private static void vaultAdd(ServerPlayer owner, UUID from, ItemStack it) {
        CompoundTag root = com.myriam.unrp.item.UnItems.persisted(owner);
        ListTag l = root.getList(VAULT, Tag.TAG_COMPOUND);
        CompoundTag e = new CompoundTag();
        e.putUUID("from", from);
        e.put("item", it.save(new CompoundTag()));
        l.add(e);
        root.put(VAULT, l);
    }

    private static void returnVault(MinecraftServer server, UUID ownerId) {
        ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
        if (owner == null) return;
        CompoundTag root = com.myriam.unrp.item.UnItems.persisted(owner);
        ListTag l = root.getList(VAULT, Tag.TAG_COMPOUND);
        ListTag keep = new ListTag();
        for (Tag t : l) {
            CompoundTag e = (CompoundTag) t;
            ItemStack it = ItemStack.of(e.getCompound("item"));
            ServerPlayer back = server.getPlayerList().getPlayer(e.getUUID("from"));
            ServerPlayer to = back != null ? back : owner;
            if (back == null) {
                keep.add(e); // le propriétaire est absent : on garde, rien n'est perdu
                continue;
            }
            if (!to.getInventory().add(it)) to.drop(it, false);
        }
        root.put(VAULT, keep);
        if (!keep.isEmpty()) bar(owner, "§7Confiscated items are held for their offline owners and returned when they reconnect.");
    }

    /** À la connexion d'un joueur : rend les objets qu'un Souverain avait gardés pour lui. */
    public static void onLogin(ServerPlayer joiner) {
        start(joiner.server);
        returnItems(joiner.server);
        for (Sov state : SOV.values()) {
            List<String> mail = state.handMail.remove(joiner.getUUID());
            if (mail != null) for (String message : mail) {
                joiner.sendSystemMessage(Component.literal("§6On your hand: §f" + message));
                showPactParchment(joiner, "Covenant", message);
            }
        }
        Offer pendingOffer = OFFERS.get(joiner.getUUID());
        if (pendingOffer != null && pendingOffer.until > joiner.server.overworld().getGameTime()) deliverOffer(joiner, pendingOffer);
        for (Lease lease : LEASES) if (lease.holder.equals(joiner.getUUID())) AbilityEngine.addCopy(joiner, lease.power);
        for (ServerPlayer o : joiner.server.getPlayerList().getPlayers()) {
            CompoundTag root = com.myriam.unrp.item.UnItems.persisted(o);
            if (!root.contains(VAULT)) continue;
            ListTag l = root.getList(VAULT, Tag.TAG_COMPOUND), keep = new ListTag();
            for (Tag t : l) {
                CompoundTag e = (CompoundTag) t;
                if (e.getUUID("from").equals(joiner.getUUID()) && !(SOV.containsKey(o.getUUID()) && SOV.get(o.getUUID()).court != null)) {
                    ItemStack it = ItemStack.of(e.getCompound("item"));
                    if (!joiner.getInventory().add(it)) joiner.drop(it, false);
                } else keep.add(e);
            }
            root.put(VAULT, keep);
        }
        // un Souverain qui se reconnecte reprend ses objets non rendus
        CompoundTag mine = com.myriam.unrp.item.UnItems.persisted(joiner);
        if (mine.contains(VAULT) && !mine.getList(VAULT, Tag.TAG_COMPOUND).isEmpty() && joiner.server.getPlayerList().getPlayers().size() > 0) {
            returnVault(joiner.server, joiner.getUUID());
        }
    }

    private static boolean isWeapon(ItemStack s) {
        var i = s.getItem();
        return i instanceof SwordItem || i instanceof net.minecraft.world.item.AxeItem || i instanceof net.minecraft.world.item.BowItem
                || i instanceof net.minecraft.world.item.CrossbowItem || i instanceof net.minecraft.world.item.TridentItem
                || (s.hasTag() && s.getTag().getBoolean("unrp_ad"));
    }

    private static void collectTerms(Court c, ServerPlayer v) {
        if (c.terms.isEmpty()) return;
        var inv = v.getInventory();
        int n = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            boolean take = c.terms.contains(Term.ALL)
                    || (c.terms.contains(Term.WEAPONS) && isWeapon(s))
                    || (c.terms.contains(Term.ARMOR) && s.getItem() instanceof ArmorItem)
                    || (c.terms.contains(Term.ELYTRA) && s.is(Items.ELYTRA))
                    || (c.terms.contains(Term.PEARLS) && s.is(Items.ENDER_PEARL));
            if (take) {
                VAULT_ITEMS.add(new VaultEntry(c.id, c.owner, v.getUUID(), s.save(new CompoundTag()).toString(), false));
                inv.setItem(i, ItemStack.EMPTY);
                n++;
            }
        }
        if (n > 0) {
            bar(v, "§6Terms of Entry: " + n + " item(s) confiscated, returned when the Court closes.");
            collectGage(c, v);
        }
    }

    private static void collectGage(Court c, ServerPlayer v) { /* les termes prennent des objets, pas de gage de stat */ }

    // ------------------------------------------------------------------ capacités
    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m) {
        boolean sneak = p.isShiftKeyDown();
        double level = moveLevel(p, "covenant", power.moves.indexOf(m));
        if (level < CovenantRules.minimum(m.special)) {
            bar(p, "§7Level " + CovenantRules.minimum(m.special) + " required."); return false;
        }
        Sov s = sov(p.getUUID());
        if (s.court == null && on(p)) open(p);
        Court c = editingCourt(s);
        long now = p.serverLevel().getGameTime();
        if (c == null && !m.special.equals("cv_immunity")) {
            bar(p, "§7The Court is closed: activate your power (R).");
            return false;
        }
        switch (m.special) {
            case "cv_enact" -> {
                if (sneak) { // abroger la dernière loi
                    if (c.laws.isEmpty()) return false;
                    LawRec r = c.laws.remove(c.laws.size() - 1);
                    announce(p, c, "§6Law #" + r.id + " repealed.");
                    rebuildParchments(p, c);
                    return false;
                }
                PENDING.put(p.getUUID(), new Pending(Await.LAW, now + 600, null));
                p.sendSystemMessage(Component.literal("§6Write a law: §fNo one may run. Penalty: their Speed"));
                writingPrompt(p, "law");
                return false;
            }
            case "cv_terms" -> {
                if (sneak) {
                    if (c.tier < 3) {
                        bar(p, "§7Sealing a Court requires High tier.");
                        return false;
                    }
                    if (s.sealed != null) { closeCourt(p.server, s.sealed, s); SEALED.remove(s.sealed); }
                    Court k = new Court();
                    k.owner = p.getUUID();
                    k.dim = c.dim;
                    k.center = p.position();
                    k.radius = c.radius;
                    k.follow = false;
                    k.tier = c.tier; k.ownerLevel = c.ownerLevel;
                    for (LawRec law : c.laws) k.laws.add(copyLaw(law));
                    k.terms.addAll(c.terms);
                    k.next = c.next;
                    s.sealed = k;
                    SEALED.add(k);
                    Fx.wave(p.serverLevel(), p.position(), 20, 0xF2C94C, 40);
                    announce(p, c, "§6Court sealed here. Its laws apply even while you are absent (saved).");
                    return true;
                }
                PENDING.put(p.getUUID(), new Pending(Await.TERMS, now + 600, null));
                p.sendSystemMessage(Component.literal("§6Write the Terms of Entry: §fWhoever enters gives up their weapons"));
                writingPrompt(p, "terms");
                return false;
            }
            case "cv_fine" -> {
                if (sneak) {
                    for (LawRec l : c.laws) if (l.hidden) { l.hidden = false; announce(p, c, "§6Fine Print revealed: §f" + l.text); rebuildParchments(p, c); return true; }
                    bar(p, "§7No hidden law.");
                    return false;
                }
                if (c.laws.stream().anyMatch(l -> l.hidden)) { bar(p, "§7Only one hidden law at a time; reveal it first."); return false; }
                for (int i = c.laws.size() - 1; i >= 0; i--) {
                    LawRec l = c.laws.get(i);
                    if (!l.hidden) {
                        l.hidden = true;
                        rebuildParchments(p, c);
                        bar(p, "§6Law #" + l.id + " hidden: it applies without being displayed.");
                        return true;
                    }
                }
                return false;
            }
            case "cv_foreclose", "cv_exile" -> {
                ServerPlayer t = target(p, c);
                if (t == null || !subject(c, t)) {
                    bar(p, "§7Target a signer.");
                    return false;
                }
                if (sneak || m.special.equals("cv_exile")) { // Exile
                    if (c.breaches.getOrDefault(t.getUUID(), 0) < 3) {
                        bar(p, "§7Exile requires three breaches (" + c.breaches.getOrDefault(t.getUUID(), 0) + ").");
                        return false;
                    }
                    if (!pay(p, 25)) return false;
                    c.exiled.add(t.getUUID());
                    c.inside.remove(t.getUUID());
                    if (insideOf(c, t)) expel(c, t);
                    announce(p, c, "§6" + t.getGameProfile().getName() + " is exiled from the Court.");
                    return true;
                }
                List<Pen> d = c.debt.remove(t.getUUID());
                if (d == null || d.isEmpty()) {
                    bar(p, "§7No outstanding debt to foreclose.");
                    return false;
                }
                if (!pay(p, 20)) { c.debt.put(t.getUUID(), d); return false; }
                int total = (int) Math.ceil(d.size() * 1.25);
                List<Pen> unpaid = new ArrayList<>();
                int taken = 0;
                for (int i = 0; i < total; i++) {
                    Pen penalty = d.get(i % d.size());
                    if (collectUnit(c, t, penalty, false, i == 0)) taken++; else unpaid.add(penalty);
                }
                if (!unpaid.isEmpty()) c.debt.put(t.getUUID(), unpaid);
                bar(p, "§6Foreclosure: " + taken + " unit(s), including interest. Unpaid: " + unpaid.size());
                return true;
            }
            case "cv_pact" -> {
                if (sneak) { // convoquer une âme
                    if (levelOf(p) < CovenantRules.HIGH) { bar(p, "§7The soul passive unlocks at level 5."); return false; }
                    Coll soul = selectedStock(s, Kind.SOUL);
                    if (soul == null) {
                        bar(p, "§7You hold no souls.");
                        return false;
                    }
                    ServerPlayer v = p.server.getPlayerList().getPlayer(soul.from);
                    if (v == null) {
                        bar(p, "§7This soul is outside the world.");
                        return false;
                    }
                    if (!pay(p, 15)) return false;
                    v.teleportTo(p.serverLevel(), p.getX(), p.getY(), p.getZ(), v.getYRot(), v.getXRot());
                    bar(v, "§6Your soul has been summoned.");
                    return true;
                }
                ServerPlayer t = target(p, c);
                if (t == null || t == p) {
                    bar(p, "§7Target the person receiving your Pact offer.");
                    return false;
                }
                PENDING.put(p.getUUID(), new Pending(Await.PACT, now + 600, t.getUUID()));
                writingPrompt(p, "pact");
                return false;
            }
            case "cv_endow" -> {
                List<Coll> free = new ArrayList<>();
                for (Coll k : s.stock) if (k.holder == null && (k.pen.kind() == Kind.STAT || k.pen.kind() == Kind.BRANCH || k.pen.kind() == Kind.SOUL)) free.add(k);
                if (free.isEmpty()) {
                    bar(p, "§7Your collateral stock is empty.");
                    return false;
                }
                Coll chosen = s.stock.get(Math.floorMod(s.sel, s.stock.size()));
                int position = free.indexOf(chosen);
                if (position < 0) position = 0;
                if (sneak) {
                    Coll next = free.get((position + 1) % free.size());
                    s.sel = s.stock.indexOf(next);
                    bar(p, "§6Selected collateral: §f" + next.label()); return false;
                }
                ServerPlayer t = target(p, c);
                if (t == null || t == p) {
                    bar(p, "§7Target the ally to endow.");
                    return false;
                }
                if (!pay(p, 8)) return false;
                Coll k = free.get(position);
                DIRTY_PLAYERS.add(p.getUUID()); DIRTY_PLAYERS.add(t.getUUID());
                s.stock.remove(k);
                k.holder = t.getUUID();
                ENDOW.computeIfAbsent(t.getUUID(), x -> new ArrayList<>()).add(k);
                if ((k.pen.kind() == Kind.BRANCH || k.pen.kind() == Kind.ABILITY) && !lendBranch(t, k)) {
                    ENDOW.get(t.getUUID()).remove(k); k.holder = null; s.stock.add(k); return false;
                }
                bar(t, "§6Endowment: " + k.pen.label() + " (until the Court closes or your first breach).");
                bar(p, "§6Endowment granted to " + t.getGameProfile().getName() + ".");
                return true;
            }
            case "cv_proxy" -> {
                if (sneak) {
                    s.proxy = null;
                    bar(p, "§7Proxy released.");
                    return false;
                }
                ServerPlayer t = target(p, c);
                if (t == null || !subject(c, t)) {
                    bar(p, "§7Target a signer.");
                    return false;
                }
                if (!pay(p, 25)) return false;
                s.proxy = t.getUUID(); s.proxyCourt = c.id;
                s.proxyUntil = Long.MAX_VALUE;
                bar(p, "§6Proxy: " + t.getGameProfile().getName() + " takes your wounds until the Court closes.");
                bar(t, "§cYou are bound to a Sovereign: their wounds become yours.");
                return true;
            }
            case "cv_appropriate" -> {
                List<Coll> branches = s.stock.stream().filter(x -> (x.holder == null || (x.pen.kind() == Kind.ABILITY && p.getUUID().equals(x.holder))) && (x.pen.kind() == Kind.BRANCH || x.pen.kind() == Kind.ABILITY)).toList();
                if (branches.isEmpty()) { bar(p, "§7No available branches."); return false; }
                Coll selected = s.stock.isEmpty() ? null : s.stock.get(Math.floorMod(s.sel, s.stock.size()));
                Coll collateral = selected != null && branches.contains(selected) ? selected : branches.get(0);
                if (!lendBranch(p, collateral, true)) return false;
                collateral.holder = p.getUUID();
                bar(p, "§6Appropriation : " + collateral.label() + ". Only this branch is available, at its original strength.");
                saveState(p.server);
                return true;
            }
            case "cv_immunity" -> {
                s.bound = !s.bound;
                if (s.bound && c != null && c.tier >= 4) s.signatures.put(p.getUUID(), now + CovenantRules.WEEK);
                bar(p, s.bound ? "§cYou bind yourself to your own laws." : "§6Sovereign Immunity: your laws do not bind you.");
                return false;
            }
            case "cv_indemnity" -> {
                if (s.indemUntil == Long.MAX_VALUE && level >= 5) { s.indemUntil = 0; bar(p, "§7Indemnity lifted."); return false; }
                if (!pay(p, 20)) return false;
                s.indemUntil = CovenantRules.indemnityExpiry(level, now);
                p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1f, 1.4f);
                Vfx.glow(p.serverLevel(), Vfx.SPARKS, 0xF2C94C, p.position().add(0,1,0), new Vec3(.4,.5,.4), 5,.02);
                bar(p, level >= 5 ? "§6Permanent Indemnity while your personal Court is active." : "§6Indemnity: wounds voided for 6 s.");
                return true;
            }
            case "cv_escrow" -> {
                if (sneak) {
                    AbilityPower own = UnodexData.get().powerFor(AbilityEngine.basePowerId(p));
                    s.escrow = -1;
                    List<Integer> choices = new ArrayList<>();
                    if (own != null) for (int i = 0; i < own.moves.size(); i++) {
                        var move = own.moves.get(i);
                        if (!"stance_next".equals(move.special) && !"mode_next".equals(move.special) && levelOf(p) >= move.minLevel
                                && !Unlocks.locked(Unlocks.mask(p.server, p), own, i, Unlocks.mastered(p.server, p))) choices.add(i);
                    }
                    if (choices.isEmpty()) { s.escrowBranch = -1; return false; }
                    s.escrowBranch = choices.get((choices.indexOf(s.escrowBranch) + 1) % choices.size());
                    bar(p, "§6Escrowed branch: " + own.moves.get(s.escrowBranch).name); return false;
                }
                s.escrowBranch = -1;
                s.escrow = s.escrow >= 4 ? -1 : s.escrow + 1;
                bar(p, s.escrow < 0 ? "§7No stat in escrow." : "§6In escrow: " + STAT[s.escrow] + " (protected from penalties).");
                return false;
            }
            case "cv_arbitrate" -> {
                ServerPlayer t = target(p, c);
                if (t == null || c.debt.getOrDefault(t.getUUID(), List.of()).isEmpty()) {
                    bar(p, "§7Target someone with outstanding debt.");
                    return false;
                }
                if (branchEscrowed(t)) { bar(p, "§7An escrowed branch prevents whole-ability suspension."); return false; }
                if (!pay(p, 20)) return false;
                ARB.put(t.getUUID(), p.getUUID());
                AbilityEngine.allOff(t);
                bar(p, "§6Arbitration: their power is suspended until their debt is paid.");
                bar(t, "§cArbitration: your power is suspended until your debt is paid.");
                return true;
            }
            case "cv_constitution", "cv_retroactive", "cv_forfeit", "cv_sovereign_pact", "cv_due", "cv_jurisdiction", "cv_message" -> {
                Await action = switch (m.special) {
                    case "cv_constitution" -> Await.CONSTITUTION;
                    case "cv_retroactive" -> Await.RETROACTIVE;
                    case "cv_forfeit" -> Await.FORFEIT;
                    case "cv_sovereign_pact" -> Await.SOVEREIGN_PACT;
                    case "cv_due" -> Await.TRIBUTE;
                    case "cv_jurisdiction" -> Await.JURISDICTION;
                    default -> Await.MESSAGE;
                };
                PENDING.put(p.getUUID(), new Pending(action, now + 1200, null));
                String help = switch (action) {
                    case SOVEREIGN_PACT -> "PlayerName | nothing for your branch";
                    case MESSAGE -> "PlayerName | your message";
                    case TRIBUTE -> "Speed / Recovery / memory / ...";
                    case JURISDICTION -> "territory name, or here";
                    case FORFEIT -> "No attacking. Penalty: whole ability";
                    default -> "No running. Penalty: Speed";
                };
                p.sendSystemMessage(Component.literal("§6Write in chat: §f" + help));
                writingPrompt(p, switch (action) {
                    case CONSTITUTION -> "constitution"; case RETROACTIVE -> "retroactive"; case FORFEIT -> "forfeit";
                    case SOVEREIGN_PACT -> "sovereign_pact"; case TRIBUTE -> "tribute"; case JURISDICTION -> "jurisdiction"; default -> "message";
                });
                return false;
            }
            case "cv_class_action" -> {
                int count = 0;
                Set<UUID> charged = new HashSet<>();
                for (Court ownCourt : allCourts()) {
                    if (!ownCourt.owner.equals(p.getUUID())) continue;
                    for (var entry : new ArrayList<>(ownCourt.debt.entrySet())) {
                        if (entry.getValue().isEmpty() || charged.contains(entry.getKey())) continue;
                        ServerPlayer debtor = p.server.getPlayerList().getPlayer(entry.getKey());
                        boolean paid = debtor != null ? collectUnit(ownCourt, debtor, entry.getValue().get(0), false, true)
                                : collectOffline(p.server, ownCourt, entry.getKey(), entry.getValue().get(0));
                        if (paid) {
                            entry.getValue().remove(0); charged.add(entry.getKey()); count++;
                        }
                    }
                }
                bar(p, "§6Class Action : " + count + " penalties collected simultaneously.");
                saveState(p.server); return count > 0;
            }
            case "cv_ledger" -> { ledger(p); return false; }
            case "cv_return", "cv_spend" -> {
                if (s.stock.isEmpty()) { bar(p, "§7No collateral available."); return false; }
                Coll collateral = s.stock.get(Math.floorMod(s.sel, s.stock.size()));
                if (m.special.equals("cv_return")) returnCollateral(p.server, collateral);
                else {
                    if (OFFERS.values().stream().anyMatch(o -> collateral.id.equals(o.reserved))) { bar(p, "§7This collateral is reserved for a Pact."); return false; }
                    // Spending consumes the held benefit, while the victim's return schedule remains intact.
                    returnCollateral(p.server, collateral);
                    var profile = ClaimData.get(p.server).profileOf(p.getUUID()).orElse(null);
                    if (profile != null) { profile.aura += 20; ClaimData.get(p.server).setDirty(); }
                    bar(p, "§6Collateral spent: +20 aura.");
                }
                saveState(p.server); return true;
            }
            case "cv_summon" -> {
                List<Coll> souls = s.stock.stream().filter(k -> k.pen.kind() == Kind.SOUL).toList();
                if (souls.isEmpty()) return false;
                Coll soul = selectedStock(s, Kind.SOUL);
                ServerPlayer target = p.server.getPlayerList().getPlayer(soul.from);
                if (target == null) { bar(p, "§7This soul's owner is offline."); return false; }
                target.teleportTo(p.serverLevel(), p.getX(), p.getY(), p.getZ(), target.getYRot(), target.getXRot());
                return true;
            }
            default -> { return false; }
        }
    }

    public static void stop(ServerPlayer p, String special) {}

    public static void whileOn(ServerPlayer p, AbilityPower.Move m, long now) {}

    private static ServerPlayer target(ServerPlayer player, Court court) {
        Sov state = sov(player.getUUID());
        if (state.target != null) {
            ServerPlayer selected = player.server.getPlayerList().getPlayer(state.target);
            if (selected != null && subject(court, selected)) return selected;
        }
        return playerAimed(player, Math.max(10, court.radius));
    }

    private static Coll selectedStock(Sov state, Kind kind) {
        if (!state.stock.isEmpty()) {
            Coll selected = state.stock.get(Math.floorMod(state.sel, state.stock.size()));
            if (selected.pen.kind() == kind) return selected;
        }
        return state.stock.stream().filter(c -> c.pen.kind() == kind).findFirst().orElse(null);
    }

    private static ServerPlayer playerAimed(ServerPlayer p, double range) {
        LivingEntity e = Specials.aimed(p, range);
        return e instanceof ServerPlayer sp ? sp : null;
    }

    private static void expel(Court c, ServerPlayer v) {
        Vec3 dir = v.position().subtract(c.center).multiply(1, 0, 1);
        if (dir.lengthSqr() < 1e-4) dir = new Vec3(1, 0, 0);
        Vec3 to = c.center.add(dir.normalize().scale(c.radius + 4));
        v.teleportTo(v.serverLevel(), to.x, v.getY(), to.z, v.getYRot(), v.getXRot());
    }

    private static List<Court> allCourts() {
        List<Court> courts = new ArrayList<>();
        for (Sov s : new ArrayList<>(SOV.values())) {
            if (s.court != null) courts.add(s.court);
            if (s.sealed != null) courts.add(s.sealed);
            courts.addAll(s.jurisdictions);
            if (s.constitution != null && !s.constitution.laws.isEmpty()) courts.add(s.constitution);
        }
        return courts;
    }

    private static boolean signed(UUID owner, UUID person, long now) {
        Sov state = SOV.get(owner);
        return state != null && state.signatures.getOrDefault(person, 0L) > now;
    }

    private static boolean subject(Court court, ServerPlayer player) {
        long now = player.server.overworld().getGameTime();
        if (court.expires <= now) return false;
        if (player.getUUID().equals(court.owner)) {
            Sov state = SOV.get(court.owner);
            boolean inScope = court.constitutional || insideOf(court, player)
                    || (court.tier >= 4 && signed(court.owner, player.getUUID(), now));
            return inScope && state != null && (state.bound || (court.tier < 4 && state.dailyLaw != null));
        }
        if (court.constitutional) return signed(court.owner, player.getUUID(), now);
        return insideOf(court, player) || (court.tier >= 4 && signed(court.owner, player.getUUID(), now));
    }

    private static Court editingCourt(Sov state) {
        return state.selectedCourt >= 0 && state.selectedCourt < state.jurisdictions.size()
                ? state.jurisdictions.get(state.selectedCourt) : state.court;
    }

    private static Court constitution(UUID owner, Court source) {
        Sov state = sov(owner);
        if (state.constitution == null) {
            state.constitution = new Court();
            state.constitution.owner = owner;
            state.constitution.dim = source.dim;
            state.constitution.center = source.center;
            state.constitution.tier = 4;
            state.constitution.radius = 0;
            state.constitution.follow = false;
            state.constitution.constitutional = true;
        }
        return state.constitution;
    }

    public static boolean protectedTrait(ServerPlayer player) {
        Sov state = SOV.get(player.getUUID());
        return state != null && (state.escrow >= 0 || state.escrowBranch >= 0);
    }

    public static boolean branchEscrowed(ServerPlayer player) {
        Sov state = SOV.get(player.getUUID());
        return state != null && state.escrowBranch >= 0;
    }

    public static boolean powerBlocked(ServerPlayer player) {
        return APPLIED.stream().anyMatch(c -> c.from.equals(player.getUUID()) && c.pen.kind() == Kind.ABILITY)
                || (ARB.containsKey(player.getUUID()) && allCourts().stream().anyMatch(c -> c.owner.equals(ARB.get(player.getUUID())) && !c.debt.getOrDefault(player.getUUID(), List.of()).isEmpty()));
    }

    static void upgradeCollateral(UUID owner, double level) {
        if (level < CovenantRules.GOD) return;
        for (Coll collateral : APPLIED) if (collateral.by.equals(owner)) {
            collateral.tier = 4; collateral.higher = false; collateral.until = Long.MAX_VALUE;
        }
        Sov state = SOV.get(owner);
        if (state != null) state.dailyLaw = null;
    }

    public static boolean hasLease(ServerPlayer player, String power) {
        return LEASES.stream().anyMatch(l -> l.holder.equals(player.getUUID()) && l.power.equals(power));
    }

    public static boolean restrictedBorrow(ServerPlayer player, String power) {
        if (power == null || power.equals(AbilityEngine.basePowerId(player))) return false;
        List<Lease> leases = LEASES.stream().filter(l -> l.holder.equals(player.getUUID()) && power.equals(l.power)).toList();
        return !leases.isEmpty() && leases.stream().noneMatch(l -> l.ownedBefore || l.full);
    }

    public static AbilityPower borrowedView(ServerPlayer player, String id, AbilityPower original) {
        if (!restrictedBorrow(player, id)) return original;
        return original.branchView(id);
    }

    public static double moveLevel(ServerPlayer player, String power, int move) {
        for (Lease lease : LEASES) if (lease.holder.equals(player.getUUID()) && lease.power.equals(power)
                && !lease.ownedBefore && (lease.full || lease.move == move)) return lease.originalLevel;
        return levelOf(player);
    }

    public static boolean borrowedMoveAllowed(ServerPlayer player, String power, int move) {
        List<Lease> leases = LEASES.stream().filter(l -> l.holder.equals(player.getUUID()) && l.power.equals(power)).toList();
        if (leases.isEmpty() || power.equals(AbilityEngine.basePowerId(player))) return true;
        Set<Integer> moves = leases.stream().map(l -> l.move).collect(java.util.stream.Collectors.toSet());
        return CovenantRules.branchAllowed(leases.stream().anyMatch(l -> l.ownedBefore), leases.stream().anyMatch(l -> l.full), moves, move);
    }

    public static int[] borrowedMoveStats(ServerPlayer player, String power, int move, int[] fallback) {
        for (Lease lease : LEASES) if (lease.holder.equals(player.getUUID()) && lease.power.equals(power)
                && !lease.ownedBefore && (lease.full || lease.move == move)) return lease.stats.clone();
        return fallback;
    }

    public static int[] borrowedStats(ServerPlayer player, String power, int[] fallback) {
        for (Lease lease : LEASES) if (lease.holder.equals(player.getUUID()) && lease.power.equals(power) && !lease.ownedBefore) return lease.stats.clone();
        return fallback;
    }

    private static boolean lendBranch(ServerPlayer holder, Coll collateral) { return lendBranch(holder, collateral, false); }

    private static boolean lendBranch(ServerPlayer holder, Coll collateral, boolean branchOnly) {
        if (collateral.power == null) return false;
        int chosenMove = collateral.move;
        if (branchOnly && collateral.pen.kind() == Kind.ABILITY) {
            AbilityPower definition = UnodexData.get().powerFor(collateral.power);
            if (definition == null) return false;
            List<Integer> moves = new ArrayList<>();
            for (int i = 0; i < definition.moves.size(); i++) {
                String special = definition.moves.get(i).special;
                if (!"stance_next".equals(special) && !"mode_next".equals(special)) moves.add(i);
            }
            if (moves.isEmpty()) return false;
            chosenMove = moves.get(Math.floorMod(sov(holder.getUUID()).branchSelection, moves.size()));
        }
        final int branch = chosenMove;
        long owned = LEASES.stream().filter(l -> l.holder.equals(holder.getUUID())).count();
        if (owned >= CovenantRules.wieldCap(levelOf(holder))) {
            bar(holder, "§7Simultaneous branch limit reached.");
            return false;
        }
        if (LEASES.stream().anyMatch(l -> l.collateral.equals(collateral.id) && l.holder.equals(holder.getUUID()) && l.move == branch)) {
            AbilityEngine.addCopy(holder, collateral.power); return true;
        }
        Lease lease = new Lease();
        lease.collateral = collateral.id;
        lease.holder = holder.getUUID();
        lease.power = collateral.power;
        lease.move = chosenMove;
        lease.full = collateral.pen.kind() == Kind.ABILITY && !branchOnly;
        lease.stats = collateral.originalStats.clone(); lease.originalLevel = collateral.originalLevel;
        lease.ownedBefore = LEASES.stream().anyMatch(l -> l.holder.equals(holder.getUUID()) && l.power.equals(lease.power) && l.ownedBefore)
                || (AbilityEngine.copies(holder).contains(lease.power) && LEASES.stream().noneMatch(l -> l.holder.equals(holder.getUUID()) && l.power.equals(lease.power)));
        LEASES.add(lease);
        AbilityEngine.addCopy(holder, lease.power);
        return true;
    }

    private static void dropLease(MinecraftServer server, UUID collateral) {
        List<Lease> removed = LEASES.stream().filter(l -> l.collateral.equals(collateral)).toList();
        LEASES.removeIf(l -> l.collateral.equals(collateral));
        for (Lease lease : removed) {
            ServerPlayer holder = server.getPlayerList().getPlayer(lease.holder);
            if (holder != null && !lease.ownedBefore && LEASES.stream().noneMatch(l -> l.holder.equals(lease.holder) && l.power.equals(lease.power))) {
                AbilityEngine.removeCopy(holder, lease.power);
            }
        }
    }

    private static void returnCollateral(MinecraftServer server, Coll collateral) {
        DIRTY_PLAYERS.add(collateral.from); DIRTY_PLAYERS.add(collateral.by);
        if (collateral.holder != null) DIRTY_PLAYERS.add(collateral.holder);
        dropLease(server, collateral.id);
        APPLIED.removeIf(c -> c.id.equals(collateral.id));
        for (Sov s : SOV.values()) s.stock.removeIf(c -> c.id.equals(collateral.id));
        for (List<Coll> list : ENDOW.values()) list.removeIf(c -> c.id.equals(collateral.id));
        ServerPlayer victim = server.getPlayerList().getPlayer(collateral.from);
        if (victim != null) bar(victim, "§aCollateral returned: " + collateral.pen.label());
    }

    private static void ledger(ServerPlayer owner) {
        Sov state = sov(owner.getUUID());
        long now = owner.server.overworld().getGameTime();
        owner.sendSystemMessage(Component.literal("§6COVENANT · " + state.stock.size() + " collateral items"));
        for (int i = 0; i < state.stock.size(); i++) owner.sendSystemMessage(Component.literal("§e" + (i + 1) + ". §f" + state.stock.get(i).label()));
        for (Court court : allCourts()) if (court.owner.equals(owner.getUUID())) {
            for (LawRec law : court.laws) owner.sendSystemMessage(Component.literal("§6#" + law.id + (law.hidden ? " (hidden)" : "") + " : §f" + law.text));
        }
        if (levelOf(owner) < 5) { owner.sendSystemMessage(Component.literal("§7The signature registry unlocks at level 5.")); return; }
        Set<UUID> people = new HashSet<>(state.signatures.keySet());
        for (Court court : allCourts()) if (court.owner.equals(owner.getUUID())) people.addAll(court.inside);
        for (UUID id : people) {
            ServerPlayer person = owner.server.getPlayerList().getPlayer(id);
            if (person == null) {
                Subject last = state.subjects.get(id);
                if (last != null) owner.sendSystemMessage(Component.literal("§6" + last.name + " §7(offline) · " + last.dimension + " " + (int) last.x + " " + (int) last.y + " " + (int) last.z));
                continue;
            }
            List<Pen> debt = new ArrayList<>();
            for (Court court : allCourts()) if (court.owner.equals(owner.getUUID())) debt.addAll(court.debt.getOrDefault(id, List.of()));
            String location = levelOf(owner) >= CovenantRules.GOD ? " · " + person.serverLevel().dimension().location() + " " + person.blockPosition().toShortString() : "";
            var profile = ClaimData.get(person.server).profileOf(id).orElse(null);
            int[] stats = profile == null ? new int[] {1, 1, 1, 1, 1} : com.myriam.unrp.player.PlayerStateService.stats(person.server, id, profile, character(person));
            owner.sendSystemMessage(Component.literal("§6" + person.getGameProfile().getName() + location
                    + " §f· debt " + debt.stream().map(Pen::label).toList() + " · remaining " + java.util.Arrays.toString(stats)
                    + " · power " + AbilityEngine.basePowerId(person) + " · collected " + APPLIED.stream().filter(c -> c.from.equals(id)).map(Coll::label).toList()
                    + (levelOf(owner) >= 6 ? " · Pacts " + pactsOf(id) : "")));
        }
        for (int i = 0; i < state.jurisdictions.size(); i++) owner.sendSystemMessage(Component.literal("§6Jurisdiction " + (i + 1) + " : " + state.jurisdictions.get(i).name));
        if (state.constitution != null) for (LawRec law : state.constitution.laws) owner.sendSystemMessage(Component.literal("§6Constitution #" + law.id + " : " + law.text));
        owner.sendSystemMessage(Component.literal("§7Chat : cv select N · cv return N · cv repeal N · cv laws"));
    }

    private static com.myriam.unrp.data.Models.Character character(ServerPlayer player) {
        var profile = ClaimData.get(player.server).profileOf(player.getUUID()).orElse(null);
        return profile == null || profile.characterId == null ? null : UnodexData.get().characters.get(profile.characterId);
    }

    private static boolean controlChat(ServerPlayer player, String text) {
        String normalized = CovenantParse.norm(text).trim();
        if (normalized.equals("cv pay") || normalized.equals("cv payer")) {
            int paid = 0;
            for (Court court : allCourts()) {
                List<Pen> debt = court.debt.get(player.getUUID());
                while (debt != null && !debt.isEmpty() && collectUnit(court, player, debt.get(0), false, false)) {
                    debt.remove(0); paid++;
                }
            }
            bar(player, "§6Debt paid: " + paid + " unit(s). The remainder requires the corresponding resources.");
            saveState(player.server); return true;
        }
        if (!normalized.startsWith("cv ") || !SOV.containsKey(player.getUUID())) return false;
        Sov state = sov(player.getUUID());
        String[] words = normalized.split("\\s+", 3);
        switch (words[1]) {
            case "ledger", "registre", "laws" -> ledger(player);
            case "branch", "branche" -> {
                try { state.branchSelection = Math.max(0, Integer.parseInt(words[2]) - 1); bar(player, "§6Branch to extract: " + (state.branchSelection + 1)); }
                catch (RuntimeException e) { bar(player, "§7cv branch <usable branch number>"); }
            }
            case "target", "cible" -> {
                if (words.length < 3 || words[2].equals("clear")) { state.target = null; bar(player, "§7Normal targeting restored."); }
                else {
                    ServerPlayer online = player.server.getPlayerList().getPlayerByName(words[2]);
                    Court editing = editingCourt(state);
                    Subject subject = online == null ? known(words[2]) : editing == null ? known(words[2]) : remember(editing, online);
                    if (subject == null) bar(player, "§7Unknown person.");
                    else { state.target = subject.id; bar(player, "§6Target: " + subject.name); }
                }
            }
            case "court", "tribunal" -> {
                try { state.selectedCourt = Integer.parseInt(words[2]) - 1; bar(player, "§6Selected Court (0 = personal): " + words[2]); }
                catch (RuntimeException e) { bar(player, "§7cv court 0, or a jurisdiction number from the ledger"); }
            }
            case "select", "choisir" -> {
                try { state.sel = Math.max(0, Integer.parseInt(words[2]) - 1); bar(player, "§6Selected collateral: " + (state.sel + 1)); }
                catch (RuntimeException e) { bar(player, "§7cv select <collateral number>"); }
            }
            case "return", "rendre" -> {
                try {
                    int index = Integer.parseInt(words[2]) - 1;
                    returnCollateral(player.server, state.stock.get(index));
                } catch (RuntimeException e) { bar(player, "§7cv return <collateral number>"); }
            }
            case "repeal", "abroger" -> {
                try {
                    int id = Integer.parseInt(words[2]);
                    for (Court court : allCourts()) if (court.owner.equals(player.getUUID())) court.laws.removeIf(l -> l.id == id);
                    bar(player, "§6Law / Constitution #" + id + " repealed.");
                } catch (RuntimeException e) { bar(player, "§7cv repeal <law number>"); }
            }
            default -> bar(player, "§7cv ledger · cv select N · cv return N · cv repeal N");
        }
        saveState(player.server);
        return true;
    }

    private static void jurisdiction(ServerPlayer player, Court source, String text) {
        Court fixed = new Court();
        fixed.owner = player.getUUID(); fixed.follow = false; fixed.district = true; fixed.tier = 4;
        fixed.dim = source.dim; fixed.center = player.position(); fixed.radius = 500;
        fixed.expires = player.server.overworld().getGameTime() + CovenantRules.DAY;
        String name = text.trim();
        fixed.name = name;
        var turf = com.myriam.unrp.social.SocialData.get(player.server).store.turfs.stream()
                .filter(t -> t.name != null && t.name.equalsIgnoreCase(name)).findFirst().orElse(null);
        if (turf != null) {
            fixed.dim = turf.dim;
            fixed.center = new Vec3(turf.x, player.getY(), turf.z); fixed.radius = turf.r;
        } else if (!CovenantParse.norm(name).equals("here") && !CovenantParse.norm(name).equals("ici")) {
            bar(player, "§7Unknown territory. Use its exact name or 'here' (500 m radius)."); return;
        }
        for (LawRec law : source.laws) fixed.laws.add(copyLaw(law));
        fixed.terms.addAll(source.terms); fixed.next = source.next;
        sov(player.getUUID()).jurisdictions.add(fixed);
        rebuildParchments(player, fixed);
        bar(player, "§6Jurisdiction declared for 24 hours: " + name);
    }

    private static LawRec copyLaw(LawRec law) {
        LawRec copy = new LawRec(); copy.id = law.id; copy.text = law.text; copy.law = law.law;
        copy.hidden = law.hidden; copy.activeAt = law.activeAt; copy.specific=law.specific;
        return copy;
    }

    private static final class Snapshot {
        Map<UUID, Sov> sovereigns = new HashMap<>();
        List<Coll> applied = new ArrayList<>();
        List<Lease> leases = new ArrayList<>();
        List<VaultEntry> vault = new ArrayList<>();
        Map<UUID, Offer> offers = new HashMap<>();
        Map<UUID, UUID> arbitration = new HashMap<>();
        Map<String, Long> immunity = new HashMap<>();
    }

    private static final com.google.gson.Gson LEDGER_JSON = new com.google.gson.Gson();

    private static ResourceKey<Level> courtDimension(Court court) {
        return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, new net.minecraft.resources.ResourceLocation(court.dim));
    }

    public static void start(MinecraftServer server) {
        if (activeServer == server) return;
        clearState();
        activeServer = server;
        String json = CovenantStateData.get(server).read();
        if (json.isBlank()) return;
        restoreJson(json);
    }

    static void restoreJson(String json) {
        clearState();
        if (json.isBlank()) return;
        Snapshot state = LEDGER_JSON.fromJson(json, Snapshot.class);
        if (state == null) return;
        SOV.putAll(state.sovereigns); APPLIED.addAll(state.applied); LEASES.addAll(state.leases);
        VAULT_ITEMS.addAll(state.vault); OFFERS.putAll(state.offers); ARB.putAll(state.arbitration); PACT_IMMUNITY.putAll(state.immunity);
        // Restore shared identity: a stock item and its applied penalty are one piece of collateral.
        Map<UUID, Coll> canonical = new HashMap<>();
        for (Coll collateral : APPLIED) canonical.put(collateral.id, collateral);
        for (Sov sov : SOV.values()) {
            for (int i = 0; i < sov.stock.size(); i++) sov.stock.set(i, canonical.getOrDefault(sov.stock.get(i).id, sov.stock.get(i)));
            if (sov.sealed != null) SEALED.add(sov.sealed);
        }
        for (Court court : allCourts()) for (LawRec law : court.laws) {
            Sov owner = sov(court.owner); owner.nextLaw = Math.max(owner.nextLaw, law.id + 1);
        }
        for (Coll collateral : APPLIED) if (collateral.holder != null && !collateral.holder.equals(collateral.by)) {
            ENDOW.computeIfAbsent(collateral.holder, k -> new ArrayList<>()).add(collateral);
        }
    }

    public static void saveState(MinecraftServer server) {
        if (activeServer != server) return;
        CovenantStateData.get(server).write(snapshotJson());
    }

    static String snapshotJson() {
        Snapshot snapshot = new Snapshot();
        snapshot.sovereigns.putAll(SOV); snapshot.applied.addAll(APPLIED); snapshot.leases.addAll(LEASES);
        snapshot.vault.addAll(VAULT_ITEMS); snapshot.offers.putAll(OFFERS); snapshot.arbitration.putAll(ARB); snapshot.immunity.putAll(PACT_IMMUNITY);
        return LEDGER_JSON.toJson(snapshot);
    }

    public static void shutdown(MinecraftServer server) {
        saveState(server);
        // SavedData is flushed by the server's normal shutdown save.
        activeServer = null;
        clearState();
    }

    private static void clearState() {
        SOV.clear(); SEALED.clear(); APPLIED.clear(); ENDOW.clear(); PENDING.clear(); OFFERS.clear();
        ARB.clear(); LAST_CHAT.clear(); CD.clear(); LEASES.clear(); VAULT_ITEMS.clear(); PACT_IMMUNITY.clear(); PROXY_DAMAGE.clear(); COLLECTION_GATE.clear(); DIRTY_PLAYERS.clear();
    }

    private static void returnItems(MinecraftServer server) {
        Iterator<VaultEntry> items = VAULT_ITEMS.iterator();
        while (items.hasNext()) {
            VaultEntry item = items.next();
            ServerPlayer target = server.getPlayerList().getPlayer(item.from);
            if (!item.released || target == null) continue;
            try {
                ItemStack stack = ItemStack.of(net.minecraft.nbt.TagParser.parseTag(item.item));
                if (!target.getInventory().add(stack)) target.drop(stack, false);
                items.remove();
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException error) {
                throw new IllegalStateException("Invalid Covenant escrow item", error);
            }
        }
    }

    private static long heldSouls(UUID owner) {
        return APPLIED.stream().filter(c -> c.by.equals(owner) && c.pen.kind() == Kind.SOUL).count();
    }

    private static List<String> pactsOf(UUID person) {
        List<String> pacts = new ArrayList<>();
        for (var owner : SOV.entrySet()) for (PactRec pact : owner.getValue().pacts) {
            if (owner.getKey().equals(person) || pact.target.equals(person)) pacts.add(pact.text);
        }
        return pacts;
    }

    private static void syncSignatures(MinecraftServer server, long now) {
        com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
        com.google.gson.JsonArray people = new com.google.gson.JsonArray();
        List<Court> courts = allCourts();
        // Retire loaded legacy floating name tags, including saved sealed Courts restored after restart.
        for(Court court:courts) {
            ServerLevel level=server.getLevel(courtDimension(court));
            if(level==null||court.constitutional)continue;
            var area=new net.minecraft.world.phys.AABB(court.center.subtract(16,2,16),court.center.add(16,12,16));
            for(ArmorStand stand:level.getEntitiesOfClass(ArmorStand.class,area,e->e.getTags().contains("unrp.cv_law")))stand.discard();
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            Sov own = SOV.get(id);
            long souls = heldSouls(id);
            boolean signature = courts.stream().anyMatch(c -> !c.owner.equals(id) && subject(c, player));
            signature |= own != null && (own.bound || CovenantRules.soulBurden(levelOf(player), souls));
            signature |= SOV.values().stream().anyMatch(state -> state.signatures.getOrDefault(id, 0L) > now);
            boolean deaf = APPLIED.stream().anyMatch(c -> c.from.equals(id) && c.pen.kind() == Kind.SENSE && c.pen.key().equals("hearing"));
            com.google.gson.JsonArray constitutions = new com.google.gson.JsonArray();
            if (own != null && own.constitution != null) for (LawRec law : own.constitution.laws) constitutions.add(law.text);
            com.google.gson.JsonObject person = new com.google.gson.JsonObject();
            person.addProperty("entity", player.getId()); person.addProperty("signed", signature);
            person.addProperty("souls", CovenantRules.soulBurden(levelOf(player), souls) ? souls : 0);
            person.addProperty("deaf", deaf); person.add("constitutions", constitutions);
            people.add(person);
        }
        payload.add("people", people);
        com.myriam.unrp.net.Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), new com.myriam.unrp.net.CovenantPacket(payload.toString()));
        for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            var scene = new com.google.gson.JsonObject();var visuals = new com.google.gson.JsonArray();
            for (Court court : courts) {
                if (visuals.size() >= 8) break;
                if (court.constitutional || court.expires <= now || viewer.serverLevel() != server.getLevel(courtDimension(court))) continue;
                if (viewer.position().distanceToSqr(court.center) > Math.pow(court.radius+48,2)) continue;
                ServerPlayer owner=server.getPlayerList().getPlayer(court.owner);
                var visual=new com.google.gson.JsonObject();
                visual.addProperty("dimension",courtDimension(court).location().toString());
                visual.addProperty("entity",owner==null?-1:owner.getId());visual.addProperty("follow",court.follow);visual.addProperty("district",court.district);
                visual.addProperty("x",court.center.x);visual.addProperty("y",court.center.y);visual.addProperty("z",court.center.z);
                visual.addProperty("radius",court.radius);visual.addProperty("tier",court.tier);
                visual.addProperty("breach",!court.debt.getOrDefault(viewer.getUUID(),List.of()).isEmpty());
                Sov sovereign=SOV.get(court.owner);visual.addProperty("covered",sovereign!=null&&sovereign.indemUntil>now&&court.follow);
                var publicLaws=court.laws.stream().map(law->new CovenantPresentation.Law(law.id,law.text,law.hidden)).toList();
                var laws=new com.google.gson.JsonArray();
                for(var law:CovenantPresentation.visiblePage(publicLaws,now)) {
                    var card=new com.google.gson.JsonObject();card.addProperty("id",law.id());card.addProperty("text",law.text());laws.add(card);
                }
                visual.add("laws",laws);visual.addProperty("total",publicLaws.stream().filter(law->!law.hidden()).count());visuals.add(visual);
            }
            scene.add("courts",visuals);
            com.myriam.unrp.net.Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(()->viewer),new com.myriam.unrp.net.CovenantPacket(scene.toString()));
        }
        for (ServerPlayer sovereign : server.getPlayerList().getPlayers()) {
            if (levelOf(sovereign) < 5 || !SOV.containsKey(sovereign.getUUID())) continue;
            com.google.gson.JsonObject privateState = new com.google.gson.JsonObject();
            com.google.gson.JsonArray markers = new com.google.gson.JsonArray();
            for (ServerPlayer person : server.getPlayerList().getPlayers()) {
                if (person == sovereign) continue;
                boolean visible = courts.stream().anyMatch(c -> c.owner.equals(sovereign.getUUID()) && subject(c, person));
                visible |= levelOf(sovereign) >= 6 && signed(sovereign.getUUID(), person.getUUID(), now);
                if (!visible) continue;
                int debt = courts.stream().filter(c -> c.owner.equals(sovereign.getUUID())).mapToInt(c -> c.debt.getOrDefault(person.getUUID(), List.of()).size()).sum();
                com.google.gson.JsonObject marker = new com.google.gson.JsonObject();
                marker.addProperty("name", person.getGameProfile().getName()); marker.addProperty("dimension", person.serverLevel().dimension().location().toString());
                marker.addProperty("x", person.getX()); marker.addProperty("y", person.getY()); marker.addProperty("z", person.getZ()); marker.addProperty("debt", debt);
                markers.add(marker);
            }
            privateState.add("markers", markers);
            com.myriam.unrp.net.Net.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sovereign), new com.myriam.unrp.net.CovenantPacket(privateState.toString()));
        }
    }

    private static final class Subject {
        UUID id;
        String name, power, dimension, memory;
        int[] stats = {1, 1, 1, 1, 1};
        double level, x, y, z;
    }

    private static Subject remember(Court court, ServerPlayer person) {
        Subject subject = new Subject();
        subject.id = person.getUUID(); subject.name = person.getGameProfile().getName();
        subject.power = AbilityEngine.basePowerId(person); subject.level = levelOf(person);
        var profile = ClaimData.get(person.server).profileOf(person.getUUID()).orElse(null);
        if (profile != null) subject.stats = com.myriam.unrp.player.PlayerStateService.stats(person.server, subject.id, profile, character(person));
        subject.x = person.getX(); subject.y = person.getY(); subject.z = person.getZ();
        subject.dimension = person.serverLevel().dimension().location().toString();
        subject.memory = LAST_CHAT.getOrDefault(person.getUUID(), "");
        sov(court.owner).subjects.put(subject.id, subject);
        return subject;
    }

    private static Subject known(String name) {
        for (Sov state : SOV.values()) for (Subject subject : state.subjects.values()) if (subject.name.equalsIgnoreCase(name)) return subject;
        return null;
    }

    private static boolean collectOffline(MinecraftServer server, Court court, UUID id, Pen penalty) {
        Subject person = sov(court.owner).subjects.get(id);
        if (person == null || !allowed(court.tier, penalty.kind()) || penalty.kind() == Kind.NONE || penalty.kind() == Kind.LIFE) return false;
        Sov escrow = SOV.get(id);
        if (penalty.kind() == Kind.STAT && (person.stats[Integer.parseInt(penalty.key())] <= 1
                || (escrow != null && escrow.escrow == Integer.parseInt(penalty.key())))) return false;
        if (penalty.kind() == Kind.ABILITY && (person.power == null || (escrow != null && escrow.escrowBranch >= 0))) return false;
        if (penalty.kind() != Kind.STAT && penalty.kind() != Kind.MEMORY && penalty.kind() != Kind.BRANCH
                && APPLIED.stream().anyMatch(c -> c.from.equals(id) && c.pen.equals(penalty))) return false;
        Coll collateral = new Coll(); collateral.court = court.id; collateral.tier = court.tier;
        collateral.pen = penalty; collateral.from = id; collateral.fromName = person.name; collateral.power = person.power;
        collateral.originalStats = person.stats.clone(); collateral.originalLevel = person.level; collateral.by = court.owner;
        collateral.memo = person.memory == null ? "" : person.memory;
        if (penalty.kind() == Kind.BRANCH) {
            AbilityPower definition = UnodexData.get().powerFor(person.power);
            if (definition == null) return false;
            List<Integer> choices = new ArrayList<>();
            for (int i = 0; i < definition.moves.size(); i++) {
                final int index = i;
                var move = definition.moves.get(i);
                if ("stance_next".equals(move.special) || "mode_next".equals(move.special) || (escrow != null && escrow.escrowBranch == i)) continue;
                if (!penalty.key().isBlank() && !penalty.key().equals(String.valueOf(i + 1)) && !CovenantParse.norm(move.name).contains(penalty.key())) continue;
                if (APPLIED.stream().noneMatch(c -> c.from.equals(id) && c.pen.kind() == Kind.BRANCH && c.move == index && person.power.equals(c.power))) choices.add(i);
            }
            if (choices.isEmpty()) return false;
            collateral.move = choices.get(0);
        }
        if (penalty.kind() == Kind.STAT) person.stats[Integer.parseInt(penalty.key())]--;
        APPLIED.add(collateral); sov(court.owner).stock.add(collateral);
        return true;
    }

    private static void offerPact(ServerPlayer owner, Pending pending, String text, Court court) {
        Sov state = sov(owner.getUUID());
        long now = owner.server.overworld().getGameTime();
        boolean sovereign = pending.what == Await.SOVEREIGN_PACT;
        ServerPlayer target;
        Subject targetInfo;
        String agreement = text;
        if (sovereign) {
            int separator = text.indexOf('|');
            if (separator < 1) { bar(owner, "§7PlayerName | nothing for your branch"); return; }
            String name = text.substring(0, separator).trim(); agreement = text.substring(separator + 1).trim();
            target = owner.server.getPlayerList().getPlayerByName(name);
            targetInfo = target == null ? known(name) : remember(court, target);
        } else {
            target = owner.server.getPlayerList().getPlayer(pending.target);
            targetInfo = target == null ? null : remember(court, target);
        }
        Pen[] deal = CovenantParse.pact(agreement);
        if (targetInfo == null || targetInfo.id.equals(owner.getUUID()) || deal == null) { bar(owner, "§7Unknown person or unrecognized deal."); return; }
        if (!sovereign && (target == null || !subject(court, target))) { bar(owner, "§7A local Pact requires a signer inside the Court."); return; }
        if (PACT_IMMUNITY.getOrDefault(owner.getUUID() + ":" + targetInfo.id, 0L) > now) { bar(owner, "§7This person is immune to your Pacts for one hour."); return; }
        if (!allowed(court.tier, deal[1].kind()) && deal[1].kind() != Kind.LIFE) { bar(owner, "§7This penalty exceeds your level."); return; }
        Coll offered = findStock(state, deal[0]);
        boolean life = deal[0].kind() == Kind.LIFE && target != null && target.getHealth() < target.getMaxHealth();
        if (court.tier < 4 && offered == null && !life) { bar(owner, "§7The Pact must offer real consideration."); return; }
        if (court.tier >= 4 && offered == null && deal[0].kind() != Kind.NONE && deal[0].kind() != Kind.LIFE) {
            bar(owner, "§7You may offer 'nothing', but cannot promise collateral you do not hold."); return;
        }
        if (offered != null && OFFERS.values().stream().anyMatch(o -> offered.id.equals(o.reserved))) { bar(owner, "§7This collateral is already reserved."); return; }
        if (!pay(owner, 12)) return;
        Offer offer = new Offer(owner.getUUID(), deal[0], deal[1], now + (sovereign ? CovenantRules.DAY : 600), sovereign,
                offered == null ? null : offered.id, agreement);
        OFFERS.put(targetInfo.id, offer);
        if (sovereign) constitution(owner.getUUID(), court); // keeps the remote contract's authority if the personal Court closes
        if (target != null) deliverOffer(target, offer);
        bar(owner, "§6Private Pact sent to " + targetInfo.name + (target == null ? " (delivered on reconnect)." : "."));
    }

    private static void deliverOffer(ServerPlayer target, Offer offer) {
        String name = "Sovereign";
        ServerPlayer by = target.server.getPlayerList().getPlayer(offer.by);
        if (by != null) name = by.getGameProfile().getName();
        target.sendSystemMessage(Component.literal("§6Parchment from " + name + " : " + offer.text + " §eReply accept / refuse."));
        showPactParchment(target, name, offer.text);
    }

    // ------------------------------------------------------------------ tick
    public static void onLogout(UUID id) {
        PENDING.remove(id);
        Offer offer = OFFERS.get(id);
        if (offer != null && !offer.sovereign) OFFERS.remove(id);
        LAST_CHAT.remove(id);
    }

    public static void tick(MinecraftServer server) {
        start(server);
        long now = server.overworld().getGameTime();
        // tribunaux des Souverains en ligne
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            Sov s = SOV.get(p.getUUID());
            upgradeCollateral(p.getUUID(), levelOf(p));
            boolean active = on(p);
            if (active && (s == null || s.court == null)) open(p);
            else if (!active && s != null && s.court != null) {
                close(p, server);
                bar(p, "§7Court closed.");
            }
        }
        // Souverains hors ligne : leur Tribunal se ferme
        for (Sov s : new ArrayList<>(SOV.values())) {
            if (s.court != null && server.getPlayerList().getPlayer(s.court.owner) == null) {
                closeCourt(server, s.court, s);
                s.court = null;
            }
        }
        for (Sov state : new ArrayList<>(SOV.values())) {
            state.signatures.entrySet().removeIf(e -> e.getValue() <= now);
            for (Court expired : state.jurisdictions.stream().filter(c -> c.expires <= now).toList()) closeCourt(server, expired, state);
            state.jurisdictions.removeIf(c -> c.expires <= now);
        }
        for (Court court : allCourts()) tickCourt(server, court, now);
        if (now % 20 == 0) returnItems(server);
        if (now % 20 == 0) {
            for (Sov state : new ArrayList<>(SOV.values())) {
                if (state.tribute != null && now >= state.tributeAt) {
                    Court tributeCourt = state.court != null ? state.court : state.constitution;
                    if (tributeCourt == null || tributeCourt.tier < 4) continue;
                    for (UUID signer : new HashSet<>(state.signatures.keySet())) {
                        ServerPlayer person = server.getPlayerList().getPlayer(signer);
                        if (signed(tributeCourt.owner, signer, now)) {
                            if (person != null) collectUnit(tributeCourt, person, state.tribute, false, false);
                            else collectOffline(server, tributeCourt, signer, state.tribute);
                        }
                    }
                    state.tributeAt = now + CovenantRules.DAY;
                }
            }
        }
        if (now % 100 == 0) {
            PACT_IMMUNITY.entrySet().removeIf(e -> e.getValue() <= now);
            COLLECTION_GATE.entrySet().removeIf(e -> e.getValue() <= now);
            OFFERS.entrySet().removeIf(e -> e.getValue().until <= now);
            for (Court court : allCourts()) court.history.removeIf(a -> a.at < now - CovenantRules.HOUR);
            saveState(server);
        }
        // gages arrivés à terme
        Iterator<Coll> it = APPLIED.iterator();
        while (it.hasNext()) {
            Coll k = it.next();
            if (now < k.until) continue;
            it.remove();
            dropLease(server, k.id);
            for (Sov state : SOV.values()) state.stock.removeIf(x -> x.id.equals(k.id));
            for (List<Coll> endowed : ENDOW.values()) endowed.removeIf(x -> x.id.equals(k.id));
            ServerPlayer v = server.getPlayerList().getPlayer(k.from);
            if (v != null) bar(v, "§aYour collateral is returned: " + k.pen.label() + ".");
        }
        if (now % 10 == 0) effects(server, now);
        if (now % 20 == 0) {
            arbitration(server);
            for (UUID id : new HashSet<>(DIRTY_PLAYERS)) {
                ServerPlayer player = server.getPlayerList().getPlayer(id);
                if (player != null) com.myriam.unrp.player.PlayerStateService.send(player);
            }
            DIRTY_PLAYERS.clear();
        }
        if (now % 40 == 0) syncSignatures(server, now);
    }

    private static void tickCourt(MinecraftServer server, Court c, long now) {
        ServerPlayer owner = server.getPlayerList().getPlayer(c.owner);
        Sov so = sov(c.owner);
        if (owner != null && !c.constitutional) {
            c.ownerLevel = courtLevelOf(owner); c.tier = tierOf(c.ownerLevel);
            if (c.follow) {
                c.center = owner.position(); c.dim = owner.serverLevel().dimension().location().toString();
                c.radius = RADIUS[c.tier];
            }
        }
        ServerLevel lv = server.getLevel(courtDimension(c));
        if (lv == null) return;
        // Stationary and following Courts share the dedicated textured client seal.
        for (ServerPlayer v : server.getPlayerList().getPlayers()) {
            if (v.getUUID().equals(c.owner)) continue;
            if (v.isSpectator()) continue;
            if (c.tier >= 4 && !c.constitutional && v.serverLevel() == lv && v.position().distanceToSqr(c.center) <= 96 * 96) {
                Vec3 seal = c.center.add(0, 0.15, 0);
                Vec3 direction = seal.subtract(v.getEyePosition()).normalize();
                var sight = lv.clip(new net.minecraft.world.level.ClipContext(v.getEyePosition(), seal,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, v));
                if (v.getViewVector(1).dot(direction) >= 0.35 && (sight.getType() == net.minecraft.world.phys.HitResult.Type.MISS
                        || sight.getLocation().distanceToSqr(seal) < 1)) so.signatures.put(v.getUUID(), now + CovenantRules.WEEK);
            }
            boolean physical = !c.constitutional && insideOf(c, v);
            boolean wasPhysical = c.physical.contains(v.getUUID());
            boolean entered = physical && !wasPhysical;
            if (wasPhysical && !physical) breach(v, Viol.LEAVE, c);
            if (physical) c.physical.add(v.getUUID()); else c.physical.remove(v.getUUID());
            boolean in = subject(c, v);
            boolean was = c.inside.contains(v.getUUID());
            if (entered && !c.exiled.contains(v.getUUID())) collectTerms(c, v);
            if (in && !was) {
                if (c.exiled.contains(v.getUUID()) && physical) {
                    expel(c, v);
                    continue;
                }
                c.inside.add(v.getUUID());
                readLaws(v, c);
            } else if (!in && was) {
                c.inside.remove(v.getUUID());
            }
            if(physical&&!c.exiled.contains(v.getUUID()))for(LawRec law:c.laws) {
                if(law.specific==null||now<law.activeAt||now<law.grace.getOrDefault(v.getUUID(),0L))continue;
                if(law.specific.kind()==CovenantSpecificRules.Kind.HOME_REPORT&&entered&&law.specific.matches(v.getGameProfile().getName()))
                    law.reports.putIfAbsent(v.getUUID(),CovenantSpecificEffects.snapshot(v));
                if(law.specific.kind()==CovenantSpecificRules.Kind.SIGNED_CONTRACTS&&now%20==0&&owner!=null)
                    CovenantSpecificEffects.transferContracts(v,owner);
            }
            if (in) {
                if (now % 20 == 0) remember(c, v);
                if (c.exiled.contains(v.getUUID()) && physical) { expel(c, v); continue; }
                if (v.isSprinting()) breach(v, Viol.SPRINT);
                if (v.isShiftKeyDown()) breach(v, Viol.SNEAK);
                if (v.isFallFlying() || v.getAbilities().flying || Pal.soaring(v)) breach(v, Viol.FLY);
                if (now % 10 == 0 && v.serverLevel() == lv) Vfx.glow(lv, Vfx.SPARKS, 0xF2C94C, v.position().add(0, 0.9, 0), new Vec3(0.15, 0.1, 0.15), 1, 0.01); // signature sur la main
            }
        }
        if(owner!=null&&now%20==0)for(LawRec law:c.laws) {
            var pending=law.reports.entrySet().iterator();
            while(pending.hasNext()) {
                if(!CovenantSpecificEffects.deliver(pending.next().getValue(),owner))break;
                pending.remove();
            }
        }
        // Debt can be paid in stationary/constitutional Courts even when the caster is absent.
        if (now % 200 == 0) for (var entry : new ArrayList<>(c.debt.entrySet())) {
            ServerPlayer person = server.getPlayerList().getPlayer(entry.getKey());
            if (person != null && !entry.getValue().isEmpty() && subject(c, person)
                    && collectUnit(c, person, entry.getValue().get(0), false, false)) entry.getValue().remove(0);
        }
        if (owner != null && c.follow) {
            // Indemnity uses the dedicated shield glyphs in the visual scene.
            // The dedicated client seal follows the interpolated owner instead of intermittent generic rings.

            if (now % 10 == 0) moveParchments(lv, owner, c);
            if (so.proxy != null && now > so.proxyUntil) so.proxy = null;
            // niveau 5 : le registre
            if (now % 40 == 0 && levelOf(owner) >= 5) registry(owner, c);
            // haut niveau : plus de 10 âmes, une loi du Tribunal s'applique à lui chaque jour
            long souls = heldSouls(c.owner);
            if (!CovenantRules.soulBurden(levelOf(owner), souls)) so.dailyLaw = null;
            if (CovenantRules.soulBurden(levelOf(owner), souls) && !c.laws.isEmpty()) {
                if (now >= so.daily) {
                    so.dailyLaw = c.laws.get(owner.getRandom().nextInt(c.laws.size()));
                    so.daily = now + CovenantRules.DAY;
                    bar(owner, "§cYour souls bind you: law #" + so.dailyLaw.id + " applies to you today.");
                }
                if (now % 10 == 0) Vfx.glow(lv, Vfx.SPARKS, 0xF2C94C, owner.position().add(0, 0.9, 0), new Vec3(0.2, 0.1, 0.2), 2, 0.01);
            }
        }
    }

    private static void readLaws(ServerPlayer v, Court c) {
        List<String> shown = new ArrayList<>();
        for (LawRec l : c.laws) if (!l.hidden) shown.add("§e#" + l.id + " §f" + l.text);
        v.sendSystemMessage(Component.literal("§6You entered a Court: you have signed the Covenant."));
        for (String s : shown) v.sendSystemMessage(Component.literal(s));
        if (c.tier == 0) bar(v, "§7Laws are active; people already inside receive three seconds before a new law applies.");
    }

    private static void moveParchments(ServerLevel lv, ServerPlayer owner, Court c) {
        int n = 0;
        for (UUID id : c.parchments) {
            var e = lv.getEntity(id);
            if (e != null) e.teleportTo(owner.getX(), owner.getY() + 2.6 + n * 0.28, owner.getZ());
            n++;
        }
    }

    private static void registry(ServerPlayer owner, Court c) {
        StringBuilder b = new StringBuilder("§6Court §f" + c.inside.size() + " §6signers");
        int shown = 0;
        for (var e : c.debt.entrySet()) {
            if (e.getValue().isEmpty() || shown >= 3) continue;
            ServerPlayer v = owner.server.getPlayerList().getPlayer(e.getKey());
            if (v == null) continue;
            b.append(shown == 0 ? " §7| §cIn breach: " : " §7, ").append("§f").append(v.getGameProfile().getName()).append(" §7(owes ").append(e.getValue().size()).append(")");
            shown++;
        }
        bar(owner, b.toString());
    }

    /** Effets des peines sur les victimes, rafraîchis toutes les 10 ticks. */
    private static void effects(MinecraftServer server, long now) {
        Map<UUID, Integer> souls = new HashMap<>();
        for (Coll k : APPLIED) {
            ServerPlayer v = server.getPlayerList().getPlayer(k.from);
            if (v == null) continue;
            switch (k.pen.kind()) {
                case MOVE -> {
                    if (k.pen.key().equals("sprint")) v.setSprinting(false);
                    else if (k.pen.key().equals("walk")) {
                        v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 255, true, false));
                        v.setDeltaMovement(0, v.getDeltaMovement().y, 0); v.hurtMarked = true;
                    }
                }
                case SENSE -> {
                    if (k.pen.key().equals("sight")) v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false));
                    else if (k.pen.key().equals("touch")) {
                        v.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30, 1, true, false));
                        v.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 0, true, false));
                    }
                }
                case SOUL -> souls.merge(v.getUUID(), 1, Integer::sum);
                default -> {}
            }
        }
        for (ServerPlayer v : server.getPlayerList().getPlayers()) {
            AttributeInstance hp = v.getAttribute(Attributes.MAX_HEALTH);
            if (hp == null) continue;
            int n = souls.getOrDefault(v.getUUID(), 0);
            List<Coll> en = ENDOW.get(v.getUUID());
            int gift = 0;
            if (en != null) for (Coll k : en) if (k.pen.kind() == Kind.SOUL) gift++;
            double amt = -4.0 * n + 4.0 * gift;
            AttributeModifier cur = hp.getModifier(SOUL_MOD);
            if (cur != null && cur.getAmount() == amt) continue;
            if (cur != null) hp.removeModifier(SOUL_MOD);
            if (amt != 0) hp.addTransientModifier(new AttributeModifier(SOUL_MOD, "unrp_covenant_soul", amt, AttributeModifier.Operation.ADDITION));
            if (v.getHealth() > v.getMaxHealth()) v.setHealth(v.getMaxHealth());
        }
    }

    private static void arbitration(MinecraftServer server) {
        Iterator<Map.Entry<UUID, UUID>> it = ARB.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            ServerPlayer v = server.getPlayerList().getPlayer(e.getKey());
            Sov s = SOV.get(e.getValue());
            boolean owes = allCourts().stream().anyMatch(c -> c.owner.equals(e.getValue()) && !c.debt.getOrDefault(e.getKey(), List.of()).isEmpty());
            if (s == null || !owes) {
                it.remove();
                if (v != null) bar(v, "§aYour debt is paid: your power is restored.");
                continue;
            }
            if (v != null) AbilityEngine.allOff(v);
        }
    }

    // ------------------------------------------------------------------ blessures
    public static void onHurt(LivingHurtEvent e) {
        if (SOV.isEmpty()) return;
        // un coup porté par un signataire est un manquement (« attaquer »)
        if (e.getSource().getEntity() instanceof ServerPlayer by && by != e.getEntity()) breach(by, Viol.ATTACK);
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        Sov s = SOV.get(p.getUUID());
        if (s == null) return;
        long now = p.serverLevel().getGameTime();
        if (on(p) && now < s.indemUntil) { // Indemnité : la blessure est rayée et retombe sur son auteur
            e.setCanceled(true);
            if (e.getSource().getEntity() instanceof ServerPlayer by && s.court != null && by != p) {
                collectBreach(s.court, by, new Pen(Kind.STAT, String.valueOf(by.getRandom().nextInt(5))), now);
            }
            return;
        }
        if (on(p) && s.proxy != null && now < s.proxyUntil && !PROXY_DAMAGE.contains(p.getUUID())) { // Procuration : la blessure passe au lié
            ServerPlayer t = p.server.getPlayerList().getPlayer(s.proxy);
            if (t != null && t.isAlive()) {
                e.setCanceled(true);
                PROXY_DAMAGE.add(p.getUUID());
                try { t.hurt(t.damageSources().magic(), e.getAmount()); }
                finally { PROXY_DAMAGE.remove(p.getUUID()); }
            }
        }
    }
}
