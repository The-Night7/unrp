package com.myriam.unrp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.myriam.unrp.data.PlayerData;
import com.myriam.unrp.data.UnrpState;
import com.myriam.unrp.net.Net;
import com.myriam.unrp.power.Power;
import com.myriam.unrp.power.Powers;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * /unrp info [joueur]
 * /unrp power list | set <joueur> <pouvoir> | roll <joueur> | clear <joueur>
 * /unrp aura set|add <joueur> <valeur>
 * /unrp stat set <joueur> <stat> <valeur>
 */
public final class UnrpCommands {
    private UnrpCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("unrp")
                .then(literal("info")
                        .executes(ctx -> info(ctx.getSource(), ctx.getSource().getPlayerOrThrow()))
                        .then(argument("player", EntityArgumentType.player())
                                .requires(s -> s.hasPermissionLevel(2))
                                .executes(ctx -> info(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))))
                .then(literal("power").requires(s -> s.hasPermissionLevel(2))
                        .then(literal("list").executes(ctx -> {
                            ctx.getSource().sendFeedback(() -> Text.translatable(
                                    "unrp.cmd.power_list", String.join(", ", Powers.ids())), false);
                            return 1;
                        }))
                        .then(literal("set").then(argument("player", EntityArgumentType.player())
                                .then(argument("power", StringArgumentType.word())
                                        .suggests((c, b) -> CommandSource.suggestMatching(Powers.ids(), b))
                                        .executes(ctx -> setPower(ctx.getSource(),
                                                EntityArgumentType.getPlayer(ctx, "player"),
                                                StringArgumentType.getString(ctx, "power"))))))
                        .then(literal("roll").then(argument("player", EntityArgumentType.player())
                                .executes(ctx -> setPower(ctx.getSource(),
                                        EntityArgumentType.getPlayer(ctx, "player"), Powers.randomId()))))
                        .then(literal("clear").then(argument("player", EntityArgumentType.player())
                                .executes(ctx -> clearPower(ctx.getSource(),
                                        EntityArgumentType.getPlayer(ctx, "player"))))))
                .then(literal("aura").requires(s -> s.hasPermissionLevel(2))
                        .then(literal("set").then(argument("player", EntityArgumentType.player())
                                .then(argument("value", IntegerArgumentType.integer(0))
                                        .executes(ctx -> setAura(ctx.getSource(),
                                                EntityArgumentType.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "value"), false)))))
                        .then(literal("add").then(argument("player", EntityArgumentType.player())
                                .then(argument("value", IntegerArgumentType.integer())
                                        .executes(ctx -> setAura(ctx.getSource(),
                                                EntityArgumentType.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "value"), true))))))
                .then(literal("stat").requires(s -> s.hasPermissionLevel(2))
                        .then(literal("set").then(argument("player", EntityArgumentType.player())
                                .then(argument("stat", StringArgumentType.word())
                                        .suggests((c, b) -> CommandSource.suggestMatching(java.util.Arrays.asList(PlayerData.STAT_NAMES), b))
                                        .then(argument("value", IntegerArgumentType.integer(
                                                PlayerData.STAT_MIN, PlayerData.STAT_MAX))
                                                .executes(ctx -> setStat(ctx.getSource(),
                                                        EntityArgumentType.getPlayer(ctx, "player"),
                                                        StringArgumentType.getString(ctx, "stat"),
                                                        IntegerArgumentType.getInteger(ctx, "value")))))))));
    }

    private static int info(ServerCommandSource source, ServerPlayerEntity target) throws CommandSyntaxException {
        PlayerData d = UnrpState.get(source.getServer()).get(target.getUuid());
        Power power = Powers.get(d.power);
        Text powerName = power == null ? Text.translatable("unrp.power.none") : power.displayName();
        source.sendFeedback(() -> Text.translatable("unrp.cmd.info",
                target.getName(), powerName, d.aura, d.maxAura(),
                d.stats[0], d.stats[1], d.stats[2], d.stats[3], d.stats[4]), false);
        return 1;
    }

    private static int setPower(ServerCommandSource source, ServerPlayerEntity target, String id) {
        Power power = Powers.get(id);
        if (power == null) {
            source.sendError(Text.translatable("unrp.cmd.unknown_power", id));
            return 0;
        }
        UnrpState state = UnrpState.get(source.getServer());
        PlayerData d = state.get(target.getUuid());
        d.power = id;
        d.aura = d.maxAura();
        d.cooldownUntil = 0;
        state.markDirty();
        Net.sync(target, d);
        source.sendFeedback(() -> Text.translatable("unrp.cmd.power_set", target.getName(), power.displayName()), true);
        return 1;
    }

    private static int clearPower(ServerCommandSource source, ServerPlayerEntity target) {
        UnrpState state = UnrpState.get(source.getServer());
        PlayerData d = state.get(target.getUuid());
        d.power = "";
        state.markDirty();
        Net.sync(target, d);
        source.sendFeedback(() -> Text.translatable("unrp.cmd.power_clear", target.getName()), true);
        return 1;
    }

    private static int setAura(ServerCommandSource source, ServerPlayerEntity target, int value, boolean add) {
        UnrpState state = UnrpState.get(source.getServer());
        PlayerData d = state.get(target.getUuid());
        d.aura = Math.max(0, Math.min(d.maxAura(), add ? d.aura + value : value));
        state.markDirty();
        Net.sync(target, d);
        source.sendFeedback(() -> Text.translatable("unrp.cmd.aura_set", target.getName(), d.aura, d.maxAura()), true);
        return 1;
    }

    private static int setStat(ServerCommandSource source, ServerPlayerEntity target, String stat, int value) {
        int index = PlayerData.statIndex(stat);
        if (index < 0) {
            source.sendError(Text.translatable("unrp.cmd.unknown_stat", stat));
            return 0;
        }
        UnrpState state = UnrpState.get(source.getServer());
        PlayerData d = state.get(target.getUuid());
        d.stats[index] = value;
        d.aura = Math.min(d.aura, d.maxAura());
        state.markDirty();
        Net.sync(target, d);
        source.sendFeedback(() -> Text.translatable("unrp.cmd.stat_set", target.getName(), stat, value), true);
        return 1;
    }
}
