package com.myriam.unrp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.data.Models;
import com.myriam.unrp.data.RarityTable;
import com.myriam.unrp.data.RarityTier;
import com.myriam.unrp.data.UnodexData;
import com.myriam.unrp.menu.MenuService;
import com.myriam.unrp.player.PlayerStateService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Commandes d'administration (niveau 2) :
 *   /unrp roll                          tire un potentiel pour soi
 *   /unrp rolltest <n>                  simule n tirages et compare aux probabilités attendues
 *   /unrp school list | info <id>
 *   /unrp character list [categorie] | info <id> | claim <id> <joueur> | unclaim <id>
 * Les joueurs choisiront leur personnage via le menu (phase 2), pas par commande.
 */
public final class UnrpCommands {
    private static final Random RNG = new Random();
    private static final Predicate<CommandSourceStack> ADMIN = s -> s.hasPermission(2);

    private UnrpCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("unrp")
                .then(Commands.literal("menu").executes(ctx -> menu(ctx.getSource())))
                .then(Commands.literal("undress").executes(ctx -> {
                    var p = ctx.getSource().getPlayerOrException();
                    com.myriam.unrp.item.Cosmetics.clear(p);
                    ctx.getSource().sendSuccess(() -> Component.literal("Tous tes vêtements cosmétiques sont retirés."), false);
                    return 1;
                }))
                .then(Commands.literal("codex").executes(ctx -> {
                    com.myriam.unrp.uo.HollowMarkEvents.openCodex(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("whoami").executes(ctx -> whoami(ctx.getSource())))
                // ReputationCommands du modèle : /unrp reputation [cibles]
                .then(Commands.literal("reputation").requires(ADMIN)
                        .executes(ctx -> reputation(ctx.getSource(), java.util.List.of(ctx.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> reputation(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                .then(Commands.literal("reset").requires(ADMIN)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> reset(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("rollability").requires(ADMIN)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> rollAbility(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("giveability").requires(ADMIN)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("id", com.mojang.brigadier.arguments.StringArgumentType.word())
                                        .executes(ctx -> giveAbility(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "id"))))))
                .then(Commands.literal("unlock").requires(ADMIN)
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("all").executes(ctx -> unlockAll(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), true)))
                                .then(Commands.literal("reset").executes(ctx -> unlockAll(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), false)))))
                .then(Commands.literal("roll").requires(ADMIN).executes(ctx -> roll(ctx.getSource())))
                .then(Commands.literal("rolltest").requires(ADMIN)
                        .then(Commands.argument("n", IntegerArgumentType.integer(1, 5_000_000))
                                .executes(ctx -> rollTest(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "n")))))
                .then(Commands.literal("school").requires(ADMIN)
                        .then(Commands.literal("list").executes(ctx -> schoolList(ctx.getSource())))
                        .then(Commands.literal("info")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(UnodexData.get().schools.keySet(), b))
                                        .executes(ctx -> schoolInfo(ctx.getSource(), StringArgumentType.getString(ctx, "id"))))))
                .then(Commands.literal("character").requires(ADMIN)
                        .then(Commands.literal("list")
                                .executes(ctx -> characterList(ctx.getSource(), null))
                                .then(Commands.argument("category", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                                new String[]{"lore", "rp"}, b))
                                        .executes(ctx -> characterList(ctx.getSource(), StringArgumentType.getString(ctx, "category")))))
                        .then(Commands.literal("info")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(UnodexData.get().characters.keySet(), b))
                                        .executes(ctx -> characterInfo(ctx.getSource(), StringArgumentType.getString(ctx, "id")))))
                        .then(Commands.literal("claim")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(UnodexData.get().characters.keySet(), b))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> claim(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "id"),
                                                        EntityArgument.getPlayer(ctx, "player"))))))
                        .then(Commands.literal("unclaim")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> unclaim(ctx.getSource(), StringArgumentType.getString(ctx, "id")))))));
    }

    private static int fail(CommandSourceStack src, String msg) {
        src.sendFailure(Component.literal(msg));
        return 0;
    }

    private static int say(CommandSourceStack src, String msg) {
        src.sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }

    // ------------------------------------------------------------------ roll
    private static int roll(CommandSourceStack src) {
        RarityTable table = UnodexData.get().rarity;
        if (table == null) return fail(src, "rarity.json non chargé.");
        RarityTable.Roll r = table.roll(RNG);
        return say(src, "Potentiel tiré : " + r.tier.label + " (niveau " + r.level + ")"
                + (r.tier.lifelong ? " - ne pourra jamais avoir de capacité" : ""));
    }

    private static int rollTest(CommandSourceStack src, int n) {
        RarityTable table = UnodexData.get().rarity;
        if (table == null) return fail(src, "rarity.json non chargé.");
        Map<String, Integer> counts = new LinkedHashMap<>();
        table.tiers().forEach(t -> counts.put(t.id, 0));
        for (int i = 0; i < n; i++) counts.merge(table.roll(RNG).tier.id, 1, Integer::sum);
        StringBuilder sb = new StringBuilder("Simulation de " + n + " tirages (obtenu / attendu) :");
        for (RarityTier t : table.tiers()) {
            sb.append(String.format("%n  %s : %.3f %% / %.3f %%", t.label,
                    100.0 * counts.get(t.id) / n, 100.0 * table.probability(t)));
        }
        return say(src, sb.toString());
    }

    // ---------------------------------------------------------------- écoles
    private static int schoolList(CommandSourceStack src) {
        UnodexData data = UnodexData.get();
        if (data.schools.isEmpty()) return fail(src, "Aucune école chargée.");
        StringBuilder sb = new StringBuilder("Écoles et groupes (" + data.schools.size() + ") :");
        for (Models.School s : data.schools.values()) {
            sb.append(String.format("%n  %s - %s [%s] %d membres", s.id, s.name, s.kind, s.memberCount));
        }
        return say(src, sb.toString());
    }

    private static int schoolInfo(CommandSourceStack src, String id) {
        Models.School s = UnodexData.get().schools.get(id);
        if (s == null) return fail(src, "École inconnue : " + id);
        String leaders = s.leaders.isEmpty() ? "-"
                : s.leaders.stream().map(l -> l.characterId + " (" + l.role + ")").collect(Collectors.joining(", "));
        return say(src, s.name + " (" + s.kind + (s.kindConfirmed ? "" : ", à confirmer") + ")"
                + "\n  Dirigeants : " + leaders
                + "\n  Membres (" + s.memberCount + ") : " + String.join(", ", s.members.stream().limit(15).toList())
                + (s.memberCount > 15 ? ", ..." : ""));
    }

    // ----------------------------------------------------------- personnages
    private static int characterList(CommandSourceStack src, String category) {
        UnodexData data = UnodexData.get();
        ClaimData claims = ClaimData.get(src.getServer());
        var list = data.characters.values().stream()
                .filter(c -> category == null || category.equals(c.category)).toList();
        if (list.isEmpty()) return fail(src, "Aucun personnage" + (category == null ? "" : " dans '" + category + "'") + ".");
        long free = list.stream().filter(c -> !claims.isClaimed(c.id)).count();
        String names = list.stream().limit(40)
                .map(c -> c.id + (claims.isClaimed(c.id) ? "*" : ""))
                .collect(Collectors.joining(", "));
        return say(src, list.size() + " personnages (" + free + " libres, * = pris) : " + names
                + (list.size() > 40 ? ", ..." : ""));
    }

    private static int characterInfo(CommandSourceStack src, String id) {
        Models.Character c = UnodexData.get().characters.get(id);
        if (c == null) return fail(src, "Personnage inconnu : " + id);
        ClaimData claims = ClaimData.get(src.getServer());
        String owner = claims.ownerOf(id).map(u -> {
            ServerPlayer p = src.getServer().getPlayerList().getPlayer(u);
            return p != null ? p.getGameProfile().getName() : u.toString();
        }).orElse("libre");
        return say(src, c.name + " [" + c.category + "] " + c.tier + " niv. " + c.level
                + "\n  Capacité : " + (c.abilityId == null ? "aucune" : c.abilityId)
                + "\n  Écoles : " + c.schools.stream().map(m -> m.school + (m.roles.isEmpty() ? "" : " " + m.roles)).toList()
                + "\n  Statut : " + owner);
    }

    private static int claim(CommandSourceStack src, String id, ServerPlayer player) {
        if (!UnodexData.get().characters.containsKey(id)) return fail(src, "Personnage inconnu : " + id);
        if (!ClaimData.get(src.getServer()).forceClaim(id, player.getUUID())) return fail(src, id + " est déjà pris.");
        PlayerStateService.send(player);
        return say(src, player.getGameProfile().getName() + " joue maintenant " + id);
    }

    private static int unclaim(CommandSourceStack src, String id) {
        if (!ClaimData.get(src.getServer()).release(id)) return fail(src, id + " n'était pas pris.");
        return say(src, id + " est de nouveau libre.");
    }

    // ------------------------------------------------------- joueurs
    private static int reputation(CommandSourceStack src, java.util.Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            int value = com.myriam.unrp.social.Reputation.rep(player);
            String standing = com.myriam.unrp.social.Reputation.standingOf(value);
            String whose = player == src.getEntity() ? "Ta réputation" : "Réputation de " + player.getDisplayName().getString();
            src.sendSuccess(() -> Component.literal("§7" + whose + " : §f" + standing + " §8(" + value + ")"), false);
        }
        return targets.size();
    }

    private static int menu(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        if (ClaimData.get(src.getServer()).profileOf(player.getUUID()).isPresent()) {
            return fail(src, "Tu as déjà un personnage (/unrp whoami). Un admin peut le réinitialiser.");
        }
        MenuService.send(player, true, false, "");
        return 1;
    }

    private static int whoami(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        var profile = ClaimData.get(src.getServer()).profileOf(player.getUUID());
        if (profile.isEmpty()) return say(src, "Tu n'as pas encore de personnage : /unrp menu");
        ClaimData.Profile p = profile.get();
        if (ClaimData.Profile.CHARACTER.equals(p.kind)) {
            Models.Character c = UnodexData.get().characters.get(p.characterId);
            return say(src, "Tu joues " + (c != null ? c.name : p.characterId) + ".");
        }
        return say(src, "OC " + p.ocName + " (" + p.ocSchool + ") - potentiel " + p.tier + " niveau " + p.level
                + (p.lifelong ? " - jamais de capacité" : ""));
    }

    private static int reset(CommandSourceStack src, ServerPlayer target) {
        if (!ClaimData.get(src.getServer()).reset(target.getUUID())) {
            return fail(src, target.getGameProfile().getName() + " n'avait pas de personnage.");
        }
        PlayerStateService.send(target);
        MenuService.send(target, true, false, "Ton personnage a été réinitialisé : choisis-en un nouveau.");
        return say(src, target.getGameProfile().getName() + " est réinitialisé.");
    }

    private static int unlockAll(CommandSourceStack src, net.minecraft.server.level.ServerPlayer p, boolean on) {
        com.myriam.unrp.ability.Unlocks.setAll(p, on);
        com.myriam.unrp.player.PlayerStateService.send(p);
        src.sendSuccess(() -> Component.literal((on ? "Tous les rangs débloqués pour " : "Rangs reverrouillés pour ") + p.getName().getString()), true);
        return 1;
    }

    private static int rollAbility(CommandSourceStack src, net.minecraft.server.level.ServerPlayer p) {
        ClaimData claims = ClaimData.get(src.getServer());
        ClaimData.Profile pr = claims.profileOf(p.getUUID()).orElse(null);
        if (pr == null || !ClaimData.Profile.OC.equals(pr.kind) || pr.lifelong) {
            src.sendFailure(Component.literal("Ce joueur n'a pas d'OC pouvant recevoir une capacité."));
            return 0;
        }
        String id = claims.rollFreeAbility(p.getUUID(), new java.util.Random());
        if (id == null) {
            src.sendFailure(Component.literal("Aucune capacité libre : elles sont toutes détenues."));
            return 0;
        }
        claims.setOcAbility(p.getUUID(), id);
        com.myriam.unrp.player.PlayerStateService.send(p);
        src.sendSuccess(() -> Component.literal("Capacité tirée pour " + p.getName().getString() + " : " + id), true);
        return 1;
    }

    /** Attribue une capacité précise (y compris les Origin, jamais tirées au roll) à un OC. Une capacité reste unique. */
    private static int giveAbility(CommandSourceStack src, net.minecraft.server.level.ServerPlayer p, String id) {
        ClaimData claims = ClaimData.get(src.getServer());
        ClaimData.Profile pr = claims.profileOf(p.getUUID()).orElse(null);
        if (pr == null || !ClaimData.Profile.OC.equals(pr.kind) || pr.lifelong) {
            src.sendFailure(Component.literal("Ce joueur n'a pas d'OC pouvant recevoir une capacité."));
            return 0;
        }
        var data = com.myriam.unrp.data.UnodexData.get();
        if (data.powerFor(id) == null) {
            src.sendFailure(Component.literal("Pouvoir inconnu ou pas encore jouable : " + id));
            return 0;
        }
        if (claims.heldAbilities(pr).contains(data.canon(id))) {
            src.sendFailure(Component.literal("Cette capacité est déjà détenue par quelqu'un."));
            return 0;
        }
        claims.setOcAbility(p.getUUID(), id);
        com.myriam.unrp.player.PlayerStateService.send(p);
        src.sendSuccess(() -> Component.literal("Capacité attribuée à " + p.getName().getString() + " : " + id), true);
        return 1;
    }
}
