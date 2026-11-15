package com.myriam.unrp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CovenantWritingCommandTest {
    @SuppressWarnings("unchecked")
    @Test void ruleAndContractTextSurvivesTheRealCommandGrammar() throws Exception {
        var factory = UnrpCommands.class.getDeclaredMethod("covenantWritingCommands");
        factory.setAccessible(true);
        var dispatcher = new CommandDispatcher<CommandSourceStack>();
        dispatcher.register((LiteralArgumentBuilder<CommandSourceStack>) factory.invoke(null));
        for (String[] example : new String[][] {
                {"law", "No one may run. Penalty: their Speed"},
                {"terms", "Whoever enters gives up their weapons"},
                {"constitution", "No attacking. Penalty: whole ability"},
                {"sovereign_pact", "PlayerName | nothing for your branch"},
                {"message", "PlayerName | You owe me Speed"}}) {
            var result = dispatcher.parse("covenant " + example[0] + " " + example[1], null);
            assertTrue(result.getExceptions().isEmpty());
            assertFalse(result.getReader().canRead());
            assertEquals(example[1], result.getContext().build("").getArgument("text", String.class));
        }
    }
}
