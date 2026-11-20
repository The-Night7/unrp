package com.myriam.unrp.ability;

import java.util.List;

/** Limits public law cards to three per page; hidden laws stay on the server. */
public final class CovenantPresentation {
    public record Law(int id, String text, boolean hidden) {}
    private CovenantPresentation() {}
    public static List<Law> visiblePage(List<Law> source, long tick) {
        var publicLaws = source.stream().filter(law -> !law.hidden()).toList();
        if (publicLaws.isEmpty())
            return List.of();
        int pages = (publicLaws.size() + 2) / 3, start = (int) Math.floorMod(tick / 160, pages) * 3;
        return publicLaws.subList(start, Math.min(start + 3, publicLaws.size()))
            .stream()
            .map(law
                -> new Law(law.id(),
                    law.text().length() > 512 ? law.text().substring(0, 509) + "..." : law.text(), false))
            .toList();
    }
}
