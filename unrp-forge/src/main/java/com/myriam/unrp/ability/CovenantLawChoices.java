package com.myriam.unrp.ability;

import java.util.List;

public final class CovenantLawChoices {
    public record Action(String label, String sentence, CovenantParse.Viol violation) {}
    public record Penalty(String label, String clause, CovenantParse.Kind kind) {}
    public static final List<Action> ACTIONS =
        List.of(new Action("Sprint", "No running", CovenantParse.Viol.SPRINT),
            new Action("Jump", "No jumping", CovenantParse.Viol.JUMP),
            new Action("Attack", "No attacking", CovenantParse.Viol.ATTACK),
            new Action("Abilities", "No ability use", CovenantParse.Viol.ABILITY),
            new Action("Chat", "No speaking", CovenantParse.Viol.SPEAK),
            new Action("Fly", "No flying", CovenantParse.Viol.FLY),
            new Action("Break", "No breaking blocks", CovenantParse.Viol.BREAK),
            new Action("Place", "No block placement", CovenantParse.Viol.PLACE),
            new Action("Crouch", "No crouching", CovenantParse.Viol.SNEAK),
            new Action("Leave", "No leaving", CovenantParse.Viol.LEAVE));
    public static final List<Penalty> PENALTIES =
        List.of(new Penalty("Speed", "Speed", CovenantParse.Kind.STAT),
            new Penalty("Power", "Power", CovenantParse.Kind.STAT),
            new Penalty("Defense", "Defense", CovenantParse.Kind.STAT),
            new Penalty("Recovery", "Recovery", CovenantParse.Kind.STAT),
            new Penalty("Trick", "Trick", CovenantParse.Kind.STAT),
            new Penalty("Sprint", "sprint", CovenantParse.Kind.MOVE),
            new Penalty("Jump", "jump", CovenantParse.Kind.MOVE),
            new Penalty("Movement", "movement", CovenantParse.Kind.MOVE),
            new Penalty("Branch", "ability branch", CovenantParse.Kind.BRANCH),
            new Penalty("Sight", "sight", CovenantParse.Kind.SENSE),
            new Penalty("Hearing", "hearing", CovenantParse.Kind.SENSE),
            new Penalty("Touch", "touch", CovenantParse.Kind.SENSE),
            new Penalty("Memory", "memory", CovenantParse.Kind.MEMORY),
            new Penalty("Soul", "soul", CovenantParse.Kind.SOUL),
            new Penalty("Ability", "whole ability", CovenantParse.Kind.ABILITY));
    private CovenantLawChoices() {}
    public static List<Penalty> available(double level) {
        return PENALTIES.stream().filter(p -> CovenantRules.allowed(level, p.kind())).toList();
    }
    public static String text(Action action, Penalty penalty) {
        return action.sentence() + ". Penalty: " + penalty.clause();
    }
}
