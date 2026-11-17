package com.myriam.unrp.ability;

import static org.junit.jupiter.api.Assertions.*;

import com.myriam.unrp.data.AbilityEffect;
import com.myriam.unrp.data.AbilityPower;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CovenantBranchTest {
    @Test
    void appropriationDoesNotGrantUnrelatedMoves() {
        assertTrue(CovenantRules.branchAllowed(false, false, Set.of(2, 5), 2));
        assertTrue(CovenantRules.branchAllowed(false, false, Set.of(2, 5), 5));
        assertFalse(CovenantRules.branchAllowed(false, false, Set.of(2, 5), 1));
        assertTrue(CovenantRules.branchAllowed(true, false, Set.of(2), 1));
        assertTrue(CovenantRules.branchAllowed(false, true, Set.of(), 1));
    }
    @Test
    void aBranchDoesNotSmuggleInTheWholePowersPassives() {
        AbilityPower original = new AbilityPower();
        original.attributes.add(new AbilityPower.Attr());
        original.passive.add(new AbilityEffect.Eff());
        original.healEvery = 20;
        original.healAmount = 2;
        original.climb = true;
        original.drainEvery = 5;
        original.thorns = 8;
        original.stances.add("One");
        original.modes.add(new AbilityPower.Mode());
        original.moves.add(new AbilityPower.Move());
        original.icon = "unrp:textures/icons/covenant_posture.png";
        AbilityPower branch = original.branchView("source_power");
        assertEquals("source_power", branch.branchSource);
        assertSame(original.moves, branch.moves);
        assertEquals(original.icon, branch.icon);
        assertTrue(branch.attributes.isEmpty());
        assertTrue(branch.passive.isEmpty());
        assertTrue(branch.stances.isEmpty());
        assertTrue(branch.modes.isEmpty());
        assertEquals(0, branch.healEvery);
        assertEquals(0, branch.thorns);
        assertFalse(branch.climb);
    }
}
