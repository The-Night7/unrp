package com.myriam.unrp.command;

import com.myriam.unrp.data.ClaimData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LevelCommandsTest {
    private static ClaimData.Profile oc() {
        var p=new ClaimData.Profile();p.kind=ClaimData.Profile.OC;p.level=2.5;p.masteryXp=900;
        p.ocAbility="covenant";p.pot=new int[]{20,30,40,50,60};p.tier="elite";p.aura=170;return p;
    }
    @Test void setRoundsToTenthsAndPreservesBuildAndOwnership() {
        var p=oc();var potential=p.pot;
        assertEquals(5.3,LevelCommands.apply(p,5.26,false));
        assertEquals(0,p.masteryXp);assertSame(potential,p.pot);assertEquals("covenant",p.ocAbility);
        assertEquals("elite",p.tier);assertEquals(170,p.aura);
    }
    @Test void addAndSubtractReachBothSupportedEndpoints() {
        var p=oc();assertEquals(10,LevelCommands.apply(p,7.5,true));
        assertEquals(1,LevelCommands.apply(p,-9,true));
        assertEquals(1.1,LevelCommands.apply(p,.1,true));
    }
    @Test void invalidResultsDoNotChangeLevelOrMastery() {
        for(double value:new double[]{.9,10.1,Double.NaN,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY}) {
            var p=oc();assertThrows(IllegalArgumentException.class,()->LevelCommands.apply(p,value,false));
            assertEquals(2.5,p.level);assertEquals(900,p.masteryXp);
        }
        var p=oc();assertThrows(IllegalArgumentException.class,()->LevelCommands.apply(p,-2,true));assertEquals(2.5,p.level);
    }
    @Test void missingFixedAndPowerlessProfilesAreRejected() {
        assertThrows(IllegalArgumentException.class,()->LevelCommands.apply(null,5,false));
        var p=oc();p.kind=ClaimData.Profile.CHARACTER;
        assertThrows(IllegalArgumentException.class,()->LevelCommands.apply(p,5,false));assertEquals(2.5,p.level);
        p.kind=ClaimData.Profile.OC;p.lifelong=true;
        assertThrows(IllegalArgumentException.class,()->LevelCommands.apply(p,5,false));assertEquals(900,p.masteryXp);
    }
}
