package com.myriam.unrp.ability;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CovenantSpecificRulesTest {
    @Test void bothMenuRulesRoundTripThroughTheServerParser() {
        for(var rule:new CovenantSpecificRules.Rule[]{
                new CovenantSpecificRules.Rule(CovenantSpecificRules.Kind.SIGNED_CONTRACTS,"*"),
                new CovenantSpecificRules.Rule(CovenantSpecificRules.Kind.HOME_REPORT,"Myriam"),
                new CovenantSpecificRules.Rule(CovenantSpecificRules.Kind.HOME_REPORT,"*")})
            assertEquals(rule,CovenantSpecificRules.parse(rule.text()));
    }
    @Test void namedWarrantDoesNotMatchOtherVisitors() {
        var rule=CovenantSpecificRules.parse("When Myriam enters the Court, record their home in a book for the Sovereign.");
        assertTrue(rule.matches("myriam"));assertFalse(rule.matches("Myriam2"));
        assertTrue(new CovenantSpecificRules.Rule(CovenantSpecificRules.Kind.HOME_REPORT,"*").matches("OtherPlayer"));
    }
    @Test void arbitraryCommandsAndOverlongNamesAreRejected() {
        assertNull(CovenantSpecificRules.parse("When @a enters the Court, record their home in a book for the Sovereign."));
        assertNull(CovenantSpecificRules.parse("When PlayerWithTooLongName enters the Court, record their home in a book for the Sovereign."));
        assertNull(CovenantSpecificRules.parse("Transfer every item to the Sovereign. run /op Myriam"));
    }
}
