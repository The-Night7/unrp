package com.myriam.unrp.ability;

import static org.junit.jupiter.api.Assertions.*;

import com.myriam.unrp.ability.CovenantParse.*;
import org.junit.jupiter.api.Test;

class CovenantParseTest {
    @Test
    void englishLaw() {
        Law l = CovenantParse.law("No one may run. Penalty: their Speed");
        assertEquals(Viol.SPRINT, l.viol());
        assertEquals(new Pen(Kind.STAT, "4"), l.pen());
    }

    @Test
    void frenchLawWithAccents() {
        Law l = CovenantParse.law("Nul ne peut sauter | pénalité : leur Défense");
        assertEquals(Viol.JUMP, l.viol());
        assertEquals(new Pen(Kind.STAT, "1"), l.pen());
    }

    @Test
    void penaltyKinds() {
        assertEquals(Kind.SOUL, CovenantParse.law("Pas de pouvoir. Pénalité : leur âme").pen().kind());
        assertEquals(Kind.MEMORY, CovenantParse.law("Ne parlez pas. Peine : mémoire").pen().kind());
        assertEquals(Kind.BRANCH, CovenantParse.law("No attacking. Penalty: an ability branch").pen().kind());
        assertEquals("sight", CovenantParse.law("Never run. Penalty: sight").pen().key());
    }

    @Test
    void unreadableLawIsRejected() {
        assertNull(CovenantParse.law("Soyez gentils"));
        assertNull(CovenantParse.law("No one may run"));
    }

    @Test
    void entryTerms() {
        var t = CovenantParse.terms("Whoever enters gives up their weapons and armor");
        assertTrue(t.contains(Term.WEAPONS) && t.contains(Term.ARMOR) && !t.contains(Term.ALL));
        assertTrue(CovenantParse.terms("tout l'inventaire").contains(Term.ALL));
    }

    @Test
    void pact() {
        Pen[] p = CovenantParse.pact("ma vie contre ta branche");
        assertEquals(Kind.LIFE, p[0].kind());
        assertEquals(Kind.BRANCH, p[1].kind());
        assertNull(CovenantParse.pact("juste un cadeau"));
    }
    @Test
    void godClausesAndNamedBranches() {
        assertEquals(Kind.ABILITY, CovenantParse.law("No running. Penalty: whole ability").pen().kind());
        assertEquals(Kind.NONE, CovenantParse.pact("nothing for your whole ability")[0].kind());
        assertEquals(Kind.STAT, CovenantParse.pen("Power").kind());
        assertEquals("4", CovenantParse.pen("Speed").key());
        assertEquals("fire", CovenantParse.pen("branch fire").key());
        assertEquals("2", CovenantParse.pen("branch: 2").key());
        assertEquals("hearing", CovenantParse.pen("hearing").key());
    }
    @Test void courtAndFlameAreNotMisreadAsRunningAndSouls() {
        assertEquals(Viol.ATTACK, CovenantParse.law("No attacking in my court. Penalty: your Speed").viol());
        assertEquals(Viol.LEAVE, CovenantParse.law("No leaving the court. Penalty: movement").viol());
        assertEquals(Viol.SPRINT, CovenantParse.law("Personne ne court. Penalty: Speed").viol());
        assertEquals(Kind.BRANCH, CovenantParse.pen("your flame branch").kind());
        assertEquals("flame", CovenantParse.pen("your flame branch").key());
        assertEquals(Kind.SOUL, CovenantParse.pen("their soul").kind());
    }
}
