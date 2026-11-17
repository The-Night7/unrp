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

public final class UnrpCommands {
    private static final Random RNG = new Random();
    private static final Predicate<CommandSourceStack> ADMIN = s -> s.hasPermission(2);

    private UnrpCommands() {}

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> covenantWritingCommands() {
        var root = Commands.literal("covenant");
        for (String action : java.util.List.of("law", "pact", "terms", "constitution", "retroactive", "forfeit", "tribute", "jurisdiction", "message", "sovereign_pact")) {
            var entry=Commands.literal(action);
            if(action.equals("law"))entry.executes(ctx->com.myriam.unrp.ability.Specials17.openLawEditor(ctx.getSource().getPlayerOrException())?1:0);
            entry.then(Commands.argument("text", StringArgumentType.greedyString())
                    .executes(ctx -> com.myriam.unrp.ability.Specials17.submitWriting(
                            ctx.getSource().getPlayerOrException(), action, StringArgumentType.getString(ctx, "text")) ? 1 : 0));
            root.then(entry);
        }
        root.then(Commands.literal("claim")
                .executes(ctx -> com.myriam.unrp.ability.Specials17.openTerritoryEditor(ctx.getSource().getPlayerOrException()) ? 1 : 0)
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> com.myriam.unrp.ability.Specials17.claimTerritory(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name")) ? 1 : 0)));
        root.then(Commands.literal("territory").then(Commands.argument("name", StringArgumentType.greedyString())
                .executes(ctx -> com.myriam.unrp.ability.Specials17.selectTerritory(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name")) ? 1 : 0)));
        root.then(Commands.literal("unclaim").then(Commands.argument("name", StringArgumentType.greedyString())
                .executes(ctx -> com.myriam.unrp.ability.Specials17.releaseTerritory(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name")) ? 1 : 0)));
        root.then(Commands.literal("territories").executes(ctx -> {
            com.myriam.unrp.ability.Specials17.listTerritories(ctx.getSource().getPlayerOrException()); return 1;
        }));
        root.then(Commands.literal("personal").executes(ctx -> {
            com.myriam.unrp.ability.Specials17.selectPersonalCourt(ctx.getSource().getPlayerOrException()); return 1;
        }));
        root.then(Commands.literal("cancel").executes(ctx -> {
            com.myriam.unrp.ability.Specials17.cancelWriting(ctx.getSource().getPlayerOrException()); return 1;
        }));
        return root;
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("unrp")
                .then(Commands.literal("menu").executes(ctx -> menu(ctx.getSource())))
                .then(covenantWritingCommands())
                .then(LevelCommands.tree())
                .then(Commands.literal("recoverybed")
                        .then(Commands.literal("rate").then(Commands.argument("percent_per_second",com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(.1,20))
                                .executes(ctx->com.myriam.unrp.item.RecoveryBeds.commandRate(ctx.getSource().getPlayerOrException(),com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx,"percent_per_second")))))
                        .then(Commands.literal("cap").then(Commands.argument("percent",com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg(1,100))
                                .executes(ctx->com.myriam.unrp.item.RecoveryBeds.commandCeiling(ctx.getSource().getPlayerOrException(),com.mojang.brigadier.arguments.DoubleArgumentType.getDouble(ctx,"percent"))))))
                .then(Commands.literal("undress").executes(ctx -> {
                    var p = ctx.getSource().getPlayerOrException();
                    com.myriam.unrp.item.Cosmetics.clear(p);
                    ctx.getSource().sendSuccess(() -> Component.literal("All cosmetic clothing removed."), false);
                    return 1;
                }))
                .then(Commands.literal("codex").executes(ctx -> {
                    com.myriam.unrp.uo.HollowMarkEvents.openCodex(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("whoami").executes(ctx -> whoami(ctx.getSource())))

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
                        .then(Commands.literal("locate")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(com.myriam.unrp.worldgen.SchoolSites.SCHOOLS.stream().map(com.myriam.unrp.worldgen.SchoolSites.School::id), b))
                                        .executes(ctx -> locateSchool(ctx.getSource(), StringArgumentType.getString(ctx, "id")))))
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
        if (table == null) return fail(src, "rarity.json is not loaded.");
        RarityTable.Roll r = table.roll(RNG);
        return say(src, "Potential rolled: " + r.tier.label + " (level " + r.level + ")"
                + (r.tier.lifelong ? " - permanently without an ability" : ""));
    }

    private static int rollTest(CommandSourceStack src, int n) {
        RarityTable table = UnodexData.get().rarity;
        if (table == null) return fail(src, "rarity.json is not loaded.");
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

    private static int locateSchool(CommandSourceStack source, String id) {
        com.myriam.unrp.worldgen.SchoolSites.School school;
        try { school = com.myriam.unrp.worldgen.SchoolSites.school(id); }
        catch (IllegalArgumentException ex) { source.sendFailure(Component.literal(ex.getMessage())); return 0; }
        var overworld = source.getServer().overworld();
        var site = com.myriam.unrp.worldgen.SchoolSites.site(overworld.getSeed(), school);
        source.sendSuccess(() -> Component.literal(school.name() + " campus site: Overworld X " + site.centerX()
                + ", Z " + site.centerZ() + ". Generated when its terrain is first created; no retroactive placement in existing chunks."), false);
        return 1;
    }

    private static int schoolList(CommandSourceStack src) {
        UnodexData data = UnodexData.get();
        if (data.schools.isEmpty()) return fail(src, "No schools loaded.");
        StringBuilder sb = new StringBuilder("Schools and groups (" + data.schools.size() + ") :");
        for (Models.School s : data.schools.values()) {
            sb.append(String.format("%n  %s - %s [%s] %d members", s.id, s.name, s.kind, s.memberCount));
        }
        return say(src, sb.toString());
    }

    private static int schoolInfo(CommandSourceStack src, String id) {
        Models.School s = UnodexData.get().schools.get(id);
        if (s == null) return fail(src, "Unknown school: " + id);
        String leaders = s.leaders.isEmpty() ? "-"
                : s.leaders.stream().map(l -> l.characterId + " (" + l.role + ")").collect(Collectors.joining(", "));
        return say(src, s.name + " (" + s.kind + (s.kindConfirmed ? "" : ", unconfirmed") + ")"
                + "\n  Dirigeants : " + leaders
                + "\n  Members (" + s.memberCount + ") : " + String.join(", ", s.members.stream().limit(15).toList())
                + (s.memberCount > 15 ? ", ..." : ""));
    }

    // ----------------------------------------------------------- characters
    private static int characterList(CommandSourceStack src, String category) {
        UnodexData data = UnodexData.get();
        ClaimData claims = ClaimData.get(src.getServer());
        var list = data.characters.values().stream()
                .filter(c -> category == null || category.equals(c.category)).toList();
        if (list.isEmpty()) return fail(src, "No characters" + (category == null ? "" : " in '" + category + "'") + ".");
        long free = list.stream().filter(c -> !claims.isClaimed(c.id)).count();
        String names = list.stream().limit(40)
                .map(c -> c.id + (claims.isClaimed(c.id) ? "*" : ""))
                .collect(Collectors.joining(", "));
        return say(src, list.size() + " characters (" + free + " libres, * = pris) : " + names
                + (list.size() > 40 ? ", ..." : ""));
    }

    private static int characterInfo(CommandSourceStack src, String id) {
        Models.Character c = UnodexData.get().characters.get(id);
        if (c == null) return fail(src, "Unknown character: " + id);
        ClaimData claims = ClaimData.get(src.getServer());
        String owner = claims.ownerOf(id).map(u -> {
            ServerPlayer p = src.getServer().getPlayerList().getPlayer(u);
            return p != null ? p.getGameProfile().getName() : u.toString();
        }).orElse("available");
        return say(src, c.name + " [" + c.category + "] " + c.tier + " niv. " + c.level
                + "\n  Ability: " + (c.abilityId == null ? "none" : c.abilityId)
                + "\n  Schools: " + c.schools.stream().map(m -> m.school + (m.roles.isEmpty() ? "" : " " + m.roles)).toList()
                + "\n  Statut : " + owner);
    }

    private static int claim(CommandSourceStack src, String id, ServerPlayer player) {
        if (!UnodexData.get().characters.containsKey(id)) return fail(src, "Unknown character: " + id);
        if (!ClaimData.get(src.getServer()).forceClaim(id, player.getUUID())) return fail(src, id + " is already claimed.");
        PlayerStateService.send(player);
        return say(src, player.getGameProfile().getName() + " joue maintenant " + id);
    }

    private static int unclaim(CommandSourceStack src, String id) {
        if (!ClaimData.get(src.getServer()).release(id)) return fail(src, id + " was not claimed.");
        return say(src, id + " is available again.");
    }

    // ------------------------------------------------------- joueurs
    private static int reputation(CommandSourceStack src, java.util.Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            int value = com.myriam.unrp.social.Reputation.rep(player);
            String standing = com.myriam.unrp.social.Reputation.standingOf(value);
            String whose = player == src.getEntity() ? "Your reputation" : "Reputation of " + player.getDisplayName().getString();
            src.sendSuccess(() -> Component.literal("§7" + whose + " : §f" + standing + " §8(" + value + ")"), false);
        }
        return targets.size();
    }

    private static int menu(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        if (ClaimData.get(src.getServer()).profileOf(player.getUUID()).isPresent()) {
            return fail(src, "You already have a character (/unrp whoami). An administrator can reset it.");
        }
        MenuService.send(player, true, false, "");
        return 1;
    }

    private static int whoami(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        var profile = ClaimData.get(src.getServer()).profileOf(player.getUUID());
        if (profile.isEmpty()) return say(src, "You have no character yet: /unrp menu");
        ClaimData.Profile p = profile.get();
        if (ClaimData.Profile.CHARACTER.equals(p.kind)) {
            Models.Character c = UnodexData.get().characters.get(p.characterId);
            return say(src, "You play " + (c != null ? c.name : p.characterId) + ".");
        }
        return say(src, "OC " + p.ocName + " (" + p.ocSchool + ") - potential " + p.tier + " level " + p.level
                + (p.lifelong ? " - permanently without an ability" : ""));
    }

    private static int reset(CommandSourceStack src, ServerPlayer target) {
        if (!ClaimData.get(src.getServer()).reset(target.getUUID())) {
            return fail(src, target.getGameProfile().getName() + " had no character.");
        }
        PlayerStateService.send(target);
        MenuService.send(target, true, false, "Your character was reset. Choose a new one.");
        return say(src, target.getGameProfile().getName() + " has been reset.");
    }

    private static int unlockAll(CommandSourceStack src, net.minecraft.server.level.ServerPlayer p, boolean on) {
        com.myriam.unrp.ability.Unlocks.setAll(p, on);
        com.myriam.unrp.player.PlayerStateService.send(p);
        src.sendSuccess(() -> Component.literal((on ? "All ranks unlocked for " : "Ranks locked again for ") + p.getName().getString()), true);
        return 1;
    }

    private static int rollAbility(CommandSourceStack src, net.minecraft.server.level.ServerPlayer p) {
        ClaimData claims = ClaimData.get(src.getServer());
        ClaimData.Profile pr = claims.profileOf(p.getUUID()).orElse(null);
        if (pr == null || !ClaimData.Profile.OC.equals(pr.kind) || pr.lifelong) {
            src.sendFailure(Component.literal("This player has no OC eligible to receive an ability."));
            return 0;
        }
        String id = claims.rollFreeAbility(p.getUUID(), new java.util.Random());
        if (id == null) {
            src.sendFailure(Component.literal("No free abilities remain. All are held by other players."));
            return 0;
        }
        claims.setOcAbility(p.getUUID(), id);
        com.myriam.unrp.player.PlayerStateService.send(p);
        src.sendSuccess(() -> Component.literal("Ability rolled for " + p.getName().getString() + " : " + id), true);
        return 1;
    }

    private static int giveAbility(CommandSourceStack src, net.minecraft.server.level.ServerPlayer p, String id) {
        ClaimData claims = ClaimData.get(src.getServer());
        ClaimData.Profile pr = claims.profileOf(p.getUUID()).orElse(null);
        if (pr == null || !ClaimData.Profile.OC.equals(pr.kind) || pr.lifelong) {
            src.sendFailure(Component.literal("This player has no OC eligible to receive an ability."));
            return 0;
        }
        var data = com.myriam.unrp.data.UnodexData.get();
        if (data.powerFor(id) == null) {
            src.sendFailure(Component.literal("Unknown or unimplemented power: " + id));
            return 0;
        }
        if (claims.heldAbilities(pr).contains(data.canon(id))) {
            src.sendFailure(Component.literal("That ability is already held by another player."));
            return 0;
        }
        claims.setOcAbility(p.getUUID(), id);
        com.myriam.unrp.player.PlayerStateService.send(p);
        src.sendSuccess(() -> Component.literal("Ability assigned to " + p.getName().getString() + " : " + id), true);
        return 1;
    }
}
