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
 * Tout est en mémoire (un redémarrage rend tout aux victimes) sauf les objets confisqués, sauvegardés chez le Souverain.
 */
public final class Specials17 {
    private Specials17() {}

    private static final Set<String> ALL = Set.of("cv_enact", "cv_terms", "cv_fine", "cv_foreclose", "cv_pact", "cv_endow", "cv_proxy",
            "cv_appropriate", "cv_immunity", "cv_indemnity", "cv_escrow", "cv_arbitrate");
    private static final double[] RADIUS = {10, 30, 75, 150};
    private static final int[] LAW_CAP = {2, 5, Integer.MAX_VALUE, Integer.MAX_VALUE};
    private static final String[] STAT = {"Puissance", "Défense", "Récupération", "Ruse", "Vitesse"};
    private static final UUID SOUL_MOD = UUID.fromString("c0a1e7a2-7a11-4c0d-9e60-2d1b0a5e0c01");
    private static final String VAULT = "unrp_cv_vault";

    public static boolean handles(String s) {
        return s != null && ALL.contains(s);
    }

    // ------------------------------------------------------------------ données
    private static final class Court {
        UUID owner;
        ResourceKey<Level> dim;
        Vec3 center;
        double radius;
        boolean follow = true;
        final List<LawRec> laws = new ArrayList<>();
        int next = 1;
        final Set<Term> terms = EnumSet.noneOf(Term.class);
        final Set<UUID> inside = new HashSet<>();
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
        boolean hidden;
        long activeAt;
    }

    /** Un gage : ce qui a été pris à quelqu'un. */
    private static final class Coll {
        Pen pen;
        int amt = 1;
        UUID from;
        String fromName;
        String power;
        int move = -1;
        String memo = "";
        boolean permanent;
        UUID by;
        UUID holder; // qui en profite (Souverain ou allié)
        long until = Long.MAX_VALUE; // retour à la victime
        boolean higher;

        String label() {
            String s = pen.label() + (pen.kind() == Kind.STAT && amt > 1 ? " ×" + amt : "") + " de " + fromName;
            if (pen.kind() == Kind.BRANCH && power != null) s = "branche " + (move + 1) + " de " + power + " de " + fromName;
            if (pen.kind() == Kind.MEMORY && !memo.isEmpty()) s += " (« " + memo + " »)";
            return s;
        }
    }

    private static final class Sov {
        Court court;
        Court sealed;
        final List<Coll> stock = new ArrayList<>();
        boolean bound;
        int sel;
        int escrow = -1;
        long indemUntil, proxyUntil, daily;
        UUID proxy;
        LawRec dailyLaw;
    }

    private record Offer(UUID by, Pen give, Pen take, long until) {}

