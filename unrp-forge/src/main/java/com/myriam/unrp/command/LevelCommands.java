package com.myriam.unrp.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.myriam.unrp.data.ClaimData;
import com.myriam.unrp.data.UnodexData;
import com.myriam.unrp.player.PlayerStateService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.Locale;

/** Operator commands for OC ability level, distinct from Minecraft experience levels. */
final class LevelCommands {
    private LevelCommands() {}

    static LiteralArgumentBuilder<CommandSourceStack> tree() {
        var root=Commands.literal("level").requires(s->s.hasPermission(2));
        root.then(Commands.literal("get")
                .executes(c->get(c.getSource(),c.getSource().getPlayerOrException()))
                .then(Commands.argument("player",EntityArgument.player())
                        .executes(c->get(c.getSource(),EntityArgument.getPlayer(c,"player")))));
        for (String action : new String[]{"set","add"}) {
            boolean add=action.equals("add");
            root.then(Commands.literal(action)
                    .then(Commands.argument("value",add?DoubleArgumentType.doubleArg(-9,9):DoubleArgumentType.doubleArg(1,10))
                            .executes(c->change(c.getSource(),c.getSource().getPlayerOrException(),DoubleArgumentType.getDouble(c,"value"),add))
                            .then(Commands.argument("player",EntityArgument.player())
                                    .executes(c->change(c.getSource(),EntityArgument.getPlayer(c,"player"),DoubleArgumentType.getDouble(c,"value"),add)))));
        }
        return root;
    }

    private static int get(CommandSourceStack source,ServerPlayer player) {
        var profile=ClaimData.get(source.getServer()).profileOf(player.getUUID()).orElse(null);
        if(profile==null)return fail(source,"This player has no character. Use /unrp menu first.");
        double level=profile.level;
        if(ClaimData.Profile.CHARACTER.equals(profile.kind)) {
            var character=UnodexData.get().characters.get(profile.characterId);
            if(character!=null && character.level!=null)level=character.level;
        }
        String text=String.format(Locale.ROOT,"%s: ability level %.1f",player.getGameProfile().getName(),level);
        source.sendSuccess(()->Component.literal(text),false);
        return 1;
    }

    private static int change(CommandSourceStack source,ServerPlayer player,double value,boolean add) {
        var claims=ClaimData.get(source.getServer());
        var profile=claims.profileOf(player.getUUID()).orElse(null);
        double level;
        try {level=apply(profile,value,add);} catch(IllegalArgumentException ex) {return fail(source,ex.getMessage());}
        claims.setDirty();
        // A lower level can reduce maximum aura. Clamp stored aura before the immediate sync.
        int max=PlayerStateService.build(player).maxAura;
        if(profile.aura>max)profile.aura=max;
        PlayerStateService.send(player);
        String text=String.format(Locale.ROOT,"%s's ability level is now %.1f.",player.getGameProfile().getName(),level);
        source.sendSuccess(()->Component.literal(text),true);
        return 1;
    }

    /** Validate before mutating; preserve potential, ability ownership and trained bonuses. */
    static double apply(ClaimData.Profile profile,double value,boolean add) {
        if(profile==null)throw new IllegalArgumentException("This player has no character. Use /unrp menu first.");
        if(!ClaimData.Profile.OC.equals(profile.kind))throw new IllegalArgumentException("Unodex characters have fixed levels. This command modifies OC levels only.");
        if(profile.lifelong)throw new IllegalArgumentException("This OC is permanently powerless and cannot change ability level.");
        double requested=add?profile.level+value:value;
        if(!Double.isFinite(value)||!Double.isFinite(requested)||requested<1||requested>10)
            throw new IllegalArgumentException("The resulting ability level must be between 1.0 and 10.0.");
        profile.level=Math.round(requested*10.0)/10.0;
        profile.masteryXp=0;
        return profile.level;
    }

    private static int fail(CommandSourceStack source,String message) {
        source.sendFailure(Component.literal(message));return 0;
    }
}
