package com.myriam.unrp.ability;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CovenantLawChoicesTest {
    @Test void everyButtonCombinationProducesExactlyTheIntendedServerRule() {
        for(var action:CovenantLawChoices.ACTIONS)for(var penalty:CovenantLawChoices.PENALTIES) {
            var text=CovenantLawChoices.text(action,penalty);
            var parsed=CovenantParse.law(text);
            assertNotNull(parsed,text);assertEquals(action.violation(),parsed.viol(),text);assertEquals(penalty.kind(),parsed.pen().kind(),text);
        }
        var defaultLaw=CovenantParse.law(CovenantLawChoices.text(CovenantLawChoices.ACTIONS.get(0),CovenantLawChoices.PENALTIES.get(0)));
        assertEquals("4",defaultLaw.pen().key());
    }
    @Test void flyingAndPlacementAreNotMisreadAsSpeech() {
        assertEquals(CovenantParse.Viol.FLY,CovenantParse.viol("No flying"));
        assertEquals(CovenantParse.Viol.PLACE,CovenantParse.viol("No block placement"));
        assertEquals(CovenantParse.Viol.SPEAK,CovenantParse.viol("No lying"));
        assertEquals(CovenantParse.Viol.SPEAK,CovenantParse.viol("Interdit de mentir"));
    }
    @Test void formChoicesFollowActualServerTierLimits() {
        assertEquals(8,CovenantLawChoices.available(1).size());
        assertEquals(8,CovenantLawChoices.available(3.4).size());
        assertEquals(12,CovenantLawChoices.available(3.5).size());
        assertEquals(14,CovenantLawChoices.available(5).size());
        assertEquals(15,CovenantLawChoices.available(6).size());
        for(double level:new double[]{1,2,3.5,5,6})for(var penalty:CovenantLawChoices.available(level))
            assertTrue(CovenantRules.allowed(level,penalty.kind()));
    }
}