    private enum Await { LAW, TERMS, PACT }

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
    private static final Map<UUID, Boolean> SOUL_SEEN = new HashMap<>();

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
            bar(p, "§cIl faut " + cost + " d'aura.");
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
            return c != null && c.level != null ? c.level : 10;
        }
        return pr.level;
    }

    static int tierOf(double level) {
        return level < 4 ? 0 : level < 7 ? 1 : level < 9 ? 2 : 3;
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
        String pid = AbilityEngine.basePowerId(p);
        for (Coll c : APPLIED) if (c.from.equals(p.getUUID()) && c.pen.kind() == Kind.BRANCH && c.move == slot && c.power != null && c.power.equals(pid)) return true;
        return false;
    }

    // ------------------------------------------------------------------ tribunal
    private static Court open(ServerPlayer p) {
        Sov s = sov(p.getUUID());
        if (s.court != null) return s.court;
        Court c = new Court();
        c.owner = p.getUUID();
        c.dim = p.serverLevel().dimension();
        c.center = p.position();
        c.tier = tierOf(levelOf(p));
        c.radius = RADIUS[c.tier];
        if (s.dailyLaw != null) { /* la loi du jour n'est pas une loi du Tribunal */ }
        s.court = c;
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1f, 0.6f);
        Fx.wave(p.serverLevel(), p.position(), Math.min(c.radius, 14), 0xF2C94C, 24);
        bar(p, "§6Tribunal ouvert : " + (int) c.radius + " m. Écris tes lois.");
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
            if (!k.by.equals(c.owner) || k.until != Long.MAX_VALUE || k.permanent) continue;
            k.until = now + (k.pen.kind() == Kind.SENSE || k.higher ? 12000 : 72000);
        }
        // objets confisqués : rendus à leur propriétaire s'il est là, sinon gardés dans le coffre (sauvegardé)
        returnVault(server, c.owner);
        // les prêts s'arrêtent
        for (List<Coll> l : ENDOW.values()) l.removeIf(k -> k.by.equals(c.owner));
        for (UUID id : c.inside) {
            ServerPlayer v = server.getPlayerList().getPlayer(id);
            if (v != null) bar(v, "§7Le Tribunal se ferme.");
        }
        c.inside.clear();
        ARB.values().removeIf(o -> o.equals(c.owner));
        Sov so = SOV.get(c.owner);
        if (so != null) so.proxy = null;
    }

    private static boolean insideOf(Court c, ServerPlayer v) {
        return v.serverLevel().dimension().equals(c.dim) && v.position().distanceTo(c.center) <= c.radius;
    }

    private static List<Court> courtsOf(ServerPlayer v) {
        List<Court> out = new ArrayList<>();
        for (Sov s : SOV.values()) if (s.court != null && s.court.inside.contains(v.getUUID())) out.add(s.court);
        for (Court c : SEALED) if (c.inside.contains(v.getUUID())) out.add(c);
        return out;
    }

    // ------------------------------------------------------------------ pénalités
    private static boolean allowed(int tier, Kind k) {
        return switch (k) {
            case STAT, MOVE -> true;
            case BRANCH, SENSE -> tier >= 2;
            case MEMORY, SOUL -> tier >= 3;
            case LIFE -> false;
        };
    }

    private static Coll makeColl(Court c, ServerPlayer v, Pen pen, boolean pact) {
        Sov sv = sov(c.owner);
        Pen use = pen;
        if (use.kind() == Kind.STAT && sov(v.getUUID()).escrow == Integer.parseInt(use.key())) return null; // Escrow
        if (use.kind() == Kind.BRANCH) {
            long held = sv.stock.stream().filter(k -> k.pen.kind() == Kind.BRANCH).count();
            AbilityPower pw = UnodexData.get().powerFor(AbilityEngine.basePowerId(v));
            List<Integer> idx = new ArrayList<>();
            if (pw != null) {
                for (int i = 0; i < pw.moves.size() && i < 12; i++) {
                    if ("stance_next".equals(pw.moves.get(i).special) || "mode_next".equals(pw.moves.get(i).special)) continue;
                    boolean taken = false;
                    for (Coll k : APPLIED) if (k.from.equals(v.getUUID()) && k.pen.kind() == Kind.BRANCH && k.move == i) taken = true;
                    if (!taken) idx.add(i);
                }
            }
            if (held >= 5 || idx.isEmpty()) use = new Pen(Kind.STAT, "4"); // plus de branche à prendre : on retombe sur une stat
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
        k.from = v.getUUID();
        k.fromName = v.getGameProfile().getName();
        k.by = c.owner;
        k.permanent = pact && pen.kind() == Kind.SOUL && c.tier >= 3;
        k.higher = levelOfSafe(v) > levelOfOwner(v.server, c.owner);
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

    private static void collectUnit(Court c, ServerPlayer v, Pen pen, boolean pact, boolean announce) {
        Sov sv = sov(c.owner);
        Coll k = makeColl(c, v, pen, pact);
        if (k == null) return;
        APPLIED.add(k);
        sv.stock.add(k);
        if (k.pen.kind() == Kind.MEMORY) {
            v.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
            AbilityEngine.spendAura(v, (int) (AbilityEngine.aura(v) * 0.4));
        }
        ServerPlayer owner = v.server.getPlayerList().getPlayer(c.owner);
        if (announce) {
            bar(v, "§6Manquement. Prélevé : " + k.pen.label());
            if (owner != null) bar(owner, "§6Gage reçu : " + k.label());
        }
        v.serverLevel().playSound(null, v.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 0.6f);
        Vfx.glow(v.serverLevel(), Vfx.SPARKS, 0xF2C94C, v.position().add(0, 1, 0), new Vec3(0.4, 0.6, 0.4), 8, 0.05);
    }

    /** Un manquement : chaque loi qui correspond prélève son gage, le reste de la peine devient une dette. */
    private static void breach(ServerPlayer v, Viol viol) {
        for (Court c : courtsOf(v)) {
            Sov so = SOV.get(c.owner);
            if (c.owner.equals(v.getUUID()) && (so == null || !so.bound)) continue;
            long now = v.serverLevel().getGameTime();
            for (LawRec l : c.laws) {
                if (l.law.viol() != viol || now < l.activeAt) continue;
                String key = v.getUUID() + "#" + l.id;
                if (now < c.gate.getOrDefault(key, 0L)) continue;
                c.gate.put(key, now + 20);
                collectUnit(c, v, l.law.pen(), false, true);
                c.breaches.merge(v.getUUID(), 1, Integer::sum);
                c.lastBreach.put(v.getUUID(), now);
                List<Pen> d = c.debt.computeIfAbsent(v.getUUID(), k -> new ArrayList<>());
                d.add(l.law.pen());
                d.add(l.law.pen());
                revokeEndow(v);
            }
        }
    }

    private static void revokeEndow(ServerPlayer v) {
        List<Coll> e = ENDOW.remove(v.getUUID());
        if (e == null) return;
        for (Coll k : e) {
            k.holder = null;
            Sov s = SOV.get(k.by);
            if (s != null) s.stock.add(k);
        }
        bar(v, "§cTu as enfreint une loi : le prêt est retiré.");
    }

    // ------------------------------------------------------------------ événements de jeu (branchés par AbilityEvents)
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
                bar(p, "§7Offre refusée.");
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

    private static void finish(ServerPlayer p, Pending pd, String text) {
        Sov s = sov(p.getUUID());
        Court c = s.court;
        if (c == null) {
            bar(p, "§7Le Tribunal est fermé.");
            return;
        }
        switch (pd.what) {
            case LAW -> {
                Law law = CovenantParse.law(text);
                if (law == null) {
                    p.sendSystemMessage(Component.literal("§cLoi non comprise. Écris : §fPersonne ne court. Pénalité : sa Vitesse"));
                    p.sendSystemMessage(Component.literal("§7Infractions : courir, sauter, attaquer, pouvoir, parler, voler, casser, poser, s'accroupir, sortir. Peines : puissance, défense, récupération, ruse, vitesse, mouvement, vue, toucher, branche, mémoire, âme."));
                    return;
                }
                if (!allowed(c.tier, law.pen().kind())) {
                    bar(p, "§cTon niveau ne permet pas encore cette peine (" + law.pen().label() + ").");
                    return;
                }
                if (c.laws.size() >= LAW_CAP[c.tier]) {
                    bar(p, "§cTu as déjà " + LAW_CAP[c.tier] + " lois (accroupi + Enact : en abroger une).");
                    return;
                }
                if (!pay(p, 10)) return;
                LawRec r = new LawRec();
                r.id = c.next++;
                r.text = text.length() > 70 ? text.substring(0, 70) : text;
                r.law = law;
                r.activeAt = p.serverLevel().getGameTime() + (c.tier == 0 ? 60 : 0);
                c.laws.add(r);
                announce(p, c, "§6Nouvelle loi §e#" + r.id + " : §f" + r.text);
                rebuildParchments(p, c);
            }
            case TERMS -> {
                var t = CovenantParse.terms(text);
                if (t.isEmpty()) {
                    bar(p, "§cTermes non compris : armes, armures, élytres, perles ou tout l'inventaire.");
                    return;
                }
                if (!pay(p, 8)) return;
                c.terms.clear();
                c.terms.addAll(t);
                announce(p, c, "§6Termes d'entrée : §f" + t);
            }
            case PACT -> {
                Pen[] pt = CovenantParse.pact(text);
                ServerPlayer tgt = p.server.getPlayerList().getPlayer(pd.target);
                if (pt == null || tgt == null) {
                    bar(p, "§cPacte non compris. Écris : §fma vie contre ta branche §c(« je donne X contre Y »).");
                    return;
                }
                if (!allowed(c.tier, pt[1].kind()) && pt[1].kind() != Kind.LIFE) {
                    bar(p, "§cTon niveau ne permet pas de prendre : " + pt[1].label());
                    return;
                }
                if (pt[0].kind() != Kind.LIFE && findStock(s, pt[0]) == null) {
                    bar(p, "§cTu n'as pas ce gage à offrir : " + pt[0].label());
                    return;
                }
                if (!pay(p, 12)) return;
                OFFERS.put(tgt.getUUID(), new Offer(p.getUUID(), pt[0], pt[1], p.serverLevel().getGameTime() + 600));
                tgt.sendSystemMessage(Component.literal("§6" + p.getGameProfile().getName() + " te propose un Pacte : il donne §f" + pt[0].label() + "§6 contre §f" + pt[1].label() + "§6. Réponds §faccepter§6 ou §frefuser§6 (30 s)."));
                bar(p, "§7Offre envoyée à " + tgt.getGameProfile().getName() + ".");
            }
        }
    }

    private static Coll findStock(Sov s, Pen want) {
        for (Coll k : s.stock) if (k.holder == null && k.pen.kind() == want.kind() && (want.key().isEmpty() || k.pen.key().equals(want.key()))) return k;
        return null;
    }

    private static void acceptPact(ServerPlayer target, Offer o) {
        ServerPlayer by = target.server.getPlayerList().getPlayer(o.by);
        Sov s = SOV.get(o.by);
        if (by == null || s == null || s.court == null) {
            bar(target, "§7Le Souverain n'est plus là.");
            return;
        }
        Court c = s.court;
        // ce que le Souverain donne
        if (o.give.kind() == Kind.LIFE) {
            target.setHealth(target.getMaxHealth());
            target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
            by.hurt(by.damageSources().magic(), Math.min(by.getHealth() - 1, by.getMaxHealth() * 0.25f));
        } else {
            Coll k = findStock(s, o.give);
            if (k == null) {
                bar(target, "§7Le Souverain n'a plus ce gage.");
                return;
            }
            s.stock.remove(k);
            k.holder = target.getUUID();
            ENDOW.computeIfAbsent(target.getUUID(), x -> new ArrayList<>()).add(k);
        }
        // ce qu'il prend
        if (o.take.kind() == Kind.LIFE) {
            Pen soul = new Pen(Kind.SOUL, "");
            collectUnit(c, target, soul, true, false);
        } else {
            collectUnit(c, target, o.take, true, false);
        }
        target.sendSystemMessage(Component.literal("§6Pacte conclu. Il s'applique des deux côtés."));
        bar(by, "§6Pacte conclu avec " + target.getGameProfile().getName() + ".");
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
        ServerLevel lv = owner.serverLevel();
        for (UUID id : c.parchments) {
            var e = lv.getEntity(id);
            if (e != null) e.discard();
        }
        c.parchments.clear();
        int n = 0;
        for (LawRec l : c.laws) {
            if (l.hidden || n >= 6) continue;
            ArmorStand st = EntityType.ARMOR_STAND.create(lv);
            if (st == null) continue;
            st.moveTo(owner.getX(), owner.getY() + 2.6 + n * 0.28, owner.getZ(), 0, 0);
            st.setInvisible(true);
            st.setNoGravity(true);
            st.setInvulnerable(true);
            st.setCustomNameVisible(true);
            st.setCustomName(Component.literal("§6§l#" + l.id + " §r§f" + l.text));
            st.addTag("unrp.cv_law");
            CompoundTag t = new CompoundTag();
            st.saveWithoutId(t);
            t.putBoolean("Marker", true);
            st.load(t);
            lv.addFreshEntity(st);
            c.parchments.add(st.getUUID());
            n++;
        }
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
        if (!keep.isEmpty()) bar(owner, "§7Des objets attendent leurs propriétaires absents : ils te sont rendus à leur retour.");
    }

    /** À la connexion d'un joueur : rend les objets qu'un Souverain avait gardés pour lui. */
    public static void onLogin(ServerPlayer joiner) {
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
        ServerPlayer owner = v.server.getPlayerList().getPlayer(c.owner);
        if (owner == null || c.terms.isEmpty()) return;
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
                vaultAdd(owner, v.getUUID(), s.copy());
                inv.setItem(i, ItemStack.EMPTY);
                n++;
            }
        }
        if (n > 0) {
            bar(v, "§6Termes d'entrée : " + n + " objet(s) confisqué(s), rendus à la fermeture.");
            collectGage(c, v);
        }
    }

    private static void collectGage(Court c, ServerPlayer v) { /* les termes prennent des objets, pas de gage de stat */ }

    // ------------------------------------------------------------------ capacités
    public static boolean run(ServerPlayer p, AbilityPower power, AbilityPower.Move m) {
        boolean sneak = p.isShiftKeyDown();
        Sov s = sov(p.getUUID());
        Court c = s.court;
        long now = p.serverLevel().getGameTime();
        if (c == null && !m.special.equals("cv_immunity")) {
            bar(p, "§7Le Tribunal n'est pas ouvert : active ton pouvoir (R).");
            return false;
        }
        switch (m.special) {
            case "cv_enact" -> {
                if (sneak) { // abroger la dernière loi
                    if (c.laws.isEmpty()) return false;
                    LawRec r = c.laws.remove(c.laws.size() - 1);
                    announce(p, c, "§6Loi #" + r.id + " abrogée.");
                    rebuildParchments(p, c);
                    return false;
                }
                PENDING.put(p.getUUID(), new Pending(Await.LAW, now + 600, null));
                p.sendSystemMessage(Component.literal("§6Écris ta loi dans le chat (30 s) : §fPersonne ne court. Pénalité : sa Vitesse"));
                return false;
            }
            case "cv_terms" -> {
                if (sneak) {
                    if (c.tier < 3) {
                        bar(p, "§7Sceller un Tribunal demande le haut niveau.");
                        return false;
                    }
                    if (s.sealed != null) SEALED.remove(s.sealed);
                    Court k = new Court();
                    k.owner = p.getUUID();
                    k.dim = c.dim;
                    k.center = p.position();
                    k.radius = c.radius;
                    k.follow = false;
                    k.tier = c.tier;
                    k.laws.addAll(c.laws);
                    k.terms.addAll(c.terms);
                    k.next = c.next;
                    s.sealed = k;
                    SEALED.add(k);
                    Fx.wave(p.serverLevel(), p.position(), 20, 0xF2C94C, 40);
                    announce(p, c, "§6Tribunal scellé ici. Ses lois s'appliquent même en ton absence (jusqu'au redémarrage).");
                    return true;
                }
                PENDING.put(p.getUUID(), new Pending(Await.TERMS, now + 600, null));
                p.sendSystemMessage(Component.literal("§6Écris les termes d'entrée (30 s) : §fQui entre abandonne ses armes"));
                return false;
            }
            case "cv_fine" -> {
                if (sneak) {
                    for (LawRec l : c.laws) if (l.hidden) { l.hidden = false; announce(p, c, "§6Clause révélée : §f" + l.text); rebuildParchments(p, c); return true; }
                    bar(p, "§7Aucune clause cachée.");
                    return false;
                }
                for (int i = c.laws.size() - 1; i >= 0; i--) {
                    LawRec l = c.laws.get(i);
                    if (!l.hidden) {
                        l.hidden = true;
                        rebuildParchments(p, c);
                        bar(p, "§6Loi #" + l.id + " cachée : elle s'applique, sans s'afficher.");
                        return true;
                    }
                }
                return false;
            }
            case "cv_foreclose" -> {
                ServerPlayer t = playerAimed(p, 24);
                if (t == null || !c.inside.contains(t.getUUID())) {
                    bar(p, "§7Vise un signataire.");
                    return false;
                }
                if (sneak) { // Exile
                    if (c.breaches.getOrDefault(t.getUUID(), 0) < 3) {
                        bar(p, "§7Il faut 3 manquements pour l'exil (" + c.breaches.getOrDefault(t.getUUID(), 0) + ").");
                        return false;
                    }
                    if (!pay(p, 25)) return false;
                    c.exiled.add(t.getUUID());
                    c.inside.remove(t.getUUID());
                    expel(c, t);
                    announce(p, c, "§6" + t.getGameProfile().getName() + " est exilé du Tribunal.");
                    return true;
                }
                List<Pen> d = c.debt.remove(t.getUUID());
                if (d == null || d.isEmpty()) {
                    bar(p, "§7Aucune dette à saisir.");
                    return false;
                }
                if (!pay(p, 20)) { c.debt.put(t.getUUID(), d); return false; }
                int total = (int) Math.ceil(d.size() * 1.25);
                for (int i = 0; i < total; i++) collectUnit(c, t, d.get(i % d.size()), false, i == 0);
                bar(p, "§6Saisie : " + total + " unité(s), intérêts compris.");
                return true;
            }
            case "cv_pact" -> {
                if (sneak) { // convoquer une âme
                    Coll soul = null;
                    for (Coll k : s.stock) if (k.pen.kind() == Kind.SOUL) { soul = k; break; }
                    if (soul == null) {
                        bar(p, "§7Tu ne détiens aucune âme.");
                        return false;
                    }
                    ServerPlayer v = p.server.getPlayerList().getPlayer(soul.from);
                    if (v == null) {
                        bar(p, "§7Cette âme est hors du monde.");
                        return false;
                    }
                    if (!pay(p, 15)) return false;
                    v.teleportTo(p.serverLevel(), p.getX(), p.getY(), p.getZ(), v.getYRot(), v.getXRot());
                    bar(v, "§6Ton âme est convoquée.");
                    return true;
                }
                ServerPlayer t = playerAimed(p, 12);
                if (t == null || t == p) {
                    bar(p, "§7Vise la personne à qui tu offres un pacte.");
                    return false;
                }
                PENDING.put(p.getUUID(), new Pending(Await.PACT, now + 600, t.getUUID()));
                p.sendSystemMessage(Component.literal("§6Écris l'accord (30 s) : §fma vie contre ta branche §7(je donne X contre Y)"));
                return false;
            }
            case "cv_endow" -> {
                List<Coll> free = new ArrayList<>();
                for (Coll k : s.stock) if (k.holder == null) free.add(k);
                if (free.isEmpty()) {
                    bar(p, "§7Ton Gage est vide.");
                    return false;
                }
                s.sel = Math.floorMod(s.sel, free.size());
                if (sneak) {
                    s.sel = (s.sel + 1) % free.size();
                    bar(p, "§6Gage choisi : §f" + free.get(s.sel).label());
                    return false;
                }
                ServerPlayer t = playerAimed(p, 16);
                if (t == null || t == p) {
                    bar(p, "§7Vise l'allié à doter.");
                    return false;
                }
                if (!pay(p, 8)) return false;
                Coll k = free.get(s.sel);
                s.stock.remove(k);
                k.holder = t.getUUID();
                ENDOW.computeIfAbsent(t.getUUID(), x -> new ArrayList<>()).add(k);
                if (k.pen.kind() == Kind.BRANCH && k.power != null) AbilityEngine.setCopy(t, k.power, now + 6000);
                bar(t, "§6Dotation : " + k.pen.label() + " (jusqu'à la fermeture, ou à ta première infraction).");
                bar(p, "§6Dotation accordée à " + t.getGameProfile().getName() + ".");
                return true;
            }
            case "cv_proxy" -> {
                if (sneak) {
                    s.proxy = null;
                    bar(p, "§7Procuration levée.");
                    return false;
                }
                ServerPlayer t = playerAimed(p, 20);
                if (t == null || !c.inside.contains(t.getUUID())) {
                    bar(p, "§7Vise un signataire.");
                    return false;
                }
                if (!pay(p, 25)) return false;
                s.proxy = t.getUUID();
                s.proxyUntil = now + 600;
                bar(p, "§6Procuration : " + t.getGameProfile().getName() + " encaisse tes blessures (30 s).");
                bar(t, "§cTu es lié à un Souverain : ses blessures sont les tiennes.");
                return true;
            }
            case "cv_appropriate" -> {
                Coll k = null;
                for (Coll x : s.stock) if (x.holder == null && x.pen.kind() == Kind.BRANCH) { k = x; break; }
                if (k == null) {
                    bar(p, "§7Aucune branche volée à dépenser.");
                    return false;
                }
                if (!pay(p, 30)) return false;
                s.stock.remove(k);
                AbilityEngine.setCopy(p, k.power, now + 400);
                bar(p, "§6Appropriation : le pouvoir de " + k.fromName + " est à toi 20 s (≈ copie entière).");
                return true;
            }
            case "cv_immunity" -> {
                s.bound = !s.bound;
                bar(p, s.bound ? "§cTu te lies à tes propres lois." : "§6Immunité souveraine : tu n'es lié par rien.");
                return false;
            }
            case "cv_indemnity" -> {
                if (!pay(p, 20)) return false;
                s.indemUntil = now + 120;
                p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1f, 1.4f);
                Fx.shield(p.serverLevel(), p, 0xF2C94C, 1.6f, 120);
                bar(p, "§6Indemnité : les blessures reçues (6 s) sont rayées du registre.");
                return true;
            }
            case "cv_escrow" -> {
                s.escrow = s.escrow >= 4 ? -1 : s.escrow + 1;
                bar(p, s.escrow < 0 ? "§7Aucune stat sous séquestre." : "§6Sous séquestre : " + STAT[s.escrow] + " (intouchable par les peines).");
                return false;
            }
            case "cv_arbitrate" -> {
                ServerPlayer t = playerAimed(p, 24);
                if (t == null || c.debt.getOrDefault(t.getUUID(), List.of()).isEmpty()) {
                    bar(p, "§7Vise quelqu'un qui a une dette.");
                    return false;
                }
                if (!pay(p, 20)) return false;
                ARB.put(t.getUUID(), p.getUUID());
                AbilityEngine.disable(t, 60);
                bar(p, "§6Arbitrage : son pouvoir est suspendu tant qu'il doit.");
                bar(t, "§cArbitrage : ton pouvoir est suspendu jusqu'au règlement de ta dette.");
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public static void stop(ServerPlayer p, String special) {}

    public static void whileOn(ServerPlayer p, AbilityPower.Move m, long now) {}

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

    // ------------------------------------------------------------------ tick
    public static void onLogout(UUID id) {
        PENDING.remove(id);
        OFFERS.remove(id);
        LAST_CHAT.remove(id);
        ARB.remove(id);
    }

    public static void tick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        // tribunaux des Souverains en ligne
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            Sov s = SOV.get(p.getUUID());
            boolean active = on(p);
            if (active && (s == null || s.court == null)) open(p);
            else if (!active && s != null && s.court != null) {
                close(p, server);
                bar(p, "§7Tribunal fermé.");
            }
        }
        // Souverains hors ligne : leur Tribunal se ferme
        for (Sov s : SOV.values()) {
            if (s.court != null && server.getPlayerList().getPlayer(s.court.owner) == null) {
                closeCourt(server, s.court, s);
                s.court = null;
            }
        }
        for (Sov s : SOV.values()) if (s.court != null) tickCourt(server, s.court, now);
        for (Court c : SEALED) tickCourt(server, c, now);
        // gages arrivés à terme
        Iterator<Coll> it = APPLIED.iterator();
        while (it.hasNext()) {
            Coll k = it.next();
            if (now < k.until) continue;
            it.remove();
            Sov s = SOV.get(k.by);
            if (s != null) s.stock.remove(k);
            ServerPlayer v = server.getPlayerList().getPlayer(k.from);
            if (v != null) bar(v, "§aTon gage te revient : " + k.pen.label() + ".");
        }
        if (now % 10 == 0) effects(server, now);
        if (now % 20 == 0) arbitration(server);
    }

    private static void tickCourt(MinecraftServer server, Court c, long now) {
        ServerPlayer owner = server.getPlayerList().getPlayer(c.owner);
        Sov so = sov(c.owner);
        if (c.follow && owner != null) {
            c.center = owner.position();
            c.dim = owner.serverLevel().dimension();
            c.tier = tierOf(levelOf(owner));
            c.radius = RADIUS[c.tier];
        }
        ServerLevel lv = server.getLevel(c.dim);
        if (lv == null) return;
        for (ServerPlayer v : server.getPlayerList().getPlayers()) {
            if (v.getUUID().equals(c.owner)) continue;
            if (v.isCreative() || v.isSpectator()) continue;
            boolean in = insideOf(c, v);
            boolean was = c.inside.contains(v.getUUID());
            if (in && !was) {
                if (c.exiled.contains(v.getUUID())) {
                    expel(c, v);
                    continue;
                }
                c.inside.add(v.getUUID());
                collectTerms(c, v);
                readLaws(v, c);
            } else if (!in && was) {
                c.inside.remove(v.getUUID());
                for (Viol x : new Viol[] {Viol.LEAVE}) {
                    c.inside.add(v.getUUID()); // la sortie se juge dedans
                    breach(v, x);
                    c.inside.remove(v.getUUID());
                }
            }
            if (in) {
                if (v.isSprinting()) breach(v, Viol.SPRINT);
                if (v.isShiftKeyDown()) breach(v, Viol.SNEAK);
                if (v.isFallFlying() || v.getAbilities().flying || Pal.soaring(v)) breach(v, Viol.FLY);
                if (now % 10 == 0) Vfx.glow(lv, Vfx.SPARKS, 0xF2C94C, v.position().add(0, 0.9, 0), new Vec3(0.15, 0.1, 0.15), 1, 0.01); // signature sur la main
            }
        }
        if (owner != null && c.follow) {
            if (now % 20 == 0) Vfx.glow(lv, Vfx.RING, 0xF2C94C, owner.position().add(0, 0.05, 0), new Vec3(3.2, 0, 0), 1, 0); // sceau d'or sous lui
            if (now % 100 == 0) Fx.wave(lv, owner.position(), Math.min(c.radius, 16), 0xF2C94C, 30);
            if (now % 10 == 0) moveParchments(lv, owner, c);
            if (so.proxy != null && now > so.proxyUntil) so.proxy = null;
            // dettes : une unité prélevée toutes les 10 s tant qu'on reste dans le Tribunal
            if (now % 200 == 0) {
                for (var e : new ArrayList<>(c.debt.entrySet())) {
                    ServerPlayer v = server.getPlayerList().getPlayer(e.getKey());
                    if (v == null || e.getValue().isEmpty() || !c.inside.contains(v.getUUID())) continue;
                    collectUnit(c, v, e.getValue().remove(0), false, false);
                }
            }
            // niveau 5 : le registre
            if (now % 40 == 0 && levelOf(owner) >= 5) registry(owner, c);
            // haut niveau : plus de 10 âmes, une loi du Tribunal s'applique à lui chaque jour
            long souls = so.stock.stream().filter(k -> k.pen.kind() == Kind.SOUL).count();
            if (souls > 10 && !c.laws.isEmpty()) {
                if (now >= so.daily) {
                    so.dailyLaw = c.laws.get(owner.getRandom().nextInt(c.laws.size()));
                    so.daily = now + 24000;
                    bar(owner, "§cTes âmes te lient : la loi #" + so.dailyLaw.id + " s'applique à toi aujourd'hui.");
                }
                if (now % 10 == 0) Vfx.glow(lv, Vfx.SPARKS, 0xF2C94C, owner.position().add(0, 0.9, 0), new Vec3(0.2, 0.1, 0.2), 2, 0.01);
            }
        }
    }

    private static void readLaws(ServerPlayer v, Court c) {
        List<String> shown = new ArrayList<>();
        for (LawRec l : c.laws) if (!l.hidden) shown.add("§e#" + l.id + " §f" + l.text);
        v.sendSystemMessage(Component.literal("§6Tu entres dans un Tribunal : tu as signé le Covenant."));
        for (String s : shown) v.sendSystemMessage(Component.literal(s));
        if (c.tier == 0) bar(v, "§7Tu as 3 secondes pour lire avant que les nouvelles lois comptent.");
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
        StringBuilder b = new StringBuilder("§6Tribunal §f" + c.inside.size() + " §6signataires");
        int shown = 0;
        for (var e : c.debt.entrySet()) {
            if (e.getValue().isEmpty() || shown >= 3) continue;
            ServerPlayer v = owner.server.getPlayerList().getPlayer(e.getKey());
            if (v == null) continue;
            b.append(shown == 0 ? " §7| §cEn infraction : " : " §7, ").append("§f").append(v.getGameProfile().getName()).append(" §7(doit ").append(e.getValue().size()).append(")");
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
                    else if (k.pen.key().equals("walk")) v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, true, false));
                }
                case SENSE -> {
                    if (k.pen.key().equals("sight")) v.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false));
                    else {
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
            if (v == null || s == null || s.court == null || s.court.debt.getOrDefault(v.getUUID(), List.of()).isEmpty()) {
                it.remove();
                if (v != null) bar(v, "§aTa dette est réglée : ton pouvoir revient.");
                continue;
            }
            AbilityEngine.disable(v, 60);
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
        if (now < s.indemUntil) { // Indemnité : la blessure est rayée et retombe sur son auteur
            e.setCanceled(true);
            if (e.getSource().getEntity() instanceof ServerPlayer by && s.court != null && by != p) {
                collectUnit(s.court, by, new Pen(Kind.STAT, String.valueOf(by.getRandom().nextInt(5))), false, true);
                s.court.breaches.merge(by.getUUID(), 1, Integer::sum);
            }
            return;
        }
        if (s.proxy != null && now < s.proxyUntil) { // Procuration : la blessure passe au lié
            ServerPlayer t = p.server.getPlayerList().getPlayer(s.proxy);
            if (t != null && t.isAlive()) {
                e.setCanceled(true);
                t.hurt(t.damageSources().magic(), e.getAmount());
            }
        }
    }
}
